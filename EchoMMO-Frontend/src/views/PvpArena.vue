<template>
  <div class="pvp-arena-container">
    <div class="arena-bg"></div>
    <div class="scanline"></div>
    <div class="vignette"></div>

    <header class="arena-header">
      <h1 class="title-main">VÕ ĐÀI TRANH HÙNG</h1>
      <div class="title-sub">BÁCH CHIẾN BÁCH THẮNG - DANH CHẤN GIANG HỒ</div>
    </header>

    <div v-if="gameState === 'LOBBY' || gameState === 'SEARCHING'" class="lobby-panel">
      <div class="char-profile-card">
        <div class="portrait-wrapper shadow-gold">
          <img :src="authStore.character?.avatarUrl || 'https://i.imgur.com/7Y7t5Xp.png'" class="portrait-img" />
          <div class="lv-tag">Lv.{{ authStore.character?.level || 1 }}</div>
        </div>
        <div class="char-info">
          <h2 class="name-text">{{ authStore.character?.name }}</h2>
          <div class="win-badge">🏆 THẮNG: {{ authStore.character?.pvpWins || 0 }}</div>
        </div>
      </div>
      <div class="lobby-actions">
        <button :class="['btn-main-action', gameState === 'SEARCHING' ? 'searching-mode' : '']" @click="toggleSearch" :disabled="gameState === 'SEARCHING'">
          <span v-if="gameState === 'LOBBY'">KHỞI CHIẾN</span>
          <span v-else><i class="fas fa-yin-yang fa-spin"></i> ĐANG TÌM... ({{ searchTimer }}s)</span>
        </button>
        <button v-if="gameState === 'SEARCHING'" class="btn-cancel" @click="cancelSearch">HỦY TÌM</button>
      </div>
    </div>

    <div v-else-if="gameState === 'MATCH_FOUND'" class="match-found-panel">
      <div class="match-alert-box">
        <h2 class="alert-title">⚔️ ĐÃ TÌM THẤY ĐỐI THỦ ⚔️</h2>
        
        <div class="enemy-preview">
          <div class="portrait-wrapper shadow-red">
            <img :src="enemyAvatar || 'https://i.imgur.com/mZ5tN8v.png'" class="portrait-img" />
          </div>
          <div class="enemy-name">{{ enemyName }}</div>
          <div class="enemy-lv">Lv.{{ enemyLevel }}</div>
        </div>

        <div class="countdown-circle">{{ acceptTimer }}s</div>

        <div class="accept-actions">
          <button class="btn-decline" @click="declineMatch">BỎ QUA</button>
          <button class="btn-accept" @click="acceptMatch" :disabled="hasAccepted">
            {{ hasAccepted ? 'ĐANG CHỜ ĐỐI THỦ...' : 'CHẤP NHẬN CHIẾN ĐẤU' }}
          </button>
        </div>
      </div>
    </div>

    <div v-else class="battle-main-grid">
      <section class="arena-top">
        <div class="battle-hud">
          <div class="turn-box">HIỆP ĐẤU {{ currentTurnCount }}</div>
          <button class="surrender-btn" @click="handleSurrender"><i class="fas fa-flag"></i> ĐẦU HÀNG</button>
        </div>

        <div class="fighters-container">
          <div class="fighter-unit p1">
            <div class="hp-info">
              <div class="f-name">{{ authStore.character?.name }}</div>
              <div class="hp-track"><div class="hp-bar p1-color" :style="{ width: percent(myHp, myMaxHp) + '%' }"></div></div>
              <div class="hp-num">{{ myHp }} / {{ myMaxHp }}</div>
            </div>
            <div class="f-avatar" :class="{ 'hit-effect': isMyHit }">
              <img :src="authStore.character?.avatarUrl || 'https://i.imgur.com/7Y7t5Xp.png'" />
              <div v-if="battlePhase === 'RPS_REVEAL' && myRpsMove" class="rps-badge p1-glow"><i :class="getRpsIcon(myRpsMove)"></i></div>
            </div>
          </div>

          <div class="vs-text">VS</div>

          <div class="fighter-unit p2">
            <div class="hp-info">
              <div class="f-name">{{ enemyName }}</div>
              <div class="hp-track"><div class="hp-bar p2-color" :style="{ width: percent(enemyHp, enemyMaxHp) + '%' }"></div></div>
              <div class="hp-num">{{ enemyHp }} / {{ enemyMaxHp }}</div>
            </div>
            <div class="f-avatar" :class="{ 'hit-effect': isEnemyHit }">
              <img :src="enemyAvatar || 'https://i.imgur.com/mZ5tN8v.png'" class="flipped" />
              <div v-if="battlePhase === 'RPS_REVEAL' && enemyRpsMove" class="rps-badge p2-glow">
                <i v-if="enemyRpsMove === 'HIDDEN'" class="fas fa-question"></i>
                <i v-else :class="getRpsIcon(enemyRpsMove)"></i>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="arena-bottom">
        <div class="control-panel">
          <div class="rps-buttons">
            <div v-if="battlePhase === 'RPS_WAIT'" class="rps-group">
              <button @click="submitRps('ROCK')" class="rps-btn" title="Búa"><i class="fas fa-hand-rock"></i><span class="btn-label">BÚA</span></button>
              <button @click="submitRps('PAPER')" class="rps-btn" title="Bao"><i class="fas fa-hand-paper"></i><span class="btn-label">BAO</span></button>
              <button @click="submitRps('SCISSORS')" class="rps-btn" title="Kéo"><i class="fas fa-hand-scissors"></i><span class="btn-label">KÉO</span></button>
            </div>
            <div v-else class="status-msg">
              <span v-if="lastResultText" class="result-flash">{{ lastResultText }}</span>
              <span v-else>ĐANG CHỜ KẾT QUẢ...</span>
            </div>
          </div>
          <div class="log-screen custom-scroll" ref="logContainer">
            <div v-for="(log, i) in battleLogs" :key="i" class="log-line">» {{ log }}</div>
          </div>
        </div>

        <div class="private-chat-panel">
          <div class="chat-head">CHAT ĐỐI THỦ</div>
          <div class="chat-body custom-scroll" ref="chatBox">
            <div v-for="(msg, i) in matchMessages" :key="i" class="chat-msg" :class="{'msg-me': msg.isMe}">
              <span class="sender">{{ msg.sender }}:</span> {{ msg.text }}
            </div>
          </div>
          <div class="chat-input">
            <input v-model="chatInput" @keyup.enter="sendPrivateChat" placeholder="Chat..." />
            <button @click="sendPrivateChat"><i class="fas fa-paper-plane"></i></button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useAuthStore } from '../stores/authStore';
import axios from 'axios';

// --- CONFIG ---
const API_URL = "http://localhost:8080/api/pvp";
const authStore = useAuthStore();

// --- STATE ---
const gameState = ref('LOBBY'); // LOBBY, SEARCHING, MATCH_FOUND, BATTLE
const matchId = ref(null);
const battlePhase = ref('RPS_WAIT'); 
const searchTimer = ref(0);
const acceptTimer = ref(10); // Timer 10s chấp nhận trận
const currentTurnCount = ref(1);
const hasAccepted = ref(false); // Check xem mình bấm chấp nhận chưa

// DATA
const myHp = ref(100); const myMaxHp = ref(100); const myRpsMove = ref(null);
const enemyName = ref("Đang tìm..."); const enemyLevel = ref(1);
const enemyHp = ref(100); const enemyMaxHp = ref(100); 
const enemyRpsMove = ref(null); const enemyAvatar = ref(null);

// UI
const isMyHit = ref(false); const isEnemyHit = ref(false); const lastResultText = ref('');
const battleLogs = ref([]); const matchMessages = ref([]); const chatInput = ref('');
const logContainer = ref(null); const chatBox = ref(null);

let pollId = null;
let searchInterval = null;
let acceptInterval = null;
let prevMyHp = 100; let prevEnemyHp = 100;

const getHeaders = () => ({ 'Authorization': `Bearer ${authStore.token}` });
const percent = (cur, max) => (max > 0 ? (cur / max) * 100 : 0);
const playSound = (t) => {}; // Đã tắt âm thanh

// --- ACTIONS ---

// 1. TÌM TRẬN
const toggleSearch = async () => {
  resetData();
  gameState.value = 'SEARCHING'; searchTimer.value = 0;
  if(searchInterval) clearInterval(searchInterval);
  searchInterval = setInterval(() => searchTimer.value++, 1000);

  try { await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() }); startPolling(); }
  catch(e) { console.error(e); alert("Lỗi kết nối Server!"); resetToLobby(); }
};

const cancelSearch = async () => { try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch(e){} resetToLobby(); };

// 2. CHẤP NHẬN / TỪ CHỐI
const acceptMatch = async () => {
  if(!matchId.value) return;
  hasAccepted.value = true;
  try {
    await axios.post(`${API_URL}/accept`, { matchId: matchId.value }, { headers: getHeaders() });
    // Không chuyển state ngay, đợi polling thấy ACTIVE thì mới chuyển
  } catch (e) {
    console.error(e);
  }
};

const declineMatch = async () => {
  // Từ chối coi như hủy tìm
  await cancelSearch();
};

// 3. START COUNTDOWN 10S
const startAcceptTimer = () => {
  if(acceptInterval) clearInterval(acceptInterval);
  acceptTimer.value = 10;
  acceptInterval = setInterval(() => {
    acceptTimer.value--;
    if(acceptTimer.value <= 0) {
      clearInterval(acceptInterval);
      if(!hasAccepted.value) declineMatch(); // Hết giờ tự hủy
    }
  }, 1000);
};

// 4. SYNC DATA (CORE LOGIC)
const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data;
      if (!data) return;

      if (data.status === 'SEARCHING') { 
         if(gameState.value !== 'SEARCHING') gameState.value = 'SEARCHING'; 
      }
      else if (data.status === 'PENDING') {
         // TÌM THẤY TRẬN -> HIỆN KHUNG CHẤP NHẬN
         if (gameState.value !== 'MATCH_FOUND') {
            gameState.value = 'MATCH_FOUND';
            if(searchInterval) clearInterval(searchInterval);
            
            // Sync thông tin địch để hiện lên
            syncBasicInfo(data);
            startAcceptTimer(); // Bắt đầu đếm 10s
         }
      }
      else if (data.status === 'ACTIVE') {
         // ĐÃ VÀO TRẬN -> CHUYỂN SANG BATTLE
         if (gameState.value !== 'BATTLE') {
            gameState.value = 'BATTLE';
            if(acceptInterval) clearInterval(acceptInterval); // Dừng đếm 10s
         }
         syncBattleData(data);
      }
      else if (data.status === 'FINISHED') {
         syncBattleData(data);
         stopPolling();
         const myCharId = Number(authStore.character?.id || authStore.user?.id);
         const iWin = (Number(data.winnerId) === myCharId);
         setTimeout(async () => {
            alert(iWin ? "🏆 CHIẾN THẮNG!" : "💀 THẤT BẠI!");
            await authStore.fetchCharacter();
            resetToLobby();
         }, 1500);
      }
    } catch (e) {
       // Ignore errors during polling
    }
  }, 1000);
};

const syncBasicInfo = (data) => {
  matchId.value = data.matchId;
  const myCharId = Number(authStore.character?.id || authStore.character?.charId || authStore.user?.id);
  const isP1 = (myCharId === Number(data.p1Id));
  
  enemyName.value = (isP1 ? data.p2Name : data.p1Name) || "Đối thủ";
  enemyLevel.value = (isP1 ? data.p2Level : data.p1Level) || 1;
  enemyAvatar.value = isP1 ? data.p2AvatarUrl : data.p1AvatarUrl;
};

const syncBattleData = (data) => {
  syncBasicInfo(data); // Update tên/info
  const myCharId = Number(authStore.character?.id || authStore.character?.charId || authStore.user?.id);
  const isP1 = (myCharId === Number(data.p1Id));

  const newMyHp = isP1 ? data.p1Hp : data.p2Hp;
  const newEnemyHp = isP1 ? data.p2Hp : data.p1Hp;
  myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;

  // Effect
  if(gameState.value === 'BATTLE') {
    if(newMyHp < prevMyHp) { isMyHit.value = true; setTimeout(()=>isMyHit.value=false, 500); }
    if(newEnemyHp < prevEnemyHp) { isEnemyHit.value = true; setTimeout(()=>isEnemyHit.value=false, 500); }
  }
  myHp.value = newMyHp; enemyHp.value = newEnemyHp;
  prevMyHp = newMyHp; prevEnemyHp = newEnemyHp;

  // Chat
  if(data.messages && data.messages.length > matchMessages.value.length) {
    data.messages.slice(matchMessages.value.length).forEach(m => matchMessages.value.push({ 
        sender: m.senderName, text: m.content, isMe: Number(m.senderId) === myCharId 
    }));
    nextTick(() => { if(chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight; });
  }

  // Battle Logic
  currentTurnCount.value = data.turnCount;
  const myM = isP1 ? data.p1Move : data.p2Move;
  const enM = isP1 ? data.p2Move : data.p1Move;
  myRpsMove.value = myM; enemyRpsMove.value = enM;

  if (!myM) {
      battlePhase.value = 'RPS_WAIT'; 
      if(enM === null) lastResultText.value = ''; 
  } else {
      battlePhase.value = 'RPS_REVEAL'; 
  }

  if (data.lastLog && battleLogs.value[battleLogs.value.length - 1] !== data.lastLog) {
      battleLogs.value.push(data.lastLog);
      if (data.lastLog.includes("thắng")) lastResultText.value = "THẮNG!";
      else if (data.lastLog.includes("Hòa") || data.lastLog.includes("HÒA")) lastResultText.value = "HÒA!";
      else lastResultText.value = "THUA!";
      nextTick(() => { if(logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight; });
  }
};

// 5. OTHER UTILS
const submitRps = async (move) => {
  if (!matchId.value) return;
  battlePhase.value = 'RPS_REVEAL'; 
  try { await axios.post(`${API_URL}/move`, { matchId: matchId.value, move }, { headers: getHeaders() }); } 
  catch (e) { battlePhase.value = 'RPS_WAIT'; }
};

const sendPrivateChat = async () => { if(chatInput.value.trim()){ try{ await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); }catch(e){} chatInput.value = ''; } };

const handleSurrender = async () => { 
  if(confirm("Bạn có chắc chắn muốn đầu hàng không?")) { 
    try { 
      // Gửi lệnh đầu hàng
      await axios.post(`${API_URL}/surrender`, { matchId: matchId.value }, { headers: getHeaders() }); 
    } catch (e) {
      console.error(e);
    } finally {
      // Quan trọng: Dù thành công hay lỗi, cũng buộc về sảnh ngay lập tức
      resetToLobby();
    }
  } 
};

const resetData = () => {
  matchId.value = null; battleLogs.value = []; matchMessages.value = []; lastResultText.value = '';
  enemyName.value = "Đang tìm đối thủ..."; enemyAvatar.value = null;
  hasAccepted.value = false;
};

const resetToLobby = () => { 
  gameState.value='LOBBY'; 
  resetData();
  stopPolling(); 
  if(searchInterval) clearInterval(searchInterval);
  if(acceptInterval) clearInterval(acceptInterval);
};

const stopPolling = () => { if(pollId) clearInterval(pollId); pollId=null; };
const getRpsIcon = (m) => ({ 'ROCK': 'fas fa-hand-rock', 'PAPER': 'fas fa-hand-paper', 'SCISSORS': 'fas fa-hand-scissors' }[m]);

onMounted(() => startPolling());
onUnmounted(() => resetToLobby());
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@700&family=Oswald:wght@400;500&display=swap');

/* --- LAYOUT --- */
.pvp-arena-container { position: relative; width: 100%; height: 92vh; background: #050505; color: #fff; font-family: 'Oswald', sans-serif; overflow: hidden; }
.arena-bg { position: absolute; inset: 0; background: url('https://i.imgur.com/S6L8F3h.jpeg') center/cover; opacity: 0.15; z-index: 0; }
.scanline { position: absolute; inset: 0; background: linear-gradient(rgba(18,16,16,0) 50%, rgba(0,0,0,0.2) 50%); background-size: 100% 4px; z-index: 2; pointer-events: none; }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle, transparent 30%, #000 100%); z-index: 1; pointer-events: none; }

.arena-header { position: relative; z-index: 10; text-align: center; height: 12%; padding-top: 15px; }
.title-main { font-family: 'Cinzel'; font-size: 2.2rem; color: #ffd700; text-shadow: 0 0 15px rgba(255, 215, 0, 0.6); margin: 0; }
.title-sub { font-size: 0.8rem; color: #888; letter-spacing: 4px; }

/* --- LOBBY --- */
.lobby-panel { position: relative; z-index: 50; width: 380px; margin: 60px auto; background: rgba(15,15,15,0.95); padding: 40px; border-radius: 20px; border: 1px solid #ffd70044; text-align: center; box-shadow: 0 0 50px rgba(0,0,0,0.8); }
.portrait-wrapper { width: 150px; height: 150px; margin: 0 auto 20px; border: 3px solid #ffd700; border-radius: 50%; padding: 5px; position: relative; background: #000; overflow: hidden; }
.portrait-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.lv-tag { position: absolute; bottom: 5px; right: 5px; background: #ffd700; color: #000; font-weight: bold; padding: 3px 10px; border-radius: 12px; font-size: 0.8rem; }
.name-text { font-family: 'Cinzel'; font-size: 1.8rem; color: #fff; margin-bottom: 10px; }
.win-badge { color: #4caf50; font-size: 1.1rem; letter-spacing: 1px; }
.btn-main-action { width: 100%; margin-top: 25px; padding: 18px; border: none; background: linear-gradient(135deg, #b8860b, #ffd700); font-family: 'Cinzel'; font-weight: bold; font-size: 1.1rem; cursor: pointer; border-radius: 8px; transition: 0.3s; z-index: 100; position: relative; }
.btn-main-action:hover { filter: brightness(1.2); transform: scale(1.02); }

/* --- MATCH FOUND (NEW) --- */
.match-found-panel { position: relative; z-index: 50; width: 100%; height: 80%; display: flex; align-items: center; justify-content: center; }
.match-alert-box { background: rgba(20,20,20,0.95); padding: 40px; border-radius: 15px; border: 2px solid #ffd700; text-align: center; width: 450px; box-shadow: 0 0 50px rgba(255, 215, 0, 0.3); animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
@keyframes popIn { from { transform: scale(0.5); opacity: 0; } to { transform: scale(1); opacity: 1; } }

.alert-title { font-family: 'Cinzel'; color: #ffd700; margin-bottom: 30px; font-size: 1.5rem; text-shadow: 0 0 10px #ff0000; }
.enemy-preview { display: flex; flex-direction: column; align-items: center; margin-bottom: 20px; }
.shadow-red { border-color: #ff4444; box-shadow: 0 0 20px #ff4444; }
.enemy-name { font-size: 1.5rem; font-weight: bold; margin-top: 10px; color: #fff; }
.enemy-lv { color: #888; font-size: 0.9rem; }
.countdown-circle { width: 60px; height: 60px; border-radius: 50%; border: 3px solid #fff; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: bold; margin: 20px auto; animation: pulse 1s infinite; }
.accept-actions { display: flex; gap: 15px; justify-content: center; }
.btn-accept { flex: 2; padding: 15px; background: #00cc00; border: none; color: #fff; font-weight: bold; cursor: pointer; border-radius: 5px; font-family: 'Oswald'; font-size: 1rem; }
.btn-accept:hover { background: #00ee00; }
.btn-accept:disabled { background: #555; cursor: not-allowed; }
.btn-decline { flex: 1; padding: 15px; background: transparent; border: 1px solid #ff4444; color: #ff4444; cursor: pointer; border-radius: 5px; font-weight: bold; }
.btn-decline:hover { background: #ff4444; color: #fff; }

/* --- BATTLE UI --- */
.battle-main-grid { position: relative; z-index: 10; height: 88%; display: flex; flex-direction: column; }
.arena-top { height: 60%; display: flex; flex-direction: column; justify-content: space-between; padding: 0 30px; }
.battle-hud { display: flex; justify-content: center; align-items: center; position: relative; margin-top: 10px; }
.turn-box { background: rgba(255,215,0,0.1); border: 1px solid #ffd70044; padding: 5px 25px; border-radius: 20px; color: #ffd700; font-weight: bold; }
.surrender-btn { position: absolute; right: 0; background: transparent; color: #ff4444; border: 1px solid #ff4444; padding: 5px 15px; border-radius: 5px; cursor: pointer; z-index: 50; transition: 0.3s; }
.surrender-btn:hover { background: #ff4444; color: #fff; }

.fighters-container { flex: 1; display: flex; justify-content: space-between; align-items: center; padding: 0 50px; }
.fighter-unit { width: 300px; display: flex; flex-direction: column; align-items: center; gap: 15px; }
.hp-info { width: 100%; text-align: center; }
.f-name { color: #fff; font-size: 1.2rem; margin-bottom: 5px; font-weight: 500; font-family: 'Cinzel'; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 250px; }
.hp-track { height: 16px; background: #111; border-radius: 8px; border: 1px solid #444; overflow: hidden; position: relative; width: 100%; }
.hp-bar { height: 100%; transition: width 0.5s ease-out; }
.p1-color { background: linear-gradient(90deg, #0066ff, #00ccff); box-shadow: 0 0 15px #0066ff; }
.p2-color { background: linear-gradient(-90deg, #cc0000, #ff3333); box-shadow: 0 0 15px #ff0000; }
.hp-num { font-size: 0.8rem; color: #ccc; margin-top: 4px; }
.f-avatar { position: relative; width: 200px; height: 200px; transition: transform 0.1s; }
.f-avatar img { width: 100%; height: 100%; object-fit: contain; filter: drop-shadow(0 0 20px rgba(0,0,0,0.7)); }
.flipped { transform: scaleX(-1); }
.vs-text { font-family: 'Cinzel'; font-size: 5rem; color: #fff; text-shadow: 0 0 30px #ff4500; font-style: italic; opacity: 0.8; }
.rps-badge { position: absolute; top: 0; width: 70px; height: 70px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2rem; z-index: 5; }
.p1-glow { background: #0066ff; color: #fff; box-shadow: 0 0 30px #0066ff; }
.p2-glow { background: #cc0000; color: #fff; box-shadow: 0 0 30px #cc0000; }

.arena-bottom { height: 40%; background: rgba(10,10,10,0.9); border-top: 1px solid #ffd70033; display: grid; grid-template-columns: 1fr 360px; gap: 20px; padding: 20px 40px; position: relative; z-index: 20; }
.control-panel { display: flex; flex-direction: column; gap: 15px; position: relative; z-index: 50; }
.rps-group { display: flex; gap: 30px; justify-content: center; padding: 15px; }
.rps-btn { position: relative; z-index: 100; width: 90px; height: 90px; border-radius: 50%; border: 2px solid #ffd700; display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 2rem; cursor: pointer; color: #ffd700; background: #000; transition: all 0.3s; box-shadow: 0 0 10px rgba(255, 215, 0, 0.2); }
.rps-btn:hover { background: #ffd700; color: #000; transform: translateY(-5px) scale(1.1); box-shadow: 0 0 30px #ffd700; }
.btn-label { font-size: 0.7rem; margin-top: 5px; font-weight: bold; font-family: 'Oswald'; }
.status-msg { text-align: center; color: #ffd700; font-size: 1.2rem; display: flex; justify-content: center; align-items: center; height: 90px; font-weight: bold; letter-spacing: 1px; }
.result-flash { font-weight: bold; font-size: 2rem; color: #ff4444; text-shadow: 0 0 20px #ff0000; animation: blink 0.5s infinite alternate; }
.log-screen { flex: 1; background: #080808; border: 1px solid #333; border-radius: 10px; padding: 15px; font-family: monospace; font-size: 0.9rem; color: #aaa; overflow-y: auto; }
.log-line { margin-bottom: 5px; border-left: 2px solid #555; padding-left: 8px; }

.private-chat-panel { background: #080808; border: 1px solid #ffd70033; border-radius: 12px; display: flex; flex-direction: column; overflow: hidden; position: relative; z-index: 50; }
.chat-head { background: rgba(255,215,0,0.1); padding: 8px; text-align: center; font-size: 0.8rem; color: #ffd700; font-weight: bold; border-bottom: 1px solid #ffd70022; }
.chat-body { flex: 1; padding: 10px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; }
.chat-msg { font-size: 0.9rem; color: #ccc; word-wrap: break-word; }
.msg-me { text-align: right; }
.msg-me .sender { color: #00ccff; }
.sender { color: #ffd700; font-weight: bold; margin-right: 5px; font-size: 0.8rem; }
.chat-input { display: flex; padding: 10px; border-top: 1px solid #222; background: #111; }
.chat-input input { flex: 1; background: transparent; border: none; color: #fff; padding: 0 10px; font-size: 0.9rem; outline: none; }
.chat-input button { background: #ffd700; border: none; padding: 5px 15px; border-radius: 5px; cursor: pointer; color: #000; font-weight: bold; }

@keyframes shake { 0% { transform: translate(1px, 1px); } 20% { transform: translate(-3px, 0px); } 40% { transform: translate(1px, -1px); } 60% { transform: translate(-3px, 1px); } 80% { transform: translate(-1px, -1px); } 100% { transform: translate(1px, -2px); } }
.hit-effect { animation: shake 0.5s; filter: sepia(1) hue-rotate(-50deg) saturate(5); }
@keyframes blink { from { opacity: 1; } to { opacity: 0.3; } }
@keyframes pulse { 0% { transform: scale(1); box-shadow: 0 0 10px #fff; } 50% { transform: scale(1.1); box-shadow: 0 0 20px #ff0000; color: #ff0000; border-color: #ff0000; } 100% { transform: scale(1); box-shadow: 0 0 10px #fff; } }
</style>