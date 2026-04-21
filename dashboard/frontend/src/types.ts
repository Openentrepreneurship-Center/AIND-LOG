export interface Summary {
  total_events: number
  total_tasks: number
  total_resumes: number
  rework_rate: number
  reviewed_commits: number
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

export interface DashboardData {
  summary: Summary
  tasks: Task[]
  events: EventItem[]
  counts: {
    event_types: CountItem[]
    tools: CountItem[]
  }
}
