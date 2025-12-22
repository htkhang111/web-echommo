// import { reactive } from "vue";

// // 1. Cấu hình Base URL trỏ thẳng về GitHub Pages
// const BASE_URL = "https://htkhang111.github.io";

// // 2. Map tiền tố (Prefix) -> Thư mục assets tương ứng
// const PREFIX_MAP = {
//   "s_":  "/resources/equipment/sword",
//   "a_":  "/resources/equipment/armor",
//   "h_":  "/resources/equipment/helmet",
//   "ri_": "/resources/equipment/ring",
//   "n_":  "/resources/equipment/necklace",
// };

// // Helper: Ghép Base URL với path
// const getUrl = (path) => {
//   // Nếu path đã là full url (http) hoặc data base64 thì giữ nguyên
//   if (!path) return "";
//   if (path.startsWith("http") || path.startsWith("data:")) return path;

//   const cleanPath = path.startsWith("/") ? path : `/${path}`;
//   return `${BASE_URL}${cleanPath}`;
// };

// export const resolveItemImage = (itemCode) => {
//   // 1. Fallback mặc định nếu không có code
//   if (!itemCode) return getUrl("/resources/material/o_coal.png");

//   // 2. Nếu là link full thì giữ nguyên
//   if (itemCode.includes("http")) return itemCode;

//   const code = itemCode.trim();
//   const lowerCode = code.toLowerCase();

//   // --- SPECIAL CASES ---
//   if (lowerCode === "logo") return getUrl("/logo/Logo.png");

//   // [FIX] Potion: Chấp nhận cả "r_potion" và "r_potion.png"
//   if (lowerCode === "r_potion" || lowerCode === "r_potion.png") {
//       return getUrl("/resources/r_potion.png");
//   }

//   // --- COIN ---
//   if (lowerCode.includes("coin")) {
//       const fileName = lowerCode.includes("echo") ? "r_coinEcho.png" : "r_coin.png";
//       return getUrl(`/resources/coin/${fileName}`);
//   }

//   // --- EQUIPMENT ---
//   for (const [prefix, folder] of Object.entries(PREFIX_MAP)) {
//     if (lowerCode.startsWith(prefix)) {
//       // Nếu code equipment đã có đuôi .png thì không cộng thêm
//       const fileName = lowerCode.endsWith(".png") ? lowerCode : `${lowerCode}.png`;
//       return getUrl(`${folder}/${fileName}`);
//     }
//   }

//   // --- AMBIGUOUS: 'b_' (Boots vs Background) ---
//   if (lowerCode.startsWith("b_")) {
//       if (lowerCode.includes("boot")) {
//           const fileName = lowerCode.endsWith(".png") ? lowerCode : `${lowerCode}.png`;
//           return getUrl(`/resources/equipment/boots/${fileName}`);
//       }
//       // Background: Kiểm tra xem đã có đuôi chưa
//       if (lowerCode.endsWith(".png") || lowerCode.endsWith(".jpg")) {
//           return getUrl(`/background/${lowerCode}`);
//       }
//       // Mặc định background
//       const ext = lowerCode.includes("doanhtrai") ? "png" : "jpg";
//       return getUrl(`/background/${lowerCode}.${ext}`);
//   }

//   // --- MATERIALS ---
//   // Mặc định các code khác (r_, o_, f_, w_) nằm trong material
//   // [FIX] Kiểm tra xem đã có đuôi file chưa để tránh lỗi .png.png
//   let materialFile = lowerCode;
//   if (!materialFile.endsWith(".png") && !materialFile.endsWith(".jpg")) {
//       materialFile += ".png";
//   }

//   return getUrl(`/resources/material/${materialFile}`);
// };

// // --- EXPORTS ALIAS ---
// export const getAppLogo = () => getUrl("/logo/Logo.png");
// export const getAssetUrl = resolveItemImage;
// export const getItemImage = resolveItemImage;
// export const getResourceImage = resolveItemImage;

// // --- CHARACTER & ENEMY HELPERS ---
// const getCharImg = (name) => getUrl(`/character/${name}`);
// const getEnemyImg = (name) => getUrl(`/enemy/${name}`);

// export const CHARACTER_SKINS = reactive({
//   skin_yasou: {
//     id: "skin_yasou",
//     name: "Yasuo",
//     sprites: {
//       idle: getCharImg("idle_yasou.png"),
//       run: getCharImg("run_yasou.png"),
//       attack: getCharImg("atk_yasou.png")
//     }
//   },
//   skin_demon: {
//     id: "skin_demon",
//     name: "Huyết Quỷ",
//     sprites: {
//       idle: getCharImg("idle_demon1.png"),
//       run: getCharImg("run_demon1.png"),
//       attack: getCharImg("atk_demon1.png")
//     }
//   },
//   skin_langkhach: {
//     id: "skin_langkhach",
//     name: "Lãng Khách",
//     sprites: {
//       idle: getCharImg("idle_langkhach1.png"),
//       run: getCharImg("run_langkhach1.png"),
//       attack: getCharImg("atk_langkhach1.png")
//     }
//   },
// });

// export const getCurrentSkin = (avatarUrl) => CHARACTER_SKINS[avatarUrl] || CHARACTER_SKINS["skin_yasou"];

// export const getEnemyImage = (name, state = "idle") => {
//   if (!name) return getEnemyImg("idle_goblin.png");

//   const normalizedName = name.toLowerCase();
//   const prefix = state === 'attack' ? 'atk_' : 'idle_';

//   let fileName = "goblin";
//   if (normalizedName.includes("xuong") || normalizedName.includes("skeleton")) fileName = "skeleton";
//   else if (normalizedName.includes("nam") || normalizedName.includes("mushroom")) fileName = "mushroom";
//   else if (normalizedName.includes("ac quy") || normalizedName.includes("demon")) fileName = "demon1";
//   else if (normalizedName.includes("lang khach") || normalizedName.includes("langkhach")) fileName = "langkhach1";
//   else if (normalizedName.includes("kiem si") || normalizedName.includes("yasou")) fileName = "yasou";

//   return getEnemyImg(`${prefix}${fileName}.png`);
// };
// import { reactive } from "vue";

// // 1. Cấu hình Base URL trỏ thẳng về GitHub Pages
// const BASE_URL = "https://htkhang111.github.io";

// // 2. Map tiền tố (Prefix) -> Thư mục assets tương ứng
// const PREFIX_MAP = {
//   // --- EQUIPMENT (Trang bị) ---
//   "s_":  "/resources/equipment/sword",
//   "a_":  "/resources/equipment/armor",
//   "h_":  "/resources/equipment/helmet",
//   "ri_": "/resources/equipment/ring",
//   "n_":  "/resources/equipment/necklace",
//   "b_":  "/resources/equipment/boots", // Thêm b_ vào đây luôn cho gọn

//   // --- TOOLS (Công cụ - Đã check đúng path trên git) ---
//   "fr-": "/resources/tool/fishing-rod", // Fishing Rod
//   "p-":  "/resources/tool/pickaxe",     // Pickaxe
//   "a-":  "/resources/tool/axe",         // Axe (Khác với a_ là Armor)
//   "s-":  "/resources/tool/shovel",      // Shovel (Khác với s_ là Sword)
// };

// // 3. Xử lý các file ngoại lệ (không phải .png)
// const EXCEPTIONS = {
//   "a-4-heartoftheforest": ".webp" // File rìu này dùng đuôi webp
// };

// // Helper: Ghép Base URL với path + Xử lý khoảng trắng (encodeURI)
// const getUrl = (path) => {
//   if (!path) return "";
//   if (path.startsWith("http") || path.startsWith("data:")) return path;

//   const cleanPath = path.startsWith("/") ? path : `/${path}`;
//   // encodeURI để xử lý các file có khoảng trắng như "best axeinthegame.png"
//   return `${BASE_URL}${encodeURI(cleanPath)}`;
// };

// export const resolveItemImage = (itemCode) => {
//   // 1. Fallback mặc định
//   if (!itemCode) return getUrl("/resources/material/o_coal.png");

//   // 2. Nếu là link full thì giữ nguyên
//   if (itemCode.includes("http")) return itemCode;

//   const code = itemCode.trim();
//   const lowerCode = code.toLowerCase();

//   // --- SPECIAL CASES ---
//   if (lowerCode === "logo") return getUrl("/logo/Logo.png");

//   // Potion
//   if (lowerCode === "r_potion" || lowerCode === "r_potion.png") {
//       return getUrl("/resources/r_potion.png");
//   }

//   // --- COIN ---
//   if (lowerCode.includes("coin")) {
//       const fileName = lowerCode.includes("echo") ? "r_coin-echo.png" : "r_coin.png";
//       return getUrl(`/resources/coin/${fileName}`);
//   }

//   // --- CHECK EXTENSION (Nếu code đã có đuôi file thì dùng luôn) ---
//   let ext = ".png";
//   let cleanName = lowerCode;

//   if (lowerCode.endsWith(".png") || lowerCode.endsWith(".jpg") || lowerCode.endsWith(".webp")) {
//     ext = ""; // Không cần cộng thêm đuôi
//   } else {
//     // Check ngoại lệ đuôi file
//     if (EXCEPTIONS[lowerCode]) {
//       ext = EXCEPTIONS[lowerCode];
//     }
//   }

//   // --- CHECK PREFIX MAP (Equipment & Tools) ---
//   for (const [prefix, folder] of Object.entries(PREFIX_MAP)) {
//     if (lowerCode.startsWith(prefix)) {
//       // Trường hợp đặc biệt: b_ vừa là boots vừa là background
//       if (prefix === "b_" && !lowerCode.includes("boot")) {
//          continue; // Bỏ qua, để logic background bên dưới xử lý
//       }
//       return getUrl(`${folder}/${cleanName}${ext}`);
//     }
//   }

//   // --- BACKGROUNDS (b_ nhưng không phải boots) ---
//   if (lowerCode.startsWith("b_")) {
//       if (lowerCode.endsWith(".png") || lowerCode.endsWith(".jpg")) {
//           return getUrl(`/background/${lowerCode}`);
//       }
//       const bgExt = lowerCode.includes("doanhtrai") ? ".png" : ".jpg";
//       return getUrl(`/background/${lowerCode}${bgExt}`);
//   }

//   // --- MATERIALS (Mặc định cho f_, o_, w_...) ---
//   return getUrl(`/resources/material/${cleanName}${ext}`);
// };

// // --- EXPORTS ALIAS ---
// export const getAppLogo = () => getUrl("/logo/Logo.png");
// export const getAssetUrl = resolveItemImage;
// export const getItemImage = resolveItemImage;
// export const getResourceImage = resolveItemImage;

// // --- CHARACTER & ENEMY HELPERS ---
// const getCharImg = (name) => getUrl(`/character/${name}`);
// const getEnemyImg = (name) => getUrl(`/enemy/${name}`);

// export const CHARACTER_SKINS = reactive({
//   skin_yasou: {
//     id: "skin_yasou",
//     name: "Yasuo",
//     sprites: {
//       idle: getCharImg("idle_yasou.png"),
//       run: getCharImg("run_yasou.png"),
//       attack: getCharImg("atk_yasou.png")
//     }
//   },
//   skin_demon: {
//     id: "skin_demon",
//     name: "Huyết Quỷ",
//     sprites: {
//       idle: getCharImg("idle_demon1.png"),
//       run: getCharImg("run_demon1.png"),
//       attack: getCharImg("atk_demon1.png")
//     }
//   },
//   skin_langkhach: {
//     id: "skin_langkhach",
//     name: "Lãng Khách",
//     sprites: {
//       idle: getCharImg("idle_langkhach1.png"),
//       run: getCharImg("run_langkhach1.png"),
//       attack: getCharImg("atk_langkhach1.png")
//     }
//   },
// });

// export const getCurrentSkin = (avatarUrl) => CHARACTER_SKINS[avatarUrl] || CHARACTER_SKINS["skin_yasou"];

// export const getEnemyImage = (name, state = "idle") => {
//   if (!name) return getEnemyImg("idle_goblin.png");

//   const normalizedName = name.toLowerCase();
//   const prefix = state === 'attack' ? 'atk_' : 'idle_';

//   let fileName = "goblin";
//   if (normalizedName.includes("xuong") || normalizedName.includes("skeleton")) fileName = "skeleton";
//   else if (normalizedName.includes("nam") || normalizedName.includes("mushroom")) fileName = "mushroom";
//   else if (normalizedName.includes("ac quy") || normalizedName.includes("demon")) fileName = "demon1";
//   else if (normalizedName.includes("lang khach") || normalizedName.includes("langkhach")) fileName = "langkhach1";
//   else if (normalizedName.includes("kiem si") || normalizedName.includes("yasou")) fileName = "yasou";

//   return getEnemyImg(`${prefix}${fileName}.png`);
// };
import { reactive } from "vue";

// [QUAN TRỌNG] Dùng link raw để lấy ảnh trực tiếp, không qua cache của GitHub Pages
// Nếu nhánh chính của đại hiệp là 'master', hãy đổi chữ 'main' ở cuối thành 'master'
const BASE_URL = "https://raw.githubusercontent.com/htkhang111/htkhang111.github.io/main";

// Map tiền tố (Prefix) -> Thư mục assets tương ứng
const PREFIX_MAP = {
  s_: "/resources/equipment/sword",
  a_: "/resources/equipment/armor",
  h_: "/resources/equipment/helmet",
  ri_: "/resources/equipment/ring",
  n_: "/resources/equipment/necklace",
  b_: "/resources/equipment/boots",
  
  // Công cụ (Tools)
  "fr-": "/resources/tool/fishing-rod",
  "p-": "/resources/tool/pickaxe",
  "a-": "/resources/tool/axe", // Rìu (khác a_ là giáp)
  "s-": "/resources/tool/shovel",
};

// Xử lý các file ngoại lệ (không phải .png)
const EXCEPTIONS = {
  "a-4-heartoftheforest": ".webp",
};

// Helper: Ghép Base URL với path + Xử lý khoảng trắng
const getUrl = (path) => {
  if (!path) return "";
  if (path.startsWith("http") || path.startsWith("data:")) return path;

  const cleanPath = path.startsWith("/") ? path : `/${path}`;
  // encodeURI để xử lý tên file có khoảng trắng
  return `${BASE_URL}${encodeURI(cleanPath)}`;
};

export const resolveItemImage = (itemCode) => {
  // 1. Fallback mặc định
  if (!itemCode) return getUrl("/resources/material/o_coal.png");

  // 2. Nếu là link full hoặc base64 thì giữ nguyên
  if (itemCode.includes("http") || itemCode.includes("data:")) return itemCode;

  const code = itemCode.trim();
  const lowerCode = code.toLowerCase();

  // --- XỬ LÝ ĐƯỜNG DẪN CÓ SẴN TRONG DB (QUAN TRỌNG) ---
  // Nếu DB lỡ lưu dạng "tool/axe/..." hoặc "resources/..." thì map lại cho đúng
  if (lowerCode.startsWith("tool/") || lowerCode.startsWith("equipment/")) {
    return getUrl(`/resources/${code}`);
  }
  if (lowerCode.startsWith("resources/") || lowerCode.startsWith("background/") || lowerCode.startsWith("logo/")) {
    return getUrl(`/${code}`);
  }

  // --- SPECIAL CASES ---
  if (lowerCode === "logo") return getUrl("/logo/Logo.png");
  
  // Potion
  if (lowerCode === "r_potion" || lowerCode === "r_potion.png") {
    return getUrl("/resources/r_potion.png");
  }

  // --- COIN ---
  if (lowerCode.includes("coin")) {
    const fileName = lowerCode.includes("echo") ? "r_coin-echo.png" : "r_coin.png";
    return getUrl(`/resources/coin/${fileName}`);
  }

  // --- XỬ LÝ ĐUÔI FILE ---
  let ext = ".png";
  // Nếu code đã có đuôi file thì không cộng thêm
  if (lowerCode.endsWith(".png") || lowerCode.endsWith(".jpg") || lowerCode.endsWith(".webp")) {
    ext = "";
  } else if (EXCEPTIONS[lowerCode]) {
    // Check ngoại lệ đuôi file (ví dụ .webp)
    ext = EXCEPTIONS[lowerCode];
  }

  // --- CHECK PREFIX MAP (Trang bị & Công cụ) ---
  for (const [prefix, folder] of Object.entries(PREFIX_MAP)) {
    if (lowerCode.startsWith(prefix)) {
      // Trường hợp đặc biệt: b_ vừa là boots vừa là background
      if (prefix === "b_" && !lowerCode.includes("boot")) {
         continue; 
      }
      return getUrl(`${folder}/${code}${ext}`);
    }
  }

  // --- BACKGROUNDS (b_ nhưng không phải boots) ---
  if (lowerCode.startsWith("b_")) {
      const bgExt = (ext === "") ? "" : (lowerCode.includes("doanhtrai") ? ".png" : ".jpg");
      return getUrl(`/background/${code}${bgExt}`);
  }

  // --- MATERIALS (Mặc định cho tất cả cái còn lại) ---
  return getUrl(`/resources/material/${code}${ext}`);
};

// --- EXPORTS ALIAS ---
export const getAppLogo = () => getUrl("/logo/Logo.png");
export const getAssetUrl = resolveItemImage;
export const getItemImage = resolveItemImage;
export const getResourceImage = resolveItemImage;

// --- CHARACTER & ENEMY HELPERS ---
const getCharImg = (name) => getUrl(`/character/${name}`);
const getEnemyImg = (name) => getUrl(`/enemy/${name}`);

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

export const getCurrentSkin = (avatarUrl) =>
  CHARACTER_SKINS[avatarUrl] || CHARACTER_SKINS["skin_yasou"];

export const getEnemyImage = (name, state = "idle") => {
  if (!name) return getEnemyImg("idle_goblin.png");

  const normalizedName = name.toLowerCase();
  const prefix = state === "attack" ? "atk_" : "idle_";

  let fileName = "goblin";
  if (normalizedName.includes("xuong") || normalizedName.includes("skeleton"))
    fileName = "skeleton";
  else if (normalizedName.includes("nam") || normalizedName.includes("mushroom"))
    fileName = "mushroom";
  else if (normalizedName.includes("ac quy") || normalizedName.includes("demon"))
    fileName = "demon1";
  else if (normalizedName.includes("lang khach") || normalizedName.includes("langkhach"))
    fileName = "langkhach1";
  else if (normalizedName.includes("kiem si") || normalizedName.includes("yasou"))
    fileName = "yasou";

  return getEnemyImg(`${prefix}${fileName}.png`);
};