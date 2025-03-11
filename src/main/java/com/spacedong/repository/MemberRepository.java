package com.spacedong.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.MemberBean;
import com.spacedong.mapper.MemberMapper;

@Repository
public class MemberRepository {

	@Autowired
	private MemberMapper memberMapper;
	
	
	public void signupMember(MemberBean memberBean) {
		memberMapper.signupMember(memberBean);
	}

	
	public String checkId(String member_id) {
		return memberMapper.checkId(member_id);
	}
	
	public MemberBean getLoginMember(MemberBean tempLoginMember) {
		return memberMapper.getLoginMember(tempLoginMember);
		
	}
	
	public MemberBean getMemberBySnsId(String sns_id) {
		
		return memberMapper.getMemberBySnsId(sns_id);
	}
	
	public void updateMember(MemberBean memberBean) {
		memberMapper.updateMember(memberBean);
	}
	public void naverSignUp(MemberBean memeberBean) {
		memberMapper.naverSignUp(memeberBean);
	}

	public void savePersonality(int member_personality, String member_id) {
		memberMapper.savePersonality(member_personality, member_id);
	}

	
}
