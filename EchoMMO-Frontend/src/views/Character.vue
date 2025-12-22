<template>
  <div class="page-container character-page wuxia-dark-theme">
    <div class="bg-layer">
      <div
        class="mountain-bg"
        :style="{ backgroundImage: `url(${bgImage})` }"
      ></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

    <div class="char-wrapper">
      <div class="char-grid">
        <div class="panel stats-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>CĂN CỐT</h3>
            <div class="decor-line"></div>
          </div>

          <div class="stats-body custom-scroll">
            <div class="stat-row level-row">
              <span class="label">Cảnh Giới</span>
              <span class="value highlight"
                >Cấp {{ charStore.character?.level || 1 }}</span
              >
            </div>

            <div
              class="potential-box"
              :class="{ 'has-points': availablePoints > 0 }"
            >
              <div class="pot-label">Điểm Tiềm Năng</div>
              <div class="pot-value">{{ availablePoints }}</div>
            </div>

            <div class="divider"></div>

            <div
              class="base-stat-row"
              v-for="(cfg, key) in statConfigs"
              :key="key"
            >
              <span class="stat-name" :class="cfg.colorClass">
                <i :class="['fas', cfg.icon]"></i> {{ cfg.label }}
              </span>
              <div class="stat-ctrl">
                <span class="base-val">{{ getBaseStat(key) }}</span>
                <span v-if="pendingStats[key] > 0" class="added-val"
                  >+{{ pendingStats[key] }}</span
                >
                <button
                  v-if="availablePoints > 0"
                  @click="increaseStat(key)"
                  class="btn-plus"
                >
                  <i class="fas fa-plus"></i>
                </button>
                <button
                  v-if="pendingStats[key] > 0"
                  @click="decreaseStat(key)"
                  class="btn-minus"
                >
                  <i class="fas fa-minus"></i>
                </button>
              </div>
            </div>

            <div v-if="isPendingChanges" class="save-stats-area">
              <button
                @click="confirmAddStats"
                class="btn-wood confirm full-width"
              >
                <i class="fas fa-check"></i> LƯU THAY ĐỔI
              </button>
              <button
                @click="resetPending"
                class="btn-wood cancel full-width"
                style="margin-top: 5px"
              >
                HỦY
              </button>
            </div>
          </div>
        </div>

        <div class="panel hero-panel">
          <div class="aura-bg"></div>

          <div class="equip-column left-col">
            <div
              v-for="key in ['NECKLACE', 'WEAPON', 'RING']"
              :key="key"
              class="equip-slot"
              :class="{
                filled: equipment[key],
                'target-glow': hoveredType === key,
                'broken-item':
                  equipment[key] && getDurabilityPercent(equipment[key]) <= 0,
              }"
              @mousedown.left="unequipSlow(key)"
              :title="equipment[key] ? getItemTooltip(equipment[key]) : SLOT_CONFIG[key].label"
            >
              <div class="slot-frame">
                <img
                  v-if="equipment[key]"
                  :src="resolveItemImage(equipment[key].item.imageUrl)"
                />
                <i
                  v-else
                  :class="['fas placeholder', SLOT_CONFIG[key].icon]"
                ></i>

                <div
                  v-if="
                    equipment[key] &&
                    (equipment[key].enhanceLevel || equipment[key].level)
                  "
                  class="slot-level-tag"
                  :class="
                    getLevelClass(
                      equipment[key].enhanceLevel || equipment[key].level
                    )
                  "
                >
                  +{{ equipment[key].enhanceLevel || equipment[key].level }}
                </div>
              </div>
              <div class="slot-label-small">{{ SLOT_CONFIG[key].label }}</div>
            </div>
          </div>

          <div class="char-center-col">
            <div class="char-figure">
              <img :src="userSkinImg" class="skin-preview" />
            </div>

            <div class="tool-belt">
              <div class="tool-belt-title">BỘ CÔNG CỤ</div>
              <div class="tool-row">
                <div
                  v-for="key in ['PICKAXE', 'AXE', 'SHOVEL', 'FISHING_ROD']"
                  :key="key"
                  class="mini-tool-slot"
                  :class="{
                    filled: equipment[key],
                    'target-glow': hoveredType === key,
                    broken:
                      equipment[key] &&
                      getDurabilityPercent(equipment[key]) <= 0,
                  }"
                  @mousedown.left="unequipSlow(key)"
                  :title="equipment[key] ? getItemTooltip(equipment[key]) : SLOT_CONFIG[key].label"
                >
                  <img
                    v-if="equipment[key]"
                    :src="resolveItemImage(equipment[key].item.imageUrl)"
                  />
                  <i v-else :class="['fas', SLOT_CONFIG[key].icon]"></i>

                  <div v-if="equipment[key]" class="tool-durability-bar">
                    <div
                      class="tool-dur-fill"
                      :style="{
                        width: getDurabilityPercent(equipment[key]) + '%',
                      }"
                      :class="getDurabilityColor(equipment[key])"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="equip-column right-col">
            <div
              v-for="key in ['HELMET', 'ARMOR', 'BOOTS']"
              :key="key"
              class="equip-slot"
              :class="{
                filled: equipment[key],
                'target-glow': hoveredType === key,
                'broken-item':
                  equipment[key] && getDurabilityPercent(equipment[key]) <= 0,
              }"
              @mousedown.left="unequipSlow(key)"
              :title="equipment[key] ? getItemTooltip(equipment[key]) : SLOT_CONFIG[key].label"
            >
              <div class="slot-frame">
                <img
                  v-if="equipment[key]"
                  :src="resolveItemImage(equipment[key].item.imageUrl)"
                />
                <i
                  v-else
                  :class="['fas placeholder', SLOT_CONFIG[key].icon]"
                ></i>

                <div
                  v-if="
                    equipment[key] &&
                    (equipment[key].enhanceLevel || equipment[key].level)
                  "
                  class="slot-level-tag"
                  :class="
                    getLevelClass(
                      equipment[key].enhanceLevel || equipment[key].level
                    )
                  "
                >
                  +{{ equipment[key].enhanceLevel || equipment[key].level }}
                </div>
              </div>
              <div class="slot-label-small">{{ SLOT_CONFIG[key].label }}</div>
            </div>
          </div>
        </div>

        <div class="panel bag-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>HÀNH NANG</h3>
            <div class="decor-line"></div>
          </div>

          <div class="combat-stats-box">
            <div class="c-stat">
              <span class="c-label atk"
                ><i class="fas fa-fist-raised"></i> Công</span
              >
              <span class="c-val">{{ totalStats.atk }}</span>
            </div>
            <div class="c-stat">
              <span class="c-label def"
                ><i class="fas fa-shield-alt"></i> Thủ</span
              >
              <span class="c-val">{{ totalStats.def }}</span>
            </div>
            <div class="c-stat">
              <span class="c-label speed"
                ><i class="fas fa-wind"></i> Tốc</span
              >
              <span class="c-val">{{ totalStats.speed }}</span>
            </div>
            <div class="c-stat">
              <span class="c-label hp"
                ><i class="fas fa-heart"></i> Máu</span
              >
              <span class="c-val">{{ totalStats.hp }}</span>
            </div>
            <div class="c-stat">
              <span class="c-label crit"
                ><i class="fas fa-bolt"></i> Bạo</span
              >
              <span class="c-val">{{ totalStats.crit }}%</span>
            </div>
          </div>

          <div class="bag-info">Vật phẩm: {{ bagItems.length }} / 50</div>

          <div class="mini-grid custom-scroll">
            <div
              v-for="item in bagItems"
              :key="item.userItemId"
              class="mini-slot"
              :class="[
                'rarity-' + (item.item.rarity || 'C'),
                { 'is-equipped': item.isEquipped },
              ]"
              @click="equip(item)"
              @mouseenter="hoveredType = determineSlot(item.item)"
              @mouseleave="hoveredType = null"
              :title="getItemTooltip(item)"
            >
              <img :src="resolveItemImage(item.item.imageUrl)" />

              <div
                v-if="item.enhanceLevel || item.level"
                class="mini-level"
                :class="getLevelClass(item.enhanceLevel || item.level)"
              >
                +{{ item.enhanceLevel || item.level }}
              </div>

              <span class="qty" v-if="item.quantity > 1">{{
                item.quantity
              }}</span>
            </div>

            <div
              v-for="n in Math.max(0, 20 - bagItems.length)"
              :key="'e' + n"
              class="mini-slot empty"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div
        v-if="showUnequipModal"
        class="modal-overlay"
        @click.self="closeModal"
      >
        <div class="dark-modal">
          <div class="modal-content">
            <h3 class="modal-title">GỠ BỎ VẬT PHẨM</h3>
            <div class="item-preview-box">
              <img
                v-if="itemToUnequip"
                :src="resolveItemImage(itemToUnequip.item.imageUrl)"
                class="preview-img"
              />
              <div class="preview-info" v-if="itemToUnequip">
                <span class="p-name">
                  {{ itemToUnequip.item.name }}
                </span>
                <span class="p-type">Sẽ trở về hành trang</span>
              </div>
            </div>
            <div class="modal-actions">
              <button class="btn-wood cancel" @click="closeModal">
                HỦY BỎ
              </button>
              <button class="btn-wood confirm" @click="confirmUnequip">
                XÁC NHẬN
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, reactive } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useInventoryStore } from "@/stores/inventoryStore";
import { useAuthStore } from "@/stores/authStore";
import { resolveItemImage, getCurrentSkin } from "@/utils/assetHelper";

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore();
const authStore = useAuthStore();

const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const statConfigs = {
  str: { label: "Sức Mạnh", icon: "fa-dumbbell", colorClass: "text-red" },
  vit: { label: "Thể Lực", icon: "fa-heartbeat", colorClass: "text-green" },
  agi: { label: "Thân Pháp", icon: "fa-wind", colorClass: "text-blue" },
  dex: { label: "Khéo Léo", icon: "fa-crosshairs", colorClass: "text-yellow" },
  int: { label: "Trí Tuệ", icon: "fa-brain", colorClass: "text-purple" },
  luck: { label: "Vận Khí", icon: "fa-star", colorClass: "text-gold" },
};

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- HELPER: LẤY GIÁ TRỊ AN TOÀN ---
const safeVal = (obj, ...keys) => {
  if (!obj) return 0;
  for (const k of keys) {
    if (obj[k] !== undefined && obj[k] !== null) return Number(obj[k]);
  }
  return 0;
};

// --- LOGIC STATS (CỘNG ĐIỂM) ---
const pendingStats = reactive({
  str: 0, vit: 0, agi: 0, dex: 0, int: 0, luck: 0,
});

const getBaseStat = (key) => {
  const char = charStore.character || {};
  if (key === "int") return safeVal(char, 'intelligence', 'int_stat', 'int');
  return safeVal(char, key);
};

const availablePoints = computed(() => {
  const currentPoints = safeVal(charStore.character, 'statPoints', 'stat_points');
  const used = Object.values(pendingStats).reduce((a, b) => a + b, 0);
  return Math.max(0, currentPoints - used);
});

const isPendingChanges = computed(() => Object.values(pendingStats).some((v) => v > 0));

const increaseStat = (key) => {
  if (availablePoints.value > 0) pendingStats[key]++;
};
const decreaseStat = (key) => {
  if (pendingStats[key] > 0) pendingStats[key]--;
};
const resetPending = () => {
  Object.keys(pendingStats).forEach((k) => (pendingStats[k] = 0));
};

const confirmAddStats = async () => {
  const payload = { ...pendingStats };
  if (payload.int) {
    payload.intelligence = payload.int;
    delete payload.int;
  }
  const success = await charStore.addStats(payload);
  if (success) resetPending();
};

// --- CONFIG SLOT ---
const SLOT_CONFIG = {
  NECKLACE: { label: "Dây Chuyền", icon: "fa-gem" },
  WEAPON: { label: "Vũ Khí", icon: "fa-gavel" },
  RING: { label: "Nhẫn", icon: "fa-ring" },
  HELMET: { label: "Mũ", icon: "fa-hat-cowboy-side" },
  ARMOR: { label: "Y Phục", icon: "fa-tshirt" },
  BOOTS: { label: "Giày", icon: "fa-socks" },
  PICKAXE: { label: "Cúp", icon: "fa-hammer" },
  AXE: { label: "Rìu", icon: "fa-axe" },
  SHOVEL: { label: "Xẻng", icon: "fa-trowel" },
  FISHING_ROD: { label: "Cần Câu", icon: "fa-fish" },
};

const determineSlot = (item) => {
  if (!item) return null;
  const typeStr = item.slotType || item.slot_type || "";
  const dbSlot = typeStr.toUpperCase();
  if (SLOT_CONFIG[dbSlot]) return dbSlot;
  return null;
};

// --- COMPUTED: EQUIPMENT & BAG ---
const equipment = computed(() => {
  const mapped = {};
  const allItems = inventoryStore.items || [];
  allItems.forEach((userItem) => {
    if (userItem && userItem.item && userItem.isEquipped) {
      const slot = determineSlot(userItem.item);
      if (slot) mapped[slot] = userItem;
    }
  });
  return mapped;
});

const bagItems = computed(() => {
  const items = inventoryStore.items || [];
  return items.filter((ui) => {
    if (!ui || !ui.item || ui.isEquipped) return false;
    const type = (ui.item.type || "").toUpperCase();
    if (type === "MATERIAL" || type === "RESOURCE") return false;
    return determineSlot(ui.item) !== null;
  });
});

const userSkinImg = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return skin ? skin.sprites.idle : "";
});

const hoveredType = ref(null);

// --- TOOLTIP ---
const getItemTooltip = (ui) => {
  if (!ui || !ui.item) return "";
  let text = `${ui.item.name} (+${ui.enhanceLevel || 0})\n`;
  const curDur = safeVal(ui, 'currentDurability', 'current_durability');
  const maxDur = safeVal(ui, 'maxDurability', 'max_durability') || 100;
  text += `Độ bền: ${curDur}/${maxDur}\n`;

  const type = (ui.mainStatType || "").toUpperCase();
  const val = safeVal(ui, 'mainStatValue', 'main_stat_value');

  let stats = [];
  if (val > 0) {
     if (type.includes("ATK")) stats.push(`Công: +${val}`);
     else if (type.includes("DEF")) stats.push(`Thủ: +${val}`);
     else if (type.includes("HP")) stats.push(`Máu: +${val}`);
     else if (type.includes("SPEED")) stats.push(`Tốc: +${val}`);
     else if (type.includes("CRIT")) stats.push(`Bạo: +${val}%`);
     else stats.push(`${type}: +${val}`);
  }
  
  const baseAtk = safeVal(ui.item, 'atkBonus', 'atk_bonus');
  const baseDef = safeVal(ui.item, 'defBonus', 'def_bonus');
  const baseHp = safeVal(ui.item, 'hpBonus', 'hp_bonus');
  const baseSpeed = safeVal(ui.item, 'speedBonus', 'speed_bonus');

  if (!type.includes("ATK") && baseAtk > 0) stats.push(`Công (Gốc): +${baseAtk}`);
  if (!type.includes("DEF") && baseDef > 0) stats.push(`Thủ (Gốc): +${baseDef}`);
  if (!type.includes("HP") && baseHp > 0) stats.push(`Máu (Gốc): +${baseHp}`);
  if (!type.includes("SPEED") && baseSpeed > 0) stats.push(`Tốc (Gốc): +${baseSpeed}`);

  return text + stats.join("\n");
};

// --- [CORE FIX] TÍNH TỔNG CHỈ SỐ (BAO GỒM CẢ ĐIỂM PENDING) ---
const totalStats = computed(() => {
  const char = charStore.character || {};

  // 1. Chỉ số cơ bản (Attribute) = Gốc + Điểm cộng thêm (Pending)
  let totalAttr = {
    str: safeVal(char, 'str') + pendingStats.str,
    vit: safeVal(char, 'vit') + pendingStats.vit,
    agi: safeVal(char, 'agi') + pendingStats.agi,
    dex: safeVal(char, 'dex') + pendingStats.dex,
    int: safeVal(char, 'intelligence', 'int', 'int_stat') + pendingStats.int,
    luck: safeVal(char, 'luck') + pendingStats.luck,
  };

  // 2. Chỉ số chiến đấu nền (Tính từ Attribute tổng)
  // Công thức này sẽ tự động cập nhật khi bạn bấm nút (+)
  let combat = {
    atk: 5 + totalAttr.str * 2,
    def: 2 + Math.floor(totalAttr.vit / 3),
    hp: 100 + totalAttr.vit * 15,
    speed: 10 + totalAttr.agi,
    crit: 1 + totalAttr.luck / 10,
  };

  // 3. Cộng dồn từ trang bị
  Object.values(equipment.value).forEach((ui) => {
    if (!ui || !ui.item) return;

    const curDur = safeVal(ui, 'currentDurability', 'current_durability');
    if (curDur <= 0 && (ui.maxDurability || ui.max_durability)) return;

    const type = (ui.mainStatType || "").toUpperCase();
    const val = safeVal(ui, 'mainStatValue', 'main_stat_value');

    // A. Main Stat
    if (val > 0) {
      if (type.includes("ATK")) combat.atk += val;
      else if (type.includes("DEF")) combat.def += val;
      else if (type.includes("HP")) combat.hp += val;
      else if (type.includes("SPEED")) combat.speed += val;
      else if (type.includes("CRIT")) combat.crit += val;
      else if (type === "ATK_PERCENT") combat.atk += (5 + totalAttr.str * 2) * (val / 100);
      else if (type === "HP_PERCENT") combat.hp += (100 + totalAttr.vit * 15) * (val / 100);
    }

    // B. Base Stat Fallback
    const baseAtk = safeVal(ui.item, 'atkBonus', 'atk_bonus');
    const baseDef = safeVal(ui.item, 'defBonus', 'def_bonus');
    const baseHp = safeVal(ui.item, 'hpBonus', 'hp_bonus');
    const baseSpeed = safeVal(ui.item, 'speedBonus', 'speed_bonus');

    if (!type.includes("ATK")) combat.atk += baseAtk;
    if (!type.includes("DEF")) combat.def += baseDef;
    if (!type.includes("HP")) combat.hp += baseHp;
    if (!type.includes("SPEED")) combat.speed += baseSpeed;

    // C. Substats
    if (ui.subStats) {
      let subs = [];
      try { subs = typeof ui.subStats === "string" ? JSON.parse(ui.subStats) : ui.subStats; } catch (e) {}
      if (Array.isArray(subs)) {
        subs.forEach((stat) => {
          const sType = (stat.code || stat.type || "").toUpperCase();
          const sVal = Number(stat.value || 0);
          if (sType.includes("ATK")) combat.atk += sVal;
          else if (sType.includes("DEF")) combat.def += sVal;
          else if (sType.includes("HP")) combat.hp += sVal;
          else if (sType.includes("SPEED")) combat.speed += sVal;
          else if (sType.includes("CRIT")) combat.crit += sVal;
        });
      }
    }
  });

  return {
    atk: Math.floor(combat.atk),
    def: Math.floor(combat.def),
    speed: Math.floor(combat.speed),
    crit: parseFloat(combat.crit.toFixed(2)),
    hp: Math.floor(combat.hp),
    attributes: totalAttr,
  };
});

const getLevelClass = (lv) => {
  if (!lv) return "";
  if (lv >= 15) return "lvl-red";
  if (lv >= 10) return "lvl-purple";
  if (lv >= 5) return "lvl-gold";
  return "lvl-white";
};

const getDurabilityPercent = (item) => {
  const max = safeVal(item, 'maxDurability', 'max_durability') || 100;
  const cur = safeVal(item, 'currentDurability', 'current_durability');
  if (item.currentDurability === undefined && item.current_durability === undefined) return 100; 
  return Math.max(0, (cur / max) * 100);
};

const getDurabilityColor = (item) => {
  const pct = getDurabilityPercent(item);
  if (pct <= 0) return "bg-red-600";
  if (pct < 30) return "bg-orange-500";
  return "bg-green-500";
};

// --- ACTIONS ---
const equip = async (userItem) => {
  if (!userItem || !userItem.item) return;
  const slot = determineSlot(userItem.item);
  if (slot) {
    try {
      await inventoryStore.equipItem(userItem.userItemId);
      await Promise.all([
          inventoryStore.fetchInventory(), 
          charStore.fetchCharacter()
      ]);
    } catch (e) {
      console.error("Lỗi Equip:", e);
    }
  } else {
    alert(`Vật phẩm lỗi loại: ${userItem.item.slotType}`);
  }
};

const showUnequipModal = ref(false);
const itemToUnequip = ref(null);

const unequipSlow = (slotName) => {
  const item = equipment.value[slotName];
  if (item) {
    itemToUnequip.value = item;
    showUnequipModal.value = true;
  }
};

const confirmUnequip = async () => {
  if (itemToUnequip.value) {
    try {
      await inventoryStore.unequipItem(itemToUnequip.value.userItemId);
      await Promise.all([inventoryStore.fetchInventory(), charStore.fetchCharacter()]);
    } catch (e) {
      console.error("Lỗi Unequip:", e);
    }
  }
  closeModal();
};

const closeModal = () => {
  showUnequipModal.value = false;
  itemToUnequip.value = null;
};

onMounted(async () => {
  updateDayNight();
  await Promise.all([charStore.fetchCharacter(), inventoryStore.fetchInventory()]);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --wood-dark: #3e2723;
  --wood-mid: #4e342e;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --red-seal: #b71c1c;
  --bonus-green: #66bb6a;
}

.wuxia-dark-theme {
  background-color: transparent;
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

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
  background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9));
  mix-blend-mode: multiply;
  transition: background 2s ease;
}

.wood-overlay.night-mode {
  background: linear-gradient(to bottom, rgba(10, 5, 20, 0.85), rgba(0, 0, 0, 0.95));
}

.vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, transparent 60%, #1a100d 100%);
}

.char-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 1200px;
  padding: 20px;
}

.char-grid {
  display: grid;
  grid-template-columns: 320px 1fr 300px;
  gap: 20px;
  height: 600px;
}

.panel {
  background: rgba(30, 20, 15, 0.9);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}

.panel-header {
  padding: 12px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 2px solid var(--wood-light);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.panel-header h3 {
  margin: 0;
  color: var(--gold);
  font-size: 1.1rem;
  font-weight: 900;
  letter-spacing: 1px;
}

.decor-line {
  height: 2px;
  width: 30px;
  background: var(--gold);
  opacity: 0.5;
}

.stats-body {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
}

.level-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 10px;
}

.highlight {
  color: var(--gold);
  font-weight: bold;
  text-shadow: 0 0 10px orange;
}

.potential-box {
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid #5d4037;
  border-radius: 6px;
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: 0.3s;
}

.potential-box.has-points {
  border-color: var(--bonus-green);
  box-shadow: 0 0 10px rgba(102, 187, 106, 0.2);
}

.pot-label {
  color: #ccc;
  font-size: 0.9rem;
}

.pot-value {
  color: var(--bonus-green);
  font-size: 1.2rem;
  font-weight: bold;
}

.divider {
  height: 1px;
  background: linear-gradient(to right, transparent, #5d4037, transparent);
  margin: 5px 0;
}

.base-stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.05);
}

.stat-name {
  font-weight: bold;
  font-size: 0.95rem;
  width: 110px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-ctrl {
  display: flex;
  align-items: center;
  gap: 8px;
}

.base-val {
  font-size: 1.1rem;
  font-weight: bold;
  color: white;
}

.added-val {
  color: var(--bonus-green);
  font-size: 0.9rem;
  font-weight: bold;
  animation: popIn 0.3s;
}

.btn-plus, .btn-minus {
  width: 24px;
  height: 24px;
  border: 1px solid #5d4037;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
}

.btn-plus { background: #1b5e20; color: #a5d6a7; }
.btn-plus:hover { background: #2e7d32; }
.btn-minus { background: #b71c1c; color: #ffcdd2; }
.btn-minus:hover { background: #c62828; }

.text-red { color: #ef5350; }
.text-green { color: #66bb6a; }
.text-blue { color: #42a5f5; }
.text-yellow { color: #ffee58; }
.text-purple { color: #ab47bc; }
.text-gold { color: #ffca28; }

.save-stats-area { margin-top: 10px; animation: slideUp 0.3s; }
.full-width { width: 100%; }

.combat-stats-box {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  background: rgba(0, 0, 0, 0.3);
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 10px;
}

.c-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  padding: 5px;
  border-radius: 4px;
}

.c-label { font-size: 0.8rem; opacity: 0.8; margin-bottom: 2px; }
.c-val { font-weight: bold; font-size: 1rem; }
.c-label.atk { color: #ef5350; }
.c-label.def { color: #42a5f5; }
.c-label.hp { color: #e53935; }
.c-label.speed { color: #66bb6a; }
.c-label.crit { color: #ab47bc; }

.hero-panel {
  background: radial-gradient(circle at center, rgba(78, 52, 46, 0.8) 0%, rgba(38, 24, 21, 0.9) 100%);
  border-color: var(--gold);
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.equip-column {
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  height: 100%;
  z-index: 5;
  gap: 20px;
}

.char-center-col {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.char-figure { z-index: 1; flex: 1; display: flex; align-items: center; }
.skin-preview {
  height: 250px;
  width: 250px;
  object-fit: contain;
  image-rendering: pixelated;
  transform: scale(1.5);
  filter: drop-shadow(0 10px 10px rgba(0, 0, 0, 0.8));
}

.aura-bg {
  position: absolute;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 236, 179, 0.1), transparent 70%);
  animation: pulseAura 4s infinite;
  top: 50%; left: 50%; transform: translate(-50%, -50%);
  z-index: 0;
}

/* TOOL BELT */
.tool-belt {
  margin-bottom: 10px;
  z-index: 10;
  background: rgba(0, 0, 0, 0.5);
  padding: 8px;
  border-radius: 12px;
  border: 1px solid #5d4037;
}

.tool-belt-title {
  font-size: 9px;
  color: #a1887f;
  text-align: center;
  margin-bottom: 4px;
  letter-spacing: 1px;
}

.tool-row { display: flex; gap: 8px; justify-content: center; }

.mini-tool-slot {
  width: 36px; height: 36px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #8d6e63;
  border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: 0.2s;
  position: relative;
}

.mini-tool-slot:hover { transform: scale(1.1); border-color: var(--gold); }
.mini-tool-slot.filled { border-color: var(--gold); background: rgba(255, 236, 179, 0.1); }
.mini-tool-slot.broken, .equip-slot.broken-item .slot-frame {
  border-color: #d32f2f;
  box-shadow: 0 0 5px #d32f2f;
  background: rgba(211, 47, 47, 0.1);
  filter: grayscale(100%);
}
.equip-slot.broken-item .slot-level-tag { background: #d32f2f; }
.mini-tool-slot img { width: 80%; height: 80%; object-fit: contain; }
.mini-tool-slot i { color: #5d4037; font-size: 1rem; }
.tool-durability-bar { position: absolute; bottom: 2px; left: 2px; right: 2px; height: 3px; background: #333; border-radius: 1px; overflow: hidden; }
.tool-dur-fill { height: 100%; transition: width 0.3s; }
.bg-red-600 { background: #d32f2f; }
.bg-orange-500 { background: #f57c00; }
.bg-green-500 { background: #388e3c; }

/* EQUIP SLOTS */
.equip-slot { position: relative; display: flex; flex-direction: column; align-items: center; cursor: pointer; transition: 0.2s; }
.equip-slot:hover { transform: scale(1.1); z-index: 10; }
.slot-frame {
  width: 55px; height: 55px;
  background: rgba(0, 0, 0, 0.6);
  border: 2px solid #8d6e63;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.8);
  position: relative;
}
.equip-slot.filled .slot-frame {
  border-color: var(--gold);
  background: rgba(255, 236, 179, 0.05);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);
}
.slot-frame img { width: 90%; height: 90%; object-fit: contain; }
.placeholder { font-size: 1.5rem; color: #5d4037; opacity: 0.5; }
.slot-label-small {
  font-size: 9px; color: #aaa; margin-top: 4px;
  text-transform: uppercase; text-shadow: 1px 1px 1px #000;
}
.slot-level-tag {
  position: absolute; bottom: -5px; right: -5px;
  font-size: 9px; font-weight: 900;
  background: #000; border: 1px solid #444; padding: 0 3px; border-radius: 4px;
}

/* BAG */
.bag-info { text-align: right; padding: 5px; color: #a1887f; font-size: 0.8rem; }
.mini-grid {
  padding: 5px; display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 8px; overflow-y: auto; max-height: 400px;
}
.mini-slot {
  height: 50px; background: rgba(0, 0, 0, 0.4);
  border: 1px solid #4e342e; border-radius: 4px;
  position: relative; cursor: pointer;
}
.mini-slot img { width: 100%; height: 100%; padding: 4px; object-fit: contain; }
.mini-slot.empty { opacity: 0.15; border-style: dashed; }
.qty {
  position: absolute; bottom: 1px; right: 1px;
  background: rgba(0, 0, 0, 0.8); color: white;
  font-size: 9px; padding: 0 3px; border-radius: 2px;
}

/* MODAL & BTNS */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex; justify-content: center; align-items: center; z-index: 1000;
}
.dark-modal {
  width: 300px; background: var(--wood-dark);
  border: 1px solid #5d4037; box-shadow: 0 0 30px #000;
}
.modal-content { padding: 20px; text-align: center; }
.item-preview-box {
  background: rgba(0, 0, 0, 0.3); padding: 10px; margin: 15px 0;
  display: flex; align-items: center; gap: 10px;
}
.preview-img { width: 50px; height: 50px; border: 1px solid var(--gold); }
.modal-actions { display: flex; gap: 10px; }
.btn-wood { flex: 1; padding: 10px; cursor: pointer; font-weight: bold; border-radius: 4px; border: none; color: #ccc; transition: 0.2s; }
.cancel { background: #4e342e; }
.cancel:hover { background: #5d4037; }
.confirm { background: var(--red-seal); color: white; }
.confirm:hover { background: #d32f2f; }

@keyframes pulseAura {
  0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(0.9); }
  50% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.1); }
}
@keyframes popIn {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}
@keyframes slideUp {
  0% { opacity: 0; transform: translateY(10px); }
  100% { opacity: 1; transform: translateY(0); }
}

@media (max-width: 900px) {
  .char-grid { grid-template-columns: 1fr; height: auto; gap: 20px; }
  .hero-panel { height: 450px; order: -1; }
}
</style>