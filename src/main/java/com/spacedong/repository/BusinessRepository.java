package com.spacedong.repository;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.mapper.BusinessMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BusinessRepository {

    @Autowired
    private BusinessMapper businessMapper;

    // 회원 관련 메서드
    public void businessJoin(BusinessBean businessBean) {
        businessMapper.businessJoin(businessBean);
    }

    public BusinessBean getLoginBusiness(BusinessBean businessBean) {
        return businessMapper.getLoginBusiness(businessBean);
    }

    // 아이디 중복 검사
    public int checkId(String business_id) {
        return businessMapper.checkId(business_id);
    }

    // 이메일 중복 검사
    public int checkEmail(String business_email) {
        return businessMapper.checkEmail(business_email);
    }

    // 회원 정보 관리 메서드
    public boolean checkPassword(String businessId, String password) {
        int count = businessMapper.checkPassword(businessId, password);
        return count > 0;
    }

    public void updatePassword(String businessId, String newPassword) {
        businessMapper.updatePassword(businessId, newPassword);
    }

    public void updateBusiness(BusinessBean businessBean) {
        businessMapper.updateBusiness(businessBean);
    }

    public void deleteBusiness(String businessId) {
        businessMapper.deleteBusiness(businessId);
    }

    // 프로필 관리
    public void updateBusinessProfile(String businessId, String profileFileName) {
        businessMapper.updateBusinessProfile(businessId, profileFileName);
    }

    //아이템 등록
    public void create_item(BusinessItemBean businessItemBean){
        businessMapper.create_item(businessItemBean);
    }
    //모든 아이템 리스트
    public List<BusinessItemBean> allItem(){
       return   businessMapper.allItem();
    }

    // 아이템 ID로 아이템 정보 가져오기
    public BusinessItemBean getItemById(@Param("item_id") String item_id){
        return businessMapper.getItemById(item_id);
    }

    //판매자 정보 가져오기
    public BusinessBean getBusinessById(String business_id){
        return businessMapper.getBusinessById(business_id);
    }

    // 특정 날짜에 예약된 시간대 조회
    public List<Integer> getReservedTimesByItemIdAndDate(@Param("itemId") String itemId, @Param("date") String date){
        return businessMapper.getReservedTimesByItemIdAndDate(itemId, date) ;
    }

    // 아이템 삭제
    public void deleteItem(@Param("itemId") String item_id){
        businessMapper.deleteItem(item_id);
    }

    //아이템 정보 업데이트
    public void updateItem(BusinessItemBean businessItemBean){
        businessMapper.updateItem(businessItemBean);
    }

    public Map<String, String> getItemInfo(@Param("itemId") String itemId){
        return businessMapper.getItemInfo(itemId);
    }


    // 포인트 관련 메서드
    public void updateBusinessPoint(String businessId, int pointChange) {
        businessMapper.updateBusinessPoint(businessId, pointChange);
    }

    public int getBusinessPoint(String businessId) {
        return businessMapper.getBusinessPoint(businessId);
    }

    // 상품 관련 메서드
    public List<BusinessItemBean> getBusinessItemsByBusinessId(String businessId) {
        return businessMapper.selectBusinessItemsByBusinessId(businessId);
    }

    public List<BusinessItemBean> getBusinessItemsByCategory(String businessId, String category) {
        return businessMapper.selectBusinessItemsByCategory(businessId, category);
    }

    public BusinessItemBean getBusinessItemById(String itemId) {
        return businessMapper.selectBusinessItemById(itemId);
    }

    public int insertBusinessItem(BusinessItemBean businessItemBean) {
        return businessMapper.insertBusinessItem(businessItemBean);
    }

    public int updateBusinessItem(BusinessItemBean businessItemBean) {
        return businessMapper.updateBusinessItem(businessItemBean);
    }

    public int deleteBusinessItem(String itemId) {
        return businessMapper.deleteBusinessItem(itemId);
    }

    // 게시글 관련 메서드
    public List<BoardBean> getBoardsByBusinessId(String businessId) {
        return businessMapper.selectBoardsByBusinessId(businessId);
    }

    public BoardBean getBoardById(int boardId) {
        return businessMapper.selectBoardById(boardId);
    }

    public int insertBoard(BoardBean boardBean) {
        businessMapper.insertBoard(boardBean);
        return boardBean.getBoard_id(); // MyBatis에서 auto-increment된 ID 반환
    }

    public int updateBoard(BoardBean boardBean) {
        return businessMapper.updateBoard(boardBean);
    }

    public int deleteBoard(int boardId) {
        return businessMapper.deleteBoard(boardId);
    }
    public BusinessBean getLoginBusinessById(String businessId) {
        return businessMapper.getBusinessInfoById(businessId);
    }

    public BusinessBean selectBusinessById(String businessId) {
        return businessMapper.selectBusinessById(businessId);
    }

    //사업자 취소시 포인트 차감==========================================
    public void cancleReservation(int price, String business_id){
        businessMapper.cancleReservation(price, business_id);
    }
    public void cancleReservationMP(int price, String member_id){
        businessMapper.cancleReservationMP(price, member_id);
    }

    // 모든 판매자 목록 조회
    public List<BusinessBean> getAllBusiness() {
        return businessMapper.getAllBusiness();
    }

    // 판매자 검색
    public List<BusinessBean> searchBusiness(String searchType, String keyword) {
        return businessMapper.searchBusiness(searchType, keyword);
    }

    // 판매자 상태 업데이트
    public void updateBusinessStatus(String businessId, String status) {
        businessMapper.updateBusinessStatus(businessId, status);
    }

    /**
     * 이메일과 사업자 번호로 기업회원 아이디 찾기
     *
     * @param email 기업 이메일
     * @param businessNumber 사업자 등록번호
     * @return 찾은 아이디 또는 null
     */
    public String findBusinessIdByEmailAndNumber(String email, String businessNumber) {
        return businessMapper.findBusinessIdByEmailAndNumber(email, businessNumber);
    }

    /**
     * 기업회원 정보 확인 (비밀번호 찾기용)
     *
     * @param businessId 기업 아이디
     * @param email 기업 이메일
     * @param businessNumber 사업자 등록번호
     * @return 정보 일치 여부
     */
    public boolean verifyBusinessInfo(String businessId, String email, String businessNumber) {
        int count = businessMapper.verifyBusinessInfo(businessId, email, businessNumber);
        return count > 0;
    }

    /**
     * 휴대폰 번호로 기업회원 아이디 찾기
     */
    public String findBusinessIdByPhone(String phone) {
        return businessMapper.findBusinessIdByPhone(phone);
    }

    /**
     * 아이디와 휴대폰 번호로 기업회원 정보 확인
     */
    public boolean verifyBusinessIdAndPhone(String businessId, String phone) {
        int count = businessMapper.verifyBusinessIdAndPhone(businessId, phone);
        return count > 0;
    }
    public void clubReservationMP(@Param("price") int price,@Param("club_id)") int club_id){
        businessMapper.clubReservationMP(price, club_id);
    }


    //===============================================================
}