package com.spacedong.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/login")
public class NaverLoginController {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.redirect.uri}")
    private String redirectUri;

    private static final String NAVER_AUTH_URL = "https://nid.naver.com/oauth2.0/authorize";

    @GetMapping("/naver")
    public String naverLogin() {
        // CSRF 방지를 위한 랜덤 state 값 생성
        String state = UUID.randomUUID().toString();

        // 네이버 로그인 URL 생성
        String url = NAVER_AUTH_URL + "?response_type=code" + // 네이버에서 인증 코드를 반환하도록 설정
                "&client_id=" + clientId + // 내 애플리케이션 ID
                "&redirect_uri=" + redirectUri + // 네이버 로그인 후 이동할 내 서버의 URL
                "&state=" + state; // CSRF 방지를 위한 state 값

        return "redirect:" + url; // 네이버 로그인 페이지로 이동

    }



}

