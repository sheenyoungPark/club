package com.spacedong.beans;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatMessageBean {
    private Long messageId;
    private Long roomId;
    private String senderId;
    private String senderType;  // "MEMBER", "BUSINESS", or "ADMIN"
    private String messageContent;
    private String messageType;  // "TEXT", "IMAGE", or "FILE"
    private String filePath;
    private LocalDateTime sendTime;
    private int readCount;

    // 추가 정보
    private String senderNickname;
    private String senderProfile;
    private boolean isRead;  // 현재 사용자가 이 메시지를 읽었는지
}