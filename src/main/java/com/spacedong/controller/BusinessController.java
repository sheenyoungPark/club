package com.spacedong.controller;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.CategoryBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.BusinessService;
import com.spacedong.service.CategoryService;
import com.spacedong.validator.BusinessValidator;
import com.spacedong.validator.MemberValidator;
import jakarta.annotation.Resource;
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

import java.util.List;

@Controller
@RequestMapping("/business")
public class BusinessController {
    private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);

    @Autowired
    public CategoryService categoryService;

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

    @GetMapping("/signup")
    public String signup(@ModelAttribute("businessBean") BusinessBean businessBean) {
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

        // 사업자 번호 검증은 프론트엔드에서 처리
        // 사업자 번호가 정상적이면 회원가입 진행
        businessService.businessJoin(businessBean);
        return "business/signup_success";
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


    @GetMapping("/category")
    public String category(Model model) {

        List<CategoryBean> categoryList = categoryService.categoryType();
        model.addAttribute("categoryList", categoryList);

        return "business/item_category"; // 대여 카테고리를 보여줄 HTML 페이지
    }

    @GetMapping("/category_info")
    public String getItemCategoryInfo(Model model) {

        return "business/category";
    }

}