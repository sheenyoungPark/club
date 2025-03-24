package com.spacedong.mapper;

import com.spacedong.beans.ReservationBean;
import com.spacedong.beans.ReservationReviewBean;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReservationMapper {

    //맴버 예약 생성 (자동 증가 ID 반환)
    @Insert("INSERT INTO reservation (reservation_id, item_id, member_id, reservation_date, " +
            "start_time, end_time, total_price, status, created_at, user_type, club_id) " +
            "VALUES (reservation_id_seq.nextval, #{item_id}, #{member_id}, #{reservation_date}, " +
            "#{start_time}, #{end_time}, #{total_price}, #{status}, #{created_at}, #{user_type},  #{club_id, jdbcType=INTEGER})")
    void createReservation(ReservationBean reservationBean);

    //클럽 예약 생성
    @Insert("INSERT INTO reservation " +
            "(reservation_id, item_id, member_id, club_id, reservation_date, " +
            "start_time, end_time, total_price, status, created_at, user_type) " +
            "VALUES (reservation_id_seq.nextval, #{item_id}, #{member_id}, #{reservation_date}, " +
            "#{start_time}, #{end_time}, #{total_price}, #{status}, #{created_at}, 'club')")
    void createClubReservation(ReservationBean reservationBean);

    //대기중 예약 번호 조회
    @Select("select reservation_id from reservation where member_id = #{memberId} and item_id = #{itemId} and start_time = #{startTime} and reservation_date = #{reservationDate} and status = 'PENDING' ")
    int getReservationId(@Param("memberId") String memberId, @Param("itemId") String itemId, @Param("startTime") int startTime, @Param("reservationDate") Date reservationDate);

    // 특정 날짜에 예약된 시간대 조회 - 쉼표 추가
    @Select("SELECT DISTINCT r.item_id ,r.start_time, r.end_time " +
            "FROM reservation r " +
            "WHERE r.item_id = #{itemId} " +
            "AND r.reservation_date = to_date(#{date}, 'yyyy-MM-dd') " +
            "AND r.status != 'CANCELLED'")
    List<ReservationBean> getReservedTimesByItemIdAndDate(
            @Param("itemId") String itemId,
            @Param("date") String date);

    // 예약된 시간 목록 가져오기
    @Select("SELECT r.start_time, r.end_time " +
            "FROM reservation r " +
            "WHERE r.item_id = #{itemId} " +
            "AND r.reservation_date = to_date(#{date}, 'yyyy-MM-dd') " +
            "AND r.status != 'CANCELLED'")
    List<Map<String, Integer>> getReservedTimeRangesByItemIdAndDate(
            @Param("itemId") String itemId,
            @Param("date") String date);

    // 예약 가능 여부 확인 (시간 범위 내에 예약된 시간이 있는지)
    @Select("SELECT COUNT(*) FROM reservation " +
            "WHERE item_id = #{itemId} " +
            "AND reservation_date = #{date} " +
            "AND status != 'CANCELLED' " +
            "AND ((start_time < #{endTime} AND end_time > #{startTime}))")
    int checkTimeRangeOverlap(
            @Param("itemId") String itemId,
            @Param("date") Date date,
            @Param("startTime") int startTime,
            @Param("endTime") int endTime);

    // 예약 ID로 예약 정보 가져오기
    @Select("SELECT r.*, i.item_title, b.business_id, b.business_name " +
            "FROM reservation r " +
            "JOIN business_item i ON r.item_id = i.item_id " +
            "JOIN business b ON i.business_id = b.business_id " +
            "WHERE r.reservation_id = #{reservationId}")
    ReservationBean getReservationById(@Param("reservationId") int reservationId);


    // 회원별 예약 목록 가져오기
    @Select("SELECT r.*, i.item_title, b.business_id, b.business_name " +
            "FROM reservation r " +
            "JOIN business_item i ON r.item_id = i.item_id " +
            "JOIN business b ON i.business_id = b.business_id " +
            "WHERE r.member_id = #{memberId} " +
            "ORDER BY r.reservation_date DESC, r.start_time ASC")
    List<ReservationBean> getReservationsByMemberId(@Param("memberId") String memberId);



    // 예약 취소
    @Update("UPDATE reservation SET " +
            "status = 'CANCELLED' " +
            "WHERE reservation_id = #{reservationId}")
    void cancelReservation(@Param("reservationId") int reservationId);


    // 특정 상품 ID와 상태에 해당하는 예약
    @Select("<script>" +
            "SELECT * FROM reservation WHERE item_id IN " +
            "<foreach item='id' collection='item_id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND status = #{status}" +
            "</script>")
    List<ReservationBean> getReservationsByItemIdsAndStatus(@Param("item_id") List<Integer> item_id,@Param("status") String status);

    //예약 상태 변경
    @Update("update reservation set status = #{status} where reservation_id = #{reservation_id}")
    void  updateReservationStatus(@Param("status") String status, @Param("reservation_id")int reservation_id);

    @Select("<script>" +
            "SELECT COUNT(*) FROM reservation WHERE item_id IN " +
            "<foreach item='id' collection='item_id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND status = 'PENDING'" +
            "</script>")
    int countReservationsByItemIdsAndStatus(@Param("item_id") List<Integer> item_id);


    // 클럽이 예약한 목록 조회
    @Select("SELECT r.*, i.item_title, i.item_category " +
            "FROM reservation r " +
            "JOIN business_item i ON r.item_id = i.item_id " +
            "WHERE r.club_id = #{clubId} " +
            "ORDER BY r.reservation_date DESC, r.start_time ASC")
    List<ReservationBean> getReservationsByClubId(int clubId);

    //리뷰작성
    @Insert("INSERT INTO reservation_review " +
            "(review_id, rating, review_title, review_text, review_img, created_at) " +
            "VALUES (#{review_id}, #{rating}, #{review_title}, #{review_text}, #{review_img}, SYSDATE)")
    void insertReview(ReservationReviewBean review);

    //리뷰가져오기
    @Select("SELECT r.* " +
            "FROM reservation_review r " +
            "JOIN reservation res ON r.review_id = res.reservation_id " +
            "WHERE res.item_id = #{itemId} " +
            "ORDER BY r.created_at DESC")
    List<ReservationReviewBean> getReviewsByItemId(@Param("itemId") String itemId);

}
