<template>
  <div class="page-container admin-view dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="admin-wrapper custom-scroll">
      <div class="admin-header">
        <div class="header-decor left"></div>
        <h2 class="header-title">QUẢN TRỊ THIÊN PHỦ</h2>
        <div class="header-decor right"></div>

        <div class="tabs-scroll-wrapper">
          <div class="wuxia-tabs">
            <button
              :class="{ active: activeTab === 'stats' }"
              @click="switchTab('stats')"
              class="tab-btn"
            >
              <i class="fas fa-chart-line"></i> THỐNG KÊ
            </button>
            <div class="tab-divider"></div>
            <button
              :class="{ active: activeTab === 'users' }"
              @click="switchTab('users')"
              class="tab-btn"
            >
              <i class="fas fa-users"></i> NHÂN SĨ
            </button>
            <div class="tab-divider"></div>
            <button
              :class="{ active: activeTab === 'items' }"
              @click="switchTab('items')"
              class="tab-btn"
            >
              <i class="fas fa-khanda"></i> KHO TÀNG
            </button>
            <div class="tab-divider"></div>
            <button
              :class="{ active: activeTab === 'grant' }"
              @click="switchTab('grant')"
              class="tab-btn"
            >
              <i class="fas fa-gift"></i> BAN THƯỞNG
            </button>
            <div class="tab-divider"></div>
            <button
              :class="{ active: activeTab === 'notify' }"
              @click="switchTab('notify')"
              class="tab-btn"
            >
              <i class="fas fa-scroll"></i> CÁO THỊ
            </button>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'stats'" class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon"><i class="fas fa-users"></i></div>
          <div class="stat-info">
            <h3>TỔNG NHÂN SĨ</h3>
            <p class="num">{{ adminStore.stats.totalUsers || 0 }}</p>
          </div>
          <div class="seal-bg"><i class="fas fa-user"></i></div>
        </div>
        <div class="stat-card">
          <div class="stat-icon"><i class="fas fa-box-open"></i></div>
          <div class="stat-info">
            <h3>VẬT PHẨM</h3>
            <p class="num">{{ adminStore.stats.totalItems || 0 }}</p>
          </div>
          <div class="seal-bg"><i class="fas fa-box"></i></div>
        </div>
        <div class="stat-card gold-card">
          <div class="stat-icon"><i class="fas fa-coins"></i></div>
          <div class="stat-info">
            <h3>NGÂN LƯỢNG</h3>
            <p class="num gold-text">
              {{ formatNumber(adminStore.stats.totalGold || 0) }}
            </p>
          </div>
          <div class="seal-bg"><i class="fas fa-coins"></i></div>
        </div>
      </div>

      <div v-if="activeTab === 'users'" class="content-panel">
        <div class="panel-header"><h3>DANH SÁCH NHÂN SĨ</h3></div>
        
        <div class="filter-box">
          <div class="filter-row">
            <div class="search-input-wrapper">
              <i class="fas fa-search search-icon"></i>
              <input
                v-model="userFilters.search"
                placeholder="Tìm kiếm theo tên, email..."
                class="dark-input search-input"
              />
            </div>
            <select v-model="userFilters.role" class="dark-input filter-select">
              <option value="">Tất cả vai trò</option>
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
            <select v-model="userFilters.status" class="dark-input filter-select">
              <option value="">Tất cả trạng thái</option>
              <option value="active">Hoạt động</option>
              <option value="banned">Đã giam</option>
            </select>
            <button @click="resetUserFilters" class="btn-wood small reset-btn">
              <i class="fas fa-redo"></i> Reset
            </button>
          </div>
          <div class="result-count">
            Tìm thấy: <span class="highlight-text">{{ filteredUsers.length }}</span> / {{ adminStore.users?.length || 0 }}
          </div>
        </div>

        <div class="table-responsive custom-scroll">
          <table class="dark-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>DANH TÍNH</th>
                <th>THƯ TÍN</th>
                <th>VAI TRÒ</th>
                <th>TRẠNG THÁI</th>
                <th>XỬ LÝ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="filteredUsers.length === 0">
                <td colspan="6" class="empty-state">
                  <i class="fas fa-search"></i>
                  <p>Không tìm thấy nhân sĩ phù hợp</p>
                </td>
              </tr>
              <tr v-for="u in filteredUsers" :key="u.userId">
                <td class="dim-text">#{{ u.userId }}</td>
                <td class="highlight-text">{{ u.username }}</td>
                <td>{{ u.email }}</td>
                <td>
                  <span class="role-tag" :class="'role-' + u.role.toLowerCase()">{{ u.role }}</span>
                </td>
                <td>
                  <span v-if="u.isActive" class="status-tag active">HOẠT ĐỘNG</span>
                  <span v-else class="status-tag banned">ĐÃ GIAM</span>
                </td>
                <td class="action-cell">
                  <button
                    v-if="u.isActive"
                    @click="openBanModal(u)"
                    class="btn-icon red"
                    title="Giam cầm"
                  >
                    <i class="fas fa-gavel"></i>
                  </button>
                  <button
                    v-else
                    @click="requestUnban(u)"
                    class="btn-icon green"
                    title="Phóng thích"
                  >
                    <i class="fas fa-lock-open"></i>
                  </button>
                  <button
                    @click="requestDeleteUser(u)"
                    class="btn-icon gray"
                    title="Xóa bỏ"
                  >
                    <i class="fas fa-skull"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="activeTab === 'items'" class="content-panel">
        <div class="panel-header flex-between">
          <h3>KHO TÀNG VẬT PHẨM</h3>
          <button
            @click="showCreateItem = !showCreateItem"
            class="btn-wood small"
          >
            <i class="fas fa-plus"></i> CHẾ TÁC
          </button>
        </div>

        <transition name="slide-down">
          <div v-if="showCreateItem" class="create-form-box">
            <form @submit.prevent="createItem" class="ancient-form">
              <div class="form-grid">
                <input
                  v-model="itemForm.name"
                  placeholder="Tên vật phẩm..."
                  class="dark-input"
                  required
                />
                <input
                  v-model="itemForm.description"
                  placeholder="Mô tả..."
                  class="dark-input"
                />
                <select v-model="itemForm.type" class="dark-input">
                  <option value="WEAPON">Binh Khí</option>
                  <option value="ARMOR">Y Phục</option>
                  <option value="HELMET">Mũ</option>
                  <option value="BOOTS">Giày</option>
                  <option value="RING">Nhẫn</option>
                  <option value="NECKLACE">Dây Chuyền</option>
                  <option value="CONSUMABLE">Tiêu Hao</option>
                  <option value="MATERIAL">Nguyên Liệu</option>
                </select>
                <select v-model="itemForm.rarity" class="dark-input">
                  <option value="C">Thường (C)</option>
                  <option value="B">Hiếm (B)</option>
                  <option value="A">Sử Thi (A)</option>
                  <option value="S">Truyền Thuyết (S)</option>
                </select>
                <input
                  v-model.number="itemForm.basePrice"
                  type="number"
                  placeholder="Giá bán"
                  class="dark-input"
                />
              </div>
              <div class="form-row mt-10">
                <input
                  v-model="itemForm.imageUrl"
                  placeholder="URL Hình ảnh..."
                  class="dark-input full-width"
                />
              </div>
              <div class="form-footer">
                <label class="check-label"
                  ><input type="checkbox" v-model="itemForm.isSystemItem" />
                  <span>Hàng Shop Vô Hạn?</span></label
                >
                <button type="submit" class="btn-wood primary">HOÀN TẤT</button>
              </div>
            </form>
          </div>
        </transition>

        <div class="filter-box mt-20">
          <div class="filter-row">
            <div class="search-input-wrapper">
              <i class="fas fa-search search-icon"></i>
              <input
                v-model="itemFilters.search"
                placeholder="Tìm kiếm vật phẩm..."
                class="dark-input search-input"
              />
            </div>
            <select v-model="itemFilters.type" class="dark-input filter-select">
              <option value="">Tất cả loại</option>
              <option value="WEAPON">Binh Khí</option>
              <option value="ARMOR">Y Phục</option>
              <option value="HELMET">Mũ</option>
              <option value="BOOTS">Giày</option>
              <option value="RING">Nhẫn</option>
              <option value="NECKLACE">Dây Chuyền</option>
              <option value="CONSUMABLE">Tiêu Hao</option>
              <option value="MATERIAL">Nguyên Liệu</option>
            </select>
            <select v-model="itemFilters.rarity" class="dark-input filter-select">
              <option value="">Tất cả phẩm cấp</option>
              <option value="C">Thường (C)</option>
              <option value="B">Hiếm (B)</option>
              <option value="A">Sử Thi (A)</option>
              <option value="S">Truyền Thuyết (S)</option>
            </select>
            <button @click="resetItemFilters" class="btn-wood small reset-btn">
              <i class="fas fa-redo"></i> Reset
            </button>
          </div>
          <div class="result-count">
            Tìm thấy: <span class="highlight-text">{{ filteredItems.length }}</span> / {{ adminStore.items?.length || 0 }}
          </div>
        </div>

        <div class="table-responsive custom-scroll mt-20">
          <table class="dark-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>TÊN VẬT PHẨM</th>
                <th>LOẠI</th>
                <th>PHẨM CẤP</th>
                <th>GIÁ</th>
                <th>XÓA</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="filteredItems.length === 0">
                <td colspan="6" class="empty-state">
                  <i class="fas fa-search"></i>
                  <p>Không tìm thấy vật phẩm phù hợp</p>
                </td>
              </tr>
              <tr v-for="i in filteredItems" :key="i.itemId">
                <td class="dim-text">#{{ i.itemId }}</td>
                <td class="highlight-text">{{ i.name }}</td>
                <td>{{ i.type }}</td>
                <td :class="'rarity-' + i.rarity">{{ i.rarity }}</td>
                <td class="gold-text">{{ i.basePrice }}</td>
                <td>
                  <button @click="requestDeleteItem(i)" class="btn-icon red">
                    <i class="fas fa-trash-alt"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="activeTab === 'grant'" class="content-panel">
        <h3 class="panel-header">BAN THƯỞNG</h3>
        <div class="grant-grid">
          <div class="grant-box">
            <h4 class="sub-title">
              <i class="fas fa-coins"></i> CẤP NGÂN LƯỢNG
            </h4>
            <form @submit.prevent="handleGrantGold" class="ancient-form">
              <input
                v-model="grantGoldForm.username"
                placeholder="Danh tính (username)"
                class="dark-input mb-10"
                required
              />
              <input
                v-model.number="grantGoldForm.amount"
                type="number"
                placeholder="Số lượng"
                class="dark-input mb-10"
                min="1"
                required
              />
              <button type="submit" class="btn-wood gold full-width">
                BAN THƯỞNG
              </button>
            </form>
          </div>
          <div class="grant-box">
            <h4 class="sub-title"><i class="fas fa-box"></i> CẤP VẬT PHẨM</h4>
            <form @submit.prevent="handleGrantItem" class="ancient-form">
              <input
                v-model="grantItemForm.username"
                placeholder="Danh tính (username)"
                class="dark-input mb-10"
                required
              />
              <select
                v-model.number="grantItemForm.itemId"
                class="dark-input mb-10"
                required
              >
                <option :value="0" disabled>Chọn vật phẩm...</option>
                <option
                  v-for="item in itemOptions"
                  :key="item.id"
                  :value="item.id"
                >
                  #{{ item.id }} - {{ item.name }}
                </option>
              </select>
              <input
                v-model.number="grantItemForm.quantity"
                type="number"
                placeholder="Số lượng"
                class="dark-input mb-10"
                min="1"
                required
              />
              <button type="submit" class="btn-wood primary full-width">
                BAN THƯỞNG
              </button>
            </form>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'notify'" class="content-panel">
        <h3 class="panel-header">SOẠN CÁO THỊ</h3>
        <div class="notify-box">
          <form @submit.prevent="handleSendNotification" class="ancient-form">
            <div class="form-grid">
              <input
                v-model="notificationForm.title"
                placeholder="Tiêu đề..."
                class="dark-input"
                required
              />
              <select v-model="notificationForm.type" class="dark-input">
                <option value="INFO">Thông Tin</option>
                <option value="SUCCESS">Tin Mừng</option>
                <option value="WARNING">Cảnh Báo</option>
                <option value="REWARD">Phần Thưởng</option>
              </select>
            </div>
            <input
              v-model="notificationForm.recipientUsername"
              placeholder="Người nhận (Để trống = Toàn server)"
              class="dark-input mt-10"
            />
            <textarea
              v-model="notificationForm.message"
              placeholder="Nội dung cáo thị..."
              class="dark-input text-area mt-10"
              rows="5"
              required
            ></textarea>
            <button type="submit" class="btn-wood primary full-width mt-10">
              <i class="fas fa-bullhorn"></i> PHÁT LOA TOÀN GIANG HỒ
            </button>
          </form>
        </div>
      </div>
    </div>

    <div
      v-if="showBanModal"
      class="modal-overlay"
      @click.self="showBanModal = false"
    >
      <div class="dark-modal">
        <div class="modal-header">
          <span class="decor">❖</span> PHÁN QUYẾT <span class="decor">❖</span>
        </div>
        <div class="modal-body">
          <p class="target-user">
            Tội nhân: <span>{{ selectedUser?.username }}</span>
          </p>
          <textarea
            v-model="banReason"
            placeholder="Lý do trừng phạt..."
            class="dark-input text-area"
            rows="3"
          ></textarea>
          <div class="modal-footer">
            <button @click="showBanModal = false" class="btn-wood">HỦY</button>
            <button @click="confirmBan" class="btn-wood red">THI HÀNH</button>
          </div>
        </div>
      </div>
    </div>

    <transition name="pop-up">
      <div
        v-if="confirmModal.visible"
        class="modal-overlay"
        @click.self="closeConfirmModal"
      >
        <div class="dark-modal small">
          <div class="modal-header danger">
            <i class="fas fa-exclamation-triangle"></i> CẢNH BÁO
          </div>
          <div class="modal-body center-text">
            <h3>{{ confirmModal.title }}</h3>
            <p>{{ confirmModal.message }}</p>
          </div>
          <div class="modal-footer">
            <button @click="closeConfirmModal" class="btn-wood">KHÔNG</button>
            <button @click="executeConfirm" class="btn-wood red">ĐỒNG Ý</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useAdminStore } from "../stores/adminStore";
import { useNotificationStore } from "../stores/notificationStore";
import axiosClient from "../api/axiosClient";

const adminStore = useAdminStore();
const notificationStore = useNotificationStore();

const activeTab = ref("stats");
const showCreateItem = ref(false);
const showBanModal = ref(false);
const selectedUser = ref(null);
const banReason = ref("");

// Filter states
const userFilters = reactive({
  search: "",
  role: "",
  status: ""
});

const itemFilters = reactive({
  search: "",
  type: "",
  rarity: ""
});

const confirmModal = reactive({
  visible: false,
  title: "",
  message: "",
  onConfirm: null,
});

const itemForm = reactive({
  name: "",
  description: "",
  type: "WEAPON",
  rarity: "C",
  basePrice: 100,
  imageUrl: "",
  isSystemItem: false,
});
const grantGoldForm = reactive({ username: "", amount: 1000 });
const grantItemForm = reactive({ username: "", itemId: 0, quantity: 1 });
const notificationForm = reactive({
  title: "",
  message: "",
  type: "INFO",
  recipientUsername: "",
});

// Computed filters (AN TOÀN)
const filteredUsers = computed(() => {
  let users = adminStore.users || [];
  
  if (userFilters.search) {
    const search = userFilters.search.toLowerCase();
    users = users.filter(u => 
      (u.username || "").toLowerCase().includes(search) ||
      (u.email || "").toLowerCase().includes(search) ||
      (u.userId || "").toString().includes(search)
    );
  }
  
  if (userFilters.role) {
    users = users.filter(u => u.role === userFilters.role);
  }
  
  if (userFilters.status === "active") {
    users = users.filter(u => u.isActive === true);
  } else if (userFilters.status === "banned") {
    users = users.filter(u => u.isActive === false);
  }
  
  return users;
});

const filteredItems = computed(() => {
  let items = adminStore.items || [];
  
  if (itemFilters.search) {
    const search = itemFilters.search.toLowerCase();
    items = items.filter(i => 
      (i.name || "").toLowerCase().includes(search) ||
      (i.description || "").toLowerCase().includes(search) ||
      (i.itemId || "").toString().includes(search)
    );
  }
  
  if (itemFilters.type) {
    items = items.filter(i => i.type === itemFilters.type);
  }
  
  if (itemFilters.rarity) {
    items = items.filter(i => i.rarity === itemFilters.rarity);
  }
  
  return items;
});

const itemOptions = computed(() =>
  (adminStore.items || []).map((i) => ({ id: i.itemId, name: i.name })),
);

const formatNumber = (n) => Number(n).toLocaleString();

const switchTab = (tab) => {
  activeTab.value = tab;
  if (tab === "stats") adminStore.fetchStats();
  if (tab === "users") adminStore.fetchUsers();
  if (tab === "items" || tab === "grant") adminStore.fetchItems();
};

const resetUserFilters = () => {
  userFilters.search = "";
  userFilters.role = "";
  userFilters.status = "";
};

const resetItemFilters = () => {
  itemFilters.search = "";
  itemFilters.type = "";
  itemFilters.rarity = "";
};

const showConfirm = (title, msg, action) => {
  confirmModal.title = title;
  confirmModal.message = msg;
  confirmModal.onConfirm = action;
  confirmModal.visible = true;
};
const closeConfirmModal = () => {
  confirmModal.visible = false;
  confirmModal.onConfirm = null;
};
const executeConfirm = () => {
  if (confirmModal.onConfirm) confirmModal.onConfirm();
  closeConfirmModal();
};

const createItem = async () => {
  try {
    await axiosClient.post("/admin/item/create", itemForm);
    notificationStore.showToast("Chế tác vật phẩm thành công!", "success");
    adminStore.fetchItems();
    showCreateItem.value = false;
  } catch (e) {
    notificationStore.showToast(
      e.response?.data?.message || "Lỗi chế tác",
      "error",
    );
  }
};

const handleGrantGold = async () => {
  try {
    await adminStore.grantGold(grantGoldForm);
    notificationStore.showToast(
      `Đã ban ${grantGoldForm.amount} vàng cho ${grantGoldForm.username}`,
      "success",
    );
    grantGoldForm.amount = 1000;
  } catch (e) {
    notificationStore.showToast("Lỗi khi cấp vàng", "error");
  }
};

const handleGrantItem = async () => {
  try {
    await adminStore.grantItem(grantItemForm);
    notificationStore.showToast("Gửi vật phẩm thành công!", "success");
    grantItemForm.itemId = 0;
  } catch (e) {
    notificationStore.showToast("Lỗi khi cấp vật phẩm", "error");
  }
};

const handleSendNotification = async () => {
  try {
    await adminStore.sendNotification(notificationForm);
    notificationStore.showToast("Đã phát loa thông báo!", "success");
    notificationForm.title = "";
    notificationForm.message = "";
  } catch (e) {
    notificationStore.showToast("Lỗi khi gửi thông báo", "error");
  }
};

const openBanModal = (u) => {
  selectedUser.value = u;
  banReason.value = "";
  showBanModal.value = true;
};
const confirmBan = async () => {
  if (!banReason.value)
    return notificationStore.showToast("Cần lý do trừng phạt!", "warning");
  try {
    await adminStore.banUser(selectedUser.value.userId, banReason.value);
    notificationStore.showToast("Đã thi hành án!", "success");
    showBanModal.value = false;
  } catch (e) {
    notificationStore.showToast("Lỗi khi ban user", "error");
  }
};

const requestUnban = (u) => {
  showConfirm(
    "XÁC NHẬN ÂN XÁ",
    `Phóng thích nhân sĩ ${u.username}?`,
    async () => {
      try {
        await adminStore.unbanUser(u.userId);
        notificationStore.showToast("Đã ân xá thành công!", "success");
      } catch (e) {
        notificationStore.showToast("Lỗi ân xá", "error");
      }
    },
  );
};

const requestDeleteUser = (u) => {
  showConfirm(
    "TRẢM QUYẾT",
    `Xóa vĩnh viễn nhân sĩ ${u.username}? Hành động không thể hoàn tác!`,
    async () => {
      try {
        await axiosClient.delete(`/admin/user/${u.userId}`);
        await adminStore.fetchUsers();
        notificationStore.showToast("Đã xóa nhân sĩ!", "success");
      } catch (e) {
        notificationStore.showToast("Lỗi khi xóa", "error");
      }
    },
  );
};

const requestDeleteItem = (i) => {
  showConfirm(
    "HỦY VẬT PHẨM",
    `Xóa vật phẩm ${i.name} khỏi hệ thống?`,
    async () => {
      try {
        await axiosClient.delete(`/admin/item/${i.itemId}`);
        await adminStore.fetchItems();
        notificationStore.showToast("Đã hủy vật phẩm!", "success");
      } catch (e) {
        notificationStore.showToast(
          "Vật phẩm đang được sử dụng, không thể xóa!",
          "error",
        );
      }
    },
  );
};

// LOAD DỮ LIỆU BAN ĐẦU
onMounted(() => {
  adminStore.fetchStats();
  adminStore.fetchUsers(); // QUAN TRỌNG: Load users ngay
  adminStore.fetchItems(); // QUAN TRỌNG: Load items ngay
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --panel-bg: rgba(30, 20, 15, 0.95);
  --gold: #ffecb3;
  --red-seal: #b71c1c;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
}
.dark-theme {
  min-height: 100vh;
  background-color: #212121;
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
.admin-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  height: 100vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.admin-header {
  text-align: center;
  margin-bottom: 25px;
  background: var(--panel-bg);
  border-top: 4px solid var(--wood-light);
  border-bottom: 4px solid var(--wood-light);
  padding: 20px;
  position: relative;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  flex-shrink: 0;
}
.header-title {
  font-size: 2.2rem;
  color: var(--gold);
  margin: 0 0 15px 0;
  text-transform: uppercase;
  letter-spacing: 3px;
  font-weight: 900;
  text-shadow: 0 0 10px rgba(255, 236, 179, 0.3);
}
.header-decor {
  width: 60px;
  height: 2px;
  background: var(--gold);
  position: absolute;
  top: 35px;
}
.left {
  left: 15%;
}
.right {
  right: 15%;
}
.tabs-scroll-wrapper {
  overflow-x: auto;
  padding-bottom: 5px;
}
.wuxia-tabs {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  min-width: max-content;
}
.tab-btn {
  background: transparent;
  border: none;
  color: var(--text-dim);
  padding: 8px 15px;
  font-family: inherit;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
  border-bottom: 2px solid transparent;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
.tab-btn:hover {
  color: var(--gold);
}
.tab-btn.active {
  color: var(--gold);
  border-bottom-color: var(--gold);
  text-shadow: 0 0 5px rgba(255, 236, 179, 0.4);
}
.tab-divider {
  width: 1px;
  height: 15px;
  background: #555;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  background: var(--panel-bg);
  border: 1px solid var(--wood-light);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.stat-icon {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: var(--gold);
  z-index: 2;
}
.stat-info h3 {
  margin: 0;
  font-size: 0.9rem;
  color: #aaa;
  letter-spacing: 1px;
}
.stat-info .num {
  margin: 5px 0 0;
  font-size: 2rem;
  font-weight: bold;
  color: #fff;
}
.gold-text {
  color: var(--gold) !important;
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}
.seal-bg {
  position: absolute;
  right: -10px;
  bottom: -15px;
  font-size: 5rem;
  color: rgba(255, 255, 255, 0.03);
  transform: rotate(-15deg);
  pointer-events: none;
}
.content-panel {
  background: var(--panel-bg);
  border: 1px solid var(--wood-light);
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}
.panel-header {
  border-bottom: 1px solid var(--wood-light);
  padding-bottom: 15px;
  margin-bottom: 20px;
  color: var(--gold);
  font-size: 1.2rem;
  font-weight: bold;
  display: flex;
  align-items: center;
}
.flex-between {
  justify-content: space-between;
}

/* Filter Box Styles */
.filter-box {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--wood-light);
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
}
.filter-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
.search-input-wrapper {
  position: relative;
  flex: 1;
  min-width: 250px;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--gold);
  pointer-events: none;
}
.search-input {
  padding-left: 40px !important;
}
.filter-select {
  min-width: 150px;
}
.reset-btn {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: #666 !important;
  color: #ccc !important;
}
.reset-btn:hover {
  background: rgba(255, 255, 255, 0.15) !important;
  color: #fff !important;
}
.result-count {
  margin-top: 10px;
  font-size: 0.9rem;
  color: var(--text-dim);
  text-align: right;
}

.table-responsive {
  overflow-x: auto;
}
.dark-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}
.dark-table th {
  background: rgba(0, 0, 0, 0.3);
  color: var(--gold);
  padding: 12px;
  text-align: left;
  font-weight: bold;
  border-bottom: 2px solid var(--wood-light);
}
.dark-table td {
  padding: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: var(--text-light);
}
.dark-table tr:hover td {
  background: rgba(255, 255, 255, 0.02);
}
.empty-state {
  text-align: center;
  padding: 40px !important;
  color: var(--text-dim);
}
.empty-state i {
  font-size: 3rem;
  margin-bottom: 10px;
  opacity: 0.3;
  display: block;
}
.empty-state p {
  margin: 0;
  font-size: 1.1rem;
}
.dim-text {
  color: #777;
  font-size: 0.9rem;
}
.highlight-text {
  color: #fff;
  font-weight: bold;
}
.role-tag {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}
.role-tag.role-admin {
  background: rgba(255, 193, 7, 0.2);
  color: #ffc107;
  border: 1px solid #ffc107;
}
.role-tag.role-user {
  background: rgba(33, 150, 243, 0.2);
  color: #2196f3;
  border: 1px solid #2196f3;
}
.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}
.status-tag.active {
  color: #4caf50;
  border: 1px solid #4caf50;
}
.status-tag.banned {
  color: #f44336;
  border: 1px solid #f44336;
}
.action-cell {
  display: flex;
  gap: 5px;
}
.btn-icon {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 1.1rem;
  padding: 5px;
  transition: 0.2s;
}
.btn-icon.red {
  color: #e57373;
}
.btn-icon.green {
  color: #81c784;
}
.btn-icon.gray {
  color: #9e9e9e;
}
.btn-icon:hover {
  transform: scale(1.2);
}
.create-form-box {
  background: rgba(0, 0, 0, 0.2);
  border: 1px dashed var(--wood-light);
  padding: 20px;
  margin-bottom: 20px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}
.dark-input {
  background: #121212;
  border: 1px solid #444;
  color: #fff;
  padding: 10px;
  width: 100%;
  box-sizing: border-box;
  font-family: inherit;
}
.dark-input:focus {
  border-color: var(--gold);
  outline: none;
}
.text-area {
  resize: vertical;
}
.mt-10 {
  margin-top: 10px;
}
.mt-20 {
  margin-top: 20px;
}
.mb-10 {
  margin-bottom: 10px;
}
.full-width {
  width: 100%;
}
.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
}
.check-label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #aaa;
}
.btn-wood {
  background: var(--wood-light);
  border: 1px solid var(--gold);
  color: var(--gold);
  padding: 10px 20px;
  font-weight: bold;
  cursor: pointer;
  font-family: inherit;
  transition: 0.2s;
}
.btn-wood:hover {
  background: var(--gold);
  color: var(--wood-dark);
}
.btn-wood.primary {
  background: var(--red-seal);
  color: #fff;
  border: none;
}
.btn-wood.primary:hover {
  background: #d32f2f;
}
.btn-wood.small {
  padding: 5px 15px;
  font-size: 0.9rem;
}
.btn-wood.gold {
  background: var(--gold);
  color: var(--wood-dark);
}
.btn-wood.red {
  background: var(--red-seal);
  color: #fff;
  border: none;
}
.grant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}
.grant-box {
  background: rgba(0, 0, 0, 0.2);
  padding: 20px;
  border: 1px solid #444;
}
.sub-title {
  margin: 0 0 15px 0;
  color: #ccc;
  border-bottom: 1px solid #444;
  padding-bottom: 5px;
}
.notify-box {
  max-width: 600px;
  margin: 0 auto;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(2px);
}
.dark-modal {
  width: 400px;
  background: var(--wood-dark);
  border: 2px solid var(--red-seal);
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.9);
}
.modal-header {
  background: var(--red-seal);
  color: #fff;
  padding: 10px;
  text-align: center;
  font-weight: bold;
}
.modal-body {
  padding: 20px;
  text-align: center;
}
.target-user {
  font-size: 1.1rem;
  margin-bottom: 15px;
}
.target-user span {
  color: var(--red-seal);
  font-weight: bold;
}
.modal-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 15px;
}
.rarity-C {
  color: #bdbdbd;
}
.rarity-B {
  color: #42a5f5;
}
.rarity-A {
  color: #ab47bc;
}
.rarity-S {
  color: var(--gold);
  font-weight: bold;
}
.custom-scroll::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: var(--wood-light);
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
.dark-modal.small {
  width: 350px;
  border-color: var(--wood-light);
}
.modal-header.danger {
  background: #b71c1c;
}
.center-text {
  text-align: center;
}
.center-text h3 {
  color: var(--gold);
  margin-top: 0;
}
.pop-up-enter-active,
.pop-up-leave-active {
  transition: all 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
.pop-up-enter-from,
.pop-up-leave-to {
  transform: scale(0.8);
  opacity: 0;
}
</style>