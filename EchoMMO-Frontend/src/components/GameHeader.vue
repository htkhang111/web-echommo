<template>
  <header class="game-header">
    <div class="header-texture"></div>
    <div class="header-border-bot"></div>
    <div class="spacer"></div>

    <div class="hud-container" v-if="authStore.token">
      <div class="resource-bank">
        <div class="res-module gold" title="Ngân Lượng">
          <div class="res-icon"><i class="fas fa-coins"></i></div>
          <div class="res-val">
            {{ formatNumber(authStore.wallet?.gold || 0) }}
          </div>
        </div>

        <div class="res-module jade mobile-hide" title="Linh Thạch">
          <div class="res-icon"><i class="fas fa-gem"></i></div>
          <div class="res-val">
            {{ formatNumber(authStore.wallet?.diamonds || 0) }}
          </div>
        </div>

        <div class="res-module energy" title="Chân Khí">
          <div class="res-icon"><i class="fas fa-bolt"></i></div>
          <div class="energy-track">
            <div
              class="energy-bar"
              :style="{ width: energyPercent + '%' }"
            ></div>
          </div>
          <div class="res-val small">
            {{ formatNumber(charStore.character?.energy || 0) }}
          </div>
        </div>
      </div>

      <router-link
        to="/friends"
        class="hud-icon-node friend-node"
        :class="{ 'has-signal': friendRequestCount > 0 }"
      >
        <div class="node-icon"><i class="fas fa-user-friends"></i></div>
        <div class="node-badge" v-if="friendRequestCount > 0">
          {{ friendRequestCount > 9 ? "9+" : friendRequestCount }}
        </div>
      </router-link>

      <router-link
        to="/notifications"
        class="hud-icon-node noti-node"
        :class="{ 'has-signal': notiStore.unreadCount > 0 }"
      >
        <div class="node-icon"><i class="fas fa-bell"></i></div>
        <div class="node-badge" v-if="notiStore.unreadCount > 0">
          {{ notiStore.unreadCount > 99 ? "99+" : notiStore.unreadCount }}
        </div>
      </router-link>

      <router-link
        to="/profile"
        class="profile-link"
        v-if="charStore.character"
      >
        <div class="profile-info mobile-hide">
          <div class="char-name">{{ charStore.character.name }}</div>
          <div class="xp-track">
            <div class="xp-fill" :style="{ width: xpPercent + '%' }"></div>
          </div>
        </div>

        <div class="avatar-frame">
          <img :src="userSkinAvatar" class="pixel-focus" />
        </div>
      </router-link>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useAuthStore } from "../stores/authStore";
import { useCharacterStore } from "../stores/characterStore";
import { useNotificationStore } from "../stores/notificationStore";
import { getCurrentSkin } from "@/utils/assetHelper";

const authStore = useAuthStore();
const charStore = useCharacterStore();
const notiStore = useNotificationStore();

// Khai báo biến ref cho số lượng yêu cầu kết bạn (Đã thêm)
const friendRequestCount = ref(0);

const userSkinAvatar = computed(
  () => getCurrentSkin(authStore.user?.avatarUrl).sprites.idle,
);

const xpPercent = computed(() => {
  if (!charStore.character) return 0;
  const needed = 100 * Math.pow(charStore.character.lv, 2);
  return Math.min((charStore.character.exp / needed) * 100, 100);
});

const energyPercent = computed(() => {
  if (!charStore.character || charStore.character.maxEnergy === 0) return 0;
  return Math.min(
    (charStore.character.energy / charStore.character.maxEnergy) * 100,
    100,
  );
});

const formatNumber = (num) => {
  if (!num) return "0";
  if (num >= 1000000)
    return (num / 1000000).toFixed(1).replace(/\.0$/, "") + "M";
  if (num >= 1000) return (num / 1000).toFixed(1).replace(/\.0$/, "") + "K";
  return num.toString();
};

onMounted(() => {
  if (authStore.token) {
    authStore.fetchProfile();
    charStore.fetchCharacter();
    notiStore.fetchUnreadCount();
    // Thêm logic fetch friend requests count nếu cần
  }
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700&family=Cinzel:wght@700&display=swap");

:root {
  --wood-dark: #3e2723;
  --gold-accent: #fbc02d;
  --red-seal: #b71c1c;
}

.game-header {
  position: sticky;
  top: 0;
  z-index: 900;
  width: 100%;
  height: 60px;
  background: #3e2723;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  font-family: "Cinzel", serif;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
  border-bottom: 2px solid #5d4037;
}

.header-border-bot {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    var(--gold-accent),
    transparent
  );
}

.spacer {
  flex: 1;
}

.hud-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

/* --- RESOURCE BANK --- */
.resource-bank {
  display: flex;
  gap: 12px; /* Tăng khoảng cách giữa các tài nguyên */
  align-items: center;
  background: rgba(0, 0, 0, 0.3);
  padding: 5px 12px;
  border-radius: 6px;
  border: 1px solid #5d4037;
}

.res-module {
  display: flex;
  align-items: center;
  gap: 6px;
}

.res-icon {
  font-size: 1em;
  filter: drop-shadow(0 2px 2px rgba(0, 0, 0, 0.8)); /* Bóng đổ icon đậm hơn */
}

.res-val {
  font-weight: bold;
  font-size: 0.95em;
}

.res-val.small {
  font-size: 0.85em;
  min-width: 30px;
  text-align: right;
  color: #e0e0e0;
}

/* --- MÀU SẮC TÀI NGUYÊN (FIXED) --- */

/* 1. NGÂN LƯỢNG (GOLD) - Màu Vàng Rực */
.resource-bank .gold .res-icon {
  color: #ffeb3b;
}
.resource-bank .gold .res-val {
  color: #ffd700;
  text-shadow: 0 0 2px #b71c1c;
}

/* 2. LINH THẠCH (JADE) - Màu Xanh Ngọc */
.resource-bank .jade .res-icon {
  color: #00e676;
}
.resource-bank .jade .res-val {
  color: #69f0ae;
}

/* 3. CHÂN KHÍ (ENERGY) - Màu Xanh Dương */
.resource-bank .energy .res-icon {
  color: #2979ff;
} /* Icon xanh dương sáng */

.energy-track {
  width: 60px;
  height: 8px; /* Đã giữ nguyên */
  background: #1a1a1a;
  border: 1px solid #546e7a;
  border-radius: 3px;
  overflow: hidden;
  box-shadow: inset 0 0 3px #000;
}

.energy-bar {
  height: 100%; /* Đảm bảo thanh đầy đủ chiều cao container */
  background: linear-gradient(
    90deg,
    #1565c0,
    #42a5f5
  ); /* Gradient xanh dương */
  transition: width 0.3s ease;
  box-shadow: 0 0 5px #2979ff; /* Phát sáng nhẹ */
}

/* --- HUD ICON NODES --- */
.hud-icon-node {
  position: relative;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bcaaa4;
  transition: 0.3s;
  cursor: pointer;
  border-radius: 50%;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid transparent;
}

.hud-icon-node:hover {
  color: #fbc02d;
  background: rgba(255, 255, 255, 0.1);
  border-color: #fbc02d;
}

.hud-icon-node.has-signal .node-icon {
  animation: shake 2s infinite;
  color: #ffca28;
}

@keyframes shake {
  0%,
  100% {
    transform: rotate(0);
  }
  25% {
    transform: rotate(-15deg);
  }
  75% {
    transform: rotate(15deg);
  }
}

.node-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #d32f2f;
  color: #fff;
  font-size: 0.7em;
  font-weight: bold;
  padding: 2px 5px;
  border-radius: 10px;
  border: 1px solid #fff;
  min-width: 18px;
  text-align: center;
  box-shadow: 0 2px 2px rgba(0, 0, 0, 0.3);
}

/* --- PROFILE --- */
.profile-link {
  display: flex;
  align-items: center;
  gap: 15px;
  text-decoration: none;
  padding-left: 15px;
  border-left: 1px solid #5d4037;
  transition: 0.3s;
}

.profile-link:hover .char-name {
  color: #fbc02d;
  text-shadow: 0 0 5px rgba(251, 192, 45, 0.5);
}

.profile-info {
  text-align: right;
}

.char-name {
  font-weight: bold;
  font-size: 1em;
  color: #fdf5e6;
  font-family: "Playfair Display", serif;
}

.xp-track {
  width: 80px;
  height: 4px;
  background: #1a1a1a;
  margin-top: 4px;
  margin-left: auto;
  border-radius: 2px;
  overflow: hidden;
}

.xp-fill {
  height: 100%;
  background: linear-gradient(90deg, #fbc02d, #ffeb3b);
  transition: width 0.5s;
}

.avatar-frame {
  width: 50px;
  height: 50px;
  position: relative;
  border-radius: 50%;
  background: #1a1510;
  border: 2px solid #8d6e63;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.6);
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.pixel-focus {
  width: 130%;
  height: 130%;
  object-fit: contain;
  object-position: center;
  image-rendering: pixelated;
}

@media (max-width: 600px) {
  .mobile-hide {
    display: none;
  }

  .resource-bank {
    gap: 8px;
    padding: 4px 8px;
  }

  .energy-track {
    width: 35px;
  }

  .game-header {
    padding: 0 10px;
  }
}
</style>