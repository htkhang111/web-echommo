<!-- <template>
  <div class="page-container character-page wuxia-dark-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

    <div class="char-wrapper">
      <div class="char-grid">
        
        <div class="panel stats-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>BẢN SẮC</h3>
            <div class="decor-line"></div>
          </div>

          <div class="stats-body">
            <div class="stat-row level-row">
              <span class="label">CẢNH GIỚI</span>
              <span class="value highlight">Luyện Khí Tầng {{ charStore.character?.lv || 1 }}</span>
            </div>
            <div class="divider"></div>
            
            <div class="stat-row">
              <span class="label"><i class="fas fa-fist-raised"></i> Công Lực</span>
              <span class="value atk">
                {{ totalStats.atk }}
                <span v-if="totalStats.atk > (charStore.character?.baseAtk || 0)" class="stat-bonus">
                  (+{{ totalStats.atk - (charStore.character?.baseAtk || 0) }})
                </span>
              </span>
            </div>

            <div class="stat-row">
              <span class="label"><i class="fas fa-shield-alt"></i> Hộ Thể</span>
              <span class="value def">
                {{ totalStats.def }}
                <span v-if="totalStats.def > (charStore.character?.baseDef || 0)" class="stat-bonus">
                  (+{{ totalStats.def - (charStore.character?.baseDef || 0) }})
                </span>
              </span>
            </div>

            <div class="stat-row">
              <span class="label"><i class="fas fa-wind"></i> Thân Pháp</span>
              <span class="value speed">
                {{ totalStats.speed }}
                <span v-if="totalStats.speed > (charStore.character?.baseSpeed || 0)" class="stat-bonus">
                  (+{{ totalStats.speed - (charStore.character?.baseSpeed || 0) }})
                </span>
              </span>
            </div>

            <div class="stat-row">
              <span class="label"><i class="fas fa-bolt"></i> Bạo Kích</span>
              <span class="value crit">
                {{ totalStats.crit }}%
              </span>
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
                    'filled': equipment[key],
                    'target-glow': hoveredType === key
                }"
                @mousedown.left="unequipSlow(key)"
                :title="SLOT_CONFIG[key].label"
              >
                <div class="slot-frame">
                    <img v-if="equipment[key]" :src="resolveItemImage(equipment[key].item.imageUrl)" />
                    <i v-else :class="['fas placeholder', SLOT_CONFIG[key].icon]"></i>
                    
                    <div v-if="equipment[key] && (equipment[key].enhanceLevel || equipment[key].level)" class="slot-level-tag" :class="getLevelClass(equipment[key].enhanceLevel || equipment[key].level)">
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
          </div>

          <div class="equip-column right-col">
              <div 
                v-for="key in ['HELMET', 'ARMOR', 'BOOTS']" 
                :key="key"
                class="equip-slot"
                :class="{ 
                    'filled': equipment[key],
                    'target-glow': hoveredType === key
                }"
                @mousedown.left="unequipSlow(key)"
                :title="SLOT_CONFIG[key].label"
              >
                <div class="slot-frame">
                    <img v-if="equipment[key]" :src="resolveItemImage(equipment[key].item.imageUrl)" />
                    <i v-else :class="['fas placeholder', SLOT_CONFIG[key].icon]"></i>

                    <div v-if="equipment[key] && (equipment[key].enhanceLevel || equipment[key].level)" class="slot-level-tag" :class="getLevelClass(equipment[key].enhanceLevel || equipment[key].level)">
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
          <div class="bag-info">
            Trang bị: {{ bagItems.length }} / 50
          </div>

          <div class="mini-grid custom-scroll">
            <div
              v-for="item in bagItems"
              :key="item.userItemId"
              class="mini-slot"
              :class="[
                'rarity-' + (item.item.rarity || 'C'),
                { 'is-equipped': item.isEquipped }
              ]"
              @click="equip(item)"
              @mouseenter="hoveredType = determineSlot(item.item)" 
              @mouseleave="hoveredType = null"
              :title="item.item.name"
            >
              <img :src="resolveItemImage(item.item.imageUrl)" />

              <div
                v-if="item.enhanceLevel || item.level"
                class="mini-level"
                :class="getLevelClass(item.enhanceLevel || item.level)"
              >
                +{{ item.enhanceLevel || item.level }}
              </div>

              <span class="qty" v-if="item.quantity > 1">{{ item.quantity }}</span>
            </div>

            <div
              v-for="n in Math.max(0, 25 - (bagItems.length))"
              :key="'e' + n"
              class="mini-slot empty"
            ></div>
          </div>
        </div>

      </div>
    </div>

    <transition name="fade">
      <div v-if="showUnequipModal" class="modal-overlay" @click.self="closeModal">
        <div class="dark-modal">
          <div class="modal-content">
            <h3 class="modal-title">GỠ BỎ TRANG BỊ</h3>
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
              <button class="btn-wood cancel" @click="closeModal">HỦY BỎ</button>
              <button class="btn-wood confirm" @click="confirmUnequip">XÁC NHẬN</button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useInventoryStore } from "@/stores/inventoryStore";
import { useAuthStore } from "@/stores/authStore";
import { resolveItemImage, getCurrentSkin } from "@/utils/assetHelper";

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore();
const authStore = useAuthStore();

// --- 1. BACKGROUND LOGIC (MỚI THÊM) ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  // Chỉ đổi màu nền theo giờ, không hiện text
  isNight.value = h >= 18 || h < 6;
};

// --- 2. CẤU HÌNH SLOT (GIỮ NGUYÊN) ---
const SLOT_CONFIG = {
  NECKLACE: { label: "Dây Chuyền", icon: "fa-gem", className: "necklace-slot" },
  WEAPON:   { label: "Vũ Khí",     icon: "fa-gavel", className: "weapon-slot" },
  RING:     { label: "Nhẫn",       icon: "fa-ring",  className: "ring-slot" },
  HELMET:   { label: "Mũ",         icon: "fa-hat-cowboy-side", className: "head-slot" },
  ARMOR:    { label: "Y Phục",     icon: "fa-tshirt",          className: "body-slot" },
  BOOTS:    { label: "Giày",       icon: "fa-socks",           className: "boots-slot" },
};

// --- 3. HÀM CHUẨN HÓA SLOT ---
const determineSlot = (item) => {
  if (!item) return null;
  const dbSlot = (item.slotType || item.slot_type || "").toUpperCase(); 
  if (SLOT_CONFIG[dbSlot]) {
      return dbSlot;
  }
  return null;
};

// --- 4. XỬ LÝ DỮ LIỆU ---
const equipment = computed(() => {
  const mapped = {};
  const allItems = inventoryStore.items || [];
  allItems.forEach(userItem => {
      if (userItem && userItem.item && userItem.isEquipped) {
          const slot = determineSlot(userItem.item);
          if (slot) {
              mapped[slot] = userItem;
          }
      }
  });
  return mapped;
});

const bagItems = computed(() => {
  const items = inventoryStore.items || [];
  return items.filter(ui => {
    if (!ui || !ui.item) return false;
    if (ui.isEquipped) return false;
    const type = (ui.item.type || "").toUpperCase();
    if (type === 'MATERIAL' || type === 'CONSUMABLE' || type === 'RESOURCE') {
        return false;
    }
    return determineSlot(ui.item) !== null;
  });
});

const userSkinImg = computed(() => {
    const skin = getCurrentSkin(authStore.user?.avatarUrl);
    return skin ? skin.sprites.idle : ''; 
});

const hoveredType = ref(null);

const totalStats = computed(() => {
  const char = charStore.character || {};
  let stats = {
    atk: char.baseAtk || 0,
    def: char.baseDef || 0,
    speed: char.baseSpeed || 0,
    crit: char.baseCritRate || 0,
  };

  Object.values(equipment.value).forEach((slotItem) => {
    if (slotItem && slotItem.item) {
      stats.atk += (slotItem.item.atkBonus || slotItem.item.atk || 0); 
      stats.def += (slotItem.item.defBonus || slotItem.item.def || 0);
      stats.speed += (slotItem.item.speedBonus || slotItem.item.speed || 0);
      const lv = slotItem.enhanceLevel || slotItem.level || 0;
      if (lv > 0) {
        stats.atk += lv * 2; 
        stats.def += lv * 2;
      }
    }
  });

  return {
    atk: Math.floor(stats.atk),
    def: Math.floor(stats.def),
    speed: Math.floor(stats.speed),
    crit: parseFloat(stats.crit.toFixed(2))
  };
});

const getLevelClass = (lv) => {
  if (!lv) return "";
  if (lv >= 15) return "lvl-red";
  if (lv >= 10) return "lvl-purple";
  if (lv >= 5) return "lvl-gold";
  return "lvl-white";
};

// --- 5. LOGIC MẶC/GỠ ĐỒ ---
const equip = async (userItem) => {
  if (!userItem || !userItem.item) return;
  const slot = determineSlot(userItem.item);
  if (slot) {
    try {
        await inventoryStore.equipItem(userItem.userItemId);
        await Promise.all([inventoryStore.fetchInventory(), charStore.fetchCharacter()]);
    } catch (e) {
        console.error("Lỗi API Equip:", e);
    }
  } else {
    alert(`Vật phẩm này chưa có Slot Type hợp lệ (Type: ${userItem.item.type})`);
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
        console.error("Lỗi API Unequip:", e);
    }
  }
  closeModal();
};

const closeModal = () => {
  showUnequipModal.value = false;
  itemToUnequip.value = null;
};

onMounted(async () => {
  updateDayNight(); // [MỚI] Kích hoạt màu nền
  await Promise.all([
      charStore.fetchCharacter(),
      inventoryStore.fetchInventory()
  ]);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- VARIABLES --- */
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
  /* Xóa màu nền cứng, thay bằng transparent để BG layer hiện lên */
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

/* --- 1. BACKGROUND SYSTEM (MỚI) --- */
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

/* --- 2. LAYOUT --- */
.char-wrapper { position: relative; z-index: 10; width: 100%; max-width: 1200px; padding: 20px; }
.char-grid { 
    display: grid; 
    grid-template-columns: 300px 1fr 300px; 
    gap: 20px; 
    height: 600px; 
}

/* Panels (Thêm độ trong suốt nhẹ để hòa vào nền) */
.panel {
  background: rgba(30, 20, 15, 0.9); /* Trong suốt hơn */
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  display: flex; flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}
.panel-header {
  padding: 15px; background: rgba(0, 0, 0, 0.3);
  border-bottom: 2px solid var(--wood-light);
  display: flex; align-items: center; justify-content: center; gap: 10px;
}
.panel-header h3 { margin: 0; color: var(--gold); font-size: 1.2rem; font-weight: 900; }
.decor-line { height: 2px; width: 30px; background: var(--gold); opacity: 0.5; }

/* Stats */
.stats-body { padding: 20px; flex: 1; display: flex; flex-direction: column; gap: 15px; }
.stat-row { display: flex; justify-content: space-between; border-bottom: 1px dashed rgba(255,255,255,0.1); padding-bottom: 5px; }
.label { color: #a1887f; font-weight: bold; }
.value { font-weight: bold; font-size: 1.1rem; }
.stat-bonus { color: var(--bonus-green); font-size: 0.8rem; }
.atk { color: #ef5350; } .def { color: #42a5f5; } .speed { color: #66bb6a; } .crit { color: #ab47bc; }

/* Hero Panel (Giữa) */
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
    gap: 30px; 
}

.char-center-col {
    flex: 1;
    position: relative;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.char-figure { z-index: 1; }
.skin-preview { height: 250px; width: 250px; object-fit: contain; image-rendering: pixelated; transform: scale(1.5); filter: drop-shadow(0 10px 10px rgba(0,0,0,0.8)); }
.aura-bg { position: absolute; width: 300px; height: 300px; background: radial-gradient(circle, rgba(255,236,179,0.1), transparent 70%); animation: pulseAura 4s infinite; }

/* Slots */
.equip-slot {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    transition: 0.2s;
}
.equip-slot:hover { transform: scale(1.1); z-index: 10; }

.slot-frame {
    width: 60px; height: 60px;
    background: rgba(0,0,0,0.6);
    border: 2px solid #8d6e63;
    border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 0 10px rgba(0,0,0,0.8);
    position: relative;
}
.equip-slot.filled .slot-frame { border-color: var(--gold); background: rgba(255,236,179,0.05); box-shadow: 0 0 15px rgba(255,215,0,0.3); }
.equip-slot.target-glow .slot-frame { border-color: #66bb6a; box-shadow: 0 0 20px #66bb6a; transform: scale(1.05); }

.slot-frame img { width: 90%; height: 90%; object-fit: contain; }
.placeholder { font-size: 1.5rem; color: #5d4037; opacity: 0.5; }
.slot-label-small { font-size: 10px; color: #aaa; margin-top: 4px; text-transform: uppercase; text-shadow: 1px 1px 1px #000; }

.slot-level-tag {
    position: absolute; bottom: -5px; right: -5px;
    font-size: 10px; font-weight: 900; background: #000; border: 1px solid #444;
    padding: 0 3px; border-radius: 4px;
}
.lvl-white { color: white; } .lvl-gold { color: gold; border-color: gold; }

/* Bag Panel (Phải) */
.bag-info { text-align: right; padding: 10px; color: #a1887f; font-size: 0.85rem; }
.mini-grid {
    padding: 10px; display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px; overflow-y: auto; max-height: 500px;
}
.mini-slot {
    height: 60px; background: rgba(0,0,0,0.4); border: 1px solid #4e342e;
    border-radius: 4px; position: relative; cursor: pointer;
}
.mini-slot img { width: 100%; height: 100%; padding: 4px; object-fit: contain; }
.mini-slot.empty { opacity: 0.15; border-style: dashed; }
.qty { position: absolute; bottom: 2px; right: 2px; background: rgba(0,0,0,0.8); color: white; font-size: 10px; padding: 0 4px; border-radius: 2px; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.dark-modal { width: 300px; background: var(--wood-dark); border: 1px solid #5d4037; box-shadow: 0 0 30px #000; }
.modal-content { padding: 20px; text-align: center; }
.item-preview-box { background: rgba(0,0,0,0.3); padding: 10px; margin: 15px 0; display: flex; align-items: center; gap: 10px; }
.preview-img { width: 50px; height: 50px; border: 1px solid var(--gold); }
.modal-actions { display: flex; gap: 10px; }
.btn-wood { flex: 1; padding: 10px; cursor: pointer; font-weight: bold; border-radius: 4px; border: none; }
.cancel { background: #4e342e; color: #ccc; }
.confirm { background: var(--red-seal); color: white; }

@keyframes pulseAura { 0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(0.9); } 50% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.1); } }

@media (max-width: 900px) {
    .char-grid { grid-template-columns: 1fr; height: auto; gap: 20px; }
    .hero-panel { height: 450px; order: -1; }
}
</style> -->




<template>
  <div class="page-container character-page wuxia-dark-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
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
              <span class="value highlight">Cấp {{ charStore.character?.level || 1 }}</span>
            </div>
            
            <div class="potential-box" :class="{ 'has-points': availablePoints > 0 }">
              <div class="pot-label">Điểm Tiềm Năng</div>
              <div class="pot-value">{{ availablePoints }}</div>
            </div>

            <div class="divider"></div>

            <div class="base-stat-row">
              <span class="stat-name text-red"><i class="fas fa-dumbbell"></i> Sức Mạnh</span>
              <div class="stat-ctrl">
                <span class="base-val">{{ getBaseStat('str') }}</span>
                <span v-if="pendingStats.str > 0" class="added-val">+{{ pendingStats.str }}</span>
                <button 
                  v-if="availablePoints > 0" 
                  @click="increaseStat('str')" 
                  class="btn-plus"
                ><i class="fas fa-plus"></i></button>
                <button 
                  v-if="pendingStats.str > 0" 
                  @click="decreaseStat('str')" 
                  class="btn-minus"
                ><i class="fas fa-minus"></i></button>
              </div>
            </div>

            <div class="base-stat-row">
              <span class="stat-name text-green"><i class="fas fa-heartbeat"></i> Thể Lực</span>
              <div class="stat-ctrl">
                <span class="base-val">{{ getBaseStat('vit') }}</span>
                <span v-if="pendingStats.vit > 0" class="added-val">+{{ pendingStats.vit }}</span>
                <button v-if="availablePoints > 0" @click="increaseStat('vit')" class="btn-plus"><i class="fas fa-plus"></i></button>
                <button v-if="pendingStats.vit > 0" @click="decreaseStat('vit')" class="btn-minus"><i class="fas fa-minus"></i></button>
              </div>
            </div>

            <div class="base-stat-row">
              <span class="stat-name text-blue"><i class="fas fa-wind"></i> Thân Pháp</span>
              <div class="stat-ctrl">
                <span class="base-val">{{ getBaseStat('agi') }}</span>
                <span v-if="pendingStats.agi > 0" class="added-val">+{{ pendingStats.agi }}</span>
                <button v-if="availablePoints > 0" @click="increaseStat('agi')" class="btn-plus"><i class="fas fa-plus"></i></button>
                <button v-if="pendingStats.agi > 0" @click="decreaseStat('agi')" class="btn-minus"><i class="fas fa-minus"></i></button>
              </div>
            </div>

             <div class="base-stat-row">
              <span class="stat-name text-yellow"><i class="fas fa-crosshairs"></i> Khéo Léo</span>
              <div class="stat-ctrl">
                <span class="base-val">{{ getBaseStat('dex') }}</span>
                <span v-if="pendingStats.dex > 0" class="added-val">+{{ pendingStats.dex }}</span>
                <button v-if="availablePoints > 0" @click="increaseStat('dex')" class="btn-plus"><i class="fas fa-plus"></i></button>
                <button v-if="pendingStats.dex > 0" @click="decreaseStat('dex')" class="btn-minus"><i class="fas fa-minus"></i></button>
              </div>
            </div>

            <div class="base-stat-row">
              <span class="stat-name text-purple"><i class="fas fa-brain"></i> Trí Tuệ</span>
              <div class="stat-ctrl">
                <span class="base-val">{{ getBaseStat('intelligence') }}</span>
                <span v-if="pendingStats.int > 0" class="added-val">+{{ pendingStats.int }}</span>
                <button v-if="availablePoints > 0" @click="increaseStat('int')" class="btn-plus"><i class="fas fa-plus"></i></button>
                <button v-if="pendingStats.int > 0" @click="decreaseStat('int')" class="btn-minus"><i class="fas fa-minus"></i></button>
              </div>
            </div>

            <div class="base-stat-row">
              <span class="stat-name text-gold"><i class="fas fa-star"></i> Vận Khí</span>
              <div class="stat-ctrl">
                <span class="base-val">{{ getBaseStat('luck') }}</span>
                <span v-if="pendingStats.luck > 0" class="added-val">+{{ pendingStats.luck }}</span>
                <button v-if="availablePoints > 0" @click="increaseStat('luck')" class="btn-plus"><i class="fas fa-plus"></i></button>
                <button v-if="pendingStats.luck > 0" @click="decreaseStat('luck')" class="btn-minus"><i class="fas fa-minus"></i></button>
              </div>
            </div>

            <div v-if="isPendingChanges" class="save-stats-area">
                <button @click="confirmAddStats" class="btn-wood confirm full-width">
                    <i class="fas fa-check"></i> LƯU THAY ĐỔI
                </button>
                <button @click="resetPending" class="btn-wood cancel full-width" style="margin-top:5px">
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
                :class="{ 'filled': equipment[key], 'target-glow': hoveredType === key }"
                @mousedown.left="unequipSlow(key)"
                :title="SLOT_CONFIG[key].label"
              >
                <div class="slot-frame">
                    <img v-if="equipment[key]" :src="resolveItemImage(equipment[key].item.imageUrl)" />
                    <i v-else :class="['fas placeholder', SLOT_CONFIG[key].icon]"></i>
                    
                    <div v-if="equipment[key] && (equipment[key].enhanceLevel || equipment[key].level)" class="slot-level-tag" :class="getLevelClass(equipment[key].enhanceLevel || equipment[key].level)">
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
                <div class="tool-belt-title">PHÁP BẢO KHAI KHOÁNG</div>
                <div class="tool-row">
                    <div 
                        v-for="key in ['PICKAXE', 'AXE', 'SHOVEL', 'FISHING_ROD']" 
                        :key="key"
                        class="mini-tool-slot"
                        :class="{ 'filled': equipment[key], 'target-glow': hoveredType === key }"
                        @mousedown.left="unequipSlow(key)"
                        :title="SLOT_CONFIG[key].label"
                    >
                        <img v-if="equipment[key]" :src="resolveItemImage(equipment[key].item.imageUrl)" />
                        <i v-else :class="['fas', SLOT_CONFIG[key].icon]"></i>
                    </div>
                </div>
            </div>
          </div>

          <div class="equip-column right-col">
              <div 
                v-for="key in ['HELMET', 'ARMOR', 'BOOTS']" 
                :key="key"
                class="equip-slot"
                :class="{ 'filled': equipment[key], 'target-glow': hoveredType === key }"
                @mousedown.left="unequipSlow(key)"
                :title="SLOT_CONFIG[key].label"
              >
                <div class="slot-frame">
                    <img v-if="equipment[key]" :src="resolveItemImage(equipment[key].item.imageUrl)" />
                    <i v-else :class="['fas placeholder', SLOT_CONFIG[key].icon]"></i>

                    <div v-if="equipment[key] && (equipment[key].enhanceLevel || equipment[key].level)" class="slot-level-tag" :class="getLevelClass(equipment[key].enhanceLevel || equipment[key].level)">
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
            <h3>BẢN SẮC & HÀNH TRANG</h3>
            <div class="decor-line"></div>
          </div>
          
          <div class="combat-stats-box">
             <div class="c-stat">
                <span class="c-label atk"><i class="fas fa-fist-raised"></i> Công</span>
                <span class="c-val">{{ totalStats.atk }}</span>
             </div>
             <div class="c-stat">
                <span class="c-label def"><i class="fas fa-shield-alt"></i> Thủ</span>
                <span class="c-val">{{ totalStats.def }}</span>
             </div>
             <div class="c-stat">
                <span class="c-label speed"><i class="fas fa-wind"></i> Tốc</span>
                <span class="c-val">{{ totalStats.speed }}</span>
             </div>
             <div class="c-stat">
                <span class="c-label crit"><i class="fas fa-bolt"></i> Bạo</span>
                <span class="c-val">{{ totalStats.crit }}%</span>
             </div>
          </div>

          <div class="bag-info">
            Vật phẩm: {{ bagItems.length }} / 50
          </div>

          <div class="mini-grid custom-scroll">
            <div
              v-for="item in bagItems"
              :key="item.userItemId"
              class="mini-slot"
              :class="[
                'rarity-' + (item.item.rarity || 'C'),
                { 'is-equipped': item.isEquipped }
              ]"
              @click="equip(item)"
              @mouseenter="hoveredType = determineSlot(item.item)" 
              @mouseleave="hoveredType = null"
              :title="item.item.name"
            >
              <img :src="resolveItemImage(item.item.imageUrl)" />
              <div
                v-if="item.enhanceLevel || item.level"
                class="mini-level"
                :class="getLevelClass(item.enhanceLevel || item.level)"
              >
                +{{ item.enhanceLevel || item.level }}
              </div>
              <span class="qty" v-if="item.quantity > 1">{{ item.quantity }}</span>
            </div>

            <div
              v-for="n in Math.max(0, 20 - (bagItems.length))"
              :key="'e' + n"
              class="mini-slot empty"
            ></div>
          </div>
        </div>

      </div>
    </div>

    <transition name="fade">
      <div v-if="showUnequipModal" class="modal-overlay" @click.self="closeModal">
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
              <button class="btn-wood cancel" @click="closeModal">HỦY BỎ</button>
              <button class="btn-wood confirm" @click="confirmUnequip">XÁC NHẬN</button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, reactive, watch } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useInventoryStore } from "@/stores/inventoryStore";
import { useAuthStore } from "@/stores/authStore";
import { resolveItemImage, getCurrentSkin } from "@/utils/assetHelper";

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore();
const authStore = useAuthStore();

// --- BACKGROUND ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);
const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC CỘNG ĐIỂM TIỀM NĂNG (NEW) ---
// Biến lưu trữ số điểm đang cộng tạm thời (chưa lưu vào DB)
const pendingStats = reactive({
    str: 0,
    vit: 0,
    agi: 0,
    dex: 0,
    int: 0,
    luck: 0
});

// Lấy chỉ số gốc từ Store (tránh null)
const getBaseStat = (key) => {
    return charStore.character?.[key] || 0;
};

// Tính số điểm thực tế còn lại (Total - Pending)
const availablePoints = computed(() => {
    const currentPoints = charStore.character?.statPoints || 0;
    const used = Object.values(pendingStats).reduce((a, b) => a + b, 0);
    return Math.max(0, currentPoints - used);
});

// Có đang thay đổi không?
const isPendingChanges = computed(() => {
    return Object.values(pendingStats).some(v => v > 0);
});

const increaseStat = (key) => {
    if (availablePoints.value > 0) {
        pendingStats[key]++;
    }
};

const decreaseStat = (key) => {
    if (pendingStats[key] > 0) {
        pendingStats[key]--;
    }
};

const resetPending = () => {
    Object.keys(pendingStats).forEach(k => pendingStats[k] = 0);
};

const confirmAddStats = async () => {
    const success = await charStore.addStats(pendingStats);
    if (success) {
        resetPending();
    }
};

// --- CẤU HÌNH SLOT ---
const SLOT_CONFIG = {
  NECKLACE: { label: "Dây Chuyền", icon: "fa-gem" },
  WEAPON:   { label: "Vũ Khí",     icon: "fa-gavel" },
  RING:     { label: "Nhẫn",       icon: "fa-ring" },
  HELMET:   { label: "Mũ",         icon: "fa-hat-cowboy-side" },
  ARMOR:    { label: "Y Phục",     icon: "fa-tshirt" },
  BOOTS:    { label: "Giày",       icon: "fa-socks" },
  PICKAXE:     { label: "Cúp",     icon: "fa-hammer" },
  AXE:         { label: "Rìu",     icon: "fa-axe" },
  SHOVEL:      { label: "Xẻng",    icon: "fa-trowel" },
  FISHING_ROD: { label: "Cần Câu", icon: "fa-fish" },
};

const determineSlot = (item) => {
  if (!item) return null;
  const dbSlot = (item.slotType || item.slot_type || "").toUpperCase(); 
  if (SLOT_CONFIG[dbSlot]) return dbSlot;
  return null;
};

// --- COMPUTED DATA ---
const equipment = computed(() => {
  const mapped = {};
  const allItems = inventoryStore.items || [];
  allItems.forEach(userItem => {
      if (userItem && userItem.item && userItem.isEquipped) {
          const slot = determineSlot(userItem.item);
          if (slot) mapped[slot] = userItem;
      }
  });
  return mapped;
});

const bagItems = computed(() => {
  const items = inventoryStore.items || [];
  return items.filter(ui => {
    if (!ui || !ui.item || ui.isEquipped) return false;
    // Ẩn nguyên liệu, chỉ hiện đồ mặc được
    const type = (ui.item.type || "").toUpperCase();
    if (type === 'MATERIAL' || type === 'RESOURCE') return false;
    return determineSlot(ui.item) !== null;
  });
});

const userSkinImg = computed(() => {
    const skin = getCurrentSkin(authStore.user?.avatarUrl);
    return skin ? skin.sprites.idle : ''; 
});

const hoveredType = ref(null);

// Tính tổng stats (Base + Đồ + Pending)
const totalStats = computed(() => {
  const char = charStore.character || {};
  
  // Base stats gốc
  let rawStr = (char.str || 0) + pendingStats.str;
  let rawVit = (char.vit || 0) + pendingStats.vit;
  let rawAgi = (char.agi || 0) + pendingStats.agi;
  let rawDex = (char.dex || 0) + pendingStats.dex;
  let rawLuck = (char.luck || 0) + pendingStats.luck;

  // Tính lại các chỉ số phụ dựa trên công thức Backend (mô phỏng) để hiển thị Realtime
  // Backend: Atk = 10 + Str*2 + LevelBonus ...
  // Ở đây chúng ta cộng đè lên BaseAtk hiện tại cho đơn giản, 
  // hoặc chính xác hơn là hiển thị những gì Store trả về + bonus từ đồ.
  // Tuy nhiên, để UX mượt, ta chỉ hiển thị Stats từ Server trả về + Đồ.
  // Việc cộng điểm sẽ thay đổi Stats sau khi bấm "Lưu".
  
  let stats = {
    atk: char.baseAtk || 0,
    def: char.baseDef || 0,
    speed: char.baseSpeed || 0,
    crit: char.baseCritRate || 0,
  };

  Object.values(equipment.value).forEach((slotItem) => {
    if (slotItem && slotItem.item) {
      stats.atk += (slotItem.item.atkBonus || slotItem.item.atk || 0); 
      stats.def += (slotItem.item.defBonus || slotItem.item.def || 0);
      stats.speed += (slotItem.item.speedBonus || slotItem.item.speed || 0);
      const lv = slotItem.enhanceLevel || slotItem.level || 0;
      if (lv > 0) {
        stats.atk += lv * 2; 
        stats.def += lv * 2;
      }
    }
  });

  return {
    atk: Math.floor(stats.atk),
    def: Math.floor(stats.def),
    speed: Math.floor(stats.speed),
    crit: parseFloat(stats.crit.toFixed(2))
  };
});

const getLevelClass = (lv) => {
  if (!lv) return "";
  if (lv >= 15) return "lvl-red";
  if (lv >= 10) return "lvl-purple";
  if (lv >= 5) return "lvl-gold";
  return "lvl-white";
};

// --- ACTIONS ---
const equip = async (userItem) => {
  if (!userItem || !userItem.item) return;
  const slot = determineSlot(userItem.item);
  if (slot) {
    try {
        await inventoryStore.equipItem(userItem.userItemId);
        await Promise.all([inventoryStore.fetchInventory(), charStore.fetchCharacter()]);
    } catch (e) {
        console.error("Lỗi Equip:", e);
    }
  } else {
    alert(`Vật phẩm lỗi loại: ${userItem.item.type}`);
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
  await Promise.all([
      charStore.fetchCharacter(),
      inventoryStore.fetchInventory()
  ]);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- VARIABLES --- */
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

/* --- BACKGROUND --- */
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

/* --- LAYOUT --- */
.char-wrapper { position: relative; z-index: 10; width: 100%; max-width: 1200px; padding: 20px; }
.char-grid { 
    display: grid; 
    grid-template-columns: 320px 1fr 300px; /* Điều chỉnh cột trái to hơn xíu */
    gap: 20px; 
    height: 600px; 
}

/* Panels */
.panel {
  background: rgba(30, 20, 15, 0.9);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  display: flex; flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}
.panel-header {
  padding: 12px; background: rgba(0, 0, 0, 0.3);
  border-bottom: 2px solid var(--wood-light);
  display: flex; align-items: center; justify-content: center; gap: 10px;
}
.panel-header h3 { margin: 0; color: var(--gold); font-size: 1.1rem; font-weight: 900; letter-spacing: 1px; }
.decor-line { height: 2px; width: 30px; background: var(--gold); opacity: 0.5; }

/* --- STATS PANEL CSS (New) --- */
.stats-body { padding: 15px; flex: 1; display: flex; flex-direction: column; gap: 10px; overflow-y: auto; }

.level-row { display: flex; justify-content: space-between; align-items: center; font-size: 1.1rem; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; }
.highlight { color: var(--gold); font-weight: bold; text-shadow: 0 0 10px orange; }

.potential-box {
    background: rgba(0,0,0,0.4);
    border: 1px solid #5d4037;
    border-radius: 6px;
    padding: 10px;
    display: flex; justify-content: space-between; align-items: center;
    transition: 0.3s;
}
.potential-box.has-points { border-color: var(--bonus-green); box-shadow: 0 0 10px rgba(102, 187, 106, 0.2); }
.pot-label { color: #ccc; font-size: 0.9rem; }
.pot-value { color: var(--bonus-green); font-size: 1.2rem; font-weight: bold; }

.divider { height: 1px; background: linear-gradient(to right, transparent, #5d4037, transparent); margin: 5px 0; }

.base-stat-row {
    display: flex; justify-content: space-between; align-items: center;
    padding: 5px 0;
    border-bottom: 1px dashed rgba(255,255,255,0.05);
}
.stat-name { font-weight: bold; font-size: 0.95rem; width: 110px; display: flex; align-items: center; gap: 8px; }
.stat-ctrl { display: flex; align-items: center; gap: 8px; }
.base-val { font-size: 1.1rem; font-weight: bold; color: white; }
.added-val { color: var(--bonus-green); font-size: 0.9rem; font-weight: bold; animation: popIn 0.3s; }

.btn-plus, .btn-minus {
    width: 24px; height: 24px;
    border: 1px solid #5d4037; border-radius: 4px;
    cursor: pointer; display: flex; align-items: center; justify-content: center;
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

/* --- COMBAT STATS (RIGHT PANEL) --- */
.combat-stats-box {
    display: grid; grid-template-columns: 1fr 1fr; gap: 10px;
    background: rgba(0,0,0,0.3); padding: 10px; border-radius: 8px; margin-bottom: 10px;
}
.c-stat { display: flex; flex-direction: column; align-items: center; background: rgba(255,255,255,0.05); padding: 5px; border-radius: 4px; }
.c-label { font-size: 0.8rem; opacity: 0.8; margin-bottom: 2px; }
.c-val { font-weight: bold; font-size: 1rem; }
.c-label.atk { color: #ef5350; } .c-label.def { color: #42a5f5; } 
.c-label.speed { color: #66bb6a; } .c-label.crit { color: #ab47bc; }

/* --- HERO PANEL --- */
.hero-panel {
    background: radial-gradient(circle at center, rgba(78, 52, 46, 0.8) 0%, rgba(38, 24, 21, 0.9) 100%);
    border-color: var(--gold);
    display: flex; flexDirection: row; justifyContent: space-between; alignItems: center; padding: 20px;
}
.equip-column { display: flex; flex-direction: column; justify-content: space-around; height: 100%; z-index: 5; gap: 20px; }
.char-center-col { flex: 1; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.char-figure { z-index: 1; flex: 1; display: flex; align-items: center; }
.skin-preview { height: 250px; width: 250px; object-fit: contain; image-rendering: pixelated; transform: scale(1.5); filter: drop-shadow(0 10px 10px rgba(0,0,0,0.8)); }
.aura-bg { position: absolute; width: 300px; height: 300px; background: radial-gradient(circle, rgba(255,236,179,0.1), transparent 70%); animation: pulseAura 4s infinite; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 0; }

/* TOOL BELT */
.tool-belt { margin-bottom: 10px; z-index: 10; background: rgba(0, 0, 0, 0.5); padding: 8px; border-radius: 12px; border: 1px solid #5d4037; }
.tool-belt-title { font-size: 9px; color: #a1887f; text-align: center; margin-bottom: 4px; letter-spacing: 1px; }
.tool-row { display: flex; gap: 8px; justify-content: center; }
.mini-tool-slot { width: 36px; height: 36px; background: rgba(0,0,0,0.6); border: 1px solid #8d6e63; border-radius: 6px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.2s; position: relative; }
.mini-tool-slot:hover { transform: scale(1.1); border-color: var(--gold); }
.mini-tool-slot.filled { border-color: var(--gold); background: rgba(255, 236, 179, 0.1); }
.mini-tool-slot img { width: 80%; height: 80%; object-fit: contain; }
.mini-tool-slot i { color: #5d4037; font-size: 1rem; }

/* EQUIP SLOTS */
.equip-slot { position: relative; display: flex; flex-direction: column; align-items: center; cursor: pointer; transition: 0.2s; }
.equip-slot:hover { transform: scale(1.1); z-index: 10; }
.slot-frame { width: 55px; height: 55px; background: rgba(0,0,0,0.6); border: 2px solid #8d6e63; border-radius: 8px; display: flex; align-items: center; justify-content: center; box-shadow: 0 0 10px rgba(0,0,0,0.8); position: relative; }
.equip-slot.filled .slot-frame { border-color: var(--gold); background: rgba(255,236,179,0.05); box-shadow: 0 0 15px rgba(255,215,0,0.3); }
.slot-frame img { width: 90%; height: 90%; object-fit: contain; }
.placeholder { font-size: 1.5rem; color: #5d4037; opacity: 0.5; }
.slot-label-small { font-size: 9px; color: #aaa; margin-top: 4px; text-transform: uppercase; text-shadow: 1px 1px 1px #000; }
.slot-level-tag { position: absolute; bottom: -5px; right: -5px; font-size: 9px; font-weight: 900; background: #000; border: 1px solid #444; padding: 0 3px; border-radius: 4px; }

/* BAG */
.bag-info { text-align: right; padding: 5px; color: #a1887f; font-size: 0.8rem; }
.mini-grid { padding: 5px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; overflow-y: auto; max-height: 400px; }
.mini-slot { height: 50px; background: rgba(0,0,0,0.4); border: 1px solid #4e342e; border-radius: 4px; position: relative; cursor: pointer; }
.mini-slot img { width: 100%; height: 100%; padding: 4px; object-fit: contain; }
.mini-slot.empty { opacity: 0.15; border-style: dashed; }
.qty { position: absolute; bottom: 1px; right: 1px; background: rgba(0,0,0,0.8); color: white; font-size: 9px; padding: 0 3px; border-radius: 2px; }

/* MODAL & BTNS */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.dark-modal { width: 300px; background: var(--wood-dark); border: 1px solid #5d4037; box-shadow: 0 0 30px #000; }
.modal-content { padding: 20px; text-align: center; }
.item-preview-box { background: rgba(0,0,0,0.3); padding: 10px; margin: 15px 0; display: flex; align-items: center; gap: 10px; }
.preview-img { width: 50px; height: 50px; border: 1px solid var(--gold); }
.modal-actions { display: flex; gap: 10px; }
.btn-wood { flex: 1; padding: 10px; cursor: pointer; font-weight: bold; border-radius: 4px; border: none; color: #ccc; transition: 0.2s; }
.cancel { background: #4e342e; } .cancel:hover { background: #5d4037; }
.confirm { background: var(--red-seal); color: white; } .confirm:hover { background: #d32f2f; }

@keyframes pulseAura { 0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(0.9); } 50% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.1); } }
@keyframes popIn { 0% { transform: scale(0); } 100% { transform: scale(1); } }
@keyframes slideUp { 0% { opacity: 0; transform: translateY(10px); } 100% { opacity: 1; transform: translateY(0); } }

@media (max-width: 900px) {
    .char-grid { grid-template-columns: 1fr; height: auto; gap: 20px; }
    .hero-panel { height: 450px; order: -1; }
}
</style>