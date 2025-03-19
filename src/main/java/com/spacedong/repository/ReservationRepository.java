package com.spacedong.repository;

import com.spacedong.beans.ReservationBean;
import com.spacedong.mapper.ReservationMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class ReservationRepository {

    @Autowired
    private ReservationMapper reservationMapper;



    //예약된 시간 목록 가져오기
    public List<Map<String, Integer>> getReservedTimeRangesByItemIdAndDate(
            @Param("itemId") String itemId,
            @Param("date") String date){
        return reservationMapper.getReservedTimeRangesByItemIdAndDate(itemId, date);
    }

    public int checkTimeRangeOverlap(@Param("itemId") String itemId, @Param("date") Date date, @Param("startTime") int startTime, @Param("endTime") int endTime){
        return reservationMapper.checkTimeRangeOverlap(itemId, date, startTime, endTime);
    }

    //예약생성
    public void createReservation(ReservationBean reservationBean){
       reservationMapper.createReservation(reservationBean);
    }
    //예약번호 조회
    public int getReservationId(String member_id, String item_id, int start_time, Date date){
        return reservationMapper.getReservationId(member_id, item_id, start_time, date);
    }

    //예약정보 가져오기
    public ReservationBean getReservationById(@Param("reservationId") int reservationId){
        return reservationMapper.getReservationById(reservationId);
    }

    //회원별 예약목록
    public List<ReservationBean> getReservationsByMemberId(@Param("memberId") String memberId){
        return reservationMapper.getReservationsByMemberId(memberId);
    }

    //예약취소
    public void cancelReservation(@Param("reservationId") int reservationId){
        reservationMapper.cancelReservation(reservationId);
    }
    // 특정 날짜에 예약된 시간대 조회
    public List<ReservationBean> getReservedTimesByItemIdAndDate(@Param("itemId") String itemId, @Param("date") String date){
        return reservationMapper.getReservedTimesByItemIdAndDate(itemId, date) ;
    }

}
