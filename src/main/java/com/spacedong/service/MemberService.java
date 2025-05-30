package com.spacedong.service;

import com.spacedong.beans.BoardBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.mapper.AdminMapper;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spacedong.beans.MemberBean;
import com.spacedong.repository.MemberRepository;

import java.util.List;

@Service
public class MemberService {

	@Resource(name = "loginMember")
	private MemberBean loginMember;

	@Autowired
	private MemberRepository memberRepository;
    @Autowired
    private AdminMapper adminMapper;

	@Transactional
	public void signupMember(MemberBean memberBean) {
		System.out.println("서비스: " + memberBean.getMember_id());
		memberRepository.signupMember(memberBean);
	}

	// 아이디 중복 확인
	public boolean checkId(String member_id) {
		int check_id = memberRepository.checkId(member_id);
		if(check_id == 1) {
			return false;  // 중복 있음, 사용 불가
		} else {
			return true;   // 중복 없음, 사용 가능
		}
	}

	// 닉네임 중복 확인 - 기존 버전 (회원가입용)
	public boolean checkNickname(String member_nickname) {
		int check_nick = memberRepository.checkNickname(member_nickname);
		if(check_nick == 1) {
			return false;  // 중복 있음, 사용 불가
		} else {
			return true;   // 중복 없음, 사용 가능
		}
	}

	// 닉네임 중복 확인 - 오버로드 버전 (회원정보 수정용)
	// String 타입으로 변경됨 (Long -> String)
	public boolean checkNickname(String member_nickname, String current_id) {
		// 현재 사용자의 닉네임인 경우 처리
		if (current_id != null && !current_id.isEmpty()) {
			MemberBean currentMember = memberRepository.getMemberById(current_id);
			if (currentMember != null && currentMember.getMember_nickname().equals(member_nickname)) {
				return true;  // 본인 닉네임은 사용 가능
			}
		}

		// 다른 사용자의 닉네임 중복 체크
		int check_nick = memberRepository.checkNickname(member_nickname);
		if(check_nick == 1) {
			return false;  // 중복 있음, 사용 불가
		} else {
			return true;   // 중복 없음, 사용 가능
		}
	}

	public void naverLogin(MemberBean memberBean) {
		// DB에서 사용자가 존재하는지 확인 (sns_id로 체크)
		MemberBean existingMember = memberRepository.getMemberBySnsId(memberBean.getSns_id());


		if (existingMember != null) {
			// 사용자 정보가 존재하면, 정보를 업데이트
			loginMember.setMember_id(existingMember.getMember_id());
			loginMember.setMember_name(existingMember.getMember_name());
			loginMember.setMember_phone(existingMember.getMember_phone());
			loginMember.setMember_email(existingMember.getMember_email());
			loginMember.setMember_nickname(existingMember.getMember_nickname());
			loginMember.setMember_address(existingMember.getMember_address());
			loginMember.setMember_joindate(existingMember.getMember_joindate());
			loginMember.setMember_profile(existingMember.getMember_profile());
			loginMember.setMember_personality(existingMember.getMember_personality());
			loginMember.setMember_point(existingMember.getMember_point());
			loginMember.setMember_birthdate(existingMember.getMember_birthdate());
			loginMember.setMember_gender(existingMember.getMember_gender());
			memberRepository.updateMember(memberBean);
		} else {
			// 사용자가 존재하지 않으면, 신규 사용자로 등록
			memberRepository.naverSignUp(memberBean);
			MemberBean member = memberRepository.getMemberBySnsId(memberBean.getSns_id());
			loginMember.setMember_id(member.getMember_id());
			loginMember.setMember_name(member.getMember_name());
			loginMember.setMember_phone(member.getMember_phone());
			loginMember.setMember_email(member.getMember_email());
			loginMember.setMember_nickname(member.getMember_nickname());
			loginMember.setMember_address(member.getMember_address());
			loginMember.setMember_joindate(member.getMember_joindate());
			loginMember.setMember_profile(member.getMember_profile());
			loginMember.setMember_personality(member.getMember_personality());
			loginMember.setMember_point(member.getMember_point());
			loginMember.setMember_birthdate(member.getMember_birthdate());
			loginMember.setMember_gender(member.getMember_gender());
		}
    }
	
	public boolean getLoginMember(MemberBean tempLoginMember) {
		MemberBean temp = memberRepository.getLoginMember(tempLoginMember);
		if(temp!=null) {
			loginMember.setMember_id(temp.getMember_id());
			loginMember.setMember_pw(temp.getMember_pw());
			loginMember.setMember_name(temp.getMember_name());
			loginMember.setMember_email(temp.getMember_email());
			loginMember.setMember_gender(temp.getMember_gender());
			loginMember.setMember_birthdate(temp.getMember_birthdate());
			loginMember.setMember_nickname(temp.getMember_nickname());
			loginMember.setMember_address(temp.getMember_address());
			loginMember.setMember_phone(temp.getMember_phone());
			loginMember.setMember_personality(temp.getMember_personality());
			loginMember.setMember_profile(temp.getMember_profile());
			loginMember.setMember_point(temp.getMember_point());



			loginMember.setLogin(true);
			System.out.println(loginMember.isLogin());
			System.out.println(loginMember.getMember_nickname());
			System.out.println(loginMember.getMember_profile());
			return true;
		}
		return false;
	}

	public void savePersonality(int member_personality, String member_id) {
		memberRepository.savePersonality(member_personality, member_id);
	}

	// 회원 정보 업데이트
	public void updateMember(MemberBean memberBean) {
		memberRepository.updateMember(memberBean);
		loginMember.setMember_name(memberBean.getMember_name());
	}


	public void editMember(MemberBean memberBean) {
		try {
			// 로그 추가
			System.out.println("editMember 호출됨 - ID: " + memberBean.getMember_id());
			System.out.println("업데이트할 닉네임: " + memberBean.getMember_nickname());
			System.out.println("업데이트할 전화번호: " + memberBean.getMember_phone());
			System.out.println("업데이트할 주소: " + memberBean.getMember_address());

			// DB 업데이트
			memberRepository.editMember(memberBean);

			// 세션 정보 업데이트
			loginMember.setMember_nickname(memberBean.getMember_nickname());
			loginMember.setMember_address(memberBean.getMember_address());
			loginMember.setMember_phone(memberBean.getMember_phone());

			System.out.println("세션 업데이트 완료 - 닉네임: " + loginMember.getMember_nickname());
		} catch (Exception e) {
			System.out.println("editMember 메소드 오류: " + e.getMessage());
			e.printStackTrace();
			throw e; // 상위로 예외 전파
		}
	}

	public boolean checkPassword(String member_id, String currentPassword) {
		String storedPassword = memberRepository.getPasswordById(member_id);
		return storedPassword != null && storedPassword.equals(currentPassword);
	}

	@Transactional
	public void updatePassword(String member_id, String newPassword) {
		memberRepository.updatePassword(member_id, newPassword);
	}

	// 회원 탈퇴 처리
	public void deleteMember(String member_id) {
		memberRepository.deleteMember(member_id);
		loginMember.setLogin(false);
		loginMember.setMember_id(null);
		loginMember.setMember_name(null);
	}

	public String getMaskedPhone() {
		String phone = loginMember.getMember_phone();

		if (phone != null && phone.length() == 11) { // 01029920409 같은 형식이면 자동 변환
			phone = phone.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
		}

		if (phone != null && phone.length() == 13) { // 변환된 010-2992-0409 형식이면 마스킹
			String masked = phone.replaceAll("(\\d{3})-(\\d{4})-(\\d{4})", "$1-****-$3");
			System.out.println("마스킹된 휴대폰 번호: " + masked);
			return masked;
		}

		System.out.println("원래 번호 반환: " + phone);
		return phone;
	}
	/** ✅ 회원 프로필 업데이트 */
	public void updateMemberProfile(String memberId, String fileName) {
		memberRepository.updateMemberProfile(memberId, fileName);
	}

	// 모든 회원 목록 조회
	public List<MemberBean> getAllMembers() {
		return memberRepository.getAllMembers();
	}

	// 회원 검색
	public List<MemberBean> searchMembers(String searchType, String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return getAllMembers();
		}

		switch (searchType) {
			case "id":
				return memberRepository.searchMembersById(keyword);
			case "name":
				return memberRepository.searchMembersByName(keyword);
			case "email":
				return memberRepository.searchMembersByEmail(keyword);
			case "phone":
				return memberRepository.searchMembersByPhone(keyword);
			default:
				return memberRepository.searchMembersByAllFields(keyword);
		}
	}

	public List<ClubBean> getJoinedClubs(String memberId) {
		return memberRepository.getJoinedClubsWithRole(memberId);
	}


	public List<BoardBean> getUserPosts(String memberId) {
		return memberRepository.getUserPosts(memberId);
	}

	public MemberBean selectMemberById(String memberId) {
		return memberRepository.selectMemberById(memberId);
	}

	public MemberBean getMemberById(String member_id){
		return memberRepository.getMemberById(member_id);
	}

	//핸드폰으로 멤버찾기
	public String findByPhone(String member_phone){
		String formattedPhone = formatPhoneNumber(member_phone);

		return memberRepository.findByPhone(formattedPhone);
	}

	public MemberBean findMemberByIdAndPhone(String member_id,String member_phone){
		String formattedPhone = formatPhoneNumber(member_phone);
		System.out.println("mService" + formattedPhone);
		return memberRepository.findMemberByIdAndPhone(member_id, formattedPhone);
	}

	public void newpassowrd(String member_id, String newPassword) {
		MemberBean member = memberRepository.selectMemberById(member_id);

		if (member != null) {
			// 비밀번호 암호화
			member.setMember_pw(newPassword);

			memberRepository.resetPw(member.getMember_id(), member.getMember_pw());
		}

	}

	/**
	 * 전화번호 형식 변환 (하이픈 추가)
	 * 예: 01012345678 -> 010-1234-5678
	 */
	private String formatPhoneNumber(String phone) {
		System.out.println("mService" + phone);
		// 이미 하이픈이 있는 경우 그대로 반환
		if (phone.contains("-")) {
			return phone;
		}

		// 숫자만 남기기
		phone = phone.replaceAll("[^0-9]", "");

		// 길이에 따라 적절한 형식으로 변환
		if (phone.length() == 10) { // 10자리 (예: 0101231234)
			return phone.substring(0, 2) + "-" +
					phone.substring(3, 6) + "-" +
					phone.substring(6);
		} else if (phone.length() == 11) { // 11자리 (예: 01012345678)
			return phone.substring(0, 3) + "-" +
					phone.substring(3, 7) + "-" +
					phone.substring(7);
		} else {
			// 기타 형식은 그대로 반환
			return phone;
		}
	}

	public String getMemberIdbyNickname(String member_nickname){
		return memberRepository.getMemberIdbyNickname(member_nickname);
	}

	/**
	 * 주어진 아이디가 관리자 아이디인지 확인
	 * @param memberId 확인할 아이디
	 * @return 관리자 아이디이면 true, 아니면 false
	 */
	public boolean isAdminId(String memberId) {
		// 관리자 테이블에서 해당 아이디가 존재하는지 확인
		return adminMapper.countAdminById(memberId) > 0;
	}


}