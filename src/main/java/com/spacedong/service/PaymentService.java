package com.spacedong.service;

import com.spacedong.repository.PaymentRepository;
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
}
