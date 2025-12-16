// import { createRouter, createWebHistory } from "vue-router";
// import { useAuthStore } from "../stores/authStore";

// // Views
// import Login from "../views/Login.vue";
// import Register from "../views/Register.vue";
// import Home from "../views/Home.vue";
// import CreateCharacter from "../views/CreateCharacter.vue";
// import Game from "../views/Game.vue"; // Main World View
// import Inventory from "../views/Inventory.vue";
// import Marketplace from "../views/Marketplace.vue";
// import Leaderboard from "../views/Leaderboard.vue";
// import Admin from "../views/Admin.vue";
// import Battle from "../views/Battle.vue";
// import Profile from "../views/Profile.vue";
// import Notifications from "../views/Notifications.vue";
// import Friends from "../views/Friends.vue";
// import ForgotPassword from "../views/ForgotPassword.vue";
// import Village from "../views/Village.vue"; // <--- Import lại Village (Base)
// import Explore from "../views/Explore.vue"; // <--- Import Explore (Map)
// import Character from "../views/Character.vue";

// const routes = [
//   { path: "/", name: "Home", component: Home, meta: { requiresAuth: true } },
//   { path: "/login", name: "Login", component: Login },
//   { path: "/register", name: "Register", component: Register },
//   {
//     path: "/create-character",
//     name: "CreateCharacter",
//     component: CreateCharacter,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/village", // <--- Route Căn Cứ Địa (Safe Zone)
//     name: "Village",
//     component: Village,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/game", // <--- Có thể dùng làm Main Layout hoặc Hub tổng
//     name: "Game",
//     component: Game,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/explore", // <--- Route Chọn Map Chiến Đấu
//     name: "Explore",
//     component: Explore,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/character",
//     name: "Character",
//     component: Character,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/inventory",
//     name: "Inventory",
//     component: Inventory,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/market",
//     name: "Marketplace",
//     component: Marketplace,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/leaderboard",
//     name: "Leaderboard",
//     component: Leaderboard,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/admin",
//     name: "Admin",
//     component: Admin,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/battle",
//     name: "Battle",
//     component: Battle,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/profile",
//     name: "Profile",
//     component: Profile,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/notifications",
//     name: "Notifications",
//     component: Notifications,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/friends",
//     name: "Friends",
//     component: Friends,
//     meta: { requiresAuth: true },
//   },
//   {
//     path: "/forgot-password",
//     name: "ForgotPassword",
//     component: ForgotPassword,
//   },
// ];

// const router = createRouter({
//   history: createWebHistory(),
//   routes,
// });

// router.beforeEach((to, from, next) => {
//   const authStore = useAuthStore();
//   // Check auth cơ bản
//   if (to.meta.requiresAuth && !authStore.token) {
//     next("/login");
//   } else {
//     next();
//   }
// });

// export default router;

// import { createRouter, createWebHistory } from "vue-router";
// import { useAuthStore } from "../stores/authStore";
// import Login from "../views/Login.vue";
// import Register from "../views/Register.vue";
// import Home from "../views/Home.vue";
// import CreateCharacter from "../views/CreateCharacter.vue";
// import Inventory from "../views/Inventory.vue";
// import Marketplace from "../views/Marketplace.vue";
// import Leaderboard from "../views/Leaderboard.vue";
// import Admin from "../views/Admin.vue";
// import Battle from "../views/Battle.vue";
// import Profile from "../views/Profile.vue";
// import Notifications from "../views/Notifications.vue";
// import Friends from "../views/Friends.vue";
// import ForgotPassword from "../views/ForgotPassword.vue";
// import Village from "../views/Village.vue";
// import Gathering from "../views/Gathering.vue";
// import Explore from "../views/Explore.vue";
// import Character from "../views/Character.vue";
// import Forge from "../views/Forge.vue";

// const routes = [
//   { path: "/", name: "Home", component: Home, meta: { requiresAuth: true } },
//   { path: "/login", name: "Login", component: Login },
//   { path: "/register", name: "Register", component: Register },
//   { path: "/create-character", name: "CreateCharacter", component: CreateCharacter, meta: { requiresAuth: true } },
//   { path: "/village", name: "Village", component: Village, meta: { requiresAuth: true } },
//   { path: "/explore", name: "Explore", component: Explore, meta: { requiresAuth: true } },
//   {
//     path: '/gathering',
//     name: 'Gathering',
//     component: Gathering,
//     meta: { requiresAuth: true }
//   },
//   { path: "/inventory", name: "Inventory", component: Inventory, meta: { requiresAuth: true } },
//   { path: "/market", name: "Marketplace", component: Marketplace, meta: { requiresAuth: true } },
//   { path: "/leaderboard", name: "Leaderboard", component: Leaderboard, meta: { requiresAuth: true } },
//   { path: "/admin", name: "Admin", component: Admin, meta: { requiresAuth: true } },
//   { path: "/battle", name: "Battle", component: Battle, meta: { requiresAuth: true } },
//   { path: "/profile", name: "Profile", component: Profile, meta: { requiresAuth: true } },
//   { path: "/notifications", name: "Notifications", component: Notifications, meta: { requiresAuth: true } },
//   { path: "/friends", name: "Friends", component: Friends, meta: { requiresAuth: true } },
//   { path: "/forge", name: "Forge", component: Forge, meta: { requiresAuth: true } },
//   { path: "/forgot-password", name: "ForgotPassword", component: ForgotPassword },
//   { path: "/character", name: "Character", component: Character, meta: { requiresAuth: true } }
// ];

// const router = createRouter({
//   history: createWebHistory(),
//   routes,
// });

// router.beforeEach((to, from, next) => {
//   const authStore = useAuthStore();
//   if (to.meta.requiresAuth && !authStore.token) next("/login");
//   else next();
// });

// export default router;

// 5:28
import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/authStore";

// Import các View
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import ForgotPassword from "../views/ForgotPassword.vue";
import Home from "../views/Home.vue";
import Character from "../views/Character.vue";
import Adventure from "../views/Adventure.vue";
import Inventory from "../views/Inventory.vue";
import Marketplace from "../views/Marketplace.vue";
import Friends from "../views/Friends.vue";
import Leaderboard from "../views/Leaderboard.vue";
import Admin from "../views/Admin.vue";
import Explore from "../views/Explore.vue";
import Battle from "../views/Battle.vue";
import Combat from "../views/Combat.vue";
import Gathering from "../views/Gathering.vue";
import Forge from "../views/Forge.vue";
import CreateCharacter from "../views/CreateCharacter.vue";
import Village from "../views/Village.vue";
import Notifications from "../views/Notifications.vue";
import Profile from "../views/Profile.vue";
import AboutView from "../views/AboutView.vue";
import RulesView from "../views/RulesView.vue";
import UpdatesView from "../views/UpdatesView.vue";
import HelpView from "../views/HelpView.vue";
import ReportView from "../views/ReportView.vue";
import PrivacyView from "../views/PrivacyView.vue";

const routes = [
  // --- PUBLIC ROUTES (Không cần đăng nhập) ---
  { path: "/login", name: "Login", component: Login, meta: { guest: true } },
  {
    path: "/register",
    name: "Register",
    component: Register,
    meta: { guest: true },
  },
  {
    path: "/forgot-password",
    name: "ForgotPassword",
    component: ForgotPassword,
    meta: { guest: true },
  },

  // --- PROTECTED ROUTES (Cần đăng nhập) ---
  { path: "/", name: "Home", component: Home, meta: { requiresAuth: true } },
  {
    path: "/character",
    name: "Character",
    component: Character,
    meta: { requiresAuth: true },
  },
  {
    path: "/create-character",
    name: "CreateCharacter",
    component: CreateCharacter,
    meta: { requiresAuth: true },
  },
  {
    path: "/adventure",
    name: "Adventure",
    component: Adventure,
    meta: { requiresAuth: true },
  },
  {
    path: "/inventory",
    name: "Inventory",
    component: Inventory,
    meta: { requiresAuth: true },
  },
  {
    path: "/marketplace",
    name: "Marketplace",
    component: Marketplace,
    meta: { requiresAuth: true },
  },
  {
    path: "/friends",
    name: "Friends",
    component: Friends,
    meta: { requiresAuth: true },
  },
  {
    path: "/leaderboard",
    name: "Leaderboard",
    component: Leaderboard,
    meta: { requiresAuth: true },
  },
  {
    path: "/explore",
    name: "Explore",
    component: Explore,
    meta: { requiresAuth: true },
  },
  {
    path: "/battle",
    name: "Battle",
    component: Battle,
    meta: { requiresAuth: true },
  },
  {
    path: "/combat/:enemyId",
    name: "Combat",
    component: Combat,
    meta: { requiresAuth: true },
  },
  {
    path: "/gathering",
    name: "Gathering",
    component: Gathering,
    meta: { requiresAuth: true },
  },
  {
    path: "/forge",
    name: "Forge",
    component: Forge,
    meta: { requiresAuth: true },
  },
  {
    path: "/village",
    name: "Village",
    component: Village,
    meta: { requiresAuth: true },
  },
  {
    path: "/notifications",
    name: "Notifications",
    component: Notifications,
    meta: { requiresAuth: true },
  },
  {
    path: "/profile",
    name: "Profile",
    component: Profile,
    meta: { requiresAuth: true },
  },
  // Footer Routes - Trò Chơi
  { path: "/about", name: "about", component: AboutView },
  { path: "/rules", name: "rules", component: RulesView },
  { path: "/updates", name: "updates", component: UpdatesView },

  // Footer Routes - Hỗ Trợ
  { path: "/help", name: "help", component: HelpView },
  { path: "/report", name: "report", component: ReportView },
  { path: "/privacy", name: "privacy", component: PrivacyView },

  // --- ADMIN ROUTES ---
  {
    path: "/admin",
    name: "Admin",
    component: Admin,
    meta: { requiresAuth: true, role: "ADMIN" },
  },

  // Catch-all (404) -> Home
  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// --- NAVIGATION GUARD (Gác cổng) ---
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();

  // [FIX] Luôn đảm bảo Store được đồng bộ với LocalStorage trước khi kiểm tra
  if (!authStore.token) {
    authStore.initialize();
  }

  const isAuthenticated = authStore.isAuthenticated;

  // 1. Nếu route yêu cầu đăng nhập mà chưa có token -> Đá về Login
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next("/login");
  }

  // 2. Nếu route dành cho khách (Login/Register) mà đã đăng nhập -> Đá về Home
  if (to.meta.guest && isAuthenticated) {
    return next("/");
  }

  // 3. Kiểm tra quyền Admin (nếu cần)
  if (to.meta.role && authStore.user?.role !== to.meta.role) {
    alert("Khu vực cấm địa! Chỉ Admin mới được vào.");
    return next("/");
  }

  next();
});

export default router;
