package com.spacedong.interceptor;

import com.spacedong.beans.MemberBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class TopMenuInterceptor implements HandlerInterceptor {

    private MemberBean loginMember;

    public TopMenuInterceptor(MemberBean memberBean) {

        loginMember = memberBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        request.setAttribute("loginMember", loginMember);
        request.setAttribute("BoardType", "member");

        return true;
    }

}
