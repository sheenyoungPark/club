package com.spacedong.repository;

import com.spacedong.beans.ReservationBean;
import com.spacedong.beans.ReservationReviewBean;
import com.spacedong.mapper.ReservationMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    //멤버 예약생성
    public void createReservation(ReservationBean reservationBean){
       reservationMapper.createReservation(reservationBean);
    }
    //클럽 예약생성
    public void createClubReservation(ReservationBean reservationBean){
        reservationMapper.createClubReservation(reservationBean);
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

    // 특정 상품 ID와 상태에 해당하는 예약
    public List<ReservationBean> getReservationsByItemIdsAndStatus(@Param("item_id")List<Integer> item_id,@Param("status") String status){
        return reservationMapper.getReservationsByItemIdsAndStatus(item_id, status);
    }
    //예약 상태 변경
    public void  updateReservationStatus(@Param("status") String status, @Param("reservation_id")int reservation_id){
        reservationMapper.updateReservationStatus(status, reservation_id);
    }

    //대기중인 예약 수
    public int countReservationsByItemIdsAndStatus(List<Integer> business_ids){
        return reservationMapper.countReservationsByItemIdsAndStatus(business_ids);
    }

    // 클럽이 예약한 목록 조회
    public List<ReservationBean> getReservationsByClubId(Integer clubId){
        return reservationMapper.getReservationsByClubId(clubId);
    }

    //예약정보 업데이트
    public void updateReservation(ReservationBean reservation){
        reservationMapper.updateReservation(reservation);
    }


    // 리뷰 등록
    public void insertReview(ReservationReviewBean review) {
        reservationMapper.insertReview(review);
    }

    //리뷰가져오기
    public List<ReservationReviewBean> getReviewsByItemId(String itemId) {
        return reservationMapper.getReviewsByItemId(itemId);
    }


    public boolean checkReviewExists(int reservationId) {
        return reservationMapper.countReviewByReservationId(reservationId) >0;
    }
}
