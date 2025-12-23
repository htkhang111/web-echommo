<!-- <template>
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
                  :src="resolveItemImage(selectedItem.item.image || selectedItem.item.imageUrl)"
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
                <h3 class="item-title-large" :class="getRarityTextClass(selectedItem)">
                  {{ selectedItem.item.name }}
                  <span class="rank-badge">{{ getRarityLabel(selectedItem.item.rarity) }}</span>
                </h3>

                <div class="level-compare">
                  <div class="lv-box curr">
                    <span class="label">Hiện tại</span>
                    <span class="val">
                      {{ selectedItem.isMythic ? "M" + selectedItem.mythicLevel : "+" + selectedItem.enhanceLevel }}
                    </span>
                  </div>
                  <div class="arrow-anim"><i class="fas fa-angle-double-right"></i></div>
                  <div class="lv-box next">
                    <span class="label">Sau khi rèn</span>
                    <span class="val highlight">
                      {{ selectedItem.isMythic ? "M" + (selectedItem.mythicLevel + 1) : "+" + (selectedItem.enhanceLevel + 1) }}
                    </span>
                  </div>
                </div>

                <div class="stats-preview custom-scroll">
                  <div class="stat-row main-stat">
                    <span>{{ getStatLabel(selectedItem.mainStatType) }}</span>
                    <div class="stat-change">
                      <span class="old">{{ formatNumber(selectedItem.mainStatValue) }}</span>
                      <i class="fas fa-arrow-right"></i>
                      <span class="new">{{ formatNumber(getPredictedMainStat(selectedItem)) }}</span>
                    </div>
                  </div>
                  <div v-for="(sub, idx) in parsedSubStats" :key="idx" class="stat-row sub-stat">
                    <span class="sub-dot">•</span>
                    <span>{{ getStatLabel(sub.code) }}</span>
                    <span class="val">+{{ formatNumber(sub.value) }}{{ sub.isPercent ? "%" : "" }}</span>
                  </div>
                </div>

                <div class="cost-section">
                   <div class="resource-row" :class="{ 'not-enough': !canAffordGold }">
                    <i class="fas fa-coins text-gold"></i>
                    <span>{{ formatNumber(upgradeCost.gold) }} Vàng</span>
                  </div>
                  
                  <div class="resource-row" v-for="(mat, idx) in upgradeCost.materials" :key="idx">
                    <img :src="resolveItemImage(mat.img)" class="mat-icon" @error="handleImgError" />
                    <span>{{ mat.qty }} {{ mat.name }}</span>
                  </div>

                  <div class="rate-row">
                    Tỉ lệ thành công: <span class="high-rate">100%</span>
                  </div>
                </div>

                <div class="actions-group">
                  <button
                    v-if="!selectedItem.isMythic && selectedItem.enhanceLevel < 30"
                    class="btn-forge"
                    @click="handleUpgrade"
                    :disabled="isForging"
                  >
                    <span v-if="!isForging">CƯỜNG HÓA</span>
                    <span v-else><i class="fas fa-spinner fa-spin"></i> ĐANG RÈN...</span>
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
                    v-if="!selectedItem.isMythic && selectedItem.enhanceLevel >= 30"
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
        <div class="result-card success" @click.stop>
          <div class="result-header">
            <div class="result-icon-circle"><i class="fas fa-check"></i></div>
          </div>
          <div class="result-body">
            <h2 class="result-title">THÀNH CÔNG!</h2>
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
import { useMarketStore } from "../stores/marketStore";
import { resolveItemImage } from "@/utils/assetHelper";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const characterStore = useCharacterStore();
const marketStore = useMarketStore();

const selectedItem = ref(null);
const isForging = ref(false);
const showResult = ref(false);
const resultMessage = ref("");
const errorMessage = ref("");
const filterType = ref("ALL");

// --- COMPUTED ---
const sortedEquipableItems = computed(() => {
  const items = inventoryStore.items || [];
  if (!Array.isArray(items)) return [];

  let filtered = items.filter(
    (i) => i.item && ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"].includes(i.item.type)
  );

  if (filterType.value === "WEAPON") filtered = filtered.filter((i) => i.item.type === "WEAPON");
  else if (filterType.value === "ARMOR") filtered = filtered.filter((i) => ["ARMOR", "HELMET", "BOOTS"].includes(i.item.type));
  else if (filterType.value === "ACCESSORY") filtered = filtered.filter((i) => ["RING", "NECKLACE"].includes(i.item.type));

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

// [LOGIC ĐỒNG BỘ BACKEND] Tính toán chi phí
const upgradeCost = computed(() => {
  if (!selectedItem.value) return { gold: 0, materials: [] };
  
  const item = selectedItem.value;
  const type = item.item.type;
  
  // Logic Mythic Upgrade (Tăng cấp cho đồ Mythic)
  if (item.isMythic) {
      const nextLv = item.mythicLevel + 1;
      return {
          gold: nextLv * 100000,
          materials: [
             { name: "Nguyên liệu lạ", qty: nextLv * 50, img: "r_mystrile_node.png" }, // ID 12
             { name: "Echo Coin", qty: nextLv, img: "r_echo_coin.png" } // ID 11
          ]
      };
  }

  // Logic Normal Enhance (+1 -> +30)
  const nextLv = item.enhanceLevel + 1;
  let gold = 0;
  let mats = [];

  if (nextLv <= 10) {
      gold = nextLv * 1000;
      const mainQty = nextLv * 15;
      const subQty = nextLv * 5;
      
      // Mat 1 (Chính)
      if (type === 'WEAPON') mats.push({ name: "Quặng Đồng", qty: mainQty, img: "r_copper_node.png" }); // ID 6
      else mats.push({ name: "Đá", qty: mainQty, img: "stone_1.png" }); // ID 5
      
      // Mat 2 (Phụ)
      mats.push({ name: "Gỗ Sồi", qty: subQty, img: "r_wood.png" }); // ID 1
  } 
  else if (nextLv <= 20) {
      gold = nextLv * 3000;
      const scale = nextLv - 10;
      mats.push({ name: "Sắt", qty: scale * 15, img: "r_silver_bar.png" }); // ID 7
      mats.push({ name: "Gỗ Khô", qty: scale * 5, img: "r_red_wood.png" }); // ID 2
  } 
  else { // 21 - 30
      gold = nextLv * 10000;
      const scale = nextLv - 20;
      mats.push({ name: "Bạch Kim", qty: scale * 20, img: "r_mystrile_bar.png" }); // ID 8
      mats.push({ name: "Gỗ Lạnh", qty: scale * 10, img: "r_white_wood.png" }); // ID 3
  }

  return { gold, materials: mats };
});

const canAffordGold = computed(() => {
  const userGold = authStore.wallet?.gold || 0;
  return userGold >= upgradeCost.value.gold;
});

// --- ACTIONS ---
const selectItem = (item) => {
  if (isForging.value) return;
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";

  try {
    const url = `/equipment/enhance/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
        inventoryStore.fetchInventory(),
        characterStore.fetchCharacter(),
        authStore.fetchProfile(),
        marketStore.refresh()
    ]);

    const updatedItem = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    selectedItem.value = updatedItem || null;

    showResultModal(`Cường hóa lên +${selectedItem.value.enhanceLevel} thành công!`);
  } catch (err) {
    console.error(err);
    errorMessage.value = err.response?.data?.message || err.response?.data || "Thiếu nguyên liệu hoặc lỗi hệ thống.";
  } finally {
    isForging.value = false;
  }
};

const handleMythicUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";
  try {
      const url = `/equipment/upgrade-mythic/${selectedItem.value.userItemId}`;
      await axiosClient.post(url);

      await Promise.all([
        inventoryStore.fetchInventory(),
        characterStore.fetchCharacter(),
        authStore.fetchProfile()
      ]);

      const updatedItem = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
      selectedItem.value = updatedItem || null;

      showResultModal("Thăng cấp Thần thoại thành công!");
  } catch (err) {
      console.error(err);
      errorMessage.value = err.response?.data?.message || err.response?.data || "Thất bại.";
  } finally {
      isForging.value = false;
  }
};

const showResultModal = (msg) => {
  resultMessage.value = msg;
  showResult.value = true;
};

const closeResult = () => {
  showResult.value = false;
  errorMessage.value = "";
};

// Helpers
const formatNumber = (n) => Number(n || 0).toLocaleString();
const getPredictedMainStat = (item) => {
  if (!item) return 0;
  const base = item.originalMainStatValue || item.mainStatValue;
  const currentLv = item.isMythic ? item.mythicLevel : item.enhanceLevel;
  return Math.floor(base * (1 + (currentLv + 1) * 0.1)); // Ước lượng hiển thị
};
const getStatLabel = (code) => {
  const map = { ATK_FLAT: "Công", ATK_PERCENT: "Công %", HP_FLAT: "Máu", CRIT_RATE: "Chí Mạng", CRIT_DMG: "ST.Chí Mạng", SPEED: "Tốc Độ", DEF_FLAT: "Thủ", DEF_PERCENT: "Thủ %" };
  return map[code] || code;
};
const getRarityClass = (item) => item.isMythic ? "mythic" : (item.item.rarity ? item.item.rarity.toLowerCase() : "common");
const getRarityTextClass = (item) => item.isMythic ? "text-mythic" : (item.item.rarity ? `text-${item.item.rarity.toLowerCase()}` : "text-common");
const getRarityLabel = (rarity) => {
  const map = { COMMON: "Thường", UNCOMMON: "Phàm", RARE: "Hiếm", EPIC: "Sử Thi", LEGENDARY: "Huyền Thoại", MYTHIC: "Thần Thoại" };
  return map[rarity] || rarity;
};
const getFilterLabel = (t) => { const map = { ALL: "Tất cả", WEAPON: "Vũ khí", ARMOR: "Giáp trụ", ACCESSORY: "Trang sức" }; return map[t] || t; };
const handleImgError = (e) => { e.target.src = resolveItemImage("default_item.png"); };

onMounted(async () => {
  if (authStore.token) {
    await authStore.fetchProfile();
    await fetchInventory();
    await characterStore.fetchCharacter();
  }
});
const fetchInventory = async () => { await inventoryStore.fetchInventory(); };
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;700&display=swap");

.forge-page { background-color: #0c0c0c; height: 100vh; position: relative; overflow: hidden; font-family: "Noto Serif TC", serif; color: #dcdcdc; }
.wood-bg-layer { position: absolute; inset: 0; background: url("https://www.transparenttextures.com/patterns/dark-wood.png"), linear-gradient(to bottom, #1a0f0a, #000); z-index: 0; }
.fire-overlay { position: absolute; bottom: 0; width: 100%; height: 40%; background: radial-gradient(ellipse at bottom, rgba(255, 69, 0, 0.2) 0%, transparent 70%); pointer-events: none; animation: fire-flicker 4s infinite alternate; }
@keyframes fire-flicker { 0% { opacity: 0.5; transform: scaleY(1); } 100% { opacity: 0.8; transform: scaleY(1.1); } }
.forge-wrapper { position: relative; z-index: 10; max-width: 1200px; margin: 0 auto; padding: 20px; height: 100vh; display: flex; flex-direction: column; }
.forge-header { display: flex; align-items: center; justify-content: center; gap: 20px; margin-bottom: 20px; padding-top: 20px; }
.title-ancient { font-family: "Cinzel", serif; font-size: 2.5rem; font-weight: 900; background: linear-gradient(180deg, #ffd700, #b8860b); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0 4px 10px rgba(0, 0, 0, 0.8); margin: 0; }
.header-ornament { height: 2px; width: 100px; background: linear-gradient(90deg, transparent, #b8860b, transparent); }
.forge-layout { display: flex; gap: 20px; flex: 1; min-height: 0; }
.wood-panel { background: rgba(20, 15, 10, 0.9); border: 1px solid #5d4037; box-shadow: 0 0 20px rgba(0, 0, 0, 0.8), inset 0 0 50px rgba(0, 0, 0, 0.5); border-radius: 8px; display: flex; flex-direction: column; backdrop-filter: blur(5px); }
.inventory-panel { width: 380px; }
.panel-title { background: linear-gradient(90deg, #3e2723, #5d4037, #3e2723); padding: 12px; text-align: center; font-weight: bold; color: #ffcc80; border-bottom: 2px solid #8d6e63; text-transform: uppercase; letter-spacing: 1px; }
.filter-tabs { display: flex; justify-content: space-around; background: rgba(0, 0, 0, 0.3); padding: 5px; }
.filter-tab { font-size: 0.8rem; cursor: pointer; padding: 5px 10px; border-radius: 4px; color: #888; transition: 0.2s; }
.filter-tab:hover, .filter-tab.active { color: #ffd700; background: rgba(255, 255, 255, 0.1); }
.item-grid { flex: 1; padding: 15px; display: grid; grid-template-columns: repeat(auto-fill, minmax(64px, 1fr)); gap: 10px; align-content: start; overflow-y: auto; }
.item-slot { aspect-ratio: 1; border: 2px solid #444; background: rgba(0, 0, 0, 0.4); border-radius: 6px; cursor: pointer; position: relative; transition: all 0.2s; }
.item-slot:hover { transform: translateY(-2px); border-color: #888; }
.item-slot.selected { border-color: #ffd700; box-shadow: 0 0 15px rgba(255, 215, 0, 0.4); }
.slot-img-box { width: 100%; height: 100%; padding: 5px; display: flex; align-items: center; justify-content: center; }
.item-img-display { max-width: 100%; max-height: 100%; object-fit: contain; }
.level-badge { position: absolute; bottom: 2px; right: 2px; background: rgba(0, 0, 0, 0.8); color: #fff; font-size: 0.7rem; padding: 1px 4px; border-radius: 4px; border: 1px solid #555; }
.empty-msg { grid-column: 1 / -1; text-align: center; color: #666; margin-top: 50px; }
.anvil-panel { flex: 1; position: relative; overflow: hidden; }
.anvil-zone { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px; }
.main-slot-container { width: 140px; height: 140px; border: 4px double #8d6e63; border-radius: 12px; background: radial-gradient(circle, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0.9) 100%); display: flex; align-items: center; justify-content: center; margin-bottom: 20px; position: relative; transition: 0.3s; }
.main-slot-container.shaking { animation: shake 0.2s infinite; border-color: #ff5722; box-shadow: 0 0 30px #ff5722; }
.main-slot img { width: 100px; height: 100px; object-fit: contain; filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5)); }
.empty-anvil { color: #5d4037; text-align: center; }
.upgrade-info { width: 100%; max-width: 500px; background: rgba(0, 0, 0, 0.6); border: 1px solid #5d4037; padding: 20px; border-radius: 8px; }
.item-title-large { text-align: center; font-size: 1.5rem; margin-bottom: 15px; display: flex; align-items: center; justify-content: center; gap: 10px; }
.rank-badge { font-size: 0.7rem; background: #333; padding: 2px 6px; border-radius: 4px; color: #fff; border: 1px solid #555; }
.level-compare { display: flex; justify-content: center; align-items: center; gap: 20px; margin-bottom: 20px; background: rgba(255, 255, 255, 0.05); padding: 10px; border-radius: 6px; }
.lv-box { display: flex; flex-direction: column; align-items: center; }
.lv-box .label { font-size: 0.7rem; color: #888; text-transform: uppercase; }
.lv-box .val { font-size: 1.8rem; font-weight: bold; color: #bbb; }
.lv-box .val.highlight { color: #4caf50; text-shadow: 0 0 10px rgba(76, 175, 80, 0.5); }
.arrow-anim { color: #666; animation: slide-right 1s infinite; }
.stat-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #333; }
.stat-row.main-stat { font-size: 1.1rem; color: #ffd700; border-bottom: 1px solid #5d4037; margin-bottom: 10px; }
.stat-change { display: flex; gap: 10px; align-items: center; }
.stat-change .new { color: #4caf50; font-weight: bold; }
.cost-section { margin-top: 20px; padding-top: 15px; border-top: 2px solid #333; display: flex; flex-wrap: wrap; gap: 15px; justify-content: center; }
.resource-row { display: flex; align-items: center; gap: 5px; background: #222; padding: 5px 10px; border-radius: 20px; border: 1px solid #444; }
.resource-row.not-enough { border-color: #f44336; color: #f44336; }
.mat-icon { width: 20px; height: 20px; }
.text-gold { color: #ffca28; }
.rate-row { width: 100%; text-align: center; margin-top: 5px; font-size: 0.9rem; color: #888; }
.high-rate { color: #4caf50; font-weight: bold; }
.actions-group { margin-top: 20px; }
.btn-forge { width: 100%; padding: 15px; font-family: "Cinzel", serif; font-size: 1.2rem; font-weight: bold; background: linear-gradient(180deg, #e65100, #bf360c); border: 1px solid #ff5722; color: white; cursor: pointer; transition: 0.2s; text-shadow: 0 2px 2px rgba(0, 0, 0, 0.5); position: relative; overflow: hidden; }
.btn-forge:hover:not(:disabled) { transform: scale(1.02); box-shadow: 0 0 20px rgba(255, 87, 34, 0.5); }
.btn-forge:disabled { background: #444; border-color: #222; color: #888; cursor: not-allowed; }
.btn-mythic-upgrade { width: 100%; padding: 15px; background: #000; border: 1px solid #d50000; color: #d50000; font-weight: bold; cursor: pointer; font-family: "Cinzel", serif; }
.btn-mythic-upgrade:hover { background: #d50000; color: #fff; box-shadow: 0 0 15px #d50000; }
@keyframes shake { 0% { transform: translate(1px, 1px) rotate(0deg); } 25% { transform: translate(-1px, -2px) rotate(-1deg); } 50% { transform: translate(-3px, 0px) rotate(1deg); } 75% { transform: translate(3px, 2px) rotate(0deg); } 100% { transform: translate(1px, -1px) rotate(-1deg); } }
@keyframes slide-right { 0% { transform: translateX(0); opacity: 0.5; } 50% { transform: translateX(5px); opacity: 1; } 100% { transform: translateX(0); opacity: 0.5; } }
.result-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 100; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(5px); }
.result-card { background: #1a1a1a; width: 400px; padding: 30px; border-radius: 12px; text-align: center; box-shadow: 0 0 50px rgba(0, 0, 0, 0.8); border: 1px solid #333; animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.result-card.success { border-color: #4caf50; box-shadow: 0 0 30px rgba(76, 175, 80, 0.3); }
.result-icon-circle { width: 80px; height: 80px; border-radius: 50%; background: #000; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; color: #4caf50; border: 2px solid #4caf50; }
.result-title { font-family: "Cinzel", serif; font-size: 1.8rem; margin-bottom: 10px; }
.btn-confirm { margin-top: 20px; padding: 10px 30px; background: transparent; border: 1px solid #fff; color: #fff; cursor: pointer; transition: 0.2s; }
.btn-confirm:hover { background: #fff; color: #000; }
@keyframes pop-in { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.text-common { color: #bdbdbd; } .text-uncommon { color: #81c784; } .text-rare { color: #42a5f5; } .text-epic { color: #ba68c8; } .text-legendary { color: #ffca28; } .text-mythic { color: #ff1744; text-shadow: 0 0 5px #ff1744; }
.border-mythic { border-color: #ff1744 !important; } .mythic-glow-anim { animation: pulse-red 2s infinite; }
@keyframes pulse-red { 0% { box-shadow: 0 0 10px #ff1744; } 50% { box-shadow: 0 0 25px #ff1744; } 100% { box-shadow: 0 0 10px #ff1744; } }
.error-text { color: #ff5252; margin-top: 10px; font-size: 0.9rem; }
.custom-scroll::-webkit-scrollbar { width: 6px; } .custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; } .custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); }
</style> -->

<!-- <template>
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

        <div class="anvil-panel wood-panel custom-scroll">
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
                  :src="resolveItemImage(selectedItem.item.image || selectedItem.item.imageUrl)"
                />
                <div class="glow-ring"></div>
              </div>
              <div class="empty-anvil" v-else>
                <i class="fas fa-hammer"></i>
                <p>Chọn trang bị</p>
              </div>
            </div>

            <transition name="fade">
              <div v-if="selectedItem" class="upgrade-info">
                <h3 class="item-title-large" :class="getRarityTextClass(selectedItem)">
                  {{ selectedItem.item.name }}
                  <span class="rank-badge">{{ getRarityLabel(selectedItem.item.rarity) }}</span>
                </h3>

                <div class="level-compare">
                  <div class="lv-box curr">
                    <span class="label">Hiện tại</span>
                    <span class="val">
                      {{ selectedItem.isMythic ? "M" + selectedItem.mythicLevel : "+" + selectedItem.enhanceLevel }}
                    </span>
                  </div>
                  <div class="arrow-anim"><i class="fas fa-angle-double-right"></i></div>
                  <div class="lv-box next">
                    <span class="label">Sau khi rèn</span>
                    <span class="val highlight">
                      {{ selectedItem.isMythic ? "M" + (selectedItem.mythicLevel + 1) : "+" + (selectedItem.enhanceLevel + 1) }}
                    </span>
                  </div>
                </div>

                <div class="stats-preview custom-scroll">
                  <div class="stat-row main-stat">
                    <span>{{ getStatLabel(selectedItem.mainStatType) }}</span>
                    <div class="stat-change">
                      <span class="old">{{ formatNumber(selectedItem.mainStatValue) }}</span>
                      <i class="fas fa-arrow-right"></i>
                      <span class="new">{{ formatNumber(getPredictedMainStat(selectedItem)) }}</span>
                    </div>
                  </div>
                  <div v-for="(sub, idx) in parsedSubStats" :key="idx" class="stat-row sub-stat">
                    <span class="sub-dot">•</span>
                    <span>{{ getStatLabel(sub.code) }}</span>
                    <span class="val">+{{ formatNumber(sub.value) }}{{ sub.isPercent ? "%" : "" }}</span>
                  </div>
                </div>

                <div class="cost-section">
                   <div class="resource-row" :class="{ 'not-enough': !canAffordGold }">
                    <i class="fas fa-coins text-gold"></i>
                    <span>{{ formatNumber(upgradeCost.gold) }} Vàng</span>
                  </div>
                  
                  <div class="resource-row" v-for="(mat, idx) in upgradeCost.materials" :key="idx">
                    <img :src="resolveItemImage(mat.img)" class="mat-icon" @error="handleImgError" />
                    <span>{{ mat.qty }} {{ mat.name }}</span>
                  </div>

                  <div class="rate-row">
                    Tỉ lệ: <span class="high-rate">100%</span>
                  </div>
                </div>

                <div class="actions-group">
                  <button
                    v-if="!selectedItem.isMythic && selectedItem.enhanceLevel < 30"
                    class="btn-forge"
                    @click="handleUpgrade"
                    :disabled="isForging"
                  >
                    <span v-if="!isForging">CƯỜNG HÓA</span>
                    <span v-else><i class="fas fa-spinner fa-spin"></i> ĐANG RÈN...</span>
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
                    v-if="!selectedItem.isMythic && selectedItem.enhanceLevel >= 30"
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
        <div class="result-card success" @click.stop>
          <div class="result-header">
            <div class="result-icon-circle"><i class="fas fa-check"></i></div>
          </div>
          <div class="result-body">
            <h2 class="result-title">THÀNH CÔNG!</h2>
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
import { useMarketStore } from "../stores/marketStore";
import { resolveItemImage } from "@/utils/assetHelper";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const characterStore = useCharacterStore();
const marketStore = useMarketStore();

const selectedItem = ref(null);
const isForging = ref(false);
const showResult = ref(false);
const resultMessage = ref("");
const errorMessage = ref("");
const filterType = ref("ALL");

// --- COMPUTED ---
const sortedEquipableItems = computed(() => {
  const items = inventoryStore.items || [];
  if (!Array.isArray(items)) return [];

  let filtered = items.filter(
    (i) => i.item && ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"].includes(i.item.type)
  );

  if (filterType.value === "WEAPON") filtered = filtered.filter((i) => i.item.type === "WEAPON");
  else if (filterType.value === "ARMOR") filtered = filtered.filter((i) => ["ARMOR", "HELMET", "BOOTS"].includes(i.item.type));
  else if (filterType.value === "ACCESSORY") filtered = filtered.filter((i) => ["RING", "NECKLACE"].includes(i.item.type));

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

// [LOGIC ĐỒNG BỘ BACKEND] Tính toán chi phí
const upgradeCost = computed(() => {
  if (!selectedItem.value) return { gold: 0, materials: [] };
  
  const item = selectedItem.value;
  const type = item.item.type;
  
  // Logic Mythic Upgrade (Tăng cấp cho đồ Mythic)
  if (item.isMythic) {
      const nextLv = item.mythicLevel + 1;
      return {
          gold: nextLv * 100000,
          materials: [
             { name: "Nguyên liệu lạ", qty: nextLv * 50, img: "r_mystrile_node.png" }, // ID 12
             { name: "Echo Coin", qty: nextLv, img: "r_echo_coin.png" } // ID 11
          ]
      };
  }

  // Logic Normal Enhance (+1 -> +30)
  const nextLv = item.enhanceLevel + 1;
  let gold = 0;
  let mats = [];

  if (nextLv <= 10) {
      gold = nextLv * 1000;
      const mainQty = nextLv * 15;
      const subQty = nextLv * 5;
      
      // Mat 1 (Chính)
      if (type === 'WEAPON') mats.push({ name: "Quặng Đồng", qty: mainQty, img: "r_copper_node.png" }); // ID 6
      else mats.push({ name: "Đá", qty: mainQty, img: "stone_1.png" }); // ID 5
      
      // Mat 2 (Phụ)
      mats.push({ name: "Gỗ Sồi", qty: subQty, img: "r_wood.png" }); // ID 1
  } 
  else if (nextLv <= 20) {
      gold = nextLv * 3000;
      const scale = nextLv - 10;
      mats.push({ name: "Sắt", qty: scale * 15, img: "r_silver_bar.png" }); // ID 7
      mats.push({ name: "Gỗ Khô", qty: scale * 5, img: "r_red_wood.png" }); // ID 2
  } 
  else { // 21 - 30
      gold = nextLv * 10000;
      const scale = nextLv - 20;
      mats.push({ name: "Bạch Kim", qty: scale * 20, img: "r_mystrile_bar.png" }); // ID 8
      mats.push({ name: "Gỗ Lạnh", qty: scale * 10, img: "r_white_wood.png" }); // ID 3
  }

  return { gold, materials: mats };
});

const canAffordGold = computed(() => {
  const userGold = authStore.wallet?.gold || 0;
  return userGold >= upgradeCost.value.gold;
});

// --- ACTIONS ---
const selectItem = (item) => {
  if (isForging.value) return;
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";

  try {
    const url = `/equipment/enhance/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
        inventoryStore.fetchInventory(),
        characterStore.fetchCharacter(),
        authStore.fetchProfile(),
        marketStore.refresh()
    ]);

    const updatedItem = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    selectedItem.value = updatedItem || null;

    showResultModal(`Cường hóa lên +${selectedItem.value.enhanceLevel} thành công!`);
  } catch (err) {
    console.error(err);
    errorMessage.value = err.response?.data?.message || err.response?.data || "Thiếu nguyên liệu hoặc lỗi hệ thống.";
  } finally {
    isForging.value = false;
  }
};

const handleMythicUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";
  try {
      const url = `/equipment/upgrade-mythic/${selectedItem.value.userItemId}`;
      await axiosClient.post(url);

      await Promise.all([
        inventoryStore.fetchInventory(),
        characterStore.fetchCharacter(),
        authStore.fetchProfile()
      ]);

      const updatedItem = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
      selectedItem.value = updatedItem || null;

      showResultModal("Thăng cấp Thần thoại thành công!");
  } catch (err) {
      console.error(err);
      errorMessage.value = err.response?.data?.message || err.response?.data || "Thất bại.";
  } finally {
      isForging.value = false;
  }
};

const showResultModal = (msg) => {
  resultMessage.value = msg;
  showResult.value = true;
};

const closeResult = () => {
  showResult.value = false;
  errorMessage.value = "";
};

// Helpers
const formatNumber = (n) => Number(n || 0).toLocaleString();
const getPredictedMainStat = (item) => {
  if (!item) return 0;
  const base = item.originalMainStatValue || item.mainStatValue;
  const currentLv = item.isMythic ? item.mythicLevel : item.enhanceLevel;
  return Math.floor(base * (1 + (currentLv + 1) * 0.1)); // Ước lượng hiển thị
};
const getStatLabel = (code) => {
  const map = { ATK_FLAT: "Công", ATK_PERCENT: "Công %", HP_FLAT: "Máu", CRIT_RATE: "Chí Mạng", CRIT_DMG: "ST.Chí Mạng", SPEED: "Tốc Độ", DEF_FLAT: "Thủ", DEF_PERCENT: "Thủ %" };
  return map[code] || code;
};
const getRarityClass = (item) => item.isMythic ? "mythic" : (item.item.rarity ? item.item.rarity.toLowerCase() : "common");
const getRarityTextClass = (item) => item.isMythic ? "text-mythic" : (item.item.rarity ? `text-${item.item.rarity.toLowerCase()}` : "text-common");
const getRarityLabel = (rarity) => {
  const map = { COMMON: "Thường", UNCOMMON: "Phàm", RARE: "Hiếm", EPIC: "Sử Thi", LEGENDARY: "Huyền Thoại", MYTHIC: "Thần Thoại" };
  return map[rarity] || rarity;
};
const getFilterLabel = (t) => { const map = { ALL: "Tất cả", WEAPON: "Vũ khí", ARMOR: "Giáp trụ", ACCESSORY: "Trang sức" }; return map[t] || t; };
const handleImgError = (e) => { e.target.src = resolveItemImage("default_item.png"); };

onMounted(async () => {
  if (authStore.token) {
    await authStore.fetchProfile();
    await fetchInventory();
    await characterStore.fetchCharacter();
  }
});
const fetchInventory = async () => { await inventoryStore.fetchInventory(); };
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;700&display=swap");

.forge-page { background-color: #0c0c0c; height: 100vh; position: relative; overflow: hidden; font-family: "Noto Serif TC", serif; color: #dcdcdc; }
.wood-bg-layer { position: absolute; inset: 0; background: url("https://www.transparenttextures.com/patterns/dark-wood.png"), linear-gradient(to bottom, #1a0f0a, #000); z-index: 0; }
.fire-overlay { position: absolute; bottom: 0; width: 100%; height: 40%; background: radial-gradient(ellipse at bottom, rgba(255, 69, 0, 0.2) 0%, transparent 70%); pointer-events: none; animation: fire-flicker 4s infinite alternate; }
@keyframes fire-flicker { 0% { opacity: 0.5; transform: scaleY(1); } 100% { opacity: 0.8; transform: scaleY(1.1); } }
.forge-wrapper { position: relative; z-index: 10; max-width: 1200px; margin: 0 auto; padding: 20px; height: 100vh; display: flex; flex-direction: column; }
.forge-header { display: flex; align-items: center; justify-content: center; gap: 20px; margin-bottom: 20px; padding-top: 20px; }
.title-ancient { font-family: "Cinzel", serif; font-size: 2.5rem; font-weight: 900; background: linear-gradient(180deg, #ffd700, #b8860b); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0 4px 10px rgba(0, 0, 0, 0.8); margin: 0; }
.header-ornament { height: 2px; width: 100px; background: linear-gradient(90deg, transparent, #b8860b, transparent); }
.forge-layout { display: flex; gap: 20px; flex: 1; min-height: 0; }
.wood-panel { background: rgba(20, 15, 10, 0.9); border: 1px solid #5d4037; box-shadow: 0 0 20px rgba(0, 0, 0, 0.8), inset 0 0 50px rgba(0, 0, 0, 0.5); border-radius: 8px; display: flex; flex-direction: column; backdrop-filter: blur(5px); }
.inventory-panel { width: 380px; }
.panel-title { background: linear-gradient(90deg, #3e2723, #5d4037, #3e2723); padding: 12px; text-align: center; font-weight: bold; color: #ffcc80; border-bottom: 2px solid #8d6e63; text-transform: uppercase; letter-spacing: 1px; }
.filter-tabs { display: flex; justify-content: space-around; background: rgba(0, 0, 0, 0.3); padding: 5px; }
.filter-tab { font-size: 0.8rem; cursor: pointer; padding: 5px 10px; border-radius: 4px; color: #888; transition: 0.2s; }
.filter-tab:hover, .filter-tab.active { color: #ffd700; background: rgba(255, 255, 255, 0.1); }
.item-grid { flex: 1; padding: 15px; display: grid; grid-template-columns: repeat(auto-fill, minmax(64px, 1fr)); gap: 10px; align-content: start; overflow-y: auto; }
.item-slot { aspect-ratio: 1; border: 2px solid #444; background: rgba(0, 0, 0, 0.4); border-radius: 6px; cursor: pointer; position: relative; transition: all 0.2s; }
.item-slot:hover { transform: translateY(-2px); border-color: #888; }
.item-slot.selected { border-color: #ffd700; box-shadow: 0 0 15px rgba(255, 215, 0, 0.4); }
.slot-img-box { width: 100%; height: 100%; padding: 5px; display: flex; align-items: center; justify-content: center; }
.item-img-display { max-width: 100%; max-height: 100%; object-fit: contain; }
.level-badge { position: absolute; bottom: 2px; right: 2px; background: rgba(0, 0, 0, 0.8); color: #fff; font-size: 0.7rem; padding: 1px 4px; border-radius: 4px; border: 1px solid #555; }
.empty-msg { grid-column: 1 / -1; text-align: center; color: #666; margin-top: 50px; }

/* --- KHU VỰC RÈN (COMPACT & SCROLLABLE) --- */
.anvil-panel { 
  flex: 1; 
  position: relative; 
  overflow-y: auto; /* Cho phép cuộn */
  display: flex;
  flex-direction: column;
}

.anvil-zone { 
  min-height: 100%; 
  height: auto;
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: flex-start; /* Nội dung bắt đầu từ trên */
  padding: 20px 10px; 
}

.main-slot-container { 
  width: 100px; /* Nhỏ gọn */
  height: 100px; 
  border: 3px double #8d6e63; 
  border-radius: 12px; 
  background: radial-gradient(circle, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0.9) 100%); 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  margin-bottom: 15px; 
  position: relative; 
  transition: 0.3s; 
  flex-shrink: 0; 
}

.main-slot-container.shaking { animation: shake 0.2s infinite; border-color: #ff5722; box-shadow: 0 0 20px #ff5722; }
.main-slot img { width: 70px; height: 70px; object-fit: contain; filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5)); }
.empty-anvil { color: #5d4037; text-align: center; font-size: 0.8rem; }
.empty-anvil i { font-size: 2rem !important; margin-bottom: 5px; display: block; }

/* --- BẢNG THÔNG TIN NÂNG CẤP (COMPACT VERSION) --- */
.upgrade-info { 
  width: 100%; 
  max-width: 380px; /* Thu hẹp chiều ngang */
  background: rgba(0, 0, 0, 0.6); 
  border: 1px solid #5d4037; 
  padding: 15px; 
  border-radius: 8px; 
}

.item-title-large { 
  text-align: center; 
  font-size: 1.1rem; 
  margin-bottom: 10px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  gap: 8px; 
}

.rank-badge { 
  font-size: 0.6rem; 
  background: #333; 
  padding: 1px 5px; 
  border-radius: 3px; 
  color: #fff; 
  border: 1px solid #555; 
}

.level-compare { 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  gap: 15px; 
  margin-bottom: 10px; 
  background: rgba(255, 255, 255, 0.03); 
  padding: 8px; 
  border-radius: 6px; 
}

.lv-box { display: flex; flex-direction: column; align-items: center; }
.lv-box .label { font-size: 0.65rem; color: #888; text-transform: uppercase; }
.lv-box .val { font-size: 1.4rem; font-weight: bold; color: #bbb; }
.lv-box .val.highlight { color: #4caf50; text-shadow: 0 0 5px rgba(76, 175, 80, 0.5); }

.arrow-anim { color: #666; font-size: 0.8rem; animation: slide-right 1s infinite; }

.stats-preview { 
    max-height: 120px; 
    overflow-y: auto;
    margin-bottom: 10px;
}

.stat-row { display: flex; justify-content: space-between; padding: 4px 0; border-bottom: 1px dashed #333; font-size: 0.85rem; }
.stat-row.main-stat { font-size: 0.95rem; color: #ffd700; border-bottom: 1px solid #5d4037; margin-bottom: 5px; }
.stat-change { display: flex; gap: 5px; align-items: center; }
.stat-change .new { color: #4caf50; font-weight: bold; }
.sub-dot { margin-right: 5px; color: #666; }

.cost-section { 
  margin-top: 10px; 
  padding-top: 10px; 
  border-top: 1px solid #333; 
  display: flex; 
  flex-wrap: wrap; 
  gap: 8px; 
  justify-content: center; 
}

.resource-row { 
  display: flex; 
  align-items: center; 
  gap: 4px; 
  background: #222; 
  padding: 3px 8px; 
  border-radius: 15px; 
  border: 1px solid #444; 
  font-size: 0.8rem; 
}

.resource-row.not-enough { border-color: #f44336; color: #f44336; }
.mat-icon { width: 16px; height: 16px; }

.rate-row { width: 100%; text-align: center; margin-top: 5px; font-size: 0.8rem; color: #888; }
.high-rate { color: #4caf50; font-weight: bold; }

.actions-group { margin-top: 15px; }

.btn-forge, .btn-mythic-upgrade { 
  width: 100%; 
  padding: 10px; 
  font-family: "Cinzel", serif; 
  font-size: 1rem; 
  font-weight: bold; 
  cursor: pointer; 
  transition: 0.2s; 
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5); 
  position: relative; 
  overflow: hidden; 
}

.btn-forge { background: linear-gradient(180deg, #e65100, #bf360c); border: 1px solid #ff5722; color: white; }
.btn-forge:hover:not(:disabled) { transform: scale(1.02); box-shadow: 0 0 20px rgba(255, 87, 34, 0.5); }
.btn-forge:disabled { background: #444; border-color: #222; color: #888; cursor: not-allowed; }

.btn-mythic-upgrade { background: #000; border: 1px solid #d50000; color: #d50000; }
.btn-mythic-upgrade:hover { background: #d50000; color: #fff; box-shadow: 0 0 15px #d50000; }

@keyframes shake { 0% { transform: translate(1px, 1px) rotate(0deg); } 25% { transform: translate(-1px, -2px) rotate(-1deg); } 50% { transform: translate(-3px, 0px) rotate(1deg); } 75% { transform: translate(3px, 2px) rotate(0deg); } 100% { transform: translate(1px, -1px) rotate(-1deg); } }
@keyframes slide-right { 0% { transform: translateX(0); opacity: 0.5; } 50% { transform: translateX(5px); opacity: 1; } 100% { transform: translateX(0); opacity: 0.5; } }

.result-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 100; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(5px); }
.result-card { background: #1a1a1a; width: 400px; padding: 30px; border-radius: 12px; text-align: center; box-shadow: 0 0 50px rgba(0, 0, 0, 0.8); border: 1px solid #333; animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.result-card.success { border-color: #4caf50; box-shadow: 0 0 30px rgba(76, 175, 80, 0.3); }
.result-icon-circle { width: 80px; height: 80px; border-radius: 50%; background: #000; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; color: #4caf50; border: 2px solid #4caf50; }
.result-title { font-family: "Cinzel", serif; font-size: 1.8rem; margin-bottom: 10px; }
.btn-confirm { margin-top: 20px; padding: 10px 30px; background: transparent; border: 1px solid #fff; color: #fff; cursor: pointer; transition: 0.2s; }
.btn-confirm:hover { background: #fff; color: #000; }
@keyframes pop-in { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.text-common { color: #bdbdbd; } .text-uncommon { color: #81c784; } .text-rare { color: #42a5f5; } .text-epic { color: #ba68c8; } .text-legendary { color: #ffca28; } .text-mythic { color: #ff1744; text-shadow: 0 0 5px #ff1744; }
.border-mythic { border-color: #ff1744 !important; } .mythic-glow-anim { animation: pulse-red 2s infinite; }
@keyframes pulse-red { 0% { box-shadow: 0 0 10px #ff1744; } 50% { box-shadow: 0 0 25px #ff1744; } 100% { box-shadow: 0 0 10px #ff1744; } }
.error-text { color: #ff5252; margin-top: 10px; font-size: 0.9rem; }
.custom-scroll::-webkit-scrollbar { width: 6px; } .custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; } .custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); }
</style> -->

<!-- <template>
  <div class="page-container forge-page ancient-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

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

        <div class="anvil-panel wood-panel custom-scroll">
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
                  :src="resolveItemImage(selectedItem.item.image || selectedItem.item.imageUrl)"
                />
                <div class="glow-ring"></div>
              </div>
              <div class="empty-anvil" v-else>
                <i class="fas fa-hammer"></i>
                <p>Chọn trang bị</p>
              </div>
            </div>

            <transition name="fade">
              <div v-if="selectedItem" class="upgrade-info">
                <h3 class="item-title-large" :class="getRarityTextClass(selectedItem)">
                  {{ selectedItem.item.name }}
                  <span class="rank-badge">{{ getRarityLabel(selectedItem.item.rarity) }}</span>
                </h3>

                <div class="level-compare">
                  <div class="lv-box curr">
                    <span class="label">Hiện tại</span>
                    <span class="val">
                      {{ selectedItem.isMythic ? "M" + selectedItem.mythicLevel : "+" + selectedItem.enhanceLevel }}
                    </span>
                  </div>
                  <div class="arrow-anim"><i class="fas fa-angle-double-right"></i></div>
                  <div class="lv-box next">
                    <span class="label">Sau khi rèn</span>
                    <span class="val highlight">
                      {{ selectedItem.isMythic ? "M" + (selectedItem.mythicLevel + 1) : "+" + (selectedItem.enhanceLevel + 1) }}
                    </span>
                  </div>
                </div>

                <div class="stats-preview custom-scroll">
                  <div class="stat-row main-stat">
                    <span>{{ getStatLabel(selectedItem.mainStatType) }}</span>
                    <div class="stat-change">
                      <span class="old">{{ formatNumber(selectedItem.mainStatValue) }}</span>
                      <i class="fas fa-arrow-right"></i>
                      <span class="new">{{ formatNumber(getPredictedMainStat(selectedItem)) }}</span>
                    </div>
                  </div>
                  <div v-for="(sub, idx) in parsedSubStats" :key="idx" class="stat-row sub-stat">
                    <span class="sub-dot">•</span>
                    <span>{{ getStatLabel(sub.code) }}</span>
                    <span class="val">+{{ formatNumber(sub.value) }}{{ sub.isPercent ? "%" : "" }}</span>
                  </div>
                </div>

                <div class="cost-section">
                    <div class="resource-row" :class="{ 'not-enough': !canAffordGold }">
                    <i class="fas fa-coins text-gold"></i>
                    <span>{{ formatNumber(upgradeCost.gold) }} Vàng</span>
                  </div>
                  
                  <div class="resource-row" v-for="(mat, idx) in upgradeCost.materials" :key="idx">
                    <img :src="resolveItemImage(mat.img)" class="mat-icon" @error="handleImgError" />
                    <span>{{ mat.qty }} {{ mat.name }}</span>
                  </div>

                  <div class="rate-row">
                    Tỉ lệ: <span class="high-rate">100%</span>
                  </div>
                </div>

                <div class="actions-group">
                  <button
                    v-if="!selectedItem.isMythic && selectedItem.enhanceLevel < 30"
                    class="btn-forge"
                    @click="handleUpgrade"
                    :disabled="isForging"
                  >
                    <span v-if="!isForging">CƯỜNG HÓA</span>
                    <span v-else><i class="fas fa-spinner fa-spin"></i> ĐANG RÈN...</span>
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
                    v-if="!selectedItem.isMythic && selectedItem.enhanceLevel >= 30"
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
        <div class="result-card success" @click.stop>
          <div class="result-header">
            <div class="result-icon-circle"><i class="fas fa-check"></i></div>
          </div>
          <div class="result-body">
            <h2 class="result-title">THÀNH CÔNG!</h2>
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
import { useMarketStore } from "../stores/marketStore";
import { resolveItemImage } from "@/utils/assetHelper";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const characterStore = useCharacterStore();
const marketStore = useMarketStore();

// --- BACKGROUND LOGIC (MỚI) ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  // Chỉ đổi màu nền theo giờ (18h - 6h là tối), không hiện text
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC RÈN (GIỮ NGUYÊN) ---
const selectedItem = ref(null);
const isForging = ref(false);
const showResult = ref(false);
const resultMessage = ref("");
const errorMessage = ref("");
const filterType = ref("ALL");

const sortedEquipableItems = computed(() => {
  const items = inventoryStore.items || [];
  if (!Array.isArray(items)) return [];

  let filtered = items.filter(
    (i) => i.item && ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"].includes(i.item.type)
  );

  if (filterType.value === "WEAPON") filtered = filtered.filter((i) => i.item.type === "WEAPON");
  else if (filterType.value === "ARMOR") filtered = filtered.filter((i) => ["ARMOR", "HELMET", "BOOTS"].includes(i.item.type));
  else if (filterType.value === "ACCESSORY") filtered = filtered.filter((i) => ["RING", "NECKLACE"].includes(i.item.type));

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

const upgradeCost = computed(() => {
  if (!selectedItem.value) return { gold: 0, materials: [] };
  
  const item = selectedItem.value;
  const type = item.item.type;
  
  if (item.isMythic) {
      const nextLv = item.mythicLevel + 1;
      return {
          gold: nextLv * 100000,
          materials: [
             { name: "Nguyên liệu lạ", qty: nextLv * 50, img: "r_mystrile_node.png" }, 
             { name: "Echo Coin", qty: nextLv, img: "r_echo_coin.png" } 
          ]
      };
  }

  const nextLv = item.enhanceLevel + 1;
  let gold = 0;
  let mats = [];

  if (nextLv <= 10) {
      gold = nextLv * 1000;
      const mainQty = nextLv * 15;
      const subQty = nextLv * 5;
      
      if (type === 'WEAPON') mats.push({ name: "Quặng Đồng", qty: mainQty, img: "r_copper_node.png" });
      else mats.push({ name: "Đá", qty: mainQty, img: "stone_1.png" });
      
      mats.push({ name: "Gỗ Sồi", qty: subQty, img: "r_wood.png" });
  } 
  else if (nextLv <= 20) {
      gold = nextLv * 3000;
      const scale = nextLv - 10;
      mats.push({ name: "Sắt", qty: scale * 15, img: "r_silver_bar.png" });
      mats.push({ name: "Gỗ Khô", qty: scale * 5, img: "r_red_wood.png" });
  } 
  else { 
      gold = nextLv * 10000;
      const scale = nextLv - 20;
      mats.push({ name: "Bạch Kim", qty: scale * 20, img: "r_mystrile_bar.png" });
      mats.push({ name: "Gỗ Lạnh", qty: scale * 10, img: "r_white_wood.png" });
  }

  return { gold, materials: mats };
});

const canAffordGold = computed(() => {
  const userGold = authStore.wallet?.gold || 0;
  return userGold >= upgradeCost.value.gold;
});

const selectItem = (item) => {
  if (isForging.value) return;
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";

  try {
    const url = `/equipment/enhance/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
        inventoryStore.fetchInventory(),
        characterStore.fetchCharacter(),
        authStore.fetchProfile(),
        marketStore.refresh()
    ]);

    const updatedItem = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    selectedItem.value = updatedItem || null;

    showResultModal(`Cường hóa lên +${selectedItem.value.enhanceLevel} thành công!`);
  } catch (err) {
    console.error(err);
    errorMessage.value = err.response?.data?.message || err.response?.data || "Thiếu nguyên liệu hoặc lỗi hệ thống.";
  } finally {
    isForging.value = false;
  }
};

const handleMythicUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";
  try {
      const url = `/equipment/upgrade-mythic/${selectedItem.value.userItemId}`;
      await axiosClient.post(url);

      await Promise.all([
        inventoryStore.fetchInventory(),
        characterStore.fetchCharacter(),
        authStore.fetchProfile()
      ]);

      const updatedItem = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
      selectedItem.value = updatedItem || null;

      showResultModal("Thăng cấp Thần thoại thành công!");
  } catch (err) {
      console.error(err);
      errorMessage.value = err.response?.data?.message || err.response?.data || "Thất bại.";
  } finally {
      isForging.value = false;
  }
};

const showResultModal = (msg) => {
  resultMessage.value = msg;
  showResult.value = true;
};

const closeResult = () => {
  showResult.value = false;
  errorMessage.value = "";
};

const formatNumber = (n) => Number(n || 0).toLocaleString();
const getPredictedMainStat = (item) => {
  if (!item) return 0;
  const base = item.originalMainStatValue || item.mainStatValue;
  const currentLv = item.isMythic ? item.mythicLevel : item.enhanceLevel;
  return Math.floor(base * (1 + (currentLv + 1) * 0.1));
};
const getStatLabel = (code) => {
  const map = { ATK_FLAT: "Công", ATK_PERCENT: "Công %", HP_FLAT: "Máu", CRIT_RATE: "Chí Mạng", CRIT_DMG: "ST.Chí Mạng", SPEED: "Tốc Độ", DEF_FLAT: "Thủ", DEF_PERCENT: "Thủ %" };
  return map[code] || code;
};
const getRarityClass = (item) => item.isMythic ? "mythic" : (item.item.rarity ? item.item.rarity.toLowerCase() : "common");
const getRarityTextClass = (item) => item.isMythic ? "text-mythic" : (item.item.rarity ? `text-${item.item.rarity.toLowerCase()}` : "text-common");
const getRarityLabel = (rarity) => {
  const map = { COMMON: "Thường", UNCOMMON: "Phàm", RARE: "Hiếm", EPIC: "Sử Thi", LEGENDARY: "Huyền Thoại", MYTHIC: "Thần Thoại" };
  return map[rarity] || rarity;
};
const getFilterLabel = (t) => { const map = { ALL: "Tất cả", WEAPON: "Vũ khí", ARMOR: "Giáp trụ", ACCESSORY: "Trang sức" }; return map[t] || t; };
const handleImgError = (e) => { e.target.src = resolveItemImage("default_item.png"); };

onMounted(async () => {
  updateDayNight(); // [MỚI] Kích hoạt màu nền
  if (authStore.token) {
    await authStore.fetchProfile();
    await fetchInventory();
    await characterStore.fetchCharacter();
  }
});
const fetchInventory = async () => { await inventoryStore.fetchInventory(); };
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;700&display=swap");

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
.forge-page { 
  background-color: transparent; /* Nền trong suốt để lộ bg-layer */
  height: 100vh; 
  position: relative; 
  overflow: hidden; 
  font-family: "Noto Serif TC", serif; 
  color: #dcdcdc; 
}

.forge-wrapper { position: relative; z-index: 10; max-width: 1200px; margin: 0 auto; padding: 20px; height: 100vh; display: flex; flex-direction: column; }
.forge-header { display: flex; align-items: center; justify-content: center; gap: 20px; margin-bottom: 20px; padding-top: 20px; }
.title-ancient { font-family: "Cinzel", serif; font-size: 2.5rem; font-weight: 900; background: linear-gradient(180deg, #ffd700, #b8860b); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0 4px 10px rgba(0, 0, 0, 0.8); margin: 0; }
.header-ornament { height: 2px; width: 100px; background: linear-gradient(90deg, transparent, #b8860b, transparent); }

.forge-layout { display: flex; gap: 20px; flex: 1; min-height: 0; }

/* Panels (Thêm độ trong suốt) */
.wood-panel { 
  background: rgba(20, 15, 10, 0.85); /* Trong suốt hơn */
  border: 1px solid #5d4037; 
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.8), inset 0 0 50px rgba(0, 0, 0, 0.5); 
  border-radius: 8px; display: flex; flex-direction: column; 
  backdrop-filter: blur(5px); 
}
.inventory-panel { width: 380px; }
.panel-title { background: linear-gradient(90deg, #3e2723, #5d4037, #3e2723); padding: 12px; text-align: center; font-weight: bold; color: #ffcc80; border-bottom: 2px solid #8d6e63; text-transform: uppercase; letter-spacing: 1px; }

/* Filter Tabs */
.filter-tabs { display: flex; justify-content: space-around; background: rgba(0, 0, 0, 0.3); padding: 5px; }
.filter-tab { font-size: 0.8rem; cursor: pointer; padding: 5px 10px; border-radius: 4px; color: #888; transition: 0.2s; }
.filter-tab:hover, .filter-tab.active { color: #ffd700; background: rgba(255, 255, 255, 0.1); }

/* Item Grid */
.item-grid { flex: 1; padding: 15px; display: grid; grid-template-columns: repeat(auto-fill, minmax(64px, 1fr)); gap: 10px; align-content: start; overflow-y: auto; }
.item-slot { aspect-ratio: 1; border: 2px solid #444; background: rgba(0, 0, 0, 0.4); border-radius: 6px; cursor: pointer; position: relative; transition: all 0.2s; }
.item-slot:hover { transform: translateY(-2px); border-color: #888; }
.item-slot.selected { border-color: #ffd700; box-shadow: 0 0 15px rgba(255, 215, 0, 0.4); }
.slot-img-box { width: 100%; height: 100%; padding: 5px; display: flex; align-items: center; justify-content: center; }
.item-img-display { max-width: 100%; max-height: 100%; object-fit: contain; }
.level-badge { position: absolute; bottom: 2px; right: 2px; background: rgba(0, 0, 0, 0.8); color: #fff; font-size: 0.7rem; padding: 1px 4px; border-radius: 4px; border: 1px solid #555; }
.empty-msg { grid-column: 1 / -1; text-align: center; color: #666; margin-top: 50px; }

/* Anvil Section */
.anvil-panel { flex: 1; position: relative; overflow-y: auto; display: flex; flex-direction: column; }
.anvil-zone { min-height: 100%; height: auto; display: flex; flex-direction: column; align-items: center; justify-content: flex-start; padding: 20px 10px; }

.fire-particles { position: absolute; bottom: 0; left: 0; width: 100%; height: 100%; pointer-events: none; background: radial-gradient(circle at bottom, rgba(255, 87, 34, 0.1) 0%, transparent 60%); z-index: 0; }

.main-slot-container { width: 100px; height: 100px; border: 3px double #8d6e63; border-radius: 12px; background: radial-gradient(circle, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0.9) 100%); display: flex; align-items: center; justify-content: center; margin-bottom: 15px; position: relative; transition: 0.3s; flex-shrink: 0; z-index: 1; }
.main-slot-container.shaking { animation: shake 0.2s infinite; border-color: #ff5722; box-shadow: 0 0 20px #ff5722; }
.main-slot img { width: 70px; height: 70px; object-fit: contain; filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5)); }
.empty-anvil { color: #5d4037; text-align: center; font-size: 0.8rem; }
.empty-anvil i { font-size: 2rem !important; margin-bottom: 5px; display: block; }

/* Upgrade Info */
.upgrade-info { width: 100%; max-width: 380px; background: rgba(0, 0, 0, 0.6); border: 1px solid #5d4037; padding: 15px; border-radius: 8px; z-index: 1; }
.item-title-large { text-align: center; font-size: 1.1rem; margin-bottom: 10px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.rank-badge { font-size: 0.6rem; background: #333; padding: 1px 5px; border-radius: 3px; color: #fff; border: 1px solid #555; }

.level-compare { display: flex; justify-content: center; align-items: center; gap: 15px; margin-bottom: 10px; background: rgba(255, 255, 255, 0.03); padding: 8px; border-radius: 6px; }
.lv-box { display: flex; flex-direction: column; align-items: center; }
.lv-box .label { font-size: 0.65rem; color: #888; text-transform: uppercase; }
.lv-box .val { font-size: 1.4rem; font-weight: bold; color: #bbb; }
.lv-box .val.highlight { color: #4caf50; text-shadow: 0 0 5px rgba(76, 175, 80, 0.5); }
.arrow-anim { color: #666; font-size: 0.8rem; animation: slide-right 1s infinite; }

.stats-preview { max-height: 120px; overflow-y: auto; margin-bottom: 10px; }
.stat-row { display: flex; justify-content: space-between; padding: 4px 0; border-bottom: 1px dashed #333; font-size: 0.85rem; }
.stat-row.main-stat { font-size: 0.95rem; color: #ffd700; border-bottom: 1px solid #5d4037; margin-bottom: 5px; }
.stat-change { display: flex; gap: 5px; align-items: center; }
.stat-change .new { color: #4caf50; font-weight: bold; }
.sub-dot { margin-right: 5px; color: #666; }

.cost-section { margin-top: 10px; padding-top: 10px; border-top: 1px solid #333; display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.resource-row { display: flex; align-items: center; gap: 4px; background: #222; padding: 3px 8px; border-radius: 15px; border: 1px solid #444; font-size: 0.8rem; }
.resource-row.not-enough { border-color: #f44336; color: #f44336; }
.mat-icon { width: 16px; height: 16px; }
.rate-row { width: 100%; text-align: center; margin-top: 5px; font-size: 0.8rem; color: #888; }
.high-rate { color: #4caf50; font-weight: bold; }

.actions-group { margin-top: 15px; }
.btn-forge, .btn-mythic-upgrade { width: 100%; padding: 10px; font-family: "Cinzel", serif; font-size: 1rem; font-weight: bold; cursor: pointer; transition: 0.2s; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5); position: relative; overflow: hidden; }
.btn-forge { background: linear-gradient(180deg, #e65100, #bf360c); border: 1px solid #ff5722; color: white; }
.btn-forge:hover:not(:disabled) { transform: scale(1.02); box-shadow: 0 0 20px rgba(255, 87, 34, 0.5); }
.btn-forge:disabled { background: #444; border-color: #222; color: #888; cursor: not-allowed; }
.btn-mythic-upgrade { background: #000; border: 1px solid #d50000; color: #d50000; }
.btn-mythic-upgrade:hover { background: #d50000; color: #fff; box-shadow: 0 0 15px #d50000; }

@keyframes shake { 0% { transform: translate(1px, 1px) rotate(0deg); } 25% { transform: translate(-1px, -2px) rotate(-1deg); } 50% { transform: translate(-3px, 0px) rotate(1deg); } 75% { transform: translate(3px, 2px) rotate(0deg); } 100% { transform: translate(1px, -1px) rotate(-1deg); } }
@keyframes slide-right { 0% { transform: translateX(0); opacity: 0.5; } 50% { transform: translateX(5px); opacity: 1; } 100% { transform: translateX(0); opacity: 0.5; } }

.result-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 100; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(5px); }
.result-card { background: #1a1a1a; width: 400px; padding: 30px; border-radius: 12px; text-align: center; box-shadow: 0 0 50px rgba(0, 0, 0, 0.8); border: 1px solid #333; animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.result-card.success { border-color: #4caf50; box-shadow: 0 0 30px rgba(76, 175, 80, 0.3); }
.result-icon-circle { width: 80px; height: 80px; border-radius: 50%; background: #000; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; color: #4caf50; border: 2px solid #4caf50; }
.result-title { font-family: "Cinzel", serif; font-size: 1.8rem; margin-bottom: 10px; }
.btn-confirm { margin-top: 20px; padding: 10px 30px; background: transparent; border: 1px solid #fff; color: #fff; cursor: pointer; transition: 0.2s; }
.btn-confirm:hover { background: #fff; color: #000; }
@keyframes pop-in { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.text-common { color: #bdbdbd; } .text-uncommon { color: #81c784; } .text-rare { color: #42a5f5; } .text-epic { color: #ba68c8; } .text-legendary { color: #ffca28; } .text-mythic { color: #ff1744; text-shadow: 0 0 5px #ff1744; }
.border-mythic { border-color: #ff1744 !important; } .mythic-glow-anim { animation: pulse-red 2s infinite; }
@keyframes pulse-red { 0% { box-shadow: 0 0 10px #ff1744; } 50% { box-shadow: 0 0 25px #ff1744; } 100% { box-shadow: 0 0 10px #ff1744; } }
.error-text { color: #ff5252; margin-top: 10px; font-size: 0.9rem; }
.custom-scroll::-webkit-scrollbar { width: 6px; } .custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; } .custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); }
</style> -->

<!-- <template>
  <div class="page-container forge-page ancient-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

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
                  M{{ item.mythicStars || 0 }}
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

        <div class="anvil-panel wood-panel custom-scroll">
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
                  :src="resolveItemImage(selectedItem.item.image || selectedItem.item.imageUrl)"
                />
                <div class="glow-ring"></div>
              </div>
              <div class="empty-anvil" v-else>
                <i class="fas fa-hammer"></i>
                <p>Chọn trang bị</p>
              </div>
            </div>

            <transition name="fade">
              <div v-if="selectedItem" class="upgrade-info">
                <h3 class="item-title-large" :class="getRarityTextClass(selectedItem)">
                  {{ selectedItem.item.name }}
                  <span class="rank-badge">{{ getRarityLabel(selectedItem.item.rarity) }}</span>
                </h3>

                <div class="level-compare">
                  <div class="lv-box curr">
                    <span class="label">Hiện tại</span>
                    <span class="val">
                      {{ selectedItem.isMythic ? "M" + (selectedItem.mythicStars || 0) : "+" + selectedItem.enhanceLevel }}
                    </span>
                  </div>
                  <div class="arrow-anim"><i class="fas fa-angle-double-right"></i></div>
                  <div class="lv-box next">
                    <span class="label">Sau khi rèn</span>
                    <span class="val highlight">
                      {{ selectedItem.isMythic ? "M" + ((selectedItem.mythicStars || 0) + 1) : "+" + (selectedItem.enhanceLevel + 1) }}
                    </span>
                  </div>
                </div>

                <div class="stats-preview custom-scroll">
                  <div class="stat-row main-stat">
                    <span>{{ getStatLabel(selectedItem.mainStatType) }}</span>
                    <div class="stat-change">
                      <span class="old">{{ formatNumber(selectedItem.mainStatValue) }}</span>
                      <i class="fas fa-arrow-right"></i>
                      <span class="new">{{ formatNumber(getPredictedMainStat(selectedItem)) }}</span>
                    </div>
                  </div>
                  <div v-for="(sub, idx) in parsedSubStats" :key="idx" class="stat-row sub-stat">
                    <span class="sub-dot">•</span>
                    <span>{{ getStatLabel(sub.code) }}</span>
                    <span class="val">+{{ formatNumber(sub.value) }}{{ sub.isPercent ? "%" : "" }}</span>
                  </div>
                </div>

                <div class="cost-section">
                    <div class="resource-row" :class="{ 'not-enough': !canAffordGold }">
                    <i class="fas fa-coins text-gold"></i>
                    <span>{{ formatNumber(upgradeCost.gold) }} Vàng</span>
                  </div>
                  
                  <div class="resource-row" v-for="(mat, idx) in upgradeCost.materials" :key="idx">
                    <img :src="resolveItemImage(mat.img)" class="mat-icon" @error="handleImgError" />
                    <span>{{ mat.qty }} {{ mat.name }}</span>
                  </div>

                  <div class="rate-row">
                    Tỉ lệ: <span class="high-rate">{{ getSuccessRate(selectedItem) }}%</span>
                  </div>
                </div>

                <div class="actions-group">
                  <button
                    v-if="!selectedItem.isMythic && selectedItem.enhanceLevel < 30"
                    class="btn-forge"
                    @click="handleUpgrade"
                    :disabled="isForging"
                  >
                    <span v-if="!isForging">CƯỜNG HÓA</span>
                    <span v-else><i class="fas fa-spinner fa-spin"></i> ĐANG RÈN...</span>
                  </button>

                  <button
                    v-if="selectedItem.isMythic"
                    class="btn-mythic-upgrade"
                    @click="handleMythicUpgrade"
                    :disabled="isForging || (selectedItem.mythicStars || 0) >= 10"
                  >
                    <span v-if="!isForging">THĂNG CẤP THẦN</span>
                    <span v-else>ĐANG HẤP THỤ...</span>
                  </button>

                  <button
                    v-if="!selectedItem.isMythic && selectedItem.enhanceLevel >= 30"
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
        <div class="result-card success" @click.stop>
          <div class="result-header">
            <div class="result-icon-circle"><i class="fas fa-check"></i></div>
          </div>
          <div class="result-body">
            <h2 class="result-title">THÀNH CÔNG!</h2>
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
import { useMarketStore } from "../stores/marketStore";
import { resolveItemImage } from "@/utils/assetHelper";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const characterStore = useCharacterStore();
const marketStore = useMarketStore();

// --- BACKGROUND LOGIC ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC RÈN ---
const selectedItem = ref(null);
const isForging = ref(false);
const showResult = ref(false);
const resultMessage = ref("");
const errorMessage = ref("");
const filterType = ref("ALL");

const sortedEquipableItems = computed(() => {
  const items = inventoryStore.items || [];
  if (!Array.isArray(items)) return [];

  let filtered = items.filter(
    (i) => i.item && ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"].includes(i.item.type)
  );

  if (filterType.value === "WEAPON") filtered = filtered.filter((i) => i.item.type === "WEAPON");
  else if (filterType.value === "ARMOR") filtered = filtered.filter((i) => ["ARMOR", "HELMET", "BOOTS"].includes(i.item.type));
  else if (filterType.value === "ACCESSORY") filtered = filtered.filter((i) => ["RING", "NECKLACE"].includes(i.item.type));

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

// [LOGIC ĐỒNG BỘ BACKEND] Tính toán chi phí
const upgradeCost = computed(() => {
  if (!selectedItem.value) return { gold: 0, materials: [] };
  
  const item = selectedItem.value;
  const type = item.item.type;
  
  // [FIX] Logic Mythic Upgrade: Khớp với EquipmentService.enhanceMythicStars
  if (item.isMythic) {
      const nextStar = (item.mythicStars || 0) + 1;
      
      // Bảng giá từ Backend
      const costs = {
          1: { gold: 1000000, coin: 1 },
          2: { gold: 2000000, coin: 2 },
          3: { gold: 3500000, coin: 3 },
          4: { gold: 5000000, coin: 5 },
          5: { gold: 7500000, coin: 7 },
          6: { gold: 10000000, coin: 10 },
          7: { gold: 15000000, coin: 15 },
          8: { gold: 20000000, coin: 20 },
          9: { gold: 30000000, coin: 30 },
          10: { gold: 50000000, coin: 50 }
      };

      const cost = costs[nextStar] || { gold: 0, coin: 0 };

      return {
          gold: cost.gold,
          materials: [
             { name: "Echo Coin", qty: cost.coin, img: "r_echo_coin.png" } 
          ]
      };
  }

  // [FIX] Logic Normal Enhance: Khớp với EquipmentService.enhanceItem
  const nextLv = item.enhanceLevel + 1;
  const gold = nextLv * 2000; // Backend: Level * 2000
  let mats = [];

  if (nextLv <= 10) {
      const mainQty = 5;
      const subQty = 5;
      
      // Backend dùng ID 5 (Coal/Stone) và ID 1 (Oak)
      mats.push({ name: "Than Đá", qty: mainQty, img: "stone_1.png" }); 
      mats.push({ name: "Gỗ Sồi", qty: subQty, img: "r_wood.png" });
  } 
  else if (nextLv <= 20) {
      const mainQty = 10;
      const subQty = 10;
      
      // Backend dùng ID 7 (Iron) và ID 2 (Dried Wood)
      mats.push({ name: "Quặng Sắt", qty: mainQty, img: "r_silver_bar.png" });
      mats.push({ name: "Gỗ Khô", qty: subQty, img: "r_red_wood.png" });
  } 
  else { // 21 - 30
      const mainQty = 15;
      const subQty = 15;
      
      // Backend dùng ID 8 (Platinum) và ID 3 (Cold Wood)
      mats.push({ name: "Bạch Kim", qty: mainQty, img: "r_mystrile_bar.png" });
      mats.push({ name: "Gỗ Lạnh", qty: subQty, img: "r_white_wood.png" });
  }

  return { gold, materials: mats };
});

const canAffordGold = computed(() => {
  const userGold = authStore.wallet?.gold || 0;
  return userGold >= upgradeCost.value.gold;
});

const getSuccessRate = (item) => {
    if (!item.isMythic) return 100;
    const nextStar = (item.mythicStars || 0) + 1;
    const rates = { 1: 100, 2: 80, 3: 60, 4: 50, 5: 40, 6: 30, 7: 20, 8: 15, 9: 10, 10: 5 };
    return rates[nextStar] || 0;
};

const selectItem = (item) => {
  if (isForging.value) return;
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";

  try {
    const url = `/equipment/enhance/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
        inventoryStore.fetchInventory(),
        characterStore.fetchCharacter(),
        authStore.fetchProfile(),
        marketStore.refresh()
    ]);

    // Cập nhật lại item đang chọn
    const updatedItem = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    selectedItem.value = updatedItem || null;

    showResultModal(`Cường hóa lên +${selectedItem.value.enhanceLevel} thành công!`);
  } catch (err) {
    console.error(err);
    errorMessage.value = err.response?.data?.message || err.response?.data || "Thiếu nguyên liệu hoặc lỗi hệ thống.";
  } finally {
    isForging.value = false;
  }
};

const handleMythicUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";
  try {
      // [FIX] Đúng endpoint trong Controller: enhance-mythic-star
      const url = `/equipment/enhance-mythic-star/${selectedItem.value.userItemId}`;
      await axiosClient.post(url);

      await Promise.all([
        inventoryStore.fetchInventory(),
        characterStore.fetchCharacter(),
        authStore.fetchProfile()
      ]);

      const updatedItem = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
      selectedItem.value = updatedItem || null;

      showResultModal("Thăng cấp Thần thoại thành công!");
  } catch (err) {
      console.error(err);
      errorMessage.value = err.response?.data?.message || err.response?.data || "Thất bại.";
  } finally {
      isForging.value = false;
  }
};

const showResultModal = (msg) => {
  resultMessage.value = msg;
  showResult.value = true;
};

const closeResult = () => {
  showResult.value = false;
  errorMessage.value = "";
};

const formatNumber = (n) => Number(n || 0).toLocaleString();
const getPredictedMainStat = (item) => {
  if (!item) return 0;
  const base = item.originalMainStatValue || item.mainStatValue;
  // Mythic: Tăng 10% mỗi sao (Backend: boost = current * 0.1)
  if (item.isMythic) {
      return Math.floor(item.mainStatValue * 1.1);
  }
  // Normal: Tăng 10% mỗi cấp (Backend: base * (1 + lv * 0.1))
  const currentLv = item.enhanceLevel;
  return Math.floor(base * (1 + (currentLv + 1) * 0.1));
};
const getStatLabel = (code) => {
  const map = { ATK_FLAT: "Công", ATK_PERCENT: "Công %", HP_FLAT: "Máu", CRIT_RATE: "Chí Mạng", CRIT_DMG: "ST.Chí Mạng", SPEED: "Tốc Độ", DEF_FLAT: "Thủ", DEF_PERCENT: "Thủ %" };
  return map[code] || code;
};
const getRarityClass = (item) => item.isMythic ? "mythic" : (item.item.rarity ? item.item.rarity.toLowerCase() : "common");
const getRarityTextClass = (item) => item.isMythic ? "text-mythic" : (item.item.rarity ? `text-${item.item.rarity.toLowerCase()}` : "text-common");
const getRarityLabel = (rarity) => {
  const map = { COMMON: "Thường", UNCOMMON: "Phàm", RARE: "Hiếm", EPIC: "Sử Thi", LEGENDARY: "Huyền Thoại", MYTHIC: "Thần Thoại" };
  return map[rarity] || rarity;
};
const getFilterLabel = (t) => { const map = { ALL: "Tất cả", WEAPON: "Vũ khí", ARMOR: "Giáp trụ", ACCESSORY: "Trang sức" }; return map[t] || t; };
const handleImgError = (e) => { e.target.src = resolveItemImage("default_item.png"); };

onMounted(async () => {
  updateDayNight(); 
  if (authStore.token) {
    await authStore.fetchProfile();
    await fetchInventory();
    await characterStore.fetchCharacter();
  }
});
const fetchInventory = async () => { await inventoryStore.fetchInventory(); };
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;700&display=swap");

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

/* --- 2. LAYOUT CHUNG --- */
.forge-page { 
  background-color: transparent; 
  height: 100vh; 
  position: relative; 
  overflow: hidden; 
  font-family: "Noto Serif TC", serif; 
  color: #dcdcdc; 
}

.forge-wrapper { position: relative; z-index: 10; max-width: 1200px; margin: 0 auto; padding: 20px; height: 100vh; display: flex; flex-direction: column; }
.forge-header { display: flex; align-items: center; justify-content: center; gap: 20px; margin-bottom: 20px; padding-top: 20px; }
.title-ancient { font-family: "Cinzel", serif; font-size: 2.5rem; font-weight: 900; background: linear-gradient(180deg, #ffd700, #b8860b); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0 4px 10px rgba(0, 0, 0, 0.8); margin: 0; }
.header-ornament { height: 2px; width: 100px; background: linear-gradient(90deg, transparent, #b8860b, transparent); }

.forge-layout { display: flex; gap: 20px; flex: 1; min-height: 0; }

/* Panels */
.wood-panel { 
  background: rgba(20, 15, 10, 0.85); 
  border: 1px solid #5d4037; 
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.8), inset 0 0 50px rgba(0, 0, 0, 0.5); 
  border-radius: 8px; display: flex; flex-direction: column; 
  backdrop-filter: blur(5px); 
}
.inventory-panel { width: 380px; }
.panel-title { background: linear-gradient(90deg, #3e2723, #5d4037, #3e2723); padding: 12px; text-align: center; font-weight: bold; color: #ffcc80; border-bottom: 2px solid #8d6e63; text-transform: uppercase; letter-spacing: 1px; }

/* Filter Tabs */
.filter-tabs { display: flex; justify-content: space-around; background: rgba(0, 0, 0, 0.3); padding: 5px; }
.filter-tab { font-size: 0.8rem; cursor: pointer; padding: 5px 10px; border-radius: 4px; color: #888; transition: 0.2s; }
.filter-tab:hover, .filter-tab.active { color: #ffd700; background: rgba(255, 255, 255, 0.1); }

/* Item Grid */
.item-grid { flex: 1; padding: 15px; display: grid; grid-template-columns: repeat(auto-fill, minmax(64px, 1fr)); gap: 10px; align-content: start; overflow-y: auto; }
.item-slot { aspect-ratio: 1; border: 2px solid #444; background: rgba(0, 0, 0, 0.4); border-radius: 6px; cursor: pointer; position: relative; transition: all 0.2s; }
.item-slot:hover { transform: translateY(-2px); border-color: #888; }
.item-slot.selected { border-color: #ffd700; box-shadow: 0 0 15px rgba(255, 215, 0, 0.4); }
.slot-img-box { width: 100%; height: 100%; padding: 5px; display: flex; align-items: center; justify-content: center; }
.item-img-display { max-width: 100%; max-height: 100%; object-fit: contain; }
.level-badge { position: absolute; bottom: 2px; right: 2px; background: rgba(0, 0, 0, 0.8); color: #fff; font-size: 0.7rem; padding: 1px 4px; border-radius: 4px; border: 1px solid #555; }
.empty-msg { grid-column: 1 / -1; text-align: center; color: #666; margin-top: 50px; }

/* Anvil Section */
.anvil-panel { flex: 1; position: relative; overflow-y: auto; display: flex; flex-direction: column; }
.anvil-zone { min-height: 100%; height: auto; display: flex; flex-direction: column; align-items: center; justify-content: flex-start; padding: 20px 10px; }

.fire-particles { position: absolute; bottom: 0; left: 0; width: 100%; height: 100%; pointer-events: none; background: radial-gradient(circle at bottom, rgba(255, 87, 34, 0.1) 0%, transparent 60%); z-index: 0; }

.main-slot-container { width: 100px; height: 100px; border: 3px double #8d6e63; border-radius: 12px; background: radial-gradient(circle, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0.9) 100%); display: flex; align-items: center; justify-content: center; margin-bottom: 15px; position: relative; transition: 0.3s; flex-shrink: 0; z-index: 1; }
.main-slot-container.shaking { animation: shake 0.2s infinite; border-color: #ff5722; box-shadow: 0 0 20px #ff5722; }
.main-slot img { width: 70px; height: 70px; object-fit: contain; filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5)); }
.empty-anvil { color: #5d4037; text-align: center; font-size: 0.8rem; }
.empty-anvil i { font-size: 2rem !important; margin-bottom: 5px; display: block; }

/* Upgrade Info */
.upgrade-info { width: 100%; max-width: 380px; background: rgba(0, 0, 0, 0.6); border: 1px solid #5d4037; padding: 15px; border-radius: 8px; z-index: 1; }
.item-title-large { text-align: center; font-size: 1.1rem; margin-bottom: 10px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.rank-badge { font-size: 0.6rem; background: #333; padding: 1px 5px; border-radius: 3px; color: #fff; border: 1px solid #555; }

.level-compare { display: flex; justify-content: center; align-items: center; gap: 15px; margin-bottom: 10px; background: rgba(255, 255, 255, 0.03); padding: 8px; border-radius: 6px; }
.lv-box { display: flex; flex-direction: column; align-items: center; }
.lv-box .label { font-size: 0.65rem; color: #888; text-transform: uppercase; }
.lv-box .val { font-size: 1.4rem; font-weight: bold; color: #bbb; }
.lv-box .val.highlight { color: #4caf50; text-shadow: 0 0 5px rgba(76, 175, 80, 0.5); }
.arrow-anim { color: #666; font-size: 0.8rem; animation: slide-right 1s infinite; }

.stats-preview { max-height: 120px; overflow-y: auto; margin-bottom: 10px; }
.stat-row { display: flex; justify-content: space-between; padding: 4px 0; border-bottom: 1px dashed #333; font-size: 0.85rem; }
.stat-row.main-stat { font-size: 0.95rem; color: #ffd700; border-bottom: 1px solid #5d4037; margin-bottom: 5px; }
.stat-change { display: flex; gap: 5px; align-items: center; }
.stat-change .new { color: #4caf50; font-weight: bold; }
.sub-dot { margin-right: 5px; color: #666; }

.cost-section { margin-top: 10px; padding-top: 10px; border-top: 1px solid #333; display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.resource-row { display: flex; align-items: center; gap: 4px; background: #222; padding: 3px 8px; border-radius: 15px; border: 1px solid #444; font-size: 0.8rem; }
.resource-row.not-enough { border-color: #f44336; color: #f44336; }
.mat-icon { width: 16px; height: 16px; }
.rate-row { width: 100%; text-align: center; margin-top: 5px; font-size: 0.8rem; color: #888; }
.high-rate { color: #4caf50; font-weight: bold; }

.actions-group { margin-top: 15px; }
.btn-forge, .btn-mythic-upgrade { width: 100%; padding: 10px; font-family: "Cinzel", serif; font-size: 1rem; font-weight: bold; cursor: pointer; transition: 0.2s; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5); position: relative; overflow: hidden; }
.btn-forge { background: linear-gradient(180deg, #e65100, #bf360c); border: 1px solid #ff5722; color: white; }
.btn-forge:hover:not(:disabled) { transform: scale(1.02); box-shadow: 0 0 20px rgba(255, 87, 34, 0.5); }
.btn-forge:disabled { background: #444; border-color: #222; color: #888; cursor: not-allowed; }
.btn-mythic-upgrade { background: #000; border: 1px solid #d50000; color: #d50000; }
.btn-mythic-upgrade:hover { background: #d50000; color: #fff; box-shadow: 0 0 15px #d50000; }

@keyframes shake { 0% { transform: translate(1px, 1px) rotate(0deg); } 25% { transform: translate(-1px, -2px) rotate(-1deg); } 50% { transform: translate(-3px, 0px) rotate(1deg); } 75% { transform: translate(3px, 2px) rotate(0deg); } 100% { transform: translate(1px, -1px) rotate(-1deg); } }
@keyframes slide-right { 0% { transform: translateX(0); opacity: 0.5; } 50% { transform: translateX(5px); opacity: 1; } 100% { transform: translateX(0); opacity: 0.5; } }

.result-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 100; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(5px); }
.result-card { background: #1a1a1a; width: 400px; padding: 30px; border-radius: 12px; text-align: center; box-shadow: 0 0 50px rgba(0, 0, 0, 0.8); border: 1px solid #333; animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.result-card.success { border-color: #4caf50; box-shadow: 0 0 30px rgba(76, 175, 80, 0.3); }
.result-icon-circle { width: 80px; height: 80px; border-radius: 50%; background: #000; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; color: #4caf50; border: 2px solid #4caf50; }
.result-title { font-family: "Cinzel", serif; font-size: 1.8rem; margin-bottom: 10px; }
.btn-confirm { margin-top: 20px; padding: 10px 30px; background: transparent; border: 1px solid #fff; color: #fff; cursor: pointer; transition: 0.2s; }
.btn-confirm:hover { background: #fff; color: #000; }
@keyframes pop-in { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.text-common { color: #bdbdbd; } .text-uncommon { color: #81c784; } .text-rare { color: #42a5f5; } .text-epic { color: #ba68c8; } .text-legendary { color: #ffca28; } .text-mythic { color: #ff1744; text-shadow: 0 0 5px #ff1744; }
.border-mythic { border-color: #ff1744 !important; } .mythic-glow-anim { animation: pulse-red 2s infinite; }
@keyframes pulse-red { 0% { box-shadow: 0 0 10px #ff1744; } 50% { box-shadow: 0 0 25px #ff1744; } 100% { box-shadow: 0 0 10px #ff1744; } }
.error-text { color: #ff5252; margin-top: 10px; font-size: 0.9rem; }
.custom-scroll::-webkit-scrollbar { width: 6px; } .custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; } .custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); }
</style> -->

<!-- <template>
  <div class="page-container forge-page ancient-theme">
    <div class="bg-layer">
      <div
        class="mountain-bg"
        :style="{ backgroundImage: `url(${bgImage})` }"
      ></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

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
                  M{{ item.mythicStars || 0 }}
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

        <div class="anvil-panel wood-panel custom-scroll">
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
                <i class="fas fa-hammer"></i>
                <p>Chọn trang bị</p>
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
                          ? "M" + (selectedItem.mythicStars || 0)
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
                          ? "M" + ((selectedItem.mythicStars || 0) + 1)
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

                  <div
                    class="resource-row"
                    v-for="(mat, idx) in upgradeCost.materials"
                    :key="idx"
                  >
                    <img
                      :src="resolveItemImage(mat.img)"
                      class="mat-icon"
                      @error="handleImgError"
                    />
                    <span>{{ mat.qty }} {{ mat.name }}</span>
                  </div>

                  <div class="rate-row">
                    Tỉ lệ:
                    <span class="high-rate"
                      >{{ getSuccessRate(selectedItem) }}%</span
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
                    v-if="selectedItem.isMythic"
                    class="btn-mythic-upgrade"
                    @click="handleMythicUpgrade"
                    :disabled="
                      isForging || (selectedItem.mythicStars || 0) >= 10
                    "
                  >
                    <span v-if="!isForging">THĂNG CẤP THẦN</span>
                    <span v-else>ĐANG HẤP THỤ...</span>
                  </button>

                  <button
                    v-if="
                      !selectedItem.isMythic && selectedItem.enhanceLevel >= 30
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
        <div class="result-card success" @click.stop>
          <div class="result-header">
            <div class="result-icon-circle"><i class="fas fa-check"></i></div>
          </div>
          <div class="result-body">
            <h2 class="result-title">THÀNH CÔNG!</h2>
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
import { useMarketStore } from "../stores/marketStore";
import { resolveItemImage } from "@/utils/assetHelper";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const characterStore = useCharacterStore();
const marketStore = useMarketStore();

// --- BACKGROUND LOGIC ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC RÈN ---
const selectedItem = ref(null);
const isForging = ref(false);
const showResult = ref(false);
const resultMessage = ref("");
const errorMessage = ref("");
const filterType = ref("ALL");

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

// [LOGIC ĐỒNG BỘ BACKEND] Tính toán chi phí
const upgradeCost = computed(() => {
  if (!selectedItem.value) return { gold: 0, materials: [] };

  const item = selectedItem.value;

  // Logic Mythic Upgrade
  if (item.isMythic) {
    const nextStar = (item.mythicStars || 0) + 1;

    const costs = {
      1: { gold: 1000000, coin: 1 },
      2: { gold: 2000000, coin: 2 },
      3: { gold: 3500000, coin: 3 },
      4: { gold: 5000000, coin: 5 },
      5: { gold: 7500000, coin: 7 },
      6: { gold: 10000000, coin: 10 },
      7: { gold: 15000000, coin: 15 },
      8: { gold: 20000000, coin: 20 },
      9: { gold: 30000000, coin: 30 },
      10: { gold: 50000000, coin: 50 },
    };

    const cost = costs[nextStar] || { gold: 0, coin: 0 };

    return {
      gold: cost.gold,
      materials: [
        { name: "Echo Coin", qty: cost.coin, img: "r_echo_coin.png" },
      ],
    };
  }

  // [FIX] Logic Normal Enhance
  const nextLv = item.enhanceLevel + 1;
  const gold = nextLv * 1000;
  let mats = [];

  if (nextLv <= 10) {
    const mainQty = nextLv * 15;
    const subQty = nextLv * 5;

    // Vũ khí dùng Đồng (o_copper.png), Giáp dùng Đá (o_coal.png - dùng icon than đá cho đá)
    if (item.item.type === "WEAPON") {
      mats.push({ name: "Quặng Đồng", qty: mainQty, img: "o_copper.png" });
    } else {
      mats.push({ name: "Than Đá", qty: mainQty, img: "o_coal.png" });
    }
    // [FIX] Ảnh Gỗ Sồi: w_wood.png (thay vì r_wood.png)
    mats.push({ name: "Gỗ Sồi", qty: subQty, img: "w_wood.png" });
  } else if (nextLv <= 20) {
    const scale = nextLv - 10;
    const mainQty = scale * 15;
    const subQty = scale * 5;

    // [FIX] Ảnh Quặng Sắt: o_iron.png | Gỗ Khô: w_wood-red.png
    mats.push({ name: "Quặng Sắt", qty: mainQty, img: "o_iron.png" });
    mats.push({ name: "Gỗ Khô", qty: subQty, img: "w_wood-red.png" });
  } else {
    // 21 - 30
    const scale = nextLv - 20;
    const mainQty = scale * 20;
    const subQty = scale * 10;

    // [FIX] Ảnh Bạch Kim: o_platinum.png | Gỗ Lạnh: w_wood-white.png
    mats.push({ name: "Bạch Kim", qty: mainQty, img: "o_platinum.png" });
    mats.push({ name: "Gỗ Lạnh", qty: subQty, img: "w_wood-white.png" });
  }

  return { gold, materials: mats };
});

const canAffordGold = computed(() => {
  const userGold = authStore.wallet?.gold || 0;
  return userGold >= upgradeCost.value.gold;
});

const getSuccessRate = (item) => {
  if (!item.isMythic) return 100;
  const nextStar = (item.mythicStars || 0) + 1;
  const rates = {
    1: 100,
    2: 80,
    3: 60,
    4: 50,
    5: 40,
    6: 30,
    7: 20,
    8: 15,
    9: 10,
    10: 5,
  };
  return rates[nextStar] || 0;
};

const selectItem = (item) => {
  if (isForging.value) return;
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";

  try {
    const url = `/equipment/enhance/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
      inventoryStore.fetchInventory(),
      characterStore.fetchCharacter(),
      authStore.fetchProfile(),
      marketStore.refresh(),
    ]);

    // Cập nhật lại item đang chọn
    const updatedItem = inventoryStore.items.find(
      (i) => i.userItemId === selectedItem.value.userItemId,
    );
    selectedItem.value = updatedItem || null;

    showResultModal(
      `Cường hóa lên +${selectedItem.value.enhanceLevel} thành công!`,
    );
  } catch (err) {
    console.error(err);
    errorMessage.value =
      err.response?.data?.message ||
      err.response?.data ||
      "Thiếu nguyên liệu hoặc lỗi hệ thống.";
  } finally {
    isForging.value = false;
  }
};

const handleMythicUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  isForging.value = true;
  errorMessage.value = "";
  try {
    const url = `/equipment/enhance-mythic-star/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
      inventoryStore.fetchInventory(),
      characterStore.fetchCharacter(),
      authStore.fetchProfile(),
    ]);

    const updatedItem = inventoryStore.items.find(
      (i) => i.userItemId === selectedItem.value.userItemId,
    );
    selectedItem.value = updatedItem || null;

    showResultModal("Thăng cấp Thần thoại thành công!");
  } catch (err) {
    console.error(err);
    errorMessage.value =
      err.response?.data?.message || err.response?.data || "Thất bại.";
  } finally {
    isForging.value = false;
  }
};

const showResultModal = (msg) => {
  resultMessage.value = msg;
  showResult.value = true;
};

const closeResult = () => {
  showResult.value = false;
  errorMessage.value = "";
};

const formatNumber = (n) => Number(n || 0).toLocaleString();
const getPredictedMainStat = (item) => {
  if (!item) return 0;
  const base = item.originalMainStatValue || item.mainStatValue;
  // Mythic: Tăng 10% mỗi sao (Backend: boost = current * 0.1)
  if (item.isMythic) {
    return Math.floor(item.mainStatValue * 1.1);
  }
  // Normal: Tăng 10% mỗi cấp (Backend: base * (1 + lv * 0.1))
  const currentLv = item.enhanceLevel;
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
const getRarityClass = (item) =>
  item.isMythic
    ? "mythic"
    : item.item.rarity
      ? item.item.rarity.toLowerCase()
      : "common";
const getRarityTextClass = (item) =>
  item.isMythic
    ? "text-mythic"
    : item.item.rarity
      ? `text-${item.item.rarity.toLowerCase()}`
      : "text-common";
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
  updateDayNight();
  if (authStore.token) {
    await authStore.fetchProfile();
    await fetchInventory();
    await characterStore.fetchCharacter();
  }
});
const fetchInventory = async () => {
  await inventoryStore.fetchInventory();
};
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;700&display=swap");

/* --- 1. HỆ THỐNG BACKGROUND --- */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: #261815;
}
.mountain-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center bottom;
  opacity: 0.6;
  filter: sepia(10%) contrast(1.1);
}
.wood-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(62, 39, 35, 0.7),
    rgba(30, 20, 15, 0.9)
  );
  mix-blend-mode: multiply;
  transition: background 2s ease;
}
.wood-overlay.night-mode {
  background: linear-gradient(
    to bottom,
    rgba(10, 5, 20, 0.85),
    rgba(0, 0, 0, 0.95)
  );
}
.vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, transparent 60%, #1a100d 100%);
}

/* --- 2. LAYOUT CHUNG --- */
.forge-page {
  background-color: transparent;
  height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: "Noto Serif TC", serif;
  color: #dcdcdc;
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

/* Panels */
.wood-panel {
  background: rgba(20, 15, 10, 0.85);
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

/* Filter Tabs */
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

/* Item Grid */
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

/* Anvil Section */
.anvil-panel {
  flex: 1;
  position: relative;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.anvil-zone {
  min-height: 100%;
  height: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 20px 10px;
}

.fire-particles {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: radial-gradient(
    circle at bottom,
    rgba(255, 87, 34, 0.1) 0%,
    transparent 60%
  );
  z-index: 0;
}

.main-slot-container {
  width: 100px;
  height: 100px;
  border: 3px double #8d6e63;
  border-radius: 12px;
  background: radial-gradient(
    circle,
    rgba(0, 0, 0, 0.6) 0%,
    rgba(0, 0, 0, 0.9) 100%
  );
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  position: relative;
  transition: 0.3s;
  flex-shrink: 0;
  z-index: 1;
}
.main-slot-container.shaking {
  animation: shake 0.2s infinite;
  border-color: #ff5722;
  box-shadow: 0 0 20px #ff5722;
}
.main-slot img {
  width: 70px;
  height: 70px;
  object-fit: contain;
  filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5));
}
.empty-anvil {
  color: #5d4037;
  text-align: center;
  font-size: 0.8rem;
}
.empty-anvil i {
  font-size: 2rem !important;
  margin-bottom: 5px;
  display: block;
}

/* Upgrade Info */
.upgrade-info {
  width: 100%;
  max-width: 380px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #5d4037;
  padding: 15px;
  border-radius: 8px;
  z-index: 1;
}
.item-title-large {
  text-align: center;
  font-size: 1.1rem;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.rank-badge {
  font-size: 0.6rem;
  background: #333;
  padding: 1px 5px;
  border-radius: 3px;
  color: #fff;
  border: 1px solid #555;
}

.level-compare {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.03);
  padding: 8px;
  border-radius: 6px;
}
.lv-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.lv-box .label {
  font-size: 0.65rem;
  color: #888;
  text-transform: uppercase;
}
.lv-box .val {
  font-size: 1.4rem;
  font-weight: bold;
  color: #bbb;
}
.lv-box .val.highlight {
  color: #4caf50;
  text-shadow: 0 0 5px rgba(76, 175, 80, 0.5);
}
.arrow-anim {
  color: #666;
  font-size: 0.8rem;
  animation: slide-right 1s infinite;
}

.stats-preview {
  max-height: 120px;
  overflow-y: auto;
  margin-bottom: 10px;
}
.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px dashed #333;
  font-size: 0.85rem;
}
.stat-row.main-stat {
  font-size: 0.95rem;
  color: #ffd700;
  border-bottom: 1px solid #5d4037;
  margin-bottom: 5px;
}
.stat-change {
  display: flex;
  gap: 5px;
  align-items: center;
}
.stat-change .new {
  color: #4caf50;
  font-weight: bold;
}
.sub-dot {
  margin-right: 5px;
  color: #666;
}

.cost-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #333;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.resource-row {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #222;
  padding: 3px 8px;
  border-radius: 15px;
  border: 1px solid #444;
  font-size: 0.8rem;
}
.resource-row.not-enough {
  border-color: #f44336;
  color: #f44336;
}
.mat-icon {
  width: 16px;
  height: 16px;
}
.rate-row {
  width: 100%;
  text-align: center;
  margin-top: 5px;
  font-size: 0.8rem;
  color: #888;
}
.high-rate {
  color: #4caf50;
  font-weight: bold;
}

.actions-group {
  margin-top: 15px;
}
.btn-forge,
.btn-mythic-upgrade {
  width: 100%;
  padding: 10px;
  font-family: "Cinzel", serif;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  position: relative;
  overflow: hidden;
}
.btn-forge {
  background: linear-gradient(180deg, #e65100, #bf360c);
  border: 1px solid #ff5722;
  color: white;
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
.btn-mythic-upgrade {
  background: #000;
  border: 1px solid #d50000;
  color: #d50000;
}
.btn-mythic-upgrade:hover {
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
  color: #4caf50;
  border: 2px solid #4caf50;
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
</style> -->




<!-- <template>
  <div class="page-container forge-page ancient-theme">
    <div class="bg-layer">
      <div
        class="mountain-bg"
        :style="{ backgroundImage: `url(${bgImage})` }"
      ></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

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
                  M{{ item.mythicStars || 0 }}
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

        <div class="anvil-panel wood-panel custom-scroll">
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
                <i class="fas fa-hammer"></i>
                <p>Chọn trang bị</p>
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
                          ? "M" + (selectedItem.mythicStars || 0)
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
                          ? "M" + ((selectedItem.mythicStars || 0) + 1)
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

                  <div
                    class="resource-row"
                    v-for="(mat, idx) in upgradeCost.materials"
                    :key="idx"
                    :class="{ 'not-enough': getOwnedQty(mat.name) < mat.qty }"
                  >
                    <img
                      :src="resolveItemImage(mat.img)"
                      class="mat-icon"
                      @error="handleImgError"
                    />
                    <span>
                        {{ getOwnedQty(mat.name) }}/{{ mat.qty }} {{ mat.name }}
                    </span>
                  </div>

                  <div class="rate-row">
                    Tỉ lệ:
                    <span class="high-rate"
                      >{{ getSuccessRate(selectedItem) }}%</span
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
                    v-if="selectedItem.isMythic"
                    class="btn-mythic-upgrade"
                    @click="handleMythicUpgrade"
                    :disabled="
                      isForging || (selectedItem.mythicStars || 0) >= 10
                    "
                  >
                    <span v-if="!isForging">THĂNG CẤP THẦN</span>
                    <span v-else>ĐANG HẤP THỤ...</span>
                  </button>

                  <button
                    v-if="
                      !selectedItem.isMythic && selectedItem.enhanceLevel >= 30
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
        <div class="result-card success" @click.stop>
          <div class="result-header">
            <div class="result-icon-circle"><i class="fas fa-check"></i></div>
          </div>
          <div class="result-body">
            <h2 class="result-title">THÀNH CÔNG!</h2>
            <p class="result-desc">{{ resultMessage }}</p>
          </div>
          <button class="btn-confirm" @click="closeResult">XÁC NHẬN</button>
        </div>
      </div>
    </transition>
    
    <GameToast ref="toast" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import axiosClient from "../api/axiosClient";
import { useInventoryStore } from "../stores/inventoryStore";
import { useAuthStore } from "../stores/authStore";
import { useCharacterStore } from "../stores/characterStore";
import { useMarketStore } from "../stores/marketStore";
import { resolveItemImage } from "@/utils/assetHelper";
import GameToast from "@/components/GameToast.vue";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const characterStore = useCharacterStore();
const marketStore = useMarketStore();
const toast = ref(null);

// --- BACKGROUND LOGIC ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC RÈN ---
const selectedItem = ref(null);
const isForging = ref(false);
const showResult = ref(false);
const resultMessage = ref("");
const errorMessage = ref("");
const filterType = ref("ALL");

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

// [FIXED] Đồng bộ tên nguyên liệu với Database (seed_core.sql)
const upgradeCost = computed(() => {
  if (!selectedItem.value) return { gold: 0, materials: [] };

  const item = selectedItem.value;

  // Logic Mythic Upgrade
  if (item.isMythic) {
    const nextStar = (item.mythicStars || 0) + 1;

    const costs = {
      1: { gold: 1000000, coin: 1 },
      2: { gold: 2000000, coin: 2 },
      3: { gold: 3500000, coin: 3 },
      4: { gold: 5000000, coin: 5 },
      5: { gold: 7500000, coin: 7 },
      6: { gold: 10000000, coin: 10 },
      7: { gold: 15000000, coin: 15 },
      8: { gold: 20000000, coin: 20 },
      9: { gold: 30000000, coin: 30 },
      10: { gold: 50000000, coin: 50 },
    };

    const cost = costs[nextStar] || { gold: 0, coin: 0 };

    return {
      gold: cost.gold,
      materials: [
        { name: "Echo Coin", qty: cost.coin, img: "r_echo_coin.png" },
      ],
    };
  }

  // Logic Normal Enhance
  const nextLv = item.enhanceLevel + 1;
  const gold = nextLv * 1000;
  let mats = [];

  if (nextLv <= 10) {
    const mainQty = nextLv * 15;
    const subQty = nextLv * 5;

    // [FIX] Sửa tên "Than Đá" thành "Than"
    if (item.item.type === "WEAPON") {
      mats.push({ name: "Quặng Đồng", qty: mainQty, img: "o_copper.png" });
    } else {
      mats.push({ name: "Than", qty: mainQty, img: "o_coal.png" }); 
    }
    mats.push({ name: "Gỗ Sồi", qty: subQty, img: "w_wood.png" });
  } else if (nextLv <= 20) {
    const scale = nextLv - 10;
    const mainQty = scale * 15;
    const subQty = scale * 5;

    // [FIX] Sửa tên "Quặng Sắt" thành "Sắt"
    mats.push({ name: "Sắt", qty: mainQty, img: "o_iron.png" }); 
    mats.push({ name: "Gỗ Khô", qty: subQty, img: "w_wood-red.png" });
  } else {
    // 21 - 30
    const scale = nextLv - 20;
    const mainQty = scale * 20;
    const subQty = scale * 10;

    mats.push({ name: "Bạch Kim", qty: mainQty, img: "o_platinum.png" });
    mats.push({ name: "Gỗ Lạnh", qty: subQty, img: "w_wood-white.png" });
  }

  return { gold, materials: mats };
});

const canAffordGold = computed(() => {
  const userGold = authStore.wallet?.gold || 0;
  return userGold >= upgradeCost.value.gold;
});

// Hàm hỗ trợ lấy số lượng đang có
const getOwnedQty = (matName) => {
    const found = inventoryStore.items.find(i => i.item.name === matName);
    return found ? found.quantity : 0;
};

// Hàm kiểm tra nguyên liệu trước khi đập
const checkResources = () => {
    const cost = upgradeCost.value;

    // 1. Kiểm tra Vàng
    if (!canAffordGold.value) {
        const missing = cost.gold - (authStore.wallet?.gold || 0);
        toast.value?.show(`Thiếu ${formatNumber(missing)} Vàng!`, "error");
        return false;
    }

    // 2. Kiểm tra Nguyên liệu
    const missingMats = [];
    cost.materials.forEach(mat => {
        const owned = getOwnedQty(mat.name);
        if (owned < mat.qty) {
            missingMats.push(`${mat.name} (thiếu ${mat.qty - owned})`);
        }
    });

    if (missingMats.length > 0) {
        toast.value?.show(`Không đủ nguyên liệu: ${missingMats.join(', ')}`, "error");
        return false;
    }

    return true;
};

const getSuccessRate = (item) => {
  if (!item.isMythic) return 100;
  const nextStar = (item.mythicStars || 0) + 1;
  const rates = {
    1: 100,
    2: 80,
    3: 60,
    4: 50,
    5: 40,
    6: 30,
    7: 20,
    8: 15,
    9: 10,
    10: 5,
  };
  return rates[nextStar] || 0;
};

const selectItem = (item) => {
  if (isForging.value) return;
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;
  
  // KIỂM TRA NGUYÊN LIỆU TRƯỚC
  if (!checkResources()) return;

  isForging.value = true;
  errorMessage.value = "";

  try {
    const url = `/equipment/enhance/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
      inventoryStore.fetchInventory(),
      characterStore.fetchCharacter(),
      authStore.fetchProfile(),
      marketStore.refresh(),
    ]);

    // Cập nhật lại item đang chọn
    const updatedItem = inventoryStore.items.find(
      (i) => i.userItemId === selectedItem.value.userItemId,
    );
    selectedItem.value = updatedItem || null;

    showResultModal(
      `Cường hóa lên +${selectedItem.value.enhanceLevel} thành công!`,
    );
  } catch (err) {
    console.error(err);
    errorMessage.value =
      err.response?.data?.message ||
      err.response?.data ||
      "Thiếu nguyên liệu hoặc lỗi hệ thống.";
  } finally {
    isForging.value = false;
  }
};

const handleMythicUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;

  // KIỂM TRA NGUYÊN LIỆU TRƯỚC
  if (!checkResources()) return;

  isForging.value = true;
  errorMessage.value = "";
  try {
    const url = `/equipment/enhance-mythic-star/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
      inventoryStore.fetchInventory(),
      characterStore.fetchCharacter(),
      authStore.fetchProfile(),
    ]);

    const updatedItem = inventoryStore.items.find(
      (i) => i.userItemId === selectedItem.value.userItemId,
    );
    selectedItem.value = updatedItem || null;

    showResultModal("Thăng cấp Thần thoại thành công!");
  } catch (err) {
    console.error(err);
    errorMessage.value =
      err.response?.data?.message || err.response?.data || "Thất bại.";
  } finally {
    isForging.value = false;
  }
};

const showResultModal = (msg) => {
  resultMessage.value = msg;
  showResult.value = true;
};

const closeResult = () => {
  showResult.value = false;
  errorMessage.value = "";
};

const formatNumber = (n) => Number(n || 0).toLocaleString();
const getPredictedMainStat = (item) => {
  if (!item) return 0;
  const base = item.originalMainStatValue || item.mainStatValue;
  // Mythic: Tăng 10% mỗi sao (Backend: boost = current * 0.1)
  if (item.isMythic) {
    return Math.floor(item.mainStatValue * 1.1);
  }
  // Normal: Tăng 10% mỗi cấp (Backend: base * (1 + lv * 0.1))
  const currentLv = item.enhanceLevel;
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
const getRarityClass = (item) =>
  item.isMythic
    ? "mythic"
    : item.item.rarity
      ? item.item.rarity.toLowerCase()
      : "common";
const getRarityTextClass = (item) =>
  item.isMythic
    ? "text-mythic"
    : item.item.rarity
      ? `text-${item.item.rarity.toLowerCase()}`
      : "text-common";
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
  updateDayNight();
  if (authStore.token) {
    await authStore.fetchProfile();
    await fetchInventory();
    await characterStore.fetchCharacter();
  }
});
const fetchInventory = async () => {
  await inventoryStore.fetchInventory();
};
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;700&display=swap");

/* --- 1. HỆ THỐNG BACKGROUND --- */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: #261815;
}
.mountain-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center bottom;
  opacity: 0.6;
  filter: sepia(10%) contrast(1.1);
}
.wood-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(62, 39, 35, 0.7),
    rgba(30, 20, 15, 0.9)
  );
  mix-blend-mode: multiply;
  transition: background 2s ease;
}
.wood-overlay.night-mode {
  background: linear-gradient(
    to bottom,
    rgba(10, 5, 20, 0.85),
    rgba(0, 0, 0, 0.95)
  );
}
.vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, transparent 60%, #1a100d 100%);
}

/* --- 2. LAYOUT CHUNG --- */
.forge-page {
  background-color: transparent;
  height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: "Noto Serif TC", serif;
  color: #dcdcdc;
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

/* Panels */
.wood-panel {
  background: rgba(20, 15, 10, 0.85);
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

/* Filter Tabs */
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

/* Item Grid */
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

/* Anvil Section */
.anvil-panel {
  flex: 1;
  position: relative;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.anvil-zone {
  min-height: 100%;
  height: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 20px 10px;
}

.fire-particles {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: radial-gradient(
    circle at bottom,
    rgba(255, 87, 34, 0.1) 0%,
    transparent 60%
  );
  z-index: 0;
}

.main-slot-container {
  width: 100px;
  height: 100px;
  border: 3px double #8d6e63;
  border-radius: 12px;
  background: radial-gradient(
    circle,
    rgba(0, 0, 0, 0.6) 0%,
    rgba(0, 0, 0, 0.9) 100%
  );
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  position: relative;
  transition: 0.3s;
  flex-shrink: 0;
  z-index: 1;
}
.main-slot-container.shaking {
  animation: shake 0.2s infinite;
  border-color: #ff5722;
  box-shadow: 0 0 20px #ff5722;
}
.main-slot img {
  width: 70px;
  height: 70px;
  object-fit: contain;
  filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5));
}
.empty-anvil {
  color: #5d4037;
  text-align: center;
  font-size: 0.8rem;
}
.empty-anvil i {
  font-size: 2rem !important;
  margin-bottom: 5px;
  display: block;
}

/* Upgrade Info */
.upgrade-info {
  width: 100%;
  max-width: 380px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #5d4037;
  padding: 15px;
  border-radius: 8px;
  z-index: 1;
}
.item-title-large {
  text-align: center;
  font-size: 1.1rem;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.rank-badge {
  font-size: 0.6rem;
  background: #333;
  padding: 1px 5px;
  border-radius: 3px;
  color: #fff;
  border: 1px solid #555;
}

.level-compare {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.03);
  padding: 8px;
  border-radius: 6px;
}
.lv-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.lv-box .label {
  font-size: 0.65rem;
  color: #888;
  text-transform: uppercase;
}
.lv-box .val {
  font-size: 1.4rem;
  font-weight: bold;
  color: #bbb;
}
.lv-box .val.highlight {
  color: #4caf50;
  text-shadow: 0 0 5px rgba(76, 175, 80, 0.5);
}
.arrow-anim {
  color: #666;
  font-size: 0.8rem;
  animation: slide-right 1s infinite;
}

.stats-preview {
  max-height: 120px;
  overflow-y: auto;
  margin-bottom: 10px;
}
.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px dashed #333;
  font-size: 0.85rem;
}
.stat-row.main-stat {
  font-size: 0.95rem;
  color: #ffd700;
  border-bottom: 1px solid #5d4037;
  margin-bottom: 5px;
}
.stat-change {
  display: flex;
  gap: 5px;
  align-items: center;
}
.stat-change .new {
  color: #4caf50;
  font-weight: bold;
}
.sub-dot {
  margin-right: 5px;
  color: #666;
}

.cost-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #333;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.resource-row {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #222;
  padding: 3px 8px;
  border-radius: 15px;
  border: 1px solid #444;
  font-size: 0.8rem;
}
.resource-row.not-enough {
  border-color: #f44336;
  color: #f44336;
}
.mat-icon {
  width: 16px;
  height: 16px;
}
.rate-row {
  width: 100%;
  text-align: center;
  margin-top: 5px;
  font-size: 0.8rem;
  color: #888;
}
.high-rate {
  color: #4caf50;
  font-weight: bold;
}

.actions-group {
  margin-top: 15px;
}
.btn-forge,
.btn-mythic-upgrade {
  width: 100%;
  padding: 10px;
  font-family: "Cinzel", serif;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  position: relative;
  overflow: hidden;
}
.btn-forge {
  background: linear-gradient(180deg, #e65100, #bf360c);
  border: 1px solid #ff5722;
  color: white;
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
.btn-mythic-upgrade {
  background: #000;
  border: 1px solid #d50000;
  color: #d50000;
}
.btn-mythic-upgrade:hover {
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
  color: #4caf50;
  border: 2px solid #4caf50;
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
</style> -->


<template>
  <div class="page-container forge-page ancient-theme">
    <div class="bg-layer">
      <div
        class="mountain-bg"
        :style="{ backgroundImage: `url(${bgImage})` }"
      ></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

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
                  M{{ item.mythicStars || 0 }}
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

        <div class="anvil-panel wood-panel custom-scroll">
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
                <i class="fas fa-hammer"></i>
                <p>Chọn trang bị</p>
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
                          ? "M" + (selectedItem.mythicStars || 0)
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
                          ? "M" + ((selectedItem.mythicStars || 0) + 1)
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

                  <div
                    class="resource-row"
                    v-for="(mat, idx) in upgradeCost.materials"
                    :key="idx"
                    :class="{ 'not-enough': getOwnedQty(mat.name) < mat.qty }"
                  >
                    <img
                      :src="resolveItemImage(mat.img)"
                      class="mat-icon"
                      @error="handleImgError"
                    />
                    <span>
                      {{ getOwnedQty(mat.name) }}/{{ mat.qty }} {{ mat.name }}
                    </span>
                  </div>

                  <div class="rate-row">
                    Tỉ lệ:
                    <span class="high-rate"
                      >{{ getSuccessRate(selectedItem) }}%</span
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
                    v-if="selectedItem.isMythic"
                    class="btn-mythic-upgrade"
                    @click="handleMythicUpgrade"
                    :disabled="
                      isForging || (selectedItem.mythicStars || 0) >= 10
                    "
                  >
                    <span v-if="!isForging">THĂNG CẤP THẦN</span>
                    <span v-else>ĐANG HẤP THỤ...</span>
                  </button>

                  <button
                    v-if="
                      !selectedItem.isMythic && selectedItem.enhanceLevel >= 30
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
        <div class="result-card success" @click.stop>
          <div class="result-header">
            <div class="result-icon-circle"><i class="fas fa-check"></i></div>
          </div>
          <div class="result-body">
            <h2 class="result-title">THÀNH CÔNG!</h2>
            <p class="result-desc">{{ resultMessage }}</p>
          </div>
          <button class="btn-confirm" @click="closeResult">XÁC NHẬN</button>
        </div>
      </div>
    </transition>

    <GameToast ref="toast" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import axiosClient from "@/api/axiosClient";
import { useInventoryStore } from "@/stores/inventoryStore";
import { useAuthStore } from "@/stores/authStore";
import { useCharacterStore } from "@/stores/characterStore";
import { useMarketStore } from "@/stores/marketStore";
import { resolveItemImage } from "@/utils/assetHelper";
import GameToast from "@/components/GameToast.vue";

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const characterStore = useCharacterStore();
const marketStore = useMarketStore();
const toast = ref(null);

// --- BACKGROUND LOGIC ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC RÈN ---
const selectedItem = ref(null);
const isForging = ref(false);
const showResult = ref(false);
const resultMessage = ref("");
const errorMessage = ref("");
const filterType = ref("ALL");

// Filter danh sách trang bị
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

// Parse subStats từ JSON string nếu cần
const parsedSubStats = computed(() => {
  if (!selectedItem.value || !selectedItem.value.subStats) return [];
  try {
    const data = selectedItem.value.subStats;
    return typeof data === "string" ? JSON.parse(data) : data;
  } catch (e) {
    return [];
  }
});

// --- [CRITICAL FIX] ĐỒNG BỘ TÊN NGUYÊN LIỆU VỚI BACKEND ---
// Đã sửa "Than Đá" -> "Than", "Quặng Sắt" -> "Sắt" để khớp seed_core.sql
const upgradeCost = computed(() => {
  if (!selectedItem.value) return { gold: 0, materials: [] };

  const item = selectedItem.value;

  // 1. Logic Mythic Upgrade (Dùng Echo Coin)
  if (item.isMythic) {
    const nextStar = (item.mythicStars || 0) + 1;
    const costs = {
      1: { gold: 1000000, coin: 1 },
      2: { gold: 2000000, coin: 2 },
      3: { gold: 3500000, coin: 3 },
      4: { gold: 5000000, coin: 5 },
      5: { gold: 7500000, coin: 7 },
      6: { gold: 10000000, coin: 10 },
      7: { gold: 15000000, coin: 15 },
      8: { gold: 20000000, coin: 20 },
      9: { gold: 30000000, coin: 30 },
      10: { gold: 50000000, coin: 50 },
    };
    const cost = costs[nextStar] || { gold: 0, coin: 0 };
    return {
      gold: cost.gold,
      materials: [
        { name: "Echo Coin", qty: cost.coin, img: "r_echo_coin.png" },
      ],
    };
  }

  // 2. Logic Normal Enhance
  const nextLv = item.enhanceLevel + 1;
  const gold = nextLv * 1000;
  let mats = [];

  if (nextLv <= 10) {
    // Level 1-10: Đồng / Than + Gỗ Sồi
    const mainQty = nextLv * 15;
    const subQty = nextLv * 5;

    if (item.item.type === "WEAPON") {
      mats.push({ name: "Quặng Đồng", qty: mainQty, img: "o_copper.png" });
    } else {
      // Đã fix: Than Đá -> Than
      mats.push({ name: "Than", qty: mainQty, img: "o_coal.png" });
    }
    mats.push({ name: "Gỗ Sồi", qty: subQty, img: "w_wood.png" });

  } else if (nextLv <= 20) {
    // Level 11-20: Sắt + Gỗ Khô
    const scale = nextLv - 10;
    const mainQty = scale * 15;
    const subQty = scale * 5;

    // Đã fix: Quặng Sắt -> Sắt
    mats.push({ name: "Sắt", qty: mainQty, img: "o_iron.png" });
    mats.push({ name: "Gỗ Khô", qty: subQty, img: "w_wood-red.png" });

  } else {
    // Level 21-30: Bạch Kim + Gỗ Lạnh
    const scale = nextLv - 20;
    const mainQty = scale * 20;
    const subQty = scale * 10;

    mats.push({ name: "Bạch Kim", qty: mainQty, img: "o_platinum.png" });
    mats.push({ name: "Gỗ Lạnh", qty: subQty, img: "w_wood-white.png" });
  }

  return { gold, materials: mats };
});

const canAffordGold = computed(() => {
  const userGold = authStore.wallet?.gold || 0;
  return userGold >= upgradeCost.value.gold;
});

// Hàm lấy số lượng đang có trong kho
const getOwnedQty = (matName) => {
  if (!inventoryStore.items) return 0;
  // Tìm item có tên chính xác khớp với định nghĩa ở upgradeCost
  const found = inventoryStore.items.find((i) => i.item.name === matName);
  return found ? found.quantity : 0;
};

// Kiểm tra đủ nguyên liệu trước khi gọi API
const checkResources = () => {
  const cost = upgradeCost.value;

  // 1. Kiểm tra Vàng
  if (!canAffordGold.value) {
    const missing = cost.gold - (authStore.wallet?.gold || 0);
    toast.value?.show(`Thiếu ${formatNumber(missing)} Vàng!`, "error");
    return false;
  }

  // 2. Kiểm tra Nguyên liệu
  const missingMats = [];
  cost.materials.forEach((mat) => {
    const owned = getOwnedQty(mat.name);
    if (owned < mat.qty) {
      missingMats.push(`${mat.name} (thiếu ${mat.qty - owned})`);
    }
  });

  if (missingMats.length > 0) {
    toast.value?.show(
      `Không đủ nguyên liệu: ${missingMats.join(", ")}`,
      "error",
    );
    return false;
  }

  return true;
};

const getSuccessRate = (item) => {
  if (!item.isMythic) return 100;
  const nextStar = (item.mythicStars || 0) + 1;
  const rates = {
    1: 100, 2: 80, 3: 60, 4: 50, 5: 40,
    6: 30, 7: 20, 8: 15, 9: 10, 10: 5,
  };
  return rates[nextStar] || 0;
};

const selectItem = (item) => {
  if (isForging.value) return;
  selectedItem.value = item;
  errorMessage.value = "";
};

const handleUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;

  if (!checkResources()) return;

  isForging.value = true;
  errorMessage.value = "";

  try {
    const url = `/equipment/enhance/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    // Refresh dữ liệu sau khi đập thành công
    await Promise.all([
      inventoryStore.fetchInventory(),
      characterStore.fetchCharacter(),
      authStore.fetchProfile(),
      marketStore.refresh(),
    ]);

    // Update lại UI item đang chọn
    const updatedItem = inventoryStore.items.find(
      (i) => i.userItemId === selectedItem.value.userItemId,
    );
    selectedItem.value = updatedItem || null;

    showResultModal(
      `Cường hóa lên +${selectedItem.value.enhanceLevel} thành công!`,
    );
  } catch (err) {
    console.error(err);
    errorMessage.value =
      err.response?.data?.message ||
      err.response?.data ||
      "Lỗi hệ thống hoặc thiếu nguyên liệu (Server check).";
  } finally {
    isForging.value = false;
  }
};

const handleMythicUpgrade = async () => {
  if (!selectedItem.value || isForging.value) return;

  if (!checkResources()) return;

  isForging.value = true;
  errorMessage.value = "";
  try {
    const url = `/equipment/enhance-mythic-star/${selectedItem.value.userItemId}`;
    await axiosClient.post(url);

    await Promise.all([
      inventoryStore.fetchInventory(),
      characterStore.fetchCharacter(),
      authStore.fetchProfile(),
    ]);

    const updatedItem = inventoryStore.items.find(
      (i) => i.userItemId === selectedItem.value.userItemId,
    );
    selectedItem.value = updatedItem || null;

    showResultModal("Thăng cấp Thần thoại thành công!");
  } catch (err) {
    console.error(err);
    errorMessage.value =
      err.response?.data?.message || err.response?.data || "Thất bại.";
  } finally {
    isForging.value = false;
  }
};

const showResultModal = (msg) => {
  resultMessage.value = msg;
  showResult.value = true;
};

const closeResult = () => {
  showResult.value = false;
  errorMessage.value = "";
};

// --- Helpers ---
const formatNumber = (n) => Number(n || 0).toLocaleString();

const getPredictedMainStat = (item) => {
  if (!item) return 0;
  const base = item.originalMainStatValue || item.mainStatValue;
  // Mythic: +10% mỗi sao
  if (item.isMythic) {
    return Math.floor(item.mainStatValue * 1.1);
  }
  // Normal: base * (1 + lv * 0.1)
  const currentLv = item.enhanceLevel;
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

const getRarityClass = (item) =>
  item.isMythic
    ? "mythic"
    : item.item.rarity
      ? item.item.rarity.toLowerCase()
      : "common";

const getRarityTextClass = (item) =>
  item.isMythic
    ? "text-mythic"
    : item.item.rarity
      ? `text-${item.item.rarity.toLowerCase()}`
      : "text-common";

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

// --- Hooks ---
onMounted(async () => {
  updateDayNight();
  if (authStore.token) {
    await authStore.fetchProfile();
    await inventoryStore.fetchInventory();
    await characterStore.fetchCharacter();
  }
});
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;700&display=swap");

/* --- 1. HỆ THỐNG BACKGROUND --- */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: #261815;
}
.mountain-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center bottom;
  opacity: 0.6;
  filter: sepia(10%) contrast(1.1);
}
.wood-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(62, 39, 35, 0.7),
    rgba(30, 20, 15, 0.9)
  );
  mix-blend-mode: multiply;
  transition: background 2s ease;
}
.wood-overlay.night-mode {
  background: linear-gradient(
    to bottom,
    rgba(10, 5, 20, 0.85),
    rgba(0, 0, 0, 0.95)
  );
}
.vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, transparent 60%, #1a100d 100%);
}

/* --- 2. LAYOUT CHUNG --- */
.forge-page {
  background-color: transparent;
  height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: "Noto Serif TC", serif;
  color: #dcdcdc;
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

/* Panels */
.wood-panel {
  background: rgba(20, 15, 10, 0.85);
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

/* Filter Tabs */
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

/* Item Grid */
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

/* Anvil Section */
.anvil-panel {
  flex: 1;
  position: relative;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.anvil-zone {
  min-height: 100%;
  height: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 20px 10px;
}

.fire-particles {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: radial-gradient(
    circle at bottom,
    rgba(255, 87, 34, 0.1) 0%,
    transparent 60%
  );
  z-index: 0;
}

.main-slot-container {
  width: 100px;
  height: 100px;
  border: 3px double #8d6e63;
  border-radius: 12px;
  background: radial-gradient(
    circle,
    rgba(0, 0, 0, 0.6) 0%,
    rgba(0, 0, 0, 0.9) 100%
  );
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  position: relative;
  transition: 0.3s;
  flex-shrink: 0;
  z-index: 1;
}
.main-slot-container.shaking {
  animation: shake 0.2s infinite;
  border-color: #ff5722;
  box-shadow: 0 0 20px #ff5722;
}
.main-slot img {
  width: 70px;
  height: 70px;
  object-fit: contain;
  filter: drop-shadow(0 0 5px rgba(0, 0, 0, 0.5));
}
.empty-anvil {
  color: #5d4037;
  text-align: center;
  font-size: 0.8rem;
}
.empty-anvil i {
  font-size: 2rem !important;
  margin-bottom: 5px;
  display: block;
}

/* Upgrade Info */
.upgrade-info {
  width: 100%;
  max-width: 380px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #5d4037;
  padding: 15px;
  border-radius: 8px;
  z-index: 1;
}
.item-title-large {
  text-align: center;
  font-size: 1.1rem;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.rank-badge {
  font-size: 0.6rem;
  background: #333;
  padding: 1px 5px;
  border-radius: 3px;
  color: #fff;
  border: 1px solid #555;
}

.level-compare {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.03);
  padding: 8px;
  border-radius: 6px;
}
.lv-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.lv-box .label {
  font-size: 0.65rem;
  color: #888;
  text-transform: uppercase;
}
.lv-box .val {
  font-size: 1.4rem;
  font-weight: bold;
  color: #bbb;
}
.lv-box .val.highlight {
  color: #4caf50;
  text-shadow: 0 0 5px rgba(76, 175, 80, 0.5);
}
.arrow-anim {
  color: #666;
  font-size: 0.8rem;
  animation: slide-right 1s infinite;
}

.stats-preview {
  max-height: 120px;
  overflow-y: auto;
  margin-bottom: 10px;
}
.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px dashed #333;
  font-size: 0.85rem;
}
.stat-row.main-stat {
  font-size: 0.95rem;
  color: #ffd700;
  border-bottom: 1px solid #5d4037;
  margin-bottom: 5px;
}
.stat-change {
  display: flex;
  gap: 5px;
  align-items: center;
}
.stat-change .new {
  color: #4caf50;
  font-weight: bold;
}
.sub-dot {
  margin-right: 5px;
  color: #666;
}

.cost-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #333;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.resource-row {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #222;
  padding: 3px 8px;
  border-radius: 15px;
  border: 1px solid #444;
  font-size: 0.8rem;
}
.resource-row.not-enough {
  border-color: #f44336;
  color: #f44336;
}
.mat-icon {
  width: 16px;
  height: 16px;
}
.rate-row {
  width: 100%;
  text-align: center;
  margin-top: 5px;
  font-size: 0.8rem;
  color: #888;
}
.high-rate {
  color: #4caf50;
  font-weight: bold;
}

.actions-group {
  margin-top: 15px;
}
.btn-forge,
.btn-mythic-upgrade {
  width: 100%;
  padding: 10px;
  font-family: "Cinzel", serif;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  position: relative;
  overflow: hidden;
}
.btn-forge {
  background: linear-gradient(180deg, #e65100, #bf360c);
  border: 1px solid #ff5722;
  color: white;
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
.btn-mythic-upgrade {
  background: #000;
  border: 1px solid #d50000;
  color: #d50000;
}
.btn-mythic-upgrade:hover {
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
  color: #4caf50;
  border: 2px solid #4caf50;
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