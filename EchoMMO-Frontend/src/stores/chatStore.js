import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useChatStore = defineStore("chat", {
  state: () => ({
    // Dữ liệu tin nhắn
    messages: [],
    isLoading: false,

    // Trạng thái hiển thị của Widget Chat (Mới thêm)
    isChatOpen: false,
  }),

  actions: {
    // --- PHẦN 1: GỌI API (Backend) ---
    async fetchMessages() {
      this.isLoading = true;
      try {
        // Gọi API lấy tin nhắn gần đây
        const res = await axiosClient.get("/chat/recent");
        this.messages = res.data;
      } catch (error) {
        console.error("Lỗi tải chat:", error);
      } finally {
        this.isLoading = false;
      }
    },

    async sendMessage(content) {
      if (!content.trim()) return;
      try {
        // Gọi API gửi tin nhắn
        await axiosClient.post("/chat/send", { content });
        // Gửi xong thì tải lại danh sách luôn
        await this.fetchMessages();
      } catch (error) {
        console.error("Gửi thất bại:", error);
        alert("Gửi thất bại: " + (error.response?.data || "Lỗi mạng"));
      }
    },

    // --- PHẦN 2: ĐIỀU KHIỂN GIAO DIỆN (Frontend) ---
    openChat() {
      this.isChatOpen = true;
      // Khi mở chat lên thì tiện tay tải tin nhắn luôn
      this.fetchMessages();
    },

    closeChat() {
      this.isChatOpen = false;
    },

    toggleChat() {
      this.isChatOpen = !this.isChatOpen;
      if (this.isChatOpen) {
        this.fetchMessages();
      }
    },
  },
});
