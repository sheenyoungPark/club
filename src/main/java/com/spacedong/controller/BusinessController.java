package com.spacedong.controller;

import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.CategoryBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.BusinessService;
import com.spacedong.service.CategoryService;
import com.spacedong.validator.BusinessValidator;
import com.spacedong.validator.MemberValidator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.File;
import java.util.List;
import java.util.Collections;
import org.springframework.http.MediaType;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;
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

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/business")
public class BusinessController {
    private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);
    private static final String UPLOAD_DIR = "C:/upload/image/business/";

    @Autowired
    public CategoryService categoryService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private BusinessValidator businessValidator;

    // session‑scope 빈(loginBusiness)을 주입받음 (Spring 설정 필요)
    @Resource(name = "loginBusiness")
    private BusinessBean loginBusiness;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        if (binder.getTarget() instanceof MemberBean) {
            binder.addValidators(new MemberValidator());
        } else if (binder.getTarget() instanceof BusinessBean) {
            binder.addValidators(new BusinessValidator(businessService));
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
    public String login(@ModelAttribute("temploginBusiness") BusinessBean temploginBusiness, Model model) {
        model.addAttribute("businessBean", new BusinessBean());
        return "member/login";
    }

    @PostMapping("/login_pro")
    public String login_pro(@ModelAttribute BusinessBean businessBean) {
        // null 체크
        if (businessBean.getBusiness_id() == null || businessBean.getBusiness_pw() == null) {
            return "/member/login_fail";
        }
        if (businessService.getLoginBusiness(businessBean)) {
            // DB에서 전체 사업자 정보를 조회 (예: businessService.getBusinessById)
            BusinessBean fullBusiness = businessService.selectBusinessById(businessBean.getBusiness_id());
            if(fullBusiness != null && fullBusiness.getBusiness_pw().equals(businessBean.getBusiness_pw())){
                loginBusiness.setBusiness_id(fullBusiness.getBusiness_id());
                loginBusiness.setBusiness_pw(fullBusiness.getBusiness_pw());
                loginBusiness.setBusiness_profile(fullBusiness.getBusiness_profile());
                loginBusiness.setBusiness_name(fullBusiness.getBusiness_name());
                // 필요한 다른 필드들도 업데이트
                loginBusiness.setLogin(true);
                return "business/login_success";
            } else {
                return "member/login_fail";
            }
        } else {
            return "member/login_fail";
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        // DI로 주입된 loginBusiness 빈의 로그인 플래그를 false로 설정
        if (loginBusiness != null) {
            loginBusiness.setLogin(false);
        }
        // (필요 시 session에서 loginBusiness를 제거할 수 있음)
        session.removeAttribute("loginBusiness");
        // 일반 회원 로그아웃 처리
        MemberBean loginMember = (MemberBean) session.getAttribute("loginMember");
        if (loginMember != null) {
            loginMember.setLogin(false);
            session.removeAttribute("loginMember");
        }
        return "redirect:/";
    }

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

    // 간소화된 사업자 정보 페이지 메소드
    @GetMapping("/info")
    public String businessInfo(Model model) {
        // 로그인 검증
        if (loginBusiness == null || !loginBusiness.isLogin()) {
            logger.info("사용자가 로그인하지 않았습니다. 로그인 페이지로 리다이렉트합니다.");
            return "redirect:/member/login";
        }

        String businessId = loginBusiness.getBusiness_id();
        logger.info("businessInfo 메소드 호출 - 사업자 ID: {}", businessId);

        try {
            // 아이템 목록 조회
            List<BusinessItemBean> businessItems = businessService.getBusinessItems(businessId);
            logger.info("사업자 아이템 조회 성공 - 항목 수: {}", businessItems.size());

            // 게시글 목록 조회
            List<BoardBean> businessPosts = businessService.getBusinessPosts(loginBusiness.getBusiness_id());
            logger.info("사업자 게시글 조회 성공 - 항목 수: {}", businessPosts.size());

            model.addAttribute("loginBusiness", loginBusiness);
            model.addAttribute("businessItems", businessItems);
            model.addAttribute("businessPosts", businessPosts);

            return "business/businessinfo";
        } catch (Exception e) {
            logger.error("사업자 정보 페이지 로드 중 오류 발생: ", e);
            return "redirect:/";
        }
    }

    // 프로필 업데이트
    @PostMapping("/updateprofile")
    public String updateprofile(@RequestParam("logoImage") MultipartFile profileImage) {
        if (loginBusiness == null || !loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }

        try {
            if (!profileImage.isEmpty()) {
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // 파일명 생성 및 저장
                String fileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                File destFile = new File(UPLOAD_DIR + fileName);
                profileImage.transferTo(destFile);

                // DB 업데이트
                businessService.updateBusinessProfile(loginBusiness.getBusiness_id(), fileName);
                loginBusiness.setBusiness_profile(fileName);

                logger.info("프로필 이미지 업데이트 성공: {}", fileName);
            }
        } catch (Exception e) {
            logger.error("프로필 업데이트 중 오류 발생: ", e);
        }

        return "redirect:/business/info";
    }

    @GetMapping("/edit")
    public String editInfoVerification(Model model) {
        if (loginBusiness == null || !loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }
        return "business/edit_verification";
    }
    // 비밀번호 확인 처리
    @PostMapping("/verify_password")
    public String verifyPassword(@RequestParam("password") String password,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (loginBusiness == null || !loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }
        boolean isCorrectPassword = businessService.checkPassword(loginBusiness.getBusiness_id(), password);
        if (!isCorrectPassword) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "business/edit_verification";
        }
        return "redirect:/business/integrated_edit";
    }

    @GetMapping("/integrated_edit")
    public String integratedEdit(Model model) {
        if (loginBusiness == null || !loginBusiness.isLogin()) {
            return "redirect:/member/login";
        }
        System.out.println("멤버 비밀번호: " + loginBusiness.getBusiness_pw());
        System.out.println("비밀번호 null 여부: " + (loginBusiness.getBusiness_pw() == null));
        model.addAttribute("loginBusiness", loginBusiness);
        return "business/integrated_edit";
    }

    @PostMapping("/integrated_update")
    public String integratedUpdate(@ModelAttribute BusinessBean businessBean,
                                   @RequestParam(value = "newPassword", required = false) String newPassword,
                                   @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                   RedirectAttributes redirectAttributes) {
        if (loginBusiness == null || !loginBusiness.isLogin()) {
            redirectAttributes.addFlashAttribute("message", "로그인이 필요한 서비스입니다.");
            return "redirect:/member/login";
        }
        try {
            if (loginBusiness.getBusiness_pw() != null && !loginBusiness.getBusiness_pw().isEmpty() &&
                    newPassword != null && !newPassword.isEmpty()) {
                if (newPassword.length() < 8 || newPassword.length() > 16) {
                    redirectAttributes.addFlashAttribute("error", "비밀번호는 8~16자리여야 합니다.");
                    return "redirect:/business/integrated_edit";
                }
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                    return "redirect:/business/integrated_edit";
                }
                businessService.updatePassword(loginBusiness.getBusiness_id(), newPassword);
            }
            businessService.editBusinessWithValidation(businessBean);
            redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
            return "redirect:/business/info";
        } catch (Exception e) {
            logger.error("회원정보 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "회원정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/business/integrated_edit";
        }
    }


    //컨트롤러를 분리해야되나 고민중
    @GetMapping("/category")
    public String category(Model model) {

        List<CategoryBean> categoryList = categoryService.categoryType();
        List<BusinessItemBean> itemList = businessService.allItem();
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("itemList", itemList);

        return "business/item_category"; // 대여 카테고리를 보여줄 HTML 페이지
    }

    //아이템 등록
    @GetMapping("create_item")
    public String create_item( @ModelAttribute BusinessItemBean businessItemBean, Model model){

        if (loginBusiness.getBusiness_id() != null) {
            List<String> categoryTypes = categoryService.getAllCategoryType();
            model.addAttribute("categoryType", categoryTypes);
            model.addAttribute("businessItemBean", businessItemBean);
            return "business/create_item";
        } else {
            return "club/join_fail";
        }

    }

    // ✅ AJAX로 소분류 카테고리 목록 가져오기
    @GetMapping("/get_sub_categories")
    @ResponseBody
    public List<CategoryBean> getSubCategories(@RequestParam("category_type") String categoryType) {
        return categoryService.categoryInfo(categoryType);
    }

    @GetMapping("/category_info")
    public String getItemCategoryInfo(Model model) {

        return "business/category";
    }
    // ✅ 아이템 등록 처리 + 이미지 저장
    @PostMapping("create_item_pro")
    public String create_item_pro(@Valid BusinessItemBean businessItemBean,
                                  BindingResult result,
                                  @RequestParam(value = "itemImage", required = false) MultipartFile itemImage,
                                  Model model) {

        System.out.println("business category : " + businessItemBean.getItem_category());

        // 유효성 검사 오류 처리
        if (result.hasErrors()) {
            List<String> categoryTypes = categoryService.getAllCategoryType();
            model.addAttribute("categoryType", categoryTypes);

            if (businessItemBean.getItem_category() != null && !businessItemBean.getItem_category().isEmpty()) {
                List<CategoryBean> subCategoryList = categoryService.categoryInfo(businessItemBean.getItem_category());
                model.addAttribute("subCategoryList", subCategoryList);
            }
            return "business/create_item";
        }

        // 비즈니스 ID 설정
        businessItemBean.setBusiness_id(loginBusiness.getBusiness_id());
        System.out.println("business_id : " + businessItemBean.getBusiness_id());

        // ✅ 디렉토리 생성 (상수로 정의된 업로드 디렉토리 경로 사용)
        final String UPLOAD_DIR = "src/main/resources/static/upload/item/"; // 적절한 경로로 수정하세요
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean dirCreated = uploadDir.mkdirs();
            System.out.println("📁 아이템 이미지 폴더 생성됨: " + dirCreated);
        }

        // ✅ 기본 이미지 설정 (NULL 방지)
        String imageFileName = "기본이미지.png"; // 기본 이미지 파일명

        // ✅ 업로드된 파일이 있는 경우 저장
        if (itemImage != null && !itemImage.isEmpty()) {
            try {
                String originalFilename = itemImage.getOriginalFilename();
                if (originalFilename != null && !originalFilename.isEmpty()) {
                    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif");

                    if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
                        System.out.println("🚨 허용되지 않은 파일 확장자: " + fileExtension);
                        return "redirect:/business/create_item?error=invalid_file_type";
                    }

                    // ✅ 파일명 생성
                    imageFileName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File destFile = new File(UPLOAD_DIR + imageFileName);
                    itemImage.transferTo(destFile);

                    // ✅ 파일이 정상적으로 저장되었는지 확인
                    if (destFile.exists()) {
                        System.out.println("📌 아이템 이미지 저장 완료: " + imageFileName);
                    } else {
                        System.out.println("🚨 파일 저장 실패: " + imageFileName);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("🚨 아이템 이미지 저장 중 오류 발생: " + e.getMessage());
            }
        }

        // ✅ 아이템 생성 + 이미지 경로 저장
        businessItemBean.setItem_img(imageFileName);
        businessService.create_item(businessItemBean);

        return "redirect:/business/category"; // 또는 적절한 리다이렉트 경로
    }


    // 아이템 상세 정보 보기
    @GetMapping("/item_info")
    public String itemInfo(@RequestParam("item_id") String itemId, Model model) {
        // 아이템 정보 가져오기
        BusinessItemBean item = businessService.getItemById(itemId);

        if (item == null) {
            return "business/item_not_found";
        }

        // 판매자 정보 가져오기
        BusinessBean businessInfo = businessService.getBusinessById(item.getBusiness_id());

        // 예약된 시간 목록 가져오기 (오늘 날짜 기준)
        List<Integer> reservedTimes = businessService.getReservedTimesByItemIdAndDate(
                itemId,
                java.time.LocalDate.now().toString()
        );

        model.addAttribute("item", item);
        model.addAttribute("businessInfo", businessInfo);
        model.addAttribute("reservedTimes", reservedTimes);

        return "reservation/item_info";
    }



    // 아이템 수정 페이지
    @GetMapping("/edit_item")
    public String editItem(
            @RequestParam("item_id") String itemId,
            Model model) {

        BusinessItemBean item = businessService.getItemById(loginBusiness.getBusiness_id());

        // 자신이 등록한 아이템인지 확인
        if (!item.getBusiness_id().equals(loginBusiness.getBusiness_id())) {
            return "redirect:/business/category";
        }

        List<String> categoryTypes = categoryService.getAllCategoryType();
        model.addAttribute("categoryType", categoryTypes);
        model.addAttribute("businessItemBean", item);

        return "business/edit_item";
    }

    // 아이템 수정 처리
    @PostMapping("/edit_item_pro")
    public String editItemPro(
            @ModelAttribute BusinessItemBean businessItemBean,
            @RequestParam(value = "itemImage", required = false) MultipartFile itemImage) {

        // 기존 아이템 정보 가져오기
        BusinessItemBean existingItem = businessService.getItemById(businessItemBean.getItem_id());

        // 자신이 등록한 아이템인지 확인
        if (!existingItem.getBusiness_id().equals(loginBusiness.getBusiness_id())) {
            return "redirect:/business/category";
        }

        // 이미지 처리
        if (itemImage != null && !itemImage.isEmpty()) {
            try {
                // 파일 저장 경로 설정
                String uploadPath = "src/main/resources/static/image/itemImage/";
                File uploadDir = new File(uploadPath);

                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // 이전 이미지 파일 삭제
                if (existingItem.getItem_img() != null) {
                    File oldFile = new File(uploadPath + existingItem.getItem_img());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // 파일명 생성 (아이템ID + 원본 파일 확장자)
                String originalFilename = itemImage.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String savedFilename = businessItemBean.getItem_id() + fileExtension;

                // 파일 저장
                File dest = new File(uploadPath + savedFilename);
                itemImage.transferTo(dest);

                // DB에 저장할 이미지 경로 설정
                businessItemBean.setItem_img(savedFilename);

            } catch (Exception e) {
                logger.error("이미지 업로드 오류: {}", e.getMessage());
            }
        } else {
            // 이미지를 변경하지 않은 경우 기존 이미지 유지
            businessItemBean.setItem_img(existingItem.getItem_img());
        }

        // 판매자 ID 설정 (변경 불가)
        businessItemBean.setBusiness_id(existingItem.getBusiness_id());

        // 아이템 업데이트
        businessService.updateItem(businessItemBean);

        return "redirect:/business/item_info?item_id=" + businessItemBean.getItem_id();
    }

    // 아이템 삭제
    @GetMapping("/delete_item")
    public String deleteItem(@RequestParam("item_id") String itemId) {
        BusinessItemBean item = businessService.getItemById(itemId);

        // 자신이 등록한 아이템인지 확인
        if (item.getBusiness_id().equals(loginBusiness.getBusiness_id())) {
            // 이미지 파일 삭제
            if (item.getItem_img() != null) {
                String uploadPath = "src/main/resources/static/image/itemImage/";
                File imageFile = new File(uploadPath + item.getItem_img());
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }

            // 아이템 삭제
            businessService.deleteItem(itemId);
        }

        return "redirect:/business/category";
    }

}