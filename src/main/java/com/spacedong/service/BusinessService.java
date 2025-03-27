package com.spacedong.service;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.repository.BusinessRepository;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessService {
    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);

    @Resource(name = "loginBusiness")
    public BusinessBean loginBusiness;

    @Autowired
    private BusinessRepository businessRepository;

    // 회원 관련 메서드
    public void businessJoin(BusinessBean businessBean) {
        // business_public 값을 'WAIT'로 강제 설정
        businessBean.setBusiness_public("WAIT");

        // 기타 필드 처리
        if (businessBean.getBusiness_point() < 0) {
            businessBean.setBusiness_point(0);
        }

        if (businessBean.getBusiness_info() == null) {
            businessBean.setBusiness_info("");
        }

        businessRepository.businessJoin(businessBean);
    }

    public boolean getLoginBusiness(BusinessBean businessBean) {
        BusinessBean temp = businessRepository.getLoginBusiness(businessBean);
        if (temp != null) {
            // 먼저 승인 상태를 체크합니다.
            if ("WAIT".equalsIgnoreCase(temp.getBusiness_public())) {
                logger.info("사업자 승인 대기중: {}", temp.getBusiness_id());
                return false;  // 승인 대기중이면 false를 반환
            }
            // 승인 상태(PASS)인 경우에만 세션 업데이트
            loginBusiness.setBusiness_id(temp.getBusiness_id());
            loginBusiness.setBusiness_name(temp.getBusiness_name());
            loginBusiness.setBusiness_email(temp.getBusiness_email());
            loginBusiness.setBusiness_phone(temp.getBusiness_phone());
            loginBusiness.setBusiness_address(temp.getBusiness_address());
            loginBusiness.setBusiness_number(temp.getBusiness_number());
            loginBusiness.setBusiness_profile(temp.getBusiness_profile());
            loginBusiness.setBusiness_public(temp.getBusiness_public());
            loginBusiness.setBusiness_info(temp.getBusiness_info() != null ? temp.getBusiness_info() : "");
            loginBusiness.setBusiness_point(temp.getBusiness_point());
            loginBusiness.setBusiness_pw(temp.getBusiness_pw());
            loginBusiness.setBusiness_joindate(temp.getBusiness_joindate());
            loginBusiness.setLogin(true);
            logger.info("비즈니스 로그인 성공: {}", loginBusiness.getBusiness_name());
            return true;
        }
        return false;
    }

    // 아이디 중복 확인
    public boolean checkId(String business_id) {
        int count = businessRepository.checkId(business_id);
        return count == 0; // 중복이 없으면 true, 있으면 false 반환
    }

    public BusinessBean selectBusinessById(String businessId) {
        return businessRepository.selectBusinessById(businessId);
    }

    // 이메일 중복 확인
    public boolean checkEmail(String business_email) {
        int count = businessRepository.checkEmail(business_email);
        return count == 0; // 중복이 없으면 true, 있으면 false 반환
    }

    // 회원 정보 관리 메서드
    public boolean checkPassword(String businessId, String password) {
        return businessRepository.checkPassword(businessId, password);
    }

    public void updatePassword(String businessId, String newPassword) {
        businessRepository.updatePassword(businessId, newPassword);
    }

    public void editBusiness(BusinessBean businessBean) {
        // info가 null이면 빈 문자열로 설정
        if (businessBean.getBusiness_info() == null) {
            businessBean.setBusiness_info("");
        }
        businessRepository.updateBusiness(businessBean);
    }

    public void deleteBusiness(String businessId) {
        businessRepository.deleteBusiness(businessId);
    }

    // 프로필 관리
    public void updateBusinessProfile(String businessId, String profileName) {
        businessRepository.updateBusinessProfile(businessId, profileName);
    }

    public BusinessBean getBusinessInfoById(String businessId) {
        return businessRepository.getLoginBusinessById(businessId);
    }

    public void editBusinessWithValidation(BusinessBean businessBean) {
        // 필수 필드 검증
        if (businessBean.getBusiness_id() == null || businessBean.getBusiness_id().isEmpty()) {
            throw new IllegalArgumentException("비즈니스 ID는 필수입니다.");
        }

        // 이메일 형식 검증 (정규식 사용)
        if (!isValidEmail(businessBean.getBusiness_email())) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }

        // info가 null이면 빈 문자열로 설정
        if (businessBean.getBusiness_info() == null) {
            businessBean.setBusiness_info("");
        }

        // 실제 업데이트 수행
        businessRepository.updateBusiness(businessBean);

        // 세션 업데이트
        if (loginBusiness.getBusiness_id().equals(businessBean.getBusiness_id())) {
            loginBusiness.setBusiness_phone(businessBean.getBusiness_phone());
            loginBusiness.setBusiness_address(businessBean.getBusiness_address());
            loginBusiness.setBusiness_email(businessBean.getBusiness_email());
            loginBusiness.setBusiness_info(businessBean.getBusiness_info());
        }
    }

    // 이메일 형식 검증 메서드
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    // 포인트 관련 메서드
    public void addPoints(String businessId, int points) {
        businessRepository.updateBusinessPoint(businessId, points);
        // 세션 정보 업데이트
        if (loginBusiness.getBusiness_id().equals(businessId)) {
            int currentPoints = businessRepository.getBusinessPoint(businessId);
            loginBusiness.setBusiness_point(currentPoints);
        }
    }

    public int getBusinessPoint(String businessId) {
        return businessRepository.getBusinessPoint(businessId);
    }

    // 상품 관련 메서드
    public List<BusinessItemBean> getBusinessItems(String businessId) {
        return businessRepository.getBusinessItemsByBusinessId(businessId);
    }

    public List<BusinessItemBean> getBusinessItemsByCategory(String businessId, String category) {
        return businessRepository.getBusinessItemsByCategory(businessId, category);
    }

    public BusinessItemBean getBusinessItemById(String itemId) {
        return businessRepository.getBusinessItemById(itemId);
    }

    public int registerBusinessItem(BusinessItemBean businessItemBean) {
        return businessRepository.insertBusinessItem(businessItemBean);
    }

    public boolean updateBusinessItem(BusinessItemBean businessItemBean) {
        return businessRepository.updateBusinessItem(businessItemBean) > 0;
    }

    public boolean deleteBusinessItem(String itemId) {
        return businessRepository.deleteBusinessItem(itemId) > 0;
    }

    // 게시글 관련 메서드
    public List<BoardBean> getBusinessPosts(String businessId) {
        return businessRepository.getBoardsByBusinessId(businessId);
    }

    public BoardBean getBoardById(int boardId) {
        return businessRepository.getBoardById(boardId);
    }

    public int createBoard(BoardBean boardBean) {
        return businessRepository.insertBoard(boardBean);
    }

    public boolean updateBoard(BoardBean boardBean) {
        return businessRepository.updateBoard(boardBean) > 0;
    }

    public boolean deleteBoard(int boardId) {
        return businessRepository.deleteBoard(boardId) > 0;
    }


    //아이템 등록
    public void create_item(BusinessItemBean businessItemBean){
        businessRepository.create_item(businessItemBean);
    }

    //모든 아이템 리스트
    public List<BusinessItemBean> allItem(){
        return businessRepository.allItem();
    }

    // 아이템 ID로 아이템 정보 가져오기
    public BusinessItemBean getItemById(@Param("item_id") String item_id){
        return businessRepository.getItemById(item_id);
    }

    //판매자 정보 가져오기
    public BusinessBean getBusinessById(String business_id){
        return businessRepository.getBusinessById(business_id);
    }

    // 특정 날짜에 예약된 시간대 조회
    public List<Integer> getReservedTimesByItemIdAndDate(@Param("itemId") String itemId, @Param("date") String date){
        return businessRepository.getReservedTimesByItemIdAndDate(itemId, date) ;
    }

    // 아이템 삭제
   public void deleteItem(@Param("itemId") String item_id){
        businessRepository.deleteItem(item_id);
    }

    //아이템 정보 업데이트
    public void updateItem(BusinessItemBean businessItemBean){
        businessRepository.updateItem(businessItemBean);
    }

   public Map<String, String> getItemInfo(@Param("itemId") String itemId){
        return businessRepository.getItemInfo(itemId);
    }

    //사업자 취소시 포인트 차감==========================================
    public void cancleReservation(int price, String business_id){
        businessRepository.cancleReservation(price, business_id);
    }
    public void cancleReservationMP(int price, String member_id){
        businessRepository.cancleReservationMP(price, member_id);
    }

    // 모든 판매자 목록 조회
    public List<BusinessBean> getAllBusiness() {
        return businessRepository.getAllBusiness();
    }

    // 판매자 검색
    public List<BusinessBean> searchBusiness(String searchType, String keyword) {
        return businessRepository.searchBusiness(searchType, keyword);
    }

    // 판매자 상세 정보 조회
    public BusinessBean getBusinessDetailInfo(String businessId) {
        BusinessBean business = businessRepository.getLoginBusinessById(businessId);

        // 추가 정보 설정 (예: 판매 상품 수, 총 판매액 등)
        // business.setProductCount(businessRepository.getProductCount(businessId));
        // business.setTotalSales(businessRepository.getTotalSales(businessId));

        return business;
    }
    // 판매자 상태 업데이트 메서드
    public void updateBusinessStatus(String businessId, String status) {
        // 유효한 상태 값인지 검증
        if (!status.equals("WAIT") && !status.equals("PASS")) {
            throw new IllegalArgumentException("유효하지 않은 상태 값입니다: " + status);
        }

        businessRepository.updateBusinessStatus(businessId, status);
    }
    // 대기 중인 판매자 목록만 조회
    public List<BusinessBean> getPendingBusiness() {
        return businessRepository.getAllBusiness().stream()
                .filter(business -> "WAIT".equals(business.getBusiness_public()))
                .collect(Collectors.toList());
    }

    /**
     * 이메일과 사업자 번호로 기업회원 아이디 찾기
     *
     * @param email 기업 이메일
     * @param businessNumber 사업자 등록번호
     * @return 찾은 아이디 또는 null
     */
    public String findBusinessIdByEmailAndNumber(String email, String businessNumber) {
        return businessRepository.findBusinessIdByEmailAndNumber(email, businessNumber);
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
        return businessRepository.verifyBusinessInfo(businessId, email, businessNumber);
    }

    /**
     * 휴대폰 번호로 기업회원 아이디 찾기
     *
     * @param phone 휴대폰 번호
     * @return 찾은 아이디 또는 null
     */
    public String findBusinessIdByPhone(String phone) {
        String formattedPhone = formatPhoneNumber(phone);
        System.out.println("bService formattedPhone: " + formattedPhone);
        return businessRepository.findBusinessIdByPhone(formattedPhone);
    }
    /**
     * 아이디와 휴대폰 번호로 기업회원 정보 확인 (비밀번호 찾기용)
     *
     * @param businessId 기업 아이디
     * @param phone 휴대폰 번호
     * @return 정보 일치 여부
     */
    public boolean verifyBusinessIdAndPhone(String businessId, String phone) {
        String formattedPhone = formatPhoneNumber(phone);
        System.out.println("bService formattedPhone: " + formattedPhone);
        return businessRepository.verifyBusinessIdAndPhone(businessId, formattedPhone);
    }


    /**
     * 전화번호 형식 변환 (하이픈 추가)
     * 예: 01012345678 -> 010-1234-5678
     */
    private String formatPhoneNumber(String phone) {
        System.out.println("bService 원본 phone: " + phone);

        // 이미 하이픈이 있는 경우 그대로 반환
        if (phone.contains("-")) {
            return phone;
        }
        // 길이에 따라 적절한 형식으로 변환
        if (phone.length() == 10) { // 10자리 (예: 0101231234)
            return phone.substring(0, 3) + "-" +
                    phone.substring(3, 6) + "-" +
                    phone.substring(6);
        } else if (phone.length() == 11) { // 11자리 (예: 01012345678)
            return phone.substring(0, 3) + "-" +
                    phone.substring(3, 7) + "-" +
                    phone.substring(7);
        } else {
            // 기타 형식은 그대로 반환
            return phone;
        }
    }

    //===============================================================
}