import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import router from "../router";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    // Lấy user từ localStorage nếu có
    user: JSON.parse(localStorage.getItem("user")) || null,
    token: localStorage.getItem("token") || null,
    wallet: null,
    isLoading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) =>
      !!state.token && state.token !== "null" && state.token !== "undefined",

    // [FIX] Getter quan trọng để lấy User ID an toàn
    userId: (state) =>
      state.user?.id || state.user?.userId || state.user?.user_id,
  },

  actions: {
    // Khôi phục session khi F5
    initialize() {
      try {
        const token = localStorage.getItem("token");
        const user = localStorage.getItem("user");

        if (token && token !== "null" && token !== "undefined") {
          this.token = token;
          axiosClient.defaults.headers.common["Authorization"] =
            `Bearer ${token}`;
        } else {
          this.token = null;
          delete axiosClient.defaults.headers.common["Authorization"];
        }

        if (user && user !== "null" && user !== "undefined") {
          this.user = JSON.parse(user);
        }
      } catch (e) {
        console.error("Lỗi khôi phục session:", e);
        this.logout();
      }
    },

    async login(credentials) {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await axiosClient.post("/auth/login", credentials);

        // [FIX] Lấy đầy đủ thông tin từ Backend trả về (bao gồm ID)
        const { token, id, userId, username, role, roles } = response.data;

        // 1. Lưu Token
        this.token = token;
        localStorage.setItem("token", token);
        axiosClient.defaults.headers.common["Authorization"] =
          `Bearer ${token}`;

        // 2. Lưu User Info (Quan trọng: Phải có ID)
        const realId = id || userId;
        const realRole = role || (roles && roles[0]) || "USER";

        this.user = {
          id: realId,
          userId: realId,
          username,
          role: realRole,
        };

        localStorage.setItem("user", JSON.stringify(this.user));

        // 3. Tải thêm thông tin ví/chỉ số
        await this.fetchProfile();

        router.push("/");
      } catch (err) {
        console.error(err);
        this.error = err.response?.data?.message || "Đăng nhập thất bại!";
      } finally {
        this.isLoading = false;
      }
    },

    async fetchProfile() {
      if (!this.token) return;
      try {
        const res = await axiosClient.get("/user/me");

        // Merge thông tin cũ và mới để không mất ID
        this.user = { ...this.user, ...res.data };

        // Đảm bảo ID luôn tồn tại
        if (!this.user.userId && this.user.id) this.user.userId = this.user.id;
        if (!this.user.id && this.user.userId) this.user.id = this.user.userId;

        this.wallet = res.data.wallet;

        // Cập nhật lại localStorage
        localStorage.setItem("user", JSON.stringify(this.user));
      } catch (error) {
        console.error("Lỗi tải profile:", error);
        if (error.response && error.response.status === 401) {
          this.logout();
        }
      }
    },

    async register(registerData) {
      this.isLoading = true;
      this.error = null;
      try {
        await axiosClient.post("/auth/register", registerData);
        alert("Đăng ký thành công! Hãy đăng nhập.");
        router.push("/login");
      } catch (err) {
        this.error = err.response?.data || "Đăng ký thất bại!";
      } finally {
        this.isLoading = false;
      }
    },

    logout() {
      this.token = null;
      this.user = null;
      this.wallet = null;
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      delete axiosClient.defaults.headers.common["Authorization"];
      router.push("/login");
    },
  },
});
