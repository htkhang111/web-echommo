<template>
  <div class="page-container wuxia-leaderboard dark-theme">
    <div class="bg-layer">
      <div
        class="mountain-bg"
        :style="{ backgroundImage: `url(${bgImage})` }"
      ></div>
      <div class="wood-overlay" :class="{ 'night-mode': isNight }"></div>
      <div class="vignette"></div>
    </div>

    <div class="lb-wrapper">
      <div class="lb-header">
        <div class="header-decor left"></div>
        <h2 class="title-main">THIÊN THƯ</h2>
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
            :class="{
              active: activeTab === 'pvp',
              'text-purple': activeTab === 'pvp',
            }"
            @click="switchTab('pvp')"
            class="tab-btn"
          >
            <i class="fas fa-swords"></i> GIANG HỒ
          </button>
          <div class="tab-divider"></div>
          <button
            :class="{
              active: activeTab === 'kills',
              'text-red': activeTab === 'kills',
            }"
            @click="switchTab('kills')"
            class="tab-btn"
          >
            <i class="fas fa-skull"></i> TRẢM YÊU
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
                <img
                  :src="resolveAvatar(currentList[1].avatar)"
                  class="podium-img"
                />
              </div>
            </div>
            <div class="podium-base silver-base">
              <div class="p-name">{{ currentList[1].username }}</div>
              <div class="p-val">{{ formatVal(currentList[1].value) }}</div>
            </div>
          </div>

          <div class="podium-col rank-1" v-if="currentList[0]">
            <div class="avatar-group">
              <div
                class="crown-icon"
                :class="{
                  'red-crown': activeTab === 'kills',
                  'purple-crown': activeTab === 'pvp',
                }"
              >
                <i class="fas fa-crown"></i>
              </div>
              <div class="avatar-frame gold-border">
                <img
                  :src="resolveAvatar(currentList[0].avatar)"
                  class="podium-img"
                />
              </div>
              <div class="radiance"></div>
            </div>
            <div
              class="podium-base gold-base"
              :class="{
                'red-base': activeTab === 'kills',
                'purple-base': activeTab === 'pvp',
              }"
            >
              <div
                class="seal-rank"
                :class="{
                  'bg-black': activeTab === 'kills' || activeTab === 'pvp',
                }"
              >
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
                <img
                  :src="resolveAvatar(currentList[2].avatar)"
                  class="podium-img"
                />
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
            :key="entry.username + index"
            class="list-row"
            :class="{
              'kill-row': activeTab === 'kills',
              'pvp-row': activeTab === 'pvp',
            }"
            :style="{ animationDelay: index * 0.05 + 's' }"
          >
            <div class="row-rank">{{ entry.rank }}</div>
            <div class="row-avatar-box">
              <img :src="resolveAvatar(entry.avatar)" class="row-img" />
            </div>
            <div class="row-info">
              <div class="row-name">{{ entry.username }}</div>
              <div class="ink-bar-track">
                <div
                  class="ink-bar-fill"
                  :class="{
                    'bg-blood': activeTab === 'kills',
                    'bg-void': activeTab === 'pvp',
                  }"
                  :style="{ width: entry.barWidth + '%' }"
                ></div>
              </div>
            </div>
            <div class="row-val">{{ formatVal(entry.value) }}</div>
          </div>

          <div v-if="currentList.length === 0" class="empty-msg">
            <i class="fas fa-scroll"></i>
            <p>_Chưa có cao thủ nào ghi danh_</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useLeaderboardStore } from "../stores/leaderboardStore"; // Chỉnh lại đường dẫn nếu cần
import { CHARACTER_SKINS } from "../utils/assetHelper"; // Chỉnh lại đường dẫn nếu cần

const activeTab = ref("level");
const lbStore = useLeaderboardStore();

const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const isNight = ref(false);

const updateDayNight = () => {
  const h = new Date().getHours();
  isNight.value = h >= 18 || h < 6;
};

// --- COMPUTED: Loading State ---
const isLoading = computed(() => {
  if (activeTab.value === "level") return lbStore.loadingLevels;
  if (activeTab.value === "wealth") return lbStore.loadingWealth;
  if (activeTab.value === "kills") return lbStore.loadingMonsters;
  if (activeTab.value === "pvp") return lbStore.loadingPvp;
  return false;
});

// --- [FIX] COMPUTED: Mapping Dữ liệu ---
// Logic này chuẩn hóa mọi dữ liệu về dạng { username, value, avatar } để UI hiển thị đúng
const currentList = computed(() => {
  // 1. TOP LEVEL
  if (activeTab.value === "level") {
    return (lbStore.topLevels || []).map((e) => ({
      ...e,
      username: e.username || e.name || "Vô Danh",
      value: e.level || 0,
      avatar: e.avatar || e.avatarUrl,
    }));
  }

  // 2. TOP PHÚ HỘ
  if (activeTab.value === "wealth") {
    return (lbStore.topWealth || []).map((e) => ({
      ...e,
      username: e.username || e.name || "Vô Danh",
      // Lấy từ wallet.gold nếu có, hoặc gold trực tiếp
      value: e.wallet?.gold !== undefined ? e.wallet.gold : (e.gold || 0),
      avatar: e.avatar || e.avatarUrl,
    }));
  }

  // 3. TOP TRẢM YÊU
  if (activeTab.value === "kills") {
    return (lbStore.topMonsters || []).map((e) => ({
      ...e,
      username: e.username || e.name || "Vô Danh",
      value: e.kills || 0,
      avatar: e.avatar || e.avatarUrl,
    }));
  }

  // 4. [FIX QUAN TRỌNG] TOP PVP
  // API PvP trả về: name, reputation, avatarUrl -> Map sang username, value, avatar
  if (activeTab.value === "pvp") {
    return (lbStore.topPvp || []).map((e) => ({
      ...e,
      username: e.name || "Vô Danh",
      value: e.reputation || 0,
      avatar: e.avatarUrl,
    }));
  }

  return [];
});

const rankTitle = computed(() => {
  if (activeTab.value === "level") return "MINH CHỦ";
  if (activeTab.value === "wealth") return "PHÚ GIA";
  if (activeTab.value === "kills") return "SÁT THẦN";
  if (activeTab.value === "pvp") return "CHIẾN THẦN";
  return "TOP 1";
});

// Tính toán giá trị lớn nhất để vẽ thanh bar (%)
const maxVal = computed(() => {
  if (!currentList.value || currentList.value.length === 0) return 1;
  return Math.max(
    ...currentList.value.map((e) => {
      // Đảm bảo value là số
      return Number(e.value) || 0;
    })
  );
});

// Danh sách từ top 4 trở đi
const rankedRestOfList = computed(() => {
  if (!currentList.value || currentList.value.length < 3) return [];
  const list = currentList.value.slice(3);
  const max = maxVal.value;

  return list.map((entry, index) => {
    const num = Number(entry.value) || 0;
    return {
      ...entry,
      rank: index + 4,
      barWidth: max > 0 ? (num / max) * 100 : 0,
    };
  });
});

const formatVal = (val) => {
    return new Intl.NumberFormat('vi-VN').format(val || 0);
};

// --- ASSET HELPER ---
const getSpriteUrl = (skinId) => {
  const skin = CHARACTER_SKINS[skinId] || CHARACTER_SKINS["skin_yasou"];
  return skin ? skin.sprites.idle : "https://placehold.co/50?text=?";
};

const resolveAvatar = (avatarStr) => {
  if (!avatarStr) return getSpriteUrl("skin_yasou");
  if (avatarStr.startsWith("/uploads/"))
    return `http://localhost:8080${avatarStr}`;
  if (avatarStr.includes("http") || avatarStr.startsWith("data:"))
    return avatarStr;
  
  // Nếu là skin ID (ví dụ 'skin_yasou')
  return getSpriteUrl(avatarStr);
};

const switchTab = async (tab) => {
  activeTab.value = tab;
  if (tab === "level") await lbStore.fetchLevelBoard();
  else if (tab === "wealth") await lbStore.fetchWealthBoard();
  else if (tab === "kills") await lbStore.fetchMonsterBoard();
  else if (tab === "pvp") await lbStore.fetchPvPBoard();
};

onMounted(() => {
  updateDayNight();
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
  --blood: #d32f2f;
  --purple: #a855f7;
}

.dark-theme {
  background-color: transparent;
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
}

/* Background Systems */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: #261815;
}
.mountain-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center bottom;
  opacity: 0.6;
}
.wood-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(62, 39, 35, 0.7),
    rgba(30, 20, 15, 0.9)
  );
  mix-blend-mode: multiply;
}
.wood-overlay.night-mode {
  background: linear-gradient(
    to bottom,
    rgba(10, 5, 20, 0.85),
    rgba(0, 0, 0, 0.95)
  );
}
.vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, transparent 60%, #1a100d 100%);
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
  background: rgba(30, 20, 15, 0.8);
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
  letter-spacing: 5px;
  margin: 0;
}
.subtitle {
  color: var(--text-dim);
  font-size: 0.9em;
  margin-bottom: 20px;
  font-style: italic;
}

.wuxia-tabs {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.tab-btn {
  background: transparent;
  border: none;
  color: var(--text-dim);
  padding: 8px 10px;
  font-family: inherit;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: 0.3s;
  border-bottom: 2px solid transparent;
}
.tab-btn:hover,
.tab-btn.active {
  color: var(--gold);
  border-bottom-color: var(--gold);
}
.tab-divider {
  width: 1px;
  height: 12px;
  background: #555;
}
.tab-btn.text-red:hover,
.tab-btn.text-red.active {
  color: #ff5252;
  border-bottom-color: #ff5252;
  text-shadow: 0 0 8px rgba(255, 0, 0, 0.5);
}
.tab-btn.text-purple:hover,
.tab-btn.text-purple.active {
  color: var(--purple);
  border-bottom-color: var(--purple);
  text-shadow: 0 0 8px rgba(168, 85, 247, 0.5);
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.podium-section {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 260px;
  gap: 15px;
  margin-bottom: 20px;
  margin-top: 10px;
}
.podium-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.avatar-frame {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #261815;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid;
  z-index: 2;
  overflow: hidden;
}
.rank-1 .avatar-frame {
  width: 85px;
  height: 85px;
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
.podium-img {
  width: 130%;
  height: 130%;
  object-fit: contain;
  image-rendering: pixelated;
}

.crown-icon {
  position: absolute;
  top: -35px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 2em;
  color: var(--gold);
  animation: float 2s infinite ease-in-out;
  z-index: 3;
}
.crown-icon.red-crown {
  color: #ff5252;
}
.crown-icon.purple-crown {
  color: var(--purple);
  text-shadow: 0 0 10px var(--purple);
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
}
.rank-1 .podium-base {
  width: 130px;
  height: 140px;
  border-color: var(--gold);
}
.podium-base.red-base {
  border-color: #ff5252;
  background: linear-gradient(to bottom, rgba(255, 0, 0, 0.2), #000);
}
.podium-base.purple-base {
  border-color: var(--purple);
  background: linear-gradient(to bottom, rgba(168, 85, 247, 0.2), #000);
}

.p-name {
  font-weight: bold;
  font-size: 0.9em;
  color: var(--text-light);
}
.main-name {
  font-size: 1.1em;
  color: var(--gold);
}
.p-val {
  font-size: 0.8em;
  color: var(--text-dim);
}
.highlight {
  color: #fff;
  font-weight: bold;
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
  animation: slideIn 0.4s ease-out forwards;
  opacity: 0;
}
.list-row:hover {
  border-color: var(--gold);
}
.row-rank {
  font-weight: 900;
  font-size: 1.1em;
  color: var(--text-dim);
  width: 25px;
}
.row-avatar-box {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #5d4037;
  background: #000;
}
.row-img {
  width: 130%;
  height: 130%;
  object-fit: contain;
  image-rendering: pixelated;
}
.row-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.row-name {
  font-weight: bold;
  font-size: 0.95em;
}
.row-val {
  font-weight: bold;
  color: var(--gold);
  font-size: 0.9em;
}

.ink-bar-track {
  width: 100%;
  height: 4px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 2px;
}
.ink-bar-fill {
  height: 100%;
  background: var(--red-seal);
  transition: width 0.5s;
}
.ink-bar-fill.bg-blood {
  background: #ff5252;
}
.ink-bar-fill.bg-void {
  background: var(--purple);
  box-shadow: 0 0 5px var(--purple);
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
  }
}
.custom-scroll::-webkit-scrollbar {
  width: 4px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: var(--wood-light);
}
</style>