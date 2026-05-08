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
import OverviewExtras from './components/OverviewExtras'

type Tab = 'overview' | 'similarity' | 'commits' | 'events'

const TABS: { id: Tab; label: string; icon: string }[] = [
  { id: 'overview', label: '개요', icon: '◈' },
  { id: 'similarity', label: '유사도 분석', icon: '◎' },
  { id: 'commits', label: '커밋', icon: '⊙' },
  { id: 'events', label: '이벤트 로그', icon: '≡' },
]

const DEMO_UI = import.meta.env.VITE_SCREENSHOT_BRANCH === '1'

export default function App() {
  const [data, setData] = useState<DashboardData | null>(null)
  const [connected, setConnected] = useState(false)
  const [lastUpdated, setLastUpdated] = useState('')
  const [tab, setTab] = useState<Tab>('overview')
  const [similarityFile, setSimilarityFile] = useState(
    'src/main/java/com/backend/domain/user/service/UserService.java',
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
      } catch {
        /* ignore */
      }
    }
    es.onerror = () => {
      setConnected(false)
      setTimeout(() => {
        es.close()
        esRef.current = null
      }, 3000)
    }
    return () => es.close()
  }, [])

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900 flex flex-col">
      <header className="border-b border-slate-200 bg-white px-6 py-3 flex items-center justify-between shrink-0 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-sky-500 to-indigo-600 flex items-center justify-center text-white text-sm font-bold shadow-md">
            C
          </div>
          <div>
            <h1 className="text-sm font-semibold text-slate-900 leading-none">Cline Metrics</h1>
            <p className="text-slate-500 text-[11px] mt-0.5 flex items-center gap-2 flex-wrap">
              Dev Agent 모니터링
              {DEMO_UI && (
                <span className="px-2 py-0.5 rounded-md bg-amber-100 text-amber-900 text-[10px] font-semibold border border-amber-200">
                  스크린샷 픽스처 모드
                </span>
              )}
            </p>
          </div>
        </div>

        <nav className="flex items-center gap-1">
          {TABS.map(t => (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors
                ${tab === t.id
                  ? 'bg-slate-200 text-slate-900 shadow-sm'
                  : 'text-slate-500 hover:text-slate-800 hover:bg-slate-50'}`}
            >
              <span className="opacity-70">{t.icon}</span>
              {t.label}
            </button>
          ))}
        </nav>

        <div className="flex items-center gap-3">
          {lastUpdated && (
            <span className="text-slate-400 text-xs hidden lg:block">
              갱신 {lastUpdated}
            </span>
          )}
          <div className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs border ${
            connected
              ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
              : 'bg-rose-50 text-rose-800 border-rose-200'
          }`}>
            <span className={`w-1.5 h-1.5 rounded-full ${connected ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`} />
            {connected ? 'LIVE' : '끊김'}
          </div>
        </div>
      </header>

      <main className="flex-1 p-5 overflow-auto pb-10">
        {!data ? (
          <div className="flex flex-col items-center justify-center min-h-[50vh] gap-3 text-slate-500">
            <div className="w-8 h-8 border-2 border-slate-200 border-t-sky-500 rounded-full animate-spin" />
            <p className="text-sm font-medium text-slate-600">데이터 불러오는 중…</p>
          </div>
        ) : (
          <>
            {tab === 'overview' && (
              <div className="space-y-5 max-w-[1600px] mx-auto w-full">
                <SummaryCards summary={data.summary} />
                <div className="grid grid-cols-1 xl:grid-cols-12 gap-5 items-stretch min-h-[520px]">
                  <div className="xl:col-span-7 flex flex-col min-h-[520px]">
                    <div className="flex-1 min-h-0">
                      <TaskTable tasks={data.tasks} />
                    </div>
                  </div>
                  <div className="xl:col-span-5 flex flex-col min-h-[520px]">
                    <div className="flex-1 min-h-[520px] flex flex-col">
                      <EventChart counts={data.counts} dense />
                    </div>
                  </div>
                </div>
                <OverviewExtras />
              </div>
            )}

            {tab === 'similarity' && (
              <div className="space-y-4 max-w-7xl mx-auto">
                <div className="flex flex-wrap items-center gap-3 bg-white border border-slate-200 rounded-xl px-4 py-3 shadow-sm">
                  <div className="flex rounded-lg overflow-hidden border border-slate-200 shrink-0">
                    {([
                      { id: 'file' as const, label: '파일별 분석' },
                      { id: 'project' as const, label: '프로젝트 전체' },
                    ]).map(m => (
                      <button key={m.id} onClick={() => setSimMode(m.id)}
                        className={`px-3 py-1.5 text-xs font-medium transition-colors ${simMode === m.id ? 'bg-slate-700 text-white' : 'text-slate-600 hover:bg-slate-50'}`}>
                        {m.label}
                      </button>
                    ))}
                  </div>

                  {simMode === 'file' && (
                    <>
                      <span className="text-xs text-slate-500 shrink-0 font-medium">파일</span>
                      <div className="flex-1 min-w-[200px]">
                        <RepoTreePicker value={similarityFile} onSelect={setSimilarityFile} />
                      </div>
                    </>
                  )}
                  {simMode === 'project' && (
                    <p className="text-xs text-slate-600">
                      모든 소스 파일의 변화율을 커밋 단위로 집계합니다 · 스크린샷 모드에선 진입 시 바로 픽스처를 불러옵니다.
                    </p>
                  )}
                </div>

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
                    <div className="flex flex-col items-center justify-center min-h-[200px] gap-2 text-slate-500 bg-white border border-slate-200 rounded-xl shadow-sm">
                      <span className="text-2xl opacity-60">◎</span>
                      <p className="text-sm font-medium text-slate-600">파일을 선택하면 유사도 분석이 시작됩니다</p>
                    </div>
                  )
                )}

                {simMode === 'project' && <SimilarityProjectView />}
              </div>
            )}

            {tab === 'commits' && (
              <div className="max-w-7xl mx-auto">
                <CommitView />
              </div>
            )}

            {tab === 'events' && (
              <div className="max-w-[1600px] mx-auto w-full grid grid-cols-1 xl:grid-cols-2 gap-5 items-start xl:items-stretch">
                <div className="min-w-0 xl:h-full xl:flex xl:flex-col">
                  <EventLog events={data.events} fillColumn />
                </div>
                <div className="min-w-0 flex flex-col gap-4 xl:min-h-[min(680px,78vh)] xl:max-h-[85vh] xl:overflow-y-auto">
                  <div className="flex flex-wrap items-center gap-3 bg-white border border-slate-200 rounded-xl px-4 py-3 shadow-sm shrink-0">
                    <span className="text-xs text-slate-500 font-medium shrink-0">유사도 · 파일</span>
                    <div className="flex-1 min-w-[180px]">
                      <RepoTreePicker value={similarityFile} onSelect={setSimilarityFile} />
                    </div>
                  </div>
                  {similarityFile ? (
                    <div className="space-y-4 min-w-0 pb-2">
                      <SimilarityStepView file={similarityFile} />
                      <SimilarityView file={similarityFile} />
                      <SimilarityFirstLast file={similarityFile} />
                    </div>
                  ) : (
                    <div className="flex flex-col items-center justify-center min-h-[200px] gap-2 text-slate-500 bg-white border border-slate-200 rounded-xl shadow-sm">
                      <span className="text-2xl opacity-60">◎</span>
                      <p className="text-sm font-medium text-slate-600">파일을 선택하면 우측에 유사도가 표시됩니다</p>
                    </div>
                  )}
                </div>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  )
}
