// import { defineStore } from 'pinia';
// import axiosClient from '../api/axiosClient';

// export const useChatStore = defineStore('chat', {
//   state: () => ({
//     messages: [], 
//     isConnected: false,
//     isLoading: false,
//     error: null, // Thêm state lỗi
    
//     // State quản lý Chat Widget (Chat riêng)
//     isWidgetOpen: false,
//     privateChatTarget: null 
//   }),
  
//   actions: {
//     // --- CHAT THẾ GIỚI ---
//     async fetchMessages() {
//       this.isLoading = true;
//       this.error = null;
//       try {
//         const res = await axiosClient.get('/chat/recent');
//         // Phòng thủ: Nếu res.data null thì gán mảng rỗng
//         if (Array.isArray(res.data)) {
//           this.messages = res.data;
//         } else {
//           console.warn("API Chat trả về dữ liệu không phải mảng:", res.data);
//           this.messages = [];
//         }
//       } catch (error) {
//         console.error("🔥 Lỗi tải chat (Backend 500?):", error);
//         this.error = "Không thể kết nối kênh chat.";
//       } finally {
//         this.isLoading = false;
//       }
//     },

//     addMessage(message) {
//       if (!message) return;
      
//       // Tránh trùng lặp tin nhắn
//       const exists = this.messages.some(m => 
//         m.timestamp === message.timestamp && 
//         m.senderName === message.senderName && 
//         m.content === message.content
//       );
//       if (!exists) {
//         this.messages.push(message);
//         if (this.messages.length > 50) this.messages.shift();
//       }
//     },

//     setConnected(status) {
//       this.isConnected = status;
//     },

//     // --- CHAT RIÊNG (WIDGET) ---
//     openChatWith(user) {
//       if (user) {
//         // Tự động tìm ID đúng (id, userId, hoặc characterId)
//         const targetId = user.id || user.userId || user.characterId;
//         const targetName = user.username || user.senderName || user.name;
        
//         if (targetId) {
//             this.privateChatTarget = {
//               id: targetId,
//               username: targetName || "Người lạ",
//               avatarUrl: user.avatarUrl
//             };
//             this.isWidgetOpen = true;
//         } else {
//             console.error("Không tìm thấy ID người dùng để chat:", user);
//         }
//       }
//     },

//     openChat() {
//       this.isWidgetOpen = true;
//     },

//     closeChat() {
//       this.isWidgetOpen = false;
//       this.privateChatTarget = null;
//     }
//   }
// });
import { defineStore } from 'pinia';
import axiosClient from '../api/axiosClient';

export const useChatStore = defineStore('chat', {
  state: () => ({
    messages: [], 
    isConnected: false,
    isLoading: false,
    error: null,
    
    // State quản lý Chat Widget (Chat riêng)
    isWidgetOpen: false,
    privateChatTarget: null 
  }),
  
  actions: {
    // --- CHAT THẾ GIỚI ---
    async fetchMessages() {
      this.isLoading = true;
      this.error = null;
      try {
        const res = await axiosClient.get('/chat/recent');
        if (Array.isArray(res.data)) {
          this.messages = res.data;
        } else {
          console.warn("API Chat trả về dữ liệu không phải mảng:", res.data);
          this.messages = [];
        }
      } catch (error) {
        console.error("🔥 Lỗi tải chat:", error);
        this.error = "Không thể kết nối kênh chat.";
      } finally {
        this.isLoading = false;
      }
    },

    addMessage(message) {
      if (!message) return;
      
      // Phòng thủ nếu không có timestamp
      if (!message.timestamp) {
        message.timestamp = new Date().toISOString();
      }

      // Tránh trùng lặp (Check content + sender + time xấp xỉ)
      const exists = this.messages.some(m => 
        m.content === message.content && 
        m.senderName === message.senderName &&
        // Check trùng thời gian trong khoảng 1s (đề phòng delay)
        Math.abs(new Date(m.timestamp) - new Date(message.timestamp)) < 1000
      );

      if (!exists) {
        this.messages.push(message);
        // Giữ lại 50 tin mới nhất để nhẹ store
        if (this.messages.length > 50) this.messages.shift();
      }
    },

    setConnected(status) {
      this.isConnected = status;
    },

    // --- CHAT RIÊNG (WIDGET) ---
    openChatWith(user) {
      if (user) {
        const targetId = user.id || user.userId || user.characterId;
        const targetName = user.username || user.senderName || user.name;
        
        if (targetId) {
            this.privateChatTarget = {
              id: targetId,
              username: targetName || "Người lạ",
              avatarUrl: user.avatarUrl
            };
            this.isWidgetOpen = true;
        } else {
            console.error("Không tìm thấy ID người dùng để chat:", user);
        }
      }
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