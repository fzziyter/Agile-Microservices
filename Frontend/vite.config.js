import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            // Forward the Authorization header explicitly
            const auth = req.headers['authorization']
            if (auth) {
              proxyReq.setHeader('Authorization', auth)
            }
          })
        }
      }
    }
  }
})