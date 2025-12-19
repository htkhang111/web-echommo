<!-- <template>
  <div class="page-container wuxia-dashboard">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
      </div>

    <div class="dashboard-wrapper">
      <div class="command-header">
        <div class="header-left">
          <div class="sys-badge">
            <span class="status-dot online">●</span> MÁY CHỦ: ỔN ĐỊNH
          </div>
          <h1 class="welcome-text">
            CHÀO MỪNG,
            <span
              class="user-rank"
              :data-text="authStore.user?.username || 'ĐẠI HIỆP'"
            >
              {{ authStore.user?.username || "ĐẠI HIỆP" }}
            </span>
          </h1>
          <div class="player-stats">
            <span class="stat-pill"> <i class="fas fa-signal"></i> 12ms </span>
            <span class="stat-pill">
              <i class="fas fa-server"></i> MÁY CHỦ 1
            </span>
          </div>
        </div>
        <div class="header-right">
          <div class="weather-widget">
            <div class="weather-icon"><i class="fas fa-moon"></i></div>
            <div class="weather-info">
              <span class="location">HẮC PHONG SƠN</span>
              <span class="temp">Canh Ba / Trăng Khuyết</span>
            </div>
          </div>
        </div>
      </div>

      <div class="command-grid">
        <router-link to="/game" class="cmd-card play-card">
          <div class="card-bg-ink"></div>
          <div class="card-content">
            <div class="play-content-left">
              <div class="icon-wrapper">
                <i class="fas fa-dragon"></i>
              </div>
              <div class="text-group">
                <h2>NHẬP THẾ</h2>
                <p>BƯỚC VÀO GIANG HỒ</p>
              </div>
            </div>
            <div class="play-arrow">
              <i class="fas fa-chevron-right"></i>
            </div>
          </div>
        </router-link>

        <router-link to="/inventory" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-box-open"></i></div>
            <h3>HÀNH TRANG</h3>
            <p>Vật Phẩm & Bảo Bối</p>
          </div>
        </router-link>

        <router-link to="/market" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-store"></i></div>
            <h3>THƯƠNG HỘI</h3>
            <p>Giao Thương</p>
          </div>
        </router-link>

        <router-link to="/ranking" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-trophy"></i></div>
            <h3>BẢNG VÀNG</h3>
            <p>Danh Chấn Giang Hồ</p>
          </div>
        </router-link>

        <router-link
          v-if="authStore.user?.role === 'ADMIN'"
          to="/admin"
          class="cmd-card admin-card"
        >
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-gavel"></i></div>
            <h3>QUAN PHỦ</h3>
            <p>Điều Hành Hệ Thống</p>
          </div>
        </router-link>
      </div>

      <div class="news-ticker-box">
        <div class="ticker-label"><i class="fas fa-scroll"></i> CÁO THỊ</div>
        <div class="ticker-track">
          <div class="ticker-content">
            <span class="ticker-item">⚠️ BẢO TRÌ: Giờ Tý canh ba</span>
            <span class="ticker-separator">❖</span>
            <span class="ticker-item highlight"
              >💎 SỰ KIỆN: Nhân đôi kinh nghiệm</span
            >
            <span class="ticker-separator">❖</span>
            <span class="ticker-item"
              >⚔️ BOSS: Hắc Long đã xuất hiện tại Bắc Sơn</span
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();
onMounted(() => {
  if (authStore.token) authStore.fetchProfile();
});
</script>

<style scoped>
/* Dùng Noto Serif TC cho cả body và header để nhất quán, dễ đọc hơn Playfair ở nhiều kích thước */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

/* --- VARIABLES --- */
:root {
  --paper-bg: #e3d5b8;
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --ink: #212121;
  --red-seal: #b71c1c;
  --gold: #ffecb3; /* Vàng sáng hơn để dễ đọc trên nền tối */
  --text-light: #f3f4f6; /* Màu chữ chính (Trắng kem/Light Grey) */
  --text-dim: #a1887f; /* Màu chữ mờ */
}

/* --- BASE LAYOUT --- */
.wuxia-dashboard {
  min-height: 100vh;
  background-color: var(--wood-dark);
  /* Màu chữ mặc định của trang là màu sáng */
  color: var(--text-light);
  font-family: "Noto Serif TC", serif;
  position: relative;
  overflow: hidden;
}

.dashboard-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* --- BACKGROUND EFFECTS --- */
/* Bỏ paper-overlay vì chúng ta đã chuyển sang nền tối */
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
  background-position: center bottom;
  filter: sepia(30%) brightness(0.7) contrast(1.1);
  opacity: 0.9;
}

.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(62, 39, 35, 0.9) 0%,
    transparent 60%
  );
}

/* --- HEADER --- */
.command-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 20px;
  border-bottom: 3px double rgba(255, 255, 255, 0.3);
}

.sys-badge {
  font-family: "Noto Serif TC", serif;
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--gold);
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-shadow: 1px 1px 2px #000;
}
.status-dot {
  color: #4caf50;
  font-size: 1.2em;
  text-shadow: 0 0 5px #4caf50;
}

.welcome-text {
  font-size: 2.5rem;
  font-weight: 900;
  margin: 0;
  line-height: 1.1;
  color: #ffffff; /* Chữ trắng */
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.user-rank {
  color: var(--gold);
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  font-family: "Noto Serif TC", serif;
}

.player-stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.stat-pill {
  font-size: 0.85rem;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 5px 15px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.weather-widget {
  background: rgba(0, 0, 0, 0.6);
  padding: 10px 20px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}
.weather-icon i {
  font-size: 1.8rem;
  color: var(--gold);
}
.weather-info {
  display: flex;
  flex-direction: column;
  text-align: right;
  font-family: "Noto Serif TC";
}
.location {
  font-size: 0.8rem;
  color: var(--gold);
  font-weight: bold;
  letter-spacing: 1px;
}
.temp {
  font-size: 1rem;
  color: #fff;
  font-weight: bold;
}

/* --- GRID SYSTEM --- */
.command-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-auto-rows: 180px;
  gap: 20px;
  flex-grow: 1;
}

/* Base Card Style (Đã chuyển sang nền tối/gỗ) */
.cmd-card {
  position: relative;
  text-decoration: none;
  background: var(--wood-dark); /* Nền gỗ tối */
  border: 4px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.cmd-card:hover {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5), 0 0 10px rgba(255, 215, 0, 0.4);
}

.card-content {
  position: relative;
  z-index: 2;
  padding: 24px;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
}

.card-content.vertical {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

/* Icon Styles */
.icon-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--wood-light); /* Nền icon sáng hơn gỗ */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: var(--gold); /* Màu icon vàng */
  margin-bottom: 15px;
  border: 2px solid var(--gold);
  transition: all 0.3s ease;
}

.cmd-card:hover .icon-circle {
  background: var(--red-seal);
  color: #fff;
  transform: scale(1.1);
}

/* Chữ trong thẻ đã Flip sang màu sáng */
h3 {
  margin: 0;
  font-family: "Noto Serif TC";
  font-size: 1.3rem;
  color: var(--gold); /* Màu Vàng Kim */
  font-weight: 900;
}
p {
  margin: 5px 0 0;
  color: var(--text-light); /* Màu Trắng Kem */
  font-size: 0.95rem;
  font-weight: 600;
}

/* --- PLAY CARD (THẺ NHẬP THẾ - Giữ nguyên) --- */
.play-card {
  grid-column: span 2;
  grid-row: span 2;
  background: var(--wood-dark);
  border-color: var(--gold);
}

.play-card h2 {
  font-family: "Noto Serif TC";
  font-size: 4rem;
  margin: 0;
  color: #fff;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.play-card p {
  color: var(--gold);
  font-size: 1.2rem;
  letter-spacing: 4px;
  font-weight: 700;
  border-top: 2px solid var(--red-seal);
  display: inline-block;
  padding-top: 10px;
}

.card-bg-ink {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, #4e342e 0%, #000000 100%);
  z-index: 1;
  opacity: 0.9;
}

.play-card::before {
  content: "";
  position: absolute;
  inset: 5px;
  border: 1px solid rgba(255, 236, 179, 0.3);
  z-index: 2;
  pointer-events: none;
}

.play-card .card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.play-content-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.play-card .icon-wrapper i {
  font-size: 5rem;
  color: var(--gold);
  filter: drop-shadow(0 0 10px rgba(212, 160, 23, 0.4));
}

.play-arrow {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #fff;
  transition: all 0.3s ease;
  background: var(--red-seal);
}

.play-card:hover .play-arrow {
  background: #fff;
  color: var(--red-seal);
  transform: scale(1.1);
  box-shadow: 0 0 20px #fff;
}

/* --- ADMIN CARD (QUAN PHỦ - Vẫn nền tối) --- */
.admin-card {
  background: #263238;
  border-color: #37474f;
}
/* Đảm bảo chữ bên trong vẫn sáng */
.admin-card h3 {
  color: #fff;
}
.admin-card p {
  color: #cfd8dc;
}
.admin-card .icon-circle {
  background: #eceff1;
  color: #263238;
  border-color: #263238;
}

.admin-card:hover {
  border-color: #ef5350;
}
.admin-card:hover .icon-circle {
  background: #ef5350;
  color: #fff;
}

/* --- NEWS TICKER (CÁO THỊ - Đã Flip) --- */
.news-ticker-box {
  background: var(--wood-dark);
  border: 4px double var(--gold);
  border-radius: 4px;
  height: 50px;
  display: flex;
  overflow: hidden;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.ticker-label {
  background: var(--gold);
  color: var(--wood-dark);
  padding: 0 20px;
  display: flex;
  align-items: center;
  font-weight: 900;
  font-family: "Noto Serif TC";
  font-size: 1rem;
  gap: 10px;
  z-index: 5;
  box-shadow: 5px 0 15px rgba(0, 0, 0, 0.3);
}

.ticker-track {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  background: #2c1810; /* FLIP: Nền gỗ tối/Mực */
}

.ticker-content {
  display: flex;
  white-space: nowrap;
  animation: ticker 30s linear infinite;
}

.ticker-item {
  color: var(--text-light); /* FLIP: Chữ sáng */
  font-family: "Noto Serif TC";
  font-weight: bold;
  font-size: 1.1rem;
}

.ticker-item.highlight {
  color: var(--red-seal);
}

.ticker-separator {
  margin: 0 30px;
  color: var(--gold);
  font-size: 0.8em;
  text-shadow: 1px 1px 0 #000;
}

@keyframes ticker {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}

/* --- RESPONSIVE --- */
@media (max-width: 900px) {
  .command-grid {
    grid-template-columns: 1fr;
    grid-auto-rows: auto;
  }
  .play-card {
    grid-column: span 1;
    grid-row: span 1;
    height: 300px;
  }
  .play-card .card-content {
    flex-direction: column;
    justify-content: center;
    gap: 30px;
    text-align: center;
  }
  .command-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  .header-right {
    width: 100%;
  }
  .weather-widget {
    width: 100%;
    justify-content: space-between;
    box-sizing: border-box;
  }
}
</style> -->

<!-- trương khang thêm -->
<!-- <template>
  <div class="page-container wuxia-dashboard">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
      </div>

    <div class="dashboard-wrapper">
      <div class="command-header">
        <div class="header-left">
          <div class="sys-badge">
            <span class="status-dot online">●</span> MÁY CHỦ: ỔN ĐỊNH
          </div>
          <h1 class="welcome-text">
            CHÀO MỪNG,
            <span
              class="user-rank"
              :data-text="authStore.user?.username || 'ĐẠI HIỆP'"
            >
              {{ authStore.user?.username || "ĐẠI HIỆP" }}
            </span>
          </h1>
          <div class="player-stats">
            <span class="stat-pill"> <i class="fas fa-signal"></i> 12ms </span>
            <span class="stat-pill">
              <i class="fas fa-server"></i> MÁY CHỦ 1
            </span>
          </div>
        </div>
        <div class="header-right">
          <div class="weather-widget">
            <div class="weather-icon"><i class="fas fa-moon"></i></div>
            <div class="weather-info">
              <span class="location">HẮC PHONG SƠN</span>
              <span class="temp">Canh Ba / Trăng Khuyết</span>
            </div>
          </div>
        </div>
      </div>

      <div class="command-grid">
        <router-link to="/explore" class="cmd-card play-card">
          <div class="card-bg-ink"></div>
          <div class="card-content">
            <div class="play-content-left">
              <div class="icon-wrapper">
                <i class="fas fa-dragon"></i>
              </div>
              <div class="text-group">
                <h2>NHẬP THẾ</h2>
                <p>BƯỚC VÀO GIANG HỒ</p>
              </div>
            </div>
            <div class="play-arrow">
              <i class="fas fa-chevron-right"></i>
            </div>
          </div>
        </router-link>

        <router-link to="/inventory" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-box-open"></i></div>
            <h3>HÀNH TRANG</h3>
            <p>Vật Phẩm & Bảo Bối</p>
          </div>
        </router-link>

        <router-link to="/market" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-store"></i></div>
            <h3>THƯƠNG HỘI</h3>
            <p>Giao Thương</p>
          </div>
        </router-link>

        <router-link to="/leaderboard" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-trophy"></i></div>
            <h3>BẢNG VÀNG</h3>
            <p>Danh Chấn Giang Hồ</p>
          </div>
        </router-link>

        <router-link
          v-if="authStore.user?.role === 'ADMIN'"
          to="/admin"
          class="cmd-card admin-card"
        >
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-gavel"></i></div>
            <h3>QUAN PHỦ</h3>
            <p>Điều Hành Hệ Thống</p>
          </div>
        </router-link>
      </div>

      <div class="news-ticker-box">
        <div class="ticker-label"><i class="fas fa-scroll"></i> CÁO THỊ</div>
        <div class="ticker-track">
          <div class="ticker-content">
            <span class="ticker-item">⚠️ BẢO TRÌ: Giờ Tý canh ba</span>
            <span class="ticker-separator">❖</span>
            <span class="ticker-item highlight"
              >💎 SỰ KIỆN: Nhân đôi kinh nghiệm</span
            >
            <span class="ticker-separator">❖</span>
            <span class="ticker-item"
              >⚔️ BOSS: Hắc Long đã xuất hiện tại Bắc Sơn</span
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();
onMounted(() => {
  if (authStore.token) authStore.fetchProfile();
});
</script>

<style scoped>
/* Giữ nguyên CSS cũ trong file Home.vue của bạn */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --paper-bg: #e3d5b8;
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --ink: #212121;
  --red-seal: #b71c1c;
  --gold: #ffecb3; 
  --text-light: #f3f4f6;
  --text-dim: #a1887f; 
}

.wuxia-dashboard {
  min-height: 100vh;
  background-color: var(--wood-dark);
  color: var(--text-light);
  font-family: "Noto Serif TC", serif;
  position: relative;
  overflow: hidden;
}

.dashboard-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  height: 100vh;
}

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
  background-position: center bottom;
  filter: sepia(30%) brightness(0.7) contrast(1.1);
  opacity: 0.9;
}

.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(62, 39, 35, 0.9) 0%,
    transparent 60%
  );
}

.command-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 20px;
  border-bottom: 3px double rgba(255, 255, 255, 0.3);
}

.sys-badge {
  font-family: "Noto Serif TC", serif;
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--gold);
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-shadow: 1px 1px 2px #000;
}
.status-dot {
  color: #4caf50;
  font-size: 1.2em;
  text-shadow: 0 0 5px #4caf50;
}

.welcome-text {
  font-size: 2.5rem;
  font-weight: 900;
  margin: 0;
  line-height: 1.1;
  color: #ffffff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.user-rank {
  color: var(--gold);
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  font-family: "Noto Serif TC", serif;
}

.player-stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.stat-pill {
  font-size: 0.85rem;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 5px 15px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.weather-widget {
  background: rgba(0, 0, 0, 0.6);
  padding: 10px 20px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}
.weather-icon i {
  font-size: 1.8rem;
  color: var(--gold);
}
.weather-info {
  display: flex;
  flex-direction: column;
  text-align: right;
  font-family: "Noto Serif TC";
}
.location {
  font-size: 0.8rem;
  color: var(--gold);
  font-weight: bold;
  letter-spacing: 1px;
}
.temp {
  font-size: 1rem;
  color: #fff;
  font-weight: bold;
}

.command-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-auto-rows: 180px;
  gap: 20px;
  flex-grow: 1;
}

.cmd-card {
  position: relative;
  text-decoration: none;
  background: var(--wood-dark); 
  border: 4px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.cmd-card:hover {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5), 0 0 10px rgba(255, 215, 0, 0.4);
}

.card-content {
  position: relative;
  z-index: 2;
  padding: 24px;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
}

.card-content.vertical {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.icon-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--wood-light); 
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: var(--gold); 
  margin-bottom: 15px;
  border: 2px solid var(--gold);
  transition: all 0.3s ease;
}

.cmd-card:hover .icon-circle {
  background: var(--red-seal);
  color: #fff;
  transform: scale(1.1);
}

h3 {
  margin: 0;
  font-family: "Noto Serif TC";
  font-size: 1.3rem;
  color: var(--gold);
  font-weight: 900;
}
p {
  margin: 5px 0 0;
  color: var(--text-light);
  font-size: 0.95rem;
  font-weight: 600;
}

.play-card {
  grid-column: span 2;
  grid-row: span 2;
  background: var(--wood-dark);
  border-color: var(--gold);
}

.play-card h2 {
  font-family: "Noto Serif TC";
  font-size: 4rem;
  margin: 0;
  color: #fff;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.play-card p {
  color: var(--gold);
  font-size: 1.2rem;
  letter-spacing: 4px;
  font-weight: 700;
  border-top: 2px solid var(--red-seal);
  display: inline-block;
  padding-top: 10px;
}

.card-bg-ink {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, #4e342e 0%, #000000 100%);
  z-index: 1;
  opacity: 0.9;
}

.play-card::before {
  content: "";
  position: absolute;
  inset: 5px;
  border: 1px solid rgba(255, 236, 179, 0.3);
  z-index: 2;
  pointer-events: none;
}

.play-card .card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.play-content-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.play-card .icon-wrapper i {
  font-size: 5rem;
  color: var(--gold);
  filter: drop-shadow(0 0 10px rgba(212, 160, 23, 0.4));
}

.play-arrow {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #fff;
  transition: all 0.3s ease;
  background: var(--red-seal);
}

.play-card:hover .play-arrow {
  background: #fff;
  color: var(--red-seal);
  transform: scale(1.1);
  box-shadow: 0 0 20px #fff;
}

.admin-card {
  background: #263238;
  border-color: #37474f;
}
.admin-card h3 {
  color: #fff;
}
.admin-card p {
  color: #cfd8dc;
}
.admin-card .icon-circle {
  background: #eceff1;
  color: #263238;
  border-color: #263238;
}

.admin-card:hover {
  border-color: #ef5350;
}
.admin-card:hover .icon-circle {
  background: #ef5350;
  color: #fff;
}

.news-ticker-box {
  background: var(--wood-dark);
  border: 4px double var(--gold);
  border-radius: 4px;
  height: 50px;
  display: flex;
  overflow: hidden;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.ticker-label {
  background: var(--gold);
  color: var(--wood-dark);
  padding: 0 20px;
  display: flex;
  align-items: center;
  font-weight: 900;
  font-family: "Noto Serif TC";
  font-size: 1rem;
  gap: 10px;
  z-index: 5;
  box-shadow: 5px 0 15px rgba(0, 0, 0, 0.3);
}

.ticker-track {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  background: #2c1810; 
}

.ticker-content {
  display: flex;
  white-space: nowrap;
  animation: ticker 30s linear infinite;
}

.ticker-item {
  color: var(--text-light); 
  font-family: "Noto Serif TC";
  font-weight: bold;
  font-size: 1.1rem;
}

.ticker-item.highlight {
  color: var(--red-seal);
}

.ticker-separator {
  margin: 0 30px;
  color: var(--gold);
  font-size: 0.8em;
  text-shadow: 1px 1px 0 #000;
}

@keyframes ticker {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}

@media (max-width: 900px) {
  .command-grid {
    grid-template-columns: 1fr;
    grid-auto-rows: auto;
  }
  .play-card {
    grid-column: span 1;
    grid-row: span 1;
    height: 300px;
  }
  .play-card .card-content {
    flex-direction: column;
    justify-content: center;
    gap: 30px;
    text-align: center;
  }
  .command-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  .header-right {
    width: 100%;
  }
  .weather-widget {
    width: 100%;
    justify-content: space-between;
    box-sizing: border-box;
  }
}
</style> -->

<!-- 4h16 -->
<!-- <template>
  <div class="page-container wuxia-dashboard">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="dashboard-wrapper">
      <div class="command-header">
        <div class="header-left">
          <div class="sys-badge">
            <span class="status-dot online">●</span> MÁY CHỦ: ỔN ĐỊNH
          </div>
          <h1 class="welcome-text">
            CHÀO MỪNG,
            <span class="user-rank" :data-text="authStore.user?.username || 'ĐẠI HIỆP'">
              {{ authStore.user?.username || "ĐẠI HIỆP" }}
            </span>
          </h1>
          <div class="player-stats">
            <span class="stat-pill"> <i class="fas fa-signal"></i> 12ms </span>
            <span class="stat-pill">
              <i class="fas fa-server"></i> MÁY CHỦ 1
            </span>
          </div>
        </div>
        <div class="header-right">
          <div class="weather-widget">
            <div class="weather-icon"><i class="fas fa-moon"></i></div>
            <div class="weather-info">
              <span class="location">HẮC PHONG SƠN</span>
              <span class="temp">Canh Ba / Trăng Khuyết</span>
            </div>
          </div>
        </div>
      </div>

      <div class="command-grid">
        <router-link to="/explore" class="cmd-card play-card">
          <div class="card-bg-ink"></div>
          <div class="card-content">
            <div class="play-content-left">
              <div class="icon-wrapper">
                <i class="fas fa-dragon"></i>
              </div>
              <div class="text-group">
                <h2>NHẬP THẾ</h2>
                <p>BƯỚC VÀO GIANG HỒ</p>
              </div>
            </div>
            <div class="play-arrow">
              <i class="fas fa-chevron-right"></i>
            </div>
          </div>
        </router-link>

        <router-link to="/inventory" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-box-open"></i></div>
            <h3>HÀNH TRANG</h3>
            <p>Vật Phẩm & Bảo Bối</p>
          </div>
        </router-link>

        <router-link to="/market" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-store"></i></div>
            <h3>THƯƠNG HỘI</h3>
            <p>Giao Thương</p>
          </div>
        </router-link>

        <router-link to="/leaderboard" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-trophy"></i></div>
            <h3>BẢNG VÀNG</h3>
            <p>Danh Chấn Giang Hồ</p>
          </div>
        </router-link>

        <router-link v-if="authStore.user?.role === 'ADMIN'" to="/admin" class="cmd-card admin-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-gavel"></i></div>
            <h3>QUAN PHỦ</h3>
            <p>Điều Hành Hệ Thống</p>
          </div>
        </router-link>
      </div>

      <div class="news-ticker-box">
        <div class="ticker-label"><i class="fas fa-scroll"></i> CÁO THỊ</div>
        <div class="ticker-track">
          <div class="ticker-content">
            <span class="ticker-item">⚠️ BẢO TRÌ: Giờ Tý canh ba</span>
            <span class="ticker-separator">❖</span>
            <span class="ticker-item highlight">💎 SỰ KIỆN: Nhân đôi kinh nghiệm</span>
            <span class="ticker-separator">❖</span>
            <span class="ticker-item">⚔️ BOSS: Hắc Long đã xuất hiện tại Bắc Sơn</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();
onMounted(() => {
  if (authStore.token) authStore.fetchProfile();
});
</script>

<style scoped>
/* CSS GIỮ NGUYÊN (Đã có trong file cũ) */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --paper-bg: #e3d5b8;
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --ink: #212121;
  --red-seal: #b71c1c;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #a1887f;
}

.wuxia-dashboard {
  min-height: 100vh;
  background-color: var(--wood-dark);
  color: var(--text-light);
  font-family: "Noto Serif TC", serif;
  position: relative;
  overflow: hidden;
}

.dashboard-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

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
  background-position: center bottom;
  filter: sepia(30%) brightness(0.7) contrast(1.1);
  opacity: 0.9;
}

.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(62, 39, 35, 0.9) 0%, transparent 60%);
}

.command-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 20px;
  border-bottom: 3px double rgba(255, 255, 255, 0.3);
}

.sys-badge {
  font-family: "Noto Serif TC", serif;
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--gold);
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-shadow: 1px 1px 2px #000;
}

.status-dot {
  color: #4caf50;
  font-size: 1.2em;
  text-shadow: 0 0 5px #4caf50;
}

.welcome-text {
  font-size: 2.5rem;
  font-weight: 900;
  margin: 0;
  line-height: 1.1;
  color: #ffffff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.user-rank {
  color: var(--gold);
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  font-family: "Noto Serif TC", serif;
}

.player-stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.stat-pill {
  font-size: 0.85rem;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 5px 15px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.weather-widget {
  background: rgba(0, 0, 0, 0.6);
  padding: 10px 20px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}

.weather-icon i {
  font-size: 1.8rem;
  color: var(--gold);
}

.weather-info {
  display: flex;
  flex-direction: column;
  text-align: right;
  font-family: "Noto Serif TC";
}

.location {
  font-size: 0.8rem;
  color: var(--gold);
  font-weight: bold;
  letter-spacing: 1px;
}

.temp {
  font-size: 1rem;
  color: #fff;
  font-weight: bold;
}

.command-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-auto-rows: 180px;
  gap: 20px;
  flex-grow: 1;
}

.cmd-card {
  position: relative;
  text-decoration: none;
  background: var(--wood-dark);
  border: 4px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.cmd-card:hover {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5), 0 0 10px rgba(255, 215, 0, 0.4);
}

.card-content {
  position: relative;
  z-index: 2;
  padding: 24px;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
}

.card-content.vertical {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.icon-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--wood-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: var(--gold);
  margin-bottom: 15px;
  border: 2px solid var(--gold);
  transition: all 0.3s ease;
}

.cmd-card:hover .icon-circle {
  background: var(--red-seal);
  color: #fff;
  transform: scale(1.1);
}

h3 {
  margin: 0;
  font-family: "Noto Serif TC";
  font-size: 1.3rem;
  color: var(--gold);
  font-weight: 900;
}

p {
  margin: 5px 0 0;
  color: var(--text-light);
  font-size: 0.95rem;
  font-weight: 600;
}

.play-card {
  grid-column: span 2;
  grid-row: span 2;
  background: var(--wood-dark);
  border-color: var(--gold);
}

.play-card h2 {
  font-family: "Noto Serif TC";
  font-size: 4rem;
  margin: 0;
  color: #fff;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.play-card p {
  color: var(--gold);
  font-size: 1.2rem;
  letter-spacing: 4px;
  font-weight: 700;
  border-top: 2px solid var(--red-seal);
  display: inline-block;
  padding-top: 10px;
}

.card-bg-ink {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, #4e342e 0%, #000000 100%);
  z-index: 1;
  opacity: 0.9;
}

.play-card::before {
  content: "";
  position: absolute;
  inset: 5px;
  border: 1px solid rgba(255, 236, 179, 0.3);
  z-index: 2;
  pointer-events: none;
}

.play-card .card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.play-content-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.play-card .icon-wrapper i {
  font-size: 5rem;
  color: var(--gold);
  filter: drop-shadow(0 0 10px rgba(212, 160, 23, 0.4));
}

.play-arrow {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #fff;
  transition: all 0.3s ease;
  background: var(--red-seal);
}

.play-card:hover .play-arrow {
  background: #fff;
  color: var(--red-seal);
  transform: scale(1.1);
  box-shadow: 0 0 20px #fff;
}

.admin-card {
  background: #263238;
  border-color: #37474f;
}

.admin-card h3 {
  color: #fff;
}

.admin-card p {
  color: #cfd8dc;
}

.admin-card .icon-circle {
  background: #eceff1;
  color: #263238;
  border-color: #263238;
}

.admin-card:hover {
  border-color: #ef5350;
}

.admin-card:hover .icon-circle {
  background: #ef5350;
  color: #fff;
}

.news-ticker-box {
  background: var(--wood-dark);
  border: 4px double var(--gold);
  border-radius: 4px;
  height: 50px;
  display: flex;
  overflow: hidden;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.ticker-label {
  background: var(--gold);
  color: var(--wood-dark);
  padding: 0 20px;
  display: flex;
  align-items: center;
  font-weight: 900;
  font-family: "Noto Serif TC";
  font-size: 1rem;
  gap: 10px;
  z-index: 5;
  box-shadow: 5px 0 15px rgba(0, 0, 0, 0.3);
}

.ticker-track {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  background: #2c1810;
}

.ticker-content {
  display: flex;
  white-space: nowrap;
  animation: ticker 30s linear infinite;
}

.ticker-item {
  color: var(--text-light);
  font-family: "Noto Serif TC";
  font-weight: bold;
  font-size: 1.1rem;
}

.ticker-item.highlight {
  color: var(--red-seal);
}

.ticker-separator {
  margin: 0 30px;
  color: var(--gold);
  font-size: 0.8em;
  text-shadow: 1px 1px 0 #000;
}

@keyframes ticker {
  0% {
    transform: translateX(100%);
  }

  100% {
    transform: translateX(-100%);
  }
}

@media (max-width: 900px) {
  .command-grid {
    grid-template-columns: 1fr;
    grid-auto-rows: auto;
  }

  .play-card {
    grid-column: span 1;
    grid-row: span 1;
    height: 300px;
  }

  .play-card .card-content {
    flex-direction: column;
    justify-content: center;
    gap: 30px;
    text-align: center;
  }

  .command-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-right {
    width: 100%;
  }

  .weather-widget {
    width: 100%;
    justify-content: space-between;
    box-sizing: border-box;
  }
}
</style> -->

<!-- 5h09 -->
<!-- <template>
  <div class="page-container wuxia-dashboard">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="dashboard-wrapper">
      <div class="command-header">
        <div class="header-left">
          <div class="sys-badge">
            <span class="status-dot online">●</span> MÁY CHỦ: ỔN ĐỊNH
          </div>
          <h1 class="welcome-text">
            CHÀO MỪNG,
            <span class="user-rank" :data-text="authStore.user?.username || 'ĐẠI HIỆP'">
              {{ authStore.user?.username || "ĐẠI HIỆP" }}
            </span>
          </h1>
          <div class="player-stats">
            <span class="stat-pill"> <i class="fas fa-signal"></i> 12ms </span>
            <span class="stat-pill">
              <i class="fas fa-server"></i> MÁY CHỦ 1
            </span>
          </div>
        </div>
        <div class="header-right">
          <div class="weather-widget">
            <div class="weather-icon"><i class="fas fa-moon"></i></div>
            <div class="weather-info">
              <span class="location">HẮC PHONG SƠN</span>
              <span class="temp">Canh Ba / Trăng Khuyết</span>
            </div>
          </div>
        </div>
      </div>

      <div class="command-grid">
        <router-link to="/explore" class="cmd-card play-card">
          <div class="card-bg-ink"></div>
          <div class="card-content">
            <div class="play-content-left">
              <div class="icon-wrapper">
                <i class="fas fa-dragon"></i>
              </div>
              <div class="text-group">
                <h2>NHẬP THẾ</h2>
                <p>BƯỚC VÀO GIANG HỒ</p>
              </div>
            </div>
            <div class="play-arrow">
              <i class="fas fa-chevron-right"></i>
            </div>
          </div>
        </router-link>

        <router-link to="/inventory" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-box-open"></i></div>
            <h3>HÀNH TRANG</h3>
            <p>Vật Phẩm & Bảo Bối</p>
          </div>
        </router-link>

        <router-link to="/market" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-store"></i></div>
            <h3>THƯƠNG HỘI</h3>
            <p>Giao Thương</p>
          </div>
        </router-link>

        <router-link to="/leaderboard" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-trophy"></i></div>
            <h3>BẢNG VÀNG</h3>
            <p>Danh Chấn Giang Hồ</p>
          </div>
        </router-link>

        <router-link v-if="authStore.user?.role === 'ADMIN'" to="/admin" class="cmd-card admin-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-gavel"></i></div>
            <h3>QUAN PHỦ</h3>
            <p>Điều Hành Hệ Thống</p>
          </div>
        </router-link>
      </div>

      <div class="news-ticker-box">
        <div class="ticker-label"><i class="fas fa-scroll"></i> CÁO THỊ</div>
        <div class="ticker-track">
          <div class="ticker-content">
            <span class="ticker-item">⚠️ BẢO TRÌ: Giờ Tý canh ba</span>
            <span class="ticker-separator">❖</span>
            <span class="ticker-item highlight">💎 SỰ KIỆN: Nhân đôi kinh nghiệm</span>
            <span class="ticker-separator">❖</span>
            <span class="ticker-item">⚔️ BOSS: Hắc Long đã xuất hiện tại Bắc Sơn</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();
onMounted(() => {
  if (authStore.token) authStore.fetchProfile();
});
</script>

<style scoped>
/* CSS GIỮ NGUYÊN (Đã có trong file cũ) */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --paper-bg: #e3d5b8;
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --ink: #212121;
  --red-seal: #b71c1c;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #a1887f;
}

.wuxia-dashboard {
  /* Đã xóa min-height: 100vh; để cho phép cuộn */
  background-color: var(--wood-dark);
  color: var(--text-light);
  font-family: "Noto Serif TC", serif;
  position: relative;
  overflow: hidden;
}

.dashboard-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  /* Đã xóa height: 100vh; để cho phép cuộn */
}

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
  background-position: center bottom;
  filter: sepia(30%) brightness(0.7) contrast(1.1);
  opacity: 0.9;
}

.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(62, 39, 35, 0.9) 0%, transparent 60%);
}

.command-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 20px;
  border-bottom: 3px double rgba(255, 255, 255, 0.3);
}

.sys-badge {
  font-family: "Noto Serif TC", serif;
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--gold);
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-shadow: 1px 1px 2px #000;
}

.status-dot {
  color: #4caf50;
  font-size: 1.2em;
  text-shadow: 0 0 5px #4caf50;
}

.welcome-text {
  font-size: 2.5rem;
  font-weight: 900;
  margin: 0;
  line-height: 1.1;
  color: #ffffff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.user-rank {
  color: var(--gold);
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  font-family: "Noto Serif TC", serif;
}

.player-stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.stat-pill {
  font-size: 0.85rem;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 5px 15px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.weather-widget {
  background: rgba(0, 0, 0, 0.6);
  padding: 10px 20px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}

.weather-icon i {
  font-size: 1.8rem;
  color: var(--gold);
}

.weather-info {
  display: flex;
  flex-direction: column;
  text-align: right;
  font-family: "Noto Serif TC";
}

.location {
  font-size: 0.8rem;
  color: var(--gold);
  font-weight: bold;
  letter-spacing: 1px;
}

.temp {
  font-size: 1rem;
  color: #fff;
  font-weight: bold;
}

.command-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-auto-rows: 180px;
  gap: 20px;
  flex-grow: 1;
}

.cmd-card {
  position: relative;
  text-decoration: none;
  background: var(--wood-dark);
  border: 4px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.cmd-card:hover {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5), 0 0 10px rgba(255, 215, 0, 0.4);
}

.card-content {
  position: relative;
  z-index: 2;
  padding: 24px;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
}

.card-content.vertical {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.icon-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--wood-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: var(--gold);
  margin-bottom: 15px;
  border: 2px solid var(--gold);
  transition: all 0.3s ease;
}

.cmd-card:hover .icon-circle {
  background: var(--red-seal);
  color: #fff;
  transform: scale(1.1);
}

h3 {
  margin: 0;
  font-family: "Noto Serif TC";
  font-size: 1.3rem;
  color: var(--gold);
  font-weight: 900;
}

p {
  margin: 5px 0 0;
  color: var(--text-light);
  font-size: 0.95rem;
  font-weight: 600;
}

.play-card {
  grid-column: span 2;
  grid-row: span 2;
  background: var(--wood-dark);
  border-color: var(--gold);
}

.play-card h2 {
  font-family: "Noto Serif TC";
  font-size: 4rem;
  margin: 0;
  color: #fff;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.play-card p {
  color: var(--gold);
  font-size: 1.2rem;
  letter-spacing: 4px;
  font-weight: 700;
  border-top: 2px solid var(--red-seal);
  display: inline-block;
  padding-top: 10px;
}

.card-bg-ink {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, #4e342e 0%, #000000 100%);
  z-index: 1;
  opacity: 0.9;
}

.play-card::before {
  content: "";
  position: absolute;
  inset: 5px;
  border: 1px solid rgba(255, 236, 179, 0.3);
  z-index: 2;
  pointer-events: none;
}

.play-card .card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.play-content-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.play-card .icon-wrapper i {
  font-size: 5rem;
  color: var(--gold);
  filter: drop-shadow(0 0 10px rgba(212, 160, 23, 0.4));
}

.play-arrow {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #fff;
  transition: all 0.3s ease;
  background: var(--red-seal);
}

.play-card:hover .play-arrow {
  background: #fff;
  color: var(--red-seal);
  transform: scale(1.1);
  box-shadow: 0 0 20px #fff;
}

.admin-card {
  background: #263238;
  border-color: #37474f;
}

.admin-card h3 {
  color: #fff;
}

.admin-card p {
  color: #cfd8dc;
}

.admin-card .icon-circle {
  background: #eceff1;
  color: #263238;
  border-color: #263238;
}

.admin-card:hover {
  border-color: #ef5350;
}

.admin-card:hover .icon-circle {
  background: #ef5350;
  color: #fff;
}

.news-ticker-box {
  background: var(--wood-dark);
  border: 4px double var(--gold);
  border-radius: 4px;
  height: 50px;
  display: flex;
  overflow: hidden;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.ticker-label {
  background: var(--gold);
  color: var(--wood-dark);
  padding: 0 20px;
  display: flex;
  align-items: center;
  font-weight: 900;
  font-family: "Noto Serif TC";
  font-size: 1rem;
  gap: 10px;
  z-index: 5;
  box-shadow: 5px 0 15px rgba(0, 0, 0, 0.3);
}

.ticker-track {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  background: #2c1810;
}

.ticker-content {
  display: flex;
  white-space: nowrap;
  animation: ticker 30s linear infinite;
}

.ticker-item {
  color: var(--text-light);
  font-family: "Noto Serif TC";
  font-weight: bold;
  font-size: 1.1rem;
}

.ticker-item.highlight {
  color: var(--red-seal);
}

.ticker-separator {
  margin: 0 30px;
  color: var(--gold);
  font-size: 0.8em;
  text-shadow: 1px 1px 0 #000;
}

@keyframes ticker {
  0% {
    transform: translateX(100%);
  }

  100% {
    transform: translateX(-100%);
  }
}

@media (max-width: 900px) {
  .command-grid {
    grid-template-columns: 1fr;
    grid-auto-rows: auto;
  }

  .play-card {
    grid-column: span 1;
    grid-row: span 1;
    height: 300px;
  }

  .play-card .card-content {
    flex-direction: column;
    justify-content: center;
    gap: 30px;
    text-align: center;
  }

  .command-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-right {
    width: 100%;
  }

  .weather-widget {
    width: 100%;
    justify-content: space-between;
    box-sizing: border-box;
  }
}
</style> -->

<!-- 5:52 -->
<!-- <template>
  <div class="page-container wuxia-dashboard">
    <div class="ink-bg-layer">
      <img :src="bgImage" class="bg-image-full" />
      <div class="fog-anim"></div>
    </div>

    <div class="dashboard-wrapper">
      <div class="command-header">
        <div class="header-left">
          <div class="sys-badge">
            <span class="status-dot online">●</span> MÁY CHỦ: ỔN ĐỊNH
          </div>
          <h1 class="welcome-text">
            CHÀO MỪNG,
            <span
              class="user-rank"
              :data-text="authStore.user?.username || 'ĐẠI HIỆP'"
            >
              {{ authStore.user?.username || "ĐẠI HIỆP" }}
            </span>
          </h1>
          <div class="player-stats">
            <span class="stat-pill"> <i class="fas fa-signal"></i> 12ms </span>
            <span class="stat-pill">
              <i class="fas fa-server"></i> MÁY CHỦ 1
            </span>
          </div>
        </div>
        <div class="header-right">
          <div class="weather-widget">
            <div class="weather-icon"><i class="fas fa-moon"></i></div>
            <div class="weather-info">
              <span class="location">HẮC PHONG SƠN</span>
              <span class="temp">Canh Ba / Trăng Khuyết</span>
            </div>
          </div>
        </div>
      </div>

      <div class="command-grid">
        <router-link to="/explore" class="cmd-card play-card">
          <div class="card-bg-ink"></div>
          <div class="card-content">
            <div class="play-content-left">
              <div class="icon-wrapper">
                <i class="fas fa-dragon"></i>
              </div>
              <div class="text-group">
                <h2>NHẬP THẾ</h2>
                <p>BƯỚC VÀO GIANG HỒ</p>
              </div>
            </div>
            <div class="play-arrow">
              <i class="fas fa-chevron-right"></i>
            </div>
          </div>
        </router-link>

        <router-link to="/inventory" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-box-open"></i></div>
            <h3>HÀNH TRANG</h3>
            <p>Vật Phẩm & Bảo Bối</p>
          </div>
        </router-link>

        <router-link to="/market" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-store"></i></div>
            <h3>THƯƠNG HỘI</h3>
            <p>Giao Thương</p>
          </div>
        </router-link>

        <router-link to="/leaderboard" class="cmd-card wood-card">
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-trophy"></i></div>
            <h3>BẢNG VÀNG</h3>
            <p>Danh Chấn Giang Hồ</p>
          </div>
        </router-link>

        <router-link
          v-if="authStore.user?.role === 'ADMIN'"
          to="/admin"
          class="cmd-card admin-card"
        >
          <div class="card-content vertical">
            <div class="icon-circle"><i class="fas fa-gavel"></i></div>
            <h3>QUAN PHỦ</h3>
            <p>Điều Hành Hệ Thống</p>
          </div>
        </router-link>
      </div>

      <div class="news-ticker-box">
        <div class="ticker-label"><i class="fas fa-scroll"></i> CÁO THỊ</div>
        <div class="ticker-track">
          <div class="ticker-content">
            <span class="ticker-item">⚠️ BẢO TRÌ: Giờ Tý canh ba</span>
            <span class="ticker-separator">❖</span>
            <span class="ticker-item highlight"
              >💎 SỰ KIỆN: Nhân đôi kinh nghiệm</span
            >
            <span class="ticker-separator">❖</span>
            <span class="ticker-item"
              >⚔️ BOSS: Hắc Long đã xuất hiện tại Bắc Sơn</span
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useAuthStore } from "../stores/authStore";
import { resolveItemImage } from "../utils/assetHelper"; // [FIX] Import helper

const authStore = useAuthStore();

// [FIX] Load ảnh nền thông qua assetHelper để Vite xử lý đường dẫn đúng
const bgImage = resolveItemImage("b_doanhtrai.png");

onMounted(() => {
  if (authStore.token) authStore.fetchProfile();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --ink: #212121;
  --red-seal: #b71c1c;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #a1887f;
}

.wuxia-dashboard {
  /* Cho phép cuộn trên mobile */
  min-height: 100vh;
  background-color: var(--wood-dark);
  color: var(--text-light);
  font-family: "Noto Serif TC", serif;
  position: relative;
  overflow-x: hidden;
}

.dashboard-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* --- BACKGROUND --- */
.ink-bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-color: #3e2723;
  overflow: hidden;
}

.bg-image-full {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  inset: 0;
  filter: sepia(30%) brightness(0.6) contrast(1.1); /* Làm tối nền để nổi chữ */
}

.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(62, 39, 35, 0.95) 0%,
    rgba(62, 39, 35, 0.4) 50%,
    transparent 100%
  );
}

/* --- HEADER --- */
.command-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 20px;
  border-bottom: 3px double rgba(255, 255, 255, 0.3);
}

.sys-badge {
  font-family: "Noto Serif TC", serif;
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--gold);
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-shadow: 1px 1px 2px #000;
}

.status-dot {
  color: #4caf50;
  font-size: 1.2em;
  text-shadow: 0 0 5px #4caf50;
}

.welcome-text {
  font-size: 2.5rem;
  font-weight: 900;
  margin: 0;
  line-height: 1.1;
  color: #ffffff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.user-rank {
  color: var(--gold);
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  font-family: "Noto Serif TC", serif;
}

.player-stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.stat-pill {
  font-size: 0.85rem;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  padding: 5px 15px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.weather-widget {
  background: rgba(0, 0, 0, 0.6);
  padding: 10px 20px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}

.weather-icon i {
  font-size: 1.8rem;
  color: var(--gold);
}

.weather-info {
  display: flex;
  flex-direction: column;
  text-align: right;
  font-family: "Noto Serif TC";
}

.location {
  font-size: 0.8rem;
  color: var(--gold);
  font-weight: bold;
  letter-spacing: 1px;
}

.temp {
  font-size: 1rem;
  color: #fff;
  font-weight: bold;
}

/* --- GRID --- */
.command-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-auto-rows: 180px;
  gap: 20px;
  flex-grow: 1;
}

.cmd-card {
  position: relative;
  text-decoration: none;
  background: var(--wood-dark);
  border: 4px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.cmd-card:hover {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow:
    0 10px 25px rgba(0, 0, 0, 0.5),
    0 0 10px rgba(255, 215, 0, 0.4);
}

.card-content {
  position: relative;
  z-index: 2;
  padding: 24px;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
}

.card-content.vertical {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.icon-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--wood-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: var(--gold);
  margin-bottom: 15px;
  border: 2px solid var(--gold);
  transition: all 0.3s ease;
}

.cmd-card:hover .icon-circle {
  background: var(--red-seal);
  color: #fff;
  transform: scale(1.1);
}

h3 {
  margin: 0;
  font-family: "Noto Serif TC";
  font-size: 1.3rem;
  color: var(--gold);
  font-weight: 900;
}

p {
  margin: 5px 0 0;
  color: var(--text-light);
  font-size: 0.95rem;
  font-weight: 600;
}

/* PLAY CARD */
.play-card {
  grid-column: span 2;
  grid-row: span 2;
  background: var(--wood-dark);
  /* border-color: var(--gold); */
}

.play-card h2 {
  font-family: "Noto Serif TC";
  font-size: 4rem;
  margin: 0;
  color: #fff;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.play-card p {
  color: var(--gold);
  font-size: 1.2rem;
  letter-spacing: 4px;
  font-weight: 700;
  border-top: 2px solid var(--red-seal);
  display: inline-block;
  padding-top: 10px;
}

.card-bg-ink {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, #4e342e 0%, #000000 100%);
  z-index: 1;
  opacity: 0.9;
}

.play-card::before {
  content: "";
  position: absolute;
  inset: 5px;
  border: 1px solid rgba(255, 236, 179, 0.3);
  z-index: 2;
  pointer-events: none;
}

.play-card .card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.play-content-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.play-card .icon-wrapper i {
  font-size: 5rem;
  color: var(--gold);
  filter: drop-shadow(0 0 10px rgba(212, 160, 23, 0.4));
}

.play-arrow {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #fff;
  transition: all 0.3s ease;
  background: var(--red-seal);
}

.play-card:hover .play-arrow {
  background: #fff;
  color: var(--red-seal);
  transform: scale(1.1);
  box-shadow: 0 0 20px #fff;
}

/* ADMIN CARD */
.admin-card {
  background: #263238;
  border-color: #37474f;
}
.admin-card h3 {
  color: #fff;
}
.admin-card p {
  color: #cfd8dc;
}
.admin-card .icon-circle {
  background: #eceff1;
  color: #263238;
  border-color: #263238;
}
.admin-card:hover {
  border-color: #ef5350;
}
.admin-card:hover .icon-circle {
  background: #ef5350;
  color: #fff;
}

/* NEWS TICKER */
.news-ticker-box {
  background: var(--wood-dark);
  border: 4px double var(--gold);
  border-radius: 4px;
  height: 50px;
  display: flex;
  overflow: hidden;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.ticker-label {
  background: var(--gold);
  color: var(--wood-dark);
  padding: 0 20px;
  display: flex;
  align-items: center;
  font-weight: 900;
  font-family: "Noto Serif TC";
  font-size: 1rem;
  gap: 10px;
  z-index: 5;
  box-shadow: 5px 0 15px rgba(0, 0, 0, 0.3);
}

.ticker-track {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  background: #2c1810;
}

.ticker-content {
  display: flex;
  white-space: nowrap;
  animation: ticker 30s linear infinite;
}

.ticker-item {
  color: var(--text-light);
  font-family: "Noto Serif TC";
  font-weight: bold;
  font-size: 1.1rem;
}

.ticker-item.highlight {
  color: var(--red-seal);
}

.ticker-separator {
  margin: 0 30px;
  color: var(--gold);
  font-size: 0.8em;
  text-shadow: 1px 1px 0 #000;
}

@keyframes ticker {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}

@media (max-width: 900px) {
  .command-grid {
    grid-template-columns: 1fr;
    grid-auto-rows: auto;
  }
  .play-card {
    grid-column: span 1;
    grid-row: span 1;
    height: 300px;
  }
  .play-card .card-content {
    flex-direction: column;
    justify-content: center;
    gap: 30px;
    text-align: center;
  }
  .command-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  .header-right {
    width: 100%;
  }
  .weather-widget {
    width: 100%;
    justify-content: space-between;
    box-sizing: border-box;
  }
}
</style> -->
<template>
  <div class="page-container wuxia-dashboard">
    <div class="bg-layer">
      <div class="mountain-bg"></div>
      <div class="wood-overlay"></div>
      <div class="vignette"></div>
    </div>

    <div class="dashboard-wrapper">
      
      <div class="command-header wood-panel">
        <div class="header-left">
          <div class="server-tag">
            <span class="status-orb"></span> 
            <span class="server-txt">MÁY CHỦ: HẮC LONG</span>
          </div>
          
          <div class="char-block">
            <div class="char-text">
              <span class="greet-txt"><i class="fas fa-scroll"></i> {{ greetingTime }}</span>
              <h1 class="char-name">
                <span class="title-prefix">ĐẠI HIỆP</span> 
                <span class="real-name">{{ authStore.user?.fullName || authStore.user?.username || "Vô Danh" }}</span>
              </h1>
            </div>
          </div>
        </div>

        <div class="header-right">
          <div class="wealth-bar" v-if="authStore.user?.wallet">
            <div class="wealth-item">
              <i class="fas fa-coins gold-icon"></i>
              <span class="amt">{{ authStore.user.wallet.gold?.toLocaleString() }}</span>
            </div>
          </div>

          <div class="weather-seal">
            <div class="w-icon"><i class="fas fa-cloud-sun"></i></div>
            <div class="w-info">
              <span class="map">DOANH TRẠI</span>
              <span class="stt">Trời Quang Mây Tạnh</span>
            </div>
          </div>
        </div>
      </div>

      <div class="command-grid">
        
        <router-link to="/explore" class="wood-card hero-tile">
          <div class="card-bg-pattern"></div>
          <div class="card-content">
            <div class="hero-center">
              <div class="icon-stamp">
                <i class="fas fa-dragon"></i>
              </div>
              <h2 class="hero-title">NHẬP THẾ</h2>
              <div class="ornament-line">
                <span class="diamond">♦</span>
                <span class="line"></span>
                <span class="diamond">♦</span>
              </div>
              <p class="hero-sub">HÀNH TẨU GIANG HỒ</p>
            </div>
            <div class="action-btn">
              <span>KHỞI HÀNH NGAY</span>
              <i class="fas fa-caret-right"></i>
            </div>
          </div>
          <div class="sheen"></div>
        </router-link>

        <router-link to="/inventory" class="wood-card sub-tile">
          <div class="tile-icon"><i class="fas fa-suitcase"></i></div>
          <div class="tile-info">
            <h3>HÀNH TRANG</h3>
            <span>Vật Phẩm & Trang Bị</span>
          </div>
          <div class="corner-decor top-right"></div>
          <div class="corner-decor bottom-left"></div>
        </router-link>

        <router-link to="/marketplace" class="wood-card sub-tile">
          <div class="tile-icon"><i class="fas fa-balance-scale"></i></div>
          <div class="tile-info">
            <h3>THƯƠNG HỘI</h3>
            <span>Giao Thương Mua Bán</span>
          </div>
          <div class="corner-decor top-right"></div>
          <div class="corner-decor bottom-left"></div>
        </router-link>

        <router-link to="/leaderboard" class="wood-card sub-tile">
          <div class="tile-icon"><i class="fas fa-trophy"></i></div>
          <div class="tile-info">
            <h3>BẢNG VÀNG</h3>
            <span>Danh Chấn Thiên Hạ</span>
          </div>
          <div class="corner-decor top-right"></div>
          <div class="corner-decor bottom-left"></div>
        </router-link>

        <router-link v-if="authStore.user?.role === 'ADMIN'" to="/admin" class="wood-card sub-tile admin-tile">
          <div class="tile-icon"><i class="fas fa-gavel"></i></div>
          <div class="tile-info">
            <h3>QUAN PHỦ</h3>
            <span>Điều Hành Hệ Thống</span>
          </div>
          <div class="corner-decor top-right"></div>
          <div class="corner-decor bottom-left"></div>
        </router-link>

      </div>

      <div class="news-bar">
        <div class="news-label">
            <i class="fas fa-bell"></i> 
            <span>CÁO THỊ</span>
        </div>
        <div class="news-track">
          <div class="news-content">
            <span>⚠️ Bảo trì định kỳ vào giờ Tý</span>
            <span class="sep">❖</span>
            <span class="highlight">🔥 Sự kiện: Đua top lực chiến nhận Thần Binh</span>
            <span class="sep">❖</span>
            <span>⚔️ Bang hội [Hắc Long] đã chiếm được thành Tương Dương</span>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useAuthStore } from "../stores/authStore";

const authStore = useAuthStore();

const greetingTime = computed(() => {
  const h = new Date().getHours();
  if (h < 5) return "Đêm Khuya Tĩnh Mịch";
  if (h < 11) return "Bình Minh Rạng Rỡ";
  if (h < 17) return "Nhật Quang Chói Chang";
  return "Hoàng Hôn Buông Xuống";
});

onMounted(() => {
  if (authStore.token && !authStore.user?.wallet) {
    authStore.fetchProfile();
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif:ital,wght@0,400;0,700;1,400&family=Playfair+Display:wght@700;900&display=swap");

:root {
  --wood-base: #3e2723;        
  --wood-card: #5d4037;        
  --wood-hover: #6d4c41;       
   
  --gold: #ffecb3;             
  --gold-accent: #ffd700;      
   
  --text-main: #fff8e1;        
  --text-dim: #d7ccc8;        
}

.wuxia-dashboard {
  min-height: 100vh;
  padding: 20px;
  font-family: "Noto Serif", serif;
  color: var(--text-main);
  position: relative;
  overflow-x: hidden;
  box-sizing: border-box; /* Quan trọng để padding không làm lệch khung */
}

/* --- BACKGROUND --- */
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; }

.mountain-bg {
  position: absolute; inset: 0;
  background-image: url("@/assets/Background/b_doanhtrai.png");
  background-size: cover; background-position: center bottom;
  opacity: 0.6; 
  filter: sepia(10%) contrast(1.1);
}

.wood-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9));
  mix-blend-mode: multiply;
}

.vignette {
  position: absolute; inset: 0;
  background: radial-gradient(circle, transparent 60%, #1a100d 100%);
}

.dashboard-wrapper {
  position: relative; z-index: 10;
  max-width: 1200px; margin: 0 auto;
  display: flex; flex-direction: column; gap: 20px;
}

/* --- 1. HEADER (Cân chỉnh kỹ) --- */
.wood-panel {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(90deg, rgba(62, 39, 35, 0.95), rgba(93, 64, 55, 0.9));
  border: 2px solid #6d4c41;
  border-radius: 6px;
  padding: 15px 30px; /* Giảm padding một chút để gọn */
  box-shadow: 0 10px 25px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(255, 236, 179, 0.1);
}

.header-left { display: flex; flex-direction: column; gap: 6px; }

.server-tag {
  display: inline-flex; align-items: center; gap: 8px; /* Dùng Flex để chấm tròn và chữ thẳng hàng */
  font-size: 0.8rem; color: var(--text-dim); letter-spacing: 1px;
  background: rgba(0,0,0,0.3); padding: 4px 10px; border-radius: 4px; width: fit-content;
  border: 1px solid rgba(255,255,255,0.1);
  line-height: 1; /* Reset line-height để không bị cao thấp */
}
/* Chỉnh text server */
.server-txt {
  padding-top: 1px; /* Font Serif thường cần đẩy xuống 1px để giữa */
}

.status-orb { width: 8px; height: 8px; background: #66bb6a; border-radius: 50%; box-shadow: 0 0 8px #66bb6a; }

.char-block { display: flex; align-items: center; }
.greet-txt { font-size: 0.9rem; color: var(--gold); margin-bottom: 4px; display: flex; align-items: center; gap: 6px; }

.char-name {
  margin: 0; 
  font-family: "Playfair Display", serif; 
  font-weight: 700;
  font-size: 2.2rem; color: #fff;
  text-shadow: 0 2px 5px rgba(0,0,0,0.6);
  line-height: 1.1;
  display: flex; align-items: center; /* Quan trọng: Flex để Badge và Tên thẳng hàng ngang */
  gap: 12px;
}
.title-prefix {
  font-family: "Noto Serif", serif;
  font-size: 0.9rem; 
  background: var(--gold-accent); color: #261815;
  padding: 4px 8px; 
  border-radius: 4px; 
  font-weight: 800; 
  box-shadow: 0 2px 5px rgba(0,0,0,0.3);
  /* Reset line-height để badge nằm giữa */
  line-height: 1; 
  display: flex; align-items: center; 
  height: fit-content;
}
.real-name {
  padding-bottom: 2px; /* Fix quang học cho font Playfair Display */
}

.header-right { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; }

.wealth-bar {
  background: rgba(0,0,0,0.4); border: 1px solid #6d4c41;
  padding: 6px 15px; border-radius: 20px;
  box-shadow: inset 0 0 10px rgba(0,0,0,0.5);
  display: flex; align-items: center;
}
.wealth-item { 
  display: flex; align-items: center; gap: 8px; 
  font-weight: bold; color: var(--gold-accent); font-size: 1.1rem; 
  line-height: 1; /* Rất quan trọng cho số tiền */
}
.gold-icon { 
    color: #ffd700; filter: drop-shadow(0 0 5px rgba(255, 215, 0, 0.5)); 
    font-size: 1rem;
}
.amt {
    font-variant-numeric: tabular-nums; /* Giúp các con số thẳng hàng nhau */
    padding-top: 2px; /* Cân chỉnh quang học số với icon */
}

.weather-seal { display: flex; align-items: center; gap: 12px; }
.w-icon { 
    display: flex; align-items: center; justify-content: center;
    font-size: 2rem; color: var(--gold); filter: drop-shadow(0 0 5px rgba(0,0,0,0.5)); 
}
.w-info { text-align: right; display: flex; flex-direction: column; justify-content: center;}
.map { display: block; font-weight: bold; font-family: "Playfair Display", serif; letter-spacing: 0.5px; line-height: 1.2; }
.stt { font-size: 0.8rem; color: var(--text-dim); font-style: italic; margin-top: 2px; }


/* --- 2. GRID (Cân chỉnh Card) --- */
.command-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: 200px 200px;
  gap: 15px; 
}

.wood-card {
  position: relative;
  text-decoration: none;
  background: linear-gradient(135deg, var(--wood-card) 0%, var(--wood-base) 100%);
  border: 1px solid #6d4c41;
  border-radius: 6px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
}

.wood-card:hover {
  transform: translateY(-4px);
  background: linear-gradient(135deg, var(--wood-hover) 0%, var(--wood-card) 100%);
  border-color: var(--gold-accent);
  box-shadow: 0 15px 30px rgba(0,0,0,0.7), 0 0 15px rgba(255, 215, 0, 0.1);
  z-index: 5;
}

/* HERO TILE */
.hero-tile {
  grid-column: span 2; grid-row: span 2;
  background: radial-gradient(circle at center, #4e342e 0%, #261815 100%);
  border-color: var(--gold-accent);
  border-width: 2px;
}
.card-bg-pattern {
  position: absolute; inset: 0; opacity: 0.15;
  background-image: url("@/assets/resources/r_wood.png"); 
  background-size: cover; mix-blend-mode: overlay;
}

.card-content { position: relative; z-index: 2; text-align: center; display: flex; flex-direction: column; gap: 15px; align-items: center; justify-content: center; height: 100%;}

.icon-stamp {
  width: 90px; height: 90px; border-radius: 50%;
  border: 3px double var(--gold-accent);
  display: flex; align-items: center; justify-content: center;
  font-size: 3rem; color: var(--gold-accent);
  background: rgba(0,0,0,0.3);
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.15);
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
}
.hero-tile:hover .icon-stamp { transform: scale(1.1); transition: 0.4s; background: rgba(255, 215, 0, 0.1); }

/* Chỉnh chữ tiêu đề lớn */
.hero-title {
  font-family: "Playfair Display", serif; 
  font-weight: 900;
  font-size: 3rem; margin: 0; color: #fff;
  text-shadow: 0 4px 10px rgba(0,0,0,0.8); letter-spacing: 2px;
  line-height: 1; /* Reset line-height */
}
.ornament-line { 
  display: flex; align-items: center; gap: 10px; width: 100%; justify-content: center;
  color: var(--gold-accent); font-size: 0.8rem;
}
.ornament-line .line { height: 2px; width: 60px; background: linear-gradient(to right, transparent, var(--gold-accent), transparent); }

.hero-sub { margin: 0; font-weight: bold; color: var(--text-dim); letter-spacing: 3px; font-size: 0.95rem; }

.action-btn {
  background: var(--gold-accent); color: #261815;
  padding: 10px 25px; border-radius: 4px; font-weight: 900;
  text-transform: uppercase; font-size: 1rem;
  box-shadow: 0 5px 15px rgba(0,0,0,0.4);
  transition: 0.3s;
  /* Flexbox cho nút bấm */
  display: flex; align-items: center; gap: 8px;
  line-height: 1;
}
.action-btn span {
    padding-top: 2px; /* Cân chỉnh text nút bấm */
}
.hero-tile:hover .action-btn { background: #fff; color: #b71c1c; box-shadow: 0 0 20px #fff; }

/* SUB TILE */
.tile-icon { font-size: 2.5rem; color: var(--text-dim); margin-bottom: 15px; transition: 0.3s; display: flex; align-items: center; justify-content: center; }
.wood-card:hover .tile-icon { color: var(--gold-accent); transform: scale(1.15) rotate(-5deg); filter: drop-shadow(0 0 8px rgba(255,215,0,0.6)); }

.tile-info { text-align: center; z-index: 2; display: flex; flex-direction: column; align-items: center; }
.tile-info h3 { 
  margin: 0 0 5px 0; 
  font-family: "Playfair Display", serif; 
  font-weight: 700;
  font-size: 1.3rem; color: #fff; 
  line-height: 1.2;
}
.tile-info span { font-size: 0.85rem; color: var(--gold); display: block; }

/* Decor */
.corner-decor {
  position: absolute; width: 10px; height: 10px;
  border: 2px solid transparent; transition: 0.3s;
}
.corner-decor.top-right { top: 5px; right: 5px; border-top-color: rgba(255,255,255,0.2); border-right-color: rgba(255,255,255,0.2); }
.corner-decor.bottom-left { bottom: 5px; left: 5px; border-bottom-color: rgba(255,255,255,0.2); border-left-color: rgba(255,255,255,0.2); }

.wood-card:hover .corner-decor { border-color: var(--gold-accent); width: 100%; height: 100%; }

.admin-tile:hover { border-color: #ef5350; background: linear-gradient(135deg, #3e2723, #b71c1c); }
.admin-tile:hover .tile-icon { color: #fff; filter: drop-shadow(0 0 8px #ef5350); }

.sheen {
  position: absolute; top: 0; left: -150%; width: 100%; height: 100%;
  background: linear-gradient(to right, transparent, rgba(255,255,255,0.2), transparent);
  transform: skewX(-25deg); pointer-events: none; transition: 0.5s;
}
.wood-card:hover .sheen { left: 150%; transition: 0.7s ease-in-out; }


/* --- 3. NEWS BAR (Căn chỉnh giữa tuyệt đối) --- */
.news-bar {
  display: flex; height: 45px;
  background: linear-gradient(to bottom, #2d201c, #261815);
  border: 1px solid #5d4037; border-radius: 4px;
  overflow: hidden; margin-top: 10px;
  box-shadow: 0 5px 10px rgba(0,0,0,0.5);
  align-items: center; /* Quan trọng */
}
.news-label {
  height: 100%; /* Full chiều cao */
  background: #4e342e; color: var(--gold-accent);
  padding: 0 25px; font-weight: 900; 
  font-family: "Playfair Display", serif;
  display: flex; align-items: center; gap: 10px;
  border-right: 2px solid #3e2723;
  box-shadow: 5px 0 10px rgba(0,0,0,0.3); z-index: 2;
}
.news-label span { padding-top: 2px; } /* Căn text với icon chuông */

.news-track { flex: 1; display: flex; align-items: center; overflow: hidden; padding-left: 20px; height: 100%; }
.news-content { display: flex; align-items: center; white-space: nowrap; animation: scroll 30s linear infinite; font-size: 0.95rem; font-weight: 500; }
.highlight { color: #ffab00; text-shadow: 0 0 5px rgba(255, 171, 0, 0.4); }
.sep { margin: 0 30px; color: #5d4037; font-size: 0.8em; display: flex; align-items: center; }

@keyframes scroll { 0% { transform: translateX(100%); } 100% { transform: translateX(-100%); } }

/* RESPONSIVE */
@media (max-width: 900px) {
  .command-grid { grid-template-columns: 1fr 1fr; grid-template-rows: auto; }
  .hero-tile { grid-column: span 2; height: 260px; }
  .wood-panel { flex-direction: column; align-items: flex-start; gap: 15px; }
  .header-right { width: 100%; justify-content: space-between; flex-direction: row; }
}
@media (max-width: 600px) {
  .command-grid { grid-template-columns: 1fr; }
  .hero-tile { grid-column: span 1; }
  .header-right { flex-direction: column; align-items: flex-start; }
}
</style>