package com.spacedong.service;

import com.spacedong.beans.ReservationBean;
import com.spacedong.repository.ReservationRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    //예약된 시간 목록 가져오기
    public List<Map<String, Integer>> getReservedTimeRangesByItemIdAndDate(
            @Param("itemId") String itemId,
            @Param("date") String date){
        return reservationRepository.getReservedTimeRangesByItemIdAndDate(itemId, date);
    }

    //예약번호 조회
    public int getReservationId(String member_id, String item_id, int start_time, Date date){
        return reservationRepository.getReservationId(member_id, item_id, start_time, date);
    }

    public int checkTimeRangeOverlap(@Param("itemId") String itemId, @Param("date") Date date, @Param("startTime") int startTime, @Param("endTime") int endTime){
        return reservationRepository.checkTimeRangeOverlap(itemId, date, startTime, endTime);
    }

    //예약생성
    public void createReservation(ReservationBean reservationBean){
        reservationRepository.createReservation(reservationBean);
    }

    //예약정보 가져오기
    public ReservationBean getReservationById(@Param("reservationId") int reservationId){
        return reservationRepository.getReservationById(reservationId);
    }

    //회원별 예약목록
    public List<ReservationBean> getReservationsByMemberId(@Param("memberId") String memberId){
        return reservationRepository.getReservationsByMemberId(memberId);
    }

    //예약취소
    public void cancelReservation(@Param("reservationId") int reservationId){
        reservationRepository.cancelReservation(reservationId);
    }

    // 특정 날짜에 예약된 시간대 조회
    public List<ReservationBean> getReservedTimesByItemIdAndDate(@Param("itemId") String itemId, @Param("date") String date){
        return reservationRepository.getReservedTimesByItemIdAndDate(itemId, date) ;
    }

}
