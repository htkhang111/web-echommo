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
        <div class="dark-scroll-modal result-modal">
          <div class="modal-border-top"></div>
          <div class="modal-body">
            <div class="modal-stamp glow-success"><i class="fas fa-check"></i></div>
            
            <h3 class="modal-title success-title">HỒI PHỤC HOÀN TẤT</h3>
            
            <div class="msg-box">
              <p class="modal-msg">
                "Khách quan thần thái hồng hào, nội công sung mãn.<br>
                Chúc ngài thượng lộ bình an!"
              </p>
            </div>

            <button class="btn-confirm-hero" @click="closeRestModal">
              <span class="btn-text">ĐA TẠ</span>
            </button>
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
    const res = await axiosClient.post("/spa/relax", { 
      type: type 
    });

    const duration = res.data.secondsRemaining || (type === "VIP" ? 10 : 60);
    const finishTime = Date.now() + duration * 1000;
    
    localStorage.setItem(STORAGE_KEY_END_TIME, finishTime);

    countdown.value = duration;
    isResting.value = true;
    await authStore.fetchProfile();
    startTimer();
  } catch (e) {
    const errorMsg = e.response?.data?.message || e.response?.data || "Không thể nghỉ ngơi lúc này.";
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
    await charStore.fetchCharacter();
    isResting.value = false;
    showRestModal.value = true;
    localStorage.removeItem(STORAGE_KEY_END_TIME);
  } catch (e) {
    console.error(e);
    isResting.value = false;
    localStorage.removeItem(STORAGE_KEY_END_TIME);
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

.btn-close {
  width: 100%;
  margin-top: 10px;
  padding: 10px 40px;
  background: #b71c1c;
  color: #fff;
  border: 2px solid #d32f2f;
  font-weight: bold;
  cursor: pointer;
}

.btn-close:hover {
  background: rgba(229, 115, 115, 0.1);
}

/* ========================================= */
/* NEW STYLES FOR RESULT MODAL         */
/* ========================================= */

.result-modal {
  /* Làm cho modal kết quả trông gọn gàng hơn */
  max-width: 450px; 
}

/* 1. Icon Check Glow */
.glow-success {
  font-size: 5rem;
  color: #00e676; /* Xanh ngọc */
  margin-bottom: 20px;
  filter: drop-shadow(0 0 15px rgba(0, 230, 118, 0.6)); /* Hiệu ứng tỏa sáng */
  animation: pulseIcon 2s infinite ease-in-out;
}

@keyframes pulseIcon {
  0% { transform: scale(1); filter: drop-shadow(0 0 15px rgba(0, 230, 118, 0.6)); }
  50% { transform: scale(1.1); filter: drop-shadow(0 0 25px rgba(0, 230, 118, 0.9)); }
  100% { transform: scale(1); filter: drop-shadow(0 0 15px rgba(0, 230, 118, 0.6)); }
}

/* 2. Tiêu đề Modal */
.success-title {
  font-size: 1.8rem;
  color: #ffecb3;
  text-transform: uppercase;
  margin-bottom: 15px;
  border-bottom: 1px solid rgba(255, 236, 179, 0.2);
  padding-bottom: 15px;
  letter-spacing: 1px;
}

/* 3. Message Text */
.msg-box {
  background: rgba(0, 0, 0, 0.2); /* Nền mờ nhẹ cho text dễ đọc */
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 25px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.modal-msg {
  font-size: 1.1rem;
  font-style: italic;
  line-height: 1.6;
  color: #e0e0e0;
  margin: 0;
}

/* 4. NÚT ĐA TẠ (Hero Style) */
.btn-confirm-hero {
  margin-top: 10px;
  background: linear-gradient(to bottom, #d50000, #9b0000); /* Đỏ thẫm sang trọng */
  color: #fff;
  border: 2px solid #ffb300; /* Viền vàng kim */
  padding: 12px 60px;
  font-size: 1.3rem;
  font-weight: 900;
  letter-spacing: 2px;
  font-family: "Noto Serif TC", serif;
  cursor: pointer;
  box-shadow: 0 5px 15px rgba(213, 0, 0, 0.5); /* Bóng đỏ mờ */
  border-radius: 4px; /* Bo nhẹ góc */
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
}

/* Hiệu ứng Hover nút Đa Tạ */
.btn-confirm-hero:hover {
  transform: translateY(-3px) scale(1.02); /* Nổi lên nhẹ */
  box-shadow: 0 8px 25px rgba(255, 179, 0, 0.6); /* Bóng vàng rực hơn khi hover */
  background: linear-gradient(to bottom, #ff1744, #b71c1c); /* Sáng màu hơn */
  border-color: #ffd700; /* Viền vàng sáng hơn */
}

.btn-confirm-hero:active {
  transform: translateY(1px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
}

/* Responsive cho Mobile */
@media (max-width: 600px) {
  .spa-options-grid {
    grid-template-columns: 1fr;
  }
}
</style> -->

<!-- <template>
  <div class="page-container wuxia-dashboard">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
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
               <span class="greet-txt"><i class="fas fa-clock"></i> {{ wuxiaTime.zodiac }}</span>
               <h1 class="char-name">LẠC DƯƠNG THÀNH</h1>
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
          <div class="wood-card inn-card" :class="{ 'resting-mode': isResting }">
             <div class="card-bg-pattern"></div>
             <div class="card-content">
                <template v-if="!isResting">
                  <div class="tile-icon"><i class="fas fa-bed"></i></div>
                  <h3 class="tile-title">KHÁCH ĐIẾM</h3>
                  <p class="tile-desc">Hồi phục sinh lực</p>
                  
                  <div class="stats-mini">
                    <div class="stat-row">
                      <span class="lbl">HP</span>
                      <div class="bar-track"><div class="bar-fill hp" :style="{ width: hpPercent + '%' }"></div></div>
                    </div>
                    <div class="stat-row">
                      <span class="lbl">MP</span>
                      <div class="bar-track"><div class="bar-fill mp" :style="{ width: energyPercent + '%' }"></div></div>
                    </div>
                  </div>

                  <button class="wuxia-btn outline" @click="openSpaMenu" :disabled="isFull">
                    {{ isFull ? 'SUNG MÃN' : 'NGHỈ NGƠI' }}
                  </button>
                </template>

                <template v-else>
                  <div class="night-scene">
                    <div class="sleeping-anim">
                      <i class="fas fa-user-ninja"></i>
                      <div class="zzz-particles"><span>z</span><span>z</span><span>Z</span></div>
                    </div>
                    <div class="resting-text">
                      Đang điều tức... <br><span class="count-txt">{{ countdown }}s</span>
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

          <div class="wood-card mythic-card" :class="{ 'locked': !canEnterMythic }" @click="handleEnterMythic">
             <div class="card-bg-pattern"></div>
             <div class="card-content">
                <div class="tile-icon"><i class="fas fa-dragon"></i></div>
                <h3 class="tile-title">THẦN ĐIỆN</h3>
                <p class="tile-desc">Chuyển hóa thần khí</p>
                
                <div v-if="!canEnterMythic" class="lock-msg">
                  <i class="fas fa-lock"></i> <span style="margin-left:5px">Cần đồ +30</span>
                </div>
                <div v-else class="wuxia-btn outline">VÀO ĐIỆN</div>
             </div>
             <div class="corner-decor top-right"></div>
             <div class="corner-decor bottom-left"></div>
          </div>
        </div>

        <div class="deploy-container">
           <button @click="$router.push('/explore')" class="wood-card hero-tile" :disabled="isResting">
              <div class="card-content row-layout">
                 <div class="icon-stamp small"><i class="fas fa-torii-gate"></i></div>
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
      <div v-if="showSpaMenu" class="modal-overlay" @click.self="showSpaMenu = false">
        <div class="wood-panel modal-box spa-menu">
           <h3 class="modal-header-txt">~ CHỌN PHÒNG NGHỈ ~</h3>
           
           <div class="spa-options-grid">
              <div class="wood-card spa-item standard" @click="confirmRest('STANDARD')">
                 <div class="tile-icon small"><i class="fas fa-mug-hot"></i></div>
                 <h4>Phòng Bình Dân</h4>
                 <div class="meta-row"><span><i class="far fa-clock"></i> 60s</span> <span class="gold-txt">50 Vàng</span></div>
              </div>

              <div class="wood-card spa-item vip" @click="confirmRest('VIP')">
                 <div class="vip-ribbon">THƯỢNG HẠNG</div>
                 <div class="tile-icon small"><i class="fas fa-wine-bottle"></i></div>
                 <h4>Phòng Thượng Hạng</h4>
                 <div class="meta-row"><span><i class="far fa-clock"></i> 10s</span> <span class="gold-txt">200 Vàng</span></div>
              </div>
           </div>
           <button class="wuxia-btn close-btn" @click="showSpaMenu = false">ĐÓNG</button>
        </div>
      </div>
    </transition>

    <transition name="fade-modal">
      <div v-if="showRestModal" class="modal-overlay" @click.self="closeRestModal">
        <div class="wood-panel modal-box result-modal">
           <div class="success-icon"><i class="fas fa-check-circle"></i></div>
           <h3 class="modal-header-txt">HỒI PHỤC HOÀN TẤT</h3>
           <p class="modal-msg">"Khách quan thần thái hồng hào, nội công sung mãn. Chúc ngài thượng lộ bình an!"</p>
           <button class="wuxia-btn full-width" @click="closeRestModal">ĐA TẠ</button>
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

const canEnterMythic = computed(() => {
  if (!inventoryStore.items || inventoryStore.items.length === 0) return false;
  return inventoryStore.items.some(item => (item.enhancementLevel || 0) >= 30);
});

const handleEnterMythic = () => {
  if (canEnterMythic.value) router.push('/evolve-mythic');
};

const openSpaMenu = () => showSpaMenu.value = true;

const closeRestModal = () => {
  showRestModal.value = false;
  localStorage.removeItem(STORAGE_KEY_END_TIME);
};

const confirmRest = async (type) => {
  showSpaMenu.value = false;
  try {
    const res = await axiosClient.post("/spa/relax", { type: type });
    const duration = res.data.secondsRemaining || (type === "VIP" ? 10 : 60);
    const finishTime = Date.now() + duration * 1000;
    
    localStorage.setItem(STORAGE_KEY_END_TIME, finishTime);
    countdown.value = duration;
    isResting.value = true;
    await authStore.fetchProfile(); 
    startTimer();
  } catch (e) {
    const errorMsg = e.response?.data?.message || "Lỗi hệ thống";
    alert(errorMsg);
  }
};

const startTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    const savedEndTime = localStorage.getItem(STORAGE_KEY_END_TIME);
    if (!savedEndTime) { clearInterval(timerInterval); return; }
    
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
  try {
    await charStore.fetchCharacter();
    isResting.value = false;
    showRestModal.value = true;
    localStorage.removeItem(STORAGE_KEY_END_TIME);
  } catch (e) { console.error(e); }
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
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif:ital,wght@0,400;0,700;1,400&family=Playfair+Display:wght@700;900&display=swap");

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

/* --- BACKGROUND SYSTEM --- */
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center bottom; opacity: 0.6; filter: sepia(10%) contrast(1.1); }
.wood-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); mix-blend-mode: multiply; transition: background 2s ease; }
.wood-overlay.night-mode { background: linear-gradient(to bottom, rgba(10, 5, 20, 0.85), rgba(0, 0, 0, 0.95)); }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle, transparent 60%, #1a100d 100%); }

.dashboard-wrapper { position: relative; z-index: 10; max-width: 1000px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }

/* --- HEADER --- */
.wood-panel {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(90deg, rgba(62, 39, 35, 0.95), rgba(93, 64, 55, 0.9));
  border: 2px solid #6d4c41; border-radius: 6px; padding: 10px 20px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
}
.header-left { display: flex; flex-direction: column; gap: 4px; }
.server-tag { font-size: 0.75rem; color: var(--text-dim); display: flex; align-items: center; gap: 6px; }
.status-orb { width: 6px; height: 6px; background: #66bb6a; border-radius: 50%; box-shadow: 0 0 5px #66bb6a; }
.greet-txt { font-size: 0.8rem; color: var(--gold); }
.char-name { font-family: "Playfair Display", serif; font-size: 1.8rem; margin: 0; color: #fff; text-shadow: 0 2px 4px rgba(0,0,0,0.6); line-height: 1; }
.header-right { display: flex; align-items: center; gap: 15px; }
.wealth-item { display: flex; align-items: center; gap: 6px; font-weight: bold; color: var(--gold-accent); font-size: 1rem; }
.gold-icon { color: #ffd700; filter: drop-shadow(0 0 5px rgba(255, 215, 0, 0.5)); }
.weather-seal { display: flex; align-items: center; gap: 8px; color: var(--text-dim); }

/* --- CONTENT GRID --- */
.town-content { padding-top: 20px; }
.ornament-divider { text-align: center; color: var(--gold); opacity: 0.6; margin-bottom: 20px; font-size: 0.9rem; display: flex; align-items: center; justify-content: center; gap: 10px; }
.ornament-divider .diamond { font-size: 0.7rem; }

.hub-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-bottom: 25px; }

/* --- WOOD CARD STYLE --- */
.wood-card {
  position: relative; background: linear-gradient(135deg, var(--wood-card) 0%, var(--wood-base) 100%);
  border: 1px solid #6d4c41; border-radius: 6px; overflow: hidden;
  transition: all 0.3s ease; box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  cursor: pointer; text-decoration: none; display: block;
}
.wood-card:hover:not(.locked):not(.resting-mode) {
  transform: translateY(-4px); border-color: var(--gold-accent);
  box-shadow: 0 15px 30px rgba(0,0,0,0.6), 0 0 10px rgba(255, 215, 0, 0.1);
}
.card-content { padding: 25px; display: flex; flex-direction: column; align-items: center; text-align: center; position: relative; z-index: 2; height: 100%; box-sizing: border-box; }
.tile-icon { font-size: 2.2rem; color: var(--text-dim); margin-bottom: 15px; transition: 0.3s; }
.wood-card:hover .tile-icon { color: var(--gold-accent); transform: scale(1.1); filter: drop-shadow(0 0 8px rgba(255,215,0,0.4)); }

.tile-title { margin: 0; font-family: "Playfair Display", serif; font-size: 1.4rem; color: #fff; margin-bottom: 5px; }
.tile-desc { font-size: 0.85rem; color: var(--gold); font-style: italic; margin-bottom: 20px; opacity: 0.8; }

.corner-decor { position: absolute; width: 8px; height: 8px; border: 1px solid transparent; transition: 0.3s; }
.top-right { top: 4px; right: 4px; border-top-color: rgba(255,255,255,0.1); border-right-color: rgba(255,255,255,0.1); }
.bottom-left { bottom: 4px; left: 4px; border-bottom-color: rgba(255,255,255,0.1); border-left-color: rgba(255,255,255,0.1); }
.wood-card:hover .corner-decor { width: 100%; height: 100%; border-color: var(--gold-accent); opacity: 0.3; }

/* ========================================= */
/* 🔥 BUTTON STYLES (Đã update dạng Outline) */
/* ========================================= */
.wuxia-btn {
  font-family: "Noto Serif", serif;
  font-weight: bold;
  cursor: pointer;
  margin-top: auto;
  transition: all 0.3s ease;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 1px;
  display: inline-block;
  line-height: 1.2;
}

/* Kiểu Outline (Viền mảnh, bo tròn) */
.wuxia-btn.outline {
  background: transparent;
  border: 1px solid var(--text-dim);
  color: var(--text-dim);
  padding: 10px 30px;
  border-radius: 30px;
  width: auto;
}

/* Hover Effect cho nút Outline */
.wood-card:hover .wuxia-btn.outline:not(:disabled),
.wuxia-btn.outline:hover:not(:disabled) {
  border-color: var(--gold-accent);
  color: var(--gold-accent);
  background: rgba(255, 215, 0, 0.05);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.2);
}

.wuxia-btn.outline:active { transform: scale(0.95); }
.wuxia-btn.outline:disabled { color: #4e342e; border-color: #3e2723; cursor: not-allowed; }

/* Kiểu nút đóng / full width */
.wuxia-btn.close-btn { background: #3e2723; color: #d7ccc8; border: none; padding: 10px; width: 100%; margin-top: 15px; border-radius: 4px; }
.wuxia-btn.full-width { background: var(--gold-accent); color: #261815; border: none; padding: 10px; width: 100%; border-radius: 4px; }

/* --- SPECIFIC CARD STYLES --- */
/* Inn Stats */
.stats-mini { width: 100%; margin-bottom: 25px; display: flex; flex-direction: column; gap: 8px; }
.stat-row { display: flex; align-items: center; gap: 8px; font-size: 0.75rem; color: var(--text-dim); font-weight: bold; }
.bar-track { flex: 1; height: 4px; background: rgba(0,0,0,0.5); border-radius: 2px; overflow: hidden; }
.bar-fill { height: 100%; transition: width 0.5s; }
.bar-fill.hp { background: #ef5350; }
.bar-fill.mp { background: #42a5f5; }

/* Resting Animation */
.resting-mode { border-color: #66bb6a; background: #1b5e20; cursor: default; }
.night-scene { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #a5d6a7; }
.sleeping-anim { font-size: 2rem; margin-bottom: 10px; position: relative; }
.zzz-particles span { position: absolute; font-size: 0.8rem; animation: floatUp 2s infinite; opacity: 0; top: 0; right: -10px; }
.zzz-particles span:nth-child(2) { animation-delay: 0.6s; right: -15px; }
.zzz-particles span:nth-child(3) { animation-delay: 1.2s; right: -5px; }
@keyframes floatUp { 0% { transform: translateY(0); opacity: 0; } 50% { opacity: 1; } 100% { transform: translateY(-20px); opacity: 0; } }
.count-txt { font-weight: bold; font-size: 1.2rem; color: #fff; }

/* Locked Mythic */
.mythic-card.locked { filter: grayscale(1); opacity: 0.7; cursor: not-allowed; }
.lock-msg { color: #ef9a9a; font-size: 0.85rem; margin-top: auto; display: flex; align-items: center; gap: 5px; }

/* Deploy Button */
.deploy-container { margin-top: 10px; }
.hero-tile { background: radial-gradient(circle at center, #4e342e 0%, #261815 100%); border: 2px solid var(--gold-accent); width: 100%; }
.hero-tile .card-content { flex-direction: row; justify-content: space-between; text-align: left; padding: 20px 30px; }
.hero-tile .row-layout { width: 100%; display: flex; align-items: center; gap: 20px; }
.icon-stamp.small { width: 50px; height: 50px; font-size: 1.5rem; border: 2px double var(--gold-accent); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--gold-accent); background: rgba(0,0,0,0.3); }
.text-group { flex: 1; }
.hero-title.small { font-size: 1.5rem; margin: 0; color: #fff; line-height: 1; }
.arrow-indicator { font-size: 1.2rem; color: var(--gold-accent); animation: slideRight 1s infinite alternate; }
@keyframes slideRight { from { transform: translateX(0); } to { transform: translateX(5px); } }

/* --- MODALS --- */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); display: flex; justify-content: center; align-items: center; z-index: 2000; backdrop-filter: blur(4px); }
.modal-box { width: 90%; max-width: 500px; flex-direction: column; padding: 30px; background: #261815; border-color: var(--gold); }
.modal-header-txt { color: var(--gold); font-family: "Playfair Display", serif; margin: 0 0 20px 0; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; width: 100%; text-align: center; }

.spa-options-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; width: 100%; margin-bottom: 20px; }
.spa-item { padding: 15px; border-radius: 4px; display: flex; flex-direction: column; align-items: center; gap: 10px; border: 1px solid #5d4037; position: relative; overflow: hidden; }
.spa-item:hover { border-color: var(--gold-accent); background: rgba(255,215,0,0.05); }
.tile-icon.small { font-size: 1.5rem; margin: 0; }
.spa-item h4 { margin: 0; font-size: 1rem; color: #fff; }
.meta-row { display: flex; justify-content: space-between; width: 100%; font-size: 0.8rem; color: var(--text-dim); }
.gold-txt { color: #ffd700; }
.vip-ribbon { position: absolute; top: 0; right: 0; background: #b71c1c; color: #fff; font-size: 0.6rem; padding: 2px 8px; border-bottom-left-radius: 4px; font-weight: bold; }

.success-icon { font-size: 4rem; color: #66bb6a; margin-bottom: 15px; text-shadow: 0 0 20px rgba(102,187,106,0.4); }
.modal-msg { text-align: center; font-style: italic; color: var(--text-dim); margin-bottom: 25px; line-height: 1.5; }

@media (max-width: 600px) {
  .hero-tile .card-content { padding: 15px; }
  .icon-stamp.small { width: 40px; height: 40px; font-size: 1.2rem; }
  .hero-title.small { font-size: 1.2rem; }
  .header-right { flex-direction: column; align-items: flex-end; gap: 5px; }
}
</style> -->

<!-- <template>
  <div class="page-container wuxia-dashboard">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
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
               <span class="greet-txt"><i class="fas fa-clock"></i> {{ wuxiaTime.zodiac }}</span>
               <h1 class="char-name">LẠC DƯƠNG THÀNH</h1>
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
          <div class="wood-card inn-card" :class="{ 'resting-mode': isResting }">
             <div class="card-bg-pattern"></div>
             <div class="card-content">
                <template v-if="!isResting">
                  <div class="tile-icon"><i class="fas fa-bed"></i></div>
                  <h3 class="tile-title">KHÁCH ĐIẾM</h3>
                  <p class="tile-desc">Hồi phục sinh lực</p>
                  
                  <div class="stats-mini">
                    <div class="stat-row">
                      <span class="lbl">HP</span>
                      <div class="bar-track"><div class="bar-fill hp" :style="{ width: hpPercent + '%' }"></div></div>
                    </div>
                    <div class="stat-row">
                      <span class="lbl">MP</span>
                      <div class="bar-track"><div class="bar-fill mp" :style="{ width: energyPercent + '%' }"></div></div>
                    </div>
                  </div>

                  <button class="wuxia-btn outline" @click="openSpaMenu" :disabled="isFull">
                    {{ isFull ? 'SUNG MÃN' : 'NGHỈ NGƠI' }}
                  </button>
                </template>

                <template v-else>
                  <div class="night-scene">
                    <div class="sleeping-anim">
                      <i class="fas fa-user-ninja"></i>
                      <div class="zzz-particles"><span>z</span><span>z</span><span>Z</span></div>
                    </div>
                    <div class="resting-text">
                      Đang điều tức... <br><span class="count-txt">{{ countdown }}s</span>
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

          <div class="wood-card mythic-card" :class="{ 'locked': !canEnterMythic }" @click="handleEnterMythic">
             <div class="card-bg-pattern"></div>
             <div class="card-content">
                <div class="tile-icon"><i class="fas fa-dragon"></i></div>
                <h3 class="tile-title">THẦN ĐIỆN</h3>
                <p class="tile-desc">Chuyển hóa thần khí</p>
                
                <div v-if="!canEnterMythic" class="lock-msg">
                  <i class="fas fa-lock"></i> <span style="margin-left:5px">Cần đồ +30</span>
                </div>
                <div v-else class="wuxia-btn outline">VÀO ĐIỆN</div>
             </div>
             <div class="corner-decor top-right"></div>
             <div class="corner-decor bottom-left"></div>
          </div>
        </div>

        <div class="deploy-container">
           <button @click="$router.push('/explore')" class="wood-card hero-tile" :disabled="isResting">
              <div class="card-content row-layout">
                 <div class="icon-stamp small"><i class="fas fa-torii-gate"></i></div>
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
      <div v-if="showSpaMenu" class="modal-overlay" @click.self="showSpaMenu = false">
        <div class="wood-panel modal-box spa-menu">
           <h3 class="modal-header-txt">~ CHỌN PHÒNG NGHỈ ~</h3>
           
           <div class="spa-options-grid">
              <div class="wood-card spa-item standard" @click="confirmRest('BASIC')">
                 <div class="tile-icon small"><i class="fas fa-mug-hot"></i></div>
                 <h4>Phòng Bình Dân</h4>
                 <div class="meta-row"><span><i class="far fa-clock"></i> 60s</span> <span class="gold-txt">50 Vàng</span></div>
              </div>

              <div class="wood-card spa-item vip" @click="confirmRest('VIP')">
                 <div class="vip-ribbon">THƯỢNG HẠNG</div>
                 <div class="tile-icon small"><i class="fas fa-wine-bottle"></i></div>
                 <h4>Phòng Thượng Hạng</h4>
                 <div class="meta-row"><span><i class="far fa-clock"></i> 10s</span> <span class="gold-txt">200 Vàng</span></div>
              </div>
           </div>
           <button class="wuxia-btn close-btn" @click="showSpaMenu = false">ĐÓNG</button>
        </div>
      </div>
    </transition>

    <transition name="fade-modal">
      <div v-if="showRestModal" class="modal-overlay" @click.self="closeRestModal">
        <div class="wood-panel modal-box result-modal">
           <div class="success-icon"><i class="fas fa-check-circle"></i></div>
           <h3 class="modal-header-txt">HỒI PHỤC HOÀN TẤT</h3>
           <p class="modal-msg">"Khách quan thần thái hồng hào, nội công sung mãn. Chúc ngài thượng lộ bình an!"</p>
           <button class="wuxia-btn full-width" @click="closeRestModal">ĐA TẠ</button>
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

const canEnterMythic = computed(() => {
  if (!inventoryStore.items || inventoryStore.items.length === 0) return false;
  return inventoryStore.items.some(item => (item.enhancementLevel || 0) >= 30);
});

const handleEnterMythic = () => {
  if (canEnterMythic.value) router.push('/evolve-mythic');
};

const openSpaMenu = () => showSpaMenu.value = true;

const closeRestModal = () => {
  showRestModal.value = false;
  localStorage.removeItem(STORAGE_KEY_END_TIME);
};

const confirmRest = async (type) => {
  showSpaMenu.value = false;
  try {
    const res = await axiosClient.post("/spa/relax", { type: type });
    // Fallback: VIP=10s, BASIC=60s nếu API không trả về secondsRemaining
    const duration = res.data.secondsRemaining || (type === "VIP" ? 10 : 60);
    const finishTime = Date.now() + duration * 1000;
    
    localStorage.setItem(STORAGE_KEY_END_TIME, finishTime);
    countdown.value = duration;
    isResting.value = true;
    await authStore.fetchProfile(); 
    startTimer();
  } catch (e) {
    const errorMsg = e.response?.data?.message || "Lỗi hệ thống";
    alert(errorMsg);
  }
};

const startTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    const savedEndTime = localStorage.getItem(STORAGE_KEY_END_TIME);
    if (!savedEndTime) { clearInterval(timerInterval); return; }
    
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
  try {
    await charStore.fetchCharacter();
    isResting.value = false;
    showRestModal.value = true;
    localStorage.removeItem(STORAGE_KEY_END_TIME);
  } catch (e) { console.error(e); }
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
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif:ital,wght@0,400;0,700;1,400&family=Playfair+Display:wght@700;900&display=swap");

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

/* --- BACKGROUND SYSTEM --- */
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center bottom; opacity: 0.6; filter: sepia(10%) contrast(1.1); }
.wood-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); mix-blend-mode: multiply; transition: background 2s ease; }
.wood-overlay.night-mode { background: linear-gradient(to bottom, rgba(10, 5, 20, 0.85), rgba(0, 0, 0, 0.95)); }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle, transparent 60%, #1a100d 100%); }

.dashboard-wrapper { position: relative; z-index: 10; max-width: 1000px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }

/* --- HEADER --- */
.wood-panel {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(90deg, rgba(62, 39, 35, 0.95), rgba(93, 64, 55, 0.9));
  border: 2px solid #6d4c41; border-radius: 6px; padding: 10px 20px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
}
.header-left { display: flex; flex-direction: column; gap: 4px; }
.server-tag { font-size: 0.75rem; color: var(--text-dim); display: flex; align-items: center; gap: 6px; }
.status-orb { width: 6px; height: 6px; background: #66bb6a; border-radius: 50%; box-shadow: 0 0 5px #66bb6a; }
.greet-txt { font-size: 0.8rem; color: var(--gold); }
.char-name { font-family: "Playfair Display", serif; font-size: 1.8rem; margin: 0; color: #fff; text-shadow: 0 2px 4px rgba(0,0,0,0.6); line-height: 1; }
.header-right { display: flex; align-items: center; gap: 15px; }
.wealth-item { display: flex; align-items: center; gap: 6px; font-weight: bold; color: var(--gold-accent); font-size: 1rem; }
.gold-icon { color: #ffd700; filter: drop-shadow(0 0 5px rgba(255, 215, 0, 0.5)); }
.weather-seal { display: flex; align-items: center; gap: 8px; color: var(--text-dim); }

/* --- CONTENT GRID --- */
.town-content { padding-top: 20px; }
.ornament-divider { text-align: center; color: var(--gold); opacity: 0.6; margin-bottom: 20px; font-size: 0.9rem; display: flex; align-items: center; justify-content: center; gap: 10px; }
.ornament-divider .diamond { font-size: 0.7rem; }

.hub-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-bottom: 25px; }

/* --- WOOD CARD STYLE --- */
.wood-card {
  position: relative; background: linear-gradient(135deg, var(--wood-card) 0%, var(--wood-base) 100%);
  border: 1px solid #6d4c41; border-radius: 6px; overflow: hidden;
  transition: all 0.3s ease; box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  cursor: pointer; text-decoration: none; display: block;
}
.wood-card:hover:not(.locked):not(.resting-mode) {
  transform: translateY(-4px); border-color: var(--gold-accent);
  box-shadow: 0 15px 30px rgba(0,0,0,0.6), 0 0 10px rgba(255, 215, 0, 0.1);
}
.card-content { padding: 25px; display: flex; flex-direction: column; align-items: center; text-align: center; position: relative; z-index: 2; height: 100%; box-sizing: border-box; }
.tile-icon { font-size: 2.2rem; color: var(--text-dim); margin-bottom: 15px; transition: 0.3s; }
.wood-card:hover .tile-icon { color: var(--gold-accent); transform: scale(1.1); filter: drop-shadow(0 0 8px rgba(255,215,0,0.4)); }

.tile-title { margin: 0; font-family: "Playfair Display", serif; font-size: 1.4rem; color: #fff; margin-bottom: 5px; }
.tile-desc { font-size: 0.85rem; color: var(--gold); font-style: italic; margin-bottom: 20px; opacity: 0.8; }

.corner-decor { position: absolute; width: 8px; height: 8px; border: 1px solid transparent; transition: 0.3s; }
.top-right { top: 4px; right: 4px; border-top-color: rgba(255,255,255,0.1); border-right-color: rgba(255,255,255,0.1); }
.bottom-left { bottom: 4px; left: 4px; border-bottom-color: rgba(255,255,255,0.1); border-left-color: rgba(255,255,255,0.1); }
.wood-card:hover .corner-decor { width: 100%; height: 100%; border-color: var(--gold-accent); opacity: 0.3; }

/* ========================================= */
/* 🔥 BUTTON STYLES (Đã update dạng Outline) */
/* ========================================= */
.wuxia-btn {
  font-family: "Noto Serif", serif;
  font-weight: bold;
  cursor: pointer;
  margin-top: auto;
  transition: all 0.3s ease;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 1px;
  display: inline-block;
  line-height: 1.2;
}

/* Kiểu Outline (Viền mảnh, bo tròn) */
.wuxia-btn.outline {
  background: transparent;
  border: 1px solid var(--text-dim);
  color: var(--text-dim);
  padding: 10px 30px;
  border-radius: 30px;
  width: auto;
}

/* Hover Effect cho nút Outline */
.wood-card:hover .wuxia-btn.outline:not(:disabled),
.wuxia-btn.outline:hover:not(:disabled) {
  border-color: var(--gold-accent);
  color: var(--gold-accent);
  background: rgba(255, 215, 0, 0.05);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.2);
}

.wuxia-btn.outline:active { transform: scale(0.95); }
.wuxia-btn.outline:disabled { color: #4e342e; border-color: #3e2723; cursor: not-allowed; }

/* Kiểu nút đóng / full width */
.wuxia-btn.close-btn { background: #3e2723; color: #d7ccc8; border: none; padding: 10px; width: 100%; margin-top: 15px; border-radius: 4px; }
.wuxia-btn.full-width { background: var(--gold-accent); color: #261815; border: none; padding: 10px; width: 100%; border-radius: 4px; }

/* --- SPECIFIC CARD STYLES --- */
/* Inn Stats */
.stats-mini { width: 100%; margin-bottom: 25px; display: flex; flex-direction: column; gap: 8px; }
.stat-row { display: flex; align-items: center; gap: 8px; font-size: 0.75rem; color: var(--text-dim); font-weight: bold; }
.bar-track { flex: 1; height: 4px; background: rgba(0,0,0,0.5); border-radius: 2px; overflow: hidden; }
.bar-fill { height: 100%; transition: width 0.5s; }
.bar-fill.hp { background: #ef5350; }
.bar-fill.mp { background: #42a5f5; }

/* Resting Animation */
.resting-mode { border-color: #66bb6a; background: #1b5e20; cursor: default; }
.night-scene { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #a5d6a7; }
.sleeping-anim { font-size: 2rem; margin-bottom: 10px; position: relative; }
.zzz-particles span { position: absolute; font-size: 0.8rem; animation: floatUp 2s infinite; opacity: 0; top: 0; right: -10px; }
.zzz-particles span:nth-child(2) { animation-delay: 0.6s; right: -15px; }
.zzz-particles span:nth-child(3) { animation-delay: 1.2s; right: -5px; }
@keyframes floatUp { 0% { transform: translateY(0); opacity: 0; } 50% { opacity: 1; } 100% { transform: translateY(-20px); opacity: 0; } }
.count-txt { font-weight: bold; font-size: 1.2rem; color: #fff; }

/* Locked Mythic */
.mythic-card.locked { filter: grayscale(1); opacity: 0.7; cursor: not-allowed; }
.lock-msg { color: #ef9a9a; font-size: 0.85rem; margin-top: auto; display: flex; align-items: center; gap: 5px; }

/* Deploy Button */
.deploy-container { margin-top: 10px; }
.hero-tile { background: radial-gradient(circle at center, #4e342e 0%, #261815 100%); border: 2px solid var(--gold-accent); width: 100%; }
.hero-tile .card-content { flex-direction: row; justify-content: space-between; text-align: left; padding: 20px 30px; }
.hero-tile .row-layout { width: 100%; display: flex; align-items: center; gap: 20px; }
.icon-stamp.small { width: 50px; height: 50px; font-size: 1.5rem; border: 2px double var(--gold-accent); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--gold-accent); background: rgba(0,0,0,0.3); }
.text-group { flex: 1; }
.hero-title.small { font-size: 1.5rem; margin: 0; color: #fff; line-height: 1; }
.arrow-indicator { font-size: 1.2rem; color: var(--gold-accent); animation: slideRight 1s infinite alternate; }
@keyframes slideRight { from { transform: translateX(0); } to { transform: translateX(5px); } }

/* --- MODALS --- */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); display: flex; justify-content: center; align-items: center; z-index: 2000; backdrop-filter: blur(4px); }
.modal-box { width: 90%; max-width: 500px; flex-direction: column; padding: 30px; background: #261815; border-color: var(--gold); }
.modal-header-txt { color: var(--gold); font-family: "Playfair Display", serif; margin: 0 0 20px 0; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; width: 100%; text-align: center; }

.spa-options-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; width: 100%; margin-bottom: 20px; }
.spa-item { padding: 15px; border-radius: 4px; display: flex; flex-direction: column; align-items: center; gap: 10px; border: 1px solid #5d4037; position: relative; overflow: hidden; }
.spa-item:hover { border-color: var(--gold-accent); background: rgba(255,215,0,0.05); }
.tile-icon.small { font-size: 1.5rem; margin: 0; }
.spa-item h4 { margin: 0; font-size: 1rem; color: #fff; }
.meta-row { display: flex; justify-content: space-between; width: 100%; font-size: 0.8rem; color: var(--text-dim); }
.gold-txt { color: #ffd700; }
.vip-ribbon { position: absolute; top: 0; right: 0; background: #b71c1c; color: #fff; font-size: 0.6rem; padding: 2px 8px; border-bottom-left-radius: 4px; font-weight: bold; }

.success-icon { font-size: 4rem; color: #66bb6a; margin-bottom: 15px; text-shadow: 0 0 20px rgba(102,187,106,0.4); }
.modal-msg { text-align: center; font-style: italic; color: var(--text-dim); margin-bottom: 25px; line-height: 1.5; }

@media (max-width: 600px) {
  .hero-tile .card-content { padding: 15px; }
  .icon-stamp.small { width: 40px; height: 40px; font-size: 1.2rem; }
  .hero-title.small { font-size: 1.2rem; }
  .header-right { flex-direction: column; align-items: flex-end; gap: 5px; }
}
</style> -->

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
              <div class="meta-row">
                <span><i class="far fa-clock"></i> 60s</span>
                <span class="gold-txt">50 Vàng</span>
              </div>
            </div>

            <div class="wood-card spa-item vip" @click="confirmRest('VIP')">
              <div class="vip-ribbon">THƯỢNG HẠNG</div>
              <div class="tile-icon small">
                <i class="fas fa-wine-bottle"></i>
              </div>
              <h4>Phòng Thượng Hạng</h4>
              <div class="meta-row">
                <span><i class="far fa-clock"></i> 10s</span>
                <span class="gold-txt">200 Vàng</span>
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
  const zodiacs = [
    "Tý",
    "Sửu",
    "Dần",
    "Mão",
    "Thìn",
    "Tỵ",
    "Ngọ",
    "Mùi",
    "Thân",
    "Dậu",
    "Tuất",
    "Hợi",
  ];
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
  return Math.min(
    (charStore.character.currentHp / charStore.character.maxHp) * 100,
    100,
  );
});

const energyPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min(
    (charStore.character.currentEnergy / charStore.character.maxEnergy) * 100,
    100,
  );
});

const isFull = computed(
  () => hpPercent.value >= 100 && energyPercent.value >= 100,
);

const canEnterMythic = computed(() => {
  if (!inventoryStore.items || inventoryStore.items.length === 0) return false;
  return inventoryStore.items.some(
    (item) => (item.enhancementLevel || 0) >= 30,
  );
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
    const duration = res.data.secondsRemaining || (type === "VIP" ? 10 : 60);
    const finishTime = Date.now() + duration * 1000;

    localStorage.setItem(STORAGE_KEY_END_TIME, finishTime);
    countdown.value = duration;
    isResting.value = true;
    await authStore.fetchProfile();
    startTimer();
  } catch (e) {
    const errorMsg = e.response?.data?.message || "Lỗi hệ thống";
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
    await charStore.fetchCharacter();
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
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif:ital,wght@0,400;0,700;1,400&family=Playfair+Display:wght@700;900&display=swap");

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

/* --- BACKGROUND SYSTEM --- */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: #261815;
}
.mountain-bg {
  position: absolute;
  inset: 0;
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
  max-width: 1000px;
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
  padding: 10px 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.server-tag {
  font-size: 0.75rem;
  color: var(--text-dim);
  display: flex;
  align-items: center;
  gap: 6px;
}
.status-orb {
  width: 6px;
  height: 6px;
  background: #66bb6a;
  border-radius: 50%;
  box-shadow: 0 0 5px #66bb6a;
}
.greet-txt {
  font-size: 0.8rem;
  color: var(--gold);
}
.char-name {
  font-family: "Playfair Display", serif;
  font-size: 1.8rem;
  margin: 0;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.6);
  line-height: 1;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
.wealth-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
  color: var(--gold-accent);
  font-size: 1rem;
}
.gold-icon {
  color: #ffd700;
  filter: drop-shadow(0 0 5px rgba(255, 215, 0, 0.5));
}
.weather-seal {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-dim);
}

/* --- CONTENT GRID --- */
.town-content {
  padding-top: 20px;
}
.ornament-divider {
  text-align: center;
  color: var(--gold);
  opacity: 0.6;
  margin-bottom: 20px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.ornament-divider .diamond {
  font-size: 0.7rem;
}

.hub-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 25px;
}

/* --- WOOD CARD STYLE --- */
.wood-card {
  position: relative;
  background: linear-gradient(
    135deg,
    var(--wood-card) 0%,
    var(--wood-base) 100%
  );
  border: 1px solid #6d4c41;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
  cursor: pointer;
  text-decoration: none;
  display: block;
}
.wood-card:hover:not(.locked):not(.resting-mode) {
  transform: translateY(-4px);
  border-color: var(--gold-accent);
  box-shadow:
    0 15px 30px rgba(0, 0, 0, 0.6),
    0 0 10px rgba(255, 215, 0, 0.1);
}
.card-content {
  padding: 25px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  z-index: 2;
  height: 100%;
  box-sizing: border-box;
}
.tile-icon {
  font-size: 2.2rem;
  color: var(--text-dim);
  margin-bottom: 15px;
  transition: 0.3s;
}
.wood-card:hover .tile-icon {
  color: var(--gold-accent);
  transform: scale(1.1);
  filter: drop-shadow(0 0 8px rgba(255, 215, 0, 0.4));
}

.tile-title {
  margin: 0;
  font-family: "Playfair Display", serif;
  font-size: 1.4rem;
  color: #fff;
  margin-bottom: 5px;
}
.tile-desc {
  font-size: 0.85rem;
  color: var(--gold);
  font-style: italic;
  margin-bottom: 20px;
  opacity: 0.8;
}

.corner-decor {
  position: absolute;
  width: 8px;
  height: 8px;
  border: 1px solid transparent;
  transition: 0.3s;
}
.top-right {
  top: 4px;
  right: 4px;
  border-top-color: rgba(255, 255, 255, 0.1);
  border-right-color: rgba(255, 255, 255, 0.1);
}
.bottom-left {
  bottom: 4px;
  left: 4px;
  border-bottom-color: rgba(255, 255, 255, 0.1);
  border-left-color: rgba(255, 255, 255, 0.1);
}
.wood-card:hover .corner-decor {
  width: 100%;
  height: 100%;
  border-color: var(--gold-accent);
  opacity: 0.3;
}

/* ========================================= */
/* 🔥 BUTTON STYLES (Đã update dạng Outline) */
/* ========================================= */
.wuxia-btn {
  font-family: "Noto Serif", serif;
  font-weight: bold;
  cursor: pointer;
  margin-top: auto;
  transition: all 0.3s ease;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 1px;
  display: inline-block;
  line-height: 1.2;
}

/* Kiểu Outline (Viền mảnh, bo tròn) */
.wuxia-btn.outline {
  background: transparent;
  border: 1px solid var(--text-dim);
  color: var(--text-dim);
  padding: 10px 30px;
  border-radius: 30px;
  width: auto;
}

/* Hover Effect cho nút Outline */
.wood-card:hover .wuxia-btn.outline:not(:disabled),
.wuxia-btn.outline:hover:not(:disabled) {
  border-color: var(--gold-accent);
  color: var(--gold-accent);
  background: rgba(255, 215, 0, 0.05);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.2);
}

.wuxia-btn.outline:active {
  transform: scale(0.95);
}
.wuxia-btn.outline:disabled {
  color: #4e342e;
  border-color: #3e2723;
  cursor: not-allowed;
}

/* Kiểu nút đóng / full width */
.wuxia-btn.close-btn {
  background: #3e2723;
  color: #d7ccc8;
  border: none;
  padding: 10px;
  width: 100%;
  margin-top: 15px;
  border-radius: 4px;
}

/* [FIX] Button ĐA TẠ - Style cứng để đảm bảo hiển thị rõ ràng */
.wuxia-btn.full-width {
  background-color: #ffd700 !important; /* Màu vàng kim bắt buộc */
  color: #3e2723 !important; /* Chữ màu nâu gỗ đậm */
  border: 2px solid #ffecb3; /* Viền sáng nhẹ */
  padding: 12px;
  width: 100%;
  border-radius: 8px; /* Bo góc mềm mại hơn */
  font-weight: 900; /* Chữ đậm hơn */
  font-family: "Noto Serif", serif;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-top: 20px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5); /* Đổ bóng để nút nổi lên */
  transition: all 0.2s ease;
  position: relative;
  z-index: 100;
}

/* Hiệu ứng khi di chuột vào */
.wuxia-btn.full-width:hover {
  background-color: #ffca28 !important; /* Vàng đậm hơn chút khi hover */
  transform: translateY(-2px); /* Nảy lên nhẹ */
  box-shadow: 0 6px 15px rgba(255, 215, 0, 0.3); /* Bóng vàng phát sáng */
}

.wuxia-btn.full-width:active {
  transform: translateY(1px); /* Nhấn xuống */
}

/* --- SPECIFIC CARD STYLES --- */
/* Inn Stats */
.stats-mini {
  width: 100%;
  margin-bottom: 25px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.75rem;
  color: var(--text-dim);
  font-weight: bold;
}
.bar-track {
  flex: 1;
  height: 4px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 2px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  transition: width 0.5s;
}
.bar-fill.hp {
  background: #ef5350;
}
.bar-fill.mp {
  background: #42a5f5;
}

/* Resting Animation */
.resting-mode {
  border-color: #66bb6a;
  background: #1b5e20;
  cursor: default;
}
.night-scene {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #a5d6a7;
}
.sleeping-anim {
  font-size: 2rem;
  margin-bottom: 10px;
  position: relative;
}
.zzz-particles span {
  position: absolute;
  font-size: 0.8rem;
  animation: floatUp 2s infinite;
  opacity: 0;
  top: 0;
  right: -10px;
}
.zzz-particles span:nth-child(2) {
  animation-delay: 0.6s;
  right: -15px;
}
.zzz-particles span:nth-child(3) {
  animation-delay: 1.2s;
  right: -5px;
}
@keyframes floatUp {
  0% {
    transform: translateY(0);
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    transform: translateY(-20px);
    opacity: 0;
  }
}
.count-txt {
  font-weight: bold;
  font-size: 1.2rem;
  color: #fff;
}

/* Locked Mythic */
.mythic-card.locked {
  filter: grayscale(1);
  opacity: 0.7;
  cursor: not-allowed;
}
.lock-msg {
  color: #ef9a9a;
  font-size: 0.85rem;
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* Deploy Button */
.deploy-container {
  margin-top: 10px;
}
.hero-tile {
  background: radial-gradient(circle at center, #4e342e 0%, #261815 100%);
  border: 2px solid var(--gold-accent);
  width: 100%;
}
.hero-tile .card-content {
  flex-direction: row;
  justify-content: space-between;
  text-align: left;
  padding: 20px 30px;
}
.hero-tile .row-layout {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 20px;
}

/* [MODIFIED] Ngôi đền đỏ tối, vòng tròn xám */
.icon-stamp.small {
  width: 50px;
  height: 50px;
  font-size: 1.5rem;
  border: 2px double #9e9e9e; /* Vòng tròn ngoài màu XÁM */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b0000; /* Ngôi đền bên trong màu ĐỎ TỐI */
  background: rgba(0, 0, 0, 0.2);
}

.text-group {
  flex: 1;
}

/* [MODIFIED] Chữ XUẤT THÀNH màu trắng */
.hero-title.small {
  font-size: 1.5rem;
  margin: 0;
  color: #ffffff; /* Màu TRẮNG */
  line-height: 1;
}
/* [MODIFIED] Chữ Hành Hiệp Trượng Nghĩa màu trắng */
.hero-sub {
  color: #ffffff; /* Màu TRẮNG */
  opacity: 0.9;
  margin-top: 4px;
}
.arrow-indicator {
  font-size: 1.2rem;
  color: var(--gold-accent);
  animation: slideRight 1s infinite alternate;
}
@keyframes slideRight {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(5px);
  }
}

/* --- MODALS --- */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

/* [FIX] Reset display flex cho modal box để tránh lỗi layout từ wood-panel */
.modal-box {
  width: 90%;
  max-width: 500px;
  display: flex; /* Bắt buộc flex */
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  background: #261815;
  border-color: var(--gold);
  z-index: 2001;
}

.modal-header-txt {
  color: var(--gold);
  font-family: "Playfair Display", serif;
  margin: 0 0 20px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 10px;
  width: 100%;
  text-align: center;
}

.spa-options-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  width: 100%;
  margin-bottom: 20px;
}
.spa-item {
  padding: 15px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  border: 1px solid #5d4037;
  position: relative;
  overflow: hidden;
}
.spa-item:hover {
  border-color: var(--gold-accent);
  background: rgba(255, 215, 0, 0.05);
}
.tile-icon.small {
  font-size: 1.5rem;
  margin: 0;
}
.spa-item h4 {
  margin: 0;
  font-size: 1rem;
  color: #fff;
}
.meta-row {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 0.8rem;
  color: var(--text-dim);
}
.gold-txt {
  color: #ffd700;
}
.vip-ribbon {
  position: absolute;
  top: 0;
  right: 0;
  background: #b71c1c;
  color: #fff;
  font-size: 0.6rem;
  padding: 2px 8px;
  border-bottom-left-radius: 4px;
  font-weight: bold;
}

.success-icon {
  font-size: 4rem;
  color: #66bb6a;
  margin-bottom: 15px;
  text-shadow: 0 0 20px rgba(102, 187, 106, 0.4);
}
.modal-msg {
  text-align: center;
  font-style: italic;
  color: var(--text-dim);
  margin-bottom: 25px;
  line-height: 1.5;
}

.fade-modal-enter-active,
.fade-modal-leave-active {
  transition: opacity 0.3s ease;
}

.fade-modal-enter-from,
.fade-modal-leave-to {
  opacity: 0;
}

@media (max-width: 600px) {
  .hero-tile .card-content {
    padding: 15px;
  }
  .icon-stamp.small {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }
  .hero-title.small {
    font-size: 1.2rem;
  }
  .header-right {
    flex-direction: column;
    align-items: flex-end;
    gap: 5px;
  }
}
</style>
