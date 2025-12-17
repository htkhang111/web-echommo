<template>
  <div class="page-container gathering-page dark-theme">
      <div class="gathering-panel" v-if="currentEvent">
           <div class="event-header">
               <div class="node-circle" :class="{ 'shake-anim': isGathering }">
                   <img :src="eventImage" class="node-img" />
               </div>
               <h2 class="node-name">{{ currentEvent.name }}</h2>
               </div>
           
           <div class="info-scroll-area">
                <div class="status-row">
                    <span class="val gold-text">{{ remainingNode }} / {{ maxNode }}</span>
                </div>
           </div>
           <div class="btn-grid">
               <button class="btn-wood action-btn" @click="handleGather(1)" :disabled="!canGather">
                   <span class="btn-main">KHAI THÁC</span>
                   <span class="btn-sub">Tốn 1 <i class="fas fa-bolt"></i></span>
               </button>
               </div>
           <div class="feedback-text" v-if="feedbackMsg">{{ feedbackMsg }}</div>
      </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useAuthStore } from "@/stores/authStore";
import { useRouter } from "vue-router";
import axiosClient from "@/api/axiosClient";

// Import ảnh để map thủ công (hoặc backend trả url thì tốt hơn)
import copperNodeImg from "@/assets/resources/r_copper_node.png";
import woodNodeImg from "@/assets/resources/r_wood.png";
import stoneNodeImg from "@/assets/resources/stone_1.png";

const charStore = useCharacterStore();
const authStore = useAuthStore();
const router = useRouter();

const currentEvent = ref(null);
const remainingNode = ref(0);
const maxNode = ref(0);
const isGathering = ref(false);
const feedbackMsg = ref("");
const floatingLoots = ref([]);

const playerLevel = computed(() => charStore.character?.level || 1);
const currentEnergy = computed(() => charStore.character?.currentEnergy || 0);

// Helper lấy ảnh theo ID
const eventImage = computed(() => {
    if (!currentEvent.value) return "";
    // Mapping ID -> Ảnh (Tạm thời map tay, sau này nên lưu trong DB)
    const id = currentEvent.value.itemId;
    if (id === 1) return woodNodeImg; // Gỗ
    if (id === 2) return stoneNodeImg; // Đá
    if (id === 3) return copperNodeImg; // Đồng
    return woodNodeImg; // Mặc định
});

const progressPercent = computed(() => {
  if (maxNode.value === 0) return 0;
  return Math.min(Math.round((1 - remainingNode.value / maxNode.value) * 100), 100);
});

const canGather = computed(() => !isGathering.value && remainingNode.value > 0 && currentEnergy.value >= 1);

const goBack = () => router.push("/explore");

const initEvent = () => {
  // [FIX] Lấy dữ liệu từ Pinia Store (Do Explore API trả về)
  const state = charStore.gatheringState;
  
  if (!state || !state.itemId) {
      alert("Không có dữ liệu tài nguyên!");
      router.push("/explore");
      return;
  }

  currentEvent.value = {
      itemId: state.itemId,
      name: state.name || "Tài nguyên bí ẩn",
      reqLevel: 1, // Tạm thời hardcode, sau này BE trả về thì lấy
      reqTool: "Tay không"
  };
  
  maxNode.value = state.amount;
  remainingNode.value = state.amount;
  feedbackMsg.value = "Sẵn sàng khai thác!";
};

const handleGather = async (times) => {
  if (isGathering.value) return;
  if (currentEnergy.value < times) {
      feedbackMsg.value = "⚠️ Không đủ nội năng!";
      return;
  }

  isGathering.value = true;
  feedbackMsg.value = "Đang khai thác...";
  
  const actualGathered = Math.min(times, remainingNode.value);

  try {
    // [FIX] Gửi itemId thay vì type string
    const res = await axiosClient.post("/exploration/gather", {
      itemId: currentEvent.value.itemId, // Gửi ID chuẩn
      amount: actualGathered,
    });

    // Update lại số lượng từ Server trả về cho chắc chắn
    if (res.data.remainingAmount !== undefined) {
        remainingNode.value = res.data.remainingAmount;
    } else {
        remainingNode.value -= actualGathered;
    }

    if (charStore.character) {
      charStore.character.currentEnergy = res.data.currentEnergy;
    }

    if (actualGathered > 0) {
       showFloatingText(`+${actualGathered} ${currentEvent.value.name}`);
       feedbackMsg.value = `Thu hoạch thành công!`;
    }

    if (remainingNode.value <= 0) {
       feedbackMsg.value = "Mỏ đã cạn kiệt!";
       setTimeout(() => router.push("/explore"), 1500);
    }
  } catch (error) {
    console.error(error);
    feedbackMsg.value = error.response?.data?.message || "Lỗi khai thác!";
    // Nếu lỗi do hết hạn/gian lận -> đá về trang explore
    if (error.response?.status === 500 || error.response?.data?.message?.includes("Hết hạn")) {
        setTimeout(() => router.push("/explore"), 1500);
    }
  } finally {
    isGathering.value = false;
  }
};

const handleGatherAll = () => {
    const times = Math.min(10, remainingNode.value, currentEnergy.value);
    if (times > 0) handleGather(times);
    else feedbackMsg.value = "⚠️ Không đủ nội năng!";
};

const showFloatingText = (text) => {
  const id = Date.now();
  floatingLoots.value.push({ id, amount: text.split(" ")[0], name: text.substring(text.indexOf(" ")) });
  setTimeout(() => floatingLoots.value = floatingLoots.value.filter(i => i.id !== id), 1500);
};

onMounted(() => {
  charStore.fetchCharacter();
  authStore.fetchProfile();
  initEvent();
});
</script>