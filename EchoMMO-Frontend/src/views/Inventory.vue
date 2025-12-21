<!-- <template>
  <div class="page-container inventory-page">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

    <div class="inventory-wrapper">
      <div class="main-chest-frame">
        <div class="chest-header">
          <div class="header-decoration left"></div>
          <h2 class="chest-title">HÀNH TRANG</h2>
          <div class="header-decoration right"></div>
        </div>

        <div class="tab-control-bar">
          <button class="tab-btn" :class="{ active: filter === 'ALL' }" @click="filter = 'ALL'">
            TẤT CẢ
          </button>
          <div class="separator">|</div>
          <button class="tab-btn" :class="{ active: filter === 'EQUIP' }" @click="filter = 'EQUIP'">
            TRANG BỊ
          </button>
          <div class="separator">|</div>
          <button class="tab-btn" :class="{ active: filter === 'MAT' }" @click="filter = 'MAT'">
            VẬT PHẨM
          </button>
          <div class="capacity-badge">
            <i class="fas fa-weight-hanging"></i> {{ filteredItems.length }} / 50
          </div>
        </div>

        <div class="inventory-grid custom-scroll">
          <div
            v-for="u in filteredItems"
            :key="u.userItemId"
            class="grid-slot"
            :class="[
              'rarity-' + (u.rarity || getSafeItem(u).rarity || 'COMMON'),
              {
                selected: selectedItem?.userItemId === u.userItemId,
                equipped: u.isEquipped,
                virtual: u.isVirtual,
              },
            ]"
            @click="selectedItem = u"
          >
            <div class="slot-inner">
              <img
                :src="getCorrectImage(getSafeItem(u))"
                class="item-icon"
                alt="item"
                @error="$event.target.src = getItemImage('o_coal.png')"
              />

              <div class="qty-tag" v-if="u.quantity > 1 || u.isVirtual">
                {{ u.quantity }}
              </div>

              <div class="equip-tag" v-if="u.isEquipped">
                <i class="fas fa-shield-alt"></i> E
              </div>

              <div
                v-if="u.enhanceLevel > 0"
                class="level-tag-mini"
                :class="getLevelClass(u.enhanceLevel)"
              >
                +{{ u.enhanceLevel }}
              </div>
            </div>

            <div
              class="selection-border"
              v-if="selectedItem?.userItemId === u.userItemId"
            ></div>
          </div>

          <div
            v-for="n in Math.max(0, 50 - filteredItems.length)"
            :key="'empty-' + n"
            class="grid-slot empty"
          ></div>
        </div>
      </div>

      <div class="right-panel-container">
        <transition name="fade-slide" mode="out-in">
          <div
            v-if="selectedItem"
            class="item-card-panel"
            :key="selectedItem.userItemId"
          >
            <div class="card-header-bg">
              <div class="card-icon-wrapper">
                <div
                  class="card-icon-frame"
                  :class="'rarity-' + (selectedItem.rarity || getSafeItem(selectedItem).rarity || 'COMMON')"
                >
                  <img
                    :src="getCorrectImage(getSafeItem(selectedItem))"
                    @error="$event.target.src = getItemImage('o_coal.png')"
                  />
                </div>

                <div v-if="selectedItem.enhanceLevel > 0" class="card-level-badge">
                  +{{ selectedItem.enhanceLevel }}
                </div>
                <div v-if="selectedItem.isMythic" class="card-mythic-badge">
                  M{{ selectedItem.mythicLevel }}
                </div>
              </div>

              <div class="card-title-block">
                <div class="card-rarity-label">
                  {{ translateRarity(selectedItem.rarity || getSafeItem(selectedItem).rarity) }}
                  {{ translateType(getSafeItem(selectedItem).type) }}
                </div>
                <h3
                  class="card-item-name"
                  :class="'text-rarity-' + (selectedItem.rarity || getSafeItem(selectedItem).rarity || 'COMMON')"
                >
                  {{ getSafeItem(selectedItem).name }}
                </h3>
              </div>
            </div>

            <div class="card-lore">
              "{{ getSafeItem(selectedItem).description || "Vật phẩm bí ẩn." }}"
            </div>
            <div class="separator-line"></div>

            <div class="card-main-stat">
              <div class="stat-icon"><i class="fas fa-khanda"></i></div>
              <div class="stat-info">
                <span class="stat-label">SỨC MẠNH CHIẾN ĐẤU</span>
                <span class="stat-value main-val">
                  {{ calculateCombatPower(selectedItem) }}
                </span>
              </div>
            </div>

            <div class="separator-line"></div>

            <div class="card-sub-stats">
              <div class="sub-stat-row">
                <span class="sub-label">Tấn Công</span>
                <span class="sub-val atk">+{{ calculateTotalStat(selectedItem, "ATK") }}</span>
              </div>
              <div class="sub-stat-row">
                <span class="sub-label">Phòng Thủ</span>
                <span class="sub-val def">+{{ calculateTotalStat(selectedItem, "DEF") }}</span>
              </div>
              <div class="sub-stat-row">
                <span class="sub-label">Sinh Lực</span>
                <span class="sub-val hp">+{{ calculateTotalStat(selectedItem, "HP") }}</span>
              </div>
              <div class="sub-stat-row">
                <span class="sub-label">Tốc Độ</span>
                <span class="sub-val speed">+{{ calculateTotalStat(selectedItem, "SPEED") }}</span>
              </div>

              <div v-if="selectedItem.isVirtual" class="sub-stat-row resource-type">
                <span class="sub-label">Loại</span>
                <span class="sub-val resource">Tài Nguyên</span>
              </div>
            </div>

            <div class="card-footer-info">
              <div class="eq-score">
                <span class="score-label">Điểm Trang Bị</span>
                <span class="score-val">
                  {{ calculateCombatPower(selectedItem) + (getSafeItem(selectedItem).tier * 5 || 0) }}
                </span>
              </div>
            </div>

            <div class="card-actions">
              <button
                v-if="getSafeItem(selectedItem).type === 'CONSUMABLE' && !selectedItem.isVirtual"
                class="card-btn use-btn"
                @click="inventoryStore.useItem(selectedItem.userItemId)"
              >
                DÙNG
              </button>

              <template v-if="canEquip(selectedItem)">
                <button
                  v-if="!selectedItem.isEquipped"
                  class="card-btn equip-btn"
                  @click="inventoryStore.equipItem(selectedItem.userItemId)"
                >
                  TRANG BỊ
                </button>
                <button
                  v-else
                  class="card-btn unequip-btn"
                  @click="inventoryStore.unequipItem(selectedItem.userItemId)"
                >
                  GỠ BỎ
                </button>
              </template>

              <button
                v-if="!selectedItem.isEquipped && !selectedItem.isVirtual"
                class="card-btn sell-btn"
                @click="openSellModal(selectedItem)"
              >
                BÁN
              </button>
            </div>
          </div>

          <div v-else class="item-card-placeholder" key="placeholder">
            <div class="placeholder-text">Chọn một vật phẩm để xem</div>
          </div>
        </transition>
      </div>
    </div>

    <div v-if="showSell" class="modal-overlay">
      <div class="dark-modal">
        <div class="modal-header">
          <span class="decor">❖</span> GIAO DỊCH <span class="decor">❖</span>
        </div>
        <div class="modal-body">
          <h3 class="trade-item-name text-gradient">
            {{ getSafeItem(sellItem).name }}
          </h3>
          <div class="trade-tabs">
            <div class="trade-tab" :class="{ active: mode === 'NPC' }" @click="mode = 'NPC'">
              TIỆM CẦM ĐỒ
            </div>
            <div class="trade-tab" :class="{ active: mode === 'P2P' }" @click="mode = 'P2P'">
              CHỢ TRỜI
            </div>
          </div>
          <div class="trade-form">
            <div class="form-group">
              <label>SỐ LƯỢNG</label>
              <input type="number" v-model.number="qty" min="1" :max="sellItem.quantity" class="dark-input" />
            </div>
            <div v-if="mode === 'P2P'" class="form-group">
              <label>ĐƠN GIÁ</label>
              <input type="number" v-model.number="price" class="dark-input" />
            </div>
            <div class="trade-summary">
              Tổng thu:
              <span class="money-highlight">
                {{ mode === "NPC" ? getSafeItem(sellItem).basePrice * 0.5 * qty : price * qty }}
                <i class="fas fa-coins"></i>
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="showSell = false">HỦY</button>
          <button class="modal-btn confirm" @click="mode === 'NPC' ? confirmNPC() : confirmP2P()">
            XÁC NHẬN
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useInventoryStore } from "../stores/inventoryStore";
import { useCharacterStore } from "../stores/characterStore";
import { useMarketStore } from "../stores/marketStore";
import { useAuthStore } from "../stores/authStore";
import { resolveItemImage } from "@/utils/assetHelper";

// --- BACKGROUND LOGIC ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC HÀNH TRANG ---
const inventoryStore = useInventoryStore();
const charStore = useCharacterStore();
const marketStore = useMarketStore();
const authStore = useAuthStore();

const getItemImage = resolveItemImage;

const filter = ref("ALL");
const selectedItem = ref(null);

const EQUIP_TYPES = ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"];
const MATERIAL_TYPES = ["MATERIAL", "CONSUMABLE"];

const getSafeItem = (u) => (u && u.item ? u.item : {});
const getLevelClass = (lv) =>
  lv >= 30 ? "lvl-red" : lv >= 15 ? "lvl-purple" : lv >= 10 ? "lvl-gold" : "lvl-white";

// --- [FIX] HÀM XỬ LÝ ẢNH CHUẨN GIT ---
const getCorrectImage = (item) => {
  if (!item) return getItemImage('o_coal.png');
  
  let imgName = item.image || item.imageUrl || 'o_coal.png';
  
  // Mapping tên từ Database (CamelCase) sang tên file thực tế trên Git (gạch nối)
  const mapping = {
    'w_woodRed.png': 'w_wood-red.png',
    'w_woodWhite.png': 'w_wood-white.png',
    'w_woodBlack.png': 'w_wood-black.png',
    
    // Đề phòng trường hợp database lưu tên khác một chút
    'w_woodred.png': 'w_wood-red.png',
    'w_woodwhite.png': 'w_wood-white.png',
    'w_woodblack.png': 'w_wood-black.png'
  };

  if (mapping[imgName]) {
    imgName = mapping[imgName];
  }

  return getItemImage(imgName);
};

// --- STATS ---
const getStatVal = (obj, ...keys) => {
  if (!obj) return 0;
  for (const key of keys) {
    if (obj[key] !== undefined && obj[key] !== null) return Number(obj[key]);
  }
  return 0;
};

const calculateTotalStat = (u, type) => {
  if (!u || !u.item) return 0;
  let total = 0;
  const itemBase = u.item;
  const subStats = u.subStats || [];
  const mainType = u.mainStatType || u.main_stat_type || "";
  const mainVal = Number(u.mainStatValue || u.main_stat_value || 0);

  if (type === "ATK") total += getStatVal(itemBase, "atkBonus", "atk_bonus");
  if (type === "DEF") total += getStatVal(itemBase, "defBonus", "def_bonus");
  if (type === "HP") total += getStatVal(itemBase, "hpBonus", "hp_bonus");
  if (type === "SPEED") total += getStatVal(itemBase, "speedBonus", "speed_bonus", "speed");

  if (mainType) {
    if (type === "ATK" && ["ATK_FLAT", "STR"].includes(mainType)) total += mainVal;
    if (type === "DEF" && mainType === "DEF_FLAT") total += mainVal;
    if (type === "HP" && mainType === "HP_FLAT") total += mainVal;
    if (type === "SPEED" && mainType === "SPEED") total += mainVal;
  }

  if (Array.isArray(subStats)) {
    subStats.forEach((stat) => {
      if (stat.is_percent) return;
      const code = stat.code;
      const val = Number(stat.value || 0);
      if (type === "ATK" && ["ATK_FLAT", "STR"].includes(code)) total += val;
      if (type === "DEF" && code === "DEF_FLAT") total += val;
      if (type === "HP" && code === "HP_FLAT") total += val;
      if (type === "SPEED" && ["SPEED", "DEX"].includes(code)) total += val;
    });
  }
  return Math.floor(total);
};

const calculateCombatPower = (u) => {
  const dbPower = getStatVal(u, "totalPower", "total_power");
  if (dbPower > 0) return dbPower;
  const atk = calculateTotalStat(u, "ATK");
  const def = calculateTotalStat(u, "DEF");
  const hp = calculateTotalStat(u, "HP");
  const speed = calculateTotalStat(u, "SPEED");
  const cp = atk + def + Math.floor(hp / 10) + speed * 2;
  return cp > 0 ? cp : getSafeItem(u).tier * 10 || 10;
};

// --- HELPER ITEM ẢO ---
const createVirtualItem = (id, name, imgCode, qty) => ({
  userItemId: id,
  quantity: qty,
  isEquipped: false,
  isVirtual: true,
  item: {
    name,
    type: "MATERIAL",
    rarity: "COMMON",
    imageUrl: imgCode, 
    description: "Tài nguyên khai thác được.",
    basePrice: 0,
  },
});

// --- FILTER & MAP ---
const filteredItems = computed(() => {
  let items = [];

  if (inventoryStore.items && Array.isArray(inventoryStore.items)) {
    items = [...inventoryStore.items];
  }

  if (authStore.wallet) {
    const w = authStore.wallet;
    const woodVal = w.wood || 0;
    const stoneVal = w.stone || 0;
    const ironVal = w.ironOre || w.iron_ore || 0;
    const platinumVal = w.platinum || 0;
    const fishVal = w.fish || 0;

    // [FIX] Cập nhật tên ảnh ảo cho đúng với Assets
    if (woodVal > 0) items.push(createVirtualItem("v_wood", "Gỗ Sồi", "w_wood.png", woodVal));
    if (stoneVal > 0) items.push(createVirtualItem("v_stone", "Đá", "o_coal.png", stoneVal)); 
    if (ironVal > 0) items.push(createVirtualItem("v_iron", "Quặng Sắt", "o_iron.png", ironVal));
    if (platinumVal > 0) items.push(createVirtualItem("v_plat", "Bạch Kim", "o_platinum.png", platinumVal));
    if (fishVal > 0) items.push(createVirtualItem("v_fish", "Cá", "f_fish.png", fishVal));
  }

  if (filter.value === "EQUIP") {
    items = items.filter((i) => i.item && EQUIP_TYPES.includes(i.item.type));
  }
  if (filter.value === "MAT") {
    items = items.filter((i) => i.item && MATERIAL_TYPES.includes(i.item.type));
  }

  return items;
});

// --- MODAL ---
const showSell = ref(false);
const sellItem = ref(null);
const mode = ref("NPC");
const qty = ref(1);
const price = ref(0);

const openSellModal = (item) => {
  if (item.isVirtual) return;
  sellItem.value = item;
  qty.value = 1;
  price.value = item.item.basePrice || 0;
  showSell.value = true;
};
const confirmNPC = async () => {
  await marketStore.sellItem(sellItem.value.userItemId, qty.value);
  showSell.value = false;
  selectedItem.value = null;
  await authStore.fetchProfile();
};
const confirmP2P = async () => {
  await marketStore.createListing(sellItem.value.userItemId, price.value, qty.value);
  showSell.value = false;
  selectedItem.value = null;
  await authStore.fetchProfile();
};

const canEquip = (u) => u.item && EQUIP_TYPES.includes(u.item.type);
const translateType = (t) =>
  ({
    WEAPON: "Binh Khí",
    ARMOR: "Y Phục",
    HELMET: "Mũ",
    BOOTS: "Giày",
    RING: "Nhẫn",
    NECKLACE: "Dây Chuyền",
  })[t] || t;
const translateRarity = (r) =>
  ({
    COMMON: "Thường",
    UNCOMMON: "Phổ Thông",
    RARE: "Hiếm",
    EPIC: "Sử Thi",
    LEGENDARY: "Huyền Thoại",
    MYTHIC: "Thần Thoại",
  })[r] || "Thường";

onMounted(async () => {
  updateDayNight(); 
  await Promise.all([
    inventoryStore.fetchInventory(),
    charStore.fetchCharacter(),
    authStore.fetchProfile(),
  ]);
});
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap");

.inventory-page {
  background-color: transparent;
  min-height: 100vh;
  color: #e0e0e0;
  position: relative;
  overflow: hidden;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
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

/* --- 2. LAYOUT HÀNH TRANG --- */
.inventory-wrapper {
  position: relative;
  z-index: 10;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30px;
  padding: 20px;
}

/* LEFT: GRID */
.main-chest-frame {
  width: 600px;
  height: 85vh;
  background: rgba(20, 20, 25, 0.95);
  border: 1px solid #444;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
  position: relative;
}
.chest-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #333;
  background: linear-gradient(to bottom, #1a1a1a, #222);
}
.chest-title {
  font-family: "Orbitron", sans-serif;
  color: var(--gold, #ffd700);
  font-size: 1.4rem;
  margin: 0 15px;
  text-transform: uppercase;
  letter-spacing: 2px;
}
.tab-control-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 15px;
  background: rgba(0, 0, 0, 0.4);
  font-size: 0.9rem;
}
.tab-btn {
  background: none;
  border: none;
  color: #777;
  font-weight: bold;
  cursor: pointer;
  padding: 5px;
  transition: 0.2s;
}
.tab-btn.active {
  color: #ffd700;
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}
.capacity-badge {
  margin-left: auto;
  color: #555;
}
.inventory-grid {
  flex: 1;
  padding: 15px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(65px, 1fr));
  grid-auto-rows: 65px;
  gap: 8px;
  overflow-y: auto;
  align-content: start;
  background: rgba(0, 0, 0, 0.2);
}
.grid-slot {
  position: relative;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  cursor: pointer;
  transition: all 0.2s;
}
.grid-slot:hover {
  background: rgba(255, 255, 255, 0.08);
}
.grid-slot.selected {
  border-color: #ffd700;
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.2);
}
.grid-slot.empty {
  border: 1px dashed #333;
  opacity: 0.2;
  pointer-events: none;
  background: none;
}
.slot-inner {
  width: 100%;
  height: 100%;
  padding: 4px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.item-icon {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.qty-tag {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  font-size: 9px;
  padding: 1px 3px;
  border-radius: 2px;
}
.equip-tag {
  position: absolute;
  top: 1px;
  left: 1px;
  color: #00ff9d;
  font-size: 9px;
  background: rgba(0, 0, 0, 0.6);
  padding: 1px 2px;
  border-radius: 2px;
}
.level-tag-mini {
  position: absolute;
  top: 1px;
  right: 1px;
  font-size: 9px;
  font-weight: 900;
  color: #ffd700;
  text-shadow: 1px 1px 0 #000;
}

/* RIGHT: CARD */
.item-card-panel,
.item-card-placeholder {
  width: 360px;
  height: 600px;
  background: #1e1b1b;
  border: 2px solid #5a4a42;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 5px 5px 20px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}
.item-card-placeholder {
  border: 2px dashed #444;
  background: transparent;
  justify-content: center;
  align-items: center;
  color: #555;
  font-style: italic;
  background: rgba(30,27,27,0.8); 
}
.card-header-bg {
  background: linear-gradient(135deg, #2b1d1d 0%, #1a1a1a 100%);
  padding: 20px;
  display: flex;
  gap: 15px;
  border-bottom: 2px solid #5a4a42;
  position: relative;
}
.card-icon-wrapper {
  position: relative;
  width: 70px;
  height: 70px;
}
.card-icon-frame {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  border: 2px solid #666;
  background: #000;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}
.card-icon-frame img {
  width: 90%;
  height: 90%;
  object-fit: contain;
}
.card-level-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #c62828;
  color: #fff;
  font-weight: bold;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  border: 1px solid #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);
}
.card-mythic-badge {
  position: absolute;
  bottom: -8px;
  right: -8px;
  background: #6200ea;
  color: #fff;
  font-weight: 900;
  font-size: 11px;
  padding: 2px 5px;
  border-radius: 4px;
  border: 1px solid #b388ff;
  z-index: 10;
}
.card-title-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.card-rarity-label {
  font-size: 11px;
  text-transform: uppercase;
  color: #aaa;
  margin-bottom: 4px;
  letter-spacing: 1px;
}
.card-item-name {
  margin: 0;
  font-size: 1.1rem;
  font-family: "Playfair Display", serif;
  line-height: 1.2;
}
.card-lore {
  padding: 15px 20px;
  font-style: italic;
  color: #888;
  font-size: 0.9rem;
  line-height: 1.4;
  font-family: "Playfair Display", serif;
}
.separator-line {
  height: 1px;
  background: linear-gradient(to right, transparent, #5a4a42, transparent);
  width: 100%;
  margin: 5px 0;
}
.card-main-stat {
  padding: 15px 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}
.stat-icon {
  font-size: 24px;
  color: #ddd;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 10px;
  text-transform: uppercase;
  color: #aaa;
  font-weight: bold;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #fff;
  font-family: "Orbitron", sans-serif;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}
.main-val {
  color: #ffd700;
  text-shadow: 0 0 8px rgba(255, 215, 0, 0.4);
}

.card-sub-stats {
  padding: 10px 20px;
  flex: 1;
  overflow-y: auto;
}
.sub-stat-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.95rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding-bottom: 2px;
}
.resource-type {
  margin-top: 10px;
  border-top: 1px dashed #444;
  padding-top: 5px;
}
.sub-label {
  color: #999;
}
.sub-val {
  font-weight: bold;
}
.sub-val.atk {
  color: #ff5252;
}
.sub-val.def {
  color: #448aff;
}
.sub-val.hp {
  color: #69f0ae;
}
.sub-val.speed {
  color: #00e5ff;
}
.sub-val.resource {
  color: #ffd700;
}

.card-footer-info {
  padding: 10px 20px;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid #333;
  font-size: 0.85rem;
  color: #d4af37;
}
.eq-score {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.score-val {
  font-weight: bold;
  font-family: "Orbitron";
  font-size: 1.1em;
}
.card-actions {
  padding: 15px 20px;
  display: flex;
  gap: 10px;
  background: #151515;
}
.card-btn {
  flex: 1;
  padding: 12px;
  border: none;
  font-weight: bold;
  text-transform: uppercase;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  clip-path: polygon(10% 0, 100% 0, 100% 80%, 90% 100%, 0 100%, 0 20%);
}
.card-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.2);
}
.use-btn {
  background: #ffa000;
  color: #000;
}
.equip-btn {
  background: linear-gradient(to right, #2e7d32, #43a047);
  color: #fff;
}
.unequip-btn {
  background: #424242;
  color: #fff;
}
.sell-btn {
  background: #c62828;
  color: #fff;
}

.text-rarity-COMMON {
  color: #fff;
}
.text-rarity-UNCOMMON {
  color: #4caf50;
}
.text-rarity-RARE {
  color: #42a5f5;
}
.text-rarity-EPIC {
  color: #ab47bc;
}
.text-rarity-LEGENDARY {
  color: #ffeb3b;
  text-shadow: 0 0 5px #ffeb3b;
}
.text-rarity-MYTHIC {
  color: #ff1744;
  text-shadow: 0 0 8px #ff1744;
}
.rarity-COMMON {
  border-color: #777;
}
.rarity-UNCOMMON {
  border-color: #4caf50;
}
.rarity-RARE {
  border-color: #42a5f5;
}
.rarity-EPIC {
  border-color: #ab47bc;
  box-shadow: 0 0 5px #ab47bc;
}
.rarity-LEGENDARY {
  border-color: #ffeb3b;
  box-shadow: 0 0 8px #ffeb3b;
}
.rarity-MYTHIC {
  border-color: #ff1744;
  box-shadow: 0 0 12px #ff1744;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}
.dark-modal {
  background: #222;
  border: 1px solid #555;
  width: 400px;
}
.modal-header {
  background: #333;
  color: #eee;
  padding: 10px;
  text-align: center;
  font-weight: bold;
}
.modal-body {
  padding: 20px;
}
.trade-tabs {
  display: flex;
  margin-bottom: 15px;
  border: 1px solid #444;
}
.trade-tab {
  flex: 1;
  padding: 8px;
  text-align: center;
  cursor: pointer;
  background: #111;
  color: #777;
}
.trade-tab.active {
  background: #444;
  color: #fff;
}
.dark-input {
  width: 100%;
  background: #111;
  border: 1px solid #444;
  color: #fff;
  padding: 8px;
  margin-top: 5px;
}
.modal-footer {
  display: flex;
  border-top: 1px solid #333;
}
.modal-btn {
  flex: 1;
  padding: 12px;
  border: none;
  cursor: pointer;
  font-weight: bold;
}
.modal-btn.cancel {
  background: #333;
  color: #aaa;
}
.modal-btn.confirm {
  background: #00e676;
  color: #000;
}
</style> -->


<template>
  <div class="page-container inventory-page">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

    <div class="inventory-wrapper">
      <div class="main-chest-frame">
        <div class="chest-header">
          <div class="header-decoration left"></div>
          <h2 class="chest-title">HÀNH TRANG</h2>
          <div class="header-decoration right"></div>
        </div>

        <div class="tab-control-bar">
          <button class="tab-btn" :class="{ active: filter === 'ALL' }" @click="filter = 'ALL'">
            TẤT CẢ
          </button>
          <div class="separator">|</div>
          <button class="tab-btn" :class="{ active: filter === 'EQUIP' }" @click="filter = 'EQUIP'">
            TRANG BỊ
          </button>
          <div class="separator">|</div>
          <button class="tab-btn" :class="{ active: filter === 'MAT' }" @click="filter = 'MAT'">
            VẬT PHẨM
          </button>
          <div class="capacity-badge">
            <i class="fas fa-weight-hanging"></i> {{ filteredItems.length }} / 50
          </div>
        </div>

        <div class="inventory-grid custom-scroll">
          <div
            v-for="u in filteredItems"
            :key="u.userItemId"
            class="grid-slot"
            :class="[
              'rarity-' + (u.rarity || getSafeItem(u).rarity || 'COMMON'),
              {
                selected: selectedItem?.userItemId === u.userItemId,
                equipped: u.isEquipped,
                // virtual: u.isVirtual, // [FIX] Bỏ class virtual
              },
            ]"
            @click="selectedItem = u"
          >
            <div class="slot-inner">
              <img
                :src="getCorrectImage(getSafeItem(u))"
                class="item-icon"
                alt="item"
                @error="$event.target.src = getItemImage('o_coal.png')"
              />

              <div class="qty-tag" v-if="u.quantity > 1">
                {{ u.quantity }}
              </div>

              <div class="equip-tag" v-if="u.isEquipped">
                <i class="fas fa-shield-alt"></i> E
              </div>

              <div
                v-if="u.enhanceLevel > 0"
                class="level-tag-mini"
                :class="getLevelClass(u.enhanceLevel)"
              >
                +{{ u.enhanceLevel }}
              </div>
            </div>

            <div
              class="selection-border"
              v-if="selectedItem?.userItemId === u.userItemId"
            ></div>
          </div>

          <div
            v-for="n in Math.max(0, 50 - filteredItems.length)"
            :key="'empty-' + n"
            class="grid-slot empty"
          ></div>
        </div>
      </div>

      <div class="right-panel-container">
        <transition name="fade-slide" mode="out-in">
          <div
            v-if="selectedItem"
            class="item-card-panel"
            :key="selectedItem.userItemId"
          >
            <div class="card-header-bg">
              <div class="card-icon-wrapper">
                <div
                  class="card-icon-frame"
                  :class="'rarity-' + (selectedItem.rarity || getSafeItem(selectedItem).rarity || 'COMMON')"
                >
                  <img
                    :src="getCorrectImage(getSafeItem(selectedItem))"
                    @error="$event.target.src = getItemImage('o_coal.png')"
                  />
                </div>

                <div v-if="selectedItem.enhanceLevel > 0" class="card-level-badge">
                  +{{ selectedItem.enhanceLevel }}
                </div>
                <div v-if="selectedItem.isMythic" class="card-mythic-badge">
                  M{{ selectedItem.mythicStars || selectedItem.mythicLevel || 0 }}
                </div>
              </div>

              <div class="card-title-block">
                <div class="card-rarity-label">
                  {{ translateRarity(selectedItem.rarity || getSafeItem(selectedItem).rarity) }}
                  {{ translateType(getSafeItem(selectedItem).type) }}
                </div>
                <h3
                  class="card-item-name"
                  :class="'text-rarity-' + (selectedItem.rarity || getSafeItem(selectedItem).rarity || 'COMMON')"
                >
                  {{ getSafeItem(selectedItem).name }}
                </h3>
              </div>
            </div>

            <div class="card-lore">
              "{{ getSafeItem(selectedItem).description || "Vật phẩm bí ẩn." }}"
            </div>
            <div class="separator-line"></div>

            <div class="card-main-stat">
              <div class="stat-icon"><i class="fas fa-khanda"></i></div>
              <div class="stat-info">
                <span class="stat-label">SỨC MẠNH CHIẾN ĐẤU</span>
                <span class="stat-value main-val">
                  {{ calculateCombatPower(selectedItem) }}
                </span>
              </div>
            </div>

            <div class="separator-line"></div>

            <div class="card-sub-stats">
              <div class="sub-stat-row">
                <span class="sub-label">Tấn Công</span>
                <span class="sub-val atk">+{{ calculateTotalStat(selectedItem, "ATK") }}</span>
              </div>
              <div class="sub-stat-row">
                <span class="sub-label">Phòng Thủ</span>
                <span class="sub-val def">+{{ calculateTotalStat(selectedItem, "DEF") }}</span>
              </div>
              <div class="sub-stat-row">
                <span class="sub-label">Sinh Lực</span>
                <span class="sub-val hp">+{{ calculateTotalStat(selectedItem, "HP") }}</span>
              </div>
              <div class="sub-stat-row">
                <span class="sub-label">Tốc Độ</span>
                <span class="sub-val speed">+{{ calculateTotalStat(selectedItem, "SPEED") }}</span>
              </div>
            </div>

            <div class="card-footer-info">
              <div class="eq-score">
                <span class="score-label">Điểm Trang Bị</span>
                <span class="score-val">
                  {{ calculateCombatPower(selectedItem) + (getSafeItem(selectedItem).tier * 5 || 0) }}
                </span>
              </div>
            </div>

            <div class="card-actions">
              <button
                v-if="getSafeItem(selectedItem).type === 'CONSUMABLE'"
                class="card-btn use-btn"
                @click="inventoryStore.useItem(selectedItem.userItemId)"
              >
                DÙNG
              </button>

              <template v-if="canEquip(selectedItem)">
                <button
                  v-if="!selectedItem.isEquipped"
                  class="card-btn equip-btn"
                  @click="inventoryStore.equipItem(selectedItem.userItemId)"
                >
                  TRANG BỊ
                </button>
                <button
                  v-else
                  class="card-btn unequip-btn"
                  @click="inventoryStore.unequipItem(selectedItem.userItemId)"
                >
                  GỠ BỎ
                </button>
              </template>

              <button
                v-if="!selectedItem.isEquipped"
                class="card-btn sell-btn"
                @click="openSellModal(selectedItem)"
              >
                BÁN
              </button>
            </div>
          </div>

          <div v-else class="item-card-placeholder" key="placeholder">
            <div class="placeholder-text">Chọn một vật phẩm để xem</div>
          </div>
        </transition>
      </div>
    </div>

    <div v-if="showSell" class="modal-overlay">
      <div class="dark-modal">
        <div class="modal-header">
          <span class="decor">❖</span> GIAO DỊCH <span class="decor">❖</span>
        </div>
        <div class="modal-body">
          <h3 class="trade-item-name text-gradient">
            {{ getSafeItem(sellItem).name }}
          </h3>
          <div class="trade-tabs">
            <div class="trade-tab" :class="{ active: mode === 'NPC' }" @click="mode = 'NPC'">
              TIỆM CẦM ĐỒ
            </div>
            <div class="trade-tab" :class="{ active: mode === 'P2P' }" @click="mode = 'P2P'">
              CHỢ TRỜI
            </div>
          </div>
          <div class="trade-form">
            <div class="form-group">
              <label>SỐ LƯỢNG</label>
              <input type="number" v-model.number="qty" min="1" :max="sellItem.quantity" class="dark-input" />
            </div>
            <div v-if="mode === 'P2P'" class="form-group">
              <label>ĐƠN GIÁ</label>
              <input type="number" v-model.number="price" class="dark-input" />
            </div>
            <div class="trade-summary">
              Tổng thu:
              <span class="money-highlight">
                {{ mode === "NPC" ? getSafeItem(sellItem).basePrice * 0.5 * qty : price * qty }}
                <i class="fas fa-coins"></i>
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="showSell = false">HỦY</button>
          <button class="modal-btn confirm" @click="mode === 'NPC' ? confirmNPC() : confirmP2P()">
            XÁC NHẬN
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useInventoryStore } from "../stores/inventoryStore";
import { useCharacterStore } from "../stores/characterStore";
import { useMarketStore } from "../stores/marketStore";
import { useAuthStore } from "../stores/authStore";
import { resolveItemImage } from "@/utils/assetHelper";

// --- BACKGROUND LOGIC ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC HÀNH TRANG ---
const inventoryStore = useInventoryStore();
const charStore = useCharacterStore();
const marketStore = useMarketStore();
const authStore = useAuthStore();

const getItemImage = resolveItemImage;

const filter = ref("ALL");
const selectedItem = ref(null);

const EQUIP_TYPES = ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"];
const MATERIAL_TYPES = ["MATERIAL", "CONSUMABLE"];

const getSafeItem = (u) => (u && u.item ? u.item : {});
const getLevelClass = (lv) =>
  lv >= 30 ? "lvl-red" : lv >= 15 ? "lvl-purple" : lv >= 10 ? "lvl-gold" : "lvl-white";

// --- [FIX] HÀM XỬ LÝ ẢNH CHUẨN GIT ---
const getCorrectImage = (item) => {
  if (!item) return getItemImage('o_coal.png');
  
  let imgName = item.image || item.imageUrl || 'o_coal.png';
  
  const mapping = {
    'w_woodRed.png': 'w_wood-red.png',
    'w_woodWhite.png': 'w_wood-white.png',
    'w_woodBlack.png': 'w_wood-black.png',
    'w_woodred.png': 'w_wood-red.png',
    'w_woodwhite.png': 'w_wood-white.png',
    'w_woodblack.png': 'w_wood-black.png'
  };

  if (mapping[imgName]) {
    imgName = mapping[imgName];
  }

  return getItemImage(imgName);
};

// --- STATS ---
const getStatVal = (obj, ...keys) => {
  if (!obj) return 0;
  for (const key of keys) {
    if (obj[key] !== undefined && obj[key] !== null) return Number(obj[key]);
  }
  return 0;
};

const calculateTotalStat = (u, type) => {
  if (!u || !u.item) return 0;
  let total = 0;
  const itemBase = u.item;
  const subStats = u.subStats || [];
  const mainType = u.mainStatType || u.main_stat_type || "";
  const mainVal = Number(u.mainStatValue || u.main_stat_value || 0);

  if (type === "ATK") total += getStatVal(itemBase, "atkBonus", "atk_bonus");
  if (type === "DEF") total += getStatVal(itemBase, "defBonus", "def_bonus");
  if (type === "HP") total += getStatVal(itemBase, "hpBonus", "hp_bonus");
  if (type === "SPEED") total += getStatVal(itemBase, "speedBonus", "speed_bonus", "speed");

  if (mainType) {
    if (type === "ATK" && ["ATK_FLAT", "STR"].includes(mainType)) total += mainVal;
    if (type === "DEF" && mainType === "DEF_FLAT") total += mainVal;
    if (type === "HP" && mainType === "HP_FLAT") total += mainVal;
    if (type === "SPEED" && mainType === "SPEED") total += mainVal;
  }

  if (Array.isArray(subStats)) {
    subStats.forEach((stat) => {
      if (stat.is_percent) return;
      const code = stat.code;
      const val = Number(stat.value || 0);
      if (type === "ATK" && ["ATK_FLAT", "STR"].includes(code)) total += val;
      if (type === "DEF" && code === "DEF_FLAT") total += val;
      if (type === "HP" && code === "HP_FLAT") total += val;
      if (type === "SPEED" && ["SPEED", "DEX"].includes(code)) total += val;
    });
  }
  return Math.floor(total);
};

const calculateCombatPower = (u) => {
  const dbPower = getStatVal(u, "totalPower", "total_power");
  if (dbPower > 0) return dbPower;
  const atk = calculateTotalStat(u, "ATK");
  const def = calculateTotalStat(u, "DEF");
  const hp = calculateTotalStat(u, "HP");
  const speed = calculateTotalStat(u, "SPEED");
  const cp = atk + def + Math.floor(hp / 10) + speed * 2;
  return cp > 0 ? cp : getSafeItem(u).tier * 10 || 10;
};

// --- [FIX] LOẠI BỎ LOGIC ITEM ẢO ---
// const createVirtualItem = ... (Đã xóa để tránh hiển thị sai)

// --- FILTER & MAP ---
const filteredItems = computed(() => {
  let items = [];

  // Chỉ lấy item thực từ InventoryStore (đồng bộ với Backend)
  if (inventoryStore.items && Array.isArray(inventoryStore.items)) {
    items = [...inventoryStore.items];
  }

  // [FIX] Đã xóa đoạn code thêm Item ảo từ authStore.wallet
  // Điều này đảm bảo những gì bạn thấy là những gì bạn CÓ trong kho UserItem

  if (filter.value === "EQUIP") {
    items = items.filter((i) => i.item && EQUIP_TYPES.includes(i.item.type));
  }
  if (filter.value === "MAT") {
    items = items.filter((i) => i.item && MATERIAL_TYPES.includes(i.item.type));
  }

  return items;
});

// --- MODAL ---
const showSell = ref(false);
const sellItem = ref(null);
const mode = ref("NPC");
const qty = ref(1);
const price = ref(0);

const openSellModal = (item) => {
  // if (item.isVirtual) return; // Không cần check nữa
  sellItem.value = item;
  qty.value = 1;
  price.value = item.item.basePrice || 0;
  showSell.value = true;
};
const confirmNPC = async () => {
  await marketStore.sellItem(sellItem.value.userItemId, qty.value);
  showSell.value = false;
  selectedItem.value = null;
  await authStore.fetchProfile();
};
const confirmP2P = async () => {
  await marketStore.createListing(sellItem.value.userItemId, price.value, qty.value);
  showSell.value = false;
  selectedItem.value = null;
  await authStore.fetchProfile();
};

const canEquip = (u) => u.item && EQUIP_TYPES.includes(u.item.type);
const translateType = (t) =>
  ({
    WEAPON: "Binh Khí",
    ARMOR: "Y Phục",
    HELMET: "Mũ",
    BOOTS: "Giày",
    RING: "Nhẫn",
    NECKLACE: "Dây Chuyền",
  })[t] || t;
const translateRarity = (r) =>
  ({
    COMMON: "Thường",
    UNCOMMON: "Phổ Thông",
    RARE: "Hiếm",
    EPIC: "Sử Thi",
    LEGENDARY: "Huyền Thoại",
    MYTHIC: "Thần Thoại",
  })[r] || "Thường";

onMounted(async () => {
  updateDayNight(); 
  await Promise.all([
    inventoryStore.fetchInventory(),
    charStore.fetchCharacter(),
    authStore.fetchProfile(),
  ]);
});
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap");

.inventory-page {
  background-color: transparent;
  min-height: 100vh;
  color: #e0e0e0;
  position: relative;
  overflow: hidden;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
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

/* --- 2. LAYOUT HÀNH TRANG --- */
.inventory-wrapper {
  position: relative;
  z-index: 10;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30px;
  padding: 20px;
}

/* LEFT: GRID */
.main-chest-frame {
  width: 600px;
  height: 85vh;
  background: rgba(20, 20, 25, 0.95);
  border: 1px solid #444;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
  position: relative;
}
.chest-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #333;
  background: linear-gradient(to bottom, #1a1a1a, #222);
}
.chest-title {
  font-family: "Orbitron", sans-serif;
  color: var(--gold, #ffd700);
  font-size: 1.4rem;
  margin: 0 15px;
  text-transform: uppercase;
  letter-spacing: 2px;
}
.tab-control-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 15px;
  background: rgba(0, 0, 0, 0.4);
  font-size: 0.9rem;
}
.tab-btn {
  background: none;
  border: none;
  color: #777;
  font-weight: bold;
  cursor: pointer;
  padding: 5px;
  transition: 0.2s;
}
.tab-btn.active {
  color: #ffd700;
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}
.capacity-badge {
  margin-left: auto;
  color: #555;
}
.inventory-grid {
  flex: 1;
  padding: 15px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(65px, 1fr));
  grid-auto-rows: 65px;
  gap: 8px;
  overflow-y: auto;
  align-content: start;
  background: rgba(0, 0, 0, 0.2);
}
.grid-slot {
  position: relative;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  cursor: pointer;
  transition: all 0.2s;
}
.grid-slot:hover {
  background: rgba(255, 255, 255, 0.08);
}
.grid-slot.selected {
  border-color: #ffd700;
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.2);
}
.grid-slot.empty {
  border: 1px dashed #333;
  opacity: 0.2;
  pointer-events: none;
  background: none;
}
.slot-inner {
  width: 100%;
  height: 100%;
  padding: 4px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.item-icon {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.qty-tag {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  font-size: 9px;
  padding: 1px 3px;
  border-radius: 2px;
}
.equip-tag {
  position: absolute;
  top: 1px;
  left: 1px;
  color: #00ff9d;
  font-size: 9px;
  background: rgba(0, 0, 0, 0.6);
  padding: 1px 2px;
  border-radius: 2px;
}
.level-tag-mini {
  position: absolute;
  top: 1px;
  right: 1px;
  font-size: 9px;
  font-weight: 900;
  color: #ffd700;
  text-shadow: 1px 1px 0 #000;
}

/* RIGHT: CARD */
.item-card-panel,
.item-card-placeholder {
  width: 360px;
  height: 600px;
  background: #1e1b1b;
  border: 2px solid #5a4a42;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 5px 5px 20px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}
.item-card-placeholder {
  border: 2px dashed #444;
  background: transparent;
  justify-content: center;
  align-items: center;
  color: #555;
  font-style: italic;
  background: rgba(30,27,27,0.8); 
}
.card-header-bg {
  background: linear-gradient(135deg, #2b1d1d 0%, #1a1a1a 100%);
  padding: 20px;
  display: flex;
  gap: 15px;
  border-bottom: 2px solid #5a4a42;
  position: relative;
}
.card-icon-wrapper {
  position: relative;
  width: 70px;
  height: 70px;
}
.card-icon-frame {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  border: 2px solid #666;
  background: #000;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}
.card-icon-frame img {
  width: 90%;
  height: 90%;
  object-fit: contain;
}
.card-level-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #c62828;
  color: #fff;
  font-weight: bold;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  border: 1px solid #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);
}
.card-mythic-badge {
  position: absolute;
  bottom: -8px;
  right: -8px;
  background: #6200ea;
  color: #fff;
  font-weight: 900;
  font-size: 11px;
  padding: 2px 5px;
  border-radius: 4px;
  border: 1px solid #b388ff;
  z-index: 10;
}
.card-title-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.card-rarity-label {
  font-size: 11px;
  text-transform: uppercase;
  color: #aaa;
  margin-bottom: 4px;
  letter-spacing: 1px;
}
.card-item-name {
  margin: 0;
  font-size: 1.1rem;
  font-family: "Playfair Display", serif;
  line-height: 1.2;
}
.card-lore {
  padding: 15px 20px;
  font-style: italic;
  color: #888;
  font-size: 0.9rem;
  line-height: 1.4;
  font-family: "Playfair Display", serif;
}
.separator-line {
  height: 1px;
  background: linear-gradient(to right, transparent, #5a4a42, transparent);
  width: 100%;
  margin: 5px 0;
}
.card-main-stat {
  padding: 15px 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}
.stat-icon {
  font-size: 24px;
  color: #ddd;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 10px;
  text-transform: uppercase;
  color: #aaa;
  font-weight: bold;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #fff;
  font-family: "Orbitron", sans-serif;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}
.main-val {
  color: #ffd700;
  text-shadow: 0 0 8px rgba(255, 215, 0, 0.4);
}

.card-sub-stats {
  padding: 10px 20px;
  flex: 1;
  overflow-y: auto;
}
.sub-stat-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.95rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding-bottom: 2px;
}
.resource-type {
  margin-top: 10px;
  border-top: 1px dashed #444;
  padding-top: 5px;
}
.sub-label {
  color: #999;
}
.sub-val {
  font-weight: bold;
}
.sub-val.atk {
  color: #ff5252;
}
.sub-val.def {
  color: #448aff;
}
.sub-val.hp {
  color: #69f0ae;
}
.sub-val.speed {
  color: #00e5ff;
}
.sub-val.resource {
  color: #ffd700;
}

.card-footer-info {
  padding: 10px 20px;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid #333;
  font-size: 0.85rem;
  color: #d4af37;
}
.eq-score {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.score-val {
  font-weight: bold;
  font-family: "Orbitron";
  font-size: 1.1em;
}
.card-actions {
  padding: 15px 20px;
  display: flex;
  gap: 10px;
  background: #151515;
}
.card-btn {
  flex: 1;
  padding: 12px;
  border: none;
  font-weight: bold;
  text-transform: uppercase;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  clip-path: polygon(10% 0, 100% 0, 100% 80%, 90% 100%, 0 100%, 0 20%);
}
.card-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.2);
}
.use-btn {
  background: #ffa000;
  color: #000;
}
.equip-btn {
  background: linear-gradient(to right, #2e7d32, #43a047);
  color: #fff;
}
.unequip-btn {
  background: #424242;
  color: #fff;
}
.sell-btn {
  background: #c62828;
  color: #fff;
}

.text-rarity-COMMON {
  color: #fff;
}
.text-rarity-UNCOMMON {
  color: #4caf50;
}
.text-rarity-RARE {
  color: #42a5f5;
}
.text-rarity-EPIC {
  color: #ab47bc;
}
.text-rarity-LEGENDARY {
  color: #ffeb3b;
  text-shadow: 0 0 5px #ffeb3b;
}
.text-rarity-MYTHIC {
  color: #ff1744;
  text-shadow: 0 0 8px #ff1744;
}
.rarity-COMMON {
  border-color: #777;
}
.rarity-UNCOMMON {
  border-color: #4caf50;
}
.rarity-RARE {
  border-color: #42a5f5;
}
.rarity-EPIC {
  border-color: #ab47bc;
  box-shadow: 0 0 5px #ab47bc;
}
.rarity-LEGENDARY {
  border-color: #ffeb3b;
  box-shadow: 0 0 8px #ffeb3b;
}
.rarity-MYTHIC {
  border-color: #ff1744;
  box-shadow: 0 0 12px #ff1744;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}
.dark-modal {
  background: #222;
  border: 1px solid #555;
  width: 400px;
}
.modal-header {
  background: #333;
  color: #eee;
  padding: 10px;
  text-align: center;
  font-weight: bold;
}
.modal-body {
  padding: 20px;
}
.trade-tabs {
  display: flex;
  margin-bottom: 15px;
  border: 1px solid #444;
}
.trade-tab {
  flex: 1;
  padding: 8px;
  text-align: center;
  cursor: pointer;
  background: #111;
  color: #777;
}
.trade-tab.active {
  background: #444;
  color: #fff;
}
.dark-input {
  width: 100%;
  background: #111;
  border: 1px solid #444;
  color: #fff;
  padding: 8px;
  margin-top: 5px;
}
.modal-footer {
  display: flex;
  border-top: 1px solid #333;
}
.modal-btn {
  flex: 1;
  padding: 12px;
  border: none;
  cursor: pointer;
  font-weight: bold;
}
.modal-btn.cancel {
  background: #333;
  color: #aaa;
}
.modal-btn.confirm {
  background: #00e676;
  color: #000;
}
</style>