import { useState, useEffect, useRef } from 'react'
import { DashboardData } from './types'
import SummaryCards from './components/SummaryCards'
import TaskTable from './components/TaskTable'
import EventLog from './components/EventLog'
import EventChart from './components/EventChart'
import CommitView from './components/CommitView'

export default function App() {
  const [data, setData] = useState<DashboardData | null>(null)
  const [connected, setConnected] = useState(false)
  const [lastUpdated, setLastUpdated] = useState('')
  const [updateCount, setUpdateCount] = useState(0)
  const esRef = useRef<EventSource | null>(null)

  useEffect(() => {
    const es = new EventSource('/api/stream')
    esRef.current = es

    es.onopen = () => setConnected(true)

    es.onmessage = (e: MessageEvent) => {
      try {
        setData(JSON.parse(e.data) as DashboardData)
        setLastUpdated(new Date().toLocaleTimeString('ko-KR'))
        setUpdateCount((n) => n + 1)
      } catch {
        /* ignore parse errors */
      }
    }

    es.onerror = () => {
      setConnected(false)
      // 3초 후 자동 재연결
      setTimeout(() => {
        es.close()
        esRef.current = null
      }, 3000)
    }

    return () => {
      es.close()
    }
  }, [])

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100">
      {/* Header */}
      <header className="border-b border-gray-800 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center text-white text-sm font-bold">
            C
          </div>
          <div>
            <h1 className="text-base font-semibold text-white leading-none">
              Cline Metrics Dashboard
            </h1>
            <p className="text-gray-500 text-xs mt-0.5">Dev Agent 활동 실시간 모니터링</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          {lastUpdated && (
            <span className="text-gray-500 text-xs">
              마지막 업데이트: <span className="text-gray-300">{lastUpdated}</span>
              {updateCount > 0 && (
                <span className="ml-2 text-gray-600">({updateCount}회 갱신)</span>
              )}
            </span>
          )}
          <div
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium ${
              connected
                ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                : 'bg-red-500/10 text-red-400 border border-red-500/20'
            }`}
          >
            <span
              className={`w-1.5 h-1.5 rounded-full ${
                connected ? 'bg-emerald-400 animate-pulse' : 'bg-red-400'
              }`}
            />
            {connected ? '실시간 연결됨' : '연결 끊김'}
          </div>
        </div>
      </header>

      {/* Content */}
      <main className="p-6 space-y-5">
        {!data ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3 text-gray-500">
            <div className="w-8 h-8 border-2 border-gray-700 border-t-blue-500 rounded-full animate-spin" />
            <p className="text-sm">데이터 로딩 중...</p>
          </div>
        ) : (
          <>
            {/* Summary Cards */}
            <SummaryCards summary={data.summary} />

            {/* Task Table + Chart */}
            <div className="grid grid-cols-1 xl:grid-cols-3 gap-5">
              <div className="xl:col-span-2">
                <TaskTable tasks={data.tasks} />
              </div>
              <div>
                <EventChart counts={data.counts} />
              </div>
            </div>

            {/* Commit View */}
            <CommitView />

            {/* Event Log */}
            <EventLog events={data.events} />
          </>
        )}
      </main>
    </div>
  )
}
