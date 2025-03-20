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

        return businessMapper.getLoginMember(businessBean);
    }

    // 아이디 중복 검사
    public int checkId(String business_id) {
        return businessMapper.checkId(business_id);
    }

    // 이메일 중복 검사
    public int checkEmail(String business_email) {
        return businessMapper.checkEmail(business_email);
    }
}

