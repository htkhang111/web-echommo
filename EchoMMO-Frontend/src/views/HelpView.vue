<template>
  <div class="help-view page-container">
    <div class="view-header">
      <h2 class="header-title">LIÊN LẠC QUAN PHỦ</h2>
      <div class="header-decor"></div>
    </div>

    <div class="paper-scroll-container custom-scroll">
      <div class="intro-section">
        <p class="npc-text">
          "Tại hạ là Tiếp Dẫn Sứ. Hãy chọn vấn đề bên dưới, ta sẽ lập tức kết
          nối đường dây nóng (Chat) trực tiếp với Quan Phủ đại nhân."
        </p>
      </div>

      <div class="form-container">
        <div class="form-group">
          <label class="label-ink">Loại Sự Vụ:</label>
          <div class="custom-select-wrapper">
            <select v-model="form.category" class="input-wood select">
              <option value="" disabled selected>-- Chọn vấn đề --</option>
              <option value="BUG">Báo Lỗi (Bug/Lag)</option>
              <option value="PAYMENT">Vấn đề Nạp/Ngân Lượng</option>
              <option value="REPORT">Tố Cáo Người Chơi</option>
              <option value="ACCOUNT">Tài Khoản/Mật Khẩu</option>
              <option value="OTHER">Góp Ý Khác</option>
            </select>
            <i class="fas fa-caret-down select-arrow"></i>
          </div>
        </div>

        <div class="form-group">
          <label class="label-ink">Chi Tiết Sự Việc:</label>
          <textarea
            v-model="form.description"
            class="input-wood textarea"
            placeholder="Ví dụ: Tôi bị kẹt ở map Lạc Dương..."
            rows="6"
          ></textarea>
        </div>

        <div class="action-area">
          <button
            class="btn-submit-seal"
            @click="startChatWithAdmin"
            :disabled="isSubmitting || !form.category || !form.description"
          >
            <span v-if="!isSubmitting"
              ><i class="fas fa-comments"></i> BẮT ĐẦU CHAT VỚI ADMIN</span
            >
            <span v-else
              ><i class="fas fa-spinner fa-spin"></i> Đang kết nối...</span
            >
          </button>
        </div>
      </div>

      <div class="faq-section">
        <div class="faq-title">Lưu ý:</div>
        <ul class="faq-list">
          <li>Admin không bao giờ hỏi mật khẩu của bạn.</li>
          <li>Vấn đề nạp thẻ vui lòng cung cấp mã giao dịch.</li>
          <li>Cố tình báo tin giả sẽ bị cấm ngôn.</li>
        </ul>
      </div>
    </div>
    <ChatWidget />
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useChatStore } from "../stores/chatStore"; // 1. Import ChatStore
import ChatWidget from "../components/ChatWidget.vue";
const chatStore = useChatStore(); // 2. Khởi tạo store
const isSubmitting = ref(false);

const form = reactive({
  category: "",
  description: "",
});

const getCategoryLabel = (code) => {
  const map = {
    BUG: "Báo Lỗi",
    PAYMENT: "Nạp Thẻ",
    REPORT: "Tố Cáo",
    ACCOUNT: "Tài Khoản",
    OTHER: "Khác",
  };
  return map[code] || code;
};

// --- HÀM XỬ LÝ CHÍNH ---
const startChatWithAdmin = async () => {
  if (!form.category || !form.description) return;

  isSubmitting.value = true;

  try {
    // 1. Tạo nội dung tin nhắn từ Form
    const messageContent = `[${getCategoryLabel(form.category)}] ${form.description}`;

    // 2. GIẢ LẬP GỬI TIN NHẮN (Lưu vào localStorage để ChatWidget đọc được)
    const newMsg = {
      id: Date.now(),
      text: messageContent,
      sender: "me", // Đánh dấu là tin nhắn của mình
      timestamp: new Date().toISOString(),
    };

    // Lấy lịch sử chat cũ
    const chatKey = "chat_history_admin";
    const history = JSON.parse(localStorage.getItem(chatKey) || "[]");

    // Thêm tin nhắn mới vào
    history.push(newMsg);

    // Lưu lại
    localStorage.setItem(chatKey, JSON.stringify(history));

    // *QUAN TRỌNG*: Bắn sự kiện để ChatWidget biết mà cập nhật lại danh sách tin nhắn
    window.dispatchEvent(new Event("storage"));

    // 3. Giả lập delay 1 chút cho hiệu ứng (0.5s)
    await new Promise((r) => setTimeout(r, 500));

    // 4. MỞ KHUNG CHAT LÊN (Thay vì chuyển trang)
    chatStore.openChat();

    // 5. Reset form
    form.category = "";
    form.description = "";
  } catch (error) {
    console.error("Lỗi:", error);
    alert("Không thể kết nối với Quan Phủ lúc này!");
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700&display=swap");

.help-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  color: #fdf5e6;
  font-family: "Noto Serif TC", serif;
}

/* HEADER */
.view-header {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}
.header-title {
  color: #fbc02d;
  font-size: 1.8em;
  font-weight: 900;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.8);
  margin: 0;
  letter-spacing: 2px;
}
.header-decor {
  height: 2px;
  width: 150px;
  background: linear-gradient(90deg, transparent, #fbc02d, transparent);
  margin: 5px auto;
}

/* CONTAINER */
.paper-scroll-container {
  flex: 1;
  background: rgba(62, 39, 35, 0.9);
  border: 1px solid #5d4037;
  border-radius: 4px;
  padding: 20px;
  box-shadow: inset 0 0 30px rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
  gap: 25px;
  overflow-y: auto;
}

/* INTRO */
.intro-section {
  background: rgba(0, 0, 0, 0.3);
  padding: 15px;
  border-left: 3px solid #fbc02d;
  border-radius: 4px;
}
.npc-text {
  font-style: italic;
  color: #d7ccc8;
  line-height: 1.6;
  margin: 0;
}

/* FORM */
.form-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.label-ink {
  font-weight: bold;
  color: #fbc02d;
  font-size: 1.1em;
}

.input-wood {
  background: #261815;
  border: 1px solid #5d4037;
  color: #fdf5e6;
  padding: 12px;
  border-radius: 4px;
  font-family: inherit;
  font-size: 1em;
  outline: none;
  transition: 0.3s;
  width: 100%;
  box-sizing: border-box;
}

.input-wood:focus {
  border-color: #fbc02d;
  box-shadow: 0 0 8px rgba(251, 192, 45, 0.3);
  background: #1a100e;
}

.custom-select-wrapper {
  position: relative;
}
.select-arrow {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #fbc02d;
  pointer-events: none;
}

.textarea {
  resize: vertical;
  min-height: 120px;
}

/* BUTTON */
.action-area {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.btn-submit-seal {
  background: #b71c1c;
  color: #fff;
  border: 2px solid #8a1c1c;
  padding: 12px 30px;
  font-size: 1.1em;
  font-weight: bold;
  font-family: "Noto Serif TC", serif;
  cursor: pointer;
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-submit-seal:hover:not(:disabled) {
  background: #d32f2f;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(211, 47, 47, 0.4);
}

.btn-submit-seal:disabled {
  background: #5d4037;
  border-color: #3e2723;
  color: #8d6e63;
  cursor: not-allowed;
  transform: none;
}

/* FAQ */
.faq-section {
  margin-top: auto;
  border-top: 1px dashed #5d4037;
  padding-top: 15px;
}
.faq-title {
  color: #ef5350;
  font-weight: bold;
  margin-bottom: 5px;
}
.faq-list {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 0.9em;
  color: #a1887f;
}
.faq-list li::before {
  content: "•";
  color: #fbc02d;
  margin-right: 8px;
}

/* SCROLLBAR */
.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}
</style>
