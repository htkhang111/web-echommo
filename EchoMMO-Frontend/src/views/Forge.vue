<template>
  <div class="page-container forge-page ancient-theme">
    <div class="wood-bg-layer"></div>
    <div class="fire-overlay"></div>

    <div class="forge-wrapper">
      <div class="forge-header">
        <div class="header-ornament left"></div>
        <h2 class="title-ancient">
          <i class="fas fa-hammer"></i> THẦN BINH CÁC
        </h2>
        <div class="header-ornament right"></div>
      </div>

      <div class="forge-layout">
        <div class="inventory-panel wood-panel">
          <div class="panel-title">
            <i class="fas fa-dungeon"></i> KHO TRANG BỊ
          </div>

          <div class="filter-tabs">
            <span
              v-for="type in ['ALL', 'WEAPON', 'ARMOR', 'ACCESSORY']"
              :key="type"
              class="filter-tab"
              :class="{ active: filterType === type }"
              @click="filterType = type"
            >
              {{ getFilterLabel(type) }}
            </span>
          </div>

          <div class="item-grid custom-scroll">
            <div
              v-for="item in sortedEquipableItems"
              :key="item.userItemId"
              class="item-slot"
              :class="{
                selected: selectedItem?.userItemId === item.userItemId,
                'border-mythic': item.isMythic,
                [getRarityClass(item)]: true,
              }"
              @click="selectItem(item)"
            >
              <div class="slot-img-box">
                <img
                  :src="resolveItemImage(item.item.image || item.item.imageUrl)"
                  @error="handleImgError"
                  class="item-img-display"
                />

                <div class="equipped-badge" v-if="item.isEquipped">
                  <i class="fas fa-shield-alt"></i>
                </div>

                <div class="level-badge" v-if="item.isMythic">
                  M{{ item.mythicLevel }}
                </div>
                <div class="level-badge" v-else-if="item.enhanceLevel > 0">
                  +{{ item.enhanceLevel }}
                </div>
              </div>
            </div>

            <div v-if="sortedEquipableItems.length === 0" class="empty-msg">
              <i class="fas fa-box-open"></i> Không có trang bị phù hợp
            </div>
          </div>
        </div>

        <div class="anvil-panel wood-panel">
          <div class="anvil-zone">
            <div class="fire-particles"></div>

            <div
              class="main-slot-container"
              :class="{
                shaking: isForging,
                'mythic-glow-anim': selectedItem?.isMythic,
              }"
            >
              <div class="main-slot" v-if="selectedItem">
                <img
                  :src="
                    resolveItemImage(
                      selectedItem.item.image || selectedItem.item.imageUrl,
                    )
                  "
                />
                <div class="glow-ring"></div>
              </div>
              <div class="empty-anvil" v-else>
                <i class="fas fa-hammer fa-3x"></i>
                <p>Chọn trang bị để rèn</p>
              </div>
            </div>

            <transition name="fade">
              <div v-if="selectedItem" class="upgrade-info">
                <h3
                  class="item-title-large"
                  :class="getRarityTextClass(selectedItem)"
                >
                  {{ selectedItem.item.name }}
                  <span class="rank-badge">{{
                    getRarityLabel(selectedItem.item.rarity)
                  }}</span>
                </h3>

                <div class="level-compare">
                  <div class="lv-box curr">
                    <span class="label">Hiện tại</span>
                    <span class="val">
                      {{
                        selectedItem.isMythic
                          ? "M" + selectedItem.mythicLevel
                          : "+" + selectedItem.enhanceLevel
                      }}
                    </span>
                  </div>
                  <div class="arrow-anim">
                    <i class="fas fa-angle-double-right"></i>
                  </div>
                  <div class="lv-box next">
                    <span class="label">Sau khi rèn</span>
                    <span class="val highlight">
                      {{
                        selectedItem.isMythic
                          ? "M" + (selectedItem.mythicLevel + 1)
                          : "+" + (selectedItem.enhanceLevel + 1)
                      }}
                    </span>
                  </div>
                </div>

                <div class="stats-preview custom-scroll">
                  <div class="stat-row main-stat">
                    <span>{{ getStatLabel(selectedItem.mainStatType) }}</span>
                    <div class="stat-change">
                      <span class="old">{{
                        formatNumber(selectedItem.mainStatValue)
                      }}</span>
                      <i class="fas fa-arrow-right"></i>
                      <span class="new">{{
                        formatNumber(getPredictedMainStat(selectedItem))
                      }}</span>
                    </div>
                  </div>

                  <div
                    v-for="(sub, idx) in parsedSubStats"
                    :key="idx"
                    class="stat-row sub-stat"
                  >
                    <span class="sub-dot">•</span>
                    <span>{{ getStatLabel(sub.code) }}</span>
                    <span class="val"
                      >+{{ formatNumber(sub.value)
                      }}{{ sub.isPercent ? "%" : "" }}</span
                    >
                  </div>
                </div>

                <div class="cost-section">
                  <div
                    class="resource-row"
                    :class="{ 'not-enough': !canAffordGold }"
                  >
                    <i class="fas fa-coins text-gold"></i>
                    <span>{{ formatNumber(upgradeCost.gold) }} Vàng</span>
                  </div>
                  <div class="resource-row">
                    <img
                      :src="
                        resolveItemImage(getMaterialImage(upgradeCost.matName))
                      "
                      class="mat-icon"
                    />
                    <span
                      >{{ upgradeCost.matQty }} {{ upgradeCost.matName }}</span
                    >
                  </div>

                  <div class="rate-row" v-if="!selectedItem.isMythic">
                    Tỉ lệ thành công:
                    <span :class="getRateColor(successRate)"
                      >{{ successRate }}%</span
                    >
                  </div>
                </div>

                <div class="actions-group">
                  <button
                    v-if="
                      !selectedItem.isMythic && selectedItem.enhanceLevel < 30
                    "
                    class="btn-forge"
                    @click="handleUpgrade"
                    :disabled="isForging"
                  >
                    <span v-if="!isForging">CƯỜNG HÓA</span>
                    <span v-else
                      ><i class="fas fa-spinner fa-spin"></i> ĐANG RÈN...</span
                    >
                  </button>

                  <button
                    v-if="canEvolve"
                    class="btn-evolve glitch-effect"
                    @click="handleEvolve"
                    :disabled="isForging"
                  >
                    ☠️ ĐỘT PHÁ THẦN (5%) ☠️
                  </button>

                  <button
                    v-if="selectedItem.isMythic"
                    class="btn-mythic-upgrade"
                    @click="handleMythicUpgrade"
                    :disabled="isForging || selectedItem.mythicLevel >= 30"
                  >
                    <span v-if="!isForging">THĂNG CẤP THẦN</span>
                    <span v-else>ĐANG HẤP THỤ...</span>
                  </button>

                  <button
                    v-if="
                      !selectedItem.isMythic &&
                      selectedItem.enhanceLevel >= 30 &&
                      selectedItem.item.rarity !== 'LEGENDARY'
                    "
                    class="btn-forge disabled"
                    disabled
                  >
                    MAX LEVEL
                  </button>
                </div>

                <div v-if="errorMessage" class="error-text">
                  <i class="fas fa-exclamation-triangle"></i> {{ errorMessage }}
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>

    <transition name="pop-up">
      <div v-if="showResult" class="result-overlay" @click="closeResult">
        <div class="result-card" :class="resultType" @click.stop>
          <div class="result-header">
            <div class="result-icon-circle">
              <i :class="resultIcon"></i>
            </div>
          </div>
          <div class="result-body">
            <h2 class="result-title">{{ resultTitle }}</h2>
            <p class="result-desc">{{ resultMessage }}</p>
          </div>
          <button class="btn-confirm" @click="closeResult">XÁC NHẬN</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import axiosClient from "../api/axiosClient";
import { useInventoryStore } from "../stores/inventoryStore";
import { useAuthStore } from "../stores/authStore";
import { useCharacterStore } from "../stores/characterStore";
import { useMarketStore } from "../stores/marketStore"; // <-- IMPORT MARKET STORE
import { resolveItemImage } from "@/utils/assetHelper";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const characterStore = useCharacterStore();
const marketStore = useMarketStore(); // <-- KHỞI TẠO MARKET STORE

const selectedItem = ref(null);
const isForging = ref(false);
const showResult = ref(false);
const resultType = ref("success");
const resultMessage = ref("");
const errorMessage = ref("");
const filterType = ref("ALL");

// --- COMPUTED ---
const sortedEquipableItems = computed(() => {
  const items = inventoryStore.items || [];
  if (!Array.isArray(items)) return [];

  let filtered = items.filter(
    (i) =>
      i.item &&
      ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"].includes(
        i.item.type,
      ),
  );

  if (filterType.value === "WEAPON")
    filtered = filtered.filter((i) => i.item.type === "WEAPON");
  else if (filterType.value === "ARMOR")
    filtered = filtered.filter((i) =>
      ["ARMOR", "HELMET", "BOOTS"].includes(i.item.type),
    );
  else if (filterType.value === "ACCESSORY")
    filtered = filtered.filter((i) =>
      ["RING", "NECKLACE"].includes(i.item.type),
    );

  return filtered.sort((a, b) => {
    if (a.isMythic !== b.isMythic) return b.isMythic ? 1 : -1;
    return b.enhanceLevel - a.enhanceLevel;
  });
});

const parsedSubStats = computed(() => {
  if (!selectedItem.value || !selectedItem.value.subStats) return [];
  try {
    const data = selectedItem.value.subStats;
    return typeof data === "string" ? JSON.parse(data) : data;
  } catch (e) {
    return [];
  }
});

const canEvolve = computed(() => {
  if (!selectedItem.value) return false;
  return (
    selectedItem.value.item.rarity === "LEGENDARY" &&
    selectedItem.value.enhanceLevel >= 30
  );
});

const upgradeCost = computed(() => {
  if (!selectedItem.value) return { gold: 0, matQty: 0, matName: "Gỗ" };
  const level = selectedItem.value.isMythic
    ? selectedItem.value.mythicLevel
    : selectedItem.value.enhanceLevel;

  let matName = "Gỗ";
  if (level >= 10) matName = "Sắt";
  if (level >= 20) matName = "Bạch Kim";
  if (selectedItem.value.isMythic) matName = "Kim Cương";

  return {
    gold: selectedItem.value.isMythic ? 5000 * (level + 1) : 100 + level * 50,
    matQty: selectedItem.value.isMythic ? 1 : Math.floor(level / 5) + 1,
    matName: matName,
  };
});

const canAffordGold = computed(() => {
  const userGold = authStore.wallet?.gold || 0;
  return userGold >= upgradeCost.value.gold;
});

const successRate = computed(() => {
  if (!selectedItem.value || selectedItem.value.isMythic) return 100;
  return Math.max(10, 100 - (selectedItem.value.enhanceLevel || 0) * 3);
});

const resultTitle = computed(() => {
  if (resultType.value === "mythic") return "THẦN KHÍ GIÁNG LÂM!";
  return resultType.value === "success" ? "THÀNH CÔNG!" : "THẤT BẠI!";
});

const resultIcon = computed(() => {
  if (resultType.value === "mythic") return "fas fa-dragon";
  return resultType.value === "success" ? "fas fa-check" : "fas fa-times";
});

// --- ACTIONS ---

const fetchInventory = async () => {
  // Đảm bảo gọi fetchInventory đúng tên
  await inventoryStore.fetchInventory();
  if (selectedItem.value) {
    const found = inventoryStore.items.find(
      (i) => i.userItemId === selectedItem.value.userItemId,
    );
    selectedItem.value = found || null;
  }
};

const selectItem = (item) => {
  if (isForging.value) return;
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;

  const currentUserId = authStore.userId;
  if (!currentUserId) {
    errorMessage.value = "Chưa xác thực người dùng. Vui lòng đăng nhập lại.";
    return;
  }

  isForging.value = true;
  errorMessage.value = "";

  try {
    const url = `/game/item/enhance/${selectedItem.value.userItemId}?userId=${currentUserId}`;
    await axiosClient.post(url);

    // ====================================================
    // [FINAL FIX SYNC] Đảm bảo gọi đúng tên hàm và tải lại tất cả store
    // ====================================================

    // 1. Tải lại Inventory
    await inventoryStore.fetchInventory();

    // 2. Tải lại Stats nhân vật (FIXED: Dùng fetchCharacter() - không tham số)
    // Tên hàm chính xác từ characterStore.js là fetchCharacter()
    await characterStore.fetchCharacter();

    // 3. Tải lại Wallet (để cập nhật vàng/vật liệu)
    await authStore.fetchProfile();

    // 4. Đồng bộ với Marketplace (QUAN TRỌNG cho việc sync item được rao bán)
    // marketStore có hàm refresh() bao gồm cả fetch Inventory và Auth Profile
    await marketStore.refresh();

    // 5. Cập nhật selectedItem hiển thị
    const updatedItem = inventoryStore.items.find(
      (i) => i.userItemId === selectedItem.value.userItemId,
    );
    selectedItem.value = updatedItem || null;
    // ====================================================

    showResultModal(
      "success",
      `Cường hóa lên +${selectedItem.value.enhanceLevel} thành công!`,
    );
  } catch (err) {
    console.error(err);
    const msg = err.response?.data || "Lỗi hệ thống hoặc thiếu nguyên liệu.";
    showResultModal("fail", msg);
    errorMessage.value = msg;
  } finally {
    isForging.value = false;
  }
};

const handleEvolve = async () => {
  isForging.value = true;
  try {
    const url = `/equipment/evolve-mythic/${selectedItem.value.userItemId}`;
    const res = await axiosClient.post(url);
    if (
      res.data &&
      typeof res.data === "string" &&
      res.data.includes("THÀNH CÔNG")
    ) {
      showResultModal("mythic", res.data);
    } else {
      showResultModal("fail", res.data || "Tiến hóa thất bại.");
    }
  } catch (err) {
    showResultModal("fail", err.response?.data || "Lỗi tiến hóa.");
  } finally {
    isForging.value = false;
    await fetchInventory();
  }
};

const handleMythicUpgrade = async () => {
  isForging.value = false;
};

const showResultModal = (type, msg) => {
  resultType.value = type;
  resultMessage.value = msg;
  showResult.value = true;
};

const closeResult = () => {
  showResult.value = false;
  if (resultType.value === "success" || resultType.value === "mythic")
    errorMessage.value = "";
};

// Helpers
const formatNumber = (n) => Number(n || 0).toLocaleString();
const getMaterialImage = (name) => {
  const map = {
    Gỗ: "r_go.png",
    Sắt: "r_silver_bar.png",
    "Bạch Kim": "r_mystrile_bar.png",
    "Kim Cương": "stone_1.png",
  };
  return map[name] || "r_go.png";
};
const getPredictedMainStat = (item) => {
  if (!item) return 0;
  const base = item.originalMainStatValue || item.mainStatValue;
  const currentLv = item.isMythic ? item.mythicLevel : item.enhanceLevel;
  // Lưu ý: Công thức này là giả định, nên phụ thuộc vào cách Backend tính toán.
  return Math.floor(base * (1 + (currentLv + 1) * 0.1));
};
const getStatLabel = (code) => {
  const map = {
    ATK_FLAT: "Công",
    ATK_PERCENT: "Công %",
    HP_FLAT: "Máu",
    CRIT_RATE: "Chí Mạng",
    CRIT_DMG: "ST.Chí Mạng",
    SPEED: "Tốc Độ",
    DEF_FLAT: "Thủ",
    DEF_PERCENT: "Thủ %",
  };
  return map[code] || code;
};
const getRarityClass = (item) => {
  if (item.isMythic) return "mythic";
  return item.item.rarity ? item.item.rarity.toLowerCase() : "common";
};
const getRarityTextClass = (item) => {
  if (item.isMythic) return "text-mythic";
  return item.item.rarity
    ? `text-${item.item.rarity.toLowerCase()}`
    : "text-common";
};
const getRarityLabel = (rarity) => {
  const map = {
    COMMON: "Thường",
    UNCOMMON: "Phàm",
    RARE: "Hiếm",
    EPIC: "Sử Thi",
    LEGENDARY: "Huyền Thoại",
    MYTHIC: "Thần Thoại",
  };
  return map[rarity] || rarity;
};
const getRateColor = (r) =>
  r >= 80 ? "high-rate" : r >= 50 ? "mid-rate" : "low-rate";
const getFilterLabel = (t) => {
  const map = {
    ALL: "Tất cả",
    WEAPON: "Vũ khí",
    ARMOR: "Giáp trụ",
    ACCESSORY: "Trang sức",
  };
  return map[t] || t;
};
const handleImgError = (e) => {
  e.target.src = resolveItemImage("default_item.png");
};

onMounted(async () => {
  if (authStore.token) {
    await authStore.fetchProfile();
    await fetchInventory();
    // Tải dữ liệu nhân vật lần đầu sau khi login
    await characterStore.fetchCharacter();
  }
});
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;700&display=swap");

.forge-page {
  background-color: #0c0c0c;
  height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: "Noto Serif TC", serif;
  color: #dcdcdc;
}

.wood-bg-layer {
  position: absolute;
  inset: 0;
  background:
    url("https://www.transparenttextures.com/patterns/dark-wood.png"),
    linear-gradient(to bottom, #1a0f0a, #000);
  z-index: 0;
}

.fire-overlay {
  position: absolute;
  bottom: 0;
  width: 100%;
  height: 40%;
  background: radial-gradient(
    ellipse at bottom,
    rgba(255, 69, 0, 0.2) 0%,
    transparent 70%
  );
  pointer-events: none;
  animation: fire-flicker 4s infinite alternate;
}

@keyframes fire-flicker {
  0% {
    opacity: 0.5;
    transform: scaleY(1);
  }
  100% {
    opacity: 0.8;
    transform: scaleY(1.1);
  }
}

.forge-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.forge-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
  padding-top: 20px;
}

.title-ancient {
  font-family: "Cinzel", serif;
  font-size: 2.5rem;
  font-weight: 900;
  background: linear-gradient(180deg, #ffd700, #b8860b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 4px 10px rgba(0, 0, 0, 0.8);
  margin: 0;
}

.header-ornament {
  height: 2px;
  width: 100px;
  background: linear-gradient(90deg, transparent, #b8860b, transparent);
}

.forge-layout {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

.wood-panel {
  background: rgba(20, 15, 10, 0.9);
  border: 1px solid #5d4037;
  box-shadow:
    0 0 20px rgba(0, 0, 0, 0.8),
    inset 0 0 50px rgba(0, 0, 0, 0.5);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(5px);
}

.inventory-panel {
  width: 380px;
}

.panel-title {
  background: linear-gradient(90deg, #3e2723, #5d4037, #3e2723);
  padding: 12px;
  text-align: center;
  font-weight: bold;
  color: #ffcc80;
  border-bottom: 2px solid #8d6e63;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.filter-tabs {
  display: flex;
  justify-content: space-around;
  background: rgba(0, 0, 0, 0.3);
  padding: 5px;
}

.filter-tab {
  font-size: 0.8rem;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  color: #888;
  transition: 0.2s;
}

.filter-tab:hover,
.filter-tab.active {
  color: #ffd700;
  background: rgba(255, 255, 255, 0.1);
}

.item-grid {
  flex: 1;
  padding: 15px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(64px, 1fr));
  gap: 10px;
  align-content: start;
  overflow-y: auto;
}

.item-slot {
  aspect-ratio: 1;
  border: 2px solid #444;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.item-slot:hover {
  transform: translateY(-2px);
  border-color: #888;
}

.item-slot.selected {
  border-color: #ffd700;
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.4);
}

.slot-img-box {
  width: 100%;
  height: 100%;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-img-display {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.level-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 0.7rem;
  padding: 1px 4px;
  border-radius: 4px;
  border: 1px solid #555;
}

.empty-msg {
  grid-column: 1 / -1;
  text-align: center;
  color: #666;
  margin-top: 50px;
}

.anvil-panel {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.anvil-zone {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.main-slot-container {
  width: 140px;
  height: 140px;
  border: 4px double #8d6e63;
  border-radius: 12px;
  background: radial-gradient(
    circle,
    rgba(0, 0, 0, 0.6) 0%,
    rgba(0, 0, 0, 0.9) 100%
  );
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  position: relative;
  transition: 0.3s;
}

.main-slot-container.shaking {
  animation: shake 0.2s infinite;
  border-color: #ff5722;
  box-shadow: 0 0 30px #ff5722;
}

.main-slot img {
  width: 100px;
  height: 100px;
  object-fit: contain;
  filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5));
}

.empty-anvil {
  color: #5d4037;
  text-align: center;
}

.upgrade-info {
  width: 100%;
  max-width: 500px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #5d4037;
  padding: 20px;
  border-radius: 8px;
}

.item-title-large {
  text-align: center;
  font-size: 1.5rem;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.rank-badge {
  font-size: 0.7rem;
  background: #333;
  padding: 2px 6px;
  border-radius: 4px;
  color: #fff;
  border: 1px solid #555;
}

.level-compare {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.05);
  padding: 10px;
  border-radius: 6px;
}

.lv-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.lv-box .label {
  font-size: 0.7rem;
  color: #888;
  text-transform: uppercase;
}

.lv-box .val {
  font-size: 1.8rem;
  font-weight: bold;
  color: #bbb;
}

.lv-box .val.highlight {
  color: #4caf50;
  text-shadow: 0 0 10px rgba(76, 175, 80, 0.5);
}

.arrow-anim {
  color: #666;
  animation: slide-right 1s infinite;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #333;
}

.stat-row.main-stat {
  font-size: 1.1rem;
  color: #ffd700;
  border-bottom: 1px solid #5d4037;
  margin-bottom: 10px;
}

.stat-change {
  display: flex;
  gap: 10px;
  align-items: center;
}

.stat-change .new {
  color: #4caf50;
  font-weight: bold;
}

.cost-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 2px solid #333;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  justify-content: center;
}

.resource-row {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #222;
  padding: 5px 10px;
  border-radius: 20px;
  border: 1px solid #444;
}

.resource-row.not-enough {
  border-color: #f44336;
  color: #f44336;
}

.mat-icon {
  width: 20px;
  height: 20px;
}

.text-gold {
  color: #ffca28;
}
.rate-row {
  width: 100%;
  text-align: center;
  margin-top: 5px;
  font-size: 0.9rem;
  color: #888;
}
.high-rate {
  color: #4caf50;
}
.low-rate {
  color: #f44336;
}

.actions-group {
  margin-top: 20px;
}

.btn-forge {
  width: 100%;
  padding: 15px;
  font-family: "Cinzel", serif;
  font-size: 1.2rem;
  font-weight: bold;
  background: linear-gradient(180deg, #e65100, #bf360c);
  border: 1px solid #ff5722;
  color: white;
  cursor: pointer;
  transition: 0.2s;
  text-shadow: 0 2px 2px rgba(0, 0, 0, 0.5);
  position: relative;
  overflow: hidden;
}

.btn-forge::after {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    to right,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transform: rotate(45deg);
  animation: shine 3s infinite;
}

.btn-forge:hover:not(:disabled) {
  transform: scale(1.02);
  box-shadow: 0 0 20px rgba(255, 87, 34, 0.5);
}

.btn-forge:disabled {
  background: #444;
  border-color: #222;
  color: #888;
  cursor: not-allowed;
}

.btn-evolve {
  width: 100%;
  padding: 15px;
  background: #000;
  border: 1px solid #d50000;
  color: #d50000;
  font-weight: bold;
  cursor: pointer;
  font-family: "Cinzel", serif;
}

.btn-evolve:hover {
  background: #d50000;
  color: #fff;
  box-shadow: 0 0 15px #d50000;
}

@keyframes shake {
  0% {
    transform: translate(1px, 1px) rotate(0deg);
  }
  25% {
    transform: translate(-1px, -2px) rotate(-1deg);
  }
  50% {
    transform: translate(-3px, 0px) rotate(1deg);
  }
  75% {
    transform: translate(3px, 2px) rotate(0deg);
  }
  100% {
    transform: translate(1px, -1px) rotate(-1deg);
  }
}

@keyframes slide-right {
  0% {
    transform: translateX(0);
    opacity: 0.5;
  }
  50% {
    transform: translateX(5px);
    opacity: 1;
  }
  100% {
    transform: translateX(0);
    opacity: 0.5;
  }
}

@keyframes shine {
  0% {
    transform: translate(-100%, -100%) rotate(45deg);
  }
  20% {
    transform: translate(100%, 100%) rotate(45deg);
  }
  100% {
    transform: translate(100%, 100%) rotate(45deg);
  }
}

.result-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(5px);
}

.result-card {
  background: #1a1a1a;
  width: 400px;
  padding: 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
  border: 1px solid #333;
  animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.result-card.success {
  border-color: #4caf50;
  box-shadow: 0 0 30px rgba(76, 175, 80, 0.3);
}
.result-card.fail {
  border-color: #f44336;
  box-shadow: 0 0 30px rgba(244, 67, 54, 0.3);
}

.result-icon-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #000;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
}

.success .result-icon-circle {
  color: #4caf50;
  border: 2px solid #4caf50;
}
.fail .result-icon-circle {
  color: #f44336;
  border: 2px solid #f44336;
}

.result-title {
  font-family: "Cinzel", serif;
  font-size: 1.8rem;
  margin-bottom: 10px;
}

.btn-confirm {
  margin-top: 20px;
  padding: 10px 30px;
  background: transparent;
  border: 1px solid #fff;
  color: #fff;
  cursor: pointer;
  transition: 0.2s;
}

.btn-confirm:hover {
  background: #fff;
  color: #000;
}

@keyframes pop-in {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.text-common {
  color: #bdbdbd;
}
.text-uncommon {
  color: #81c784;
}
.text-rare {
  color: #42a5f5;
}
.text-epic {
  color: #ba68c8;
}
.text-legendary {
  color: #ffca28;
}
.text-mythic {
  color: #ff1744;
  text-shadow: 0 0 5px #ff1744;
}

.border-mythic {
  border-color: #ff1744 !important;
}
.mythic-glow-anim {
  animation: pulse-red 2s infinite;
}

@keyframes pulse-red {
  0% {
    box-shadow: 0 0 10px #ff1744;
  }
  50% {
    box-shadow: 0 0 25px #ff1744;
  }
  100% {
    box-shadow: 0 0 10px #ff1744;
  }
}

.error-text {
  color: #ff5252;
  margin-top: 10px;
  font-size: 0.9rem;
}

.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}
</style>
