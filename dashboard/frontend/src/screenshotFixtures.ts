/**
 * demo/dashboard-screenshot 전용: 백엔드 없이 UI 캡처용 정적 응답.
 */
import type {
  Commit,
  DashboardData,
  EventItem,
  FirstLastSimilarity,
  ProjectSimilarityResult,
  RepoTree,
  SimilarityResult,
  Task,
} from './types'

export const DEMO_FILE = 'src/main/java/com/backend/domain/user/service/UserService.java'

const scores = (L1: number, L2: number, L3: number, L4: number) => ({ L1, L2, L3, L4 })

export const MOCK_REPO_TREE: RepoTree = {
  root: '/demo/repo',
  file_count: 2,
  tree: {
    name: '',
    type: 'dir',
    path: '',
    children: [
      {
        name: 'src',
        type: 'dir',
        path: 'src',
        children: [
          {
            name: 'main',
            type: 'dir',
            path: 'src/main',
            children: [
              {
                name: 'java',
                type: 'dir',
                path: 'src/main/java',
                children: [
                  {
                    name: 'com',
                    type: 'dir',
                    path: 'src/main/java/com',
                    children: [
                      {
                        name: 'backend',
                        type: 'dir',
                        path: 'src/main/java/com/backend',
                        children: [
                          {
                            name: 'domain',
                            type: 'dir',
                            path: 'src/main/java/com/backend/domain',
                            children: [
                              {
                                name: 'user',
                                type: 'dir',
                                path: 'src/main/java/com/backend/domain/user',
                                children: [
                                  {
                                    name: 'service',
                                    type: 'dir',
                                    path: 'src/main/java/com/backend/domain/user/service',
                                    children: [
                                      {
                                        name: 'UserService.java',
                                        type: 'file',
                                        path: DEMO_FILE,
                                      },
                                    ],
                                  },
                                ],
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
}

export const MOCK_SIMILARITY: SimilarityResult[] = [
  {
    sha: 'a1b2c3d4e5f6789012345678901234567890abcd',
    sha_short: 'a1b2c3d',
    prev_sha: '',
    prev_sha_short: '',
    message: 'init user service',
    ts_kst: '2026-04-28 10:12:00',
    file: DEMO_FILE,
    scores: scores(1, 1, 1, 1),
    old_size: 0,
    new_size: 2100,
    changed: true,
  },
  {
    sha: 'b2c3d4e5f6789012345678901234567890abcdef',
    sha_short: 'b2c3d4e',
    prev_sha: 'a1b2c3d4e5f6789012345678901234567890abcd',
    prev_sha_short: 'a1b2c3d',
    message: 'refactor: extract validation',
    ts_kst: '2026-04-28 14:30:00',
    file: DEMO_FILE,
    scores: scores(0.72, 0.81, 0.69, 0.77),
    old_size: 2100,
    new_size: 2380,
    changed: true,
  },
  {
    sha: 'c3d4e5f6789012345678901234567890abcdef12',
    sha_short: 'c3d4e5f',
    prev_sha: 'b2c3d4e5f6789012345678901234567890abcdef',
    prev_sha_short: 'b2c3d4e',
    message: 'fix: NPE on empty email',
    ts_kst: '2026-04-29 09:05:00',
    file: DEMO_FILE,
    scores: scores(0.94, 0.96, 0.91, 0.93),
    old_size: 2380,
    new_size: 2410,
    changed: true,
  },
]

export const MOCK_FIRST_LAST: FirstLastSimilarity = {
  file: DEMO_FILE,
  first_sha: 'a1b2c3d4e5f6789012345678901234567890abcd',
  first_sha_short: 'a1b2c3d',
  first_message: 'init user service',
  first_ts_kst: '2026-04-28 10:12:00',
  last_sha: 'c3d4e5f6789012345678901234567890abcdef12',
  last_sha_short: 'c3d4e5f',
  last_message: 'fix: NPE on empty email',
  last_ts_kst: '2026-04-29 09:05:00',
  total_commits: 3,
  first_size: 2100,
  last_size: 2410,
  scores: scores(0.38, 0.45, 0.34, 0.41),
  avg_step_scores: scores(0.82, 0.88, 0.79, 0.85),
}

export const MOCK_PROJECT_SIMILARITY: ProjectSimilarityResult[] = [
  {
    sha: 'a1b2c3d4e5f6789012345678901234567890abcd',
    sha_short: 'a1b2c3d',
    prev_sha: '',
    prev_sha_short: '',
    message: '프로젝트 스캐폴딩',
    ts_kst: '2026-04-28 09:00:00',
    files_changed: 0,
    total_files: 42,
    changed_size: 0,
    total_size: 128000,
    scores: scores(1, 1, 1, 1),
    raw_scores: scores(1, 1, 1, 1),
  },
  {
    sha: 'b2c3d4e5f6789012345678901234567890abcdef',
    sha_short: 'b2c3d4e',
    prev_sha: 'a1b2c3d4e5f6789012345678901234567890abcd',
    prev_sha_short: 'a1b2c3d',
    message: 'domain 레이어 대량 추가',
    ts_kst: '2026-04-28 12:15:00',
    files_changed: 18,
    total_files: 42,
    changed_size: 42000,
    total_size: 128000,
    scores: scores(0.61, 0.58, 0.55, 0.62),
    raw_scores: scores(0.52, 0.49, 0.47, 0.54),
  },
  {
    sha: 'c3d4e5f6789012345678901234567890abcdef12',
    sha_short: 'c3d4e5f',
    prev_sha: 'b2c3d4e5f6789012345678901234567890abcdef',
    prev_sha_short: 'b2c3d4e',
    message: '버그 수정 + 테스트',
    ts_kst: '2026-04-29 09:05:00',
    files_changed: 6,
    total_files: 42,
    changed_size: 8900,
    total_size: 131000,
    scores: scores(0.89, 0.91, 0.86, 0.88),
    raw_scores: scores(0.84, 0.87, 0.82, 0.85),
  },
]

const MOCK_COMMITS: Commit[] = [
  {
    sha: 'f6789012345678901234567890abcdef12345678',
    sha_short: 'f678901',
    message: '[reviewed] docs: 업데이트 훅 가이드 & 대시보드 캡처용 문구 정리\n\n노션 링크 첨부',
    ts: null,
    ts_kst: '2026-04-29 21:41:58',
    is_reviewed: true,
    task_id: 'task_demo_006',
    has_patch: true,
    has_snapshot: true,
    patch_content:
      'diff --git a/README.md b/README.md\n--- a/README.md\n+++ b/README.md\n@@ -12,6 +12,10 @@ ## Dashboard\n-+ - 스크린샷 브랜치는 로컬 데모용 데이터를 사용합니다.\n',
    changed_files: ['README.md'],
    snapshot_files: [
      { path: 'README.md', content: '# AIND-LOG 데모\n\n스크린샷용 스텁 파일입니다.', is_changed: true },
    ],
  },
  {
    sha: 'e5f6789012345678901234567890abcdef123456',
    sha_short: 'e5f6789',
    message: 'chore(ci): 테스트 리포트 아티팩트 업로드',
    ts: null,
    ts_kst: '2026-04-29 18:03:41',
    is_reviewed: false,
    task_id: 'task_demo_004',
    has_patch: true,
    has_snapshot: false,
    patch_content:
      'diff --git a/.github/workflows/ci.yml b/.github/workflows/ci.yml\n+      - uses: actions/upload-artifact@v4\n',
    changed_files: ['.github/workflows/ci.yml'],
    snapshot_files: [],
  },
  {
    sha: 'c3d4e5f6789012345678901234567890abcdef12',
    sha_short: 'c3d4e5f',
    message: '[reviewed] fix: NPE on empty email\n\n- guard clause\n- unit test',
    ts: null,
    ts_kst: '2026-04-29 09:05:12',
    is_reviewed: true,
    task_id: 'task_demo_003',
    has_patch: true,
    has_snapshot: true,
    patch_content:
      'diff --git a/domain/user/UserService.java b/domain/user/UserService.java\n' +
      '--- a/domain/user/UserService.java\n+++ b/domain/user/UserService.java\n@@ -12,6 +12,9 @@ class UserService {\n   User find(String id) {\n+    if (email == null || email.isBlank()) {\n+      throw new IllegalArgumentException("email");\n+    }\n     return repo.find(id);\n   }\n }',
    changed_files: [DEMO_FILE, 'src/main/java/com/backend/domain/user/dto/UserDto.java'],
    snapshot_files: [
      {
        path: DEMO_FILE,
        content:
          '// demo snapshot\npackage com.backend.domain.user.service;\npublic class UserService {\n}\n',
        is_changed: true,
      },
    ],
  },
  {
    sha: 'b2c3d4e5f6789012345678901234567890abcdef',
    sha_short: 'b2c3d4e',
    message: 'refactor: extract validation',
    ts: null,
    ts_kst: '2026-04-28 14:30:00',
    is_reviewed: false,
    task_id: 'task_demo_002',
    has_patch: true,
    has_snapshot: false,
    patch_content:
      'diff --git a/UserService.java b/UserService.java\n--- a/UserService.java\n+++ b/UserService.java\n@@ -1 +1,3 @@\n+import javax.validation.constraints.*;\n',
    changed_files: [DEMO_FILE],
    snapshot_files: [],
  },
]

export function mockSimilarityForFile(file: string): SimilarityResult[] {
  return MOCK_SIMILARITY.map(r => ({ ...r, file: file || DEMO_FILE }))
}

export function mockFirstLastForFile(file: string): FirstLastSimilarity {
  return { ...MOCK_FIRST_LAST, file: file || DEMO_FILE }
}

const emptyTaskExtras = (
  overrides: Partial<
    Pick<
      Task,
      | 'time_to_first_code_sec'
      | 'test_runs_count'
      | 'test_total_sec'
      | 'test_pct_of_duration'
      | 'tools_used'
      | 'file_paths'
      | 'last_result'
    >
  > = {},
) => ({
  time_to_first_code_sec: 42,
  test_runs_count: 2,
  test_total_sec: 120,
  test_pct_of_duration: 12,
  tools_used: ['read_file', 'write_to_file', 'run_terminal_cmd'] as Task['tools_used'],
  file_paths: [DEMO_FILE] as Task['file_paths'],
  last_result: '요약: 테스트 통과.',
  ...overrides,
})

function demoEv(idx: number, taskId: string, event: string, patch: Partial<EventItem>): EventItem {
  return {
    idx,
    taskId,
    event,
    ts: 1716973200000 + idx * 45000,
    ts_kst: patch.ts_kst ?? '2026-04-29 12:00:00',
    tool: patch.tool ?? '-',
    path: patch.path ?? '-',
    command: patch.command ?? '-',
    success: patch.success ?? '-',
    exec_sec: patch.exec_sec ?? null,
    model: patch.model ?? 'cline/anthropic/claude-opus-4.6',
    git_sha: patch.git_sha ?? '-',
    git_message: patch.git_message ?? '-',
    clineVersion: patch.clineVersion ?? 'demo',
    raw_payload: patch.raw_payload ?? {},
    prompt: patch.prompt ?? '',
    initial_task: patch.initial_task ?? '데모 Task',
    result_preview: patch.result_preview ?? '',
    content_preview: patch.content_preview ?? '',
    requires_approval: patch.requires_approval ?? '-',
    previous_state: patch.previous_state ?? {},
    completion_status: patch.completion_status ?? '-',
  }
}

const MOCK_TASKS: Task[] = [
  {
    taskId: 'task_demo_001',
    start_kst: '2026-04-29 09:41:07',
    end_kst: '2026-04-29 09:53:52',
    duration_sec: 765,
    status: '완료됨',
    initial_task: '로그인 API 리팩터',
    first_prompt: '로그인 흐름 점검해줘',
    event_count: 48,
    write_count: 12,
    read_count: 31,
    exec_count: 5,
    ...emptyTaskExtras(),
    resume_count: 0,
    model: 'cline/anthropic/claude-opus-4.6',
  },
  {
    taskId: 'task_demo_002',
    start_kst: '2026-04-29 03:52:54',
    end_kst: '2026-04-29 04:51:54',
    duration_sec: 3540,
    status: '완료됨',
    initial_task: '대시보드 메트릭 카드 디자인',
    first_prompt: '카드를 좀 더 읽기 쉽게',
    event_count: 112,
    write_count: 28,
    read_count: 64,
    exec_count: 9,
    ...emptyTaskExtras({ test_runs_count: 4, last_result: '요약: 시각적 정렬만 반영.' }),
    resume_count: 2,
    model: 'cline/anthropic/claude-sonnet-4.5',
  },
  {
    taskId: 'task_demo_003',
    start_kst: '2026-04-29 06:03:41',
    end_kst: '2026-04-29 06:52:41',
    duration_sec: 2940,
    status: '완료됨',
    initial_task: '유사도 탭 접근성',
    first_prompt: '차트 레이블이 겹치는 문제',
    event_count: 78,
    write_count: 16,
    read_count: 49,
    exec_count: 7,
    ...emptyTaskExtras({ time_to_first_code_sec: 28, file_paths: [DEMO_FILE, 'vite.config.ts'] }),
    resume_count: 0,
    model: 'cline/openai/gpt-5-mini',
  },
  {
    taskId: 'task_demo_004',
    start_kst: '2026-04-28 21:51:52',
    end_kst: '2026-04-29 03:53:53',
    duration_sec: 21721,
    status: '완료됨',
    initial_task: 'CI 파이프라인 보강',
    first_prompt: '실패 원인부터 추적해서 고쳐줘',
    event_count: 203,
    write_count: 41,
    read_count: 112,
    exec_count: 22,
    ...emptyTaskExtras({
      tools_used: ['read_file', 'write_to_file', 'run_terminal_cmd', 'glob_file_search'],
      test_total_sec: 540,
      test_pct_of_duration: 6,
      last_result: '요약: 워크플로 단계별 캐시 분리 적용 완료.',
    }),
    resume_count: 1,
    model: 'cline/anthropic/claude-opus-4.6',
  },
  {
    taskId: 'task_demo_005',
    start_kst: '2026-04-28 07:53:53',
    end_kst: '',
    duration_sec: null,
    status: '진행중',
    initial_task: 'PostgreSQL 성능 회귀',
    first_prompt: '느린 쿼리 p95가 올라갔음',
    event_count: 36,
    write_count: 4,
    read_count: 18,
    exec_count: 6,
    ...emptyTaskExtras({ test_runs_count: 1, tools_used: ['read_file', 'run_terminal_cmd', 'glob_file_search'] }),
    resume_count: 0,
    model: 'cline/anthropic/claude-sonnet-4.5',
  },
  {
    taskId: 'task_demo_006',
    start_kst: '2026-04-29 07:53:53',
    end_kst: '2026-04-29 08:53:53',
    duration_sec: 3600,
    status: '취소됨',
    initial_task: '대규모 리네이밍 패치 적용',
    first_prompt: '패키지 전체를 새 네임스페이스로 바꿔줘',
    event_count: 22,
    write_count: 3,
    read_count: 9,
    exec_count: 2,
    ...emptyTaskExtras({ last_result: '사용자 취소: 범위가 넓어 브랜치 분할 요청함.' }),
    resume_count: 0,
    model: 'cline/anthropic/claude-opus-4.6',
  },
]

const MOCK_EVENTS: EventItem[] = [
  demoEv(1, 'task_demo_001', 'TaskStart', { ts_kst: '2026-04-29 09:41:07', prompt: '로그인 흐름 점검', initial_task: '로그인 API 리팩터' }),
  demoEv(2, 'task_demo_001', 'UserPromptSubmit', { ts_kst: '2026-04-29 09:41:18', prompt: 'OAuth 콜백 핸들러도 같이 봐줘' }),
  demoEv(3, 'task_demo_001', 'PreToolUse', { ts_kst: '2026-04-29 09:41:22', tool: 'read_file', path: DEMO_FILE }),
  demoEv(4, 'task_demo_001', 'PostToolUse', { ts_kst: '2026-04-29 09:42:01', tool: 'write_to_file', path: DEMO_FILE, success: true, exec_sec: 0.42 }),
  demoEv(5, 'task_demo_001', 'PreToolUse', { ts_kst: '2026-04-29 09:43:51', tool: 'run_terminal_cmd', command: './gradlew test', requires_approval: 'false' }),
  demoEv(6, 'task_demo_001', 'PostToolUse', { ts_kst: '2026-04-29 09:53:52', tool: 'run_terminal_cmd', command: './gradlew test', success: true, exec_sec: 118 }),
  demoEv(7, 'task_demo_001', 'TaskComplete', {
    ts_kst: '2026-04-29 09:53:53',
    result_preview: '로그인 API 리팩터 완료, 단위 테스트 14개 통과',
    completion_status: 'ok',
  }),
  demoEv(8, 'task_demo_002', 'TaskStart', { ts_kst: '2026-04-29 03:52:54', model: 'cline/anthropic/claude-sonnet-4.5', initial_task: '대시보드 메트릭 카드 디자인' }),
  demoEv(9, 'task_demo_002', 'TaskResume', { ts_kst: '2026-04-29 03:53:54', completion_status: '재개됨', previous_state: { view: 'Overview' }, model: 'cline/anthropic/claude-sonnet-4.5' }),
  demoEv(10, 'task_demo_004', 'PreCompact', {
    ts_kst: '2026-04-29 06:53:53',
    model: 'cline/anthropic/claude-opus-4.6',
    prompt: '컨텍스트 압축 직전',
    raw_payload: { usage: { tokens_in: 11820, tokens_out: 903 } },
  }),
  demoEv(
    11,
    'task_demo_004',
    'GitCommit',
    {
      ts_kst: '2026-04-29 18:03:41',
      tool: '-',
      success: '-',
      git_sha: 'e5f6789012345678901234567890abcdef123456',
      git_message: 'chore(ci): 테스트 리포트 아티팩트 업로드',
      prompt: '-',
      initial_task: 'CI 파이프라인 보강',
      raw_payload: {
        changed_files: ['.github/workflows/ci.yml'],
        total_files_in_snapshot: 128,
        has_snapshot: false,
      },
    },
  ),
  demoEv(12, 'task_demo_004', 'PreToolUse', { ts_kst: '2026-04-29 18:10:52', tool: 'read_file', path: '.github/workflows/ci.yml', model: 'cline/anthropic/claude-opus-4.6' }),
  demoEv(13, 'task_demo_004', 'UserPromptSubmit', { ts_kst: '2026-04-29 18:53:40', prompt: '리베이스 후 다시 푸시해줘', initial_task: 'CI 파이프라인 보강' }),
  demoEv(
    14,
    'task_demo_006',
    'TaskCancel',
    {
      ts_kst: '2026-04-29 07:53:54',
      result_preview: '사용자 요청으로 범위 축소',
      completion_status: 'cancel',
      model: 'cline/anthropic/claude-opus-4.6',
    },
  ),
  demoEv(
    15,
    'task_demo_004',
    'PostToolUse',
    {
      ts_kst: '2026-04-29 18:53:53',
      tool: 'write_to_file',
      path: 'dashboard/frontend/src/App.tsx',
      success: false,
      exec_sec: 0.11,
      result_preview: '쓰기 충돌 (스텁 재시도)',
      model: 'cline/anthropic/claude-opus-4.6',
    },
  ),
  demoEv(16, 'task_demo_003', 'TaskComplete', {
    ts_kst: '2026-04-29 06:52:41',
    result_preview: '유사도 탭 간격 수정·범례 정렬 적용 완료',
    completion_status: 'ok',
    model: 'cline/openai/gpt-5-mini',
  }),
]

export const MOCK_DASHBOARD: DashboardData = {
  summary: {
    total_events: 2840,
    total_hook_events: 2103,
    total_git_events: 47,
    total_tasks: 120,
    total_resumes: 18,
    rework_rate: 15,
    reviewed_commits: 12,
    total_writes: 340,
    total_reads: 890,
    file_rework_count: 28,
    file_rework_rate: 23.3,
    read_write_ratio: 2.62,
    model_usage: {
      'cline/anthropic/claude-opus-4.6': 1200,
      'cline/anthropic/claude-sonnet-4.5': 903,
      'cline/openai/gpt-5-mini': 312,
    },
    top_model: 'cline/anthropic/claude-opus-4.6',
    unique_models: 3,
    consistency_defect_rate: 4.2,
    contribution_pct: 78,
    automation_pct: 91,
    total_tokens_estimate: 1820000,
    revenue_contribution_pct: 14.6,
  },
  tasks: MOCK_TASKS,
  events: MOCK_EVENTS,
  counts: {
    event_types: [
      { name: 'PreToolUse', count: 892 },
      { name: 'PostToolUse', count: 867 },
      { name: 'TaskStart', count: 120 },
      { name: 'TaskComplete', count: 98 },
      { name: 'TaskResume', count: 18 },
      { name: 'TaskCancel', count: 4 },
      { name: 'UserPromptSubmit', count: 240 },
      { name: 'PreCompact', count: 36 },
      { name: 'GitCommit', count: 47 },
    ],
    tools: [
      { name: 'read_file', count: 890 },
      { name: 'write_to_file', count: 342 },
      { name: 'run_terminal_cmd', count: 215 },
      { name: 'glob_file_search', count: 88 },
      { name: 'search_replace', count: 64 },
      { name: 'grep', count: 120 },
      { name: 'list_dir', count: 56 },
      { name: 'apply_diff', count: 31 },
      { name: 'codebase_search', count: 18 },
    ],
  },
}

export { MOCK_COMMITS }
