package com.spacedong.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.spacedong.beans.*;
import com.spacedong.repository.AdminRepository;
import com.spacedong.repository.BusinessRepository;
import com.spacedong.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
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

    @Autowired
    private MemberService memberService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private BusinessService businessService;

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
                    participant.setUser_nickname(users.get(0).getNickname());
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

    /**
     * 1:1 채팅방 생성 또는 조회
     */
    @Transactional
    public ChatRoomCreationResult getOrCreatePersonalChatRoomWithResult(String userId1, String userType1, String userId2, String userType2) {
        // 비즈니스-비즈니스 채팅 방지
        if (userType1.equals("BUSINESS") && userType2.equals("BUSINESS")) {
            throw new RuntimeException("비즈니스 계정 간 채팅은 지원되지 않습니다.");
        }

        ChatRoomBean chatRoom = chatRepository.getPersonalChatRoom(userId1, userType1, userId2, userType2);

        String user1nickname = "";
        String user2nickname = "";
        String user1profile = null;
        String user2profile = null;

        boolean isNewRoom = false;
        boolean isNewParticipant = false;

        // 사용자 정보 초기화 (유형별로 다르게 처리)
        if (userType1.equals("MEMBER")) {
            MemberBean user1 = memberService.getMemberById(userId1);
            user1nickname = user1 != null ? user1.getMember_nickname() : userId1;
            user1profile = user1 != null ? user1.getMember_profile() : "";
            System.out.println("user1의 프로필 : " + user1profile);
        } else if (userType1.equals("BUSINESS")) {
            BusinessBean user1 = businessService.getBusinessById(userId1);
            user1nickname = user1 != null ? user1.getBusiness_name() : userId1;
            user1profile = user1 != null ? user1.getBusiness_profile() : "";
        } else if (userType1.equals("ADMIN")) {
            AdminBean user1 = adminRepository.getAdminById(userId1);
            user1nickname = user1 != null ? user1.getAdmin_name() : userId1;
            user1profile = ""; // 관리자는 프로필 없음
        }

        if (userType2.equals("MEMBER")) {
            MemberBean user2 = memberService.getMemberById(userId2);
            user2nickname = user2 != null ? user2.getMember_nickname() : userId2;
            user2profile = user2 != null ? user2.getMember_profile() : "";
        } else if (userType2.equals("BUSINESS")) {
            BusinessBean user2 = businessService.getBusinessById(userId2);
            user2nickname = user2 != null ? user2.getBusiness_name() : userId2;
            user2profile = user2 != null ? user2.getBusiness_profile() : "";
        } else if (userType2.equals("ADMIN")) {
            AdminBean user2 = adminRepository.getAdminById(userId2);
            user2nickname = user2 != null ? user2.getAdmin_name() : userId2;
            user2profile = ""; // 관리자는 프로필 없음
        }

        if (chatRoom == null) {
            isNewRoom = true;
            chatRoom = new ChatRoomBean();
            chatRoom.setRoom_type("PERSONAL");

            // 채팅방 이름 설정 - 항상 "nickname1,nickname2" 형식으로 설정
            // 멤버와 비즈니스가 대화할 경우 "멤버닉네임,판매자명"으로 저장

            String roomName = "";
            if ((userType1.equals("BUSINESS") && userType2.equals("MEMBER"))) {
                roomName = "판매자 " + user1nickname + ",일반회원 " + user2nickname;
            }
            else if ((userType2.equals("BUSINESS") && userType1.equals("MEMBER"))) {
                roomName = "판매자 " + user2nickname + ",일반회원 " + user1nickname;
            }
            // 관리자-일반회원 대화인 경우
            else if ((userType1.equals("ADMIN") && userType2.equals("MEMBER"))) {
                roomName = "관리자 " + user1nickname + ",일반회원 " + user2nickname;
            }
            else if ((userType2.equals("ADMIN") && userType1.equals("MEMBER"))) {
                roomName = "관리자 " + user2nickname + ",일반회원 " + user1nickname;
            }
            // 관리자-판매자 대화인 경우
            else if ((userType1.equals("ADMIN") && userType2.equals("BUSINESS"))) {
                roomName = "관리자 " + user1nickname + ",판매자 " + user2nickname;
            }
            else if ((userType2.equals("ADMIN") && userType1.equals("BUSINESS"))) {
                roomName = "관리자 " + user2nickname + ",판매자 " + user1nickname;
            }else if((userType1.equals("ADMIN") && userType2.equals("ADMIN"))) {
                roomName = "관리자 " + user1nickname + ",관리자 " + user2nickname;
            }
            // 일반회원-일반회원 대화인 경우
            else {
                roomName = "일반회원 " + user1nickname + "," + "일반회원 " + user2nickname;
            }

            chatRoom.setRoom_name(roomName);
            chatRepository.createChatRoom(chatRoom);

            // 첫 번째 참여자 추가
            ChatParticipantBean participant1 = new ChatParticipantBean();
            participant1.setRoom_id(chatRoom.getRoom_id());
            participant1.setUser_id(userId1);
            participant1.setUser_type(userType1);
            participant1.setUser_nickname(user1nickname);
            if (user1profile != null && !user1profile.isEmpty()) {
                participant1.setUserProfile(user1profile);
            }
            participant1.setJoinDate(LocalDateTime.now());
            chatRepository.addParticipant(participant1);

            // 두 번째 참여자 추가
            ChatParticipantBean participant2 = new ChatParticipantBean();
            participant2.setRoom_id(chatRoom.getRoom_id());
            participant2.setUser_id(userId2);
            participant2.setUser_type(userType2);
            participant2.setUser_nickname(user2nickname);
            if (user2profile != null && !user2profile.isEmpty()) {
                participant2.setUserProfile(user2profile);
            }
            participant2.setJoinDate(LocalDateTime.now());
            chatRepository.addParticipant(participant2);
        } else {
            // 기존 룸 이름 업데이트 - 항상 채팅방 유형 포맷을 유지
            String newRoomName = "";

            // 채팅방 타입에 맞게 이름 포맷 유지
            if ((userType1.equals("BUSINESS") && userType2.equals("MEMBER"))) {
                newRoomName = "판매자 " + user1nickname + ",일반회원 " + user2nickname;
            }
            else if ((userType2.equals("BUSINESS") && userType1.equals("MEMBER"))) {
                newRoomName = "판매자 " + user2nickname + ",일반회원 " + user1nickname;
            }
            // 관리자-일반회원 대화인 경우
            else if ((userType1.equals("ADMIN") && userType2.equals("MEMBER"))) {
                newRoomName = "관리자 " + user1nickname + ",일반회원 " + user2nickname;
            }
            else if ((userType2.equals("ADMIN") && userType1.equals("MEMBER"))) {
                newRoomName = "관리자 " + user2nickname + ",일반회원 " + user1nickname;
            }
            // 관리자-판매자 대화인 경우
            else if ((userType1.equals("ADMIN") && userType2.equals("BUSINESS"))) {
                newRoomName = "관리자 " + user1nickname + ",판매자 " + user2nickname;
            }
            else if ((userType2.equals("ADMIN") && userType1.equals("BUSINESS"))) {
                newRoomName = "관리자 " + user2nickname + ",판매자 " + user1nickname;
            }
            else if((userType1.equals("ADMIN") && userType2.equals("ADMIN"))) {
                newRoomName = "관리자 " + user1nickname + ",관리자 " + user2nickname;
            }
            // 일반회원-일반회원 대화인 경우
            else {
                newRoomName = "일반회원 " + user1nickname + "," + "일반회원 " + user2nickname;
            }

            if (!newRoomName.equals(chatRoom.getRoom_name())) {
                chatRoom.setRoom_name(newRoomName);
                chatRepository.updateChatRoomName(Long.valueOf(chatRoom.getRoom_id()), newRoomName);
            }

            // 첫 번째 사용자가 참여자가 아니면 추가
            ChatParticipantBean participant1 = chatRepository.getParticipant(chatRoom.getRoom_id(), userId1);
            if (participant1 == null) {
                isNewParticipant = true;
                participant1 = new ChatParticipantBean();
                participant1.setRoom_id(chatRoom.getRoom_id());
                participant1.setUser_id(userId1);
                participant1.setUser_type(userType1);
                participant1.setUser_nickname(user1nickname);
                if (user1profile != null && !user1profile.isEmpty()) {
                    participant1.setUserProfile(user1profile);
                }
                participant1.setJoinDate(LocalDateTime.now());
                chatRepository.addParticipant(participant1);

                // 이전 메시지 읽음 처리
                markPreviousMessagesAsRead(chatRoom.getRoom_id(), userId1);
            } else {
                // 기존 참여자 정보 업데이트
                boolean needUpdate = false;

                // 닉네임 업데이트 필요시
                if (!user1nickname.equals(participant1.getUser_nickname())) {
                    participant1.setUser_nickname(user1nickname);
                    needUpdate = true;
                }

                // 프로필 정보가 없거나 비어있는 경우에만 업데이트
                if ((participant1.getUserProfile() == null || participant1.getUserProfile().isEmpty())
                        && user1profile != null && !user1profile.isEmpty()) {
                    participant1.setUserProfile(user1profile);
                    needUpdate = true;
                }

                if (needUpdate) {
                    chatRepository.updateParticipant(participant1);
                    System.out.println("참가자1 프로필 업데이트: " + user1profile);
                }
            }

            // 두 번째 사용자가 참여자가 아니면 추가
            ChatParticipantBean participant2 = chatRepository.getParticipant(chatRoom.getRoom_id(), userId2);
            if (participant2 == null) {
                isNewParticipant = true;
                participant2 = new ChatParticipantBean();
                participant2.setRoom_id(chatRoom.getRoom_id());
                participant2.setUser_id(userId2);
                participant2.setUser_type(userType2);
                participant2.setUser_nickname(user2nickname);
                if (user2profile != null && !user2profile.isEmpty()) {
                    participant2.setUserProfile(user2profile);
                }
                participant2.setJoinDate(LocalDateTime.now());
                chatRepository.addParticipant(participant2);

                // 이전 메시지 읽음 처리
                markPreviousMessagesAsRead(chatRoom.getRoom_id(), userId2);
            } else {
                // 기존 참여자 정보 업데이트
                boolean needUpdate = false;

                // 닉네임 업데이트 필요시
                if (!user2nickname.equals(participant2.getUser_nickname())) {
                    participant2.setUser_nickname(user2nickname);
                    needUpdate = true;
                }

                // 프로필 정보가 없거나 비어있는 경우에만 업데이트
                if ((participant2.getUserProfile() == null || participant2.getUserProfile().isEmpty())
                        && user2profile != null && !user2profile.isEmpty()) {
                    participant2.setUserProfile(user2profile);
                    needUpdate = true;
                }

                if (needUpdate) {
                    chatRepository.updateParticipant(participant2);
                    System.out.println("참가자2 프로필 업데이트: " + user2profile);
                }
            }
        }

        return new ChatRoomCreationResult(chatRoom, isNewRoom, isNewParticipant);
    }

    /**
     * 기존 메서드를 새 메서드에 위임 (호환성 유지)
     */
    @Transactional
    public ChatRoomBean getOrCreatePersonalChatRoom(String userId1, String userType1, String userId2, String userType2) {
        return getOrCreatePersonalChatRoomWithResult(userId1, userType1, userId2, userType2).getRoom();
    }

    /**
     * 클럽 채팅방 생성 또는 조회 (확장된 결과 반환 버전)
     */
    @Transactional
    public ChatRoomCreationResult getOrCreateClubChatRoomWithResult(int clubId, String userId, String userType) {
        // Existing code for checking club membership and getting club info
        boolean isClubMember = false;
        String User_nickname = chatRepository.searchUsers(userId).get(0).getNickname();
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
        boolean isNewRoom = false;
        boolean isNewParticipant = false;

        // 현재 사용자 프로필 정보 가져오기
        String userProfile = null;
        if (userType.equals("MEMBER")) {
            MemberBean user = memberService.getMemberById(userId);
            userProfile = user != null ? user.getMember_profile() : null;
        }
        // 관리자 프로필은 일반적으로 null로 처리

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
                participant.setUser_nickname(User_nickname);
                participant.setUserProfile(userProfile); // 프로필 정보 추가
                participant.setJoinDate(LocalDateTime.now()); // Set join date to current time
                chatRepository.addParticipant(participant);

                // 수정: 채팅방의 모든 메시지 조회 (가입 시간과 관계없이)
                List<ChatMessageBean> allMessages = chatRepository.getAllRoomMessages((long)chatRoom.getRoom_id());

                // 메시지가 있는 경우 모두 읽음 처리
                if (allMessages != null && !allMessages.isEmpty()) {
                    // 각 메시지에 대해 읽음 표시 생성
                    for (ChatMessageBean message : allMessages) {
                        // 자신이 보낸 메시지는 제외
                        if (!message.getSenderId().equals(userId)) {
                            ChatReadReceiptBean readReceipt = new ChatReadReceiptBean();
                            readReceipt.setMessageId(message.getMessageId());
                            readReceipt.setReaderId(userId);
                            readReceipt.setReadTime(LocalDateTime.now());
                            chatRepository.markAsRead(readReceipt);
                        }
                    }

                    // 마지막 메시지 ID를 last_read_msg_id로 설정
                    Long lastMessageId = allMessages.get(allMessages.size() - 1).getMessageId();
                    chatRepository.updateLastReadMsgId((long)chatRoom.getRoom_id(), userId, lastMessageId);

                    // 디버깅 로그
                    System.out.println("새 참가자 '" + userId + "'의 모든 이전 메시지 읽음 처리 완료: " + allMessages.size() + "개의 메시지, 마지막 메시지 ID: " + lastMessageId);
                } else {
                    // 메시지가 없는 경우, 마지막 읽은 메시지 ID를 0으로 설정 (기준점 설정)
                    chatRepository.updateLastReadMsgId((long)chatRoom.getRoom_id(), userId, 0L);
                    System.out.println("새 참가자 '" + userId + "' 추가됨. 채팅방에 메시지 없음.");
                }
            } else {
                // 기존 참여자 정보 업데이트 (프로필이 변경되었을 수 있음)
                boolean needUpdate = false;

                // 닉네임 업데이트 필요시
                if (!User_nickname.equals(participant.getUser_nickname())) {
                    participant.setUser_nickname(User_nickname);
                    needUpdate = true;
                }

                // 프로필 정보가 없거나 비어있는 경우에만 업데이트
                if ((participant.getUserProfile() == null || participant.getUserProfile().isEmpty())
                        && userProfile != null && !userProfile.isEmpty()) {
                    participant.setUserProfile(userProfile);
                    needUpdate = true;
                }

                if (needUpdate) {
                    chatRepository.updateParticipant(participant);
                }
            }
        } else {
            // Create new room if none exists
            isNewRoom = true;
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
            participant.setUser_nickname(User_nickname);
            participant.setUserProfile(userProfile); // 프로필 정보 추가
            participant.setJoinDate(LocalDateTime.now());
            chatRepository.addParticipant(participant);

            // 새 채팅방이므로 마지막 읽은 메시지 ID를 0으로 설정 (기준점 설정)
            chatRepository.updateLastReadMsgId((long)chatRoom.getRoom_id(), userId, 0L);

            // Add all club members as participants
            List<ChatUserBean> clubMembers = chatRepository.getClubMembers((long)clubId);
            for (ChatUserBean member : clubMembers) {
                // 현재 사용자는 건너뛰기 (이미 추가됨)
                if (member.getUser_id().equals(userId)) {
                    continue;
                }

                // 멤버 정보 가져오기
                String memberNickname = chatRepository.searchUsers(member.getUser_id()).get(0).getNickname();

                // 멤버 프로필 정보 가져오기
                MemberBean memberInfo = memberService.getMemberById(member.getUser_id());
                String memberProfile = memberInfo != null ? memberInfo.getMember_profile() : null;

                ChatParticipantBean memberParticipant = new ChatParticipantBean();
                memberParticipant.setRoom_id(chatRoom.getRoom_id());
                memberParticipant.setUser_id(member.getUser_id());
                memberParticipant.setUser_type("MEMBER");
                memberParticipant.setUser_nickname(memberNickname);
                memberParticipant.setUserProfile(memberProfile); // 프로필 정보 추가
                memberParticipant.setJoinDate(LocalDateTime.now());
                chatRepository.addParticipant(memberParticipant);

                // 새 채팅방이므로 각 멤버의 마지막 읽은 메시지 ID를 0으로 설정
                chatRepository.updateLastReadMsgId((long)chatRoom.getRoom_id(), member.getUser_id(), 0L);
            }
        }

        return new ChatRoomCreationResult(chatRoom, isNewRoom, isNewParticipant);
    }

    /**
     * 기존 메서드를 새 메서드에 위임 (호환성 유지)
     */
    @Transactional
    public ChatRoomBean getOrCreateClubChatRoom(int clubId, String userId, String userType) {
        return getOrCreateClubChatRoomWithResult(clubId, userId, userType).getRoom();
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
                message.setSenderProfile(member.getMember_profile()); // 멤버 프로필 추가
            }
        } else if (message.getSenderType().equals("BUSINESS")) {
            BusinessBean business = businessRepository.getBusinessById(message.getSenderId());
            if (business != null) {
                message.setSenderNickname(business.getBusiness_name());
                message.setSenderProfile(business.getBusiness_profile()); // 비즈니스 프로필 추가
            }
        } else if (message.getSenderType().equals("ADMIN")) {
            AdminBean admin = adminRepository.getAdminById(message.getSenderId());
            if (admin != null) {
                message.setSenderNickname(admin.getAdmin_name());
                // 관리자는 보통 프로필 없음, 필요하다면 admin.getAdmin_profile() 추가
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
    public void markPreviousMessagesAsRead(int roomId, String userId) {
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

        // 읽지 않은 메시지 가져오기 (자신이 보낸 메시지 제외)
        List<ChatMessageBean> unreadMessages = chatRepository.getMessagesByRoomId(roomId, userId)
                .stream()
                .filter(m -> !m.getSenderId().equals(userId) && !m.isRead())
                .collect(Collectors.toList());

        if (unreadMessages.isEmpty()) {
            return;
        }

        // 가장 최근 메시지 ID 조회
        Long lastMessageId = unreadMessages.get(unreadMessages.size() - 1).getMessageId();

        // last_read_msg_id 업데이트
        chatRepository.updateLastReadMsgId(roomId, userId, lastMessageId);

        // 모든 메시지에 대해 읽음 처리
        for (ChatMessageBean message : unreadMessages) {
            if (!chatRepository.checkIfRead(message.getMessageId(), userId)) {
                ChatReadReceiptBean readReceipt = new ChatReadReceiptBean();
                readReceipt.setMessageId(message.getMessageId());
                readReceipt.setReaderId(userId);
                readReceipt.setReadTime(LocalDateTime.now());
                chatRepository.markAsRead(readReceipt);

                // 메시지 읽음 카운트 증가
                chatRepository.incrementReadCount(message.getMessageId());
            }
        }
    }

    public int getUnreadMessageCount(Long roomId, String userId) {
        return chatRepository.getUnreadMessageCount(roomId, userId);
    }

    public int getTotalUnreadMessageCount(String userId) {
        return chatRepository.getTotalUnreadMessageCount(userId);
    }

    /**
     * 채팅방에서 현재 사용자를 제외한 다른 참여자 정보를 가져옵니다.
     * 주로 1:1 채팅에서 상대방 정보를 가져오는 데 사용됩니다.
     *
     * @param roomId 채팅방 ID
     * @param currentUserId 현재 사용자 ID
     * @return 상대방 참여자 정보 (없으면 null)
     */
    public ChatParticipantBean getOtherParticipant(long roomId, String currentUserId) {
        List<ChatParticipantBean> participants = chatRepository.getParticipantsByRoomId(roomId);

        if (participants != null && !participants.isEmpty()) {
            // 현재 사용자가 아닌 참여자 찾기
            for (ChatParticipantBean participant : participants) {
                if (!participant.getUser_id().equals(currentUserId)) {
                    return participant;
                }
            }
        }

        return null; // 다른 참여자가 없는 경우
    }

    @PostConstruct
    public void initChatSystem() {
        try {
            // 닉네임이 비어있는 참여자들의 정보 업데이트
            int updatedRows = chatRepository.updateAllParticipantsNickname();

            if (updatedRows > 0) {
                System.out.println("채팅 참여자 닉네임 자동 업데이트 완료: " + updatedRows + "명");
            }

            // 남아있는 닉네임 없는 참여자 확인
            List<ChatParticipantBean> emptyNicknameParticipants = chatRepository.getParticipantsWithoutNickname();

            // 개별 처리
            for (ChatParticipantBean participant : emptyNicknameParticipants) {
                if (participant.getUser_type().equals("MEMBER")) {
                    MemberBean member = memberService.getMemberById(participant.getUser_id());
                    if (member != null) {
                        participant.setUser_nickname(member.getMember_nickname());
                        chatRepository.updateParticipantNickname(participant);
                        System.out.println("참여자 닉네임 업데이트: " + participant.getUser_id() + " -> " + member.getMember_nickname());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("채팅 시스템 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

    }

    // ClubService.java에 추가할 메서드
    /**
     * 클럽 ID로 클럽 프로필 이미지 경로 조회
     *
     * @param clubId 클럽 ID
     * @return 클럽 프로필 이미지 경로 (없으면 null)
     */
    public String getClubProfile(Integer clubId) {
        if (clubId == null) {
            return null;
        }

        ClubBean club = clubService.oneClubInfo(clubId);
        return club != null ? club.getClub_profile() : null;
    }

    // 채팅 시스템에 필요한 초기 설정
    @Transactional
    public void setupChatSystem() {
        // 테이블이나 시퀀스가 없는 경우 생성하는 로직을 추가할 수 있음
        // 이 메소드는 애플리케이션 시작 시 호출할 수 있음
    }
}