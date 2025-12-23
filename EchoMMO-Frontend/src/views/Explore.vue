<template>
  <div class="page-container explore-page">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${pageBgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

    <div class="explore-dashboard">
      
      <div class="left-main-col">
        
        <div class="stage-zone-wrapper wood-panel-embossed">
          <div class="game-board">
            <div class="status-header">
              <div class="level-badge"><span>Lv.{{ charStore.character?.level || 1 }}</span></div>
              <div class="bars-container">
                <div class="stat-group">
                  <div class="stat-row">
                    <span class="stat-icon"><i class="fas fa-heart text-red"></i></span>
                    <div class="progress-bg">
                      <div class="progress-fill hp" :style="{ width: charStore.hpPercent + '%' }"></div>
                      <span class="stat-text">{{ charStore.character?.currentHp }}/{{ charStore.character?.maxHp }}</span>
                    </div>
                  </div>
                  <div class="stat-row">
                    <span class="stat-icon"><i class="fas fa-bolt text-blue"></i></span>
                    <div class="progress-bg">
                      <div class="progress-fill energy" :style="{ width: charStore.energyPercent + '%' }"></div>
                      <span class="stat-text">{{ charStore.character?.currentEnergy }}/{{ charStore.character?.maxEnergy }}</span>
                    </div>
                  </div>
                </div>
                <div class="exp-row">
                    <div class="exp-bg">
                      <div class="exp-fill" :style="{ width: charStore.xpPercent + '%' }"></div>
                    </div>
                </div>
              </div>
            </div>

            <div class="stage-viewport">
              <div class="stage-background" :style="{ backgroundImage: `url(${getMapBg})` }">
                
                <div class="actor player" 
                     :style="{ left: charStore.explorationState.playerPos + '%', transform: `scaleX(${charStore.explorationState.moveDir})` }">
                    <div class="avatar-circle"><img :src="imgPlayer" class="avatar-img" /></div>
                </div>

                <transition name="pop-up">
                  <div v-if="currentLog && currentLog.rewards" class="stage-reward-overlay">
                      <div class="float-reward xp">
                        <span>+{{ currentLog.rewards.exp }} EXP</span>
                      </div>
                      <div class="float-reward gold">
                        <img src="https://htkhang111.github.io/resources/coin/r_coin.png" class="tiny-coin" />
                        <span>+{{ currentLog.rewards.gold }}</span>
                      </div>
                  </div>
                </transition>

              </div>
            </div>

            <div class="action-panel">
              <button class="btn-action map-btn" @click="showMapModal = true" :disabled="isMoving">
                <div class="btn-content"><span><i class="fas fa-map"></i> {{ currentMapName }}</span></div>
              </button>
              
              <button class="btn-action main-btn" @click="startExploration" :disabled="isMoving">
                <div class="btn-content">
                  <i class="fas fa-walking"></i>
                  <span v-if="!isMoving"> HÀNH TẨU</span>
                  <span v-else> ... ({{ countdown }}s)</span>
                </div>
              </button>
              
              <button class="btn-action sub-btn" @click="$router.push('/village')" :disabled="isMoving">
                <i class="fas fa-dungeon"></i>
              </button>
            </div>
          </div>
        </div>

        <div class="log-zone-wrapper wood-panel-embossed">
          <transition name="fade-slide" mode="out-in">
            <div v-if="currentLog" class="single-log-panel" :class="currentLog.type" :key="'log'">
              <div class="log-content-box">
                <div class="log-text" v-html="currentLog.msg"></div>
                <div v-if="currentLog.action" class="log-action-btn">
                  <button @click="currentLog.action.handler" class="btn-interact wuxia-btn-small">
                    {{ currentLog.action.label }}
                  </button>
                </div>
              </div>
            </div>
            
            <div v-else-if="isMoving" class="log-placeholder running" :key="'run'">
               <div style="display: flex; flex-direction: column; align-items: center; gap: 5px;">
                 <i class="fas fa-shoe-prints fa-spin" style="font-size: 24px;"></i> 
                 <span>Đang thám thính...</span>
               </div>
            </div>
            
            <div v-else class="log-placeholder idle" :key="'idle'">
              <span class="blink">...Chờ lệnh hành tẩu...</span>
            </div>
          </transition>
        </div>

      </div>

      <div class="right-side-col wood-panel-embossed">
        <div class="chat-zone-wrapper">
          <ChatPanel />
        </div>
      </div>

    </div>

    <div v-if="showMapModal" class="modal-overlay" @click.self="showMapModal = false">
      <div class="wood-panel modal-box map-modal">
        <h3 class="modal-header-txt">~ BẢN ĐỒ KHU VỰC ~</h3>
        <div class="map-grid">
          <div v-for="map in maps" :key="map.id" class="wood-card map-item" :class="{ active: currentMapId === map.id, locked: userLv < map.minLv }" @click="selectMap(map)">
            <div class="map-info">
              <div class="map-name">{{ map.name }}</div>
              <div class="map-lv">Cấp độ: {{ map.minLv }}-{{ map.maxLv }}</div>
            </div>
            <div v-if="userLv < map.minLv" class="lock-overlay"><i class="fas fa-lock"></i></div>
            <div class="active-indicator" v-if="currentMapId === map.id"><i class="fas fa-check"></i></div>
          </div>
        </div>
        <button class="wuxia-btn close-btn" @click="showMapModal = false">ĐÓNG</button>
      </div>
    </div>
    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useBattleStore } from "@/stores/battleStore";
import { useQuestStore } from "@/stores/questStore";
import { getItemImage, getCurrentSkin, getAssetUrl, getEnemyImage } from "@/utils/assetHelper";

import CaptchaModal from "@/components/CaptchaModal.vue";
import ChatPanel from "@/components/ChatPanel.vue";

const charStore = useCharacterStore();
const authStore = useAuthStore();
const battleStore = useBattleStore();
const questStore = useQuestStore();
const router = useRouter();
const captchaModal = ref(null);

const pageBgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);
const updateDayNight = () => { isNight.value = new Date().getHours() >= 18 || new Date().getHours() < 6; };

const isMoving = ref(false);
const countdown = ref(0);
const currentLog = ref(null); 

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
  if (userLv.value < map.minLv) { 
      setLog(`🔒 Cần Lv.${map.minLv} để vào ${map.name}`, "TEXT"); 
      return; 
  }
  currentMapId.value = map.id;
  showMapModal.value = false;
  setLog(`Đã chuyển đến: <b>${map.name}</b>`, "TEXT");
};

const getMapBg = computed(() => getAssetUrl("b_doanhtrai.png"));
const imgPlayer = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return isMoving.value ? skin.sprites.run : skin.sprites.idle;
});

const setLog = (msg, type = "TEXT", image = null, action = null, rewards = null) => {
    currentLog.value = { msg, type, image, action, rewards };
};

const handleImgError = (e) => { e.target.style.display = 'none'; };

let moveInterval = null;
const startMovingJS = () => {
  if (moveInterval) clearInterval(moveInterval);
  moveInterval = setInterval(() => {
    charStore.explorationState.playerPos += 0.5 * charStore.explorationState.moveDir;
    if (charStore.explorationState.playerPos >= 70) charStore.explorationState.moveDir = -1;
    else if (charStore.explorationState.playerPos <= 30) charStore.explorationState.moveDir = 1;
  }, 16);
};

const startExploration = () => {
  if (isMoving.value) return;
  if (charStore.character?.currentEnergy <= 0) {
      setLog(`<span style="color:#ef5350; font-weight:bold;">Đại hiệp đã kiệt sức! Đang về doanh trại...</span>`, "TEXT", null);
      setTimeout(() => { router.push('/village'); }, 1500);
      return;
  }

  currentLog.value = null;
  isMoving.value = true;
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
    const res = await charStore.explore({ mapId: currentMapId.value });
    await charStore.fetchCharacter();

    // 1. Chuẩn bị Rewards (chỉ cần Exp và Gold)
    const rewards = {
       exp: charStore.character.level + 10,
       gold: res.goldGained
    };

    // 2. Nội dung Text
    const eventMsg = res.message; 
    const displayMsg = res.type === 'ENEMY' 
        ? `<span style="color:#ef5350; font-weight:bold;">⚠️ ${eventMsg}</span>` 
        : `<span style="color:#eee;">${eventMsg}</span>`;

    // 3. Xử lý Log (Không cần quan tâm image cho stage nữa)
    if (res.type === "GATHERING") {
      setLog(displayMsg, "GATHER", null, { label: "⛏️ KHAI THÁC", handler: () => router.push("/gathering") }, rewards);
    }
    else if (res.type === "ENEMY") {
      const img = getEnemyImage(res.rewardName); // Vẫn lấy ảnh để lưu vào battleStore
      battleStore.setEncounter({ name: res.rewardName, img: img });
      setLog(displayMsg, "ENEMY", null, { label: "⚔️ CHIẾN ĐẤU", handler: () => router.push("/battle") }, rewards);
    } 
    else if (res.type === "ITEM") {
      setLog(`<span style="color:#ffd700;">${eventMsg}</span>`, "ITEM", null, null, rewards);
    }
    else {
      setLog(displayMsg, "TEXT", null, null, rewards);
    }

  } catch (e) {
    if (e.message === "CAPTCHA") captchaModal.value.open();
    else if (e.message && (e.message.includes("thể lực") || e.message === "NOT_ENOUGH_ENERGY")) {
        setLog(`<span style="color:#ef5350; font-weight:bold;">Hết thể lực! Đang hồi hương...</span>`, "TEXT");
        setTimeout(() => { router.push('/village'); }, 1500);
    }
    else {
        setLog(`<span style="color:red">Lỗi: ${e.message}</span>`, "TEXT");
    }
  }
};

onMounted(() => {
  charStore.fetchCharacter();
  if(questStore.fetchQuests) questStore.fetchQuests();
  updateDayNight();
});
onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
:root {
  --wood-border: #6d4c41;
  --gold-accent: #ffd700;
}

.explore-page { 
  background: transparent; 
  width: 100%; 
  height: 100vh; 
  display: flex;
  align-items: center; 
  justify-content: center; 
  box-sizing: border-box;
  color: #eee; 
  font-family: "Noto Serif TC", serif; 
  position: relative; 
  overflow: hidden;
}

.bg-layer { position: absolute; inset: 0; z-index: 0; background: #1a100d; pointer-events: none; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; opacity: 0.5; filter: sepia(20%) contrast(1.1); }
.wood-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, rgba(62, 39, 35, 0.8), rgba(20, 10, 5, 0.95)); mix-blend-mode: multiply; }

/* === DASHBOARD LAYOUT (FIXED HEIGHT) === */
.explore-dashboard {
  position: relative;
  z-index: 10;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 15px;
  width: 95%;
  max-width: 1200px;
  height: 520px;
  min-height: 520px;
  align-items: stretch;
}

/* Wood Panel Style */
.wood-panel-embossed {
  background: linear-gradient(135deg, rgba(45, 30, 25, 0.98), rgba(30, 20, 15, 0.99));
  border: 1px solid var(--wood-border);
  box-shadow: inset 0 0 20px rgba(0,0,0,0.8), 0 10px 20px rgba(0,0,0,0.5);
  border-radius: 6px;
  position: relative;
}
.wood-panel-embossed::after {
  content: ""; position: absolute; inset: 2px; border: 1px solid rgba(255, 215, 0, 0.15); border-radius: 4px; pointer-events: none;
}

/* === LEFT COLUMN === */
.left-main-col {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
}

/* 1. STAGE (MAP) */
.stage-zone-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between; 
}
.game-board { display: flex; flex-direction: column; height: 100%; }

.status-header { 
  flex: 0 0 auto; 
  padding: 10px 15px; background: rgba(0,0,0,0.4); border-bottom: 1px solid rgba(255,255,255,0.1); display: flex; align-items: center; gap: 12px; 
}

.level-badge span { font-weight: 900; color: var(--gold-accent); border: 1px solid var(--gold-accent); padding: 3px 8px; border-radius: 4px; font-size: 0.85rem; background: rgba(0,0,0,0.3); }
.bars-container { flex: 1; display: flex; flex-direction: column; gap: 6px; }
.stat-group { display: flex; gap: 15px; }
.stat-row { flex: 1; display: flex; align-items: center; gap: 8px; }
.progress-bg { flex: 1; height: 10px; background: #1a100e; border: 1px solid #4e342e; border-radius: 2px; position: relative; }
.progress-fill { height: 100%; transition: width 0.3s; }
.progress-fill.hp { background: linear-gradient(to right, #b71c1c, #ef5350); }
.progress-fill.energy { background: linear-gradient(to right, #0d47a1, #42a5f5); }
.stat-text { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); font-size: 0.65rem; font-weight: bold; color: #fff; }

.stage-viewport { 
  flex: 1; 
  min-height: 180px; 
  position: relative; border-top: 1px solid rgba(0,0,0,0.5); border-bottom: 1px solid rgba(0,0,0,0.5); overflow: hidden; 
}
.stage-background { width: 100%; height: 100%; background-size: cover; background-position: center bottom; filter: sepia(15%); position: relative; }
.actor { position: absolute; bottom: 15px; display: flex; flex-direction: column; align-items: center; width: 100px; transition: left 0.1s linear; }
.avatar-circle { width: 70px; height: 70px; filter: drop-shadow(0 5px 10px rgba(0,0,0,0.7)); }
.avatar-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.3); }

.action-panel { 
  flex: 0 0 70px;
  background: rgba(20, 10, 5, 0.8); display: flex; gap: 12px; padding: 12px; align-items: center; justify-content: center; border-top: 1px solid rgba(255, 255, 255, 0.05); 
}
.btn-action { height: 100%; border: 1px solid #4e342e; border-radius: 4px; cursor: pointer; color: #d7ccc8; font-weight: bold; transition: all 0.2s; display: flex; align-items: center; justify-content: center; background: linear-gradient(to bottom, #3e2723, #261815); }
.btn-action:hover:not(:disabled) { border-color: var(--gold-accent); color: var(--gold-accent); background: #4e342e; }
.map-btn { flex: 1; }
.main-btn { flex: 2; border-color: var(--gold-accent); color: var(--gold-accent); font-size: 1.1rem; background: linear-gradient(to bottom, #4e342e, #2e1e19); box-shadow: 0 4px 10px rgba(0,0,0,0.3); }
.main-btn:active { transform: translateY(1px); }
.sub-btn { flex: 0.5; font-size: 1.2rem; }

/* 2. LOG ZONE (UPDATED) */
.log-zone-wrapper {
  flex: 0 0 160px;
  display: flex; 
  align-items: center; 
  justify-content: center; 
  padding: 10px 20px;
}

.single-log-panel { 
  width: 100%; 
  display: flex; 
  flex-direction: column;
  align-items: center; 
  justify-content: center; 
  gap: 10px; 
  text-align: center;
}

.log-content-box { 
  width: 100%;
  display: flex; 
  flex-direction: column; 
  justify-content: center; 
  align-items: center; 
  gap: 6px; 
}

.log-text { font-size: 1rem; color: #fff; line-height: 1.3; }
.wuxia-btn-small { background: #b71c1c; color: #fff; border: 1px solid #ef5350; padding: 4px 15px; border-radius: 4px; font-size: 0.85rem; cursor: pointer; margin-top: 5px; }
.log-placeholder { color: #a1887f; font-style: italic; font-size: 1rem; display: flex; align-items: center; justify-content: center; gap: 10px; width: 100%; }
.blink { animation: blinking 2s infinite; }
@keyframes blinking { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }

/* === RIGHT COLUMN === */
.right-side-col {
  height: 100%;
  display: flex; 
  flex-direction: column;
  overflow: hidden; 
}
.chat-zone-wrapper { 
  flex: 1; 
  height: 100%;
  background: transparent; 
  overflow: hidden; 
  display: flex; 
  flex-direction: column; 
}

/* MODAL & RESPONSIVE */
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 3000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(2px); }
.modal-box { width: 90%; max-width: 450px; padding: 25px; color: #fff; display: flex; flex-direction: column; gap: 15px; }
.modal-header-txt { text-align: center; color: var(--gold-accent); margin: 0; padding-bottom: 10px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.map-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; max-height: 300px; overflow-y: auto; padding-right: 5px; }
.map-item { padding: 15px; cursor: pointer; position: relative; display: flex; flex-direction: column; align-items: center; text-align: center; gap: 5px; background: rgba(0,0,0,0.2); border: 1px solid #4e342e; }
.map-item:hover:not(.locked) { border-color: var(--gold-accent); background: rgba(255, 215, 0, 0.05); }
.map-item.active { border-color: #66bb6a; background: rgba(102, 187, 106, 0.1); }
.wuxia-btn.close-btn { background: #3e2723; color: #d7ccc8; border: 1px solid #5d4037; padding: 10px; width: 100%; border-radius: 4px; font-weight: bold; cursor: pointer; }

/* === REWARD OVERLAY (NEW) === */
.stage-reward-overlay {
    position: absolute;
    top: 20%;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    z-index: 50;
    pointer-events: none;
}

.float-reward {
    padding: 4px 12px;
    border-radius: 20px;
    font-weight: 900;
    font-size: 1.1rem;
    text-shadow: 1px 1px 0 #000, -1px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000;
    display: flex;
    align-items: center;
    gap: 5px;
    box-shadow: 0 4px 8px rgba(0,0,0,0.5);
    animation: floatUp 1.5s ease-out forwards;
}

.float-reward.xp {
    background: linear-gradient(90deg, #1565c0, #42a5f5);
    color: #fff;
    border: 1px solid #90caf9;
}

.float-reward.gold {
    background: linear-gradient(90deg, #ff8f00, #ffca28);
    color: #fff;
    border: 1px solid #ffe082;
}

.tiny-coin { width: 20px; height: 20px; object-fit: contain; }

@keyframes floatUp {
    0% { transform: translateY(10px) scale(0.8); opacity: 0; }
    20% { transform: translateY(0) scale(1.1); opacity: 1; }
    100% { transform: translateY(-40px) scale(1); opacity: 1; }
}

/* Transitions */
.pop-up-enter-active, .pop-up-leave-active { transition: all 0.3s ease; }
.pop-up-enter-from, .pop-up-leave-to { opacity: 0; transform: scale(0.5); }

::-webkit-scrollbar { width: 5px; }
::-webkit-scrollbar-thumb { background: #4e342e; border-radius: 2px; }

@media (max-width: 900px) {
  .explore-page { height: auto; padding: 10px 10px 80px 10px; display: block; overflow-y: auto; }
  .explore-dashboard { display: flex; flex-direction: column; gap: 15px; height: auto; min-height: 0; }
  .left-main-col { height: auto; }
  .right-side-col { height: 400px; }
}
</style>