package com.spacedong.mapper;

import com.spacedong.beans.ChatUserBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatUserMapper {

    @Select("SELECT c.user_id, c.user_type, " +
            "CASE " +
            "    WHEN c.user_type = 'MEMBER' THEN m.member_nickname " +
            "    WHEN c.user_type = 'BUSINESS' THEN b.business_name " +
            "    WHEN c.user_type = 'ADMIN' THEN a.admin_name " +
            "END AS nickname, " +
            "CASE " +
            "    WHEN c.user_type = 'MEMBER' THEN COALESCE(m.member_profile, '') " +
            "    WHEN c.user_type = 'BUSINESS' THEN COALESCE(b.business_profile, '') " +
            "    WHEN c.user_type = 'ADMIN' THEN '/sources/picture/기본이미지.png' " +
            "END AS profileImage " +
            "FROM chat_participant c " +
            "LEFT JOIN member m ON c.user_id = m.member_id AND c.user_type = 'MEMBER' " +
            "LEFT JOIN business b ON c.user_id = b.business_id AND c.user_type = 'BUSINESS' " +
            "LEFT JOIN admin a ON c.user_id = a.admin_id AND c.user_type = 'ADMIN' " +
            "WHERE c.room_id = #{roomId} AND c.user_id != #{myId}")
    ChatUserBean getChatPartner(@Param("roomId") Long roomId, @Param("myId") String myId);
}
