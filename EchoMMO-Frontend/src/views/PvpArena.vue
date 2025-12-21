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
              <div class="hp-track">
                <div class="hp-bar p1-color" :style="{ width: percent(myHp, myMaxHp) + '%' }"></div>
              </div>
              <div class="hp-num">{{ myHp }} / {{ myMaxHp }}</div>
            </div>
            <div class="f-avatar" :class="{ 'hit-effect': isMyHit }">
              <img :src="authStore.character?.avatarUrl || 'https://i.imgur.com/7Y7t5Xp.png'" />
              <div v-if="battlePhase === 'RPS_REVEAL' && myRpsMove" class="rps-badge p1-glow">
                <i :class="getRpsIcon(myRpsMove)"></i>
              </div>
            </div>
          </div>

          <div class="vs-text">VS</div>

          <div class="fighter-unit p2">
            <div class="hp-info">
              <div class="f-name">{{ enemyName }}</div>
              <div class="hp-track">
                <div class="hp-bar p2-color" :style="{ width: percent(enemyHp, enemyMaxHp) + '%' }"></div>
              </div>
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
            <div v-if="battlePhase === 'RPS_WAIT' && gameState === 'BATTLE'" class="rps-group">
              <button @click="submitRps('ROCK')" class="rps-btn" title="Búa"><i class="fas fa-hand-rock"></i></button>
              <button @click="submitRps('PAPER')" class="rps-btn" title="Bao"><i class="fas fa-hand-paper"></i></button>
              <button @click="submitRps('SCISSORS')" class="rps-btn" title="Kéo"><i class="fas fa-hand-scissors"></i></button>
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
            <input v-model="chatInput" @keyup.enter="sendPrivateChat" placeholder="Chat..." maxlength="50" />
            <button @click="sendPrivateChat"><i class="fas fa-paper-plane"></i></button>
          </div>
        </div>
      </section>
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

// --- STATE ---
const gameState = ref('LOBBY'); 
const matchId = ref(null);
const battlePhase = ref('RPS_WAIT'); 
const searchTimer = ref(0);
const currentTurnCount = ref(1);

// INFO
const myHp = ref(100); const myMaxHp = ref(100); const myRpsMove = ref(null);
const enemyName = ref("Đang tìm..."); const enemyHp = ref(100); const enemyMaxHp = ref(100); 
const enemyRpsMove = ref(null); const enemyAvatar = ref(null);

// UI/FX
const isMyHit = ref(false);
const isEnemyHit = ref(false);
const lastResultText = ref('');
const battleLogs = ref([]);
const matchMessages = ref([]);
const chatInput = ref('');
const logContainer = ref(null);
const chatBox = ref(null);

let pollId = null;
let searchInterval = null;
let prevMyHp = 100;
let prevEnemyHp = 100;

// SOUNDS
const sounds = {
  find: new Audio('https://freesound.org/data/previews/171/171671_2437358-lq.mp3'),
  hit: new Audio('https://freesound.org/data/previews/566/566435_10793605-lq.mp3'),
  win: new Audio('https://freesound.org/data/previews/270/270404_5123851-lq.mp3'),
};
const playSound = (t) => { try { if(sounds[t]) { sounds[t].currentTime=0; sounds[t].volume=0.4; sounds[t].play(); } } catch(e){} };
const getHeaders = () => ({ 'Authorization': `Bearer ${authStore.token}` });
const percent = (cur, max) => (max > 0 ? (cur / max) * 100 : 0);

// --- FUNCTIONS ---

// 1. GỬI CHIÊU (Đã fix lỗi 400)
const submitRps = async (move) => {
  // DEBUG: Kiểm tra dữ liệu trước khi gửi
  console.log("--> Chuẩn bị gửi chiêu:", move);
  console.log("--> Match ID hiện tại:", matchId.value);

  if (!matchId.value) {
    alert("Lỗi: Chưa có ID trận đấu! Đang đồng bộ lại...");
    return;
  }

  // UI phản hồi ngay lập tức để tránh bấm nhiều lần
  battlePhase.value = 'RPS_REVEAL'; 

  try {
    // Payload phải đúng tên biến mà Java Backend yêu cầu
    const payload = { 
        matchId: matchId.value, 
        move: move 
    };
    
    await axios.post(`${API_URL}/move`, payload, { headers: getHeaders() });
    console.log("--> Gửi thành công!");
  } catch (e) {
    console.error("--> Lỗi gửi chiêu:", e);
    
    // Nếu lỗi 400, in ra chi tiết để sửa Backend
    if(e.response && e.response.status === 400) {
        console.error("DATA SERVER TỪ CHỐI:", e.response.data);
        alert("Lỗi Dữ Liệu (400): Server không hiểu lệnh này. Kiểm tra log!");
    } else {
        alert("Mất kết nối server!");
    }
    
    // Nếu lỗi thì hiện lại nút cho bấm lại
    battlePhase.value = 'RPS_WAIT'; 
  }
};

// 2. TÌM TRẬN
const toggleSearch = async () => {
  matchId.value = null; battleLogs.value = []; matchMessages.value = []; lastResultText.value = '';
  gameState.value = 'SEARCHING'; searchTimer.value = 0;
  if(searchInterval) clearInterval(searchInterval);
  searchInterval = setInterval(() => searchTimer.value++, 1000);

  try { await axios.post(`${API_URL}/find`, {}, { headers: getHeaders() }); startPolling(); }
  catch(e) { console.error(e); alert("Lỗi kết nối Server!"); resetToLobby(); }
};

const cancelSearch = async () => { try { await axios.post(`${API_URL}/cancel`, {}, { headers: getHeaders() }); } catch(e){} resetToLobby(); };

// 3. POLLING (HỎI SERVER LIÊN TỤC)
const startPolling = () => {
  if (pollId) return;
  pollId = setInterval(async () => {
    try {
      const res = await axios.get(`${API_URL}/status`, { headers: getHeaders() });
      const data = res.data;
      if (!data) return;

      if (data.status === 'SEARCHING') { if(gameState.value !== 'SEARCHING') gameState.value = 'SEARCHING'; }
      else if (data.status === 'PENDING' || data.status === 'ACTIVE') syncData(data);
      else if (data.status === 'FINISHED') syncData(data);
    } catch (e) {
      if (e.response && (e.response.status === 400 || e.response.status === 404)) { if(gameState.value === 'BATTLE') resetToLobby(); }
    }
  }, 1000);
};

// 4. ĐỒNG BỘ DỮ LIỆU
const syncData = async (data) => {
  matchId.value = data.matchId; // Quan trọng: Cập nhật ID để dùng cho hàm submitRps
  
  const charId = authStore.character?.id;
  const isP1 = (data.p1Id == charId);

  // Set Info
  const newMyHp = isP1 ? data.p1Hp : data.p2Hp;
  const newEnemyHp = isP1 ? data.p2Hp : data.p1Hp;
  myMaxHp.value = isP1 ? data.p1MaxHp : data.p2MaxHp;
  enemyMaxHp.value = isP1 ? data.p2MaxHp : data.p1MaxHp;
  enemyName.value = isP1 ? data.p2Name : data.p1Name;
  enemyAvatar.value = isP1 ? data.p2AvatarUrl : data.p1AvatarUrl;

  // Sound FX
  if(gameState.value === 'BATTLE') {
    if(newMyHp < prevMyHp) { isMyHit.value = true; playSound('hit'); setTimeout(()=>isMyHit.value=false, 500); }
    if(newEnemyHp < prevEnemyHp) { isEnemyHit.value = true; playSound('hit'); setTimeout(()=>isEnemyHit.value=false, 500); }
  }
  myHp.value = newMyHp; enemyHp.value = newEnemyHp;
  prevMyHp = newMyHp; prevEnemyHp = newEnemyHp;

  // Chat Sync
  if(data.messages && data.messages.length > matchMessages.value.length) {
    data.messages.slice(matchMessages.value.length).forEach(m => matchMessages.value.push({ sender: m.senderName, text: m.content, isMe: m.senderId == charId }));
    nextTick(() => { if(chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight; });
  }

  // Game Logic
  if (data.status === 'ACTIVE' || data.status === 'PENDING') {
    if (gameState.value !== 'BATTLE') {
        gameState.value = 'BATTLE'; playSound('find');
        if(searchInterval) clearInterval(searchInterval);
    }
    currentTurnCount.value = data.turnCount;
    
    // Check Move
    const myM = isP1 ? data.p1Move : data.p2Move;
    const enM = isP1 ? data.p2Move : data.p1Move;
    myRpsMove.value = myM; enemyRpsMove.value = enM;

    // Logic Ẩn/Hiện nút
    if (!myM) {
        battlePhase.value = 'RPS_WAIT'; // Chưa đánh -> Hiện nút
        lastResultText.value = ''; 
    } else {
        battlePhase.value = 'RPS_REVEAL'; // Đánh rồi -> Ẩn nút
    }

    // Log
    if (data.lastLog && battleLogs.value[battleLogs.value.length - 1] !== data.lastLog) {
      battleLogs.value.push(data.lastLog);
      if (data.lastLog.includes("thắng")) lastResultText.value = "THẮNG HIỆP!";
      else if (data.lastLog.includes("Hòa")) lastResultText.value = "HÒA!";
      else lastResultText.value = "THUA HIỆP!";
      nextTick(() => { if(logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight; });
    }
  } 
  else if (data.status === 'FINISHED') {
    stopPolling();
    const iWin = (data.winnerId == charId);
    if(iWin) playSound('win');
    setTimeout(async () => {
        alert(iWin ? "🏆 CHIẾN THẮNG!" : "💀 THẤT BẠI!");
        await authStore.fetchCharacter();
        resetToLobby();
    }, 1000);
  }
};

// Utils
const sendPrivateChat = async () => { if(chatInput.value.trim()){ try{ await axios.post(`${API_URL}/chat`, { matchId: matchId.value, message: chatInput.value }, { headers: getHeaders() }); }catch(e){} chatInput.value = ''; } };
const handleSurrender = async () => { if(confirm("Đầu hàng?")){ try{ await axios.post(`${API_URL}/surrender`, { matchId: matchId.value }, { headers: getHeaders() }); }finally{ resetToLobby(); } } };
const resetToLobby = () => { gameState.value='LOBBY'; matchId.value=null; matchMessages.value=[]; battleLogs.value=[]; stopPolling(); if(searchInterval) clearInterval(searchInterval); };
const stopPolling = () => { if(pollId) clearInterval(pollId); pollId=null; };
const getRpsIcon = (m) => ({ 'ROCK': 'fas fa-hand-rock', 'PAPER': 'fas fa-hand-paper', 'SCISSORS': 'fas fa-hand-scissors' }[m]);

onMounted(() => startPolling());
onUnmounted(() => resetToLobby());
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@700&family=Oswald:wght@400;500&display=swap');

/* --- CẤU TRÚC CHÍNH --- */
.pvp-arena-container { position: relative; width: 100%; height: 92vh; background: #050505; color: #fff; font-family: 'Oswald', sans-serif; overflow: hidden; }
.arena-bg { position: absolute; inset: 0; background: url('https://i.imgur.com/S6L8F3h.jpeg') center/cover; opacity: 0.15; z-index: 0; }
.scanline { position: absolute; inset: 0; background: linear-gradient(rgba(18,16,16,0) 50%, rgba(0,0,0,0.2) 50%); background-size: 100% 4px; z-index: 2; pointer-events: none; }
.vignette { position: absolute; inset: 0; background: radial-gradient(circle, transparent 30%, #000 100%); z-index: 1; pointer-events: none; }

.arena-header { position: relative; z-index: 10; text-align: center; height: 12%; padding-top: 15px; }
.title-main { font-family: 'Cinzel'; font-size: 2.2rem; color: #ffd700; text-shadow: 0 0 15px rgba(255, 215, 0, 0.6); margin: 0; }
.title-sub { font-size: 0.8rem; color: #888; letter-spacing: 4px; }

/* --- BATTLE AREA --- */
.battle-main-grid { position: relative; z-index: 10; height: 88%; display: flex; flex-direction: column; }
.arena-top { height: 60%; display: flex; flex-direction: column; justify-content: space-between; padding: 0 30px; }
.battle-hud { display: flex; justify-content: center; align-items: center; position: relative; margin-top: 10px; }
.turn-box { background: rgba(255,215,0,0.1); border: 1px solid #ffd70044; padding: 5px 25px; border-radius: 20px; color: #ffd700; font-weight: bold; }
.surrender-btn { position: absolute; right: 0; background: transparent; color: #ff4444; border: 1px solid #ff4444; padding: 5px 15px; border-radius: 5px; cursor: pointer; z-index: 50; }

.fighters-container { flex: 1; display: flex; justify-content: space-between; align-items: center; padding: 0 50px; }
.fighter-unit { width: 300px; display: flex; flex-direction: column; align-items: center; gap: 15px; }
.hp-info { width: 100%; text-align: center; }
.f-name { color: #fff; font-size: 1.2rem; margin-bottom: 5px; font-weight: 500; font-family: 'Cinzel'; }
.hp-track { height: 16px; background: #111; border-radius: 8px; border: 1px solid #444; overflow: hidden; }
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

/* --- CONTROLS --- */
.arena-bottom { height: 40%; background: rgba(10,10,10,0.9); border-top: 1px solid #ffd70033; display: grid; grid-template-columns: 1fr 360px; gap: 20px; padding: 20px 40px; position: relative; z-index: 20; }
.control-panel { display: flex; flex-direction: column; gap: 15px; position: relative; z-index: 50; }
.rps-group { display: flex; gap: 25px; justify-content: center; padding: 10px; }
.rps-btn { position: relative; z-index: 100; width: 70px; height: 70px; border-radius: 50%; border: 2px solid #ffd700; display: flex; align-items: center; justify-content: center; font-size: 1.8rem; cursor: pointer; color: #ffd700; background: #000; transition: 0.3s; }
.rps-btn:hover { background: #ffd700; color: #000; transform: translateY(-8px); box-shadow: 0 0 25px #ffd700; }
.status-msg { text-align: center; color: #ffd700; font-size: 1.1rem; display: flex; justify-content: center; align-items: center; height: 70px; }
.result-flash { font-weight: bold; font-size: 1.5rem; color: #ff4444; animation: blink 0.5s infinite alternate; }
.log-screen { flex: 1; background: #080808; border: 1px solid #333; border-radius: 10px; padding: 15px; font-family: monospace; font-size: 0.85rem; color: #aaa; overflow-y: auto; }
.log-line { margin-bottom: 5px; border-left: 2px solid #555; padding-left: 8px; }

/* --- CHAT --- */
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

/* --- LOBBY --- */
.lobby-panel { position: relative; z-index: 50; width: 380px; margin: 60px auto; background: rgba(15,15,15,0.95); padding: 40px; border-radius: 20px; border: 1px solid #ffd70044; text-align: center; box-shadow: 0 0 50px rgba(0,0,0,0.8); }
.portrait-wrapper { width: 150px; height: 150px; margin: 0 auto 20px; border: 3px solid #ffd700; border-radius: 50%; padding: 5px; position: relative; background: #000; }
.portrait-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.lv-tag { position: absolute; bottom: 5px; right: 5px; background: #ffd700; color: #000; font-weight: bold; padding: 3px 10px; border-radius: 12px; font-size: 0.8rem; }
.name-text { font-family: 'Cinzel'; font-size: 1.8rem; color: #fff; margin-bottom: 10px; }
.win-badge { color: #4caf50; font-size: 1.1rem; letter-spacing: 1px; }
.btn-main-action { width: 100%; margin-top: 25px; padding: 18px; border: none; background: linear-gradient(135deg, #b8860b, #ffd700); font-family: 'Cinzel'; font-weight: bold; font-size: 1.1rem; cursor: pointer; border-radius: 8px; transition: 0.3s; z-index: 100; position: relative; }
.btn-main-action:hover { filter: brightness(1.2); transform: scale(1.02); }

/* --- ANIMATION --- */
@keyframes shake {
  0% { transform: translate(1px, 1px); } 20% { transform: translate(-3px, 0px); } 40% { transform: translate(1px, -1px); }
  60% { transform: translate(-3px, 1px); } 80% { transform: translate(-1px, -1px); } 100% { transform: translate(1px, -2px); }
}
.hit-effect { animation: shake 0.5s; filter: sepia(1) hue-rotate(-50deg) saturate(5); }
@keyframes blink { from { opacity: 1; } to { opacity: 0.3; } }
</style>