import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import { useAuthStore } from "./authStore";

export const useChatStore = defineStore("chat", {
  state: () => ({
    messages: [],
    stompClient: null,
    isConnected: false,
    isLoading: false,
    error: null,
    // [ĐÃ XÓA] joinedUsers: new Set(), -> Không dùng cái này nữa
    isWidgetOpen: false,
    privateChatTarget: null,
  }),

  actions: {
    connect() {
      if (this.stompClient && this.stompClient.connected) return;

      const socket = new SockJS("http://localhost:8080/ws");
      this.stompClient = Stomp.over(socket);
      
      // Tắt log debug nếu muốn console gọn
      // this.stompClient.debug = () => {};

      const authStore = useAuthStore();
      const token = authStore.token || localStorage.getItem('token');
      const username = authStore.user?.username;

      // Header gửi Token (để fix lỗi Unauthorized bên Socket nếu có)
      const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

      this.stompClient.connect(
        headers,
        (frame) => {
          this.setConnected(true);
          console.log("WS Connected (" + username + ")");

          // 1. Kênh Chat Thế Giới
          this.stompClient.subscribe("/topic/public", (payload) => {
            this.addMessage(JSON.parse(payload.body));
          });

          // 2. Kênh Riêng (Nhận thông báo bạn bè)
          if (username) {
            this.stompClient.subscribe("/user/queue/notifications", (payload) => {
               const notification = JSON.parse(payload.body);
               this.addMessage(notification);
            });
          }

          // 3. Báo danh
          if (username) {
            this.stompClient.send(
              "/app/chat.addUser",
              {},
              JSON.stringify({ senderName: username, type: "JOIN" })
            );
          }
        },
        (error) => {
          console.error("Lỗi WS:", error);
          this.setConnected(false);
          setTimeout(() => this.connect(), 5000);
        }
      );
    },

    disconnect() {
      if (this.stompClient) {
        this.stompClient.disconnect();
      }
      this.setConnected(false);
    },

    sendMessage(content) {
        const authStore = useAuthStore();
        if (this.stompClient && this.isConnected && content.trim()) {
            const chatMessage = {
                senderName: authStore.user?.username,
                content: content,
                type: 'CHAT'
            };
            this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        }
    },

    async fetchMessages() {
      this.isLoading = true;
      this.error = null;
      try {
        const res = await axiosClient.get("/chat/recent");
        if (Array.isArray(res.data)) {
          this.messages = res.data;
        } else {
          this.messages = [];
        }
      } catch (error) {
        // Lỗi 401 thường rơi vào đây
        console.error("Lỗi tải chat:", error);
      } finally {
        this.isLoading = false;
      }
    },

    addMessage(message) {
      if (!message) return;

      // [QUAN TRỌNG] Đã XÓA đoạn kiểm tra message.type === 'JOIN'
      // Để tin nhắn thông báo luôn được hiển thị, không bị chặn

      if (!message.timestamp) message.timestamp = new Date().toISOString();

      // Chỉ lọc trùng tin nhắn chat thường (tránh mạng lag)
      // Tin hệ thống (SYSTEM) thì luôn cho qua
      if (message.role !== 'SYSTEM') {
          const exists = this.messages.some(
            (m) =>
              m.content === message.content &&
              m.senderName === message.senderName &&
              Math.abs(new Date(m.timestamp) - new Date(message.timestamp)) < 1000
          );
          if (exists) return;
      }

      this.messages.push(message);
      
      // Giới hạn 50 tin
      if (this.messages.length > 50) this.messages.shift();
    },

    setConnected(status) {
      this.isConnected = status;
    },

    openChatWith(user) {
      if (user) {
        this.privateChatTarget = {
            id: user.id || user.userId,
            username: user.username || user.senderName,
            avatarUrl: user.avatarUrl,
        };
        this.isWidgetOpen = true;
      }
    },
    openChat() { this.isWidgetOpen = true; },
    closeChat() { this.isWidgetOpen = false; this.privateChatTarget = null; },
  },
});