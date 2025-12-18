<!-- <template>
  <div class="page-container explore-page">
    <div class="explore-layout">
      <div class="center-zone">
        <div class="game-board">
          <div class="status-header">
            <div class="level-badge">
              <span>Lv.{{ charStore.character?.level }}</span>
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
              </div>
            </div>
          </div>

          <div class="stage-viewport">
            <div
              class="stage-background"
              :style="{ backgroundImage: `url(${getMapBg()})` }"
            >
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
                class="btn-action map-btn"
                @click="showMapModal = true"
                :disabled="isMoving"
              >
                <div class="btn-content">
                  <span>🗺️ {{ currentMapName }}</span>
                </div>
              </button>
              <button
                class="btn-action main-btn"
                @click="startExploration"
                :disabled="isMoving"
              >
                <div class="btn-content">
                  <i class="fas fa-walking"></i
                  ><span v-if="!isMoving">HÀNH TẨU</span
                  ><span v-else>... ({{ countdown }}s)</span>
                </div>
              </button>
              <button
                class="btn-action sub-btn"
                @click="$router.push('/village')"
                :disabled="isMoving"
              >
                <i class="fas fa-home"></i>
              </button>
            </template>
            <div v-else class="encounter-msg">⚠️ Đang chiến đấu...</div>
          </div>
        </div>

        <div class="chat-board">
          <ChatPanel />
        </div>
      </div>

      <div class="right-zone">
        <div class="log-panel">
          <div class="log-header">NHẬT KÝ</div>
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

    <div
      v-if="showMapModal"
      class="modal-overlay"
      @click.self="showMapModal = false"
    >
      <div class="map-modal-card">
        <div class="map-header">CHỌN KHU VỰC</div>
        <div class="map-grid">
          <div
            v-for="map in maps"
            :key="map.id"
            class="map-item"
            :class="{
              active: currentMapId === map.id,
              locked: userLv < map.minLv,
            }"
            @click="selectMap(map)"
          >
            <div class="map-info">
              <div class="map-name">{{ map.name }}</div>
              <div class="map-lv">Lv.{{ map.minLv }}-{{ map.maxLv }}</div>
            </div>
            <div v-if="userLv < map.minLv" class="lock-icon">🔒</div>
          </div>
        </div>
        <button class="close-btn" @click="showMapModal = false">Đóng</button>
      </div>
    </div>

    <div v-if="isEncounter" class="encounter-modal">
      <div class="modal-card">
        <div class="modal-header">CẢNH BÁO</div>
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
          <button class="modal-btn flee" @click="flee">Bỏ Chạy</button
          ><button class="modal-btn fight" @click="goToBattle">
            CHIẾN ĐẤU
          </button>
        </div>
      </div>
    </div>
    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
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

const showMapModal = ref(false);
const currentMapId = ref("MAP_01");
const userLv = computed(() => charStore.character?.level || 1);
const maps = [
  { id: "MAP_01", name: "Đồng Bằng", minLv: 1, maxLv: 19 },
  { id: "MAP_02", name: "Rừng Rậm", minLv: 20, maxLv: 30 },
  { id: "MAP_03", name: "Sa Mạc", minLv: 30, maxLv: 40 },
  { id: "MAP_04", name: "Núi Cao", minLv: 40, maxLv: 50 },
  { id: "MAP_05", name: "Băng Đảo", minLv: 50, maxLv: 60 },
  { id: "MAP_06", name: "Đầm Lầy", minLv: 60, maxLv: 70 },
];
const currentMapName = computed(
  () => maps.find((m) => m.id === currentMapId.value)?.name || "Đồng Bằng",
);

const selectMap = (map) => {
  if (userLv.value < map.minLv) {
    addLog(`🔒 Cần Lv.${map.minLv} để vào ${map.name}`);
    return;
  }
  currentMapId.value = map.id;
  showMapModal.value = false;
  addLog(`Đã chọn: <b>${map.name}</b>`);
};

const getMapBg = () =>
  new URL(`../assets/Background/b_doanhtrai.png`, import.meta.url).href;
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
    // Map bé lại (từ 30-70)
    if (charStore.explorationState.playerPos >= 70)
      charStore.explorationState.moveDir = -1;
    else if (charStore.explorationState.playerPos <= 30)
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
  const timer = setInterval(async () => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      await handleResult();
    }
  }, 1000);
};

const handleResult = async () => {
  clearInterval(moveInterval);
  isMoving.value = false;

  // [LOGIC MỚI] 20% Cơ hội chuyển sang trang Khai thác (Gathering)
  const eventChance = Math.random() * 100;
  if (eventChance < 20) {
    addLog(
      `<span style="color:#00e676; font-weight:bold;">🌿 Phát hiện khu vực tài nguyên!</span>`,
    );
    setTimeout(() => {
      router.push("/gathering");
    }, 800);
    return;
  }

  try {
    const res = await charStore.explore({ mapId: currentMapId.value });
    if (res.type === "ITEM" && res.rewardName) {
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = getItemImage(res.rewardName) || getItemImage("GOLD");
      addLog(`<span style="color:#00e676;">${res.message}</span>`);
    } else if (res.type === "ENEMY") {
      isEncounter.value = true;
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = new URL(
        `../assets/enemy/idle_goblin.png`,
        import.meta.url,
      ).href;
      battleStore.setEncounter({
        name: res.rewardName,
        img: targetImage.value,
      });
      addLog(`<span style="color:#ef5350;">⚠️ ${res.message}</span>`);
    } else {
      addLog(`<span style="color:#aaa;">${res.message}</span>`);
    }
  } catch (e) {
    if (e.message === "CAPTCHA") captchaModal.value.open();
    else addLog(`<span style="color:red">Lỗi: ${e.message || e}</span>`);
  }
};

const goToBattle = () => router.push("/battle");
const flee = () => {
  isEncounter.value = false;
  showTarget.value = false;
  addLog("Đã chạy thoát.");
};

onMounted(() => charStore.fetchCharacter());
onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
.explore-page {
  padding: 10px;
  height: 100vh;
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
  gap: 10px;
  height: 100%;
  overflow: hidden;
}

.game-board {
  flex: 0 0 320px;
  background: #261815;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: relative;
}

.chat-board {
  flex: 1;
  min-height: 0;
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid #444;
  border-radius: 8px;
}

.status-header {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.4);
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  gap: 12px;
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

.stage-viewport {
  flex: 1;
  margin: 5px;
  border: 2px solid #3e2723;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}

.stage-background {
  width: 100%;
  height: 100%;
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
  display: flex;
  justify-content: center;
  align-items: center;
  filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5));
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(1.2);
}

.action-panel {
  height: 60px;
  background: #1a100e;
  border-top: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
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
  font-weight: bold;
  color: #fff;
}

.map-btn {
  flex: 1;
  background: #2c3e50;
  border: 1px solid #34495e;
}

.main-btn {
  flex: 2;
  background: linear-gradient(to bottom, #4e342e, #3e2723);
  border: 1px solid #c5a059;
  color: #c5a059;
}

.sub-btn {
  flex: 0.5;
  background: #3e2723;
  border: 1px solid #5d4037;
}

.right-zone {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
}

.log-panel {
  flex: 0 0 40%;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.log-content {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
  background: #111;
  font-size: 0.85em;
}

.quest-panel-wrapper {
  flex: 1;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  overflow: hidden;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-modal-card {
  width: 90%;
  max-width: 500px;
  background: #1a1a1a;
  border: 2px solid #d4af37;
  border-radius: 8px;
  padding: 15px;
  color: #fff;
}

.map-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 15px;
}

.map-item {
  background: #333;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #444;
  position: relative;
}

.map-item.active {
  border-color: #00e676;
  background: #1b5e20;
}

.map-item.locked {
  opacity: 0.6;
  cursor: not-allowed;
}

.lock-icon {
  position: absolute;
  right: 10px;
  top: 10px;
}

.close-btn {
  width: 100%;
  padding: 10px;
  background: #b71c1c;
  border: none;
  color: white;
  font-weight: bold;
  cursor: pointer;
}

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
}

.modal-header {
  background: #b71c1c;
  color: #fff;
  padding: 10px;
  text-align: center;
  font-weight: bold;
}

.modal-body {
  padding: 20px;
  text-align: center;
}

.modal-footer {
  padding: 15px;
  display: flex;
  gap: 15px;
}

.modal-btn {
  flex: 1;
  padding: 10px;
  cursor: pointer;
  font-weight: bold;
}

.modal-btn.flee {
  background: #555;
  color: #ccc;
}

.modal-btn.fight {
  background: #d32f2f;
  color: #fff;
}

@media (max-width: 900px) {
  .explore-layout {
    grid-template-columns: 1fr;
  }

  .right-zone {
    height: 400px;
  }
}
</style> -->

<template>
  <div class="page-container explore-page">
    <div class="explore-layout">
      <div class="center-zone">
        <div class="game-board">
          <div class="status-header">
            <div class="level-badge">
              <span>Lv.{{ charStore.character?.level }}</span>
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
                      >{{ charStore.character?.currentHp }}/{{
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
                      >{{ charStore.character?.currentEnergy }}/{{
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
              </div>
            </div>
          </div>

          <div class="stage-viewport">
            <div
              class="stage-background"
              :style="{ backgroundImage: `url(${getMapBg()})` }"
            >
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
                class="btn-action map-btn"
                @click="showMapModal = true"
                :disabled="isMoving"
              >
                <div class="btn-content">
                  <span>🗺️ {{ currentMapName }}</span>
                </div>
              </button>
              <button
                class="btn-action main-btn"
                @click="startExploration"
                :disabled="isMoving"
              >
                <div class="btn-content">
                  <i class="fas fa-walking"></i
                  ><span v-if="!isMoving">HÀNH TẨU</span
                  ><span v-else>... ({{ countdown }}s)</span>
                </div>
              </button>
              <button
                class="btn-action sub-btn"
                @click="$router.push('/village')"
                :disabled="isMoving"
              >
                <i class="fas fa-home"></i>
              </button>
            </template>
            <div v-else class="encounter-msg">⚠️ Đang chiến đấu...</div>
          </div>
        </div>

        <div class="chat-board">
          <ChatPanel />
        </div>
      </div>

      <div class="right-zone">
        <div class="log-panel">
          <div class="log-header">NHẬT KÝ</div>
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

    <div
      v-if="showMapModal"
      class="modal-overlay"
      @click.self="showMapModal = false"
    >
      <div class="map-modal-card">
        <div class="map-header">CHỌN KHU VỰC</div>
        <div class="map-grid">
          <div
            v-for="map in maps"
            :key="map.id"
            class="map-item"
            :class="{
              active: currentMapId === map.id,
              locked: userLv < map.minLv,
            }"
            @click="selectMap(map)"
          >
            <div class="map-info">
              <div class="map-name">{{ map.name }}</div>
              <div class="map-lv">Lv.{{ map.minLv }}-{{ map.maxLv }}</div>
            </div>
            <div v-if="userLv < map.minLv" class="lock-icon">🔒</div>
          </div>
        </div>
        <button class="close-btn" @click="showMapModal = false">Đóng</button>
      </div>
    </div>

    <div v-if="isEncounter" class="encounter-modal">
      <div class="modal-card">
        <div class="modal-header">CẢNH BÁO</div>
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
          <button class="modal-btn flee" @click="flee">Bỏ Chạy</button
          ><button class="modal-btn fight" @click="goToBattle">
            CHIẾN ĐẤU
          </button>
        </div>
      </div>
    </div>
    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useBattleStore } from "@/stores/battleStore";
import { useRouter } from "vue-router";
import CaptchaModal from "@/components/CaptchaModal.vue";
import ChatPanel from "@/components/ChatPanel.vue";
import QuestPanel from "@/components/QuestPanel.vue";
import {
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

const showMapModal = ref(false);
const currentMapId = ref("MAP_01");
const userLv = computed(() => charStore.character?.level || 1);
const maps = [
  { id: "MAP_01", name: "Đồng Bằng", minLv: 1, maxLv: 19 },
  { id: "MAP_02", name: "Rừng Rậm", minLv: 20, maxLv: 30 },
  { id: "MAP_03", name: "Sa Mạc", minLv: 30, maxLv: 40 },
  { id: "MAP_04", name: "Núi Cao", minLv: 40, maxLv: 50 },
  { id: "MAP_05", name: "Băng Đảo", minLv: 50, maxLv: 60 },
  { id: "MAP_06", name: "Đầm Lầy", minLv: 60, maxLv: 70 },
];
const currentMapName = computed(
  () => maps.find((m) => m.id === currentMapId.value)?.name || "Đồng Bằng",
);

const selectMap = (map) => {
  if (userLv.value < map.minLv) {
    addLog(`🔒 Cần Lv.${map.minLv} để vào ${map.name}`);
    return;
  }
  currentMapId.value = map.id;
  showMapModal.value = false;
  addLog(`Đã chọn: <b>${map.name}</b>`);
};

// [CHÚ Ý] Cần đảm bảo file này tồn tại hoặc sửa lại đường dẫn
const getMapBg = () =>
  new URL(`../assets/Background/b_doanhtrai.png`, import.meta.url).href;
  
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
    // Map bé lại (từ 30-70)
    if (charStore.explorationState.playerPos >= 70)
      charStore.explorationState.moveDir = -1;
    else if (charStore.explorationState.playerPos <= 30)
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
  const timer = setInterval(async () => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      await handleResult();
    }
  }, 1000);
};

const handleResult = async () => {
  clearInterval(moveInterval);
  isMoving.value = false;

  try {
    // Gọi API từ Server (Đã bao gồm random bên Server)
    const res = await charStore.explore({ mapId: currentMapId.value });

    // 1. Trường hợp tìm thấy TÀI NGUYÊN -> Chuyển trang
    if (res.type === "GATHERING") {
      addLog(
        `<span style="color:#00e676; font-weight:bold;">🌿 ${res.message}</span>`,
      );
      setTimeout(() => {
        router.push("/gathering");
      }, 500); // Chuyển trang nhanh hơn chút
      return;
    }

    // 2. Trường hợp nhặt được ITEM
    if (res.type === "ITEM" && res.rewardName) {
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = getItemImage(res.rewardName) || getItemImage("GOLD");
      addLog(`<span style="color:#00e676;">${res.message}</span>`);
    } 
    // 3. Trường hợp gặp QUÁI
    else if (res.type === "ENEMY") {
      isEncounter.value = true;
      showTarget.value = true;
      targetName.value = res.rewardName;
      // Dùng ảnh goblin mặc định nếu chưa có ảnh quái cụ thể
      targetImage.value = new URL(
        `../assets/enemy/idle_goblin.png`,
        import.meta.url,
      ).href;
      battleStore.setEncounter({
        name: res.rewardName,
        img: targetImage.value,
      });
      addLog(`<span style="color:#ef5350;">⚠️ ${res.message}</span>`);
    } 
    // 4. Trường hợp Text thường
    else {
      addLog(`<span style="color:#aaa;">${res.message}</span>`);
    }
  } catch (e) {
    if (e.message === "CAPTCHA") captchaModal.value.open();
    else addLog(`<span style="color:red">Lỗi: ${e.message || e}</span>`);
  }
};

const goToBattle = () => router.push("/battle");
const flee = () => {
  isEncounter.value = false;
  showTarget.value = false;
  addLog("Đã chạy thoát.");
};

onMounted(() => charStore.fetchCharacter());
onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
.explore-page {
  padding: 10px;
  height: 100vh;
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
  gap: 10px;
  height: 100%;
  overflow: hidden;
}

.game-board {
  flex: 0 0 320px;
  background: #261815;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: relative;
}

.chat-board {
  flex: 1;
  min-height: 0;
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid #444;
  border-radius: 8px;
}

.status-header {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.4);
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  gap: 12px;
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

.stage-viewport {
  flex: 1;
  margin: 5px;
  border: 2px solid #3e2723;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}

.stage-background {
  width: 100%;
  height: 100%;
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
  display: flex;
  justify-content: center;
  align-items: center;
  filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5));
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(1.2);
}

.action-panel {
  height: 60px;
  background: #1a100e;
  border-top: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
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
  font-weight: bold;
  color: #fff;
}

.map-btn {
  flex: 1;
  background: #2c3e50;
  border: 1px solid #34495e;
}

.main-btn {
  flex: 2;
  background: linear-gradient(to bottom, #4e342e, #3e2723);
  border: 1px solid #c5a059;
  color: #c5a059;
}

.sub-btn {
  flex: 0.5;
  background: #3e2723;
  border: 1px solid #5d4037;
}

.right-zone {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
}

.log-panel {
  flex: 0 0 40%;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.log-content {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
  background: #111;
  font-size: 0.85em;
}

.quest-panel-wrapper {
  flex: 1;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  overflow: hidden;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-modal-card {
  width: 90%;
  max-width: 500px;
  background: #1a1a1a;
  border: 2px solid #d4af37;
  border-radius: 8px;
  padding: 15px;
  color: #fff;
}

.map-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 15px;
}

.map-item {
  background: #333;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #444;
  position: relative;
}

.map-item.active {
  border-color: #00e676;
  background: #1b5e20;
}

.map-item.locked {
  opacity: 0.6;
  cursor: not-allowed;
}

.lock-icon {
  position: absolute;
  right: 10px;
  top: 10px;
}

.close-btn {
  width: 100%;
  padding: 10px;
  background: #b71c1c;
  border: none;
  color: white;
  font-weight: bold;
  cursor: pointer;
}

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
}

.modal-header {
  background: #b71c1c;
  color: #fff;
  padding: 10px;
  text-align: center;
  font-weight: bold;
}

.modal-body {
  padding: 20px;
  text-align: center;
}

.enemy-preview-img {
  width: 100px;
  height: 100px;
  object-fit: contain;
}

.modal-footer {
  padding: 15px;
  display: flex;
  gap: 15px;
}

.modal-btn {
  flex: 1;
  padding: 10px;
  cursor: pointer;
  font-weight: bold;
}

.modal-btn.flee {
  background: #555;
  color: #ccc;
}

.modal-btn.fight {
  background: #d32f2f;
  color: #fff;
}

/* Scrollbar */
.custom-scroll::-webkit-scrollbar {
  width: 4px;
}

.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
}

@media (max-width: 900px) {
  .explore-layout {
    grid-template-columns: 1fr;
  }

  .right-zone {
    height: 400px;
  }
}
</style>