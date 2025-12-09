<template>
  <div class="game-page">
    <div class="section-title">
      <h2>Báo Cáo Lỗi</h2>
      <div class="title-decor"></div>
    </div>

    <div class="wuxia-panel form-container">
      <p class="desc">
        Phát hiện lỗi game? Hãy báo cho chúng tôi để nhận thưởng Linh Thạch.
      </p>

      <form @submit.prevent="submitReport">
        <div class="form-group">
          <label>Tiêu Đề Lỗi</label>
          <input
            type="text"
            placeholder="Ví dụ: Lỗi không nhặt được vật phẩm..."
            v-model="form.title"
            required
            :disabled="isSending"
          />
        </div>

        <div class="form-group">
          <label>Mô Tả Chi Tiết</label>
          <textarea
            rows="5"
            placeholder="Mô tả các bước để tái hiện lỗi..."
            v-model="form.description"
            required
            :disabled="isSending"
          ></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" class="submit-btn" :disabled="isSending">
            <span v-if="isSending"
              ><i class="fas fa-spinner fa-spin"></i> Đang Gửi...</span
            >
            <span v-else>Gửi Tấu Chương</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axios from "axios"; // Đảm bảo đã cài: npm install axios
import { useAuthStore } from "../stores/authStore"; // Lấy token nếu cần

// Cấu hình API Endpoint của bạn
const API_URL = "http://localhost:8080/api/report/send";

const authStore = useAuthStore();
const form = ref({ title: "", description: "" });
const isSending = ref(false);

const submitReport = async () => {
  if (!form.value.title || !form.value.description) return;

  isSending.value = true;

  try {
    // Gọi về Backend Spring Boot
    const response = await axios.post(
      API_URL,
      {
        title: form.value.title,
        description: form.value.description,
      },
      {
        // Đính kèm Token để Backend biết ai gửi (nếu cần)
        headers: {
          Authorization: `Bearer ${authStore.token}`,
        },
      },
    );

    if (response.status === 200 || response.status === 201) {
      alert("Cảm ơn đạo hữu! Tấu chương đã được gửi về triều đình.");
      form.value = { title: "", description: "" }; // Reset form
    }
  } catch (error) {
    console.error("Lỗi gửi báo cáo:", error);
    alert("Có lỗi xảy ra khi kết nối tới máy chủ. Vui lòng thử lại sau.");
  } finally {
    isSending.value = false;
  }
};
</script>

<style scoped>
/* Style Giữ nguyên theo phong cách Tiên Hiệp cũ */
.game-page {
  padding: 30px;
  max-width: 600px;
  margin: 0 auto;
  animation: fadeIn 0.5s ease;
}
.section-title {
  text-align: center;
  margin-bottom: 30px;
}
.section-title h2 {
  font-family: "Cinzel", serif;
  font-size: 2em;
  color: #fbc02d;
  margin: 0;
}
.title-decor {
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #b71c1c, transparent);
  margin: 10px auto;
}

.wuxia-panel {
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #5d4037;
  padding: 30px;
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
}
.desc {
  color: #a1887f;
  text-align: center;
  margin-bottom: 20px;
  font-style: italic;
}

.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  color: #fbc02d;
  margin-bottom: 8px;
  font-weight: bold;
  font-family: "Noto Serif TC", serif;
}
.form-group input,
.form-group textarea {
  width: 100%;
  background: #1a1510;
  border: 1px solid #5d4037;
  color: #d7ccc8;
  padding: 10px;
  border-radius: 4px;
  font-family: inherit;
  transition: 0.3s;
}
.form-group input:focus,
.form-group textarea:focus {
  border-color: #fbc02d;
  outline: none;
  box-shadow: 0 0 8px rgba(251, 192, 45, 0.2);
}
.form-group input:disabled,
.form-group textarea:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn {
  width: 100%;
  background: #b71c1c;
  color: #fff;
  padding: 12px;
  border: 1px solid #ffc107;
  font-family: "Cinzel", serif;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
  font-size: 1.1em;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.submit-btn:hover:not(:disabled) {
  background: #d32f2f;
  box-shadow: 0 0 15px #b71c1c;
  transform: translateY(-2px);
}
.submit-btn:disabled {
  background: #5d4037;
  border-color: #8d6e63;
  cursor: not-allowed;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
