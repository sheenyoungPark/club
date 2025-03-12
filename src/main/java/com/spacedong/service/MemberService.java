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
			loginMember.setLogin(true);
			System.out.println(loginMember.isLogin());
			return true;
		}
		return false;
	}
	
}
