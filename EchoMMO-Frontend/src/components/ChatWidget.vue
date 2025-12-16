<template>
  <div
    class="chat-trigger"
    @click="chatStore.toggleChat"
    v-if="!chatStore.isChatOpen"
  >
    <div class="trigger-icon"><i class="fas fa-comment-dots"></i></div>
    <div class="trigger-glow"></div>
  </div>

  <transition name="pop-up">
    <div v-if="chatStore.isChatOpen" class="dark-chat-window">
      <div class="chat-header">
        <div class="chat-title">
          <span class="decor">❖</span> Quan Phủ (Admin)
          <span class="decor">❖</span>
        </div>
        <button @click="chatStore.closeChat" class="btn-close">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="chat-stream custom-scroll" ref="msgBox">
        <div v-if="messages.length === 0" class="chat-init">
          <span class="divider">-- Kết nối thành công --</span>
          <p style="margin-top: 10px; font-size: 0.9em; color: #888">
            Đại hiệp có việc gì cần bẩm báo?
          </p>
        </div>

        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-row"
          :class="{ me: msg.sender === 'me' }"
        >
          <div v-if="msg.sender !== 'me'" class="sender-icon">
            <i class="fas fa-user-shield"></i>
          </div>

          <div class="msg-bubble">
            {{ msg.text }}
            <div class="msg-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <input
          v-model="inputMsg"
          @keyup.enter="sendMessage"
          placeholder="Nhập mật thư..."
          class="dark-chat-input"
          ref="inputFocus"
        />
        <button @click="sendMessage" class="btn-send">
          <i class="fas fa-paper-plane"></i>
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from "vue";
import { useChatStore } from "@/stores/chatStore";

const chatStore = useChatStore();
const inputMsg = ref("");
const messages = ref([]);
const msgBox = ref(null);
const inputFocus = ref(null);
const chatKey = "chat_history_admin";

// --- LOGIC GIỐNG CŨ ---
const loadMessages = () => {
  messages.value = JSON.parse(localStorage.getItem(chatKey) || "[]");
  scrollToBottom();
};

const scrollToBottom = async () => {
  await nextTick();
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight;
  }
};

const formatTime = (isoString) => {
  if (!isoString) return "";
  return new Date(isoString).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
};

const sendMessage = () => {
  if (!inputMsg.value.trim()) return;

  const newMsg = {
    id: Date.now(),
    text: inputMsg.value,
    sender: "me",
    timestamp: new Date().toISOString(),
  };

  messages.value.push(newMsg);
  localStorage.setItem(chatKey, JSON.stringify(messages.value));
  inputMsg.value = "";
  scrollToBottom();

  // Giả lập Admin trả lời sau 2s (Cho sinh động)
  setTimeout(() => {
    const replyMsg = {
      id: Date.now(),
      text: "Bổn phủ đã nhận được tin. Sẽ xử lý ngay!",
      sender: "admin",
      timestamp: new Date().toISOString(),
    };
    messages.value.push(replyMsg);
    localStorage.setItem(chatKey, JSON.stringify(messages.value));
    scrollToBottom();
  }, 2000);
};

// Auto scroll & focus khi mở chat
watch(
  () => chatStore.isChatOpen,
  (newVal) => {
    if (newVal) {
      loadMessages();
      nextTick(() => inputFocus.value?.focus());
    }
  },
);

// Lắng nghe sự kiện từ HelpView gửi sang
window.addEventListener("storage", () => {
  loadMessages();
});

onMounted(() => {
  loadMessages();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- COPY STYLE TỪ FRIENDS PAGE --- */
.dark-chat-window {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 340px;
  height: 480px;
  background: #1a1a1a;
  border: 2px solid #ffecb3; /* Gold border */
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  z-index: 9999;
  box-shadow: 0 5px 30px rgba(0, 0, 0, 0.9);
  font-family: "Noto Serif TC", serif;
}

/* HEADER */
.chat-header {
  background: #5d4037; /* Wood light */
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ffecb3;
}
.chat-title {
  color: #ffecb3;
  font-weight: bold;
  font-size: 1rem;
}
.decor {
  color: #b71c1c;
  margin: 0 5px;
} /* Red accent */

.btn-close {
  background: none;
  border: none;
  color: #ccc;
  cursor: pointer;
  font-size: 1.1em;
}
.btn-close:hover {
  color: #fff;
}

/* BODY STREAM */
.chat-stream {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background: #261815; /* Darker wood */
  /* Thêm background pattern gỗ nhẹ nếu muốn */
  background-image: url("https://www.transparenttextures.com/patterns/wood-pattern.png");
}

.chat-init {
  text-align: center;
  color: #555;
  font-size: 0.8rem;
  margin-bottom: 20px;
}
.divider {
  border-bottom: 1px dashed #444;
  padding-bottom: 5px;
}

/* MESSAGES */
.msg-row {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-end;
}
.msg-row.me {
  justify-content: flex-end;
}

/* Bong bóng chat */
.msg-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  max-width: 75%;
  font-size: 0.95rem;
  line-height: 1.4;
  position: relative;
  word-wrap: break-word;
}

/* Tin nhắn của ADMIN (Bên trái) */
.msg-row:not(.me) .msg-bubble {
  background: #3e2723; /* Wood dark */
  color: #ddd;
  border: 1px solid #5d4037;
  border-bottom-left-radius: 0;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.3);
}

/* Tin nhắn của MÌNH (Bên phải) */
.msg-row.me .msg-bubble {
  background: #5d4037; /* Wood light */
  color: #ffecb3; /* Gold text */
  border: 1px solid #8d6e63;
  border-bottom-right-radius: 0;
  box-shadow: -2px 2px 5px rgba(0, 0, 0, 0.3);
}

.msg-time {
  font-size: 0.65em;
  opacity: 0.6;
  text-align: right;
  margin-top: 4px;
}

.sender-icon {
  width: 24px;
  height: 24px;
  background: #b71c1c;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7em;
  margin-right: 8px;
  margin-bottom: 2px;
}

/* FOOTER INPUT */
.chat-input-area {
  padding: 12px;
  background: #1a1a1a;
  border-top: 1px solid #5d4037;
  display: flex;
  gap: 10px;
}

.dark-chat-input {
  flex: 1;
  background: #000;
  border: 1px solid #444;
  color: #fff;
  padding: 8px 12px;
  font-family: inherit;
  border-radius: 4px;
}
.dark-chat-input:focus {
  border-color: #ffecb3;
  outline: none;
}

.btn-send {
  background: #ffecb3; /* Gold */
  color: #261815;
  border: none;
  padding: 0 15px;
  cursor: pointer;
  font-weight: bold;
  border-radius: 4px;
  transition: 0.2s;
}
.btn-send:hover {
  background: #ffca28;
}

/* NÚT TRÒN NỔI (TRIGGER) */
.chat-trigger {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 60px;
  height: 60px;
  background: #b71c1c; /* Red seal */
  border-radius: 50%;
  z-index: 9998;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.6);
  border: 2px solid #ffecb3;
  transition: transform 0.3s;
}
.chat-trigger:hover {
  transform: scale(1.1);
}
.trigger-icon {
  color: #ffecb3;
  font-size: 1.8em;
  z-index: 2;
}
.trigger-glow {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  animation: pulse 2s infinite;
  border: 1px solid #ffecb3;
}

/* ANIMATIONS */
.pop-up-enter-active,
.pop-up-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.pop-up-enter-from,
.pop-up-leave-to {
  transform: translateY(120%);
  opacity: 0;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

/* SCROLLBAR */
.custom-scroll::-webkit-scrollbar {
  width: 5px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}
</style>
