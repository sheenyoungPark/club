package com.spacedong.controller;

import com.spacedong.beans.*;
import com.spacedong.service.BusinessService;
import com.spacedong.service.ClubService;
import com.spacedong.service.MemberService;
import com.spacedong.service.PaymentService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
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

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ClubService clubService;

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

    // 사용자가 마스터인 클럽 목록 API
    @GetMapping("/api/clubs/master")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getMasterClubs() {
        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // 사용자가 마스터인 클럽 목록 조회
            List<Map<String, Object>> masterClubs = clubService.getMasterClubsByMemberId(loginMember.getMember_id());
            return ResponseEntity.ok(masterClubs);
        } catch (Exception e) {
            logger.error("마스터 클럽 목록 조회 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // 예약 생성 처리
    @PostMapping("/create")
    public String createReservation(
            @RequestParam("item_id") String itemId,
            @RequestParam("reservation_date") Date reservationDate,
            @RequestParam("start_time") int startTime,
            @RequestParam("end_time") int endTime,
            @RequestParam(value = "user_type", defaultValue = "member") String userType,
            @RequestParam(value = "club_id", required = false) Integer clubId,
            @RequestParam(value = "reservation_people", defaultValue = "1") int reservationPeople,
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

        // 예약 정보 생성
        ReservationBean reservation = new ReservationBean();
        reservation.setItem_id(itemId);
        reservation.setMember_id(loginMember.getMember_id());
        reservation.setReservation_date(reservationDate);
        reservation.setStart_time(startTime);
        reservation.setEnd_time(endTime);
        reservation.setTotal_price(totalPrice);
        reservation.setStatus("PENDING"); // 초기 상태: 대기 중
        reservation.setCreated_at(new Date()); // 현재 시간
        reservation.setUser_type(userType); // 예약 유형 설정

        // 클럽 또는 개인 예약 처리
        if ("club".equals(userType) && clubId != null) {
            // 클럽 예약 처리

            // 회원이 클럽의 마스터인지 확인
            boolean isMaster = clubService.isClubMaster(loginMember.getMember_id(), clubId);
            if (!isMaster) {
                model.addAttribute("error", "해당 클럽의 마스터 권한이 없습니다.");
                return "redirect:/business/item_info?item_id=" + itemId + "&errorInteger=not_club_master";
            }

            // 클럽 정보 가져오기
            ClubBean club = clubService.oneClubInfo(clubId);
            if (club == null) {
                model.addAttribute("error", "존재하지 않는 클럽입니다.");
                return "redirect:/business/item_info?item_id=" + itemId + "&error=club_not_found";
            }

            // 클럽 포인트 확인
            if (club.getClub_point() < totalPrice) {
                model.addAttribute("error", "클럽 포인트가 부족합니다.");
                return "redirect:/business/item_info?item_id=" + itemId + "&error=enough_point";
            }

            // 클럽 포인트 차감
            clubService.updateClubPoints(clubId, -totalPrice);

            // 예약 정보에 클럽 ID 설정
            reservation.setClub_id(clubId);
            String clubType = "club";
            reservation.setUser_type(clubType);

        } else {
            // 개인 예약 처리
            if (loginMember.getMember_point() < totalPrice) {
                model.addAttribute("error", "포인트가 부족합니다.");
                return "redirect:/business/item_info?item_id=" + itemId + "&error=enough_point";
            } else {
                String memberType = "member";
                reservation.setUser_type(memberType);
                paymentService.payMoney(totalPrice, loginMember.getMember_id());
                loginMember.setMember_point(loginMember.getMember_point() - totalPrice);
                // paymentService.businessAddPoint(totalPrice, item.getBusiness_id());
            }
        }


        // 예약 생성
        reservationService.createReservation(reservation);
        int reservationId = reservationService.getReservationId(reservation.getMember_id(), reservation.getItem_id(), reservation.getStart_time(), reservation.getReservation_date());

        return "redirect:/reservation/confirmation?reservation_id=" + reservationId;
    }

    // 예약 확인 페이지
    @GetMapping("/confirmation")
    public String reservationConfirmation(
            @RequestParam("reservation_id") int reservationId,
            Model model) {

        System.out.println("Res" + reservationId);

        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return "redirect:/member/login";
        }

        // 예약 정보 가져오기
        ReservationBean reservation = reservationService.getReservationById(reservationId);

        System.out.println(reservation.getReservation_id());
        System.out.println(reservation.getMember_id());


        if (reservation == null || !reservation.getMember_id().equals(loginMember.getMember_id())) {
            return "redirect:/member/memberinfo";
        }

        // 아이템 정보 가져오기
        BusinessItemBean item = businessService.getItemById(reservation.getItem_id());

        // 클럽 예약인 경우 클럽 정보 추가
        if ("club".equals(reservation.getUser_type()) && reservation.getClub_id() > 0) {
            ClubBean club = clubService.oneClubInfo(reservation.getClub_id());
            model.addAttribute("club", club);
        }

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
        List<ReservationBean> reservations = reservationService.getReservationsByMemberId2(loginMember.getMember_id());

        // 각 예약에 대해 리뷰 존재 여부 확인
        Map<Integer, Boolean> reviewExistsMap = new HashMap<>();
        for (ReservationBean r : reservations) {
            boolean hasReview = reservationService.checkReviewExists(r.getReservation_id());
            reviewExistsMap.put(r.getReservation_id(), hasReview);
        }

        model.addAttribute("reservations", reservations);
        model.addAttribute("reviewExistsMap", reviewExistsMap);

        return "reservation/my_reservations";
    }

    // 클럽 예약 목록 보기
    @GetMapping("/club-reservations")
    public String clubReservations(
            @RequestParam("club_id") int clubId,
            Model model) {
        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return "redirect:/member/login";
        }

        // 클럽 마스터 권한 확인
        boolean isMaster = clubService.isClubMaster(loginMember.getMember_id(), clubId);
        if (!isMaster) {
            return "redirect:/club/my-clubs";
        }

        // 클럽 정보 가져오기
        ClubBean club = clubService.oneClubInfo(clubId);

        // 클럽 예약 목록 가져오기
        List<ReservationBean> reservations = reservationService.getReservationsByClubId(clubId);

        // ✅ 리뷰 존재 여부를 Map으로 만들어서 전달
        Map<Integer, Boolean> reviewExistsMap = new HashMap<>();
        for (ReservationBean r : reservations) {
            boolean hasReview = reservationService.checkReviewExists(r.getReservation_id());
            reviewExistsMap.put(r.getReservation_id(), hasReview);
        }

        model.addAttribute("club", club);
        model.addAttribute("clubReservation", reservations);
        model.addAttribute("reviewExistsMap", reviewExistsMap);

        return "reservation/club_reservations";
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

        BusinessItemBean businessItemBean = businessService.getItemById(reservation.getItem_id());

        if (reservation == null || !reservation.getMember_id().equals(loginMember.getMember_id())) {
            return "redirect:/member/memberinfo";
        }

        // 클럽 예약인 경우 클럽 포인트 환불
        if ("club".equals(reservation.getUser_type()) && reservation.getClub_id() > 0) {
            // 클럽 포인트 환불
            clubService.updateClubPoints(reservation.getClub_id(), reservation.getTotal_price());
        } else {
            // 개인 예약인 경우 회원 포인트 환불
            //취소시 비지니스 금액 차감(회수)
            // paymentService.businessCanclePoint(reservation.getTotal_price(), businessItemBean.getBusiness_id());
            paymentService.updateMemberPoint(loginMember.getMember_id(), reservation.getTotal_price());

            MemberBean mem = memberService.getMemberById(loginMember.getMember_id());
            loginMember.setMember_point(mem.getMember_point());
        }

        // 예약 취소 (상태 변경)
        reservation.setStatus("CANCELLED");
        reservationService.cancelReservation(reservationId);

        return "redirect:/reservation/my-reservations?cancel=success";
    }

    // 예약 취소
    @PostMapping("/clubcancel")
    public String clubCancelReservation(

            @RequestParam("reservation_id") int reservationId) {

        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return "redirect:/member/login";
        }

        // 예약 정보 가져오기
        ReservationBean reservation = reservationService.getReservationById(reservationId);

        BusinessItemBean businessItemBean = businessService.getItemById(reservation.getItem_id());

        if (reservation == null || !reservation.getMember_id().equals(loginMember.getMember_id())) {
            return "redirect:/member/memberinfo";
        }

        // 클럽 예약인 경우 클럽 포인트 환불
        if ("club".equals(reservation.getUser_type()) && reservation.getClub_id() > 0) {
            // 클럽 포인트 환불
            clubService.updateClubPoints(reservation.getClub_id(), reservation.getTotal_price());
        } else {
            // 개인 예약인 경우 회원 포인트 환불
            //취소시 비지니스 금액 차감(회수)
            // paymentService.businessCanclePoint(reservation.getTotal_price(), businessItemBean.getBusiness_id());
            paymentService.updateMemberPoint(loginMember.getMember_id(), reservation.getTotal_price());

            MemberBean mem = memberService.getMemberById(loginMember.getMember_id());
            loginMember.setMember_point(mem.getMember_point());
        }

        // 예약 취소 (상태 변경)
        reservation.setStatus("CANCELLED");
        reservationService.cancelReservation(reservationId);

        return "redirect:/club/club_info?club_id=" + reservation.getClub_id();
    }

    @PostMapping("/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestParam("reservation_id") int reservationId,
            @RequestParam("status") String status) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 예약 정보 가져오기
            ReservationBean reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                response.put("success", false);
                response.put("message", "예약 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 상태 업데이트
            reservation.setStatus(status);
            reservationService.updateReservation(reservation);

            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("예약 상태 업데이트 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "예약 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }



    // 리뷰 작성 페이지 이동
    @GetMapping("/write_review")
    public String writeReview(@RequestParam("reservation_id") int reservationId, Model model) {
        // 로그인 확인
        if (loginMember.getMember_id() == null) {
            return "redirect:/member/login";
        }

        // 예약 정보 가져오기
        ReservationBean reservation = reservationService.getReservationById(reservationId);

        // 본인 예약인지 확인
        if (reservation == null || !reservation.getMember_id().equals(loginMember.getMember_id())) {
            return "redirect:/reservation/my-reservations?error=unauthorized";
        }

        // 예약 정보와 함께 리뷰 작성 페이지로 이동
        model.addAttribute("reservation", reservation);

        return "reservation/write_review"; // templates/review/write_review.html 로 연결
    }

    @PostMapping("/write_review_pro")
    public String writeReviewPro(@RequestParam("review_id") int reviewId,
                                 @RequestParam("rating") int rating,
                                 @RequestParam("review_title") String reviewTitle,
                                 @RequestParam("review_text") String reviewText,
                                 @RequestParam("item_id") String itemId,  // ✅ item_id 추가
                                 @RequestParam(value = "review_img", required = false) MultipartFile reviewImg,
                                 RedirectAttributes redirectAttributes) {

        ReservationReviewBean review = new ReservationReviewBean();
        review.setReview_id(reviewId);
        review.setRating(rating);
        review.setReview_title(reviewTitle);
        review.setReview_text(reviewText);

        // 이미지 업로드 처리
        if (reviewImg != null && !reviewImg.isEmpty()) {
            String uploadDir = "C:/upload/image/review/";
            String originalFilename = reviewImg.getOriginalFilename();
            String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            File saveFolder = new File(uploadDir);
            if (!saveFolder.exists()) {
                saveFolder.mkdirs();
            }

            File saveFile = new File(uploadDir + savedFilename);
            try {
                reviewImg.transferTo(saveFile);
                review.setReview_img("/upload/image/review/" + savedFilename);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "이미지 업로드 중 오류 발생");
                return "redirect:/reservation/write_review?reservation_id=" + reviewId;
            }
        }

        reservationService.insertReview(review);
        redirectAttributes.addFlashAttribute("message", "리뷰가 등록되었습니다.");

        // ✅ 아이템 상세 페이지로 리다이렉트
        return "redirect:/business/item_info?item_id=" + itemId;
    }






}