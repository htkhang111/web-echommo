<template>
  <div class="auth-page" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="auth-container">
      <div class="auth-header">
        <img :src="appLogo" alt="Logo" class="logo" />
        <h2>Ghi Danh</h2>
        <p>Gia nhập giang hồ, xưng bá võ lâm</p>
      </div>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label><i class="fas fa-id-card"></i> Họ và Tên</label>
          <input
            v-model="form.fullName"
            type="text"
            placeholder="Nhập tên hiển thị"
            required
          />
        </div>
        <div class="form-group">
          <label><i class="fas fa-user"></i> Tài khoản</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="Tên đăng nhập"
            required
          />
        </div>
        <div class="form-group">
          <label><i class="fas fa-envelope"></i> Email</label>
          <input
            v-model="form.email"
            type="email"
            placeholder="Địa chỉ email"
            required
          />
        </div>
        <div class="form-group">
          <label><i class="fas fa-lock"></i> Mật khẩu</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="Nhập mật khẩu"
            required
          />
        </div>
        <div class="form-group">
          <label><i class="fas fa-lock"></i> Xác nhận mật khẩu</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="Nhập lại mật khẩu"
            required
          />
        </div>

        <button
          type="submit"
          class="btn-submit"
          :disabled="authStore.isLoading"
        >
          <span v-if="authStore.isLoading"
            ><i class="fas fa-spinner fa-spin"></i> Đang xử lý...</span
          >
          <span v-else>KHỞI TẠO NHÂN VẬT</span>
        </button>
      </form>

      <div class="auth-footer">
        <p>
          Đã có tài khoản? <router-link to="/login">Đăng nhập ngay</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/authStore";
import { useNotificationStore } from "../stores/notificationStore";
import { getAppLogo, getAssetUrl } from "@/utils/assetHelper"; // [FIX]

const authStore = useAuthStore();
const notiStore = useNotificationStore();
const router = useRouter();

const appLogo = getAppLogo();
const bgImage = getAssetUrl("b_mountain.jpg");

const form = reactive({ username: "", password: "", email: "", fullName: "" });
const confirmPassword = ref("");

const handleRegister = async () => {
  if (form.password !== confirmPassword.value) {
    notiStore.showToast("Mật khẩu xác nhận không khớp!", "error");
    return;
  }
  if (form.password.length < 6) {
    notiStore.showToast("Mật khẩu phải có ít nhất 6 ký tự!", "error");
    return;
  }
  try {
    const success = await authStore.register(form);
    if (success) {
      notiStore.showToast(
        `Chào mừng đại hiệp ${form.username} gia nhập giang hồ!`,
        "success",
      );
      router.push("/");
    }
  } catch (error) {
    let msg = error.response?.data?.message;
    if (!msg && typeof error.response?.data === "string")
      msg = error.response.data;
    if (!msg) msg = "Đăng ký thất bại! Vui lòng kiểm tra lại thông tin.";
    notiStore.showToast(msg, "error");
  }
};
</script>

<style scoped>
/* =========================================
   1. CẤU TRÚC CHÍNH (FIX LỖI SCROLL TRIỆT ĐỂ)
   ========================================= */
.auth-page {
  /* 1. Cố định chiều cao trang bằng đúng màn hình */
  height: 100vh;
  width: 100%;

  /* 2. Bắt buộc tạo thanh cuộn nếu nội dung dài hơn màn hình */
  overflow-y: auto;

  display: flex;

  /* 3. Thêm padding để khi cuộn xuống dưới cùng không bị sát mép */
  padding: 20px 15px;
  box-sizing: border-box;

  /* Background settings */
  background-repeat: no-repeat;
  background-position: center center;
  background-size: cover;
  background-attachment: fixed; /* Giữ hình nền đứng yên khi cuộn */
  position: relative;

  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* Lớp phủ tối */
.auth-page::before {
  content: "";
  /* Dùng fixed để lớp phủ luôn che toàn màn hình kể cả khi cuộn */
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.65);
  z-index: 1;
  /* Đảm bảo lớp phủ không che mất thanh cuộn (nếu có) */
  pointer-events: none;
}

/* Khung chứa nội dung */
.auth-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 480px;

  /* --- [QUAN TRỌNG] FIX SCROLL --- */
  /* margin: auto giúp căn giữa khi đủ chỗ, và đẩy nội dung đi khi thiếu chỗ */
  margin: auto;
  /* ------------------------------- */

  background: rgba(30, 20, 15, 0.95);
  padding: 30px 35px;
  border-radius: 8px;
  border: 2px solid #5d4037;

  box-shadow: 0 0 25px rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(4px);
  color: #f3f4f6;
}

/* =========================================
   2. HEADER (GHI DANH)
   ========================================= */
.auth-header {
  text-align: center;
  margin-bottom: 25px;
}

.logo {
  width: 80px;
  margin-bottom: 10px;
  filter: drop-shadow(0 0 5px rgba(251, 192, 45, 0.3));
}

.auth-header h2 {
  margin: 5px 0;
  color: #fbc02d;
  font-family: "UTM Thu Phap Thien An", cursive;
  white-space: nowrap;
  font-size: 3rem;
  font-weight: normal;
  letter-spacing: 2px;
  text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.8);
  line-height: 1.2;
}

.auth-header p {
  color: #a1887f;
  font-size: 1rem;
  margin-top: 5px;
  font-style: italic;
}

/* =========================================
   3. FORM INPUTS
   ========================================= */
.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #ffca28;
  font-weight: 600;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-group input {
  width: 100%;
  padding: 10px 15px;
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

.form-group input::placeholder {
  color: #8d6e63;
}

/* =========================================
   4. BUTTON (KHỞI TẠO NHÂN VẬT)
   ========================================= */
.btn-submit {
  width: 100%;
  padding: 8px 10px 12px 10px;
  margin-top: 15px;
  background: linear-gradient(to bottom, #d32f2f, #b71c1c);
  color: #fff;
  border: 1px solid #e53935;
  border-radius: 4px;
  font-family: "UTM Thu Phap Thien An", cursive;
  font-size: 1.8rem;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
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

/* =========================================
   5. FOOTER
   ========================================= */
.auth-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 0.95rem;
  color: #a1887f;
}

.auth-footer a {
  color: #fbc02d;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.auth-footer a:hover {
  color: #fff;
  text-decoration: underline;
  text-shadow: 0 0 5px #fbc02d;
}
</style>
