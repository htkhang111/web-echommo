<template>
  <transition name="fade">
    <div class="security-overlay" v-if="isVisible">
      <div class="scan-grid"></div>

      <div
        class="security-terminal"
        :class="{ 'shake-anim': errorMsg, 'success-mode': isSuccess }"
      >
        <div class="scan-bar"></div>

        <div class="term-header">
          <div class="icon-box warning-blink">
            <i class="fas fa-shield-virus"></i>
          </div>
          <div class="text-info">
            <h3 class="glitch-text" data-text="SECURITY ALERT">
              SECURITY ALERT
            </h3>
            <p class="sub-text">BOT ACTIVITY DETECTED // VERIFY IDENTITY</p>
          </div>
        </div>

        <div v-if="isLoading" class="loading-zone">
          <div class="loader-ring"></div>
          <span>DOWNLOADING PROTOCOLS...</span>
        </div>

        <div v-else-if="isSuccess" class="success-zone">
          <i class="fas fa-unlock-alt big-icon"></i>
          <h2>ACCESS GRANTED</h2>
          <p>IDENTITY CONFIRMED. PROCEEDING...</p>
        </div>

        <div v-else class="puzzle-zone">
          <div class="instruction-box">
            <span class="cmd-prompt">> TARGET:</span>
            <span class="target-name">{{ question }}</span>
          </div>

          <div class="hex-grid">
            <div
              v-for="item in options"
              :key="item.itemId"
              class="hex-option"
              @click="submitAnswer(item.itemId)"
            >
              <div class="hex-content">
                <img
                  :src="item.imageUrl"
                  @error="
                    $event.target.src =
                      'https://via.placeholder.com/64?text=ERR'
                  "
                />
              </div>
              <div class="hex-border"></div>
            </div>
          </div>
        </div>

        <div class="term-footer">
          <div v-if="errorMsg" class="status-msg error">
            <i class="fas fa-times-circle"></i> {{ errorMsg }}
          </div>
          <div v-else class="status-msg info">AWAITING INPUT...</div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
// [ĐÃ SỬA] Bỏ defineExpose ra khỏi import vì nó là macro mặc định
import { ref } from "vue";
import axiosClient from "../api/axiosClient";

const isVisible = ref(false);
const isLoading = ref(false);
const isSuccess = ref(false); 
const question = ref("");
const options = ref([]);
const errorMsg = ref("");

const open = async () => {
  isVisible.value = true;
  isSuccess.value = false;
  errorMsg.value = "";
  await fetchCaptcha();
};

const fetchCaptcha = async () => {
  isLoading.value = true;
  try {
    const res = await axiosClient.get("/captcha/generate");
    question.value = res.data.question;
    options.value = res.data.options;
  } catch (e) {
    errorMsg.value = "CONNECTION LOST. RETRYING...";
  } finally {
    isLoading.value = false;
  }
};

const submitAnswer = async (itemId) => {
  try {
    errorMsg.value = "";
    await axiosClient.post("/captcha/solve", { itemId });
    isSuccess.value = true;
    setTimeout(() => {
      isVisible.value = false;
    }, 1500);
  } catch (e) {
    errorMsg.value = "ACCESS DENIED. INCORRECT TARGET.";
    setTimeout(() => {
      errorMsg.value = ""; 
      fetchCaptcha();
    }, 1000);
  }
};

// Vẫn gọi hàm defineExpose bình thường (không cần import)
defineExpose({ open });
</script>

<style scoped>
/* OVERLAY */
.security-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(10, 0, 0, 0.95);
  z-index: 10000; 
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(5px);
}

.scan-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 0, 0, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 0, 0, 0.1) 1px, transparent 1px);
  background-size: 30px 30px;
  pointer-events: none;
}

/* TERMINAL BOX */
.security-terminal {
  position: relative;
  width: 450px;
  background: #050000;
  border: 1px solid #ff0055;
  box-shadow: 0 0 50px rgba(255, 0, 85, 0.2);
  padding: 2px; 
  overflow: hidden;
  font-family: "Courier New", monospace;
  transition: 0.3s;
}

.security-terminal.success-mode {
  border-color: #2ecc71;
  box-shadow: 0 0 50px rgba(46, 204, 113, 0.4);
}

/* Scan Line Animation */
.scan-bar {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background: rgba(255, 0, 85, 0.5);
  box-shadow: 0 0 10px #ff0055;
  animation: scan 2s infinite linear;
  pointer-events: none;
  z-index: 10;
}
.success-mode .scan-bar {
  background: #2ecc71;
  box-shadow: 0 0 10px #2ecc71;
}

/* HEADER */
.term-header {
  background: rgba(255, 0, 85, 0.1);
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  border-bottom: 1px solid #ff0055;
}
.success-mode .term-header {
  background: rgba(46, 204, 113, 0.1);
  border-color: #2ecc71;
}

.icon-box {
  font-size: 2em;
  color: #ff0055;
}
.success-mode .icon-box {
  color: #2ecc71;
  animation: none;
}
.warning-blink {
  animation: blink 0.5s infinite alternate;
}

.text-info h3 {
  margin: 0;
  font-size: 1.5em;
  color: #fff;
  letter-spacing: 2px;
}
.sub-text {
  margin: 0;
  font-size: 0.7em;
  color: #ff0055;
  letter-spacing: 1px;
}
.success-mode .sub-text {
  color: #2ecc71;
}

/* LOADING */
.loading-zone {
  padding: 40px;
  text-align: center;
  color: #ff0055;
}
.loader-ring {
  width: 40px;
  height: 40px;
  border: 4px solid #ff0055;
  border-top-color: transparent;
  border-radius: 50%;
  margin: 0 auto 15px;
  animation: spin 1s infinite linear;
}

/* SUCCESS ZONE */
.success-zone {
  padding: 40px;
  text-align: center;
  color: #2ecc71;
}
.big-icon {
  font-size: 4em;
  margin-bottom: 15px;
  animation: pop 0.5s;
}
.success-zone h2 {
  margin: 0;
  letter-spacing: 2px;
}

/* PUZZLE AREA */
.puzzle-zone {
  padding: 20px;
}

.instruction-box {
  background: #1a0000;
  border: 1px dashed #ff0055;
  padding: 10px;
  margin-bottom: 20px;
  text-align: center;
  color: #fff;
  font-size: 1.1em;
}
.cmd-prompt {
  color: #ff0055;
  margin-right: 10px;
  font-weight: bold;
}
.target-name {
  color: #ffd700;
  font-weight: bold;
  text-transform: uppercase;
  text-decoration: underline;
}

/* HEX GRID */
.hex-grid {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
}

.hex-option {
  width: 80px;
  height: 80px;
  position: relative;
  cursor: pointer;
  transition: 0.2s;
}
.hex-option:hover {
  transform: scale(1.1);
  z-index: 5;
}

.hex-content {
  width: 100%;
  height: 100%;
  background: #000;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
}
.hex-content img {
  width: 60%;
  height: 60%;
  object-fit: contain;
}

.hex-border {
  position: absolute;
  inset: -2px;
  background: #333;
  z-index: 1;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  transition: 0.3s;
}
.hex-option:hover .hex-border {
  background: #ff0055;
}

/* FOOTER */
.term-footer {
  padding: 10px;
  background: #0a0000;
  border-top: 1px solid #333;
  font-size: 0.8em;
  text-align: right;
  letter-spacing: 1px;
}
.status-msg.error {
  color: #ff0055;
  font-weight: bold;
  animation: shake 0.3s;
}
.status-msg.info {
  color: #666;
  animation: blink 1s infinite;
}

/* ANIMATIONS */
@keyframes scan {
  0% {
    top: 0%;
  }
  100% {
    top: 100%;
  }
}
@keyframes blink {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@keyframes pop {
  0% {
    transform: scale(0);
  }
  80% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.shake-anim {
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}
@keyframes shake {
  10%,
  90% {
    transform: translate3d(-2px, 0, 0);
  }
  20%,
  80% {
    transform: translate3d(4px, 0, 0);
  }
  30%,
  50%,
  70% {
    transform: translate3d(-8px, 0, 0);
  }
  40%,
  60% {
    transform: translate3d(8px, 0, 0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>