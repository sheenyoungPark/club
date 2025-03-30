package com.spacedong.interceptor;

import com.spacedong.beans.AdminBean;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {

    private AdminBean loginAdmin;

    public AdminAuthInterceptor(AdminBean loginAdmin) {
        this.loginAdmin = loginAdmin;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그인 체크
        boolean isLoggedIn = (loginAdmin != null && loginAdmin.isAdmin_login());

        // 요청 경로 확인
        String requestURI = request.getRequestURI();

        // 로그인 페이지, 로그인 처리 URL, 정적 리소스는 항상 허용
        if (requestURI.equals("/admin/login") ||
                requestURI.equals("/admin/loginproc") ||
                requestURI.startsWith("/admin/assets/")) {
            // 이미 로그인된 상태에서 로그인 페이지 접근 시 대시보드로 리다이렉트
            if (isLoggedIn && requestURI.equals("/admin/login")) {
                response.sendRedirect("/admin/dashboard");
                return true;
            }
            return true;
        }

        // 로그인되지 않은 상태에서 다른 admin 경로 접근 시
        if (!isLoggedIn) {
            // 로그인 페이지로 리다이렉트
            response.sendRedirect("/admin/login");
            return false;
        }

        // 로그인된 상태에서는 모든 admin 경로 접근 허용
        return true;
    }
}