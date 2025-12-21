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
  if (!itemCode) return getUrl("/resources/material/o_coal.png");

  // 2. Nếu là link full thì giữ nguyên
  if (itemCode.includes("http")) return itemCode;

  const code = itemCode.trim();
  const lowerCode = code.toLowerCase();

  // --- SPECIAL CASES ---
  if (lowerCode === "logo") return getUrl("/logo/Logo.png");
  
  // [FIX] Potion: Chấp nhận cả "r_potion" và "r_potion.png"
  if (lowerCode === "r_potion" || lowerCode === "r_potion.png") {
      return getUrl("/resources/r_potion.png");
  }

  // --- COIN ---
  if (lowerCode.includes("coin")) {
      const fileName = lowerCode.includes("echo") ? "r_coinEcho.png" : "r_coin.png";
      return getUrl(`/resources/coin/${fileName}`);
  }

  // --- EQUIPMENT ---
  for (const [prefix, folder] of Object.entries(PREFIX_MAP)) {
    if (lowerCode.startsWith(prefix)) {
      // Nếu code equipment đã có đuôi .png thì không cộng thêm
      const fileName = lowerCode.endsWith(".png") ? lowerCode : `${lowerCode}.png`;
      return getUrl(`${folder}/${fileName}`);
    }
  }

  // --- AMBIGUOUS: 'b_' (Boots vs Background) ---
  if (lowerCode.startsWith("b_")) {
      if (lowerCode.includes("boot")) {
          const fileName = lowerCode.endsWith(".png") ? lowerCode : `${lowerCode}.png`;
          return getUrl(`/resources/equipment/boots/${fileName}`);
      }
      // Background: Kiểm tra xem đã có đuôi chưa
      if (lowerCode.endsWith(".png") || lowerCode.endsWith(".jpg")) {
          return getUrl(`/background/${lowerCode}`);
      }
      // Mặc định background
      const ext = lowerCode.includes("doanhtrai") ? "png" : "jpg";
      return getUrl(`/background/${lowerCode}.${ext}`);
  }

  // --- MATERIALS ---
  // Mặc định các code khác (r_, o_, f_, w_) nằm trong material
  // [FIX] Kiểm tra xem đã có đuôi file chưa để tránh lỗi .png.png
  let materialFile = lowerCode;
  if (!materialFile.endsWith(".png") && !materialFile.endsWith(".jpg")) {
      materialFile += ".png";
  }
  
  return getUrl(`/resources/material/${materialFile}`);
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