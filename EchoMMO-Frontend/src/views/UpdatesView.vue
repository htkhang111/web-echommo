<template>
  <div class="page-container wuxia-updates jade-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="pattern-overlay"></div>
      <div class="mist-layer"></div>
    </div>

    <div class="updates-layout custom-scroll">
      <header class="page-header fade-in-down">
        <div class="seal-mark-bg">SỬ</div>
        <h1 class="main-title">THIÊN THƯ BIÊN NIÊN</h1>
        <div class="decor-line">
          <span class="dragon-head left"></span>
          <span class="gold-line"></span>
          <span class="dragon-head right"></span>
        </div>
        <p class="sub-title">"Ghi chép lại những biến động của càn khôn vũ trụ"</p>
      </header>

      <div class="timeline-container">
        <div class="timeline-spine"></div>

        <div 
          v-for="(update, index) in updates" 
          :key="index" 
          class="timeline-item fade-in-up"
          :class="getUpdateTypeClass(update.type)"
          :style="{ '--delay': `${index * 0.15}s` }"
        >
          <div class="time-node">
            <div class="node-icon">
              <i :class="getNodeIcon(update.type)"></i>
            </div>
            <div class="node-ripple"></div>
          </div>

          <div class="update-card glass-panel">
            <div class="corner top-left"></div>
            <div class="corner top-right"></div>
            <div class="corner bottom-left"></div>
            <div class="corner bottom-right"></div>

            <div class="card-header">
              <div class="version-badge">
                <span>{{ update.version }}</span>
              </div>
              <div class="update-date">
                <i class="far fa-calendar-alt"></i> {{ update.date }}
              </div>
            </div>
            
            <h3 class="update-title">
              {{ update.title }}
            </h3>

            <div class="divider-dash"></div>

            <ul class="changes-list">
              <li v-for="(change, idx) in update.changes" :key="idx" class="change-item">
                <div class="bullet-point" :class="change.type">
                  <i :class="getChangeIcon(change.type)"></i>
                </div>
                <span class="change-text">{{ change.text }}</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="timeline-end fade-in">
          <div class="end-gem">
            <i class="fas fa-book-dead"></i>
          </div>
          <span class="end-text">KHỞI NGUYÊN</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png"; 

// Dữ liệu mẫu
const updates = ref([
  {
    version: "v1.0.2 - Open Beta",
    date: "08/12/2025",
    type: "MAJOR", // Đại Cập Nhật
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
    type: "MINOR", // Tiểu Cập Nhật
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
    type: "INIT", // Khởi đầu
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
    case 'fix': return 'fas fa-hammer';
    default: return 'fas fa-circle';
  }
};

const getUpdateTypeClass = (type) => {
    return type.toLowerCase(); // major, minor, init
};

const getNodeIcon = (type) => {
    switch(type) {
        case 'MAJOR': return 'fas fa-dragon';
        case 'MINOR': return 'fas fa-scroll';
        case 'INIT': return 'fas fa-egg';
        default: return 'fas fa-dot-circle';
    }
};
</script>

<style scoped>
/* Import Font Xịn */
@import url("https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;600;700&family=Noto+Serif:wght@400;700&display=swap");

.wuxia-updates {
  min-height: 100vh;
  background-color: #050505;
  color: #e0e0e0;
  font-family: "Noto Serif", serif;
  position: relative;
  overflow: hidden;
}

/* BACKGROUND */
.bg-layer { position: fixed; inset: 0; pointer-events: none; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0.2; filter: grayscale(80%) sepia(20%); }
.pattern-overlay {
  position: absolute; inset: 0;
  background-image: url("https://www.transparenttextures.com/patterns/black-scales.png");
  opacity: 0.4;
}
.mist-layer {
  position: absolute; inset: 0;
  background: radial-gradient(circle at 50% 0%, rgba(20, 50, 40, 0.4), transparent 70%);
}

/* LAYOUT */
.updates-layout {
  position: relative; z-index: 10;
  max-width: 900px; margin: 0 auto;
  height: 100vh; overflow-y: auto;
  padding: 40px 20px;
}
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #004d40; border-radius: 3px; }

/* HEADER */
.page-header { text-align: center; margin-bottom: 60px; position: relative; }
.seal-mark-bg {
  position: absolute; top: -30px; left: 50%; transform: translateX(-50%);
  font-size: 10rem; color: #b71c1c; 
  font-family: "Cormorant Garamond", serif;
  opacity: 0.08; font-weight: 900; pointer-events: none; z-index: -1;
}
.main-title {
  font-family: "Cormorant Garamond", serif; font-size: 3.5rem; font-weight: 700;
  color: #e0f2f1; text-transform: uppercase; margin: 0;
  text-shadow: 0 0 20px rgba(0, 230, 118, 0.3);
  letter-spacing: 3px;
}
.decor-line { display: flex; align-items: center; justify-content: center; gap: 15px; margin: 15px 0; }
.gold-line { width: 250px; height: 2px; background: linear-gradient(90deg, transparent, #ffd700, transparent); }
.dragon-head {
  width: 10px; height: 10px; background: #00bfa5;
  transform: rotate(45deg); box-shadow: 0 0 10px #00bfa5;
}
.sub-title { color: #80cbc4; font-style: italic; letter-spacing: 1px; font-size: 1.1rem; }

/* TIMELINE CONTAINER */
.timeline-container { position: relative; padding-left: 50px; padding-bottom: 50px; }
.timeline-spine {
  position: absolute; left: 19px; top: 0; bottom: 0; width: 2px;
  background: linear-gradient(to bottom, #00bfa5, #ffd700, transparent);
  box-shadow: 0 0 10px rgba(0, 191, 165, 0.3);
}

.timeline-item { position: relative; margin-bottom: 50px; animation: fadeInUp 0.6s ease backwards; animation-delay: var(--delay); }

/* TIME NODE */
.time-node {
  position: absolute; left: -50px; top: 20px;
  width: 40px; height: 40px;
  display: flex; align-items: center; justify-content: center;
  z-index: 2;
}
.node-icon {
  width: 32px; height: 32px; border-radius: 50%;
  background: #000; border: 2px solid #555;
  color: #555; display: flex; align-items: center; justify-content: center;
  font-size: 1rem; position: relative; z-index: 2;
  transition: 0.3s;
}
.node-ripple {
  position: absolute; inset: 0; border-radius: 50%;
  border: 1px solid transparent; animation: ripple 2s infinite; opacity: 0;
}

/* Colors based on Type */
.timeline-item.major .node-icon { border-color: #ffd700; color: #ffd700; background: #221; box-shadow: 0 0 15px rgba(255, 215, 0, 0.4); }
.timeline-item.major .node-ripple { border-color: #ffd700; opacity: 1; }

.timeline-item.minor .node-icon { border-color: #00bfa5; color: #00bfa5; background: #001; box-shadow: 0 0 15px rgba(0, 191, 165, 0.4); }
.timeline-item.minor .node-ripple { border-color: #00bfa5; opacity: 1; }

.timeline-item.init .node-icon { border-color: #b71c1c; color: #b71c1c; background: #100; }

/* UPDATE CARD */
.update-card {
  position: relative;
  background: rgba(20, 20, 20, 0.9);
  border: 1px solid #333;
  border-radius: 4px;
  padding: 25px;
  transition: 0.3s;
  backdrop-filter: blur(10px);
}
.update-card:hover { transform: translateX(5px); border-color: #555; box-shadow: 0 5px 20px rgba(0,0,0,0.5); }

/* Decorative Corners */
.corner { position: absolute; width: 10px; height: 10px; border-style: solid; border-color: #555; transition: 0.3s; }
.corner.top-left { top: -1px; left: -1px; border-width: 2px 0 0 2px; }
.corner.top-right { top: -1px; right: -1px; border-width: 2px 2px 0 0; }
.corner.bottom-left { bottom: -1px; left: -1px; border-width: 0 0 2px 2px; }
.corner.bottom-right { bottom: -1px; right: -1px; border-width: 0 2px 2px 0; }

.timeline-item.major .update-card { border-color: rgba(255, 215, 0, 0.3); }
.timeline-item.major .corner { border-color: #ffd700; }
.timeline-item.minor .update-card { border-color: rgba(0, 191, 165, 0.3); }
.timeline-item.minor .corner { border-color: #00bfa5; }

/* Card Content */
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.version-badge {
  background: #333; color: #fff; padding: 4px 12px;
  border-radius: 4px; font-weight: bold; font-family: "Cormorant Garamond", serif;
  border: 1px solid #555; letter-spacing: 1px;
}
.timeline-item.major .version-badge { background: linear-gradient(90deg, #b78900, #ffca28); color: #000; border: none; }
.timeline-item.minor .version-badge { background: linear-gradient(90deg, #00695c, #00bfa5); color: #fff; border: none; }

.update-date { color: #888; font-size: 0.9rem; font-style: italic; }

.update-title {
  font-family: "Cormorant Garamond", serif; font-size: 1.8rem;
  margin: 0 0 15px 0; font-weight: 700; color: #e0f2f1;
}
.timeline-item.major .update-title { color: #ffd700; text-shadow: 0 0 10px rgba(255, 215, 0, 0.2); }
.timeline-item.minor .update-title { color: #00bfa5; }

.divider-dash { height: 1px; background: repeating-linear-gradient(90deg, #444 0, #444 5px, transparent 5px, transparent 10px); margin-bottom: 15px; }

/* Changes List */
.changes-list { list-style: none; padding: 0; margin: 0; }
.change-item { display: flex; align-items: flex-start; gap: 15px; margin-bottom: 12px; }
.bullet-point {
  min-width: 24px; height: 24px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.8rem; background: rgba(255,255,255,0.05);
}
.bullet-point.new { color: #ffd700; border: 1px solid #ffd700; }
.bullet-point.balance { color: #29b6f6; border: 1px solid #29b6f6; }
.bullet-point.fix { color: #ef5350; border: 1px solid #ef5350; }

.change-text { line-height: 1.6; color: #ccc; }

/* END TIMELINE */
.timeline-end { display: flex; flex-direction: column; align-items: center; margin-top: 30px; margin-left: -32px; opacity: 0.7; }
.end-gem {
  width: 50px; height: 50px; background: #000; border: 2px solid #b71c1c;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  color: #b71c1c; font-size: 1.5rem; margin-bottom: 10px;
  box-shadow: 0 0 20px rgba(183, 28, 28, 0.2);
}
.end-text { font-family: "Cormorant Garamond", serif; font-weight: 700; letter-spacing: 2px; color: #b71c1c; }

/* ANIMATIONS */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
.fade-in-down { animation: fadeInDown 0.8s ease; }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes ripple { 0% { transform: scale(1); opacity: 0.6; } 100% { transform: scale(1.8); opacity: 0; } }

/* RESPONSIVE */
@media (max-width: 600px) {
  .timeline-container { padding-left: 20px; }
  .timeline-spine { left: 9px; }
  .time-node { left: -20px; width: 20px; height: 20px; top: 25px; }
  .node-icon { width: 20px; height: 20px; font-size: 0.7rem; }
  .timeline-end { margin-left: -10px; }
  .end-gem { width: 40px; height: 40px; font-size: 1.2rem; }
}
</style>