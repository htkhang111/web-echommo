<template>
  <div class="page-container wuxia-noti ink-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="ink-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="particles">
        <div v-for="n in 15" :key="n" class="particle" :style="getParticleStyle(n)"></div>
      </div>
    </div>

    <div class="noti-layout">
      <div class="noti-header">
        <div class="header-left">
          <button @click="$router.push('/')" class="btn-ink-back">
            <div class="btn-content"><i class="fas fa-chevron-left"></i></div>
          </button>
          <div class="title-group">
            <h2 class="main-title">MẬT THƯ</h2>
            <div class="sub-title">Tin tức chốn giang hồ</div>
          </div>
        </div>
        <div class="header-right">
           <button @click="notiStore.markAllRead()" class="btn-royal-seal">
             <i class="fas fa-check-double"></i> <span>ĐÃ ĐỌC HẾT</span>
           </button>
        </div>
      </div>

      <div class="content-frame">
        <div v-if="notiStore.isLoading" class="loading-state">
          <div class="brush-loader"></div>
          <span class="loading-text">Đang giải mã...</span>
        </div>

        <div v-else class="noti-list custom-scroll">
          <transition-group name="staggered-fade">
            <div 
              v-for="(noti, index) in (notiStore.notifications || [])" 
              :key="noti.id" 
              class="noti-item"
              :class="[getTypeClass(noti.type), { 'is-read': noti.isRead }]"
              :style="{ '--i': index }"
              @click="openPopup(noti)"
            >
              <div class="glow-border" v-if="!noti.isRead"></div>
              
              <div class="item-icon-box">
                <i :class="getIcon(noti.type)"></i>
              </div>

              <div class="item-body">
                <div class="item-header">
                  <span class="item-title">{{ noti.title }}</span>
                  <span class="time-stamp">{{ formatTime(noti.createdAt) }}</span>
                </div>
                <div class="item-desc">{{ noti.message }}</div>
              </div>

              <div class="item-action">
                <i class="fas fa-chevron-right"></i>
              </div>
            </div>
          </transition-group>

          <div v-if="!notiStore.isLoading && notiStore.notifications?.length === 0" class="empty-state">
             <div class="empty-icon"><i class="fas fa-wind"></i></div>
             <p>Gió lặng mây ngừng, không có tin mới.</p>
          </div>
        </div>
      </div>
    </div>

    <transition name="scroll-unroll">
      <div v-if="selectedNoti" class="modal-overlay" @click.self="selectedNoti = null">
        <div class="scroll-container">
          <div class="rod rod-top">
            <div class="rod-end left"></div>
            <div class="rod-mid"></div>
            <div class="rod-end right"></div>
          </div>

          <div class="paper-content custom-scroll">
            <div class="inner-border-frame">
              
              <button class="close-btn-corner" @click="selectedNoti = null">
                <i class="fas fa-times"></i>
              </button>

              <div class="paper-inner">
                <div class="detail-header">
                  <div class="decor-cloud left"></div>
                  <span class="badge-type" :class="getTypeClass(selectedNoti.type)">
                    {{ getHeaderTitle(selectedNoti.type) }}
                  </span>
                  <div class="decor-cloud right"></div>
                </div>

                <h3 class="detail-title">{{ selectedNoti.title }}</h3>
                
                <div class="detail-divider">
                  <span class="line"></span>
                  <i class="fas fa-dragon center-icon"></i>
                  <span class="line"></span>
                </div>

                <div class="detail-body-text">
                  {{ selectedNoti.message }}
                </div>

                <div class="detail-footer">
                  <div class="meta-info">
                    <div class="meta-row">
                      <i class="fas fa-scroll"></i>
                      <span>Nguồn: <strong>Hệ Thống</strong></span>
                    </div>
                    <div class="meta-row">
                      <i class="far fa-clock"></i>
                      <span>{{ new Date(selectedNoti.createdAt).toLocaleString('vi-VN') }}</span>
                    </div>
                  </div>
                  
                  <div class="stamp-seal">
                    <div class="seal-inner">
                       ĐÃ DUYỆT
                    </div>
                  </div>
                </div>

                <button class="btn-close-main" @click="selectedNoti = null">
                  Đóng Mật Thư
                </button>
              </div>
            </div>
          </div>

          <div class="rod rod-bottom">
            <div class="rod-end left"></div>
            <div class="rod-mid"></div>
            <div class="rod-end right"></div>
          </div>
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

// Background Logic
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// Particles Logic
const getParticleStyle = (n) => {
  const size = Math.random() * 3 + 2 + 'px';
  const left = Math.random() * 100 + '%';
  const delay = Math.random() * 5 + 's';
  const duration = Math.random() * 10 + 10 + 's';
  return {
    width: size,
    height: size,
    left: left,
    animationDelay: delay,
    animationDuration: duration,
    opacity: Math.random() * 0.5 + 0.1
  };
};

const openPopup = (noti) => {
  selectedNoti.value = noti;
  notiStore.markRead(noti.id);
};

const formatTime = (isoString) => {
  const date = new Date(isoString);
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'});
};

const getTypeClass = (type) => {
  switch (type) {
    case "SUCCESS": return "type-success";
    case "WARNING": return "type-warning";
    case "ERROR": return "type-error";
    default: return "type-info";
  }
};

const getHeaderTitle = (type) => {
  switch (type) {
    case "SUCCESS": return "TIN MỪNG";
    case "WARNING": return "CẢNH BÁO";
    case "ERROR": return "KHẨN CẤP";
    default: return "MẬT THƯ";
  }
};

const getIcon = (type) => {
  switch (type) {
    case "SUCCESS": return "fas fa-gift";
    case "WARNING": return "fas fa-exclamation-triangle";
    case "ERROR": return "fas fa-skull-crossbones";
    default: return "fas fa-scroll";
  }
};

onMounted(() => {
  updateDayNight();
  notiStore.fetchNotifications();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --ink-black: #1a1a1a;
  --ink-light: #2c2c2c;
  --gold-main: #d4af37;
  --gold-dim: #8a7338;
  --gold-bright: #ffdf80;
  --red-seal: #b22222;
  --paper-bg: #000000;
  --text-white: #f0f0f0;
  --text-gray: #a0a0a0;
  --text-ink: #2c241b;
}

/* BACKGROUND */
.ink-theme { 
  font-family: "Noto Serif TC", serif; 
  color: var(--text-white); 
  min-height: 100vh; 
  position: relative; 
  overflow: hidden; 
}

.bg-layer { 
  position: absolute; 
  inset: 0; 
  z-index: 0; 
  background: #000; 
  overflow: hidden; 
}

.mountain-bg { 
  position: absolute; 
  inset: 0; 
  background-size: cover; 
  background-position: center; 
  opacity: 0.5; 
  filter: grayscale(40%) contrast(1.1); 
  transition: opacity 1s; 
}

.ink-overlay { 
  position: absolute; 
  inset: 0; 
  background: radial-gradient(circle at center, rgba(20,20,20,0.4) 0%, rgba(0,0,0,0.9) 100%); 
  mix-blend-mode: multiply; 
}

.ink-overlay.night-mode { 
  background: radial-gradient(circle at center, rgba(10,10,25,0.6) 0%, rgba(0,0,5,0.95) 100%); 
}

.particles { 
  position: absolute; 
  inset: 0; 
  pointer-events: none; 
}

.particle { 
  position: absolute; 
  bottom: -10px; 
  background: rgba(255, 255, 255, 0.3); 
  border-radius: 50%; 
  animation: floatUp linear infinite; 
}

@keyframes floatUp { 
  0% { transform: translateY(0) translateX(0); opacity: 0; } 
  100% { transform: translateY(-110vh) translateX(20px); opacity: 0; } 
}

/* LAYOUT */
.noti-layout { 
  position: relative; 
  z-index: 10; 
  max-width: 900px; 
  margin: 0 auto; 
  height: 100vh; 
  display: flex; 
  flex-direction: column; 
  padding: 20px; 
}

/* HEADER */
.noti-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 20px; 
  padding: 15px 25px; 
  background: rgba(0, 0, 0, 0.6); 
  border: 1px solid var(--gold-dim); 
  border-radius: 8px; 
  backdrop-filter: blur(8px); 
  box-shadow: 0 5px 15px rgba(0,0,0,0.5); 
}

.header-left { 
  display: flex; 
  align-items: center; 
  gap: 20px; 
}

.btn-ink-back { 
  background: transparent; 
  border: none; 
  cursor: pointer; 
  padding: 0; 
}

.btn-content { 
  width: 45px; 
  height: 45px; 
  border: 2px solid var(--gold-dim); 
  border-radius: 50%; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  color: var(--gold-main); 
  font-size: 1.2rem; 
  transition: all 0.3s ease; 
  background: rgba(0,0,0,0.3); 
}

.btn-ink-back:hover .btn-content { 
  border-color: var(--gold-bright); 
  color: var(--gold-bright); 
  background: rgba(212, 175, 55, 0.1); 
  transform: scale(1.05); 
}

.title-group { 
  display: flex; 
  flex-direction: column; 
}

.main-title { 
  margin: 0; 
  font-size: 2rem; 
  background: linear-gradient(to bottom, #000000, #000000); 
  -webkit-background-clip: text; 
  color: transparent; 
  font-weight: 900; 
  letter-spacing: 2px; 
  text-shadow: 0 2px 4px rgba(0,0,0,0.8); 
}

.sub-title { 
  font-size: 0.9rem; 
  color: var(--gold-dim); 
  font-style: italic; 
  margin-top: -2px; 
}

.btn-royal-seal { 
  background: linear-gradient(135deg, #800000, #500000); 
  border: 1px solid #ff4d4d; 
  color: #000000000; 
  padding: 10px 20px; 
  border-radius: 4px; 
  cursor: pointer; 
  font-weight: bold; 
  font-family: inherit; 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  box-shadow: 0 4px 10px rgba(0,0,0,0.5); 
  transition: all 0.3s; 
}

.btn-royal-seal:hover { 
  background: linear-gradient(135deg, #a00000, #700000); 
  transform: translateY(-2px); 
  box-shadow: 0 6px 15px rgba(178, 34, 34, 0.4); 
}

/* CONTENT FRAME */
.content-frame { 
  flex: 1; 
  overflow: hidden; 
  position: relative; 
}

.noti-list { 
  height: 100%; 
  overflow-y: auto; 
  padding-right: 10px; 
  padding-bottom: 20px; 
}

/* NOTIFICATION ITEMS - ENHANCED */
.noti-item { 
  position: relative; 
  background: linear-gradient(135deg, rgba(30, 30, 30, 0.95), rgba(20, 20, 20, 0.9)); 
  margin-bottom: 16px; 
  padding: 20px; 
  border-radius: 12px; 
  display: flex; 
  align-items: center; 
  gap: 18px; 
  cursor: pointer; 
  border: 1px solid rgba(212, 175, 55, 0.15); 
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); 
  overflow: hidden; 
  animation: slideIn 0.5s ease forwards; 
  animation-delay: calc(var(--i) * 0.05s); 
  opacity: 0; 
  transform: translateY(20px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

@keyframes slideIn { 
  to { opacity: 1; transform: translateY(0); } 
}

.noti-item:hover { 
  background: linear-gradient(135deg, rgba(45, 40, 35, 0.98), rgba(35, 30, 25, 0.95)); 
  border-color: var(--gold-main); 
  transform: translateX(8px) scale(1.02); 
  box-shadow: 0 8px 25px rgba(212, 175, 55, 0.2), 0 0 40px rgba(212, 175, 55, 0.1); 
}

.noti-item.is-read { 
  opacity: 0.65; 
  filter: grayscale(0.3); 
}

.noti-item.is-read:hover { 
  opacity: 1; 
  filter: grayscale(0); 
}

.glow-border { 
  position: absolute; 
  inset: 0; 
  border: 1px solid var(--gold-main); 
  box-shadow: inset 0 0 15px rgba(212, 175, 55, 0.2); 
  pointer-events: none; 
  animation: pulseGlow 2s infinite alternate;
  border-radius: 12px;
}

@keyframes pulseGlow { 
  0% { opacity: 0.5; } 
  100% { opacity: 1; box-shadow: inset 0 0 25px rgba(212, 175, 55, 0.4); } 
}

/* ICON BOX - ENHANCED */
.item-icon-box { 
  width: 60px; 
  height: 60px; 
  background: linear-gradient(135deg, #1a1a1a, #0f0f0f); 
  border: 2px solid #444; 
  border-radius: 10px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  font-size: 1.6rem; 
  flex-shrink: 0; 
  position: relative; 
  z-index: 2; 
  box-shadow: 0 4px 15px rgba(0,0,0,0.5), inset 0 2px 5px rgba(255,255,255,0.05); 
  transition: all 0.3s;
}

.noti-item:hover .item-icon-box { 
  transform: scale(1.1) rotate(5deg); 
  box-shadow: 0 6px 20px rgba(0,0,0,0.6), inset 0 2px 8px rgba(255,255,255,0.1);
}

.type-success .item-icon-box { 
  color: #66bb6a; 
  border-color: #43a047; 
  background: linear-gradient(135deg, #1b5e20, #2e7d32); 
}

.type-warning .item-icon-box { 
  color: #ffa726; 
  border-color: #fb8c00; 
  background: linear-gradient(135deg, #e65100, #ef6c00); 
}

.type-error .item-icon-box { 
  color: #ef5350; 
  border-color: #e53935; 
  background: linear-gradient(135deg, #b71c1c, #c62828); 
}

.type-info .item-icon-box { 
  color: var(--gold-bright); 
  border-color: var(--gold-main); 
  background: linear-gradient(135deg, #6d5c2e, #8a7338); 
}

.item-body { 
  flex: 1; 
  overflow: hidden; 
}

.item-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 8px; 
}

.item-title { 
  font-weight: bold; 
  font-size: 1.15rem; 
  color: #000000; 
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
}

.time-stamp { 
  font-size: 0.85rem; 
  color: var(--text-gray); 
  font-style: italic; 
}

.item-desc { 
  font-size: 0.95rem; 
  color: #d0d0d0; 
  white-space: nowrap; 
  overflow: hidden; 
  text-overflow: ellipsis; 
  line-height: 1.4;
}

.item-action { 
  color: var(--gold-main); 
  font-size: 1.2rem; 
  transition: 0.3s; 
  opacity: 0; 
  transform: translateX(-10px); 
}

.noti-item:hover .item-action { 
  opacity: 1; 
  transform: translateX(0); 
}

/* EMPTY STATE */
.empty-state { 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: center; 
  height: 300px; 
  color: var(--text-gray); 
  opacity: 0.6; 
}

.empty-icon { 
  font-size: 4rem; 
  margin-bottom: 20px; 
  color: var(--gold-dim); 
}

/* LOADING */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.brush-loader { 
  width: 50px; 
  height: 50px; 
  border: 3px solid rgba(255,255,255,0.1); 
  border-top-color: var(--gold-main); 
  border-radius: 50%; 
  animation: spin 1s linear infinite; 
  margin-bottom: 20px; 
}

.loading-text { 
  color: var(--gold-dim); 
  font-style: italic; 
}

@keyframes spin { 
  to { transform: rotate(360deg); } 
}

/* ============================================================ */
/* MODAL POPUP - ENHANCED */
/* ============================================================ */

.modal-overlay {
  position: fixed; 
  inset: 0; 
  z-index: 2000;
  background: rgba(0,0,0,0.9);
  backdrop-filter: blur(8px);
  display: flex; 
  align-items: center; 
  justify-content: center;
}

.scroll-container {
  width: 650px; 
  max-width: 92vw;
  display: flex; 
  flex-direction: column;
  position: relative;
  filter: drop-shadow(0 20px 50px rgba(0,0,0,0.9));
}

/* SCROLL RODS - 3D ENHANCED */
.rod {
  height: 32px;
  background: linear-gradient(to right, #3e2723, #6d4c41 15%, #8d6e63 50%, #6d4c41 85%, #3e2723);
  display: flex; 
  align-items: center; 
  justify-content: space-between;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.6), inset 0 2px 0 rgba(255,255,255,0.1);
  z-index: 10;
  position: relative;
}

.rod-mid {
  flex: 1;
  height: 100%;
  background: linear-gradient(to bottom, rgba(255,255,255,0.15), transparent 30%, rgba(0,0,0,0.2) 70%, rgba(0,0,0,0.4));
  border-radius: 16px;
}

.rod-end {
  width: 45px; 
  height: 38px;
  background: radial-gradient(circle at 35% 35%, #ffd54f 0%, #ffb300 40%, #ff6f00 70%, #3e2723 100%);
  border-radius: 6px;
  border: 2px solid #3e2723;
  box-shadow: inset 0 3px 8px rgba(255,255,255,0.5), inset 0 -2px 5px rgba(0,0,0,0.4);
}

.rod-end.left { 
  margin-right: -12px; 
  transform: translateX(-8px); 
}

.rod-end.right { 
  margin-left: -12px; 
  transform: translateX(8px); 
}

/* PAPER SCROLL - ENHANCED */
.paper-content {
  background-color: var(--paper-bg);
  background-image: 
    linear-gradient(0deg, transparent 24%, rgba(139, 69, 19, 0.05) 25%, rgba(139, 69, 19, 0.05) 26%, transparent 27%, transparent 74%, rgba(139, 69, 19, 0.05) 75%, rgba(139, 69, 19, 0.05) 76%, transparent 77%, transparent),
    linear-gradient(90deg, transparent 24%, rgba(139, 69, 19, 0.05) 25%, rgba(139, 69, 19, 0.05) 26%, transparent 27%, transparent 74%, rgba(139, 69, 19, 0.05) 75%, rgba(139, 69, 19, 0.05) 76%, transparent 77%, transparent);
  background-size: 50px 50px;
  color: var(--text-ink);
  overflow-y: auto;
  max-height: 75vh;
  min-height: 400px;
  margin: -8px 15px;
  padding: 0;
  transform-origin: top center;
  animation: unroll 0.6s cubic-bezier(0.25, 1, 0.5, 1);
  box-shadow: 
    inset 0 0 80px rgba(139, 69, 19, 0.12),
    inset 0 0 20px rgba(101, 67, 33, 0.15);
  border-left: 3px solid rgba(139, 69, 19, 0.3);
  border-right: 3px solid rgba(139, 69, 19, 0.3);
}

@keyframes unroll { 
  from { max-height: 0; opacity: 0; transform: scaleY(0); } 
  to { max-height: 75vh; opacity: 1; transform: scaleY(1); } 
}

/* INNER FRAME - ENHANCED */
.inner-border-frame {
  margin: 25px;
  border: 4px double #8d6e63;
  padding: 35px 45px;
  position: relative;
  min-height: 350px;
  background: linear-gradient(to bottom, rgba(255,251,240,0.5), rgba(255,251,240,0));
  box-shadow: inset 0 0 30px rgba(139, 69, 19, 0.08);
}

.inner-border-frame::before {
  content: ""; 
  position: absolute; 
  top: 8px; 
  left: 8px; 
  right: 8px; 
  bottom: 8px;
  border: 1px solid rgba(141, 110, 99, 0.4); 
  pointer-events: none;
}

.paper-inner {
  text-align: left;
  position: relative;
  z-index: 2;
}

/* HEADER BADGE - ENHANCED */
.detail-header {
  display: flex; 
  align-items: center; 
  justify-content: center; 
  gap: 15px;
  margin-bottom: 25px;
}

.badge-type {
  font-family: "Noto Serif TC", serif;
  font-weight: 900; 
  font-size: 1.1rem;
  padding: 8px 28px;
  border-radius: 25px;
  letter-spacing: 2px;
  border: 2px solid currentColor;
  box-shadow: 0 3px 8px rgba(0,0,0,0.15), inset 0 1px 0 rgba(255,255,255,0.3);
  text-shadow: 0 1px 2px rgba(255,255,255,0.5);
}

.type-success { 
  color: #1b5e20; 
  border-color: #2e7d32; 
  background: linear-gradient(135deg, #c8e6c9, #a5d6a7); 
}

.type-warning { 
  color: #e65100; 
  border-color: #ef6c00; 
  background: linear-gradient(135deg, #ffe0b2, #ffcc80); 
}

.type-error { 
  color: #b71c1c; 
  border-color: #c62828; 
  background: linear-gradient(135deg, #ffcdd2, #ef9a9a); 
}

.type-info { 
  color: #0d47a1; 
  border-color: #1565c0; 
  background: linear-gradient(135deg, #bbdefb, #90caf9); 
}

/* TITLE - ENHANCED */
.detail-title {
  text-align: center;
  font-size: 2rem;
  color: #2c241b;
  margin: 0 0 20px 0;
  text-transform: uppercase;
  font-weight: 900;
  letter-spacing: 2px;
  text-shadow: 
    0 1px 0 rgba(255,255,255,0.9),
    0 2px 3px rgba(0,0,0,0.15);
  line-height: 1.3;
}

/* DIVIDER - ENHANCED */
.detail-divider {
  display: flex; 
  align-items: center; 
  justify-content: center; 
  gap: 20px;
  color: #8d6e63; 
  opacity: 0.7; 
  margin-bottom: 30px;
}

.detail-divider .line { 
  height: 2px; 
  width: 80px; 
  background: linear-gradient(to right, transparent, currentColor, transparent); 
}

.detail-divider .center-icon { 
  font-size: 1.5rem; 
  color: #6d4c41;
  filter: drop-shadow(0 2px 3px rgba(0,0,0,0.2));
}

/* BODY TEXT - ENHANCED READABILITY */
.detail-body-text {
  font-size: 1.2rem;
  line-height: 2;
  color: #2c241b;
  text-align: justify;
  margin-bottom: 45px;
  white-space: pre-wrap;
  text-indent: 2em;
  letter-spacing: 0.3px;
  text-shadow: 0 1px 0 rgba(255,255,255,0.5);
}

/* FOOTER - ENHANCED */
.detail-footer {
  display: flex; 
  justify-content: space-between; 
  align-items: flex-end;
  border-top: 2px dashed rgba(141, 110, 99, 0.4);
  padding-top: 25px;
  margin-top: auto;
  position: relative;
}

.meta-info {
  display: flex; 
  flex-direction: column; 
  gap: 10px;
  font-size: 1rem; 
  color: #5d4037;
}

.meta-row { 
  display: flex; 
  align-items: center; 
  gap: 10px; 
}

.meta-row i { 
  color: #8d6e63; 
  font-size: 1.1rem;
}

/* STAMP SEAL - ENHANCED 3D */
.stamp-seal {
  width: 110px; 
  height: 110px;
  border: 4px solid #b71c1c;
  border-radius: 15px;
  display: flex; 
  align-items: center; 
  justify-content: center;
  color: #b71c1c;
  opacity: 0.75;
  transform: rotate(-18deg);
  position: relative;
  right: 5px;
  box-shadow: 
    0 4px 8px rgba(183, 28, 28, 0.3),
    inset 0 0 15px rgba(183, 28, 28, 0.2);
  background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.1), transparent);
}

.seal-inner {
  border: 2px solid #b71c1c;
  padding: 8px; 
  width: 88px; 
  height: 88px;
  display: flex; 
  align-items: center; 
  justify-content: center;
  font-weight: 900; 
  font-size: 1.2rem; 
  text-align: center;
  box-shadow: inset 0 0 0 3px #b71c1c;
  letter-spacing: 1px;
  line-height: 1.2;
}

/* BUTTONS - ENHANCED */
.close-btn-corner {
  position: absolute; 
  top: 15px; 
  right: 15px;
  background: rgba(141, 110, 99, 0.1); 
  border: 1px solid #8d6e63; 
  border-radius: 50%;
  width: 40px;
  height: 40px;
  font-size: 1.3rem;
  color: #8d6e63; 
  cursor: pointer; 
  transition: all 0.3s;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.close-btn-corner:hover { 
  color: #b71c1c; 
  background: rgba(183, 28, 28, 0.1);
  border-color: #b71c1c;
  transform: rotate(90deg) scale(1.1); 
}

.btn-close-main {
  display: block; 
  width: 100%;
  margin-top: 35px;
  padding: 14px;
  background: linear-gradient(135deg, #4e342e, #3e2723);
  color: #d7ccc8;
  border: 2px solid #6d4c41;
  font-family: inherit; 
  font-weight: bold; 
  text-transform: uppercase;
  cursor: pointer; 
  transition: all 0.3s;
  box-shadow: 0 5px 10px rgba(0,0,0,0.3);
  letter-spacing: 1px;
  font-size: 1.05rem;
  border-radius: 4px;
}

.btn-close-main:hover {
  background: linear-gradient(135deg, #5d4037, #4e342e); 
  color: #000000;
  border-color: #8d6e63;
  transform: translateY(-2px);
  box-shadow: 0 7px 15px rgba(0,0,0,0.4);
}

/* CUSTOM SCROLLBAR */
.custom-scroll::-webkit-scrollbar { 
  width: 8px; 
}

.custom-scroll::-webkit-scrollbar-thumb { 
  background: rgba(141, 110, 99, 0.5); 
  border-radius: 4px; 
  transition: 0.3s;
}

.custom-scroll::-webkit-scrollbar-thumb:hover { 
  background: rgba(141, 110, 99, 0.8); 
}

.custom-scroll::-webkit-scrollbar-track { 
  background: rgba(0,0,0,0.05); 
  border-radius: 4px;
}

/* MODAL TRANSITIONS */
.scroll-unroll-enter-active {
  transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}

.scroll-unroll-leave-active {
  transition: all 0.4s cubic-bezier(0.5, 0, 0.75, 0);
}

.scroll-unroll-enter-from {
  opacity: 0;
  transform: scale(0.8) translateY(-50px);
}

.scroll-unroll-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(30px);
}

/* RESPONSIVE */
@media (max-width: 768px) {
  .noti-header {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }
  
  .header-left {
    width: 100%;
    justify-content: center;
  }
  
  .header-right {
    width: 100%;
  }
  
  .btn-royal-seal {
    width: 100%;
    justify-content: center;
  }
  
  .main-title {
    font-size: 1.5rem;
  }
  
  .noti-item {
    padding: 15px;
  }
  
  .item-icon-box {
    width: 50px;
    height: 50px;
    font-size: 1.3rem;
  }
  
  .item-title {
    font-size: 1rem;
  }
  
  .scroll-container {
    width: 95vw;
  }
  
  .inner-border-frame {
    margin: 15px;
    padding: 20px 25px;
  }
  
  .detail-title {
    font-size: 1.5rem;
  }
  
  .detail-body-text {
    font-size: 1.05rem;
    line-height: 1.8;
  }
  
  .detail-footer {
    flex-direction: column;
    gap: 20px;
    align-items: center;
  }
  
  .stamp-seal {
    transform: rotate(-15deg) scale(0.9);
  }
}
</style>