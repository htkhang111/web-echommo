<!-- <template>
  <div class="admin-page custom-scrollbar">
    <div class="bg-layer base"></div>
    <div class="bg-layer overlay"></div>
    <div class="particles"></div>

    <div class="admin-container">
      <header class="admin-header fade-in-down">
        <div class="header-content">
          <h1 class="glow-text">QUẢN TRỊ THIÊN PHỦ</h1>
          <p class="subtitle">Nơi nắm giữ vận mệnh chúng sinh, điều phối thiên cơ</p>
        </div>
        <div class="header-decor"></div>
      </header>

      <nav class="admin-nav fade-in">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          @click="switchTab(tab.id)"
          :class="['nav-item', { active: activeTab === tab.id }]"
        >
          <i :class="tab.icon"></i>
          <span>{{ tab.label }}</span>
          <div class="nav-glow"></div>
        </button>
      </nav>

      <main class="admin-content fade-in-up">
        
        <section v-if="activeTab === 'stats'" class="stats-section">
          <div class="stat-card glass-panel">
            <div class="icon-box user"><i class="fas fa-users"></i></div>
            <div class="info">
              <h3>Tổng Nhân Sĩ</h3>
              <div class="number">{{ adminStore.stats.totalUsers || 0 }}</div>
            </div>
          </div>
          <div class="stat-card glass-panel">
            <div class="icon-box item"><i class="fas fa-khanda"></i></div>
            <div class="info">
              <h3>Kỳ Trân Dị Bảo</h3>
              <div class="number">{{ adminStore.stats.totalItems || 0 }}</div>
            </div>
          </div>
          <div class="stat-card glass-panel highlight">
            <div class="icon-box gold"><i class="fas fa-coins"></i></div>
            <div class="info">
              <h3>Tổng Ngân Lượng</h3>
              <div class="number gold-text">{{ formatNumber(adminStore.stats.totalGold || 0) }}</div>
            </div>
          </div>
          <div class="stat-card glass-panel highlight-echo">
            <div class="icon-box echo"><i class="fas fa-gem"></i></div>
            <div class="info">
              <h3>Tổng EchoCoin</h3>
              <div class="number echo-text">{{ formatNumber(adminStore.stats.totalEchoMined || 0) }}</div>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'users'" class="data-section">
          <div class="filter-bar glass-panel">
            <div class="search-group">
              <i class="fas fa-search"></i>
              <input v-model="userFilters.search" placeholder="Truy tìm danh tính..." />
            </div>
            <div class="actions">
               <select v-model="userFilters.role">
                 <option value="">Tất cả Giới</option>
                 <option value="USER">Đạo Hữu (User)</option>
                 <option value="ADMIN">Tiên Đế (Admin)</option>
               </select>
               <select v-model="userFilters.status">
                 <option value="">Sinh Mệnh</option>
                 <option value="active">Tự Do</option>
                 <option value="banned">Ngục Tối</option>
               </select>
            </div>
          </div>

          <div class="table-wrapper glass-panel custom-scroll-panel">
            <table class="wuxia-table">
              <thead>
                <tr>
                  <th>Linh Bài (ID)</th>
                  <th>Đạo Hiệu</th>
                  <th>Truyền Âm (Email)</th>
                  <th>Cảnh Giới</th>
                  <th>Trạng Thái</th>
                  <th>Phán Quyết</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in filteredUsers" :key="u.userId">
                  <td class="id-col">#{{ u.userId }}</td>
                  <td class="name-col">{{ u.username }}</td>
                  <td>{{ u.email }}</td>
                  <td>
                    <span :class="['badge', u.role.toLowerCase()]">{{ u.role }}</span>
                  </td>
                  <td>
                    <span v-if="u.isActive" class="status-dot active"></span>
                    <span v-else class="status-dot banned"></span>
                    {{ u.isActive ? 'Tiêu Dao' : 'Phong Ấn' }}
                  </td>
                  <td class="actions-cell">
                    <button @click="openAdminChat(u)" class="btn-icon chat" title="Mật đàm">
                      <i class="fas fa-comment-dots"></i>
                    </button>
                    <button v-if="u.isActive" @click="openBanModal(u)" class="btn-icon ban" title="Giam cầm">
                      <i class="fas fa-gavel"></i>
                    </button>
                    <button v-else @click="requestUnban(u)" class="btn-icon unban" title="Ân xá">
                      <i class="fas fa-lock-open"></i>
                    </button>
                    <button @click="requestDeleteUser(u)" class="btn-icon delete" title="Trảm hồn (Xóa)">
                      <i class="fas fa-skull"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="filteredUsers.length === 0" class="empty-data">
              <i class="fas fa-wind"></i> Không tìm thấy bóng dáng ai...
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'items'" class="data-section">
          <div class="panel-header">
            <button @click="showCreateItem = !showCreateItem" class="btn-create">
              <i class="fas fa-plus-circle"></i> Chế Tác Bảo Vật
            </button>
          </div>

          <transition name="fade">
            <div v-if="showCreateItem" class="create-panel glass-panel fade-in">
               <h3 class="panel-title-small">Lò Luyện Khí</h3>
               <form @submit.prevent="createItem" class="grid-form">
                  <input v-model="itemForm.code" placeholder="Mã Vật Phẩm (Code - Duy nhất)" required />
                  
                  <input v-model="itemForm.name" placeholder="Tên Pháp Bảo" required />
                  <input v-model="itemForm.description" placeholder="Mô tả huyền năng" class="full-col" />
                  
                  <select v-model="itemForm.type" @change="syncSlotType">
                    <option value="WEAPON">Binh Khí</option>
                    <option value="ARMOR">Y Phục</option>
                    <option value="HELMET">Mũ Giáp</option>
                    <option value="BOOTS">Hài (Giày)</option>
                    <option value="RING">Nhẫn Trữ Vật</option>
                    <option value="NECKLACE">Vòng Cổ</option>
                    <option value="CONSUMABLE">Đan Dược</option>
                    <option value="MATERIAL">Thiên Tài Địa Bảo</option>
                  </select>

                  <select v-model="itemForm.rarity">
                    <option value="COMMON">Thường (Common)</option>
                    <option value="UNCOMMON">Phi Thường (Uncommon)</option>
                    <option value="RARE">Hiếm (Rare)</option>
                    <option value="EPIC">Sử Thi (Epic)</option>
                    <option value="LEGENDARY">Huyền Thoại (Legendary)</option>
                    <option value="MYTHIC">Thần Thoại (Mythic)</option>
                  </select>

                  <input v-model.number="itemForm.basePrice" type="number" placeholder="Giá trị (Vàng)" />
                  <input v-model="itemForm.imageUrl" placeholder="Linh Ảnh (URL)" class="full-col" />
                  
                  <div class="stats-inputs full-col">
                      <input v-model.number="itemForm.atkBonus" type="number" placeholder="Tấn Công" />
                      <input v-model.number="itemForm.defBonus" type="number" placeholder="Phòng Thủ" />
                      <input v-model.number="itemForm.hpBonus" type="number" placeholder="Sinh Lực" />
                      <input v-model.number="itemForm.speedBonus" type="number" placeholder="Tốc Độ" />
                  </div>

                  <div class="form-actions full-col">
                      <label class="checkbox-label">
                        <input type="checkbox" v-model="itemForm.isSystemItem"> Vật phẩm Vô Hạn (Shop)
                      </label>
                      <button type="submit" class="btn-submit">Khai Lò (Tạo)</button>
                  </div>
               </form>
            </div>
          </transition>

          <div class="table-wrapper glass-panel custom-scroll-panel">
            <table class="wuxia-table">
              <thead>
                <tr>
                  <th>Mã (Code)</th>
                  <th>Pháp Bảo</th>
                  <th>Chủng Loại</th>
                  <th>Phẩm Cấp</th>
                  <th>Giá Trị</th>
                  <th>Hủy Bỏ</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="i in filteredItems" :key="i.itemId">
                  <td class="id-col">{{ i.code }}</td>
                  <td class="name-col">{{ i.name }}</td>
                  <td>{{ i.type }}</td>
                  <td :class="['rarity', 'rarity-' + i.rarity]">{{ i.rarity }}</td>
                  <td class="gold-val">{{ formatNumber(i.basePrice) }}</td>
                  <td>
                    <button @click="requestDeleteItem(i)" class="btn-icon delete">
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
        
        <section v-if="activeTab === 'notify'" class="grid-2-col">
           <div class="glass-panel">
             <h3 class="panel-title"><i class="fas fa-gift"></i> Ban Thưởng Thiên Triều</h3>
             
             <form @submit.prevent="handleGrantGold" class="simple-form">
               <input v-model="grantGoldForm.username" placeholder="Đạo hiệu người nhận" required />
               <input v-model.number="grantGoldForm.amount" type="number" placeholder="Số lượng vàng" required />
               <button type="submit" class="btn-action gold">Ban Ngân Lượng</button>
             </form>

             <form @submit.prevent="handleGrantEcho" class="simple-form" style="margin-top: 15px;">
                <input v-model="grantEchoForm.username" placeholder="Đạo hiệu người nhận" required />
                <input v-model="grantEchoForm.amount" type="text" placeholder="Số lượng Echo (VD: 10.5)" required />
                <button type="submit" class="btn-action cyan">Ban EchoCoin</button>
             </form>

             <div class="divider-line"></div>

             <form @submit.prevent="handleGrantItem" class="simple-form">
               <input v-model="grantItemForm.username" placeholder="Đạo hiệu người nhận" required />
               <select v-model.number="grantItemForm.itemId" required>
                 <option :value="0">Chọn vật phẩm...</option>
                 <option v-for="item in itemOptions" :key="item.id" :value="item.id">
                   {{ item.name }}
                 </option>
               </select>
               <input v-model.number="grantItemForm.quantity" type="number" placeholder="Số lượng" required />
               <button type="submit" class="btn-action blue">Ban Bảo Vật</button>
             </form>
           </div>

           <div class="glass-panel">
              <h3 class="panel-title"><i class="fas fa-scroll"></i> Soạn Cáo Thị</h3>
              <form @submit.prevent="handleSendNotification" class="simple-form">
                <input v-model="notificationForm.title" placeholder="Tiêu đề..." required />
                <select v-model="notificationForm.type" class="dark-select">
                  <option value="INFO">Tin Tức</option>
                  <option value="SUCCESS">Tin Mừng</option>
                  <option value="WARNING">Cảnh Báo</option>
                  <option value="REWARD">Phần Thưởng</option>
                </select>
                <textarea v-model="notificationForm.message" rows="5" placeholder="Nội dung cáo thị..."></textarea>
                <input v-model="notificationForm.recipientUsername" placeholder="Người nhận (Để trống = Toàn Server)" />
                <button type="submit" class="btn-action red">Phát Loa Toàn Cõi</button>
              </form>
           </div>
        </section>

      </main>
    </div>

    <div v-if="showBanModal" class="modal-backdrop" @click.self="showBanModal = false">
      <div class="modal-content ban-theme zoom-in">
         <div class="modal-header-danger">
            <i class="fas fa-gavel"></i> THI HÀNH KỶ LUẬT
         </div>
         <div class="modal-body">
            <p>Đối tượng: <strong class="target-name">{{ selectedUser?.username }}</strong></p>
            <textarea v-model="banReason" placeholder="Lý do trừng phạt (Vi phạm thiên quy)..."></textarea>
         </div>
         <div class="modal-btns">
           <button @click="showBanModal = false" class="btn-cancel">Hủy</button>
           <button @click="confirmBan" class="btn-confirm-ban">Thực Thi</button>
         </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useAdminStore } from "../stores/adminStore";
import { useNotificationStore } from "../stores/notificationStore";
import { useChatStore } from "../stores/chatStore";
import axiosClient from "../api/axiosClient";

const adminStore = useAdminStore();
const notificationStore = useNotificationStore();
const chatStore = useChatStore();

const activeTab = ref("stats");
const showCreateItem = ref(false);
const showBanModal = ref(false);
const selectedUser = ref(null);
const banReason = ref("");

const tabs = [
  { id: 'stats', label: 'Thiên Cơ (Thống Kê)', icon: 'fas fa-chart-pie' },
  { id: 'users', label: 'Nhân Sĩ (User)', icon: 'fas fa-users' },
  { id: 'items', label: 'Tàng Bảo (Item)', icon: 'fas fa-box-open' },
  { id: 'notify', label: 'Điều Hành (Action)', icon: 'fas fa-tasks' }
];

const userFilters = reactive({ search: "", role: "", status: "" });
const itemFilters = reactive({ search: "", type: "", rarity: "" });

// [FIX] Cập nhật itemForm để khớp với Entity Item
const itemForm = reactive({
    code: "", // Bắt buộc
    name: "",
    description: "",
    type: "WEAPON",
    slotType: "WEAPON", // Bắt buộc
    rarity: "COMMON", // Giá trị mặc định chuẩn
    basePrice: 100,
    imageUrl: "",
    isSystemItem: false,
    atkBonus: 0,
    defBonus: 0,
    hpBonus: 0,
    speedBonus: 0
});

// Forms
const grantGoldForm = reactive({ username: "", amount: 1000 });
const grantEchoForm = reactive({ username: "", amount: "" });
const grantItemForm = reactive({ username: "", itemId: 0, quantity: 1 });
const notificationForm = reactive({ title: "", message: "", type: "INFO", recipientUsername: "" });

// Computed & Helpers
const formatNumber = (n) => Number(n).toLocaleString();

const filteredUsers = computed(() => {
  let users = adminStore.users || [];
  if (userFilters.search) {
    const s = userFilters.search.toLowerCase();
    users = users.filter(u => u.username?.toLowerCase().includes(s) || u.email?.toLowerCase().includes(s) || u.userId?.toString().includes(s));
  }
  if (userFilters.role) users = users.filter(u => u.role === userFilters.role);
  if (userFilters.status === 'active') users = users.filter(u => u.isActive);
  if (userFilters.status === 'banned') users = users.filter(u => !u.isActive);
  return users;
});

const filteredItems = computed(() => {
  let items = adminStore.items || [];
  if (itemFilters.search) {
    const s = itemFilters.search.toLowerCase();
    items = items.filter(i => i.name?.toLowerCase().includes(s));
  }
  return items;
});

const itemOptions = computed(() => (adminStore.items || []).map(i => ({ id: i.itemId, name: i.name })));

// Methods
const switchTab = (tab) => {
  activeTab.value = tab;
  if (tab === 'stats') adminStore.fetchStats();
  if (tab === 'users') adminStore.fetchUsers();
  if (tab === 'items' || tab === 'notify') adminStore.fetchItems();
};

const openAdminChat = (user) => {
  chatStore.openChatWith(user);
  notificationStore.showToast(`Đang kết nối thần thức với ${user.username}`, "info");
};

// [NEW] Đồng bộ SlotType với Type (vì logic hiện tại chúng khá giống nhau)
const syncSlotType = () => {
    itemForm.slotType = itemForm.type;
};

// Actions
const createItem = async () => {
    try { 
        // Đảm bảo slotType có giá trị
        if (!itemForm.slotType) itemForm.slotType = itemForm.type;
        
        await axiosClient.post("/admin/item/create", itemForm); 
        notificationStore.showToast("Chế tác thành công!", "success"); 
        adminStore.fetchItems(); 
        showCreateItem.value = false;
        
        // Reset code để tránh trùng lặp cho lần tạo sau
        itemForm.code = "";
    } catch(e) { 
        console.error(e);
        notificationStore.showToast("Lỗi chế tác: " + (e.response?.data?.message || "Kiểm tra lại thông tin"), "error"); 
    }
};

const handleGrantGold = async () => {
    try { await adminStore.grantGold(grantGoldForm.username, grantGoldForm.amount); notificationStore.showToast("Đã cấp ngân lượng!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
};

const handleGrantEcho = async () => {
    try { 
        await adminStore.grantEcho(grantEchoForm.username, grantEchoForm.amount); 
        notificationStore.showToast("Đã ban EchoCoin!", "success");
        grantEchoForm.username = "";
        grantEchoForm.amount = "";
    } catch(e) { 
        notificationStore.showToast("Lỗi ban thưởng Echo!", "error"); 
    }
};

const handleGrantItem = async () => {
    try { await adminStore.grantItem(grantItemForm.username, grantItemForm.itemId, grantItemForm.quantity); notificationStore.showToast("Đã ban bảo vật!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
};
const handleSendNotification = async () => {
    try { await adminStore.sendNotification(notificationForm); notificationStore.showToast("Đã phát loa!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
};

// Ban Logic
const openBanModal = (u) => { selectedUser.value = u; banReason.value = ""; showBanModal.value = true; };
const confirmBan = async () => {
    if(!banReason.value) return notificationStore.showToast("Cần lý do trừng phạt!", "warning");
    try { 
        await adminStore.banUser(selectedUser.value.userId, banReason.value); 
        notificationStore.showToast("Đã thi hành án!", "success"); 
        showBanModal.value = false; 
    } catch(e) { notificationStore.showToast("Lỗi ban!", "error"); }
};
const requestUnban = async (u) => {
    if(confirm(`Ân xá cho ${u.username}?`)) {
        try { await adminStore.unbanUser(u.userId); notificationStore.showToast("Đã ân xá!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
    }
};
const requestDeleteUser = async (u) => {
    if(confirm(`Xóa vĩnh viễn ${u.username}?`)) {
        try { await axiosClient.delete(`/admin/user/${u.userId}`); adminStore.fetchUsers(); notificationStore.showToast("Đã xóa!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
    }
};
const requestDeleteItem = async (i) => {
    if(confirm(`Hủy bỏ vật phẩm ${i.name}?`)) {
        try { await axiosClient.delete(`/admin/item/${i.itemId}`); adminStore.fetchItems(); notificationStore.showToast("Đã hủy!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
    }
};

onMounted(() => {
    adminStore.fetchStats();
    adminStore.fetchUsers();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700&family=Noto+Serif+TC:wght@400;700&display=swap');

/* --- GLOBAL LAYOUT --- */
.admin-page {
  min-height: 100vh;
  background-color: #0a0a0a;
  color: #e0e0e0;
  font-family: 'Noto Serif TC', serif;
  position: relative;
  overflow-x: hidden;
}

.bg-layer { position: fixed; inset: 0; z-index: 0; pointer-events: none; }
.bg-layer.base { background: radial-gradient(circle at top, #1a100e, #000000); }
.bg-layer.overlay { 
  background-image: url("https://www.transparenttextures.com/patterns/black-scales.png");
  opacity: 0.3;
}

.admin-container {
  position: relative;
  z-index: 10;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* --- HEADER --- */
.admin-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
}
.glow-text {
  font-family: 'Cinzel', serif;
  font-size: 3.5rem;
  color: #ffca28;
  text-transform: uppercase;
  margin: 0;
  text-shadow: 0 0 10px rgba(255, 202, 40, 0.5), 0 0 20px rgba(255, 0, 0, 0.3);
  letter-spacing: 5px;
  background: -webkit-linear-gradient(#ffd700, #ff8c00);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.subtitle {
  color: #aaa;
  font-size: 1.1rem;
  font-style: italic;
  margin-top: 10px;
  letter-spacing: 1px;
}
.header-decor {
  height: 2px;
  background: linear-gradient(90deg, transparent, #ffca28, transparent);
  width: 50%;
  margin: 20px auto 0;
}

/* --- NAVIGATION --- */
.admin-nav {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}
.nav-item {
  background: rgba(20, 20, 20, 0.8);
  border: 1px solid #444;
  color: #bbb;
  padding: 12px 25px;
  font-size: 1.2rem;
  cursor: pointer;
  position: relative;
  transition: all 0.4s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: 'Noto Serif TC', serif;
  text-transform: uppercase;
  clip-path: polygon(10% 0, 100% 0, 100% 100%, 0 100%, 0 20%);
}
.nav-item:hover, .nav-item.active {
  background: rgba(62, 39, 35, 0.9);
  color: #ffca28;
  border-color: #ffca28;
  box-shadow: 0 0 15px rgba(255, 202, 40, 0.2);
  transform: translateY(-2px);
}
.nav-item.active i { color: #ffca28; }

/* --- SECTIONS & PANELS --- */
.glass-panel {
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 215, 0, 0.15);
  border-radius: 8px;
  padding: 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-bottom: 20px;
}

.table-wrapper {
  max-height: 70vh; 
  overflow-y: auto; 
  position: relative;
  padding: 0;
}

.custom-scroll-panel::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.custom-scroll-panel::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}
.custom-scroll-panel::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 4px;
  border: 1px solid #3e2723;
}
.custom-scroll-panel::-webkit-scrollbar-thumb:hover {
  background: #ffca28;
}

.wuxia-table {
  width: 100%;
  border-collapse: collapse;
}

.wuxia-table th {
  position: sticky;
  top: 0;
  z-index: 2;
  text-align: left;
  padding: 15px;
  color: #ffca28;
  font-family: 'Cinzel', serif;
  border-bottom: 2px solid rgba(255, 215, 0, 0.5);
  text-transform: uppercase;
  letter-spacing: 1px;
  background: rgba(30, 20, 15, 0.98); 
  box-shadow: 0 2px 5px rgba(0,0,0,0.5);
}

.wuxia-table td {
  padding: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  vertical-align: middle;
  color: #ccc;
  transition: background 0.2s;
}
.wuxia-table tr:hover td {
  background: linear-gradient(90deg, rgba(255, 202, 40, 0.05), transparent);
  color: #fff;
}
.id-col { font-family: monospace; color: #666; }
.name-col { font-weight: bold; color: #fff; font-size: 1.1rem; }
.gold-val { color: #ffca28; font-weight: bold; }

/* Stats Cards */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 25px;
  border-left: 4px solid #444;
}
.stat-card.highlight { border-left-color: #ffca28; }
.stat-card.highlight-echo { border-left-color: #26c6da; }

.icon-box {
  width: 80px; height: 80px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 2.5rem;
  background: rgba(0,0,0,0.4);
}
.user { color: #42a5f5; text-shadow: 0 0 10px #42a5f5; }
.item { color: #ab47bc; text-shadow: 0 0 10px #ab47bc; }
.gold { color: #ffca28; text-shadow: 0 0 10px #ffca28; }
.echo { color: #26c6da; text-shadow: 0 0 10px #26c6da; }

.info h3 { margin: 0; color: #888; font-size: 1rem; text-transform: uppercase; letter-spacing: 1px; }
.info .number { font-size: 2.5rem; font-weight: bold; color: #eee; font-family: 'Cinzel', serif; }
.echo-text { color: #26c6da; }

/* Badges */
.badge { padding: 4px 10px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; text-transform: uppercase; }
.badge.user { background: rgba(33, 150, 243, 0.15); color: #64b5f6; border: 1px solid #1e88e5; }
.badge.admin { background: rgba(255, 167, 38, 0.15); color: #ffb74d; border: 1px solid #fb8c00; }
.status-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 8px; }
.status-dot.active { background: #00e676; box-shadow: 0 0 8px #00e676; }
.status-dot.banned { background: #ff1744; box-shadow: 0 0 8px #ff1744; }

/* Action Buttons */
.actions-cell { display: flex; gap: 8px; }
.btn-icon {
  width: 40px; height: 40px; border: none; border-radius: 50%; 
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.3s; color: #fff; box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}
.chat { background: linear-gradient(135deg, #42a5f5, #1565c0); }
.ban { background: linear-gradient(135deg, #ef5350, #c62828); }
.unban { background: linear-gradient(135deg, #66bb6a, #2e7d32); }
.delete { background: linear-gradient(135deg, #757575, #424242); }
.btn-icon:hover { transform: translateY(-3px) scale(1.1); filter: brightness(1.2); }

/* --- FORMS & FILTERS --- */
.filter-bar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.search-group { position: relative; flex: 1; max-width: 400px; }
.search-group i { position: absolute; left: 15px; top: 50%; transform: translateY(-50%); color: #ffca28; }
.search-group input { 
  width: 100%; padding: 12px 12px 12px 45px; background: rgba(0,0,0,0.3); 
  border: 1px solid #555; color: #fff; border-radius: 30px; outline: none;
  font-family: inherit;
}
.search-group input:focus { border-color: #ffca28; box-shadow: 0 0 10px rgba(255, 202, 40, 0.2); }
select { background: #222; color: #fff; border: 1px solid #555; padding: 10px 15px; border-radius: 4px; outline: none; }

.grid-form { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-top: 15px; }
.stats-inputs { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.full-col { grid-column: span 2; }
.simple-form input, .simple-form textarea, .grid-form input, .grid-form select, .stats-inputs input {
  width: 100%; padding: 12px; background: rgba(0,0,0,0.4); border: 1px solid #555;
  color: #fff; border-radius: 4px; box-sizing: border-box; margin-bottom: 10px; font-family: inherit;
}
.btn-action, .btn-submit {
  width: 100%; padding: 12px; border: none; font-weight: bold; cursor: pointer;
  border-radius: 4px; text-transform: uppercase; transition: 0.3s; font-family: 'Cinzel', serif; letter-spacing: 1px;
}
.btn-action.gold { background: linear-gradient(45deg, #FFD700, #FFA000); color: #000; }
.btn-action.cyan { background: linear-gradient(45deg, #26c6da, #00acc1); color: #fff; }
.btn-action.blue { background: linear-gradient(45deg, #42a5f5, #1976d2); color: #fff; }
.btn-action.red { background: linear-gradient(45deg, #ef5350, #c62828); color: #fff; }
.btn-submit { background: #ffca28; color: #000; margin-top: 10px; font-weight: bold; }
.btn-create {
  background: transparent; border: 1px solid #ffca28; color: #ffca28;
  padding: 10px 25px; border-radius: 30px; cursor: pointer; font-weight: bold;
  transition: 0.3s; text-transform: uppercase;
}
.btn-create:hover { background: #ffca28; color: #000; box-shadow: 0 0 15px #ffca28; }

.grid-2-col { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
.divider-line { height: 1px; background: #444; margin: 20px 0; }

/* --- MODAL PHÁN QUYẾT --- */
.modal-backdrop {
  position: fixed; inset: 0; background: rgba(0,0,0,0.85); backdrop-filter: blur(5px);
  z-index: 2000; display: flex; align-items: center; justify-content: center;
}
.modal-content.ban-theme {
  background: #1a0505; border: 2px solid #ef5350; width: 450px; 
  box-shadow: 0 0 50px rgba(239, 83, 80, 0.4); overflow: hidden;
}
.modal-header-danger {
  background: #c62828; color: #fff; padding: 15px; text-align: center;
  font-weight: bold; font-size: 1.2rem; text-transform: uppercase;
}
.modal-body { padding: 25px; }
.target-name { color: #ef5350; font-size: 1.2rem; margin-left: 5px; }
.modal-content textarea { width: 100%; height: 100px; margin-top: 15px; background: #000; border: 1px solid #555; color: #fff; padding: 10px; }
.modal-btns { display: flex; justify-content: flex-end; padding: 15px; gap: 10px; background: rgba(0,0,0,0.2); }
.btn-cancel { background: transparent; color: #aaa; border: 1px solid #555; padding: 8px 20px; cursor: pointer; }
.btn-confirm-ban { background: #ef5350; color: #fff; border: none; padding: 8px 25px; cursor: pointer; font-weight: bold; }

/* --- RARITY COLORS --- */
.rarity-COMMON { color: #bdbdbd; }
.rarity-UNCOMMON { color: #4caf50; }
.rarity-RARE { color: #42a5f5; }
.rarity-EPIC { color: #ab47bc; }
.rarity-LEGENDARY { color: #ffca28; text-shadow: 0 0 5px #ffca28; }
.rarity-MYTHIC { color: #ef5350; text-shadow: 0 0 8px #ef5350; font-weight: bold; }

/* --- ANIMATIONS --- */
.fade-in { animation: fadeIn 0.5s ease; }
.fade-in-down { animation: fadeInDown 0.6s ease; }
.fade-in-up { animation: fadeInUp 0.6s ease; }
.zoom-in { animation: zoomIn 0.3s ease; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes zoomIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style> -->
<template>
  <div class="admin-page custom-scrollbar">
    <div class="bg-layer base"></div>
    <div class="bg-layer overlay"></div>
    <div class="particles"></div>

    <div class="admin-container">
      <header class="admin-header fade-in-down">
        <div class="header-content">
          <h1 class="glow-text">QUẢN TRỊ THIÊN PHỦ</h1>
          <p class="subtitle">Nơi nắm giữ vận mệnh chúng sinh, điều phối thiên cơ</p>
        </div>
        <div class="header-decor"></div>
      </header>

      <nav class="admin-nav fade-in">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          @click="switchTab(tab.id)"
          :class="['nav-item', { active: activeTab === tab.id }]"
        >
          <i :class="tab.icon"></i>
          <span>{{ tab.label }}</span>
          <div class="nav-glow"></div>
        </button>
      </nav>

      <main class="admin-content fade-in-up">
        
        <section v-if="activeTab === 'stats'" class="stats-section">
          <div class="stat-card glass-panel">
            <div class="icon-box user"><i class="fas fa-users"></i></div>
            <div class="info">
              <h3>Tổng Nhân Sĩ</h3>
              <div class="number">{{ adminStore.stats.totalUsers || 0 }}</div>
            </div>
          </div>
          <div class="stat-card glass-panel">
            <div class="icon-box item"><i class="fas fa-khanda"></i></div>
            <div class="info">
              <h3>Kỳ Trân Dị Bảo</h3>
              <div class="number">{{ adminStore.stats.totalItems || 0 }}</div>
            </div>
          </div>
          <div class="stat-card glass-panel highlight">
            <div class="icon-box gold"><i class="fas fa-coins"></i></div>
            <div class="info">
              <h3>Tổng Ngân Lượng</h3>
              <div class="number gold-text">{{ formatNumber(adminStore.stats.totalGold || 0) }}</div>
            </div>
          </div>
          <div class="stat-card glass-panel highlight-echo">
            <div class="icon-box echo"><i class="fas fa-gem"></i></div>
            <div class="info">
              <h3>Tổng EchoCoin</h3>
              <div class="number echo-text">{{ formatNumber(adminStore.stats.totalEchoMined || 0) }}</div>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'users'" class="data-section">
          <div class="filter-bar glass-panel">
            <div class="search-group">
              <i class="fas fa-search"></i>
              <input v-model="userFilters.search" placeholder="Truy tìm danh tính..." />
            </div>
            <div class="actions">
               <select v-model="userFilters.role">
                 <option value="">Tất cả Giới</option>
                 <option value="USER">Đạo Hữu (User)</option>
                 <option value="ADMIN">Tiên Đế (Admin)</option>
               </select>
               <select v-model="userFilters.status">
                 <option value="">Sinh Mệnh</option>
                 <option value="active">Tự Do</option>
                 <option value="banned">Ngục Tối</option>
               </select>
            </div>
          </div>

          <div class="table-wrapper glass-panel custom-scroll-panel">
            <table class="wuxia-table">
              <thead>
                <tr>
                  <th>Linh Bài (ID)</th>
                  <th>Đạo Hiệu</th>
                  <th>Truyền Âm (Email)</th>
                  <th>Cảnh Giới</th>
                  <th>Trạng Thái</th>
                  <th>Phán Quyết</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in filteredUsers" :key="u.userId">
                  <td class="id-col">#{{ u.userId }}</td>
                  <td class="name-col">{{ u.username }}</td>
                  <td>{{ u.email }}</td>
                  <td>
                    <span :class="['badge', u.role.toLowerCase()]">{{ u.role }}</span>
                  </td>
                  <td>
                    <span v-if="u.isActive" class="status-dot active"></span>
                    <span v-else class="status-dot banned"></span>
                    {{ u.isActive ? 'Tiêu Dao' : 'Phong Ấn' }}
                  </td>
                  <td class="actions-cell">
                    <button @click="openAdminChat(u)" class="btn-icon chat" title="Mật đàm">
                      <i class="fas fa-comment-dots"></i>
                    </button>
                    <button v-if="u.isActive" @click="openBanModal(u)" class="btn-icon ban" title="Giam cầm">
                      <i class="fas fa-gavel"></i>
                    </button>
                    <button v-else @click="requestUnban(u)" class="btn-icon unban" title="Ân xá">
                      <i class="fas fa-lock-open"></i>
                    </button>
                    <button @click="requestDeleteUser(u)" class="btn-icon delete" title="Trảm hồn (Xóa)">
                      <i class="fas fa-skull"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="filteredUsers.length === 0" class="empty-data">
              <i class="fas fa-wind"></i> Không tìm thấy bóng dáng ai...
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'items'" class="data-section">
          <div class="panel-header">
            <button @click="toggleCreateMode" class="btn-create">
              <i :class="showCreateItem ? 'fas fa-times' : 'fas fa-plus-circle'"></i> 
              {{ showCreateItem ? 'Đóng Lò Luyện' : 'Chế Tác Bảo Vật' }}
            </button>
          </div>

          <transition name="fade">
            <div v-if="showCreateItem" class="create-panel glass-panel fade-in wizard-container">
               <div class="wizard-header">
                   <h3 class="panel-title-small"><i class="fas fa-fire"></i> Lò Luyện Tạo Tác</h3>
                   <div class="step-counter">Bước {{ currentStep }}/6</div>
               </div>
               
               <div class="progress-container">
                 <div class="progress-bar" :style="{ width: ((currentStep / 6) * 100) + '%' }"></div>
               </div>

               <form @submit.prevent="createItem" class="wizard-form">
                  
                  <div v-if="currentStep === 1" class="wizard-step active">
                    <div class="step-label">1. Chọn Chủng Loại</div>
                    <div class="type-grid">
                      <div v-for="t in itemTypes" :key="t.value" 
                           :class="['type-card', { selected: itemForm.type === t.value }]"
                           @click="selectType(t.value)">
                        <div class="card-glow"></div>
                        <i :class="t.icon"></i>
                        <span>{{ t.label }}</span>
                      </div>
                    </div>
                  </div>

                  <div v-if="currentStep === 2" class="wizard-step active">
                    <div class="step-label">2. Định Danh Pháp Bảo</div>
                    <div class="grid-2">
                      <div class="input-group">
                        <label>Mã Định Danh (Code)</label>
                        <input v-model="itemForm.code" placeholder="VD: SWORD_DRAGON_01" required />
                      </div>
                      <div class="input-group">
                        <label>Tên Hiển Thị</label>
                        <input v-model="itemForm.name" placeholder="VD: Huyết Long Đao" required />
                      </div>
                      <div class="input-group full-width">
                         <label>Mô Tả Huyền Năng</label>
                         <textarea v-model="itemForm.description" placeholder="Mô tả công dụng, truyền thuyết..." rows="3"></textarea>
                      </div>
                    </div>
                    <div class="wizard-actions">
                        <button type="button" @click="currentStep--" class="btn-back">Quay lại</button>
                        <button type="button" @click="nextStep" class="btn-next" :disabled="!itemForm.code || !itemForm.name">
                          Tiếp Tục <i class="fas fa-arrow-right"></i>
                        </button>
                    </div>
                  </div>

                  <div v-if="currentStep === 3" class="wizard-step active">
                    <div class="step-label">3. Chọn Hình Dáng (Asset)</div>
                    <div class="assets-wrapper custom-scrollbar">
                       <div class="assets-grid">
                           <div v-for="(img, idx) in currentAssets" :key="idx" 
                                :class="['asset-item', { selected: itemForm.imageUrl === img }]"
                                @click="selectAsset(img)">
                              <img :src="img" alt="asset" />
                              <div class="asset-check"><i class="fas fa-check"></i></div>
                           </div>
                       </div>
                    </div>
                    <div class="input-group mt-2">
                        <label>Hoặc nhập URL ảnh ngoài:</label>
                        <input v-model="itemForm.imageUrl" placeholder="https://..." class="full-width"/>
                    </div>
                    <div class="wizard-actions">
                        <button type="button" @click="currentStep--" class="btn-back">Quay lại</button>
                        <button type="button" @click="nextStep" class="btn-next" :disabled="!itemForm.imageUrl">
                          Tiếp Tục <i class="fas fa-arrow-right"></i>
                        </button>
                    </div>
                  </div>

                  <div v-if="currentStep === 4" class="wizard-step active">
                    <div class="step-label">4. Chọn Phẩm Cấp</div>
                    <div class="rarity-grid">
                        <div v-for="r in rarities" :key="r.value"
                             :class="['rarity-card', r.value, { selected: itemForm.rarity === r.value }]"
                             @click="itemForm.rarity = r.value; calculatePrice()">
                            <span>{{ r.label }}</span>
                            <small>{{ getSubStatInfo(r.value) }}</small>
                        </div>
                    </div>
                    <div class="wizard-actions">
                        <button type="button" @click="currentStep--" class="btn-back">Quay lại</button>
                        <button type="button" @click="nextStep" class="btn-next">
                          Tiếp Tục <i class="fas fa-arrow-right"></i>
                        </button>
                    </div>
                  </div>

                  <div v-if="currentStep === 5" class="wizard-step active">
                    <div class="step-label">5. Thổi Hồn (Chỉ Số)</div>
                    
                    <div class="stat-section">
                      <label class="sub-label highlight">1. Chỉ Số Chính (Dựa theo loại {{ itemForm.type }})</label>
                      <div class="stat-type-selector">
                          <div v-for="stat in getAllowedMainStats" :key="stat.value"
                               :class="['stat-chip', { active: selectedMainStatType === stat.value }]"
                               @click="selectedMainStatType = stat.value; calculatePrice()">
                              <i :class="stat.icon"></i> {{ stat.label }}
                          </div>
                      </div>
                      <div class="input-group mt-2">
                          <input type="number" v-model.number="selectedMainStatValue" 
                                 class="big-input" placeholder="Nhập giá trị..." 
                                 @input="calculatePrice"/>
                      </div>
                    </div>

                    <div class="divider-line"></div>

                    <div class="stat-section">
                      <label class="sub-label">2. Dòng Phụ ({{ getSubStatCount() }} dòng)</label>
                      <div v-if="getSubStatCount() > 0" class="sub-stats-container">
                          <div v-for="i in getSubStatCount()" :key="i" class="sub-stat-row fade-in">
                             <span class="row-index">#{{i}}</span>
                             <select class="sub-stat-select" @change="calculatePrice">
                                <option value="ATK_PERCENT">ATK %</option>
                                <option value="CRIT_RATE">Crit Rate %</option>
                                <option value="CRIT_DMG">Crit Dmg %</option>
                                <option value="SPEED">Speed</option>
                             </select>
                             <input type="number" placeholder="Giá trị" @input="calculatePrice" />
                          </div>
                      </div>
                      <div v-else class="empty-text">Phẩm chất này không có dòng phụ.</div>
                    </div>

                    <div class="wizard-actions">
                        <button type="button" @click="currentStep--" class="btn-back">Quay lại</button>
                        <button type="button" @click="nextStep" class="btn-next">
                          Tiếp Tục <i class="fas fa-arrow-right"></i>
                        </button>
                    </div>
                  </div>

                  <div v-if="currentStep === 6" class="wizard-step active">
                     <div class="step-label">6. Thẩm Định & Xuất Xưởng</div>
                     
                     <div class="summary-card">
                         <div class="item-preview">
                             <img :src="itemForm.imageUrl || '/placeholder.png'" class="preview-img"/>
                             <div class="preview-info">
                                 <h4 :class="'rarity-' + itemForm.rarity">{{ itemForm.name }}</h4>
                                 <p>{{ itemForm.rarity }} - {{ itemForm.type }}</p>
                                 <div class="preview-stats">
                                     Main: +{{ selectedMainStatValue }} {{ selectedMainStatType }}
                                 </div>
                             </div>
                         </div>
                     </div>

                     <div class="distribution-box">
                        <label class="sub-label">Loại Hình Phân Phối:</label>
                        <div class="dist-options">
                            <div :class="['dist-card', { active: distributionType === 'SHOP' }]" @click="distributionType = 'SHOP'">
                                <i class="fas fa-store"></i>
                                <div><strong>Shop Vô Hạn</strong><small>Bán đại trà.</small></div>
                            </div>
                            <div :class="['dist-card', { active: distributionType === 'DROP' }]" @click="distributionType = 'DROP'">
                                <i class="fas fa-skull"></i>
                                <div><strong>Săn Bắt (Drop)</strong><small>Rơi từ quái.</small></div>
                            </div>
                            <div :class="['dist-card', 'limited', { active: distributionType === 'LIMITED' }]" @click="distributionType = 'LIMITED'">
                                <i class="fas fa-crown"></i>
                                <div><strong>Hàng Hiếm</strong><small>Độc quyền Admin.</small></div>
                            </div>
                        </div>
                     </div>

                     <div class="pricing-box">
                        <div class="price-row">
                          <span>Giá Gợi Ý:</span>
                          <strong class="suggested-price">{{ formatNumber(suggestedPrice) }} Vàng</strong>
                        </div>
                        <div class="input-group">
                           <label>Giá Niêm Yết:</label>
                           <input v-model.number="itemForm.basePrice" type="number" class="price-input" />
                        </div>
                     </div>

                     <div class="wizard-actions final">
                        <button type="button" @click="currentStep = 1" class="btn-reset">Làm Lại</button>
                        <button type="submit" class="btn-submit-final">KHAI LÒ (TẠO NGAY)</button>
                     </div>
                  </div>

               </form>
            </div>
          </transition>

          <div class="table-wrapper glass-panel custom-scroll-panel">
            <table class="wuxia-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Pháp Bảo</th>
                  <th>Loại</th>
                  <th>Phẩm Cấp</th>
                  <th>Giá</th>
                  <th>Nguồn</th>
                  <th>Hủy</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="i in filteredItems" :key="i.itemId">
                  <td class="id-col">{{ i.code }}</td>
                  <td class="name-col">
                     <img :src="resolveItemImage(i.imageUrl)" class="mini-icon"/> {{ i.name }}
                  </td>
                  <td>{{ i.type }}</td>
                  <td :class="['rarity', 'rarity-' + i.rarity]">{{ i.rarity }}</td>
                  <td class="gold-val">{{ formatNumber(i.basePrice) }}</td>
                  <td>
                      <span v-if="i.isLimited" class="badge limited">Limited</span>
                      <span v-else-if="i.isSystemItem" class="badge active">Shop</span>
                      <span v-else class="badge drop">Drop</span>
                  </td>
                  <td>
                    <button @click="requestDeleteItem(i)" class="btn-icon delete">
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
        
        <section v-if="activeTab === 'notify'" class="grid-2-col">
           <div class="glass-panel">
             <h3 class="panel-title"><i class="fas fa-gift"></i> Ban Thưởng Thiên Triều</h3>
             
             <form @submit.prevent="handleGrantGold" class="simple-form">
               <input v-model="grantGoldForm.username" placeholder="Đạo hiệu người nhận" required />
               <input v-model.number="grantGoldForm.amount" type="number" placeholder="Số lượng vàng" required />
               <button type="submit" class="btn-action gold">Ban Ngân Lượng</button>
             </form>

             <form @submit.prevent="handleGrantEcho" class="simple-form" style="margin-top: 15px;">
                <input v-model="grantEchoForm.username" placeholder="Đạo hiệu người nhận" required />
                <input v-model="grantEchoForm.amount" type="text" placeholder="Số lượng Echo (VD: 10.5)" required />
                <button type="submit" class="btn-action cyan">Ban EchoCoin</button>
             </form>

             <div class="divider-line"></div>

             <form @submit.prevent="handleGrantItem" class="simple-form">
               <input v-model="grantItemForm.username" placeholder="Đạo hiệu người nhận" required />
               <select v-model.number="grantItemForm.itemId" required>
                 <option :value="0">Chọn vật phẩm...</option>
                 <option v-for="item in itemOptions" :key="item.id" :value="item.id">
                   {{ item.name }}
                 </option>
               </select>
               <input v-model.number="grantItemForm.quantity" type="number" placeholder="Số lượng" required />
               <button type="submit" class="btn-action blue">Ban Bảo Vật</button>
             </form>
           </div>

           <div class="glass-panel">
              <h3 class="panel-title"><i class="fas fa-scroll"></i> Soạn Cáo Thị</h3>
              <form @submit.prevent="handleSendNotification" class="simple-form">
                <input v-model="notificationForm.title" placeholder="Tiêu đề..." required />
                <select v-model="notificationForm.type" class="dark-select">
                  <option value="INFO">Tin Tức</option>
                  <option value="SUCCESS">Tin Mừng</option>
                  <option value="WARNING">Cảnh Báo</option>
                  <option value="REWARD">Phần Thưởng</option>
                </select>
                <textarea v-model="notificationForm.message" rows="5" placeholder="Nội dung cáo thị..."></textarea>
                <input v-model="notificationForm.recipientUsername" placeholder="Người nhận (Để trống = Toàn Server)" />
                <button type="submit" class="btn-action red">Phát Loa Toàn Cõi</button>
              </form>
           </div>
        </section>

      </main>
    </div>

    <div v-if="showBanModal" class="modal-backdrop" @click.self="showBanModal = false">
      <div class="modal-content ban-theme zoom-in">
         <div class="modal-header-danger">
            <i class="fas fa-gavel"></i> THI HÀNH KỶ LUẬT
         </div>
         <div class="modal-body">
            <p>Đối tượng: <strong class="target-name">{{ selectedUser?.username }}</strong></p>
            <textarea v-model="banReason" placeholder="Lý do trừng phạt (Vi phạm thiên quy)..."></textarea>
         </div>
         <div class="modal-btns">
           <button @click="showBanModal = false" class="btn-cancel">Hủy</button>
           <button @click="confirmBan" class="btn-confirm-ban">Thực Thi</button>
         </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useAdminStore } from "../stores/adminStore";
import { useNotificationStore } from "../stores/notificationStore";
import { useChatStore } from "../stores/chatStore";
import axiosClient from "../api/axiosClient";
import { resolveItemImage } from "../utils/assetHelper"; // Import helper xử lý ảnh

const adminStore = useAdminStore();
const notificationStore = useNotificationStore();
const chatStore = useChatStore();

const activeTab = ref("stats");
const showCreateItem = ref(false);
const showBanModal = ref(false);
const selectedUser = ref(null);
const banReason = ref("");

// WIZARD STATES
const currentStep = ref(1);
const suggestedPrice = ref(0);
const distributionType = ref('DROP'); 
const selectedMainStatType = ref('ATK'); 
const selectedMainStatValue = ref(0);

// --- ASSET CONFIG (CHÍNH XÁC TỪ GITHUB) ---
// Dùng tên file thô để khớp với resolveItemImage
const assetLibrary = {
    WEAPON: [
        "s_sword_0.png", "s_sword_1.png", "s_sword_2.png", "s_sword_3.png", "s_sword_4.png",
        "tool/axe/a-0-strongaxe.png", "tool/axe/a-1-best axeinthegame.png", "tool/axe/a-2-axeofthegods.png",
        "tool/pickaxe/p-0-strongpickaxe.png", "tool/pickaxe/p-4-dimensionalpickaxe.png",
        "tool/fishing-rod/fr-0-strongfishingrod.png", "tool/fishing-rod/fr-4-themasterbaiter.png"
    ],
    ARMOR: [ "a_armor_0.png", "a_armor_1.png", "a_armor_2.png", "a_armor_3.png", "a_armor_4.png" ],
    HELMET: [ "h_helmet_0.png", "h_helmet_1.png", "h_helmet_2.png", "h_helmet_3.png", "h_helmet_4.png" ],
    BOOTS: [ "b_boot_0.png", "b_boot_1.png", "b_boot_2.png", "b_boot_3.png", "b_boot_4.png" ],
    RING: [ "ri_ring_0.png", "ri_ring_1.png", "ri_ring_2.png", "ri_ring_3.png", "ri_ring_4.png" ],
    NECKLACE: [ "n_necklace_0.png", "n_necklace_1.png", "n_necklace_2.png", "n_necklace_3.png", "n_necklace_4.png" ],
    CONSUMABLE: [ "r_potion.png" ],
    MATERIAL: [ "w_wood.png", "o_iron.png", "o_gold.png", "f_fish.png", "r_coinEcho.png" ]
};

const tabs = [
  { id: 'stats', label: 'Thiên Cơ (Thống Kê)', icon: 'fas fa-chart-pie' },
  { id: 'users', label: 'Nhân Sĩ (User)', icon: 'fas fa-users' },
  { id: 'items', label: 'Tàng Bảo (Item)', icon: 'fas fa-box-open' },
  { id: 'notify', label: 'Điều Hành (Action)', icon: 'fas fa-tasks' }
];

const itemTypes = [
  { value: 'WEAPON', label: 'Binh Khí', icon: 'fas fa-khanda' },
  { value: 'ARMOR', label: 'Y Phục', icon: 'fas fa-tshirt' },
  { value: 'HELMET', label: 'Mũ Giáp', icon: 'fas fa-hard-hat' },
  { value: 'BOOTS', label: 'Hài (Giày)', icon: 'fas fa-shoe-prints' },
  { value: 'RING', label: 'Nhẫn', icon: 'fas fa-ring' },
  { value: 'NECKLACE', label: 'Vòng Cổ', icon: 'fas fa-gem' },
  { value: 'CONSUMABLE', label: 'Đan Dược', icon: 'fas fa-flask' },
  { value: 'MATERIAL', label: 'Nguyên Liệu', icon: 'fas fa-cubes' }
];

const rarities = [
    { value: 'COMMON', label: 'Thường' },
    { value: 'UNCOMMON', label: 'Phi Thường' },
    { value: 'RARE', label: 'Hiếm' },
    { value: 'EPIC', label: 'Sử Thi' },
    { value: 'LEGENDARY', label: 'Huyền Thoại' },
    { value: 'MYTHIC', label: 'Thần Thoại' }
];

const userFilters = reactive({ search: "", role: "", status: "" });

// Item Form
const itemForm = reactive({
    code: "", name: "", description: "",
    type: "WEAPON", slotType: "WEAPON",
    rarity: "COMMON", basePrice: 0, imageUrl: "", isSystemItem: false, isLimited: false,
    atkBonus: 0, defBonus: 0, hpBonus: 0, speedBonus: 0
});

// Forms
const grantGoldForm = reactive({ username: "", amount: 1000 });
const grantEchoForm = reactive({ username: "", amount: "" });
const grantItemForm = reactive({ username: "", itemId: 0, quantity: 1 });
const notificationForm = reactive({ title: "", message: "", type: "INFO", recipientUsername: "" });

// Computed & Helpers
const formatNumber = (n) => Number(n).toLocaleString();

const filteredUsers = computed(() => {
  let users = adminStore.users || [];
  if (userFilters.search) {
    const s = userFilters.search.toLowerCase();
    users = users.filter(u => u.username?.toLowerCase().includes(s) || u.email?.toLowerCase().includes(s) || u.userId?.toString().includes(s));
  }
  if (userFilters.role) users = users.filter(u => u.role === userFilters.role);
  if (userFilters.status === 'active') users = users.filter(u => u.isActive);
  if (userFilters.status === 'banned') users = users.filter(u => !u.isActive);
  return users;
});

const filteredItems = computed(() => {
  return (adminStore.items || []).slice().reverse();
});

const itemOptions = computed(() => (adminStore.items || []).map(i => ({ id: i.itemId, name: i.name })));

const currentAssets = computed(() => {
    const list = assetLibrary[itemForm.type] || assetLibrary['WEAPON'];
    // Dùng resolveItemImage để tạo link full cho Preview trong Wizard
    return list.map(path => resolveItemImage(path));
});

const getAllowedMainStats = computed(() => {
    const all = [
        { value: 'ATK', label: 'Tấn Công', icon: 'fas fa-fist-raised' },
        { value: 'DEF', label: 'Phòng Thủ', icon: 'fas fa-shield-alt' },
        { value: 'HP', label: 'Sinh Lực', icon: 'fas fa-heart' },
        { value: 'SPEED', label: 'Tốc Độ', icon: 'fas fa-wind' }
    ];
    switch(itemForm.type) {
        case 'WEAPON': return [all[0]]; 
        case 'ARMOR': return [all[1]]; 
        case 'HELMET': return [all[2], all[1]]; 
        case 'BOOTS': return [all[3], all[1]];
        case 'RING': return [all[0], all[3]];
        case 'NECKLACE': return [all[0], all[1], all[2]];
        default: return all;
    }
});

// Methods
const switchTab = (tab) => {
  activeTab.value = tab;
  if (tab === 'stats') adminStore.fetchStats();
  if (tab === 'users') adminStore.fetchUsers();
  if (tab === 'items' || tab === 'notify') adminStore.fetchItems();
};

const openAdminChat = (user) => {
  chatStore.openChatWith(user);
  notificationStore.showToast(`Đang kết nối thần thức với ${user.username}`, "info");
};

// --- WIZARD METHODS ---
const toggleCreateMode = () => {
    showCreateItem.value = !showCreateItem.value;
    if(showCreateItem.value) {
        currentStep.value = 1;
        Object.assign(itemForm, {
            code: "", name: "", description: "", type: "WEAPON", slotType: "WEAPON",
            rarity: "COMMON", basePrice: 0, imageUrl: "", isSystemItem: false, isLimited: false,
            atkBonus: 0, defBonus: 0, hpBonus: 0, speedBonus: 0
        });
        distributionType.value = 'DROP';
        selectedMainStatType.value = 'ATK';
        selectedMainStatValue.value = 0;
        suggestedPrice.value = 0;
    }
};

const selectType = (t) => {
    itemForm.type = t; 
    itemForm.slotType = t;
    setTimeout(() => nextStep(), 200);
};

// Khi chọn asset, ta cần lấy lại tên file gốc (bỏ domain) để lưu vào DB
const selectAsset = (fullUrl) => {
    // Tách tên file từ URL (ví dụ: https://.../resources/tool/axe/a-0.png -> tool/axe/a-0.png)
    // Cách đơn giản nhất là dựa vào phần sau 'resources/' hoặc lấy tên file nếu assetHelper xử lý
    // Ở đây ta gán tạm fullUrl vào để hiển thị, lúc save sẽ xử lý sau hoặc Backend tự xử lý
    // Tuy nhiên để đồng bộ với logic assetHelper mới, ta nên lưu tên file nếu có thể.
    // Hack: Ta duyệt lại mảng gốc để tìm match
    const rawList = assetLibrary[itemForm.type] || assetLibrary['WEAPON'];
    const matched = rawList.find(raw => resolveItemImage(raw) === fullUrl);
    
    itemForm.imageUrl = matched || fullUrl; // Lưu tên file gốc (vd: tool/axe/...)
};

const nextStep = () => {
    if(currentStep.value < 6) currentStep.value++;
    if(currentStep.value === 6) calculatePrice();
};

const getSubStatCount = () => {
    const map = { COMMON: 1, UNCOMMON: 0, RARE: 2, EPIC: 3, LEGENDARY: 4, MYTHIC: 5 };
    return map[itemForm.rarity] || 0;
};

const getSubStatInfo = (r) => {
    const map = { COMMON: 1, UNCOMMON: 0, RARE: 2, EPIC: 3, LEGENDARY: 4, MYTHIC: 5 };
    return (map[r] || 0) + ' dòng';
};

const calculatePrice = () => {
    itemForm.atkBonus = 0; itemForm.defBonus = 0; itemForm.hpBonus = 0; itemForm.speedBonus = 0;
    if (selectedMainStatType.value === 'ATK') itemForm.atkBonus = selectedMainStatValue.value;
    if (selectedMainStatType.value === 'DEF') itemForm.defBonus = selectedMainStatValue.value;
    if (selectedMainStatType.value === 'HP') itemForm.hpBonus = selectedMainStatValue.value;
    if (selectedMainStatType.value === 'SPEED') itemForm.speedBonus = selectedMainStatValue.value;

    let score = 200;
    score += (itemForm.atkBonus || 0) + (itemForm.defBonus || 0) + (itemForm.hpBonus || 0) + ((itemForm.speedBonus || 0) * 200);
    const mult = { COMMON: 1, UNCOMMON: 2, RARE: 5, EPIC: 15, LEGENDARY: 50, MYTHIC: 200 };
    suggestedPrice.value = score * (mult[itemForm.rarity] || 1);
    itemForm.basePrice = suggestedPrice.value;
};

const createItem = async () => {
    try {
        calculatePrice(); 
        if (!itemForm.slotType) itemForm.slotType = itemForm.type;
        
        // [FIX LOGIC LIMITED]
        if (distributionType.value === 'SHOP') {
            itemForm.isSystemItem = true;
            itemForm.isLimited = false;
        } else if (distributionType.value === 'LIMITED') {
            itemForm.isSystemItem = false;
            itemForm.isLimited = true; // Backend cần trường này!
        } else {
            // DROP
            itemForm.isSystemItem = false;
            itemForm.isLimited = false;
        }
        
        await axiosClient.post("/admin/item/create", itemForm); 
        notificationStore.showToast("Chế tác thành công!", "success"); 
        adminStore.fetchItems(); 
        showCreateItem.value = false;
    } catch(e) { 
        console.error(e);
        notificationStore.showToast("Lỗi chế tác: " + (e.response?.data?.message || "Kiểm tra lại thông tin"), "error"); 
    }
};

// Existing Actions
const handleGrantGold = async () => {
    try { await adminStore.grantGold(grantGoldForm.username, grantGoldForm.amount); notificationStore.showToast("Đã cấp ngân lượng!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
};

const handleGrantEcho = async () => {
    try { 
        await adminStore.grantEcho(grantEchoForm.username, grantEchoForm.amount); 
        notificationStore.showToast("Đã ban EchoCoin!", "success");
        grantEchoForm.username = ""; grantEchoForm.amount = "";
    } catch(e) { notificationStore.showToast("Lỗi ban thưởng Echo!", "error"); }
};

const handleGrantItem = async () => {
    try { await adminStore.grantItem(grantItemForm.username, grantItemForm.itemId, grantItemForm.quantity); notificationStore.showToast("Đã ban bảo vật!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
};
const handleSendNotification = async () => {
    try { await adminStore.sendNotification(notificationForm); notificationStore.showToast("Đã phát loa!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
};

const openBanModal = (u) => { selectedUser.value = u; banReason.value = ""; showBanModal.value = true; };
const confirmBan = async () => {
    if(!banReason.value) return notificationStore.showToast("Cần lý do trừng phạt!", "warning");
    try { 
        await adminStore.banUser(selectedUser.value.userId, banReason.value); 
        notificationStore.showToast("Đã thi hành án!", "success"); 
        showBanModal.value = false; 
    } catch(e) { notificationStore.showToast("Lỗi ban!", "error"); }
};
const requestUnban = async (u) => {
    if(confirm(`Ân xá cho ${u.username}?`)) {
        try { await adminStore.unbanUser(u.userId); notificationStore.showToast("Đã ân xá!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
    }
};
const requestDeleteUser = async (u) => {
    if(confirm(`Xóa vĩnh viễn ${u.username}?`)) {
        try { await axiosClient.delete(`/admin/user/${u.userId}`); adminStore.fetchUsers(); notificationStore.showToast("Đã xóa!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
    }
};
const requestDeleteItem = async (i) => {
    if(confirm(`Hủy bỏ vật phẩm ${i.name}?`)) {
        try { await axiosClient.delete(`/admin/item/${i.itemId}`); adminStore.fetchItems(); notificationStore.showToast("Đã hủy!", "success"); } catch(e) { notificationStore.showToast("Lỗi!", "error"); }
    }
};

onMounted(() => {
    adminStore.fetchStats();
    adminStore.fetchUsers();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700&family=Noto+Serif+TC:wght@400;700&display=swap');

/* --- GLOBAL LAYOUT --- */
.admin-page {
  min-height: 100vh;
  background-color: #0a0a0a;
  color: #e0e0e0;
  font-family: 'Noto Serif TC', serif;
  position: relative;
  overflow-x: hidden;
}

.bg-layer { position: fixed; inset: 0; z-index: 0; pointer-events: none; }
.bg-layer.base { background: radial-gradient(circle at top, #1a100e, #000000); }
.bg-layer.overlay { 
  background-image: url("https://www.transparenttextures.com/patterns/black-scales.png");
  opacity: 0.3;
}

.admin-container {
  position: relative;
  z-index: 10;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* --- HEADER --- */
.admin-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
}
.glow-text {
  font-family: 'Cinzel', serif;
  font-size: 3.5rem;
  color: #ffca28;
  text-transform: uppercase;
  margin: 0;
  text-shadow: 0 0 10px rgba(255, 202, 40, 0.5), 0 0 20px rgba(255, 0, 0, 0.3);
  letter-spacing: 5px;
  background: -webkit-linear-gradient(#ffd700, #ff8c00);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.subtitle {
  color: #aaa;
  font-size: 1.1rem;
  font-style: italic;
  margin-top: 10px;
  letter-spacing: 1px;
}
.header-decor {
  height: 2px;
  background: linear-gradient(90deg, transparent, #ffca28, transparent);
  width: 50%;
  margin: 20px auto 0;
}

/* --- NAVIGATION --- */
.admin-nav {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}
.nav-item {
  background: rgba(20, 20, 20, 0.8);
  border: 1px solid #444;
  color: #bbb;
  padding: 12px 25px;
  font-size: 1.2rem;
  cursor: pointer;
  position: relative;
  transition: all 0.4s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: 'Noto Serif TC', serif;
  text-transform: uppercase;
  clip-path: polygon(10% 0, 100% 0, 100% 100%, 0 100%, 0 20%);
}
.nav-item:hover, .nav-item.active {
  background: rgba(62, 39, 35, 0.9);
  color: #ffca28;
  border-color: #ffca28;
  box-shadow: 0 0 15px rgba(255, 202, 40, 0.2);
  transform: translateY(-2px);
}
.nav-item.active i { color: #ffca28; }

/* --- SECTIONS & PANELS --- */
.glass-panel {
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 215, 0, 0.15);
  border-radius: 8px;
  padding: 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-bottom: 20px;
}

.table-wrapper {
  max-height: 70vh; 
  overflow-y: auto; 
  position: relative;
  padding: 0;
}

.custom-scroll-panel::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.custom-scroll-panel::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}
.custom-scroll-panel::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 4px;
  border: 1px solid #3e2723;
}
.custom-scroll-panel::-webkit-scrollbar-thumb:hover {
  background: #ffca28;
}

.wuxia-table {
  width: 100%;
  border-collapse: collapse;
}

.wuxia-table th {
  position: sticky;
  top: 0;
  z-index: 2;
  text-align: left;
  padding: 15px;
  color: #ffca28;
  font-family: 'Cinzel', serif;
  border-bottom: 2px solid rgba(255, 215, 0, 0.5);
  text-transform: uppercase;
  letter-spacing: 1px;
  background: rgba(30, 20, 15, 0.98); 
  box-shadow: 0 2px 5px rgba(0,0,0,0.5);
}

.wuxia-table td {
  padding: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  vertical-align: middle;
  color: #ccc;
  transition: background 0.2s;
}
.wuxia-table tr:hover td {
  background: linear-gradient(90deg, rgba(255, 202, 40, 0.05), transparent);
  color: #fff;
}
.id-col { font-family: monospace; color: #666; }
.name-col { font-weight: bold; color: #fff; font-size: 1.1rem; }
.gold-val { color: #ffca28; font-weight: bold; }

/* Stats Cards */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 25px;
  border-left: 4px solid #444;
}
.stat-card.highlight { border-left-color: #ffca28; }
.stat-card.highlight-echo { border-left-color: #26c6da; }

.icon-box {
  width: 80px; height: 80px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 2.5rem;
  background: rgba(0,0,0,0.4);
}
.user { color: #42a5f5; text-shadow: 0 0 10px #42a5f5; }
.item { color: #ab47bc; text-shadow: 0 0 10px #ab47bc; }
.gold { color: #ffca28; text-shadow: 0 0 10px #ffca28; }
.echo { color: #26c6da; text-shadow: 0 0 10px #26c6da; }

.info h3 { margin: 0; color: #888; font-size: 1rem; text-transform: uppercase; letter-spacing: 1px; }
.info .number { font-size: 2.5rem; font-weight: bold; color: #eee; font-family: 'Cinzel', serif; }
.echo-text { color: #26c6da; }

/* Badges */
.badge { padding: 4px 10px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; text-transform: uppercase; }
.badge.user { background: rgba(33, 150, 243, 0.15); color: #64b5f6; border: 1px solid #1e88e5; }
.badge.admin { background: rgba(255, 167, 38, 0.15); color: #ffb74d; border: 1px solid #fb8c00; }
.badge.active { color: #00e676; border: 1px solid #00e676; }
.badge.drop { color: #29b6f6; border: 1px solid #29b6f6; }
.badge.limited { color: #ffca28; border: 1px solid #ffca28; font-weight: bold; text-shadow: 0 0 5px #ffca28; }

.status-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 8px; }
.status-dot.active { background: #00e676; box-shadow: 0 0 8px #00e676; }
.status-dot.banned { background: #ff1744; box-shadow: 0 0 8px #ff1744; }

/* Action Buttons */
.actions-cell { display: flex; gap: 8px; }
.btn-icon {
  width: 40px; height: 40px; border: none; border-radius: 50%; 
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.3s; color: #fff; box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}
.chat { background: linear-gradient(135deg, #42a5f5, #1565c0); }
.ban { background: linear-gradient(135deg, #ef5350, #c62828); }
.unban { background: linear-gradient(135deg, #66bb6a, #2e7d32); }
.delete { background: linear-gradient(135deg, #757575, #424242); }
.btn-icon:hover { transform: translateY(-3px) scale(1.1); filter: brightness(1.2); }

/* --- FORMS & FILTERS --- */
.filter-bar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.search-group { position: relative; flex: 1; max-width: 400px; }
.search-group i { position: absolute; left: 15px; top: 50%; transform: translateY(-50%); color: #ffca28; }
.search-group input { 
  width: 100%; padding: 12px 12px 12px 45px; background: rgba(0,0,0,0.3); 
  border: 1px solid #555; color: #fff; border-radius: 30px; outline: none;
  font-family: inherit;
}
.search-group input:focus { border-color: #ffca28; box-shadow: 0 0 10px rgba(255, 202, 40, 0.2); }
select { background: #222; color: #fff; border: 1px solid #555; padding: 10px 15px; border-radius: 4px; outline: none; }

.simple-form input, .simple-form textarea {
  width: 100%; padding: 12px; background: rgba(0,0,0,0.4); border: 1px solid #555;
  color: #fff; border-radius: 4px; box-sizing: border-box; margin-bottom: 10px; font-family: inherit;
}
.btn-action {
  width: 100%; padding: 12px; border: none; font-weight: bold; cursor: pointer;
  border-radius: 4px; text-transform: uppercase; transition: 0.3s; font-family: 'Cinzel', serif; letter-spacing: 1px;
}
.btn-action.gold { background: linear-gradient(45deg, #FFD700, #FFA000); color: #000; }
.btn-action.cyan { background: linear-gradient(45deg, #26c6da, #00acc1); color: #fff; }
.btn-action.blue { background: linear-gradient(45deg, #42a5f5, #1976d2); color: #fff; }
.btn-action.red { background: linear-gradient(45deg, #ef5350, #c62828); color: #fff; }

.btn-create {
  background: transparent; border: 1px solid #ffca28; color: #ffca28;
  padding: 10px 25px; border-radius: 30px; cursor: pointer; font-weight: bold;
  transition: 0.3s; text-transform: uppercase;
}
.btn-create:hover { background: #ffca28; color: #000; box-shadow: 0 0 15px #ffca28; }

.grid-2-col { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
.divider-line { height: 1px; background: #444; margin: 20px 0; }

/* --- MODAL PHÁN QUYẾT --- */
.modal-backdrop {
  position: fixed; inset: 0; background: rgba(0,0,0,0.85); backdrop-filter: blur(5px);
  z-index: 2000; display: flex; align-items: center; justify-content: center;
}
.modal-content.ban-theme {
  background: #1a0505; border: 2px solid #ef5350; width: 450px; 
  box-shadow: 0 0 50px rgba(239, 83, 80, 0.4); overflow: hidden;
}
.modal-header-danger {
  background: #c62828; color: #fff; padding: 15px; text-align: center;
  font-weight: bold; font-size: 1.2rem; text-transform: uppercase;
}
.modal-body { padding: 25px; }
.target-name { color: #ef5350; font-size: 1.2rem; margin-left: 5px; }
.modal-content textarea { width: 100%; height: 100px; margin-top: 15px; background: #000; border: 1px solid #555; color: #fff; padding: 10px; }
.modal-btns { display: flex; justify-content: flex-end; padding: 15px; gap: 10px; background: rgba(0,0,0,0.2); }
.btn-cancel { background: transparent; color: #aaa; border: 1px solid #555; padding: 8px 20px; cursor: pointer; }
.btn-confirm-ban { background: #ef5350; color: #fff; border: none; padding: 8px 25px; cursor: pointer; font-weight: bold; }

/* --- RARITY COLORS --- */
.rarity-COMMON { color: #bdbdbd; }
.rarity-UNCOMMON { color: #4caf50; }
.rarity-RARE { color: #42a5f5; }
.rarity-EPIC { color: #ab47bc; }
.rarity-LEGENDARY { color: #ffca28; text-shadow: 0 0 5px #ffca28; }
.rarity-MYTHIC { color: #ef5350; text-shadow: 0 0 8px #ef5350; font-weight: bold; }

.mini-icon { width: 24px; height: 24px; vertical-align: middle; margin-right: 5px; }

/* --- WIZARD STYLES (NEW) --- */
.wizard-container {
    background: rgba(15, 10, 8, 0.95);
    border: 1px solid #5d4037;
    box-shadow: 0 0 30px rgba(0,0,0,0.8);
    border-radius: 12px;
    padding: 30px;
    min-height: 500px;
    display: flex; flex-direction: column;
}

.wizard-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;
}
.panel-title-small { font-family: 'Cinzel', serif; color: #ffca28; font-size: 1.4rem; margin: 0; }
.step-counter { color: #888; font-family: monospace; }

.progress-container {
    height: 4px; background: #333; border-radius: 2px; margin-bottom: 30px; overflow: hidden;
}
.progress-bar {
    height: 100%; background: linear-gradient(90deg, #ff6f00, #ffca28);
    transition: width 0.4s ease; box-shadow: 0 0 10px #ffca28;
}

/* Steps Animation */
.wizard-step { animation: fadeInUp 0.4s ease; flex: 1; display: flex; flex-direction: column; }
.step-label { font-size: 1.2rem; color: #e0e0e0; margin-bottom: 20px; border-left: 3px solid #ffca28; padding-left: 10px; font-weight: bold; }

/* TYPE GRID */
.type-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.type-card {
    background: #1a1a1a; padding: 25px; text-align: center; border-radius: 8px;
    border: 1px solid #444; cursor: pointer; position: relative; overflow: hidden;
    transition: all 0.3s;
}
.type-card:hover, .type-card.selected {
    border-color: #ffca28; transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0,0,0,0.5);
}
.type-card.selected i, .type-card.selected span { color: #ffca28; }
.type-card i { font-size: 2.5rem; margin-bottom: 10px; color: #888; transition: 0.3s; }
.card-glow {
    position: absolute; inset: 0; background: radial-gradient(circle, rgba(255,202,40,0.1), transparent);
    opacity: 0; transition: 0.3s;
}
.type-card:hover .card-glow { opacity: 1; }

/* INPUTS */
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.full-width { grid-column: span 2; width: 100%; }
.input-group label { display: block; color: #aaa; margin-bottom: 8px; font-size: 0.9rem; }
.input-group input, .input-group textarea {
    width: 100%; background: rgba(0,0,0,0.3); border: 1px solid #444; color: #fff;
    padding: 12px; border-radius: 4px; font-family: inherit; transition: 0.3s; box-sizing: border-box;
}
.input-group input:focus, .input-group textarea:focus {
    border-color: #ffca28; box-shadow: 0 0 10px rgba(255,202,40,0.1); outline: none;
}

/* ASSETS */
.assets-wrapper { padding-bottom: 10px; overflow-x: auto; }
.assets-grid { display: flex; gap: 15px; }
.asset-item {
    min-width: 90px; height: 90px; border: 2px solid #333; border-radius: 8px;
    background: #000; cursor: pointer; position: relative; transition: 0.3s;
}
.asset-item img { width: 100%; height: 100%; object-fit: contain; padding: 5px; box-sizing: border-box; }
.asset-item.selected { border-color: #00e676; box-shadow: 0 0 15px rgba(0,230,118,0.3); }
.asset-check {
    position: absolute; top: -8px; right: -8px; background: #00e676; color: #000;
    width: 20px; height: 20px; border-radius: 50%; font-size: 12px;
    display: flex; align-items: center; justify-content: center; opacity: 0; transform: scale(0); transition: 0.3s;
}
.asset-item.selected .asset-check { opacity: 1; transform: scale(1); }

/* RARITY */
.rarity-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.rarity-card {
    padding: 15px; border: 1px solid #444; border-radius: 6px; cursor: pointer;
    text-align: center; background: #181818; transition: 0.2s;
}
.rarity-card span { display: block; font-weight: bold; margin-bottom: 5px; }
.rarity-card small { color: #666; font-size: 0.8rem; }
.rarity-card.COMMON.selected { border-color: #bdbdbd; box-shadow: 0 0 10px #bdbdbd; }
.rarity-card.UNCOMMON.selected { border-color: #4caf50; box-shadow: 0 0 10px #4caf50; }
.rarity-card.RARE.selected { border-color: #42a5f5; box-shadow: 0 0 10px #42a5f5; }
.rarity-card.EPIC.selected { border-color: #ab47bc; box-shadow: 0 0 10px #ab47bc; }
.rarity-card.LEGENDARY.selected { border-color: #ffca28; box-shadow: 0 0 10px #ffca28; }
.rarity-card.MYTHIC.selected { border-color: #ef5350; box-shadow: 0 0 10px #ef5350; }

/* STATS */
.stat-type-selector { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 10px; }
.stat-chip {
    padding: 8px 15px; background: #333; border-radius: 20px; cursor: pointer;
    border: 1px solid #555; transition: 0.2s; color: #aaa;
}
.stat-chip.active {
    background: #ffca28; color: #000; border-color: #ffca28; font-weight: bold;
    box-shadow: 0 0 10px rgba(255,202,40,0.3);
}
.big-input { font-size: 1.5rem; color: #00e676 !important; font-weight: bold; text-align: center; }
.sub-stats-container { margin-top: 15px; }
.sub-stat-row { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; }
.row-index { color: #555; font-family: monospace; }
.sub-stat-select { width: 150px; }

/* DISTRIBUTION */
.distribution-box { margin-bottom: 20px; }
.dist-options { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 10px; }
.dist-card { background: #1a1a1a; border: 1px solid #444; border-radius: 8px; padding: 15px; cursor: pointer; display: flex; align-items: center; gap: 10px; transition: 0.3s; }
.dist-card:hover { background: #222; transform: translateY(-2px); }
.dist-card.active { border-color: #00e676; background: rgba(0, 230, 118, 0.1); }
.dist-card.active i { color: #00e676; }
.dist-card.limited.active { border-color: #ffca28; background: rgba(255, 202, 40, 0.1); }
.dist-card.limited.active i { color: #ffca28; }
.dist-card i { font-size: 1.5rem; color: #666; }
.dist-card strong { display: block; color: #eee; }
.dist-card small { color: #888; font-size: 0.8rem; }

/* ACTIONS */
.wizard-actions { margin-top: auto; padding-top: 20px; border-top: 1px solid #333; display: flex; justify-content: space-between; }
.btn-next, .btn-submit-final {
    background: linear-gradient(45deg, #FFD700, #FFA000); color: #000; border: none;
    padding: 10px 30px; border-radius: 30px; font-weight: bold; cursor: pointer;
    box-shadow: 0 4px 15px rgba(255,160,0,0.3); transition: 0.3s;
}
.btn-next:disabled { background: #444; color: #888; box-shadow: none; cursor: not-allowed; }
.btn-next:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255,160,0,0.5); }
.btn-back { background: transparent; border: 1px solid #555; color: #aaa; padding: 8px 20px; border-radius: 30px; cursor: pointer; transition: 0.3s; }
.btn-back:hover { border-color: #fff; color: #fff; }

/* CHECKBOX & PREVIEW */
.checkbox-container {
    display: flex; gap: 15px; background: #222; padding: 15px; border-radius: 8px;
    margin-top: 20px; cursor: pointer; border: 1px solid #444;
}
.checkbox-container input { width: 20px; height: 20px; accent-color: #ffca28; margin-top: 3px; }
.label-content strong { color: #fff; }
.label-content p { color: #888; margin: 5px 0 0; font-size: 0.9rem; }
.warning-text { color: #ef5350 !important; }

.summary-card { background: #121212; padding: 15px; border-radius: 8px; border: 1px solid #333; margin-bottom: 20px; }
.item-preview { display: flex; gap: 15px; align-items: center; }
.preview-img { width: 64px; height: 64px; border: 1px solid #444; border-radius: 4px; background: #000; object-fit: contain;}
.preview-info h4 { margin: 0; font-size: 1.2rem; }
.preview-info p { margin: 5px 0; color: #888; font-size: 0.9rem; }
.preview-stats { color: #00e676; font-family: monospace; }
.pricing-box { display: flex; justify-content: space-between; align-items: center; background: #1a1a1a; padding: 15px; border-radius: 8px; margin-bottom: 15px; border-left: 4px solid #ffca28; }
.suggested-price { color: #00e676; font-size: 1.5rem; margin-left: 10px; }
.price-input { width: 150px !important; text-align: right; color: #ffca28 !important; font-weight: bold; }

/* --- ANIMATIONS --- */
.fade-in { animation: fadeIn 0.5s ease; }
.fade-in-down { animation: fadeInDown 0.6s ease; }
.fade-in-up { animation: fadeInUp 0.6s ease; }
.zoom-in { animation: zoomIn 0.3s ease; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes zoomIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>