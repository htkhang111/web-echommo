<template>
  <div class="auth-page" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="auth-container">
      <div class="auth-header">
        <img :src="appLogo" alt="Logo" class="logo" />
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

    <div v-if="showBanModal" class="ban-modal-overlay">
      <div class="ban-modal">
        <div class="ban-icon">🚫</div>
        <h3>TÀI KHOẢN BỊ KHÓA</h3>
        <p class="ban-msg">Đại hiệp đã bị cấm túc khỏi giang hồ!</p>
        <div class="ban-details">
          <p><strong>Lý do:</strong> {{ banData.reason }}</p>
          <p><strong>Thời gian:</strong> {{ banData.time }}</p>
        </div>
        <button @click="showBanModal = false" class="btn-close-ban">Đã hiểu</button>
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

// Lấy ảnh từ Helper thay vì import
const appLogo = getAppLogo();
const bgImage = getAssetUrl("b_mountain.jpg");

const showBanModal = ref(false);
const banData = reactive({ reason: '', time: '' });
const form = reactive({ username: '', password: '' });

const handleLogin = async () => {
  try {
    const success = await authStore.login(form);
    if (success) {
      notiStore.showToast(`Chào mừng trở lại, ${authStore.user?.username}!`, "success");
      router.push('/');
    }
  } catch (error) {
    if (error.response && error.response.status === 401 && error.response.data.error === 'BANNED') {
        banData.reason = error.response.data.reason || "Vi phạm quy tắc";
        banData.time = error.response.data.bannedAt || "Vĩnh viễn";
        showBanModal.value = true;
    } else {
        const msg = error.response?.data?.message || "Sai tài khoản hoặc mật khẩu!";
        notiStore.showToast(msg, "error");
    }
  }
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background-repeat: no-repeat; background-position: center center; background-size: cover;
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
.ban-modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.85); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.ban-modal { background: #2d1b1b; border: 2px solid #b71c1c; padding: 30px; border-radius: 10px; width: 90%; max-width: 400px; text-align: center; color: #fff; }
.ban-icon { font-size: 3rem; margin-bottom: 15px; }
.ban-modal h3 { color: #b71c1c; font-family: "Noto Serif TC"; margin-bottom: 10px; }
.ban-msg { color: #a1887f; margin-bottom: 20px; }
.ban-details { background: rgba(0, 0, 0, 0.3); padding: 15px; border-radius: 5px; margin-bottom: 20px; text-align: left; }
.ban-details p { margin: 5px 0; color: #e0e0e0; }
.ban-details strong { color: #fbc02d; }
.btn-close-ban { background: #5d4037; color: white; border: none; padding: 10px 30px; border-radius: 5px; cursor: pointer; font-weight: bold; }
.btn-close-ban:hover { background: #b71c1c; }
</style>