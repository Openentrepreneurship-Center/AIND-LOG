import { useState, useEffect } from 'react'
import { FirstLastSimilarity, SimilarityScores } from '../types'

const LAYERS = ['L1', 'L2', 'L3', 'L4'] as const
type Layer = typeof LAYERS[number]

const LAYER_META: Record<Layer, { label: string; color: string; bg: string; border: string; desc: string }> = {
  L1: { label: 'L1 Levenshtein', color: '#60a5fa', bg: 'rgba(96,165,250,0.08)',  border: 'rgba(96,165,250,0.25)',  desc: '문자 단위' },
  L2: { label: 'L2 BLEU',       color: '#34d399', bg: 'rgba(52,211,153,0.08)',  border: 'rgba(52,211,153,0.25)',  desc: '토큰 단위' },
  L3: { label: 'L3 구조',        color: '#fbbf24', bg: 'rgba(251,191,36,0.08)', border: 'rgba(251,191,36,0.25)', desc: '라인 구조' },
  L4: { label: 'L4 의미',        color: '#c084fc', bg: 'rgba(192,132,252,0.08)', border: 'rgba(192,132,252,0.25)', desc: 'TF-IDF 의미' },
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
  return (
    <svg width={size} height={size} className="shrink-0">
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke="#1f2937" strokeWidth={6} />
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke={color} strokeWidth={6}
        strokeDasharray={circ} strokeDashoffset={circ*(1-value)} strokeLinecap="round"
        transform={`rotate(-90 ${size/2} ${size/2})`}
        style={{ transition: 'stroke-dashoffset 0.6s ease' }}
      />
      <text x={size/2} y={size/2+1} textAnchor="middle" dominantBaseline="middle"
        fill={color} fontSize={size < 50 ? 9 : 12} fontWeight="700" fontFamily="monospace">
        {Math.round(value*100)}%
      </text>
    </svg>
  )
}

function ScoreBar({ layer, value, sub }: { layer: Layer; value: number; sub?: string }) {
  const m = LAYER_META[layer]
  const g = grade(value)
  return (
    <div className="rounded-lg px-3 py-2.5 space-y-1.5"
         style={{ background: m.bg, border: `1px solid ${m.border}` }}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <GaugeRing value={value} color={m.color} size={40} />
          <div>
            <p className="text-xs font-semibold" style={{ color: m.color }}>{m.label}</p>
            <p className="text-[10px] text-gray-600">{m.desc}</p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-base font-black font-mono" style={{ color: g.color }}>{Math.round(value*100)}%</p>
          {sub && <p className="text-[10px] text-gray-600 font-mono">{sub}</p>}
        </div>
      </div>
      <div className="w-full h-1.5 bg-gray-800 rounded-full overflow-hidden">
        <div className="h-full rounded-full" style={{ width: `${value*100}%`, background: m.color }} />
      </div>
    </div>
  )
}

function avg(s: SimilarityScores) {
  return (s.L1 + s.L2 + s.L3 + s.L4) / 4
}

interface Props { file: string }

export default function SimilarityFirstLast({ file }: Props) {
  const [data, setData] = useState<FirstLastSimilarity | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!file) { setData(null); setError(''); return }
    setLoading(true); setError(''); setData(null)
    fetch(`/api/similarity/first-last?file=${encodeURIComponent(file)}`)
      .then(async r => {
        if (!r.ok) { const b = await r.json().catch(() => ({})); throw new Error(b.detail ?? `HTTP ${r.status}`) }
        return r.json() as Promise<FirstLastSimilarity>
      })
      .then(d => { setData(d); setLoading(false) })
      .catch(e => { setError(e.message); setLoading(false) })
  }, [file])

  const sizeKb = (n: number) => n < 1024 ? `${n}B` : `${(n/1024).toFixed(1)}KB`

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-4">
      <div>
        <h2 className="text-sm font-semibold text-white flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-rose-400" />
          첫 커밋 ↔ 마지막 커밋 비교
        </h2>
        <p className="text-xs text-gray-500 mt-0.5">전체 개발 기간 동안 코드가 얼마나 달라졌는가</p>
      </div>

      {!file && <p className="text-gray-600 text-sm text-center py-8">파일을 선택하세요.</p>}
      {loading && (
        <div className="flex items-center justify-center h-32 gap-2 text-gray-500 text-sm">
          <div className="w-4 h-4 border-2 border-gray-700 border-t-rose-400 rounded-full animate-spin" />
          계산 중…
        </div>
      )}
      {error && (
        <p className="text-red-400 text-xs p-3 bg-red-500/10 border border-red-500/20 rounded-lg">오류: {error}</p>
      )}

      {!loading && !error && data && (
        <div className="space-y-4">
          {/* 커밋 비교 헤더 */}
          <div className="grid grid-cols-2 gap-3">
            <div className="rounded-lg px-3 py-2.5 bg-emerald-500/5 border border-emerald-500/20">
              <p className="text-[10px] text-emerald-500 font-medium mb-1">최초 커밋</p>
              <p className="font-mono text-xs text-emerald-400">{data.first_sha_short}</p>
              <p className="text-xs text-gray-400 truncate mt-0.5">{data.first_message}</p>
              <p className="text-[10px] text-gray-600 mt-1">{data.first_ts_kst}</p>
              <p className="text-[10px] font-mono text-gray-600 mt-0.5">{sizeKb(data.first_size)}</p>
            </div>
            <div className="rounded-lg px-3 py-2.5 bg-blue-500/5 border border-blue-500/20">
              <p className="text-[10px] text-blue-400 font-medium mb-1">최신 커밋</p>
              <p className="font-mono text-xs text-blue-400">{data.last_sha_short}</p>
              <p className="text-xs text-gray-400 truncate mt-0.5">{data.last_message}</p>
              <p className="text-[10px] text-gray-600 mt-1">{data.last_ts_kst}</p>
              <p className="text-[10px] font-mono text-gray-600 mt-0.5">{sizeKb(data.last_size)}</p>
            </div>
          </div>

          {/* 메타 배지 */}
          <div className="flex flex-wrap gap-2 text-xs">
            <span className="px-2 py-1 rounded-full bg-gray-800 border border-gray-700 text-gray-400">
              총 <b className="text-white">{data.total_commits}</b>개 커밋
            </span>
            <span className={`px-2 py-1 rounded-full border text-xs
              ${data.last_size > data.first_size
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                : 'bg-red-500/10 border-red-500/30 text-red-400'}`}>
              크기 {data.last_size > data.first_size ? '+' : ''}{((data.last_size - data.first_size)/1024).toFixed(1)}KB
            </span>
          </div>

          {/* 두 컬럼: 직접 비교 vs 단계 평균 */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {/* 직접 비교 */}
            <div className="space-y-2">
              <div className="flex items-center gap-2 mb-2">
                <div className="flex items-center gap-2">
                  {(() => {
                    const a = avg(data.scores)
                    const g = grade(a)
                    return (
                      <>
                        <GaugeRing value={a} color={g.color} size={52} />
                        <div>
                          <p className="text-xs text-gray-500">처음↔끝 직접 비교</p>
                          <p className="text-sm font-black font-mono" style={{ color: g.color }}>
                            {Math.round(a*100)}% · {g.label}
                          </p>
                        </div>
                      </>
                    )
                  })()}
                </div>
              </div>
              {LAYERS.map(l => (
                <ScoreBar key={l} layer={l} value={data.scores[l]} />
              ))}
            </div>

            {/* 단계별 평균 */}
            <div className="space-y-2">
              <div className="flex items-center gap-2 mb-2">
                {(() => {
                  const a = avg(data.avg_step_scores)
                  const g = grade(a)
                  return (
                    <>
                      <GaugeRing value={a} color={g.color} size={52} />
                      <div>
                        <p className="text-xs text-gray-500">단계별 평균 유사도</p>
                        <p className="text-sm font-black font-mono" style={{ color: g.color }}>
                          {Math.round(a*100)}% · {g.label}
                        </p>
                        <p className="text-[10px] text-gray-600">낮을수록 커밋마다 큰 변경</p>
                      </div>
                    </>
                  )
                })()}
              </div>
              {LAYERS.map(l => (
                <ScoreBar key={l} layer={l} value={data.avg_step_scores[l]}
                  sub={`직접 ${Math.round(data.scores[l]*100)}% 대비`}
                />
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
