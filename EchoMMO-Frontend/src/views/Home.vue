<template>
  <div class="page-container wuxia-dashboard">
    <div class="bg-layer">
      <div
        class="mountain-bg"
        :style="{ backgroundImage: `url(${bgImage})` }"
      ></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
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
              <span class="greet-txt">
                <i class="fas fa-clock"></i> {{ wuxiaTime.zodiac }} ({{
                  wuxiaTime.realTime
                }})
              </span>
              <h1 class="char-name">
                <span class="title-prefix">ĐẠI HIỆP</span>
                <span class="real-name">{{
                  authStore.user?.fullName ||
                  authStore.user?.username ||
                  "Vô Danh"
                }}</span>
              </h1>
            </div>
          </div>
        </div>

        <div class="header-right">
          <div class="wealth-bar" v-if="authStore.user?.wallet">
            <div class="wealth-item">
              <i class="fas fa-coins gold-icon"></i>
              <span class="amt">{{
                authStore.user.wallet.gold?.toLocaleString()
              }}</span>
            </div>
          </div>

          <div class="weather-seal">
            <div class="w-icon">
              <i :class="weatherData.icon"></i>
            </div>
            <div class="w-info">
              <span class="map">SÀI THÀNH</span>
              <span class="stt">{{ weatherData.desc }}</span>
              <span class="temp-real">{{ weatherData.temp }}°C</span>
            </div>
          </div>
        </div>
      </div>

      <div class="command-grid">
        <router-link to="/explore" class="wood-card hero-tile">
          <div
            class="card-bg-pattern"
            :style="{ backgroundImage: `url(${woodPattern})` }"
          ></div>
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

        <router-link
          v-if="authStore.user?.role === 'ADMIN'"
          to="/admin"
          class="wood-card sub-tile admin-tile"
        >
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
            <span class="highlight"
              >🔥 Sự kiện: Đua top lực chiến nhận Thần Binh</span
            >
            <span class="sep">❖</span>
            <span>⚔️ Bang hội [Hắc Long] đã chiếm được thành Tương Dương</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, onUnmounted, ref } from "vue";
import { useAuthStore } from "../stores/authStore";
import { getAssetUrl } from "@/utils/assetHelper";

const authStore = useAuthStore();

// Lấy ảnh nền và pattern gỗ từ Git
const bgImage = getAssetUrl("b_doanhtrai.png");
const woodPattern = getAssetUrl("w_wood.png");

// --- LOGIC THỜI GIAN & THỜI TIẾT ---
const wuxiaTime = reactive({
  zodiac: "Giờ Tý",
  realTime: "00:00",
});

const weatherData = reactive({
  temp: 28,
  desc: "Đang quan sát thiên văn...",
  icon: "fas fa-spinner fa-spin",
});

const isNight = ref(false);
let timerInterval = null;

const getZodiacTime = (hour) => {
  if (hour >= 23 || hour < 1) return "Giờ Tý (Canh Ba)";
  if (hour >= 1 && hour < 3) return "Giờ Sửu (Canh Tư)";
  if (hour >= 3 && hour < 5) return "Giờ Dần (Canh Năm)";
  if (hour >= 5 && hour < 7) return "Giờ Mão (Bình Minh)";
  if (hour >= 7 && hour < 9) return "Giờ Thìn";
  if (hour >= 9 && hour < 11) return "Giờ Tỵ";
  if (hour >= 11 && hour < 13) return "Giờ Ngọ (Chính Ngọ)";
  if (hour >= 13 && hour < 15) return "Giờ Mùi";
  if (hour >= 15 && hour < 17) return "Giờ Thân";
  if (hour >= 17 && hour < 19) return "Giờ Dậu (Hoàng Hôn)";
  if (hour >= 19 && hour < 21) return "Giờ Tuất (Canh Một)";
  if (hour >= 21 && hour < 23) return "Giờ Hợi (Canh Hai)";
  return "Giờ Lạ";
};

const updateTime = () => {
  const now = new Date();
  const h = now.getHours();
  const m = now.getMinutes();
  wuxiaTime.realTime = `${h.toString().padStart(2, "0")}:${m.toString().padStart(2, "0")}`;
  wuxiaTime.zodiac = getZodiacTime(h);
  isNight.value = h >= 18 || h < 6;
};

const fetchWeather = async () => {
  try {
    const res = await fetch(
      "https://api.open-meteo.com/v1/forecast?latitude=10.8231&longitude=106.6297&current=weather_code,temperature_2m,is_day&timezone=Asia%2FBangkok",
    );
    const data = await res.json();
    const current = data.current;

    weatherData.temp = Math.round(current.temperature_2m);

    const code = current.weather_code;
    const isDay = current.is_day === 1;

    if (code === 0) {
      weatherData.desc = isDay ? "Trời Quang Mây Tạnh" : "Trăng Thanh Gió Mát";
      weatherData.icon = isDay ? "fas fa-sun" : "fas fa-moon";
    } else if (code >= 1 && code <= 3) {
      weatherData.desc = "Mây Mù Bao Phủ";
      weatherData.icon = "fas fa-cloud";
    } else if (code >= 51 && code <= 67) {
      weatherData.desc = "Mưa Phùn Lất Phất";
      weatherData.icon = "fas fa-cloud-rain";
    } else if (code >= 80 && code <= 99) {
      weatherData.desc = "Mưa Gió Bão Bùng";
      weatherData.icon = "fas fa-bolt";
    } else {
      weatherData.desc = "Thiên Khí Thất Thường";
      weatherData.icon = "fas fa-smog";
    }
  } catch (error) {
    console.error("Lỗi xem thiên văn:", error);
    weatherData.desc = "Thiên Cơ Bất Khả Lộ";
    weatherData.icon = "fas fa-eye-slash";
  }
};

onMounted(() => {
  if (authStore.token && !authStore.user?.wallet) {
    authStore.fetchProfile();
  }
  updateTime();
  fetchWeather();
  timerInterval = setInterval(updateTime, 60000);
  setInterval(fetchWeather, 30 * 60000);
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
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
  box-sizing: border-box;
}

/* --- BACKGROUND --- */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: #261815;
}

.mountain-bg {
  position: absolute;
  inset: 0;
  /* background-image được set inline từ script */
  background-size: cover;
  background-position: center bottom;
  opacity: 0.6;
  filter: sepia(10%) contrast(1.1);
}

.wood-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(62, 39, 35, 0.7),
    rgba(30, 20, 15, 0.9)
  );
  mix-blend-mode: multiply;
  transition: background 2s ease;
}

.wood-overlay.night-mode {
  background: linear-gradient(
    to bottom,
    rgba(10, 5, 20, 0.85),
    rgba(0, 0, 0, 0.95)
  );
}

.vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, transparent 60%, #1a100d 100%);
}

.dashboard-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* --- HEADER --- */
.wood-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(
    90deg,
    rgba(62, 39, 35, 0.95),
    rgba(93, 64, 55, 0.9)
  );
  border: 2px solid #6d4c41;
  border-radius: 6px;
  padding: 15px 30px;
  box-shadow:
    0 10px 25px rgba(0, 0, 0, 0.5),
    inset 0 0 0 1px rgba(255, 236, 179, 0.1);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.server-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  color: var(--text-dim);
  letter-spacing: 1px;
  background: rgba(0, 0, 0, 0.3);
  padding: 4px 10px;
  border-radius: 4px;
  width: fit-content;
  border: 1px solid rgba(255, 255, 255, 0.1);
  line-height: 1;
}
.server-txt {
  padding-top: 1px;
}
.status-orb {
  width: 8px;
  height: 8px;
  background: #66bb6a;
  border-radius: 50%;
  box-shadow: 0 0 8px #66bb6a;
}

.char-block {
  display: flex;
  align-items: center;
}
.greet-txt {
  font-size: 0.9rem;
  color: var(--gold);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.char-name {
  margin: 0;
  font-family: "Playfair Display", serif;
  font-weight: 700;
  font-size: 2.2rem;
  color: #fff;
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.6);
  line-height: 1.1;
  display: flex;
  align-items: center;
  gap: 12px;
}
.title-prefix {
  font-family: "Noto Serif", serif;
  font-size: 0.9rem;
  background: var(--gold-accent);
  color: #261815;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 800;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
  line-height: 1;
  display: flex;
  align-items: center;
  height: fit-content;
}
.real-name {
  padding-bottom: 2px;
}

.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.wealth-bar {
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid #6d4c41;
  padding: 6px 15px;
  border-radius: 20px;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
}
.wealth-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: var(--gold-accent);
  font-size: 1.1rem;
  line-height: 1;
}
.gold-icon {
  color: #ffd700;
  filter: drop-shadow(0 0 5px rgba(255, 215, 0, 0.5));
  font-size: 1rem;
}
.amt {
  font-variant-numeric: tabular-nums;
  padding-top: 2px;
}

.weather-seal {
  display: flex;
  align-items: center;
  gap: 12px;
}
.w-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.2rem;
  color: var(--gold);
  filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5));
  width: 40px; /* Cố định chiều rộng để không nhảy layout khi icon đổi */
  text-align: center;
}
.w-info {
  text-align: right;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.map {
  display: block;
  font-weight: bold;
  font-family: "Playfair Display", serif;
  letter-spacing: 0.5px;
  line-height: 1.2;
}
.stt {
  font-size: 0.8rem;
  color: var(--text-dim);
  font-style: italic;
  margin-top: 2px;
}
.temp-real {
  font-size: 0.9rem;
  color: var(--gold-accent);
  font-weight: bold;
}

/* --- GRID --- */
.command-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: 200px 200px;
  gap: 15px;
}

.wood-card {
  position: relative;
  text-decoration: none;
  background: linear-gradient(
    135deg,
    var(--wood-card) 0%,
    var(--wood-base) 100%
  );
  border: 1px solid #6d4c41;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.wood-card:hover {
  transform: translateY(-4px);
  background: linear-gradient(
    135deg,
    var(--wood-hover) 0%,
    var(--wood-card) 100%
  );
  border-color: var(--gold-accent);
  box-shadow:
    0 15px 30px rgba(0, 0, 0, 0.7),
    0 0 15px rgba(255, 215, 0, 0.1);
  z-index: 5;
}

.hero-tile {
  grid-column: span 2;
  grid-row: span 2;
  background: radial-gradient(circle at center, #4e342e 0%, #261815 100%);
  border-color: var(--gold-accent);
  border-width: 2px;
}
.card-bg-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.15;
  /* background-image được set inline từ script */
  background-size: cover;
  mix-blend-mode: overlay;
}
.card-content {
  position: relative;
  z-index: 2;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: center;
  justify-content: center;
  height: 100%;
}
.icon-stamp {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  border: 3px double var(--gold-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: var(--gold-accent);
  background: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.15);
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
}
.hero-tile:hover .icon-stamp {
  transform: scale(1.1);
  transition: 0.4s;
  background: rgba(255, 215, 0, 0.1);
}
.hero-title {
  font-family: "Playfair Display", serif;
  font-weight: 900;
  font-size: 3rem;
  margin: 0;
  color: #fff;
  text-shadow: 0 4px 10px rgba(0, 0, 0, 0.8);
  letter-spacing: 2px;
  line-height: 1;
}
.ornament-line {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  justify-content: center;
  color: var(--gold-accent);
  font-size: 0.8rem;
}
.ornament-line .line {
  height: 2px;
  width: 60px;
  background: linear-gradient(
    to right,
    transparent,
    var(--gold-accent),
    transparent
  );
}
.hero-sub {
  margin: 0;
  font-weight: bold;
  color: var(--text-dim);
  letter-spacing: 3px;
  font-size: 0.95rem;
}
.action-btn {
  background: var(--gold-accent);
  color: #261815;
  padding: 10px 25px;
  border-radius: 4px;
  font-weight: 900;
  text-transform: uppercase;
  font-size: 1rem;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.4);
  transition: 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1;
}
.action-btn span {
  padding-top: 2px;
}
.hero-tile:hover .action-btn {
  background: #fff;
  color: #b71c1c;
  box-shadow: 0 0 20px #fff;
}

.tile-icon {
  font-size: 2.5rem;
  color: var(--text-dim);
  margin-bottom: 15px;
  transition: 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wood-card:hover .tile-icon {
  color: var(--gold-accent);
  transform: scale(1.15) rotate(-5deg);
  filter: drop-shadow(0 0 8px rgba(255, 215, 0, 0.6));
}
.tile-info {
  text-align: center;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.tile-info h3 {
  margin: 0 0 5px 0;
  font-family: "Playfair Display", serif;
  font-weight: 700;
  font-size: 1.3rem;
  color: #fff;
  line-height: 1.2;
}
.tile-info span {
  font-size: 0.85rem;
  color: var(--gold);
  display: block;
}

.corner-decor {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid transparent;
  transition: 0.3s;
}
.corner-decor.top-right {
  top: 5px;
  right: 5px;
  border-top-color: rgba(255, 255, 255, 0.2);
  border-right-color: rgba(255, 255, 255, 0.2);
}
.corner-decor.bottom-left {
  bottom: 5px;
  left: 5px;
  border-bottom-color: rgba(255, 255, 255, 0.2);
  border-left-color: rgba(255, 255, 255, 0.2);
}
.wood-card:hover .corner-decor {
  border-color: var(--gold-accent);
  width: 100%;
  height: 100%;
}
.admin-tile:hover {
  border-color: #ef5350;
  background: linear-gradient(135deg, #3e2723, #b71c1c);
}
.admin-tile:hover .tile-icon {
  color: #fff;
  filter: drop-shadow(0 0 8px #ef5350);
}
.sheen {
  position: absolute;
  top: 0;
  left: -150%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to right,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transform: skewX(-25deg);
  pointer-events: none;
  transition: 0.5s;
}
.wood-card:hover .sheen {
  left: 150%;
  transition: 0.7s ease-in-out;
}

/* NEWS BAR */
.news-bar {
  display: flex;
  height: 45px;
  background: linear-gradient(to bottom, #2d201c, #261815);
  border: 1px solid #5d4037;
  border-radius: 4px;
  overflow: hidden;
  margin-top: 10px;
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.5);
  align-items: center;
}
.news-label {
  height: 100%;
  background: #4e342e;
  color: var(--gold-accent);
  padding: 0 25px;
  font-weight: 900;
  font-family: "Playfair Display", serif;
  display: flex;
  align-items: center;
  gap: 10px;
  border-right: 2px solid #3e2723;
  box-shadow: 5px 0 10px rgba(0, 0, 0, 0.3);
  z-index: 2;
}
.news-label span {
  padding-top: 2px;
}
.news-track {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding-left: 20px;
  height: 100%;
}
.news-content {
  display: flex;
  align-items: center;
  white-space: nowrap;
  animation: scroll 30s linear infinite;
  font-size: 0.95rem;
  font-weight: 500;
}
.highlight {
  color: #ffab00;
  text-shadow: 0 0 5px rgba(255, 171, 0, 0.4);
}
.sep {
  margin: 0 30px;
  color: #5d4037;
  font-size: 0.8em;
  display: flex;
  align-items: center;
}

@keyframes scroll {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}

@media (max-width: 900px) {
  .command-grid {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto;
  }
  .hero-tile {
    grid-column: span 2;
    height: 260px;
  }
  .wood-panel {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  .header-right {
    width: 100%;
    justify-content: space-between;
    flex-direction: row;
  }
}
@media (max-width: 600px) {
  .command-grid {
    grid-template-columns: 1fr;
  }
  .hero-tile {
    grid-column: span 1;
  }
  .header-right {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
