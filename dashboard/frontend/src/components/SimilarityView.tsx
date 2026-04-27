import { useState, useEffect } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer, ReferenceLine,
} from 'recharts'
import { SimilarityResult } from '../types'

// 레이어별 색상
const LAYER_COLORS = {
  L1: '#60a5fa',  // blue-400
  L2: '#34d399',  // emerald-400
  L3: '#fbbf24',  // amber-400
  L4: '#c084fc',  // purple-400
}

const LAYER_LABELS: Record<string, string> = {
  L1: 'L1 Levenshtein',
  L2: 'L2 BLEU',
  L3: 'L3 구조적',
  L4: 'L4 의미론적',
}

const LAYER_DESC: Record<string, string> = {
  L1: '문자 단위 표면 유사도 (rapidfuzz Levenshtein)',
  L2: '토큰 n-gram BLEU (JS regex 토크나이저)',
  L3: '라인 구조 유사도 (SequenceMatcher · TSED 적응)',
  L4: 'TF-IDF char cosine (CodeBERTScore 경량 적응)',
}

function ScoreBadge({ value }: { value: number }) {
  const pct = Math.round(value * 100)
  const color =
    pct >= 90 ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20' :
    pct >= 70 ? 'text-blue-400 bg-blue-500/10 border-blue-500/20' :
    pct >= 40 ? 'text-amber-400 bg-amber-500/10 border-amber-500/20' :
                'text-red-400 bg-red-500/10 border-red-500/20'
  return (
    <span className={`inline-block px-2 py-0.5 rounded border text-xs font-mono font-semibold ${color}`}>
      {pct}%
    </span>
  )
}

function SizeChange({ oldSize, newSize }: { oldSize: number; newSize: number }) {
  if (oldSize === 0) return <span className="text-gray-500 text-xs">신규</span>
  const diff = newSize - oldSize
  const sign = diff > 0 ? '+' : ''
  const color = diff > 0 ? 'text-emerald-400' : diff < 0 ? 'text-red-400' : 'text-gray-500'
  return (
    <span className={`text-xs font-mono ${color}`}>
      {sign}{diff.toLocaleString()}B
    </span>
  )
}

// 커스텀 툴팁
function CustomTooltip({ active, payload, label }: any) {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg p-3 shadow-xl text-xs">
      <p className="text-gray-300 font-medium mb-2">{label}</p>
      {payload.map((p: any) => (
        <div key={p.dataKey} className="flex items-center gap-2 py-0.5">
          <span className="w-2 h-2 rounded-full" style={{ background: p.color }} />
          <span className="text-gray-400 w-24">{LAYER_LABELS[p.dataKey]}</span>
          <span className="font-mono font-semibold" style={{ color: p.color }}>
            {(p.value * 100).toFixed(1)}%
          </span>
        </div>
      ))}
    </div>
  )
}

interface Props {
  file: string
}

export default function SimilarityView({ file }: Props) {
  const [results, setResults] = useState<SimilarityResult[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [selectedRow, setSelectedRow] = useState<number | null>(null)

  useEffect(() => {
    if (!file) {
      setResults([])
      setError('')
      return
    }
    setLoading(true)
    setError('')
    setResults([])
    setSelectedRow(null)
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
        setLoading(false)
      })
      .catch(e => {
        setError(e.message)
        setLoading(false)
      })
  }, [file])

  // 차트 데이터: 이전 커밋이 있는 행만 (첫 커밋은 비교 대상 없음)
  const chartData = results
    .filter(r => r.prev_sha !== '')
    .map(r => ({
      label: r.sha_short,
      commit: r.sha_short,
      message: r.message.slice(0, 30),
      L1: r.scores.L1,
      L2: r.scores.L2,
      L3: r.scores.L3,
      L4: r.scores.L4,
      changed: r.changed,
    }))

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-4">
      {/* 헤더 */}
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="text-sm font-semibold text-white flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-violet-400" />
            유사도 추이 차트
            {results.length > 0 && (
              <span className="text-xs font-normal text-gray-500">{results.filter(r=>r.prev_sha!=='').length}개 비교점</span>
            )}
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">낮을수록 큰 변경 · 1.0 = 완전 동일</p>
        </div>
        {/* 레이어 범례 */}
        <div className="flex gap-3 shrink-0">
          {(['L1','L2','L3','L4'] as const).map(layer => (
            <div key={layer} className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 rounded-full inline-block" style={{ background: LAYER_COLORS[layer] }} />
              <span className="text-xs text-gray-400">{layer}</span>
            </div>
          ))}
        </div>
      </div>

      {loading && (
        <div className="flex items-center justify-center h-40 text-gray-500 text-sm gap-2">
          <div className="w-4 h-4 border-2 border-gray-700 border-t-violet-500 rounded-full animate-spin" />
          유사도 계산 중…
        </div>
      )}

      {error && (
        <div className="text-red-400 text-sm p-3 bg-red-500/10 border border-red-500/20 rounded-lg">
          오류: {error}
        </div>
      )}

      {!loading && !error && results.length > 0 && (
        <>
          {/* 라인 차트 */}
          {chartData.length > 0 ? (
            <div className="bg-gray-950/50 rounded-lg p-4 border border-gray-800">
              <ResponsiveContainer width="100%" height={260}>
                <LineChart data={chartData} margin={{ top: 4, right: 16, left: -10, bottom: 4 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                  <XAxis
                    dataKey="label"
                    tick={{ fill: '#9ca3af', fontSize: 11 }}
                    tickLine={{ stroke: '#374151' }}
                    axisLine={{ stroke: '#374151' }}
                  />
                  <YAxis
                    domain={[0, 1]}
                    tickFormatter={v => `${Math.round(v * 100)}%`}
                    tick={{ fill: '#9ca3af', fontSize: 11 }}
                    tickLine={{ stroke: '#374151' }}
                    axisLine={{ stroke: '#374151' }}
                  />
                  <ReferenceLine y={1} stroke="#374151" strokeDasharray="4 2" />
                  <Tooltip content={<CustomTooltip />} />
                  <Legend formatter={(value) => (
                    <span style={{ color: '#9ca3af', fontSize: 11 }}>
                      {LAYER_LABELS[value as keyof typeof LAYER_LABELS] ?? value}
                    </span>
                  )} />
                  {(['L1','L2','L3','L4'] as const).map(layer => (
                    <Line key={layer} type="monotone" dataKey={layer}
                      stroke={LAYER_COLORS[layer]} strokeWidth={2}
                      dot={{ r: 3, fill: LAYER_COLORS[layer], strokeWidth: 0 }}
                      activeDot={{ r: 5 }}
                    />
                  ))}
                </LineChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <p className="text-xs text-gray-500 text-center py-6">비교 가능한 커밋 쌍이 없습니다.</p>
          )}

          {/* 커밋별 테이블 (접기 가능) */}
          <details className="rounded-lg border border-gray-800">
            <summary className="text-xs text-gray-500 font-medium px-4 py-2.5 cursor-pointer select-none hover:text-gray-400 bg-gray-800/30 rounded-lg">
              커밋별 상세 수치 테이블 ({results.length}개)
            </summary>
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="bg-gray-800/60 border-b border-gray-700">
                    <th className="text-left px-3 py-2 text-gray-400 font-medium w-20">커밋</th>
                    <th className="text-left px-3 py-2 text-gray-400 font-medium">메시지</th>
                    <th className="text-center px-3 py-2 text-gray-400 font-medium w-16">변경</th>
                    <th className="text-center px-3 py-2 font-semibold w-16" style={{ color: LAYER_COLORS.L1 }}>L1</th>
                    <th className="text-center px-3 py-2 font-semibold w-16" style={{ color: LAYER_COLORS.L2 }}>L2</th>
                    <th className="text-center px-3 py-2 font-semibold w-16" style={{ color: LAYER_COLORS.L3 }}>L3</th>
                    <th className="text-center px-3 py-2 font-semibold w-16" style={{ color: LAYER_COLORS.L4 }}>L4</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((r, i) => {
                    const isFirst = r.prev_sha === ''
                    const isOpen = selectedRow === i
                    return (
                      <>
                        <tr
                          key={r.sha}
                          className={`border-b border-gray-800/60 cursor-pointer transition-colors
                            ${isOpen ? 'bg-gray-800/40' : 'hover:bg-gray-800/20'}`}
                          onClick={() => setSelectedRow(isOpen ? null : i)}
                        >
                          <td className="px-3 py-2"><span className="font-mono text-blue-400">{r.sha_short}</span></td>
                          <td className="px-3 py-2 text-gray-300 max-w-xs truncate">{r.message}</td>
                          <td className="px-3 py-2 text-center">
                            {r.changed ? <span className="text-amber-400">●</span> : <span className="text-gray-700">–</span>}
                          </td>
                          {isFirst ? (
                            <td colSpan={4} className="px-3 py-2 text-center text-gray-600">최초 커밋</td>
                          ) : (
                            (['L1','L2','L3','L4'] as const).map(layer => (
                              <td key={layer} className="px-3 py-2 text-center">
                                <ScoreBadge value={r.scores[layer]} />
                              </td>
                            ))
                          )}
                        </tr>
                        {isOpen && !isFirst && (
                          <tr key={`${r.sha}-d`} className="bg-gray-950/70">
                            <td colSpan={7} className="px-4 py-3">
                              <div className="grid grid-cols-2 lg:grid-cols-4 gap-2">
                                {(['L1','L2','L3','L4'] as const).map(layer => (
                                  <div key={layer} className="rounded-lg border p-2"
                                    style={{ borderColor: LAYER_COLORS[layer]+'33', background: LAYER_COLORS[layer]+'08' }}>
                                    <div className="flex items-center justify-between mb-1">
                                      <span className="text-xs font-semibold" style={{ color: LAYER_COLORS[layer] }}>
                                        {LAYER_LABELS[layer]}
                                      </span>
                                      <span className="text-sm font-bold font-mono" style={{ color: LAYER_COLORS[layer] }}>
                                        {(r.scores[layer]*100).toFixed(1)}%
                                      </span>
                                    </div>
                                    <div className="w-full h-1.5 bg-gray-800 rounded-full overflow-hidden">
                                      <div className="h-full rounded-full" style={{ width: `${r.scores[layer]*100}%`, background: LAYER_COLORS[layer] }} />
                                    </div>
                                    <p className="text-xs text-gray-600 mt-1 leading-tight">{LAYER_DESC[layer]}</p>
                                  </div>
                                ))}
                              </div>
                            </td>
                          </tr>
                        )}
                      </>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </details>
        </>
      )}

      {!loading && !error && results.length === 0 && file && (
        <p className="text-gray-500 text-sm text-center py-8">
          '{file}' 에 해당하는 커밋 히스토리가 없습니다.
        </p>
      )}

      {!loading && !error && !file && (
        <p className="text-gray-500 text-sm text-center py-8">파일을 선택해주세요.</p>
      )}
    </div>
  )
}
