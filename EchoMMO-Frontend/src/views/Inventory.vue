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
            <i class="fas fa-weight-hanging"></i> {{ filteredItems.length }} /
            50
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
                :src="
                  resolveItemImage(
                    getSafeItem(u).image || getSafeItem(u).imageUrl,
                  )
                "
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
                  :class="
                    'rarity-' +
                    (selectedItem.rarity ||
                      getSafeItem(selectedItem).rarity ||
                      'COMMON')
                  "
                >
                  <img
                    :src="
                      resolveItemImage(
                        getSafeItem(selectedItem).image ||
                          getSafeItem(selectedItem).imageUrl,
                      )
                    "
                    @error="
                      $event.target.src = resolveItemImage('r_gold_coin.png')
                    "
                  />
                </div>

                <div
                  v-if="selectedItem.enhanceLevel > 0"
                  class="card-level-badge"
                >
                  +{{ selectedItem.enhanceLevel }}
                </div>
                <div v-if="selectedItem.isMythic" class="card-mythic-badge">
                  M{{ selectedItem.mythicLevel }}
                </div>
              </div>

              <div class="card-title-block">
                <div class="card-rarity-label">
                  {{
                    translateRarity(
                      selectedItem.rarity || getSafeItem(selectedItem).rarity,
                    )
                  }}
                  {{ translateType(getSafeItem(selectedItem).type) }}
                </div>
                <h3
                  class="card-item-name"
                  :class="
                    'text-rarity-' +
                    (selectedItem.rarity ||
                      getSafeItem(selectedItem).rarity ||
                      'COMMON')
                  "
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
                <span class="sub-val atk">
                  +{{ calculateTotalStat(selectedItem, "ATK") }}
                </span>
              </div>

              <div class="sub-stat-row">
                <span class="sub-label">Phòng Thủ</span>
                <span class="sub-val def">
                  +{{ calculateTotalStat(selectedItem, "DEF") }}
                </span>
              </div>

              <div class="sub-stat-row">
                <span class="sub-label">Sinh Lực</span>
                <span class="sub-val hp">
                  +{{ calculateTotalStat(selectedItem, "HP") }}
                </span>
              </div>

              <div class="sub-stat-row">
                <span class="sub-label">Tốc Độ</span>
                <span class="sub-val speed">
                  +{{ calculateTotalStat(selectedItem, "SPEED") }}
                </span>
              </div>

              <div
                v-if="selectedItem.isVirtual"
                class="sub-stat-row"
                style="
                  margin-top: 10px;
                  border-top: 1px dashed #444;
                  padding-top: 5px;
                "
              >
                <span class="sub-label">Loại</span>
                <span class="sub-val resource">Tài Nguyên</span>
              </div>
            </div>

            <div class="card-footer-info">
              <div class="eq-score">
                <span class="score-label">Điểm Trang Bị</span>
                <span class="score-val">{{
                  calculateCombatPower(selectedItem) +
                  (getSafeItem(selectedItem).tier * 5 || 0)
                }}</span>
              </div>
            </div>

            <div class="card-actions">
              <button
                v-if="
                  getSafeItem(selectedItem).type === 'CONSUMABLE' &&
                  !selectedItem.isVirtual
                "
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
              <span class="money-highlight"
                >{{
                  mode === "NPC"
                    ? getSafeItem(sellItem).basePrice * 0.5 * qty
                    : price * qty
                }}
                <i class="fas fa-coins"></i
              ></span>
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

// --- HELPER FUNCTION: Null safety ---
const getSafeItem = (u) => {
  return u && u.item ? u.item : {};
};

const getLevelClass = (lv) => {
  if (lv >= 30) return "lvl-red";
  if (lv >= 15) return "lvl-purple";
  if (lv >= 10) return "lvl-gold";
  return "lvl-white";
};

// --- HÀM LẤY GIÁ TRỊ TỪ DB (QUAN TRỌNG) ---
// Hàm này check cả 2 kiểu tên: camelCase (JS) và snake_case (SQL Raw)
const getStatVal = (obj, ...keys) => {
  if (!obj) return 0;
  for (const key of keys) {
    if (obj[key] !== undefined && obj[key] !== null) return Number(obj[key]);
  }
  return 0;
};

// --- TÍNH TỔNG CHỈ SỐ (Logic cốt lõi) ---
const calculateTotalStat = (u, type) => {
  if (!u || !u.item) return 0;

  let total = 0;
  const itemBase = u.item; // Thông tin gốc (Base Stats)
  const subStats = u.subStats || []; // Đã parse trong Store

  // Lấy Main Stat từ UserItem (Do cường hóa)
  // Xử lý cả tên biến camelCase và snake_case từ DB
  const mainType = u.mainStatType || u.main_stat_type || "";
  const mainVal = Number(u.mainStatValue || u.main_stat_value || 0);

  // 1. CỘNG BASE STATS (Từ bảng ITEMS)
  if (type === "ATK") total += getStatVal(itemBase, "atkBonus", "atk_bonus");
  if (type === "DEF") total += getStatVal(itemBase, "defBonus", "def_bonus");
  if (type === "HP") total += getStatVal(itemBase, "hpBonus", "hp_bonus");
  if (type === "SPEED")
    total += getStatVal(itemBase, "speedBonus", "speed_bonus", "speed");

  // 2. CỘNG MAIN STAT (Từ bảng USER_ITEMS)
  if (mainType) {
    if (type === "ATK" && (mainType === "ATK_FLAT" || mainType === "STR"))
      total += mainVal;
    if (type === "DEF" && mainType === "DEF_FLAT") total += mainVal;
    if (type === "HP" && mainType === "HP_FLAT") total += mainVal;
    if (type === "SPEED" && mainType === "SPEED") total += mainVal;
  }

  // 3. CỘNG SUB STATS (Từ cột JSON sub_stats)
  if (Array.isArray(subStats)) {
    subStats.forEach((stat) => {
      // Chỉ cộng chỉ số thường (FLAT), bỏ qua % (is_percent)
      if (stat.is_percent) return;

      const code = stat.code;
      const val = Number(stat.value || 0);

      if (type === "ATK" && (code === "ATK_FLAT" || code === "STR"))
        total += val;
      if (type === "DEF" && code === "DEF_FLAT") total += val;
      if (type === "HP" && code === "HP_FLAT") total += val;
      if (type === "SPEED" && (code === "SPEED" || code === "DEX"))
        total += val;
    });
  }

  return Math.floor(total);
};

// --- TÍNH SỨC MẠNH CHIẾN ĐẤU (Số to ở giữa) ---
const calculateCombatPower = (u) => {
  // Nếu DB có lưu sẵn thì dùng
  const dbPower = getStatVal(u, "totalPower", "total_power");
  if (dbPower > 0) return dbPower;

  // Nếu không thì tự tính tổng
  const atk = calculateTotalStat(u, "ATK");
  const def = calculateTotalStat(u, "DEF");
  const hp = calculateTotalStat(u, "HP");
  const speed = calculateTotalStat(u, "SPEED");

  // Công thức giả định: Công + Thủ + Máu/10 + Tốc*2
  const cp = atk + def + Math.floor(hp / 10) + speed * 2;

  // Nếu item rác (stats = 0) thì lấy Tier * 10
  return cp > 0 ? cp : getSafeItem(u).tier * 10 || 10;
};

// --- LỌC ITEM & INJECT VÍ ---
const filteredItems = computed(() => {
  let items = inventoryStore.items ? [...inventoryStore.items] : [];

  if (authStore.wallet) {
    const { wood, stone, iron_ore, platinum } = authStore.wallet;
    // Sử dụng tên cột chuẩn từ DB mới
    if (wood > 0)
      items.push(createVirtualItem("v_wood", "Gỗ", "r_go.png", wood));
    if (stone > 0)
      items.push(createVirtualItem("v_stone", "Đá", "stone_1.png", stone));
    if (iron_ore > 0)
      items.push(
        createVirtualItem("v_iron", "Quặng Sắt", "iron_ore.png", iron_ore),
      );
    if (platinum > 0)
      items.push(
        createVirtualItem("v_plat", "Bạch Kim", "platinum.png", platinum),
      );
  }

  if (filter.value === "EQUIP")
    items = items.filter((i) => i.item && EQUIP_TYPES.includes(i.item.type));
  if (filter.value === "MAT")
    items = items.filter((i) => i.item && MATERIAL_TYPES.includes(i.item.type));

  return items;
});

const createVirtualItem = (id, name, img, qty) => ({
  userItemId: id,
  quantity: qty,
  isEquipped: false,
  isVirtual: true,
  item: {
    name,
    type: "MATERIAL",
    rarity: "COMMON",
    imageUrl: img,
    description: "Tài nguyên.",
    basePrice: 0,
  },
});

// --- MODAL LOGIC ---
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
  inventoryStore.fetchInventory();
  charStore.fetchCharacter();
  await authStore.fetchProfile();
});
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap");

.wuxia-dark-theme {
  background-color: #121212;
  min-height: 100vh;
  color: #e0e0e0;
  position: relative;
  overflow: hidden;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}
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
            <i class="fas fa-weight-hanging"></i> {{ filteredItems.length }} /
            50
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
                :src="
                  resolveItemImage(
                    getSafeItem(u).image || getSafeItem(u).imageUrl,
                  )
                "
                class="item-icon"
                alt="item"
                @error="
                  $event.target.src = resolveItemImage('r_stone_3.png')
                "
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
                  :class="
                    'rarity-' +
                    (selectedItem.rarity ||
                      getSafeItem(selectedItem).rarity ||
                      'COMMON')
                  "
                >
                  <img
                    :src="
                      resolveItemImage(
                        getSafeItem(selectedItem).image ||
                          getSafeItem(selectedItem).imageUrl,
                      )
                    "
                    @error="
                      $event.target.src = resolveItemImage('r_stone_3.png')
                    "
                  />
                </div>

                <div
                  v-if="selectedItem.enhanceLevel > 0"
                  class="card-level-badge"
                >
                  +{{ selectedItem.enhanceLevel }}
                </div>
                <div v-if="selectedItem.isMythic" class="card-mythic-badge">
                  M{{ selectedItem.mythicLevel }}
                </div>
              </div>

              <div class="card-title-block">
                <div class="card-rarity-label">
                  {{
                    translateRarity(
                      selectedItem.rarity || getSafeItem(selectedItem).rarity,
                    )
                  }}
                  {{ translateType(getSafeItem(selectedItem).type) }}
                </div>
                <h3
                  class="card-item-name"
                  :class="
                    'text-rarity-' +
                    (selectedItem.rarity ||
                      getSafeItem(selectedItem).rarity ||
                      'COMMON')
                  "
                >
                  {{ getSafeItem(selectedItem).name }}
                </h3>
              </div>
            </div>

            <div class="card-lore">
              "{{
                getSafeItem(selectedItem).description || "Vật phẩm bí ẩn."
              }}"
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
                <span class="sub-val atk">
                  +{{ calculateTotalStat(selectedItem, "ATK") }}
                </span>
              </div>

              <div class="sub-stat-row">
                <span class="sub-label">Phòng Thủ</span>
                <span class="sub-val def">
                  +{{ calculateTotalStat(selectedItem, "DEF") }}
                </span>
              </div>

              <div class="sub-stat-row">
                <span class="sub-label">Sinh Lực</span>
                <span class="sub-val hp">
                  +{{ calculateTotalStat(selectedItem, "HP") }}
                </span>
              </div>

              <div class="sub-stat-row">
                <span class="sub-label">Tốc Độ</span>
                <span class="sub-val speed">
                  +{{ calculateTotalStat(selectedItem, "SPEED") }}
                </span>
              </div>

              <div
                v-if="selectedItem.isVirtual"
                class="sub-stat-row"
                style="
                  margin-top: 10px;
                  border-top: 1px dashed #444;
                  padding-top: 5px;
                "
              >
                <span class="sub-label">Loại</span>
                <span class="sub-val resource">Tài Nguyên</span>
              </div>
            </div>

            <div class="card-footer-info">
              <div class="eq-score">
                <span class="score-label">Điểm Trang Bị</span>
                <span class="score-val">{{
                  calculateCombatPower(selectedItem) +
                  (getSafeItem(selectedItem).tier * 5 || 0)
                }}</span>
              </div>
            </div>

            <div class="card-actions">
              <button
                v-if="
                  getSafeItem(selectedItem).type === 'CONSUMABLE' &&
                  !selectedItem.isVirtual
                "
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
                  @click="
                    inventoryStore.unequipItem(selectedItem.userItemId)
                  "
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
              <input
                type="number"
                v-model.number="price"
                class="dark-input"
              />
            </div>
            <div class="trade-summary">
              Tổng thu:
              <span class="money-highlight"
                >{{
                  mode === "NPC"
                    ? getSafeItem(sellItem).basePrice * 0.5 * qty
                    : price * qty
                }}
                <i class="fas fa-coins"></i
              ></span>
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
import { ref, computed, onMounted, watch } from "vue";
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

const getSafeItem = (u) => (u && u.item ? u.item : {});
const getLevelClass = (lv) =>
  lv >= 30
    ? "lvl-red"
    : lv >= 15
    ? "lvl-purple"
    : lv >= 10
    ? "lvl-gold"
    : "lvl-white";

// --- STAT CALCULATORS ---
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
  if (type === "SPEED")
    total += getStatVal(itemBase, "speedBonus", "speed_bonus", "speed");

  if (mainType) {
    if (type === "ATK" && ["ATK_FLAT", "STR"].includes(mainType))
      total += mainVal;
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

// --- [FIX] LỌC ITEM & MAPPING HÌNH ẢNH ĐÚNG ---
const filteredItems = computed(() => {
  let items = inventoryStore.items ? [...inventoryStore.items] : [];

  if (authStore.wallet) {
    // [FIX] Lấy tất cả các loại tài nguyên từ wallet
    const { wood, stone, ironOre, iron_ore, platinum, fish, gold } = authStore.wallet;
    
    // Support cả 2 kiểu đặt tên biến (camelCase và snake_case)
    const ironVal = ironOre || iron_ore || 0;

    if (wood > 0)
      items.push(createVirtualItem("v_wood", "Gỗ", "r_wood.png", wood));
    
    if (stone > 0)
      items.push(createVirtualItem("v_stone", "Đá", "stone_1.png", stone));
    
    if (ironVal > 0)
      items.push(createVirtualItem("v_iron", "Quặng Sắt", "r_iron_ore.png", ironVal));
    
    if (platinum > 0)
      items.push(createVirtualItem("v_plat", "Bạch Kim", "r_platinum.png", platinum));

    // [MỚI] Thêm Cá
    if (fish > 0)
       items.push(createVirtualItem("v_fish", "Cá", "r_fish.png", fish));
       
    // [OPTIONAL] Thêm Vàng nếu muốn hiện trong túi
    // if (gold > 0)
    //   items.push(createVirtualItem("v_gold", "Ngân Lượng", "r_coin.png", gold));
  }

  if (filter.value === "EQUIP")
    items = items.filter((i) => i.item && EQUIP_TYPES.includes(i.item.type));
  if (filter.value === "MAT")
    items = items.filter(
      (i) => i.item && MATERIAL_TYPES.includes(i.item.type)
    );

  return items;
});

const createVirtualItem = (id, name, img, qty) => ({
  userItemId: id,
  quantity: qty,
  isEquipped: false,
  isVirtual: true,
  item: {
    name,
    type: "MATERIAL",
    rarity: "COMMON",
    imageUrl: img, // assetHelper sẽ tự map sang link GitHub
    description: "Tài nguyên khai thác được.",
    basePrice: 0,
  },
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
  // Refresh lại ví sau khi bán
  await authStore.fetchProfile();
};
const confirmP2P = async () => {
  await marketStore.createListing(
    sellItem.value.userItemId,
    price.value,
    qty.value
  );
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
  // Gọi đồng thời để tối ưu tốc độ
  await Promise.all([
    inventoryStore.fetchInventory(),
    charStore.fetchCharacter(),
    authStore.fetchProfile() // [QUAN TRỌNG] Lấy lại ví để cập nhật số lượng
  ]);
});
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap");

.wuxia-dark-theme {
  background-color: #121212;
  min-height: 100vh;
  color: #e0e0e0;
  position: relative;
  overflow: hidden;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}
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
                :src="getItemImage(getSafeItem(u).image || getSafeItem(u).imageUrl)"
                class="item-icon"
                alt="item"
                @error="$event.target.src = getItemImage('o_coalOre.png')"
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
                    :src="getItemImage(getSafeItem(selectedItem).image || getSafeItem(selectedItem).imageUrl)"
                    @error="$event.target.src = getItemImage('o_coalOre.png')"
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
    // [FIX] Truyền đúng tên file (không cần .png, helper sẽ lo) hoặc có .png cũng ok vì helper có clean
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

    // [CẬP NHẬT] Tên file ảnh chính xác theo CSV của bạn
    if (woodVal > 0) items.push(createVirtualItem("v_wood", "Gỗ Sồi", "w_wood", woodVal));
    if (stoneVal > 0) items.push(createVirtualItem("v_stone", "Đá", "o_coalOre", stoneVal)); // Dùng o_coalOre vì không thấy file stone
    if (ironVal > 0) items.push(createVirtualItem("v_iron", "Quặng Sắt", "o_ironOre", ironVal));
    if (platinumVal > 0) items.push(createVirtualItem("v_plat", "Bạch Kim", "o_platinumOre", platinumVal));
    if (fishVal > 0) items.push(createVirtualItem("v_fish", "Cá", "r_fish", fishVal));
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

.wuxia-dark-theme {
  background-color: #121212;
  min-height: 100vh;
  color: #e0e0e0;
  position: relative;
  overflow: hidden;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}
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
                :src="getItemImage(getSafeItem(u).image || getSafeItem(u).imageUrl)"
                class="item-icon"
                alt="item"
                @error="$event.target.src = getItemImage('o_coalOre')"
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
                    :src="getItemImage(getSafeItem(selectedItem).image || getSafeItem(selectedItem).imageUrl)"
                    @error="$event.target.src = getItemImage('o_coalOre')"
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
    // [FIX] Chỉ truyền tên gốc (ví dụ: w_wood), không truyền .png
    // assetHelper sẽ tự xử lý thêm đuôi file.
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

    // [FIX] Sửa lại tên resource cho khớp với file ảnh thực tế trên GitHub Pages
    if (woodVal > 0) items.push(createVirtualItem("v_wood", "Gỗ Sồi", "w_wood", woodVal));
    if (stoneVal > 0) items.push(createVirtualItem("v_stone", "Đá", "o_coal", stoneVal)); // Dùng o_coal thay vì o_coalOre
    if (ironVal > 0) items.push(createVirtualItem("v_iron", "Quặng Sắt", "o_iron", ironVal));
    if (platinumVal > 0) items.push(createVirtualItem("v_plat", "Bạch Kim", "o_platinum", platinumVal));
    if (fishVal > 0) items.push(createVirtualItem("v_fish", "Cá", "f_fish", fishVal));
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

.wuxia-dark-theme {
  background-color: #121212;
  min-height: 100vh;
  color: #e0e0e0;
  position: relative;
  overflow: hidden;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}
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