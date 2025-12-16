<template>
  <div class="create-char-container">
    <h2>Khởi Tạo Nhân Vật</h2>
    <p>Chào mừng nhà thám hiểm mới! Hãy đặt tên cho danh tính của bạn.</p>

    <form @submit.prevent="handleSubmit">
      <input v-model="charName" placeholder="Tên nhân vật..." required />
      <button type="submit">Bắt Đầu Hành Trình</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useCharacterStore } from "../stores/characterStore";
import { useRouter } from "vue-router";

const charName = ref("");
const charStore = useCharacterStore();
const router = useRouter();

const handleSubmit = async () => {
  const success = await charStore.createCharacter(charName.value);
  if (success) {
    router.push("/game"); // Chuyển vào trang game chính
  }
};
</script>

<style scoped>
.create-char-container {
  text-align: center;
  margin-top: 50px;
}
input {
  padding: 10px;
  font-size: 16px;
  margin-right: 10px;
}
button {
  padding: 10px 20px;
  background: #42b983;
  color: white;
  border: none;
  cursor: pointer;
}
</style>
