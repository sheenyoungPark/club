package com.spacedong.repository;

import com.spacedong.beans.ChatUserBean;
import com.spacedong.mapper.ChatUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ChatUserRepository {

    @Autowired
    private ChatUserMapper chatUserMapper;

    public ChatUserBean getChatPartner(Long roomId, String myId) {
        return chatUserMapper.getChatPartner(roomId, myId);
    }

}
