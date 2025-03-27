package com.spacedong.controller;

import com.spacedong.beans.MemberBean;
import com.spacedong.service.MemberService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("/login")
public class NaverLoginCallbackController {


    @Resource(name = "loginMember")
    private MemberBean loginMember;

    @Autowired
    private MemberService memberService;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.redirect.uri}")
    private String redirectUri;

    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    @GetMapping("/naver/callback")
    public String naverCallback(@RequestParam("code") String code, @RequestParam("state")String state, Model model) {

        RestTemplate restTemplate = new RestTemplate();

        // 1. 네이버에 액세스 토큰 요청
        String tokenUrl = NAVER_TOKEN_URL +
                "?grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&state=" + state;

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, null, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if(responseBody != null && responseBody.containsKey("access_token")) {

            String access_token = (String) responseBody.get("access_token");

            // 2. 액세스 토큰을 이용해 네이버 사용자 정보 가져오기
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer "+ access_token);
            System.out.println(access_token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> memberInfoResponse = restTemplate.exchange(NAVER_PROFILE_URL, HttpMethod.GET, entity, Map.class);
            Map<String, Object> memberInfo = memberInfoResponse.getBody();

            if(memberInfo != null && memberInfo.containsKey("response")) {
                Map<String, Object> responseData = (Map<String, Object>) memberInfo.get("response");

                String id = (String) responseData.get("id");
                String email = (String) responseData.get("email");
                String name = (String) responseData.get("name");
                String modile = (String) responseData.get("mobile");
                String gender = (String) responseData.get("gender");
                String birthyear = (String) responseData.get("birthyear");
                String birthday = (String) responseData.get("birthday");

                String birth = birthyear + "-" + birthday;

                String dateStr = birth;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate localDate = LocalDate.parse(dateStr, formatter);

                Date date = java.sql.Date.valueOf(localDate);

                System.out.println(gender);
                System.out.println(birthyear);
                System.out.println(birthday);
                System.out.println(birth);

                // 4. 사용자 정보를 MemberBean에 저장

                MemberBean memberBean = new MemberBean();
                memberBean.setMember_id(id);
                memberBean.setSns_id(id);
                memberBean.setMember_email(email);
                memberBean.setMember_name(name);
                memberBean.setMember_phone(modile);
                memberBean.setMember_birthdate(date);
                memberBean.setMember_gender(gender);

                loginMember.setLogin(true);

                // 5. DB 저장 (이미 존재하면 업데이트)
                memberService.naverLogin(memberBean);
            }

            System.out.println(loginMember.getMember_id());

            // 3. 사용자 정보를 모델에 저장
            model.addAttribute("memberInfo", memberInfo);

        }
        return "redirect:/";

    }



}
