<template>
  <div class="page-container profile-page ancient-theme">
    <div class="wood-bg-layer"></div>

    <div class="profile-wrapper">
      <div class="profile-header">
        <button @click="$router.push('/')" class="btn-back wood-btn">
          <i class="fas fa-arrow-left"></i> VỀ SẢNH
        </button>
        <h2 class="header-title">DANH TƯỚNG LỤC</h2>
      </div>

      <div class="char-select-container">
        <div class="skin-list custom-scroll">
          <div
            v-for="skin in skins"
            :key="skin.id"
            class="skin-card"
            :class="{ active: selectedSkinId === skin.id }"
            @click="selectSkin(skin.id)"
          >
            <div class="skin-icon">
              <img :src="skin.sprites.idle" />
            </div>
            <div class="skin-info">
              <div class="skin-name">{{ skin.name }}</div>
              <div class="skin-desc">{{ skin.description }}</div>
            </div>
            <div v-if="currentSkinId === skin.id" class="equipped-icon">
              <i class="fas fa-check-circle"></i>
            </div>
          </div>
        </div>

        <div class="preview-stage">
          <div class="stage-bg">
            <div class="preview-actor">
              <img
                :src="previewImage"
                class="actor-img"
                :class="previewState"
              />
            </div>
          </div>

          <div class="control-bar">
            <div class="main-action">
              <button
                v-if="currentSkinId !== selectedSkinId"
                class="btn-equip"
                @click="saveSkin"
              >
                CHỌN NHÂN VẬT NÀY
              </button>
              <button v-else class="btn-equipped" disabled>ĐANG SỬ DỤNG</button>
            </div>
          </div>

          <div class="char-stats-box">
            <h3>THÔNG TIN</h3>
            <div class="stat-row">
              <span>Hiệu Danh:</span>
              <span class="hl">{{ authStore.user?.username }}</span>
            </div>
            <div class="stat-row">
              <span>Cấp Độ:</span>
              <span class="hl">{{ charStore.character?.lv || 1 }}</span>
            </div>
            <div class="rename-box">
              <input v-model="newName" placeholder="Đổi tên mới..." />
              <button @click="renameChar"><i class="fas fa-pen"></i></button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useAuthStore } from "../stores/authStore";
import { useCharacterStore } from "../stores/characterStore";
import { CHARACTER_SKINS } from "../utils/assetHelper";
import axiosClient from "../api/axiosClient";

const authStore = useAuthStore();
const charStore = useCharacterStore();

const skins = Object.values(CHARACTER_SKINS);
const currentSkinId = computed(() => authStore.user?.avatarUrl || "skin_yasou");
const selectedSkinId = ref("skin_yasou");

const previewState = ref("idle");
const newName = ref("");
let animInterval = null;

// [AUTO ANIMATION] Chu trình: Đứng -> Chạy -> Đánh
const animCycle = ["idle", "run", "attack"];
let animIndex = 0;

const previewImage = computed(() => {
  const skin = CHARACTER_SKINS[selectedSkinId.value];
  return skin ? skin.sprites[previewState.value] : "";
});

const selectSkin = (id) => {
  selectedSkinId.value = id;
  // Reset về idle khi chọn con mới
  animIndex = 0;
  previewState.value = "idle";
};

const saveSkin = async () => {
  try {
    await axiosClient.put("/user/update", { avatarUrl: selectedSkinId.value });
    await authStore.fetchProfile();
    alert("Đã thay đổi nhân vật thành công!");
  } catch (e) {
    alert("Lỗi lưu nhân vật: " + e.message);
  }
};

const renameChar = async () => {
  if (!newName.value) return;
  try {
    await axiosClient.post("/character/rename", { name: newName.value });
    await charStore.fetchCharacter();
    alert("Đổi tên thành công!");
  } catch (e) {
    alert(e.response?.data || "Lỗi đổi tên");
  }
};

onMounted(() => {
  charStore.fetchCharacter();
  selectedSkinId.value = currentSkinId.value;

  // [TIMER] Đổi hành động mỗi 2 giây
  animInterval = setInterval(() => {
    animIndex = (animIndex + 1) % animCycle.length;
    previewState.value = animCycle[animIndex];
  }, 2000);
});

onUnmounted(() => {
  if (animInterval) clearInterval(animInterval);
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.profile-page {
  background: #1a1a1a;
  min-height: 100vh;
  color: #d7ccc8;
  font-family: "Noto Serif TC", serif;
}

.wood-bg-layer {
  position: absolute;
  inset: 0;
  background-image: url("https://www.transparenttextures.com/patterns/wood-pattern.png");
  opacity: 0.2;
  pointer-events: none;
}

.profile-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 10;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #5d4037;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.header-title {
  font-size: 2em;
  color: #fbc02d;
  font-weight: 900;
  margin: 0;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.wood-btn {
  background: #3e2723;
  color: #fff;
  border: 1px solid #5d4037;
  padding: 8px 15px;
  cursor: pointer;
  font-family: inherit;
  font-weight: bold;
}

.char-select-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  height: 70vh;
}

/* LIST SKINS */
.skin-list {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #5d4037;
  border-radius: 8px;
  overflow-y: auto;
  padding: 10px;
}

.skin-card {
  display: flex;
  gap: 10px;
  padding: 10px;
  background: #261815;
  border: 1px solid #3e2723;
  margin-bottom: 10px;
  cursor: pointer;
  transition: 0.2s;
  position: relative;
  align-items: center;
}

.skin-card:hover {
  border-color: #8d6e63;
  background: #3e2723;
}

.skin-card.active {
  background: #4e342e;
  border-color: #fbc02d;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.5);
}

.skin-icon {
  width: 50px;
  height: 50px;
  background: #000;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #3e2723;
}

.skin-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.skin-info {
  flex: 1;
}

.skin-name {
  font-weight: bold;
  color: #fbc02d;
  font-size: 1.1em;
}

.skin-desc {
  font-size: 0.8em;
  color: #a1887f;
  font-style: italic;
}

.equipped-icon {
  color: #2e7d32;
  font-size: 1.2em;
}

/* PREVIEW STAGE */
.preview-stage {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stage-bg {
  flex: 1;
  background: #000;
  border: 2px solid #5d4037;
  border-radius: 8px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: radial-gradient(circle, #261815 0%, #000 80%);
  overflow: hidden;
}

.preview-actor {
  width: 128px;
  height: 128px;
  position: relative;
  z-index: 5;
  /* Đẩy nhân vật xuống một chút cho cân đối vì đã bỏ đế */
  margin-top: 20px;
}

.actor-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(2);
  image-rendering: pixelated;
  transition: 0.3s ease;
  filter: drop-shadow(
    0 10px 10px rgba(0, 0, 0, 0.8)
  ); /* Thêm bóng đổ tự nhiên */
}

/* Control & Info */
.control-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #261815;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #3e2723;
}

.main-action {
  width: 100%;
  display: flex;
  justify-content: center;
}

.btn-equip {
  background: #fbc02d;
  color: #000;
  font-weight: bold;
  border: none;
  padding: 10px 20px;
  cursor: pointer;
  border-radius: 4px;
  width: 100%;
  transition: 0.2s;
}

.btn-equip:hover {
  background: #fdd835;
  transform: translateY(-2px);
}

.btn-equipped {
  background: #3e2723;
  color: #757575;
  border: 1px solid #5d4037;
  padding: 10px 20px;
  border-radius: 4px;
  opacity: 1;
  cursor: default;
  width: 100%;
  font-weight: bold;
}

.char-stats-box {
  background: #261815;
  padding: 15px;
  border: 1px solid #5d4037;
  border-radius: 8px;
}

.char-stats-box h3 {
  margin: 0 0 10px 0;
  color: #fbc02d;
  font-size: 1.1em;
  border-bottom: 1px solid #3e2723;
  padding-bottom: 5px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.95em;
  border-bottom: 1px dashed #3e2723;
  padding-bottom: 4px;
}

.hl {
  color: #fbc02d;
  font-weight: bold;
}

.rename-box {
  display: flex;
  margin-top: 15px;
  gap: 5px;
}

.rename-box input {
  flex: 1;
  background: #1a1a1a;
  border: 1px solid #5d4037;
  color: #fff;
  padding: 8px;
  font-family: inherit;
}

.rename-box button {
  background: #3e2723;
  color: #fbc02d;
  border: 1px solid #5d4037;
  cursor: pointer;
  width: 40px;
}
.rename-box button:hover {
  background: #4e342e;
}

/* Scrollbar */
.custom-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: #5d4037;
  border-radius: 3px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}
</style>
