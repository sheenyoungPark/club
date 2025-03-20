package com.spacedong.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PaymentMapper {

    // 🔹 회원 포인트 업데이트 (기존 포인트 + 충전 금액)
    @Update("UPDATE member SET member_point = member_point + #{amount} WHERE member_id = #{memberId}")
    void updateMemberPoint(@Param("memberId") String memberId, @Param("amount") int amount);

    //회원 포인트 업데이트 (아이템 구매시 차감)
    @Update("UPDATE member SET member_point = member_point - #{amount} WHERE member_id = #{memberId}")
    void payMoney(@Param("amount") int amount, @Param("memberId") String memberId);

    @Update("UPDATE business set business_point = business_point + #{amount} where business_id = #{business_id}")
    void businessAddPoint(@Param("amount")int amount, @Param("business_id")String business_id);

    @Update("UPDATE business set business_point = business_point - #{amount} where business_id = #{business_id}")
    void businessCanclePoint(@Param("amount")int amount, @Param("business_id")String business_id);



}
