import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

// Hàm tiện ích: Lấy dữ liệu an toàn từ LocalStorage tránh lỗi crash JSON
function getSafeUser() {
  try {
    const storedUser = localStorage.getItem("user");
    if (!storedUser || storedUser === "undefined" || storedUser === "null") {
      return null;
    }
    return JSON.parse(storedUser);
  } catch (e) {
    console.warn("Dữ liệu user trong LocalStorage bị lỗi, đang reset...", e);
    localStorage.removeItem("user");
    return null;
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: getSafeUser(), // Dùng hàm an toàn ở trên
    token: localStorage.getItem("token") || null,
    isLoading: false,
    error: null,
  }),

  getters: {
    // Kiểm tra đã đăng nhập chưa
    isAuthenticated: (state) =>
      !!state.token && state.token !== "null" && state.token !== "undefined",

    // Lấy User ID an toàn (ưu tiên userId theo Entity backend)
    userId: (state) =>
      state.user?.userId || state.user?.id || state.user?.user_id,

    // Lấy ví tiền (xử lý null safe để không báo lỗi đỏ màn hình)
    wallet: (state) => state.user?.wallet || { gold: 0, diamonds: 0 },
    gold: (state) => state.user?.wallet?.gold || 0,
    diamonds: (state) => state.user?.wallet?.diamonds || 0,
    
    // Check quyền
    isAdmin: (state) => {
        const role = state.user?.role || state.user?.roles?.[0];
        return role === "ADMIN" || role === "ROLE_ADMIN";
    }
  },

  actions: {
    // 1. Khôi phục session khi F5 (Gọi hàm này ở App.vue hoặc main.js)
    async initialize() {
      const token = localStorage.getItem("token");
      
      if (token && token !== "null" && token !== "undefined") {
        this.token = token;
        // Set header mặc định ngay lập tức
        axiosClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
        
        // Tải lại thông tin mới nhất từ server
        await this.fetchProfile();
      } else {
        // Nếu không có token hoặc token rác -> Xóa sạch
        this.logout(); 
      }
    },

    // 2. Đăng nhập
    async login(credentials) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axiosClient.post("/auth/login", credentials);
        const data = response.data; 

        // -- Xử lý dữ liệu trả về --
        // Backend trả về: { token, id, username, email, roles... }
        
        // 1. Lưu Token
        this.token = data.token;
        localStorage.setItem("token", data.token);
        axiosClient.defaults.headers.common["Authorization"] = `Bearer ${data.token}`;

        // 2. Lưu User tạm thời (Mapping field cho khớp)
        const tempUser = {
          userId: data.id || data.userId, // Ưu tiên map về userId
          id: data.id || data.userId,
          username: data.username,
          email: data.email,
          role: data.roles?.[0] || "USER",
          wallet: null // Chưa có wallet, sẽ fetch ngay bên dưới
        };

        this.user = tempUser;
        localStorage.setItem("user", JSON.stringify(tempUser));

        // 3. Tải Full Info (Lấy ví tiền, Avatar...)
        await this.fetchProfile();
        
        // Trả về true để Component biết là login thành công -> Chuyển hướng
        return true; 

      } catch (err) {
        console.error("Login Error:", err);
        this.error = err.response?.data?.message || "Đăng nhập thất bại!";
        throw err; // Ném lỗi ra để component hiển thị thông báo
      } finally {
        this.isLoading = false;
      }
    },

    // 3. Tải thông tin chi tiết (Gọi API /user/me)
    async fetchProfile() {
      if (!this.token) return;
      try {
        const res = await axiosClient.get("/user/me");
        
        // Merge dữ liệu cũ và mới
        this.user = { ...this.user, ...res.data };
        
        // Đảm bảo wallet luôn tồn tại để không lỗi UI
        if (!this.user.wallet) {
            this.user.wallet = { gold: 0, diamonds: 0 };
        }

        // Lưu lại vào LocalStorage
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
        return true; // Báo thành công
      } catch (err) {
        this.error = err.response?.data?.message || "Đăng ký thất bại!";
        throw err;
      } finally {
        this.isLoading = false;
      }
    },

    // 5. Đăng xuất
    logout() {
      this.token = null;
      this.user = null;
      this.error = null;
      
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      
      // Xóa header
      delete axiosClient.defaults.headers.common["Authorization"];
      
      // LƯU Ý: Không dùng router.push ở đây để tránh lỗi vòng lặp.
      // Component hoặc Interceptor sẽ lo việc chuyển hướng.
      // Hoặc nếu cần kíp, dùng lệnh này để tải lại trang về Login:
      // window.location.href = "/login"; 
    },
  },
});