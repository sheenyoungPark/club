package com.spacedong.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.spacedong.beans.MemberBean;

public class MemberValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return MemberBean.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		MemberBean memberBean = (MemberBean) target;

		if (memberBean.getMember_id().length() < 4) {
			errors.rejectValue("member_id", "Size.memberBean.member_id", "아이디는 최소 5자 이상이어야 합니다.");
		}
		if (memberBean.getMember_pw().length() < 7) {
			errors.rejectValue("member_pw", "Size.memberBean.member_pw", "비밀번호는 최소 8자 이상이어야 합니다.");
		}


		// 비밀번호와 비밀번호 확인이 일치하는지 검사
		if (memberBean.getMember_pw() != null && memberBean.getMember_pw2() != null) {
			if (!memberBean.getMember_pw().equals(memberBean.getMember_pw2())) {
				errors.rejectValue("member_pw2", null, "비밀번호가 일치하지 않습니다.");
			}
		}

		// 아이디 중복확인 체크
		if (!memberBean.isIdExist()) {
			errors.rejectValue("member_id", null, "중복확인 버튼을 눌러주세요.");
		}

		// 닉네임 중복확인 체크
		if (!memberBean.isNickExist()) {
			errors.rejectValue("member_nickname", null, "중복확인 버튼을 눌러주세요.");
		}
	}
}
