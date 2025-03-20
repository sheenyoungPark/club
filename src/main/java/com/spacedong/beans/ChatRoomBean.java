package com.spacedong.beans;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatRoomBean {
    private int room_id;
    private String room_name;
    private String room_type;  // "PERSONAL" or "CLUB"
    private Integer club_id;      // Null for personal chats
    private LocalDateTime createDate;

    // 채팅방 목록 표시용 추가 필드
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
}