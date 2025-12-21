<template>
  <transition name="toast-fade">
    <div v-if="visible" class="game-toast" :class="type">
      <div class="toast-content">
        <i v-if="type === 'success'" class="fas fa-check-circle"></i>
        <i v-else-if="type === 'error'" class="fas fa-exclamation-circle"></i>
        <i v-else class="fas fa-info-circle"></i>
        <span>{{ message }}</span>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref } from "vue";

const visible = ref(false);
const message = ref("");
const type = ref("info"); 

let timeout = null;

const show = (msg, msgType = "info") => {
  message.value = msg;
  type.value = msgType;
  visible.value = true;

  if (timeout) clearTimeout(timeout);
  timeout = setTimeout(() => {
    visible.value = false;
  }, 3000);
};

// [FIX] Expose show ra ngoài
defineExpose({
  show,
});
</script>

<style scoped>
.game-toast {
  position: fixed;
  top: 80px; 
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
}

.success {
  background: rgba(27, 94, 32, 0.95);
  color: #a5d6a7;
  border-color: #2e7d32;
}

.error {
  background: rgba(183, 28, 28, 0.95);
  color: #ffcdd2;
  border-color: #c62828;
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
  font-size: 1.2rem;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}
</style>