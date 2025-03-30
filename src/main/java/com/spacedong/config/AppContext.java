package com.spacedong.config;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.interceptor.AdminAuthInterceptor;
import com.spacedong.interceptor.TopMenuInterceptor;
import com.spacedong.service.ChatService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@Configuration
@ComponentScan({"com.spacedong.controller", "com.spacedong.service"}) // 컨트롤러와 서비스 스캐닝
public class AppContext implements WebMvcConfigurer {

    @Bean(name="loginMember")
    @SessionScope
    public MemberBean loginMember(){
        MemberBean memberBean = new MemberBean();
        // login 속성 기본값 설정
        memberBean.setLogin(false);
        return memberBean;
    }

    @Bean(name="loginBusiness")
    @SessionScope
    public BusinessBean loginBusiness() {
        BusinessBean businessBean = new BusinessBean();
        // login 속성 기본값 설정
        businessBean.setLogin(false);
        return businessBean;
    }

    @Bean(name = "loginAdmin")
    @SessionScope
    public AdminBean loginAdmin() {
        AdminBean adminBean = new AdminBean();
        adminBean.setAdmin_login(false);
        return adminBean;
    }

    // ApplicationContext 주입
    private final ApplicationContext applicationContext;

    // 생성자 주입 방식으로 변경
    public AppContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String membername;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClass);
        config.setJdbcUrl(url);
        config.setUsername(membername);
        config.setPassword(password);
        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        return sqlSessionFactoryBean.getObject();
    }

    // TopMenuInterceptor 빈 정의
    @Bean
    public TopMenuInterceptor topMenuInterceptor() {

        MemberBean memberbean = loginMember();
        BusinessBean businessbean = loginBusiness();

        return new TopMenuInterceptor(memberbean, businessbean, () -> applicationContext.getBean("chatService", ChatService.class));

    }

    // 인터셉터 등록
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("인터셉터 등록");
        registry.addInterceptor(topMenuInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/resources/**", "/css/**", "/js/**", "/images/**");





    }
}