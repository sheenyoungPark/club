package com.spacedong.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    private String id;
    private String roomName;
    private String roomType; // "PERSONAL", "CLUB"
    private List<String> participants; // 참여자 ID 목록
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageTime;
    private String lastMessage;
    private Integer clubId; // 클럽 채팅방일 경우 클럽 ID (null이면 개인 채팅방)
    private int unreadCount; // 안 읽은 메시지 수
}