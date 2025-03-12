package com.spacedong.controller;

import com.spacedong.validator.MemberValidator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import com.spacedong.beans.MemberBean;
import com.spacedong.service.MemberService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/member")
public class MemberController {

	@Autowired
	private MemberService memberService;

	@Resource(name="loginMember")
	private MemberBean loginMember;

	@InitBinder("memberBean")
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new MemberValidator());
	}

	@GetMapping("/login")
	public String login(@RequestParam(value = "fail", defaultValue = "false") boolean fail,
						@ModelAttribute("tempLoginMember") MemberBean memberBean, Model model) {
		model.addAttribute("fail", fail);
		return "member/login";
	}

	@PostMapping("/login_pro")
	public String login_pro(@ModelAttribute("tempLoginMember") MemberBean memberBean) {
		if (memberBean.getMember_id().equals("admin") && memberBean.getMember_pw().equals("admin")) {
			return "admin/init";
		} else {
			if (memberService.getLoginMember(memberBean)) {
				loginMember.setLogin(true);
				return "member/login_success";
			} else {
				return "member/login_fail";
			}
		}
	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}

	@GetMapping("/signup")
	public String signup(@ModelAttribute("memberBean") MemberBean memberBean) {
		return "member/signup";
	}

	@PostMapping("/signup_pro")
	public String signupPro(@Valid @ModelAttribute MemberBean memberBean, BindingResult result) {
		if (result.hasErrors()) {
			return "member/signup";
		}
		memberService.signupMember(memberBean);
		return "member/signup_success";
	}

	@GetMapping("/memberinfo")
	public String getMemberInfo(Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		model.addAttribute("loginMember", loginMember);
		model.addAttribute("maskedPhone", memberService.getMaskedPhone());
		return "member/memberinfo";
	}

	@GetMapping("/editmemberinfo")
	public String editMemberInfoPage(Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		model.addAttribute("loginMember", loginMember);
		return "member/editmemberinfo";
	}

	@PostMapping("/editmemberinfo_pro")
	public String updateMemberInfo(@ModelAttribute("loginMember") MemberBean memberBean) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		memberService.updateMember(memberBean);
		return "redirect:/member/memberinfo";
	}

	@GetMapping("/changePassword")
	public String changePasswordPage() {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		return "member/changePassword";
	}

	@GetMapping("/deleteAccount")
	public String deleteAccount() {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		memberService.deleteMember(loginMember.getMember_id());
		loginMember.setLogin(false);
		loginMember.setMember_id(null);
		loginMember.setMember_name(null);
		return "redirect:/";
	}

	private static final String UPLOAD_DIR = "C:/upload/image/profile/";

	/** ✅ 프로필 이미지 업데이트 */
	@PostMapping("/updateProfile")
	public String updateProfile(@RequestParam("profileImage") MultipartFile profileImage) {
		// 로그인 여부 확인
		if (!loginMember.isLogin()) {
			return "redirect:/member/login"; // 로그인되지 않은 경우 로그인 페이지로 이동
		}

		if (!profileImage.isEmpty()) {
			try {
				// ✅ 디렉토리가 없으면 자동 생성
				File dir = new File(UPLOAD_DIR);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				// ✅ 기존 파일 삭제 (중복 방지)
				if (loginMember.getMember_profile() != null) {
					File oldFile = new File(UPLOAD_DIR + loginMember.getMember_profile());
					if (oldFile.exists()) {
						oldFile.delete();
					}
				}

				// ✅ 새로운 파일 저장
				String fileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
				File destFile = new File(UPLOAD_DIR + fileName);
				profileImage.transferTo(destFile);

				// ✅ DB 업데이트
				memberService.updateMemberProfile(loginMember.getMember_id(), fileName);

				// ✅ 세션에 반영
				loginMember.setMember_profile(fileName);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "redirect:/member/memberinfo";
	}

	}







