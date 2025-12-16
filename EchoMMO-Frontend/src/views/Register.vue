<template>
  <div class="page-container wuxia-register">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
      <div class="paper-overlay"></div>
    </div>

    <div class="register-wrapper">
      <div class="auth-panel">
        <div class="decor-corner top-left"></div>
        <div class="decor-corner top-right"></div>
        <div class="decor-corner bottom-left"></div>
        <div class="decor-corner bottom-right"></div>

        <div class="auth-header">
          <div class="sect-seal">
            <span class="seal-char">名</span>
          </div>
          <h2 class="title-ink">GHI DANH</h2>
          <p class="subtitle">Sơ nhập giang hồ, định danh thiên hạ</p>
        </div>

        <form @submit.prevent="handleRegister" class="auth-form">
          <div class="input-group">
            <div class="icon-box"><i class="fas fa-user-tag"></i></div>
            <input
              v-model="form.username"
              type="text"
              placeholder="Danh Xưng (Tài khoản)"
              required
              class="ink-input"
            />
          </div>

          <div class="input-group">
            <div class="icon-box">
              <i class="fas fa-envelope-open-text"></i>
            </div>
            <input
              v-model="form.email"
              type="email"
              placeholder="Địa Chỉ Thư Tín (Email)"
              required
              class="ink-input"
            />
          </div>

          <div class="input-group">
            <div class="icon-box"><i class="fas fa-id-card-alt"></i></div>
            <input
              v-model="form.fullName"
              type="text"
              placeholder="Biệt Hiệu (Tên hiển thị)"
              class="ink-input"
            />
          </div>

          <div class="input-group">
            <div class="icon-box"><i class="fas fa-key"></i></div>
            <input
              v-model="form.password"
              type="password"
              placeholder="Mật Khẩu"
              required
              class="ink-input"
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
              GIA NHẬP GIANG HỒ <i class="fas fa-dragon"></i>
            </span>
            <span v-else>
              <i class="fas fa-circle-notch fa-spin"></i> ĐANG KHẮC TÊN...
            </span>
          </button>
        </form>

        <div class="auth-footer">
          <span class="text-muted">Đã có danh phận?</span>
          <router-link to="/login" class="footer-link highlight">
            Đăng Nhập Ngay <i class="fas fa-sign-in-alt"></i>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();
const form = reactive({
  username: "",
  email: "",
  fullName: "",
  password: "",
});

const handleRegister = () => {
  authStore.register(form);
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Noto+Serif+TC:wght@500;700;900&display=swap");

/* --- VARIABLES --- */
:root {
  --paper-bg: #e3d5b8;
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --ink: #212121;
  --red-seal: #b71c1c;
  --gold: #d4a017;
}

/* --- BASE --- */
.wuxia-register {
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

/* --- BACKGROUND EFFECTS --- */
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

.register-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  padding: 20px;
  display: flex;
  justify-content: center;
}

/* --- AUTH PANEL (BẢNG GỖ/GIẤY) --- */
.auth-panel {
  width: 100%;
  max-width: 450px; /* Rộng hơn login một chút */
  background: var(--paper-bg);
  border: 4px double var(--wood-light);
  padding: 40px;
  position: relative;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.6);
  /* Vân giấy */
  background-image: repeating-linear-gradient(
    45deg,
    rgba(0, 0, 0, 0.02) 0,
    rgba(0, 0, 0, 0.02) 2px,
    transparent 2px,
    transparent 6px
  );
}

/* Góc trang trí */
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
  margin-bottom: 25px;
}

.sect-seal {
  width: 70px;
  height: 70px;
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
  font-size: 2.5rem;
  font-weight: 900;
  color: var(--red-seal);
  line-height: 1;
}

.title-ink {
  font-family: "Noto Serif TC", serif;
  font-size: 2rem;
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

/* FORM & INPUTS */
.auth-form {
  display: flex;
  flex-direction: column;
}

.input-group {
  position: relative;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  border-bottom: 2px solid var(--wood-light);
  transition: 0.3s;
}

.icon-box {
  width: 40px;
  text-align: center;
  color: var(--wood-light);
  font-size: 1.1em;
}

.ink-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--wood-dark);
  padding: 10px;
  font-family: "Playfair Display", serif;
  font-size: 1rem;
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

/* ERROR */
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

/* BUTTON */
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
  margin-top: 5px;
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

/* FOOTER */
.auth-footer {
  margin-top: 25px;
  text-align: center;
  font-size: 0.95em;
  font-family: "Playfair Display", serif;
}

.text-muted {
  color: #795548;
  margin-right: 10px;
  font-style: italic;
}

.footer-link {
  color: var(--wood-dark);
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

/* Animations */
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
