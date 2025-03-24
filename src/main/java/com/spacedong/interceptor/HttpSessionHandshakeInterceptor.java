package com.spacedong.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.spacedong.beans.MemberBean;
import com.spacedong.beans.BusinessBean;

import jakarta.servlet.http.HttpSession;

/**
 * HTTP 세션의 사용자 정보를 WebSocket 세션으로 전달하는 인터셉터
 */
public class HttpSessionHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession(false);

            if (session != null) {
                // 세션에서 로그인 멤버 정보 추출
                MemberBean loginMember = (MemberBean) session.getAttribute("loginMember");
                if (loginMember != null && loginMember.isLogin()) {
                    attributes.put("userId", loginMember.getMember_id());
                    attributes.put("userType", "MEMBER");
                    return true;
                }

                // 세션에서 비즈니스 정보 추출
                BusinessBean loginBusiness = (BusinessBean) session.getAttribute("loginBusiness");
                if (loginBusiness != null && loginBusiness.isLogin()) {
                    attributes.put("userId", loginBusiness.getBusiness_id());
                    attributes.put("userType", "BUSINESS");
                    return true;
                }
            }
        }

        // 로그인 정보가 없어도 연결 허용 (익명 사용자 처리 필요시 구현)
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 후 추가 작업이 필요한 경우 구현
    }
}