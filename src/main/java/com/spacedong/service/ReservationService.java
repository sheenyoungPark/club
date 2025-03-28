package com.spacedong.service;

import com.spacedong.beans.ReservationBean;
import com.spacedong.beans.ReservationReviewBean;
import com.spacedong.mapper.ReservationMapper;
import com.spacedong.repository.ReservationRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationMapper reservationMapper;


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

    //멤버 예약생성
    public void createReservation(ReservationBean reservationBean){
        reservationRepository.createReservation(reservationBean);
    }

    //클럽 예약생성
    public void createClubReservation(ReservationBean reservationBean){
        reservationRepository.createClubReservation(reservationBean);
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

    // 특정 상품 ID와 상태에 해당하는 예약
    public List<ReservationBean> getReservationsByItemIdsAndStatus(@Param("item_id") List<Integer> item_id,@Param("status") String status){
        return reservationRepository.getReservationsByItemIdsAndStatus(item_id, status);
    }

    //예약 상태 변경
    public void  updateReservationStatus(@Param("status") String status, @Param("reservation_id")int reservation_id){
        reservationRepository.updateReservationStatus(status, reservation_id);
    }

    //대기중인 예약 수
    public int countReservationsByItemIdsAndStatus(List<Integer> business_ids){
        return reservationRepository.countReservationsByItemIdsAndStatus(business_ids);
    }

    // 클럽이 예약한 목록 조회
    public List<ReservationBean> getReservationsByClubId(Integer clubId){
        return reservationRepository.getReservationsByClubId(clubId);
    }

    //예약정보 업데이트
    public void updateReservation(ReservationBean reservation){
        reservationRepository.updateReservation(reservation);
    }

    public List<ReservationBean> getReservationsByMemberId2(String memberId) {
        List<ReservationBean> reservations = reservationMapper.getReservationsByMemberId(memberId);
        updatePastReservations(reservations);
        return reservations;
    }

    private void updatePastReservations(List<ReservationBean> reservations) {
        Date today = new Date();

        for (ReservationBean reservation : reservations) {
            if (!"PENDING".equals(reservation.getStatus()) && !"CONFIRMED".equals(reservation.getStatus())) {
                continue;
            }

            Calendar reservationEndTime = Calendar.getInstance();
            reservationEndTime.setTime(reservation.getReservation_date());
            reservationEndTime.set(Calendar.HOUR_OF_DAY, reservation.getEnd_time());

            if (today.after(reservationEndTime.getTime())) {
                reservation.setStatus("COMPLETED");
                reservationMapper.updateReservation(reservation);
            }
        }
    }
    // 리뷰 등록
    public void insertReview(ReservationReviewBean review) {
        reservationRepository.insertReview(review);
    }

    //리뷰 가져오기
    public List<ReservationReviewBean> getReviewsByItemId(String itemId) {
        return reservationRepository.getReviewsByItemId(itemId);
    }

    //리뷰 존재여부 체크
    public boolean checkReviewExists(int reservation_id) {
        return reservationRepository.checkReviewExists(reservation_id);
    }

}
