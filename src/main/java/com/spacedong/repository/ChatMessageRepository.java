package com.spacedong.repository;

import com.spacedong.documents.ChatMessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {
    // 특정 채팅방의 메시지 조회 (시간순 정렬)
    List<ChatMessageDocument> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);

    // 특정 채팅방의 메시지 페이지네이션 조회
    Page<ChatMessageDocument> findByChatRoomIdOrderByTimestampDesc(String chatRoomId, Pageable pageable);

    // 특정 채팅방의 안 읽은 메시지 수 조회
    long countByChatRoomIdAndReadFalseAndSenderIdNot(String chatRoomId, String userId);

    // 특정 날짜 이후의 메시지 조회
    List<ChatMessageDocument> findByChatRoomIdAndTimestampAfterOrderByTimestampAsc(String chatRoomId, LocalDateTime timestamp);

    // 특정 채팅방의 모든 메시지 읽음 처리
    @Query("{ 'chatRoomId': ?0, 'senderId': { $ne: ?1 }, 'read': false }")
    List<ChatMessageDocument> findUnreadMessages(String chatRoomId, String userId);
}