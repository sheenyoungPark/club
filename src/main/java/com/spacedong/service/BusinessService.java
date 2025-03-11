package com.spacedong.service;

import com.spacedong.beans.BusinessBean;
import com.spacedong.repository.BusinessRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessService {

    @Resource(name = "loginBusiness")
    public BusinessBean loginBusiness;



    @Autowired
    private BusinessRepository businessRepository;

    public void businessJoin(BusinessBean businessBean) {
        businessRepository.businessJoin(businessBean);

    }

    public boolean getLoginMember(BusinessBean businessBean) {

        BusinessBean temp = businessRepository.getLoginMember(businessBean);

        if(temp!=null) {
            loginBusiness.setBusiness_id(temp.getBusines_id());
            loginBusiness.setBusiness_id(temp.getBusiness_name());

            loginBusiness.setLogin(true);

            return true;
        }
        return false;

    }

}
