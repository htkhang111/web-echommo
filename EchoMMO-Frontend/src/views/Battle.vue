<template>
  <div class="page-container battle-page">
    <div class="battle-bg-layer"></div>
    <div class="battle-layout">
      <aside class="side-panel left-panel">
        <div class="panel-header">
          <i class="fas fa-trophy text-gold"></i> BẢNG XẾP HẠNG
        </div>
        <div class="leaderboard-list custom-scroll">
          <div class="rank-item" v-for="i in 5" :key="i">
            <div class="rank-num" :class="'top-' + i">{{ i }}</div>
            <div class="rank-avatar">
              <img
                src="https://cdn-icons-png.flaticon.com/512/149/149071.png"
              />
            </div>
            <div class="rank-info">
              <div class="rank-name">Người Chơi {{ i }}</div>
              <div class="rank-power">Lực chiến: {{ 10000 - i * 500 }}</div>
            </div>
          </div>
        </div>
      </aside>

      <main class="center-panel">
        <div
          class="combat-arena"
          v-if="
            battleStore.enemy &&
            battleStore.status !== 'LOADING' &&
            battleStore.status !== 'ERROR'
          "
        >
          <div class="vs-badge">VS</div>

          <div class="fighter-card enemy" :class="{ 'hit-anim': isEnemyHit }">
            <div class="fighter-visual">
              <div class="fighter-circle brown-ring">
                <img
                  :src="getEnemyAsset(battleStore.enemy.name)"
                  class="fighter-img"
                />
              </div>
              <div class="damage-text" v-if="isEnemyHit">-{{ lastDamage }}</div>
            </div>
            <div class="fighter-stats">
              <div class="name-tag red-tag">
                {{ battleStore.enemy.name }} (Lv.{{
                  battleStore.enemy.level || "?"
                }})
              </div>
              <div class="stat-bar-box">
                <div
                  class="bar-fill hp-fill"
                  :style="{
                    width:
                      percent(battleStore.enemyHp, battleStore.enemyMaxHp) +
                      '%',
                  }"
                ></div>
                <span class="bar-text"
                  >{{ battleStore.enemyHp }} /
                  {{ battleStore.enemyMaxHp }}</span
                >
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
              <div class="damage-text player-dmg" v-if="isPlayerHit">
                -{{ lastDamageTaken }}
              </div>
            </div>
            <div class="fighter-stats">
              <div class="name-tag blue-tag">
                {{ authStore.user?.username }}
              </div>
              <div class="stat-bar-box">
                <div
                  class="bar-fill hp-fill"
                  :style="{
                    width:
                      percent(battleStore.playerHp, battleStore.playerMaxHp) +
                      '%',
                  }"
                ></div>
                <span class="bar-text"
                  >{{ battleStore.playerHp }} /
                  {{ battleStore.playerMaxHp }}</span
                >
              </div>
              <div class="stat-bar-box energy-box">
                <div
                  class="bar-fill energy-fill"
                  :style="{
                    width:
                      percent(
                        charStore.character?.energy,
                        charStore.character?.maxEnergy,
                      ) + '%',
                  }"
                ></div>
                <span class="bar-text"
                  ><i class="fas fa-bolt"></i>
                  {{ charStore.character?.energy || 0 }}</span
                >
              </div>
            </div>
          </div>

          <div class="combat-controls">
            <div v-if="showQTE" class="qte-overlay">
              <button class="qte-button pixel-btn danger" @click="handleBlock">
                🛡️ ĐỠ ĐÒN! ({{ qteTimer.toFixed(1) }}s)
              </button>
            </div>

            <div
              v-else-if="battleStore.status === 'ONGOING'"
              class="ongoing-actions"
            >
              <div class="auto-fight-btn">
                <div class="spinner"></div>
                AUTO FIGHT
              </div>
              <button
                class="btn-skill"
                @click="activateBuff"
                :disabled="
                  nextAttackBuffed || (charStore.character?.energy || 0) < 5
                "
              >
                <span v-if="!nextAttackBuffed">🔥 TỤ LỰC (5⚡)</span>
                <span v-else>SẴN SÀNG!</span>
              </button>
            </div>

            <div v-else class="result-actions">
              <div class="result-text" :class="battleStore.status">
                {{
                  battleStore.status === "VICTORY"
                    ? "CHIẾN THẮNG!"
                    : "THẤT BẠI..."
                }}
              </div>
              <div v-if="battleStore.droppedItem" class="loot-display">
                🎁 Nhặt được: {{ battleStore.droppedItem.name }}
              </div>

              <div class="btn-group">
                <button
                  class="btn-nav forest-btn"
                  @click="$router.push('/explore')"
                >
                  🌲 Về Rừng
                </button>
                <button class="btn-nav" @click="$router.push('/village')">
                  🏠 Về Làng
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="loading-arena">
          <div v-if="battleStore.status === 'ERROR'" class="error-box">
            <h2 class="text-red">⚠️ LỖI GAME</h2>
            <p v-for="(err, idx) in battleStore.combatLogs" :key="idx">
              {{ err }}
            </p>

            <div class="btn-group">
              <button
                class="btn-nav forest-btn"
                @click="$router.push('/explore')"
              >
                🌲 Thử Lại
              </button>
              <button @click="$router.push('/village')" class="btn-nav">
                🏠 Về Làng
              </button>
            </div>
          </div>
          <div v-else>
            <i class="fas fa-circle-notch fa-spin"></i>
            <h2>⚔️ Đang tìm đối thủ...</h2>
          </div>
        </div>

        <div class="chat-section">
          <div class="chat-header">KÊNH THẾ GIỚI</div>
          <div class="chat-messages custom-scroll">
            <div class="chat-msg system">
              <span class="sender">[Hệ thống]:</span> Chào mừng đại hiệp!
            </div>
          </div>
          <div class="chat-input-area">
            <input type="text" placeholder="Nhập tin nhắn..." />
            <button class="btn-send"><i class="fas fa-paper-plane"></i></button>
          </div>
        </div>
      </main>

      <aside class="side-panel right-panel">
        <div class="char-detail-card">
          <div class="panel-header">THÔNG TIN</div>
          <div class="detail-row">
            <span><i class="fas fa-fist-raised"></i> Tấn Công</span
            ><span class="val">{{ charStore.character?.baseAtk || 0 }}</span>
          </div>
          <div class="detail-row">
            <span><i class="fas fa-shield-alt"></i> Phòng Thủ</span
            ><span class="val">{{ charStore.character?.baseDef || 0 }}</span>
          </div>
          <div class="detail-row">
            <span><i class="fas fa-wind"></i> Thân Pháp</span
            ><span class="val">{{ charStore.character?.baseSpeed || 0 }}</span>
          </div>
          <div class="detail-row">
            <span><i class="fas fa-bolt"></i> Chí Mạng</span
            ><span class="val"
              >{{ charStore.character?.baseCritRate || 0 }}%</span
            >
          </div>
        </div>

        <div class="combat-log-panel">
          <div class="panel-header">NHẬT KÝ TRẬN ĐẤU</div>
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
import { useRouter } from "vue-router";
import { getEnemyImage, getCurrentSkin } from "@/utils/assetHelper";

const battleStore = useBattleStore();
const charStore = useCharacterStore();
const authStore = useAuthStore();
const router = useRouter();

const logContainer = ref(null);
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

// --- HELPERS ---
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

// --- LOGIC ---
const startBattle = async () => {
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
  if (!battleStore.isReady || battleStore.status !== "ONGOING" || showQTE.value)
    return;

  isPlayerAttacking.value = true;
  setTimeout(() => (isPlayerAttacking.value = false), 500);

  const prevEnemyHp = battleStore.enemyHp;
  const prevPlayerHp = battleStore.playerHp;

  const res = await battleStore.autoTurn(nextAttackBuffed.value);

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

watch(
  () => battleStore.combatLogs,
  () => {
    nextTick(() => {
      if (logContainer.value)
        logContainer.value.scrollTop = logContainer.value.scrollHeight;
    });
  },
  { deep: true },
);

watch(
  () => battleStore.status,
  (st) => {
    if (st !== "ONGOING") {
      clearInterval(autoInterval);
      if (qteInterval) clearInterval(qteInterval);
    }
  },
);

onMounted(async () => {
  await charStore.fetchCharacter();
  if (!battleStore.enemy) await startBattle();
  else if (battleStore.status === "ONGOING") startAutoLoop();
});

onUnmounted(() => {
  clearInterval(autoInterval);
  if (qteInterval) clearInterval(qteInterval);
  battleStore.resetBattle();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;700&family=Cinzel:wght@700&display=swap");

:root {
  --bg-dark: #121212;
  --bg-panel: #1e1e1e;
  --border-color: #333;
  --text-main: #e0e0e0;
  --text-muted: #aaa;
  --hp-red: #e53935;
  --energy-blue: #1e88e5;
  --gold: #fdd835;
  --green-player: #43a047;
}

.battle-page {
  background-color: var(--bg-dark);
  min-height: calc(100vh - 60px);
  color: var(--text-main);
  font-family: "Inter", sans-serif;
  overflow: hidden;
  padding: 10px;
}

.battle-bg-layer {
  position: absolute;
  inset: 0;
  background-image: url("@/assets/Background/b_doanhtrai.png");
  background-size: cover;
  background-position: center;
  opacity: 0.15;
  z-index: 0;
  pointer-events: none;
}

.battle-layout {
  position: relative;
  z-index: 10;
  display: grid;
  grid-template-columns: 250px 1fr 250px;
  gap: 15px;
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 80px);
}

.side-panel,
.center-panel {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.center-panel {
  min-width: 0;
}

.leaderboard-list,
.combat-arena,
.chat-section,
.char-detail-card,
.combat-log-panel {
  background: rgba(30, 30, 30, 0.9);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  backdrop-filter: blur(5px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
}

.panel-header {
  background: rgba(255, 255, 255, 0.05);
  padding: 10px;
  font-weight: bold;
  font-size: 0.9em;
  border-bottom: 1px solid var(--border-color);
  text-align: center;
  color: var(--text-muted);
  letter-spacing: 1px;
}

.leaderboard-list {
  flex: 1;
  overflow-y: auto;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #333;
  gap: 10px;
}

.rank-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.8em;
}

.rank-num.top-1 {
  background: var(--gold);
  color: #000;
}

.rank-num.top-2 {
  background: #c0c0c0;
  color: #000;
}

.rank-num.top-3 {
  background: #cd7f32;
  color: #000;
}

.rank-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #000;
}

.rank-info {
  flex: 1;
}

.rank-name {
  font-size: 0.9em;
  font-weight: bold;
}

.rank-power {
  font-size: 0.75em;
  color: var(--text-muted);
}

.combat-arena {
  flex: 2;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle, #2a1a1a 0%, #1a0f0f 80%);
  border: 1px solid #5c2b2b;
  padding: 20px;
}

.vs-badge {
  position: absolute;
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-family: "Cinzel", serif;
  font-size: 3em;
  font-weight: 900;
  color: #3e2723;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
  z-index: 0;
  opacity: 0.5;
}

.fighter-card {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 140px;
  z-index: 5;
  transition: transform 0.1s;
}

.fighter-card.player.attacking {
  transform: translateX(40px) scale(1.1);
}

.fighter-card.player {
  bottom: 100px;
  left: 10%;
}

.fighter-card.enemy {
  top: 60px;
  right: 10%;
}

.fighter-visual {
  position: relative;
}

.fighter-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.green-ring {
  border: 3px solid var(--green-player);
}

.brown-ring {
  border: 3px solid #8d6e63;
}

.fighter-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}

.hit-anim .fighter-img {
  filter: brightness(2) sepia(1) hue-rotate(-50deg);
  transform: translateX(-5px);
}

.damage-text {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 1.5em;
  font-weight: bold;
  color: #ffeb3b;
  text-shadow: 2px 2px 0 #b71c1c;
  animation: floatUp 0.5s ease-out forwards;
}

.player-dmg {
  color: #ff5252;
}

.fighter-stats {
  width: 100%;
  margin-top: 10px;
  text-align: center;
}

.name-tag {
  font-size: 0.8em;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 4px;
}

.red-tag {
  background: #b71c1c;
  color: white;
}

.blue-tag {
  background: #1565c0;
  color: white;
}

.stat-bar-box {
  width: 100%;
  height: 14px;
  background: #333;
  border-radius: 2px;
  position: relative;
  margin-bottom: 2px;
  border: 1px solid #000;
}

.energy-box {
  height: 6px;
  width: 80%;
  margin: 0 auto;
}

.bar-fill {
  height: 100%;
  transition: width 0.3s;
}

.hp-fill {
  background: linear-gradient(to right, #d32f2f, #f44336);
}

.energy-fill {
  background: linear-gradient(to right, #1976d2, #42a5f5);
}

.bar-text {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.65em;
  font-weight: bold;
  text-shadow: 1px 1px 0 #000;
  z-index: 2;
}

.combat-controls {
  position: absolute;
  bottom: 20px;
  width: 100%;
  text-align: center;
}

.ongoing-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  align-items: center;
}

.auto-fight-btn {
  background: #333;
  color: #aaa;
  padding: 8px 15px;
  border-radius: 20px;
  font-size: 0.8em;
  font-weight: bold;
  display: flex;
  gap: 5px;
  align-items: center;
}

.spinner {
  width: 12px;
  height: 12px;
  border: 2px solid #aaa;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s infinite linear;
}

.btn-skill {
  background: var(--gold);
  color: #000;
  font-weight: bold;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 0 10px rgba(253, 216, 53, 0.3);
  transition: 0.2s;
}

.btn-skill:disabled {
  background: #555;
  color: #888;
  cursor: not-allowed;
  box-shadow: none;
}

.qte-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 100;
}

.qte-button {
  font-size: 2rem;
  padding: 20px 40px;
  background: #e74c3c;
  color: white;
  border: 4px solid #c0392b;
  animation: pulse 0.5s infinite;
  cursor: pointer;
}

.result-text {
  font-size: 2em;
  font-weight: 900;
  margin-bottom: 10px;
  text-shadow: 0 0 10px black;
}

.result-text.VICTORY {
  color: #4caf50;
}

.result-text.DEFEAT {
  color: #f44336;
}

/* --- UPDATED CSS FOR BUTTONS --- */
.btn-group {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 10px;
}

.btn-nav {
  background: #3e2723;
  color: #fff;
  border: 1px solid #5d4037;
  padding: 10px 20px;
  cursor: pointer;
  border-radius: 4px;
  font-weight: bold;
  transition: transform 0.1s;
}

.btn-nav:active {
  transform: scale(0.95);
}

.forest-btn {
  background: #2e7d32;
  border-color: #1b5e20;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}

.forest-btn:hover {
  background: #388e3c;
}
/* ------------------------------- */

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(30, 20, 10, 0.9);
  border-color: #d84315;
  min-height: 150px;
}

.chat-header {
  background: #d84315;
  color: white;
  padding: 5px 10px;
  font-weight: bold;
  font-size: 0.8em;
}

.chat-messages {
  flex: 1;
  padding: 10px;
  font-size: 0.85em;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-msg .sender {
  font-weight: bold;
  margin-right: 5px;
}

.chat-msg.system {
  color: #ffd54f;
  font-style: italic;
}

.chat-input-area {
  display: flex;
  padding: 5px;
  border-top: 1px solid #5d4037;
}

.chat-input-area input {
  flex: 1;
  background: rgba(0, 0, 0, 0.3);
  border: none;
  padding: 5px 10px;
  color: white;
}

.btn-send {
  background: #d84315;
  border: none;
  color: white;
  padding: 0 15px;
  cursor: pointer;
}

.char-detail-card {
  padding: 10px;
  margin-bottom: 15px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
  border-bottom: 1px dashed #333;
  font-size: 0.9em;
}

.detail-row .val {
  color: var(--gold);
  font-weight: bold;
}

.combat-log-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.logs-container {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  font-size: 0.8em;
  font-family: monospace;
}

.log-entry {
  margin-bottom: 4px;
}

.log-player {
  color: #fff176;
}

.log-enemy {
  color: #ef9a9a;
}

.log-win {
  color: #a5d6a7;
  font-weight: bold;
}

.log-normal {
  color: #bdbdbd;
}

.loading-arena {
  flex: 2;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  color: #aaa;
  font-size: 1.2em;
}

.error-box {
  text-align: center;
}

.text-red {
  color: #ff5252;
}

.loot-display {
  margin-bottom: 10px;
  font-weight: bold;
  color: gold;
}

@media (max-width: 900px) {
  .battle-layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto auto;
    height: auto;
    padding-bottom: 60px;
  }

  .center-panel {
    order: 1;
  }

  .right-panel {
    order: 2;
  }

  .left-panel {
    order: 3;
    display: none;
  }

  .combat-arena {
    height: 400px;
  }

  .chat-section {
    height: 200px;
  }

  .fighter-card.player {
    left: 5%;
    bottom: 80px;
  }

  .fighter-card.enemy {
    right: 5%;
    top: 40px;
  }
}

@keyframes floatUp {
  0% {
    transform: translate(-50%, 0);
    opacity: 1;
  }

  100% {
    transform: translate(-50%, -30px);
    opacity: 0;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }

  100% {
    transform: scale(1);
  }
}
</style>
