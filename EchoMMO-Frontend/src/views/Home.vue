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
<template>
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
</style>