package com.spacedong.mapper;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.ReservationBean;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;


@Mapper
public interface BusinessMapper {


    @Insert("insert into business(business_id, business_pw, business_name, business_email, business_phone, business_address,business_number, business_joindate, business_public) "
            + "values(#{business_id}, #{business_pw}, #{business_name}, #{business_email}, #{business_phone}, #{business_address}, #{business_number}, sysdate,'WAIT')")
    public void businessJoin(BusinessBean businessBean);

    @Select("select * from business where business_id = #{business_id} and business_pw = #{business_pw}")
    BusinessBean getLoginMember(BusinessBean businessBean);

    // 아이디 중복 검사
    @Select("SELECT COUNT(*) FROM business WHERE business_id = #{business_id}")
    int checkId(String business_id);

    @Select("SELECT COUNT(*) FROM business WHERE business_email = #{business_email}")
    int checkEmail(String business_email);

    //아이템 등록
    @Insert("insert into business_item(business_id, item_id, item_title, item_text, item_price, item_category, item_img, item_starttime, item_endtime) " +
            "values( #{business_id}, item_id_seq.nextval, #{item_title}, #{item_text}, #{item_price}, #{item_category}, #{item_img}, #{item_starttime},#{item_endtime})")
    void create_item(BusinessItemBean businessItemBean);

    //클럽 아이템 리스트
    @Select("select * from business_item")
    List<BusinessItemBean> allItem();

    // 아이템 ID로 아이템 정보 가져오기
    @Select("SELECT * FROM business_item WHERE item_id = #{item_id}")
    BusinessItemBean getItemById(@Param("item_id") String item_id);

    // 판매자 ID로 아이템 목록 가져오기
    @Select("SELECT * FROM business_item WHERE business_id = #{business_id} ORDER BY item_id DESC")
    List<BusinessItemBean> getItemsByBusinessId(@Param("business_id") String business_id);

    //판매자 정보 가져오기
    @Select("select * from business where business_id = #{business_id}")
    BusinessBean getBusinessById(String business_id);

    // 특정 날짜에 예약된 시간대 조회
    @Select("SELECT DISTINCT r.start_time " +
            "FROM reservation r " +
            "WHERE r.item_id = #{item_id} " +
            "AND r.reservation_date = #{date}")
    List<Integer> getReservedTimesByItemIdAndDate(@Param("item_id") String itemId, @Param("date") String date);

    // 아이템 삭제
    @Delete("DELETE FROM business_item WHERE item_id = #{item_id}")
    void deleteItem(@Param("item_id") String item_id);


    // 아이템 정보 업데이트
    @Update("UPDATE business_item SET " +
            "item_title = #{item_title}, " +
            "item_text = #{item_text}, " +
            "item_price = #{item_price}, " +
            "item_category = #{item_category}, " +
            "item_starttime = #{item_starttime}, " +
            "item_endtime = #{item_endtime} " +
             "item_img = #{item_img}" +
            "WHERE item_id = #{item_id}")
    void updateItem(BusinessItemBean businessItemBean);


    // 예약 ID로 예약 정보 가져오기
    @Select("SELECT r.*, i.item_title, b.business_id, b.business_name " +
            "FROM reservation r " +
            "JOIN business_item i ON r.item_id = i.item_id " +
            "JOIN business b ON i.business_id = b.business_id " +
            "WHERE r.reservation_id = #{reservationId}")
    ReservationBean getReservationById(@Param("reservationId") int reservationId);

    @Select("SELECT i.item_title, b.business_name " +
            "FROM business_item i " +
            "JOIN business b ON i.business_id = b.business_id " +
            "WHERE i.item_id = #{itemId}")
    Map<String, String> getItemInfo(@Param("itemId") String itemId);




}
