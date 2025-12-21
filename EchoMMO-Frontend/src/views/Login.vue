<template>
  <div class="auth-page" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="auth-container">
      <div class="auth-header">
        <img :src="appLogo" alt="Logo" class="logo" />
        <h2>Đăng Nhập</h2>
        <p>Mừng đại hiệp quay trở lại</p>
      </div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label><i class="fas fa-user"></i> Tài khoản</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="Nhập tên đăng nhập"
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

        <button
          type="submit"
          class="btn-submit"
          :disabled="authStore.isLoading"
        >
          <span v-if="authStore.isLoading"
            ><i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...</span
          >
          <span v-else>Vào Giang Hồ</span>
        </button>
      </form>

      <div class="auth-footer">
        <p>
          Chưa có tài khoản?
          <router-link to="/register">Ghi danh ngay</router-link>
        </p>
        <p style="margin-top: 10px">
          <router-link to="/forgot-password" style="color: #90a4ae"
            >Quên mật khẩu?</router-link
          >
        </p>
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
        <button @click="showBanModal = false" class="btn-close-ban">
          Đã hiểu
        </button>
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

// Lấy ảnh từ Helper thay vì import
const appLogo = getAppLogo();
const bgImage = getAssetUrl("b_mountain.jpg");

const showBanModal = ref(false);
const banData = reactive({ reason: "", time: "" });
const form = reactive({ username: "", password: "" });

const handleLogin = async () => {
  try {
    const success = await authStore.login(form);
    if (success) {
      notiStore.showToast(
        `Chào mừng trở lại, ${authStore.user?.username}!`,
        "success",
      );
      router.push("/");
    }
  } catch (error) {
    if (
      error.response &&
      error.response.status === 401 &&
      error.response.data.error === "BANNED"
    ) {
      banData.reason = error.response.data.reason || "Vi phạm quy tắc";
      banData.time = error.response.data.bannedAt || "Vĩnh viễn";
      showBanModal.value = true;
    } else {
      const msg =
        error.response?.data?.message || "Sai tài khoản hoặc mật khẩu!";
      notiStore.showToast(msg, "error");
    }
  }
};
</script>

<style scoped>
/* =========================================
   1. CẤU TRÚC CHÍNH (LAYOUT & BACKGROUND)
   ========================================= */
.auth-page {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;

  /* Hình nền xử lý ở template, class này định vị */
  background-repeat: no-repeat;
  background-position: center center;
  background-size: cover;
  position: relative;

  /* Font mặc định cho toàn trang (tránh lỗi chữ nhỏ) */
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* Lớp phủ tối màu lên background để làm nổi bật khung đăng nhập */
.auth-page::before {
  content: "";
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.65); /* Độ tối 65% */
  z-index: 1;
}

/* Khung chứa nội dung (Container) */
.auth-container {
  position: relative;
  z-index: 2; /* Nằm trên lớp phủ */
  width: 100%;
  max-width: 450px; /* Tăng nhẹ độ rộng để tiêu đề thoải mái hơn */

  /* Màu nền: Nâu đen trong suốt (kiểu gỗ mục/sơn mài) */
  background: rgba(30, 20, 15, 0.95);

  padding: 40px 35px;
  border-radius: 8px;

  /* Viền nâu đất */
  border: 2px solid #5d4037;

  /* Hiệu ứng bóng đổ và làm mờ hậu cảnh */
  box-shadow: 0 0 25px rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(4px);

  color: #f3f4f6;
}

/* =========================================
   2. HEADER (LOGO & TIÊU ĐỀ)
   ========================================= */
.auth-header {
  text-align: center;
  margin-bottom: 35px;
}

.logo {
  width: 90px;
  margin-bottom: 15px;
  filter: drop-shadow(0 0 5px rgba(251, 192, 45, 0.3));
}

/* Tiêu đề chính "ĐĂNG NHẬP" - Dùng Font Thư Pháp */
.auth-header h2 {
  margin: 5px 0;
  color: #fbc02d; /* Màu vàng kim */

  font-family: "UTM Thu Phap Thien An", cursive;

  /* --- [ĐÃ SỬA] Đảm bảo nằm ngang 1 dòng --- */
  white-space: nowrap;
  font-size: 3rem; /* Kích thước chữ lớn */
  /* ----------------------------------------- */

  font-weight: normal;
  letter-spacing: 2px;
  text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.8);
  line-height: 1.2;
}

/* Dòng chào mừng nhỏ - Dùng Font thường */
.auth-header p {
  color: #a1887f; /* Màu nâu nhạt */
  font-size: 1rem;
  margin-top: 5px;
  font-style: italic;
}

/* =========================================
   3. FORM INPUTS
   ========================================= */
.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #ffca28; /* Vàng sáng hơn chút */
  font-weight: 600;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;

  /* Nền input trong suốt tối */
  background: rgba(0, 0, 0, 0.4);

  border: 1px solid #5d4037;
  border-radius: 4px;
  color: #fff;
  font-size: 1rem;
  box-sizing: border-box;
  transition: all 0.3s ease;

  /* Font nhập liệu phải rõ ràng (Arial/System) */
  font-family: sans-serif;
}

/* Hiệu ứng khi bấm vào input */
.form-group input:focus {
  outline: none;
  border-color: #fbc02d;
  background: rgba(0, 0, 0, 0.6);
  box-shadow: 0 0 8px rgba(251, 192, 45, 0.4);
}

/* Placeholder màu tối hơn */
.form-group input::placeholder {
  color: #8d6e63;
}

/* =========================================
   4. BUTTONS (NÚT BẤM)
   ========================================= */
/* Nút chính "VÀO GIANG HỒ" - Dùng Font Thư Pháp */
.btn-submit {
  width: 100%;
  padding: 8px 10px 14px 10px; /* Padding bottom nhiều hơn để cân chữ */
  margin-top: 10px;

  background: #b71c1c; /* Đỏ đậm */
  background: linear-gradient(to bottom, #d32f2f, #b71c1c);

  color: #fff;
  border: 1px solid #e53935;
  border-radius: 4px;

  font-family: "UTM Thu Phap Thien An", cursive;
  font-size: 2.2rem; /* Chữ to, rõ */
  line-height: 1;

  cursor: pointer;
  transition: all 0.2s ease-in-out;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.btn-submit:hover {
  background: linear-gradient(to bottom, #e53935, #c62828);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(183, 28, 28, 0.4);
}

.btn-submit:active {
  transform: translateY(0);
}

.btn-submit:disabled {
  background: #4e342e;
  border-color: #3e2723;
  color: #8d6e63;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* =========================================
   5. FOOTER (LINK)
   ========================================= */
.auth-footer {
  margin-top: 25px;
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

/* =========================================
   6. MODAL (TÀI KHOẢN BỊ KHÓA)
   ========================================= */
.ban-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.ban-modal {
  background: #2d1b1b;
  border: 2px solid #b71c1c;
  padding: 30px;
  border-radius: 8px;
  width: 90%;
  max-width: 450px;
  text-align: center;
  color: #fff;
  box-shadow: 0 0 30px rgba(183, 28, 28, 0.3);
}

.ban-icon {
  font-size: 3.5rem;
  margin-bottom: 15px;
  animation: shake 0.5s;
}

/* Tiêu đề Modal - Dùng Font Thư Pháp */
.ban-modal h3 {
  color: #ef5350;
  margin-bottom: 10px;

  font-family: "UTM Thu Phap Thien An", cursive;
  font-size: 2.5rem;
  font-weight: normal;
}

.ban-msg {
  color: #cfd8dc;
  margin-bottom: 20px;
  font-size: 1.1rem;
}

.ban-details {
  background: rgba(0, 0, 0, 0.4);
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 25px;
  text-align: left;
  border-left: 3px solid #b71c1c;
}

.ban-details p {
  margin: 8px 0;
  color: #e0e0e0;
  font-size: 1rem;
}

.ban-details strong {
  color: #fbc02d;
}

/* Nút đóng modal - Dùng Font Thư Pháp */
.btn-close-ban {
  background: #5d4037;
  color: white;
  border: 1px solid #795548;
  padding: 8px 35px 12px 35px;
  border-radius: 4px;
  cursor: pointer;

  font-family: "UTM Thu Phap Thien An", cursive;
  font-size: 1.6rem;
  line-height: 1;
  transition: 0.3s;
}

.btn-close-ban:hover {
  background: #b71c1c;
  border-color: #d32f2f;
}

/* Animation rung cho icon cấm */
@keyframes shake {
  0% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(15deg);
  }
  50% {
    transform: rotate(0deg);
  }
  75% {
    transform: rotate(-15deg);
  }
  100% {
    transform: rotate(0deg);
  }
}
</style>
