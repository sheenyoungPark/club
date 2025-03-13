package com.spacedong.mapper;

import org.apache.ibatis.annotations.*;

import com.spacedong.beans.MemberBean;

@Mapper
public interface MemberMapper {

    @Insert("INSERT INTO member (member_id, member_pw, member_name, member_email, member_phone, member_address, member_joindate," +
            " member_nickname, member_personality, member_point, member_gender, member_birthdate) " +
            "VALUES (#{member_id}, #{member_pw}, #{member_name}, #{member_email}, #{member_phone}, #{member_address}, sysdate," +
            " #{member_nickname}, NVL(#{member_personality}, 0), NVL(#{member_point}, 0), #{member_gender}, #{member_birthdate})")
    void signupMember(MemberBean memberBean);

    @Select("select * from member where member_id = #{member_id} and member_pw = #{member_pw}")
    MemberBean getLoginMember(MemberBean tempLoginMember);

    @Select("SELECT COUNT(*) FROM member WHERE member_id = #{member_id}")
    int checkId(String member_id);

    @Select("SELECT COUNT(*) FROM member WHERE member_nickname = #{member_nickname}")
    int checkNickname(String member_nickname);

    // ID로 회원 정보 조회 (닉네임 중복체크용)
    @Select("SELECT * FROM member WHERE member_id = #{member_id}")
    MemberBean getMemberById(@Param("member_id") String member_id);

//	@Insert("insert into member(member_id, sns_id, member_name, member_email, member_phone, sns_type) values(#{member_id}, #{sns_id}, #{member_name}, #{member_email}, #{member_phone}, 'naver')")
//	public void naverLogin(MemberBean memberBean);


    @Select("select * from member where sns_id = #{sns_id} ")
    MemberBean getMemberBySnsId(@Param("sns_id") String sns_id);

    @Update("update member set member_email = #{member_email}, member_name = #{member_name}, member_phone = #{member_phone}  WHERE sns_id = #{sns_id}")
    public void updateMember(MemberBean memberBean);


    @Update("UPDATE member SET member_nickname = #{member_nickname}, " +
            "member_phone = #{member_phone}, member_address = #{member_address} " +
            "WHERE member_id = #{member_id}")
    void editMember(MemberBean member);

    @Select("SELECT member_pw FROM member WHERE member_id = #{member_id}")
    String getPasswordById(@Param("member_id") String memberId);

    @Update("UPDATE member SET member_pw = #{newPassword} WHERE member_id = #{member_id}")
    void updatePassword(@Param("member_id") String memberId, @Param("newPassword") String newPassword);

    @Insert("insert into member(member_id, sns_id, member_name, member_email, member_phone, sns_type) values(#{member_id}, #{sns_id}, #{member_name}, #{member_email}, #{member_phone}, 'naver')")
    public void naverSignUp(MemberBean memberBean);

    @Update("update member set member_personality=#{member_personality} where member_id = #{member_id}")
    public void savePersonality(@Param("member_personality") int member_personality, @Param("member_id") String member_id);

    @Delete("DELETE FROM member WHERE member_id = #{member_id}")
    void deleteMember(String member_id);
}