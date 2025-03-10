package com.spacedong.validator;

import com.spacedong.beans.MemberBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class MemberValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return MemberBean.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		MemberBean memberBean = (MemberBean) target;

		// 비밀번호와 비밀번호 확인이 일치하는지 검사
		if (memberBean.getMember_pw() != null && memberBean.getMember_pw2() != null) {
			if (!memberBean.getMember_pw().equals(memberBean.getMember_pw2())) {
				errors.rejectValue("member_pw2", null, "비밀번호가 일치하지 않습니다.");
			}

		}
	}

}
