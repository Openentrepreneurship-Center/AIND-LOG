import { Fragment, useState } from 'react'
import { Task } from '../types'

const STATUS_STYLE: Record<string, string> = {
  완료됨: 'bg-emerald-50 text-emerald-900 border border-emerald-200',
  취소됨: 'bg-red-50 text-red-900 border border-red-200',
  재개됨: 'bg-amber-50 text-amber-950 border border-amber-200',
  진행중: 'bg-sky-50 text-sky-900 border border-sky-200',
}

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <p className="text-slate-500 text-xs font-semibold mb-0.5">{label}</p>
      <p className="text-slate-700 text-xs break-all">{value ?? '-'}</p>
    </div>
  )
}

interface Props {
  tasks: Task[]
}

export default function TaskTable({ tasks }: Props) {
  const [expanded, setExpanded] = useState<string | null>(null)

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm h-full flex flex-col min-h-[520px]">
      <div className="px-5 py-3 border-b border-slate-100 flex items-center justify-between bg-slate-50/70 rounded-t-xl shrink-0">
        <div>
          <h2 className="font-semibold text-slate-900 text-sm">작업(Task) 목록</h2>
          <p className="text-[11px] text-slate-500 mt-0.5">스크린샷용 픽스처 행 포함</p>
        </div>
        <span className="text-slate-600 text-xs font-medium">{tasks.length}건</span>
      </div>
      <div className="overflow-x-auto flex-1 min-h-0">
        <table className="w-full text-xs">
          <thead className="sticky top-0 z-[1] bg-white border-b border-slate-200 shadow-sm">
            <tr>
              {['시작시각', '종료시각', '상태', '소요(초)', '코드생성(초)', '테스트(회)', '재개', '첫 요청'].map(h => (
                <th key={h} className="text-left px-4 py-2 text-slate-600 font-semibold whitespace-nowrap">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {tasks.map(t => (
              <Fragment key={t.taskId}>
                <tr
                  onClick={() => setExpanded(expanded === t.taskId ? null : t.taskId)}
                  className={`border-b border-slate-100 cursor-pointer transition-colors ${
                    expanded === t.taskId ? 'bg-sky-50/80' : 'hover:bg-slate-50'
                  }`}
                >
                  <td className="px-4 py-2.5 text-slate-800 whitespace-nowrap font-medium">{t.start_kst}</td>
                  <td className="px-4 py-2.5 text-slate-600 whitespace-nowrap">{t.end_kst || '-'}</td>
                  <td className="px-4 py-2.5 whitespace-nowrap">
                    <span
                      className={`inline-block px-2 py-0.5 rounded-full text-xs font-semibold whitespace-nowrap ${STATUS_STYLE[t.status] ?? 'bg-slate-100 text-slate-800 border border-slate-200'}`}
                    >
                      {t.status}
                    </span>
                  </td>
                  <td className="px-4 py-2.5 text-slate-800 tabular-nums">{t.duration_sec ?? '-'}</td>
                  <td className="px-4 py-2.5 text-slate-800 tabular-nums">{t.time_to_first_code_sec ?? '-'}</td>
                  <td className="px-4 py-2.5">
                    {t.test_runs_count > 0 ? (
                      <span className="text-violet-700 font-bold">{t.test_runs_count}회</span>
                    ) : (
                      <span className="text-slate-400">-</span>
                    )}
                  </td>
                  <td className="px-4 py-2.5 text-slate-800 tabular-nums">{t.resume_count > 0 ? t.resume_count : '-'}</td>
                  <td className="px-4 py-2.5 text-slate-600 max-w-xs truncate">{t.initial_task || t.first_prompt || '-'}</td>
                </tr>

                {expanded === t.taskId && (
                  <tr className="border-b border-slate-100 bg-slate-50/90">
                    <td colSpan={8} className="px-6 py-4">
                      <div className="grid grid-cols-2 lg:grid-cols-3 gap-x-8 gap-y-3">
                        <Row label="Task ID" value={<span className="font-mono text-xs">{t.taskId}</span>} />
                        <Row label="모델" value={<span className="font-mono text-[11px]">{t.model}</span>} />
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
                          <Row label="파일 경로" value={t.file_paths.length ? t.file_paths.join('\n') : '-'} />
                        </div>
                        {t.last_result && (
                          <div className="col-span-2 lg:col-span-3">
                            <p className="text-slate-500 text-xs font-semibold mb-0.5">완료 결과 요약</p>
                            <p className="text-slate-700 text-xs whitespace-pre-wrap leading-relaxed">{t.last_result}</p>
                          </div>
                        )}
                      </div>
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
