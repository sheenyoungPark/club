package com.spacedong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.spacedong.service.MemberService;

@RestController
@RequestMapping("/member")
public class RestAPIController {

    @Autowired
    private MemberService memberService;

    // 아이디 중복 확인 API
    @GetMapping("/checkId")
    public boolean checkId(@RequestParam("member_id") String member_id) {
        return memberService.checkId(member_id);
    }

    // 닉네임 중복 확인 API - 수정된 버전
    @GetMapping("/checkNickname")
    public boolean checkNickname(
            @RequestParam("member_nickname") String member_nickname,
            @RequestParam(value = "current_id", required = false) String current_id) {

        // current_id 파라미터가 있으면 회원정보 수정용 메서드 호출
        if (current_id != null && !current_id.isEmpty()) {
            return memberService.checkNickname(member_nickname, current_id);
        } else {
            // current_id 파라미터가 없으면 회원가입용 메서드 호출
            return memberService.checkNickname(member_nickname);
        }
    }
}