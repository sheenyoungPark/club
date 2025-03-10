package com.spacedong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reserve")
public class ReserveController {
	
	@GetMapping("/main")
	public String reserve_main() {
		
		return "reserve/reserve_main";
	}
}
