import { Summary } from '../types'

interface Props { summary: Summary }

const cards = (s: Summary) => [
  {
    label: '총 이벤트',
    value: s.total_events.toLocaleString(),
    sub: '누적 이벤트 수',
    color: 'border-blue-500/40',
    badge: 'bg-blue-500/10 text-blue-400',
  },
  {
    label: '총 작업(Task)',
    value: s.total_tasks.toLocaleString(),
    sub: '시작된 Task 수',
    color: 'border-violet-500/40',
    badge: 'bg-violet-500/10 text-violet-400',
  },
  {
    label: '재업무율',
    value: `${s.rework_rate}%`,
    sub: `재개 ${s.total_resumes}회 / 전체 ${s.total_tasks}건`,
    color: s.rework_rate > 50 ? 'border-red-500/40' : 'border-emerald-500/40',
    badge: s.rework_rate > 50 ? 'bg-red-500/10 text-red-400' : 'bg-emerald-500/10 text-emerald-400',
  },
  {
    label: '검수 커밋',
    value: s.reviewed_commits.toLocaleString(),
    sub: '[reviewed] 태그 커밋',
    color: 'border-amber-500/40',
    badge: 'bg-amber-500/10 text-amber-400',
  },
]

export default function SummaryCards({ summary }: Props) {
  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      {cards(summary).map((c) => (
        <div
          key={c.label}
          className={`bg-gray-900 rounded-xl border ${c.color} p-5 flex flex-col gap-2`}
        >
          <span className={`text-xs font-medium px-2 py-0.5 rounded-full w-fit ${c.badge}`}>
            {c.label}
          </span>
          <p className="text-3xl font-bold text-white tracking-tight">{c.value}</p>
          <p className="text-gray-500 text-xs">{c.sub}</p>
        </div>
      ))}
    </div>
  )
}
