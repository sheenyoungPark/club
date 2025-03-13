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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

		if (memberBean.getMember_phone() == null || memberBean.getMember_phone().trim().isEmpty()) {
			result.rejectValue("member_phone", "error.member_phone", "휴대폰 번호를 입력해주세요.");
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


	@GetMapping("/deleteAccount")
	public String deleteAccountPage(Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		model.addAttribute("loginMember", loginMember);
		return "member/deleteAccount";
	}
	@PostMapping("/deleteAccount_pro")
	public String deleteAccount(@RequestParam("password") String password, Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}

		// 현재 비밀번호 확인
		boolean isCorrectPassword = memberService.checkPassword(loginMember.getMember_id(), password);
		if (!isCorrectPassword) {
			model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
			return "member/deleteAccount";
		}

		// 회원 탈퇴 처리
		memberService.deleteMember(loginMember.getMember_id());

		// 세션 초기화
		loginMember.setLogin(false);
		loginMember.setMember_id(null);
		loginMember.setMember_name(null);

		// 성공 페이지로 이동 (알림창 표시)
		return "member/deleteAccount_success";
	}

	@GetMapping("/deleteAccount_success")
	public String deleteAccountSuccess() {
		return "member/deleteAccount_success";
	}
	@GetMapping("/edit")
	public String editInfoVerification(Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		return "member/edit_verification";
	}

	// 비밀번호 확인 처리
	@PostMapping("/verify_password")
	public String verifyPassword(@RequestParam("password") String password,
								 Model model,
								 RedirectAttributes redirectAttributes) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}

		// 비밀번호 검증
		boolean isCorrectPassword = memberService.checkPassword(loginMember.getMember_id(), password);
		if (!isCorrectPassword) {
			model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
			return "member/edit_verification";
		}

		// 비밀번호 확인 성공 - 통합 정보 수정 페이지로 이동
		return "redirect:/member/integrated_edit";
	}

	// 통합 정보 수정 페이지
	@GetMapping("/integrated_edit")
	public String integratedEdit(Model model, HttpSession session) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}

		model.addAttribute("loginMember", loginMember);
		return "member/integrated_edit";
	}

	// 통합 정보 수정 처리
	@PostMapping("/integrated_update")
	public String integratedUpdate(
			@ModelAttribute MemberBean memberBean,
			@RequestParam(value = "newPassword", required = false) String newPassword,
			@RequestParam(value = "confirmPassword", required = false) String confirmPassword,
			RedirectAttributes redirectAttributes) {

		try {
			if (!loginMember.isLogin()) {
				redirectAttributes.addFlashAttribute("message", "로그인이 필요한 서비스입니다.");
				return "redirect:/member/login";
			}

			// 요청 정보 로깅
			System.out.println("정보 수정 요청 - ID: " + memberBean.getMember_id());
			System.out.println("변경된 닉네임: " + memberBean.getMember_nickname());
			System.out.println("변경된 전화번호: " + memberBean.getMember_phone());
			System.out.println("변경된 주소: " + memberBean.getMember_address());
			System.out.println("변경된 이메일: " + memberBean.getMember_email());

			// ID 설정
			if (memberBean.getMember_id() == null || memberBean.getMember_id().isEmpty()) {
				memberBean.setMember_id(loginMember.getMember_id());
			}

			// 비밀번호 변경 처리 (입력된 경우에만)
			if (newPassword != null && !newPassword.isEmpty()) {
				// 비밀번호 유효성 검사
				if (newPassword.length() < 8 || newPassword.length() > 16) {
					redirectAttributes.addFlashAttribute("error", "비밀번호는 8~16자리여야 합니다.");
					return "redirect:/member/integrated_edit";
				}

				// 비밀번호 일치 확인
				if (!newPassword.equals(confirmPassword)) {
					redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
					return "redirect:/member/integrated_edit";
				}

				// 비밀번호 업데이트
				memberService.updatePassword(loginMember.getMember_id(), newPassword);
				System.out.println("비밀번호 변경 완료");
			}

			// 회원정보 업데이트
			memberService.editMember(memberBean);
			System.out.println("회원정보 업데이트 완료");

			redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
			return "redirect:/member/memberinfo";
		} catch (Exception e) {
			System.out.println("회원정보 수정 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "회원정보 수정 중 오류가 발생했습니다: " + e.getMessage());
			return "redirect:/member/integrated_edit";
		}
	}
}