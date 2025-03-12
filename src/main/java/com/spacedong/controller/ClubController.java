package com.spacedong.controller;

import com.spacedong.beans.ClubMemberBean;
import com.spacedong.beans.MemberBean;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spacedong.beans.ClubBean;
import com.spacedong.service.ClubService;

import java.time.LocalDate;

@Controller
@RequestMapping("/club")
public class ClubController {

	@Resource(name = "loginMember")
	private MemberBean loginMember;

	@Autowired
	   private ClubService clubService; 

	   //클럽 정보 페이지
	   @GetMapping("/club_info")
	   public String club_info(@RequestParam("club_id")int club_id ,Model model) {
	      
	      ClubBean club = clubService.oneClubInfo(club_id);



	      model.addAttribute("club", club);
	      
	      return "club/club_info";
	   }

	   //클럽 가입
	@GetMapping("/club_join")
	public String club_join(@RequestParam int club_id,String member_id) {

			member_id = loginMember.getMember_id();

			if (member_id != null) {
				ClubMemberBean clubMemberBean = clubService.getClubMember(club_id, member_id);

				if (clubMemberBean == null) {
				clubService.join_club(club_id, loginMember.getMember_id());
				return "club/club_join_success";
			} else {
				return "club/club_member_fail";
			}

		}else {
			return "club/join_fail";
		}
	}


}
