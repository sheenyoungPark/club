package com.spacedong.service;

import com.spacedong.beans.BankBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BankService {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private BusinessService businessService;

    // 환전 요청 등록
    @Transactional
    public void requestExchange(String businessId, int exchangePoint) throws Exception {
        // 판매자 정보 확인
        BusinessBean business = businessService.getBusinessById(businessId);

        if (business == null) {
            throw new Exception("판매자 정보를 찾을 수 없습니다.");
        }

        // 포인트 확인
        if (business.getBusiness_point() < exchangePoint) {
            throw new Exception("보유한 포인트보다 많은 금액을 환전할 수 없습니다.");
        }

        if (exchangePoint < 1000) {
            throw new Exception("최소 환전 가능 금액은 1,000P입니다.");
        }

        // 환전 요청 객체 생성
        BankBean bankBean = new BankBean();
        bankBean.setBusiness_id(businessId);
        bankBean.setTotalpoint(business.getBusiness_point());
        bankBean.setExchange_point(exchangePoint);
        bankBean.setStatus("WAIT");

        // 환전 요청 등록
        bankRepository.requestExchange(bankBean);
    }

    // 모든 환전 요청 목록 조회
    public List<BankBean> getAllExchangeRequests() {
        return bankRepository.getAllExchangeRequests();
    }

    // 특정 판매자의 환전 요청 목록 조회
    public List<BankBean> getExchangeRequestsByBusinessId(String businessId) {
        return bankRepository.getExchangeRequestsByBusinessId(businessId);
    }

    // 보류 중인 환전 요청 조회
    public List<BankBean> getPendingExchangeRequests() {
        return bankRepository.getPendingExchangeRequests();
    }

    // 환전 요청 상세 조회
    public BankBean getExchangeRequestById(int bankId) {
        return bankRepository.getExchangeRequestById(bankId);
    }

    // 환전 요청 승인
    @Transactional
    public void approveExchangeRequest(int bankId, String adminId) throws Exception {
        // 환전 요청 정보 조회
        BankBean bank = bankRepository.getExchangeRequestById(bankId);

        if (bank == null) {
            throw new Exception("환전 요청 정보를 찾을 수 없습니다.");
        }

        if (!"WAIT".equals(bank.getStatus())) {
            throw new Exception("이미 처리된 요청입니다.");
        }

        // 환전 요청 승인 처리
        bankRepository.approveExchangeRequest(bankId, adminId);

        // 판매자 포인트 초기화
        bankRepository.reduceBusinessPoint(bank.getBusiness_id(), bank.getExchange_point());
    }

    // 환전 요청 취소/삭제
    public void deleteExchangeRequest(int bankId) throws Exception {
        BankBean bank = bankRepository.getExchangeRequestById(bankId);

        if (bank == null) {
            throw new Exception("환전 요청 정보를 찾을 수 없습니다.");
        }

        if (!"WAIT".equals(bank.getStatus())) {
            throw new Exception("이미 처리된 요청은 취소할 수 없습니다.");
        }

        bankRepository.deleteExchangeRequest(bankId);
    }
}