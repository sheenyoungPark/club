package com.spacedong.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_message_reads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "message_reader", def = "{'messageId': 1, 'readerId': 1}", unique = true)
public class ChatReadDocument {
    @Id
    private String id;

    @Indexed
    private String messageId;

    @Indexed
    private String readerId;

    private LocalDateTime readAt;
}