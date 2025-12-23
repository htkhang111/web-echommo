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
            <input v-model="searchQuery" type="text" placeholder="Tìm kiếm bảo vật..." />
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
                <div class="img-frame" :class="'border-' + (item.rarity || 'C')">
                  <img :src="resolveItemImage(item.imageUrl)" @error="handleImgError" />
                  <span class="rarity-tag" :class="'bg-' + (item.rarity || 'C')">{{ item.rarity || 'C' }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">{{ item.name }}</div>
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
                <div class="img-frame" :class="'border-' + listing.item.rarity">
                  <img :src="resolveItemImage(listing.item.imageUrl)" @error="handleImgError" />
                  <div v-if="listing.enhanceLevel > 0" class="level-tag" :class="getLevelClass(listing.enhanceLevel)">
                    +{{ listing.enhanceLevel }}
                  </div>
                  <span class="rarity-tag" :class="'bg-' + listing.item.rarity">{{ listing.item.rarity }}</span>
                </div>
              </div>
              <div class="card-body p2p-body">
                <div class="item-name gold-glow">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0" class="enhance-txt">(+{{ listing.enhanceLevel }})</span>
                </div>
                <div class="stock-info">Tồn kho: <span class="highlight">{{ listing.quantity }}</span></div>
                <div class="price-row">
                  <span class="val gold-text large">{{ formatNumber(listing.price) }} <i class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer">
                <input type="number" v-model.number="p2pQty[listing.listingId]" min="1" :max="listing.quantity" class="dark-input" placeholder="1" />
                <button @click="askBuyP2P(listing)" class="btn-action btn-buy-p2p"><i class="fas fa-shopping-basket"></i> MUA</button>
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
                <div class="img-frame" :class="'border-' + listing.item.rarity">
                  <img :src="resolveItemImage(listing.item.imageUrl)" @error="handleImgError" />
                  <span class="qty-badge-corner">x{{ listing.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0">(+{{ listing.enhanceLevel }})</span>
                </div>
                <div class="price-row">
                  <span class="val gold-text">{{ formatNumber(listing.price) }} <i class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer full-width">
                <button @click="handleCancelListing(listing.listingId)" class="btn-action btn-cancel">THU HỒI</button>
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
                <div class="img-frame small">
                  <img :src="resolveItemImage(uItem.item.imageUrl)" @error="handleImgError" />
                  <span class="qty-badge">x{{ uItem.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name">{{ uItem.item.name }}</div>
                <div class="price-row">
                  <span class="label">Thu mua:</span>
                  <span class="val red-text">{{ formatNumber(uItem.item.basePrice * 0.8) }} <i class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer">
                <input type="number" v-model.number="sellQty[uItem.userItemId]" min="1" :max="uItem.quantity" class="dark-input" placeholder="All" />
                <button v-if="!uItem.isEquipped" @click="askSellSystem(uItem)" class="btn-action btn-sell">BÁN</button>
                <div v-else class="equipped-warn">ĐANG DÙNG</div>
              </div>
            </div>
          </div>
        </transition>
      </div>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useMarketStore } from "../stores/marketStore";
import { useInventoryStore } from "../stores/inventoryStore";
import { useNotificationStore } from "../stores/notificationStore";
import { useAuthStore } from "../stores/authStore";
import { resolveItemImage } from "../utils/assetHelper";

const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const notificationStore = useNotificationStore();
const authStore = useAuthStore();

const activeTab = ref("buy");
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});
const searchQuery = ref("");
const filterRarity = ref("ALL");

const confirmModal = reactive({
  visible: false,
  type: "",
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

// Hàm lọc an toàn, tránh crash nếu item null
const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];
  return list.filter((entry) => {
    const itemData = isInventory ? entry.item : entry.item || entry;
    if (!itemData) return false;
    
    // Nếu đang mua (buy/p2p), không hiện Nguyên liệu (MATERIAL) - Tuỳ logic game của ông
    if (!isInventory && itemData.type === 'MATERIAL' && activeTab.value === 'buy') return false;

    const itemName = itemData.name || "";
    const nameMatch = itemName.toLowerCase().includes(searchQuery.value.toLowerCase());
    const itemRarity = itemData.rarity || "C";
    const rarityMatch = filterRarity.value === "ALL" || itemRarity === filterRarity.value;
    return nameMatch && rarityMatch;
  });
};

const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() => filterItems(marketStore.playerListings));
const filteredInventory = computed(() => filterItems(inventoryStore.items, true));

const formatNumber = (n) => Number(n).toLocaleString("en-US");
const getLevelClass = (lv) => (lv >= 15 ? "lvl-red" : lv >= 10 ? "lvl-purple" : lv >= 5 ? "lvl-gold" : "lvl-white");
const handleImgError = (e) => { e.target.src = "https://via.placeholder.com/64?text=IMG"; };

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

const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  openConfirm("SYS", { id: item.itemId, name: item.name, imgCode: item.imageUrl, price: item.basePrice, qty, total: item.basePrice * qty });
};

const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  openConfirm("P2P", { id: listing.listingId, name: listing.item.name, imgCode: listing.item.imageUrl, price: listing.price, qty, total: listing.price * qty });
};

const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = Math.floor(uItem.item.basePrice * 0.8);
  openConfirm("SELL", { id: uItem.userItemId, name: uItem.item.name, imgCode: uItem.item.imageUrl, price: sellPrice, qty, total: sellPrice * qty });
};

const openConfirm = (type, data) => { confirmModal.type = type; confirmModal.data = data; confirmModal.visible = true; };
const closeConfirm = () => { confirmModal.visible = false; };

const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();
  
  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1;
      notificationStore.showToast(`Mua thành công ${data.qty} ${data.name}!`, "success");
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      notificationStore.showToast("Giao dịch thành công!", "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      // Xóa khỏi danh sách hiển thị tạm thời
      const idx = inventoryStore.items.findIndex(i => i.userItemId === data.id);
      if(idx !== -1 && inventoryStore.items[idx].quantity <= data.qty) {
         inventoryStore.items.splice(idx, 1);
      }
      notificationStore.showToast(`Đã bán, nhận ${formatNumber(data.total)} xu!`, "success");
    }
    // Update tiền
    authStore.fetchProfile();
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings(); // Refresh nếu ai đó mua rồi
    const msg = e.response?.data?.message || "Giao dịch thất bại, vui lòng thử lại.";
    notificationStore.showToast(msg, "error");
  }
};

const handleCancelListing = async (listingId) => {
  if(!confirm("Muốn thu hồi vật phẩm này về túi?")) return;
  try {
    await marketStore.cancelListing(listingId);
    await marketStore.fetchMyListings();
    notificationStore.showToast("Đã thu hồi vật phẩm.", "success");
  } catch (e) { notificationStore.showToast("Lỗi thu hồi.", "error"); }
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

onMounted(() => {
  Promise.all([
    marketStore.fetchShopItems(),
    marketStore.fetchPlayerListings(),
  ]);
});
</script>

<style scoped>
/* Copy y nguyên Style cũ của ông vào đây nhé, tôi giữ nguyên cấu trúc class */
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
/* ... (GIỮ NGUYÊN CSS CŨ ĐỂ KHÔNG BỊ VỠ GIAO DIỆN) ... */
.ink-bg-layer, .mountain-bg, .fog-anim { position: absolute; inset: 0; }
.ink-bg-layer { z-index: 0; background-color: #3e2723; }
.mountain-bg { background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop"); background-size: cover; filter: sepia(40%) brightness(0.5) contrast(1.2); opacity: 0.6; }
.fog-anim { background: linear-gradient(to top, #261815 10%, transparent 90%); }
.market-overlay { position: relative; z-index: 10; max-width: 1200px; margin: 0 auto; padding: 30px 20px; height: 100vh; display: flex; flex-direction: column; }
.market-header { text-align: center; margin-bottom: 20px; background: var(--panel-bg); border-top: 4px solid var(--wood-light); border-bottom: 4px solid var(--wood-light); padding: 20px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8); position: relative; }
.market-title { font-size: 2.5rem; color: var(--gold); margin: 0 0 15px 0; text-transform: uppercase; letter-spacing: 5px; font-weight: 900; text-shadow: 0 0 10px rgba(255, 236, 179, 0.3); }
.header-decor { width: 60px; height: 2px; background: var(--gold); position: absolute; top: 50px; }
.left { left: 20%; } .right { right: 20%; }
.filter-bar { display: flex; justify-content: center; gap: 15px; margin-bottom: 20px; flex-wrap: wrap; }
.search-box, .filter-box { background: rgba(0, 0, 0, 0.3); border: 1px solid #5d4037; padding: 5px 15px; border-radius: 20px; display: flex; align-items: center; gap: 10px; transition: 0.3s; }
.search-box:focus-within, .filter-box:hover { border-color: var(--gold); background: rgba(0, 0, 0, 0.5); box-shadow: 0 0 10px rgba(255, 236, 179, 0.1); }
.search-box i, .filter-box i { color: var(--text-dim); }
.search-box input { background: transparent; border: none; color: #fff; font-family: inherit; width: 200px; outline: none; }
.filter-box select { background: transparent; border: none; color: #fff; font-family: inherit; outline: none; cursor: pointer; }
.filter-box select option { background: #3e2723; color: #fff; }
.market-tabs { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.tab-btn { background: transparent; border: none; color: var(--text-dim); font-weight: bold; font-size: 1rem; cursor: pointer; padding: 10px 15px; transition: 0.3s; border-bottom: 2px solid transparent; }
.tab-btn:hover, .tab-btn.active { color: var(--gold); }
.tab-btn.active { border-bottom-color: var(--gold); text-shadow: 0 0 8px rgba(255, 236, 179, 0.4); }
.tab-divider { width: 1px; height: 20px; background: #555; }
.market-content { flex: 1; overflow-y: auto; padding: 10px; }
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.1); }
.grid-layout { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.empty-state { grid-column: 1 / -1; text-align: center; padding: 50px; color: #757575; font-size: 1.2rem; border: 2px dashed #444; background: rgba(0, 0, 0, 0.2); }
.empty-state i { font-size: 3rem; opacity: 0.5; margin-bottom: 10px; display: block; }
.item-card { background: var(--card-bg); border: 1px solid var(--wood-light); border-radius: 4px; padding: 15px; display: flex; flex-direction: column; gap: 10px; transition: 0.3s; position: relative; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); }
.item-card:hover { transform: translateY(-5px); border-color: var(--gold); box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7); }
.card-top { display: flex; justify-content: center; padding-bottom: 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
.img-frame { width: 70px; height: 70px; background: rgba(0, 0, 0, 0.3); border: 2px solid #5d4037; display: flex; align-items: center; justify-content: center; position: relative; }
.img-frame img { max-width: 90%; max-height: 90%; }
.level-tag { position: absolute; top: 2px; right: 2px; font-size: 10px; font-weight: 900; z-index: 5; font-family: sans-serif; padding: 0 3px; border-radius: 3px; background: rgba(0, 0, 0, 0.6); border: 1px solid rgba(255, 255, 255, 0.1); }
.level-tag.top-left { right: auto; left: 2px; }
.lvl-white { color: #fff; } .lvl-gold { color: #ffd700; border-color: #ffd700; box-shadow: 0 0 5px #ffd700; } .lvl-purple { color: #d580ff; border-color: #d580ff; box-shadow: 0 0 5px #d580ff; } .lvl-red { color: #ff3333; border-color: #ff3333; box-shadow: 0 0 8px #ff0000; }
.border-C { border-color: #bdbdbd; } .bg-C { background: #bdbdbd; } .border-B { border-color: #42a5f5; } .bg-B { background: #42a5f5; color: #fff; } .border-A { border-color: #ab47bc; } .bg-A { background: #ab47bc; color: #fff; } .border-S { border-color: var(--gold); box-shadow: 0 0 10px rgba(255, 215, 0, 0.3); } .bg-S { background: var(--gold); }
.rarity-tag { position: absolute; bottom: -8px; font-size: 0.65rem; font-weight: bold; padding: 2px 6px; border-radius: 3px; color: #000; }
.qty-badge-corner { position: absolute; top: -5px; right: -5px; background: #b71c1c; color: #fff; font-size: 0.7rem; padding: 2px 5px; border-radius: 4px; font-weight: bold; }
.card-body { flex: 1; text-align: center; }
.item-name { font-size: 1rem; font-weight: bold; color: var(--text-light); margin-bottom: 5px; }
.item-type { font-size: 0.8rem; color: #9e9e9e; font-style: italic; margin-bottom: 8px; }
.price-row { font-size: 0.95rem; display: flex; justify-content: center; align-items: center; gap: 5px; }
.gold-text { color: var(--gold); font-weight: bold; }
.red-text { color: #ef5350; font-weight: bold; }
.card-footer { display: flex; gap: 5px; margin-top: auto; }
.dark-input { width: 50px; background: #121212; border: 1px solid #444; color: var(--gold); text-align: center; font-weight: bold; padding: 5px; }
.btn-action { flex: 1; border: none; font-weight: bold; cursor: pointer; color: #fff; text-transform: uppercase; font-size: 0.85rem; border-radius: 4px; box-shadow: 0 3px 0 rgba(0, 0, 0, 0.3); transition: 0.2s; }
.btn-action:hover { filter: brightness(1.2); transform: translateY(-2px); } .btn-action:active { transform: translateY(2px); box-shadow: none; }
.btn-buy-sys { background: var(--red-seal); border: 1px solid #b71c1c; } .btn-sell { background: #c62828; border: 1px solid #b71c1c; } .btn-cancel { background: #4e342e; padding: 8px; width: 100%; } .btn-buy-p2p { background: linear-gradient(to bottom, #4caf50, #2e7d32); border: 1px solid #2e7d32; display: flex; align-items: center; justify-content: center; gap: 5px; }
.p2p-card { border-color: #81c784; } .seller-badge { position: absolute; top: 0; left: 0; right: 0; background: rgba(46, 125, 50, 0.2); font-size: 0.7rem; color: #a5d6a7; text-align: center; padding: 2px; } .p2p-img-top { margin-top: 15px; border-bottom: none; padding-bottom: 0; } .p2p-body { padding-top: 5px; } .stock-info { font-size: 0.8rem; color: #aaa; margin-bottom: 5px; } .highlight { color: #fff; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 1000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.bill-modal { width: 400px; background: #fdf5e6; color: #3e2723; border: 4px double #3e2723; box-shadow: 0 0 50px rgba(0, 0, 0, 0.9); animation: popIn 0.3s; }
.bill-header { background: #3e2723; color: var(--gold); padding: 15px; text-align: center; border-bottom: 4px solid var(--red-seal); position: relative; } .bill-header h3 { margin: 0; font-size: 1.4rem; } .bill-seal { position: absolute; top: 5px; left: 10px; border: 2px solid var(--red-seal); color: var(--red-seal); padding: 2px 5px; font-weight: bold; transform: rotate(-15deg); opacity: 0.8; font-size: 0.8rem; }
.bill-body { padding: 20px; } .item-preview-row { display: flex; gap: 15px; align-items: center; margin-bottom: 15px; background: rgba(0, 0, 0, 0.05); padding: 10px; border-radius: 4px; } .preview-img { width: 60px; height: 60px; border: 1px solid #3e2723; background: #fff; display: flex; align-items: center; justify-content: center; } .preview-img img { max-width: 90%; } .preview-info .p-name { font-weight: bold; color: #bf360c; } .preview-info .p-type { font-size: 0.9rem; font-style: italic; }
.bill-calc { font-family: "Courier New", monospace; font-weight: bold; } .calc-row { display: flex; justify-content: space-between; margin-bottom: 5px; } .divider-dashed { border-bottom: 2px dashed #3e2723; margin: 10px 0; } .total { font-size: 1.2rem; color: #d84315; }
.bill-footer { padding: 15px; display: flex; gap: 10px; background: rgba(0, 0, 0, 0.05); } .btn-wood { flex: 1; padding: 10px; font-weight: bold; cursor: pointer; border: 2px solid #3e2723; transition: 0.2s; } .btn-wood.cancel:hover { background: #d7ccc8; } .btn-wood.confirm { background: #bf360c; color: #fff; border-color: #bf360c; } .btn-wood.confirm:hover { background: #d84315; }
@keyframes popIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; } .fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateY(10px); }
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
            <input v-model="searchQuery" type="text" placeholder="Tìm kiếm bảo vật..." />
          </div>
          <div class="filter-box">
            <i class="fas fa-filter"></i>
            <select v-model="filterRarity">
              <option value="ALL">Tất cả phẩm chất</option>
              <option value="COMMON">Phàm phẩm (Common)</option>
              <option value="UNCOMMON">Lương phẩm (Uncommon)</option>
              <option value="RARE">Linh phẩm (Rare)</option>
              <option value="EPIC">Tiên phẩm (Epic)</option>
              <option value="LEGENDARY">Thần phẩm (Legendary)</option>
              <option value="MYTHIC">Thánh phẩm (Mythic)</option>
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
                <div class="img-frame" :class="'border-' + resolveRarity(item.rarity)">
                  <img :src="resolveItemImage(item.imageUrl)" @error="handleImgError" />
                  <span class="rarity-tag" :class="'bg-' + resolveRarity(item.rarity)">
                    {{ resolveRarityName(item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + resolveRarity(item.rarity)">{{ item.name }}</div>
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
                <div class="img-frame" :class="'border-' + resolveRarity(listing.item.rarity)">
                  <img :src="resolveItemImage(listing.item.imageUrl)" @error="handleImgError" />
                  <div v-if="listing.enhanceLevel > 0" class="level-tag" :class="getLevelClass(listing.enhanceLevel)">
                    +{{ listing.enhanceLevel }}
                  </div>
                  <span class="rarity-tag" :class="'bg-' + resolveRarity(listing.item.rarity)">
                    {{ resolveRarityName(listing.item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body p2p-body">
                <div class="item-name" :class="'text-' + resolveRarity(listing.item.rarity)">
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
                <div class="img-frame" :class="'border-' + resolveRarity(listing.item.rarity)">
                  <img :src="resolveItemImage(listing.item.imageUrl)" @error="handleImgError" />
                  <span class="qty-badge-corner">x{{ listing.quantity }}</span>
                  <span class="rarity-tag" :class="'bg-' + resolveRarity(listing.item.rarity)">
                    {{ resolveRarityName(listing.item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + resolveRarity(listing.item.rarity)">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0">(+{{ listing.enhanceLevel }})</span>
                </div>
                <div class="price-row">
                  <span class="val gold-text">{{ formatNumber(listing.price) }} <i class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer full-width">
                <button @click="handleCancelListing(listing.listingId)" class="btn-action btn-cancel">THU HỒI</button>
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
                <div class="img-frame small" :class="'border-' + resolveRarity(uItem.item.rarity)">
                  <img :src="resolveItemImage(uItem.item.imageUrl)" @error="handleImgError" />
                  <span class="qty-badge">x{{ uItem.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + resolveRarity(uItem.item.rarity)">{{ uItem.item.name }}</div>
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
            <button class="btn-wood confirm" @click="confirmTransaction"><i class="fas fa-fingerprint"></i> ẤN
              ĐỊNH</button>
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
import { useAuthStore } from "../stores/authStore";
import { resolveItemImage } from "../utils/assetHelper";

const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const notificationStore = useNotificationStore();
const authStore = useAuthStore();

const activeTab = ref("buy");
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});
const searchQuery = ref("");
const filterRarity = ref("ALL");

const confirmModal = reactive({
  visible: false,
  type: "",
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

// [FIX] Hàm chuyển đổi từ Enum Backend -> Mã hiển thị Frontend
const resolveRarity = (backendRarity) => {
  if (!backendRarity) return 'C';
  const map = {
    'COMMON': 'C',
    'UNCOMMON': 'D',    // Xanh lá (New)
    'RARE': 'B',        // Xanh dương
    'EPIC': 'A',        // Tím
    'LEGENDARY': 'S',   // Vàng
    'MYTHIC': 'SS'      // Đỏ (New)
  };
  return map[backendRarity] || 'C';
};

// [FIX] Hàm hiển thị tên rút gọn
const resolveRarityName = (backendRarity) => {
  const map = {
    'COMMON': 'C',
    'UNCOMMON': 'D',
    'RARE': 'B',
    'EPIC': 'A',
    'LEGENDARY': 'S',
    'MYTHIC': 'SS'
  };
  return map[backendRarity] || 'C';
};

const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];
  return list.filter((entry) => {
    const itemData = isInventory ? entry.item : entry.item || entry;
    if (!itemData) return false;

    if (!isInventory && itemData.type === 'MATERIAL' && activeTab.value === 'buy') return false;

    const itemName = itemData.name || "";
    const nameMatch = itemName.toLowerCase().includes(searchQuery.value.toLowerCase());
    
    // So sánh trực tiếp với Enum backend vì filterRarity lưu giá trị Enum
    const itemRarity = itemData.rarity || "COMMON"; 
    const rarityMatch = filterRarity.value === "ALL" || itemRarity === filterRarity.value;
    
    return nameMatch && rarityMatch;
  });
};

const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() => filterItems(marketStore.playerListings));
const filteredInventory = computed(() => filterItems(inventoryStore.items, true));

const formatNumber = (n) => Number(n).toLocaleString("en-US");
const getLevelClass = (lv) => (lv >= 15 ? "lvl-red" : lv >= 10 ? "lvl-purple" : lv >= 5 ? "lvl-gold" : "lvl-white");
const handleImgError = (e) => { e.target.src = "https://via.placeholder.com/64?text=IMG"; };

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

const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  openConfirm("SYS", { id: item.itemId, name: item.name, imgCode: item.imageUrl, price: item.basePrice, qty, total: item.basePrice * qty });
};

const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  openConfirm("P2P", { id: listing.listingId, name: listing.item.name, imgCode: listing.item.imageUrl, price: listing.price, qty, total: listing.price * qty });
};

const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = Math.floor(uItem.item.basePrice * 0.8);
  openConfirm("SELL", { id: uItem.userItemId, name: uItem.item.name, imgCode: uItem.item.imageUrl, price: sellPrice, qty, total: sellPrice * qty });
};

const openConfirm = (type, data) => { confirmModal.type = type; confirmModal.data = data; confirmModal.visible = true; };
const closeConfirm = () => { confirmModal.visible = false; };

const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();

  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1;
      notificationStore.showToast(`Mua thành công ${data.qty} ${data.name}!`, "success");
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      notificationStore.showToast("Giao dịch thành công!", "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      const idx = inventoryStore.items.findIndex(i => i.userItemId === data.id);
      if (idx !== -1 && inventoryStore.items[idx].quantity <= data.qty) {
        inventoryStore.items.splice(idx, 1);
      }
      notificationStore.showToast(`Đã bán, nhận ${formatNumber(data.total)} xu!`, "success");
    }
    authStore.fetchProfile();
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings();
    const msg = e.response?.data?.message || "Giao dịch thất bại, vui lòng thử lại.";
    notificationStore.showToast(msg, "error");
  }
};

const handleCancelListing = async (listingId) => {
  if (!confirm("Muốn thu hồi vật phẩm này về túi?")) return;
  try {
    await marketStore.cancelListing(listingId);
    await marketStore.fetchMyListings();
    notificationStore.showToast("Đã thu hồi vật phẩm.", "success");
  } catch (e) { notificationStore.showToast("Lỗi thu hồi.", "error"); }
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

onMounted(() => {
  Promise.all([
    marketStore.fetchShopItems(),
    marketStore.fetchPlayerListings(),
  ]);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* =========================================
   NÂNG CẤP HIỂN THỊ PHẨM CHẤT (RARITY) - FIX
   ========================================= */

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;
  --panel-bg: rgba(30, 20, 15, 0.95);
  --card-bg: #261815;

  /* Màu phẩm chất */
  --r-c-color: #a0a0a0; /* C - Common (Xám) */
  --r-d-color: #4caf50; /* D - Uncommon (Xanh lá) */
  --r-b-color: #00b0ff; /* B - Rare (Lam) */
  --r-a-color: #d500f9; /* A - Epic (Tím) */
  --r-s-color: #ffd700; /* S - Legendary (Vàng) */
  --r-ss-color: #ff1744; /* SS - Mythic (Đỏ) */
}

/* Base cho khung item */
.img-frame {
  width: 70px;
  height: 70px;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s ease;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.8);
}

.img-frame.small {
  width: 50px;
  height: 50px;
}

/* --- PHẨM CHẤT C (Common) --- */
.border-C { border: 2px solid var(--r-c-color); box-shadow: 0 0 2px var(--r-c-color); }
.bg-C { background: linear-gradient(135deg, #424242, #616161); color: #fff; border: 1px solid var(--r-c-color); }
.text-C { color: var(--r-c-color); }

/* --- PHẨM CHẤT D (Uncommon - NEW) --- */
.border-D { border: 2px solid var(--r-d-color); box-shadow: 0 0 5px rgba(76, 175, 80, 0.5); }
.bg-D { background: linear-gradient(135deg, #2e7d32, #43a047); color: #fff; border: 1px solid #66bb6a; }
.text-D { color: var(--r-d-color); text-shadow: 0 0 5px rgba(76, 175, 80, 0.4); }

/* --- PHẨM CHẤT B (Rare - Lam) --- */
.border-B { border: 2px solid var(--r-b-color); box-shadow: 0 0 8px rgba(0, 176, 255, 0.4), inset 0 0 5px rgba(0, 176, 255, 0.2); }
.bg-B { background: linear-gradient(135deg, #01579b, #0288d1); color: #fff; box-shadow: 0 0 5px var(--r-b-color); border: 1px solid #81d4fa; }
.text-B { color: var(--r-b-color); text-shadow: 0 0 5px rgba(0, 176, 255, 0.5); }

/* --- PHẨM CHẤT A (Epic - Tím) --- */
.border-A { border: 2px solid var(--r-a-color); box-shadow: 0 0 10px rgba(213, 0, 249, 0.5), inset 0 0 8px rgba(213, 0, 249, 0.3); }
.bg-A { background: linear-gradient(135deg, #4a148c, #7b1fa2); color: #fff; box-shadow: 0 0 8px var(--r-a-color); border: 1px solid #ea80fc; }
.text-A { color: #ea80fc; text-shadow: 0 0 8px rgba(213, 0, 249, 0.6); }

/* --- PHẨM CHẤT S (Legendary - Vàng Rồng) --- */
.border-S { border: 2px solid var(--r-s-color); box-shadow: 0 0 10px var(--r-s-color), 0 0 20px rgba(255, 215, 0, 0.4), inset 0 0 10px rgba(255, 215, 0, 0.5); animation: pulse-gold 2s infinite alternate; }
.bg-S { background: linear-gradient(135deg, #ff6f00, #ffca28); color: #3e2723; font-weight: 900; border: 1px solid #fff; box-shadow: 0 0 10px var(--r-s-color); }
.text-S { color: var(--r-s-color); text-shadow: 0 0 10px rgba(255, 215, 0, 0.8), 0 0 2px #fff; font-weight: bold; }

/* --- PHẨM CHẤT SS (Mythic - Đỏ Huyết - NEW) --- */
.border-SS { border: 2px solid var(--r-ss-color); box-shadow: 0 0 15px var(--r-ss-color), inset 0 0 10px rgba(255, 0, 0, 0.6); animation: pulse-red 1.5s infinite alternate; }
.bg-SS { background: linear-gradient(135deg, #b71c1c, #d32f2f); color: #fff; font-weight: 900; border: 1px solid #ff8a80; box-shadow: 0 0 10px var(--r-ss-color); }
.text-SS { color: var(--r-ss-color); text-shadow: 0 0 10px rgba(255, 23, 68, 0.8), 0 0 2px #fff; font-weight: 900; text-transform: uppercase; }

@keyframes pulse-gold { 0% { box-shadow: 0 0 10px var(--r-s-color); } 100% { box-shadow: 0 0 20px var(--r-s-color), 0 0 30px rgba(255, 69, 0, 0.6); } }
@keyframes pulse-red { 0% { box-shadow: 0 0 10px var(--r-ss-color); } 100% { box-shadow: 0 0 25px var(--r-ss-color), 0 0 40px rgba(255, 0, 0, 0.8); } }

.rarity-tag { position: absolute; bottom: -10px; font-size: 0.6rem; font-weight: 800; padding: 2px 8px; border-radius: 10px; z-index: 2; text-transform: uppercase; letter-spacing: 1px; }

/* --- GIỮ NGUYÊN CSS LAYOUT CŨ --- */
.dark-theme { background-color: #212121; min-height: 100vh; font-family: "Noto Serif TC", serif; color: var(--text-light); position: relative; overflow: hidden; }
.ink-bg-layer, .mountain-bg, .fog-anim { position: absolute; inset: 0; }
.ink-bg-layer { z-index: 0; background-color: #3e2723; }
.mountain-bg { background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop"); background-size: cover; filter: sepia(40%) brightness(0.5) contrast(1.2); opacity: 0.6; }
.fog-anim { background: linear-gradient(to top, #261815 10%, transparent 90%); }
.market-overlay { position: relative; z-index: 10; max-width: 1200px; margin: 0 auto; padding: 30px 20px; height: 100vh; display: flex; flex-direction: column; }
.market-header { text-align: center; margin-bottom: 20px; background: var(--panel-bg); border-top: 4px solid var(--wood-light); border-bottom: 4px solid var(--wood-light); padding: 20px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8); position: relative; }
.market-title { font-size: 2.5rem; color: var(--gold); margin: 0 0 15px 0; text-transform: uppercase; letter-spacing: 5px; font-weight: 900; text-shadow: 0 0 10px rgba(255, 236, 179, 0.3); }
.header-decor { width: 60px; height: 2px; background: var(--gold); position: absolute; top: 50px; }
.left { left: 20%; } .right { right: 20%; }
.filter-bar { display: flex; justify-content: center; gap: 15px; margin-bottom: 20px; flex-wrap: wrap; }
.search-box, .filter-box { background: rgba(0, 0, 0, 0.3); border: 1px solid #5d4037; padding: 5px 15px; border-radius: 20px; display: flex; align-items: center; gap: 10px; transition: 0.3s; }
.search-box:focus-within, .filter-box:hover { border-color: var(--gold); background: rgba(0, 0, 0, 0.5); box-shadow: 0 0 10px rgba(255, 236, 179, 0.1); }
.search-box i, .filter-box i { color: var(--text-dim); }
.search-box input { background: transparent; border: none; color: #fff; font-family: inherit; width: 200px; outline: none; }
.filter-box select { background: transparent; border: none; color: #fff; font-family: inherit; outline: none; cursor: pointer; }
.filter-box select option { background: #3e2723; color: #fff; }
.market-tabs { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.tab-btn { background: transparent; border: none; color: var(--text-dim); font-weight: bold; font-size: 1rem; cursor: pointer; padding: 10px 15px; transition: 0.3s; border-bottom: 2px solid transparent; }
.tab-btn:hover, .tab-btn.active { color: var(--gold); }
.tab-btn.active { border-bottom-color: var(--gold); text-shadow: 0 0 8px rgba(255, 236, 179, 0.4); }
.tab-divider { width: 1px; height: 20px; background: #555; }
.market-content { flex: 1; overflow-y: auto; padding: 10px; }
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.1); }
.grid-layout { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.empty-state { grid-column: 1 / -1; text-align: center; padding: 50px; color: #757575; font-size: 1.2rem; border: 2px dashed #444; background: rgba(0, 0, 0, 0.2); }
.empty-state i { font-size: 3rem; opacity: 0.5; margin-bottom: 10px; display: block; }
.item-card { background: var(--card-bg); border: 1px solid var(--wood-light); border-radius: 4px; padding: 15px; display: flex; flex-direction: column; gap: 10px; transition: 0.3s; position: relative; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); }
.item-card:hover { transform: translateY(-5px); border-color: var(--gold); box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7); }
.card-top { display: flex; justify-content: center; padding-bottom: 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
.level-tag { position: absolute; top: 2px; right: 2px; font-size: 10px; font-weight: 900; z-index: 5; font-family: sans-serif; padding: 0 3px; border-radius: 3px; background: rgba(0, 0, 0, 0.6); border: 1px solid rgba(255, 255, 255, 0.1); }
.lvl-white { color: #fff; } .lvl-gold { color: #ffd700; border-color: #ffd700; box-shadow: 0 0 5px #ffd700; } .lvl-purple { color: #d580ff; border-color: #d580ff; box-shadow: 0 0 5px #d580ff; } .lvl-red { color: #ff3333; border-color: #ff3333; box-shadow: 0 0 8px #ff0000; }
.qty-badge-corner { position: absolute; top: -5px; right: -5px; background: #b71c1c; color: #fff; font-size: 0.7rem; padding: 2px 5px; border-radius: 4px; font-weight: bold; z-index: 5; }
.card-body { flex: 1; text-align: center; }
.item-name { font-size: 1rem; font-weight: bold; margin-bottom: 5px; }
.item-type { font-size: 0.8rem; color: #9e9e9e; font-style: italic; margin-bottom: 8px; }
.price-row { font-size: 0.95rem; display: flex; justify-content: center; align-items: center; gap: 5px; }
.gold-text { color: var(--gold); font-weight: bold; }
.red-text { color: #ef5350; font-weight: bold; }
.card-footer { display: flex; gap: 5px; margin-top: auto; }
.dark-input { width: 50px; background: #121212; border: 1px solid #444; color: var(--gold); text-align: center; font-weight: bold; padding: 5px; }
.btn-action { flex: 1; border: none; font-weight: bold; cursor: pointer; color: #fff; text-transform: uppercase; font-size: 0.85rem; border-radius: 4px; box-shadow: 0 3px 0 rgba(0, 0, 0, 0.3); transition: 0.2s; }
.btn-action:hover { filter: brightness(1.2); transform: translateY(-2px); } .btn-action:active { transform: translateY(2px); box-shadow: none; }
.btn-buy-sys { background: var(--red-seal); border: 1px solid #b71c1c; } .btn-sell { background: #c62828; border: 1px solid #b71c1c; } .btn-cancel { background: #4e342e; padding: 8px; width: 100%; } .btn-buy-p2p { background: linear-gradient(to bottom, #4caf50, #2e7d32); border: 1px solid #2e7d32; display: flex; align-items: center; justify-content: center; gap: 5px; }
.p2p-card { border-color: #81c784; } .seller-badge { position: absolute; top: 0; left: 0; right: 0; background: rgba(46, 125, 50, 0.2); font-size: 0.7rem; color: #a5d6a7; text-align: center; padding: 2px; } .p2p-img-top { margin-top: 15px; border-bottom: none; padding-bottom: 0; } .p2p-body { padding-top: 5px; } .stock-info { font-size: 0.8rem; color: #aaa; margin-bottom: 5px; } .highlight { color: #fff; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 1000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.bill-modal { width: 400px; background: #fdf5e6; color: #3e2723; border: 4px double #3e2723; box-shadow: 0 0 50px rgba(0, 0, 0, 0.9); animation: popIn 0.3s; }
.bill-header { background: #3e2723; color: var(--gold); padding: 15px; text-align: center; border-bottom: 4px solid var(--red-seal); position: relative; } .bill-header h3 { margin: 0; font-size: 1.4rem; } .bill-seal { position: absolute; top: 5px; left: 10px; border: 2px solid var(--red-seal); color: var(--red-seal); padding: 2px 5px; font-weight: bold; transform: rotate(-15deg); opacity: 0.8; font-size: 0.8rem; }
.bill-body { padding: 20px; } .item-preview-row { display: flex; gap: 15px; align-items: center; margin-bottom: 15px; background: rgba(0, 0, 0, 0.05); padding: 10px; border-radius: 4px; } .preview-img { width: 60px; height: 60px; border: 1px solid #3e2723; background: #fff; display: flex; align-items: center; justify-content: center; } .preview-img img { max-width: 90%; } .preview-info .p-name { font-weight: bold; color: #bf360c; } .preview-info .p-type { font-size: 0.9rem; font-style: italic; }
.bill-calc { font-family: "Courier New", monospace; font-weight: bold; } .calc-row { display: flex; justify-content: space-between; margin-bottom: 5px; } .divider-dashed { border-bottom: 2px dashed #3e2723; margin: 10px 0; } .total { font-size: 1.2rem; color: #d84315; }
.bill-footer { padding: 15px; display: flex; gap: 10px; background: rgba(0, 0, 0, 0.05); } .btn-wood { flex: 1; padding: 10px; font-weight: bold; cursor: pointer; border: 2px solid #3e2723; transition: 0.2s; } .btn-wood.cancel:hover { background: #d7ccc8; } .btn-wood.confirm { background: #bf360c; color: #fff; border-color: #bf360c; } .btn-wood.confirm:hover { background: #d84315; }
@keyframes popIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; } .fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateY(10px); }
</style> -->


<!-- <template>
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
            <i class="fas fa-filter"></i>
            <select v-model="filterRarity">
              <option value="ALL">Tất cả phẩm chất</option>
              <option value="COMMON">Phàm phẩm (Common)</option>
              <option value="UNCOMMON">Lương phẩm (Uncommon)</option>
              <option value="RARE">Linh phẩm (Rare)</option>
              <option value="EPIC">Tiên phẩm (Epic)</option>
              <option value="LEGENDARY">Thần phẩm (Legendary)</option>
              <option value="MYTHIC">Thánh phẩm (Mythic)</option>
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
                <div class="img-frame" :class="'border-' + resolveRarity(item.rarity)">
                  <img :src="resolveItemImage(item.imageUrl)" @error="handleImgError" />
                  <span class="rarity-tag" :class="'bg-' + resolveRarity(item.rarity)">
                    {{ resolveRarityName(item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + resolveRarity(item.rarity)">{{ item.name }}</div>
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
                <div class="img-frame" :class="'border-' + resolveRarity(listing.item.rarity)">
                  <img :src="resolveItemImage(listing.item.imageUrl)" @error="handleImgError" />
                  <div v-if="listing.enhanceLevel > 0" class="level-tag" :class="getLevelClass(listing.enhanceLevel)">
                    +{{ listing.enhanceLevel }}
                  </div>
                  <span class="rarity-tag" :class="'bg-' + resolveRarity(listing.item.rarity)">
                    {{ resolveRarityName(listing.item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body p2p-body">
                <div class="item-name" :class="'text-' + resolveRarity(listing.item.rarity)">
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
                <div class="img-frame" :class="'border-' + resolveRarity(listing.item.rarity)">
                  <img :src="resolveItemImage(listing.item.imageUrl)" @error="handleImgError" />
                  <span class="qty-badge-corner">x{{ listing.quantity }}</span>
                  <span class="rarity-tag" :class="'bg-' + resolveRarity(listing.item.rarity)">
                    {{ resolveRarityName(listing.item.rarity) }}
                  </span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + resolveRarity(listing.item.rarity)">
                  {{ listing.item.name }}
                  <span v-if="listing.enhanceLevel > 0">(+{{ listing.enhanceLevel }})</span>
                </div>
                <div class="price-row">
                  <span class="val gold-text">{{ formatNumber(listing.price) }} <i class="fas fa-coins"></i></span>
                </div>
              </div>
              <div class="card-footer full-width">
                <button @click="handleCancelListing(listing.listingId)" class="btn-action btn-cancel">THU HỒI</button>
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
                <div class="img-frame small" :class="'border-' + resolveRarity(uItem.item.rarity)">
                  <img :src="resolveItemImage(uItem.item.imageUrl)" @error="handleImgError" />
                  <span class="qty-badge">x{{ uItem.quantity }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="item-name" :class="'text-' + resolveRarity(uItem.item.rarity)">{{ uItem.item.name }}</div>
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
            <button class="btn-wood confirm" @click="confirmTransaction"><i class="fas fa-fingerprint"></i> ẤN
              ĐỊNH</button>
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
import { useAuthStore } from "../stores/authStore";
import { resolveItemImage } from "../utils/assetHelper";

const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const notificationStore = useNotificationStore();
const authStore = useAuthStore();

// --- BACKGROUND LOGIC (MỚI) ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  // Chỉ đổi màu nền theo giờ, không hiện text
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC GIAO DỊCH CŨ (GIỮ NGUYÊN) ---
const activeTab = ref("buy");
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});
const searchQuery = ref("");
const filterRarity = ref("ALL");

const confirmModal = reactive({
  visible: false,
  type: "",
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

const resolveRarity = (backendRarity) => {
  if (!backendRarity) return 'C';
  const map = {
    'COMMON': 'C',
    'UNCOMMON': 'D',
    'RARE': 'B',
    'EPIC': 'A',
    'LEGENDARY': 'S',
    'MYTHIC': 'SS'
  };
  return map[backendRarity] || 'C';
};

const resolveRarityName = (backendRarity) => {
  const map = {
    'COMMON': 'C',
    'UNCOMMON': 'D',
    'RARE': 'B',
    'EPIC': 'A',
    'LEGENDARY': 'S',
    'MYTHIC': 'SS'
  };
  return map[backendRarity] || 'C';
};

const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];
  return list.filter((entry) => {
    const itemData = isInventory ? entry.item : entry.item || entry;
    if (!itemData) return false;

    if (!isInventory && itemData.type === 'MATERIAL' && activeTab.value === 'buy') return false;

    const itemName = itemData.name || "";
    const nameMatch = itemName.toLowerCase().includes(searchQuery.value.toLowerCase());
    
    const itemRarity = itemData.rarity || "COMMON"; 
    const rarityMatch = filterRarity.value === "ALL" || itemRarity === filterRarity.value;
    
    return nameMatch && rarityMatch;
  });
};

const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() => filterItems(marketStore.playerListings));
const filteredInventory = computed(() => filterItems(inventoryStore.items, true));

const formatNumber = (n) => Number(n).toLocaleString("en-US");
const getLevelClass = (lv) => (lv >= 15 ? "lvl-red" : lv >= 10 ? "lvl-purple" : lv >= 5 ? "lvl-gold" : "lvl-white");
const handleImgError = (e) => { e.target.src = "https://via.placeholder.com/64?text=IMG"; };

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

const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  openConfirm("SYS", { id: item.itemId, name: item.name, imgCode: item.imageUrl, price: item.basePrice, qty, total: item.basePrice * qty });
};

const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  openConfirm("P2P", { id: listing.listingId, name: listing.item.name, imgCode: listing.item.imageUrl, price: listing.price, qty, total: listing.price * qty });
};

const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = Math.floor(uItem.item.basePrice * 0.8);
  openConfirm("SELL", { id: uItem.userItemId, name: uItem.item.name, imgCode: uItem.item.imageUrl, price: sellPrice, qty, total: sellPrice * qty });
};

const openConfirm = (type, data) => { confirmModal.type = type; confirmModal.data = data; confirmModal.visible = true; };
const closeConfirm = () => { confirmModal.visible = false; };

const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();

  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1;
      notificationStore.showToast(`Mua thành công ${data.qty} ${data.name}!`, "success");
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      notificationStore.showToast("Giao dịch thành công!", "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      const idx = inventoryStore.items.findIndex(i => i.userItemId === data.id);
      if (idx !== -1 && inventoryStore.items[idx].quantity <= data.qty) {
        inventoryStore.items.splice(idx, 1);
      }
      notificationStore.showToast(`Đã bán, nhận ${formatNumber(data.total)} xu!`, "success");
    }
    authStore.fetchProfile();
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings();
    const msg = e.response?.data?.message || "Giao dịch thất bại, vui lòng thử lại.";
    notificationStore.showToast(msg, "error");
  }
};

const handleCancelListing = async (listingId) => {
  if (!confirm("Muốn thu hồi vật phẩm này về túi?")) return;
  try {
    await marketStore.cancelListing(listingId);
    await marketStore.fetchMyListings();
    notificationStore.showToast("Đã thu hồi vật phẩm.", "success");
  } catch (e) { notificationStore.showToast("Lỗi thu hồi.", "error"); }
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

onMounted(() => {
  updateDayNight(); // [MỚI] Kích hoạt màu nền
  Promise.all([
    marketStore.fetchShopItems(),
    marketStore.fetchPlayerListings(),
  ]);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* =========================================
   VARIABLES & BASE
   ========================================= */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;
  --panel-bg: rgba(30, 20, 15, 0.95);
  --card-bg: rgba(38, 24, 21, 0.9); /* Trong suốt hơn để ăn nền mới */

  /* Màu phẩm chất */
  --r-c-color: #a0a0a0;
  --r-d-color: #4caf50;
  --r-b-color: #00b0ff;
  --r-a-color: #d500f9;
  --r-s-color: #ffd700;
  --r-ss-color: #ff1744;
}

/* --- 1. HỆ THỐNG BACKGROUND (MỚI) --- */
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

/* --- LAYOUT CHUNG --- */
.dark-theme { 
  /* Nền trong suốt để lộ bg-layer */
  background-color: transparent; 
  min-height: 100vh; 
  font-family: "Noto Serif TC", serif; 
  color: var(--text-light); 
  position: relative; 
  overflow: hidden; 
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

/* --- HEADER & TABS --- */
.market-header { 
  text-align: center; margin-bottom: 20px; 
  background: rgba(30, 20, 15, 0.8); /* Trong suốt hơn */
  border-top: 4px solid var(--wood-light); 
  border-bottom: 4px solid var(--wood-light); 
  padding: 20px; 
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8); 
  position: relative; 
}
.market-title { font-size: 2.5rem; color: var(--gold); margin: 0 0 15px 0; text-transform: uppercase; letter-spacing: 5px; font-weight: 900; text-shadow: 0 0 10px rgba(255, 236, 179, 0.3); }
.header-decor { width: 60px; height: 2px; background: var(--gold); position: absolute; top: 50px; }
.left { left: 20%; } .right { right: 20%; }

.filter-bar { display: flex; justify-content: center; gap: 15px; margin-bottom: 20px; flex-wrap: wrap; }
.search-box, .filter-box { background: rgba(0, 0, 0, 0.5); border: 1px solid #5d4037; padding: 5px 15px; border-radius: 20px; display: flex; align-items: center; gap: 10px; transition: 0.3s; }
.search-box:focus-within, .filter-box:hover { border-color: var(--gold); background: rgba(0, 0, 0, 0.7); box-shadow: 0 0 10px rgba(255, 236, 179, 0.1); }
.search-box i, .filter-box i { color: var(--text-dim); }
.search-box input { background: transparent; border: none; color: #fff; font-family: inherit; width: 200px; outline: none; }
.filter-box select { background: transparent; border: none; color: #fff; font-family: inherit; outline: none; cursor: pointer; }
.filter-box select option { background: #3e2723; color: #fff; }

.market-tabs { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.tab-btn { background: transparent; border: none; color: var(--text-dim); font-weight: bold; font-size: 1rem; cursor: pointer; padding: 10px 15px; transition: 0.3s; border-bottom: 2px solid transparent; }
.tab-btn:hover, .tab-btn.active { color: var(--gold); }
.tab-btn.active { border-bottom-color: var(--gold); text-shadow: 0 0 8px rgba(255, 236, 179, 0.4); }
.tab-divider { width: 1px; height: 20px; background: #555; }

/* --- CONTENT GRID & CARDS --- */
.market-content { flex: 1; overflow-y: auto; padding: 10px; }
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.1); }

.grid-layout { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.empty-state { grid-column: 1 / -1; text-align: center; padding: 50px; color: #757575; font-size: 1.2rem; border: 2px dashed #444; background: rgba(0, 0, 0, 0.2); }
.empty-state i { font-size: 3rem; opacity: 0.5; margin-bottom: 10px; display: block; }

.item-card { 
  background: var(--card-bg); 
  border: 1px solid var(--wood-light); 
  border-radius: 4px; padding: 15px; 
  display: flex; flex-direction: column; gap: 10px; 
  transition: 0.3s; position: relative; 
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); 
}
.item-card:hover { transform: translateY(-5px); border-color: var(--gold); box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7); }

/* Card Elements */
.card-top { display: flex; justify-content: center; padding-bottom: 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
.card-body { flex: 1; text-align: center; }
.card-footer { display: flex; gap: 5px; margin-top: auto; }

/* Image Frame */
.img-frame { width: 70px; height: 70px; background: #1a1a1a; display: flex; align-items: center; justify-content: center; position: relative; transition: all 0.3s ease; box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.8); }
.img-frame.small { width: 50px; height: 50px; }

/* Rarity Styles (Giữ nguyên) */
.border-C { border: 2px solid var(--r-c-color); box-shadow: 0 0 2px var(--r-c-color); }
.bg-C { background: linear-gradient(135deg, #424242, #616161); color: #fff; border: 1px solid var(--r-c-color); }
.text-C { color: var(--r-c-color); }

.border-D { border: 2px solid var(--r-d-color); box-shadow: 0 0 5px rgba(76, 175, 80, 0.5); }
.bg-D { background: linear-gradient(135deg, #2e7d32, #43a047); color: #fff; border: 1px solid #66bb6a; }
.text-D { color: var(--r-d-color); text-shadow: 0 0 5px rgba(76, 175, 80, 0.4); }

.border-B { border: 2px solid var(--r-b-color); box-shadow: 0 0 8px rgba(0, 176, 255, 0.4), inset 0 0 5px rgba(0, 176, 255, 0.2); }
.bg-B { background: linear-gradient(135deg, #01579b, #0288d1); color: #fff; box-shadow: 0 0 5px var(--r-b-color); border: 1px solid #81d4fa; }
.text-B { color: var(--r-b-color); text-shadow: 0 0 5px rgba(0, 176, 255, 0.5); }

.border-A { border: 2px solid var(--r-a-color); box-shadow: 0 0 10px rgba(213, 0, 249, 0.5), inset 0 0 8px rgba(213, 0, 249, 0.3); }
.bg-A { background: linear-gradient(135deg, #4a148c, #7b1fa2); color: #fff; box-shadow: 0 0 8px var(--r-a-color); border: 1px solid #ea80fc; }
.text-A { color: #ea80fc; text-shadow: 0 0 8px rgba(213, 0, 249, 0.6); }

.border-S { border: 2px solid var(--r-s-color); box-shadow: 0 0 10px var(--r-s-color), 0 0 20px rgba(255, 215, 0, 0.4), inset 0 0 10px rgba(255, 215, 0, 0.5); animation: pulse-gold 2s infinite alternate; }
.bg-S { background: linear-gradient(135deg, #ff6f00, #ffca28); color: #3e2723; font-weight: 900; border: 1px solid #fff; box-shadow: 0 0 10px var(--r-s-color); }
.text-S { color: var(--r-s-color); text-shadow: 0 0 10px rgba(255, 215, 0, 0.8), 0 0 2px #fff; font-weight: bold; }

.border-SS { border: 2px solid var(--r-ss-color); box-shadow: 0 0 15px var(--r-ss-color), inset 0 0 10px rgba(255, 0, 0, 0.6); animation: pulse-red 1.5s infinite alternate; }
.bg-SS { background: linear-gradient(135deg, #b71c1c, #d32f2f); color: #fff; font-weight: 900; border: 1px solid #ff8a80; box-shadow: 0 0 10px var(--r-ss-color); }
.text-SS { color: var(--r-ss-color); text-shadow: 0 0 10px rgba(255, 23, 68, 0.8), 0 0 2px #fff; font-weight: 900; text-transform: uppercase; }

@keyframes pulse-gold { 0% { box-shadow: 0 0 10px var(--r-s-color); } 100% { box-shadow: 0 0 20px var(--r-s-color), 0 0 30px rgba(255, 69, 0, 0.6); } }
@keyframes pulse-red { 0% { box-shadow: 0 0 10px var(--r-ss-color); } 100% { box-shadow: 0 0 25px var(--r-ss-color), 0 0 40px rgba(255, 0, 0, 0.8); } }

/* Tags & Labels */
.rarity-tag { position: absolute; bottom: -10px; font-size: 0.6rem; font-weight: 800; padding: 2px 8px; border-radius: 10px; z-index: 2; text-transform: uppercase; letter-spacing: 1px; }
.level-tag { position: absolute; top: 2px; right: 2px; font-size: 10px; font-weight: 900; z-index: 5; font-family: sans-serif; padding: 0 3px; border-radius: 3px; background: rgba(0, 0, 0, 0.6); border: 1px solid rgba(255, 255, 255, 0.1); }
.qty-badge-corner { position: absolute; top: -5px; right: -5px; background: #b71c1c; color: #fff; font-size: 0.7rem; padding: 2px 5px; border-radius: 4px; font-weight: bold; z-index: 5; }

/* Text & Colors */
.item-name { font-size: 1rem; font-weight: bold; margin-bottom: 5px; }
.item-type { font-size: 0.8rem; color: #9e9e9e; font-style: italic; margin-bottom: 8px; }
.price-row { font-size: 0.95rem; display: flex; justify-content: center; align-items: center; gap: 5px; }
.gold-text { color: var(--gold); font-weight: bold; }
.red-text { color: #ef5350; font-weight: bold; }

/* Buttons & Inputs */
.dark-input { width: 50px; background: #121212; border: 1px solid #444; color: var(--gold); text-align: center; font-weight: bold; padding: 5px; }
.btn-action { flex: 1; border: none; font-weight: bold; cursor: pointer; color: #fff; text-transform: uppercase; font-size: 0.85rem; border-radius: 4px; box-shadow: 0 3px 0 rgba(0, 0, 0, 0.3); transition: 0.2s; }
.btn-action:hover { filter: brightness(1.2); transform: translateY(-2px); } .btn-action:active { transform: translateY(2px); box-shadow: none; }
.btn-buy-sys { background: var(--red-seal); border: 1px solid #b71c1c; } .btn-sell { background: #c62828; border: 1px solid #b71c1c; } .btn-cancel { background: #4e342e; padding: 8px; width: 100%; } .btn-buy-p2p { background: linear-gradient(to bottom, #4caf50, #2e7d32); border: 1px solid #2e7d32; display: flex; align-items: center; justify-content: center; gap: 5px; }

/* Special Cards */
.p2p-card { border-color: #81c784; } .seller-badge { position: absolute; top: 0; left: 0; right: 0; background: rgba(46, 125, 50, 0.2); font-size: 0.7rem; color: #a5d6a7; text-align: center; padding: 2px; } .p2p-img-top { margin-top: 15px; border-bottom: none; padding-bottom: 0; } .p2p-body { padding-top: 5px; } .stock-info { font-size: 0.8rem; color: #aaa; margin-bottom: 5px; } .highlight { color: #fff; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 1000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.bill-modal { width: 400px; background: #fdf5e6; color: #3e2723; border: 4px double #3e2723; box-shadow: 0 0 50px rgba(0, 0, 0, 0.9); animation: popIn 0.3s; }
.bill-header { background: #3e2723; color: var(--gold); padding: 15px; text-align: center; border-bottom: 4px solid var(--red-seal); position: relative; } .bill-header h3 { margin: 0; font-size: 1.4rem; } .bill-seal { position: absolute; top: 5px; left: 10px; border: 2px solid var(--red-seal); color: var(--red-seal); padding: 2px 5px; font-weight: bold; transform: rotate(-15deg); opacity: 0.8; font-size: 0.8rem; }
.bill-body { padding: 20px; } .item-preview-row { display: flex; gap: 15px; align-items: center; margin-bottom: 15px; background: rgba(0, 0, 0, 0.05); padding: 10px; border-radius: 4px; } .preview-img { width: 60px; height: 60px; border: 1px solid #3e2723; background: #fff; display: flex; align-items: center; justify-content: center; } .preview-img img { max-width: 90%; } .preview-info .p-name { font-weight: bold; color: #bf360c; } .preview-info .p-type { font-size: 0.9rem; font-style: italic; }
.bill-calc { font-family: "Courier New", monospace; font-weight: bold; } .calc-row { display: flex; justify-content: space-between; margin-bottom: 5px; } .divider-dashed { border-bottom: 2px dashed #3e2723; margin: 10px 0; } .total { font-size: 1.2rem; color: #d84315; }
.bill-footer { padding: 15px; display: flex; gap: 10px; background: rgba(0, 0, 0, 0.05); } .btn-wood { flex: 1; padding: 10px; font-weight: bold; cursor: pointer; border: 2px solid #3e2723; transition: 0.2s; } .btn-wood.cancel:hover { background: #d7ccc8; } .btn-wood.confirm { background: #bf360c; color: #fff; border-color: #bf360c; } .btn-wood.confirm:hover { background: #d84315; }

@keyframes popIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; } .fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateY(10px); }
</style> -->


<!-- <template>
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
              <option value="WEAPON">Vũ khí (Weapon)</option>
              <option value="ARMOR">Giáp (Armor)</option>
              <option value="ACCESSORY">Trang sức (Accessory)</option>
              <option value="CONSUMABLE">Tiêu hao (Consumable)</option>
              <option value="MATERIAL">Nguyên liệu (Material)</option>
              <option value="TOOL">Công cụ (Tool)</option>
            </select>
          </div>

          <div class="filter-box">
            <i class="fas fa-filter"></i>
            <select v-model="filterRarity">
              <option value="ALL">Tất cả phẩm chất</option>
              <option value="COMMON">Phàm phẩm (Common)</option>
              <option value="UNCOMMON">Lương phẩm (Uncommon)</option>
              <option value="RARE">Linh phẩm (Rare)</option>
              <option value="EPIC">Tiên phẩm (Epic)</option>
              <option value="LEGENDARY">Thần phẩm (Legendary)</option>
              <option value="MYTHIC">Thánh phẩm (Mythic)</option>
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

    <div class="toast-container">
      <transition-group name="toast-slide">
        <div 
          v-for="note in notificationStore.notifications" 
          :key="note.id" 
          class="toast-card" 
          :class="note.type"
        >
          <div class="toast-icon">
            <i v-if="note.type === 'success'" class="fas fa-check-circle"></i>
            <i v-else-if="note.type === 'error'" class="fas fa-skull-crossbones"></i>
            <i v-else class="fas fa-info-circle"></i>
          </div>
          <div class="toast-body">
            <div class="toast-title">{{ note.type === 'success' ? 'Thành Công' : (note.type === 'error' ? 'Thất Bại' : 'Thông Báo') }}</div>
            <div class="toast-msg">{{ note.message }}</div>
          </div>
          <div class="toast-decor"><i class="fas fa-dragon"></i></div>
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
            <button class="btn-wood confirm" @click="confirmTransaction"><i class="fas fa-fingerprint"></i> ẤN
              ĐỊNH</button>
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
import { useAuthStore } from "../stores/authStore";
import { resolveItemImage } from "../utils/assetHelper";

const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const notificationStore = useNotificationStore();
const authStore = useAuthStore();

// --- BACKGROUND LOGIC ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  // Chỉ đổi màu nền theo giờ (18h-6h là tối)
  isNight.value = h >= 18 || h < 6;
};

// --- LOGIC GIAO DỊCH ---
const activeTab = ref("buy");
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});
const searchQuery = ref("");
const filterRarity = ref("ALL");
const filterType = ref("ALL"); 

const confirmModal = reactive({
  visible: false,
  type: "",
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

// Hàm map tên hiển thị
const resolveRarityName = (backendRarity) => {
  const map = { 'COMMON': 'Common', 'UNCOMMON': 'Uncommon', 'RARE': 'Rare', 'EPIC': 'Epic', 'LEGENDARY': 'Legendary', 'MYTHIC': 'Mythic' };
  return map[backendRarity] || 'Common';
};

// Hàm lọc item
const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];
  return list.filter((entry) => {
    const itemData = isInventory ? entry.item : entry.item || entry;
    if (!itemData) return false;

    // Ẩn Nguyên liệu trong Shop
    if (!isInventory && itemData.type === 'MATERIAL' && activeTab.value === 'buy') return false;

    const itemName = itemData.name || "";
    const nameMatch = itemName.toLowerCase().includes(searchQuery.value.toLowerCase());
    
    // Lọc theo Phẩm chất
    const itemRarity = itemData.rarity || "COMMON"; 
    const rarityMatch = filterRarity.value === "ALL" || itemRarity === filterRarity.value;

    // Lọc theo Loại
    const itemType = itemData.type || "";
    const typeMatch = filterType.value === "ALL" || itemType === filterType.value;
    
    return nameMatch && rarityMatch && typeMatch;
  });
};

const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() => filterItems(marketStore.playerListings));
const filteredInventory = computed(() => filterItems(inventoryStore.items, true));

const formatNumber = (n) => Number(n).toLocaleString("en-US");
const getLevelClass = (lv) => (lv >= 15 ? "lvl-red" : lv >= 10 ? "lvl-purple" : lv >= 5 ? "lvl-gold" : "lvl-white");
const handleImgError = (e) => { e.target.src = "https://via.placeholder.com/64?text=IMG"; };

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

const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  openConfirm("SYS", { id: item.itemId, name: item.name, imgCode: item.imageUrl, price: item.basePrice, qty, total: item.basePrice * qty });
};

const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  openConfirm("P2P", { id: listing.listingId, name: listing.item.name, imgCode: listing.item.imageUrl, price: listing.price, qty, total: listing.price * qty });
};

const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = Math.floor(uItem.item.basePrice * 0.8);
  openConfirm("SELL", { id: uItem.userItemId, name: uItem.item.name, imgCode: uItem.item.imageUrl, price: sellPrice, qty, total: sellPrice * qty });
};

const openConfirm = (type, data) => { confirmModal.type = type; confirmModal.data = data; confirmModal.visible = true; };
const closeConfirm = () => { confirmModal.visible = false; };

const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();

  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1;
      notificationStore.showToast(`Mua thành công ${data.qty} ${data.name}!`, "success");
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      notificationStore.showToast("Giao dịch thành công!", "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      // Cập nhật UI ngay lập tức
      const idx = inventoryStore.items.findIndex(i => i.userItemId === data.id);
      if (idx !== -1) {
         if(inventoryStore.items[idx].quantity <= data.qty) {
            inventoryStore.items.splice(idx, 1);
         } else {
            inventoryStore.items[idx].quantity -= data.qty;
         }
      }
      notificationStore.showToast(`Đã bán, nhận ${formatNumber(data.total)} xu!`, "success");
    }
    authStore.fetchProfile();
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings();
    const msg = e.response?.data?.message || "Giao dịch thất bại, vui lòng thử lại.";
    notificationStore.showToast(msg, "error");
  }
};

const handleCancelListing = async (listingId) => {
  if (!confirm("Muốn thu hồi vật phẩm này về túi?")) return;
  try {
    await marketStore.cancelListing(listingId);
    notificationStore.showToast("Đã thu hồi vật phẩm về hành trang.", "success");
  } catch (e) { notificationStore.showToast("Lỗi thu hồi.", "error"); }
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

onMounted(() => {
  updateDayNight();
  Promise.all([
    marketStore.fetchShopItems(),
    marketStore.fetchPlayerListings(),
  ]);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* =========================================
   VARIABLES & BASE
   ========================================= */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;
  --panel-bg: rgba(30, 20, 15, 0.95);
  --card-bg: rgba(38, 24, 21, 0.9);

  /* Màu phẩm chất */
  --r-c-color: #a0a0a0;
  --r-d-color: #4caf50;
  --r-b-color: #00b0ff;
  --r-a-color: #d500f9;
  --r-s-color: #ffd700;
  --r-ss-color: #ff1744;
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
.dark-theme { 
  background-color: transparent; 
  min-height: 100vh; 
  font-family: "Noto Serif TC", serif; 
  color: var(--text-light); 
  position: relative; 
  overflow: hidden; 
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

/* --- HEADER & TABS --- */
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
.header-decor { width: 60px; height: 2px; background: var(--gold); position: absolute; top: 50px; }
.left { left: 20%; } .right { right: 20%; }

.filter-bar { display: flex; justify-content: center; gap: 15px; margin-bottom: 20px; flex-wrap: wrap; }
.search-box, .filter-box { background: rgba(0, 0, 0, 0.5); border: 1px solid #5d4037; padding: 5px 15px; border-radius: 20px; display: flex; align-items: center; gap: 10px; transition: 0.3s; }
.search-box:focus-within, .filter-box:hover { border-color: var(--gold); background: rgba(0, 0, 0, 0.7); box-shadow: 0 0 10px rgba(255, 236, 179, 0.1); }
.search-box i, .filter-box i { color: var(--text-dim); }
.search-box input { background: transparent; border: none; color: #fff; font-family: inherit; width: 200px; outline: none; }
.filter-box select { background: transparent; border: none; color: #fff; font-family: inherit; outline: none; cursor: pointer; }
.filter-box select option { background: #3e2723; color: #fff; }

.market-tabs { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.tab-btn { background: transparent; border: none; color: var(--text-dim); font-weight: bold; font-size: 1rem; cursor: pointer; padding: 10px 15px; transition: 0.3s; border-bottom: 2px solid transparent; }
.tab-btn:hover, .tab-btn.active { color: var(--gold); }
.tab-btn.active { border-bottom-color: var(--gold); text-shadow: 0 0 8px rgba(255, 236, 179, 0.4); }
.tab-divider { width: 1px; height: 20px; background: #555; }

/* --- CONTENT GRID & CARDS --- */
.market-content { flex: 1; overflow-y: auto; padding: 10px; }
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.1); }

.grid-layout { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.empty-state { grid-column: 1 / -1; text-align: center; padding: 50px; color: #757575; font-size: 1.2rem; border: 2px dashed #444; background: rgba(0, 0, 0, 0.2); }
.empty-state i { font-size: 3rem; opacity: 0.5; margin-bottom: 10px; display: block; }

.item-card { 
  background: var(--card-bg); 
  border: 1px solid var(--wood-light); 
  border-radius: 4px; padding: 15px; 
  display: flex; flex-direction: column; gap: 10px; 
  transition: 0.3s; position: relative; 
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); 
}
.item-card:hover { transform: translateY(-5px); border-color: var(--gold); box-shadow: 0 10px 25px rgba(0, 0, 0, 0.7); }

/* Card Elements */
.card-top { display: flex; justify-content: center; padding-bottom: 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
.card-body { flex: 1; text-align: center; }
.card-footer { display: flex; gap: 5px; margin-top: auto; }

/* Image Frame */
.img-frame { width: 70px; height: 70px; background: #1a1a1a; display: flex; align-items: center; justify-content: center; position: relative; transition: all 0.3s ease; box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.8); }
.img-frame.small { width: 50px; height: 50px; }
.img-frame img { max-width: 90%; max-height: 90%; }

/* RARITY COLORS */
.border-COMMON { border: 2px solid var(--r-common-color); box-shadow: 0 0 2px var(--r-common-color); }
.bg-COMMON { background: linear-gradient(135deg, #424242, #616161); color: #fff; border: 1px solid var(--r-common-color); }
.text-COMMON { color: var(--r-common-color); }

.border-UNCOMMON { border: 2px solid var(--r-uncommon-color); box-shadow: 0 0 5px rgba(76, 175, 80, 0.5); }
.bg-UNCOMMON { background: linear-gradient(135deg, #2e7d32, #43a047); color: #fff; border: 1px solid #66bb6a; }
.text-UNCOMMON { color: var(--r-uncommon-color); text-shadow: 0 0 5px rgba(76, 175, 80, 0.4); }

.border-RARE { border: 2px solid var(--r-rare-color); box-shadow: 0 0 8px rgba(0, 176, 255, 0.4), inset 0 0 5px rgba(0, 176, 255, 0.2); }
.bg-RARE { background: linear-gradient(135deg, #01579b, #0288d1); color: #fff; box-shadow: 0 0 5px var(--r-rare-color); border: 1px solid #81d4fa; }
.text-RARE { color: var(--r-rare-color); text-shadow: 0 0 5px rgba(0, 176, 255, 0.5); }

.border-EPIC { border: 2px solid var(--r-epic-color); box-shadow: 0 0 10px rgba(213, 0, 249, 0.5), inset 0 0 8px rgba(213, 0, 249, 0.3); }
.bg-EPIC { background: linear-gradient(135deg, #4a148c, #7b1fa2); color: #fff; box-shadow: 0 0 8px var(--r-epic-color); border: 1px solid #ea80fc; }
.text-EPIC { color: #ea80fc; text-shadow: 0 0 8px rgba(213, 0, 249, 0.6); }

.border-LEGENDARY { border: 2px solid var(--r-legendary-color); box-shadow: 0 0 10px var(--r-legendary-color), 0 0 20px rgba(255, 215, 0, 0.4), inset 0 0 10px rgba(255, 215, 0, 0.5); animation: pulse-gold 2s infinite alternate; }
.bg-LEGENDARY { background: linear-gradient(135deg, #ff6f00, #ffca28); color: #3e2723; font-weight: 900; border: 1px solid #fff; box-shadow: 0 0 10px var(--r-legendary-color); }
.text-LEGENDARY { color: var(--r-legendary-color); text-shadow: 0 0 10px rgba(255, 215, 0, 0.8), 0 0 2px #fff; font-weight: bold; }

.border-MYTHIC { border: 2px solid var(--r-mythic-color); box-shadow: 0 0 15px var(--r-mythic-color), inset 0 0 10px rgba(255, 0, 0, 0.6); animation: pulse-red 1.5s infinite alternate; }
.bg-MYTHIC { background: linear-gradient(135deg, #b71c1c, #d32f2f); color: #fff; font-weight: 900; border: 1px solid #ff8a80; box-shadow: 0 0 10px var(--r-mythic-color); }
.text-MYTHIC { color: var(--r-mythic-color); text-shadow: 0 0 10px rgba(255, 23, 68, 0.8), 0 0 2px #fff; font-weight: 900; text-transform: uppercase; }

@keyframes pulse-gold { 0% { box-shadow: 0 0 10px var(--r-legendary-color); } 100% { box-shadow: 0 0 20px var(--r-legendary-color), 0 0 30px rgba(255, 69, 0, 0.6); } }
@keyframes pulse-red { 0% { box-shadow: 0 0 10px var(--r-mythic-color); } 100% { box-shadow: 0 0 25px var(--r-mythic-color), 0 0 40px rgba(255, 0, 0, 0.8); } }

/* Tags & Labels */
.rarity-tag { position: absolute; bottom: -10px; font-size: 0.6rem; font-weight: 800; padding: 2px 8px; border-radius: 10px; z-index: 2; text-transform: uppercase; letter-spacing: 1px; }
.level-tag { position: absolute; top: 2px; right: 2px; font-size: 10px; font-weight: 900; z-index: 5; font-family: sans-serif; padding: 0 3px; border-radius: 3px; background: rgba(0, 0, 0, 0.6); border: 1px solid rgba(255, 255, 255, 0.1); }
.qty-badge-corner { position: absolute; top: -5px; right: -5px; background: #b71c1c; color: #fff; font-size: 0.7rem; padding: 2px 5px; border-radius: 4px; font-weight: bold; z-index: 5; }

/* Text & Colors */
.item-name { font-size: 1rem; font-weight: bold; margin-bottom: 5px; }
.item-type { font-size: 0.8rem; color: #9e9e9e; font-style: italic; margin-bottom: 8px; }
.price-row { font-size: 0.95rem; display: flex; justify-content: center; align-items: center; gap: 5px; }
.gold-text { color: var(--gold); font-weight: bold; }
.red-text { color: #ef5350; font-weight: bold; }

/* Buttons & Inputs */
.dark-input { width: 50px; background: #121212; border: 1px solid #444; color: var(--gold); text-align: center; font-weight: bold; padding: 5px; }
.btn-action { flex: 1; border: none; font-weight: bold; cursor: pointer; color: #fff; text-transform: uppercase; font-size: 0.85rem; border-radius: 4px; box-shadow: 0 3px 0 rgba(0, 0, 0, 0.3); transition: 0.2s; }
.btn-action:hover { filter: brightness(1.2); transform: translateY(-2px); } .btn-action:active { transform: translateY(2px); box-shadow: none; }
.btn-buy-sys { background: var(--red-seal); border: 1px solid #b71c1c; } 
.btn-sell { background: #c62828; border: 1px solid #b71c1c; } 
.btn-cancel { background: #4e342e; padding: 8px; width: 100%; } 
.btn-buy-p2p { background: linear-gradient(to bottom, #4caf50, #2e7d32); border: 1px solid #2e7d32; display: flex; align-items: center; justify-content: center; gap: 5px; }

/* Special Cards */
.p2p-card { border-color: #81c784; } .seller-badge { position: absolute; top: 0; left: 0; right: 0; background: rgba(46, 125, 50, 0.2); font-size: 0.7rem; color: #a5d6a7; text-align: center; padding: 2px; } .p2p-img-top { margin-top: 15px; border-bottom: none; padding-bottom: 0; } .p2p-body { padding-top: 5px; } .stock-info { font-size: 0.8rem; color: #aaa; margin-bottom: 5px; } .highlight { color: #fff; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 1000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.bill-modal { width: 400px; background: #fdf5e6; color: #3e2723; border: 4px double #3e2723; box-shadow: 0 0 50px rgba(0, 0, 0, 0.9); animation: popIn 0.3s; }
.bill-header { background: #3e2723; color: var(--gold); padding: 15px; text-align: center; border-bottom: 4px solid var(--red-seal); position: relative; } .bill-header h3 { margin: 0; font-size: 1.4rem; } .bill-seal { position: absolute; top: 5px; left: 10px; border: 2px solid var(--red-seal); color: var(--red-seal); padding: 2px 5px; font-weight: bold; transform: rotate(-15deg); opacity: 0.8; font-size: 0.8rem; }
.bill-body { padding: 20px; } .item-preview-row { display: flex; gap: 15px; align-items: center; margin-bottom: 15px; background: rgba(0, 0, 0, 0.05); padding: 10px; border-radius: 4px; } .preview-img { width: 60px; height: 60px; border: 1px solid #3e2723; background: #fff; display: flex; align-items: center; justify-content: center; } .preview-img img { max-width: 90%; } .preview-info .p-name { font-weight: bold; color: #bf360c; } .preview-info .p-type { font-size: 0.9rem; font-style: italic; }
.bill-calc { font-family: "Courier New", monospace; font-weight: bold; } .calc-row { display: flex; justify-content: space-between; margin-bottom: 5px; } .divider-dashed { border-bottom: 2px dashed #3e2723; margin: 10px 0; } .total { font-size: 1.2rem; color: #d84315; }
.bill-footer { padding: 15px; display: flex; gap: 10px; background: rgba(0, 0, 0, 0.05); } .btn-wood { flex: 1; padding: 10px; font-weight: bold; cursor: pointer; border: 2px solid #3e2723; transition: 0.2s; } .btn-wood.cancel:hover { background: #d7ccc8; } .btn-wood.confirm { background: #bf360c; color: #fff; border-color: #bf360c; } .btn-wood.confirm:hover { background: #d84315; }

/* [NEW] TOAST STYLES */
.toast-container {
  position: fixed;
  top: 80px; 
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 15px;
  pointer-events: none; 
}

.toast-card {
  pointer-events: auto;
  width: 300px;
  background: rgba(38, 24, 21, 0.95); 
  border: 1px solid #5d4037;
  border-left: 4px solid #5d4037;
  padding: 12px 15px;
  border-radius: 4px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(5px);
}

.toast-decor {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 3rem;
  opacity: 0.1;
  color: #fff;
  transform: rotate(-15deg);
}

/* Toast Success */
.toast-card.success {
  border-left-color: #4caf50;
  box-shadow: 0 5px 15px rgba(76, 175, 80, 0.2);
}
.toast-card.success .toast-icon { color: #4caf50; }
.toast-card.success .toast-title { color: #81c784; }

/* Toast Error */
.toast-card.error {
  border-left-color: #d32f2f;
  box-shadow: 0 5px 15px rgba(211, 47, 47, 0.2);
}
.toast-card.error .toast-icon { color: #d32f2f; }
.toast-card.error .toast-title { color: #ef5350; }

/* Toast Info/Default */
.toast-card.info { border-left-color: #29b6f6; }
.toast-card.info .toast-icon { color: #29b6f6; }
.toast-card.info .toast-title { color: #81d4fa; }

/* Toast Content */
.toast-icon { font-size: 1.5rem; }
.toast-body { flex: 1; }
.toast-title { font-size: 0.9rem; font-weight: 900; text-transform: uppercase; margin-bottom: 2px; }
.toast-msg { font-size: 0.85rem; color: #e0e0e0; font-family: sans-serif; }

/* Toast Animation */
.toast-slide-enter-active, .toast-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.toast-slide-enter-from { opacity: 0; transform: translateX(100%); }
.toast-slide-leave-to { opacity: 0; transform: translateX(100%); }

@keyframes popIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; } .fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateY(10px); }
</style> -->



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
                <button @click="openCancelDialog(listing.listingId)" class="btn-action btn-cancel"><i class="fas fa-undo"></i> THU HỒI</button>
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

const marketStore = useMarketStore();
const inventoryStore = useInventoryStore();
const authStore = useAuthStore();

// --- LOGIC CUSTOM TOAST (Thay alert) ---
const toasts = ref([]);
let toastIdCounter = 0;

const showToast = (message, type = 'info') => {
  const id = toastIdCounter++;
  toasts.value.push({ id, message, type });
  setTimeout(() => {
    const idx = toasts.value.findIndex(t => t.id === id);
    if (idx !== -1) toasts.value.splice(idx, 1);
  }, 3000); // Tự tắt sau 3s
};

// --- LOGIC MESSAGE BOX (Thay confirm browser) ---
const msgBox = reactive({
  visible: false,
  message: "",
  callback: null
});

const openCancelDialog = (listingId) => {
  msgBox.message = "Đạo hữu muốn thu hồi vật phẩm này về túi?";
  msgBox.visible = true;
  // Lưu hành động vào callback để thực hiện khi bấm Đồng Ý
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

const confirmMsgAction = () => {
  if (msgBox.callback) msgBox.callback();
  closeMsgBox();
};

// --- BACKGROUND ---
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);
const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- SEARCH & FILTER ---
const activeTab = ref("buy");
const sellQty = reactive({});
const buyQty = reactive({});
const p2pQty = reactive({});
const searchQuery = ref("");
const filterRarity = ref("ALL");
const filterType = ref("ALL"); 

// --- TRANSACTION CONFIRM MODAL ---
const confirmModal = reactive({
  visible: false,
  type: "",
  data: { id: null, name: "", imgCode: "", price: 0, qty: 0, total: 0 },
});

// Helpers
const resolveRarityName = (backendRarity) => {
  const map = { 'COMMON': 'Common', 'UNCOMMON': 'Uncommon', 'RARE': 'Rare', 'EPIC': 'Epic', 'LEGENDARY': 'Legendary', 'MYTHIC': 'Mythic' };
  return map[backendRarity] || 'Common';
};

const formatNumber = (n) => Number(n).toLocaleString("en-US");
const getLevelClass = (lv) => (lv >= 15 ? "lvl-red" : lv >= 10 ? "lvl-purple" : lv >= 5 ? "lvl-gold" : "lvl-white");
const handleImgError = (e) => { e.target.src = "https://via.placeholder.com/64?text=IMG"; };

const filterItems = (list, isInventory = false) => {
  if (!list || !Array.isArray(list)) return [];
  return list.filter((entry) => {
    const itemData = isInventory ? entry.item : entry.item || entry;
    if (!itemData) return false;
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

const filteredShopItems = computed(() => filterItems(marketStore.shopItems));
const filteredPlayerListings = computed(() => filterItems(marketStore.playerListings));
const filteredInventory = computed(() => filterItems(inventoryStore.items, true));

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

// --- LOGIC MUA BÁN (Dùng Custom Toast thay vì alert) ---
const askBuySystem = (item) => {
  const qty = buyQty[item.itemId] || 1;
  const totalCost = Number(item.basePrice) * Number(qty);
  const userGold = Number(authStore.user?.gold || 0); 

  if (userGold < totalCost) {
    showToast("Ngân lượng không đủ, không thể mua!", "error");
    return;
  }
  openConfirm("SYS", { id: item.itemId, name: item.name, imgCode: item.imageUrl, price: item.basePrice, qty, total: totalCost });
};

const askBuyP2P = (listing) => {
  const qty = p2pQty[listing.listingId] || 1;
  const totalCost = Number(listing.price) * Number(qty);
  const userGold = Number(authStore.user?.gold || 0);

  if (userGold < totalCost) {
    showToast("Túi tiền rỗng tuếch, không thể giao dịch!", "error");
    return;
  }
  openConfirm("P2P", { id: listing.listingId, name: listing.item.name, imgCode: listing.item.imageUrl, price: listing.price, qty, total: totalCost });
};

const askSellSystem = (uItem) => {
  const qty = sellQty[uItem.userItemId] || uItem.quantity;
  const sellPrice = Math.floor(uItem.item.basePrice * 0.8);
  openConfirm("SELL", { id: uItem.userItemId, name: uItem.item.name, imgCode: uItem.item.imageUrl, price: sellPrice, qty, total: sellPrice * qty });
};

const openConfirm = (type, data) => { confirmModal.type = type; confirmModal.data = data; confirmModal.visible = true; };
const closeConfirm = () => { confirmModal.visible = false; };

const confirmTransaction = async () => {
  const { type, data } = confirmModal;
  closeConfirm();

  try {
    if (type === "SYS") {
      await marketStore.buyItem(data.id, data.qty);
      buyQty[data.id] = 1;
      showToast(`Mua thành công ${data.qty} ${data.name}!`, "success");
    } else if (type === "P2P") {
      await marketStore.buyPlayerListing(data.id, data.qty);
      p2pQty[data.id] = 1;
      showToast("Giao dịch thành công!", "success");
    } else if (type === "SELL") {
      await marketStore.sellItem(data.id, data.qty);
      const idx = inventoryStore.items.findIndex(i => i.userItemId === data.id);
      if (idx !== -1) {
         if(inventoryStore.items[idx].quantity <= data.qty) {
            inventoryStore.items.splice(idx, 1);
         } else {
            inventoryStore.items[idx].quantity -= data.qty;
         }
      }
      showToast(`Đã bán, nhận ${formatNumber(data.total)} ngân lượng!`, "success");
    }
    await authStore.fetchProfile(); 
  } catch (e) {
    if (type === "P2P") await marketStore.fetchPlayerListings();
    const msg = e.response?.data?.message || "Có lỗi xảy ra.";
    showToast(msg, "error");
  }
};

const loadMyListings = async () => {
  switchTab("my_listings");
  await marketStore.fetchMyListings();
};

onMounted(async () => {
  updateDayNight();
  if (authStore.fetchProfile) await authStore.fetchProfile();
  
  Promise.all([
    marketStore.fetchShopItems(),
    marketStore.fetchPlayerListings(),
  ]);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* =========================================
   BASE STYLES
   ========================================= */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;
  --card-bg: rgba(38, 24, 21, 0.9);
}

.dark-theme { 
  background-color: transparent; 
  min-height: 100vh; 
  font-family: "Noto Serif TC", serif; 
  color: var(--text-light); 
  position: relative; 
  overflow: hidden; 
}

/* Background */
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

/* Layout */
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
  text-align: center; margin-bottom: 20px; 
  background: rgba(30, 20, 15, 0.8); 
  border-top: 4px solid var(--wood-light); 
  border-bottom: 4px solid var(--wood-light); 
  padding: 20px; 
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8); 
  position: relative; 
}
.market-title { font-size: 2.5rem; color: var(--gold); margin: 0 0 15px 0; text-transform: uppercase; letter-spacing: 5px; font-weight: 900; text-shadow: 0 0 10px rgba(255, 236, 179, 0.3); }
.header-decor { width: 60px; height: 2px; background: var(--gold); position: absolute; top: 50px; }
.left { left: 20%; } .right { right: 20%; }

.filter-bar { display: flex; justify-content: center; gap: 15px; margin-bottom: 20px; flex-wrap: wrap; }
.search-box, .filter-box { background: rgba(0, 0, 0, 0.5); border: 1px solid #5d4037; padding: 5px 15px; border-radius: 20px; display: flex; align-items: center; gap: 10px; transition: 0.3s; }
.search-box input { background: transparent; border: none; color: #fff; font-family: inherit; width: 200px; outline: none; }
.filter-box select { background: transparent; border: none; color: #fff; font-family: inherit; outline: none; cursor: pointer; }
.filter-box select option { background: #3e2723; color: #fff; }

.market-tabs { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.tab-btn { background: transparent; border: none; color: var(--text-dim); font-weight: bold; font-size: 1rem; cursor: pointer; padding: 10px 15px; transition: 0.3s; border-bottom: 2px solid transparent; }
.tab-btn:hover, .tab-btn.active { color: var(--gold); border-bottom-color: var(--gold); text-shadow: 0 0 8px rgba(255, 236, 179, 0.4); }
.tab-divider { width: 1px; height: 20px; background: #555; }

/* Grid & Cards */
.market-content { flex: 1; overflow-y: auto; padding: 10px; }
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

/* Styles cho ảnh, phẩm chất, giá, nút bấm (Giữ nguyên như cũ) */
.img-frame { width: 70px; height: 70px; background: #1a1a1a; display: flex; align-items: center; justify-content: center; position: relative; transition: all 0.3s ease; box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.8); }
.img-frame.small { width: 50px; height: 50px; }
.img-frame img { max-width: 90%; max-height: 90%; }
.rarity-tag { position: absolute; bottom: -10px; font-size: 0.6rem; font-weight: 800; padding: 2px 8px; border-radius: 10px; z-index: 2; text-transform: uppercase; letter-spacing: 1px; }
.level-tag { position: absolute; top: 2px; right: 2px; font-size: 10px; font-weight: 900; z-index: 5; background: rgba(0,0,0,0.6); padding: 0 3px; border-radius: 3px; border: 1px solid rgba(255,255,255,0.1); }
.qty-badge-corner { position: absolute; top: -5px; right: -5px; background: #b71c1c; color: #fff; font-size: 0.7rem; padding: 2px 5px; border-radius: 4px; font-weight: bold; z-index: 5; }

/* Màu Rarity */
.border-COMMON { border: 2px solid #a0a0a0; } .bg-COMMON { background: #616161; color: #fff; } .text-COMMON { color: #a0a0a0; }
.border-UNCOMMON { border: 2px solid #4caf50; } .bg-UNCOMMON { background: #2e7d32; color: #fff; } .text-UNCOMMON { color: #4caf50; }
.border-RARE { border: 2px solid #00b0ff; } .bg-RARE { background: #01579b; color: #fff; } .text-RARE { color: #00b0ff; }
.border-EPIC { border: 2px solid #d500f9; } .bg-EPIC { background: #4a148c; color: #fff; } .text-EPIC { color: #ea80fc; }
.border-LEGENDARY { border: 2px solid #ffd700; animation: pulse-gold 2s infinite; } .bg-LEGENDARY { background: linear-gradient(135deg, #ff6f00, #ffca28); color: #3e2723; } .text-LEGENDARY { color: #ffd700; }
.border-MYTHIC { border: 2px solid #ff1744; animation: pulse-red 1.5s infinite; } .bg-MYTHIC { background: #b71c1c; color: #fff; } .text-MYTHIC { color: #ff1744; }

@keyframes pulse-gold { 0%, 100% { box-shadow: 0 0 10px #ffd700; } 50% { box-shadow: 0 0 20px #ff6f00; } }
@keyframes pulse-red { 0%, 100% { box-shadow: 0 0 10px #ff1744; } 50% { box-shadow: 0 0 25px #b71c1c; } }

.item-name { font-size: 1rem; font-weight: bold; margin-bottom: 5px; }
.item-type { font-size: 0.8rem; color: #9e9e9e; font-style: italic; margin-bottom: 8px; }
.price-row { font-size: 0.95rem; } .gold-text { color: var(--gold); font-weight: bold; } .red-text { color: #ef5350; font-weight: bold; }

.dark-input { width: 50px; background: #121212; border: 1px solid #444; color: var(--gold); text-align: center; padding: 5px; }
.btn-action { flex: 1; border: none; font-weight: bold; cursor: pointer; color: #fff; text-transform: uppercase; font-size: 0.85rem; border-radius: 4px; box-shadow: 0 3px 0 rgba(0,0,0,0.3); transition: 0.2s; }
.btn-action:hover { transform: translateY(-2px); filter: brightness(1.2); }
.btn-buy-sys { background: var(--red-seal); } .btn-sell { background: #c62828; } .btn-buy-p2p { background: linear-gradient(to bottom, #4caf50, #2e7d32); } .btn-cancel { background: #4e342e; }

.seller-badge { position: absolute; top: 0; left: 0; right: 0; background: rgba(46, 125, 50, 0.2); font-size: 0.7rem; color: #a5d6a7; text-align: center; padding: 2px; }
.p2p-img-top { margin-top: 15px; border-bottom: none; padding-bottom: 0; }
.p2p-body { padding-top: 5px; } .stock-info { font-size: 0.8rem; color: #aaa; margin-bottom: 5px; } .highlight { color: #fff; }

/* --- [MỚI] CUSTOM TOAST STYLES --- */
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

/* --- [MỚI] MODAL CHUNG & MESSAGE BOX --- */
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); z-index: 1000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.bill-modal { width: 400px; background: #fdf5e6; color: #3e2723; border: 4px double #3e2723; box-shadow: 0 0 50px rgba(0, 0, 0, 0.9); animation: popIn 0.3s; }
.bill-header { background: #3e2723; color: var(--gold); padding: 15px; text-align: center; border-bottom: 4px solid var(--red-seal); position: relative; }
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