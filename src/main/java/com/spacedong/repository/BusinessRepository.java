package com.spacedong.repository;

import com.spacedong.beans.BusinessBean;

import com.spacedong.beans.BusinessItemBean;
import com.spacedong.mapper.BusinessMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    //아이템 등록
    public void create_item(BusinessItemBean businessItemBean){
        businessMapper.create_item(businessItemBean);
    }
    //모든 아이템 리스트
    public List<BusinessItemBean> allItem(){
       return   businessMapper.allItem();
    }

    // 아이템 ID로 아이템 정보 가져오기
    public BusinessItemBean getItemById(@Param("item_id") String item_id){
        return businessMapper.getItemById(item_id);
    }

    //판매자 정보 가져오기
    public BusinessBean getBusinessById(String business_id){
        return businessMapper.getBusinessById(business_id);
    }

    // 특정 날짜에 예약된 시간대 조회
    public List<Integer> getReservedTimesByItemIdAndDate(@Param("itemId") String itemId, @Param("date") String date){
        return businessMapper.getReservedTimesByItemIdAndDate(itemId, date) ;
    }

    // 아이템 삭제
    public void deleteItem(@Param("itemId") String item_id){
        businessMapper.deleteItem(item_id);
    }

    //아이템 정보 업데이트
    public void updateItem(BusinessItemBean businessItemBean){
        businessMapper.updateItem(businessItemBean);
    }

}

