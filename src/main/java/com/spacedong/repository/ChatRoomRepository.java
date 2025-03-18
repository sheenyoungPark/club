package com.spacedong.repository;

import com.spacedong.documents.ChatRoomDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoomDocument, String> {
    // 사용자가 참여한 모든 채팅방 조회 (최근 메시지순)
    List<ChatRoomDocument> findByParticipantsContainingOrderByLastMessageTimeDesc(String userId);

    // 특정 유형의 채팅방 조회
    List<ChatRoomDocument> findByRoomTypeAndParticipantsContainingOrderByLastMessageTimeDesc(String roomType, String userId);

    // 클럽 ID로 채팅방 조회
    Optional<ChatRoomDocument> findByRoomTypeAndClubId(String roomType, Integer clubId);

    // 개인 채팅방 찾기 (정확히 같은 참여자들로 구성된)
    @Query("{ 'roomType': 'PERSONAL', 'participants': { $all: ?0 }, 'participants': { $size: ?1 } }")
    Optional<ChatRoomDocument> findPersonalChatRoomByParticipants(List<String> participants, int participantCount);
}
