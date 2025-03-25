package com.spacedong.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spacedong.beans.*;
import com.spacedong.repository.AdminRepository;
import com.spacedong.repository.BusinessRepository;
import com.spacedong.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spacedong.repository.ChatRepository;

import jakarta.annotation.Resource;

@Service
public class ChatService {

    @Resource(name = "loginMember")
    private MemberBean loginMember;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ClubService clubService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private AdminRepository adminRepository;

    // 채팅방 관련 메서드
    @Transactional
    public ChatRoomBean createChatRoom(ChatRoomBean chatRoom, List<ChatParticipantBean> participants) {
        // For personal chats, make sure club_id is null
        if ("PERSONAL".equals(chatRoom.getRoom_type())) {
            chatRoom.setClub_id(null);
        }

        chatRepository.createChatRoom(chatRoom);

        for (ChatParticipantBean participant : participants) {
            participant.setRoom_id(chatRoom.getRoom_id());

            // 참가자의 닉네임 설정 추가
            String userId = participant.getUser_id();
            if (userId != null) {
                // searchUsers 메서드를 통해 사용자의 닉네임을 가져옴
                List<ChatUserBean> users = chatRepository.searchUsers(userId);
                if (users != null && !users.isEmpty()) {
                    participant.setUserNickname(users.get(0).getNickname());
                }
            }

            chatRepository.addParticipant(participant);
        }

        return chatRoom;
    }

    public ChatRoomBean getChatRoomById(Long roomId) {
        return chatRepository.getChatRoomById(roomId);
    }

    public List<ChatRoomBean> getChatRoomsByUserId(String userId) {
        return chatRepository.getChatRoomsByUserId(userId);
    }

    public List<ChatRoomBean> getChatRoomsByClubId(Long clubId) {
        return chatRepository.getChatRoomsByClubId(clubId);
    }

    // 현재 로그인한 사용자의 채팅방 목록 조회
    public List<ChatRoomBean> getMyRooms() {
        if(loginMember != null && loginMember.isLogin()) {
            // 개인 채팅방과 클럽 채팅방을 모두 가져오기
            return getChatRoomsByUserId(loginMember.getMember_id());
        }
        return new ArrayList<>();
    }

    // Also modify the getOrCreatePersonalChatRoom method to include similar logic
    @Transactional
    public ChatRoomBean getOrCreatePersonalChatRoom(String userId1, String userType1, String userId2, String userType2) {
        ChatRoomBean chatRoom = chatRepository.getPersonalChatRoom(userId1, userType1, userId2, userType2);

        String user1nickname = chatRepository.searchUsers(userId1).get(0).getNickname();
        String user2nickname = chatRepository.searchUsers(userId2).get(0).getNickname();

        boolean isNewRoom = false;

        if (chatRoom == null) {
            isNewRoom = true;
            chatRoom = new ChatRoomBean();
            chatRoom.setRoom_type("PERSONAL");

            // Room name based on user types
            String roomName = "";
            if(userType2.equals("MEMBER")) {
                MemberBean mem1 = memberRepository.getMemberById(userId1);
                MemberBean mem2 = memberRepository.getMemberById(userId2);
                roomName = mem1.getMember_nickname() + mem2.getMember_nickname()+"의 대화";
            } else if(userType2.equals("BUSINESS")) {
                roomName = "판매자와의 대화";
            } else if(userType2.equals("ADMIN")) {
                roomName = "관리자와의 대화";
            }

            chatRoom.setRoom_name(roomName);
            chatRepository.createChatRoom(chatRoom);

            // Add participants
            ChatParticipantBean participant1 = new ChatParticipantBean();
            participant1.setRoom_id(chatRoom.getRoom_id());
            participant1.setUser_id(userId1);
            participant1.setUser_type(userType1);
            participant1.setUserNickname(user1nickname);
            participant1.setJoinDate(LocalDateTime.now()); // Set join date
            chatRepository.addParticipant(participant1);

            ChatParticipantBean participant2 = new ChatParticipantBean();
            participant2.setRoom_id(chatRoom.getRoom_id());
            participant2.setUser_id(userId2);
            participant2.setUser_type(userType2);
            participant2.setUserNickname(user2nickname);
            participant2.setJoinDate(LocalDateTime.now()); // Set join date
            chatRepository.addParticipant(participant2);
        } else {
            // Check if users are already participants, if not add them
            // This covers the case where a user was removed and is rejoining
            ChatParticipantBean participant1 = chatRepository.getParticipant(chatRoom.getRoom_id(), userId1);
            if (participant1 == null) {
                participant1 = new ChatParticipantBean();
                participant1.setRoom_id(chatRoom.getRoom_id());
                participant1.setUser_id(userId1);
                participant1.setUser_type(userType1);
                participant1.setUserNickname(user1nickname);
                participant1.setJoinDate(LocalDateTime.now());
                chatRepository.addParticipant(participant1);

                // Mark all previous messages as read for the new participant
                markPreviousMessagesAsRead(chatRoom.getRoom_id(), userId1);
            }

            ChatParticipantBean participant2 = chatRepository.getParticipant(chatRoom.getRoom_id(), userId2);
            if (participant2 == null) {
                participant2 = new ChatParticipantBean();
                participant2.setRoom_id(chatRoom.getRoom_id());
                participant2.setUser_id(userId2);
                participant2.setUser_type(userType2);
                participant2.setUserNickname(user2nickname);
                participant2.setJoinDate(LocalDateTime.now());
                chatRepository.addParticipant(participant2);

                // Mark all previous messages as read for the new participant
                markPreviousMessagesAsRead(chatRoom.getRoom_id(), userId2);
            }
        }

        return chatRoom;
    }

    @Transactional
    public ChatRoomBean getOrCreateClubChatRoom(int clubId, String userId, String userType) {
        // Existing code for checking club membership and getting club info
        boolean isClubMember = false;
        String usernickname = chatRepository.searchUsers(userId).get(0).getNickname();
        if (userType.equals("MEMBER")) {
            isClubMember = clubService.isMemberOfClub(clubId, userId);
        } else if (userType.equals("ADMIN")) {
            isClubMember = true;
        }

        if (!isClubMember) {
            throw new RuntimeException("클럽 회원이 아닙니다.");
        }

        ClubBean clubInfo = clubService.oneClubInfo(clubId);
        if (clubInfo == null) {
            throw new RuntimeException("존재하지 않는 클럽입니다.");
        }

        List<ChatRoomBean> clubRooms = chatRepository.getChatRoomsByClubId((long)clubId);
        ChatRoomBean chatRoom = null;
        boolean isNewParticipant = false;

        // Existing code for finding or creating a room
        if (!clubRooms.isEmpty()) {
            chatRoom = clubRooms.get(0);

            // Check if user is already a participant
            ChatParticipantBean participant = chatRepository.getParticipant(chatRoom.getRoom_id(), userId);
            if (participant == null) {
                // User is not a participant, add them
                isNewParticipant = true;
                participant = new ChatParticipantBean();
                participant.setRoom_id(chatRoom.getRoom_id());
                participant.setUser_id(userId);
                participant.setUser_type(userType);
                participant.setUserNickname(usernickname);
                participant.setJoinDate(LocalDateTime.now()); // Set join date to current time
                chatRepository.addParticipant(participant);

                // Mark all previous messages as read for the new participant
                markPreviousMessagesAsRead(chatRoom.getRoom_id(), userId);
            }
        } else {
            // Create new room if none exists
            chatRoom = new ChatRoomBean();
            chatRoom.setRoom_type("CLUB");
            chatRoom.setClub_id(clubId);
            chatRoom.setRoom_name(clubInfo.getClub_name() + " 채팅");
            chatRepository.createChatRoom(chatRoom);

            // Add current user as first participant
            ChatParticipantBean participant = new ChatParticipantBean();
            participant.setRoom_id(chatRoom.getRoom_id());
            participant.setUser_id(userId);
            participant.setUser_type(userType);
            participant.setUserNickname(usernickname);
            participant.setJoinDate(LocalDateTime.now());
            chatRepository.addParticipant(participant);

            // Add all club members as participants
            List<ChatUserBean> clubMembers = chatRepository.getClubMembers((long)clubId);
            for (ChatUserBean member : clubMembers) {
                String memberNickname = chatRepository.searchUsers(member.getUser_id()).get(0).getNickname();
                if (!member.getUser_id().equals(userId)) {  // Skip current user as they're already added
                    ChatParticipantBean memberParticipant = new ChatParticipantBean();
                    memberParticipant.setRoom_id(chatRoom.getRoom_id());
                    memberParticipant.setUser_id(member.getUser_id());
                    memberParticipant.setUser_type("MEMBER");
                    memberParticipant.setUserNickname(memberNickname);
                    memberParticipant.setJoinDate(LocalDateTime.now());
                    chatRepository.addParticipant(memberParticipant);

                    // No need to mark messages as read for a new room (no messages yet)
                }
            }
        }

        return chatRoom;
    }

    // 채팅 참여자 관련 메서드
    public List<ChatParticipantBean> getRoomParticipants(Long roomId) {
        return chatRepository.getParticipantsByRoomId(roomId);
    }

    public int leaveRoom(Long roomId, String userId) {
        return chatRepository.leaveRoom(roomId, userId);
    }

    // 메시지 관련 메서드
    @Transactional
    public ChatMessageBean sendMessage(ChatMessageBean message) {


        if (message.getSenderType().equals("MEMBER")) {
            MemberBean member = memberRepository.getMemberById(message.getSenderId());
            if (member != null) {
                message.setSenderNickname(member.getMember_nickname());
            }
        } else if (message.getSenderType().equals("BUSINESS")) {
            BusinessBean business = businessRepository.getBusinessById(message.getSenderId());
            if (business != null) {
                message.setSenderNickname(business.getBusiness_name());
            }
        } else if (message.getSenderType().equals("ADMIN")) {
            AdminBean admin = adminRepository.getAdminById(message.getSenderId());
            if (admin != null) {
                message.setSenderNickname(admin.getAdmin_name());
            }
        }

        chatRepository.sendMessage(message);

        // 실시간으로 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), message);

        // 참여자들에게 알림 전송
        List<ChatParticipantBean> participants = chatRepository.getParticipantsByRoomId(message.getRoomId());
        for (ChatParticipantBean participant : participants) {
            if (!participant.getUser_id().equals(message.getSenderId())) {
                messagingTemplate.convertAndSendToUser(
                        participant.getUser_id(),
                        "/queue/notifications",
                        message
                );
            }
        }

        return message;
    }

    public List<ChatMessageBean> getRoomMessages(Long roomId, String currentUserId) {
        return chatRepository.getMessagesByRoomId(roomId, currentUserId);
    }

    @Transactional
    public void markAsRead(Long roomId, String userId, Long messageId) {
        // 이미 읽은 메시지인지 확인
        if (!chatRepository.checkIfRead(messageId, userId)) {
            ChatReadReceiptBean readReceipt = new ChatReadReceiptBean();
            readReceipt.setMessageId(messageId);
            readReceipt.setReaderId(userId);
            readReceipt.setReadTime(LocalDateTime.now());  // 현재 시간으로 설정

            // 읽음 표시 저장
            int result = chatRepository.markAsRead(readReceipt);
            System.out.println("읽음 표시 저장 결과: " + result + ", 메시지 ID: " + messageId + ", 사용자: " + userId);

            // 메시지 읽음 수 증가
            int incrementResult = chatRepository.incrementReadCount(messageId);
            System.out.println("읽음 수 증가 결과: " + incrementResult + ", 메시지 ID: " + messageId);

            // 참가자의 마지막으로 읽은 메시지 ID 업데이트
            int updateResult = chatRepository.updateLastReadMsgId(roomId, userId, messageId);
            System.out.println("마지막 읽은 메시지 ID 업데이트 결과: " + updateResult + ", 룸 ID: " + roomId + ", 사용자: " + userId);
        } else {
            System.out.println("이미 읽은 메시지입니다. 메시지 ID: " + messageId + ", 사용자: " + userId);
        }
    }

    // New method to mark all previous messages as read for a new participant
    @Transactional
    private void markPreviousMessagesAsRead(int roomId, String userId) {
        // Get all messages in the room
        List<ChatMessageBean> allMessages = chatRepository.getMessagesByRoomId((long)roomId, userId);

        // For each message, create a read receipt
        for (ChatMessageBean message : allMessages) {
            // Check if this message was sent before the user joined
            // Skip if user is the sender (no need to mark your own messages as read)
            if (!message.getSenderId().equals(userId)) {
                // Only create read receipt if one doesn't already exist
                if (!chatRepository.checkIfRead(message.getMessageId(), userId)) {
                    ChatReadReceiptBean readReceipt = new ChatReadReceiptBean();
                    readReceipt.setMessageId(message.getMessageId());
                    readReceipt.setReaderId(userId);
                    readReceipt.setReadTime(LocalDateTime.now());
                    chatRepository.markAsRead(readReceipt);

                    // Update read count for the message
                    chatRepository.incrementReadCount(message.getMessageId());
                }
            }
        }

        // If there are messages, update the last read message ID for the participant
        if (!allMessages.isEmpty()) {
            Long lastMessageId = allMessages.get(allMessages.size() - 1).getMessageId();
            chatRepository.updateLastReadMsgId((long)roomId, userId, lastMessageId);
        }
    }

    // 사용자 검색 관련 메서드
    public List<ChatUserBean> searchUsers(String keyword) {
        return chatRepository.searchUsers(keyword);
    }

    public List<ChatUserBean> getClubMembers(Long clubId) {
        return chatRepository.getClubMembers(clubId);
    }

    // 메시지 검색
    public List<ChatMessageBean> searchMessages(Long roomId, String keyword) {
        return chatRepository.searchMessages(roomId, keyword);
    }

    // 클럽 검색
    public List<Map<String, Object>> searchClubs(String keyword) {
        List<ClubBean> clubs = clubService.searchClubs("name", keyword);
        List<Map<String, Object>> results = new ArrayList<>();

        for (ClubBean club : clubs) {
            Map<String, Object> clubMap = new HashMap<>();
            clubMap.put("clubId", club.getClub_id());
            clubMap.put("clubName", club.getClub_name());
            clubMap.put("clubCategory", club.getClub_category());
            clubMap.put("clubRegion", club.getClub_region());
            clubMap.put("clubProfile", club.getClub_profile());

            results.add(clubMap);
        }

        return results;
    }

    public ChatMessageBean getMessageById(Long messageId) {
        System.out.println("ChatRepository.getMessageById 호출: " + messageId);
        ChatMessageBean message = chatRepository.getMessageById(messageId);
        System.out.println("조회 결과: " + (message != null ? "메시지 찾음" : "메시지 없음"));
        return message;
    }

    // 메시지 ID로 읽음 확인 목록 조회
    public List<ChatReadReceiptBean> getReadReceiptsByMessageId(Long messageId) {
        return chatRepository.getReadReceiptsByMessageId(messageId);
    }

    /**
     * 사용자의 총 안 읽은 메시지 수 계산
     * @param userId 사용자 ID
     * @return 총 안 읽은 메시지 수
     */
    public int calculateTotalUnreadMessages(String userId) {
        // 사용자의 모든 채팅방 조회 - 분리된 메서드를 통해 조회
        List<ChatRoomBean> personalRooms = chatRepository.getPersonalChatRoomsByUserId(userId);
        List<ChatRoomBean> clubRooms = chatRepository.getClubChatRoomsByUserId(userId);

        int totalUnread = 0;

        // 개인 채팅방의 안 읽은 메시지 수 합산
        for (ChatRoomBean room : personalRooms) {
            totalUnread += room.getUnreadCount();
        }

        // 클럽 채팅방의 안 읽은 메시지 수 합산
        for (ChatRoomBean room : clubRooms) {
            totalUnread += room.getUnreadCount();
        }

        return totalUnread;
    }

    @Transactional
    public void markAllMessagesAsRead(Long roomId, String userId) {
        // 현재 사용자의 참여 정보 확인
        ChatParticipantBean participant = chatRepository.getParticipant(roomId.intValue(), userId);
        if (participant == null) {
            return; // 참여자가 아닌 경우 처리하지 않음
        }

        // 가장 최근 메시지 ID 조회
        List<ChatMessageBean> messages = chatRepository.getMessagesByRoomId(roomId, userId);
        if (messages == null || messages.isEmpty()) {
            return; // 메시지가 없는 경우
        }

        // 가장 최근 메시지 ID
        Long lastMessageId = messages.get(messages.size() - 1).getMessageId();

        // last_read_msg_id 업데이트
        chatRepository.updateLastReadMsgId(roomId, userId, lastMessageId);

        // 읽지 않은 모든 메시지에 대해 읽음 처리
        for (ChatMessageBean message : messages) {
            if (!message.getSenderId().equals(userId) && !chatRepository.checkIfRead(message.getMessageId(), userId)) {
                ChatReadReceiptBean readReceipt = new ChatReadReceiptBean();
                readReceipt.setMessageId(message.getMessageId());
                readReceipt.setReaderId(userId);
                readReceipt.setReadTime(LocalDateTime.now());
                chatRepository.markAsRead(readReceipt);
            }
        }
    }

    public int getUnreadMessageCount(Long roomId, String userId) {
        return chatRepository.getUnreadMessageCount(roomId, userId);
    }

    public int getTotalUnreadMessageCount(String userId) {
        return chatRepository.getTotalUnreadMessageCount(userId);
    }




    // 채팅 시스템에 필요한 초기 설정
    @Transactional
    public void setupChatSystem() {
        // 테이블이나 시퀀스가 없는 경우 생성하는 로직을 추가할 수 있음
        // 이 메소드는 애플리케이션 시작 시 호출할 수 있음
    }
}