package com.spacedong.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chat_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDocument {
    @Id
    private String id;

    private String roomName;

    @Indexed
    private String roomType; // PERSONAL, CLUB

    @Indexed
    private List<String> participants; // 참여자 ID 목록

    private LocalDateTime createdAt;
    private LocalDateTime lastMessageTime;
    private String lastMessage;

    @Indexed
    private Integer clubId; // 클럽 채팅방일 경우 클럽 ID (null이면 개인 채팅방)
}