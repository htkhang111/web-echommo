import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

// Hàm tiện ích: Lấy JSON từ LocalStorage an toàn (tránh lỗi cú pháp)
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
    // [QUAN TRỌNG] Lưu thông tin nhân vật (Level, HP, Power...)
    character: getSafeJSON("character"), 
    token: localStorage.getItem("token") || null,
    isLoading: false,
    
    // --- TRẠNG THÁI PHONG ẤN (BAN) ---
    isBanned: false, 
    banReason: "Vi phạm thiên quy",
    banTime: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userId: (state) => state.user?.userId || state.user?.id,
    
    // Lấy ví tiền an toàn (tránh lỗi null)
    wallet: (state) => state.user?.wallet || { gold: 0, diamonds: 0 },
    
    isAdmin: (state) => state.user?.role === "ADMIN" || state.user?.roles?.[0] === "ADMIN",

    // [TIỆN ÍCH] Getter lấy Header Auth nhanh gọn cho các file Vue khác
    authHeader: (state) => ({
      Authorization: `Bearer ${state.token}`,
      "Content-Type": "application/json"
    }),
  },

  actions: {
    // 1. Khởi tạo khi load trang (F5)
    async initialize() {
      const token = localStorage.getItem("token");
      if (token) {
        this.token = token;
        // Cài đặt header mặc định cho axios
        axiosClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
        
        // Gọi song song: Lấy thông tin User + Lấy thông tin Nhân vật mới nhất
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
        axiosClient.defaults.headers.common["Authorization"] = `Bearer ${data.token}`;
        
        // B. Lưu User cơ bản
        const tempUser = {
          userId: data.id || data.userId,
          username: data.username,
          email: data.email,
          role: data.roles?.[0] || "USER",
          wallet: null 
        };
        this.user = tempUser;
        localStorage.setItem("user", JSON.stringify(tempUser));
        
        // C. Gọi API lấy dữ liệu đầy đủ
        await this.fetchProfile();
        await this.fetchCharacter(); // <--- Lấy Level, Power ngay lập tức
        
        this.isBanned = false; 
        return true;
      } catch (err) {
        throw err;
      } finally {
        this.isLoading = false;
      }
    },

    // 3. Lấy Profile User (Ví tiền, thông tin tài khoản)
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

    // 4. [QUAN TRỌNG] Lấy thông tin Nhân vật (Level, Power, HP...)
    async fetchCharacter() {
        if (!this.token) return;
        try {
            // Gọi đúng endpoint trong CharacterController: /api/character/me
            const res = await axiosClient.get("/character/me"); 
            
            this.character = res.data;
            // Lưu vào LocalStorage để F5 không bị mất
            localStorage.setItem("character", JSON.stringify(this.character));
        } catch (error) {
            console.warn("Chưa tạo nhân vật hoặc lỗi fetch character");
            this.character = null;
        }
    },

    // 5. Kích hoạt án phạt (Hiển thị màn hình khóa)
    triggerBan(reason) {
      this.isBanned = true;
      this.banReason = reason || "Tài khoản của đạo hữu đã bị phong ấn vĩnh viễn.";
      this.banTime = new Date().toLocaleString("vi-VN");
    },

    // 6. Đăng xuất
    logout() {
      this.token = null;
      this.user = null;
      this.character = null; // Xóa character
      this.isBanned = false; 
      
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      localStorage.removeItem("character"); // Xóa sạch storage
      
      delete axiosClient.defaults.headers.common["Authorization"];
    },
    
    register(data) { return axiosClient.post("/auth/register", data); }
  },
});