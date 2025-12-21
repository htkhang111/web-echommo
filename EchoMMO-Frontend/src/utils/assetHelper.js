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
  
//   // Potion
//   if (lowerCode === "r_potion") return getUrl("/resources/r_potion.png");

//   // --- COIN ---
//   if (lowerCode.includes("coin")) {
//       const fileName = lowerCode.includes("echo") ? "r_coinEcho.png" : "r_coin.png";
//       return getUrl(`/resources/coin/${fileName}`);
//   }

//   // --- EQUIPMENT ---
//   for (const [prefix, folder] of Object.entries(PREFIX_MAP)) {
//     if (lowerCode.startsWith(prefix)) {
//       return getUrl(`${folder}/${lowerCode}.png`);
//     }
//   }

//   // --- AMBIGUOUS: 'b_' (Boots vs Background) ---
//   if (lowerCode.startsWith("b_")) {
//       if (lowerCode.includes("boot")) {
//           return getUrl(`/resources/equipment/boots/${lowerCode}.png`);
//       }
//       // Background (Mặc định là jpg, riêng doanhtrai là png)
//       const ext = lowerCode.includes("doanhtrai") ? "png" : "jpg";
//       return getUrl(`/background/${lowerCode}.${ext}`);
//   }

//   // --- MATERIALS ---
//   // Mặc định các code khác (r_, o_, f_, w_) nằm trong material
//   // return getUrl(`/resources/material/${lowerCode}.png`);
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

// 1. Cấu hình Base URL trỏ thẳng về GitHub Pages
const BASE_URL = "https://htkhang111.github.io";

// 2. Map tiền tố (Prefix) -> Thư mục assets tương ứng
const PREFIX_MAP = {
  "s_":  "/resources/equipment/sword",
  "a_":  "/resources/equipment/armor",
  "h_":  "/resources/equipment/helmet",
  "ri_": "/resources/equipment/ring",
  "n_":  "/resources/equipment/necklace",
};

// Helper: Ghép Base URL với path
const getUrl = (path) => {
  // Nếu path đã là full url (http) hoặc data base64 thì giữ nguyên
  if (!path) return "";
  if (path.startsWith("http") || path.startsWith("data:")) return path;
  
  const cleanPath = path.startsWith("/") ? path : `/${path}`;
  return `${BASE_URL}${cleanPath}`;
};

export const resolveItemImage = (itemCode) => {
  // 1. Fallback mặc định nếu không có code
  // [FIX] Sửa tên file từ o_coalOre.png thành o_coal.png cho đúng thực tế
  if (!itemCode) return getUrl("/resources/material/o_coal.png");

  // 2. Nếu là link full thì giữ nguyên
  if (itemCode.includes("http")) return itemCode;

  const code = itemCode.trim();
  const lowerCode = code.toLowerCase();

  // --- SPECIAL CASES ---
  if (lowerCode === "logo") return getUrl("/logo/Logo.png");
  
  // Potion
  if (lowerCode === "r_potion") return getUrl("/resources/r_potion.png");

  // --- COIN ---
  if (lowerCode.includes("coin")) {
      const fileName = lowerCode.includes("echo") ? "r_coinEcho.png" : "r_coin.png";
      return getUrl(`/resources/coin/${fileName}`);
  }

  // --- EQUIPMENT ---
  for (const [prefix, folder] of Object.entries(PREFIX_MAP)) {
    if (lowerCode.startsWith(prefix)) {
      return getUrl(`${folder}/${lowerCode}.png`);
    }
  }

  // --- AMBIGUOUS: 'b_' (Boots vs Background) ---
  if (lowerCode.startsWith("b_")) {
      if (lowerCode.includes("boot")) {
          return getUrl(`/resources/equipment/boots/${lowerCode}.png`);
      }
      // Background (Mặc định là jpg, riêng doanhtrai là png)
      const ext = lowerCode.includes("doanhtrai") ? "png" : "jpg";
      return getUrl(`/background/${lowerCode}.${ext}`);
  }

  // --- MATERIALS ---
  // Mặc định các code khác (r_, o_, f_, w_) nằm trong material
  return getUrl(`/resources/material/${lowerCode}.png`);
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
      attack: getCharImg("atk_yasou.png") 
    } 
  },
  skin_demon: { 
    id: "skin_demon", 
    name: "Huyết Quỷ", 
    sprites: { 
      idle: getCharImg("idle_demon1.png"), 
      run: getCharImg("run_demon1.png"), 
      attack: getCharImg("atk_demon1.png") 
    } 
  },
  skin_langkhach: { 
    id: "skin_langkhach", 
    name: "Lãng Khách", 
    sprites: { 
      idle: getCharImg("idle_langkhach1.png"), 
      run: getCharImg("run_langkhach1.png"), 
      attack: getCharImg("atk_langkhach1.png") 
    } 
  },
});

export const getCurrentSkin = (avatarUrl) => CHARACTER_SKINS[avatarUrl] || CHARACTER_SKINS["skin_yasou"];

export const getEnemyImage = (name, state = "idle") => {
  if (!name) return getEnemyImg("idle_goblin.png");
  
  const normalizedName = name.toLowerCase(); 
  const prefix = state === 'attack' ? 'atk_' : 'idle_';
  
  let fileName = "goblin"; 
  if (normalizedName.includes("xuong") || normalizedName.includes("skeleton")) fileName = "skeleton";
  else if (normalizedName.includes("nam") || normalizedName.includes("mushroom")) fileName = "mushroom";
  else if (normalizedName.includes("ac quy") || normalizedName.includes("demon")) fileName = "demon1";
  else if (normalizedName.includes("lang khach") || normalizedName.includes("langkhach")) fileName = "langkhach1";
  else if (normalizedName.includes("kiem si") || normalizedName.includes("yasou")) fileName = "yasou";
  
  return getEnemyImg(`${prefix}${fileName}.png`);
};

