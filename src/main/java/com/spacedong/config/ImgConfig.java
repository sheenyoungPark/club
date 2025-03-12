package com.spacedong.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class ImgConfig implements WebMvcConfigurer {
    private static final String UPLOAD_DIR = "C:/upload/image/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 폴더가 존재하지 않으면 생성
        createUploadDir();

        // ✅ 업로드된 이미지가 /image/ 경로에서 접근 가능하도록 설정
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:///" + UPLOAD_DIR);
    }

    // ✅ 폴더가 없으면 생성하는 메서드
    private void createUploadDir() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("📂 업로드 폴더 생성 완료: " + UPLOAD_DIR);
            } else {
                System.out.println("🚨 업로드 폴더 생성 실패!");
            }
        }
    }
}