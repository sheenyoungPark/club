package com.spacedong.service;

import com.spacedong.repository.PaymentRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;  // 🔹 PaymentRepository 주입

    // 🔹 회원 포인트 업데이트 (기존 포인트 + 충전 금액)
    @Transactional
    public void updateMemberPoint(String memberId, int amount) {
        paymentRepository.updateMemberPoint(memberId, amount);  // 🔹 레포지토리 호출 (DB 업데이트)
    }

    //회원 포인트 업데이트 (아이템 구매시 차감)
    public void payMoney(@Param("amount") int amount, @Param("memberId") String memberId){
        paymentRepository.payMoney(amount, memberId);
    }

    public void businessAddPoint(@Param("amount")int amount, @Param("business_id")String business_id){
        paymentRepository.businessAddPoint(amount, business_id);
    }


}
