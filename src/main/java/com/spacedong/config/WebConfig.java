package com.spacedong.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Date 타입 변환을 위한 설정
        DateFormatterRegistrar dateRegistrar = new DateFormatterRegistrar();
        dateRegistrar.setFormatter(new DateFormatter("yyyy-MM-dd"));
        dateRegistrar.registerFormatters(registry);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 외부 경로의 이미지 파일을 웹에서 접근할 수 있도록 설정
        registry.addResourceHandler("/upload/image/**")
                .addResourceLocations("file:///" + uploadDir);
    }
}