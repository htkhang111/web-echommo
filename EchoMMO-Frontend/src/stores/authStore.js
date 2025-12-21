import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

// Hàm tiện ích: Lấy JSON từ LocalStorage an toàn
function getSafeJSON(key) {
  try {
    const data = localStorage.getItem(key);
    if (!data || data === "undefined" || data === "null") return null;
    return JSON.parse(data);
  } catch (e) {
    localStorage.removeItem(key);
    return null;
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: getSafeJSON("user"),
    // [QUAN TRỌNG] Chứa level, pvpWins, avatarUrl...
    character: getSafeJSON("character"),
    token: localStorage.getItem("token") || null,
    isLoading: false,

    // --- TRẠNG THÁI BAN ---
    isBanned: false,
    banReason: "Vi phạm thiên quy",
    banTime: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userId: (state) => state.user?.userId || state.user?.id,

    // Lấy ví tiền an toàn
    wallet: (state) => state.user?.wallet || { gold: 0, diamonds: 0 },

    isAdmin: (state) =>
      state.user?.role === "ADMIN" || state.user?.roles?.[0] === "ADMIN",

    // Getter lấy Header Auth
    authHeader: (state) => ({
      Authorization: `Bearer ${state.token}`,
      "Content-Type": "application/json",
    }),
  },

  actions: {
    // 1. Khởi tạo (F5 trang)
    async initialize() {
      const token = localStorage.getItem("token");
      if (token) {
        this.token = token;
        axiosClient.defaults.headers.common["Authorization"] =
          `Bearer ${token}`;

        // Gọi song song để tiết kiệm thời gian load
        await Promise.all([this.fetchProfile(), this.fetchCharacter()]);
      } else {
        this.logout();
      }
    },

    // 2. Đăng nhập
    async login(credentials) {
      this.isLoading = true;
      try {
        const response = await axiosClient.post("/auth/login", credentials);
        const data = response.data;

        // A. Lưu Token
        this.token = data.token;
        localStorage.setItem("token", data.token);
        axiosClient.defaults.headers.common["Authorization"] =
          `Bearer ${data.token}`;

        // B. Lưu User cơ bản
        const tempUser = {
          userId: data.id || data.userId,
          username: data.username,
          email: data.email,
          role: data.roles?.[0] || "USER",
          wallet: null,
        };
        this.user = tempUser;
        localStorage.setItem("user", JSON.stringify(tempUser));

        // C. Lấy dữ liệu chi tiết ngay lập tức
        await this.fetchProfile();
        await this.fetchCharacter();

        this.isBanned = false;
        return true;
      } catch (err) {
        throw err;
      } finally {
        this.isLoading = false;
      }
    },

    // 3. Lấy Profile (Ví tiền, Account)
    async fetchProfile() {
      if (!this.token) return;
      try {
        const res = await axiosClient.get("/user/me");
        this.user = { ...this.user, ...res.data };
        if (!this.user.wallet) this.user.wallet = { gold: 0, diamonds: 0 };
        localStorage.setItem("user", JSON.stringify(this.user));
      } catch (error) {
        console.error("Lỗi fetch profile:", error);
      }
    },

    // 4. [QUAN TRỌNG] Lấy Character (Stats, Wins, Level)
    async fetchCharacter() {
      if (!this.token) return;
      try {
        const res = await axiosClient.get("/character/me");
        this.character = res.data;
        // Lưu ngay vào LocalStorage để F5 không mất số trận thắng
        localStorage.setItem("character", JSON.stringify(this.character));
      } catch (error) {
        console.warn("Chưa tạo nhân vật hoặc lỗi API Character");
        this.character = null;
      }
    },

    // 5. Cập nhật nhanh số liệu (Không cần gọi API) - Dùng khi kết thúc trận
    updateLocalStats(newStats) {
      if (this.character) {
        this.character = { ...this.character, ...newStats };
        localStorage.setItem("character", JSON.stringify(this.character));
      }
    },

    // 6. Xử lý Ban
    triggerBan(reason) {
      this.isBanned = true;
      this.banReason = reason || "Tài khoản bị khóa.";
      this.banTime = new Date().toLocaleString("vi-VN");
    },

    // 7. Đăng xuất
    logout() {
      this.token = null;
      this.user = null;
      this.character = null;
      this.isBanned = false;

      localStorage.removeItem("token");
      localStorage.removeItem("user");
      localStorage.removeItem("character");

      delete axiosClient.defaults.headers.common["Authorization"];
    },

    register(data) {
      return axiosClient.post("/auth/register", data);
    },
  },
});
