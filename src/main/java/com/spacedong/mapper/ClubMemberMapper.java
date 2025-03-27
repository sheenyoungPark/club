package com.spacedong.mapper;

import com.spacedong.beans.ClubMemberBean;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClubMemberMapper {

    //등급 확인
    @Select("SELECT * FROM club_member WHERE club_id = #{club_id} AND member_id = #{member_id}")
    ClubMemberBean getMemberInfo(@Param("club_id") int club_id,@Param("member_id") String member_id);

    //신청한 회원들 리스트
    @Select("SELECT cm.*, m.member_name FROM club_member cm " +
            " JOIN member m ON cm.member_id = m.member_id " +
            " WHERE cm.club_id = #{club_id} AND cm.member_role = 'reserve'" +
            " ORDER BY cm.member_joindate ASC")
    List<ClubMemberBean> getPendingMembers(int club_id);

    //회원 등급 변경
    @Update("UPDATE club_member SET member_role = 'normal' WHERE club_id = #{club_id} AND member_id = #{member_id}")
    void UpdateMemberRole(@Param("club_id") int club_id,@Param("member_id") String member_id);

    @Delete("DELETE FROM club_member WHERE club_id = #{club_id} AND member_id = #{member_id}")
    void deleteMember(@Param("club_id") int club_id,@Param("member_id") String member_id);

    @Select("select * from club_member where club_id = #{club_id}")
    List<ClubMemberBean> getClubMembers(@Param("club_id") int club_id);

    @Select("SELECT cm.*, m.member_nickname, m.member_profile " +
            "FROM club_member cm " +
            "JOIN member m ON cm.member_id = m.member_id " +
            "WHERE cm.club_id = #{club_id} " +
            "AND cm.member_role IN ('normal', 'master')")
    List<ClubMemberBean> getClubMemberList(@Param("club_id") int club_id);



}
