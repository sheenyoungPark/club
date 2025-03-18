package com.spacedong.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDocument {
    @Id
    private String id;

    @Indexed
    private String chatRoomId;

    @Indexed
    private String senderId;

    private String senderName;
    private String senderType; // MEMBER, BUSINESS, ADMIN
    private String message;
    private String messageType; // TEXT, IMAGE, FILE
    private String attachmentUrl; // 첨부 파일 URL

    @Indexed
    private LocalDateTime timestamp;

    private boolean read;
}