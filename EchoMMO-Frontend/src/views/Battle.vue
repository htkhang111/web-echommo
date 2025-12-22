<template>
  <div class="page-container battle-page">
    <div class="battle-bg-layer"></div>
    <canvas ref="fireworksCanvas" class="fireworks-canvas"></canvas>

    <div class="battle-layout">
      <aside class="side-panel left-panel">
        <div class="panel-header">
          <i class="fas fa-skull text-red"></i> BẢNG SÁT THẦN
        </div>
        <div class="leaderboard-list custom-scroll">
          <div v-if="lbStore.loadingMonsters" class="loading-mini">
            <div class="spinner"></div>
          </div>
          <div
            v-else
            class="rank-item"
            v-for="(entry, index) in lbStore.topMonsters"
            :key="index"
          >
            <div class="rank-num" :class="'top-' + (index + 1)">{{ index + 1 }}</div>
            <div class="rank-content">
              <div class="rank-name" :class="{ 'text-gold': index === 0 }">
                {{ entry.username }}
              </div>
              <div class="rank-sub">
                <i class="fas fa-skull-crossbones text-red"></i>
                <span class="kill-count">{{ entry.value }}</span>
              </div>
            </div>
            <div class="rank-avatar-mini">
              <img
                v-if="!entry.avatar"
                src="https://cdn-icons-png.flaticon.com/512/149/149071.png"
              />
              <span v-else>{{ entry.avatar }}</span>
            </div>
          </div>
          <div v-if="!lbStore.loadingMonsters && lbStore.topMonsters.length === 0" class="empty-mini">
            Chưa có dữ liệu
          </div>
        </div>
      </aside>

      <main class="center-panel">
        <div class="arena-wrapper">
          <div class="combat-arena" v-if="battleStore.enemy">
            <div class="vs-badge">VS</div>

            <div class="fighter-card enemy" :class="{ 'hit-anim': isEnemyHit }">
              <div class="fighter-visual">
                <div class="fighter-circle brown-ring">
                  <img :src="getEnemyAsset(battleStore.enemy.name)" class="fighter-img" />
                </div>
                <div class="damage-text" v-if="isEnemyHit">-{{ lastDamage }}</div>
              </div>
              <div class="fighter-stats">
                <div class="name-tag red-tag">{{ battleStore.enemy.name }}</div>
                <div class="stat-bar-box">
                  <div
                    class="bar-fill hp-fill"
                    :style="{ width: percent(battleStore.enemyHp, battleStore.enemyMaxHp) + '%' }"
                  ></div>
                </div>
              </div>
            </div>

            <div
              class="fighter-card player"
              :class="{ 'hit-anim': isPlayerHit, attacking: isPlayerAttacking }"
            >
              <div class="fighter-visual">
                <div class="fighter-circle green-ring">
                  <img :src="getPlayerAsset()" class="fighter-img" />
                </div>
                <div class="damage-text player-dmg" v-if="isPlayerHit">-{{ lastDamageTaken }}</div>
              </div>
              <div class="fighter-stats">
                <div class="name-tag blue-tag">{{ authStore.user?.username }}</div>
                
                <div class="stat-bar-box">
                  <div
                    class="bar-fill hp-fill"
                    :style="{ width: percent(battleStore.playerHp, totalStats.hp) + '%' }"
                  ></div>
                </div>
                
                <div class="stat-bar-box energy-box">
                  <div
                    class="bar-fill energy-fill"
                    :style="{ width: percent(charStore.character?.energy, charStore.character?.maxEnergy) + '%' }"
                  ></div>
                </div>
              </div>
            </div>

            <div class="combat-controls">
              <div v-if="showQTE" class="qte-overlay">
                <button class="qte-button pixel-btn danger" @click="handleBlock">
                  🛡️ ĐỠ ĐÒN! ({{ qteTimer.toFixed(1) }}s)
                </button>
              </div>

              <div v-else-if="battleStore.status === 'ONGOING'" class="ongoing-actions">
                <div class="auto-fight-btn">
                  <div class="spinner"></div> AUTO FIGHT
                </div>
                <button
                  class="btn-skill"
                  @click="activateBuff"
                  :disabled="nextAttackBuffed || (charStore.character?.energy || 0) < 5"
                >
                  <span v-if="!nextAttackBuffed">🔥 TỤ LỰC (5⚡)</span>
                  <span v-else>SẴN SÀNG!</span>
                </button>
              </div>

              <div v-else class="result-overlay">
                <div class="result-content">
                  <div class="result-title" :class="battleStore.status">
                    {{ battleStore.status === "VICTORY" ? "CHIẾN THẮNG" : "THẤT BẠI" }}
                  </div>

                  <div v-if="battleStore.status === 'VICTORY'" class="rewards-row">
                    <div class="reward-item">
                      <span class="icon">💰</span>
                      <span class="val">+{{ battleStore.rewardGold || 0 }} Vàng</span>
                    </div>
                    <div class="reward-item">
                      <span class="icon">✨</span>
                      <span class="val">+{{ battleStore.rewardExp || 0 }} EXP</span>
                    </div>
                  </div>

                  <div
                    v-if="battleStore.droppedItem"
                    class="loot-display"
                    :class="battleStore.droppedItem.rarity"
                  >
                    <div class="loot-icon">🎁</div>
                    <div class="loot-info">
                      <div class="loot-label">NHẶT ĐƯỢC</div>
                      <div class="loot-name">{{ battleStore.droppedItem.name }}</div>
                    </div>
                  </div>

                  <div class="btn-group-large">
                    <button class="btn-big forest" @click="$router.push('/explore')">
                      <i class="fas fa-tree"></i> Về Rừng
                    </button>
                    <button class="btn-big village" @click="$router.push('/village')">
                      <i class="fas fa-home"></i> Về Làng
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="loading-arena">
            <div v-if="battleStore.status === 'ERROR'" class="error-box">
              <h3 class="text-red">LỖI</h3>
              <p>{{ battleStore.combatLogs[battleStore.combatLogs.length - 1] }}</p>
              <button @click="$router.push('/village')" class="btn-big village">
                🏠 Về Làng
              </button>
            </div>
            <div v-else class="searching-anim">
              <div class="swords-cross">⚔️</div>
              <h3>Đang tìm đối thủ...</h3>
            </div>
          </div>
        </div>

        <div class="chat-section-wrapper">
          <ChatPanel height="100%" />
        </div>
      </main>

      <aside class="side-panel right-panel">
        <div class="char-detail-card">
          <div class="panel-header">CHỈ SỐ (ĐÃ CỘNG ĐỒ)</div>
          <div class="detail-row">
            <span>⚔️ Tấn Công</span>
            <span class="val">{{ totalStats.atk }}</span>
          </div>
          <div class="detail-row">
            <span>🛡️ Phòng Thủ</span>
            <span class="val">{{ totalStats.def }}</span>
          </div>
          <div class="detail-row">
            <span>❤️ Máu Tối Đa</span>
            <span class="val">{{ totalStats.hp }}</span>
          </div>
          <div class="detail-row">
            <span>🦶 Thân Pháp</span>
            <span class="val">{{ totalStats.speed }}</span>
          </div>
          <div class="detail-row">
            <span>⚡ Chí Mạng</span>
            <span class="val">{{ totalStats.crit }}%</span>
          </div>
        </div>

        <div class="combat-log-panel">
          <div class="panel-header">NHẬT KÝ</div>
          <div class="logs-container custom-scroll" ref="logContainer">
            <div
              v-for="(log, idx) in battleStore.combatLogs"
              :key="idx"
              class="log-entry"
              :class="getLogClass(log)"
            >
              {{ formatLog(log) }}
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from "vue";
import { useBattleStore } from "@/stores/battleStore";
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useLeaderboardStore } from "@/stores/leaderboardStore";
import { useInventoryStore } from "@/stores/inventoryStore"; // Import thêm Inventory
import { useRouter, useRoute } from "vue-router";
import { getEnemyImage, getCurrentSkin, getAssetUrl } from "@/utils/assetHelper";
import ChatPanel from "@/components/ChatPanel.vue";

const battleStore = useBattleStore();
const charStore = useCharacterStore();
const authStore = useAuthStore();
const lbStore = useLeaderboardStore();
const inventoryStore = useInventoryStore(); // Khởi tạo inventory store
const router = useRouter();
const route = useRoute();

const logContainer = ref(null);
const fireworksCanvas = ref(null);

// State Battle
const isEnemyHit = ref(false);
const isPlayerHit = ref(false);
const isPlayerAttacking = ref(false);
const nextAttackBuffed = ref(false);
const lastDamage = ref(0);
const lastDamageTaken = ref(0);

const showQTE = ref(false);
const qteTimer = ref(0);
let qteInterval = null;
let autoInterval = null;

// Helpers
const percent = (cur, max) => (max > 0 ? (cur / max) * 100 : 0);
const getEnemyAsset = (name) => getEnemyImage(name, "idle");
const getPlayerAsset = () => {
  const skin = getCurrentSkin(authStore.user?.avatarUrl);
  return isPlayerAttacking.value ? skin.sprites.attack : skin.sprites.idle;
};
const getLogClass = (log) => {
  if (!log) return "log-normal";
  if (log.includes("Bạn gây") || log.includes("BẠO KÍCH")) return "log-player";
  if (log.includes("bị đánh") || log.includes("mất")) return "log-enemy";
  if (log.includes("Thắng") || log.includes("EXP")) return "log-win";
  return "log-normal";
};
const formatLog = (log) => (log ? log.replace(/<[^>]*>/g, "") : "");

// --- [CORE FIX] TÍNH TỔNG CHỈ SỐ (Logic copy từ Character Page) ---
const safeVal = (obj, ...keys) => {
  if (!obj) return 0;
  for (const k of keys) {
    if (obj[k] !== undefined && obj[k] !== null) return Number(obj[k]);
  }
  return 0;
};

const equipment = computed(() => {
  const mapped = {};
  const allItems = inventoryStore.items || [];
  allItems.forEach((userItem) => {
    if (userItem && userItem.item && userItem.isEquipped) {
      const typeStr = userItem.item.slotType || userItem.item.slot_type || "";
      const slot = typeStr.toUpperCase();
      if (slot) mapped[slot] = userItem;
    }
  });
  return mapped;
});

const totalStats = computed(() => {
  const char = charStore.character || {};
  
  // 1. Attribute Base
  let totalAttr = {
    str: safeVal(char, 'str'),
    vit: safeVal(char, 'vit'),
    agi: safeVal(char, 'agi'),
    dex: safeVal(char, 'dex'),
    int: safeVal(char, 'intelligence', 'int', 'int_stat'),
    luck: safeVal(char, 'luck'),
  };

  // 2. Combat Base
  let combat = {
    atk: 5 + totalAttr.str * 2,
    def: 2 + Math.floor(totalAttr.vit / 3),
    hp: 100 + totalAttr.vit * 15,
    speed: 10 + totalAttr.agi,
    crit: 1 + totalAttr.luck / 10,
  };

  // 3. Equipment Bonus
  Object.values(equipment.value).forEach((ui) => {
    if (!ui || !ui.item) return;
    const curDur = safeVal(ui, 'currentDurability', 'current_durability');
    // Hỏng thì không tính
    if (curDur <= 0 && (ui.maxDurability || ui.max_durability)) return;

    const type = (ui.mainStatType || "").toUpperCase();
    const val = safeVal(ui, 'mainStatValue', 'main_stat_value');

    // A. Main Stat
    if (val > 0) {
      if (type.includes("ATK")) combat.atk += val;
      else if (type.includes("DEF")) combat.def += val;
      else if (type.includes("HP")) combat.hp += val;
      else if (type.includes("SPEED")) combat.speed += val;
      else if (type.includes("CRIT")) combat.crit += val;
      else if (type === "ATK_PERCENT") combat.atk += (5 + totalAttr.str * 2) * (val / 100);
      else if (type === "HP_PERCENT") combat.hp += (100 + totalAttr.vit * 15) * (val / 100);
    }

    // B. Base Stat Fallback (Chỉ số gốc của item)
    const baseAtk = safeVal(ui.item, 'atkBonus', 'atk_bonus');
    const baseDef = safeVal(ui.item, 'defBonus', 'def_bonus');
    const baseHp = safeVal(ui.item, 'hpBonus', 'hp_bonus');
    const baseSpeed = safeVal(ui.item, 'speedBonus', 'speed_bonus');

    if (!type.includes("ATK")) combat.atk += baseAtk;
    if (!type.includes("DEF")) combat.def += baseDef;
    if (!type.includes("HP")) combat.hp += baseHp;
    if (!type.includes("SPEED")) combat.speed += baseSpeed;

    // C. Substats
    if (ui.subStats) {
      let subs = [];
      try { subs = typeof ui.subStats === "string" ? JSON.parse(ui.subStats) : ui.subStats; } catch (e) {}
      if (Array.isArray(subs)) {
        subs.forEach((stat) => {
          const sType = (stat.code || stat.type || "").toUpperCase();
          const sVal = Number(stat.value || 0);
          if (sType.includes("ATK")) combat.atk += sVal;
          else if (sType.includes("DEF")) combat.def += sVal;
          else if (sType.includes("HP")) combat.hp += sVal;
          else if (sType.includes("SPEED")) combat.speed += sVal;
          else if (sType.includes("CRIT")) combat.crit += sVal;
        });
      }
    }
  });

  return {
    atk: Math.floor(combat.atk),
    def: Math.floor(combat.def),
    speed: Math.floor(combat.speed),
    crit: parseFloat(combat.crit.toFixed(2)),
    hp: Math.floor(combat.hp),
  };
});

// --- BATTLE LOGIC ---
const startBattle = async () => {
  battleStore.resetBattle();
  await battleStore.startBattle();
  if (battleStore.isReady) startAutoLoop();
};

const startAutoLoop = () => {
  if (autoInterval) clearInterval(autoInterval);
  autoInterval = setInterval(runAutoTurn, 1500);
};

const activateBuff = () => {
  if (charStore.character?.energy >= 5) {
    charStore.character.energy -= 5;
    nextAttackBuffed.value = true;
  }
};

const runAutoTurn = async () => {
  if (!battleStore.isReady || battleStore.status !== "ONGOING" || showQTE.value) return;

  isPlayerAttacking.value = true;
  setTimeout(() => (isPlayerAttacking.value = false), 500);

  const prevEnemyHp = battleStore.enemyHp;
  const prevPlayerHp = battleStore.playerHp;

  // --- QUAN TRỌNG: TRUYỀN STATS VÀO STORE ĐỂ TÍNH DAME ---
  const currentStats = {
    atk: totalStats.value.atk,
    def: totalStats.value.def,
    hp: totalStats.value.hp,
    speed: totalStats.value.speed,
    crit: totalStats.value.crit
  };

  const res = await battleStore.autoTurn(nextAttackBuffed.value, currentStats);

  if (!res) {
    clearInterval(autoInterval);
    return;
  }

  if (res.status === "QTE_ACTION" || res.qteTriggered) {
    triggerQTE();
    return;
  }

  if (nextAttackBuffed.value) nextAttackBuffed.value = false;

  const dmgDealt = prevEnemyHp - res.enemyHp;
  if (dmgDealt > 0) {
    lastDamage.value = dmgDealt;
    isEnemyHit.value = true;
    setTimeout(() => (isEnemyHit.value = false), 300);
  }

  const dmgTaken = prevPlayerHp - res.playerHp;
  if (dmgTaken > 0) {
    setTimeout(() => {
      lastDamageTaken.value = dmgTaken;
      isPlayerHit.value = true;
      setTimeout(() => (isPlayerHit.value = false), 300);
    }, 600);
  }
};

const triggerQTE = () => {
  clearInterval(autoInterval);
  showQTE.value = true;
  qteTimer.value = 0.75;
  qteInterval = setInterval(() => {
    qteTimer.value -= 0.05;
    if (qteTimer.value <= 0) failQTE();
  }, 50);
};

const handleBlock = async () => {
  clearInterval(qteInterval);
  showQTE.value = false;
  await battleStore.sendAction("BLOCK");
  startAutoLoop();
};

const failQTE = async () => {
  clearInterval(qteInterval);
  showQTE.value = false;
  isPlayerHit.value = true;
  setTimeout(() => (isPlayerHit.value = false), 500);
  await battleStore.sendAction("IGNORE_QTE");
  startAutoLoop();
};

// FIREWORKS
const triggerFireworks = () => {
  const canvas = fireworksCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
  const particles = [];
  const colors = ["#FFD700", "#FF0000", "#00FF00", "#0000FF", "#FF00FF", "#00FFFF", "#FFFFFF"];
  const createExplosion = (x, y) => {
    for (let i = 0; i < 50; i++) {
      particles.push({
        x: x, y: y,
        vx: (Math.random() - 0.5) * 10,
        vy: (Math.random() - 0.5) * 10,
        alpha: 1,
        color: colors[Math.floor(Math.random() * colors.length)],
        size: Math.random() * 3 + 1,
      });
    }
  };
  const animate = () => {
    ctx.globalCompositeOperation = "destination-out";
    ctx.fillStyle = "rgba(0, 0, 0, 0.1)";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.globalCompositeOperation = "lighter";
    particles.forEach((p, index) => {
      p.x += p.vx; p.y += p.vy;
      p.vy += 0.05; p.alpha -= 0.01;
      ctx.globalAlpha = p.alpha;
      ctx.fillStyle = p.color;
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      ctx.fill();
      if (p.alpha <= 0) particles.splice(index, 1);
    });
    if (particles.length > 0) requestAnimationFrame(animate);
    else ctx.clearRect(0, 0, canvas.width, canvas.height);
  };
  createExplosion(canvas.width / 2, canvas.height / 2);
  setTimeout(() => createExplosion(canvas.width / 3, canvas.height / 3), 200);
  setTimeout(() => createExplosion((canvas.width * 2) / 3, canvas.height / 3), 400);
  animate();
};

watch(() => battleStore.combatLogs, () => {
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight;
  });
}, { deep: true });

watch(() => battleStore.status, (st) => {
  if (st === "VICTORY") {
    clearInterval(autoInterval);
    if (qteInterval) clearInterval(qteInterval);
    charStore.fetchCharacter();
    const item = battleStore.droppedItem;
    if (item) {
      const rareTypes = ["RARE", "EPIC", "LEGENDARY", "MYTHIC"];
      if (rareTypes.includes(item.rarity) || rareTypes.includes(item.baseRarity)) {
        setTimeout(triggerFireworks, 500);
      }
    }
  } else if (st !== "ONGOING") {
    clearInterval(autoInterval);
    if (qteInterval) clearInterval(qteInterval);
  }
});

onMounted(async () => {
  // Lấy data nhân vật VÀ data túi đồ để tính chỉ số
  await Promise.all([
    charStore.fetchCharacter(),
    inventoryStore.fetchInventory()
  ]);
  
  if (lbStore.fetchMonsterBoard) lbStore.fetchMonsterBoard();
  
  if (battleStore.enemy && battleStore.status === "ONGOING") startAutoLoop();
  else startBattle();
});

onUnmounted(() => {
  clearInterval(autoInterval);
  if (qteInterval) clearInterval(qteInterval);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;700&family=Cinzel:wght@700;900&display=swap");
@import url("https://fonts.googleapis.com/css2?family=Bangers&display=swap");

:root {
  --bg-dark: #121212;
  --bg-panel: #1e1e1e;
  --border-color: #333;
  --text-main: #e0e0e0;
  --text-muted: #aaa;
  --gold: #fdd835;
  --red: #d32f2f;
  --green: #43a047;
}

.battle-page {
  background-color: var(--bg-dark);
  height: calc(100vh - 60px);
  color: var(--text-main);
  font-family: "Inter", sans-serif;
  overflow: hidden;
  padding: 10px;
  box-sizing: border-box;
  position: relative;
}

.fireworks-canvas {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  pointer-events: none; z-index: 9999;
}

.battle-bg-layer {
  position: absolute; inset: 0;
  background-image: url("@/assets/Background/b_doanhtrai.png");
  background-size: cover; background-position: center;
  opacity: 0.3; z-index: 0; pointer-events: none; filter: blur(1px);
}

.battle-layout {
  position: relative; z-index: 10;
  display: grid; grid-template-columns: 240px 1fr 240px;
  gap: 12px; max-width: 1600px; margin: 0 auto; height: 100%;
}

.side-panel, .center-panel {
  display: flex; flex-direction: column; height: 100%; overflow: hidden;
}

.leaderboard-list, .combat-log-panel, .char-detail-card {
  background: rgba(20, 20, 20, 0.85);
  border: 1px solid #444; border-radius: 12px;
  overflow: hidden; display: flex; flex-direction: column;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6);
}

.leaderboard-list { flex: 1; }
.combat-log-panel { flex: 1; margin-top: 10px; }

.panel-header {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0) 100%);
  padding: 12px; font-weight: bold; font-size: 0.95em;
  border-bottom: 1px solid #444; text-align: center;
  color: var(--gold); letter-spacing: 1.5px; text-transform: uppercase;
}

/* RANKING */
.rank-item {
  display: flex; align-items: center; padding: 10px 12px;
  border-bottom: 1px solid #333; gap: 10px; transition: 0.2s;
}
.rank-item:hover { background: rgba(255, 255, 255, 0.08); }
.rank-num {
  width: 26px; height: 26px; border-radius: 50%;
  background: #333; color: #fff; display: flex; align-items: center; justify-content: center;
  font-weight: bold; font-size: 0.8em; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);
}
.top-1 { background: linear-gradient(135deg, #ffd700, #fdb931); color: #000; box-shadow: 0 0 8px #ffd700; }
.top-2 { background: linear-gradient(135deg, #e0e0e0, #bdbdbd); color: #000; }
.top-3 { background: linear-gradient(135deg, #cd7f32, #a0522d); color: #fff; }

.rank-content { flex: 1; overflow: hidden; }
.rank-name { font-weight: bold; font-size: 0.95em; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rank-sub { font-size: 0.8em; color: #aaa; margin-top: 2px; }
.rank-avatar-mini img { width: 36px; height: 36px; border-radius: 50%; border: 2px solid #555; }

/* CENTER ARENA */
.arena-wrapper { flex: 1; display: flex; flex-direction: column; min-height: 0; margin-bottom: 10px; position: relative; }
.chat-section-wrapper {
  height: 200px; flex-shrink: 0; background: rgba(0, 0, 0, 0.6);
  border-radius: 8px; box-shadow: 0 -4px 10px rgba(0, 0, 0, 0.5); border: 1px solid #333;
}

.combat-arena, .loading-arena {
  width: 100%; height: 100%;
  background: radial-gradient(ellipse at center bottom, rgba(60, 40, 40, 0.8) 0%, rgba(10, 10, 10, 0.95) 70%);
  border: 2px solid #5c2b2b; border-radius: 12px; position: relative;
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  box-shadow: inset 0 0 100px rgba(0, 0, 0, 0.9); overflow: hidden;
}

.vs-badge {
  position: absolute; top: 40%; left: 50%; transform: translate(-50%, -50%);
  font-family: "Cinzel", serif; font-size: 6em; font-weight: 900;
  background: linear-gradient(to bottom, #d32f2f, #580808);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 20px rgba(211, 47, 47, 0.6)); z-index: 0; opacity: 0.4;
}

.fighter-card {
  position: absolute; display: flex; flex-direction: column; align-items: center;
  width: 220px; z-index: 5; transition: transform 0.1s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.fighter-card.player { bottom: 100px; left: 12%; }
.fighter-card.enemy { top: 60px; right: 12%; }
.fighter-card.player.attacking { transform: translateX(100px) scale(1.1); z-index: 10; }

.fighter-visual { position: relative; animation: breathing 3s ease-in-out infinite; width: 100%; display: flex; justify-content: center; }
.fighter-circle { width: auto; height: 200px; margin: 0 auto; display: flex; justify-content: center; align-items: flex-end; position: relative; }
.fighter-img { width: auto; height: 100%; max-width: 100%; object-fit: contain; filter: drop-shadow(0 15px 15px rgba(0, 0, 0, 0.7)); transition: filter 0.2s; }
.hit-anim .fighter-circle { animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both; }
.hit-anim .fighter-img { filter: brightness(3) sepia(1) hue-rotate(-50deg) drop-shadow(0 15px 15px rgba(0, 0, 0, 0.7)); }

.damage-text {
  position: absolute; top: -30px; left: 50%; transform: translateX(-50%);
  font-family: "Bangers", cursive, sans-serif; font-size: 3.5rem; color: #ffeb3b;
  text-shadow: 3px 3px 0 #b71c1c, -2px -2px 0 #000;
  animation: popUp 0.8s cubic-bezier(0.68, -0.55, 0.27, 1.55) forwards; z-index: 20; pointer-events: none; white-space: nowrap;
}
.player-dmg { color: #ff5252; text-shadow: 3px 3px 0 #3e2723, -2px -2px 0 #000; }

.fighter-stats { width: 100%; margin-top: 5px; text-align: center; padding: 5px; text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.8); }
.name-tag { font-size: 1em; font-weight: bold; color: white; display: inline-block; margin-bottom: 5px; letter-spacing: 0.5px; font-family: "Cinzel", serif; }
.red-tag { color: #ff8a80; }
.blue-tag { color: #90caf9; }

.stat-bar-box { width: 100%; height: 14px; background: #0a0a0a; margin-top: 4px; border-radius: 7px; overflow: hidden; border: 1px solid #333; position: relative; box-shadow: inset 0 2px 5px rgba(0, 0, 0, 1); }
.energy-box { height: 10px; width: 90%; margin: 6px auto 0; }
.bar-fill { height: 100%; transition: width 0.3s ease-out; position: relative; border-radius: 7px; }
.bar-fill::after { content: ""; position: absolute; top: 0; left: 0; right: 0; height: 40%; background: rgba(255, 255, 255, 0.25); border-radius: 7px 7px 0 0; }
.hp-fill { background: linear-gradient(to bottom, #ff5252, #c62828); box-shadow: 0 0 15px rgba(239, 83, 80, 0.6); }
.energy-fill { background: linear-gradient(to bottom, #42a5f5, #1565c0); box-shadow: 0 0 15px rgba(66, 165, 245, 0.6); }

/* CONTROLS */
.combat-controls { position: absolute; bottom: 0; left: 0; width: 100%; height: 100%; pointer-events: none; }
.ongoing-actions, .qte-overlay, .result-overlay button { pointer-events: auto; }
.ongoing-actions { position: absolute; bottom: 30px; width: 100%; text-align: center; display: flex; justify-content: center; gap: 20px; }
.auto-fight-btn {
  background: #333; color: #aaa; padding: 12px 30px; border-radius: 8px; font-size: 1.1em;
  font-weight: bold; display: flex; gap: 10px; align-items: center; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); font-family: "Inter", sans-serif;
}
.spinner { width: 14px; height: 14px; border: 2px solid #aaa; border-top-color: transparent; border-radius: 50%; animation: spin 1s infinite linear; }
.btn-skill {
  background: linear-gradient(to bottom, #ffd700, #fbc02d); color: #3e2723; font-weight: 900; font-size: 1.1em;
  border: 2px solid #f57f17; padding: 12px 30px; border-radius: 8px; cursor: pointer;
  box-shadow: 0 5px 15px rgba(253, 216, 53, 0.4), inset 0 2px 0 rgba(255, 255, 255, 0.5);
  text-transform: uppercase; letter-spacing: 1px; transition: transform 0.1s, filter 0.2s;
}
.btn-skill:hover { filter: brightness(1.1); transform: translateY(-2px); }
.btn-skill:active { transform: translateY(1px); box-shadow: 0 2px 5px rgba(253, 216, 53, 0.4); }
.btn-skill:disabled { background: #555; border-color: #333; color: #888; box-shadow: none; transform: none; cursor: not-allowed; }

.qte-overlay { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 100; }
.qte-button {
  font-size: 2.5rem; padding: 20px 50px; background: #d32f2f; color: white; border: 6px solid #b71c1c; border-radius: 10px;
  font-family: "Bangers", sans-serif; letter-spacing: 2px; animation: pulseQTE 0.6s infinite; cursor: pointer; box-shadow: 0 0 30px rgba(211, 47, 47, 0.8);
}

/* RESULT */
.result-overlay { position: absolute; inset: 0; background: rgba(0, 0, 0, 0.85); display: flex; justify-content: center; align-items: center; z-index: 50; animation: fadeIn 0.3s ease-out; pointer-events: auto; }
.result-content { text-align: center; display: flex; flex-direction: column; align-items: center; gap: 15px; }
.result-title { font-family: "Cinzel", serif; font-size: 4rem; font-weight: 900; letter-spacing: 4px; text-transform: uppercase; margin-bottom: 10px; }
.result-title.VICTORY {
  color: transparent; background: linear-gradient(to bottom, #ffd700, #ffa000); -webkit-background-clip: text;
  text-shadow: 0 0 30px rgba(255, 215, 0, 0.6); animation: zoomIn 0.5s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
.result-title.DEFEAT { color: #b0bec5; text-shadow: 0 0 20px rgba(176, 190, 197, 0.5); }

.rewards-row { display: flex; gap: 20px; margin-bottom: 10px; background: rgba(255, 255, 255, 0.05); padding: 10px 20px; border-radius: 8px; border: 1px solid #444; }
.reward-item { display: flex; align-items: center; gap: 8px; font-size: 1.2em; font-weight: bold; color: #fff; }
.reward-item .icon { font-size: 1.4em; }
.reward-item .val { color: #a5d6a7; text-shadow: 0 0 5px rgba(76, 175, 80, 0.5); }

.loot-display {
  width: 100%; max-width: 300px; background: linear-gradient(90deg, rgba(20, 20, 20, 0.9), rgba(40, 40, 40, 0.9));
  padding: 10px; border-radius: 8px; border: 1px solid #555; display: flex; align-items: center; gap: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); position: relative; overflow: hidden;
}
.loot-display.RARE, .loot-display.EPIC { border-color: #9c27b0; box-shadow: 0 0 15px rgba(156, 39, 176, 0.4); }
.loot-display.LEGENDARY { border-color: #ffca28; box-shadow: 0 0 20px rgba(255, 202, 40, 0.6); }

.loot-icon { font-size: 2em; animation: bounce 2s infinite; }
.loot-info { text-align: left; }
.loot-label { font-size: 0.7em; color: #aaa; letter-spacing: 1px; }
.loot-name { font-size: 1.1em; font-weight: bold; color: #ffd700; text-shadow: 0 0 5px #ffd700; }

.btn-group-large { display: flex; gap: 20px; margin-top: 15px; }
.btn-big {
  padding: 15px 30px; font-size: 1.2rem; font-weight: bold; font-family: "Inter", sans-serif; color: white;
  border: none; border-radius: 8px; cursor: pointer; display: flex; align-items: center; gap: 10px;
  transition: all 0.2s; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.btn-big:hover { transform: translateY(-3px) scale(1.02); filter: brightness(1.1); box-shadow: 0 8px 20px rgba(0, 0, 0, 0.6); }
.forest { background: linear-gradient(to bottom, #43a047, #2e7d32); border-bottom: 4px solid #1b5e20; }
.village { background: linear-gradient(to bottom, #8d6e63, #6d4c41); border-bottom: 4px solid #4e342e; }

/* RESPONSIVE */
@media (max-width: 900px) {
  .battle-layout { grid-template-columns: 1fr; grid-template-rows: auto auto auto auto; height: auto; padding-bottom: 80px; }
  .left-panel, .right-panel { display: none; }
  .battle-page { height: auto; overflow: auto; }
  .arena-wrapper { height: 500px; }
  .fighter-card { width: 160px; }
  .fighter-circle { height: 110px; }
  .fighter-card.player { left: 5%; bottom: 100px; }
  .fighter-card.enemy { right: 5%; top: 50px; }
}

@keyframes breathing { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }
@keyframes popUp { 0% { opacity: 0; transform: translate(-50%, 20px) scale(0.5); } 50% { opacity: 1; transform: translate(-50%, -20px) scale(1.2); } 100% { opacity: 0; transform: translate(-50%, -60px) scale(1); } }
@keyframes shake { 10%, 90% { transform: translate3d(-2px, 0, 0); } 20%, 80% { transform: translate3d(4px, 0, 0); } 30%, 50%, 70% { transform: translate3d(-6px, 0, 0); } 40%, 60% { transform: translate3d(6px, 0, 0); } }
@keyframes zoomIn { from { transform: scale(0.5); opacity: 0; } to { transform: scale(1); opacity: 1; } }
@keyframes pulseQTE { 0% { transform: scale(1); box-shadow: 0 0 0 0 rgba(211, 47, 47, 0.7); } 70% { transform: scale(1.05); box-shadow: 0 0 0 15px rgba(211, 47, 47, 0); } 100% { transform: scale(1); box-shadow: 0 0 0 0 rgba(211, 47, 47, 0); } }
@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>