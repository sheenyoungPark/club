package com.spacedong.controller;

import com.spacedong.beans.Category;
import com.spacedong.beans.ClubMemberBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.CategoryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.spacedong.beans.ClubBean;
import com.spacedong.service.ClubService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/club")
public class ClubController {


	@Resource(name = "loginMember")
	private MemberBean loginMember;

	@Autowired
	   private ClubService clubService;

    @Autowired
    private CategoryService categoryService;

	//클럽 정보 페이지
	   @GetMapping("/club_info")
	   public String club_info(@RequestParam("club_id")int club_id ,Model model) {
	      
	      ClubBean club = clubService.oneClubInfo(club_id);



	      model.addAttribute("club", club);
	      
	      return "club/club_info";
	   }

	   //클럽 가입
	@GetMapping("/club_join")
	public String club_join(@RequestParam int club_id) {

			 String member_id = loginMember.getMember_id();

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
	// 클럽 생성 페이지
	@GetMapping("create")
	public String create(@ModelAttribute ClubBean clubBean, Model model) {
		if(loginMember.getMember_id() != null) {
			// 대분류 목록 가져오기
			List<String> categoryTypes = categoryService.getAllCategoryType();
			model.addAttribute("categoryType", categoryTypes);
			model.addAttribute("clubBean", clubBean);
			return "club/create";
		} else {
			return "club/join_fail";
		}
	}

	// AJAX로 소분류 카테고리 목록 가져오기
	@GetMapping("/get_sub_categories")
	@ResponseBody
	public List<Category> getSubCategories(@RequestParam("category_type") String categoryType) {
		return categoryService.categoryInfo(categoryType);
	}

	// 클럽 생성
	@PostMapping("create_pro")
	public String create_pro(@Valid ClubBean clubBean, BindingResult result, Model model) {
		if (result.hasErrors()) {
			// 에러 발생 시 필요한 데이터 다시 로드
			List<String> categoryTypes = categoryService.getAllCategoryType();
			model.addAttribute("categoryType", categoryTypes);

			// 선택된 카테고리가 있다면 해당하는 소분류 목록도 다시 로드
			if (clubBean.getClub_category() != null && !clubBean.getClub_category().isEmpty()) {
				Category category = categoryService.getCategoryByName(clubBean.getClub_category());
				if (category != null) {
					List<Category> subCategoryList = categoryService.categoryInfo(category.getCategory_type());
					model.addAttribute("subCategoryList", subCategoryList);
				}
			}

			return "club/create";
		}

		// 클럽 생성 처리
		clubService.create_club(clubBean);
		System.out.println("클럽 아이디" + clubBean.getClub_id() + "로그인멤버 아이디 : " + loginMember.getMember_id());
		ClubBean newClubBean = clubService.searchClubName(clubBean.getClub_name());
		clubService.create_join_club(newClubBean.getClub_id(), loginMember.getMember_id());

		return "redirect:/category/category_info";
	}

}
