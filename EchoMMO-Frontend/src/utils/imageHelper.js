// Cấu hình URL của Backend.
// Nếu LuNu có file .env thì dùng import.meta.env.VITE_API_URL
// Nếu không thì hardcode tạm localhost:8080 như dưới đây:
const API_BASE_URL = "http://localhost:8080";

/**
 * Hàm xử lý hiển thị ảnh avatar
 * @param {string} avatarPath - Đường dẫn ảnh từ database (VD: /uploads/abc.png)
 * @returns {string} - Đường dẫn full có thể hiển thị được
 */
export const getAvatarUrl = (avatarPath) => {
  // 1. Nếu không có avatar hoặc chuỗi rỗng -> Trả về ảnh mặc định
  if (!avatarPath || avatarPath.trim() === "") {
    return "https://placehold.co/150?text=No+Image";
  }

  // 2. Nếu là link online (Google, Facebook...) -> Trả về nguyên gốc
  if (avatarPath.startsWith("http") || avatarPath.startsWith("https")) {
    return avatarPath;
  }

  // 3. Nếu là link nội bộ (/uploads/...) -> Nối thêm domain Backend
  // Xử lý trường hợp avatarPath có hoặc không có dấu / ở đầu
  const path = avatarPath.startsWith("/") ? avatarPath : `/${avatarPath}`;
  return `${API_BASE_URL}${path}`;
};
