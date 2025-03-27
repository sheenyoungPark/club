package com.spacedong.service;

import com.spacedong.beans.ChatUserBean;
import com.spacedong.mapper.ChatUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatUserService {

    @Autowired
    private ChatUserMapper chatUserMapper;

    public ChatUserBean getChatPartner(Long roomId, String myId) {
        return chatUserMapper.getChatPartner(roomId, myId);
    }

}
