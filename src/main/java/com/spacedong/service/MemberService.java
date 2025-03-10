package com.spacedong.service;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spacedong.beans.MemberBean;
import com.spacedong.repository.MemberRepository;

@Service
public class MemberService {
	
	@Resource(name = "loginUser")
	private MemberBean loginUser;	

	@Autowired
	private MemberRepository memberRepository;
		 
	@Transactional
	public void signupUser(MemberBean memberBean) {
		
		System.out.println("서비스: " + memberBean.getMember_id());
		memberRepository.signupMember(memberBean);
	}	
	
	
	public void naverLogin(MemberBean memberBean) {
        // DB에서 사용자가 존재하는지 확인 (sns_id로 체크)
        MemberBean existingUser = memberRepository.getMemberBySnsId(memberBean.getSns_id());

        if (existingUser != null) {
            // 사용자 정보가 존재하면, 정보를 업데이트
        	memberRepository.updateMember(memberBean);
        } else {
            // 사용자가 존재하지 않으면, 신규 사용자로 등록
        	memberRepository.naverSignUp(memberBean);
        }
    }
	
	public boolean getLoginUser(MemberBean tempLoginUser) {
		MemberBean temp = memberRepository.getLoginMember(tempLoginUser);
		if(temp!=null) {
			loginUser.setMember_id(temp.getMember_id());
			loginUser.setMember_name(temp.getMember_name());
			loginUser.setLogin(true);
			System.out.println(loginUser.isLogin());
			return true;
		}
		return false;
	}
	
}
