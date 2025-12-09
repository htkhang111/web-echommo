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
        <div
          class="location-card inn-card"
          :class="{ 'resting-mode': isResting }"
        >
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
                      <div
                        class="progress-bar hp-bar"
                        :style="{ width: hpPercent + '%' }"
                      ></div>
                    </div>
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">NỘI</span>
                    <div class="progress-track">
                      <div
                        class="progress-bar mp-bar"
                        :style="{ width: energyPercent + '%' }"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="card-bot">
                <button
                  class="btn-action wuxia-btn"
                  @click="restAtInn"
                  :disabled="isLoading || isFull"
                >
                  <span class="btn-text">
                    <span v-if="isLoading">ĐANG GỌI TIỂU NHỊ...</span>
                    <span v-else-if="isFull">ĐÃ SUNG MÃN</span>
                    <span v-else>NGHỈ NGƠI (50 <i class="fas fa-coins text-gold"></i>)</span>
                  </span>
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
                  Đang điều tức... <br />
                  <small style="color: #bbb; font-size: 0.8em">
                    (HP: {{ Math.floor(hpPercent) }}% | MP: {{ Math.floor(energyPercent) }}%)
                  </small>
                </div>
              </div>
            </template>
          </div>
        </div>

        <router-link to="/forge" class="location-card forge-card">
          <div class="card-content">
            <div class="card-top">
              <div class="icon-circle"><i class="fas fa-hammer"></i></div>
              <h3 class="card-name">THẦN BINH CÁC</h3>
            </div>
            <div class="card-mid">
              <p class="desc">Rèn đúc binh khí, cường hóa trang bị.</p>
              <div class="forge-fire-anim">
                <i class="fas fa-fire"></i>
              </div>
            </div>
            <div class="card-bot">
              <div class="btn-action wuxia-btn outline">
                <span class="btn-text">VÀO LÒ RÈN</span>
              </div>
            </div>
          </div>
        </router-link>
      </div>

      <div class="deploy-container">
        <button @click="$router.push('/explore')" class="imperial-seal-btn">
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
      <div
        v-if="showRestModal"
        class="modal-overlay"
        @click.self="closeRestModal"
      >
        <div class="dark-scroll-modal">
          <div class="modal-border-top"></div>
          <div class="modal-body">
            <div class="modal-stamp"><i class="fas fa-check"></i></div>
            <h3 class="modal-title">HỒI PHỤC HOÀN TẤT</h3>
            <p class="modal-msg">
              Tiểu nhị: <br />
              <span class="quote">
                "Khách quan thần thái hồng hào, nội công sung mãn. Chúc ngài thượng lộ bình an!"
              </span>
            </p>
            <div class="modal-stats-tags">
              <span class="tag hp">Đầy Sinh Lực</span>
              <span class="tag mp">Đầy Nội Lực</span>
            </div>
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

// State
const isResting = ref(false);
const isLoading = ref(false); // Tránh spam nút click
const showRestModal = ref(false);
let restInterval = null;

// Computed Stats
const hpPercent = computed(() => {
  if (!charStore.character || !charStore.character.maxHp) return 0;
  return Math.min((charStore.character.hp / charStore.character.maxHp) * 100, 100);
});

const energyPercent = computed(() => {
  if (!charStore.character || !charStore.character.maxEnergy) return 0;
  return Math.min((charStore.character.energy / charStore.character.maxEnergy) * 100, 100);
});

// Kiểm tra xem đã đầy chưa (để disable nút)
const isFull = computed(() => {
  if (!charStore.character) return false;
  return (
    charStore.character.hp >= charStore.character.maxHp &&
    charStore.character.energy >= charStore.character.maxEnergy
  );
});

const closeRestModal = () => {
  showRestModal.value = false;
};

// Hàm dừng nghỉ ngơi an toàn
const stopResting = () => {
  if (restInterval) {
    clearInterval(restInterval);
    restInterval = null;
  }
  
  // Delay nhẹ để UI mượt hơn
  setTimeout(() => {
    isResting.value = false;
    showRestModal.value = true;
  }, 500);
};

const restAtInn = async () => {
  if (isResting.value || isLoading.value) return;

  if (isFull.value) {
      alert("Tiểu nhị: 'Khách quan đang rất khỏe mạnh, không cần nghỉ ngơi!'");
      return;
  }

  if ((authStore.wallet?.gold || 0) < 50) {
    alert("Tiểu nhị: 'Khách quan không đủ ngân lượng (50 Xu)!'");
    return;
  }

  isLoading.value = true;

  try {
    // 1. Gọi API bắt đầu nghỉ
    await axiosClient.post(`/game/rest?playerId=${authStore.user.userId}`);
    
    // 2. Cập nhật ví tiền ngay lập tức
    await authStore.fetchProfile();
    
    // 3. Chuyển sang chế độ nghỉ ngơi
    isResting.value = true;
    isLoading.value = false;

    // 4. Bắt đầu vòng lặp kiểm tra
    if (restInterval) clearInterval(restInterval);
    
    restInterval = setInterval(async () => {
      // Fetch data mới nhất
      await charStore.fetchCharacter();
      
      const char = charStore.character;
      if (!char) return;

      // Logic kiểm tra ĐÚNG: So sánh giá trị tuyệt đối
      const isHpFull = char.hp >= char.maxHp;
      const isEnergyFull = char.energy >= char.maxEnergy;

      if (isHpFull && isEnergyFull) {
        stopResting();
      }
    }, 1500); // Check mỗi 1.5s để đỡ lag server

  } catch (e) {
    isLoading.value = false;
    isResting.value = false;
    if (restInterval) clearInterval(restInterval);
    alert(e.response?.data?.message || "Có lỗi xảy ra khi nghỉ ngơi.");
  }
};

onMounted(() => {
  charStore.fetchCharacter();
  if (authStore.token) authStore.fetchProfile();
});

onUnmounted(() => {
  if (restInterval) clearInterval(restInterval);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- PALETTE --- */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --red-seal: #b71c1c;
  --ink-black: #000000;
  --overlay-dark: rgba(0, 0, 0, 0.7);
}

/* --- BASE LAYOUT --- */
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
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  background-position: center bottom;
  filter: sepia(30%) brightness(0.6) contrast(1.2);
  opacity: 0.8;
}
.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(62, 39, 35, 1) 0%, transparent 80%);
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

/* --- HEADER --- */
.hub-header {
  text-align: center;
  margin-bottom: 50px;
}

.title-plaque {
  display: inline-block;
  padding: 10px 40px;
  border-bottom: 3px double var(--gold);
  position: relative;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(0, 0, 0, 0.5),
    transparent
  );
}

.hub-title {
  font-size: 3rem;
  margin: 0;
  color: var(--text-light);
  text-transform: uppercase;
  letter-spacing: 6px;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.8), 0 0 20px rgba(255, 236, 179, 0.2);
  font-weight: 900;
}

.plaque-ornament {
  width: 8px;
  height: 8px;
  background: var(--red-seal);
  border: 1px solid var(--gold);
  transform: rotate(45deg);
  position: absolute;
  top: 50%;
  margin-top: -4px;
}
.left { left: 0; }
.right { right: 0; }

.hub-subtitle {
  margin-top: 10px;
  font-size: 1.1rem;
  color: var(--gold);
  font-style: italic;
  opacity: 0.9;
}
.decor-icon {
  font-size: 0.8em;
  color: var(--red-seal);
}

/* --- GRID SYSTEM --- */
.hub-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  width: 100%;
  margin-bottom: 40px;
}

/* --- CARD STYLES --- */
.location-card {
  background: var(--wood-dark);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
}

.location-card:hover:not(.resting-mode) {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.8), 0 0 15px rgba(255, 236, 179, 0.1);
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
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.card-name {
  margin: 0;
  font-size: 1.5rem;
  color: var(--gold);
  font-weight: 800;
  text-shadow: 1px 1px 2px #000;
}

.card-mid {
  flex: 1;
  margin-bottom: 20px;
}

.desc {
  color: #ccc;
  font-size: 0.95rem;
  font-style: italic;
  margin-bottom: 20px;
  line-height: 1.4;
}

/* Stats Bars */
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
.stat-label {
  font-size: 0.75rem;
  font-weight: bold;
  color: var(--gold);
  width: 35px;
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
.hp-bar { background: linear-gradient(90deg, #b71c1c, #e53935); }
.mp-bar { background: linear-gradient(90deg, #1565c0, #42a5f5); }

/* --- BUTTONS --- */
.wuxia-btn {
  width: 100%;
  padding: 12px;
  background: var(--red-seal);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  font-family: "Noto Serif TC";
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.wuxia-btn:hover:not(:disabled) {
  background: #d32f2f;
  box-shadow: 0 0 10px rgba(255, 0, 0, 0.3);
}
.wuxia-btn:disabled {
  background: #444;
  cursor: not-allowed;
  opacity: 0.7;
}
.wuxia-btn.outline {
  background: transparent;
  border: 2px solid var(--wood-light);
  color: var(--text-light);
}
.wuxia-btn.outline:hover {
  border-color: var(--gold);
  color: var(--gold);
}
.text-gold { color: var(--gold); }

/* --- RESTING MODE --- */
.resting-mode {
  border-color: #1a237e;
  box-shadow: 0 0 20px rgba(26, 35, 126, 0.4);
}
.night-scene {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 20px 0;
}
.moon-glow {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #fff59d;
  box-shadow: 0 0 30px rgba(255, 245, 157, 0.4);
  margin-bottom: 20px;
}
.sleeping-anim {
  font-size: 3rem;
  color: var(--text-light);
  position: relative;
  margin-bottom: 15px;
}
.zzz-particles span {
  position: absolute;
  font-size: 0.5em;
  color: var(--gold);
  animation: floatZ 2s infinite;
  opacity: 0;
  top: -10px;
  right: -10px;
}
.zzz-particles span:nth-child(2) { animation-delay: 0.5s; right: -20px; top: -20px; }
.zzz-particles span:nth-child(3) { animation-delay: 1s; right: -30px; top: -30px; }
@keyframes floatZ {
  0% { transform: translate(0, 0); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translate(10px, -20px); opacity: 0; }
}
.resting-text {
  color: #9fa8da;
  font-style: italic;
  font-size: 0.9rem;
}

/* --- FORGE ANIM --- */
.forge-fire-anim {
  text-align: center;
  font-size: 2.5rem;
  color: #ff5722;
  margin-top: 20px;
  filter: drop-shadow(0 0 10px #ff5722);
  animation: fire 1.5s infinite alternate;
}
@keyframes fire {
  from { opacity: 0.7; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1.1); }
}

/* --- DEPLOY BUTTON --- */
.deploy-container {
  width: 100%;
  max-width: 400px;
  margin-top: 30px;
}
.imperial-seal-btn {
  width: 100%;
  background: linear-gradient(to bottom, #b71c1c, #880e4f);
  border: 4px double var(--gold);
  border-radius: 4px;
  padding: 15px;
  cursor: pointer;
  transition: transform 0.2s;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.5);
}
.imperial-seal-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.6), 0 0 15px rgba(183, 28, 28, 0.5);
}
.seal-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}
.seal-icon {
  font-size: 2.5rem;
  color: var(--gold);
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}
.seal-text-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.seal-big {
  color: #fff;
  font-size: 1.8rem;
  font-weight: 900;
  letter-spacing: 2px;
  text-shadow: 1px 1px 2px #000;
}
.seal-small {
  color: var(--gold);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* --- MODAL --- */
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
  width: 90%;
  max-width: 450px;
  background: var(--wood-dark);
  border-left: 12px solid #2d1e1b;
  border-right: 12px solid #2d1e1b;
  position: relative;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
}
.modal-border-top, .modal-border-bot {
  height: 20px;
  background: #2d1e1b;
  position: relative;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.5);
}
.modal-border-top::after, .modal-border-bot::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 10%;
  right: 10%;
  height: 2px;
  background: #5d4037;
}
.modal-body {
  padding: 30px 20px;
  text-align: center;
  color: var(--text-light);
  border: 1px solid rgba(255, 255, 255, 0.05);
}
.modal-stamp {
  font-size: 3rem;
  color: #4caf50;
  border: 3px solid #4caf50;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  transform: rotate(-10deg);
  text-shadow: 0 0 10px rgba(76, 175, 80, 0.4);
  box-shadow: 0 0 10px rgba(76, 175, 80, 0.2);
}
.modal-title {
  color: var(--gold);
  font-size: 1.4rem;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 236, 179, 0.3);
  padding-bottom: 10px;
  display: inline-block;
}
.modal-msg {
  font-size: 1rem;
  line-height: 1.6;
  color: #ccc;
  margin-bottom: 25px;
}
.quote {
  font-style: italic;
  color: #fff;
  display: block;
  margin-top: 5px;
}
.modal-stats-tags {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 25px;
}
.tag {
  font-size: 0.8rem;
  padding: 5px 10px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}
.tag.hp { color: #ef5350; border-color: #ef5350; }
.tag.mp { color: #42a5f5; border-color: #42a5f5; }
.btn-confirm {
  background: var(--wood-light);
  color: var(--gold);
  border: 2px solid var(--gold);
  padding: 10px 40px;
  font-family: inherit;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
}
.btn-confirm:hover {
  background: var(--gold);
  color: var(--wood-dark);
}
.fade-modal-enter-active, .fade-modal-leave-active { transition: opacity 0.3s ease; }
.fade-modal-enter-from, .fade-modal-leave-to { opacity: 0; }
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
        <div
          class="location-card inn-card"
          :class="{ 'resting-mode': isResting }"
        >
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
                      <div
                        class="progress-bar hp-bar"
                        :style="{ width: hpPercent + '%' }"
                      ></div>
                    </div>
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">NỘI</span>
                    <div class="progress-track">
                      <div
                        class="progress-bar mp-bar"
                        :style="{ width: energyPercent + '%' }"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="card-bot">
                <button
                  class="btn-action wuxia-btn"
                  @click="restAtInn"
                  :disabled="isLoading || isFull"
                >
                  <span class="btn-text">
                    <span v-if="isLoading">ĐANG GỌI TIỂU NHỊ...</span>
                    <span v-else-if="isFull">ĐÃ SUNG MÃN</span>
                    <span v-else>NGHỈ NGƠI (50 <i class="fas fa-coins text-gold"></i>)</span>
                  </span>
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
                  Đang điều tức... <br />
                  <small style="color: #bbb; font-size: 0.8em">
                    (HP: {{ Math.floor(hpPercent) }}% | MP: {{ Math.floor(energyPercent) }}%)
                  </small>
                </div>
              </div>
            </template>
          </div>
        </div>

        <router-link to="/forge" class="location-card forge-card">
          <div class="card-content">
            <div class="card-top">
              <div class="icon-circle"><i class="fas fa-hammer"></i></div>
              <h3 class="card-name">THẦN BINH CÁC</h3>
            </div>
            <div class="card-mid">
              <p class="desc">Rèn đúc binh khí, cường hóa trang bị.</p>
              <div class="forge-fire-anim">
                <i class="fas fa-fire"></i>
              </div>
            </div>
            <div class="card-bot">
              <div class="btn-action wuxia-btn outline">
                <span class="btn-text">VÀO LÒ RÈN</span>
              </div>
            </div>
          </div>
        </router-link>
      </div>

      <div class="deploy-container">
        <button @click="$router.push('/explore')" class="imperial-seal-btn">
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
      <div
        v-if="showRestModal"
        class="modal-overlay"
        @click.self="closeRestModal"
      >
        <div class="dark-scroll-modal">
          <div class="modal-border-top"></div>
          <div class="modal-body">
            <div class="modal-stamp"><i class="fas fa-check"></i></div>
            <h3 class="modal-title">HỒI PHỤC HOÀN TẤT</h3>
            <p class="modal-msg">
              Tiểu nhị: <br />
              <span class="quote">
                "Khách quan thần thái hồng hào, nội công sung mãn. Chúc ngài thượng lộ bình an!"
              </span>
            </p>
            <div class="modal-stats-tags">
              <span class="tag hp">Đầy Sinh Lực</span>
              <span class="tag mp">Đầy Nội Lực</span>
            </div>
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

// State
const isResting = ref(false);
const isLoading = ref(false);
const showRestModal = ref(false);
let restInterval = null;

// Computed Stats
const hpPercent = computed(() => {
  if (!charStore.character || !charStore.character.maxHp) return 0;
  return Math.min((charStore.character.hp / charStore.character.maxHp) * 100, 100);
});

const energyPercent = computed(() => {
  if (!charStore.character || !charStore.character.maxEnergy) return 0;
  return Math.min((charStore.character.energy / charStore.character.maxEnergy) * 100, 100);
});

const isFull = computed(() => {
  if (!charStore.character) return false;
  return (
    charStore.character.hp >= charStore.character.maxHp &&
    charStore.character.energy >= charStore.character.maxEnergy
  );
});

const closeRestModal = () => {
  showRestModal.value = false;
};

const stopResting = () => {
  if (restInterval) {
    clearInterval(restInterval);
    restInterval = null;
  }
  
  setTimeout(() => {
    isResting.value = false;
    showRestModal.value = true;
  }, 500);
};

// --- LOGIC GỌI SPA CONTROLLER ---
const restAtInn = async () => {
  if (isResting.value || isLoading.value) return;

  // Check đầy máu
  if (isFull.value) {
      alert("Tiểu nhị: 'Khách quan đang rất khỏe mạnh, không cần nghỉ ngơi!'");
      return;
  }

  // Check tiền sơ bộ (50 xu cho gói STANDARD)
  if ((authStore.wallet?.gold || 0) < 50) {
    alert("Tiểu nhị: 'Khách quan không đủ ngân lượng (Cần 50 Vàng)!'");
    return;
  }

  isLoading.value = true;

  try {
    // [QUAN TRỌNG] Gửi tham số packageType khớp với Enum SpaPackage
    // STANDARD: Vé Thường (50 Vàng, hồi 50%)
    await axiosClient.post('/spa/use', null, { 
        params: { packageType: 'STANDARD' } 
    });
    
    // Cập nhật ví tiền
    await authStore.fetchProfile();
    
    // Hiệu ứng Visual
    isResting.value = true;

    // Vòng lặp check máu để dừng hiệu ứng ngủ
    if (restInterval) clearInterval(restInterval);
    
    restInterval = setInterval(async () => {
      await charStore.fetchCharacter();
      const char = charStore.character;
      
      // Dừng khi đầy máu (hoặc khi user bấm nút khác, tùy logic game)
      // Vì gói STANDARD chỉ hồi 50%, có thể nó sẽ không đầy hẳn,
      // nhưng ta cứ để hiệu ứng chạy đến khi API trả về xong.
      if (char && char.hp >= char.maxHp && char.energy >= char.maxEnergy) {
        stopResting();
      }
    }, 1500); 

  } catch (e) {
    isResting.value = false;
    if (restInterval) clearInterval(restInterval);
    const msg = e.response?.data?.message || "Có lỗi xảy ra.";
    alert(msg);
  } finally {
    isLoading.value = false;
  }
};
onMounted(() => {
  charStore.fetchCharacter();
  if (authStore.token) authStore.fetchProfile();
});

onUnmounted(() => {
  if (restInterval) clearInterval(restInterval);
});
</script>

<style scoped>
/* Giữ nguyên Style của bạn - Nó rất đẹp! */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- PALETTE --- */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --red-seal: #b71c1c;
  --ink-black: #000000;
  --overlay-dark: rgba(0, 0, 0, 0.7);
}

/* --- BASE LAYOUT --- */
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
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  background-position: center bottom;
  filter: sepia(30%) brightness(0.6) contrast(1.2);
  opacity: 0.8;
}
.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(62, 39, 35, 1) 0%, transparent 80%);
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

/* --- HEADER --- */
.hub-header {
  text-align: center;
  margin-bottom: 50px;
}

.title-plaque {
  display: inline-block;
  padding: 10px 40px;
  border-bottom: 3px double var(--gold);
  position: relative;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(0, 0, 0, 0.5),
    transparent
  );
}

.hub-title {
  font-size: 3rem;
  margin: 0;
  color: var(--text-light);
  text-transform: uppercase;
  letter-spacing: 6px;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.8), 0 0 20px rgba(255, 236, 179, 0.2);
  font-weight: 900;
}

.plaque-ornament {
  width: 8px;
  height: 8px;
  background: var(--red-seal);
  border: 1px solid var(--gold);
  transform: rotate(45deg);
  position: absolute;
  top: 50%;
  margin-top: -4px;
}
.left { left: 0; }
.right { right: 0; }

.hub-subtitle {
  margin-top: 10px;
  font-size: 1.1rem;
  color: var(--gold);
  font-style: italic;
  opacity: 0.9;
}
.decor-icon {
  font-size: 0.8em;
  color: var(--red-seal);
}

/* --- GRID SYSTEM --- */
.hub-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  width: 100%;
  margin-bottom: 40px;
}

/* --- CARD STYLES --- */
.location-card {
  background: var(--wood-dark);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  overflow: hidden;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
}

.location-card:hover:not(.resting-mode) {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.8), 0 0 15px rgba(255, 236, 179, 0.1);
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
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.card-name {
  margin: 0;
  font-size: 1.5rem;
  color: var(--gold);
  font-weight: 800;
  text-shadow: 1px 1px 2px #000;
}

.card-mid {
  flex: 1;
  margin-bottom: 20px;
}

.desc {
  color: #ccc;
  font-size: 0.95rem;
  font-style: italic;
  margin-bottom: 20px;
  line-height: 1.4;
}

/* Stats Bars */
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
.stat-label {
  font-size: 0.75rem;
  font-weight: bold;
  color: var(--gold);
  width: 35px;
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
.hp-bar { background: linear-gradient(90deg, #b71c1c, #e53935); }
.mp-bar { background: linear-gradient(90deg, #1565c0, #42a5f5); }

/* --- BUTTONS --- */
.wuxia-btn {
  width: 100%;
  padding: 12px;
  background: var(--red-seal);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  font-family: "Noto Serif TC";
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.wuxia-btn:hover:not(:disabled) {
  background: #d32f2f;
  box-shadow: 0 0 10px rgba(255, 0, 0, 0.3);
}
.wuxia-btn:disabled {
  background: #444;
  cursor: not-allowed;
  opacity: 0.7;
}
.wuxia-btn.outline {
  background: transparent;
  border: 2px solid var(--wood-light);
  color: var(--text-light);
}
.wuxia-btn.outline:hover {
  border-color: var(--gold);
  color: var(--gold);
}
.text-gold { color: var(--gold); }

/* --- RESTING MODE --- */
.resting-mode {
  border-color: #1a237e;
  box-shadow: 0 0 20px rgba(26, 35, 126, 0.4);
}
.night-scene {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 20px 0;
}
.moon-glow {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #fff59d;
  box-shadow: 0 0 30px rgba(255, 245, 157, 0.4);
  margin-bottom: 20px;
}
.sleeping-anim {
  font-size: 3rem;
  color: var(--text-light);
  position: relative;
  margin-bottom: 15px;
}
.zzz-particles span {
  position: absolute;
  font-size: 0.5em;
  color: var(--gold);
  animation: floatZ 2s infinite;
  opacity: 0;
  top: -10px;
  right: -10px;
}
.zzz-particles span:nth-child(2) { animation-delay: 0.5s; right: -20px; top: -20px; }
.zzz-particles span:nth-child(3) { animation-delay: 1s; right: -30px; top: -30px; }
@keyframes floatZ {
  0% { transform: translate(0, 0); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translate(10px, -20px); opacity: 0; }
}
.resting-text {
  color: #9fa8da;
  font-style: italic;
  font-size: 0.9rem;
}

/* --- FORGE ANIM --- */
.forge-fire-anim {
  text-align: center;
  font-size: 2.5rem;
  color: #ff5722;
  margin-top: 20px;
  filter: drop-shadow(0 0 10px #ff5722);
  animation: fire 1.5s infinite alternate;
}
@keyframes fire {
  from { opacity: 0.7; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1.1); }
}

/* --- DEPLOY BUTTON --- */
.deploy-container {
  width: 100%;
  max-width: 400px;
  margin-top: 30px;
}
.imperial-seal-btn {
  width: 100%;
  background: linear-gradient(to bottom, #b71c1c, #880e4f);
  border: 4px double var(--gold);
  border-radius: 4px;
  padding: 15px;
  cursor: pointer;
  transition: transform 0.2s;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.5);
}
.imperial-seal-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.6), 0 0 15px rgba(183, 28, 28, 0.5);
}
.seal-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}
.seal-icon {
  font-size: 2.5rem;
  color: var(--gold);
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}
.seal-text-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.seal-big {
  color: #fff;
  font-size: 1.8rem;
  font-weight: 900;
  letter-spacing: 2px;
  text-shadow: 1px 1px 2px #000;
}
.seal-small {
  color: var(--gold);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* --- MODAL --- */
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
  width: 90%;
  max-width: 450px;
  background: var(--wood-dark);
  border-left: 12px solid #2d1e1b;
  border-right: 12px solid #2d1e1b;
  position: relative;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
}
.modal-border-top, .modal-border-bot {
  height: 20px;
  background: #2d1e1b;
  position: relative;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.5);
}
.modal-border-top::after, .modal-border-bot::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 10%;
  right: 10%;
  height: 2px;
  background: #5d4037;
}
.modal-body {
  padding: 30px 20px;
  text-align: center;
  color: var(--text-light);
  border: 1px solid rgba(255, 255, 255, 0.05);
}
.modal-stamp {
  font-size: 3rem;
  color: #4caf50;
  border: 3px solid #4caf50;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  transform: rotate(-10deg);
  text-shadow: 0 0 10px rgba(76, 175, 80, 0.4);
  box-shadow: 0 0 10px rgba(76, 175, 80, 0.2);
}
.modal-title {
  color: var(--gold);
  font-size: 1.4rem;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 236, 179, 0.3);
  padding-bottom: 10px;
  display: inline-block;
}
.modal-msg {
  font-size: 1rem;
  line-height: 1.6;
  color: #ccc;
  margin-bottom: 25px;
}
.quote {
  font-style: italic;
  color: #fff;
  display: block;
  margin-top: 5px;
}
.modal-stats-tags {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 25px;
}
.tag {
  font-size: 0.8rem;
  padding: 5px 10px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}
.tag.hp { color: #ef5350; border-color: #ef5350; }
.tag.mp { color: #42a5f5; border-color: #42a5f5; }
.btn-confirm {
  background: var(--wood-light);
  color: var(--gold);
  border: 2px solid var(--gold);
  padding: 10px 40px;
  font-family: inherit;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
}
.btn-confirm:hover {
  background: var(--gold);
  color: var(--wood-dark);
}
.fade-modal-enter-active, .fade-modal-leave-active { transition: opacity 0.3s ease; }
.fade-modal-enter-from, .fade-modal-leave-to { opacity: 0; }
</style>