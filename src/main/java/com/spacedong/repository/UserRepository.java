package com.spacedong.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.MemberBean;
import com.spacedong.mapper.UserMapper;

@Repository
public class UserRepository {

	@Autowired
	private UserMapper userMapper;
	
	
	public void signupUser(MemberBean memberBean) {
		userMapper.signupUser(memberBean);
	}

	
	public String checkId(String user_id) {
		return userMapper.checkId(user_id);
	}
	
	public MemberBean getLoginUser(MemberBean tempLoginUser) {
		return userMapper.getLoginUser(tempLoginUser);
		
	}
	
	public MemberBean getUserBySnsId(String sns_id) {
		
		return userMapper.getUserBySnsId(sns_id);
	}
	
	public void updateUser(MemberBean memberBean) {
		userMapper.updateUser(memberBean);
	}
	public void naverSignUp(MemberBean memeberBean) {
		userMapper.naverSignUp(memeberBean);
	}
	
}
