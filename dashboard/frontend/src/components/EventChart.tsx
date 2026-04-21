import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell,
} from 'recharts'
import { CountItem } from '../types'

const PALETTE = [
  '#60a5fa', '#a78bfa', '#34d399', '#fbbf24',
  '#f87171', '#fb923c', '#4ade80', '#f472b6',
  '#38bdf8', '#818cf8',
]

const TIP_STYLE = {
  background: '#1f2937',
  border: '1px solid #374151',
  borderRadius: '8px',
  color: '#f9fafb',
  fontSize: 12,
}

interface Props {
  counts: { event_types: CountItem[]; tools: CountItem[] }
}

function MiniBar({ data, title }: { data: CountItem[]; title: string }) {
  if (!data.length) return null
  return (
    <div>
      <p className="text-gray-500 text-xs font-medium mb-2">{title}</p>
      <ResponsiveContainer width="100%" height={data.length * 26 + 10}>
        <BarChart data={data} layout="vertical" margin={{ left: 0, right: 12, top: 0, bottom: 0 }}>
          <XAxis type="number" tick={{ fill: '#6b7280', fontSize: 10 }} axisLine={false} tickLine={false} />
          <YAxis
            type="category"
            dataKey="name"
            tick={{ fill: '#9ca3af', fontSize: 10 }}
            width={120}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip contentStyle={TIP_STYLE} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
          <Bar dataKey="count" radius={[0, 4, 4, 0]} barSize={14}>
            {data.map((_, i) => (
              <Cell key={i} fill={PALETTE[i % PALETTE.length]} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}

export default function EventChart({ counts }: Props) {
  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800 h-full">
      <div className="px-5 py-3 border-b border-gray-800">
        <h2 className="font-semibold text-white text-sm">이벤트 / 도구 분포</h2>
      </div>
      <div className="p-5 space-y-6 overflow-y-auto max-h-[420px]">
        <MiniBar data={counts.event_types} title="이벤트 종류별" />
        {counts.tools.length > 0 && (
          <MiniBar data={counts.tools} title="도구 사용 횟수" />
        )}
      </div>
    </div>
  )
}
