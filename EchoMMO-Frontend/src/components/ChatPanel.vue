<template>
  <div class="chat-wrapper" :style="{ height: height }">
    <div class="chat-header">
      <div class="header-left">
        <i class="fas fa-bullhorn text-red"></i>
        <span class="channel-name">GIANG HỒ ĐÀM</span>
      </div>
      <div class="header-right">
        <span class="status-dot">●</span>
      </div>
    </div>

    <div class="chat-body custom-scroll" ref="msgContainer">
      <div
        v-for="(msg, index) in chatStore.messages"
        :key="index"
        class="chat-row"
        :class="{
          'admin-line': msg.role === 'ADMIN',
          me: isMe(msg.senderName),
        }"
      >
        <div class="msg-meta">
          <span class="time">[{{ msg.time }}]</span>
          <span v-if="msg.role === 'ADMIN'" class="seal adm">QUAN</span>
          <span v-else class="seal usr">HIỆP</span>
          <span class="sender">{{ msg.senderName }}:</span>
        </div>
        <div class="msg-text">{{ msg.content }}</div>
      </div>

      <div class="chat-row waiting" v-if="isLoading">
        <span class="dots">...</span>
      </div>
    </div>

    <form @submit.prevent="handleSend" class="chat-footer">
      <div class="input-box">
        <input
          v-model="inputMsg"
          placeholder="Nhập tin nhắn..."
          maxlength="100"
          class="ink-input"
        />
        <button type="submit" class="btn-send">
          <i class="fas fa-paper-plane"></i>
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue";
import { useChatStore } from "../stores/chatStore";
import { useAuthStore } from "../stores/authStore";

const props = defineProps({
  height: {
    type: String,
    default: "100%",
  },
});

const chatStore = useChatStore();
const authStore = useAuthStore();
const inputMsg = ref("");
const msgContainer = ref(null);
const isLoading = ref(false);
let pollingInterval = null;

const isMe = (senderName) => {
  return authStore.user?.username === senderName;
};

// --- LOGIC CUỘN THÔNG MINH ---
const scrollToBottom = (force = false) => {
  nextTick(() => {
    if (!msgContainer.value) return;

    const { scrollTop, scrollHeight, clientHeight } = msgContainer.value;

    // Kiểm tra xem user có đang ở gần đáy không (cách đáy < 150px)
    const isNearBottom = scrollHeight - scrollTop - clientHeight < 150;

    // Chỉ cuộn nếu bị ép buộc (force) hoặc đang ở gần đáy
    if (force || isNearBottom) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight;
    }
  });
};

const handleSend = async () => {
  if (!inputMsg.value.trim()) return;

  // Gửi tin nhắn
  await chatStore.sendMessage(inputMsg.value);
  inputMsg.value = "";

  // Refresh lại list và ÉP BUỘC cuộn xuống
  await chatStore.fetchMessages();
  scrollToBottom(true);
};

onMounted(async () => {
  await chatStore.fetchMessages();
  scrollToBottom(true); // Lần đầu vào thì luôn cuộn xuống đáy

  // Polling: Gọi API mỗi 3 giây
  pollingInterval = setInterval(async () => {
    await chatStore.fetchMessages();
  }, 3000);
});

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
});

// Khi có tin nhắn mới -> Cuộn (nếu cần)
watch(
  () => chatStore.messages,
  () => {
    scrollToBottom(false);
  },
  { deep: true },
);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700&display=swap");

.chat-wrapper {
  /* QUAN TRỌNG: Flex column để chia vùng */
  display: flex;
  flex-direction: column;
  width: 100%;
  /* Fix cứng chiều cao theo cha (100%), không cho nở ra */
  height: 100%;
  overflow: hidden; /* Cắt bỏ phần thừa nếu có */
  background: #e3d5b8;
  border-radius: 4px;
  font-family: "Noto Serif TC", serif;
  position: relative;
  box-sizing: border-box;
}

.chat-header {
  /* Cố định chiều cao Header */
  flex: 0 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
  background: #3e2723;
  color: #d7ccc8;
  border-bottom: 2px solid #5d4037;
  font-size: 0.9rem;
  z-index: 2;
}

.chat-body {
  /* Flex 1: Chiếm toàn bộ khoảng trống còn lại */
  flex: 1;
  /* Min-height 0 là hack quan trọng để scroll hoạt động trong Flexbox */
  min-height: 0;
  /* Chỉ cuộn nội dung bên trong body này */
  overflow-y: auto;

  padding: 10px;
  background-color: #e3d5b8;
  background-image: url("https://www.transparenttextures.com/patterns/aged-paper.png");
  scroll-behavior: smooth;
}

.chat-footer {
  /* Cố định footer, không cho phép bị đẩy đi */
  flex: 0 0 auto;
  padding: 8px;
  background: #d7ccc8;
  border-top: 2px solid #5d4037;
  z-index: 2;
}

/* --- Style trang trí --- */
.text-red {
  color: #ff4444;
  margin-right: 8px;
  animation: pulse 2s infinite;
}
.channel-name {
  font-weight: bold;
  color: #ffccbc;
  letter-spacing: 1px;
}
.status-dot {
  color: #4caf50;
  font-size: 1.2em;
  text-shadow: 0 0 5px #4caf50;
}

.chat-row {
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px dashed rgba(62, 39, 35, 0.2);
  font-size: 0.85rem;
  line-height: 1.4;
  color: #3e2723;
}
.msg-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 2px;
  font-size: 0.8em;
  flex-wrap: wrap;
}
.time {
  color: #8d6e63;
  font-style: italic;
  font-size: 0.85em;
}
.seal {
  font-size: 0.7em;
  padding: 0 4px;
  border: 1px solid;
  border-radius: 2px;
  font-weight: bold;
}
.seal.usr {
  color: #3e2723;
  border-color: #3e2723;
}
.seal.adm {
  background: #b71c1c;
  color: #fff;
  border-color: #b71c1c;
}
.sender {
  font-weight: bold;
  color: #1a1a1a;
}
.msg-text {
  word-break: break-word;
  font-weight: 500;
}

.admin-line .sender,
.admin-line .msg-text {
  color: #b71c1c;
  font-weight: bold;
}
.me .sender {
  color: #0d47a1;
}
.me .msg-text {
  color: #000;
  font-weight: 600;
}

.input-box {
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1px solid #5d4037;
  border-radius: 4px;
  padding: 0 5px;
  height: 36px;
}
.ink-input {
  flex: 1;
  border: none;
  outline: none;
  height: 100%;
  padding: 0 8px;
  font-family: "Noto Serif TC", serif;
  font-size: 0.9rem;
  color: #000;
  font-weight: 600;
  background: transparent;
}
.btn-send {
  background: #b71c1c;
  color: #fff;
  border: none;
  border-radius: 2px;
  width: 32px;
  height: 30px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}
.btn-send:hover {
  background: #d32f2f;
  transform: translateY(-1px);
}

/* Scrollbar nhỏ gọn */
.custom-scroll::-webkit-scrollbar {
  width: 4px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #8d6e63;
  border-radius: 2px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}
</style>
