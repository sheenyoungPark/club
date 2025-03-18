// src/main/java/com/spacedong/service/ChatService.java
package com.spacedong.service;

import com.spacedong.beans.ChatMessage;
import com.spacedong.beans.ChatRoom;
import com.spacedong.documents.ChatMessageDocument;
import com.spacedong.documents.ChatReadDocument;
import com.spacedong.documents.ChatRoomDocument;
import com.spacedong.repository.ChatMessageRepository;
import com.spacedong.repository.ChatReadRepository;
import com.spacedong.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatReadRepository chatReadRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 전송
     * @param message 전송할 채팅 메시지
     * @return 저장된 메시지
     */
    public ChatMessage sendMessage(ChatMessage message) {
        // 타임스탬프 설정
        message.setTimestamp(LocalDateTime.now());

        // 채팅방 타입 확인
        Optional<ChatRoomDocument> roomOpt = chatRoomRepository.findById(message.getChatRoomId());
        if (roomOpt.isEmpty()) {
            throw new RuntimeException("채팅방을 찾을 수 없습니다.");
        }

        ChatRoomDocument room = roomOpt.get();

        // 클럽 채팅은 바로 MongoDB에 저장, 개인 채팅은 Redis에 임시 저장
        if ("CLUB".equals(room.getRoomType())) {
            saveChatMessageToMongoDB(message);
        } else {
            saveMessageToRedis(message);
        }

        // 채팅방 최신 메시지 정보 업데이트
        updateChatRoomLastMessage(message);

        // WebSocket으로 메시지 전송
        if ("CLUB".equals(room.getRoomType())) {
            // 클럽 채팅은 토픽으로 전송
            messagingTemplate.convertAndSend("/topic/club/" + message.getChatRoomId(), message);
        } else {
            // 개인 채팅은 개별 사용자에게 전송
            for (String participantId : room.getParticipants()) {
                messagingTemplate.convertAndSendToUser(
                        participantId,
                        "/queue/messages",
                        message
                );
            }
        }

        return message;
    }

    /**
     * Redis에 메시지 임시 저장
     */
    private void saveMessageToRedis(ChatMessage message) {
        String redisKey = "chat:messages:" + message.getChatRoomId();
        redisTemplate.opsForList().rightPush(redisKey, message);
    }

    /**
     * MongoDB에 채팅 메시지 저장
     */
    private ChatMessageDocument saveChatMessageToMongoDB(ChatMessage message) {
        ChatMessageDocument document = ChatMessageDocument.builder()
                .id(UUID.randomUUID().toString())
                .chatRoomId(message.getChatRoomId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderType(message.getSenderType())
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .attachmentUrl(message.getAttachmentUrl())
                .timestamp(message.getTimestamp())
                .read(message.isRead())
                .build();

        return chatMessageRepository.save(document);
    }

    /**
     * 채팅방의 마지막 메시지 정보 업데이트
     */
    private void updateChatRoomLastMessage(ChatMessage message) {
        Optional<ChatRoomDocument> roomOpt = chatRoomRepository.findById(message.getChatRoomId());

        if (roomOpt.isPresent()) {
            ChatRoomDocument room = roomOpt.get();
            room.setLastMessage(message.getMessage());
            room.setLastMessageTime(message.getTimestamp());
            chatRoomRepository.save(room);
        }
    }

    /**
     * 10분마다 Redis의 임시 메시지를 MongoDB로 이동
     */
    @Scheduled(fixedRate = 600000) // 10분마다
    public void moveMessagesFromRedisToMongoDB() {
        Set<String> keys = redisTemplate.keys("chat:messages:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        log.info("Scheduled job: Moving messages from Redis to MongoDB. Keys found: {}", keys.size());

        for (String key : keys) {
            Long size = redisTemplate.opsForList().size(key);
            if (size == null || size == 0) {
                continue;
            }

            List<Object> messages = redisTemplate.opsForList().range(key, 0, size - 1);
            for (Object obj : messages) {
                if (obj instanceof ChatMessage) {
                    saveChatMessageToMongoDB((ChatMessage) obj);
                }
            }

            // Redis에서 처리된 메시지 삭제
            redisTemplate.delete(key);
        }
    }

    /**
     * 새 채팅방 생성
     */
    public ChatRoom createChatRoom(String roomName, String roomType, List<String> participants, Integer clubId) {
        if (participants.size() < 2) {
            throw new IllegalArgumentException("채팅방에는 최소 2명의 참여자가 필요합니다.");
        }

        // 개인 채팅방 중복 확인 (같은 참여자들의 채팅방이 이미 있는지 확인)
        if ("PERSONAL".equals(roomType) && participants.size() == 2) {
            Optional<ChatRoomDocument> existingRoom =
                    chatRoomRepository.findPersonalChatRoomByParticipants(participants, participants.size());

            if (existingRoom.isPresent()) {
                return convertToChatRoom(existingRoom.get());
            }
        }

        // 클럽 채팅방 중복 확인
        if ("CLUB".equals(roomType) && clubId != null) {
            Optional<ChatRoomDocument> existingRoom =
                    chatRoomRepository.findByRoomTypeAndClubId(roomType, clubId);

            if (existingRoom.isPresent()) {
                return convertToChatRoom(existingRoom.get());
            }
        }

        // 새 채팅방 생성
        ChatRoomDocument roomDoc = ChatRoomDocument.builder()
                .id(UUID.randomUUID().toString())
                .roomName(roomName)
                .roomType(roomType)
                .participants(participants)
                .createdAt(LocalDateTime.now())
                .lastMessageTime(LocalDateTime.now())
                .clubId(clubId)
                .build();

        ChatRoomDocument savedRoom = chatRoomRepository.save(roomDoc);

        return convertToChatRoom(savedRoom);
    }

    /**
     * 특정 사용자의 모든 채팅방 목록 조회
     */
    public List<ChatRoom> getChatRoomsByUserId(String userId) {
        List<ChatRoomDocument> rooms =
                chatRoomRepository.findByParticipantsContainingOrderByLastMessageTimeDesc(userId);

        return rooms.stream()
                .map(room -> {
                    ChatRoom chatRoom = convertToChatRoom(room);

                    // 안 읽은 메시지 수 계산
                    long unreadCount = chatMessageRepository.countByChatRoomIdAndReadFalseAndSenderIdNot(
                            room.getId(), userId);
                    chatRoom.setUnreadCount((int) unreadCount);

                    return chatRoom;
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 특정 유형 채팅방 목록 조회
     */
    public List<ChatRoom> getChatRoomsByType(String userId, String roomType) {
        List<ChatRoomDocument> rooms =
                chatRoomRepository.findByRoomTypeAndParticipantsContainingOrderByLastMessageTimeDesc(roomType, userId);

        return rooms.stream()
                .map(room -> {
                    ChatRoom chatRoom = convertToChatRoom(room);

                    // 안 읽은 메시지 수 계산
                    long unreadCount = chatMessageRepository.countByChatRoomIdAndReadFalseAndSenderIdNot(
                            room.getId(), userId);
                    chatRoom.setUnreadCount((int) unreadCount);

                    return chatRoom;
                })
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 ID로 채팅방 조회
     */
    public ChatRoom getChatRoomById(String roomId, String userId) {
        Optional<ChatRoomDocument> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return null;
        }

        ChatRoomDocument room = roomOpt.get();
        ChatRoom chatRoom = convertToChatRoom(room);

        // 안 읽은 메시지 수 계산
        long unreadCount = chatMessageRepository.countByChatRoomIdAndReadFalseAndSenderIdNot(roomId, userId);
        chatRoom.setUnreadCount((int) unreadCount);

        return chatRoom;
    }

    /**
     * 채팅방 참여자 목록 업데이트
     */
    public void updateChatRoomParticipants(String roomId, List<String> participants) {
        Optional<ChatRoomDocument> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            ChatRoomDocument room = roomOpt.get();
            room.setParticipants(participants);
            chatRoomRepository.save(room);
        }
    }

    /**
     * 특정 채팅방의 메시지 이력 조회
     */
    public List<ChatMessage> getChatHistory(String roomId) {
        // MongoDB에서 저장된 메시지 조회
        List<ChatMessageDocument> mongoMessages =
                chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);

        List<ChatMessage> messages = mongoMessages.stream()
                .map(this::convertToChatMessage)
                .collect(Collectors.toList());

        // 개인 채팅방인 경우 Redis의 임시 메시지도 조회
        Optional<ChatRoomDocument> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isPresent() && "PERSONAL".equals(roomOpt.get().getRoomType())) {
            String redisKey = "chat:messages:" + roomId;
            Long size = redisTemplate.opsForList().size(redisKey);

            if (size != null && size > 0) {
                List<Object> redisMessages = redisTemplate.opsForList().range(redisKey, 0, size - 1);

                for (Object obj : redisMessages) {
                    if (obj instanceof ChatMessage) {
                        messages.add((ChatMessage) obj);
                    }
                }
            }
        }

        // 시간순 정렬
        messages.sort(Comparator.comparing(ChatMessage::getTimestamp));

        return messages;
    }

    /**
     * 메시지 읽음 상태 업데이트
     */
    public void markMessagesAsRead(String roomId, String userId) {
        // 읽지 않은 메시지 찾기
        List<ChatMessageDocument> unreadMessages = chatMessageRepository.findUnreadMessages(roomId, userId);

        // 메시지 읽음 상태 업데이트
        for (ChatMessageDocument message : unreadMessages) {
            // 메시지 읽음 상태 변경
            message.setRead(true);
            chatMessageRepository.save(message);

            // 읽음 기록 저장
            ChatReadDocument readDoc = ChatReadDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .messageId(message.getId())
                    .readerId(userId)
                    .readAt(LocalDateTime.now())
                    .build();

            // 이미 있는지 확인 후 저장
            Optional<ChatReadDocument> existingRead =
                    chatReadRepository.findByMessageIdAndReaderId(message.getId(), userId);

            if (existingRead.isEmpty()) {
                chatReadRepository.save(readDoc);
            }
        }
    }

    /**
     * 사용자의 안 읽은 메시지 총 개수 조회
     */
    public int getUnreadMessageCount(String userId) {
        // 사용자가 참여한 모든 채팅방에서 안 읽은 메시지 개수의 총합 계산
        List<ChatRoom> chatRooms = getChatRoomsByUserId(userId);
        return chatRooms.stream()
                .mapToInt(ChatRoom::getUnreadCount)
                .sum();
    }

    /**
     * 클럽 ID로 채팅방 조회
     */
    public ChatRoom getClubChatRoomByClubId(Integer clubId) {
        Optional<ChatRoomDocument> roomOpt = chatRoomRepository.findByRoomTypeAndClubId("CLUB", clubId);
        return roomOpt.map(this::convertToChatRoom).orElse(null);
    }

    /**
     * 클럽 ID로 채팅방 조회 (없으면 생성)
     */
    public ChatRoom getOrCreateClubChatRoom(Integer clubId, String clubName, List<String> members) {
        Optional<ChatRoomDocument> roomOpt =
                chatRoomRepository.findByRoomTypeAndClubId("CLUB", clubId);

        if (roomOpt.isPresent()) {
            return convertToChatRoom(roomOpt.get());
        } else {
            return createChatRoom(clubName + " 채팅방", "CLUB", members, clubId);
        }
    }

    /**
     * Document -> Bean 변환
     */
    private ChatRoom convertToChatRoom(ChatRoomDocument doc) {
        return ChatRoom.builder()
                .id(doc.getId())
                .roomName(doc.getRoomName())
                .roomType(doc.getRoomType())
                .participants(doc.getParticipants())
                .createdAt(doc.getCreatedAt())
                .lastMessageTime(doc.getLastMessageTime())
                .lastMessage(doc.getLastMessage())
                .clubId(doc.getClubId())
                .build();
    }

    /**
     * Document -> Bean 변환
     */
    private ChatMessage convertToChatMessage(ChatMessageDocument doc) {
        return ChatMessage.builder()
                .id(doc.getId())
                .chatRoomId(doc.getChatRoomId())
                .senderId(doc.getSenderId())
                .senderName(doc.getSenderName())
                .senderType(doc.getSenderType())
                .message(doc.getMessage())
                .messageType(doc.getMessageType())
                .attachmentUrl(doc.getAttachmentUrl())
                .timestamp(doc.getTimestamp())
                .read(doc.isRead())
                .build();
    }
}