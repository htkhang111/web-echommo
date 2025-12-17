import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useNotificationStore = defineStore("notification", {
  state: () => ({
    unreadCount: 0,
    // State quản lý Toast (Popup)
    toast: {
      visible: false,
      message: "",
      type: "info", // success, error, warning, info
    },
    timeoutId: null,
  }),
  actions: {
    // 1. Lấy số thông báo chưa đọc
    async fetchUnreadCount() {
      try {
        const res = await axiosClient.get("/notifications/unread-count");
        this.unreadCount = res.data;
      } catch (e) {
        console.error(e);
      }
    },

    // 2. Hiển thị Popup (Toast)
    showToast(message, type = "info") {
      // Xóa timeout cũ nếu đang chạy để tránh xung đột
      if (this.timeoutId) clearTimeout(this.timeoutId);

      this.toast.message = message;
      this.toast.type = type;
      this.toast.visible = true;

      // Tự động tắt sau 3 giây
      this.timeoutId = setTimeout(() => {
        this.toast.visible = false;
      }, 3000);
    },

    // 3. Ẩn Popup ngay lập tức (khi bấm nút X)
    hideToast() {
      this.toast.visible = false;
      if (this.timeoutId) clearTimeout(this.timeoutId);
    }
  },
});