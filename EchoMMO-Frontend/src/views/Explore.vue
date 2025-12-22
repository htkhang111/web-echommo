<template>
  <div class="page-container explore-page">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${pageBgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

    <div class="explore-layout">
      <div class="center-zone">
        <div class="game-board">
          <div class="status-header">
            <div class="level-badge"><span>Lv.{{ charStore.character?.level || 1 }}</span></div>
            <div class="bars-container">
              <div class="stat-group">
                <div class="stat-row">
                  <span class="stat-icon">❤️</span>
                  <div class="progress-bg">
                    <div class="progress-fill hp" :style="{ width: charStore.hpPercent + '%' }"></div>
                    <span class="stat-text">{{ charStore.character?.currentHp }}/{{ charStore.character?.maxHp }}</span>
                  </div>
                </div>
                <div class="stat-row">
                  <span class="stat-icon">⚡</span>
                  <div class="progress-bg">
                    <div class="progress-fill energy" :style="{ width: charStore.energyPercent + '%' }"></div>
                    <span class="stat-text">{{ charStore.character?.currentEnergy }}/{{ charStore.character?.maxEnergy }}</span>
                  </div>
                </div>
              </div>
              <div class="exp-row">
                <div class="exp-bg"><div class="exp-fill" :style="{ width: charStore.xpPercent + '%' }"></div></div>
              </div>
            </div>
          </div>

          <div class="stage-viewport">
            <div class="stage-background" :style="{ backgroundImage: `url(${getMapBg})` }">
              <div class="actor player" :style="{ left: charStore.explorationState.playerPos + '%', transform: `scaleX(${charStore.explorationState.moveDir})` }">
                <div class="avatar-circle"><img :src="imgPlayer" class="avatar-img" /></div>
                <div class="actor-label">Bạn</div>
              </div>
              </div>
          </div>

          <div class="action-panel">
            <button class="btn-action map-btn" @click="showMapModal = true" :disabled="isMoving">
              <div class="btn-content"><span>🗺️ {{ currentMapName }}</span></div>
            </button>
            <button class="btn-action main-btn" @click="startExploration" :disabled="isMoving">
              <div class="btn-content">
                <i class="fas fa-walking"></i>
                <span v-if="!isMoving">HÀNH TẨU</span>
                <span v-else>... ({{ countdown }}s)</span>
              </div>
            </button>
            <button class="btn-action sub-btn" @click="$router.push('/village')" :disabled="isMoving">
              <i class="fas fa-home"></i>
            </button>
          </div>
        </div>

        <div class="chat-board"><ChatPanel /></div>
      </div>

      <div class="right-zone">
        <div class="log-panel">
          <div class="log-header">NHẬT KÝ HÀNH TRÌNH</div>
          <div class="log-content custom-scroll">
            <div v-for="(log, index) in logs" :key="index" class="log-entry" :class="log.type">
              <div class="log-icon-col">
                <div v-if="log.image" class="log-thumb-box">
                  <img :src="log.image" class="log-thumb-img" />
                </div>
                <span v-else class="log-time-text">{{ log.time }}</span>
              </div>
              
              <div class="log-body-col">
                <div class="log-message" v-html="log.msg"></div>
                
                <div v-if="log.action" class="log-action">
                   <button class="btn-small-action" @click="log.action.handler">
                     {{ log.action.label }}
                   </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="quest-panel-wrapper"><QuestPanel /></div>
      </div>
    </div>

    <div v-if="showMapModal" class="modal-overlay" @click.self="showMapModal = false">
      <div class="map-modal-card">
        <div class="map-header">CHỌN KHU VỰC</div>
        <div class="map-grid">
          <div v-for="map in maps" :key="map.id" class="map-item" :class="{ active: currentMapId === map.id, locked: userLv < map.minLv }" @click="selectMap(map)">
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
    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, onActivated, watch } from "vue";
import { useRouter } from "vue-router";
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useBattleStore } from "@/stores/battleStore";
import { useQuestStore } from "@/stores/questStore";
import { useChatStore } from "@/stores/chatStore";
import axiosClient from "@/api/axiosClient";
import { getItemImage, getCurrentSkin, getAssetUrl, getEnemyImage } from "@/utils/assetHelper";

import CaptchaModal from "@/components/CaptchaModal.vue";
import ChatPanel from "@/components/ChatPanel.vue";
import QuestPanel from "@/components/QuestPanel.vue";

const charStore = useCharacterStore();
const authStore = useAuthStore();
const battleStore = useBattleStore();
const questStore = useQuestStore();
const chatStore = useChatStore();
const router = useRouter();
const captchaModal = ref(null);

const pageBgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);
const updateDayNight = () => { isNight.value = new Date().getHours() >= 18 || new Date().getHours() < 6; };

const isMoving = ref(false);
const countdown = ref(0);
const logs = ref([]); 

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
const currentMapName = computed(() => maps.find((m) => m.id === currentMapId.value)?.name || "Đồng Bằng");

watch(() => charStore.character, (newChar) => {
    if (newChar && newChar.currentMapId) {
      const savedMap = maps.find((m) => m.id === newChar.currentMapId);
      if (savedMap) currentMapId.value = newChar.currentMapId;
    }
  }, { immediate: true });

const selectMap = (map) => {
  if (userLv.value < map.minLv) { addLog(`🔒 Cần Lv.${map.minLv} để vào ${map.name}`, "TEXT"); return; }
  currentMapId.value = map.id;
  showMapModal.value = false;
  addLog(`Đã chọn: <b>${map.name}</b>`, "TEXT");
};

const getMapBg = computed(() => getAssetUrl("b_doanhtrai.png"));
const imgPlayer = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return isMoving.value ? skin.sprites.run : skin.sprites.idle;
});

// --- LOG SYSTEM (Updated) ---
const getTime = () => new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });

/**
 * Thêm một dòng log mới.
 * @param msg HTML message
 * @param type Loại log: 'TEXT', 'ENEMY', 'ITEM', 'GATHER'
 * @param image URL ảnh (nếu có)
 * @param action Object chứa { label, handler } (nếu có nút bấm)
 */
const addLogEntry = (msg, type = "TEXT", image = null, action = null) => {
    logs.value.unshift({
        time: getTime(),
        msg,
        type,
        image,
        action
    });
    // Giới hạn 50 dòng log
    if (logs.value.length > 50) logs.value.pop();
};

// Wrapper để tương thích code cũ nếu có gọi addLog
const addLog = (msg) => addLogEntry(msg, "TEXT");

let moveInterval = null;
const startMovingJS = () => {
  if (moveInterval) clearInterval(moveInterval);
  moveInterval = setInterval(() => {
    charStore.explorationState.playerPos += 0.5 * charStore.explorationState.moveDir;
    if (charStore.explorationState.playerPos >= 70) charStore.explorationState.moveDir = -1;
    else if (charStore.explorationState.playerPos <= 30) charStore.explorationState.moveDir = 1;
  }, 16);
};

const broadcastJoinMessage = async () => {
  const username = authStore.user?.username || "Đạo hữu";
  try {
    // chatStore.addMessage(...) // Tạm tắt để không spam
  } catch (e) {}
};

const startExploration = () => {
  if (isMoving.value) return;
  isMoving.value = true;
  countdown.value = 2; // Đếm ngược 2s
  startMovingJS();

  const timer = setInterval(async () => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      await handleResult();
    }
  }, 1000);
};

// --- XỬ LÝ KẾT QUẢ "PASSIVE" ---
const handleResult = async () => {
  clearInterval(moveInterval);
  isMoving.value = false;

  try {
    const res = await charStore.explore({ mapId: currentMapId.value });
    await charStore.fetchCharacter();

    // 1. TÀI NGUYÊN (GATHERING)
    if (res.type === "GATHERING") {
      const img = getItemImage(res.rewardName); 
      addLogEntry(
        `<span style="color:#00e676;">${res.message}</span>`,
        "GATHER",
        img,
        {
          label: "⛏️ Khai thác",
          handler: () => router.push("/gathering")
        }
      );
      return;
    }

    // 2. VẬT PHẨM (ITEM) - Nhặt luôn
    if (res.type === "ITEM" && res.rewardName) {
      const img = getItemImage(res.rewardName) || getItemImage("GOLD");
      let msg = `<span style="color:#00e676;">${res.message}</span>`;
      
      // Quest Check
      if (questStore.checkQuestCompletion) {
        const rGold = questStore.checkQuestCompletion(res.rewardName);
        if (rGold > 0) {
            msg += `<br><span style="color:#ffd700;">💰 Quest: +${rGold} Vàng</span>`;
            if(charStore.character) charStore.character.gold += rGold;
        }
      }
      addLogEntry(msg, "ITEM", img);
    } 
    // 3. QUÁI VẬT (ENEMY) - Hiện nút Chiến đấu
    else if (res.type === "ENEMY") {
      const img = getEnemyImage(res.rewardName);
      
      // Lưu thông tin quái để Battle Store biết
      battleStore.setEncounter({ name: res.rewardName, img: img });

      addLogEntry(
        `<span style="color:#ef5350;">⚠️ ${res.message}</span>`,
        "ENEMY",
        img,
        {
          label: "⚔️ Chiến đấu",
          handler: () => router.push("/battle")
        }
      );
    } 
    // 4. KHÁC (TEXT)
    else {
      addLogEntry(`<span style="color:#aaa;">${res.message}</span>`, "TEXT");
    }

  } catch (e) {
    if (e.message === "CAPTCHA") captchaModal.value.open();
    else addLogEntry(`<span style="color:red">Lỗi: ${e.message}</span>`, "TEXT");
  }
};

onMounted(() => {
  charStore.fetchCharacter();
  if(questStore.fetchQuests) questStore.fetchQuests();
  updateDayNight();
  broadcastJoinMessage();
});

onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
/* Basic Layout */
.explore-page { background: transparent; padding: 10px; height: 100vh; overflow: hidden; color: #eee; font-family: "Noto Serif TC", serif; position: relative; }
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; opacity: 0.6; filter: sepia(10%) contrast(1.1); }
.wood-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); mix-blend-mode: multiply; }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle, transparent 60%, #1a100d 100%); }
.explore-layout { display: grid; grid-template-columns: 1fr 350px; gap: 15px; height: 100%; max-width: 1400px; margin: 0 auto; position: relative; z-index: 10; }

/* Center Zone */
.center-zone { display: flex; flex-direction: column; gap: 10px; height: 100%; overflow: hidden; }
.game-board { flex: 1; background: rgba(38, 24, 21, 0.9); border: 2px solid #5d4037; border-radius: 8px; display: flex; flex-direction: column; }
.chat-board { height: 180px; flex: none; background: rgba(0, 0, 0, 0.6); border: 1px solid #444; border-radius: 8px; overflow: hidden; }

/* Header & Status */
.status-header { padding: 8px 12px; background: rgba(0,0,0,0.4); border-bottom: 1px solid #5d4037; display: flex; align-items: center; gap: 12px; }
.level-badge span { font-weight: bold; color: #ffd700; border: 1px solid #ffd700; padding: 2px 6px; border-radius: 4px; font-size: 0.8rem; }
.bars-container { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.stat-group { display: flex; gap: 10px; }
.stat-row { flex: 1; display: flex; align-items: center; gap: 5px; }
.progress-bg { flex: 1; height: 14px; background: #000; border: 1px solid #444; border-radius: 2px; position: relative; overflow: hidden; }
.progress-fill { height: 100%; transition: width 0.3s ease; }
.progress-fill.hp { background: linear-gradient(to right, #c62828, #e53935); }
.progress-fill.energy { background: linear-gradient(to right, #1565c0, #42a5f5); }
.exp-row .exp-bg { height: 4px; background: #333; margin-top: 2px; }
.exp-fill { height: 100%; background: #00e676; width: 0%; }
.stat-text { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); font-size: 0.65em; font-weight: bold; text-shadow: 1px 1px 0 #000; white-space: nowrap; }

/* Stage */
.stage-viewport { flex: 1; margin: 5px; border: 2px solid #3e2723; border-radius: 6px; overflow: hidden; position: relative; }
.stage-background { width: 100%; height: 100%; background-size: cover; background-position: center bottom; position: relative; }
.actor { position: absolute; bottom: 25px; display: flex; flex-direction: column; align-items: center; width: 140px; transition: left 0.1s linear; z-index: 10; }
.avatar-circle { width: 96px; height: 96px; display: flex; justify-content: center; align-items: center; filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5)); }
.avatar-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.2); }
.actor-label { margin-top: 5px; background: rgba(0,0,0,0.6); padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; }

/* Action Panel */
.action-panel { height: 60px; background: #1a100e; border-top: 2px solid #5d4037; display: flex; align-items: center; justify-content: center; gap: 10px; padding: 0 15px; }
.btn-action { border: none; border-radius: 6px; cursor: pointer; height: 40px; display: flex; align-items: center; justify-content: center; font-weight: bold; color: #fff; }
.map-btn { flex: 1; background: #2c3e50; border: 1px solid #34495e; }
.main-btn { flex: 2; background: linear-gradient(to bottom, #4e342e, #3e2723); border: 1px solid #c5a059; color: #c5a059; }
.sub-btn { flex: 0.5; background: #3e2723; border: 1px solid #5d4037; }

/* RIGHT ZONE & LOG PANEL */
.right-zone { display: flex; flex-direction: column; gap: 15px; height: 100%; overflow: hidden; }
.log-panel { height: 40%; flex: none; background: rgba(30, 30, 30, 0.95); border: 2px solid #5d4037; border-radius: 8px; display: flex; flex-direction: column; box-shadow: 0 5px 15px rgba(0,0,0,0.5); }
.log-header { background: #3e2723; padding: 8px; font-weight: bold; font-size: 0.95em; text-align: center; border-bottom: 2px solid #5d4037; text-transform: uppercase; letter-spacing: 1px; color: #ffb74d; }
.log-content { flex: 1; padding: 0; overflow-y: auto; background: #121212; }

/* LOG ITEM STYLES */
.log-entry { display: flex; padding: 10px; border-bottom: 1px solid #333; transition: background 0.2s; }
.log-entry:hover { background: #1e1e1e; }
.log-entry.ENEMY { border-left: 3px solid #ef5350; background: rgba(239, 83, 80, 0.05); }
.log-entry.GATHER { border-left: 3px solid #00e676; background: rgba(0, 230, 118, 0.05); }
.log-entry.ITEM { border-left: 3px solid #ffd700; }

.log-icon-col { width: 50px; flex-shrink: 0; display: flex; justify-content: center; align-items: flex-start; padding-top: 2px; }
.log-thumb-box { width: 42px; height: 42px; background: #2c2c2c; border: 1px solid #444; border-radius: 6px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.log-thumb-img { width: 80%; height: 80%; object-fit: contain; }
.log-time-text { font-size: 0.7rem; color: #666; font-family: monospace; }

.log-body-col { flex: 1; padding-left: 10px; display: flex; flex-direction: column; gap: 6px; }
.log-message { font-size: 0.9rem; line-height: 1.4; color: #ddd; }
.log-action { display: flex; justify-content: flex-start; }
.btn-small-action { background: #3e2723; border: 1px solid #8d6e63; color: #ffcc80; padding: 4px 12px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; cursor: pointer; transition: all 0.2s; }
.btn-small-action:hover { background: #5d4037; border-color: #ffcc80; color: #fff; transform: translateY(-1px); }

.quest-panel-wrapper { flex: 1; min-height: 0; background: rgba(30, 30, 30, 0.9); border: 2px solid #5d4037; border-radius: 8px; overflow: hidden; display: flex; flex-direction: column; }
.quest-panel-wrapper :deep(> div) { height: 100%; overflow-y: auto; }

/* Modals */
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.8); z-index: 3000; display: flex; align-items: center; justify-content: center; }
.map-modal-card { width: 90%; max-width: 500px; background: #1a1a1a; border: 2px solid #d4af37; border-radius: 8px; padding: 15px; color: #fff; }
.map-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 15px; }
.map-item { background: #333; padding: 10px; border-radius: 6px; cursor: pointer; border: 1px solid #444; position: relative; }
.map-item.active { border-color: #00e676; background: #1b5e20; }
.map-item.locked { opacity: 0.6; cursor: not-allowed; }
.lock-icon { position: absolute; right: 10px; top: 10px; }
.close-btn { width: 100%; padding: 10px; background: #b71c1c; border: none; color: white; font-weight: bold; cursor: pointer; }
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; }
@media (max-width: 900px) { .explore-layout { grid-template-columns: 1fr; } .right-zone { height: 400px; flex: none; } }
</style>