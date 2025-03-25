package com.spacedong.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.spacedong.beans.*;
import com.spacedong.service.ClubMemberService;
import com.spacedong.service.ClubService;
import com.spacedong.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spacedong.service.ChatService;

import jakarta.annotation.Resource;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Resource(name = "loginMember")
    private MemberBean loginMember;

    @Resource(name = "loginBusiness")
    private BusinessBean loginBusiness;

    @Resource(name = "loginAdmin")
    private AdminBean loginAdmin;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ClubService clubService;
    @Autowired
    private ClubMemberService clubMemberService;

    // 로그인한 사용자 정보를 가져오는 헬퍼 메서드
    private Map<String, String> getLoggedInUserInfo() {
        Map<String, String> userInfo = new HashMap<>();

        if (loginMember != null && loginMember.isLogin()) {
            userInfo.put("userId", loginMember.getMember_id());
            userInfo.put("userType", "MEMBER");
            userInfo.put("userNickname", loginMember.getMember_nickname());

            // 프로필 정보가 null이 아닌지 확인하고 설정
            String profilePath = loginMember.getMember_profile();
            userInfo.put("userProfile", profilePath != null ? profilePath : "");

            System.out.println("로그인 회원 프로필: " + profilePath);
        } else if (loginBusiness != null && loginBusiness.isLogin()) {
            userInfo.put("userId", loginBusiness.getBusiness_id());
            userInfo.put("userType", "BUSINESS");
            userInfo.put("userNickname", loginBusiness.getBusiness_name());

            // 프로필 정보가 null이 아닌지 확인하고 설정
            String profilePath = loginBusiness.getBusiness_profile();
            userInfo.put("userProfile", profilePath != null ? profilePath : "");

            System.out.println("로그인 비즈니스 프로필: " + profilePath);
        } else if (loginAdmin != null && loginAdmin.isAdmin_login()) {
            userInfo.put("userId", loginAdmin.getAdmin_id());
            userInfo.put("userType", "ADMIN");
            userInfo.put("userNickname", loginAdmin.getAdmin_name());
            userInfo.put("userProfile", ""); // 관리자는 일반적으로 프로필 이미지가 없음
        }

        return userInfo;
    }

    // 로그인 체크 헬퍼 메서드
    private boolean isUserLoggedIn() {
        return (loginMember != null && loginMember.isLogin()) ||
                (loginBusiness != null && loginBusiness.isLogin()) ||
                (loginAdmin != null && loginAdmin.isAdmin_login());
    }

    // === REST API 엔드포인트 ===

    // 사용자의 채팅방 목록 조회
    @GetMapping("/rooms")
    @ResponseBody
    public ResponseEntity<List<ChatRoomBean>> getUserChatRooms() {
        if (!isUserLoggedIn()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> userInfo = getLoggedInUserInfo();
        String userId = userInfo.get("userId");

        List<ChatRoomBean> rooms = chatService.getChatRoomsByUserId(userId);
        return ResponseEntity.ok(rooms);
    }

    // 채팅방 상세 정보 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChatRoomDetail(@PathVariable Long roomId) {
        if (!isUserLoggedIn()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> userInfo = getLoggedInUserInfo();
        String userId = userInfo.get("userId");

        ChatRoomBean room = chatService.getChatRoomById(roomId);
        List<ChatParticipantBean> participants = chatService.getRoomParticipants(roomId);
        List<ChatMessageBean> messages = chatService.getRoomMessages(roomId, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("room", room);
        responseData.put("participants", participants);
        responseData.put("messages", messages);

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/room/create")
    @ResponseBody
    public ResponseEntity<ChatRoomBean> createChatRoom(@RequestBody Map<String, Object> request) {
        if (!isUserLoggedIn()) {
            return ResponseEntity.badRequest().build();
        }

        ChatRoomBean chatRoom = new ChatRoomBean();
        chatRoom.setRoom_name((String) request.get("roomName"));
        chatRoom.setRoom_type((String) request.get("roomType"));

        if ("CLUB".equals(chatRoom.getRoom_type()) && request.get("clubId") != null) {
            chatRoom.setClub_id(Integer.parseInt(request.get("clubId").toString()));
        } else {
            // For personal chats, set club_id to null
            chatRoom.setClub_id(null);
        }

        @SuppressWarnings("unchecked")
        List<ChatParticipantBean> participants = (List<ChatParticipantBean>) request.get("participants");

        ChatRoomBean createdRoom = chatService.createChatRoom(chatRoom, participants);
        return ResponseEntity.ok(createdRoom);
    }

    // 1:1 채팅방 생성/조회
    @PostMapping("/room/personal")
    @ResponseBody
    public ResponseEntity<ChatRoomBean> getOrCreatePersonalChatRoom(@RequestBody Map<String, String> request) {
        if (!isUserLoggedIn()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> userInfo = getLoggedInUserInfo();
        String userId1 = userInfo.get("userId");
        String userType1 = userInfo.get("userType");

        String userId2 = request.get("targetUserId");
        String userType2 = request.get("targetUserType");

        // 채팅방 생성 결과 객체 받기
        ChatRoomCreationResult result = chatService.getOrCreatePersonalChatRoomWithResult(userId1, userType1, userId2, userType2);
        ChatRoomBean room = result.getRoom();

        // 새로운 채팅방이 생성된 경우 또는 새 참가자가 추가된 경우 WebSocket으로 알림 전송
        if (result.isNewRoom() || result.isNewParticipant()) {
            // 채팅방 정보에 최근 메시지와 미읽은 메시지 수 정보 추가
            room.setLastMessage("새로운 대화가 시작되었습니다.");
            room.setLastMessageTime(LocalDateTime.now());
            room.setUnreadCount(0);

            // 상대방에게 알림 전송
            messagingTemplate.convertAndSendToUser(
                    userId2,
                    "/queue/new-room",
                    room
            );
        }

        return ResponseEntity.ok(room);
    }

    // 클럽 채팅방 생성/조회 - Member 및 Admin만 가능
    @PostMapping("/room/club")
    @ResponseBody
    public ResponseEntity<ChatRoomBean> getOrCreateClubChatRoom(@RequestBody Map<String, Object> request) {
        // 비즈니스 계정은 클럽 채팅에 참여할 수 없음
        if ((loginMember == null || !loginMember.isLogin()) &&
                (loginAdmin == null || !loginAdmin.isAdmin_login())) {
            return ResponseEntity.badRequest().build();
        }

        int clubId = Integer.parseInt(request.get("clubId").toString());
        String userId;
        String userType;

        if (loginMember != null && loginMember.isLogin()) {
            userId = loginMember.getMember_id();
            userType = "MEMBER";
        } else {
            userId = loginAdmin.getAdmin_id();
            userType = "ADMIN";
        }

        // 채팅방 생성 결과 객체 받기
        ChatRoomCreationResult result = chatService.getOrCreateClubChatRoomWithResult(clubId, userId, userType);
        ChatRoomBean room = result.getRoom();

        // 새로운 채팅방이 생성된 경우 WebSocket으로 알림 전송
        if (result.isNewRoom()) {
            // 채팅방 정보에 최근 메시지와 미읽은 메시지 수 정보 추가
            room.setLastMessage("새로운 동호회 채팅방이 개설되었습니다.");
            room.setLastMessageTime(LocalDateTime.now());
            room.setUnreadCount(0);

            // 클럽의 모든 참여자들에게 채팅방 정보 전송
            List<ChatParticipantBean> participants = chatService.getRoomParticipants((long)room.getRoom_id());
            for (ChatParticipantBean participant : participants) {
                if (!participant.getUser_id().equals(userId)) { // 현재 사용자는 제외
                    messagingTemplate.convertAndSendToUser(
                            participant.getUser_id(),
                            "/queue/new-room",
                            room
                    );
                }
            }
        }
        // 새 참가자가 추가된 경우 해당 참가자에게만 알림
        else if (result.isNewParticipant()) {
            // 채팅방 정보에 미읽은 메시지 수 추가
            int unreadCount = chatService.getUnreadMessageCount((long)room.getRoom_id(), userId);
            room.setUnreadCount(unreadCount);

            // 최근 메시지 정보 추가 (기존에 있는 정보 활용)
        }

        return ResponseEntity.ok(room);
    }

    // 채팅방 나가기
    @PostMapping("/room/{roomId}/leave")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> leaveRoom(@PathVariable Long roomId) {
        if (!isUserLoggedIn()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> userInfo = getLoggedInUserInfo();
        String userId = userInfo.get("userId");

        int result = chatService.leaveRoom(roomId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result > 0);

        return ResponseEntity.ok(response);
    }

    // 메시지 검색
    @GetMapping("/room/{roomId}/search")
    @ResponseBody
    public ResponseEntity<List<ChatMessageBean>> searchMessages(@PathVariable Long roomId,
                                                                @RequestParam String keyword) {
        if (!isUserLoggedIn()) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatMessageBean> messages = chatService.searchMessages(roomId, keyword);
        return ResponseEntity.ok(messages);
    }

    // 사용자 검색
    @GetMapping("/users/search")
    @ResponseBody
    public ResponseEntity<List<ChatUserBean>> searchUsers(@RequestParam String keyword) {
        if (!isUserLoggedIn()) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatUserBean> users = chatService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    // 클럽 검색 (멤버와 관리자만 가능)
    @GetMapping("/clubs/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchClubs(@RequestParam String keyword) {
        if ((loginMember == null || !loginMember.isLogin()) &&
                (loginAdmin == null || !loginAdmin.isAdmin_login())) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, Object>> clubs = chatService.searchClubs(keyword);
        return ResponseEntity.ok(clubs);
    }

    // 클럽 멤버 조회 (멤버와 관리자만 가능)
    @GetMapping("/club/{clubId}/members")
    @ResponseBody
    public ResponseEntity<List<ChatUserBean>> getClubMembers(@PathVariable Long clubId) {
        if ((loginMember == null || !loginMember.isLogin()) &&
                (loginAdmin == null || !loginAdmin.isAdmin_login())) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatUserBean> members = chatService.getClubMembers(clubId);
        return ResponseEntity.ok(members);
    }

    /**
     * API endpoint to get unread message counts for AJAX updates
     */
    @GetMapping("/unread-counts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCounts() {
        if (!isUserLoggedIn()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> userInfo = getLoggedInUserInfo();
        String userId = userInfo.get("userId");

        Map<String, Object> response = new HashMap<>();

        // Get all rooms
        List<ChatRoomBean> allRooms = chatService.getChatRoomsByUserId(userId);

        // Separate rooms by type
        List<ChatRoomBean> personalRooms = allRooms.stream()
                .filter(room -> room.getClub_id() == null)
                .collect(Collectors.toList());

        List<ChatRoomBean> clubRooms = allRooms.stream()
                .filter(room -> room.getClub_id() != null)
                .collect(Collectors.toList());

        if (!personalRooms.isEmpty()) {
            for (ChatRoomBean room : personalRooms) {
                int personalunreadcount = chatService.getUnreadMessageCount((long) room.getRoom_id(), userId);
                room.setUnreadCount(personalunreadcount);
            }
        }

        if (!clubRooms.isEmpty()) {
            for (ChatRoomBean room : clubRooms) {
                int clubunreadcount = chatService.getUnreadMessageCount((long) room.getRoom_id(), userId);
                room.setUnreadCount(clubunreadcount);
            }
        }

        // Calculate unread counts
        int personalUnread = personalRooms.stream()
                .mapToInt(ChatRoomBean::getUnreadCount)
                .sum();

        int clubUnread = clubRooms.stream()
                .mapToInt(ChatRoomBean::getUnreadCount)
                .sum();

        int totalUnread = personalUnread + clubUnread;

        // Create map of room IDs to their unread counts
        Map<String, Integer> roomUnreadCounts = new HashMap<>();
        allRooms.forEach(room -> {
            if (room.getUnreadCount() > 0) {
                roomUnreadCounts.put(String.valueOf(room.getRoom_id()), room.getUnreadCount());
            }
        });

        // Add all to response
        response.put("personalUnread", personalUnread);
        response.put("clubUnread", clubUnread);
        response.put("totalUnread", totalUnread);
        response.put("roomUnreadCounts", roomUnreadCounts);

        return ResponseEntity.ok(response);
    }

    // === WebSocket 메서드 ===

    // 메시지 전송
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageBean message) {
        chatService.sendMessage(message);
    }

    @MessageMapping("/chat.markAsRead/{roomId}")
    public void markAsRead(@DestinationVariable Long roomId,
                           @Payload Map<String, Object> payload,
                           SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        String userId = principal != null ? principal.getName() : (String) payload.get("userId");
        Long messageId = Long.valueOf(payload.get("messageId").toString());

        // 읽음 처리
        chatService.markAsRead(roomId, userId, messageId);

        // 메시지 정보 가져오기
        ChatMessageBean message = chatService.getMessageById(messageId);

        if (message != null) {
            // 모든 읽음 상태 정보 가져오기
            List<ChatReadReceiptBean> readReceipts = chatService.getReadReceiptsByMessageId(messageId);
            message.setReadCount(readReceipts.size());

            System.out.println("메시지 ID: " + messageId + ", 읽은 사용자 수: " + readReceipts.size());

            // 룸의 모든 참여자에게 읽음 상태 업데이트 브로드캐스트
            messagingTemplate.convertAndSend("/topic/read-status/" + roomId, message);

            // 원래 발신자에게도 개인 알림
            if (!message.getSenderId().equals(userId)) {
                messagingTemplate.convertAndSendToUser(
                        message.getSenderId(),
                        "/queue/read-receipts",
                        message
                );
            }
        }
    }

    @MessageMapping("/chat.getReadStatus/{roomId}")
    public void getReadStatus(@DestinationVariable Long roomId,
                              @Payload Map<String, Object> payload) {
        System.out.println("getReadStatus 호출됨: " + payload);

        // messageId를 얻는 과정에서 에러가 있는지 확인
        try {
            Long messageId = Long.valueOf(payload.get("messageId").toString());
            System.out.println("messageId 변환 성공: " + messageId);

            // 메시지 정보 가져오기
            ChatMessageBean message = chatService.getMessageById(messageId);

            if (message != null) {
                System.out.println("메시지 찾음: " + message.getMessageId());

                // 모든 읽음 상태 정보 가져오기
                List<ChatReadReceiptBean> readReceipts = chatService.getReadReceiptsByMessageId(messageId);
                message.setReadCount(readReceipts.size());

                System.out.println("메시지 읽음 상태: " + message.getReadCount() + ", 전송 경로: /topic/read-status/" + roomId);

                // 룸의 모든 참여자에게 읽음 상태 업데이트 브로드캐스트
                messagingTemplate.convertAndSend("/topic/read-status/" + roomId, message);
                System.out.println("읽음 상태 브로드캐스트 완료");
            } else {
                System.out.println("메시지를 찾을 수 없음: " + messageId);
            }
        } catch (Exception e) {
            System.out.println("getReadStatus 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 타이핑 상태 전송
    @MessageMapping("/chat.typing.{roomId}")
    public void typingStatus(@DestinationVariable Long roomId,
                             @Payload Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        boolean isTyping = (boolean) payload.get("isTyping");

        // 타이핑 상태를 방의 모든 참여자에게 브로드캐스트
        List<ChatParticipantBean> participants = chatService.getRoomParticipants(roomId);

        for (ChatParticipantBean participant : participants) {
            if (!participant.getUser_id().equals(userId)) {
                Map<String, Object> typingStatus = new HashMap<>();
                typingStatus.put("userId", userId);
                typingStatus.put("isTyping", isTyping);

                // 참여자의 닉네임 정보 추가
                for (ChatParticipantBean p : participants) {
                    if (p.getUser_id().equals(userId)) {
                        typingStatus.put("userNickname", p.getUser_nickname());
                        break;
                    }
                }

                messagingTemplate.convertAndSendToUser(
                        participant.getUser_id(),
                        "/queue/typing." + roomId,
                        typingStatus
                );
            }
        }
    }

    // === 뷰 컨트롤러 메서드 ===

    // 채팅방 목록 뷰
    @GetMapping("/view/rooms")
    public String chatRoomList(Model model) {
        if (!isUserLoggedIn()) {
            return "redirect:/member/login";
        }

        Map<String, String> userInfo = getLoggedInUserInfo();
        String userId = userInfo.get("userId");
        String userType = userInfo.get("userType");

        // 모든 채팅방 가져오기
        List<ChatRoomBean> allRooms = chatService.getChatRoomsByUserId(userId);

        // 유형별로 분류
        List<ChatRoomBean> personalRooms = allRooms.stream()
                .filter(room -> room.getClub_id() == null)
                .collect(Collectors.toList());

        List<ChatRoomBean> clubRooms = allRooms.stream()
                .filter(room -> room.getClub_id() != null)
                .collect(Collectors.toList());

        // 개인 채팅방에 대해 상대방 정보 추가
        Map<Long, ChatParticipantBean> otherParticipants = new HashMap<>();

        if (!personalRooms.isEmpty()) {
            for (ChatRoomBean room : personalRooms) {
                // 읽지 않은 메시지 카운트 설정
                int personalunreadcount = chatService.getUnreadMessageCount((long) room.getRoom_id(), userId);
                room.setUnreadCount(personalunreadcount);

                // 상대방 정보 가져오기
                ChatParticipantBean otherParticipant = chatService.getOtherParticipant(room.getRoom_id(), userId);
                if (otherParticipant != null) {
                    otherParticipants.put((long) room.getRoom_id(), otherParticipant);
                    System.out.println("상대방 정보: " + otherParticipant.getUserProfile());
                }
            }
        }

        for (Map.Entry<Long, ChatParticipantBean> entry : otherParticipants.entrySet()) {
            System.out.println("roomId: " + entry.getKey() + ", userProfile: " + entry.getValue().getUserProfile());
        }

        if (!clubRooms.isEmpty()) {
            for (ChatRoomBean room : clubRooms) {
                int clubunreadcount = chatService.getUnreadMessageCount((long) room.getRoom_id(), userId);
                room.setUnreadCount(clubunreadcount);
            }
        }

        // 읽지 않은 메시지 수 계산
        int personalUnread = personalRooms.stream()
                .mapToInt(ChatRoomBean::getUnreadCount)
                .sum();

        int clubUnread = clubRooms.stream()
                .mapToInt(ChatRoomBean::getUnreadCount)
                .sum();

        int totalUnread = personalUnread + clubUnread;

        Map<Integer, ChatParticipantBean> convertedMap = new HashMap<>();
        for (Map.Entry<Long, ChatParticipantBean> entry : otherParticipants.entrySet()) {
            convertedMap.put(entry.getKey().intValue(), entry.getValue());
        }

        // 상대방 정보 모델에 추가
        model.addAttribute("otherParticipants", convertedMap);

        // 모든 방에 unreadCount가 제대로 설정되어 있는지 확인
        for (ChatRoomBean room : allRooms) {
            if (room.getUnreadCount() <= 0) {
                int unreadCount = chatService.getUnreadMessageCount((long) room.getRoom_id(), userId);
                room.setUnreadCount(unreadCount);
                System.out.println("Room " + room.getRoom_id() + " unread: " + unreadCount);
            }
        }

        // 모델에 모든 정보 추가
        model.addAttribute("rooms", allRooms);
        model.addAttribute("personalRooms", personalRooms);
        model.addAttribute("clubRooms", clubRooms);
        model.addAttribute("personalUnread", personalUnread);
        model.addAttribute("clubUnread", clubUnread);
        model.addAttribute("totalUnread", totalUnread);

        // 현재 로그인한 사용자 정보 추가
        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUserType", userType);

        return "chat/roomList";
    }

    @GetMapping("/view/room/{roomId}")
    public String chatRoomDetail(@PathVariable Long roomId, Model model) {
        if (!isUserLoggedIn()) {
            return "redirect:/member/login";
        }

        Map<String, String> userInfo = getLoggedInUserInfo();
        String userId = userInfo.get("userId");
        String userType = userInfo.get("userType");

        ChatRoomBean room = chatService.getChatRoomById(roomId);
        if (room == null) {
            return "redirect:/chat/view/rooms";
        }

        // 해당 채팅방의 모든 메시지를 읽음 처리
        chatService.markAllMessagesAsRead(roomId, userId);

        // 현재 로그인한 사용자 정보를 모델에 추가
        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUserType", userType);

        // 모델에 정보 추가
        model.addAttribute("room", room);
        model.addAttribute("participants", chatService.getRoomParticipants(roomId));

        // 각 사용자 유형에 따라 로그인 정보 전달
        // 반드시 null 체크 후 모델에 추가
        if (loginMember != null) {
            model.addAttribute("loginMember", loginMember);
        }

        if (loginBusiness != null) {
            model.addAttribute("loginBusiness", loginBusiness);
        }

        if (loginAdmin != null) {
            model.addAttribute("loginAdmin", loginAdmin);
        }

        return "chat/roomDetail";
    }

    // 새 채팅 시작 페이지 (사용자 검색)
    @GetMapping("/view/new")
    public String newChat(Model model) {
        if (!isUserLoggedIn()) {
            return "redirect:/member/login";
        }

        Map<String, String> userInfo = getLoggedInUserInfo();
        String userId = userInfo.get("userId");
        String userType = userInfo.get("userType");

        // 현재 로그인한 사용자 정보 추가
        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUserType", userType);

        return "chat/newChat";
    }

    // ChatController.java - markAllMessagesAsRead 메소드 수정
    @MessageMapping("/chat.markAllAsRead/{roomId}")
    public void markAllAsRead(@DestinationVariable Long roomId,
                              @Payload Map<String, Object> payload) {
        String userId = (String) payload.get("userId");

        // 모든 메시지 읽음 처리
        chatService.markAllMessagesAsRead(roomId, userId);

        // 채팅방의 모든 참여자에게 읽음 상태 업데이트 브로드캐스트
        List<ChatMessageBean> messages = chatService.getRoomMessages(roomId, userId);
        for (ChatMessageBean message : messages) {
            // 발신자가 현재 사용자가 아닌 메시지만 브로드캐스트
            if (!message.getSenderId().equals(userId)) {
                // 읽음 상태 정보 갱신
                List<ChatReadReceiptBean> readReceipts = chatService.getReadReceiptsByMessageId(message.getMessageId());
                message.setReadCount(readReceipts.size());

                messagingTemplate.convertAndSend("/topic/read-status/" + roomId, message);

                // 원래 발신자에게도 개인 알림
                messagingTemplate.convertAndSendToUser(
                        message.getSenderId(),
                        "/queue/read-receipts",
                        message
                );
            }
        }
    }

    @GetMapping("/new/ask")
    public String newAsk(Model model,
                         @RequestParam("item_id") String itemId,
                         @RequestParam("business_id") String businessId) {

        BusinessItemBean item = itemService.getItemById(itemId);
        if (!isUserLoggedIn()) {
            return "redirect:/member/login";
        }

        String itemTitle = (item != null) ? item.getItem_title() : "상품 문의";

        // 2. 채팅방 생성 또는 기존 채팅방 가져오기
        ChatRoomCreationResult result = chatService.getOrCreatePersonalChatRoomWithResult(loginMember.getMember_id(), "MEMBER", businessId, "BUSINESS");
        ChatRoomBean room = result.getRoom();

        // 새 채팅방 또는 새 참가자인 경우 알림 전송
        if (result.isNewRoom() || result.isNewParticipant()) {
            room.setLastMessage("새로운 대화가 시작되었습니다.");
            room.setLastMessageTime(LocalDateTime.now());
            room.setUnreadCount(0);

            // 비즈니스 사용자에게 새 채팅방 알림
            messagingTemplate.convertAndSendToUser(
                    businessId,
                    "/queue/new-room",
                    room
            );
        }

        int room_id = room.getRoom_id();
        long roomId = (long) room_id;

        // 항상 메시지 생성 및 전송 (새 채팅방이든 기존 채팅방이든)
        String firstMessage = itemTitle + "에 대해서 문의할게요";
        ChatMessageBean message = new ChatMessageBean();
        message.setRoomId(roomId);
        message.setSenderId(loginMember.getMember_id());
        message.setSenderType("MEMBER");
        message.setMessageContent(firstMessage);
        message.setMessageType("TEXT");
        message.setSenderNickname(loginMember.getMember_nickname());
        message.setSenderProfile(loginMember.getMember_profile());

        // 메시지 저장 및 발송
        chatService.sendMessage(message);

        // 모든 경우에 채팅방으로 리다이렉트
        return "redirect:/chat/view/room/" + roomId;
    }

    @GetMapping("club/ask")
    public String clubask(@RequestParam("club_id") int club_id, Model model) {

        if (!isUserLoggedIn()) {
            return "redirect:/member/login";
        }

        String masterID = clubMemberService.getMasterMember(club_id);

        ChatRoomCreationResult result = chatService.getOrCreatePersonalChatRoomWithResult(loginMember.getMember_id(), "MEMBER", masterID, "MEMBER");
        ChatRoomBean room = result.getRoom();

        if (result.isNewRoom() || result.isNewParticipant()) {
            room.setLastMessage("새로운 대화가 시작되었습니다.");
            room.setLastMessageTime(LocalDateTime.now());
            room.setUnreadCount(0);

            // 비즈니스 사용자에게 새 채팅방 알림
            messagingTemplate.convertAndSendToUser(
                    masterID,
                    "/queue/new-room",
                    room
            );
        }

        int room_id = room.getRoom_id();
        long roomId = (long) room_id;

        ClubBean oneclub = clubService.oneClubInfo(club_id);


        String firstMessage = oneclub.getClub_name() + "에 대해서 문의할게요";
        ChatMessageBean message = new ChatMessageBean();
        message.setRoomId(roomId);
        message.setSenderId(loginMember.getMember_id());
        message.setSenderType("MEMBER");
        message.setMessageContent(firstMessage);
        message.setMessageType("TEXT");
        message.setSenderNickname(loginMember.getMember_nickname());
        message.setSenderProfile(loginMember.getMember_profile());

        // 메시지 저장 및 발송
        chatService.sendMessage(message);

        return "redirect:/chat/view/room/" + roomId;
    }




}