import { reactive } from "vue";

// URL gốc trỏ vào GitHub Pages
const GITHUB_BASE = "https://htkhang111.github.io";

export const resolveItemImage = (itemCode) => {
  // 1. Fallback nếu không có code (Mặc định trả về Than)
  if (!itemCode) return `${GITHUB_BASE}/resources/material/o_coalOre.png`; 
  
  // 2. Nếu server trả về link full (http...) thì giữ nguyên
  if (itemCode.includes("http")) return itemCode;

  const code = itemCode.trim();
  const lowerCode = code.toLowerCase();

  // --- UI & LOGO ---
  if (lowerCode === "logo") return `${GITHUB_BASE}/logo/Logo.png`;
  
  // --- COIN (Tiền tệ) ---
  if (lowerCode.includes("coin")) {
      if (lowerCode.includes("echo")) return `${GITHUB_BASE}/resources/coin/r_coinEcho.png`;
      return `${GITHUB_BASE}/resources/coin/r_coin.png`;
  }

  // --- EQUIPMENT (Trang bị) ---
  // Kiểm tra tiền tố để trỏ vào đúng thư mục con
  if (lowerCode.startsWith("s_")) return `${GITHUB_BASE}/resources/equipment/sword/${code}.png`;
  if (lowerCode.startsWith("a_")) return `${GITHUB_BASE}/resources/equipment/armor/${code}.png`;
  if (lowerCode.startsWith("h_")) return `${GITHUB_BASE}/resources/equipment/helmet/${code}.png`;
  
  // Riêng giày (Boots) bắt đầu bằng b_ nên cần check kỹ để không nhầm với Background
  if (lowerCode.startsWith("b_") && lowerCode.includes("boot")) {
      return `${GITHUB_BASE}/resources/equipment/boots/${code}.png`;
  }
  
  if (lowerCode.startsWith("ri_")) return `${GITHUB_BASE}/resources/equipment/ring/${code}.png`;
  if (lowerCode.startsWith("n_")) return `${GITHUB_BASE}/resources/equipment/necklace/${code}.png`;

  // --- BACKGROUND (Ảnh nền) ---
  // Nếu bắt đầu bằng b_ mà KHÔNG PHẢI giày -> Là Background
  if (lowerCode.startsWith("b_")) {
      // Fix cứng extension cho background vì trên git có thể là jpg hoặc png
      if (lowerCode.includes("doanhtrai")) return `${GITHUB_BASE}/background/${code}.png`;
      return `${GITHUB_BASE}/background/${code}.jpg`;
  }

  // --- RESOURCES / MATERIALS (Mặc định) ---
  // Các code như: w_wood, o_ironOre, f_fish, r_potion... nằm thẳng trong folder material
  // (Lưu ý: r_potion.png cũng nằm trong resources hoặc resources/material tùy cách bro up, 
  // code dưới đây ưu tiên tìm trong material)
  
  // Xử lý riêng potion nếu nó nằm ngoài (tùy cấu trúc git của bro, nhưng safe nhất là cứ trỏ vào material nếu bro đã quy hoạch)
  if (lowerCode === "r_potion") return `${GITHUB_BASE}/resources/r_potion.png`;

  return `${GITHUB_BASE}/resources/material/${code}.png`;
};

// --- EXPORTS CẦN THIẾT CHO VUE COMPONENT ---
export const getAppLogo = () => `${GITHUB_BASE}/logo/Logo.png`;
export const getAssetUrl = resolveItemImage;
export const getItemImage = resolveItemImage;
export const getResourceImage = resolveItemImage; // Giữ alias này để code cũ không bị lỗi

// --- CHARACTER & ENEMY HELPERS ---
const getCharImg = (name) => `${GITHUB_BASE}/character/${name}`;
const getEnemyImg = (name) => `${GITHUB_BASE}/enemy/${name}`;

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
  
  // Chuẩn hóa tên enemy (xóa dấu, lowercase) để map file
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