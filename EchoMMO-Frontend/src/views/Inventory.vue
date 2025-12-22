<!-- <template>
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
</style> -->





<template>
  <div class="page-container inventory-page wuxia-theme">
    
    <div class="bg-overlay"></div>

    <div class="inventory-layout">
      
      <div class="inv-list-panel glass-panel">
        <div class="panel-header">
          <h3><i class="fas fa-sack-dollar"></i> HÀNH NANG</h3>
          <span class="slots-info">
            {{ inventoryStore.items.length }} / {{ authStore.user?.inventorySlots || 50 }}
            <button class="btn-tiny-add" @click="expandSlots" title="Mở rộng túi">
              <i class="fas fa-plus"></i>
            </button>
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
              <img :src="resolveItemImage(item.item.imageUrl)" loading="lazy" class="item-icon" />
              
              <span v-if="item.enhanceLevel || item.level" class="enhance-tag" :class="getEnhanceColor(item.enhanceLevel || item.level)">
                +{{ item.enhanceLevel || item.level }}
              </span>

              <span v-if="item.quantity > 1" class="qty-tag">x{{ item.quantity }}</span>

              <div v-if="item.isEquipped" class="equipped-badge">
                <i class="fas fa-check"></i>
              </div>

              <div v-if="shouldShowDurability(item)" class="mini-durability-bar">
                <div 
                  class="mini-bar-fill" 
                  :style="{ width: getDurabilityPercent(item) + '%' }"
                  :class="getDurabilityColorClass(item)"
                ></div>
              </div>
            </div>
            
            <div class="rarity-glow"></div>
          </div>

          <div 
            v-for="n in Math.max(0, (authStore.user?.inventorySlots || 50) - filteredItems.length)" 
            :key="'empty-' + n"
            class="inv-slot empty"
          ></div>
        </div>
      </div>

      <div class="inv-detail-panel glass-panel detail-mode">
        <div v-if="selectedItem" class="detail-content custom-scroll">
          
          <div class="detail-header">
            <h2 :class="'text-rarity-' + (selectedItem.item.rarity || 'COMMON')">
              {{ selectedItem.item.name }}
            </h2>
            <div class="item-meta">
              <span class="type-badge">{{ selectedItem.item.type }}</span>
              <span class="tier-badge">Tier {{ selectedItem.item.tier || 1 }}</span>
            </div>
          </div>

          <div class="detail-image-box" :class="'glow-' + (selectedItem.item.rarity || 'COMMON')">
            <div class="image-halo"></div>
            <img :src="resolveItemImage(selectedItem.item.imageUrl)" class="big-preview" />
          </div>

          <div class="stats-box">
            <div v-if="selectedItem.mainStatValue > 0" class="stat-row main-stat">
              <span class="stat-label">
                <i class="fas fa-khanda"></i> {{ getStatLabel(selectedItem.mainStatType || selectedItem.item) }}
              </span>
              <span class="stat-val">+{{ formatNumber(selectedItem.mainStatValue) }}</span>
            </div>

            <div v-if="parsedSubStats.length > 0" class="sub-stats-container">
              <div v-for="(sub, idx) in parsedSubStats" :key="idx" class="stat-row sub-stat">
                <span class="dot">◆</span>
                <span class="sub-label">{{ getStatName(sub.code) }}</span>
                <span class="sub-val">+{{ sub.value }}{{ sub.isPercent ? '%' : '' }}</span>
              </div>
            </div>

            <div v-if="selectedItem.item.type === 'TOOL' && selectedItem.maxDurability" class="durability-box">
                <div class="durability-header">
                    <span><i class="fas fa-hammer"></i> Độ Bền</span>
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
                    <i class="fas fa-skull-crossbones"></i> HỎNG HÓC - Cần sửa ngay!
                </div>
            </div>

            <div v-if="isTier5Tool(selectedItem)" class="legendary-effect">
                <div class="effect-title">🌟 Thần Binh Nội Tại</div>
                <div class="effect-desc">
                    • <b>May Mắn:</b> {{ selectedItem.item.minLuck }}-{{ selectedItem.item.maxLuck }} <br>
                    • <b>Bảo Khí:</b> {{ (selectedItem.item.energySaveChance * 100).toFixed(0) }}% tỷ lệ bảo toàn thể lực.
                </div>
            </div>

            <div class="desc-text">
              <i class="fas fa-quote-left"></i> {{ selectedItem.item.description }} <i class="fas fa-quote-right"></i>
            </div>
          </div>

          <div class="action-buttons">
            <button 
              v-if="canEquip(selectedItem)" 
              class="btn-action btn-equip" 
              @click="handleEquip"
            >
              <span>{{ selectedItem.isEquipped ? 'Gỡ Trang Bị' : 'Trang Bị' }}</span>
            </button>

            <button 
              v-if="selectedItem.item.type === 'CONSUMABLE'" 
              class="btn-action btn-use" 
              @click="handleUse"
            >
              <span>Sử Dụng</span>
            </button>

            <button 
              v-if="needsRepair(selectedItem)" 
              class="btn-action btn-repair" 
              @click="handleRepair"
            >
              <span><i class="fas fa-tools"></i> Sửa ({{ calculateRepairCost(selectedItem) }} <i class="fas fa-coins"></i>)</span>
            </button>
          </div>

        </div>
        
        <div v-else class="empty-detail">
          <div class="empty-icon-glow">
            <i class="fas fa-box-open"></i>
          </div>
          <p>Chọn vật phẩm để xem chi tiết</p>
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
    return uItem.item.type === 'TOOL' && uItem.maxDurability && uItem.maxDurability > 0;
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
    return uItem.item.type === 'TOOL' && uItem.maxDurability && uItem.currentDurability < uItem.maxDurability;
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
/* --- FONTS & GLOBAL --- */
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700&family=Noto+Serif+TC:wght@300;400;700&display=swap');

.wuxia-theme {
  background-color: #050505;
  background-image: radial-gradient(circle at 50% 30%, #1a100d 0%, #000000 90%);
  color: #e0d4b9;
  font-family: 'Noto Serif TC', serif;
  min-height: 100vh;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* Background overlay (vignette texture) */
.bg-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: url('https://www.transparenttextures.com/patterns/dark-leather.png'); /* Texture da tối nhẹ */
  opacity: 0.3;
  pointer-events: none;
  z-index: 0;
}

.inventory-layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 24px;
  max-width: 1280px;
  margin: 0 auto;
  height: 85vh;
}

/* --- GLASS PANELS (Bảng kính mờ) --- */
.glass-panel {
  background: rgba(30, 20, 15, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(139, 94, 60, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.glass-panel::before {
    /* Viền trên sáng bóng */
    content: '';
    position: absolute; top: 0; left: 0; right: 0; height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 236, 179, 0.5), transparent);
}

.panel-header {
  background: linear-gradient(180deg, rgba(62, 39, 35, 0.9) 0%, rgba(46, 29, 25, 0.8) 100%);
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(139, 94, 60, 0.4);
}

.panel-header h3 {
  margin: 0;
  color: #ffecb3;
  font-family: 'Cinzel', serif;
  font-weight: 700;
  font-size: 1.2rem;
  text-shadow: 0 2px 4px rgba(0,0,0,0.8);
  letter-spacing: 1px;
}

.slots-info {
  font-size: 0.9rem;
  color: #c7b299;
  font-family: 'Cinzel', serif;
}

.btn-tiny-add {
  background: linear-gradient(135deg, #66bb6a, #2e7d32);
  border: none;
  color: white;
  width: 24px; height: 24px;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.5);
  transition: transform 0.2s;
  display: inline-flex;
  align-items: center; justify-content: center;
}
.btn-tiny-add:hover { transform: scale(1.1); filter: brightness(1.2); }

/* --- TABS --- */
.filter-tabs {
  display: flex;
  background: rgba(0, 0, 0, 0.3);
  padding: 8px 12px;
  gap: 8px;
  overflow-x: auto;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.filter-tabs button {
  flex: 1;
  background: transparent;
  border: 1px solid rgba(139, 94, 60, 0.3);
  color: #a1887f;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  font-family: 'Noto Serif TC', serif;
  font-size: 0.9rem;
  border-radius: 4px;
}

.filter-tabs button:hover {
  background: rgba(139, 94, 60, 0.1);
  color: #e0d4b9;
}

.filter-tabs button.active {
  background: linear-gradient(180deg, #5d4037 0%, #3e2723 100%);
  color: #ffecb3;
  border-color: #ffecb3;
  box-shadow: 0 2px 5px rgba(0,0,0,0.5);
  text-shadow: 0 1px 2px black;
}

/* --- GRID ITEMS --- */
.inv-grid {
  flex: 1;
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(64px, 1fr));
  gap: 10px;
  overflow-y: auto;
  align-content: start;
}

.inv-slot {
  aspect-ratio: 1;
  background: rgba(10, 5, 2, 0.6);
  border: 1px solid #3e2723;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: inset 0 0 10px rgba(0,0,0,0.8); /* Depth effect */
  overflow: hidden;
}

.inv-slot:hover {
  transform: translateY(-3px) scale(1.02);
  z-index: 10;
  box-shadow: 0 8px 15px rgba(0,0,0,0.6);
  border-color: #a1887f;
}

.inv-slot.selected {
  border-color: #ffd54f;
  box-shadow: 0 0 0 1px #ffd54f, 0 0 15px rgba(255, 213, 79, 0.3);
  background: rgba(255, 213, 79, 0.05);
}

.inv-slot.empty {
  opacity: 0.3;
  background: rgba(0,0,0,0.2);
  border: 1px dashed #4e342e;
  pointer-events: none;
}

/* Rarity Styles for Slots (Background glow) */
.rarity-COMMON { box-shadow: inset 0 0 5px rgba(158,158,158,0.2); }
.rarity-UNCOMMON { border-color: #2e7d32; box-shadow: inset 0 0 10px rgba(46,125,50,0.3); }
.rarity-RARE { border-color: #1565c0; box-shadow: inset 0 0 10px rgba(21,101,192,0.3); }
.rarity-EPIC { border-color: #7b1fa2; box-shadow: inset 0 0 15px rgba(123,31,162,0.4); }
.rarity-LEGENDARY { 
  border-color: #ffa000; 
  box-shadow: inset 0 0 20px rgba(255,160,0,0.5); 
  animation: legendaryPulse 3s infinite;
}

@keyframes legendaryPulse {
  0% { box-shadow: inset 0 0 15px rgba(255,160,0,0.4); }
  50% { box-shadow: inset 0 0 25px rgba(255,160,0,0.7); }
  100% { box-shadow: inset 0 0 15px rgba(255,160,0,0.4); }
}

.slot-inner { 
  width: 100%; height: 100%; 
  display: flex; justify-content: center; align-items: center; 
  padding: 4px;
}

.item-icon {
  max-width: 90%; max-height: 90%;
  object-fit: contain;
  filter: drop-shadow(0 4px 4px rgba(0,0,0,0.6));
}

/* Tags (Qty, Enhance, Equipped) */
.qty-tag {
  position: absolute; bottom: 2px; right: 2px;
  background: rgba(0,0,0,0.85); color: #fff;
  font-size: 10px; padding: 1px 4px; border-radius: 4px;
  border: 1px solid rgba(255,255,255,0.2);
}

.enhance-tag {
  position: absolute; top: 2px; right: 2px;
  font-size: 10px; padding: 1px 4px; border-radius: 3px;
  font-weight: bold; color: white;
  text-shadow: 0 1px 2px black;
  box-shadow: 0 1px 3px rgba(0,0,0,0.5);
}
.tag-white { background: #616161; }
.tag-gold { background: linear-gradient(135deg, #fbc02d, #f57f17); color: black; }
.tag-purple { background: linear-gradient(135deg, #ab47bc, #7b1fa2); }
.tag-red { background: linear-gradient(135deg, #ef5350, #c62828); }

.equipped-badge {
  position: absolute; top: 2px; left: 2px;
  background: #2e7d32; color: white;
  width: 16px; height: 16px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 9px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.5);
  border: 1px solid #66bb6a;
}

/* Mini Durability */
.mini-durability-bar {
  position: absolute; bottom: 2px; left: 2px; right: 2px;
  height: 3px; background: rgba(0,0,0,0.6); border-radius: 2px;
  overflow: hidden;
}
.mini-bar-fill { height: 100%; border-radius: 2px; }

/* --- DETAIL PANEL --- */
.detail-mode {
  background: rgba(30, 20, 15, 0.85); /* Darker for better text readability */
}

.detail-content {
  padding: 24px;
  display: flex; flex-direction: column;
  height: 100%; overflow-y: auto;
}

.empty-detail {
  display: flex; flex-direction: column;
  justify-content: center; align-items: center;
  height: 100%; color: #8d6e63;
}
.empty-icon-glow {
  font-size: 4rem; margin-bottom: 20px; opacity: 0.5;
  filter: drop-shadow(0 0 10px rgba(141, 110, 99, 0.5));
}

/* Detail Header */
.detail-header {
  text-align: center; margin-bottom: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding-bottom: 15px;
}
.detail-header h2 {
  margin: 0 0 5px 0;
  font-family: 'Cinzel', serif;
  font-size: 1.6rem;
  letter-spacing: 1px;
}
.item-meta { display: flex; gap: 10px; justify-content: center; }
.type-badge, .tier-badge {
  font-size: 0.75rem; color: #bcaaa4;
  background: rgba(255,255,255,0.05);
  padding: 2px 8px; border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.1);
}

/* Text Colors */
.text-rarity-COMMON { color: #bdbdbd; }
.text-rarity-UNCOMMON { color: #66bb6a; text-shadow: 0 0 5px rgba(102,187,106,0.5); }
.text-rarity-RARE { color: #42a5f5; text-shadow: 0 0 5px rgba(66,165,245,0.5); }
.text-rarity-EPIC { color: #ab47bc; text-shadow: 0 0 8px rgba(171,71,188,0.6); }
.text-rarity-LEGENDARY { 
  color: #ffd54f; 
  text-shadow: 0 0 10px rgba(255, 213, 79, 0.8), 0 0 20px rgba(255, 160, 0, 0.4); 
}

/* Detail Image Stage */
.detail-image-box {
  height: 180px;
  display: flex; justify-content: center; align-items: center;
  margin-bottom: 20px;
  border-radius: 8px;
  position: relative;
  background: radial-gradient(circle, rgba(255,255,255,0.03) 0%, transparent 70%);
  border: 1px solid rgba(255,255,255,0.05);
  overflow: hidden;
}

.image-halo {
  position: absolute; width: 100px; height: 100px;
  border-radius: 50%;
  filter: blur(40px);
  z-index: 0;
}
.glow-COMMON .image-halo { background: rgba(255,255,255,0.1); }
.glow-UNCOMMON .image-halo { background: rgba(102,187,106,0.2); }
.glow-RARE .image-halo { background: rgba(66,165,245,0.2); }
.glow-EPIC .image-halo { background: rgba(171,71,188,0.25); }
.glow-LEGENDARY .image-halo { background: rgba(255,213,79,0.3); animation: haloPulse 4s infinite; }

@keyframes haloPulse { 0% { opacity: 0.5; transform: scale(1); } 50% { opacity: 0.8; transform: scale(1.2); } 100% { opacity: 0.5; transform: scale(1); } }

.big-preview { 
  height: 100px; width: 100px; object-fit: contain; 
  z-index: 1; filter: drop-shadow(0 10px 15px rgba(0,0,0,0.6));
  image-rendering: pixelated; transform: scale(1.8);
}

/* Stats */
.stats-box { flex: 1; }
.stat-row { 
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; border-bottom: 1px dashed rgba(255,255,255,0.1);
}
.main-stat {
  background: linear-gradient(90deg, rgba(239,83,80,0.1), transparent);
  padding: 10px; border-radius: 4px; border: 1px solid rgba(239,83,80,0.3);
  margin-bottom: 15px;
}
.main-stat .stat-label { font-size: 1.1rem; color: #ef9a9a; font-weight: bold; }
.main-stat .stat-val { font-size: 1.2rem; color: #fff; font-weight: bold; text-shadow: 0 0 5px #ef5350; }

.sub-stats-container { margin-bottom: 15px; }
.sub-stat { font-size: 0.95rem; color: #b0bec5; }
.dot { color: #78909c; font-size: 0.7rem; margin-right: 8px; }
.sub-val { color: #81d4fa; }

/* Durability */
.durability-box {
  margin-top: 15px; background: rgba(0,0,0,0.3);
  padding: 12px; border-radius: 6px; border: 1px solid #3e2723;
}
.durability-header { display: flex; justify-content: space-between; font-size: 0.9rem; color: #bdbdbd; margin-bottom: 8px; }
.durability-progress-bg { height: 8px; background: #212121; border-radius: 4px; overflow: hidden; box-shadow: inset 0 1px 3px black; }
.durability-progress-fill { height: 100%; transition: width 0.4s ease; background-image: linear-gradient(45deg,rgba(255,255,255,.15) 25%,transparent 25%,transparent 50%,rgba(255,255,255,.15) 50%,rgba(255,255,255,.15) 75%,transparent 75%,transparent); background-size: 1rem 1rem; }

.dur-high { background-color: #43a047; }
.dur-mid { background-color: #fb8c00; }
.dur-low { background-color: #e53935; }
.dur-broken { background-color: #b71c1c; }

.broken-warning { 
  color: #ff5252; font-weight: bold; font-size: 0.9rem; margin-top: 8px; text-align: center; 
  animation: blink 1s infinite; text-transform: uppercase; letter-spacing: 1px;
}

/* Effect Box */
.legendary-effect {
    background: linear-gradient(135deg, rgba(255, 160, 0, 0.1), transparent);
    border: 1px solid #ffb300;
    padding: 10px; border-radius: 6px; margin-top: 10px;
}
.effect-title { color: #ffca28; font-weight: bold; font-size: 0.9rem; margin-bottom: 5px; text-transform: uppercase; }
.effect-desc { font-size: 0.85rem; color: #ffe082; line-height: 1.4; }

.desc-text { 
  margin-top: 20px; font-style: italic; color: #a1887f; font-size: 0.9rem; line-height: 1.5; 
  padding: 15px; background: rgba(0,0,0,0.2); border-left: 3px solid #5d4037;
}

/* Buttons */
.action-buttons { margin-top: 20px; display: grid; gap: 12px; }
.btn-action {
  position: relative;
  padding: 14px;
  border: none;
  font-family: 'Cinzel', serif;
  font-weight: 700;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
  transition: all 0.2s;
  overflow: hidden;
}

/* Button Equip (Green/Jade) */
.btn-equip {
  background: linear-gradient(180deg, #388e3c 0%, #1b5e20 100%);
  color: #e8f5e9;
  border-bottom: 3px solid #1b5e20;
  text-shadow: 0 1px 2px rgba(0,0,0,0.8);
}
.btn-equip:hover { filter: brightness(1.1); transform: translateY(-1px); }
.btn-equip:active { transform: translateY(2px); border-bottom-width: 0; }

/* Button Use (Blue) */
.btn-use {
  background: linear-gradient(180deg, #1976d2 0%, #0d47a1 100%);
  color: #e3f2fd;
  border-bottom: 3px solid #0d47a1;
}

/* Button Repair (Gold/Iron) */
.btn-repair {
  background: linear-gradient(180deg, #fbc02d 0%, #f57f17 100%);
  color: #3e2723;
  border-bottom: 3px solid #e65100;
}

/* Custom Scrollbar */
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0,0,0,0.2); }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }
.custom-scroll::-webkit-scrollbar-thumb:hover { background: #8d6e63; }

@media (max-width: 768px) {
  .inventory-layout { grid-template-columns: 1fr; height: auto; }
  .inv-grid { max-height: 400px; }
  .detail-image-box { height: 120px; }
}
</style>