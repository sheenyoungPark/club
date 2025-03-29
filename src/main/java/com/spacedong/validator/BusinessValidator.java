package com.spacedong.validator;

import com.spacedong.beans.BusinessBean;
import com.spacedong.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class BusinessValidator implements Validator {

    private final BusinessService businessService;

    @Autowired
    public BusinessValidator(BusinessService businessService) {
        this.businessService = businessService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return BusinessBean.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BusinessBean businessBean = (BusinessBean) target;

        // 이메일 중복 체크만 수행
        if (businessBean.getBusiness_email() != null && !businessBean.getBusiness_email().isEmpty()) {
            // 이메일이 이미 존재하는지 확인 (회원 가입시에만)
            // 단, 기존 회원 정보 수정 시에는 자신의 이메일은 제외해야 함
            if (!businessService.checkEmail(businessBean.getBusiness_email())) {
                errors.rejectValue("business_email", "duplicate", "이미 사용 중인 이메일입니다.");
            }
        }
    }

    /**
     * 비밀번호 유효성 검사 - 길이만 체크
     *
     * @param password 검사할 비밀번호
     * @return 유효성 검사 결과 (true: 유효, false: 유효하지 않음)
     */
    public boolean validatePassword(String password) {
        // 비밀번호 길이 체크 (8~16자)
        return password != null && password.length() >= 8 && password.length() <= 16;
    }
}