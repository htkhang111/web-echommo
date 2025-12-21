<!-- <template>
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
                        {{ hasAccepted ? 'ĐANG CHỜ ĐỐI THỦ...' : 'CHẤP NHẬN' }} 
                        <span v-if="!hasAccepted">({{ acceptTimer }}s)</span>
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
                <div class="log-header">DIỄN BIẾN <span @click="showLog=false" class="close-log">×</span></div>
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
                </div>
                <div class="sprite-box">
                    <div class="shadow"></div>
                    <img :src="enemySprite" class="char-img pixel-art" :class="{'shake-hit': isEnemyHit}" />
                </div>
            </div>

            <div class="fighter player">
                <div class="sprite-box">
                    <div class="shadow"></div>
                    <img :src="mySprite" class="char-img pixel-art" :class="{'shake-hit': isMyHit}" />
                </div>
                <div class="hud player-hud">
                    <div class="name">{{ characterName }} <small>Lv.{{ characterLevel }}</small></div>
                    <div class="hp-bar-bg"><div class="hp-fill" :style="{width: percent(myHp, myMaxHp)+'%'}"></div></div>
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
                    <button class="square-btn rock" @click="submitRps('ROCK')" :disabled="isActionPending"><i class="fas fa-hand-rock"></i><span>BÚA</span></button>
                    <button class="square-btn paper" @click="submitRps('PAPER')" :disabled="isActionPending"><i class="fas fa-hand-paper"></i><span>BAO</span></button>
                    <button class="square-btn scissor" @click="submitRps('SCISSORS')" :disabled="isActionPending"><i class="fas fa-hand-scissors"></i><span>KÉO</span></button>
                    <button class="square-btn surrender" @click="handleSurrender"><i class="fas fa-flag"></i><span>THOÁT</span></button>
                </div>
                <div v-else class="waiting-square">
                    <div class="spinner-box">
                        <i class="fas fa-yin-yang fa-spin"></i>
                        <p v-if="isRevealing">ĐANG XỬ LÝ...</p>
                        <p v-else>CHỜ ĐỐI THỦ...</p>
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

// --- RESOURCES ---
const getSpriteUrl = (skinId) => {
    const skin = CHARACTER_SKINS[skinId] || CHARACTER_SKINS['skin_yasou'];
    return skin.sprites.idle;
};

// --- DATA COMPUTED ---
const characterName = computed(() => authStore.character?.name || authStore.user?.username || 'Đại Hiệp');
const characterLevel = computed(() => authStore.character?.level || 1);
const characterWins = computed(() => authStore.character?.pvpWins || 0);
const mySprite = computed(() => getSpriteUrl(authStore.user?.avatarUrl));
const enemySprite = computed(() => getSpriteUrl(enemySkinId.value));

// --- STATE ---
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

// Battle Data
const enemyName = ref("Đối thủ"); const enemyLevel = ref(1); const enemySkinId = ref(null);
const myHp = ref(100); const myMaxHp = ref(100); 
const enemyHp = ref(100); const enemyMaxHp = ref(100); 
const isMyHit = ref(false); const isEnemyHit = ref(false);
const battleLogs = ref([]); const matchMessages = ref([]); 
const chatInput = ref(''); const chatBoxRef = ref(null);
const displayedMyMove = ref(null); const displayedEnemyMove = ref(null); 
const lastProcessedTurn = ref(-1);
const isRevealing = ref(false);

let pollId, searchInterval, acceptInterval, turnTimerInterval, revealTimeout;
const getHeaders = () => ({ 'Authorization': `Bearer ${authStore.token}` });

// --- UTILS ---
const percent = (c, m) => { if (!m || m <= 0) return 0; return Math.max(0, Math.min(100, (c/m)*100)); };

const checkIsPlayer1 = (data) => {
    const myCharId = Number(authStore.character?.charId || authStore.character?.id || 0);
    return myCharId === Number(data.p1Id);
};

const syncBasicInfo = (data) => {
    const isP1 = checkIsPlayer1(data);
    enemyName.value = (isP1 ? data.p2Name : data.p1Name) || "Đối thủ";
    enemyLevel.value = (isP1 ? data.p2Level : data.p1Level) || 1;
    enemySkinId.value = isP1 ? data.p2AvatarUrl : data.p1AvatarUrl;
};

const resetToLobby = () => {
    gameState.value = 'LOBBY'; matchId.value = null;
    clearAllIntervals();
    battleLogs.value = []; matchMessages.value = [];
    battlePhase.value = 'RPS_WAIT';
    isActionPending.value = false; isRevealing.value = false; lastProcessedTurn.value = -1;
    hasAccepted.value = false;
};

const clearAllIntervals = () => {
    if(searchInterval) clearInterval(searchInterval);
    if(acceptInterval) clearInterval(acceptInterval);
    if(turnTimerInterval) clearInterval(turnTimerInterval);
    if(pollId) { clearInterval(pollId); pollId = null; }
    if(revealTimeout) clearTimeout(revealTimeout);
};

// --- ACTIONS ---
const toggleSearch = async () => {
  resetToLobby(); gameState.value = 'SEARCHING'; 
  searchTimer.value = 0; searchInterval = setInterval(() => searchTimer.value++, 1000);
  try { await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() }); startPolling(); } catch(e) { resetToLobby(); }
};

const cancelSearch = async () => { try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch(e){} resetToLobby(); };
const acceptMatch = async () => { if(!matchId.value) return; hasAccepted.value = true; try { await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) {} };
const declineMatch = async () => { cancelSearch(); };

const submitRps = async (move) => {
  if (isActionPending.value || isRevealing.value) return; 
  isActionPending.value = true; battlePhase.value = 'RPS_PENDING'; 
  try { await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() }); } catch (e) { isActionPending.value = false; }
};

const handleSurrender = async () => {
    if(!confirm("Xác nhận thoát trận (Tính thua)?")) return;
    const mid = matchId.value;
    if(mid) ignoredMatchIds.value.add(Number(mid));
    resetToLobby(); 
    try { await axios.post(`${API_URL}/surrender`, { matchId: mid }, { headers: getHeaders() }); } catch(e) {}
    setTimeout(() => { authStore.fetchCharacter(); }, 500);
};

// --- SYNC ENGINE (FIX HIỂN THỊ THÔNG BÁO) ---
const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data; if (!data) return;
      if (data.matchId && ignoredMatchIds.value.has(Number(data.matchId))) return;

      // [1] TRẬN ĐẤU KẾT THÚC (QUAN TRỌNG NHẤT)
      if (data.status === 'FINISHED') {
         console.log("Match Finished detected");
         clearAllIntervals();
         
         const myCharId = Number(authStore.character?.charId || authStore.character?.id || 0);
         const serverWinnerId = data.winnerId !== null ? Number(data.winnerId) : null;
         const isWinner = (serverWinnerId === myCharId);
         
         const log = data.lastLog || "";
         const isSurrender = log.toLowerCase().includes("đầu hàng");

         syncBattleData(data); // Sync HP lần cuối

         let msg = "";
         if (serverWinnerId === null) {
             msg = "💀 LƯỠNG BẠI CÂU THƯƠNG! HÒA!";
         } else if (isWinner) {
             msg = isSurrender ? "🏳️ ĐỐI THỦ ĐÃ ĐẦU HÀNG! BẠN CHIẾN THẮNG!" : "🏆 CHIẾN THẮNG!";
         } else {
             msg = isSurrender ? "🏳️ BẠN ĐÃ ĐẦU HÀNG!" : "💀 THẤT BẠI!";
         }

         setTimeout(() => {
             alert(msg);
             resetToLobby();
             authStore.fetchCharacter();
         }, 200);
         return; 
      }

      // [2] PENDING
      if (data.status === 'PENDING') {
         if (gameState.value !== 'MATCH_FOUND' && gameState.value !== 'BATTLE') {
             gameState.value = 'MATCH_FOUND'; matchId.value = data.matchId;
             if(searchInterval) clearInterval(searchInterval);
             syncBasicInfo(data); 
             if(!hasAccepted.value) {
                 acceptTimer.value = 10;
                 if(acceptInterval) clearInterval(acceptInterval);
                 acceptInterval = setInterval(()=> { acceptTimer.value--; if(acceptTimer.value <= 0 && !hasAccepted.value) resetToLobby(); }, 1000);
             }
         }
      } 
      // [3] ACTIVE
      else if (data.status === 'ACTIVE') {
         if (gameState.value !== 'BATTLE') {
            gameState.value = 'BATTLE'; matchId.value = data.matchId;
            if(acceptInterval) clearInterval(acceptInterval);
            startTurnTimer();
         }
         syncBattleData(data); 
      } 
    } catch (e) { }
  }, 1000);
};

const syncBattleData = (data) => {
  syncBasicInfo(data);
  const isP1 = checkIsPlayer1(data);
  const nMyHp = isP1 ? data.p1Hp : data.p2Hp; const nEnHp = isP1 ? data.p2Hp : data.p1Hp;
  
  if(nMyHp < myHp.value) { isMyHit.value = true; setTimeout(()=>isMyHit.value=false, 300); }
  if(nEnHp < enemyHp.value) { isEnemyHit.value = true; setTimeout(()=>isEnemyHit.value=false, 300); }
  
  myHp.value = nMyHp; enemyHp.value = nEnHp;
  myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;

  if(data.messages && data.messages.length > matchMessages.value.length) {
    data.messages.slice(matchMessages.value.length).forEach(m => matchMessages.value.push({ 
       sender: m.senderName, text: m.content, isMe: (checkIsPlayer1(data) ? data.p1Id : data.p2Id) == m.senderId 
    }));
    nextTick(() => { if(chatBoxRef.value) chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight; });
  }

  if (isRevealing.value) return; 
  const currentTurn = data.turnCount;
  const bothMoved = data.lastP1Move && data.lastP2Move;

  if (bothMoved && currentTurn > lastProcessedTurn.value) {
      isRevealing.value = true; lastProcessedTurn.value = currentTurn; isActionPending.value = false;
      displayedMyMove.value = isP1 ? data.lastP1Move : data.lastP2Move;
      displayedEnemyMove.value = isP1 ? data.lastP2Move : data.lastP1Move;
      if (data.lastLog) {
          battleLogs.value.push(data.lastLog);
          lastResultText.value = data.lastLog.includes("thắng") ? "THẮNG" : (data.lastLog.includes("thua") ? "THUA" : "XUNG KHẮC");
      }
      battlePhase.value = 'RPS_REVEAL';
      revealTimeout = setTimeout(() => { isRevealing.value = false; battlePhase.value = 'RPS_WAIT'; turnTimer.value = 30; }, 3000);
  } else {
      const myM = isP1 ? data.p1Move : data.p2Move;
      battlePhase.value = myM ? 'RPS_PENDING' : 'RPS_WAIT';
  }
};

const startTurnTimer = () => {
  if(turnTimerInterval) clearInterval(turnTimerInterval);
  turnTimerInterval = setInterval(() => { if (turnTimer.value > 0) turnTimer.value--; }, 1000);
};

const manualRefresh = async () => { await authStore.fetchCharacter(); };
const sendPrivateChat = async () => {
    if(!chatInput.value.trim()) return;
    try { await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); } catch(e){}
    chatInput.value = '';
};

const getRpsIcon = (m) => ({ 'ROCK': 'fas fa-hand-rock', 'PAPER': 'fas fa-hand-paper', 'SCISSORS': 'fas fa-hand-scissors' }[m] || 'fas fa-question');

const getAnimClass = (myMove, enemyMove, isMe) => {
    if (!myMove || !enemyMove) return '';
    if (myMove === enemyMove) return 'anim-clash'; 
    let iWin = ((myMove === 'ROCK' && enemyMove === 'SCISSORS') || (myMove === 'SCISSORS' && enemyMove === 'PAPER') || (myMove === 'PAPER' && enemyMove === 'ROCK'));
    if (isMe) return iWin ? 'anim-smash-win' : 'anim-lose'; 
    else return !iWin ? 'anim-smash-win' : 'anim-lose';
};

onMounted(() => { 
    authStore.fetchCharacter(); 
    window.addEventListener('keydown', (e) => {
        if (document.activeElement.tagName !== 'INPUT' && e.key.toLowerCase() === 'l') {
            e.preventDefault();
            showLog.value = !showLog.value;
        }
    });
    startPolling(); 
});

onUnmounted(() => { 
    clearAllIntervals(); 
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Patrick+Hand&family=Roboto+Condensed:wght@700&display=swap');

.pixel-art { image-rendering: pixelated; }

/* LAYOUT */
.wuxia-battle-container { width: 100%; height: 100%; min-height: 600px; background: #1a1a1a; font-family: 'Roboto Condensed', sans-serif; color: #e0e0e0; position: relative; overflow: hidden; display: flex; flex-direction: column; }

/* LOBBY */
.lobby-overlay { flex: 1; background: url('https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1920&auto=format&fit=crop') center/cover; display: flex; align-items: center; justify-content: center; }
.lobby-content { background: rgba(0,0,0,0.9); border: 2px solid #b8860b; padding: 40px; border-radius: 10px; text-align: center; width: 450px; box-shadow: 0 0 50px #000; position: relative; }
.wuxia-title { font-size: 2.5rem; color: #ffd700; margin-bottom: 20px; text-shadow: 0 0 10px #b8860b; }
.avatar-frame { width: 100px; height: 100px; border-radius: 50%; border: 3px solid #ffd700; overflow: hidden; margin: 0 auto; background: #000; display: flex; justify-content: center; align-items: center; }
.avatar-frame img { width: 80%; height: 80%; object-fit: contain; }
.hero-info h2 { color: #fff; margin: 10px 0; }
.stats-row { display: flex; justify-content: center; gap: 10px; }
.stat-badge { background: #222; border: 1px solid #555; padding: 4px 12px; border-radius: 4px; color: #ccc; font-weight: bold; }
.refresh-btn { background: none; border: none; color: #b8860b; cursor: pointer; } 
.btn-start { background: #b8860b; color: #000; padding: 15px 30px; border: none; cursor: pointer; width: 100%; font-weight: bold; font-size: 1.2rem; border-radius: 4px; transition: 0.3s; }
.btn-start:hover { background: #ffd700; box-shadow: 0 0 20px #b8860b; }
.btn-cancel { background: #333; color: #fff; padding: 15px; width: 100%; border: 1px solid #555; border-radius: 4px; }
.match-popup { margin-top: 20px; background: #222; padding: 20px; border-radius: 8px; border: 2px solid #28a745; animation: slideUp 0.3s; }
.preview-avatar-box { width: 80px; height: 80px; background: #000; border: 2px solid #fff; border-radius: 50%; margin: 0 auto; overflow: hidden; display: flex; justify-content: center; align-items: center; }
.preview-avatar-box img { width: 80%; height: 80%; object-fit: contain; }
.popup-actions { display: flex; gap: 10px; margin-top: 15px; }
.btn-decline { flex: 1; background: #333; color: #dc3545; border: 1px solid #dc3545; cursor: pointer; font-weight: bold; }
.btn-accept { flex: 2; background: #28a745; color: #fff; border: none; padding: 10px; cursor: pointer; font-weight: bold; }
.btn-accept.accepted { background: #155724; cursor: default; }

/* BATTLE AREA */
.battle-arena { display: flex; flex-direction: column; height: 100%; background: url('https://images.unsplash.com/photo-1535581652167-3d6b98c365b2?q=80&w=1920&auto=format&fit=crop') center/cover; position: relative; }
.log-hint { position: absolute; top: 15px; left: 15px; z-index: 60; background: rgba(0,0,0,0.6); color: #ffd700; padding: 8px 15px; border-radius: 20px; border: 1px solid #b8860b; cursor: pointer; }
.floating-log-panel { position: absolute; top: 60px; left: 15px; z-index: 60; width: 350px; max-height: 250px; background: rgba(0,0,0,0.9); border: 1px solid #ffd700; padding: 10px; overflow-y: auto; font-family: monospace; display: flex; flex-direction: column; }
.log-header { font-weight: bold; color: #ffd700; border-bottom: 1px solid #444; padding-bottom: 5px; margin-bottom: 5px; display: flex; justify-content: space-between; }
.close-log { cursor: pointer; }
.log-line { margin-bottom: 5px; color: #ccc; border-bottom: 1px solid #333; padding-bottom: 2px; }
.top-timer-floating { position: absolute; top: 15px; left: 50%; transform: translateX(-50%); width: 60px; height: 60px; background: rgba(0,0,0,0.8); border: 3px solid #ffd700; border-radius: 50%; z-index: 50; display: flex; align-items: center; justify-content: center; font-size: 1.8rem; font-weight: bold; color: #fff; }
.top-timer-floating.urgent { border-color: #ff4444; color: #ff4444; animation: pulse 0.5s infinite; }

/* CHARACTERS */
.scene-stage { flex: 1; position: relative; }
.fighter { position: absolute; width: 300px; display: flex; flex-direction: column; }
.fighter.enemy { top: 15%; right: 15%; align-items: flex-end; }
.fighter.player { bottom: 15%; left: 15%; align-items: flex-start; }
.sprite-box { width: 180px; height: 180px; display: flex; justify-content: center; align-items: center; position: relative; }
.char-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); filter: drop-shadow(0 5px 10px rgba(0,0,0,0.5)); transition: 0.2s; }
.enemy .char-img { transform: scale(1.5) scaleX(-1); }
.shadow { position: absolute; bottom: 0; width: 100%; height: 20px; background: rgba(0,0,0,0.3); border-radius: 50%; transform: scaleY(0.4); }

.hud { background: rgba(0,0,0,0.7); border: 1px solid #555; padding: 5px 10px; border-radius: 4px; width: 220px; color: #fff; }
.hp-bar-bg { width: 100%; height: 8px; background: #333; border: 1px solid #000; margin: 3px 0; }
.hp-fill { height: 100%; background: linear-gradient(90deg, #d32f2f, #f44336); transition: width 0.3s; }

/* REVEAL ANIMATION */
.clash-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.7); z-index: 40; display: flex; align-items: center; justify-content: center; gap: 40px; }
.move-icon { width: 100px; height: 100px; border-radius: 50%; background: #fff; color: #000; display: flex; align-items: center; justify-content: center; font-size: 3rem; border: 5px solid #ccc; transition: all 0.5s; }
.vs-spark { font-size: 4rem; animation: sparkPop 0.2s; }
.result-text { position: absolute; bottom: 25%; font-size: 3rem; font-weight: bold; color: #ffd700; text-shadow: 0 0 20px #d32f2f; animation: fadeInUp 0.5s; }

/* CONSOLE */
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

/* ANIMATIONS */
@keyframes slideUp { from {transform: translateY(20px); opacity: 0;} to {transform: translateY(0); opacity: 1;} }
@keyframes sparkPop { 0% {transform: scale(0);} 100% {transform: scale(1.5); opacity: 0;} }
@keyframes fadeInUp { from {transform: translateY(20px); opacity: 0;} to {transform: translateY(0); opacity: 1;} }
@keyframes smash { 0% {transform: scale(1);} 50% {transform: scale(1.5) translateY(-50px);} 100% {transform: scale(1) translateY(0);} }
@keyframes slice { 0% {transform: translateX(0);} 50% {transform: translateX(50px) rotate(45deg);} 100% {transform: translateX(0);} }
@keyframes shake { 10%, 90% { transform: translate3d(-1px, 0, 0); } 20%, 80% { transform: translate3d(2px, 0, 0); } 30%, 50%, 70% { transform: translate3d(-4px, 0, 0); } 40%, 60% { transform: translate3d(4px, 0, 0); } }

.anim-smash-win { animation: smash 0.5s forwards; border-color: #ffd700; box-shadow: 0 0 30px #ffd700; z-index: 10; }
.anim-lose { filter: grayscale(1) brightness(0.5); transform: scale(0.8); }
.anim-clash, .shake-hit { animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both; border-color: #ff4444 !important; filter: sepia(1) hue-rotate(-50deg) saturate(3); }
</style> -->



<!-- <template>
  <div class="wuxia-battle-container">
    
    <div v-if="gameState !== 'BATTLE'" class="lobby-layout-wrapper">
      
      <div class="leaderboard-sidebar">
        <div class="lb-mini-header">
          <i class="fas fa-scroll"></i> THIÊN THƯ CHIẾN THẦN
        </div>
        
        <div v-if="lbStore.loadingPvp" class="lb-loading">
          <i class="fas fa-yin-yang fa-spin"></i> Đang tra cứu...
        </div>

        <div v-else class="lb-mini-list custom-scroll">
          <div v-for="(entry, index) in lbStore.topPvp" :key="index" class="lb-mini-item">
            <div class="item-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <div class="item-avatar">
              <img :src="resolveAvatar(entry.avatar)" class="pixel-art" />
            </div>
            <div class="item-info">
              <div class="item-name">{{ entry.username }}</div>
              <div class="item-val">🏆 Thắng: {{ entry.value }}</div>
            </div>
          </div>
          <div v-if="!lbStore.topPvp?.length" class="lb-empty">Chưa có cao thủ...</div>
        </div>
      </div>

      <div class="lobby-main-content">
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
                {{ hasAccepted ? 'ĐANG CHỜ ĐỐI THỦ...' : 'CHẤP NHẬN' }} 
                <span v-if="!hasAccepted">({{ acceptTimer }}s)</span>
              </button>
            </div>
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
          <div class="log-header">DIỄN BIẾN <span @click="showLog=false" class="close-log">×</span></div>
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
          </div>
          <div class="sprite-box">
            <div class="shadow"></div>
            <img :src="enemySprite" class="char-img pixel-art" :class="{'shake-hit': isEnemyHit}" />
          </div>
        </div>

        <div class="fighter player">
          <div class="sprite-box">
            <div class="shadow"></div>
            <img :src="mySprite" class="char-img pixel-art" :class="{'shake-hit': isMyHit}" />
          </div>
          <div class="hud player-hud">
            <div class="name">{{ characterName }} <small>Lv.{{ characterLevel }}</small></div>
            <div class="hp-bar-bg"><div class="hp-fill" :style="{width: percent(myHp, myMaxHp)+'%'}"></div></div>
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
            <button class="square-btn rock" @click="submitRps('ROCK')" :disabled="isActionPending"><i class="fas fa-hand-rock"></i><span>BÚA</span></button>
            <button class="square-btn paper" @click="submitRps('PAPER')" :disabled="isActionPending"><i class="fas fa-hand-paper"></i><span>BAO</span></button>
            <button class="square-btn scissor" @click="submitRps('SCISSORS')" :disabled="isActionPending"><i class="fas fa-hand-scissors"></i><span>KÉO</span></button>
            <button class="square-btn surrender" @click="handleSurrender"><i class="fas fa-flag"></i><span>THOÁT</span></button>
          </div>
          <div v-else class="waiting-square">
            <div class="spinner-box">
              <i class="fas fa-yin-yang fa-spin"></i>
              <p v-if="isRevealing">ĐANG XỬ LÝ...</p>
              <p v-else>CHỜ ĐỐI THỦ...</p>
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
import { useLeaderboardStore } from '../stores/leaderboardStore';
import { CHARACTER_SKINS } from '../utils/assetHelper'; 
import axios from 'axios';

const API_URL = "http://localhost:8080/api/pvp";
const authStore = useAuthStore();
const charStore = useCharacterStore(); 
const lbStore = useLeaderboardStore();

// --- RESOURCES & HELPERS ---
const getSpriteUrl = (skinId) => {
    const skin = CHARACTER_SKINS[skinId] || CHARACTER_SKINS['skin_yasou'];
    return skin.sprites.idle;
};

const resolveAvatar = (avatarStr) => {
    if (!avatarStr) return getSpriteUrl('skin_yasou');
    if (avatarStr.startsWith("/uploads/")) return `http://localhost:8080${avatarStr}`;
    if (avatarStr.includes("http") || avatarStr.startsWith("data:")) return avatarStr;
    return getSpriteUrl(avatarStr);
};

// --- DATA COMPUTED ---
const characterName = computed(() => authStore.character?.name || authStore.user?.username || 'Đại Hiệp');
const characterLevel = computed(() => authStore.character?.level || 1);
const characterWins = computed(() => authStore.character?.pvpWins || 0);
const mySprite = computed(() => getSpriteUrl(authStore.user?.avatarUrl));
const enemySprite = computed(() => getSpriteUrl(enemySkinId.value));

// --- STATE ---
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

// Battle Data
const enemyName = ref("Đối thủ"); const enemyLevel = ref(1); const enemySkinId = ref(null);
const myHp = ref(100); const myMaxHp = ref(100); 
const enemyHp = ref(100); const enemyMaxHp = ref(100); 
const isMyHit = ref(false); const isEnemyHit = ref(false);
const battleLogs = ref([]); const matchMessages = ref([]); 
const chatInput = ref(''); const chatBoxRef = ref(null);
const displayedMyMove = ref(null); const displayedEnemyMove = ref(null); 
const lastProcessedTurn = ref(-1);
const isRevealing = ref(false);

let pollId, searchInterval, acceptInterval, turnTimerInterval, revealTimeout;
const getHeaders = () => ({ 'Authorization': `Bearer ${authStore.token}` });

// --- UTILS ---
const percent = (c, m) => { if (!m || m <= 0) return 0; return Math.max(0, Math.min(100, (c/m)*100)); };
const checkIsPlayer1 = (data) => {
    const myCharId = Number(authStore.character?.charId || authStore.character?.id || 0);
    return myCharId === Number(data.p1Id);
};

const syncBasicInfo = (data) => {
    const isP1 = checkIsPlayer1(data);
    enemyName.value = (isP1 ? data.p2Name : data.p1Name) || "Đối thủ";
    enemyLevel.value = (isP1 ? data.p2Level : data.p1Level) || 1;
    enemySkinId.value = isP1 ? data.p2AvatarUrl : data.p1AvatarUrl;
};

const resetToLobby = () => {
    gameState.value = 'LOBBY'; matchId.value = null;
    clearAllIntervals();
    battleLogs.value = []; matchMessages.value = [];
    battlePhase.value = 'RPS_WAIT';
    isActionPending.value = false; isRevealing.value = false; lastProcessedTurn.value = -1;
    hasAccepted.value = false;
};

const clearAllIntervals = () => {
    if(searchInterval) clearInterval(searchInterval);
    if(acceptInterval) clearInterval(acceptInterval);
    if(turnTimerInterval) clearInterval(turnTimerInterval);
    if(pollId) { clearInterval(pollId); pollId = null; }
    if(revealTimeout) clearTimeout(revealTimeout);
};

// --- ACTIONS ---
const toggleSearch = async () => {
  resetToLobby(); gameState.value = 'SEARCHING'; 
  searchTimer.value = 0; searchInterval = setInterval(() => searchTimer.value++, 1000);
  try { await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() }); startPolling(); } catch(e) { resetToLobby(); }
};

const cancelSearch = async () => { try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch(e){} resetToLobby(); };
const acceptMatch = async () => { if(!matchId.value) return; hasAccepted.value = true; try { await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) {} };
const declineMatch = async () => { cancelSearch(); };

const submitRps = async (move) => {
  if (isActionPending.value || isRevealing.value) return; 
  isActionPending.value = true; battlePhase.value = 'RPS_PENDING'; 
  try { await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() }); } catch (e) { isActionPending.value = false; }
};

const handleSurrender = async () => {
    if(!confirm("Xác nhận thoát trận (Tính thua)?")) return;
    const mid = matchId.value;
    if(mid) ignoredMatchIds.value.add(Number(mid));
    resetToLobby(); 
    try { await axios.post(`${API_URL}/surrender`, { matchId: mid }, { headers: getHeaders() }); } catch(e) {}
    setTimeout(() => { authStore.fetchCharacter(); }, 500);
};

// --- SYNC ENGINE ---
const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data; if (!data) return;
      if (data.matchId && ignoredMatchIds.value.has(Number(data.matchId))) return;

      if (data.status === 'FINISHED') {
         clearAllIntervals();
         const myCharId = Number(authStore.character?.charId || authStore.character?.id || 0);
         const serverWinnerId = data.winnerId !== null ? Number(data.winnerId) : null;
         const isWinner = (serverWinnerId === myCharId);
         const log = data.lastLog || "";
         const isSurrender = log.toLowerCase().includes("đầu hàng");
         syncBattleData(data);
         let msg = (serverWinnerId === null) ? "💀 HÒA!" : (isWinner ? (isSurrender ? "🏳️ ĐỐI THỦ ĐẦU HÀNG! THẮNG!" : "🏆 CHIẾN THẮNG!") : (isSurrender ? "🏳️ BẠN ĐÃ ĐẦU HÀNG!" : "💀 THẤT BẠI!"));
         setTimeout(() => { alert(msg); resetToLobby(); authStore.fetchCharacter(); lbStore.fetchPvPBoard(); }, 200);
         return; 
      }

      if (data.status === 'PENDING') {
          if (gameState.value !== 'MATCH_FOUND' && gameState.value !== 'BATTLE') {
              gameState.value = 'MATCH_FOUND'; matchId.value = data.matchId;
              if(searchInterval) clearInterval(searchInterval);
              syncBasicInfo(data); 
              if(!hasAccepted.value) {
                  acceptTimer.value = 10;
                  if(acceptInterval) clearInterval(acceptInterval);
                  acceptInterval = setInterval(()=> { acceptTimer.value--; if(acceptTimer.value <= 0 && !hasAccepted.value) resetToLobby(); }, 1000);
              }
          }
      } else if (data.status === 'ACTIVE') {
          if (gameState.value !== 'BATTLE') {
            gameState.value = 'BATTLE'; matchId.value = data.matchId;
            if(acceptInterval) clearInterval(acceptInterval);
            startTurnTimer();
          }
          syncBattleData(data); 
      } 
    } catch (e) { }
  }, 1000);
};

const syncBattleData = (data) => {
  syncBasicInfo(data);
  const isP1 = checkIsPlayer1(data);
  const nMyHp = isP1 ? data.p1Hp : data.p2Hp; const nEnHp = isP1 ? data.p2Hp : data.p1Hp;
  if(nMyHp < myHp.value) { isMyHit.value = true; setTimeout(()=>isMyHit.value=false, 300); }
  if(nEnHp < enemyHp.value) { isEnemyHit.value = true; setTimeout(()=>isEnemyHit.value=false, 300); }
  myHp.value = nMyHp; enemyHp.value = nEnHp;
  myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;

  if(data.messages && data.messages.length > matchMessages.value.length) {
    data.messages.slice(matchMessages.value.length).forEach(m => matchMessages.value.push({ 
       sender: m.senderName, text: m.content, isMe: (checkIsPlayer1(data) ? data.p1Id : data.p2Id) == m.senderId 
    }));
    nextTick(() => { if(chatBoxRef.value) chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight; });
  }

  if (isRevealing.value) return; 
  const currentTurn = data.turnCount;
  const bothMoved = data.lastP1Move && data.lastP2Move;

  if (bothMoved && currentTurn > lastProcessedTurn.value) {
      isRevealing.value = true; lastProcessedTurn.value = currentTurn; isActionPending.value = false;
      displayedMyMove.value = isP1 ? data.lastP1Move : data.lastP2Move;
      displayedEnemyMove.value = isP1 ? data.lastP2Move : data.lastP1Move;
      if (data.lastLog) {
          battleLogs.value.push(data.lastLog);
          lastResultText.value = data.lastLog.includes("thắng") ? "THẮNG" : (data.lastLog.includes("thua") ? "THUA" : "XUNG KHẮC");
      }
      battlePhase.value = 'RPS_REVEAL';
      revealTimeout = setTimeout(() => { isRevealing.value = false; battlePhase.value = 'RPS_WAIT'; turnTimer.value = 30; }, 3000);
  } else {
      const myM = isP1 ? data.p1Move : data.p2Move;
      battlePhase.value = myM ? 'RPS_PENDING' : 'RPS_WAIT';
  }
};

const startTurnTimer = () => {
  if(turnTimerInterval) clearInterval(turnTimerInterval);
  turnTimerInterval = setInterval(() => { if (turnTimer.value > 0) turnTimer.value--; }, 1000);
};

const manualRefresh = async () => { await authStore.fetchCharacter(); lbStore.fetchPvPBoard(); };
const sendPrivateChat = async () => {
    if(!chatInput.value.trim()) return;
    try { await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); } catch(e){}
    chatInput.value = '';
};

const getRpsIcon = (m) => ({ 'ROCK': 'fas fa-hand-rock', 'PAPER': 'fas fa-hand-paper', 'SCISSORS': 'fas fa-hand-scissors' }[m] || 'fas fa-question');
const getAnimClass = (myMove, enemyMove, isMe) => {
    if (!myMove || !enemyMove) return '';
    if (myMove === enemyMove) return 'anim-clash'; 
    let iWin = ((myMove === 'ROCK' && enemyMove === 'SCISSORS') || (myMove === 'SCISSORS' && enemyMove === 'PAPER') || (myMove === 'PAPER' && enemyMove === 'ROCK'));
    return isMe ? (iWin ? 'anim-smash-win' : 'anim-lose') : (!iWin ? 'anim-smash-win' : 'anim-lose');
};

onMounted(() => { 
    authStore.fetchCharacter(); 
    lbStore.fetchPvPBoard();
    window.addEventListener('keydown', (e) => {
        if (document.activeElement.tagName !== 'INPUT' && e.key.toLowerCase() === 'l') {
            e.preventDefault(); showLog.value = !showLog.value;
        }
    });
    startPolling(); 
});

onUnmounted(() => { clearAllIntervals(); });
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Roboto+Condensed:wght@700&display=swap');

.pixel-art { image-rendering: pixelated; }

/* LAYOUT CẤU TRÚC MỚI */
.wuxia-battle-container { width: 100%; height: 100vh; background: #1a1a1a; font-family: 'Roboto Condensed', sans-serif; overflow: hidden; }

.lobby-layout-wrapper { display: flex; width: 100%; height: 100%; background: url('https://htkhang111.github.io/background/b_doanhtrai.png') center/cover; }

/* BẢNG XẾP HẠNG SIDEBAR */
.leaderboard-sidebar { width: 300px; background: rgba(0, 0, 0, 0.85); border-right: 2px solid #b8860b; display: flex; flex-direction: column; padding: 20px; backdrop-filter: blur(8px); z-index: 10; }
.lb-mini-header { color: #ffd700; font-size: 1.3rem; text-align: center; padding-bottom: 15px; border-bottom: 2px double #b8860b; margin-bottom: 15px; font-weight: bold; }
.lb-mini-list { flex: 1; overflow-y: auto; padding-right: 5px; }
.lb-mini-item { display: flex; align-items: center; gap: 12px; padding: 10px; background: rgba(255, 255, 255, 0.03); border: 1px solid #333; margin-bottom: 8px; border-radius: 4px; }
.item-rank { font-weight: bold; width: 25px; color: #888; text-align: center; }
.item-rank.rank-1 { color: #ffd700; font-size: 1.2rem; }
.item-rank.rank-2 { color: #c0c0c0; }
.item-rank.rank-3 { color: #cd7f32; }
.item-avatar { width: 45px; height: 45px; background: #111; border: 1px solid #444; border-radius: 50%; overflow: hidden; display: flex; justify-content: center; align-items: center; }
.item-avatar img { width: 80%; height: 80%; object-fit: contain; }
.item-name { color: #eee; font-size: 0.95rem; font-weight: bold; }
.item-val { color: #b8860b; font-size: 0.85rem; }
.lb-loading, .lb-empty { text-align: center; color: #666; padding: 20px; }

/* NỘI DUNG CHÍNH BÊN PHẢI */
.lobby-main-content { flex: 1; display: flex; align-items: center; justify-content: center; position: relative; background: rgba(0,0,0,0.2); }
.lobby-content { background: rgba(0,0,0,0.9); border: 2px solid #b8860b; padding: 40px; border-radius: 10px; text-align: center; width: 450px; box-shadow: 0 0 50px #000; }

/* CSS CHI TIẾT CŨ */
.wuxia-title { font-size: 2.5rem; color: #ffd700; margin-bottom: 20px; text-shadow: 0 0 10px #b8860b; }
.avatar-frame { width: 100px; height: 100px; border-radius: 50%; border: 3px solid #ffd700; overflow: hidden; margin: 0 auto; background: #000; display: flex; justify-content: center; align-items: center; }
.avatar-frame img { width: 80%; height: 80%; object-fit: contain; }
.hero-info h2 { color: #fff; margin: 10px 0; }
.stats-row { display: flex; justify-content: center; gap: 10px; }
.stat-badge { background: #222; border: 1px solid #555; padding: 4px 12px; border-radius: 4px; color: #ccc; font-weight: bold; }
.refresh-btn { background: none; border: none; color: #b8860b; cursor: pointer; } 
.btn-start { background: #b8860b; color: #000; padding: 15px 30px; border: none; cursor: pointer; width: 100%; font-weight: bold; font-size: 1.2rem; border-radius: 4px; transition: 0.3s; }
.btn-start:hover { background: #ffd700; box-shadow: 0 0 20px #b8860b; }
.btn-cancel { background: #333; color: #fff; padding: 15px; width: 100%; border: 1px solid #555; border-radius: 4px; }

/* BATTLE ARENA (Giữ nguyên từ code cũ của bạn) */
.battle-arena { display: flex; flex-direction: column; height: 100%; background: url('https://images.unsplash.com/photo-1535581652167-3d6b98c365b2?q=80&w=1920&auto=format&fit=crop') center/cover; position: relative; }
.log-hint { position: absolute; top: 15px; left: 15px; z-index: 60; background: rgba(0,0,0,0.6); color: #ffd700; padding: 8px 15px; border-radius: 20px; border: 1px solid #b8860b; cursor: pointer; }
.floating-log-panel { position: absolute; top: 60px; left: 15px; z-index: 60; width: 350px; max-height: 250px; background: rgba(0,0,0,0.9); border: 1px solid #ffd700; padding: 10px; overflow-y: auto; }
.top-timer-floating { position: absolute; top: 15px; left: 50%; transform: translateX(-50%); width: 60px; height: 60px; background: rgba(0,0,0,0.8); border: 3px solid #ffd700; border-radius: 50%; z-index: 50; display: flex; align-items: center; justify-content: center; font-size: 1.8rem; color: #fff; }
.top-timer-floating.urgent { border-color: #ff4444; color: #ff4444; animation: pulse 0.5s infinite; }

/* FIGHTERS */
.scene-stage { flex: 1; position: relative; }
.fighter { position: absolute; width: 300px; display: flex; flex-direction: column; }
.fighter.enemy { top: 15%; right: 15%; align-items: flex-end; }
.fighter.player { bottom: 15%; left: 15%; align-items: flex-start; }
.sprite-box { width: 180px; height: 180px; display: flex; justify-content: center; align-items: center; position: relative; }
.char-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); transition: 0.2s; }
.enemy .char-img { transform: scale(1.5) scaleX(-1); }
.hud { background: rgba(0,0,0,0.7); border: 1px solid #555; padding: 5px 10px; border-radius: 4px; width: 220px; color: #fff; }
.hp-bar-bg { width: 100%; height: 8px; background: #333; margin: 3px 0; }
.hp-fill { height: 100%; background: linear-gradient(90deg, #d32f2f, #f44336); transition: width 0.3s; }

/* CLASH OVERLAY */
.clash-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.7); z-index: 40; display: flex; align-items: center; justify-content: center; gap: 40px; }
.move-icon { width: 100px; height: 100px; border-radius: 50%; background: #fff; color: #000; display: flex; align-items: center; justify-content: center; font-size: 3rem; }
.result-text { position: absolute; bottom: 25%; font-size: 3rem; font-weight: bold; color: #ffd700; }

/* CONSOLE */
.bottom-console-split { height: 240px; background: #111; border-top: 2px solid #b8860b; display: flex; }
.left-chat-column { flex: 6; display: flex; flex-direction: column; border-right: 1px solid #333; background: #000; }
.chat-display { flex: 1; padding: 10px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.chat-bubble { font-size: 0.9rem; background: #222; padding: 5px 10px; border-radius: 4px; color: #ddd; align-self: flex-start; }
.chat-bubble.me { align-self: flex-end; background: #004d40; color: #a7ffeb; }
.chat-input-row { display: flex; padding: 8px; }
.chat-input-row input { flex: 1; background: #222; border: 1px solid #444; color: #fff; padding: 8px; }
.chat-input-row button { background: #b8860b; border: none; padding: 0 15px; margin-left: 5px; cursor: pointer; }

.right-action-column { flex: 4; padding: 10px; display: flex; align-items: center; justify-content: center; }
.square-grid-actions { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 10px; width: 100%; height: 100%; }
.square-btn { background: #222; border: 1px solid #444; color: #eee; cursor: pointer; display: flex; flex-direction: column; align-items: center; justify-content: center; font-weight: bold; }
.square-btn i { font-size: 2rem; }

/* SCROLLBAR */
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #b8860b; border-radius: 2px; }

/* ANIMATIONS */
@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
@keyframes shake { 10%, 90% { transform: translate3d(-1px, 0, 0); } 20%, 80% { transform: translate3d(2px, 0, 0); } 40%, 60% { transform: translate3d(4px, 0, 0); } }
.shake-hit { animation: shake 0.5s both; filter: sepia(1) saturate(3); }
.anim-smash-win { transform: scale(1.3); border: 4px solid #ffd700; transition: 0.3s; }
.anim-lose { opacity: 0.5; transform: scale(0.8); }
</style> -->



<template>
  <div class="wuxia-battle-container">
    
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${currentBg})` }"></div>
      <div class="wood-overlay"></div>
      <div class="vignette"></div>
    </div>

    <div class="content-layer">
      
      <div v-if="gameState !== 'BATTLE'" class="lobby-layout-wrapper">
        
        <div class="leaderboard-sidebar">
          <div class="lb-mini-header">
            <i class="fas fa-scroll"></i> THIÊN THƯ CHIẾN THẦN
          </div>
          
          <div v-if="lbStore.loadingPvp" class="lb-loading">
            <i class="fas fa-yin-yang fa-spin"></i> Đang tra cứu...
          </div>

          <div v-else class="lb-mini-list custom-scroll">
            <div v-for="(entry, index) in lbStore.topPvp" :key="index" class="lb-mini-item">
              <div class="item-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <div class="item-avatar">
                <img :src="resolveAvatar(entry.avatar)" class="pixel-art" />
              </div>
              <div class="item-info">
                <div class="item-name">{{ entry.username }}</div>
                <div class="item-val">🏆 Thắng: {{ entry.value }}</div>
              </div>
            </div>
            <div v-if="!lbStore.topPvp?.length" class="lb-empty">Chưa có cao thủ...</div>
          </div>
        </div>

        <div class="lobby-main-content">
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
                  {{ hasAccepted ? 'ĐANG CHỜ ĐỐI THỦ...' : 'CHẤP NHẬN' }} 
                  <span v-if="!hasAccepted">({{ acceptTimer }}s)</span>
                </button>
              </div>
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
            <div class="log-header">DIỄN BIẾN <span @click="showLog=false" class="close-log">×</span></div>
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
            </div>
            <div class="sprite-box">
              <div class="shadow"></div>
              <img :src="enemySprite" class="char-img pixel-art" :class="{'shake-hit': isEnemyHit}" />
            </div>
          </div>

          <div class="fighter player">
            <div class="sprite-box">
              <div class="shadow"></div>
              <img :src="mySprite" class="char-img pixel-art" :class="{'shake-hit': isMyHit}" />
            </div>
            <div class="hud player-hud">
              <div class="name">{{ characterName }} <small>Lv.{{ characterLevel }}</small></div>
              <div class="hp-bar-bg"><div class="hp-fill" :style="{width: percent(myHp, myMaxHp)+'%'}"></div></div>
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
              <button class="square-btn rock" @click="submitRps('ROCK')" :disabled="isActionPending"><i class="fas fa-hand-rock"></i><span>BÚA</span></button>
              <button class="square-btn paper" @click="submitRps('PAPER')" :disabled="isActionPending"><i class="fas fa-hand-paper"></i><span>BAO</span></button>
              <button class="square-btn scissor" @click="submitRps('SCISSORS')" :disabled="isActionPending"><i class="fas fa-hand-scissors"></i><span>KÉO</span></button>
              <button class="square-btn surrender" @click="handleSurrender"><i class="fas fa-flag"></i><span>THOÁT</span></button>
            </div>
            <div v-else class="waiting-square">
              <div class="spinner-box">
                <i class="fas fa-yin-yang fa-spin"></i>
                <p v-if="isRevealing">ĐANG XỬ LÝ...</p>
                <p v-else>CHỜ ĐỐI THỦ...</p>
                <button class="emergency-exit" @click="handleSurrender">THOÁT NGAY</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    
    </div> </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useAuthStore } from '../stores/authStore';
import { useCharacterStore } from '../stores/characterStore';
import { useLeaderboardStore } from '../stores/leaderboardStore';
import { CHARACTER_SKINS } from '../utils/assetHelper'; 
import axios from 'axios';

const API_URL = "http://localhost:8080/api/pvp";
const authStore = useAuthStore();
const charStore = useCharacterStore(); 
const lbStore = useLeaderboardStore();

// --- RESOURCES & HELPERS ---
const getSpriteUrl = (skinId) => {
    const skin = CHARACTER_SKINS[skinId] || CHARACTER_SKINS['skin_yasou'];
    return skin.sprites.idle;
};

const resolveAvatar = (avatarStr) => {
    if (!avatarStr) return getSpriteUrl('skin_yasou');
    if (avatarStr.startsWith("/uploads/")) return `http://localhost:8080${avatarStr}`;
    if (avatarStr.includes("http") || avatarStr.startsWith("data:")) return avatarStr;
    return getSpriteUrl(avatarStr);
};

// --- DATA COMPUTED ---
const characterName = computed(() => authStore.character?.name || authStore.user?.username || 'Đại Hiệp');
const characterLevel = computed(() => authStore.character?.level || 1);
const characterWins = computed(() => authStore.character?.pvpWins || 0);
const mySprite = computed(() => getSpriteUrl(authStore.user?.avatarUrl));
const enemySprite = computed(() => getSpriteUrl(enemySkinId.value));

// [MỚI] Computed để đổi ảnh nền
const currentBg = computed(() => {
  if (gameState.value === 'BATTLE') {
    return 'https://images.unsplash.com/photo-1535581652167-3d6b98c365b2?q=80&w=1920&auto=format&fit=crop';
  }
  return 'https://htkhang111.github.io/background/b_doanhtrai.png';
});

// --- STATE ---
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

// Battle Data
const enemyName = ref("Đối thủ"); const enemyLevel = ref(1); const enemySkinId = ref(null);
const myHp = ref(100); const myMaxHp = ref(100); 
const enemyHp = ref(100); const enemyMaxHp = ref(100); 
const isMyHit = ref(false); const isEnemyHit = ref(false);
const battleLogs = ref([]); const matchMessages = ref([]); 
const chatInput = ref(''); const chatBoxRef = ref(null);
const displayedMyMove = ref(null); const displayedEnemyMove = ref(null); 
const lastProcessedTurn = ref(-1);
const isRevealing = ref(false);

let pollId, searchInterval, acceptInterval, turnTimerInterval, revealTimeout;
const getHeaders = () => ({ 'Authorization': `Bearer ${authStore.token}` });

// --- UTILS ---
const percent = (c, m) => { if (!m || m <= 0) return 0; return Math.max(0, Math.min(100, (c/m)*100)); };
const checkIsPlayer1 = (data) => {
    const myCharId = Number(authStore.character?.charId || authStore.character?.id || 0);
    return myCharId === Number(data.p1Id);
};

const syncBasicInfo = (data) => {
    const isP1 = checkIsPlayer1(data);
    enemyName.value = (isP1 ? data.p2Name : data.p1Name) || "Đối thủ";
    enemyLevel.value = (isP1 ? data.p2Level : data.p1Level) || 1;
    enemySkinId.value = isP1 ? data.p2AvatarUrl : data.p1AvatarUrl;
};

const resetToLobby = () => {
    gameState.value = 'LOBBY'; matchId.value = null;
    clearAllIntervals();
    battleLogs.value = []; matchMessages.value = [];
    battlePhase.value = 'RPS_WAIT';
    isActionPending.value = false; isRevealing.value = false; lastProcessedTurn.value = -1;
    hasAccepted.value = false;
};

const clearAllIntervals = () => {
    if(searchInterval) clearInterval(searchInterval);
    if(acceptInterval) clearInterval(acceptInterval);
    if(turnTimerInterval) clearInterval(turnTimerInterval);
    if(pollId) { clearInterval(pollId); pollId = null; }
    if(revealTimeout) clearTimeout(revealTimeout);
};

// --- ACTIONS ---
const toggleSearch = async () => {
  resetToLobby(); gameState.value = 'SEARCHING'; 
  searchTimer.value = 0; searchInterval = setInterval(() => searchTimer.value++, 1000);
  try { await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() }); startPolling(); } catch(e) { resetToLobby(); }
};

const cancelSearch = async () => { try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch(e){} resetToLobby(); };
const acceptMatch = async () => { if(!matchId.value) return; hasAccepted.value = true; try { await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) {} };
const declineMatch = async () => { cancelSearch(); };

const submitRps = async (move) => {
  if (isActionPending.value || isRevealing.value) return; 
  isActionPending.value = true; battlePhase.value = 'RPS_PENDING'; 
  try { await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() }); } catch (e) { isActionPending.value = false; }
};

const handleSurrender = async () => {
    if(!confirm("Xác nhận thoát trận (Tính thua)?")) return;
    const mid = matchId.value;
    if(mid) ignoredMatchIds.value.add(Number(mid));
    resetToLobby(); 
    try { await axios.post(`${API_URL}/surrender`, { matchId: mid }, { headers: getHeaders() }); } catch(e) {}
    setTimeout(() => { authStore.fetchCharacter(); }, 500);
};

// --- SYNC ENGINE ---
const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data; if (!data) return;
      if (data.matchId && ignoredMatchIds.value.has(Number(data.matchId))) return;

      if (data.status === 'FINISHED') {
         clearAllIntervals();
         const myCharId = Number(authStore.character?.charId || authStore.character?.id || 0);
         const serverWinnerId = data.winnerId !== null ? Number(data.winnerId) : null;
         const isWinner = (serverWinnerId === myCharId);
         const log = data.lastLog || "";
         const isSurrender = log.toLowerCase().includes("đầu hàng");
         syncBattleData(data);
         let msg = (serverWinnerId === null) ? "💀 HÒA!" : (isWinner ? (isSurrender ? "🏳️ ĐỐI THỦ ĐẦU HÀNG! THẮNG!" : "🏆 CHIẾN THẮNG!") : (isSurrender ? "🏳️ BẠN ĐÃ ĐẦU HÀNG!" : "💀 THẤT BẠI!"));
         setTimeout(() => { alert(msg); resetToLobby(); authStore.fetchCharacter(); lbStore.fetchPvPBoard(); }, 200);
         return; 
      }

      if (data.status === 'PENDING') {
          if (gameState.value !== 'MATCH_FOUND' && gameState.value !== 'BATTLE') {
              gameState.value = 'MATCH_FOUND'; matchId.value = data.matchId;
              if(searchInterval) clearInterval(searchInterval);
              syncBasicInfo(data); 
              if(!hasAccepted.value) {
                  acceptTimer.value = 10;
                  if(acceptInterval) clearInterval(acceptInterval);
                  acceptInterval = setInterval(()=> { acceptTimer.value--; if(acceptTimer.value <= 0 && !hasAccepted.value) resetToLobby(); }, 1000);
              }
          }
      } else if (data.status === 'ACTIVE') {
          if (gameState.value !== 'BATTLE') {
            gameState.value = 'BATTLE'; matchId.value = data.matchId;
            if(acceptInterval) clearInterval(acceptInterval);
            startTurnTimer();
          }
          syncBattleData(data); 
      } 
    } catch (e) { }
  }, 1000);
};

const syncBattleData = (data) => {
  syncBasicInfo(data);
  const isP1 = checkIsPlayer1(data);
  const nMyHp = isP1 ? data.p1Hp : data.p2Hp; const nEnHp = isP1 ? data.p2Hp : data.p1Hp;
  if(nMyHp < myHp.value) { isMyHit.value = true; setTimeout(()=>isMyHit.value=false, 300); }
  if(nEnHp < enemyHp.value) { isEnemyHit.value = true; setTimeout(()=>isEnemyHit.value=false, 300); }
  myHp.value = nMyHp; enemyHp.value = nEnHp;
  myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;

  if(data.messages && data.messages.length > matchMessages.value.length) {
    data.messages.slice(matchMessages.value.length).forEach(m => matchMessages.value.push({ 
       sender: m.senderName, text: m.content, isMe: (checkIsPlayer1(data) ? data.p1Id : data.p2Id) == m.senderId 
    }));
    nextTick(() => { if(chatBoxRef.value) chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight; });
  }

  if (isRevealing.value) return; 
  const currentTurn = data.turnCount;
  const bothMoved = data.lastP1Move && data.lastP2Move;

  if (bothMoved && currentTurn > lastProcessedTurn.value) {
      isRevealing.value = true; lastProcessedTurn.value = currentTurn; isActionPending.value = false;
      displayedMyMove.value = isP1 ? data.lastP1Move : data.lastP2Move;
      displayedEnemyMove.value = isP1 ? data.lastP2Move : data.lastP1Move;
      if (data.lastLog) {
          battleLogs.value.push(data.lastLog);
          lastResultText.value = data.lastLog.includes("thắng") ? "THẮNG" : (data.lastLog.includes("thua") ? "THUA" : "XUNG KHẮC");
      }
      battlePhase.value = 'RPS_REVEAL';
      revealTimeout = setTimeout(() => { isRevealing.value = false; battlePhase.value = 'RPS_WAIT'; turnTimer.value = 30; }, 3000);
  } else {
      const myM = isP1 ? data.p1Move : data.p2Move;
      battlePhase.value = myM ? 'RPS_PENDING' : 'RPS_WAIT';
  }
};

const startTurnTimer = () => {
  if(turnTimerInterval) clearInterval(turnTimerInterval);
  turnTimerInterval = setInterval(() => { if (turnTimer.value > 0) turnTimer.value--; }, 1000);
};

const manualRefresh = async () => { await authStore.fetchCharacter(); lbStore.fetchPvPBoard(); };
const sendPrivateChat = async () => {
    if(!chatInput.value.trim()) return;
    try { await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); } catch(e){}
    chatInput.value = '';
};

const getRpsIcon = (m) => ({ 'ROCK': 'fas fa-hand-rock', 'PAPER': 'fas fa-hand-paper', 'SCISSORS': 'fas fa-hand-scissors' }[m] || 'fas fa-question');
const getAnimClass = (myMove, enemyMove, isMe) => {
    if (!myMove || !enemyMove) return '';
    if (myMove === enemyMove) return 'anim-clash'; 
    let iWin = ((myMove === 'ROCK' && enemyMove === 'SCISSORS') || (myMove === 'SCISSORS' && enemyMove === 'PAPER') || (myMove === 'PAPER' && enemyMove === 'ROCK'));
    return isMe ? (iWin ? 'anim-smash-win' : 'anim-lose') : (!iWin ? 'anim-smash-win' : 'anim-lose');
};

onMounted(() => { 
    authStore.fetchCharacter(); 
    lbStore.fetchPvPBoard();
    window.addEventListener('keydown', (e) => {
        if (document.activeElement.tagName !== 'INPUT' && e.key.toLowerCase() === 'l') {
            e.preventDefault(); showLog.value = !showLog.value;
        }
    });
    startPolling(); 
});

onUnmounted(() => { clearAllIntervals(); });
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Roboto+Condensed:wght@700&display=swap');

.pixel-art { image-rendering: pixelated; }

/* CONTAINER CHÍNH */
.wuxia-battle-container { 
  width: 100%; 
  min-height: 100vh; 
  font-family: 'Roboto Condensed', sans-serif; 
  position: relative; 
  overflow: hidden; 
  background: #000;
}

/* --- HỆ THỐNG BACKGROUND 3 LỚP --- */
.bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none; /* Không chặn click */
}

.mountain-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  transition: background-image 0.5s ease-in-out;
  opacity: 0.7;
}

.wood-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(62, 39, 35, 0.6), rgba(30, 20, 15, 0.8));
  mix-blend-mode: multiply;
  /* Giữ nguyên màu gỗ, không đổi theo giờ */
}

.vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, transparent 50%, #1a100d 100%);
}

/* --- CONTENT LAYER (Đè lên background) --- */
.content-layer {
  position: relative;
  z-index: 10;
  width: 100%;
  height: 100vh;
}

/* --- LOBBY LAYOUT --- */
.lobby-layout-wrapper { 
  display: flex; 
  width: 100%; 
  height: 100%; 
  /* Đã xóa background cũ */
}

/* BẢNG XẾP HẠNG SIDEBAR */
.leaderboard-sidebar { width: 300px; background: rgba(0, 0, 0, 0.85); border-right: 2px solid #b8860b; display: flex; flex-direction: column; padding: 20px; backdrop-filter: blur(8px); z-index: 10; }
.lb-mini-header { color: #ffd700; font-size: 1.3rem; text-align: center; padding-bottom: 15px; border-bottom: 2px double #b8860b; margin-bottom: 15px; font-weight: bold; }
.lb-mini-list { flex: 1; overflow-y: auto; padding-right: 5px; }
.lb-mini-item { display: flex; align-items: center; gap: 12px; padding: 10px; background: rgba(255, 255, 255, 0.03); border: 1px solid #333; margin-bottom: 8px; border-radius: 4px; }
.item-rank { font-weight: bold; width: 25px; color: #888; text-align: center; }
.item-rank.rank-1 { color: #ffd700; font-size: 1.2rem; }
.item-rank.rank-2 { color: #c0c0c0; }
.item-rank.rank-3 { color: #cd7f32; }
.item-avatar { width: 45px; height: 45px; background: #111; border: 1px solid #444; border-radius: 50%; overflow: hidden; display: flex; justify-content: center; align-items: center; }
.item-avatar img { width: 80%; height: 80%; object-fit: contain; }
.item-name { color: #eee; font-size: 0.95rem; font-weight: bold; }
.item-val { color: #b8860b; font-size: 0.85rem; }
.lb-loading, .lb-empty { text-align: center; color: #666; padding: 20px; }

/* NỘI DUNG CHÍNH BÊN PHẢI */
.lobby-main-content { flex: 1; display: flex; align-items: center; justify-content: center; position: relative; }
.lobby-content { background: rgba(0,0,0,0.9); border: 2px solid #b8860b; padding: 40px; border-radius: 10px; text-align: center; width: 450px; box-shadow: 0 0 50px #000; }

/* CSS CHI TIẾT CŨ */
.wuxia-title { font-size: 2.5rem; color: #ffd700; margin-bottom: 20px; text-shadow: 0 0 10px #b8860b; }
.avatar-frame { width: 100px; height: 100px; border-radius: 50%; border: 3px solid #ffd700; overflow: hidden; margin: 0 auto; background: #000; display: flex; justify-content: center; align-items: center; }
.avatar-frame img { width: 80%; height: 80%; object-fit: contain; }
.hero-info h2 { color: #fff; margin: 10px 0; }
.stats-row { display: flex; justify-content: center; gap: 10px; }
.stat-badge { background: #222; border: 1px solid #555; padding: 4px 12px; border-radius: 4px; color: #ccc; font-weight: bold; }
.refresh-btn { background: none; border: none; color: #b8860b; cursor: pointer; } 
.btn-start { background: #b8860b; color: #000; padding: 15px 30px; border: none; cursor: pointer; width: 100%; font-weight: bold; font-size: 1.2rem; border-radius: 4px; transition: 0.3s; }
.btn-start:hover { background: #ffd700; box-shadow: 0 0 20px #b8860b; }
.btn-cancel { background: #333; color: #fff; padding: 15px; width: 100%; border: 1px solid #555; border-radius: 4px; }

/* --- BATTLE ARENA --- */
.battle-arena { 
  display: flex; 
  flex-direction: column; 
  height: 100%; 
  position: relative; 
  /* Đã xóa background cũ */
}

.log-hint { position: absolute; top: 15px; left: 15px; z-index: 60; background: rgba(0,0,0,0.6); color: #ffd700; padding: 8px 15px; border-radius: 20px; border: 1px solid #b8860b; cursor: pointer; }
.floating-log-panel { position: absolute; top: 60px; left: 15px; z-index: 60; width: 350px; max-height: 250px; background: rgba(0,0,0,0.9); border: 1px solid #ffd700; padding: 10px; overflow-y: auto; }
.top-timer-floating { position: absolute; top: 15px; left: 50%; transform: translateX(-50%); width: 60px; height: 60px; background: rgba(0,0,0,0.8); border: 3px solid #ffd700; border-radius: 50%; z-index: 50; display: flex; align-items: center; justify-content: center; font-size: 1.8rem; color: #fff; }
.top-timer-floating.urgent { border-color: #ff4444; color: #ff4444; animation: pulse 0.5s infinite; }

/* FIGHTERS */
.scene-stage { flex: 1; position: relative; }
.fighter { position: absolute; width: 300px; display: flex; flex-direction: column; }
.fighter.enemy { top: 15%; right: 15%; align-items: flex-end; }
.fighter.player { bottom: 15%; left: 15%; align-items: flex-start; }
.sprite-box { width: 180px; height: 180px; display: flex; justify-content: center; align-items: center; position: relative; }
.char-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); transition: 0.2s; }
.enemy .char-img { transform: scale(1.5) scaleX(-1); }
.hud { background: rgba(0,0,0,0.7); border: 1px solid #555; padding: 5px 10px; border-radius: 4px; width: 220px; color: #fff; }
.hp-bar-bg { width: 100%; height: 8px; background: #333; margin: 3px 0; }
.hp-fill { height: 100%; background: linear-gradient(90deg, #d32f2f, #f44336); transition: width 0.3s; }

/* CLASH OVERLAY */
.clash-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.7); z-index: 40; display: flex; align-items: center; justify-content: center; gap: 40px; }
.move-icon { width: 100px; height: 100px; border-radius: 50%; background: #fff; color: #000; display: flex; align-items: center; justify-content: center; font-size: 3rem; }
.result-text { position: absolute; bottom: 25%; font-size: 3rem; font-weight: bold; color: #ffd700; }

/* CONSOLE */
.bottom-console-split { height: 240px; background: #111; border-top: 2px solid #b8860b; display: flex; }
.left-chat-column { flex: 6; display: flex; flex-direction: column; border-right: 1px solid #333; background: #000; }
.chat-display { flex: 1; padding: 10px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.chat-bubble { font-size: 0.9rem; background: #222; padding: 5px 10px; border-radius: 4px; color: #ddd; align-self: flex-start; }
.chat-bubble.me { align-self: flex-end; background: #004d40; color: #a7ffeb; }
.chat-input-row { display: flex; padding: 8px; }
.chat-input-row input { flex: 1; background: #222; border: 1px solid #444; color: #fff; padding: 8px; }
.chat-input-row button { background: #b8860b; border: none; padding: 0 15px; margin-left: 5px; cursor: pointer; }

.right-action-column { flex: 4; padding: 10px; display: flex; align-items: center; justify-content: center; }
.square-grid-actions { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 10px; width: 100%; height: 100%; }
.square-btn { background: #222; border: 1px solid #444; color: #eee; cursor: pointer; display: flex; flex-direction: column; align-items: center; justify-content: center; font-weight: bold; }
.square-btn i { font-size: 2rem; }

/* SCROLLBAR */
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #b8860b; border-radius: 2px; }

/* ANIMATIONS */
@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
@keyframes shake { 10%, 90% { transform: translate3d(-1px, 0, 0); } 20%, 80% { transform: translate3d(2px, 0, 0); } 40%, 60% { transform: translate3d(4px, 0, 0); } }
.shake-hit { animation: shake 0.5s both; filter: sepia(1) saturate(3); }
.anim-smash-win { transform: scale(1.3); border: 4px solid #ffd700; transition: 0.3s; }
.anim-lose { opacity: 0.5; transform: scale(0.8); }
</style>