package com.echommo.controller;

import com.echommo.dto.ChatMessageDTO;
import com.echommo.entity.User;
import com.echommo.service.PrivateMessageService;
import com.echommo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @Autowired
    private UserService userService;

    // Lấy lịch sử chat
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer otherUserId) { // [FIX] Long -> Integer

        User currentUser = userService.findByUsername(userDetails.getUsername());
        // [FIX] getId() -> getUserId()
        List<ChatMessageDTO> history = privateMessageService.getChatHistory(currentUser.getUserId(), otherUserId);
        return ResponseEntity.ok(history);
    }

    // Gửi tin nhắn
    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> payload) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        // [FIX] Parse Integer an toàn
        Integer receiverId = Integer.valueOf(payload.get("receiverId").toString());
        String content = (String) payload.get("content");

        // [FIX] getId() -> getUserId()
        ChatMessageDTO message = privateMessageService.sendPrivateMessage(currentUser.getUserId(), receiverId, content);
        return ResponseEntity.ok(message);
    }

    // Lấy danh sách hội thoại
    @GetMapping("/conversations")
    public ResponseEntity<List<Map<String, Object>>> getConversations(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // [FIX] getId() -> getUserId()
        List<Map<String, Object>> conversations = privateMessageService.getRecentConversations(currentUser.getUserId());
        return ResponseEntity.ok(conversations);
    }

    // Đếm tin chưa đọc
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // [FIX] getId() -> getUserId()
        long count = privateMessageService.countTotalUnread(currentUser.getUserId());
        return ResponseEntity.ok(count);
    }

    // Đánh dấu đã đọc
    @PostMapping("/read/{senderId}")
    public ResponseEntity<?> markAsRead(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer senderId) { // [FIX] Long -> Integer
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // [FIX] getId() -> getUserId()
        privateMessageService.markAsRead(senderId, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }
}