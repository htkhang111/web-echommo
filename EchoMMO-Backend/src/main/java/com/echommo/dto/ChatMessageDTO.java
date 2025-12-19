package com.echommo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private Integer senderId;   // [MỚI] ID người gửi để biết là tin nhắn của ai
    private String senderName;
    private String avatarUrl;   // [MỚI] Avatar người gửi
    private String content;
    private String timestamp;   // [ĐỔI TÊN] time -> timestamp cho chuẩn Frontend
    private String role;        // ADMIN / USER (để tô màu tên)
}