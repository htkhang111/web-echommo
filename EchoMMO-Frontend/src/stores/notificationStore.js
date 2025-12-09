import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useNotificationStore = defineStore("notification", {
  state: () => ({
    notifications: [],
    unreadCount: 0,
    // Global Toast State
    toast: {
      visible: false,
      message: "",
      type: "success",
    },
  }),
  actions: {
    async fetchNotifications() {
      try {
        const res = await axiosClient.get("/notifications");
        this.notifications = res.data;
        this.unreadCount = this.notifications.filter((n) => !n.isRead).length;
      } catch (e) {
        console.error("Lỗi lấy thông báo:", e);
      }
    },

    async fetchUnreadCount() {
      await this.fetchNotifications();
      return this.unreadCount;
    },

    async markRead(id) {
      try {
        // Optimistic update: Đánh dấu đã đọc ở client trước cho nhanh
        const noti = this.notifications.find((n) => n.id === id);
        if (noti && !noti.isRead) {
          noti.isRead = true;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        }
        await axiosClient.post(`/notifications/${id}/read`);
      } catch (e) {
        console.error(e);
      }
    },

    // [FIX] Thêm hàm này
    async markAllRead() {
      try {
        // Giả sử API backend hỗ trợ endpoint này, nếu chưa có thì có thể loop gọi markRead
        // Nhưng tốt nhất nên bảo backend viết 1 API: POST /notifications/read-all
        // Ở đây tạm thời ta loop hoặc gọi 1 API giả định
        await axiosClient.post("/notifications/read-all");

        // Update state
        this.notifications.forEach((n) => (n.isRead = true));
        this.unreadCount = 0;
        this.showToast("Đã đánh dấu đọc tất cả!", "success");
      } catch (e) {
        // Fallback nếu API không tồn tại: chỉ clear ở frontend cho sướng mắt
        this.notifications.forEach((n) => (n.isRead = true));
        this.unreadCount = 0;
        this.showToast("Đã đọc hết mật thư (Client-side)", "info");
      }
    },

    showToast(message, type = "success") {
      this.toast.message = message;
      this.toast.type = type;
      this.toast.visible = true;
      setTimeout(() => {
        this.toast.visible = false;
      }, 3000);
    },

    clearToast() {
      this.toast.visible = false;
    },
  },
});
