import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import { useAuthStore } from "./authStore";

export const useQuestStore = defineStore("quest", {
  state: () => ({
    quests: [],
    isLoading: false,
  }),
  actions: {
    async fetchQuests() {
      this.isLoading = true;
      try {
        const res = await axiosClient.get("/quests/daily");
        this.quests = res.data;
      } catch (e) {
        console.error(e);
      } finally {
        this.isLoading = false;
      }
    },
    async claim(id) {
      try {
        const res = await axiosClient.post(`/quests/claim/${id}`);
        alert(res.data);
        this.fetchQuests(); // Refresh trạng thái

        const authStore = useAuthStore();
        authStore.fetchProfile(); // Refresh tiền
      } catch (e) {
        alert(e.response?.data || "Lỗi");
      }
    },
  },
});
