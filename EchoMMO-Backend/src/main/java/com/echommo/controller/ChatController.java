package com.echommo.controller;

import com.echommo.dto.ChatMessageDTO;
import com.echommo.entity.User;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    // --- WebSocket Handler (Giữ nguyên) ---
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO chatMessage) {
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now().toString());
        }
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageDTO addUser(@Payload ChatMessageDTO chatMessage,
                                  SimpMessageHeaderAccessor headerAccessor) {
        if (chatMessage.getSenderName() != null) {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderName());
        }
        return chatMessage;
    }

    // --- [REST API] Gửi tin nhắn từ Frontend (Adventure) ---
    @PostMapping("/api/chat/send")
    public void sendRestMessage(@RequestBody ChatMessageDTO chatMessage) {
        // Tự động lấy User từ Security Context (Token)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                // Gán thông tin chuẩn từ Database để tránh fake
                chatMessage.setSenderName(user.getUsername());

                // [FIX] Sửa user.getId() -> user.getUserId()
                chatMessage.setSenderId(user.getUserId());

                // [FIX] Lấy Avatar thật của user luôn (User.java có field avatarUrl)
                chatMessage.setAvatarUrl(user.getAvatarUrl());
            }
        }

        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now().toString());
        }

        // Đẩy tin nhắn vào WebSocket topic để tất cả client (kể cả người gửi) đều thấy
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
}