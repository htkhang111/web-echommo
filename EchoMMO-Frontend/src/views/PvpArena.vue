<template>
  <div class="wuxia-battle-container">
    
    <div v-if="gameState !== 'BATTLE'" class="lobby-overlay">
        <div class="lobby-content">
            <h1 class="wuxia-title">VÕ ĐÀI TRANH HÙNG</h1>
            
            <div class="hero-card">
                <div class="avatar-frame gold-border">
                    <img :src="authStore.character?.avatarUrl || defaultAvatar" />
                </div>
                <div class="hero-info">
                    <h2>{{ authStore.character?.name }}</h2>
                    <div class="stats-row">
                        <span class="stat-badge level">Lv.{{ authStore.character?.level || 1 }}</span>
                        <span class="stat-badge wins">🏆 Thắng: {{ authStore.character?.pvpWins || 0 }}</span>
                    </div>
                </div>
            </div>

            <div class="action-area">
                <button v-if="gameState === 'LOBBY'" class="btn-start" @click="toggleSearch">
                    <i class="fas fa-swords"></i> TÌM ĐỐI THỦ
                </button>
                <button v-else-if="gameState === 'SEARCHING'" class="btn-cancel" @click="cancelSearch">
                    <i class="fas fa-spinner fa-spin"></i> HỦY TÌM ({{ searchTimer }}s)
                </button>
            </div>

            <div v-if="gameState === 'MATCH_FOUND'" class="match-popup">
                <h3>⚔️ ĐỊCH THỦ XUẤT HIỆN ⚔️</h3>
                <div class="enemy-preview">
                    <img :src="enemyAvatar || defaultAvatar" />
                    <p>{{ enemyName }} (Lv.{{ enemyLevel }})</p>
                </div>
                <div class="popup-actions">
                    <button class="btn-decline" @click="declineMatch">BỎ QUA</button>
                    <button class="btn-accept" @click="acceptMatch" :disabled="hasAccepted">
                        {{ hasAccepted ? 'ĐANG CHỜ...' : 'CHẤP NHẬN' }} ({{ acceptTimer }}s)
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div v-else class="battle-arena">
        
        <div class="log-hint" @click="showLog = !showLog">
            <i class="fas fa-book"></i> [L] NHẬT KÝ
        </div>

        <transition name="fade">
            <div v-if="showLog" class="floating-log-panel custom-scroll">
                <div class="log-header">DIỄN BIẾN TRẬN ĐẤU</div>
                <div class="log-content">
                    <div v-if="battlePhase === 'RPS_WAIT'" class="log-line warning">
                        ➤ Hãy ra chiêu trước khi hết giờ!
                    </div>
                    <div v-else class="log-line highlight">
                        {{ consoleMessage }}
                    </div>
                     <div v-for="(log, i) in battleLogs.slice().reverse()" :key="i" class="log-line old">
                        » {{ log }}
                    </div>
                </div>
            </div>
        </transition>

        <div class="top-timer-floating" :class="{'urgent': turnTimer <= 10}">
            <div class="timer-text">{{ turnTimer }}</div>
        </div>

        <div class="scene-stage">
            <div class="fighter enemy">
                <div class="hud enemy-hud">
                    <div class="name">{{ enemyName }} <small>Lv.{{ enemyLevel }}</small></div>
                    <div class="hp-bar-bg"><div class="hp-fill" :style="{width: percent(enemyHp, enemyMaxHp)+'%'}"></div></div>
                    <div class="hp-text">{{ enemyHp }}/{{ enemyMaxHp }}</div>
                </div>
                <div class="sprite-box">
                    <div class="shadow"></div>
                    <img :src="enemyAvatar || defaultAvatar" class="char-img" :class="{'shake-hit': isEnemyHit}" />
                </div>
            </div>

            <div class="fighter player">
                <div class="sprite-box">
                    <div class="shadow"></div>
                    <img :src="authStore.character?.avatarUrl || defaultAvatar" class="char-img" :class="{'shake-hit': isMyHit}" />
                </div>
                <div class="hud player-hud">
                    <div class="name">{{ authStore.character?.name }} <small>Lv.{{ authStore.character?.level || 1 }}</small></div>
                    <div class="hp-bar-bg"><div class="hp-fill" :style="{width: percent(myHp, myMaxHp)+'%'}"></div></div>
                    <div class="hp-text">{{ myHp }}/{{ myMaxHp }}</div>
                </div>
            </div>

            <div v-if="battlePhase === 'RPS_REVEAL'" class="clash-overlay">
                <div class="move-icon p1" :class="getMyAnimClass()"><i :class="getRpsIcon(myRpsMove)"></i></div>
                <div class="vs-spark">💥</div>
                <div class="move-icon p2" :class="getEnemyAnimClass()"><i :class="getRpsIcon(enemyRpsMove)"></i></div>
                <div class="result-text">{{ lastResultText }}</div>
            </div>
        </div>

        <div class="bottom-console-split">
            
            <div class="left-chat-column">
                <div class="panel-header">ĐÀM ĐẠO GIANG HỒ</div>
                <div class="chat-display custom-scroll" ref="chatBoxRef">
                     <div v-for="(msg, i) in matchMessages" :key="i" class="chat-bubble" :class="{'me': msg.isMe}">
                        <span class="sender">{{ msg.sender }}:</span> {{ msg.text }}
                    </div>
                </div>
                <div class="chat-input-row">
                    <input v-model="chatInput" @keyup.enter="sendPrivateChat" placeholder="Nhập tin nhắn..." />
                    <button @click="sendPrivateChat"><i class="fas fa-paper-plane"></i></button>
                </div>
            </div>

            <div class="right-action-column">
                <div v-if="battlePhase === 'RPS_WAIT'" class="square-grid-actions">
                    <button class="square-btn rock" @click="submitRps('ROCK')" :disabled="isActionPending" :class="{'btn-disabled': isActionPending}">
                        <i class="fas fa-hand-rock"></i><span>BÚA</span>
                    </button>
                    <button class="square-btn paper" @click="submitRps('PAPER')" :disabled="isActionPending" :class="{'btn-disabled': isActionPending}">
                        <i class="fas fa-hand-paper"></i><span>BAO</span>
                    </button>
                    <button class="square-btn scissor" @click="submitRps('SCISSORS')" :disabled="isActionPending" :class="{'btn-disabled': isActionPending}">
                        <i class="fas fa-hand-scissors"></i><span>KÉO</span>
                    </button>
                    
                    <button class="square-btn surrender" @click="handleSurrender">
                        <i class="fas fa-flag"></i><span>ĐẦU HÀNG</span>
                    </button>
                </div>
                
                <div v-else class="waiting-square">
                    <div class="spinner-box">
                        <i class="fas fa-yin-yang fa-spin"></i>
                        <p>XỬ LÝ...</p>
                        <button class="mini-exit-btn" @click="handleSurrender">THOÁT NGAY</button>
                    </div>
                </div>
            </div>

        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useAuthStore } from '../stores/authStore';
import axios from 'axios';

// --- CONFIG ---
const API_URL = "http://localhost:8080/api/pvp";
const authStore = useAuthStore();
const defaultAvatar = "https://i.imgur.com/7Y7t5Xp.png"; 

// --- GAME STATE ---
const gameState = ref('LOBBY'); 
const matchId = ref(null);
const battlePhase = ref('RPS_WAIT'); 
const turnTimer = ref(30);
const consoleMessage = ref("Sẵn sàng chiến đấu!");
const currentTurnCount = ref(1);
const lastResultText = ref('');
const hasAccepted = ref(false);
const showLog = ref(false); 
const isActionPending = ref(false);
const searchTimer = ref(0);
const acceptTimer = ref(10);

// --- DATA ---
const myHp = ref(100); const myMaxHp = ref(100); const myRpsMove = ref(null);
const enemyName = ref("Đối thủ"); const enemyLevel = ref(1);
const enemyHp = ref(100); const enemyMaxHp = ref(100); 
const enemyRpsMove = ref(null); const enemyAvatar = ref(null);

// --- UI REFS ---
const isMyHit = ref(false); const isEnemyHit = ref(false);
const battleLogs = ref([]); const matchMessages = ref([]); const chatInput = ref('');
const chatBoxRef = ref(null);

let pollId, searchInterval, acceptInterval, turnTimerInterval;

const getHeaders = () => ({ 'Authorization': `Bearer ${authStore.token}` });

// --- UTILS RESET ---
const resetLocalState = () => {
    matchId.value = null;
    battleLogs.value = [];
    matchMessages.value = [];
    battlePhase.value = 'RPS_WAIT';
    isActionPending.value = false;
    myHp.value = 100; myMaxHp.value = 100;
    enemyHp.value = 100; enemyMaxHp.value = 100;
    enemyName.value = "Đối thủ";
    consoleMessage.value = "Sẵn sàng chiến đấu!";
    currentTurnCount.value = 1;
    hasAccepted.value = false;
    showLog.value = false;
};

// --- KEYBOARD ---
const handleKeydown = (e) => {
    if (document.activeElement.tagName === 'INPUT') return;
    if (e.key === 'l' || e.key === 'L') showLog.value = !showLog.value;
};

// --- ANIMATION ---
const getMyAnimClass = () => {
    if (!myRpsMove.value || !enemyRpsMove.value) return '';
    const m = myRpsMove.value; const e = enemyRpsMove.value;
    if (m === e) return 'anim-clash'; 
    if ((m==='ROCK' && e==='SCISSORS') || (m==='SCISSORS' && e==='PAPER') || (m==='PAPER' && e==='ROCK')) {
        return m === 'SCISSORS' ? 'anim-cut-win' : 'anim-smash-win';
    }
    return 'anim-lose';
};

const getEnemyAnimClass = () => {
    if (!myRpsMove.value || !enemyRpsMove.value) return '';
    const m = myRpsMove.value; const e = enemyRpsMove.value;
    if (m === e) return 'anim-clash'; 
    if ((e==='ROCK' && m==='SCISSORS') || (e==='SCISSORS' && m==='PAPER') || (e==='PAPER' && m==='ROCK')) {
        return e === 'SCISSORS' ? 'anim-cut-win' : 'anim-smash-win';
    }
    return 'anim-lose';
};

// --- ACTIONS ---
const toggleSearch = async () => {
  resetLocalState();
  gameState.value = 'SEARCHING'; searchTimer.value = 0;
  searchInterval = setInterval(() => searchTimer.value++, 1000);
  try { await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() }); startPolling(); }
  catch(e) { console.error(e); resetToLobby(); }
};

const cancelSearch = async () => { 
    try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch(e){} 
    resetToLobby(); 
};

const acceptMatch = async () => {
  if(!matchId.value) return;
  hasAccepted.value = true;
  try { await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) { console.error(e); }
};

const declineMatch = async () => { cancelSearch(); };

// --- TIMER & AUTO MOVE ---
const startTurnTimer = () => {
  if(turnTimerInterval) clearInterval(turnTimerInterval);
  turnTimer.value = 30;
  turnTimerInterval = setInterval(() => {
    turnTimer.value--;
    if(turnTimer.value <= 0) {
      clearInterval(turnTimerInterval);
      
      // TỰ ĐỘNG RANDOM CHIÊU KHI HẾT GIỜ
      if(gameState.value === 'BATTLE' && battlePhase.value === 'RPS_WAIT') {
         if(!isActionPending.value) {
             const randomMoves = ['ROCK', 'PAPER', 'SCISSORS'];
             const randomChoice = randomMoves[Math.floor(Math.random() * randomMoves.length)];
             consoleMessage.value = `Hết giờ! Tự động chọn: ${randomChoice}`;
             submitRps(randomChoice);
         }
      }
    }
  }, 1000);
};

const submitRps = async (move) => {
  if (isActionPending.value) return; 
  if (!matchId.value) return;

  isActionPending.value = true;
  // Safety reset: Sau 5s tự mở khóa nếu kẹt
  setTimeout(() => { if(isActionPending.value) isActionPending.value = false; }, 5000);

  if(turnTimerInterval) clearInterval(turnTimerInterval);
  consoleMessage.value = `Đang thi triển: ${move === 'ROCK' ? 'BÚA' : move === 'PAPER' ? 'BAO' : 'KÉO'}...`;

  try { 
      await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() }); 
      battlePhase.value = 'RPS_REVEAL'; 
  } 
  catch (e) { 
      console.error("Move Error:", e);
      battlePhase.value = 'RPS_WAIT'; 
      isActionPending.value = false; // Mở lại ngay nếu lỗi
  }
};

const handleSurrender = async () => {
    if(confirm("Đại hiệp muốn rút lui? (Xử thua)")) {
        // Reset trước để thoát giao diện ngay
        resetToLobby();
        try { 
            if(matchId.value) await axios.post(`${API_URL}/surrender`, { matchId: matchId.value }, { headers: getHeaders() }); 
        } catch(e){} 
    }
};

const sendPrivateChat = async () => {
    if(!chatInput.value.trim()) return;
    try { await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); } catch(e){}
    chatInput.value = '';
};

// --- POLLING ---
const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data; if (!data) return;

      // FIX: BỎ QUA TRẬN CŨ KHI ĐANG Ở SẢNH
      if (data.status === 'FINISHED' && gameState.value !== 'BATTLE') return;

      // 1. MATCH FOUND
      if (data.status === 'PENDING' && gameState.value !== 'MATCH_FOUND') {
         gameState.value = 'MATCH_FOUND';
         if(searchInterval) clearInterval(searchInterval);
         matchId.value = data.matchId;
         syncBasicInfo(data);
         acceptTimer.value = 10;
         if(acceptInterval) clearInterval(acceptInterval);
         acceptInterval = setInterval(()=> { acceptTimer.value--; if(acceptTimer.value<=0 && !hasAccepted.value) declineMatch(); }, 1000);
      }
      // 2. ACTIVE
      else if (data.status === 'ACTIVE') {
         if (gameState.value !== 'BATTLE') {
            gameState.value = 'BATTLE'; 
            if(acceptInterval) clearInterval(acceptInterval);
            startTurnTimer();
         }
         syncBattleData(data);
      }
      // 3. FINISHED
      else if (data.status === 'FINISHED' && gameState.value === 'BATTLE') {
         syncBattleData(data);
         if(pollId) clearInterval(pollId); pollId = null; 
         const myCharId = Number(authStore.character?.id || authStore.user?.id);
         const winnerId = Number(data.winnerId);
         const msg = (winnerId === myCharId) ? "🏆 CHIẾN THẮNG!" : "💀 THẤT BẠI!";
         setTimeout(() => { alert(msg); resetToLobby(); }, 1500);
      }
    } catch (e) {}
  }, 1000);
};

const syncBasicInfo = (data) => {
  const myCharId = Number(authStore.character?.id || authStore.user?.id);
  const isP1 = (myCharId === Number(data.p1Id));
  enemyName.value = (isP1 ? data.p2Name : data.p1Name) || "Đối thủ";
  enemyLevel.value = (isP1 ? data.p2Level : data.p1Level) || 1;
  enemyAvatar.value = isP1 ? data.p2AvatarUrl : data.p1AvatarUrl;
};

const syncBattleData = (data) => {
  syncBasicInfo(data);
  const myCharId = Number(authStore.character?.id || authStore.user?.id);
  const isP1 = (myCharId === Number(data.p1Id));
  const newMyHp = isP1 ? data.p1Hp : data.p2Hp;
  const newEnemyHp = isP1 ? data.p2Hp : data.p1Hp;
  if(newMyHp < myHp.value) { isMyHit.value = true; setTimeout(()=>isMyHit.value=false, 300); }
  if(newEnemyHp < enemyHp.value) { isEnemyHit.value = true; setTimeout(()=>isEnemyHit.value=false, 300); }
  myHp.value = newMyHp; myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyHp.value = newEnemyHp; enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;
  if (currentTurnCount.value !== data.turnCount) currentTurnCount.value = data.turnCount;

  const myM = isP1 ? data.p1Move : data.p2Move;
  const enM = isP1 ? data.p2Move : data.p1Move;
  myRpsMove.value = myM; enemyRpsMove.value = enM;

  if (!myM) {
    if(battlePhase.value !== 'RPS_WAIT' && !isActionPending.value) {
        battlePhase.value = 'RPS_WAIT';
        startTurnTimer();
        consoleMessage.value = `Hiệp ${data.turnCount} bắt đầu!`;
        isActionPending.value = false; // Mở khóa
    }
  } else {
    isActionPending.value = false;
    battlePhase.value = 'RPS_REVEAL';
    if(turnTimerInterval) clearInterval(turnTimerInterval);
    if(enM) {
       if(data.lastLog) {
           if(battleLogs.value.length === 0 || battleLogs.value[battleLogs.value.length-1] !== data.lastLog) {
               battleLogs.value.push(data.lastLog);
               lastResultText.value = data.lastLog.includes("thắng") ? "THẮNG" : data.lastLog.includes("Hòa") ? "HÒA" : "THUA";
               consoleMessage.value = data.lastLog;
           }
       }
    } else {
       consoleMessage.value = "Chờ đối thủ...";
    }
  }
  if(data.messages && data.messages.length > matchMessages.value.length) {
    data.messages.slice(matchMessages.value.length).forEach(m => matchMessages.value.push({ 
       sender: m.senderName, text: m.content, isMe: Number(m.senderId) === myCharId 
    }));
    nextTick(() => { if(chatBoxRef.value) chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight; });
  }
};

const resetToLobby = () => {
  gameState.value = 'LOBBY'; 
  resetLocalState();
  if(pollId) clearInterval(pollId); pollId=null;
  if(searchInterval) clearInterval(searchInterval);
  if(acceptInterval) clearInterval(acceptInterval);
  if(turnTimerInterval) clearInterval(turnTimerInterval);
};

const percent = (c, m) => (m>0 ? (c/m)*100 : 0);
const getRpsIcon = (m) => ({ 'ROCK': 'fas fa-hand-rock', 'PAPER': 'fas fa-hand-paper', 'SCISSORS': 'fas fa-hand-scissors' }[m]);

onMounted(() => {
    resetToLobby();
    startPolling();
    window.addEventListener('keydown', handleKeydown);
});
onUnmounted(() => {
    resetToLobby();
    window.removeEventListener('keydown', handleKeydown);
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Patrick+Hand&family=Roboto+Condensed:wght@700&display=swap');

/* --- MAIN --- */
.wuxia-battle-container {
    width: 100%; height: 100%; min-height: 600px;
    background: #1a1a1a; font-family: 'Roboto Condensed', sans-serif;
    color: #e0e0e0; position: relative; overflow: hidden;
    display: flex; flex-direction: column;
}

/* --- LOBBY --- */
.lobby-overlay { flex: 1; background: url('https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1920&auto=format&fit=crop') center/cover; display: flex; align-items: center; justify-content: center; }
.lobby-content { background: rgba(0,0,0,0.85); border: 2px solid #b8860b; padding: 40px; border-radius: 10px; text-align: center; width: 400px; box-shadow: 0 0 30px #000; }
.wuxia-title { font-size: 2.5rem; color: #ffd700; margin-bottom: 20px; text-shadow: 0 0 10px #b8860b; }
.avatar-frame { width: 100px; height: 100px; border-radius: 50%; border: 3px solid #ffd700; overflow: hidden; margin: 0 auto; }
.avatar-frame img { width: 100%; height: 100%; object-fit: cover; }
.stats-row { margin-top: 10px; display: flex; justify-content: center; gap: 10px; }
.stat-badge { background: #333; color: #ffd700; padding: 2px 10px; border-radius: 4px; font-size: 0.9rem; border: 1px solid #555; }
.btn-start { background: #b8860b; color: #000; padding: 15px 30px; border: none; cursor: pointer; margin-top: 20px; width: 100%; font-weight: bold; border-radius: 4px; }
.btn-cancel { background: #333; color: #fff; padding: 15px 30px; border: none; margin-top: 20px; width: 100%; border-radius: 4px; }
.match-popup { margin-top: 20px; border-top: 1px solid #555; padding-top: 20px; animation: slideUp 0.3s; }
.btn-accept { background: #28a745; color: #fff; border: none; padding: 8px 20px; margin-left: 10px; cursor: pointer; }
.btn-decline { background: transparent; border: 1px solid #dc3545; color: #dc3545; padding: 8px 20px; cursor: pointer; }

/* --- BATTLE ARENA --- */
.battle-arena { 
    display: flex; flex-direction: column; height: 100%;
    background: url('http://googleusercontent.com/image_collection/image_retrieval/12189370109437321239_0') center/cover no-repeat;
    position: relative;
}
.battle-arena::before { content: ""; position: absolute; inset: 0; background: rgba(0,0,0,0.25); pointer-events: none; z-index: 0; }

/* --- LOG & TIMER --- */
.log-hint {
    position: absolute; top: 15px; left: 15px; z-index: 60;
    background: rgba(0,0,0,0.6); color: #ffd700;
    padding: 8px 15px; border-radius: 20px; border: 1px solid #b8860b;
    cursor: pointer; font-size: 0.85rem; font-weight: bold;
    transition: 0.2s;
}
.log-hint:hover { background: #b8860b; color: #000; }
.floating-log-panel {
    position: absolute; top: 60px; left: 15px; z-index: 60;
    width: 350px; max-height: 250px;
    background: rgba(10, 10, 10, 0.95);
    border: 1px solid #ffd700; border-radius: 8px;
    display: flex; flex-direction: column;
    box-shadow: 0 5px 20px rgba(0,0,0,0.8);
}
.log-header { padding: 8px; background: #222; color: #b8860b; font-weight: bold; border-bottom: 1px solid #444; text-align: center; font-size: 0.9rem; }
.log-content { padding: 10px; overflow-y: auto; color: #ccc; font-family: monospace; font-size: 0.9rem; }
.log-line { margin-bottom: 5px; }
.log-line.warning { color: #ff4444; }
.log-line.highlight { color: #fff; border-left: 3px solid #b8860b; padding-left: 5px; font-weight: bold; }
.log-line.old { opacity: 0.6; font-size: 0.8rem; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.top-timer-floating {
    position: absolute; top: 15px; left: 50%; transform: translateX(-50%);
    width: 60px; height: 60px;
    background: rgba(0,0,0,0.8); border: 2px solid #ffd700;
    border-radius: 50%; z-index: 50;
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 0 15px #ffd700;
}
.top-timer-floating.urgent { border-color: #ff4444; color: #ff4444; box-shadow: 0 0 20px #ff4444; animation: pulse 0.5s infinite; }
.timer-text { font-size: 1.8rem; font-weight: bold; color: #fff; }

/* --- SCENE --- */
.scene-stage { flex: 1; position: relative; }
.fighter { position: absolute; width: 300px; display: flex; flex-direction: column; align-items: center; }
.enemy { top: 8%; right: 8%; align-items: flex-end; }
.player { bottom: 5%; left: 8%; align-items: flex-start; }
.sprite-box { width: 180px; height: 180px; position: relative; margin: 10px; }
.char-img { width: 100%; height: 100%; object-fit: contain; filter: drop-shadow(0 5px 10px rgba(0,0,0,0.5)); transition: transform 0.2s; }
.enemy .char-img { transform: scaleX(-1); }
.shadow { position: absolute; bottom: 0; width: 100%; height: 20px; background: rgba(0,0,0,0.3); border-radius: 50%; transform: scaleY(0.4); }
.hud { background: rgba(0,0,0,0.6); border: 1px solid #555; padding: 5px 10px; border-radius: 4px; width: 220px; color: #fff; backdrop-filter: blur(4px); }
.hp-bar-bg { width: 100%; height: 8px; background: #333; border: 1px solid #000; margin: 3px 0; }
.hp-fill { height: 100%; background: linear-gradient(90deg, #d32f2f, #f44336); transition: width 0.3s; }
.hp-text { text-align: right; font-size: 0.8rem; color: #bbb; }
.clash-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.6); z-index: 40; display: flex; align-items: center; justify-content: center; gap: 40px; }
.move-icon { width: 100px; height: 100px; border-radius: 50%; background: #fff; color: #000; display: flex; align-items: center; justify-content: center; font-size: 3rem; border: 5px solid #ccc; }
.vs-spark { font-size: 4rem; animation: sparkPop 0.2s; }
.result-text { position: absolute; bottom: 25%; font-size: 3rem; font-weight: bold; color: #ffd700; text-shadow: 0 0 20px #d32f2f; animation: fadeInUp 0.5s; }

/* --- CONSOLE --- */
.bottom-console-split {
    height: 240px; background: #080808; border-top: 2px solid #b8860b;
    display: flex; z-index: 50; position: relative;
}
.left-chat-column { 
    flex: 6; display: flex; flex-direction: column; 
    border-right: 1px solid #333; background: #111;
}
.panel-header { background: #222; color: #b8860b; font-size: 0.8rem; padding: 6px 10px; text-transform: uppercase; border-bottom: 1px solid #333; font-weight: bold; letter-spacing: 1px; }
.chat-display { flex: 1; padding: 10px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.chat-bubble { font-size: 0.9rem; background: #222; padding: 5px 10px; border-radius: 4px; align-self: flex-start; max-width: 90%; color: #ddd; word-wrap: break-word; }
.chat-bubble.me { align-self: flex-end; background: #004d40; color: #a7ffeb; }
.chat-bubble .sender { color: #b8860b; font-weight: bold; margin-right: 5px; font-size: 0.8rem; }
.chat-input-row { display: flex; border-top: 1px solid #333; padding: 8px; background: #000; }
.chat-input-row input { flex: 1; background: #222; border: 1px solid #444; color: #fff; padding: 8px 10px; outline: none; border-radius: 4px; }
.chat-input-row button { background: #b8860b; border: none; color: #000; cursor: pointer; padding: 0 15px; margin-left: 8px; border-radius: 4px; font-weight: bold; }
.chat-input-row button:hover { background: #fff; }

.right-action-column { 
    flex: 4; padding: 10px; background: #0a0a0a; 
    display: flex; align-items: center; justify-content: center;
}
.square-grid-actions {
    display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; 
    gap: 10px; width: 100%; height: 100%;
}
.btn-disabled { opacity: 0.5; pointer-events: none; filter: grayscale(1); }

.square-btn {
    background: #222; border: 1px solid #444; color: #eee; cursor: pointer;
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    border-radius: 6px; transition: all 0.2s; font-size: 0.9rem; font-weight: bold;
}
.square-btn i { font-size: 2rem; margin-bottom: 5px; }
.square-btn.rock { background: linear-gradient(135deg, #c62828, #b71c1c); border-color: #ef5350; }
.square-btn.paper { background: linear-gradient(135deg, #f9a825, #f57f17); border-color: #fdd835; color: #000; }
.square-btn.scissor { background: linear-gradient(135deg, #2e7d32, #1b5e20); border-color: #66bb6a; }
.square-btn.surrender { background: linear-gradient(135deg, #1565c0, #0d47a1); border-color: #42a5f5; }
.square-btn:hover { transform: translateY(-2px); filter: brightness(1.2); box-shadow: 0 5px 15px rgba(0,0,0,0.5); }
.waiting-square { color: #888; font-size: 1.2rem; display: flex; align-items: center; justify-content: center; height: 100%; width: 100%; background: #111; border-radius: 8px; border: 1px dashed #444; flex-direction: column; }
.spinner-box { text-align: center; }
.spinner-box i { font-size: 2rem; margin-bottom: 10px; color: #b8860b; }
.mini-exit-btn { margin-top: 10px; background: #333; color: #fff; border: 1px solid #555; padding: 5px 15px; cursor: pointer; }

/* ANIMATIONS */
.anim-clash { animation: bounce 1s infinite; border-color: #888; }
.anim-smash-win { animation: smash 0.5s forwards; border-color: #ffd700; box-shadow: 0 0 30px #ffd700; z-index: 10; }
.anim-cut-win { animation: slice 0.5s forwards; border-color: #28a745; box-shadow: 0 0 30px #28a745; z-index: 10; }
.anim-lose { filter: grayscale(1) brightness(0.5); transform: scale(0.8); }

@keyframes smash { 0% {transform: scale(1);} 50% {transform: scale(1.5) translateY(-50px);} 100% {transform: scale(1) translateY(0);} }
@keyframes slice { 0% {transform: translateX(0);} 50% {transform: translateX(50px) rotate(45deg);} 100% {transform: translateX(0);} }
@keyframes sparkPop { 0% {transform: scale(0);} 100% {transform: scale(1.5); opacity: 0;} }
@keyframes shake { 0%, 100% {transform: translateX(0);} 25% {transform: translateX(-5px);} 75% {transform: translateX(5px);} }
@keyframes pulse { 0% {transform: scale(1);} 50% {transform: scale(1.1);} 100% {transform: scale(1);} }
@keyframes slideUp { from {transform: translateY(20px); opacity: 0;} to {transform: translateY(0); opacity: 1;} }
@keyframes fadeInUp { from {transform: translateY(20px); opacity: 0;} to {transform: translateY(0); opacity: 1;} }
</style>