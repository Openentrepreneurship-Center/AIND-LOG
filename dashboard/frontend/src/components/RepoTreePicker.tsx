import { useState, useEffect, useRef } from 'react'
import { RepoTree, RepoTreeNode } from '../types'

interface Props {
  value: string
  onSelect: (path: string) => void
}

function TreeRow({
  node, depth, expanded, onToggle, onPickFile, selected,
}: {
  node: RepoTreeNode
  depth: number
  expanded: Set<string>
  onToggle: (path: string) => void
  onPickFile: (path: string) => void
  selected: string
}) {
  const isDir = node.type === 'dir'
  const isOpen = expanded.has(node.path)
  const isSelected = !isDir && node.path === selected
  return (
    <>
      <div
        role="button"
        onClick={() => isDir ? onToggle(node.path) : onPickFile(node.path)}
        className={`flex items-center gap-1.5 px-2 py-1 cursor-pointer text-xs rounded
          ${isSelected ? 'bg-violet-500/20 text-violet-200' : 'hover:bg-gray-800/60 text-gray-300'}`}
        style={{ paddingLeft: 8 + depth * 14 }}
      >
        {isDir ? (
          <span className="text-gray-500 w-3 inline-block text-[10px]">{isOpen ? '▼' : '▶'}</span>
        ) : (
          <span className="w-3 inline-block" />
        )}
        <span className={isDir ? 'text-amber-300/80' : 'text-gray-200 font-mono'}>
          {isDir ? '📁' : '📄'} {node.name}
        </span>
      </div>
      {isDir && isOpen && node.children?.map(child => (
        <TreeRow
          key={child.path}
          node={child}
          depth={depth + 1}
          expanded={expanded}
          onToggle={onToggle}
          onPickFile={onPickFile}
          selected={selected}
        />
      ))}
    </>
  )
}

export default function RepoTreePicker({ value, onSelect }: Props) {
  const [tree, setTree] = useState<RepoTree | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [open, setOpen] = useState(false)
  const [expanded, setExpanded] = useState<Set<string>>(new Set())
  const ref = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    fetch('/api/repo/tree')
      .then(async r => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`)
        return r.json() as Promise<RepoTree>
      })
      .then(data => {
        setTree(data)
        // 루트 자동 펼침
        setExpanded(new Set(['']))
        setLoading(false)
      })
      .catch(e => { setError(e.message); setLoading(false) })
  }, [])

  // 외부 클릭 시 닫기
  useEffect(() => {
    if (!open) return
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    window.addEventListener('mousedown', handler)
    return () => window.removeEventListener('mousedown', handler)
  }, [open])

  const toggle = (path: string) => {
    setExpanded(prev => {
      const next = new Set(prev)
      if (next.has(path)) next.delete(path); else next.add(path)
      return next
    })
  }

  const pick = (path: string) => {
    onSelect(path)
    setOpen(false)
  }

  return (
    <div ref={ref} className="relative w-full">
      <button
        type="button"
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center justify-between gap-2 bg-gray-950 border border-gray-800 rounded-lg px-3 py-2 text-xs text-left hover:border-gray-700 focus:outline-none focus:border-violet-500/60"
      >
        <span className={`truncate font-mono ${value ? 'text-gray-200' : 'text-gray-600'}`}>
          {value || '분석할 파일을 선택하세요…'}
        </span>
        <span className="text-gray-500 text-[10px]">
          {loading ? '로딩…' : tree ? `${tree.file_count}개 파일` : ''} {open ? '▲' : '▼'}
        </span>
      </button>

      {open && (
        <div className="absolute z-30 mt-1 w-full bg-gray-950 border border-gray-800 rounded-lg shadow-2xl max-h-[480px] overflow-auto">
          {loading && (
            <p className="text-xs text-gray-500 px-3 py-3">파일 트리 불러오는 중…</p>
          )}
          {error && (
            <p className="text-xs text-red-400 px-3 py-3">오류: {error}</p>
          )}
          {tree && (
            <div className="py-2">
              {tree.tree.children?.map(child => (
                <TreeRow
                  key={child.path}
                  node={child}
                  depth={0}
                  expanded={expanded}
                  onToggle={toggle}
                  onPickFile={pick}
                  selected={value}
                />
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
