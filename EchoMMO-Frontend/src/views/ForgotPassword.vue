<template>
  <div class="auth-page" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="auth-container">
      <div class="auth-header">
        <div class="sect-seal">
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
            <div class="form-group">
              <label><i class="fas fa-envelope"></i> Thư tín (Email)</label>
              <input
                v-model="email"
                type="email"
                placeholder="Nhập địa chỉ email đã đăng ký"
                required
              />
            </div>

            <button type="submit" class="btn-submit" :disabled="isLoading">
              <span v-if="!isLoading">GỬI BỒ CÂU</span>
              <span v-else
                ><i class="fas fa-spinner fa-spin"></i> Đang tìm...</span
              >
            </button>
          </form>

          <form
            v-else
            key="step2"
            @submit.prevent="resetPass"
            class="auth-form"
          >
            <div class="alert-scroll">
              <i class="fas fa-scroll"></i> Đã gửi mật thư đến hòm thư của bạn!
            </div>

            <div class="form-group">
              <label><i class="fas fa-shield-alt"></i> Mã Xác Nhận</label>
              <input
                v-model="otp"
                type="text"
                placeholder="Nhập mã OTP"
                required
                class="text-center"
                maxlength="6"
              />
            </div>

            <div class="form-group">
              <label><i class="fas fa-key"></i> Mật khẩu mới</label>
              <input
                v-model="newPassword"
                type="password"
                placeholder="Thiết lập mật khẩu mới"
                required
              />
            </div>

            <button
              type="submit"
              class="btn-submit success-mode"
              :disabled="isLoading"
            >
              <span v-if="!isLoading">THIẾT LẬP LẠI</span>
              <span v-else
                ><i class="fas fa-spinner fa-spin"></i> Đang khắc ấn...</span
              >
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
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/authStore"; // Giả sử bạn dùng store
import axiosClient from "../api/axiosClient";
import { getAssetUrl } from "@/utils/assetHelper";

const router = useRouter();
const bgImage = getAssetUrl("b_mountain.jpg"); // Dùng chung background

const step = ref(1);
const email = ref("");
const otp = ref("");
const newPassword = ref("");
const isLoading = ref(false);

const sendOtp = async () => {
  isLoading.value = true;
  try {
    // API gửi OTP
    await axiosClient.post("/auth/forgot-password", { email: email.value });

    // Giả lập delay một chút cho trải nghiệm user
    setTimeout(() => {
      step.value = 2;
      isLoading.value = false;
    }, 800);
  } catch (e) {
    alert(
      e.response?.data?.message ||
        "Không tìm thấy địa chỉ thư tín này trong giang hồ.",
    );
    isLoading.value = false;
  }
};

const resetPass = async () => {
  isLoading.value = true;
  try {
    // API Reset Pass
    await axiosClient.post("/auth/reset-password", {
      email: email.value,
      otp: otp.value,
      newPassword: newPassword.value,
    });
    alert("Khôi phục thành công. Mời đại hiệp đăng nhập lại.");
    router.push("/login");
  } catch (e) {
    alert(
      e.response?.data?.message ||
        "Thất bại: Mã xác nhận không đúng hoặc đã hết hạn.",
    );
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* =========================================
   1. CẤU TRÚC CHÍNH (FULL SCREEN SCROLL FIX)
   ========================================= */
.auth-page {
  height: 100vh;
  width: 100%;
  overflow-y: auto; /* Cho phép cuộn */

  display: flex;
  padding: 20px 15px;
  box-sizing: border-box;

  background-repeat: no-repeat;
  background-position: center center;
  background-size: cover;
  background-attachment: fixed;
  position: relative;

  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

.auth-page::before {
  content: "";
  position: fixed; /* Che toàn màn hình */
  inset: 0;
  background: rgba(0, 0, 0, 0.65);
  z-index: 1;
  pointer-events: none;
}

.auth-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 450px;

  /* CĂN GIỮA & SCROLL */
  margin: auto;

  background: rgba(30, 20, 15, 0.95);
  padding: 40px 35px;
  border-radius: 8px;
  border: 2px solid #5d4037;

  box-shadow: 0 0 25px rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(4px);
  color: #f3f4f6;
}

/* =========================================
   2. HEADER & CON DẤU
   ========================================= */
.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

/* Con dấu trang trí */
.sect-seal {
  width: 60px;
  height: 60px;
  margin: 0 auto 10px;
  border: 2px solid #fbc02d;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(251, 192, 45, 0.1);
  box-shadow: 0 0 10px rgba(251, 192, 45, 0.3);
}

.seal-char {
  font-family: "UTM Thu Phap Thien An", cursive;
  font-size: 2.2rem;
  color: #fbc02d;
  line-height: 1;
}

.title-ink {
  margin: 5px 0;
  color: #fbc02d;
  font-family: "UTM Thu Phap Thien An", cursive;
  font-size: 2.8rem;
  font-weight: normal;
  letter-spacing: 2px;
  text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.8);
  white-space: nowrap;
}

.subtitle {
  color: #a1887f;
  font-size: 1rem;
  margin-top: 5px;
  font-style: italic;
}

/* =========================================
   3. FORM INPUTS
   ========================================= */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #ffca28;
  font-weight: 600;
  font-size: 0.9rem;
  text-transform: uppercase;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid #5d4037;
  border-radius: 4px;
  color: #fff;
  font-size: 1rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
  font-family: sans-serif;
}
.form-group input:focus {
  outline: none;
  border-color: #fbc02d;
  background: rgba(0, 0, 0, 0.6);
  box-shadow: 0 0 8px rgba(251, 192, 45, 0.4);
}
.text-center {
  text-align: center;
  letter-spacing: 3px;
}

/* Thông báo đã gửi mail */
.alert-scroll {
  background: rgba(251, 192, 45, 0.1);
  color: #ffe082;
  padding: 10px;
  border: 1px solid #fbc02d;
  border-radius: 4px;
  font-size: 0.95rem;
  text-align: center;
  margin-bottom: 5px;
}
.alert-scroll i {
  margin-right: 5px;
}

/* =========================================
   4. BUTTON
   ========================================= */
.btn-submit {
  width: 100%;
  padding: 10px 10px 14px 10px;
  background: linear-gradient(to bottom, #d32f2f, #b71c1c);
  color: #fff;
  border: 1px solid #e53935;
  border-radius: 4px;

  font-family: "UTM Thu Phap Thien An", cursive;
  font-size: 2rem;
  line-height: 1;
  white-space: nowrap;

  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.btn-submit:hover {
  background: linear-gradient(to bottom, #e53935, #c62828);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(183, 28, 28, 0.4);
}

.btn-submit:disabled {
  background: #4e342e;
  border-color: #3e2723;
  color: #8d6e63;
  cursor: not-allowed;
  transform: none;
}

/* Nút thành công (Bước 2) đổi màu chút cho khác biệt */
.btn-submit.success-mode {
  background: linear-gradient(to bottom, #f57f17, #fbc02d);
  border-color: #fdd835;
  color: #3e2723; /* Chữ màu tối trên nền vàng */
  text-shadow: none;
}
.btn-submit.success-mode:hover {
  background: linear-gradient(to bottom, #fbc02d, #fdd835);
}

/* =========================================
   5. FOOTER & TRANSITION
   ========================================= */
.auth-footer {
  margin-top: 30px;
  text-align: center;
}

.footer-link {
  color: #fbc02d;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}
.footer-link:hover {
  color: #fff;
  text-decoration: underline;
}

/* Animation chuyển form */
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
