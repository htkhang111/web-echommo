<template>
  <div
    v-if="chatStore.isWidgetOpen"
    class="chat-widget-overlay"
    @click.self="closeChat"
  >
    <div class="chat-widget-window">
      <div class="chat-header">
        <div class="header-title">
          <i class="fas fa-envelope"></i> Truyền Thư
        </div>
        <button class="close-btn" @click="closeChat">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="chat-body">
        <div class="chat-sidebar">
          <div class="sidebar-header">Tin Nhắn</div>
          <div class="conversation-list custom-scroll">
            <div v-if="loadingList" class="loading-text">Đang triệu hồi...</div>
            <div v-else-if="conversations.length === 0" class="empty-conv">
              Chưa có ai liên lạc.
            </div>

            <div
              v-for="conv in conversations"
              :key="conv.userId"
              class="conv-item"
              :class="{ active: currentChatUser?.id == conv.userId }"
              @click="selectUser(conv)"
            >
              <div class="conv-avatar">
                <img
                  :src="getAvatar(conv.avatarUrl)"
                  @error="handleAvatarError"
                />
                <span v-if="conv.unreadCount > 0" class="conv-badge">{{
                  conv.unreadCount
                }}</span>
              </div>
              <div class="conv-info">
                <div class="conv-name">{{ conv.username }}</div>
                <div
                  class="conv-preview"
                  :class="{ unread: conv.unreadCount > 0 }"
                >
                  {{ conv.lastMessage || "..." }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-main" v-if="currentChatUser">
          <div class="chat-main-header">
            <span class="user-status-dot online"></span>
            <span class="partner-name">{{ currentChatUser.username }}</span>
          </div>

          <div class="messages-area custom-scroll" ref="msgContainer">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              class="message-row"
              :class="{ 'my-msg': msg.isMine }"
            >
              <div class="msg-bubble">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.timestamp) }}</div>
            </div>
          </div>

          <div class="chat-input-area">
            <input
              v-model="newMessage"
              @keyup.enter="sendMessage"
              placeholder="Nhập mật ngữ..."
              ref="inputRef"
            />
            <button @click="sendMessage" :disabled="!newMessage.trim()">
              <i class="fas fa-paper-plane"></i>
            </button>
          </div>
        </div>

        <div class="chat-main empty-main" v-else>
          <i class="fas fa-paper-plane big-icon"></i>
          <p>Chọn bằng hữu để đàm đạo</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, onUnmounted } from "vue";
import axiosClient from "@/api/axiosClient";
import { useAuthStore } from "@/stores/authStore";
import { useChatStore } from "@/stores/chatStore";
import { getCurrentSkin } from "@/utils/assetHelper";

const authStore = useAuthStore();
const chatStore = useChatStore();

const conversations = ref([]);
const messages = ref([]);
const currentChatUser = ref(null);
const newMessage = ref("");
const msgContainer = ref(null);
const inputRef = ref(null);
const loadingList = ref(false);

const closeChat = () => {
  chatStore.closeChat();
};

const fetchConversations = async () => {
  loadingList.value = true;
  try {
    const res = await axiosClient.get("/messages/conversations");
    conversations.value = res.data || [];
  } catch (e) {
    console.error("Lỗi load hội thoại", e);
  } finally {
    loadingList.value = false;
  }
};

const selectUser = async (conv) => {
  if (!conv) return;

  // Map dữ liệu cẩn thận để tránh lỗi
  currentChatUser.value = {
    id: conv.userId || conv.id,
    username: conv.username,
  };
  messages.value = [];

  // Đánh dấu đã đọc
  if (conv.unreadCount > 0) {
    try {
      await axiosClient.post(`/messages/read/${conv.userId}`);
      conv.unreadCount = 0;
    } catch (e) {}
  }

  await fetchHistory(currentChatUser.value.id);
  scrollToBottom();
  nextTick(() => inputRef.value?.focus());
};

const fetchHistory = async (userId) => {
  if (!userId) return;
  try {
    const res = await axiosClient.get(`/messages/history/${userId}`);
    messages.value = res.data.map((m) => ({
      content: m.content,
      timestamp: m.timestamp,
      isMine: m.senderId === authStore.userId,
    }));
    scrollToBottom();
  } catch (e) {
    console.error(e);
  }
};

const sendMessage = async () => {
  if (!newMessage.value.trim() || !currentChatUser.value) return;
  const content = newMessage.value;
  newMessage.value = "";

  // Optimistic update
  messages.value.push({
    content,
    timestamp: new Date().toISOString(),
    isMine: true,
  });
  scrollToBottom();

  try {
    await axiosClient.post("/messages/send", {
      receiverId: currentChatUser.value.id,
      content,
    });

    // Cập nhật lại lastMessage trong list bên trái
    const conv = conversations.value.find(
      (c) => c.userId === currentChatUser.value.id,
    );
    if (conv) {
      conv.lastMessage = "Bạn: " + content;
    } else {
      // Nếu là chat mới chưa có trong list -> Reload list
      fetchConversations();
    }
  } catch (e) {
    console.error(e);
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (msgContainer.value)
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight;
  });
};

// [HÀM MỚI] Xử lý mở chat với user cụ thể
const handleOpenChatWithTarget = () => {
  if (!chatStore.privateChatTarget) return;

  const targetId = chatStore.privateChatTarget.id;
  let targetConv = conversations.value.find((c) => c.userId == targetId);

  // Nếu chưa có trong list chat -> Tạo dummy để hiển thị ngay
  if (!targetConv) {
    targetConv = {
      userId: targetId,
      username: chatStore.privateChatTarget.username,
      avatarUrl: chatStore.privateChatTarget.avatarUrl,
      lastMessage: "(Bắt đầu trò chuyện)",
      unreadCount: 0,
    };
    conversations.value.unshift(targetConv);
  }

  selectUser(targetConv);
};

// --- WATCHERS QUAN TRỌNG ---

// 1. Khi bật/tắt Widget
watch(
  () => chatStore.isWidgetOpen,
  (isOpen) => {
    if (isOpen) {
      fetchConversations().then(() => {
        handleOpenChatWithTarget();
      });
    }
  },
);

// 2. [QUAN TRỌNG] Khi Admin đổi người chat (kể cả khi widget đang mở)
watch(
  () => chatStore.privateChatTarget,
  (newTarget) => {
    if (newTarget && chatStore.isWidgetOpen) {
      // Nếu list chưa load xong thì đợi, nếu xong rồi thì switch luôn
      if (conversations.value.length === 0 && loadingList.value) {
        // Đợi fetchConversations xong sẽ tự gọi handleOpenChatWithTarget ở watcher trên
      } else {
        handleOpenChatWithTarget();
      }
    }
  },
  { deep: true },
);

// Utils
const getAvatar = (url) =>
  getCurrentSkin(url)?.sprites?.idle || "https://placehold.co/50?text=U";
const handleAvatarError = (e) => {
  if (!e.target.src.includes("placehold"))
    e.target.src = "https://placehold.co/50?text=U";
};
const formatTime = (iso) =>
  iso
    ? new Date(iso).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      })
    : "";

let pollingInterval;
onMounted(() => {
  // Nếu widget đang mở sẵn (reload trang) thì load luôn
  if (chatStore.isWidgetOpen) {
    fetchConversations();
  }

  pollingInterval = setInterval(() => {
    if (chatStore.isWidgetOpen && currentChatUser.value) {
      // Polling tin nhắn mới
      // Lưu ý: Cần tối ưu polling nếu backend hỗ trợ SSE/WebSocket
      fetchHistory(currentChatUser.value.id);
    }
  }, 5000);
});
onUnmounted(() => clearInterval(pollingInterval));
</script>

<style scoped>
/* CSS giữ nguyên từ bản cũ của bạn */
.chat-widget-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(2px);
}
.chat-widget-window {
  width: 800px;
  height: 600px;
  background: #2b1d1a;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
}
.chat-header {
  background: #3e2723;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fbc02d;
  font-family: "Cinzel", serif;
  border-bottom: 1px solid #5d4037;
}
.close-btn {
  background: none;
  border: none;
  color: #ef5350;
  font-size: 1.2em;
  cursor: pointer;
}
.chat-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.chat-sidebar {
  width: 260px;
  background: #1a1510;
  border-right: 1px solid #5d4037;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 10px;
  color: #a1887f;
  font-weight: bold;
  border-bottom: 1px solid #3e2723;
}
.conversation-list {
  flex: 1;
  overflow-y: auto;
}
.loading-text,
.empty-conv {
  text-align: center;
  color: #777;
  padding: 20px;
}
.conv-item {
  display: flex;
  gap: 10px;
  padding: 12px 10px;
  cursor: pointer;
  border-bottom: 1px solid #2b1d1a;
  transition: 0.2s;
}
.conv-item:hover {
  background: #3e2723;
}
.conv-item.active {
  background: #4e342e;
  border-left: 3px solid #fbc02d;
}
.conv-avatar {
  position: relative;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #8d6e63;
  background: #000;
}
.conv-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.conv-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #d32f2f;
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 10px;
  font-weight: bold;
  border: 1px solid #fff;
}
.conv-info {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.conv-name {
  font-weight: bold;
  color: #eecfa1;
  font-size: 0.95em;
}
.conv-preview {
  font-size: 0.85em;
  color: #8d6e63;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.conv-preview.unread {
  color: #fbc02d;
  font-weight: bold;
}
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #261815;
  position: relative;
}
.chat-main-header {
  padding: 10px 15px;
  background: #3e2723;
  color: #fff;
  font-weight: bold;
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-status-dot {
  width: 8px;
  height: 8px;
  background: #00e676;
  border-radius: 50%;
  box-shadow: 0 0 5px #00e676;
}
.partner-name {
  color: #fbc02d;
  font-family: "Cinzel", serif;
}
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background-color: rgba(0, 0, 0, 0.2);
}
.message-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 75%;
}
.message-row.my-msg {
  align-self: flex-end;
  align-items: flex-end;
}
.msg-bubble {
  background: #424242;
  color: #fff;
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid #616161;
  word-wrap: break-word;
  font-size: 0.95em;
  line-height: 1.4;
  border-top-left-radius: 2px;
}
.my-msg .msg-bubble {
  background: #1b5e20;
  border-color: #2e7d32;
  border-top-left-radius: 12px;
  border-top-right-radius: 2px;
}
.msg-time {
  font-size: 0.7em;
  color: #757575;
  margin-top: 4px;
  padding: 0 2px;
}
.chat-input-area {
  padding: 15px;
  background: #2b1d1a;
  display: flex;
  gap: 10px;
  border-top: 1px solid #5d4037;
}
.chat-input-area input {
  flex: 1;
  padding: 10px 12px;
  background: #1a1510;
  border: 1px solid #5d4037;
  color: #fff;
  border-radius: 4px;
  outline: none;
  transition: 0.3s;
}
.chat-input-area input:focus {
  border-color: #fbc02d;
  box-shadow: 0 0 5px rgba(251, 192, 45, 0.3);
}
.chat-input-area button {
  background: #fbc02d;
  border: none;
  padding: 0 20px;
  cursor: pointer;
  border-radius: 4px;
  font-weight: bold;
  color: #3e2723;
  transition: 0.2s;
}
.chat-input-area button:hover {
  background: #ffca28;
  transform: translateY(-1px);
}
.chat-input-area button:disabled {
  background: #5d4037;
  color: #8d6e63;
  cursor: not-allowed;
}
.empty-main {
  align-items: center;
  justify-content: center;
  color: #5d4037;
  display: flex;
  flex-direction: column;
}
.big-icon {
  font-size: 4em;
  margin-bottom: 10px;
  opacity: 0.5;
}
.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}
@media (max-width: 768px) {
  .chat-widget-window {
    width: 100%;
    height: 100%;
    border-radius: 0;
  }
  .chat-sidebar {
    width: 70px;
  }
  .sidebar-header,
  .conv-info {
    display: none;
  }
  .conv-item {
    justify-content: center;
    padding: 10px 0;
  }
  .conv-avatar {
    width: 36px;
    height: 36px;
  }
}
</style>
