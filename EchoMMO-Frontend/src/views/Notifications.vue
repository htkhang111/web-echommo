<template>
  <div class="page-container wuxia-noti dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="noti-layout">
      <div class="noti-header">
        <div class="header-left">
          <button @click="$router.push('/')" class="btn-wood-back">
            <i class="fas fa-chevron-left"></i>
          </button>
          <div class="title-box">
            <h2 class="title-ink">MẬT THƯ</h2>
            <div class="subtitle">Tin tức chốn giang hồ</div>
          </div>
        </div>

        <button @click="notiStore.markAllRead()" class="btn-seal">
          <i class="fas fa-check-double"></i> ĐÃ ĐỌC HẾT
        </button>
      </div>

      <div v-if="notiStore.isLoading" class="loading-state">
        <div class="ink-spinner"></div>
        <span>Đang giải mã...</span>
      </div>

      <div v-else class="noti-feed custom-scroll">
        <transition-group name="list">
          <div
            v-for="noti in (notiStore.notifications || [])"
            :key="noti.id"
            class="noti-card"
            :class="[getTypeClass(noti.type), { unread: !noti.isRead }]"
            @click="openPopup(noti)"
          >
            <div v-if="!noti.isRead" class="unread-glow"></div>

            <div class="card-icon">
              <i :class="getIcon(noti.type)"></i>
            </div>

            <div class="card-content">
              <div class="card-top">
                <span class="card-title">{{ noti.title }}</span>
                <span v-if="!noti.isRead" class="seal-new">MỚI</span>
              </div>
              <div class="card-preview">{{ noti.message }}</div>
              <div class="card-time">
                <i class="far fa-clock"></i>
                {{ new Date(noti.createdAt).toLocaleString() }}
              </div>
            </div>

            <div class="card-arrow"><i class="fas fa-chevron-right"></i></div>
          </div>
        </transition-group>

        <div v-if="notiStore.notifications?.length === 0" class="empty-state">
          <i class="fas fa-wind"></i>
          <p>Gió lặng mây ngừng, không có tin mới.</p>
        </div>
      </div>
    </div>

    <transition name="zoom">
      <div
        v-if="selectedNoti"
        class="overlay-modal"
        @click.self="selectedNoti = null"
      >
        <div class="dark-scroll-modal">
          <div class="scroll-rod top"></div>

          <div class="scroll-paper custom-scroll">
            <div class="paper-header" :class="getTypeClass(selectedNoti.type)">
              <span class="header-label">{{
                getHeaderTitle(selectedNoti.type)
              }}</span>
              <button @click="selectedNoti = null" class="btn-close">
                <i class="fas fa-times"></i>
              </button>
            </div>

            <div class="paper-body">
              <h3 class="msg-title">{{ selectedNoti.title }}</h3>
              <div class="gold-divider"></div>
              <p class="msg-content">{{ selectedNoti.message }}</p>

              <div class="msg-footer">
                <div class="msg-meta">
                  <span><i class="fas fa-scroll"></i> Nguồn: Hệ Thống</span>
                  <span
                    ><i class="far fa-calendar-alt"></i>
                    {{
                      new Date(selectedNoti.createdAt).toLocaleString()
                    }}</span
                  >
                </div>
                <div class="stamp-box">
                  <div class="red-stamp">ĐÃ DUYỆT</div>
                </div>
              </div>
            </div>
          </div>

          <div class="scroll-rod bot"></div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useNotificationStore } from "../stores/notificationStore";

const notiStore = useNotificationStore();
const selectedNoti = ref(null);

const openPopup = (noti) => {
  selectedNoti.value = noti;
  notiStore.markRead(noti.id);
};

const getTypeClass = (type) => {
  switch (type) {
    case "SUCCESS":
      return "type-success";
    case "WARNING":
      return "type-warning";
    case "ERROR":
      return "type-error";
    default:
      return "type-info";
  }
};

const getHeaderTitle = (type) => {
  switch (type) {
    case "SUCCESS":
      return "TIN MỪNG";
    case "WARNING":
      return "CẢNH BÁO";
    case "ERROR":
      return "KHẨN CẤP";
    default:
      return "MẬT THƯ";
  }
};

const getIcon = (type) => {
  switch (type) {
    case "SUCCESS":
      return "fas fa-gift";
    case "WARNING":
      return "fas fa-exclamation-triangle";
    case "ERROR":
      return "fas fa-skull";
    default:
      return "fas fa-envelope";
  }
};

onMounted(() => {
  notiStore.fetchNotifications();
});
</script>

<style scoped>
/* Copy CSS cũ của bạn vào đây (không thay đổi gì) */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- PALETTE --- */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --bg-dark: #212121;
  --gold: #ffecb3;
  --red-seal: #b71c1c;
  --text-light: #f3f4f6;
  --text-dim: #9ca3af;
}

/* --- BASE LAYOUT --- */
.dark-theme {
  background-color: var(--bg-dark);
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
}

/* Backgrounds (Copy from Home) */
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

.noti-layout {
  position: relative;
  z-index: 10;
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-sizing: border-box;
}

/* --- HEADER --- */
.noti-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 15px 20px;
  background: rgba(30, 20, 15, 0.9);
  border: 2px solid var(--wood-light);
  border-radius: 4px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-wood-back {
  background: transparent;
  border: 1px solid var(--wood-light);
  color: var(--gold);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.3s;
}
.btn-wood-back:hover {
  background: var(--wood-light);
  color: #fff;
  border-color: var(--gold);
}

.title-ink {
  font-size: 1.8em;
  color: var(--gold);
  margin: 0;
  font-weight: 900;
  letter-spacing: 2px;
  text-shadow: 0 0 10px rgba(255, 236, 179, 0.3);
}
.subtitle {
  font-size: 0.85em;
  color: var(--text-dim);
  font-style: italic;
}

.btn-seal {
  background: var(--red-seal);
  border: 1px solid #d32f2f;
  color: #fff;
  padding: 8px 15px;
  cursor: pointer;
  font-weight: bold;
  font-family: inherit;
  border-radius: 4px;
  font-size: 0.9em;
  box-shadow: 0 4px 10px rgba(183, 28, 28, 0.4);
  transition: 0.2s;
}
.btn-seal:hover {
  background: #d32f2f;
  transform: translateY(-2px);
}

/* --- FEED LIST --- */
.noti-feed {
  flex: 1;
  overflow-y: auto;
  padding-right: 5px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* CARD STYLES */
.noti-card {
  position: relative;
  background: rgba(40, 25, 20, 0.9); /* Dark wood bg */
  border: 1px solid var(--wood-light);
  border-left: 4px solid var(--wood-light); /* Default border left */
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 2px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.noti-card:hover {
  transform: translateX(5px);
  background: rgba(60, 40, 30, 0.95);
  border-color: var(--gold);
}

/* Type Colors */
.type-info {
  border-left-color: #42a5f5;
}
.type-success {
  border-left-color: #66bb6a;
}
.type-warning {
  border-left-color: #ffa726;
}
.type-error {
  border-left-color: #ef5350;
}

.type-info .card-icon {
  color: #42a5f5;
  border-color: #42a5f5;
}
.type-success .card-icon {
  color: #66bb6a;
  border-color: #66bb6a;
}
.type-warning .card-icon {
  color: #ffa726;
  border-color: #ffa726;
}
.type-error .card-icon {
  color: #ef5350;
  border-color: #ef5350;
}

/* Unread State */
.noti-card.unread {
  background: linear-gradient(
    90deg,
    rgba(183, 28, 28, 0.1),
    rgba(40, 25, 20, 0.9)
  );
  border-left-color: var(--red-seal);
}
.unread-glow {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 8px;
  height: 8px;
  background: var(--red-seal);
  border-radius: 50%;
  box-shadow: 0 0 8px var(--red-seal);
}

/* Icon */
.card-icon {
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2em;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #555;
  border-radius: 50%;
  flex-shrink: 0;
}

/* Content */
.card-content {
  flex: 1;
  overflow: hidden;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.card-title {
  font-weight: bold;
  font-size: 1.05em;
  color: var(--gold);
}
.seal-new {
  background: var(--red-seal);
  color: #fff;
  font-size: 0.65em;
  padding: 2px 6px;
  border-radius: 2px;
  font-weight: bold;
}

.card-preview {
  font-size: 0.9em;
  color: #ccc;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-time {
  font-size: 0.75em;
  color: var(--text-dim);
  margin-top: 5px;
  font-style: italic;
}

.card-arrow {
  color: var(--wood-light);
  font-size: 0.8em;
}

/* --- MODAL (Dark Scroll) --- */
.overlay-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(3px);
}

.dark-scroll-modal {
  width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.scroll-rod {
  height: 15px;
  background: #1a1a1a;
  border-radius: 8px;
  border: 1px solid var(--wood-light);
  position: relative;
  z-index: 2;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.8);
}
.scroll-rod::after {
  content: "";
  position: absolute;
  right: 5px;
  top: 1px;
  bottom: 1px;
  width: 30px;
  background: var(--gold);
  border-radius: 2px;
  opacity: 0.5;
}

.scroll-paper {
  background: #261815; /* Dark background for scroll */
  border-left: 2px solid var(--wood-light);
  border-right: 2px solid var(--wood-light);
  padding: 30px;
  color: var(--text-light);
  overflow-y: auto;
  background-image: url("https://www.transparenttextures.com/patterns/wood-pattern.png");
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--wood-light);
}
.header-label {
  font-weight: 900;
  font-size: 1.1em;
  letter-spacing: 2px;
}
.type-success .header-label {
  color: #66bb6a;
}
.type-warning .header-label {
  color: #ffa726;
}
.type-error .header-label {
  color: #ef5350;
}
.type-info .header-label {
  color: var(--gold);
}

.btn-close {
  background: none;
  border: none;
  color: var(--text-dim);
  font-size: 1.2em;
  cursor: pointer;
}
.btn-close:hover {
  color: #fff;
}

.msg-title {
  font-size: 1.5em;
  margin: 0 0 15px 0;
  color: #fff;
  text-align: center;
}
.gold-divider {
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--gold), transparent);
  margin-bottom: 20px;
  opacity: 0.5;
}
.msg-content {
  font-size: 1.05em;
  line-height: 1.6;
  text-align: justify;
  color: #ddd;
}

.msg-footer {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.msg-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 0.85em;
  color: var(--text-dim);
}
.red-stamp {
  border: 3px solid var(--red-seal);
  color: var(--red-seal);
  font-weight: 900;
  padding: 5px 15px;
  font-size: 1.2em;
  transform: rotate(-10deg);
  opacity: 0.8;
  text-shadow: 0 0 5px rgba(183, 28, 28, 0.5);
  box-shadow: inset 0 0 10px rgba(183, 28, 28, 0.2);
}

/* LOADING & EMPTY */
.loading-state,
.empty-state {
  text-align: center;
  padding: 60px;
  color: var(--text-dim);
}
.ink-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--wood-light);
  border-top-color: var(--gold);
  border-radius: 50%;
  margin: 0 auto 15px;
  animation: spin 1s linear infinite;
}
.empty-state i {
  font-size: 3em;
  margin-bottom: 15px;
  opacity: 0.3;
}

/* ANIMATION */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
.zoom-enter-active,
.zoom-leave-active {
  transition: all 0.3s ease;
}
.zoom-enter-from,
.zoom-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* SCROLLBAR */
.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: var(--wood-light);
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}
</style>