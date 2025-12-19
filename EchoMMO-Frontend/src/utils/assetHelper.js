import { reactive } from "vue";

// URL Gốc của GitHub Pages
const GITHUB_BASE = "https://htkhang111.github.io";

// ============================================================
// 1. HÀM MAPPING ITEM & BACKGROUND (GITHUB ASSETS)
// ============================================================

export const resolveItemImage = (imgCode) => {
  // 1. Fallback an toàn
  if (!imgCode) return `${GITHUB_BASE}/material/stone_1.png`;

  // 2. Nếu là URL online sẵn thì giữ nguyên
  if (imgCode.startsWith("http")) return imgCode;

  // 3. Chuẩn hóa tên file
  let fileName = imgCode.trim();
  if (!fileName.match(/\.(png|jpg|jpeg|gif|webp)$/i)) {
    fileName += ".png";
  }
  const lowerName = fileName.toLowerCase();

  // --- MAPPING ĐẶC BIỆT THEO CẤU TRÚC GITHUB CỦA BẠN ---
  // Dựa trên repo: htkhang111/htkhang111.github.io

  // Gỗ
  if (["r_wood.png", "r_go.png", "v_wood.png"].includes(lowerName)) return `${GITHUB_BASE}/material/w_wood.png`;
  if (lowerName === "r_red_wood.png") return `${GITHUB_BASE}/material/w_woodRed.png`;
  if (lowerName === "r_black_wood.png") return `${GITHUB_BASE}/material/w_woodBlack.png`;
  if (lowerName === "r_white_wood.png") return `${GITHUB_BASE}/material/w_woodWhite.png`;

  // Khoáng sản
  if (["r_iron_ore.png", "iron_ore.png", "v_iron.png"].includes(lowerName)) return `${GITHUB_BASE}/material/o_ironOre.png`;
  if (["r_coal_ore.png", "coal_ore.png"].includes(lowerName)) return `${GITHUB_BASE}/material/o_coalOre.png`;
  if (["r_platinum.png", "platinum.png", "v_plat.png"].includes(lowerName)) return `${GITHUB_BASE}/material/o_platinumOre.png`;
  if (lowerName === "r_strange_ore.png") return `${GITHUB_BASE}/material/o_strangeOre.png`;
  
  // Đá
  if (["stone_1.png", "r_stone_3.png", "v_stone.png"].includes(lowerName)) return `${GITHUB_BASE}/resources/stone_1.png`; 

  // Sinh vật / Khác
  if (["r_fish.png", "v_fish.png"].includes(lowerName)) return `${GITHUB_BASE}/material/r_fish.png`;
  if (lowerName === "r_shark.png") return `${GITHUB_BASE}/material/r_fishShark.png`;
  if (lowerName === "r_potion.png") return `${GITHUB_BASE}/material/r_potion.png`;

  // Tiền tệ (Trong folder resources/coin)
  if (lowerName.includes("coin")) return `${GITHUB_BASE}/resources/coin/r_coin.png`;

  // --- EQUIPMENT (TRANG BỊ) ---
  // Nếu bạn chưa upload folder 'assets/equipment' lên Github, các link này sẽ 404.
  // Nhưng cứ để sẵn code, khi nào upload xong là nó tự hiện.
  if (lowerName.includes("sword") || lowerName.startsWith("s_")) {
    return `${GITHUB_BASE}/assets/equipment/sword/${fileName}`;
  }
  if (lowerName.includes("armor") || lowerName.startsWith("a_")) {
    return `${GITHUB_BASE}/assets/equipment/armor/${fileName}`;
  }
  if (lowerName.includes("helmet") || lowerName.startsWith("h_") || lowerName.startsWith("mu")) {
    return `${GITHUB_BASE}/assets/equipment/helmet/${fileName}`;
  }
  if (lowerName.includes("boot")) {
    return `${GITHUB_BASE}/assets/equipment/boots/${fileName}`;
  }
  if (lowerName.includes("ring") || lowerName.startsWith("ri_")) {
    return `${GITHUB_BASE}/assets/equipment/ring/${fileName}`;
  }
  if (lowerName.includes("neck") || lowerName.startsWith("n_")) {
    return `${GITHUB_BASE}/assets/equipment/necklace/${fileName}`;
  }
  
  // Background
  if (lowerName.startsWith("b_")) {
    return `${GITHUB_BASE}/assets/Background/${fileName}`;
  }

  // Mặc định trả về link material nếu không khớp rule nào
  return `${GITHUB_BASE}/material/${fileName}`;
};

// ============================================================
// 2. CHARACTER & ENEMY HELPERS
// ============================================================

const getCharImg = (name) => `${GITHUB_BASE}/assets/char/${name}`;
const getEnemyImg = (name) => `${GITHUB_BASE}/assets/enemy/${name}`;

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

// Enemy mặc định
const defaultEnemy = { idle: getEnemyImg("idle_goblin.png"), atk: getEnemyImg("atk_goblin.png") };

export const getEnemyImage = (name, state = "idle") => {
  if (!name) return defaultEnemy.idle;
  
  const normalizedName = name.toLowerCase().replace(/\s+/g, '');
  const prefix = state === 'attack' ? 'atk_' : 'idle_';
  
  // Map tạm thời một số tên tiếng Việt sang tên file tiếng Anh
  let fileName = normalizedName;
  if (normalizedName.includes("yeutinh") || normalizedName.includes("goblin")) fileName = "goblin";
  if (normalizedName.includes("nam") || normalizedName.includes("mushroom")) fileName = "mushroom";
  if (normalizedName.includes("xuong") || normalizedName.includes("skeleton")) fileName = "skeleton";

  return `${GITHUB_BASE}/assets/enemy/${prefix}${fileName}.png`; 
};

export const getRandomEnemyData = () => {
   return { name: "Goblin", img: getEnemyImg("idle_goblin.png") };
};

export const getItemImage = (type) => {
  if (!type) return resolveItemImage("r_stone_3.png");

  // Mapping Type từ DB sang Tên file
  const itemMapping = {
    "GOLD": "r_coin.png",
    "Gỗ": "r_wood.png",
    "Đá": "stone_1.png",
    "Đồng": "r_copper_bar.png",
    "Cá": "r_fish.png",
    "Sắt": "r_iron_ore.png",
    "Bạch Kim": "r_platinum.png",
    "Gỗ Khô": "r_red_wood.png",
    "Gỗ Lạnh": "r_white_wood.png",
    "Gỗ Lạ": "r_black_wood.png",
    "Cá Mập": "r_shark.png",
  };

  return resolveItemImage(itemMapping[type] || "r_stone_3.png");
};

// Export alias để tương thích các file cũ đang gọi getAssetUrl
export const getAssetUrl = resolveItemImage;