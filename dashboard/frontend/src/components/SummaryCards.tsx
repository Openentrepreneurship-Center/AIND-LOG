import { useState } from 'react'
import { Summary, TokenUsageEntry } from '../types'

interface Props { summary: Summary }

// ── 포맷 헬퍼 ────────────────────────────────────────────────────────────────
function fmt(n: number) {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}K`
  return n.toLocaleString()
}

function shortModel(m: string) {
  const parts = m.split('/')
  return parts[parts.length - 1] || m
}

// ── 지표 정의 타입 ────────────────────────────────────────────────────────────
interface MetricDef {
  label: string
  value: string
  sub: string
  color: string
  badge: string
  formula: string
  description: string
  example?: string
}

// ── 공식 모달 ────────────────────────────────────────────────────────────────
function FormulaModal({ metric, onClose }: { metric: MetricDef; onClose: () => void }) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="bg-gray-900 border border-gray-700 rounded-2xl p-6 max-w-md w-full mx-4 shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between mb-5">
          <div>
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${metric.badge}`}>
              {metric.label}
            </span>
            <p className="text-3xl font-bold text-white mt-2">{metric.value}</p>
          </div>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-300 text-xl leading-none mt-1">✕</button>
        </div>

        {/* 이게 뭔가요? */}
        <div className="mb-4">
          <p className="text-xs text-gray-500 mb-1.5 font-semibold uppercase tracking-wide">이게 뭔가요?</p>
          <p className="text-sm text-gray-200 leading-relaxed">{metric.description}</p>
        </div>

        {/* 어떻게 계산하나요? */}
        <div className="bg-gray-800/60 rounded-xl p-4 mb-4">
          <p className="text-xs text-gray-500 mb-2 font-semibold uppercase tracking-wide">어떻게 계산하나요?</p>
          <p className="text-sm text-amber-300 leading-relaxed whitespace-pre-line">{metric.formula}</p>
        </div>

        {/* 이 숫자가 높으면? */}
        {metric.example && (
          <div className="bg-gray-800/30 border border-gray-700/50 rounded-xl p-4">
            <p className="text-xs text-gray-500 mb-1.5 font-semibold uppercase tracking-wide">이 숫자가 높으면?</p>
            <p className="text-sm text-gray-300 leading-relaxed whitespace-pre-line">{metric.example}</p>
          </div>
        )}
      </div>
    </div>
  )
}

// ── 지표 카드 ────────────────────────────────────────────────────────────────
function MetricCard({ metric }: { metric: MetricDef }) {
  const [open, setOpen] = useState(false)
  return (
    <>
      <button
        onClick={() => setOpen(true)}
        className={`bg-gray-900 rounded-xl border ${metric.color} p-4 flex flex-col gap-1.5 relative group
          text-left w-full hover:brightness-110 transition-all cursor-pointer active:scale-95`}
      >
        <span className={`text-xs font-medium px-2 py-0.5 rounded-full w-fit ${metric.badge}`}>
          {metric.label}
        </span>
        <p className="text-2xl font-bold text-white tracking-tight">{metric.value}</p>
        <p className="text-gray-500 text-xs leading-tight">{metric.sub}</p>
        <span className="absolute top-3 right-3 text-gray-700 text-[10px] group-hover:text-gray-400 transition-colors">
          ?
        </span>
      </button>
      {open && <FormulaModal metric={metric} onClose={() => setOpen(false)} />}
    </>
  )
}

// ── Auto-Approve 전용 모달 ────────────────────────────────────────────────────
function AutoApproveModal({ summary: s, onClose }: { summary: Summary; onClose: () => void }) {
  const autoTotal = s.auto_approved_count ?? 0
  const manualTotal = s.manual_approval_count ?? 0
  const safeTotal = s.safe_tools_count ?? 0
  const allTotal = autoTotal + manualTotal + safeTotal

  const autoByTool = s.auto_approval_by_tool ?? {}
  const manualByTool = s.manual_approval_by_tool ?? {}

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="bg-gray-900 border border-gray-700 rounded-2xl p-6 max-w-lg w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between mb-5">
          <div>
            <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-orange-500/10 text-orange-400">
              Auto-Approve 분석
            </span>
            <p className="text-sm text-gray-400 mt-2">총 {allTotal}번의 도구 실행 중</p>
          </div>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-300 text-xl leading-none">✕</button>
        </div>

        {/* 요약 바 */}
        <div className="flex h-3 rounded-full overflow-hidden mb-3">
          {allTotal > 0 && <>
            <div className="bg-emerald-500" style={{ width: `${safeTotal / allTotal * 100}%` }} title="승인 불필요" />
            <div className="bg-blue-500" style={{ width: `${autoTotal / allTotal * 100}%` }} title="자동 승인(설정됨)" />
            <div className="bg-amber-500" style={{ width: `${manualTotal / allTotal * 100}%` }} title="수동 승인 필요" />
          </>}
        </div>
        <div className="flex gap-4 mb-5 text-xs">
          <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-emerald-400" />승인 불필요 {safeTotal}회</span>
          <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-blue-400" />자동 승인 {autoTotal}회</span>
          <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-amber-400" />수동 승인 {manualTotal}회</span>
        </div>

        {/* 구분 설명 */}
        <div className="space-y-3 mb-5">
          {/* 승인 불필요 도구 */}
          <div className="bg-emerald-500/5 border border-emerald-500/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="w-2 h-2 rounded-full bg-emerald-400" />
              <span className="text-sm font-semibold text-emerald-300">승인 없이 자동 실행</span>
              <span className="text-xs text-gray-500 ml-auto">{safeTotal}회</span>
            </div>
            <p className="text-xs text-gray-400 mb-2">파일 읽기, 검색 등 코드를 바꾸지 않는 작업. Cline이 항상 자동으로 실행합니다.</p>
            {Object.keys(autoByTool).length === 0 && safeTotal > 0 && (
              <p className="text-xs text-gray-600 italic">도구별 상세 데이터는 다음 세션부터 기록됩니다</p>
            )}
          </div>

          {/* 자동 승인 (설정됨) */}
          <div className="bg-blue-500/5 border border-blue-500/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="w-2 h-2 rounded-full bg-blue-400" />
              <span className="text-sm font-semibold text-blue-300">Auto-Approve 설정으로 자동 실행</span>
              <span className="text-xs text-gray-500 ml-auto">{autoTotal}회</span>
            </div>
            <p className="text-xs text-gray-400 mb-2">
              원래는 승인이 필요한 작업이지만, 사용자가 Auto-Approve를 켜서 자동으로 통과된 경우입니다.
              (예: execute_command를 자동 허용으로 설정)
            </p>
            {Object.entries(autoByTool).length > 0 ? (
              <div className="space-y-1">
                {Object.entries(autoByTool).sort((a,b) => b[1]-a[1]).map(([tool, cnt]) => (
                  <div key={tool} className="flex justify-between text-xs">
                    <span className="text-gray-300 font-mono">{tool}</span>
                    <span className="text-blue-400">{cnt}회 자동 실행됨</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-gray-600 italic">없음 — 설정된 Auto-Approve 실행이 없습니다</p>
            )}
          </div>

          {/* 수동 승인 */}
          <div className="bg-amber-500/5 border border-amber-500/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="w-2 h-2 rounded-full bg-amber-400" />
              <span className="text-sm font-semibold text-amber-300">반드시 사람이 승인해야 실행</span>
              <span className="text-xs text-gray-500 ml-auto">{manualTotal}회</span>
            </div>
            <p className="text-xs text-gray-400 mb-2">
              Cline이 실행 전 사용자의 확인을 요청한 작업입니다. 이 단계에서 사람이 직접 Allow/Deny를 눌러야 합니다.
            </p>
            {Object.entries(manualByTool).length > 0 ? (
              <div className="space-y-1">
                {Object.entries(manualByTool).sort((a,b) => b[1]-a[1]).map(([tool, cnt]) => (
                  <div key={tool} className="flex justify-between text-xs">
                    <span className="text-gray-300 font-mono">{tool}</span>
                    <span className="text-amber-400">{cnt}회 승인 요청</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-gray-600 italic">없음 — 수동 승인 요청이 없었습니다</p>
            )}
          </div>
        </div>

        {/* 결론 */}
        <div className="bg-gray-800/50 rounded-xl p-4">
          <p className="text-xs text-gray-500 mb-1 font-semibold uppercase tracking-wide">결론</p>
          {manualTotal === 0 && autoTotal > 0 && (
            <p className="text-sm text-gray-300">
              execute_command 등 위험 가능성이 있는 도구가 <span className="text-blue-300 font-semibold">모두 자동 승인</span>으로 실행됐습니다.
              사람이 직접 개입해야 할 상황은 없었습니다.
            </p>
          )}
          {manualTotal === 0 && autoTotal === 0 && (
            <p className="text-sm text-gray-300">
              이번 세션에서는 파일 읽기·검색만 사용됐습니다.
              <span className="text-emerald-300 font-semibold"> 코드 수정이나 명령 실행이 없어 승인 요청 자체가 없었습니다.</span>
            </p>
          )}
          {manualTotal > 0 && (
            <p className="text-sm text-gray-300">
              <span className="text-amber-300 font-semibold">{manualTotal}번</span> 사람의 직접 승인이 필요했습니다.
              이 순간들이 사람이 agent를 통제할 수 있는 핵심 개입 지점입니다.
            </p>
          )}
        </div>
      </div>
    </div>
  )
}

// ── LLM 모델 배지 ────────────────────────────────────────────────────────────
function ModelBadge({ model, count, total, tokenEntry }: {
  model: string; count: number; total: number; tokenEntry?: TokenUsageEntry
}) {
  const pct = total > 0 ? Math.round(count / total * 100) : 0
  const short = shortModel(model)
  const hasTokens = tokenEntry && (tokenEntry.tokens_in + tokenEntry.tokens_out) > 0
  return (
    <div className="flex flex-col gap-1.5 bg-gray-800/50 rounded-lg px-3 py-2">
      <div className="flex items-center justify-between">
        <span className="text-xs text-gray-200 font-mono truncate">{short}</span>
        <span className="text-xs text-gray-500 shrink-0 ml-2">{count.toLocaleString()}회 ({pct}%)</span>
      </div>
      <div className="w-full h-1 bg-gray-700 rounded-full overflow-hidden">
        <div className="h-full bg-violet-500/70 rounded-full" style={{ width: `${pct}%` }} />
      </div>
      {hasTokens && (
        <div className="grid grid-cols-2 gap-x-4 gap-y-0.5 pt-1 border-t border-gray-700/50">
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-gray-500">입력</span>
            <span className="text-[10px] text-blue-400 font-mono">{fmt(tokenEntry!.tokens_in)}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-gray-500">출력</span>
            <span className="text-[10px] text-emerald-400 font-mono">{fmt(tokenEntry!.tokens_out)}</span>
          </div>
          {tokenEntry!.tokens_in_cache > 0 && (
            <div className="flex items-center justify-between">
              <span className="text-[10px] text-gray-500">캐시↑</span>
              <span className="text-[10px] text-amber-400 font-mono">{fmt(tokenEntry!.tokens_in_cache)}</span>
            </div>
          )}
          {tokenEntry!.tokens_out_cache > 0 && (
            <div className="flex items-center justify-between">
              <span className="text-[10px] text-gray-500">캐시↓</span>
              <span className="text-[10px] text-amber-300 font-mono">{fmt(tokenEntry!.tokens_out_cache)}</span>
            </div>
          )}
          <div className="col-span-2 flex items-center justify-between">
            <span className="text-[10px] text-gray-500">압축 횟수</span>
            <span className="text-[10px] text-gray-400 font-mono">{tokenEntry!.compact_count}회</span>
          </div>
        </div>
      )}
    </div>
  )
}

// ── 메인 컴포넌트 ─────────────────────────────────────────────────────────────
export default function SummaryCards({ summary: s }: Props) {
  const [llmOpen, setLlmOpen] = useState(false)

  const totalModelEvents = Object.values(s.model_usage || {}).reduce((a, b) => a + b, 0)
  const sortedModels = Object.entries(s.model_usage || {}).sort((a, b) => b[1] - a[1])
  const totalIn = s.total_tokens_in ?? 0
  const totalOut = s.total_tokens_out ?? 0
  const totalCacheIn = s.total_tokens_in_cache ?? 0
  const totalCacheOut = s.total_tokens_out_cache ?? 0
  const hasTokenData = (totalIn + totalOut) > 0
  const compactCount = s.compact_count ?? 0

  const reworkBad = s.file_rework_rate > 30
  const ratioHigh = s.read_write_ratio > 5
  const efficiencyGood = (s.efficiency_score ?? 0) >= 1.5
  const [autoApproveOpen, setAutoApproveOpen] = useState(false)
  const autoTotal = s.auto_approved_count ?? 0
  const manualTotal = s.manual_approval_count ?? 0
  const safeTotal = s.safe_tools_count ?? 0

  // 지표 카드 정의
  const metrics: MetricDef[] = [
    {
      label: 'Hook 이벤트',
      value: s.total_hook_events.toLocaleString(),
      sub: 'cline 훅 (Task/Tool/Prompt)',
      color: 'border-blue-500/40',
      badge: 'bg-blue-500/10 text-blue-400',
      formula: 'Cline이 작업하는 동안 발생한 모든 활동 이벤트 수\n(작업 시작·완료·취소, 파일 읽기·쓰기, 메시지 전송 등)',
      description: 'AI agent가 작업하면서 기록된 모든 행동의 총 횟수입니다. 숫자가 클수록 agent가 더 많은 활동을 했다는 의미입니다.',
      example: '높을수록 → agent가 더 많은 작업을 수행했습니다.\n낮을수록 → 작업 자체가 적었습니다.',
    },
    {
      label: 'GitCommit',
      value: s.total_git_events.toLocaleString(),
      sub: 'git 커밋 추적 이벤트',
      color: 'border-pink-500/40',
      badge: 'bg-pink-500/10 text-pink-400',
      formula: '프로젝트에 기록된 git 커밋의 총 개수',
      description: '지금까지 코드베이스에 저장된 커밋(변경 이력)의 수입니다. agent가 만든 것과 사람이 만든 것 모두 포함됩니다.',
      example: '높을수록 → 코드 변경이 자주 저장되었습니다. (작은 단위로 자주 커밋 = 좋은 습관)',
    },
    {
      label: '총 Task',
      value: s.total_tasks.toLocaleString(),
      sub: `재개 ${s.total_resumes}회 포함`,
      color: 'border-violet-500/40',
      badge: 'bg-violet-500/10 text-violet-400',
      formula: 'Cline 사이드바에서 새 대화창(채팅)을 시작한 횟수',
      description: 'Cline 대화 창의 총 수입니다. 새 대화를 열 때마다 1개씩 카운트됩니다.',
      example: '높을수록 → Cline을 자주, 다양한 작업에 활용했습니다.',
    },
    {
      label: 'TaskResume 율',
      value: `${s.rework_rate}%`,
      sub: `재개 ${s.total_resumes} / 전체 ${s.total_tasks}`,
      color: s.rework_rate > 50 ? 'border-red-500/40' : 'border-emerald-500/40',
      badge: s.rework_rate > 50 ? 'bg-red-500/10 text-red-400' : 'bg-emerald-500/10 text-emerald-400',
      formula: '전체 Task 중 중간에 멈췄다가 다시 이어서 한 비율\n= (재개 횟수 ÷ 전체 Task 수) × 100',
      description: 'Task가 한 번에 끝나지 않고 중단됐다가 재개된 비율입니다. 높으면 작업이 자주 막히거나 사용자가 개입해야 했다는 뜻입니다.',
      example: '낮을수록 좋습니다 → agent가 한 번에 작업을 완료했습니다.\n50% 초과 → 절반 이상의 작업이 중간에 끊겼습니다.',
    },
    {
      label: '검수 커밋',
      value: s.reviewed_commits.toLocaleString(),
      sub: '[reviewed] 태그',
      color: 'border-amber-500/40',
      badge: 'bg-amber-500/10 text-amber-400',
      formula: '커밋 메시지에 "[reviewed]"라고 직접 표시된 커밋 수',
      description: '사람이 직접 확인하고 승인 표시를 남긴 커밋의 수입니다. 커밋 메시지에 [reviewed]를 붙이면 자동으로 집계됩니다.',
      example: '높을수록 → 사람이 직접 검토한 코드가 많습니다.\n0개 → 아직 검수 표시가 된 커밋이 없습니다.',
    },
    {
      label: '재업무율',
      value: `${s.file_rework_rate}%`,
      sub: `${s.file_rework_count}개 파일 중복 write`,
      color: reworkBad ? 'border-red-500/40' : 'border-cyan-500/40',
      badge: reworkBad ? 'bg-red-500/10 text-red-400' : 'bg-cyan-500/10 text-cyan-400',
      formula: '같은 파일을 2번 이상 수정한 파일의 비율\n= (중복 수정 파일 수 ÷ 수정한 전체 파일 수) × 100',
      description: 'agent가 한 파일을 여러 번 고쳤다면 처음에 제대로 못 만든 것입니다. 이 비율이 높으면 agent가 시행착오를 많이 겪었다는 뜻입니다.',
      example: '낮을수록 좋습니다 → 파일을 한 번에 완성했습니다.\n30% 초과 → 수정한 파일 중 30% 이상을 두 번 이상 고쳤습니다.',
    },
    {
      label: 'Write / Read',
      value: `${s.total_writes} / ${s.total_reads}`,
      sub: 'write_to_file · read_file',
      color: 'border-indigo-500/40',
      badge: 'bg-indigo-500/10 text-indigo-400',
      formula: '파일을 새로 쓴 횟수 / 파일을 읽어본 횟수',
      description: 'agent가 파일을 생성·수정한 횟수(Write)와 파일을 열어본 횟수(Read)입니다. 읽기만 많고 쓰기가 적으면 agent가 결과물을 잘 못 만들고 있다는 신호일 수 있습니다.',
      example: '쓰기 ≥ 읽기가 이상적입니다.\n읽기가 쓰기의 5배 이상 → 파일은 많이 보는데 실제 작업은 적습니다.',
    },
    {
      label: 'R/W 비율',
      value: `${s.read_write_ratio}x`,
      sub: ratioHigh ? '읽기 과다 → 효율 점검' : '적정 읽기 비율',
      color: ratioHigh ? 'border-orange-500/40' : 'border-teal-500/40',
      badge: ratioHigh ? 'bg-orange-500/10 text-orange-400' : 'bg-teal-500/10 text-teal-400',
      formula: '파일을 1번 쓸 때 평균 몇 번 읽었는지\n= 읽기 횟수 ÷ 쓰기 횟수',
      description: '코드를 1번 작성하기 위해 파일을 몇 번이나 열어봤는지 나타냅니다. 숫자가 낮을수록 agent가 헤매지 않고 바로 코드를 만들었다는 뜻입니다.',
      example: '0.5x → 쓰기 2번당 읽기 1번 (효율적)\n5.0x 초과 → 파일을 너무 많이 들여다보고 있습니다.',
    },
    {
      label: '효율성 지표',
      value: `${s.efficiency_score ?? 0}`,
      sub: efficiencyGood ? '코드 생성 효율 양호' : '개선 여지 있음',
      color: efficiencyGood ? 'border-emerald-500/40' : 'border-yellow-500/40',
      badge: efficiencyGood ? 'bg-emerald-500/10 text-emerald-400' : 'bg-yellow-500/10 text-yellow-400',
      formula: '"재작업 없이, 읽기만 잔뜩 하지 않고, 바로 결과물을 만들어냈는가?"\n= (1 - 재업무율) ÷ R/W비율',
      description: '재업무율과 읽기/쓰기 비율을 종합한 agent 효율 점수입니다. 파일을 한 번에 올바르게 만들고, 읽기보다 쓰기를 많이 할수록 점수가 높습니다.',
      example: '2.0 이상 → 매우 효율적. 시행착오 없이 바로 결과물 생성\n1.0 미만 → 같은 파일을 반복 수정하거나 파일 탐색이 너무 많음',
    },
  ]

  return (
    <div className="space-y-3">
      {/* 지표 카드 그리드 */}
      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-3">
        {metrics.map(m => <MetricCard key={m.label} metric={m} />)}

        {/* Auto-Approve 카드 — 클릭 시 전용 모달 */}
        <button
          onClick={() => setAutoApproveOpen(true)}
          className="bg-gray-900 rounded-xl border border-orange-500/40 p-4 flex flex-col gap-1.5 relative group
            text-left w-full hover:brightness-110 transition-all cursor-pointer active:scale-95"
        >
          <span className="text-xs font-medium px-2 py-0.5 rounded-full w-fit bg-orange-500/10 text-orange-400">
            Auto-Approve
          </span>
          <p className="text-2xl font-bold text-white tracking-tight">
            {autoTotal + safeTotal}<span className="text-sm text-gray-500 font-normal ml-1">/ {autoTotal + manualTotal + safeTotal}</span>
          </p>
          <p className="text-gray-500 text-xs leading-tight">
            {manualTotal > 0 ? `수동 승인 ${manualTotal}회 있음` : '수동 승인 없음'}
          </p>
          {/* 미니 바 */}
          {(autoTotal + manualTotal + safeTotal) > 0 && (
            <div className="flex h-1 rounded-full overflow-hidden mt-1">
              <div className="bg-emerald-500/60" style={{ width: `${safeTotal / (autoTotal + manualTotal + safeTotal) * 100}%` }} />
              <div className="bg-blue-500/60" style={{ width: `${autoTotal / (autoTotal + manualTotal + safeTotal) * 100}%` }} />
              <div className="bg-amber-500/60" style={{ width: `${manualTotal / (autoTotal + manualTotal + safeTotal) * 100}%` }} />
            </div>
          )}
          <span className="absolute top-3 right-3 text-gray-700 text-[10px] group-hover:text-gray-400 transition-colors">?</span>
        </button>
      </div>
      {autoApproveOpen && (
        <AutoApproveModal summary={s} onClose={() => setAutoApproveOpen(false)} />
      )}

      {/* LLM 사용 현황 패널 */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
        <button
          onClick={() => setLlmOpen(o => !o)}
          className="w-full flex items-center justify-between px-5 py-3 hover:bg-gray-800/40 transition-colors"
        >
          <div className="flex items-center gap-3 flex-wrap">
            <span className="w-2 h-2 rounded-full bg-violet-400 shrink-0" />
            <span className="text-sm font-semibold text-white">LLM 사용 현황</span>
            {s.top_model && (
              <span className="text-xs px-2 py-0.5 rounded-full bg-violet-500/10 text-violet-300 border border-violet-500/20 font-mono">
                {shortModel(s.top_model)}
              </span>
            )}
            <span className="text-xs text-gray-500">
              {s.unique_models}개 모델 · {totalModelEvents.toLocaleString()}회 이벤트
            </span>
            {hasTokenData && (
              <span className="text-xs text-gray-500 flex items-center gap-2">
                <span className="text-blue-400">↑{fmt(totalIn)}</span>
                <span className="text-emerald-400">↓{fmt(totalOut)}</span>
                {(totalCacheIn + totalCacheOut) > 0 && (
                  <span className="text-amber-400">캐시 {fmt(totalCacheIn + totalCacheOut)}</span>
                )}
                <span className="text-gray-600">| 압축 {compactCount}회</span>
              </span>
            )}
          </div>
          <div className="flex items-center gap-3">
            {!hasTokenData && (
              <span className="text-xs text-gray-600 hidden sm:block">
                ※ PreCompact 이벤트 없음 — 이벤트 횟수로 대체
              </span>
            )}
            <span className="text-gray-500 text-xs">{llmOpen ? '▲' : '▼'}</span>
          </div>
        </button>

        {llmOpen && sortedModels.length > 0 && (
          <div className="px-5 pb-4 border-t border-gray-800">
            {hasTokenData && (
              <div className="mt-3 grid grid-cols-2 sm:grid-cols-4 gap-2 mb-4">
                {[
                  { label: '총 입력 토큰', value: fmt(totalIn), color: 'text-blue-400', bg: 'bg-blue-500/5 border-blue-500/20' },
                  { label: '총 출력 토큰', value: fmt(totalOut), color: 'text-emerald-400', bg: 'bg-emerald-500/5 border-emerald-500/20' },
                  { label: '캐시 입력', value: fmt(totalCacheIn), color: 'text-amber-400', bg: 'bg-amber-500/5 border-amber-500/20' },
                  { label: '캐시 출력', value: fmt(totalCacheOut), color: 'text-amber-300', bg: 'bg-amber-500/5 border-amber-500/20' },
                ].map(({ label, value, color, bg }) => (
                  <div key={label} className={`rounded-lg border px-3 py-2 ${bg}`}>
                    <p className="text-[10px] text-gray-500 mb-0.5">{label}</p>
                    <p className={`text-base font-bold font-mono ${color}`}>{value}</p>
                  </div>
                ))}
              </div>
            )}
            <div className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-2">
              {sortedModels.map(([model, count]) => (
                <ModelBadge
                  key={model}
                  model={model}
                  count={count}
                  total={totalModelEvents}
                  tokenEntry={(s.token_usage || {})[model]}
                />
              ))}
            </div>
            <p className="text-xs text-gray-700 mt-3">
              {hasTokenData
                ? '토큰 수: PreCompact 이벤트 기반 | 이벤트 횟수: 모든 hook 이벤트 포함'
                : '토큰 데이터 없음 — PreCompact hook 활성화 시 자동 집계됩니다'}
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
