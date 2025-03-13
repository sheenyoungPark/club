package com.spacedong.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.spacedong.beans.MemberBean;
import java.util.Date;  // Date 클래스 import 추가

public class MemberValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return MemberBean.class.isAssignableFrom(clazz);
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

		// 성별 검증 - 메소드 안으로 이동
		if (memberBean.getMember_gender() == null || memberBean.getMember_gender().isEmpty()) {
			errors.rejectValue("member_gender", "error.member_gender", "성별을 선택해주세요.");
		}

		// 생년월일 검증 - 메소드 안으로 이동
		if (memberBean.getMember_birthdate() == null) {
			errors.rejectValue("member_birthdate", "error.member_birthdate", "생년월일을 입력해주세요.");
		} else {
			// 현재 날짜
			Date currentDate = new Date();

			// 생년월일이 미래 날짜인지 확인
			if (memberBean.getMember_birthdate().after(currentDate)) {
				errors.rejectValue("member_birthdate", "error.member_birthdate.future", "생년월일은 현재 날짜보다 이전이어야 합니다.");
			}
		}
	}
}