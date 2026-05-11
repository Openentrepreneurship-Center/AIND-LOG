import { useState, useEffect } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, ReferenceLine,
} from 'recharts'
import { ProjectFromFirstItem } from '../types'
import { MOCK_PROJECT_SIMILARITY } from '../mockData'

const API_BASE = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''

const LAYER_COLORS = {
  L1: '#60a5fa', L2: '#34d399', L3: '#fbbf24', L4: '#c084fc',
}
const LAYER_LABELS = {
  L1: 'L1 Levenshtein', L2: 'L2 BLEU', L3: 'L3 구조적', L4: 'L4 의미론적',
}
const LAYERS = ['L1', 'L2', 'L3', 'L4'] as const

function CustomTooltip({ active, payload, label }: any) {
  if (!active || !payload?.length) return null
  const item = payload[0]?.payload as (ProjectFromFirstItem & { index: number }) | undefined
  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg p-3 shadow-xl text-xs max-w-xs">
      <p className="text-gray-300 font-medium mb-1">{label}</p>
      {item && (
        <p className="text-gray-500 mb-2 truncate">{item.message}</p>
      )}
      {payload.map((p: any) => (
        <div key={p.dataKey} className="flex justify-between gap-4">
          <span style={{ color: p.color }}>{p.name}</span>
          <span className="font-mono text-white">{Math.round(p.value * 100)}%</span>
        </div>
      ))}
      {item && (
        <p className="text-gray-600 mt-1">
          전체 {item.file_count}개
          {(item as any).new_file_count > 0 && (
            <span className="text-amber-700 ml-1">(신규 {(item as any).new_file_count}개 0점 반영)</span>
          )}
        </p>
      )}
    </div>
  )
}

const ESTIMATED_SECS = 90  // 계산 예상 소요 시간 (초)

export default function SimilarityFromFirstView({
  prefetchedData,
  parentLoading = false,
}: {
  prefetchedData?: ProjectFromFirstItem[]
  parentLoading?: boolean
}) {
  const [data, setData] = useState<ProjectFromFirstItem[]>(prefetchedData ?? [])
  const [loading, setLoading] = useState(!prefetchedData && !parentLoading ? false : !prefetchedData)
  const [visibleLayers, setVisibleLayers] = useState<Set<string>>(new Set(['L1', 'L2', 'L3', 'L4']))
  const [error, setError] = useState(false)
  const [elapsed, setElapsed] = useState(0)

  useEffect(() => {
    // 부모가 이미 fetch 중이면 컴포넌트 자체 fetch 금지 (중복 방지)
    if (parentLoading) { setLoading(true); return }
    if (prefetchedData) { setData(prefetchedData); setLoading(false); return }
    setLoading(true)
    setElapsed(0)
    const start = Date.now()
    const timer = setInterval(() => {
      setElapsed(Math.floor((Date.now() - start) / 1000))
    }, 1000)
    fetch(`${API_BASE}/api/similarity/project-from-first`)
      .then(r => r.ok ? r.json() : Promise.reject(r))
      .then((d: ProjectFromFirstItem[]) => { clearInterval(timer); setData(d); setLoading(false) })
      .catch(() => {
        clearInterval(timer)
        const mock = MOCK_PROJECT_SIMILARITY.map((m, i) => ({
          sha: m.sha, sha_short: m.sha_short, message: m.message, ts_kst: m.ts_kst,
          file_count: m.files_changed,
          scores: { L1: 1 - i * 0.003, L2: 1 - i * 0.004, L3: 1 - i * 0.003, L4: 1 - i * 0.005 },
        }))
        setData(mock); setLoading(false); setError(true)
      })
    return () => clearInterval(timer)
  }, [prefetchedData, parentLoading])

  const refSha = data[0]?.ref_sha_short ?? data[0]?.sha_short

  const chartData = data.map((item, i) => ({
    ...item,
    index: i,
    label: item.sha_short,
    L1: item.scores.L1,
    L2: item.scores.L2,
    L3: item.scores.L3,
    L4: item.scores.L4,
  }))

  const toggleLayer = (layer: string) => {
    setVisibleLayers(prev => {
      const next = new Set(prev)
      if (next.has(layer)) { if (next.size > 1) next.delete(layer) }
      else next.add(layer)
      return next
    })
  }

  // 마지막 커밋의 평균 유사도
  const last = chartData[chartData.length - 1]
  const lastAvgRaw = last ? ((last.L1 + last.L2 + last.L3 + last.L4) / 4) * 100 : null
  const lastAvg = lastAvgRaw !== null ? parseFloat(lastAvgRaw.toFixed(1)) : null

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
      {/* 헤더 */}
      <div className="flex items-start justify-between mb-3 gap-3 flex-wrap">
        <div>
          <h3 className="text-sm font-semibold text-white">AI코드 Quality 차트</h3>
          <p className="text-xs text-gray-500 mt-0.5">
            각 커밋을 프로젝트 시작 시점
            {refSha && <span className="font-mono text-gray-400 mx-1">{refSha}</span>}
            과 직접 비교 — 낮을수록 초기 코드에서 많이 달라진 것
          </p>
        </div>
        <div className="flex flex-wrap gap-1.5">
          {LAYERS.map(l => (
            <button
              key={l}
              onClick={() => toggleLayer(l)}
              className="text-[11px] px-2 py-0.5 rounded-full border transition-all font-mono"
              style={visibleLayers.has(l)
                ? { background: LAYER_COLORS[l] + '22', color: LAYER_COLORS[l], borderColor: LAYER_COLORS[l] + '50' }
                : { background: 'transparent', color: '#4b5563', borderColor: '#374151', opacity: 0.5 }
              }
            >
              {l}
            </button>
          ))}
        </div>
      </div>

      {error && <p className="text-xs text-amber-500/70 mb-2">⚠ 샘플 데이터 표시 중</p>}

      {loading ? (
        <div className="h-52 flex flex-col items-center justify-center gap-4">
          <p className="text-gray-400 text-sm font-medium">
            커밋별 유사도 계산 중…
            <span className="font-mono text-gray-500 ml-2">{elapsed}s / ~{ESTIMATED_SECS}s</span>
          </p>
          <div className="w-64 h-2 bg-gray-800 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-blue-600 to-purple-500 rounded-full transition-all duration-1000"
              style={{ width: `${Math.min(Math.round((elapsed / ESTIMATED_SECS) * 100), 95)}%` }}
            />
          </div>
          <p className="text-xs text-gray-600">전체 커밋 × 소스파일 L1~L4 유사도 일괄 계산 중</p>
        </div>
      ) : (
        <>
          {/* 마지막 커밋 요약 수치 */}
          {lastAvg !== null && last && (
            <div className="flex items-center gap-2 mb-3 flex-wrap">
              <span className="text-xs text-gray-500">현재 유사도</span>
              <span className={`text-xl font-bold font-mono ${
                lastAvg >= 80 ? 'text-emerald-400' : lastAvg >= 55 ? 'text-blue-400' : lastAvg >= 30 ? 'text-amber-400' : 'text-red-400'
              }`}>{lastAvg}%</span>
              <span className="text-gray-700 text-xs">avg</span>
              <span className="text-xs text-gray-600 ml-1">{last.file_count}개 파일</span>
              <span className="ml-2 flex gap-3">
                {LAYERS.map(l => (
                  <span key={l} className="text-xs font-mono" style={{ color: LAYER_COLORS[l] }}>
                    {l} {Math.round((last[l] ?? 0) * 100)}%
                  </span>
                ))}
              </span>
            </div>
          )}

          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={chartData} margin={{ top: 4, right: 16, left: -18, bottom: 4 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
              <XAxis
                dataKey="label"
                tick={{ fontSize: 9, fill: '#6b7280', fontFamily: 'monospace' }}
                interval={Math.max(1, Math.floor(chartData.length / 14))}
              />
              <YAxis
                domain={[0, 1]}
                tickFormatter={v => `${Math.round(v * 100)}%`}
                tick={{ fontSize: 10, fill: '#6b7280' }}
              />
              <Tooltip content={<CustomTooltip />} />
              <ReferenceLine y={0.8} stroke="#374151" strokeDasharray="4 2" label={{ value: '80%', position: 'insideRight', fontSize: 9, fill: '#4b5563' }} />
              <ReferenceLine y={0.5} stroke="#374151" strokeDasharray="4 2" label={{ value: '50%', position: 'insideRight', fontSize: 9, fill: '#4b5563' }} />
              {LAYERS.filter(l => visibleLayers.has(l)).map(l => (
                <Line key={l} type="monotone" dataKey={l} stroke={LAYER_COLORS[l]}
                  strokeWidth={1.5} dot={false} name={LAYER_LABELS[l]} activeDot={{ r: 3 }} />
              ))}
            </LineChart>
          </ResponsiveContainer>

          {/* 범례 */}
          <div className="flex gap-4 mt-2 flex-wrap">
            {LAYERS.map(l => (
              <span key={l} className="flex items-center gap-1 text-[10px]"
                style={{ color: visibleLayers.has(l) ? '#9ca3af' : '#4b5563' }}>
                <span className="inline-block w-4 h-0.5 rounded" style={{ background: LAYER_COLORS[l] }} />
                {LAYER_LABELS[l]}
              </span>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
