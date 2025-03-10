package com.spacedong.config;


import com.spacedong.interceptor.LocationInterceptor;
import com.spacedong.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebContext implements WebMvcConfigurer {

    @Autowired
    private LocationService locationService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        LocationInterceptor locationInterceptor = new LocationInterceptor(locationService);
        InterceptorRegistration reg1 = registry.addInterceptor(locationInterceptor);
        reg1.addPathPatterns("/**");



    }
}
