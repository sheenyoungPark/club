package com.spacedong.interceptor;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.function.Supplier;

public class TopMenuInterceptor implements HandlerInterceptor {

    private final MemberBean loginMember;
    private final BusinessBean loginBusiness;
    private final Supplier<ChatService> chatServiceSupplier;

    public TopMenuInterceptor(MemberBean memberBean, BusinessBean businessBean, Supplier<ChatService> chatServiceSupplier) {
        this.loginMember = memberBean;
        this.loginBusiness = businessBean;
        this.chatServiceSupplier = chatServiceSupplier;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // BoardType은 유지
        request.setAttribute("BoardType", "member");

        // 멤버와 비즈니스 둘 다 로그인 상태인 경우 (비정상 상태) 처리
        if (loginMember != null && loginBusiness != null &&
                loginMember.isLogin() && loginBusiness.isLogin()) {

            // 로그 출력
            System.out.println("비정상 상태 감지: 멤버와 비즈니스 모두 로그인 상태");

            // 두 로그인 모두 초기화 (더 안전한 방법)
            loginMember.setLogin(false);
            loginMember.setMember_id(null);
            loginMember.setMember_nickname(null);
            loginMember.setMember_pw(null);

            loginBusiness.setLogin(false);
            loginBusiness.setBusiness_id(null);
            loginBusiness.setBusiness_name(null);
            loginBusiness.setBusiness_pw(null);

            System.out.println("모든 로그인 초기화됨 - 재로그인 필요");

            // 로그인 페이지로 리다이렉트
            response.sendRedirect(request.getContextPath() + "/member/login?error=duplicate_login");
            return false; // 요청 중단
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject("loginMember", loginMember);
            modelAndView.addObject("loginBusiness", loginBusiness);

            // 로그인 상태일 때만 안 읽은 메시지 수 계산
            if (loginMember != null && loginMember.isLogin()) {
                // 현재 페이지가 채팅 관련 페이지인지 확인
                String requestURI = request.getRequestURI();
                boolean isChatPage = requestURI.contains("/chat/");

                // 채팅 페이지가 아닐 때만 알림 카운트 추가
                if (!isChatPage) {
                    try {
                        // 사용자의 총 안 읽은 메시지 수 계산
                        ChatService chatService = chatServiceSupplier.get();


                        int unreadCount = chatService.calculateTotalUnreadMessages(loginMember.getMember_id());
                        modelAndView.addObject("unreadChatCount", unreadCount);
                    } catch (Exception e) {
                        // 에러 발생 시 로그를 남기고 계속 진행 (페이지 로딩은 차단하지 않음)
                        System.err.println("안 읽은 메시지 수 계산 중 오류: " + e.getMessage());
                        modelAndView.addObject("unreadChatCount", 0);
                    }
                }
            }

            // 비즈니스 로그인 상태일 때도 필요하다면 같은 방식으로 처리
            if (loginBusiness != null && loginBusiness.isLogin()) {
                String requestURI = request.getRequestURI();
                boolean isChatPage = requestURI.contains("/chat/");

                // 채팅 페이지가 아닐 때만 알림 카운트 추가
                if (!isChatPage) {
                    try {
                        // 사용자의 총 안 읽은 메시지 수 계산
                        ChatService chatService = chatServiceSupplier.get();


                        int unreadCount = chatService.calculateTotalUnreadMessages(loginBusiness.getBusiness_id());
                        modelAndView.addObject("unreadChatCount", unreadCount);
                    } catch (Exception e) {
                        // 에러 발생 시 로그를 남기고 계속 진행 (페이지 로딩은 차단하지 않음)
                        System.err.println("안 읽은 메시지 수 계산 중 오류: " + e.getMessage());
                        modelAndView.addObject("unreadChatCount", 0);
                    }
                }
            }
        }
    }
}