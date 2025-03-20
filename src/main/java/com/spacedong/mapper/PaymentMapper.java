package com.spacedong.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PaymentMapper {

    // 🔹 회원 포인트 업데이트 (기존 포인트 + 충전 금액)
    @Update("UPDATE member SET member_point = member_point + #{amount} WHERE member_id = #{memberId}")
    void updateMemberPoint(@Param("memberId") String memberId, @Param("amount") int amount);
}
