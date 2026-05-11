import { useState, useMemo, useEffect } from 'react'
import { Summary, TokenUsageEntry, CancelFollowup, ManualApprovalItem, HumanInteractionItem, Task, CountItem, ProjectFirstLastSimilarity } from '../types'

/** 행 그룹 컨테이너: PCL/PNL/BCL/기본정보 레이블 포함 */
function SectionRow({ label, sub, color, children }: {
  label: string
  sub?: string
  color: 'info' | 'blue' | 'purple' | 'emerald' | 'red' | 'mixed'
  children: React.ReactNode
}) {
  const cfg = {
    info:    { bar: 'bg-gray-500',    text: 'text-gray-300',    sub: 'text-gray-500',    border: 'border-gray-700/50' },
    blue:    { bar: 'bg-blue-500',    text: 'text-blue-200',    sub: 'text-blue-400/70', border: 'border-blue-500/30' },
    purple:  { bar: 'bg-purple-500',  text: 'text-purple-200',  sub: 'text-purple-400/70', border: 'border-purple-500/30' },
    emerald: { bar: 'bg-emerald-500', text: 'text-emerald-200', sub: 'text-emerald-400/70', border: 'border-emerald-500/30' },
    red:     { bar: 'bg-red-500',     text: 'text-red-300',     sub: 'text-red-400/60',  border: 'border-red-500/25'  },
    mixed:   { bar: 'bg-gray-500',    text: 'text-gray-300',    sub: 'text-gray-500',    border: 'border-gray-700/50' },
  }[color]
  return (
    <div className="space-y-2">
      <div className={`flex items-center gap-3 border-b pb-2 ${cfg!.border}`}>
        <span className={`w-1.5 h-6 rounded-full shrink-0 ${cfg!.bar}`} />
        <div>
          <span className={`text-base font-bold tracking-wide ${cfg!.text}`}>{label}</span>
          {sub && <span className={`ml-2 text-xs font-normal ${cfg!.sub}`}>{sub}</span>}
        </div>
      </div>
      {children}
    </div>
  )
}

/** 모달이 열려 있는 동안 body 스크롤·드래그를 잠급니다. */
function useBodyLock() {
  useEffect(() => {
    const prev = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => { document.body.style.overflow = prev }
  }, [])
}

interface Props {
  summary: Summary
  cancelFollowups?: CancelFollowup[]
  manualApprovalItems?: ManualApprovalItem[]
  humanInteractionItems?: HumanInteractionItem[]
  tasks?: Task[]
  eventTypeCounts?: CountItem[]
  projectFirstLast?: ProjectFirstLastSimilarity
  projectFirstLastLoading?: boolean
}

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
export interface DetailRow { [key: string]: string | number }
export interface DetailColumn { key: string; label: string; mono?: boolean; align?: 'left' | 'right'; highlight?: boolean }

interface MetricDef {
  label: string
  value: string
  sub: string
  color: string
  badge: string
  formula: string
  computation?: string
  interpretation?: string
  description: string
  example?: string
  detail?: {
    title: string
    columns: DetailColumn[]
    rows: DetailRow[]
    emptyText?: string
  }
}

// ── 공식 모달 ────────────────────────────────────────────────────────────────
function FormulaModal({ metric, onClose, onOpenDetail }: { metric: MetricDef; onClose: () => void; onOpenDetail: () => void }) {
  useBodyLock()
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl p-6 max-w-md w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
        onMouseDown={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between mb-5">
          <div>
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${metric.badge}`}>
              {metric.label}
            </span>
            <p className="text-3xl font-bold text-white mt-2 tracking-tight">{metric.value}</p>
            {metric.interpretation && (
              <p className="text-sm text-gray-400 mt-1">{metric.interpretation}</p>
            )}
          </div>
          <button onClick={onClose} className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg shrink-0 ml-2">✕</button>
        </div>

        {/* 이게 뭔가요? */}
        <div className="mb-4">
          <p className="text-xs text-gray-500 mb-1.5 font-semibold uppercase tracking-wide">이게 뭔가요?</p>
          <p className="text-sm text-gray-200 leading-relaxed">{metric.description}</p>
        </div>

        {/* 어떻게 계산하나요? */}
        <div className="bg-gray-800/60 rounded-xl p-4 mb-3">
          <p className="text-xs text-gray-500 mb-2 font-semibold uppercase tracking-wide">계산 방법</p>
          <p className="text-sm text-amber-300 leading-relaxed whitespace-pre-line">{metric.formula}</p>
        </div>

        {/* 실제 데이터 대입 계산식 */}
        {metric.computation && (
          <div className="bg-blue-500/5 border border-blue-500/20 rounded-xl p-4 mb-3">
            <p className="text-xs text-blue-400/70 mb-2 font-semibold uppercase tracking-wide">실제 데이터 대입</p>
            <p className="text-sm text-blue-200 leading-relaxed whitespace-pre-line font-mono">{metric.computation}</p>
          </div>
        )}

        {/* 이 숫자가 의미하는 것 */}
        {metric.example && (
          <div className="bg-gray-800/30 border border-gray-700/50 rounded-xl p-4 mb-3">
            <p className="text-xs text-gray-500 mb-1.5 font-semibold uppercase tracking-wide">해석 기준</p>
            <p className="text-sm text-gray-300 leading-relaxed whitespace-pre-line">{metric.example}</p>
          </div>
        )}
        {/* 세부 데이터 보기 버튼 */}
        {metric.detail && (
          <button
            onClick={onOpenDetail}
            className="w-full mt-1 rounded-xl bg-gray-800/60 border border-gray-700 text-gray-300 text-sm font-semibold py-2.5 hover:bg-gray-700/60 active:scale-[0.99] transition-all"
          >
            세부 데이터 보기 →
          </button>
        )}
      </div>
    </div>
  )
}

// ── 세부 데이터 모달 (공용) ───────────────────────────────────────────────────
// 짧은 숫자/코드 열은 min-width 고정, 나머지는 콘텐츠 크기대로 확장
const COL_MIN_WIDTHS: Record<string, string> = {
  rank: '32px', status: '64px', pct: '52px', rw: '60px', duration: '80px',
  writes: '56px', reads: '56px', count: '64px', write_count: '72px',
  cancel_count: '72px', resume_count: '72px',
  start_kst: '152px', end_kst: '152px', ts_kst: '152px',
}

function GenericDetailModal({ title, badge, badgeClass, columns, rows, emptyText = '데이터가 없습니다.', onClose }: {
  title: string
  badge: string
  badgeClass: string
  columns: DetailColumn[]
  rows: DetailRow[]
  emptyText?: string
  onClose: () => void
}) {
  useBodyLock()
  const [search, setSearch] = useState('')
  const filtered = search
    ? rows.filter(r => Object.values(r).some(v => String(v).toLowerCase().includes(search.toLowerCase())))
    : rows

  return (
    <div
      className="fixed inset-0 z-[60] flex items-center justify-center bg-black/75 backdrop-blur-md"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl p-6 w-full mx-4 shadow-2xl flex flex-col"
        style={{ maxWidth: '92vw', maxHeight: '88vh' }}
        onMouseDown={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-4 shrink-0">
          <div>
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${badgeClass}`}>{badge}</span>
            <h3 className="text-lg font-bold text-white mt-1">{title}</h3>
            <p className="text-xs text-gray-500">
              {filtered.length}{filtered.length !== rows.length ? ` / ${rows.length}` : ''}개 항목
              <span className="ml-2 text-gray-700">← → 가로 스크롤 가능</span>
            </p>
          </div>
          <button onClick={onClose} className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg shrink-0">✕</button>
        </div>

        {/* 검색 */}
        {rows.length > 5 && (
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="검색…"
            className="mb-3 shrink-0 bg-gray-800 border border-gray-700 rounded-lg px-3 py-1.5 text-sm text-gray-200 placeholder-gray-600 focus:outline-none focus:border-gray-500 w-full"
          />
        )}

        {/* 테이블 — 가로·세로 스크롤 모두 허용, 텍스트 truncate 없음 */}
        {filtered.length === 0 ? (
          <div className="flex-1 flex items-center justify-center text-gray-600 text-sm">{emptyText}</div>
        ) : (
          <div className="flex-1 overflow-auto min-h-0">
            <table className="text-xs border-collapse" style={{ minWidth: '100%', width: 'max-content' }}>
              <thead className="sticky top-0 bg-gray-950 z-10">
                <tr className="border-b border-gray-800">
                  {columns.map(c => (
                    <th
                      key={c.key}
                      className={`px-3 py-2 font-semibold text-gray-500 uppercase tracking-wide whitespace-nowrap ${c.align === 'right' ? 'text-right' : 'text-left'}`}
                      style={{ minWidth: COL_MIN_WIDTHS[c.key] ?? '80px' }}
                    >
                      {c.label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map((row, i) => (
                  <tr key={i} className="border-b border-gray-800/40 hover:bg-gray-800/30 transition-colors">
                    {columns.map(c => {
                      const val = String(row[c.key] ?? '—')
                      const isHighlight = c.highlight && Number(row[c.key]) > 1
                      return (
                        <td
                          key={c.key}
                          className={[
                            'px-3 py-2 whitespace-nowrap',
                            c.mono ? 'font-mono' : '',
                            c.align === 'right' ? 'text-right' : '',
                            isHighlight ? 'text-amber-300 font-bold' : 'text-gray-300',
                          ].join(' ')}
                        >
                          {val}
                        </td>
                      )
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

// ── 지표 카드 ────────────────────────────────────────────────────────────────
function MetricCard({ metric }: { metric: MetricDef }) {
  const [open, setOpen] = useState(false)
  const [detailOpen, setDetailOpen] = useState(false)
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
          {metric.detail ? '↗' : '?'}
        </span>
      </button>
      {open && (
        <FormulaModal
          metric={metric}
          onClose={() => setOpen(false)}
          onOpenDetail={() => { setOpen(false); setDetailOpen(true) }}
        />
      )}
      {detailOpen && metric.detail && (
        <GenericDetailModal
          title={metric.detail.title}
          badge={metric.label}
          badgeClass={metric.badge}
          columns={metric.detail.columns}
          rows={metric.detail.rows}
          emptyText={metric.detail.emptyText}
          onClose={() => setDetailOpen(false)}
        />
      )}
    </>
  )
}

// ── follow_status 뱃지 ───────────────────────────────────────────────────────
const FOLLOW_STATUS_STYLE: Record<string, string> = {
  완료:   'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30',
  재취소: 'bg-red-500/15 text-red-300 border border-red-500/30',
  진행중: 'bg-gray-700/50 text-gray-400 border border-gray-600/30',
}

// ── 취소→재프롬프트 세부 데이터 모달 (2단계) ─────────────────────────────────
function CancelDetailModal({ rows, onClose }: { rows: CancelFollowup[]; onClose: () => void }) {
  useBodyLock()
  const [copied, setCopied] = useState(false)
  const json = useMemo(() => JSON.stringify(rows, null, 2), [rows])

  async function handleCopy() {
    try { await navigator.clipboard.writeText(json); setCopied(true); setTimeout(() => setCopied(false), 2000) }
    catch { /* ignore */ }
  }

  return (
    <div
      className="fixed inset-0 z-[60] flex items-center justify-center bg-black/80 backdrop-blur-md p-4"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl w-full max-w-5xl max-h-[88vh] flex flex-col shadow-2xl"
        onMouseDown={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-800 shrink-0">
          <div>
            <p className="text-sm font-semibold text-white">취소 직후 프롬프트 &amp; 응답 결과 — 전체 목록</p>
            <p className="text-xs text-gray-500 mt-0.5">{rows.length}건 · TaskCancel → UserPromptSubmit 짝</p>
          </div>
          <div className="flex items-center gap-2">
            <button type="button" onClick={handleCopy} disabled={rows.length === 0}
              className="text-xs px-3 py-1.5 rounded-lg bg-gray-800 text-gray-200 border border-gray-700 hover:bg-gray-700 disabled:opacity-40 transition-colors">
              {copied ? '✓ 복사됨' : 'JSON 복사'}
            </button>
            <button onClick={onClose}
              className="ml-1 w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg">✕</button>
          </div>
        </div>

        {rows.length === 0 ? (
          <div className="flex-1 flex items-center justify-center text-gray-600 text-sm py-16">짝 매칭된 데이터가 없습니다.</div>
        ) : (
          <div className="overflow-auto flex-1">
            <table className="w-full text-xs">
              <thead className="sticky top-0 bg-gray-950 border-b border-gray-800 z-10">
                <tr>
                  {['상태', '간격(초)', '취소 시각', 'Task ID', '취소 당시 작업', '재지시 프롬프트', '응답 결과'].map(h => (
                    <th key={h} className="text-left px-3 py-2.5 text-gray-500 font-medium whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {rows.map((r, i) => (
                  <tr key={i} className="border-b border-gray-800/50 align-top hover:bg-gray-800/30 transition-colors">
                    <td className="px-3 py-3 whitespace-nowrap">
                      <span className={`inline-block px-2 py-0.5 rounded-full text-[10px] font-medium ${FOLLOW_STATUS_STYLE[r.follow_status] ?? FOLLOW_STATUS_STYLE['진행중']}`}>
                        {r.follow_status}
                      </span>
                    </td>
                    <td className="px-3 py-3 text-fuchsia-400 font-mono whitespace-nowrap">
                      {r.gap_sec != null ? `${r.gap_sec}s` : '—'}
                    </td>
                    <td className="px-3 py-3 text-gray-500 whitespace-nowrap">{r.cancel_ts_kst}</td>
                    <td className="px-3 py-3 text-gray-600 font-mono text-[10px] max-w-[100px] truncate" title={r.taskId}>{r.taskId}</td>
                    <td className="px-3 py-3 text-amber-200/80 whitespace-pre-wrap min-w-[180px] max-w-xs leading-relaxed">
                      {r.cancel_context || <span className="text-gray-600 italic">—</span>}
                    </td>
                    <td className="px-3 py-3 text-gray-200 whitespace-pre-wrap min-w-[180px] max-w-xs leading-relaxed">{r.prompt_text}</td>
                    <td className="px-3 py-3 min-w-[180px] max-w-xs">
                      {r.follow_result
                        ? <p className="text-emerald-300 whitespace-pre-wrap leading-relaxed">{r.follow_result}</p>
                        : <span className="text-gray-600 italic">{r.follow_status === '재취소' ? '재취소됨' : '아직 완료 없음'}</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

// ── 취소→재프롬프트 요약 모달 (1단계) ────────────────────────────────────────
function CancelFollowupModal({ rows, onClose }: { rows: CancelFollowup[]; onClose: () => void }) {
  useBodyLock()
  const [showDetail, setShowDetail] = useState(false)

  const avgGap = useMemo(() => {
    const valid = rows.filter(r => r.gap_sec != null).map(r => r.gap_sec as number)
    if (!valid.length) return null
    return Math.round(valid.reduce((a, b) => a + b, 0) / valid.length * 10) / 10
  }, [rows])
  const minGap = useMemo(() => {
    const v = rows.filter(r => r.gap_sec != null).map(r => r.gap_sec as number)
    return v.length ? Math.min(...v) : null
  }, [rows])
  const maxGap = useMemo(() => {
    const v = rows.filter(r => r.gap_sec != null).map(r => r.gap_sec as number)
    return v.length ? Math.max(...v) : null
  }, [rows])

  const completedCount = rows.filter(r => r.follow_status === '완료').length
  const recanceledCount = rows.filter(r => r.follow_status === '재취소').length
  const pendingCount = rows.filter(r => r.follow_status === '진행중').length
  const preview = rows.slice(0, 3)

  return (
    <>
      <div
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md"
        onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
      >
        <div
          className="bg-gray-950 border border-gray-700 rounded-2xl p-6 max-w-lg w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
          onMouseDown={e => e.stopPropagation()}
        >
          {/* 헤더 */}
          <div className="flex items-start justify-between mb-5">
            <div>
              <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-fuchsia-500/10 text-fuchsia-400">
                취소→재프롬프트 분석
              </span>
              <p className="text-3xl font-bold text-white mt-2">
                {rows.length}<span className="text-base text-gray-500 font-normal ml-1">건</span>
              </p>
              <p className="text-xs text-gray-500 mt-1">TaskCancel 직후 동일 Task에서 이어진 사용자 발화</p>
            </div>
            <button onClick={onClose}
              className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg mt-1">✕</button>
          </div>

          {/* 결과 현황 */}
          {rows.length > 0 && (
            <div className="grid grid-cols-3 gap-2 mb-4">
              {[
                { label: '완료', value: completedCount, color: 'text-emerald-400', bg: 'bg-emerald-500/5 border-emerald-500/20' },
                { label: '재취소', value: recanceledCount, color: 'text-red-400', bg: 'bg-red-500/5 border-red-500/20' },
                { label: '진행중', value: pendingCount, color: 'text-gray-400', bg: 'bg-gray-700/30 border-gray-700' },
              ].map(({ label, value, color, bg }) => (
                <div key={label} className={`rounded-xl border p-3 text-center ${bg}`}>
                  <p className="text-[10px] text-gray-500 mb-1">{label}</p>
                  <p className={`text-xl font-bold font-mono ${color}`}>{value}</p>
                </div>
              ))}
            </div>
          )}

          {/* 간격 통계 */}
          {rows.length > 0 && (
            <div className="grid grid-cols-3 gap-2 mb-5">
              {[
                { label: '평균 간격', value: avgGap != null ? `${avgGap}s` : '—' },
                { label: '최소 간격', value: minGap != null ? `${minGap}s` : '—' },
                { label: '최대 간격', value: maxGap != null ? `${maxGap}s` : '—' },
              ].map(({ label, value }) => (
                <div key={label} className="bg-gray-800/40 rounded-xl p-3 text-center">
                  <p className="text-[10px] text-gray-500 mb-1">{label}</p>
                  <p className="text-base font-bold text-white font-mono">{value}</p>
                </div>
              ))}
            </div>
          )}

          {/* 최근 3건 미리보기 */}
          {preview.length > 0 && (
            <div className="mb-5">
              <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide mb-2">최근 발화 미리보기</p>
              <div className="space-y-2">
                {preview.map((r, i) => (
                  <div key={i} className="bg-gray-800/40 rounded-xl px-3 py-3 space-y-1.5">
                    <div className="flex items-center gap-2">
                      <span className={`inline-block px-1.5 py-0.5 rounded text-[10px] font-medium ${FOLLOW_STATUS_STYLE[r.follow_status] ?? FOLLOW_STATUS_STYLE['진행중']}`}>
                        {r.follow_status}
                      </span>
                      <span className="text-[10px] text-gray-600 font-mono">{r.cancel_ts_kst}</span>
                      {r.gap_sec != null && (
                        <span className="text-[10px] text-fuchsia-400/70">{r.gap_sec}s 후 재지시</span>
                      )}
                    </div>
                    {r.cancel_context && (
                      <p className="text-xs text-amber-200/70 leading-relaxed line-clamp-2 border-l-2 border-amber-500/40 pl-2">
                        취소 전: {r.cancel_context}
                      </p>
                    )}
                    <p className="text-xs text-gray-300 leading-relaxed line-clamp-2">↳ {r.prompt_text}</p>
                    {r.follow_result && (
                      <p className="text-xs text-emerald-400/80 leading-relaxed line-clamp-2 border-t border-gray-700/50 pt-1.5">
                        응답: {r.follow_result}
                      </p>
                    )}
                  </div>
                ))}
                {rows.length > 3 && (
                  <p className="text-xs text-gray-600 text-center">…외 {rows.length - 3}건</p>
                )}
              </div>
            </div>
          )}

          {rows.length === 0 && (
            <div className="text-center py-6 text-gray-600 text-sm">아직 취소 직후 재지시 데이터가 없습니다.</div>
          )}

          {/* 세부 데이터 보기 버튼 */}
          <button
            type="button"
            onClick={() => setShowDetail(true)}
            disabled={rows.length === 0}
            className="w-full mt-2 rounded-xl bg-fuchsia-900/30 border border-fuchsia-500/30 text-fuchsia-200
              text-sm font-semibold py-2.5 hover:bg-fuchsia-900/50 active:scale-[0.99] transition-all
              disabled:opacity-40 disabled:cursor-not-allowed"
          >
            세부 데이터 보기 →
          </button>
        </div>
      </div>

      {showDetail && (
        <CancelDetailModal rows={rows} onClose={() => setShowDetail(false)} />
      )}
    </>
  )
}

// ── 에이전트 자율성 모달 ──────────────────────────────────────────────────────
function AutonomyModal({ summary: s, onClose }: { summary: Summary; onClose: () => void }) {
  useBodyLock()
  const [showDetail, setShowDetail] = useState(false)
  const h = s.human_action_count ?? 0
  const a = s.agent_action_count ?? 0
  const m = s.mixed_action_count ?? 0
  const total = h + a + m
  const pct = (n: number) => total > 0 ? Math.round(n / total * 100) : 0
  const hBd = s.human_actions_breakdown ?? {}
  const aBd = s.agent_actions_breakdown ?? {}
  const mBd = s.mixed_actions_breakdown ?? {}

  const HUMAN_COLOR  = 'bg-rose-500'
  const AGENT_COLOR  = 'bg-emerald-500'
  const MIXED_COLOR  = 'bg-sky-500'

  return (
    <>
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl p-6 max-w-lg w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
        onMouseDown={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between mb-4">
          <div>
            <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400">
              에이전트 자율성 분석
            </span>
            <p className="text-3xl font-bold text-white mt-2">
              {s.autonomy_pct ?? 0}<span className="text-base text-gray-500 font-normal ml-0.5">%</span>
            </p>
            <p className="text-xs text-gray-500 mt-1">Agent 단독 행동 ÷ 전체 행동 × 100</p>
          </div>
          <button onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg mt-1">✕</button>
        </div>

        {/* 실제 계산식 */}
        <div className="bg-blue-500/5 border border-blue-500/20 rounded-xl p-3 mb-4">
          <p className="text-xs text-blue-400/70 mb-1.5 font-semibold uppercase tracking-wide">실제 데이터 대입</p>
          <p className="text-sm text-blue-200 font-mono whitespace-pre-line">{
            `Agent 자율 행동  = ${a}회\n사람 개입 (혼합) = ${m}회\n사람 직접 개입   = ${h}회\n전체             = ${total}회\n\n${a} ÷ ${total} × 100 = ${s.autonomy_pct ?? 0}%`
          }</p>
        </div>

        {/* 시각적 바 — 왼쪽: Agent(자율), 오른쪽: 사람 관여(혼합+순수사람) */}
        {total > 0 && (
          <>
            <div className="flex h-3 rounded-full overflow-hidden mb-2">
              <div className={`${AGENT_COLOR} transition-all`} style={{ width: `${pct(a)}%` }} title={`Agent 자율 ${a}회`} />
              <div className={`${MIXED_COLOR} transition-all`} style={{ width: `${pct(m)}%` }} title={`혼합(사람 트리거) ${m}회`} />
              <div className={`${HUMAN_COLOR} transition-all`} style={{ width: `${pct(h)}%` }} title={`사람 직접 개입 ${h}회`} />
            </div>
            {/* 범례: Agent | [혼합+사람] 으로 묶어 표현 */}
            <div className="flex flex-col gap-1.5 mb-5">
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-1.5 text-xs"><span className="w-2 h-2 rounded-full bg-emerald-400" />Agent 자율 행동</span>
                <span className="text-xs font-mono text-emerald-400">{a}회 ({pct(a)}%)</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-1.5 text-xs text-gray-400">
                  <span className="flex gap-0.5">
                    <span className="w-2 h-2 rounded-full bg-sky-400" />
                    <span className="w-2 h-2 rounded-full bg-rose-400" />
                  </span>
                  사람 개입 (혼합 + 직접)
                </span>
                <span className="text-xs font-mono text-rose-300">{h + m}회 ({pct(h + m)}%)</span>
              </div>
            </div>
            {/* 세부 구분 */}
            <div className="flex gap-3 mb-5 text-xs text-gray-500">
              <span className="flex items-center gap-1"><span className="w-1.5 h-1.5 rounded-full bg-sky-400" />혼합(트리거) {m}회</span>
              <span className="flex items-center gap-1"><span className="w-1.5 h-1.5 rounded-full bg-rose-400" />직접 개입 {h}회</span>
            </div>
          </>
        )}

        {/* b) Agent 단독 */}
        <div className="bg-emerald-500/5 border border-emerald-500/20 rounded-xl p-4 mb-3">
          <div className="flex items-center gap-2 mb-2">
            <span className="w-2 h-2 rounded-full bg-emerald-400 shrink-0" />
            <span className="text-sm font-semibold text-emerald-300">b) Agent 단독 행동</span>
            <span className="text-xs text-gray-500 ml-auto">{a}회</span>
          </div>
          <p className="text-xs text-gray-400 mb-2">사람 개입 없이 Agent가 자율적으로 수행한 이벤트입니다.</p>
          <div className="space-y-1">
            {Object.entries(aBd).map(([ev, cnt]) => (
              <div key={ev} className="flex justify-between text-xs">
                <span className="text-gray-300 font-mono">{ev}</span>
                <span className="text-emerald-400">{cnt}회</span>
              </div>
            ))}
          </div>
        </div>

        {/* c) 혼합 */}
        <div className="bg-sky-500/5 border border-sky-500/20 rounded-xl p-4 mb-3">
          <div className="flex items-center gap-2 mb-2">
            <span className="w-2 h-2 rounded-full bg-sky-400 shrink-0" />
            <span className="text-sm font-semibold text-sky-300">c) 혼합 — 사람 트리거, Agent 참여</span>
            <span className="text-xs text-gray-500 ml-auto">{m}회</span>
          </div>
          <p className="text-xs text-gray-400 mb-2">
            사람이 직접 시작해야 발생하는 이벤트입니다. Agent도 처리에 관여하지만 <span className="text-sky-300">트리거는 항상 사람</span>이므로 자율성 분모에 포함됩니다.
          </p>
          <div className="space-y-1">
            {Object.entries(mBd).map(([ev, cnt]) => (
              <div key={ev} className="flex justify-between text-xs">
                <span className="text-gray-300 font-mono">{ev}</span>
                <span className="text-sky-400">{cnt}회</span>
              </div>
            ))}
          </div>
        </div>

        {/* a) 사람 단독 */}
        <div className="bg-rose-500/5 border border-rose-500/20 rounded-xl p-4 mb-4">
          <div className="flex items-center gap-2 mb-2">
            <span className="w-2 h-2 rounded-full bg-rose-400 shrink-0" />
            <span className="text-sm font-semibold text-rose-300">a) 사람 단독 행동</span>
            <span className="text-xs text-gray-500 ml-auto">{h}회</span>
          </div>
          <p className="text-xs text-gray-400 mb-2">사람이 직접 개입·명령·취소를 한 이벤트입니다. 낮을수록 자율성이 높습니다.</p>
          <div className="space-y-1">
            {Object.entries(hBd).map(([ev, cnt]) => (
              <div key={ev} className="flex justify-between text-xs">
                <span className="text-gray-300 font-mono">{ev}</span>
                <span className="text-rose-400">{cnt}회</span>
              </div>
            ))}
          </div>
        </div>

        {/* 세부 데이터 보기 버튼 */}
        <button
          onClick={() => setShowDetail(true)}
          className="w-full text-sm text-indigo-400 border border-indigo-500/30 rounded-xl py-2.5 hover:bg-indigo-500/10 transition-colors"
        >
          전체 세부 데이터 보기 →
        </button>

      </div>
    </div>
    {showDetail && (
      <GenericDetailModal
        title="자율성 — 이벤트 유형별 분류"
        badge="에이전트 자율성"
        badgeClass="bg-indigo-900/50 text-indigo-300"
        columns={[
          { key: 'event', label: '이벤트 타입' },
          { key: 'category', label: '분류' },
          { key: 'count', label: '횟수', align: 'right', mono: true },
          { key: 'pct', label: '비율', align: 'right', mono: true },
        ]}
        rows={[
          ...Object.entries(hBd).map(([ev, cnt]) => ({ event: ev, category: '사람 단독', count: cnt, pct: `${pct(cnt as number)}%` })),
          ...Object.entries(aBd).map(([ev, cnt]) => ({ event: ev, category: 'Agent 단독', count: cnt, pct: `${pct(cnt as number)}%` })),
          ...Object.entries(mBd).map(([ev, cnt]) => ({ event: ev, category: '혼합(사람 트리거)', count: cnt, pct: `${pct(cnt as number)}%` })),
        ].sort((a, b) => (b.count as number) - (a.count as number))}
        onClose={() => setShowDetail(false)}
      />
    )}
    </>
  )
}

// ── 사람 상호작용 세부 모달 ───────────────────────────────────────────────────
function HumanInteractionDetailModal({ items, onClose }: { items: HumanInteractionItem[]; onClose: () => void }) {
  useBodyLock()
  const [copied, setCopied] = useState(false)
  const json = useMemo(() => JSON.stringify(items, null, 2), [items])
  async function handleCopy() {
    try { await navigator.clipboard.writeText(json); setCopied(true); setTimeout(() => setCopied(false), 2000) }
    catch { /* ignore */ }
  }

  return (
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center bg-black/80 backdrop-blur-md p-4"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl w-full max-w-4xl max-h-[88vh] flex flex-col shadow-2xl"
        onMouseDown={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-800 shrink-0">
          <div>
            <p className="text-sm font-semibold text-white">사람 직접 응답 상호작용 — 전체 목록</p>
            <p className="text-xs text-gray-500 mt-0.5">{items.length}건 · Auto-Approve와 무관하게 사람이 직접 응답한 대화</p>
          </div>
          <div className="flex items-center gap-2">
            <button onClick={handleCopy} disabled={items.length === 0}
              className="text-xs px-3 py-1.5 rounded-lg bg-gray-800 text-gray-200 border border-gray-700 hover:bg-gray-700 disabled:opacity-40 transition-colors">
              {copied ? '✓ 복사됨' : 'JSON 복사'}
            </button>
            <button onClick={onClose}
              className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg">✕</button>
          </div>
        </div>
        {items.length === 0 ? (
          <div className="flex-1 flex items-center justify-center text-gray-600 text-sm py-16">상호작용 데이터가 없습니다.</div>
        ) : (
          <div className="overflow-auto flex-1">
            <table className="w-full text-xs">
              <thead className="sticky top-0 bg-gray-950 border-b border-gray-800 z-10">
                <tr>
                  {['유형', '시각', 'Cline 질문/응답', '선택지', '사용자 답변', '작업 컨텍스트'].map(h => (
                    <th key={h} className="text-left px-3 py-2.5 text-gray-500 font-medium whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {items.map((r, i) => (
                  <tr key={i} className="border-b border-gray-800/50 align-top hover:bg-gray-800/30 transition-colors">
                    <td className="px-3 py-3 whitespace-nowrap">
                      <span className={`inline-block px-2 py-0.5 rounded-md text-[10px] font-medium border
                        ${r.interaction_type === 'ask_followup'
                          ? 'bg-violet-500/10 text-violet-300 border-violet-500/20'
                          : 'bg-sky-500/10 text-sky-300 border-sky-500/20'}`}>
                        {r.interaction_type === 'ask_followup' ? '질문' : '플랜 응답'}
                      </span>
                    </td>
                    <td className="px-3 py-3 text-gray-500 whitespace-nowrap">{r.ts_kst}</td>
                    <td className="px-3 py-3 text-gray-300 max-w-xs whitespace-pre-wrap leading-relaxed">{r.agent_message}</td>
                    <td className="px-3 py-3 min-w-[120px]">
                      {r.options.length > 0 ? (
                        <div className="flex flex-col gap-0.5">
                          {r.options.map((o, oi) => (
                            <span key={oi}
                              className={`text-[10px] px-1.5 py-0.5 rounded border ${r.user_answer === o
                                ? 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30 font-semibold'
                                : 'bg-gray-800/60 text-gray-500 border-gray-700'}`}>
                              {o}
                            </span>
                          ))}
                        </div>
                      ) : <span className="text-gray-700 italic text-[10px]">자유 입력</span>}
                    </td>
                    <td className="px-3 py-3 text-emerald-300 max-w-xs whitespace-pre-wrap leading-relaxed font-medium">{r.user_answer}</td>
                    <td className="px-3 py-3 text-gray-500 max-w-xs leading-relaxed whitespace-pre-wrap">
                      {r.task_context || <span className="text-gray-700 italic">—</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

// ── 수동 승인 필요 항목 세부 모달 ─────────────────────────────────────────────
function ManualApprovalDetailModal({ items, onClose }: { items: ManualApprovalItem[]; onClose: () => void }) {
  useBodyLock()
  const [copied, setCopied] = useState(false)
  const json = useMemo(() => JSON.stringify(items, null, 2), [items])

  async function handleCopy() {
    try { await navigator.clipboard.writeText(json); setCopied(true); setTimeout(() => setCopied(false), 2000) }
    catch { /* ignore */ }
  }

  // 도구별 그룹 집계
  const byTool = useMemo(() => {
    const m: Record<string, number> = {}
    items.forEach(r => { m[r.tool_name] = (m[r.tool_name] ?? 0) + 1 })
    return Object.entries(m).sort((a, b) => b[1] - a[1])
  }, [items])

  return (
    <div
      className="fixed inset-0 z-[60] flex items-center justify-center bg-black/80 backdrop-blur-md p-4"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl w-full max-w-5xl max-h-[88vh] flex flex-col shadow-2xl"
        onMouseDown={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-800 shrink-0">
          <div>
            <p className="text-sm font-semibold text-white">수동 승인 필요 항목 — 전체 목록</p>
            <p className="text-xs text-gray-500 mt-0.5">{items.length}건 · Auto-Approve 활성화 상태에서도 사람이 직접 승인해야 했던 작업</p>
          </div>
          <div className="flex items-center gap-2">
            <button type="button" onClick={handleCopy} disabled={items.length === 0}
              className="text-xs px-3 py-1.5 rounded-lg bg-gray-800 text-gray-200 border border-gray-700 hover:bg-gray-700 disabled:opacity-40 transition-colors">
              {copied ? '✓ 복사됨' : 'JSON 복사'}
            </button>
            <button onClick={onClose}
              className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg">✕</button>
          </div>
        </div>

        {items.length === 0 ? (
          <div className="flex-1 flex items-center justify-center text-gray-600 text-sm py-16">수동 승인이 필요한 항목이 없습니다.</div>
        ) : (
          <div className="overflow-auto flex-1">
            {/* 도구별 집계 바 */}
            <div className="px-6 py-3 border-b border-gray-800/60 flex flex-wrap gap-2">
              {byTool.map(([tool, cnt]) => (
                <span key={tool} className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-amber-500/10 border border-amber-500/20 text-xs">
                  <span className="text-amber-300 font-mono">{tool}</span>
                  <span className="text-amber-500 font-bold">{cnt}회</span>
                </span>
              ))}
            </div>
            <table className="w-full text-xs">
              <thead className="sticky top-0 bg-gray-950 border-b border-gray-800 z-10">
                <tr>
                  {['시각', '도구', '파일/명령', '작업 컨텍스트', '내용 미리보기'].map(h => (
                    <th key={h} className="text-left px-3 py-2.5 text-gray-500 font-medium whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {items.map((r, i) => (
                  <tr key={i} className="border-b border-gray-800/50 align-top hover:bg-gray-800/30 transition-colors">
                    <td className="px-3 py-3 text-gray-500 whitespace-nowrap">{r.ts_kst}</td>
                    <td className="px-3 py-3 whitespace-nowrap">
                      <span className="inline-block px-2 py-0.5 rounded-md bg-amber-500/10 text-amber-300 border border-amber-500/20 font-mono text-[11px]">
                        {r.tool_name}
                      </span>
                    </td>
                    <td className="px-3 py-3 font-mono text-[11px] text-gray-300 max-w-[200px]">
                      {r.command
                        ? <span className="text-sky-300 whitespace-pre-wrap">{r.command}</span>
                        : r.file_path
                          ? <span className="text-gray-400 truncate block" title={r.file_path}>{r.file_path.split('/').slice(-2).join('/')}</span>
                          : <span className="text-gray-700">—</span>
                      }
                    </td>
                    <td className="px-3 py-3 text-gray-400 max-w-xs leading-relaxed whitespace-pre-wrap">
                      {r.task_context || <span className="text-gray-700 italic">—</span>}
                    </td>
                    <td className="px-3 py-3 text-gray-500 max-w-xs leading-relaxed whitespace-pre-wrap">
                      {r.content_preview || <span className="text-gray-700 italic">—</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

// ── 수동 승인 + 상호작용 통합 모달 ──────────────────────────────────────────
function ManualApprovalModal({ items, interactions, onClose }: {
  items: ManualApprovalItem[]
  interactions: HumanInteractionItem[]
  onClose: () => void
}) {
  useBodyLock()
  const [tab, setTab] = useState<'approval' | 'interaction'>('approval')
  const [showApprovalDetail, setShowApprovalDetail] = useState(false)
  const [showInteractionDetail, setShowInteractionDetail] = useState(false)

  const byTool = useMemo(() => {
    const m: Record<string, number> = {}
    items.forEach(r => { m[r.tool_name] = (m[r.tool_name] ?? 0) + 1 })
    return Object.entries(m).sort((a, b) => b[1] - a[1])
  }, [items])

  const askCount = interactions.filter(r => r.interaction_type === 'ask_followup').length
  const planCount = interactions.filter(r => r.interaction_type === 'plan_mode').length

  return (
    <>
      <div
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md"
        onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
      >
        <div
          className="bg-gray-950 border border-gray-700 rounded-2xl p-6 max-w-lg w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
          onMouseDown={e => e.stopPropagation()}
        >
          {/* 헤더 */}
          <div className="flex items-start justify-between mb-4">
            <div>
              <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400">
                사람 개입 분석
              </span>
              <p className="text-xs text-gray-500 mt-1.5">Auto-Approve 설정과 무관하게 사람이 직접 개입한 순간</p>
            </div>
            <button onClick={onClose}
              className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg mt-1 shrink-0">✕</button>
          </div>

          {/* 탭 */}
          <div className="flex gap-1 mb-5 p-1 bg-gray-800/50 rounded-xl">
            <button
              onClick={() => setTab('approval')}
              className={`flex-1 text-xs font-medium py-2 rounded-lg transition-colors ${
                tab === 'approval' ? 'bg-amber-500/20 text-amber-300' : 'text-gray-500 hover:text-gray-300'
              }`}
            >
              수동 승인 요청 <span className="ml-1 opacity-70">{items.length}건</span>
            </button>
            <button
              onClick={() => setTab('interaction')}
              className={`flex-1 text-xs font-medium py-2 rounded-lg transition-colors ${
                tab === 'interaction' ? 'bg-violet-500/20 text-violet-300' : 'text-gray-500 hover:text-gray-300'
              }`}
            >
              질문·선택 응답 <span className="ml-1 opacity-70">{interactions.length}건</span>
            </button>
          </div>

          {/* 탭 1: 수동 승인 */}
          {tab === 'approval' && (
            <>
              <div className="mb-3">
                <p className="text-2xl font-bold text-white">{items.length}<span className="text-base text-gray-500 font-normal ml-1">건</span></p>
                <p className="text-xs text-gray-500 mt-0.5">requiresApproval=true — Auto-Approve 상태에서도 사람이 Allow/Deny를 눌러야 했던 도구 실행</p>
              </div>
              {byTool.length > 0 && (
                <div className="mb-4 space-y-2">
                  <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide">도구별 발생 횟수</p>
                  {byTool.map(([tool, cnt]) => (
                    <div key={tool} className="flex items-center gap-3">
                      <span className="text-xs font-mono text-gray-300 w-36 shrink-0 truncate">{tool}</span>
                      <div className="flex-1 h-1.5 bg-gray-800 rounded-full overflow-hidden">
                        <div className="h-full bg-amber-500/70 rounded-full" style={{ width: `${(cnt/items.length)*100}%` }} />
                      </div>
                      <span className="text-xs text-amber-400 font-mono w-8 text-right">{cnt}회</span>
                    </div>
                  ))}
                </div>
              )}
              {/* 실제 항목 미리보기 */}
              {items.slice(0, 3).length > 0 && (
                <div className="space-y-2 mb-4">
                  <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide">최근 항목 미리보기</p>
                  {items.slice(0, 3).map((r, i) => (
                    <div key={i} className="bg-gray-800/40 rounded-xl px-3 py-3 space-y-1.5">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-amber-500/15 text-amber-300 border border-amber-500/20 font-mono">
                          {r.tool_name}
                        </span>
                        <span className="text-[10px] text-gray-600">{r.ts_kst}</span>
                      </div>
                      {r.command && (
                        <p className="text-xs text-cyan-300/80 font-mono line-clamp-1 bg-gray-900/60 px-2 py-1 rounded">
                          $ {r.command}
                        </p>
                      )}
                      {r.file_path && (
                        <p className="text-xs text-blue-300/80 font-mono line-clamp-1">📄 {r.file_path}</p>
                      )}
                      {r.task_context && (
                        <p className="text-xs text-gray-500 line-clamp-1 border-l-2 border-gray-600 pl-2">{r.task_context}</p>
                      )}
                    </div>
                  ))}
                  {items.length > 3 && <p className="text-xs text-gray-600 text-center">…외 {items.length - 3}건</p>}
                </div>
              )}
              {items.length === 0 && <p className="text-center py-6 text-gray-600 text-sm">수동 승인 요청이 없었습니다.</p>}
              <button type="button" onClick={() => setShowApprovalDetail(true)} disabled={items.length === 0}
                className="w-full mt-2 rounded-xl bg-amber-900/30 border border-amber-500/30 text-amber-200 text-sm font-semibold py-2.5 hover:bg-amber-900/50 active:scale-[0.99] transition-all disabled:opacity-40 disabled:cursor-not-allowed">
                전체 세부 데이터 보기 →
              </button>
            </>
          )}

          {/* 탭 2: 질문·선택 응답 */}
          {tab === 'interaction' && (
            <>
              <div className="mb-4">
                <p className="text-2xl font-bold text-white">{interactions.length}<span className="text-base text-gray-500 font-normal ml-1">건</span></p>
                <p className="text-xs text-gray-500 mt-0.5">ask_followup_question · plan_mode_respond — Cline이 사람에게 질문하거나 선택지를 제시한 순간</p>
              </div>
              <div className="grid grid-cols-2 gap-2 mb-4">
                {[
                  { label: '질문·선택지 응답', value: askCount, color: 'text-violet-400', bg: 'bg-violet-500/5 border-violet-500/20' },
                  { label: '플랜 모드 응답',   value: planCount, color: 'text-sky-400',    bg: 'bg-sky-500/5 border-sky-500/20' },
                ].map(({ label, value, color, bg }) => (
                  <div key={label} className={`rounded-xl border p-3 text-center ${bg}`}>
                    <p className="text-[10px] text-gray-500 mb-1">{label}</p>
                    <p className={`text-xl font-bold font-mono ${color}`}>{value}</p>
                  </div>
                ))}
              </div>
              {/* 미리보기 */}
              <div className="space-y-2 mb-4">
                {interactions.slice(0, 3).map((r, i) => (
                  <div key={i} className="bg-gray-800/40 rounded-xl px-3 py-3 space-y-1.5">
                    <div className="flex items-center gap-2">
                      <span className={`inline-block px-1.5 py-0.5 rounded text-[10px] font-medium border ${
                        r.interaction_type === 'ask_followup'
                          ? 'bg-violet-500/10 text-violet-300 border-violet-500/20'
                          : 'bg-sky-500/10 text-sky-300 border-sky-500/20'
                      }`}>
                        {r.interaction_type === 'ask_followup' ? '질문' : '플랜'}
                      </span>
                      <span className="text-[10px] text-gray-600">{r.ts_kst}</span>
                    </div>
                    <p className="text-xs text-gray-300 line-clamp-2">{r.agent_message}</p>
                    {r.options.length > 0 && (
                      <div className="flex flex-wrap gap-1">
                        {r.options.map((o, oi) => (
                          <span key={oi} className={`text-[10px] px-1.5 py-0.5 rounded border ${
                            r.user_answer === o
                              ? 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30 font-semibold'
                              : 'bg-gray-800/60 text-gray-600 border-gray-700'
                          }`}>{o}</span>
                        ))}
                      </div>
                    )}
                    <p className="text-xs text-emerald-300 font-medium">↳ {r.user_answer}</p>
                  </div>
                ))}
                {interactions.length > 3 && <p className="text-xs text-gray-600 text-center">…외 {interactions.length - 3}건</p>}
              </div>
              {interactions.length === 0 && <p className="text-center py-6 text-gray-600 text-sm">상호작용 데이터가 없습니다.</p>}
              <button type="button" onClick={() => setShowInteractionDetail(true)} disabled={interactions.length === 0}
                className="w-full mt-2 rounded-xl bg-violet-900/30 border border-violet-500/30 text-violet-200 text-sm font-semibold py-2.5 hover:bg-violet-900/50 active:scale-[0.99] transition-all disabled:opacity-40 disabled:cursor-not-allowed">
                전체 세부 데이터 보기 →
              </button>
            </>
          )}
        </div>
      </div>

      {showApprovalDetail && <ManualApprovalDetailModal items={items} onClose={() => setShowApprovalDetail(false)} />}
      {showInteractionDetail && <HumanInteractionDetailModal items={interactions} onClose={() => setShowInteractionDetail(false)} />}
    </>
  )
}

// ── Auto-Approve 전용 모달 ────────────────────────────────────────────────────
const CAT_META: Record<string, { label: string; docSetting: string; color: string; border: string; badge: string }> = {
  파일읽기:  { label: '파일 읽기',    docSetting: 'Read project files',    color: 'text-emerald-300', border: 'border-emerald-500/20', badge: 'bg-emerald-500/10' },
  파일편집:  { label: '파일 편집',    docSetting: 'Edit project files',    color: 'text-blue-300',    border: 'border-blue-500/20',    badge: 'bg-blue-500/10' },
  명령실행:  { label: '명령 실행',    docSetting: 'Execute commands',      color: 'text-orange-300',  border: 'border-orange-500/20',  badge: 'bg-orange-500/10' },
  브라우저:  { label: '브라우저',     docSetting: 'Use the browser',       color: 'text-violet-300',  border: 'border-violet-500/20',  badge: 'bg-violet-500/10' },
  MCP서버:   { label: 'MCP 서버',     docSetting: 'Use MCP servers',       color: 'text-pink-300',    border: 'border-pink-500/20',    badge: 'bg-pink-500/10' },
  기타:      { label: '기타',         docSetting: '—',                     color: 'text-gray-400',    border: 'border-gray-700',       badge: 'bg-gray-700/40' },
}

function AutoApproveModal({ summary: s, onClose }: { summary: Summary; onClose: () => void }) {
  useBodyLock()
  const [showDetail, setShowDetail] = useState(false)
  const autoTotal  = s.auto_approved_count ?? 0
  const manualTotal = s.manual_approval_count ?? 0
  const safeTotal  = s.safe_tools_count ?? 0
  const allTotal   = autoTotal + manualTotal + safeTotal

  const byCategory = s.auto_approve_by_category ?? {}
  const inferred   = s.inferred_auto_approve ?? []
  const yolo       = s.yolo_mode_suspected ?? false
  const autoByTool = s.auto_approval_by_tool ?? {}
  const manualByTool = s.manual_approval_by_tool ?? {}

  const CAT_ORDER = ['파일읽기', '파일편집', '명령실행', '브라우저', 'MCP서버', '기타']
  const usedCats = CAT_ORDER.filter(c => byCategory[c] &&
    (byCategory[c].auto + byCategory[c].manual + byCategory[c].safe) > 0)

  return (
    <>
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl p-6 max-w-xl w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
        onMouseDown={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between mb-4">
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-orange-500/10 text-orange-400">
                Auto-Approve 분석
              </span>
              {yolo && (
                <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-red-500/20 text-red-300 border border-red-500/30 animate-pulse">
                  ⚠ YOLO Mode 의심
                </span>
              )}
            </div>
            <p className="text-xs text-gray-500 mt-2">총 {allTotal}회 도구 실행 · requiresApproval 플래그 기반 간접 추론</p>
          </div>
          <button onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg shrink-0 ml-2">✕</button>
        </div>

        {/* 전체 비율 바 */}
        {allTotal > 0 && (
          <>
            <div className="flex h-2.5 rounded-full overflow-hidden mb-2">
              <div className="bg-emerald-500/80" style={{ width: `${safeTotal/allTotal*100}%` }} title="항상 안전(파일읽기 등)" />
              <div className="bg-blue-500/80"    style={{ width: `${autoTotal/allTotal*100}%` }} title="Auto-Approve 설정으로 자동" />
              <div className="bg-amber-500/80"   style={{ width: `${manualTotal/allTotal*100}%` }} title="수동 승인 필요" />
            </div>
            <div className="flex gap-4 mb-5 text-xs text-gray-400">
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-emerald-400"/>항상 안전 {safeTotal}회</span>
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-blue-400"/>Auto 설정 {autoTotal}회</span>
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-amber-400"/>수동 {manualTotal}회</span>
            </div>
          </>
        )}

        {/* 추론된 Auto-Approve 설정 */}
        <div className="mb-4">
          <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide mb-2">데이터 기반 추론 — 활성화된 것으로 보이는 설정</p>
          {inferred.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {inferred.map(cat => {
                const m = CAT_META[cat] ?? CAT_META['기타']
                return (
                  <div key={cat} className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg border ${m.badge} ${m.border}`}>
                    <span className="w-1.5 h-1.5 rounded-full bg-current opacity-60" style={{ color: 'currentColor' }} />
                    <span className={`text-xs font-medium ${m.color}`}>{m.label}</span>
                    <span className="text-[10px] text-gray-500 font-mono">({m.docSetting})</span>
                    <span className="text-[10px] text-emerald-400 font-bold ml-1">ON ✓</span>
                  </div>
                )
              })}
            </div>
          ) : (
            <p className="text-xs text-gray-600 italic">추론 가능한 Auto-Approve 설정 없음 — 수동 승인이 있었거나 활동이 적습니다</p>
          )}
        </div>

        {/* YOLO 경고 */}
        {yolo && (
          <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-4 mb-4">
            <p className="text-sm font-semibold text-red-300 mb-1">⚠ YOLO Mode가 활성화됐을 가능성이 있습니다</p>
            <p className="text-xs text-gray-400">
              승인이 필요한 모든 도구(파일 편집·명령 실행 등)가 수동 승인 없이 자동으로 처리됐습니다.
              YOLO Mode는 Cline Settings → Features에서 확인할 수 있습니다.
            </p>
          </div>
        )}

        {/* 카테고리별 세부 현황 */}
        {usedCats.length > 0 && (
          <div className="mb-4">
            <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide mb-2">카테고리별 승인 현황 (Cline 문서 기준)</p>
            <div className="space-y-2">
              {usedCats.map(cat => {
                const d = byCategory[cat]
                const m = CAT_META[cat] ?? CAT_META['기타']
                const total = d.auto + d.manual + d.safe
                return (
                  <div key={cat} className={`rounded-xl border p-3 ${m.badge} ${m.border}`}>
                    <div className="flex items-center justify-between mb-1.5">
                      <span className={`text-xs font-semibold ${m.color}`}>{m.label}</span>
                      <span className="text-[10px] text-gray-500 font-mono">{m.docSetting}</span>
                    </div>
                    <div className="flex h-1.5 rounded-full overflow-hidden mb-1.5">
                      <div className="bg-emerald-500/70" style={{ width: `${(d.safe/total)*100}%` }} />
                      <div className="bg-blue-500/70"    style={{ width: `${(d.auto/total)*100}%` }} />
                      <div className="bg-amber-500/70"   style={{ width: `${(d.manual/total)*100}%` }} />
                    </div>
                    <div className="flex gap-3 text-[10px] text-gray-500">
                      {d.safe > 0   && <span><span className="text-emerald-400">{d.safe}</span>회 항상안전</span>}
                      {d.auto > 0   && <span><span className="text-blue-400">{d.auto}</span>회 Auto설정</span>}
                      {d.manual > 0 && <span><span className="text-amber-400">{d.manual}</span>회 수동승인</span>}
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        )}

        {/* 세부 데이터 보기 버튼 */}
        <button
          onClick={() => setShowDetail(true)}
          className="w-full text-sm text-blue-400 border border-blue-500/30 rounded-xl py-2.5 hover:bg-blue-500/10 transition-colors"
        >
          전체 세부 데이터 보기 →
        </button>
      </div>
    </div>
    {showDetail && (
      <GenericDetailModal
        title="Auto-Approve 도구별 승인 현황"
        badge="Auto-Approve"
        badgeClass="bg-blue-900/50 text-blue-300"
        columns={[
          { key: 'tool', label: '도구 이름' },
          { key: 'category', label: '카테고리' },
          { key: 'type', label: '승인 방식' },
          { key: 'count', label: '횟수', align: 'right', mono: true },
        ]}
        rows={[
          ...Object.entries(autoByTool).sort((a,b)=>b[1]-a[1]).map(([tool,cnt]) => ({
            tool, category: 'Auto 설정', type: '자동', count: cnt
          })),
          ...Object.entries(manualByTool).sort((a,b)=>b[1]-a[1]).map(([tool,cnt]) => ({
            tool, category: '수동 승인', type: '수동', count: cnt
          })),
          ...Object.entries(s.safe_tools_by_tool ?? {}).sort((a,b)=>b[1]-a[1]).map(([tool,cnt]) => ({
            tool, category: '항상 안전', type: '자동', count: cnt
          })),
        ]}
        onClose={() => setShowDetail(false)}
      />
    )}
    </>
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

// ── 토큰 추정 모달 ───────────────────────────────────────────────────────────
// ── AI 토큰 추정 사용량 모달 ─────────────────────────────────────────────────
function TokenUsageModal({ summary: s, onClose }: { summary: Summary; onClose: () => void }) {
  useBodyLock()
  const [showDetail, setShowDetail] = useState(false)
  const models = s.est_by_model ?? []
  const total = s.est_total_tokens ?? 0
  const totalIn  = models.reduce((a, m) => a + m.tokens_in,  0)
  const totalOut = models.reduce((a, m) => a + m.tokens_out, 0)

  const fmtTok = (n: number) =>
    n >= 1_000_000 ? `${(n / 1_000_000).toFixed(2)}M` : n >= 1_000 ? `${(n / 1_000).toFixed(1)}K` : String(n)

  return (
    <>
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl p-6 max-w-lg w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
        onMouseDown={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between mb-5">
          <div>
            <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400">
              AI 토큰 추정 사용량 <span className="text-cyan-600">(예시)</span>
            </span>
            <p className="text-3xl font-bold text-white mt-2">
              {fmtTok(total)}<span className="text-base text-gray-500 font-normal ml-1">토큰</span>
              <span className="text-sm text-cyan-600 font-normal ml-1">(추정)</span>
            </p>
            <p className="text-xs text-gray-500 mt-1">
              입력 {fmtTok(totalIn)} · 출력 {fmtTok(totalOut)} <span className="text-gray-700">(추정)</span>
            </p>
          </div>
          <button onClick={onClose} className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg shrink-0">✕</button>
        </div>

        {/* 추정 방법 설명 */}
        <div className="bg-cyan-500/5 border border-cyan-500/20 rounded-xl p-4 mb-4">
          <p className="text-xs text-gray-400 leading-relaxed">
            <span className="text-cyan-300 font-semibold">추정 방식:</span>{' '}
            이벤트 텍스트의 글자 수를 수집해 <span className="text-white">÷ 4 = 토큰 수</span>로 변환합니다.
            실제 토큰화 방식과 다를 수 있어 <span className="text-cyan-300">참고용 수치</span>입니다.
          </p>
        </div>

        {/* 모델별 사용량 */}
        {models.length > 0 ? (
          <div className="space-y-2 mb-4">
            <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide mb-2">모델별 토큰 사용량</p>
            {[...models]
              .sort((a, b) => (b.tokens_in + b.tokens_out) - (a.tokens_in + a.tokens_out))
              .map((m, i) => {
                const tot = m.tokens_in + m.tokens_out
                const pct = total > 0 ? Math.round(tot / total * 100) : 0
                return (
                  <div key={i} className="bg-gray-800/40 border border-gray-700/50 rounded-xl p-3">
                    <div className="flex items-center justify-between mb-1.5">
                      <span className="text-xs font-mono text-gray-200 truncate max-w-[200px]" title={m.model}>
                        {m.model}
                      </span>
                      <span className="text-xs font-bold text-cyan-300">{fmtTok(tot)}</span>
                    </div>
                    <div className="h-1.5 rounded-full bg-gray-700 mb-1.5 overflow-hidden">
                      <div className="h-full rounded-full bg-cyan-500/70" style={{ width: `${pct}%` }} />
                    </div>
                    <div className="flex gap-4 text-[10px] text-gray-500">
                      <span>in <span className="text-cyan-400">{fmtTok(m.tokens_in)}</span></span>
                      <span>out <span className="text-cyan-400">{fmtTok(m.tokens_out)}</span></span>
                      <span className="ml-auto text-gray-600">{pct}%</span>
                    </div>
                  </div>
                )
              })}
          </div>
        ) : (
          <div className="text-center py-6 text-gray-600 text-sm">추정 데이터가 없습니다.</div>
        )}

        {/* 세부 데이터 보기 */}
        {models.length > 0 && (
          <button
            onClick={() => setShowDetail(true)}
            className="w-full text-sm text-cyan-400 border border-cyan-500/30 rounded-xl py-2.5 hover:bg-cyan-500/10 transition-colors"
          >
            전체 세부 데이터 보기 →
          </button>
        )}
      </div>
    </div>
    {showDetail && (
      <GenericDetailModal
        title="AI 토큰 추정 사용량 — 모델별 상세"
        badge="AI 토큰 추정 사용량"
        badgeClass="bg-cyan-900/50 text-cyan-300"
        columns={[
          { key: 'rank', label: '#', align: 'right' },
          { key: 'model', label: '모델' },
          { key: 'tokens_in', label: '입력 토큰', align: 'right', mono: true },
          { key: 'tokens_out', label: '출력 토큰', align: 'right', mono: true },
          { key: 'total', label: '합계', align: 'right', mono: true },
          { key: 'pct', label: '비율', align: 'right' },
        ]}
        rows={[...models]
          .sort((a, b) => (b.tokens_in + b.tokens_out) - (a.tokens_in + a.tokens_out))
          .map((m, i) => {
            const tot = m.tokens_in + m.tokens_out
            return {
              rank: i + 1,
              model: m.model,
              tokens_in: fmtTok(m.tokens_in),
              tokens_out: fmtTok(m.tokens_out),
              total: fmtTok(tot),
              pct: `${total > 0 ? Math.round(tot / total * 100) : 0}%`,
            }
          })}
        onClose={() => setShowDetail(false)}
      />
    )}
    </>
  )
}

// ── AI 토큰 추정 비용 모달 ─────────────────────────────────────────────────────
function TokenEstModal({ summary: s, onClose }: { summary: Summary; onClose: () => void }) {
  useBodyLock()
  const [showDetail, setShowDetail] = useState(false)
  const models = s.est_by_model ?? []
  const total = s.est_total_tokens ?? 0
  const cost  = s.est_total_cost_usd ?? 0

  const fmtTokens = (n: number) =>
    n >= 1_000_000 ? `${(n / 1_000_000).toFixed(2)}M` : n >= 1_000 ? `${(n / 1_000).toFixed(1)}K` : String(n)

  return (
    <>
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl p-6 max-w-xl w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
        onMouseDown={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-start justify-between mb-4">
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-yellow-500/10 text-yellow-400">
                AI 토큰 추정 비용
              </span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-orange-500/10 text-orange-300 border border-orange-500/20">
                ⚠ 간접 추정값 — 실제 청구 요금과 다를 수 있습니다
              </span>
            </div>
            <p className="text-3xl font-bold text-white mt-2">
              ${cost.toFixed(3)}
              <span className="text-sm text-yellow-500 font-normal ml-1">(추정)</span>
            </p>
            <p className="text-xs text-gray-500 mt-1">
              {fmtTokens(total)} 토큰 <span className="text-gray-700">(추정)</span> · 텍스트 볼륨(chars ÷ 4) 기반
            </p>
          </div>
          <button onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg mt-1 shrink-0">✕</button>
        </div>

        {/* 추정 방법 설명 */}
        <div className="bg-blue-500/5 border border-blue-500/20 rounded-xl p-3 mb-4 text-xs text-blue-300/80 space-y-1">
          <p className="font-semibold text-blue-300">추정 방법</p>
          <p>1. 각 이벤트의 텍스트(프롬프트, 도구 파라미터, 결과)를 수집합니다.</p>
          <p>2. 문자 수 ÷ 4 = 추정 토큰 수 (평균 4자/토큰 가정)</p>
          <p>3. 이벤트가 속한 Task의 모델에 아래 가격표(예시)를 적용합니다.</p>
          <p className="text-orange-300/70">※ 실제 컨텍스트 윈도우 누적, 캐시 히트율, 내부 처리 토큰은 반영되지 않습니다.</p>
        </div>

        {/* 모델별 분석 */}
        {models.length > 0 ? (
          <div className="space-y-3 mb-4">
            <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide">모델별 추정 사용량</p>
            {models.map((m, i) => {
              const pct = total > 0 ? ((m.tokens_in + m.tokens_out) / total) * 100 : 0
              return (
                <div key={i} className="bg-gray-800/40 rounded-xl p-3">
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <p className="text-xs font-semibold text-white font-mono">{m.model}</p>
                      <p className="text-[10px] text-gray-600 mt-0.5">
                        매칭 가격표: <span className="text-gray-500">{m.price_key}</span>
                      </p>
                    </div>
                    <span className="text-sm font-bold text-yellow-300">${m.cost_usd.toFixed(3)}</span>
                  </div>
                  <div className="h-1.5 bg-gray-700 rounded-full overflow-hidden mb-2">
                    <div className="h-full bg-yellow-500/60 rounded-full" style={{ width: `${pct}%` }} />
                  </div>
                  <div className="grid grid-cols-3 gap-2 text-[10px]">
                    <div>
                      <p className="text-gray-600">입력 토큰</p>
                      <p className="text-blue-300 font-mono">{fmtTokens(m.tokens_in)}</p>
                      <p className="text-gray-700">${m.price_input}/MTok <span className="text-orange-400/60">(예시)</span></p>
                    </div>
                    <div>
                      <p className="text-gray-600">출력 토큰</p>
                      <p className="text-emerald-300 font-mono">{fmtTokens(m.tokens_out)}</p>
                      <p className="text-gray-700">${m.price_output}/MTok <span className="text-orange-400/60">(예시)</span></p>
                    </div>
                    <div>
                      <p className="text-gray-600">합계</p>
                      <p className="text-yellow-300 font-mono">{fmtTokens(m.tokens_in + m.tokens_out)}</p>
                      <p className="text-gray-700">{pct.toFixed(1)}% 비중</p>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        ) : (
          <div className="text-center py-6 text-gray-600 text-sm">추정할 텍스트 데이터가 없습니다.</div>
        )}

        {/* 가격표 참고 */}
        <div className="border border-gray-800 rounded-xl p-3">
          <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide mb-2">
            적용된 가격표 <span className="text-orange-400 font-normal normal-case">(예시 — 확정 아님)</span>
          </p>
          <div className="space-y-1">
            {[
              { label: 'Claude Sonnet 4.x',  input: 3.0,   output: 15.0 },
              { label: 'Claude Haiku 3.5',   input: 0.80,  output: 4.0  },
              { label: 'Claude Opus 4.x',    input: 15.0,  output: 75.0 },
              { label: 'GPT-4o',             input: 2.5,   output: 10.0 },
              { label: 'GPT-4o-mini',        input: 0.15,  output: 0.60 },
              { label: 'Gemini 2.0 Flash',   input: 0.10,  output: 0.40 },
            ].map(row => (
              <div key={row.label} className="flex items-center justify-between text-[10px]">
                <span className="text-gray-400">{row.label}</span>
                <span className="text-gray-600 font-mono">
                  in ${row.input} / out ${row.output} per MTok
                </span>
              </div>
            ))}
          </div>
          <p className="text-[10px] text-gray-700 mt-2">출처: Anthropic / OpenAI / Google 공개 가격표 (2025년 기준 예시)</p>
        </div>

        {/* 세부 데이터 보기 버튼 */}
        {models.length > 0 && (
          <button
            onClick={() => setShowDetail(true)}
            className="w-full mt-3 text-sm text-orange-400 border border-orange-500/30 rounded-xl py-2.5 hover:bg-orange-500/10 transition-colors shrink-0"
          >
            전체 세부 데이터 보기 →
          </button>
        )}
      </div>
    </div>
    {showDetail && (
      <GenericDetailModal
        title="모델별 토큰 추정 상세"
        badge="AI 토큰 추정"
        badgeClass="bg-orange-900/50 text-orange-300"
        columns={[
          { key: 'rank', label: '#', align: 'right' },
          { key: 'model', label: '모델' },
          { key: 'price_key', label: '가격 키' },
          { key: 'tokens_in', label: '입력 토큰', align: 'right', mono: true },
          { key: 'tokens_out', label: '출력 토큰', align: 'right', mono: true },
          { key: 'cost_usd', label: '추정 비용(USD)', align: 'right', mono: true },
          { key: 'pct', label: '비율', align: 'right' },
        ]}
        rows={[...models]
          .sort((a, b) => b.cost_usd - a.cost_usd)
          .map((m, i) => ({
            rank: i + 1,
            model: m.model,
            price_key: m.price_key ?? '—',
            tokens_in: m.tokens_in.toLocaleString(),
            tokens_out: m.tokens_out.toLocaleString(),
            cost_usd: `$${m.cost_usd.toFixed(4)}`,
            pct: total > 0 ? `${Math.round((m.tokens_in + m.tokens_out) / total * 100)}%` : '—',
          }))}
        onClose={() => setShowDetail(false)}
      />
    )}
    </>
  )
}

// ── Auto-Approve 자동 처리 업무 단위 모달 ────────────────────────────────────
function AutoWorkModal({ summary: s, onClose }: { summary: Summary; onClose: () => void }) {
  useBodyLock()
  const [showDetail, setShowDetail] = useState(false)
  const byCategory = s.auto_approve_by_category ?? {}
  const autoByTool = s.auto_approval_by_tool ?? {}
  const inferred   = s.inferred_auto_approve ?? []
  const yolo       = s.yolo_mode_suspected ?? false
  const autoTotal  = s.auto_approved_count ?? 0

  const CAT_ORDER = ['파일편집', '명령실행', '브라우저', 'MCP서버', '기타']
  const autoCats = CAT_ORDER
    .filter(c => (byCategory[c]?.auto ?? 0) > 0)
    .map(c => ({ cat: c, count: byCategory[c].auto, meta: CAT_META[c] ?? CAT_META['기타'] }))

  const sortedTools = Object.entries(autoByTool).sort((a, b) => b[1] - a[1])

  const toolToCat: Record<string, string> = {
    write_to_file: '파일편집', replace_in_file: '파일편집', new_file: '파일편집', apply_diff: '파일편집', create_file: '파일편집',
    execute_command: '명령실행', run_command: '명령실행',
    browser_action: '브라우저', web_fetch: '브라우저', web_search: '브라우저', fetch: '브라우저',
  }

  return (
    <>
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md"
      onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-gray-950 border border-gray-700 rounded-2xl p-6 max-w-lg w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
        onMouseDown={e => e.stopPropagation()}
      >
        <div className="flex items-start justify-between mb-4">
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400">
                Auto-Approve 자동 처리 업무 단위
              </span>
              {yolo && (
                <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-red-500/20 text-red-300 border border-red-500/30 animate-pulse">
                  ⚠ YOLO 의심
                </span>
              )}
            </div>
            <p className="text-3xl font-bold text-white mt-2">
              {autoTotal.toLocaleString()}<span className="text-base text-gray-500 font-normal ml-1">회</span>
            </p>
            <p className="text-xs text-gray-500 mt-1">승인이 필요했지만 Auto-Approve 설정으로 자동 실행된 도구 호출</p>
          </div>
          <button onClick={onClose}
            className="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-white hover:bg-gray-800 transition-colors text-lg mt-1 shrink-0">✕</button>
        </div>

        {inferred.length > 0 && (
          <div className="mb-4 p-3 bg-blue-500/5 border border-blue-500/20 rounded-xl">
            <p className="text-xs text-gray-500 mb-2">활성화된 것으로 추정되는 Auto-Approve 설정</p>
            <div className="flex flex-wrap gap-1.5">
              {inferred.map(cat => {
                const m = CAT_META[cat] ?? CAT_META['기타']
                return (
                  <span key={cat} className={`text-xs px-2 py-0.5 rounded-lg border font-medium ${m.badge} ${m.border} ${m.color}`}>
                    {m.label} ✓
                  </span>
                )
              })}
            </div>
          </div>
        )}

        {autoCats.length > 0 ? (
          <div className="mb-5">
            <p className="text-xs text-gray-500 font-semibold uppercase tracking-wide mb-3">카테고리별 자동 처리 횟수</p>
            <div className="space-y-4">
              {autoCats.map(({ cat, count, meta }) => (
                <div key={cat}>
                  <div className="flex items-center justify-between mb-1">
                    <div className="flex items-center gap-2">
                      <span className={`text-sm font-semibold ${meta.color}`}>{meta.label}</span>
                      <span className="text-[10px] text-gray-600 font-mono">({meta.docSetting})</span>
                    </div>
                    <span className={`text-sm font-mono font-bold ${meta.color}`}>{count}회</span>
                  </div>
                  <div className="h-2 bg-gray-800 rounded-full overflow-hidden mb-1.5">
                    <div
                      className="h-full rounded-full bg-blue-500/60"
                      style={{ width: `${autoTotal > 0 ? (count / autoTotal) * 100 : 0}%` }}
                    />
                  </div>
                  <div className="flex flex-wrap gap-1">
                    {sortedTools
                      .filter(([tool]) => (toolToCat[tool] ?? 'MCP서버') === cat)
                      .map(([tool, cnt]) => (
                        <span key={tool} className="text-[10px] px-1.5 py-0.5 rounded bg-gray-800/60 text-gray-400 font-mono border border-gray-700/50">
                          {tool} <span className="text-blue-400 font-bold">{cnt}</span>
                        </span>
                      ))
                    }
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="text-center py-8 text-gray-600 text-sm">
            Auto-Approve로 자동 처리된 작업이 없습니다.
          </div>
        )}

        {yolo && (
          <div className="bg-red-500/8 border border-red-500/25 rounded-xl p-4">
            <p className="text-sm font-semibold text-red-300 mb-1">⚠ YOLO Mode 가능성</p>
            <p className="text-xs text-gray-400">
              승인이 필요한 모든 작업이 수동 승인 없이 자동 처리됐습니다.
              Cline Settings → Features에서 확인하세요.
            </p>
          </div>
        )}

        {/* 세부 데이터 보기 버튼 */}
        {sortedTools.length > 0 && (
          <button
            onClick={() => setShowDetail(true)}
            className="w-full mt-3 text-sm text-emerald-400 border border-emerald-500/30 rounded-xl py-2.5 hover:bg-emerald-500/10 transition-colors"
          >
            전체 세부 데이터 보기 →
          </button>
        )}
      </div>
    </div>
    {showDetail && (
      <GenericDetailModal
        title="Auto-Approve 자동 처리 도구 목록"
        badge="자동 처리 단위"
        badgeClass="bg-emerald-900/50 text-emerald-300"
        columns={[
          { key: 'rank', label: '#', align: 'right' },
          { key: 'tool', label: '도구 이름' },
          { key: 'category', label: '카테고리' },
          { key: 'count', label: '자동 처리 횟수', align: 'right', mono: true },
        ]}
        rows={sortedTools.map(([tool, cnt], i) => ({
          rank: i + 1,
          tool,
          category: toolToCat[tool] ?? '기타',
          count: cnt,
        }))}
        onClose={() => setShowDetail(false)}
      />
    )}
    </>
  )
}

// ── 메인 컴포넌트 ─────────────────────────────────────────────────────────────
export default function SummaryCards({ summary: s, cancelFollowups = [], manualApprovalItems = [], humanInteractionItems = [], tasks = [], eventTypeCounts = [], projectFirstLast, projectFirstLastLoading = false }: Props) {
  const [llmOpen, setLlmOpen] = useState(false)
  const [cancelModalOpen, setCancelModalOpen] = useState(false)
  const [autonomyOpen, setAutonomyOpen] = useState(false)

  const autonomyPct = s.autonomy_pct ?? 0
  const autonomyGood = autonomyPct >= 85
  const autonomyMid  = autonomyPct >= 65 && autonomyPct < 85

  const totalModelEvents = Object.values(s.model_usage || {}).reduce((a, b) => a + b, 0)
  const sortedModels = Object.entries(s.model_usage || {}).sort((a, b) => b[1] - a[1])
  const totalIn = s.total_tokens_in ?? 0
  const totalOut = s.total_tokens_out ?? 0
  const totalCacheIn = s.total_tokens_in_cache ?? 0
  const totalCacheOut = s.total_tokens_out_cache ?? 0
  const hasTokenData = (totalIn + totalOut) > 0
  const compactCount = s.compact_count ?? 0

  const efficiencyGood = (s.efficiency_score ?? 0) >= 1.5
  const [autoApproveOpen, setAutoApproveOpen] = useState(false)
  const [manualApprovalOpen, setManualApprovalOpen] = useState(false)
  const [autoWorkOpen, setAutoWorkOpen] = useState(false)
  const [tokenEstOpen, setTokenEstOpen] = useState(false)
  const [tokenUsageOpen, setTokenUsageOpen] = useState(false)
  const autoTotal = s.auto_approved_count ?? 0
  const manualTotal = s.manual_approval_count ?? 0

  // ── 컬러 헬퍼 ──
  const NEG = { border: 'border-blue-500/60',  badge: 'bg-blue-500/15 text-blue-300'  } // (-) 파란색
  const POS = { border: 'border-red-500/60',   badge: 'bg-red-500/15  text-red-300'   } // (+) 빨간색

  // ── 공통 파일 맵 ──
  const wMap = Object.fromEntries((s.top_written_files ?? []).map(f => [f.file, f.count]))
  const rMap = Object.fromEntries((s.top_read_files ?? []).map(f => [f.file, f.count]))
  const allFiles = Array.from(new Set([...Object.keys(wMap), ...Object.keys(rMap)]))
    .map(file => ({ file, writes: wMap[file] ?? 0, reads: rMap[file] ?? 0 }))
    .sort((a, b) => (b.writes + b.reads) - (a.writes + a.reads))

  // 지표 카드 정의
  const metrics: MetricDef[] = [
    {
      label: 'Hook 이벤트',
      value: s.total_hook_events.toLocaleString(),
      sub: 'cline 훅 (Task/Tool/Prompt)',
      color: 'border-gray-700',
      badge: 'bg-gray-700/50 text-gray-400',
      formula: 'Cline이 작업하는 동안 발생한 모든 활동 이벤트 수\n(Task시작·완료·취소 / 도구 사용 전·후 / 프롬프트 제출 등)',
      computation: `수집된 훅 이벤트 합계 = ${s.total_hook_events.toLocaleString()}개\n\n이벤트 유형 분포 (상위):\n${
        Object.entries(s.event_type_counts ?? {})
          .sort((a, b) => b[1] - a[1])
          .slice(0, 5)
          .map(([k, v]) => `  ${k.padEnd(20)} ${v.toLocaleString()}회`)
          .join('\n')
      }`,
      interpretation: `총 ${s.total_hook_events.toLocaleString()}개의 활동이 기록됐습니다.`,
      description: 'AI agent가 작업하면서 기록된 모든 행동의 총 횟수입니다. 숫자가 클수록 agent가 더 많은 활동을 했다는 의미입니다.',
      example: '높을수록 → agent가 더 많은 작업을 수행했습니다.\n낮을수록 → 작업 자체가 적었습니다.',
      detail: {
        title: '이벤트 타입별 발생 횟수',
        columns: [
          { key: 'rank', label: '#', align: 'right' },
          { key: 'name', label: '이벤트 타입', mono: true },
          { key: 'count', label: '횟수', align: 'right', mono: true },
          { key: 'pct', label: '비율', align: 'right' },
        ],
        rows: eventTypeCounts
          .sort((a, b) => b.count - a.count)
          .map((c, i) => ({
            rank: i + 1,
            name: c.name,
            count: c.count.toLocaleString(),
            pct: `${Math.round(c.count / s.total_hook_events * 100)}%`,
          })),
      },
    },
    {
      label: 'GitCommit',
      value: s.total_git_events.toLocaleString(),
      sub: 'git 커밋 추적 이벤트',
      color: 'border-gray-700',
      badge: 'bg-gray-700/50 text-gray-400',
      formula: '프로젝트에 기록된 git 커밋의 총 개수',
      computation: `기록된 커밋 수 = ${s.total_git_events.toLocaleString()}개`,
      interpretation: `${s.total_git_events}개의 커밋이 코드베이스에 저장됐습니다.`,
      description: '지금까지 코드베이스에 저장된 커밋(변경 이력)의 수입니다. agent가 만든 것과 사람이 만든 것 모두 포함됩니다.',
      example: '높을수록 → 코드 변경이 자주 저장됐습니다 (작은 단위 커밋 = 좋은 습관)',
      detail: {
        title: '이벤트 타입별 발생 횟수 (GitCommit 포함)',
        columns: [
          { key: 'name', label: '이벤트', mono: true },
          { key: 'count', label: '횟수', align: 'right', mono: true },
        ],
        rows: eventTypeCounts
          .filter(c => c.name === 'GitCommit')
          .concat(eventTypeCounts.filter(c => c.name !== 'GitCommit').sort((a, b) => b.count - a.count))
          .map(c => ({ name: c.name, count: c.count.toLocaleString() })),
        emptyText: 'GitCommit 이벤트가 없습니다.',
      },
    },
    {
      label: '총 Task',
      value: s.total_tasks.toLocaleString(),
      sub: `재개 ${s.total_resumes}회 포함`,
      color: 'border-gray-700',
      badge: 'bg-gray-700/50 text-gray-400',
      formula: 'Cline 사이드바에서 새 대화창(채팅)을 시작한 횟수',
      computation: `TaskStart 이벤트  = ${s.total_tasks}회\nTaskResume 이벤트 = ${s.total_resumes}회\n\n완료 종료 = ${(s.total_tasks - (s.tasks_ended_canceled ?? 0)).toLocaleString()}건\n취소 종료 = ${(s.tasks_ended_canceled ?? 0).toLocaleString()}건`,
      interpretation: `총 ${s.total_tasks}개 대화 중 ${s.tasks_ended_canceled ?? 0}개가 취소로 끝났습니다.`,
      description: 'Cline 대화 창의 총 수입니다. 새 대화를 열 때마다 1개씩 카운트됩니다.',
      example: '높을수록 → Cline을 자주, 다양한 작업에 활용했습니다.',
      detail: {
        title: '전체 Task 목록',
        columns: [
          { key: 'start_kst', label: '시작시각', mono: true },
          { key: 'status', label: '상태' },
          { key: 'duration', label: '소요시간', align: 'right', mono: true },
          { key: 'writes', label: 'Write', align: 'right', mono: true },
          { key: 'reads', label: 'Read', align: 'right', mono: true },
          { key: 'task', label: '초기 요청' },
        ],
        rows: tasks.map(t => ({
          start_kst: t.start_kst,
          status: t.status,
          duration: t.duration_sec != null ? `${t.duration_sec}s` : '—',
          writes: t.write_count,
          reads: t.read_count,
          task: (t.initial_task || t.first_prompt || '').slice(0, 60),
        })),
      },
    },
    {
      label: 'Task 결함율',
      value: (s.total_task_cancel_events ?? 0).toLocaleString(),
      sub: '동일 Task에서 여러 번 취소 가능',
      color: NEG.border,
      badge: NEG.badge,
      formula: '로그에 기록된 TaskCancel 이벤트 건수 (원시 카운트)',
      computation: `TaskCancel 이벤트 수 = ${s.total_task_cancel_events ?? 0}회\n취소 이후 재프롬프트 쌍 = ${s.post_cancel_prompt_pairs ?? 0}건\n\n취소 후 바로 다시 입력한 비율:\n= ${s.post_cancel_prompt_pairs ?? 0} ÷ ${s.total_task_cancel_events ?? 0} × 100\n= ${s.total_task_cancel_events ? Math.round((s.post_cancel_prompt_pairs ?? 0) / s.total_task_cancel_events * 100) : 0}%`,
      interpretation: `${s.total_task_cancel_events ?? 0}번 취소, 그 중 ${s.post_cancel_prompt_pairs ?? 0}번은 취소 직후 재프롬프트했습니다.`,
      description: '작업 도중 사용자가 중단(Cancel)을 눌렀거나 agent가 취소 상태로 종료된 횟수입니다. 업무 품질의 보조 지표로 활용됩니다.',
      example: '높을수록 → 시도가 자주 끊겼습니다.\n낮을수록 → 한 번에 이어서 진행한 비율이 큽니다.',
      detail: {
        title: '취소 이벤트가 발생한 Task',
        columns: [
          { key: 'start_kst', label: '시작시각', mono: true },
          { key: 'cancel_count', label: '취소횟수', align: 'right', mono: true, highlight: true },
          { key: 'status', label: '최종상태' },
          { key: 'task', label: '초기 요청' },
        ],
        rows: tasks
          .filter(t => t.cancel_count > 0)
          .map(t => ({
            start_kst: t.start_kst,
            cancel_count: t.cancel_count,
            status: t.status,
            task: (t.initial_task || t.first_prompt || '').slice(0, 70),
          })),
        emptyText: '취소 이벤트가 없습니다.',
      },
    },
    {
      label: 'AI재업무율',
      value: `${s.file_rework_rate}%`,
      sub: `${s.file_rework_count}개 파일 중복 write`,
      color: NEG.border,
      badge: NEG.badge,
      formula: '같은 파일을 2번 이상 수정한 파일의 비율 (AI가 수정한 것만 집계)\n= (중복 수정 파일 수 ÷ 수정한 전체 파일 수) × 100',
      computation: `AI가 2회 이상 수정한 파일  = ${s.file_rework_count}개\nAI가 수정한 전체 파일    = ${s.unique_written_files ?? '?'}개\n\n${s.file_rework_count} ÷ ${s.unique_written_files ?? '?'} × 100 = ${s.file_rework_rate}%\n\n※ 수작업 수정은 집계에 포함되지 않습니다.`,
      interpretation: `AI가 수정한 파일 ${s.unique_written_files ?? '?'}개 중 ${s.file_rework_count}개를 두 번 이상 고쳤습니다.`,
      description: 'AI(agent)가 한 파일을 여러 번 고쳤다면 처음에 제대로 못 만든 것입니다. 수작업 수정은 포함되지 않으며, AI가 직접 쓴 횟수만 반영됩니다.',
      example: '낮을수록 좋습니다 → 파일을 한 번에 완성했습니다.\n30% 초과 → 수정한 파일 중 30% 이상을 두 번 이상 고쳤습니다.',
      detail: {
        title: 'AI가 2회 이상 수정한 파일 (재업무 파일)',
        columns: [
          { key: 'rank', label: '#', align: 'right' },
          { key: 'file', label: '파일 경로', mono: true },
          { key: 'write_count', label: '수정횟수', align: 'right', mono: true, highlight: true },
        ],
        rows: (s.rework_files ?? []).map((r, i) => ({
          rank: i + 1,
          file: r.file,
          write_count: r.write_count,
        })),
        emptyText: '재업무 파일이 없습니다 — 모든 파일을 한 번에 완성했습니다.',
      },
    },
    {
      label: 'AI수정빈도',
      value: s.total_writes.toLocaleString(),
      sub: 'write_to_file · replace_in_file',
      color: NEG.border,
      badge: NEG.badge,
      formula: 'AI가 파일을 새로 쓰거나 수정한 총 횟수',
      computation: `파일 쓰기 호출 = ${s.total_writes}회`,
      interpretation: `AI가 파일을 총 ${s.total_writes}번 수정했습니다.`,
      description: 'AI(agent)가 파일을 생성하거나 내용을 변경한 총 횟수입니다.',
      example: '높을수록 → AI가 많은 코드를 작성·수정했습니다.',
      detail: {
        title: 'AI수정빈도 — 파일별 Write 횟수',
        columns: [
          { key: 'rank', label: '#', align: 'right' },
          { key: 'file', label: '파일 경로', mono: true },
          { key: 'writes', label: 'Write 횟수', align: 'right', mono: true, highlight: true },
        ],
        rows: (s.top_written_files ?? []).map((f, i) => ({ rank: i + 1, file: f.file, writes: f.count })),
        emptyText: '파일 쓰기 기록이 없습니다.',
      },
    },
    {
      label: 'AI투입정도',
      value: s.total_reads.toLocaleString(),
      sub: 'read_file · search_files',
      color: NEG.border,
      badge: NEG.badge,
      formula: 'AI가 파일을 읽거나 검색한 총 횟수',
      computation: `파일 읽기 호출 = ${s.total_reads}회\n파일 1개 쓸 때 평균 읽기 = ${s.read_write_ratio}x`,
      interpretation: `AI가 파일을 총 ${s.total_reads}번 읽었습니다. 쓰기 대비 ${s.read_write_ratio}배 읽기가 발생했습니다.`,
      description: 'AI가 파일을 읽어보거나 검색한 총 횟수입니다.',
      example: '읽기가 쓰기의 5배 이상이면 AI가 탐색에 많은 비중을 쏟고 있습니다.',
      detail: {
        title: 'AI투입정도 — 파일별 Read 횟수',
        columns: [
          { key: 'rank', label: '#', align: 'right' },
          { key: 'file', label: '파일 경로', mono: true },
          { key: 'reads', label: 'Read 횟수', align: 'right', mono: true, highlight: true },
          { key: 'writes', label: 'Write', align: 'right', mono: true },
        ],
        rows: allFiles.slice(0, 30).map((f, i) => ({ rank: i + 1, file: f.file, reads: f.reads, writes: f.writes })),
        emptyText: '파일 읽기 기록이 없습니다.',
      },
    },
    {
      label: 'AI작업 효율성',
      value: `${s.efficiency_score ?? 0}`,
      sub: efficiencyGood ? 'AI 코드 생성 효율 양호' : '개선 여지 있음',
      color: POS.border,
      badge: POS.badge,
      formula: '= (1 - AI재업무율) × R/W비율\n(R/W = 읽기 ÷ 쓰기)',
      computation: `(1 - AI재업무율) × R/W\n= (1 - ${(s.file_rework_rate / 100).toFixed(2)}) × ${s.read_write_ratio}\n= ${(1 - s.file_rework_rate / 100).toFixed(2)} × ${s.read_write_ratio}\n= ${s.efficiency_score ?? 0}`,
      interpretation: `재업무율 ${s.file_rework_rate}%, R/W ${s.read_write_ratio}x 기준 효율 점수: ${s.efficiency_score ?? 0}`,
      description: 'AI재업무율과 R/W 비율을 종합한 AI 작업 효율 점수입니다. 재작업이 없고 읽기 대비 쓰기가 많을수록 높습니다.',
      example: '2.0 이상 → 매우 효율적. AI가 시행착오 없이 바로 결과물 생성\n1.0 미만 → 반복 수정이 많거나 파일 탐색이 적음',
      detail: {
        title: 'Task별 AI작업 효율성 분해',
        columns: [
          { key: 'start_kst', label: '시작시각', mono: true },
          { key: 'status', label: '상태' },
          { key: 'writes', label: 'Write', align: 'right', mono: true },
          { key: 'reads', label: 'Read', align: 'right', mono: true },
          { key: 'rw', label: 'R/W', align: 'right', mono: true },
          { key: 'task', label: '초기 요청' },
        ],
        rows: tasks
          .filter(t => t.write_count > 0 || t.read_count > 0)
          .sort((a, b) => {
            const ratioA = a.write_count > 0 ? a.read_count / a.write_count : 9999
            const ratioB = b.write_count > 0 ? b.read_count / b.write_count : 9999
            return ratioB - ratioA
          })
          .map(t => ({
            start_kst: t.start_kst,
            status: t.status,
            writes: t.write_count,
            reads: t.read_count,
            rw: t.write_count > 0 ? `${(t.read_count / t.write_count).toFixed(1)}x` : '—',
            task: (t.initial_task || t.first_prompt || '').slice(0, 50),
          })),
        emptyText: '파일 작업 데이터가 없습니다.',
      },
    },
  ]

  return (
    <div className="space-y-4">

      {/* ── 기본 정보 ── */}
      <SectionRow label="기본 정보" color="info">
        <div className="grid grid-cols-3 gap-3">
          {metrics.filter(m => ['Hook 이벤트', 'GitCommit', '총 Task'].includes(m.label))
            .map(m => <MetricCard key={m.label} metric={m} />)}
        </div>
      </SectionRow>

      {/* ── PCL ── */}
      <SectionRow label="PCL" sub="Performance Constancy Level" color="blue">
        <div className="grid grid-cols-3 gap-3">
          {metrics.filter(m => ['AI수정빈도', 'AI재업무율', 'AI투입정도'].includes(m.label))
            .sort((a, b) => ['AI수정빈도', 'AI재업무율', 'AI투입정도'].indexOf(a.label) - ['AI수정빈도', 'AI재업무율', 'AI투입정도'].indexOf(b.label))
            .map(m => <MetricCard key={m.label} metric={m} />)}
        </div>
      </SectionRow>

      {/* ── PNL ── */}
      <SectionRow label="PNL" sub="In-project Nativeness Level" color="purple">
        <div className="grid grid-cols-4 gap-3">
          {/* Task 결함율 (-) */}
          {metrics.filter(m => m.label === 'Task 결함율').map(m => <MetricCard key={m.label} metric={m} />)}
          {/* HITL 승인수 (-) */}
          <button
            onClick={() => setManualApprovalOpen(true)}
            className="bg-gray-900 rounded-xl border border-blue-500/60 p-4 flex flex-col gap-1.5 relative group
              text-left w-full hover:brightness-110 transition-all cursor-pointer active:scale-95"
          >
            <span className="text-xs font-medium px-2 py-0.5 rounded-full w-fit bg-blue-500/15 text-blue-300">
              HITL 승인수
            </span>
            <p className="text-2xl font-bold text-white tracking-tight">
              {(manualTotal + humanInteractionItems.length).toLocaleString()}
            </p>
            <p className="text-gray-500 text-xs leading-tight">
              승인요청 {manualTotal} · 질문·선택 {humanInteractionItems.length}
            </p>
            {humanInteractionItems.length > 0 && (
              <div className="flex flex-wrap gap-1 mt-0.5">
                <span className="text-[10px] px-1.5 py-0.5 rounded bg-violet-500/10 text-violet-400/80">
                  질문 {humanInteractionItems.filter(i => i.interaction_type === 'ask_followup').length}
                </span>
                <span className="text-[10px] px-1.5 py-0.5 rounded bg-sky-500/10 text-sky-400/80">
                  플랜 {humanInteractionItems.filter(i => i.interaction_type === 'plan_mode').length}
                </span>
              </div>
            )}
            <span className="absolute top-3 right-3 text-gray-700 text-[10px] group-hover:text-gray-400 transition-colors">↗</span>
          </button>
          {/* AI 자율성 (+) */}
          <button
            onClick={() => setAutonomyOpen(true)}
            className="bg-gray-900 rounded-xl border border-red-500/60 p-4 flex flex-col gap-1.5 relative group
              text-left w-full hover:brightness-110 transition-all cursor-pointer active:scale-95"
          >
            <span className="text-xs font-medium px-2 py-0.5 rounded-full w-fit bg-red-500/15 text-red-300">
              AI 자율성
            </span>
            <p className="text-2xl font-bold text-white tracking-tight">{autonomyPct}<span className="text-sm text-gray-500 font-normal ml-0.5">%</span></p>
            <p className="text-gray-500 text-xs leading-tight">
              {autonomyGood ? '매우 높은 자율성' : autonomyMid ? '양호한 자율성' : '사람 개입 多'} · 전체 행동 기준
            </p>
            {((s.agent_action_count ?? 0) + (s.human_action_count ?? 0) + (s.mixed_action_count ?? 0)) > 0 && (() => {
              const tot = (s.agent_action_count ?? 0) + (s.human_action_count ?? 0) + (s.mixed_action_count ?? 0)
              return (
                <div className="flex h-1 rounded-full overflow-hidden mt-1">
                  <div className="bg-emerald-500/70" style={{ width: `${(s.agent_action_count ?? 0) / tot * 100}%` }} />
                  <div className="bg-sky-500/60"     style={{ width: `${(s.mixed_action_count ?? 0) / tot * 100}%` }} />
                  <div className="bg-rose-500/60"    style={{ width: `${(s.human_action_count ?? 0) / tot * 100}%` }} />
                </div>
              )
            })()}
            <span className="absolute top-3 right-3 text-gray-700 text-[10px] group-hover:text-gray-400 transition-colors">↗</span>
          </button>
          {/* AI 자동승인수 (+) */}
          <button
            onClick={() => setAutoWorkOpen(true)}
            className="bg-gray-900 rounded-xl border border-red-500/60 p-4 flex flex-col gap-1.5 relative group
              text-left w-full hover:brightness-110 transition-all cursor-pointer active:scale-95"
          >
            <span className="text-xs font-medium px-2 py-0.5 rounded-full w-fit bg-red-500/15 text-red-300">
              AI 자동승인수
            </span>
            <p className="text-2xl font-bold text-white tracking-tight">{autoTotal.toLocaleString()}</p>
            <p className="text-gray-500 text-xs leading-tight">
              {autoTotal === 0 ? 'Auto-Approve 자동 실행 없음' : '자동 통과된 승인 요청'}
            </p>
            {autoTotal > 0 && (() => {
              const cats = Object.entries(s.auto_approve_by_category ?? {})
                .filter(([, v]) => v.auto > 0)
                .sort((a, b) => b[1].auto - a[1].auto)
                .slice(0, 3)
              return (
                <div className="flex flex-wrap gap-1 mt-0.5">
                  {cats.map(([cat, v]) => (
                    <span key={cat} className="text-[10px] px-1.5 py-0.5 rounded bg-red-500/10 text-red-400/80 font-mono">
                      {cat} {v.auto}
                    </span>
                  ))}
                </div>
              )
            })()}
            <span className="absolute top-3 right-3 text-gray-700 text-[10px] group-hover:text-gray-400 transition-colors">↗</span>
          </button>
        </div>
      </SectionRow>

      {/* ── BCL ── */}
      <SectionRow label="BCL" sub="Biz-value Creation Level" color="emerald">
        <div className="grid grid-cols-3 gap-3">
          {/* 토큰 사용량 (-) */}
          {(() => {
            const estTokens = s.est_total_tokens ?? 0
            const models = s.est_by_model ?? []
            const fmtTok = (n: number) =>
              n >= 1_000_000 ? `${(n / 1_000_000).toFixed(2)}M` : n >= 1_000 ? `${(n / 1_000).toFixed(1)}K` : String(n)
            const totalIn  = models.reduce((a, m) => a + m.tokens_in, 0)
            const totalOut = models.reduce((a, m) => a + m.tokens_out, 0)
            return (
              <button
                onClick={() => setTokenUsageOpen(true)}
                className="bg-gray-900 rounded-xl border border-blue-500/60 p-4 flex flex-col gap-1.5 relative group
                  text-left w-full hover:brightness-110 transition-all cursor-pointer active:scale-95"
              >
                <div className="flex items-center gap-1.5">
                  <span className="text-xs font-medium px-2 py-0.5 rounded-full w-fit bg-blue-500/15 text-blue-300">
                    토큰 사용량
                  </span>
                  <span className="text-[9px] text-blue-400/50">(추정)</span>
                </div>
                <p className="text-2xl font-bold text-white tracking-tight">
                  {fmtTok(estTokens)}
                  <span className="text-xs text-gray-500 font-normal ml-1">(추정)</span>
                </p>
                <p className="text-gray-500 text-xs leading-tight">
                  in {fmtTok(totalIn)} / out {fmtTok(totalOut)}
                </p>
                <p className="text-[10px] text-blue-400/60">
                  {models.length}개 모델 · 텍스트 볼륨 기반 추정
                </p>
                <span className="absolute top-3 right-3 text-gray-700 text-[10px] group-hover:text-gray-400 transition-colors">↗</span>
              </button>
            )
          })()}
          {/* AI code quality (+) */}
          {(() => {
            const fl = projectFirstLast
            const hasData = fl && fl.file_count > 0
            const avgScore = hasData ? Math.round(((fl!.L1 + fl!.L2 + fl!.L3 + fl!.L4) / 4) * 100) : null
            const l4Pct   = hasData ? Math.round(fl!.L4 * 100) : null
            const scoreColor = (v: number) =>
              v >= 80 ? 'text-emerald-300' : v >= 55 ? 'text-blue-300' : v >= 30 ? 'text-amber-300' : 'text-red-300'
            const LAYER_DESC: Record<string, string> = {
              L1: 'Lev', L2: 'BLEU', L3: '구조', L4: '의미',
            }
            return (
              <div className="bg-gray-900 rounded-xl border border-red-500/60 p-4 flex flex-col gap-1.5 relative">
                <span className="text-xs font-medium px-2 py-0.5 rounded-full w-fit bg-red-500/15 text-red-300">
                  AI code quality
                </span>

                {projectFirstLastLoading ? (
                  <>
                    <p className="text-2xl font-bold text-gray-600 tracking-tight">…</p>
                    <p className="text-gray-600 text-xs">유사도 계산 중</p>
                  </>
                ) : hasData && avgScore !== null ? (
                  <>
                    <p className={`text-2xl font-bold tracking-tight ${scoreColor(avgScore)}`}>
                      {avgScore}%
                      <span className="text-sm text-gray-500 font-normal ml-1">avg L1–L4</span>
                    </p>
                    <p className="text-gray-500 text-xs leading-tight">
                      {fl!.first_sha_short} → {fl!.last_sha_short} · 전체 {fl!.file_count}개 파일
                      {fl!.new_file_count != null && fl!.new_file_count > 0 && (
                        <span className="text-gray-600 ml-1">(신규 {fl!.new_file_count}개 0점)</span>
                      )}
                    </p>
                    {/* L1~L4 미니 바 */}
                    <div className="grid grid-cols-4 gap-1 mt-1">
                      {(['L1','L2','L3','L4'] as const).map(k => {
                        const pct = Math.round(fl![k] * 100)
                        return (
                          <div key={k} className="flex flex-col items-center gap-0.5">
                            <div className="w-full bg-gray-800 rounded-full h-1.5">
                              <div className={`h-1.5 rounded-full ${
                                pct >= 80 ? 'bg-emerald-500' : pct >= 55 ? 'bg-blue-500' : pct >= 30 ? 'bg-amber-500' : 'bg-red-500'
                              }`} style={{ width: `${pct}%` }} />
                            </div>
                            <p className="text-[9px] text-gray-500">{LAYER_DESC[k]} {pct}%</p>
                          </div>
                        )
                      })}
                    </div>
                    {fl && (
                      <p className="text-[10px] text-gray-600">
                        실측 {fl.common_file_count ?? fl.file_count}개 · 의미유사도 {l4Pct}%
                      </p>
                    )}
                  </>
                ) : (
                  <>
                    <p className="text-2xl font-bold text-gray-600 tracking-tight">—</p>
                    <p className="text-gray-600 text-xs leading-tight">
                      {fl && fl.file_count === 0 ? '공통 소스 파일 없음' : '백엔드 연결 시 측정'}
                    </p>
                  </>
                )}
              </div>
            )
          })()}
          {/* AI작업 효율성 (+) */}
          {metrics.filter(m => m.label === 'AI작업 효율성').map(m => <MetricCard key={m.label} metric={m} />)}
        </div>
      </SectionRow>

      {autoApproveOpen && (
        <AutoApproveModal summary={s} onClose={() => setAutoApproveOpen(false)} />
      )}
      {cancelModalOpen && (
        <CancelFollowupModal rows={cancelFollowups} onClose={() => setCancelModalOpen(false)} />
      )}
      {autonomyOpen && (
        <AutonomyModal summary={s} onClose={() => setAutonomyOpen(false)} />
      )}
      {manualApprovalOpen && (
        <ManualApprovalModal items={manualApprovalItems} interactions={humanInteractionItems} onClose={() => setManualApprovalOpen(false)} />
      )}
      {autoWorkOpen && (
        <AutoWorkModal summary={s} onClose={() => setAutoWorkOpen(false)} />
      )}
      {tokenEstOpen && (
        <TokenEstModal summary={s} onClose={() => setTokenEstOpen(false)} />
      )}
      {tokenUsageOpen && (
        <TokenUsageModal summary={s} onClose={() => setTokenUsageOpen(false)} />
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
