package com.spacedong.service;

import com.spacedong.beans.BusinessBean;
import com.spacedong.repository.BusinessRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BusinessService {
    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);

    @Resource(name = "loginBusiness")
    public BusinessBean loginBusiness;

    @Autowired
    private BusinessRepository businessRepository;

    public void businessJoin(BusinessBean businessBean) {
        businessRepository.businessJoin(businessBean);
    }

    public boolean getLoginMember(BusinessBean businessBean) {
        BusinessBean temp = businessRepository.getLoginMember(businessBean);

        if(temp != null) {
            loginBusiness.setBusiness_id(temp.getBusiness_id());
            loginBusiness.setBusiness_name(temp.getBusiness_name());
            loginBusiness.setBusiness_pw(temp.getBusiness_pw());
            logger.info("비지니스 서비스 로그인: {}", loginBusiness.getBusiness_name());
            loginBusiness.setBusiness_address(temp.getBusiness_address());
            loginBusiness.setBusiness_email(temp.getBusiness_email());
            loginBusiness.setBusiness_info(temp.getBusiness_info());
            loginBusiness.setBusiness_phone(temp.getBusiness_phone());
            loginBusiness.setBusiness_point(temp.getBusiness_point());
            loginBusiness.setBusiness_joindate(temp.getBusiness_joindate());
            loginBusiness.setBusiness_profile(temp.getBusiness_profile());
            loginBusiness.setBusiness_public(temp.getBusiness_public());
            loginBusiness.setBusiness_number(temp.getBusiness_number());
            loginBusiness.setLogin(true);
            return true;
        }
        return false;
    }

    // 아이디 중복 확인
    public boolean checkId(String business_id) {
        int count = businessRepository.checkId(business_id);
        return count == 0; // 중복이 없으면 true, 있으면 false 반환
    }

    public boolean checkEmail(String business_email) {
        int count = businessRepository.checkEmail(business_email);
        return count == 0; // 중복이 없으면 true, 있으면 false 반환
    }
}