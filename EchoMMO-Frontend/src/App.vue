<template>
  <div id="app-root">
    <GameToast />
    
    <router-view v-if="isAuthPage" />

    <MainLayout v-else>
      <router-view />
    </MainLayout>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from './stores/authStore';
import GameToast from './components/GameToast.vue'; 
import MainLayout from './layouts/MainLayout.vue'; // [QUAN TRỌNG] Import Layout chính

const authStore = useAuthStore();
const route = useRoute();

// Kiểm tra xem trang hiện tại có phải là trang Auth (Login/Register) không
const isAuthPage = computed(() => {
  return ['Login', 'Register', 'ForgotPassword'].includes(route.name);
});

onMounted(async () => {
  await authStore.initialize();
});
</script>

<style>
/* CSS Reset cơ bản */
body { margin: 0; padding: 0; background-color: #000; color: #fff; font-family: "Noto Serif TC", serif; }
#app-root { min-height: 100vh; position: relative; }

/* Scrollbar đẹp */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: #1a1a1a; }
::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #8d6e63; }
</style>