import { reactive } from "vue";

const GITHUB_BASE = "https://htkhang111.github.io";

export const resolveItemImage = (imgCode) => {
  if (!imgCode) return `${GITHUB_BASE}/resources/stone_1.png`;
  if (imgCode.startsWith("http")) return imgCode;

  let fileName = imgCode.trim();
  if (!fileName.match(/\.(png|jpg|jpeg|gif|webp)$/i)) fileName += ".png";
  const lowerName = fileName.toLowerCase();

  // --- LOGO ---
  if (lowerName.includes("logo")) return `${GITHUB_BASE}/logo/Logo.png`;

  // --- BACKGROUND ---
  if (lowerName.startsWith("b_")) return `${GITHUB_BASE}/background/${fileName}`;

  // --- MAPPING TÀI NGUYÊN (GATHERING) ---
  // Gỗ
  if (["r_wood.png", "r_go.png", "v_wood.png"].includes(lowerName)) return `${GITHUB_BASE}/resources/material/w_wood.png`;
  if (lowerName === "r_red_wood.png") return `${GITHUB_BASE}/resources/material/w_woodRed.png`;
  if (lowerName === "r_black_wood.png") return `${GITHUB_BASE}/resources/material/w_woodBlack.png`;
  if (lowerName === "r_white_wood.png") return `${GITHUB_BASE}/resources/material/w_woodWhite.png`;

  // Khoáng sản (Nodes & Bars)
  if (lowerName.includes("copper_node")) return `${GITHUB_BASE}/resources/material/r_copper_node.png`; // Mới
  if (lowerName.includes("gold_node")) return `${GITHUB_BASE}/resources/material/r_gold_node.png`;     // Mới
  if (lowerName.includes("silver_node") || lowerName.includes("iron")) return `${GITHUB_BASE}/resources/material/r_silver_node.png`; // Mới
  if (lowerName.includes("mystrile_node") || lowerName.includes("platinum")) return `${GITHUB_BASE}/resources/material/r_mystrile_node.png`; // Mới

  if (lowerName.includes("copper")) return `${GITHUB_BASE}/resources/material/r_copper_bar.png`;
  if (lowerName.includes("iron_ore") || lowerName.includes("iron")) return `${GITHUB_BASE}/resources/material/o_ironOre.png`;
  if (lowerName.includes("coal")) return `${GITHUB_BASE}/resources/material/o_coalOre.png`;
  
  // Đá
  if (["stone_1.png", "r_stone_3.png", "v_stone.png"].includes(lowerName)) return `${GITHUB_BASE}/resources/stone_1.png`;

  // Sinh vật
  if (["r_fish.png", "v_fish.png"].includes(lowerName)) return `${GITHUB_BASE}/resources/material/r_fish.png`;
  if (lowerName === "r_shark.png") return `${GITHUB_BASE}/resources/material/r_fishShark.png`;
  if (lowerName === "r_potion.png") return `${GITHUB_BASE}/resources/material/r_potion.png`;

  // Tiền
  if (lowerName.includes("coin")) return `${GITHUB_BASE}/resources/coin/r_coin.png`;

  // --- TRANG BỊ ---
  if (lowerName.includes("sword") || lowerName.startsWith("s_")) return `${GITHUB_BASE}/resources/equipment/sword/${fileName}`;
  if (lowerName.includes("armor") || lowerName.startsWith("a_")) return `${GITHUB_BASE}/resources/equipment/armor/${fileName}`;
  if (lowerName.includes("helmet") || lowerName.startsWith("h_") || lowerName.startsWith("mu")) return `${GITHUB_BASE}/resources/equipment/helmet/${fileName}`;
  if (lowerName.includes("boot")) return `${GITHUB_BASE}/resources/equipment/boots/${fileName}`;
  if (lowerName.includes("ring") || lowerName.startsWith("ri_")) return `${GITHUB_BASE}/resources/equipment/ring/${fileName}`;
  if (lowerName.includes("neck") || lowerName.startsWith("n_")) return `${GITHUB_BASE}/resources/equipment/necklace/${fileName}`;

  return `${GITHUB_BASE}/resources/material/${fileName}`;
};

export const getAppLogo = () => `${GITHUB_BASE}/logo/Logo.png`;
export const getAssetUrl = resolveItemImage; // Alias

// --- HELPER QUÁI VẬT & NHÂN VẬT ---
const getCharImg = (name) => `${GITHUB_BASE}/character/${name}`;
const getEnemyImg = (name) => `${GITHUB_BASE}/enemy/${name}`;

export const CHARACTER_SKINS = reactive({
  skin_yasou: { id: "skin_yasou", name: "Yasuo", sprites: { idle: getCharImg("idle_yasou.png"), run: getCharImg("run_yasou.png"), attack: getCharImg("atk_yasou.png") } },
  skin_demon: { id: "skin_demon", name: "Huyết Quỷ", sprites: { idle: getCharImg("idle_demon1.png"), run: getCharImg("run_demon1.png"), attack: getCharImg("atk_demon1.png") } },
  skin_langkhach: { id: "skin_langkhach", name: "Lãng Khách", sprites: { idle: getCharImg("idle_langkhach1.png"), run: getCharImg("run_langkhach1.png"), attack: getCharImg("atk_langkhach1.png") } },
});

export const getCurrentSkin = (avatarUrl) => CHARACTER_SKINS[avatarUrl] || CHARACTER_SKINS["skin_yasou"];

export const getEnemyImage = (name, state = "idle") => {
  if (!name) return getEnemyImg("idle_goblin.png");
  const normalizedName = name.toLowerCase().replace(/\s+/g, '');
  const prefix = state === 'attack' ? 'atk_' : 'idle_';
  let fileName = "goblin"; // Default
  
  if (normalizedName.includes("xuong") || normalizedName.includes("skeleton")) fileName = "skeleton";
  else if (normalizedName.includes("nam") || normalizedName.includes("mushroom")) fileName = "mushroom";
  else if (normalizedName.includes("yeutinh") || normalizedName.includes("goblin")) fileName = "goblin";

  return getEnemyImg(`${prefix}${fileName}.png`);
};

export const getItemImage = (type) => resolveItemImage("r_stone_3.png"); // Fallback simple