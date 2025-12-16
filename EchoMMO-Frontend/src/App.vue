<template>
  <component :is="layout">
    <router-view v-slot="{ Component }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </component>

  <CaptchaModal
    v-if="showCaptcha"
    :is-visible="showCaptcha"
    @success="onCaptchaSuccess"
    @close="showCaptcha = false"
  />

  <transition name="toast-slide">
    <div
      v-if="notificationStore.toast.visible"
      class="global-toast"
      :class="notificationStore.toast.type"
    >
      <div class="toast-icon">
        <i
          v-if="notificationStore.toast.type === 'success'"
          class="fas fa-check-circle"
        ></i>
        <i
          v-else-if="notificationStore.toast.type === 'error'"
          class="fas fa-times-circle"
        ></i>
        <i v-else class="fas fa-info-circle"></i>
      </div>
      <div class="toast-content">
        <div class="toast-title">
          {{
            notificationStore.toast.type === "success"
              ? "THÀNH CÔNG"
              : notificationStore.toast.type === "error"
                ? "THẤT BẠI"
                : "THÔNG BÁO"
          }}
        </div>
        <div class="toast-msg">{{ notificationStore.toast.message }}</div>
      </div>
      <button class="toast-close" @click="notificationStore.clearToast">
        <i class="fas fa-times"></i>
      </button>
    </div>
  </transition>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { useAuthStore } from "./stores/authStore";
import { useNotificationStore } from "./stores/notificationStore"; // Nhớ import Notification Store
import MainLayout from "./layouts/MainLayout.vue";
import CaptchaModal from "./components/CaptchaModal.vue";

const route = useRoute();
const authStore = useAuthStore();
const notificationStore = useNotificationStore();
const showCaptcha = ref(false);

const layout = computed(() => {
  return route.meta.guest ? "div" : MainLayout;
});

onMounted(async () => {
  authStore.initialize();
  if (authStore.isAuthenticated) {
    await authStore.fetchProfile();
  }
});

const onCaptchaSuccess = () => {
  showCaptcha.value = false;
};
</script>

<style>
/* ... CSS cũ ... */
body {
  margin: 0;
  padding: 0;
  background-color: #1a1a1a;
  color: #e0e0e0;
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Copy lại CSS Global Toast từ câu trả lời trước vào đây */
.global-toast {
  position: fixed;
  top: 90px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 15px;
  min-width: 320px;
  max-width: 90vw;
  padding: 15px 20px;
  border-radius: 4px;
  background: rgba(30, 20, 15, 0.98);
  border: 1px solid #5d4037;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  color: #f3f4f6;
  font-family: "Noto Serif TC", serif;
}
.global-toast.success {
  border-left: 5px solid #2e7d32;
}
.global-toast.success .toast-icon {
  color: #4caf50;
}
.global-toast.success .toast-title {
  color: #4caf50;
}

.global-toast.error {
  border-left: 5px solid #b71c1c;
}
.global-toast.error .toast-icon {
  color: #ef5350;
}
.global-toast.error .toast-title {
  color: #ef5350;
}

.global-toast.info {
  border-left: 5px solid #0277bd;
}
.global-toast.info .toast-icon {
  color: #29b6f6;
}
.global-toast.info .toast-title {
  color: #29b6f6;
}

.toast-icon {
  font-size: 1.8rem;
}
.toast-content {
  flex: 1;
}
.toast-title {
  font-weight: 900;
  font-size: 0.9rem;
  letter-spacing: 1px;
  margin-bottom: 2px;
}
.toast-msg {
  font-size: 0.95rem;
  line-height: 1.4;
}

.toast-close {
  background: none;
  border: none;
  color: #757575;
  cursor: pointer;
  font-size: 1.2rem;
}
.toast-close:hover {
  color: #fff;
}

.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
.toast-slide-enter-from,
.toast-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}
</style>
