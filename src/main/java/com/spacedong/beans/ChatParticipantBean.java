package com.spacedong.beans;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatParticipantBean {
    private int room_id;
    private String user_id;
    private String user_type;  // "MEMBER", "BUSINESS", or "ADMIN"
    private LocalDateTime joinDate;
    private Long lastReadMsgId;

    // 추가 정보
    private String userNickname;  // 회원 닉네임 또는 이름
    private String userProfile;   // 프로필 이미지
}