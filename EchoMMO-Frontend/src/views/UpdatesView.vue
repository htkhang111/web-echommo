<template>
  <div class="page-container wuxia-updates ink-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="ink-overlay"></div>
      <div class="particles"></div>
    </div>

    <div class="updates-layout custom-scroll">
      <header class="page-header fade-in-down">
        <h1 class="main-title">THIÊN THƯ BIÊN NIÊN</h1>
        <div class="decor-line">
          <span class="dot left"></span>
          <span class="line"></span>
          <i class="fas fa-book-open center-icon"></i>
          <span class="line"></span>
          <span class="dot right"></span>
        </div>
        <p class="sub-title">Ghi chép lại những biến động của càn khôn</p>
      </header>

      <div class="timeline-container">
        <div class="timeline-spine"></div>

        <div 
          v-for="(update, index) in updates" 
          :key="index" 
          class="timeline-item fade-in-up"
          :style="{ '--delay': `${index * 0.1}s` }"
        >
          <div class="time-node">
            <div class="node-inner"></div>
            <div class="node-glow"></div>
          </div>

          <div class="update-card glass-panel">
            <div class="card-header">
              <div class="version-tag">
                <i class="fas fa-tag"></i> {{ update.version }}
              </div>
              <div class="update-date">
                <i class="far fa-calendar-alt"></i> {{ update.date }}
              </div>
            </div>
            
            <div class="update-type-label" :class="getUpdateTypeClass(update.type)">
                {{ update.title }}
            </div>

            <ul class="changes-list">
              <li v-for="(change, idx) in update.changes" :key="idx" class="change-item">
                <div class="icon-box" :class="getChangeTypeClass(change.type)">
                  <i :class="getChangeIcon(change.type)"></i>
                </div>
                <span class="change-text">{{ change.text }}</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="timeline-end">
          <i class="fas fa-infinity"></i>
          <span>Khởi Nguyên</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// Sử dụng chung background với các trang khác để đồng bộ
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png"; 

// Dữ liệu mẫu (Sau này có thể fetch từ API)
const updates = ref([
  {
    version: "v1.0.2 - Open Beta",
    date: "08/12/2025",
    type: "MAJOR",
    title: "Khai Mở Giang Hồ",
    changes: [
      { type: "new", text: "Ra mắt tính năng Bang Hội tranh hùng." },
      { type: "balance", text: "Cân bằng sức mạnh phái Võ Đang (Giảm sát thương Thái Cực Kiếm)." },
      { type: "fix", text: "Sửa lỗi hiển thị Túi Đồ khi mạng lag." },
      { type: "new", text: "Thêm sự kiện Đua Top Server." }
    ]
  },
  {
    version: "v1.0.1 - Alpha Test 2",
    date: "01/11/2025",
    type: "MINOR",
    title: "Ngũ Hành Tương Khắc",
    changes: [
      { type: "new", text: "Mở bản đồ Rừng Trúc (Map 20-30)." },
      { type: "new", text: "Kích hoạt hệ thống Ngũ Hành (Kim, Mộc, Thủy, Hỏa, Thổ)." },
      { type: "fix", text: "Vá lỗi kẹt map tại Tân Thủ Thôn." }
    ]
  },
  {
    version: "v1.0.0 - Alpha Test 1",
    date: "15/10/2025",
    type: "INIT",
    title: "Sơ Nhập Giang Hồ",
    changes: [
      { type: "new", text: "Hệ thống nhân vật cơ bản." },
      { type: "new", text: "Hệ thống chiến đấu Turn-base sơ khai." }
    ]
  }
]);

// Helper functions
const getChangeIcon = (type) => {
  switch (type) {
    case 'new': return 'fas fa-star';
    case 'balance': return 'fas fa-balance-scale';
    case 'fix': return 'fas fa-wrench';
    default: return 'fas fa-circle';
  }
};

const getChangeTypeClass = (type) => {
  switch (type) {
    case 'new': return 'text-gold';
    case 'balance': return 'text-blue';
    case 'fix': return 'text-red';
    default: return 'text-gray';
  }
};

const getUpdateTypeClass = (type) => {
    switch(type) {
        case 'MAJOR': return 'major';
        case 'MINOR': return 'minor';
        default: return 'init';
    }
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700&family=Noto+Serif+TC:wght@400;700&display=swap");

.wuxia-updates {
  min-height: 100vh;
  background-color: #0a0a0a;
  color: #e0e0e0;
  font-family: "Noto Serif TC", serif;
  position: relative;
  overflow: hidden;
}

/* Background Layers */
.bg-layer { position: fixed; inset: 0; pointer-events: none; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0.3; filter: grayscale(60%) contrast(1.1); }
.ink-overlay { position: absolute; inset: 0; background: radial-gradient(circle at center, rgba(0,0,0,0.4), #000); }

/* Layout */
.updates-layout {
  position: relative;
  z-index: 10;
  max-width: 900px;
  margin: 0 auto;
  height: 100vh;
  overflow-y: auto;
  padding: 40px 20px;
}

/* Scrollbar */
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0,0,0,0.2); }

/* Header */
.page-header { text-align: center; margin-bottom: 60px; }
.main-title {
  font-family: "Cinzel", serif;
  font-size: 3rem;
  color: #ffca28;
  text-transform: uppercase;
  margin: 0;
  text-shadow: 0 0 15px rgba(255, 202, 40, 0.4);
  background: -webkit-linear-gradient(#ffd700, #ff8c00);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.sub-title { color: #888; font-style: italic; letter-spacing: 1px; margin-top: 10px; }

.decor-line { display: flex; align-items: center; justify-content: center; gap: 15px; margin-top: 15px; opacity: 0.8; }
.line { height: 2px; width: 100px; background: linear-gradient(90deg, transparent, #ffca28, transparent); }
.center-icon { color: #ffca28; font-size: 1.5rem; }
.dot { width: 6px; height: 6px; background: #ffca28; border-radius: 50%; transform: rotate(45deg); }

/* Timeline */
.timeline-container { position: relative; padding-left: 40px; padding-bottom: 50px; }
.timeline-spine {
  position: absolute;
  left: 14px; /* (30px node / 2) - (2px line / 2) */
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #ffca28, #5d4037, transparent);
  box-shadow: 0 0 10px rgba(255, 202, 40, 0.2);
}

.timeline-item { position: relative; margin-bottom: 50px; animation: fadeInUp 0.6s ease backwards; animation-delay: var(--delay); }

/* Node (Cục tròn trên dòng kẻ) */
.time-node {
  position: absolute;
  left: -40px; /* Điều chỉnh để khớp với timeline-spine */
  top: 25px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.node-inner {
  width: 14px; height: 14px;
  background: #000;
  border: 2px solid #ffca28;
  transform: rotate(45deg);
  z-index: 2;
}
.node-glow {
  position: absolute;
  width: 100%; height: 100%;
  background: radial-gradient(circle, rgba(255,202,40,0.4), transparent);
  animation: pulse 2s infinite;
}

/* Card */
.update-card {
  background: rgba(20, 20, 20, 0.8);
  border: 1px solid rgba(255, 215, 0, 0.15);
  border-radius: 8px;
  padding: 25px;
  backdrop-filter: blur(10px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.5);
  position: relative;
  transition: transform 0.3s;
}
.update-card:hover { transform: translateX(5px); border-color: rgba(255, 215, 0, 0.4); }

/* Mũi tên chỉ vào card */
.update-card::before {
  content: "";
  position: absolute;
  left: -8px;
  top: 32px;
  width: 16px; height: 16px;
  background: rgba(20, 20, 20, 0.8);
  border-left: 1px solid rgba(255, 215, 0, 0.15);
  border-bottom: 1px solid rgba(255, 215, 0, 0.15);
  transform: rotate(45deg);
}

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; border-bottom: 1px dashed #444; padding-bottom: 10px; }
.version-tag {
  background: linear-gradient(90deg, #ffca28, #ff6f00);
  color: #000;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: bold;
  font-family: "Cinzel", serif;
  font-size: 0.9rem;
  box-shadow: 0 0 10px rgba(255, 202, 40, 0.3);
}
.update-date { color: #888; font-size: 0.9rem; font-style: italic; }

.update-type-label { font-size: 1.2rem; font-weight: bold; text-transform: uppercase; margin-bottom: 15px; letter-spacing: 1px; }
.update-type-label.major { color: #ffca28; text-shadow: 0 0 5px #ffca28; }
.update-type-label.minor { color: #29b6f6; }
.update-type-label.init { color: #66bb6a; }

.changes-list { list-style: none; padding: 0; margin: 0; }
.change-item { display: flex; align-items: flex-start; gap: 15px; margin-bottom: 12px; }
.icon-box {
  min-width: 24px; height: 24px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.05);
  font-size: 0.8rem;
}
.text-gold { color: #ffca28; border: 1px solid #ffca28; }
.text-blue { color: #29b6f6; border: 1px solid #29b6f6; }
.text-red { color: #ef5350; border: 1px solid #ef5350; }
.change-text { line-height: 1.5; color: #ccc; }

.timeline-end {
  text-align: center;
  color: #5d4037;
  font-size: 1.5rem;
  margin-left: -26px; /* Căn chỉnh với line */
  opacity: 0.6;
  margin-top: 20px;
}
.timeline-end span { display: block; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 2px; margin-top: 5px; }

/* Animations */
.fade-in-down { animation: fadeInDown 0.8s ease; }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes pulse { 0% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.5); opacity: 0; } 100% { transform: scale(1); opacity: 0; } }

/* Responsive */
@media (max-width: 600px) {
  .page-header { margin-bottom: 40px; }
  .main-title { font-size: 2rem; }
  .timeline-container { padding-left: 20px; }
  .timeline-spine { left: 9px; }
  .time-node { left: -26px; width: 20px; height: 20px; }
  .node-inner { width: 10px; height: 10px; }
  .update-card::before { left: -6px; top: 22px; }
}
</style>