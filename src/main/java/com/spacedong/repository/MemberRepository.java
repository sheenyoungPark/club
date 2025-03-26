package com.spacedong.repository;

import com.spacedong.beans.BoardBean;
import com.spacedong.beans.ClubBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.MemberBean;
import com.spacedong.mapper.MemberMapper;

import java.util.List;

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

	public MemberBean selectMemberById(String memberId) {
		return memberMapper.selectMemberById(memberId);
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

	public List<ClubBean> getJoinedClubsWithRole(String memberId) {
		return memberMapper.getJoinedClubsWithRole(memberId);
	}

	public List<BoardBean> getUserPosts(String memberId) {
		return memberMapper.getUserPosts(memberId);
	}
	// 전체 회원 목록 조회
	public List<MemberBean> getAllMembers() {
		return memberMapper.getAllMembers();
	}

	// ID로 회원 검색
	public List<MemberBean> searchMembersById(String keyword) {
		return memberMapper.searchMembersById(keyword);
	}

	// 이름으로 회원 검색
	public List<MemberBean> searchMembersByName(String keyword) {
		return memberMapper.searchMembersByName(keyword);
	}

	// 이메일로 회원 검색
	public List<MemberBean> searchMembersByEmail(String keyword) {
		return memberMapper.searchMembersByEmail(keyword);
	}

	// 전화번호로 회원 검색
	public List<MemberBean> searchMembersByPhone(String keyword) {
		return memberMapper.searchMembersByPhone(keyword);
	}

	// 모든 필드로 회원 검색
	public List<MemberBean> searchMembersByAllFields(String keyword) {
		return memberMapper.searchMembersByAllFields(keyword);
	}




}
