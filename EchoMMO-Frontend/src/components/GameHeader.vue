<template>
  <header class="game-header">
    <div class="header-texture"></div>
    <div class="header-border-bot"></div>
    <div class="spacer"></div>

    <div class="hud-container" v-if="authStore.token">
      <div class="resource-bank">
        <div class="res-module gold" title="Ngân Lượng">
          <div class="res-icon"><i class="fas fa-coins"></i></div>
          <div class="res-val">{{ formatNumber(currentGold) }}</div>
        </div>

        <div class="res-module jade mobile-hide" title="Linh Thạch">
          <div class="res-icon"><i class="fas fa-gem"></i></div>
          <div class="res-val">{{ formatNumber(currentDiamonds) }}</div>
        </div>

        <div class="res-module energy" title="Chân Khí">
          <div class="res-icon"><i class="fas fa-bolt"></i></div>
          <div class="energy-track">
            <div class="energy-bar" :style="{ width: charStore.energyPercent + '%' }"></div>
          </div>
          <div class="res-val small">
            {{ formatNumber(charStore.character?.currentEnergy || 0) }}
          </div>
        </div>
      </div>

      <router-link to="/friends" class="hud-icon-node friend-node" :class="{ 'has-signal': friendRequestCount > 0 }">
        <div class="node-icon"><i class="fas fa-user-friends"></i></div>
        <div class="node-badge" v-if="friendRequestCount > 0">
          {{ friendRequestCount > 9 ? "9+" : friendRequestCount }}
        </div>
      </router-link>

      <router-link to="/notifications" class="hud-icon-node noti-node" :class="{ 'has-signal': notiStore.unreadCount > 0 }">
        <div class="node-icon"><i class="fas fa-bell"></i></div>
        <div class="node-badge" v-if="notiStore.unreadCount > 0">
          {{ notiStore.unreadCount > 99 ? "99+" : notiStore.unreadCount }}
        </div>
      </router-link>

      <div class="profile-group">
        <router-link to="/profile" class="profile-link" v-if="charStore.character">
          <div class="profile-info mobile-hide">
            <div class="char-name">{{ charStore.character.name }}</div>
            <div class="xp-track">
              <div class="xp-fill" :style="{ width: charStore.xpPercent + '%' }"></div>
            </div>
          </div>
          <div class="avatar-frame">
            <img :src="userSkinAvatar" class="pixel-focus" @error="handleAvatarError"/>
          </div>
        </router-link>
        
        </div>

    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router"; // [THÊM]
import { useAuthStore } from "../stores/authStore";
import { useCharacterStore } from "../stores/characterStore";
import { useNotificationStore } from "../stores/notificationStore";
import { getCurrentSkin } from "@/utils/assetHelper";

const router = useRouter(); // [THÊM]
const authStore = useAuthStore();
const charStore = useCharacterStore();
const notiStore = useNotificationStore();

const friendRequestCount = ref(0);

// Xử lý Avatar an toàn
const userSkinAvatar = computed(() => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return skin ? skin.sprites.idle : '/default-avatar.png'; // Fallback nếu lỗi
});

// [FIXED] Xử lý lỗi ảnh avatar để tránh lặp vô tận
const handleAvatarError = (e) => {
  const fallbackUrl = "https://placehold.co/50?text=U"; // Dùng service ổn định hơn
  
  // Nếu ảnh hiện tại đã là ảnh fallback mà vẫn lỗi, thì dừng lại (return) để không lặp
  if (e.target.src === fallbackUrl || e.target.src.includes('placehold.co')) {
    return;
  }
  
  e.target.src = fallbackUrl;
}

// Lấy Vàng từ Wallet (đảm bảo không crash nếu null)
const currentGold = computed(() => authStore.user?.wallet?.gold || 0);
const currentDiamonds = computed(() => authStore.user?.wallet?.diamonds || 0);

// [FIX] HÀM LOGOUT CHUẨN
// const handleLogout = () => {
//   if (confirm("Đại hiệp muốn quy ẩn giang hồ?")) {
//     authStore.logout(); // Xóa token
//     router.push({ name: "Login" }); // Chuyển trang (đảm bảo route tên 'Login')
//   }
// };

const formatNumber = (val) => {
  const num = Number(val);
  if (!num || isNaN(num)) return "0";
  if (num >= 1000000000) return (num / 1000000000).toFixed(1).replace(/\.0$/, "") + "B";
  if (num >= 1000000) return (num / 1000000).toFixed(1).replace(/\.0$/, "") + "M";
  if (num >= 1000) return (num / 1000).toFixed(1).replace(/\.0$/, "") + "K";
  return Math.floor(num).toString();
};

onMounted(() => {
  if (authStore.token) {
    notiStore.fetchUnreadCount();
    // Nếu App.vue chưa gọi thì gọi ở đây cho chắc
    if (!charStore.character) charStore.fetchCharacter(); 
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
  background: linear-gradient(90deg, transparent, var(--gold-accent), transparent);
}

.spacer { flex: 1; }
.hud-container { display: flex; align-items: center; gap: 15px; }

/* Resource Bank */
.resource-bank {
  display: flex;
  gap: 12px;
  align-items: center;
  background: rgba(0, 0, 0, 0.3);
  padding: 5px 12px;
  border-radius: 6px;
  border: 1px solid #5d4037;
}
.res-module { display: flex; align-items: center; gap: 6px; }
.res-icon { font-size: 1em; filter: drop-shadow(0 2px 2px rgba(0, 0, 0, 0.8)); }
.res-val { font-weight: bold; font-size: 0.95em; color: #fff; }
.res-val.small { font-size: 0.85em; min-width: 30px; text-align: right; color: #e0e0e0; }
.resource-bank .gold .res-icon { color: #ffeb3b; }
.resource-bank .gold .res-val { color: #ffd700; text-shadow: 0 0 2px #b71c1c; }
.resource-bank .jade .res-icon { color: #00e676; }
.resource-bank .jade .res-val { color: #69f0ae; }
.resource-bank .energy .res-icon { color: #2979ff; }
.energy-track {
  width: 60px; height: 8px; background: #1a1a1a;
  border: 1px solid #546e7a; border-radius: 3px; overflow: hidden;
  box-shadow: inset 0 0 3px #000;
}
.energy-bar {
  height: 100%; background: linear-gradient(90deg, #1565c0, #42a5f5);
  transition: width 0.3s ease; box-shadow: 0 0 5px #2979ff;
}

/* Icons */
.hud-icon-node {
  position: relative; width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  color: #bcaaa4; transition: 0.3s; cursor: pointer;
  border-radius: 50%; text-decoration: none;
  background: rgba(255, 255, 255, 0.05); border: 1px solid transparent;
}
.hud-icon-node:hover { color: #fbc02d; background: rgba(255, 255, 255, 0.1); border-color: #fbc02d; }
.node-badge {
  position: absolute; top: -4px; right: -4px;
  background: #d32f2f; color: #fff; font-size: 0.7em; font-weight: bold;
  padding: 2px 5px; border-radius: 10px; border: 1px solid #fff;
  min-width: 18px; text-align: center; box-shadow: 0 2px 2px rgba(0, 0, 0, 0.3);
}

/* Profile Group */
.profile-group {
  display: flex; align-items: center; gap: 10px;
  border-left: 1px solid #5d4037; padding-left: 15px;
}
.profile-link { display: flex; align-items: center; gap: 10px; text-decoration: none; transition: 0.3s; }
.profile-link:hover .char-name { color: #fbc02d; text-shadow: 0 0 5px rgba(251, 192, 45, 0.5); }
.profile-info { text-align: right; }
.char-name { font-weight: bold; font-size: 1em; color: #fdf5e6; font-family: "Playfair Display", serif; }
.xp-track { width: 80px; height: 4px; background: #1a1a1a; margin-top: 4px; margin-left: auto; border-radius: 2px; overflow: hidden; }
.xp-fill { height: 100%; background: linear-gradient(90deg, #fbc02d, #ffeb3b); transition: width 0.5s; }
.avatar-frame {
  width: 45px; height: 45px; position: relative; border-radius: 50%;
  background: #1a1510; border: 2px solid #8d6e63; box-shadow: 0 0 8px rgba(0, 0, 0, 0.6);
  overflow: hidden; display: flex; justify-content: center; align-items: center;
}
.pixel-focus { width: 130%; height: 130%; object-fit: contain; object-position: center; image-rendering: pixelated; }

/* Logout Button */
/* .btn-logout {
  background: #2b1d1a; color: #ef5350; border: 1px solid #5d4037;
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s;
}
.btn-logout:hover {
  background: #b71c1c; color: #fff; transform: scale(1.1);
  box-shadow: 0 0 10px #b71c1c;
} */

@media (max-width: 600px) {
  .mobile-hide { display: none; }
  .resource-bank { gap: 8px; padding: 4px 8px; }
  .energy-track { width: 35px; }
  .game-header { padding: 0 10px; }
}
</style>