package com.spacedong.service;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spacedong.beans.MemberBean;
import com.spacedong.repository.MemberRepository;

@Service
public class MemberService {
	
	@Resource(name = "loginMember")
	private MemberBean loginMember;	

	@Autowired
	private MemberRepository memberRepository;


		 
	@Transactional
	public void signupMember(MemberBean memberBean) {
		
		System.out.println("서비스: " + memberBean.getMember_id());
		memberRepository.signupMember(memberBean);
	}

	// 아이디 중복 확인
	public boolean checkId(String member_id) {
		int check_id = memberRepository.checkId(member_id);
		if(check_id == 1){
			return false;

		}else {
			return true;
		}
	}

	// 닉네임 중복 확인
	public boolean checkNickname(String member_nickname) {
		int check_nick = memberRepository.checkNickname(member_nickname);
		if(check_nick == 1){
			return false;

		}else {
			return true;
		}
	}
	
	
	public void naverLogin(MemberBean memberBean) {
        // DB에서 사용자가 존재하는지 확인 (sns_id로 체크)
        MemberBean existingMember = memberRepository.getMemberBySnsId(memberBean.getSns_id());

        if (existingMember != null) {
            // 사용자 정보가 존재하면, 정보를 업데이트
        	memberRepository.updateMember(memberBean);
        } else {
            // 사용자가 존재하지 않으면, 신규 사용자로 등록
        	memberRepository.naverSignUp(memberBean);
        }
    }
	
	public boolean getLoginMember(MemberBean tempLoginMember) {
		MemberBean temp = memberRepository.getLoginMember(tempLoginMember);
		if(temp!=null) {
			loginMember.setMember_id(temp.getMember_id());
			loginMember.setMember_name(temp.getMember_name());
			loginMember.setMember_email(temp.getMember_email());
			loginMember.setMember_nickname(temp.getMember_nickname());
			loginMember.setMember_address(temp.getMember_address());
			loginMember.setMember_phone(temp.getMember_phone());
			loginMember.setLogin(true);
			System.out.println(loginMember.isLogin());
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
}

