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
public class ChatRead {
    private String id;
    private String messageId;
    private String readerId;
    private LocalDateTime readAt;
}