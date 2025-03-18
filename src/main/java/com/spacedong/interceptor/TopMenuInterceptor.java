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

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject("loginMember", loginMember);
            modelAndView.addObject("loginBusiness", loginBusiness);
        }
    }
}