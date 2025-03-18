package com.spacedong.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 주제 접두사 설정
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 메시지를 보낼 접두사 설정
        config.setApplicationDestinationPrefixes("/app");

        // 사용자별 대상 메시지 접두사 설정
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws-connect")
                .setAllowedOriginPatterns("*") // CORS 설정
                .withSockJS(); // SockJS 지원 추가
    }
}