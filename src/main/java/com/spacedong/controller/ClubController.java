package com.spacedong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spacedong.beans.ClubBean;
import com.spacedong.service.ClubService;

import java.time.LocalDate;

@Controller
@RequestMapping("/club")
public class ClubController {

	@Autowired
	   private ClubService clubService; 

	   
	   @GetMapping("/club_info")
	   public String club_info(@RequestParam("club_id")int club_id ,Model model) {
	      
	      ClubBean club = clubService.oneClubInfo(club_id);



	      model.addAttribute("club", club);
	      
	      return "club/club_info";
	   }
}
