package com.spacedong.service;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SmsAuthService {
    private static final ConcurrentHashMap<String, String> authCodes = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static String generateAuthCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6자리 랜덤 숫자 생성
    }

    public static void sendAuthCode(String phoneNumber) {
        try {
            String authCode = generateAuthCode();
            authCodes.put(phoneNumber, authCode);

            // 5분 후 인증번호 자동 삭제
            scheduler.schedule(() -> authCodes.remove(phoneNumber), 5, TimeUnit.MINUTES);

            SmsService.sendSms(phoneNumber, "[우주동] 인증번호: " + authCode);
        } catch (Exception e) {
            System.out.println("SMS 발송 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean verifyAuthCode(String phoneNumber, String inputCode) {
        return authCodes.containsKey(phoneNumber) && authCodes.get(phoneNumber).equals(inputCode);
    }
}
