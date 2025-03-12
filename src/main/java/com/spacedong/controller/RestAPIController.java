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

    // 닉네임 중복 확인 API
    @GetMapping("/checkNickname")
    public boolean checkNickname(@RequestParam("member_nickname") String member_nickname) {
        return memberService.checkNickname(member_nickname);
    }
}
