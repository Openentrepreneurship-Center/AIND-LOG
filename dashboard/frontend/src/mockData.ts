import { DashboardData, Commit, SimilarityResult, ProjectSimilarityResult, ProjectFromFirstItem, ProjectFirstLastSimilarity } from './types'

export const MOCK_DATA: DashboardData = {
  summary: {
    total_events: 1240,
    total_hook_events: 1240,
    event_type_counts: {
      PostToolUse: 480,
      PreToolUse: 480,
      TaskComplete: 38,
      UserPromptSubmit: 42,
      TaskStart: 42,
      TaskCancel: 12,
      TaskResume: 8,
      PreCompact: 0,
    },
    total_git_events: 27,
    total_tasks: 42,
    total_resumes: 8,
    rework_rate: 18.5,
    reviewed_commits: 0,
    total_writes: 216,
    total_reads: 892,
    file_rework_count: 14,
    file_rework_rate: 22.3,
    read_write_ratio: 4.1,
    efficiency_score: 3.19,
    unique_written_files: 63,
    auto_approved_count: 310,
    manual_approval_count: 8,
    auto_approval_by_tool: {
      write_to_file: 120,
      replace_in_file: 95,
      execute_command: 65,
      browser_action: 30,
    },
    manual_approval_by_tool: {
      execute_command: 5,
      write_to_file: 3,
    },
    safe_tools_count: 162,
    safe_tools_by_tool: {
      read_file: 82,
      search_files: 45,
      list_files: 35,
    },
    auto_approve_by_category: {
      파일편집: { auto: 215, manual: 3, safe: 82 },
      명령실행: { auto: 65, manual: 5, safe: 0 },
      브라우저: { auto: 30, manual: 0, safe: 0 },
    },
    inferred_auto_approve: ['파일편집', '명령실행'],
    yolo_mode_suspected: false,
    model_usage: {
      'anthropic/claude-sonnet-4.6': 920,
      'anthropic/claude-haiku-3.5': 320,
    },
    top_model: 'anthropic/claude-sonnet-4.6',
    unique_models: 2,
    token_usage: {},
    total_tokens_in: 0,
    total_tokens_out: 0,
    total_tokens_in_cache: 0,
    total_tokens_out_cache: 0,
    compact_count: 0,
    total_task_cancel_events: 12,
    tasks_ended_canceled: 7,
    task_cancel_rate_pct: 16.7,
    post_cancel_prompt_pairs: 5,
    human_action_count: 62,
    agent_action_count: 998,
    mixed_action_count: 50,
    autonomy_pct: 89.7,
    human_actions_breakdown: { TaskCancel: 12, UserPromptSubmit: 42, TaskStart: 8 },
    agent_actions_breakdown: { PostToolUse: 480, PreToolUse: 480, TaskComplete: 38 },
    mixed_actions_breakdown: { TaskStart: 42, TaskResume: 8 },
    rework_files: [
      { file: 'src/main/service/UserService.java', write_count: 5 },
      { file: 'src/main/controller/AuthController.java', write_count: 4 },
      { file: 'src/main/repository/UserRepository.java', write_count: 3 },
    ],
    top_written_files: [
      { file: 'src/main/service/UserService.java', count: 12 },
      { file: 'src/main/controller/AuthController.java', count: 9 },
      { file: 'src/main/dto/UserDto.java', count: 7 },
      { file: 'build.gradle', count: 5 },
    ],
    top_read_files: [
      { file: 'src/main/service/UserService.java', count: 28 },
      { file: 'README.md', count: 22 },
      { file: 'src/main/controller/AuthController.java', count: 18 },
    ],
    est_total_tokens: 18420,
    est_total_cost_usd: 0.0842,
    est_by_model: [
      {
        model: 'anthropic/claude-sonnet-4.6',
        price_key: 'claude-sonnet',
        price_input: 3.0,
        price_output: 15.0,
        tokens_in: 13800,
        tokens_out: 3200,
        cost_usd: 0.0894,
      },
      {
        model: 'anthropic/claude-haiku-3.5',
        price_key: 'claude-haiku',
        price_input: 0.8,
        price_output: 4.0,
        tokens_in: 980,
        tokens_out: 440,
        cost_usd: 0.0026,
      },
    ],
  },
  tasks: [
    {
      taskId: 'demo-task-001',
      start_kst: '2026-05-08 09:12:00',
      end_kst: '2026-05-08 10:04:00',
      duration_sec: 3120,
      status: '완료됨',
      initial_task: '유저 서비스 리팩토링 및 JWT 인증 추가',
      first_prompt: '유저 서비스 리팩토링 및 JWT 인증 추가',
      event_count: 148,
      write_count: 22,
      read_count: 65,
      exec_count: 8,
      time_to_first_code_sec: 42,
      test_runs_count: 3,
      test_total_sec: 18,
      test_pct_of_duration: 0.6,
      resume_count: 0,
      cancel_count: 0,
      model: 'anthropic/claude-sonnet-4.6',
      tools_used: ['write_to_file', 'replace_in_file', 'execute_command', 'read_file'],
      file_paths: ['src/main/service/UserService.java', 'src/main/controller/AuthController.java'],
      last_result: 'JWT 인증 구현 완료. UserService 리팩토링 및 테스트 통과.',
    },
    {
      taskId: 'demo-task-002',
      start_kst: '2026-05-08 10:15:00',
      end_kst: '2026-05-08 10:22:00',
      duration_sec: 420,
      status: '취소됨',
      initial_task: 'README 업데이트',
      first_prompt: 'README 업데이트',
      event_count: 18,
      write_count: 2,
      read_count: 4,
      exec_count: 0,
      time_to_first_code_sec: 15,
      test_runs_count: 0,
      test_total_sec: null,
      test_pct_of_duration: null,
      resume_count: 0,
      cancel_count: 1,
      model: 'anthropic/claude-sonnet-4.6',
      tools_used: ['read_file', 'write_to_file'],
      file_paths: ['README.md'],
      last_result: '',
    },
    {
      taskId: 'demo-task-003',
      start_kst: '2026-05-08 11:00:00',
      end_kst: '2026-05-08 12:30:00',
      duration_sec: 5400,
      status: '완료됨',
      initial_task: '결제 API 연동 및 에러 핸들링',
      first_prompt: '결제 API 연동 및 에러 핸들링',
      event_count: 212,
      write_count: 34,
      read_count: 98,
      exec_count: 12,
      time_to_first_code_sec: 55,
      test_runs_count: 5,
      test_total_sec: 42,
      test_pct_of_duration: 0.8,
      resume_count: 1,
      cancel_count: 0,
      model: 'anthropic/claude-sonnet-4.6',
      tools_used: ['write_to_file', 'replace_in_file', 'execute_command', 'browser_action'],
      file_paths: ['src/main/service/PaymentService.java'],
      last_result: '결제 API 연동 완료. 에러 핸들링 및 재시도 로직 추가.',
    },
  ],
  events: [
    { idx: 1, taskId: 'demo-task-001', event: 'TaskStart', ts: 1746655920, ts_kst: '2026-05-08 09:12:00', tool: '', path: '', command: '', success: true, exec_sec: null, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '유저 서비스 리팩토링 및 JWT 인증 추가', initial_task: '유저 서비스 리팩토링', result_preview: '', content_preview: '', requires_approval: '', previous_state: {}, completion_status: '' },
    { idx: 2, taskId: 'demo-task-001', event: 'UserPromptSubmit', ts: 1746655921, ts_kst: '2026-05-08 09:12:01', tool: '', path: '', command: '', success: true, exec_sec: null, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '유저 서비스 리팩토링 및 JWT 인증 추가', initial_task: '유저 서비스 리팩토링', result_preview: '', content_preview: '', requires_approval: '', previous_state: {}, completion_status: '' },
    { idx: 3, taskId: 'demo-task-001', event: 'PreToolUse', ts: 1746655963, ts_kst: '2026-05-08 09:12:43', tool: 'read_file', path: 'src/main/service/UserService.java', command: '', success: true, exec_sec: 0.3, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '', initial_task: '유저 서비스 리팩토링', result_preview: '', content_preview: '', requires_approval: 'false', previous_state: {}, completion_status: '' },
    { idx: 4, taskId: 'demo-task-001', event: 'PostToolUse', ts: 1746655964, ts_kst: '2026-05-08 09:12:44', tool: 'read_file', path: 'src/main/service/UserService.java', command: '', success: true, exec_sec: 0.3, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '', initial_task: '유저 서비스 리팩토링', result_preview: '파일 읽기 완료', content_preview: 'public class UserService {', requires_approval: 'false', previous_state: {}, completion_status: '' },
    { idx: 5, taskId: 'demo-task-001', event: 'PreToolUse', ts: 1746656010, ts_kst: '2026-05-08 09:13:30', tool: 'write_to_file', path: 'src/main/service/UserService.java', command: '', success: true, exec_sec: 1.2, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '', initial_task: '유저 서비스 리팩토링', result_preview: '', content_preview: '', requires_approval: 'false', previous_state: {}, completion_status: '' },
    { idx: 6, taskId: 'demo-task-001', event: 'PostToolUse', ts: 1746656011, ts_kst: '2026-05-08 09:13:31', tool: 'write_to_file', path: 'src/main/service/UserService.java', command: '', success: true, exec_sec: 1.2, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '', initial_task: '유저 서비스 리팩토링', result_preview: '파일 수정 완료', content_preview: '', requires_approval: 'false', previous_state: {}, completion_status: '' },
    { idx: 7, taskId: 'demo-task-001', event: 'PreToolUse', ts: 1746656080, ts_kst: '2026-05-08 09:14:40', tool: 'execute_command', path: '', command: './gradlew test', success: true, exec_sec: 18.4, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '', initial_task: '유저 서비스 리팩토링', result_preview: '', content_preview: '', requires_approval: 'true', previous_state: {}, completion_status: '' },
    { idx: 8, taskId: 'demo-task-001', event: 'PostToolUse', ts: 1746656099, ts_kst: '2026-05-08 09:14:59', tool: 'execute_command', path: '', command: './gradlew test', success: true, exec_sec: 18.4, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '', initial_task: '유저 서비스 리팩토링', result_preview: 'BUILD SUCCESS - 42 tests passed', content_preview: '', requires_approval: 'true', previous_state: {}, completion_status: '' },
    { idx: 9, taskId: 'demo-task-001', event: 'TaskComplete', ts: 1746659040, ts_kst: '2026-05-08 10:04:00', tool: '', path: '', command: '', success: true, exec_sec: null, model: 'anthropic/claude-sonnet-4.6', git_sha: 'a1b2c3d', git_message: 'feat: JWT 인증 추가 및 UserService 리팩토링', clineVersion: '3.78.0', raw_payload: {}, prompt: '', initial_task: '유저 서비스 리팩토링', result_preview: 'JWT 인증 구현 완료', content_preview: '', requires_approval: '', previous_state: {}, completion_status: 'completed' },
    { idx: 10, taskId: 'demo-task-002', event: 'TaskStart', ts: 1746659700, ts_kst: '2026-05-08 10:15:00', tool: '', path: '', command: '', success: true, exec_sec: null, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: 'README 업데이트', initial_task: 'README 업데이트', result_preview: '', content_preview: '', requires_approval: '', previous_state: {}, completion_status: '' },
    { idx: 11, taskId: 'demo-task-002', event: 'TaskCancel', ts: 1746660120, ts_kst: '2026-05-08 10:22:00', tool: '', path: '', command: '', success: false, exec_sec: null, model: 'anthropic/claude-sonnet-4.6', git_sha: '', git_message: '', clineVersion: '3.78.0', raw_payload: {}, prompt: '', initial_task: 'README 업데이트', result_preview: '', content_preview: '', requires_approval: '', previous_state: {}, completion_status: 'cancelled' },
  ],
  cancel_followups: [
    {
      taskId: 'demo-task-002',
      cancel_event_idx: 18,
      prompt_event_idx: 19,
      cancel_ts_kst: '2026-05-08 10:22:00',
      prompt_ts_kst: '2026-05-08 10:25:00',
      gap_sec: 180,
      cancel_context: 'README 파일 작성 중',
      prompt_text: '그냥 API 문서 자동 생성으로 바꿔줘',
      follow_result: 'API 문서 자동 생성 스크립트 작성 완료',
      follow_status: '완료',
    },
  ],
  manual_approval_items: [
    {
      event_idx: 88,
      taskId: 'demo-task-001',
      ts_kst: '2026-05-08 09:44:00',
      tool_name: 'execute_command',
      command: 'rm -rf ./build',
      file_path: '',
      task_context: '유저 서비스 리팩토링',
      content_preview: 'rm -rf ./build',
    },
  ],
  human_interaction_items: [
    {
      event_idx: 120,
      taskId: 'demo-task-003',
      ts_kst: '2026-05-08 12:10:00',
      interaction_type: 'ask_followup',
      agent_message: '결제 실패 시 재시도 횟수를 몇 번으로 설정할까요?',
      options: ['1회', '3회', '5회', '직접 입력'],
      user_answer: '3회',
      task_context: '결제 API 연동',
    },
  ],
  counts: {
    event_types: [
      { name: 'PostToolUse', count: 480 },
      { name: 'PreToolUse', count: 480 },
      { name: 'UserPromptSubmit', count: 42 },
      { name: 'TaskComplete', count: 38 },
      { name: 'TaskStart', count: 42 },
      { name: 'TaskCancel', count: 12 },
      { name: 'TaskResume', count: 8 },
    ],
    tools: [
      { name: 'read_file', count: 220 },
      { name: 'write_to_file', count: 120 },
      { name: 'replace_in_file', count: 95 },
      { name: 'execute_command', count: 65 },
      { name: 'search_files', count: 45 },
    ],
  },
}

// ── 커밋 샘플 데이터 ──────────────────────────────────────────────────────────
export const MOCK_COMMITS: Commit[] = [
  {
    sha: 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2',
    sha_short: 'a1b2c3d',
    message: 'feat: JWT 인증 추가 및 UserService 리팩토링',
    ts: 1746659040,
    ts_kst: '2026-05-08 10:04:00',
    is_reviewed: false,
    task_id: 'demo-task-001',
    has_patch: true,
    has_snapshot: false,
    patch_content: `diff --git a/src/main/service/UserService.java b/src/main/service/UserService.java
index 1234567..abcdefg 100644
--- a/src/main/service/UserService.java
+++ b/src/main/service/UserService.java
@@ -12,6 +12,18 @@ public class UserService {
     private final UserRepository userRepository;
+    private final JwtTokenProvider jwtTokenProvider;
+
+    public String login(String email, String password) {
+        User user = userRepository.findByEmail(email)
+            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
+        if (!passwordEncoder.matches(password, user.getPassword())) {
+            throw new BadCredentialsException("Invalid password");
+        }
+        return jwtTokenProvider.createToken(user.getEmail(), user.getRoles());
+    }
 
     public User findById(Long id) {`,
    changed_files: ['src/main/service/UserService.java', 'src/main/controller/AuthController.java'],
    snapshot_files: [],
  },
  {
    sha: 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3',
    sha_short: 'b2c3d4e',
    message: 'feat: 결제 API 연동 및 에러 핸들링 추가',
    ts: 1746664200,
    ts_kst: '2026-05-08 11:30:00',
    is_reviewed: false,
    task_id: 'demo-task-003',
    has_patch: true,
    has_snapshot: false,
    patch_content: `diff --git a/src/main/service/PaymentService.java b/src/main/service/PaymentService.java
new file mode 100644
--- /dev/null
+++ b/src/main/service/PaymentService.java
@@ -0,0 +1,42 @@
+@Service
+public class PaymentService {
+    private static final int MAX_RETRY = 3;
+
+    public PaymentResult processPayment(PaymentRequest request) {
+        for (int i = 0; i < MAX_RETRY; i++) {
+            try {
+                return paymentGateway.charge(request);
+            } catch (PaymentGatewayException e) {
+                if (i == MAX_RETRY - 1) throw e;
+                log.warn("Payment retry {}/{}", i+1, MAX_RETRY);
+            }
+        }
+        throw new PaymentException("Max retries exceeded");
+    }
+}`,
    changed_files: ['src/main/service/PaymentService.java', 'src/main/controller/PaymentController.java'],
    snapshot_files: [],
  },
  {
    sha: 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4',
    sha_short: 'c3d4e5f',
    message: 'fix: 유저 조회 NPE 수정 및 예외 처리 개선',
    ts: 1746668400,
    ts_kst: '2026-05-08 12:40:00',
    is_reviewed: false,
    task_id: null,
    has_patch: true,
    has_snapshot: false,
    patch_content: `diff --git a/src/main/repository/UserRepository.java b/src/main/repository/UserRepository.java
--- a/src/main/repository/UserRepository.java
+++ b/src/main/repository/UserRepository.java
@@ -8,7 +8,7 @@ public interface UserRepository extends JpaRepository<User, Long> {
-    User findByEmail(String email);
+    Optional<User> findByEmail(String email);
 }`,
    changed_files: ['src/main/repository/UserRepository.java'],
    snapshot_files: [],
  },
]

// ── 유사도 샘플 데이터 ────────────────────────────────────────────────────────
export const MOCK_SIMILARITY: SimilarityResult[] = [
  { sha: 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', sha_short: 'a1b2c3d', prev_sha: 'init', prev_sha_short: 'init', message: 'feat: UserService 최초 작성', ts_kst: '2026-05-08 09:00:00', file: 'src/main/service/UserService.java', scores: { L1: 0, L2: 0, L3: 0, L4: 0 }, old_size: 0, new_size: 820, changed: true },
  { sha: 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', sha_short: 'b2c3d4e', prev_sha: 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', prev_sha_short: 'a1b2c3d', message: 'feat: JWT 인증 추가', ts_kst: '2026-05-08 10:04:00', file: 'src/main/service/UserService.java', scores: { L1: 0.72, L2: 0.68, L3: 0.75, L4: 0.81 }, old_size: 820, new_size: 1240, changed: true },
  { sha: 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', sha_short: 'c3d4e5f', prev_sha: 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', prev_sha_short: 'b2c3d4e', message: 'fix: NPE 수정 및 예외 처리', ts_kst: '2026-05-08 12:40:00', file: 'src/main/service/UserService.java', scores: { L1: 0.91, L2: 0.88, L3: 0.93, L4: 0.95 }, old_size: 1240, new_size: 1285, changed: true },
]

export const MOCK_PROJECT_SIMILARITY: ProjectSimilarityResult[] = [
  { sha: 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', sha_short: 'a1b2c3d', prev_sha: '', prev_sha_short: '', message: 'feat: UserService 최초 작성', ts_kst: '2026-05-08 09:00:00', files_changed: 3, added_files: 3, deleted_files: 0, total_files: 12, changed_size: 2400, total_size: 18000, scores: { L1: 0, L2: 0, L3: 0, L4: 0 }, raw_scores: { L1: 0, L2: 0, L3: 0, L4: 0 } },
  { sha: 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', sha_short: 'b2c3d4e', prev_sha: 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', prev_sha_short: 'a1b2c3d', message: 'feat: JWT 인증 추가', ts_kst: '2026-05-08 10:04:00', files_changed: 5, added_files: 2, deleted_files: 0, total_files: 15, changed_size: 3800, total_size: 21000, scores: { L1: 0.74, L2: 0.71, L3: 0.78, L4: 0.83 }, raw_scores: { L1: 0.68, L2: 0.65, L3: 0.72, L4: 0.79 } },
  { sha: 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', sha_short: 'c3d4e5f', prev_sha: 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', prev_sha_short: 'b2c3d4e', message: 'feat: 결제 API 연동', ts_kst: '2026-05-08 11:30:00', files_changed: 4, added_files: 1, deleted_files: 0, total_files: 17, changed_size: 2900, total_size: 23400, scores: { L1: 0.88, L2: 0.85, L3: 0.90, L4: 0.92 }, raw_scores: { L1: 0.82, L2: 0.79, L3: 0.84, L4: 0.88 } },
  { sha: 'd4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5', sha_short: 'd4e5f6a', prev_sha: 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', prev_sha_short: 'c3d4e5f', message: 'fix: NPE 수정 및 예외 처리', ts_kst: '2026-05-08 12:40:00', files_changed: 2, added_files: 0, deleted_files: 0, total_files: 17, changed_size: 480, total_size: 23480, scores: { L1: 0.96, L2: 0.94, L3: 0.97, L4: 0.98 }, raw_scores: { L1: 0.93, L2: 0.91, L3: 0.95, L4: 0.96 } },
]

export const MOCK_PROJECT_FROM_FIRST: ProjectFromFirstItem[] = [
  { sha: 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', sha_short: 'a1b2c3d', message: '기능구현 (초기)', ts_kst: '2026-05-08 09:00:00', file_count: 42, scores: { L1: 1.00, L2: 1.00, L3: 1.00, L4: 1.00 }, ref_sha_short: 'a1b2c3d' },
  { sha: 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', sha_short: 'b2c3d4e', message: 'feat: 사용자 인증 모듈 추가', ts_kst: '2026-05-08 10:04:00', file_count: 52, scores: { L1: 0.91, L2: 0.89, L3: 0.88, L4: 0.94 }, ref_sha_short: 'a1b2c3d' },
  { sha: 'c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4', sha_short: 'c3d4e5f', message: 'feat: 결제 API 연동', ts_kst: '2026-05-08 11:30:00', file_count: 60, scores: { L1: 0.86, L2: 0.84, L3: 0.82, L4: 0.90 }, ref_sha_short: 'a1b2c3d' },
  { sha: 'd4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5', sha_short: 'd4e5f6a', message: 'refactor: 서비스 레이어 전면 개편', ts_kst: '2026-05-08 13:10:00', file_count: 65, scores: { L1: 0.79, L2: 0.77, L3: 0.74, L4: 0.85 }, ref_sha_short: 'a1b2c3d' },
  { sha: 'e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6', sha_short: 'e5f6a1b', message: 'feat: 알림 시스템 구현', ts_kst: '2026-05-08 15:00:00', file_count: 74, scores: { L1: 0.74, L2: 0.72, L3: 0.69, L4: 0.81 }, ref_sha_short: 'a1b2c3d' },
  { sha: 'f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1', sha_short: 'f6a1b2c', message: 'feat: 관리자 대시보드 추가', ts_kst: '2026-05-09 09:30:00', file_count: 89, scores: { L1: 0.70, L2: 0.68, L3: 0.65, L4: 0.77 }, ref_sha_short: 'a1b2c3d' },
  { sha: 'a2b3c4d5e6f7a2b3c4d5e6f7a2b3c4d5e6f7a2b3', sha_short: 'a2b3c4d', message: 'fix: 보안 취약점 패치', ts_kst: '2026-05-09 11:00:00', file_count: 91, scores: { L1: 0.69, L2: 0.67, L3: 0.64, L4: 0.76 }, ref_sha_short: 'a1b2c3d' },
  { sha: 'b3c4d5e6f7a2b3c4d5e6f7a2b3c4d5e6f7a2b3c4', sha_short: 'b3c4d5e', message: 'feat: 통계 API 구현', ts_kst: '2026-05-10 09:00:00', file_count: 98, scores: { L1: 0.67, L2: 0.65, L3: 0.62, L4: 0.74 }, ref_sha_short: 'a1b2c3d' },
]

export const MOCK_PROJECT_FIRST_LAST: ProjectFirstLastSimilarity = {
  L1: 0.67, L2: 0.65, L3: 0.62, L4: 0.74,
  file_count: 98, common_file_count: 15, new_file_count: 56,
  total_commits: 8,
  first_sha_short: 'a1b2c3d',
  last_sha_short: 'b3c4d5e',
}
