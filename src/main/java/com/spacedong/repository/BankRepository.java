package com.spacedong.repository;

import com.spacedong.beans.BankBean;
import com.spacedong.mapper.BankMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BankRepository {

    @Autowired
    private BankMapper bankMapper;

    // 환전 요청 등록
    public void requestExchange(BankBean bankBean) {
        bankMapper.requestExchange(bankBean);
    }

    // 모든 환전 요청 목록 조회
    public List<BankBean> getAllExchangeRequests() {
        return bankMapper.getAllExchangeRequests();
    }

    // 특정 판매자의 환전 요청 목록 조회
    public List<BankBean> getExchangeRequestsByBusinessId(String business_id) {
        return bankMapper.getExchangeRequestsByBusinessId(business_id);
    }

    // 보류 중인 환전 요청 조회
    public List<BankBean> getPendingExchangeRequests() {
        return bankMapper.getPendingExchangeRequests();
    }

    // 환전 요청 상세 조회
    public BankBean getExchangeRequestById(int bank_id) {
        return bankMapper.getExchangeRequestById(bank_id);
    }

    // 환전 요청 승인
    public void approveExchangeRequest(int bank_id, String admin_id) {
        bankMapper.approveExchangeRequest(bank_id, admin_id);
    }

    // 환전 요청 취소/삭제
    public void deleteExchangeRequest(int bank_id) {
        bankMapper.deleteExchangeRequest(bank_id);
    }

    // 판매자의 포인트 초기화 (환전 승인 시)
    public void reduceBusinessPoint(String business_id, int exchange_point) {
        bankMapper.reduceBusinessPoint(business_id, exchange_point);
    }

    public String getBusinessIdByBankId(int bank_id) {
        return bankMapper.getBusinessIdByBankId(bank_id);
    }
}