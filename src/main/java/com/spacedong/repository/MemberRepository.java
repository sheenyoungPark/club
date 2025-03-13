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


	public int checkId(String member_id) {
		return memberMapper.checkId(member_id);
	}

	public int checkNickname(String member_nickname) {
		return memberMapper.checkNickname(member_nickname);
	}

	// ID로 회원 정보 조회 (닉네임 중복체크용)
	public MemberBean getMemberById(String member_id) {
		return memberMapper.getMemberById(member_id);
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

	public void editMember(MemberBean memberBean){
		memberMapper.editMember(memberBean);
	}

	public void naverSignUp(MemberBean memeberBean) {
		memberMapper.naverSignUp(memeberBean);
	}

	public void savePersonality(int member_personality, String member_id) {
		memberMapper.savePersonality(member_personality, member_id);
	}

	public void deleteMember(String member_id){
		memberMapper.deleteMember(member_id);
	}

	public String getPasswordById(String member_id) {
		return memberMapper.getPasswordById(member_id);
	}

	public void updatePassword(String member_id, String newPassword) {
		memberMapper.updatePassword(member_id, newPassword);
	}
	/** ✅ 프로필 이미지 업데이트 */
	public void updateMemberProfile(String memberId, String fileName) {
		memberMapper.updateMemberProfile(memberId, fileName);
	}

}
