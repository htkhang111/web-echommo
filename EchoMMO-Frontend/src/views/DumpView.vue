<!-- <template>
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
</style> -->


<template>
  <div class="dump-page page-container">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

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
import { useInventoryStore } from '../stores/inventoryStore'; 
import { getAssetUrl } from '../utils/assetHelper'; 
import dumpApi from '../api/dumpApi';

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore(); 

// --- BACKGROUND LOGIC (MỚI) ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  // Chỉ đổi màu nền theo giờ (18h - 6h là tối), không hiện text
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC HỒ BÍ ẨN CŨ (GIỮ NGUYÊN) ---
const selectedType = ref('NORMAL'); // 'NORMAL' | 'SHARK'
const amount = ref(1);
const isDumping = ref(false);
const lastResult = ref(null);

const normalFishCount = computed(() => {
  if (!inventoryStore.items) return 0;
  const item = inventoryStore.items.find(i => i.item && i.item.code === 'f_fish');
  return item ? item.quantity : 0;
});

const sharkCount = computed(() => {
  if (!inventoryStore.items) return 0;
  const item = inventoryStore.items.find(i => i.item && i.item.code === 'f_shark');
  return item ? item.quantity : 0;
});

const maxAmount = computed(() => {
  return selectedType.value === 'NORMAL' ? normalFishCount.value : sharkCount.value;
});

watch(selectedType, () => {
  amount.value = 1;
});

watch(maxAmount, (newMax) => {
  if (amount.value > newMax) amount.value = newMax > 0 ? newMax : 1;
});

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
  updateDayNight(); // [MỚI] Kích hoạt màu nền
  inventoryStore.fetchInventory();
  charStore.fetchCharacter();
});
</script>

<style scoped>
/* --- 1. HỆ THỐNG BACKGROUND (MỚI) --- */
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

/* --- 2. LAYOUT CHUNG --- */
.page-container {
  padding: 20px;
  min-height: 100vh;
  /* Xóa nền gradient cũ, để trong suốt cho bg-layer */
  background: transparent; 
  color: #fff8e1; /* Chữ màu kem cho hợp nền gỗ */
  display: flex;
  justify-content: center;
  position: relative;
  overflow: hidden;
  font-family: "Noto Serif", serif;
}

.content-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 500px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header-section {
  text-align: center;
  background: rgba(30, 20, 15, 0.8); /* Nền gỗ mờ */
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #5d4037;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
}

.page-title {
  margin: 0;
  font-size: 1.8rem;
  color: #ffecb3;
  text-shadow: 0 2px 4px rgba(0,0,0,0.8);
}

.subtitle {
  font-style: italic;
  opacity: 0.8;
  font-size: 0.9rem;
  color: #d7ccc8;
}

.btn-back {
  background: transparent;
  border: 1px solid #8d6e63;
  color: #d7ccc8;
  padding: 5px 15px;
  border-radius: 4px;
  margin-top: 10px;
  cursor: pointer;
  transition: 0.2s;
}
.btn-back:hover { background: rgba(255,255,255,0.1); color: #fff; }

/* Action Card */
.action-card {
  background: rgba(38, 24, 21, 0.85); /* Nền gỗ đậm */
  backdrop-filter: blur(5px);
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #5d4037;
  box-shadow: 0 10px 30px rgba(0,0,0,0.6);
}

.fish-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.fish-option {
  flex: 1;
  background: rgba(0, 0, 0, 0.4);
  padding: 15px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #4e342e;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.fish-option.active {
  border-color: #66bb6a;
  background: rgba(74, 222, 128, 0.1);
  box-shadow: 0 0 10px rgba(102, 187, 106, 0.2);
}

.fish-option.shark.active {
  border-color: #ef5350;
  background: rgba(248, 113, 113, 0.1);
  box-shadow: 0 0 10px rgba(239, 83, 80, 0.2);
}

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
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.5));
}

.fish-option .name { font-weight: bold; margin: 5px 0; color: #fff; }
.fish-option .count { font-size: 0.8rem; color: #a1887f; }

/* Slider Controls */
.amount-control { margin-bottom: 20px; }
.slider-wrapper { display: flex; align-items: center; gap: 10px; margin: 10px 0; }
.slider { flex: 1; height: 6px; accent-color: #ffd700; cursor: pointer; }
.quick-btns { display: flex; justify-content: space-between; gap: 5px; }
.btn-mini { 
  background: #3e2723; border: 1px solid #5d4037; 
  color: #d7ccc8; padding: 5px 10px; border-radius: 4px; 
  cursor: pointer; font-size: 0.8rem; transition: 0.2s;
}
.btn-mini:hover { background: #4e342e; color: #fff; border-color: #ffd700; }

.btn-dump {
  width: 100%;
  padding: 15px;
  background: linear-gradient(to bottom, #1b5e20, #2e7d32); /* Xanh ngọc bích đậm */
  border: 1px solid #43a047;
  border-radius: 6px;
  color: #fff;
  font-weight: bold;
  font-size: 1.1rem;
  cursor: pointer;
  transition: transform 0.1s;
  text-transform: uppercase;
  letter-spacing: 1px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.btn-dump:hover:not(:disabled) { filter: brightness(1.1); box-shadow: 0 6px 15px rgba(0,0,0,0.4); }
.btn-dump:active { transform: scale(0.98); }
.btn-dump:disabled { background: #424242; border-color: #616161; opacity: 0.7; cursor: not-allowed; }

/* Result Modal */
.result-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  animation: fadeIn 0.3s;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: #261815;
  padding: 30px;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  text-align: center;
  border: 2px solid #ffd700;
  box-shadow: 0 0 30px rgba(0,0,0,0.8);
}

.modal-content h3 { color: #ffd700; margin-top: 0; font-family: "Playfair Display", serif; letter-spacing: 2px; }
.message { color: #d7ccc8; font-style: italic; }
.rewards-grid { display: flex; flex-direction: column; gap: 10px; margin: 20px 0; }

.reward-item {
  background: rgba(255,255,255,0.05);
  padding: 10px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px solid #4e342e;
}

.reward-icon { width: 24px; height: 24px; object-fit: contain; }

.reward-item.special {
  background: rgba(255, 215, 0, 0.1);
  border: 1px solid #ffd700;
  color: #ffd700;
  font-weight: bold;
}

.btn-close {
  background: #3e2723;
  color: #ffd700;
  border: 1px solid #5d4037;
  padding: 10px 25px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
}
.btn-close:hover { background: #4e342e; border-color: #ffd700; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>