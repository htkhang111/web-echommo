import { defineStore } from 'pinia';
import axiosClient from '../api/axiosClient';

export const useChatStore = defineStore('chat', {
  state: () => ({
    messages: [], 
    isConnected: false,
    isLoading: false,
    
    // State quản lý Chat Widget (Chat riêng)
    isWidgetOpen: false,
    privateChatTarget: null 
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
    
    // [FIX] Hàm này đã được sửa logic
    openChatWith(user) {
      // B1: Gán thông tin người cần chat trước
      if (user) {
        this.privateChatTarget = {
          id: user.id || user.userId,
          username: user.username || user.senderName,
          avatarUrl: user.avatarUrl
        };
      }
      
      // B2: Mới mở Widget lên -> Lúc này Watcher bên ChatWidget mới chạy và thấy có privateChatTarget
      this.isWidgetOpen = true;
    },

    openChat() {
      this.isWidgetOpen = true;
    },

    closeChat() {
      this.isWidgetOpen = false;
      this.privateChatTarget = null;
    }
  }
});