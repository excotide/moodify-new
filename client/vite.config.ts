import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  // Dev server proxy: forward /api requests to backend (Spring Boot default port 8080)
  // Adjust target if your backend uses a different host/port.
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // optional: rewrite path if backend doesn't use /api prefix
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
  },
})
