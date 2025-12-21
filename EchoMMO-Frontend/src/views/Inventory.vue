<template>
  <div class="page-container inventory-page wuxia-theme">
    <div class="inventory-layout">
      
      <div class="inv-list-panel panel-wood">
        <div class="panel-header">
          <h3>HÀNH NANG</h3>
          <span class="slots-info">
            {{ inventoryStore.items.length }} / {{ authStore.user?.inventorySlots || 50 }}
            <button class="btn-tiny-add" @click="expandSlots" title="Mở rộng túi">+</button>
          </span>
        </div>

        <div class="filter-tabs">
          <button 
            v-for="tab in tabs" 
            :key="tab.id"
            :class="{ active: currentTab === tab.id }"
            @click="currentTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="inv-grid custom-scroll">
          <div 
            v-for="item in filteredItems" 
            :key="item.userItemId"
            class="inv-slot"
            :class="[
              'rarity-' + (item.item.rarity || 'COMMON'),
              { 'selected': selectedItem?.userItemId === item.userItemId },
              { 'equipped': item.isEquipped }
            ]"
            @click="selectItem(item)"
          >
            <div class="slot-inner">
              <img :src="resolveItemImage(item.item.imageUrl)" loading="lazy" />
              
              <span v-if="item.enhanceLevel || item.level" class="enhance-tag" :class="getEnhanceColor(item.enhanceLevel || item.level)">
                +{{ item.enhanceLevel || item.level }}
              </span>

              <span v-if="item.quantity > 1" class="qty-tag">{{ item.quantity }}</span>

              <i v-if="item.isEquipped" class="fas fa-shield-alt equipped-icon"></i>

              <div v-if="shouldShowDurability(item)" class="mini-durability-bar">
                <div 
                  class="mini-bar-fill" 
                  :style="{ width: getDurabilityPercent(item) + '%' }"
                  :class="getDurabilityColorClass(item)"
                ></div>
              </div>
            </div>
          </div>

          <div 
            v-for="n in Math.max(0, (authStore.user?.inventorySlots || 50) - filteredItems.length)" 
            :key="'empty-' + n"
            class="inv-slot empty"
          ></div>
        </div>
      </div>

      <div class="inv-detail-panel panel-paper">
        <div v-if="selectedItem" class="detail-content">
          <div class="detail-header" :class="'text-' + (selectedItem.item.rarity || 'COMMON')">
            <h2>{{ selectedItem.item.name }}</h2>
            <span class="type-badge">{{ selectedItem.item.type }} - Tier {{ selectedItem.item.tier || 1 }}</span>
          </div>

          <div class="detail-image-box">
            <img :src="resolveItemImage(selectedItem.item.imageUrl)" class="big-preview" />
          </div>

          <div class="stats-box">
            <div v-if="selectedItem.mainStatValue > 0" class="stat-row main-stat">
              <span class="stat-label">
                {{ getStatLabel(selectedItem.mainStatType || selectedItem.item) }}
              </span>
              <span class="stat-val">+{{ formatNumber(selectedItem.mainStatValue) }}</span>
            </div>

            <div v-if="parsedSubStats.length > 0" class="sub-stats">
              <div v-for="(sub, idx) in parsedSubStats" :key="idx" class="stat-row sub-stat">
                <span class="dot">•</span>
                <span>{{ getStatName(sub.code) }}:</span>
                <span class="val">+{{ sub.value }}{{ sub.isPercent ? '%' : '' }}</span>
              </div>
            </div>

            <div v-if="selectedItem.maxDurability" class="durability-box">
                <div class="durability-header">
                    <span>Độ Bền</span>
                    <span :class="getDurabilityColorClass(selectedItem)">
                        {{ selectedItem.currentDurability }}/{{ selectedItem.maxDurability }}
                    </span>
                </div>
                <div class="durability-progress-bg">
                    <div 
                        class="durability-progress-fill" 
                        :style="{ width: getDurabilityPercent(selectedItem) + '%' }"
                        :class="getDurabilityColorClass(selectedItem)"
                    ></div>
                </div>
                <div v-if="selectedItem.currentDurability <= 0" class="broken-warning">
                    <i class="fas fa-exclamation-triangle"></i> ĐÃ HỎNG - Cần sửa chữa!
                </div>
            </div>

            <div v-if="isTier5Tool(selectedItem)" class="legendary-effect">
                <div class="effect-title">🌟 Nội Tại Huyền Thoại</div>
                <div class="effect-desc">
                    • <b>May Mắn:</b> {{ selectedItem.item.minLuck }}-{{ selectedItem.item.maxLuck }} <br>
                    • <b>Tiết Kiệm Sức:</b> {{ (selectedItem.item.energySaveChance * 100).toFixed(0) }}% tỷ lệ không tốn Energy.
                </div>
            </div>

            <div class="desc-text">
              "{{ selectedItem.item.description }}"
            </div>
          </div>

          <div class="action-buttons">
            <button 
              v-if="canEquip(selectedItem)" 
              class="btn-action btn-equip" 
              @click="handleEquip"
            >
              {{ selectedItem.isEquipped ? 'THÁO RA' : 'TRANG BỊ' }}
            </button>

            <button 
              v-if="selectedItem.item.type === 'CONSUMABLE'" 
              class="btn-action btn-use" 
              @click="handleUse"
            >
              SỬ DỤNG
            </button>

            <button 
              v-if="needsRepair(selectedItem)" 
              class="btn-action btn-repair" 
              @click="handleRepair"
            >
              <i class="fas fa-hammer"></i> SỬA ({{ calculateRepairCost(selectedItem) }} <i class="fas fa-coins text-yellow-400"></i>)
            </button>
          </div>

        </div>
        
        <div v-else class="empty-detail">
          <i class="fas fa-box-open"></i>
          <p>Chọn một vật phẩm để xem chi tiết</p>
        </div>
      </div>

    </div>

    <GameToast ref="toast" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useInventoryStore } from '@/stores/inventoryStore';
import { useAuthStore } from '@/stores/authStore';
import { resolveItemImage } from '@/utils/assetHelper';
import GameToast from '@/components/GameToast.vue';

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const toast = ref(null);

const currentTab = ref('ALL');
const selectedItem = ref(null);

const tabs = [
  { id: 'ALL', label: 'Tất Cả' },
  { id: 'EQUIP', label: 'Trang Bị' },
  { id: 'TOOL', label: 'Công Cụ' }, 
  { id: 'CONSUMABLE', label: 'Tiêu Hao' },
  { id: 'MATERIAL', label: 'Nguyên Liệu' }
];

const filteredItems = computed(() => {
  let items = inventoryStore.items || [];
  if (currentTab.value === 'ALL') return items;
  
  if (currentTab.value === 'EQUIP') {
    return items.filter(i => ['WEAPON', 'ARMOR', 'RING', 'NECKLACE', 'BOOTS', 'HELMET'].includes(i.item.type));
  }
  
  if (currentTab.value === 'TOOL') {
    return items.filter(i => i.item.type === 'TOOL');
  }

  return items.filter(i => i.item.type === currentTab.value);
});

const parsedSubStats = computed(() => {
  if (!selectedItem.value || !selectedItem.value.subStats) return [];
  try {
    return JSON.parse(selectedItem.value.subStats);
  } catch (e) {
    return [];
  }
});

const getStatLabel = (statInfo) => {
    if(typeof statInfo === 'string') return statInfo; 
    if(statInfo.atkBonus) return "Công Lực";
    if(statInfo.defBonus) return "Hộ Thể";
    if(statInfo.hpBonus) return "Sinh Lực";
    if(statInfo.speedBonus) return "Thân Pháp";
    return "Sức Mạnh";
};

const getStatName = (code) => {
    const dict = {
        "ATK_FLAT": "Công Lực", "ATK_PERCENT": "Công Lực %",
        "DEF_FLAT": "Hộ Thể", "DEF_PERCENT": "Hộ Thể %",
        "HP_FLAT": "Sinh Lực", "HP_PERCENT": "Sinh Lực %",
        "SPEED": "Thân Pháp", "CRIT_RATE": "Bạo Kích", "CRIT_DMG": "Sát Thương Bạo"
    };
    return dict[code] || code;
};

const canEquip = (uItem) => {
    const type = uItem.item.type;
    return ['WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'TOOL'].includes(type);
};

const selectItem = (item) => {
  selectedItem.value = item;
};

const handleEquip = async () => {
  if (!selectedItem.value) return;
  try {
    if (selectedItem.value.isEquipped) {
      await inventoryStore.unequipItem(selectedItem.value.userItemId);
      toast.value.show("Đã tháo trang bị!", "success");
    } else {
      await inventoryStore.equipItem(selectedItem.value.userItemId);
      toast.value.show("Đã trang bị thành công!", "success");
    }
    const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    if(fresh) selectedItem.value = fresh;
  } catch (err) {
    toast.value.show(err, "error");
  }
};

const handleUse = async () => {
  if (!selectedItem.value) return;
  try {
    const msg = await inventoryStore.useItem(selectedItem.value.userItemId);
    toast.value.show(msg, "success");
    const exists = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    if(!exists) selectedItem.value = null;
    else selectedItem.value = exists;
  } catch (err) {
    toast.value.show(err, "error");
  }
};

const expandSlots = async () => {
    if(!confirm("Bạn có muốn dùng Echo Coin để mở rộng túi đồ không?")) return;
    try {
        await inventoryStore.expandInventory();
        toast.value.show("Mở rộng thành công!", "success");
    } catch (e) {
        toast.value.show(e, "error");
    }
};

const shouldShowDurability = (uItem) => {
    return uItem.maxDurability && uItem.maxDurability > 0;
};

const getDurabilityPercent = (uItem) => {
    if (!uItem.maxDurability) return 100;
    return Math.max(0, Math.min(100, (uItem.currentDurability / uItem.maxDurability) * 100));
};

const getDurabilityColorClass = (uItem) => {
    const pct = getDurabilityPercent(uItem);
    if (pct <= 0) return 'dur-broken';
    if (pct < 30) return 'dur-low';
    if (pct < 70) return 'dur-mid';
    return 'dur-high';
};

const needsRepair = (uItem) => {
    return uItem.maxDurability && uItem.currentDurability < uItem.maxDurability;
};

const calculateRepairCost = (uItem) => {
    if (!uItem.maxDurability) return 0;
    const missing = uItem.maxDurability - uItem.currentDurability;
    return Math.max(1, Math.ceil(missing / 10));
};

const handleRepair = async () => {
    if (!selectedItem.value) return;
    const cost = calculateRepairCost(selectedItem.value);
    
    if (authStore.wallet.echoCoin < cost) {
        toast.value.show(`Không đủ Echo Coin! Cần ${cost}.`, "error");
        return;
    }

    if (!confirm(`Sửa vật phẩm này tốn ${cost} Echo Coin. Đồng ý?`)) return;

    try {
        const msg = await inventoryStore.repairItem(selectedItem.value.userItemId);
        toast.value.show("Sửa chữa thành công!", "success");
        const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
        if(fresh) selectedItem.value = fresh;
    } catch (e) {
        toast.value.show(typeof e === 'string' ? e : "Lỗi sửa đồ", "error");
    }
};

const isTier5Tool = (uItem) => {
    return uItem.item.type === 'TOOL' && uItem.item.tier === 5;
};

const formatNumber = (num) => {
    if(!num) return 0;
    return new Intl.NumberFormat().format(num);
};

const getEnhanceColor = (lv) => {
    if(lv >= 15) return 'tag-red';
    if(lv >= 10) return 'tag-purple';
    if(lv >= 5) return 'tag-gold';
    return 'tag-white';
};

onMounted(() => {
  inventoryStore.fetchInventory();
});
</script>

<style scoped>
.wuxia-theme {
  background-color: #1a100d;
  color: #e0d4b9;
  font-family: 'Noto Serif TC', serif;
  min-height: 100vh;
  padding: 20px;
}

.inventory-layout {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  height: 85vh;
}

.panel-wood, .panel-paper {
  background: rgba(46, 30, 25, 0.95);
  border: 2px solid #5d4037;
  border-radius: 8px;
  box-shadow: 0 0 15px rgba(0,0,0,0.7);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  background: #3e2723;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #8d6e63;
}
.panel-header h3 { margin: 0; color: #ffecb3; font-weight: bold; letter-spacing: 1px; }
.slots-info { font-size: 0.9rem; color: #a1887f; }
.btn-tiny-add {
    background: #66bb6a; border: none; color: white;
    width: 20px; height: 20px; border-radius: 50%;
    cursor: pointer; font-weight: bold; margin-left: 5px;
}

.filter-tabs {
  display: flex;
  background: #281915;
  padding: 5px;
  gap: 5px;
  overflow-x: auto;
}
.filter-tabs button {
  flex: 1;
  background: transparent;
  border: 1px solid #5d4037;
  color: #a1887f;
  padding: 8px;
  cursor: pointer;
  transition: 0.2s;
  white-space: nowrap;
}
.filter-tabs button.active, .filter-tabs button:hover {
  background: #5d4037;
  color: #ffecb3;
  border-color: #ffecb3;
}

.inv-grid {
  flex: 1;
  padding: 10px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 8px;
  overflow-y: auto;
  align-content: start;
}

.inv-slot {
  aspect-ratio: 1;
  background: rgba(0,0,0,0.3);
  border: 1px solid #4e342e;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  transition: 0.2s;
}
.inv-slot:hover { border-color: #bcaaa4; transform: scale(1.05); z-index: 10; }
.inv-slot.selected { border-color: #ffecb3; box-shadow: 0 0 10px #ffecb3; }
.inv-slot.empty { opacity: 0.1; pointer-events: none; border-style: dashed; }

.slot-inner { width: 100%; height: 100%; padding: 4px; display: flex; justify-content: center; align-items: center; }
.slot-inner img { max-width: 100%; max-height: 100%; object-fit: contain; }

.rarity-COMMON { border-color: #9e9e9e; background: radial-gradient(circle, #424242 0%, transparent 70%); }
.rarity-UNCOMMON { border-color: #66bb6a; background: radial-gradient(circle, rgba(102,187,106,0.2) 0%, transparent 70%); }
.rarity-RARE { border-color: #42a5f5; background: radial-gradient(circle, rgba(66,165,245,0.2) 0%, transparent 70%); }
.rarity-EPIC { border-color: #ab47bc; background: radial-gradient(circle, rgba(171,71,188,0.2) 0%, transparent 70%); }
.rarity-LEGENDARY { border-color: #ffca28; background: radial-gradient(circle, rgba(255,202,40,0.2) 0%, transparent 70%); box-shadow: 0 0 5px #ffca28; }

.qty-tag {
  position: absolute; bottom: 2px; right: 2px;
  background: rgba(0,0,0,0.8); color: white;
  font-size: 10px; padding: 0 4px; border-radius: 2px;
}
.enhance-tag {
    position: absolute; top: 2px; right: 2px;
    font-size: 10px; padding: 0 3px; border-radius: 2px;
    font-weight: bold; color: white;
}
.tag-white { background: #555; }
.tag-gold { background: #fbc02d; color: black; }
.tag-purple { background: #7b1fa2; }
.tag-red { background: #d32f2f; }

.equipped-icon {
    position: absolute; top: 2px; left: 2px;
    font-size: 12px; color: #66bb6a;
    filter: drop-shadow(0 0 2px black);
}

.detail-content { padding: 20px; display: flex; flex-direction: column; height: 100%; overflow-y: auto; }
.empty-detail { display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100%; color: #5d4037; font-size: 1.2rem; }
.empty-detail i { font-size: 4rem; margin-bottom: 20px; opacity: 0.5; }

.detail-header { text-align: center; margin-bottom: 15px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; }
.detail-header h2 { margin: 0; font-size: 1.4rem; text-transform: uppercase; letter-spacing: 1px; }
.type-badge { font-size: 0.8rem; color: #a1887f; background: rgba(0,0,0,0.3); padding: 2px 8px; border-radius: 10px; }

.text-COMMON { color: #bdbdbd; }
.text-UNCOMMON { color: #81c784; }
.text-RARE { color: #64b5f6; }
.text-EPIC { color: #ba68c8; }
.text-LEGENDARY { color: #ffd54f; text-shadow: 0 0 10px rgba(255, 213, 79, 0.5); }

.detail-image-box { 
    height: 150px; display: flex; justify-content: center; align-items: center; 
    background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 70%);
    margin-bottom: 20px; border: 1px solid #3e2723; border-radius: 8px;
}
.big-preview { height: 80%; filter: drop-shadow(0 5px 10px rgba(0,0,0,0.5)); image-rendering: pixelated; transform: scale(1.5); }

.stats-box { flex: 1; }
.stat-row { display: flex; justify-content: space-between; padding: 5px 0; border-bottom: 1px dashed rgba(255,255,255,0.1); }
.main-stat { font-size: 1.1rem; font-weight: bold; color: #ef9a9a; margin-bottom: 10px; }
.sub-stat { color: #90caf9; font-size: 0.95rem; }
.dot { color: #5d4037; margin-right: 5px; }

.desc-text { 
    margin-top: 20px; font-style: italic; color: #a1887f; font-size: 0.9rem; line-height: 1.4; 
    background: rgba(0,0,0,0.2); padding: 10px; border-radius: 4px;
}

.durability-box { margin-top: 15px; background: #1e120f; padding: 10px; border-radius: 4px; }
.durability-header { display: flex; justify-content: space-between; font-size: 0.85rem; color: #ccc; margin-bottom: 5px; }
.durability-progress-bg { height: 6px; background: #3e2723; border-radius: 3px; overflow: hidden; }
.durability-progress-fill { height: 100%; transition: width 0.3s; }

.dur-high { background-color: #66bb6a; color: #66bb6a; }
.dur-mid { background-color: #ffa726; color: #ffa726; }
.dur-low { background-color: #ef5350; color: #ef5350; }
.dur-broken { background-color: #b71c1c; color: #b71c1c; }

.broken-warning { color: #ff5252; font-weight: bold; font-size: 0.9rem; margin-top: 5px; text-align: center; animation: blink 1s infinite; }
@keyframes blink { 50% { opacity: 0.5; } }

.mini-durability-bar {
    position: absolute; bottom: 2px; left: 2px; right: 2px;
    height: 3px; background: rgba(0,0,0,0.5); border-radius: 1px;
}
.mini-bar-fill { height: 100%; }

.action-buttons { margin-top: 20px; display: grid; grid-template-columns: 1fr; gap: 10px; }
.btn-action {
    padding: 12px; border: none; border-radius: 4px;
    font-family: inherit; font-weight: bold; cursor: pointer;
    text-transform: uppercase; letter-spacing: 1px;
    transition: 0.2s;
}
.btn-equip { background: #388e3c; color: white; }
.btn-equip:hover { background: #4caf50; }
.btn-use { background: #1976d2; color: white; }
.btn-repair { background: #f57f17; color: black; }
.btn-repair:hover { background: #fbc02d; }

@media (max-width: 768px) {
  .inventory-layout { grid-template-columns: 1fr; height: auto; }
  .inv-grid { max-height: 400px; }
}
</style>