<template>
  <div class="quest-panel-container">
    <div class="panel-header">
      <span class="decor-left">❖</span>
      <h3>CÁO THỊ</h3>
      <span class="decor-right">❖</span>
    </div>

    <div v-if="questStore.isLoading" class="state-msg">
      <i class="fas fa-circle-notch fa-spin"></i> Đang cập nhật...
    </div>

    <div v-else class="quest-list custom-scroll">
      <div v-if="questStore.quests.length === 0" class="state-msg empty">
        Chưa có nhiệm vụ mới
      </div>

      <div
        v-for="q in questStore.quests"
        :key="q.id"
        class="mission-card"
        :class="{
          ready: !q.isClaimed && q.progress >= q.target,
          done: q.isClaimed,
        }"
      >
        <div class="mission-left">
          <div class="m-desc">
            {{ q.description }}
          </div>

          <div class="m-progress-wrap">
            <div class="progress-track">
              <div
                class="progress-fill"
                :class="{ 'fill-ready': q.progress >= q.target }"
                :style="{
                  width: Math.min((q.progress / q.target) * 100, 100) + '%',
                }"
              ></div>
            </div>
            <div class="progress-text">{{ q.progress }} / {{ q.target }}</div>
          </div>
        </div>

        <div class="mission-right">
          <button
            v-if="!q.isClaimed && q.progress >= q.target"
            class="btn-claim"
            @click="questStore.claim(q.id)"
          >
            <div class="btn-inner">
              <span>NHẬN</span>
              <span class="reward-val"
                >{{ q.rewardGold }} <i class="fas fa-coins"></i
              ></span>
            </div>
          </button>

          <div v-else-if="q.isClaimed" class="stamp-done">ĐÃ NHẬN</div>

          <div v-else class="reward-preview">
            <i class="fas fa-coins"></i> {{ q.rewardGold }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { useQuestStore } from "../stores/questStore";

const questStore = useQuestStore();

onMounted(() => {
  questStore.fetchQuests();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700&display=swap");

.quest-panel-container {
  width: 100%;
  height: 100%;
  background: #1e1e1e; /* Dark bg matches log panel */
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #ccc;
  font-family: "Noto Serif TC", serif;
}

/* HEADER */
.panel-header {
  height: 36px;
  background: #3e2723;
  border-bottom: 1px solid #5d4037;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fbc02d; /* Gold text */
  font-weight: bold;
  font-size: 0.9em;
  gap: 8px;
  flex-shrink: 0;
}
.decor-left,
.decor-right {
  font-size: 0.8em;
  opacity: 0.7;
}

/* STATE MESSAGES */
.state-msg {
  text-align: center;
  padding: 20px;
  font-size: 0.85em;
  color: #757575;
  font-style: italic;
}

/* LIST */
.quest-list {
  flex: 1;
  overflow-y: auto;
  padding: 5px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

/* CARD */
.mission-card {
  background: #261815;
  border: 1px solid #3e2723;
  padding: 8px;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: 0.2s;
}

.mission-card:hover {
  background: #2d1e1b;
  border-color: #5d4037;
}

/* Ready State */
.mission-card.ready {
  border-color: #fbc02d;
  box-shadow: inset 0 0 5px rgba(251, 192, 45, 0.1);
}

/* Done State */
.mission-card.done {
  opacity: 0.5;
  filter: grayscale(0.8);
}

/* LEFT CONTENT */
.mission-left {
  flex: 1;
  margin-right: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.m-desc {
  font-size: 0.85em;
  color: #e0e0e0;
  line-height: 1.2;
}

.m-progress-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.75em;
  color: #a1887f;
}

.progress-track {
  flex: 1;
  height: 4px;
  background: #000;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #5d4037;
  transition: width 0.3s;
}
.progress-fill.fill-ready {
  background: #d32f2f; /* Red when ready */
  box-shadow: 0 0 5px #d32f2f;
}

/* RIGHT ACTION */
.mission-right {
  min-width: 60px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

/* BUTTON CLAIM */
.btn-claim {
  background: linear-gradient(to bottom, #d32f2f, #b71c1c);
  border: 1px solid #fbc02d;
  border-radius: 4px;
  color: #fff;
  cursor: pointer;
  padding: 2px;
  animation: pulse 1.5s infinite;
}
.btn-inner {
  border: 1px dashed rgba(255, 255, 255, 0.3);
  padding: 4px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.btn-inner span {
  font-size: 0.7em;
  font-weight: bold;
}
.reward-val {
  color: #ffeb3b;
}

/* STAMP DONE */
.stamp-done {
  border: 2px solid #5d4037;
  color: #5d4037;
  font-size: 0.65em;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 4px;
  transform: rotate(-10deg);
}

/* PREVIEW */
.reward-preview {
  font-size: 0.8em;
  color: #fbc02d;
  font-weight: bold;
}

/* ANIMATION */
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

/* SCROLLBAR */
.custom-scroll::-webkit-scrollbar {
  width: 4px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 2px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}
</style>
