import { useState, useEffect } from 'react'
import { SimilarityResult } from '../types'

const LAYERS = ['L1', 'L2', 'L3', 'L4'] as const
type Layer = typeof LAYERS[number]

const LAYER_META: Record<Layer, { label: string; short: string; color: string; bg: string; border: string; desc: string }> = {
  L1: {
    label: 'L1 · Levenshtein',
    short: '문자 유사도',
    color: '#60a5fa',
    bg: 'rgba(96,165,250,0.08)',
    border: 'rgba(96,165,250,0.25)',
    desc: '글자 하나하나가 얼마나 같은가',
  },
  L2: {
    label: 'L2 · BLEU',
    short: '토큰 유사도',
    color: '#34d399',
    bg: 'rgba(52,211,153,0.08)',
    border: 'rgba(52,211,153,0.25)',
    desc: '단어·기호 조합이 얼마나 같은가',
  },
  L3: {
    label: 'L3 · 구조',
    short: '구조 유사도',
    color: '#fbbf24',
    bg: 'rgba(251,191,36,0.08)',
    border: 'rgba(251,191,36,0.25)',
    desc: '코드 줄 구조가 얼마나 같은가',
  },
  L4: {
    label: 'L4 · 의미',
    short: '의미 유사도',
    color: '#c084fc',
    bg: 'rgba(192,132,252,0.08)',
    border: 'rgba(192,132,252,0.25)',
    desc: '코드의 의미·맥락이 얼마나 같은가',
  },
}

function grade(v: number): { label: string; color: string } {
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
      <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#1f2937" strokeWidth={6} />
      <circle
        cx={size / 2} cy={size / 2} r={r}
        fill="none" stroke={color} strokeWidth={6}
        strokeDasharray={circ}
        strokeDashoffset={offset}
        strokeLinecap="round"
        transform={`rotate(-90 ${size / 2} ${size / 2})`}
        style={{ transition: 'stroke-dashoffset 0.6s ease' }}
      />
      <text
        x={size / 2} y={size / 2 + 1}
        textAnchor="middle" dominantBaseline="middle"
        fill={color} fontSize={size < 50 ? 9 : 11} fontWeight="700" fontFamily="monospace"
      >
        {Math.round(value * 100)}%
      </text>
    </svg>
  )
}

function LayerBar({ layer, value }: { layer: Layer; value: number }) {
  const m = LAYER_META[layer]
  const pct = Math.round(value * 100)
  const g = grade(value)
  return (
    <div
      className="flex items-center gap-3 rounded-lg px-3 py-2.5"
      style={{ background: m.bg, border: `1px solid ${m.border}` }}
    >
      <GaugeRing value={value} color={m.color} size={48} />
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-1">
          <span className="text-xs font-semibold" style={{ color: m.color }}>{m.label}</span>
          <span className="text-xs font-bold font-mono" style={{ color: g.color }}>{g.label}</span>
        </div>
        {/* 바 */}
        <div className="w-full h-2 bg-gray-800 rounded-full overflow-hidden">
          <div
            className="h-full rounded-full"
            style={{ width: `${pct}%`, background: m.color, transition: 'width 0.6s ease' }}
          />
        </div>
        <p className="text-xs text-gray-600 mt-1 truncate">{m.desc}</p>
      </div>
      <span
        className="text-lg font-black font-mono shrink-0 w-14 text-right"
        style={{ color: m.color }}
      >
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
        <p className="text-xl font-black font-mono" style={{ color: g.color }}>
          {Math.round(avg * 100)}%
        </p>
        <p className="text-xs font-medium mt-0.5" style={{ color: g.color }}>{g.label}</p>
      </div>
    </div>
  )
}

function ArrowDown({ changed }: { changed: boolean }) {
  return (
    <div className="flex flex-col items-center py-1">
      <div className={`w-0.5 h-4 ${changed ? 'bg-amber-500/40' : 'bg-gray-700'}`} />
      <svg width="16" height="10" viewBox="0 0 16 10" fill="none">
        <path
          d="M8 10 L0 0 L16 0 Z"
          fill={changed ? '#f59e0b' : '#374151'}
        />
      </svg>
    </div>
  )
}

function CommitChip({
  sha, message, ts, isFirst, isCurrent,
}: {
  sha: string; message: string; ts: string; isFirst?: boolean; isCurrent?: boolean
}) {
  return (
    <div className={`flex items-center gap-2 px-3 py-2 rounded-lg border
      ${isCurrent ? 'bg-blue-500/10 border-blue-500/30' : 'bg-gray-800/60 border-gray-700/50'}`}>
      {isFirst && (
        <span className="text-xs px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 shrink-0">
          시작
        </span>
      )}
      <span className={`font-mono text-xs shrink-0 ${isCurrent ? 'text-blue-400' : 'text-gray-500'}`}>
        {sha}
      </span>
      <span className="text-xs text-gray-400 truncate flex-1">{message}</span>
      {ts && <span className="text-xs text-gray-600 shrink-0">{ts}</span>}
    </div>
  )
}

interface Props {
  file: string
}

export default function SimilarityStepView({ file }: Props) {
  const [results, setResults] = useState<SimilarityResult[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [activeStep, setActiveStep] = useState<number | null>(null)

  useEffect(() => {
    if (!file) {
      setResults([])
      setError('')
      setActiveStep(null)
      return
    }
    setLoading(true)
    setError('')
    setResults([])
    setActiveStep(null)
    fetch(`/api/similarity?file=${encodeURIComponent(file)}`)
      .then(async r => {
        if (!r.ok) {
          const body = await r.json().catch(() => ({}))
          throw new Error(body.detail ?? `HTTP ${r.status}`)
        }
        return r.json()
      })
      .then((data: SimilarityResult[]) => {
        setResults(data)
        // 변경이 있는 마지막 스텝을 기본 선택
        let idx = -1
        for (let j = data.length - 1; j >= 0; j--) {
          if (data[j].changed && data[j].prev_sha !== '') { idx = j; break }
        }
        setActiveStep(idx >= 0 ? idx : null)
        setLoading(false)
      })
      .catch(e => { setError(e.message); setLoading(false) })
  }, [file])

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-4">
      {/* 헤더 */}
      <div>
        <h2 className="text-sm font-semibold text-white flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-amber-400" />
          커밋 단계별 유사도 수치
        </h2>
        <p className="text-xs text-gray-500 mt-1">
          각 커밋 단계마다 코드가 얼마나 바뀌었는지 L1~L4 수치로 확인합니다.
          낮을수록 이전 커밋과 크게 다른 코드입니다.
        </p>
      </div>

      {!loading && !error && !file && (
        <p className="text-gray-500 text-sm text-center py-8">
          위쪽 트리에서 분석할 파일을 선택해주세요.
        </p>
      )}
      {!loading && !error && file && results.length === 0 && (
        <p className="text-gray-500 text-sm text-center py-8">
          '{file}' 에 해당하는 커밋 히스토리가 없습니다.
        </p>
      )}

      {loading && (
        <div className="flex items-center justify-center h-40 text-gray-500 text-sm gap-2">
          <div className="w-4 h-4 border-2 border-gray-700 border-t-amber-500 rounded-full animate-spin" />
          계산 중…
        </div>
      )}
      {error && (
        <p className="text-red-400 text-xs p-3 bg-red-500/10 border border-red-500/20 rounded-lg">
          오류: {error}
        </p>
      )}

      {!loading && !error && results.length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          {/* ── 왼쪽: 스텝 타임라인 ── */}
          <div className="space-y-0">
            <p className="text-xs text-gray-500 mb-3 font-medium">커밋 흐름 (클릭하면 상세 수치 표시)</p>

            {results.map((r, i) => {
              const isFirst = r.prev_sha === ''
              const isActive = activeStep === i
              const hasChange = !isFirst && r.changed

              return (
                <div key={r.sha}>
                  {/* 커밋 노드 */}
                  <div
                    className={`flex items-start gap-2 cursor-pointer group
                      ${!isFirst ? 'rounded-lg p-1 transition-colors ' +
                        (isActive ? 'bg-blue-500/5 outline outline-1 outline-blue-500/30' : 'hover:bg-gray-800/30') : ''}`}
                    onClick={() => !isFirst && setActiveStep(isActive ? null : i)}
                  >
                    {/* 타임라인 선 */}
                    <div className="flex flex-col items-center shrink-0 mt-1">
                      <div
                        className={`w-3 h-3 rounded-full border-2 shrink-0
                          ${isFirst ? 'border-emerald-500 bg-emerald-500/20' :
                            hasChange ? 'border-amber-400 bg-amber-400/20' :
                            'border-gray-600 bg-gray-800'}`}
                      />
                    </div>

                    {/* 커밋 정보 */}
                    <div className="flex-1 min-w-0 pb-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className={`font-mono text-xs
                          ${isFirst ? 'text-emerald-400' : isActive ? 'text-blue-400' : 'text-gray-500'}`}>
                          {r.sha_short}
                        </span>
                        {isFirst && (
                          <span className="text-xs px-1.5 rounded bg-emerald-500/15 text-emerald-400 border border-emerald-500/20">
                            시작
                          </span>
                        )}
                        {hasChange && !isFirst && (
                          <span className="text-xs px-1.5 rounded bg-amber-500/15 text-amber-400 border border-amber-500/20">
                            변경됨
                          </span>
                        )}
                        {!isFirst && !r.changed && (
                          <span className="text-xs px-1.5 rounded bg-gray-800 text-gray-600 border border-gray-700">
                            동일
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-gray-400 truncate mt-0.5">{r.message}</p>
                      {r.ts_kst && (
                        <p className="text-xs text-gray-600 mt-0.5">{r.ts_kst}</p>
                      )}
                      {/* 미니 스코어 */}
                      {!isFirst && (
                        <div className="flex items-center gap-2 mt-1.5 flex-wrap">
                          {LAYERS.map(l => {
                            const m = LAYER_META[l]
                            const pct = Math.round(r.scores[l] * 100)
                            return (
                              <span
                                key={l}
                                className="text-xs font-mono px-1.5 py-0.5 rounded border"
                                style={{
                                  color: m.color,
                                  borderColor: m.border,
                                  background: m.bg,
                                }}
                              >
                                {l} {pct}%
                              </span>
                            )
                          })}
                        </div>
                      )}
                    </div>
                  </div>

                  {/* 연결선 */}
                  {i < results.length - 1 && (
                    <div className="ml-[5px] w-0.5 h-4 bg-gray-800" />
                  )}
                </div>
              )
            })}
          </div>

          {/* ── 오른쪽: 선택된 스텝 상세 ── */}
          <div>
            {activeStep !== null && results[activeStep] ? (() => {
              const r = results[activeStep]
              const scores = r.scores as Record<Layer, number>
              const sizeKb = (n: number) => n < 1024 ? `${n}B` : `${(n/1024).toFixed(1)}KB`
              return (
                <div className="space-y-3">
                  <p className="text-xs text-gray-500 font-medium">상세 유사도</p>

                  {/* 이전 → 현재 */}
                  <div className="space-y-1">
                    <CommitChip sha={r.prev_sha_short} message={results.find(x=>x.sha_short===r.prev_sha_short)?.message ?? ''} ts="" isFirst />
                    <ArrowDown changed={r.changed} />
                    <CommitChip sha={r.sha_short} message={r.message} ts={r.ts_kst} isCurrent />
                  </div>

                  {/* 파일 크기 변화 */}
                  <div className="flex items-center gap-3 text-xs px-3 py-2 rounded-lg bg-gray-800/40 border border-gray-700/40">
                    <span className="text-gray-500">파일 크기</span>
                    <span className="font-mono text-gray-400">{sizeKb(r.old_size)}</span>
                    <span className="text-gray-700">→</span>
                    <span className="font-mono text-gray-300">{sizeKb(r.new_size)}</span>
                    {r.new_size !== r.old_size && (
                      <span className={`font-mono font-semibold ml-auto
                        ${r.new_size > r.old_size ? 'text-emerald-400' : 'text-red-400'}`}>
                        {r.new_size > r.old_size ? '+' : ''}
                        {((r.new_size - r.old_size) / 1024).toFixed(1)}KB
                      </span>
                    )}
                  </div>

                  {/* 종합 점수 */}
                  <OverallScore scores={scores} />

                  {/* L1~L4 상세 바 */}
                  <div className="space-y-2">
                    {LAYERS.map(layer => (
                      <LayerBar key={layer} layer={layer} value={scores[layer]} />
                    ))}
                  </div>

                  {/* 해석 가이드 */}
                  <div className="rounded-lg p-3 bg-gray-800/30 border border-gray-700/30 space-y-1.5">
                    <p className="text-xs text-gray-500 font-medium">수치 해석 가이드</p>
                    {[
                      { range: '95~100%', meaning: '거의 동일 — 공백·주석만 변경', color: '#34d399' },
                      { range: '80~94%',  meaning: '유사함 — 소규모 수정',         color: '#60a5fa' },
                      { range: '55~79%',  meaning: '부분 변경 — 기능 일부 추가/수정', color: '#fbbf24' },
                      { range: '25~54%',  meaning: '많이 변경 — 큰 리팩토링/확장',  color: '#f97316' },
                      { range: '0~24%',   meaning: '대폭 변경 — 거의 새 코드',      color: '#f87171' },
                    ].map(item => (
                      <div key={item.range} className="flex items-center gap-2">
                        <span
                          className="text-xs font-mono w-20 shrink-0 font-semibold"
                          style={{ color: item.color }}
                        >
                          {item.range}
                        </span>
                        <span className="text-xs text-gray-500">{item.meaning}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )
            })() : (
              <div className="flex flex-col items-center justify-center h-full min-h-40 gap-2 text-gray-600">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M15 10l-4 4M9 10l4 4" />
                  <circle cx="12" cy="12" r="9" />
                </svg>
                <p className="text-sm">왼쪽 커밋을 클릭하면</p>
                <p className="text-xs">L1~L4 상세 수치가 표시됩니다</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
