/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_SCREENSHOT_BRANCH?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
