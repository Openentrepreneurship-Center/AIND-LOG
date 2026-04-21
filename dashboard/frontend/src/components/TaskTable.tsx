import { useState } from 'react'
import { Task } from '../types'

const STATUS_STYLE: Record<string, string> = {
  완료됨: 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30',
  취소됨: 'bg-red-500/15 text-red-400 border border-red-500/30',
  재개됨: 'bg-amber-500/15 text-amber-400 border border-amber-500/30',
  진행중: 'bg-blue-500/15 text-blue-400 border border-blue-500/30',
}

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <p className="text-gray-500 text-xs font-medium mb-0.5">{label}</p>
      <p className="text-gray-300 text-xs break-all">{value ?? '-'}</p>
    </div>
  )
}

interface Props { tasks: Task[] }

export default function TaskTable({ tasks }: Props) {
  const [expanded, setExpanded] = useState<string | null>(null)

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800">
      <div className="px-5 py-3 border-b border-gray-800 flex items-center justify-between">
        <h2 className="font-semibold text-white text-sm">작업(Task) 목록</h2>
        <span className="text-gray-500 text-xs">{tasks.length}건</span>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="border-b border-gray-800">
              {['시작시각', '종료시각', '상태', '소요(초)', '코드생성(초)', '테스트(회)', '재개', '첫 요청'].map(h => (
                <th key={h} className="text-left px-4 py-2 text-gray-500 font-medium whitespace-nowrap">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {tasks.map(t => (
              <>
                <tr
                  key={t.taskId}
                  onClick={() => setExpanded(expanded === t.taskId ? null : t.taskId)}
                  className={`border-b border-gray-800/60 cursor-pointer transition-colors ${
                    expanded === t.taskId ? 'bg-gray-800/50' : 'hover:bg-gray-800/30'
                  }`}
                >
                  <td className="px-4 py-2.5 text-gray-300 whitespace-nowrap">{t.start_kst}</td>
                  <td className="px-4 py-2.5 text-gray-400 whitespace-nowrap">{t.end_kst || '-'}</td>
                  <td className="px-4 py-2.5">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_STYLE[t.status] ?? ''}`}>
                      {t.status}
                    </span>
                  </td>
                  <td className="px-4 py-2.5 text-gray-300">{t.duration_sec ?? '-'}</td>
                  <td className="px-4 py-2.5 text-gray-300">{t.time_to_first_code_sec ?? '-'}</td>
                  <td className="px-4 py-2.5">
                    {t.test_runs_count > 0
                      ? <span className="text-violet-400 font-semibold">{t.test_runs_count}회</span>
                      : <span className="text-gray-600">-</span>}
                  </td>
                  <td className="px-4 py-2.5 text-gray-300">{t.resume_count > 0 ? t.resume_count : '-'}</td>
                  <td className="px-4 py-2.5 text-gray-400 max-w-xs truncate">
                    {t.initial_task || t.first_prompt || '-'}
                  </td>
                </tr>

                {expanded === t.taskId && (
                  <tr key={`${t.taskId}-detail`} className="border-b border-gray-800">
                    <td colSpan={8} className="px-6 py-4 bg-gray-800/30">
                      <div className="grid grid-cols-2 lg:grid-cols-3 gap-x-8 gap-y-3">
                        <Row label="Task ID" value={<span className="font-mono">{t.taskId}</span>} />
                        <Row label="모델" value={t.model} />
                        <Row label="이벤트 수" value={`${t.event_count}건`} />
                        <Row label="write / read / exec" value={`${t.write_count} / ${t.read_count} / ${t.exec_count}`} />
                        <Row label="사용 도구 목록" value={t.tools_used.join(', ') || '-'} />
                        <Row label="재개 횟수" value={t.resume_count} />
                        <Row
                          label="테스트 총 소요(초) / 전체 비중"
                          value={`${t.test_total_sec ?? '-'}초 / ${t.test_pct_of_duration != null ? t.test_pct_of_duration + '%' : '-'}`}
                        />
                        <Row label="종료 시각" value={t.end_kst} />
                        <div className="col-span-2 lg:col-span-3">
                          <Row
                            label="파일 경로"
                            value={t.file_paths.length ? t.file_paths.join('\n') : '-'}
                          />
                        </div>
                        {t.last_result && (
                          <div className="col-span-2 lg:col-span-3">
                            <p className="text-gray-500 text-xs font-medium mb-0.5">완료 결과 요약</p>
                            <p className="text-gray-300 text-xs whitespace-pre-wrap">{t.last_result}</p>
                          </div>
                        )}
                      </div>
                    </td>
                  </tr>
                )}
              </>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
