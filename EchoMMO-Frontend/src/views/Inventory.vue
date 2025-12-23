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





<!-- <template>
  <div class="page-container inventory-page wuxia-theme">
    <div class="bg-overlay"></div>

    <div class="inventory-layout">
      
      <div class="inv-list-panel glass-panel">
        
        <div class="panel-header">
          <div class="header-left">
            <h3><i class="fas fa-sack-dollar"></i> HÀNH NANG</h3>
          </div>
          <div class="header-right">
            <span class="slots-text">
              {{ inventoryStore.items.length }} / {{ authStore.user?.inventorySlots || 49 }}
            </span>
            
            <button class="btn-icon-action" @click="toggleViewMode" :title="viewMode === 'list' ? 'Xem dạng lưới' : 'Xem dạng cuộn'">
                <i :class="viewMode === 'list' ? 'fas fa-th' : 'fas fa-stream'"></i>
            </button>

            <button class="btn-add-slots" @click="expandSlots" title="Mở rộng (+7 ô)">
              <i class="fas fa-plus"></i>
            </button>
          </div>
        </div>

        <div class="filter-tabs">
          <button v-for="tab in tabs" :key="tab.id" 
            :class="{ active: currentTab === tab.id }" 
            @click="switchTab(tab.id)">
            {{ tab.label }}
          </button>
        </div>

        <div 
            v-if="viewMode === 'list'"
            class="infinite-wheel-container" 
            ref="wheelContainer"
            @wheel.prevent="onWheel"
            @touchstart="onTouchStart"
            @touchmove="onTouchMove"
            @touchend="onTouchEnd"
            @mousedown="onMouseDown"
        >
          <div class="center-highlight-bar"></div>

          <div 
            v-for="itemObj in renderedItems" 
            :key="itemObj.virtualKey"
            class="wheel-item"
            :class="{ 
                'active': itemObj.isActive, 
                ['rarity-' + (itemObj.data.item.rarity || 'COMMON')]: true 
            }"
            :style="itemObj.style"
            @click="scrollToIndex(itemObj.index)"
          >
            <div class="wheel-inner">
                <div class="icon-box">
                    <img :src="resolveItemImage(itemObj.data.item.imageUrl)" class="item-icon" />
                    <span v-if="itemObj.data.quantity > 1" class="qty-badge">{{ itemObj.data.quantity }}</span>
                </div>
                
                <div class="info-box">
                    <div class="name-row">
                        <span class="item-name">{{ itemObj.data.item.name }}</span>
                        <span v-if="itemObj.data.enhanceLevel" class="enhance-txt">+{{ itemObj.data.enhanceLevel }}</span>
                    </div>
                    <div class="sub-row" v-if="itemObj.isActive">
                        <span class="tier-txt">Tier {{ itemObj.data.item.tier }}</span>
                        <span v-if="itemObj.data.isEquipped" class="status-equipped">
                            <i class="fas fa-shield-alt"></i>
                        </span>
                    </div>
                </div>
            </div>
          </div>

          <div v-if="filteredItems.length === 0" class="empty-msg">
            <i class="fas fa-box-open"></i> Túi Trống
          </div>
        </div>

        <div v-else class="grid-view-container custom-scroll">
            <div class="inv-grid">
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
                        <span v-if="item.enhanceLevel" class="enhance-tag" :class="getEnhanceColor(item.enhanceLevel)">
                            +{{ item.enhanceLevel }}
                        </span>
                        <span v-if="item.quantity > 1" class="qty-tag">{{ item.quantity }}</span>
                        <i v-if="item.isEquipped" class="fas fa-shield-alt equipped-icon-grid"></i>
                    </div>
                </div>
                <div 
                    v-for="n in Math.max(0, (authStore.user?.inventorySlots || 49) - filteredItems.length)" 
                    :key="'empty-' + n"
                    class="inv-slot empty"
                ></div>
            </div>
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
                <i class="fas fa-khanda"></i> 
                {{ getStatLabel(selectedItem.mainStatType, selectedItem.item.type) }}
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
                    <div class="durability-progress-fill" 
                        :style="{ width: getDurabilityPercent(selectedItem) + '%' }"
                        :class="getDurabilityColorClass(selectedItem)"></div>
                </div>
            </div>

            <div class="desc-text">
              <i class="fas fa-quote-left"></i> {{ selectedItem.item.description }} <i class="fas fa-quote-right"></i>
            </div>
          </div>

          <div class="action-buttons">
            <button v-if="canEquip(selectedItem)" class="btn-action btn-equip" @click="handleEquip">
              <span>{{ selectedItem.isEquipped ? 'Gỡ Trang Bị' : 'Trang Bị' }}</span>
            </button>
            <button v-if="selectedItem.item.type === 'CONSUMABLE'" class="btn-action btn-use" @click="handleUse">
              <span>Sử Dụng</span>
            </button>
            <button v-if="needsRepair(selectedItem)" class="btn-action btn-repair" @click="handleRepair">
              <span><i class="fas fa-tools"></i> Sửa ({{ calculateRepairCost(selectedItem) }} <i class="fas fa-coins"></i>)</span>
            </button>
          </div>
        </div>
        
        <div v-else class="empty-detail">
          <div class="empty-icon-glow"><i class="fas fa-box-open"></i></div>
          <p>Chọn vật phẩm để xem chi tiết</p>
        </div>
      </div>
    </div>
    <GameToast ref="toast" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue';
import { useInventoryStore } from '@/stores/inventoryStore';
import { useAuthStore } from '@/stores/authStore';
import { resolveItemImage } from '@/utils/assetHelper';
import GameToast from '@/components/GameToast.vue';

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const toast = ref(null);

const currentTab = ref('ALL');
const selectedItem = ref(null);
const viewMode = ref('list'); 

// --- CONFIG WHEEL ---
const ITEM_HEIGHT = 50; 
// Giới hạn buffer render để tối ưu, nhưng vẫn đủ để hiển thị 3 trên + 3 dưới + 1 giữa + buffer
const VISIBLE_COUNT = 9; 
const scrollY = ref(0);
const wheelContainer = ref(null);

// Physics vars
let isDragging = false;
let startY = 0;
let lastTouchY = 0;
let velocity = 0;
let animationFrameId = null;
let lastTime = 0;
let targetScrollY = null; 

const tabs = [
  { id: 'ALL', label: 'Tất Cả' },
  { id: 'EQUIP', label: 'Trang Bị' },
  { id: 'TOOL', label: 'Công Cụ' }, 
  { id: 'CONSUMABLE', label: 'Tiêu Hao' },
  { id: 'MATERIAL', label: 'Nguyên Liệu' }
];

const filteredItems = computed(() => {
  let items = inventoryStore.items || [];
  if (currentTab.value === 'EQUIP') return items.filter(i => ['WEAPON', 'ARMOR', 'RING', 'NECKLACE', 'BOOTS', 'HELMET'].includes(i.item.type));
  if (currentTab.value === 'TOOL') return items.filter(i => i.item.type === 'TOOL');
  if (currentTab.value !== 'ALL') return items.filter(i => i.item.type === currentTab.value);
  return items;
});

const toggleViewMode = () => {
    viewMode.value = viewMode.value === 'list' ? 'grid' : 'list';
    if (viewMode.value === 'list') {
        scrollY.value = 0;
    }
};

// --- LOGIC AUTO SELECT ---
watch(scrollY, (newVal) => {
    if (viewMode.value !== 'list') return;
    const items = filteredItems.value;
    if (!items.length) return;
    const centerIndex = Math.round(newVal / ITEM_HEIGHT);
    const total = items.length;
    const realIndex = ((centerIndex % total) + total) % total;
    const targetItem = items[realIndex];
    if (selectedItem.value?.userItemId !== targetItem?.userItemId) {
        selectedItem.value = targetItem;
    }
});

// Auto select first item
watch(filteredItems, (newItems) => {
    if (newItems.length > 0 && !selectedItem.value) {
        selectedItem.value = newItems[0];
    }
}, { immediate: true });

// --- RENDER LOGIC (UPDATED FOR 3 TOP / 3 BOTTOM) ---
const renderedItems = computed(() => {
    if (viewMode.value !== 'list') return [];
    const items = filteredItems.value;
    const total = items.length;
    if (total === 0) return [];

    const containerHeight = wheelContainer.value ? wheelContainer.value.clientHeight : 350;
    const centerY = containerHeight / 2;

    const result = [];
    const baseIndex = Math.floor(scrollY.value / ITEM_HEIGHT);
    const buffer = Math.ceil(VISIBLE_COUNT / 2);

    for (let i = baseIndex - buffer; i <= baseIndex + buffer; i++) {
        let dataIndex = ((i % total) + total) % total;
        const itemData = items[dataIndex];
        
        // Tính khoảng cách pixel so với tâm
        const itemY = i * ITEM_HEIGHT - scrollY.value; 
        
        // Khoảng cách theo đơn vị slot (0 = tâm, 1 = ngay trên/dưới)
        const distSlots = Math.abs(itemY / ITEM_HEIGHT);

        let scale = 1;
        let opacity = 1;
        let rotateX = 0;
        let brightness = 1;
        let zIndex = 10;
        let blurAmount = 0;

        // Logic hiển thị: Chỉ rõ nét trong khoảng 3.5 slot
        if (distSlots <= 3.5) {
            if (distSlots < 0.5) {
                // ITEM Ở GIỮA (Active)
                scale = 1.05;
                opacity = 1;
                brightness = 1.2;
                zIndex = 100;
                blurAmount = 0;
            } else {
                // CÁC ITEM XUNG QUANH (1, 2, 3)
                const ratio = 1 - (distSlots / 4); // Càng xa càng giảm
                scale = 0.9 + (ratio * 0.1); 
                opacity = 0.3 + (ratio * 0.7); // 3: mờ dần
                brightness = 0.5 + (ratio * 0.5);
                rotateX = -itemY * 0.15; // Góc nghiêng nhẹ
                zIndex = Math.floor((4 - distSlots) * 10);
                
                // Hiệu ứng mờ (blur) tăng dần theo khoảng cách
                blurAmount = (distSlots - 0.5) * 1.5; 
            }
        } else {
            // NGOÀI VÙNG 3 ITEM -> Ẩn hoặc rất mờ
            opacity = 0;
            scale = 0.8;
            blurAmount = 5;
        }

        const isActive = distSlots < 0.5;

        const style = {
            transform: `translateY(${centerY + itemY - (ITEM_HEIGHT/2)}px) perspective(500px) rotateX(${rotateX}deg) scale(${scale})`,
            opacity: opacity,
            zIndex: zIndex,
            filter: `brightness(${brightness}) blur(${blurAmount}px)`,
            visibility: opacity <= 0.05 ? 'hidden' : 'visible'
        };

        result.push({
            virtualKey: `${itemData.userItemId}_${i}`,
            index: i,
            data: itemData,
            style,
            isActive
        });
    }
    return result;
});

// --- SCROLL HELPERS ---
const scrollToIndex = (virtualIndex) => {
    targetScrollY = virtualIndex * ITEM_HEIGHT;
    cancelAnimationFrame(animationFrameId);
    animateToTarget();
};

const animateToTarget = () => {
    if (targetScrollY === null) return;
    const diff = targetScrollY - scrollY.value;
    if (Math.abs(diff) > 0.5) {
        scrollY.value += diff * 0.1;
        animationFrameId = requestAnimationFrame(animateToTarget);
    } else {
        scrollY.value = targetScrollY;
        targetScrollY = null;
    }
};

const inertiaLoop = (time) => {
    if (!lastTime) lastTime = time;
    const delta = time - lastTime;
    lastTime = time;
    if (Math.abs(velocity) > 0.1) {
        scrollY.value -= velocity * 16;
        velocity *= 0.92;
        animationFrameId = requestAnimationFrame(inertiaLoop);
    } else {
        velocity = 0;
        snapToGrid();
    }
};

const snapToGrid = () => {
    const targetY = Math.round(scrollY.value / ITEM_HEIGHT) * ITEM_HEIGHT;
    const diff = targetY - scrollY.value;
    if (Math.abs(diff) > 0.5) {
        scrollY.value += diff * 0.15;
        animationFrameId = requestAnimationFrame(snapToGrid);
    } else {
        scrollY.value = targetY;
        cancelAnimationFrame(animationFrameId);
    }
};

const onWheel = (e) => {
    cancelAnimationFrame(animationFrameId);
    targetScrollY = null;
    velocity = 0;
    scrollY.value += e.deltaY * 0.4;
    snapToGrid();
};

const onMouseDown = (e) => {
    isDragging = true;
    startY = e.clientY;
    lastTouchY = startY;
    velocity = 0;
    targetScrollY = null;
    cancelAnimationFrame(animationFrameId);
    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
};
const onMouseMove = (e) => {
    if (!isDragging) return;
    const delta = lastTouchY - e.clientY;
    scrollY.value += delta;
    lastTouchY = e.clientY;
};
const onMouseUp = (e) => {
    isDragging = false;
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
    velocity = (startY - e.clientY) * 0.05;
    lastTime = 0;
    inertiaLoop(performance.now());
};

const onTouchStart = (e) => {
    isDragging = true;
    startY = e.touches[0].clientY;
    lastTouchY = startY;
    velocity = 0;
    targetScrollY = null;
    cancelAnimationFrame(animationFrameId);
};
const onTouchMove = (e) => {
    if (!isDragging) return;
    const delta = lastTouchY - e.touches[0].clientY;
    scrollY.value += delta;
    lastTouchY = e.touches[0].clientY;
};
const onTouchEnd = (e) => {
    isDragging = false;
    velocity = (startY - lastTouchY) * 0.05;
    lastTime = 0;
    inertiaLoop(performance.now());
};

const switchTab = (tabId) => {
    currentTab.value = tabId;
    scrollY.value = 0;
    velocity = 0;
};

// --- HELPERS ---
const selectItem = (item) => { selectedItem.value = item; };

const parsedSubStats = computed(() => {
  if (!selectedItem.value || !selectedItem.value.subStats) return [];
  try { return JSON.parse(selectedItem.value.subStats); } catch (e) { return []; }
});

const getStatName = (code) => {
    const dict = { 
        "ATK": "Công Lực", "ATK_FLAT": "Công Lực", "ATK_PERCENT": "Công Lực %",
        "DEF": "Hộ Thể", "DEF_FLAT": "Hộ Thể", "DEF_PERCENT": "Hộ Thể %",
        "HP": "Sinh Lực", "HP_FLAT": "Sinh Lực", "HP_PERCENT": "Sinh Lực %",
        "SPEED": "Thân Pháp", 
        "CRIT": "Bạo Kích", "CRIT_RATE": "Bạo Kích", 
        "CRIT_DMG": "Sát Thương Bạo",
        "LUCK": "May Mắn", "DURABILITY": "Độ Bền"
    };
    return dict[code] || code;
};

const getStatLabel = (mainStatType, itemType) => {
    if (mainStatType && typeof mainStatType === 'string') {
        return getStatName(mainStatType);
    }
    if (itemType) {
        if (itemType === 'WEAPON') return "Công Lực";
        if (['ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE'].includes(itemType)) return "Hộ Thể";
        if (itemType === 'TOOL') return "Hiệu Suất";
    }
    return "Sức Mạnh";
};

const canEquip = (uItem) => ['WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'TOOL'].includes(uItem.item.type);
const handleEquip = async () => {
  if (!selectedItem.value) return;
  try {
    if (selectedItem.value.isEquipped) {
      await inventoryStore.unequipItem(selectedItem.value.userItemId);
      toast.value?.show("Đã tháo trang bị!", "success");
    } else {
      await inventoryStore.equipItem(selectedItem.value.userItemId);
      toast.value?.show("Đã trang bị thành công!", "success");
    }
    const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    if(fresh) selectedItem.value = fresh;
  } catch (err) { toast.value?.show(err, "error"); }
};
const handleUse = async () => { 
    if(!selectedItem.value) return;
    try {
        const msg = await inventoryStore.useItem(selectedItem.value.userItemId);
        toast.value?.show(msg, "success");
    } catch(e) { toast.value?.show(e, "error"); }
};
const expandSlots = async () => {
    const currentSlots = authStore.user?.inventorySlots || 49;
    if (currentSlots >= 210) { toast.value?.show("Đã đạt giới hạn tối đa!", "error"); return; }
    const cost = Math.floor((currentSlots - 49) / 7) + 1;
    if(!confirm(`Mở rộng thêm 7 ô chứa?\nChi phí: ${cost} Echo Coin`)) return;
    if (authStore.wallet.echoCoin < cost) { toast.value?.show(`Thiếu Echo Coin! Cần ${cost}.`, "error"); return; }
    try {
        const msg = await inventoryStore.expandInventory();
        toast.value?.show(msg || "Mở rộng thành công!", "success");
    } catch (e) { toast.value?.show(typeof e === 'string' ? e : "Lỗi", "error"); }
};
const getDurabilityPercent = (uItem) => (!uItem.maxDurability) ? 100 : Math.max(0, Math.min(100, (uItem.currentDurability / uItem.maxDurability) * 100));
const getDurabilityColorClass = (uItem) => { const pct = getDurabilityPercent(uItem); return pct < 30 ? 'dur-low' : 'dur-high'; };
const needsRepair = (uItem) => uItem.item.type === 'TOOL' && uItem.maxDurability && uItem.currentDurability < uItem.maxDurability;
const calculateRepairCost = (uItem) => Math.max(1, Math.ceil((uItem.maxDurability - uItem.currentDurability) / 10));
const handleRepair = async () => {
    if (!selectedItem.value) return;
    const cost = calculateRepairCost(selectedItem.value);
    if(!confirm(`Sửa chữa tốn ${cost} Coin?`)) return;
    try {
        await inventoryStore.repairItem(selectedItem.value.userItemId);
        toast.value?.show("Đã sửa!", "success");
        const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
        if(fresh) selectedItem.value = fresh;
    } catch(e) { toast.value?.show(e, "error"); }
};
const getEnhanceColor = (lv) => {
    if(lv >= 15) return 'tag-red';
    if(lv >= 10) return 'tag-purple';
    if(lv >= 5) return 'tag-gold';
    return 'tag-white';
};
const formatNumber = (num) => new Intl.NumberFormat().format(num || 0);

onMounted(async () => {
  await inventoryStore.fetchInventory();
});
onUnmounted(() => {
    cancelAnimationFrame(animationFrameId);
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
});
</script>

<style scoped>
/* --- BASE THEME --- */
.wuxia-theme {
  background-color: #050505;
  background-image: radial-gradient(circle at 50% 30%, #1a100d 0%, #000000 90%);
  color: #e0d4b9;
  font-family: 'Noto Serif TC', serif;
  min-height: 100vh;
  padding: 20px;
  position: relative;
  overflow: hidden;
  user-select: none;
}
.bg-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: url('https://www.transparenttextures.com/patterns/dark-leather.png'); opacity: 0.3;
  pointer-events: none; z-index: 0;
}
.inventory-layout {
  position: relative; z-index: 1;
  display: grid; grid-template-columns: 1.2fr 1fr;
  gap: 30px;
  max-width: 1100px; margin: 0 auto; height: 85vh;
}

/* --- GLASS PANEL --- */
.glass-panel {
  background: rgba(30, 20, 15, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(139, 94, 60, 0.3);
  box-shadow: 0 10px 40px rgba(0,0,0,0.8);
  border-radius: 12px;
  display: flex; flex-direction: column; overflow: hidden;
}

/* Header */
.panel-header {
  background: linear-gradient(180deg, rgba(62, 39, 35, 0.95) 0%, rgba(46, 30, 25, 0.9) 100%);
  padding: 10px 16px;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(139, 94, 60, 0.4);
  z-index: 20;
}
.panel-header h3 { margin: 0; color: #ffecb3; font-size: 1.1rem; font-weight: bold; letter-spacing: 1px; }
.header-right { display: flex; align-items: center; gap: 8px; }
.slots-text { font-size: 0.85rem; color: #a1887f; }

/* Buttons in Header */
.btn-add-slots, .btn-icon-action {
    border: 1px solid #66bb6a;
    color: white; width: 26px; height: 26px; border-radius: 4px;
    cursor: pointer; display: flex; align-items: center; justify-content: center;
    transition: transform 0.2s; font-size: 0.8rem; margin-left: 5px;
}
.btn-add-slots { background: linear-gradient(135deg, #43a047, #2e7d32); }
.btn-icon-action { background: #3e2723; border-color: #8d6e63; color: #ffecb3; }
.btn-add-slots:hover, .btn-icon-action:hover { transform: scale(1.1); filter: brightness(1.2); }

/* Tabs */
.filter-tabs { display: flex; background: rgba(0,0,0,0.4); padding: 4px; gap: 2px; border-bottom: 1px solid rgba(255,255,255,0.05); }
.filter-tabs button {
    flex: 1; padding: 6px; border: none; background: transparent; color: #8d6e63; cursor: pointer;
    font-size: 0.8rem; border-radius: 4px; transition: 0.2s;
}
.filter-tabs button.active { background: rgba(93, 64, 55, 0.6); color: #ffca28; font-weight: bold; }

/* --- INFINITE WHEEL CONTAINER --- */
.infinite-wheel-container {
    flex: 1;
    position: relative;
    overflow: hidden;
    background: radial-gradient(circle at center, rgba(62, 39, 35, 0.2) 0%, transparent 80%);
    cursor: grab;
    /* CỐ ĐỊNH CHIỀU CAO ĐỂ VỪA 7 ITEM (7 * 50px = 350px) */
    min-height: 350px;
    max-height: 350px; 
    /* MASK ĐỂ LÀM MỜ 2 ĐẦU */
    mask-image: linear-gradient(to bottom, transparent 0%, black 20%, black 80%, transparent 100%);
    -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 20%, black 80%, transparent 100%);
}
.infinite-wheel-container:active { cursor: grabbing; }

/* Center Bar */
.center-highlight-bar {
    position: absolute; top: 50%; left: 0; right: 0; height: 56px; 
    transform: translateY(-50%);
    background: linear-gradient(90deg, transparent 0%, rgba(255, 202, 40, 0.08) 20%, rgba(255, 202, 40, 0.08) 80%, transparent 100%);
    border-top: 1px solid rgba(255, 202, 40, 0.3);
    border-bottom: 1px solid rgba(255, 202, 40, 0.3);
    box-shadow: 0 0 15px rgba(255, 202, 40, 0.1);
    pointer-events: none; z-index: 0;
}

/* Item Wheel */
.wheel-item {
    position: absolute; left: 0; right: 0; top: 0; height: 50px;
    display: flex; align-items: center; justify-content: center;
    padding: 0 10px;
    will-change: transform, opacity, filter;
    cursor: pointer;
    transition: filter 0.2s;
}

.wheel-inner {
    width: 90%; height: 44px;
    display: flex; align-items: center;
    background: rgba(46, 30, 25, 0.6);
    border: 1px solid rgba(139, 94, 60, 0.3);
    border-radius: 6px;
    padding: 0 10px;
    transition: background 0.2s, border 0.2s;
}

.wheel-item.active .wheel-inner {
    background: linear-gradient(90deg, rgba(62, 39, 35, 0.9) 0%, rgba(93, 64, 55, 1) 50%, rgba(62, 39, 35, 0.9) 100%);
    border-color: #ffca28;
    transform: scale(1.05);
}

.icon-box { position: relative; margin-right: 12px; }
.item-icon { width: 32px; height: 32px; object-fit: contain; filter: drop-shadow(0 2px 2px black); }
.qty-badge { position: absolute; bottom: -2px; right: -4px; background: #3e2723; color: #fff; font-size: 0.6rem; padding: 0 3px; border-radius: 2px; border: 1px solid #5d4037; }

.info-box { flex: 1; display: flex; flex-direction: column; justify-content: center; overflow: hidden; }
.name-row { display: flex; align-items: center; gap: 6px; }
.item-name { font-weight: bold; font-size: 0.9rem; color: #e0d4b9; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 150px; }
.active .item-name { color: #fff; text-shadow: 0 0 5px rgba(255,255,255,0.4); }

.enhance-txt { color: #ff5252; font-size: 0.8rem; font-weight: bold; }
.sub-row { display: flex; justify-content: space-between; align-items: center; font-size: 0.7rem; color: #a1887f; margin-top: 1px; }
.status-equipped { color: #66bb6a; display: flex; align-items: center; gap: 3px; }

/* Rarity */
.rarity-COMMON .item-name { color: #bdbdbd; }
.rarity-UNCOMMON .item-name { color: #81c784; }
.rarity-RARE .item-name { color: #64b5f6; }
.rarity-EPIC .item-name { color: #ba68c8; }
.rarity-LEGENDARY .item-name { color: #ffd54f; }

.empty-msg { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #5d4037; opacity: 0.5; font-style: italic; }

/* --- GRID VIEW STYLES --- */
.grid-view-container { flex: 1; padding: 15px; overflow-y: auto; }
.inv-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 8px;
}
.inv-slot {
    aspect-ratio: 1;
    background: rgba(0,0,0,0.3);
    border: 1px solid #4e342e;
    border-radius: 4px;
    cursor: pointer; position: relative;
    transition: 0.2s;
}
.inv-slot:hover { border-color: #bcaaa4; transform: scale(1.05); z-index: 5; }
.inv-slot.selected { border-color: #ffecb3; box-shadow: 0 0 8px #ffecb3; }
.inv-slot.empty { opacity: 0.1; pointer-events: none; border-style: dashed; }

.slot-inner { width: 100%; height: 100%; padding: 4px; display: flex; justify-content: center; align-items: center; }
.slot-inner img { max-width: 100%; max-height: 100%; object-fit: contain; }
.enhance-tag { position: absolute; top: 1px; right: 1px; font-size: 9px; padding: 0 3px; border-radius: 2px; font-weight: bold; color: white; background: #555; }
.tag-gold { background: #fbc02d; color: black; }
.tag-purple { background: #7b1fa2; }
.tag-red { background: #d32f2f; }
.qty-tag { position: absolute; bottom: 1px; right: 1px; font-size: 9px; background: rgba(0,0,0,0.8); color: white; padding: 0 3px; border-radius: 2px; }
.equipped-icon-grid { position: absolute; top: 2px; left: 2px; font-size: 10px; color: #66bb6a; }

/* Rarity Borders in Grid */
.rarity-COMMON { border-color: #9e9e9e; }
.rarity-UNCOMMON { border-color: #66bb6a; }
.rarity-RARE { border-color: #42a5f5; }
.rarity-EPIC { border-color: #ab47bc; }
.rarity-LEGENDARY { border-color: #ffca28; }

/* --- RIGHT PANEL --- */
.inv-detail-panel { padding: 20px; }
.detail-content { display: flex; flex-direction: column; height: 100%; }
.detail-image-box { 
    height: 160px; display: flex; justify-content: center; align-items: center;
    background: radial-gradient(circle, rgba(255,255,255,0.03) 0%, transparent 70%);
    margin-bottom: 20px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.05);
}
.big-preview { transform: scale(1.8); filter: drop-shadow(0 10px 20px rgba(0,0,0,0.8)); }

.stats-box { flex: 1; }
.stat-row { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px dashed rgba(255,255,255,0.1); }
.main-stat { margin-bottom: 12px; border-bottom: 1px solid rgba(255, 82, 82, 0.4); padding-bottom: 8px; }
.main-stat .stat-label { font-size: 1rem; color: #ef9a9a; font-weight: bold; }
.main-stat .stat-val { font-size: 1.1rem; color: #fff; font-weight: bold; text-shadow: 0 0 5px rgba(255,82,82,0.5); }
.sub-stat { color: #b0bec5; font-size: 0.9rem; }
.sub-val { color: #81d4fa; }
.dot { color: #5d4037; margin-right: 6px; font-size: 0.7rem; }

.desc-text { margin-top: 15px; font-style: italic; color: #a1887f; font-size: 0.85rem; line-height: 1.4; padding: 10px; background: rgba(0,0,0,0.2); border-left: 3px solid #5d4037; }
.durability-box { margin-top: 10px; padding: 8px; background: rgba(0,0,0,0.3); border-radius: 4px; }
.durability-header { display: flex; justify-content: space-between; font-size: 0.8rem; color: #ccc; margin-bottom: 4px; }
.durability-progress-bg { height: 6px; background: #263238; border-radius: 3px; overflow: hidden; }
.durability-progress-fill { height: 100%; transition: width 0.3s; }
.broken-warning { color: #ff5252; font-weight: bold; font-size: 0.8rem; margin-top: 4px; text-align: center; }
.dur-high { background: #66bb6a; color: #66bb6a; }
.dur-low { background: #ef5350; color: #ef5350; }

.btn-action {
    width: 100%; padding: 12px; margin-top: 10px; border: none; font-weight: bold; cursor: pointer; text-transform: uppercase; letter-spacing: 1px;
    clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
    transition: 0.2s;
}
.btn-action:hover { filter: brightness(1.15); transform: translateY(-2px); }
.btn-equip { background: linear-gradient(to bottom, #2e7d32, #1b5e20); color: white; }
.btn-use { background: linear-gradient(to bottom, #1565c0, #0d47a1); color: white; }
.btn-repair { background: linear-gradient(to bottom, #ff8f00, #ff6f00); color: black; }

@media (max-width: 768px) {
  .inventory-layout { grid-template-columns: 1fr; height: auto; }
  .infinite-wheel-container, .grid-view-container { height: 320px; }
}
</style> -->






<!-- <template>
  <div class="page-container inventory-page wuxia-theme">
    <div class="bg-overlay"></div>

    <div class="inventory-layout">
      <div class="inv-list-panel glass-panel">
        <div class="panel-header">
          <div class="header-left">
            <h3><i class="fas fa-sack-dollar"></i> HÀNH NANG</h3>
          </div>
          <div class="header-right">
            <span class="slots-text">
              {{ inventoryStore.items.length }} / {{ authStore.user?.inventorySlots || 49 }}
            </span>
            <button class="btn-icon-action" @click="toggleViewMode" :title="viewMode === 'list' ? 'Xem dạng lưới' : 'Xem dạng cuộn'">
                <i :class="viewMode === 'list' ? 'fas fa-th' : 'fas fa-stream'"></i>
            </button>
            <button class="btn-add-slots" @click="expandSlots" title="Mở rộng (+7 ô)">
              <i class="fas fa-plus"></i>
            </button>
          </div>
        </div>

        <div class="filter-tabs">
          <button v-for="tab in tabs" :key="tab.id" 
            :class="{ active: currentTab === tab.id }" 
            @click="switchTab(tab.id)">
            {{ tab.label }}
          </button>
        </div>

        <div 
            v-if="viewMode === 'list'"
            class="infinite-wheel-container" 
            ref="wheelContainer"
            @wheel.prevent="onWheel"
            @touchstart="onTouchStart"
            @touchmove="onTouchMove"
            @touchend="onTouchEnd"
            @mousedown="onMouseDown"
        >
          <div class="center-highlight-bar"></div>
          <div 
            v-for="itemObj in renderedItems" 
            :key="itemObj.virtualKey"
            class="wheel-item"
            :class="{ 
                'active': itemObj.isActive, 
                ['rarity-' + (itemObj.data.item.rarity || 'COMMON')]: true 
            }"
            :style="itemObj.style"
            @click="scrollToIndex(itemObj.index)"
          >
            <div class="wheel-inner">
                <div class="icon-box">
                    <img :src="resolveItemImage(itemObj.data.item.imageUrl)" class="item-icon" />
                    <span v-if="itemObj.data.quantity > 1" class="qty-badge">{{ itemObj.data.quantity }}</span>
                </div>
                <div class="info-box">
                    <div class="name-row">
                        <span class="item-name">{{ itemObj.data.item.name }}</span>
                        <span v-if="itemObj.data.isMythic" class="mythic-txt">M{{ itemObj.data.mythicStars || 0 }}</span>
                        <span v-else-if="itemObj.data.enhanceLevel" class="enhance-txt">+{{ itemObj.data.enhanceLevel }}</span>
                    </div>
                    <div class="sub-row" v-if="itemObj.isActive">
                        <span class="tier-txt">Tier {{ itemObj.data.item.tier }}</span>
                        <span v-if="itemObj.data.isEquipped" class="status-equipped">
                            <i class="fas fa-shield-alt"></i> Đang dùng
                        </span>
                    </div>
                </div>
            </div>
          </div>
          <div v-if="filteredItems.length === 0" class="empty-msg">
            <i class="fas fa-box-open"></i> Túi Trống
          </div>
        </div>

        <div v-else class="grid-view-container custom-scroll">
            <div class="inv-grid">
                <div 
                    v-for="item in filteredItems" 
                    :key="item.userItemId"
                    class="item-slot"
                    :class="{
                        selected: selectedItem?.userItemId === item.userItemId,
                        'border-mythic': item.isMythic,
                        [getRarityClass(item)]: true,
                    }"
                    @click="selectItem(item)"
                >
                    <div class="slot-inner">
                        <img :src="resolveItemImage(item.item.imageUrl)" loading="lazy" />
                        <div class="level-badge" v-if="item.isMythic">M{{ item.mythicStars || 0 }}</div>
                        <div class="level-badge" v-else-if="item.enhanceLevel > 0">+{{ item.enhanceLevel }}</div>
                        <span v-if="item.quantity > 1" class="qty-tag">{{ item.quantity }}</span>
                        <div class="equipped-badge" v-if="item.isEquipped">
                            <i class="fas fa-shield-alt"></i>
                        </div>
                    </div>
                </div>
                <div 
                    v-for="n in Math.max(0, (authStore.user?.inventorySlots || 49) - filteredItems.length)" 
                    :key="'empty-' + n"
                    class="item-slot empty"
                ></div>
            </div>
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
                <i class="fas fa-khanda"></i> 
                {{ getStatLabel(selectedItem.mainStatType, selectedItem.item.type) }}
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
                    <div class="durability-progress-fill" 
                        :style="{ width: getDurabilityPercent(selectedItem) + '%' }"
                        :class="getDurabilityColorClass(selectedItem)"></div>
                </div>
            </div>

            <div class="desc-text">
              <i class="fas fa-quote-left"></i> {{ selectedItem.item.description }} <i class="fas fa-quote-right"></i>
            </div>
          </div>

          <div class="action-buttons">
            <button v-if="canEquip(selectedItem)" class="btn-action btn-equip" @click="handleEquip">
              <span>{{ selectedItem.isEquipped ? 'Gỡ Trang Bị' : 'Trang Bị' }}</span>
            </button>
            <button v-if="selectedItem.item.type === 'CONSUMABLE'" class="btn-action btn-use" @click="handleUse">
              <span>Sử Dụng</span>
            </button>
            
            <button v-if="needsRepair(selectedItem)" class="btn-action btn-repair" @click="handleRepair">
              <span>
                  <i class="fas fa-tools"></i> Sửa 
                  <span class="cost-mini">
                      ({{ formatNumber(getRepairCost(selectedItem).gold) }} Vàng
                      <span v-if="getRepairCost(selectedItem).coin > 0"> + {{ getRepairCost(selectedItem).coin }} Coin</span>)
                  </span>
              </span>
            </button>

            <button v-if="!selectedItem.isEquipped" class="btn-action btn-sell" @click="openSellModal(selectedItem)">
                <span><i class="fas fa-coins"></i> Treo Bán</span>
            </button>
          </div>
        </div>
        
        <div v-else class="empty-detail">
          <div class="empty-icon-glow"><i class="fas fa-box-open"></i></div>
          <p>Chọn vật phẩm để xem chi tiết</p>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="showSellModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>ĐỊNH GIÁ BÁN</h3>
            <button class="close-btn" @click="showSellModal = false"><i class="fas fa-times"></i></button>
          </div>
          
          <div class="modal-body">
            <div class="item-preview-mini">
               <img :src="resolveItemImage(sellItemData?.item.imageUrl)" />
               <div class="mini-info">
                 <span class="mini-name" :class="'text-' + (sellItemData?.item.rarity?.toLowerCase() || 'common')">
                    {{ sellItemData?.item.name }}
                 </span>
                 <span class="mini-qty">Số lượng trong túi: {{ sellItemData?.quantity }}</span>
               </div>
            </div>

            <div class="input-group">
                <label>Số lượng bán:</label>
                <input type="number" v-model.number="sellForm.quantity" min="1" :max="sellItemData?.quantity || 1" />
            </div>

            <div class="input-group">
                <label>Đơn giá (Vàng):</label>
                <input type="number" v-model.number="sellForm.price" min="1" placeholder="Nhập giá mong muốn..." />
            </div>

            <div class="price-summary">
                <p>Tổng thu về: <span class="highlight-gold">{{ formatNumber(sellForm.price * sellForm.quantity) }}</span> <i class="fas fa-coins"></i></p>
                <p class="fee-text">(Phí sàn: Miễn phí)</p>
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn-modal btn-cancel" @click="showSellModal = false">Hủy</button>
            <button class="btn-modal btn-confirm-sell" @click="confirmSell">
                <i class="fas fa-gavel"></i> Đăng Bán
            </button>
          </div>
        </div>
      </div>
    </transition>

    <GameToast ref="toast" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import { useInventoryStore } from '@/stores/inventoryStore';
import { useAuthStore } from '@/stores/authStore';
import { useMarketStore } from '@/stores/marketStore'; 
import { resolveItemImage } from '@/utils/assetHelper';
import GameToast from '@/components/GameToast.vue';

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const marketStore = useMarketStore(); 
const toast = ref(null);

const currentTab = ref('ALL');
const selectedItem = ref(null);
const viewMode = ref('list'); 

// --- STATE MODAL BÁN ---
const showSellModal = ref(false);
const sellItemData = ref(null);
const sellForm = ref({ price: 0, quantity: 1 });

// --- CONFIG WHEEL ---
const ITEM_HEIGHT = 50; 
const VISIBLE_COUNT = 9; 
const scrollY = ref(0);
const wheelContainer = ref(null);

// Physics vars
let isDragging = false;
let startY = 0;
let lastTouchY = 0;
let velocity = 0;
let animationFrameId = null;
let lastTime = 0;
let targetScrollY = null; 

const tabs = [
  { id: 'ALL', label: 'Tất Cả' },
  { id: 'EQUIP', label: 'Trang Bị' },
  { id: 'TOOL', label: 'Công Cụ' }, 
  { id: 'CONSUMABLE', label: 'Tiêu Hao' },
  { id: 'MATERIAL', label: 'Nguyên Liệu' }
];

const filteredItems = computed(() => {
  let items = inventoryStore.items || [];
  if (currentTab.value === 'EQUIP') return items.filter(i => ['WEAPON', 'ARMOR', 'RING', 'NECKLACE', 'BOOTS', 'HELMET'].includes(i.item.type));
  if (currentTab.value === 'TOOL') return items.filter(i => i.item.type === 'TOOL');
  if (currentTab.value !== 'ALL') return items.filter(i => i.item.type === currentTab.value);
  return items;
});

const toggleViewMode = () => {
    viewMode.value = viewMode.value === 'list' ? 'grid' : 'list';
    if (viewMode.value === 'list') {
        scrollY.value = 0;
    }
};

// --- LOGIC AUTO SELECT ---
watch(scrollY, (newVal) => {
    if (viewMode.value !== 'list') return;
    const items = filteredItems.value;
    if (!items.length) return;
    const centerIndex = Math.round(newVal / ITEM_HEIGHT);
    const total = items.length;
    const realIndex = ((centerIndex % total) + total) % total;
    const targetItem = items[realIndex];
    if (selectedItem.value?.userItemId !== targetItem?.userItemId) {
        selectedItem.value = targetItem;
    }
});

watch(filteredItems, (newItems) => {
    if (newItems.length > 0 && !selectedItem.value) {
        selectedItem.value = newItems[0];
    }
}, { immediate: true });

// --- RENDER LOGIC ---
const renderedItems = computed(() => {
    if (viewMode.value !== 'list') return [];
    const items = filteredItems.value;
    const total = items.length;
    if (total === 0) return [];

    const containerHeight = wheelContainer.value ? wheelContainer.value.clientHeight : 350;
    const centerY = containerHeight / 2;

    const result = [];
    const baseIndex = Math.floor(scrollY.value / ITEM_HEIGHT);
    const buffer = Math.ceil(VISIBLE_COUNT / 2);

    for (let i = baseIndex - buffer; i <= baseIndex + buffer; i++) {
        let dataIndex = ((i % total) + total) % total;
        const itemData = items[dataIndex];
        
        const itemY = i * ITEM_HEIGHT - scrollY.value; 
        const distSlots = Math.abs(itemY / ITEM_HEIGHT);

        let scale = 1;
        let opacity = 1;
        let rotateX = 0;
        let brightness = 1;
        let zIndex = 10;
        let blurAmount = 0;

        if (distSlots <= 3.5) {
            if (distSlots < 0.5) {
                scale = 1.05;
                opacity = 1;
                brightness = 1.2;
                zIndex = 100;
                blurAmount = 0;
            } else {
                const ratio = 1 - (distSlots / 4);
                scale = 0.9 + (ratio * 0.1); 
                opacity = 0.3 + (ratio * 0.7);
                brightness = 0.5 + (ratio * 0.5);
                rotateX = -itemY * 0.15;
                zIndex = Math.floor((4 - distSlots) * 10);
                blurAmount = (distSlots - 0.5) * 1.5; 
            }
        } else {
            opacity = 0;
            scale = 0.8;
            blurAmount = 5;
        }

        const isActive = distSlots < 0.5;

        const style = {
            transform: `translateY(${centerY + itemY - (ITEM_HEIGHT/2)}px) perspective(500px) rotateX(${rotateX}deg) scale(${scale})`,
            opacity: opacity,
            zIndex: zIndex,
            filter: `brightness(${brightness}) blur(${blurAmount}px)`,
            visibility: opacity <= 0.05 ? 'hidden' : 'visible'
        };

        result.push({
            virtualKey: `${itemData.userItemId}_${i}`,
            index: i,
            data: itemData,
            style,
            isActive
        });
    }
    return result;
});

// --- SCROLL HELPERS ---
const scrollToIndex = (virtualIndex) => {
    targetScrollY = virtualIndex * ITEM_HEIGHT;
    cancelAnimationFrame(animationFrameId);
    animateToTarget();
};

const animateToTarget = () => {
    if (targetScrollY === null) return;
    const diff = targetScrollY - scrollY.value;
    if (Math.abs(diff) > 0.5) {
        scrollY.value += diff * 0.1;
        animationFrameId = requestAnimationFrame(animateToTarget);
    } else {
        scrollY.value = targetScrollY;
        targetScrollY = null;
    }
};

const inertiaLoop = (time) => {
    if (!lastTime) lastTime = time;
    const delta = time - lastTime;
    lastTime = time;
    if (Math.abs(velocity) > 0.1) {
        scrollY.value -= velocity * 16;
        velocity *= 0.92;
        animationFrameId = requestAnimationFrame(inertiaLoop);
    } else {
        velocity = 0;
        snapToGrid();
    }
};

const snapToGrid = () => {
    const targetY = Math.round(scrollY.value / ITEM_HEIGHT) * ITEM_HEIGHT;
    const diff = targetY - scrollY.value;
    if (Math.abs(diff) > 0.5) {
        scrollY.value += diff * 0.15;
        animationFrameId = requestAnimationFrame(snapToGrid);
    } else {
        scrollY.value = targetY;
        cancelAnimationFrame(animationFrameId);
    }
};

const onWheel = (e) => {
    cancelAnimationFrame(animationFrameId);
    targetScrollY = null;
    velocity = 0;
    scrollY.value += e.deltaY * 0.4;
    snapToGrid();
};

const onMouseDown = (e) => {
    isDragging = true;
    startY = e.clientY;
    lastTouchY = startY;
    velocity = 0;
    targetScrollY = null;
    cancelAnimationFrame(animationFrameId);
    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
};
const onMouseMove = (e) => {
    if (!isDragging) return;
    const delta = lastTouchY - e.clientY;
    scrollY.value += delta;
    lastTouchY = e.clientY;
};
const onMouseUp = (e) => {
    isDragging = false;
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
    velocity = (startY - e.clientY) * 0.05;
    lastTime = 0;
    inertiaLoop(performance.now());
};

const onTouchStart = (e) => {
    isDragging = true;
    startY = e.touches[0].clientY;
    lastTouchY = startY;
    velocity = 0;
    targetScrollY = null;
    cancelAnimationFrame(animationFrameId);
};
const onTouchMove = (e) => {
    if (!isDragging) return;
    const delta = lastTouchY - e.touches[0].clientY;
    scrollY.value += delta;
    lastTouchY = e.touches[0].clientY;
};
const onTouchEnd = (e) => {
    isDragging = false;
    velocity = (startY - lastTouchY) * 0.05;
    lastTime = 0;
    inertiaLoop(performance.now());
};

const switchTab = (tabId) => {
    currentTab.value = tabId;
    scrollY.value = 0;
    velocity = 0;
};

// --- HELPERS ---
const selectItem = (item) => { selectedItem.value = item; };

const parsedSubStats = computed(() => {
  if (!selectedItem.value || !selectedItem.value.subStats) return [];
  try { return JSON.parse(selectedItem.value.subStats); } catch (e) { return []; }
});

const getStatName = (code) => {
    const dict = { 
        "ATK": "Công Lực", "ATK_FLAT": "Công Lực", "ATK_PERCENT": "Công Lực %",
        "DEF": "Hộ Thể", "DEF_FLAT": "Hộ Thể", "DEF_PERCENT": "Hộ Thể %",
        "HP": "Sinh Lực", "HP_FLAT": "Sinh Lực", "HP_PERCENT": "Sinh Lực %",
        "SPEED": "Thân Pháp", 
        "CRIT": "Bạo Kích", "CRIT_RATE": "Bạo Kích", 
        "CRIT_DMG": "Sát Thương Bạo",
        "LUCK": "May Mắn", "DURABILITY": "Độ Bền"
    };
    return dict[code] || code;
};

const getStatLabel = (mainStatType, itemType) => {
    if (mainStatType && typeof mainStatType === 'string') {
        return getStatName(mainStatType);
    }
    if (itemType) {
        if (itemType === 'WEAPON') return "Công Lực";
        if (['ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE'].includes(itemType)) return "Hộ Thể";
        if (itemType === 'TOOL') return "Hiệu Suất";
    }
    return "Sức Mạnh";
};

const canEquip = (uItem) => ['WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'TOOL'].includes(uItem.item.type);

// --- HANDLERS ---
const handleEquip = async () => {
  if (!selectedItem.value) return;
  try {
    if (selectedItem.value.isEquipped) {
      await inventoryStore.unequipItem(selectedItem.value.userItemId);
      toast.value?.show("Đã tháo trang bị!", "success");
    } else {
      await inventoryStore.equipItem(selectedItem.value.userItemId);
      toast.value?.show("Đã trang bị thành công!", "success");
    }
    const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    if(fresh) selectedItem.value = fresh;
  } catch (err) { toast.value?.show(err, "error"); }
};

const handleUse = async () => { 
    if(!selectedItem.value) return;
    try {
        const msg = await inventoryStore.useItem(selectedItem.value.userItemId);
        toast.value?.show(msg, "success");
    } catch(e) { toast.value?.show(e, "error"); }
};

// --- LOGIC MỞ MODAL BÁN ---
const openSellModal = (item) => {
    if(item.isEquipped) {
        toast.value?.show("Phải gỡ trang bị mới được bán!", "error");
        return;
    }
    sellItemData.value = item;
    // Mặc định giá = giá base * 1.5 (Gợi ý), số lượng = 1
    sellForm.value = { 
        price: item.item.basePrice ? Math.ceil(item.item.basePrice * 1.5) : 100, 
        quantity: 1 
    };
    showSellModal.value = true;
};

// --- LOGIC XÁC NHẬN BÁN (TREO CHỢ) ---
const confirmSell = async () => {
    if(!sellItemData.value) return;
    if(sellForm.value.price <= 0) {
        toast.value?.show("Giá bán phải lớn hơn 0!", "error");
        return;
    }
    if(sellForm.value.quantity > sellItemData.value.quantity) {
        toast.value?.show("Không đủ số lượng!", "error");
        return;
    }

    try {
        // GỌI API TREO CHỢ (MARKET LISTING)
        await marketStore.createListing(
            sellItemData.value.userItemId,
            sellForm.value.price,
            sellForm.value.quantity
        );
        toast.value?.show("Đã treo bán thành công!", "success");
        showSellModal.value = false;
        await inventoryStore.fetchInventory(); // Làm mới túi đồ
        selectedItem.value = null;
    } catch (e) {
        toast.value?.show(typeof e === 'string' ? e : "Lỗi khi đăng bán", "error");
    }
};

const expandSlots = async () => {
    const currentSlots = authStore.user?.inventorySlots || 49;
    if (currentSlots >= 210) { toast.value?.show("Đã đạt giới hạn tối đa!", "error"); return; }
    const cost = Math.floor((currentSlots - 49) / 7) + 1;
    if(!confirm(`Mở rộng thêm 7 ô chứa?\nChi phí: ${cost} Echo Coin`)) return;
    if (authStore.wallet.echoCoin < cost) { toast.value?.show(`Thiếu Echo Coin! Cần ${cost}.`, "error"); return; }
    try {
        const msg = await inventoryStore.expandInventory();
        toast.value?.show(msg || "Mở rộng thành công!", "success");
    } catch (e) { toast.value?.show(typeof e === 'string' ? e : "Lỗi", "error"); }
};

// --- DURABILITY & REPAIR LOGIC (UPDATED) ---
const getDurabilityPercent = (uItem) => (!uItem.maxDurability) ? 100 : Math.max(0, Math.min(100, (uItem.currentDurability / uItem.maxDurability) * 100));
const getDurabilityColorClass = (uItem) => { const pct = getDurabilityPercent(uItem); return pct < 30 ? 'dur-low' : 'dur-high'; };
const needsRepair = (uItem) => uItem.item.type === 'TOOL' && uItem.maxDurability && uItem.currentDurability < uItem.maxDurability;

// [NEW] HÀM TÍNH PHÍ SỬA CHỮA CHI TIẾT
const getRepairCost = (uItem) => {
    if (!uItem.maxDurability || !uItem.currentDurability) return { gold: 0, coin: 0 };
    const missing = uItem.maxDurability - uItem.currentDurability;
    if (missing <= 0) return { gold: 0, coin: 0 };

    let goldPerPoint = 10;
    let coinPerPoint = 0;

    const rarity = uItem.isMythic ? 'MYTHIC' : (uItem.item.rarity || 'COMMON');

    switch (rarity) {
        case 'COMMON':
        case 'UNCOMMON': // Phàm Phẩm
            goldPerPoint = 10;
            break;
        case 'RARE':     // Lương Phẩm
            goldPerPoint = 50;
            break;
        case 'EPIC':     // Linh Phẩm
            goldPerPoint = 200;
            coinPerPoint = 0.1;
            break;
        case 'LEGENDARY':
        case 'MYTHIC':   // Tiên Phẩm
            goldPerPoint = 1000;
            coinPerPoint = 1.0;
            break;
        default:
            goldPerPoint = 10;
    }

    return {
        gold: missing * goldPerPoint,
        coin: parseFloat((missing * coinPerPoint).toFixed(1)) // Làm tròn 1 số thập phân
    };
};

const handleRepair = async () => {
    if (!selectedItem.value) return;
    const cost = getRepairCost(selectedItem.value);
    
    // Tạo thông báo xác nhận chi tiết
    let confirmMsg = `Sửa chữa vật phẩm này?\nChi phí: ${formatNumber(cost.gold)} Vàng`;
    if (cost.coin > 0) {
        confirmMsg += ` + ${cost.coin} Echo Coin`;
    }

    if(!confirm(confirmMsg)) return;

    try {
        await inventoryStore.repairItem(selectedItem.value.userItemId);
        toast.value?.show("Đã sửa thành công!", "success");
        const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
        if(fresh) selectedItem.value = fresh;
    } catch(e) { toast.value?.show(e, "error"); }
};

// --- STYLING HELPERS ---
const getRarityClass = (item) => item.isMythic ? "mythic" : (item.item.rarity ? item.item.rarity.toLowerCase() : "common");
const formatNumber = (num) => new Intl.NumberFormat().format(num || 0);

onMounted(async () => {
  await inventoryStore.fetchInventory();
});
onUnmounted(() => {
    cancelAnimationFrame(animationFrameId);
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
});
</script>

<style scoped>
/* --- BASE THEME --- */
.wuxia-theme {
  background-color: #050505;
  background-image: radial-gradient(circle at 50% 30%, #1a100d 0%, #000000 90%);
  color: #e0d4b9;
  font-family: 'Noto Serif TC', serif;
  min-height: 100vh;
  padding: 20px;
  position: relative;
  overflow: hidden;
  user-select: none;
}
.bg-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: url('https://www.transparenttextures.com/patterns/dark-leather.png'); opacity: 0.3;
  pointer-events: none; z-index: 0;
}
.inventory-layout {
  position: relative; z-index: 1;
  display: grid; grid-template-columns: 1.2fr 1fr;
  gap: 30px;
  max-width: 1100px; margin: 0 auto; height: 85vh;
}

/* --- GLASS PANEL --- */
.glass-panel {
  background: rgba(30, 20, 15, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(139, 94, 60, 0.3);
  box-shadow: 0 10px 40px rgba(0,0,0,0.8);
  border-radius: 12px;
  display: flex; flex-direction: column; overflow: hidden;
}

/* Header */
.panel-header {
  background: linear-gradient(180deg, rgba(62, 39, 35, 0.95) 0%, rgba(46, 30, 25, 0.9) 100%);
  padding: 10px 16px;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(139, 94, 60, 0.4);
  z-index: 20;
}
.panel-header h3 { margin: 0; color: #ffecb3; font-size: 1.1rem; font-weight: bold; letter-spacing: 1px; }
.header-right { display: flex; align-items: center; gap: 8px; }
.slots-text { font-size: 0.85rem; color: #a1887f; }

/* Buttons in Header */
.btn-add-slots, .btn-icon-action {
    border: 1px solid #66bb6a;
    color: white; width: 26px; height: 26px; border-radius: 4px;
    cursor: pointer; display: flex; align-items: center; justify-content: center;
    transition: transform 0.2s; font-size: 0.8rem; margin-left: 5px;
}
.btn-add-slots { background: linear-gradient(135deg, #43a047, #2e7d32); }
.btn-icon-action { background: #3e2723; border-color: #8d6e63; color: #ffecb3; }
.btn-add-slots:hover, .btn-icon-action:hover { transform: scale(1.1); filter: brightness(1.2); }

/* Tabs */
.filter-tabs { display: flex; background: rgba(0,0,0,0.4); padding: 4px; gap: 2px; border-bottom: 1px solid rgba(255,255,255,0.05); }
.filter-tabs button {
    flex: 1; padding: 6px; border: none; background: transparent; color: #8d6e63; cursor: pointer;
    font-size: 0.8rem; border-radius: 4px; transition: 0.2s;
}
.filter-tabs button.active { background: rgba(93, 64, 55, 0.6); color: #ffca28; font-weight: bold; }

/* --- INFINITE WHEEL CONTAINER --- */
.infinite-wheel-container {
    flex: 1;
    position: relative;
    overflow: hidden;
    background: radial-gradient(circle at center, rgba(62, 39, 35, 0.2) 0%, transparent 80%);
    cursor: grab;
    min-height: 350px;
    max-height: 350px; 
    mask-image: linear-gradient(to bottom, transparent 0%, black 20%, black 80%, transparent 100%);
    -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 20%, black 80%, transparent 100%);
}
.infinite-wheel-container:active { cursor: grabbing; }

.center-highlight-bar {
    position: absolute; top: 50%; left: 0; right: 0; height: 56px; 
    transform: translateY(-50%);
    background: linear-gradient(90deg, transparent 0%, rgba(255, 202, 40, 0.08) 20%, rgba(255, 202, 40, 0.08) 80%, transparent 100%);
    border-top: 1px solid rgba(255, 202, 40, 0.3);
    border-bottom: 1px solid rgba(255, 202, 40, 0.3);
    box-shadow: 0 0 15px rgba(255, 202, 40, 0.1);
    pointer-events: none; z-index: 0;
}

.wheel-item {
    position: absolute; left: 0; right: 0; top: 0; height: 50px;
    display: flex; align-items: center; justify-content: center;
    padding: 0 10px;
    will-change: transform, opacity, filter;
    cursor: pointer;
    transition: filter 0.2s;
}

.wheel-inner {
    width: 90%; height: 44px;
    display: flex; align-items: center;
    background: rgba(46, 30, 25, 0.6);
    border: 1px solid rgba(139, 94, 60, 0.3);
    border-radius: 6px;
    padding: 0 10px;
    transition: background 0.2s, border 0.2s;
}

.wheel-item.active .wheel-inner {
    background: linear-gradient(90deg, rgba(62, 39, 35, 0.9) 0%, rgba(93, 64, 55, 1) 50%, rgba(62, 39, 35, 0.9) 100%);
    border-color: #ffca28;
    transform: scale(1.05);
}

.icon-box { position: relative; margin-right: 12px; }
.item-icon { width: 32px; height: 32px; object-fit: contain; filter: drop-shadow(0 2px 2px black); }
.qty-badge { position: absolute; bottom: -2px; right: -4px; background: #3e2723; color: #fff; font-size: 0.6rem; padding: 0 3px; border-radius: 2px; border: 1px solid #5d4037; }

.info-box { flex: 1; display: flex; flex-direction: column; justify-content: center; overflow: hidden; }
.name-row { display: flex; align-items: center; gap: 6px; }
.item-name { font-weight: bold; font-size: 0.9rem; color: #e0d4b9; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 150px; }
.active .item-name { color: #fff; text-shadow: 0 0 5px rgba(255,255,255,0.4); }

.enhance-txt { color: #ff5252; font-size: 0.8rem; font-weight: bold; }
.mythic-txt { color: #ff1744; font-size: 0.8rem; font-weight: bold; text-shadow: 0 0 2px #ff1744; }
.sub-row { display: flex; justify-content: space-between; align-items: center; font-size: 0.7rem; color: #a1887f; margin-top: 1px; }
.status-equipped { color: #66bb6a; display: flex; align-items: center; gap: 3px; }

/* Rarity */
.rarity-COMMON .item-name { color: #bdbdbd; }
.rarity-UNCOMMON .item-name { color: #81c784; }
.rarity-RARE .item-name { color: #64b5f6; }
.rarity-EPIC .item-name { color: #ba68c8; }
.rarity-LEGENDARY .item-name { color: #ffca28; }
.rarity-MYTHIC .item-name { color: #ff1744; text-shadow: 0 0 5px #ff1744; }

.empty-msg { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #5d4037; opacity: 0.5; font-style: italic; }

/* --- GRID VIEW STYLES --- */
.grid-view-container { flex: 1; padding: 15px; overflow-y: auto; }
.inv-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(64px, 1fr));
    gap: 10px;
    align-content: start;
}

.item-slot {
  aspect-ratio: 1;
  border: 2px solid #4e342e; /* Mặc định */
  background: rgba(0, 0, 0, 0.4);
  border-radius: 6px;
  cursor: pointer; position: relative;
  transition: all 0.2s;
}
.item-slot:hover { transform: translateY(-2px); border-color: #bcaaa4; z-index: 5; }
.item-slot.selected { border-color: #ffd700; box-shadow: 0 0 15px rgba(255, 215, 0, 0.4); }
.item-slot.empty { opacity: 0.1; pointer-events: none; border-style: dashed; }

.slot-inner { width: 100%; height: 100%; padding: 5px; display: flex; justify-content: center; align-items: center; position: relative; }
.slot-inner img { max-width: 100%; max-height: 100%; object-fit: contain; }

/* Badges Grid */
.level-badge {
    position: absolute; bottom: 2px; right: 2px;
    background: rgba(0,0,0,0.8); color: #fff;
    font-size: 0.7rem; padding: 1px 4px;
    border-radius: 4px; border: 1px solid #555;
    z-index: 2;
}
.equipped-badge {
    position: absolute; top: 2px; left: 2px;
    font-size: 0.8rem; color: #66bb6a;
    background: rgba(0,0,0,0.7); border-radius: 50%;
    width: 18px; height: 18px; display: flex; align-items: center; justify-content: center;
}
.qty-tag { position: absolute; bottom: 2px; right: 2px; font-size: 9px; background: rgba(0,0,0,0.8); color: white; padding: 0 3px; border-radius: 2px; }

/* Rarity Borders */
.common { border-color: #9e9e9e; }
.uncommon { border-color: #66bb6a; }
.rare { border-color: #42a5f5; }
.epic { border-color: #ab47bc; }
.legendary { border-color: #ffca28; }
.mythic { border-color: #ff1744; box-shadow: 0 0 5px #ff1744; }
.border-mythic { border-color: #ff1744 !important; }

/* --- RIGHT PANEL --- */
.inv-detail-panel { padding: 20px; }
.detail-content { display: flex; flex-direction: column; height: 100%; }
.detail-image-box { 
    height: 160px; display: flex; justify-content: center; align-items: center;
    background: radial-gradient(circle, rgba(255,255,255,0.03) 0%, transparent 70%);
    margin-bottom: 20px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.05);
}
.big-preview { transform: scale(1.8); filter: drop-shadow(0 10px 20px rgba(0,0,0,0.8)); }

.stats-box { flex: 1; }
.stat-row { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px dashed rgba(255,255,255,0.1); }
.main-stat { margin-bottom: 12px; border-bottom: 1px solid rgba(255, 82, 82, 0.4); padding-bottom: 8px; }
.main-stat .stat-label { font-size: 1rem; color: #ef9a9a; font-weight: bold; }
.main-stat .stat-val { font-size: 1.1rem; color: #fff; font-weight: bold; text-shadow: 0 0 5px rgba(255,82,82,0.5); }
.sub-stat { color: #b0bec5; font-size: 0.9rem; }
.sub-val { color: #81d4fa; }
.dot { color: #5d4037; margin-right: 6px; font-size: 0.7rem; }

.desc-text { margin-top: 15px; font-style: italic; color: #a1887f; font-size: 0.85rem; line-height: 1.4; padding: 10px; background: rgba(0,0,0,0.2); border-left: 3px solid #5d4037; }
.durability-box { margin-top: 10px; padding: 8px; background: rgba(0,0,0,0.3); border-radius: 4px; }
.durability-header { display: flex; justify-content: space-between; font-size: 0.8rem; color: #ccc; margin-bottom: 4px; }
.durability-progress-bg { height: 6px; background: #263238; border-radius: 3px; overflow: hidden; }
.durability-progress-fill { height: 100%; transition: width 0.3s; }
.dur-high { background: #66bb6a; color: #66bb6a; }
.dur-low { background: #ef5350; color: #ef5350; }

.btn-action {
    width: 100%; padding: 12px; margin-top: 10px; border: none; font-weight: bold; cursor: pointer; text-transform: uppercase; letter-spacing: 1px;
    clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
    transition: 0.2s;
}
.btn-action:hover { filter: brightness(1.15); transform: translateY(-2px); }
.btn-equip { background: linear-gradient(to bottom, #2e7d32, #1b5e20); color: white; }
.btn-use { background: linear-gradient(to bottom, #1565c0, #0d47a1); color: white; }
.btn-repair { background: linear-gradient(to bottom, #ff8f00, #ff6f00); color: black; }
.btn-sell { background: linear-gradient(to bottom, #c62828, #b71c1c); color: white; }
.cost-mini { font-size: 0.75rem; display: block; font-weight: normal; margin-top: 2px; text-transform: none; color: #424242; }

/* --- MODAL STYLES --- */
.modal-overlay {
    position: fixed; inset: 0; z-index: 100;
    background: rgba(0,0,0,0.8); backdrop-filter: blur(5px);
    display: flex; justify-content: center; align-items: center;
}
.modal-content {
    background: #1a100d; border: 2px solid #5d4037; border-radius: 8px;
    width: 350px; padding: 0;
    box-shadow: 0 0 30px rgba(0,0,0,0.8);
    display: flex; flex-direction: column;
}
.modal-header {
    background: linear-gradient(90deg, #3e2723, #5d4037);
    padding: 12px 15px; display: flex; justify-content: space-between; align-items: center;
    border-bottom: 1px solid #8d6e63;
}
.modal-header h3 { margin: 0; font-family: 'Cinzel', serif; color: #ffcc80; }
.close-btn { background: none; border: none; color: #ccc; cursor: pointer; font-size: 1.2rem; }
.close-btn:hover { color: white; }

.modal-body { padding: 20px; }
.item-preview-mini {
    display: flex; gap: 10px; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px dashed #444;
}
.item-preview-mini img { width: 40px; height: 40px; border: 1px solid #555; background: #000; }
.mini-info { display: flex; flex-direction: column; justify-content: center; }
.mini-name { font-weight: bold; font-size: 0.9rem; }
.mini-qty { font-size: 0.75rem; color: #888; }

.input-group { margin-bottom: 15px; }
.input-group label { display: block; margin-bottom: 5px; color: #ccc; font-size: 0.85rem; }
.input-group input {
    width: 100%; background: #000; border: 1px solid #5d4037; color: #ffd700;
    padding: 8px; border-radius: 4px; font-weight: bold;
}
.input-group input:focus { border-color: #ffd700; outline: none; }

.price-summary { text-align: right; font-size: 0.9rem; margin-top: 10px; }
.highlight-gold { color: #ffcc80; font-size: 1.1rem; font-weight: bold; }
.fee-text { font-size: 0.7rem; color: #666; font-style: italic; }

.modal-actions {
    padding: 15px; border-top: 1px solid #333; display: flex; justify-content: flex-end; gap: 10px; background: rgba(0,0,0,0.2);
}
.btn-modal { padding: 8px 16px; border-radius: 4px; border: none; font-weight: bold; cursor: pointer; }
.btn-cancel { background: transparent; color: #888; border: 1px solid #444; }
.btn-cancel:hover { color: #ccc; border-color: #666; }
.btn-confirm-sell { background: linear-gradient(135deg, #c62828, #b71c1c); color: white; border: 1px solid #ef5350; }
.btn-confirm-sell:hover { filter: brightness(1.2); }

@media (max-width: 768px) {
  .inventory-layout { grid-template-columns: 1fr; height: auto; }
  .infinite-wheel-container, .grid-view-container { height: 320px; }
}
</style> -->


<template>
  <div class="page-container inventory-page wuxia-theme">
    <div class="bg-overlay"></div>

    <div class="inventory-layout">
      <div class="inv-list-panel glass-panel">
        <div class="panel-header">
          <div class="header-left">
            <h3><i class="fas fa-sack-dollar"></i> HÀNH NANG</h3>
          </div>
          <div class="header-right">
            <span class="slots-text">
              {{ inventoryStore.items.length }} / {{ authStore.user?.inventorySlots || 49 }}
            </span>
            <button class="btn-icon-action" @click="toggleViewMode" :title="viewMode === 'list' ? 'Xem dạng lưới' : 'Xem dạng cuộn'">
                <i :class="viewMode === 'list' ? 'fas fa-th' : 'fas fa-stream'"></i>
            </button>
            <button class="btn-add-slots" @click="expandSlots" title="Mở rộng (+7 ô)">
              <i class="fas fa-plus"></i>
            </button>
          </div>
        </div>

        <div class="filter-tabs">
          <button v-for="tab in tabs" :key="tab.id" 
            :class="{ active: currentTab === tab.id }" 
            @click="switchTab(tab.id)">
            {{ tab.label }}
          </button>
        </div>

        <div v-if="viewMode === 'list'" class="wheel-wrapper" :style="{ marginTop: OFFSET_TOP + 'px' }">
            <div 
                class="infinite-wheel-container" 
                ref="wheelContainer"
                @wheel.prevent="onWheel"
                @touchstart="onTouchStart"
                @touchmove="onTouchMove"
                @touchend="onTouchEnd"
                @mousedown="onMouseDown"
            >
              <div class="center-highlight-bar"></div>
              
              <div 
                v-for="itemObj in renderedItems" 
                :key="itemObj.virtualKey"
                class="wheel-item"
                :class="{ 
                    'active': itemObj.isActive, 
                    ['rarity-' + (itemObj.data.item.rarity || 'COMMON')]: true 
                }"
                :style="itemObj.style"
                @click="scrollToIndex(itemObj.index)"
              >
                <div class="wheel-inner">
                    <div class="icon-box">
                        <img :src="resolveItemImage(itemObj.data.item.imageUrl)" class="item-icon" />
                        <span v-if="itemObj.data.quantity > 1" class="qty-badge">{{ itemObj.data.quantity }}</span>
                    </div>
                    <div class="info-box">
                        <div class="name-row">
                            <span class="item-name">{{ itemObj.data.item.name }}</span>
                            <span v-if="itemObj.data.isMythic" class="mythic-txt">M{{ itemObj.data.mythicStars || 0 }}</span>
                            <span v-else-if="itemObj.data.enhanceLevel" class="enhance-txt">+{{ itemObj.data.enhanceLevel }}</span>
                        </div>
                        <div class="sub-row" v-if="itemObj.isActive">
                            <span class="tier-txt">Tier {{ itemObj.data.item.tier }}</span>
                            <span v-if="itemObj.data.isEquipped" class="status-equipped">
                                <i class="fas fa-shield-alt"></i> Đang dùng
                            </span>
                        </div>
                    </div>
                </div>
              </div>
              <div v-if="filteredItems.length === 0" class="empty-msg">
                <i class="fas fa-box-open"></i> Túi Trống
              </div>
            </div>
        </div>

        <div v-else class="grid-view-container custom-scroll">
            <div class="inv-grid">
                <div 
                    v-for="item in filteredItems" 
                    :key="item.userItemId"
                    class="item-slot"
                    :class="{
                        selected: selectedItem?.userItemId === item.userItemId,
                        'border-mythic': item.isMythic,
                        [getRarityClass(item)]: true,
                    }"
                    @click="selectItem(item)"
                >
                    <div class="slot-inner">
                        <img :src="resolveItemImage(item.item.imageUrl)" loading="lazy" />
                        <div class="level-badge" v-if="item.isMythic">M{{ item.mythicStars || 0 }}</div>
                        <div class="level-badge" v-else-if="item.enhanceLevel > 0">+{{ item.enhanceLevel }}</div>
                        <span v-if="item.quantity > 1" class="qty-tag">{{ item.quantity }}</span>
                        <div class="equipped-badge" v-if="item.isEquipped">
                            <i class="fas fa-shield-alt"></i>
                        </div>
                    </div>
                </div>
                <div 
                    v-for="n in Math.max(0, (authStore.user?.inventorySlots || 49) - filteredItems.length)" 
                    :key="'empty-' + n"
                    class="item-slot empty"
                ></div>
            </div>
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
                <i class="fas fa-khanda"></i> 
                {{ getStatLabel(selectedItem.mainStatType, selectedItem.item.type) }}
              </span>
              <span class="stat-val">
                +{{ formatNumber(selectedItem.mainStatValue) }}{{ isStatPercent(selectedItem.mainStatType) ? '%' : '' }}
              </span>
            </div>

            <div v-if="parsedSubStats.length > 0" class="sub-stats-container">
              <div v-for="(sub, idx) in parsedSubStats" :key="idx" class="stat-row sub-stat">
                <span class="dot">◆</span>
                <span class="sub-label">{{ getStatName(sub.code) }}</span>
                <span class="sub-val">
                    +{{ sub.value }}{{ (sub.isPercent || isStatPercent(sub.code)) ? '%' : '' }}
                </span>
              </div>
            </div>
             
            <div v-if="selectedItem.item.type === 'TOOL' && selectedItem.maxDurability" class="durability-box">
                <div class="durability-header">
                    <span><i class="fas fa-hammer"></i> Độ Bền</span>
                    </div>
                
                <div class="durability-progress-bg">
                    <div class="durability-progress-fill" 
                        :style="{ width: getDurabilityPercent(selectedItem) + '%' }"
                        :class="getDurabilityColorClass(selectedItem)">
                    </div>
                    
                    <div class="durability-percent-text">
                        {{ Math.floor((selectedItem.currentDurability / selectedItem.maxDurability) * 100) }}%
                    </div>
                </div>
            </div>

            <div class="desc-text">
              <i class="fas fa-quote-left"></i> {{ selectedItem.item.description }} <i class="fas fa-quote-right"></i>
            </div>
          </div>

          <div class="action-buttons">
            <button v-if="canEquip(selectedItem)" class="btn-action btn-equip" @click="handleEquip">
              <span>{{ selectedItem.isEquipped ? 'Gỡ Trang Bị' : 'Trang Bị' }}</span>
            </button>
            <button v-if="selectedItem.item.type === 'CONSUMABLE'" class="btn-action btn-use" @click="handleUse">
              <span>Sử Dụng</span>
            </button>
            
            <button v-if="needsRepair(selectedItem)" class="btn-action btn-repair" @click="handleRepair">
              <span>
                  <i class="fas fa-tools"></i> Sửa 
                  <span class="cost-mini">
                      ({{ formatNumber(getRepairCost(selectedItem).gold) }} Vàng
                      <span v-if="getRepairCost(selectedItem).coin > 0"> + {{ getRepairCost(selectedItem).coin }} Coin</span>)
                  </span>
              </span>
            </button>

            <button v-if="!selectedItem.isEquipped" class="btn-action btn-sell" @click="openSellModal(selectedItem)">
                <span><i class="fas fa-coins"></i> Treo Bán</span>
            </button>
          </div>
        </div>
        
        <div v-else class="empty-detail">
          <div class="empty-icon-glow"><i class="fas fa-box-open"></i></div>
          <p>Chọn vật phẩm để xem chi tiết</p>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="showSellModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>ĐỊNH GIÁ BÁN</h3>
            <button class="close-btn" @click="showSellModal = false"><i class="fas fa-times"></i></button>
          </div>
          
          <div class="modal-body">
            <div class="item-preview-mini">
               <img :src="resolveItemImage(sellItemData?.item.imageUrl)" />
               <div class="mini-info">
                 <span class="mini-name" :class="'text-' + (sellItemData?.item.rarity?.toLowerCase() || 'common')">
                   {{ sellItemData?.item.name }}
                 </span>
                 <span class="mini-qty">Số lượng trong túi: {{ sellItemData?.quantity }}</span>
               </div>
            </div>

            <div class="input-group">
                <label>Số lượng bán:</label>
                <input type="number" v-model.number="sellForm.quantity" min="1" :max="sellItemData?.quantity || 1" />
            </div>

            <div class="input-group">
                <label>Đơn giá (Vàng):</label>
                <input type="number" v-model.number="sellForm.price" min="1" placeholder="Nhập giá mong muốn..." />
            </div>

            <div class="price-summary">
                <p>Tổng thu về: <span class="highlight-gold">{{ formatNumber(sellForm.price * sellForm.quantity) }}</span> <i class="fas fa-coins"></i></p>
                <p class="fee-text">(Phí sàn: Miễn phí)</p>
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn-modal btn-cancel" @click="showSellModal = false">Hủy</button>
            <button class="btn-modal btn-confirm-sell" @click="confirmSell">
                <i class="fas fa-gavel"></i> Đăng Bán
            </button>
          </div>
        </div>
      </div>
    </transition>

    <GameToast ref="toast" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import { useInventoryStore } from '@/stores/inventoryStore';
import { useAuthStore } from '@/stores/authStore';
import { useMarketStore } from '@/stores/marketStore'; 
import { resolveItemImage } from '@/utils/assetHelper';
import GameToast from '@/components/GameToast.vue';

// --- CẤU HÌNH VỊ TRÍ DANH SÁCH ---
// Thay đổi số này để chỉnh danh sách LÊN (-) hoặc XUỐNG (+)
const OFFSET_TOP = 0; 

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const marketStore = useMarketStore(); 
const toast = ref(null);

const currentTab = ref('ALL');
const selectedItem = ref(null);
const viewMode = ref('list'); 

// --- STATE MODAL BÁN ---
const showSellModal = ref(false);
const sellItemData = ref(null);
const sellForm = ref({ price: 0, quantity: 1 });

// --- CONFIG WHEEL ---
const ITEM_HEIGHT = 50; 
const VISIBLE_COUNT = 9; 
const scrollY = ref(0);
const wheelContainer = ref(null);

// Physics vars
let isDragging = false;
let startY = 0;
let lastTouchY = 0;
let velocity = 0;
let animationFrameId = null;
let lastTime = 0;
let targetScrollY = null; 

const tabs = [
  { id: 'ALL', label: 'Tất Cả' },
  { id: 'EQUIP', label: 'Trang Bị' },
  { id: 'TOOL', label: 'Công Cụ' }, 
  { id: 'CONSUMABLE', label: 'Tiêu Hao' },
  { id: 'MATERIAL', label: 'Nguyên Liệu' }
];

const filteredItems = computed(() => {
  let items = inventoryStore.items || [];
  if (currentTab.value === 'EQUIP') return items.filter(i => ['WEAPON', 'ARMOR', 'RING', 'NECKLACE', 'BOOTS', 'HELMET'].includes(i.item.type));
  if (currentTab.value === 'TOOL') return items.filter(i => i.item.type === 'TOOL');
  if (currentTab.value !== 'ALL') return items.filter(i => i.item.type === currentTab.value);
  return items;
});

const toggleViewMode = () => {
    viewMode.value = viewMode.value === 'list' ? 'grid' : 'list';
    if (viewMode.value === 'list') {
        scrollY.value = 0;
    }
};

// --- LOGIC AUTO SELECT ---
watch(scrollY, (newVal) => {
    if (viewMode.value !== 'list') return;
    const items = filteredItems.value;
    if (!items.length) return;
    const centerIndex = Math.round(newVal / ITEM_HEIGHT);
    const total = items.length;
    const realIndex = ((centerIndex % total) + total) % total;
    const targetItem = items[realIndex];
    if (selectedItem.value?.userItemId !== targetItem?.userItemId) {
        selectedItem.value = targetItem;
    }
});

watch(filteredItems, (newItems) => {
    if (newItems.length > 0 && !selectedItem.value) {
        selectedItem.value = newItems[0];
    }
}, { immediate: true });

// --- RENDER LOGIC ---
const renderedItems = computed(() => {
    if (viewMode.value !== 'list') return [];
    const items = filteredItems.value;
    const total = items.length;
    if (total === 0) return [];

    // Lấy chiều cao container
    const containerHeight = wheelContainer.value ? wheelContainer.value.clientHeight : 350;
    const centerY = containerHeight / 2;

    const result = [];
    const baseIndex = Math.floor(scrollY.value / ITEM_HEIGHT);
    const buffer = Math.ceil(VISIBLE_COUNT / 2);

    for (let i = baseIndex - buffer; i <= baseIndex + buffer; i++) {
        let dataIndex = ((i % total) + total) % total;
        const itemData = items[dataIndex];
        
        const itemY = i * ITEM_HEIGHT - scrollY.value; 
        const distSlots = Math.abs(itemY / ITEM_HEIGHT);

        let scale = 1;
        let opacity = 1;
        let rotateX = 0;
        let brightness = 1;
        let zIndex = 10;
        let blurAmount = 0;

        if (distSlots <= 3.5) {
            if (distSlots < 0.5) {
                scale = 1.05;
                opacity = 1;
                brightness = 1.2;
                zIndex = 100;
                blurAmount = 0;
            } else {
                const ratio = 1 - (distSlots / 4);
                scale = 0.9 + (ratio * 0.1); 
                opacity = 0.3 + (ratio * 0.7);
                brightness = 0.5 + (ratio * 0.5);
                rotateX = -itemY * 0.15;
                zIndex = Math.floor((4 - distSlots) * 10);
                blurAmount = (distSlots - 0.5) * 1.5; 
            }
        } else {
            opacity = 0;
            scale = 0.8;
            blurAmount = 5;
        }

        const isActive = distSlots < 0.5;

        const style = {
            transform: `translateY(${centerY + itemY - (ITEM_HEIGHT/2)}px) perspective(500px) rotateX(${rotateX}deg) scale(${scale})`,
            opacity: opacity,
            zIndex: zIndex,
            filter: `brightness(${brightness}) blur(${blurAmount}px)`,
            visibility: opacity <= 0.05 ? 'hidden' : 'visible'
        };

        result.push({
            virtualKey: `${itemData.userItemId}_${i}`,
            index: i,
            data: itemData,
            style,
            isActive
        });
    }
    return result;
});

// --- SCROLL HELPERS ---
const scrollToIndex = (virtualIndex) => {
    targetScrollY = virtualIndex * ITEM_HEIGHT;
    cancelAnimationFrame(animationFrameId);
    animateToTarget();
};

const animateToTarget = () => {
    if (targetScrollY === null) return;
    const diff = targetScrollY - scrollY.value;
    if (Math.abs(diff) > 0.5) {
        scrollY.value += diff * 0.1;
        animationFrameId = requestAnimationFrame(animateToTarget);
    } else {
        scrollY.value = targetScrollY;
        targetScrollY = null;
    }
};

const inertiaLoop = (time) => {
    if (!lastTime) lastTime = time;
    const delta = time - lastTime;
    lastTime = time;
    if (Math.abs(velocity) > 0.1) {
        scrollY.value -= velocity * 16;
        velocity *= 0.92;
        animationFrameId = requestAnimationFrame(inertiaLoop);
    } else {
        velocity = 0;
        snapToGrid();
    }
};

const snapToGrid = () => {
    const targetY = Math.round(scrollY.value / ITEM_HEIGHT) * ITEM_HEIGHT;
    const diff = targetY - scrollY.value;
    if (Math.abs(diff) > 0.5) {
        scrollY.value += diff * 0.15;
        animationFrameId = requestAnimationFrame(snapToGrid);
    } else {
        scrollY.value = targetY;
        cancelAnimationFrame(animationFrameId);
    }
};

const onWheel = (e) => {
    cancelAnimationFrame(animationFrameId);
    targetScrollY = null;
    velocity = 0;
    scrollY.value += e.deltaY * 0.4;
    snapToGrid();
};

const onMouseDown = (e) => {
    isDragging = true;
    startY = e.clientY;
    lastTouchY = startY;
    velocity = 0;
    targetScrollY = null;
    cancelAnimationFrame(animationFrameId);
    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
};
const onMouseMove = (e) => {
    if (!isDragging) return;
    const delta = lastTouchY - e.clientY;
    scrollY.value += delta;
    lastTouchY = e.clientY;
};
const onMouseUp = (e) => {
    isDragging = false;
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
    velocity = (startY - e.clientY) * 0.05;
    lastTime = 0;
    inertiaLoop(performance.now());
};

const onTouchStart = (e) => {
    isDragging = true;
    startY = e.touches[0].clientY;
    lastTouchY = startY;
    velocity = 0;
    targetScrollY = null;
    cancelAnimationFrame(animationFrameId);
};
const onTouchMove = (e) => {
    if (!isDragging) return;
    const delta = lastTouchY - e.touches[0].clientY;
    scrollY.value += delta;
    lastTouchY = e.touches[0].clientY;
};
const onTouchEnd = (e) => {
    isDragging = false;
    velocity = (startY - lastTouchY) * 0.05;
    lastTime = 0;
    inertiaLoop(performance.now());
};

const switchTab = (tabId) => {
    currentTab.value = tabId;
    scrollY.value = 0;
    velocity = 0;
};

// --- HELPERS ---
const selectItem = (item) => { selectedItem.value = item; };

const parsedSubStats = computed(() => {
  if (!selectedItem.value || !selectedItem.value.subStats) return [];
  try { return JSON.parse(selectedItem.value.subStats); } catch (e) { return []; }
});

const isStatPercent = (code) => {
    if(!code) return false;
    return code.endsWith('_PERCENT') || ['CRIT_RATE', 'CRIT_DMG'].includes(code);
};

const getStatName = (code) => {
    const dict = { 
        "ATK": "Công Lực", "ATK_FLAT": "Công Lực", "ATK_PERCENT": "Công Lực", 
        "DEF": "Hộ Thể", "DEF_FLAT": "Hộ Thể", "DEF_PERCENT": "Hộ Thể",
        "HP": "Sinh Lực", "HP_FLAT": "Sinh Lực", "HP_PERCENT": "Sinh Lực",
        "SPEED": "Thân Pháp", 
        "CRIT": "Bạo Kích", "CRIT_RATE": "Bạo Kích", 
        "CRIT_DMG": "Sát Thương Bạo",
        "LUCK": "May Mắn", "DURABILITY": "Độ Bền"
    };
    return dict[code] || code;
};

const getStatLabel = (mainStatType, itemType) => {
    if (mainStatType && typeof mainStatType === 'string') {
        return getStatName(mainStatType);
    }
    if (itemType) {
        if (itemType === 'WEAPON') return "Công Lực";
        if (['ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE'].includes(itemType)) return "Hộ Thể";
        if (itemType === 'TOOL') return "Hiệu Suất";
    }
    return "Sức Mạnh";
};

const canEquip = (uItem) => ['WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'TOOL'].includes(uItem.item.type);

// --- HANDLERS ---
const handleEquip = async () => {
  if (!selectedItem.value) return;
  try {
    if (selectedItem.value.isEquipped) {
      await inventoryStore.unequipItem(selectedItem.value.userItemId);
      toast.value?.show("Đã tháo trang bị!", "success");
    } else {
      await inventoryStore.equipItem(selectedItem.value.userItemId);
      toast.value?.show("Đã trang bị thành công!", "success");
    }
    const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
    if(fresh) selectedItem.value = fresh;
  } catch (err) { toast.value?.show(err, "error"); }
};

const handleUse = async () => { 
    if(!selectedItem.value) return;
    try {
        const msg = await inventoryStore.useItem(selectedItem.value.userItemId);
        toast.value?.show(msg, "success");
    } catch(e) { toast.value?.show(e, "error"); }
};

const openSellModal = (item) => {
    if(item.isEquipped) {
        toast.value?.show("Phải gỡ trang bị mới được bán!", "error");
        return;
    }
    sellItemData.value = item;
    sellForm.value = { 
        price: item.item.basePrice ? Math.ceil(item.item.basePrice * 1.5) : 100, 
        quantity: 1 
    };
    showSellModal.value = true;
};

const confirmSell = async () => {
    if(!sellItemData.value) return;
    if(sellForm.value.price <= 0) {
        toast.value?.show("Giá bán phải lớn hơn 0!", "error");
        return;
    }
    if(sellForm.value.quantity > sellItemData.value.quantity) {
        toast.value?.show("Không đủ số lượng!", "error");
        return;
    }

    try {
        await marketStore.createListing(
            sellItemData.value.userItemId,
            sellForm.value.price,
            sellForm.value.quantity
        );
        toast.value?.show("Đã treo bán thành công!", "success");
        showSellModal.value = false;
        await inventoryStore.fetchInventory(); 
        selectedItem.value = null;
    } catch (e) {
        toast.value?.show(typeof e === 'string' ? e : "Lỗi khi đăng bán", "error");
    }
};

const expandSlots = async () => {
    const currentSlots = authStore.user?.inventorySlots || 49;
    if (currentSlots >= 210) { toast.value?.show("Đã đạt giới hạn tối đa!", "error"); return; }
    const cost = Math.floor((currentSlots - 49) / 7) + 1;
    if(!confirm(`Mở rộng thêm 7 ô chứa?\nChi phí: ${cost} Echo Coin`)) return;
    if (authStore.wallet.echoCoin < cost) { toast.value?.show(`Thiếu Echo Coin! Cần ${cost}.`, "error"); return; }
    try {
        const msg = await inventoryStore.expandInventory();
        toast.value?.show(msg || "Mở rộng thành công!", "success");
    } catch (e) { toast.value?.show(typeof e === 'string' ? e : "Lỗi", "error"); }
};

const getDurabilityPercent = (uItem) => (!uItem.maxDurability) ? 100 : Math.max(0, Math.min(100, (uItem.currentDurability / uItem.maxDurability) * 100));
const getDurabilityColorClass = (uItem) => { const pct = getDurabilityPercent(uItem); return pct < 30 ? 'dur-low' : 'dur-high'; };
const needsRepair = (uItem) => uItem.item.type === 'TOOL' && uItem.maxDurability && uItem.currentDurability < uItem.maxDurability;

const getRepairCost = (uItem) => {
    if (!uItem.maxDurability || !uItem.currentDurability) return { gold: 0, coin: 0 };
    const missing = uItem.maxDurability - uItem.currentDurability;
    if (missing <= 0) return { gold: 0, coin: 0 };

    let goldPerPoint = 10;
    let coinPerPoint = 0;
    const rarity = uItem.isMythic ? 'MYTHIC' : (uItem.item.rarity || 'COMMON');

    switch (rarity) {
        case 'COMMON': case 'UNCOMMON': goldPerPoint = 10; break;
        case 'RARE': goldPerPoint = 50; break;
        case 'EPIC': goldPerPoint = 200; coinPerPoint = 0.1; break;
        case 'LEGENDARY': case 'MYTHIC': goldPerPoint = 1000; coinPerPoint = 1.0; break;
        default: goldPerPoint = 10;
    }
    return {
        gold: missing * goldPerPoint,
        coin: parseFloat((missing * coinPerPoint).toFixed(1))
    };
};

const handleRepair = async () => {
    if (!selectedItem.value) return;
    const cost = getRepairCost(selectedItem.value);
    let confirmMsg = `Sửa chữa vật phẩm này?\nChi phí: ${formatNumber(cost.gold)} Vàng`;
    if (cost.coin > 0) confirmMsg += ` + ${cost.coin} Echo Coin`;

    if(!confirm(confirmMsg)) return;

    try {
        await inventoryStore.repairItem(selectedItem.value.userItemId);
        toast.value?.show("Đã sửa thành công!", "success");
        const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId);
        if(fresh) selectedItem.value = fresh;
    } catch(e) { toast.value?.show(e, "error"); }
};

const getRarityClass = (item) => item.isMythic ? "mythic" : (item.item.rarity ? item.item.rarity.toLowerCase() : "common");
const formatNumber = (num) => new Intl.NumberFormat().format(num || 0);

onMounted(async () => {
  await inventoryStore.fetchInventory();
});
onUnmounted(() => {
    cancelAnimationFrame(animationFrameId);
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
});
</script>

<style scoped>
/* --- BASE THEME --- */
.wuxia-theme {
  background-color: #050505;
  background-image: radial-gradient(circle at 50% 30%, #1a100d 0%, #000000 90%);
  color: #e0d4b9;
  font-family: 'Noto Serif TC', serif;
  min-height: 100vh;
  padding: 20px;
  position: relative;
  overflow: hidden;
  user-select: none;
}
.bg-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: url('https://www.transparenttextures.com/patterns/dark-leather.png'); opacity: 0.3;
  pointer-events: none; z-index: 0;
}
.inventory-layout {
  position: relative; z-index: 1;
  display: grid; grid-template-columns: 1.2fr 1fr;
  gap: 30px;
  max-width: 1100px; margin: 0 auto; height: 85vh;
}

/* --- GLASS PANEL --- */
.glass-panel {
  background: rgba(30, 20, 15, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(139, 94, 60, 0.3);
  box-shadow: 0 10px 40px rgba(0,0,0,0.8);
  border-radius: 12px;
  display: flex; flex-direction: column; overflow: hidden;
}

/* Header */
.panel-header {
  background: linear-gradient(180deg, rgba(62, 39, 35, 0.95) 0%, rgba(46, 30, 25, 0.9) 100%);
  padding: 10px 16px;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(139, 94, 60, 0.4);
  z-index: 20;
}
.panel-header h3 { margin: 0; color: #ffecb3; font-size: 1.1rem; font-weight: bold; letter-spacing: 1px; }
.header-right { display: flex; align-items: center; gap: 8px; }
.slots-text { font-size: 0.85rem; color: #a1887f; }

/* Buttons in Header */
.btn-add-slots, .btn-icon-action {
    border: 1px solid #66bb6a;
    color: white; width: 26px; height: 26px; border-radius: 4px;
    cursor: pointer; display: flex; align-items: center; justify-content: center;
    transition: transform 0.2s; font-size: 0.8rem; margin-left: 5px;
}
.btn-add-slots { background: linear-gradient(135deg, #43a047, #2e7d32); }
.btn-icon-action { background: #3e2723; border-color: #8d6e63; color: #ffecb3; }
.btn-add-slots:hover, .btn-icon-action:hover { transform: scale(1.1); filter: brightness(1.2); }

/* Tabs */
.filter-tabs { display: flex; background: rgba(0,0,0,0.4); padding: 4px; gap: 2px; border-bottom: 1px solid rgba(255,255,255,0.05); }
.filter-tabs button {
    flex: 1; padding: 6px; border: none; background: transparent; color: #8d6e63; cursor: pointer;
    font-size: 0.8rem; border-radius: 4px; transition: 0.2s;
}
.filter-tabs button.active { background: rgba(93, 64, 55, 0.6); color: #ffca28; font-weight: bold; }

/* --- WHEEL WRAPPER & CONTAINER --- */
.wheel-wrapper {
    flex: 1;
    display: flex; justify-content: center; align-items: center;
    overflow: hidden;
}

.infinite-wheel-container {
    width: 100%;
    position: relative;
    overflow: hidden;
    background: radial-gradient(circle at center, rgba(62, 39, 35, 0.2) 0%, transparent 80%);
    cursor: grab;
    
    /* Cố định chiều cao cho vòng quay */
    height: 350px; 
    
    mask-image: linear-gradient(to bottom, transparent 0%, black 20%, black 80%, transparent 100%);
    -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 20%, black 80%, transparent 100%);
}
.infinite-wheel-container:active { cursor: grabbing; }

.center-highlight-bar {
    position: absolute; 
    top: 50%; 
    left: 0; right: 0; height: 56px; 
    transform: translateY(-50%);
    background: linear-gradient(90deg, transparent 0%, rgba(255, 202, 40, 0.08) 20%, rgba(255, 202, 40, 0.08) 80%, transparent 100%);
    border-top: 1px solid rgba(255, 202, 40, 0.3);
    border-bottom: 1px solid rgba(255, 202, 40, 0.3);
    box-shadow: 0 0 15px rgba(255, 202, 40, 0.1);
    pointer-events: none; z-index: 0;
}

.wheel-item {
    position: absolute; left: 0; right: 0; top: 0; height: 50px;
    display: flex; align-items: center; justify-content: center;
    padding: 0 10px;
    will-change: transform, opacity, filter;
    cursor: pointer;
    transition: filter 0.2s;
}

.wheel-inner {
    width: 90%; height: 44px;
    display: flex; align-items: center;
    background: rgba(46, 30, 25, 0.6);
    border: 1px solid rgba(139, 94, 60, 0.3);
    border-radius: 6px;
    padding: 0 10px;
    transition: background 0.2s, border 0.2s;
}

.wheel-item.active .wheel-inner {
    background: linear-gradient(90deg, rgba(62, 39, 35, 0.9) 0%, rgba(93, 64, 55, 1) 50%, rgba(62, 39, 35, 0.9) 100%);
    border-color: #ffca28;
    transform: scale(1.05);
}

.icon-box { position: relative; margin-right: 12px; }
.item-icon { width: 32px; height: 32px; object-fit: contain; filter: drop-shadow(0 2px 2px black); }
.qty-badge { position: absolute; bottom: -2px; right: -4px; background: #3e2723; color: #fff; font-size: 0.6rem; padding: 0 3px; border-radius: 2px; border: 1px solid #5d4037; }

.info-box { flex: 1; display: flex; flex-direction: column; justify-content: center; overflow: hidden; }
.name-row { display: flex; align-items: center; gap: 6px; }
.item-name { font-weight: bold; font-size: 0.9rem; color: #e0d4b9; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 150px; }
.active .item-name { color: #fff; text-shadow: 0 0 5px rgba(255,255,255,0.4); }

.enhance-txt { color: #ff5252; font-size: 0.8rem; font-weight: bold; }
.mythic-txt { color: #ff1744; font-size: 0.8rem; font-weight: bold; text-shadow: 0 0 2px #ff1744; }
.sub-row { display: flex; justify-content: space-between; align-items: center; font-size: 0.7rem; color: #a1887f; margin-top: 1px; }
.status-equipped { color: #66bb6a; display: flex; align-items: center; gap: 3px; }

/* Rarity */
.rarity-COMMON .item-name { color: #bdbdbd; }
.rarity-UNCOMMON .item-name { color: #81c784; }
.rarity-RARE .item-name { color: #64b5f6; }
.rarity-EPIC .item-name { color: #ba68c8; }
.rarity-LEGENDARY .item-name { color: #ffca28; }
.rarity-MYTHIC .item-name { color: #ff1744; text-shadow: 0 0 5px #ff1744; }

.empty-msg { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #5d4037; opacity: 0.5; font-style: italic; }

/* --- GRID VIEW STYLES --- */
.grid-view-container { flex: 1; padding: 15px; overflow-y: auto; }
.inv-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(64px, 1fr));
    gap: 10px;
    align-content: start;
}

.item-slot {
  aspect-ratio: 1;
  border: 2px solid #4e342e;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 6px;
  cursor: pointer; position: relative;
  transition: all 0.2s;
}
.item-slot:hover { transform: translateY(-2px); border-color: #bcaaa4; z-index: 5; }
.item-slot.selected { border-color: #ffd700; box-shadow: 0 0 15px rgba(255, 215, 0, 0.4); }
.item-slot.empty { opacity: 0.1; pointer-events: none; border-style: dashed; }

.slot-inner { width: 100%; height: 100%; padding: 5px; display: flex; justify-content: center; align-items: center; position: relative; }
.slot-inner img { max-width: 100%; max-height: 100%; object-fit: contain; }

/* Badges Grid */
.level-badge {
    position: absolute; bottom: 2px; right: 2px;
    background: rgba(0,0,0,0.8); color: #fff;
    font-size: 0.7rem; padding: 1px 4px;
    border-radius: 4px; border: 1px solid #555;
    z-index: 2;
}
.equipped-badge {
    position: absolute; top: 2px; left: 2px;
    font-size: 0.8rem; color: #66bb6a;
    background: rgba(0,0,0,0.7); border-radius: 50%;
    width: 18px; height: 18px; display: flex; align-items: center; justify-content: center;
}
.qty-tag { position: absolute; bottom: 2px; right: 2px; font-size: 9px; background: rgba(0,0,0,0.8); color: white; padding: 0 3px; border-radius: 2px; }

/* Rarity Borders */
.common { border-color: #9e9e9e; }
.uncommon { border-color: #66bb6a; }
.rare { border-color: #42a5f5; }
.epic { border-color: #ab47bc; }
.legendary { border-color: #ffca28; }
.mythic { border-color: #ff1744; box-shadow: 0 0 5px #ff1744; }
.border-mythic { border-color: #ff1744 !important; }

/* --- RIGHT PANEL --- */
.inv-detail-panel { padding: 20px; }
.detail-content { display: flex; flex-direction: column; height: 100%; }
.detail-image-box { 
    height: 160px; display: flex; justify-content: center; align-items: center;
    background: radial-gradient(circle, rgba(255,255,255,0.03) 0%, transparent 70%);
    margin-bottom: 20px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.05);
}
.big-preview { transform: scale(1.8); filter: drop-shadow(0 10px 20px rgba(0,0,0,0.8)); }

.stats-box { flex: 1; }
.stat-row { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px dashed rgba(255,255,255,0.1); }
.main-stat { margin-bottom: 12px; border-bottom: 1px solid rgba(255, 82, 82, 0.4); padding-bottom: 8px; }
.main-stat .stat-label { font-size: 1rem; color: #ef9a9a; font-weight: bold; }
.main-stat .stat-val { font-size: 1.1rem; color: #fff; font-weight: bold; text-shadow: 0 0 5px rgba(255,82,82,0.5); }
.sub-stat { color: #b0bec5; font-size: 0.9rem; }
.sub-val { color: #81d4fa; }
.dot { color: #5d4037; margin-right: 6px; font-size: 0.7rem; }

.desc-text { margin-top: 15px; font-style: italic; color: #a1887f; font-size: 0.85rem; line-height: 1.4; padding: 10px; background: rgba(0,0,0,0.2); border-left: 3px solid #5d4037; }
.durability-box { margin-top: 10px; padding: 8px; background: rgba(0,0,0,0.3); border-radius: 4px; }
.durability-header { display: flex; justify-content: space-between; font-size: 0.8rem; color: #ccc; margin-bottom: 4px; }

/* [UPDATE] DURABILITY BAR STYLES */
.durability-progress-bg { 
    height: 18px; /* Tăng độ cao lên chút để chứa text */
    background: #263238; border-radius: 9px; overflow: hidden; 
    position: relative; /* Để text overlay căn theo cái này */
    box-shadow: inset 0 1px 3px rgba(0,0,0,0.8);
}
.durability-progress-fill { 
    height: 100%; transition: width 0.3s; 
    z-index: 1; /* Lớp dưới */
    position: absolute; left: 0; top: 0;
}

/* Text overlay nằm đè lên thanh */
.durability-percent-text {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    display: flex; align-items: center; justify-content: center;
    font-size: 0.7rem; font-weight: bold; color: #fff;
    text-shadow: 0 0 3px rgba(0,0,0,0.8);
    z-index: 2; /* Lớp trên */
}

.dur-high { background: #66bb6a; color: #66bb6a; }
.dur-low { background: #ef5350; color: #ef5350; }

.btn-action {
    width: 100%; padding: 12px; margin-top: 10px; border: none; font-weight: bold; cursor: pointer; text-transform: uppercase; letter-spacing: 1px;
    clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
    transition: 0.2s;
}
.btn-action:hover { filter: brightness(1.15); transform: translateY(-2px); }
.btn-equip { background: linear-gradient(to bottom, #2e7d32, #1b5e20); color: white; }
.btn-use { background: linear-gradient(to bottom, #1565c0, #0d47a1); color: white; }
.btn-repair { background: linear-gradient(to bottom, #ff8f00, #ff6f00); color: black; }
.btn-sell { background: linear-gradient(to bottom, #c62828, #b71c1c); color: white; }
.cost-mini { font-size: 0.75rem; display: block; font-weight: normal; margin-top: 2px; text-transform: none; color: #424242; }

/* --- MODAL STYLES --- */
.modal-overlay {
    position: fixed; inset: 0; z-index: 100;
    background: rgba(0,0,0,0.8); backdrop-filter: blur(5px);
    display: flex; justify-content: center; align-items: center;
}
.modal-content {
    background: #1a100d; border: 2px solid #5d4037; border-radius: 8px;
    width: 350px; padding: 0;
    box-shadow: 0 0 30px rgba(0,0,0,0.8);
    display: flex; flex-direction: column;
}
.modal-header {
    background: linear-gradient(90deg, #3e2723, #5d4037);
    padding: 12px 15px; display: flex; justify-content: space-between; align-items: center;
    border-bottom: 1px solid #8d6e63;
}
.modal-header h3 { margin: 0; font-family: 'Cinzel', serif; color: #ffcc80; }
.close-btn { background: none; border: none; color: #ccc; cursor: pointer; font-size: 1.2rem; }
.close-btn:hover { color: white; }

.modal-body { padding: 20px; }
.item-preview-mini {
    display: flex; gap: 10px; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px dashed #444;
}
.item-preview-mini img { width: 40px; height: 40px; border: 1px solid #555; background: #000; }
.mini-info { display: flex; flex-direction: column; justify-content: center; }
.mini-name { font-weight: bold; font-size: 0.9rem; }
.mini-qty { font-size: 0.75rem; color: #888; }

.input-group { margin-bottom: 15px; }
.input-group label { display: block; margin-bottom: 5px; color: #ccc; font-size: 0.85rem; }
.input-group input {
    width: 100%; background: #000; border: 1px solid #5d4037; color: #ffd700;
    padding: 8px; border-radius: 4px; font-weight: bold;
}
.input-group input:focus { border-color: #ffd700; outline: none; }

.price-summary { text-align: right; font-size: 0.9rem; margin-top: 10px; }
.highlight-gold { color: #ffcc80; font-size: 1.1rem; font-weight: bold; }
.fee-text { font-size: 0.7rem; color: #666; font-style: italic; }

.modal-actions {
    padding: 15px; border-top: 1px solid #333; display: flex; justify-content: flex-end; gap: 10px; background: rgba(0,0,0,0.2);
}
.btn-modal { padding: 8px 16px; border-radius: 4px; border: none; font-weight: bold; cursor: pointer; }
.btn-cancel { background: transparent; color: #888; border: 1px solid #444; }
.btn-cancel:hover { color: #ccc; border-color: #666; }
.btn-confirm-sell { background: linear-gradient(135deg, #c62828, #b71c1c); color: white; border: 1px solid #ef5350; }
.btn-confirm-sell:hover { filter: brightness(1.2); }

@media (max-width: 768px) {
  .inventory-layout { grid-template-columns: 1fr; height: auto; }
  .infinite-wheel-container, .grid-view-container { height: 320px; }
}
</style>