<template>
  <div class="combat-page">
    <div class="combat-container">
      <h2
        class="combat-title"
        :class="{
          'text-red': battleStore.status === 'DEFEAT',
          'text-green': battleStore.status === 'VICTORY',
        }"
      >
        {{ lastLog }}
      </h2>

      <div class="battlefield">
        <div class="fighter enemy" :class="{ 'hit-anim': enemyHit }">
          <div class="avatar-box">
            <img
              :src="getEnemyImage(battleStore.enemy?.name)"
              class="fighter-img"
            />
          </div>
          <div class="name">{{ battleStore.enemy?.name || "Kẻ địch" }}</div>
          <div class="hp-bar-container">
            <div class="hp-bar" :style="{ width: enemyHpPercent + '%' }"></div>
          </div>
          <div class="hp-text">
            {{ battleStore.enemyHp }}/{{ battleStore.enemyMaxHp }}
          </div>
        </div>

        <div class="vs-text">VS</div>

        <div class="fighter player" :class="{ 'hit-anim': playerHit }">
          <div class="avatar-box">
            <img
              :src="
                getCurrentSkin(characterStore.character?.avatarUrl).sprites.idle
              "
              class="fighter-img"
            />
          </div>
          <div class="name">{{ characterStore.character?.name }}</div>
          <div class="hp-bar-container">
            <div
              class="hp-bar player-bar"
              :style="{ width: playerHpPercent + '%' }"
            ></div>
          </div>
          <div class="hp-text">
            {{ battleStore.playerHp }}/{{ battleStore.playerMaxHp }}
          </div>
        </div>
      </div>

      <div class="controls">
        <template v-if="battleStore.status === 'ONGOING'">
          <button
            class="btn btn-attack"
            @click="handleAction('ATTACK')"
            :disabled="battleStore.isLoading"
          >
            ⚔️ Tấn Công
          </button>
          <button
            class="btn btn-strong"
            @click="handleAction('SKILL')"
            :disabled="battleStore.isLoading"
          >
            💥 Kỹ Năng
          </button>
          <button
            class="btn btn-defend"
            @click="handleAction('DEFEND')"
            :disabled="battleStore.isLoading"
          >
            🛡️ Phòng Thủ
          </button>
        </template>

        <template v-else>
          <button class="btn btn-village" @click="$router.push('/village')">
            {{
              battleStore.status === "VICTORY"
                ? "🎉 Về làng nhận thưởng"
                : "💀 Về làng dưỡng thương"
            }}
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
// [FIX] Import đúng store
import { useBattleStore } from "@/stores/battleStore";
import { useCharacterStore } from "@/stores/characterStore";
import { getEnemyImage, getCurrentSkin } from "@/utils/assetHelper";
import { useRouter } from "vue-router";

const battleStore = useBattleStore();
const characterStore = useCharacterStore();
const router = useRouter();

const enemyHit = ref(false);
const playerHit = ref(false);

const lastLog = computed(() => {
  const logs = battleStore.combatLogs;
  return logs.length > 0 ? logs[logs.length - 1] : "Sẵn sàng chiến đấu!";
});

// Tính % HP
const enemyHpPercent = computed(() =>
  battleStore.enemyMaxHp > 0
    ? (battleStore.enemyHp / battleStore.enemyMaxHp) * 100
    : 0,
);
const playerHpPercent = computed(() =>
  battleStore.playerMaxHp > 0
    ? (battleStore.playerHp / battleStore.playerMaxHp) * 100
    : 0,
);

// Xử lý hành động
const handleAction = async (action) => {
  await battleStore.sendAction(action);
  // Hiệu ứng chớp nháy kẻ địch khi bị đánh
  enemyHit.value = true;
  setTimeout(() => (enemyHit.value = false), 300);
};

// Hiệu ứng khi người chơi bị mất máu
watch(
  () => battleStore.playerHp,
  (newVal, oldVal) => {
    if (newVal < oldVal) {
      playerHit.value = true;
      setTimeout(() => (playerHit.value = false), 300);
    }
  },
);

onMounted(async () => {
  // Đảm bảo có thông tin nhân vật để hiển thị tên/ảnh
  if (!characterStore.character) await characterStore.fetchCharacter();

  // Kết nối lại trận đấu (Nếu reload trang)
  await battleStore.reconnectBattle();
});
</script>

<style scoped>
.combat-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1a120b;
  color: white;
}

.combat-container {
  width: 100%;
  max-width: 600px;
  padding: 20px;
  background: rgba(0, 0, 0, 0.8);
  border: 2px solid #8d6e63;
  border-radius: 12px;
}

.combat-title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 1.2rem;
  min-height: 30px;
}

.battlefield {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.fighter {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40%;
}

.avatar-box {
  width: 100px;
  height: 100px;
  border: 2px solid #aaa;
  border-radius: 50%;
  overflow: hidden;
  background: #333;
  margin-bottom: 5px;
}

.fighter-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.hp-bar-container {
  width: 100%;
  height: 10px;
  background: #444;
  border-radius: 5px;
  overflow: hidden;
  margin-top: 5px;
}

.hp-bar {
  height: 100%;
  background: #e53935;
  transition: width 0.3s;
}

.player-bar {
  background: #43a047;
}

.hp-text {
  font-size: 0.8rem;
  margin-top: 2px;
  color: #ccc;
}

.vs-text {
  font-size: 2rem;
  font-weight: bold;
  color: #8d6e63;
  font-style: italic;
}

.controls {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 12px 20px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  color: white;
  min-width: 100px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-attack {
  background: #e53935;
}

.btn-strong {
  background: #fb8c00;
}

.btn-defend {
  background: #1e88e5;
}

.btn-village {
  background: #43a047;
  width: 100%;
  margin-top: 10px;
}

.hit-anim {
  animation: shake 0.3s;
  filter: brightness(2) sepia(1) hue-rotate(-50deg);
}

.text-red {
  color: #ff5252;
}

.text-green {
  color: #69f0ae;
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
</style>
