package com.spacedong.controller;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.AdminService;
import com.spacedong.repository.ChatRepository;
import com.spacedong.service.ChatService;
import com.spacedong.service.MemberService;
import com.spacedong.service.SessionService;
import com.spacedong.validator.MemberValidator;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/member")
public class MemberController {

	@Autowired
	private MemberService memberService;

	@Autowired
	private AdminService adminService;

	@Autowired
	private ChatRepository chatRepository;

	// session‑scope 빈으로 정의된 loginMember와 loginBusiness를 주입받음
	@Resource(name = "loginMember")
	private MemberBean loginMember;

	@Resource(name = "loginBusiness")
	private BusinessBean loginBusiness;

	@Resource(name = "loginAdmin")
	private AdminBean loginAdmin;
    @Autowired
    private SessionService sessionService;

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
	public String login_pro(@ModelAttribute("tempLoginMember") MemberBean memberBean,
							Model model,
							RedirectAttributes redirectAttributes) {
		// 관리자 ID 확인
		boolean isAdmin = memberService.isAdminId(memberBean.getMember_id());

		// 관리자 계정으로 로그인 시도 감지
		if (isAdmin) {
			// 관리자 ID와 PW가 일치하는지 먼저 확인
			AdminBean tempAdmin = new AdminBean();
			tempAdmin.setAdmin_id(memberBean.getMember_id());
			tempAdmin.setAdmin_pw(memberBean.getMember_pw());

			// 관리자 로그인 정보를 임시로 저장해서 관리자 로그인 페이지로 전달
			redirectAttributes.addFlashAttribute("adminId", memberBean.getMember_id());

			// 관리자 로그인 페이지로 리다이렉트
			return "redirect:/admin/login";
		}

		// 일반 회원 로그인 로직은 그대로 유지
		sessionService.resetAllLoginSessions();

		// 일반 회원 로그인 처리
		if (memberService.getLoginMember(memberBean)) {
			MemberBean fullMember = memberService.selectMemberById(memberBean.getMember_id());
			if(fullMember != null && fullMember.getMember_pw().equals(memberBean.getMember_pw())){
				// 로그인 성공 처리...
				loginMember.setMember_id(fullMember.getMember_id());
				loginMember.setMember_pw(fullMember.getMember_pw());
				loginMember.setMember_nickname(fullMember.getMember_nickname());
				loginMember.setMember_phone(fullMember.getMember_phone());
				loginMember.setMember_email(fullMember.getMember_email());
				loginMember.setLogin(true);

				return "member/login_success";
			} else {
				return "member/login_fail";
			}
		} else {
			return "member/login_fail";
		}
	}

	@RequestMapping("/logout")
	public String logout() {
		// 모든 로그인 세션 초기화
		sessionService.resetAllLoginSessions();
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

	@GetMapping("/signup_choice")
	public String signup_choice() {
		return "member/signup_choice";
	}

	@GetMapping("/memberinfo")
	public String getMemberInfo(Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		MemberBean member =  memberService.getMemberById(loginMember.getMember_id());
		loginMember.setMember_point(member.getMember_point());

		model.addAttribute("loginMember", loginMember);

		model.addAttribute("maskedPhone", memberService.getMaskedPhone());
		// 내가 가입한 클럽 목록 가져오기
		List<ClubBean> joinedClubs = memberService.getJoinedClubs(loginMember.getMember_id());
		model.addAttribute("joinedClubs", joinedClubs);
		// 내가 작성한 게시글 목록 가져오기
		List<BoardBean> userPosts = memberService.getUserPosts(loginMember.getMember_id());
		model.addAttribute("userPosts", userPosts);
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
			return "member/login";
		}
		// 비밀번호 확인
		boolean isCorrectPassword = memberService.checkPassword(loginMember.getMember_id(), password);
		if (!isCorrectPassword) {
			model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
			return "member/deleteAccount";
		}
		// 회원 탈퇴 처리
		memberService.deleteMember(loginMember.getMember_id());
		// DI로 주입된 loginMember 초기화
		loginMember.setLogin(false);
		loginMember.setMember_id(null);
		loginMember.setMember_name(null);
		return "member/deleteAccount_success";
	}

	@GetMapping("/deleteAccount_success")
	public String deleteAccountSuccess() {
		return "member/deleteAccount_success";
	}

	@GetMapping("/edit")
	public String editInfoVerification(Model model) {
		if (!loginMember.isLogin()) {
			return "member/login";
		}
		// 비밀번호가 없는 경우 (예: SNS 로그인 사용자)에는 바로 정보 수정 페이지로 이동
		if (loginMember.getMember_pw() == null || loginMember.getMember_pw().isEmpty()) {
			return "redirect:/member/integrated_edit";
		}
		return "member/edit_verification";
	}

	@PostMapping("/verify_password")
	public String verifyPassword(@RequestParam("password") String password,
								 Model model,
								 RedirectAttributes redirectAttributes) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		// 비밀번호가 없으면 인증 없이 바로 통과
		if (loginMember.getMember_pw() == null || loginMember.getMember_pw().isEmpty()) {
			return "redirect:/member/integrated_edit";
		}
		boolean isCorrectPassword = memberService.checkPassword(loginMember.getMember_id(), password);
		if (!isCorrectPassword) {
			model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
			return "member/edit_verification";
		}
		return "redirect:/member/integrated_edit";
	}

	@GetMapping("/integrated_edit")
	public String integratedEdit(Model model) {
		if (!loginMember.isLogin()) {
			return "redirect:/member/login";
		}
		System.out.println("멤버 비밀번호: " + loginMember.getMember_pw());
		System.out.println("비밀번호 null 여부: " + (loginMember.getMember_pw() == null));
		model.addAttribute("loginMember", loginMember);
		return "member/integrated_edit";
	}

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
			// 정보 수정 요청 로깅
			System.out.println("정보 수정 요청 - ID: " + memberBean.getMember_id());
			System.out.println("변경된 닉네임: " + memberBean.getMember_nickname());
			System.out.println("변경된 전화번호: " + memberBean.getMember_phone());
			System.out.println("변경된 주소: " + memberBean.getMember_address());
			System.out.println("변경된 이메일: " + memberBean.getMember_email());
			if (memberBean.getMember_id() == null || memberBean.getMember_id().isEmpty()) {
				memberBean.setMember_id(loginMember.getMember_id());
			}
			// 비밀번호 변경 처리 (입력된 경우에만)
			if (loginMember.getMember_pw() != null && !loginMember.getMember_pw().isEmpty() &&
					newPassword != null && !newPassword.isEmpty()) {
				if (newPassword.length() < 8 || newPassword.length() > 16) {
					redirectAttributes.addFlashAttribute("error", "비밀번호는 8~16자리여야 합니다.");
					return "redirect:/member/integrated_edit";
				}
				if (!newPassword.equals(confirmPassword)) {
					redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
					return "redirect:/member/integrated_edit";
				}
				memberService.updatePassword(loginMember.getMember_id(), newPassword);
				System.out.println("비밀번호 변경 완료");
			}
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

	private static final String UPLOAD_DIR = "C:/upload/image/profile/";

	@PostMapping("/updateProfile")
	public String updateProfile(@RequestParam("profileImage") MultipartFile profileImage) {
		if (!profileImage.isEmpty()) {
			try {
				File dir = new File(UPLOAD_DIR);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				if (loginMember.getMember_profile() != null) {
					File oldFile = new File(UPLOAD_DIR + loginMember.getMember_profile());
					if (oldFile.exists()) {
						oldFile.delete();
					}
				}
				String fileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
				File destFile = new File(UPLOAD_DIR + fileName);
				profileImage.transferTo(destFile);
				memberService.updateMemberProfile(loginMember.getMember_id(), fileName);
				chatRepository.updateProfile(loginMember.getMember_id(), fileName);
				loginMember.setMember_profile(fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "redirect:/member/memberinfo";
	}

	/**
	 * 아이디 찾기 페이지 표시
	 */
	@GetMapping("/find_id")
	public String findIdForm() {
		return "member/find_id";
	}

	/**
	 * 아이디 찾기 처리
	 */
	@PostMapping("/find_id_pro")
	public String findId(@RequestParam("phone") String phone, Model model) {
		try {
			// 휴대폰 번호로 회원 아이디 찾기
			String foundId = memberService.findByPhone(phone);

			if (foundId != null) {
				model.addAttribute("foundId", foundId);
				model.addAttribute("message", "회원님의 아이디를 찾았습니다.");
				model.addAttribute("success", true);
			} else {
				model.addAttribute("message", "해당 휴대폰 번호로 등록된 회원 정보가 없습니다.");
				model.addAttribute("success", false);
			}
		} catch (Exception e) {
			model.addAttribute("message", "아이디 찾기 중 오류가 발생했습니다. 다시 시도해 주세요.");
			model.addAttribute("success", false);
		}

		return "member/find_id";
	}


	/**
	 * 비밀번호 찾기 페이지 표시
	 */
	@GetMapping("/find_password")
	public String findPasswordForm() {
		return "member/find_password";
	}

	/**
	 * 비밀번호 찾기 처리 (본인 확인)
	 */
	@PostMapping("/find_password_pro")
	public String findPassword(
			@RequestParam("member_id") String memberId,
			@RequestParam("phone") String phone,
			Model model) {

		try {
			// 아이디와 휴대폰 번호로 회원 확인
			MemberBean member = memberService.findMemberByIdAndPhone(memberId, phone);

			if (member != null) {

				// 본인 확인 성공 - 세션에 정보 저장
				model.addAttribute("message", "본인 확인이 완료되었습니다. 새 비밀번호를 설정해 주세요.");
				model.addAttribute("success", true);

				model.addAttribute("member_id", member.getMember_id());

			} else {
				model.addAttribute("message", "일치하는 회원 정보가 없습니다. 아이디와 휴대폰 번호를 확인해 주세요.");
				model.addAttribute("success", false);
			}
		} catch (Exception e) {
			model.addAttribute("message", "비밀번호 찾기 중 오류가 발생했습니다. 다시 시도해 주세요.");
			model.addAttribute("success", false);
		}

		return "member/reset_password";
	}

	/**
	 * 비밀번호 재설정 페이지 표시
	 */
	@GetMapping("/reset_password")
	public String resetPasswordForm(@RequestParam("member_id")String memberId, Model model) {
		// 세션에서 회원 아이디 확인

		if (memberId == null) {
			// 세션 정보가 없으면 비밀번호 찾기 페이지로 리다이렉트
			model.addAttribute("message", "비밀번호 재설정 세션이 만료되었습니다. 다시 시도해 주세요.");
			model.addAttribute("success", false);
			return "redirect:/member/find_password";
		}

		model.addAttribute("member_id", memberId);
		return "member/reset-password";
	}

	/**
	 * 비밀번호 재설정 처리
	 */
	@PostMapping("/reset_password_pro")
	public String resetPassword(
			@RequestParam("member_id") String memberId,
			@RequestParam("new_password") String newPassword,
			RedirectAttributes redirectAttributes) {

			// 새 비밀번호 설정
			memberService.newpassowrd(memberId, newPassword);

			return "member/reset_password_success";

	}

}