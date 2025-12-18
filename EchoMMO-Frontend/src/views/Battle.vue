<!-- <template>
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
</style> -->
<!-- <template>
  <div class="page-container battle-page">
    <div class="battle-bg-layer"></div>
    <div class="battle-layout">
      
      <aside class="side-panel left-panel">
        <div class="panel-header">
          <i class="fas fa-skull text-red"></i> BẢNG SÁT THẦN
        </div>
        
        <div class="leaderboard-list custom-scroll">
          <div v-if="lbStore.loadingMonsters" class="loading-mini">
             <div class="spinner"></div> Đang tải...
          </div>

          <div 
            v-else 
            class="rank-item" 
            v-for="(entry, index) in lbStore.topMonsters" 
            :key="index"
          >
            <div class="rank-num" :class="'top-' + (index + 1)">{{ index + 1 }}</div>
            <div class="rank-avatar">
              <span v-if="entry.avatar" style="font-size: 1.2em">{{ entry.avatar }}</span>
              <img v-else src="https://cdn-icons-png.flaticon.com/512/149/149071.png" />
            </div>
            <div class="rank-info">
              <div class="rank-name">{{ entry.username }}</div>
              <div class="rank-power text-red">
                 <i class="fas fa-skull-crossbones"></i> {{ entry.value }}
              </div>
            </div>
          </div>
          
          <div v-if="!lbStore.loadingMonsters && lbStore.topMonsters.length === 0" class="empty-mini">
             Chưa có ai
          </div>
        </div>
      </aside>

      <main class="center-panel">
        <div
          class="combat-arena"
          v-if="battleStore.enemy && battleStore.status !== 'LOADING' && battleStore.status !== 'ERROR'"
        >
          <div class="vs-badge">VS</div>

          <div class="fighter-card enemy" :class="{ 'hit-anim': isEnemyHit }">
            <div class="fighter-visual">
              <div class="fighter-circle brown-ring">
                <img :src="getEnemyAsset(battleStore.enemy.name)" class="fighter-img" />
              </div>
              <div class="damage-text" v-if="isEnemyHit">-{{ lastDamage }}</div>
            </div>
            <div class="fighter-stats">
              <div class="name-tag red-tag">
                {{ battleStore.enemy.name }} (Lv.{{ battleStore.enemy.level || "?" }})
              </div>
              <div class="stat-bar-box">
                <div
                  class="bar-fill hp-fill"
                  :style="{ width: percent(battleStore.enemyHp, battleStore.enemyMaxHp) + '%' }"
                ></div>
                <span class="bar-text">{{ battleStore.enemyHp }} / {{ battleStore.enemyMaxHp }}</span>
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
                  :style="{ width: percent(battleStore.playerHp, battleStore.playerMaxHp) + '%' }"
                ></div>
                <span class="bar-text">{{ battleStore.playerHp }} / {{ battleStore.playerMaxHp }}</span>
              </div>
              <div class="stat-bar-box energy-box">
                <div
                  class="bar-fill energy-fill"
                  :style="{
                    width: percent(charStore.character?.energy, charStore.character?.maxEnergy) + '%',
                  }"
                ></div>
                <span class="bar-text">
                  <i class="fas fa-bolt"></i> {{ charStore.character?.energy || 0 }}
                </span>
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

            <div v-else class="result-actions">
              <div class="result-text" :class="battleStore.status">
                {{ battleStore.status === "VICTORY" ? "CHIẾN THẮNG!" : "THẤT BẠI..." }}
              </div>
              <div v-if="battleStore.droppedItem" class="loot-display">
                🎁 Nhặt được: {{ battleStore.droppedItem.name }}
              </div>
              <div class="btn-group">
                <button class="btn-nav forest-btn" @click="$router.push('/explore')">
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
              <button class="btn-nav forest-btn" @click="$router.push('/explore')">
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
          <ChatPanel height="100%" />
        </div>
      </main>

      <aside class="side-panel right-panel">
        <div class="char-detail-card">
          <div class="panel-header">THÔNG TIN</div>
          <div class="detail-row">
            <span><i class="fas fa-fist-raised"></i> Tấn Công</span>
            <span class="val">{{ charStore.character?.baseAtk || 0 }}</span>
          </div>
          <div class="detail-row">
            <span><i class="fas fa-shield-alt"></i> Phòng Thủ</span>
            <span class="val">{{ charStore.character?.baseDef || 0 }}</span>
          </div>
          <div class="detail-row">
            <span><i class="fas fa-wind"></i> Thân Pháp</span>
            <span class="val">{{ charStore.character?.baseSpeed || 0 }}</span>
          </div>
          <div class="detail-row">
            <span><i class="fas fa-bolt"></i> Chí Mạng</span>
            <span class="val">{{ charStore.character?.baseCritRate || 0 }}%</span>
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
// [MỚI] Import store Leaderboard
import { useLeaderboardStore } from "@/stores/leaderboardStore";

import { getEnemyImage, getCurrentSkin } from "@/utils/assetHelper";
import ChatPanel from "@/components/ChatPanel.vue";

const battleStore = useBattleStore();
const charStore = useCharacterStore();
const authStore = useAuthStore();
// [MỚI] Khởi tạo store Leaderboard
const lbStore = useLeaderboardStore();

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
  { deep: true }
);

watch(
  () => battleStore.status,
  (st) => {
    if (st !== "ONGOING") {
      clearInterval(autoInterval);
      if (qteInterval) clearInterval(qteInterval);
    }
  }
);

onMounted(async () => {
  await charStore.fetchCharacter();
  
  // [MỚI] Gọi API lấy bảng Sát Thần khi vào trận
  await lbStore.fetchMonsterBoard();

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

.chat-section {
  flex: 1; 
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden; 
  min-height: 200px; 
  background: transparent;
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

.rank-num.top-1 { background: var(--gold); color: #000; }
.rank-num.top-2 { background: #c0c0c0; color: #000; }
.rank-num.top-3 { background: #cd7f32; color: #000; }

.rank-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #000;
}

.rank-info { flex: 1; }
.rank-name { font-size: 0.9em; font-weight: bold; }
.rank-power { font-size: 0.75em; color: var(--text-muted); }

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

.fighter-card.player.attacking { transform: translateX(40px) scale(1.1); }
.fighter-card.player { bottom: 100px; left: 10%; }
.fighter-card.enemy { top: 60px; right: 10%; }

.fighter-visual { position: relative; }
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

.green-ring { border: 3px solid var(--green-player); }
.brown-ring { border: 3px solid #8d6e63; }

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

.player-dmg { color: #ff5252; }

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
.red-tag { background: #b71c1c; color: white; }
.blue-tag { background: #1565c0; color: white; }

.stat-bar-box {
  width: 100%;
  height: 14px;
  background: #333;
  border-radius: 2px;
  position: relative;
  margin-bottom: 2px;
  border: 1px solid #000;
}
.energy-box { height: 6px; width: 80%; margin: 0 auto; }
.bar-fill { height: 100%; transition: width 0.3s; }
.hp-fill { background: linear-gradient(to right, #d32f2f, #f44336); }
.energy-fill { background: linear-gradient(to right, #1976d2, #42a5f5); }

.bar-text {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.65em; font-weight: bold;
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
  display: flex; justify-content: center; gap: 15px; align-items: center;
}
.auto-fight-btn {
  background: #333; color: #aaa; padding: 8px 15px;
  border-radius: 20px; font-size: 0.8em; font-weight: bold;
  display: flex; gap: 5px; align-items: center;
}
.spinner {
  width: 12px; height: 12px;
  border: 2px solid #aaa; border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s infinite linear;
}
.btn-skill {
  background: var(--gold); color: #000; font-weight: bold;
  border: none; padding: 10px 20px; border-radius: 4px;
  cursor: pointer; box-shadow: 0 0 10px rgba(253, 216, 53, 0.3);
  transition: 0.2s;
}
.btn-skill:disabled {
  background: #555; color: #888; cursor: not-allowed; box-shadow: none;
}
.qte-overlay {
  position: absolute; top: 50%; left: 50%;
  transform: translate(-50%, -50%); z-index: 100;
}
.qte-button {
  font-size: 2rem; padding: 20px 40px; background: #e74c3c;
  color: white; border: 4px solid #c0392b;
  animation: pulse 0.5s infinite; cursor: pointer;
}
.result-text {
  font-size: 2em; font-weight: 900; margin-bottom: 10px;
  text-shadow: 0 0 10px black;
}
.result-text.VICTORY { color: #4caf50; }
.result-text.DEFEAT { color: #f44336; }

.btn-group {
  display: flex; gap: 15px; justify-content: center; margin-top: 10px;
}
.btn-nav {
  background: #3e2723; color: #fff; border: 1px solid #5d4037;
  padding: 10px 20px; cursor: pointer; border-radius: 4px;
  font-weight: bold; transition: transform 0.1s;
}
.btn-nav:active { transform: scale(0.95); }
.forest-btn {
  background: #2e7d32; border-color: #1b5e20;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}
.forest-btn:hover { background: #388e3c; }

.char-detail-card { padding: 10px; margin-bottom: 15px; }
.detail-row {
  display: flex; justify-content: space-between;
  padding: 5px 0; border-bottom: 1px dashed #333; font-size: 0.9em;
}
.detail-row .val { color: var(--gold); font-weight: bold; }

.combat-log-panel { flex: 1; display: flex; flex-direction: column; }
.logs-container {
  flex: 1; padding: 10px; overflow-y: auto;
  font-size: 0.8em; font-family: monospace;
}
.log-entry { margin-bottom: 4px; }
.log-player { color: #fff176; }
.log-enemy { color: #ef9a9a; }
.log-win { color: #a5d6a7; font-weight: bold; }
.log-normal { color: #bdbdbd; }

.loading-arena {
  flex: 2; display: flex; justify-content: center;
  align-items: center; flex-direction: column;
  color: #aaa; font-size: 1.2em;
}
.error-box { text-align: center; }
.text-red { color: #ff5252; }
.loot-display { margin-bottom: 10px; font-weight: bold; color: gold; }

.loading-mini, .empty-mini {
  padding: 20px; text-align: center; color: #aaa;
  display: flex; align-items: center; justify-content: center; gap: 8px;
}

@media (max-width: 900px) {
  .battle-layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto auto;
    height: auto;
    padding-bottom: 60px;
  }
  .center-panel { order: 1; }
  .right-panel { order: 2; }
  .left-panel { order: 3; display: none; }
  .combat-arena { height: 400px; }
  .chat-section { height: 250px; min-height: auto; }
  .fighter-card.player { left: 5%; bottom: 80px; }
  .fighter-card.enemy { right: 5%; top: 40px; }
}

@keyframes floatUp {
  0% { transform: translate(-50%, 0); opacity: 1; }
  100% { transform: translate(-50%, -30px); opacity: 0; }
}
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
</style> -->
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
               <div class="rank-name" :class="{'text-gold': index === 0}">{{ entry.username }}</div>
               <div class="rank-sub">
                 <i class="fas fa-skull-crossbones text-red"></i> 
                 <span class="kill-count">{{ entry.value }}</span>
               </div>
            </div>
            
            <div class="rank-avatar-mini">
               <img v-if="!entry.avatar" src="https://cdn-icons-png.flaticon.com/512/149/149071.png" />
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
            <div
              class="combat-arena"
              v-if="battleStore.enemy"
            >
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
                    <div class="bar-fill hp-fill" :style="{ width: percent(battleStore.enemyHp, battleStore.enemyMaxHp) + '%' }"></div>
                  </div>
                </div>
              </div>

              <div class="fighter-card player" :class="{ 'hit-anim': isPlayerHit, attacking: isPlayerAttacking }">
                <div class="fighter-visual">
                  <div class="fighter-circle green-ring">
                    <img :src="getPlayerAsset()" class="fighter-img" />
                  </div>
                  <div class="damage-text player-dmg" v-if="isPlayerHit">
                    -{{ lastDamageTaken }}
                  </div>
                </div>
                <div class="fighter-stats">
                  <div class="name-tag blue-tag">{{ authStore.user?.username }}</div>
                  <div class="stat-bar-box">
                    <div class="bar-fill hp-fill" :style="{ width: percent(battleStore.playerHp, battleStore.playerMaxHp) + '%' }"></div>
                  </div>
                  <div class="stat-bar-box energy-box">
                    <div class="bar-fill energy-fill" :style="{ width: percent(charStore.character?.energy, charStore.character?.maxEnergy) + '%' }"></div>
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
                   <button class="btn-skill" @click="activateBuff" :disabled="nextAttackBuffed || (charStore.character?.energy || 0) < 5">
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

                     <div v-if="battleStore.droppedItem" class="loot-display" :class="battleStore.droppedItem.rarity">
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
                <p>{{ battleStore.combatLogs[0] }}</p>
                <button @click="$router.push('/village')" class="btn-big village">🏠 Về Làng</button>
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
          <div class="panel-header">CHỈ SỐ</div>
          <div class="detail-row"><span>⚔️ Tấn Công</span><span class="val">{{ charStore.character?.baseAtk || 0 }}</span></div>
          <div class="detail-row"><span>🛡️ Phòng Thủ</span><span class="val">{{ charStore.character?.baseDef || 0 }}</span></div>
          <div class="detail-row"><span>🦶 Thân Pháp</span><span class="val">{{ charStore.character?.baseSpeed || 0 }}</span></div>
          <div class="detail-row"><span>⚡ Chí Mạng</span><span class="val">{{ charStore.character?.baseCritRate || 0 }}%</span></div>
        </div>

        <div class="combat-log-panel">
          <div class="panel-header">NHẬT KÝ</div>
          <div class="logs-container custom-scroll" ref="logContainer">
            <div v-for="(log, idx) in battleStore.combatLogs" :key="idx" class="log-entry" :class="getLogClass(log)">
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
import { useRouter } from "vue-router";
import { getEnemyImage, getCurrentSkin } from "@/utils/assetHelper";
import ChatPanel from "@/components/ChatPanel.vue";

const battleStore = useBattleStore();
const charStore = useCharacterStore();
const authStore = useAuthStore();
const lbStore = useLeaderboardStore();
const router = useRouter();

const logContainer = ref(null);
const fireworksCanvas = ref(null); // Ref cho canvas pháo hoa

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

// --- HÀM BẮN PHÁO HOA (Không dùng thư viện ngoài) ---
const triggerFireworks = () => {
  const canvas = fireworksCanvas.value;
  if (!canvas) return;
  
  const ctx = canvas.getContext('2d');
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
  
  const particles = [];
  const colors = ['#FFD700', '#FF0000', '#00FF00', '#0000FF', '#FF00FF', '#00FFFF', '#FFFFFF'];
  
  // Tạo hạt pháo hoa
  const createExplosion = (x, y) => {
    for (let i = 0; i < 50; i++) {
      particles.push({
        x: x,
        y: y,
        vx: (Math.random() - 0.5) * 10,
        vy: (Math.random() - 0.5) * 10,
        alpha: 1,
        color: colors[Math.floor(Math.random() * colors.length)],
        size: Math.random() * 3 + 1
      });
    }
  };

  // Vòng lặp Animation
  const animate = () => {
    ctx.globalCompositeOperation = 'destination-out';
    ctx.fillStyle = 'rgba(0, 0, 0, 0.1)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.globalCompositeOperation = 'lighter';

    particles.forEach((p, index) => {
      p.x += p.vx;
      p.y += p.vy;
      p.vy += 0.05; // Gravity
      p.alpha -= 0.01;
      
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

  // Kích hoạt nổ ở vài điểm ngẫu nhiên
  createExplosion(canvas.width / 2, canvas.height / 2);
  setTimeout(() => createExplosion(canvas.width / 3, canvas.height / 3), 200);
  setTimeout(() => createExplosion(canvas.width * 2 / 3, canvas.height / 3), 400);
  
  animate();
};

watch(() => battleStore.combatLogs, () => {
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight;
  });
}, { deep: true });

// --- [UPDATE] LOGIC KẾT THÚC TRẬN ---
watch(() => battleStore.status, (st) => {
  if (st === "VICTORY") {
    // 1. Dừng vòng lặp auto
    clearInterval(autoInterval);
    if (qteInterval) clearInterval(qteInterval);
    
    // 2. Cập nhật lại thông tin nhân vật để khớp với Gold/Exp mới
    charStore.fetchCharacter();

    // 3. Kiểm tra nếu có đồ xịn (RARE trở lên) thì bắn pháo hoa
    const item = battleStore.droppedItem;
    if (item) {
        const rareTypes = ['RARE', 'EPIC', 'LEGENDARY', 'MYTHIC'];
        if (rareTypes.includes(item.rarity) || rareTypes.includes(item.baseRarity)) {
            setTimeout(triggerFireworks, 500); // Đợi bảng hiện ra rồi bắn
        }
    }
  } else if (st !== "ONGOING") {
    clearInterval(autoInterval);
    if (qteInterval) clearInterval(qteInterval);
  }
});

onMounted(async () => {
  await charStore.fetchCharacter();
  lbStore.fetchMonsterBoard();
  if (battleStore.enemy && battleStore.status === "ONGOING") {
     startAutoLoop();
  } else {
     startBattle();
  }
});

onUnmounted(() => {
  clearInterval(autoInterval);
  if (qteInterval) clearInterval(qteInterval);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;700&family=Cinzel:wght@700;900&display=swap");
@import url('https://fonts.googleapis.com/css2?family=Bangers&display=swap');

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
  position: relative; /* Cho canvas đè lên */
}

/* Canvas pháo hoa */
.fireworks-canvas {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none; /* Không chặn click */
  z-index: 9999; /* Luôn nổi trên cùng */
}

.battle-bg-layer {
  position: absolute; inset: 0;
  background-image: url("@/assets/Background/b_doanhtrai.png");
  background-size: cover; background-position: center;
  opacity: 0.3;
  z-index: 0; pointer-events: none;
  filter: blur(1px);
}

/* === LAYOUT === */
.battle-layout {
  position: relative; z-index: 10;
  display: grid;
  grid-template-columns: 240px 1fr 240px;
  gap: 12px;
  max-width: 1600px;
  margin: 0 auto;
  height: 100%;
}

.side-panel, .center-panel {
  display: flex; flex-direction: column;
  height: 100%; overflow: hidden;
}

/* PANEL STYLE */
.leaderboard-list, .combat-log-panel, .char-detail-card {
  background: rgba(20, 20, 20, 0.85);
  border: 1px solid #444;
  border-radius: 12px;
  overflow: hidden;
  display: flex; flex-direction: column;
  box-shadow: 0 8px 20px rgba(0,0,0,0.6);
}

.leaderboard-list { flex: 1; }
.combat-log-panel { flex: 1; margin-top: 10px; }

.panel-header {
  background: linear-gradient(90deg, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0) 100%);
  padding: 12px; font-weight: bold; font-size: 0.95em;
  border-bottom: 1px solid #444;
  text-align: center; color: var(--gold); letter-spacing: 1.5px;
  text-transform: uppercase;
}

/* === RANKING === */
.rank-item {
  display: flex; align-items: center; padding: 10px 12px;
  border-bottom: 1px solid #333; gap: 10px; transition: 0.2s;
}
.rank-item:hover { background: rgba(255,255,255,0.08); }

.rank-num {
  width: 26px; height: 26px; border-radius: 50%;
  background: #333; color: #fff; display: flex;
  align-items: center; justify-content: center;
  font-weight: bold; font-size: 0.8em;
  box-shadow: 0 2px 5px rgba(0,0,0,0.5);
}
.top-1 { background: linear-gradient(135deg, #FFD700, #FDB931); color: #000; box-shadow: 0 0 8px #FFD700; }
.top-2 { background: linear-gradient(135deg, #E0E0E0, #BDBDBD); color: #000; }
.top-3 { background: linear-gradient(135deg, #CD7F32, #A0522D); color: #fff; }

.rank-content { flex: 1; overflow: hidden; }
.rank-name { font-weight: bold; font-size: 0.95em; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rank-sub { font-size: 0.8em; color: #aaa; margin-top: 2px; }
.rank-avatar-mini img { width: 36px; height: 36px; border-radius: 50%; border: 2px solid #555; }

/* === CENTER ARENA === */
.arena-wrapper {
  flex: 1; display: flex; flex-direction: column;
  min-height: 0; margin-bottom: 10px; position: relative;
}

.chat-section-wrapper {
  height: 200px; flex-shrink: 0;
  background: rgba(0,0,0,0.6); border-radius: 8px;
  box-shadow: 0 -4px 10px rgba(0,0,0,0.5);
  border: 1px solid #333;
}

.combat-arena, .loading-arena {
  width: 100%; height: 100%;
  background: radial-gradient(ellipse at center bottom, rgba(60, 40, 40, 0.8) 0%, rgba(10, 10, 10, 0.95) 70%);
  border: 2px solid #5c2b2b;
  border-radius: 12px;
  position: relative;
  display: flex; flex-direction: column;
  justify-content: center; align-items: center;
  box-shadow: inset 0 0 100px rgba(0,0,0,0.9);
  overflow: hidden;
}

.vs-badge {
  position: absolute; top: 40%; left: 50%;
  transform: translate(-50%, -50%);
  font-family: "Cinzel", serif;
  font-size: 6em; font-weight: 900;
  background: linear-gradient(to bottom, #d32f2f, #580808);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 20px rgba(211, 47, 47, 0.6));
  z-index: 0; opacity: 0.4;
}

.fighter-card {
  position: absolute; display: flex; flex-direction: column; align-items: center;
  width: 220px; z-index: 5;
  transition: transform 0.1s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.fighter-card.player { bottom: 100px; left: 12%; }
.fighter-card.enemy { top: 60px; right: 12%; }
.fighter-card.player.attacking { transform: translateX(100px) scale(1.1); z-index: 10; }

.fighter-visual {
  position: relative; animation: breathing 3s ease-in-out infinite;
  width: 100%; display: flex; justify-content: center;
}

.fighter-circle {
  width: auto; height: 200px; margin: 0 auto;
  display: flex; justify-content: center; align-items: flex-end;
  position: relative;
}

.fighter-img {
  width: auto; height: 100%; max-width: 100%; object-fit: contain;
  filter: drop-shadow(0 15px 15px rgba(0,0,0,0.7)); transition: filter 0.2s;
}

.hit-anim .fighter-circle { animation: shake 0.4s cubic-bezier(.36,.07,.19,.97) both; }
.hit-anim .fighter-img { filter: brightness(3) sepia(1) hue-rotate(-50deg) drop-shadow(0 15px 15px rgba(0,0,0,0.7)); }

.damage-text {
  position: absolute; top: -30px; left: 50%; transform: translateX(-50%);
  font-family: 'Bangers', cursive, sans-serif; font-size: 3.5rem;
  color: #ffeb3b; text-shadow: 3px 3px 0 #b71c1c, -2px -2px 0 #000;
  animation: popUp 0.8s cubic-bezier(0.68, -0.55, 0.27, 1.55) forwards;
  z-index: 20; pointer-events: none; white-space: nowrap;
}
.player-dmg { color: #ff5252; text-shadow: 3px 3px 0 #3e2723, -2px -2px 0 #000; }

.fighter-stats {
  width: 100%; margin-top: 5px; text-align: center; padding: 5px;
  text-shadow: 1px 1px 3px rgba(0,0,0,0.8);
}

.name-tag {
  font-size: 1em; font-weight: bold; color: white; display: inline-block;
  margin-bottom: 5px; letter-spacing: 0.5px; font-family: "Cinzel", serif;
}
.red-tag { color: #ff8a80; } .blue-tag { color: #90caf9; }

.stat-bar-box {
  width: 100%; height: 14px; background: #0a0a0a; margin-top: 4px;
  border-radius: 7px; overflow: hidden; border: 1px solid #333; position: relative;
  box-shadow: inset 0 2px 5px rgba(0,0,0,1);
}
.energy-box { height: 10px; width: 90%; margin: 6px auto 0; }

.bar-fill { height: 100%; transition: width 0.3s ease-out; position: relative; border-radius: 7px; }
.bar-fill::after {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 40%;
  background: rgba(255,255,255,0.25); border-radius: 7px 7px 0 0;
}
.hp-fill { background: linear-gradient(to bottom, #ff5252, #c62828); box-shadow: 0 0 15px rgba(239, 83, 80, 0.6); }
.energy-fill { background: linear-gradient(to bottom, #42a5f5, #1565c0); box-shadow: 0 0 15px rgba(66, 165, 245, 0.6); }

.combat-controls { position: absolute; bottom: 0; left: 0; width: 100%; height: 100%; pointer-events: none; }
.ongoing-actions, .qte-overlay, .result-overlay button { pointer-events: auto; }

.ongoing-actions {
  position: absolute; bottom: 30px; width: 100%; text-align: center;
  display: flex; justify-content: center; gap: 20px;
}

.btn-skill {
  background: linear-gradient(to bottom, #FFD700, #FBC02D);
  color: #3e2723; font-weight: 900; font-size: 1.1em;
  border: 2px solid #F57F17; padding: 12px 30px; border-radius: 8px; cursor: pointer;
  box-shadow: 0 5px 15px rgba(253, 216, 53, 0.4), inset 0 2px 0 rgba(255,255,255,0.5);
  text-transform: uppercase; letter-spacing: 1px; transition: transform 0.1s, filter 0.2s;
}
.btn-skill:hover { filter: brightness(1.1); transform: translateY(-2px); }
.btn-skill:active { transform: translateY(1px); box-shadow: 0 2px 5px rgba(253, 216, 53, 0.4); }
.btn-skill:disabled { background: #555; border-color: #333; color: #888; box-shadow: none; transform: none; cursor: not-allowed; }

.qte-overlay { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 100; }
.qte-button {
  font-size: 2.5rem; padding: 20px 50px; background: #d32f2f; color: white;
  border: 6px solid #b71c1c; border-radius: 10px; font-family: 'Bangers', sans-serif;
  letter-spacing: 2px; animation: pulseQTE 0.6s infinite; cursor: pointer;
  box-shadow: 0 0 30px rgba(211, 47, 47, 0.8);
}

.result-overlay {
  position: absolute; inset: 0; background: rgba(0, 0, 0, 0.85);
  display: flex; justify-content: center; align-items: center;
  z-index: 50; animation: fadeIn 0.3s ease-out; pointer-events: auto;
}
.result-content { text-align: center; display: flex; flex-direction: column; align-items: center; gap: 15px; }
.result-title {
  font-family: 'Cinzel', serif; font-size: 4rem; font-weight: 900;
  letter-spacing: 4px; text-transform: uppercase; margin-bottom: 10px;
}
.result-title.VICTORY {
  color: transparent; background: linear-gradient(to bottom, #FFD700, #FFA000);
  -webkit-background-clip: text; text-shadow: 0 0 30px rgba(255, 215, 0, 0.6);
  animation: zoomIn 0.5s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
.result-title.DEFEAT { color: #B0BEC5; text-shadow: 0 0 20px rgba(176, 190, 197, 0.5); }

/* --- REWARDS STYLING --- */
.rewards-row {
  display: flex; gap: 20px; margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.05); padding: 10px 20px;
  border-radius: 8px; border: 1px solid #444;
}
.reward-item {
  display: flex; align-items: center; gap: 8px; font-size: 1.2em; font-weight: bold; color: #fff;
}
.reward-item .icon { font-size: 1.4em; }
.reward-item .val { color: #a5d6a7; text-shadow: 0 0 5px rgba(76, 175, 80, 0.5); }

/* Loot Styling */
.loot-display {
  width: 100%; max-width: 300px;
  background: linear-gradient(90deg, rgba(20,20,20,0.9), rgba(40,40,40,0.9));
  padding: 10px; border-radius: 8px; border: 1px solid #555;
  display: flex; align-items: center; gap: 15px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5); position: relative; overflow: hidden;
}
/* Hiệu ứng viền cho đồ hiếm */
.loot-display.RARE, .loot-display.EPIC { border-color: #9c27b0; box-shadow: 0 0 15px rgba(156, 39, 176, 0.4); }
.loot-display.LEGENDARY { border-color: #ffca28; box-shadow: 0 0 20px rgba(255, 202, 40, 0.6); }

.loot-icon { font-size: 2em; animation: bounce 2s infinite; }
.loot-info { text-align: left; }
.loot-label { font-size: 0.7em; color: #aaa; letter-spacing: 1px; }
.loot-name { font-size: 1.1em; font-weight: bold; color: #FFD700; text-shadow: 0 0 5px #FFD700; }

.btn-group-large { display: flex; gap: 20px; margin-top: 15px; }
.btn-big {
  padding: 15px 30px; font-size: 1.2rem; font-weight: bold;
  font-family: 'Inter', sans-serif; color: white;
  border: none; border-radius: 8px; cursor: pointer;
  display: flex; align-items: center; gap: 10px;
  transition: all 0.2s; box-shadow: 0 5px 15px rgba(0,0,0,0.5);
}
.btn-big:hover { transform: translateY(-3px) scale(1.02); filter: brightness(1.1); box-shadow: 0 8px 20px rgba(0,0,0,0.6); }
.forest { background: linear-gradient(to bottom, #43a047, #2e7d32); border-bottom: 4px solid #1b5e20; }
.village { background: linear-gradient(to bottom, #8d6e63, #6d4c41); border-bottom: 4px solid #4e342e; }

/* === RESPONSIVE === */
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
</style>