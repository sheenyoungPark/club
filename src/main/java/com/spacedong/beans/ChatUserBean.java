package com.spacedong.beans;

import lombok.Data;

@Data
public class ChatUserBean {
    private String user_id;
    private String user_type;  // "MEMBER", "BUSINESS", or "ADMIN"
    private String nickname;  // 회원 닉네임 또는 이름
    private String profileImage;
}