<template>
  <div class="adventure-page">
    <div class="status-bar">
      <div class="stat-item">
        ❤️ {{ characterStore.character?.currentHp || 0 }}/{{ characterStore.character?.maxHp || 100 }}
      </div>
      <div class="stat-item">
        ⚡ {{ characterStore.character?.currentEnergy || 0 }}/{{ characterStore.character?.maxEnergy || 100 }}
      </div>
      <div class="stat-item">💰 {{ characterStore.gold }}</div>
    </div>

    <div class="explore-wrapper">
      <div class="location-header">
        <h2>🌲 {{ characterStore.character?.currentLocation || 'Rừng Chạng Vạng' }}</h2>
        <p>Tự do khám phá. Cẩn thận quái vật!</p>
      </div>

      <div class="event-log" ref="logContainer">
        <div v-for="(log, index) in characterStore.logs" :key="index" class="log-item">
          <span class="content" :class="log.type" v-html="formatLog(log)"></span>
        </div>
      </div>

      <div class="actions">
        <button class="btn-step" @click="handleExplore" :disabled="characterStore.isLoading">
          {{ characterStore.isLoading ? '...' : '👣 Bước tiếp' }}
        </button>

        <button class="btn-village" @click="$router.push('/village')">
          🏘️ Về làng
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, nextTick } from "vue";
// [FIX] Xóa playerStore, nhập characterStore
import { useCharacterStore } from "@/stores/characterStore";
import { useRouter } from "vue-router";

const characterStore = useCharacterStore();
const router = useRouter();
const logContainer = ref(null);

const formatLog = (log) => {
  const time = new Date(log.id).toLocaleTimeString();
  return `<span class="time">[${time}]</span> ${log.message}`;
};

const handleExplore = async () => {
  try {
    // Gọi API explore từ store
    const result = await characterStore.explore({ mapId: "MAP_01" });

    // Nếu gặp quái -> Chuyển sang màn hình Battle
    if (result.type === "ENEMY") {
      router.push("/battle");
    }
  } catch (e) {
    // Lỗi đã được store xử lý và push vào logs
  }
};

// Tự động cuộn log
watch(
  () => characterStore.logs.length,
  async () => {
    await nextTick();
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight;
    }
  }
);

onMounted(() => {
  characterStore.fetchCharacter();
});
</script>

<style scoped>
.adventure-page {
  padding: 20px;
  color: #eee;
  max-width: 600px;
  margin: 0 auto;
  font-family: "Noto Serif TC", serif;
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
  padding: 5px 0;
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

.btn-step {
  padding: 20px;
  background: #06d6a0;
  color: #000;
  border: none;
  font-weight: 900;
  text-transform: uppercase;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.1s;
  box-shadow: 0 4px 0 #04c68e;
}

.btn-step:active {
  transform: translateY(4px);
  box-shadow: none;
}

.btn-step:disabled {
  background: #555;
  box-shadow: none;
  cursor: not-allowed;
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

/* Màu sắc log */
.INFO {
  color: #ccc;
}

.ENEMY {
  color: #ff5252;
  font-weight: bold;
}

.LEVEL_UP {
  color: #ffd700;
  font-weight: bold;
  text-shadow: 0 0 5px orange;
}

.ERROR {
  color: #f44336;
}
</style>