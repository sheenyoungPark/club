package com.spacedong.controller;

import com.spacedong.beans.ChatMessage;
import com.spacedong.beans.ChatRoom;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.ChatService;
import com.spacedong.service.ClubChatService;
import com.spacedong.service.MemberService;
import com.spacedong.service.UserSearchService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final ClubChatService clubChatService;
    private final MemberService memberService;
    private final UserSearchService userSearchService;
    private final SimpMessagingTemplate messagingTemplate;

    @Resource(name = "loginMember")
    private MemberBean loginMember;

    /**
     * 채팅 페이지 (HTML 페이지 반환)
     */
    @GetMapping("/view")
    public String chatView(Model model) {
        // 로그인 확인
        if (!loginMember.isLogin()) {
            return "redirect:/member/login";
        }

        // 사용자의 채팅방 목록 조회
        List<ChatRoom> chatRooms = chatService.getChatRoomsByUserId(loginMember.getMember_id());
        model.addAttribute("chatRooms", chatRooms);

        return "chat/chatView";
    }

    /**
     * 채팅방 목록 조회 API
     */
    @GetMapping("/api/rooms")
    @ResponseBody
    public ResponseEntity<List<ChatRoom>> getChatRooms() {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        List<ChatRoom> chatRooms = chatService.getChatRoomsByUserId(loginMember.getMember_id());
        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 채팅방 타입별 목록 조회 API
     */
    @GetMapping("/api/rooms/type/{roomType}")
    @ResponseBody
    public ResponseEntity<List<ChatRoom>> getChatRoomsByType(@PathVariable String roomType) {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        List<ChatRoom> chatRooms = chatService.getChatRoomsByType(loginMember.getMember_id(), roomType);
        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 특정 채팅방 정보 조회 API
     */
    @GetMapping("/api/room/{roomId}")
    @ResponseBody
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable String roomId) {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        ChatRoom chatRoom = chatService.getChatRoomById(roomId, loginMember.getMember_id());
        if (chatRoom == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 채팅 메시지 이력 조회 API
     */
    @GetMapping("/api/history/{roomId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String roomId) {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        // 메시지 이력 조회
        List<ChatMessage> messages = chatService.getChatHistory(roomId);

        // 메시지 읽음 상태 업데이트
        chatService.markMessagesAsRead(roomId, loginMember.getMember_id());

        return ResponseEntity.ok(messages);
    }

    /**
     * 안 읽은 메시지 수 조회 API
     */
    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        int unreadCount = chatService.getUnreadMessageCount(loginMember.getMember_id());

        Map<String, Object> response = new HashMap<>();
        response.put("count", unreadCount);

        return ResponseEntity.ok(response);
    }

    /**
     * 개인 채팅방 생성 API
     */
    @PostMapping("/api/room/personal")
    @ResponseBody
    public ResponseEntity<ChatRoom> createPersonalChatRoom(@RequestBody Map<String, String> request) {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        String targetUserId = request.get("targetUserId");
        String targetUserName = request.get("targetUserName");

        if (targetUserId == null || targetUserName == null) {
            return ResponseEntity.badRequest().build();
        }

        // 채팅방 참여자 목록
        List<String> participants = List.of(loginMember.getMember_id(), targetUserId);

        // 채팅방 생성 (이미 존재하면 기존 채팅방 반환)
        ChatRoom chatRoom = chatService.createChatRoom(targetUserName, "PERSONAL", participants, null);

        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 클럽 채팅방 조회 API
     */
    @GetMapping("/api/room/club/{clubId}")
    @ResponseBody
    public ResponseEntity<ChatRoom> getClubChatRoom(@PathVariable Integer clubId) {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        try {
            // 클럽 채팅방 조회 또는 생성
            ChatRoom chatRoom = clubChatService.getOrCreateClubChatRoom(clubId);
            return ResponseEntity.ok(chatRoom);
        } catch (Exception e) {
            log.error("클럽 채팅방 조회 중 오류 발생", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 메시지 읽음 상태 업데이트 API
     */
    @PostMapping("/api/read/{roomId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(@PathVariable String roomId) {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        chatService.markMessagesAsRead(roomId, loginMember.getMember_id());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    /**
     * WebSocket을 통한 메시지 전송
     */
    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload ChatMessage chatMessage) {
        log.info("메시지 수신: {}", chatMessage);
        chatService.sendMessage(chatMessage);
    }

    /**
     * REST API를 통한 메시지 전송 (WebSocket을 사용할 수 없는 경우 대체 방법)
     */
    @PostMapping("/api/message")
    @ResponseBody
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        // 발신자 정보 설정
        message.setSenderId(loginMember.getMember_id());
        message.setSenderName(loginMember.getMember_nickname() != null ?
                loginMember.getMember_nickname() : loginMember.getMember_name());
        message.setSenderType("MEMBER"); // 또는 "BUSINESS", "ADMIN" 등
        message.setRead(false);

        // 메시지 전송
        ChatMessage sentMessage = chatService.sendMessage(message);

        return ResponseEntity.ok(sentMessage);
    }

    /**
     * 사용자 검색 API (채팅 상대 찾기)
     */
    @GetMapping("/api/search/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> searchUsers(@RequestParam String keyword) {
        if (!loginMember.isLogin()) {
            return ResponseEntity.status(401).build();
        }

        List<Map<String, String>> results = userSearchService.searchAllUsersByKeyword(keyword);
        return ResponseEntity.ok(results);
    }
}