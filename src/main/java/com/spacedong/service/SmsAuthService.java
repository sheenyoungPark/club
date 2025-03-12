package com.spacedong.service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SmsAuthService {
    private static final ConcurrentHashMap<String, String> authCodes = new ConcurrentHashMap<>();

    public static String generateAuthCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6자리 랜덤 숫자 생성
    }

    public static void sendAuthCode(String phoneNumber) {
        String authCode = generateAuthCode();
        authCodes.put(phoneNumber, authCode);
        SmsService.sendSms(phoneNumber, "[우주동]인증번호: " + authCode);
    }

    public static boolean verifyAuthCode(String phoneNumber, String inputCode) {
        return authCodes.containsKey(phoneNumber) && authCodes.get(phoneNumber).equals(inputCode);
    }
}