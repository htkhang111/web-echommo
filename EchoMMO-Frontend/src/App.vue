<template>
  <div id="app-root">
    <GameToast />
    <ChatWidget />

    <transition name="fade-overlay">
      <div v-if="authStore.isBanned" class="banned-overlay">
        <div class="banned-box">
          <div class="seal-animation">
            <div class="circle-seal">PHONG ẤN</div>
          </div>
          <h2 class="ban-title">THIÊN LÔI GIÁNG THẾ</h2>
          <p class="ban-subtitle">
            Đạo hạnh của ngươi đã bị phế bỏ. Vĩnh viễn không thể siêu sinh.
          </p>

          <div class="ban-reason-box">
            <label><i class="fas fa-scroll"></i> LÝ DO TRỪNG PHẠT:</label>
            <p class="reason-text">"{{ authStore.banReason }}"</p>
            <p class="ban-time" v-if="authStore.banTime">
              Thời gian thi hành: {{ authStore.banTime }}
            </p>
          </div>

          <div class="ban-actions">
            <button @click="handleLogout" class="btn-leave">
              <i class="fas fa-door-open"></i> RỜI KHỎI GIANG HỒ
            </button>
          </div>
        </div>
      </div>
    </transition>

    <router-view v-if="isAuthPage" />
    <MainLayout v-else>
      <router-view />
    </MainLayout>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "./stores/authStore";
import GameToast from "./components/GameToast.vue";
import ChatWidget from "./components/ChatWidget.vue";
import MainLayout from "./layouts/MainLayout.vue";

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const isAuthPage = computed(() => {
  // [FIX] Thêm ?. để tránh lỗi "Cannot read properties of undefined" khi router chưa sẵn sàng
  return ["Login", "Register", "ForgotPassword"].includes(route?.name);
});

const handleLogout = () => {
  authStore.logout();
  router.push("/login");
};

onMounted(async () => {
  // Chỉ init nếu không phải trang login để tránh call dư thừa
  if (!isAuthPage.value) {
      await authStore.initialize();
  }
});
</script>

<style>
/* Reset & Global */
body {
  margin: 0;
  padding: 0;
  background-color: #000;
  color: #fff;
  font-family: "Noto Serif TC", serif;
}
#app-root {
  min-height: 100vh;
  position: relative;
}

/* Scrollbar */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: #1a1a1a;
}
::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}

/* --- BANNED OVERLAY STYLES (XỊN XÒ) --- */
.banned-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.96);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(15px);
}

.banned-box {
  text-align: center;
  border: 2px solid #b71c1c;
  padding: 60px 40px;
  background: linear-gradient(180deg, #1a0505 0%, #000 100%);
  max-width: 600px;
  width: 90%;
  position: relative;
  box-shadow:
    0 0 100px rgba(183, 28, 28, 0.4),
    inset 0 0 50px rgba(183, 28, 28, 0.1);
  border-radius: 4px;
}

/* Hiệu ứng Ấn Chú xoay */
.seal-animation {
  position: absolute;
  top: -60px;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 120px;
  background: #b71c1c;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4px solid #ff5252;
  box-shadow: 0 0 40px #ff5252;
  animation: pulseSeal 3s infinite;
}
.circle-seal {
  color: #fff;
  font-weight: 900;
  font-size: 1.2rem;
  transform: rotate(-15deg);
  border: 2px solid #fff;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.2);
}

.ban-title {
  color: #ff5252;
  font-size: 2.5rem;
  margin-top: 50px;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(255, 82, 82, 0.6);
  font-family: "Cinzel", serif;
}
.ban-subtitle {
  color: #888;
  margin-bottom: 40px;
  font-size: 1.1rem;
  font-style: italic;
}

.ban-reason-box {
  background: rgba(255, 23, 68, 0.05);
  padding: 25px;
  border-left: 4px solid #ff5252;
  border-right: 4px solid #ff5252;
  margin-bottom: 40px;
  text-align: left;
}
.ban-reason-box label {
  display: block;
  font-size: 0.9rem;
  color: #ff5252;
  text-transform: uppercase;
  font-weight: bold;
  margin-bottom: 10px;
  letter-spacing: 1px;
}
.reason-text {
  font-style: italic;
  color: #fff;
  margin: 0;
  font-size: 1.3rem;
  line-height: 1.5;
  font-weight: bold;
}
.ban-time {
  margin-top: 15px;
  font-size: 0.85rem;
  color: #666;
  text-align: right;
}

.btn-leave {
  background: transparent;
  border: 2px solid #b71c1c;
  color: #ff5252;
  padding: 15px 50px;
  font-weight: bold;
  cursor: pointer;
  text-transform: uppercase;
  letter-spacing: 2px;
  font-family: inherit;
  transition: 0.4s;
  font-size: 1rem;
}
.btn-leave:hover {
  background: #b71c1c;
  color: #fff;
  box-shadow: 0 0 30px #b71c1c;
}

@keyframes pulseSeal {
  0% {
    box-shadow: 0 0 20px #ff5252;
  }
  50% {
    box-shadow: 0 0 60px #ff5252;
  }
  100% {
    box-shadow: 0 0 20px #ff5252;
  }
}
.fade-overlay-enter-active,
.fade-overlay-leave-active {
  transition: opacity 0.5s;
}
.fade-overlay-enter-from,
.fade-overlay-leave-to {
  opacity: 0;
}
</style>
