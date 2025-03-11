package com.spacedong.service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

public class SmsService {

    private static final String ACCESS_KEY = System.getenv("AWS_ACCESS_KEY");
    private static final String SECRET_KEY = System.getenv("AWS_SECRET_KEY");
    private static final Region AWS_REGION = Region.AP_NORTHEAST_1; // 일본 리전(도쿄)

    private static final SnsClient snsClient = SnsClient.builder()
            .region(AWS_REGION)
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
            .build();

    public static void sendSms(String phoneNumber, String message) {
        try {
            System.out.println("AWS SNS SMS 요청: " + phoneNumber + " | 메시지: " + message);

            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .build();

            PublishResponse result = snsClient.publish(request);
            System.out.println("AWS SNS 응답: " + result.messageId());

        } catch (Exception e) {
            System.out.println("AWS SMS 전송 실패! 오류 메시지: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
