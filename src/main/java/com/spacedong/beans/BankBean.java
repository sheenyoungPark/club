package com.spacedong.beans;

import java.util.Date;

public class BankBean {
    private int bank_id;          // 거래번호
    private String admin_id;      // 거래 승인자(관리자)
    private String business_id;   // 거래 요청자(비즈니스)
    private int totalpoint;       // 우주동의 총금액
    private Date bank_date;       // 거래(환전요청) 날짜
    private int exchange_point;   // 거래 금액
    private String status;        // 승인 상태(wait, pass)
    private String business_name; // 추가: 비즈니스 이름 (조회용)

    public BankBean() {
        // 기본 생성자
    }

    public int getBank_id() {
        return bank_id;
    }

    public void setBank_id(int bank_id) {
        this.bank_id = bank_id;
    }

    public String getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(String admin_id) {
        this.admin_id = admin_id;
    }

    public String getBusiness_id() {
        return business_id;
    }

    public void setBusiness_id(String business_id) {
        this.business_id = business_id;
    }

    public int getTotalpoint() {
        return totalpoint;
    }

    public void setTotalpoint(int totalpoint) {
        this.totalpoint = totalpoint;
    }

    public Date getBank_date() {
        return bank_date;
    }

    public void setBank_date(Date bank_date) {
        this.bank_date = bank_date;
    }

    public int getExchange_point() {
        return exchange_point;
    }

    public void setExchange_point(int exchange_point) {
        this.exchange_point = exchange_point;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    @Override
    public String toString() {
        return "BankBean{" +
                "bank_id=" + bank_id +
                ", admin_id='" + admin_id + '\'' +
                ", business_id='" + business_id + '\'' +
                ", totalpoint=" + totalpoint +
                ", bank_date=" + bank_date +
                ", exchange_point=" + exchange_point +
                ", status='" + status + '\'' +
                ", business_name='" + business_name + '\'' +
                '}';
    }
}