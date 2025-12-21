<!-- <template>
  <div class="page-container explore-page">
    <div class="explore-layout">
      <div class="center-zone">
        
        <div class="game-board">
          <div class="status-header">
            <div class="level-badge">
              <span>Lv.{{ charStore.character?.level || 1 }}</span>
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
              :style="{ backgroundImage: `url(${getMapBg})` }"
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
                  <i class="fas fa-walking"></i>
                  <span v-if="!isMoving">HÀNH TẨU</span>
                  <span v-else>... ({{ countdown }}s)</span>
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
          <button class="modal-btn flee" @click="flee">Bỏ Chạy</button>
          <button class="modal-btn fight" @click="goToBattle">
            CHIẾN ĐẤU
          </button>
        </div>
      </div>
    </div>
    
    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useRouter } from "vue-router";
// Stores
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useBattleStore } from "@/stores/battleStore";
import { useQuestStore } from "@/stores/questStore";

// API & Utils
import axiosClient from "@/api/axiosClient"; // [FIX] Import thêm axiosClient để gửi chat
import { getItemImage, getCurrentSkin, getAssetUrl, getEnemyImage } from "@/utils/assetHelper";

// Components
import CaptchaModal from "@/components/CaptchaModal.vue";
import ChatPanel from "@/components/ChatPanel.vue";
import QuestPanel from "@/components/QuestPanel.vue";

// --- INIT STORES ---
const charStore = useCharacterStore();
const authStore = useAuthStore();
const battleStore = useBattleStore();
const questStore = useQuestStore();
const router = useRouter();
const captchaModal = ref(null);

// --- STATE ---
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

// Map Config
const maps = [
  { id: "MAP_01", name: "Đồng Bằng", minLv: 1, maxLv: 19 },
  { id: "MAP_02", name: "Rừng Rậm", minLv: 20, maxLv: 30 },
  { id: "MAP_03", name: "Sa Mạc", minLv: 30, maxLv: 40 },
  { id: "MAP_04", name: "Núi Cao", minLv: 40, maxLv: 50 },
  { id: "MAP_05", name: "Băng Đảo", minLv: 50, maxLv: 60 },
  { id: "MAP_06", name: "Đầm Lầy", minLv: 60, maxLv: 70 },
];
const currentMapName = computed(
  () => maps.find((m) => m.id === currentMapId.value)?.name || "Đồng Bằng"
);

// Đồng bộ Map khi load game
watch(
  () => charStore.character,
  (newChar) => {
    if (newChar && newChar.currentMapId) {
      const savedMap = maps.find((m) => m.id === newChar.currentMapId);
      if (savedMap) currentMapId.value = newChar.currentMapId;
    }
  },
  { immediate: true }
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

// Background Map
const getMapBg = computed(() => {
    // Logic map bg tương ứng id (hoặc random tạm)
    return getAssetUrl("b_doanhtrai.png");
});

const imgPlayer = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return isMoving.value ? skin.sprites.run : skin.sprites.idle;
});

// Logic Nhật ký
let moveInterval = null;
const getTime = () =>
  new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
const addLog = (msg) => logs.value.unshift({ time: getTime(), msg });

// Animation chạy
const startMovingJS = () => {
  if (moveInterval) clearInterval(moveInterval);
  moveInterval = setInterval(() => {
    charStore.explorationState.playerPos +=
      0.5 * charStore.explorationState.moveDir;
    if (charStore.explorationState.playerPos >= 70)
      charStore.explorationState.moveDir = -1;
    else if (charStore.explorationState.playerPos <= 30)
      charStore.explorationState.moveDir = 1;
  }, 16);
};

// [TÍNH NĂNG MỚI] Gửi thông báo "Xuất quan" lên kênh Chat
const broadcastJoinMessage = async () => {
  const username = authStore.user?.username || "Đạo hữu";
  
  const phrases = [
    `${username} Vừa xuất quan.`,
    `${username} Đã khởi hành.`,
    `${username} Đã dấn thân vào hồng trần.`,
    `${username} đã đạp gió lên đường.`,
    `${username} vừa tế lên phi chu.`
  ];
  
  const randomPhrase = phrases[Math.floor(Math.random() * phrases.length)];
  
  try {
    // Gọi API REST (đã fix ở Backend)
    await axiosClient.post('/chat/send', {
        content: randomPhrase
        // senderName sẽ được backend tự điền
    });
  } catch (e) {
    console.warn("Không thể gửi thông báo hành tẩu:", e);
  }
};

// BẮT ĐẦU HÀNH TẨU
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

// XỬ LÝ KẾT QUẢ
const handleResult = async () => {
  clearInterval(moveInterval);
  isMoving.value = false;

  try {
    const res = await charStore.explore({ mapId: currentMapId.value });
    await charStore.fetchCharacter(); // Đồng bộ lại stat

    // 1. Gặp tài nguyên (Gathering)
    if (res.type === "GATHERING") {
      addLog(`<span style="color:#00e676; font-weight:bold;">🌿 ${res.message}</span>`);
      setTimeout(() => { router.push("/gathering"); }, 500);
      return;
    }

    // 2. Nhặt được Vật phẩm (Item)
    if (res.type === "ITEM" && res.rewardName) {
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = getItemImage(res.rewardName) || getItemImage("GOLD");
      
      // Check Quest
      if (questStore.checkQuestCompletion) {
        const rewardGold = questStore.checkQuestCompletion(res.rewardName);
        if (rewardGold > 0) {
            addLog(`<span style="color:#00e676;">${res.message}</span>`);
            addLog(`<span style="color:#ffd700; font-weight:bold;">💰 Xong nhiệm vụ: +${rewardGold} Vàng!</span>`);
            if(charStore.character) charStore.character.gold += rewardGold;
        } else {
            addLog(`<span style="color:#00e676;">${res.message}</span>`);
        }
      } else {
        addLog(`<span style="color:#00e676;">${res.message}</span>`);
      }

    } 
    // 3. Gặp Quái (Enemy)
    else if (res.type === "ENEMY") {
      isEncounter.value = true;
      showTarget.value = true;
      targetName.value = res.rewardName;
      
      targetImage.value = getEnemyImage(res.rewardName); 
      
      battleStore.setEncounter({
        name: res.rewardName,
        img: targetImage.value,
      });
      addLog(`<span style="color:#ef5350;">⚠️ ${res.message}</span>`);
    } 
    // 4. Khác
    else {
      addLog(`<span style="color:#aaa;">${res.message}</span>`);
    }

  } catch (e) {
    if (e.message === "CAPTCHA") {
      captchaModal.value.open();
    } else {
      const errorMsg = e.response?.data?.message || e.message;
      addLog(`<span style="color:red">Lỗi: ${errorMsg}</span>`);
    }
  }
};

const goToBattle = () => router.push("/battle");
const flee = () => {
  isEncounter.value = false;
  showTarget.value = false;
  addLog("Đã chạy thoát.");
};

onMounted(() => {
  charStore.fetchCharacter();
  if(questStore.fetchQuests) questStore.fetchQuests();
  
  // [FIX] Gọi hàm broadcast khi vào trang
  broadcastJoinMessage();
});

onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
/* PAGE CONTAINER: Full màn hình, không scroll */
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

/* --- CỘT GIỮA --- */
.center-zone {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  overflow: hidden;
}

/* Game Board: Flex 1 để chiếm hết chỗ trống */
.game-board {
  flex: 1; 
  min-height: 0; /* Fix flexbox scroll issue */
  background: #261815;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Chat Board: Cố định chiều cao (ngắn lại) */
.chat-board {
  height: 180px; /* Cố định chiều cao */
  flex: none;    /* Không co giãn */
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid #444;
  border-radius: 8px;
  overflow: hidden;
}

/* Các thành phần bên trong Game Board */
.status-header {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.4);
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.level-badge span {
  font-weight: bold;
  color: #ffd700;
  border: 1px solid #ffd700;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
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
.progress-fill.hp { background: linear-gradient(to right, #c62828, #e53935); }
.progress-fill.energy { background: linear-gradient(to right, #1565c0, #42a5f5); }
.exp-row .exp-bg {
  height: 4px;
  background: #333;
  margin-top: 2px;
}
.exp-fill {
  height: 100%;
  background: #00e676;
  width: 0%;
}

.stat-text {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.65em;
  font-weight: bold;
  text-shadow: 1px 1px 0 #000;
  white-space: nowrap;
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
  width: 100%; height: 100%;
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

.avatar-circle, .avatar-target {
  width: 96px; height: 96px;
  display: flex; justify-content: center; align-items: center;
  filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5));
}

.avatar-img {
  width: 100%; height: 100%;
  object-fit: contain;
  transform: scale(1.2);
}

.actor-label {
  margin-top: 5px;
  background: rgba(0,0,0,0.6);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.target-name { color: #ffd700; }

.action-panel {
  height: 60px;
  background: #1a100e;
  border-top: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 15px;
  flex-shrink: 0;
}

.btn-action {
  border: none; border-radius: 6px;
  cursor: pointer; height: 40px;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; color: #fff;
}
.map-btn { flex: 1; background: #2c3e50; border: 1px solid #34495e; }
.main-btn { flex: 2; background: linear-gradient(to bottom, #4e342e, #3e2723); border: 1px solid #c5a059; color: #c5a059; }
.sub-btn { flex: 0.5; background: #3e2723; border: 1px solid #5d4037; }

/* --- CỘT PHẢI --- */
.right-zone {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
  overflow: hidden;
}

.log-panel {
  height: 35%; /* Cố định tỷ lệ */
  flex: none;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex; flex-direction: column;
}

.log-header {
  background: #3e2723; padding: 5px 10px;
  font-weight: bold; font-size: 0.9em; text-align: center;
  border-bottom: 1px solid #5d4037;
}

.log-content {
  flex: 1; padding: 8px; overflow-y: auto;
  background: #111; font-size: 0.85em;
}

.quest-panel-wrapper {
  flex: 1; /* Chiếm hết phần dưới */
  min-height: 0;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  overflow: hidden;
  display: flex; flex-direction: column;
}
.quest-panel-wrapper :deep(> div) { height: 100%; overflow-y: auto; }

/* Modal Styles */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 3000;
  display: flex; align-items: center; justify-content: center;
}
.map-modal-card {
  width: 90%; max-width: 500px;
  background: #1a1a1a;
  border: 2px solid #d4af37;
  border-radius: 8px;
  padding: 15px; color: #fff;
}
.map-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 15px; }
.map-item {
  background: #333; padding: 10px;
  border-radius: 6px; cursor: pointer;
  border: 1px solid #444; position: relative;
}
.map-item.active { border-color: #00e676; background: #1b5e20; }
.map-item.locked { opacity: 0.6; cursor: not-allowed; }
.lock-icon { position: absolute; right: 10px; top: 10px; }
.close-btn { width: 100%; padding: 10px; background: #b71c1c; border: none; color: white; font-weight: bold; cursor: pointer; }

.encounter-modal {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 2000;
  display: flex; align-items: center; justify-content: center;
}
.modal-card {
  width: 350px; background: #261815;
  border: 2px solid #b71c1c; border-radius: 12px;
}
.modal-header { background: #b71c1c; color: #fff; padding: 10px; text-align: center; font-weight: bold; }
.modal-body { padding: 20px; text-align: center; }
.enemy-preview-img { width: 100px; height: 100px; object-fit: contain; }
.modal-footer { padding: 15px; display: flex; gap: 15px; }
.modal-btn { flex: 1; padding: 10px; cursor: pointer; font-weight: bold; }
.modal-btn.flee { background: #555; color: #ccc; }
.modal-btn.fight { background: #d32f2f; color: #fff; }

.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; }

@media (max-width: 900px) {
  .explore-layout { grid-template-columns: 1fr; }
  .right-zone { height: 400px; flex: none; }
}
</style> -->

<!-- <template>
  <div class="page-container explore-page">
    <div class="explore-layout">
      <div class="center-zone">
        
        <div class="game-board">
          <div class="status-header">
            <div class="level-badge">
              <span>Lv.{{ charStore.character?.level || 1 }}</span>
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
              :style="{ backgroundImage: `url(${getMapBg})` }"
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
                  <i class="fas fa-walking"></i>
                  <span v-if="!isMoving">HÀNH TẨU</span>
                  <span v-else>... ({{ countdown }}s)</span>
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
          <button class="modal-btn flee" @click="flee">Bỏ Chạy</button>
          <button class="modal-btn fight" @click="goToBattle">
            CHIẾN ĐẤU
          </button>
        </div>
      </div>
    </div>
    
    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, onActivated, watch } from "vue";
import { useRouter } from "vue-router";
// Stores
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useBattleStore } from "@/stores/battleStore";
import { useQuestStore } from "@/stores/questStore";
import { useChatStore } from "@/stores/chatStore"; // [FIX] Import Chat Store

// API & Utils
import axiosClient from "@/api/axiosClient";
import { getItemImage, getCurrentSkin, getAssetUrl, getEnemyImage } from "@/utils/assetHelper";

// Components
import CaptchaModal from "@/components/CaptchaModal.vue";
import ChatPanel from "@/components/ChatPanel.vue";
import QuestPanel from "@/components/QuestPanel.vue";

// --- INIT STORES ---
const charStore = useCharacterStore();
const authStore = useAuthStore();
const battleStore = useBattleStore();
const questStore = useQuestStore();
const chatStore = useChatStore(); // [FIX] Init Chat Store
const router = useRouter();
const captchaModal = ref(null);

// --- STATE ---
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

// Map Config
const maps = [
  { id: "MAP_01", name: "Đồng Bằng", minLv: 1, maxLv: 19 },
  { id: "MAP_02", name: "Rừng Rậm", minLv: 20, maxLv: 30 },
  { id: "MAP_03", name: "Sa Mạc", minLv: 30, maxLv: 40 },
  { id: "MAP_04", name: "Núi Cao", minLv: 40, maxLv: 50 },
  { id: "MAP_05", name: "Băng Đảo", minLv: 50, maxLv: 60 },
  { id: "MAP_06", name: "Đầm Lầy", minLv: 60, maxLv: 70 },
];
const currentMapName = computed(
  () => maps.find((m) => m.id === currentMapId.value)?.name || "Đồng Bằng"
);

// Đồng bộ Map khi load game
watch(
  () => charStore.character,
  (newChar) => {
    if (newChar && newChar.currentMapId) {
      const savedMap = maps.find((m) => m.id === newChar.currentMapId);
      if (savedMap) currentMapId.value = newChar.currentMapId;
    }
  },
  { immediate: true }
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

// Background Map
const getMapBg = computed(() => {
    return getAssetUrl("b_doanhtrai.png");
});

const imgPlayer = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return isMoving.value ? skin.sprites.run : skin.sprites.idle;
});

// Logic Nhật ký
let moveInterval = null;
const getTime = () =>
  new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
const addLog = (msg) => logs.value.unshift({ time: getTime(), msg });

// Animation chạy
const startMovingJS = () => {
  if (moveInterval) clearInterval(moveInterval);
  moveInterval = setInterval(() => {
    charStore.explorationState.playerPos +=
      0.5 * charStore.explorationState.moveDir;
    if (charStore.explorationState.playerPos >= 70)
      charStore.explorationState.moveDir = -1;
    else if (charStore.explorationState.playerPos <= 30)
      charStore.explorationState.moveDir = 1;
  }, 16);
};

// [TÍNH NĂNG MỚI] Gửi thông báo "Xuất quan" lên kênh Chat
const broadcastJoinMessage = async () => {
  const username = authStore.user?.username || "Đạo hữu";
  
  const phrases = [
    `${username} Vừa xuất quan.`,
    `${username} Đã khởi hành.`,
    `${username} Đã dấn thân vào hồng trần.`,
    `${username} đã đạp gió lên đường.`,
    `${username} vừa tế lên phi chu.`
  ];
  
  const randomPhrase = phrases[Math.floor(Math.random() * phrases.length)];
  
  try {
    // 1. Gửi API để người khác thấy
    await axiosClient.post('/chat/send', {
        content: randomPhrase
    });
    
    // 2. [QUAN TRỌNG] Tự thêm vào chat của chính mình ngay lập tức
    chatStore.addMessage({
        senderName: username,
        senderAvatar: authStore.user?.avatarUrl, // Nếu cần avatar
        content: randomPhrase,
        timestamp: new Date().toISOString(),
        role: authStore.user?.role || 'USER'
    });

  } catch (e) {
    console.warn("Không thể gửi thông báo hành tẩu:", e);
  }
};

// BẮT ĐẦU HÀNH TẨU
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

// XỬ LÝ KẾT QUẢ
const handleResult = async () => {
  clearInterval(moveInterval);
  isMoving.value = false;

  try {
    const res = await charStore.explore({ mapId: currentMapId.value });
    await charStore.fetchCharacter();

    if (res.type === "GATHERING") {
      addLog(`<span style="color:#00e676; font-weight:bold;">🌿 ${res.message}</span>`);
      setTimeout(() => { router.push("/gathering"); }, 500);
      return;
    }

    if (res.type === "ITEM" && res.rewardName) {
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = getItemImage(res.rewardName) || getItemImage("GOLD");
      
      if (questStore.checkQuestCompletion) {
        const rewardGold = questStore.checkQuestCompletion(res.rewardName);
        if (rewardGold > 0) {
            addLog(`<span style="color:#00e676;">${res.message}</span>`);
            addLog(`<span style="color:#ffd700; font-weight:bold;">💰 Xong nhiệm vụ: +${rewardGold} Vàng!</span>`);
            if(charStore.character) charStore.character.gold += rewardGold;
        } else {
            addLog(`<span style="color:#00e676;">${res.message}</span>`);
        }
      } else {
        addLog(`<span style="color:#00e676;">${res.message}</span>`);
      }

    } 
    else if (res.type === "ENEMY") {
      isEncounter.value = true;
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = getEnemyImage(res.rewardName); 
      
      battleStore.setEncounter({
        name: res.rewardName,
        img: targetImage.value,
      });
      addLog(`<span style="color:#ef5350;">⚠️ ${res.message}</span>`);
    } 
    else {
      addLog(`<span style="color:#aaa;">${res.message}</span>`);
    }

  } catch (e) {
    if (e.message === "CAPTCHA") {
      captchaModal.value.open();
    } else {
      const errorMsg = e.response?.data?.message || e.message;
      addLog(`<span style="color:red">Lỗi: ${errorMsg}</span>`);
    }
  }
};

const goToBattle = () => router.push("/battle");
const flee = () => {
  isEncounter.value = false;
  showTarget.value = false;
  addLog("Đã chạy thoát.");
};

onMounted(() => {
  charStore.fetchCharacter();
  if(questStore.fetchQuests) questStore.fetchQuests();
  broadcastJoinMessage();
});

// [FIX] Nếu dùng keep-alive, khi quay lại tab Explore nó sẽ chạy hàm này
onActivated(() => {
  broadcastJoinMessage();
});

onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
/* PAGE CONTAINER: Full màn hình, không scroll */
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

/* --- CỘT GIỮA --- */
.center-zone {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  overflow: hidden;
}

/* Game Board: Flex 1 để chiếm hết chỗ trống */
.game-board {
  flex: 1; 
  min-height: 0; /* Fix flexbox scroll issue */
  background: #261815;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Chat Board: Cố định chiều cao (ngắn lại) */
.chat-board {
  height: 180px; /* Cố định chiều cao */
  flex: none;    /* Không co giãn */
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid #444;
  border-radius: 8px;
  overflow: hidden;
}

/* Các thành phần bên trong Game Board */
.status-header {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.4);
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.level-badge span {
  font-weight: bold;
  color: #ffd700;
  border: 1px solid #ffd700;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
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
.progress-fill.hp { background: linear-gradient(to right, #c62828, #e53935); }
.progress-fill.energy { background: linear-gradient(to right, #1565c0, #42a5f5); }
.exp-row .exp-bg {
  height: 4px;
  background: #333;
  margin-top: 2px;
}
.exp-fill {
  height: 100%;
  background: #00e676;
  width: 0%;
}

.stat-text {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.65em;
  font-weight: bold;
  text-shadow: 1px 1px 0 #000;
  white-space: nowrap;
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
  width: 100%; height: 100%;
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

.avatar-circle, .avatar-target {
  width: 96px; height: 96px;
  display: flex; justify-content: center; align-items: center;
  filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5));
}

.avatar-img {
  width: 100%; height: 100%;
  object-fit: contain;
  transform: scale(1.2);
}

.actor-label {
  margin-top: 5px;
  background: rgba(0,0,0,0.6);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.target-name { color: #ffd700; }

.action-panel {
  height: 60px;
  background: #1a100e;
  border-top: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 15px;
  flex-shrink: 0;
}

.btn-action {
  border: none; border-radius: 6px;
  cursor: pointer; height: 40px;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; color: #fff;
}
.map-btn { flex: 1; background: #2c3e50; border: 1px solid #34495e; }
.main-btn { flex: 2; background: linear-gradient(to bottom, #4e342e, #3e2723); border: 1px solid #c5a059; color: #c5a059; }
.sub-btn { flex: 0.5; background: #3e2723; border: 1px solid #5d4037; }

/* --- CỘT PHẢI --- */
.right-zone {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
  overflow: hidden;
}

.log-panel {
  height: 35%; /* Cố định tỷ lệ */
  flex: none;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex; flex-direction: column;
}

.log-header {
  background: #3e2723; padding: 5px 10px;
  font-weight: bold; font-size: 0.9em; text-align: center;
  border-bottom: 1px solid #5d4037;
}

.log-content {
  flex: 1; padding: 8px; overflow-y: auto;
  background: #111; font-size: 0.85em;
}

.quest-panel-wrapper {
  flex: 1; /* Chiếm hết phần dưới */
  min-height: 0;
  background: #1e1e1e;
  border: 2px solid #5d4037;
  border-radius: 8px;
  overflow: hidden;
  display: flex; flex-direction: column;
}
.quest-panel-wrapper :deep(> div) { height: 100%; overflow-y: auto; }

/* Modal Styles */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 3000;
  display: flex; align-items: center; justify-content: center;
}
.map-modal-card {
  width: 90%; max-width: 500px;
  background: #1a1a1a;
  border: 2px solid #d4af37;
  border-radius: 8px;
  padding: 15px; color: #fff;
}
.map-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 15px; }
.map-item {
  background: #333; padding: 10px;
  border-radius: 6px; cursor: pointer;
  border: 1px solid #444; position: relative;
}
.map-item.active { border-color: #00e676; background: #1b5e20; }
.map-item.locked { opacity: 0.6; cursor: not-allowed; }
.lock-icon { position: absolute; right: 10px; top: 10px; }
.close-btn { width: 100%; padding: 10px; background: #b71c1c; border: none; color: white; font-weight: bold; cursor: pointer; }

.encounter-modal {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 2000;
  display: flex; align-items: center; justify-content: center;
}
.modal-card {
  width: 350px; background: #261815;
  border: 2px solid #b71c1c; border-radius: 12px;
}
.modal-header { background: #b71c1c; color: #fff; padding: 10px; text-align: center; font-weight: bold; }
.modal-body { padding: 20px; text-align: center; }
.enemy-preview-img { width: 100px; height: 100px; object-fit: contain; }
.modal-footer { padding: 15px; display: flex; gap: 15px; }
.modal-btn { flex: 1; padding: 10px; cursor: pointer; font-weight: bold; }
.modal-btn.flee { background: #555; color: #ccc; }
.modal-btn.fight { background: #d32f2f; color: #fff; }

.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; }

@media (max-width: 900px) {
  .explore-layout { grid-template-columns: 1fr; }
  .right-zone { height: 400px; flex: none; }
}
</style> -->

<!-- <template>
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
            <div class="level-badge">
              <span>Lv.{{ charStore.character?.level || 1 }}</span>
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
              :style="{ backgroundImage: `url(${getMapBg})` }"
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
                  <i class="fas fa-walking"></i>
                  <span v-if="!isMoving">HÀNH TẨU</span>
                  <span v-else>... ({{ countdown }}s)</span>
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
          <button class="modal-btn flee" @click="flee">Bỏ Chạy</button>
          <button class="modal-btn fight" @click="goToBattle">
            CHIẾN ĐẤU
          </button>
        </div>
      </div>
    </div>
    
    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, onActivated, watch } from "vue";
import { useRouter } from "vue-router";
// Stores
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useBattleStore } from "@/stores/battleStore";
import { useQuestStore } from "@/stores/questStore";
import { useChatStore } from "@/stores/chatStore";

// API & Utils
import axiosClient from "@/api/axiosClient";
import { getItemImage, getCurrentSkin, getAssetUrl, getEnemyImage } from "@/utils/assetHelper";

// Components
import CaptchaModal from "@/components/CaptchaModal.vue";
import ChatPanel from "@/components/ChatPanel.vue";
import QuestPanel from "@/components/QuestPanel.vue";

// --- INIT STORES ---
const charStore = useCharacterStore();
const authStore = useAuthStore();
const battleStore = useBattleStore();
const questStore = useQuestStore();
const chatStore = useChatStore();
const router = useRouter();
const captchaModal = ref(null);

// --- BACKGROUND LOGIC (MỚI THÊM) ---
const pageBgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  // Chỉ kiểm tra giờ để chỉnh màu nền, không hiển thị text
  isNight.value = h >= 18 || h < 6;
};

// --- STATE ---
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

// Map Config
const maps = [
  { id: "MAP_01", name: "Đồng Bằng", minLv: 1, maxLv: 19 },
  { id: "MAP_02", name: "Rừng Rậm", minLv: 20, maxLv: 30 },
  { id: "MAP_03", name: "Sa Mạc", minLv: 30, maxLv: 40 },
  { id: "MAP_04", name: "Núi Cao", minLv: 40, maxLv: 50 },
  { id: "MAP_05", name: "Băng Đảo", minLv: 50, maxLv: 60 },
  { id: "MAP_06", name: "Đầm Lầy", minLv: 60, maxLv: 70 },
];
const currentMapName = computed(
  () => maps.find((m) => m.id === currentMapId.value)?.name || "Đồng Bằng"
);

// Đồng bộ Map khi load game
watch(
  () => charStore.character,
  (newChar) => {
    if (newChar && newChar.currentMapId) {
      const savedMap = maps.find((m) => m.id === newChar.currentMapId);
      if (savedMap) currentMapId.value = newChar.currentMapId;
    }
  },
  { immediate: true }
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

// Background STAGE (Khu vực chạy)
const getMapBg = computed(() => {
    return getAssetUrl("b_doanhtrai.png");
});

const imgPlayer = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return isMoving.value ? skin.sprites.run : skin.sprites.idle;
});

// Logic Nhật ký
let moveInterval = null;
const getTime = () =>
  new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
const addLog = (msg) => logs.value.unshift({ time: getTime(), msg });

// Animation chạy
const startMovingJS = () => {
  if (moveInterval) clearInterval(moveInterval);
  moveInterval = setInterval(() => {
    charStore.explorationState.playerPos +=
      0.5 * charStore.explorationState.moveDir;
    if (charStore.explorationState.playerPos >= 70)
      charStore.explorationState.moveDir = -1;
    else if (charStore.explorationState.playerPos <= 30)
      charStore.explorationState.moveDir = 1;
  }, 16);
};

// Gửi thông báo "Xuất quan"
const broadcastJoinMessage = async () => {
  const username = authStore.user?.username || "Đạo hữu";
  const phrases = [
    `${username} Vừa xuất quan.`,
    `${username} Đã khởi hành.`,
    `${username} Đã dấn thân vào hồng trần.`,
    `${username} đã đạp gió lên đường.`,
    `${username} vừa tế lên phi chu.`
  ];
  const randomPhrase = phrases[Math.floor(Math.random() * phrases.length)];
  try {
    await axiosClient.post('/chat/send', { content: randomPhrase });
    chatStore.addMessage({
        senderName: username,
        senderAvatar: authStore.user?.avatarUrl,
        content: randomPhrase,
        timestamp: new Date().toISOString(),
        role: authStore.user?.role || 'USER'
    });
  } catch (e) {
    console.warn("Không thể gửi thông báo hành tẩu:", e);
  }
};

// BẮT ĐẦU HÀNH TẨU
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

// XỬ LÝ KẾT QUẢ
const handleResult = async () => {
  clearInterval(moveInterval);
  isMoving.value = false;

  try {
    const res = await charStore.explore({ mapId: currentMapId.value });
    await charStore.fetchCharacter();

    if (res.type === "GATHERING") {
      addLog(`<span style="color:#00e676; font-weight:bold;">🌿 ${res.message}</span>`);
      setTimeout(() => { router.push("/gathering"); }, 500);
      return;
    }

    if (res.type === "ITEM" && res.rewardName) {
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = getItemImage(res.rewardName) || getItemImage("GOLD");
      
      if (questStore.checkQuestCompletion) {
        const rewardGold = questStore.checkQuestCompletion(res.rewardName);
        if (rewardGold > 0) {
            addLog(`<span style="color:#00e676;">${res.message}</span>`);
            addLog(`<span style="color:#ffd700; font-weight:bold;">💰 Xong nhiệm vụ: +${rewardGold} Vàng!</span>`);
            if(charStore.character) charStore.character.gold += rewardGold;
        } else {
            addLog(`<span style="color:#00e676;">${res.message}</span>`);
        }
      } else {
        addLog(`<span style="color:#00e676;">${res.message}</span>`);
      }

    } 
    else if (res.type === "ENEMY") {
      isEncounter.value = true;
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = getEnemyImage(res.rewardName); 
      
      battleStore.setEncounter({
        name: res.rewardName,
        img: targetImage.value,
      });
      addLog(`<span style="color:#ef5350;">⚠️ ${res.message}</span>`);
    } 
    else {
      addLog(`<span style="color:#aaa;">${res.message}</span>`);
    }

  } catch (e) {
    if (e.message === "CAPTCHA") {
      captchaModal.value.open();
    } else {
      const errorMsg = e.response?.data?.message || e.message;
      addLog(`<span style="color:red">Lỗi: ${errorMsg}</span>`);
    }
  }
};

const goToBattle = () => router.push("/battle");
const flee = () => {
  isEncounter.value = false;
  showTarget.value = false;
  addLog("Đã chạy thoát.");
};

onMounted(() => {
  charStore.fetchCharacter();
  if(questStore.fetchQuests) questStore.fetchQuests();
  updateDayNight(); // Cập nhật màu nền sáng/tối
  broadcastJoinMessage();
});

onActivated(() => {
  broadcastJoinMessage();
  updateDayNight();
});

onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
/* PAGE CONTAINER: Full màn hình, không scroll */
.explore-page {
  /* Xóa màu nền xám cũ, để trong suốt cho BG layer hiện lên */
  background-color: transparent; 
  padding: 10px;
  height: 100vh; 
  box-sizing: border-box;
  overflow: hidden;
  color: #eee;
  font-family: "Noto Serif TC", serif;
  position: relative;
}

/* --- 1. HỆ THỐNG BACKGROUND (MỚI THÊM) --- */
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; }
.mountain-bg { 
  position: absolute; inset: 0; 
  background-size: cover; background-position: center bottom; 
  opacity: 0.6; filter: sepia(10%) contrast(1.1); 
}
.wood-overlay { 
  position: absolute; inset: 0; 
  background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); 
  mix-blend-mode: multiply; 
  transition: background 2s ease; 
}
.wood-overlay.night-mode { 
  background: linear-gradient(to bottom, rgba(10, 5, 20, 0.85), rgba(0, 0, 0, 0.95)); 
}
.vignette { 
  position: absolute; inset: 0; 
  background: radial-gradient(circle, transparent 60%, #1a100d 100%); 
}

/* --- EXPLORE LAYOUT (Thêm z-index để nổi lên trên BG) --- */
.explore-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 15px;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  position: relative; /* Để nổi lên trên bg-layer */
  z-index: 10;
}

/* --- CỘT GIỮA --- */
.center-zone {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  overflow: hidden;
}

/* Game Board */
.game-board {
  flex: 1; 
  min-height: 0; 
  background: rgba(38, 24, 21, 0.9); /* Thêm độ trong suốt để hòa vào nền */
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 0 10px 20px rgba(0,0,0,0.5);
}

/* Chat Board */
.chat-board {
  height: 180px; 
  flex: none; 
  background: rgba(0, 0, 0, 0.6); /* Trong suốt hơn */
  border: 1px solid #444;
  border-radius: 8px;
  overflow: hidden;
}

/* Các thành phần bên trong Game Board */
.status-header {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.4);
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.level-badge span {
  font-weight: bold;
  color: #ffd700;
  border: 1px solid #ffd700;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
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
.progress-fill.hp { background: linear-gradient(to right, #c62828, #e53935); }
.progress-fill.energy { background: linear-gradient(to right, #1565c0, #42a5f5); }
.exp-row .exp-bg {
  height: 4px;
  background: #333;
  margin-top: 2px;
}
.exp-fill {
  height: 100%;
  background: #00e676;
  width: 0%;
}

.stat-text {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.65em;
  font-weight: bold;
  text-shadow: 1px 1px 0 #000;
  white-space: nowrap;
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
  width: 100%; height: 100%;
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

.avatar-circle, .avatar-target {
  width: 96px; height: 96px;
  display: flex; justify-content: center; align-items: center;
  filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5));
}

.avatar-img {
  width: 100%; height: 100%;
  object-fit: contain;
  transform: scale(1.2);
}

.actor-label {
  margin-top: 5px;
  background: rgba(0,0,0,0.6);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.target-name { color: #ffd700; }

.action-panel {
  height: 60px;
  background: #1a100e;
  border-top: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 15px;
  flex-shrink: 0;
}

.btn-action {
  border: none; border-radius: 6px;
  cursor: pointer; height: 40px;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; color: #fff;
}
.map-btn { flex: 1; background: #2c3e50; border: 1px solid #34495e; }
.main-btn { flex: 2; background: linear-gradient(to bottom, #4e342e, #3e2723); border: 1px solid #c5a059; color: #c5a059; }
.sub-btn { flex: 0.5; background: #3e2723; border: 1px solid #5d4037; }

/* --- CỘT PHẢI --- */
.right-zone {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
  overflow: hidden;
}

.log-panel {
  height: 35%; /* Cố định tỷ lệ */
  flex: none;
  background: rgba(30, 30, 30, 0.9); /* Trong suốt nhẹ */
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex; flex-direction: column;
}

.log-header {
  background: #3e2723; padding: 5px 10px;
  font-weight: bold; font-size: 0.9em; text-align: center;
  border-bottom: 1px solid #5d4037;
}

.log-content {
  flex: 1; padding: 8px; overflow-y: auto;
  background: rgba(17, 17, 17, 0.8);
  font-size: 0.85em;
}

.quest-panel-wrapper {
  flex: 1; /* Chiếm hết phần dưới */
  min-height: 0;
  background: rgba(30, 30, 30, 0.9);
  border: 2px solid #5d4037;
  border-radius: 8px;
  overflow: hidden;
  display: flex; flex-direction: column;
}
.quest-panel-wrapper :deep(> div) { height: 100%; overflow-y: auto; }

/* Modal Styles */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 3000;
  display: flex; align-items: center; justify-content: center;
}
.map-modal-card {
  width: 90%; max-width: 500px;
  background: #1a1a1a;
  border: 2px solid #d4af37;
  border-radius: 8px;
  padding: 15px; color: #fff;
}
.map-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 15px; }
.map-item {
  background: #333; padding: 10px;
  border-radius: 6px; cursor: pointer;
  border: 1px solid #444; position: relative;
}
.map-item.active { border-color: #00e676; background: #1b5e20; }
.map-item.locked { opacity: 0.6; cursor: not-allowed; }
.lock-icon { position: absolute; right: 10px; top: 10px; }
.close-btn { width: 100%; padding: 10px; background: #b71c1c; border: none; color: white; font-weight: bold; cursor: pointer; }

.encounter-modal {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 2000;
  display: flex; align-items: center; justify-content: center;
}
.modal-card {
  width: 350px; background: #261815;
  border: 2px solid #b71c1c; border-radius: 12px;
}
.modal-header { background: #b71c1c; color: #fff; padding: 10px; text-align: center; font-weight: bold; }
.modal-body { padding: 20px; text-align: center; }
.enemy-preview-img { width: 100px; height: 100px; object-fit: contain; }
.modal-footer { padding: 15px; display: flex; gap: 15px; }
.modal-btn { flex: 1; padding: 10px; cursor: pointer; font-weight: bold; }
.modal-btn.flee { background: #555; color: #ccc; }
.modal-btn.fight { background: #d32f2f; color: #fff; }

.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; }

@media (max-width: 900px) {
  .explore-layout { grid-template-columns: 1fr; }
  .right-zone { height: 400px; flex: none; }
}
</style> -->



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
            <div class="level-badge">
              <span>Lv.{{ charStore.character?.level || 1 }}</span>
            </div>
            <div class="bars-container">
              <div class="stat-group">
                <div class="stat-row">
                  <span class="stat-icon">❤️</span>
                  <div class="progress-bg">
                    <div
                      class="progress-fill hp"
                      :style="{ width: hpPercent + '%' }"
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
                      :style="{ width: energyPercent + '%' }"
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
                    :style="{ width: xpPercent + '%' }"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <div class="stage-viewport">
            <div
              class="stage-background"
              :style="{ backgroundImage: `url(${getMapBg})` }"
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
                    @error="handleImageError"
                  />
                  <div v-else class="fallback-icon">🎁</div>
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
                  <i class="fas fa-walking"></i>
                  <span v-if="!isMoving">HÀNH TẨU</span>
                  <span v-else>... ({{ countdown }}s)</span>
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
            <img :src="targetImage" class="enemy-preview-img" @error="handleImageError" />
          </div>
          <p>
            Gặp <strong>{{ targetName }}</strong
            >!
          </p>
        </div>
        <div class="modal-footer">
          <button class="modal-btn flee" @click="flee">Bỏ Chạy</button>
          <button class="modal-btn fight" @click="goToBattle">
            CHIẾN ĐẤU
          </button>
        </div>
      </div>
    </div>
    
    <CaptchaModal ref="captchaModal" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, onActivated, watch } from "vue";
import { useRouter } from "vue-router";
// Stores
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useBattleStore } from "@/stores/battleStore";
import { useQuestStore } from "@/stores/questStore";
import { useChatStore } from "@/stores/chatStore";
import { useInventoryStore } from "@/stores/inventoryStore"; // [QUAN TRỌNG]

// API & Utils
import axiosClient from "@/api/axiosClient";
import { getItemImage, getCurrentSkin, getAssetUrl, getEnemyImage } from "@/utils/assetHelper";

// Components
import CaptchaModal from "@/components/CaptchaModal.vue";
import ChatPanel from "@/components/ChatPanel.vue";
import QuestPanel from "@/components/QuestPanel.vue";

// --- INIT STORES ---
const charStore = useCharacterStore();
const authStore = useAuthStore();
const battleStore = useBattleStore();
const questStore = useQuestStore();
const chatStore = useChatStore();
const inventoryStore = useInventoryStore(); // [QUAN TRỌNG] Init store
const router = useRouter();
const captchaModal = ref(null);

// --- COMPUTED STATS ---
const hpPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min((charStore.character.currentHp / charStore.character.maxHp) * 100, 100);
});
const energyPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min((charStore.character.currentEnergy / charStore.character.maxEnergy) * 100, 100);
});
const xpPercent = computed(() => {
  if (!charStore.character) return 0;
  return Math.min((charStore.character.experience / charStore.character.nextLevelExp) * 100, 100);
});

// --- BACKGROUND LOGIC ---
const pageBgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- STATE ---
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

// Map Config
const maps = [
  { id: "MAP_01", name: "Đồng Bằng", minLv: 1, maxLv: 19 },
  { id: "MAP_02", name: "Rừng Rậm", minLv: 20, maxLv: 30 },
  { id: "MAP_03", name: "Sa Mạc", minLv: 30, maxLv: 40 },
  { id: "MAP_04", name: "Núi Cao", minLv: 40, maxLv: 50 },
  { id: "MAP_05", name: "Băng Đảo", minLv: 50, maxLv: 60 },
  { id: "MAP_06", name: "Đầm Lầy", minLv: 60, maxLv: 70 },
];
const currentMapName = computed(
  () => maps.find((m) => m.id === currentMapId.value)?.name || "Đồng Bằng"
);

// Đồng bộ Map
watch(
  () => charStore.character,
  (newChar) => {
    if (newChar && newChar.currentMapId) {
      const savedMap = maps.find((m) => m.id === newChar.currentMapId);
      if (savedMap) currentMapId.value = newChar.currentMapId;
    }
  },
  { immediate: true }
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

const getMapBg = computed(() => getAssetUrl("b_doanhtrai.png"));

const imgPlayer = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return isMoving.value ? skin.sprites.run : skin.sprites.idle;
});

// Logic Nhật ký
let moveInterval = null;
const getTime = () => new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
const addLog = (msg) => logs.value.unshift({ time: getTime(), msg });

// Animation chạy
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
  const phrases = [
    `${username} Vừa xuất quan.`, `${username} Đã khởi hành.`,
    `${username} Đã dấn thân vào hồng trần.`, `${username} đã đạp gió lên đường.`
  ];
  const randomPhrase = phrases[Math.floor(Math.random() * phrases.length)];
  try {
    await axiosClient.post('/chat/send', { content: randomPhrase });
    chatStore.addMessage({
        senderName: username,
        senderAvatar: authStore.user?.avatarUrl,
        content: randomPhrase,
        timestamp: new Date().toISOString(),
        role: authStore.user?.role || 'USER'
    });
  } catch (e) { console.warn(e); }
};

// [FIX] Helper: Map tên tiếng Việt sang Code ảnh (Tạm thời)
const mapVietnameseToCode = (name) => {
    const n = name.toLowerCase();
    if (n.includes("gỗ")) return "r_wood";
    if (n.includes("đá")) return "r_stone";
    if (n.includes("sắt")) return "r_iron";
    if (n.includes("thảo dược")) return "r_herb";
    if (n.includes("vải")) return "r_cloth";
    if (n.includes("da")) return "r_leather";
    if (n.includes("vàng")) return "r_coin";
    return name; // Nếu không map được thì trả về nguyên gốc
};

// [FIX] Xử lý khi ảnh lỗi -> Chuyển về icon quà
const handleImageError = (e) => {
    e.target.src = getAssetUrl("resources/r_gift.png"); // Hoặc một ảnh mặc định an toàn
    // Nếu không có ảnh r_gift, nó sẽ bị lỗi tiếp, nên ẩn đi và hiện div fallback
    e.target.style.display = 'none'; 
    // Logic fallback trong template: v-else sẽ hiển thị icon 🎁
    targetImage.value = null; 
};

// BẮT ĐẦU HÀNH TẨU
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

// XỬ LÝ KẾT QUẢ
const handleResult = async () => {
  clearInterval(moveInterval);
  isMoving.value = false;

  try {
    const res = await charStore.explore({ mapId: currentMapId.value });
    await charStore.fetchCharacter();

    if (res.type === "GATHERING") {
      addLog(`<span style="color:#00e676; font-weight:bold;">🌿 ${res.message}</span>`);
      setTimeout(() => { router.push("/gathering"); }, 500);
      return;
    }

    if (res.type === "ITEM" && res.rewardName) {
      showTarget.value = true;
      targetName.value = res.rewardName;
      
      // [FIX] Logic lấy ảnh thông minh hơn
      // 1. Ưu tiên code từ server. 2. Nếu không, map từ tên TV. 3. Fallback về tên gốc.
      let codeToUse = res.rewardCode;
      if (!codeToUse) codeToUse = mapVietnameseToCode(res.rewardName);
      
      // Thử lấy ảnh
      targetImage.value = getItemImage(codeToUse); 
      
      // [QUAN TRỌNG] Cập nhật Túi đồ để item "vào túi" thật
      await inventoryStore.fetchInventory();

      if (questStore.checkQuestCompletion) {
        const rewardGold = questStore.checkQuestCompletion(res.rewardName);
        if (rewardGold > 0) {
            addLog(`<span style="color:#00e676;">${res.message}</span>`);
            addLog(`<span style="color:#ffd700; font-weight:bold;">💰 Xong nhiệm vụ: +${rewardGold} Vàng!</span>`);
            if(charStore.character) charStore.character.gold += rewardGold;
        } else {
            addLog(`<span style="color:#00e676;">${res.message}</span>`);
        }
      } else {
        addLog(`<span style="color:#00e676;">${res.message}</span>`);
      }

    } 
    else if (res.type === "ENEMY") {
      isEncounter.value = true;
      showTarget.value = true;
      targetName.value = res.rewardName;
      targetImage.value = getEnemyImage(res.rewardName); 
      
      battleStore.setEncounter({
        name: res.rewardName,
        img: targetImage.value,
      });
      addLog(`<span style="color:#ef5350;">⚠️ ${res.message}</span>`);
    } 
    else {
      addLog(`<span style="color:#aaa;">${res.message}</span>`);
    }

  } catch (e) {
    if (e.message === "CAPTCHA") {
      captchaModal.value.open();
    } else {
      const errorMsg = e.response?.data?.message || e.message;
      addLog(`<span style="color:red">Lỗi: ${errorMsg}</span>`);
    }
  }
};

const goToBattle = () => router.push("/battle");
const flee = () => {
  isEncounter.value = false;
  showTarget.value = false;
  addLog("Đã chạy thoát.");
};

onMounted(() => {
  charStore.fetchCharacter();
  if(questStore.fetchQuests) questStore.fetchQuests();
  if(inventoryStore.fetchInventory) inventoryStore.fetchInventory(); // Fetch lần đầu
  updateDayNight();
  broadcastJoinMessage();
});

onActivated(() => {
  broadcastJoinMessage();
  updateDayNight();
});

onUnmounted(() => clearInterval(moveInterval));
</script>

<style scoped>
/* PAGE CONTAINER: Full màn hình, không scroll */
.explore-page {
  background-color: transparent; 
  padding: 10px;
  height: 100vh; 
  box-sizing: border-box;
  overflow: hidden;
  color: #eee;
  font-family: "Noto Serif TC", serif;
  position: relative;
}

/* --- 1. HỆ THỐNG BACKGROUND --- */
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; }
.mountain-bg { 
  position: absolute; inset: 0; 
  background-size: cover; background-position: center bottom; 
  opacity: 0.6; filter: sepia(10%) contrast(1.1); 
}
.wood-overlay { 
  position: absolute; inset: 0; 
  background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); 
  mix-blend-mode: multiply; 
  transition: background 2s ease; 
}
.wood-overlay.night-mode { 
  background: linear-gradient(to bottom, rgba(10, 5, 20, 0.85), rgba(0, 0, 0, 0.95)); 
}
.vignette { 
  position: absolute; inset: 0; 
  background: radial-gradient(circle, transparent 60%, #1a100d 100%); 
}

/* --- EXPLORE LAYOUT --- */
.explore-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 15px;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  position: relative; 
  z-index: 10;
}

/* --- CỘT GIỮA --- */
.center-zone {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  overflow: hidden;
}

/* Game Board */
.game-board {
  flex: 1; 
  min-height: 0; 
  background: rgba(38, 24, 21, 0.9); 
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 0 10px 20px rgba(0,0,0,0.5);
}

/* Chat Board */
.chat-board {
  height: 180px; 
  flex: none; 
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #444;
  border-radius: 8px;
  overflow: hidden;
}

/* Các thành phần bên trong Game Board */
.status-header {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.4);
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.level-badge span {
  font-weight: bold;
  color: #ffd700;
  border: 1px solid #ffd700;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
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
.progress-fill.hp { background: linear-gradient(to right, #c62828, #e53935); }
.progress-fill.energy { background: linear-gradient(to right, #1565c0, #42a5f5); }
.exp-row .exp-bg {
  height: 4px;
  background: #333;
  margin-top: 2px;
}
.exp-fill {
  height: 100%;
  background: #00e676;
  width: 0%;
}

.stat-text {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.65em;
  font-weight: bold;
  text-shadow: 1px 1px 0 #000;
  white-space: nowrap;
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
  width: 100%; height: 100%;
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

.avatar-circle, .avatar-target {
  width: 96px; height: 96px;
  display: flex; justify-content: center; align-items: center;
  filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5));
}

/* Fallback icon khi ảnh lỗi */
.fallback-icon {
  font-size: 3rem;
}

.avatar-img {
  width: 100%; height: 100%;
  object-fit: contain;
  transform: scale(1.2);
}

.actor-label {
  margin-top: 5px;
  background: rgba(0,0,0,0.6);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.target-name { color: #ffd700; }

.action-panel {
  height: 60px;
  background: #1a100e;
  border-top: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 15px;
  flex-shrink: 0;
}

.btn-action {
  border: none; border-radius: 6px;
  cursor: pointer; height: 40px;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; color: #fff;
}
.map-btn { flex: 1; background: #2c3e50; border: 1px solid #34495e; }
.main-btn { flex: 2; background: linear-gradient(to bottom, #4e342e, #3e2723); border: 1px solid #c5a059; color: #c5a059; }
.sub-btn { flex: 0.5; background: #3e2723; border: 1px solid #5d4037; }

/* --- CỘT PHẢI --- */
.right-zone {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
  overflow: hidden;
}

.log-panel {
  height: 35%; 
  flex: none;
  background: rgba(30, 30, 30, 0.9); 
  border: 2px solid #5d4037;
  border-radius: 8px;
  display: flex; flex-direction: column;
}

.log-header {
  background: #3e2723; padding: 5px 10px;
  font-weight: bold; font-size: 0.9em; text-align: center;
  border-bottom: 1px solid #5d4037;
}

.log-content {
  flex: 1; padding: 8px; overflow-y: auto;
  background: rgba(17, 17, 17, 0.8);
  font-size: 0.85em;
}

.quest-panel-wrapper {
  flex: 1; 
  min-height: 0;
  background: rgba(30, 30, 30, 0.9);
  border: 2px solid #5d4037;
  border-radius: 8px;
  overflow: hidden;
  display: flex; flex-direction: column;
}
.quest-panel-wrapper :deep(> div) { height: 100%; overflow-y: auto; }

/* Modal Styles */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 3000;
  display: flex; align-items: center; justify-content: center;
}
.map-modal-card {
  width: 90%; max-width: 500px;
  background: #1a1a1a;
  border: 2px solid #d4af37;
  border-radius: 8px;
  padding: 15px; color: #fff;
}
.map-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 15px; }
.map-item {
  background: #333; padding: 10px;
  border-radius: 6px; cursor: pointer;
  border: 1px solid #444; position: relative;
}
.map-item.active { border-color: #00e676; background: #1b5e20; }
.map-item.locked { opacity: 0.6; cursor: not-allowed; }
.lock-icon { position: absolute; right: 10px; top: 10px; }
.close-btn { width: 100%; padding: 10px; background: #b71c1c; border: none; color: white; font-weight: bold; cursor: pointer; }

.encounter-modal {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 2000;
  display: flex; align-items: center; justify-content: center;
}
.modal-card {
  width: 350px; background: #261815;
  border: 2px solid #b71c1c; border-radius: 12px;
}
.modal-header { background: #b71c1c; color: #fff; padding: 10px; text-align: center; font-weight: bold; }
.modal-body { padding: 20px; text-align: center; }
.enemy-preview-img { width: 100px; height: 100px; object-fit: contain; }
.modal-footer { padding: 15px; display: flex; gap: 15px; }
.modal-btn { flex: 1; padding: 10px; cursor: pointer; font-weight: bold; }
.modal-btn.flee { background: #555; color: #ccc; }
.modal-btn.fight { background: #d32f2f; color: #fff; }

.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; }

@media (max-width: 900px) {
  .explore-layout { grid-template-columns: 1fr; }
  .right-zone { height: 400px; flex: none; }
}
</style>