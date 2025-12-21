// // import axios from "axios";
// // import { useAuthStore } from "../stores/authStore";

// // const axiosClient = axios.create({
// //   baseURL: "http://localhost:8080/api",
// //   headers: {
// //     "Content-Type": "application/json",
// //   },
// // });

// // // Interceptor cho Request: Gắn token vào header
// // axiosClient.interceptors.request.use((config) => {
// //   const authStore = useAuthStore();
// //   if (authStore.token) {
// //     config.headers.Authorization = `Bearer ${authStore.token}`;
// //   }
// //   return config;
// // });

// // // Interceptor cho Response: Xử lý lỗi toàn cục
// // axiosClient.interceptors.response.use(
// //   (response) => {
// //     return response;
// //   },
// //   (error) => {
// //     const authStore = useAuthStore();

// //     if (error.response) {
// //       // [FIX] Chỉ logout khi mã lỗi là 401 (Unauthorized - Hết hạn token)
// //       if (error.response.status === 401) {
// //         console.warn(
// //           "[AXIOS] Phiên đăng nhập hết hạn (401). Đang đăng xuất...",
// //         );
// //         authStore.logout();
// //         window.location.href = "/login";
// //       }
// // //       // [FIX] 403 (Forbidden) thì chỉ log ra, không logout
// // //       else if (error.response.status === 403) {
// // //         console.error(
// // //           "[AXIOS] Truy cập bị từ chối (403). Bạn không có quyền thực hiện hành động này.",
// // //         );
// // //       }
// // //     }

// // //     return Promise.reject(error);
// // //   },
// // // );

// // // export default axiosClient;
// // // [FIX] 403 (Forbidden) thì chỉ log ra, không logout
// //       else if (error.response.status === 403) {
// //         console.error("--- LỖI QUYỀN HẠN (403) ---");

// //         // 1. In ra URL bị lỗi để chắc chắn mình gọi đúng API
// //         console.error("API gọi đến:", error.config.url);

// //         // 2. In ra tin nhắn từ Server (Quan trọng nhất)
// //         // Server thường trả về lý do: "User ID 10 cannot access Player ID 5"
// //         console.error("Lý do từ Server:", error.response.data);

// //         // 3. Kiểm tra xem lúc đó đang dùng Token nào (để debug)
// //         const authStore = useAuthStore();
// //         console.log("Token hiện tại:", authStore.token);
// //       }
// //     }
// //     return Promise.reject(error);
// //   },
// // );

// // export default axiosClient;
// import axios from "axios";
// import { useAuthStore } from "../stores/authStore";

// const axiosClient = axios.create({
//   baseURL: "http://localhost:8080/api",
//   headers: {
//     "Content-Type": "application/json",
//   },
// });

// /**
//  * Hàm kiểm tra Token hết hạn bằng cách giải mã Payload JWT (base64)
//  * Giúp chặn request ngay tại máy khách, giảm tải cho Server và tránh loop redirect
//  */
// const isTokenExpired = (token) => {
//   if (!token || token === "null" || token === "undefined") return true;
//   try {
//     const base64Url = token.split(".")[1];
//     const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
//     const payload = JSON.parse(window.atob(base64));
//     const now = Math.floor(Date.now() / 1000);
    
//     // Nếu thời gian hiện tại lớn hơn thời gian hết hạn (exp) thì token die
//     return payload.exp < now; 
//   } catch (e) {
//     return true; // Lỗi định dạng coi như hết hạn để an toàn
//   }
// };

// // --- 1. REQUEST INTERCEPTOR: Xử lý trước khi gửi đi ---
// axiosClient.interceptors.request.use(
//   (config) => {
//     const authStore = useAuthStore();
    
//     if (authStore.token) {
//       // KIỂM TRA TRƯỚC KHI GỬI:
//       // Nếu phát hiện token đã hết hạn, không gửi request nữa mà đá về Login luôn
//       if (isTokenExpired(authStore.token)) {
//         console.warn("[AUTH] Token expired detected before request. Redirecting to login...");
//         authStore.logout();
//         // Chỉ redirect nếu không phải đang ở trang login để tránh loop
//         if (window.location.pathname !== "/login") {
//             window.location.href = "/login";
//         }
//         return Promise.reject(new Error("Token expired - Request cancelled"));
//       }

//       // Nếu ổn thì gắn token vào Header
//       config.headers.Authorization = `Bearer ${authStore.token}`;
//     }
//     return config;
//   },
//   (error) => Promise.reject(error)
// );

// // --- 2. RESPONSE INTERCEPTOR: Xử lý kết quả trả về từ Server ---
// axiosClient.interceptors.response.use(
//   (response) => {
//     return response;
//   },
//   (error) => {
//     const authStore = useAuthStore();

//     if (error.response) {
//       const status = error.response.status;
//       const originalRequest = error.config;

//       // Xử lý lỗi 401 (Unauthorized) hoặc 403 (Forbidden) khi token chết
//       if (status === 401 || (status === 403 && isTokenExpired(authStore.token))) {
//         console.error("[AUTH] Unauthorized access. Logging out...");
//         authStore.logout();
        
//         if (window.location.pathname !== "/login") {
//             window.location.href = "/login";
//         }
//       }
      
//       // Log lỗi 403 chi tiết để debug quyền hạn (nếu token vẫn còn hạn)
//       if (status === 403 && !isTokenExpired(authStore.token)) {
//         console.error("--- LỖI QUYỀN HẠN (403) ---");
//         console.error("API:", originalRequest.url);
//         console.error("Data:", error.response.data);
//       }
//     }

//     return Promise.reject(error);
//   }
// );

// export default axiosClient;
import axios from "axios";
import { useAuthStore } from "../stores/authStore";

// [FIX] Đổi localhost -> 127.0.0.1 để tránh lỗi kết nối trên Windows
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
  (config) => {
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

// --- RESPONSE INTERCEPTOR (Xử lý Ban ở đây) ---
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const authStore = useAuthStore();
    
    if (error.response) {
      const { status, data } = error.response;

      // [ƯU TIÊN 1] KIỂM TRA BANNED
      // Backend trả về 403 và JSON có chứa error="BANNED" hoặc message liên quan
      const isBannedSignal = 
        status === 403 && 
        (
          (data && data.error === 'BANNED') || 
          (data && data.message && data.message.toLowerCase().includes('phong ấn'))
        );

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