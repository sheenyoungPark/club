package com.spacedong.controller;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.beans.ReservationBean;
import com.spacedong.mapper.BusinessMapper;
import com.spacedong.service.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/business/reservations")
public class BusinessReservationController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ItemService businessItemService;

    @Autowired
    private MemberService memberService;

    @Resource(name = "loginBusiness")
    private BusinessBean loginBusiness;

    @Autowired
    private BusinessService businessService;

    // 통합 예약 관리 페이지 (대기 + 완료)
    @GetMapping("")
    public String allReservations(Model model) {
        // 로그인한 사업자 정보 확인
        if (loginBusiness == null) {
            return "redirect:/business/login";
        }

        // 해당 사업자의 상품 ID 목록 조회
        List<Integer> businessItemIds = businessItemService.getItemIdsByBusinessId(loginBusiness.getBusiness_id());

        // 대기 중인 예약 목록 조회 (상태가 'PENDING'인 예약)
        List<ReservationBean> waitingReservationsList = reservationService.getReservationsByItemIdsAndStatus(
                businessItemIds, "PENDING");

        // 완료된 예약 목록 조회 (상태가 'CONFIRMED' 또는 'CANCELLED'인 예약)
        List<ReservationBean> confirmedReservationsList = reservationService.getReservationsByItemIdsAndStatus(
                businessItemIds, "CONFIRMED");
        List<ReservationBean> cancelledReservationsList = reservationService.getReservationsByItemIdsAndStatus(
                businessItemIds, "CANCELLED");

        // 대기중인 예약 목록
        List<Map<String, Object>> waitingReservations = processReservations(waitingReservationsList);

        // 완료된 예약 목록 (확정 + 취소)
        List<Map<String, Object>> completedReservations = new ArrayList<>();
        completedReservations.addAll(processReservations(confirmedReservationsList));
        completedReservations.addAll(processReservations(cancelledReservationsList));

        model.addAttribute("waitingReservations", waitingReservations);
        model.addAttribute("completedReservations", completedReservations);

        return "business/reservations";
    }

    // 대기 중인 예약 목록 페이지
    @GetMapping("/waiting")
    public String waitingReservations(Model model) {
        // 로그인한 사업자 정보 가져오기
        if (loginBusiness == null) {
            return "redirect:/business/login";
        }

        // 해당 사업자의 상품 ID 목록 조회
        List<Integer> businessItemIds = businessItemService.getItemIdsByBusinessId(loginBusiness.getBusiness_id());

        // 대기 중인 예약 목록 조회 (상태가 'PENDING'인 예약)
        List<ReservationBean> waitingReservationsList = reservationService.getReservationsByItemIdsAndStatus(
                businessItemIds, "PENDING");

        // 완료된 예약 목록 조회 (상태가 'CONFIRMED' 또는 'CANCELLED'인 예약)
        List<ReservationBean> confirmedReservationsList = reservationService.getReservationsByItemIdsAndStatus(
                businessItemIds, "CONFIRMED");
        List<ReservationBean> cancelledReservationsList = reservationService.getReservationsByItemIdsAndStatus(
                businessItemIds, "CANCELLED");

        // 예약 정보 처리
        List<Map<String, Object>> waitingReservations = processReservations(waitingReservationsList);

        // 완료된 예약 목록 처리
        List<Map<String, Object>> completedReservations = new ArrayList<>();
        completedReservations.addAll(processReservations(confirmedReservationsList));
        completedReservations.addAll(processReservations(cancelledReservationsList));

        model.addAttribute("waitingReservations", waitingReservations);
        model.addAttribute("completedReservations", completedReservations);

        return "business/waiting_reservations";
    }

    // 예약을 승인합니다. (상태를 'CONFIRMED'로 변경)
    @PostMapping("/approve")
    public String approveReservation(@RequestParam("reservation_id") int reservationId) {
        // 로그인한 사업자 정보 가져오기
        if (loginBusiness == null) {
            return "redirect:/business/login";
        }

        // 예약 정보 조회
        ReservationBean reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            return "redirect:/business/reservations/waiting?error=not_found";
        }

        // 해당 사업자의 상품인지 확인
        BusinessItemBean item = businessItemService.getItemById(reservation.getItem_id());
        if (!item.getBusiness_id().equals(loginBusiness.getBusiness_id())) {
            return "redirect:/business/reservations/waiting?error=unauthorized";
        }

        // 예약 상태 변경 (대기중 -> 확정됨)
        reservationService.updateReservationStatus("CONFIRMED", reservationId);
        paymentService.businessAddPoint(reservation.getTotal_price(), loginBusiness.getBusiness_id());

        BusinessBean bs = businessService.getBusinessById(loginBusiness.getBusiness_id());
        loginBusiness.setBusiness_point(bs.getBusiness_point());

        return "redirect:/business/reservations/waiting?success=approved";
    }

    // 예약을 거절합니다. (상태를 'CANCELLED'로 변경)
    @PostMapping("/decline")
    public String declineReservation(@RequestParam("reservation_id") int reservationId) {
        // 로그인한 사업자 정보 가져오기
        if (loginBusiness == null) {
            return "redirect:/business/login";
        }

        // 예약 정보 조회
        ReservationBean reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            return "redirect:/business/reservations/waiting?error=not_found";
        }

        // 해당 사업자의 상품인지 확인
        BusinessItemBean item = businessItemService.getItemById(reservation.getItem_id());
        if (!item.getBusiness_id().equals(loginBusiness.getBusiness_id())) {
            return "redirect:/business/reservations/waiting?error=unauthorized";
        }

        if (reservation.getClub_id() != null){
            reservationService.updateReservationStatus("CANCELLED", reservationId);
            businessService.clubReservationMP(reservation.getTotal_price(), reservation.getClub_id());
            return "redirect:/business/reservations/waiting?success=declined";
        }else {
        // 예약 상태 변경 (대기중 -> 취소됨)
        reservationService.updateReservationStatus("CANCELLED", reservationId);
        businessService.cancleReservationMP(reservation.getTotal_price(), reservation.getMember_id());
        return "redirect:/business/reservations/waiting?success=declined";
        }
    }

    // 확정된 예약을 취소합니다
    @PostMapping("/cancel")
    public String cancelReservation(@RequestParam("reservation_id") int reservationId,
                                    @RequestParam(value = "returnUrl", defaultValue = "waiting") String returnUrl) {
        // 로그인한 사업자 정보 가져오기
        if (loginBusiness == null) {
            return "redirect:/business/login";
        }

        // 예약 정보 조회
        ReservationBean reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            return "redirect:/business/reservations/" + returnUrl + "?error=not_found";
        }

        // 해당 사업자의 상품인지 확인
        BusinessItemBean item = businessItemService.getItemById(reservation.getItem_id());
        if (!item.getBusiness_id().equals(loginBusiness.getBusiness_id())) {
            return "redirect:/business/reservations/" + returnUrl + "?error=unauthorized";
        }

        // 예약이 확정된 상태인지 확인
        if (!"CONFIRMED".equals(reservation.getStatus())) {
            return "redirect:/business/reservations/" + returnUrl + "?error=invalid_status";
        }

        // 예약 상태 변경 (확정됨 -> 취소됨)
        reservationService.updateReservationStatus("CANCELLED", reservationId);

        // 사업자 포인트 차감
        businessService.cancleReservation(reservation.getTotal_price(), loginBusiness.getBusiness_id());

        // 회원에게 포인트 환불
        businessService.cancleReservationMP(reservation.getTotal_price(), reservation.getMember_id());

        // 세션 업데이트
        BusinessBean bs = businessService.getBusinessById(loginBusiness.getBusiness_id());
        loginBusiness.setBusiness_point(bs.getBusiness_point());

        return "redirect:/business/reservations/" + returnUrl + "?success=cancelled";
    }

    // 대기 중인 예약 수를 조회하는 메서드
    public int getWaitingReservationsCount(String businessId) {
        // 해당 사업자의 상품 ID 목록 조회
        List<Integer> businessItemIds = businessItemService.getItemIdsByBusinessId(businessId);

        // 대기 중인 예약 수 조회
        return reservationService.countReservationsByItemIdsAndStatus(businessItemIds);
    }

    // 예약 정보 처리 헬퍼 메서드
    private List<Map<String, Object>> processReservations(List<ReservationBean> reservationsList) {
        List<Map<String, Object>> processedReservations = new ArrayList<>();

        for (ReservationBean reservation : reservationsList) {
            // 상품 정보 조회
            BusinessItemBean item = businessItemService.getItemById(reservation.getItem_id());

            // 예약자 정보 조회
            MemberBean member = memberService.getMemberById(reservation.getMember_id());

            // 필요한 정보를 맵에 추가
            Map<String, Object> reservationDetails = new HashMap<>();
            reservationDetails.put("reservation_id", reservation.getReservation_id());
            reservationDetails.put("item_id", reservation.getItem_id());
            reservationDetails.put("itemTitle", item != null ? item.getItem_title() : "알 수 없는 상품");
            reservationDetails.put("member_id", reservation.getMember_id());
            reservationDetails.put("memberName", member != null ? member.getMember_name() : "알 수 없는 회원");
            reservationDetails.put("reservation_date", reservation.getReservation_date());
            reservationDetails.put("start_time", reservation.getStart_time());
            reservationDetails.put("end_time", reservation.getEnd_time());
            reservationDetails.put("total_price", reservation.getTotal_price());
            reservationDetails.put("status", reservation.getStatus());
            reservationDetails.put("created_at", reservation.getCreated_at());

            processedReservations.add(reservationDetails);
        }

        return processedReservations;
    }
}