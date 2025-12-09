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
      try {
        await axiosClient.post(`/friends/accept/${id}`);
        await this.fetchAll();
      } catch (error) {
        console.error("Lỗi chấp nhận lời mời:", error);
        alert(error.response?.data || "Lỗi chấp nhận lời mời");
      }
    },
    async remove(id) {
      if (!confirm("Bạn chắc chứ?")) return;
      try {
        await axiosClient.delete(`/friends/remove/${id}`);
        await this.fetchAll();
      } catch (error) {
        console.error("Lỗi xóa bạn bè:", error);
        alert(error.response?.data || "Lỗi xóa bạn bè");
      }
    },
  },
});
