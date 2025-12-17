<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <img src="@/assets/logo/Logo.png" alt="Logo" class="logo" />
        <h2>ĐĂNG NHẬP</h2>
        <p>Mừng đại hiệp quay trở lại</p>
      </div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label><i class="fas fa-user"></i> Tài khoản</label>
          <input v-model="form.username" type="text" placeholder="Nhập tên đăng nhập" required />
        </div>

        <div class="form-group">
          <label><i class="fas fa-lock"></i> Mật khẩu</label>
          <input v-model="form.password" type="password" placeholder="Nhập mật khẩu" required />
        </div>

        <button type="submit" class="btn-submit" :disabled="authStore.isLoading">
          <span v-if="authStore.isLoading"><i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...</span>
          <span v-else>VÀO GIANG HỒ</span>
        </button>
      </form>

      <div class="auth-footer">
        <p>Chưa có tài khoản? <router-link to="/register">Ghi danh ngay</router-link></p>
        <p style="margin-top: 10px;"><router-link to="/forgot-password" style="color: #90a4ae;">Quên mật khẩu?</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/authStore';
import { useNotificationStore } from '../stores/notificationStore'; // [THÊM]

const authStore = useAuthStore();
const notiStore = useNotificationStore(); // [THÊM]
const router = useRouter();

const form = reactive({
  username: '',
  password: ''
});

const handleLogin = async () => {
  try {
    const success = await authStore.login(form);
    if (success) {
      // [FIX] Thông báo đẹp khi đăng nhập thành công
      notiStore.showToast(`Chào mừng trở lại, ${authStore.user?.username}!`, "success");
      router.push('/');
    }
  } catch (error) {
    // [FIX] Thông báo lỗi đẹp
    const msg = error.response?.data?.message || "Sai tài khoản hoặc mật khẩu!";
    notiStore.showToast(msg, "error");
  }
};
</script>

<style scoped>
/* Dùng chung style với Register ở trên */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('@/assets/Background/b_mountain.jpg') no-repeat center center/cover;
  position: relative;
}
.auth-page::before { content: ''; position: absolute; inset: 0; background: rgba(0,0,0,0.7); }
.auth-container {
  position: relative; width: 100%; max-width: 400px;
  background: rgba(30, 20, 15, 0.9); padding: 40px;
  border-radius: 8px; border: 2px solid #5d4037;
  box-shadow: 0 0 20px rgba(0,0,0,0.8); backdrop-filter: blur(5px); color: #f3f4f6;
}
.auth-header { text-align: center; margin-bottom: 30px; }
.logo { width: 80px; margin-bottom: 10px; }
.auth-header h2 { margin: 10px 0 5px; color: #fbc02d; font-family: "Noto Serif TC"; font-size: 2rem; }
.auth-header p { color: #a1887f; font-size: 0.9rem; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; color: #fbc02d; font-weight: bold; }
.form-group input { width: 100%; padding: 12px; background: rgba(0,0,0,0.3); border: 1px solid #5d4037; border-radius: 4px; color: #fff; font-size: 1rem; box-sizing: border-box; }
.form-group input:focus { outline: none; border-color: #fbc02d; box-shadow: 0 0 5px rgba(251, 192, 45, 0.5); }
.btn-submit { width: 100%; padding: 12px; background: #b71c1c; color: #fff; border: none; border-radius: 4px; font-size: 1.1rem; font-weight: bold; cursor: pointer; transition: 0.3s; font-family: "Noto Serif TC"; }
.btn-submit:hover { background: #d32f2f; transform: translateY(-2px); }
.btn-submit:disabled { background: #5d4037; cursor: not-allowed; }
.auth-footer { margin-top: 20px; text-align: center; font-size: 0.9rem; color: #a1887f; }
.auth-footer a { color: #fbc02d; text-decoration: none; font-weight: bold; }
.auth-footer a:hover { text-decoration: underline; }
</style>