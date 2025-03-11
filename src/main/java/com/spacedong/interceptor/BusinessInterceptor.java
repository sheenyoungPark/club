package com.spacedong.interceptor;

import com.spacedong.beans.BusinessBean;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class BusinessInterceptor implements HandlerInterceptor {

    @Resource(name = "loginBuseiness")
    private BusinessBean loginBusiness;

    public BusinessInterceptor(BusinessBean businessBean) {
        loginBusiness = businessBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        request.setAttribute("loginBusiness", loginBusiness);
        System.out.println("인터셉터: " + loginBusiness.getBusiness_id());

        return true;
    }


}
