package com.spacedong.repository;

import com.spacedong.documents.ChatReadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatReadRepository extends MongoRepository<ChatReadDocument, String> {
    // 특정 메시지의 읽음 상태 조회
    Optional<ChatReadDocument> findByMessageIdAndReaderId(String messageId, String readerId);

    // 특정 사용자가 읽은 메시지 ID 목록 조회
    List<ChatReadDocument> findByReaderId(String readerId);

    // 특정 메시지들에 대한 특정 사용자의 읽음 상태 조회
    List<ChatReadDocument> findByMessageIdInAndReaderId(List<String> messageIds, String readerId);
}