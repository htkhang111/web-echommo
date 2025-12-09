<template>
  <div class="page-container wuxia-login">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
      <div class="paper-overlay"></div>
    </div>

    <div class="login-wrapper">
      <div class="auth-panel">
        <div class="decor-corner top-left"></div>
        <div class="decor-corner top-right"></div>
        <div class="decor-corner bottom-left"></div>
        <div class="decor-corner bottom-right"></div>

        <div class="auth-header">
          <div class="sect-seal">
            <span class="seal-char">龍</span>
          </div>
          <h2 class="title-ink">NHẬP MÔN</h2>
          <p class="subtitle">Danh chấn giang hồ, bắt đầu từ đây</p>
        </div>

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="input-group">
            <div class="icon-box"><i class="fas fa-user-ninja"></i></div>
            <input
              v-model="username"
              type="text"
              placeholder="Danh xưng / Tài khoản"
              required
              class="ink-input"
            />
          </div>

          <div class="input-group">
            <div class="icon-box"><i class="fas fa-key"></i></div>
            <input
              v-model="password"
              type="password"
              placeholder="Mật khẩu"
              required
              class="ink-input"
              autocomplete="current-password"
            />
          </div>

          <transition name="shake">
            <div v-if="authStore.error" class="error-scroll">
              <i class="fas fa-exclamation-circle"></i>
              <span>{{ authStore.error }}</span>
            </div>
          </transition>

          <button
            type="submit"
            class="btn-seal-submit"
            :disabled="authStore.isLoading"
          >
            <span v-if="!authStore.isLoading">
              BƯỚC VÀO GIANG HỒ <i class="fas fa-scroll"></i>
            </span>
            <span v-else>
              <i class="fas fa-circle-notch fa-spin"></i> ĐANG KẾT NỐI...
            </span>
          </button>
        </form>

        <div class="auth-footer">
          <router-link to="/forgot-password" class="footer-link">
            Quên Mật Khẩu?
          </router-link>
          <span class="sep">❖</span>
          <router-link to="/register" class="footer-link highlight">
            Đăng Ký Mới
          </router-link>
        </div>
      </div>
    </div>

    <div v-if="banData" class="ban-overlay">
      <div class="ban-scroll">
        <div class="scroll-rods top"></div>
        <div class="ban-header">
          <div class="ban-seal">TRỤC XUẤT</div>
          <h3>LỆNH TRỤC KHÁCH</h3>
        </div>
        <div class="ban-content">
          <p class="ban-text warning">Tại hạ đã bị trục xuất khỏi giang hồ.</p>
          <div class="ban-reason-box">
            <p><strong>Lý do:</strong> "{{ banData.reason }}"</p>
            <p>
              <strong>Thời gian:</strong>
              {{ new Date(banData.date).toLocaleString() }}
            </p>
          </div>
          <p class="ban-text info">Vui lòng liên hệ Chưởng Môn để khiếu nại.</p>
        </div>
        <button @click="banData = null" class="btn-leave">RỜI KHỎI</button>
        <div class="scroll-rods bot"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useAuthStore } from "../stores/authStore";

const username = ref("");
const password = ref("");
const authStore = useAuthStore();
const banData = ref(null);

const handleLogin = async () => {
  // [FIX] Gọi qua Store để đảm bảo lưu ID và Token chuẩn
  await authStore.login({
    username: username.value,
    password: password.value,
  });

  // Nếu có lỗi đặc biệt như BANNED (tùy chỉnh logic backend của bạn)
  if (authStore.error && authStore.error.includes("BANNED")) {
    // Xử lý hiển thị banData nếu cần
  }
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --paper-bg: #e3d5b8;
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --ink: #212121;
  --red-seal: #b71c1c;
  --gold: #d4a017;
}

.wuxia-login {
  min-height: 100vh;
  background: var(--wood-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  font-family: "Noto Serif TC", serif;
  color: var(--wood-dark);
}

.ink-bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-color: #d7ccc8;
}

.mountain-bg {
  position: absolute;
  inset: 0;
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  background-position: center;
  filter: sepia(40%) grayscale(20%) contrast(0.9);
  opacity: 0.8;
}

.fog-anim {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle,
    transparent 40%,
    rgba(62, 39, 35, 0.8) 100%
  );
}

.paper-overlay {
  position: absolute;
  inset: 0;
  background-image: url("https://www.transparenttextures.com/patterns/aged-paper.png");
  opacity: 0.3;
  pointer-events: none;
}

.login-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.auth-panel {
  width: 100%;
  max-width: 420px;
  background: var(--paper-bg);
  border: 4px double var(--wood-light);
  padding: 40px;
  position: relative;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
  background-image: repeating-linear-gradient(
    45deg,
    rgba(0, 0, 0, 0.02) 0,
    rgba(0, 0, 0, 0.02) 2px,
    transparent 2px,
    transparent 6px
  );
}

.decor-corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 2px solid var(--wood-dark);
  transition: 0.3s;
}
.top-left {
  top: 4px;
  left: 4px;
  border-right: none;
  border-bottom: none;
}
.top-right {
  top: 4px;
  right: 4px;
  border-left: none;
  border-bottom: none;
}
.bottom-left {
  bottom: 4px;
  left: 4px;
  border-right: none;
  border-top: none;
}
.bottom-right {
  bottom: 4px;
  right: 4px;
  border-left: none;
  border-top: none;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.sect-seal {
  width: 80px;
  height: 80px;
  margin: 0 auto 15px;
  border: 3px solid var(--red-seal);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(183, 28, 28, 0.1);
  box-shadow: 0 0 15px rgba(183, 28, 28, 0.2);
}

.seal-char {
  font-family: "Noto Serif TC", serif;
  font-size: 3rem;
  font-weight: 900;
  color: var(--red-seal);
  line-height: 1;
}

.title-ink {
  font-family: "Noto Serif TC", serif;
  font-size: 2.2rem;
  color: var(--wood-dark);
  margin: 0;
  font-weight: 900;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.subtitle {
  color: #795548;
  font-size: 0.9em;
  font-style: italic;
  margin-top: 5px;
  font-family: "Playfair Display", serif;
}

.input-group {
  position: relative;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  border-bottom: 2px solid var(--wood-light);
  transition: 0.3s;
}

.icon-box {
  width: 40px;
  text-align: center;
  color: var(--wood-light);
  font-size: 1.2em;
}

.ink-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--wood-dark);
  padding: 10px;
  font-family: "Playfair Display", serif;
  font-size: 1.1em;
  font-weight: bold;
  outline: none;
}
.ink-input::placeholder {
  color: #a1887f;
  font-weight: normal;
}
.input-group:focus-within {
  border-color: var(--red-seal);
}
.input-group:focus-within .icon-box {
  color: var(--red-seal);
}

.error-scroll {
  background: #ffcdd2;
  border-left: 4px solid var(--red-seal);
  color: #b71c1c;
  padding: 10px;
  font-size: 0.9em;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
}

.btn-seal-submit {
  width: 100%;
  padding: 12px;
  background: var(--red-seal);
  color: #fff;
  border: 2px solid #8a1c1c;
  font-family: "Noto Serif TC", serif;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
  border-radius: 4px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
}
.btn-seal-submit:hover {
  background: #d32f2f;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
}
.btn-seal-submit:disabled {
  background: #9e9e9e;
  border-color: #757575;
  cursor: not-allowed;
  transform: none;
}

.auth-footer {
  margin-top: 30px;
  text-align: center;
  font-size: 0.95em;
  font-family: "Playfair Display", serif;
}
.footer-link {
  color: #5d4037;
  text-decoration: none;
  transition: 0.2s;
  font-weight: bold;
}
.footer-link:hover {
  color: var(--red-seal);
  text-decoration: underline;
}
.footer-link.highlight {
  color: var(--red-seal);
}
.sep {
  margin: 0 10px;
  color: #a1887f;
  font-size: 0.8em;
}

.ban-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ban-scroll {
  width: 400px;
  background: #fff8e1;
  padding: 40px 30px;
  position: relative;
  text-align: center;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
}
.scroll-rods {
  position: absolute;
  left: 0;
  right: 0;
  height: 15px;
  background: var(--wood-dark);
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);
}
.scroll-rods.top {
  top: -8px;
}
.scroll-rods.bot {
  bottom: -8px;
}
.ban-header {
  margin-bottom: 20px;
  border-bottom: 2px solid var(--wood-light);
  padding-bottom: 15px;
}
.ban-seal {
  font-family: "Noto Serif TC";
  font-size: 3rem;
  color: rgba(183, 28, 28, 0.15);
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-15deg);
  border: 4px solid rgba(183, 28, 28, 0.15);
  padding: 10px;
  border-radius: 8px;
  pointer-events: none;
  font-weight: 900;
  white-space: nowrap;
}
.ban-header h3 {
  margin: 0;
  color: var(--red-seal);
  font-size: 1.8rem;
  font-weight: 900;
}
.ban-content {
  margin-bottom: 30px;
  font-family: "Playfair Display";
  position: relative;
  z-index: 2;
}
.ban-text.warning {
  font-size: 1.1rem;
  color: var(--wood-dark);
  font-weight: bold;
}
.ban-reason-box {
  background: rgba(0, 0, 0, 0.05);
  padding: 10px;
  margin: 15px 0;
  border-radius: 4px;
  text-align: left;
  font-size: 0.95em;
  color: #3e2723;
}
.ban-text.info {
  font-size: 0.9em;
  color: #795548;
  font-style: italic;
}
.btn-leave {
  background: var(--wood-dark);
  color: #fff;
  border: none;
  padding: 10px 20px;
  font-family: "Noto Serif TC";
  font-weight: bold;
  cursor: pointer;
  align-self: center;
  border-radius: 4px;
}
.btn-leave:hover {
  background: var(--red-seal);
}

.shake-enter-active {
  animation: shake 0.5s;
}
@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-5px);
  }
  75% {
    transform: translateX(5px);
  }
}
</style>
