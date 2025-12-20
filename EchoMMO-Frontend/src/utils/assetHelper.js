import { reactive } from "vue";

// 1. Cấu hình Base URL động
// Ưu tiên lấy từ biến môi trường VITE_ASSET_BASE_URL (Tạo file .env để config nếu cần)
// Nếu không có -> Fallback về GitHub Pages như cũ
const BASE_URL = import.meta.env.VITE_ASSET_BASE_URL || "https://htkhang111.github.io";

// 2. Map tiền tố (Prefix) -> Thư mục assets tương ứng
// Giúp code gọn hơn, dễ mở rộng thêm loại item mới
const PREFIX_MAP = {
  "s_":  "/resources/equipment/sword",
  "a_":  "/resources/equipment/armor",
  "h_":  "/resources/equipment/helmet",
  "ri_": "/resources/equipment/ring",
  "n_":  "/resources/equipment/necklace",
};

// Helper: Ghép Base URL với path (đảm bảo xử lý dấu / chuẩn)
const getUrl = (path) => {
  const cleanPath = path.startsWith("/") ? path : `/${path}`;
  return `${BASE_URL}${cleanPath}`;
};

export const resolveItemImage = (itemCode) => {
  // 1. Fallback mặc định nếu không có code (Trả về Than)
  if (!itemCode) return getUrl("/resources/material/o_coalOre.png");

  // 2. Nếu là link full (http...) thì giữ nguyên
  if (itemCode.includes("http")) return itemCode;

  const code = itemCode.trim();
  const lowerCode = code.toLowerCase(); // Chuẩn hóa toàn bộ về chữ thường để tránh lỗi 404 trên Linux/GitHub

  // --- SPECIAL CASES ---
  if (lowerCode === "logo") return getUrl("/logo/Logo.png");
  
  // Xử lý riêng potion nếu user để nó nằm ngoài folder material
  if (lowerCode === "r_potion") return getUrl("/resources/r_potion.png");

  // --- COIN (Tiền tệ) ---
  if (lowerCode.includes("coin")) {
      const fileName = lowerCode.includes("echo") ? "r_coinEcho.png" : "r_coin.png";
      return getUrl(`/resources/coin/${fileName}`);
  }

  // --- EQUIPMENT (Check dựa trên Prefix Map) ---
  for (const [prefix, folder] of Object.entries(PREFIX_MAP)) {
    if (lowerCode.startsWith(prefix)) {
      // Trả về đường dẫn với tên file đã lowercase
      return getUrl(`${folder}/${lowerCode}.png`);
    }
  }

  // --- AMBIGUOUS: 'b_' (Boots vs Background) ---
  if (lowerCode.startsWith("b_")) {
      // Logic chặt chẽ hơn: Phải có chữ "boot" trong tên mới tính là Giày
      if (lowerCode.includes("boot")) {
          return getUrl(`/resources/equipment/boots/${lowerCode}.png`);
      }
      
      // Còn lại là Background
      // Giữ logic cũ: doanhtrai -> png, các background khác -> jpg
      const ext = lowerCode.includes("doanhtrai") ? "png" : "jpg";
      return getUrl(`/background/${lowerCode}.${ext}`);
  }

  // --- MATERIALS (Mặc định cho các case còn lại) ---
  // w_wood, o_ironOre, f_fish... sẽ chạy vào đây
  return getUrl(`/resources/material/${lowerCode}.png`);
};

// --- EXPORTS ALIAS (Giữ tương thích ngược) ---
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
  
  // Chuẩn hóa tên enemy
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