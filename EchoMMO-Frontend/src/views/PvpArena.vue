<template>
  <div class="pvp-arena-container">
    <div class="arena-bg"></div>

    <div class="arena-header">
      <h1 class="title">THIÊN HẠ ĐỆ NHẤT LÔI ĐÀI</h1>
      <div class="subtitle">Tỷ thí võ công - Phân định cao thấp</div>
    </div>

    <div v-if="gameState === 'LOBBY' || gameState === 'SEARCHING'" class="lobby-panel">
      <div class="my-character">
        <div class="avatar-frame">
          <img :src="myAvatar || 'https://i.imgur.com/7Y7t5Xp.png'" alt="My Char" />
        </div>
        <div class="char-info">
          <h3>{{ myName }}</h3>
          <p>Cấp độ: <span class="highlight">{{ myLevel }}</span></p>
          <p>Chiến lực: <span class="highlight">{{ myPower }}</span></p>
        </div>
      </div>

      <div class="action-area">
        <button 
          class="btn-search" 
          :class="{ searching: gameState === 'SEARCHING' }"
          @click="toggleSearch"
          :disabled="gameState === 'SEARCHING'"
        >
          <span v-if="gameState === 'LOBBY'">TÌM ĐỐI THỦ</span>
          <span v-else>
            <i class="fas fa-spinner fa-spin"></i> ĐANG TÌM... ({{ searchTimer }}s)
          </span>
        </button>
        
        <button v-if="gameState === 'SEARCHING'" class="btn-cancel" @click="cancelSearch">
            HỦY
        </button>

        <p v-if="gameState === 'SEARCHING'" class="tip-text">Đang tìm kiếm cao thủ cùng cấp độ...</p>
      </div>
    </div>

    <div v-if="gameState === 'MATCH_FOUND'" class="match-found-modal">
      <div class="modal-content">
        <h2>ĐÃ TÌM THẤY ĐỐI THỦ!</h2>
        <div class="opponent-preview">
          <div class="unknown-avatar">?</div>
          <p>Cao thủ ẩn danh (Lv. {{ enemyLevel }})</p>
        </div>
        
        <div class="timer-bar">
          <div class="progress" :style="{ width: (acceptTimer / 10 * 100) + '%' }"></div>
        </div>
        <p class="timer-text">Tự động hủy sau: {{ acceptTimer }}s</p>

        <div class="modal-actions">
          <button class="btn-decline" @click="declineMatch">TỪ CHỐI</button>
          <button class="btn-accept" @click="acceptMatch">CHẤP NHẬN CHIẾN ĐẤU</button>
        </div>
      </div>
    </div>

    <div v-if="gameState === 'BATTLE' || gameState === 'RESULT'" class="battle-interface">
      
      <div class="scoreboard">
        <div class="round-indicator">HIỆP ĐẤU THỨ: {{ currentTurnCount }}</div>
      </div>

      <div class="combat-zone">
        <div class="fighter p1" :class="{ 'turn-active': isMyTurn && battlePhase === 'COMBAT' }">
          <div class="fighter-visual">
            <img :src="myAvatar || 'https://i.imgur.com/7Y7t5Xp.png'" />
            <div class="hp-bar-wrap">
              <div class="hp-bar" :style="{ width: (myHp / myMaxHp * 100) + '%' }"></div>
            </div>
            <span class="hp-text">{{ myHp }}/{{ myMaxHp }}</span>
          </div>
          <div v-if="battlePhase === 'RPS_REVEAL' && myRpsMove" class="rps-bubble">
            <i :class="getRpsIcon(myRpsMove)"></i>
          </div>
        </div>

        <div class="vs-center">
          <span v-if="battlePhase === 'RPS_WAIT'" class="blink">OẲN TÙ TÌ</span>
          <span v-else-if="battlePhase === 'RPS_REVEAL'">SO KÈ...</span>
          <span v-else-if="battlePhase === 'COMBAT'">
             {{ isMyTurn ? 'BẠN TẤN CÔNG!' : 'ĐỠ ĐÒN!' }}
          </span>
        </div>

        <div class="fighter p2" :class="{ 'turn-active': !isMyTurn && battlePhase === 'COMBAT' }">
          <div class="fighter-visual">
            <img src="https://i.imgur.com/mZ5tN8v.png" /> <div class="hp-bar-wrap">
              <div class="hp-bar enemy" :style="{ width: (enemyHp / enemyMaxHp * 100) + '%' }"></div>
            </div>
             <span class="hp-text">{{ enemyHp }}/{{ enemyMaxHp }}</span>
          </div>
          <div v-if="battlePhase === 'RPS_REVEAL' && enemyRpsMove" class="rps-bubble">
             <i v-if="enemyRpsMove === 'HIDDEN'" class="fas fa-question"></i>
             <i v-else :class="getRpsIcon(enemyRpsMove)"></i>
          </div>
        </div>
      </div>

      <div v-if="battlePhase === 'RPS_WAIT'" class="rps-controls">
        <p>Chọn chiêu để giành quyền tấn công:</p>
        <div class="rps-buttons">
          <button @click="submitRps('ROCK')" class="rps-btn"><i class="fas fa-hand-rock"></i> BÚA</button>
          <button @click="submitRps('PAPER')" class="rps-btn"><i class="fas fa-hand-paper"></i> BAO</button>
          <button @click="submitRps('SCISSORS')" class="rps-btn"><i class="fas fa-hand-scissors"></i> KÉO</button>
        </div>
      </div>

      <div class="battle-log" ref="logContainer">
        <p v-for="(log, index) in battleLogs" :key="index" class="log-line">{{ log }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useAuthStore } from '../stores/authStore'; 
import { useRouter } from 'vue-router';
import axios from 'axios';

// --- CONFIG ---
const API_URL = "http://localhost:8080/api/pvp"; 
const POLL_INTERVAL = 1000;

const authStore = useAuthStore();
const router = useRouter();

// --- STATE ---
const gameState = ref('LOBBY'); // LOBBY, SEARCHING, MATCH_FOUND, BATTLE, RESULT
const matchId = ref(null);

// Thông tin nhân vật (Khởi tạo mặc định, sẽ được cập nhật bởi API status)
const myName = ref(authStore.character?.name || "Tôi");
const myAvatar = ref(authStore.user?.avatarUrl);
const myLevel = ref(authStore.character?.level || 1);
const myPower = ref(authStore.character?.totalPower || 0);
const myMaxHp = ref(authStore.character?.maxHp || 100);
const myHp = ref(authStore.character?.currentHp || 100);

const enemyName = ref("???");
const enemyLevel = ref(0);
const enemyMaxHp = ref(100);
const enemyHp = ref(100);

const currentTurnCount = ref(1);
const battleLogs = ref([]);
const logContainer = ref(null);
const battlePhase = ref('RPS_WAIT'); 
const myRpsMove = ref(null);
const enemyRpsMove = ref(null);
const isMyTurn = ref(false);

let pollIntervalId = null;
let searchTimer = ref(0);
let acceptTimer = ref(10);
let searchIntervalId = null;

// --- HÀM HELPER: LẤY HEADER TOKEN MỚI NHẤT ---
const getHeaders = () => {
  const token = authStore.token || localStorage.getItem('token');
  return { 
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
};

// --- HÀM HELPER: XỬ LÝ LỖI (Đặc biệt là 403) ---
const handleError = (e) => {
    console.error("API Error:", e);
    if (e.response && e.response.status === 403) {
        alert("Phiên đăng nhập hết hạn! Vui lòng đăng nhập lại.");
        authStore.logout();
        router.push('/login');
    }
};

// --- API ACTIONS ---

// 1. Tìm trận
const toggleSearch = async () => {
  if (gameState.value === 'LOBBY') {
    try {
      gameState.value = 'SEARCHING';
      searchTimer.value = 0;
      
      await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() });
      
      // Đếm giây hiển thị
      if (searchIntervalId) clearInterval(searchIntervalId);
      searchIntervalId = setInterval(() => searchTimer.value++, 1000);

      // Bắt đầu polling
      startPolling();
    } catch (e) {
      handleError(e);
      gameState.value = 'LOBBY';
    }
  }
};

const cancelSearch = () => {
    stopPolling();
    clearInterval(searchIntervalId);
    gameState.value = 'LOBBY';
};

// 2. Chấp nhận trận đấu
const acceptMatch = async () => {
  try {
    await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() });
    // Polling sẽ tự chuyển state khi status thành ACTIVE
  } catch (e) {
    handleError(e);
  }
};

const declineMatch = () => {
  stopPolling();
  clearInterval(searchIntervalId);
  gameState.value = 'LOBBY';
};

// 3. Gửi nước đi RPS
const submitRps = async (move) => {
  myRpsMove.value = move;
  battlePhase.value = 'RPS_REVEAL'; 
  try {
    await axios.post(`${API_URL}/move`, { 
      matchId: matchId.value, 
      move: move 
    }, { headers: getHeaders() });
  } catch (e) {
    handleError(e);
  }
};

// --- CORE: POLLING FUNCTION ---
// Gọi API /status liên tục để đồng bộ dữ liệu
const startPolling = () => {
  if (pollIntervalId) return;
  
  pollIntervalId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data;

      if (!data) return; 

      syncGameState(data);

    } catch (e) {
      // Nếu lỗi 403 khi polling -> Dừng ngay
      if (e.response && e.response.status === 403) {
          stopPolling();
          handleError(e);
      }
    }
  }, POLL_INTERVAL);
};

const stopPolling = () => {
  if (pollIntervalId) clearInterval(pollIntervalId);
  pollIntervalId = null;
};

// --- LOGIC ĐỒNG BỘ DỮ LIỆU ---
const syncGameState = (data) => {
  matchId.value = data.matchId;
  const isPlayer1 = data.p1Id === authStore.character.charId;
  
  // [QUAN TRỌNG] Cập nhật Level và Power thực tế từ API trả về
  // Dù ở LOBBY hay BATTLE, thông tin p1 luôn là của người gọi API (xem logic Controller)
  myLevel.value = isPlayer1 ? data.p1Level : data.p2Level;
  myPower.value = isPlayer1 ? data.p1Power : data.p2Power;
  
  // Cập nhật máu
  myHp.value = isPlayer1 ? data.p1Hp : data.p2Hp;
  myMaxHp.value = isPlayer1 ? data.p1MaxHp : data.p2MaxHp;
  enemyHp.value = isPlayer1 ? data.p2Hp : data.p1Hp;
  enemyMaxHp.value = isPlayer1 ? data.p2MaxHp : data.p1MaxHp;
  
  enemyName.value = isPlayer1 ? data.p2Name : data.p1Name;
  enemyLevel.value = isPlayer1 ? data.p2Level : data.p1Level;

  // --- Xử lý chuyển trạng thái ---
  if (data.status === 'PENDING') {
    if (gameState.value !== 'MATCH_FOUND') {
        gameState.value = 'MATCH_FOUND';
        clearInterval(searchIntervalId);
    }
  } 
  else if (data.status === 'ACTIVE') {
    gameState.value = 'BATTLE';
    
    const myMove = isPlayer1 ? data.p1Move : data.p2Move;
    const enemyMove = isPlayer1 ? data.p2Move : data.p1Move;
    
    enemyRpsMove.value = enemyMove; 

    // Logic xác định Phase
    if (!myMove && !enemyMove) {
      if (battlePhase.value !== 'RPS_WAIT') {
          battlePhase.value = 'RPS_WAIT';
          myRpsMove.value = null;
          enemyRpsMove.value = null;
      }
    } else if (myMove && !enemyMove) {
       battlePhase.value = 'RPS_REVEAL'; 
    }

    currentTurnCount.value = data.turnCount;
    
    // Xử lý Log
    if (data.lastLog && (!battleLogs.value.length || battleLogs.value[battleLogs.value.length - 1] !== data.lastLog)) {
       addLog(data.lastLog);
       // Nếu có log mới và cả 2 đã ra đòn -> Combat
       if (myMove && enemyMove && enemyMove !== 'HIDDEN') {
           battlePhase.value = 'COMBAT';
           isMyTurn.value = data.lastLog.includes(myName.value + " thắng RPS");
       }
    }
  } 
  else if (data.status === 'FINISHED') {
    if (gameState.value !== 'RESULT') {
       gameState.value = 'RESULT';
       stopPolling();
       
       const isWinner = data.winnerId === authStore.character.charId;
       setTimeout(() => {
           alert(isWinner ? "CHIẾN THẮNG! BẠN LÀ VUA LÔI ĐÀI." : "THẤT BẠI! HÃY LUYỆN TẬP THÊM.");
           gameState.value = 'LOBBY';
       }, 500);
    }
  }
};

const addLog = (msg) => {
  battleLogs.value.push(msg);
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight;
  });
};

// Gọi polling ngay khi vào trang để lấy data mới nhất (Level, Power) kể cả khi chưa tìm trận
onMounted(() => {
    startPolling();
});

onUnmounted(() => {
  stopPolling();
  if (searchIntervalId) clearInterval(searchIntervalId);
});

const getRpsIcon = (move) => {
  if (move === 'ROCK') return 'fas fa-hand-rock';
  if (move === 'PAPER') return 'fas fa-hand-paper';
  if (move === 'SCISSORS') return 'fas fa-hand-scissors';
  return '';
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@700&family=Noto+Serif+TC:wght@500;700&display=swap");

.pvp-arena-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 80vh;
  color: #fff;
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
}

.arena-bg {
  position: absolute;
  inset: 0;
  background-image: url('https://img.freepik.com/free-vector/asian-landscape-mountains-graphic-style_23-2148239563.jpg?w=1380');
  background-size: cover;
  filter: brightness(0.4) sepia(0.3);
  z-index: 0;
}

.arena-header {
  position: relative;
  z-index: 2;
  text-align: center;
  margin-top: 20px;
}

.title {
  font-family: 'Cinzel', serif;
  font-size: 2rem;
  color: #fbc02d;
  text-shadow: 0 0 10px #b71c1c;
  margin: 0;
}
.subtitle { color: #d7ccc8; letter-spacing: 2px; font-size: 0.9rem; }

/* LOBBY */
.lobby-panel {
  position: relative;
  z-index: 2;
  margin-top: 40px;
  background: rgba(0,0,0,0.6);
  padding: 30px;
  border: 2px solid #5d4037;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 90%;
  max-width: 400px;
}

.my-character { display: flex; align-items: center; gap: 20px; }
.avatar-frame { width: 80px; height: 80px; border: 3px solid #fbc02d; border-radius: 50%; overflow: hidden; background: #000; }
.avatar-frame img { width: 100%; height: 100%; object-fit: cover; }
.highlight { color: #fbc02d; font-weight: bold; }

.action-area { display: flex; flex-direction: column; align-items: center; gap: 10px; }

.btn-search {
  padding: 12px 30px;
  background: #b71c1c;
  color: #fff;
  border: none;
  font-family: 'Cinzel', serif;
  font-size: 1rem;
  cursor: pointer;
  border-radius: 4px;
  transition: 0.3s;
  box-shadow: 0 0 15px rgba(183, 28, 28, 0.5);
}
.btn-search:hover:not(:disabled) { background: #d32f2f; transform: scale(1.05); }
.btn-search:disabled { background: #424242; cursor: default; opacity: 0.8; }

.btn-cancel {
    background: transparent;
    border: 1px solid #757575;
    color: #bdbdbd;
    padding: 5px 15px;
    cursor: pointer;
    font-size: 0.8rem;
}
.btn-cancel:hover { border-color: #fff; color: #fff; }

/* MODAL */
.match-found-modal {
  position: absolute;
  inset: 0;
  z-index: 100;
  background: rgba(0,0,0,0.9);
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  background: #3e2723;
  padding: 30px;
  border: 2px solid #fbc02d;
  text-align: center;
  width: 90%;
  max-width: 400px;
  animation: slideDown 0.3s ease;
}
.timer-bar { width: 100%; height: 8px; background: #1a1a1a; margin: 20px 0 5px; border-radius: 4px; overflow: hidden; }
.progress { height: 100%; background: #fbc02d; transition: width 1s linear; }
.modal-actions { display: flex; justify-content: center; gap: 15px; margin-top: 20px; }
.btn-decline { background: #424242; color: #fff; border: 1px solid #fff; padding: 8px 15px; cursor: pointer; }
.btn-accept { background: #fbc02d; color: #000; font-weight: bold; border: none; padding: 8px 25px; cursor: pointer; box-shadow: 0 0 10px #fbc02d; }

/* BATTLE */
.battle-interface {
  position: relative;
  z-index: 2;
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 10px;
}

.scoreboard { text-align: center; margin-bottom: 20px; }
.round-indicator { font-family: 'Cinzel', serif; font-size: 1.2rem; color: #fbc02d; text-shadow: 0 0 5px #000; }

.combat-zone {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  margin-bottom: auto; 
}

.fighter { text-align: center; position: relative; transition: 0.3s; opacity: 0.8; width: 35%; }
.fighter.turn-active { opacity: 1; transform: scale(1.05); z-index: 10; }
.fighter img { height: 180px; filter: drop-shadow(0 0 5px rgba(0,0,0,0.8)); }

.hp-bar-wrap { width: 100%; height: 8px; background: #000; border: 1px solid #fff; margin: 5px 0; border-radius: 4px; overflow: hidden; }
.hp-bar { height: 100%; background: #2e7d32; transition: width 0.3s ease-out; }
.hp-bar.enemy { background: #c62828; }
.hp-text { font-size: 0.8rem; font-weight: bold; text-shadow: 1px 1px 0 #000; }

.vs-center { 
  font-family: 'Cinzel', serif; 
  font-size: 1.5rem; 
  color: #fbc02d; 
  text-align: center; 
  width: 30%;
  font-weight: bold;
}
.blink { animation: blinker 1.5s linear infinite; }

.rps-bubble {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  color: #000;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  box-shadow: 0 0 10px #fbc02d;
  z-index: 20;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.rps-controls {
  position: absolute;
  bottom: 110px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  background: rgba(0,0,0,0.8);
  padding: 15px;
  border-radius: 10px;
  border: 1px solid #fbc02d;
  width: 90%;
  max-width: 400px;
  z-index: 50;
}
.rps-buttons { display: flex; gap: 10px; justify-content: center; margin-top: 10px; }
.rps-btn {
  background: transparent; border: 1px solid #d7ccc8; color: #d7ccc8; padding: 8px 12px; cursor: pointer; font-size: 1rem;
  transition: 0.2s; flex: 1;
}
.rps-btn:hover { background: #fbc02d; color: #000; border-color: #fbc02d; }

.battle-log {
  height: 120px;
  background: rgba(0,0,0,0.7);
  border-top: 2px solid #5d4037;
  overflow-y: auto;
  padding: 10px;
  font-size: 0.85rem;
  color: #d7ccc8;
  margin-top: 20px;
}
.log-line { margin: 4px 0; border-bottom: 1px dashed rgba(255,255,255,0.1); padding-bottom: 2px; }

@keyframes slideDown { from { transform: translateY(-50px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
@keyframes popIn { from { transform: translateX(-50%) scale(0); } to { transform: translateX(-50%) scale(1); } }
@keyframes blinker { 50% { opacity: 0.5; } }
</style>