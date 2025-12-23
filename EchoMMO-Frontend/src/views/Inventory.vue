<template>
  <div class="page-container inventory-page wuxia-theme">
    <div class="bg-overlay"></div>

    <div class="inventory-layout">
      <div class="inv-list-panel wood-panel">
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

        <div v-if="viewMode === 'list'" class="wheel-wrapper">
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

      <div class="inv-detail-panel wood-panel detail-mode">
        <div v-if="selectedItem" class="detail-content custom-scroll">
          <div class="detail-header">
            <h2 :class="'text-rarity-' + (selectedItem.item.rarity || 'COMMON')">
              {{ selectedItem.item.name }}
            </h2>
            <div class="item-meta">
              <span class="type-badge">{{ selectedItem.item.type }}</span>
              <span class="tier-badge">Phẩm {{ selectedItem.item.tier || 1 }}</span>
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
                      ({{ formatNumber(getRepairCost(selectedItem).gold) }} Vàng)
                  </span>
              </span>
            </button>

            <button v-if="!selectedItem.isEquipped" class="btn-action btn-market" @click="openMarketModal(selectedItem)">
                <span><i class="fas fa-store"></i> Treo Bán (Chợ)</span>
            </button>

            <button v-if="!selectedItem.isEquipped" class="btn-action btn-sell" @click="openSellModal(selectedItem)">
                <span><i class="fas fa-coins"></i> Bán Shop (NPC)</span>
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
        <div class="modal-content wood-panel">
          <div class="modal-header">
            <h3>BÁN CHO SHOP</h3>
            <button class="close-btn" @click="showSellModal = false"><i class="fas fa-times"></i></button>
          </div>
          
          <div class="modal-body">
            <div class="item-preview-mini">
               <img :src="resolveItemImage(sellItemData?.item.imageUrl)" />
               <div class="mini-info">
                 <span class="mini-name" :class="'text-' + (sellItemData?.item.rarity?.toLowerCase() || 'common')">
                   {{ sellItemData?.item.name }}
                 </span>
                 <span class="mini-qty">Hiện có: {{ sellItemData?.quantity }}</span>
               </div>
            </div>

            <div class="input-group">
                <label>Số lượng bán:</label>
                <input type="number" v-model.number="sellForm.quantity" min="1" :max="sellItemData?.quantity || 1" />
            </div>

            <div class="price-summary">
                <p>Giá thu mua: <span class="highlight-gold">{{ formatNumber((sellItemData?.item.basePrice ? Math.ceil(sellItemData.item.basePrice * 0.5) : 0) * sellForm.quantity) }}</span> <i class="fas fa-coins"></i></p>
                <small class="hint-text">(NPC thu mua 50% giá gốc)</small>
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn-modal btn-cancel" @click="showSellModal = false">Hủy</button>
            <button class="btn-modal btn-confirm-sell" @click="confirmSell">
                <i class="fas fa-check"></i> Bán Ngay
            </button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="showMarketModal" class="modal-overlay">
        <div class="modal-content wood-panel market-modal">
          <div class="modal-header market-header">
            <h3><i class="fas fa-balance-scale"></i> NIÊM YẾT GIÁ</h3>
            <button class="close-btn" @click="showMarketModal = false"><i class="fas fa-times"></i></button>
          </div>
          
          <div class="modal-body">
            <div class="item-preview-mini">
               <img :src="resolveItemImage(marketItemData?.item.imageUrl)" />
               <div class="mini-info">
                 <span class="mini-name" :class="'text-' + (marketItemData?.item.rarity?.toLowerCase() || 'common')">
                   {{ marketItemData?.item.name }}
                 </span>
                 <span class="mini-qty">Hiện có: {{ marketItemData?.quantity }}</span>
               </div>
            </div>

            <div class="input-group">
                <label>Số lượng treo:</label>
                <input type="number" v-model.number="marketForm.quantity" min="1" :max="marketItemData?.quantity || 1" />
            </div>

            <div class="input-group">
                <label>Đơn giá mong muốn (mỗi cái):</label>
                <div class="price-input-wrapper">
                    <input type="number" v-model.number="marketForm.price" min="1" placeholder="Nhập giá..." />
                    <i class="fas fa-coins gold-icon-input"></i>
                </div>
            </div>

            <div class="price-summary market-summary">
                <p>Tổng thu dự kiến: <span class="highlight-gold">{{ formatNumber(marketForm.price * marketForm.quantity) }}</span> <i class="fas fa-coins"></i></p>
                <small class="hint-text text-market">Phí sàn giao dịch: 0% (Sự kiện)</small>
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn-modal btn-cancel" @click="showMarketModal = false">Thôi</button>
            <button class="btn-modal btn-confirm-market" @click="confirmMarketSell">
                <i class="fas fa-gavel"></i> Treo Bán
            </button>
          </div>
        </div>
      </div>
    </transition>

    <GameToast />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import { useInventoryStore } from '@/stores/inventoryStore';
import { useAuthStore } from '@/stores/authStore';
import { useMarketStore } from '@/stores/marketStore';
import { useNotificationStore } from '@/stores/notificationStore';
import { resolveItemImage } from '@/utils/assetHelper';
import GameToast from '@/components/GameToast.vue';

// --- CONFIG ---
const ITEM_HEIGHT = 50; 
const VISIBLE_COUNT = 5; 
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

const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const marketStore = useMarketStore();
const notificationStore = useNotificationStore();

const currentTab = ref('ALL');
const selectedItem = ref(null);
const viewMode = ref('list'); 

// Modal States
const showSellModal = ref(false);
const sellItemData = ref(null);
const sellForm = ref({ quantity: 1 });

const showMarketModal = ref(false);
const marketItemData = ref(null);
const marketForm = ref({ price: 0, quantity: 1 });

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

watch(scrollY, (newVal) => {
    if (viewMode.value !== 'list') return;
    const items = filteredItems.value;
    if (!items.length) return;
    const centerIndex = Math.round(newVal / ITEM_HEIGHT);
    if (centerIndex >= 0 && centerIndex < items.length) {
        const targetItem = items[centerIndex];
        if (selectedItem.value?.userItemId !== targetItem?.userItemId) {
            selectedItem.value = targetItem;
        }
    }
});

watch(filteredItems, (newItems) => {
    if (newItems.length > 0 && !selectedItem.value) {
        selectedItem.value = newItems[0];
    }
}, { immediate: true });

const renderedItems = computed(() => {
    if (viewMode.value !== 'list') return [];
    const items = filteredItems.value;
    const total = items.length;
    if (total === 0) return [];

    const containerHeight = 250;
    const centerY = containerHeight / 2;

    const result = [];
    const baseIndex = Math.floor(scrollY.value / ITEM_HEIGHT);
    const buffer = Math.ceil(VISIBLE_COUNT / 2);

    for (let i = baseIndex - buffer; i <= baseIndex + buffer; i++) {
        if (i < 0 || i >= total) continue;

        const itemData = items[i];
        const itemY = i * ITEM_HEIGHT - scrollY.value; 
        const distSlots = Math.abs(itemY / ITEM_HEIGHT);

        let scale = 1, opacity = 1, rotateX = 0, brightness = 1, zIndex = 10, blurAmount = 0;

        if (distSlots <= 2.5) { 
            if (distSlots < 0.5) {
                scale = 1.05; opacity = 1; brightness = 1.2; zIndex = 100; blurAmount = 0;
            } else {
                const ratio = 1 - (distSlots / 3);
                scale = 0.9 + (ratio * 0.1); 
                opacity = 0.3 + (ratio * 0.7);
                brightness = 0.5 + (ratio * 0.5);
                rotateX = -itemY * 0.15;
                zIndex = Math.floor((3 - distSlots) * 10);
                blurAmount = (distSlots - 0.5) * 1.5; 
            }
        } else {
            opacity = 0; scale = 0.8; blurAmount = 5;
        }

        const isActive = distSlots < 0.5;
        const style = {
            transform: `translateY(${centerY + itemY - (ITEM_HEIGHT/2)}px) perspective(500px) rotateX(${rotateX}deg) scale(${scale})`,
            opacity: opacity, zIndex: zIndex,
            filter: `brightness(${brightness}) blur(${blurAmount}px)`,
            visibility: opacity <= 0.05 ? 'hidden' : 'visible'
        };

        result.push({ virtualKey: `${itemData.userItemId}_${i}`, index: i, data: itemData, style, isActive });
    }
    return result;
});

// Scroll Logic (Giữ nguyên)
const scrollToIndex = (virtualIndex) => { targetScrollY = virtualIndex * ITEM_HEIGHT; cancelAnimationFrame(animationFrameId); animateToTarget(); };
const animateToTarget = () => { if (targetScrollY === null) return; const diff = targetScrollY - scrollY.value; if (Math.abs(diff) > 0.5) { scrollY.value += diff * 0.1; animationFrameId = requestAnimationFrame(animateToTarget); } else { scrollY.value = targetScrollY; targetScrollY = null; } };
const inertiaLoop = (time) => { if (!lastTime) lastTime = time; const delta = time - lastTime; lastTime = time; if (Math.abs(velocity) > 0.1) { scrollY.value -= velocity * 16; const maxScroll = Math.max(0, (filteredItems.value.length - 1) * ITEM_HEIGHT); if (scrollY.value < -50) velocity = -0.5; if (scrollY.value > maxScroll + 50) velocity = 0.5; velocity *= 0.92; animationFrameId = requestAnimationFrame(inertiaLoop); } else { velocity = 0; snapToGrid(); } };
const snapToGrid = () => { let targetY = Math.round(scrollY.value / ITEM_HEIGHT) * ITEM_HEIGHT; const maxScroll = Math.max(0, (filteredItems.value.length - 1) * ITEM_HEIGHT); targetY = Math.max(0, Math.min(targetY, maxScroll)); const diff = targetY - scrollY.value; if (Math.abs(diff) > 0.5) { scrollY.value += diff * 0.15; animationFrameId = requestAnimationFrame(snapToGrid); } else { scrollY.value = targetY; cancelAnimationFrame(animationFrameId); } };
const onWheel = (e) => { cancelAnimationFrame(animationFrameId); targetScrollY = null; velocity = 0; scrollY.value += e.deltaY * 0.4; const maxScroll = Math.max(0, (filteredItems.value.length - 1) * ITEM_HEIGHT); if (scrollY.value < -20) scrollY.value = -20; if (scrollY.value > maxScroll + 20) scrollY.value = maxScroll + 20; snapToGrid(); };
const onMouseDown = (e) => { isDragging = true; startY = e.clientY; lastTouchY = startY; velocity = 0; targetScrollY = null; cancelAnimationFrame(animationFrameId); window.addEventListener('mousemove', onMouseMove); window.addEventListener('mouseup', onMouseUp); };
const onMouseMove = (e) => { if (!isDragging) return; const delta = lastTouchY - e.clientY; scrollY.value += delta; lastTouchY = e.clientY; };
const onMouseUp = (e) => { isDragging = false; window.removeEventListener('mousemove', onMouseMove); window.removeEventListener('mouseup', onMouseUp); velocity = (startY - e.clientY) * 0.05; lastTime = 0; inertiaLoop(performance.now()); };
const onTouchStart = (e) => { isDragging = true; startY = e.touches[0].clientY; lastTouchY = startY; velocity = 0; targetScrollY = null; cancelAnimationFrame(animationFrameId); };
const onTouchMove = (e) => { if (!isDragging) return; const delta = lastTouchY - e.touches[0].clientY; scrollY.value += delta; lastTouchY = e.touches[0].clientY; };
const onTouchEnd = (e) => { isDragging = false; velocity = (startY - lastTouchY) * 0.05; lastTime = 0; inertiaLoop(performance.now()); };

const switchTab = (tabId) => { currentTab.value = tabId; scrollY.value = 0; velocity = 0; };

// Helpers
const selectItem = (item) => { selectedItem.value = item; };
const parsedSubStats = computed(() => { if (!selectedItem.value || !selectedItem.value.subStats) return []; try { return JSON.parse(selectedItem.value.subStats); } catch (e) { return []; } });
const isStatPercent = (code) => { if(!code) return false; return code.endsWith('_PERCENT') || ['CRIT_RATE', 'CRIT_DMG'].includes(code); };
const getStatName = (code) => { const dict = { "ATK": "Công Lực", "ATK_FLAT": "Công Lực", "ATK_PERCENT": "Công Lực", "DEF": "Hộ Thể", "DEF_FLAT": "Hộ Thể", "DEF_PERCENT": "Hộ Thể", "HP": "Sinh Lực", "HP_FLAT": "Sinh Lực", "HP_PERCENT": "Sinh Lực", "SPEED": "Thân Pháp", "CRIT": "Bạo Kích", "CRIT_RATE": "Bạo Kích", "CRIT_DMG": "Sát Thương Bạo", "LUCK": "May Mắn", "DURABILITY": "Độ Bền" }; return dict[code] || code; };
const getStatLabel = (mainStatType, itemType) => { if (mainStatType && typeof mainStatType === 'string') return getStatName(mainStatType); if (itemType) { if (itemType === 'WEAPON') return "Công Lực"; if (['ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE'].includes(itemType)) return "Hộ Thể"; if (itemType === 'TOOL') return "Hiệu Suất"; } return "Sức Mạnh"; };
const canEquip = (uItem) => ['WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'TOOL'].includes(uItem.item.type);

const handleEquip = async () => { if (!selectedItem.value) return; try { if (selectedItem.value.isEquipped) { await inventoryStore.unequipItem(selectedItem.value.userItemId); notificationStore.showToast("Đã tháo trang bị!", "success"); } else { await inventoryStore.equipItem(selectedItem.value.userItemId); notificationStore.showToast("Đã trang bị thành công!", "success"); } const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId); if(fresh) selectedItem.value = fresh; } catch (err) { notificationStore.showToast(err, "error"); } };
const handleUse = async () => { if(!selectedItem.value) return; try { const msg = await inventoryStore.useItem(selectedItem.value.userItemId); notificationStore.showToast(msg, "success"); } catch(e) { notificationStore.showToast(e, "error"); } };

// --- BÁN SHOP ---
const openSellModal = (item) => { 
    if(item.isEquipped) { notificationStore.showToast("Phải gỡ trang bị mới được bán!", "error"); return; } 
    sellItemData.value = item; sellForm.value = { quantity: 1 }; showSellModal.value = true; 
};
const confirmSell = async () => { 
    if(!sellItemData.value) return; 
    if(sellForm.value.quantity > sellItemData.value.quantity) { notificationStore.showToast("Không đủ số lượng!", "error"); return; } 
    try { 
        const result = await marketStore.sellItem(sellItemData.value.userItemId, sellForm.value.quantity);
        notificationStore.showToast(result.message || "Đã bán thành công!", "success"); 
        showSellModal.value = false; selectedItem.value = null; 
    } catch (e) { notificationStore.showToast(typeof e === 'string' ? e : "Lỗi khi bán vật phẩm", "error"); } 
};

// --- TREO BÁN (CHỢ) ---
const openMarketModal = (item) => {
    if(item.isEquipped) { notificationStore.showToast("Phải gỡ trang bị mới được treo bán!", "error"); return; }
    marketItemData.value = item;
    marketForm.value = { price: 100, quantity: 1 }; // Default price suggestion
    showMarketModal.value = true;
};
const confirmMarketSell = async () => {
    if (!marketItemData.value) return;
    if (marketForm.value.quantity > marketItemData.value.quantity) {
        notificationStore.showToast("Không đủ số lượng để bán!", "error");
        return;
    }
    if (marketForm.value.price <= 0) {
        notificationStore.showToast("Giá bán phải lớn hơn 0!", "error");
        return;
    }

    try {
        const result = await marketStore.createListing(
            marketItemData.value.userItemId,
            marketForm.value.price,
            marketForm.value.quantity
        );
        notificationStore.showToast(result.message || "Đã niêm yết lên chợ thành công!", "success");
        showMarketModal.value = false;
        selectedItem.value = null; // Reset selection vì item đã mất khỏi túi
    } catch (e) {
        notificationStore.showToast(e.response?.data?.message || "Lỗi khi đăng bán", "error");
    }
};

const expandSlots = async () => { const currentSlots = authStore.user?.inventorySlots || 49; if (currentSlots >= 210) { notificationStore.showToast("Đã đạt giới hạn tối đa!", "error"); return; } const cost = Math.floor((currentSlots - 49) / 7) + 1; if(!confirm(`Mở rộng thêm 7 ô chứa?\nChi phí: ${cost} Echo Coin`)) return; if (authStore.wallet.echoCoin < cost) { notificationStore.showToast(`Thiếu Echo Coin! Cần ${cost}.`, "error"); return; } try { const msg = await inventoryStore.expandInventory(); notificationStore.showToast(msg || "Mở rộng thành công!", "success"); } catch (e) { notificationStore.showToast(typeof e === 'string' ? e : "Lỗi", "error"); } };

const getDurabilityPercent = (uItem) => (!uItem.maxDurability) ? 100 : Math.max(0, Math.min(100, (uItem.currentDurability / uItem.maxDurability) * 100));
const getDurabilityColorClass = (uItem) => { const pct = getDurabilityPercent(uItem); return pct < 30 ? 'dur-low' : 'dur-high'; };
const needsRepair = (uItem) => uItem.item.type === 'TOOL' && uItem.maxDurability && uItem.currentDurability < uItem.maxDurability;
const getRepairCost = (uItem) => { if (!uItem.maxDurability || !uItem.currentDurability) return { gold: 0, coin: 0 }; const missing = uItem.maxDurability - uItem.currentDurability; if (missing <= 0) return { gold: 0, coin: 0 }; let goldPerPoint = 10; let coinPerPoint = 0; const rarity = uItem.isMythic ? 'MYTHIC' : (uItem.item.rarity || 'COMMON'); switch (rarity) { case 'COMMON': case 'UNCOMMON': goldPerPoint = 10; break; case 'RARE': goldPerPoint = 50; break; case 'EPIC': goldPerPoint = 200; coinPerPoint = 0.1; break; case 'LEGENDARY': case 'MYTHIC': goldPerPoint = 1000; coinPerPoint = 1.0; break; default: goldPerPoint = 10; } return { gold: missing * goldPerPoint, coin: parseFloat((missing * coinPerPoint).toFixed(1)) }; };
const handleRepair = async () => { if (!selectedItem.value) return; const cost = getRepairCost(selectedItem.value); let confirmMsg = `Sửa chữa vật phẩm này?\nChi phí: ${formatNumber(cost.gold)} Vàng`; if (cost.coin > 0) confirmMsg += ` + ${cost.coin} Echo Coin`; if(!confirm(confirmMsg)) return; try { await inventoryStore.repairItem(selectedItem.value.userItemId); notificationStore.showToast("Đã sửa thành công!", "success"); const fresh = inventoryStore.items.find(i => i.userItemId === selectedItem.value.userItemId); if(fresh) selectedItem.value = fresh; } catch(e) { notificationStore.showToast(e, "error"); } };
const getRarityClass = (item) => item.isMythic ? "mythic" : (item.item.rarity ? item.item.rarity.toLowerCase() : "common");
const formatNumber = (num) => new Intl.NumberFormat().format(num || 0);

onMounted(async () => { await inventoryStore.fetchInventory(); });
onUnmounted(() => { cancelAnimationFrame(animationFrameId); window.removeEventListener('mousemove', onMouseMove); window.removeEventListener('mouseup', onMouseUp); });
</script>

<style scoped>
/* GIỮ NGUYÊN STYLE CŨ VÀ THÊM STYLE MỚI CHO NÚT BÁN VÀ MODAL */
.wuxia-theme { background-color: #050505; background-image: radial-gradient(circle at 50% 30%, #1a100d 0%, #000000 90%); color: #e0d4b9; font-family: 'Noto Serif TC', serif; height: 100vh; overflow: hidden; box-sizing: border-box; padding: 20px; position: relative; user-select: none; }
.bg-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: url('https://www.transparenttextures.com/patterns/dark-leather.png'); opacity: 0.3; pointer-events: none; z-index: 0; }
.inventory-layout { position: relative; z-index: 1; display: grid; grid-template-columns: 1.2fr 1fr; gap: 20px; max-width: 1100px; margin: 0 auto; height: calc(100vh - 40px); }
.wood-panel { background: linear-gradient(135deg, rgba(45, 30, 25, 0.95), rgba(30, 20, 15, 0.98)); border: 1px solid #6d4c41; box-shadow: 0 10px 30px rgba(0,0,0,0.8), inset 0 0 10px rgba(0,0,0,0.5); border-radius: 8px; display: flex; flex-direction: column; overflow: hidden; }
.panel-header { background: linear-gradient(180deg, #3e2723 0%, #261815 100%); padding: 10px 16px; display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #5d4037; z-index: 20; }
.panel-header h3 { margin: 0; color: #ffd700; font-size: 1.1rem; font-weight: bold; letter-spacing: 1px; }
.header-right { display: flex; align-items: center; gap: 8px; }
.slots-text { font-size: 0.85rem; color: #a1887f; }
.btn-add-slots, .btn-icon-action { border: 1px solid #5d4037; color: #ffd700; width: 26px; height: 26px; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: transform 0.2s; font-size: 0.8rem; margin-left: 5px; background: #3e2723; }
.btn-add-slots { background: linear-gradient(135deg, #2e7d32, #1b5e20); color: #fff; }
.btn-add-slots:hover, .btn-icon-action:hover { transform: scale(1.1); filter: brightness(1.2); }
.filter-tabs { display: flex; background: rgba(0,0,0,0.3); padding: 4px; gap: 2px; border-bottom: 1px solid #4e342e; }
.filter-tabs button { flex: 1; padding: 6px; border: none; background: transparent; color: #8d6e63; cursor: pointer; font-size: 0.8rem; border-radius: 4px; transition: 0.2s; font-family: 'Noto Serif TC', serif; }
.filter-tabs button.active { background: #3e2723; color: #ffd700; font-weight: bold; border: 1px solid #5d4037; }
.wheel-wrapper { flex: 1; display: flex; justify-content: center; align-items: center; overflow: hidden; }
.infinite-wheel-container { width: 100%; position: relative; overflow: hidden; background: radial-gradient(circle at center, rgba(62, 39, 35, 0.4) 0%, transparent 80%); cursor: grab; height: 250px; mask-image: linear-gradient(to bottom, transparent 0%, black 20%, black 80%, transparent 100%); -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 20%, black 80%, transparent 100%); }
.infinite-wheel-container:active { cursor: grabbing; }
.center-highlight-bar { position: absolute; top: 50%; left: 0; right: 0; height: 50px; transform: translateY(-50%); background: linear-gradient(90deg, transparent 0%, rgba(255, 215, 0, 0.1) 20%, rgba(255, 215, 0, 0.1) 80%, transparent 100%); border-top: 1px solid rgba(255, 215, 0, 0.3); border-bottom: 1px solid rgba(255, 215, 0, 0.3); pointer-events: none; z-index: 0; }
.wheel-item { position: absolute; left: 0; right: 0; top: 0; height: 50px; display: flex; align-items: center; justify-content: center; padding: 0 10px; will-change: transform, opacity, filter; cursor: pointer; transition: filter 0.2s; }
.wheel-inner { width: 90%; height: 44px; display: flex; align-items: center; background: rgba(38, 24, 21, 0.8); border: 1px solid #4e342e; border-radius: 4px; padding: 0 10px; transition: background 0.2s, border 0.2s; }
.wheel-item.active .wheel-inner { background: linear-gradient(90deg, rgba(62, 39, 35, 0.9) 0%, rgba(93, 64, 55, 1) 50%, rgba(62, 39, 35, 0.9) 100%); border-color: #ffd700; transform: scale(1.02); }
.icon-box { position: relative; margin-right: 12px; }
.item-icon { width: 32px; height: 32px; object-fit: contain; filter: drop-shadow(0 2px 2px black); }
.qty-badge { position: absolute; bottom: -2px; right: -4px; background: #3e2723; color: #fff; font-size: 0.6rem; padding: 0 3px; border-radius: 2px; border: 1px solid #5d4037; }
.info-box { flex: 1; display: flex; flex-direction: column; justify-content: center; overflow: hidden; }
.name-row { display: flex; align-items: center; gap: 6px; }
.item-name { font-weight: bold; font-size: 0.9rem; color: #e0d4b9; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 150px; }
.active .item-name { color: #fff; text-shadow: 0 0 5px rgba(255,255,255,0.2); }
.enhance-txt { color: #ff5252; font-size: 0.8rem; font-weight: bold; }
.mythic-txt { color: #ff1744; font-size: 0.8rem; font-weight: bold; }
.sub-row { display: flex; justify-content: space-between; align-items: center; font-size: 0.7rem; color: #a1887f; margin-top: 1px; }
.status-equipped { color: #66bb6a; display: flex; align-items: center; gap: 3px; }
.rarity-COMMON .item-name { color: #bdbdbd; }
.rarity-UNCOMMON .item-name { color: #81c784; }
.rarity-RARE .item-name { color: #64b5f6; }
.rarity-EPIC .item-name { color: #ba68c8; }
.rarity-LEGENDARY .item-name { color: #ffd54f; }
.rarity-MYTHIC .item-name { color: #ff1744; text-shadow: 0 0 5px #ff1744; }
.empty-msg { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #5d4037; opacity: 0.5; font-style: italic; }
.grid-view-container { flex: 1; padding: 10px; overflow-y: auto; }
.inv-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(56px, 1fr)); gap: 6px; align-content: start; }
.item-slot { aspect-ratio: 1; border: 1px solid #4e342e; background: rgba(0, 0, 0, 0.4); border-radius: 4px; cursor: pointer; position: relative; transition: all 0.2s; }
.item-slot:hover { transform: translateY(-2px); border-color: #ffd700; z-index: 5; }
.item-slot.selected { border-color: #ffd700; box-shadow: 0 0 8px rgba(255, 215, 0, 0.3); }
.item-slot.empty { opacity: 0.1; pointer-events: none; border-style: dashed; }
.slot-inner { width: 100%; height: 100%; padding: 3px; display: flex; justify-content: center; align-items: center; position: relative; }
.slot-inner img { max-width: 100%; max-height: 100%; object-fit: contain; }
.level-badge { position: absolute; bottom: 1px; right: 1px; background: rgba(0,0,0,0.8); color: #fff; font-size: 0.6rem; padding: 0 3px; border-radius: 2px; }
.equipped-badge { position: absolute; top: 1px; left: 1px; font-size: 0.7rem; color: #66bb6a; background: rgba(0,0,0,0.7); border-radius: 50%; width: 14px; height: 14px; display: flex; align-items: center; justify-content: center; }
.qty-tag { position: absolute; bottom: 1px; right: 1px; font-size: 8px; background: rgba(0,0,0,0.8); color: white; padding: 0 2px; border-radius: 2px; }
.common { border-color: #9e9e9e; }
.uncommon { border-color: #66bb6a; }
.rare { border-color: #42a5f5; }
.epic { border-color: #ab47bc; }
.legendary { border-color: #ffd54f; }
.mythic { border-color: #ff1744; }
.inv-detail-panel { padding: 15px; }
.detail-content { display: flex; flex-direction: column; height: 100%; }
.detail-image-box { height: 140px; display: flex; justify-content: center; align-items: center; background: radial-gradient(circle, rgba(255,255,255,0.03) 0%, transparent 70%); margin-bottom: 15px; border-radius: 6px; border: 1px solid rgba(255,255,255,0.05); }
.big-preview { transform: scale(1.6); filter: drop-shadow(0 5px 10px rgba(0,0,0,0.8)); }
.stats-box { flex: 1; }
.stat-row { display: flex; justify-content: space-between; padding: 5px 0; border-bottom: 1px dashed #4e342e; }
.main-stat { margin-bottom: 10px; border-bottom: 2px solid #5d4037; padding-bottom: 8px; }
.main-stat .stat-label { font-size: 1rem; color: #ef9a9a; font-weight: bold; }
.main-stat .stat-val { font-size: 1.1rem; color: #fff; font-weight: bold; text-shadow: 0 0 5px rgba(255,82,82,0.5); }
.sub-stat { color: #b0bec5; font-size: 0.9rem; }
.sub-val { color: #81d4fa; }
.dot { color: #5d4037; margin-right: 6px; font-size: 0.7rem; }
.desc-text { margin-top: 15px; font-style: italic; color: #a1887f; font-size: 0.85rem; line-height: 1.4; padding: 10px; background: rgba(0,0,0,0.2); border-left: 3px solid #5d4037; }
.durability-box { margin-top: 10px; padding: 8px; background: rgba(0,0,0,0.3); border-radius: 4px; }
.durability-header { display: flex; justify-content: space-between; font-size: 0.8rem; color: #ccc; margin-bottom: 4px; }
.durability-progress-bg { height: 16px; background: #261815; border-radius: 8px; overflow: hidden; position: relative; box-shadow: inset 0 1px 3px rgba(0,0,0,0.8); }
.durability-progress-fill { height: 100%; transition: width 0.3s; z-index: 1; position: absolute; left: 0; top: 0; }
.durability-percent-text { position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 0.7rem; font-weight: bold; color: #fff; text-shadow: 0 0 3px rgba(0,0,0,0.8); z-index: 2; }
.dur-high { background: #66bb6a; } .dur-low { background: #ef5350; }

.btn-action { width: 100%; padding: 12px; margin-top: 8px; border: none; font-weight: bold; cursor: pointer; text-transform: uppercase; letter-spacing: 1px; clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px); transition: 0.2s; font-family: 'Noto Serif TC', serif; }
.btn-action:hover { filter: brightness(1.15); transform: translateY(-2px); }
.btn-equip { background: linear-gradient(to bottom, #2e7d32, #1b5e20); color: white; }
.btn-use { background: linear-gradient(to bottom, #1565c0, #0d47a1); color: white; }
.btn-repair { background: linear-gradient(to bottom, #ff8f00, #ff6f00); color: black; }
.btn-sell { background: linear-gradient(to bottom, #c62828, #b71c1c); color: white; }
/* [NEW] Style nút treo bán */
.btn-market { background: linear-gradient(to bottom, #7b1fa2, #4a148c); color: #fff; border: 1px solid #9c27b0; }
.cost-mini { font-size: 0.75rem; display: block; font-weight: normal; margin-top: 2px; text-transform: none; color: #e0e0e0; }

.modal-overlay { position: fixed; inset: 0; z-index: 100; background: rgba(0,0,0,0.85); backdrop-filter: blur(2px); display: flex; justify-content: center; align-items: center; }
.modal-content { width: 350px; padding: 0; }
.modal-header { background: #3e2723; padding: 10px 15px; display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #5d4037; }
.modal-header h3 { margin: 0; font-family: 'Playfair Display', serif; color: #ffd700; }
.close-btn { background: none; border: none; color: #aaa; cursor: pointer; font-size: 1.2rem; }
.modal-body { padding: 15px; }
.input-group label { color: #d7ccc8; font-size: 0.9rem; }
.input-group input { width: 100%; background: #261815; border: 1px solid #5d4037; color: #fff; padding: 8px; font-family: inherit; }
.fixed-price-display { background: #1a100d; border: 1px dashed #5d4037; padding: 8px; text-align: right; color: #e0e0e0; font-weight: bold; }
.gold-icon { color: #ffd700; margin-left: 4px; }
.highlight-gold { color: #ffd700; font-weight: bold; }
.hint-text { color: #8d6e63; font-size: 0.75rem; margin-top: 5px; display: block; font-style: italic; }
.modal-actions { padding: 15px; border-top: 1px solid #4e342e; display: flex; justify-content: flex-end; gap: 10px; background: rgba(0,0,0,0.2); }
.btn-modal { padding: 8px 16px; border-radius: 4px; border: none; font-weight: bold; cursor: pointer; }
.btn-cancel { background: transparent; color: #888; border: 1px solid #444; }
.btn-confirm-sell { background: #b71c1c; color: white; }

/* [NEW] Style riêng cho Modal Market */
.market-modal { border-color: #9c27b0; box-shadow: 0 0 20px rgba(156, 39, 176, 0.3); }
.market-header { background: #4a148c; border-bottom-color: #7b1fa2; }
.price-input-wrapper { position: relative; display: flex; align-items: center; }
.price-input-wrapper input { padding-right: 30px; text-align: right; font-weight: bold; color: #ffd700; font-size: 1.1rem; }
.gold-icon-input { position: absolute; right: 10px; color: #ffd700; pointer-events: none; }
.market-summary { background: rgba(74, 20, 140, 0.2); border: 1px dashed #7b1fa2; padding: 10px; border-radius: 4px; margin-top: 15px; text-align: right; }
.text-market { color: #ce93d8; }
.btn-confirm-market { background: #7b1fa2; color: #fff; }
.btn-confirm-market:hover { background: #8e24aa; }

@media (max-width: 768px) {
  .inventory-layout { grid-template-columns: 1fr; height: auto; }
  .infinite-wheel-container, .grid-view-container { height: 250px; }
}
</style>