import { useState, useMemo } from 'react'
import { EventItem } from '../types'

const EVENT_COLOR: Record<string, { text: string; dot: string; badge: string }> = {
  TaskStart:        { text: 'text-blue-400',    dot: 'bg-blue-400',    badge: 'bg-blue-500/10 text-blue-400 border-blue-500/20' },
  TaskComplete:     { text: 'text-emerald-400', dot: 'bg-emerald-400', badge: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' },
  TaskCancel:       { text: 'text-red-400',     dot: 'bg-red-400',     badge: 'bg-red-500/10 text-red-400 border-red-500/20' },
  TaskResume:       { text: 'text-amber-400',   dot: 'bg-amber-400',   badge: 'bg-amber-500/10 text-amber-400 border-amber-500/20' },
  UserPromptSubmit: { text: 'text-violet-400',  dot: 'bg-violet-400',  badge: 'bg-violet-500/10 text-violet-400 border-violet-500/20' },
  PreToolUse:       { text: 'text-orange-400',  dot: 'bg-orange-400',  badge: 'bg-orange-500/10 text-orange-400 border-orange-500/20' },
  PostToolUse:      { text: 'text-teal-400',    dot: 'bg-teal-400',    badge: 'bg-teal-500/10 text-teal-400 border-teal-500/20' },
  GitCommit:        { text: 'text-pink-400',    dot: 'bg-pink-400',    badge: 'bg-pink-500/10 text-pink-400 border-pink-500/20' },
}
const DEFAULT_COLOR = { text: 'text-gray-300', dot: 'bg-gray-400', badge: 'bg-gray-700 text-gray-300 border-gray-600' }

function JsonViewer({ data }: { data: unknown }) {
  const text = JSON.stringify(data, null, 2)
  return (
    <pre className="text-xs text-gray-300 bg-gray-950 rounded-lg p-3 overflow-auto max-h-64 whitespace-pre-wrap break-all font-mono leading-relaxed">
      {text}
    </pre>
  )
}

function DetailPanel({ ev }: { ev: EventItem }) {
  const sections: { label: string; value: unknown; show: boolean }[] = [
    { label: '프롬프트', value: ev.prompt, show: !!ev.prompt },
    { label: '초기 요청', value: ev.initial_task, show: !!ev.initial_task },
    { label: '실행 커맨드', value: ev.command, show: !!ev.command },
    { label: '파일 경로', value: ev.path, show: !!ev.path },
    { label: '콘텐츠 미리보기', value: ev.content_preview, show: !!ev.content_preview },
    { label: '결과 요약', value: ev.result_preview, show: !!ev.result_preview },
    { label: 'Git SHA', value: ev.git_sha, show: !!ev.git_sha },
    { label: 'Git 메시지', value: ev.git_message, show: !!ev.git_message },
    { label: '완료 상태', value: ev.completion_status, show: !!ev.completion_status },
    { label: '승인 필요', value: ev.requires_approval, show: ev.requires_approval !== '' && ev.requires_approval !== undefined },
    { label: '이전 상태 (TaskResume)', value: ev.previous_state, show: Object.keys(ev.previous_state || {}).length > 0 },
    { label: '모델', value: ev.model, show: !!ev.model },
    { label: 'Cline 버전', value: ev.clineVersion, show: !!ev.clineVersion },
  ]

  return (
    <div className="px-6 py-4 bg-gray-800/40 border-t border-gray-800 space-y-4">
      {/* 핵심 필드 */}
      <div className="grid grid-cols-2 gap-x-8 gap-y-3">
        {sections.filter(s => s.show).map(s => (
          <div key={s.label} className={typeof s.value === 'object' ? 'col-span-2' : ''}>
            <p className="text-gray-500 text-xs font-medium mb-1">{s.label}</p>
            {typeof s.value === 'object' ? (
              <JsonViewer data={s.value} />
            ) : (
              <p className="text-gray-300 text-xs font-mono whitespace-pre-wrap break-all">{String(s.value)}</p>
            )}
          </div>
        ))}
      </div>

      {/* 전체 payload JSON */}
      <div>
        <p className="text-gray-500 text-xs font-medium mb-1">전체 payload (raw JSON)</p>
        <JsonViewer data={ev.raw_payload} />
      </div>
    </div>
  )
}

const ALL_EVENTS = 'ALL'

interface Props { events: EventItem[] }

export default function EventLog({ events }: Props) {
  const [expanded, setExpanded] = useState<number | null>(null)
  const [filter, setFilter] = useState(ALL_EVENTS)
  const [search, setSearch] = useState('')

  const eventTypes = useMemo(() => {
    const types = Array.from(new Set(events.map(e => e.event))).sort()
    return [ALL_EVENTS, ...types]
  }, [events])

  const filtered = useMemo(() => {
    return [...events]
      .reverse()
      .filter(ev => filter === ALL_EVENTS || ev.event === filter)
      .filter(ev => {
        if (!search.trim()) return true
        const q = search.toLowerCase()
        return (
          ev.event.toLowerCase().includes(q) ||
          ev.tool.toLowerCase().includes(q) ||
          ev.command.toLowerCase().includes(q) ||
          ev.prompt.toLowerCase().includes(q) ||
          ev.path.toLowerCase().includes(q) ||
          ev.taskId.toLowerCase().includes(q) ||
          ev.git_message.toLowerCase().includes(q)
        )
      })
  }, [events, filter, search])

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800">
      {/* Header */}
      <div className="px-5 py-3 border-b border-gray-800 flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-2 flex-1">
          <h2 className="font-semibold text-white text-sm">이벤트 로그</h2>
          <span className="text-gray-500 text-xs">전체 {events.length}건 · 표시 {filtered.length}건</span>
        </div>
        {/* 검색 */}
        <input
          type="text"
          placeholder="검색 (이벤트·도구·경로·프롬프트…)"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="bg-gray-800 border border-gray-700 rounded-lg px-3 py-1.5 text-xs text-gray-200 placeholder-gray-600 focus:outline-none focus:border-gray-500 w-64"
        />
        {/* 이벤트 필터 */}
        <div className="flex flex-wrap gap-1.5">
          {eventTypes.map(type => {
            const c = EVENT_COLOR[type]
            const active = filter === type
            return (
              <button
                key={type}
                onClick={() => setFilter(type)}
                className={`px-2.5 py-1 rounded-full text-xs font-medium border transition-all ${
                  active
                    ? (c ? `${c.badge} border` : 'bg-gray-600 text-white border-gray-500')
                    : 'bg-transparent text-gray-500 border-gray-700 hover:border-gray-500'
                }`}
              >
                {type === ALL_EVENTS ? '전체' : type}
              </button>
            )
          })}
        </div>
      </div>

      {/* Table */}
      <div className="overflow-auto max-h-[500px]">
        <table className="w-full text-xs font-mono">
          <thead className="sticky top-0 bg-gray-900 z-10 border-b border-gray-800">
            <tr>
              {['#', '시각(KST)', 'Task ID', '이벤트', '도구/유형', '주요 정보', '소요(초)', '성공'].map(h => (
                <th key={h} className="text-left px-4 py-2 text-gray-500 font-medium whitespace-nowrap">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filtered.map(ev => {
              const c = EVENT_COLOR[ev.event] ?? DEFAULT_COLOR
              const isOpen = expanded === ev.idx
              // 행에 보여줄 주요 정보
              const mainInfo =
                ev.prompt || ev.initial_task || ev.command ||
                ev.path || ev.git_message || ev.result_preview || '-'

              return (
                <>
                  <tr
                    key={ev.idx}
                    onClick={() => setExpanded(isOpen ? null : ev.idx)}
                    className={`border-b border-gray-800/40 cursor-pointer transition-colors ${
                      isOpen ? 'bg-gray-800/50' : 'hover:bg-gray-800/30'
                    }`}
                  >
                    <td className="px-4 py-2 text-gray-600">{ev.idx}</td>
                    <td className="px-4 py-2 text-gray-400 whitespace-nowrap">{ev.ts_kst}</td>
                    <td className="px-4 py-2 text-gray-600 whitespace-nowrap">
                      {ev.taskId ? ev.taskId.slice(-8) : '-'}
                    </td>
                    <td className="px-4 py-2 whitespace-nowrap">
                      <span className={`flex items-center gap-1.5 font-semibold ${c.text}`}>
                        <span className={`w-1.5 h-1.5 rounded-full flex-shrink-0 ${c.dot}`} />
                        {ev.event}
                      </span>
                    </td>
                    <td className="px-4 py-2 text-gray-400">
                      {ev.tool || (ev.git_sha ? 'GitCommit' : '-')}
                    </td>
                    <td className="px-4 py-2 text-gray-400 max-w-sm truncate">
                      {mainInfo}
                    </td>
                    <td className="px-4 py-2 text-gray-400">
                      {ev.exec_sec != null ? `${ev.exec_sec}s` : '-'}
                    </td>
                    <td className="px-4 py-2">
                      {ev.success === true  ? <span className="text-emerald-400 font-bold">✓</span>
                      : ev.success === false ? <span className="text-red-400 font-bold">✗</span>
                      : <span className="text-gray-700">—</span>}
                    </td>
                  </tr>
                  {isOpen && (
                    <tr key={`${ev.idx}-detail`} className="border-b border-gray-700">
                      <td colSpan={8} className="p-0">
                        <DetailPanel ev={ev} />
                      </td>
                    </tr>
                  )}
                </>
              )
            })}
            {filtered.length === 0 && (
              <tr>
                <td colSpan={8} className="px-4 py-8 text-center text-gray-600">
                  일치하는 이벤트가 없습니다
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
