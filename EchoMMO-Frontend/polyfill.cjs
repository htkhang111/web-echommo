const crypto = require('crypto');

// --- FIX CORE MODULE: Can thiệp trực tiếp vào module 'crypto' của Node ---
// Vite import 'crypto', nên ta phải gán hàm getRandomValues vào chính object này.

if (!crypto.getRandomValues) {
  crypto.getRandomValues = function(buffer) {
    return crypto.randomFillSync(buffer);
  };
}

// --- FIX GLOBAL: Phòng trường hợp Vite dùng globalThis.crypto ---
if (!globalThis.crypto) {
  globalThis.crypto = crypto;
} else if (!globalThis.crypto.getRandomValues) {
  globalThis.crypto.getRandomValues = crypto.getRandomValues;
}

console.log('✅ Đã tiêm Polyfill vào tận lõi module Crypto!');