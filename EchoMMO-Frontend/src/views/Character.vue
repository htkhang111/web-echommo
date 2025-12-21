<template>
  <div class="page-container character-page wuxia-dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
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

// --- 1. CẤU HÌNH SLOT (Khớp hoàn toàn với ENUM trong Database) ---
const SLOT_CONFIG = {
  NECKLACE: { label: "Dây Chuyền", icon: "fa-gem", className: "necklace-slot" },
  WEAPON:   { label: "Vũ Khí",     icon: "fa-gavel", className: "weapon-slot" },
  RING:     { label: "Nhẫn",       icon: "fa-ring",  className: "ring-slot" },
  HELMET:   { label: "Mũ",         icon: "fa-hat-cowboy-side", className: "head-slot" },
  ARMOR:    { label: "Y Phục",     icon: "fa-tshirt",          className: "body-slot" },
  BOOTS:    { label: "Giày",       icon: "fa-socks",           className: "boots-slot" },
};

// --- 2. HÀM CHUẨN HÓA SLOT (STRICT MODE) ---
const determineSlot = (item) => {
  if (!item) return null;

  // Lấy slotType từ API (Backend trả về slotType hoặc slot_type)
  const dbSlot = (item.slotType || item.slot_type || "").toUpperCase(); 

  // Chỉ chấp nhận nếu nó nằm trong danh sách trang bị
  // Nếu là MATERIAL, NONE, CONSUMABLE... sẽ trả về null
  if (SLOT_CONFIG[dbSlot]) {
      return dbSlot;
  }
  return null;
};

// --- 3. XỬ LÝ DỮ LIỆU HIỂN THỊ ---

// Trang bị đang mặc trên người
const equipment = computed(() => {
  const mapped = {};
  const allItems = inventoryStore.items || [];
  
  // Chỉ lấy những món đang equipped = true
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

// Túi đồ (Đã lọc bỏ rác và nguyên liệu)
const bagItems = computed(() => {
  const items = inventoryStore.items || [];
  
  return items.filter(ui => {
    // Check an toàn
    if (!ui || !ui.item) return false;

    // Không hiện đồ đang mặc
    if (ui.isEquipped) return false;

    // Lọc theo TYPE (Từ Database: WEAPON, ARMOR, MATERIAL, CONSUMABLE)
    const type = (ui.item.type || "").toUpperCase();
    
    // Nếu là nguyên liệu hoặc đồ tiêu hao -> Ẩn khỏi túi trang bị
    if (type === 'MATERIAL' || type === 'CONSUMABLE' || type === 'RESOURCE') {
        return false;
    }

    // Kiểm tra kỹ hơn: Phải có Slot hợp lệ mới hiện
    return determineSlot(ui.item) !== null;
  });
});

// Skin nhân vật
const userSkinImg = computed(() => {
    const skin = getCurrentSkin(authStore.user?.avatarUrl);
    return skin ? skin.sprites.idle : ''; 
});

const hoveredType = ref(null);

// Tính tổng chỉ số
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

// --- 4. HÀM MẶC ĐỒ ---
const equip = async (userItem) => {
  if (!userItem || !userItem.item) return;

  const slot = determineSlot(userItem.item);
  
  if (slot) {
    try {
        await inventoryStore.equipItem(userItem.userItemId);
        // Refresh lại data sau khi mặc
        await Promise.all([
            inventoryStore.fetchInventory(), 
            charStore.fetchCharacter()
        ]);
    } catch (e) {
        console.error("Lỗi API Equip:", e);
    }
  } else {
    // Trường hợp items cũ trong DB chưa set slot_type đúng
    alert(`Vật phẩm này chưa có Slot Type hợp lệ (Type: ${userItem.item.type})`);
  }
};

// --- 5. HÀM GỠ ĐỒ ---
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
        await Promise.all([
            inventoryStore.fetchInventory(), 
            charStore.fetchCharacter()
        ]);
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
  background-color: #212121;
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Backgrounds */
.ink-bg-layer { position: absolute; inset: 0; z-index: 0; background-color: #3e2723; }
.mountain-bg {
  position: absolute; inset: 0;
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover; opacity: 0.6; filter: sepia(40%) brightness(0.5);
}
.fog-anim { position: absolute; inset: 0; background: linear-gradient(to top, #261815 10%, transparent 90%); }

.char-wrapper { position: relative; z-index: 10; width: 100%; max-width: 1200px; padding: 20px; }
.char-grid { 
    display: grid; 
    grid-template-columns: 300px 1fr 300px; 
    gap: 20px; 
    height: 600px; 
}

/* Panels */
.panel {
  background: rgba(30, 20, 15, 0.95);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  display: flex; flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}
.panel-header {
  padding: 15px; background: rgba(0, 0, 0, 0.2);
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

/* --- HERO PANEL --- */
.hero-panel {
    background: radial-gradient(circle at center, #4e342e 0%, #261815 100%);
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

/* Bag */
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
</style>