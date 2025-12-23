<template>
  <div class="page-container wuxia-dashboard">
    <div class="bg-layer">
      <div
        class="mountain-bg"
        :style="{ backgroundImage: `url(${currentBg})` }"
      ></div>
      <div class="wood-overlay" :class="{ 'night-mode': false }"></div>
      <div class="vignette"></div>
    </div>

    <div class="dashboard-wrapper">
      <div class="command-header wood-panel">
        <div class="header-left">
          <div class="server-tag">
            <span class="status-orb red"></span>
            <span class="server-txt">KHU VỰC: TỶ VÕ ĐÀI</span>
          </div>
          <div class="char-block">
            <div class="char-text">
              <span class="greet-txt"
                ><i class="fas fa-fist-raised"></i> Luận Kiếm Giang Hồ</span
              >
              <h1 class="char-name">VÕ ĐÀI TRANH HÙNG</h1>
            </div>
          </div>
        </div>
        <div class="header-right">
          <div class="wealth-bar">
            <div class="wealth-item">
              <span class="lbl">DANH VỌNG:</span>
              <span class="amt gold-txt">{{ characterReputation }}</span>
            </div>
            <div class="wealth-item" style="margin-left: 15px">
              <span class="lbl">DANH HIỆU:</span>
              <span class="amt rank-txt">{{ characterRankTitle }}</span>
            </div>
          </div>
          <div class="weather-seal">
            <i class="fas fa-dragon"></i>
          </div>
        </div>
      </div>

      <div class="pvp-stage-container wood-panel-content">
        <div v-if="gameState !== 'BATTLE'" class="lobby-layout">
          <div class="sidebar-col">
            <div class="inner-wood-box">
              <div class="box-header">
                <i class="fas fa-scroll"></i> CAO THỦ GIANG HỒ
              </div>
              <div v-if="lbStore.loadingPvp" class="lb-loading">
                <i class="fas fa-yin-yang fa-spin"></i> Đang tra cứu...
              </div>
              <div v-else class="lb-mini-list custom-scroll">
                <div
                  v-for="(entry, index) in lbStore.topPvp"
                  :key="index"
                  class="lb-mini-item"
                >
                  <div class="item-rank" :class="'rank-' + (index + 1)">
                    {{ index + 1 }}
                  </div>
                  <div class="item-avatar small">
                    <img :src="resolveAvatar(entry.avatarUrl)" class="pixel-art" />
                  </div>
                  <div class="item-info">
                    <div class="item-name">{{ entry.name }}</div>
                    <div class="item-val">
                        <span class="rank-badge">{{ entry.rankTitle || 'Vô Danh' }}</span>
                        <span class="rep-val">({{ entry.reputation }} DV)</span>
                    </div>
                  </div>
                </div>
                <div v-if="!lbStore.topPvp?.length" class="lb-empty">
                  Chưa có dữ liệu...
                </div>
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
                <div class="hero-rank-display">{{ characterRankTitle }}</div>
              </div>

              <div class="action-area">
                <div v-if="gameState === 'LOBBY'" class="btn-group">
                  <button
                    class="wuxia-btn primary big pulse-anim"
                    @click="toggleSearch"
                  >
                    <i class="fas fa-swords"></i> TÌM ĐỐI THỦ
                  </button>
                  <button class="wuxia-btn gold-btn big" @click="manualRefresh">
                    <i class="fas fa-sync-alt"></i> CẬP NHẬT BXH
                  </button>
                </div>

                <div v-else-if="gameState === 'SEARCHING'" class="searching-state">
                  <div class="spinner-radar">
                    <i class="fas fa-circle-notch fa-spin"></i>
                  </div>
                  <p>Đang tìm kiếm đối thủ xứng tầm... ({{ searchTimer }}s)</p>
                  <button class="wuxia-btn danger" @click="cancelSearch">
                    HỦY BỎ
                  </button>
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
                    <button
                      class="wuxia-btn primary"
                      @click="acceptMatch"
                      :disabled="hasAccepted"
                    >
                      {{ hasAccepted ? "ĐANG CHỜ ĐỐI THỦ..." : `CHẤP NHẬN CHIẾN (${acceptTimer}s)` }}
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
              <div class="log-header">
                NHẬT KÝ CHIẾN ĐẤU
                <span @click="showLog = false" class="close-log">×</span>
              </div>
              <div class="log-content">
                <div v-for="(log, i) in battleLogs.slice().reverse()" :key="i" class="log-line">
                  » {{ log }}
                </div>
              </div>
            </div>
          </transition>

          <div class="top-timer-display" :class="{ urgent: turnTimer <= 10 }">
            <div class="timer-value">{{ turnTimer }}</div>
          </div>

          <div class="fighters-stage">
            <div class="fighter left-side player">
              <div class="sprite-container">
                <img :src="mySprite" class="char-img pixel-art" :class="{ 'shake-hit': isMyHit }" />
                <div class="shadow-oval"></div>
              </div>
              <div class="hud player-hud">
                <div class="hud-row">
                  <span class="f-name">{{ characterName }}</span>
                  <span class="f-lv">Lv.{{ characterLevel }}</span>
                </div>
                <div class="hp-track">
                  <div class="hp-text">{{ myHp }} / {{ myMaxHp }}</div>
                  <div class="hp-fill" :style="{ width: percent(myHp, myMaxHp) + '%' }"></div>
                </div>
              </div>
            </div>

            <div class="fighter right-side enemy">
              <div class="hud enemy-hud">
                <div class="hud-row">
                  <span class="f-name">{{ enemyName }}</span>
                  <span class="f-lv">Lv.{{ enemyLevel }}</span>
                </div>
                <div class="hp-track">
                  <div class="hp-text">{{ enemyHp }} / {{ enemyMaxHp }}</div>
                  <div class="hp-fill" :style="{ width: percent(enemyHp, enemyMaxHp) + '%' }"></div>
                </div>
              </div>
              <div class="sprite-container">
                <img :src="enemySprite" class="char-img pixel-art enemy-flip" :class="{ 'shake-hit': isEnemyHit }" />
                <div class="shadow-oval"></div>
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
              <div class="result-label" :class="getResultClass(lastResultText)">
                {{ lastResultText }}
              </div>
            </div>
          </div>

          <div class="battle-controls">
            <div class="chat-section">
              <div class="chat-history custom-scroll" ref="chatBoxRef">
                <div v-for="(msg, i) in matchMessages" :key="i" class="chat-bubble" :class="{ me: msg.isMe }">
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
                <button class="square-btn rock" @click="submitRps('ROCK')" :disabled="isActionPending">
                  <i class="fas fa-hand-rock"></i><span>BÚA</span>
                </button>
                <button class="square-btn paper" @click="submitRps('PAPER')" :disabled="isActionPending">
                  <i class="fas fa-hand-paper"></i><span>BAO</span>
                </button>
                <button class="square-btn scissor" @click="submitRps('SCISSORS')" :disabled="isActionPending">
                  <i class="fas fa-hand-scissors"></i><span>KÉO</span>
                </button>
                <button class="square-btn surrender" @click="handleSurrender">
                  <i class="fas fa-flag"></i><span>THOÁT</span>
                </button>
              </div>
              <div v-else class="status-overlay">
                <div class="spinner-box">
                  <i v-if="!isRevealing" class="fas fa-yin-yang fa-spin"></i>
                  <span>{{ isRevealing ? "ĐANG TÍNH TOÁN..." : "CHỜ ĐỐI THỦ RA CHIÊU..." }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="showEndGameModal" class="endgame-modal-overlay">
        <div class="endgame-content" :class="{ win: isWin, lose: !isWin }">
          <div class="endgame-title">{{ endGameTitle }}</div>
          <div class="endgame-desc">{{ endGameMessage }}</div>
          
          <div class="reward-box" v-if="gainedReputation !== 0">
             <div class="reward-row">
                <span class="lbl">Danh Vọng:</span>
                <span class="val" :class="gainedReputation > 0 ? 'plus' : 'minus'">
                    {{ gainedReputation > 0 ? '+' : '' }}{{ gainedReputation }}
                </span>
             </div>
          </div>

          <div class="auto-back-timer">
            Tự động về sảnh sau {{ autoBackTimer }}s
          </div>
          <button class="wuxia-btn primary big" @click="closeEndGameModal">
            VỀ SẢNH NGAY
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from "vue";
// [IMPORTANT] Sử dụng đúng Store của bạn
import { useAuthStore } from "../stores/authStore";
import { useLeaderboardStore } from "../stores/leaderboardStore";
import { CHARACTER_SKINS } from "../utils/assetHelper";
import axios from "axios";

const API_URL = "http://localhost:8080/api/pvp";
const authStore = useAuthStore();
const lbStore = useLeaderboardStore();

// --- ASSET HELPERS ---
const getSpriteUrl = (skinId) => {
  const skin = CHARACTER_SKINS[skinId] || CHARACTER_SKINS["skin_yasou"];
  return skin.sprites.idle;
};

const resolveAvatar = (avatarStr) => {
  if (!avatarStr) return getSpriteUrl("skin_yasou");
  if (avatarStr.startsWith("/uploads/"))
    return `http://localhost:8080${avatarStr}`;
  if (avatarStr.includes("http") || avatarStr.startsWith("data:"))
    return avatarStr;
  return getSpriteUrl(avatarStr);
};

// --- COMPUTED: Lấy data từ AuthStore (Store này bạn đã cấu hình tốt) ---
const characterName = computed(() => authStore.character?.name || authStore.user?.username || "Đại Hiệp");
const characterLevel = computed(() => authStore.character?.level || 1);
const characterReputation = computed(() => authStore.character?.reputation || 0);
const characterRankTitle = computed(() => authStore.character?.rankTitle || "Vô Danh");

const mySprite = computed(() => getSpriteUrl(authStore.user?.avatarUrl));
const enemySprite = computed(() => getSpriteUrl(enemySkinId.value));

const currentBg = computed(() => {
  if (gameState.value === "BATTLE") return "https://images.unsplash.com/photo-1535581652167-3d6b98c365b2?q=80&w=1920&auto=format&fit=crop";
  return "https://htkhang111.github.io/background/b_doanhtrai.png";
});

// --- STATE ---
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

// --- END GAME STATE ---
const showEndGameModal = ref(false);
const isWin = ref(false);
const endGameTitle = ref("");
const endGameMessage = ref("");
const autoBackTimer = ref(5);
let autoBackInterval = null;

// [FIX] Biến hiển thị kết quả
const gainedReputation = ref(0);

// --- BATTLE DATA ---
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

let pollId, searchInterval, acceptInterval, turnTimerInterval, revealTimeout;
const getHeaders = () => ({ Authorization: `Bearer ${authStore.token}` });

const percent = (c, m) => (!m || m <= 0) ? 0 : Math.max(0, Math.min(100, (c / m) * 100));
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

const manualRefresh = async () => {
    await authStore.fetchCharacter();
    await lbStore.fetchPvPBoard();
}

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
  gainedReputation.value = 0;
  if (autoBackInterval) clearInterval(autoBackInterval);
};

const clearAllIntervals = () => {
  if (searchInterval) clearInterval(searchInterval);
  if (acceptInterval) clearInterval(acceptInterval);
  if (turnTimerInterval) clearInterval(turnTimerInterval);
  if (pollId) { clearInterval(pollId); pollId = null; }
  if (revealTimeout) clearTimeout(revealTimeout);
  if (autoBackInterval) clearInterval(autoBackInterval);
};

// --- [LOGIC QUAN TRỌNG NHẤT] ---
const toggleSearch = async () => {
  resetToLobby();
  
  // 1. Cập nhật dữ liệu từ Server
  await authStore.fetchCharacter();
  
  // 2. [FIX] Lưu điểm danh vọng vào LocalStorage (chống F5 mất dữ liệu)
  // Store của bạn lưu trong 'character.reputation'
  if (authStore.character) {
    const currentRep = authStore.character.reputation || 0;
    localStorage.setItem('pvp_start_rep', currentRep); 
  }
  
  gameState.value = "SEARCHING";
  searchTimer.value = 0;
  searchInterval = setInterval(() => searchTimer.value++, 1000);
  try {
    await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() });
    startPolling();
  } catch (e) {
    console.error(e);
    resetToLobby();
  }
};

const cancelSearch = async () => {
  try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch (e) {}
  resetToLobby();
};
const acceptMatch = async () => {
  if (!matchId.value) return;
  hasAccepted.value = true;
  try { await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) {}
};

const submitRps = async (move) => {
  if (isActionPending.value || isRevealing.value) return;
  isActionPending.value = true;
  battlePhase.value = "RPS_PENDING";
  try {
    await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() });
  } catch (e) {
    isActionPending.value = false;
  }
};

const handleSurrender = async () => {
  if (!confirm("Xác nhận thoát trận (Tính thua)?")) return;
  try { await axios.post(`${API_URL}/surrender`, { matchId: matchId.value }, { headers: getHeaders() }); } catch (e) {}
};

// --- SYNC ENGINE ---
const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data;
      if (!data) return;
      if (data.matchId && ignoredMatchIds.value.has(Number(data.matchId))) return;

      // [XỬ LÝ KẾT THÚC TRẬN]
      if (data.status === "FINISHED") {
        if (gameState.value === 'LOBBY' || gameState.value === 'SEARCHING') {
            if (data.matchId) ignoredMatchIds.value.add(Number(data.matchId));
            return;
        }

        if (!showEndGameModal.value) {
          const myCharId = Number(authStore.character?.charId || authStore.character?.id || 0);
          const serverWinnerId = data.winnerId !== null ? Number(data.winnerId) : null;

          syncBattleData(data);
          const delayTime = isRevealing.value ? 3000 : 500;

          setTimeout(async () => {
            clearAllIntervals();
            
            // 1. Lấy dữ liệu mới nhất từ Server
            await authStore.fetchCharacter(); 
            
            // 2. [FIX] Tính toán điểm chênh lệch từ LocalStorage
            const currentRep = authStore.character?.reputation || 0;
            // Nếu không tìm thấy trong storage thì lấy luôn currentRep (tránh lỗi NaN)
            const savedStartRep = localStorage.getItem('pvp_start_rep');
            const startRep = savedStartRep ? Number(savedStartRep) : currentRep;
            
            gainedReputation.value = currentRep - startRep;

            const iWon = serverWinnerId === myCharId;
            isWin.value = iWon;

            const log = (data.lastLog || "").toLowerCase();
            const isSurrender = log.includes("đầu hàng");
            const isDisconnect = log.includes("hết giờ") || log.includes("thoát");

            if (serverWinnerId === null) {
              endGameTitle.value = "HÒA";
              endGameMessage.value = "Lưỡng bại câu thương!";
            } else if (iWon) {
              endGameTitle.value = "CHIẾN THẮNG";
              endGameMessage.value = isSurrender ? "Đối thủ đã đầu hàng!" : "Bạn đã đánh bại đối thủ!";
            } else {
              endGameTitle.value = "THẤT BẠI";
              endGameMessage.value = isSurrender ? "Bạn đã đầu hàng!" : (isDisconnect ? "Hết giờ, bạn bị xử thua!" : "Bạn đã bị đánh bại...");
            }

            showEndGameModal.value = true;
            lbStore.fetchPvPBoard();
            if (data.matchId) ignoredMatchIds.value.add(Number(data.matchId));

            // Auto back timer
            autoBackTimer.value = 5;
            autoBackInterval = setInterval(() => {
              autoBackTimer.value--;
              if (autoBackTimer.value <= 0) closeEndGameModal();
            }, 1000);
          }, delayTime);
        }
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

const closeEndGameModal = () => {
  showEndGameModal.value = false;
  resetToLobby();
};

const syncBattleData = (data) => {
  syncBasicInfo(data);
  const isP1 = checkIsPlayer1(data);
  const nMyHp = isP1 ? data.p1Hp : data.p2Hp;
  const nEnHp = isP1 ? data.p2Hp : data.p1Hp;
  
  if (nMyHp < myHp.value) { isMyHit.value = true; setTimeout(() => (isMyHit.value = false), 300); }
  if (nEnHp < enemyHp.value) { isEnemyHit.value = true; setTimeout(() => (isEnemyHit.value = false), 300); }
  
  myHp.value = nMyHp;
  enemyHp.value = nEnHp;
  myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;

  if (data.messages && data.messages.length > matchMessages.value.length) {
    data.messages.slice(matchMessages.value.length).forEach((m) =>
      matchMessages.value.push({
        sender: m.senderName, text: m.content, isMe: (checkIsPlayer1(data) ? data.p1Id : data.p2Id) == m.senderId,
      }),
    );
    nextTick(() => { if (chatBoxRef.value) chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight; });
  }

  if (isRevealing.value) return;
  const currentTurn = data.turnCount;
  const bothMoved = data.lastP1Move && data.lastP2Move;
  
  if (bothMoved && currentTurn > lastProcessedTurn.value) {
    isRevealing.value = true;
    lastProcessedTurn.value = currentTurn;
    isActionPending.value = false;
    displayedMyMove.value = isP1 ? data.lastP1Move : data.lastP2Move;
    displayedEnemyMove.value = isP1 ? data.lastP2Move : data.lastP1Move;

    if (data.lastLog) {
      battleLogs.value.push(data.lastLog);
      const logUpper = data.lastLog.toUpperCase();
      if (logUpper.includes("NÉ")) lastResultText.value = "NÉ ĐÒN!";
      else if (logUpper.includes("CHÍ MẠNG")) lastResultText.value = "BẠO KÍCH!";
      else if (logUpper.includes("HÒA") || logUpper.includes("XUNG KHẮC")) lastResultText.value = "XUNG KHẮC!";
      else lastResultText.value = "TRÚNG!";
    }

    battlePhase.value = "RPS_REVEAL";
    revealTimeout = setTimeout(() => {
      isRevealing.value = false;
      battlePhase.value = "RPS_WAIT";
      turnTimer.value = 30;
    }, 3000);
  } else {
    const myM = isP1 ? data.p1Move : data.p2Move;
    battlePhase.value = (myM && myM !== 'HIDDEN') ? "RPS_PENDING" : "RPS_WAIT";
  }
};

const startTurnTimer = () => {
  if (turnTimerInterval) clearInterval(turnTimerInterval);
  turnTimerInterval = setInterval(() => { if (turnTimer.value > 0) turnTimer.value--; }, 1000);
};

const sendPrivateChat = async () => {
  if (!chatInput.value.trim()) return;
  try { await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); } catch (e) {}
  chatInput.value = "";
};

const getRpsIcon = (m) => ({ ROCK: "fas fa-hand-rock", PAPER: "fas fa-hand-paper", SCISSORS: "fas fa-hand-scissors" })[m] || "fas fa-question";
const getAnimClass = (myMove, enemyMove, isMe) => {
  if (!myMove || !enemyMove) return "";
  if (myMove === enemyMove) return "anim-clash";
  let iWin = (myMove === "ROCK" && enemyMove === "SCISSORS") || (myMove === "SCISSORS" && enemyMove === "PAPER") || (myMove === "PAPER" && enemyMove === "ROCK");
  return isMe ? (iWin ? "anim-smash-win" : "anim-lose") : (!iWin ? "anim-smash-win" : "anim-lose");
};
const getResultClass = (text) => {
  if (text === "NÉ ĐÒN!") return "res-dodge";
  if (text === "BẠO KÍCH!") return "res-crit";
  if (text === "XUNG KHẮC!") return "res-clash";
  return "res-normal";
};

onMounted(() => {
  authStore.fetchCharacter();
  lbStore.fetchPvPBoard();
  window.addEventListener("keydown", (e) => {
    if (document.activeElement.tagName !== "INPUT" && e.key.toLowerCase() === "l") {
      e.preventDefault(); showLog.value = !showLog.value;
    }
  });
  startPolling();
});
onUnmounted(() => clearAllIntervals());
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif:ital,wght@0,400;0,700;1,400&family=Playfair+Display:wght@700;900&display=swap");
@import url("https://fonts.googleapis.com/css2?family=Roboto+Condensed:wght@700&display=swap");

.pixel-art { image-rendering: pixelated; }
:root { --wood-base: #3e2723; --wood-card: #5d4037; --gold: #ffecb3; --gold-accent: #ffd700; --text-main: #fff8e1; --text-dim: #d7ccc8; }
.wuxia-dashboard { min-height: 100vh; padding: 20px; font-family: "Noto Serif", serif; color: var(--text-main); position: relative; overflow-x: hidden; box-sizing: border-box; }
.bg-layer { position: absolute; inset: 0; z-index: 0; background: #261815; pointer-events: none; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center bottom; opacity: 0.6; filter: sepia(10%) contrast(1.1); transition: background-image 0.5s ease-in-out; }
.wood-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, rgba(62, 39, 35, 0.7), rgba(30, 20, 15, 0.9)); mix-blend-mode: multiply; transition: background 2s ease; }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle, transparent 60%, #1a100d 100%); }
.dashboard-wrapper { position: relative; z-index: 10; max-width: 1000px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; height: calc(100vh - 40px); }
.wood-panel { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(90deg, rgba(62, 39, 35, 0.95), rgba(93, 64, 55, 0.9)); border: 2px solid #6d4c41; border-radius: 6px; padding: 10px 20px; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5); flex-shrink: 0; }
.header-left { display: flex; flex-direction: column; gap: 4px; }
.server-tag { font-size: 0.75rem; color: var(--text-dim); display: flex; align-items: center; gap: 6px; }
.status-orb { width: 6px; height: 6px; background: #66bb6a; border-radius: 50%; box-shadow: 0 0 5px #66bb6a; }
.status-orb.red { background: #ff5252; box-shadow: 0 0 5px #ff5252; }
.greet-txt { font-size: 0.8rem; color: var(--gold); }
.char-name { font-family: "Playfair Display", serif; font-size: 1.8rem; margin: 0; color: #fff; text-shadow: 0 2px 4px rgba(0, 0, 0, 0.6); line-height: 1; }
.header-right { display: flex; align-items: center; gap: 15px; }
.wealth-item { display: flex; align-items: center; gap: 6px; font-weight: bold; color: var(--gold-accent); font-size: 1rem; }
.gold-txt { color: #ffd700; }
.rank-txt { color: #e0f7fa; text-transform: uppercase; font-size: 0.9rem; border: 1px solid #4dd0e1; padding: 2px 6px; border-radius: 4px; background: rgba(0, 188, 212, 0.2); }
.weather-seal { display: flex; align-items: center; gap: 8px; color: var(--text-dim); font-size: 1.5rem; }
.pvp-stage-container { flex: 1; background: rgba(40, 20, 10, 0.6); border: 1px solid #5d4037; border-radius: 6px; position: relative; overflow: hidden; box-shadow: 0 5px 20px rgba(0, 0, 0, 0.5); display: flex; }
.lobby-layout { display: flex; width: 100%; height: 100%; gap: 15px; padding: 15px; }
.sidebar-col { width: 280px; flex-shrink: 0; display: flex; flex-direction: column; }
.inner-wood-box { background: rgba(30, 20, 15, 0.9); border: 1px solid #6d4c41; flex: 1; border-radius: 4px; display: flex; flex-direction: column; overflow: hidden; }
.box-header { padding: 10px; text-align: center; font-weight: bold; color: var(--gold); border-bottom: 1px solid #5d4037; background: rgba(62, 39, 35, 0.8); }
.lb-mini-list { flex: 1; overflow-y: auto; padding: 10px; }
.lb-mini-item { display: flex; align-items: center; gap: 10px; padding: 8px; border-bottom: 1px dashed #444; }
.item-rank { width: 20px; font-weight: bold; text-align: center; color: #888; }
.item-rank.rank-1 { color: #ffd700; } .item-rank.rank-2 { color: #c0c0c0; } .item-rank.rank-3 { color: #cd7f32; }
.item-avatar.small { width: 32px; height: 32px; background: #111; border-radius: 50%; overflow: hidden; }
.item-avatar.small img { width: 100%; height: 100%; object-fit: contain; }
.item-info { flex: 1; font-size: 0.9rem; display: flex; flex-direction: column; }
.item-name { color: #fff; font-weight: bold; }
.item-val { font-size: 0.8rem; color: var(--gold); display: flex; align-items: center; gap: 5px; }
.rank-badge { color: #4dd0e1; font-size: 0.75rem; }
.rep-val { color: #ffd700; font-size: 0.75rem; }
.main-col { flex: 1; display: flex; align-items: center; justify-content: center; }
.hero-card-wide { width: 100%; max-width: 400px; background: linear-gradient(135deg, var(--wood-card) 0%, var(--wood-base) 100%); border: 2px solid var(--gold); border-radius: 8px; padding: 30px; text-align: center; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5); position: relative; }
.hero-visual { display: flex; flex-direction: column; align-items: center; margin-bottom: 25px; position: relative; }
.avatar-frame.large { width: 140px; height: 140px; margin: 0 auto; border: 4px solid var(--gold); border-radius: 50%; overflow: visible; background: radial-gradient(circle, #261815 60%, #000 100%); box-shadow: 0 0 25px rgba(255, 215, 0, 0.25); position: relative; display: flex; justify-content: center; align-items: center; }
.avatar-frame.large img { width: 110%; height: 110%; object-fit: cover; transform: translateY(-5px); border-radius: 50%; z-index: 1; }
.level-tag { position: absolute; bottom: -12px; left: 50%; transform: translateX(-50%); z-index: 10; background: linear-gradient(180deg, #ffd700 0%, #ff8f00 100%); border: 2px solid #fff; border-radius: 12px; padding: 4px 16px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.5); display: flex; align-items: center; gap: 5px; min-width: 80px; justify-content: center; }
.level-tag .lbl { font-size: 0.7rem; font-weight: bold; color: #5d4037; text-transform: uppercase; }
.level-tag .val { font-size: 1.1rem; font-weight: 900; color: #bf360c; text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5); }
.hero-name { margin-top: 25px; font-family: "Playfair Display"; color: #fff; font-size: 1.8rem; text-shadow: 0 2px 10px rgba(0, 0, 0, 0.8); letter-spacing: 1px; }
.hero-rank-display { color: #4dd0e1; font-size: 1rem; font-weight: bold; text-transform: uppercase; letter-spacing: 2px; margin-top: 5px; text-shadow: 0 0 5px rgba(77, 208, 225, 0.6); }
.action-area .btn-group { display: flex; flex-direction: column; gap: 12px; margin-top: 10px; }
.wuxia-btn { padding: 12px 20px; border: none; cursor: pointer; font-weight: bold; border-radius: 6px; transition: 0.2s; font-family: "Noto Serif"; text-transform: uppercase; font-size: 1rem; position: relative; overflow: hidden; }
.wuxia-btn.primary { background: linear-gradient(135deg, #d32f2f 0%, #b71c1c 100%); color: #fff; border: 2px solid #ff5252; box-shadow: 0 4px 10px rgba(183, 28, 28, 0.5); font-size: 1.2rem; letter-spacing: 1px; }
.wuxia-btn.primary:hover { background: linear-gradient(135deg, #f44336 0%, #c62828 100%); transform: translateY(-2px); box-shadow: 0 6px 15px rgba(211, 47, 47, 0.7); }
.wuxia-btn.primary:active { transform: translateY(1px); box-shadow: none; }
.wuxia-btn.gold-btn { background: linear-gradient(to bottom, #ffd700, #fbc02d); color: #3e2723; border: 2px solid #ffecb3; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.4); }
.wuxia-btn.gold-btn:hover { background: linear-gradient(to bottom, #ffeb3b, #fdd835); transform: translateY(-2px); box-shadow: 0 6px 12px rgba(255, 215, 0, 0.3); }
.wuxia-btn.gold-btn:active { transform: translateY(1px); }
.wuxia-btn.outline { background: transparent; border: 1px solid var(--gold); color: var(--gold); }
.wuxia-btn.outline:hover { background: rgba(255, 236, 179, 0.1); }
.wuxia-btn.big { width: 100%; }
.searching-state { color: var(--gold); }
.spinner-radar { font-size: 2rem; margin-bottom: 10px; color: var(--gold-accent); }
.match-popup-embed { border-top: 1px solid rgba(255, 255, 255, 0.2); margin-top: 15px; padding-top: 15px; animation: slideUp 0.3s; }
.enemy-preview-row { display: flex; align-items: center; gap: 15px; background: rgba(0, 0, 0, 0.3); padding: 10px; border-radius: 4px; margin: 10px 0; }
.preview-avatar { width: 50px; height: 50px; background: #000; border-radius: 50%; overflow: hidden; }
.preview-avatar img { width: 100%; height: 100%; object-fit: contain; }
.preview-info { text-align: left; display: flex; flex-direction: column; }
.popup-actions-row { display: flex; gap: 10px; justify-content: center; }
@keyframes slideUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes btn-pulse { 0% { box-shadow: 0 0 0 0 rgba(211, 47, 47, 0.7); } 70% { box-shadow: 0 0 0 10px rgba(211, 47, 47, 0); } 100% { box-shadow: 0 0 0 0 rgba(211, 47, 47, 0); } }
.pulse-anim { animation: btn-pulse 2s infinite; }
.battle-arena-frame { width: 100%; height: 100%; display: flex; flex-direction: column; position: relative; background: #000; }
.arena-background { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0.5; }
.log-overlay-btn { position: absolute; top: 10px; left: 10px; z-index: 20; background: rgba(0, 0, 0, 0.6); padding: 5px 10px; border: 1px solid var(--gold); color: var(--gold); cursor: pointer; border-radius: 4px; }
.floating-log-panel { position: absolute; top: 40px; left: 10px; width: 300px; height: 200px; background: rgba(0, 0, 0, 0.9); border: 1px solid var(--gold); z-index: 30; padding: 10px; overflow-y: auto; color: #fff; font-size: 0.85rem; }
.top-timer-display { position: absolute; top: 10px; left: 50%; transform: translateX(-50%); width: 50px; height: 50px; background: rgba(0, 0, 0, 0.7); border: 2px solid var(--gold); border-radius: 50%; display: flex; align-items: center; justify-content: center; z-index: 20; font-weight: bold; font-size: 1.2rem; color: #fff; }
.top-timer-display.urgent { border-color: #f44336; color: #f44336; animation: pulse 0.5s infinite; }
.fighters-stage { flex: 1; position: relative; z-index: 10; display: flex; justify-content: space-between; padding: 20px 40px; align-items: center; }
.fighter { display: flex; flex-direction: column; width: 180px; align-items: center; position: relative; }
.fighter.left-side { align-items: flex-start; } .fighter.left-side .hud { text-align: left; }
.fighter.right-side { align-items: flex-end; } .fighter.right-side .hud { text-align: right; }
.sprite-container { width: 120px; height: 120px; position: relative; display: flex; align-items: center; justify-content: center; }
.char-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); transition: 0.2s; z-index: 2; }
.enemy-flip { transform: scale(1.5) scaleX(-1); }
.shadow-oval { position: absolute; bottom: 5px; width: 80%; height: 10px; background: rgba(0, 0, 0, 0.5); border-radius: 50%; filter: blur(2px); z-index: 1; }
.hud { width: 100%; background: rgba(0, 0, 0, 0.6); padding: 5px; border-radius: 4px; margin-top: 5px; color: #fff; font-size: 0.85rem; }
.hud-row { display: flex; justify-content: space-between; margin-bottom: 3px; }
.hp-track { width: 100%; height: 5px; background: #333; border-radius: 2px; position: relative; }
.hp-text { position: absolute; top: -15px; right: 0; font-size: 0.7rem; color: #fff; font-weight: bold; text-shadow: 1px 1px 0 #000; }
.hp-fill { height: 100%; background: #d32f2f; width: 100%; transition: width 0.3s; }
.clash-visual { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; gap: 20px; pointer-events: none; z-index: 50; }
.move-card { width: 80px; height: 80px; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; color: #333; border: 3px solid #000; }
.clash-spark { font-size: 3rem; animation: pulse 0.1s infinite; }
.result-label { position: absolute; bottom: 100px; font-size: 2.5rem; font-weight: 900; text-shadow: 0 0 10px #000; letter-spacing: 2px; }
.res-dodge { color: #69f0ae; text-shadow: 0 0 10px #00e676; animation: floatUp 0.5s ease-out; }
.res-crit { color: #ff5252; text-shadow: 0 0 15px #d50000; transform: scale(1.2); animation: critShake 0.2s infinite; }
.res-clash { color: #bcaaa4; }
.res-normal { color: var(--gold-accent); }
.battle-controls { height: 160px; background: #1a1a1a; border-top: 2px solid #5d4037; display: flex; z-index: 20; position: relative; }
.chat-section { flex: 1.2; border-right: 1px solid #333; display: flex; flex-direction: column; }
.chat-history { flex: 1; padding: 10px; overflow-y: auto; font-size: 0.85rem; display: flex; flex-direction: column; gap: 4px; }
.chat-bubble { padding: 4px 8px; background: #333; border-radius: 4px; align-self: flex-start; color: #ccc; max-width: 90%; }
.chat-bubble.me { align-self: flex-end; background: #2e7d32; color: #fff; }
.chat-input-area { display: flex; border-top: 1px solid #333; }
.chat-input-area input { flex: 1; background: #000; border: none; color: #fff; padding: 8px; outline: none; }
.chat-input-area button { background: #5d4037; border: none; color: #fff; padding: 0 15px; cursor: pointer; }
.actions-section { flex: 1; padding: 15px; display: flex; align-items: center; justify-content: center; }
.rps-grid-simple { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 8px; width: 100%; height: 100%; }
.square-btn { background: #222; border: 1px solid #444; color: #eee; cursor: pointer; display: flex; flex-direction: column; align-items: center; justify-content: center; font-weight: bold; border-radius: 4px; transition: 0.2s; }
.square-btn i { font-size: 1.5rem; margin-bottom: 2px; }
.square-btn:hover:not(:disabled) { background: #333; border-color: var(--gold); color: var(--gold); }
.square-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.status-overlay { display: flex; flex-direction: column; align-items: center; gap: 10px; color: #ccc; width: 100%; }
.text-btn-tiny { background: rgba(255, 255, 255, 0.1); border: 1px solid #555; color: #aaa; font-size: 0.75rem; padding: 5px 15px; cursor: pointer; border-radius: 20px; transition: 0.3s; }
.text-btn-tiny:hover { background: #d32f2f; color: #fff; border-color: #d32f2f; }
.endgame-modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); display: flex; align-items: center; justify-content: center; z-index: 100; backdrop-filter: blur(5px); }
.endgame-content { background: #1a1a1a; padding: 40px; border-radius: 10px; text-align: center; border: 3px solid #555; width: 400px; box-shadow: 0 0 50px rgba(0, 0, 0, 0.8); animation: popIn 0.3s; }
.endgame-content.win { border-color: #ffd700; box-shadow: 0 0 50px rgba(255, 215, 0, 0.4); }
.endgame-content.lose { border-color: #d32f2f; box-shadow: 0 0 50px rgba(211, 47, 47, 0.4); }
.endgame-title { font-size: 2.5rem; font-weight: 900; margin-bottom: 10px; font-family: "Playfair Display"; }
.win .endgame-title { color: #ffd700; text-shadow: 0 0 20px #ff8f00; }
.lose .endgame-title { color: #d32f2f; text-shadow: 0 0 20px #b71c1c; }
.endgame-desc { font-size: 1.2rem; color: #ddd; margin-bottom: 30px; }
.auto-back-timer { font-size: 0.9rem; color: #888; margin-bottom: 10px; font-style: italic; }
.custom-scroll::-webkit-scrollbar { width: 4px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #555; }

/* CSS MỚI CHO PHẦN THƯỞNG */
.reward-box { margin-bottom: 20px; background: rgba(0,0,0,0.5); padding: 10px; border-radius: 6px; border: 1px dashed #555; }
.reward-row { display: flex; justify-content: center; align-items: center; gap: 10px; font-size: 1.2rem; }
.reward-row .lbl { color: #aaa; }
.reward-row .val.plus { color: #69f0ae; font-weight: bold; text-shadow: 0 0 10px #00e676; }
.reward-row .val.minus { color: #ff5252; font-weight: bold; }

@keyframes pulse { 0% { transform: scale(1); } 50% { transform: scale(1.1); } 100% { transform: scale(1); } }
.shake-hit { animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both; filter: sepia(1) saturate(5) hue-rotate(-50deg); }
@keyframes shake { 10%, 90% { transform: translate3d(-1px, 0, 0); } 20%, 80% { transform: translate3d(2px, 0, 0); } 30%, 50%, 70% { transform: translate3d(-4px, 0, 0); } 40%, 60% { transform: translate3d(4px, 0, 0); } }
.anim-smash-win { transform: scale(1.3); border-color: #ffd700; z-index: 10; box-shadow: 0 0 20px rgba(255, 215, 0, 0.5); }
.anim-lose { opacity: 0.5; transform: scale(0.8); filter: grayscale(1); }
@keyframes floatUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
@keyframes critShake { 0% { transform: translate(1px, 1px) rotate(0deg); } 10% { transform: translate(-1px, -2px) rotate(-1deg); } 20% { transform: translate(-3px, 0px) rotate(1deg); } 30% { transform: translate(3px, 2px) rotate(0deg); } 40% { transform: translate(1px, -1px) rotate(1deg); } 50% { transform: translate(-1px, 2px) rotate(-1deg); } 60% { transform: translate(-3px, 1px) rotate(0deg); } 70% { transform: translate(3px, 1px) rotate(-1deg); } 80% { transform: translate(-1px, -1px) rotate(1deg); } 90% { transform: translate(1px, 2px) rotate(0deg); } 100% { transform: translate(1px, -2px) rotate(-1deg); } }
@keyframes popIn { 0% { transform: scale(0.5); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
</style>  