<template>
  <div class="page-container wuxia-recovery">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
      <div class="paper-overlay"></div>
    </div>

    <div class="recovery-wrapper">
      <div class="auth-panel">
        <div class="decor-corner top-left"></div>
        <div class="decor-corner top-right"></div>
        <div class="decor-corner bottom-left"></div>
        <div class="decor-corner bottom-right"></div>

        <div class="auth-header">
          <div class="sect-seal warning-mode">
            <span class="seal-char">密</span>
          </div>
          <h2 class="title-ink">KHÔI PHỤC</h2>
          <p class="subtitle">Tìm lại mật tịch thất lạc</p>
        </div>

        <div class="step-container">
          <transition name="slide-fade" mode="out-in">
            <form
              v-if="step === 1"
              key="step1"
              @submit.prevent="sendOtp"
              class="auth-form"
            >
              <div class="input-group">
                <div class="icon-box">
                  <i class="fas fa-envelope"></i>
                </div>
                <input
                  v-model="email"
                  type="email"
                  placeholder="Địa chỉ thư tín (Email)"
                  required
                  class="ink-input"
                />
              </div>

              <button
                type="submit"
                class="btn-seal-submit"
                :disabled="isLoading"
              >
                <span v-if="!isLoading">
                  GỬI BỒ CÂU <i class="fas fa-dove"></i>
                </span>
                <span v-else>
                  <i class="fas fa-circle-notch fa-spin"></i> ĐANG TÌM KIẾM...
                </span>
              </button>
            </form>

            <form
              v-else
              key="step2"
              @submit.prevent="resetPass"
              class="auth-form"
            >
              <div class="alert-scroll">
                <i class="fas fa-scroll"></i> Đã gửi mật thư đến hòm thư của
                bạn.
              </div>

              <div class="input-group">
                <div class="icon-box"><i class="fas fa-shield-alt"></i></div>
                <input
                  v-model="otp"
                  type="text"
                  placeholder="Mã Xác Nhận (OTP)"
                  required
                  class="ink-input text-center"
                  maxlength="6"
                />
              </div>

              <div class="input-group">
                <div class="icon-box"><i class="fas fa-key"></i></div>
                <input
                  v-model="newPassword"
                  type="password"
                  placeholder="Mật Khẩu Mới"
                  required
                  class="ink-input"
                />
              </div>

              <button
                type="submit"
                class="btn-seal-submit success-mode"
                :disabled="isLoading"
              >
                <span v-if="!isLoading">
                  THIẾT LẬP LẠI <i class="fas fa-pen-nib"></i>
                </span>
                <span v-else>
                  <i class="fas fa-circle-notch fa-spin"></i> ĐANG KHẮC ẤN...
                </span>
              </button>
            </form>
          </transition>
        </div>

        <div class="auth-footer">
          <router-link to="/login" class="footer-link">
            <i class="fas fa-undo"></i> Quay lại
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axiosClient from "../api/axiosClient";
import { useRouter } from "vue-router";

const router = useRouter();
const step = ref(1);
const email = ref("");
const otp = ref("");
const newPassword = ref("");
const isLoading = ref(false);

const sendOtp = async () => {
  isLoading.value = true;
  try {
    await axiosClient.post("/auth/forgot-password", { email: email.value });
    // Giả lập delay
    setTimeout(() => {
      step.value = 2;
      isLoading.value = false;
    }, 1000);
  } catch (e) {
    alert(
      e.response?.data || "Gửi thất bại: Không tìm thấy địa chỉ thư tín này.",
    );
    isLoading.value = false;
  }
};

const resetPass = async () => {
  isLoading.value = true;
  try {
    await axiosClient.post("/auth/reset-password", {
      email: email.value,
      otp: otp.value,
      newPassword: newPassword.value,
    });
    alert("Khôi phục thành công. Mời đại hiệp đăng nhập lại.");
    router.push("/login");
  } catch (e) {
    alert(e.response?.data || "Thất bại: Mã xác nhận không đúng.");
  } finally {
    isLoading.value = false;
  }
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
.wuxia-recovery {
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

.recovery-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  padding: 20px;
  display: flex;
  justify-content: center;
}

/* --- AUTH PANEL --- */
.auth-panel {
  width: 100%;
  max-width: 420px;
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

/* HEADER */
.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.sect-seal {
  width: 70px;
  height: 70px;
  margin: 0 auto 15px;
  border: 3px solid var(--gold); /* Màu vàng cam cho cảnh báo/khôi phục */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 160, 23, 0.1);
  box-shadow: 0 0 15px rgba(212, 160, 23, 0.2);
}

.seal-char {
  font-family: "Noto Serif TC", serif;
  font-size: 2.5rem;
  font-weight: 900;
  color: var(--gold);
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
  gap: 20px;
}

.input-group {
  position: relative;
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
  padding: 12px 10px;
  font-family: "Playfair Display", serif;
  font-size: 1.1em;
  font-weight: bold;
  outline: none;
}
.ink-input::placeholder {
  color: #a1887f;
  font-weight: normal;
}
.text-center {
  text-align: center;
  letter-spacing: 3px;
}

.input-group:focus-within {
  border-color: var(--gold);
}
.input-group:focus-within .icon-box {
  color: var(--gold);
}

/* ALERT SCROLL (Thông báo đã gửi mail) */
.alert-scroll {
  background: rgba(212, 160, 23, 0.15);
  color: #8d6e63;
  padding: 10px;
  border: 1px solid var(--gold);
  border-radius: 4px;
  font-size: 0.9em;
  text-align: center;
  font-weight: bold;
  margin-bottom: 10px;
}
.alert-scroll i {
  color: var(--gold);
  margin-right: 5px;
}

/* BUTTON */
.btn-seal-submit {
  width: 100%;
  padding: 12px;
  background: var(--gold); /* Mặc định là màu vàng (warning) */
  color: #fff;
  border: 2px solid #8d6e63;
  font-family: "Noto Serif TC", serif;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
  border-radius: 4px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
  text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.2);
}

.btn-seal-submit:hover {
  background: #ffb300;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
}

.btn-seal-submit.success-mode {
  background: var(--red-seal); /* Bước 2 đổi sang đỏ để chốt */
  border-color: #8a1c1c;
}
.btn-seal-submit.success-mode:hover {
  background: #d32f2f;
}

.btn-seal-submit:disabled {
  background: #9e9e9e;
  border-color: #757575;
  cursor: not-allowed;
  transform: none;
}

/* FOOTER */
.auth-footer {
  margin-top: 30px;
  text-align: center;
  font-size: 0.9em;
}

.footer-link {
  color: #5d4037;
  text-decoration: none;
  transition: 0.2s;
  font-weight: bold;
  font-family: "Playfair Display", serif;
}
.footer-link:hover {
  color: var(--red-seal);
}

/* TRANSITIONS */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
