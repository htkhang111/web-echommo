<template>
  <transition name="toast-slide">
    <div v-if="notificationStore.toast.visible" class="game-toast" :class="notificationStore.toast.type">
      <div class="toast-icon">
        <i v-if="notificationStore.toast.type === 'success'" class="fas fa-check-circle"></i>
        <i v-else-if="notificationStore.toast.type === 'error'" class="fas fa-exclamation-circle"></i>
        <i v-else-if="notificationStore.toast.type === 'warning'" class="fas fa-exclamation-triangle"></i>
        <i v-else class="fas fa-info-circle"></i>
      </div>
      <div class="toast-content">
        {{ notificationStore.toast.message }}
      </div>
      <button class="toast-close" @click="notificationStore.hideToast">
        <i class="fas fa-times"></i>
      </button>
    </div>
  </transition>
</template>

<script setup>
import { useNotificationStore } from "../stores/notificationStore";
const notificationStore = useNotificationStore();
</script>

<style scoped>
.game-toast {
  position: fixed;
  top: 80px; /* Thấp xuống dưới Header một chút */
  right: 20px;
  z-index: 9999;
  min-width: 300px;
  max-width: 400px;
  background: rgba(30, 20, 15, 0.95);
  color: #fff;
  padding: 15px 20px;
  border-radius: 4px;
  border-left: 5px solid;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  gap: 15px;
  font-family: "Noto Serif TC", serif;
  backdrop-filter: blur(5px);
}

/* Các loại màu sắc */
.game-toast.success { border-left-color: #2e7d32; }
.game-toast.success .toast-icon { color: #4caf50; }

.game-toast.error { border-left-color: #c62828; }
.game-toast.error .toast-icon { color: #ef5350; }

.game-toast.warning { border-left-color: #f9a825; }
.game-toast.warning .toast-icon { color: #fbc02d; }

.game-toast.info { border-left-color: #1565c0; }
.game-toast.info .toast-icon { color: #42a5f5; }

.toast-icon { font-size: 1.5rem; }
.toast-content { flex: 1; font-weight: 600; font-size: 0.95rem; line-height: 1.4; }
.toast-close { background: none; border: none; color: #aaa; cursor: pointer; font-size: 1rem; transition: 0.2s; }
.toast-close:hover { color: #fff; }

/* Hiệu ứng Animation */
.toast-slide-enter-active, .toast-slide-leave-active { transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.toast-slide-enter-from, .toast-slide-leave-to { opacity: 0; transform: translateX(100%); }
</style>