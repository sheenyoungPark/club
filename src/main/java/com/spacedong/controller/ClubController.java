package com.spacedong.controller;

import com.spacedong.beans.Category;
import com.spacedong.beans.ClubBean;
import com.spacedong.beans.ClubMemberBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.CategoryService;
import com.spacedong.service.ClubService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/club")
public class ClubController {

	// ✅ 클럽 프로필 이미지 저장 경로
	private static final String UPLOAD_DIR = "C:/upload/image/clubprofile/";

	@Resource(name = "loginMember")
	private MemberBean loginMember;

	@Autowired
	private ClubService clubService;

	@Autowired
	private CategoryService categoryService;

	// ✅ 클럽 정보 페이지
	@GetMapping("/club_info")
	public String club_info(@RequestParam("club_id") int club_id, Model model) {
		ClubBean club = clubService.oneClubInfo(club_id);
		model.addAttribute("club", club);
		return "club/club_info";
	}

	// ✅ 클럽 가입
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
		} else {
			return "club/join_fail";
		}
	}

	// ✅ 클럽 생성 페이지
	@GetMapping("/create")
	public String create(@ModelAttribute ClubBean clubBean, Model model) {
		if (loginMember.getMember_id() != null) {
			List<String> categoryTypes = categoryService.getAllCategoryType();
			model.addAttribute("categoryType", categoryTypes);
			model.addAttribute("clubBean", clubBean);
			return "club/create";
		} else {
			return "club/join_fail";
		}
	}

	// ✅ AJAX로 소분류 카테고리 목록 가져오기
	@GetMapping("/get_sub_categories")
	@ResponseBody
	public List<Category> getSubCategories(@RequestParam("category_type") String categoryType) {
		return categoryService.categoryInfo(categoryType);
	}

	// ✅ 클럽 생성 처리 + 대표 이미지 저장
	@PostMapping("/create_pro")
	public String create_pro(@Valid ClubBean clubBean,
							 BindingResult result,
							 @RequestParam(value = "clubImage", required = false) MultipartFile clubImage,
							 Model model) {
		if (result.hasErrors()) {
			List<String> categoryTypes = categoryService.getAllCategoryType();
			model.addAttribute("categoryType", categoryTypes);

			if (clubBean.getClub_category() != null && !clubBean.getClub_category().isEmpty()) {
				Category category = categoryService.getCategoryByName(clubBean.getClub_category());
				if (category != null) {
					List<Category> subCategoryList = categoryService.categoryInfo(category.getCategory_type());
					model.addAttribute("subCategoryList", subCategoryList);
				}
			}
			return "club/create";
		}

		// ✅ 디렉토리 생성
		File uploadDir = new File(UPLOAD_DIR);
		if (!uploadDir.exists()) {
			boolean dirCreated = uploadDir.mkdirs();
			System.out.println("📁 클럽 프로필 폴더 생성됨: " + dirCreated);
		}

		// ✅ 기본 프로필 이미지 설정 (NULL 방지)
		String profileFileName = null;

		// ✅ 업로드된 파일이 있는 경우 저장
		if (clubImage != null && !clubImage.isEmpty()) {
			try {
				String originalFilename = clubImage.getOriginalFilename();
				if (originalFilename != null && !originalFilename.isEmpty()) {
					String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
					List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif");

					if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
						System.out.println("🚨 허용되지 않은 파일 확장자: " + fileExtension);
						return "redirect:/club/create?error=invalid_file_type";
					}

					// ✅ 파일명 생성
					profileFileName = UUID.randomUUID().toString() + "_" + originalFilename;
					File destFile = new File(UPLOAD_DIR + profileFileName);
					clubImage.transferTo(destFile);

					// ✅ 파일이 정상적으로 저장되었는지 확인
					if (destFile.exists()) {
						System.out.println("📌 클럽 프로필 저장 완료: " + profileFileName);
					} else {
						System.out.println("🚨 파일 저장 실패: " + profileFileName);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("🚨 클럽 프로필 저장 중 오류 발생: " + e.getMessage());
			}
		}

		// ✅ 클럽 생성 + 프로필 이미지 저장
		clubBean.setClub_profile(profileFileName);
		clubService.create_club(clubBean);

		// ✅ 생성된 클럽의 ID로 회장 자동 가입
		ClubBean newClubBean = clubService.searchClubName(clubBean.getClub_name());
		clubService.create_join_club(newClubBean.getClub_id(), loginMember.getMember_id());

		return "redirect:/category/category_info";
	}
}
