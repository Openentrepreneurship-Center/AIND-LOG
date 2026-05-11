import { useCallback } from 'react'

export type PeriodUnit = 'all' | 'day' | 'week' | 'month'

export interface PeriodState {
  unit: PeriodUnit
  anchor: Date
}

export interface DateRange {
  start_ts: number | null
  end_ts: number | null
}

export function periodToRange(p: PeriodState): DateRange {
  if (p.unit === 'all') return { start_ts: null, end_ts: null }
  const a = new Date(p.anchor)
  a.setHours(0, 0, 0, 0)
  if (p.unit === 'day') {
    const end = new Date(a); end.setDate(end.getDate() + 1); end.setMilliseconds(-1)
    return { start_ts: a.getTime(), end_ts: end.getTime() }
  }
  if (p.unit === 'week') {
    const day = a.getDay()
    const mon = new Date(a); mon.setDate(a.getDate() + (day === 0 ? -6 : 1 - day)); mon.setHours(0,0,0,0)
    const sun = new Date(mon); sun.setDate(mon.getDate() + 7); sun.setMilliseconds(-1)
    return { start_ts: mon.getTime(), end_ts: sun.getTime() }
  }
  const start = new Date(a.getFullYear(), a.getMonth(), 1)
  const end = new Date(a.getFullYear(), a.getMonth() + 1, 1); end.setMilliseconds(-1)
  return { start_ts: start.getTime(), end_ts: end.getTime() }
}

function isCurrentPeriod(p: PeriodState): boolean {
  const now = new Date()
  const a = p.anchor
  if (p.unit === 'day')   return a.toDateString() === now.toDateString()
  if (p.unit === 'week') {
    const { start_ts, end_ts } = periodToRange(p)
    const t = now.getTime()
    return start_ts !== null && end_ts !== null && t >= start_ts && t <= end_ts
  }
  if (p.unit === 'month') return a.getFullYear() === now.getFullYear() && a.getMonth() === now.getMonth()
  return true
}

function anchorLabel(p: PeriodState): { main: string; sub?: string } {
  const a = p.anchor
  const pad = (n: number) => String(n).padStart(2, '0')
  if (p.unit === 'day') {
    const days = ['일', '월', '화', '수', '목', '금', '토']
    return {
      main: `${a.getFullYear()}.${pad(a.getMonth()+1)}.${pad(a.getDate())}`,
      sub: days[a.getDay()] + '요일',
    }
  }
  if (p.unit === 'week') {
    const { start_ts, end_ts } = periodToRange(p)
    const s = start_ts ? new Date(start_ts) : a
    const e = end_ts   ? new Date(end_ts)   : a
    const fmt = (d: Date) => `${pad(d.getMonth()+1)}.${pad(d.getDate())}`
    return {
      main: `${fmt(s)} – ${fmt(e)}`,
      sub: `${s.getFullYear()}년 ${Math.ceil(s.getDate() / 7)}주차`,
    }
  }
  const monthNames = ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월']
  return {
    main: `${a.getFullYear()}년 ${monthNames[a.getMonth()]}`,
  }
}

function navigate(p: PeriodState, dir: -1 | 1): PeriodState {
  const a = new Date(p.anchor)
  if (p.unit === 'day')   a.setDate(a.getDate() + dir)
  if (p.unit === 'week')  a.setDate(a.getDate() + dir * 7)
  if (p.unit === 'month') a.setMonth(a.getMonth() + dir)
  return { ...p, anchor: a }
}

const UNITS: { id: PeriodUnit; label: string; icon: string }[] = [
  { id: 'all',   label: '전체',   icon: '∞' },
  { id: 'day',   label: '일',     icon: '□' },
  { id: 'week',  label: '주',     icon: '▤' },
  { id: 'month', label: '월',     icon: '▦' },
]

interface Props {
  value: PeriodState
  onChange: (next: PeriodState) => void
  isDark?: boolean
}

export default function PeriodSelector({ value, onChange, isDark = true }: Props) {
  const handleUnit = useCallback((unit: PeriodUnit) => {
    onChange({ unit, anchor: new Date() })
  }, [onChange])

  const handleNav = useCallback((dir: -1 | 1) => {
    onChange(navigate(value, dir))
  }, [value, onChange])

  const isCurrent = isCurrentPeriod(value)
  const label = value.unit !== 'all' ? anchorLabel(value) : null

  const card = isDark ? 'bg-gray-900/80 border-gray-700/60' : 'bg-white border-gray-200'
  const unitActive = 'bg-blue-600 text-white shadow-sm shadow-blue-900/40'
  const unitInactive = isDark
    ? 'text-gray-500 hover:text-gray-200 hover:bg-gray-800/70'
    : 'text-gray-400 hover:text-gray-700 hover:bg-gray-100'
  const navCls = isDark
    ? 'text-gray-500 hover:text-white hover:bg-gray-700/60 active:bg-gray-700'
    : 'text-gray-400 hover:text-gray-700 hover:bg-gray-100'
  const textMain = isDark ? 'text-gray-100' : 'text-gray-800'
  const textSub  = isDark ? 'text-gray-500' : 'text-gray-400'

  return (
    <div className={`flex items-center gap-3 rounded-2xl border px-4 py-2 backdrop-blur-sm ${card}`}>
      {/* 단위 선택 pill */}
      <div className={`flex items-center rounded-xl p-0.5 gap-0.5 ${isDark ? 'bg-gray-800/80' : 'bg-gray-100'}`}>
        {UNITS.map(u => (
          <button
            key={u.id}
            onClick={() => handleUnit(u.id)}
            className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all duration-150 ${
              value.unit === u.id ? unitActive : unitInactive
            }`}
          >
            {u.label}
          </button>
        ))}
      </div>

      {/* 기간 네비게이션 */}
      {label && (
        <>
          <div className={`w-px h-5 ${isDark ? 'bg-gray-700' : 'bg-gray-200'}`} />

          <div className="flex items-center gap-1.5">
            <button
              onClick={() => handleNav(-1)}
              className={`w-7 h-7 rounded-lg flex items-center justify-center text-base transition-all duration-150 ${navCls}`}
              title="이전"
            >‹</button>

            <div className="flex flex-col items-center min-w-[130px]">
              <span className={`text-sm font-semibold leading-tight tracking-tight ${textMain}`}>
                {label.main}
              </span>
              {label.sub && (
                <span className={`text-[10px] leading-tight ${textSub}`}>{label.sub}</span>
              )}
            </div>

            <button
              onClick={() => handleNav(1)}
              className={`w-7 h-7 rounded-lg flex items-center justify-center text-base transition-all duration-150 ${navCls}`}
              title="다음"
            >›</button>
          </div>

          {/* 현재로 돌아가기 */}
          {!isCurrent && (
            <button
              onClick={() => onChange({ ...value, anchor: new Date() })}
              className={`text-[11px] px-2.5 py-1 rounded-lg border font-medium transition-all ${
                isDark
                  ? 'border-blue-700/60 text-blue-400 hover:bg-blue-900/30 hover:border-blue-500'
                  : 'border-blue-300 text-blue-500 hover:bg-blue-50'
              }`}
            >
              현재로
            </button>
          )}

          {/* 현재 기간 배지 */}
          {isCurrent && (
            <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${
              isDark ? 'bg-blue-900/40 text-blue-400' : 'bg-blue-50 text-blue-500'
            }`}>
              현재
            </span>
          )}
        </>
      )}

      {/* 전체 선택 시 안내 */}
      {value.unit === 'all' && (
        <span className={`text-xs ${isDark ? 'text-gray-600' : 'text-gray-400'}`}>
          프로젝트 전체 기간
        </span>
      )}
    </div>
  )
}
