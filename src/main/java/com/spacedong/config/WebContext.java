package com.spacedong.config;


import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.interceptor.BusinessInterceptor;
import com.spacedong.interceptor.LocationInterceptor;
import com.spacedong.interceptor.TopMenuInterceptor;
import com.spacedong.service.ChatService;
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

    @Resource(name = "loginBusiness")  // ✅ @Resource 추가 (세션에서 주입)
    public BusinessBean loginBusiness;

    @Autowired
    private ChatService ChatService;
    @Autowired
    private ChatService chatService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        LocationInterceptor locationInterceptor = new LocationInterceptor(locationService);
        InterceptorRegistration reg1 = registry.addInterceptor(locationInterceptor);
        reg1.addPathPatterns("/**");

        // 2️⃣ 상단 메뉴 인터셉터 (일반 회원 + 판매자 로그인 정보 추가)
        TopMenuInterceptor topMenuInterceptor = new TopMenuInterceptor(loginMember, loginBusiness, () -> chatService); // ✅ 수정됨!
        InterceptorRegistration reg2 = registry.addInterceptor(topMenuInterceptor);
        reg2.addPathPatterns("/**");

        BusinessInterceptor businessInterceptor = new BusinessInterceptor(loginBusiness);
        InterceptorRegistration reg5 = registry.addInterceptor(businessInterceptor);
        reg5.addPathPatterns("/**");



    }
}
