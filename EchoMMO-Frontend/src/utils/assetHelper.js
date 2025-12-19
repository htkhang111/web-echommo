import { reactive } from "vue";

// URL gốc của kho tài nguyên GitHub Pages
const GITHUB_BASE = "https://htkhang111.github.io";

// Hàm xử lý đường dẫn ảnh chung (Items, Backgrounds, Logo)
export const resolveItemImage = (imgCode) => {
  if (!imgCode) return `${GITHUB_BASE}/resources/material/stone_1.png`;
  if (imgCode.startsWith("http")) return imgCode;

  let fileName = imgCode.trim();
  if (!fileName.match(/\.(png|jpg|jpeg|gif|webp)$/i)) fileName += ".png";
  const lowerName = fileName.toLowerCase();

  // 1. LOGO
  if (lowerName.includes("logo")) return `${GITHUB_BASE}/logo/Logo.png`;

  // 2. BACKGROUND (Thư mục 'background')
  if (lowerName.startsWith("b_")) return `${GITHUB_BASE}/background/${fileName}`;

  // 3. TÀI NGUYÊN - GỖ (Thư mục 'resources/material')
  if (["r_wood.png", "r_go.png", "v_wood.png"].includes(lowerName)) return `${GITHUB_BASE}/resources/material/w_wood.png`;
  if (lowerName === "r_red_wood.png") return `${GITHUB_BASE}/resources/material/w_woodRed.png`;
  if (lowerName === "r_black_wood.png") return `${GITHUB_BASE}/resources/material/w_woodBlack.png`;
  if (lowerName === "r_white_wood.png") return `${GITHUB_BASE}/resources/material/w_woodWhite.png`;

  // 4. TÀI NGUYÊN - KHOÁNG SẢN & NODE
  if (lowerName.includes("copper_node")) return `${GITHUB_BASE}/resources/material/r_copper_node.png`;
  if (lowerName.includes("gold_node")) return `${GITHUB_BASE}/resources/material/r_gold_node.png`;
  if (lowerName.includes("silver_node") || lowerName.includes("iron_node")) return `${GITHUB_BASE}/resources/material/r_silver_node.png`;
  if (lowerName.includes("mystrile_node") || lowerName.includes("platinum_node")) return `${GITHUB_BASE}/resources/material/r_mystrile_node.png`;

  if (lowerName.includes("copper") && !lowerName.includes("node")) return `${GITHUB_BASE}/resources/material/r_copper_bar.png`;
  if ((lowerName.includes("iron") || lowerName.includes("sat")) && !lowerName.includes("node")) return `${GITHUB_BASE}/resources/material/o_ironOre.png`;
  if (lowerName.includes("coal") || lowerName.includes("than")) return `${GITHUB_BASE}/resources/material/o_coalOre.png`;
  if (lowerName.includes("platinum") || lowerName.includes("bachkim")) return `${GITHUB_BASE}/resources/material/o_platinumOre.png`;
  if (lowerName.includes("strange")) return `${GITHUB_BASE}/resources/material/o_strangeOre.png`;

  // 5. TÀI NGUYÊN - ĐÁ & KHÁC
  if (["stone_1.png", "r_stone_3.png", "v_stone.png", "da"].includes(lowerName)) return `${GITHUB_BASE}/resources/material/stone_1.png`;
  if (lowerName.includes("fish") || lowerName === "ca") return `${GITHUB_BASE}/resources/material/r_fish.png`;
  if (lowerName.includes("shark")) return `${GITHUB_BASE}/resources/material/r_fishShark.png`;
  if (lowerName.includes("potion")) return `${GITHUB_BASE}/resources/material/r_potion.png`;

  // 6. TIỀN TỆ (Thư mục 'resources/coin')
  if (lowerName.includes("coin") || lowerName.includes("xu")) return `${GITHUB_BASE}/resources/coin/r_coin.png`;

  // 7. TRANG BỊ (Thư mục 'resources/equipment/...')
  if (lowerName.includes("sword") || lowerName.startsWith("s_")) return `${GITHUB_BASE}/resources/equipment/sword/${fileName}`;
  if (lowerName.includes("armor") || lowerName.startsWith("a_")) return `${GITHUB_BASE}/resources/equipment/armor/${fileName}`;
  if (lowerName.includes("helmet") || lowerName.startsWith("h_") || lowerName.startsWith("mu")) return `${GITHUB_BASE}/resources/equipment/helmet/${fileName}`;
  if (lowerName.includes("boot") || lowerName.startsWith("b_boot")) return `${GITHUB_BASE}/resources/equipment/boots/${fileName}`;
  if (lowerName.includes("ring") || lowerName.startsWith("ri_")) return `${GITHUB_BASE}/resources/equipment/ring/${fileName}`;
  if (lowerName.includes("neck") || lowerName.startsWith("n_")) return `${GITHUB_BASE}/resources/equipment/necklace/${fileName}`;

  // Fallback mặc định vào material nếu không tìm thấy
  return `${GITHUB_BASE}/resources/material/${fileName}`;
};

// --- HELPER LẤY ẢNH APP ---
export const getAppLogo = () => `${GITHUB_BASE}/logo/Logo.png`;
export const getAssetUrl = resolveItemImage; // Alias

// --- HELPER NHÂN VẬT (Character) ---
const getCharImg = (name) => `${GITHUB_BASE}/character/${name}`;

export const CHARACTER_SKINS = reactive({
  skin_yasou: { 
    id: "skin_yasou", name: "Yasuo", 
    sprites: { idle: getCharImg("idle_yasou.png"), run: getCharImg("run_yasou.png"), attack: getCharImg("atk_yasou.png") } 
  },
  skin_demon: { 
    id: "skin_demon", name: "Huyết Quỷ", 
    sprites: { idle: getCharImg("idle_demon1.png"), run: getCharImg("run_demon1.png"), attack: getCharImg("atk_demon1.png") } 
  },
  skin_langkhach: { 
    id: "skin_langkhach", name: "Lãng Khách", 
    sprites: { idle: getCharImg("idle_langkhach1.png"), run: getCharImg("run_langkhach1.png"), attack: getCharImg("atk_langkhach1.png") } 
  },
});

export const getCurrentSkin = (avatarUrl) => {
  return (avatarUrl && CHARACTER_SKINS[avatarUrl]) ? CHARACTER_SKINS[avatarUrl] : CHARACTER_SKINS["skin_yasou"];
};

// --- HELPER QUÁI VẬT (Enemy) ---
const getEnemyImg = (name) => `${GITHUB_BASE}/enemy/${name}`;

export const getEnemyImage = (name, state = "idle") => {
  if (!name) return getEnemyImg("idle_goblin.png");
  
  const normalizedName = name.toLowerCase().replace(/\s+/g, '');
  const prefix = state === 'attack' ? 'atk_' : 'idle_';
  let fileName = "goblin"; 
  
  if (normalizedName.includes("xuong") || normalizedName.includes("skeleton")) fileName = "skeleton";
  else if (normalizedName.includes("nam") || normalizedName.includes("mushroom")) fileName = "mushroom";
  else if (normalizedName.includes("yeutinh") || normalizedName.includes("goblin")) fileName = "goblin";

  return getEnemyImg(`${prefix}${fileName}.png`);
};

export const getItemImage = (type) => resolveItemImage("r_stone_3.png");