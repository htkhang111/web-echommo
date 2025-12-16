// src/utils/resourceHelper.js

// Import thủ công các hình ảnh đang có trong folder
import imgGo from "@/assets/resources/r_go.png";
import imgDa from "@/assets/resources/stone_1.png";
import imgDong from "@/assets/resources/r_copper_node.png";
import imgSat from "@/assets/resources/r_silver_node.png"; // Dùng tạm hình Bạc cho Sắt
import imgBachKim from "@/assets/resources/r_mystrile_node.png";
import imgCa from "@/assets/resources/r_sliver_coin.png"; // Dùng tạm hình coin cho Cá (vì thiếu ảnh cá)
import imgDefault from "@/assets/logo/Logo.png";

export const getResourceImage = (itemName) => {
  if (!itemName) return imgDefault;

  // Map tên từ Backend (Database) -> Biến hình ảnh
  const map = {
    Gỗ: imgGo,
    Đá: imgDa,
    "Quặng Đồng": imgDong,
    Sắt: imgSat,
    "Bạch Kim": imgBachKim,
    Cá: imgCa,
    "Gỗ Hóa Thạch": imgGo, // Ví dụ thêm
  };

  return map[itemName] || imgDefault;
};
