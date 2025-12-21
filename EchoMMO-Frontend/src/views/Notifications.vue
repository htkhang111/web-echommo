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
                <i class="fas fa-caret-right"></i>
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
            <div class="rod-ornament left"></div>
            <div class="rod-body"></div>
            <div class="rod-ornament right"></div>
          </div>

          <div class="paper-content custom-scroll">
            <div class="paper-texture"></div>
            
            <div class="paper-inner">
              <div class="message-header">
                <div class="header-decor-line"></div>
                <span class="msg-type" :class="getTypeClass(selectedNoti.type)">
                  {{ getHeaderTitle(selectedNoti.type) }}
                </span>
                <div class="header-decor-line"></div>
              </div>

              <h3 class="msg-title-text">{{ selectedNoti.title }}</h3>
              
              <div class="msg-divider">
                <i class="fas fa-dragon"></i>
              </div>

              <div class="msg-text-body">
                {{ selectedNoti.message }}
              </div>

              <div class="msg-footer">
                <div class="info-row">
                  <span class="label">Nguồn:</span> <span class="val">Hệ Thống</span>
                </div>
                <div class="info-row">
                  <span class="label">Thời gian:</span> <span class="val">{{ new Date(selectedNoti.createdAt).toLocaleString() }}</span>
                </div>
              </div>

              <div class="official-seal">
                <div class="seal-ring">
                   <span class="seal-txt">ĐÃ DUYỆT</span>
                </div>
              </div>
            </div>
            
            <button class="close-btn" @click="selectedNoti = null">
               <i class="fas fa-times"></i>
            </button>
          </div>

          <div class="rod rod-bottom">
            <div class="rod-ornament left"></div>
            <div class="rod-body"></div>
            <div class="rod-ornament right"></div>
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

// Particles Logic (Random floating particles)
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

/* ================= VARIABLES ================= */
:root {
  --ink-black: #1a1a1a;
  --ink-light: #2c2c2c;
  --gold-main: #d4af37;
  --gold-dim: #8a7338;
  --gold-bright: #ffdf80;
  --red-seal: #b22222;
  --paper-bg: #f5e6d3;
  --text-white: #f0f0f0;
  --text-gray: #a0a0a0;
}

.ink-theme {
  font-family: "Noto Serif TC", serif;
  color: var(--text-white);
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* ================= BACKGROUND ================= */
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #000; overflow: hidden; }
.mountain-bg { 
  position: absolute; inset: 0; 
  background-size: cover; background-position: center; 
  opacity: 0.5; filter: grayscale(40%) contrast(1.1); 
  transition: opacity 1s;
}
.ink-overlay { 
  position: absolute; inset: 0; 
  background: radial-gradient(circle at center, rgba(20,20,20,0.4) 0%, rgba(0,0,0,0.9) 100%);
  mix-blend-mode: multiply;
}
.ink-overlay.night-mode { background: radial-gradient(circle at center, rgba(10,10,25,0.6) 0%, rgba(0,0,5,0.95) 100%); }

/* Particles */
.particles { position: absolute; inset: 0; pointer-events: none; }
.particle {
  position: absolute; bottom: -10px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  animation: floatUp linear infinite;
}
@keyframes floatUp {
  0% { transform: translateY(0) translateX(0); opacity: 0; }
  20% { opacity: 0.5; }
  80% { opacity: 0.5; }
  100% { transform: translateY(-110vh) translateX(20px); opacity: 0; }
}

/* ================= LAYOUT ================= */
.noti-layout {
  position: relative; z-index: 10;
  max-width: 900px; margin: 0 auto;
  height: 100vh;
  display: flex; flex-direction: column;
  padding: 20px;
}

/* HEADER */
.noti-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
  padding: 15px 25px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid var(--gold-dim);
  border-radius: 8px;
  backdrop-filter: blur(8px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
}

.header-left { display: flex; align-items: center; gap: 20px; }
.btn-ink-back {
  background: transparent; border: none; cursor: pointer; padding: 0;
}
.btn-content {
  width: 45px; height: 45px;
  border: 2px solid var(--gold-dim);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
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

.title-group { display: flex; flex-direction: column; }
.main-title {
  margin: 0; font-size: 2rem;
  background: linear-gradient(to bottom, #fff, #bbb);
  -webkit-background-clip: text; color: transparent;
  font-weight: 900; letter-spacing: 2px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.8);
}
.sub-title { font-size: 0.9rem; color: var(--gold-dim); font-style: italic; margin-top: -2px; }

.btn-royal-seal {
  background: linear-gradient(135deg, #800000, #500000);
  border: 1px solid #ff4d4d;
  color: #fff; padding: 10px 20px;
  border-radius: 4px; cursor: pointer;
  font-weight: bold; font-family: inherit;
  display: flex; align-items: center; gap: 8px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.5);
  transition: all 0.3s;
}
.btn-royal-seal:hover {
  background: linear-gradient(135deg, #a00000, #700000);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(178, 34, 34, 0.4);
}

/* LIST CONTENT */
.content-frame { flex: 1; overflow: hidden; position: relative; }
.noti-list {
  height: 100%; overflow-y: auto; padding-right: 10px;
  padding-bottom: 20px;
}

/* NOTIFICATION ITEM */
.noti-item {
  position: relative;
  background: rgba(30, 30, 30, 0.85);
  margin-bottom: 12px;
  padding: 15px;
  border-radius: 4px;
  display: flex; align-items: center; gap: 15px;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  animation: slideIn 0.5s ease forwards;
  animation-delay: calc(var(--i) * 0.05s);
  opacity: 0; transform: translateY(20px);
}
@keyframes slideIn { to { opacity: 1; transform: translateY(0); } }

.noti-item:hover {
  background: rgba(45, 45, 45, 0.95);
  border-color: var(--gold-dim);
  transform: translateX(5px) scale(1.01);
  box-shadow: 0 5px 20px rgba(0,0,0,0.5);
}

/* Status & Types */
.noti-item.is-read { opacity: 0.7; }
.noti-item.is-read:hover { opacity: 1; }

.glow-border {
  position: absolute; inset: 0;
  border: 1px solid var(--gold-main);
  box-shadow: inset 0 0 15px rgba(212, 175, 55, 0.2);
  pointer-events: none;
  animation: pulseGlow 2s infinite alternate;
}
@keyframes pulseGlow { 0% { opacity: 0.5; } 100% { opacity: 1; box-shadow: inset 0 0 25px rgba(212, 175, 55, 0.4); } }

.item-icon-box {
  width: 50px; height: 50px;
  background: #111;
  border: 1px solid #444;
  border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.4rem;
  flex-shrink: 0;
  position: relative; z-index: 2;
  box-shadow: inset 0 0 10px rgba(0,0,0,0.8);
}

.type-success .item-icon-box { color: #4caf50; border-color: #2e7d32; }
.type-warning .item-icon-box { color: #ff9800; border-color: #ef6c00; }
.type-error .item-icon-box { color: #f44336; border-color: #c62828; }
.type-info .item-icon-box { color: var(--gold-main); border-color: var(--gold-dim); }

.item-body { flex: 1; overflow: hidden; }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 5px; }
.item-title { font-weight: bold; font-size: 1.1rem; color: #fff; }
.time-stamp { font-size: 0.8rem; color: var(--text-gray); font-style: italic; }
.item-desc { font-size: 0.95rem; color: #ccc; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.item-action { color: var(--gold-dim); font-size: 0.8rem; transition: 0.3s; opacity: 0; transform: translateX(-10px); }
.noti-item:hover .item-action { opacity: 1; transform: translateX(0); }

/* EMPTY STATE */
.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 300px; color: var(--text-gray); opacity: 0.6;
}
.empty-icon { font-size: 4rem; margin-bottom: 20px; color: var(--gold-dim); }

/* LOADING */
.brush-loader {
  width: 50px; height: 50px;
  border: 3px solid rgba(255,255,255,0.1);
  border-top-color: var(--gold-main);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 100px auto 20px;
}
.loading-text { display: block; text-align: center; color: var(--gold-dim); font-style: italic; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ================= MODAL SCROLL ================= */
.modal-overlay {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(0,0,0,0.85);
  backdrop-filter: blur(5px);
  display: flex; align-items: center; justify-content: center;
}

.scroll-container {
  width: 600px; max-width: 90vw;
  max-height: 85vh;
  display: flex; flex-direction: column;
  position: relative;
  filter: drop-shadow(0 0 30px rgba(0,0,0,0.9));
}

/* RODS */
.rod {
  height: 24px;
  background: linear-gradient(90deg, #3e2723, #5d4037 20%, #5d4037 80%, #3e2723);
  position: relative; z-index: 5;
  display: flex; align-items: center; justify-content: space-between;
  border-radius: 2px;
  box-shadow: 0 5px 10px rgba(0,0,0,0.5);
}
.rod-ornament {
  width: 30px; height: 32px;
  background: radial-gradient(circle, var(--gold-main) 10%, #5d4037 90%);
  border: 2px solid #2d1b18;
  border-radius: 4px;
}
.rod-ornament.left { transform: translateX(-5px); }
.rod-ornament.right { transform: translateX(5px); }

/* PAPER */
.paper-content {
  background: var(--paper-bg);
  position: relative;
  overflow: hidden;
  padding: 40px;
  color: #2c2c2c;
  transform-origin: top;
  animation: unroll 0.6s ease-out;
}
@keyframes unroll { from { max-height: 0; opacity: 0; } to { max-height: 80vh; opacity: 1; } }

.paper-texture {
  position: absolute; inset: 0;
  background-image: url("https://www.transparenttextures.com/patterns/aged-paper.png");
  opacity: 0.6; pointer-events: none;
}

.paper-inner { position: relative; z-index: 2; text-align: center; }

.message-header {
  display: flex; align-items: center; justify-content: center; gap: 15px;
  margin-bottom: 30px;
}
.header-decor-line { height: 1px; width: 50px; background: #8a7338; opacity: 0.5; }

.msg-type { font-weight: 900; font-size: 1.2rem; letter-spacing: 2px; }
.msg-type.type-success { color: #2e7d32; }
.msg-type.type-warning { color: #e65100; }
.msg-type.type-error { color: #c62828; }
.msg-type.type-info { color: #0d47a1; }

.msg-title-text {
  font-size: 1.8rem; font-weight: 900;
  color: #1a1a1a; margin: 0 0 10px;
  text-transform: uppercase;
}

.msg-divider { color: var(--gold-dim); font-size: 1.5rem; margin: 15px 0 25px; opacity: 0.8; }

.msg-text-body {
  font-size: 1.1rem; line-height: 1.8;
  text-align: justify; margin-bottom: 40px;
  color: #333; font-weight: 500;
}

.msg-footer {
  border-top: 1px solid rgba(138, 115, 56, 0.3);
  padding-top: 15px;
  display: flex; justify-content: space-between;
  font-size: 0.9rem; color: #5d4037;
  font-style: italic;
}
.info-row .val { font-weight: bold; }

/* SEAL STAMP */
.official-seal {
  position: absolute; bottom: 10px; right: 10px;
  opacity: 0.8;
  transform: rotate(-15deg);
  pointer-events: none;
}
.seal-ring {
  width: 100px; height: 100px;
  border: 4px double var(--red-seal);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: inset 0 0 10px rgba(178, 34, 34, 0.2);
}
.seal-txt {
  color: var(--red-seal);
  font-weight: 900; font-size: 1.2rem;
  text-transform: uppercase;
  border-top: 2px solid var(--red-seal);
  border-bottom: 2px solid var(--red-seal);
  padding: 5px;
}

/* Close Button */
.close-btn {
  position: absolute; top: 15px; right: 15px;
  background: none; border: none;
  font-size: 1.5rem; color: #8a7338;
  cursor: pointer; transition: 0.2s;
  z-index: 10;
}
.close-btn:hover { color: #b22222; transform: rotate(90deg); }

/* SCROLLBAR */
.custom-scroll::-webkit-scrollbar { width: 5px; }
.custom-scroll::-webkit-scrollbar-thumb { background: var(--gold-dim); border-radius: 4px; }
.custom-scroll::-webkit-scrollbar-track { background: transparent; }
</style>