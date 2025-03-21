package com.spacedong.repository;

import java.util.List;

import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.ChatMessageBean;
import com.spacedong.beans.ChatParticipantBean;
import com.spacedong.beans.ChatReadReceiptBean;
import com.spacedong.beans.ChatRoomBean;
import com.spacedong.beans.ChatUserBean;
import com.spacedong.mapper.ChatMapper;

@Repository
public class ChatRepository {

    @Autowired
    private ChatMapper chatMapper;

    // === 채팅방 관련 메서드 ===

    public int createChatRoom(ChatRoomBean chatRoom) {

        return chatMapper.createChatRoom(chatRoom);
    }

    public ChatRoomBean getChatRoomById(Long roomId) {
        return chatMapper.getChatRoomById(roomId);
    }

    public List<ChatRoomBean> getChatRoomsByUserId(String userId) {
        return chatMapper.getChatRoomsByUserId(userId);
    }

    public List<ChatRoomBean> getChatRoomsByClubId(Long clubId) {
        return chatMapper.getChatRoomsByClubId(clubId);
    }

    public ChatRoomBean getPersonalChatRoom(String userId1, String userType1, String userId2, String userType2) {
        return chatMapper.getPersonalChatRoom(userId1, userType1, userId2, userType2);
    }

    public int updateChatRoomName(Long roomId, String roomName) {
        return chatMapper.updateChatRoomName(roomId, roomName);
    }

    public int deleteChatRoom(Long roomId) {
        return chatMapper.deleteChatRoom(roomId);
    }

    // === 채팅 참여자 관련 메서드 ===

    public int addParticipant(ChatParticipantBean participant) {
        return chatMapper.addParticipant(participant);
    }

    public List<ChatParticipantBean> getParticipantsByRoomId(Long roomId) {
        return chatMapper.getParticipantsByRoomId(roomId);
    }

    public ChatParticipantBean getParticipant(int roomId, String userId) {
        return chatMapper.getParticipant(roomId, userId);
    }

    public int updateLastReadMsgId(Long roomId, String userId, Long lastReadMsgId) {
        return chatMapper.updateLastReadMsgId(roomId, userId, lastReadMsgId);
    }

    public int leaveRoom(Long roomId, String userId) {
        return chatMapper.leaveRoom(roomId, userId);
    }

    public List<ChatUserBean> searchUsers(String keyword) {
        return chatMapper.searchUsers(keyword);
    }

    public List<ChatUserBean> getClubMembers(Long clubId) {
        return chatMapper.getClubMembers(clubId);
    }

    // === 메시지 관련 메서드 ===

    public int sendMessage(ChatMessageBean message) {
        return chatMapper.sendMessage(message);
    }

    public List<ChatMessageBean> getMessagesByRoomId(Long roomId, String currentUserId) {
        return chatMapper.getMessagesByRoomId(roomId, currentUserId);
    }

    public ChatMessageBean getMessageById(Long messageId) {
        return chatMapper.getMessageById(messageId);
    }

    public int incrementReadCount(Long messageId) {
        return chatMapper.incrementReadCount(messageId);
    }

    public List<ChatMessageBean> searchMessages(Long roomId, String keyword) {
        return chatMapper.searchMessages(roomId, keyword);
    }

    // === 읽음 확인 관련 메서드 ===

    public int markAsRead(ChatReadReceiptBean readReceipt) {
        return chatMapper.markAsRead(readReceipt);
    }

    public List<ChatReadReceiptBean> getReadReceiptsByMessageId(Long messageId) {
        return chatMapper.getReadReceiptsByMessageId(messageId);
    }

    public boolean checkIfRead(Long messageId, String readerId) {
        return chatMapper.checkIfRead(messageId, readerId) > 0;
    }
}