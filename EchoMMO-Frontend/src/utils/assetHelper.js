// import { reactive } from 'vue';

// // ============================================================
// // 1. HÀM MAPPING ITEM IMAGE (DYNAMIC VITE URL)
// // ============================================================

// export const resolveItemImage = (imgCode) => {
//     // 1. Fallback nếu null/undefined -> Trả về cục đá hoặc ảnh rỗng
//     if (!imgCode) return new URL('../assets/resources/r_stone_3.png', import.meta.url).href;

//     // 2. Nếu là URL online (http) thì trả về luôn
//     if (imgCode.startsWith('http')) return imgCode;

//     // 3. Xử lý tên file (Quan trọng)
//     let fileName = imgCode.trim();

//     // TỰ ĐỘNG THÊM .png NẾU THIẾU (Vì DB lưu 'n_necklace_0')
//     if (!fileName.toLowerCase().endsWith('.png') && !fileName.toLowerCase().endsWith('.jpg')) {
//         fileName += '.png';
//     }

//     // Chuyển hết về chữ thường để so sánh cho dễ
//     const lowerName = fileName.toLowerCase();

//     try {
//         // --- A. EQUIPMENT (Trang bị) ---

//         // Kiếm (s_sword...)
//         if (lowerName.startsWith('s_') || lowerName.includes('sword')) {
//             return new URL(`../assets/equipment/sword/${lowerName}`, import.meta.url).href;
//         }
//         // Áo (a_armor...)
//         if (lowerName.startsWith('a_') || lowerName.includes('armor')) {
//             return new URL(`../assets/equipment/armor/${lowerName}`, import.meta.url).href;
//         }
//         // Mũ (h_helmet..., mu...)
//         if (lowerName.startsWith('h_') || lowerName.startsWith('mu') || lowerName.includes('helmet')) {
//             return new URL(`../assets/equipment/helmet/${lowerName}`, import.meta.url).href;
//         }
//         // Giày (b_boot...)
//         if (lowerName.startsWith('b_') || lowerName.includes('boot')) {
//             return new URL(`../assets/equipment/boots/${lowerName}`, import.meta.url).href;
//         }
//         // Nhẫn (ri_ring...)
//         if (lowerName.startsWith('ri_') || lowerName.includes('ring')) {
//             return new URL(`../assets/equipment/ring/${lowerName}`, import.meta.url).href;
//         }
//         // Dây chuyền (n_necklace...) [CÁI NÍ ĐANG CẦN]
//         if (lowerName.startsWith('n_') || lowerName.includes('neck')) {
//             return new URL(`../assets/equipment/necklace/${lowerName}`, import.meta.url).href;
//         }

//         // --- B. RESOURCES (Tài nguyên) ---
//         if (lowerName.startsWith('r_') || lowerName.includes('stone') || lowerName.includes('gold') || lowerName.includes('wood') || lowerName.includes('copper')) {
//             return new URL(`../assets/resources/${lowerName}`, import.meta.url).href;
//         }

//         // --- C. MẶC ĐỊNH ---
//         // Thử tìm trong assets gốc nếu không match rule trên
//         return new URL(`../assets/${lowerName}`, import.meta.url).href;

//     } catch (e) {
//         // Nếu file không tồn tại thật -> Trả về cục đá (để biết là lỗi chứ không phải là đồng vàng)
//         console.error(`[Asset Error] Không tìm thấy file: ${lowerName}`);
//         return new URL('../assets/resources/r_stone_3.png', import.meta.url).href;
//     }
// };

// // ============================================================
// // 2. CHARACTER SKINS & ENEMY & HELPER KHÁC
// // ============================================================

// const getCharImg = (name) => new URL(`../assets/char/${name}`, import.meta.url).href;
// const getEnemyImg = (name) => new URL(`../assets/enemy/${name}`, import.meta.url).href;

// export const CHARACTER_SKINS = reactive({
//     "skin_yasou": {
//         id: "skin_yasou", name: "Yasuo", description: "Kẻ bất dung thứ.",
//         sprites: { idle: getCharImg('idle_yasou.png'), run: getCharImg('run_yasou.png'), attack: getCharImg('atk_yasou.png') }
//     },
//     "skin_demon": {
//         id: "skin_demon", name: "Huyết Quỷ", description: "Sức mạnh từ bóng tối.",
//         sprites: { idle: getCharImg('idle_demon1.png'), run: getCharImg('run_demon1.png'), attack: getCharImg('atk_demon1.png') }
//     },
//     "skin_langkhach": {
//         id: "skin_langkhach", name: "Lãng Khách", description: "Kiếm khách vô danh.",
//         sprites: { idle: getCharImg('idle_langkhach1.png'), run: getCharImg('run_langkhach1.png'), attack: getCharImg('atk_langkhach1.png') }
//     }
// });

// export const getCurrentSkin = (avatarUrl) => {
//     if (avatarUrl && CHARACTER_SKINS[avatarUrl]) return CHARACTER_SKINS[avatarUrl];
//     return CHARACTER_SKINS["skin_yasou"];
// };

// const enemyMap = {
//     "Yêu Tinh": { idle: getEnemyImg('idle_goblin.png'), atk: getEnemyImg('atk_goblin.png') },
//     "Bộ Xương": { idle: getEnemyImg('idle_skeleton.png'), atk: getEnemyImg('atk_skeleton.png') },
//     "Nấm Độc": { idle: getEnemyImg('idle_mushroom.png'), atk: getEnemyImg('atk_mushroom.png') },
//     "default": { idle: getEnemyImg('idle_goblin.png'), atk: getEnemyImg('atk_goblin.png') }
// };

// export const getEnemyImage = (name, state = 'idle') => {
//     const key = Object.keys(enemyMap).find(k => name && name.includes(k)) || "default";
//     const target = enemyMap[key];
//     return state === 'attack' ? target.atk : target.idle;
// };

// // Hàm lấy kẻ địch ngẫu nhiên
// export const getRandomEnemyData = () => {
//     const keys = Object.keys(enemyMap).filter(k => k !== 'default');
//     const randomKey = keys[Math.floor(Math.random() * keys.length)];
//     return { name: randomKey, img: enemyMap[randomKey].idle };
// };

// // Hàm lấy ảnh item (dùng cho sự kiện explore nhặt vàng)
// export const getItemImage = (type) => {
//     if (type === 'GOLD') return resolveItemImage('r_gold_coin.png');
//     return resolveItemImage('r_copper_bar.png');
// };

// 5:46

// import { reactive } from "vue";

// // ============================================================
// // 1. HÀM MAPPING ITEM & BACKGROUND (DYNAMIC VITE URL)
// // ============================================================

// export const resolveItemImage = (imgCode) => {
//   // 1. Fallback an toàn nếu null
//   if (!imgCode)
//     return new URL("../assets/resources/r_stone_3.png", import.meta.url).href;

//   // 2. Nếu là URL online (http/https) thì giữ nguyên
//   if (imgCode.startsWith("http")) return imgCode;

//   // 3. Chuẩn hóa tên file (thêm .png nếu thiếu)
//   let fileName = imgCode.trim();
//   if (!fileName.match(/\.(png|jpg|jpeg|gif|webp)$/i)) {
//     fileName += ".png";
//   }

//   const lowerName = fileName.toLowerCase();

//   try {
//     // --- A. EQUIPMENT (Trang bị) ---
//     // Kiếm (s_sword)
//     if (lowerName.includes("sword") || lowerName.startsWith("s_")) {
//       return new URL(`../assets/equipment/sword/${fileName}`, import.meta.url)
//         .href;
//     }
//     // Áo (a_armor)
//     if (lowerName.includes("armor") || lowerName.startsWith("a_")) {
//       return new URL(`../assets/equipment/armor/${fileName}`, import.meta.url)
//         .href;
//     }
//     // Mũ (h_helmet hoặc mu)
//     if (
//       lowerName.includes("helmet") ||
//       lowerName.startsWith("h_") ||
//       lowerName.startsWith("mu")
//     ) {
//       return new URL(`../assets/equipment/helmet/${fileName}`, import.meta.url)
//         .href;
//     }
//     // Giày (b_boot) - Ưu tiên check 'boot' để không nhầm với background 'b_'
//     if (lowerName.includes("boot")) {
//       return new URL(`../assets/equipment/boots/${fileName}`, import.meta.url)
//         .href;
//     }
//     // Nhẫn (ri_ring)
//     if (lowerName.includes("ring") || lowerName.startsWith("ri_")) {
//       return new URL(`../assets/equipment/ring/${fileName}`, import.meta.url)
//         .href;
//     }
//     // Dây chuyền (n_neck)
//     if (lowerName.includes("neck") || lowerName.startsWith("n_")) {
//       return new URL(
//         `../assets/equipment/necklace/${fileName}`,
//         import.meta.url,
//       ).href;
//     }

//     // --- B. RESOURCES (Tài nguyên) ---
//     if (
//       lowerName.startsWith("r_") ||
//       ["stone", "gold", "wood", "copper", "coin"].some((k) =>
//         lowerName.includes(k),
//       )
//     ) {
//       return new URL(`../assets/resources/${fileName}`, import.meta.url).href;
//     }

//     // --- C. BACKGROUND (Ảnh nền - Folder Background viết hoa) ---
//     if (lowerName.startsWith("b_")) {
//       return new URL(`../assets/Background/${fileName}`, import.meta.url).href;
//     }

//     // --- D. MẶC ĐỊNH (Tìm ở gốc assets) ---
//     return new URL(`../assets/${fileName}`, import.meta.url).href;
//   } catch (e) {
//     console.error(`[Asset Helper] Không tìm thấy ảnh: ${fileName}`, e);
//     return new URL("../assets/resources/r_stone_3.png", import.meta.url).href;
//   }
// };

// // ============================================================
// // 2. CHARACTER & ENEMY HELPERS
// // ============================================================

// const getCharImg = (name) => {
//   try {
//     return new URL(`../assets/char/${name}`, import.meta.url).href;
//   } catch (e) {
//     return new URL("../assets/char/idle_yasou.png", import.meta.url).href;
//   }
// };

// const getEnemyImg = (name) => {
//   try {
//     return new URL(`../assets/enemy/${name}`, import.meta.url).href;
//   } catch (e) {
//     return new URL("../assets/enemy/idle_goblin.png", import.meta.url).href;
//   }
// };

// // Dữ liệu Skin
// export const CHARACTER_SKINS = reactive({
//   skin_yasou: {
//     id: "skin_yasou",
//     name: "Yasuo",
//     sprites: {
//       idle: getCharImg("idle_yasou.png"),
//       run: getCharImg("run_yasou.png"),
//       attack: getCharImg("atk_yasou.png"),
//     },
//   },
//   skin_demon: {
//     id: "skin_demon",
//     name: "Huyết Quỷ",
//     sprites: {
//       idle: getCharImg("idle_demon1.png"),
//       run: getCharImg("run_demon1.png"),
//       attack: getCharImg("atk_demon1.png"),
//     },
//   },
//   skin_langkhach: {
//     id: "skin_langkhach",
//     name: "Lãng Khách",
//     sprites: {
//       idle: getCharImg("idle_langkhach1.png"),
//       run: getCharImg("run_langkhach1.png"),
//       attack: getCharImg("atk_langkhach1.png"),
//     },
//   },
// });

// export const getCurrentSkin = (avatarUrl) => {
//   if (avatarUrl && CHARACTER_SKINS[avatarUrl])
//     return CHARACTER_SKINS[avatarUrl];
//   return CHARACTER_SKINS["skin_yasou"]; // Mặc định
// };

// const enemyMap = {
//   Goblin: {
//     idle: getEnemyImg("idle_goblin.png"),
//     atk: getEnemyImg("atk_goblin.png"),
//   },
//   "Yêu Tinh": {
//     idle: getEnemyImg("idle_goblin.png"),
//     atk: getEnemyImg("atk_goblin.png"),
//   },
//   Skeleton: {
//     idle: getEnemyImg("idle_skeleton.png"),
//     atk: getEnemyImg("atk_skeleton.png"),
//   },
//   "Bộ Xương": {
//     idle: getEnemyImg("idle_skeleton.png"),
//     atk: getEnemyImg("atk_skeleton.png"),
//   },
//   Mushroom: {
//     idle: getEnemyImg("idle_mushroom.png"),
//     atk: getEnemyImg("atk_mushroom.png"),
//   },
//   "Nấm Độc": {
//     idle: getEnemyImg("idle_mushroom.png"),
//     atk: getEnemyImg("atk_mushroom.png"),
//   },
//   default: {
//     idle: getEnemyImg("idle_goblin.png"),
//     atk: getEnemyImg("atk_goblin.png"),
//   },
// };

// export const getEnemyImage = (name, state = "idle") => {
//   if (!name) return enemyMap["default"].idle;
//   const key = Object.keys(enemyMap).find((k) => name.includes(k)) || "default";
//   const target = enemyMap[key];
//   return state === "attack" ? target.atk : target.idle;
// };

// export const getRandomEnemyData = () => {
//   const keys = Object.keys(enemyMap).filter((k) => k !== "default");
//   const randomKey = keys[Math.floor(Math.random() * keys.length)];
//   return { name: randomKey, img: enemyMap[randomKey].idle };
// };

// export const getItemImage = (type) => {
//   if (!type) return resolveItemImage("r_stone_3.png");

//   const itemMapping = {
//     GOLD: "r_gold_coin.png",
//     Gỗ: "r_go.png",
//     Đá: "r_stone_3.png",
//     Đồng: "r_copper_bar.png",
//     Cá: "r_gohoathach.png",
//     Sắt: "r_silver_bar.png",
//     "Bạch Kim": "r_mystrile_bar.png",
//     "Gỗ Khô": "r_go.png",
//     "Gỗ Lạnh": "r_go.png",
//     "Gỗ Hóa Thạch": "r_go.png",
//     "Cá Độc": "r_gohoathach.png",
//     "Kim Cương": "r_gold_node.png",
//     "Nguyên liệu lạ": "r_mystrile_node.png",
//     "Quặng Đồng": "r_copper_node.png",
//     "Quặng Sắt": "r_silver_node.png",
//   };

//   return resolveItemImage(itemMapping[type] || "r_stone_3.png");
// };

// // [FIX QUAN TRỌNG] Thêm dòng này để fix lỗi Combat.vue gọi getAssetUrl
// export const getAssetUrl = resolveItemImage;


import { reactive } from "vue";

// ============================================================
// 1. HÀM MAPPING ITEM & BACKGROUND (DYNAMIC VITE URL)
// ============================================================

export const resolveItemImage = (imgCode) => {
  // 1. Fallback an toàn nếu null
  if (!imgCode)
    return new URL("../assets/resources/r_stone_3.png", import.meta.url).href;

  // 2. Nếu là URL online (http/https) thì giữ nguyên
  if (imgCode.startsWith("http")) return imgCode;

  // 3. Chuẩn hóa tên file (thêm .png nếu thiếu)
  let fileName = imgCode.trim();
  if (!fileName.match(/\.(png|jpg|jpeg|gif|webp)$/i)) {
    fileName += ".png";
  }

  const lowerName = fileName.toLowerCase();

  try {
    // --- A. EQUIPMENT (Trang bị) ---
    if (lowerName.includes("sword") || lowerName.startsWith("s_")) {
      return new URL(`../assets/equipment/sword/${fileName}`, import.meta.url).href;
    }
    if (lowerName.includes("armor") || lowerName.startsWith("a_")) {
      return new URL(`../assets/equipment/armor/${fileName}`, import.meta.url).href;
    }
    if (lowerName.includes("helmet") || lowerName.startsWith("h_") || lowerName.startsWith("mu")) {
      return new URL(`../assets/equipment/helmet/${fileName}`, import.meta.url).href;
    }
    if (lowerName.includes("boot")) {
      return new URL(`../assets/equipment/boots/${fileName}`, import.meta.url).href;
    }
    if (lowerName.includes("ring") || lowerName.startsWith("ri_")) {
      return new URL(`../assets/equipment/ring/${fileName}`, import.meta.url).href;
    }
    if (lowerName.includes("neck") || lowerName.startsWith("n_")) {
      return new URL(`../assets/equipment/necklace/${fileName}`, import.meta.url).href;
    }

    // --- B. RESOURCES (Tài nguyên) ---
    if (
      lowerName.startsWith("r_") ||
      ["stone", "gold", "wood", "copper", "coin"].some((k) => lowerName.includes(k))
    ) {
      return new URL(`../assets/resources/${fileName}`, import.meta.url).href;
    }

    // --- C. BACKGROUND (Ảnh nền - Folder Background viết hoa) ---
    if (lowerName.startsWith("b_")) {
      return new URL(`../assets/Background/${fileName}`, import.meta.url).href;
    }

    // --- D. MẶC ĐỊNH (Tìm ở gốc assets) ---
    return new URL(`../assets/${fileName}`, import.meta.url).href;
  } catch (e) {
    console.error(`[Asset Helper] Không tìm thấy ảnh: ${fileName}`, e);
    return new URL("../assets/resources/r_stone_3.png", import.meta.url).href;
  }
};

// ============================================================
// 2. CHARACTER & ENEMY HELPERS
// ============================================================

const getCharImg = (name) => {
  try {
    return new URL(`../assets/char/${name}`, import.meta.url).href;
  } catch (e) {
    return new URL("../assets/char/idle_yasou.png", import.meta.url).href;
  }
};

const getEnemyImg = (name) => {
  try {
    return new URL(`../assets/enemy/${name}`, import.meta.url).href;
  } catch (e) {
    return new URL("../assets/enemy/idle_goblin.png", import.meta.url).href;
  }
};

// Dữ liệu Skin
export const CHARACTER_SKINS = reactive({
  skin_yasou: {
    id: "skin_yasou",
    name: "Yasuo",
    sprites: {
      idle: getCharImg("idle_yasou.png"),
      run: getCharImg("run_yasou.png"),
      attack: getCharImg("atk_yasou.png"),
    },
  },
  skin_demon: {
    id: "skin_demon",
    name: "Huyết Quỷ",
    sprites: {
      idle: getCharImg("idle_demon1.png"),
      run: getCharImg("run_demon1.png"),
      attack: getCharImg("atk_demon1.png"),
    },
  },
  skin_langkhach: {
    id: "skin_langkhach",
    name: "Lãng Khách",
    sprites: {
      idle: getCharImg("idle_langkhach1.png"),
      run: getCharImg("run_langkhach1.png"),
      attack: getCharImg("atk_langkhach1.png"),
    },
  },
});

export const getCurrentSkin = (avatarUrl) => {
  if (avatarUrl && CHARACTER_SKINS[avatarUrl]) return CHARACTER_SKINS[avatarUrl];
  return CHARACTER_SKINS["skin_yasou"];
};

const enemyMap = {
  Goblin: { idle: getEnemyImg("idle_goblin.png"), atk: getEnemyImg("atk_goblin.png") },
  "Yêu Tinh": { idle: getEnemyImg("idle_goblin.png"), atk: getEnemyImg("atk_goblin.png") },
  Skeleton: { idle: getEnemyImg("idle_skeleton.png"), atk: getEnemyImg("atk_skeleton.png") },
  "Bộ Xương": { idle: getEnemyImg("idle_skeleton.png"), atk: getEnemyImg("atk_skeleton.png") },
  Mushroom: { idle: getEnemyImg("idle_mushroom.png"), atk: getEnemyImg("atk_mushroom.png") },
  "Nấm Độc": { idle: getEnemyImg("idle_mushroom.png"), atk: getEnemyImg("atk_mushroom.png") },
  default: { idle: getEnemyImg("idle_goblin.png"), atk: getEnemyImg("atk_goblin.png") },
};

export const getEnemyImage = (name, state = "idle") => {
  if (!name) return enemyMap["default"].idle;
  const key = Object.keys(enemyMap).find((k) => name.includes(k)) || "default";
  const target = enemyMap[key];
  return state === "attack" ? target.atk : target.idle;
};

export const getRandomEnemyData = () => {
  const keys = Object.keys(enemyMap).filter((k) => k !== "default");
  const randomKey = keys[Math.floor(Math.random() * keys.length)];
  return { name: randomKey, img: enemyMap[randomKey].idle };
};

// [FIX QUAN TRỌNG] Cập nhật mapping đúng với tên file thực tế trong folder assets
export const getItemImage = (type) => {
  if (!type) return resolveItemImage("r_stone_3.png");

  const itemMapping = {
    // Mapping Tên Tiếng Việt -> File Tiếng Anh thực tế
    "GOLD": "r_gold_coin.png",
    "Gỗ": "r_wood.png",         // [FIX] r_go -> r_wood
    "Đá": "stone_1.png",        // [FIX] r_stone_3 -> stone_1 (hoặc r_stone_3 tùy ý)
    "Đồng": "r_copper_bar.png",
    "Cá": "r_fish.png",         // [FIX] r_gohoathach -> r_fish
    "Sắt": "r_silver_bar.png",
    "Bạch Kim": "r_mystrile_bar.png",
    
    // Các loại gỗ biến thể (Fallback về gỗ thường nếu chưa có icon riêng)
    "Gỗ Khô": "r_red_wood.png",   // [FIX] Có file r_red_wood.png
    "Gỗ Lạnh": "r_white_wood.png", // [FIX] Có file r_white_wood.png
    "Gỗ Lạ": "r_black_wood.png",   // [FIX] Có file r_black_wood.png
    
    // Các loại khác
    "Cá Mập": "r_shark.png",    // [FIX] Có file r_shark.png
    "Kim Cương": "stone_1.png", // Tạm dùng đá quý
    "Nguyên liệu lạ": "r_mystrile_node.png",
    "Quặng Đồng": "r_copper_node.png",
    "Quặng Sắt": "r_silver_node.png",
  };

  return resolveItemImage(itemMapping[type] || "r_stone_3.png");
};

export const getAssetUrl = resolveItemImage;