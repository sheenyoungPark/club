package com.spacedong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/include")
public class OtherController {

    @GetMapping("/chat")
    public String chat() {
        return "include/chat";
    }
}
