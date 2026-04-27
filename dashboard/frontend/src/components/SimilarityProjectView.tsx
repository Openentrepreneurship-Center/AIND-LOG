import { useState, useEffect } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer, ReferenceLine,
} from 'recharts'
import { ProjectSimilarityResult, SimilarityScores } from '../types'
import Modal from './Modal'

const LAYERS = ['L1', 'L2', 'L3', 'L4'] as const
type Layer = typeof LAYERS[number]

const LAYER_COLORS: Record<Layer, string> = {
  L1: '#60a5fa', L2: '#34d399', L3: '#fbbf24', L4: '#c084fc',
}
const LAYER_LABELS: Record<Layer, string> = {
  L1: 'L1 Levenshtein', L2: 'L2 BLEU', L3: 'L3 구조적', L4: 'L4 의미론적',
}

function grade(v: number) {
  if (v >= 0.95) return { label: '거의 동일', color: '#34d399' }
  if (v >= 0.80) return { label: '유사함',   color: '#60a5fa' }
  if (v >= 0.55) return { label: '부분 변경', color: '#fbbf24' }
  if (v >= 0.25) return { label: '많이 변경', color: '#f97316' }
  return               { label: '대폭 변경', color: '#f87171' }
}

function GaugeRing({ value, color, size = 48 }: { value: number; color: string; size?: number }) {
  const r = (size - 8) / 2
  const circ = 2 * Math.PI * r
  return (
    <svg width={size} height={size} className="shrink-0">
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke="#1f2937" strokeWidth={6} />
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke={color} strokeWidth={6}
        strokeDasharray={circ} strokeDashoffset={circ*(1-value)} strokeLinecap="round"
        transform={`rotate(-90 ${size/2} ${size/2})`}
        style={{ transition: 'stroke-dashoffset 0.5s ease' }}
      />
      <text x={size/2} y={size/2+1} textAnchor="middle" dominantBaseline="middle"
        fill={color} fontSize={10} fontWeight="700" fontFamily="monospace">
        {Math.round(value*100)}%
      </text>
    </svg>
  )
}

function avgScores(s: SimilarityScores) {
  return (s.L1 + s.L2 + s.L3 + s.L4) / 4
}

function ScoreBadge({ value }: { value: number }) {
  const g = grade(value)
  return (
    <span className="text-xs font-mono font-bold" style={{ color: g.color }}>
      {Math.round(value * 100)}%
    </span>
  )
}

function sizeLabel(n: number) {
  if (n >= 1024 * 1024) return `${(n / 1024 / 1024).toFixed(1)}MB`
  if (n >= 1024) return `${(n / 1024).toFixed(1)}KB`
  return `${n}B`
}

/* ── 커스텀 툴팁 ─── */
function CustomTooltip({ active, payload, label }: { active?: boolean; payload?: { name: string; value: number; color: string }[]; label?: string }) {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg p-3 text-xs shadow-xl">
      <p className="text-gray-400 font-mono mb-2">{label}</p>
      {payload.map(p => (
        <div key={p.name} className="flex items-center gap-2 mb-1">
          <span className="w-2 h-2 rounded-full" style={{ background: p.color }} />
          <span className="text-gray-400">{LAYER_LABELS[p.name as Layer] ?? p.name}</span>
          <span className="font-bold font-mono ml-auto" style={{ color: p.color }}>
            {(p.value * 100).toFixed(1)}%
          </span>
        </div>
      ))}
    </div>
  )
}

/* ── 모달 상세 ─── */
function ProjectCommitDetail({ r }: { r: ProjectSimilarityResult }) {
  const avg = avgScores(r.scores)
  const rawAvg = avgScores(r.raw_scores)
  const g = grade(avg)
  const gRaw = grade(rawAvg)
  return (
    <div className="space-y-4">
      {/* 메타 */}
      <div className="grid grid-cols-2 gap-3 text-xs">
        {r.prev_sha && (
          <div className="bg-gray-800/50 rounded-lg px-3 py-2 col-span-2">
            <p className="text-gray-500 mb-0.5">비교 커밋</p>
            <p className="font-mono text-gray-400">
              <span className="text-gray-500">{r.prev_sha_short}</span>
              <span className="mx-2 text-gray-700">→</span>
              <span className="text-blue-400">{r.sha_short}</span>
            </p>
          </div>
        )}
        <div className="bg-gray-800/50 rounded-lg px-3 py-2">
          <p className="text-gray-500 mb-0.5">변경 파일</p>
          <p className="text-white font-bold">{r.files_changed} / {r.total_files}개 소스파일</p>
        </div>
        <div className="bg-gray-800/50 rounded-lg px-3 py-2">
          <p className="text-gray-500 mb-0.5">변경 크기</p>
          <p className="text-white font-bold">
            {sizeLabel(r.changed_size)} / {sizeLabel(r.total_size)}
            <span className="text-gray-500 text-xs ml-1">
              ({Math.round(r.changed_size / Math.max(r.total_size, 1) * 100)}%)
            </span>
          </p>
        </div>
      </div>

      {/* 두 점수 비교 */}
      <div className="grid grid-cols-2 gap-3">
        <div className="rounded-xl bg-gray-800/40 border border-gray-700/50 p-3">
          <p className="text-xs text-gray-500 mb-2">프로젝트 전체 유사도</p>
          <div className="flex items-center gap-2">
            <GaugeRing value={avg} color={g.color} size={52} />
            <div>
              <p className="text-lg font-black font-mono" style={{ color: g.color }}>{Math.round(avg*100)}%</p>
              <p className="text-xs" style={{ color: g.color }}>{g.label}</p>
              <p className="text-[10px] text-gray-600 mt-0.5">변경 비중 반영</p>
            </div>
          </div>
        </div>
        <div className="rounded-xl bg-gray-800/40 border border-gray-700/50 p-3">
          <p className="text-xs text-gray-500 mb-2">변경 파일만 평균</p>
          <div className="flex items-center gap-2">
            <GaugeRing value={rawAvg} color={gRaw.color} size={52} />
            <div>
              <p className="text-lg font-black font-mono" style={{ color: gRaw.color }}>{Math.round(rawAvg*100)}%</p>
              <p className="text-xs" style={{ color: gRaw.color }}>{gRaw.label}</p>
              <p className="text-[10px] text-gray-600 mt-0.5">변경 파일 기준</p>
            </div>
          </div>
        </div>
      </div>

      {/* L1~L4 상세 */}
      <div className="space-y-2">
        {LAYERS.map(layer => (
          <div key={layer} className="flex items-center gap-3 rounded-lg px-3 py-2"
               style={{ background: `${LAYER_COLORS[layer]}0d`, border: `1px solid ${LAYER_COLORS[layer]}33` }}>
            <span className="text-xs font-semibold w-28" style={{ color: LAYER_COLORS[layer] }}>
              {LAYER_LABELS[layer]}
            </span>
            <div className="flex-1 grid grid-cols-2 gap-2">
              <div>
                <p className="text-[10px] text-gray-600">전체</p>
                <div className="flex items-center gap-2">
                  <div className="flex-1 h-1.5 bg-gray-800 rounded-full overflow-hidden">
                    <div className="h-full rounded-full" style={{ width: `${r.scores[layer]*100}%`, background: LAYER_COLORS[layer] }} />
                  </div>
                  <ScoreBadge value={r.scores[layer]} />
                </div>
              </div>
              <div>
                <p className="text-[10px] text-gray-600">변경파일</p>
                <div className="flex items-center gap-2">
                  <div className="flex-1 h-1.5 bg-gray-800 rounded-full overflow-hidden">
                    <div className="h-full rounded-full opacity-60" style={{ width: `${r.raw_scores[layer]*100}%`, background: LAYER_COLORS[layer] }} />
                  </div>
                  <ScoreBadge value={r.raw_scores[layer]} />
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

/* ── 메인 컴포넌트 ─── */
export default function SimilarityProjectView() {
  const [results, setResults] = useState<ProjectSimilarityResult[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [modalIdx, setModalIdx] = useState<number | null>(null)
  const [scoreMode, setScoreMode] = useState<'project' | 'raw'>('project')
  const [loaded, setLoaded] = useState(false)

  const load = (refresh = false) => {
    setLoading(true); setError('')
    fetch(`/api/similarity/project${refresh ? '?refresh=true' : ''}`)
      .then(async r => {
        if (!r.ok) { const b = await r.json().catch(() => ({})); throw new Error(b.detail ?? `HTTP ${r.status}`) }
        return r.json() as Promise<ProjectSimilarityResult[]>
      })
      .then(data => { setResults(data); setLoading(false); setLoaded(true) })
      .catch(e => { setError(e.message); setLoading(false) })
  }

  useEffect(() => { /* lazy load — 버튼 클릭 시 */ }, [])

  const chartData = results
    .filter(r => r.prev_sha !== '')
    .map(r => ({
      label: r.sha_short,
      message: r.message.slice(0, 28),
      files: r.files_changed,
      L1: scoreMode === 'project' ? r.scores.L1 : r.raw_scores.L1,
      L2: scoreMode === 'project' ? r.scores.L2 : r.raw_scores.L2,
      L3: scoreMode === 'project' ? r.scores.L3 : r.raw_scores.L3,
      L4: scoreMode === 'project' ? r.scores.L4 : r.raw_scores.L4,
    }))

  const selected = modalIdx !== null ? results[modalIdx] : null

  return (
    <>
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-4">
        {/* 헤더 */}
        <div className="flex items-center justify-between flex-wrap gap-3">
          <div>
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-rose-400" />
              프로젝트 전체 유사도
              {results.length > 0 && (
                <span className="text-xs font-normal text-gray-500">{results.length}개 커밋</span>
              )}
            </h2>
            <p className="text-xs text-gray-500 mt-0.5">
              전체 소스 파일 변화율 · 낮을수록 해당 커밋에서 프로젝트가 크게 변경됨
            </p>
          </div>

          <div className="flex items-center gap-2">
            {/* 점수 모드 토글 */}
            {loaded && (
              <div className="flex rounded-lg overflow-hidden border border-gray-700 text-xs">
                {(['project', 'raw'] as const).map(mode => (
                  <button key={mode} onClick={() => setScoreMode(mode)}
                    className={`px-3 py-1.5 transition-colors ${
                      scoreMode === mode ? 'bg-gray-700 text-white' : 'text-gray-500 hover:text-gray-300'
                    }`}>
                    {mode === 'project' ? '전체 가중' : '변경파일만'}
                  </button>
                ))}
              </div>
            )}
            <button
              onClick={() => load(loaded)}
              disabled={loading}
              className="px-3 py-1.5 text-xs rounded-lg bg-rose-500/10 text-rose-400 border border-rose-500/20 hover:bg-rose-500/20 disabled:opacity-50 transition-colors"
            >
              {loading ? '계산 중…' : loaded ? '새로고침' : '분석 시작'}
            </button>
          </div>
        </div>

        {/* 안내 메시지 (미로드) */}
        {!loaded && !loading && !error && (
          <div className="flex flex-col items-center justify-center h-48 gap-3 text-gray-600 bg-gray-800/20 rounded-xl border border-gray-800/50">
            <span className="text-3xl">◎</span>
            <p className="text-sm">분석 시작 버튼을 눌러 프로젝트 전체 유사도를 계산합니다</p>
            <p className="text-xs text-gray-700">커밋 수에 따라 수 초 ~ 수십 초 소요</p>
          </div>
        )}

        {loading && (
          <div className="flex items-center justify-center h-48 gap-3 text-gray-500">
            <div className="w-5 h-5 border-2 border-gray-700 border-t-rose-400 rounded-full animate-spin" />
            <div>
              <p className="text-sm">프로젝트 유사도 계산 중…</p>
              <p className="text-xs text-gray-700 mt-1">모든 커밋의 변경 파일을 L1~L4로 측정합니다</p>
            </div>
          </div>
        )}

        {error && (
          <p className="text-red-400 text-xs p-3 bg-red-500/10 border border-red-500/20 rounded-lg">오류: {error}</p>
        )}

        {!loading && !error && loaded && results.length > 0 && (
          <>
            {/* 추이 차트 */}
            <div className="bg-gray-950/50 rounded-lg p-4 border border-gray-800">
              <div className="flex items-center justify-between mb-2 text-xs text-gray-500">
                <span>커밋별 프로젝트 유사도 추이 ({scoreMode === 'project' ? '전체 가중' : '변경 파일만'})</span>
                <div className="flex gap-3">
                  {LAYERS.map(l => (
                    <span key={l} className="flex items-center gap-1">
                      <span className="w-2.5 h-0.5 rounded-full inline-block" style={{ background: LAYER_COLORS[l] }} />
                      <span>{l}</span>
                    </span>
                  ))}
                </div>
              </div>
              <ResponsiveContainer width="100%" height={240}>
                <LineChart data={chartData} margin={{ top: 4, right: 16, left: -10, bottom: 4 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                  <XAxis dataKey="label" tick={{ fill: '#6b7280', fontSize: 10 }}
                    tickLine={{ stroke: '#374151' }} axisLine={{ stroke: '#374151' }} />
                  <YAxis domain={[0, 1]} tickFormatter={v => `${Math.round(v*100)}%`}
                    tick={{ fill: '#6b7280', fontSize: 10 }}
                    tickLine={{ stroke: '#374151' }} axisLine={{ stroke: '#374151' }} />
                  <ReferenceLine y={1} stroke="#374151" strokeDasharray="4 2" />
                  <Tooltip content={<CustomTooltip />} />
                  <Legend formatter={v => <span style={{ color: '#9ca3af', fontSize: 10 }}>{LAYER_LABELS[v as Layer] ?? v}</span>} />
                  {LAYERS.map(l => (
                    <Line key={l} type="monotone" dataKey={l} stroke={LAYER_COLORS[l]}
                      strokeWidth={1.5} dot={{ r: 2, fill: LAYER_COLORS[l], strokeWidth: 0 }}
                      activeDot={{ r: 4 }} />
                  ))}
                </LineChart>
              </ResponsiveContainer>
            </div>

            {/* 커밋 목록 */}
            <div className="overflow-y-auto max-h-[360px] space-y-0"
                 style={{ scrollbarWidth: 'thin', scrollbarColor: '#374151 transparent' }}>
              <p className="text-xs text-gray-600 mb-2">항목 클릭 → 상세 모달</p>
              {results.map((r, i) => {
                const isFirst = r.prev_sha === ''
                const avg = avgScores(scoreMode === 'project' ? r.scores : r.raw_scores)
                const g = grade(avg)
                const changePct = r.total_size > 0 ? Math.round(r.changed_size / r.total_size * 100) : 0
                return (
                  <div key={r.sha}>
                    <div
                      onClick={() => !isFirst && setModalIdx(i)}
                      className={`flex items-center gap-3 px-2 py-2 rounded-lg transition-colors
                        ${!isFirst ? 'cursor-pointer hover:bg-gray-800/50' : ''}`}
                    >
                      {/* 타임라인 */}
                      <div className={`w-2 h-2 rounded-full shrink-0
                        ${isFirst ? 'bg-emerald-500' : r.files_changed > 0 ? 'bg-amber-400' : 'bg-gray-700'}`} />

                      {/* 커밋 정보 */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-1.5">
                          <span className="font-mono text-xs text-gray-500">{r.sha_short}</span>
                          {isFirst && <span className="text-[10px] px-1.5 rounded bg-emerald-500/15 text-emerald-400 border border-emerald-500/20">시작</span>}
                          {!isFirst && r.files_changed > 0 && (
                            <span className="text-[10px] text-amber-400">{r.files_changed}개 변경</span>
                          )}
                        </div>
                        <p className="text-xs text-gray-400 truncate mt-0.5">{r.message}</p>
                      </div>

                      {/* 변경 비중 바 */}
                      {!isFirst && (
                        <div className="w-16 shrink-0 hidden sm:block">
                          <p className="text-[10px] text-gray-600 mb-0.5">{changePct}% 변경</p>
                          <div className="w-full h-1 bg-gray-800 rounded-full overflow-hidden">
                            <div className="h-full bg-amber-500/60 rounded-full" style={{ width: `${Math.min(changePct, 100)}%` }} />
                          </div>
                        </div>
                      )}

                      {/* 평균 점수 */}
                      {!isFirst && (
                        <div className="shrink-0 flex items-center gap-1">
                          <span className="text-xs font-mono font-bold" style={{ color: g.color }}>
                            {Math.round(avg * 100)}%
                          </span>
                          <div className="w-1.5 h-1.5 rounded-full" style={{ background: g.color }} />
                        </div>
                      )}
                    </div>
                    {i < results.length - 1 && <div className="ml-[7px] w-0.5 h-2 bg-gray-800" />}
                  </div>
                )
              })}
            </div>
          </>
        )}
      </div>

      {/* 상세 모달 */}
      <Modal
        open={modalIdx !== null && selected !== null}
        onClose={() => setModalIdx(null)}
        title={`프로젝트 유사도 · ${selected?.sha_short ?? ''} — ${(selected?.message ?? '').slice(0, 40)}`}
        width="xl"
      >
        {selected && <ProjectCommitDetail r={selected} />}
      </Modal>
    </>
  )
}
