<!-- <template>
  <div class="page-container wuxia-leaderboard dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="lb-wrapper">
      <div class="lb-header">
        <div class="header-decor left"></div>
        <h2 class="title-main">BẢNG VÀNG</h2>
        <div class="header-decor right"></div>
        <div class="subtitle">--- Thiên Hạ Đệ Nhất Nhân ---</div>

        <div class="wuxia-tabs">
          <button
            :class="{ active: activeTab === 'level' }"
            @click="switchTab('level')"
            class="tab-btn"
          >
            <i class="fas fa-fist-raised"></i> CAO THỦ
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'wealth' }"
            @click="switchTab('wealth')"
            class="tab-btn"
          >
            <i class="fas fa-coins"></i> PHÚ HỘ
          </button>
        </div>
      </div>

      <div v-if="lbStore.isLoading" class="loading-state">
        <div class="ink-spinner"></div>
        <span>Đang tra cứu danh sách...</span>
      </div>

      <div v-else class="content-area">
        <div class="podium-section" v-if="currentList.length > 0">
          <div class="podium-col rank-2" v-if="currentList[1]">
            <div class="avatar-group">
              <div class="rank-badge silver">NHÌ</div>
              <div class="avatar-frame silver-border">
                <span class="char-emoji">{{
                  currentList[1].avatar || "👤"
                }}</span>
              </div>
            </div>
            <div class="podium-base silver-base">
              <div class="p-name">{{ currentList[1].username }}</div>
              <div class="p-val">{{ formatVal(currentList[1].value) }}</div>
            </div>
          </div>

          <div class="podium-col rank-1" v-if="currentList[0]">
            <div class="avatar-group">
              <div class="crown-icon"><i class="fas fa-crown"></i></div>
              <div class="avatar-frame gold-border">
                <span class="char-emoji">{{
                  currentList[0].avatar || "🐲"
                }}</span>
              </div>
              <div class="radiance"></div>
            </div>
            <div class="podium-base gold-base">
              <div class="seal-rank">MINH CHỦ</div>
              <div class="p-name main-name">{{ currentList[0].username }}</div>
              <div class="p-val highlight">
                {{ formatVal(currentList[0].value) }}
              </div>
            </div>
          </div>

          <div class="podium-col rank-3" v-if="currentList[2]">
            <div class="avatar-group">
              <div class="rank-badge bronze">BA</div>
              <div class="avatar-frame bronze-border">
                <span class="char-emoji">{{
                  currentList[2].avatar || "👤"
                }}</span>
              </div>
            </div>
            <div class="podium-base bronze-base">
              <div class="p-name">{{ currentList[2].username }}</div>
              <div class="p-val">{{ formatVal(currentList[2].value) }}</div>
            </div>
          </div>
        </div>

        <div class="rank-scroll custom-scroll">
          <div
            v-for="(entry, index) in rankedRestOfList"
            :key="entry.username"
            class="list-row"
            :style="{ animationDelay: index * 0.05 + 's' }"
          >
            <div class="row-rank">{{ entry.rank }}</div>
            <div class="row-avatar">{{ entry.avatar || "👤" }}</div>
            <div class="row-info">
              <div class="row-name">{{ entry.username }}</div>
              <div class="ink-bar-track">
                <div
                  class="ink-bar-fill"
                  :style="{ width: entry.barWidth + '%' }"
                ></div>
              </div>
            </div>
            <div class="row-val">{{ formatVal(entry.value) }}</div>
          </div>

          <div v-if="currentList.length === 0" class="empty-msg">
            <i class="fas fa-scroll"></i>
            <p>_Chưa có ai ghi danh_</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useLeaderboardStore } from "../stores/leaderboardStore";

const activeTab = ref("level");
const lbStore = useLeaderboardStore();

const currentList = computed(() => {
  return activeTab.value === "level" ? lbStore.topLevels : lbStore.topWealth;
});

const maxVal = computed(() => {
  if (!currentList.value || currentList.value.length === 0) return 1;
  // Chuyển value sang số để tìm max chính xác
  return Math.max(...currentList.value.map((e) => Number(e.value) || 0));
});

const rankedRestOfList = computed(() => {
  if (!currentList.value) return [];
  const list = currentList.value.slice(3);
  const max = maxVal.value;

  return list.map((entry, index) => ({
    ...entry,
    rank: index + 4,
    // Tính toán độ dài thanh bar dựa trên giá trị số
    barWidth: max > 0 ? (Number(entry.value) / max) * 100 : 0,
  }));
});

const formatVal = (val) => {
  const num = Number(val) || 0;
  if (activeTab.value === "wealth") {
    return num.toLocaleString() + " Vàng";
  }
  return "Cấp " + num;
};

const switchTab = async (tab) => {
  activeTab.value = tab;
  if (tab === "level") await lbStore.fetchLevelBoard();
  else await lbStore.fetchWealthBoard();
};

onMounted(() => {
  lbStore.fetchLevelBoard();
});
</script>

<style scoped>
/* Giữ nguyên phần Style cũ của bạn */
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --gold-dim: #ffe082;
  --text-light: #f3f4f6;
  --text-dim: #bdbdbd;
  --red-seal: #b71c1c;
  --panel-bg: rgba(30, 20, 15, 0.95);
}

.dark-theme {
  min-height: 100vh;
  background-color: #212121;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
}

.ink-bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-color: #3e2723;
}
.mountain-bg {
  position: absolute;
  inset: 0;
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}
.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #261815 10%, transparent 90%);
}

.lb-wrapper {
  position: relative;
  z-index: 10;
  max-width: 600px;
  margin: 0 auto;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.lb-header {
  text-align: center;
  margin-bottom: 20px;
  background: var(--panel-bg);
  border-top: 4px solid var(--wood-light);
  border-bottom: 4px solid var(--wood-light);
  padding: 20px;
  position: relative;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
}

.title-main {
  font-size: 2.5em;
  font-weight: 900;
  color: var(--gold);
  text-shadow: 0 0 10px rgba(255, 236, 179, 0.3);
  margin: 0;
  letter-spacing: 5px;
}

.subtitle {
  color: var(--text-dim);
  font-size: 0.9em;
  margin-bottom: 20px;
  font-style: italic;
  margin-top: 5px;
}

.header-decor {
  width: 50px;
  height: 2px;
  background: var(--gold);
  position: absolute;
  top: 35px;
}
.left {
  left: 20%;
}
.right {
  right: 20%;
}

.wuxia-tabs {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
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

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.podium-section {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 280px;
  margin-bottom: 20px;
  gap: 15px;
}

.podium-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  transition: 0.3s;
}
.podium-col:hover {
  transform: translateY(-5px);
}

.avatar-group {
  position: relative;
  margin-bottom: 10px;
}
.avatar-frame {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #261815;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8em;
  border: 3px solid;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.6);
  z-index: 2;
  position: relative;
}
.rank-1 .avatar-frame {
  width: 90px;
  height: 90px;
  font-size: 3em;
  border-width: 4px;
}

.gold-border {
  border-color: var(--gold);
}
.silver-border {
  border-color: #e0e0e0;
}
.bronze-border {
  border-color: #cd7f32;
}

.crown-icon {
  position: absolute;
  top: -35px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 2em;
  color: var(--gold);
  text-shadow: 0 0 10px var(--gold);
  animation: float 2s infinite ease-in-out;
  z-index: 3;
}
.rank-badge {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  background: #261815;
  color: #fff;
  font-size: 0.7em;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid;
  z-index: 3;
  font-weight: bold;
}
.silver {
  border-color: #e0e0e0;
  color: #e0e0e0;
}
.bronze {
  border-color: #cd7f32;
  color: #cd7f32;
}

.podium-base {
  width: 100px;
  text-align: center;
  padding: 10px 5px;
  border-radius: 4px 4px 0 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  border: 1px solid;
  background: rgba(30, 20, 15, 0.9);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.8);
}
.rank-1 .podium-base {
  width: 140px;
  height: 150px;
  border-color: var(--gold);
  background: linear-gradient(
    to bottom,
    rgba(255, 236, 179, 0.1),
    rgba(0, 0, 0, 0.8)
  );
}
.rank-2 .podium-base {
  height: 110px;
  border-color: #e0e0e0;
  background: linear-gradient(
    to bottom,
    rgba(224, 224, 224, 0.1),
    rgba(0, 0, 0, 0.8)
  );
}
.rank-3 .podium-base {
  height: 90px;
  border-color: #cd7f32;
  background: linear-gradient(
    to bottom,
    rgba(205, 127, 50, 0.1),
    rgba(0, 0, 0, 0.8)
  );
}

.p-name {
  font-weight: bold;
  font-size: 0.9em;
  color: var(--text-light);
}
.main-name {
  font-size: 1.1em;
  color: var(--gold);
  margin: 5px 0;
}
.p-val {
  font-size: 0.8em;
  color: var(--text-dim);
}
.highlight {
  color: #fff;
  font-weight: bold;
}
.seal-rank {
  background: var(--red-seal);
  color: #fff;
  font-size: 0.7em;
  padding: 2px 8px;
  margin-bottom: auto;
  align-self: center;
  border-radius: 2px;
}

.rank-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.list-row {
  display: flex;
  align-items: center;
  gap: 15px;
  background: rgba(30, 20, 15, 0.8);
  border: 1px solid var(--wood-light);
  margin-bottom: 8px;
  padding: 10px 15px;
  border-radius: 4px;
  transition: 0.2s;
  animation: slideIn 0.4s ease-out forwards;
  opacity: 0;
  transform: translateY(10px);
}
.list-row:hover {
  border-color: var(--gold);
  background: rgba(255, 255, 255, 0.05);
}

.row-rank {
  font-weight: 900;
  font-size: 1.2em;
  color: var(--text-dim);
  width: 30px;
  text-align: center;
}
.row-avatar {
  font-size: 1.2em;
}
.row-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.row-name {
  font-weight: bold;
  color: var(--text-light);
}

.ink-bar-track {
  width: 100%;
  height: 4px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 2px;
  overflow: hidden;
}
.ink-bar-fill {
  height: 100%;
  background: var(--red-seal);
  box-shadow: 0 0 5px var(--red-seal);
  transition: width 0.5s ease;
}

.row-val {
  font-weight: bold;
  color: var(--gold);
  font-size: 0.9em;
  text-align: right;
}

.loading-state {
  text-align: center;
  padding: 50px;
  color: var(--text-dim);
}
.ink-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--wood-light);
  border-top-color: var(--gold);
  border-radius: 50%;
  margin: 0 auto 15px;
  animation: spin 1s linear infinite;
}
.empty-msg {
  text-align: center;
  color: var(--text-dim);
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.empty-msg i {
  font-size: 2em;
  opacity: 0.5;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@keyframes float {
  0%,
  100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(-5px);
  }
}
@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.custom-scroll::-webkit-scrollbar {
  width: 4px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: var(--wood-light);
  border-radius: 2px;
}
</style> -->
<template>
  <div class="page-container wuxia-leaderboard dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="lb-wrapper">
      <div class="lb-header">
        <div class="header-decor left"></div>
        <h2 class="title-main">BẢNG VÀNG</h2>
        <div class="header-decor right"></div>
        <div class="subtitle">--- Thiên Hạ Đệ Nhất Nhân ---</div>

        <div class="wuxia-tabs">
          <button
            :class="{ active: activeTab === 'level' }"
            @click="switchTab('level')"
            class="tab-btn"
          >
            <i class="fas fa-fist-raised"></i> CAO THỦ
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'wealth' }"
            @click="switchTab('wealth')"
            class="tab-btn"
          >
            <i class="fas fa-coins"></i> PHÚ HỘ
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{ active: activeTab === 'kills', 'text-red': activeTab === 'kills' }"
            @click="switchTab('kills')"
            class="tab-btn"
          >
            <i class="fas fa-skull"></i> SÁT THẦN
          </button>
        </div>
      </div>

      <div v-if="isLoading" class="loading-state">
        <div class="ink-spinner"></div>
        <span>Đang tra cứu danh sách...</span>
      </div>

      <div v-else class="content-area">
        
        <div class="podium-section" v-if="currentList.length > 0">
          
          <div class="podium-col rank-2" v-if="currentList[1]">
            <div class="avatar-group">
              <div class="rank-badge silver">NHÌ</div>
              <div class="avatar-frame silver-border">
                <span class="char-emoji">{{ currentList[1].avatar || "👤" }}</span>
              </div>
            </div>
            <div class="podium-base silver-base">
              <div class="p-name">{{ currentList[1].username }}</div>
              <div class="p-val">{{ formatVal(currentList[1].value) }}</div>
            </div>
          </div>

          <div class="podium-col rank-1" v-if="currentList[0]">
            <div class="avatar-group">
              <div class="crown-icon" :class="{ 'red-crown': activeTab === 'kills' }">
                <i class="fas fa-crown"></i>
              </div>
              <div class="avatar-frame gold-border">
                <span class="char-emoji">{{ currentList[0].avatar || "🐲" }}</span>
              </div>
              <div class="radiance"></div>
            </div>
            <div class="podium-base gold-base" :class="{ 'red-base': activeTab === 'kills' }">
              <div class="seal-rank" :class="{ 'bg-black': activeTab === 'kills' }">
                {{ rankTitle }}
              </div>
              <div class="p-name main-name">{{ currentList[0].username }}</div>
              <div class="p-val highlight">
                {{ formatVal(currentList[0].value) }}
              </div>
            </div>
          </div>

          <div class="podium-col rank-3" v-if="currentList[2]">
            <div class="avatar-group">
              <div class="rank-badge bronze">BA</div>
              <div class="avatar-frame bronze-border">
                <span class="char-emoji">{{ currentList[2].avatar || "👤" }}</span>
              </div>
            </div>
            <div class="podium-base bronze-base">
              <div class="p-name">{{ currentList[2].username }}</div>
              <div class="p-val">{{ formatVal(currentList[2].value) }}</div>
            </div>
          </div>
        </div>

        <div class="rank-scroll custom-scroll">
          <div
            v-for="(entry, index) in rankedRestOfList"
            :key="entry.username"
            class="list-row"
            :class="{ 'kill-row': activeTab === 'kills' }"
            :style="{ animationDelay: index * 0.05 + 's' }"
          >
            <div class="row-rank">{{ entry.rank }}</div>
            <div class="row-avatar">{{ entry.avatar || "👤" }}</div>
            <div class="row-info">
              <div class="row-name">{{ entry.username }}</div>
              <div class="ink-bar-track">
                <div
                  class="ink-bar-fill"
                  :class="{ 'bg-blood': activeTab === 'kills' }"
                  :style="{ width: entry.barWidth + '%' }"
                ></div>
              </div>
            </div>
            <div class="row-val">{{ formatVal(entry.value) }}</div>
          </div>

          <div v-if="currentList.length === 0" class="empty-msg">
            <i class="fas fa-scroll"></i>
            <p>_Chưa có ai ghi danh_</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useLeaderboardStore } from "@/stores/leaderboardStore"; // Đảm bảo đúng đường dẫn file store

const activeTab = ref("level");
const lbStore = useLeaderboardStore();

// Computed: Kiểm tra loading dựa trên tab đang chọn
const isLoading = computed(() => {
  if (activeTab.value === 'level') return lbStore.loadingLevels;
  if (activeTab.value === 'wealth') return lbStore.loadingWealth;
  if (activeTab.value === 'kills') return lbStore.loadingMonsters;
  return false;
});

// Computed: Lấy danh sách dựa trên tab (Có fallback || [] để tránh lỗi .length)
const currentList = computed(() => {
  if (activeTab.value === "level") return lbStore.topLevels || [];
  if (activeTab.value === "wealth") return lbStore.topWealth || [];
  if (activeTab.value === "kills") return lbStore.topMonsters || []; // [FIX] Map đúng biến Store
  return [];
});

// Computed: Tiêu đề danh hiệu Top 1
const rankTitle = computed(() => {
  if (activeTab.value === "level") return "MINH CHỦ";
  if (activeTab.value === "wealth") return "PHÚ GIA";
  if (activeTab.value === "kills") return "SÁT THẦN";
  return "TOP 1";
});

// Helper: Tìm giá trị lớn nhất để vẽ thanh bar (%)
const maxVal = computed(() => {
  if (!currentList.value || currentList.value.length === 0) return 1;
  // Regex để lấy số từ chuỗi (VD: "10,500 Quái" -> 10500)
  return Math.max(...currentList.value.map((e) => {
    if(!e.value) return 0;
    // Xóa tất cả ký tự không phải số
    const num = Number(e.value.toString().replace(/[^0-9]/g, '')); 
    return num || 0;
  }));
});

// Computed: Danh sách từ hạng 4 trở đi
const rankedRestOfList = computed(() => {
  if (!currentList.value || currentList.value.length < 3) return [];
  const list = currentList.value.slice(3);
  const max = maxVal.value;

  return list.map((entry, index) => {
    // Parse số để tính % width
    const num = Number(entry.value.toString().replace(/[^0-9]/g, '')) || 0;
    return {
      ...entry,
      rank: index + 4,
      barWidth: max > 0 ? (num / max) * 100 : 0,
    };
  });
});

// Helper: Format hiển thị (Thực ra Backend đã format rồi, hàm này chỉ để đảm bảo)
const formatVal = (val) => {
  return val; // Backend đã trả về string đẹp ("Lv 10", "1,000 Vàng", "500 Quái") nên cứ thế hiển thị
};

// Action: Chuyển tab và gọi API tương ứng
const switchTab = async (tab) => {
  activeTab.value = tab;
  if (tab === "level") await lbStore.fetchLevelBoard();
  else if (tab === "wealth") await lbStore.fetchWealthBoard();
  
  // [FIX QUAN TRỌNG] Gọi đúng tên hàm trong Store
  else if (tab === "kills") await lbStore.fetchMonsterBoard(); 
};

// Init: Tải mặc định tab Level
onMounted(() => {
  lbStore.fetchLevelBoard();
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
  --panel-bg: rgba(30, 20, 15, 0.95);
  --blood: #d32f2f;
}

.dark-theme {
  min-height: 100vh;
  background-color: #212121;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
}

/* Background Layers */
.ink-bg-layer, .mountain-bg, .fog-anim { position: absolute; inset: 0; }
.ink-bg-layer { background-color: #3e2723; z-index: 0; }
.mountain-bg {
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}
.fog-anim { background: linear-gradient(to top, #261815 10%, transparent 90%); }

/* Wrapper & Header */
.lb-wrapper {
  position: relative; z-index: 10; max-width: 600px; margin: 0 auto;
  padding: 30px 20px; display: flex; flex-direction: column; height: 100vh;
}
.lb-header {
  text-align: center; margin-bottom: 20px; background: var(--panel-bg);
  border-top: 4px solid var(--wood-light); border-bottom: 4px solid var(--wood-light);
  padding: 20px; position: relative; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
}
.title-main { font-size: 2.5em; font-weight: 900; color: var(--gold); letter-spacing: 5px; margin: 0; }
.subtitle { color: var(--text-dim); font-size: 0.9em; margin-bottom: 20px; font-style: italic; }
.header-decor { width: 50px; height: 2px; background: var(--gold); position: absolute; top: 35px; }
.left { left: 20%; } .right { right: 20%; }

/* Tabs */
.wuxia-tabs { display: flex; justify-content: center; align-items: center; gap: 10px; }
.tab-btn {
  background: transparent; border: none; color: var(--text-dim);
  padding: 8px 10px; font-family: inherit; font-weight: bold; cursor: pointer;
  display: flex; align-items: center; gap: 6px; transition: 0.3s;
  border-bottom: 2px solid transparent;
}
.tab-btn:hover, .tab-btn.active { color: var(--gold); border-bottom-color: var(--gold); }
.tab-divider { width: 1px; height: 15px; background: #555; }
/* Style riêng cho Sát Thần */
.tab-btn.text-red:hover, .tab-btn.text-red.active { color: #ff5252; border-bottom-color: #ff5252; text-shadow: 0 0 8px rgba(255, 0, 0, 0.6); }

/* Content & Loading */
.content-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.loading-state, .empty-msg { text-align: center; padding: 50px; color: var(--text-dim); }
.ink-spinner { width: 40px; height: 40px; border: 3px solid var(--wood-light); border-top-color: var(--gold); border-radius: 50%; margin: 0 auto 15px; animation: spin 1s linear infinite; }

/* Podium (Bục vinh quang) */
.podium-section { display: flex; align-items: flex-end; justify-content: center; height: 280px; gap: 15px; margin-bottom: 20px; }
.podium-col { display: flex; flex-direction: column; align-items: center; position: relative; }

.avatar-frame { width: 60px; height: 60px; border-radius: 50%; background: #261815; display: flex; align-items: center; justify-content: center; font-size: 1.8em; border: 3px solid; z-index: 2; box-shadow: 0 5px 15px rgba(0,0,0,0.5); }
.rank-1 .avatar-frame { width: 90px; height: 90px; font-size: 3em; border-width: 4px; }
.gold-border { border-color: var(--gold); }
.silver-border { border-color: #e0e0e0; }
.bronze-border { border-color: #cd7f32; }

.rank-badge { position: absolute; bottom: -8px; left: 50%; transform: translateX(-50%); background: #261815; font-size: 0.7em; padding: 2px 8px; border-radius: 4px; border: 1px solid; z-index: 3; font-weight: bold; }
.silver { border-color: #e0e0e0; color: #e0e0e0; }
.bronze { border-color: #cd7f32; color: #cd7f32; }

.crown-icon { position: absolute; top: -35px; left: 50%; transform: translateX(-50%); font-size: 2em; color: var(--gold); animation: float 2s infinite ease-in-out; z-index: 3; }
.crown-icon.red-crown { color: #ff5252; text-shadow: 0 0 10px #b71c1c; }

.podium-base { width: 100px; text-align: center; padding: 10px 5px; border-radius: 4px 4px 0 0; display: flex; flex-direction: column; justify-content: flex-end; border: 1px solid; background: rgba(30, 20, 15, 0.9); box-shadow: 0 5px 15px rgba(0,0,0,0.6); }
.rank-1 .podium-base { width: 140px; height: 150px; border-color: var(--gold); background: linear-gradient(to bottom, rgba(255, 236, 179, 0.1), rgba(0, 0, 0, 0.8)); }
.rank-2 .podium-base { height: 110px; border-color: #e0e0e0; background: linear-gradient(to bottom, rgba(224, 224, 224, 0.1), rgba(0, 0, 0, 0.8)); }
.rank-3 .podium-base { height: 90px; border-color: #cd7f32; background: linear-gradient(to bottom, rgba(205, 127, 50, 0.1), rgba(0, 0, 0, 0.8)); }

.podium-base.red-base { border-color: #ff5252; background: linear-gradient(to bottom, rgba(255, 0, 0, 0.2), #000); }
.seal-rank { background: var(--red-seal); color: #fff; font-size: 0.7em; padding: 2px 8px; margin-bottom: auto; align-self: center; border-radius: 2px; }
.seal-rank.bg-black { background: #000; border: 1px solid #ff5252; color: #ff5252; }

.p-name { font-weight: bold; font-size: 0.9em; color: var(--text-light); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%; }
.main-name { font-size: 1.1em; color: var(--gold); margin: 5px 0; }
.p-val { font-size: 0.8em; color: var(--text-dim); }
.highlight { color: #fff; font-weight: bold; }

/* Scroll List */
.rank-scroll { flex: 1; overflow-y: auto; padding: 10px; }
.list-row { display: flex; align-items: center; gap: 15px; background: rgba(30, 20, 15, 0.8); border: 1px solid var(--wood-light); margin-bottom: 8px; padding: 10px 15px; border-radius: 4px; transition: 0.2s; animation: slideIn 0.4s ease-out forwards; opacity: 0; transform: translateY(10px); }
.list-row:hover { border-color: var(--gold); background: rgba(255, 255, 255, 0.05); }
.list-row.kill-row:hover { border-color: #ff5252; background: rgba(255, 0, 0, 0.1); }

.row-rank { font-weight: 900; font-size: 1.2em; color: var(--text-dim); width: 30px; text-align: center; }
.row-avatar { font-size: 1.2em; }
.row-info { flex: 1; display: flex; flex-direction: column; gap: 5px; min-width: 0; }
.row-name { font-weight: bold; color: var(--text-light); }
.row-val { font-weight: bold; color: var(--gold); font-size: 0.9em; text-align: right; white-space: nowrap;}

.ink-bar-track { width: 100%; height: 4px; background: rgba(0, 0, 0, 0.5); border-radius: 2px; overflow: hidden; }
.ink-bar-fill { height: 100%; background: var(--red-seal); transition: width 0.5s ease; }
.ink-bar-fill.bg-blood { background: #ff5252; box-shadow: 0 0 8px #ff5252; }

@keyframes spin { to { transform: rotate(360deg); } }
@keyframes float { 0%, 100% { transform: translateX(-50%) translateY(0); } 50% { transform: translateX(-50%) translateY(-5px); } }
@keyframes slideIn { to { opacity: 1; transform: translateY(0); } }
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: var(--wood-light); border-radius: 2px; }
</style>