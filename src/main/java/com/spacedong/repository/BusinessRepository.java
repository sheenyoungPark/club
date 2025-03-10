package com.spacedong.repository;

import com.spacedong.beans.BusinessBean;

import com.spacedong.mapper.BusinessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BusinessRepository {

    @Autowired
    private BusinessMapper businessMapper;

    public void businessJoin(BusinessBean businessBean) {
        businessMapper.businessJoin(businessBean);

    }

    public BusinessBean getLoginMember(BusinessBean businessBean) {

        System.out.println("리포지토리: " + businessMapper.getLoginMember(businessBean));
        return businessMapper.getLoginMember(businessBean);
    }
}

