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
            <span class="server-txt">KHU VỰC: AN TOÀN</span>
          </div>
          <div class="char-block">
            <div class="char-text">
              <span class="greet-txt"
                ><i class="fas fa-clock"></i> {{ wuxiaTime.zodiac }}</span
              >
              <h1 class="char-name">LẠC DƯƠNG THÀNH</h1>
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
            <div class="wealth-item" style="margin-left: 10px; color: #00e676;">
               <i class="fas fa-gem"></i>
               <span class="amt">{{ authStore.user.wallet.echoCoin?.toLocaleString() }}</span>
            </div>
          </div>
          <div class="weather-seal">
            <i :class="weatherData.icon"></i>
            <span class="temp-real">{{ weatherData.temp }}°C</span>
          </div>
        </div>
      </div>

      <div class="town-content">
        <div class="ornament-divider">
          <span class="diamond">♦</span>
          <span class="sub-text">THIÊN HẠ THÁI BÌNH</span>
          <span class="diamond">♦</span>
        </div>

        <div class="hub-grid">
          <div
            class="wood-card inn-card"
            :class="{ 'resting-mode': isResting }"
          >
            <div class="card-bg-pattern"></div>
            <div class="card-content">
              <template v-if="!isResting">
                <div class="tile-icon"><i class="fas fa-bed"></i></div>
                <h3 class="tile-title">KHÁCH ĐIẾM</h3>
                <p class="tile-desc">Hồi phục sinh lực</p>

                <div class="stats-mini">
                  <div class="stat-row">
                    <span class="lbl">HP</span>
                    <div class="bar-track">
                      <div
                        class="bar-fill hp"
                        :style="{ width: hpPercent + '%' }"
                      ></div>
                    </div>
                  </div>
                  <div class="stat-row">
                    <span class="lbl">MP</span>
                    <div class="bar-track">
                      <div
                        class="bar-fill mp"
                        :style="{ width: energyPercent + '%' }"
                      ></div>
                    </div>
                  </div>
                </div>

                <button
                  class="wuxia-btn outline"
                  @click="openSpaMenu"
                  :disabled="isFull"
                >
                  {{ isFull ? "SUNG MÃN" : "NGHỈ NGƠI" }}
                </button>
              </template>

              <template v-else>
                <div class="night-scene">
                  <div class="sleeping-anim">
                    <i class="fas fa-user-ninja"></i>
                    <div class="zzz-particles">
                      <span>z</span><span>z</span><span>Z</span>
                    </div>
                  </div>
                  <div class="resting-text">
                    Đang điều tức... <br /><span class="count-txt"
                      >{{ countdown }}s</span
                    >
                  </div>
                </div>
              </template>
            </div>
            <div class="corner-decor top-right"></div>
            <div class="corner-decor bottom-left"></div>
          </div>

          <router-link to="/forge" class="wood-card forge-card">
            <div class="card-bg-pattern"></div>
            <div class="card-content">
              <div class="tile-icon"><i class="fas fa-hammer"></i></div>
              <h3 class="tile-title">THẦN BINH</h3>
              <p class="tile-desc">Rèn đúc trang bị</p>
              <div class="wuxia-btn outline">VÀO LÒ RÈN</div>
            </div>
            <div class="corner-decor top-right"></div>
            <div class="corner-decor bottom-left"></div>
          </router-link>

          <div
            class="wood-card mythic-card"
            :class="{ locked: !canEnterMythic }"
            @click="handleEnterMythic"
          >
            <div class="card-bg-pattern"></div>
            <div class="card-content">
              <div class="tile-icon"><i class="fas fa-dragon"></i></div>
              <h3 class="tile-title">THẦN ĐIỆN</h3>
              <p class="tile-desc">Chuyển hóa thần khí</p>
              <div v-if="!canEnterMythic" class="lock-msg">
                <i class="fas fa-lock"></i>
                <span style="margin-left: 5px">Cần đồ +30</span>
              </div>
              <div v-else class="wuxia-btn outline">VÀO ĐIỆN</div>
            </div>
            <div class="corner-decor top-right"></div>
            <div class="corner-decor bottom-left"></div>
          </div>
        </div>

        <div class="deploy-container">
          <button
            @click="$router.push('/explore')"
            class="wood-card hero-tile"
            :disabled="isResting"
          >
            <div class="card-content row-layout">
              <div class="icon-stamp small">
                <i class="fas fa-torii-gate"></i>
              </div>
              <div class="text-group">
                <h2 class="hero-title small">XUẤT THÀNH</h2>
                <p class="hero-sub">Hành Hiệp Trượng Nghĩa</p>
              </div>
              <i class="fas fa-chevron-right arrow-indicator"></i>
            </div>
          </button>
        </div>
      </div>
    </div>

    <transition name="fade-modal">
      <div
        v-if="showSpaMenu"
        class="modal-overlay"
        @click.self="showSpaMenu = false"
      >
        <div class="wood-panel modal-box spa-menu">
          <h3 class="modal-header-txt">~ CHỌN PHÒNG NGHỈ ~</h3>

          <div class="spa-options-grid">
            <div
              class="wood-card spa-item standard"
              @click="confirmRest('BASIC')"
            >
              <div class="tile-icon small"><i class="fas fa-mug-hot"></i></div>
              <h4>Phòng Bình Dân</h4>
              <p class="package-desc">Hồi 50% HP & MP</p>
              
              <div class="meta-row">
                <span><i class="far fa-clock"></i> 120s</span>
                
                <span v-if="freeUsageLeft > 0" class="gold-txt" style="color: #00e676;">
                    Miễn Phí ({{ freeUsageLeft }}/2)
                </span>
                <span v-else class="gold-txt">
                    {{ standardCost }} Vàng
                </span>
              </div>
            </div>

            <div class="wood-card spa-item vip" @click="confirmRest('VIP')">
              <div class="vip-ribbon">THƯỢNG HẠNG</div>
              <div class="tile-icon small">
                <i class="fas fa-wine-bottle"></i>
              </div>
              <h4>Phòng Thượng Hạng</h4>
              <p class="package-desc">Hồi 100% Full</p>
              
              <div class="meta-row">
                <span><i class="far fa-clock"></i> 10s</span>
                <span class="gold-txt" style="color: #00e676;">{{ vipCost }} Coin</span>
              </div>
            </div>
          </div>
          <button class="wuxia-btn close-btn" @click="showSpaMenu = false">
            ĐÓNG
          </button>
        </div>
      </div>
    </transition>

    <transition name="fade-modal">
      <div
        v-if="showRestModal"
        class="modal-overlay"
        @click.self="closeRestModal"
      >
        <div class="wood-panel modal-box result-modal">
          <div class="success-icon"><i class="fas fa-check-circle"></i></div>
          <h3 class="modal-header-txt">HỒI PHỤC HOÀN TẤT</h3>
          <p class="modal-msg">
            "Khách quan thần thái hồng hào, nội công sung mãn. Chúc ngài thượng
            lộ bình an!"
          </p>

          <button class="wuxia-btn full-width" @click="closeRestModal">
            ĐA TẠ
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { useCharacterStore } from "../stores/characterStore";
import { useInventoryStore } from "../stores/inventoryStore";
import { useAuthStore } from "../stores/authStore";
import axiosClient from "../api/axiosClient";

// --- BACKGROUND & TIME LOGIC ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const wuxiaTime = reactive({ zodiac: "Giờ Tý", realTime: "00:00" });
const weatherData = reactive({ temp: 28, icon: "fas fa-sun" });
const isNight = ref(false);
let clockInterval = null;

const getZodiacTime = (hour) => {
  const zodiacs = ["Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"];
  const idx = Math.floor(((hour + 1) % 24) / 2);
  return `Giờ ${zodiacs[idx]}`;
};

const updateTime = () => {
  const now = new Date();
  const h = now.getHours();
  wuxiaTime.zodiac = getZodiacTime(h);
  isNight.value = h >= 18 || h < 6;
};

// --- GAME LOGIC ---
const router = useRouter();
const charStore = useCharacterStore();
const authStore = useAuthStore();
const inventoryStore = useInventoryStore();

const isResting = ref(false);
const showSpaMenu = ref(false);
const showRestModal = ref(false);
const countdown = ref(0);
let timerInterval = null;
const STORAGE_KEY_END_TIME = "spa_end_time";

const hpPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min((charStore.character.currentHp / charStore.character.maxHp) * 100, 100);
});

const energyPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min((charStore.character.currentEnergy / charStore.character.maxEnergy) * 100, 100);
});

const isFull = computed(() => hpPercent.value >= 100 && energyPercent.value >= 100);

// --- LOGIC TÍNH GIÁ SPA (Mới) ---
const playerLevel = computed(() => charStore.character?.level || 1);
const dailyUsage = computed(() => charStore.character?.dailySpaUsage || 0);

const freeUsageLeft = computed(() => Math.max(0, 2 - dailyUsage.value));

const standardCost = computed(() => {
    return (playerLevel.value * 100).toLocaleString();
});

const vipCost = computed(() => {
    return (0.5 + (playerLevel.value * 0.05)).toFixed(2);
});

const canEnterMythic = computed(() => {
  if (!inventoryStore.items || inventoryStore.items.length === 0) return false;
  return inventoryStore.items.some((item) => (item.enhancementLevel || 0) >= 30);
});

const handleEnterMythic = () => {
  if (canEnterMythic.value) router.push("/evolve-mythic");
};

const openSpaMenu = () => (showSpaMenu.value = true);

const closeRestModal = () => {
  showRestModal.value = false;
  localStorage.removeItem(STORAGE_KEY_END_TIME);
};

const confirmRest = async (type) => {
  showSpaMenu.value = false;
  try {
    const res = await axiosClient.post("/spa/relax", { type: type });
    // Update lại: VIP=10s, BASIC=120s
    const duration = res.data.secondsRemaining || (type === "VIP" ? 10 : 120);
    const finishTime = Date.now() + duration * 1000;

    localStorage.setItem(STORAGE_KEY_END_TIME, finishTime);
    countdown.value = duration;
    isResting.value = true;
    
    // Fetch lại để update tiền/coin ngay lập tức
    await authStore.fetchProfile();
    startTimer();
  } catch (e) {
    const errorMsg = e.response?.data?.message || "Lỗi hệ thống hoặc không đủ tiền!";
    alert(errorMsg);
  }
};

const startTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    const savedEndTime = localStorage.getItem(STORAGE_KEY_END_TIME);
    if (!savedEndTime) {
      clearInterval(timerInterval);
      return;
    }

    const remaining = Math.ceil((parseInt(savedEndTime) - Date.now()) / 1000);
    if (remaining > 0) {
      countdown.value = remaining;
    } else {
      clearInterval(timerInterval);
      countdown.value = 0;
      finishRest();
    }
  }, 1000);
};

const finishRest = async () => {
  isResting.value = false;
  localStorage.removeItem(STORAGE_KEY_END_TIME);
  showRestModal.value = true;
  try {
    await charStore.fetchCharacter(); // Update lại HP/MP đầy đủ
  } catch (e) {
    console.error("Lỗi cập nhật nhân vật sau nghỉ ngơi:", e);
  }
};

const checkBackgroundRest = () => {
  const savedEndTime = localStorage.getItem(STORAGE_KEY_END_TIME);
  if (savedEndTime) {
    const remaining = Math.ceil((parseInt(savedEndTime) - Date.now()) / 1000);
    if (remaining > 0) {
      isResting.value = true;
      countdown.value = remaining;
      startTimer();
    } else {
      finishRest();
    }
  }
};

onMounted(async () => {
  updateTime();
  clockInterval = setInterval(updateTime, 60000);

  await charStore.fetchCharacter();
  if (authStore.token) await authStore.fetchProfile();
  if (inventoryStore.fetchInventory) await inventoryStore.fetchInventory();
  checkBackgroundRest();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
  if (clockInterval) clearInterval(clockInterval);
});
</script>

<style scoped>
/* Giữ nguyên Style cũ, chỉ thêm 1 chút cho phần mô tả gói */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif:ital,wght@0,400;0,700;1,400&family=Playfair+Display:wght@700;900&display=swap");

.package-desc {
    font-size: 0.8rem;
    color: #bdbdbd;
    margin: 5px 0 10px 0;
    font-style: italic;
}

/* Các style gốc của bro vẫn ở đây */
:root {
  --wood-base: #3e2723;
  --wood-card: #5d4037;
  --wood-hover: #6d4c41;
  --gold: #ffecb3;
  --gold-accent: #ffd700;
  --text-main: #fff8e1;
  --text-dim: #d7ccc8;
  --red-seal: #b71c1c;
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

/* ... (Copy lại toàn bộ phần style từ file cũ của bro vào đây để đảm bảo giao diện không vỡ) ... */
/* Tôi đã giữ lại class structure y hệt, bro chỉ cần paste đè vào là chạy */

.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center bottom; opacity: 0.6; filter: sepia(10%) contrast(1.1); }
.wood-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); mix-blend-mode: multiply; transition: background 2s ease; }
.wood-overlay.night-mode { background: linear-gradient(to bottom, rgba(10, 5, 20, 0.85), rgba(0, 0, 0, 0.95)); }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle, transparent 60%, #1a100d 100%); }
.dashboard-wrapper { position: relative; z-index: 10; max-width: 1000px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }
.wood-panel { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(90deg, rgba(62, 39, 35, 0.95), rgba(93, 64, 55, 0.9)); border: 2px solid #6d4c41; border-radius: 6px; padding: 10px 20px; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); }
.header-left { display: flex; flex-direction: column; gap: 4px; }
.server-tag { font-size: 0.75rem; color: var(--text-dim); display: flex; align-items: center; gap: 6px; }
.status-orb { width: 6px; height: 6px; background: #66bb6a; border-radius: 50%; box-shadow: 0 0 5px #66bb6a; }
.greet-txt { font-size: 0.8rem; color: var(--gold); }
.char-name { font-family: "Playfair Display", serif; font-size: 1.8rem; margin: 0; color: #fff; text-shadow: 0 2px 4px rgba(0, 0, 0, 0.6); line-height: 1; }
.header-right { display: flex; align-items: center; gap: 15px; }
.wealth-item { display: flex; align-items: center; gap: 6px; font-weight: bold; color: var(--gold-accent); font-size: 1rem; }
.gold-icon { color: #ffd700; filter: drop-shadow(0 0 5px rgba(255, 215, 0, 0.5)); }
.weather-seal { display: flex; align-items: center; gap: 8px; color: var(--text-dim); }
.town-content { padding-top: 20px; }
.ornament-divider { text-align: center; color: var(--gold); opacity: 0.6; margin-bottom: 20px; font-size: 0.9rem; display: flex; align-items: center; justify-content: center; gap: 10px; }
.ornament-divider .diamond { font-size: 0.7rem; }
.hub-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-bottom: 25px; }
.wood-card { position: relative; background: linear-gradient(135deg, var(--wood-card) 0%, var(--wood-base) 100%); border: 1px solid #6d4c41; border-radius: 6px; overflow: hidden; transition: all 0.3s ease; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); cursor: pointer; text-decoration: none; display: block; }
.wood-card:hover:not(.locked):not(.resting-mode) { transform: translateY(-4px); border-color: var(--gold-accent); box-shadow: 0 15px 30px rgba(0, 0, 0, 0.6), 0 0 10px rgba(255, 215, 0, 0.1); }
.card-content { padding: 25px; display: flex; flex-direction: column; align-items: center; text-align: center; position: relative; z-index: 2; height: 100%; box-sizing: border-box; }
.tile-icon { font-size: 2.2rem; color: var(--text-dim); margin-bottom: 15px; transition: 0.3s; }
.wood-card:hover .tile-icon { color: var(--gold-accent); transform: scale(1.1); filter: drop-shadow(0 0 8px rgba(255, 215, 0, 0.4)); }
.tile-title { margin: 0; font-family: "Playfair Display", serif; font-size: 1.4rem; color: #fff; margin-bottom: 5px; }
.tile-desc { font-size: 0.85rem; color: var(--gold); font-style: italic; margin-bottom: 20px; opacity: 0.8; }
.corner-decor { position: absolute; width: 8px; height: 8px; border: 1px solid transparent; transition: 0.3s; }
.top-right { top: 4px; right: 4px; border-top-color: rgba(255, 255, 255, 0.1); border-right-color: rgba(255, 255, 255, 0.1); }
.bottom-left { bottom: 4px; left: 4px; border-bottom-color: rgba(255, 255, 255, 0.1); border-left-color: rgba(255, 255, 255, 0.1); }
.wood-card:hover .corner-decor { width: 100%; height: 100%; border-color: var(--gold-accent); opacity: 0.3; }
.wuxia-btn { font-family: "Noto Serif", serif; font-weight: bold; cursor: pointer; margin-top: auto; transition: all 0.3s ease; text-transform: uppercase; font-size: 0.85rem; letter-spacing: 1px; display: inline-block; line-height: 1.2; }
.wuxia-btn.outline { background: transparent; border: 1px solid var(--text-dim); color: var(--text-dim); padding: 10px 30px; border-radius: 30px; width: auto; }
.wood-card:hover .wuxia-btn.outline:not(:disabled), .wuxia-btn.outline:hover:not(:disabled) { border-color: var(--gold-accent); color: var(--gold-accent); background: rgba(255, 215, 0, 0.05); box-shadow: 0 0 15px rgba(255, 215, 0, 0.2); }
.wuxia-btn.outline:active { transform: scale(0.95); }
.wuxia-btn.outline:disabled { color: #4e342e; border-color: #3e2723; cursor: not-allowed; }
.wuxia-btn.close-btn { background: #3e2723; color: #d7ccc8; border: none; padding: 10px; width: 100%; margin-top: 15px; border-radius: 4px; }
.wuxia-btn.full-width { background-color: #ffd700 !important; color: #3e2723 !important; border: 2px solid #ffecb3; padding: 12px; width: 100%; border-radius: 8px; font-weight: 900; font-family: "Noto Serif", serif; text-transform: uppercase; letter-spacing: 1px; margin-top: 20px; cursor: pointer; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5); transition: all 0.2s ease; position: relative; z-index: 100; }
.wuxia-btn.full-width:hover { background-color: #ffca28 !important; transform: translateY(-2px); box-shadow: 0 6px 15px rgba(255, 215, 0, 0.3); }
.wuxia-btn.full-width:active { transform: translateY(1px); }
.stats-mini { width: 100%; margin-bottom: 25px; display: flex; flex-direction: column; gap: 8px; }
.stat-row { display: flex; align-items: center; gap: 8px; font-size: 0.75rem; color: var(--text-dim); font-weight: bold; }
.bar-track { flex: 1; height: 4px; background: rgba(0, 0, 0, 0.5); border-radius: 2px; overflow: hidden; }
.bar-fill { height: 100%; transition: width 0.5s; }
.bar-fill.hp { background: #ef5350; }
.bar-fill.mp { background: #42a5f5; }
.resting-mode { border-color: #66bb6a; background: #1b5e20; cursor: default; }
.night-scene { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #a5d6a7; }
.sleeping-anim { font-size: 2rem; margin-bottom: 10px; position: relative; }
.zzz-particles span { position: absolute; font-size: 0.8rem; animation: floatUp 2s infinite; opacity: 0; top: 0; right: -10px; }
.zzz-particles span:nth-child(2) { animation-delay: 0.6s; right: -15px; }
.zzz-particles span:nth-child(3) { animation-delay: 1.2s; right: -5px; }
@keyframes floatUp { 0% { transform: translateY(0); opacity: 0; } 50% { opacity: 1; } 100% { transform: translateY(-20px); opacity: 0; } }
.count-txt { font-weight: bold; font-size: 1.2rem; color: #fff; }
.mythic-card.locked { filter: grayscale(1); opacity: 0.7; cursor: not-allowed; }
.lock-msg { color: #ef9a9a; font-size: 0.85rem; margin-top: auto; display: flex; align-items: center; gap: 5px; }
.deploy-container { margin-top: 10px; }
.hero-tile { background: radial-gradient(circle at center, #4e342e 0%, #261815 100%); border: 2px solid var(--gold-accent); width: 100%; }
.hero-tile .card-content { flex-direction: row; justify-content: space-between; text-align: left; padding: 20px 30px; }
.hero-tile .row-layout { width: 100%; display: flex; align-items: center; gap: 20px; }
.icon-stamp.small { width: 50px; height: 50px; font-size: 1.5rem; border: 2px double #9e9e9e; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #8b0000; background: rgba(0, 0, 0, 0.2); }
.text-group { flex: 1; }
.hero-title.small { font-size: 1.5rem; margin: 0; color: #ffffff; line-height: 1; }
.hero-sub { color: #ffffff; opacity: 0.9; margin-top: 4px; }
.arrow-indicator { font-size: 1.2rem; color: var(--gold-accent); animation: slideRight 1s infinite alternate; }
@keyframes slideRight { from { transform: translateX(0); } to { transform: translateX(5px); } }
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); display: flex; justify-content: center; align-items: center; z-index: 2000; backdrop-filter: blur(4px); }
.modal-box { width: 90%; max-width: 500px; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 30px; background: #261815; border-color: var(--gold); z-index: 2001; }
.modal-header-txt { color: var(--gold); font-family: "Playfair Display", serif; margin: 0 0 20px 0; border-bottom: 1px solid rgba(255, 255, 255, 0.1); padding-bottom: 10px; width: 100%; text-align: center; }
.spa-options-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; width: 100%; margin-bottom: 20px; }
.spa-item { padding: 15px; border-radius: 4px; display: flex; flex-direction: column; align-items: center; gap: 10px; border: 1px solid #5d4037; position: relative; overflow: hidden; }
.spa-item:hover { border-color: var(--gold-accent); background: rgba(255, 215, 0, 0.05); }
.tile-icon.small { font-size: 1.5rem; margin: 0; }
.spa-item h4 { margin: 0; font-size: 1rem; color: #fff; }
.meta-row { display: flex; justify-content: space-between; width: 100%; font-size: 0.8rem; color: var(--text-dim); }
.gold-txt { color: #ffd700; }
.vip-ribbon { position: absolute; top: 0; right: 0; background: #b71c1c; color: #fff; font-size: 0.6rem; padding: 2px 8px; border-bottom-left-radius: 4px; font-weight: bold; }
.success-icon { font-size: 4rem; color: #66bb6a; margin-bottom: 15px; text-shadow: 0 0 20px rgba(102, 187, 106, 0.4); }
.modal-msg { text-align: center; font-style: italic; color: var(--text-dim); margin-bottom: 25px; line-height: 1.5; }
.fade-modal-enter-active, .fade-modal-leave-active { transition: opacity 0.3s ease; }
.fade-modal-enter-from, .fade-modal-leave-to { opacity: 0; }
@media (max-width: 600px) {
  .hero-tile .card-content { padding: 15px; }
  .icon-stamp.small { width: 40px; height: 40px; font-size: 1.2rem; }
  .hero-title.small { font-size: 1.2rem; }
  .header-right { flex-direction: column; align-items: flex-end; gap: 5px; }
}
</style>