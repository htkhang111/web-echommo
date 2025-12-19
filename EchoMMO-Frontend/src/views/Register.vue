<template>
  <div class="auth-page" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="auth-container">
      <div class="auth-header">
        <img :src="appLogo" alt="Logo" class="logo" />
        <h2>GHI DANH</h2>
        <p>Gia nhập giang hồ, xưng bá võ lâm</p>
      </div>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label><i class="fas fa-id-card"></i> Họ và Tên</label>
          <input v-model="form.fullName" type="text" placeholder="Nhập tên hiển thị" required />
        </div>
        <div class="form-group">
          <label><i class="fas fa-user"></i> Tài khoản</label>
          <input v-model="form.username" type="text" placeholder="Tên đăng nhập" required />
        </div>
        <div class="form-group">
          <label><i class="fas fa-envelope"></i> Email</label>
          <input v-model="form.email" type="email" placeholder="Địa chỉ email" required />
        </div>
        <div class="form-group">
          <label><i class="fas fa-lock"></i> Mật khẩu</label>
          <input v-model="form.password" type="password" placeholder="Nhập mật khẩu" required />
        </div>
        <div class="form-group">
          <label><i class="fas fa-lock"></i> Xác nhận mật khẩu</label>
          <input v-model="confirmPassword" type="password" placeholder="Nhập lại mật khẩu" required />
        </div>

        <button type="submit" class="btn-submit" :disabled="authStore.isLoading">
          <span v-if="authStore.isLoading"><i class="fas fa-spinner fa-spin"></i> Đang xử lý...</span>
          <span v-else>KHỞI TẠO NHÂN VẬT</span>
        </button>
      </form>

      <div class="auth-footer">
        <p>Đã có tài khoản? <router-link to="/login">Đăng nhập ngay</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/authStore';
import { useNotificationStore } from '../stores/notificationStore';
import { getAppLogo, getAssetUrl } from '@/utils/assetHelper'; // [FIX]

const authStore = useAuthStore();
const notiStore = useNotificationStore();
const router = useRouter();

const appLogo = getAppLogo();
const bgImage = getAssetUrl("b_mountain.jpg");

const form = reactive({ username: '', password: '', email: '', fullName: '' });
const confirmPassword = ref('');

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
      notiStore.showToast(`Chào mừng đại hiệp ${form.username} gia nhập giang hồ!`, "success");
      router.push('/');
    }
  } catch (error) {
    let msg = error.response?.data?.message;
    if (!msg && typeof error.response?.data === 'string') msg = error.response.data;
    if (!msg) msg = "Đăng ký thất bại! Vui lòng kiểm tra lại thông tin.";
    notiStore.showToast(msg, "error");
  }
};
</script>

<style scoped>
/* Copy y nguyên style từ Login.vue ở trên (bao gồm .auth-page bỏ background-image cứng) */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-repeat: no-repeat;
  background-position: center center;
  background-size: cover;
  position: relative;
}
.auth-page::before { content: ''; position: absolute; inset: 0; background: rgba(0,0,0,0.7); }
.auth-container {
  position: relative; width: 100%; max-width: 450px;
  background: rgba(30, 20, 15, 0.9); padding: 40px;
  border-radius: 8px; border: 2px solid #5d4037;
  box-shadow: 0 0 20px rgba(0,0,0,0.8); backdrop-filter: blur(5px); color: #f3f4f6;
}
.auth-header { text-align: center; margin-bottom: 25px; }
.logo { width: 80px; margin-bottom: 10px; }
.auth-header h2 { margin: 10px 0 5px; color: #fbc02d; font-family: "Noto Serif TC"; font-size: 2rem; }
.auth-header p { color: #a1887f; font-size: 0.9rem; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #fbc02d; font-weight: bold; font-size: 0.9rem; }
.form-group input { width: 100%; padding: 10px; background: rgba(0,0,0,0.3); border: 1px solid #5d4037; border-radius: 4px; color: #fff; font-size: 1rem; box-sizing: border-box; }
.form-group input:focus { outline: none; border-color: #fbc02d; box-shadow: 0 0 5px rgba(251, 192, 45, 0.5); }
.btn-submit { width: 100%; padding: 12px; background: #b71c1c; color: #fff; border: none; border-radius: 4px; font-size: 1.1rem; font-weight: bold; cursor: pointer; transition: 0.3s; font-family: "Noto Serif TC"; margin-top: 10px; }
.btn-submit:hover { background: #d32f2f; transform: translateY(-2px); }
.btn-submit:disabled { background: #5d4037; cursor: not-allowed; }
.auth-footer { margin-top: 20px; text-align: center; font-size: 0.9rem; color: #a1887f; }
.auth-footer a { color: #fbc02d; text-decoration: none; font-weight: bold; }
.auth-footer a:hover { text-decoration: underline; }
</style>