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
            logger.info("비지니스 서비스 로그인: {}", loginBusiness.getBusiness_name());
            loginBusiness.setLogin(true);
            return true;
        }
        return false;
    }
}