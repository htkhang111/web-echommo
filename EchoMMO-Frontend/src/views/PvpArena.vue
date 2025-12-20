<template>
  <div class="pvp-arena-container">
    <div class="arena-bg"></div>
    <div class="scanline"></div>

    <div class="arena-header">
      <h1 class="title-main">VÕ ĐÀI TRANH HÙNG</h1>
      <div class="title-sub">Nơi anh hùng tao ngộ - Tuyệt kỹ phân tranh</div>
    </div>

    <div v-if="gameState === 'LOBBY' || gameState === 'SEARCHING'" class="lobby-panel">
      <div class="char-profile">
        <div class="char-portrait shadow-gold">
          <img :src="myAvatar" class="portrait-img" />
          <div class="lv-tag">Lv.{{ myLevel }}</div>
        </div>
        <div class="char-details">
          <h2 class="name-text">{{ myName }}</h2>
          <div class="stats-row">
            <span class="stat-item"><i class="fas fa-bolt"></i> Lực chiến: {{ myPower }}</span>
            <span class="stat-item win-color"><i class="fas fa-trophy"></i> Thắng: {{ myWins }}</span>
          </div>
        </div>
      </div>

      <div class="lobby-actions">
        <button class="btn-gold-glow" @click="toggleSearch" :disabled="gameState === 'SEARCHING'">
          <span v-if="gameState === 'LOBBY'">BẮT ĐẦU TÌM TRẬN</span>
          <span v-else><i class="fas fa-yin-yang fa-spin"></i> ĐANG TÌM... ({{ searchTimer }}s)</span>
        </button>
        <button v-if="gameState === 'SEARCHING'" class="btn-outline-red" @click="cancelSearch">HỦY TÌM</button>
      </div>
    </div>

    <div v-if="gameState === 'MATCH_FOUND'" class="match-found-overlay">
      <div class="match-alert-card">
        <div class="alert-title">ĐỐI THỦ XUẤT HIỆN</div>
        <div class="enemy-preview">
          <img src="https://i.imgur.com/mZ5tN8v.png" class="enemy-img" />
          <h3 class="enemy-name">{{ enemyName }}</h3>
          <p class="enemy-lv">Cấp độ: {{ enemyLevel }}</p>
        </div>
        <div class="accept-timer">
          <div class="timer-bar" :style="{ width: (acceptTimer / 10 * 100) + '%' }"></div>
        </div>
        <div class="modal-btns">
          <button class="btn-flat-red" @click="declineMatch">TỪ CHỐI</button>
          <button class="btn-flat-green" @click="acceptMatch">XÁC NHẬN</button>
        </div>
      </div>
    </div>

    <div v-if="gameState === 'BATTLE' || gameState === 'RESULT'" class="battle-stage">
      <div class="battle-top-info">
        <div class="turn-count">HIỆP ĐẤU: {{ currentTurnCount }}</div>
      </div>

      <div class="fighter-zone">
        <div class="fighter-box p1">
          <div class="fighter-header">
            <div class="f-name">{{ myName }}</div>
            <div class="f-hp-bar">
              <div class="hp-inner shadow-green" :style="{ width: (myHp / myMaxHp * 100) + '%' }"></div>
              <span class="hp-val">{{ myHp }}/{{ myMaxHp }}</span>
            </div>
          </div>
          <div class="f-image-wrap">
            <img :src="myAvatar" class="f-img" />
            <div v-if="battlePhase === 'RPS_REVEAL' && myRpsMove" class="move-icon">
              <i :class="getRpsIcon(myRpsMove)"></i>
            </div>
          </div>
        </div>

        <div class="vs-divider-gold">VS</div>

        <div class="fighter-box p2">
          <div class="fighter-header">
            <div class="f-name">{{ enemyName }}</div>
            <div class="f-hp-bar">
              <div class="hp-inner shadow-red" :style="{ width: (enemyHp / enemyMaxHp * 100) + '%' }"></div>
              <span class="hp-val">{{ enemyHp }}/{{ enemyMaxHp }}</span>
            </div>
          </div>
          <div class="f-image-wrap">
            <img src="https://i.imgur.com/mZ5tN8v.png" class="f-img flipped" />
            <div v-if="battlePhase === 'RPS_REVEAL' && enemyRpsMove" class="move-icon">
              <i v-if="enemyRpsMove === 'HIDDEN'" class="fas fa-question"></i>
              <i v-else :class="getRpsIcon(enemyRpsMove)"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="battle-middle-layout">
        <div class="rps-control-box">
          <div v-if="battlePhase === 'RPS_WAIT' && gameState === 'BATTLE'" class="rps-btns">
            <button @click="submitRps('ROCK')" class="rps-item btn-rock"><i class="fas fa-hand-rock"></i></button>
            <button @click="submitRps('PAPER')" class="rps-item btn-paper"><i class="fas fa-hand-paper"></i></button>
            <button @click="submitRps('SCISSORS')" class="rps-item btn-scissors"><i class="fas fa-hand-scissors"></i></button>
          </div>
          <div v-else class="rps-wait-text">ĐANG CHỜ KẾT QUẢ...</div>
        </div>

        <div class="battle-logs-v3" ref="logContainer">
          <div v-for="(log, i) in battleLogs" :key="i" class="log-line">
            <span class="log-bullet">»</span> {{ log }}
          </div>
        </div>
      </div>

      <div class="battle-chat-section">
        <div class="chat-messages-box" ref="chatBox">
            <div v-for="(msg, i) in matchMessages" :key="i" class="chat-row" :class="{'my-msg': msg.isMe}">
                <span class="chat-sender">{{ msg.sender }}:</span>
                <span class="chat-content">{{ msg.text }}</span>
            </div>
        </div>
        <div class="chat-input-wrap">
            <input v-model="chatInput" @keyup.enter="sendMatchChat" placeholder="Nhập lời khiêu khích..." maxlength="50" />
            <button @click="sendMatchChat"><i class="fas fa-paper-plane"></i></button>
        </div>
      </div>

      <div class="battle-footer">
        <button class="btn-surrender" @click="handleSurrender">
          <i class="fas fa-door-open"></i> ĐẦU HÀNG & RỜI VÕ ĐÀI
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useAuthStore } from '../stores/authStore';
import { useChatStore } from '../stores/chatStore';
import axios from 'axios';

const API_URL = "http://localhost:8080/api/pvp";
const authStore = useAuthStore();
const chatStore = useChatStore();

// STATE
const gameState = ref('LOBBY');
const matchId = ref(null);
const battlePhase = ref('RPS_WAIT');
const chatInput = ref('');
const matchMessages = ref([]);

// NHÂN VẬT
const myName = ref(authStore.character?.name || "Tôi");
const myAvatar = ref(authStore.user?.avatarUrl || 'https://i.imgur.com/7Y7t5Xp.png');
const myLevel = ref(authStore.character?.level || 1);
const myPower = ref(authStore.character?.totalPower || 0);
const myWins = ref(authStore.character?.pvpWins || 0);

const myHp = ref(100);
const myMaxHp = ref(100);

// ĐỐI THỦ
const enemyName = ref("Đang tìm...");
const enemyLevel = ref(0);
const enemyHp = ref(100);
const enemyMaxHp = ref(100);

const currentTurnCount = ref(1);
const battleLogs = ref([]);
const logContainer = ref(null);
const chatBox = ref(null);
const myRpsMove = ref(null);
const enemyRpsMove = ref(null);

let pollId = null;
let searchTimer = ref(0);
let searchInterval = null;
let acceptTimer = ref(10);

const getHeaders = () => ({ 'Authorization': `Bearer ${localStorage.getItem('token')}` });

// RESET VỀ SẢNH
const resetToLobby = () => {
  gameState.value = 'LOBBY';
  matchId.value = null;
  battleLogs.value = [];
  matchMessages.value = [];
  stopPolling();
  if (searchInterval) clearInterval(searchInterval);
};

// --- CHAT LOGIC ---
const sendMatchChat = () => {
    if (!chatInput.value.trim()) return;
    // Ở bản này ta dùng tạm mảng local. Để chat với người khác thật bạn cần qua Socket
    // Đây là ví dụ gửi tin nhắn mẫu
    matchMessages.value.push({
        sender: myName.value,
        text: chatInput.value,
        isMe: true
    });
    chatInput.value = '';
    nextTick(() => { chatBox.value.scrollTop = chatBox.value.scrollHeight; });
};

const handleSurrender = async () => {
  if (!confirm("Xác nhận đầu hàng?")) return;
  try {
    await axios.post(`${API_URL}/surrender`, { matchId: matchId.value }, { headers: getHeaders() });
    resetToLobby();
  } catch (e) { resetToLobby(); }
};

const toggleSearch = async () => {
  gameState.value = 'SEARCHING';
  searchTimer.value = 0;
  try {
    await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() });
    searchInterval = setInterval(() => searchTimer.value++, 1000);
    startPolling();
  } catch (e) { resetToLobby(); }
};

const cancelSearch = () => resetToLobby();

const submitRps = async (move) => {
  myRpsMove.value = move;
  battlePhase.value = 'RPS_REVEAL';
  try {
    await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() });
  } catch (e) { console.error(e); }
};

const acceptMatch = async () => {
  try { await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() }); } 
  catch (e) { console.error(e); }
};

const declineMatch = () => resetToLobby();

const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      if (res.data) syncData(res.data);
    } catch (e) { if (e.response?.status === 403) resetToLobby(); }
  }, 1000);
};

const stopPolling = () => { clearInterval(pollId); pollId = null; };

const syncData = async (data) => {
  matchId.value = data.matchId;
  const isP1 = (data.p1Id == data.myId);

  myLevel.value = isP1 ? data.p1Level : data.p2Level;
  myPower.value = isP1 ? data.p1Power : data.p2Power;
  myHp.value = isP1 ? data.p1Hp : data.p2Hp;
  myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyName.value = isP1 ? data.p2Name : data.p1Name;
  enemyLevel.value = isP1 ? data.p2Level : data.p1Level;
  enemyHp.value = isP1 ? data.p2Hp : data.p1Hp;
  enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;

  if (data.status === 'PENDING') {
    gameState.value = 'MATCH_FOUND';
    if (searchInterval) clearInterval(searchInterval);
  } else if (data.status === 'ACTIVE') {
    gameState.value = 'BATTLE';
    const myM = isP1 ? data.p1Move : data.p2Move;
    const enM = isP1 ? data.p2Move : data.p1Move;
    enemyRpsMove.value = enM;

    if (!myM && !enM) {
      battlePhase.value = 'RPS_WAIT';
      myRpsMove.value = null;
    }

    if (data.lastLog && battleLogs.value[battleLogs.value.length - 1] !== data.lastLog) {
      battleLogs.value.push(data.lastLog);
      nextTick(() => { if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight; });
    }
    currentTurnCount.value = data.turnCount;
  } else if (data.status === 'FINISHED') {
    const win = (data.winnerId == data.myId);
    await authStore.fetchCharacter();
    myWins.value = authStore.character?.pvpWins || 0;
    alert(win ? "BẠN ĐÃ CHIẾN THẮNG!" : "BẠN ĐÃ THẤT BẠI!");
    resetToLobby();
  }
};

onMounted(() => startPolling());
onUnmounted(() => resetToLobby());

const getRpsIcon = (m) => {
  if (m === 'ROCK') return 'fas fa-hand-rock';
  if (m === 'PAPER') return 'fas fa-hand-paper';
  return 'fas fa-hand-scissors';
};
</script>

<style scoped>
.pvp-arena-container {
  position: relative;
  width: 100%;
  height: 92vh;
  display: flex;
  flex-direction: column;
  color: #fff;
  font-family: 'Noto Serif TC', serif;
  overflow: hidden;
}

.arena-bg {
  position: absolute;
  inset: 0;
  background: url('https://img.freepik.com/free-photo/vibrant-night-sky-with-stars-nebula-generative-ai_188544-9721.jpg') center/cover;
  filter: brightness(0.2) saturate(0.5);
  z-index: 0;
}

.scanline {
  position: absolute;
  inset: 0;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.1) 50%), linear-gradient(90deg, rgba(255, 0, 0, 0.03), rgba(0, 255, 0, 0.01), rgba(0, 0, 255, 0.03));
  background-size: 100% 3px, 3px 100%;
  pointer-events: none;
  z-index: 10;
}

.arena-header { z-index: 1; text-align: center; padding: 15px; }
.title-main { font-family: 'Cinzel', serif; color: #ffd700; font-size: 2.2rem; text-shadow: 0 0 15px #000; margin: 0; }
.title-sub { font-size: 0.85rem; color: #aaa; letter-spacing: 2px; }

/* LOBBY V3 */
.lobby-panel {
  z-index: 1;
  margin: auto;
  background: rgba(15, 15, 15, 0.85);
  padding: 35px;
  border-radius: 15px;
  border: 1px solid #ffd70044;
  width: 90%;
  max-width: 480px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 40px rgba(0,0,0,0.8);
}

.char-portrait { position: relative; width: 110px; height: 110px; border: 2px solid #ffd700; border-radius: 12px; overflow: hidden; background: #000; }
.shadow-gold { box-shadow: 0 0 15px rgba(255, 215, 0, 0.3); }

.btn-gold-glow { 
  width: 100%; padding: 14px; background: linear-gradient(45deg, #ffd700, #b8860b); color: #000; border: none; 
  font-weight: bold; font-family: 'Cinzel'; cursor: pointer; transition: 0.3s; box-shadow: 0 0 20px #ffd70033;
}
.btn-gold-glow:hover { transform: scale(1.03); filter: brightness(1.2); }

/* BATTLE STAGE */
.battle-stage { z-index: 1; flex: 1; display: flex; flex-direction: column; padding: 10px; }
.fighter-zone { display: flex; justify-content: space-around; align-items: center; padding: 15px 0; }
.fighter-box { width: 35%; }

.f-hp-bar { width: 100%; height: 16px; background: #111; border: 1px solid #555; position: relative; border-radius: 4px; overflow: hidden; }
.hp-inner { height: 100%; transition: 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.shadow-green { background: linear-gradient(to right, #2e7d32, #4caf50); box-shadow: 0 0 10px #4caf50; }
.shadow-red { background: linear-gradient(to left, #c62828, #f44336); box-shadow: 0 0 10px #f44336; }

.vs-divider-gold { font-family: 'Cinzel'; font-size: 2.2rem; color: #ffd700; text-shadow: 0 0 15px #ff4500; font-weight: bold; }

/* RPS & LOG LAYOUT */
.battle-middle-layout { display: flex; flex-direction: column; gap: 10px; margin: 10px 0; }
.rps-control-box { height: 70px; display: flex; justify-content: center; align-items: center; }
.rps-btns { display: flex; gap: 20px; }
.rps-item { 
  width: 60px; height: 60px; border-radius: 50%; background: #222; color: #ffd700; border: 2px solid #ffd700; 
  cursor: pointer; font-size: 1.4rem; transition: 0.3s;
}
.rps-item:hover { transform: translateY(-5px); background: #ffd700; color: #000; }

.battle-logs-v3 { 
    height: 90px; background: rgba(0, 0, 0, 0.7); overflow-y: auto; padding: 12px; 
    font-size: 0.85rem; border: 1px solid #333; color: #ddd; border-radius: 8px;
}

/* BATTLE CHAT */
.battle-chat-section {
    background: rgba(20, 20, 20, 0.9);
    border: 1px solid #444;
    border-radius: 8px;
    margin: 0 5px;
    display: flex;
    flex-direction: column;
}
.chat-messages-box { height: 100px; overflow-y: auto; padding: 8px; font-size: 0.85rem; }
.chat-row { margin-bottom: 4px; color: #bbb; }
.chat-sender { color: #ffd700; font-weight: bold; margin-right: 5px; }
.my-msg .chat-sender { color: #4caf50; }
.chat-input-wrap { display: flex; border-top: 1px solid #333; padding: 5px; }
.chat-input-wrap input { flex: 1; background: transparent; border: none; color: #fff; padding: 5px; outline: none; }
.chat-input-wrap button { background: #ffd700; border: none; width: 35px; border-radius: 4px; cursor: pointer; }

.btn-surrender { color: #f44336; border: 1px solid #f44336; background: transparent; padding: 6px 15px; margin: 10px auto; cursor: pointer; }

/* MATCH MODAL */
.match-alert-card { background: #111; border: 2px solid #ffd700; padding: 25px; border-radius: 12px; box-shadow: 0 0 50px #000; }
</style>