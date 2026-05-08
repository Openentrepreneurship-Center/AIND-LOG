import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell,
} from 'recharts'
import { CountItem } from '../types'

const PALETTE = [
  '#0ea5e9', '#8b5cf6', '#059669', '#d97706',
  '#dc2626', '#ea580c', '#16a34a', '#db2777',
  '#0284c7', '#6366f1',
]

const createTipStyle = () => ({
  background: '#ffffff',
  border: '1px solid #e2e8f0',
  borderRadius: '8px',
  color: '#0f172a',
  boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.06)',
  fontSize: 12,
})

interface Props {
  counts: { event_types: CountItem[]; tools: CountItem[] }
  /** 채워진 카드 높이(스크린샷 레이아웃)용 */
  dense?: boolean
}

function MiniBar({ data, title, dense }: { data: CountItem[]; title: string; dense?: boolean }) {
  if (!data.length) return null
  const rowH = dense ? 30 : 26
  return (
    <div>
      <p className="text-slate-500 text-xs font-semibold mb-2">{title}</p>
      <ResponsiveContainer width="100%" height={data.length * rowH + 24}>
        <BarChart data={data} layout="vertical" margin={{ left: 0, right: 18, top: 0, bottom: 0 }}>
          <XAxis type="number" tick={{ fill: '#64748b', fontSize: 10 }} axisLine={false} tickLine={false} />
          <YAxis
            type="category"
            dataKey="name"
            tick={{ fill: '#475569', fontSize: 10 }}
            width={dense ? 132 : 120}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip contentStyle={createTipStyle()} cursor={{ fill: 'rgba(15,23,42,0.04)' }} />
          <Bar dataKey="count" radius={[0, 4, 4, 0]} barSize={dense ? 16 : 14}>
            {data.map((_, i) => (
              <Cell key={i} fill={PALETTE[i % PALETTE.length]} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}

export default function EventChart({ counts, dense }: Props) {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm h-full min-h-[520px] flex flex-col">
      <div className="px-5 py-3 border-b border-slate-100 bg-slate-50/70 rounded-t-xl">
        <h2 className="font-semibold text-slate-900 text-sm">이벤트 / 도구 분포</h2>
        <p className="text-[11px] text-slate-500 mt-0.5">
          픽스처 데이터 · 종류별·도구별 빈도
        </p>
      </div>
      <div className={`p-5 flex-1 overflow-y-auto ${dense ? 'space-y-8' : 'space-y-6'}`}>
        <MiniBar dense={dense} data={counts.event_types} title="이벤트 종류별" />
        {counts.tools.length > 0 && (
          <MiniBar dense={dense} data={counts.tools} title="도구 사용 횟수" />
        )}
      </div>
    </div>
  )
}
