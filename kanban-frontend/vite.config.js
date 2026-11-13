import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Plain HTTP on localhost only; Caddy terminates HTTPS in front of it
    host: '127.0.0.1',
    port: 5173,
    strictPort: true,
    // Vite rejects unknown Host headers; allow the No-IP hostname (Caddy forwards it)
    allowedHosts: ['lucyechkaaa.myddns.me'],
    proxy: {
      '/api': 'http://127.0.0.1:8081',
    },
  },
})
