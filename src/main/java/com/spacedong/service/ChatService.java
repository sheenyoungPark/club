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
            return getChatRoomsByUserId(loginMember.getMember_id());
        }
        return new ArrayList<>();
    }

    // 1:1 채팅방 찾기 또는 생성하기
    @Transactional
    public ChatRoomBean getOrCreatePersonalChatRoom(String userId1, String userType1, String userId2, String userType2) {
        ChatRoomBean chatRoom = chatRepository.getPersonalChatRoom(userId1, userType1, userId2, userType2);

        String user1nickname = chatRepository.searchUsers(userId1).get(0).getNickname();
        String user2nickname = chatRepository.searchUsers(userId2).get(0).getNickname();

        if (chatRoom == null) {
            chatRoom = new ChatRoomBean();
            chatRoom.setRoom_type("PERSONAL");

            // 상대방의 닉네임이나 이름을 가져와서 채팅방 이름으로 설정
            String roomName = "";
            if(userType2.equals("MEMBER")) {
                //회원의 정보 가져오기
                MemberBean mem1 = memberRepository.getMemberById(userId1);
                MemberBean mem2 = memberRepository.getMemberById(userId2);

                roomName = mem1.getMember_nickname() + mem2.getMember_nickname()+"의 대화";
                // 모든 채팅 참여자 정보 조회 후 설정할 수도 있음
            } else if(userType2.equals("BUSINESS")) {


                roomName = "판매자와의 대화";
            } else if(userType2.equals("ADMIN")) {
                roomName = "관리자와의 대화";
            }

            chatRoom.setRoom_name(roomName);

            chatRepository.createChatRoom(chatRoom);

            // 참여자 추가
            ChatParticipantBean participant1 = new ChatParticipantBean();
            participant1.setRoom_id(chatRoom.getRoom_id());
            participant1.setUser_id(userId1);
            participant1.setUser_type(userType1);
            participant1.setUserNickname(user1nickname);
            chatRepository.addParticipant(participant1);

            ChatParticipantBean participant2 = new ChatParticipantBean();
            participant2.setRoom_id(chatRoom.getRoom_id());
            participant2.setUser_id(userId2);
            participant2.setUser_type(userType2);
            participant2.setUserNickname(user2nickname);
            chatRepository.addParticipant(participant2);
        }

        return chatRoom;
    }

    // 클럽 채팅방 찾기 또는 생성하기
    @Transactional
    public ChatRoomBean getOrCreateClubChatRoom(int clubId, String userId, String userType) {
        // 클럽 존재 여부 확인 (클럽 회원인지도 확인)
        boolean isClubMember = false;
        String usernickname = chatRepository.searchUsers(userId).get(0).getNickname();
        if (userType.equals("MEMBER")) {
            // 클럽 회원인지 확인
            isClubMember = clubService.isMemberOfClub(clubId, userId);
        } else if (userType.equals("ADMIN")) {
            // 관리자는 항상 모든 클럽에 접근 가능
            isClubMember = true;
        }

        if (!isClubMember) {
            throw new RuntimeException("클럽 회원이 아닙니다.");
        }

        // 클럽 정보 가져오기
        ClubBean clubInfo = clubService.oneClubInfo(clubId);
        if (clubInfo == null) {
            throw new RuntimeException("존재하지 않는 클럽입니다.");
        }

        // 클럽 채팅방 찾기
        List<ChatRoomBean> clubRooms = chatRepository.getChatRoomsByClubId((long)clubId);
        ChatRoomBean chatRoom = null;

        // 기존 클럽 채팅방이 있는 경우
        if (!clubRooms.isEmpty()) {
            chatRoom = clubRooms.get(0);

            // 사용자가 이미 참여자인지 확인
            ChatParticipantBean participant = chatRepository.getParticipant(chatRoom.getRoom_id(), userId);
            if (participant == null) {
                // 참여자가 아니면 추가
                participant = new ChatParticipantBean();
                participant.setRoom_id(chatRoom.getRoom_id());
                participant.setUser_id(userId);
                participant.setUser_type(userType);
                participant.setUserNickname(usernickname);
                chatRepository.addParticipant(participant);
            }
        } else {
            // 채팅방이 없으면 생성
            chatRoom = new ChatRoomBean();
            chatRoom.setRoom_type("CLUB");
            chatRoom.setClub_id(clubId);

            // 클럽 이름 설정
            chatRoom.setRoom_name(clubInfo.getClub_name() + " 채팅");

            chatRepository.createChatRoom(chatRoom);

            // 현재 사용자를 참여자로 추가
            ChatParticipantBean participant = new ChatParticipantBean();
            participant.setRoom_id(chatRoom.getRoom_id());
            participant.setUser_id(userId);
            participant.setUser_type(userType);
            participant.setUserNickname(usernickname);
            chatRepository.addParticipant(participant);

            // 모든 클럽 회원을 참여자로 추가
            List<ChatUserBean> clubMembers = chatRepository.getClubMembers((long)clubId);
            for (ChatUserBean member : clubMembers) {
                String memberNickname = chatRepository.searchUsers(member.getUser_id()).get(0).getNickname();
                if (!member.getUser_id().equals(userId)) {  // 현재 사용자는 이미 추가했으므로 제외
                    ChatParticipantBean memberParticipant = new ChatParticipantBean();
                    memberParticipant.setRoom_id(chatRoom.getRoom_id());
                    memberParticipant.setUser_id(member.getUser_id());
                    memberParticipant.setUser_type("MEMBER");
                    memberParticipant.setUserNickname(memberNickname);
                    chatRepository.addParticipant(memberParticipant);
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

    // 읽음 확인 관련 메서드
    @Transactional
    public void markAsRead(Long roomId, String userId, Long messageId) {
        // 이미 읽은 메시지인지 확인
        if (!chatRepository.checkIfRead(messageId, userId)) {
            ChatReadReceiptBean readReceipt = new ChatReadReceiptBean();
            readReceipt.setMessageId(messageId);
            readReceipt.setReaderId(userId);
            readReceipt.setReadTime(LocalDateTime.now());  // 현재 시간으로 설정

            // 읽음 표시 저장
            chatRepository.markAsRead(readReceipt);

            // 메시지 읽음 수 증가
            chatRepository.incrementReadCount(messageId);

            // 참가자의 마지막으로 읽은 메시지 ID 업데이트
            chatRepository.updateLastReadMsgId(roomId, userId, messageId);

            // 메시지 정보 가져오기 (이 부분은 컨트롤러에서 처리)
            // ChatMessageBean message = chatRepository.getMessageById(messageId);

            // 여기서는 DB 업데이트만 처리하고 알림 전송은 컨트롤러에서 담당
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

    // 채팅 시스템에 필요한 초기 설정
    @Transactional
    public void setupChatSystem() {
        // 테이블이나 시퀀스가 없는 경우 생성하는 로직을 추가할 수 있음
        // 이 메소드는 애플리케이션 시작 시 호출할 수 있음
    }
}