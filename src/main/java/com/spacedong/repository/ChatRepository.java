package com.spacedong.repository;

import java.util.ArrayList;
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

    // 모든 채팅방 가져오기 (기존 메서드)
    public List<ChatRoomBean> getChatRoomsByUserId(String userId) {
        // 개인 채팅방과 클럽 채팅방을 따로 가져와서 합치기
        List<ChatRoomBean> personalRooms = chatMapper.getPersonalChatRoomsByUserId(userId);
        List<ChatRoomBean> clubRooms = chatMapper.getClubChatRoomsByUserId(userId);

        // 두 리스트 합치기
        List<ChatRoomBean> allRooms = new ArrayList<>();
        allRooms.addAll(personalRooms);
        allRooms.addAll(clubRooms);

        return allRooms;
    }

    // 개인 채팅방만 가져오기 (새 메서드)
    public List<ChatRoomBean> getPersonalChatRoomsByUserId(String userId) {
        return chatMapper.getPersonalChatRoomsByUserId(userId);
    }

    // 클럽 채팅방만 가져오기 (새 메서드)
    public List<ChatRoomBean> getClubChatRoomsByUserId(String userId) {
        return chatMapper.getClubChatRoomsByUserId(userId);
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
        // 해당 채팅방 정보 조회
        ChatRoomBean room = chatMapper.getChatRoomById(roomId);

        // 채팅방 타입에 따라 다른 메서드 호출
        if (room != null) {
            if ("CLUB".equals(room.getRoom_type())) {
                // 클럽 채팅방인 경우 - 가입 시간 이후 메시지만
                return chatMapper.getClubChatMessages(roomId, currentUserId);
            } else {
                // 개인 채팅방인 경우 - 모든 메시지
                return chatMapper.getPersonalChatMessages(roomId, currentUserId);
            }
        }

        // 채팅방이 없거나 타입을 알 수 없는 경우 빈 리스트 반환
        return new ArrayList<>();
    }

    // 채팅방 타입별 메시지 조회 메서드 추가
    public List<ChatMessageBean> getPersonalChatMessages(Long roomId, String currentUserId) {
        return chatMapper.getPersonalChatMessages(roomId, currentUserId);
    }

    public List<ChatMessageBean> getClubChatMessages(Long roomId, String currentUserId) {
        return chatMapper.getClubChatMessages(roomId, currentUserId);
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

    public int getUnreadMessageCount(Long roomId, String userId) {
        return chatMapper.getUnreadMessageCount(roomId, userId);
    }

    public int getTotalUnreadMessageCount(String userId) {
        return chatMapper.getTotalUnreadMessageCount(userId);
    }

    /**
     * 참여자의 닉네임을 업데이트합니다.
     *
     * @param participant 업데이트할 참여자 정보
     * @return 업데이트된 행 수
     */
    public int updateParticipantNickname(ChatParticipantBean participant) {
        return chatMapper.updateParticipantNickname(participant);
    }

    /**
     * 닉네임이 없는 모든 참여자의 정보를 업데이트합니다.
     *
     * @return 업데이트된 행 수
     */
    public int updateAllParticipantsNickname() {
        return chatMapper.updateAllParticipantsNickname();
    }

    /**
     * 닉네임이 없는 참여자 목록을 조회합니다.
     *
     * @return 닉네임이 없는 참여자 목록
     */
    public List<ChatParticipantBean> getParticipantsWithoutNickname() {
        return chatMapper.getParticipantsWithoutNickname();
    }

    public int updateParticipant(ChatParticipantBean participant) {
        return chatMapper.updateParticipant(participant);
    }

    public void updateProfile(String user_id, String userProfile){
        chatMapper.updateProfile(user_id, userProfile);
    }


}