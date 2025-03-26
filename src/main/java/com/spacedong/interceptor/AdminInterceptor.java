package com.spacedong.interceptor;

import com.spacedong.beans.AdminBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class AdminInterceptor implements HandlerInterceptor {

    private AdminBean loginAdmin;

    public AdminInterceptor(AdminBean loginAdmin) {
        this.loginAdmin = loginAdmin;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        // ModelAndView가 null이 아니고, 로그인 정보가 있을 때만 처리
        if (modelAndView != null && loginAdmin != null && loginAdmin.isAdmin_login()) {
            // 모든 admin/* URL 패턴에 로그인 정보 추가
            String requestURI = request.getRequestURI();
            if (requestURI.startsWith("/admin/") && !requestURI.equals("/admin/login")) {
                modelAndView.addObject("loginAdmin", loginAdmin);
            }
        }
    }


}
