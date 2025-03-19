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
}