import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

// --- Public Views ---
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ForgotPassword from '../views/ForgotPassword.vue'

// --- Game Views ---
import Character from '../views/Character.vue'
import CreateCharacter from '../views/CreateCharacter.vue'
import Village from '../views/Village.vue'
import Battle from '../views/Battle.vue'
import Combat from '../views/Combat.vue'
import Explore from '../views/Explore.vue'
import Gathering from '../views/Gathering.vue'
import Inventory from '../views/Inventory.vue'
import Marketplace from '../views/Marketplace.vue'
import Forge from '../views/Forge.vue'
import EvolveMythic from '../views/EvolveMythic.vue'
import PvpArena from '../views/PvpArena.vue'
import DumpView from '../views/DumpView.vue'

// --- Social & Info ---
import Leaderboard from '../views/Leaderboard.vue'
import Friends from '../views/Friends.vue'
import Notifications from '../views/Notifications.vue'
import Profile from '../views/Profile.vue'

// --- Admin & Static ---
import Admin from '../views/Admin.vue'
import AboutView from '../views/AboutView.vue'
import HelpView from '../views/HelpView.vue'
import PrivacyView from '../views/PrivacyView.vue'
import RulesView from '../views/RulesView.vue'
import ReportView from '../views/ReportView.vue'
import UpdatesView from '../views/UpdatesView.vue'

// [FIX] Hàm kiểm tra Token hết hạn (Client-side check)
function isTokenExpired(token) {
  if (!token) return true;
  try {
    const payload = JSON.parse(window.atob(token.split('.')[1]));
    // exp tính bằng giây, Date.now() tính bằng ms
    return payload.exp * 1000 < Date.now();
  } catch (e) {
    return true;
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'Home', component: Home, meta: { requiresAuth: true } },
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    { path: '/forgot-password', name: 'ForgotPassword', component: ForgotPassword },

    // --- Game Routes (Yêu cầu đăng nhập) ---
    { path: '/character', name: 'Character', component: Character, meta: { requiresAuth: true } },
    { path: '/create-character', name: 'CreateCharacter', component: CreateCharacter, meta: { requiresAuth: true } },
    { path: '/village', name: 'Village', component: Village, meta: { requiresAuth: true } },
    { path: '/battle', name: 'Battle', component: Battle, meta: { requiresAuth: true } },
    { path: '/combat', name: 'Combat', component: Combat, meta: { requiresAuth: true } },
    { path: '/explore', name: 'Explore', component: Explore, meta: { requiresAuth: true } },
    { path: '/gathering', name: 'Gathering', component: Gathering, meta: { requiresAuth: true } },
    { path: '/inventory', name: 'Inventory', component: Inventory, meta: { requiresAuth: true } },
    { path: '/marketplace', name: 'Marketplace', component: Marketplace, meta: { requiresAuth: true } },
    { path: '/forge', name: 'Forge', component: Forge, meta: { requiresAuth: true } },
    { path: '/evolve-mythic', name: 'EvolveMythic', component: EvolveMythic, meta: { requiresAuth: true } },
    
    { path: '/pvp', name: 'PvpArena', component: PvpArena, meta: { requiresAuth: true } },
    { path: '/dump', name: 'Dump', component: DumpView, meta: { requiresAuth: true } },

    { path: '/leaderboard', name: 'Leaderboard', component: Leaderboard, meta: { requiresAuth: true } },
    { path: '/friends', name: 'Friends', component: Friends, meta: { requiresAuth: true } },
    { path: '/notifications', name: 'Notifications', component: Notifications, meta: { requiresAuth: true } },
    { path: '/profile', name: 'Profile', component: Profile, meta: { requiresAuth: true } },
    { path: '/admin', name: 'Admin', component: Admin, meta: { requiresAuth: true, requiresAdmin: true } },

    // Static Pages
    { path: '/about', name: 'About', component: AboutView },
    { path: '/help', name: 'Help', component: HelpView },
    { path: '/privacy', name: 'Privacy', component: PrivacyView },
    { path: '/rules', name: 'Rules', component: RulesView },
    { path: '/report', name: 'Report', component: ReportView },
    { path: '/updates', name: 'Updates', component: UpdatesView },
  ]
})

router.beforeEach(async (to, from, next) => {
  const publicPages = ['/login', '/register', '/forgot-password', '/about', '/help', '/privacy', '/rules', '/updates'];
  const authRequired = !publicPages.includes(to.path);
  const authStore = useAuthStore();

  // [FIX] Nếu có Token nhưng đã HẾT HẠN -> Logout ngay lập tức để tránh vòng lặp redirect
  if (authStore.token && isTokenExpired(authStore.token)) {
    console.warn("⚠️ Token detected but expired. Logging out...");
    authStore.logout();
    return next('/login');
  }

  // Nếu cần Auth mà không có token -> Về Login
  if (authRequired && !authStore.token) {
    return next('/login');
  }

  // Nếu đã Login (và Token còn hạn) mà cố vào trang Login/Register -> Vào Village
  if (authStore.token && (to.path === '/login' || to.path === '/register')) {
    return next('/village');
  }

  next();
});

export default router