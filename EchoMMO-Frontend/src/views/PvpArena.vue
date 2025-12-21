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
  <div class="page-container wuxia-dashboard">
    
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${currentBg})` }"></div>
      <div class="wood-overlay" :class="{ 'night-mode': false }"></div>
      <div class="vignette"></div>
    </div>

    <div class="dashboard-wrapper">
      
      <div class="command-header wood-panel">
        <div class="header-left">
           <div class="server-tag">
             <span class="status-orb red"></span> 
             <span class="server-txt">KHU VỰC: PK (TỰ DO)</span>
           </div>
           <div class="char-block">
             <div class="char-text">
               <span class="greet-txt"><i class="fas fa-fist-raised"></i> Tỷ Võ Luận Đạo</span>
               <h1 class="char-name">VÕ ĐÀI TRANH HÙNG</h1>
             </div>
           </div>
        </div>
        <div class="header-right">
          <div class="wealth-bar">
            <div class="wealth-item">
              <span class="lbl">THẮNG:</span>
              <span class="amt gold-txt">{{ characterWins }}</span>
            </div>
          </div>
          <div class="weather-seal">
            <i class="fas fa-trophy"></i>
          </div>
        </div>
      </div>

      <div class="pvp-stage-container wood-panel-content">
        
        <div v-if="gameState !== 'BATTLE'" class="lobby-layout">
          
          <div class="sidebar-col">
            <div class="inner-wood-box">
              <div class="box-header">
                <i class="fas fa-scroll"></i> CAO THỦ BẢNG
              </div>
              
              <div v-if="lbStore.loadingPvp" class="lb-loading">
                <i class="fas fa-yin-yang fa-spin"></i> Đang tra cứu...
              </div>

              <div v-else class="lb-mini-list custom-scroll">
                <div v-for="(entry, index) in lbStore.topPvp" :key="index" class="lb-mini-item">
                  <div class="item-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
                  <div class="item-avatar small">
                    <img :src="resolveAvatar(entry.avatar)" class="pixel-art" />
                  </div>
                  <div class="item-info">
                    <div class="item-name">{{ entry.username }}</div>
                    <div class="item-val">{{ entry.value }} Trận</div>
                  </div>
                </div>
                <div v-if="!lbStore.topPvp?.length" class="lb-empty">Chưa có dữ liệu...</div>
              </div>
            </div>
          </div>

          <div class="main-col">
            <div class="hero-card-wide">
               <div class="card-bg-pattern"></div>
               
               <div class="hero-visual">
                 <div class="avatar-frame gold-border large">
                   <img :src="mySprite" class="pixel-art" />
                   <div class="level-tag">
                      <span class="lbl">CẤP</span>
                      <span class="val">{{ characterLevel }}</span>
                   </div>
                 </div>
                 <h2 class="hero-name">{{ characterName }}</h2>
               </div>

               <div class="action-area">
                  <div v-if="gameState === 'LOBBY'" class="btn-group">
                    <button class="wuxia-btn primary big pulse-anim" @click="toggleSearch">
                      <i class="fas fa-swords"></i> TÌM ĐỐI THỦ
                    </button>
                    
                    <button class="wuxia-btn gold-btn big" @click="manualRefresh">
                      <i class="fas fa-sync-alt"></i> CẬP NHẬT
                    </button>
                  </div>

                  <div v-else-if="gameState === 'SEARCHING'" class="searching-state">
                    <div class="spinner-radar"><i class="fas fa-spinner fa-spin"></i></div>
                    <p>Đang tìm kiếm đối thủ... ({{ searchTimer }}s)</p>
                    <button class="wuxia-btn danger" @click="cancelSearch">HỦY BỎ</button>
                  </div>

                  <div v-if="gameState === 'MATCH_FOUND'" class="match-popup-embed">
                    <h3>⚔️ ĐỐI THỦ XUẤT HIỆN ⚔️</h3>
                    <div class="enemy-preview-row">
                      <div class="preview-avatar">
                        <img :src="enemySprite" class="pixel-art" />
                      </div>
                      <div class="preview-info">
                        <strong>{{ enemyName }}</strong>
                        <span>Cấp {{ enemyLevel }}</span>
                      </div>
                    </div>
                    <div class="popup-actions-row">
                      <button class="wuxia-btn outline danger" @click="declineMatch">BỎ QUA</button>
                      <button class="wuxia-btn primary" @click="acceptMatch" :disabled="hasAccepted">
                        {{ hasAccepted ? 'CHỜ ĐỐI THỦ...' : `CHẤP NHẬN (${acceptTimer}s)` }}
                      </button>
                    </div>
                  </div>
               </div>
            </div>
          </div>
        </div>

        <div v-else class="battle-arena-frame">
          <div class="arena-background" :style="{ backgroundImage: `url(${currentBg})` }"></div>
          
          <div class="log-overlay-btn" @click="showLog = !showLog">
            <i class="fas fa-book"></i> [L]
          </div>
          
          <transition name="fade">
            <div v-if="showLog" class="floating-log-panel custom-scroll">
              <div class="log-header">NHẬT KÝ CHIẾN ĐẤU <span @click="showLog=false" class="close-log">×</span></div>
              <div class="log-content">
                <div v-for="(log, i) in battleLogs.slice().reverse()" :key="i" class="log-line">» {{ log }}</div>
              </div>
            </div>
          </transition>

          <div class="top-timer-display" :class="{'urgent': turnTimer <= 10}">
            <div class="timer-value">{{ turnTimer }}</div>
          </div>

          <div class="fighters-stage">
            <div class="fighter enemy">
              <div class="hud enemy-hud">
                <div class="hud-row">
                  <span class="f-name">{{ enemyName }}</span>
                  <span class="f-lv">Lv.{{ enemyLevel }}</span>
                </div>
                <div class="hp-track"><div class="hp-fill" :style="{width: percent(enemyHp, enemyMaxHp)+'%'}"></div></div>
              </div>
              <div class="sprite-container">
                <img :src="enemySprite" class="char-img pixel-art" :class="{'shake-hit': isEnemyHit}" />
                <div class="shadow-oval"></div>
              </div>
            </div>

            <div class="fighter player">
              <div class="sprite-container">
                <img :src="mySprite" class="char-img pixel-art" :class="{'shake-hit': isMyHit}" />
                <div class="shadow-oval"></div>
              </div>
              <div class="hud player-hud">
                <div class="hud-row">
                  <span class="f-name">{{ characterName }}</span>
                  <span class="f-lv">Lv.{{ characterLevel }}</span>
                </div>
                <div class="hp-track"><div class="hp-fill" :style="{width: percent(myHp, myMaxHp)+'%'}"></div></div>
              </div>
            </div>

            <div v-if="isRevealing" class="clash-visual">
              <div class="move-card p1" :class="getAnimClass(displayedMyMove, displayedEnemyMove, true)">
                <i :class="getRpsIcon(displayedMyMove)"></i>
              </div>
              <div class="clash-spark">💥</div>
              <div class="move-card p2" :class="getAnimClass(displayedMyMove, displayedEnemyMove, false)">
                <i :class="getRpsIcon(displayedEnemyMove)"></i>
              </div>
              <div class="result-label">{{ lastResultText }}</div>
            </div>
          </div>

          <div class="battle-controls">
            <div class="chat-section">
              <div class="chat-history custom-scroll" ref="chatBoxRef">
                <div v-for="(msg, i) in matchMessages" :key="i" class="chat-bubble" :class="{'me': msg.isMe}">
                  <span class="sender">{{ msg.sender }}:</span> {{ msg.text }}
                </div>
              </div>
              <div class="chat-input-area">
                <input v-model="chatInput" @keyup.enter="sendPrivateChat" placeholder="Chat..." />
                <button @click="sendPrivateChat"><i class="fas fa-paper-plane"></i></button>
              </div>
            </div>

            <div class="actions-section">
              <div v-if="battlePhase === 'RPS_WAIT' && !isRevealing" class="rps-grid-simple">
                <button class="square-btn rock" @click="submitRps('ROCK')" :disabled="isActionPending"><i class="fas fa-hand-rock"></i><span>BÚA</span></button>
                <button class="square-btn paper" @click="submitRps('PAPER')" :disabled="isActionPending"><i class="fas fa-hand-paper"></i><span>BAO</span></button>
                <button class="square-btn scissor" @click="submitRps('SCISSORS')" :disabled="isActionPending"><i class="fas fa-hand-scissors"></i><span>KÉO</span></button>
                <button class="square-btn surrender" @click="handleSurrender"><i class="fas fa-flag"></i><span>THOÁT</span></button>
              </div>
              <div v-else class="status-overlay">
                <div class="spinner-box">
                  <i class="fas fa-yin-yang fa-spin"></i>
                  <span>{{ isRevealing ? 'ĐANG XỬ LÝ...' : 'CHỜ ĐỐI THỦ...' }}</span>
                </div>
                <button class="text-btn-tiny" @click="handleSurrender">Thoát</button>
              </div>
            </div>
          </div>
        </div>

      </div> </div> </div>
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
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif:ital,wght@0,400;0,700;1,400&family=Playfair+Display:wght@700;900&display=swap");
@import url('https://fonts.googleapis.com/css2?family=Roboto+Condensed:wght@700&display=swap');

.pixel-art { image-rendering: pixelated; }

/* ========================================= */
/* 🔥 GLOBAL LAYOUT (Đồng bộ với Village)    */
/* ========================================= */
:root {
  --wood-base: #3e2723;        
  --wood-card: #5d4037;        
  --wood-hover: #6d4c41;       
  --gold: #ffecb3;             
  --gold-accent: #ffd700;      
  --text-main: #fff8e1;        
  --text-dim: #d7ccc8;  
  --red-seal: #b71c1c;      
}

.wuxia-dashboard {
  min-height: 100vh;
  padding: 20px;
  font-family: "Noto Serif", serif;
  color: var(--text-main);
  position: relative;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* --- BACKGROUND SYSTEM --- */
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; pointer-events: none; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center bottom; opacity: 0.6; filter: sepia(10%) contrast(1.1); transition: background-image 0.5s ease-in-out; }
.wood-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); mix-blend-mode: multiply; transition: background 2s ease; }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle, transparent 60%, #1a100d 100%); }

/* --- DASHBOARD WRAPPER (Căn giữa) --- */
.dashboard-wrapper { position: relative; z-index: 10; max-width: 1000px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; height: calc(100vh - 40px); }

/* --- HEADER (Giống Village) --- */
.wood-panel {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(90deg, rgba(62, 39, 35, 0.95), rgba(93, 64, 55, 0.9));
  border: 2px solid #6d4c41; border-radius: 6px; padding: 10px 20px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  flex-shrink: 0;
}
.header-left { display: flex; flex-direction: column; gap: 4px; }
.server-tag { font-size: 0.75rem; color: var(--text-dim); display: flex; align-items: center; gap: 6px; }
.status-orb { width: 6px; height: 6px; background: #66bb6a; border-radius: 50%; box-shadow: 0 0 5px #66bb6a; }
.status-orb.red { background: #ff5252; box-shadow: 0 0 5px #ff5252; }
.greet-txt { font-size: 0.8rem; color: var(--gold); }
.char-name { font-family: "Playfair Display", serif; font-size: 1.8rem; margin: 0; color: #fff; text-shadow: 0 2px 4px rgba(0,0,0,0.6); line-height: 1; }
.header-right { display: flex; align-items: center; gap: 15px; }
.wealth-item { display: flex; align-items: center; gap: 6px; font-weight: bold; color: var(--gold-accent); font-size: 1rem; }
.gold-txt { color: #ffd700; }
.weather-seal { display: flex; align-items: center; gap: 8px; color: var(--text-dim); font-size: 1.5rem; }

/* ========================================= */
/* 🔥 CONTENT BOX (Chứa Lobby/Battle)        */
/* ========================================= */
.pvp-stage-container {
  flex: 1;
  background: rgba(40, 20, 10, 0.6); /* Nền mờ để nhìn thấy BG */
  border: 1px solid #5d4037;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 5px 20px rgba(0,0,0,0.5);
  display: flex;
}

/* --- LOBBY LAYOUT --- */
.lobby-layout { display: flex; width: 100%; height: 100%; gap: 15px; padding: 15px; }

/* Cột Sidebar (BXH) */
.sidebar-col { width: 280px; flex-shrink: 0; display: flex; flex-direction: column; }
.inner-wood-box { 
  background: rgba(30, 20, 15, 0.9); border: 1px solid #6d4c41; 
  flex: 1; border-radius: 4px; display: flex; flex-direction: column; overflow: hidden;
}
.box-header { 
  padding: 10px; text-align: center; font-weight: bold; color: var(--gold); 
  border-bottom: 1px solid #5d4037; background: rgba(62, 39, 35, 0.8);
}
.lb-mini-list { flex: 1; overflow-y: auto; padding: 10px; }
.lb-mini-item { display: flex; align-items: center; gap: 10px; padding: 8px; border-bottom: 1px dashed #444; }
.item-rank { width: 20px; font-weight: bold; text-align: center; color: #888; }
.item-rank.rank-1 { color: #ffd700; }
.item-rank.rank-2 { color: #c0c0c0; }
.item-rank.rank-3 { color: #cd7f32; }
.item-avatar.small { width: 32px; height: 32px; background: #111; border-radius: 50%; overflow: hidden; }
.item-avatar.small img { width: 100%; height: 100%; object-fit: contain; }
.item-info { flex: 1; font-size: 0.9rem; }
.item-name { color: #fff; font-weight: bold; }
.item-val { font-size: 0.8rem; color: var(--gold); }

/* Cột Main Lobby */
.main-col { flex: 1; display: flex; align-items: center; justify-content: center; }
.hero-card-wide { 
  width: 100%; max-width: 400px; 
  background: linear-gradient(135deg, var(--wood-card) 0%, var(--wood-base) 100%);
  border: 2px solid var(--gold); border-radius: 8px; padding: 30px; text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5); position: relative;
}

/* --- HERO VISUAL (Avatar to, Level Tag) --- */
.hero-visual { 
  display: flex; flex-direction: column; align-items: center; 
  margin-bottom: 25px; position: relative;
}
.avatar-frame.large { 
  width: 140px; height: 140px; margin: 0 auto; 
  border: 4px solid var(--gold); border-radius: 50%; 
  overflow: visible; background: radial-gradient(circle, #261815 60%, #000 100%); 
  box-shadow: 0 0 25px rgba(255, 215, 0, 0.25); position: relative; display: flex; justify-content: center; align-items: center;
}
.avatar-frame.large img { 
  width: 110%; height: 110%; object-fit: cover; transform: translateY(-5px); border-radius: 50%; z-index: 1;
}
.level-tag { 
  position: absolute; bottom: -12px; left: 50%; transform: translateX(-50%); z-index: 10;
  background: linear-gradient(180deg, #ffd700 0%, #ff8f00 100%); 
  border: 2px solid #fff; border-radius: 12px; padding: 4px 16px; box-shadow: 0 4px 6px rgba(0,0,0,0.5);
  display: flex; align-items: center; gap: 5px; min-width: 80px; justify-content: center;
}
.level-tag .lbl { font-size: 0.7rem; font-weight: bold; color: #5d4037; text-transform: uppercase; }
.level-tag .val { font-size: 1.1rem; font-weight: 900; color: #bf360c; text-shadow: 0 1px 0 rgba(255,255,255,0.5); }
.hero-name { margin-top: 25px; font-family: "Playfair Display"; color: #fff; font-size: 1.8rem; text-shadow: 0 2px 10px rgba(0,0,0,0.8); letter-spacing: 1px; }

/* --- ACTION BUTTONS (Lobby Only) --- */
.action-area .btn-group { display: flex; flex-direction: column; gap: 12px; margin-top: 10px; }
.wuxia-btn { padding: 12px 20px; border: none; cursor: pointer; font-weight: bold; border-radius: 6px; transition: 0.2s; font-family: "Noto Serif"; text-transform: uppercase; font-size: 1rem; position: relative; overflow: hidden; }

/* Nút TÌM ĐỐI THỦ */
.wuxia-btn.primary { 
  background: linear-gradient(135deg, #d32f2f 0%, #b71c1c 100%); color: #fff; border: 2px solid #ff5252; box-shadow: 0 4px 10px rgba(183, 28, 28, 0.5); font-size: 1.2rem; letter-spacing: 1px;
}
.wuxia-btn.primary:hover { background: linear-gradient(135deg, #f44336 0%, #c62828 100%); transform: translateY(-2px); box-shadow: 0 6px 15px rgba(211, 47, 47, 0.7); }
.wuxia-btn.primary:active { transform: translateY(1px); box-shadow: none; }

/* Nút CẬP NHẬT */
.wuxia-btn.gold-btn {
  background: linear-gradient(to bottom, #ffd700, #fbc02d); color: #3e2723; border: 2px solid #ffecb3; box-shadow: 0 4px 8px rgba(0,0,0,0.4);
}
.wuxia-btn.gold-btn:hover { background: linear-gradient(to bottom, #ffeb3b, #fdd835); transform: translateY(-2px); box-shadow: 0 6px 12px rgba(255, 215, 0, 0.3); }
.wuxia-btn.gold-btn:active { transform: translateY(1px); }

/* Nút hủy */
.wuxia-btn.outline { background: transparent; border: 1px solid var(--gold); color: var(--gold); }
.wuxia-btn.outline:hover { background: rgba(255, 236, 179, 0.1); }
.wuxia-btn.big { width: 100%; }

.searching-state { color: var(--gold); }
.spinner-radar { font-size: 2rem; margin-bottom: 10px; color: var(--gold-accent); }

.match-popup-embed { border-top: 1px solid rgba(255,255,255,0.2); margin-top: 15px; padding-top: 15px; animation: slideUp 0.3s; }
.enemy-preview-row { display: flex; align-items: center; gap: 15px; background: rgba(0,0,0,0.3); padding: 10px; border-radius: 4px; margin: 10px 0; }
.preview-avatar { width: 50px; height: 50px; background: #000; border-radius: 50%; overflow: hidden; }
.preview-avatar img { width: 100%; height: 100%; object-fit: contain; }
.preview-info { text-align: left; display: flex; flex-direction: column; }
.popup-actions-row { display: flex; gap: 10px; justify-content: center; }

@keyframes slideUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes btn-pulse { 0% { box-shadow: 0 0 0 0 rgba(211, 47, 47, 0.7); } 70% { box-shadow: 0 0 0 10px rgba(211, 47, 47, 0); } 100% { box-shadow: 0 0 0 0 rgba(211, 47, 47, 0); } }
.pulse-anim { animation: btn-pulse 2s infinite; }

/* --- BATTLE ARENA (Framed) --- */
.battle-arena-frame { 
  width: 100%; height: 100%; display: flex; flex-direction: column; position: relative; background: #000;
}
.arena-background { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0.5; }

/* Top UI */
.log-overlay-btn { position: absolute; top: 10px; left: 10px; z-index: 20; background: rgba(0,0,0,0.6); padding: 5px 10px; border: 1px solid var(--gold); color: var(--gold); cursor: pointer; border-radius: 4px; }
.floating-log-panel { position: absolute; top: 40px; left: 10px; width: 300px; height: 200px; background: rgba(0,0,0,0.9); border: 1px solid var(--gold); z-index: 30; padding: 10px; overflow-y: auto; color: #fff; font-size: 0.85rem; }
.top-timer-display { position: absolute; top: 10px; left: 50%; transform: translateX(-50%); width: 50px; height: 50px; background: rgba(0,0,0,0.7); border: 2px solid var(--gold); border-radius: 50%; display: flex; align-items: center; justify-content: center; z-index: 20; font-weight: bold; font-size: 1.2rem; color: #fff; }
.top-timer-display.urgent { border-color: #f44336; color: #f44336; animation: pulse 0.5s infinite; }

/* Stage */
.fighters-stage { flex: 1; position: relative; z-index: 10; display: flex; justify-content: space-between; padding: 20px 40px; align-items: center; }
.fighter { display: flex; flex-direction: column; width: 180px; align-items: center; position: relative; }
.fighter.enemy { align-items: flex-end; }
.fighter.player { align-items: flex-start; }

.sprite-container { width: 120px; height: 120px; position: relative; display: flex; align-items: center; justify-content: center; }
.char-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); transition: 0.2s; z-index: 2; }
.enemy .char-img { transform: scale(1.5) scaleX(-1); }
.shadow-oval { position: absolute; bottom: 5px; width: 80%; height: 10px; background: rgba(0,0,0,0.5); border-radius: 50%; filter: blur(2px); z-index: 1; }

.hud { width: 100%; background: rgba(0,0,0,0.6); padding: 5px; border-radius: 4px; margin-top: 5px; color: #fff; font-size: 0.85rem; }
.hud-row { display: flex; justify-content: space-between; margin-bottom: 3px; }
.hp-track { width: 100%; height: 5px; background: #333; border-radius: 2px; }
.hp-fill { height: 100%; background: #d32f2f; width: 100%; transition: width 0.3s; }

.clash-visual { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; gap: 20px; pointer-events: none; z-index: 50; }
.move-card { width: 80px; height: 80px; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; color: #333; border: 3px solid #000; }
.clash-spark { font-size: 3rem; animation: pulse 0.1s infinite; }
.result-label { position: absolute; bottom: 100px; font-size: 2rem; font-weight: bold; color: var(--gold-accent); text-shadow: 0 0 10px #000; }

/* Controls */
.battle-controls { height: 160px; background: #1a1a1a; border-top: 2px solid #5d4037; display: flex; z-index: 20; position: relative; }
.chat-section { flex: 1.2; border-right: 1px solid #333; display: flex; flex-direction: column; }
.chat-history { flex: 1; padding: 10px; overflow-y: auto; font-size: 0.85rem; display: flex; flex-direction: column; gap: 4px; }
.chat-bubble { padding: 4px 8px; background: #333; border-radius: 4px; align-self: flex-start; color: #ccc; max-width: 90%; }
.chat-bubble.me { align-self: flex-end; background: #2e7d32; color: #fff; }
.chat-input-area { display: flex; border-top: 1px solid #333; }
.chat-input-area input { flex: 1; background: #000; border: none; color: #fff; padding: 8px; outline: none; }
.chat-input-area button { background: #5d4037; border: none; color: #fff; padding: 0 15px; cursor: pointer; }

/* --- ACTIONS SECTION (Battle) --- */
.actions-section { flex: 1; padding: 15px; display: flex; align-items: center; justify-content: center; }

/* [FIX] REVERTED: Simple Grid & Button Styles */
.rps-grid-simple { 
  display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 8px; width: 100%; height: 100%; 
}

.square-btn { 
  background: #222; 
  border: 1px solid #444; 
  color: #eee; 
  cursor: pointer; 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: center; 
  font-weight: bold;
  border-radius: 4px;
  transition: 0.2s;
}
.square-btn i { font-size: 1.5rem; margin-bottom: 2px; }
.square-btn:hover:not(:disabled) { background: #333; border-color: var(--gold); color: var(--gold); }
.square-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.status-overlay { display: flex; flex-direction: column; align-items: center; gap: 10px; color: #ccc; width: 100%; }
.text-btn-tiny { background: rgba(255,255,255,0.1); border: 1px solid #555; color: #aaa; font-size: 0.75rem; padding: 5px 15px; cursor: pointer; border-radius: 20px; transition: 0.3s; }
.text-btn-tiny:hover { background: #d32f2f; color: #fff; border-color: #d32f2f; }

/* Custom Scroll */
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #555; }

/* Animations */
@keyframes pulse { 0% { transform: scale(1); } 50% { transform: scale(1.1); } 100% { transform: scale(1); } }
.shake-hit { animation: shake 0.4s cubic-bezier(.36,.07,.19,.97) both; filter: sepia(1) saturate(5) hue-rotate(-50deg); }
@keyframes shake { 10%, 90% { transform: translate3d(-1px, 0, 0); } 20%, 80% { transform: translate3d(2px, 0, 0); } 30%, 50%, 70% { transform: translate3d(-4px, 0, 0); } 40%, 60% { transform: translate3d(4px, 0, 0); } }
.anim-smash-win { transform: scale(1.3); border-color: #ffd700; z-index: 10; box-shadow: 0 0 20px rgba(255,215,0,0.5); }
.anim-lose { opacity: 0.5; transform: scale(0.8); filter: grayscale(1); }
</style>