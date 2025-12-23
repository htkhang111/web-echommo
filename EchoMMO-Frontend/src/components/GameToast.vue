<template>
  <transition name="toast-fade">
    <div
      v-if="notificationStore.toast.visible"
      class="game-toast"
      :class="notificationStore.toast.type"
      @click="notificationStore.hideToast"
    >
      <div class="toast-content">
        <i
          v-if="notificationStore.toast.type === 'success'"
          class="fas fa-check-circle"
        ></i>
        <i
          v-else-if="notificationStore.toast.type === 'error'"
          class="fas fa-exclamation-circle"
        ></i>
        <i
          v-else-if="notificationStore.toast.type === 'warning'"
          class="fas fa-exclamation-triangle"
        ></i>
        <i v-else class="fas fa-info-circle"></i>
        
        <span>{{ notificationStore.toast.message }}</span>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { useNotificationStore } from "../stores/notificationStore";

// [FIX] Kết nối với Store
const notificationStore = useNotificationStore();
</script>

<style scoped>
.game-toast {
  position: fixed;
  top: 80px; /* Né cái Header ra xíu */
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  padding: 12px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
  font-family: "Noto Serif TC", serif;
  font-weight: bold;
  border: 1px solid rgba(255, 255, 255, 0.1);
  min-width: 300px;
  text-align: center;
  cursor: pointer;
  backdrop-filter: blur(5px);
}

.success {
  background: rgba(27, 94, 32, 0.95);
  color: #a5d6a7;
  border-color: #2e7d32;
  box-shadow: 0 0 15px rgba(46, 125, 50, 0.3);
}

.error {
  background: rgba(183, 28, 28, 0.95);
  color: #ffcdd2;
  border-color: #c62828;
  box-shadow: 0 0 15px rgba(198, 40, 40, 0.3);
}

.warning {
  background: rgba(255, 111, 0, 0.95);
  color: #ffe0b2;
  border-color: #ff8f00;
  box-shadow: 0 0 15px rgba(255, 143, 0, 0.3);
}

.info {
  background: rgba(33, 33, 33, 0.95);
  color: #e0e0e0;
  border-color: #424242;
}

.toast-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.toast-content i {
  font-size: 1.3rem;
}

/* Animation */
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -30px) scale(0.9);
}
</style>


