import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { screenshotMockPlugin } from './vite-screenshot-mock-plugin'

/** demo/dashboard-screenshot: SCREENSHOT_API_MOCK=1 로 /api 를 로컬 픽스처로 제공 (백엔드 불필요) */
const apiMock = process.env.SCREENSHOT_API_MOCK === '1'

export default defineConfig({
  plugins: [react(), screenshotMockPlugin()],
  server: {
    port: 5200,
    ...(apiMock
      ? {}
      : {
          proxy: {
            '/api': {
              target: 'http://localhost:8000',
              changeOrigin: true,
            },
          },
        }),
  },
})
