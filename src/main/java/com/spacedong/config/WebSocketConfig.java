package com.spacedong.config;

import com.spacedong.interceptor.AuthenticatedChannelInterceptor;
import com.spacedong.interceptor.HttpSessionHandshakeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 구독 prefix
        config.enableSimpleBroker("/topic", "/queue", "/notification");
        // 메시지 발행 prefix
        config.setApplicationDestinationPrefixes("/app");
        // 특정 사용자에게 메시지를 보내기 위한 prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS 지원
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인증 채널 인터셉터 등록
        registration.interceptors(authenticatedChannelInterceptor());
    }

    @Bean
    public HttpSessionHandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor();
    }

    @Bean
    public AuthenticatedChannelInterceptor authenticatedChannelInterceptor() {
        return new AuthenticatedChannelInterceptor();
    }
}