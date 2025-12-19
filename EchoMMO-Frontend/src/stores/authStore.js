import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

function getSafeUser() {
  try {
    const storedUser = localStorage.getItem("user");
    if (!storedUser || storedUser === "undefined" || storedUser === "null") return null;
    return JSON.parse(storedUser);
  } catch (e) {
    localStorage.removeItem("user");
    return null;
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: getSafeUser(),
    token: localStorage.getItem("token") || null,
    isLoading: false,
    
    // --- [NEW] TRẠNG THÁI PHONG ẤN ---
    isBanned: false, 
    banReason: "Vi phạm thiên quy", // Lý do mặc định
    banTime: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userId: (state) => state.user?.userId || state.user?.id,
    wallet: (state) => state.user?.wallet || { gold: 0, diamonds: 0 },
    isAdmin: (state) => state.user?.role === "ADMIN" || state.user?.roles?.[0] === "ADMIN",
  },

  actions: {
    async initialize() {
      const token = localStorage.getItem("token");
      if (token) {
        this.token = token;
        axiosClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
        await this.fetchProfile();
      } else {
        this.logout();
      }
    },

    async login(credentials) {
      this.isLoading = true;
      try {
        const response = await axiosClient.post("/auth/login", credentials);
        const data = response.data;
        
        this.token = data.token;
        localStorage.setItem("token", data.token);
        axiosClient.defaults.headers.common["Authorization"] = `Bearer ${data.token}`;
        
        const tempUser = {
          userId: data.id || data.userId,
          username: data.username,
          email: data.email,
          role: data.roles?.[0] || "USER",
          wallet: null 
        };
        this.user = tempUser;
        localStorage.setItem("user", JSON.stringify(tempUser));
        await this.fetchProfile();
        
        // Reset trạng thái khi đăng nhập thành công
        this.isBanned = false; 
        return true;
      } catch (err) {
        throw err;
      } finally {
        this.isLoading = false;
      }
    },

    async fetchProfile() {
      if (!this.token) return;
      try {
        const res = await axiosClient.get("/user/me");
        this.user = { ...this.user, ...res.data };
        if (!this.user.wallet) this.user.wallet = { gold: 0, diamonds: 0 };
        localStorage.setItem("user", JSON.stringify(this.user));
      } catch (error) {
         // Lỗi sẽ được axiosClient intercept xử lý
      }
    },

    // --- [NEW] KÍCH HOẠT ÁN PHẠT (Gọi từ Axios) ---
    triggerBan(reason) {
      this.isBanned = true;
      this.banReason = reason || "Tài khoản của đạo hữu đã bị phong ấn vĩnh viễn.";
      this.banTime = new Date().toLocaleString("vi-VN");
      // Lưu ý: Không xóa token ở đây để App.vue vẫn render được Overlay đè lên
    },

    logout() {
      this.token = null;
      this.user = null;
      this.isBanned = false; 
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      delete axiosClient.defaults.headers.common["Authorization"];
    },
    
    register(data) { return axiosClient.post("/auth/register", data); }
  },
});