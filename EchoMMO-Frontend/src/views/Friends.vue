<template>
  <div class="page-container friends-page dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="network-wrapper">
      <div class="network-header">
        <div class="header-decor left"></div>
        <h2 class="title-ancient">GIANG HỒ HẢO HỮU</h2>
        <div class="header-decor right"></div>

        <div class="search-box-wrapper">
          <div class="search-input-frame">
            <i class="fas fa-search search-icon"></i>
            <input
              v-model="searchName"
              placeholder="Tìm nhân sĩ (Tên/ID)..."
              class="real-input"
              @keyup.enter="addFriend"
            />
          </div>
          <button @click="addFriend" class="btn-wood primary">
            <i class="fas fa-user-plus"></i> KẾT GIAO
          </button>
        </div>
      </div>

      <div class="network-body">
        <transition name="slide-down">
          <div v-if="friendStore.requests.length" class="requests-section">
            <div class="section-title">
              <i class="fas fa-envelope-open-text"></i> THƯ MỜI KẾT GIAO
              <span class="count-badge">{{ friendStore.requests.length }}</span>
            </div>
            <div class="req-grid custom-scroll">
              <div
                v-for="req in friendStore.requests"
                :key="req.id"
                class="req-card"
              >
                <div class="req-left">
                  <div class="avatar-mini">
                    {{ req.requester.username.charAt(0).toUpperCase() }}
                  </div>
                  <span class="req-name">{{ req.requester.username }}</span>
                </div>
                <div class="req-actions">
                  <button
                    @click="friendStore.accept(req.id)"
                    class="btn-icon success"
                    title="Chấp nhận"
                  >
                    <i class="fas fa-check"></i>
                  </button>
                  <button
                    @click="friendStore.remove(req.id)"
                    class="btn-icon danger"
                    title="Từ chối"
                  >
                    <i class="fas fa-times"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </transition>

        <div class="friends-section">
          <div class="section-header">
            <h3 class="section-title"><i class="fas fa-users"></i> TRI KỶ</h3>
            <span class="total-count">{{ friendStore.friends.length }} vị</span>
          </div>

          <div v-if="friendStore.friends.length === 0" class="empty-state">
            <i class="fas fa-wind"></i>
            <p>Chưa có tri kỷ nào</p>
          </div>

          <div v-else class="friend-list custom-scroll">
            <div
              v-for="f in friendStore.friends"
              :key="f.id"
              class="friend-slot"
            >
              <div class="slot-left">
                <div class="avatar-frame">
                  <span class="char">{{
                    getFriendName(f).charAt(0).toUpperCase()
                  }}</span>
                  <div class="status-dot online"></div>
                </div>
                <div class="slot-info">
                  <span class="slot-name">{{ getFriendName(f) }}</span>
                  <span class="slot-desc">Đang hành tẩu...</span>
                </div>
              </div>

              <div class="slot-actions">
                <button @click="openChat(f)" class="btn-action chat">
                  <i class="fas fa-comment-dots"></i> TRUYỀN ÂM
                </button>
                <button
                  @click="friendStore.remove(f.id)"
                  class="btn-action del"
                  title="Tuyệt giao"
                >
                  <i class="fas fa-user-slash"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <transition name="pop-up">
        <div v-if="showChatModal" class="dark-chat-window">
          <div class="chat-header">
            <div class="chat-title">
              <span class="decor">❖</span> {{ chatTargetName }}
              <span class="decor">❖</span>
            </div>
            <button @click="closeChat" class="btn-close">
              <i class="fas fa-times"></i>
            </button>
          </div>

          <div class="chat-stream custom-scroll" ref="msgBox">
            <div v-if="messages.length === 0" class="chat-init">
              <span class="divider">-- Khởi tạo truyền âm --</span>
            </div>
            <div
              v-for="(msg, index) in messages"
              :key="msg.id || index"
              class="msg-row"
              :class="{ me: msg.senderName === authStore.user.username }"
            >
              <div class="msg-bubble">
                {{ msg.content }}
              </div>
            </div>
          </div>

          <div class="chat-input-area">
            <input
              v-model="chatInput"
              @keyup.enter="sendDM"
              placeholder="Nhập mật thư..."
              ref="inputFocus"
              class="dark-chat-input"
            />
            <button @click="sendDM" class="btn-send">
              <i class="fas fa-paper-plane"></i>
            </button>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from "vue";
import { useFriendStore } from "../stores/friendStore";
import { useAuthStore } from "../stores/authStore";
import axiosClient from "../api/axiosClient";

const friendStore = useFriendStore();
const authStore = useAuthStore();
const searchName = ref("");

// Chat state
const showChatModal = ref(false);
const chatTargetId = ref(null);
const chatTargetName = ref("");
const messages = ref([]);
const chatInput = ref("");
const msgBox = ref(null);
const inputFocus = ref(null);

let chatInterval = null;

onMounted(() => friendStore.fetchAll());
onUnmounted(() => stopChatPolling());

const addFriend = async () => {
  if (!searchName.value) return;
  await friendStore.sendRequest(searchName.value);
  searchName.value = "";
};

const getFriendName = (f) => {
  return f.requester.userId === authStore.user.userId
    ? f.addressee.username
    : f.requester.username;
};

const getFriendUserId = (f) => {
  return f.requester.userId === authStore.user.userId
    ? f.addressee.userId
    : f.requester.userId;
};

const openChat = async (f) => {
  const friendId = getFriendUserId(f);
  chatTargetId.value = friendId;
  chatTargetName.value = getFriendName(f);

  await fetchMessages();
  showChatModal.value = true;
  scrollToBottom();

  nextTick(() => inputFocus.value?.focus());

  stopChatPolling();
  chatInterval = setInterval(fetchMessages, 2000);
};

const closeChat = () => {
  showChatModal.value = false;
  stopChatPolling();
};

const stopChatPolling = () => {
  if (chatInterval) {
    clearInterval(chatInterval);
    chatInterval = null;
  }
};

const fetchMessages = async () => {
  if (!chatTargetId.value) return;
  try {
    // [FIX] Đúng endpoint Backend
    const res = await axiosClient.get(
      `/messages/history/${chatTargetId.value}`,
    );

    // Logic đơn giản để update tin nhắn
    if (
      res.data.length !== messages.value.length ||
      messages.value.length === 0
    ) {
      messages.value = res.data;
      scrollToBottom();
    }
  } catch (e) {
    console.error("Lỗi tải chat", e);
  }
};

const sendDM = async () => {
  if (!chatInput.value.trim()) return;

  const content = chatInput.value;
  chatInput.value = "";

  try {
    // [FIX] Gửi đúng payload receiverId cho Backend
    const res = await axiosClient.post("/messages/send", {
      receiverId: chatTargetId.value,
      content: content,
    });
    messages.value.push(res.data);
    scrollToBottom();
  } catch (e) {
    console.error("Lỗi gửi tin nhắn", e);
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight;
  });
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- PALETTE --- */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --panel-bg: rgba(30, 20, 15, 0.95);
  --gold: #ffecb3;
  --red-seal: #b71c1c;
  --text-light: #f3f4f6;
  --text-dim: #9ca3af;
  --chat-bg-me: #3e2723;
  --chat-bg-them: #261815;
}

/* --- BASE LAYOUT --- */
.dark-theme {
  height: 100vh;
  background-color: #212121;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  overflow: hidden;
  position: relative;
}

/* Backgrounds (Standard Wuxia Dark) */
.ink-bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-color: #3e2723;
}
.mountain-bg {
  position: absolute;
  inset: 0;
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}
.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #261815 10%, transparent 90%);
}

.network-wrapper {
  position: relative;
  z-index: 10;
  max-width: 800px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 40px 20px 20px;
  box-sizing: border-box;
  gap: 20px;
}

/* --- HEADER --- */
.network-header {
  text-align: center;
  position: relative;
  flex-shrink: 0;
}

.title-ancient {
  font-size: 2rem;
  color: var(--gold);
  margin: 0 0 20px;
  text-transform: uppercase;
  font-weight: 900;
  letter-spacing: 3px;
  text-shadow: 0 0 10px rgba(255, 236, 179, 0.3);
}

.header-decor {
  width: 60px;
  height: 2px;
  background: var(--gold);
  position: absolute;
  top: 15px;
}
.left {
  left: 10%;
}
.right {
  right: 10%;
}

/* Search Box */
.search-box-wrapper {
  display: flex;
  gap: 10px;
  background: rgba(0, 0, 0, 0.3);
  padding: 10px;
  border-radius: 4px;
  border: 1px solid var(--wood-light);
}

.search-input-frame {
  flex: 1;
  display: flex;
  align-items: center;
  background: #121212;
  border: 1px solid var(--wood-light);
  padding: 0 10px;
  border-radius: 2px;
}
.search-icon {
  color: var(--gold);
  margin-right: 10px;
}
.real-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-light);
  padding: 10px 0;
  font-family: inherit;
  font-size: 1rem;
}
.real-input:focus {
  outline: none;
}

.btn-wood {
  padding: 10px 20px;
  border: 1px solid var(--gold);
  background: var(--red-seal);
  color: #fff;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}
.btn-wood:hover {
  background: #d32f2f;
}

/* --- BODY --- */
.network-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
  min-height: 0;
}

/* REQUESTS */
.requests-section {
  background: var(--panel-bg);
  border: 1px solid var(--wood-light);
  padding: 10px;
  flex-shrink: 0;
  max-height: 25%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.section-title {
  color: var(--gold);
  font-weight: bold;
  border-bottom: 1px solid var(--wood-light);
  padding-bottom: 5px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
}
.count-badge {
  background: var(--red-seal);
  color: #fff;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 0.8rem;
}

.req-grid {
  flex: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}
.req-card {
  background: rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border: 1px solid var(--wood-light);
}
.req-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.avatar-mini {
  width: 30px;
  height: 30px;
  background: var(--wood-light);
  color: var(--gold);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.8rem;
}
.req-name {
  font-weight: bold;
  font-size: 0.9rem;
}

.req-actions {
  display: flex;
  gap: 5px;
}
.btn-icon {
  width: 25px;
  height: 25px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.success {
  background: #2e7d32;
}
.danger {
  background: #c62828;
}

/* FRIENDS LIST */
.friends-section {
  flex: 1;
  background: var(--panel-bg);
  border: 1px solid var(--wood-light);
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.section-header {
  padding: 12px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid var(--wood-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-header h3 {
  margin: 0;
  color: var(--gold);
  font-size: 1.1rem;
}
.total-count {
  color: var(--text-dim);
  font-size: 0.9rem;
  font-style: italic;
}

.friend-list {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}
.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-dim);
}
.empty-state i {
  font-size: 3rem;
  margin-bottom: 10px;
  opacity: 0.5;
}

.friend-slot {
  background: #261815;
  border: 1px solid var(--wood-light);
  padding: 10px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: 0.2s;
}
.friend-slot:hover {
  border-color: var(--gold);
  background: #3e2723;
}

.slot-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.avatar-frame {
  width: 45px;
  height: 45px;
  border: 2px solid var(--gold);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1a1a1a;
  position: relative;
}
.char {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--gold);
}
.status-dot {
  width: 10px;
  height: 10px;
  background: #4caf50;
  border-radius: 50%;
  position: absolute;
  bottom: 0;
  right: 0;
  border: 2px solid #261815;
}

.slot-info {
  display: flex;
  flex-direction: column;
}
.slot-name {
  font-weight: bold;
  color: var(--text-light);
  font-size: 1rem;
}
.slot-desc {
  font-size: 0.8rem;
  color: var(--text-dim);
}

.slot-actions {
  display: flex;
  gap: 8px;
}
.btn-action {
  background: transparent;
  border: 1px solid var(--wood-light);
  color: var(--text-dim);
  padding: 5px 10px;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.8rem;
  transition: 0.2s;
}
.btn-action:hover {
  color: var(--gold);
  border-color: var(--gold);
}
.btn-action.chat {
  background: rgba(255, 255, 255, 0.05);
}
.btn-action.del:hover {
  color: #ef5350;
  border-color: #ef5350;
}

/* --- CHAT MODAL (Dark Scroll) --- */
.dark-chat-window {
  position: fixed;
  bottom: 0;
  right: 20px;
  width: 320px;
  height: 450px;
  background: #1a1a1a;
  border: 2px solid var(--gold);
  border-bottom: none;
  border-radius: 8px 8px 0 0;
  display: flex;
  flex-direction: column;
  z-index: 100;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.8);
}

.chat-header {
  background: var(--wood-light);
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--gold);
}
.chat-title {
  color: var(--gold);
  font-weight: bold;
  font-size: 0.9rem;
}
.decor {
  color: var(--red-seal);
}
.btn-close {
  background: none;
  border: none;
  color: #ccc;
  cursor: pointer;
}
.btn-close:hover {
  color: #fff;
}

.chat-stream {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background: #261815;
}
.chat-init {
  text-align: center;
  color: #555;
  font-size: 0.8rem;
  margin-bottom: 10px;
}

.msg-row {
  display: flex;
  margin-bottom: 10px;
}
.msg-row.me {
  justify-content: flex-end;
}

.msg-bubble {
  padding: 8px 12px;
  border-radius: 8px;
  max-width: 80%;
  font-size: 0.9rem;
  line-height: 1.4;
  position: relative;
}
.msg-row:not(.me) .msg-bubble {
  background: #3e2723;
  color: #ddd;
  border: 1px solid #5d4037;
  border-bottom-left-radius: 0;
}
.msg-row.me .msg-bubble {
  background: #5d4037;
  color: var(--gold);
  border: 1px solid #8d6e63;
  border-bottom-right-radius: 0;
}

.chat-input-area {
  padding: 10px;
  background: #1a1a1a;
  border-top: 1px solid var(--wood-light);
  display: flex;
  gap: 8px;
}
.dark-chat-input {
  flex: 1;
  background: #000;
  border: 1px solid #444;
  color: #fff;
  padding: 5px 10px;
  font-family: inherit;
  font-size: 1rem;
}
.dark-chat-input:focus {
  border-color: var(--gold);
  outline: none;
}
.btn-send {
  background: var(--gold);
  color: #261815;
  border: none;
  padding: 0 12px;
  cursor: pointer;
  font-weight: bold;
}
.btn-send:hover {
  background: #ffca28;
}

/* Transitions & Scroll */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.pop-up-enter-active,
.pop-up-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.pop-up-enter-from,
.pop-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.custom-scroll::-webkit-scrollbar {
  width: 5px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
</style>
