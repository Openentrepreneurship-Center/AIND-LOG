import { useState } from 'react'
import { Summary } from '../types'

interface Props { summary: Summary }

function MetricCard({
  label, value, sub, color, badge, tooltip,
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
      className={`bg-gray-900 rounded-xl border ${color} p-4 flex flex-col gap-1.5 relative group`}
      title={tooltip}
    >
      <span className={`text-xs font-medium px-2 py-0.5 rounded-full w-fit ${badge}`}>
        {label}
      </span>
      <p className="text-2xl font-bold text-white tracking-tight">{value}</p>
      <p className="text-gray-500 text-xs leading-tight">{sub}</p>
      {tooltip && (
        <span className="absolute top-3 right-3 text-gray-700 text-[10px] group-hover:text-gray-400 transition-colors cursor-help">
          ?
        </span>
      )}
    </div>
  )
}

function shortModel(m: string) {
  // "cline/anthropic/claude-opus-4.6" → "claude-opus-4.6"
  const parts = m.split('/')
  return parts[parts.length - 1] || m
}

function ModelBadge({ model, count, total }: { model: string; count: number; total: number }) {
  const pct = total > 0 ? Math.round(count / total * 100) : 0
  const short = shortModel(model)
  return (
    <div className="flex items-center gap-2">
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-0.5">
          <span className="text-xs text-gray-300 font-mono truncate">{short}</span>
          <span className="text-xs text-gray-500 shrink-0 ml-2">{count.toLocaleString()}회 ({pct}%)</span>
        </div>
        <div className="w-full h-1.5 bg-gray-800 rounded-full overflow-hidden">
          <div className="h-full bg-violet-500/70 rounded-full" style={{ width: `${pct}%` }} />
        </div>
      </div>
    </div>
  )
}

export default function SummaryCards({ summary: s }: Props) {
  const [llmOpen, setLlmOpen] = useState(false)
  const reworkBad = s.file_rework_rate > 30
  const ratioHigh = s.read_write_ratio > 5
  const totalModelEvents = Object.values(s.model_usage || {}).reduce((a, b) => a + b, 0)
  const sortedModels = Object.entries(s.model_usage || {}).sort((a, b) => b[1] - a[1])

  return (
    <div className="space-y-3">
      {/* 지표 카드 행 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 xl:grid-cols-7 gap-3">
        <MetricCard
          label="총 이벤트"
          value={s.total_events.toLocaleString()}
          sub="누적 이벤트 수"
          color="border-blue-500/40"
          badge="bg-blue-500/10 text-blue-400"
        />
        <MetricCard
          label="총 Task"
          value={s.total_tasks.toLocaleString()}
          sub={`재개 ${s.total_resumes}회 포함`}
          color="border-violet-500/40"
          badge="bg-violet-500/10 text-violet-400"
        />
        <MetricCard
          label="TaskResume 율"
          value={`${s.rework_rate}%`}
          sub={`재개 ${s.total_resumes} / 전체 ${s.total_tasks}`}
          color={s.rework_rate > 50 ? 'border-red-500/40' : 'border-emerald-500/40'}
          badge={s.rework_rate > 50 ? 'bg-red-500/10 text-red-400' : 'bg-emerald-500/10 text-emerald-400'}
          tooltip="TaskResume 기반 재업무율. 높을수록 Task를 자주 재개한 것."
        />
        <MetricCard
          label="검수 커밋"
          value={s.reviewed_commits.toLocaleString()}
          sub="[reviewed] 태그"
          color="border-amber-500/40"
          badge="bg-amber-500/10 text-amber-400"
        />
        <MetricCard
          label="파일 재작업률"
          value={`${s.file_rework_rate}%`}
          sub={`${s.file_rework_count}개 파일 중복 write`}
          color={reworkBad ? 'border-red-500/40' : 'border-cyan-500/40'}
          badge={reworkBad ? 'bg-red-500/10 text-red-400' : 'bg-cyan-500/10 text-cyan-400'}
          tooltip="write_to_file로 같은 파일을 2회 이상 수정한 파일 비율."
        />
        <MetricCard
          label="Write / Read"
          value={`${s.total_writes} / ${s.total_reads}`}
          sub="write_to_file · read_file"
          color="border-indigo-500/40"
          badge="bg-indigo-500/10 text-indigo-400"
        />
        <MetricCard
          label="R/W 비율"
          value={`${s.read_write_ratio}x`}
          sub={ratioHigh ? '읽기 과다 → 효율 점검' : '적정 읽기 비율'}
          color={ratioHigh ? 'border-orange-500/40' : 'border-teal-500/40'}
          badge={ratioHigh ? 'bg-orange-500/10 text-orange-400' : 'bg-teal-500/10 text-teal-400'}
          tooltip="read_file / write_to_file 비율. 낮을수록 쓰기 대비 읽기가 적어 효율적."
        />
      </div>

      {/* LLM 사용 현황 패널 */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
        <button
          onClick={() => setLlmOpen(o => !o)}
          className="w-full flex items-center justify-between px-5 py-3 hover:bg-gray-800/40 transition-colors"
        >
          <div className="flex items-center gap-3">
            <span className="w-2 h-2 rounded-full bg-violet-400" />
            <span className="text-sm font-semibold text-white">LLM 사용 현황</span>
            {s.top_model && (
              <span className="text-xs px-2 py-0.5 rounded-full bg-violet-500/10 text-violet-300 border border-violet-500/20 font-mono">
                {shortModel(s.top_model)}
              </span>
            )}
            <span className="text-xs text-gray-500">
              {s.unique_models}개 모델 · {totalModelEvents.toLocaleString()}회 이벤트
            </span>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-xs text-gray-600 hidden sm:block">
              ※ Cline 훅은 토큰 수를 전달하지 않아 이벤트 횟수로 대체
            </span>
            <span className="text-gray-500 text-xs">{llmOpen ? '▲' : '▼'}</span>
          </div>
        </button>

        {llmOpen && sortedModels.length > 0 && (
          <div className="px-5 pb-4 border-t border-gray-800">
            <div className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-3">
              {sortedModels.map(([model, count]) => (
                <ModelBadge key={model} model={model} count={count} total={totalModelEvents} />
              ))}
            </div>
            <p className="text-xs text-gray-700 mt-3">
              이벤트 기준 횟수 (PreToolUse, PostToolUse 등 모든 이벤트 포함)
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
