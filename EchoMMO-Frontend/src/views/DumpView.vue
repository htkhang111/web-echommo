<template>
  <div class="dump-page page-container">
    <div class="content-wrapper">
      <div class="header-section">
        <h1 class="page-title">🌊 Hồ Bí Ẩn</h1>
        <p class="subtitle">Thả cá về hồ, cầu nguyện vận may...</p>
        <button class="btn-back" @click="$router.push('/village')">⬅ Về Làng</button>
      </div>

      <div class="action-card">
        <div class="fish-selector">
          <div 
            class="fish-option" 
            :class="{ active: selectedType === 'NORMAL' }"
            @click="selectedType = 'NORMAL'"
          >
            <div class="img-wrapper">
              <img :src="getAssetUrl('f_fish')" alt="Cá thường" class="fish-img" />
            </div>
            <span class="name">Cá Thường</span>
            <span class="count">Đang có: {{ normalFishCount }}</span>
          </div>

          <div 
            class="fish-option shark" 
            :class="{ active: selectedType === 'SHARK' }"
            @click="selectedType = 'SHARK'"
          >
            <div class="img-wrapper">
              <img :src="getAssetUrl('f_shark')" alt="Cá mập" class="fish-img" />
            </div>
            <span class="name">Cá Mập</span>
            <span class="count">Đang có: {{ sharkCount }}</span>
          </div>
        </div>

        <div class="amount-control" v-if="maxAmount > 0">
          <label>Số lượng muốn thả:</label>
          <div class="slider-wrapper">
            <input 
              type="range" 
              v-model.number="amount" 
              :min="1" 
              :max="maxAmount" 
              class="slider"
            />
            <span class="amount-display">{{ amount }} / {{ maxAmount }}</span>
          </div>
          <div class="quick-btns">
            <button class="btn-mini" @click="amount = 1">1</button>
            <button class="btn-mini" @click="amount = Math.floor(maxAmount / 2)">50%</button>
            <button class="btn-mini" @click="amount = maxAmount">Tất cả</button>
          </div>
        </div>
        
        <div class="empty-msg" v-else>
          Đại hiệp không còn con {{ selectedType === 'NORMAL' ? 'Cá thường' : 'Cá mập' }} nào trong túi!
        </div>

        <button 
          class="btn-dump" 
          :disabled="isDumping || maxAmount <= 0"
          @click="handleDump"
        >
          <span v-if="isDumping">Đang thả... 🌊</span>
          <span v-else>🙏 Thả Cá Cầu May</span>
        </button>
      </div>

      <div v-if="lastResult" class="result-modal">
        <div class="modal-content">
          <h3>✨ Kết Quả ✨</h3>
          <p class="message">{{ lastResult.message }}</p>
          
          <div class="rewards-grid">
            <div class="reward-item" v-if="lastResult.goldReceived > 0">
              <img :src="getAssetUrl('r_coin.png')" class="reward-icon" />
              <span class="val">+{{ formatNumber(lastResult.goldReceived) }} Vàng</span>
            </div>
            
            <div class="reward-item special" v-if="lastResult.echoReceived > 0">
              <img :src="getAssetUrl('r_coinEcho.png')" class="reward-icon" />
              <span class="val">+{{ lastResult.echoReceived }} Echo</span>
            </div>

            <div class="reward-item" v-for="(qty, name) in lastResult.itemsReceived" :key="name">
              <img :src="getAssetUrl('o_strange')" class="reward-icon" />
              <span class="val">{{ name }} x{{ qty }}</span>
            </div>
          </div>

          <button class="btn-close" @click="lastResult = null">Tiếp tục</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useCharacterStore } from '../stores/characterStore';
import { useInventoryStore } from '../stores/inventoryStore'; // [NEW] Import Inventory
import { getAssetUrl } from '../utils/assetHelper'; // [NEW] Import Asset Helper
import dumpApi from '../api/dumpApi';

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore(); // [NEW]

// State
const selectedType = ref('NORMAL'); // 'NORMAL' | 'SHARK'
const amount = ref(1);
const isDumping = ref(false);
const lastResult = ref(null);

// [FIX] Lấy số lượng từ Inventory Store thay vì Wallet
const normalFishCount = computed(() => {
  if (!inventoryStore.items) return 0;
  // Tìm item có code là 'f_fish'
  const item = inventoryStore.items.find(i => i.item && i.item.code === 'f_fish');
  return item ? item.quantity : 0;
});

const sharkCount = computed(() => {
  if (!inventoryStore.items) return 0;
  // Tìm item có code là 'f_shark'
  const item = inventoryStore.items.find(i => i.item && i.item.code === 'f_shark');
  return item ? item.quantity : 0;
});

const maxAmount = computed(() => {
  return selectedType.value === 'NORMAL' ? normalFishCount.value : sharkCount.value;
});

// Watch
watch(selectedType, () => {
  amount.value = 1;
});

watch(maxAmount, (newMax) => {
  if (amount.value > newMax) amount.value = newMax > 0 ? newMax : 1;
});

// Methods
const formatNumber = (num) => {
  return new Intl.NumberFormat('vi-VN').format(num);
};

const handleDump = async () => {
  if (amount.value <= 0 || amount.value > maxAmount.value) return;
  
  isDumping.value = true;
  lastResult.value = null;

  try {
    const res = await dumpApi.dumpFish(selectedType.value, amount.value);
    lastResult.value = res.data;
    
    // [UPDATE] Gọi lại cả 2 API để cập nhật số dư vàng và số lượng cá
    await Promise.all([
      charStore.fetchCharacter(),
      inventoryStore.fetchInventory()
    ]);
    
    amount.value = 1;
  } catch (error) {
    alert(error.response?.data || "Có lỗi xảy ra khi thả cá!");
  } finally {
    isDumping.value = false;
  }
};

onMounted(() => {
  // [IMPORTANT] Load inventory khi vào trang để có số lượng cá
  inventoryStore.fetchInventory();
  charStore.fetchCharacter();
});
</script>

<style scoped>
.page-container {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(to bottom, #1a2a6c, #b21f1f, #fdbb2d);
  color: white;
  display: flex;
  justify-content: center;
}

.content-wrapper {
  width: 100%;
  max-width: 500px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header-section {
  text-align: center;
  background: rgba(0, 0, 0, 0.5);
  padding: 15px;
  border-radius: 12px;
}

.page-title {
  margin: 0;
  font-size: 1.8rem;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}

.subtitle {
  font-style: italic;
  opacity: 0.9;
  font-size: 0.9rem;
}

.btn-back {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  margin-top: 10px;
  cursor: pointer;
}

/* Action Card */
.action-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.2);
}

.fish-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.fish-option {
  flex: 1;
  background: rgba(0, 0, 0, 0.3);
  padding: 15px;
  border-radius: 10px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.fish-option.active {
  border-color: #4ade80;
  background: rgba(74, 222, 128, 0.1);
}

.fish-option.shark.active {
  border-color: #f87171;
  background: rgba(248, 113, 113, 0.1);
}

/* [NEW] Style cho ảnh cá */
.img-wrapper {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 5px;
}

.fish-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.3));
}

.fish-option .name { font-weight: bold; margin: 5px 0; }
.fish-option .count { font-size: 0.8rem; opacity: 0.8; }

/* Slider Controls */
.amount-control { margin-bottom: 20px; }
.slider-wrapper { display: flex; align-items: center; gap: 10px; margin: 10px 0; }
.slider { flex: 1; height: 8px; accent-color: #4ade80; }
.quick-btns { display: flex; justify-content: space-between; gap: 5px; }
.btn-mini { background: rgba(255,255,255,0.1); border: none; color: white; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 0.8rem; }
.btn-mini:hover { background: rgba(255,255,255,0.2); }

.btn-dump {
  width: 100%;
  padding: 15px;
  background: linear-gradient(45deg, #11998e, #38ef7d);
  border: none;
  border-radius: 12px;
  color: white;
  font-weight: bold;
  font-size: 1.1rem;
  cursor: pointer;
  transition: transform 0.1s;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.btn-dump:active { transform: scale(0.98); }
.btn-dump:disabled { opacity: 0.6; cursor: not-allowed; filter: grayscale(1); }

/* Result Modal */
.result-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  animation: fadeIn 0.3s;
}

.modal-content {
  background: #2d3748;
  padding: 30px;
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  text-align: center;
  border: 1px solid #4ade80;
  box-shadow: 0 0 20px rgba(74, 222, 128, 0.3);
}

.modal-content h3 { color: #4ade80; margin-top: 0; }
.rewards-grid { display: flex; flex-direction: column; gap: 10px; margin: 20px 0; }

.reward-item {
  background: rgba(255,255,255,0.05);
  padding: 10px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.reward-icon { width: 24px; height: 24px; object-fit: contain; }

.reward-item.special {
  background: rgba(255, 215, 0, 0.1);
  border: 1px solid rgba(255, 215, 0, 0.3);
  color: #ffd700;
  font-weight: bold;
}

.btn-close {
  background: #4ade80;
  color: #1a202c;
  border: none;
  padding: 10px 25px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>