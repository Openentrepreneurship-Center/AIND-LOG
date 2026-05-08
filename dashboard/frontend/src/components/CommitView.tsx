import { useState, useEffect } from 'react'
import { Commit } from '../types'
import Modal from './Modal'

// ── diff parser ────────────────────────────────────────────────────────────
type DiffLine = { type: 'add' | 'del' | 'hunk' | 'meta' | 'ctx'; text: string }

function parseDiff(patch: string): DiffLine[] {
  return patch.split('\n').map(line => {
    if (line.startsWith('+') && !line.startsWith('+++')) return { type: 'add', text: line }
    if (line.startsWith('-') && !line.startsWith('---')) return { type: 'del', text: line }
    if (line.startsWith('@@')) return { type: 'hunk', text: line }
    if (line.startsWith('diff ') || line.startsWith('index ') || line.startsWith('+++') || line.startsWith('---')) return { type: 'meta', text: line }
    return { type: 'ctx', text: line }
  })
}

const DIFF_STYLE: Record<string, string> = {
  add:  'bg-emerald-50 text-emerald-900',
  del:  'bg-rose-50 text-rose-900',
  hunk: 'bg-sky-50 text-sky-900 font-semibold',
  meta: 'text-slate-500',
  ctx:  'text-slate-700',
}

function DiffViewer({ patch }: { patch: string }) {
  const lines = parseDiff(patch)
  const adds = lines.filter(l => l.type === 'add').length
  const dels = lines.filter(l => l.type === 'del').length

  return (
    <div>
      <div className="flex items-center gap-3 mb-2 text-xs">
        <span className="text-emerald-700 font-mono font-semibold">+{adds} 추가</span>
        <span className="text-rose-700 font-mono font-semibold">-{dels} 삭제</span>
      </div>
      <div className="bg-slate-50 rounded-lg overflow-auto max-h-96 border border-slate-200">
        <table className="w-full text-xs font-mono border-collapse">
          <tbody>
            {lines.map((line, i) => (
              <tr key={i} className={`${DIFF_STYLE[line.type]} hover:brightness-[1.02]`}>
                <td className="px-3 py-0.5 select-none w-8 text-slate-500 border-r border-slate-200 text-right">
                  {i + 1}
                </td>
                <td className="px-3 py-0.5 whitespace-pre break-all">
                  {line.text || ' '}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

// ── snapshot viewer ────────────────────────────────────────────────────────
function SnapshotViewer({ files }: { files: Commit['snapshot_files'] }) {
  const [active, setActive] = useState(files[0]?.path ?? '')
  const current = files.find(f => f.path === active)

  if (!files.length) return <p className="text-slate-500 text-sm">스냅샷 파일 없음</p>

  return (
    <div className="space-y-2">
      {/* 파일 탭 */}
      <div className="flex flex-wrap gap-1.5">
        {files.map(f => (
          <button
            key={f.path}
            onClick={() => setActive(f.path)}
            className={`px-2.5 py-1 rounded-lg text-xs font-mono transition-all border ${
              active === f.path
                ? 'bg-blue-500/20 text-blue-300 border-blue-500/40'
                : 'bg-slate-100 text-slate-600 border-slate-200 hover:border-gray-500'
            }`}
          >
            {f.is_changed && <span className="mr-1 text-amber-400">●</span>}
            {f.path}
          </button>
        ))}
      </div>
      {/* 파일 내용 */}
      {current && (
        <div className="bg-slate-50 rounded-lg border border-slate-200 overflow-auto max-h-96">
          <div className="px-4 py-2 border-b border-slate-200 flex items-center justify-between">
            <span className="text-xs text-slate-600 font-mono">{current.path}</span>
            <div className="flex items-center gap-2">
              {current.is_changed && (
                <span className="text-xs text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2 py-0.5 rounded-full">
                  이 커밋에서 변경됨
                </span>
              )}
              <span className="text-xs text-slate-500">
                {current.content.split('\n').length}줄 / {current.content.length.toLocaleString()}자
              </span>
            </div>
          </div>
          <pre className="text-xs text-slate-700 font-mono leading-relaxed overflow-auto">
            {current.content.split('\n').map((line, i) => (
              <div
                key={i}
                className="flex hover:bg-slate-100/95"
              >
                <span className="select-none w-10 text-right pr-3 text-slate-500 border-r border-slate-200 flex-shrink-0 py-0.5 px-2">
                  {i + 1}
                </span>
                <span className="pl-3 py-0.5 whitespace-pre break-all">{line}</span>
              </div>
            ))}
          </pre>
        </div>
      )}
    </div>
  )
}

// ── main component ─────────────────────────────────────────────────────────
export default function CommitView() {
  const [commits, setCommits] = useState<Commit[]>([])
  const [modalSha, setModalSha] = useState<string | null>(null)
  const [tab, setTab] = useState<Record<string, 'diff' | 'snapshot'>>({})
  const [loading, setLoading] = useState(true)

  const fetchCommits = () => {
    fetch('/api/commits')
      .then(r => r.json())
      .then((data: Commit[]) => { setCommits(data); setLoading(false) })
      .catch(() => setLoading(false))
  }

  useEffect(() => {
    fetchCommits()
    const id = setInterval(fetchCommits, 5000)
    return () => clearInterval(id)
  }, [])

  const getTab = (sha: string) => tab[sha] ?? 'diff'
  const setTabFor = (sha: string, t: 'diff' | 'snapshot') =>
    setTab(prev => ({ ...prev, [sha]: t }))
  const modalCommit = commits.find(c => c.sha === modalSha) ?? null

  if (loading) return (
    <div className="bg-white rounded-xl border border-slate-200 p-8 text-center text-slate-500 text-sm">
      커밋 데이터 로딩 중...
    </div>
  )

  if (!commits.length) return (
    <div className="bg-white rounded-xl border border-slate-200 p-8 text-center text-slate-500 text-sm">
      커밋 데이터가 없습니다. git commit 후 자동으로 수집됩니다.
    </div>
  )

  return (
    <>
    <div className="bg-white rounded-xl border border-slate-200">
      <div className="px-5 py-3 border-b border-slate-200 flex items-center justify-between">
        <h2 className="font-semibold text-slate-900 text-sm">커밋 이력</h2>
        <span className="text-slate-500 text-xs">{commits.length}건 · 클릭해서 상세 보기</span>
      </div>

      <div className="divide-y divide-slate-200">
        {commits.map(c => {
          const currentTab = getTab(c.sha)
          const firstLine = c.message.split('\n')[0]
          void currentTab

          return (
            <div key={c.sha}>
              {/* 커밋 행 */}
              <div
                onClick={() => setModalSha(c.sha)}
                className="px-5 py-3 cursor-pointer transition-colors flex items-start gap-4 hover:bg-slate-100/95 active:bg-slate-100"
              >
                {/* sha */}
                <span className="text-xs font-mono text-slate-500 whitespace-nowrap pt-0.5 w-16 flex-shrink-0">
                  {c.sha_short}
                </span>

                {/* 메시지 + 배지 */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="text-sm text-slate-800 truncate">{firstLine}</span>
                    {c.is_reviewed && (
                      <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex-shrink-0">
                        ✓ reviewed
                      </span>
                    )}
                    {c.changed_files.length > 0 && (
                      <span className="text-xs text-amber-400 flex-shrink-0">
                        {c.changed_files.length}개 파일 변경
                      </span>
                    )}
                  </div>
                  {c.ts_kst && (
                    <p className="text-xs text-slate-500 mt-0.5">{c.ts_kst}</p>
                  )}
                </div>

                {/* 배지 */}
                <div className="flex items-center gap-1.5 flex-shrink-0">
                  {c.has_patch && (
                    <span className="text-xs px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20">diff</span>
                  )}
                  {c.has_snapshot && (
                    <span className="text-xs px-2 py-0.5 rounded-full bg-violet-500/10 text-violet-400 border border-violet-500/20">snap</span>
                  )}
                  <span className="text-slate-500 text-xs">›</span>
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>

    {/* 커밋 상세 모달 */}
    <Modal
      open={modalCommit !== null}
      onClose={() => setModalSha(null)}
      title={`커밋 · ${modalCommit?.sha_short ?? ''} — ${(modalCommit?.message.split('\n')[0] ?? '').slice(0,50)}`}
      width="2xl"
    >
      {modalCommit && (() => {
        const c = modalCommit
        const curTab = getTab(c.sha)
        return (
          <div className="space-y-4">
            {/* 메타 */}
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="bg-slate-100 rounded-lg px-3 py-2">
                <p className="text-slate-500 mb-0.5">SHA</p>
                <p className="font-mono text-slate-700 break-all">{c.sha}</p>
              </div>
              <div className="bg-slate-100 rounded-lg px-3 py-2">
                <p className="text-slate-500 mb-0.5">시각 (KST)</p>
                <p className="text-slate-700">{c.ts_kst || '-'}</p>
              </div>
              <div className="bg-slate-100 rounded-lg px-3 py-2">
                <p className="text-slate-500 mb-0.5">연관 Task</p>
                <p className="font-mono text-slate-700">{c.task_id || '-'}</p>
              </div>
              <div className="bg-slate-100 rounded-lg px-3 py-2">
                <p className="text-slate-500 mb-0.5">변경 파일 {c.changed_files.length}개</p>
                <p className="text-slate-600 text-xs truncate">{c.changed_files.join(', ') || '-'}</p>
              </div>
            </div>

            {c.message.includes('\n') && (
              <div>
                <p className="text-xs text-slate-500 mb-1">커밋 메시지 전체</p>
                <pre className="text-xs text-slate-700 whitespace-pre-wrap font-mono bg-slate-50 p-3 rounded-lg border border-slate-200">
                  {c.message}
                </pre>
              </div>
            )}

            {/* diff / snapshot 탭 */}
            <div>
              <div className="flex gap-2 mb-3">
                {(['diff', 'snapshot'] as const).map(t => (
                  <button key={t} onClick={() => setTabFor(c.sha, t)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all border ${
                      curTab === t
                        ? 'bg-blue-500/20 text-blue-300 border-blue-500/40'
                        : 'bg-slate-100 text-slate-500 border-slate-200 hover:border-gray-500'
                    }`}>
                    {t === 'diff' ? '변경분 (diff)' : `전체 스냅샷 (${c.snapshot_files.length}개 파일)`}
                  </button>
                ))}
              </div>
              {curTab === 'diff' && <DiffViewer patch={c.patch_content} />}
              {curTab === 'snapshot' && <SnapshotViewer files={c.snapshot_files} />}
            </div>
          </div>
        )
      })()}
    </Modal>
    </>
  )
}
