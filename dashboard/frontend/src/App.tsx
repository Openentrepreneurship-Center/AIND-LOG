import { useState, useEffect, useRef, useCallback } from 'react'
import { DashboardData, ProjectFirstLastSimilarity, ProjectSimilarityResult, ProjectFromFirstItem, Commit } from './types'
// ProjectFirstLastSimilarity re-exported as PFLType in ProjectFirstLastPanel
import { MOCK_DATA } from './mockData'
import SummaryCards from './components/SummaryCards'
import TaskTable from './components/TaskTable'
import EventLog from './components/EventLog'
import EventChart from './components/EventChart'
import CommitView from './components/CommitView'
import SimilarityProjectView from './components/SimilarityProjectView'
import SimilarityFromFirstView from './components/SimilarityFromFirstView'
import CancelFollowupsPanel from './components/CancelFollowupsPanel'
import PeriodSelector, { PeriodState, periodToRange } from './components/PeriodSelector'
import { ProjectFirstLastSimilarity as PFLType } from './types'

function ProjectFirstLastPanel({ data, loading }: { data?: PFLType; loading: boolean }) {
  const LAYER_COLORS = { L1: '#60a5fa', L2: '#34d399', L3: '#fbbf24', L4: '#c084fc' }
  const LAYER_LABELS = { L1: 'L1 Levenshtein', L2: 'L2 BLEU', L3: 'L3 구조적', L4: 'L4 의미론적' }
  const LAYERS = ['L1', 'L2', 'L3', 'L4'] as const
  const hasData = data && data.file_count > 0
  const avg = hasData ? Math.round(((data!.L1 + data!.L2 + data!.L3 + data!.L4) / 4) * 100) : null
  const gradeColor = (v: number) => v >= 80 ? '#34d399' : v >= 55 ? '#60a5fa' : v >= 30 ? '#fbbf24' : '#f87171'
  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
      <div className="mb-4">
        <h3 className="text-sm font-semibold text-white">첫 커밋 ↔ 마지막 커밋 직접 비교</h3>
        <p className="text-xs text-gray-500 mt-0.5">
          최초 유의미 커밋과 최신 커밋의 코드를 L1~L4로 직접 비교 — 신규 추가 파일은 0점으로 반영
        </p>
      </div>
      {loading ? (
        <p className="text-gray-600 text-sm">계산 중…</p>
      ) : hasData && data ? (
        <div className="space-y-4">
          <div className="flex items-center gap-6 flex-wrap">
            <div>
              <p className="text-xs text-gray-500 mb-0.5">평균 유사도</p>
              <p className="text-3xl font-bold font-mono" style={{ color: avg !== null ? gradeColor(avg) : '#9ca3af' }}>
                {avg}%
              </p>
            </div>
            <div className="text-xs text-gray-600 space-y-1">
              <p>{data.first_sha_short} <span className="text-gray-700">→</span> {data.last_sha_short}</p>
              <p>
                전체 {data.file_count}개 소스파일
                {data.new_file_count != null && data.new_file_count > 0 &&
                  <span className="text-amber-700/80 ml-1">(신규 {data.new_file_count}개 0점)</span>
                }
                {' '}· 총 {data.total_commits}개 커밋
              </p>
            </div>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {LAYERS.map(k => {
              const pct = Math.round(data[k] * 100)
              return (
                <div key={k} className="bg-gray-800/50 rounded-lg p-3 border border-gray-700/50">
                  <p className="text-[10px] text-gray-500 mb-1">{LAYER_LABELS[k]}</p>
                  <p className="text-xl font-bold font-mono" style={{ color: LAYER_COLORS[k] }}>{pct}%</p>
                  <div className="mt-2 h-1.5 bg-gray-700 rounded-full overflow-hidden">
                    <div className="h-full rounded-full" style={{ width: `${pct}%`, background: LAYER_COLORS[k] }} />
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      ) : (
        <p className="text-gray-600 text-sm">{data && data.file_count === 0 ? '공통 소스 파일 없음 (첫 커밋이 현재와 완전히 다른 파일 세트)' : '백엔드 연결 시 측정됩니다'}</p>
      )}
    </div>
  )
}

type Tab = 'overview' | 'similarity' | 'commits' | 'events'

const TABS: { id: Tab; label: string; icon: string }[] = [
  { id: 'overview',   label: '개요',   icon: '◈' },
  { id: 'similarity', label: 'AI코드 Quality', icon: '◎' },
  { id: 'commits',    label: '커밋',   icon: '⊙' },
  { id: 'events',     label: '이벤트 로그', icon: '≡' },
]

export default function App() {
  const [data, setData] = useState<DashboardData | null>(null)
  const [connected, setConnected] = useState(false)
  const [isMock, setIsMock] = useState(false)
  const [lastUpdated, setLastUpdated] = useState('')
  const [tab, setTab] = useState<Tab>('overview')
  const [isDark, setIsDark] = useState(true)
  const [projectFirstLast, setProjectFirstLast] = useState<ProjectFirstLastSimilarity | null>(null)
  const [projectFirstLastLoading, setProjectFirstLastLoading] = useState(false)
  const [simProjectData, setSimProjectData] = useState<ProjectSimilarityResult[] | null>(null)
  const [simFromFirstData, setSimFromFirstData] = useState<ProjectFromFirstItem[] | null>(null)
  const [simFromFirstLoading, setSimFromFirstLoading] = useState(true)
  const [period, setPeriod] = useState<PeriodState>({ unit: 'all', anchor: new Date() })
  const [commits, setCommits] = useState<Commit[] | null>(null)
  const esRef = useRef<EventSource | null>(null)

  const toggleTheme = useCallback(() => {
    setIsDark(prev => {
      const next = !prev
      document.documentElement.classList.toggle('light', !next)
      return next
    })
  }, [])

  // VITE_API_BASE_URL 환경변수로 백엔드 주소 지정 가능 (Vercel 등 외부 배포 시)
  const API_BASE = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''

  // 기간 변경 시 API 재호출
  useEffect(() => {
    const { start_ts, end_ts } = periodToRange(period)
    const params = new URLSearchParams()
    if (start_ts !== null) params.set('start_ts', String(start_ts))
    if (end_ts !== null)   params.set('end_ts',   String(end_ts))
    const qs = params.toString() ? `?${params.toString()}` : ''

    const ctrl = new AbortController()
    const timer = setTimeout(() => ctrl.abort(), 8000) // 8초 타임아웃
    fetch(`${API_BASE}/api/data${qs}`, { signal: ctrl.signal })
      .then(r => r.ok ? r.json() : Promise.reject(r.status))
      .then((d: DashboardData) => {
        clearTimeout(timer)
        setData(d)
        setIsMock(false)
        setLastUpdated(new Date().toLocaleTimeString('ko-KR'))
      })
      .catch(() => {
        clearTimeout(timer)
        setData(MOCK_DATA)
        setIsMock(true)
      })
  }, [API_BASE, period])

  // 앱 로딩 시 similarity 관련 데이터 일괄 prefetch (탭 전환 시 재계산 없음)
  useEffect(() => {
    setProjectFirstLastLoading(true)
    // 프로젝트 단계별 유사도 (연속 커밋 비교)
    fetch(`${API_BASE}/api/similarity/project`)
      .then(r => r.ok ? r.json() : Promise.reject(r.status))
      .then((d: ProjectSimilarityResult[]) => setSimProjectData(d))
      .catch(async () => {
        const m = await import('./mockData')
        setSimProjectData(m.MOCK_PROJECT_SIMILARITY)
      })
    // 첫 커밋 기준 단계별 유사도 (from-first 그래프)
    // → 마지막 항목에서 project-first-last 값도 파생 (단일 계산으로 통일)
    setSimFromFirstLoading(true)
    fetch(`${API_BASE}/api/similarity/project-from-first`)
      .then(r => r.ok ? r.json() : Promise.reject(r.status))
      .then((d: ProjectFromFirstItem[]) => {
        setSimFromFirstData(d)
        setSimFromFirstLoading(false)
        // 마지막 커밋 항목으로 첫↔마지막 요약 카드 값 통일
        if (d.length > 0) {
          const last = d[d.length - 1]
          const first = d[0]
          setProjectFirstLast({
            L1: last.scores.L1,
            L2: last.scores.L2,
            L3: last.scores.L3,
            L4: last.scores.L4,
            file_count: last.file_count,
            new_file_count: (last as any).new_file_count ?? 0,
            total_commits: d.length,
            first_sha_short: first.ref_sha_short ?? first.sha_short,
            last_sha_short: last.sha_short,
          })
        }
        setProjectFirstLastLoading(false)
      })
      .catch(async () => {
        const m = await import('./mockData')
        setSimFromFirstData(m.MOCK_PROJECT_FROM_FIRST)
        setProjectFirstLast(m.MOCK_PROJECT_FIRST_LAST)
        setProjectFirstLastLoading(false)
        setSimFromFirstLoading(false)
      })
  }, [API_BASE])

  // 커밋 데이터 한 번만 fetch (탭 전환 시 재로딩 방지)
  useEffect(() => {
    fetch(`${API_BASE}/api/commits`)
      .then(r => r.ok ? r.json() : Promise.reject(r.status))
      .then((d: Commit[]) => setCommits(d))
      .catch(async () => {
        const m = await import('./mockData')
        setCommits(m.MOCK_COMMITS)
      })
  }, [API_BASE])

  // SSE 실시간 업데이트 — 기간 필터 적용 중일 때는 SSE 덮어쓰기 무시
  const periodRef = useRef(period)
  useEffect(() => { periodRef.current = period }, [period])

  useEffect(() => {
    const es = new EventSource(`${API_BASE}/api/stream`)
    esRef.current = es
    es.onopen = () => setConnected(true)
    es.onmessage = (e: MessageEvent) => {
      try {
        // 기간 필터 활성 중이면 SSE 데이터로 덮어쓰지 않음
        if (periodRef.current.unit !== 'all') return
        setData(JSON.parse(e.data) as DashboardData)
        setIsMock(false)
        setLastUpdated(new Date().toLocaleTimeString('ko-KR'))
      } catch { /* ignore */ }
    }
    es.onerror = () => {
      setConnected(false)
      setTimeout(() => { es.close(); esRef.current = null }, 3000)
    }
    return () => es.close()
  }, [API_BASE])

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100 flex flex-col">
      {/* ── Header ── */}
      <header className="border-b border-gray-800 px-6 py-3 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center text-white text-xs font-bold">
            C
          </div>
          <div>
            <h1 className="text-sm font-semibold text-white leading-none">Cline Metrics</h1>
            <p className="text-gray-600 text-[11px] mt-0.5">Dev Agent 모니터링</p>
          </div>
        </div>

        {/* Tab nav */}
        <nav className="flex items-center gap-1">
          {TABS.map(t => (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors
                ${tab === t.id
                  ? 'bg-gray-800 text-white'
                  : 'text-gray-500 hover:text-gray-300 hover:bg-gray-900'}`}
            >
              <span className="opacity-60">{t.icon}</span>
              {t.label}
            </button>
          ))}
        </nav>

        <div className="flex items-center gap-3">
          {lastUpdated && (
            <span className="text-gray-600 text-xs hidden lg:block">갱신 {lastUpdated}</span>
          )}

          {/* 테마 토글 */}
          <button
            onClick={toggleTheme}
            className="flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs border border-gray-700 text-gray-400 hover:text-white hover:border-gray-500 transition-colors"
            title={isDark ? '라이트 모드로 전환' : '다크 모드로 전환'}
          >
            {isDark ? '☀' : '🌙'} {isDark ? 'Light' : 'Dark'}
          </button>

          <div className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs ${
            connected
              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
              : 'bg-red-500/10 text-red-400 border border-red-500/20'
          }`}>
            <span className={`w-1.5 h-1.5 rounded-full ${connected ? 'bg-emerald-400 animate-pulse' : 'bg-red-400'}`} />
            {connected ? 'LIVE' : '끊김'}
          </div>
        </div>
      </header>

      {/* ── 샘플 데이터 배너 ── */}
      {isMock && (
        <div className="bg-amber-500/10 border-b border-amber-500/30 px-6 py-2 flex items-center gap-2 text-xs text-amber-400 shrink-0">
          <span className="font-bold">⚠ 샘플 데이터 표시 중</span>
          <span className="text-amber-500/70">— 백엔드 서버에 연결할 수 없습니다. 실제 데이터를 보려면 FastAPI 서버를 실행하고 <code className="text-amber-300">VITE_API_BASE_URL</code>을 설정하세요.</span>
        </div>
      )}

      {/* ── 기간 선택 바 ── */}
      <div className={`border-b px-6 py-3 flex items-center justify-between shrink-0 ${
        isDark ? 'border-gray-800 bg-gray-950/60' : 'border-gray-200 bg-gray-50/60'
      }`}>
        <div className="flex items-center gap-3">
          <PeriodSelector value={period} onChange={setPeriod} isDark={isDark} />
          {period.unit !== 'all' && (
            <span className={`text-xs ${isDark ? 'text-gray-600' : 'text-gray-400'}`}>
              이벤트 기반 지표 필터 적용 중 · 유사도 차트는 전체 기간 기준
            </span>
          )}
        </div>
        {period.unit !== 'all' && (() => {
          const { start_ts, end_ts } = periodToRange(period)
          const fmt = (ts: number) => new Date(ts).toLocaleDateString('ko-KR', { month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' })
          return (
            <span className={`text-xs font-mono ${isDark ? 'text-gray-700' : 'text-gray-400'}`}>
              {start_ts && end_ts ? `${fmt(start_ts)} ~ ${fmt(end_ts)}` : ''}
            </span>
          )
        })()}
      </div>

      {/* ── Body ── */}
      <main className="flex-1 p-5 overflow-auto">
        {!data ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3 text-gray-600">
            <div className="w-7 h-7 border-2 border-gray-800 border-t-blue-500 rounded-full animate-spin" />
            <p className="text-sm">로딩 중…</p>
          </div>
        ) : (
          <>
            {/* ── 개요 탭 ── */}
            {tab === 'overview' && (
              <div className="space-y-5 max-w-7xl mx-auto">
                <SummaryCards
                  summary={data.summary}
                  cancelFollowups={data.cancel_followups ?? []}
                  manualApprovalItems={data.manual_approval_items ?? []}
                  humanInteractionItems={data.human_interaction_items ?? []}
                  tasks={data.tasks ?? []}
                  eventTypeCounts={data.counts?.event_types ?? []}
                  projectFirstLast={projectFirstLast ?? undefined}
                  projectFirstLastLoading={projectFirstLastLoading}
                />
                <div className="grid grid-cols-1 xl:grid-cols-3 gap-5">
                  <div className="xl:col-span-2">
                    <TaskTable tasks={data.tasks} />
                  </div>
                  <EventChart counts={data.counts} />
                </div>
                {/* 취소→재프롬프트 분석 (TaskTable 아래) */}
                {(data.cancel_followups ?? []).length > 0 && (
                  <CancelFollowupsPanel rows={data.cancel_followups ?? []} />
                )}
              </div>
            )}

            {/* ── AI코드 Quality 탭 ── */}
            {tab === 'similarity' && (() => {
              const { start_ts, end_ts } = periodToRange(period)
              const filteredFromFirst = simFromFirstData
                ? (start_ts || end_ts
                    ? simFromFirstData.filter(d => {
                        const ts = d.ts_kst ? new Date(d.ts_kst.replace(' ', 'T') + '+09:00').getTime() : null
                        return ts !== null
                          && (start_ts === null || ts >= start_ts)
                          && (end_ts   === null || ts <= end_ts)
                      })
                    : simFromFirstData)
                : undefined
              const filteredProject = simProjectData
                ? (start_ts || end_ts
                    ? simProjectData.filter(d => {
                        const ts = d.ts_kst ? new Date(d.ts_kst.replace(' ', 'T') + '+09:00').getTime() : null
                        return ts !== null
                          && (start_ts === null || ts >= start_ts)
                          && (end_ts   === null || ts <= end_ts)
                      })
                    : simProjectData)
                : undefined
              return (
              <div className="space-y-4 max-w-7xl mx-auto">
                {/* 상단: 초기 코드 유지율 추이 (각 커밋 vs 기준 커밋) */}
                <SimilarityFromFirstView
                  prefetchedData={filteredFromFirst ?? undefined}
                  parentLoading={simFromFirstLoading}
                />
                {/* 중단: 커밋별 코드 변경량 추이 (직전 커밋 vs 현재 커밋) */}
                <SimilarityProjectView prefetchedData={filteredProject ?? undefined} />
                {/* 하단: 첫↔마지막 직접 비교 요약 */}
                <ProjectFirstLastPanel
                  data={projectFirstLast ?? undefined}
                  loading={projectFirstLastLoading}
                />
              </div>
              )
            })()}

            {/* ── 커밋 탭 ── */}
            {tab === 'commits' && (
              <div className="max-w-7xl mx-auto">
                <CommitView prefetchedCommits={commits ?? undefined} />
              </div>
            )}

            {/* ── 이벤트 로그 탭 ── */}
            {tab === 'events' && (
              <div className="max-w-7xl mx-auto">
                <EventLog events={data.events} />
              </div>
            )}
          </>
        )}
      </main>
    </div>
  )
}
