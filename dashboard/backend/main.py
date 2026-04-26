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
EVENTS_PATH = ROOT / "events.jsonl"
FINAL_DIR = ROOT / "final"

# 유사도 분석 대상 repo (git 명령이 실행될 cwd). 환경변수로 다른 프로젝트 전환 가능.
TARGET_REPO_ROOT = Path(
    os.environ.get(
        "TARGET_REPO_ROOT",
        str(ROOT.parent / "decapet-official" / "backend"),
    )
)

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
        "file_paths": set(), "last_event": "", "last_result": "",
        "event_count": 0,
    })

    event_list: list[dict] = []
    event_type_counts: dict[str, int] = defaultdict(int)
    tool_counts: dict[str, int] = defaultdict(int)
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
            "git_sha": ev.get("sha", ""),
            "git_message": ev.get("message", ""),
            "clineVersion": ev.get("clineVersion", ""),
            "raw_payload": payload,
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

    for tid, t in tasks.items():
        if t["start_ts"] is None:
            continue
        total_starts += 1
        if t["resumed"]:
            total_resumes += 1

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

    return {
        "summary": {
            "total_events": len(events),
            "total_tasks": total_starts,
            "total_resumes": total_resumes,
            "rework_rate": round(total_resumes / total_starts * 100, 1) if total_starts else 0,
            "reviewed_commits": len(reviewed_shas),
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

    # .patch 파일 기준으로 커밋 목록 구성
    patch_files = sorted(commits_dir.glob("*.patch"), key=lambda p: p.stat().st_mtime, reverse=True)

    result = []
    for pf in patch_files:
        sha = pf.stem
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


def _git_file_at(sha: str, filepath: str) -> str:
    """특정 커밋 SHA 에서 파일 내용을 git show 로 읽어 반환."""
    import subprocess
    r = subprocess.run(
        ["git", "show", f"{sha}:{filepath}"],
        capture_output=True, text=True,
        cwd=str(TARGET_REPO_ROOT),
    )
    return r.stdout if r.returncode == 0 else ""


def _git_commits_for_file(filepath: str) -> list[tuple[str, int, str]]:
    """filepath 를 수정한 커밋을 시간 순(오래된 것 → 최신)으로 반환.

    reflog 를 포함해 orphan 브랜치 이전 커밋도 검색.
    반환: [(sha, unix_ts, message), ...]
    """
    import subprocess
    r = subprocess.run(
        ["git", "log", "--all", "--reflog",
         "--format=%H %at %s", "--", filepath],
        capture_output=True, text=True,
        cwd=str(TARGET_REPO_ROOT),
    )
    rows: list[tuple[str, int, str]] = []
    for line in r.stdout.strip().splitlines():
        parts = line.split(" ", 2)
        if len(parts) >= 2:
            sha_full = parts[0]
            ts_int = int(parts[1]) if parts[1].isdigit() else 0
            msg = parts[2] if len(parts) > 2 else ""
            rows.append((sha_full, ts_int, msg))
    rows.sort(key=lambda x: x[1])          # 시간 오름차순
    # sha 중복 제거 (같은 SHA 가 여러 reflog 에 등장할 수 있음)
    seen: set[str] = set()
    unique: list[tuple[str, int, str]] = []
    for row in rows:
        if row[0] not in seen:
            seen.add(row[0])
            unique.append(row)
    return unique


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
    summary="TARGET_REPO_ROOT 의 폴더 계층",
    description="git ls-tree -r HEAD 결과를 폴더 트리로 변환해 반환합니다. 유사도 분석 대상 파일 선택 UI 에서 사용.",
)
def get_repo_tree() -> dict:
    import subprocess
    r = subprocess.run(
        ["git", "ls-tree", "-r", "--name-only", "HEAD"],
        capture_output=True, text=True,
        cwd=str(TARGET_REPO_ROOT),
    )
    if r.returncode != 0:
        raise HTTPException(
            status_code=500,
            detail=f"git ls-tree 실패 (TARGET_REPO_ROOT={TARGET_REPO_ROOT}): {r.stderr.strip()}",
        )
    paths = [line.strip() for line in r.stdout.splitlines() if line.strip()]
    return {"root": str(TARGET_REPO_ROOT), "file_count": len(paths), "tree": _build_tree(paths)}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
