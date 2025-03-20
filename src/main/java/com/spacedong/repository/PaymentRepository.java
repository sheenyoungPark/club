package com.spacedong.repository;

import com.spacedong.mapper.PaymentMapper;
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
}
