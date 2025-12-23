import axios from "axios";

// [FIX] URL Backend
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
    // exp là seconds, Date.now() là milliseconds
    return payload.exp < Date.now() / 1000;
  } catch (e) {
    return true;
  }
};

// --- REQUEST INTERCEPTOR ---
// [FIX] Cắt bỏ phụ thuộc vào authStore ở đây để tránh Circular Dependency & Stack Overflow
axiosClient.interceptors.request.use(
  (config) => {
    // 1. Lấy token trực tiếp từ LocalStorage (Nhanh & An toàn)
    const token = localStorage.getItem("token");

    if (token) {
      // 2. Kiểm tra hết hạn client-side
      if (isTokenExpired(token)) {
        // Xóa token và redirect thủ công
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        localStorage.removeItem("character");
        window.location.href = "/login";
        return Promise.reject(new Error("Token expired locally"));
      }
      
      // 3. Gắn Header
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// --- RESPONSE INTERCEPTOR ---
axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Chỉ import store khi có lỗi xảy ra (Lazy Load)
    try {
      if (error.response) {
        const { status, data } = error.response;
        
        // Dynamic import để tránh lỗi khởi tạo
        const { useAuthStore } = await import("../stores/authStore");
        const authStore = useAuthStore();

        // [ƯU TIÊN 1] BANNED
        const isBannedSignal =
          status === 403 &&
          ((data && data.error === "BANNED") ||
            (data && data.message && data.message.toLowerCase().includes("phong ấn")));

        if (isBannedSignal) {
          const reason = data.message || "Vi phạm quy định thiên phủ.";
          authStore.triggerBan(reason);
          return Promise.reject(error);
        }

        // [ƯU TIÊN 2] HẾT HẠN PHIÊN (401)
        if (status === 401) {
          console.warn("⚠️ Phiên đăng nhập hết hạn (401).");
          authStore.logout();
          
          // Dùng window.location cho chắc ăn khi Axios đang lỗi
          if (window.location.pathname !== '/login') {
             window.location.href = "/login";
          }
          return Promise.reject(error);
        }
      }
    } catch (e) {
      console.error("Lỗi trong Interceptor:", e);
    }

    return Promise.reject(error);
  }
);

export default axiosClient;