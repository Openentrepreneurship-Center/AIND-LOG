import { useEffect, useRef } from 'react'

interface Props {
  open: boolean
  onClose: () => void
  title: string
  width?: 'md' | 'lg' | 'xl' | '2xl'
  children: React.ReactNode
}

const WIDTH_CLASS = {
  md: 'max-w-md',
  lg: 'max-w-lg',
  xl: 'max-w-xl',
  '2xl': 'max-w-2xl',
}

export default function Modal({ open, onClose, title, width = 'lg', children }: Props) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [open, onClose])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      onMouseDown={e => {
        if (e.target === e.currentTarget) onClose()
      }}
    >
      <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-[2px]" />

      <div
        ref={ref}
        className={`relative z-10 w-full ${WIDTH_CLASS[width]} bg-white border border-slate-200 rounded-2xl shadow-2xl flex flex-col max-h-[90vh]`}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100 shrink-0 bg-slate-50/80 rounded-t-2xl">
          <h3 className="text-sm font-semibold text-slate-900 leading-snug pr-6">{title}</h3>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-500 hover:text-slate-900 transition-colors w-8 h-8 flex items-center justify-center rounded-lg hover:bg-white border border-transparent hover:border-slate-200"
          >
            ✕
          </button>
        </div>

        <div
          className="overflow-y-auto p-5 flex-1"
          style={{ scrollbarWidth: 'thin', scrollbarColor: '#cbd5e1 transparent' }}
        >
          {children}
        </div>
      </div>
    </div>
  )
}
