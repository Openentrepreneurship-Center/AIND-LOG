#!/usr/bin/env python3
"""기존 git commit 히스토리를 .cline-metrics 로 백필.

post-commit hook 이 설치되기 전에 만들어진 commit 들을 사후에 동일 형태로 기록한다.
이미 .cline-metrics/commits/<sha>.patch 가 있는 commit 은 skip (idempotent).

사용법:
    python3 .cline-metrics/backfill.py [target_repo_root]

target_repo_root 미지정 시 환경변수 TARGET_REPO_ROOT, 그 다음 기본값
(.cline-metrics 의 부모/decapet-official/backend) 를 순서대로 사용.
"""
from __future__ import annotations

import json
import os
import subprocess
import sys
import time
from pathlib import Path

METRICS = Path(__file__).resolve().parent
COMMITS_DIR = METRICS / "commits"
FINAL_DIR = METRICS / "final"
EVENTS_PATH = METRICS / "events.jsonl"


def resolve_target() -> Path:
    if len(sys.argv) > 1:
        return Path(sys.argv[1]).resolve()
    env = os.environ.get("TARGET_REPO_ROOT")
    if env:
        return Path(env).resolve()
    return (METRICS.parent / "decapet-official" / "backend").resolve()


def run(cmd: list[str], cwd: Path, decode: bool = True):
    r = subprocess.run(cmd, capture_output=True, cwd=str(cwd))
    if r.returncode != 0:
        return "" if decode else b""
    if not decode:
        return r.stdout
    try:
        return r.stdout.decode("utf-8")
    except UnicodeDecodeError:
        return ""


def list_commits(repo: Path) -> list[tuple[str, int, str]]:
    """오래된 → 최신 순. (sha, unix_ts, message)"""
    out = run(["git", "log", "--reverse", "--format=%H%x09%at%x09%s"], repo)
    rows: list[tuple[str, int, str]] = []
    for line in out.splitlines():
        parts = line.split("\t", 2)
        if len(parts) >= 3 and parts[1].isdigit():
            rows.append((parts[0], int(parts[1]), parts[2]))
    return rows


def write_patch(repo: Path, sha: str) -> Path:
    out = COMMITS_DIR / f"{sha}.patch"
    data = run(["git", "show", sha, "--format=fuller"], repo, decode=False)
    out.write_bytes(data)
    return out


def write_snapshot(repo: Path, sha: str) -> Path:
    out = COMMITS_DIR / f"{sha}.snapshot.json"
    changed = [
        f for f in run(
            ["git", "diff-tree", "--no-commit-id", "-r", "--name-only", "--root", sha],
            repo,
        ).splitlines() if f.strip()
    ]
    all_files = [
        f for f in run(["git", "ls-tree", "-r", "--name-only", sha], repo).splitlines()
        if f.strip()
    ]
    snapshot = {"sha": sha, "changed_files": changed, "all_files": {}}
    for fp in all_files:
        snapshot["all_files"][fp] = run(["git", "show", f"{sha}:{fp}"], repo)
    out.write_text(json.dumps(snapshot, ensure_ascii=False), encoding="utf-8")
    return out


def append_event(ts_ms: int, sha: str, msg: str) -> None:
    record = {"ts": ts_ms, "event": "GitCommit", "sha": sha, "message": msg}
    with EVENTS_PATH.open("a", encoding="utf-8") as f:
        f.write(json.dumps(record, ensure_ascii=False) + "\n")


def existing_event_shas() -> set[str]:
    if not EVENTS_PATH.exists():
        return set()
    seen: set[str] = set()
    with EVENTS_PATH.open("r", encoding="utf-8") as f:
        for line in f:
            try:
                d = json.loads(line)
            except json.JSONDecodeError:
                continue
            if d.get("event") == "GitCommit" and d.get("sha"):
                seen.add(d["sha"])
    return seen


def main() -> int:
    repo = resolve_target()
    if not (repo / ".git").exists():
        print(f"ERR: {repo} 는 git repo 가 아닙니다.", file=sys.stderr)
        return 1

    COMMITS_DIR.mkdir(parents=True, exist_ok=True)
    FINAL_DIR.mkdir(parents=True, exist_ok=True)

    commits = list_commits(repo)
    print(f"target = {repo}")
    print(f"found  = {len(commits)} commits")

    already_event = existing_event_shas()
    new_count = skip_count = 0

    for i, (sha, ts, msg) in enumerate(commits, 1):
        patch_path = COMMITS_DIR / f"{sha}.patch"
        snap_path = COMMITS_DIR / f"{sha}.snapshot.json"
        need_patch = not patch_path.exists()
        need_snap = not snap_path.exists()
        need_event = sha not in already_event

        if not (need_patch or need_snap or need_event):
            skip_count += 1
            continue

        if need_patch:
            write_patch(repo, sha)
        if need_snap:
            write_snapshot(repo, sha)
        if "[reviewed]" in msg:
            (FINAL_DIR / f"{sha}.marker").touch(exist_ok=True)
        if need_event:
            append_event(ts * 1000, sha, msg)

        # /api/commits 가 patch 파일 mtime 으로 정렬하므로 commit 시간으로 맞춤
        for p in (patch_path, snap_path):
            if p.exists():
                os.utime(p, (ts, ts))

        new_count += 1
        if i % 20 == 0 or i == len(commits):
            print(f"  [{i}/{len(commits)}] processed ({new_count} new, {skip_count} skipped)")

    # events.jsonl 정렬: ts 오름차순으로 재기록 (백필 후 시간순 일관성)
    if EVENTS_PATH.exists():
        lines = []
        with EVENTS_PATH.open("r", encoding="utf-8") as f:
            for line in f:
                try:
                    lines.append(json.loads(line))
                except json.JSONDecodeError:
                    pass
        lines.sort(key=lambda d: d.get("ts", 0))
        with EVENTS_PATH.open("w", encoding="utf-8") as f:
            for d in lines:
                f.write(json.dumps(d, ensure_ascii=False) + "\n")

    print(f"\nDONE: {new_count} new, {skip_count} skipped, total events.jsonl lines synced.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
