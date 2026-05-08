export interface TokenUsageEntry {
  tokens_in: number
  tokens_out: number
  tokens_in_cache: number
  tokens_out_cache: number
  compact_count: number
}

export interface Summary {
  total_events: number
  total_hook_events: number
  event_type_counts: Record<string, number>
  total_git_events: number
  total_tasks: number
  total_resumes: number
  rework_rate: number
  reviewed_commits: number
  total_writes: number
  total_reads: number
  file_rework_count: number
  file_rework_rate: number
  read_write_ratio: number
  efficiency_score: number
  unique_written_files: number
  auto_approved_count: number
  manual_approval_count: number
  auto_approval_by_tool: Record<string, number>
  manual_approval_by_tool: Record<string, number>
  safe_tools_count: number
  safe_tools_by_tool: Record<string, number>
  auto_approve_by_category: Record<string, { auto: number; manual: number; safe: number }>
  inferred_auto_approve: string[]
  yolo_mode_suspected: boolean
  model_usage: Record<string, number>
  top_model: string
  unique_models: number
  token_usage: Record<string, TokenUsageEntry>
  total_tokens_in: number
  total_tokens_out: number
  total_tokens_in_cache: number
  total_tokens_out_cache: number
  compact_count: number
  total_task_cancel_events: number
  tasks_ended_canceled: number
  task_cancel_rate_pct: number
  post_cancel_prompt_pairs: number
  // ── 자율성 지표 ──
  human_action_count: number
  agent_action_count: number
  mixed_action_count: number
  autonomy_pct: number
  human_actions_breakdown: Record<string, number>
  agent_actions_breakdown: Record<string, number>
  mixed_actions_breakdown: Record<string, number>
  // ── 파일 세부 데이터 ──
  rework_files: { file: string; write_count: number }[]
  top_written_files: { file: string; count: number }[]
  top_read_files: { file: string; count: number }[]
  // ── 토큰 추정 ──
  est_total_tokens: number
  est_total_cost_usd: number
  est_by_model: {
    model: string
    price_key: string
    price_input: number
    price_output: number
    tokens_in: number
    tokens_out: number
    cost_usd: number
  }[]
}

export interface CancelFollowup {
  taskId: string
  cancel_event_idx: number
  prompt_event_idx: number
  cancel_ts_kst: string
  prompt_ts_kst: string
  gap_sec: number | null
  cancel_context: string
  prompt_text: string
  follow_result: string
  follow_status: '완료' | '재취소' | '진행중'
}

export interface ProjectSimilarityResult {
  sha: string
  sha_short: string
  prev_sha: string
  prev_sha_short: string
  message: string
  ts_kst: string
  files_changed: number
  total_files: number
  changed_size: number
  total_size: number
  scores: SimilarityScores
  raw_scores: SimilarityScores
}

export interface FirstLastSimilarity {
  file: string
  first_sha: string
  first_sha_short: string
  first_message: string
  first_ts_kst: string
  last_sha: string
  last_sha_short: string
  last_message: string
  last_ts_kst: string
  total_commits: number
  first_size: number
  last_size: number
  scores: SimilarityScores
  avg_step_scores: SimilarityScores
}

export interface Task {
  taskId: string
  start_kst: string
  end_kst: string
  duration_sec: number | null
  status: '완료됨' | '취소됨' | '재개됨' | '진행중'
  initial_task: string
  first_prompt: string
  event_count: number
  write_count: number
  read_count: number
  exec_count: number
  time_to_first_code_sec: number | null
  test_runs_count: number
  test_total_sec: number | null
  test_pct_of_duration: number | null
  resume_count: number
  cancel_count: number
  model: string
  tools_used: string[]
  file_paths: string[]
  last_result: string
}

export interface EventItem {
  idx: number
  taskId: string
  event: string
  ts: number | null
  ts_kst: string
  tool: string
  path: string
  command: string
  success: boolean | string
  exec_sec: number | null
  model: string
  git_sha: string
  git_message: string
  clineVersion: string
  raw_payload: Record<string, unknown>
  prompt: string
  initial_task: string
  result_preview: string
  content_preview: string
  requires_approval: string
  previous_state: Record<string, unknown>
  completion_status: string
}

export interface CountItem {
  name: string
  count: number
}

export interface CommitSnapshotFile {
  path: string
  content: string
  is_changed: boolean
}

export interface Commit {
  sha: string
  sha_short: string
  message: string
  ts: number | null
  ts_kst: string
  is_reviewed: boolean
  task_id: string | null
  has_patch: boolean
  has_snapshot: boolean
  patch_content: string
  changed_files: string[]
  snapshot_files: CommitSnapshotFile[]
}

// ── Similarity (AIND_SIMILARITY 적응) ──────────────────────────────────────
export interface SimilarityScores {
  L1: number   // Levenshtein — 문자 단위 표면 유사도
  L2: number   // BLEU — 토큰 n-gram 유사도
  L3: number   // 구조적 유사도 — 라인 단위 SequenceMatcher
  L4: number   // 의미론적 유사도 — TF-IDF char cosine
}

export interface SimilarityResult {
  sha: string
  sha_short: string
  prev_sha: string
  prev_sha_short: string
  message: string
  ts_kst: string
  file: string
  scores: SimilarityScores
  old_size: number
  new_size: number
  changed: boolean
}

export interface HumanInteractionItem {
  event_idx: number
  taskId: string
  ts_kst: string
  interaction_type: 'ask_followup' | 'plan_mode'
  agent_message: string
  options: string[]
  user_answer: string
  task_context: string
}

export interface ManualApprovalItem {
  event_idx: number
  taskId: string
  ts_kst: string
  tool_name: string
  command: string
  file_path: string
  task_context: string
  content_preview: string
}

export interface DashboardData {
  summary: Summary
  tasks: Task[]
  events: EventItem[]
  cancel_followups: CancelFollowup[]
  manual_approval_items: ManualApprovalItem[]
  human_interaction_items: HumanInteractionItem[]
  counts: {
    event_types: CountItem[]
    tools: CountItem[]
  }
}

// ── Repo Tree (파일 선택 GUI) ──────────────────────────────────────────────
export interface RepoTreeNode {
  name: string
  type: 'dir' | 'file'
  path: string
  children?: RepoTreeNode[]
}

export interface RepoTree {
  root: string
  file_count: number
  tree: RepoTreeNode
}
