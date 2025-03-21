package com.spacedong.interceptor;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class TopMenuInterceptor implements HandlerInterceptor {

    private final MemberBean loginMember;
    private final BusinessBean loginBusiness;

    public TopMenuInterceptor(MemberBean memberBean, BusinessBean businessBean) {
        this.loginMember = memberBean;
        this.loginBusiness = businessBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // BoardType은 유지
        request.setAttribute("BoardType", "member");

        // 멤버와 비즈니스 둘 다 로그인 상태인 경우 (비정상 상태) 해결
        if (loginMember != null && loginBusiness != null &&
                loginMember.isLogin() && loginBusiness.isLogin()) {

            // 로그 출력
            System.out.println("비정상 상태 감지: 멤버와 비즈니스 모두 로그인 상태");

            // 현재 접근 중인 URI 확인
            String uri = request.getRequestURI();

            // 비즈니스 관련 페이지에 접근 중이면 멤버 로그인 초기화
            if (uri.contains("/business/")) {
                loginMember.setLogin(false);
                loginMember.setMember_id(null);
                loginMember.setMember_nickname(null);
                System.out.println("비즈니스 페이지 접근 - 멤버 로그인 초기화됨");
            }
            // 그 외 페이지는 비즈니스 로그인 초기화
            else {
                loginBusiness.setLogin(false);
                loginBusiness.setBusiness_id(null);
                loginBusiness.setBusiness_name(null);
                System.out.println("일반 페이지 접근 - 비즈니스 로그인 초기화됨");
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject("loginMember", loginMember);
            modelAndView.addObject("loginBusiness", loginBusiness);

            // 디버깅 정보 출력
            System.out.println("TopMenuInterceptor - 모델에 로그인 정보 추가:");
            System.out.println("Member 로그인 상태: " + (loginMember != null ? loginMember.isLogin() : "null"));
            System.out.println("Business 로그인 상태: " + (loginBusiness != null ? loginBusiness.isLogin() : "null"));
        }
    }
}