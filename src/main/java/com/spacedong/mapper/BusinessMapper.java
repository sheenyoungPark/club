package com.spacedong.mapper;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.BoardBean;
import com.spacedong.beans.ReservationBean;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BusinessMapper {

    @Insert("insert into business(business_id, business_pw, business_name, business_email, business_phone, business_address, " +
            "business_number, business_joindate, business_public, business_info, business_point) " +
            "values(#{business_id}, #{business_pw}, #{business_name}, #{business_email}, #{business_phone}, #{business_address}, " +
            "#{business_number}, sysdate, #{business_public}, #{business_info, jdbcType=VARCHAR}, #{business_point})")
    void businessJoin(BusinessBean businessBean);

    @Select("select * from business where business_id = #{business_id, jdbcType=VARCHAR} and business_pw = #{business_pw, jdbcType=VARCHAR}")
    BusinessBean getLoginBusiness(BusinessBean businessBean);

    // 아이디 중복 검사
    @Select("SELECT COUNT(*) FROM business WHERE business_id = #{business_id}")
    int checkId(String business_id);

    // 이메일 중복 검사
    @Select("SELECT COUNT(*) FROM business WHERE business_email = #{business_email}")
    int checkEmail(String business_email);

    //아이템 등록
    @Insert("insert into business_item(business_id, item_id, item_title, item_text, item_price, item_category, item_img, item_starttime, item_endtime) " +
            "values( #{business_id}, item_id_seq.nextval, #{item_title}, #{item_text}, #{item_price}, #{item_category}, #{item_img}, #{item_starttime},#{item_endtime})")
    void create_item(BusinessItemBean businessItemBean);

    // 수정 후:
    @Select("SELECT bi.* FROM business_item bi " +
            "JOIN business b ON bi.business_id = b.business_id " +
            "WHERE b.business_public = 'PASS'")
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


    // 아이템 정보 업데이트 - 콤마 오류 수정
    @Update("UPDATE business_item SET " +
            "item_title = #{item_title}, " +
            "item_text = #{item_text}, " +
            "item_price = #{item_price}, " +
            "item_category = #{item_category}, " +
            "item_starttime = #{item_starttime}, " +
            "item_endtime = #{item_endtime}, " +
            "item_img = #{item_img} " +
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

    @Select("SELECT * FROM business WHERE (business_id LIKE '%'||#{keyword}||'%' OR business_name LIKE '%'||#{keyword}||'%') " +
            "AND business_public = 'PASS'")
    List<BusinessBean> searchBusinessByKeyword(String keyword);



    // 비밀번호 확인
    @Select("SELECT COUNT(*) FROM business WHERE business_id = #{param1} AND business_pw = #{param2}")
    int checkPassword(String businessId, String password);

    // 비밀번호 변경
    @Update("UPDATE business SET business_pw = #{param2} WHERE business_id = #{param1}")
    void updatePassword(String businessId, String newPassword);

    @Update("UPDATE business SET business_email = #{business_email, jdbcType=VARCHAR}, " +
            "business_phone = #{business_phone, jdbcType=VARCHAR}, " +
            "business_address = #{business_address, jdbcType=VARCHAR}, " +
            "business_info = #{business_info, jdbcType=VARCHAR} " +
            "WHERE business_id = #{business_id, jdbcType=VARCHAR}")
    void updateBusiness(BusinessBean businessBean);

    @Select("SELECT * FROM business WHERE business_id = #{businessId}")
    BusinessBean getBusinessInfoById(String businessId);

    // 회원 탈퇴
    @Delete("DELETE FROM business WHERE business_id = #{businessId}")
    void deleteBusiness(String businessId);

    // 프로필 이미지 업데이트
    @Update("UPDATE business SET business_profile = #{param2} WHERE business_id = #{param1}")
    void updateBusinessProfile(String businessId, String profileFileName);

    // 포인트 업데이트
    @Update("UPDATE business SET business_point = business_point + #{param2} WHERE business_id = #{param1}")
    void updateBusinessPoint(String businessId, int pointChange);

    // 포인트 조회
    @Select("SELECT business_point FROM business WHERE business_id = #{businessId}")
    int getBusinessPoint(String businessId);

    // 사업자별 아이템 조회 - 간단한 쿼리 (오류 방지)
    @Select("SELECT * FROM business_item WHERE business_id = #{business_id}")
    List<BusinessItemBean> selectBusinessItemsByBusinessId(String business_id);

    // 카테고리별 상품 조회
    @Select("SELECT * FROM business_item WHERE business_id = #{param1} AND item_category = #{param2}")
    List<BusinessItemBean> selectBusinessItemsByCategory(String businessId, String category);

    // 상품 상세 조회
    @Select("SELECT * FROM business_item WHERE item_id = #{itemId}")
    BusinessItemBean selectBusinessItemById(String itemId);

    // 상품 등록
    @Insert("INSERT INTO business_item (business_id, item_id, item_title, item_text, item_price, item_img, " +
            "item_starttime, item_endtime, item_category) " +
            "VALUES (#{business_id}, #{item_id}, #{item_title}, #{item_text}, #{item_price}, #{item_img}, " +
            "#{item_starttime}, #{item_endtime}, #{item_category})")
    int insertBusinessItem(BusinessItemBean businessItemBean);

    // 상품 수정
    @Update("UPDATE business_item SET item_title = #{item_title}, item_text = #{item_text}, " +
            "item_price = #{item_price}, item_img = #{item_img}, " +
            "item_starttime = #{item_starttime}, item_endtime = #{item_endtime}, " +
            "item_category = #{item_category} " +
            "WHERE item_id = #{item_id} AND business_id = #{business_id}")
    int updateBusinessItem(BusinessItemBean businessItemBean);

    // 상품 삭제
    @Delete("DELETE FROM business_item WHERE item_id = #{itemId}")
    int deleteBusinessItem(String itemId);

    // 사업자별 게시글 조회 쿼리 수정
    @Select("SELECT * FROM business_board WHERE board_writer_id = #{businessId} ORDER BY create_date DESC")
    List<BoardBean> selectBoardsByBusinessId(String businessId);

    // 게시글 상세 조회
    @Select("SELECT * FROM business_board WHERE board_id = #{boardId}")
    BoardBean selectBoardById(int boardId);

    // Oracle에서 시퀀스를 사용하는 방식으로 변경
    @Insert("INSERT INTO business_board (board_id, board_title, board_text, board_writer_id, board_view, board_like, create_date) " +
            "VALUES (board_id_seq.NEXTVAL, #{board_title}, #{board_text}, #{board_writer_id}, 0, 0, SYSDATE)")
    void insertBoard(BoardBean boardBean);

    // 시퀀스의 현재 값을 가져오는 쿼리 추가
    @Select("SELECT board_id_seq.CURRVAL FROM dual")
    int getLastInsertedBoardId();

    // 게시글 수정
    @Update("UPDATE business_board SET board_title = #{board_title}, board_text = #{board_text}, " +
            "update_date = SYSDATE " +
            "WHERE board_id = #{board_id} AND board_writer_id = #{board_writer_id}")
    int updateBoard(BoardBean boardBean);

    // 게시글 삭제
    @Delete("DELETE FROM business_board WHERE board_id = #{boardId}")
    int deleteBoard(int boardId);

    @Select("SELECT business_id, business_pw, business_name, business_profile " +
            "FROM business " +
            "WHERE business_id = #{businessId}")
    BusinessBean selectBusinessById(String businessId);

    //사업자 취소시 포인트 차감==========================================
    @Update("update business set business_point = business_point - #{price} where business_id = #{business_id}")
    void cancleReservation(int price, String business_id);

    @Update("update Member set Member_point = Member_point + #{price} where member_id = #{member_id}")
    void cancleReservationMP(int price, String member_id);

    // 모든 판매자 목록 조회
    @Select("SELECT * FROM business ORDER BY business_joindate DESC")
    List<BusinessBean> getAllBusiness();

    // 판매자 검색
    @Select({"<script>",
            "SELECT * FROM business",
            "<where>",
            "  <if test='searchType == \"name\"'>",
            "    business_name LIKE '%'||#{keyword}||'%'",
            "  </if>",
            "  <if test='searchType == \"business\"'>",
            "    business_id LIKE '%'||#{keyword}||'%'",
            "  </if>",
            "  <if test='searchType == \"category\"'>",
            "    EXISTS (SELECT 1 FROM business_item WHERE business_id = business.business_id AND item_category LIKE '%'||#{keyword}||'%')",
            "  </if>",
            "</where>",
            "ORDER BY business_joindate DESC",
            "</script>"})
    List<BusinessBean> searchBusiness(@Param("searchType") String searchType, @Param("keyword") String keyword);

    // 판매자 상태 업데이트
    @Update("UPDATE business SET business_public = #{param2} WHERE business_id = #{param1}")
    void updateBusinessStatus(String businessId, String status);

    @Select("SELECT COUNT(*) FROM business")
    int getBusinessCount();
    @Select("SELECT COUNT(*) FROM business WHERE TRUNC(business_joindate, 'MM') < TRUNC(SYSDATE, 'MM')")
    int getPreviousMonthBusinessCount();
    @Select("SELECT COUNT(*) FROM business WHERE EXTRACT(MONTH FROM business_joindate) = #{month} AND EXTRACT(YEAR FROM business_joindate) = EXTRACT(YEAR FROM SYSDATE)")
    int getBusinessCountByMonth(int month);

    //===============================================================
}