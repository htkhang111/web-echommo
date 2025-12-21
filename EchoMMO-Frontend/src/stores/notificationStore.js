import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useNotificationStore = defineStore("notification", {
  state: () => ({
    notifications: [], // [MỚI] Danh sách thông báo
    unreadCount: 0,
    isLoading: false, // [MỚI] Trạng thái tải

    // State quản lý Toast (Popup)
    toast: {
      visible: false,
      message: "",
      type: "info", // success, error, warning, info
    },
    timeoutId: null,
  }),

  actions: {
    // 1. Lấy danh sách thông báo (Hàm bị thiếu gây lỗi)
    async fetchNotifications() {
      this.isLoading = true;
      try {
        const res = await axiosClient.get("/notifications");
        this.notifications = res.data;

        // Cập nhật lại số lượng chưa đọc luôn
        this.unreadCount = this.notifications.filter((n) => !n.isRead).length;
      } catch (e) {
        console.error("Lỗi tải thông báo:", e);
      } finally {
        this.isLoading = false;
      }
    },

    // 2. Đánh dấu đã đọc 1 cái
    async markRead(id) {
      try {
        await axiosClient.post(`/notifications/read/${id}`);

        // Update local state
        const noti = this.notifications.find((n) => n.id === id);
        if (noti && !noti.isRead) {
          noti.isRead = true;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        }
      } catch (e) {
        console.error("Lỗi mark read:", e);
      }
    },

    // 3. Đánh dấu đã đọc tất cả
    async markAllRead() {
      try {
        await axiosClient.post("/notifications/read-all");

        // Update local state
        this.notifications.forEach((n) => (n.isRead = true));
        this.unreadCount = 0;
        this.showToast("Đã đánh dấu đọc tất cả!", "success");
      } catch (e) {
        console.error("Lỗi mark all read:", e);
      }
    },

    // 4. Lấy số thông báo chưa đọc (Dùng cho Header)
    async fetchUnreadCount() {
      try {
        const res = await axiosClient.get("/notifications/unread-count");
        this.unreadCount = res.data;
      } catch (e) {
        console.error(e);
      }
    },

    // 5. Hiển thị Popup (Toast)
    showToast(message, type = "info") {
      if (this.timeoutId) clearTimeout(this.timeoutId);

      this.toast.message = message;
      this.toast.type = type;
      this.toast.visible = true;

      this.timeoutId = setTimeout(() => {
        this.toast.visible = false;
      }, 3000);
    },

    // 6. Ẩn Popup ngay lập tức
    hideToast() {
      this.toast.visible = false;
      if (this.timeoutId) clearTimeout(this.timeoutId);
    },
  },
});
