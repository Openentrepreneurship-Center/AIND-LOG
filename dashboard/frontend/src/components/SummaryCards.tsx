import { useState } from 'react'
import { Summary } from '../types'

interface Props {
  summary: Summary
}

function MetricCard({
  label,
  value,
  sub,
  color,
  badge,
  tooltip,
}: {
  label: string
  value: string
  sub: string
  color: string
  badge: string
  tooltip?: string
}) {
  return (
    <div
      className={`bg-white rounded-xl border shadow-sm ${color} p-4 flex flex-col gap-1.5 relative group`}
      title={tooltip}
    >
      <span className={`text-[11px] font-semibold px-2 py-0.5 rounded-full w-fit ${badge}`}>{label}</span>
      <p className="text-2xl font-bold text-slate-900 tracking-tight">{value}</p>
      <p className="text-slate-500 text-xs leading-tight">{sub}</p>
      {tooltip && (
        <span className="absolute top-3 right-3 text-slate-400 text-[10px] group-hover:text-slate-600 transition-colors cursor-help">
          ?
        </span>
      )}
    </div>
  )
}

function shortModel(m: string) {
  const parts = m.split('/')
  return parts[parts.length - 1] || m
}

function ModelBadge({ model, count, total }: { model: string; count: number; total: number }) {
  const pct = total > 0 ? Math.round((count / total) * 100) : 0
  const short = shortModel(model)
  return (
    <div className="flex items-center gap-2">
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-0.5">
          <span className="text-xs text-slate-800 font-mono truncate">{short}</span>
          <span className="text-xs text-slate-500 shrink-0 ml-2">
            {count.toLocaleString()}회 ({pct}%)
          </span>
        </div>
        <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
          <div className="h-full bg-violet-500/85 rounded-full" style={{ width: `${pct}%` }} />
        </div>
      </div>
    </div>
  )
}

export default function SummaryCards({ summary: s }: Props) {
  const [llmOpen, setLlmOpen] = useState(true)
  const reworkBad = s.file_rework_rate > 30
  const ratioHigh = s.read_write_ratio > 5
  const totalModelEvents = Object.values(s.model_usage || {}).reduce((a, b) => a + b, 0)
  const sortedModels = Object.entries(s.model_usage || {}).sort((a, b) => b[1] - a[1])

  const fmtPctOpt = (v: number | undefined, digits = 1) =>
    v == null ? '—' : `${v.toFixed(digits)}%`
  const fmtTokens = (n: number | undefined) =>
    n == null
      ? '—'
      : n >= 1_000_000
        ? `${(n / 1_000_000).toFixed(2)}M`
        : n.toLocaleString('ko-KR')

  return (
    <div className="space-y-3">
      {/* 1행: 7칸 균등 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 xl:grid-cols-7 gap-3">
        <MetricCard
          label="Hook 이벤트"
          value={s.total_hook_events.toLocaleString()}
          sub="cline 훅 (Task/Tool/Prompt)"
          color="border-slate-200"
          badge="bg-sky-100 text-sky-900 border border-sky-200"
          tooltip="TaskStart/Complete/Cancel/Resume, Pre/PostToolUse, UserPromptSubmit, PreCompact 합계"
        />
        <MetricCard
          label="GitCommit"
          value={s.total_git_events.toLocaleString()}
          sub="git 커밋 추적 이벤트"
          color="border-slate-200"
          badge="bg-fuchsia-100 text-fuchsia-900 border border-fuchsia-200"
          tooltip="백필된 과거 commit + AIND-LOG post-commit hook 이 기록한 새 commit"
        />
        <MetricCard
          label="총 Task"
          value={s.total_tasks.toLocaleString()}
          sub={`재개 ${s.total_resumes}회 포함`}
          color="border-slate-200"
          badge="bg-violet-100 text-violet-900 border border-violet-200"
        />
        <MetricCard
          label="TaskResume 율"
          value={`${s.rework_rate}%`}
          sub={`재개 ${s.total_resumes} / 전체 ${s.total_tasks}`}
          color="border-slate-200"
          badge={
            s.rework_rate > 50
              ? 'bg-rose-100 text-rose-900 border border-rose-200'
              : 'bg-emerald-100 text-emerald-900 border border-emerald-200'
          }
          tooltip="TaskResume 기반 재업무율. 높을수록 Task를 자주 재개한 것."
        />
        <MetricCard
          label="검수 커밋"
          value={s.reviewed_commits.toLocaleString()}
          sub="[reviewed] 태그"
          color="border-slate-200"
          badge="bg-amber-100 text-amber-950 border border-amber-200"
        />
        <MetricCard
          label="파일 재작업률"
          value={`${s.file_rework_rate}%`}
          sub={`${s.file_rework_count}개 파일 중복 write`}
          color="border-slate-200"
          badge={
            reworkBad
              ? 'bg-rose-100 text-rose-900 border border-rose-200'
              : 'bg-cyan-100 text-cyan-900 border border-cyan-200'
          }
          tooltip="write_to_file로 같은 파일을 2회 이상 수정한 파일 비율."
        />
        <MetricCard
          label="Write / Read"
          value={`${s.total_writes} / ${s.total_reads}`}
          sub="write_to_file · read_file"
          color="border-slate-200"
          badge="bg-indigo-100 text-indigo-900 border border-indigo-200"
        />
      </div>

      {/* 2행: R/W + 확장 지표 5개 = 6칸 꽉 참 */}
      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-3">
        <MetricCard
          label="R/W 비율"
          value={`${s.read_write_ratio}x`}
          sub={ratioHigh ? '읽기 과다 → 효율 점검' : '적정 읽기 비율'}
          color="border-slate-200"
          badge={
            ratioHigh
              ? 'bg-orange-100 text-orange-950 border border-orange-200'
              : 'bg-teal-100 text-teal-900 border border-teal-200'
          }
          tooltip="read_file / write_to_file 비율. 낮을수록 쓰기 대비 읽기가 적어 효율적."
        />
        <MetricCard
          label="일관성 결함율"
          value={fmtPctOpt(s.consistency_defect_rate)}
          sub="스타일·규약 위반 빈도 (데모)"
          color="border-slate-200"
          badge="bg-slate-100 text-slate-900 border border-slate-200"
          tooltip="정적 검사 및 리뷰 태깅 기반 추정치(스크린샷 픽스처)."
        />
        <MetricCard
          label="기여율"
          value={fmtPctOpt(s.contribution_pct, 0)}
          sub="에이전트 산출 MR 머지 비중 추정"
          color="border-slate-200"
          badge="bg-lime-100 text-lime-950 border border-lime-200"
          tooltip="병합 PR 중 자동 초안 포함 비중(데모)."
        />
        <MetricCard
          label="자동화율"
          value={fmtPctOpt(s.automation_pct, 0)}
          sub="승인 없이 통과된 도구 실행"
          color="border-slate-200"
          badge="bg-emerald-100 text-emerald-900 border border-emerald-200"
          tooltip="PreToolUse requires_approval=false 비중 근사(데모)."
        />
        <MetricCard
          label="토큰 수"
          value={fmtTokens(s.total_tokens_estimate)}
          sub="PreCompact 등 누적 추정(데모)"
          color="border-slate-200"
          badge="bg-cyan-100 text-cyan-900 border border-cyan-200"
          tooltip="훅 로그 기반 근사 합계(스크린샷용)."
        />
        <MetricCard
          label="수익 기여"
          value={fmtPctOpt(s.revenue_contribution_pct)}
          sub="인건비 절감 등가 YoY 추정"
          color="border-slate-200"
          badge="bg-amber-100 text-amber-950 border border-amber-200"
          tooltip="내부 모델 가정 기반 KPI(실제 재무 데이터 아님)."
        />
      </div>

      <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
        <button
          onClick={() => setLlmOpen(o => !o)}
          className="w-full flex items-center justify-between px-5 py-3 hover:bg-slate-50 transition-colors border-b border-slate-100"
        >
          <div className="flex items-center gap-3 flex-wrap">
            <span className="w-2 h-2 rounded-full bg-violet-500" />
            <span className="text-sm font-semibold text-slate-900">LLM 사용 현황</span>
            {s.top_model && (
              <span className="text-[11px] px-2 py-0.5 rounded-full bg-violet-50 text-violet-900 border border-violet-200 font-mono">
                {shortModel(s.top_model)}
              </span>
            )}
            <span className="text-xs text-slate-600">
              {s.unique_models}개 모델 · {totalModelEvents.toLocaleString()}회 이벤트
            </span>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-xs text-slate-500 hidden sm:block">
              스크린샷용 픽스처 표시입니다
            </span>
            <span className="text-slate-400 text-xs">{llmOpen ? '▲' : '▼'}</span>
          </div>
        </button>

        {llmOpen && sortedModels.length > 0 && (
          <div className="px-5 pb-4">
            <div className="mt-3 grid grid-cols-1 md:grid-cols-3 gap-3">
              {sortedModels.map(([model, count]) => (
                <ModelBadge key={model} model={model} count={count} total={totalModelEvents} />
              ))}
            </div>
            <p className="text-xs text-slate-500 mt-3">
              이벤트 기준 횟수 (PreToolUse, PostToolUse 등 모든 이벤트 포함)
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
