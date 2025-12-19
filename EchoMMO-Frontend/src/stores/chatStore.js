import { defineStore } from 'pinia';
import axiosClient from '../api/axiosClient';

export const useChatStore = defineStore('chat', {
  state: () => ({
    messages: [], // Chat thế giới
    isConnected: false,
    isLoading: false,
    
    // [MỚI] State quản lý Chat Widget (Chat riêng)
    isWidgetOpen: false,
    privateChatTarget: null // User đang chat cùng (Object: {id, username, avatar...})
  }),
  
  actions: {
    // --- CHAT THẾ GIỚI ---
    async fetchMessages() {
      this.isLoading = true;
      try {
        const res = await axiosClient.get('/chat/recent');
        if (res.data) {
          this.messages = res.data;
        }
      } catch (error) {
        console.warn("Lỗi tải chat:", error);
      } finally {
        this.isLoading = false;
      }
    },

    addMessage(message) {
      const exists = this.messages.some(m => 
        m.timestamp === message.timestamp && 
        m.senderName === message.senderName && 
        m.content === message.content
      );
      if (!exists) {
        this.messages.push(message);
        if (this.messages.length > 50) this.messages.shift();
      }
    },

    setConnected(status) {
      this.isConnected = status;
    },

    // --- CHAT RIÊNG (WIDGET) ---
    
    // Mở chat widget (nếu có user thì chọn luôn user đó)
    openChatWith(user) {
      this.isWidgetOpen = true;
      if (user) {
        // Map field cho chuẩn format
        this.privateChatTarget = {
          id: user.id || user.userId,
          username: user.username || user.senderName,
          avatarUrl: user.avatarUrl
        };
      }
    },

    // Mở widget nhưng không chọn ai cụ thể (mở danh sách)
    openChat() {
      this.isWidgetOpen = true;
    },

    closeChat() {
      this.isWidgetOpen = false;
      this.privateChatTarget = null;
    }
  }
});