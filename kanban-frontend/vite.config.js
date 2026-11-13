import { fileURLToPath } from 'node:url'
import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// The settings live in the project-root .env (see .env.example). process.env wins if both set a value.
const projectRoot = fileURLToPath(new URL('..', import.meta.url))

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = { ...loadEnv(mode, projectRoot, ''), ...process.env }
  const frontendPort = Number(env.FRONTEND_PORT || 5173)
  const backendPort = Number(env.BACKEND_PORT || 8081)

  return {
    plugins: [react()],
    server: {
      // Plain HTTP on localhost only; Caddy terminates HTTPS in front of it
      host: '127.0.0.1',
      port: frontendPort,
      strictPort: true,
      // Vite rejects unknown Host headers; allow the public hostname that Caddy forwards
      allowedHosts: [env.PUBLIC_HOST].filter(Boolean),
      proxy: {
        '/api': `http://127.0.0.1:${backendPort}`,
      },
    },
  }
})
