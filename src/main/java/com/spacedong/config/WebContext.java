package com.spacedong.config;


import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.interceptor.BusinessInterceptor;
import com.spacedong.interceptor.LocationInterceptor;
import com.spacedong.interceptor.TopMenuInterceptor;
import com.spacedong.service.LocationService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebContext implements WebMvcConfigurer {

    @Autowired
    private LocationService locationService;

    @Resource(name = "loginMember")
    public MemberBean loginMember;
    @Autowired
    private BusinessBean loginBusiness;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        LocationInterceptor locationInterceptor = new LocationInterceptor(locationService);
        InterceptorRegistration reg1 = registry.addInterceptor(locationInterceptor);
        reg1.addPathPatterns("/**");

        TopMenuInterceptor topMenuInterceptor = new TopMenuInterceptor(loginMember);
        InterceptorRegistration reg2 = registry.addInterceptor(topMenuInterceptor);
        reg2.addPathPatterns("/**");

        BusinessInterceptor businessInterceptor = new BusinessInterceptor(loginBusiness);
        InterceptorRegistration reg5 = registry.addInterceptor(businessInterceptor);
        reg5.addPathPatterns("/**");



    }
}
