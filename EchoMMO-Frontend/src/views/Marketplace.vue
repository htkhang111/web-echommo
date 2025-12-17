<!-- <template>
  <div class="page-container wuxia-market dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="market-overlay">
      <div class="market-header">
        <div class="header-decor left"></div>
        <h2 class="market-title">THƯƠNG HỘI</h2>
        <div class="header-decor right"></div>

        <div class="filter-bar">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm kiếm bảo vật..."
            />
          </div>
          <div class="filter-box">
            <i class="fas fa-filter"></i>
            <select v-model="filterRarity">
              <option value="ALL">Tất cả phẩm chất</option>
              <option value="C">Phẩm chất C (Thường)</option>
              <option value="B">Phẩm chất B (Hiếm)</option>
              <option value="A">Phẩm chất A (Sử thi)</option>
              <option value="S">Phẩm chất S (Truyền thuyết)</option>
            </select>
          </div>
        </div>

        <div class="market-tabs">
          <button
            :class="{ active: activeTab === 'buy' }"
            @click="switchTab('buy')"
            class="tab-btn"
          >
            <i class="fas fa-store"></i> TIỆM TẠP HÓA
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'p2p' }"
            @click="switchTab('p2p')"
            class="tab-btn"
          >
            <i class="fas fa-handshake"></i> CHỢ TRỜI
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'my_listings' }"
            @click="loadMyListings"
            class="tab-btn"
          >
            <i class="fas fa-clipboard-list"></i> SẠP CỦA TA
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'sell_sys' }"
            @click="switchTab('sell_sys')"
            class="tab-btn"
          >
            <i class="fas fa-coins"></i> BÁN ĐỒ
          </button>
        </div>
      </div>

      <div class="market-content custom-scroll">
        <transition name="fade-slide" mode="out-in">
          <div v-if="activeTab === 'buy'" class="grid-layout" key="buy">
            <div v-if="filteredShopItems.length === 0" class="empty-state">
              <i class="fas fa-search-minus"></i>
              <p>Không tìm thấy vật phẩm nào.</p>
              <small
                v-if="marketStore.shopItems.length === 0"
                style="color: #ef5350"
                >(Kho hàng hệ thống đang trống hoặc lỗi kết nối)</small
              >
            </div>
            <div
              v-for="item in filteredShopItems"
              :key="item.itemId"
              class="item-card system-card"
            >
              <div class="card-top">
                <div class="img-frame" :class="'border-' + item.rarity">
                  <img
                    :src="resolveItemImage(item.imageUrl)"
                    @error="handleImgError"
                  />
                  <span class="rarity-tag" :class="'bg-' + item.rarity">{{
                    item.rarity
                  }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-type">{{ item.type }}</div>
                <div class="price-row">
                  <span class="label">Giá:</span>
                  <span class="val gold-text"
                    >{{ formatNumber(item.basePrice) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="buyQty[item.itemId]"
                  min="1"
                  class="dark-input"
                  placeholder="1"
                />
                <button
                  @click="askBuySystem(item)"
                  class="btn-action btn-buy-sys"
                >
                  MUA
                </button>
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'p2p'" class="grid-layout" key="p2p">
            <div v-if="filteredPlayerListings.length === 0" class="empty-state">
              <i class="fas fa-wind"></i>
              <p>Chợ vắng hoặc không có món này...</p>
            </div>
            <div
              v-for="listing in filteredPlayerListings"
              :key="listing.listingId"
              class="item-card p2p-card"
            >
              <div class="seller-badge">
                <i class="fas fa-user-tag"></i>
                {{ listing.seller?.username || "Ẩn danh" }}
              </div>
              <div class="card-top p2p-img-top">
                <div class="img-frame" :class="'border-' + listing.item.rarity">
                  <img
                    :src="resolveItemImage(listing.item.imageUrl)"
                    @error="handleImgError"
                  />
                  <span
                    class="rarity-tag"
                    :class="'bg-' + listing.item.rarity"
                    >{{ listing.item.rarity }}</span
                  >
                </div>
              </div>
              <div class="card-body p2p-body">
                <div class="item-name gold-glow">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0" class="enhance-txt"
                    >(+{{ listing.enhanceLevel }})</span
                  >
                </div>
                <div class="stock-info">
                  Tồn kho: <span class="highlight">{{ listing.quantity }}</span>
                </div>
                <div class="price-row">
                  <span class="val gold-text large"
                    >{{ formatNumber(listing.price) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="p2pQty[listing.listingId]"
                  min="1"
                  :max="listing.quantity"
                  class="dark-input"
                  placeholder="1"
                />
                <button
                  @click="askBuyP2P(listing)"
                  class="btn-action btn-buy-p2p"
                >
                  <i class="fas fa-shopping-basket"></i> MUA
                </button>
              </div>
            </div>
          </div>

          <div
            v-else-if="activeTab === 'my_listings'"
            class="grid-layout"
            key="my_listings"
          >
            <div v-if="marketStore.myListings.length === 0" class="empty-state">
              <i class="fas fa-scroll"></i>
              <p>Chưa bày bán vật phẩm nào.</p>
            </div>
            <div
              v-for="listing in marketStore.myListings"
              :key="listing.listingId"
              class="item-card my-card"
            >
              <div class="card-top">
                <div class="img-frame" :class="'border-' + listing.item.rarity">
                  <img
                    :src="resolveItemImage(listing.item.imageUrl)"
                    @error="handleImgError"
                  />
                  <span class="qty-badge-corner">x{{ listing.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0"
                    >(+{{ listing.enhanceLevel }})</span
                  >
                </div>
                <div class="price-row">
                  <span class="val gold-text"
                    >{{ formatNumber(listing.price) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer full-width">
                <button
                  @click="handleCancelListing(listing.listingId)"
                  class="btn-action btn-cancel"
                >
                  THU HỒI
                </button>
              </div>
            </div>
          </div>

          <div
            v-else-if="activeTab === 'sell_sys'"
            class="grid-layout"
            key="sell_sys"
          >
            <div v-if="filteredInventory.length === 0" class="empty-state">
              <i class="fas fa-box-open"></i>
              <p>Hành trang trống hoặc không tìm thấy.</p>
            </div>
            <div
              v-for="uItem in filteredInventory"
              :key="uItem.userItemId"
              class="item-card sell-card"
            >
              <div class="card-top small-top">
                <div class="img-frame small">
                  <img
                    :src="resolveItemImage(uItem.item.imageUrl)"
                    @error="handleImgError"
                  />
                  <span class="qty-badge">x{{ uItem.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">{{ uItem.item.name }}</div>
                <div class="price-row">
                  <span class="label">Thu mua:</span>
                  <span class="val red-text"
                    >{{ formatNumber(uItem.item.basePrice * 0.8) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="sellQty[uItem.userItemId]"
                  min="1"
                  :max="uItem.quantity"
                  class="dark-input"
                  placeholder="All"
                />
                <button
                  v-if="!uItem.isEquipped"
                  @click="askSellSystem(uItem)"
                  class="btn-action btn-sell"
                >
                  BÁN
                </button>
                <div v-else class="equipped-warn">ĐANG DÙNG</div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <transition name="pop-up">
      <div
        v-if="confirmModal.visible"
        class="modal-overlay"
        @click.self="closeConfirm"
      >
        <div class="bill-modal">
          <div class="bill-header">
            <div class="bill-seal">KHẾ ƯỚC</div>
            <h3>
              {{
                confirmModal.type === "SELL"
                  ? "XÁC NHẬN BÁN"
                  : "XÁC NHẬN GIAO DỊCH"
              }}
            </h3>
          </div>
          <div class="bill-body">
            <div class="item-preview-row">
              <div class="preview-img">
                <img
                  :src="resolveItemImage(confirmModal.data.imgCode)"
                  @error="handleImgError"
                />
              </div>
              <div class="preview-info">
                <div class="p-name">{{ confirmModal.data.name }}</div>
                <div class="p-type">
                  {{ getTransactionLabel(confirmModal.type) }}
                </div>
              </div>
            </div>
            <div class="bill-calc">
              <div class="calc-row">
                <span>Đơn giá:</span
                ><span>{{ formatNumber(confirmModal.data.price) }}</span>
              </div>
              <div class="calc-row">
                <span>Số lượng:</span><span>x {{ confirmModal.data.qty }}</span>
              </div>
              <div class="divider-dashed"></div>
              <div class="calc-row total">
                <span>{{
                  confirmModal.type === "SELL" ? "THỰC LĨNH:" : "THANH TOÁN:"
                }}</span>
                <span class="total-gold"
                  >{{ formatNumber(confirmModal.data.total) }}
                  <i class="fas fa-coins"></i
                ></span>
              </div>
            </div>
          </div>
          <div class="bill-footer">
            <button class="btn-wood cancel" @click="closeConfirm">
              HỦY BỎ
            </button>
            <button class="btn-wood confirm" @click="confirmTransaction">
              <i class="fas fa-fingerprint"></i> XÁC NHẬN
            </button>
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
import { useNotificationStore } from "../stores/notificationStore";
import { resolveItemImage } from "../utils/assetHelper";

const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const notificationStore = useNotificationStore();

const activeTab = ref("buy");
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});

const searchQuery = ref("");
const filterRarity = ref("ALL");

// [FIX QUAN TRỌNG] Hàm lọc an toàn hơn. Nếu item lỗi dữ liệu, nó sẽ không làm sập trang web.
const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];

  return list.filter((entry) => {
    // Lấy object item thật sự
    const itemData = isInventory ? entry.item : entry.item || entry;

    // Nếu itemData null (lỗi dữ liệu), bỏ qua dòng này để tránh crash
    if (!itemData) return false;

    // 1. Lọc tên (An toàn: check tồn tại name trước khi toLowerCase)
    const itemName = itemData.name || "";
    const nameMatch = itemName
      .toLowerCase()
      .includes(searchQuery.value.toLowerCase());

    // 2. Lọc Rarity
    const itemRarity = itemData.rarity || "C"; // Mặc định là C nếu thiếu
    const rarityMatch =
      filterRarity.value === "ALL" || itemRarity === filterRarity.value;

    return nameMatch && rarityMatch;
  });
};

const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() =>
  filterItems(marketStore.playerListings),
);
const filteredInventory = computed(() =>
  filterItems(inventoryStore.items, true),
);

const confirmModal = reactive({
  visible: false,
  type: "",
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

const formatNumber = (n) => Number(n).toLocaleString("en-US");

const handleImgError = (e) => {
  e.target.src = "https://via.placeholder.com/64?text=IMG"; // Fallback nếu ảnh lỗi
};

const switchTab = (tab) => {
  activeTab.value = tab;
  searchQuery.value = "";
  filterRarity.value = "ALL";
};

const getTransactionLabel = (type) => {
  if (type === "P2P") return "Mua từ Chợ Trời";
  if (type === "SELL") return "Bán cho Tiệm";
  return "Mua từ Cửa Tiệm";
};

const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  openConfirm("SYS", {
    id: item.itemId,
    name: item.name,
    imgCode: item.imageUrl,
    price: item.basePrice,
    qty,
    total: item.basePrice * qty,
  });
};

const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  openConfirm("P2P", {
    id: listing.listingId,
    name: listing.item.name,
    imgCode: listing.item.imageUrl,
    price: listing.price,
    qty,
    total: listing.price * qty,
  });
};

const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = uItem.item.basePrice * 0.8;
  openConfirm("SELL", {
    id: uItem.userItemId,
    name: uItem.item.name,
    imgCode: uItem.item.imageUrl,
    price: sellPrice,
    qty,
    total: sellPrice * qty,
  });
};

const openConfirm = (type, data) => {
  confirmModal.type = type;
  confirmModal.data = data;
  confirmModal.visible = true;
};
const closeConfirm = () => {
  confirmModal.visible = false;
};

const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();

  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1;
      notificationStore.showToast(
        `Đã mua ${data.qty} ${data.name}!`,
        "success",
      );
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      notificationStore.showToast(`Giao dịch thành công!`, "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      notificationStore.showToast(
        `Đã bán ${data.qty} ${data.name}, nhận ${formatNumber(data.total)} xu!`,
        "success",
      );
    }
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings();
    const errorMsg =
      e.response?.data?.message || e.response?.data || "Giao dịch thất bại.";
    notificationStore.showToast(errorMsg, "error");
  }
};

const handleCancelListing = async (listingId) => {
  try {
    await marketStore.cancelListing(listingId);
    notificationStore.showToast("Đã thu hồi vật phẩm về kho.", "success");
  } catch (e) {
    notificationStore.showToast("Lỗi khi hủy bán.", "error");
  }
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

onMounted(async () => {
  try {
    await Promise.all([
      marketStore.fetchShopItems(),
      marketStore.fetchPlayerListings(),
      inventoryStore.fetchInventory(),
    ]);
  } catch (e) {
    console.error("Lỗi tải dữ liệu chợ:", e);
    // Có thể do lỗi auth (401) hoặc server chết
    notificationStore.showToast(
      "Không thể kết nối đến Thương Hội. Hãy thử đăng nhập lại!",
      "error",
    );
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;
  --jade-green: #2e7d32;
  --card-bg: #261815;
  --panel-bg: rgba(30, 20, 15, 0.95);
}
.dark-theme {
  background-color: #212121;
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
}
.ink-bg-layer,
.mountain-bg,
.fog-anim {
  position: absolute;
  inset: 0;
}
.ink-bg-layer {
  z-index: 0;
  background-color: #3e2723;
}
.mountain-bg {
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}
.fog-anim {
  background: linear-gradient(to top, #261815 10%, transparent 90%);
}

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

/* Header */
.market-header {
  text-align: center;
  margin-bottom: 20px;
  background: var(--panel-bg);
  border-top: 4px solid var(--wood-light);
  border-bottom: 4px solid var(--wood-light);
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}
.market-title {
  font-size: 2.5rem;
  color: var(--gold);
  margin: 0 0 15px 0;
  text-transform: uppercase;
  letter-spacing: 5px;
  font-weight: 900;
  text-shadow: 0 0 10px rgba(255, 236, 179, 0.3);
}
.header-decor {
  width: 60px;
  height: 2px;
  background: var(--gold);
  position: absolute;
  top: 50px;
}
.left {
  left: 20%;
}
.right {
  right: 20%;
}

/* [MỚI] Style cho Filter Bar */
.filter-bar {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.search-box,
.filter-box {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #5d4037;
  padding: 5px 15px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: 0.3s;
}
.search-box:focus-within,
.filter-box:hover {
  border-color: var(--gold);
  background: rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 10px rgba(255, 236, 179, 0.1);
}
.search-box i,
.filter-box i {
  color: var(--text-dim);
}
.search-box input {
  background: transparent;
  border: none;
  color: #fff;
  font-family: inherit;
  width: 200px;
  outline: none;
}
.filter-box select {
  background: transparent;
  border: none;
  color: #fff;
  font-family: inherit;
  outline: none;
  cursor: pointer;
}
.filter-box select option {
  background: #3e2723;
  color: #fff;
}

/* Tabs */
.market-tabs {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
.tab-btn {
  background: transparent;
  border: none;
  color: var(--text-dim);
  font-weight: bold;
  font-size: 1rem;
  cursor: pointer;
  padding: 10px 15px;
  transition: 0.3s;
  border-bottom: 2px solid transparent;
}
.tab-btn:hover,
.tab-btn.active {
  color: var(--gold);
}
.tab-btn.active {
  border-bottom-color: var(--gold);
  text-shadow: 0 0 8px rgba(255, 236, 179, 0.4);
}
.tab-divider {
  width: 1px;
  height: 20px;
  background: #555;
}

/* Content & Grid */
.market-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}
.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}

.grid-layout {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 50px;
  color: #757575;
  font-size: 1.2rem;
  border: 2px dashed #444;
  background: rgba(0, 0, 0, 0.2);
}
.empty-state i {
  font-size: 3rem;
  opacity: 0.5;
  margin-bottom: 10px;
  display: block;
}

/* Item Card */
.item-card {
  background: var(--card-bg);
  border: 1px solid var(--wood-light);
  border-radius: 4px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: 0.3s;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.item-card:hover {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7);
}
.card-top {
  display: flex;
  justify-content: center;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
.img-frame {
  width: 70px;
  height: 70px;
  background: rgba(0, 0, 0, 0.3);
  border: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.img-frame img {
  max-width: 90%;
  max-height: 90%;
}
.border-C {
  border-color: #bdbdbd;
}
.bg-C {
  background: #bdbdbd;
}
.border-B {
  border-color: #42a5f5;
}
.bg-B {
  background: #42a5f5;
  color: #fff;
}
.border-A {
  border-color: #ab47bc;
}
.bg-A {
  background: #ab47bc;
  color: #fff;
}
.border-S {
  border-color: var(--gold);
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.3);
}
.bg-S {
  background: var(--gold);
}
.rarity-tag {
  position: absolute;
  bottom: -8px;
  font-size: 0.65rem;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 3px;
  color: #000;
}
.qty-badge-corner {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #b71c1c;
  color: #fff;
  font-size: 0.7rem;
  padding: 2px 5px;
  border-radius: 4px;
  font-weight: bold;
}

.card-body {
  flex: 1;
  text-align: center;
}
.item-name {
  font-size: 1rem;
  font-weight: bold;
  color: var(--text-light);
  margin-bottom: 5px;
}
.item-type {
  font-size: 0.8rem;
  color: #9e9e9e;
  font-style: italic;
  margin-bottom: 8px;
}
.price-row {
  font-size: 0.95rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 5px;
}
.gold-text {
  color: var(--gold);
  font-weight: bold;
}
.red-text {
  color: #ef5350;
  font-weight: bold;
}

.card-footer {
  display: flex;
  gap: 5px;
  margin-top: auto;
}
.dark-input {
  width: 50px;
  background: #121212;
  border: 1px solid #444;
  color: var(--gold);
  text-align: center;
  font-weight: bold;
  padding: 5px;
}
.btn-action {
  flex: 1;
  border: none;
  font-weight: bold;
  cursor: pointer;
  color: #fff;
  text-transform: uppercase;
  font-size: 0.85rem;
  border-radius: 4px;
  box-shadow: 0 3px 0 rgba(0, 0, 0, 0.3);
  transition: 0.2s;
}
.btn-action:hover {
  filter: brightness(1.2);
  transform: translateY(-2px);
}
.btn-action:active {
  transform: translateY(2px);
  box-shadow: none;
}
.btn-buy-sys {
  background: var(--red-seal);
  border: 1px solid #b71c1c;
}
.btn-sell {
  background: #c62828;
  border: 1px solid #b71c1c;
}
.btn-cancel {
  background: #4e342e;
  padding: 8px;
  width: 100%;
}
.btn-buy-p2p {
  background: linear-gradient(to bottom, #4caf50, #2e7d32);
  border: 1px solid #2e7d32;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
.p2p-card {
  border-color: #81c784;
}
.seller-badge {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(46, 125, 50, 0.2);
  font-size: 0.7rem;
  color: #a5d6a7;
  text-align: center;
  padding: 2px;
}
.p2p-img-top {
  margin-top: 15px;
  border-bottom: none;
  padding-bottom: 0;
}
.p2p-body {
  padding-top: 5px;
}
.stock-info {
  font-size: 0.8rem;
  color: #aaa;
  margin-bottom: 5px;
}
.highlight {
  color: #fff;
}

/* Modal & Transition */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}
.bill-modal {
  width: 400px;
  background: #fdf5e6;
  color: #3e2723;
  border: 4px double #3e2723;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.9);
  animation: popIn 0.3s;
}
.bill-header {
  background: #3e2723;
  color: var(--gold);
  padding: 15px;
  text-align: center;
  border-bottom: 4px solid var(--red-seal);
  position: relative;
}
.bill-header h3 {
  margin: 0;
  font-size: 1.4rem;
}
.bill-seal {
  position: absolute;
  top: 5px;
  left: 10px;
  border: 2px solid var(--red-seal);
  color: var(--red-seal);
  padding: 2px 5px;
  font-weight: bold;
  transform: rotate(-15deg);
  opacity: 0.8;
  font-size: 0.8rem;
}
.bill-body {
  padding: 20px;
}
.item-preview-row {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 15px;
  background: rgba(0, 0, 0, 0.05);
  padding: 10px;
  border-radius: 4px;
}
.preview-img {
  width: 60px;
  height: 60px;
  border: 1px solid #3e2723;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-img img {
  max-width: 90%;
}
.preview-info .p-name {
  font-weight: bold;
  color: #bf360c;
}
.preview-info .p-type {
  font-size: 0.9rem;
  font-style: italic;
}
.bill-calc {
  font-family: "Courier New", monospace;
  font-weight: bold;
}
.calc-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.divider-dashed {
  border-bottom: 2px dashed #3e2723;
  margin: 10px 0;
}
.total {
  font-size: 1.2rem;
  color: #d84315;
}
.bill-footer {
  padding: 15px;
  display: flex;
  gap: 10px;
  background: rgba(0, 0, 0, 0.05);
}
.btn-wood {
  flex: 1;
  padding: 10px;
  font-weight: bold;
  cursor: pointer;
  border: 2px solid #3e2723;
  transition: 0.2s;
}
.btn-wood.cancel:hover {
  background: #d7ccc8;
}
.btn-wood.confirm {
  background: #bf360c;
  color: #fff;
  border-color: #bf360c;
}
.btn-wood.confirm:hover {
  background: #d84315;
}

@keyframes popIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style> -->
<!-- <template>
  <div class="page-container wuxia-market dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="market-overlay">
      <div class="market-header">
        <div class="header-decor left"></div>
        <h2 class="market-title">THƯƠNG HỘI</h2>
        <div class="header-decor right"></div>

        <div class="filter-bar">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm kiếm bảo vật..."
            />
          </div>
          <div class="filter-box">
            <i class="fas fa-filter"></i>
            <select v-model="filterRarity">
              <option value="ALL">Tất cả phẩm chất</option>
              <option value="C">Phẩm chất C (Thường)</option>
              <option value="B">Phẩm chất B (Hiếm)</option>
              <option value="A">Phẩm chất A (Sử thi)</option>
              <option value="S">Phẩm chất S (Truyền thuyết)</option>
            </select>
          </div>
        </div>

        <div class="market-tabs">
          <button
            :class="{ active: activeTab === 'buy' }"
            @click="switchTab('buy')"
            class="tab-btn"
          >
            <i class="fas fa-store"></i> TIỆM TẠP HÓA
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'p2p' }"
            @click="switchTab('p2p')"
            class="tab-btn"
          >
            <i class="fas fa-handshake"></i> CHỢ TRỜI
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'my_listings' }"
            @click="loadMyListings"
            class="tab-btn"
          >
            <i class="fas fa-clipboard-list"></i> SẠP CỦA TA
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'sell_sys' }"
            @click="switchTab('sell_sys')"
            class="tab-btn"
          >
            <i class="fas fa-coins"></i> BÁN ĐỒ
          </button>
        </div>
      </div>

      <div class="market-content custom-scroll">
        <transition name="fade-slide" mode="out-in">
          <div v-if="activeTab === 'buy'" class="grid-layout" key="buy">
            <div v-if="filteredShopItems.length === 0" class="empty-state">
              <i class="fas fa-search-minus"></i>
              <p>Không tìm thấy vật phẩm nào.</p>
              <small
                v-if="marketStore.shopItems.length === 0"
                style="color: #ef5350"
                >(Kho hàng hệ thống đang trống hoặc lỗi kết nối)</small
              >
            </div>
            <div
              v-for="item in filteredShopItems"
              :key="item.itemId"
              class="item-card system-card"
            >
              <div class="card-top">
                <div class="img-frame" :class="'border-' + item.rarity">
                  <img
                    :src="resolveItemImage(item.imageUrl)"
                    @error="handleImgError"
                  />
                  <span class="rarity-tag" :class="'bg-' + item.rarity">{{
                    item.rarity
                  }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-type">{{ item.type }}</div>
                <div class="price-row">
                  <span class="label">Giá:</span>
                  <span class="val gold-text"
                    >{{ formatNumber(item.basePrice) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="buyQty[item.itemId]"
                  min="1"
                  class="dark-input"
                  placeholder="1"
                />
                <button
                  @click="askBuySystem(item)"
                  class="btn-action btn-buy-sys"
                >
                  MUA
                </button>
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'p2p'" class="grid-layout" key="p2p">
            <div v-if="filteredPlayerListings.length === 0" class="empty-state">
              <i class="fas fa-wind"></i>
              <p>Chợ vắng hoặc không có món này...</p>
            </div>
            <div
              v-for="listing in filteredPlayerListings"
              :key="listing.listingId"
              class="item-card p2p-card"
            >
              <div class="seller-badge">
                <i class="fas fa-user-tag"></i>
                {{ listing.seller?.username || "Ẩn danh" }}
              </div>
              <div class="card-top p2p-img-top">
                <div class="img-frame" :class="'border-' + listing.item.rarity">
                  <img
                    :src="resolveItemImage(listing.item.imageUrl)"
                    @error="handleImgError"
                  />

                  <div
                    v-if="listing.enhanceLevel > 0"
                    class="level-tag"
                    :class="getLevelClass(listing.enhanceLevel)"
                  >
                    +{{ listing.enhanceLevel }}
                  </div>

                  <span
                    class="rarity-tag"
                    :class="'bg-' + listing.item.rarity"
                    >{{ listing.item.rarity }}</span
                  >
                </div>
              </div>
              <div class="card-body p2p-body">
                <div class="item-name gold-glow">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0" class="enhance-txt"
                    >(+{{ listing.enhanceLevel }})</span
                  >
                </div>
                <div class="stock-info">
                  Tồn kho: <span class="highlight">{{ listing.quantity }}</span>
                </div>
                <div class="price-row">
                  <span class="val gold-text large"
                    >{{ formatNumber(listing.price) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="p2pQty[listing.listingId]"
                  min="1"
                  :max="listing.quantity"
                  class="dark-input"
                  placeholder="1"
                />
                <button
                  @click="askBuyP2P(listing)"
                  class="btn-action btn-buy-p2p"
                >
                  <i class="fas fa-shopping-basket"></i> MUA
                </button>
              </div>
            </div>
          </div>

          <div
            v-else-if="activeTab === 'my_listings'"
            class="grid-layout"
            key="my_listings"
          >
            <div v-if="marketStore.myListings.length === 0" class="empty-state">
              <i class="fas fa-scroll"></i>
              <p>Chưa bày bán vật phẩm nào.</p>
            </div>
            <div
              v-for="listing in marketStore.myListings"
              :key="listing.listingId"
              class="item-card my-card"
            >
              <div class="card-top">
                <div class="img-frame" :class="'border-' + listing.item.rarity">
                  <img
                    :src="resolveItemImage(listing.item.imageUrl)"
                    @error="handleImgError"
                  />

                  <div
                    v-if="listing.enhanceLevel > 0"
                    class="level-tag top-left"
                    :class="getLevelClass(listing.enhanceLevel)"
                  >
                    +{{ listing.enhanceLevel }}
                  </div>

                  <span class="qty-badge-corner">x{{ listing.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0"
                    >(+{{ listing.enhanceLevel }})</span
                  >
                </div>
                <div class="price-row">
                  <span class="val gold-text"
                    >{{ formatNumber(listing.price) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer full-width">
                <button
                  @click="handleCancelListing(listing.listingId)"
                  class="btn-action btn-cancel"
                >
                  THU HỒI
                </button>
              </div>
            </div>
          </div>

          <div
            v-else-if="activeTab === 'sell_sys'"
            class="grid-layout"
            key="sell_sys"
          >
            <div v-if="filteredInventory.length === 0" class="empty-state">
              <i class="fas fa-box-open"></i>
              <p>Hành trang trống hoặc không tìm thấy.</p>
            </div>
            <div
              v-for="uItem in filteredInventory"
              :key="uItem.userItemId"
              class="item-card sell-card"
            >
              <div class="card-top small-top">
                <div class="img-frame small">
                  <img
                    :src="resolveItemImage(uItem.item.imageUrl)"
                    @error="handleImgError"
                  />

                  <div
                    v-if="
                      (uItem.enhanceLevel && uItem.enhanceLevel > 0) ||
                      (uItem.level && uItem.level > 0)
                    "
                    class="level-tag"
                    :class="getLevelClass(uItem.enhanceLevel || uItem.level)"
                  >
                    +{{ uItem.enhanceLevel || uItem.level }}
                  </div>

                  <span class="qty-badge">x{{ uItem.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">
                  {{ uItem.item.name }}
                  <span
                    v-if="
                      (uItem.enhanceLevel && uItem.enhanceLevel > 0) ||
                      (uItem.level && uItem.level > 0)
                    "
                    style="font-size: 0.8em; color: gold"
                  >
                    (+{{ uItem.enhanceLevel || uItem.level }})
                  </span>
                </div>
                <div class="price-row">
                  <span class="label">Thu mua:</span>
                  <span class="val red-text"
                    >{{ formatNumber(uItem.item.basePrice * 0.8) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="sellQty[uItem.userItemId]"
                  min="1"
                  :max="uItem.quantity"
                  class="dark-input"
                  placeholder="All"
                />
                <button
                  v-if="!uItem.isEquipped"
                  @click="askSellSystem(uItem)"
                  class="btn-action btn-sell"
                >
                  BÁN
                </button>
                <div v-else class="equipped-warn">ĐANG DÙNG</div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <transition name="pop-up">
      <div
        v-if="confirmModal.visible"
        class="modal-overlay"
        @click.self="closeConfirm"
      >
        <div class="bill-modal">
          <div class="bill-header">
            <div class="bill-seal">KHẾ ƯỚC</div>
            <h3>
              {{
                confirmModal.type === "SELL"
                  ? "XÁC NHẬN BÁN"
                  : "XÁC NHẬN GIAO DỊCH"
              }}
            </h3>
          </div>
          <div class="bill-body">
            <div class="item-preview-row">
              <div class="preview-img">
                <img
                  :src="resolveItemImage(confirmModal.data.imgCode)"
                  @error="handleImgError"
                />
              </div>
              <div class="preview-info">
                <div class="p-name">{{ confirmModal.data.name }}</div>
                <div class="p-type">
                  {{ getTransactionLabel(confirmModal.type) }}
                </div>
              </div>
            </div>
            <div class="bill-calc">
              <div class="calc-row">
                <span>Đơn giá:</span
                ><span>{{ formatNumber(confirmModal.data.price) }}</span>
              </div>
              <div class="calc-row">
                <span>Số lượng:</span><span>x {{ confirmModal.data.qty }}</span>
              </div>
              <div class="divider-dashed"></div>
              <div class="calc-row total">
                <span>{{
                  confirmModal.type === "SELL" ? "THỰC LĨNH:" : "THANH TOÁN:"
                }}</span>
                <span class="total-gold"
                  >{{ formatNumber(confirmModal.data.total) }}
                  <i class="fas fa-coins"></i
                ></span>
              </div>
            </div>
          </div>
          <div class="bill-footer">
            <button class="btn-wood cancel" @click="closeConfirm">
              HỦY BỎ
            </button>
            <button class="btn-wood confirm" @click="confirmTransaction">
              <i class="fas fa-fingerprint"></i> XÁC NHẬN
            </button>
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
import { useNotificationStore } from "../stores/notificationStore";
import { resolveItemImage } from "../utils/assetHelper";

const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const notificationStore = useNotificationStore();

const activeTab = ref("buy");
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});

const searchQuery = ref("");
const filterRarity = ref("ALL");

// [MỚI] Hàm lấy class màu sắc level
const getLevelClass = (lv) => {
  if (!lv) return "";
  if (lv >= 15) return "lvl-red";
  if (lv >= 10) return "lvl-purple";
  if (lv >= 5) return "lvl-gold";
  return "lvl-white";
};

// [FIX] Hàm lọc an toàn
const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];

  return list.filter((entry) => {
    const itemData = isInventory ? entry.item : entry.item || entry;
    if (!itemData) return false;

    const itemName = itemData.name || "";
    const nameMatch = itemName
      .toLowerCase()
      .includes(searchQuery.value.toLowerCase());

    const itemRarity = itemData.rarity || "C";
    const rarityMatch =
      filterRarity.value === "ALL" || itemRarity === filterRarity.value;

    return nameMatch && rarityMatch;
  });
};

const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() =>
  filterItems(marketStore.playerListings),
);
const filteredInventory = computed(() =>
  filterItems(inventoryStore.items, true),
);

const confirmModal = reactive({
  visible: false,
  type: "",
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

const formatNumber = (n) => Number(n).toLocaleString("en-US");

const handleImgError = (e) => {
  e.target.src = "https://via.placeholder.com/64?text=IMG";
};

const switchTab = (tab) => {
  activeTab.value = tab;
  searchQuery.value = "";
  filterRarity.value = "ALL";
};

const getTransactionLabel = (type) => {
  if (type === "P2P") return "Mua từ Chợ Trời";
  if (type === "SELL") return "Bán cho Tiệm";
  return "Mua từ Cửa Tiệm";
};

const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  openConfirm("SYS", {
    id: item.itemId,
    name: item.name,
    imgCode: item.imageUrl,
    price: item.basePrice,
    qty,
    total: item.basePrice * qty,
  });
};

const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  openConfirm("P2P", {
    id: listing.listingId,
    name: listing.item.name,
    imgCode: listing.item.imageUrl,
    price: listing.price,
    qty,
    total: listing.price * qty,
  });
};

const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = uItem.item.basePrice * 0.8;
  openConfirm("SELL", {
    id: uItem.userItemId,
    name: uItem.item.name,
    imgCode: uItem.item.imageUrl,
    price: sellPrice,
    qty,
    total: sellPrice * qty,
  });
};

const openConfirm = (type, data) => {
  confirmModal.type = type;
  confirmModal.data = data;
  confirmModal.visible = true;
};
const closeConfirm = () => {
  confirmModal.visible = false;
};

const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();

  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1;
      notificationStore.showToast(
        `Đã mua ${data.qty} ${data.name}!`,
        "success",
      );
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      notificationStore.showToast(`Giao dịch thành công!`, "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      notificationStore.showToast(
        `Đã bán ${data.qty} ${data.name}, nhận ${formatNumber(data.total)} xu!`,
        "success",
      );
    }
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings();
    const errorMsg =
      e.response?.data?.message || e.response?.data || "Giao dịch thất bại.";
    notificationStore.showToast(errorMsg, "error");
  }
};

const handleCancelListing = async (listingId) => {
  try {
    await marketStore.cancelListing(listingId);
    notificationStore.showToast("Đã thu hồi vật phẩm về kho.", "success");
  } catch (e) {
    notificationStore.showToast("Lỗi khi hủy bán.", "error");
  }
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

onMounted(async () => {
  try {
    await Promise.all([
      marketStore.fetchShopItems(),
      marketStore.fetchPlayerListings(),
      inventoryStore.fetchInventory(),
    ]);
  } catch (e) {
    console.error("Lỗi tải dữ liệu chợ:", e);
    notificationStore.showToast(
      "Không thể kết nối đến Thương Hội. Hãy thử đăng nhập lại!",
      "error",
    );
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;
  --jade-green: #2e7d32;
  --card-bg: #261815;
  --panel-bg: rgba(30, 20, 15, 0.95);
}
.dark-theme {
  background-color: #212121;
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
}
.ink-bg-layer,
.mountain-bg,
.fog-anim {
  position: absolute;
  inset: 0;
}
.ink-bg-layer {
  z-index: 0;
  background-color: #3e2723;
}
.mountain-bg {
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}
.fog-anim {
  background: linear-gradient(to top, #261815 10%, transparent 90%);
}

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

/* Header */
.market-header {
  text-align: center;
  margin-bottom: 20px;
  background: var(--panel-bg);
  border-top: 4px solid var(--wood-light);
  border-bottom: 4px solid var(--wood-light);
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}
.market-title {
  font-size: 2.5rem;
  color: var(--gold);
  margin: 0 0 15px 0;
  text-transform: uppercase;
  letter-spacing: 5px;
  font-weight: 900;
  text-shadow: 0 0 10px rgba(255, 236, 179, 0.3);
}
.header-decor {
  width: 60px;
  height: 2px;
  background: var(--gold);
  position: absolute;
  top: 50px;
}
.left {
  left: 20%;
}
.right {
  right: 20%;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.search-box,
.filter-box {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #5d4037;
  padding: 5px 15px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: 0.3s;
}
.search-box:focus-within,
.filter-box:hover {
  border-color: var(--gold);
  background: rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 10px rgba(255, 236, 179, 0.1);
}
.search-box i,
.filter-box i {
  color: var(--text-dim);
}
.search-box input {
  background: transparent;
  border: none;
  color: #fff;
  font-family: inherit;
  width: 200px;
  outline: none;
}
.filter-box select {
  background: transparent;
  border: none;
  color: #fff;
  font-family: inherit;
  outline: none;
  cursor: pointer;
}
.filter-box select option {
  background: #3e2723;
  color: #fff;
}

/* Tabs */
.market-tabs {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
.tab-btn {
  background: transparent;
  border: none;
  color: var(--text-dim);
  font-weight: bold;
  font-size: 1rem;
  cursor: pointer;
  padding: 10px 15px;
  transition: 0.3s;
  border-bottom: 2px solid transparent;
}
.tab-btn:hover,
.tab-btn.active {
  color: var(--gold);
}
.tab-btn.active {
  border-bottom-color: var(--gold);
  text-shadow: 0 0 8px rgba(255, 236, 179, 0.4);
}
.tab-divider {
  width: 1px;
  height: 20px;
  background: #555;
}

/* Content & Grid */
.market-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}
.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}

.grid-layout {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 50px;
  color: #757575;
  font-size: 1.2rem;
  border: 2px dashed #444;
  background: rgba(0, 0, 0, 0.2);
}
.empty-state i {
  font-size: 3rem;
  opacity: 0.5;
  margin-bottom: 10px;
  display: block;
}

/* Item Card */
.item-card {
  background: var(--card-bg);
  border: 1px solid var(--wood-light);
  border-radius: 4px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: 0.3s;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.item-card:hover {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7);
}
.card-top {
  display: flex;
  justify-content: center;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
.img-frame {
  width: 70px;
  height: 70px;
  background: rgba(0, 0, 0, 0.3);
  border: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.img-frame img {
  max-width: 90%;
  max-height: 90%;
}

/* [MỚI] CSS LEVEL TAG */
.level-tag {
  position: absolute;
  top: 2px;
  right: 2px;
  font-size: 10px;
  font-weight: 900;
  z-index: 5;
  font-family: sans-serif;
  padding: 0 3px;
  border-radius: 3px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
/* Level tag ở góc trái cho case My Listings */
.level-tag.top-left {
  right: auto;
  left: 2px;
}

.lvl-white {
  color: #fff;
}
.lvl-gold {
  color: #ffd700;
  border-color: #ffd700;
  box-shadow: 0 0 5px #ffd700;
}
.lvl-purple {
  color: #d580ff;
  border-color: #d580ff;
  box-shadow: 0 0 5px #d580ff;
}
.lvl-red {
  color: #ff3333;
  border-color: #ff3333;
  box-shadow: 0 0 8px #ff0000;
}

.border-C {
  border-color: #bdbdbd;
}
.bg-C {
  background: #bdbdbd;
}
.border-B {
  border-color: #42a5f5;
}
.bg-B {
  background: #42a5f5;
  color: #fff;
}
.border-A {
  border-color: #ab47bc;
}
.bg-A {
  background: #ab47bc;
  color: #fff;
}
.border-S {
  border-color: var(--gold);
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.3);
}
.bg-S {
  background: var(--gold);
}

.rarity-tag {
  position: absolute;
  bottom: -8px;
  font-size: 0.65rem;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 3px;
  color: #000;
}
.qty-badge-corner {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #b71c1c;
  color: #fff;
  font-size: 0.7rem;
  padding: 2px 5px;
  border-radius: 4px;
  font-weight: bold;
}

.card-body {
  flex: 1;
  text-align: center;
}
.item-name {
  font-size: 1rem;
  font-weight: bold;
  color: var(--text-light);
  margin-bottom: 5px;
}
.item-type {
  font-size: 0.8rem;
  color: #9e9e9e;
  font-style: italic;
  margin-bottom: 8px;
}
.price-row {
  font-size: 0.95rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 5px;
}
.gold-text {
  color: var(--gold);
  font-weight: bold;
}
.red-text {
  color: #ef5350;
  font-weight: bold;
}

.card-footer {
  display: flex;
  gap: 5px;
  margin-top: auto;
}
.dark-input {
  width: 50px;
  background: #121212;
  border: 1px solid #444;
  color: var(--gold);
  text-align: center;
  font-weight: bold;
  padding: 5px;
}
.btn-action {
  flex: 1;
  border: none;
  font-weight: bold;
  cursor: pointer;
  color: #fff;
  text-transform: uppercase;
  font-size: 0.85rem;
  border-radius: 4px;
  box-shadow: 0 3px 0 rgba(0, 0, 0, 0.3);
  transition: 0.2s;
}
.btn-action:hover {
  filter: brightness(1.2);
  transform: translateY(-2px);
}
.btn-action:active {
  transform: translateY(2px);
  box-shadow: none;
}
.btn-buy-sys {
  background: var(--red-seal);
  border: 1px solid #b71c1c;
}
.btn-sell {
  background: #c62828;
  border: 1px solid #b71c1c;
}
.btn-cancel {
  background: #4e342e;
  padding: 8px;
  width: 100%;
}
.btn-buy-p2p {
  background: linear-gradient(to bottom, #4caf50, #2e7d32);
  border: 1px solid #2e7d32;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
.p2p-card {
  border-color: #81c784;
}
.seller-badge {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(46, 125, 50, 0.2);
  font-size: 0.7rem;
  color: #a5d6a7;
  text-align: center;
  padding: 2px;
}
.p2p-img-top {
  margin-top: 15px;
  border-bottom: none;
  padding-bottom: 0;
}
.p2p-body {
  padding-top: 5px;
}
.stock-info {
  font-size: 0.8rem;
  color: #aaa;
  margin-bottom: 5px;
}
.highlight {
  color: #fff;
}

/* Modal & Transition */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}
.bill-modal {
  width: 400px;
  background: #fdf5e6;
  color: #3e2723;
  border: 4px double #3e2723;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.9);
  animation: popIn 0.3s;
}
.bill-header {
  background: #3e2723;
  color: var(--gold);
  padding: 15px;
  text-align: center;
  border-bottom: 4px solid var(--red-seal);
  position: relative;
}
.bill-header h3 {
  margin: 0;
  font-size: 1.4rem;
}
.bill-seal {
  position: absolute;
  top: 5px;
  left: 10px;
  border: 2px solid var(--red-seal);
  color: var(--red-seal);
  padding: 2px 5px;
  font-weight: bold;
  transform: rotate(-15deg);
  opacity: 0.8;
  font-size: 0.8rem;
}
.bill-body {
  padding: 20px;
}
.item-preview-row {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 15px;
  background: rgba(0, 0, 0, 0.05);
  padding: 10px;
  border-radius: 4px;
}
.preview-img {
  width: 60px;
  height: 60px;
  border: 1px solid #3e2723;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-img img {
  max-width: 90%;
}
.preview-info .p-name {
  font-weight: bold;
  color: #bf360c;
}
.preview-info .p-type {
  font-size: 0.9rem;
  font-style: italic;
}
.bill-calc {
  font-family: "Courier New", monospace;
  font-weight: bold;
}
.calc-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.divider-dashed {
  border-bottom: 2px dashed #3e2723;
  margin: 10px 0;
}
.total {
  font-size: 1.2rem;
  color: #d84315;
}
.bill-footer {
  padding: 15px;
  display: flex;
  gap: 10px;
  background: rgba(0, 0, 0, 0.05);
}
.btn-wood {
  flex: 1;
  padding: 10px;
  font-weight: bold;
  cursor: pointer;
  border: 2px solid #3e2723;
  transition: 0.2s;
}
.btn-wood.cancel:hover {
  background: #d7ccc8;
}
.btn-wood.confirm {
  background: #bf360c;
  color: #fff;
  border-color: #bf360c;
}
.btn-wood.confirm:hover {
  background: #d84315;
}

@keyframes popIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style> -->
<template>
  <div class="page-container wuxia-market dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="market-overlay">
      <div class="market-header">
        <div class="header-decor left"></div>
        <h2 class="market-title">THƯƠNG HỘI</h2>
        <div class="header-decor right"></div>

        <div class="filter-bar">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm kiếm bảo vật..."
            />
          </div>
          <div class="filter-box">
            <i class="fas fa-filter"></i>
            <select v-model="filterRarity">
              <option value="ALL">Tất cả phẩm chất</option>
              <option value="C">Phẩm chất C (Thường)</option>
              <option value="B">Phẩm chất B (Hiếm)</option>
              <option value="A">Phẩm chất A (Sử thi)</option>
              <option value="S">Phẩm chất S (Truyền thuyết)</option>
            </select>
          </div>
        </div>

        <div class="market-tabs">
          <button
            :class="{ active: activeTab === 'buy' }"
            @click="switchTab('buy')"
            class="tab-btn"
          >
            <i class="fas fa-store"></i> TIỆM TẠP HÓA
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'p2p' }"
            @click="switchTab('p2p')"
            class="tab-btn"
          >
            <i class="fas fa-handshake"></i> CHỢ TRỜI
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'my_listings' }"
            @click="loadMyListings"
            class="tab-btn"
          >
            <i class="fas fa-clipboard-list"></i> SẠP CỦA TA
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'sell_sys' }"
            @click="switchTab('sell_sys')"
            class="tab-btn"
          >
            <i class="fas fa-coins"></i> BÁN ĐỒ
          </button>
        </div>
      </div>

      <div class="market-content custom-scroll">
        <transition name="fade-slide" mode="out-in">
          <div v-if="activeTab === 'buy'" class="grid-layout" key="buy">
            <div v-if="filteredShopItems.length === 0" class="empty-state">
              <i class="fas fa-search-minus"></i>
              <p>Không tìm thấy trang bị nào.</p>
            </div>
            <div
              v-for="item in filteredShopItems"
              :key="item.itemId"
              class="item-card system-card"
            >
              <div class="card-top">
                <div class="img-frame" :class="'border-' + item.rarity">
                  <img
                    :src="resolveItemImage(item.imageUrl)"
                    @error="handleImgError"
                  />
                  <span class="rarity-tag" :class="'bg-' + item.rarity">{{
                    item.rarity
                  }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-type">{{ item.type }}</div>
                <div class="price-row">
                  <span class="label">Giá:</span>
                  <span class="val gold-text"
                    >{{ formatNumber(item.basePrice) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="buyQty[item.itemId]"
                  min="1"
                  class="dark-input"
                  placeholder="1"
                />
                <button
                  @click="askBuySystem(item)"
                  class="btn-action btn-buy-sys"
                >
                  MUA
                </button>
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'p2p'" class="grid-layout" key="p2p">
            <div v-if="filteredPlayerListings.length === 0" class="empty-state">
              <i class="fas fa-wind"></i>
              <p>Chợ vắng hoặc không có trang bị này...</p>
            </div>
            <div
              v-for="listing in filteredPlayerListings"
              :key="listing.listingId"
              class="item-card p2p-card"
            >
              <div class="seller-badge">
                <i class="fas fa-user-tag"></i>
                {{ listing.seller?.username || "Ẩn danh" }}
              </div>
              <div class="card-top p2p-img-top">
                <div class="img-frame" :class="'border-' + listing.item.rarity">
                  <img
                    :src="resolveItemImage(listing.item.imageUrl)"
                    @error="handleImgError"
                  />

                  <div
                    v-if="listing.enhanceLevel > 0"
                    class="level-tag"
                    :class="getLevelClass(listing.enhanceLevel)"
                  >
                    +{{ listing.enhanceLevel }}
                  </div>

                  <span
                    class="rarity-tag"
                    :class="'bg-' + listing.item.rarity"
                    >{{ listing.item.rarity }}</span
                  >
                </div>
              </div>
              <div class="card-body p2p-body">
                <div class="item-name gold-glow">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0" class="enhance-txt"
                    >(+{{ listing.enhanceLevel }})</span
                  >
                </div>
                <div class="stock-info">
                  Tồn kho: <span class="highlight">{{ listing.quantity }}</span>
                </div>
                <div class="price-row">
                  <span class="val gold-text large"
                    >{{ formatNumber(listing.price) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="p2pQty[listing.listingId]"
                  min="1"
                  :max="listing.quantity"
                  class="dark-input"
                  placeholder="1"
                />
                <button
                  @click="askBuyP2P(listing)"
                  class="btn-action btn-buy-p2p"
                >
                  <i class="fas fa-shopping-basket"></i> MUA
                </button>
              </div>
            </div>
          </div>

          <div
            v-else-if="activeTab === 'my_listings'"
            class="grid-layout"
            key="my_listings"
          >
            <div v-if="marketStore.myListings.length === 0" class="empty-state">
              <i class="fas fa-scroll"></i>
              <p>Chưa bày bán vật phẩm nào.</p>
            </div>
            <div
              v-for="listing in marketStore.myListings"
              :key="listing.listingId"
              class="item-card my-card"
            >
              <div class="card-top">
                <div class="img-frame" :class="'border-' + listing.item.rarity">
                  <img
                    :src="resolveItemImage(listing.item.imageUrl)"
                    @error="handleImgError"
                  />

                  <div
                    v-if="listing.enhanceLevel > 0"
                    class="level-tag top-left"
                    :class="getLevelClass(listing.enhanceLevel)"
                  >
                    +{{ listing.enhanceLevel }}
                  </div>

                  <span class="qty-badge-corner">x{{ listing.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0"
                    >(+{{ listing.enhanceLevel }})</span
                  >
                </div>
                <div class="price-row">
                  <span class="val gold-text"
                    >{{ formatNumber(listing.price) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer full-width">
                <button
                  @click="handleCancelListing(listing.listingId)"
                  class="btn-action btn-cancel"
                >
                  THU HỒI
                </button>
              </div>
            </div>
          </div>

          <div
            v-else-if="activeTab === 'sell_sys'"
            class="grid-layout"
            key="sell_sys"
          >
            <div v-if="filteredInventory.length === 0" class="empty-state">
              <i class="fas fa-box-open"></i>
              <p>Hành trang trống hoặc không tìm thấy.</p>
            </div>
            <div
              v-for="uItem in filteredInventory"
              :key="uItem.userItemId"
              class="item-card sell-card"
            >
              <div class="card-top small-top">
                <div class="img-frame small">
                  <img
                    :src="resolveItemImage(uItem.item.imageUrl)"
                    @error="handleImgError"
                  />
                  <div
                    v-if="
                      (uItem.enhanceLevel && uItem.enhanceLevel > 0) ||
                      (uItem.level && uItem.level > 0)
                    "
                    class="level-tag"
                    :class="getLevelClass(uItem.enhanceLevel || uItem.level)"
                  >
                    +{{ uItem.enhanceLevel || uItem.level }}
                  </div>

                  <span class="qty-badge">x{{ uItem.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">
                  {{ uItem.item.name }}
                  <span
                    v-if="
                      (uItem.enhanceLevel && uItem.enhanceLevel > 0) ||
                      (uItem.level && uItem.level > 0)
                    "
                    style="font-size: 0.8em; color: gold"
                  >
                    (+{{ uItem.enhanceLevel || uItem.level }})
                  </span>
                </div>
                <div class="price-row">
                  <span class="label">Thu mua:</span>
                  <span class="val red-text"
                    >{{ formatNumber(uItem.item.basePrice * 0.8) }}
                    <i class="fas fa-coins"></i
                  ></span>
                </div>
              </div>
              <div class="card-footer">
                <input
                  type="number"
                  v-model.number="sellQty[uItem.userItemId]"
                  min="1"
                  :max="uItem.quantity"
                  class="dark-input"
                  placeholder="All"
                />
                <button
                  v-if="!uItem.isEquipped"
                  @click="askSellSystem(uItem)"
                  class="btn-action btn-sell"
                >
                  BÁN
                </button>
                <div v-else class="equipped-warn">ĐANG DÙNG</div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <transition name="pop-up">
      <div
        v-if="confirmModal.visible"
        class="modal-overlay"
        @click.self="closeConfirm"
      >
        <div class="bill-modal">
          <div class="bill-header">
            <div class="bill-seal">KHẾ ƯỚC</div>
            <h3>
              {{
                confirmModal.type === "SELL"
                  ? "XÁC NHẬN BÁN"
                  : "XÁC NHẬN GIAO DỊCH"
              }}
            </h3>
          </div>
          <div class="bill-body">
            <div class="item-preview-row">
              <div class="preview-img">
                <img
                  :src="resolveItemImage(confirmModal.data.imgCode)"
                  @error="handleImgError"
                />
              </div>
              <div class="preview-info">
                <div class="p-name">{{ confirmModal.data.name }}</div>
                <div class="p-type">
                  {{ getTransactionLabel(confirmModal.type) }}
                </div>
              </div>
            </div>
            <div class="bill-calc">
              <div class="calc-row">
                <span>Đơn giá:</span
                ><span>{{ formatNumber(confirmModal.data.price) }}</span>
              </div>
              <div class="calc-row">
                <span>Số lượng:</span><span>x {{ confirmModal.data.qty }}</span>
              </div>
              <div class="divider-dashed"></div>
              <div class="calc-row total">
                <span>{{
                  confirmModal.type === "SELL" ? "THỰC LĨNH:" : "THANH TOÁN:"
                }}</span>
                <span class="total-gold"
                  >{{ formatNumber(confirmModal.data.total) }}
                  <i class="fas fa-coins"></i
                ></span>
              </div>
            </div>
          </div>
          <div class="bill-footer">
            <button class="btn-wood cancel" @click="closeConfirm">
              HỦY BỎ
            </button>
            <button class="btn-wood confirm" @click="confirmTransaction">
              <i class="fas fa-fingerprint"></i> XÁC NHẬN
            </button>
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
import { useNotificationStore } from "../stores/notificationStore";
import { resolveItemImage } from "../utils/assetHelper";

const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const notificationStore = useNotificationStore();

const activeTab = ref("buy");
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});

const searchQuery = ref("");
const filterRarity = ref("ALL");

const getLevelClass = (lv) => {
  if (!lv) return "";
  if (lv >= 15) return "lvl-red";
  if (lv >= 10) return "lvl-purple";
  if (lv >= 5) return "lvl-gold";
  return "lvl-white";
};

// [FIX QUAN TRỌNG] Hàm lọc: Bỏ MATERIAL khỏi Chợ và Shop
const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];

  return list.filter((entry) => {
    const itemData = isInventory ? entry.item : entry.item || entry;
    if (!itemData) return false;

    // [LOGIC MỚI] Ẩn MATERIAL nếu không phải là Inventory (để bán NPC)
    // Nghĩa là: Tab Mua (Shop) và Tab Chợ (P2P) sẽ KHÔNG hiện MATERIAL
    if (!isInventory && itemData.type === "MATERIAL") {
      return false;
    }

    const itemName = itemData.name || "";
    const nameMatch = itemName
      .toLowerCase()
      .includes(searchQuery.value.toLowerCase());

    const itemRarity = itemData.rarity || "C";
    const rarityMatch =
      filterRarity.value === "ALL" || itemRarity === filterRarity.value;

    return nameMatch && rarityMatch;
  });
};

const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() =>
  filterItems(marketStore.playerListings),
);
const filteredInventory = computed(() =>
  filterItems(inventoryStore.items, true),
);

const confirmModal = reactive({
  visible: false,
  type: "",
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

const formatNumber = (n) => Number(n).toLocaleString("en-US");

const handleImgError = (e) => {
  e.target.src = "https://via.placeholder.com/64?text=IMG";
};

const switchTab = (tab) => {
  activeTab.value = tab;
  searchQuery.value = "";
  filterRarity.value = "ALL";
};

const getTransactionLabel = (type) => {
  if (type === "P2P") return "Mua từ Chợ Trời";
  if (type === "SELL") return "Bán cho Tiệm";
  return "Mua từ Cửa Tiệm";
};

const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  openConfirm("SYS", {
    id: item.itemId,
    name: item.name,
    imgCode: item.imageUrl,
    price: item.basePrice,
    qty,
    total: item.basePrice * qty,
  });
};

const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  openConfirm("P2P", {
    id: listing.listingId,
    name: listing.item.name,
    imgCode: listing.item.imageUrl,
    price: listing.price,
    qty,
    total: listing.price * qty,
  });
};

const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = uItem.item.basePrice * 0.8;
  openConfirm("SELL", {
    id: uItem.userItemId,
    name: uItem.item.name,
    imgCode: uItem.item.imageUrl,
    price: sellPrice,
    qty,
    total: sellPrice * qty,
  });
};

const openConfirm = (type, data) => {
  confirmModal.type = type;
  confirmModal.data = data;
  confirmModal.visible = true;
};
const closeConfirm = () => {
  confirmModal.visible = false;
};

const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();

  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1;
      notificationStore.showToast(
        `Đã mua ${data.qty} ${data.name}!`,
        "success",
      );
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      notificationStore.showToast(`Giao dịch thành công!`, "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      notificationStore.showToast(
        `Đã bán ${data.qty} ${data.name}, nhận ${formatNumber(data.total)} xu!`,
        "success",
      );
    }
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings();
    const errorMsg =
      e.response?.data?.message || e.response?.data || "Giao dịch thất bại.";
    notificationStore.showToast(errorMsg, "error");
  }
};

const handleCancelListing = async (listingId) => {
  try {
    await marketStore.cancelListing(listingId);
    notificationStore.showToast("Đã thu hồi vật phẩm về kho.", "success");
  } catch (e) {
    notificationStore.showToast("Lỗi khi hủy bán.", "error");
  }
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

onMounted(async () => {
  try {
    await Promise.all([
      marketStore.fetchShopItems(),
      marketStore.fetchPlayerListings(),
      inventoryStore.fetchInventory(),
    ]);
  } catch (e) {
    console.error("Lỗi tải dữ liệu chợ:", e);
    notificationStore.showToast(
      "Không thể kết nối đến Thương Hội. Hãy thử đăng nhập lại!",
      "error",
    );
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;
  --jade-green: #2e7d32;
  --card-bg: #261815;
  --panel-bg: rgba(30, 20, 15, 0.95);
}
.dark-theme {
  background-color: #212121;
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
}
.ink-bg-layer,
.mountain-bg,
.fog-anim {
  position: absolute;
  inset: 0;
}
.ink-bg-layer {
  z-index: 0;
  background-color: #3e2723;
}
.mountain-bg {
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}
.fog-anim {
  background: linear-gradient(to top, #261815 10%, transparent 90%);
}

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

/* Header */
.market-header {
  text-align: center;
  margin-bottom: 20px;
  background: var(--panel-bg);
  border-top: 4px solid var(--wood-light);
  border-bottom: 4px solid var(--wood-light);
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}
.market-title {
  font-size: 2.5rem;
  color: var(--gold);
  margin: 0 0 15px 0;
  text-transform: uppercase;
  letter-spacing: 5px;
  font-weight: 900;
  text-shadow: 0 0 10px rgba(255, 236, 179, 0.3);
}
.header-decor {
  width: 60px;
  height: 2px;
  background: var(--gold);
  position: absolute;
  top: 50px;
}
.left {
  left: 20%;
}
.right {
  right: 20%;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.search-box,
.filter-box {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #5d4037;
  padding: 5px 15px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: 0.3s;
}
.search-box:focus-within,
.filter-box:hover {
  border-color: var(--gold);
  background: rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 10px rgba(255, 236, 179, 0.1);
}
.search-box i,
.filter-box i {
  color: var(--text-dim);
}
.search-box input {
  background: transparent;
  border: none;
  color: #fff;
  font-family: inherit;
  width: 200px;
  outline: none;
}
.filter-box select {
  background: transparent;
  border: none;
  color: #fff;
  font-family: inherit;
  outline: none;
  cursor: pointer;
}
.filter-box select option {
  background: #3e2723;
  color: #fff;
}

/* Tabs */
.market-tabs {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
.tab-btn {
  background: transparent;
  border: none;
  color: var(--text-dim);
  font-weight: bold;
  font-size: 1rem;
  cursor: pointer;
  padding: 10px 15px;
  transition: 0.3s;
  border-bottom: 2px solid transparent;
}
.tab-btn:hover,
.tab-btn.active {
  color: var(--gold);
}
.tab-btn.active {
  border-bottom-color: var(--gold);
  text-shadow: 0 0 8px rgba(255, 236, 179, 0.4);
}
.tab-divider {
  width: 1px;
  height: 20px;
  background: #555;
}

/* Content & Grid */
.market-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}
.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}

.grid-layout {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 50px;
  color: #757575;
  font-size: 1.2rem;
  border: 2px dashed #444;
  background: rgba(0, 0, 0, 0.2);
}
.empty-state i {
  font-size: 3rem;
  opacity: 0.5;
  margin-bottom: 10px;
  display: block;
}

/* Item Card */
.item-card {
  background: var(--card-bg);
  border: 1px solid var(--wood-light);
  border-radius: 4px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: 0.3s;
  position: relative;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.item-card:hover {
  transform: translateY(-5px);
  border-color: var(--gold);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7);
}
.card-top {
  display: flex;
  justify-content: center;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
.img-frame {
  width: 70px;
  height: 70px;
  background: rgba(0, 0, 0, 0.3);
  border: 2px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.img-frame img {
  max-width: 90%;
  max-height: 90%;
}

/* LEVEL TAG */
.level-tag {
  position: absolute;
  top: 2px;
  right: 2px;
  font-size: 10px;
  font-weight: 900;
  z-index: 5;
  font-family: sans-serif;
  padding: 0 3px;
  border-radius: 3px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.level-tag.top-left {
  right: auto;
  left: 2px;
}
.lvl-white { color: #fff; }
.lvl-gold { color: #ffd700; border-color: #ffd700; box-shadow: 0 0 5px #ffd700; }
.lvl-purple { color: #d580ff; border-color: #d580ff; box-shadow: 0 0 5px #d580ff; }
.lvl-red { color: #ff3333; border-color: #ff3333; box-shadow: 0 0 8px #ff0000; }

.border-C { border-color: #bdbdbd; }
.bg-C { background: #bdbdbd; }
.border-B { border-color: #42a5f5; }
.bg-B { background: #42a5f5; color: #fff; }
.border-A { border-color: #ab47bc; }
.bg-A { background: #ab47bc; color: #fff; }
.border-S { border-color: var(--gold); box-shadow: 0 0 10px rgba(255, 215, 0, 0.3); }
.bg-S { background: var(--gold); }

.rarity-tag {
  position: absolute;
  bottom: -8px;
  font-size: 0.65rem;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 3px;
  color: #000;
}
.qty-badge-corner {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #b71c1c;
  color: #fff;
  font-size: 0.7rem;
  padding: 2px 5px;
  border-radius: 4px;
  font-weight: bold;
}

.card-body {
  flex: 1;
  text-align: center;
}
.item-name {
  font-size: 1rem;
  font-weight: bold;
  color: var(--text-light);
  margin-bottom: 5px;
}
.item-type {
  font-size: 0.8rem;
  color: #9e9e9e;
  font-style: italic;
  margin-bottom: 8px;
}
.price-row {
  font-size: 0.95rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 5px;
}
.gold-text {
  color: var(--gold);
  font-weight: bold;
}
.red-text {
  color: #ef5350;
  font-weight: bold;
}

.card-footer {
  display: flex;
  gap: 5px;
  margin-top: auto;
}
.dark-input {
  width: 50px;
  background: #121212;
  border: 1px solid #444;
  color: var(--gold);
  text-align: center;
  font-weight: bold;
  padding: 5px;
}
.btn-action {
  flex: 1;
  border: none;
  font-weight: bold;
  cursor: pointer;
  color: #fff;
  text-transform: uppercase;
  font-size: 0.85rem;
  border-radius: 4px;
  box-shadow: 0 3px 0 rgba(0, 0, 0, 0.3);
  transition: 0.2s;
}
.btn-action:hover {
  filter: brightness(1.2);
  transform: translateY(-2px);
}
.btn-action:active {
  transform: translateY(2px);
  box-shadow: none;
}
.btn-buy-sys {
  background: var(--red-seal);
  border: 1px solid #b71c1c;
}
.btn-sell {
  background: #c62828;
  border: 1px solid #b71c1c;
}
.btn-cancel {
  background: #4e342e;
  padding: 8px;
  width: 100%;
}
.btn-buy-p2p {
  background: linear-gradient(to bottom, #4caf50, #2e7d32);
  border: 1px solid #2e7d32;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
.p2p-card {
  border-color: #81c784;
}
.seller-badge {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(46, 125, 50, 0.2);
  font-size: 0.7rem;
  color: #a5d6a7;
  text-align: center;
  padding: 2px;
}
.p2p-img-top {
  margin-top: 15px;
  border-bottom: none;
  padding-bottom: 0;
}
.p2p-body {
  padding-top: 5px;
}
.stock-info {
  font-size: 0.8rem;
  color: #aaa;
  margin-bottom: 5px;
}
.highlight {
  color: #fff;
}

/* Modal & Transition */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}
.bill-modal {
  width: 400px;
  background: #fdf5e6;
  color: #3e2723;
  border: 4px double #3e2723;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.9);
  animation: popIn 0.3s;
}
.bill-header {
  background: #3e2723;
  color: var(--gold);
  padding: 15px;
  text-align: center;
  border-bottom: 4px solid var(--red-seal);
  position: relative;
}
.bill-header h3 {
  margin: 0;
  font-size: 1.4rem;
}
.bill-seal {
  position: absolute;
  top: 5px;
  left: 10px;
  border: 2px solid var(--red-seal);
  color: var(--red-seal);
  padding: 2px 5px;
  font-weight: bold;
  transform: rotate(-15deg);
  opacity: 0.8;
  font-size: 0.8rem;
}
.bill-body {
  padding: 20px;
}
.item-preview-row {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 15px;
  background: rgba(0, 0, 0, 0.05);
  padding: 10px;
  border-radius: 4px;
}
.preview-img {
  width: 60px;
  height: 60px;
  border: 1px solid #3e2723;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-img img {
  max-width: 90%;
}
.preview-info .p-name {
  font-weight: bold;
  color: #bf360c;
}
.preview-info .p-type {
  font-size: 0.9rem;
  font-style: italic;
}
.bill-calc {
  font-family: "Courier New", monospace;
  font-weight: bold;
}
.calc-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.divider-dashed {
  border-bottom: 2px dashed #3e2723;
  margin: 10px 0;
}
.total {
  font-size: 1.2rem;
  color: #d84315;
}
.bill-footer {
  padding: 15px;
  display: flex;
  gap: 10px;
  background: rgba(0, 0, 0, 0.05);
}
.btn-wood {
  flex: 1;
  padding: 10px;
  font-weight: bold;
  cursor: pointer;
  border: 2px solid #3e2723;
  transition: 0.2s;
}
.btn-wood.cancel:hover {
  background: #d7ccc8;
}
.btn-wood.confirm {
  background: #bf360c;
  color: #fff;
  border-color: #bf360c;
}
.btn-wood.confirm:hover {
  background: #d84315;
}

@keyframes popIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>