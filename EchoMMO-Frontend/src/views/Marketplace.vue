<template>
  <div class="page-container wuxia-market dark-theme">
    
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

    <div class="market-overlay">
      
      <div class="market-header">
        <div class="header-decor left"></div>
        <h2 class="market-title">THƯƠNG HỘI</h2>
        <div class="header-decor right"></div>

        <div class="filter-bar">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input v-model="searchQuery" type="text" placeholder="Tìm kiếm bảo vật..." />
          </div>
          
          <div class="filter-box">
            <i class="fas fa-shapes"></i>
            <select v-model="filterType">
              <option value="ALL">Tất cả loại</option>
              <option value="WEAPON">Vũ khí</option>
              <option value="ARMOR">Giáp</option>
              <option value="ACCESSORY">Trang sức</option>
              <option value="CONSUMABLE">Tiêu hao</option>
              <option value="MATERIAL">Nguyên liệu</option>
              <option value="TOOL">Công cụ</option>
            </select>
          </div>

          <div class="filter-box">
            <i class="fas fa-filter"></i>
            <select v-model="filterRarity">
              <option value="ALL">Tất cả phẩm chất</option>
              <option value="COMMON">Phàm phẩm</option>
              <option value="UNCOMMON">Lương phẩm</option>
              <option value="RARE">Linh phẩm</option>
              <option value="EPIC">Tiên phẩm</option>
              <option value="LEGENDARY">Thần phẩm</option>
              <option value="MYTHIC">Thánh phẩm</option>
            </select>
          </div>
        </div>

        <div class="market-tabs">
          <button :class="{ active: activeTab === 'buy' }" @click="switchTab('buy')" class="tab-btn">
            <i class="fas fa-store"></i> TIỆM TẠP HÓA
          </button>
          <div class="tab-divider"></div>
          
          <button :class="{ active: activeTab === 'p2p' }" @click="switchTab('p2p')" class="tab-btn">
            <i class="fas fa-handshake"></i> CHỢ TRỜI
          </button>
          <div class="tab-divider"></div>
          
          <button :class="{ active: activeTab === 'my_listings' }" @click="loadMyListings" class="tab-btn">
            <i class="fas fa-clipboard-list"></i> SẠP CỦA TA
          </button>
          <div class="tab-divider"></div>
          
          <button :class="{ active: activeTab === 'sell_sys' }" @click="switchTab('sell_sys')" class="tab-btn">
            <i class="fas fa-coins"></i> BÁN ĐỒ
          </button>
        </div>
      </div>

      <div class="market-content custom-scroll">
        <transition name="fade-slide" mode="out-in">
          
          <div v-if="activeTab === 'buy'" class="grid-layout" key="buy">
            <div v-if="filteredShopItems.length === 0" class="empty-state">
              <i class="fas fa-search-minus"></i>
              <p>Tiệm đang nhập hàng, vui lòng quay lại sau.</p>
            </div>
            <div v-for="item in filteredShopItems" :key="item.itemId" class="item-card system-card">
              <div class="card-top">
                <div class="img-frame" :class="'border-' + (item.rarity || 'COMMON')">
                  <img :src="resolveItemImage(item.imageUrl)" @error="handleImgError" />
                  <span class="rarity-tag" :class="'bg-' + (item.rarity || 'COMMON')">
                    {{ resolveRarityName(item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + (item.rarity || 'COMMON')">{{ item.name }}</div>
                <div class="item-type">{{ item.type }}</div>
                <div class="price-row">
                  <span class="label">Giá:</span>
                  <span class="val gold-text">{{ formatNumber(item.basePrice) }} <i class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer">
                <input type="number" v-model.number="buyQty[item.itemId]" min="1" class="dark-input" placeholder="1" />
                <button @click="askBuySystem(item)" class="btn-action btn-buy-sys">MUA</button>
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'p2p'" class="grid-layout" key="p2p">
            <div v-if="filteredPlayerListings.length === 0" class="empty-state">
              <i class="fas fa-wind"></i>
              <p>Chợ vắng tanh, chưa ai bán món này...</p>
            </div>
            <div v-for="listing in filteredPlayerListings" :key="listing.listingId" class="item-card p2p-card">
              <div class="seller-badge">
                <i class="fas fa-user-tag"></i> {{ listing.seller?.username || "Ẩn danh" }}
              </div>
              <div class="card-top p2p-img-top">
                <div class="img-frame" :class="'border-' + (listing.item.rarity || 'COMMON')">
                  <img :src="resolveItemImage(listing.item.imageUrl)" @error="handleImgError" />
                  <div v-if="listing.enhanceLevel > 0" class="level-tag" :class="getLevelClass(listing.enhanceLevel)">
                    +{{ listing.enhanceLevel }}
                  </div>
                  <span class="rarity-tag" :class="'bg-' + (listing.item.rarity || 'COMMON')">
                    {{ resolveRarityName(listing.item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body p2p-body">
                <div class="item-name" :class="'text-' + (listing.item.rarity || 'COMMON')">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0" class="enhance-txt">(+{{ listing.enhanceLevel }})</span>
                </div>
                <div class="stock-info">Tồn kho: <span class="highlight">{{ listing.quantity }}</span></div>
                <div class="price-row">
                  <span class="val gold-text large">{{ formatNumber(listing.price) }} <i class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer">
                <input type="number" v-model.number="p2pQty[listing.listingId]" min="1" :max="listing.quantity"
                  class="dark-input" placeholder="1" />
                <button @click="askBuyP2P(listing)" class="btn-action btn-buy-p2p"><i
                    class="fas fa-shopping-basket"></i> MUA</button>
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'my_listings'" class="grid-layout" key="my_listings">
            <div v-if="marketStore.myListings.length === 0" class="empty-state">
              <i class="fas fa-scroll"></i>
              <p>Chưa bày bán vật phẩm nào.</p>
            </div>
            <div v-for="listing in marketStore.myListings" :key="listing.listingId" class="item-card my-card">
              <div class="card-top">
                <div class="img-frame" :class="'border-' + (listing.item.rarity || 'COMMON')">
                  <img :src="resolveItemImage(listing.item.imageUrl)" @error="handleImgError" />
                  <span class="qty-badge-corner">x{{ listing.quantity }}</span>
                  <span class="rarity-tag" :class="'bg-' + (listing.item.rarity || 'COMMON')">
                    {{ resolveRarityName(listing.item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + (listing.item.rarity || 'COMMON')">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0">(+{{ listing.enhanceLevel }})</span>
                </div>
                <div class="price-row">
                    <span class="label">Đang bán:</span>
                  <span class="val gold-text">{{ formatNumber(listing.price) }} <i class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer full-width">
                <button @click="handleCancelListing(listing.listingId)" class="btn-action btn-cancel"><i class="fas fa-undo"></i> THU HỒI</button>
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'sell_sys'" class="grid-layout" key="sell_sys">
            <div v-if="filteredInventory.length === 0" class="empty-state">
              <i class="fas fa-box-open"></i>
              <p>Hành trang trống.</p>
            </div>
            <div v-for="uItem in filteredInventory" :key="uItem.userItemId" class="item-card sell-card">
              <div class="card-top small-top">
                <div class="img-frame small" :class="'border-' + (uItem.item.rarity || 'COMMON')">
                  <img :src="resolveItemImage(uItem.item.imageUrl)" @error="handleImgError" />
                  <span class="qty-badge">x{{ uItem.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + (uItem.item.rarity || 'COMMON')">{{ uItem.item.name }}</div>
                <div class="price-row">
                  <span class="label">Thu mua:</span>
                  <span class="val red-text">{{ formatNumber(uItem.item.basePrice * 0.8) }} <i
                      class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer">
                <input type="number" v-model.number="sellQty[uItem.userItemId]" min="1" :max="uItem.quantity"
                  class="dark-input" placeholder="All" />
                <button v-if="!uItem.isEquipped" @click="askSellSystem(uItem)" class="btn-action btn-sell">BÁN</button>
                <div v-else class="equipped-warn">ĐANG DÙNG</div>
              </div>
            </div>
          </div>

        </transition>
      </div>
    </div>

    <div class="custom-toast-container">
      <transition-group name="toast-anim">
        <div v-for="toast in toasts" :key="toast.id" class="wuxia-toast" :class="toast.type">
          <div class="toast-icon">
             <i v-if="toast.type === 'success'" class="fas fa-check"></i>
             <i v-else-if="toast.type === 'error'" class="fas fa-exclamation-triangle"></i>
             <i v-else class="fas fa-info"></i>
          </div>
          <div class="toast-content">
            <div class="toast-message">{{ toast.message }}</div>
          </div>
        </div>
      </transition-group>
    </div>

    <transition name="pop-up">
      <div v-if="confirmModal.visible" class="modal-overlay" @click.self="closeConfirm">
        <div class="bill-modal">
          <div class="bill-header">
            <div class="bill-seal">KHẾ ƯỚC</div>
            <h3>{{ confirmModal.type === "SELL" ? "XÁC NHẬN BÁN" : "XÁC NHẬN MUA" }}</h3>
          </div>
          <div class="bill-body">
            <div class="item-preview-row">
              <div class="preview-img">
                <img :src="resolveItemImage(confirmModal.data.imgCode)" @error="handleImgError" />
              </div>
              <div class="preview-info">
                <div class="p-name">{{ confirmModal.data.name }}</div>
                <div class="p-type">{{ getTransactionLabel(confirmModal.type) }}</div>
              </div>
            </div>
            <div class="bill-calc">
              <div class="calc-row"><span>Đơn giá:</span><span>{{ formatNumber(confirmModal.data.price) }}</span></div>
              <div class="calc-row"><span>Số lượng:</span><span>x {{ confirmModal.data.qty }}</span></div>
              <div class="divider-dashed"></div>
              <div class="calc-row total">
                <span>{{ confirmModal.type === "SELL" ? "THỰC LĨNH:" : "THANH TOÁN:" }}</span>
                <span class="total-gold">{{ formatNumber(confirmModal.data.total) }} <i class="fas fa-coins"></i></span>
              </div>
            </div>
          </div>
          <div class="bill-footer">
            <button class="btn-wood cancel" @click="closeConfirm">HỦY BỎ</button>
            <button class="btn-wood confirm" @click="confirmTransaction"><i class="fas fa-fingerprint"></i> ẤN ĐỊNH</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="pop-up">
      <div v-if="msgBox.visible" class="modal-overlay" @click.self="closeMsgBox">
        <div class="bill-modal msg-box">
          <div class="bill-header">
            <div class="bill-seal">CẢNH BÁO</div>
            <h3>THÔNG BÁO</h3>
          </div>
          <div class="bill-body msg-body">
            <p>{{ msgBox.message }}</p>
          </div>
          <div class="bill-footer">
            <button class="btn-wood cancel" @click="closeMsgBox">HỦY BỎ</button>
            <button class="btn-wood confirm" @click="confirmMsgAction">ĐỒNG Ý</button>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useMarketStore } from "../stores/marketStore";
import { useInventoryStore } from "../stores/inventoryStore";
import { useAuthStore } from "../stores/authStore";
import { resolveItemImage } from "../utils/assetHelper";

// [KHỞI TẠO] Các store quản lý dữ liệu
const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const authStore = useAuthStore();

// --- [LOGIC 1] CUSTOM TOAST (Thay thế alert trình duyệt) ---
const toasts = ref([]);
let toastIdCounter = 0;

// Hàm gọi thông báo: showToast("Mua thành công", "success")
const showToast = (message, type = 'info') => {
  const id = toastIdCounter++;
  toasts.value.push({ id, message, type });
  // Tự động tắt sau 3 giây
  setTimeout(() => {
    const idx = toasts.value.findIndex(t => t.id === id);
    if (idx !== -1) toasts.value.splice(idx, 1);
  }, 3000); 
};

// --- [LOGIC 2] MESSAGE BOX (Hộp thoại xác nhận) ---
const msgBox = reactive({
  visible: false,
  message: "",
  callback: null // Lưu hàm sẽ chạy khi user bấm "Đồng ý"
});

// Hàm gọi hộp thoại xác nhận khi muốn thu hồi vật phẩm
const openCancelDialog = (listingId) => {
  msgBox.message = "Đạo hữu muốn thu hồi vật phẩm này về túi?";
  msgBox.visible = true;
  msgBox.callback = async () => {
    try {
      await marketStore.cancelListing(listingId);
      showToast("Đã thu hồi vật phẩm về hành trang.", "success");
    } catch (e) {
      showToast("Lỗi thu hồi vật phẩm.", "error");
    }
  };
};

const closeMsgBox = () => {
  msgBox.visible = false;
  msgBox.callback = null;
};

// Khi bấm "Đồng ý" ở modal
const confirmMsgAction = () => {
  if (msgBox.callback) msgBox.callback();
  closeMsgBox();
};

// --- [LOGIC 3] XỬ LÝ NỀN & NGÀY ĐÊM ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);
const updateDayNight = () => {
  // Lấy giờ hiện tại để quyết định lớp phủ màu (sáng hay tối)
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- [LOGIC 4] STATE CHO TÌM KIẾM & TAB ---
const activeTab = ref("buy"); // Tab mặc định là Mua (Shop)
// Lưu số lượng user nhập vào ô input cho từng item (key là ID item)
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});
// Biến cho bộ lọc
const searchQuery = ref("");
const filterRarity = ref("ALL");
const filterType = ref("ALL"); 

// --- [LOGIC 5] MODAL XÁC NHẬN GIAO DỊCH ---
const confirmModal = reactive({
  visible: false,
  type: "", // 'SYS' (Shop), 'P2P' (Chợ), 'SELL' (Bán)
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

// --- HELPERS (Hàm tiện ích) ---
// Chuyển mã server (COMMON) thành tên hiển thị (Phàm phẩm/Common)
const resolveRarityName = (backendRarity) => {
  const map = { 'COMMON': 'Common', 'UNCOMMON': 'Uncommon', 'RARE': 'Rare', 'EPIC': 'Epic', 'LEGENDARY': 'Legendary', 'MYTHIC': 'Mythic' };
  return map[backendRarity] || 'Common';
};

const formatNumber = (n) => Number(n).toLocaleString("en-US"); // Format tiền: 1,000,000
const getLevelClass = (lv) => (lv >= 15 ? "lvl-red" : lv >= 10 ? "lvl-purple" : lv >= 5 ? "lvl-gold" : "lvl-white"); // Màu chữ cấp độ
const handleImgError = (e) => { e.target.src = "https://via.placeholder.com/64?text=IMG"; }; // Ảnh thay thế khi lỗi

// --- [CORE] HÀM LỌC DANH SÁCH ITEM ---
// Kết hợp: Tìm theo tên + Lọc theo Type + Lọc theo Rarity
const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];
  return list.filter((entry) => {
    // Inventory cấu trúc hơi khác (entry.item) nên cần check isInventory
    const itemData = isInventory ? entry.item : entry.item || entry;
    if (!itemData) return false;
    
    // Logic riêng: Shop Mua không hiện nguyên liệu (Material) nếu muốn
    if (!isInventory && itemData.type === 'MATERIAL' && activeTab.value === 'buy') return false;

    const itemName = itemData.name || "";
    const nameMatch = itemName.toLowerCase().includes(searchQuery.value.toLowerCase());
    const itemRarity = itemData.rarity || "COMMON"; 
    const rarityMatch = filterRarity.value === "ALL" || itemRarity === filterRarity.value;
    const itemType = itemData.type || "";
    const typeMatch = filterType.value === "ALL" || itemType === filterType.value;
    return nameMatch && rarityMatch && typeMatch;
  });
};

// Computed Properties: Tự động cập nhật danh sách khi bộ lọc thay đổi
const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() => filterItems(marketStore.playerListings));
const filteredInventory = computed(() => filterItems(inventoryStore.items, true));

// Chuyển Tab: Reset bộ lọc và tải lại dữ liệu cần thiết
const switchTab = (tab) => {
  activeTab.value = tab;
  searchQuery.value = "";
  if (tab === 'sell_sys') inventoryStore.fetchInventory();
  if (tab === 'my_listings') marketStore.fetchMyListings();
};

const getTransactionLabel = (type) => {
  if (type === "P2P") return "Giao dịch Chợ Trời";
  if (type === "SELL") return "Bán vật phẩm";
  return "Mua từ Hệ thống";
};

// --- [LOGIC MUA BÁN] ---

// 1. Hỏi mua từ hệ thống
const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  const totalCost = Number(item.basePrice) * Number(qty);
  // CHECK TIỀN TRƯỚC KHI MỞ MODAL
  const userGold = Number(authStore.wallet?.gold || 0); 

  if (userGold < totalCost) {
    showToast("Ngân lượng không đủ, không thể mua!", "error");
    return;
  }
  openConfirm("SYS", { id: item.itemId, name: item.name, imgCode: item.imageUrl, price: item.basePrice, qty, total: totalCost });
};

// 2. Hỏi mua từ người chơi
const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  const totalCost = Number(listing.price) * Number(qty);
  const userGold = Number(authStore.wallet?.gold || 0);

  if (userGold < totalCost) {
    showToast("Túi tiền rỗng tuếch, không thể giao dịch!", "error");
    return;
  }
  openConfirm("P2P", { id: listing.listingId, name: listing.item.name, imgCode: listing.item.imageUrl, price: listing.price, qty, total: totalCost });
};

// 3. Hỏi bán vào shop
const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = Math.floor(uItem.item.basePrice * 0.8); // Giá bán = 80% giá gốc
  openConfirm("SELL", { id: uItem.userItemId, name: uItem.item.name, imgCode: uItem.item.imageUrl, price: sellPrice, qty, total: sellPrice * qty });
};

// Mở modal xác nhận chung
const openConfirm = (type, data) => { confirmModal.type = type; confirmModal.data = data; confirmModal.visible = true; };
const closeConfirm = () => { confirmModal.visible = false; };

// --- XỬ LÝ GIAO DỊCH KHI BẤM 'ẤN ĐỊNH' ---
const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();

  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1; // Reset số lượng về 1
      showToast(`Mua thành công ${data.qty} ${data.name}!`, "success");
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      showToast("Giao dịch thành công!", "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      // Cập nhật giao diện ngay lập tức (Optimistic UI update)
      const idx = inventoryStore.items.findIndex(i => i.userItemId === data.id);
      if (idx !== -1) {
         if(inventoryStore.items[idx].quantity <= data.qty) {
            inventoryStore.items.splice(idx, 1); // Xóa nếu hết
         } else {
            inventoryStore.items[idx].quantity -= data.qty; // Trừ số lượng
         }
      }
      showToast(`Đã bán, nhận ${formatNumber(data.total)} ngân lượng!`, "success");
    }
    await authStore.fetchProfile(); // Cập nhật lại ví tiền
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings(); // Refresh lại chợ nếu lỗi
    const msg = e.response?.data?.message || "Giao dịch thất bại, vui lòng thử lại.";
    showToast(msg, "error");
  }
};

// Xử lý thu hồi vật phẩm (dùng confirm native của browser ở code cũ, đề xuất dùng msgBox custom ở đây)
const handleCancelListing = async (listingId) => {
  // Thay vì confirm() của browser, dùng msgBox custom cho đẹp
  openCancelDialog(listingId);
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

// [LIFECYCLE] Chạy khi component được load
onMounted(async () => {
  updateDayNight();
  if (authStore.fetchProfile) await authStore.fetchProfile();
  
  // Gọi song song 2 API để load dữ liệu nhanh hơn
  Promise.all([
    marketStore.fetchShopItems(),
    marketStore.fetchPlayerListings(),
  ]);
});
</script>

<style scoped>
/* Import Font chữ kiếm hiệp và Icon */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* =========================================
   VARIABLES & BASE (Biến màu sắc chủ đạo)
   ========================================= */
:root {
  --wood-dark: #3e2723;   /* Màu gỗ tối (khung) */
  --wood-light: #5d4037;  /* Màu gỗ sáng (viền) */
  --gold: #ffecb3;        /* Màu vàng kim (text chính) */
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;    /* Màu đỏ của triện/nút mua */
  --card-bg: rgba(38, 24, 21, 0.9); /* Nền thẻ bài bán trong suốt */
}

.dark-theme { 
  background-color: transparent; 
  min-height: 100vh; 
  font-family: "Noto Serif TC", serif; /* Font chữ có chân kiểu cổ trang */
  color: var(--text-light); 
  position: relative; 
  overflow: hidden; 
}

/* --- BACKGROUND STYLES --- */
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; }
.mountain-bg { 
  position: absolute; inset: 0; 
  background-size: cover; background-position: center bottom; 
  opacity: 0.6; filter: sepia(10%) contrast(1.1); /* Chỉnh màu ảnh nền cho cũ kỹ */
}
/* Hiệu ứng chuyển ngày đêm */
.wood-overlay { 
  position: absolute; inset: 0; 
  background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); 
  mix-blend-mode: multiply; 
  transition: background 2s ease; 
}
.wood-overlay.night-mode { 
  background: linear-gradient(to bottom, rgba(10, 5, 20, 0.85), rgba(0, 0, 0, 0.95)); /* Màu đêm tím đen */
}
.vignette { 
  position: absolute; inset: 0; 
  background: radial-gradient(circle, transparent 60%, #1a100d 100%); 
}

/* --- LAYOUT CHÍNH --- */
.market-overlay { 
  position: relative; 
  z-index: 10; 
  max-width: 1200px; 
  margin: 0 auto; 
  padding: 30px 20px; 
  height: 100vh; 
  display: flex; 
  flex-direction: column; 
}

/* --- HEADER STYLES --- */
.market-header { 
  text-align: center; margin-bottom: 20px; 
  background: rgba(30, 20, 15, 0.8); 
  border-top: 4px solid var(--wood-light); 
  border-bottom: 4px solid var(--wood-light); 
  padding: 20px; 
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8); 
  position: relative; 
}
.market-title { font-size: 2.5rem; color: var(--gold); margin: 0 0 15px 0; text-transform: uppercase; letter-spacing: 5px; font-weight: 900; text-shadow: 0 0 10px rgba(255, 236, 179, 0.3); }
/* Đường gạch trang trí tiêu đề */
.header-decor { width: 60px; height: 2px; background: var(--gold); position: absolute; top: 50px; }
.left { left: 20%; } .right { right: 20%; }

/* Style thanh Filter */
.filter-bar { display: flex; justify-content: center; gap: 15px; margin-bottom: 20px; flex-wrap: wrap; }
.search-box, .filter-box { background: rgba(0, 0, 0, 0.5); border: 1px solid #5d4037; padding: 5px 15px; border-radius: 20px; display: flex; align-items: center; gap: 10px; transition: 0.3s; }
.search-box input { background: transparent; border: none; color: #fff; font-family: inherit; width: 200px; outline: none; }
.filter-box select { background: transparent; border: none; color: #fff; font-family: inherit; outline: none; cursor: pointer; }
.filter-box select option { background: #3e2723; color: #fff; }

/* Style Tabs */
.market-tabs { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.tab-btn { background: transparent; border: none; color: var(--text-dim); font-weight: bold; font-size: 1rem; cursor: pointer; padding: 10px 15px; transition: 0.3s; border-bottom: 2px solid transparent; }
.tab-btn:hover, .tab-btn.active { color: var(--gold); border-bottom-color: var(--gold); text-shadow: 0 0 8px rgba(255, 236, 179, 0.4); }
.tab-divider { width: 1px; height: 20px; background: #555; }

/* --- GRID & CARDS (Phần hiển thị item) --- */
.market-content { flex: 1; overflow-y: auto; padding: 10px; }
/* Custom Scrollbar cho đẹp */
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.1); }

.grid-layout { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.empty-state { grid-column: 1 / -1; text-align: center; padding: 50px; color: #757575; font-size: 1.2rem; border: 2px dashed #444; background: rgba(0, 0, 0, 0.2); }

.item-card { 
  background: var(--card-bg); 
  border: 1px solid var(--wood-light); 
  border-radius: 4px; padding: 15px; 
  display: flex; flex-direction: column; gap: 10px; 
  transition: 0.3s; position: relative; 
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); 
}
.item-card:hover { transform: translateY(-5px); border-color: var(--gold); box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7); }

.card-top { display: flex; justify-content: center; padding-bottom: 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
.card-body { flex: 1; text-align: center; }
.card-footer { display: flex; gap: 5px; margin-top: auto; }

/* --- ITEM VISUALS (Ảnh, Khung Rarity) --- */
.img-frame { width: 70px; height: 70px; background: #1a1a1a; display: flex; align-items: center; justify-content: center; position: relative; transition: all 0.3s ease; box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.8); }
.img-frame.small { width: 50px; height: 50px; }
.img-frame img { max-width: 90%; max-height: 90%; }
.rarity-tag { position: absolute; bottom: -10px; font-size: 0.6rem; font-weight: 800; padding: 2px 8px; border-radius: 10px; z-index: 2; text-transform: uppercase; letter-spacing: 1px; }
.level-tag { position: absolute; top: 2px; right: 2px; font-size: 10px; font-weight: 900; z-index: 5; background: rgba(0,0,0,0.6); padding: 0 3px; border-radius: 3px; border: 1px solid rgba(255,255,255,0.1); }
.qty-badge-corner { position: absolute; top: -5px; right: -5px; background: #b71c1c; color: #fff; font-size: 0.7rem; padding: 2px 5px; border-radius: 4px; font-weight: bold; z-index: 5; }

/* --- HỆ THỐNG MÀU SẮC THEO RARITY --- */
/* Common (Xám) */
.border-COMMON { border: 2px solid #a0a0a0; } .bg-COMMON { background: #616161; color: #fff; } .text-COMMON { color: #a0a0a0; }
/* Uncommon (Xanh lá) */
.border-UNCOMMON { border: 2px solid #4caf50; } .bg-UNCOMMON { background: #2e7d32; color: #fff; } .text-UNCOMMON { color: #4caf50; }
/* Rare (Xanh dương) */
.border-RARE { border: 2px solid #00b0ff; } .bg-RARE { background: #01579b; color: #fff; } .text-RARE { color: #00b0ff; }
/* Epic (Tím) */
.border-EPIC { border: 2px solid #d500f9; } .bg-EPIC { background: #4a148c; color: #fff; } .text-EPIC { color: #ea80fc; }
/* Legendary (Vàng kim - có hiệu ứng thở sáng) */
.border-LEGENDARY { border: 2px solid #ffd700; animation: pulse-gold 2s infinite; } .bg-LEGENDARY { background: linear-gradient(135deg, #ff6f00, #ffca28); color: #3e2723; } .text-LEGENDARY { color: #ffd700; }
/* Mythic (Đỏ thánh - có hiệu ứng thở đỏ) */
.border-MYTHIC { border: 2px solid #ff1744; animation: pulse-red 1.5s infinite; } .bg-MYTHIC { background: #b71c1c; color: #fff; } .text-MYTHIC { color: #ff1744; }

/* Keyframes cho hiệu ứng thở */
@keyframes pulse-gold { 0%, 100% { box-shadow: 0 0 10px #ffd700; } 50% { box-shadow: 0 0 20px #ff6f00; } }
@keyframes pulse-red { 0%, 100% { box-shadow: 0 0 10px #ff1744; } 50% { box-shadow: 0 0 25px #b71c1c; } }

.item-name { font-size: 1rem; font-weight: bold; margin-bottom: 5px; }
.item-type { font-size: 0.8rem; color: #9e9e9e; font-style: italic; margin-bottom: 8px; }
.price-row { font-size: 0.95rem; } .gold-text { color: var(--gold); font-weight: bold; } .red-text { color: #ef5350; font-weight: bold; }

/* Styles Input và Button */
.dark-input { width: 50px; background: #121212; border: 1px solid #444; color: var(--gold); text-align: center; padding: 5px; }
.btn-action { flex: 1; border: none; font-weight: bold; cursor: pointer; color: #fff; text-transform: uppercase; font-size: 0.85rem; border-radius: 4px; box-shadow: 0 3px 0 rgba(0,0,0,0.3); transition: 0.2s; }
.btn-action:hover { transform: translateY(-2px); filter: brightness(1.2); }
/* Màu nút riêng cho từng hành động */
.btn-buy-sys { background: var(--red-seal); } .btn-sell { background: #c62828; } .btn-buy-p2p { background: linear-gradient(to bottom, #4caf50, #2e7d32); } .btn-cancel { background: #4e342e; }

/* Badge người bán ở Chợ Trời */
.seller-badge { position: absolute; top: 0; left: 0; right: 0; background: rgba(46, 125, 50, 0.2); font-size: 0.7rem; color: #a5d6a7; text-align: center; padding: 2px; }
.p2p-img-top { margin-top: 15px; border-bottom: none; padding-bottom: 0; }
.p2p-body { padding-top: 5px; } .stock-info { font-size: 0.8rem; color: #aaa; margin-bottom: 5px; } .highlight { color: #fff; }

/* --- CUSTOM TOAST STYLES (Thông báo đẹp) --- */
.custom-toast-container {
  position: fixed; top: 100px; right: 20px; z-index: 9999;
  display: flex; flex-direction: column; gap: 10px; pointer-events: none;
}
.wuxia-toast {
  pointer-events: auto; width: 300px; background: rgba(30, 20, 15, 0.95);
  border-left: 5px solid #5d4037; border-radius: 4px; padding: 12px;
  display: flex; align-items: center; gap: 12px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5); backdrop-filter: blur(4px);
  color: #fff;
}
.wuxia-toast.success { border-left-color: #4caf50; }
.wuxia-toast.success .toast-icon { color: #4caf50; }
.wuxia-toast.error { border-left-color: #f44336; background: rgba(50, 10, 10, 0.95); }
.wuxia-toast.error .toast-icon { color: #f44336; }
.toast-icon { font-size: 1.5rem; }
.toast-content { font-size: 0.9rem; font-family: sans-serif; }
.toast-anim-enter-active, .toast-anim-leave-active { transition: all 0.3s ease; }
.toast-anim-enter-from { opacity: 0; transform: translateX(50px); }
.toast-anim-leave-to { opacity: 0; transform: translateX(50px); }

/* --- MODAL CHUNG & MESSAGE BOX --- */
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 1000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
/* Khung modal giống tờ giấy/khế ước */
.bill-modal { width: 400px; background: #fdf5e6; color: #3e2723; border: 4px double #3e2723; box-shadow: 0 0 50px rgba(0, 0, 0, 0.9); animation: popIn 0.3s; }
.bill-header { background: #3e2723; color: var(--gold); padding: 15px; text-align: center; border-bottom: 4px solid var(--red-seal); position: relative; }
/* Triện đóng dấu đỏ */
.bill-seal { position: absolute; top: 5px; left: 10px; border: 2px solid var(--red-seal); color: var(--red-seal); padding: 2px 5px; font-weight: bold; transform: rotate(-15deg); opacity: 0.8; font-size: 0.8rem; }
.bill-body { padding: 20px; }
.msg-box .msg-body { text-align: center; font-size: 1.1rem; padding: 30px 20px; font-weight: bold; }

.item-preview-row { display: flex; gap: 15px; align-items: center; margin-bottom: 15px; background: rgba(0, 0, 0, 0.05); padding: 10px; border-radius: 4px; }
.preview-img { width: 60px; height: 60px; border: 1px solid #3e2723; background: #fff; display: flex; align-items: center; justify-content: center; } .preview-img img { max-width: 90%; }
.bill-calc { font-family: "Courier New", monospace; font-weight: bold; }
.calc-row { display: flex; justify-content: space-between; margin-bottom: 5px; } .divider-dashed { border-bottom: 2px dashed #3e2723; margin: 10px 0; } .total { font-size: 1.2rem; color: #d84315; }

.bill-footer { padding: 15px; display: flex; gap: 10px; background: rgba(0, 0, 0, 0.05); }
.btn-wood { flex: 1; padding: 10px; font-weight: bold; cursor: pointer; border: 2px solid #3e2723; transition: 0.2s; }
.btn-wood.cancel:hover { background: #d7ccc8; } .btn-wood.confirm { background: #bf360c; color: #fff; border-color: #bf360c; } .btn-wood.confirm:hover { background: #d84315; }

@keyframes popIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; } .fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateY(10px); }
</style>