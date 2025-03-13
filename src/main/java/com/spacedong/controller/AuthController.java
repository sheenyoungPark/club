package com.spacedong.controller;

import org.springframework.web.bind.annotation.*;
import com.spacedong.service.SmsAuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // 인증번호 요청 API
    @PostMapping(value = "/sendCode", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String sendAuthCode(@RequestParam String phoneNumber) {
//        try {
//            SmsAuthService.sendAuthCode(phoneNumber);
//            return "인증번호가 발송되었습니다.";
//        } catch (Exception e) {
//            return "인증번호 발송에 실패했습니다: " + e.getMessage();
//        }
           return "인증번호가 발송되었습니다.";
    }

    // 인증번호 검증 API
    @PostMapping("/verifyCode")
    public boolean verifyAuthCode(@RequestParam String phoneNumber, @RequestParam String authCode) {
//        return SmsAuthService.verifyAuthCode(phoneNumber, authCode);
        return true;
    }
}