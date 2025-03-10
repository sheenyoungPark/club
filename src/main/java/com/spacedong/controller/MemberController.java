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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spacedong.beans.MemberBean;
import com.spacedong.service.MemberService;

@Controller
@RequestMapping("/member")
public class MemberController {

	@Autowired
	private MemberService memberService;

	@Resource(name="loginUser")
	private MemberBean loginUser;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new MemberValidator());
	}

	@GetMapping("/login")
	public String login(@RequestParam(value = "fail", defaultValue = "false") boolean fail,
						@ModelAttribute("tempLoginUser") MemberBean memberBean, Model model) {
		model.addAttribute("fail",fail);

		return "member/login";
	}

	@PostMapping("/login_pro")
	public String login_pro(@ModelAttribute("tempLoginUser") MemberBean memberBean) {

		if(memberBean.getMember_id().equals("admin")&&memberBean.getMember_pw().equals("admin")) {
			return "admin/init";
		}else {
			if(memberService.getLoginUser(memberBean)) {
				return "member/login_success";
			}else {
				return "member/login_fail";
			}


		}

	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate(); // 세션 초기화 (세션에 담긴 모든 데이터 삭제)
		return "redirect:/"; // 로그아웃 후 메인 페이지로 이동
	}


	@GetMapping("signup")
	public String signup(@ModelAttribute MemberBean memberBean) {
		return "member/signup";
	}

	@PostMapping("/signup_pro")
	public String signupPro(@Valid @ModelAttribute MemberBean memberBean, BindingResult result) {

		System.out.println("컨트롤러: " + memberBean.getMember_id());

		if(result.hasErrors()) {
			return "member/signup";
		}
		memberService.signupUser(memberBean);
		return "member/signup_success";

	}


}
