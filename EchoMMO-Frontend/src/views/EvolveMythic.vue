<template>
  <div class="page-container mythic-page">
    <div class="bg-layer"></div>

    <div class="mythic-wrapper">
      <div class="header-section">
        <h2 class="title-mythic">THẦN ĐIỆN - MYTHIC SANCTUARY</h2>
        <p class="subtitle">Nơi phàm khí chuyển hóa thành thần khí</p>
      </div>

      <div class="content-layout">
        <div class="inventory-panel glass-panel">
          <div class="panel-header">
            <i class="fas fa-dragon"></i> VẬT PHẨM ĐỦ ĐIỀU KIỆN
          </div>
          <div class="item-grid custom-scroll">
            <div
              v-for="item in eligibleItems"
              :key="item.userItemId"
              class="item-slot legendary-glow"
              :class="{
                selected: selectedItem?.userItemId === item.userItemId,
              }"
              @click="selectItem(item)"
            >
              <img
                :src="resolveItemImage(item.item.image || item.item.imageUrl)"
                @error="handleImgError"
              />
              <span class="lv-badge">+{{ item.enhanceLevel }}</span>
            </div>
            <div v-if="eligibleItems.length === 0" class="empty-text">
              Chưa có trang bị Huyền Thoại +30 nào.
            </div>
          </div>
        </div>

        <div class="evolution-panel glass-panel">
          <div v-if="selectedItem" class="evolution-active">
            <div class="item-display">
              <div class="item-box glowing-border">
                <img
                  :src="
                    resolveItemImage(
                      selectedItem.item.image || selectedItem.item.imageUrl,
                    )
                  "
                  class="main-img"
                />
              </div>
              <h3 class="item-name text-legendary">
                {{ selectedItem.item.name }}
              </h3>
            </div>

            <div class="arrow-down"><i class="fas fa-arrow-down"></i></div>

            <div class="mythic-preview">
              <span class="mythic-label">MYTHIC</span>
              <div class="stat-boost">
                Tất cả chỉ số: <span class="boost-val">+0.5% ~ 1.0%</span>
              </div>
            </div>

            <div class="cost-grid">
              <div class="cost-item" :class="{ missing: !canAfford.gold }">
                <i class="fas fa-coins text-gold"></i>
                <span>1,000,000 Vàng</span>
              </div>

              <div class="cost-item" :class="{ missing: !checkMat(4, 500) }">
                <img
                  :src="resolveItemImage('r_black_wood.png')"
                  class="icon-sm"
                />
                <span>500 Gỗ Lạ</span>
                <span class="current-qty">({{ getUserQty(4) }})</span>
              </div>

              <div class="cost-item" :class="{ missing: !checkMat(12, 200) }">
                <img
                  :src="resolveItemImage('r_mystrile_node.png')"
                  class="icon-sm"
                />
                <span>200 Nguyên liệu lạ</span>
                <span class="current-qty">({{ getUserQty(12) }})</span>
              </div>

              <div class="cost-item" :class="{ missing: !checkMat(11, 5) }">
                <img
                  :src="resolveItemImage('r_echo_coin.png')"
                  class="icon-sm"
                />
                <span>5 Echo Coin</span>
                <span class="current-qty">({{ getUserQty(11) }})</span>
              </div>
            </div>

            <button
              class="btn-evolve glitch-btn"
              @click="handleEvolve"
              :disabled="isProcessing || !canAffordAll"
            >
              <span v-if="!isProcessing">KHỞI ĐỘNG TRẬN PHÁP</span>
              <span v-else>ĐANG CHUYỂN HÓA...</span>
            </button>

            <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>
          </div>
          <div v-else class="placeholder-text">
            Chọn vật phẩm bên trái để xem chi tiết
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="showResult" class="result-overlay" @click="closeResult">
        <div class="result-box">
          <h1 class="mythic-title">THẦN KHÍ GIÁNG LÂM!</h1>
          <div class="mythic-icon-box">
            <i class="fas fa-dragon"></i>
          </div>
          <p>
            Chúc mừng! {{ selectedItem?.item.name }} đã đạt cảnh giới Thần
            Thoại.
          </p>
          <button class="btn-confirm" @click="closeResult">NHẬN</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useInventoryStore } from "../stores/inventoryStore";
import { useAuthStore } from "../stores/authStore";
import axiosClient from "../api/axiosClient";
import { resolveItemImage } from "@/utils/assetHelper";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();

const selectedItem = ref(null);
const isProcessing = ref(false);
const showResult = ref(false);
const errorMessage = ref("");

// Filter items: Chỉ lấy Legendary +30 và chưa phải Mythic
const eligibleItems = computed(() => {
  return (inventoryStore.items || []).filter(
    (i) => i.item.rarity === "LEGENDARY" && i.enhanceLevel >= 30 && !i.isMythic,
  );
});

// Helper check số lượng trong kho
const getUserQty = (itemId) => {
  const found = inventoryStore.items.find((i) => i.item.itemId === itemId);
  return found ? found.quantity : 0;
};

// Check từng điều kiện
const checkMat = (itemId, needed) => getUserQty(itemId) >= needed;

const canAfford = computed(() => {
  return {
    gold: (authStore.wallet?.gold || 0) >= 1000000,
    mat1: checkMat(4, 500), // Gỗ Lạ
    mat2: checkMat(12, 200), // NL Lạ
    mat3: checkMat(11, 5), // Echo Coin
  };
});

const canAffordAll = computed(() => {
  const c = canAfford.value;
  return c.gold && c.mat1 && c.mat2 && c.mat3;
});

const selectItem = (item) => {
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleEvolve = async () => {
  if (!selectedItem.value) return;
  isProcessing.value = true;
  errorMessage.value = "";

  try {
    await axiosClient.post(
      `/equipment/evolve-mythic/${selectedItem.value.userItemId}`,
    );

    // Refresh data
    await Promise.all([
      inventoryStore.fetchInventory(),
      authStore.fetchProfile(),
    ]);

    showResult.value = true;
  } catch (err) {
    console.error(err);
    errorMessage.value = err.response?.data?.message || "Tiến hóa thất bại!";
  } finally {
    isProcessing.value = false;
  }
};

const closeResult = () => {
  showResult.value = false;
  selectedItem.value = null; // Reset selection
};

const handleImgError = (e) => {
  e.target.src = resolveItemImage("default_item.png");
};

onMounted(async () => {
  await Promise.all([
    inventoryStore.fetchInventory(),
    authStore.fetchProfile(),
  ]);
});
</script>

<style scoped>
/* Theme màu Tím/Đỏ tối cho Mythic */
.mythic-page {
  background: #05000a;
  min-height: 100vh;
  color: #e0d0ff;
  font-family: "Cinzel", serif;
  position: relative;
  overflow: hidden;
}

.bg-layer {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, #2a0a2e 0%, #000 80%);
  opacity: 0.6;
  z-index: 0;
}

.mythic-wrapper {
  position: relative;
  z-index: 2;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.header-section {
  text-align: center;
  margin-bottom: 20px;
  padding-top: 20px;
}

.title-mythic {
  font-size: 2.5rem;
  background: linear-gradient(to right, #ff00cc, #333399);
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 0 10px rgba(255, 0, 204, 0.5);
  margin: 0;
}

.content-layout {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0; /* Cho phép scroll con */
}

.glass-panel {
  background: rgba(20, 10, 30, 0.8);
  border: 1px solid #4a2a5e;
  border-radius: 8px;
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  padding: 15px;
}

.inventory-panel {
  flex: 1;
  max-width: 350px;
}
.evolution-panel {
  flex: 2;
  align-items: center;
  justify-content: center;
}

.panel-header {
  border-bottom: 1px solid #4a2a5e;
  padding-bottom: 10px;
  margin-bottom: 10px;
  color: #ff00cc;
  font-weight: bold;
}

.item-grid {
  flex: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(70px, 1fr));
  gap: 10px;
  align-content: start;
}

.item-slot {
  width: 70px;
  height: 70px;
  border: 1px solid #555;
  background: rgba(0, 0, 0, 0.5);
  cursor: pointer;
  position: relative;
  transition: 0.3s;
}

.item-slot img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.item-slot.selected {
  border-color: #ff00cc;
  box-shadow: 0 0 10px #ff00cc;
}
.item-slot:hover {
  transform: scale(1.05);
}
.lv-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  font-size: 10px;
  background: #000;
  padding: 1px 3px;
  border-radius: 3px;
}

.evolution-active {
  width: 100%;
  max-width: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.item-box {
  width: 120px;
  height: 120px;
  border: 2px solid #ffd700;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle, #333 0%, #000 100%);
  box-shadow: 0 0 20px #ffd700;
}
.main-img {
  width: 100px;
  height: 100px;
  object-fit: contain;
}

.text-legendary {
  color: #ffd700;
  text-shadow: 0 0 5px #ffaa00;
  margin-top: 10px;
}

.arrow-down {
  color: #ff00cc;
  font-size: 2rem;
  animation: bounce 2s infinite;
}

.mythic-preview {
  background: rgba(255, 0, 204, 0.1);
  border: 1px solid #ff00cc;
  padding: 10px 30px;
  text-align: center;
  border-radius: 4px;
}
.mythic-label {
  font-weight: bold;
  color: #ff00cc;
  letter-spacing: 2px;
  display: block;
}
.boost-val {
  color: #00ff00;
}

.cost-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  width: 100%;
}

.cost-item {
  background: #1a0f25;
  padding: 10px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #333;
}
.cost-item.missing {
  border-color: #ff3333;
  color: #ff3333;
}
.icon-sm {
  width: 24px;
  height: 24px;
}
.text-gold {
  color: #ffd700;
}
.current-qty {
  margin-left: auto;
  font-size: 0.8rem;
  opacity: 0.7;
}

.btn-evolve {
  width: 100%;
  padding: 15px;
  font-size: 1.2rem;
  font-weight: bold;
  color: white;
  background: linear-gradient(90deg, #990033, #ff0066);
  border: none;
  cursor: pointer;
  font-family: "Cinzel", serif;
  letter-spacing: 2px;
  transition: 0.3s;
}
.btn-evolve:disabled {
  filter: grayscale(1);
  cursor: not-allowed;
}
.btn-evolve:hover:not(:disabled) {
  box-shadow: 0 0 30px #ff0066;
}

.result-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
}
.result-box {
  background: #1a051a;
  border: 2px solid #ff00cc;
  padding: 40px;
  text-align: center;
  border-radius: 10px;
  box-shadow: 0 0 50px #ff00cc;
  animation: zoomIn 0.3s ease;
}
.mythic-icon-box {
  font-size: 4rem;
  color: #ff00cc;
  margin: 20px 0;
}
.btn-confirm {
  background: transparent;
  border: 1px solid white;
  color: white;
  padding: 10px 30px;
  cursor: pointer;
  margin-top: 20px;
}
.btn-confirm:hover {
  background: white;
  color: black;
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(10px);
  }
}
@keyframes zoomIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
