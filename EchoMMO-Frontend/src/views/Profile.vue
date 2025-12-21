<template>
  <div class="page-container profile-page ancient-theme">
    <div class="wood-bg-layer"></div>

    <div class="profile-wrapper">
      <div class="profile-header">
        <button @click="$router.push('/')" class="btn-back wood-btn">
          <i class="fas fa-arrow-left"></i> VỀ SẢNH
        </button>
        <h2 class="header-title">HỒ SƠ TU TIÊN</h2>
      </div>

      <div class="profile-content">
        <div class="info-column">
          <h3 class="section-title"><i class="fas fa-id-card"></i> THÔNG TIN CÁ NHÂN</h3>
          
          <div class="avatar-edit-box">
            <div class="current-avatar">
              <img :src="getAvatarUrl(form.profileImageUrl)" @error="handleImgError" />
            </div>
            
            <div class="upload-controls">
                <label>Ảnh Đại Diện:</label>
                <div class="input-tabs">
                    <button @click="uploadMode = 'file'" :class="{active: uploadMode === 'file'}">Tải Lên</button>
                    <button @click="uploadMode = 'url'" :class="{active: uploadMode === 'url'}">Link URL</button>
                </div>

                <div v-if="uploadMode === 'file'" class="input-group file-group">
                    <input type="file" @change="handleFileUpload" accept="image/*" />
                    <div v-if="isUploading" class="uploading-text"><i class="fas fa-spinner fa-spin"></i> Đang tải lên...</div>
                </div>

                 <div v-if="uploadMode === 'url'" class="input-group">
                    <input v-model="form.profileImageUrl" placeholder="https://..." />
                </div>
            </div>
          </div>

          <div class="info-form">
            <div class="form-group">
              <label>Họ Tên (Biệt Danh):</label>
              <input v-model="form.fullName" placeholder="Nhập tên hiển thị..." />
            </div>

            <div class="form-group">
              <label>Tên Đăng Nhập:</label>
              <input v-model="form.username" placeholder="Tên đăng nhập..." />
            </div>

            <div class="form-group">
              <label>Email Liên Lạc:</label>
              <input v-model="form.email" placeholder="Email..." />
            </div>

            <div class="form-group">
              <label>Mật Khẩu Mới (Bỏ trống nếu không đổi):</label>
              <input v-model="form.password" type="password" placeholder="******" />
            </div>

            <div class="form-group read-only">
              <label>Ngày Nhập Môn (Cố Định):</label>
              <div class="static-val">{{ dob }}</div>
              <small class="hint">*Đây là ngày Cưng bắt đầu tu tiên, không sửa được đâu nhé!</small>
            </div>

            <button class="btn-save" @click="saveProfile">
              <i class="fas fa-save"></i> LƯU THAY ĐỔI
            </button>
          </div>
        </div>

        <div class="char-select-column">
          <h3 class="section-title"><i class="fas fa-mask"></i> HÓA THÂN CHI THUẬT</h3>
          
          <div class="preview-stage">
            <div class="stage-bg">
              <div class="preview-actor">
                <img :src="previewImage" class="actor-img" :class="previewState" />
              </div>
            </div>
            <div class="control-bar">
               <button v-if="currentSkinId !== selectedSkinId" class="btn-equip" @click="saveSkin">
                CHỌN SKIN NÀY
              </button>
              <button v-else class="btn-equipped" disabled>ĐANG SỬ DỤNG</button>
            </div>
          </div>

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
              </div>
              <div v-if="currentSkinId === skin.id" class="equipped-icon">
                <i class="fas fa-check-circle"></i>
              </div>
            </div>
          </div>

          <div class="char-stats-box">
             <div class="rename-box">
              <input v-model="newName" placeholder="Đổi tên nhân vật game..." />
              <button @click="renameChar"><i class="fas fa-pen"></i></button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive } from "vue";
import { useAuthStore } from "../stores/authStore";
import { useCharacterStore } from "../stores/characterStore";
import { CHARACTER_SKINS } from "../utils/assetHelper";
import axiosClient from "../api/axiosClient";

const authStore = useAuthStore();
const charStore = useCharacterStore();

// --- LOGIC HỒ SƠ ---
const uploadMode = ref('file');
const isUploading = ref(false);

const form = reactive({
  fullName: "",
  username: "",
  email: "",
  password: "",
  profileImageUrl: ""
});

const defaultAvatar = "https://placehold.co/150?text=Avatar";

// [QUAN TRỌNG] Backend chạy port nào thì sửa port đó (Mặc định 8080)
const BACKEND_URL = "http://localhost:8080";

// Hiển thị ngày đăng ký (Không cho sửa)
const dob = computed(() => {
  if (!authStore.user?.createdAt) return "Chưa cập nhật";
  return new Date(authStore.user.createdAt).toLocaleDateString('vi-VN');
});

// Helper: Xử lý hiển thị đường dẫn ảnh
const getAvatarUrl = (path) => {
  if (!path) return defaultAvatar;
  if (path.startsWith("http")) return path;
  // Nếu là đường dẫn tương đối (/uploads/...) thì nối thêm domain backend
  return `${BACKEND_URL}${path}`;
};

const handleImgError = (e) => {
  e.target.src = defaultAvatar;
};

// Xử lý upload file
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  if (!file.type.match('image.*')) {
    alert("Chỉ được chọn file ảnh thôi Cưng à!");
    return;
  }

  isUploading.value = true;
  const formData = new FormData();
  formData.append("file", file);

  try {
    const res = await axiosClient.post("/upload/image", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    // Server trả về: { "url": "/uploads/xxx.jpg" }
    // Lưu tạm vào form để preview
    form.profileImageUrl = res.data.url;
  } catch (e) {
    alert("Lỗi tải ảnh: " + (e.response?.data || e.message));
  } finally {
    isUploading.value = false;
  }
};

const saveProfile = async () => {
  if (!confirm("Cưng có chắc muốn lưu thay đổi thông tin không?")) return;
  try {
    await axiosClient.put("/user/update", {
      fullName: form.fullName,
      username: form.username,
      email: form.email,
      password: form.password || null,
      profileImageUrl: form.profileImageUrl
    });
    
    // Refresh lại thông tin user trong store
    await authStore.fetchProfile();
    alert("Lưu hồ sơ thành công!");
    form.password = ""; 
  } catch (e) {
    alert("Lỗi lưu hồ sơ: " + (e.response?.data?.message || e.message));
  }
};

// --- LOGIC SKIN (GIỮ NGUYÊN) ---
const skins = Object.values(CHARACTER_SKINS);
const currentSkinId = computed(() => authStore.user?.avatarUrl || "skin_yasou");
const selectedSkinId = ref("skin_yasou");
const previewState = ref("idle");
const newName = ref("");
let animInterval = null;
const animCycle = ["idle", "run", "attack"];
let animIndex = 0;

const previewImage = computed(() => {
  const skin = CHARACTER_SKINS[selectedSkinId.value];
  return skin ? skin.sprites[previewState.value] : "";
});

const selectSkin = (id) => {
  selectedSkinId.value = id;
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
    alert("Đổi tên nhân vật thành công!");
  } catch (e) {
    alert(e.response?.data || "Lỗi đổi tên");
  }
};

onMounted(() => {
  charStore.fetchCharacter();
  selectedSkinId.value = currentSkinId.value;
  
  // Fill data vào form khi load trang
  if (authStore.user) {
    form.fullName = authStore.user.fullName;
    form.username = authStore.user.username;
    form.email = authStore.user.email;
    form.profileImageUrl = authStore.user.profileImageUrl;
  }

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
  max-width: 1200px;
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

/* CONTENT LAYOUT */
.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.section-title {
  color: #fbc02d;
  border-bottom: 1px solid #5d4037;
  padding-bottom: 10px;
  margin-top: 0;
}

/* INFO COLUMN */
.info-column {
  background: rgba(0,0,0,0.3);
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #5d4037;
}

.avatar-edit-box {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #5d4037;
}

.current-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid #fbc02d;
  overflow: hidden;
  background: #000;
  flex-shrink: 0;
}

.current-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-controls {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.input-tabs {
    display: flex;
    gap: 5px;
}
.input-tabs button {
    background: #261815;
    color: #a1887f;
    border: 1px solid #5d4037;
    padding: 4px 10px;
    cursor: pointer;
    font-size: 0.8em;
}
.input-tabs button.active {
    background: #fbc02d;
    color: #000;
    font-weight: bold;
}

.file-group input {
    padding: 5px;
    font-size: 0.9em;
    color: #fff;
}
.uploading-text {
    color: #fbc02d;
    font-size: 0.8em;
    font-style: italic;
}

.info-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #d7ccc8;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 10px;
  background: #261815;
  border: 1px solid #5d4037;
  color: #fff;
  font-family: inherit;
  border-radius: 4px;
}

.form-group.read-only .static-val {
  padding: 10px;
  background: #3e2723;
  color: #fbc02d;
  font-weight: bold;
  border: 1px solid #5d4037;
  border-radius: 4px;
}

.hint {
    color: #a1887f;
    font-size: 0.8em;
    font-style: italic;
}

.btn-save {
  margin-top: 10px;
  background: #2e7d32;
  color: #fff;
  border: none;
  padding: 12px;
  font-weight: bold;
  cursor: pointer;
  border-radius: 4px;
  transition: 0.2s;
}
.btn-save:hover {
  background: #388e3c;
}

/* CHAR SELECT COLUMN */
.char-select-column {
  background: rgba(0,0,0,0.3);
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #5d4037;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.preview-stage {
  height: 200px;
  display: flex;
  flex-direction: column;
  gap: 10px;
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
  overflow: hidden;
}
.preview-actor { width: 100px; height: 100px; margin-top: 10px; }
.actor-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); image-rendering: pixelated; }

.control-bar button {
  width: 100%;
  padding: 10px;
  font-weight: bold;
  cursor: pointer;
  border: none;
  border-radius: 4px;
}
.btn-equip { background: #fbc02d; color: #000; }
.btn-equipped { background: #3e2723; color: #757575; cursor: default; border: 1px solid #5d4037; }

.skin-list {
  flex: 1;
  max-height: 300px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #5d4037;
  border-radius: 8px;
  overflow-y: auto;
  padding: 10px;
}

.skin-card {
  display: flex;
  gap: 10px;
  padding: 8px;
  background: #261815;
  border: 1px solid #3e2723;
  margin-bottom: 8px;
  cursor: pointer;
  align-items: center;
}
.skin-card.active { border-color: #fbc02d; background: #4e342e; }
.skin-icon { width: 40px; height: 40px; background: #000; border: 1px solid #3e2723; display: flex; align-items: center; justify-content: center; }
.skin-icon img { width: 100%; height: 100%; object-fit: contain; }
.skin-name { font-weight: bold; color: #fbc02d; font-size: 0.9em; }
.equipped-icon { color: #2e7d32; }

.rename-box { display: flex; gap: 5px; }
.rename-box input { flex: 1; background: #1a1a1a; border: 1px solid #5d4037; color: #fff; padding: 8px; }
.rename-box button { background: #3e2723; color: #fbc02d; border: 1px solid #5d4037; cursor: pointer; width: 40px; }

@media (max-width: 800px) {
  .profile-content { grid-template-columns: 1fr; }
}
</style>