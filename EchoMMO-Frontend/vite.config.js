import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // Dòng này giúp Vite hiểu '@' chính là thư mục 'src'
      '@': path.resolve(__dirname, './src'),
    },
  },
})