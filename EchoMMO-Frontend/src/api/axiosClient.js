import axios from "axios";

// [FIX 1] Cấu hình baseURL động
// Sử dụng biến môi trường hoặc để "/api" để Vite tự Proxy sang port 8080
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// [FIX 2] Biến cờ để chống spam redirect (Logic của bạn rất tốt)
let isRedirecting = false;

// Hàm chuyển hướng an toàn tuyệt đối
const forceLogout = () => {
  if (isRedirecting) return; // Nếu đang chuyển hướng rồi thì thôi
  isRedirecting = true;

  console.warn("👋 Force Logout: Đang chuyển về trang đăng nhập...");
  localStorage.removeItem("token"); 
  
  // Chỉ chuyển hướng nếu chưa ở trang login
  if (window.location.pathname !== "/login") {
    window.location.href = "/login";
  }
};

// Hàm check token đơn giản
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
    let token = null;
    try {
      // Import động store để tránh lỗi khởi tạo sớm
      const { useAuthStore } = await import("../stores/authStore");
      const authStore = useAuthStore();
      token = authStore.token;

      if (token && isTokenExpired(token)) {
        console.warn("⚠️ Token hết hạn (Client). Logout ngay.");
        authStore.logout(); 
        forceLogout();      
        return Promise.reject(new Error("Token expired")); 
      }
    } catch (err) {
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
    // Nếu đang redirect thì chặn luôn mọi lỗi sau đó để đỡ spam log
    if (isRedirecting) return Promise.reject(error);

    if (error.response) {
      const { status, data } = error.response;

      // Xử lý BAN
      const isBanned = status === 403 && 
        ((data?.error === "BANNED") || (data?.message?.toLowerCase().includes("phong ấn")));

      if (isBanned) {
        try {
            const { useAuthStore } = await import("../stores/authStore");
            const authStore = useAuthStore();
            authStore.triggerBan(data.message || "Bị phong ấn.");
        } catch (e) {
            console.error("Không gọi được store để hiện ban:", e);
        }
        return Promise.reject(error);
      }

      // Xử lý 401 (Hết hạn login hoặc Token không hợp lệ)
      if (status === 401) {
        console.warn("⚠️ 401 Unauthorized -> Logout");
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