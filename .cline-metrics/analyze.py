#!/usr/bin/env python3
"""Aggregate Cline metrics JSONL into the 10 L&D indicators."""
import json
import re
import subprocess
from collections import defaultdict
from difflib import SequenceMatcher
from pathlib import Path

ROOT = Path(__file__).resolve().parent
EVENTS = ROOT / "events.jsonl"
COMMITS = ROOT / "commits"
FINAL = ROOT / "final"

TEST_CMD_RE = re.compile(r"\b(pytest|jest|vitest|mocha|go test|cargo test|npm test|yarn test|unittest)\b")
CODE_TOOLS = {"write_to_file", "replace_in_file", "new_file", "apply_diff"}


def load_events():
    if not EVENTS.exists():
        return []
    out = []
    for line in EVENTS.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            out.append(json.loads(line))
        except json.JSONDecodeError:
            continue
    return out


def similarity(a: str, b: str) -> float:
    return SequenceMatcher(None, a, b).ratio()


def analyze():
    events = load_events()
    tasks = defaultdict(lambda: {
        "start_ts": None,
        "first_code_ts": None,
        "first_prompt": None,
        "resume_prompts": [],
        "test_runs": [],
        "commits": [],
        "spec": None,
        "resumed": False,
    })

    last_task_id = None
    resume_next_prompt_task = None

    for ev in events:
        tid = ev.get("taskId")
        ts = ev.get("ts")
        name = ev.get("event")
        p = ev.get("payload") or {}

        if name == "TaskStart":
            tasks[tid]["start_ts"] = ts
            tasks[tid]["spec"] = p.get("task")
            last_task_id = tid
        elif name == "TaskResume":
            tasks[tid]["resumed"] = True
            resume_next_prompt_task = tid
        elif name == "UserPromptSubmit":
            if resume_next_prompt_task == tid:
                tasks[tid]["resume_prompts"].append(p.get("prompt"))
                resume_next_prompt_task = None
            if tasks[tid]["first_prompt"] is None:
                tasks[tid]["first_prompt"] = p.get("prompt")
        elif name == "PostToolUse":
            tool = p.get("tool")
            dur = p.get("durationMs")
            if tool in CODE_TOOLS and tasks[tid]["first_code_ts"] is None:
                tasks[tid]["first_code_ts"] = ts
            if tool == "execute_command":
                cmd = (p.get("parameters") or {}).get("command", "")
                if TEST_CMD_RE.search(cmd):
                    tasks[tid]["test_runs"].append({"cmd": cmd, "durationMs": dur, "ok": p.get("success")})
        elif name == "GitCommit":
            owner = last_task_id or tid
            tasks[owner]["commits"].append({"sha": ev.get("sha"), "ts": ts, "message": ev.get("message")})

    total_starts = sum(1 for t in tasks.values() if t["start_ts"])
    total_resumes = sum(1 for t in tasks.values() if t["resumed"])
    total_commits = sum(len(t["commits"]) for t in tasks.values())

    reviewed_shas = {p.stem for p in FINAL.glob("*.marker")} if FINAL.exists() else set()

    report = {
        "total_tasks": total_starts,
        "total_resumes": total_resumes,
        "rework_rate": (total_resumes / total_starts) if total_starts else 0.0,
        "total_commits": total_commits,
        "reviewed_commits": sorted(reviewed_shas),
        "tasks": {},
    }

    for tid, t in tasks.items():
        time_to_first_code = (
            t["first_code_ts"] - t["start_ts"]
            if t["first_code_ts"] and t["start_ts"] else None
        )
        last_commit_ts = t["commits"][-1]["ts"] if t["commits"] else None
        time_to_last_commit = (
            last_commit_ts - t["start_ts"]
            if last_commit_ts and t["start_ts"] else None
        )

        sims = []
        reviewed_for_task = [c for c in t["commits"] if c["sha"] in reviewed_shas]
        if reviewed_for_task and len(t["commits"]) >= 2:
            for reviewed in reviewed_for_task:
                idx = t["commits"].index(reviewed)
                if idx == 0:
                    continue
                a = (COMMITS / f"{t['commits'][0]['sha']}.patch")
                b = (COMMITS / f"{reviewed['sha']}.patch")
                if a.exists() and b.exists():
                    sims.append({
                        "from": t["commits"][0]["sha"],
                        "to": reviewed["sha"],
                        "similarity": similarity(
                            a.read_text(encoding="utf-8", errors="ignore"),
                            b.read_text(encoding="utf-8", errors="ignore"),
                        ),
                    })

        report["tasks"][tid] = {
            "spec_analysis": t["spec"],
            "first_prompt": t["first_prompt"],
            "resumed": t["resumed"],
            "resume_prompts": t["resume_prompts"],
            "test_runs_count": len(t["test_runs"]),
            "test_runs_total_ms": sum((r["durationMs"] or 0) for r in t["test_runs"]),
            "test_runs": t["test_runs"],
            "time_to_first_code_ms": time_to_first_code,
            "time_to_last_commit_ms": time_to_last_commit,
            "commits": [c["sha"] for c in t["commits"]],
            "reviewed_vs_initial_similarity": sims,
        }

    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    analyze()
