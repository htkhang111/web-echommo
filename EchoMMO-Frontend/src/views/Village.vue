<!-- <template>
  <div class="page-container village-page wuxia-dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="village-wrapper">
      <div class="hub-header">
        <div class="title-plaque">
          <div class="plaque-ornament left"></div>
          <h1 class="hub-title">LẠC DƯƠNG THÀNH</h1>
          <div class="plaque-ornament right"></div>
        </div>
        <p class="hub-subtitle">
          <span class="decor-icon">❖</span> Thiên Hạ Thái Bình
          <span class="decor-icon">❖</span>
        </p>
      </div>

      <div class="hub-grid">
        <div class="location-card inn-card" :class="{ 'resting-mode': isResting }">
          <div class="card-content">
            <template v-if="!isResting">
              <div class="card-top">
                <div class="icon-circle"><i class="fas fa-bed"></i></div>
                <h3 class="card-name">KHÁCH ĐIẾM</h3>
              </div>
              <div class="card-mid">
                <p class="desc">Hồi phục tinh lực, dưỡng sức đường xa.</p>
                <div class="stats-container">
                  <div class="stat-row">
                    <span class="stat-label">SINH</span>
                    <div class="progress-track">
                      <div class="progress-bar hp-bar" :style="{ width: hpPercent + '%' }"></div>
                    </div>
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">NỘI</span>
                    <div class="progress-track">
                      <div class="progress-bar mp-bar" :style="{ width: energyPercent + '%' }"></div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="card-bot">
                <button class="btn-action wuxia-btn" @click="openSpaMenu" :disabled="isFull">
                  <span v-if="isFull">ĐÃ SUNG MÃN</span>
                  <span v-else>NGHỈ NGƠI</span>
                </button>
              </div>
            </template>

            <template v-else>
              <div class="night-scene">
                <div class="moon-glow"></div>
                <div class="sleeping-anim">
                  <i class="fas fa-user-ninja"></i>
                  <div class="zzz-particles">
                    <span>z</span><span>z</span><span>Z</span>
                  </div>
                </div>
                <div class="resting-text">
                  Đang điều tức... còn <strong>{{ countdown }}s</strong>
                </div>
              </div>
            </template>
          </div>
        </div>

        <div class="location-card mythic-card" @click="$router.push('/evolve-mythic')">
          <div class="card-content">
            <div class="card-top">
              <div class="icon-circle"><i class="fas fa-dragon"></i></div>
              <h3 class="card-name">THẦN ĐIỆN</h3>
            </div>
            <div class="card-mid">
              <p class="desc">Nơi chuyển hóa phàm khí thành thần khí.</p>
            </div>
            <div class="card-bot">
              <div class="btn-action wuxia-btn outline">VÀO ĐIỆN</div>
            </div>
          </div>
        </div>

        <router-link to="/forge" class="location-card forge-card">
          <div class="card-content">
            <div class="card-top">
              <div class="icon-circle"><i class="fas fa-hammer"></i></div>
              <h3 class="card-name">THẦN BINH</h3>
            </div>
            <div class="card-mid">
              <p class="desc">Rèn đúc binh khí.</p>
              <div class="forge-fire-anim"><i class="fas fa-fire"></i></div>
            </div>
            <div class="card-bot">
              <div class="btn-action wuxia-btn outline">VÀO LÒ RÈN</div>
            </div>
          </div>
        </router-link>
      </div>

      <div class="deploy-container">
        <button @click="$router.push('/adventure')" class="imperial-seal-btn" :disabled="isResting">
          <div class="seal-content">
            <div class="seal-icon"><i class="fas fa-torii-gate"></i></div>
            <div class="seal-text-group">
              <span class="seal-big">XUẤT THÀNH</span>
              <span class="seal-small">Hành Hiệp Trượng Nghĩa</span>
            </div>
          </div>
        </button>
      </div>
    </div>

    <transition name="fade-modal">
      <div v-if="showSpaMenu" class="modal-overlay" @click.self="showSpaMenu = false">
        <div class="dark-scroll-modal">
          <div class="modal-border-top"></div>
          <div class="modal-body spa-menu">
            <h3 class="modal-title">CHỌN PHÒNG NGHỈ</h3>
            <div class="spa-options">
              <div class="spa-option" @click="confirmRest('STANDARD')">
                <div class="opt-icon">🍵</div>
                <div class="opt-info">
                  <h4>Phòng Bình Dân</h4>
                  <p>Thời gian: <strong>60 giây</strong></p>
                  <p class="cost">Giá: 50 Vàng</p>
                </div>
              </div>
              <div class="spa-option vip" @click="confirmRest('VIP')">
                <div class="opt-icon">🍶</div>
                <div class="opt-info">
                  <h4>Phòng Thượng Hạng</h4>
                  <p>Thời gian: <strong>10 giây</strong></p>
                  <p class="cost">Giá: 200 Vàng</p>
                </div>
              </div>
            </div>
            <button class="btn-close" @click="showSpaMenu = false">Đóng</button>
          </div>
          <div class="modal-border-bot"></div>
        </div>
      </div>
    </transition>

    <transition name="fade-modal">
      <div v-if="showRestModal" class="modal-overlay" @click.self="closeRestModal">
        <div class="dark-scroll-modal">
          <div class="modal-border-top"></div>
          <div class="modal-body">
            <div class="modal-stamp"><i class="fas fa-check"></i></div>
            <h3 class="modal-title">HỒI PHỤC HOÀN TẤT</h3>
            <p class="modal-msg">
              "Khách quan thần thái hồng hào, nội công sung mãn. Chúc ngài
              thượng lộ bình an!"
            </p>
            <button class="btn-confirm" @click="closeRestModal">ĐA TẠ</button>
          </div>
          <div class="modal-border-bot"></div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useCharacterStore } from "../stores/characterStore";
import { useAuthStore } from "../stores/authStore";
import axiosClient from "../api/axiosClient";

const charStore = useCharacterStore();
const authStore = useAuthStore();

const isResting = ref(false);
const showSpaMenu = ref(false);
const showRestModal = ref(false);
const countdown = ref(0);
let timerInterval = null;

// Key lưu thời gian kết thúc vào LocalStorage
const STORAGE_KEY_END_TIME = "spa_end_time";

const hpPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min(
    (charStore.character.currentHp / charStore.character.maxHp) * 100,
    100
  );
});
const energyPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min(
    (charStore.character.currentEnergy / charStore.character.maxEnergy) * 100,
    100
  );
});
const isFull = computed(
  () => hpPercent.value >= 100 && energyPercent.value >= 100
);

const openSpaMenu = () => {
  showSpaMenu.value = true;
};
const closeRestModal = () => {
  showRestModal.value = false;
  localStorage.removeItem(STORAGE_KEY_END_TIME);
};

// --- LOGIC 1: BẮT ĐẦU NGHỈ NGƠI ---
const confirmRest = async (type) => {
  showSpaMenu.value = false;
  try {
    const res = await axiosClient.post("/spa/start", null, {
      params: { packageType: type },
    });
    const duration = res.data.secondsRemaining || (type === "VIP" ? 10 : 60);
    const finishTime = Date.now() + duration * 1000;
    localStorage.setItem(STORAGE_KEY_END_TIME, finishTime);

    countdown.value = duration;
    isResting.value = true;
    await authStore.fetchProfile();
    startTimer();
  } catch (e) {
    alert(e.response?.data || "Không thể nghỉ ngơi lúc này.");
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
    const now = Date.now();
    const remaining = Math.ceil((parseInt(savedEndTime) - now) / 1000);

    if (remaining > 0) {
      countdown.value = remaining;
    } else {
      clearInterval(timerInterval);
      countdown.value = 0;
      finishRest();
    }
  }, 1000);
};

// --- LOGIC 2: KẾT THÚC NGHỈ NGƠI ---
const finishRest = async () => {
  try {
    const res = await axiosClient.post("/spa/complete");
    if (charStore.character) {
      charStore.character.currentHp = res.data.currentHp;
      charStore.character.currentEnergy = res.data.currentEnergy;
    }
    isResting.value = false;
    showRestModal.value = true;
    localStorage.removeItem(STORAGE_KEY_END_TIME);
  } catch (e) {
    console.error(e);
    if (e.response && e.response.status !== 400) {
      isResting.value = false;
      localStorage.removeItem(STORAGE_KEY_END_TIME);
    }
  }
};

// --- LOGIC 3: KHÔI PHỤC KHI F5 ---
const checkBackgroundRest = () => {
  const savedEndTime = localStorage.getItem(STORAGE_KEY_END_TIME);
  if (savedEndTime) {
    const now = Date.now();
    const remaining = Math.ceil((parseInt(savedEndTime) - now) / 1000);
    if (remaining > 0) {
      isResting.value = true;
      countdown.value = remaining;
      startTimer();
    } else {
      isResting.value = true;
      finishRest();
    }
  }
};

onMounted(() => {
  charStore.fetchCharacter();
  if (authStore.token) authStore.fetchProfile();
  checkBackgroundRest();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --red-seal: #b71c1c;
}

.wuxia-dark-theme {
  background-color: var(--wood-dark);
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
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
  background-image: url("@/assets/Background/b_doanhtrai.png");
  background-size: cover;
  opacity: 0.8;
}

.village-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
}

.hub-header {
  text-align: center;
  margin-bottom: 50px;
}

.title-plaque {
  display: inline-block;
  padding: 10px 40px;
  border-bottom: 3px double var(--gold);
  position: relative;
  background: rgba(0, 0, 0, 0.5);
}

.hub-title {
  font-size: 3rem;
  margin: 0;
  color: var(--text-light);
  text-transform: uppercase;
  font-weight: 900;
}

.hub-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  width: 100%;
  margin-bottom: 40px;
  justify-content: center;
  /* Căn giữa các thẻ */
}

.location-card {
  background: var(--wood-dark);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  text-decoration: none;
  transition: 0.3s;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  /* Thêm cursor pointer cho tất cả location-card */
}

.location-card:hover:not(.resting-mode) {
  transform: translateY(-5px);
  border-color: var(--gold);
}

/* Style riêng cho Thần Điện */
.mythic-card {
  border-color: #9c27b0;
  /* Viền tím */
  box-shadow: 0 0 10px rgba(156, 39, 176, 0.2);
}

.mythic-card:hover {
  border-color: #e040fb;
  box-shadow: 0 0 20px rgba(224, 64, 251, 0.5);
}

.mythic-card .icon-circle {
  border-color: #e040fb;
  color: #e040fb;
}

.mythic-card .card-name {
  color: #e040fb;
  text-shadow: 0 0 5px #7b1fa2;
}

.card-content {
  padding: 25px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: radial-gradient(circle at center, #4e342e 0%, #3e2723 100%);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 15px;
}

.icon-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--wood-light);
  border: 2px solid var(--gold);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: var(--gold);
}

.card-name {
  margin: 0;
  font-size: 1.5rem;
  color: var(--gold);
  font-weight: 800;
}

.card-mid {
  flex: 1;
  margin-bottom: 20px;
}

.desc {
  color: #bdbdbd;
  font-size: 0.9rem;
  font-style: italic;
  margin-bottom: 15px;
}

.stats-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-track {
  flex: 1;
  height: 8px;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  transition: width 0.5s ease;
}

.hp-bar {
  background: linear-gradient(90deg, #b71c1c, #e53935);
}

.mp-bar {
  background: linear-gradient(90deg, #1565c0, #42a5f5);
}

.wuxia-btn {
  width: 100%;
  padding: 12px;
  background: var(--red-seal);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  font-family: "Noto Serif TC", serif;
}

.wuxia-btn:hover:not(:disabled) {
  background: #d32f2f;
}

.wuxia-btn:disabled {
  background: #444;
  cursor: not-allowed;
  opacity: 0.7;
}

.wuxia-btn.outline {
  background: transparent;
  border: 2px solid var(--wood-light);
  color: #d7ccc8;
}

.wuxia-btn.outline:hover {
  border-color: var(--gold);
  color: var(--gold);
}

.night-scene {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.sleeping-anim {
  font-size: 3rem;
  color: var(--text-light);
  margin-bottom: 15px;
}

.deploy-container {
  width: 100%;
  max-width: 400px;
  margin-top: 30px;
}

.imperial-seal-btn {
  width: 100%;
  background: linear-gradient(to bottom, #b71c1c, #880e4f);
  border: 4px double var(--gold);
  padding: 15px;
  cursor: pointer;
}

.seal-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.seal-big {
  color: #fff;
  font-size: 1.8rem;
  font-weight: 900;
}

.seal-small {
  display: block;
  color: #ffcdd2;
  font-size: 0.8rem;
  margin-top: 4px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.dark-scroll-modal {
  width: 90%;
  max-width: 450px;
  background: var(--wood-dark);
  border-left: 12px solid #2d1e1b;
  border-right: 12px solid #2d1e1b;
}

.modal-body {
  padding: 30px 20px;
  text-align: center;
  color: var(--text-light);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.spa-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.spa-option {
  background: #4e342e;
  border: 2px solid #5d4037;
  padding: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 15px;
  border-radius: 8px;
  transition: 0.2s;
}

.spa-option:hover {
  border-color: var(--gold);
  background: #3e2723;
}

.spa-option.vip {
  border-color: #ffd700;
  background: #261815;
}

.opt-icon {
  font-size: 2rem;
}

.opt-info h4 {
  margin: 0;
  color: var(--gold);
}

.opt-info p {
  margin: 2px 0 0;
  font-size: 0.9rem;
  color: #ccc;
}

.btn-confirm,
.btn-close {
  background: var(--wood-light);
  color: var(--gold);
  border: 2px solid var(--gold);
  padding: 10px 40px;
  font-weight: bold;
  cursor: pointer;
}

.btn-close {
  width: 100%;
  margin-top: 10px;
  background: #b71c1c;
  border-color: #d32f2f;
  color: #fff;
}
</style> -->


<template>
  <div class="page-container village-page wuxia-dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="village-wrapper">
      <div class="hub-header">
        <div class="title-plaque">
          <div class="plaque-ornament left"></div>
          <h1 class="hub-title">LẠC DƯƠNG THÀNH</h1>
          <div class="plaque-ornament right"></div>
        </div>
        <p class="hub-subtitle">
          <span class="decor-icon">❖</span> Thiên Hạ Thái Bình
          <span class="decor-icon">❖</span>
        </p>
      </div>

      <div class="hub-grid">
        <div class="location-card inn-card" :class="{ 'resting-mode': isResting }">
          <div class="card-content">
            <template v-if="!isResting">
              <div class="card-top">
                <div class="icon-circle"><i class="fas fa-bed"></i></div>
                <h3 class="card-name">KHÁCH ĐIẾM</h3>
              </div>
              <div class="card-mid">
                <p class="desc">Hồi phục tinh lực, dưỡng sức đường xa.</p>
                <div class="stats-container">
                  <div class="stat-row">
                    <span class="stat-label">SINH</span>
                    <div class="progress-track">
                      <div class="progress-bar hp-bar" :style="{ width: hpPercent + '%' }"></div>
                    </div>
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">NỘI</span>
                    <div class="progress-track">
                      <div class="progress-bar mp-bar" :style="{ width: energyPercent + '%' }"></div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="card-bot">
                <button class="btn-action wuxia-btn" @click="openSpaMenu" :disabled="isFull">
                  <span v-if="isFull">ĐÃ SUNG MÃN</span>
                  <span v-else>NGHỈ NGƠI</span>
                </button>
              </div>
            </template>

            <template v-else>
              <div class="night-scene">
                <div class="moon-glow"></div>
                <div class="sleeping-anim">
                  <i class="fas fa-user-ninja"></i>
                  <div class="zzz-particles">
                    <span>z</span><span>z</span><span>Z</span>
                  </div>
                </div>
                <div class="resting-text">
                  Đang điều tức... còn <strong>{{ countdown }}s</strong>
                </div>
              </div>
            </template>
          </div>
        </div>

        <router-link to="/forge" class="location-card forge-card">
          <div class="card-content">
            <div class="card-top">
              <div class="icon-circle"><i class="fas fa-hammer"></i></div>
              <h3 class="card-name">THẦN BINH</h3>
            </div>
            <div class="card-mid">
              <p class="desc">Rèn đúc binh khí.</p>
            </div>
            <div class="card-bot">
              <div class="btn-action wuxia-btn outline">VÀO LÒ RÈN</div>
            </div>
          </div>
        </router-link>

        <div class="location-card mythic-card" :class="{ 'locked': !canEnterMythic }" @click="handleEnterMythic">
          <div class="card-content">
            <div class="card-top">
              <div class="icon-circle"><i class="fas fa-dragon"></i></div>
              <h3 class="card-name">THẦN ĐIỆN</h3>
            </div>
            <div class="card-mid">
              <p class="desc">Nơi chuyển hóa phàm khí thành thần khí.</p>

              <div v-if="!canEnterMythic" class="lock-overlay">
                <i class="fas fa-lock"></i>
                <span>Cần trang bị +30</span>
              </div>
            </div>
            <div class="card-bot">
              <div class="btn-action wuxia-btn outline">
                {{ canEnterMythic ? 'VÀO ĐIỆN' : 'CHƯA ĐỦ ĐIỀU KIỆN' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="deploy-container">
        <button @click="$router.push('/adventure')" class="imperial-seal-btn" :disabled="isResting">
          <div class="seal-content">
            <div class="seal-icon"><i class="fas fa-torii-gate"></i></div>
            <div class="seal-text-group">
              <span class="seal-big">XUẤT THÀNH</span>
              <span class="seal-small">Hành Hiệp Trượng Nghĩa</span>
            </div>
          </div>
        </button>
      </div>
    </div>

    <transition name="fade-modal">
      <div v-if="showSpaMenu" class="modal-overlay" @click.self="showSpaMenu = false">
        <div class="dark-scroll-modal spa-modal-custom">
          <div class="modal-border-top"></div>
          <div class="modal-body spa-menu">
            <h3 class="modal-title">
              <span class="decor-line">~</span> CHỌN PHÒNG NGHỈ <span class="decor-line">~</span>
            </h3>
            
            <div class="spa-options-grid">
              <div class="spa-card standard" @click="confirmRest('STANDARD')">
                <div class="card-inner">
                  <div class="icon-box">
                    <i class="fas fa-mug-hot"></i>
                  </div>
                  <div class="info-box">
                    <h4>Phòng Bình Dân</h4>
                    <p class="sub-desc">Trà nước đơn sơ</p>
                    <div class="detail-divider"></div>
                    <div class="detail-row">
                      <span class="label"><i class="far fa-clock"></i> Thời gian:</span>
                      <span class="value slow">60s</span>
                    </div>
                    <div class="detail-row">
                      <span class="label"><i class="fas fa-coins"></i> Chi phí:</span>
                      <span class="value gold">50 Vàng</span>
                    </div>
                  </div>
                </div>
                </div>

              <div class="spa-card vip" @click="confirmRest('VIP')">
                <div class="card-inner">
                  <div class="icon-box">
                    <i class="fas fa-wine-bottle"></i>
                  </div>
                  <div class="info-box">
                    <h4>Phòng Thượng Hạng</h4>
                    <p class="sub-desc">Rượu ngon kỹ nữ</p>
                    <div class="detail-divider"></div>
                    <div class="detail-row">
                      <span class="label"><i class="far fa-clock"></i> Thời gian:</span>
                      <span class="value highlight">10s</span>
                    </div>
                    <div class="detail-row">
                      <span class="label"><i class="fas fa-coins"></i> Chi phí:</span>
                      <span class="value gold">200 Vàng</span>
                    </div>
                  </div>
                </div>
                <div class="vip-ribbon">THƯỢNG HẠNG</div>
              </div>
            </div>

            <button class="btn-close" @click="showSpaMenu = false">Đóng</button>
          </div>
          <div class="modal-border-bot"></div>
        </div>
      </div>
    </transition>

    <transition name="fade-modal">
      <div v-if="showRestModal" class="modal-overlay" @click.self="closeRestModal">
        <div class="dark-scroll-modal">
          <div class="modal-border-top"></div>
          <div class="modal-body">
            <div class="modal-stamp"><i class="fas fa-check"></i></div>
            <h3 class="modal-title">HỒI PHỤC HOÀN TẤT</h3>
            <p class="modal-msg">
              "Khách quan thần thái hồng hào, nội công sung mãn. Chúc ngài
              thượng lộ bình an!"
            </p>
            <button class="btn-confirm" @click="closeRestModal">ĐA TẠ</button>
          </div>
          <div class="modal-border-bot"></div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { useCharacterStore } from "../stores/characterStore";
import { useInventoryStore } from "../stores/inventoryStore";
import { useAuthStore } from "../stores/authStore";
import axiosClient from "../api/axiosClient";

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
  return Math.min(
    (charStore.character.currentHp / charStore.character.maxHp) * 100,
    100
  );
});

const energyPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min(
    (charStore.character.currentEnergy / charStore.character.maxEnergy) * 100,
    100
  );
});

const isFull = computed(
  () => hpPercent.value >= 100 && energyPercent.value >= 100
);

// --- Logic Mythic ---
const canEnterMythic = computed(() => {
  if (!inventoryStore.items || inventoryStore.items.length === 0) return false;
  return inventoryStore.items.some(item => (item.enhancementLevel || 0) >= 30);
});

const handleEnterMythic = () => {
  if (canEnterMythic.value) {
    router.push('/evolve-mythic');
  }
};

// --- Logic Spa ---
const openSpaMenu = () => {
  showSpaMenu.value = true;
};

const closeRestModal = () => {
  showRestModal.value = false;
  localStorage.removeItem(STORAGE_KEY_END_TIME);
};

const confirmRest = async (type) => {
  showSpaMenu.value = false;
  try {
    const res = await axiosClient.post("/spa/start", null, {
      params: { packageType: type },
    });
    const duration = res.data.secondsRemaining || (type === "VIP" ? 10 : 60);
    const finishTime = Date.now() + duration * 1000;
    localStorage.setItem(STORAGE_KEY_END_TIME, finishTime);

    countdown.value = duration;
    isResting.value = true;
    await authStore.fetchProfile();
    startTimer();
  } catch (e) {
    alert(e.response?.data || "Không thể nghỉ ngơi lúc này.");
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
    const now = Date.now();
    const remaining = Math.ceil((parseInt(savedEndTime) - now) / 1000);

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
  try {
    const res = await axiosClient.post("/spa/complete");
    if (charStore.character) {
      charStore.character.currentHp = res.data.currentHp;
      charStore.character.currentEnergy = res.data.currentEnergy;
    }
    isResting.value = false;
    showRestModal.value = true;
    localStorage.removeItem(STORAGE_KEY_END_TIME);
  } catch (e) {
    console.error(e);
    if (e.response && e.response.status !== 400) {
      isResting.value = false;
      localStorage.removeItem(STORAGE_KEY_END_TIME);
    }
  }
};

const checkBackgroundRest = () => {
  const savedEndTime = localStorage.getItem(STORAGE_KEY_END_TIME);
  if (savedEndTime) {
    const now = Date.now();
    const remaining = Math.ceil((parseInt(savedEndTime) - now) / 1000);
    if (remaining > 0) {
      isResting.value = true;
      countdown.value = remaining;
      startTimer();
    } else {
      isResting.value = true;
      finishRest();
    }
  }
};

onMounted(async () => {
  await charStore.fetchCharacter();
  if (authStore.token) {
    await authStore.fetchProfile();
  }
  if (inventoryStore.fetchInventory) {
    await inventoryStore.fetchInventory();
  }
  checkBackgroundRest();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --red-seal: #b71c1c;

  /* Màu Hover cho 3 thẻ chính */
  --hover-green: #00e676;
  --hover-gold: #ffd700;
  --hover-red: #ff1744;
}

.wuxia-dark-theme {
  background-color: var(--wood-dark);
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
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
  background-image: url("@/assets/Background/b_doanhtrai.png");
  background-size: cover;
  opacity: 0.8;
}

.village-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
}

.hub-header {
  text-align: center;
  margin-bottom: 50px;
}

.title-plaque {
  display: inline-block;
  padding: 10px 40px;
  border-bottom: 3px double var(--gold);
  position: relative;
  background: rgba(0, 0, 0, 0.5);
}

.hub-title {
  font-size: 3rem;
  margin: 0;
  color: var(--text-light);
  text-transform: uppercase;
  font-weight: 900;
}

.hub-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  width: 100%;
  margin-bottom: 40px;
  justify-content: center;
}

.location-card {
  background: var(--wood-dark);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  text-decoration: none;
  transition: 0.3s;
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

/* Hover chung (fallback) */
.location-card:hover:not(.resting-mode):not(.locked) {
  transform: translateY(-5px);
}

/* --- 1. KHÁCH ĐIẾM (Inn) --- */
.inn-card:hover:not(.resting-mode) {
  border-color: var(--hover-green);
  box-shadow: 0 0 15px rgba(0, 230, 118, 0.4);
}

.inn-card:hover:not(.resting-mode) .icon-circle {
  border-color: var(--hover-green);
  color: var(--hover-green);
  box-shadow: 0 0 10px rgba(0, 230, 118, 0.3);
}

.inn-card:hover:not(.resting-mode) .card-name {
  color: var(--hover-green);
}

/* --- 2. THẦN BINH (Forge) --- */
.forge-card:hover:not(.locked) {
  border-color: var(--hover-gold);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.4);
}

.forge-card:hover:not(.locked) .icon-circle {
  border-color: var(--hover-gold);
  color: var(--hover-gold);
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.3);
}

.forge-card:hover:not(.locked) .card-name {
  color: var(--hover-gold);
}

/* --- 3. THẦN ĐIỆN (Mythic) --- */
.mythic-card:hover:not(.locked) {
  border-color: var(--hover-red);
  box-shadow: 0 0 20px rgba(255, 23, 68, 0.5);
}

.mythic-card:hover:not(.locked) .icon-circle {
  border-color: var(--hover-red);
  color: var(--hover-red);
  box-shadow: 0 0 10px rgba(255, 23, 68, 0.3);
}

.mythic-card:hover:not(.locked) .card-name {
  color: var(--hover-red);
  text-shadow: 0 0 5px #b71c1c;
}

/* LOCKED STATE */
.mythic-card.locked {
  cursor: not-allowed;
  filter: grayscale(0.9);
  opacity: 0.8;
  border-color: #2c1e1b;
}

.lock-overlay {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #ef9a9a;
  font-weight: bold;
}

.lock-overlay i {
  font-size: 2rem;
  margin-bottom: 5px;
}

.card-content {
  padding: 25px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: radial-gradient(circle at center, #4e342e 0%, #3e2723 100%);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 15px;
}

.icon-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--wood-light);
  border: 2px solid var(--gold);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: var(--gold);
  transition: 0.3s;
}

.card-name {
  margin: 0;
  font-size: 1.5rem;
  color: var(--gold);
  font-weight: 800;
  transition: 0.3s;
}

.card-mid {
  flex: 1;
  margin-bottom: 20px;
}

.desc {
  color: #bdbdbd;
  font-size: 0.9rem;
  font-style: italic;
  margin-bottom: 15px;
}

.stats-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-track {
  flex: 1;
  height: 8px;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  transition: width 0.5s ease;
}

.hp-bar {
  background: linear-gradient(90deg, #b71c1c, #e53935);
}

.mp-bar {
  background: linear-gradient(90deg, #1565c0, #42a5f5);
}

.wuxia-btn {
  width: 100%;
  padding: 12px;
  background: var(--red-seal);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  font-family: "Noto Serif TC", serif;
}

.wuxia-btn:hover:not(:disabled) {
  background: #d32f2f;
}

.wuxia-btn:disabled {
  background: #444;
  cursor: not-allowed;
  opacity: 0.7;
}

.wuxia-btn.outline {
  background: transparent;
  border: 2px solid var(--wood-light);
  color: #d7ccc8;
}

.wuxia-btn.outline:hover {
  border-color: var(--gold);
  color: var(--gold);
}

/* Tắt hiệu ứng hover button khi card bị khóa */
.mythic-card.locked .wuxia-btn.outline:hover {
  border-color: var(--wood-light);
  color: #d7ccc8;
  cursor: not-allowed;
}

.night-scene {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.sleeping-anim {
  font-size: 3rem;
  color: var(--text-light);
  margin-bottom: 15px;
}

.deploy-container {
  width: 100%;
  max-width: 400px;
  margin-top: 30px;
}

.imperial-seal-btn {
  width: 100%;
  background: linear-gradient(to bottom, #b71c1c, #880e4f);
  border: 4px double var(--gold);
  padding: 15px;
  cursor: pointer;
}

.seal-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.seal-big {
  color: #fff;
  font-size: 1.8rem;
  font-weight: 900;
}

.seal-small {
  display: block;
  color: #ffcdd2;
  font-size: 0.8rem;
  margin-top: 4px;
}

/* --- MODAL STYLES --- */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(5px);
}

.dark-scroll-modal {
  width: 95%;
  max-width: 600px;
  background: var(--wood-dark);
  border: 1px solid var(--wood-light);
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
  transform: scale(1);
  animation: modalPop 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes modalPop {
  from {
    transform: scale(0.8);
    opacity: 0;
  }

  to {
    transform: scale(1);
    opacity: 1;
  }
}

.modal-body {
  padding: 30px;
  text-align: center;
  color: var(--text-light);
  background-color: #261a18;
}

/* Header Modal */
.modal-title {
  color: var(--gold);
  border-bottom: 1px solid rgba(255, 236, 179, 0.2);
  padding-bottom: 15px;
  margin-bottom: 25px;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.decor-line {
  color: var(--wood-light);
  font-weight: normal;
}

/* Grid Layout cho SPA Options */
.spa-options-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

/* Style cho từng Card trong Modal */
.spa-card {
  background: #3e2723;
  border: 2px solid var(--wood-light);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  transition: 0.3s;
  overflow: hidden;
}

.spa-card:hover {
  transform: translateY(-5px);
}

.card-inner {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  z-index: 1;
  position: relative;
}

/* Icon trong Modal */
.icon-box {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  margin-bottom: 5px;
  background: rgba(0, 0, 0, 0.3);
  border: 2px solid var(--wood-light);
  transition: 0.3s;
  /* Thêm color white cho icon thường */
  color: #fff; 
}

/* Info Box - SET TẤT CẢ LÀ TRẮNG MẶC ĐỊNH */
.info-box h4 {
  margin: 0;
  color: #ffffff; /* Trắng */
  font-size: 1.1rem;
  text-transform: uppercase;
  transition: color 0.3s;
}

.sub-desc {
  font-size: 0.8rem;
  color: #ffffff; /* Trắng */
  margin: 5px 0 10px;
  font-style: italic;
}

.detail-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  width: 100%;
  margin: 10px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 0.9rem;
  margin-bottom: 5px;
}

.detail-row .label {
  color: #ffffff; /* Trắng */
}

.detail-row .value {
  font-weight: bold;
  color: #ffffff; /* Trắng */
}

.value.gold {
  color: #ffd700;
}

/* 10s là XANH */
.value.highlight {
  color: #00e676 !important;
}

/* 60s là CAM ĐỎ (Slow) */
.value.slow {
  color: #ff5722 !important; /* Cam đỏ đậm */
}

/* --- STYLE RIÊNG TỪNG LOẠI PHÒNG --- */

/* 1. STANDARD - Hover TRẮNG XÁM (#eeeeee) */
.spa-card.standard:hover {
  border-color: #eeeeee; /* Đổi thành trắng xám như yêu cầu */
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.1); /* Glow nhẹ trắng */
}

.spa-card.standard:hover h4,
.spa-card.standard:hover .sub-desc,
.spa-card.standard:hover .label {
  color: #ffffff; /* Giữ chữ màu trắng sạch */
}

/* 2. VIP */
.spa-card.vip {
  background: linear-gradient(135deg, #3e2723 0%, #261815 100%);
  border-color: #ffb300;
  box-shadow: 0 0 15px rgba(255, 179, 0, 0.1);
}

/* VIP HOVER - ĐỔI MÀU CHỮ THÀNH VÀNG */
.spa-card.vip:hover {
  box-shadow: 0 0 25px rgba(255, 179, 0, 0.4);
  transform: translateY(-5px) scale(1.02);
}

/* Icon box VIP hover */
.spa-card.vip:hover .icon-box {
  border-color: #ffb300;
  color: #ffb300; /* Vàng */
  box-shadow: 0 0 10px rgba(255, 179, 0, 0.3);
}

/* Title & Text VIP hover */
.spa-card.vip:hover h4 {
  color: #ffb300; /* Vàng */
  text-shadow: 0 0 5px rgba(255, 179, 0, 0.5);
}

.spa-card.vip:hover .sub-desc,
.spa-card.vip:hover .detail-row .label,
.spa-card.vip:hover .detail-row .value {
  color: #ffb300;
}

/* Override lại màu riêng cho value trong VIP hover để giữ đúng tính chất */
.spa-card.vip:hover .value.highlight {
  color: #00e676 !important; /* Vẫn giữ xanh cho 10s */
}
.spa-card.vip:hover .value.gold {
  color: #ffd700; /* Vẫn giữ vàng cho giá */
}

/* Dải băng THƯỢNG HẠNG (CĂN LẠI CHUẨN) */
.spa-card.vip .vip-ribbon {
  position: absolute;
  top: 18px;        /* Đẩy xuống một xíu */
  right: -45px;     /* Kéo sang phải để cân đối */
  width: 150px;     /* Set width cố định để text-align center hoạt động chuẩn */
  text-align: center;
  background: #d50000;
  color: #fff;
  font-weight: bold;
  font-size: 0.6rem; /* Font nhỏ lại tí cho vừa */
  padding: 5px 0;   /* Padding top/bot thôi, left/right tự lo do width */
  transform: rotate(45deg);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
  z-index: 2;
  pointer-events: none; /* Tránh click nhầm */
}

.btn-confirm,
.btn-close {
  background: var(--wood-light);
  color: var(--gold);
  border: 2px solid var(--gold);
  padding: 10px 40px;
  font-weight: bold;
  cursor: pointer;
}

.btn-close {
  width: 100%;
  margin-top: 10px;
  background: #b71c1c;
  border-color: #d32f2f;
  color: #fff;
}

.btn-close:hover {
  background: rgba(229, 115, 115, 0.1);
}

/* Responsive cho Mobile */
@media (max-width: 600px) {
  .spa-options-grid {
    grid-template-columns: 1fr;
  }
}
</style>