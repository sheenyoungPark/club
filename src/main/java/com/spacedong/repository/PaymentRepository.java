package com.spacedong.repository;

import com.spacedong.mapper.PaymentMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    @Autowired
    private PaymentMapper paymentMapper;  // 🔹 PaymentMapper 주입

    // 🔹 회원 포인트 업데이트 (기존 포인트 + 충전 금액)
    public void updateMemberPoint(String memberId, int amount) {
        paymentMapper.updateMemberPoint(memberId, amount);
    }

    //회원 포인트 업데이트 (아이템 구매시 차감)
    public void payMoney(@Param("amount") int amount, @Param("memberId") String memberId){
        paymentMapper.payMoney(amount, memberId);
    }

    public void businessAddPoint(@Param("amount")int amount, @Param("business_id")String business_id){
        paymentMapper.businessAddPoint(amount, business_id);
    }

    public void businessCanclePoint(@Param("amount")int amount, @Param("business_id")String business_id){
        paymentMapper.businessCanclePoint(amount, business_id);
    }


}
