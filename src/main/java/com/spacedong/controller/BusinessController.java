package com.spacedong.controller;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.BusinessService;
import com.spacedong.validator.BusinessValidator;
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

@Controller
@RequestMapping("/business")
public class BusinessController {



    @Autowired
    private BusinessService businessService;

    @Resource(name = "loginBusiness")
    private BusinessBean loginBusiness;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        if (binder.getTarget() instanceof MemberBean) {
            binder.addValidators(new MemberValidator());
        } else if (binder.getTarget() instanceof BusinessBean) {
            binder.addValidators(new BusinessValidator());
        }
    }

    @GetMapping("/business_signup")
    public String business_signup(@ModelAttribute BusinessBean businessBean) {
        return "business/business_signup"; // 템플릿: templates/business/business_signup.html
    }

    @PostMapping("/business_signup_pro")
    public String business_signup_pro(@Valid @ModelAttribute BusinessBean businessBean, BindingResult result) {

        if (result.hasErrors()) {
            return "business/business_signup"; // 회원가입 실패 시, 기존 페이지로 돌아감
        }

        businessService.businessJoin(businessBean);
        return "business/signup_success"; // 템플릿: templates/business/signup_success.html
    }

    @GetMapping("/login")
    public String login(@ModelAttribute BusinessBean businessBean, Model model) {
        model.addAttribute("memberType", "business");
        return "member/login"; // 템플릿: templates/member/login.html
    }

    @PostMapping("/login_pro")
    public String login_pro(@ModelAttribute BusinessBean businessBean) {
        if (businessService.getLoginMember(businessBean)) {
            return "/business/login_success"; // 로그인 성공 시 리디렉트
        } else {
            return "/member/login_fail"; // 로그인 실패 시, 로그인 페이지로 리디렉트 + 에러 표시
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 초기화 (로그아웃)
        return "redirect:/"; // 로그아웃 후 메인 페이지로 이동
    }
}
