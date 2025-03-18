package com.spacedong.mapper;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.BoardBean;
import org.apache.ibatis.annotations.*;

import java.util.List;

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

    // 상품 조회 (해당 사업자의 모든 상품)
    @Select("SELECT * FROM business_item WHERE business_id = #{businessId}")
    List<BusinessItemBean> selectBusinessItemsByBusinessId(String businessId);

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

    // 게시글 조회 (해당 사업자의 모든 게시글)
    @Select("SELECT * FROM business_board WHERE board_writer_id = #{businessId}")
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
}