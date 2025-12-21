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

        <div v-if="currentTool" class="current-tool-info">
          <div class="tool-icon-box" :class="{ 'broken': currentTool.currentDurability <= 0 }">
            <img :src="resolveItemImage(currentTool.item.imageUrl)" />
          </div>
          <div class="tool-stat">
            <span class="t-name" :class="'text-' + (currentTool.item.rarity || 'COMMON')">
              {{ currentTool.item.name }}
            </span>
            <div class="big-durability-bar">
              <div class="bar-fill" :style="{ width: getDurabilityPercent(currentTool) + '%' }"
                :class="getDurabilityColor(currentTool)"></div>
              <span class="bar-text">{{ currentTool.currentDurability }} / {{ currentTool.maxDurability }}</span>
            </div>
          </div>
        </div>
        <div v-else class="no-tool-warning">
          <i class="fas fa-exclamation-triangle"></i> Cần trang bị: <span class="highlight">{{ currentEvent.reqTool
            }}</span>
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

        <div v-if="remainingNode <= 0" class="lock-hint">Mỏ đã cạn, hãy rời đi.</div>
        <div v-if="playerLevel < currentEvent.reqLevel" class="lock-hint">Cấp độ chưa đủ.</div>
        <div v-if="currentTool && currentTool.currentDurability <= 0" class="lock-hint tool-broken-msg">Công cụ đã hỏng!
        </div>

        <div class="btn-grid">
          <button class="btn-wood action-btn" @click="handleGather(1)"
            :disabled="isGathering || remainingNode <= 0 || (charStore.character?.currentEnergy || 0) < 1 || playerLevel < currentEvent.reqLevel || !currentTool || currentTool.currentDurability <= 0">
            <span class="btn-main">KHAI THÁC</span>
            <span class="btn-sub">Tốn 1 <i class="fas fa-bolt"></i></span>
          </button>
          <button class="btn-seal action-btn" @click="handleGatherAll"
            :disabled="isGathering || remainingNode <= 0 || (charStore.character?.currentEnergy || 0) < 1 || playerLevel < currentEvent.reqLevel || !currentTool || currentTool.currentDurability <= 0">
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

    <GameToast ref="toast" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useInventoryStore } from "@/stores/inventoryStore"; // Import thêm store inventory
import { useRouter } from "vue-router";
import axiosClient from "@/api/axiosClient";
import { resolveItemImage, getAssetUrl } from "@/utils/assetHelper";
import GameToast from '@/components/GameToast.vue';

const charStore = useCharacterStore();
const authStore = useAuthStore();
const inventoryStore = useInventoryStore(); // Init store
const router = useRouter();
const toast = ref(null);
let energyRefreshInterval = null;

const currentEvent = ref(null);
const remainingNode = ref(0);
const maxNode = ref(10);
const isGathering = ref(false);
const feedbackMsg = ref("");
const bgImage = getAssetUrl("b_mountain.jpg");

const playerLevel = computed(() => {
  if (!charStore.character) return 1;
  return charStore.character.level !== undefined ? charStore.character.level : 1;
});

// Danh sách sự kiện (Map ID -> Data)
const EVENT_TYPES = [
  // GỖ (w_)
  { id: "wood", codePrefix: "w_", rewardItemId: 1, name: "Cây Gỗ Sồi", image: resolveItemImage("tool/axe/w_wood.png"), rarityClass: "common", rarityText: "Phổ Thông", reqLevel: 1, reqTool: "Rìu", lootName: "Gỗ Sồi" },
  // ... (Giữ nguyên các loại khác, nhưng thêm codePrefix để dễ map tool)
  // Logic tạm: Map theo rewardItemId từ DB
];

// Map ID -> Resource Code Prefix để tìm Tool
const getSlotTypeFromItemId = (itemId) => {
  // Logic map cứng tạm thời dựa trên ID seed_core.sql
  // 1-4: Gỗ (AXE), 5-8: Khoáng (PICKAXE), 9-10: Cá (ROD)
  if (itemId >= 1 && itemId <= 4) return 'AXE';
  if (itemId >= 5 && itemId <= 8) return 'PICKAXE'; // Stone, Copper, Iron, Platinum
  if (itemId >= 9 && itemId <= 10) return 'FISHING_ROD'; // Fish
  if (itemId === 11 || itemId === 12) return 'SHOVEL'; // Coin, Strange -> Xẻng
  return 'PICKAXE'; // Default
};

// [COMPUTED] TÌM TOOL ĐANG TRANG BỊ
const currentTool = computed(() => {
  if (!currentEvent.value) return null;
  const slotType = getSlotTypeFromItemId(currentEvent.value.rewardItemId);

  // Tìm trong inventory (đã fetch)
  return inventoryStore.items.find(i => i.isEquipped && i.item.slotType === slotType);
});

// [HELPER] ĐỘ BỀN
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

// INIT EVENT
const initEvent = () => {
  if (!charStore.character) return;
  const dbItemId = charStore.character.gatheringItemId;
  const dbAmount = charStore.character.gatheringRemainingAmount;

  if (!dbItemId || dbItemId === 0) {
    router.push('/explore');
    return;
  }

  // Tìm event trong list hardcode (Cần đồng bộ với BE)
  // Tạm thời find theo ID
  let evt = EVENT_TYPES.find(e => e.rewardItemId === dbItemId);

  // Nếu không tìm thấy trong hardcode (do DB mới update), tạo object tạm
  if (!evt) {
    evt = {
      id: "unknown",
      rewardItemId: dbItemId,
      name: "Tài Nguyên Lạ",
      image: resolveItemImage("o_strange.png"),
      rarityClass: "common",
      rarityText: "Unknown",
      reqLevel: 1,
      reqTool: "Dụng Cụ",
      lootName: "Vật phẩm"
    };
  }

  currentEvent.value = evt;
  remainingNode.value = dbAmount !== undefined ? dbAmount : 10;
  maxNode.value = 10;
};

// HANDLE GATHER
const handleGather = async (times = 1) => {
  if (isGathering.value || remainingNode.value <= 0) return;

  // Validate Tool Frontend
  if (!currentTool.value) {
    feedbackMsg.value = "Chưa trang bị dụng cụ!";
    return;
  }
  if (currentTool.value.currentDurability <= 0) {
    feedbackMsg.value = "Công cụ đã hỏng!";
    return;
  }

  const currentEnergy = charStore.character?.currentEnergy || 0;
  // Lưu ý: Tier 5 có tỷ lệ free energy, nên ở đây chỉ check sơ bộ
  // Nếu muốn chính xác phải để BE check. Nhưng để UX tốt thì check luôn.
  // Tuy nhiên, nếu tool xịn thì có thể đào kể cả khi energy thấp (nhờ luck), nên tạm bỏ check cứng ở đây hoặc check min 1.

  isGathering.value = true;
  feedbackMsg.value = "Đang khai thác...";

  try {
    await new Promise(r => setTimeout(r, 800)); // Fake delay animation

    const char = charStore.character;
    const charId = char.charId;

    // Gọi API Gather mới (đã update ở bước trước)
    const payload = {
      // Note: API mới dùng @RequestParam nên dùng params trong axios config, 
      // hoặc nếu dùng endpoint cũ thì giữ nguyên body.
      // Ở đây dùng theo endpoint ExplorationService: gatherResource(user, itemId, amount)
      // Check lại Controller: @PostMapping("/gather") public ... gatherResource(...)
    };

    // Gọi qua axiosClient wrapper (đã config base URL)
    const res = await axiosClient.post("/exploration/gather", null, {
      params: {
        itemId: currentEvent.value.rewardItemId,
        amount: times
      }
    });

    // UPDATE STATE TỪ RESPONSE
    const data = res.data; // { message, remaining, jobExp, toolDurability }

    remainingNode.value = data.remaining;
    feedbackMsg.value = data.message; // Message từ BE đã bao gồm info năng lượng/free

    // [QUAN TRỌNG] Cập nhật độ bền Tool ngay lập tức
    if (data.toolDurability !== undefined && currentTool.value) {
      currentTool.value.currentDurability = data.toolDurability;
    }

    await charStore.fetchCharacter(); // Sync lại Energy/Exp

    if (remainingNode.value <= 0) {
      feedbackMsg.value = "Mỏ tài nguyên đã cạn!";
      setTimeout(() => router.push('/explore'), 1500);
    }

  } catch (e) {
    console.error("🔥 [LỖI KHAI THÁC]:", e);
    const errText = e.response?.data?.message || e.response?.data || "Lỗi server";
    feedbackMsg.value = "Lỗi: " + errText;
  } finally {
    isGathering.value = false;
  }
};

const handleGatherAll = () => {
  // Logic gom nhanh: Lấy max có thể (dựa trên còn lại và energy)
  // Thực tế BE sẽ tính toán lại, cứ gửi max request
  const possible = remainingNode.value;
  if (possible > 0) handleGather(possible);
};

const handleImgError = (e) => {
  e.target.src = resolveItemImage("tool/pickaxe/o_coal.png"); // Fallback
};

onMounted(async () => {
  await Promise.all([
    charStore.fetchCharacter(),
    inventoryStore.fetchInventory() // Fetch đồ để tìm Tool
  ]);
  initEvent();
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

/* TOOL INFO UI */
.current-tool-info {
  background: rgba(0, 0, 0, 0.6);
  padding: 8px;
  border-radius: 8px;
  border: 1px solid #5d4037;
  margin: 5px auto;
  display: flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
}

.tool-icon-box {
  width: 36px;
  height: 36px;
  border: 1px solid #8d6e63;
  background: #222;
  border-radius: 4px;
  padding: 2px;
}

.tool-icon-box img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.tool-icon-box.broken {
  border-color: red;
  opacity: 0.5;
}

.tool-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
}

.t-name {
  font-weight: bold;
  font-size: 0.8rem;
  color: #d7ccc8;
}

.big-durability-bar {
  width: 100px;
  height: 10px;
  background: #333;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
  border: 1px solid #555;
}

.bar-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.bar-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  color: white;
  text-shadow: 1px 1px 1px black;
  font-weight: bold;
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

.no-tool-warning {
  color: #ff5252;
  font-size: 0.9rem;
  margin-top: 5px;
  font-style: italic;
}

.tool-broken-msg {
  color: #ff5252;
  font-weight: bold;
  text-transform: uppercase;
  animation: blink 1s infinite;
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
  padding: 10px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-wrap {
  width: 25px;
  text-align: center;
  color: #8d6e63;
  font-size: 1rem;
}

.stat-detail {
  flex: 1;
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
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
  color: #69f0ae;
  font-weight: bold;
  min-height: 20px;
  font-size: 0.9rem;
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

.lock-hint {
  color: #ef5350;
  font-size: 0.85rem;
  font-style: italic;
  margin-bottom: 5px;
}
</style>