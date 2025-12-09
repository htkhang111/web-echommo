<template>
  <div class="page-container explore-page">
    <div class="explore-layout">
      <div class="center-zone">
        <div class="game-board">
          <div class="status-header">
            <div class="level-badge">
              <span>Lv.{{ charStore.character?.lv }}</span>
            </div>
            <div class="bars-container">
              <div class="stat-group">
                <div class="stat-row">
                  <span class="stat-icon">❤️</span>
                  <div class="progress-bg">
                    <div
                      class="progress-fill hp"
                      :style="{ width: charStore.hpPercent + '%' }"
                    ></div>
                    <span class="stat-text"
                      >{{ charStore.character?.hp }}/{{
                        charStore.character?.maxHp
                      }}</span
                    >
                  </div>
                </div>
                <div class="stat-row">
                  <span class="stat-icon">⚡</span>
                  <div class="progress-bg">
                    <div
                      class="progress-fill energy"
                      :style="{ width: charStore.energyPercent + '%' }"
                    ></div>
                    <span class="stat-text"
                      >{{ charStore.character?.energy }}/{{
                        charStore.character?.maxEnergy
                      }}</span
                    >
                  </div>
                </div>
              </div>
              <div class="exp-row">
                <div class="exp-bg">
                  <div
                    class="exp-fill"
                    :style="{ width: charStore.xpPercent + '%' }"
                  ></div>
                </div>
                <span class="exp-text"
                  >{{ charStore.xpPercent.toFixed(1) }}%</span
                >
              </div>
            </div>
          </div>

          <div class="stage-viewport">
            <div class="stage-background">
              <div
                class="actor player"
                :style="{
                  left: charStore.explorationState.playerPos + '%',
                  transform: `scaleX(${charStore.explorationState.moveDir})`,
                }"
              >
                <div class="avatar-circle">
                  <img :src="imgPlayer" class="avatar-img" />
                </div>
                <div class="actor-label">Bạn</div>
              </div>

              <div
                class="actor target"
                v-if="showTarget"
                :style="{
                  left:
                    charStore.explorationState.playerPos +
                    15 * charStore.explorationState.moveDir +
                    '%',
                }"
              >
                <div
                  class="avatar-target"
                  :class="{
                    'is-enemy': isEncounter,
                    'is-reward': !isEncounter,
                  }"
                >
                  <img
                    v-if="targetImage"
                    :src="targetImage"
                    class="avatar-img"
                  />
                  <div v-else class="text-3xl">🎁</div>
                </div>
                <div class="actor-label target-name">{{ targetName }}</div>
              </div>
            </div>
          </div>

          <div class="action-panel">
            <template v-if="!isEncounter">
              <button
                class="btn-action main-btn"
                @click="startExploration"
                :disabled="isMoving"
              >
                <div class="btn-content">
                  <i class="fas fa-walking"></i>
                  <span v-if="!isMoving">HÀNH TẨU</span>
                  <span v-else>ĐANG TÌM... ({{ countdown }}s)</span>
                </div>
              </button>
              <button
                class="btn-action sub-btn"
                @click="$router.push('/village')"
                :disabled="isMoving"
              >
                <div class="btn-content">
                  <i class="fas fa-home"></i><span>VỀ TRẠI</span>
                </div>
              </button>
            </template>
            <div v-else class="encounter-msg">⚠️ Đang tương tác...</div>
          </div>
        </div>

        <div class="chat-board">
          <ChatPanel />
        </div>
      </div>

      <div class="right-zone">
        <div class="log-panel">
          <div class="log-header"><i class="fas fa-scroll"></i> NHẬT KÝ</div>
          <div class="log-content custom-scroll">
            <div v-for="(log, index) in logs" :key="index" class="log-line">
              <span class="log-time">[{{ log.time }}]</span>
              <span class="log-msg" v-html="log.msg"></span>
            </div>
          </div>
        </div>

        <div class="quest-panel-wrapper">
          <QuestPanel />
        </div>
      </div>
    </div>

    <div v-if="isEncounter" class="encounter-modal">
      <div class="modal-card">
        <div class="modal-header">⚠️ CẢNH BÁO</div>
        <div class="modal-body">
          <div class="preview-box">
            <img :src="targetImage" class="enemy-preview-img" />
          </div>
          <p>
            Gặp <strong>{{ targetName }}</strong
            >!
          </p>
        </div>
        <div class="modal-footer">
          <button class="modal-btn flee" @click="flee">🏃 Bỏ Chạy</button>
          <button class="modal-btn fight" @click="goToBattle">
            ⚔️ CHIẾN ĐẤU
          </button>
        </div>
      </div>
    </div>

    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useCharacterStore } from "@/stores/characterStore"; // Đảm bảo đường dẫn đúng
import { useAuthStore } from "@/stores/authStore";
import { useBattleStore } from "@/stores/battleStore";
import { useRouter } from "vue-router";
import CaptchaModal from "@/components/CaptchaModal.vue";
import ChatPanel from "@/components/ChatPanel.vue";
import QuestPanel from "@/components/QuestPanel.vue";
import {
  getRandomEnemyData,
  getItemImage,
  getCurrentSkin,
} from "@/utils/assetHelper";

const charStore = useCharacterStore();
const authStore = useAuthStore();
const battleStore = useBattleStore();
const router = useRouter();
const captchaModal = ref(null);

const isMoving = ref(false);
const isEncounter = ref(false);
const showTarget = ref(false);
const countdown = ref(0);
const logs = ref([]);
const targetImage = ref("");
const targetName = ref("");

// Lấy ảnh nhân vật theo Skin
const imgPlayer = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return isMoving.value ? skin.sprites.run : skin.sprites.idle;
});

let moveInterval = null;

const getTime = () =>
  new Date().toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  });
const addLog = (msg) => logs.value.unshift({ time: getTime(), msg });

const startMovingJS = () => {
  if (moveInterval) clearInterval(moveInterval);
  moveInterval = setInterval(() => {
    charStore.explorationState.playerPos +=
      0.5 * charStore.explorationState.moveDir;
    if (charStore.explorationState.playerPos >= 80)
      charStore.explorationState.moveDir = -1;
    else if (charStore.explorationState.playerPos <= 10)
      charStore.explorationState.moveDir = 1;
  }, 16);
};

const startExploration = () => {
  if (isMoving.value) return;
  isMoving.value = true;
  showTarget.value = false;
  isEncounter.value = false;
  countdown.value = 2;
  startMovingJS();
  addLog("Bắt đầu hành tẩu...");
  const timer = setInterval(async () => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      await handleResult();
    }
  }, 1000);
};

// Xử lý kết quả khám phá
const handleResult = async () => {
  clearInterval(moveInterval);
  isMoving.value = false;

  // 1. Tỉ lệ 20% gặp Tài Nguyên -> Chuyển trang Gathering
  const eventChance = Math.random() * 100;
  if (eventChance < 20) {
    addLog(
      `<span style="color:#00e676; font-weight:bold;">🌿 Phát hiện khu vực tài nguyên! Đang tiến vào...</span>`,
    );
    setTimeout(() => {
      router.push("/gathering");
    }, 1000);
    return;
  }

  // 2. Tỉ lệ 80% còn lại: Gọi API Explore
  try {
    const res = await charStore.explore();

    if (res.type === "GOLD") {
      showTarget.value = true;
      targetName.value = "Túi Vàng";
      targetImage.value = getItemImage("GOLD");
      addLog(
        `<span style="color:#ffd700; font-weight:bold;">${res.message}</span>`,
      );
    } else if (res.type === "ENEMY") {
      isEncounter.value = true;
      showTarget.value = true;
      const rndEnemy = getRandomEnemyData();
      targetName.value = rndEnemy.name;
      targetImage.value = rndEnemy.img;
      battleStore.setEncounter(rndEnemy);
      addLog(
        `<span style="color:#ef5350; font-weight:bold;">⚠️ ${res.message}</span>`,
      );
    } else {
      addLog(`<span style="color:#aaa;">${res.message}</span>`);
    }
  } catch (e) {
    const msg = e.message || e;
    if (msg === "CAPTCHA" || msg === "CAPTCHA_REQUIRED")
      captchaModal.value.open();
    else addLog(`<span style="color:red">Lỗi: ${msg}</span>`);
  }
};

const goToBattle = () => router.push("/battle");
const flee = () => {
  isEncounter.value = false;
  showTarget.value = false;
  addLog("<span style='color:#a5d6a7'>Đã chạy thoát.</span>");
};

onMounted(() => charStore.fetchCharacter());
onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
.explore-page {
  padding: 10px;
  height: 100vh;
  box-sizing: border-box;
  overflow: hidden;
  color: #eee;
  font-family: "Noto Serif TC", serif;
  background-color: #1a1a1a;
}

.explore-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 15px;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.center-zone {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
  overflow: hidden;
}

.game-board {
  flex: 1;
  min-height: 300px;
  background: #261815;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
  position: relative;
}

.status-header {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.4);
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  gap: 12px;
}

.level-badge {
  background: linear-gradient(135deg, #ffd700, #f57f17);
  color: #000;
  font-weight: 900;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  white-space: nowrap;
}

.bars-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-group {
  display: flex;
  gap: 10px;
}

.stat-row {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-icon {
  font-size: 1em;
}

.progress-bg {
  flex: 1;
  height: 14px;
  background: #000;
  border: 1px solid #444;
  border-radius: 2px;
  position: relative;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
}
.progress-fill.hp {
  background: linear-gradient(to right, #c62828, #e53935);
}
.progress-fill.energy {
  background: linear-gradient(to right, #8a1c1c, #b71c1c);
}

.stat-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.65em;
  font-weight: bold;
  text-shadow: 1px 1px 0 #000;
}

.exp-row {
  position: relative;
  width: 100%;
  margin-top: 2px;
}
.exp-bg {
  height: 3px;
  background: #333;
}
.exp-fill {
  height: 100%;
  background: #00e676;
}
.exp-text {
  position: absolute;
  right: 0;
  top: -12px;
  font-size: 0.6em;
  color: #00e676;
}

.stage-viewport {
  flex: 1;
  min-height: 0;
  margin: 5px;
  border: 2px solid #3e2723;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}

.stage-background {
  width: 100%;
  height: 100%;
  background-image: url("@/assets/Background/b_doanhtrai.png");
  background-size: cover;
  background-position: center bottom;
  position: relative;
}

.actor {
  position: absolute;
  bottom: 25px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 140px;
  transition: left 0.1s linear;
  z-index: 10;
}

.avatar-circle,
.avatar-target {
  width: 96px;
  height: 96px;
  border: none;
  background: transparent;
  display: flex;
  justify-content: center;
  align-items: center;
  filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5));
}

.avatar-target.is-reward {
  width: 48px;
  height: 48px;
  margin-bottom: 15px;
  animation: floatCoin 2s infinite ease-in-out;
}
@keyframes floatCoin {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(1.2);
}

.actor-label {
  margin-top: 0px;
  background: rgba(0, 0, 0, 0.6);
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.9em;
  color: #fff;
  border: none;
  white-space: nowrap;
  font-weight: bold;
  z-index: 12;
}
.target-name {
  color: #ffeb3b;
}

.action-panel {
  height: 60px;
  background: #1a100e;
  border-top: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  padding: 0 15px;
}

.btn-action {
  border: none;
  border-radius: 6px;
  cursor: pointer;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Noto Serif TC";
  font-weight: bold;
  font-size: 0.9em;
  transition: 0.2s;
  box-shadow: 0 3px 0 rgba(0, 0, 0, 0.5);
}
.btn-action:active {
  transform: translateY(2px);
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.5);
}
.btn-action:disabled {
  background: #444;
  border: 1px solid #333;
  color: #888;
  cursor: not-allowed;
}

.main-btn {
  flex: 2;
  background: linear-gradient(to bottom, #4e342e, #3e2723);
  border: 1px solid #c5a059;
  color: #c5a059;
  box-shadow: 0 3px 0 #261815;
}
.sub-btn {
  flex: 1;
  background: #3e2723;
  border: 1px solid #5d4037;
  color: #d7ccc8;
}
.btn-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-board {
  height: 300px;
  background: rgba(0, 0, 0, 0.6);
  border: 2px solid #5d4037;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

/* RIGHT ZONE LAYOUT */
.right-zone {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
  gap: 15px;
}

.log-panel {
  flex: 0 0 40%;
  min-height: 150px;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.log-header {
  height: 36px;
  background: #3e2723;
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #ffd700;
  gap: 6px;
  font-size: 0.9em;
}

.log-content {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
  font-size: 0.85em;
  line-height: 1.4;
  background: #111;
}
.log-line {
  margin-bottom: 5px;
}
.log-time {
  font-style: italic;
  color: #888;
  margin-right: 5px;
}
.log-msg {
  color: #d7ccc8;
}
.quest-panel-wrapper {
  flex: 1; /* Chiếm hết phần còn lại */
  min-height: 150px;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  overflow: hidden;
  padding: 0;
  display: flex;
  flex-direction: column;
}

/* MODAL STYLES */
.encounter-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-card {
  width: 350px;
  background: #261815;
  border: 2px solid #b71c1c;
  border-radius: 12px;
  box-shadow: 0 0 30px rgba(183, 28, 28, 0.5);
  overflow: hidden;
  animation: zoomIn 0.2s;
}
.modal-header {
  background: #b71c1c;
  color: #fff;
  padding: 10px;
  text-align: center;
  font-weight: bold;
  font-size: 1.1em;
}
.modal-body {
  padding: 20px 15px;
  text-align: center;
  color: #eee;
}
.preview-box {
  width: 160px;
  height: 160px;
  margin: 0 auto 15px;
  border: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
}
.enemy-preview-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 0 10px rgba(255, 0, 0, 0.5));
}
.modal-footer {
  padding: 15px;
  background: #1a100e;
  display: flex;
  gap: 15px;
}
.modal-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  font-size: 1em;
}
.modal-btn.flee {
  background: #555;
  color: #ccc;
}
.modal-btn.fight {
  background: #d32f2f;
  color: #fff;
  animation: pulse 1.5s infinite;
}

@keyframes zoomIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

@media (max-width: 900px) {
  .explore-page {
    padding: 5px;
    height: auto;
    min-height: 100vh;
    overflow-y: auto;
  }
  .explore-layout {
    grid-template-columns: 1fr;
    gap: 10px;
    height: auto;
  }
  .center-zone {
    height: auto;
  }
  .game-board {
    height: 350px;
  }
  .chat-board {
    height: 300px;
  }
  .right-zone {
    height: 400px;
    min-height: 300px;
  }
}
</style>