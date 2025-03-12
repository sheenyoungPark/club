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
}