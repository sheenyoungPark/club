package com.spacedong.interceptor;

import java.security.Principal;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * STOMP 메시지 채널의 인증을 처리하는 인터셉터
 */
public class AuthenticatedChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // CONNECT 명령 처리 (웹소켓 연결 시)
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // HTTP 핸드셰이크 인터셉터에서 넘긴 사용자 정보 가져오기
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    String userId = (String) sessionAttributes.get("userId");

                    if (userId != null && !userId.trim().isEmpty()) {
                        // Principal 객체 설정 (인증)
                        accessor.setUser(new UserPrincipal(userId));
                    }
                }
            }
        }

        return message;
    }

    /**
     * Principal 인터페이스 구현
     */
    private static class UserPrincipal implements Principal {
        private final String name;

        public UserPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return "UserPrincipal [name=" + name + "]";
        }
    }
}