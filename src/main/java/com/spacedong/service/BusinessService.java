package com.spacedong.service;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.repository.BusinessRepository;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    // 아이디 중복 확인
    public boolean checkId(String business_id) {
        int count = businessRepository.checkId(business_id);
        return count == 0; // 중복이 없으면 true, 있으면 false 반환
    }

    public boolean checkEmail(String business_email) {
        int count = businessRepository.checkEmail(business_email);
        return count == 0; // 중복이 없으면 true, 있으면 false 반환
    }

    //아이템 등록
    public void create_item(BusinessItemBean businessItemBean){
        businessRepository.create_item(businessItemBean);
    }

    //모든 아이템 리스트
    public List<BusinessItemBean> allItem(){
        return businessRepository.allItem();
    }

    // 아이템 ID로 아이템 정보 가져오기
    public BusinessItemBean getItemById(@Param("item_id") String item_id){
        return businessRepository.getItemById(item_id);
    }

    //판매자 정보 가져오기
    public BusinessBean getBusinessById(String business_id){
        return businessRepository.getBusinessById(business_id);
    }

    // 특정 날짜에 예약된 시간대 조회
    public List<Integer> getReservedTimesByItemIdAndDate(@Param("itemId") String itemId, @Param("date") String date){
        return businessRepository.getReservedTimesByItemIdAndDate(itemId, date) ;
    }

    // 아이템 삭제
   public void deleteItem(@Param("itemId") String item_id){
        businessRepository.deleteItem(item_id);
    }

    //아이템 정보 업데이트
    public void updateItem(BusinessItemBean businessItemBean){
        businessRepository.updateItem(businessItemBean);
    }

}