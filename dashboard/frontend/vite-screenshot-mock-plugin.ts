import type { IncomingMessage } from 'http'
import type { Plugin } from 'vite'
import {
  MOCK_COMMITS,
  MOCK_DASHBOARD,
  MOCK_PROJECT_SIMILARITY,
  MOCK_REPO_TREE,
  mockFirstLastForFile,
  mockSimilarityForFile,
} from './src/screenshotFixtures'

export function screenshotMockPlugin(): Plugin {
  const enabled = process.env.SCREENSHOT_API_MOCK === '1'
  return {
    name: 'screenshot-mock-api',
    configureServer(server) {
      if (!enabled) return

      server.middlewares.use(
        (req: IncomingMessage, res: { [k: string]: unknown }, next: () => void) => {
          const raw = req.url ?? ''
          if (!raw.startsWith('/api')) {
            next()
            return
          }

          const pathOnly = raw.split('?')[0] ?? ''

          if (pathOnly === '/api/stream' && req.method === 'GET') {
            const r = res as import('http').ServerResponse
            r.statusCode = 200
            r.setHeader('Content-Type', 'text/event-stream')
            r.setHeader('Cache-Control', 'no-cache')
            r.setHeader('Connection', 'keep-alive')
            r.setHeader('X-Accel-Buffering', 'no')
            r.write(`data: ${JSON.stringify(MOCK_DASHBOARD)}\n\n`)
            const t = setInterval(() => {
              r.write(': ping\n\n')
            }, 25000)
            req.on('close', () => clearInterval(t))
            return
          }

          const sendJson = (data: unknown, code = 200) => {
            const r = res as import('http').ServerResponse
            r.statusCode = code
            r.setHeader('Content-Type', 'application/json; charset=utf-8')
            r.end(JSON.stringify(data))
          }

          if (pathOnly === '/api/commits' && req.method === 'GET') {
            sendJson(MOCK_COMMITS)
            return
          }
          if (pathOnly === '/api/repo/tree' && req.method === 'GET') {
            sendJson(MOCK_REPO_TREE)
            return
          }
          if (pathOnly.startsWith('/api/similarity/first-last')) {
            const file = new URL(raw, 'http://vite.local').searchParams.get('file') ?? ''
            sendJson(mockFirstLastForFile(file))
            return
          }
          if (pathOnly.startsWith('/api/similarity/project')) {
            sendJson(MOCK_PROJECT_SIMILARITY)
            return
          }
          if (pathOnly === '/api/similarity') {
            const file = new URL(raw, 'http://vite.local').searchParams.get('file') ?? ''
            sendJson(mockSimilarityForFile(file))
            return
          }

          sendJson({ detail: 'mock: unknown /api route' }, 404)
        },
      )
    },
  }
}
