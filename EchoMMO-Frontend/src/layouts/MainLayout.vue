<template>
  <div class="app-layout wuxia-theme" :style="{ '--sidebar-width': isCollapsed ? '80px' : '260px' }">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-overlay"></div>
    </div>

    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo-area">
        <div class="logo-seal"><img :src="appLogo" alt="Logo" class="seal-image" /></div>
        <transition name="fade"><div v-if="!isCollapsed" class="logo-text"><span class="brand-text">ECHOMMO</span></div></transition>
      </div>

      <nav class="nav-links custom-scroll">
        <router-link to="/" class="nav-item">
          <div class="nav-icon"><i class="fas fa-dungeon"></i></div>
          <transition name="slide-fade"><span v-if="!isCollapsed" class="nav-label">SẢNH CHÍNH</span></transition>
          <div class="active-glow"></div>
        </router-link>

        <div class="nav-group-label" v-if="!isCollapsed">GIANG HỒ</div>
        <router-link to="/explore" class="nav-item">
          <div class="nav-icon"><i class="fas fa-map-marked-alt"></i></div>
          <transition name="slide-fade"><span v-if="!isCollapsed" class="nav-label">HÀNH TẨU</span></transition>
          <div class="active-glow"></div>
        </router-link>
        
        <router-link to="/village" class="nav-item">
          <div class="nav-icon"><i class="fas fa-campground"></i></div>
          <transition name="slide-fade"><span v-if="!isCollapsed" class="nav-label">DOANH TRẠI</span></transition>
          <div class="active-glow"></div>
        </router-link>

        <div class="nav-group-label" v-if="!isCollapsed">HÀNH TRANG</div>
        <router-link to="/inventory" class="nav-item">
          <div class="nav-icon"><i class="fas fa-suitcase"></i></div>
          <transition name="slide-fade"><span v-if="!isCollapsed" class="nav-label">TÚI ĐỒ</span></transition>
          <div class="active-glow"></div>
        </router-link>

        <router-link to="/character" class="nav-item">
          <div class="nav-icon"><i class="fas fa-user-shield"></i></div>
          <transition name="slide-fade"><span v-if="!isCollapsed" class="nav-label">TRANG BỊ</span></transition>
          <div class="active-glow"></div>
        </router-link>

        <router-link to="/marketplace" class="nav-item">
          <div class="nav-icon"><i class="fas fa-coins"></i></div>
          <transition name="slide-fade"><span v-if="!isCollapsed" class="nav-label">THƯƠNG HỘI</span></transition>
          <div class="active-glow"></div>
        </router-link>

        <router-link to="/leaderboard" class="nav-item">
          <div class="nav-icon"><i class="fas fa-crown"></i></div>
          <transition name="slide-fade"><span v-if="!isCollapsed" class="nav-label">BẢNG VÀNG</span></transition>
          <div class="active-glow"></div>
        </router-link>

        <template v-if="authStore.user?.role === 'ADMIN'">
          <div class="nav-group-label admin" v-if="!isCollapsed">TRIỀU ĐÌNH</div>
          <router-link to="/admin" class="nav-item admin-link">
            <div class="nav-icon"><i class="fas fa-dragon"></i></div>
            <transition name="slide-fade"><span v-if="!isCollapsed" class="nav-label">QUAN PHỦ</span></transition>
            <div class="active-glow"></div>
          </router-link>
        </template>
      </nav>

      <div class="sidebar-footer">
        <button class="control-btn toggle" @click="toggleSidebar">
          <i :class="isCollapsed ? 'fas fa-chevron-right' : 'fas fa-chevron-left'"></i>
        </button>
        <button class="control-btn logout" @click="handleLogout">
          <i class="fas fa-power-off"></i>
          <span v-if="!isCollapsed" class="btn-text">QUY ẨN</span>
        </button>
      </div>
    </aside>

    <div class="content-wrapper">
      <GameHeader />
      <main class="main-view custom-scroll" ref="mainScroll">
        <div class="page-body"><slot></slot></div>
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
import appLogo from "@/assets/logo/Logo.png";

const authStore = useAuthStore();
const router = useRouter();
const isCollapsed = ref(false);
const mainScroll = ref(null);

const toggleSidebar = () => { isCollapsed.value = !isCollapsed.value; };

const handleLogout = () => {
  if (confirm("Đại hiệp muốn quy ẩn giang hồ?")) {
    authStore.logout();
    router.push({ name: 'Login' }); // Chuyển về trang đăng nhập
  }
};
</script>

<style scoped>
/* CSS GIỮ NGUYÊN NHƯ CŨ CỦA ÔNG - KHÔNG CẦN THAY ĐỔI STYLE */
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+TC:wght@500;700;900&family=Orbitron:wght@400;700&display=swap");
.wuxia-theme { --sidebar-bg: #3e2723; --sidebar-border: #5d4037; --text-main: #fdf5e6; --text-dim: #d7ccc8; --accent-gold: #fbc02d; --accent-red: #b71c1c; --hover-bg: rgba(255, 255, 255, 0.08); --bg-deep: #0a0706; display: flex; min-height: 100vh; background-color: #000; color: var(--text-main); font-family: "Noto Serif TC", serif; overflow: hidden; }
.ink-bg-layer { position: fixed; inset: 0; z-index: 0; background-color: var(--bg-deep); }
.mountain-bg { position: absolute; inset: 0; background-image: url("@/assets/Background/b_doanhtrai.png"); background-size: cover; filter: sepia(30%) grayscale(20%) brightness(0.3); opacity: 0.5; }
.fog-overlay { position: absolute; inset: 0; background: linear-gradient( to right, rgba(10, 7, 6, 0.95), rgba(10, 7, 6, 0.5) ); pointer-events: none; }
.sidebar { position: fixed; top: 0; left: 0; bottom: 0; width: var(--sidebar-width); z-index: 1000; display: flex; flex-direction: column; transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); background: var(--sidebar-bg); border-right: 2px solid var(--sidebar-border); box-shadow: 5px 0 20px rgba(0, 0, 0, 0.6); }
.logo-area { height: 60px; display: flex; align-items: center; padding: 0 15px; border-bottom: 2px solid var(--sidebar-border); gap: 12px; background: rgba(0, 0, 0, 0.2); }
.logo-seal { width: 65px; height: 96px; background: transparent; border: none; box-shadow: none; display: flex; align-items: center; justify-content: center; flex-shrink: 0; overflow: visible; }
.seal-image { width: 100%; height: 100%; object-fit: contain; filter: drop-shadow(0 0 5px rgba(251, 192, 45, 0.5)); }
.logo-text { flex: 1; overflow: hidden; white-space: nowrap; display: flex; align-items: center; }
.brand-text { font-family: "Cinzel", serif; font-size: 1.5em; font-weight: 800; letter-spacing: 1px; color: #ffffff; text-shadow: 0 0 10px rgba(251, 192, 45, 0.4); }
.nav-links { flex: 1; padding: 20px 10px; overflow-y: auto; overflow-x: hidden; }
.nav-group-label { padding: 15px 15px 5px; font-size: 0.75em; color: #a1887f; font-weight: bold; letter-spacing: 1.5px; margin-bottom: 5px; font-family: "Cinzel", serif; text-shadow: 0 1px 1px rgba(0, 0, 0, 0.8); }
.nav-group-label.admin { color: var(--accent-red); }
.nav-item { position: relative; display: flex; align-items: center; gap: 15px; padding: 12px 15px; color: var(--text-dim); text-decoration: none; margin-bottom: 5px; border-radius: 6px; transition: all 0.3s ease; font-family: "Noto Serif TC", serif; font-weight: 700; }
.nav-icon { width: 24px; text-align: center; font-size: 1.1em; display: flex; justify-content: center; align-items: center; flex-shrink: 0; transition: transform 0.3s; color: #bcaaa4; }
.nav-label { font-weight: 600; white-space: nowrap; font-size: 0.9em; letter-spacing: 0.5px; }
.active-glow { position: absolute; left: 0; top: 15%; bottom: 15%; width: 3px; background: var(--accent-gold); border-radius: 0 4px 4px 0; transform: scaleY(0); transition: 0.3s; box-shadow: 2px 0 10px var(--accent-gold); }
.nav-item:hover { background: var(--hover-bg); color: #fff; }
.nav-item:hover .nav-icon { color: var(--accent-gold); transform: scale(1.1); text-shadow: 0 0 5px rgba(251, 192, 45, 0.6); }
.nav-item.router-link-active { background: linear-gradient(90deg, rgba(251, 192, 45, 0.15), transparent); color: #fff; text-shadow: 0 0 8px rgba(251, 192, 45, 0.4); }
.nav-item.router-link-active .nav-icon { color: var(--accent-gold); }
.nav-item.router-link-active .active-glow { transform: scaleY(1); }
.admin-link.router-link-active { background: linear-gradient(90deg, rgba(183, 28, 28, 0.2), transparent); }
.admin-link.router-link-active .nav-icon { color: var(--accent-red); }
.admin-link.router-link-active .active-glow { background: var(--accent-red); box-shadow: 2px 0 10px var(--accent-red); }
.sidebar-footer { padding: 15px; border-top: 2px solid var(--sidebar-border); display: flex; flex-direction: column; gap: 10px; background: rgba(0, 0, 0, 0.2); }
.control-btn { background: rgba(255, 255, 255, 0.05); border: 1px solid var(--sidebar-border); color: var(--text-dim); padding: 10px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 10px; transition: 0.3s; font-family: "Cinzel", serif; font-weight: bold; }
.control-btn:hover { background: rgba(255, 255, 255, 0.1); color: var(--accent-gold); border-color: var(--accent-gold); }
.logout:hover { border-color: var(--accent-red); color: var(--accent-red); background: rgba(183, 28, 28, 0.1); }
.content-wrapper { flex: 1; margin-left: var(--sidebar-width); transition: margin-left 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); display: flex; flex-direction: column; position: relative; z-index: 1; height: 100vh; }
.main-view { flex: 1; overflow-y: auto; overflow-x: hidden; display: flex; flex-direction: column; position: relative; }
.page-body { flex: 1 0 auto; display: flex; flex-direction: column; width: 100%; position: relative; }
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: var(--sidebar-border); border-radius: 2px; }
.custom-scroll::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); }
</style>