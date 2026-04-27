import { useState, useEffect } from 'react'
import { SimilarityResult } from '../types'
import Modal from './Modal'

const LAYERS = ['L1', 'L2', 'L3', 'L4'] as const
type Layer = typeof LAYERS[number]

const LAYER_META: Record<Layer, { label: string; color: string; bg: string; border: string; desc: string }> = {
  L1: { label: 'L1 · Levenshtein', color: '#60a5fa', bg: 'rgba(96,165,250,0.08)', border: 'rgba(96,165,250,0.25)', desc: '글자 하나하나가 얼마나 같은가' },
  L2: { label: 'L2 · BLEU',        color: '#34d399', bg: 'rgba(52,211,153,0.08)', border: 'rgba(52,211,153,0.25)', desc: '단어·기호 조합이 얼마나 같은가' },
  L3: { label: 'L3 · 구조',         color: '#fbbf24', bg: 'rgba(251,191,36,0.08)', border: 'rgba(251,191,36,0.25)', desc: '코드 줄 구조가 얼마나 같은가' },
  L4: { label: 'L4 · 의미',         color: '#c084fc', bg: 'rgba(192,132,252,0.08)', border: 'rgba(192,132,252,0.25)', desc: '코드의 의미·맥락이 얼마나 같은가' },
}

function grade(v: number) {
  if (v >= 0.95) return { label: '거의 동일', color: '#34d399' }
  if (v >= 0.80) return { label: '유사함',   color: '#60a5fa' }
  if (v >= 0.55) return { label: '부분 변경', color: '#fbbf24' }
  if (v >= 0.25) return { label: '많이 변경', color: '#f97316' }
  return               { label: '대폭 변경', color: '#f87171' }
}

function GaugeRing({ value, color, size = 56 }: { value: number; color: string; size?: number }) {
  const r = (size - 8) / 2
  const circ = 2 * Math.PI * r
  const offset = circ * (1 - value)
  return (
    <svg width={size} height={size} className="shrink-0">
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke="#1f2937" strokeWidth={6} />
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke={color} strokeWidth={6}
        strokeDasharray={circ} strokeDashoffset={offset} strokeLinecap="round"
        transform={`rotate(-90 ${size/2} ${size/2})`}
        style={{ transition: 'stroke-dashoffset 0.5s ease' }}
      />
      <text x={size/2} y={size/2+1} textAnchor="middle" dominantBaseline="middle"
        fill={color} fontSize={size < 50 ? 9 : 11} fontWeight="700" fontFamily="monospace">
        {Math.round(value*100)}%
      </text>
    </svg>
  )
}

function ScorePill({ layer, value }: { layer: Layer; value: number }) {
  const m = LAYER_META[layer]
  return (
    <span className="inline-flex items-center gap-1 text-[10px] font-mono font-bold px-1.5 py-0.5 rounded"
      style={{ color: m.color, background: m.bg, border: `1px solid ${m.border}` }}>
      {layer} {Math.round(value*100)}%
    </span>
  )
}

function LayerBar({ layer, value }: { layer: Layer; value: number }) {
  const m = LAYER_META[layer]
  const pct = Math.round(value * 100)
  const g = grade(value)
  return (
    <div className="flex items-center gap-3 rounded-lg px-3 py-2.5"
         style={{ background: m.bg, border: `1px solid ${m.border}` }}>
      <GaugeRing value={value} color={m.color} size={48} />
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-1">
          <span className="text-xs font-semibold" style={{ color: m.color }}>{m.label}</span>
          <span className="text-xs font-bold font-mono" style={{ color: g.color }}>{g.label}</span>
        </div>
        <div className="w-full h-2 bg-gray-800 rounded-full overflow-hidden">
          <div className="h-full rounded-full" style={{ width: `${pct}%`, background: m.color, transition: 'width 0.5s ease' }} />
        </div>
        <p className="text-xs text-gray-600 mt-1 truncate">{m.desc}</p>
      </div>
      <span className="text-lg font-black font-mono shrink-0 w-12 text-right" style={{ color: m.color }}>
        {pct}<span className="text-xs font-normal">%</span>
      </span>
    </div>
  )
}

function OverallScore({ scores }: { scores: Record<Layer, number> }) {
  const avg = (scores.L1 + scores.L2 + scores.L3 + scores.L4) / 4
  const g = grade(avg)
  return (
    <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-gray-800/50 border border-gray-700/50">
      <GaugeRing value={avg} color={g.color} size={64} />
      <div>
        <p className="text-xs text-gray-500 mb-0.5">종합 유사도 (L1~L4 평균)</p>
        <p className="text-2xl font-black font-mono" style={{ color: g.color }}>{Math.round(avg*100)}%</p>
        <p className="text-xs font-medium mt-0.5" style={{ color: g.color }}>{g.label}</p>
      </div>
    </div>
  )
}

/* ── 모달 내부 상세 컨텐츠 ─────────────────────────────────── */
function StepDetail({ r, prev }: { r: SimilarityResult; prev?: SimilarityResult }) {
  const scores = r.scores as Record<Layer, number>
  const sizeKb = (n: number) => n < 1024 ? `${n}B` : `${(n/1024).toFixed(1)}KB`
  return (
    <div className="space-y-4">
      {/* 커밋 비교 */}
      <div className="space-y-1.5">
        {r.prev_sha !== '' && (
          <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-gray-800/60 border border-gray-700/50">
            <span className="text-xs px-1.5 py-0.5 rounded bg-gray-700 text-gray-400 border border-gray-600 shrink-0">이전</span>
            <span className="font-mono text-xs text-gray-500 shrink-0">{r.prev_sha_short}</span>
            <span className="text-xs text-gray-400 truncate">{prev?.message ?? ''}</span>
          </div>
        )}
        <div className="flex items-center justify-center">
          <div className={`flex flex-col items-center ${r.changed ? 'text-amber-500' : 'text-gray-700'}`}>
            <div className="w-0.5 h-3 bg-current" />
            <span className="text-[10px]">▼</span>
          </div>
        </div>
        <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-blue-500/10 border border-blue-500/30">
          <span className="text-xs px-1.5 py-0.5 rounded bg-blue-500/20 text-blue-400 border border-blue-500/30 shrink-0">현재</span>
          <span className="font-mono text-xs text-blue-400 shrink-0">{r.sha_short}</span>
          <span className="text-xs text-gray-300 truncate">{r.message}</span>
          {r.ts_kst && <span className="text-xs text-gray-600 shrink-0">{r.ts_kst}</span>}
        </div>
      </div>

      {/* 파일 크기 */}
      <div className="flex items-center gap-3 text-xs px-3 py-2 rounded-lg bg-gray-800/40 border border-gray-700/40">
        <span className="text-gray-500">파일 크기</span>
        <span className="font-mono text-gray-400">{sizeKb(r.old_size)}</span>
        <span className="text-gray-700">→</span>
        <span className="font-mono text-gray-300">{sizeKb(r.new_size)}</span>
        {r.new_size !== r.old_size && (
          <span className={`font-mono font-semibold ml-auto ${r.new_size > r.old_size ? 'text-emerald-400' : 'text-red-400'}`}>
            {r.new_size > r.old_size ? '+' : ''}{((r.new_size - r.old_size) / 1024).toFixed(1)}KB
          </span>
        )}
      </div>

      {/* 종합 점수 */}
      <OverallScore scores={scores} />

      {/* L1~L4 */}
      <div className="space-y-2">
        {LAYERS.map(layer => <LayerBar key={layer} layer={layer} value={scores[layer]} />)}
      </div>

      {/* 해석 가이드 */}
      <details className="rounded-lg bg-gray-800/30 border border-gray-700/30">
        <summary className="text-xs text-gray-500 font-medium px-3 py-2 cursor-pointer select-none hover:text-gray-400">
          수치 해석 가이드
        </summary>
        <div className="px-3 pb-3 space-y-1.5 mt-1">
          {[
            { range: '95~100%', meaning: '거의 동일 — 공백·주석만 변경', color: '#34d399' },
            { range: '80~94%',  meaning: '유사함 — 소규모 수정',         color: '#60a5fa' },
            { range: '55~79%',  meaning: '부분 변경 — 기능 일부 추가/수정', color: '#fbbf24' },
            { range: '25~54%',  meaning: '많이 변경 — 큰 리팩토링/확장',  color: '#f97316' },
            { range: '0~24%',   meaning: '대폭 변경 — 거의 새 코드',      color: '#f87171' },
          ].map(item => (
            <div key={item.range} className="flex items-center gap-2">
              <span className="text-xs font-mono w-20 shrink-0 font-semibold" style={{ color: item.color }}>{item.range}</span>
              <span className="text-xs text-gray-500">{item.meaning}</span>
            </div>
          ))}
        </div>
      </details>
    </div>
  )
}

/* ── 메인 컴포넌트 ───────────────────────────────────────────── */
interface Props { file: string }

export default function SimilarityStepView({ file }: Props) {
  const [results, setResults] = useState<SimilarityResult[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [modalIdx, setModalIdx] = useState<number | null>(null)

  useEffect(() => {
    if (!file) { setResults([]); setError(''); setModalIdx(null); return }
    setLoading(true); setError(''); setResults([]); setModalIdx(null)
    fetch(`/api/similarity?file=${encodeURIComponent(file)}`)
      .then(async r => {
        if (!r.ok) { const b = await r.json().catch(() => ({})); throw new Error(b.detail ?? `HTTP ${r.status}`) }
        return r.json()
      })
      .then((data: SimilarityResult[]) => { setResults(data); setLoading(false) })
      .catch(e => { setError(e.message); setLoading(false) })
  }, [file])

  const selected = modalIdx !== null ? results[modalIdx] : null
  const prevResult = modalIdx !== null && modalIdx > 0 ? results[modalIdx - 1] : undefined

  return (
    <>
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-4">
        {/* 헤더 */}
        <div>
          <h2 className="text-sm font-semibold text-white flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-amber-400" />
            커밋 단계별 유사도
            {results.length > 0 && <span className="text-xs font-normal text-gray-500">{results.length}개 커밋</span>}
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">항목 클릭 → 상세 모달 · 낮을수록 큰 변경</p>
        </div>

        {!loading && !error && !file && (
          <p className="text-gray-500 text-sm text-center py-8">파일을 선택하세요.</p>
        )}
        {!loading && !error && file && results.length === 0 && (
          <p className="text-gray-500 text-sm text-center py-8">'{file}' 의 커밋 히스토리가 없습니다.</p>
        )}
        {loading && (
          <div className="flex items-center justify-center h-40 text-gray-500 text-sm gap-2">
            <div className="w-4 h-4 border-2 border-gray-700 border-t-amber-500 rounded-full animate-spin" />
            계산 중…
          </div>
        )}
        {error && (
          <p className="text-red-400 text-xs p-3 bg-red-500/10 border border-red-500/20 rounded-lg">오류: {error}</p>
        )}

        {!loading && !error && results.length > 0 && (
          <div className="overflow-y-auto max-h-[480px] space-y-0"
               style={{ scrollbarWidth: 'thin', scrollbarColor: '#374151 transparent' }}>
            {results.map((r, i) => {
              const isFirst = r.prev_sha === ''
              const hasChange = !isFirst && r.changed
              const scores = r.scores as Record<Layer, number>
              const avg = (scores.L1 + scores.L2 + scores.L3 + scores.L4) / 4
              const g = grade(avg)

              return (
                <div key={r.sha}>
                  <div
                    onClick={() => !isFirst && setModalIdx(i)}
                    className={`flex items-start gap-2 rounded-lg px-2 py-1.5 transition-colors
                      ${!isFirst ? 'cursor-pointer hover:bg-gray-800/50 active:bg-gray-800' : ''}`}
                  >
                    {/* 타임라인 점 */}
                    <div className="flex flex-col items-center shrink-0 mt-1.5">
                      <div className={`w-2.5 h-2.5 rounded-full border-2 shrink-0
                        ${isFirst ? 'border-emerald-500 bg-emerald-500/20' :
                          hasChange ? 'border-amber-400 bg-amber-400/20' :
                          'border-gray-600 bg-gray-800'}`}
                      />
                    </div>

                    {/* 커밋 정보 */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <span className={`font-mono text-xs ${isFirst ? 'text-emerald-400' : 'text-gray-500'}`}>
                          {r.sha_short}
                        </span>
                        {isFirst && (
                          <span className="text-[10px] px-1.5 rounded bg-emerald-500/15 text-emerald-400 border border-emerald-500/20">시작</span>
                        )}
                        {hasChange && (
                          <span className="text-[10px] px-1.5 rounded bg-amber-500/15 text-amber-400 border border-amber-500/20">변경됨</span>
                        )}
                        {!isFirst && !r.changed && (
                          <span className="text-[10px] px-1.5 rounded bg-gray-800 text-gray-600 border border-gray-700">동일</span>
                        )}
                      </div>
                      <p className="text-xs text-gray-400 truncate mt-0.5">{r.message}</p>

                      {/* 점수 pills — 첫 커밋이 아닐 때 */}
                      {!isFirst && (
                        <div className="flex items-center gap-1.5 mt-1 flex-wrap">
                          {LAYERS.map(layer => <ScorePill key={layer} layer={layer} value={scores[layer]} />)}
                          <span className="text-[10px] font-bold font-mono ml-1" style={{ color: g.color }}>
                            avg {Math.round(avg*100)}%
                          </span>
                        </div>
                      )}
                    </div>

                    {/* 전체 avg 게이지 - 첫 커밋 아닐 때 */}
                    {!isFirst && (
                      <div className="shrink-0">
                        <GaugeRing value={avg} color={g.color} size={36} />
                      </div>
                    )}
                  </div>

                  {/* 타임라인 연결선 */}
                  {i < results.length - 1 && (
                    <div className="ml-[9px] w-0.5 h-3 bg-gray-800" />
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* 상세 모달 */}
      <Modal
        open={modalIdx !== null && selected !== null}
        onClose={() => setModalIdx(null)}
        title={`커밋 상세 · ${selected?.sha_short ?? ''} — ${(selected?.message ?? '').slice(0, 40)}`}
        width="xl"
      >
        {selected && <StepDetail r={selected} prev={prevResult} />}
      </Modal>
    </>
  )
}
