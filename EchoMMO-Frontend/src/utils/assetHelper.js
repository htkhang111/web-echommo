import { reactive } from "vue";

const GITHUB_BASE = "https://htkhang111.github.io/htkhang111.github.io-2f01b0c63309bc6faf7c5589f3a6628b378c4ce8";

export const resolveItemImage = (imgCode) => {
  if (!imgCode) return `${GITHUB_BASE}/resources/material/o_coalOre.png`;
  if (imgCode.startsWith("http")) return imgCode;

  let fileName = imgCode.trim();
  if (!fileName.match(/\.(png|jpg|jpeg|gif|webp)$/i)) fileName += ".png";
  const lowerName = fileName.toLowerCase();

  // --- UI & SPECIAL ---
  if (lowerName.includes("logo")) return `${GITHUB_BASE}/logo/Logo.png`;
  if (lowerName.startsWith("b_")) return `${GITHUB_BASE}/background/${fileName}`;
  if (lowerName.includes("coin")) {
    if (lowerName.includes("echo")) return `${GITHUB_BASE}/resources/coin/r_coinEcho.png`;
    return `${GITHUB_BASE}/resources/coin/r_coin.png`;
  }

  // --- RESOURCES (Strict Mapping From Git) ---

  // Gỗ
  if (lowerName === "w_wood.png") return `${GITHUB_BASE}/resources/material/w_wood.png`;
  if (lowerName === "w_woodred.png") return `${GITHUB_BASE}/resources/material/w_woodRed.png`;
  if (lowerName === "w_woodwhite.png") return `${GITHUB_BASE}/resources/material/w_woodWhite.png`;
  if (lowerName === "w_woodblack.png") return `${GITHUB_BASE}/resources/material/w_woodBlack.png`;

  // Khoáng sản
  if (lowerName === "o_coalore.png") return `${GITHUB_BASE}/resources/material/o_coalOre.png`;
  if (lowerName === "o_copperore.png") return `${GITHUB_BASE}/resources/material/o_copperOre.png`;
  if (lowerName === "o_ironore.png") return `${GITHUB_BASE}/resources/material/o_ironOre.png`;
  if (lowerName === "o_platinumore.png") return `${GITHUB_BASE}/resources/material/o_platinumOre.png`;
  if (lowerName === "o_strangeore.png") return `${GITHUB_BASE}/resources/material/o_strangeOre.png`;
  if (lowerName === "o_goldore.png") return `${GITHUB_BASE}/resources/material/o_goldOre.png`;

  // Thủy sản & Khác
  if (lowerName === "f_fish.png" || lowerName === "r_fish.png") return `${GITHUB_BASE}/resources/material/r_fish.png`;
  if (lowerName === "f_fishshark.png" || lowerName === "r_fishshark.png") return `${GITHUB_BASE}/resources/material/r_fishShark.png`;
  if (lowerName === "f_fishmegalodon.png") return `${GITHUB_BASE}/resources/material/f_fishMegalodon.png`;
  if (lowerName === "r_potion.png") return `${GITHUB_BASE}/resources/material/r_potion.png`;

  // --- EQUIPMENT ---
  if (lowerName.startsWith("s_")) return `${GITHUB_BASE}/resources/equipment/sword/${fileName}`;
  if (lowerName.startsWith("a_")) return `${GITHUB_BASE}/resources/equipment/armor/${fileName}`;
  if (lowerName.startsWith("h_")) return `${GITHUB_BASE}/resources/equipment/helmet/${fileName}`;
  if (lowerName.startsWith("b_")) return `${GITHUB_BASE}/resources/equipment/boots/${fileName}`;
  if (lowerName.startsWith("ri_")) return `${GITHUB_BASE}/resources/equipment/ring/${fileName}`;
  if (lowerName.startsWith("n_")) return `${GITHUB_BASE}/resources/equipment/necklace/${fileName}`;

  return `${GITHUB_BASE}/resources/material/${fileName}`;
};

export const getAppLogo = () => `${GITHUB_BASE}/logo/Logo.png`;
export const getAssetUrl = resolveItemImage;

// Helper Nhân vật & Quái
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
  let fileName = "goblin";

  if (normalizedName.includes("xuong") || normalizedName.includes("skeleton")) fileName = "skeleton";
  else if (normalizedName.includes("nam") || normalizedName.includes("mushroom")) fileName = "mushroom";

  return getEnemyImg(`${prefix}${fileName}.png`);
};