import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import router from "../router";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    // User chứa tất cả: userId, username, avatarUrl, wallet...
    user: JSON.parse(localStorage.getItem("user")) || null,
    token: localStorage.getItem("token") || null,
    
    // UI states
    isLoading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) =>
      !!state.token && state.token !== "null" && state.token !== "undefined",

    // [CHUẨN HÓA] Lấy User ID an toàn (ưu tiên userId theo Entity backend)
    userId: (state) =>
      state.user?.userId || state.user?.id || state.user?.user_id,

    // [TIỆN ÍCH] Lấy nhanh ví tiền để dùng trong template
    wallet: (state) => state.user?.wallet || {},
    gold: (state) => state.user?.wallet?.gold || 0,
    diamonds: (state) => state.user?.wallet?.diamonds || 0,
  },

  actions: {
    // 1. Khôi phục session khi F5
    initialize() {
      const token = localStorage.getItem("token");
      const user = localStorage.getItem("user");

      if (token && token !== "null" && token !== "undefined") {
        this.token = token;
        this.user = user ? JSON.parse(user) : null;
        
        // Set header mặc định cho mọi request
        axiosClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
        
        // [QUAN TRỌNG] Tải lại thông tin mới nhất từ server để update vàng/ngọc
        this.fetchProfile();
      } else {
        this.logout(); // Token rác -> Xóa sạch
      }
    },

    // 2. Đăng nhập
    async login(credentials) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axiosClient.post("/auth/login", credentials);
        
        // Backend trả về: { token, type, id, username, email, roles... }
        const data = response.data; 

        // Lưu Token
        this.token = data.token;
        localStorage.setItem("token", data.token);
        axiosClient.defaults.headers.common["Authorization"] = `Bearer ${data.token}`;

        // Tạo object User tạm thời (để hiển thị ngay lập tức)
        // Lưu ý: ID backend trả về thường là 'id', nhưng Entity dùng 'userId'. Ta map cả 2.
        this.user = {
          userId: data.id, // Map id -> userId
          id: data.id,
          username: data.username,
          email: data.email,
          role: data.roles?.[0] || "USER",
          // Wallet tạm thời null, sẽ được lấp đầy bởi fetchProfile
          wallet: null 
        };
        
        localStorage.setItem("user", JSON.stringify(this.user));

        // Tải Full Info (bao gồm Wallet, Avatar)
        await this.fetchProfile();

        router.push("/");
      } catch (err) {
        console.error(err);
        this.error = err.response?.data?.message || "Đăng nhập thất bại!";
      } finally {
        this.isLoading = false;
      }
    },

    // 3. Tải thông tin chi tiết (Quan trọng nhất)
    async fetchProfile() {
      if (!this.token) return;
      try {
        // Gọi API lấy thông tin User (kèm Wallet)
        // Backend cần endpoint /user/me trả về User entity đầy đủ
        const res = await axiosClient.get("/user/me");
        
        // Merge dữ liệu mới vào state
        this.user = { ...this.user, ...res.data };
        
        // [FIX AN TOÀN] Đảm bảo cấu trúc Ví tiền tồn tại
        if (!this.user.wallet) {
            this.user.wallet = { gold: 0, diamonds: 0 };
        }

        // Lưu lại vào LocalStorage để F5 không mất
        localStorage.setItem("user", JSON.stringify(this.user));
        
      } catch (error) {
        console.error("Lỗi tải profile:", error);
        // Nếu Token hết hạn (401) -> Logout ngay
        if (error.response && error.response.status === 401) {
          this.logout();
        }
      }
    },

    // 4. Đăng ký
    async register(registerData) {
      this.isLoading = true;
      this.error = null;
      try {
        await axiosClient.post("/auth/register", registerData);
        alert("Đăng ký thành công! Hãy đăng nhập.");
        router.push("/login");
      } catch (err) {
        this.error = err.response?.data?.message || "Đăng ký thất bại!";
      } finally {
        this.isLoading = false;
      }
    },

    // 5. Đăng xuất
    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      delete axiosClient.defaults.headers.common["Authorization"];
      router.push("/login");
    },
  },
});