<template>
  <transition name="modal-fade">
    <div v-if="isVisible" class="modal-overlay" @click.self="close">
      <div class="modal-content gift-modal">
        <div class="modal-header">
          <h2><i class="fas fa-gift"></i> Nhập Giftcode</h2>
          <button class="close-btn" @click="close">&times;</button>
        </div>

        <div class="modal-body">
          <p class="guide-text">Nhập mã quà tặng để nhận vật phẩm:</p>
          <div class="input-group">
            <input 
              v-model="code" 
              placeholder="Nhập mã tại đây..." 
              @keyup.enter="submitCode"
              :disabled="isLoading"
            />
            <button @click="submitCode" :disabled="isLoading || !code">
              <span v-if="isLoading"><i class="fas fa-spinner fa-spin"></i></span>
              <span v-else>Nhận</span>
            </button>
          </div>
          
          <div v-if="message" class="result-msg" :class="{ 'error': isError }">
            {{ message }}
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, defineExpose } from 'vue'; // Bỏ defineExpose ở import nếu Vue >= 3.3
import axiosClient from '@/api/axiosClient';
import { useNotificationStore } from '@/stores/notificationStore';
import { useInventoryStore } from '@/stores/inventoryStore';
import { useAuthStore } from '@/stores/authStore';

const isVisible = ref(false);
const code = ref("");
const isLoading = ref(false);
const message = ref("");
const isError = ref(false);

const notiStore = useNotificationStore();
const inventoryStore = useInventoryStore();
const authStore = useAuthStore();

const open = () => {
  isVisible.value = true;
  code.value = "";
  message.value = "";
  isError.value = false;
};

const close = () => {
  isVisible.value = false;
};

const submitCode = async () => {
  if (!code.value.trim()) return;
  
  isLoading.value = true;
  message.value = "";
  isError.value = false;

  try {
    const res = await axiosClient.post('/giftcode/redeem', { code: code.value });
    
    // Thành công
    message.value = res.data.message;
    notiStore.showToast(res.data.message, 'success');
    
    // Reload lại data để thấy tiền và đồ
    await authStore.fetchUserProfile();
    await inventoryStore.fetchInventory();
    
    // Tự đóng sau 2s nếu thành công
    setTimeout(() => {
        if(isVisible.value) close();
    }, 2000);

  } catch (error) {
    isError.value = true;
    if (error.response && error.response.data) {
        message.value = error.response.data.error || error.response.data;
    } else {
        message.value = "Lỗi kết nối server.";
    }
  } finally {
    isLoading.value = false;
  }
};

defineExpose({ open });
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.7);
  z-index: 9000;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(2px);
}
.gift-modal {
  background: #2b1d1a;
  border: 2px solid #5d4037;
  width: 400px;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(251, 192, 45, 0.2);
  overflow: hidden;
}
.modal-header {
  background: #3e2723;
  padding: 10px 15px;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid #5d4037;
  color: #fbc02d;
}
.modal-header h2 { margin: 0; font-size: 1.2em; font-family: 'Cinzel', serif; }
.close-btn { background: none; border: none; color: #ef5350; font-size: 1.5em; cursor: pointer; }

.modal-body { padding: 20px; text-align: center; color: #d7ccc8; }
.guide-text { margin-bottom: 15px; font-size: 0.9em; }

.input-group { display: flex; gap: 5px; }
.input-group input {
  flex: 1; padding: 10px; background: #1a1510; border: 1px solid #5d4037; color: #fff; border-radius: 4px; outline: none;
  font-family: monospace; font-size: 1.1em; text-transform: uppercase;
}
.input-group input:focus { border-color: #fbc02d; }
.input-group button {
  background: #fbc02d; border: none; padding: 0 20px; font-weight: bold; color: #3e2723; border-radius: 4px; cursor: pointer;
}
.input-group button:disabled { opacity: 0.6; cursor: not-allowed; }

.result-msg { margin-top: 15px; font-weight: bold; color: #66bb6a; }
.result-msg.error { color: #ef5350; }

.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.3s; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style>