import { useState, useEffect, useRef } from 'react'
import { DashboardData } from './types'
import SummaryCards from './components/SummaryCards'
import TaskTable from './components/TaskTable'
import EventLog from './components/EventLog'
import EventChart from './components/EventChart'
import CommitView from './components/CommitView'
import SimilarityView from './components/SimilarityView'
import SimilarityStepView from './components/SimilarityStepView'
import SimilarityFirstLast from './components/SimilarityFirstLast'
import SimilarityProjectView from './components/SimilarityProjectView'
import RepoTreePicker from './components/RepoTreePicker'

type Tab = 'overview' | 'similarity' | 'commits' | 'events'

const TABS: { id: Tab; label: string; icon: string }[] = [
  { id: 'overview',   label: '개요',   icon: '◈' },
  { id: 'similarity', label: '유사도 분석', icon: '◎' },
  { id: 'commits',    label: '커밋',   icon: '⊙' },
  { id: 'events',     label: '이벤트 로그', icon: '≡' },
]

export default function App() {
  const [data, setData] = useState<DashboardData | null>(null)
  const [connected, setConnected] = useState(false)
  const [lastUpdated, setLastUpdated] = useState('')
  const [tab, setTab] = useState<Tab>('overview')
  const [similarityFile, setSimilarityFile] = useState(
    'src/main/java/com/backend/domain/user/service/UserService.java'
  )
  const [simMode, setSimMode] = useState<'file' | 'project'>('file')
  const esRef = useRef<EventSource | null>(null)

  useEffect(() => {
    const es = new EventSource('/api/stream')
    esRef.current = es
    es.onopen = () => setConnected(true)
    es.onmessage = (e: MessageEvent) => {
      try {
        setData(JSON.parse(e.data) as DashboardData)
        setLastUpdated(new Date().toLocaleTimeString('ko-KR'))
      } catch { /* ignore */ }
    }
    es.onerror = () => {
      setConnected(false)
      setTimeout(() => { es.close(); esRef.current = null }, 3000)
    }
    return () => es.close()
  }, [])

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
                <SummaryCards summary={data.summary} />
                <div className="grid grid-cols-1 xl:grid-cols-3 gap-5">
                  <div className="xl:col-span-2">
                    <TaskTable tasks={data.tasks} />
                  </div>
                  <EventChart counts={data.counts} />
                </div>
              </div>
            )}

            {/* ── 유사도 분석 탭 ── */}
            {tab === 'similarity' && (
              <div className="space-y-4 max-w-7xl mx-auto">
                {/* 모드 전환 바 */}
                <div className="flex items-center gap-3 bg-gray-900 border border-gray-800 rounded-xl px-4 py-3">
                  {/* 파일별 / 프로젝트 토글 */}
                  <div className="flex rounded-lg overflow-hidden border border-gray-700 shrink-0">
                    {([
                      { id: 'file',    label: '파일별 분석' },
                      { id: 'project', label: '프로젝트 전체' },
                    ] as const).map(m => (
                      <button key={m.id} onClick={() => setSimMode(m.id)}
                        className={`px-3 py-1.5 text-xs font-medium transition-colors ${
                          simMode === m.id
                            ? 'bg-gray-700 text-white'
                            : 'text-gray-500 hover:text-gray-300'
                        }`}>
                        {m.label}
                      </button>
                    ))}
                  </div>

                  {/* 파일 선택기 (파일별 모드만) */}
                  {simMode === 'file' && (
                    <>
                      <span className="text-xs text-gray-600 shrink-0">파일</span>
                      <div className="flex-1">
                        <RepoTreePicker value={similarityFile} onSelect={setSimilarityFile} />
                      </div>
                    </>
                  )}
                  {simMode === 'project' && (
                    <p className="text-xs text-gray-600">
                      모든 소스 파일의 변화율을 커밋 단위로 집계합니다
                    </p>
                  )}
                </div>

                {/* 파일별 모드 */}
                {simMode === 'file' && (
                  similarityFile ? (
                    <div className="space-y-4">
                      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
                        <SimilarityStepView file={similarityFile} />
                        <SimilarityView file={similarityFile} />
                      </div>
                      <SimilarityFirstLast file={similarityFile} />
                    </div>
                  ) : (
                    <div className="flex flex-col items-center justify-center h-48 gap-2 text-gray-600 bg-gray-900 border border-gray-800 rounded-xl">
                      <span className="text-2xl">◎</span>
                      <p className="text-sm">위에서 파일을 선택하면 유사도 분석이 시작됩니다</p>
                    </div>
                  )
                )}

                {/* 프로젝트 전체 모드 */}
                {simMode === 'project' && <SimilarityProjectView />}
              </div>
            )}

            {/* ── 커밋 탭 ── */}
            {tab === 'commits' && (
              <div className="max-w-7xl mx-auto">
                <CommitView />
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
