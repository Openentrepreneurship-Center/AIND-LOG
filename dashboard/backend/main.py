"""Cline Metrics Dashboard – FastAPI backend.

events.jsonl (append-only 로그)을 실시간으로 파싱해
대시보드에 집계 데이터를 제공합니다.

Swagger UI : http://localhost:8000/docs
ReDoc      : http://localhost:8000/redoc
"""
from __future__ import annotations

import asyncio
import json
import os
import re
from collections import defaultdict
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any, AsyncGenerator

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

# ── paths ──────────────────────────────────────────────────────────────────
ROOT = Path(__file__).resolve().parent.parent.parent / ".cline-metrics"
# EVENTS_FILE 환경변수로 다른 events 파일 지정 가능 (예: events_decapet.jsonl)
_events_filename = os.environ.get("EVENTS_FILE", "events_decapet.jsonl")
EVENTS_PATH = ROOT / _events_filename
FINAL_DIR = ROOT / "final"

# 유사도 분석 대상 repo 표시 경로 (UI/응답용)
TARGET_REPO_ROOT = Path(
    os.environ.get(
        "TARGET_REPO_ROOT",
        str(ROOT.parent / "decapet-official" / "backend"),
    )
)
# 새 AIND-LOG commit 의 snapshot 은 모든 파일 경로가 'decapet-official/backend/...' 처럼 prefix 가 붙는다.
# 백필된 decapet 자체 commit 의 snapshot 은 prefix 가 없다 ('src/...').
# 이 둘을 모두 매칭하기 위한 prefix.
TARGET_REPO_PREFIX = os.environ.get("TARGET_REPO_PREFIX", "decapet-official/backend/")
COMMITS_DIR = ROOT / "commits"

# ── constants ───────────────────────────────────────────────────────────────
KST = timezone(timedelta(hours=9))
TEST_CMD_RE = re.compile(
    r"\b(pytest|jest|vitest|mocha|go\s+test|cargo\s+test|npm\s+test|yarn\s+test|unittest)\b"
)
CODE_TOOLS = {"write_to_file", "replace_in_file", "new_file", "apply_diff"}

# ── Pydantic response models ─────────────────────────────────────────────────

class SummaryModel(BaseModel):
    """전체 집계 요약 지표."""
    total_events: int = Field(..., description="events.jsonl 에 기록된 전체 이벤트 수")
    total_tasks: int = Field(..., description="TaskStart 이벤트 기준 총 작업 수")
    total_resumes: int = Field(..., description="TaskResume 이벤트가 발생한 작업 수")
    rework_rate: float = Field(..., description="재업무율(%) = total_resumes / total_tasks × 100")
    reviewed_commits: int = Field(..., description="커밋 메시지에 [reviewed] 태그가 포함된 검수 완료 커밋 수")
    total_writes: int = Field(..., description="전체 write_to_file 호출 횟수")
    total_reads: int = Field(..., description="전체 read_file 호출 횟수")
    file_rework_count: int = Field(..., description="동일 파일을 2회 이상 write_to_file 한 파일 수 (재작업 추정치)")
    file_rework_rate: float = Field(..., description="파일 재작업률(%) = file_rework_count / unique_written_files × 100")
    read_write_ratio: float = Field(..., description="읽기/쓰기 비율 = total_reads / total_writes (낮을수록 효율적 코드 생성)")
    model_usage: dict[str, int] = Field(..., description="모델별 이벤트 발생 횟수 {'provider/slug': count}")
    top_model: str = Field(..., description="가장 많이 사용된 모델 (provider/slug)")
    unique_models: int = Field(..., description="사용된 고유 모델 수")


class TestRunModel(BaseModel):
    """단위 테스트 1회 실행 기록."""
    command: str = Field(..., description="실행된 테스트 커맨드 (pytest / jest / npm test 등)")
    duration_ms: float = Field(..., description="실행 소요 시간 (밀리초)")
    success: Any = Field(None, description="성공 여부 (true/false/null)")


class TaskModel(BaseModel):
    """taskId 단위 집계 결과."""
    taskId: str = Field(..., description="Cline이 부여한 Task 고유 ID")
    start_ts: int | None = Field(None, description="Task 시작 epoch timestamp (ms)")
    start_kst: str = Field(..., description="Task 시작 시각 (KST, YYYY-MM-DD HH:MM:SS)")
    end_kst: str = Field(..., description="Task 마지막 이벤트 시각 (KST)")
    duration_sec: float | None = Field(None, description="Task 총 소요 시간 (초) = 마지막이벤트 - 시작")
    status: str = Field(..., description="Task 최종 상태: 완료됨 / 취소됨 / 재개됨 / 진행중")
    initial_task: str = Field(..., description="TaskStart 시점의 초기 요청 텍스트")
    first_prompt: str = Field(..., description="UserPromptSubmit 중 첫 번째 프롬프트")
    event_count: int = Field(..., description="이 Task에 속한 전체 이벤트 수")
    write_count: int = Field(..., description="write_to_file 도구 호출 횟수 (코드 생성/수정 횟수)")
    read_count: int = Field(..., description="read_file 도구 호출 횟수")
    exec_count: int = Field(..., description="execute_command 도구 호출 횟수")
    time_to_first_code_sec: float | None = Field(
        None,
        description="코드 생성 소요시간(초): TaskStart → 첫 번째 코드 작성 도구 호출(PostToolUse)까지"
    )
    test_runs_count: int = Field(..., description="단위 테스트 실행 횟수 (pytest/jest/npm test 등 감지)")
    test_total_sec: float | None = Field(None, description="전체 테스트 실행 총 소요 시간 (초)")
    test_pct_of_duration: float | None = Field(
        None,
        description="테스트 소요시간 비중(%) = test_total_sec / duration_sec × 100"
    )
    resume_count: int = Field(..., description="이 Task가 재개(TaskResume)된 횟수")
    model: str = Field(..., description="사용된 AI 모델 (provider/slug 형식)")
    tools_used: list[str] = Field(..., description="이 Task에서 사용된 도구 목록 (중복 제거)")
    file_paths: list[str] = Field(..., description="수정/읽기된 파일 경로 목록 (최대 10개)")
    last_result: str = Field(..., description="TaskComplete 시 모델이 반환한 결과 요약 (최대 200자)")


class EventModel(BaseModel):
    """단일 이벤트 상세."""
    idx: int = Field(..., description="events.jsonl 내 줄 번호 (1-based)")
    taskId: str = Field(..., description="소속 Task ID (없으면 빈 문자열)")
    event: str = Field(
        ...,
        description=(
            "이벤트 종류: TaskStart | TaskResume | TaskCancel | TaskComplete | "
            "UserPromptSubmit | PreToolUse | PostToolUse | GitCommit | PreCompact"
        )
    )
    ts: int | None = Field(None, description="이벤트 발생 epoch timestamp (ms)")
    ts_kst: str = Field(..., description="이벤트 발생 시각 (KST, YYYY-MM-DD HH:MM:SS)")
    tool: str = Field(..., description="호출된 도구명 (PreToolUse/PostToolUse 한정, 나머지는 빈 문자열)")
    path: str = Field(..., description="도구 파라미터의 파일 경로 (absolutePath 또는 path)")
    command: str = Field(..., description="execute_command 도구의 실행 커맨드 (최대 200자)")
    success: Any = Field(None, description="PostToolUse 도구 성공 여부 (true/false/null)")
    exec_sec: float | None = Field(None, description="도구 실행 소요 시간 (초, PostToolUse 한정)")
    model: str = Field(..., description="이벤트 발생 시점의 AI 모델 (provider/slug)")
    git_sha: str = Field(..., description="GitCommit 이벤트의 커밋 SHA")
    git_message: str = Field(..., description="GitCommit 이벤트의 커밋 메시지")
    clineVersion: str = Field(..., description="이벤트 발생 시점의 Cline 버전")
    raw_payload: dict[str, Any] = Field(..., description="Cline 훅이 전달한 원본 payload JSON 전체")
    prompt: str = Field(..., description="UserPromptSubmit 이벤트의 프롬프트 텍스트")
    initial_task: str = Field(..., description="TaskStart 이벤트의 초기 요청 텍스트")
    result_preview: str = Field(..., description="TaskComplete 결과 미리보기 (최대 500자)")
    content_preview: str = Field(..., description="write_to_file 파라미터 content 미리보기 (최대 300자)")
    requires_approval: Any = Field(None, description="도구 실행 전 사용자 승인 필요 여부")
    previous_state: dict[str, Any] = Field(..., description="TaskResume 시 이전 대화 상태 (messageCount, lastMessageTs 등)")
    completion_status: str = Field(..., description="TaskCancel/TaskComplete 의 completionStatus 값")


class CountItemModel(BaseModel):
    """이름-개수 집계 항목."""
    name: str = Field(..., description="이벤트 종류 또는 도구명")
    count: int = Field(..., description="발생 횟수")


class CountsModel(BaseModel):
    """이벤트 종류·도구 사용 집계."""
    event_types: list[CountItemModel] = Field(..., description="이벤트 종류별 발생 횟수 (이름순 정렬)")
    tools: list[CountItemModel] = Field(..., description="도구 사용 횟수 (많은 순 정렬)")


class DashboardDataModel(BaseModel):
    """대시보드 전체 응답 스키마."""
    summary: SummaryModel
    tasks: list[TaskModel]
    events: list[EventModel]
    counts: CountsModel


class CommitSnapshotFile(BaseModel):
    """커밋 스냅샷 내 단일 파일."""
    path: str = Field(..., description="파일 경로 (저장소 루트 기준 상대 경로)")
    content: str = Field(..., description="해당 커밋 시점의 파일 전체 내용")
    is_changed: bool = Field(..., description="이 커밋에서 변경된 파일 여부")


class CommitModel(BaseModel):
    """커밋 단위 정보 (diff + 전체 스냅샷 포함)."""
    sha: str = Field(..., description="커밋 SHA (40자)")
    sha_short: str = Field(..., description="커밋 SHA 단축형 (7자)")
    message: str = Field(..., description="커밋 메시지 전체")
    ts: int | None = Field(None, description="커밋 발생 epoch timestamp (ms), GitCommit 이벤트 기준")
    ts_kst: str = Field(..., description="커밋 시각 (KST)")
    is_reviewed: bool = Field(..., description="커밋 메시지에 [reviewed] 태그 포함 여부")
    task_id: str | None = Field(None, description="연관된 Task ID (events.jsonl 의 GitCommit 이벤트 기준 직전 taskId)")
    has_patch: bool = Field(..., description=".patch 파일(diff) 존재 여부")
    has_snapshot: bool = Field(..., description=".snapshot.json(전체 스냅샷) 존재 여부")
    patch_content: str = Field(..., description="git diff 내용 전체 (변경분)")
    changed_files: list[str] = Field(..., description="이 커밋에서 변경된 파일 경로 목록")
    snapshot_files: list[CommitSnapshotFile] = Field(
        ...,
        description="커밋 시점 전체 파일 스냅샷 목록 (changed_files 포함 repo 내 모든 파일)"
    )


class HealthModel(BaseModel):
    """헬스체크 응답."""
    status: str = Field(..., description="서버 상태 (ok)")
    events_file_exists: bool = Field(..., description="events.jsonl 파일 존재 여부")
    events_file_path: str = Field(..., description="events.jsonl 절대 경로")
    total_lines: int = Field(..., description="현재 events.jsonl 총 라인 수")


# ── app ──────────────────────────────────────────────────────────────────────
app = FastAPI(
    title="Cline Metrics Dashboard API",
    description="""
## 개요

Cline(Dev Agent)의 **Hook 기반 활동 로그**(`events.jsonl`)를 파싱하여
대시보드에 집계 데이터를 제공하는 API입니다.

## 데이터 수집 구조

```
Cline 이벤트 발생
  → .clinerules/hooks/_record.py 호출
  → .cline-metrics/events.jsonl 에 1줄 append
  → 이 API가 파일 변경을 감지 (2초 폴링)
  → SSE 스트림으로 대시보드에 push
```

## 수집 이벤트 종류

| 이벤트 | 설명 |
|--------|------|
| `TaskStart` | 새 Task 시작 (초기 요청 포함) |
| `TaskResume` | 중단된 Task 재개 |
| `TaskCancel` | Task 취소 |
| `TaskComplete` | Task 완료 |
| `UserPromptSubmit` | 사용자 프롬프트 제출 |
| `PreToolUse` | 도구 호출 직전 |
| `PostToolUse` | 도구 호출 완료 후 (소요시간 포함) |
| `GitCommit` | git commit 발생 |
| `PreCompact` | 컨텍스트 압축 직전 |

## 주요 지표

- **Code 생성 소요시간**: `TaskStart` → 첫 `write_to_file` PostToolUse 까지
- **단위 테스트 횟수·시간**: `execute_command` 중 테스트 커맨드(pytest/jest 등) 탐지
- **재업무율**: `TaskResume` 횟수 / `TaskStart` 횟수
- **검수 완료 커밋**: 커밋 메시지에 `[reviewed]` 태그 포함 여부
""",
    version="1.0.0",
    contact={
        "name": "Cline Metrics",
    },
    openapi_tags=[
        {
            "name": "dashboard",
            "description": "대시보드용 집계 데이터. 전체 데이터를 한 번에 반환합니다.",
        },
        {
            "name": "stream",
            "description": "SSE(Server-Sent Events) 실시간 스트림. events.jsonl 변경 시 자동 push.",
        },
        {
            "name": "events",
            "description": "개별 이벤트 조회 및 필터링.",
        },
        {
            "name": "tasks",
            "description": "Task 단위 집계 조회.",
        },
        {
            "name": "commits",
            "description": "커밋 단위 diff + 전체 소스 스냅샷 조회.",
        },
        {
            "name": "system",
            "description": "서버 상태 및 설정 확인.",
        },
    ],
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── helpers ──────────────────────────────────────────────────────────────────
def _ts(value: Any) -> int | None:
    try:
        return int(value)
    except (TypeError, ValueError):
        return None


def _kst(ts_ms: int | None) -> str:
    if ts_ms is None:
        return ""
    return datetime.fromtimestamp(ts_ms / 1000, tz=KST).strftime("%Y-%m-%d %H:%M:%S")


def _sec(ms: int | None) -> float | None:
    if ms is None:
        return None
    return round(ms / 1000, 1)


# ── data processing ──────────────────────────────────────────────────────────
def load_events() -> list[dict]:
    """events.jsonl 파일을 읽어 파싱된 이벤트 목록을 반환합니다."""
    if not EVENTS_PATH.exists():
        return []
    items: list[dict] = []
    with EVENTS_PATH.open("r", encoding="utf-8", errors="ignore") as f:
        for raw in f:
            line = raw.strip()
            if not line:
                continue
            try:
                items.append(json.loads(line))
            except json.JSONDecodeError:
                continue
    return items


def process(events: list[dict]) -> dict:
    """
    raw 이벤트 목록을 받아 대시보드용 집계 데이터로 변환합니다.

    반환 구조:
    - summary : 전체 집계 지표
    - tasks   : taskId 단위 집계 목록
    - events  : 전체 이벤트 상세 목록
    - counts  : 이벤트 종류·도구 사용 빈도
    """
    tasks: dict[str, dict] = defaultdict(lambda: {
        "start_ts": None, "end_ts": None,
        "first_prompt": "", "initial_task": "",
        "model_provider": "", "model_slug": "",
        "resumed": False, "canceled": False, "completed": False,
        "resume_count": 0, "cancel_count": 0, "complete_count": 0,
        "prompts": [], "tools_used": [],
        "tool_duration_ms": 0,
        "pre_tool_count": 0, "post_tool_count": 0,
        "success_tool_count": 0,
        "write_count": 0, "read_count": 0, "exec_count": 0,
        "test_runs": [], "first_code_ts": None,
        "file_paths": set(), "file_write_counts": defaultdict(int),
        "last_event": "", "last_result": "",
        "event_count": 0,
    })

    event_list: list[dict] = []
    event_type_counts: dict[str, int] = defaultdict(int)
    tool_counts: dict[str, int] = defaultdict(int)
    model_usage: dict[str, int] = defaultdict(int)
    last_task_id: str | None = None

    for idx, ev in enumerate(events, start=1):
        tid: str = ev.get("taskId") or ""
        event: str = ev.get("event", "")
        ts = _ts(ev.get("ts"))
        payload: dict = ev.get("payload") or {}
        model: dict = ev.get("model") or {}
        provider = model.get("provider", "")
        slug = model.get("slug", "")

        tool_name = payload.get("tool") or payload.get("toolName") or ""
        params: dict = payload.get("parameters") or {}
        command: str = params.get("command", "")
        success = payload.get("success", "")
        exec_raw = payload.get("durationMs") or payload.get("executionTimeMs")
        path_val = params.get("absolutePath") or params.get("path") or ""

        event_type_counts[event] += 1
        if event in ("PreToolUse", "PostToolUse") and tool_name:
            tool_counts[tool_name] += 1
        if provider and slug:
            model_usage[f"{provider}/{slug}"] += 1

        # GitCommit 이벤트는 payload 가 없으므로 snapshot 데이터로 보강
        git_sha = ev.get("sha", "")
        enriched_payload = payload
        if event == "GitCommit" and git_sha and not payload:
            snap = _load_snapshot(git_sha)
            changed = snap.get("changed_files", [])
            all_files_count = len(snap.get("all_files", {}))
            enriched_payload = {
                "sha": git_sha,
                "message": ev.get("message", ""),
                "changed_files": changed,
                "changed_files_count": len(changed),
                "total_files_in_snapshot": all_files_count,
                "has_snapshot": bool(snap),
            }

        event_list.append({
            "idx": idx,
            "taskId": tid,
            "event": event,
            "ts": ts,
            "ts_kst": _kst(ts),
            "tool": tool_name,
            "path": path_val,
            "command": command[:200],
            "success": success,
            "exec_sec": _sec(exec_raw) if isinstance(exec_raw, (int, float)) else None,
            "model": f"{provider}/{slug}" if provider else "",
            "git_sha": git_sha,
            "git_message": ev.get("message", ""),
            "clineVersion": ev.get("clineVersion", ""),
            "raw_payload": enriched_payload,
            "prompt": payload.get("prompt", ""),
            "initial_task": (payload.get("taskMetadata") or {}).get("initialTask") or payload.get("task", ""),
            "result_preview": str((payload.get("taskMetadata") or {}).get("result", "") or payload.get("result", ""))[:500],
            "content_preview": (params.get("content", "") or "")[:300],
            "requires_approval": params.get("requiresApproval", ""),
            "previous_state": payload.get("previousState") or {},
            "completion_status": (payload.get("taskMetadata") or {}).get("completionStatus", ""),
        })

        if not tid:
            continue

        t = tasks[tid]
        t["event_count"] += 1
        t["last_event"] = event
        if provider:
            t["model_provider"] = provider
        if slug:
            t["model_slug"] = slug
        if ts:
            if t["start_ts"] is None:
                t["start_ts"] = ts
            t["end_ts"] = ts

        if event == "TaskStart":
            t["initial_task"] = (
                (payload.get("taskMetadata") or {}).get("initialTask")
                or payload.get("task", "")
            )
            last_task_id = tid
        elif event == "UserPromptSubmit":
            p = payload.get("prompt", "")
            if not t["first_prompt"]:
                t["first_prompt"] = p
            t["prompts"].append(p)
        elif event == "TaskResume":
            t["resumed"] = True
            t["resume_count"] += 1
        elif event == "TaskCancel":
            t["canceled"] = True
            t["cancel_count"] += 1
        elif event == "TaskComplete":
            t["completed"] = True
            t["complete_count"] += 1
            t["last_result"] = str(
                (payload.get("taskMetadata") or {}).get("result", "")
            )[:200]
        elif event == "PreToolUse":
            t["pre_tool_count"] += 1
            if tool_name and tool_name not in t["tools_used"]:
                t["tools_used"].append(tool_name)
        elif event == "PostToolUse":
            t["post_tool_count"] += 1
            if success is True:
                t["success_tool_count"] += 1
            if tool_name == "write_to_file":
                t["write_count"] += 1
                if path_val:
                    t["file_write_counts"][path_val] += 1
            if tool_name in ("read_file", "read_file_content"):
                t["read_count"] += 1
            if tool_name == "execute_command":
                t["exec_count"] += 1
                if TEST_CMD_RE.search(command):
                    t["test_runs"].append({
                        "command": command[:120],
                        "duration_ms": exec_raw or 0,
                        "success": success,
                    })
            if tool_name in CODE_TOOLS and t["first_code_ts"] is None:
                t["first_code_ts"] = ts
            if isinstance(exec_raw, (int, float)):
                t["tool_duration_ms"] += exec_raw
            if path_val:
                t["file_paths"].add(path_val)

    reviewed_shas = {
        p.stem for p in FINAL_DIR.glob("*.marker")
    } if FINAL_DIR.exists() else set()

    task_list: list[dict] = []
    total_starts = 0
    total_resumes = 0
    total_writes = 0
    total_reads = 0
    unique_written_files: set[str] = set()
    rework_file_count = 0

    for tid, t in tasks.items():
        if t["start_ts"] is None:
            continue
        total_starts += 1
        if t["resumed"]:
            total_resumes += 1
        total_writes += t["write_count"]
        total_reads += t["read_count"]
        for fp, cnt in t["file_write_counts"].items():
            unique_written_files.add(fp)
            if cnt > 1:
                rework_file_count += 1

        dur_ms = (t["end_ts"] - t["start_ts"]) if t["start_ts"] and t["end_ts"] else None
        code_ms = (
            t["first_code_ts"] - t["start_ts"]
            if t["first_code_ts"] and t["start_ts"]
            else None
        )
        test_ms = sum(int(r.get("duration_ms") or 0) for r in t["test_runs"])
        test_pct = (
            round(test_ms / dur_ms * 100, 1) if dur_ms and dur_ms > 0 else None
        )

        if t["canceled"]:
            status = "취소됨"
        elif t["completed"]:
            status = "완료됨"
        elif t["resumed"]:
            status = "재개됨"
        else:
            status = "진행중"

        task_list.append({
            "taskId": tid,
            "start_ts": t["start_ts"],
            "start_kst": _kst(t["start_ts"]),
            "end_kst": _kst(t["end_ts"]),
            "duration_sec": _sec(dur_ms),
            "status": status,
            "initial_task": t["initial_task"],
            "first_prompt": t["first_prompt"],
            "event_count": t["event_count"],
            "write_count": t["write_count"],
            "read_count": t["read_count"],
            "exec_count": t["exec_count"],
            "time_to_first_code_sec": _sec(code_ms),
            "test_runs_count": len(t["test_runs"]),
            "test_total_sec": _sec(test_ms),
            "test_pct_of_duration": test_pct,
            "resume_count": t["resume_count"],
            "model": f"{t['model_provider']}/{t['model_slug']}",
            "tools_used": t["tools_used"],
            "file_paths": sorted(t["file_paths"])[:10],
            "last_result": t["last_result"],
        })

    task_list.sort(key=lambda x: x["start_ts"] or 0, reverse=True)

    n_unique = len(unique_written_files)
    sorted_models = sorted(model_usage.items(), key=lambda x: -x[1])
    top_model = sorted_models[0][0] if sorted_models else ""
    return {
        "summary": {
            "total_events": len(events),
            "total_tasks": total_starts,
            "total_resumes": total_resumes,
            "rework_rate": round(total_resumes / total_starts * 100, 1) if total_starts else 0,
            "reviewed_commits": len(reviewed_shas),
            "total_writes": total_writes,
            "total_reads": total_reads,
            "file_rework_count": rework_file_count,
            "file_rework_rate": round(rework_file_count / n_unique * 100, 1) if n_unique else 0.0,
            "read_write_ratio": round(total_reads / max(total_writes, 1), 2),
            "model_usage": dict(model_usage),
            "top_model": top_model,
            "unique_models": len(model_usage),
        },
        "tasks": task_list,
        "events": event_list,
        "counts": {
            "event_types": [
                {"name": k, "count": v}
                for k, v in sorted(event_type_counts.items())
            ],
            "tools": [
                {"name": k, "count": v}
                for k, v in sorted(tool_counts.items(), key=lambda x: -x[1])
            ],
        },
    }


# ── SSE generator ─────────────────────────────────────────────────────────────
async def _sse_generator() -> AsyncGenerator[str, None]:
    """events.jsonl mtime 변경을 2초 간격으로 폴링하여 SSE 데이터를 push합니다."""
    last_mtime: float | None = None
    events = load_events()
    data = process(events)
    yield f"data: {json.dumps(data, ensure_ascii=False)}\n\n"
    last_mtime = EVENTS_PATH.stat().st_mtime if EVENTS_PATH.exists() else None

    while True:
        await asyncio.sleep(2)
        try:
            if not EVENTS_PATH.exists():
                continue
            mtime = EVENTS_PATH.stat().st_mtime
            if mtime != last_mtime:
                last_mtime = mtime
                events = load_events()
                data = process(events)
                yield f"data: {json.dumps(data, ensure_ascii=False)}\n\n"
        except Exception:
            pass


# ── routes ───────────────────────────────────────────────────────────────────

@app.get(
    "/health",
    response_model=HealthModel,
    tags=["system"],
    summary="서버 헬스체크",
    description="서버 상태와 events.jsonl 파일 존재 여부를 확인합니다.",
)
def health() -> dict:
    exists = EVENTS_PATH.exists()
    lines = 0
    if exists:
        with EVENTS_PATH.open("r", encoding="utf-8", errors="ignore") as f:
            lines = sum(1 for l in f if l.strip())
    return {
        "status": "ok",
        "events_file_exists": exists,
        "events_file_path": str(EVENTS_PATH),
        "total_lines": lines,
    }


@app.get(
    "/api/data",
    response_model=DashboardDataModel,
    tags=["dashboard"],
    summary="전체 대시보드 데이터 조회",
    description="""
events.jsonl 전체를 파싱하여 대시보드에 필요한 모든 집계 데이터를 반환합니다.

반환 구조:
- **summary** : 전체 이벤트·Task 수, 재업무율, 검수 커밋 수
- **tasks**   : taskId 단위 집계 (소요시간, 코드생성시간, 테스트 지표 등)
- **events**  : 전체 이벤트 상세 (raw payload 포함)
- **counts**  : 이벤트 종류·도구 사용 빈도
""",
)
def get_data() -> dict:
    return process(load_events())


@app.get(
    "/api/tasks",
    response_model=list[TaskModel],
    tags=["tasks"],
    summary="Task 목록 조회",
    description="TaskStart 이벤트 기준으로 집계된 Task 목록을 반환합니다. 시작 시각 내림차순 정렬.",
)
def get_tasks() -> list[dict]:
    return process(load_events())["tasks"]


@app.get(
    "/api/tasks/{task_id}",
    response_model=TaskModel,
    tags=["tasks"],
    summary="특정 Task 조회",
    description="taskId로 특정 Task의 집계 데이터를 조회합니다.",
    responses={404: {"description": "해당 taskId를 찾을 수 없음"}},
)
def get_task(task_id: str) -> dict:
    from fastapi import HTTPException
    tasks = process(load_events())["tasks"]
    for t in tasks:
        if t["taskId"] == task_id:
            return t
    raise HTTPException(status_code=404, detail=f"taskId '{task_id}' 를 찾을 수 없습니다.")


@app.get(
    "/api/events",
    response_model=list[EventModel],
    tags=["events"],
    summary="이벤트 목록 조회 (필터·페이지네이션)",
    description="""
events.jsonl 의 전체 이벤트를 반환합니다.

- `event_type` 으로 특정 이벤트 종류만 필터링할 수 있습니다.
- `task_id` 로 특정 Task의 이벤트만 필터링할 수 있습니다.
- `limit` / `offset` 으로 페이지네이션이 가능합니다.
""",
)
def get_events(
    event_type: str | None = Query(
        None,
        description="필터할 이벤트 종류 (예: TaskStart, PostToolUse, GitCommit)",
        example="PostToolUse",
    ),
    task_id: str | None = Query(
        None,
        description="필터할 Task ID",
    ),
    limit: int = Query(
        100,
        ge=1,
        le=1000,
        description="반환할 최대 이벤트 수 (1~1000, 기본 100)",
    ),
    offset: int = Query(
        0,
        ge=0,
        description="건너뛸 이벤트 수 (페이지네이션용, 기본 0)",
    ),
) -> list[dict]:
    events = process(load_events())["events"]
    if event_type:
        events = [e for e in events if e["event"] == event_type]
    if task_id:
        events = [e for e in events if e["taskId"] == task_id]
    return events[offset: offset + limit]


@app.get(
    "/api/summary",
    response_model=SummaryModel,
    tags=["dashboard"],
    summary="요약 지표만 조회",
    description="전체 집계 요약 지표(총 이벤트 수, Task 수, 재업무율 등)만 가볍게 조회합니다.",
)
def get_summary() -> dict:
    return process(load_events())["summary"]


@app.get(
    "/api/counts",
    response_model=CountsModel,
    tags=["dashboard"],
    summary="이벤트 종류·도구 사용 빈도 조회",
    description="이벤트 종류별 발생 횟수와 도구별 사용 횟수를 반환합니다. 차트 데이터 용도.",
)
def get_counts() -> dict:
    return process(load_events())["counts"]


@app.get(
    "/api/stream",
    tags=["stream"],
    summary="실시간 SSE 스트림",
    description="""
**Server-Sent Events(SSE)** 스트림입니다.

- 연결 즉시 현재 전체 데이터를 한 번 전송합니다.
- 이후 `events.jsonl` 파일이 변경될 때마다(2초 폴링) 전체 데이터를 다시 push합니다.
- 응답 Content-Type: `text/event-stream`
- 클라이언트 사용법: `const es = new EventSource('/api/stream'); es.onmessage = (e) => JSON.parse(e.data)`

> Swagger UI에서는 SSE 스트림을 직접 테스트할 수 없습니다. 브라우저 콘솔 또는 curl을 사용하세요.
> ```bash
> curl -N http://localhost:8000/api/stream
> ```
""",
    response_description="text/event-stream 형식의 DashboardData JSON",
)
async def stream() -> StreamingResponse:
    return StreamingResponse(
        _sse_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
            "Connection": "keep-alive",
        },
    )


# ── commit helpers ────────────────────────────────────────────────────────────
def load_commits() -> list[dict]:
    """
    .cline-metrics/commits/ 디렉터리에서 .patch + .snapshot.json 파일을 읽어
    커밋 목록을 반환합니다. events.jsonl 의 GitCommit 이벤트와 매핑하여
    타임스탬프·taskId 도 함께 제공합니다.
    """
    commits_dir = ROOT / "commits"
    if not commits_dir.exists():
        return []

    reviewed_shas = {p.stem for p in FINAL_DIR.glob("*.marker")} if FINAL_DIR.exists() else set()

    # events.jsonl 에서 GitCommit 이벤트 추출
    git_events: dict[str, dict] = {}
    for ev in load_events():
        if ev.get("event") == "GitCommit":
            sha = ev.get("sha", "")
            git_events[sha] = {
                "ts": _ts(ev.get("ts")),
                "message": ev.get("message", ""),
                "task_id": ev.get("taskId") or None,
            }

    # events.jsonl 에 있는 SHA만 허용 (현재 프로젝트 기준 커밋만 포함)
    allowed_shas = set(git_events.keys()) if git_events else None

    # .patch 파일 기준으로 커밋 목록 구성
    patch_files = sorted(commits_dir.glob("*.patch"), key=lambda p: p.stat().st_mtime, reverse=True)

    result = []
    for pf in patch_files:
        sha = pf.stem  # .patch 단일 확장자이므로 stem = SHA 그대로
        # 현재 events.jsonl 에 없는 SHA는 건너뜀 (다른 프로젝트 커밋 혼입 방지)
        if allowed_shas is not None and sha not in allowed_shas:
            continue
        patch_text = pf.read_text(encoding="utf-8", errors="ignore")

        snapshot_path = commits_dir / f"{sha}.snapshot.json"
        snapshot_data: dict = {}
        if snapshot_path.exists():
            try:
                snapshot_data = json.loads(snapshot_path.read_text(encoding="utf-8", errors="ignore"))
            except json.JSONDecodeError:
                pass

        git_ev = git_events.get(sha, {})
        ts = git_ev.get("ts")
        message = git_ev.get("message") or _extract_message_from_patch(patch_text)
        changed_files: list[str] = snapshot_data.get("changed_files", [])
        all_files_map: dict[str, str] = snapshot_data.get("all_files", {})

        snapshot_files = [
            {
                "path": fp,
                "content": content,
                "is_changed": fp in changed_files,
            }
            for fp, content in all_files_map.items()
        ]

        result.append({
            "sha": sha,
            "sha_short": sha[:7],
            "message": message,
            "ts": ts,
            "ts_kst": _kst(ts),
            "is_reviewed": sha in reviewed_shas,
            "task_id": git_ev.get("task_id"),
            "has_patch": True,
            "has_snapshot": snapshot_path.exists(),
            "patch_content": patch_text,
            "changed_files": changed_files,
            "snapshot_files": snapshot_files,
        })

    return result


def _extract_message_from_patch(patch_text: str) -> str:
    """patch 파일 헤더에서 커밋 메시지를 추출합니다."""
    lines = patch_text.splitlines()
    msg_lines = []
    in_msg = False
    for line in lines:
        if line.startswith("    ") and not line.startswith("diff "):
            in_msg = True
            msg_lines.append(line.strip())
        elif in_msg and line.startswith("diff "):
            break
    return "\n".join(msg_lines).strip()


# ── commit routes ─────────────────────────────────────────────────────────────

@app.get(
    "/api/commits",
    response_model=list[CommitModel],
    tags=["commits"],
    summary="커밋 목록 조회 (diff + 전체 스냅샷)",
    description="""
`.cline-metrics/commits/` 디렉터리에 저장된 커밋 데이터를 반환합니다.

각 커밋마다:
- **`patch_content`**: `git show HEAD` 결과 (변경분 diff 전체)
- **`snapshot_files`**: 커밋 시점의 모든 파일 전체 내용 (`all_files` 스냅샷)
- **`changed_files`**: 이 커밋에서 실제 변경된 파일 경로 목록
- **`is_reviewed`**: 커밋 메시지에 `[reviewed]` 태그 포함 여부

> **저장 시점**: `git commit` 실행 시 `.cline-metrics/post-commit` 훅이 자동으로 저장합니다.
""",
)
def get_commits() -> list[dict]:
    return load_commits()


@app.get(
    "/api/commits/{sha}",
    response_model=CommitModel,
    tags=["commits"],
    summary="특정 커밋 상세 조회",
    description="SHA로 특정 커밋의 diff 와 전체 소스 스냅샷을 조회합니다. SHA 전체(40자) 또는 단축형(7자) 모두 지원합니다.",
    responses={404: {"description": "해당 SHA 커밋을 찾을 수 없음"}},
)
def get_commit(sha: str) -> dict:
    from fastapi import HTTPException
    commits = load_commits()
    for c in commits:
        if c["sha"].startswith(sha) or c["sha_short"] == sha:
            return c
    raise HTTPException(status_code=404, detail=f"SHA '{sha}' 에 해당하는 커밋을 찾을 수 없습니다.")


@app.get(
    "/api/commits/{sha}/diff",
    tags=["commits"],
    summary="특정 커밋 diff(변경분)만 조회",
    description="patch 파일 텍스트를 plain text로 반환합니다.",
    response_description="git diff 텍스트",
    responses={404: {"description": "해당 커밋 없음"}},
)
def get_commit_diff(sha: str) -> dict:
    from fastapi import HTTPException
    commits = load_commits()
    for c in commits:
        if c["sha"].startswith(sha):
            return {"sha": c["sha"], "patch": c["patch_content"]}
    raise HTTPException(status_code=404, detail=f"SHA '{sha}' 에 해당하는 커밋을 찾을 수 없습니다.")


@app.get(
    "/api/commits/{sha}/snapshot",
    tags=["commits"],
    summary="특정 커밋 전체 스냅샷 조회",
    description="커밋 시점의 모든 파일 전체 내용을 반환합니다.",
    responses={404: {"description": "해당 커밋 없음"}},
)
def get_commit_snapshot(sha: str) -> dict:
    from fastapi import HTTPException
    commits = load_commits()
    for c in commits:
        if c["sha"].startswith(sha):
            return {
                "sha": c["sha"],
                "changed_files": c["changed_files"],
                "files": {f["path"]: f["content"] for f in c["snapshot_files"]},
            }
    raise HTTPException(status_code=404, detail=f"SHA '{sha}' 에 해당하는 커밋을 찾을 수 없습니다.")


# ── similarity ────────────────────────────────────────────────────────────────

class SimilarityScores(BaseModel):
    """L1~L4 유사도 점수 묶음."""
    L1: float = Field(..., description="Levenshtein — 문자 단위 표면 유사도 [0.0~1.0]")
    L2: float = Field(..., description="BLEU — 토큰 n-gram 유사도 [0.0~1.0]")
    L3: float = Field(..., description="구조적 유사도 — 라인 단위 SequenceMatcher [0.0~1.0]")
    L4: float = Field(..., description="의미론적 유사도 — TF-IDF char cosine [0.0~1.0]")


class SimilarityResult(BaseModel):
    """커밋 간 코드 유사도 측정 결과."""
    sha: str = Field(..., description="현재 커밋 SHA (full)")
    sha_short: str = Field(..., description="현재 커밋 SHA (7자)")
    prev_sha: str = Field(..., description="이전 커밋 SHA (없으면 빈 문자열)")
    prev_sha_short: str = Field(..., description="이전 커밋 SHA (7자, 없으면 빈 문자열)")
    message: str = Field(..., description="커밋 메시지")
    ts_kst: str = Field(..., description="커밋 시각 (KST)")
    file: str = Field(..., description="비교 대상 파일 경로")
    scores: SimilarityScores = Field(..., description="L1~L4 유사도 점수")
    old_size: int = Field(..., description="이전 파일 크기 (bytes)")
    new_size: int = Field(..., description="현재 파일 크기 (bytes)")
    changed: bool = Field(..., description="파일 내용이 실제로 변경됐는지 여부")


def _load_snapshot(sha: str) -> dict:
    """commits/<sha>.snapshot.json 을 읽어 반환. 없으면 {}."""
    p = COMMITS_DIR / f"{sha}.snapshot.json"
    if not p.exists():
        return {}
    try:
        return json.loads(p.read_text(encoding="utf-8"))
    except Exception:
        return {}


def _path_candidates(filepath: str) -> list[str]:
    """백필 snapshot(짧은 경로) / 신규 AIND-LOG snapshot(긴 경로) 양쪽 매칭용 후보."""
    cands = [filepath]
    if TARGET_REPO_PREFIX:
        if filepath.startswith(TARGET_REPO_PREFIX):
            cands.append(filepath[len(TARGET_REPO_PREFIX):])
        else:
            cands.append(TARGET_REPO_PREFIX + filepath)
    return cands


def _git_file_at(sha: str, filepath: str) -> str:
    """commit snapshot 에서 파일 내용을 추출 (snapshot 기반)."""
    snap = _load_snapshot(sha)
    all_files = snap.get("all_files", {})
    for c in _path_candidates(filepath):
        if c in all_files:
            return all_files[c]
    return ""


def _git_commits_for_file(filepath: str) -> list[tuple[str, int, str]]:
    """events.jsonl 의 GitCommit + snapshot.changed_files 매칭으로 시간순 반환.
    반환: [(sha, unix_ts_sec, message), ...]
    """
    if not EVENTS_PATH.exists():
        return []
    rows: list[tuple[str, int, str]] = []
    seen: set[str] = set()
    with EVENTS_PATH.open("r", encoding="utf-8") as f:
        for line in f:
            try:
                d = json.loads(line)
            except json.JSONDecodeError:
                continue
            if d.get("event") != "GitCommit":
                continue
            sha = d.get("sha")
            if not sha or sha in seen:
                continue
            snap = _load_snapshot(sha)
            changed = snap.get("changed_files", [])
            if not any(c in changed for c in _path_candidates(filepath)):
                continue
            seen.add(sha)
            ts_ms = int(d.get("ts") or 0)
            rows.append((sha, ts_ms // 1000, d.get("message", "")))
    rows.sort(key=lambda x: x[1])
    return rows


_similarity_cache: dict[str, list[dict]] = {}


@app.get(
    "/api/similarity",
    response_model=list[SimilarityResult],
    tags=["similarity"],
    summary="커밋별 코드 유사도 (L1~L4)",
    description="""
커밋 히스토리를 순서대로 탐색하며 연속된 두 커밋 간 코드 유사도를 L1~L4 레이어로 측정합니다.

원본 알고리즘: [AIND_SIMILARITY](https://github.com/Openentrepreneurship-Center/AIND_SIMILARITY)

| 레이어 | 원본 알고리즘 | 이 프로젝트 적응 |
|--------|--------------|-----------------|
| **L1** | Levenshtein.ratio | 동일 (rapidfuzz) |
| **L2** | CrystalBLEU (javalang) | BLEU + JS regex 토크나이저 |
| **L3** | TSED (tree-sitter-java APTED) | SequenceMatcher 라인 구조 유사도 |
| **L4** | CodeBERTScore F1 (codebert-java) | TF-IDF char n-gram cosine |

- `file` 파라미터는 필수입니다. TARGET_REPO_ROOT 기준 상대 경로로 지정하세요.
- 결과는 메모리에 캐시되며, `?refresh=true` 로 재계산할 수 있습니다.
""",
)
def get_similarity(
    file: str = Query(
        ...,
        min_length=1,
        description="유사도를 측정할 파일 경로 (TARGET_REPO_ROOT 기준 상대 경로)",
        example="src/main/java/com/decapet/.../SomeService.java",
    ),
    refresh: bool = Query(
        False,
        description="캐시를 무시하고 재계산할지 여부",
    ),
) -> list[dict]:
    global _similarity_cache
    cache_key = file

    if not refresh and cache_key in _similarity_cache:
        return _similarity_cache[cache_key]

    from similarity import compute_all

    commits = _git_commits_for_file(file)
    if not commits:
        raise HTTPException(
            status_code=404,
            detail=f"'{file}' 에 해당하는 커밋 히스토리를 찾을 수 없습니다 (TARGET_REPO_ROOT={TARGET_REPO_ROOT}).",
        )
    results: list[dict] = []

    for i, (sha, ts_int, msg) in enumerate(commits):
        new_code = _git_file_at(sha, file)
        prev_sha = ""
        prev_sha_short = ""
        old_code = ""

        if i > 0:
            prev_sha = commits[i - 1][0]
            prev_sha_short = prev_sha[:7]
            old_code = _git_file_at(prev_sha, file)

        scores = compute_all(old_code, new_code) if old_code else {
            "L1": 0.0, "L2": 0.0, "L3": 0.0, "L4": 0.0
        }

        results.append({
            "sha": sha,
            "sha_short": sha[:7],
            "prev_sha": prev_sha,
            "prev_sha_short": prev_sha_short,
            "message": msg,
            "ts_kst": _kst(ts_int * 1000) if ts_int else "",
            "file": file,
            "scores": scores,
            "old_size": len(old_code.encode("utf-8")),
            "new_size": len(new_code.encode("utf-8")),
            "changed": old_code != new_code,
        })

    _similarity_cache[cache_key] = results
    return results


SOURCE_EXTS = {".java", ".kt", ".py", ".ts", ".tsx", ".js", ".jsx", ".html", ".css", ".go", ".rs", ".scala", ".cs", ".cpp", ".c", ".rb", ".php", ".swift"}


class ProjectSimilarityResult(BaseModel):
    """커밋 단위 프로젝트 전체 유사도."""
    sha: str = Field(..., description="커밋 SHA")
    sha_short: str = Field(..., description="커밋 SHA 단축형 (7자)")
    prev_sha: str = Field(..., description="이전 커밋 SHA (없으면 빈 문자열)")
    prev_sha_short: str = Field(..., description="이전 커밋 SHA 단축형")
    message: str = Field(..., description="커밋 메시지")
    ts_kst: str = Field(..., description="커밋 시각 (KST)")
    files_changed: int = Field(..., description="이 커밋에서 변경된 소스 파일 수")
    total_files: int = Field(..., description="전체 소스 파일 수 (스냅샷 기준)")
    changed_size: int = Field(..., description="변경 파일 총 크기 (bytes)")
    total_size: int = Field(..., description="전체 소스 파일 총 크기 (bytes)")
    scores: SimilarityScores = Field(..., description="프로젝트 전체 가중 유사도 (변경 비중 반영)")
    raw_scores: SimilarityScores = Field(..., description="변경 파일만의 평균 유사도 (비중 미반영)")


_project_sim_cache: list[dict] | None = None


def _is_source(path: str) -> bool:
    if "." not in path.split("/")[-1]:
        return False
    ext = "." + path.rsplit(".", 1)[-1].lower()
    return ext in SOURCE_EXTS


def _strip_prefix(path: str) -> str:
    if TARGET_REPO_PREFIX and path.startswith(TARGET_REPO_PREFIX):
        return path[len(TARGET_REPO_PREFIX):]
    return path


@app.get(
    "/api/similarity/project",
    response_model=list[ProjectSimilarityResult],
    tags=["similarity"],
    summary="프로젝트 전체 단위 코드 유사도",
    description="""
커밋마다 **변경된 소스 파일들의 유사도**를 집계해 프로젝트 전체 변화율을 측정합니다.

### 계산 방식
1. 각 커밋에서 변경된 소스 파일(`changed_files`)을 추출합니다.
2. 파일별로 이전 버전 ↔ 현재 버전을 L1~L4로 측정합니다.
3. **파일 크기 가중 평균**으로 `raw_scores`(변경 파일 평균)를 계산합니다.
4. 변경 비중(`changed_size / total_size`)을 반영해 `scores`(전체 프로젝트 가중치)를 계산합니다.
   - 소수 파일만 변경된 커밋 → 높은 scores (대부분 코드가 그대로)
   - 많은 파일이 대폭 변경 → 낮은 scores

- 소스 파일 확장자: `.java .kt .py .ts .tsx .js .html .css .go .rs` 등
- 결과는 메모리에 캐시됩니다. `?refresh=true`로 재계산할 수 있습니다.
""",
)
def get_project_similarity(
    refresh: bool = Query(False, description="캐시 무시 여부"),
) -> list[dict]:
    global _project_sim_cache
    if not refresh and _project_sim_cache is not None:
        return _project_sim_cache

    from similarity import compute_all

    # 모든 커밋을 시간순 정렬
    all_commits = load_commits()
    if not all_commits:
        raise HTTPException(404, "커밋 데이터가 없습니다.")
    commits_asc = sorted(all_commits, key=lambda c: c["ts"] or 0)

    results: list[dict] = []
    for i, commit in enumerate(commits_asc):
        sha = commit["sha"]
        prev_sha = commits_asc[i - 1]["sha"] if i > 0 else ""
        prev_sha_short = prev_sha[:7] if prev_sha else ""

        # 이번 커밋의 all_files (경로 정규화)
        curr_map: dict[str, str] = {}
        for sf in commit.get("snapshot_files", []):
            short = _strip_prefix(sf["path"])
            if _is_source(short):
                curr_map[short] = sf["content"]

        total_size = sum(len(v.encode("utf-8")) for v in curr_map.values())
        total_files = len(curr_map)

        if i == 0 or not prev_sha:
            results.append({
                "sha": sha, "sha_short": sha[:7],
                "prev_sha": "", "prev_sha_short": "",
                "message": commit["message"], "ts_kst": commit["ts_kst"],
                "files_changed": 0, "total_files": total_files,
                "changed_size": 0, "total_size": total_size,
                "scores": {"L1": 1.0, "L2": 1.0, "L3": 1.0, "L4": 1.0},
                "raw_scores": {"L1": 1.0, "L2": 1.0, "L3": 1.0, "L4": 1.0},
            })
            continue

        # 이전 커밋 all_files
        prev_commit = commits_asc[i - 1]
        prev_map: dict[str, str] = {}
        for sf in prev_commit.get("snapshot_files", []):
            short = _strip_prefix(sf["path"])
            if _is_source(short):
                prev_map[short] = sf["content"]

        # changed_files 정규화
        changed = [_strip_prefix(p) for p in commit.get("changed_files", [])]
        changed_source = [p for p in changed if _is_source(p)]

        if not changed_source:
            results.append({
                "sha": sha, "sha_short": sha[:7],
                "prev_sha": prev_sha, "prev_sha_short": prev_sha_short,
                "message": commit["message"], "ts_kst": commit["ts_kst"],
                "files_changed": 0, "total_files": total_files,
                "changed_size": 0, "total_size": total_size,
                "scores": {"L1": 1.0, "L2": 1.0, "L3": 1.0, "L4": 1.0},
                "raw_scores": {"L1": 1.0, "L2": 1.0, "L3": 1.0, "L4": 1.0},
            })
            continue

        # 파일별 유사도 계산 (크기 가중)
        weighted: dict[str, float] = {"L1": 0.0, "L2": 0.0, "L3": 0.0, "L4": 0.0}
        changed_weight = 0
        for fp in changed_source:
            old = prev_map.get(fp, "")
            new = curr_map.get(fp, "")
            if not old and not new:
                continue
            sc = compute_all(old, new)
            w = len((new or old).encode("utf-8"))
            for k in weighted:
                weighted[k] += sc[k] * w
            changed_weight += w

        if changed_weight == 0:
            raw_scores = {"L1": 1.0, "L2": 1.0, "L3": 1.0, "L4": 1.0}
        else:
            raw_scores = {k: round(v / changed_weight, 4) for k, v in weighted.items()}

        # 프로젝트 전체 가중치 반영
        # project_score = 1 - (1 - raw_score) * (changed_size / total_size)
        ratio = changed_weight / max(total_size, 1)
        scores = {
            k: round(1.0 - (1.0 - raw_scores[k]) * ratio, 4)
            for k in raw_scores
        }

        results.append({
            "sha": sha, "sha_short": sha[:7],
            "prev_sha": prev_sha, "prev_sha_short": prev_sha_short,
            "message": commit["message"], "ts_kst": commit["ts_kst"],
            "files_changed": len(changed_source),
            "total_files": total_files,
            "changed_size": changed_weight,
            "total_size": total_size,
            "scores": scores,
            "raw_scores": raw_scores,
        })

    _project_sim_cache = results
    return results


class FirstLastSimilarity(BaseModel):
    """첫 번째 ↔ 마지막 커밋 유사도 비교 결과."""
    file: str = Field(..., description="분석 파일 경로")
    first_sha: str = Field(..., description="최초 커밋 SHA")
    first_sha_short: str = Field(..., description="최초 커밋 SHA (7자)")
    first_message: str = Field(..., description="최초 커밋 메시지")
    first_ts_kst: str = Field(..., description="최초 커밋 시각 (KST)")
    last_sha: str = Field(..., description="최신 커밋 SHA")
    last_sha_short: str = Field(..., description="최신 커밋 SHA (7자)")
    last_message: str = Field(..., description="최신 커밋 메시지")
    last_ts_kst: str = Field(..., description="최신 커밋 시각 (KST)")
    total_commits: int = Field(..., description="해당 파일의 전체 커밋 수")
    first_size: int = Field(..., description="최초 커밋 파일 크기 (bytes)")
    last_size: int = Field(..., description="최신 커밋 파일 크기 (bytes)")
    scores: SimilarityScores = Field(..., description="최초↔최신 L1~L4 유사도 점수")
    avg_step_scores: SimilarityScores = Field(..., description="모든 연속 스텝의 L1~L4 평균 (단계별 평균 변화량)")


@app.get(
    "/api/similarity/first-last",
    response_model=FirstLastSimilarity,
    tags=["similarity"],
    summary="첫 커밋 ↔ 마지막 커밋 유사도",
    description="""
파일의 **최초 커밋**과 **가장 최신 커밋** 간 전체 코드 변화를 L1~L4로 측정합니다.

- `avg_step_scores`: 연속 커밋 간 평균 유사도 (낮을수록 단계마다 큰 변화가 있었음을 의미)
- `scores`: 처음 ↔ 끝 직접 비교 (전체 누적 변화량)
""",
    responses={404: {"description": "해당 파일의 커밋 히스토리 없음"}},
)
def get_similarity_first_last(
    file: str = Query(..., description="분석할 파일 경로"),
    refresh: bool = Query(False, description="캐시 무시 여부"),
) -> dict:
    from similarity import compute_all

    commits = _git_commits_for_file(file)
    if len(commits) < 2:
        raise HTTPException(
            status_code=404,
            detail=f"'{file}' 의 커밋이 2개 미만입니다.",
        )

    first_sha, first_ts, first_msg = commits[0]
    last_sha, last_ts, last_msg = commits[-1]
    first_code = _git_file_at(first_sha, file)
    last_code = _git_file_at(last_sha, file)

    scores = compute_all(first_code, last_code) if first_code and last_code else {"L1":0.0,"L2":0.0,"L3":0.0,"L4":0.0}

    # 단계별 평균 계산 (캐시 재활용)
    cache_key = file
    step_results = _similarity_cache.get(cache_key)
    if not step_results or refresh:
        step_results_raw: list[dict] = []
        for i, (sha, ts_int, msg) in enumerate(commits):
            if i == 0:
                continue
            old_code = _git_file_at(commits[i-1][0], file)
            new_code = _git_file_at(sha, file)
            if old_code and new_code:
                step_results_raw.append(compute_all(old_code, new_code))
        if step_results_raw:
            avg = {
                layer: round(sum(r[layer] for r in step_results_raw) / len(step_results_raw), 4)
                for layer in ["L1","L2","L3","L4"]
            }
        else:
            avg = {"L1":0.0,"L2":0.0,"L3":0.0,"L4":0.0}
    else:
        step_only = [r for r in step_results if r["prev_sha"] != ""]
        if step_only:
            avg = {
                layer: round(sum(r["scores"][layer] for r in step_only) / len(step_only), 4)
                for layer in ["L1","L2","L3","L4"]
            }
        else:
            avg = {"L1":0.0,"L2":0.0,"L3":0.0,"L4":0.0}

    return {
        "file": file,
        "first_sha": first_sha,
        "first_sha_short": first_sha[:7],
        "first_message": first_msg,
        "first_ts_kst": _kst(first_ts * 1000) if first_ts else "",
        "last_sha": last_sha,
        "last_sha_short": last_sha[:7],
        "last_message": last_msg,
        "last_ts_kst": _kst(last_ts * 1000) if last_ts else "",
        "total_commits": len(commits),
        "first_size": len(first_code.encode("utf-8")),
        "last_size": len(last_code.encode("utf-8")),
        "scores": scores,
        "avg_step_scores": avg,
    }


def _build_tree(paths: list[str]) -> dict:
    """평면 파일 경로 리스트를 폴더 계층 dict 로 변환."""
    root: dict = {"name": "", "type": "dir", "path": "", "children": []}
    for p in sorted(paths):
        parts = p.split("/")
        cur = root
        for i, part in enumerate(parts):
            is_file = (i == len(parts) - 1)
            existing = next(
                (c for c in cur["children"] if c["name"] == part and c["type"] == ("file" if is_file else "dir")),
                None,
            )
            if existing:
                cur = existing
                continue
            node: dict = {
                "name": part,
                "type": "file" if is_file else "dir",
                "path": "/".join(parts[: i + 1]),
            }
            if not is_file:
                node["children"] = []
            cur["children"].append(node)
            cur = node
    return root


@app.get(
    "/api/repo/tree",
    tags=["repo"],
    summary="추적 대상 폴더 계층",
    description="가장 최신 snapshot 의 all_files 키들로 폴더 트리를 구성합니다. TARGET_REPO_PREFIX 가 있으면 그 prefix 로 시작하는 파일만 노출 (prefix 제거된 짧은 경로로 표시).",
)
def get_repo_tree() -> dict:
    if not COMMITS_DIR.exists():
        raise HTTPException(500, "commits 디렉토리가 없습니다 (.cline-metrics/commits 비어있음).")

    # events.jsonl 에 있는 SHA 목록만 사용 (현재 프로젝트 기준 스냅샷만 선택)
    valid_shas: set[str] = set()
    if EVENTS_PATH.exists():
        with EVENTS_PATH.open("r", encoding="utf-8", errors="ignore") as f:
            for line in f:
                try:
                    d = json.loads(line)
                    if d.get("event") == "GitCommit" and d.get("sha"):
                        valid_shas.add(d["sha"])
                except Exception:
                    pass

    snaps = sorted(COMMITS_DIR.glob("*.snapshot.json"), key=lambda p: p.stat().st_mtime, reverse=True)
    if not snaps:
        raise HTTPException(500, "snapshot.json 파일이 없습니다.")

    # valid_shas 에 속하는 스냅샷 중 가장 최신 것 선택
    # NOTE: p.stem 은 "abc123.snapshot" 이므로 .snapshot.json 제거 필요
    chosen = None
    if valid_shas:
        for p in snaps:
            snap_sha = p.name.replace(".snapshot.json", "")
            if snap_sha in valid_shas:
                chosen = p
                break
    if chosen is None:
        chosen = snaps[0]

    try:
        snap = json.loads(chosen.read_text(encoding="utf-8"))
    except Exception as e:
        raise HTTPException(500, f"snapshot 파싱 실패: {e}")
    all_paths = sorted(snap.get("all_files", {}).keys())
    if TARGET_REPO_PREFIX:
        filtered = [p[len(TARGET_REPO_PREFIX):] for p in all_paths if p.startswith(TARGET_REPO_PREFIX)]
        all_paths = filtered if filtered else all_paths
    return {"root": str(TARGET_REPO_ROOT), "file_count": len(all_paths), "tree": _build_tree(all_paths)}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
