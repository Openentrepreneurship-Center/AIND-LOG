import { useMemo, useState } from 'react'
import { CancelFollowup } from '../types'

interface Props {
  rows: CancelFollowup[]
}

export default function CancelFollowupsPanel({ rows }: Props) {
  const [copied, setCopied] = useState(false)

  const jsonText = useMemo(() => JSON.stringify(rows, null, 2), [rows])

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(jsonText)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch { /* ignore */ }
  }

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800 overflow-hidden">
      <div className="px-5 py-3 border-b border-gray-800 flex items-center justify-between gap-3 flex-wrap">
        <div>
          <h2 className="font-semibold text-white text-sm">취소 직후 사용자 프롬프트</h2>
          <p className="text-gray-500 text-[11px] mt-0.5">
            TaskCancel 이후 같은 Task에서 기록된 첫 UserPromptSubmit. JSON 복사로 외부 분석에 활용하세요.
          </p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <span className="text-gray-600 text-xs">{rows.length}건</span>
          <button
            type="button"
            onClick={handleCopy}
            disabled={rows.length === 0}
            className="text-xs px-3 py-1.5 rounded-lg bg-gray-800 text-gray-200 border border-gray-700
              hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            {copied ? '✓ 복사됨' : 'JSON 복사'}
          </button>
        </div>
      </div>

      {rows.length === 0 ? (
        <div className="px-5 py-8 text-center text-gray-600 text-xs">
          아직 짝 매칭된 데이터가 없습니다.<br />
          TaskCancel 후 동일 Task에서 UserPromptSubmit이 오면 여기에 표시됩니다.
        </div>
      ) : (
        <div className="overflow-x-auto max-h-80 overflow-y-auto">
          <table className="w-full text-xs">
            <thead className="sticky top-0 bg-gray-900 z-10 border-b border-gray-800">
              <tr>
                {['취소줄#', '프롬프트줄#', '간격(초)', '취소 시각', '프롬프트 시각', 'Task ID', '프롬프트 내용'].map(h => (
                  <th key={h} className="text-left px-3 py-2 text-gray-500 font-medium whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((r, i) => (
                <tr key={i} className="border-b border-gray-800/60 align-top hover:bg-gray-800/30">
                  <td className="px-3 py-2 text-gray-400 font-mono">{r.cancel_event_idx}</td>
                  <td className="px-3 py-2 text-gray-400 font-mono">{r.prompt_event_idx}</td>
                  <td className="px-3 py-2 text-gray-300 whitespace-nowrap">
                    {r.gap_sec != null ? r.gap_sec : '—'}
                  </td>
                  <td className="px-3 py-2 text-gray-500 whitespace-nowrap">{r.cancel_ts_kst}</td>
                  <td className="px-3 py-2 text-gray-500 whitespace-nowrap">{r.prompt_ts_kst}</td>
                  <td className="px-3 py-2 text-gray-500 font-mono text-[10px] max-w-[120px] truncate">{r.taskId}</td>
                  <td className="px-3 py-2 text-gray-300 whitespace-pre-wrap min-w-[200px] max-w-md">{r.prompt_text}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
