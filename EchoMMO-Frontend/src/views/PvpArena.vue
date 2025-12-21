<template>
  <div class="wuxia-battle-container">
    
    <div v-if="gameState !== 'BATTLE'" class="lobby-overlay">
        <div class="lobby-content">
            <h1 class="wuxia-title">VÕ ĐÀI TRANH HÙNG</h1>
            
            <div class="hero-card">
                <div class="avatar-frame gold-border">
                    <img :src="mySprite" class="pixel-art" />
                </div>
                <div class="hero-info">
                    <h2>{{ characterName }}</h2>
                    <div class="stats-row">
                        <span class="stat-badge level">Cấp {{ characterLevel }}</span>
                        <span class="stat-badge wins">🏆 Thắng: {{ characterWins }}</span>
                        <button class="refresh-btn" @click="manualRefresh"><i class="fas fa-sync-alt"></i></button>
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
                <h3>⚔️ ĐỐI THỦ ĐÃ XUẤT HIỆN ⚔️</h3>
                <div class="enemy-preview">
                    <div class="preview-avatar-box">
                        <img :src="enemySprite" class="pixel-art" />
                    </div>
                    <p>{{ enemyName }} (Lv.{{ enemyLevel }})</p>
                </div>
                <div class="popup-actions">
                    <button class="btn-decline" @click="declineMatch">BỎ QUA</button>
                    <button class="btn-accept" @click="acceptMatch" :disabled="hasAccepted" :class="{'accepted': hasAccepted}">
                        {{ hasAccepted ? 'ĐANG CHỜ ĐỐI THỦ...' : 'CHẤP NHẬN CHIẾN ĐẤU' }} 
                        <span v-if="!hasAccepted">({{ acceptTimer }}s)</span>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <div v-else class="battle-arena">
        
        <div class="log-hint" @click="showLog = !showLog"><i class="fas fa-book"></i> [L] NHẬT KÝ</div>
        <transition name="fade">
            <div v-if="showLog" class="floating-log-panel custom-scroll">
                <div class="log-header">DIỄN BIẾN TRẬN ĐẤU <span @click="showLog=false" style="float:right;cursor:pointer">x</span></div>
                <div class="log-content">
                    <div v-for="(log, i) in battleLogs.slice().reverse()" :key="i" class="log-line">» {{ log }}</div>
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
                    <img :src="enemySprite" class="char-img pixel-art" :class="{'shake-hit': isEnemyHit}" />
                </div>
            </div>

            <div class="fighter player">
                <div class="sprite-box">
                    <img :src="mySprite" class="char-img pixel-art" :class="{'shake-hit': isMyHit}" />
                </div>
                <div class="hud player-hud">
                    <div class="name">{{ characterName }} <small>Lv.{{ characterLevel }}</small></div>
                    <div class="hp-bar-bg"><div class="hp-fill" :style="{width: percent(myHp, myMaxHp)+'%'}"></div></div>
                    <div class="hp-text">{{ myHp }}/{{ myMaxHp }}</div>
                </div>
            </div>

            <div v-if="isRevealing" class="clash-overlay">
                <div class="move-icon p1" :class="getAnimClass(displayedMyMove, displayedEnemyMove, true)">
                    <i :class="getRpsIcon(displayedMyMove)"></i>
                </div>
                <div class="vs-spark">💥</div>
                <div class="move-icon p2" :class="getAnimClass(displayedMyMove, displayedEnemyMove, false)">
                    <i :class="getRpsIcon(displayedEnemyMove)"></i>
                </div>
                <div class="result-text">{{ lastResultText }}</div>
            </div>
        </div>

        <div class="bottom-console-split">
            <div class="left-chat-column">
                <div class="chat-display custom-scroll" ref="chatBoxRef">
                      <div v-for="(msg, i) in matchMessages" :key="i" class="chat-bubble" :class="{'me': msg.isMe}">
                        <span class="sender">{{ msg.sender }}:</span> {{ msg.text }}
                    </div>
                </div>
                <div class="chat-input-row">
                    <input v-model="chatInput" @keyup.enter="sendPrivateChat" placeholder="Chat..." />
                    <button @click="sendPrivateChat"><i class="fas fa-paper-plane"></i></button>
                </div>
            </div>

            <div class="right-action-column">
                <div v-if="battlePhase === 'RPS_WAIT' && !isRevealing" class="square-grid-actions">
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
                        <i class="fas fa-flag"></i><span>THOÁT</span>
                    </button>
                </div>
                
                <div v-else class="waiting-square">
                    <div class="spinner-box">
                        <i class="fas fa-yin-yang fa-spin"></i>
                        <p v-if="isRevealing">ĐANG XỬ LÝ KẾT QUẢ...</p>
                        <p v-else>CHỜ ĐỐI THỦ RA CHIÊU...</p>
                        <button class="emergency-exit" @click="handleSurrender">THOÁT NGAY</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useAuthStore } from '../stores/authStore';
import { useCharacterStore } from '../stores/characterStore';
import { CHARACTER_SKINS } from '../utils/assetHelper'; 
import axios from 'axios';

const API_URL = "http://localhost:8080/api/pvp";
const authStore = useAuthStore();
const charStore = useCharacterStore(); 

const getSpriteUrl = (skinId) => {
    const skin = CHARACTER_SKINS[skinId] || CHARACTER_SKINS['skin_yasou'];
    return skin.sprites.idle;
};

// Data
const characterName = computed(() => authStore.character?.name || authStore.user?.username || 'Đại Hiệp');
const characterLevel = computed(() => authStore.character?.level || 1);
const characterWins = computed(() => authStore.character?.pvpWins || 0);
const mySprite = computed(() => getSpriteUrl(authStore.user?.avatarUrl));
const enemySprite = computed(() => getSpriteUrl(enemySkinId.value));

const gameState = ref('LOBBY'); 
const matchId = ref(null);
const battlePhase = ref('RPS_WAIT'); 

const turnTimer = ref(30);
const lastResultText = ref('');
const hasAccepted = ref(false); 
const showLog = ref(false); 
const isActionPending = ref(false); 
const searchTimer = ref(0);
const acceptTimer = ref(10);
const ignoredMatchIds = ref(new Set()); 

// Enemy Data
const enemyName = ref("Đối thủ"); 
const enemyLevel = ref(1); 
const enemySkinId = ref(null);

const myHp = ref(100); const myMaxHp = ref(100); 
const enemyHp = ref(100); const enemyMaxHp = ref(100); 
const isMyHit = ref(false); const isEnemyHit = ref(false);
const battleLogs = ref([]); const matchMessages = ref([]); const chatInput = ref('');
const chatBoxRef = ref(null);

const displayedMyMove = ref(null);
const displayedEnemyMove = ref(null); 
const lastProcessedTurn = ref(-1);
const isRevealing = ref(false);

let pollId, searchInterval, acceptInterval, turnTimerInterval, revealTimeout;

const getHeaders = () => ({ 'Authorization': `Bearer ${authStore.token}` });

// Kiểm tra ID chuẩn xác
const checkIsPlayer1 = (data) => {
    const char = authStore.character;
    const myCharId = Number(char?.charId || char?.id || char?.characterId || 0);
    const p1Id = Number(data.p1Id);
    return myCharId === p1Id;
};

// Đồng bộ thông tin cơ bản
const syncBasicInfo = (data) => {
    const isP1 = checkIsPlayer1(data);
    enemyName.value = (isP1 ? data.p2Name : data.p1Name) || "Đối thủ";
    enemyLevel.value = (isP1 ? data.p2Level : data.p1Level) || 1;
    enemySkinId.value = isP1 ? data.p2AvatarUrl : data.p1AvatarUrl;
};

const resetToLobby = () => {
    console.log("Resetting to Lobby...");
    gameState.value = 'LOBBY';
    matchId.value = null;
    clearAllIntervals();
    battleLogs.value = [];
    matchMessages.value = [];
    battlePhase.value = 'RPS_WAIT';
    isActionPending.value = false;
    isRevealing.value = false;
    lastProcessedTurn.value = -1;
    hasAccepted.value = false;
    showLog.value = false;
};

const clearAllIntervals = () => {
    if(searchInterval) clearInterval(searchInterval);
    if(acceptInterval) clearInterval(acceptInterval);
    if(turnTimerInterval) clearInterval(turnTimerInterval);
    if(pollId) clearInterval(pollId); pollId = null;
    if(revealTimeout) clearTimeout(revealTimeout);
};

const manualRefresh = async () => { await authStore.fetchCharacter(); };

// --- ACTIONS ---
const toggleSearch = async () => {
  resetToLobby();
  gameState.value = 'SEARCHING'; 
  searchTimer.value = 0;
  searchInterval = setInterval(() => searchTimer.value++, 1000);
  try { 
      await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() }); 
      startPolling(); 
  } catch(e) { resetToLobby(); }
};

const cancelSearch = async () => { 
    try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch(e){} 
    resetToLobby(); 
};

const acceptMatch = async () => {
  if(!matchId.value) return;
  hasAccepted.value = true;
  try { await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) {}
};

const declineMatch = async () => { cancelSearch(); };

const submitRps = async (move) => {
  if (isActionPending.value || battlePhase.value !== 'RPS_WAIT') return; 
  isActionPending.value = true; 
  battlePhase.value = 'RPS_PENDING'; 
  try { await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() }); } 
  catch (e) { isActionPending.value = false; }
};

const handleSurrender = async () => {
    if(!confirm("Bạn có chắc chắn muốn thoát? (Xử thua)")) return;
    
    // Ngắt polling ngay lập tức
    if(matchId.value) ignoredMatchIds.value.add(Number(matchId.value));
    const mid = matchId.value;
    
    // Reset UI trước
    resetToLobby(); 
    
    // Gửi request sau
    try { await axios.post(`${API_URL}/surrender`, { matchId: mid }, { headers: getHeaders() }); } catch(e) {}
    
    setTimeout(() => authStore.fetchCharacter(), 1000);
};

const startTurnTimer = () => {
  if(turnTimerInterval) clearInterval(turnTimerInterval);
  turnTimer.value = 30;
  turnTimerInterval = setInterval(() => {
    if (turnTimer.value > 0) turnTimer.value--;
    if (turnTimer.value <= 0) {
       if(battlePhase.value === 'RPS_WAIT') {
           const moves = ['ROCK', 'PAPER', 'SCISSORS'];
           submitRps(moves[Math.floor(Math.random() * moves.length)]);
       }
    }
  }, 1000);
};

// --- SYNC ---
const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data; 
      if (!data) return;

      if (data.matchId && ignoredMatchIds.value.has(Number(data.matchId))) return;

      // 1. MATCH FOUND
      if (data.status === 'PENDING') {
         if (gameState.value !== 'MATCH_FOUND' && gameState.value !== 'BATTLE') {
             gameState.value = 'MATCH_FOUND';
             if(searchInterval) clearInterval(searchInterval);
             matchId.value = data.matchId;
             
             syncBasicInfo(data); 

             if(!hasAccepted.value) {
                 acceptTimer.value = 10;
                 if(acceptInterval) clearInterval(acceptInterval);
                 acceptInterval = setInterval(()=> { acceptTimer.value--; if(acceptTimer.value<=0) declineMatch(); }, 1000);
             }
         }
      } 
      // 2. ACTIVE
      else if (data.status === 'ACTIVE') {
         if (gameState.value !== 'BATTLE') {
            gameState.value = 'BATTLE'; 
            matchId.value = data.matchId;
            if(acceptInterval) clearInterval(acceptInterval);
            startTurnTimer();
         }
         syncBattleData(data); 
      } 
      // 3. FINISHED (XỬ LÝ KHI KẾT THÚC)
      else if (data.status === 'FINISHED' && gameState.value === 'BATTLE') {
         syncBattleData(data); // Sync lần cuối
         
         // Dừng Polling ngay
         if(pollId) clearInterval(pollId); pollId = null; 
         
         const char = authStore.character;
         const myCharId = Number(char?.charId || char?.id || char?.characterId || 0);
         const isWinner = (data.winnerId === myCharId);
         
         // Check log có chữ "đầu hàng" hay không (Do backend gửi về)
         const isSurrenderLog = data.lastLog && data.lastLog.toLowerCase().includes("đầu hàng");

         let msg = "";
         if (data.winnerId === null) {
             msg = "💀 LƯỠNG BẠI CÂU THƯƠNG! HÒA!";
         } else if (isWinner) {
             // [LOGIC MỚI]: Nếu thắng do đối thủ đầu hàng
             if (isSurrenderLog) {
                 msg = "🏳️ ĐỐI THỦ ĐÃ ĐẦU HÀNG! BẠN CHIẾN THẮNG!";
             } else {
                 msg = "🏆 CHIẾN THẮNG! KỸ NĂNG TUYỆT ĐỈNH!";
             }
         } else {
             msg = "💀 THẤT BẠI! HÃY PHỤC THÙ SAU!";
         }
         
         // Hiện thông báo, bấm OK xong mới thoát
         setTimeout(() => { 
             alert(msg); 
             resetToLobby(); 
         }, 500); 
      }
    } catch (e) {}
  }, 1000);
};

const syncBattleData = (data) => {
  syncBasicInfo(data);

  const isP1 = checkIsPlayer1(data);
  const newMyHp = isP1 ? data.p1Hp : data.p2Hp;
  const newEnemyHp = isP1 ? data.p2Hp : data.p1Hp;
  
  if(newMyHp < myHp.value) { isMyHit.value = true; setTimeout(()=>isMyHit.value=false, 300); }
  if(newEnemyHp < enemyHp.value) { isEnemyHit.value = true; setTimeout(()=>isEnemyHit.value=false, 300); }
  
  myHp.value = newMyHp; myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyHp.value = newEnemyHp; enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;

  if(data.messages && data.messages.length > matchMessages.value.length) {
    data.messages.slice(matchMessages.value.length).forEach(m => matchMessages.value.push({ 
       sender: m.senderName, text: m.content, isMe: (checkIsPlayer1(data) ? data.p1Id : data.p2Id) == m.senderId 
    }));
    nextTick(() => { if(chatBoxRef.value) chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight; });
  }

  if (isRevealing.value) return; 

  const currentTurn = data.turnCount;
  const isNewTurnProcessed = currentTurn > lastProcessedTurn.value;
  const bothMoved = data.lastP1Move && data.lastP2Move;

  if (bothMoved && isNewTurnProcessed) {
      lastProcessedTurn.value = currentTurn;
      isActionPending.value = false;

      const p1M = data.lastP1Move; const p2M = data.lastP2Move;
      if (isP1) { displayedMyMove.value = p1M; displayedEnemyMove.value = p2M; } 
      else { displayedMyMove.value = p2M; displayedEnemyMove.value = p1M; }

      if (data.lastLog) {
          battleLogs.value.push(data.lastLog);
          if (data.lastLog.includes("thắng")) lastResultText.value = "THẮNG";
          else if (data.lastLog.includes("thua")) lastResultText.value = "THUA";
          else lastResultText.value = "XUNG KHẮC";
      }

      isRevealing.value = true;
      if (revealTimeout) clearTimeout(revealTimeout);
      revealTimeout = setTimeout(() => {
          isRevealing.value = false;
          battlePhase.value = 'RPS_WAIT';
          startTurnTimer(); 
      }, 3000);

  } else {
      const myMove = isP1 ? data.p1Move : data.p2Move;
      if (!myMove) {
          if (battlePhase.value !== 'RPS_WAIT') {
              battlePhase.value = 'RPS_WAIT';
              isActionPending.value = false;
          }
      } else {
          if (battlePhase.value !== 'RPS_PENDING') battlePhase.value = 'RPS_PENDING';
      }
  }
};

const sendPrivateChat = async () => {
    if(!chatInput.value.trim()) return;
    try { await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); } catch(e){}
    chatInput.value = '';
};

const handleKeydown = (e) => {
    if(e.key.toLowerCase() === 'l') showLog.value = !showLog.value;
};

const percent = (c, m) => (m>0 ? (c/m)*100 : 0);
const getRpsIcon = (m) => ({ 'ROCK': 'fas fa-hand-rock', 'PAPER': 'fas fa-hand-paper', 'SCISSORS': 'fas fa-hand-scissors' }[m] || 'fas fa-question');
const getAnimClass = (myMove, enemyMove, isMe) => {
    if (!myMove || !enemyMove) return '';
    let iWin = false; let tie = false;
    if (myMove === enemyMove) tie = true;
    else if ((myMove === 'ROCK' && enemyMove === 'SCISSORS') || (myMove === 'SCISSORS' && enemyMove === 'PAPER') || (myMove === 'PAPER' && enemyMove === 'ROCK')) iWin = true;
    if (tie) return 'anim-clash'; 
    if (isMe) return iWin ? 'anim-smash-win' : 'anim-lose'; 
    else return !iWin ? 'anim-smash-win' : 'anim-lose';
};

onMounted(async () => {
    await authStore.fetchCharacter();
    window.addEventListener('keydown', handleKeydown);
    startPolling();
});

onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown);
    clearAllIntervals();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Patrick+Hand&family=Roboto+Condensed:wght@700&display=swap');

.pixel-art { image-rendering: pixelated; }

.wuxia-battle-container { width: 100%; height: 100%; min-height: 600px; background: #1a1a1a; font-family: 'Roboto Condensed', sans-serif; color: #e0e0e0; position: relative; overflow: hidden; display: flex; flex-direction: column; }
.lobby-overlay { flex: 1; background: url('https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1920&auto=format&fit=crop') center/cover; display: flex; align-items: center; justify-content: center; }
.lobby-content { background: rgba(0,0,0,0.9); border: 2px solid #b8860b; padding: 40px; border-radius: 10px; text-align: center; width: 450px; box-shadow: 0 0 50px #000; position: relative; }
.wuxia-title { font-size: 2.5rem; color: #ffd700; margin-bottom: 20px; text-shadow: 0 0 10px #b8860b; }
.avatar-frame { width: 100px; height: 100px; border-radius: 50%; border: 3px solid #ffd700; overflow: hidden; margin: 0 auto; background: #000; display: flex; justify-content: center; align-items: center; }
.avatar-frame img { width: 80%; height: 80%; object-fit: contain; }
.hero-info h2 { color: #fff; margin: 10px 0; }
.stats-row { display: flex; justify-content: center; gap: 10px; }
.stat-badge { background: #222; border: 1px solid #555; padding: 4px 12px; border-radius: 4px; color: #ccc; font-weight: bold; }
.refresh-btn { background: none; border: none; color: #b8860b; cursor: pointer; } 
.action-area { margin-top: 25px; }
.btn-start { background: #b8860b; color: #000; padding: 15px 30px; border: none; cursor: pointer; width: 100%; font-weight: bold; font-size: 1.2rem; border-radius: 4px; transition: 0.3s; }
.btn-start:hover { background: #ffd700; box-shadow: 0 0 20px #b8860b; }
.btn-cancel { background: #333; color: #fff; padding: 15px; width: 100%; border: 1px solid #555; border-radius: 4px; cursor: pointer; }
.match-popup { margin-top: 20px; background: #222; padding: 20px; border-radius: 8px; border: 2px solid #28a745; animation: slideUp 0.3s; }
.match-popup h3 { color: #28a745; margin-top: 0; }
.preview-avatar-box { width: 80px; height: 80px; background: #000; border: 2px solid #fff; border-radius: 50%; margin: 0 auto; overflow: hidden; display: flex; justify-content: center; align-items: center; }
.preview-avatar-box img { width: 80%; height: 80%; object-fit: contain; }
.popup-actions { display: flex; gap: 10px; margin-top: 15px; }
.btn-decline { flex: 1; background: #333; color: #dc3545; border: 1px solid #dc3545; padding: 10px; cursor: pointer; font-weight: bold; }
.btn-accept { flex: 2; background: #28a745; color: #fff; border: none; padding: 10px; cursor: pointer; font-weight: bold; animation: pulse 1s infinite; }
.btn-accept.accepted { background: #155724; animation: none; cursor: default; }
.battle-arena { display: flex; flex-direction: column; height: 100%; background: url('https://images.unsplash.com/photo-1535581652167-3d6b98c365b2?q=80&w=1920&auto=format&fit=crop') center/cover; position: relative; }
.log-hint { position: absolute; top: 15px; left: 15px; z-index: 60; background: rgba(0,0,0,0.6); color: #ffd700; padding: 8px 15px; border-radius: 20px; border: 1px solid #b8860b; cursor: pointer; }
.floating-log-panel { position: absolute; top: 60px; left: 15px; z-index: 60; width: 350px; max-height: 250px; background: rgba(0,0,0,0.9); border: 1px solid #ffd700; padding: 10px; overflow-y: auto; font-family: monospace; display: flex; flex-direction: column; }
.log-header { font-weight: bold; color: #ffd700; border-bottom: 1px solid #444; padding-bottom: 5px; margin-bottom: 5px; }
.log-line { margin-bottom: 5px; color: #ccc; border-bottom: 1px solid #333; padding-bottom: 2px; }
.top-timer-floating { position: absolute; top: 15px; left: 50%; transform: translateX(-50%); width: 60px; height: 60px; background: rgba(0,0,0,0.8); border: 3px solid #ffd700; border-radius: 50%; z-index: 50; display: flex; align-items: center; justify-content: center; font-size: 1.8rem; font-weight: bold; color: #fff; }
.top-timer-floating.urgent { border-color: #ff4444; color: #ff4444; animation: pulse 0.5s infinite; }
.scene-stage { flex: 1; position: relative; }
.fighter { position: absolute; width: 300px; display: flex; flex-direction: column; }
.fighter.enemy { top: 15%; right: 15%; align-items: flex-end; }
.fighter.player { bottom: 15%; left: 15%; align-items: flex-start; }
.sprite-box { width: 180px; height: 180px; display: flex; justify-content: center; align-items: center; }
.char-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); filter: drop-shadow(0 5px 10px rgba(0,0,0,0.5)); transition: 0.2s; }
.enemy .char-img { transform: scale(1.5) scaleX(-1); }
.hud { background: rgba(0,0,0,0.7); border: 1px solid #555; padding: 5px 10px; border-radius: 4px; width: 220px; color: #fff; }
.hp-bar-bg { width: 100%; height: 8px; background: #333; border: 1px solid #000; margin: 3px 0; }
.hp-fill { height: 100%; background: linear-gradient(90deg, #d32f2f, #f44336); transition: width 0.3s; }
.clash-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.7); z-index: 40; display: flex; align-items: center; justify-content: center; gap: 40px; }
.move-icon { width: 100px; height: 100px; border-radius: 50%; background: #fff; color: #000; display: flex; align-items: center; justify-content: center; font-size: 3rem; border: 5px solid #ccc; transition: all 0.5s; }
.vs-spark { font-size: 4rem; animation: sparkPop 0.2s; }
.result-text { position: absolute; bottom: 25%; font-size: 3rem; font-weight: bold; color: #ffd700; text-shadow: 0 0 20px #d32f2f; animation: fadeInUp 0.5s; }
.bottom-console-split { height: 240px; background: #111; border-top: 2px solid #b8860b; display: flex; z-index: 50; position: relative; }
.left-chat-column { flex: 6; display: flex; flex-direction: column; border-right: 1px solid #333; background: #000; }
.chat-display { flex: 1; padding: 10px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.chat-bubble { font-size: 0.9rem; background: #222; padding: 5px 10px; border-radius: 4px; align-self: flex-start; max-width: 90%; color: #ddd; }
.chat-bubble.me { align-self: flex-end; background: #004d40; color: #a7ffeb; }
.chat-input-row { display: flex; padding: 8px; background: #111; border-top: 1px solid #333; }
.chat-input-row input { flex: 1; background: #222; border: 1px solid #444; color: #fff; padding: 8px; }
.chat-input-row button { background: #b8860b; border: none; padding: 0 15px; margin-left: 5px; cursor: pointer; }
.right-action-column { flex: 4; padding: 10px; background: #0a0a0a; display: flex; align-items: center; justify-content: center; }
.square-grid-actions { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 10px; width: 100%; height: 100%; }
.square-btn { background: #222; border: 1px solid #444; color: #eee; cursor: pointer; display: flex; flex-direction: column; align-items: center; justify-content: center; font-weight: bold; font-size: 0.9rem; }
.square-btn i { font-size: 2rem; margin-bottom: 5px; }
.square-btn.rock { border-color: #ef5350; color: #ef5350; }
.square-btn.paper { border-color: #fdd835; color: #fdd835; }
.square-btn.scissor { border-color: #66bb6a; color: #66bb6a; }
.square-btn:hover:not(:disabled) { background: #333; transform: scale(1.05); }
.square-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.waiting-square { color: #888; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; width: 100%; background: #111; border: 1px dashed #444; }
.spinner-box i { font-size: 2rem; margin-bottom: 10px; color: #b8860b; }
.emergency-exit { margin-top: 10px; background: #dc3545; color: #fff; border: 1px solid #ff4444; padding: 5px 15px; cursor: pointer; font-weight: bold; font-size: 0.8rem; }

@keyframes pulse { 0% {transform: scale(1);} 50% {transform: scale(1.05);} 100% {transform: scale(1);} }
@keyframes slideUp { from {transform: translateY(20px); opacity: 0;} to {transform: translateY(0); opacity: 1;} }
@keyframes sparkPop { 0% {transform: scale(0);} 100% {transform: scale(1.5); opacity: 0;} }
@keyframes fadeInUp { from {transform: translateY(20px); opacity: 0;} to {transform: translateY(0); opacity: 1;} }
@keyframes smash { 0% {transform: scale(1);} 50% {transform: scale(1.5) translateY(-50px);} 100% {transform: scale(1) translateY(0);} }
@keyframes slice { 0% {transform: translateX(0);} 50% {transform: translateX(50px) rotate(45deg);} 100% {transform: translateX(0);} }
.anim-smash-win { animation: smash 0.5s forwards; border-color: #ffd700; box-shadow: 0 0 30px #ffd700; z-index: 10; }
.anim-cut-win { animation: slice 0.5s forwards; border-color: #28a745; box-shadow: 0 0 30px #28a745; z-index: 10; }
.anim-lose { filter: grayscale(1) brightness(0.5); transform: scale(0.8); }
.anim-clash { animation: shake 0.5s infinite; border-color: #ff4444 !important; filter: sepia(1) hue-rotate(-50deg) saturate(3); }
.shake-hit { animation: shake 0.3s cubic-bezier(.36,.07,.19,.97) both; filter: sepia(1) hue-rotate(-50deg) saturate(3); }
@keyframes shake { 10%, 90% { transform: translate3d(-1px, 0, 0); } 20%, 80% { transform: translate3d(2px, 0, 0); } 30%, 50%, 70% { transform: translate3d(-4px, 0, 0); } 40%, 60% { transform: translate3d(4px, 0, 0); } }
</style>