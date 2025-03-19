package com.spacedong.controller;

import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.beans.ReservationBean;
import com.spacedong.service.BusinessService;
import com.spacedong.service.ReservationService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BusinessService businessService;

    @Resource(name = "loginMember")
    private MemberBean loginMember;

    // 예약 가능 시간 확인 API
    @GetMapping("/check-availability")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam("item_id") String itemId,
            @RequestParam("date") String date, Model model) {

        System.out.println("date" + date);

        Map<String, Object> response = new HashMap<>();

        try {
            // 아이템 정보 가져오기
            BusinessItemBean item = businessService.getItemById(itemId);

            if (item == null) {
                response.put("success", false);
                response.put("error", "아이템을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미 예약된 시간 목록 가져오기 - 올바른 메소드 호출
            List<Map<String, Integer>> reservedTimeRanges = reservationService.getReservedTimeRangesByItemIdAndDate(itemId, date);

            // 로그에 출력하여 디버깅
            logger.info("예약된 시간 범위: {}", reservedTimeRanges);

            // 성공 응답 구성
            response.put("success", true);
            response.put("reservedTimeRanges", reservedTimeRanges);
            response.put("operatingHours", Map.of(
                    "start", item.getItem_starttime(),
                    "end", item.getItem_endtime()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("예약 가능 시간 확인 오류: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("error", "예약 가능 시간을 확인하는 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    // 예약 생성 처리
    @PostMapping("/create")
    public String createReservation(
            @RequestParam("item_id") String itemId,
            @RequestParam("reservation_date")Date reservationDate,
            @RequestParam("start_time") int startTime,
            @RequestParam("end_time") int endTime,
            Model model) {

        System.out.println(reservationDate);

        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return "redirect:/member/login";
        }

        // 아이템 정보 가져오기
        BusinessItemBean item = businessService.getItemById(itemId);

        if (item == null) {
            model.addAttribute("error", "존재하지 않는 아이템입니다.");
            return "business/item_not_found";
        }

        // 시간 유효성 검증
        if (startTime >= endTime) {
            model.addAttribute("error", "종료 시간은 시작 시간보다 커야 합니다.");
            return "redirect:/business/item_info?item_id=" + itemId + "&error=invalid_time_range";
        }

        // 운영 시간 내 예약인지 확인
        if (startTime < item.getItem_starttime() || endTime > item.getItem_endtime()) {
            model.addAttribute("error", "예약 시간이 운영 시간을 벗어났습니다.");
            return "redirect:/business/item_info?item_id=" + itemId + "&error=time_out_of_range";
        }

        System.out.println("오류  " + reservationDate);

        // 예약 가능 여부 확인 (시간 범위 내에 예약된 시간이 있는지)
        int overlapCount = reservationService.checkTimeRangeOverlap(
                itemId,
                reservationDate,
                startTime,
                endTime
        );

        if (overlapCount > 0) {
            model.addAttribute("error", "선택한 시간에 이미 예약이 존재합니다.");
            return "redirect:/business/item_info?item_id=" + itemId + "&error=time_already_reserved";
        }

        // 총 가격 계산 (시간당 가격 * 예약 시간)
        int hours = endTime - startTime;
        int totalPrice = item.getItem_price() * hours;

        // 예약 생성
        ReservationBean reservation = new ReservationBean();
        reservation.setItem_id(itemId);
        reservation.setMember_id(loginMember.getMember_id());
        reservation.setReservation_date(reservationDate);
        reservation.setStart_time(startTime);
        reservation.setEnd_time(endTime);
        reservation.setTotal_price(totalPrice);
        reservation.setStatus("PENDING"); // 초기 상태: 대기 중
        reservation.setCreated_at(new Date()); // 현재 시간

        int reservationId = reservationService.createReservation(reservation);

        if (reservationId > 0) {
            return "redirect:/reservation/confirmation?reservation_id=" + reservationId;
        } else {
            model.addAttribute("error", "예약 생성에 실패했습니다.");
            return "redirect:/business/item_info?item_id=" + itemId + "&error=reservation_failed";
        }
    }

    // 예약 확인 페이지
    @GetMapping("/confirmation")
    public String reservationConfirmation(
            @RequestParam("reservation_id") int reservationId,
            Model model) {

        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return "redirect:/member/login";
        }

        // 예약 정보 가져오기
        ReservationBean reservation = reservationService.getReservationById(reservationId);

        if (reservation == null || !reservation.getMember_id().equals(loginMember.getMember_id())) {
            return "redirect:/member/mypage";
        }

        // 아이템 정보 가져오기
        BusinessItemBean item = businessService.getItemById(reservation.getItem_id());

        model.addAttribute("reservation", reservation);
        model.addAttribute("item", item);

        return "reservation/confirmation";
    }

    // 내 예약 목록 보기
    @GetMapping("/my-reservations")
    public String myReservations(Model model) {
        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return "redirect:/member/login";
        }

        // 예약 목록 가져오기
        List<ReservationBean> reservations = reservationService.getReservationsByMemberId(loginMember.getMember_id());

        model.addAttribute("reservations", reservations);

        return "reservation/my_reservations";
    }

    // 예약 취소
    @PostMapping("/cancel")
    public String cancelReservation(
            @RequestParam("reservation_id") int reservationId) {

        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return "redirect:/member/login";
        }

        // 예약 정보 가져오기
        ReservationBean reservation = reservationService.getReservationById(reservationId);

        if (reservation == null || !reservation.getMember_id().equals(loginMember.getMember_id())) {
            return "redirect:/member/mypage";
        }

        // 예약 취소 (상태 변경)
        reservation.setStatus("CANCELLED");
        reservationService.cancelReservation(reservationId);

        return "redirect:/reservation/my-reservations?cancel=success";
    }
}