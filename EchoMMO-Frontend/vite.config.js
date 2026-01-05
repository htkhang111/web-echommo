import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import crypto from 'crypto'

// --- FIX MẠNH TAY: Polyfill crypto.getRandomValues ---
// 1. Đảm bảo object crypto tồn tại
if (!globalThis.crypto) {
  globalThis.crypto = {};
}

// 2. Kiểm tra nếu thiếu hàm getRandomValues thì chèn vào (dùng randomFillSync của Node)
if (!globalThis.crypto.getRandomValues) {
  globalThis.crypto.getRandomValues = (buffer) => {
    return crypto.randomFillSync(buffer);
  };
}
// --- KẾT THÚC FIX ---

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    },
  },
})