import axios from "axios";

// 1. Cấu hình baseURL chuẩn
const axiosClient = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// 2. Hàm chuyển hướng an toàn tuyệt đối (Dùng window.location)
// Giúp tránh mọi lỗi liên quan đến import Router trong file này
const forceLogout = () => {
  localStorage.removeItem("token"); // Hoặc logic xóa token của bạn
  window.location.href = "/login";
};

// 3. Hàm check token đơn giản
const isTokenExpired = (token) => {
  if (!token) return true;
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(window.atob(base64));
    return payload.exp * 1000 < Date.now();
  } catch (e) {
    return true;
  }
};

// --- REQUEST INTERCEPTOR ---
axiosClient.interceptors.request.use(
  async (config) => {
    // [QUAN TRỌNG] Try-catch đoạn import Store để tránh sập nếu sai đường dẫn
    let token = null;
    try {
      // Sửa đường dẫn này cho đúng vị trí file store của bạn
      // Nếu file axios nằm cùng cấp thư mục cha với stores thì dùng ../
      const { useAuthStore } = await import("../stores/authStore");
      const authStore = useAuthStore();
      token = authStore.token;

      if (token && isTokenExpired(token)) {
        console.warn("⚠️ Token hết hạn (Client). Logout ngay.");
        authStore.logout(); // Xóa state
        forceLogout();      // Chuyển trang
        return Promise.reject(new Error("Token expired")); // Ngắt request
      }
    } catch (err) {
      // Nếu import lỗi, bỏ qua check token client-side để tránh lỗi Promise
      console.error("Lỗi check token ở axios:", err);
    }

    if (token) {
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
    if (error.response) {
      const { status, data } = error.response;

      // Xử lý BAN
      const isBanned = status === 403 && 
        ((data?.error === "BANNED") || (data?.message?.toLowerCase().includes("phong ấn")));

      if (isBanned) {
        // Cố gắng import store để hiện popup ban
        try {
            const { useAuthStore } = await import("../stores/authStore");
            const authStore = useAuthStore();
            authStore.triggerBan(data.message || "Bị phong ấn.");
        } catch (e) {
            console.error("Không gọi được store để hiện ban:", e);
        }
        return Promise.reject(error);
      }

      // Xử lý 401 (Hết hạn login)
      if (status === 401) {
        console.warn("⚠️ 401 Unauthorized -> Logout");
        // Gọi logout từ store nếu được, không thì force logout luôn
        try {
            const { useAuthStore } = await import("../stores/authStore");
            const authStore = useAuthStore();
            authStore.logout();
        } catch (e) {}
        
        forceLogout();
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;