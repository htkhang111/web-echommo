<template>
  <div
    class="app-layout wuxia-theme"
    :style="{ '--sidebar-width': isCollapsed ? '80px' : '280px' }"
  >
    <div class="ink-bg-layer">
      <div
        class="mountain-bg"
        :style="{ backgroundImage: `url(${bgImage})` }"
      ></div>
      <div class="fog-overlay"></div>
    </div>

    <div
      class="sidebar-backdrop mobile-only"
      v-if="isMobileMenuOpen"
      @click="toggleMobileMenu"
    ></div>

    <aside
      class="sidebar"
      :class="{ collapsed: isCollapsed, 'mobile-open': isMobileMenuOpen }"
    >
      <div class="sidebar-texture"></div>

      <div class="logo-area">
        <div class="logo-seal">
          <img :src="appLogo" alt="Logo" class="seal-image" />
          <div class="seal-glow"></div>
        </div>
        <transition name="fade">
          <div v-if="!isCollapsed" class="logo-text">
            <span class="brand-text"
              >ECHO<span class="highlight">MMO</span></span
            >
            <span class="brand-subtitle">Võ Lâm Tranh Bá</span>
          </div>
        </transition>
      </div>

      <nav class="nav-links custom-scroll">
        <div class="nav-group-label" v-if="!isCollapsed">
          <span class="line"></span><span>THIÊN ĐỊA</span
          ><span class="line"></span>
        </div>

        <router-link to="/" class="nav-item" @click="closeMobileMenu">
          <div class="nav-icon"><i class="fas fa-dungeon"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Sảnh Chính</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>
        <router-link
          to="/leaderboard"
          class="nav-item"
          @click="closeMobileMenu"
        >
          <div class="nav-icon"><i class="fas fa-crown"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Thiên Thư</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>

        <div class="nav-group-label" v-if="!isCollapsed">
          <span class="line"></span><span>GIANG HỒ</span
          ><span class="line"></span>
        </div>

        <router-link to="/explore" class="nav-item" @click="closeMobileMenu">
          <div class="nav-icon"><i class="fas fa-map-marked-alt"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Hành Tẩu</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>
        <router-link to="/pvp" class="nav-item" @click="closeMobileMenu">
          <div class="nav-icon"><i class="fas fa-fist-raised"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Lôi Đài</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>

        <router-link to="/village" class="nav-item" @click="closeMobileMenu">
          <div class="nav-icon"><i class="fas fa-campground"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Doanh Trại</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>
        <router-link
          to="/marketplace"
          class="nav-item"
          @click="closeMobileMenu"
        >
          <div class="nav-icon"><i class="fas fa-coins"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Thương Hội</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>


        
        <router-link to="/dump" class="nav-item" @click="closeMobileMenu">
          <div class="nav-icon"><i class="fas fa-scroll"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Tàng Kinh Các</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>
        
        <div class="nav-group-label" v-if="!isCollapsed">
          <span class="line"></span><span>HÀNH TRANG</span
          ><span class="line"></span>
        </div>

        <router-link to="/inventory" class="nav-item" @click="closeMobileMenu">
          <div class="nav-icon"><i class="fas fa-suitcase"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Túi Đồ</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>

        <router-link to="/character" class="nav-item" @click="closeMobileMenu">
          <div class="nav-icon"><i class="fas fa-user-shield"></i></div>
          <transition name="slide-fade"
            ><span v-if="!isCollapsed" class="nav-label"
              >Trang Bị</span
            ></transition
          >
          <div class="active-bar"></div>
        </router-link>

        
        <template v-if="authStore.user?.role === 'ADMIN'">
          <div class="nav-group-label admin" v-if="!isCollapsed">
            <span class="line red"></span><span>TRIỀU ĐÌNH</span
            ><span class="line red"></span>
          </div>
          <router-link
            to="/admin"
            class="nav-item admin-link"
            @click="closeMobileMenu"
          >
            <div class="nav-icon"><i class="fas fa-dragon"></i></div>
            <transition name="slide-fade"
              ><span v-if="!isCollapsed" class="nav-label"
                >Quan Phủ</span
              ></transition
            >
            <div class="active-bar red"></div>
          </router-link>
        </template>
      </nav>

      <div class="sidebar-footer">
        <button class="control-btn toggle desktop-only" @click="toggleSidebar">
          <i
            :class="
              isCollapsed ? 'fas fa-chevron-right' : 'fas fa-chevron-left'
            "
          ></i>
        </button>
        <button class="control-btn logout" @click="handleLogout">
          <i class="fas fa-power-off"></i>
          <span v-if="!isCollapsed" class="btn-text">Quy Ẩn</span>
        </button>
      </div>
    </aside>

    <div class="content-wrapper">
      <GameHeader @toggle-sidebar="toggleMobileMenu" />

      <main class="main-view custom-scroll">
        <div class="page-body">
          <slot></slot>
        </div>

        <GameFooter />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/authStore";
import GameHeader from "../components/GameHeader.vue";
import GameFooter from "../components/GameFooter.vue";
import { getAppLogo, getAssetUrl } from "@/utils/assetHelper";

const authStore = useAuthStore();
const router = useRouter();
const isCollapsed = ref(false);
const isMobileMenuOpen = ref(false);

const appLogo = getAppLogo();
const bgImage = getAssetUrl("b_doanhtrai.png");

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
};
const closeMobileMenu = () => {
  isMobileMenuOpen.value = false;
};

const handleLogout = () => {
  if (confirm("Đại hiệp muốn quy ẩn giang hồ?")) {
    authStore.logout();
    router.push({ name: "Login" });
  }
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@400;500;700&display=swap");

/* --- CORE THEME VARIABLES --- */
.wuxia-theme {
  --sidebar-bg-start: #14100f;
  --sidebar-bg-end: #2d1f18;
  --sidebar-border: rgba(255, 215, 0, 0.2);

  --text-main: #fdf5e6;
  --text-dim: #9e9e9e;

  --gold-primary: #ffca28;
  --gold-glow: rgba(255, 202, 40, 0.6);
  --red-royal: #d32f2f;

  --hover-bg: linear-gradient(
    90deg,
    rgba(255, 215, 0, 0.1) 0%,
    transparent 100%
  );
  --active-bg: linear-gradient(
    90deg,
    rgba(255, 215, 0, 0.2) 0%,
    transparent 100%
  );

  display: flex;
  min-height: 100vh;
  background-color: #000;
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  overflow: hidden;
}

/* --- BACKGROUNDS --- */
.ink-bg-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
  background-color: #050505;
}
.mountain-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  filter: sepia(40%) grayscale(30%) brightness(0.25) contrast(1.2);
  opacity: 0.6;
  transition: all 1s ease;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.9), rgba(0, 0, 0, 0.4));
  pointer-events: none;
}

/* --- SIDEBAR CONTAINER --- */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  background: linear-gradient(
    180deg,
    var(--sidebar-bg-start) 0%,
    var(--sidebar-bg-end) 100%
  );
  border-right: 1px solid var(--sidebar-border);
  box-shadow: 10px 0 30px rgba(0, 0, 0, 0.8);
  transition:
    transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1),
    width 0.4s ease;
}

/* Texture Pattern (Vảy Rồng mờ) */
.sidebar-texture {
  position: absolute;
  inset: 0;
  opacity: 0.05;
  pointer-events: none;
  background-image: radial-gradient(#d4af37 1px, transparent 1px);
  background-size: 20px 20px;
}

/* --- LOGO --- */
.logo-area {
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--sidebar-border);
  background: rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
}

.logo-seal {
  width: 50px;
  height: 50px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}
.seal-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  position: relative;
  z-index: 2;
}
.seal-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(circle, var(--gold-glow) 0%, transparent 70%);
  opacity: 0.5;
  animation: pulse 3s infinite;
}

.logo-text {
  flex: 1;
  margin-left: 15px;
  display: flex;
  flex-direction: column;
  white-space: nowrap;
  overflow: hidden;
}
.brand-text {
  font-family: "Cinzel", serif;
  font-size: 1.6rem;
  font-weight: 900;
  letter-spacing: 2px;
  background: linear-gradient(to bottom, #fff, var(--gold-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 5px rgba(255, 215, 0, 0.5));
}
.brand-subtitle {
  font-size: 0.7rem;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 3px;
  margin-top: -2px;
}

/* --- NAVIGATION --- */
.nav-links {
  flex: 1;
  padding: 20px 10px;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  z-index: 2;
}

/* Group Label */
.nav-group-label {
  padding: 15px 5px 5px;
  font-size: 0.65rem;
  color: #6d4c41;
  font-weight: bold;
  letter-spacing: 2px;
  font-family: "Cinzel", serif;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.nav-group-label .line {
  height: 1px;
  background: #3e2723;
  flex: 1;
}
.nav-group-label.admin {
  color: #e57373;
}
.nav-group-label.admin .line.red {
  background: #5a1a1a;
}

/* Menu Item */
.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 14px 18px;
  color: var(--text-dim);
  text-decoration: none;
  margin-bottom: 4px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.nav-icon {
  width: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.1rem;
  color: #757575;
  transition: 0.3s;
  z-index: 2;
}
.nav-label {
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 0.5px;
  z-index: 2;
  white-space: nowrap;
  transition: 0.3s;
}

/* Hover Effect */
.nav-item:hover {
  background: var(--hover-bg);
  color: #fff;
  transform: translateX(5px);
}
.nav-item:hover .nav-icon {
  color: var(--gold-primary);
  filter: drop-shadow(0 0 5px var(--gold-primary));
  transform: scale(1.1);
}

/* Active State */
.nav-item.router-link-active {
  background: var(--active-bg);
  color: #fff;
}
.nav-item.router-link-active .nav-icon {
  color: var(--gold-primary);
  text-shadow: 0 0 10px var(--gold-primary);
}
.nav-item.router-link-active .nav-label {
  font-weight: 700;
  color: #fff;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.5);
}

/* Magic Bar (Active Indicator) */
.active-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(
    to bottom,
    transparent,
    var(--gold-primary),
    transparent
  );
  box-shadow: 2px 0 10px var(--gold-primary);
  opacity: 0;
  transition: opacity 0.3s;
}
.nav-item.router-link-active .active-bar {
  opacity: 1;
}

/* Admin Link Specifics */
.admin-link.router-link-active .nav-icon {
  color: var(--red-royal);
  text-shadow: 0 0 10px var(--red-royal);
}
.admin-link.router-link-active .active-bar.red {
  background: linear-gradient(
    to bottom,
    transparent,
    var(--red-royal),
    transparent
  );
  box-shadow: 2px 0 10px var(--red-royal);
}

/* --- FOOTER --- */
.sidebar-footer {
  padding: 20px;
  border-top: 1px solid var(--sidebar-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: rgba(0, 0, 0, 0.4);
}
.control-btn {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid #3e2723;
  color: var(--text-dim);
  padding: 12px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s;
  font-family: "Cinzel", serif;
  font-size: 0.9rem;
}
.control-btn:hover {
  background: rgba(255, 215, 0, 0.05);
  border-color: var(--gold-primary);
  color: var(--gold-primary);
}
.logout:hover {
  border-color: var(--red-royal);
  color: var(--red-royal);
  background: rgba(211, 47, 47, 0.05);
}

/* --- LAYOUT & SCROLLBAR --- */
.content-wrapper {
  flex: 1;
  margin-left: var(--sidebar-width);
  transition: margin-left 0.4s ease;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  height: 100vh;
}
.main-view {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}
.page-body {
  flex: 1 0 auto;
  width: 100%;
  position: relative;
}

.custom-scroll::-webkit-scrollbar {
  width: 4px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #4e342e;
  border-radius: 2px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: transparent;
}

/* --- MOBILE RESPONSIVE --- */
/* [REMOVED] CSS close-sidebar-btn đã bị xóa */
.sidebar-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 999;
  backdrop-filter: blur(3px);
  animation: fadeIn 0.3s;
}

@media (max-width: 1024px) {
  .sidebar {
    width: 280px;
    transform: translateX(-100%);
    border-right: 1px solid var(--gold-primary);
  }
  .sidebar.mobile-open {
    transform: translateX(0);
  }
  .content-wrapper {
    margin-left: 0;
  }
  .desktop-only {
    display: none;
  }
  .mobile-only {
    display: block;
  }
}

@keyframes pulse {
  0% {
    opacity: 0.3;
    transform: scale(0.9);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.1);
  }
  100% {
    opacity: 0.3;
    transform: scale(0.9);
  }
}
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.1s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(10px);
  opacity: 0;
}
</style>




