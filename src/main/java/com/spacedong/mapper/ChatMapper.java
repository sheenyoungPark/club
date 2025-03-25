package com.spacedong.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.spacedong.beans.ChatMessageBean;
import com.spacedong.beans.ChatParticipantBean;
import com.spacedong.beans.ChatReadReceiptBean;
import com.spacedong.beans.ChatRoomBean;
import com.spacedong.beans.ChatUserBean;

@Mapper
public interface ChatMapper {

    // === 채팅방 관련 쿼리 ===

    @Insert("INSERT INTO chat_room (room_id, room_name, room_type, club_id) " +
            "VALUES (chat_room_seq.NEXTVAL, #{room_name}, #{room_type}, #{club_id,jdbcType=INTEGER})")
    @Options(useGeneratedKeys = true, keyProperty = "room_id", keyColumn = "room_id")
    int createChatRoom(ChatRoomBean chatRoom);

    @Select("SELECT * FROM chat_room WHERE room_id = #{roomId}")
    ChatRoomBean getChatRoomById(@Param("roomId") Long roomId);

    // 모든 채팅방 조회 (기존 메서드)
    @Select("SELECT r.*, " +
            "(SELECT m.message_content FROM chat_message m WHERE m.room_id = r.room_id " +
            "ORDER BY m.send_time DESC FETCH FIRST 1 ROW ONLY) as last_message, " +
            "(SELECT m.send_time FROM chat_message m WHERE m.room_id = r.room_id " +
            "ORDER BY m.send_time DESC FETCH FIRST 1 ROW ONLY) as last_message_time, " +
            "(SELECT COUNT(*) FROM chat_message m WHERE m.room_id = r.room_id " +
            "AND m.send_time > (SELECT COALESCE(MAX(cr.read_time), TO_TIMESTAMP('1970-01-01', 'YYYY-MM-DD')) " +
            "FROM chat_read_receipt cr " +
            "JOIN chat_message cm ON cr.message_id = cm.message_id " +
            "WHERE cm.room_id = r.room_id AND cr.reader_id = #{userId})) as unread_count " +
            "FROM chat_room r " +
            "JOIN chat_participant p ON r.room_id = p.room_id " +
            "WHERE p.user_id = #{userId}")
    List<ChatRoomBean> getChatRoomsByUserId(@Param("userId") String userId);

    // 개인 채팅방만 조회 (새 메서드)
    @Select("SELECT r.*, " +
            "(SELECT m.message_content FROM chat_message m WHERE m.room_id = r.room_id " +
            "ORDER BY m.send_time DESC FETCH FIRST 1 ROW ONLY) as last_message, " +
            "(SELECT m.send_time FROM chat_message m WHERE m.room_id = r.room_id " +
            "ORDER BY m.send_time DESC FETCH FIRST 1 ROW ONLY) as last_message_time, " +
            "(SELECT COUNT(*) FROM chat_message m WHERE m.room_id = r.room_id " +
            "AND m.send_time > (SELECT COALESCE(MAX(cr.read_time), TO_TIMESTAMP('1970-01-01', 'YYYY-MM-DD')) " +
            "FROM chat_read_receipt cr " +
            "JOIN chat_message cm ON cr.message_id = cm.message_id " +
            "WHERE cm.room_id = r.room_id AND cr.reader_id = #{userId})) as unread_count " +
            "FROM chat_room r " +
            "JOIN chat_participant p ON r.room_id = p.room_id " +
            "WHERE p.user_id = #{userId} AND r.room_type = 'PERSONAL'")
    List<ChatRoomBean> getPersonalChatRoomsByUserId(@Param("userId") String userId);

    // 클럽 채팅방만 조회 (새 메서드 - 참여 시간 이후의 메시지만 카운트)
    @Select("SELECT r.*, " +
            "(SELECT m.message_content FROM chat_message m WHERE m.room_id = r.room_id " +
            "ORDER BY m.send_time DESC FETCH FIRST 1 ROW ONLY) as last_message, " +
            "(SELECT m.send_time FROM chat_message m WHERE m.room_id = r.room_id " +
            "ORDER BY m.send_time DESC FETCH FIRST 1 ROW ONLY) as last_message_time, " +
            "(SELECT COUNT(*) FROM chat_message m WHERE m.room_id = r.room_id " +
            "AND m.send_time > (SELECT COALESCE(MAX(cr.read_time), TO_TIMESTAMP('1970-01-01', 'YYYY-MM-DD')) " +
            "FROM chat_read_receipt cr " +
            "JOIN chat_message cm ON cr.message_id = cm.message_id " +
            "WHERE cm.room_id = r.room_id AND cr.reader_id = #{userId}) " +
            "AND m.send_time > (SELECT join_date FROM chat_participant " +
            "WHERE room_id = r.room_id AND user_id = #{userId})) as unread_count " +
            "FROM chat_room r " +
            "JOIN chat_participant p ON r.room_id = p.room_id " +
            "WHERE p.user_id = #{userId} AND r.room_type = 'CLUB'")
    List<ChatRoomBean> getClubChatRoomsByUserId(@Param("userId") String userId);


    @Select("SELECT * FROM chat_room WHERE club_id = #{clubId}")
    List<ChatRoomBean> getChatRoomsByClubId(@Param("clubId") Long clubId);

    @Select("SELECT r.* FROM chat_room r " +
            "JOIN chat_participant p1 ON r.room_id = p1.room_id " +
            "JOIN chat_participant p2 ON r.room_id = p2.room_id " +
            "WHERE r.room_type = 'PERSONAL' " +
            "AND p1.user_id = #{userId1} AND p1.user_type = #{userType1} " +
            "AND p2.user_id = #{userId2} AND p2.user_type = #{userType2}")
    ChatRoomBean getPersonalChatRoom(@Param("userId1") String userId1, @Param("userType1") String userType1,
                                     @Param("userId2") String userId2, @Param("userType2") String userType2);

    @Update("UPDATE chat_room SET room_name = #{roomName} WHERE room_id = #{roomId}")
    int updateChatRoomName(@Param("roomId") Long roomId, @Param("roomName") String roomName);

    @Delete("DELETE FROM chat_room WHERE room_id = #{roomId}")
    int deleteChatRoom(@Param("roomId") Long roomId);

    // === 채팅 참여자 관련 쿼리 ===

    @Insert("INSERT INTO chat_participant (room_id, user_id, user_type) " +
            "VALUES (#{room_id}, #{user_id}, #{user_type})")
    int addParticipant(ChatParticipantBean participant);

    @Select("SELECT cp.*, " +
            "CASE cp.user_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_nickname FROM member m WHERE m.member_id = cp.user_id) " +
            "  WHEN 'BUSINESS' THEN (SELECT b.business_name FROM business b WHERE b.business_id = cp.user_id) " +
            "  WHEN 'ADMIN' THEN (SELECT a.admin_name FROM admin a WHERE a.admin_id = cp.user_id) " +
            "END as userNickname, " +
            "CASE cp.user_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_profile FROM member m WHERE m.member_id = cp.user_id) " +
            "  ELSE NULL " +
            "END as userProfile " +
            "FROM chat_participant cp " +
            "WHERE cp.room_id = #{roomId}")
    List<ChatParticipantBean> getParticipantsByRoomId(@Param("roomId") Long roomId);

    @Select("SELECT * FROM chat_participant WHERE room_id = #{roomId} AND user_id = #{userId}")
    ChatParticipantBean getParticipant(@Param("roomId") int roomId, @Param("userId") String userId);

    @Update("UPDATE chat_participant SET last_read_msg_id = #{lastReadMsgId} " +
            "WHERE room_id = #{roomId} AND user_id = #{userId}")
    int updateLastReadMsgId(@Param("roomId") Long roomId, @Param("userId") String userId,
                            @Param("lastReadMsgId") Long lastReadMsgId);

    @Delete("DELETE FROM chat_participant WHERE room_id = #{roomId} AND user_id = #{userId}")
    int leaveRoom(@Param("roomId") Long roomId, @Param("userId") String userId);

    // 사용자 검색 (member, business, admin 통합 검색)
    @Select("SELECT m.member_id as user_id, 'MEMBER' as user_type, " +
            "m.member_nickname as nickname, m.member_profile as profile_image " +
            "FROM member m " +
            "WHERE m.member_nickname LIKE '%' || #{keyword} || '%' " +
            "OR m.member_id LIKE '%' || #{keyword} || '%' " +
            "UNION ALL " +
            "SELECT b.business_id as user_id, 'BUSINESS' as user_type, " +
            "b.business_name as nickname, NULL as profile_image " +
            "FROM business b " +
            "WHERE b.business_name LIKE '%' || #{keyword} || '%' " +
            "OR b.business_id LIKE '%' || #{keyword} || '%' " +
            "UNION ALL " +
            "SELECT a.admin_id as user_id, 'ADMIN' as user_type, " +
            "a.admin_name as nickname, NULL as profile_image " +
            "FROM admin a " +
            "WHERE a.admin_name LIKE '%' || #{keyword} || '%'" +
            "OR a.admin_id LIKE '%' || #{keyword} || '%'")
    List<ChatUserBean> searchUsers(@Param("keyword") String keyword);

    // Club 멤버 검색
    @Select("SELECT m.member_id as user_id, 'MEMBER' as user_type, " +
            "m.member_nickname as nickname, m.member_profile as profile_image " +
            "FROM club_member cm " +
            "JOIN member m ON cm.member_id = m.member_id " +
            "WHERE cm.club_id = #{clubId}")
    List<ChatUserBean> getClubMembers(@Param("clubId") Long clubId);

    // === 메시지 관련 쿼리 ===

    @Insert("<script>" +
            "INSERT INTO chat_message (message_id, room_id, sender_id, sender_type, " +
            "message_content, message_type, file_path) VALUES " +
            "(chat_message_seq.NEXTVAL, #{roomId}, #{senderId}, #{senderType}, " +
            "#{messageContent}, #{messageType}, " +
            "<if test='filePath != null'>#{filePath}</if>" +
            "<if test='filePath == null'>NULL</if>)" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "messageId", keyColumn = "message_id")
    int sendMessage(ChatMessageBean message);

    // 기존 메시지 조회 메서드
    @Select("SELECT cm.message_id as messageId, cm.room_id as roomId, " +
            "cm.sender_id as senderId, cm.sender_type as senderType, " +
            "cm.message_content as messageContent, cm.message_type as messageType, " +
            "cm.file_path as filePath, cm.send_time as sendTime, cm.read_count as readCount, " +
            "CASE cm.sender_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_nickname FROM member m WHERE m.member_id = cm.sender_id) " +
            "  WHEN 'BUSINESS' THEN (SELECT b.business_name FROM business b WHERE b.business_id = cm.sender_id) " +
            "  WHEN 'ADMIN' THEN (SELECT a.admin_name FROM admin a WHERE a.admin_id = cm.sender_id) " +
            "END as senderNickname, " +
            "CASE cm.sender_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_profile FROM member m WHERE m.member_id = cm.sender_id) " +
            "  ELSE NULL " +
            "END as senderProfile, " +
            "CASE WHEN EXISTS (SELECT 1 FROM chat_read_receipt crr WHERE crr.message_id = cm.message_id " +
            "                 AND crr.reader_id = #{currentUserId}) " +
            "THEN 1 ELSE 0 END as isRead " +
            "FROM chat_message cm " +
            "WHERE cm.room_id = #{roomId} " +
            "ORDER BY cm.send_time ASC")
    List<ChatMessageBean> getMessagesByRoomId(@Param("roomId") Long roomId, @Param("currentUserId") String currentUserId);

    // 개인 채팅방 메시지 조회 (모든 메시지)
    @Select("SELECT cm.message_id as messageId, cm.room_id as roomId, " +
            "cm.sender_id as senderId, cm.sender_type as senderType, " +
            "cm.message_content as messageContent, cm.message_type as messageType, " +
            "cm.file_path as filePath, cm.send_time as sendTime, cm.read_count as readCount, " +
            "CASE cm.sender_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_nickname FROM member m WHERE m.member_id = cm.sender_id) " +
            "  WHEN 'BUSINESS' THEN (SELECT b.business_name FROM business b WHERE b.business_id = cm.sender_id) " +
            "  WHEN 'ADMIN' THEN (SELECT a.admin_name FROM admin a WHERE a.admin_id = cm.sender_id) " +
            "END as senderNickname, " +
            "CASE cm.sender_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_profile FROM member m WHERE m.member_id = cm.sender_id) " +
            "  ELSE NULL " +
            "END as senderProfile, " +
            "CASE WHEN EXISTS (SELECT 1 FROM chat_read_receipt crr WHERE crr.message_id = cm.message_id " +
            "                 AND crr.reader_id = #{currentUserId}) " +
            "THEN 1 ELSE 0 END as isRead " +
            "FROM chat_message cm " +
            "JOIN chat_room cr ON cm.room_id = cr.room_id " +
            "WHERE cm.room_id = #{roomId} " +
            "AND cr.room_type = 'PERSONAL' " +
            "ORDER BY cm.send_time ASC")
    List<ChatMessageBean> getPersonalChatMessages(@Param("roomId") Long roomId, @Param("currentUserId") String currentUserId);

    // 클럽 채팅방 메시지 조회 (참가 시간 이후의 메시지만)
    @Select("SELECT cm.message_id as messageId, cm.room_id as roomId, " +
            "cm.sender_id as senderId, cm.sender_type as senderType, " +
            "cm.message_content as messageContent, cm.message_type as messageType, " +
            "cm.file_path as filePath, cm.send_time as sendTime, cm.read_count as readCount, " +
            "CASE cm.sender_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_nickname FROM member m WHERE m.member_id = cm.sender_id) " +
            "  WHEN 'BUSINESS' THEN (SELECT b.business_name FROM business b WHERE b.business_id = cm.sender_id) " +
            "  WHEN 'ADMIN' THEN (SELECT a.admin_name FROM admin a WHERE a.admin_id = cm.sender_id) " +
            "END as senderNickname, " +
            "CASE cm.sender_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_profile FROM member m WHERE m.member_id = cm.sender_id) " +
            "  ELSE NULL " +
            "END as senderProfile, " +
            "CASE WHEN EXISTS (SELECT 1 FROM chat_read_receipt crr WHERE crr.message_id = cm.message_id " +
            "                 AND crr.reader_id = #{currentUserId}) " +
            "THEN 1 ELSE 0 END as isRead " +
            "FROM chat_message cm " +
            "JOIN chat_room cr ON cm.room_id = cr.room_id " +
            "WHERE cm.room_id = #{roomId} " +
            "AND cr.room_type = 'CLUB' " +
            "AND cm.send_time >= (SELECT join_date FROM chat_participant " +
            "                     WHERE room_id = #{roomId} AND user_id = #{currentUserId}) " +
            "ORDER BY cm.send_time ASC")
    List<ChatMessageBean> getClubChatMessages(@Param("roomId") Long roomId, @Param("currentUserId") String currentUserId);

    @Select("SELECT message_id as messageId, room_id as roomId, sender_id as senderId, " +
            "sender_type as senderType, message_content as messageContent, " +
            "message_type as messageType, file_path as filePath, send_time as sendTime, " +
            "read_count as readCount " +
            "FROM chat_message WHERE message_id = #{messageId}")
    ChatMessageBean getMessageById(@Param("messageId") Long messageId);

    @Update("UPDATE chat_message SET read_count = read_count + 1 WHERE message_id = #{messageId}")
    int incrementReadCount(@Param("messageId") Long messageId);

    // 메시지 검색
    @Select("SELECT cm.message_id as messageId, cm.room_id as roomId, " +
            "cm.sender_id as senderId, cm.sender_type as senderType, " +
            "cm.message_content as messageContent, cm.message_type as messageType, " +
            "cm.file_path as filePath, cm.send_time as sendTime, cm.read_count as readCount, " +
            "CASE cm.sender_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_nickname FROM member m WHERE m.member_id = cm.sender_id) " +
            "  WHEN 'BUSINESS' THEN (SELECT b.business_name FROM business b WHERE b.business_id = cm.sender_id) " +
            "  WHEN 'ADMIN' THEN (SELECT a.admin_name FROM admin a WHERE a.admin_id = cm.sender_id) " +
            "END as senderNickname, " +
            "CASE cm.sender_type " +
            "  WHEN 'MEMBER' THEN (SELECT m.member_profile FROM member m WHERE m.member_id = cm.sender_id) " +
            "  ELSE NULL " +
            "END as senderProfile " +
            "FROM chat_message cm " +
            "WHERE cm.room_id = #{roomId} " +
            "AND cm.message_content LIKE '%' || #{keyword} || '%' " +
            "ORDER BY cm.send_time DESC")
    List<ChatMessageBean> searchMessages(@Param("roomId") Long roomId, @Param("keyword") String keyword);

    // === 읽음 확인 관련 쿼리 ===

    @Insert("INSERT INTO chat_read_receipt (message_id, reader_id) " +
            "VALUES (#{messageId}, #{readerId})")
    int markAsRead(ChatReadReceiptBean readReceipt);

    @Select("SELECT * FROM chat_read_receipt WHERE message_id = #{messageId}")
    List<ChatReadReceiptBean> getReadReceiptsByMessageId(@Param("messageId") Long messageId);

    @Select("SELECT COUNT(*) FROM chat_read_receipt " +
            "WHERE message_id = #{messageId} AND reader_id = #{readerId}")
    int checkIfRead(@Param("messageId") Long messageId, @Param("readerId") String readerId);
}