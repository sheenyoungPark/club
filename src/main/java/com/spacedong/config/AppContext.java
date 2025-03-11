package com.spacedong.config;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

import javax.sql.DataSource;

@Configuration
public class AppContext {

    @Bean(name="loginMember")
    @SessionScope
    public MemberBean loginMember(){
        return new MemberBean();
    }

    @Bean("loginBusiness")
    @SessionScope
    public BusinessBean loginBusiness() {
        return new BusinessBean();
    }

    @Bean(name = "loginAdmin")
    @SessionScope
    public AdminBean loginAdmin() {
        return new AdminBean();
    }

    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.membername}")
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
}
