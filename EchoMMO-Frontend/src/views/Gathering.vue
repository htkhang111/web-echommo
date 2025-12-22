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
            <img :src="currentEvent.image" class="node-img" @error="handleImgError" />
          </div>
          <div class="rarity-seal" :class="'bg-' + currentEvent.rarityClass">
            {{ currentEvent.rarityText }}
          </div>
        </div>
        <h2 class="node-name" :class="'text-' + currentEvent.rarityClass">
          {{ currentEvent.name }}
        </h2>
      </div>

      <div class="info-scroll-area">

        <div class="status-row" v-if="currentTool">
          <div class="icon-wrap tool-img-wrap" :class="{ 'broken': currentTool.currentDurability <= 0 }">
            <img :src="resolveItemImage(currentTool.item.imageUrl)" />
          </div>

          <div class="stat-detail tool-layout">
            <span class="t-name" :class="'text-' + (currentTool.item.rarity || 'COMMON')">
              {{ currentTool.item.name }}
            </span>

            <div class="durability-compact">
              <div class="bar-track-mini">
                <div class="bar-fill" :style="{ width: getDurabilityPercent(currentTool) + '%' }"
                  :class="getDurabilityColor(currentTool)"></div>
              </div>
              <span class="dura-text">{{ currentTool.currentDurability }}/{{ currentTool.maxDurability }}</span>
            </div>
          </div>
        </div>

        <div class="status-row" v-else>
          <div class="icon-wrap"><i class="fas fa-exclamation-triangle text-error"></i></div>
          <div class="stat-detail">
            <span class="label text-error">Cần công cụ: {{ currentEvent.reqTool }}</span>
          </div>
        </div>

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
          <span>Thời gian còn lại</span>
          <span>{{ Math.round(expiryPercent) }}%</span>
        </div>
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: expiryPercent + '%' }"></div>
        </div>
      </div>

      <div class="action-zone">
        <div class="energy-tag" :class="{ 'energy-low': (charStore.character?.currentEnergy || 0) < 1 }">
          <i class="fas fa-bolt"></i> Nội Năng: <strong>{{ charStore.character?.currentEnergy || 0 }}</strong>
        </div>

        <div v-if="remainingNode <= 0" class="lock-hint">Mỏ đã cạn, hãy rời đi.</div>
        <div v-if="playerLevel < currentEvent.reqLevel" class="lock-hint">Cấp độ chưa đủ.</div>
        <div v-if="currentTool && currentTool.currentDurability <= 0" class="lock-hint tool-broken-msg">
          CÔNG CỤ ĐÃ HỎNG! CẦN SỬA CHỮA.
        </div>

        <div class="btn-grid">
          <button class="btn-wood action-btn" @click="handleGather(1)" :disabled="isDisableGather">
            <span class="btn-main">KHAI THÁC</span>
            <span class="btn-sub">Tốn 1 <i class="fas fa-bolt"></i></span>
          </button>

          <button class="btn-seal action-btn" @click="handleGatherAll" :disabled="isDisableGather">
            <span class="btn-main">TỰ ĐỘNG</span>
            <span class="btn-sub">Gom nhanh (Max 10)</span>
          </button>
        </div>

        <div class="feedback-text" v-if="feedbackMsg" :class="feedbackClass">{{ feedbackMsg }}</div>
      </div>
    </div>

    <div v-else class="gathering-panel" style="text-align: center; color: #aaa;">
      <p>Đang tìm kiếm tài nguyên...</p>
    </div>

    <GameToast ref="toast" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useInventoryStore } from "@/stores/inventoryStore";
import { useRouter } from "vue-router";
import axiosClient from "@/api/axiosClient";
import { resolveItemImage, getAssetUrl } from "@/utils/assetHelper";
import GameToast from '@/components/GameToast.vue';

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore();
const router = useRouter();
const toast = ref(null);

let energyRefreshInterval = null;
let expiryInterval = null;

const currentEvent = ref(null);
const remainingNode = ref(0);
const maxNode = ref(10);
const isGathering = ref(false);
const feedbackMsg = ref("");
const feedbackClass = ref("");
const expiryPercent = ref(100);
const bgImage = getAssetUrl("b_mountain.jpg");

const playerLevel = computed(() => {
  return charStore.character?.level || 1;
});

// [CHỈNH SỬA] Đã đổi hoàn toàn "Stone/Mỏ Đá" thành "Coal/Mỏ Than" (ID 5)
// Loại bỏ hoàn toàn khái niệm Mỏ Đá để tránh lỗi và nhầm lẫn.
const EVENT_TYPES = [
  { id: "wood", codePrefix: "w_", rewardItemId: 1, name: "Cây Gỗ Sồi", image: resolveItemImage("w_wood.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Rìu", lootName: "Gỗ Sồi" },
  { id: "dried_wood", codePrefix: "w_", rewardItemId: 2, name: "Cây Gỗ Khô", image: resolveItemImage("w_wood-red.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Rìu", lootName: "Gỗ Khô" },
  { id: "cold_wood", codePrefix: "w_", rewardItemId: 3, name: "Cây Gỗ Lạnh", image: resolveItemImage("w_wood-white.png"), rarityClass: "uncommon", rarityText: "Ít Gặp", reqLevel: 10, reqTool: "Rìu", lootName: "Gỗ Lạnh" },
  { id: "strange_wood", codePrefix: "w_", rewardItemId: 4, name: "Cây Gỗ Lạ", image: resolveItemImage("w_wood-black.png"), rarityClass: "rare", rarityText: "Hiếm", reqLevel: 20, reqTool: "Rìu", lootName: "Gỗ Lạ" },

  // Dòng này trước là Stone, giờ là Coal chính hiệu
  { id: "coal", codePrefix: "o_", rewardItemId: 5, name: "Mỏ Than", image: resolveItemImage("o_coal.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Cúp", lootName: "Than Đen" },

  { id: "copper", codePrefix: "o_", rewardItemId: 6, name: "Mạch Đồng", image: resolveItemImage("o_copper.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 5, reqTool: "Cúp", lootName: "Quặng Đồng" },
  { id: "iron", codePrefix: "o_", rewardItemId: 7, name: "Mỏ Sắt", image: resolveItemImage("o_iron.png"), rarityClass: "rare", rarityText: "Hiếm", reqLevel: 20, reqTool: "Cúp", lootName: "Quặng Sắt" },
  { id: "platinum", codePrefix: "o_", rewardItemId: 8, name: "Tinh Thể Bạch Kim", image: resolveItemImage("o_platinum.png"), rarityClass: "epic", rarityText: "Cực Phẩm", reqLevel: 40, reqTool: "Cúp", lootName: "Bạch Kim" },
  { id: "fish", codePrefix: "f_", rewardItemId: 9, name: "Hồ Cá", image: resolveItemImage("f_fish.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Cần Câu", lootName: "Cá" },
  { id: "shark", codePrefix: "f_", rewardItemId: 10, name: "Vùng Nước Nguy Hiểm", image: resolveItemImage("f_shark.png"), rarityClass: "uncommon", rarityText: "Nguy Hiểm", reqLevel: 30, reqTool: "Cần Câu", lootName: "Cá Mập" },
  { id: "clay", codePrefix: "s_", rewardItemId: 999, name: "Đất Sét", image: resolveItemImage("s_clay.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Xẻng", lootName: "Đất Sét" }
];

const getSlotTypeByPrefix = (prefix) => {
  if (prefix === "w_") return "AXE";
  if (prefix === "o_") return "PICKAXE";
  if (prefix === "f_") return "FISHING_ROD";
  if (prefix === "s_") return "SHOVEL";
  return "PICKAXE";
};

const currentTool = computed(() => {
  if (!currentEvent.value) return null;
  const slotType = getSlotTypeByPrefix(currentEvent.value.codePrefix);
  return inventoryStore.items.find(i => i.isEquipped && i.item.slotType === slotType);
});

const isDisableGather = computed(() => {
  return isGathering.value ||
    remainingNode.value <= 0 ||
    (charStore.character?.currentEnergy || 0) < 1 ||
    playerLevel.value < (currentEvent.value?.reqLevel || 1) ||
    !currentTool.value ||
    currentTool.value.currentDurability <= 0;
});

const getDurabilityPercent = (item) => {
  if (!item.maxDurability) return 100;
  return Math.max(0, (item.currentDurability / item.maxDurability) * 100);
};

const getDurabilityColor = (item) => {
  const pct = getDurabilityPercent(item);
  if (pct <= 0) return 'bg-red-600';
  if (pct < 30) return 'bg-orange-500';
  return 'bg-green-500';
};

const initEvent = () => {
  if (!charStore.character) return;
  const dbItemId = charStore.character.gatheringItemId;
  const dbAmount = charStore.character.gatheringRemainingAmount;

  if (!dbItemId || dbItemId === 0) {
    router.push('/explore');
    return;
  }

  let evt = EVENT_TYPES.find(e => e.rewardItemId === dbItemId);

  if (!evt) {
    evt = {
      id: "unknown",
      codePrefix: "o_",
      rewardItemId: dbItemId,
      name: "Tài Nguyên Lạ",
      image: resolveItemImage("o_strange.png"),
      rarityClass: "common",
      rarityText: "Bí Ẩn",
      reqLevel: 1,
      reqTool: "Cúp",
      lootName: "Vật phẩm"
    };
  }

  currentEvent.value = evt;
  remainingNode.value = dbAmount !== undefined ? dbAmount : 10;
  maxNode.value = Math.max(10, remainingNode.value);
  startTimer();
};

const startTimer = () => {
  let timeLeft = 180;
  expiryPercent.value = 100;
  if (expiryInterval) clearInterval(expiryInterval);
  expiryInterval = setInterval(() => {
    timeLeft--;
    expiryPercent.value = (timeLeft / 180) * 100;
    if (timeLeft <= 0) {
      feedbackMsg.value = "Mỏ đã sập!";
      setTimeout(() => router.push('/explore'), 1000);
    }
  }, 1000);
};

const handleGather = async (times = 1) => {
  if (isDisableGather.value) return;

  isGathering.value = true;
  feedbackMsg.value = "Đang khai thác...";
  feedbackClass.value = "";

  try {
    await new Promise(r => setTimeout(r, 600));

    const payload = {
      itemId: currentEvent.value.rewardItemId,
      amount: times
    };

    const res = await axiosClient.post("/exploration/gather", payload);
    const data = res.data;

    remainingNode.value = data.remaining;
    feedbackMsg.value = data.message;
    feedbackClass.value = "text-success";

    if (data.toolDurability !== undefined && currentTool.value) {
      currentTool.value.currentDurability = data.toolDurability;
    }

    await charStore.fetchCharacter();

    if (remainingNode.value <= 0) {
      feedbackMsg.value = "Mỏ tài nguyên đã cạn!";
      setTimeout(() => router.push('/explore'), 1500);
    }

  } catch (e) {
    console.error("🔥 [GATHER ERROR]:", e);
    const errText = e.response?.data?.message || "Lỗi kết nối server";
    feedbackMsg.value = errText;
    feedbackClass.value = "text-error";
    if (toast.value) toast.value.show(errText, "error");
  } finally {
    isGathering.value = false;
  }
};

const handleGatherAll = () => {
  const possible = Math.min(remainingNode.value, 10);
  if (possible > 0) handleGather(possible);
};

const handleImgError = (e) => {
  e.target.src = resolveItemImage("o_coal.png");
};

onMounted(async () => {
  await Promise.all([
    charStore.fetchCharacter(),
    inventoryStore.fetchInventory()
  ]);
  initEvent();
  energyRefreshInterval = setInterval(() => charStore.fetchCharacter(), 10000);
});

onUnmounted(() => {
  if (energyRefreshInterval) clearInterval(energyRefreshInterval);
  if (expiryInterval) clearInterval(expiryInterval);
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
  margin-bottom: 10px;
  width: 100%;
  max-width: 400px;
}

.node-frame {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 5px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.node-circle {
  width: 90px;
  height: 90px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  border: 3px solid #5d4037;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.node-img {
  width: 70%;
  height: 70%;
  object-fit: contain;
}

.rarity-seal {
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  padding: 1px 6px;
  font-size: 0.7rem;
  border-radius: 8px;
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
  font-size: 1.2rem;
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

/* --- [UPDATED] STYLES CHO TOOL & INFO --- */

.info-scroll-area {
  width: 100%;
  max-width: 400px;
  background: rgba(0, 0, 0, 0.5);
  /* Đậm hơn chút cho dễ nhìn */
  border: 1px solid #444;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Icon Wrap chung: Size chuẩn 30px */
.icon-wrap {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8d6e63;
  font-size: 1.2rem;
}

/* Tool Img Wrap: Tinh chỉnh riêng cho ảnh tool */
.tool-img-wrap {
  border: 1px solid #5d4037;
  background: #222;
  border-radius: 4px;
  padding: 2px;
  box-sizing: border-box;
}

.tool-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.tool-img-wrap.broken {
  border-color: red;
  opacity: 0.5;
}

.stat-detail {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.95rem;
}

/* Layout riêng cho dòng Tool: Flex row */
.tool-layout {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.t-name {
  font-weight: bold;
  color: #d7ccc8;
  font-size: 0.95rem;
}

/* Thanh độ bền gọn gàng bên phải */
.durability-compact {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bar-track-mini {
  width: 60px;
  /* Ngắn gọn */
  height: 6px;
  background: #333;
  border-radius: 3px;
  overflow: hidden;
  border: 1px solid #555;
}

.bar-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.dura-text {
  font-size: 0.75rem;
  color: #aaa;
  min-width: 40px;
  /* Cố định width để số ko nhảy */
  text-align: right;
}

/* --- COLORS --- */
.gold-text {
  color: #ffd700;
}

.text-red {
  color: #ef5350;
}

.text-error {
  color: #ff5252;
  font-weight: bold;
}

.text-success {
  color: #69f0ae;
}

.bg-red-600 {
  background: #d32f2f;
}

.bg-orange-500 {
  background: #f57c00;
}

.bg-green-500 {
  background: #388e3c;
}

/* --- OTHER STYLES (Keep Original) --- */
.progress-container {
  width: 100%;
  max-width: 400px;
  margin-bottom: 15px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #bbb;
  margin-bottom: 3px;
}

.progress-track {
  width: 100%;
  height: 8px;
  background: #333;
  border-radius: 4px;
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
  gap: 10px;
  text-align: center;
}

.energy-tag {
  font-size: 0.9rem;
  color: #4fc3f7;
  background: rgba(3, 169, 244, 0.1);
  padding: 4px 12px;
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
  padding: 12px;
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
  font-size: 1rem;
}

.btn-sub {
  font-size: 0.75rem;
  opacity: 0.8;
  margin-top: 2px;
}

.feedback-text {
  font-weight: bold;
  min-height: 20px;
  font-size: 0.9rem;
}

.lock-hint {
  color: #ef5350;
  font-size: 0.85rem;
  font-style: italic;
  margin-bottom: 5px;
}

.tool-broken-msg {
  color: #ff5252;
  font-weight: bold;
  text-transform: uppercase;
  animation: blink 1s infinite;
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

@keyframes blink {
  50% {
    opacity: 0.5;
  }
}
</style>