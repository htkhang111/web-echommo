import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useFriendStore = defineStore("friend", {
  state: () => ({
    friends: [],
    requests: [],
    isLoading: false,
  }),
  actions: {
    async fetchAll() {
      this.isLoading = true;
      try {
        const [resFriends, resReq] = await Promise.all([
          axiosClient.get("/friends/list"),
          axiosClient.get("/friends/requests"),
        ]);
        this.friends = resFriends.data;
        this.requests = resReq.data;
      } catch (e) {
        console.error(e);
      } finally {
        this.isLoading = false;
      }
    },
    async sendRequest(username) {
      try {
        const res = await axiosClient.post("/friends/add", { username });
        alert(res.data);
      } catch (e) {
        alert(e.response?.data || "Lỗi");
      }
    },
    async accept(id) {
      await axiosClient.post(`/friends/accept/${id}`);
      this.fetchAll();
    },
    async remove(id) {
      if (!confirm("Bạn chắc chứ?")) return;
      await axiosClient.delete(`/friends/remove/${id}`);
      this.fetchAll();
    },
  },
});
