<template>
  <div class="chat-panel">
    <div class="chat-header">
      <span class="header-title"
        ><i class="fas fa-globe-asia"></i> Kênh Thế Giới</span
      >
      <div class="connection-status">
        <span v-if="isConnected" class="status-text online">Online</span>
        <span v-else class="status-text offline">Offline</span>
        <div class="status-dot" :class="{ online: isConnected }"></div>
      </div>
    </div>

    <div class="chat-messages custom-scroll" ref="chatContainer">
      <div v-if="chatStore.isLoading" class="system-msg">
        Đang tải thiên thư...
      </div>
      <div v-else-if="messages.length === 0" class="system-msg">
        Chưa có ai đàm đạo.
      </div>

      <div v-for="(msg, index) in messages" :key="index" class="chat-row">
        <span class="timestamp">[{{ formatTime(msg.timestamp) }}]</span>

        <span v-if="msg.role === 'ADMIN'" class="tag-admin">[GM]</span>
        <span v-else-if="msg.role === 'VIP'" class="tag-vip">[VIP]</span>

        <span
          class="sender"
          :class="getRoleClass(msg.role)"
          @click="handleUserClick(msg)"
        >
          {{ msg.senderName }}:
        </span>
        <span class="content">{{ msg.content }}</span>
      </div>
    </div>

    <div class="chat-input">
      <input
        v-model="newMessage"
        @keyup.enter="sendMessage"
        placeholder="Nhập tin nhắn..."
        :disabled="!isConnected"
      />
      <button
        @click="sendMessage"
        :disabled="!isConnected || !newMessage.trim()"
      >
        <i class="fas fa-paper-plane"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from "vue";
import { useChatStore } from "@/stores/chatStore";
import { useAuthStore } from "@/stores/authStore";
import SockJS from "sockjs-client";
import Stomp from "stompjs";

const chatStore = useChatStore();
const authStore = useAuthStore();
const newMessage = ref("");
const chatContainer = ref(null);

const messages = computed(() => chatStore.messages);
const isConnected = computed(() => chatStore.isConnected);

let stompClient = null;
let reconnectInterval = null;

// --- AUTO SCROLL FIX ---
// Tự động cuộn xuống khi danh sách tin nhắn thay đổi
watch(messages, () => {
    scrollToBottom();
}, { deep: true });

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
};
// -----------------------

const connectWebSocket = () => {
  if (stompClient && stompClient.connected) return;

  const socket = new SockJS("http://localhost:8080/ws");
  stompClient = Stomp.over(socket);
  stompClient.debug = () => {};

  stompClient.connect(
    { Authorization: `Bearer ${authStore.token}` },
    () => {
      chatStore.setConnected(true);

      stompClient.subscribe("/topic/public", (payload) => {
        const msg = JSON.parse(payload.body);
        chatStore.addMessage(msg);
        // Watcher sẽ tự gọi scrollToBottom
      });

      stompClient.send(
        "/app/chat.addUser",
        {},
        JSON.stringify({
          senderName: authStore.user.username,
          type: "JOIN",
        }),
      );
    },
    (error) => {
      chatStore.setConnected(false);
      if (!reconnectInterval) {
        reconnectInterval = setTimeout(connectWebSocket, 5000);
      }
    },
  );
};

const sendMessage = () => {
  if (!newMessage.value.trim() || !stompClient || !isConnected.value) return;

  const chatMessage = {
    senderName: authStore.user.username,
    content: newMessage.value,
    type: "CHAT",
  };

  try {
    stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
    newMessage.value = "";
  } catch (e) {
    console.error("Gửi tin nhắn lỗi", e);
  }
};

const formatTime = (timeStr) => {
  if (!timeStr) return "";
  if (timeStr.includes("T")) {
    const date = new Date(timeStr);
    return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  }
  return timeStr;
};

const getRoleClass = (role) => {
  if (role === "ADMIN") return "text-red";
  if (role === "VIP") return "text-gold";
  return "text-blue";
};

const handleUserClick = (msg) => {
  console.log("Clicked user:", msg.senderName);
};

onMounted(async () => {
  await chatStore.fetchMessages();
  scrollToBottom();
  connectWebSocket();
});

onUnmounted(() => {
  if (stompClient) stompClient.disconnect();
  if (reconnectInterval) clearTimeout(reconnectInterval);
  chatStore.setConnected(false);
});
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  overflow: hidden;
  font-family: "Roboto", sans-serif;
  font-size: 0.9em;
  border: 1px solid #5d4037;
  /* Đảm bảo border box */
  box-sizing: border-box; 
}
.chat-header {
  flex-shrink: 0; /* Header không được co lại */
  background: #2b1d1a;
  padding: 6px 10px;
  color: #fbc02d;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #5d4037;
  font-size: 0.85em;
  height: 32px;
  box-sizing: border-box;
}
.connection-status {
  display: flex;
  align-items: center;
  gap: 5px;
}
.status-text {
  font-size: 0.8em;
  font-weight: normal;
}
.status-text.online { color: #00e676; }
.status-text.offline { color: #757575; }
.status-dot {
  width: 8px;
  height: 8px;
  background: #555;
  border-radius: 50%;
  transition: 0.3s;
}
.status-dot.online {
  background: #00e676;
  box-shadow: 0 0 5px #00e676;
}

.chat-messages {
  flex: 1; /* Chiếm hết khoảng trống */
  min-height: 0; /* --- QUAN TRỌNG: Để flex item có thể thu nhỏ và scroll --- */
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: rgba(0, 0, 0, 0.2);
}
.system-msg {
  color: #aaa;
  text-align: center;
  margin-top: 10px;
  font-style: italic;
  font-size: 0.9em;
}

.chat-row {
  line-height: 1.4;
  word-wrap: break-word;
  font-size: 0.95em;
}
.timestamp { color: #757575; font-size: 0.8em; margin-right: 5px; }
.tag-admin { color: #fff; background: #d32f2f; padding: 0 4px; border-radius: 3px; font-size: 0.75em; margin-right: 4px; font-weight: bold; }
.tag-vip { color: #000; background: #fbc02d; padding: 0 4px; border-radius: 3px; font-size: 0.75em; margin-right: 4px; font-weight: bold; }

.sender {
  font-weight: bold;
  margin-right: 5px;
  cursor: pointer;
  transition: 0.2s;
}
.sender:hover {
  text-decoration: underline;
  filter: brightness(1.2);
}
.content { color: #e0e0e0; }
.text-red { color: #ff5252; }
.text-gold { color: #ffd740; }
.text-blue { color: #448aff; }

.chat-input {
  flex-shrink: 0; /* Input không được co lại */
  display: flex;
  border-top: 1px solid #5d4037;
  height: 36px;
  box-sizing: border-box;
}
.chat-input input {
  flex: 1;
  background: rgba(0, 0, 0, 0.3);
  border: none;
  color: white;
  padding: 0 10px;
  outline: none;
  font-size: 0.9em;
  min-width: 0; /* Tránh input vỡ layout */
}
.chat-input input:focus { background: rgba(0, 0, 0, 0.5); }
.chat-input input:disabled { background: rgba(0, 0, 0, 0.1); cursor: not-allowed; }
.chat-input button {
  background: #3e2723;
  border: none;
  color: #fbc02d;
  padding: 0 15px;
  cursor: pointer;
  transition: 0.2s;
  border-left: 1px solid #5d4037;
}
.chat-input button:hover { background: #4e342e; }
.chat-input button:disabled { color: #555; cursor: not-allowed; background: #2b1d1a; }

/* Custom Scrollbar */
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.1); }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 2px; }
</style>