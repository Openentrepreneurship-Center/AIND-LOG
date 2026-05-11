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
from collections import defaultdict, deque
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any, AsyncGenerator

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

# ── paths ──────────────────────────────────────────────────────────────────
ROOT = Path(__file__).resolve().parent.parent.parent / ".cline-metrics"
# EVENTS_FILE 환경변수로 다른 events 파일 override 가능 (기본은 events.jsonl)
_events_filename = os.environ.get("EVENTS_FILE", "events.jsonl")
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

# ── Auto-Approve 도구 카테고리 매핑 (Cline 공식 문서 기준) ───────────────────
# https://docs.cline.bot 의 Auto Approve & YOLO Mode 문서 참조
TOOL_CATEGORY: dict[str, str] = {
    # 파일 읽기 (Read project/all files) — 기본적으로 항상 자동, requiresApproval 없음
    "read_file": "파일읽기",
    "read_file_content": "파일읽기",
    "list_files": "파일읽기",
    "list_directory": "파일읽기",
    "list_files_recursive": "파일읽기",
    "search_files": "파일읽기",
    "list_code_definition_names": "파일읽기",
    "get_file_info": "파일읽기",
    # 파일 편집 (Edit project/all files) — 기본 승인 필요, Auto-Approve 가능
    "write_to_file": "파일편집",
    "replace_in_file": "파일편집",
    "new_file": "파일편집",
    "apply_diff": "파일편집",
    "create_file": "파일편집",
    # 명령 실행 (Execute safe/all commands)
    "execute_command": "명령실행",
    "run_command": "명령실행",
    # 브라우저 (Use the browser)
    "browser_action": "브라우저",
    "web_fetch": "브라우저",
    "web_search": "브라우저",
    "fetch": "브라우저",
    # MCP (Use MCP servers) — tool_name에 mcp_ prefix 또는 mcp_server_name 포함
}

def _tool_category(tool_name: str) -> str:
    """도구명으로 Cline Auto-Approve 카테고리를 반환합니다."""
    if not tool_name:
        return "기타"
    cat = TOOL_CATEGORY.get(tool_name)
    if cat:
        return cat
    # MCP 도구 휴리스틱: 소문자+밑줄 조합이지만 위 목록에 없으면 MCP로 간주
    if "_" in tool_name and tool_name not in CODE_TOOLS:
        return "MCP서버"
    return "기타"

# ── 모델별 토큰 가격표 (per 1M tokens, USD, 2025년 기준 예시) ──────────────────
# 실제 계약 가격과 다를 수 있으며 추정에만 사용됩니다.
MODEL_PRICE_TABLE: dict[str, dict[str, float]] = {
    # Anthropic — Claude Sonnet 계열
    "claude-sonnet-4-7":    {"input": 3.0,   "output": 15.0,  "cache_r": 0.30, "cache_w": 3.75},
    "claude-sonnet-4.6":    {"input": 3.0,   "output": 15.0,  "cache_r": 0.30, "cache_w": 3.75},
    "claude-sonnet-4.5":    {"input": 3.0,   "output": 15.0,  "cache_r": 0.30, "cache_w": 3.75},
    "claude-3-5-sonnet":    {"input": 3.0,   "output": 15.0,  "cache_r": 0.30, "cache_w": 3.75},
    "claude-3.5-sonnet":    {"input": 3.0,   "output": 15.0,  "cache_r": 0.30, "cache_w": 3.75},
    "claude-3-sonnet":      {"input": 3.0,   "output": 15.0,  "cache_r": 0.30, "cache_w": 3.75},
    # Anthropic — Claude Haiku 계열
    "claude-3-5-haiku":     {"input": 0.80,  "output": 4.0,   "cache_r": 0.08, "cache_w": 1.0},
    "claude-3.5-haiku":     {"input": 0.80,  "output": 4.0,   "cache_r": 0.08, "cache_w": 1.0},
    "claude-3-haiku":       {"input": 0.25,  "output": 1.25,  "cache_r": 0.03, "cache_w": 0.30},
    # Anthropic — Claude Opus 계열
    "claude-opus-4-7":      {"input": 15.0,  "output": 75.0,  "cache_r": 1.50, "cache_w": 18.75},
    "claude-3-opus":        {"input": 15.0,  "output": 75.0,  "cache_r": 1.50, "cache_w": 18.75},
    "claude-3.5-opus":      {"input": 15.0,  "output": 75.0,  "cache_r": 1.50, "cache_w": 18.75},
    # OpenAI — GPT-4o 계열
    "gpt-4o":               {"input": 2.5,   "output": 10.0,  "cache_r": 1.25, "cache_w": 0.0},
    "gpt-4o-mini":          {"input": 0.15,  "output": 0.60,  "cache_r": 0.075,"cache_w": 0.0},
    "gpt-4-turbo":          {"input": 10.0,  "output": 30.0,  "cache_r": 0.0,  "cache_w": 0.0},
    "o1":                   {"input": 15.0,  "output": 60.0,  "cache_r": 7.5,  "cache_w": 0.0},
    "o1-mini":              {"input": 1.10,  "output": 4.40,  "cache_r": 0.55, "cache_w": 0.0},
    # Google — Gemini 계열
    "gemini-2.0-flash":     {"input": 0.10,  "output": 0.40,  "cache_r": 0.025,"cache_w": 0.0},
    "gemini-1.5-pro":       {"input": 1.25,  "output": 5.0,   "cache_r": 0.0,  "cache_w": 0.0},
    "gemini-1.5-flash":     {"input": 0.075, "output": 0.30,  "cache_r": 0.0,  "cache_w": 0.0},
    # 기본값 (알 수 없는 모델)
    "_default":             {"input": 3.0,   "output": 15.0,  "cache_r": 0.30, "cache_w": 3.75},
}

def _model_price(model_slug: str) -> tuple[str, dict[str, float]]:
    """모델 slug를 가격표 키에 매핑하고 (matched_key, price_dict)를 반환합니다."""
    slug = (model_slug or "").lower()
    # 정확 매칭
    for key, price in MODEL_PRICE_TABLE.items():
        if key != "_default" and key in slug:
            return key, price
    # 부분 매칭 (claude-sonnet, gpt-4o 등)
    for key, price in MODEL_PRICE_TABLE.items():
        if key != "_default":
            parts = key.split("-")
            if all(p in slug for p in parts if len(p) > 2):
                return key, price
    return "_default", MODEL_PRICE_TABLE["_default"]

# ── Pydantic response models ─────────────────────────────────────────────────

class SummaryModel(BaseModel):
    """전체 집계 요약 지표."""
    total_events: int = Field(..., description="events.jsonl 에 기록된 전체 이벤트 수")
    total_hook_events: int = Field(..., description="cline 훅 이벤트 수 (TaskStart/Complete/Cancel/Resume, Pre/PostToolUse, UserPromptSubmit, PreCompact)")
    event_type_counts: dict[str, int] = Field(default_factory=dict, description="이벤트 타입별 발생 횟수")
    total_git_events: int = Field(..., description="GitCommit 이벤트 수 (백필 + post-commit hook)")
    total_tasks: int = Field(..., description="TaskStart 이벤트 기준 총 작업 수")
    total_resumes: int = Field(..., description="TaskResume 이벤트가 발생한 작업 수")
    rework_rate: float = Field(..., description="재업무율(%) = total_resumes / total_tasks × 100")
    reviewed_commits: int = Field(..., description="커밋 메시지에 [reviewed] 태그가 포함된 검수 완료 커밋 수")
    total_writes: int = Field(..., description="전체 write_to_file 호출 횟수")
    total_reads: int = Field(..., description="전체 read_file 호출 횟수")
    file_rework_count: int = Field(..., description="동일 파일을 2회 이상 write_to_file 한 파일 수 (재작업 추정치)")
    file_rework_rate: float = Field(..., description="파일 재작업률(%) = file_rework_count / unique_written_files × 100")
    read_write_ratio: float = Field(..., description="읽기/쓰기 비율 = total_reads / total_writes (낮을수록 효율적 코드 생성)")
    efficiency_score: float = Field(0.0, description="AI작업 효율성 = (1 - AI재업무율) × R/W비율")
    rework_files: list[dict] = Field(default_factory=list, description="2회 이상 수정된 파일 목록 [{file, write_count}]")
    top_written_files: list[dict] = Field(default_factory=list, description="가장 많이 쓰인 파일 Top 20 [{file, count}]")
    top_read_files: list[dict] = Field(default_factory=list, description="가장 많이 읽힌 파일 Top 20 [{file, count}]")
    # ── 토큰 추정 (텍스트 볼륨 기반 간접 추정) ──
    est_total_tokens: int = Field(0, description="추정 총 토큰 수 (텍스트 chars ÷ 4)")
    est_total_cost_usd: float = Field(0.0, description="추정 비용 (USD, 예시 가격표 기준)")
    est_by_model: list[dict] = Field(default_factory=list, description="모델별 추정 [{model, tokens_in, tokens_out, cost_usd, price_key}]")
    unique_written_files: int = Field(0, description="write_to_file 이 호출된 고유 파일 수")
    auto_approved_count: int = Field(0, description="requires_approval=false 인 PreToolUse 횟수 (자동 승인된 도구 실행)")
    manual_approval_count: int = Field(0, description="requires_approval=true 인 PreToolUse 횟수 (사람 승인 필요)")
    auto_approval_by_tool: dict[str, int] = Field(default_factory=dict, description="자동 승인된 도구별 횟수")
    manual_approval_by_tool: dict[str, int] = Field(default_factory=dict, description="수동 승인이 필요했던 도구별 횟수")
    safe_tools_count: int = Field(0, description="승인 필드 없이 실행된 도구 횟수 (읽기/검색 등 항상 자동 허용)")
    safe_tools_by_tool: dict[str, int] = Field(default_factory=dict, description="항상 안전 도구별 횟수")
    # ── Auto-Approve 카테고리별 분석 (Cline 문서 기준) ──
    auto_approve_by_category: dict[str, dict] = Field(
        default_factory=dict,
        description="Cline 카테고리별 승인 현황 {카테고리: {auto:N, manual:M, safe:K}}",
    )
    inferred_auto_approve: list[str] = Field(
        default_factory=list,
        description="데이터 기반으로 Auto-Approve가 활성화된 것으로 추정되는 카테고리 목록",
    )
    yolo_mode_suspected: bool = Field(False, description="YOLO Mode 의심 여부 — 승인 필요 도구가 모두 auto로 처리된 경우 True")
    model_usage: dict[str, int] = Field(..., description="모델별 이벤트 발생 횟수 {'provider/slug': count}")
    top_model: str = Field(..., description="가장 많이 사용된 모델 (provider/slug)")
    unique_models: int = Field(..., description="사용된 고유 모델 수")
    token_usage: dict[str, Any] = Field(default_factory=dict, description="모델별 토큰 사용량 집계 (PreCompact 이벤트 기반)")
    total_tokens_in: int = Field(0, description="전체 입력 토큰 합계")
    total_tokens_out: int = Field(0, description="전체 출력 토큰 합계")
    total_tokens_in_cache: int = Field(0, description="전체 캐시 입력 토큰 합계")
    total_tokens_out_cache: int = Field(0, description="전체 캐시 출력 토큰 합계")
    compact_count: int = Field(0, description="PreCompact 이벤트 발생 횟수 (컨텍스트 압축 횟수)")
    # ── Cancel 품질 지표 ──
    total_task_cancel_events: int = Field(0, description="TaskCancel 훅 발생 총 횟수 (동일 Task 내 다회 가능)")
    tasks_ended_canceled: int = Field(0, description="최종 상태가 취소로 집계된 Task 수")
    task_cancel_rate_pct: float = Field(0.0, description="총 Task 중 취소 종료 비율(%)")
    post_cancel_prompt_pairs: int = Field(0, description="취소 직후 동일 Task에서 이어진 UserPromptSubmit 짝 수")
    # ── 자율성 지표 ──
    # 분류 기준: a) 사람=TaskCancel·UserPromptSubmit, b) Agent=TaskComplete·PreToolUse·PostToolUse·PreCompact, c) 혼합=TaskStart·TaskResume
    # 혼합 이벤트도 사람이 트리거해야 발생하므로 자율성 분모에 포함
    human_action_count: int = Field(0, description="사람이 직접 트리거한 이벤트 총 수 (a류: TaskCancel + UserPromptSubmit)")
    agent_action_count: int = Field(0, description="Agent 단독 수행 이벤트 총 수 (b류: TaskComplete + PreToolUse + PostToolUse + PreCompact)")
    mixed_action_count: int = Field(0, description="사람+Agent 공동 이벤트 총 수 (c류: TaskStart + TaskResume)")
    autonomy_pct: float = Field(0.0, description="에이전트 자율성(%) = agent_actions / (agent + human + mixed) × 100")
    human_actions_breakdown: dict[str, int] = Field(default_factory=dict, description="사람 행동 이벤트별 세부 횟수 {event_type: count}")
    agent_actions_breakdown: dict[str, int] = Field(default_factory=dict, description="Agent 행동 이벤트별 세부 횟수 {event_type: count}")
    mixed_actions_breakdown: dict[str, int] = Field(default_factory=dict, description="혼합 이벤트별 세부 횟수 {event_type: count}")


class CancelFollowupModel(BaseModel):
    """TaskCancel 직후 같은 taskId에서 기록된 첫 UserPromptSubmit."""
    taskId: str = Field(..., description="Task ID")
    cancel_event_idx: int = Field(..., description="TaskCancel 이벤트 줄 번호 (1-based)")
    prompt_event_idx: int = Field(..., description="후속 UserPromptSubmit 이벤트 줄 번호 (1-based)")
    cancel_ts_kst: str = Field(..., description="취소 시각 (KST)")
    prompt_ts_kst: str = Field(..., description="후속 프롬프트 시각 (KST)")
    gap_sec: float | None = Field(None, description="취소→프롬프트 간격(초)")
    prompt_text: str = Field(..., description="후속 프롬프트 텍스트 (최대 500자)")
    cancel_context: str = Field("", description="취소 시점에 진행 중이던 마지막 프롬프트/작업 내용 (최대 500자)")
    follow_result: str = Field("", description="재프롬프트 이후 TaskComplete 결과 요약 (최대 300자, 없으면 빈 문자열)")
    follow_status: str = Field("진행중", description="재프롬프트 이후 Task 결과: 완료 / 재취소 / 진행중")


class HumanInteractionItemModel(BaseModel):
    """Auto-Approve 여부와 관계없이 사람이 반드시 직접 응답해야 했던 상호작용.
    ask_followup_question 또는 plan_mode_respond PostToolUse 이벤트 기반."""
    event_idx: int = Field(..., description="events.jsonl 줄 번호 (1-based)")
    taskId: str = Field(..., description="소속 Task ID")
    ts_kst: str = Field(..., description="발생 시각 (KST)")
    interaction_type: str = Field(..., description="ask_followup | plan_mode")
    agent_message: str = Field("", description="Cline이 사용자에게 보낸 질문/응답 (최대 300자)")
    options: list[str] = Field(default_factory=list, description="제시된 선택지 목록 (ask_followup_question 한정)")
    user_answer: str = Field("", description="사용자가 실제 입력/선택한 내용 (최대 300자)")
    task_context: str = Field("", description="해당 Task의 초기 요청 요약 (최대 200자)")


class ManualApprovalItemModel(BaseModel):
    """Auto-Approve 활성화 상태에서도 사람이 직접 승인해야 했던 PreToolUse 이벤트."""
    event_idx: int = Field(..., description="events.jsonl 줄 번호 (1-based)")
    taskId: str = Field(..., description="소속 Task ID")
    ts_kst: str = Field(..., description="발생 시각 (KST)")
    tool_name: str = Field(..., description="실행 요청된 도구명")
    command: str = Field("", description="execute_command 도구의 명령어 (최대 300자)")
    file_path: str = Field("", description="파일 경로 (write/read 도구 한정)")
    task_context: str = Field("", description="해당 Task의 초기 요청 요약 (최대 200자)")
    content_preview: str = Field("", description="파일 내용 미리보기 (최대 200자, write 도구 한정)")


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
    cancel_count: int = Field(0, description="이 Task에서 발생한 TaskCancel 횟수")
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
    cancel_followups: list[CancelFollowupModel] = Field(
        default_factory=list,
        description="TaskCancel 직후 사용자가 보낸 첫 프롬프트 목록",
    )
    manual_approval_items: list[ManualApprovalItemModel] = Field(
        default_factory=list,
        description="Auto-Approve 활성화 상태에서도 수동 승인이 필요했던 PreToolUse 이벤트 목록",
    )
    human_interaction_items: list[HumanInteractionItemModel] = Field(
        default_factory=list,
        description="사람이 직접 응답해야 했던 상호작용 (ask_followup_question / plan_mode_respond)",
    )


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


_HUMAN_EVENTS  = ("TaskCancel", "UserPromptSubmit")
_AGENT_EVENTS  = ("TaskComplete", "PreToolUse", "PostToolUse", "PreCompact")
_MIXED_EVENTS  = ("TaskStart", "TaskResume")

def _infer_auto_approve(approval_by_category: dict) -> dict:
    """카테고리별 승인 현황으로 Auto-Approve 설정을 추론합니다.

    추론 로직:
    - 해당 카테고리에서 manual=0 이고 auto>0 → 사용자가 그 카테고리 Auto-Approve를 켰을 가능성 높음
    - 파일편집·명령실행이 모두 auto (manual=0) → YOLO 의심
    - 파일읽기는 항상 safe이므로 추론에서 제외
    """
    inferred: list[str] = []
    cat_data = dict(approval_by_category)  # defaultdict → dict

    APPROVAL_REQUIRED_CATS = {"파일편집", "명령실행", "브라우저", "MCP서버"}

    for cat, counts in cat_data.items():
        if cat not in APPROVAL_REQUIRED_CATS:
            continue
        if counts.get("auto", 0) > 0 and counts.get("manual", 0) == 0:
            inferred.append(cat)

    # YOLO 의심: 승인 필요 카테고리 전부 auto (manual 합계 = 0) + auto 합계 > 0
    total_manual = sum(v.get("manual", 0) for v in cat_data.values())
    total_auto_needed = sum(
        v.get("auto", 0) for k, v in cat_data.items() if k in APPROVAL_REQUIRED_CATS
    )
    yolo = (total_manual == 0) and (total_auto_needed >= 5)

    return {
        "auto_approve_by_category": {k: dict(v) for k, v in cat_data.items()},
        "inferred_auto_approve": inferred,
        "yolo_mode_suspected": yolo,
    }


def _autonomy_metrics(event_type_counts: dict[str, int]) -> dict:
    """이벤트 카운트로 자율성 지표를 계산합니다."""
    h_bd = {e: event_type_counts.get(e, 0) for e in _HUMAN_EVENTS}
    a_bd = {e: event_type_counts.get(e, 0) for e in _AGENT_EVENTS}
    m_bd = {e: event_type_counts.get(e, 0) for e in _MIXED_EVENTS}
    human_cnt = sum(h_bd.values())
    agent_cnt = sum(a_bd.values())
    mixed_cnt = sum(m_bd.values())
    # 혼합(TaskStart·TaskResume)도 사람이 트리거해야 발생하므로 분모에 포함
    denom = agent_cnt + human_cnt + mixed_cnt
    return {
        "human_action_count": human_cnt,
        "agent_action_count": agent_cnt,
        "mixed_action_count": mixed_cnt,
        "autonomy_pct": round(agent_cnt / denom * 100, 1) if denom else 0.0,
        "human_actions_breakdown": h_bd,
        "agent_actions_breakdown": a_bd,
        "mixed_actions_breakdown": m_bd,
    }


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
        "file_paths": set(), "file_write_counts": defaultdict(int), "file_read_counts": defaultdict(int),
        "last_event": "", "last_result": "",
        "event_count": 0,
    })

    event_list: list[dict] = []
    event_type_counts: dict[str, int] = defaultdict(int)
    tool_counts: dict[str, int] = defaultdict(int)
    model_usage: dict[str, int] = defaultdict(int)
    auto_approval_by_tool: dict[str, int] = defaultdict(int)
    manual_approval_by_tool: dict[str, int] = defaultdict(int)
    manual_approval_items: list[dict] = []
    human_interaction_items: list[dict] = []
    # 카테고리별 집계: {카테고리: {auto, manual, safe}}
    approval_by_category: dict[str, dict] = defaultdict(lambda: {"auto": 0, "manual": 0, "safe": 0})
    safe_tools_by_tool: dict[str, int] = defaultdict(int)
    # 모델별 토큰 집계 (PreCompact 기반)
    token_usage: dict[str, dict] = defaultdict(lambda: {
        "tokens_in": 0, "tokens_out": 0,
        "tokens_in_cache": 0, "tokens_out_cache": 0, "compact_count": 0,
    })
    # 모델별 텍스트 볼륨 집계 (토큰 간접 추정용)
    # {model_slug: {"in_chars": int, "out_chars": int}}
    model_text_volume: dict[str, dict] = defaultdict(lambda: {"in_chars": 0, "out_chars": 0})
    last_task_id: str | None = None
    # TaskCancel 직후 UserPromptSubmit 짝 매칭용: taskId → deque of {idx, ts}
    pending_cancel: dict[str, deque] = defaultdict(deque)
    cancel_followups: list[dict] = []
    # 재프롬프트 후 결과 대기 중: taskId → list of indices into cancel_followups
    pending_reprompt: dict[str, list] = defaultdict(list)

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

        # Auto-Approve 추적: PreToolUse의 requires_approval 필드 분석
        if event == "PreToolUse" and tool_name:
            ra = params.get("requiresApproval") or params.get("requires_approval")
            cat = _tool_category(tool_name)
            if ra is False or ra == "false":
                auto_approval_by_tool[tool_name] += 1
                approval_by_category[cat]["auto"] += 1
            elif ra is True or ra == "true":
                manual_approval_by_tool[tool_name] += 1
                approval_by_category[cat]["manual"] += 1
                # 세부 컨텍스트 수집
                task_ctx = ""
                if tid and tid in tasks:
                    raw_ctx = tasks[tid]["initial_task"] or (tasks[tid]["prompts"][0] if tasks[tid]["prompts"] else "")
                    task_ctx = (raw_ctx[:200] + "…") if len(raw_ctx) > 200 else raw_ctx
                _cmd = command[:300] if command else ""
                _content = (params.get("content", "") or "")[:200]
                manual_approval_items.append({
                    "event_idx": idx,
                    "taskId": tid or "",
                    "ts_kst": _kst(ts),
                    "tool_name": tool_name,
                    "command": _cmd,
                    "file_path": path_val,
                    "task_context": task_ctx,
                    "content_preview": _content,
                })
            else:
                # requires_approval 필드 없음 = 항상 자동 허용되는 안전 도구(파일읽기 등)
                safe_tools_by_tool[tool_name] += 1
                approval_by_category[cat]["safe"] += 1
        if provider and slug:
            model_usage[f"{provider}/{slug}"] += 1

        # 토큰 데이터 수집: PreCompact payload 또는 extra 필드
        extra: dict = ev.get("extra") or {}
        token_src: dict = {}
        if event == "PreCompact":
            # payload = preCompact 내부 데이터 (tokensIn 등 직접 포함)
            token_src = payload or extra
        elif extra:
            # 다른 이벤트에서 top-level 토큰 데이터가 있을 경우 수집
            if any(k in extra for k in ("tokensIn", "tokensOut", "inputTokens", "outputTokens")):
                token_src = extra

        if token_src:
            model_key = f"{provider}/{slug}" if (provider and slug) else "unknown"
            tu = token_usage[model_key]
            tu["tokens_in"]        += (token_src.get("tokensIn") or token_src.get("inputTokens") or 0)
            tu["tokens_out"]       += (token_src.get("tokensOut") or token_src.get("outputTokens") or 0)
            tu["tokens_in_cache"]  += token_src.get("tokensInCache", 0)
            tu["tokens_out_cache"] += token_src.get("tokensOutCache", 0)
            if event == "PreCompact":
                tu["compact_count"] += 1

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
            # 텍스트 볼륨: 사용자 프롬프트 → 입력 텍스트
            if tid and t.get("model_slug"):
                model_text_volume[t["model_slug"]]["in_chars"] += len(p)
            # 취소 직후 후속 프롬프트 짝 매칭
            if tid and pending_cancel[tid]:
                c = pending_cancel[tid].popleft()
                gap: float | None = (
                    round((ts - c["ts"]) / 1000, 2)
                    if ts is not None and c["ts"] is not None
                    else None
                )
                cancel_followups.append({
                    "taskId": tid,
                    "cancel_event_idx": c["idx"],
                    "prompt_event_idx": idx,
                    "cancel_ts_kst": _kst(c["ts"]),
                    "prompt_ts_kst": _kst(ts),
                    "gap_sec": gap,
                    "cancel_context": c.get("context", ""),
                    "prompt_text": (p[:500] + "…") if len(p) > 500 else p,
                    "follow_result": "",
                    "follow_status": "진행중",
                })
                pending_reprompt[tid].append(len(cancel_followups) - 1)
            if not t["first_prompt"]:
                t["first_prompt"] = p
            t["prompts"].append(p)
        elif event == "TaskResume":
            t["resumed"] = True
            t["resume_count"] += 1
        elif event == "TaskCancel":
            t["canceled"] = True
            t["cancel_count"] += 1
            if tid:
                # 재프롬프트 후 또 취소된 경우 → "재취소" 처리
                for cf_idx in pending_reprompt[tid]:
                    cancel_followups[cf_idx]["follow_status"] = "재취소"
                pending_reprompt[tid].clear()
                # 취소 시점의 마지막 작업 내용 캡처
                _last = t["prompts"][-1] if t["prompts"] else (t["initial_task"] or "")
                _ctx = (_last[:500] + "…") if len(_last) > 500 else _last
                pending_cancel[tid].append({"idx": idx, "ts": ts, "context": _ctx})
        elif event == "TaskComplete":
            t["completed"] = True
            t["complete_count"] += 1
            result_text = str(
                (payload.get("taskMetadata") or {}).get("result", "")
            )[:300]
            t["last_result"] = result_text[:200]
            # 텍스트 볼륨: 태스크 완료 응답 → 출력 텍스트
            if tid and t.get("model_slug"):
                model_text_volume[t["model_slug"]]["out_chars"] += len(result_text)
            if tid:
                for cf_idx in pending_reprompt[tid]:
                    cancel_followups[cf_idx]["follow_result"] = result_text
                    cancel_followups[cf_idx]["follow_status"] = "완료"
                pending_reprompt[tid].clear()
        elif event == "PreToolUse":
            t["pre_tool_count"] += 1
            if tool_name and tool_name not in t["tools_used"]:
                t["tools_used"].append(tool_name)
        elif event == "PostToolUse":
            t["post_tool_count"] += 1
            if success is True:
                t["success_tool_count"] += 1
            # 텍스트 볼륨: 도구 파라미터(입력) + 결과(모델이 받는 컨텍스트)
            if tid and t.get("model_slug"):
                params_text = str(payload.get("parameters", ""))
                result_text = str(payload.get("result", ""))
                model_text_volume[t["model_slug"]]["in_chars"] += len(params_text) + len(result_text)
            if tool_name == "write_to_file":
                t["write_count"] += 1
                if path_val:
                    t["file_write_counts"][path_val] += 1
            if tool_name in ("read_file", "read_file_content"):
                t["read_count"] += 1
                if path_val:
                    t["file_read_counts"][path_val] += 1
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

            # 사람이 직접 응답해야 하는 상호작용 수집 (ask_followup_question / plan_mode_respond)
            if tool_name in ("ask_followup_question", "plan_mode_respond") and success is True:
                params_inner: dict = payload.get("parameters") or {}
                result_str: str = str(payload.get("result", ""))

                if tool_name == "ask_followup_question":
                    itype = "ask_followup"
                    agent_msg = str(params_inner.get("question", ""))[:300]
                    opts_raw = params_inner.get("options", "[]")
                    try:
                        opts: list[str] = json.loads(opts_raw) if isinstance(opts_raw, str) else (opts_raw or [])
                    except Exception:
                        opts = []
                    # result 형식: <answer>...</answer>
                    import re as _re
                    m_ans = _re.search(r"<answer>(.*?)</answer>", result_str, _re.DOTALL)
                    user_ans = m_ans.group(1).strip()[:300] if m_ans else result_str.strip()[:300]
                else:  # plan_mode_respond
                    itype = "plan_mode"
                    agent_msg = str(params_inner.get("response", ""))[:300]
                    opts = []
                    import re as _re
                    m_msg = _re.search(r"<user_message>(.*?)</user_message>", result_str, _re.DOTALL)
                    user_ans = m_msg.group(1).strip()[:300] if m_msg else result_str.strip()[:300]

                task_ctx = ""
                if tid and tid in tasks:
                    raw_ctx = tasks[tid]["initial_task"] or (tasks[tid]["prompts"][0] if tasks[tid]["prompts"] else "")
                    task_ctx = (raw_ctx[:200] + "…") if len(raw_ctx) > 200 else raw_ctx

                human_interaction_items.append({
                    "event_idx": idx,
                    "taskId": tid or "",
                    "ts_kst": _kst(ts),
                    "interaction_type": itype,
                    "agent_message": agent_msg,
                    "options": opts,
                    "user_answer": user_ans,
                    "task_context": task_ctx,
                })

    reviewed_shas = {
        p.stem for p in FINAL_DIR.glob("*.marker")
    } if FINAL_DIR.exists() else set()

    task_list: list[dict] = []
    total_starts = 0
    total_resumes = 0
    tasks_ended_canceled = 0
    total_writes = 0
    total_reads = 0
    unique_written_files: set[str] = set()
    rework_file_count = 0
    global_file_write_counts: dict[str, int] = defaultdict(int)
    global_file_read_counts: dict[str, int] = defaultdict(int)

    for tid, t in tasks.items():
        if t["start_ts"] is None:
            continue
        total_starts += 1
        if t["canceled"]:
            tasks_ended_canceled += 1
        if t["resumed"]:
            total_resumes += 1
        total_writes += t["write_count"]
        total_reads += t["read_count"]
        for fp, cnt in t["file_write_counts"].items():
            unique_written_files.add(fp)
            global_file_write_counts[fp] += cnt
            if cnt > 1:
                rework_file_count += 1
        for fp, cnt in t["file_read_counts"].items():
            global_file_read_counts[fp] += cnt

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
            "cancel_count": t["cancel_count"],
            "model": f"{t['model_provider']}/{t['model_slug']}",
            "tools_used": t["tools_used"],
            "file_paths": sorted(t["file_paths"])[:10],
            "last_result": t["last_result"],
        })

    task_list.sort(key=lambda x: x["start_ts"] or 0, reverse=True)

    n_unique = len(unique_written_files)
    sorted_models = sorted(model_usage.items(), key=lambda x: -x[1])

    # ── 토큰 추정 계산 ────────────────────────────────────────────────────────
    # 텍스트 볼륨 기반 간접 추정: 4 chars ≈ 1 token (평균)
    CHARS_PER_TOKEN = 4
    est_by_model: list[dict] = []
    est_total_tokens = 0
    est_total_cost_usd = 0.0
    for model_slug, vol in model_text_volume.items():
        in_tokens  = vol["in_chars"]  // CHARS_PER_TOKEN
        out_tokens = vol["out_chars"] // CHARS_PER_TOKEN
        # 출력량 추정: out_chars가 매우 작으면 in_tokens의 15%로 보정
        if out_tokens < in_tokens * 0.05:
            out_tokens = max(out_tokens, in_tokens // 7)
        price_key, price = _model_price(model_slug)
        cost = (
            in_tokens  / 1_000_000 * price["input"]
            + out_tokens / 1_000_000 * price["output"]
        )
        est_by_model.append({
            "model": model_slug,
            "price_key": price_key,
            "price_input": price["input"],
            "price_output": price["output"],
            "tokens_in": in_tokens,
            "tokens_out": out_tokens,
            "cost_usd": round(cost, 4),
        })
        est_total_tokens += in_tokens + out_tokens
        est_total_cost_usd += cost
    est_by_model.sort(key=lambda x: -x["cost_usd"])
    est_total_cost_usd = round(est_total_cost_usd, 4)
    top_model = sorted_models[0][0] if sorted_models else ""
    return {
        "summary": {
            "total_events": len(events),
            "total_git_events": sum(1 for e in events if e.get("event") == "GitCommit"),
            "total_hook_events": sum(1 for e in events if e.get("event") and e.get("event") != "GitCommit"),
            "event_type_counts": dict(event_type_counts),
            "total_tasks": total_starts,
            "total_resumes": total_resumes,
            "rework_rate": round(total_resumes / total_starts * 100, 1) if total_starts else 0,
            "reviewed_commits": len(reviewed_shas),
            "total_writes": total_writes,
            "total_reads": total_reads,
            "file_rework_count": rework_file_count,
            "file_rework_rate": round(rework_file_count / n_unique * 100, 1) if n_unique else 0.0,
            "read_write_ratio": round(total_reads / max(total_writes, 1), 2),
            "efficiency_score": round(
                (1 - (rework_file_count / n_unique if n_unique else 0))
                * (total_reads / max(total_writes, 1)),
                2
            ),
            "unique_written_files": n_unique,
            "rework_files": sorted(
                [{"file": fp, "write_count": cnt} for fp, cnt in global_file_write_counts.items() if cnt > 1],
                key=lambda x: -x["write_count"]
            ),
            "top_written_files": sorted(
                [{"file": fp, "count": cnt} for fp, cnt in global_file_write_counts.items()],
                key=lambda x: -x["count"]
            )[:30],
            "top_read_files": sorted(
                [{"file": fp, "count": cnt} for fp, cnt in global_file_read_counts.items()],
                key=lambda x: -x["count"]
            )[:30],
            "est_total_tokens": est_total_tokens,
            "est_total_cost_usd": est_total_cost_usd,
            "est_by_model": est_by_model,
            "auto_approved_count": sum(auto_approval_by_tool.values()),
            "manual_approval_count": sum(manual_approval_by_tool.values()),
            "auto_approval_by_tool": dict(auto_approval_by_tool),
            "manual_approval_by_tool": dict(manual_approval_by_tool),
            "safe_tools_count": sum(safe_tools_by_tool.values()),
            "safe_tools_by_tool": dict(safe_tools_by_tool),
            **_infer_auto_approve(approval_by_category),
            "model_usage": dict(model_usage),
            "top_model": top_model,
            "unique_models": len(model_usage),
            "token_usage": {k: dict(v) for k, v in token_usage.items()},
            "total_tokens_in":       sum(v["tokens_in"]        for v in token_usage.values()),
            "total_tokens_out":      sum(v["tokens_out"]       for v in token_usage.values()),
            "total_tokens_in_cache": sum(v["tokens_in_cache"]  for v in token_usage.values()),
            "total_tokens_out_cache":sum(v["tokens_out_cache"] for v in token_usage.values()),
            "compact_count":         sum(v["compact_count"]    for v in token_usage.values()),
            "total_task_cancel_events": event_type_counts.get("TaskCancel", 0),
            "tasks_ended_canceled": tasks_ended_canceled,
            "task_cancel_rate_pct": round(
                tasks_ended_canceled / total_starts * 100, 1
            ) if total_starts else 0.0,
            "post_cancel_prompt_pairs": len(cancel_followups),
            # ── 자율성 지표 ──
            **_autonomy_metrics(event_type_counts),
        },
        "tasks": task_list,
        "events": event_list,
        "cancel_followups": cancel_followups,
        "manual_approval_items": manual_approval_items,
        "human_interaction_items": human_interaction_items,
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

쿼리 파라미터:
- **start_ts** : 필터 시작 시각 (Unix ms, 포함)
- **end_ts**   : 필터 종료 시각 (Unix ms, 포함)
""",
)
def get_data(
    start_ts: float | None = Query(None, description="필터 시작 시각 (Unix ms)"),
    end_ts: float | None = Query(None, description="필터 종료 시각 (Unix ms)"),
) -> dict:
    events = load_events()
    if start_ts is not None or end_ts is not None:
        def _ts(e: dict) -> float:
            """ts 필드가 str/int 혼합이므로 안전하게 float 변환"""
            v = e.get("ts")
            try:
                return float(v) if v is not None else 0.0
            except (TypeError, ValueError):
                return 0.0
        events = [
            e for e in events
            if (start_ts is None or _ts(e) >= start_ts)
            and (end_ts is None or _ts(e) <= end_ts)
        ]
    return process(events)


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
_commits_cache: list[dict] | None = None
_commits_cache_mtime: float | None = None


def load_commits() -> list[dict]:
    """
    .cline-metrics/commits/ 디렉터리에서 .patch + .snapshot.json 파일을 읽어
    커밋 목록을 반환합니다. events.jsonl 의 GitCommit 이벤트와 매핑하여
    타임스탬프·taskId 도 함께 제공합니다.
    commits 디렉터리 mtime이 바뀌지 않으면 캐시를 반환합니다.
    """
    global _commits_cache, _commits_cache_mtime
    commits_dir = ROOT / "commits"
    if not commits_dir.exists():
        return []

    # 디렉터리 mtime 기반 캐시 무효화
    try:
        cur_mtime = commits_dir.stat().st_mtime
    except OSError:
        cur_mtime = None
    if _commits_cache is not None and cur_mtime == _commits_cache_mtime:
        return _commits_cache

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

    _commits_cache = result
    _commits_cache_mtime = cur_mtime
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
    files_changed: int = Field(..., description="수정된 소스 파일 수 (신규·삭제 제외)")
    added_files: int = Field(0, description="신규 추가 소스 파일 수 (유사도 계산 제외)")
    deleted_files: int = Field(0, description="삭제된 소스 파일 수 (유사도 계산 제외)")
    total_files: int = Field(..., description="전체 소스 파일 수 (스냅샷 기준)")
    changed_size: int = Field(..., description="변경 파일 총 크기 (bytes)")
    total_size: int = Field(..., description="전체 소스 파일 총 크기 (bytes)")
    scores: SimilarityScores = Field(..., description="프로젝트 전체 가중 유사도 (변경 비중 반영)")
    raw_scores: SimilarityScores = Field(..., description="변경 파일만의 평균 유사도 (비중 미반영)")


_project_sim_cache: list[dict] | None = None
_DISK_CACHE_PROJECT_SIM = ROOT / ".sim_project_cache.json"


def _load_disk_cache_project_sim() -> list | None:
    try:
        if _DISK_CACHE_PROJECT_SIM.exists():
            with _DISK_CACHE_PROJECT_SIM.open("r", encoding="utf-8") as f:
                return json.load(f)
    except Exception:
        pass
    return None


def _save_disk_cache_project_sim(data: list) -> None:
    try:
        with _DISK_CACHE_PROJECT_SIM.open("w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False)
    except Exception:
        pass


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
    if not refresh:
        disk = _load_disk_cache_project_sim()
        if disk is not None:
            _project_sim_cache = disk
            return disk

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
        # 신규 추가 파일(prev에 없던 것)은 제외 — "기존 코드 변경"만 측정
        weighted: dict[str, float] = {"L1": 0.0, "L2": 0.0, "L3": 0.0, "L4": 0.0}
        changed_weight = 0
        added_files = 0    # 신규 추가 파일 수 (유사도 계산 제외)
        deleted_files = 0  # 삭제된 파일 수 (유사도 계산 제외)
        for fp in changed_source:
            old = prev_map.get(fp, "")
            new = curr_map.get(fp, "")
            if not old and not new:
                continue
            if not old:
                # 신규 추가 파일 — 이전 버전 없음, 제외
                added_files += 1
                continue
            if not new:
                # 삭제된 파일 — 현재 버전 없음, 제외
                deleted_files += 1
                continue
            sc = compute_all(old, new)
            w = len(new.encode("utf-8"))
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
            "files_changed": len(changed_source) - added_files - deleted_files,
            "added_files": added_files,
            "deleted_files": deleted_files,
            "total_files": total_files,
            "changed_size": changed_weight,
            "total_size": total_size,
            "scores": scores,
            "raw_scores": raw_scores,
        })

    _project_sim_cache = results
    _save_disk_cache_project_sim(results)
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


_project_from_first_cache: list | None = None
_project_first_last_cache: dict | None = None

# 디스크 캐시 경로 (재시작해도 재계산 안 해도 되게)
_DISK_CACHE_FROM_FIRST = ROOT / ".sim_from_first_cache.json"


def _load_disk_cache_from_first() -> list | None:
    """디스크 캐시에서 from-first 유사도 데이터를 로드합니다."""
    try:
        if _DISK_CACHE_FROM_FIRST.exists():
            with _DISK_CACHE_FROM_FIRST.open("r", encoding="utf-8") as f:
                return json.load(f)
    except Exception:
        pass
    return None


def _save_disk_cache_from_first(data: list) -> None:
    """from-first 유사도 데이터를 디스크에 저장합니다."""
    try:
        with _DISK_CACHE_FROM_FIRST.open("w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False)
    except Exception:
        pass


@app.get(
    "/api/similarity/project-from-first",
    tags=["similarity"],
    summary="프로젝트 전체 — 각 커밋 vs 첫 커밋 유사도 (시계열)",
)
def get_project_from_first_similarity(
    refresh: bool = Query(False, description="캐시 무시 여부"),
) -> list:
    global _project_from_first_cache
    # 1) 메모리 캐시 확인
    if not refresh and _project_from_first_cache is not None:
        return _project_from_first_cache
    # 2) 디스크 캐시 확인 (재시작 후에도 재계산 불필요)
    if not refresh:
        disk = _load_disk_cache_from_first()
        if disk is not None:
            _project_from_first_cache = disk
            return disk

    from similarity import compute_all

    all_commits = load_commits()
    if not all_commits:
        raise HTTPException(404, "커밋 데이터가 없습니다.")

    commits_asc = sorted(all_commits, key=lambda c: c["ts"] or 0)
    if len(commits_asc) < 2:
        raise HTTPException(404, "커밋이 2개 미만입니다.")

    def _snap(commit: dict) -> dict[str, str]:
        m: dict[str, str] = {}
        for sf in commit.get("snapshot_files", []):
            short = _strip_prefix(sf.get("path", ""))
            if _is_source(short):
                m[short] = sf.get("content", "")
        return m

    # 의미 있는 기준 커밋 찾기: 마지막 커밋과 공통 소스파일이 THRESHOLD 개 이상인 가장 첫 커밋
    OVERLAP_THRESHOLD = 20
    MAX_CHARS = 50_000
    MAX_FILES = 15

    last_map = _snap(commits_asc[-1])
    last_files = set(last_map.keys())

    ref_idx = 0
    for idx, c in enumerate(commits_asc):
        candidate_map = _snap(c)
        overlap = len(set(candidate_map.keys()) & last_files)
        if overlap >= OVERLAP_THRESHOLD:
            ref_idx = idx
            break

    first_map = _snap(commits_asc[ref_idx])
    ref_sha = commits_asc[ref_idx]["sha"]

    results: list[dict] = []
    for i, commit in enumerate(commits_asc[ref_idx:], start=ref_idx):
        sha = commit["sha"]
        curr_map = _snap(commit) if i > ref_idx else first_map

        n_total_current = len(curr_map)
        common = set(first_map.keys()) & set(curr_map.keys())
        n_new = n_total_current - len(common)  # 기준 커밋에 없는 신규 파일 수

        if i == ref_idx:
            scores = {"L1": 1.0, "L2": 1.0, "L3": 1.0, "L4": 1.0}
            file_count = n_total_current
            new_file_count = 0
        else:
            candidates = sorted(
                [f for f in common
                 if first_map[f].strip() and curr_map[f].strip()
                 and len(first_map[f]) < MAX_CHARS and len(curr_map[f]) < MAX_CHARS],
                key=lambda f: len(first_map[f]) + len(curr_map[f]),
                reverse=True,
            )[:MAX_FILES]

            if not candidates:
                # 공통 파일 없음 → 신규 파일만 존재 → 유사도 0
                scores = {"L1": 0.0, "L2": 0.0, "L3": 0.0, "L4": 0.0}
                file_count = n_total_current
                new_file_count = n_new
            else:
                totals = {"L1": 0.0, "L2": 0.0, "L3": 0.0, "L4": 0.0}
                count = 0
                for f in candidates:
                    try:
                        sc = compute_all(first_map[f], curr_map[f])
                        for k in totals:
                            totals[k] += sc.get(k, 0.0)
                        count += 1
                    except Exception:
                        pass

                # 공통 파일 평균 유사도
                common_sim = {k: totals[k] / count if count else 0.0 for k in totals}
                # 신규 파일 비율만큼 0점 반영: sim * (n_common / n_total) + 0 * (n_new / n_total)
                common_ratio = len(common) / n_total_current if n_total_current else 1.0
                scores = {k: round(common_sim[k] * common_ratio, 4) for k in common_sim}
                file_count = n_total_current
                new_file_count = n_new

        results.append({
            "sha": sha,
            "sha_short": sha[:7],
            "message": commit.get("message", ""),
            "ts_kst": commit.get("ts_kst", ""),
            "file_count": file_count,
            "new_file_count": new_file_count if i != ref_idx else 0,
            "scores": scores,
            "ref_sha_short": ref_sha[:7],
        })

    _project_from_first_cache = results
    _save_disk_cache_from_first(results)  # 디스크에도 저장 → 재시작 후 재계산 불필요
    return results

@app.get(
    "/api/similarity/project-first-last",
    tags=["similarity"],
    summary="프로젝트 전체 첫 커밋 ↔ 마지막 커밋 유사도 (평균)",
)
def get_project_first_last_similarity(
    refresh: bool = Query(False, description="캐시 무시 여부"),
) -> dict:
    global _project_first_last_cache
    if not refresh and _project_first_last_cache is not None:
        return _project_first_last_cache

    from similarity import compute_all

    all_commits = load_commits()
    if not all_commits:
        raise HTTPException(404, "커밋 데이터가 없습니다.")

    commits_asc = sorted(all_commits, key=lambda c: c["ts"] or 0)
    if len(commits_asc) < 2:
        raise HTTPException(404, "커밋이 2개 미만입니다.")

    def _snap(commit: dict) -> dict[str, str]:
        m: dict[str, str] = {}
        for sf in commit.get("snapshot_files", []):
            short = _strip_prefix(sf.get("path", ""))
            if _is_source(short):
                m[short] = sf.get("content", "")
        return m

    # 의미 있는 기준 커밋 찾기: 마지막 커밋과 공통 소스파일이 THRESHOLD 개 이상인 가장 첫 커밋
    OVERLAP_THRESHOLD = 20
    last_map = _snap(commits_asc[-1])
    last_files = set(last_map.keys())

    ref_idx = 0
    for idx, c in enumerate(commits_asc):
        candidate_map = _snap(c)
        overlap = len(set(candidate_map.keys()) & last_files)
        if overlap >= OVERLAP_THRESHOLD:
            ref_idx = idx
            break

    first_map = _snap(commits_asc[ref_idx])

    # 마지막 커밋 기준 전체 소스파일 수 (신규 포함)
    n_total_last = len(last_map)
    common_files = set(first_map.keys()) & set(last_map.keys())
    n_new = n_total_last - len(common_files)  # 기준 커밋에 없는 신규 파일 수

    # 너무 작거나 큰 파일 제외 후 상위 20개만 처리 (속도 최적화)
    MAX_FILES = 20
    MAX_CHARS = 50_000
    candidates = sorted(
        [f for f in common_files if first_map[f].strip() and last_map[f].strip()
         and len(first_map[f]) < MAX_CHARS and len(last_map[f]) < MAX_CHARS],
        key=lambda f: len(first_map[f]) + len(last_map[f]),
        reverse=True,
    )[:MAX_FILES]

    totals = {"L1": 0.0, "L2": 0.0, "L3": 0.0, "L4": 0.0}
    count = 0
    for f in candidates:
        try:
            sc = compute_all(first_map[f], last_map[f])
            for k in totals:
                totals[k] += sc.get(k, 0.0)
            count += 1
        except Exception:
            pass

    # 공통 파일 평균 유사도
    common_sim = {k: totals[k] / count if count else 0.0 for k in totals}
    # 신규 파일 비율만큼 0점 반영: sim * (n_common / n_total_last)
    common_ratio = len(common_files) / n_total_last if n_total_last else 1.0
    result = {k: round(common_sim[k] * common_ratio, 4) for k in common_sim}
    result["file_count"] = n_total_last
    result["common_file_count"] = count
    result["new_file_count"] = n_new
    result["total_commits"] = len(commits_asc)
    result["first_sha_short"] = commits_asc[ref_idx]["sha"][:7]
    result["last_sha_short"] = commits_asc[-1]["sha"][:7]

    _project_first_last_cache = result
    return result


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
