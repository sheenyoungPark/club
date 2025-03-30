package com.spacedong.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${chat.upload.dir}")
    private String chatDir;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Date 타입 변환을 위한 설정
        DateFormatterRegistrar dateRegistrar = new DateFormatterRegistrar();
        dateRegistrar.setFormatter(new DateFormatter("yyyy-MM-dd"));
        dateRegistrar.registerFormatters(registry);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/chat/**")
                .addResourceLocations("file:///" + chatDir + "/");

        // 기본 이미지 업로드 경로 설정
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:///" + uploadDir + "profile/");

        registry.addResourceHandler("/clubprofile/**")
                .addResourceLocations("file:///" + uploadDir + "clubprofile/");

        // 기존 정적 리소스 설정
        registry.addResourceHandler("/sources/**")
                .addResourceLocations("classpath:/static/sources/");


        registry.addResourceHandler("/favicons/**")
                .addResourceLocations("classpath:/static/favicons/");

    }


}