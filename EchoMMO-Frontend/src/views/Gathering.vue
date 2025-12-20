<template>
  <div class="page-container gathering-page dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
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
          <i class="fas fa-exclamation-triangle"></i> Yêu cầu:
          <span class="highlight">{{ currentEvent.reqTool }}</span> (Cấp {{ currentEvent.reqLevel }})
        </div>
      </div>

      <div class="info-scroll-area">
        <div class="status-row">
          <div class="icon-wrap"><i class="fas fa-graduation-cap"></i></div>
          <div class="stat-detail">
            <span class="label">Kỹ năng</span>
            <span class="val" :class="{ 'text-red': playerLevel < currentEvent.reqLevel }">
              Cấp {{ playerLevel }} / {{ currentEvent.reqLevel }}
            </span>
          </div>
        </div>
        <div class="status-row">
          <div class="icon-wrap"><i class="fas fa-cubes"></i></div>
          <div class="stat-detail">
            <span class="label">Trữ lượng</span>
            <span class="val gold-text">{{ remainingNode }} / {{ maxNode }}</span>
          </div>
        </div>
      </div>

      <div class="progress-container">
        <div class="progress-label">
          <span>Tiến độ</span>
          <span>{{ maxNode > 0 ? Math.round((1 - remainingNode / maxNode) * 100) : 100 }}%</span>
        </div>
        <div class="progress-track">
          <div class="progress-fill"
            :style="{ width: (maxNode > 0 ? (1 - remainingNode / maxNode) * 100 : 100) + '%' }"></div>
        </div>
      </div>

      <div class="action-zone">
        <div class="energy-tag" :class="{ 'energy-low': (charStore.character?.currentEnergy || 0) < 1 }">
          <i class="fas fa-bolt"></i> Nội Năng: <strong>{{ charStore.character?.currentEnergy || 0 }}</strong>
        </div>
        <div class="btn-grid">
          <button class="btn-wood action-btn" @click="handleGather(1)"
            :disabled="isGathering || remainingNode <= 0 || (charStore.character?.currentEnergy || 0) < 1 || playerLevel < currentEvent.reqLevel">
            <span class="btn-main">KHAI THÁC</span>
            <span class="btn-sub">Tốn 1 <i class="fas fa-bolt"></i></span>
          </button>
          <button class="btn-seal action-btn" @click="handleGatherAll"
            :disabled="isGathering || remainingNode <= 0 || (charStore.character?.currentEnergy || 0) < 1 || playerLevel < currentEvent.reqLevel">
            <span class="btn-main">TỰ ĐỘNG</span>
            <span class="btn-sub">Gom nhanh (Max 10)</span>
          </button>
        </div>
        <div class="feedback-text" v-if="feedbackMsg">{{ feedbackMsg }}</div>
      </div>
    </div>

    <div v-else class="gathering-panel" style="text-align: center; color: #aaa;">
      <p>Đang đồng bộ dữ liệu mỏ...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useRouter } from "vue-router";
import axiosClient from "@/api/axiosClient";
import { resolveItemImage, getAssetUrl } from "@/utils/assetHelper";

const charStore = useCharacterStore();
const authStore = useAuthStore();
const router = useRouter();
let energyRefreshInterval = null;

const currentEvent = ref(null);
const remainingNode = ref(0);
const maxNode = ref(10);
const isGathering = ref(false);
const feedbackMsg = ref("");
const bgImage = getAssetUrl("b_mountain.jpg");

const playerLevel = computed(() => charStore.character?.lv || 1);

// Dùng resolveItemImage để lấy ảnh đúng từ github.io
const EVENT_TYPES = [
  // Gỗ
  { id: "wood", rewardItemId: 1, name: "Cây Gỗ Sồi", image: resolveItemImage("r_wood.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Rìu", lootName: "Gỗ Sồi" },
  { id: "dried_wood", rewardItemId: 2, name: "Cây Gỗ Khô", image: resolveItemImage("r_red_wood.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Rìu", lootName: "Gỗ Khô" },
  { id: "cold_wood", rewardItemId: 3, name: "Cây Gỗ Lạnh", image: resolveItemImage("r_white_wood.png"), rarityClass: "uncommon", rarityText: "Ít Gặp", reqLevel: 10, reqTool: "Rìu Sắt", lootName: "Gỗ Lạnh" },
  { id: "strange_wood", rewardItemId: 4, name: "Cây Gỗ Lạ", image: resolveItemImage("r_black_wood.png"), rarityClass: "rare", rarityText: "Hiếm", reqLevel: 20, reqTool: "Rìu Chiến", lootName: "Gỗ Lạ" },

  // Khoáng sản
  { id: "stone", rewardItemId: 5, name: "Mỏ Đá", image: resolveItemImage("stone_1.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Búa", lootName: "Đá" },
  { id: "copper", rewardItemId: 6, name: "Mạch Đồng", image: resolveItemImage("r_copper_node.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 5, reqTool: "Cuốc", lootName: "Quặng Đồng" },
  { id: "iron", rewardItemId: 7, name: "Mỏ Sắt", image: resolveItemImage("r_silver_node.png"), rarityClass: "rare", rarityText: "Hiếm", reqLevel: 20, reqTool: "Cuốc Sắt", lootName: "Quặng Sắt" },
  { id: "platinum", rewardItemId: 8, name: "Tinh Thể Bạch Kim", image: resolveItemImage("r_mystrile_node.png"), rarityClass: "epic", rarityText: "Cực Phẩm", reqLevel: 40, reqTool: "Găng Tay", lootName: "Bạch Kim" },

  // Khác
  { id: "fish", rewardItemId: 9, name: "Hồ Cá", image: resolveItemImage("r_fish.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Cần Câu", lootName: "Cá" },
  { id: "shark", rewardItemId: 10, name: "Vùng Nước Nguy Hiểm", image: resolveItemImage("r_shark.png"), rarityClass: "uncommon", rarityText: "Nguy Hiểm", reqLevel: 30, reqTool: "Cần Câu Máy", lootName: "Cá Mập" },
  { id: "coin", rewardItemId: 11, name: "Kho Báu Cổ", image: resolveItemImage("r_echo_coin.png"), rarityClass: "legendary", rarityText: "Huyền Thoại", reqLevel: 50, reqTool: "Tay Không", lootName: "Echo Coin" },
  { id: "unknown", rewardItemId: 12, name: "Vật Thể Lạ", image: resolveItemImage("r_mystrile_bar.png"), rarityClass: "epic", rarityText: "Bí Ẩn", reqLevel: 45, reqTool: "Găng Tay", lootName: "Nguyên liệu lạ" }
];

const initEvent = () => {
  if (!charStore.character) return;
  const dbItemId = charStore.character.gatheringItemId;
  const dbAmount = charStore.character.gatheringRemainingAmount;

  if (!dbItemId || dbItemId === 0) {
    router.push('/explore');
    return;
  }

  const evt = EVENT_TYPES.find(e => e.rewardItemId === dbItemId);
  if (evt) {
    currentEvent.value = evt;
    remainingNode.value = dbAmount || 0;
    maxNode.value = Math.max(dbAmount, 10);
  } else {
    currentEvent.value = EVENT_TYPES[0]; // Fallback
  }
};

const handleGather = async (times = 1) => {
  if (isGathering.value || remainingNode.value <= 0) return;
  if ((charStore.character?.currentEnergy || 0) < times) {
    feedbackMsg.value = "Không đủ nội năng!";
    return;
  }

  isGathering.value = true;
  feedbackMsg.value = "Đang khai thác...";

  try {
    await new Promise(r => setTimeout(r, 800));
    const charId = charStore.character.id || charStore.character.characterId;
    const payload = { characterId: charId, itemId: currentEvent.value.rewardItemId, amount: times };

    await axiosClient.post("/exploration/gather", payload);

    remainingNode.value -= times;
    await charStore.fetchCharacter();

    feedbackMsg.value = `Thu hoạch thành công! (+${times} ${currentEvent.value.lootName})`;

    if (remainingNode.value <= 0) {
      feedbackMsg.value = "Mỏ tài nguyên đã cạn!";
      setTimeout(() => router.push('/explore'), 1500);
    }
  } catch (e) {
    console.error(e);
    feedbackMsg.value = "Khai thác thất bại!";
  } finally {
    isGathering.value = false;
  }
};

const handleGatherAll = () => {
  const possible = Math.min(remainingNode.value, 10, charStore.character?.currentEnergy || 0);
  if (possible > 0) handleGather(possible);
};

onMounted(() => {
  charStore.fetchCharacter().then(() => initEvent());
  energyRefreshInterval = setInterval(() => charStore.fetchCharacter(), 10000);
});

onUnmounted(() => {
  if (energyRefreshInterval) clearInterval(energyRefreshInterval);
});
</script>

<style scoped>
.page-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #1a1a1a;
  position: relative;
}

.ink-bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.mountain-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  opacity: 0.3;
}

.nav-header {
  position: relative;
  z-index: 10;
  padding: 10px;
}

.btn-wood-back {
  background: #3e2723;
  color: #d7ccc8;
  border: 1px solid #5d4037;
  padding: 8px 15px;
  cursor: pointer;
  font-family: "Noto Serif TC", serif;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
}

.gathering-panel {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #eee;
}

.event-header {
  text-align: center;
  margin-bottom: 20px;
}

.node-frame {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.node-circle {
  width: 100px;
  height: 100px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  border: 3px solid #5d4037;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.node-img {
  width: 80%;
  height: 80%;
  object-fit: contain;
}

.rarity-seal {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  padding: 2px 8px;
  font-size: 0.8rem;
  border-radius: 10px;
  border: 1px solid #777;
  white-space: nowrap;
}

.bg-common {
  background: #555;
  color: #ccc;
}

.bg-uncommon {
  background: #2e7d32;
  color: #a5d6a7;
}

.bg-rare {
  background: #1565c0;
  color: #90caf9;
}

.bg-epic {
  background: #6a1b9a;
  color: #ce93d8;
}

.bg-legendary {
  background: #e65100;
  color: #ffcc80;
}

.node-name {
  font-size: 1.5rem;
  margin: 5px 0;
  font-family: "Orbitron", sans-serif;
  text-shadow: 0 0 5px currentColor;
}

.text-common {
  color: #ccc;
}

.text-uncommon {
  color: #66bb6a;
}

.text-rare {
  color: #42a5f5;
}

.text-epic {
  color: #ab47bc;
}

.text-legendary {
  color: #ffa726;
}

.req-box {
  font-size: 0.9rem;
  color: #aaa;
  margin-top: 5px;
}

.highlight {
  color: #fbc02d;
  font-weight: bold;
}

.info-scroll-area {
  width: 100%;
  max-width: 400px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #444;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 15px;
}

.icon-wrap {
  width: 30px;
  text-align: center;
  color: #8d6e63;
  font-size: 1.2rem;
}

.stat-detail {
  flex: 1;
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
}

.gold-text {
  color: #ffd700;
}

.text-red {
  color: #ef5350;
}

.progress-container {
  width: 100%;
  max-width: 400px;
  margin-bottom: 20px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: #bbb;
  margin-bottom: 5px;
}

.progress-track {
  width: 100%;
  height: 10px;
  background: #333;
  border-radius: 5px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #66bb6a, #43a047);
  transition: width 0.5s ease;
}

.action-zone {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  text-align: center;
}

.energy-tag {
  font-size: 1rem;
  color: #4fc3f7;
  background: rgba(3, 169, 244, 0.1);
  padding: 5px 15px;
  border-radius: 15px;
  display: inline-block;
  margin: 0 auto;
}

.energy-low {
  color: #ef5350;
  background: rgba(244, 67, 54, 0.1);
}

.btn-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.action-btn {
  padding: 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(1);
}

.action-btn:not(:disabled):hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

.btn-wood {
  background: #4e342e;
  color: #fff;
  border: 1px solid #6d4c41;
}

.btn-seal {
  background: #263238;
  color: #fff;
  border: 1px solid #37474f;
}

.btn-main {
  font-weight: bold;
  font-size: 1.1rem;
}

.btn-sub {
  font-size: 0.8rem;
  opacity: 0.8;
  margin-top: 3px;
}

.feedback-text {
  color: #69f0ae;
  font-weight: bold;
  min-height: 24px;
}

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
</style>