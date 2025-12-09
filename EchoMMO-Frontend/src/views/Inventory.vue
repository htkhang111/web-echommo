<!-- <template>
  <div class="page-container inventory-page wuxia-dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="inventory-wrapper">
      <div class="main-chest-frame">
        <div class="chest-header">
          <div class="header-decoration left"></div>
          <h2 class="chest-title">HÀNH TRANG</h2>
          <div class="header-decoration right"></div>
        </div>

        <div class="tab-control-bar">
          <button
            class="tab-btn"
            :class="{ active: filter === 'ALL' }"
            @click="filter = 'ALL'"
          >
            TẤT CẢ
          </button>
          <div class="separator">|</div>
          <button
            class="tab-btn"
            :class="{ active: filter === 'EQUIP' }"
            @click="filter = 'EQUIP'"
          >
            TRANG BỊ
          </button>
          <div class="separator">|</div>
          <button
            class="tab-btn"
            :class="{ active: filter === 'MAT' }"
            @click="filter = 'MAT'"
          >
            VẬT PHẨM
          </button>

          <div class="capacity-badge">
            <i class="fas fa-weight-hanging"></i>
            {{ filteredItems.length }} / 50
          </div>
        </div>

        <div class="inventory-grid custom-scroll">
          <div
            v-for="u in filteredItems"
            :key="u.userItemId"
            class="grid-slot"
            :class="[
              'rarity-' + (u.item.rarity || 'C'),
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
                :src="resolveItemImage(u.item.image || u.item.imageUrl)"
                class="item-icon"
                alt="item"
                @error="$event.target.src = resolveItemImage('r_gold_coin.png')"
              />

              <div class="qty-tag" v-if="u.quantity > 1 || u.isVirtual">
                {{ u.quantity }}
              </div>

              <div class="equip-tag" v-if="u.isEquipped">
                <i class="fas fa-shield-alt"></i> E
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

        <transition name="slide-up">
          <div v-if="selectedItem" class="item-detail-panel">
            <div class="detail-content">
              <div class="detail-left">
                <div
                  class="item-portrait"
                  :class="'rarity-' + (selectedItem.item.rarity || 'C')"
                >
                  <img
                    :src="
                      resolveItemImage(
                        selectedItem.item.image || selectedItem.item.imageUrl,
                      )
                    "
                  />
                </div>
              </div>

              <div class="detail-mid">
                <div class="item-header">
                  <h3
                    :class="'text-rarity-' + (selectedItem.item.rarity || 'C')"
                  >
                    {{ selectedItem.item.name }}
                  </h3>
                  <span class="item-type-badge">{{
                    translateType(selectedItem.item.type)
                  }}</span>
                </div>

                <div class="item-stats-row">
                  <span v-if="selectedItem.item.atkBonus" class="stat atk"
                    >+{{ selectedItem.item.atkBonus }} CÔNG</span
                  >
                  <span v-if="selectedItem.item.defBonus" class="stat def"
                    >+{{ selectedItem.item.defBonus }} THỦ</span
                  >
                  <span v-if="selectedItem.item.hpBonus" class="stat hp"
                    >+{{ selectedItem.item.hpBonus }} HP</span
                  >
                  <span v-if="selectedItem.isVirtual" class="stat resource"
                    >TÀI NGUYÊN VÍ</span
                  >
                </div>

                <p class="item-desc">
                  {{ selectedItem.item.description || "Vật phẩm bí ẩn." }}
                </p>
              </div>

              <div class="detail-right actions">
                <button
                  v-if="
                    selectedItem.item.type === 'CONSUMABLE' &&
                    !selectedItem.isVirtual
                  "
                  class="action-btn use-btn"
                  @click="inventoryStore.useItem(selectedItem.userItemId)"
                >
                  SỬ DỤNG
                </button>

                <template v-if="canEquip(selectedItem)">
                  <button
                    v-if="!selectedItem.isEquipped"
                    class="action-btn equip-btn"
                    @click="inventoryStore.equipItem(selectedItem.userItemId)"
                  >
                    TRANG BỊ
                  </button>
                  <button
                    v-else
                    class="action-btn unequip-btn"
                    @click="inventoryStore.unequipItem(selectedItem.userItemId)"
                  >
                    GỠ BỎ
                  </button>
                </template>

                <button
                  v-if="!selectedItem.isEquipped && !selectedItem.isVirtual"
                  class="action-btn sell-btn"
                  @click="openSellModal(selectedItem)"
                >
                  BÁN
                </button>

                <div v-if="selectedItem.isVirtual" class="virtual-note">
                  (Không thể bán)
                </div>
              </div>
            </div>
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
            {{ sellItem.item.name }}
          </h3>

          <div class="trade-tabs">
            <div
              class="trade-tab"
              :class="{ active: mode === 'NPC' }"
              @click="mode = 'NPC'"
            >
              TIỆM CẦM ĐỒ
            </div>
            <div
              class="trade-tab"
              :class="{ active: mode === 'P2P' }"
              @click="mode = 'P2P'"
            >
              CHỢ TRỜI
            </div>
          </div>

          <div class="trade-form">
            <div class="form-group">
              <label>SỐ LƯỢNG</label>
              <input
                type="number"
                v-model.number="qty"
                min="1"
                :max="sellItem.quantity"
                class="dark-input"
              />
            </div>

            <div v-if="mode === 'P2P'" class="form-group">
              <label>ĐƠN GIÁ</label>
              <input type="number" v-model.number="price" class="dark-input" />
            </div>

            <div class="trade-summary">
              Tổng thu:
              <span class="money-highlight">
                {{
                  mode === "NPC"
                    ? sellItem.item.basePrice * 0.5 * qty
                    : price * qty
                }}
                <i class="fas fa-coins"></i>
              </span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="modal-btn cancel" @click="showSell = false">
            HỦY
          </button>
          <button
            class="modal-btn confirm"
            @click="mode === 'NPC' ? confirmNPC() : confirmP2P()"
          >
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

const inventoryStore = useInventoryStore();
const charStore = useCharacterStore();
const marketStore = useMarketStore();
const authStore = useAuthStore();

const filter = ref("ALL");
const selectedItem = ref(null);

const EQUIP_TYPES = ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"];
const MATERIAL_TYPES = ["MATERIAL", "CONSUMABLE"];

const filteredItems = computed(() => {
  // 1. Lấy item thực từ kho
  let items = inventoryStore.items ? [...inventoryStore.items] : [];

  // 2. Inject (chèn) Gỗ và Đá từ Wallet vào danh sách như Item ảo
  // [FIX] Kiểm tra kỹ object wallet để tránh lỗi null
  if (authStore.wallet) {
    const { wood, stone } = authStore.wallet;

    // [FIX] Luôn hiển thị kể cả khi số lượng = 0 để test
    items.push({
      userItemId: "virtual_wood",
      quantity: wood || 0, // Mặc định 0 nếu null
      isEquipped: false,
      isVirtual: true,
      item: {
        name: "Gỗ",
        type: "MATERIAL",
        rarity: "C",
        image: "r_go.png",
        description: "Tài nguyên khai thác được. Dùng để xây dựng.",
        basePrice: 0,
      },
    });

    items.push({
      userItemId: "virtual_stone",
      quantity: stone || 0,
      isEquipped: false,
      isVirtual: true,
      item: {
        name: "Đá",
        type: "MATERIAL",
        rarity: "C",
        image: "stone_1.png",
        description: "Khoáng sản thô cứng. Dùng để chế tác.",
        basePrice: 0,
      },
    });
  } else {
    console.warn("Wallet data not found in AuthStore!");
  }

  // 3. Logic lọc hiển thị
  if (filter.value === "EQUIP") {
    items = items.filter((i) => i.item && EQUIP_TYPES.includes(i.item.type));
  }
  if (filter.value === "MAT") {
    items = items.filter((i) => i.item && MATERIAL_TYPES.includes(i.item.type));
  }
  return items;
});

// Logic Bán đồ (Giữ nguyên)
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
};
const confirmP2P = async () => {
  await marketStore.createListing(
    sellItem.value.userItemId,
    price.value,
    qty.value,
  );
  showSell.value = false;
  selectedItem.value = null;
};

const canEquip = (u) => u.item && EQUIP_TYPES.includes(u.item.type);

const translateType = (type) => {
  const map = {
    WEAPON: "Binh Khí",
    ARMOR: "Y Phục",
    HELMET: "Mũ",
    BOOTS: "Giày",
    RING: "Nhẫn",
    NECKLACE: "Dây Chuyền",
    CONSUMABLE: "Tiêu Hao",
    MATERIAL: "Nguyên Liệu",
  };
  return map[type] || type;
};

onMounted(async () => {
  inventoryStore.fetchInventory();
  charStore.fetchCharacter();
  // [FIX] Đảm bảo gọi await để ví về kịp
  await authStore.fetchProfile();
});
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- LAYOUT & THEME --- */
.wuxia-dark-theme {
  background-color: #212121;
  min-height: 100vh;
  color: #f3f4f6;
  position: relative;
  overflow: hidden;
}

.inventory-wrapper {
  position: relative;
  z-index: 10;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

/* --- CHEST FRAME --- */
.main-chest-frame {
  width: 100%;
  max-width: 900px;
  height: 90vh;
  background: rgba(20, 20, 25, 0.95);
  border: 1px solid #444;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
}

/* Header */
.chest-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #333;
}

.chest-title {
  font-family: "Orbitron", sans-serif;
  color: var(--gold, #ffd700);
  font-size: 1.5rem;
  margin: 0 20px;
}

/* Tabs */
.tab-control-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 20px;
  background: rgba(0, 0, 0, 0.2);
}

.tab-btn {
  background: none;
  border: none;
  color: #888;
  font-weight: bold;
  cursor: pointer;
  padding: 5px;
  transition: 0.2s;
}

.tab-btn.active {
  color: var(--gold, #ffd700);
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}

.capacity-badge {
  margin-left: auto;
  color: #666;
}

/* --- GRID --- */
.inventory-grid {
  flex: 1;
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(70px, 1fr));
  grid-auto-rows: 70px;
  gap: 10px;
  overflow-y: auto;
  align-content: start;
}

.grid-slot {
  position: relative;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 4px;
  cursor: pointer;
}

.grid-slot.empty {
  border: 1px dashed #333;
  opacity: 0.3;
  pointer-events: none;
}

.slot-inner {
  width: 100%;
  height: 100%;
  padding: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.item-icon {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* Selection Border (White outline) */
.selection-border {
  position: absolute;
  inset: -2px;
  border: 2px solid #fff;
  border-radius: 4px;
  pointer-events: none;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

/* Badges */
.qty-tag {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
}

.equip-tag {
  position: absolute;
  top: 2px;
  left: 2px;
  color: #00ff9d;
  font-size: 10px;
}

/* --- DETAIL PANEL --- */
.item-detail-panel {
  height: 220px;
  background: #1a1a1a;
  border-top: 1px solid #444;
  padding: 20px;
}

.detail-content {
  display: flex;
  gap: 20px;
  height: 100%;
}

.item-portrait {
  width: 100px;
  height: 100px;
  background: #000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.item-portrait img {
  max-width: 80%;
  max-height: 80%;
}

.detail-mid {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.item-type-badge {
  font-size: 12px;
  background: #333;
  padding: 2px 6px;
  color: #aaa;
}

.item-desc {
  font-style: italic;
  color: #888;
  font-size: 0.9rem;
}

.item-stats-row {
  display: flex;
  gap: 15px;
  margin-bottom: 8px;
  font-weight: bold;
  font-size: 0.9rem;
}

.stat.atk {
  color: #ff5252;
}

.stat.def {
  color: #448aff;
}

.stat.hp {
  color: #69f0ae;
}

.stat.resource {
  color: #ffd700;
}

.detail-right {
  width: 140px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

/* Action Buttons */
.action-btn {
  padding: 10px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  color: #000;
  text-transform: uppercase;
  transition: 0.2s;
}

.action-btn:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
}

.use-btn {
  background: #ffa000;
}

.equip-btn {
  background: #00e676;
}

.unequip-btn {
  background: #757575;
  color: #fff;
}

.sell-btn {
  background: #ff1744;
  color: #fff;
}

.virtual-note {
  color: #666;
  font-size: 0.8rem;
  text-align: center;
  font-style: italic;
}

/* Modal Styles */
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
  padding: 0;
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
  <div class="page-container inventory-page wuxia-dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="inventory-wrapper">
      <div class="main-chest-frame">
        <div class="chest-header">
          <div class="header-decoration left"></div>
          <h2 class="chest-title">HÀNH TRANG</h2>
          <div class="header-decoration right"></div>
        </div>

        <div class="tab-control-bar">
          <button
            class="tab-btn"
            :class="{ active: filter === 'ALL' }"
            @click="filter = 'ALL'"
          >
            TẤT CẢ
          </button>
          <div class="separator">|</div>
          <button
            class="tab-btn"
            :class="{ active: filter === 'EQUIP' }"
            @click="filter = 'EQUIP'"
          >
            TRANG BỊ
          </button>
          <div class="separator">|</div>
          <button
            class="tab-btn"
            :class="{ active: filter === 'MAT' }"
            @click="filter = 'MAT'"
          >
            VẬT PHẨM
          </button>

          <div class="capacity-badge">
            <i class="fas fa-weight-hanging"></i>
            {{ filteredItems.length }} / 50
          </div>
        </div>

        <div class="inventory-grid custom-scroll">
          <div
            v-for="u in filteredItems"
            :key="u.userItemId"
            class="grid-slot"
            :class="[
              'rarity-' + (u.item.rarity || 'C'),
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
                :src="resolveItemImage(u.item.image || u.item.imageUrl)"
                class="item-icon"
                alt="item"
                @error="
                  $event.target.src = resolveItemImage('r_gold_coin.png')
                "
              />

              <div class="qty-tag" v-if="u.quantity > 1 || u.isVirtual">
                {{ u.quantity }}
              </div>

              <div class="equip-tag" v-if="u.isEquipped">
                <i class="fas fa-shield-alt"></i> E
              </div>

              <div 
                v-if="(u.enhanceLevel && u.enhanceLevel > 0) || (u.level && u.level > 0)" 
                class="level-tag"
                :class="getLevelClass(u.enhanceLevel || u.level)"
              >
                +{{ u.enhanceLevel || u.level }}
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

        <transition name="slide-up">
          <div v-if="selectedItem" class="item-detail-panel">
            <div class="detail-content">
              <div class="detail-left">
                <div
                  class="item-portrait"
                  :class="'rarity-' + (selectedItem.item.rarity || 'C')"
                >
                  <img
                    :src="
                      resolveItemImage(
                        selectedItem.item.image || selectedItem.item.imageUrl,
                      )
                    "
                  />
                </div>
              </div>

              <div class="detail-mid">
                <div class="item-header">
                  <h3
                    :class="'text-rarity-' + (selectedItem.item.rarity || 'C')"
                  >
                    {{ selectedItem.item.name }}
                    
                    <span 
                      v-if="(selectedItem.enhanceLevel && selectedItem.enhanceLevel > 0) || (selectedItem.level && selectedItem.level > 0)" 
                      class="name-level-suffix"
                      :class="getLevelClass(selectedItem.enhanceLevel || selectedItem.level)"
                    >
                      (+{{ selectedItem.enhanceLevel || selectedItem.level }})
                    </span>
                  </h3>
                  <span class="item-type-badge">{{
                    translateType(selectedItem.item.type)
                  }}</span>
                </div>

                <div class="item-stats-row">
                  <span v-if="selectedItem.item.atkBonus" class="stat atk"
                    >+{{ selectedItem.item.atkBonus }} CÔNG</span
                  >
                  <span v-if="selectedItem.item.defBonus" class="stat def"
                    >+{{ selectedItem.item.defBonus }} THỦ</span
                  >
                  <span v-if="selectedItem.item.hpBonus" class="stat hp"
                    >+{{ selectedItem.item.hpBonus }} HP</span
                  >
                  <span v-if="selectedItem.isVirtual" class="stat resource"
                    >TÀI NGUYÊN VÍ</span
                  >
                </div>

                <p class="item-desc">
                  {{ selectedItem.item.description || "Vật phẩm bí ẩn." }}
                </p>
              </div>

              <div class="detail-right actions">
                <button
                  v-if="
                    selectedItem.item.type === 'CONSUMABLE' &&
                    !selectedItem.isVirtual
                  "
                  class="action-btn use-btn"
                  @click="inventoryStore.useItem(selectedItem.userItemId)"
                >
                  SỬ DỤNG
                </button>

                <template v-if="canEquip(selectedItem)">
                  <button
                    v-if="!selectedItem.isEquipped"
                    class="action-btn equip-btn"
                    @click="inventoryStore.equipItem(selectedItem.userItemId)"
                  >
                    TRANG BỊ
                  </button>
                  <button
                    v-else
                    class="action-btn unequip-btn"
                    @click="inventoryStore.unequipItem(selectedItem.userItemId)"
                  >
                    GỠ BỎ
                  </button>
                </template>

                <button
                  v-if="!selectedItem.isEquipped && !selectedItem.isVirtual"
                  class="action-btn sell-btn"
                  @click="openSellModal(selectedItem)"
                >
                  BÁN
                </button>

                <div v-if="selectedItem.isVirtual" class="virtual-note">
                  (Không thể bán)
                </div>
              </div>
            </div>
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
            {{ sellItem.item.name }}
          </h3>

          <div class="trade-tabs">
            <div
              class="trade-tab"
              :class="{ active: mode === 'NPC' }"
              @click="mode = 'NPC'"
            >
              TIỆM CẦM ĐỒ
            </div>
            <div
              class="trade-tab"
              :class="{ active: mode === 'P2P' }"
              @click="mode = 'P2P'"
            >
              CHỢ TRỜI
            </div>
          </div>

          <div class="trade-form">
            <div class="form-group">
              <label>SỐ LƯỢNG</label>
              <input
                type="number"
                v-model.number="qty"
                min="1"
                :max="sellItem.quantity"
                class="dark-input"
              />
            </div>

            <div v-if="mode === 'P2P'" class="form-group">
              <label>ĐƠN GIÁ</label>
              <input type="number" v-model.number="price" class="dark-input" />
            </div>

            <div class="trade-summary">
              Tổng thu:
              <span class="money-highlight">
                {{
                  mode === "NPC"
                    ? sellItem.item.basePrice * 0.5 * qty
                    : price * qty
                }}
                <i class="fas fa-coins"></i>
              </span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="modal-btn cancel" @click="showSell = false">
            HỦY
          </button>
          <button
            class="modal-btn confirm"
            @click="mode === 'NPC' ? confirmNPC() : confirmP2P()"
          >
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

const inventoryStore = useInventoryStore();
const charStore = useCharacterStore();
const marketStore = useMarketStore();
const authStore = useAuthStore();

const filter = ref("ALL");
const selectedItem = ref(null);

const EQUIP_TYPES = ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"];
const MATERIAL_TYPES = ["MATERIAL", "CONSUMABLE"];

// [MỚI] Hàm lấy màu sắc cho Level
const getLevelClass = (lv) => {
  if (!lv) return "";
  if (lv >= 15) return "lvl-red";      // Thần thoại
  if (lv >= 10) return "lvl-purple";   // Huyền thoại
  if (lv >= 5) return "lvl-gold";      // Cao cấp
  return "lvl-white";                  // Thường
};

const filteredItems = computed(() => {
  let items = inventoryStore.items ? [...inventoryStore.items] : [];

  if (authStore.wallet) {
    const { wood, stone } = authStore.wallet;

    items.push({
      userItemId: "virtual_wood",
      quantity: wood || 0,
      isEquipped: false,
      isVirtual: true,
      item: {
        name: "Gỗ",
        type: "MATERIAL",
        rarity: "C",
        image: "r_go.png",
        description: "Tài nguyên khai thác được. Dùng để xây dựng.",
        basePrice: 0,
      },
    });

    items.push({
      userItemId: "virtual_stone",
      quantity: stone || 0,
      isEquipped: false,
      isVirtual: true,
      item: {
        name: "Đá",
        type: "MATERIAL",
        rarity: "C",
        image: "stone_1.png",
        description: "Khoáng sản thô cứng. Dùng để chế tác.",
        basePrice: 0,
      },
    });
  } else {
    console.warn("Wallet data not found in AuthStore!");
  }

  if (filter.value === "EQUIP") {
    items = items.filter((i) => i.item && EQUIP_TYPES.includes(i.item.type));
  }
  if (filter.value === "MAT") {
    items = items.filter((i) => i.item && MATERIAL_TYPES.includes(i.item.type));
  }
  return items;
});

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
};
const confirmP2P = async () => {
  await marketStore.createListing(
    sellItem.value.userItemId,
    price.value,
    qty.value,
  );
  showSell.value = false;
  selectedItem.value = null;
};

const canEquip = (u) => u.item && EQUIP_TYPES.includes(u.item.type);

const translateType = (type) => {
  const map = {
    WEAPON: "Binh Khí",
    ARMOR: "Y Phục",
    HELMET: "Mũ",
    BOOTS: "Giày",
    RING: "Nhẫn",
    NECKLACE: "Dây Chuyền",
    CONSUMABLE: "Tiêu Hao",
    MATERIAL: "Nguyên Liệu",
  };
  return map[type] || type;
};

onMounted(async () => {
  inventoryStore.fetchInventory();
  charStore.fetchCharacter();
  await authStore.fetchProfile();
});
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- LAYOUT & THEME --- */
.wuxia-dark-theme {
  background-color: #212121;
  min-height: 100vh;
  color: #f3f4f6;
  position: relative;
  overflow: hidden;
}

.inventory-wrapper {
  position: relative;
  z-index: 10;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

/* --- CHEST FRAME --- */
.main-chest-frame {
  width: 100%;
  max-width: 900px;
  height: 90vh;
  background: rgba(20, 20, 25, 0.95);
  border: 1px solid #444;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
}

/* Header */
.chest-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #333;
}

.chest-title {
  font-family: "Orbitron", sans-serif;
  color: var(--gold, #ffd700);
  font-size: 1.5rem;
  margin: 0 20px;
}

/* Tabs */
.tab-control-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 20px;
  background: rgba(0, 0, 0, 0.2);
}

.tab-btn {
  background: none;
  border: none;
  color: #888;
  font-weight: bold;
  cursor: pointer;
  padding: 5px;
  transition: 0.2s;
}

.tab-btn.active {
  color: var(--gold, #ffd700);
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}

.capacity-badge {
  margin-left: auto;
  color: #666;
}

/* --- GRID --- */
.inventory-grid {
  flex: 1;
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(70px, 1fr));
  grid-auto-rows: 70px;
  gap: 10px;
  overflow-y: auto;
  align-content: start;
}

.grid-slot {
  position: relative;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 4px;
  cursor: pointer;
}

.grid-slot.empty {
  border: 1px dashed #333;
  opacity: 0.3;
  pointer-events: none;
}

.slot-inner {
  width: 100%;
  height: 100%;
  padding: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.item-icon {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* Selection Border (White outline) */
.selection-border {
  position: absolute;
  inset: -2px;
  border: 2px solid #fff;
  border-radius: 4px;
  pointer-events: none;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

/* Badges */
.qty-tag {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
}

.equip-tag {
  position: absolute;
  top: 2px;
  left: 2px;
  color: #00ff9d;
  font-size: 10px;
}

/* --- [MỚI] STYLE CHO LEVEL TAG --- */
.level-tag {
  position: absolute;
  top: 2px;
  right: 2px;
  font-size: 11px;
  font-weight: 900;
  z-index: 5;
  font-family: 'Orbitron', sans-serif; /* Hoặc font mặc định của bạn */
  padding: 0 3px;
  border-radius: 3px;
  background: rgba(0,0,0,0.6);
  border: 1px solid rgba(255,255,255,0.1);
}

.name-level-suffix {
  margin-left: 8px;
  font-size: 0.9em;
  font-weight: bold;
}

/* Màu sắc theo cấp */
.lvl-white { color: #ffffff; text-shadow: 1px 1px 0 #000; }
.lvl-gold { color: #ffd700; text-shadow: 0 0 5px #ffd700, 1px 1px 0 #000; border-color: #ffd700; }
.lvl-purple { color: #d580ff; text-shadow: 0 0 8px #d580ff, 1px 1px 0 #000; border-color: #d580ff; }
.lvl-red { color: #ff3333; text-shadow: 0 0 10px #ff0000, 1px 1px 0 #000; border-color: #ff3333; }
/* -------------------------------- */

/* --- DETAIL PANEL --- */
.item-detail-panel {
  height: 220px;
  background: #1a1a1a;
  border-top: 1px solid #444;
  padding: 20px;
}

.detail-content {
  display: flex;
  gap: 20px;
  height: 100%;
}

.item-portrait {
  width: 100px;
  height: 100px;
  background: #000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.item-portrait img {
  max-width: 80%;
  max-height: 80%;
}

.detail-mid {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.item-type-badge {
  font-size: 12px;
  background: #333;
  padding: 2px 6px;
  color: #aaa;
}

.item-desc {
  font-style: italic;
  color: #888;
  font-size: 0.9rem;
}

.item-stats-row {
  display: flex;
  gap: 15px;
  margin-bottom: 8px;
  font-weight: bold;
  font-size: 0.9rem;
}

.stat.atk {
  color: #ff5252;
}

.stat.def {
  color: #448aff;
}

.stat.hp {
  color: #69f0ae;
}

.stat.resource {
  color: #ffd700;
}

.detail-right {
  width: 140px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

/* Action Buttons */
.action-btn {
  padding: 10px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  color: #000;
  text-transform: uppercase;
  transition: 0.2s;
}

.action-btn:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
}

.use-btn {
  background: #ffa000;
}

.equip-btn {
  background: #00e676;
}

.unequip-btn {
  background: #757575;
  color: #fff;
}

.sell-btn {
  background: #ff1744;
  color: #fff;
}

.virtual-note {
  color: #666;
  font-size: 0.8rem;
  text-align: center;
  font-style: italic;
}

/* Modal Styles */
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
  padding: 0;
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