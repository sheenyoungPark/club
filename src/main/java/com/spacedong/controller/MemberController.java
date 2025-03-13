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

	// 회원정보 수정 페이지 표시 - 수정됨
	@GetMapping("/editmemberinfo")
	public String editMemberInfo(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
		// 로그인 여부 확인 (isLogin() 메서드 사용)
		if (loginMember == null || !loginMember.isLogin()) {
			System.out.println("로그인 상태 확인: 로그인되지 않음");
			redirectAttributes.addFlashAttribute("message", "로그인이 필요한 서비스입니다.");
			return "redirect:/member/login";
		}

		System.out.println("로그인 상태 확인: 로그인됨 (ID: " + loginMember.getMember_id() + ")");
		model.addAttribute("loginMember", loginMember);
		return "member/editmemberinfo";
	}

	// 회원정보 수정 처리 - 수정됨
	@PostMapping("/editmemberinfo_pro")
	public String updateMemberInfo(@ModelAttribute MemberBean memberBean,
								   HttpSession session,
								   RedirectAttributes redirectAttributes) {
		try {
			// 로그인 여부 확인
			if (loginMember == null || !loginMember.isLogin()) {
				System.out.println("회원정보 수정 처리: 로그인 상태 아님");
				redirectAttributes.addFlashAttribute("message", "로그인이 필요한 서비스입니다.");
				return "redirect:/member/login";
			}

			System.out.println("회원정보 수정 요청 - ID: " + memberBean.getMember_id());
			System.out.println("변경된 닉네임: " + memberBean.getMember_nickname());
			System.out.println("변경된 전화번호: " + memberBean.getMember_phone());
			System.out.println("변경된 주소: " + memberBean.getMember_address());

			// ID 설정이 안 되어 있을 경우 세션의 값으로 설정
			if (memberBean.getMember_id() == null || memberBean.getMember_id().isEmpty()) {
				memberBean.setMember_id(loginMember.getMember_id());
				System.out.println("ID가 없어서 세션에서 가져옴: " + memberBean.getMember_id());
			}

			// 회원정보 업데이트
			memberService.editMember(memberBean);

			System.out.println("회원정보 업데이트 완료");
			System.out.println("현재 세션의 닉네임: " + loginMember.getMember_nickname());

			redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
			return "redirect:/member/editmemberinfo_success";
		} catch (Exception e) {
			System.out.println("회원정보 수정 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "회원정보 수정 중 오류가 발생했습니다: " + e.getMessage());
			return "redirect:/member/editmemberinfo";
		}
	}

	@GetMapping("/changePassword")
	public String changePasswordPage(Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		model.addAttribute("loginMember", new MemberBean());
		return "member/changePassword";
	}

	@PostMapping("/changePassword_pro")
	public String changePassword(@RequestParam("currentPassword") String currentPassword,
								 @RequestParam("newPassword") String newPassword,
								 @RequestParam("confirmPassword") String confirmPassword,
								 Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}

		// 현재 비밀번호 검증
		boolean isCorrectPassword = memberService.checkPassword(loginMember.getMember_id(), currentPassword);
		if (!isCorrectPassword) {
			model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
			return "member/changePassword";
		}

		// 비밀번호 길이 검사
		if (newPassword.length() < 8 || newPassword.length() > 16) {
			model.addAttribute("error", "비밀번호는 8~16자리여야 합니다.");
			return "member/changePassword";
		}

		// 새 비밀번호 확인 검사
		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("error", "새 비밀번호가 일치하지 않습니다.");
			return "member/changePassword";
		}

		// 비밀번호 업데이트
		memberService.updatePassword(loginMember.getMember_id(), newPassword);
		return "redirect:/member/changePassword_success";
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
	@GetMapping("/changePassword_success")
	public String changePasswordSuccess() {
		return "member/changePassword_success";
	}

	@GetMapping("/editmemberinfo_success")
	public String editMemberInfoSuccess() {
		return "member/editmemberinfo_success";
	}

	@GetMapping("/deleteAccount_success")
	public String deleteAccountSuccess() {
		return "member/deleteAccount_success";
	}
}