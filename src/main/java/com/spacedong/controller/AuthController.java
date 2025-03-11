package com.spacedong.controller;

import com.spacedong.service.SmsAuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    // 인증번호 요청 API
    @PostMapping(value = "/sendCode", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String sendAuthCode(@RequestParam String phoneNumber) {
        try {
            System.out.println("휴대폰 인증 요청: " + phoneNumber);
            SmsAuthService.sendAuthCode(phoneNumber);
            return "인증번호가 발송되었습니다.";
        } catch (Exception e) {
            System.out.println("휴대폰 인증 요청 실패: " + e.getMessage());
            e.printStackTrace();
            return "오류 발생: " + e.getMessage();
        }
    }

    // 인증번호 검증 API
    @PostMapping("/verifyCode")
    public boolean verifyAuthCode(@RequestParam String phoneNumber, @RequestParam String authCode) {
        try {
            System.out.println("인증번호 확인 요청: " + phoneNumber + " | 입력한 코드: " + authCode);
            boolean result = SmsAuthService.verifyAuthCode(phoneNumber, authCode);
            System.out.println("인증 결과: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("인증번호 확인 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
