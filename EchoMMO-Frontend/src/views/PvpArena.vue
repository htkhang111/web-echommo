<template>
  <div class="pvp-contained-layout">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${currentBg})` }"></div>
      <div class="vignette"></div>
      <div class="particles"></div>
    </div>

    <div class="content-wrapper">
      
      <div v-if="gameState !== 'BATTLE'" class="lobby-grid">
        
        <div class="glass-panel left-col">
          <div class="panel-header">
            <h3><i class="fas fa-trophy"></i> CAO THỦ BẢNG</h3>
          </div>
          
          <div v-if="lbStore.loadingPvp" class="loading-state">
            <div class="spinner-ring"></div>
            <span>Đang tải...</span>
          </div>
          
          <div v-else class="ranking-list custom-scroll">
            <div 
              v-for="(entry, index) in lbStore.topPvp" 
              :key="index" 
              class="rank-card"
              :class="{ 'rank-1': index === 0, 'rank-2': index === 1, 'rank-3': index === 2 }"
            >
              <div class="rank-badge">
                 <span v-if="index === 0">👑</span>
                 <span v-else-if="index === 1">🥈</span>
                 <span v-else-if="index === 2">🥉</span>
                 <span v-else>#{{ index + 1 }}</span>
              </div>

              <div class="rank-sprite-container">
                <img 
                  :src="resolveLeaderboardAvatar(entry)" 
                  class="pixel-art mini-sprite" 
                  :class="{ 'is-real-photo': isRealPhoto(entry) }"
                  @error="$event.target.src = getSkinSprite('skin_yasou')"
                />
                <div class="lv-tag">{{ entry.level || 1 }}</div>
              </div>

              <div class="rank-info">
                <div class="r-name">{{ entry.name }}</div>
                <div class="r-stats">
                   <span class="tag-title">{{ entry.rankTitle || 'Tân Thủ' }}</span>
                   <span class="tag-rep"><i class="fas fa-star"></i> {{ entry.reputation }}</span>
                </div>
              </div>
            </div>
            
            <div v-if="!lbStore.topPvp?.length" class="empty-msg">
               Chưa có dữ liệu
            </div>
          </div>
        </div>

        <div class="right-col">
           <div class="hero-showcase">
              <div class="hero-glow"></div>
              <img :src="mySprite" class="hero-big-sprite pixel-art" />
              <div class="hero-details">
                <h1 class="hero-title">{{ characterName }}</h1>
                <div class="hero-badges">
                  <span class="badge-lv">Cấp {{ characterLevel }}</span>
                </div>
              </div>
           </div>

           <div class="action-dock">
              <template v-if="gameState === 'LOBBY'">
                <button class="btn-epic pulse-glow" @click="toggleSearch">
                  <span class="btn-icon"><i class="fas fa-swords"></i></span>
                  <span class="btn-text">TÌM ĐỐI THỦ</span>
                </button>
                <button class="btn-refresh" @click="manualRefresh" title="Làm mới">
                  <i class="fas fa-sync-alt"></i>
                </button>
              </template>

              <template v-else-if="gameState === 'SEARCHING'">
                <div class="searching-box">
                  <div class="radar-scan"></div>
                  <div class="search-text">Đang tìm kiếm... {{ searchTimer }}s</div>
                  <button class="btn-cancel" @click="cancelSearch">HỦY</button>
                </div>
              </template>

              <template v-if="gameState === 'MATCH_FOUND'">
                <div class="match-found-alert pop-in">
                  <div class="found-text">⚠️ ĐÃ TÌM THẤY ĐỐI THỦ</div>
                  <div class="enemy-preview">
                     <img :src="enemySprite" class="pixel-art enemy-sprite-preview" />
                     <div class="enemy-info-box">
                       <div class="e-name">{{ enemyName }}</div>
                       <div class="e-lv">Cấp {{ enemyLevel }}</div>
                     </div>
                  </div>
                  <button class="btn-accept white-btn" @click="acceptMatch" :disabled="hasAccepted">
                    {{ hasAccepted ? "ĐANG CHỜ..." : `CHIẾN NGAY (${acceptTimer}s)` }}
                  </button>
                </div>
              </template>
           </div>
        </div>
      </div>

      <div v-else class="battle-arena">
        <div class="battle-hud">
          <div class="hud-box player-side">
            <div class="avatar-box"><img :src="mySprite" class="pixel-art" /></div>
            <div class="bars-container">
              <div class="name-row">
                 <span class="b-name">{{ characterName }}</span>
                 <span class="b-lv">Lv.{{ characterLevel }}</span>
              </div>
              <div class="hp-bar-frame">
                <div class="hp-fill player" :style="{ width: percent(myHp, myMaxHp) + '%' }"></div>
                <span class="hp-text">{{ myHp }}/{{ myMaxHp }}</span>
              </div>
            </div>
          </div>

          <div class="battle-timer" :class="{ warning: turnTimer <= 10 }">
             {{ turnTimer }}
          </div>

          <div class="hud-box enemy-side">
            <div class="bars-container">
              <div class="name-row">
                 <span class="b-lv">Lv.{{ enemyLevel }}</span>
                 <span class="b-name">{{ enemyName }}</span>
              </div>
              <div class="hp-bar-frame">
                <div class="hp-fill enemy" :style="{ width: percent(enemyHp, enemyMaxHp) + '%' }"></div>
                 <span class="hp-text">{{ enemyHp }}/{{ enemyMaxHp }}</span>
              </div>
            </div>
            <div class="avatar-box"><img :src="enemySprite" class="pixel-art enemy-flip" /></div>
          </div>
        </div>

        <div class="battle-stage">
           <div class="fighter-display left">
              <img :src="mySprite" class="pixel-art char-anim" :class="{ 'hit-shake': isMyHit }" />
              <div class="shadow"></div>
           </div>

           <div class="clash-zone" v-if="isRevealing">
              <div class="move-icon p1" :class="getAnimClass(displayedMyMove, displayedEnemyMove, true)">
                 <i :class="getRpsIcon(displayedMyMove)"></i>
              </div>
              <div class="vs-flash">💥</div>
              <div class="move-icon p2" :class="getAnimClass(displayedMyMove, displayedEnemyMove, false)">
                 <i :class="getRpsIcon(displayedEnemyMove)"></i>
              </div>
              <div class="damage-text" :class="getResultClass(lastResultText)">{{ lastResultText }}</div>
           </div>

           <div class="fighter-display right">
              <img :src="enemySprite" class="pixel-art char-anim enemy-flip" :class="{ 'hit-shake': isEnemyHit }" />
              <div class="shadow"></div>
           </div>
        </div>

        <div class="battle-deck">
           <div class="mini-chat glass-panel">
              <div class="chat-messages custom-scroll" ref="chatBoxRef">
                <div v-for="(msg, i) in matchMessages" :key="i" class="msg-line" :class="{ 'me': msg.isMe }">
                   <span class="sender">{{ msg.sender }}:</span> {{ msg.text }}
                </div>
              </div>
              <div class="chat-input-row">
                <input v-model="chatInput" @keyup.enter="sendPrivateChat" placeholder="Chat..." />
              </div>
           </div>

           <div class="command-center">
              <div v-if="battlePhase !== 'RPS_WAIT' || isRevealing" class="status-badge">
                 <i class="fas fa-yin-yang fa-spin"></i>
                 {{ isRevealing ? "ĐANG TÍNH..." : "CHỜ ĐỐI THỦ..." }}
              </div>
              <div v-else class="rps-buttons">
                 <button class="rps-btn rock" @click.prevent="submitRps('ROCK')" :disabled="isActionPending">
                    <div class="icon-circle"><i class="fas fa-hand-rock"></i></div>
                    <span>BÚA</span>
                 </button>
                 <button class="rps-btn paper" @click.prevent="submitRps('PAPER')" :disabled="isActionPending">
                    <div class="icon-circle"><i class="fas fa-hand-paper"></i></div>
                    <span>BAO</span>
                 </button>
                 <button class="rps-btn scissors" @click.prevent="submitRps('SCISSORS')" :disabled="isActionPending">
                    <div class="icon-circle"><i class="fas fa-hand-scissors"></i></div>
                    <span>KÉO</span>
                 </button>
              </div>
           </div>

           <div class="util-deck">
              <button class="util-btn log" @click="showLog = !showLog" title="Nhật ký">
                 <i class="fas fa-book"></i>
              </button>
              <button class="util-btn surrender" @click="confirmSurrender" title="Đầu hàng">
                 <i class="fas fa-flag"></i>
              </button>
           </div>
        </div>

        <transition name="fade">
           <div v-if="showLog" class="log-modal glass-panel">
              <div class="log-head">NHẬT KÝ CHIẾN ĐẤU <span @click="showLog=false">✕</span></div>
              <div class="log-body custom-scroll">
                 <div v-for="(log, i) in battleLogs.slice().reverse()" :key="i" class="log-item">» {{ log }}</div>
              </div>
           </div>
        </transition>
      </div>
    </div>

    <transition name="zoom">
      <div v-if="showSurrenderModal" class="modal-overlay">
         <div class="modal-box danger-theme">
            <div class="modal-header">⚠️ CẢNH BÁO ĐẦU HÀNG</div>
            <div class="modal-body">
               <p>Đại hiệp có chắc chắn muốn bỏ cuộc?</p>
               <p class="sub-text">(Sẽ bị xử THUA ngay lập tức)</p>
            </div>
            <div class="modal-actions">
               <button class="btn-confirm-surrender" @click="handleSurrenderNow">🏳️ CHẤP NHẬN THUA</button>
               <button class="btn-cancel-surrender" @click="showSurrenderModal = false">QUAY LẠI</button>
            </div>
         </div>
      </div>
    </transition>

    <transition name="zoom">
      <div v-if="showEndGameModal" class="modal-overlay">
        <div class="modal-box result-box" :class="isWin ? 'win-theme' : 'lose-theme'">
           <div class="modal-header">{{ endGameTitle }}</div>
           
           <div class="modal-body">
              <p class="end-msg">{{ endGameMessage }}</p>
              
              <div class="reward-pill" v-if="gainedReputation !== 0">
                 <span class="label">Danh Vọng:</span>
                 <span class="value" :class="gainedReputation > 0 ? 'plus' : 'minus'">
                    {{ gainedReputation > 0 ? '+' : '' }}{{ gainedReputation }}
                 </span>
              </div>

              <div class="auto-back-progress">
                 <div class="progress-bar" :style="{ width: (autoBackTimer / 5) * 100 + '%' }"></div>
                 <span class="progress-text">Tự động thoát sau {{ autoBackTimer }}s...</span>
              </div>
           </div>

           <button class="btn-back-home big-btn" @click="closeEndGameModal">
             <i class="fas fa-door-open"></i> RỜI KHỎI ĐÂY NGAY
           </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from "vue";
import { useAuthStore } from "../stores/authStore";
import { useLeaderboardStore } from "../stores/leaderboardStore";
import { CHARACTER_SKINS } from "../utils/assetHelper";
import axios from "axios";

const API_URL = "http://localhost:8080/api/pvp";
const authStore = useAuthStore();
const lbStore = useLeaderboardStore();

// --- 1. ASSET HELPERS ---
const getSkinSprite = (skinId) => {
  if (!skinId) return CHARACTER_SKINS["skin_yasou"].sprites.idle;
  if (CHARACTER_SKINS[skinId]) return CHARACTER_SKINS[skinId].sprites.idle;
  return CHARACTER_SKINS["skin_yasou"].sprites.idle;
};

const resolveLeaderboardAvatar = (entry) => {
  if (entry.profileImageUrl && (entry.profileImageUrl.includes("/") || entry.profileImageUrl.includes("data:"))) {
    return entry.profileImageUrl.startsWith("/uploads/") ? `http://localhost:8080${entry.profileImageUrl}` : entry.profileImageUrl;
  }
  const skinId = entry.avatarUrl || "skin_yasou";
  return getSkinSprite(skinId);
};

const isRealPhoto = (entry) => {
  const img = entry.profileImageUrl || entry.avatar || entry.avatarUrl; 
  if (!img) return false;
  return img.includes("/") || img.includes("data:");
};

// --- DATA ---
const mySprite = computed(() => {
  const val = authStore.user?.avatarUrl || "skin_yasou";
  if (val.includes("/")) return getSkinSprite("skin_yasou"); 
  return getSkinSprite(val);
});

const enemySprite = computed(() => {
  const val = enemySkinId.value || "skin_yasou";
  if (val.includes("/")) return getSkinSprite("skin_yasou");
  return getSkinSprite(val);
});

const characterName = computed(() => authStore.character?.name || "Đại Hiệp");
const characterLevel = computed(() => authStore.character?.level || 1);
const characterReputation = computed(() => authStore.character?.reputation || 0);
const characterRankTitle = computed(() => authStore.character?.rankTitle || "Vô Danh");

const currentBg = computed(() => {
  if (gameState.value === "BATTLE") return "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1920&auto=format&fit=crop"; 
  return "https://images.unsplash.com/photo-1518544806352-a5c36195968a?q=80&w=1920&auto=format&fit=crop"; 
});

const gameState = ref("LOBBY");
const matchId = ref(null);
const battlePhase = ref("RPS_WAIT");
const turnTimer = ref(30);
const lastResultText = ref("");
const hasAccepted = ref(false);
const showLog = ref(false);
const isActionPending = ref(false);
const searchTimer = ref(0);
const acceptTimer = ref(10);
const ignoredMatchIds = ref(new Set());
const showEndGameModal = ref(false);
const showSurrenderModal = ref(false);
const isWin = ref(false);
const endGameTitle = ref("");
const endGameMessage = ref("");
const autoBackTimer = ref(5);
let autoBackInterval = null;
const gainedReputation = ref(0);
const enemyName = ref("Đối thủ");
const enemyLevel = ref(1);
const enemySkinId = ref(null);
const myHp = ref(100);
const myMaxHp = ref(100);
const enemyHp = ref(100);
const enemyMaxHp = ref(100);
const isMyHit = ref(false);
const isEnemyHit = ref(false);
const battleLogs = ref([]);
const matchMessages = ref([]);
const chatInput = ref("");
const chatBoxRef = ref(null);
const displayedMyMove = ref(null);
const displayedEnemyMove = ref(null);
const lastProcessedTurn = ref(-1);
const isRevealing = ref(false);
const userSurrendered = ref(false); 

let pollId, searchInterval, acceptInterval, turnTimerInterval, revealTimeout;
const getHeaders = () => ({ Authorization: `Bearer ${authStore.token}` });
const percent = (c, m) => (!m || m <= 0) ? 0 : Math.max(0, Math.min(100, (c / m) * 100));
const checkIsPlayer1 = (data) => Number(authStore.character?.charId || 0) === Number(data.p1Id);

const syncBasicInfo = (data) => {
  const isP1 = checkIsPlayer1(data);
  enemyName.value = (isP1 ? data.p2Name : data.p1Name) || "Đối thủ";
  enemyLevel.value = (isP1 ? data.p2Level : data.p1Level) || 1;
  enemySkinId.value = isP1 ? data.p2AvatarUrl : data.p1AvatarUrl;
};

const manualRefresh = async () => { await authStore.fetchCharacter(); await lbStore.fetchPvPBoard(); }

const resetToLobby = () => {
  gameState.value = "LOBBY";
  matchId.value = null;
  clearAllIntervals();
  battleLogs.value = [];
  matchMessages.value = [];
  battlePhase.value = "RPS_WAIT";
  isActionPending.value = false;
  isRevealing.value = false;
  lastProcessedTurn.value = -1;
  hasAccepted.value = false;
  showEndGameModal.value = false;
  showSurrenderModal.value = false;
  userSurrendered.value = false;
  gainedReputation.value = 0;
};

const clearAllIntervals = () => {
  if (searchInterval) clearInterval(searchInterval);
  if (acceptInterval) clearInterval(acceptInterval);
  if (turnTimerInterval) clearInterval(turnTimerInterval);
  if (pollId) { clearInterval(pollId); pollId = null; }
  if (revealTimeout) clearTimeout(revealTimeout);
  if (autoBackInterval) clearInterval(autoBackInterval);
};

const toggleSearch = async () => {
  resetToLobby();
  await authStore.fetchCharacter();
  if (authStore.character) localStorage.setItem('pvp_start_rep', authStore.character.reputation || 0);
  gameState.value = "SEARCHING";
  searchTimer.value = 0;
  searchInterval = setInterval(() => searchTimer.value++, 1000);
  try { await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() }); startPolling(); } catch (e) { resetToLobby(); }
};

const cancelSearch = async () => { try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch (e) {} resetToLobby(); };
const acceptMatch = async () => { if (!matchId.value) return; hasAccepted.value = true; try { await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) {} };
const submitRps = async (move) => { if (isActionPending.value || isRevealing.value) return; isActionPending.value = true; battlePhase.value = "RPS_PENDING"; try { await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() }); } catch (e) { isActionPending.value = false; } };

// [FIX] LOGIC ĐẦU HÀNG NGAY LẬP TỨC
const confirmSurrender = () => { showSurrenderModal.value = true; };
const handleSurrenderNow = async () => {
  showSurrenderModal.value = false;
  userSurrendered.value = true; 
  
  // 1. Gửi lệnh lên Server (để server xử lý cho đối thủ thắng)
  try { await axios.post(`${API_URL}/surrender`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) {}

  // 2. [QUAN TRỌNG] Tự xử thua ngay tại Client cho mình (Không chờ Polling)
  const dummyData = { winnerId: -1, lastLog: "Bạn đã đầu hàng" }; // Dữ liệu giả để trigger modal
  handleGameFinished(dummyData, true); // true = force surrender
};

const sendPrivateChat = async () => { if (!chatInput.value.trim()) return; try { await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); } catch (e) {} chatInput.value = ""; };

const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data;
      if (!data) return;
      if (data.matchId && ignoredMatchIds.value.has(Number(data.matchId))) return;

      if (data.status === "FINISHED") {
        if (gameState.value === 'LOBBY' || gameState.value === 'SEARCHING') {
            if (data.matchId) ignoredMatchIds.value.add(Number(data.matchId));
            return;
        }
        if (!showEndGameModal.value) handleGameFinished(data);
        return;
      }

      if (data.status === "PENDING") {
        if (gameState.value !== "MATCH_FOUND" && gameState.value !== "BATTLE") {
          gameState.value = "MATCH_FOUND";
          matchId.value = data.matchId;
          if (searchInterval) clearInterval(searchInterval);
          syncBasicInfo(data);
          if (!hasAccepted.value) {
            acceptTimer.value = 10;
            if (acceptInterval) clearInterval(acceptInterval);
            acceptInterval = setInterval(() => {
              acceptTimer.value--;
              if (acceptTimer.value <= 0 && !hasAccepted.value) resetToLobby();
            }, 1000);
          }
        }
      } else if (data.status === "ACTIVE") {
        if (gameState.value !== "BATTLE") {
          gameState.value = "BATTLE";
          matchId.value = data.matchId;
          if (acceptInterval) clearInterval(acceptInterval);
          startTurnTimer();
        }
        syncBattleData(data);
      }
    } catch (e) {}
  }, 1000);
};

// [FIX] LOGIC HIỆN BẢNG KẾT QUẢ CHO CẢ 2 BÊN
const handleGameFinished = (data, forceSurrender = false) => {
    // Nếu đã hiện bảng rồi thì thôi
    if (showEndGameModal.value && !forceSurrender) return;

    const myCharId = Number(authStore.character?.charId || 0);
    const serverWinnerId = data.winnerId !== null ? Number(data.winnerId) : null;
    
    // Nếu không phải forceSurrender thì sync lại lần cuối cho chắc
    if (!forceSurrender) syncBattleData(data);
    
    const delayTime = isRevealing.value ? 3000 : 500;

    setTimeout(async () => {
        clearAllIntervals();
        await authStore.fetchCharacter(); // Update lại Danh Vọng mới
        
        const currentRep = authStore.character?.reputation || 0;
        const savedStartRep = Number(localStorage.getItem('pvp_start_rep') || currentRep);
        gainedReputation.value = currentRep - savedStartRep;

        // Xác định thắng thua
        isWin.value = (serverWinnerId === myCharId);
        
        const log = (data.lastLog || "").toLowerCase();
        // Kiểm tra xem có phải đầu hàng không (dựa vào log hoặc cờ userSurrendered)
        const isSurrenderContext = log.includes("đầu hàng") || log.includes("bỏ chạy") || userSurrendered.value || forceSurrender;

        // --- LOGIC TITLE & MESSAGE ---
        if (userSurrendered.value || forceSurrender) {
            // Trường hợp 1: Mình bấm đầu hàng (Mình thua)
            isWin.value = false;
            endGameTitle.value = "ĐẦU HÀNG";
            endGameMessage.value = "Bạn đã chấp nhận thất bại!";
        } 
        else if (isWin.value) {
            // Trường hợp 2: Mình thắng
            endGameTitle.value = "CHIẾN THẮNG";
            if (isSurrenderContext) {
                 // Nếu mình thắng và log có chữ "đầu hàng" => Địch đầu hàng
                 endGameMessage.value = "Đối thủ đã sợ hãi bỏ chạy!";
            } else {
                 endGameMessage.value = "Bạn đã hạ gục đối thủ!";
            }
        } 
        else {
            // Trường hợp 3: Mình thua (bị đánh chết)
            endGameTitle.value = "THẤT BẠI";
            endGameMessage.value = "Bạn đã bị đánh bại...";
        }

        if (serverWinnerId === null && !forceSurrender) {
             endGameTitle.value = "HÒA";
             endGameMessage.value = "Lưỡng bại câu thương!";
        }

        showEndGameModal.value = true;
        lbStore.fetchPvPBoard();
        if (data.matchId) ignoredMatchIds.value.add(Number(data.matchId));

        // Timer 5s
        autoBackTimer.value = 5;
        if (autoBackInterval) clearInterval(autoBackInterval);
        autoBackInterval = setInterval(() => {
            autoBackTimer.value--;
            if (autoBackTimer.value <= 0) {
                closeEndGameModal();
            }
        }, 1000);
    }, forceSurrender ? 0 : delayTime); // Nếu force surrender thì hiện ngay ko delay
};

const closeEndGameModal = () => {
  if (autoBackInterval) clearInterval(autoBackInterval);
  showEndGameModal.value = false;
  resetToLobby();
};

const getRpsIcon = (m) => ({ ROCK: "fas fa-hand-rock", PAPER: "fas fa-hand-paper", SCISSORS: "fas fa-hand-scissors" })[m] || "fas fa-question";
const getAnimClass = (my, en, isMe) => { if (!my || !en) return ""; if (my === en) return "anim-clash"; let iWin = (my === "ROCK" && en === "SCISSORS") || (my === "SCISSORS" && en === "PAPER") || (my === "PAPER" && en === "ROCK"); return isMe ? (iWin ? "anim-attack-left" : "anim-hit") : (!iWin ? "anim-attack-right" : "anim-hit"); };
const getResultClass = (t) => t === "NÉ!" ? "res-dodge" : (t === "BẠO KÍCH!" ? "res-crit" : (t === "HÒA!" ? "res-clash" : "res-normal"));

onMounted(() => { authStore.fetchCharacter(); lbStore.fetchPvPBoard(); startPolling(); window.addEventListener("keydown", (e) => { if (document.activeElement.tagName !== "INPUT" && e.key.toLowerCase() === "l") { e.preventDefault(); showLog.value = !showLog.value; } }); });
onUnmounted(() => clearAllIntervals());
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&family=Noto+Serif+SC:wght@500;700&display=swap");

:root {
  --gold: #ffc107; --gold-glow: #ffeb3b; --red: #ff5252; --dark-bg: rgba(20, 10, 5, 0.85);
}
.pixel-art { image-rendering: pixelated; }

/* LAYOUT */
.pvp-contained-layout { width: 100%; height: 100%; font-family: 'Noto Serif SC', serif; color: white; overflow: hidden; position: relative; background: #000; box-sizing: border-box; }
.bg-layer { position: absolute; inset: 0; z-index: 0; pointer-events: none; }
.mountain-bg { width: 100%; height: 100%; background-size: cover; background-position: center; transition: background-image 0.5s; opacity: 0.6; }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle at center, transparent 40%, #000 100%); }
.content-wrapper { position: relative; z-index: 2; height: 100%; padding: 20px 30px; display: flex; flex-direction: column; }

/* LOBBY */
.lobby-grid { display: grid; grid-template-columns: 380px 1fr; gap: 30px; height: 100%; width: 100%; }
.glass-panel { background: rgba(18, 24, 33, 0.95); border: 1px solid #2d3748; border-radius: 12px; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5); display: flex; flex-direction: column; overflow: hidden; height: 100%; }
.panel-header { padding: 15px; background: linear-gradient(90deg, #1a202c, #2d3748); border-bottom: 2px solid #4a5568; color: #fbbf24; font-family: 'Cinzel', serif; text-align: center; font-size: 1.2rem; letter-spacing: 1px; }
.ranking-list { padding: 15px; flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
.rank-card { display: flex; align-items: center; gap: 12px; padding: 10px; background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 8px; transition: all 0.2s ease; }
.rank-card:hover { background: rgba(255, 255, 255, 0.1); transform: translateX(5px); border-color: rgba(255, 255, 255, 0.2); }
.rank-card.rank-1 { background: linear-gradient(90deg, rgba(251, 191, 36, 0.15), transparent); border: 1px solid rgba(251, 191, 36, 0.5); }
.rank-card.rank-2 { background: linear-gradient(90deg, rgba(226, 232, 240, 0.1), transparent); border: 1px solid rgba(226, 232, 240, 0.4); }
.rank-card.rank-3 { background: linear-gradient(90deg, rgba(245, 158, 11, 0.1), transparent); border: 1px solid rgba(245, 158, 11, 0.4); }
.rank-badge { width: 30px; text-align: center; font-weight: bold; font-size: 1.1rem; color: #718096; }
.rank-sprite-container { width: 45px; height: 45px; background: #1a100d; border: 1px solid #4a5568; border-radius: 6px; overflow: hidden; position: relative; display: flex; justify-content: center; align-items: center; box-shadow: inset 0 0 10px #000; }
.mini-sprite { height: 90%; width: auto; object-fit: contain; transform: translateY(2px); }
.mini-sprite.is-real-photo { width: 100%; height: 100%; object-fit: cover; transform: none; }
.lv-tag { position: absolute; bottom: 0; right: 0; background: #2d3748; color: #fff; font-size: 0.6rem; padding: 1px 4px; }
.rank-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.r-name { color: #e2e8f0; font-weight: 600; }
.r-stats { font-size: 0.8rem; display: flex; gap: 10px; }
.tag-rep { color: #fbbf24; }

.right-col { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; gap: 20px; }
.hero-showcase { display: flex; flex-direction: column; align-items: center; position: relative; }
.hero-glow { position: absolute; width: 400px; height: 400px; background: radial-gradient(circle, rgba(255,193,7,0.15) 0%, transparent 70%); top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: -1; animation: breathe 3s infinite; }
.hero-big-sprite { height: 260px; filter: drop-shadow(0 0 20px rgba(0,0,0,0.8)); transition: 0.3s; }
.hero-title { font-family: 'Cinzel', serif; font-size: 2.2rem; margin: 10px 0 5px; text-shadow: 0 2px 10px #000; background: -webkit-linear-gradient(#fff, #ccc); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.hero-badges { display: flex; gap: 10px; }
.badge-lv { background: var(--red); padding: 2px 10px; border-radius: 4px; font-weight: bold; font-size: 0.8rem; box-shadow: 0 0 10px rgba(211,47,47,0.5); }

.action-dock { width: 100%; display: flex; flex-direction: column; align-items: center; gap: 15px; }
.btn-epic { background: linear-gradient(45deg, #8e2de2, #4a00e0); border: 1px solid rgba(255,255,255,0.3); padding: 12px 50px; font-size: 1.4rem; color: white; font-family: 'Cinzel', serif; cursor: pointer; border-radius: 50px; display: flex; align-items: center; gap: 12px; box-shadow: 0 0 20px rgba(74, 0, 224, 0.4); transition: 0.3s; }
.btn-epic:hover { transform: translateY(-3px) scale(1.05); box-shadow: 0 0 40px rgba(74, 0, 224, 0.8); }
.pulse-glow { animation: pulse 2s infinite; }
.btn-refresh { width: 40px; height: 40px; border-radius: 50%; background: transparent; border: 1px solid #555; color: #888; cursor: pointer; transition: 0.2s; }
.btn-refresh:hover { color: #fff; border-color: #fff; transform: rotate(180deg); }
.searching-box { background: rgba(0, 0, 0, 0.6); padding: 15px 30px; border-radius: 12px; border: 1px solid var(--gold); display: flex; flex-direction: column; align-items: center; gap: 10px; animation: popIn 0.3s; }
.btn-cancel { background: transparent; border: 1px solid var(--red); color: var(--red); padding: 5px 20px; border-radius: 20px; cursor: pointer; transition: 0.2s; }

/* MATCH FOUND: ĐỎ VÀ TRẮNG */
.match-found-alert { background: rgba(20, 10, 5, 0.95); border: 2px solid var(--gold); padding: 20px; border-radius: 12px; display: flex; flex-direction: column; align-items: center; gap: 15px; box-shadow: 0 0 50px rgba(0,0,0,0.8); min-width: 300px; animation: popIn 0.3s; }
.found-text { color: var(--red); font-weight: bold; letter-spacing: 2px; animation: flash 1s infinite; font-size: 1.2rem; }
.enemy-preview { display: flex; align-items: center; gap: 15px; background: rgba(255,255,255,0.05); padding: 10px; border-radius: 8px; width: 100%; border: 1px solid #444; }
.enemy-sprite-preview { width: 60px; height: 60px; background: #000; border: 1px solid #d32f2f; border-radius: 50%; object-fit: contain; }
.enemy-info-box { flex: 1; text-align: left; }
.e-name { color: #ff5252; font-weight: 900; font-size: 1.3rem; text-shadow: 0 0 10px rgba(255, 82, 82, 0.5); }
.e-lv { color: #aaa; font-size: 0.9rem; }
.white-btn { background: #fff !important; color: #000 !important; border: 2px solid #ccc !important; font-weight: 900 !important; box-shadow: 0 0 20px rgba(255,255,255,0.5); font-size: 1.2rem !important; padding: 12px 30px; border-radius: 50px; cursor: pointer; transition: 0.2s; text-transform: uppercase; }
.white-btn:hover { transform: scale(1.05); box-shadow: 0 0 30px rgba(255,255,255,0.8); }

/* BATTLE ARENA */
.battle-arena { width: 100%; height: 100%; position: relative; overflow: hidden; border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; background: rgba(0,0,0,0.3); }
.battle-hud { position: absolute; top: 15px; width: 100%; padding: 0 30px; display: flex; justify-content: space-between; align-items: flex-start; z-index: 10; pointer-events: none; box-sizing: border-box; }
.hud-box { display: flex; gap: 15px; width: 350px; align-items: center; }
.hud-box.enemy-side { justify-content: flex-end; text-align: right; }
.avatar-box { width: 70px; height: 70px; border: 2px solid var(--gold); border-radius: 50%; background: #000; overflow: hidden; }
.avatar-box img { width: 100%; height: 100%; object-fit: cover; }
.bars-container { flex: 1; display: flex; flex-direction: column; gap: 5px; }
.name-row { display: flex; align-items: center; gap: 10px; color: #fff; font-weight: bold; text-shadow: 0 0 5px #000; }
.enemy-side .name-row { justify-content: flex-end; }
.hp-bar-frame { height: 10px; background: rgba(0,0,0,0.6); border: 1px solid #444; border-radius: 10px; overflow: hidden; position: relative; transform: skewX(-15deg); }
.hp-fill { height: 100%; transition: width 0.3s; }
.hp-fill.player { background: linear-gradient(90deg, #4caf50, #81c784); }
.hp-fill.enemy { background: linear-gradient(90deg, #e53935, #ef9a9a); float: right; }
.hp-text { position: absolute; top: -16px; right: 0; font-size: 0.75rem; color: #ddd; transform: skewX(15deg); }
.battle-timer { width: 50px; height: 50px; background: rgba(0,0,0,0.6); border: 2px solid var(--gold); border-radius: 10px; display: flex; justify-content: center; align-items: center; font-size: 1.5rem; font-weight: bold; color: #fff; box-shadow: 0 0 15px rgba(0,0,0,0.5); }
.battle-timer.warning { border-color: var(--red); color: var(--red); animation: pulse 1s infinite; }

/* GÓC ĐÁNH RỘNG RA 2% */
.battle-stage { position: absolute; inset: 0; display: flex; justify-content: center; align-items: center; z-index: 5; }
.fighter-display { position: absolute; bottom: 180px; width: 250px; text-align: center; }
.fighter-display.left { left: 2%; } 
.fighter-display.right { right: 2%; }
.char-anim { height: 220px; transform: scale(1.5); filter: drop-shadow(0 10px 10px rgba(0,0,0,0.5)); }
.enemy-flip { transform: scale(1.5) scaleX(-1); }
.shadow { width: 120px; height: 15px; background: rgba(0,0,0,0.5); border-radius: 50%; margin: -10px auto 0; filter: blur(5px); }

.clash-zone { position: absolute; top: 40%; display: flex; gap: 30px; align-items: center; z-index: 20; }
.move-icon { width: 80px; height: 80px; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; border: 4px solid #000; box-shadow: 0 0 30px rgba(255,255,255,0.5); color: #000; }
.vs-flash { font-size: 4rem; animation: boom 0.2s; }
.damage-text { position: absolute; top: -60px; left: 50%; transform: translateX(-50%); font-size: 2.5rem; font-weight: 900; -webkit-text-stroke: 1px #000; white-space: nowrap; }
.res-crit { color: #ff1744; text-shadow: 0 0 20px red; animation: shake 0.5s; }
.res-dodge { color: #00e676; text-shadow: 0 0 20px green; animation: floatUp 0.5s; }

.battle-deck { position: absolute; bottom: 0; left: 0; width: 100%; height: 130px; background: linear-gradient(to top, #000 0%, rgba(0,0,0,0.8) 80%, transparent 100%); display: grid; grid-template-columns: 300px 1fr 100px; padding: 0 20px 10px; box-sizing: border-box; align-items: end; z-index: 100; pointer-events: none; }
.mini-chat, .command-center, .util-deck { pointer-events: auto; }
.mini-chat { height: 100px; font-size: 0.8rem; display: flex; flex-direction: column; background: rgba(0,0,0,0.6); border: 1px solid #444; }
.chat-messages { flex: 1; padding: 5px; overflow-y: auto; }
.msg-line { color: #ccc; } .msg-line.me { color: #4fc3f7; }
.chat-input-row input { width: 100%; background: transparent; border: none; border-top: 1px solid #444; color: #fff; padding: 5px; }
.command-center { display: flex; justify-content: center; padding-bottom: 20px; }
.status-badge { font-size: 1.2rem; color: #ccc; display: flex; gap: 10px; align-items: center; text-shadow: 0 0 10px #000; }
.rps-buttons { display: flex; gap: 15px; position: relative; z-index: 101; }
.rps-btn { background: rgba(30,30,30,0.9); border: 1px solid #555; width: 80px; height: 90px; border-radius: 8px; cursor: pointer; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 5px; transition: 0.2s; color: #aaa; z-index: 102; }
.rps-btn:hover { transform: translateY(-10px); background: #333; border-color: var(--gold); color: var(--gold); }
.icon-circle { font-size: 1.5rem; }
.util-deck { display: flex; flex-direction: column; gap: 10px; padding-bottom: 20px; }
.util-btn { width: 40px; height: 40px; border-radius: 50%; background: rgba(255,255,255,0.1); border: 1px solid #555; color: #fff; cursor: pointer; transition: 0.2s; }
.util-btn:hover { background: #fff; color: #000; }
.util-btn.surrender:hover { background: var(--red); color: #fff; border-color: var(--red); }

/* MODAL STYLES */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.95); z-index: 200; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(10px); pointer-events: auto; }
.modal-box { width: 500px; background: #1a1a1a; border: 3px solid #444; border-radius: 16px; overflow: hidden; box-shadow: 0 0 100px rgba(0,0,0,0.9); text-align: center; animation: popIn 0.3s; }
.modal-header { padding: 20px; font-size: 2rem; font-weight: 900; background: rgba(255,255,255,0.05); border-bottom: 1px solid #333; color: #fff; font-family: 'Cinzel'; }
.modal-body { padding: 40px; font-size: 1.2rem; color: #ccc; }
.modal-actions { padding: 20px; display: flex; justify-content: center; gap: 20px; background: rgba(0,0,0,0.2); }

.danger-theme { border-color: #ff5252; }
.danger-theme .modal-header { background: #d32f2f; color: #fff; text-shadow: 0 2px 10px rgba(0,0,0,0.5); }
.win-theme { border-color: var(--gold); }
.win-theme .modal-header { color: var(--gold); text-shadow: 0 0 30px gold; background: linear-gradient(to bottom, rgba(255,215,0,0.2), transparent); }
.lose-theme { border-color: #555; }
.lose-theme .modal-header { color: #aaa; background: #333; }

.btn-confirm-surrender { background: #d32f2f; color: #fff; border: none; padding: 15px 30px; font-weight: 900; border-radius: 8px; cursor: pointer; font-size: 1.1rem; box-shadow: 0 0 20px rgba(211,47,47,0.5); transition: 0.2s; }
.btn-confirm-surrender:hover { transform: scale(1.05); background: #b71c1c; }
.btn-cancel-surrender { background: transparent; border: 2px solid #777; color: #ddd; padding: 15px 30px; border-radius: 8px; cursor: pointer; font-weight: bold; font-size: 1.1rem; }
.btn-cancel-surrender:hover { border-color: #fff; color: #fff; }

.auto-back-progress { margin-top: 20px; height: 6px; background: #333; border-radius: 3px; position: relative; overflow: hidden; }
.progress-bar { height: 100%; background: #00e676; transition: width 1s linear; }
.progress-text { display: block; margin-top: 10px; font-size: 0.9rem; color: #888; font-style: italic; }
.btn-back-home { margin-top: 20px; background: transparent; border: 2px solid #fff; color: #fff; padding: 15px 40px; border-radius: 50px; cursor: pointer; font-weight: 900; font-size: 1.2rem; transition: 0.2s; }
.btn-back-home:hover { background: #fff; color: #000; box-shadow: 0 0 30px #fff; }

.reward-pill { margin-top: 20px; display: inline-flex; align-items: center; gap: 10px; background: rgba(0,0,0,0.5); padding: 10px 30px; border-radius: 30px; border: 1px dashed #555; font-size: 1.3rem; }
.value.plus { color: #00e676; font-weight: bold; text-shadow: 0 0 10px #00e676; }
.value.minus { color: var(--red); font-weight: bold; }

@keyframes breathe { 0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.8; } 50% { transform: translate(-50%, -50%) scale(1.1); opacity: 1; } }
@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(74, 0, 224, 0.7); } 70% { box-shadow: 0 0 0 15px rgba(74, 0, 224, 0); } 100% { box-shadow: 0 0 0 0 rgba(74, 0, 224, 0); } }
@keyframes flash { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
@keyframes popIn { 0% { transform: scale(0.8); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
@keyframes hitShake { 10%, 90% { transform: translate3d(-1px, 0, 0); } 20%, 80% { transform: translate3d(2px, 0, 0); } 30%, 50%, 70% { transform: translate3d(-4px, 0, 0); } 40%, 60% { transform: translate3d(4px, 0, 0); } }
.hit-shake { animation: hitShake 0.5s; filter: sepia(1) hue-rotate(-50deg) saturate(5); }
@keyframes floatUp { from { transform: translate(-50%, 20px); opacity: 0; } to { transform: translate(-50%, 0); opacity: 1; } }
@keyframes boom { 0% { transform: scale(0.1); opacity: 0; } 50% { transform: scale(1.5); opacity: 1; } 100% { transform: scale(1); opacity: 1; } }
.anim-attack-left { animation: attackLeft 0.3s forwards; }
.anim-attack-right { animation: attackRight 0.3s forwards; }
@keyframes attackLeft { 0% { transform: translateX(0); } 50% { transform: translateX(50px) scale(1.2); } 100% { transform: translateX(0); } }
@keyframes attackRight { 0% { transform: translateX(0); } 50% { transform: translateX(-50px) scale(1.2); } 100% { transform: translateX(0); } }
.anim-hit { animation: hitRecoil 0.3s; }
@keyframes hitRecoil { 0% { opacity: 1; } 50% { opacity: 0.5; transform: scale(0.9); } 100% { opacity: 1; transform: scale(1); } }
@keyframes spin { to { transform: rotate(360deg); } }

.custom-scroll::-webkit-scrollbar { width: 5px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #444; border-radius: 3px; }
</style>