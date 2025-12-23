import axios from "axios";
// [FIX CIRCULAR DEPENDENCY] KHÔNG import useAuthStore ở đây để tránh vòng lặp:
// axiosClient -> authStore -> axiosClient -> ...

// [FIX] Đổi localhost -> 127.0.0.1 để tránh lỗi kết nối trên Windows/IPv6
const axiosClient = axios.create({
  baseURL: "http://127.0.0.1:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Hàm kiểm tra Token hết hạn (client-side check)
const isTokenExpired = (token) => {
  if (!token) return true;
  try {
    const payload = JSON.parse(window.atob(token.split(".")[1]));
    return payload.exp < Date.now() / 1000;
  } catch (e) {
    return true;
  }
};

// --- REQUEST INTERCEPTOR ---
axiosClient.interceptors.request.use(
  async (config) => {
    // [FIX] Sử dụng Dynamic Import để lấy store khi cần, phá vỡ vòng lặp dependency
    const { useAuthStore } = await import("../stores/authStore");
    const authStore = useAuthStore();

    if (authStore.token) {
      // Nếu token hết hạn thật sự (do thời gian), thì logout luôn
      if (isTokenExpired(authStore.token)) {
        authStore.logout();
        window.location.href = "/login";
        return Promise.reject(new Error("Token expired locally"));
      }
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// --- RESPONSE INTERCEPTOR (Xử lý Ban & Lỗi) ---
axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    // [FIX] Dynamic Import cho response interceptor
    const { useAuthStore } = await import("../stores/authStore");
    const authStore = useAuthStore();

    if (error.response) {
      const { status, data } = error.response;

      // [ƯU TIÊN 1] KIỂM TRA BANNED
      // Backend trả về 403 và JSON có chứa error="BANNED" hoặc message liên quan
      const isBannedSignal =
        status === 403 &&
        ((data && data.error === "BANNED") ||
          (data &&
            data.message &&
            data.message.toLowerCase().includes("phong ấn")));

      if (isBannedSignal) {
        console.error("⛔ TÀI KHOẢN ĐÃ BỊ BAN!");

        // Gọi action triggerBan trong store để hiện Popup
        const reason = data.message || "Vi phạm quy định thiên phủ.";
        authStore.triggerBan(reason);

        // Trả về reject nhưng KHÔNG logout ngay để giữ popup hiện trên màn hình
        return Promise.reject(error);
      }

      // [ƯU TIÊN 2] HẾT HẠN PHIÊN (401)
      if (status === 401) {
        console.warn("⚠️ Phiên đăng nhập hết hạn (401).");
        authStore.logout();
        window.location.href = "/login";
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;