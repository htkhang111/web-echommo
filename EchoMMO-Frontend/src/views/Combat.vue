<!-- <template>
  <div class="combat-page">
    <div class="combat-container">
      <transition name="bounce">
        <div v-if="showLevelUp" class="levelup-overlay">
          <h2>🌟 LEVEL UP! {{ playerStore.stats.lv }} 🌟</h2>
          <p>Full HP/Energy - Tăng sức mạnh!</p>
        </div>
      </transition>

      <h2
        class="combat-title"
        :class="{ 'text-red': isGameOver, 'text-green': isVictory }"
      >
        {{ combatLog }}
      </h2>

      <div v-if="showParryBtn" class="parry-overlay">
        <button class="btn-parry" @click="handleParry">🛡️ ĐỠ NGAY!</button>
        <div class="parry-timer-bar"></div>
      </div>

      <div class="battlefield">
        <div
          class="fighter enemy"
          :class="{ 'attack-anim': enemyAttacking, 'hit-anim': enemyHit }"
        >
          <div class="avatar-box">
            <img
              :src="currentEnemyImage"
              class="fighter-img enemy-img"
              alt="Enemy"
            />
          </div>
          <div class="name">{{ currentEnemyData.name }}</div>
          <div class="hp-bar-container">
            <div class="hp-bar" :style="{ width: visualEnemyHp + '%' }"></div>
          </div>
        </div>

        <div class="vs-text">VS</div>

        <div
          class="fighter player"
          :class="{ 'attack-anim': playerAttacking, 'hit-anim': playerHit }"
        >
          <div class="avatar-box">
            <img
              src="https://cdn-icons-png.flaticon.com/512/9408/9408175.png"
              class="fighter-img player-img"
            />
          </div>
          <div class="name">
            {{ playerStore.stats.name }}
            <span class="badge-lv">Lv.{{ playerStore.stats.lv }}</span>
          </div>

          <div class="hp-bar-container">
            <div
              class="hp-bar player-bar"
              :style="{ width: playerHpPercent + '%' }"
            ></div>
          </div>
          <div class="hp-text">
            {{ playerStore.stats.hp }} / {{ playerStore.stats.maxHp }}
          </div>

          <div class="exp-bar-container">
            <div
              class="hp-bar exp-bar"
              :style="{
                width:
                  (playerStore.stats.exp / playerStore.stats.nextLevelExp) *
                    100 +
                  '%',
              }"
            ></div>
          </div>
          <div class="exp-text">
            EXP: {{ playerStore.stats.exp }} /
            {{ playerStore.stats.nextLevelExp }}
          </div>

          <div class="energy-text" style="color: yellow; font-size: 0.9rem">
            ⚡ {{ playerStore.stats.energy }}
          </div>
        </div>
      </div>

      <div class="controls">
        <div v-if="!isGameOver && !isVictory">
          <button v-if="isAutoBattling" class="btn btn-disabled" disabled>
            🤖 Đang tự động đánh...
          </button>
          <template v-else>
            <button
              class="btn btn-attack"
              @click="playerTurn('normal')"
              :disabled="isBusy"
            >
              ⚔️ ĐÁNH THƯỜNG
            </button>
            <button
              class="btn btn-strong"
              @click="playerTurn('strong')"
              :disabled="isBusy || playerStore.stats.energy < 5"
            >
              💥 ĐÁNH MẠNH (5⚡)
            </button>
            <button
              class="btn btn-auto"
              @click="startAutoBattle"
              :disabled="isBusy"
            >
              🤖 AUTO
            </button>
          </template>
        </div>
        <div v-else class="end-actions">
          <button class="btn btn-back" @click="$router.push('/adventure')">
            Quay lại Rừng
          </button>
          <button class="btn btn-village" @click="$router.push('/village')">
            Về Làng
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive } from "vue";
import { usePlayerStore } from "@/stores/player";
import { useRouter } from "vue-router";

const playerStore = usePlayerStore();
const router = useRouter();

// --- ASSETS & STATE QUÁI ---
const enemyAssets = reactive({
  skeleton: {
    name: "Bộ Xương",
    idle: new URL("@/assets/enemy/idle_skeleton.png", import.meta.url).href,
    attack: new URL("@/assets/enemy/atk_skeleton.png", import.meta.url).href,
  },
  goblin: {
    name: "Yêu Tinh",
    idle: new URL("@/assets/enemy/idle_goblin.png", import.meta.url).href,
    attack: new URL("@/assets/enemy/atk_goblin.png", import.meta.url).href,
  },
  mushroom: {
    name: "Nấm Độc",
    idle: new URL("@/assets/enemy/idle_mushroom.png", import.meta.url).href,
    attack: new URL("@/assets/enemy/atk_mushroom.png", import.meta.url).href,
  },
});

const currentEnemyType = ref("skeleton");
const enemyState = ref("idle");

const currentEnemyImage = computed(() => {
  const data = enemyAssets[currentEnemyType.value];
  return !data ? "" : enemyState.value === "attack" ? data.attack : data.idle;
});
const currentEnemyData = computed(
  () => enemyAssets[currentEnemyType.value] || { name: "Quái Vật" },
);

// --- GAME STATE ---
const combatLog = ref("Sẵn sàng chiến đấu!");
const isAutoBattling = ref(false);
const isGameOver = ref(false);
const isVictory = ref(false);
const isBusy = ref(false);
const showLevelUp = ref(false); // State Level Up

const playerAttacking = ref(false);
const enemyAttacking = ref(false);
const playerHit = ref(false);
const enemyHit = ref(false);
const visualEnemyHp = ref(100);

const showParryBtn = ref(false);
let parrySuccess = false;
let autoBattleTimer = null;

const playerHpPercent = computed(() => {
  if (playerStore.stats.maxHp === 0) return 0;
  return (playerStore.stats.hp / playerStore.stats.maxHp) * 100;
});

const delay = (ms) => new Promise((r) => setTimeout(r, ms));

// --- PLAYER ACTION ---
const playerTurn = async (attackType = "normal") => {
  if (isBusy.value) return;
  isBusy.value = true;

  playerAttacking.value = true;
  await delay(300);
  playerAttacking.value = false;

  combatLog.value = attackType === "strong" ? "Tung đòn sấm sét!" : "Tấn công!";

  const result = await playerStore.attackEnemy(false, attackType);

  if (result) {
    if (result.error) {
      combatLog.value = result.message;
      isBusy.value = false;
      return;
    }

    // Xử lý Level Up
    if (result.levelUp) {
      showLevelUp.value = true;
      setTimeout(() => (showLevelUp.value = false), 3000);
    }

    enemyHit.value = true;
    const visualDmg = attackType === "strong" ? 35 : 20;
    visualEnemyHp.value = Math.max(0, visualEnemyHp.value - visualDmg);

    await delay(200);
    enemyHit.value = false;
    combatLog.value = result.message;

    if (result.status === "DIED") {
      isGameOver.value = true;
      combatLog.value = "💀 Bạn đã thất bại...";
    } else if (visualEnemyHp.value <= 0) {
      isVictory.value = true;
      combatLog.value = "🎉 Chiến thắng!";
    } else {
      await enemyTurnAnimation();
    }
  }
  isBusy.value = false;
};

// --- ENEMY ANIMATION ---
const enemyTurnAnimation = async () => {
  await delay(500);
  enemyState.value = "attack";
  enemyAttacking.value = true;
  await delay(300);
  enemyState.value = "idle";
  enemyAttacking.value = false;

  playerHit.value = true;
  await delay(300);
  playerHit.value = false;
};

// --- AUTO BATTLE ---
const startAutoBattle = () => {
  if (isAutoBattling.value) return;
  isAutoBattling.value = true;
  combatLog.value = "🤖 Auto Battle kích hoạt...";
  runAutoTurn();
};

const runAutoTurn = async () => {
  if (isGameOver.value || isVictory.value || !isAutoBattling.value) return;

  const chance = Math.random() * 100;
  parrySuccess = false;
  if (chance < 20) await triggerParryEvent();

  const result = await playerStore.attackEnemy(parrySuccess, "normal");

  if (result) {
    if (result.levelUp) {
      showLevelUp.value = true;
      setTimeout(() => (showLevelUp.value = false), 3000);
    }

    enemyHit.value = true;
    visualEnemyHp.value = Math.max(0, visualEnemyHp.value - 20);
    await delay(200);
    enemyHit.value = false;

    combatLog.value = parrySuccess ? "🛡️ PARRY THÀNH CÔNG!" : result.message;

    if (result.status === "DIED") {
      isGameOver.value = true;
      isAutoBattling.value = false;
    } else if (visualEnemyHp.value <= 0) {
      isVictory.value = true;
      isAutoBattling.value = false;
    } else {
      await enemyTurnAnimation();
      autoBattleTimer = setTimeout(runAutoTurn, 1500);
    }
  }
};

const triggerParryEvent = () => {
  return new Promise((resolve) => {
    showParryBtn.value = true;
    combatLog.value = "⚠️ ĐỠ ĐÒN!";
    setTimeout(() => {
      showParryBtn.value = false;
      resolve();
    }, 730);
  });
};

const handleParry = () => {
  parrySuccess = true;
  showParryBtn.value = false;
};

onMounted(() => {
  visualEnemyHp.value = 100;
  enemyState.value = "idle";
  const types = ["skeleton", "goblin", "mushroom"];
  currentEnemyType.value = types[Math.floor(Math.random() * types.length)];
});

onUnmounted(() => {
  clearTimeout(autoBattleTimer);
});
</script>

<style scoped>
.combat-page {
  width: 100%;
  color: white;
  text-align: center;
  padding: 20px;
  position: relative;
}

.combat-container {
  background: #222;
  padding: 20px;
  border-radius: 12px;
  border: 2px solid #444;
  max-width: 600px;
  margin: 0 auto;
  position: relative;
}

.combat-title {
  color: #ffd166;
  min-height: 30px;
  margin-bottom: 20px;
  font-size: 1.2rem;
}

.parry-overlay {
  position: absolute;
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 100;
}

.btn-parry {
  background: #ffd700;
  color: #000;
  font-size: 24px;
  font-weight: 900;
  padding: 15px 40px;
  border: 4px solid #fff;
  border-radius: 50px;
  cursor: pointer;
  box-shadow: 0 0 20px #ffd700;
  animation: popIn 0.1s;
}

.parry-timer-bar {
  width: 100%;
  height: 5px;
  background: red;
  margin-top: 5px;
  animation: shrink 0.73s linear forwards;
}

@keyframes popIn {
  from {
    transform: scale(0);
  }

  to {
    transform: scale(1);
  }
}

@keyframes shrink {
  from {
    width: 100%;
  }

  to {
    width: 0%;
  }
}

.battlefield {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 0 10px;
}

.fighter {
  width: 40%;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.2s;
}

.avatar-box {
  width: 120px;
  height: 120px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fighter-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 5px 5px rgba(0, 0, 0, 0.5));
}

.hp-bar-container {
  width: 100%;
  height: 8px;
  background: #444;
  border-radius: 5px;
  margin-top: 5px;
  overflow: hidden;
}

.hp-bar {
  height: 100%;
  background: #ef476f;
  transition: width 0.3s ease;
}

.player-bar {
  background: #06d6a0;
}

.vs-text {
  font-size: 24px;
  font-weight: 900;
  color: #555;
  font-style: italic;
}

.controls button {
  padding: 10px 15px;
  margin: 5px;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-attack {
  background: #ef476f;
  color: white;
}

.btn-strong {
  background: #9d4edd;
  color: white;
  box-shadow: 0 0 10px #9d4edd;
}

.btn-auto {
  background: #118ab2;
  color: white;
}

.btn-disabled {
  background: #555;
  color: #aaa;
  cursor: not-allowed;
}

.btn-back {
  background: #555;
  color: white;
}

.btn-village {
  background: #118ab2;
  color: white;
}

.attack-anim {
  transform: scale(1.2) translateY(-20px);
}

.hit-anim {
  filter: brightness(0) sepia(1) hue-rotate(-50deg) saturate(5);
  animation: shake 0.3s;
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }

  25% {
    transform: translateX(-5px);
  }

  75% {
    transform: translateX(5px);
  }
}

/* CSS MỚI CHO EXP & LEVEL UP */
.exp-bar-container {
  width: 100%;
  height: 4px;
  background: #333;
  margin-top: 5px;
  border-radius: 2px;
  overflow: hidden;
  position: relative;
}

.exp-bar {
  background: #3b82f6;
  height: 100%;
  transition: width 0.5s ease-out;
}

.exp-text {
  font-size: 0.7rem;
  color: #aaa;
  margin-top: 2px;
}

.badge-lv {
  background: #ffd700;
  color: black;
  padding: 1px 4px;
  border-radius: 4px;
  font-size: 0.8em;
  margin-left: 5px;
}

.levelup-overlay {
  position: absolute;
  top: 15%;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(45deg, #ffd700, #ff8c00);
  color: #fff;
  text-shadow: 0 2px 0 #000;
  padding: 15px 30px;
  border-radius: 12px;
  border: 2px solid #fff;
  z-index: 999;
  text-align: center;
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.5);
}

.levelup-overlay h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 900;
}

.levelup-overlay p {
  margin: 5px 0 0;
  font-size: 0.9rem;
}

.bounce-enter-active {
  animation: bounce-in 0.5s;
}

.bounce-leave-active {
  animation: bounce-in 0.5s reverse;
}

@keyframes bounce-in {
  0% {
    transform: translateX(-50%) scale(0);
  }

  50% {
    transform: translateX(-50%) scale(1.2);
  }

  100% {
    transform: translateX(-50%) scale(1);
  }
}
</style> -->

<!-- <template>
  <div class="combat-container page-container">
    <div class="battle-arena">
      <template v-if="battleStore.isReady && battleStore.enemy">
        <div class="enemy-section">
          <div class="enemy-info">
            <h3>{{ battleStore.enemy.name }}</h3>
            <div class="health-bar-container">
              <div
                class="health-bar enemy-hp"
                :style="{
                  width:
                    calculatePercent(
                      battleStore.enemyHp,
                      battleStore.enemyMaxHp,
                    ) + '%',
                }"
              ></div>
              <span class="hp-text"
                >{{ battleStore.enemyHp }} / {{ battleStore.enemyMaxHp }}</span
              >
            </div>
          </div>
          <div class="enemy-sprite" :class="{ 'shake-anim': isTakingDamage }">
            <img
              :src="getEnemyAsset(battleStore.enemy.name)"
              alt="Enemy"
              class="pixel-art"
            />
          </div>
        </div>

        <div class="center-stage">
          <div v-if="showQTE" class="qte-overlay">
            <button class="qte-button pixel-btn danger" @click="handleBlock">
              🛡️ ĐỠ ĐÒN! ({{ qteTimer.toFixed(1) }}s)
            </button>
          </div>

          <div v-if="battleStore.status === 'VICTORY'" class="victory-modal">
            <h2>🏆 CHIẾN THẮNG!</h2>
            <p>
              Nhận: {{ battleStore.expEarned }} EXP |
              {{ battleStore.goldEarned }} Vàng
            </p>

            <div v-if="battleStore.droppedItem" class="loot-box">
              <div
                class="item-card"
                :class="'rarity-' + battleStore.droppedItem.rarity"
              >
                <span>🎁 {{ battleStore.droppedItem.name }}</span>
              </div>
            </div>
            <button @click="goHome" class="pixel-btn primary">Về Làng</button>
          </div>

          <div v-if="battleStore.status === 'DEFEAT'" class="defeat-modal">
            <h2>💀 THẤT BẠI...</h2>
            <button @click="goHome" class="pixel-btn secondary">
              Về dưỡng thương
            </button>
          </div>
        </div>

        <div class="player-section">
          <div class="player-sprite">
            <img src="@/assets/char/idle_demon1.png" class="pixel-art" />
          </div>
          <div class="player-info">
            <h3>Đại Hiệp</h3>
            <div class="health-bar-container">
              <div
                class="health-bar player-hp"
                :style="{
                  width:
                    calculatePercent(
                      battleStore.playerHp,
                      battleStore.playerMaxHp,
                    ) + '%',
                }"
              ></div>
              <span class="hp-text"
                >{{ battleStore.playerHp }} /
                {{ battleStore.playerMaxHp }}</span
              >
            </div>
          </div>
        </div>
      </template>

      <div v-else class="loading-screen">
        <h2 v-if="battleStore.status === 'ERROR'" class="text-red">
          Lỗi kết nối máy chủ!
        </h2>
        <h2 v-else>⚔️ Đang tìm đối thủ...</h2>
      </div>
    </div>

    <div class="combat-log" ref="logContainer">
      <p
        v-for="(log, index) in battleStore.combatLogs"
        :key="index"
        class="log-entry"
      >
        {{ log }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from "vue";
import { useBattleStore } from "@/stores/battleStore";
import { useRouter } from "vue-router";

const battleStore = useBattleStore();
const router = useRouter();
const logContainer = ref(null);

const isTakingDamage = ref(false);
const showQTE = ref(false);
const qteTimer = ref(0);
let qteInterval = null;
let autoInterval = null;

// --- ASSET HELPER ---
const getEnemyAsset = (name) => {
  if (!name)
    return new URL("@/assets/enemy/idle_goblin.png", import.meta.url).href;
  const n = name.toLowerCase();
  if (n.includes("nấm"))
    return new URL("@/assets/enemy/idle_mushroom.png", import.meta.url).href;
  if (n.includes("xương"))
    return new URL("@/assets/enemy/idle_skeleton.png", import.meta.url).href;
  return new URL("@/assets/enemy/idle_goblin.png", import.meta.url).href;
};

const calculatePercent = (cur, max) => (max > 0 ? (cur / max) * 100 : 0);
const goHome = () => router.push("/village");

// --- GAME LOGIC ---
const startBattle = async () => {
  await battleStore.startBattle();
  if (battleStore.isReady) {
    startAutoLoop();
  }
};

const startAutoLoop = () => {
  if (autoInterval) clearInterval(autoInterval);
  autoInterval = setInterval(async () => {
    // Chỉ đánh khi đang ONGOING và không có QTE
    if (battleStore.status === "ONGOING" && !showQTE.value) {
      const res = await battleStore.autoTurn(false);

      // Nếu server báo có QTE -> Dừng đánh, hiện nút
      if (res && (res.status === "QTE_ACTION" || res.qteTriggered)) {
        triggerQTE();
      }
    }
  }, 1500);
};

const triggerQTE = () => {
  clearInterval(autoInterval); // Dừng auto
  showQTE.value = true;
  qteTimer.value = 0.75; // 0.75s

  qteInterval = setInterval(() => {
    qteTimer.value -= 0.05;
    if (qteTimer.value <= 0) {
      failQTE();
    }
  }, 50);
};

const handleBlock = async () => {
  clearInterval(qteInterval);
  showQTE.value = false;
  await battleStore.sendAction("BLOCK");
  startAutoLoop(); // Tiếp tục đánh
};

const failQTE = async () => {
  clearInterval(qteInterval);
  showQTE.value = false;
  isTakingDamage.value = true; // Rung màn hình
  setTimeout(() => (isTakingDamage.value = false), 500);

  await battleStore.sendAction("IGNORE_QTE");
  startAutoLoop();
};

// --- WATCHERS ---
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

// Check kết thúc trận để dừng Auto
watch(
  () => battleStore.status,
  (newStatus) => {
    if (newStatus === "VICTORY" || newStatus === "DEFEAT") {
      clearInterval(autoInterval);
      if (qteInterval) clearInterval(qteInterval);
    }
  },
);

onMounted(() => {
  startBattle();
});

onUnmounted(() => {
  clearInterval(autoInterval);
  if (qteInterval) clearInterval(qteInterval);
  battleStore.resetBattle();
});
</script>

<style scoped>
/* Copy CSS cũ vào đây, bổ sung thêm .text-red */
.text-red {
  color: #ff5252;
  text-align: center;
  margin-top: 20px;
}

.loading-screen {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #aaa;
}

/* ... (Giữ nguyên các class CSS combat-container, battle-arena... từ file cũ) */
.combat-container {
  display: flex;
  flex-direction: column;
  height: 90vh;
  background-image: url("@/assets/Background/b_doanhtrai.png");
  background-size: cover;
  color: white;
  padding: 10px;
}

.battle-arena {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
}

.enemy-section,
.player-section {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 10px;
  width: fit-content;
}

.enemy-section {
  align-self: flex-end;
  flex-direction: row-reverse;
}

/* Quái bên phải */
.pixel-art {
  width: 80px;
  height: 80px;
  image-rendering: pixelated;
}

.health-bar-container {
  width: 150px;
  height: 15px;
  background: #333;
  border: 1px solid #000;
  position: relative;
}

.health-bar {
  height: 100%;
  transition: width 0.3s;
}

.enemy-hp {
  background: #e74c3c;
}

.player-hp {
  background: #2ecc71;
}

.hp-text {
  position: absolute;
  top: -2px;
  width: 100%;
  text-align: center;
  font-size: 10px;
  font-weight: bold;
}

.center-stage {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  pointer-events: none;
}

.qte-overlay,
.victory-modal,
.defeat-modal {
  pointer-events: auto;
  z-index: 100;
  text-align: center;
}

.qte-button {
  font-size: 2rem;
  padding: 15px 30px;
  background: red;
  color: white;
  border: 3px solid white;
  border-radius: 50px;
  cursor: pointer;
  animation: pulse 0.5s infinite;
}

.victory-modal {
  background: rgba(0, 0, 0, 0.9);
  padding: 20px;
  border: 2px solid gold;
  border-radius: 10px;
}

.combat-log {
  height: 120px;
  background: rgba(0, 0, 0, 0.8);
  overflow-y: auto;
  padding: 10px;
  border-top: 2px solid #555;
  font-family: monospace;
  font-size: 13px;
}

.shake-anim {
  animation: shake 0.5s;
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }

  25% {
    transform: translateX(-5px);
  }

  75% {
    transform: translateX(5px);
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

<template>
  <div class="combat-page">
    <div class="combat-container">
      <transition name="bounce">
        <div v-if="showLevelUp" class="levelup-overlay">
          <h2>🌟 LEVEL UP! {{ playerStore.stats.lv }} 🌟</h2>
          <p>Full HP/Energy - Tăng sức mạnh!</p>
        </div>
      </transition>

      <h2 class="combat-title" :class="{ 'text-red': isGameOver, 'text-green': isVictory }">
        {{ combatLog }}
      </h2>

      <div v-if="showParryBtn" class="parry-overlay">
        <button class="btn-parry" @click="handleParry">🛡️ ĐỠ NGAY!</button>
        <div class="parry-timer-bar"></div>
      </div>

      <div class="battlefield">
        <div class="fighter enemy" :class="{ 'attack-anim': enemyAttacking, 'hit-anim': enemyHit }">
          <div class="avatar-box">
            <img :src="currentEnemyImage" class="fighter-img enemy-img" alt="Enemy" />
          </div>
          <div class="name">{{ currentEnemyData.name }}</div>
          <div class="hp-bar-container">
            <div class="hp-bar" :style="{ width: visualEnemyHp + '%' }"></div>
          </div>
        </div>

        <div class="vs-text">VS</div>

        <div class="fighter player" :class="{ 'attack-anim': playerAttacking, 'hit-anim': playerHit }">
          <div class="avatar-box">
            <img src="https://cdn-icons-png.flaticon.com/512/9408/9408175.png" class="fighter-img player-img" />
          </div>
          <div class="name">
            {{ playerStore.stats.name }}
            <span class="badge-lv">Lv.{{ playerStore.stats.lv }}</span>
          </div>
          <div class="hp-bar-container">
            <div class="hp-bar player-bar" :style="{ width: playerHpPercent + '%' }"></div>
          </div>
          <div class="hp-text">{{ playerStore.stats.hp }} / {{ playerStore.stats.maxHp }}</div>
          <div class="energy-text" style="color: yellow; font-size: 0.9rem">⚡ {{ playerStore.stats.energy }}</div>
        </div>
      </div>

      <div class="controls">
        <div v-if="!isGameOver && !isVictory">
          <button v-if="isAutoBattling" class="btn btn-disabled" disabled>🤖 Đang tự động đánh...</button>
          <template v-else>
            <button class="btn btn-attack" @click="playerTurn('normal')" :disabled="isBusy">⚔️ ĐÁNH THƯỜNG</button>
            <button class="btn btn-strong" @click="playerTurn('strong')" :disabled="isBusy || playerStore.stats.energy < 5">💥 ĐÁNH MẠNH (5⚡)</button>
            <button class="btn btn-auto" @click="startAutoBattle" :disabled="isBusy">🤖 AUTO</button>
          </template>
        </div>
        <div v-else class="end-actions">
          <button class="btn btn-back" @click="$router.push('/explore')">Quay lại</button>
          <button class="btn btn-village" @click="$router.push('/village')">Về Làng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from "vue";
import { usePlayerStore } from "@/stores/player";
import { useRouter } from "vue-router";
import { getEnemyImage } from "@/utils/assetHelper"; // [FIX]

const playerStore = usePlayerStore();
const router = useRouter();

// Dùng Helper lấy ảnh quái
const enemyAssets = reactive({
  skeleton: { name: "Bộ Xương", idle: getEnemyImage("skeleton", "idle"), attack: getEnemyImage("skeleton", "attack") },
  goblin: { name: "Yêu Tinh", idle: getEnemyImage("goblin", "idle"), attack: getEnemyImage("goblin", "attack") },
  mushroom: { name: "Nấm Độc", idle: getEnemyImage("mushroom", "idle"), attack: getEnemyImage("mushroom", "attack") },
});

const currentEnemyType = ref("skeleton");
const enemyState = ref("idle");

const currentEnemyImage = computed(() => {
  const data = enemyAssets[currentEnemyType.value];
  return !data ? "" : enemyState.value === "attack" ? data.attack : data.idle;
});
const currentEnemyData = computed(() => enemyAssets[currentEnemyType.value] || { name: "Quái Vật" });

const combatLog = ref("Sẵn sàng chiến đấu!");
const isAutoBattling = ref(false);
const isGameOver = ref(false);
const isVictory = ref(false);
const isBusy = ref(false);
const showLevelUp = ref(false);
const playerAttacking = ref(false);
const enemyAttacking = ref(false);
const playerHit = ref(false);
const enemyHit = ref(false);
const visualEnemyHp = ref(100);
const showParryBtn = ref(false);
let parrySuccess = false;
let autoBattleTimer = null;

const playerHpPercent = computed(() => {
  if (playerStore.stats.maxHp === 0) return 0;
  return (playerStore.stats.hp / playerStore.stats.maxHp) * 100;
});

const delay = (ms) => new Promise((r) => setTimeout(r, ms));

const playerTurn = async (attackType = "normal") => {
  if (isBusy.value) return;
  isBusy.value = true;
  playerAttacking.value = true;
  await delay(300);
  playerAttacking.value = false;
  combatLog.value = attackType === "strong" ? "Tung đòn sấm sét!" : "Tấn công!";
  
  const result = await playerStore.attackEnemy(false, attackType);
  
  if (result) {
    if (result.error) {
      combatLog.value = result.message;
      isBusy.value = false;
      return;
    }
    if (result.levelUp) {
      showLevelUp.value = true;
      setTimeout(() => (showLevelUp.value = false), 3000);
    }
    enemyHit.value = true;
    const visualDmg = attackType === "strong" ? 35 : 20;
    visualEnemyHp.value = Math.max(0, visualEnemyHp.value - visualDmg);
    await delay(200);
    enemyHit.value = false;
    combatLog.value = result.message;

    if (result.status === "DIED") {
      isGameOver.value = true;
      combatLog.value = "💀 Bạn đã thất bại...";
    } else if (visualEnemyHp.value <= 0) {
      isVictory.value = true;
      combatLog.value = "🎉 Chiến thắng!";
    } else {
      await enemyTurnAnimation();
    }
  }
  isBusy.value = false;
};

const enemyTurnAnimation = async () => {
  await delay(500);
  enemyState.value = "attack";
  enemyAttacking.value = true;
  await delay(300);
  enemyState.value = "idle";
  enemyAttacking.value = false;
  playerHit.value = true;
  await delay(300);
  playerHit.value = false;
};

const startAutoBattle = () => {
  if (isAutoBattling.value) return;
  isAutoBattling.value = true;
  combatLog.value = "🤖 Auto Battle kích hoạt...";
  runAutoTurn();
};

const runAutoTurn = async () => {
  if (isGameOver.value || isVictory.value || !isAutoBattling.value) return;
  const chance = Math.random() * 100;
  parrySuccess = false;
  if (chance < 20) await triggerParryEvent();
  const result = await playerStore.attackEnemy(parrySuccess, "normal");
  if (result) {
    if (result.levelUp) {
      showLevelUp.value = true;
      setTimeout(() => (showLevelUp.value = false), 3000);
    }
    enemyHit.value = true;
    visualEnemyHp.value = Math.max(0, visualEnemyHp.value - 20);
    await delay(200);
    enemyHit.value = false;
    combatLog.value = parrySuccess ? "🛡️ PARRY THÀNH CÔNG!" : result.message;
    if (result.status === "DIED") {
      isGameOver.value = true;
      isAutoBattling.value = false;
    } else if (visualEnemyHp.value <= 0) {
      isVictory.value = true;
      isAutoBattling.value = false;
    } else {
      await enemyTurnAnimation();
      autoBattleTimer = setTimeout(runAutoTurn, 1500);
    }
  }
};

const triggerParryEvent = () => {
  return new Promise((resolve) => {
    showParryBtn.value = true;
    combatLog.value = "⚠️ ĐỠ ĐÒN!";
    setTimeout(() => { showParryBtn.value = false; resolve(); }, 730);
  });
};

const handleParry = () => {
  parrySuccess = true;
  showParryBtn.value = false;
};

onMounted(() => {
  visualEnemyHp.value = 100;
  if (!playerStore.stats.hp) playerStore.initStats();
  const types = ["skeleton", "goblin", "mushroom"];
  currentEnemyType.value = types[Math.floor(Math.random() * types.length)];
});
</script>

<style scoped>
.combat-page { height: 100vh; display: flex; align-items: center; justify-content: center; background: #261815; color: white; font-family: "Noto Serif TC", serif; }
.combat-container { width: 100%; max-width: 500px; padding: 20px; background: rgba(0,0,0,0.5); border-radius: 10px; border: 2px solid #5d4037; position: relative; }
.combat-title { text-align: center; color: #fbc02d; text-shadow: 0 2px 2px #000; margin-bottom: 20px; font-size: 1.5rem; min-height: 40px; }
.text-red { color: #ef5350 !important; }
.text-green { color: #66bb6a !important; }
.battlefield { display: flex; align-items: center; justify-content: space-between; margin-bottom: 30px; position: relative; }
.fighter { display: flex; flex-direction: column; align-items: center; width: 120px; transition: transform 0.2s; }
.avatar-box { width: 100px; height: 100px; border: 3px solid #8d6e63; border-radius: 50%; overflow: hidden; background: #3e2723; display: flex; justify-content: center; align-items: center; }
.fighter-img { width: 100%; height: 100%; object-fit: contain; }
.name { margin-top: 10px; font-weight: bold; text-align: center; }
.hp-bar-container { width: 100%; height: 8px; background: #000; border-radius: 4px; margin-top: 5px; overflow: hidden; border: 1px solid #555; }
.hp-bar { height: 100%; background: #ef5350; transition: width 0.3s; }
.player-bar { background: #26a69a; }
.vs-text { font-size: 2rem; font-weight: 900; color: #5d4037; font-style: italic; }
.controls { display: flex; flex-direction: column; gap: 10px; align-items: center; }
.btn { padding: 12px 20px; border: none; border-radius: 6px; font-weight: bold; font-size: 1rem; cursor: pointer; transition: 0.2s; width: 100%; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-attack { background: #b71c1c; color: white; }
.btn-strong { background: #fbc02d; color: #000; }
.btn-auto { background: #1565c0; color: white; }
.end-actions { display: flex; gap: 10px; width: 100%; }
.btn-back { background: #5d4037; color: white; }
.btn-village { background: #2e7d32; color: white; }
.attack-anim { animation: lunge 0.3s forwards; }
.hit-anim { animation: shake 0.3s; filter: brightness(2) sepia(1) hue-rotate(-50deg); }
.levelup-overlay { position: absolute; top: 20%; left: 50%; transform: translate(-50%, -50%); background: rgba(255, 215, 0, 0.9); color: #000; padding: 20px; border-radius: 10px; text-align: center; border: 3px solid #fff; z-index: 100; animation: popIn 0.5s; }
.parry-overlay { position: absolute; top: 40%; left: 50%; transform: translate(-50%, -50%); z-index: 50; }
.btn-parry { background: #2979ff; color: white; border: 2px solid white; padding: 15px 30px; font-size: 1.2rem; font-weight: 900; border-radius: 50px; cursor: pointer; box-shadow: 0 0 20px #2979ff; animation: pulse 0.5s infinite; }
@keyframes lunge { 50% { transform: scale(1.2) translateY(-20px); } }
@keyframes shake { 0%, 100% { transform: translateX(0); } 25% { transform: translateX(-5px); } 75% { transform: translateX(5px); } }
@keyframes popIn { 0% { transform: translate(-50%, -50%) scale(0); } 100% { transform: translate(-50%, -50%) scale(1); } }
@keyframes pulse { 0% { transform: scale(1); } 50% { transform: scale(1.1); } 100% { transform: scale(1); } }
</style>