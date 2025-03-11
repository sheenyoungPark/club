package com.spacedong.interceptor;

import com.spacedong.beans.MemberBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class TopMenuInterceptor implements HandlerInterceptor {

    private final MemberBean loginMember;

    public TopMenuInterceptor(MemberBean memberBean) {
        this.loginMember = memberBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        System.out.println("preHandle 실행: loginMember 상태 = " + (loginMember != null ? "객체 있음" : "null")
                + ", 로그인 상태 = " + (loginMember != null ? loginMember.isLogin() : "N/A"));

        // BoardType은 유지
        request.setAttribute("BoardType", "member");

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            System.out.println("postHandle 실행: loginMember 상태 = " + (loginMember != null ? "객체 있음" : "null")
                    + ", 로그인 상태 = " + (loginMember != null ? loginMember.isLogin() : "N/A"));
            modelAndView.addObject("loginMember", loginMember);
        }
    }
}