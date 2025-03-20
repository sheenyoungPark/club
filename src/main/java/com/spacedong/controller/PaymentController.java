package com.spacedong.controller;

import com.spacedong.beans.MemberBean;
import com.spacedong.service.PaymentService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Resource(name = "loginMember")
    private MemberBean loginMember;

    @Autowired
    private PaymentService paymentService;  // 🔹 PaymentService 사용

    private final String CLIENT_KEY = "test_ck_DnyRpQWGrN5BYwJnKKbyVKwv1M9E"; // 토스 테스트용 클라이언트 키
    private final String SECRET_KEY = "test_sk_0RnYX2w532q19BZzeLBP8NeyqApQ"; // 토스 테스트용 시크릿 키

    // 🔹 충전 페이지 이동
    @GetMapping("/charge")
    public String showChargePage() {
        return "payment/charge"; // 결제 요청 페이지 (Thymeleaf 템플릿)
    }

    // 🔹 결제 요청 → 토스 API 호출
    @PostMapping("/request")
    public String requestPayment(@RequestParam("amount") int amount, Model model) {
        String orderId = UUID.randomUUID().toString(); // 주문 ID 생성
        String orderName = "포인트 충전";
        String successUrl = "http://localhost:8080/payment/success"; // 성공 시 이동할 URL
        String failUrl = "http://localhost:8080/payment/fail"; // 실패 시 이동할 URL

        model.addAttribute("amount", amount);
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("successUrl", successUrl);
        model.addAttribute("failUrl", failUrl);

        return "payment/request"; // 결제 요청 페이지로 이동 (Thymeleaf)
    }

    // 🔹 결제 성공 후 승인 처리 및 포인트 업데이트
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("paymentKey") String paymentKey,
                                 @RequestParam("orderId") String orderId,
                                 @RequestParam("amount") int amount,// 🔹 세션에서 로그인된 사용자 가져오기
                                 Model model) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // 🔹 Basic Auth 설정 (Base64 인코딩)
        String auth = SECRET_KEY + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 🔹 요청 바디 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.tosspayments.com/v1/payments/confirm";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 🔹 로그인된 사용자의 포인트 업데이트 (서비스 호출)
            paymentService.updateMemberPoint(loginMember.getMember_id(), amount);

            // 🔹 최신 포인트 가져오기
            int updatedPoint = loginMember.getMember_point() + amount;
            loginMember.setMember_point(updatedPoint); // 세션에 업데이트

            model.addAttribute("message", "결제가 성공적으로 완료되었습니다! 포인트가 충전되었습니다.");
            model.addAttribute("amount", amount);
            model.addAttribute("updatedPoint", updatedPoint); // 새 포인트 추가

            return "payment/success";  // 성공 페이지로 이동
        } catch (Exception e) {
            model.addAttribute("message", "결제 승인 실패: " + e.getMessage());
            return "payment/fail";
        }
    }

    // 🔹 결제 실패 페이지
    @GetMapping("/fail")
    public String paymentFail(@RequestParam("message") String message, Model model) {
        model.addAttribute("message", "결제 실패: " + message);
        return "payment/fail"; // 실패 페이지 이동
    }
}
