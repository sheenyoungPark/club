package com.spacedong.controller;

import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.BusinessService;
import com.spacedong.validator.BusinessValidator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/business")
public class BusinessController {
    private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);
    private static final String UPLOAD_DIR = "C:/upload/image/business/";

    @Autowired
    private BusinessService businessService;

    @Autowired
    private BusinessValidator businessValidator;

    @Resource(name = "loginBusiness")
    private BusinessBean loginBusiness;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        if (binder.getTarget() instanceof BusinessBean) {
            binder.addValidators(businessValidator);
        }
    }


    @GetMapping("/signup")
    public String signup(@ModelAttribute("businessBean") BusinessBean businessBean) {
        // 기본값 설정
        businessBean.setBusiness_public("WAIT");
        return "business/signup";
    }

    @PostMapping("/business_signup_pro")
    public String business_signup_pro(@Valid @ModelAttribute BusinessBean businessBean, BindingResult result) {
        if (result.hasErrors()) {
            return "business/signup";
        }

        // 사업자 번호 하이픈(-) 제거
        String cleanBusinessNumber = businessBean.getBusiness_number().replaceAll("-", "");
        businessBean.setBusiness_number(cleanBusinessNumber);

        // 값 검증 및 설정
        if (!"PASS".equals(businessBean.getBusiness_public())) {
            businessBean.setBusiness_public("WAIT");
        }

        businessService.businessJoin(businessBean);
        return "business/signup_success";
    }

    @GetMapping("/login")
    public String login(@ModelAttribute("temploginBusiness") BusinessBean temploginBusiness, Model model) {
        model.addAttribute("businessBean", new BusinessBean());
        return "member/login";
    }

    @PostMapping("/login_pro")
    public String login_pro(@ModelAttribute BusinessBean businessBean, HttpSession session) {
        // null 체크 추가
        if (businessBean.getBusiness_id() == null || businessBean.getBusiness_pw() == null) {
            return "/member/login_fail";
        }

        if (businessService.getLoginBusiness(businessBean)) {
            // 비즈니스 로그인 성공

            // 기존에 일반 회원으로 로그인되어 있다면 로그아웃 처리
            MemberBean loginMember = (MemberBean) session.getAttribute("loginMember");
            if (loginMember != null) {
                loginMember.setLogin(false);
            }

            // 비즈니스 로그인 처리
            BusinessBean loginBusiness = (BusinessBean) session.getAttribute("loginBusiness");
            if (loginBusiness != null) {
                loginBusiness.setLogin(true);
            }

            return "/business/login_success";
        } else {
            return "/member/login_fail";
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        // 두 계정 유형 모두 로그아웃 처리
        MemberBean loginMember = (MemberBean) session.getAttribute("loginMember");
        BusinessBean loginBusiness = (BusinessBean) session.getAttribute("loginBusiness");

        if (loginMember != null) {
            loginMember.setLogin(false);
        }

        if (loginBusiness != null) {
            loginBusiness.setLogin(false);
        }

        return "redirect:/";
    }

    // 아이디 중복 확인
    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(@RequestParam String business_id) {
        return businessService.checkId(business_id) ? "true" : "false";
    }

    @GetMapping("/checkEmail")
    @ResponseBody
    public String checkEmail(@RequestParam String business_email) {
        return businessService.checkEmail(business_email) ? "true" : "false";
    }
    // 사업자 내 정보 페이지 요청 처리
    @GetMapping("/info")
    public String businessInfo(Model model) {
        // 로그인 여부 확인
        if (!loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }

        // 사업자 관련 모든 데이터를 BusinessService에서 조회
        String businessId = loginBusiness.getBusiness_id();

        // 1. 사업자가 등록한 상품 목록
        List<BusinessItemBean> businessItems = businessService.getBusinessItems(businessId);

        // 2. 작성한 게시글 목록
        List<BoardBean> businessPosts = businessService.getBusinessPosts(businessId);

        // 모델에 데이터 추가
        model.addAttribute("businessItems", businessItems);  // 변수명 수정
        model.addAttribute("businessPosts", businessPosts);

        return "business/businessinfo";
    }

    @PostMapping("/updateprofile")
    public String updateprofile(@RequestParam("logoImage") MultipartFile profileImage) {
        // 로그인 여부 확인
        if (!loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }

        if (!profileImage.isEmpty()) {
            try {
                // 디렉토리가 없으면 자동 생성
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // 기존 파일 삭제 (중복 방지)
                if (loginBusiness.getBusiness_profile() != null && !loginBusiness.getBusiness_profile().isEmpty()) {
                    File oldFile = new File(UPLOAD_DIR + loginBusiness.getBusiness_profile());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // 새로운 파일 저장
                String fileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                File destFile = new File(UPLOAD_DIR + fileName);
                profileImage.transferTo(destFile);

                // DB 업데이트
                businessService.updateBusinessProfile(loginBusiness.getBusiness_id(), fileName);

                // 세션에 반영
                loginBusiness.setBusiness_profile(fileName);
            } catch (Exception e) {
                logger.error("프로필 업데이트 중 오류 발생", e);
                return "redirect:/business/info?error=true";
            }
        }

        return "redirect:/business/info";
    }
    @GetMapping("/edit")
    public String editInfoVerification(Model model) {
        if (!loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }

        return "business/edit_verification";
    }
    // 비밀번호 확인 처리
    @PostMapping("/verify_password")
    public String verifyPassword(@RequestParam("password") String password,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (!loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }

        // 비밀번호 검증
        boolean isCorrectPassword = businessService.checkPassword(loginBusiness.getBusiness_id(), password);
        if (!isCorrectPassword) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "business/edit_verification";
        }

        // 비밀번호 확인 성공 - 통합 정보 수정 페이지로 이동
        return "redirect:/business/integrated_edit";
    }

    // 통합 정보 수정 페이지
    @GetMapping("/integrated_edit")
    public String integratedEdit(Model model, HttpSession session) {
        if (!loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }
        System.out.println("멤버 비밀번호: " + loginBusiness.getBusiness_pw());
        System.out.println("비밀번호 null 여부: " + (loginBusiness.getBusiness_pw() == null));

        model.addAttribute("loginBusiness", loginBusiness);
        return "business/integrated_edit";  // 경로 수정
    }

    // 통합 정보 수정 처리 (수정된 메서드)
    @PostMapping("/integrated_update")
    public String integratedUpdate(
            @ModelAttribute BusinessBean businessBean,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        try {
            if (!loginBusiness.isLogin()) {
                redirectAttributes.addFlashAttribute("message", "로그인이 필요한 서비스입니다.");
                return "redirect:/member/login";
            }

            // 비밀번호 변경 처리 (입력된 경우에만)
            if (loginBusiness.getBusiness_pw() != null && !loginBusiness.getBusiness_pw().isEmpty() &&
                    newPassword != null && !newPassword.isEmpty()) {
                // 비밀번호 유효성 검사
                if (newPassword.length() < 8 || newPassword.length() > 16) {
                    redirectAttributes.addFlashAttribute("error", "비밀번호는 8~16자리여야 합니다.");
                    return "redirect:/business/integrated_edit";
                }

                // 비밀번호 일치 확인
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                    return "redirect:/business/integrated_edit";
                }

                // 비밀번호 업데이트
                businessService.updatePassword(loginBusiness.getBusiness_id(), newPassword);
            }

            // 회원정보 업데이트 (수정된 메서드 사용)
            businessService.editBusinessWithValidation(businessBean);

            redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
            return "redirect:/business/info";
        } catch (Exception e) {
            logger.error("회원정보 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "회원정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/business/integrated_edit";
        }
    }
}