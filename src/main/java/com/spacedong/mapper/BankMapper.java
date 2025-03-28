package com.spacedong.mapper;

import com.spacedong.beans.BankBean;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BankMapper {

    // 환전 요청 등록
    @Insert("INSERT INTO bank(bank_id, business_id, totalpoint, bank_date, exchange_point, status) " +
            "VALUES(bank_id_seq.NEXTVAL, #{business_id}, #{totalpoint}, SYSDATE, #{exchange_point}, 'WAIT')")
    void requestExchange(BankBean bankBean);

    // 환전 요청 목록 조회
    @Select("SELECT b.*, bus.business_name FROM bank b " +
            "LEFT JOIN business bus ON b.business_id = bus.business_id " +
            "ORDER BY b.bank_date DESC")
    List<BankBean> getAllExchangeRequests();

    // 특정 판매자의 환전 요청 목록 조회
    @Select("SELECT b.*, bus.business_name FROM bank b " +
            "LEFT JOIN business bus ON b.business_id = bus.business_id " +
            "WHERE b.business_id = #{business_id} " +
            "ORDER BY b.bank_date DESC")
    List<BankBean> getExchangeRequestsByBusinessId(String business_id);

    // 보류 중인 환전 요청 조회
    @Select("SELECT b.*, bus.business_name FROM bank b " +
            "LEFT JOIN business bus ON b.business_id = bus.business_id " +
            "WHERE b.status = 'WAIT' " +
            "ORDER BY b.bank_date ASC")
    List<BankBean> getPendingExchangeRequests();

    // 환전 요청 상세 조회
    @Select("SELECT b.*, bus.business_name FROM bank b " +
            "LEFT JOIN business bus ON b.business_id = bus.business_id " +
            "WHERE b.bank_id = #{bank_id}")
    BankBean getExchangeRequestById(int bank_id);

    // 환전 요청 승인
    @Update("UPDATE bank SET admin_id = #{admin_id}, status = 'PASS' WHERE bank_id = #{bank_id}")
    void approveExchangeRequest(@Param("bank_id") int bank_id, @Param("admin_id") String admin_id);

    // 환전 요청 취소/삭제
    @Delete("DELETE FROM bank WHERE bank_id = #{bank_id} AND status = 'WAIT'")
    void deleteExchangeRequest(int bank_id);

    // 판매자의 포인트 업데이트 (환전 승인 시 포인트를 0으로 설정)
    @Update("UPDATE business SET business_point = business_point - #{exchange_point} WHERE business_id = #{business_id}")
    void reduceBusinessPoint(@Param("business_id") String business_id, @Param("exchange_point") int exchange_point);

    @Select("select business_id from bank where bank_id = #{bank_id}")
    String getBusinessIdByBankId(@Param("bank_id") int bank_id);
}