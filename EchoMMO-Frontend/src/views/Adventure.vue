<template>
  <div class="adventure-page">
    <div class="status-bar">
      <div class="stat-item">
        ❤️ {{ playerStore.stats.hp }}/{{ playerStore.stats.maxHp }}
      </div>
      <div class="stat-item">
        ⚡ {{ playerStore.stats.energy }}/{{ playerStore.stats.maxEnergy }}
      </div>
      <div class="stat-item">💰 {{ playerStore.stats.gold }}</div>
    </div>

    <div class="explore-wrapper">
      <div class="location-header">
        <h2>🌲 Rừng Chạng Vạng</h2>
        <p>Tự do khám phá. Không giới hạn thể lực!</p>
      </div>

      <div class="event-log" ref="logContainer">
        <div v-for="(log, index) in logs" :key="index" class="log-item">
          <span class="time">[{{ log.time }}]</span>
          <span class="content" v-html="log.msg"></span>
        </div>
      </div>

      <div class="actions">
        <button class="btn-step" @click="takeStep">👣 Bước tiếp</button>

        <button class="btn-village" @click="$router.push('/village')">
          🏘️ Về làng
        </button>
      </div>
    </div>

    <div v-if="isFighting" class="combat-overlay">
      <div class="combat-modal">
        <CombatView @combat-end="finishFight" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from "vue";
import { usePlayerStore } from "@/stores/player";
import CombatView from "./Combat.vue";

const playerStore = usePlayerStore();
const isFighting = ref(false);
const logContainer = ref(null);
const logs = ref([{ time: getCurrentTime(), msg: "Bắt đầu hành trình..." }]);

function getCurrentTime() {
  const now = new Date();
  return `${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`;
}

const addLog = async (msg) => {
  logs.value.push({ time: getCurrentTime(), msg });
  if (logs.value.length > 50) logs.value.shift();
  await nextTick();
  if (logContainer.value)
    logContainer.value.scrollTop = logContainer.value.scrollHeight;
};

// --- LOGIC MỚI (KHÔNG CÒN TRỪ ENERGY) ---
const takeStep = () => {
  // 1. KHÔNG CHECK NĂNG LƯỢNG NỮA

  const roll = Math.random() * 100;

  if (roll < 60) {
    // 60%: Đi dạo (Flavor Text)
    const texts = [
      "Bạn nghe thấy tiếng gió thổi vi vu...",
      "Một con sóc chạy qua chân bạn.",
      "Không khí ở đây thật trong lành.",
      "Bạn phát hiện một dấu chân lạ.",
      "Khu rừng thật yên tĩnh.",
    ];
    addLog(texts[Math.floor(Math.random() * texts.length)]);
  } else if (roll < 80) {
    // 20%: Nhặt được Vàng
    const gold = Math.floor(Math.random() * 10) + 5;
    playerStore.stats.gold += gold;
    addLog(
      `<span style="color:#ffd166">✨ May mắn! Bạn nhặt được <b>${gold} Gold</b>!</span>`,
    );
  } else {
    // 20%: GẶP QUÁI (Vào đánh luôn, không tốn gì cả)
    addLog(
      '<span style="color:#ef476f; font-weight:bold;">⚔️ QUÁI VẬT XUẤT HIỆN! Chiến thôi!</span>',
    );
    setTimeout(() => {
      isFighting.value = true;
    }, 500);
  }
};

const finishFight = () => {
  isFighting.value = false;
  addLog(
    '<span style="color:#06d6a0">Đã xử lý xong quái vật. Tiếp tục nào!</span>',
  );
};
</script>

<style scoped>
.adventure-page {
  padding: 20px;
  color: #eee;
  max-width: 600px;
  margin: 0 auto;
}
.status-bar {
  display: flex;
  gap: 20px;
  background: #222;
  padding: 10px;
  border: 1px solid #444;
  justify-content: center;
  border-radius: 8px;
  margin-bottom: 20px;
}

.event-log {
  background: #111;
  height: 300px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #444;
  margin-bottom: 15px;
  font-family: monospace;
}
.log-item {
  border-bottom: 1px solid #222;
  margin-bottom: 5px;
}
.time {
  color: gray;
  font-size: 0.8em;
  margin-right: 5px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* Nút bước đi to và nổi bật hơn để dễ bấm liên tục */
.btn-step {
  padding: 20px;
  background: #06d6a0;
  color: #000;
  border: none;
  font-weight: 900; /* Chữ đậm hơn */
  text-transform: uppercase;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.1s;
  box-shadow: 0 4px 0 #04c68e; /* Tạo hiệu ứng nút nổi 3D */
}
.btn-step:active {
  transform: translateY(4px); /* Hiệu ứng lún xuống khi bấm */
  box-shadow: none;
}

.btn-village {
  padding: 12px;
  background: #118ab2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
}

.combat-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.9);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.combat-modal {
  width: 95%;
  max-width: 600px;
}
</style>
