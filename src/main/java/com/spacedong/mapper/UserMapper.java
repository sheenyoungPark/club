package com.spacedong.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.spacedong.beans.MemberBean;

@Mapper
public interface UserMapper {

	@Insert("INSERT INTO member (member_id, member_pw, member_name, member_email, "
            + "member_phone, member_address, member_joindate, member_nickname, "
            + "member_personality, member_point) "
            + "VALUES (#{member_id}, #{member_pw}, #{member_name}, #{member_email}, "
            + "#{member_phone}, #{member_address}, sysdate, #{member_nickname}, "
            + "NVL(#{member_personality}, 0), NVL(#{member_point}, 0))")
	void signupUser(MemberBean user);
	
	@Select("select * from member where member_id = #{member_id} and member_pw = #{member_pw}")
	MemberBean getLoginUser(MemberBean tempLoginUser);
	

	@Select("select member_id from member where member_id =#{member_id}")
	String checkId(String member_id);
	
//	@Insert("insert into member(member_id, sns_id, member_name, member_email, member_phone, sns_type) values(#{member_id}, #{sns_id}, #{member_name}, #{member_email}, #{member_phone}, 'naver')")
//	public void naverLogin(MemberBean memberBean);
	
	 
    @Select("select * from member where sns_id = #{sns_id} ")
    MemberBean getUserBySnsId(@Param("sns_id") String sns_id);
	
    @Update("update member set member_email = #{member_email}, member_name = #{member_name}, member_phone = #{member_phone}  WHERE sns_id = #{sns_id}")
    public void updateUser(MemberBean memberBean);
    
    @Insert("insert into member(member_id, sns_id, member_name, member_email, member_phone, sns_type) values(#{member_id}, #{sns_id}, #{member_name}, #{member_email}, #{member_phone}, 'naver')")
    public void naverSignUp(MemberBean memberBean);
    
    
}
