package com.spacedong.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String chatRoomId;
    private String senderId;
    private String senderName;
    private String senderType; // "MEMBER", "BUSINESS", "ADMIN"
    private String message;
    private String messageType; // "TEXT", "IMAGE", "FILE"
    private String attachmentUrl; // 첨부 파일 URL (이미지, 파일 등)
    private LocalDateTime timestamp;
    private boolean read;
}
