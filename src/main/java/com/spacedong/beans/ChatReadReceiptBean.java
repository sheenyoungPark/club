package com.spacedong.beans;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatReadReceiptBean {
    private Long messageId;
    private String readerId;
    private LocalDateTime readTime;
}