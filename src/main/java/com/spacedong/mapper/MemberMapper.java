package com.spacedong.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.spacedong.beans.MemberBean;

@Mapper
public interface MemberMapper {

	@Insert("INSERT INTO member (member_id, member_pw, member_name, member_email, "
            + "member_phone, member_address, member_joindate, member_nickname, "
            + "member_personality, member_point) "
            + "VALUES (#{member_id}, #{member_pw}, #{member_name}, #{member_email}, "
            + "#{member_phone}, #{member_address}, sysdate, #{member_nickname}, "
            + "NVL(#{member_personality}, 0), NVL(#{member_point}, 0))")
	void signupMember(MemberBean memberBean);
	
	@Select("select * from member where member_id = #{member_id} and member_pw = #{member_pw}")
	MemberBean getLoginMember(MemberBean tempLoginMember);
	

	@Select("SELECT COUNT(*) FROM member WHERE member_id = #{member_id}")
	int checkId(String member_id);

    @Select("SELECT COUNT(*) FROM member WHERE member_nickname = #{member_nickname}")
    int checkNickname(String member_nickname);
	
//	@Insert("insert into member(member_id, sns_id, member_name, member_email, member_phone, sns_type) values(#{member_id}, #{sns_id}, #{member_name}, #{member_email}, #{member_phone}, 'naver')")
//	public void naverLogin(MemberBean memberBean);
	
	 
    @Select("select * from member where sns_id = #{sns_id} ")
    MemberBean getMemberBySnsId(@Param("sns_id") String sns_id);
	
    @Update("update member set member_email = #{member_email}, member_name = #{member_name}, member_phone = #{member_phone}  WHERE sns_id = #{sns_id}")
    public void updateMember(MemberBean memberBean);
    
    @Insert("insert into member(member_id, sns_id, member_name, member_email, member_phone, sns_type, member_nickname) values(#{member_id}, #{sns_id}, #{member_name}, #{member_email}, #{member_phone}, 'naver','user' || TO_CHAR(member_nickname_seq.NEXTVAL))")
    public void naverSignUp(MemberBean memberBean);

    @Update("update member set member_personality=#{member_personality} where member_id = #{member_id}")
    public void savePersonality(@Param("member_personality") int member_personality, @Param("member_id") String member_id);

}
