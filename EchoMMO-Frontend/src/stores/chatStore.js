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
    joinedUsers: new Set(),
    isWidgetOpen: false,
    privateChatTarget: null,
  }),

  actions: {
    // --- CONNECT WEBSOCKET (Đã sửa để gửi Token) ---
    connect() {
      if (this.stompClient && this.stompClient.connected) return;

      const socket = new SockJS("http://localhost:8080/ws");
      this.stompClient = Stomp.over(socket);
      
      // Tắt debug log (nếu muốn)
      // this.stompClient.debug = () => {};

      const authStore = useAuthStore();
      const token = authStore.token || localStorage.getItem('token');
      const username = authStore.user?.username;

      // [QUAN TRỌNG] Tạo header chứa Token
      const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

      this.stompClient.connect(
        headers, // Gửi header vào đây
        (frame) => {
          this.setConnected(true);
          console.log("WS Connected (" + username + ")");

          // 1. Subscribe Chat Thế Giới
          this.stompClient.subscribe("/topic/public", (payload) => {
            this.addMessage(JSON.parse(payload.body));
          });

          // 2. Subscribe Chat Riêng / Thông báo Online
          if (username) {
            // Spring Boot sẽ tự map /user/queue/... tới session của user này
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
          // Auto reconnect
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
        console.error("Lỗi tải chat:", error);
      } finally {
        this.isLoading = false;
      }
    },

    addMessage(message) {
      if (!message) return;
      if (message.type === 'JOIN') {
        if (this.joinedUsers.has(message.senderName)) return; 
        this.joinedUsers.add(message.senderName);
      }
      if (!message.timestamp) message.timestamp = new Date().toISOString();

      // Check trùng
      const exists = this.messages.some(
        (m) =>
          m.content === message.content &&
          m.senderName === message.senderName &&
          Math.abs(new Date(m.timestamp) - new Date(message.timestamp)) < 1000
      );

      if (!exists) {
        this.messages.push(message);
        if (this.messages.length > 50) this.messages.shift();
      }
    },

    setConnected(status) {
      this.isConnected = status;
    },

    // --- Widget Logic (Giữ nguyên) ---
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