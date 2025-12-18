<template>
  <div class="page-container gathering-page dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="nav-header">
      <button class="btn-wood-back" @click="$router.push('/explore')">
        <i class="fas fa-chevron-left"></i> RỜI KHỎI
      </button>
    </div>

    <div class="gathering-panel" v-if="currentEvent">
      <div class="event-header">
        <div class="node-frame">
          <div class="node-circle" :class="{ 'shake-anim': isGathering }">
            <img :src="currentEvent.image" class="node-img" />
          </div>
          <div class="rarity-seal" :class="'bg-' + currentEvent.rarityClass">
            {{ currentEvent.rarityText }}
          </div>
        </div>

        <h2 class="node-name" :class="'text-' + currentEvent.rarityClass">
          {{ currentEvent.name }}
        </h2>

        <div class="req-box">
          <i class="fas fa-exclamation-triangle"></i>
          Yêu cầu:
          <span class="highlight">{{ currentEvent.reqTool }}</span> (Cấp
          {{ currentEvent.reqLevel }})
        </div>
      </div>

      <div class="info-scroll-area">
        <div class="status-row">
          <div class="icon-wrap"><i class="fas fa-graduation-cap"></i></div>
          <div class="stat-detail">
            <span class="label">Kỹ năng</span>
            <span
              class="val"
              :class="{ 'text-red': playerLevel < currentEvent.reqLevel }"
            >
              Cấp {{ playerLevel }} / {{ currentEvent.reqLevel }}
            </span>
          </div>
        </div>

        <div class="status-row">
          <div class="icon-wrap"><i class="fas fa-cubes"></i></div>
          <div class="stat-detail">
            <span class="label">Trữ lượng</span>
            <span class="val gold-text"
              >{{ remainingNode }} / {{ maxNode }}</span
            >
          </div>
        </div>
      </div>

      <div class="progress-container">
        <div class="progress-label">
          <span>Tiến độ</span>
          <span>{{ Math.round((1 - remainingNode / maxNode) * 100) }}%</span>
        </div>
        <div class="progress-track">
          <div
            class="progress-fill"
            :style="{ width: (1 - remainingNode / maxNode) * 100 + '%' }"
          ></div>
        </div>
      </div>

      <div class="action-zone">
        <div class="energy-tag" :class="{ 'energy-low': (charStore.character?.currentEnergy || 0) < 1 }">
          <i class="fas fa-bolt"></i> Nội Năng:
          <strong>{{ charStore.character?.currentEnergy || 0 }}</strong>
        </div>

        <div class="btn-grid">
          <button
            class="btn-wood action-btn"
            @click="handleGather(1)"
            :disabled="
              isGathering ||
              remainingNode <= 0 ||
              (charStore.character?.currentEnergy || 0) < 1 ||
              playerLevel < currentEvent.reqLevel
            "
            :title="
              playerLevel < currentEvent.reqLevel
                ? 'Cấp độ chưa đủ!'
                : (charStore.character?.currentEnergy || 0) < 1
                ? 'Không đủ Nội Năng!'
                : 'Khai thác 1 lần'
            "
          >
            <span class="btn-main">KHAI THÁC</span>
            <span class="btn-sub">Tốn 1 <i class="fas fa-bolt"></i></span>
          </button>

          <button
            class="btn-seal action-btn"
            @click="handleGatherAll"
            :disabled="
              isGathering ||
              remainingNode <= 0 ||
              (charStore.character?.currentEnergy || 0) < 1 ||
              playerLevel < currentEvent.reqLevel
            "
            :title="
              'Khai thác tối đa 10 lần'
            "
          >
            <span class="btn-main">TỰ ĐỘNG</span>
            <span class="btn-sub">Gom nhanh (Max 10)</span>
          </button>
        </div>

        <div class="feedback-text" v-if="feedbackMsg">
          {{ feedbackMsg }}
        </div>

        <div class="energy-warning" v-if="(charStore.character?.currentEnergy || 0) < 1 && !isGathering">
          <i class="fas fa-exclamation-circle"></i>
          Nội năng đã cạn! Hãy nghỉ ngơi hoặc sử dụng vật phẩm để hồi phục.
        </div>
      </div>
    </div>

    <div class="floating-layer">
      <transition-group name="float-up">
        <div v-for="loot in floatingLoots" :key="loot.id" class="float-item">
          <span class="plus">+{{ loot.amount }}</span> {{ loot.name }}
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useRouter } from "vue-router";
import axiosClient from "@/api/axiosClient";

// --- IMPORT ẢNH ---
import woodImg from "@/assets/resources/r_wood.png";          // Gỗ Xoài
import stoneImg from "@/assets/resources/stone_1.png";        // Đá
import copperImg from "@/assets/resources/r_copper_node.png"; // Quặng Đồng
import ironImg from "@/assets/resources/r_silver_node.png";   // Sắt
import fishImg from "@/assets/resources/r_fish.png";          // Cá
import mystrileImg from "@/assets/resources/r_mystrile_node.png"; // Bạch Kim
import redWoodImg from "@/assets/resources/r_red_wood.png";   // Gỗ Khô

const charStore = useCharacterStore();
const authStore = useAuthStore();
const router = useRouter();
let energyRefreshInterval = null;

const currentEvent = ref(null);
const remainingNode = ref(0);
const maxNode = ref(0);
const isGathering = ref(false);
const feedbackMsg = ref("");
const floatingLoots = ref([]);

const playerLevel = computed(() => charStore.character?.lv || 1);

// --- CẤU HÌNH DATA TÀI NGUYÊN ---
const EVENT_TYPES = [
  {
    id: "wood",       
    rewardItemId: 1,  // ID 1: Gỗ Xoài
    name: "Cây Gỗ Xoài",
    image: woodImg,
    rarityClass: "common",
    rarityText: "Phổ Thông",
    reqLevel: 1,
    reqTool: "Rìu",
    minYield: 5,
    maxYield: 15,
    lootName: "Gỗ Xoài",
  },
  {
    id: "stone",      
    rewardItemId: 2,  // ID 2: Đá
    name: "Mỏ Đá Tự Nhiên",
    image: stoneImg,
    rarityClass: "common",
    rarityText: "Phổ Thông",
    reqLevel: 1,
    reqTool: "Búa",
    minYield: 5,
    maxYield: 15,
    lootName: "Đá",
  },
  {
    id: "mining",     
    rewardItemId: 3,  // ID 3: Quặng Đồng
    name: "Mạch Đồng Lộ Thiên",
    image: copperImg,
    rarityClass: "common",
    rarityText: "Phổ Thông",
    reqLevel: 1,
    reqTool: "Cuốc",
    minYield: 3,
    maxYield: 10,
    lootName: "Quặng Đồng",
  },
  {
    id: "mining",
    rewardItemId: 4,  // ID 4: Sắt
    name: "Mỏ Sắt Già",
    image: ironImg,
    rarityClass: "rare", 
    rarityText: "Hiếm",
    reqLevel: 20,
    reqTool: "Cuốc Sắt",
    minYield: 2,
    maxYield: 8,
    lootName: "Sắt",
  },
  {
    id: "fishing",
    rewardItemId: 5,  // ID 5: Cá
    name: "Hồ Cá Yên Bình",
    image: fishImg,
    rarityClass: "common",
    rarityText: "Phổ Thông",
    reqLevel: 1,
    reqTool: "Cần Câu",
    minYield: 1,
    maxYield: 5,
    lootName: "Cá",
  },
  {
    id: "mining",
    rewardItemId: 6,  // ID 6: Bạch Kim
    name: "Tinh Thể Bạch Kim",
    image: mystrileImg,
    rarityClass: "epic",
    rarityText: "Cực Phẩm",
    reqLevel: 40,
    reqTool: "Găng Tay",
    minYield: 1,
    maxYield: 3,
    lootName: "Bạch Kim",
  },
  {
    id: "wood",
    rewardItemId: 7,  // ID 7: Gỗ Khô
    name: "Cây Gỗ Khô",
    image: redWoodImg,
    rarityClass: "common",
    rarityText: "Phổ Thông",
    reqLevel: 30, 
    reqTool: "Rìu",
    minYield: 5,
    maxYield: 10,
    lootName: "Gỗ Khô",
  }
];

const initEvent = () => {
  const rnd = Math.floor(Math.random() * EVENT_TYPES.length);
  const evt = EVENT_TYPES[rnd];
  
  currentEvent.value = evt;

  const amount =
    Math.floor(Math.random() * (evt.maxYield - evt.minYield + 1)) +
    evt.minYield;
  maxNode.value = amount;
  remainingNode.value = amount;
  feedbackMsg.value = `Phát hiện: ${evt.name}`;
};

const handleGather = async (times) => {
  if (isGathering.value) return;

  if (!charStore.character) {
     feedbackMsg.value = "⚠️ Đang tải dữ liệu nhân vật...";
     return;
  }
  
  // Kiểm tra cấp độ
  if (playerLevel.value < currentEvent.value.reqLevel) {
    feedbackMsg.value = `⛔ Cấp độ chưa đủ! Cần cấp ${currentEvent.value.reqLevel}.`;
    return;
  }

  const cost = times;
  if ((charStore.character?.currentEnergy || 0) < cost) {
    feedbackMsg.value = "⚠️ Nội năng không đủ!";
    return;
  }

  isGathering.value = true;
  feedbackMsg.value = times > 1 ? "Đang vận công..." : "Đang khai thác...";

  const actualGathered = Math.min(times, remainingNode.value);

  try {
    const charId = charStore.character._id || charStore.character.id;

    // [FIX QUAN TRỌNG] Gửi đúng key 'itemId' để khớp với Controller Backend
    const payload = {
      // Backend không đọc characterId từ body trong đoạn code bạn gửi, 
      // nhưng mình vẫn để đây phòng khi logic Service cần.
      characterId: charId,   
      
      // Backend check: if (req.get("itemId") == null)
      // Nên key ở đây BẮT BUỘC phải là 'itemId'
      itemId: currentEvent.value.rewardItemId, 
      
      amount: actualGathered,
    };

    const res = await axiosClient.post("/exploration/gather", payload);

    remainingNode.value -= actualGathered;

    // Đồng bộ lại data
    await charStore.syncGameData();

    if (actualGathered > 0) {
      showFloatingText(`+${actualGathered} ${currentEvent.value.lootName}`);
      feedbackMsg.value = `Thu hoạch thành công!`;
    }

    if (remainingNode.value <= 0) {
      feedbackMsg.value = "Mỏ tài nguyên đã cạn!";
      setTimeout(() => {
        router.push("/explore");
      }, 1500);
    }
  } catch (error) {
    console.error(error);
    const serverMsg = error.response?.data?.message || error.response?.data;
    // Hiển thị lỗi chi tiết trả về từ Backend (nếu có)
    feedbackMsg.value = typeof serverMsg === 'string' ? serverMsg : "Lỗi khai thác!";
  } finally {
    isGathering.value = false;
  }
};

const handleGatherAll = () => {
  const currentEnergy = charStore.character?.currentEnergy || 0;
  const times = Math.min(10, remainingNode.value, currentEnergy);

  if (times > 0) {
    handleGather(times);
  } else {
    feedbackMsg.value = "⚠️ Nội năng cạn kiệt!";
  }
};

const showFloatingText = (text) => {
  const id = Date.now();
  floatingLoots.value.push({
    id,
    amount: text.split(" ")[0],
    name: text.substring(text.indexOf(" ")),
  });
  setTimeout(() => {
    floatingLoots.value = floatingLoots.value.filter((i) => i.id !== id);
  }, 1500);
};

onMounted(async () => {
  await charStore.syncGameData(); 
  await authStore.fetchProfile();
  initEvent();

  energyRefreshInterval = setInterval(async () => {
    await charStore.fetchCharacter(); 
  }, 5000);
});

onUnmounted(() => {
  if (energyRefreshInterval) {
    clearInterval(energyRefreshInterval);
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- PALETTE --- */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --bg-dark: #212121;
  --gold: #ffecb3;
  --red-seal: #b71c1c;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --jade: #43a047;
}

/* --- BASE LAYOUT --- */
.dark-theme {
  background-color: var(--bg-dark);
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

/* Background Layers */
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
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}
.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #261815 10%, transparent 90%);
}

/* NAVIGATION */
.nav-header {
  width: 100%;
  max-width: 500px;
  margin-bottom: 20px;
  position: relative;
  z-index: 10;
}

.btn-wood-back {
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid var(--wood-light);
  color: var(--text-dim);
  padding: 8px 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: inherit;
  font-weight: bold;
  transition: 0.3s;
}
.btn-wood-back:hover {
  background: var(--wood-light);
  color: var(--gold);
  border-color: var(--gold);
}

/* --- MAIN PANEL --- */
.gathering-panel {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 450px;
  background: rgba(30, 20, 15, 0.95);
  border: 4px double var(--wood-light);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.8);
  padding: 25px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* EVENT HEADER */
.event-header {
  text-align: center;
  position: relative;
}

.node-frame {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 15px;
}

.node-circle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.1) 0%,
    rgba(0, 0, 0, 0.5) 80%
  );
  border: 3px solid var(--gold);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 20px rgba(255, 236, 179, 0.2);
}
.node-img {
  width: 70%;
  height: 70%;
  object-fit: contain;
}

.rarity-seal {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  padding: 2px 10px;
  font-size: 0.75rem;
  font-weight: bold;
  border-radius: 10px;
  border: 1px solid #fff;
  white-space: nowrap;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);
  text-transform: uppercase;
}
.bg-common {
  background: #757575;
  color: #fff;
}
.bg-rare {
  background: #1976d2;
  color: #fff;
}
.bg-epic {
  background: #7b1fa2;
  color: #fff;
  border-color: var(--gold);
}

.node-name {
  font-size: 1.6rem;
  font-weight: 900;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
  text-transform: uppercase;
  letter-spacing: 1px;
}
.text-common {
  color: var(--text-dim);
}
.text-rare {
  color: #42a5f5;
}
.text-epic {
  color: #ab47bc;
  text-shadow: 0 0 10px rgba(171, 71, 188, 0.6);
}

.req-box {
  margin-top: 5px;
  font-size: 0.9rem;
  color: #aaa;
  font-style: italic;
}
.highlight {
  color: var(--gold);
  font-weight: bold;
}

/* STATUS AREA */
.info-scroll-area {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--wood-light);
  padding: 10px;
  border-radius: 4px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}
.status-row:last-child {
  border-bottom: none;
}

.icon-wrap {
  width: 30px;
  text-align: center;
  color: var(--gold);
  font-size: 1.1rem;
}
.stat-detail {
  flex: 1;
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
}
.label {
  color: #aaa;
}
.val {
  font-weight: bold;
  color: var(--text-light);
}
.text-red {
  color: #ef5350;
}
.gold-text {
  color: var(--gold);
}

/* PROGRESS */
.progress-container {
  margin-top: 5px;
}
.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: #ccc;
  margin-bottom: 5px;
}
.progress-track {
  height: 12px;
  background: #1a1a1a;
  border: 1px solid var(--wood-light);
  border-radius: 6px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #43a047, #66bb6a);
  box-shadow: 0 0 10px rgba(67, 160, 71, 0.5);
  transition: width 0.3s ease;
}

/* ACTIONS */
.action-zone {
  text-align: center;
  margin-top: 10px;
}
.energy-tag {
  display: inline-block;
  background: rgba(255, 255, 255, 0.05);
  padding: 4px 12px;
  border-radius: 15px;
  color: #4fc3f7;
  font-weight: bold;
  font-size: 0.9rem;
  margin-bottom: 15px;
  border: 1px solid rgba(79, 195, 247, 0.3);
  transition: all 0.3s ease;
}

.energy-tag.energy-low {
  color: #ff5252;
  border-color: rgba(255, 82, 82, 0.5);
  background: rgba(255, 82, 82, 0.1);
  animation: pulse-red 2s infinite;
}

.energy-warning {
  margin-top: 12px;
  padding: 10px 15px;
  background: rgba(255, 82, 82, 0.15);
  border: 1px solid rgba(255, 82, 82, 0.4);
  border-radius: 8px;
  color: #ff8a80;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: slideDown 0.3s ease;
}

.energy-warning i {
  font-size: 1rem;
  animation: pulse 2s infinite;
}

@keyframes pulse-red {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.btn-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.action-btn {
  padding: 12px;
  border: none;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 4px;
  transition: 0.2s;
  position: relative;
}
.action-btn:active:not(:disabled) {
  transform: translateY(2px);
}
.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(1);
}

.btn-main {
  font-weight: 900;
  font-size: 1rem;
  margin-bottom: 2px;
}
.btn-sub {
  font-size: 0.75rem;
  opacity: 0.8;
}

.btn-wood {
  background: var(--wood-light);
  color: var(--gold);
  border: 1px solid var(--gold);
  box-shadow: 0 4px 0 #3e2723;
}
.btn-wood:hover:not(:disabled) {
  background: var(--wood-dark);
}

.btn-seal {
  background: var(--red-seal);
  color: #fff;
  border: 1px solid #d32f2f;
  box-shadow: 0 4px 0 #8b0000;
}
.btn-seal:hover:not(:disabled) {
  background: #d32f2f;
}

.feedback-text {
  margin-top: 15px;
  height: 20px;
  font-size: 0.9rem;
  color: var(--jade);
  font-weight: bold;
  font-style: italic;
  animation: fadeIn 0.3s;
}

/* FLOATING LOOT */
.floating-layer {
  position: absolute;
  top: 35%;
  left: 50%;
  transform: translateX(-50%);
  pointer-events: none;
  width: 100%;
  text-align: center;
  z-index: 100;
}
.float-item {
  font-size: 1.8rem;
  font-weight: 900;
  color: var(--gold);
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.9);
  margin-bottom: 5px;
  font-family: "Cinzel", serif;
}
.plus {
  color: #fff;
}

/* ANIMATIONS */
.shake-anim {
  animation: shake 0.5s infinite;
}
@keyframes shake {
  0% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-5deg);
  }
  75% {
    transform: rotate(5deg);
  }
  100% {
    transform: rotate(0deg);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.float-up-enter-active {
  animation: floatUp 1s ease-out forwards;
}
@keyframes floatUp {
  0% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-60px) scale(1.2);
  }
}

/* RESPONSIVE */
@media (max-width: 480px) {
  .gathering-panel {
    padding: 15px;
  }
  .btn-grid {
    grid-template-columns: 1fr;
  }
  .action-btn {
    flex-direction: row;
    justify-content: space-between;
    padding: 15px;
  }
}
</style>