package com.spacedong.service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private static String accessKey;
    private static String secretKey;
    private static Region awsRegion = Region.AP_NORTHEAST_1; // 일본 리전(도쿄)
    private static SnsClient snsClient;

    @Value("${aws.access-key}")
    public void setAccessKey(String key) {
        accessKey = key;
        initializeSnsClient();
    }

    @Value("${aws.secret-key}")
    public void setSecretKey(String key) {
        secretKey = key;
        initializeSnsClient();
    }

    @Value("${aws.region}")
    public void setRegion(String region) {
        awsRegion = Region.of(region);
        initializeSnsClient();
    }

    private void initializeSnsClient() {
        if (accessKey != null && secretKey != null) {
            snsClient = SnsClient.builder()
                    .region(awsRegion)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
        }
    }

    public static void sendSms(String phoneNumber, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .build();
            PublishResponse result = snsClient.publish(request);
            System.out.println("Message ID: " + result.messageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}