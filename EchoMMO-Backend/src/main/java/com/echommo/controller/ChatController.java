package com.echommo.controller;

import com.echommo.dto.ChatMessageDTO;
import com.echommo.entity.Message;
import com.echommo.entity.User;
import com.echommo.repository.MessageRepository;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    // --- WebSocket Handler ---
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

    // --- [REST API] Lấy lịch sử chat (SAFE MODE) ---
    @GetMapping("/api/chat/recent")
    public ResponseEntity<List<ChatMessageDTO>> getRecentMessages() {
        try {
            // 1. Lấy 50 tin nhắn mới nhất
            List<Message> messages = messageRepository.findTop50ByOrderBySentAtDesc();

            // Xử lý null list (phòng hờ)
            if (messages == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // 2. Đảo ngược để hiện từ cũ đến mới (Chronological)
            Collections.reverse(messages);

            // 3. Convert Entity -> DTO với chế độ AN TOÀN TUYỆT ĐỐI
            List<ChatMessageDTO> dtos = messages.stream().map(msg -> {
                        try {
                            String role = "USER";
                            String avatar = ""; // Mặc định rỗng hoặc link ảnh default
                            String senderName = "Ẩn danh";
                            Integer senderId = 0;
                            String timeStr = LocalDateTime.now().toString();

                            // Check Null Sender
                            if (msg.getSender() != null) {
                                senderName = msg.getSender().getUsername() != null ? msg.getSender().getUsername() : "Unknown";
                                senderId = msg.getSender().getUserId();
                                avatar = msg.getSender().getAvatarUrl();

                                // Check Null Role
                                if (msg.getSender().getRole() != null) {
                                    role = msg.getSender().getRole().name();
                                }
                            }

                            // Check Null Time
                            if (msg.getSentAt() != null) {
                                timeStr = msg.getSentAt().toString();
                            }

                            return new ChatMessageDTO(
                                    senderId,
                                    senderName,
                                    avatar,
                                    msg.getContent() != null ? msg.getContent() : "",
                                    timeStr,
                                    role
                            );
                        } catch (Exception e) {
                            // Nếu có 1 dòng lỗi cực dị, skip dòng đó, log lỗi nhưng KHÔNG làm sập API
                            System.err.println("Lỗi convert message ID: " + msg.getMessageId());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(item -> item != null) // Lọc bỏ những dòng lỗi
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về mảng rỗng thay vì lỗi 500 để Frontend không bị đỏ lòm
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // --- [REST API] Gửi tin nhắn từ Frontend ---
    @PostMapping("/api/chat/send")
    public void sendRestMessage(@RequestBody ChatMessageDTO chatMessage) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                chatMessage.setSenderName(user.getUsername());
                chatMessage.setSenderId(user.getUserId());
                chatMessage.setAvatarUrl(user.getAvatarUrl());

                String role = "USER";
                if (user.getRole() != null) {
                    role = user.getRole().name();
                }
                chatMessage.setRole(role);

                // Lưu vào Database (Persistence)
                try {
                    Message msgEntity = new Message();
                    msgEntity.setSender(user);
                    msgEntity.setContent(chatMessage.getContent());
                    msgEntity.setSentAt(LocalDateTime.now());
                    messageRepository.save(msgEntity);
                } catch (Exception e) {
                    System.err.println("Lỗi lưu tin nhắn: " + e.getMessage());
                }
            }
        }

        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now().toString());
        }

        // Bắn Socket
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
}