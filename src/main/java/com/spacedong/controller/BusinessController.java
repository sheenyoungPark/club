package com.spacedong.controller;

import com.spacedong.beans.*;
import com.spacedong.service.*;
import com.spacedong.validator.BusinessValidator;
import com.spacedong.validator.MemberValidator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

@Controller
@RequestMapping("/business")
public class BusinessController {
    private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);
    private static final String UPLOAD_DIR = "C:/upload/image/business/";

    @Autowired
    public CategoryService categoryService;

    @Autowired
    public ReservationService reservationService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AdminNotificationService adminNotificationService;

    @Autowired
    private BusinessValidator businessValidator;

    // session‑scope 빈(loginBusiness)을 주입받음 (Spring 설정 필요)
    @Resource(name = "loginBusiness")
    private BusinessBean loginBusiness;

    @Resource(name = "loginMember")
    private MemberBean loginMember;
    @Autowired
    private SessionService sessionService;

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
        adminNotificationService.sendApprovalNotification("admin", "ADMIN", "REQUEST2", "판매자 " + businessBean.getBusiness_name(),"");

        return "business/signup_success";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "fail", defaultValue = "false") boolean fail,
            @ModelAttribute("temploginBusiness") BusinessBean temploginBusiness,
            Model model
    ) {
        model.addAttribute("businessBean", new BusinessBean());
        model.addAttribute("fail", fail);
        return "member/login";
    }

    @PostMapping("/login_pro")
    public String login_pro(@ModelAttribute BusinessBean businessBean) {
        // null 체크
        if (businessBean.getBusiness_id() == null || businessBean.getBusiness_pw() == null) {
            return "/member/login_fail";
        }

        sessionService.resetAllLoginSessions();

        if (businessService.getLoginBusiness(businessBean)) {
            // DB에서 전체 사업자 정보를 조회
            BusinessBean fullBusiness = businessService.selectBusinessById(businessBean.getBusiness_id());
            if (fullBusiness != null && fullBusiness.getBusiness_pw().equals(businessBean.getBusiness_pw())) {
                // business_public이 WAIT이면 로그인 실패 처리
                if ("WAIT".equalsIgnoreCase(fullBusiness.getBusiness_public())) {
                    // 승인 대기중인 경우 로그인 거부
                    System.out.println("로그인 거부: 사업자 승인 대기중입니다.");
                    return "business/login_wait"; // 또는 별도의 승인 대기 안내 페이지로 리다이렉트
                }
                // 승인 상태(PASS)인 경우에만 로그인 성공 처리
                loginBusiness.setBusiness_id(fullBusiness.getBusiness_id());
                loginBusiness.setBusiness_pw(fullBusiness.getBusiness_pw());
                loginBusiness.setBusiness_profile(fullBusiness.getBusiness_profile());
                loginBusiness.setBusiness_name(fullBusiness.getBusiness_name());
                // 필요한 다른 필드들도 업데이트
                loginBusiness.setLogin(true);

                System.out.println("기업회원 로그인 성공: " + fullBusiness.getBusiness_id());
                System.out.println("개인회원 로그인 상태: " + loginMember.isLogin());

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
        MemberBean loginBusiness = (MemberBean) session.getAttribute("loginBusiness");
        if (loginBusiness != null) {
            loginBusiness.setLogin(false);
            session.removeAttribute("loginBusiness");
        }
        return "redirect:/";
    }

    @GetMapping("/login_fail")
    public String loginFail() {
        return "business/login_fail";
    }

    @GetMapping("/login_wait")
    public String loginWait() {
        return "business/login_wait";
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

        BusinessBean business = businessService.getBusinessInfoById(loginBusiness.getBusiness_id());
        loginBusiness.setBusiness_point(business.getBusiness_point());



        String businessId = loginBusiness.getBusiness_id();
        logger.info("businessInfo 메소드 호출 - 사업자 ID: {}", businessId);

        BusinessBean bs = businessService.getBusinessInfoById(businessId);

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

    @GetMapping("/deleteAccount")
    public String deleteAccountPage(Model model) {
        if (!loginBusiness.isLogin()) {
            return "redirect:/business/login";
        }
        model.addAttribute("loginBusiness", loginBusiness);
        return "business/deleteAccount";
    }

    @PostMapping("/deleteAccount_pro")
    public String deleteAccount(@RequestParam("password") String password, Model model) {
        if (!loginBusiness.isLogin()) {
            return "redirect:/business/login";
        }
        // 비밀번호 확인
        boolean isCorrectPassword = businessService.checkPassword(loginBusiness.getBusiness_id(), password);
        if (!isCorrectPassword) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "business/deleteAccount";
        }
        // 회원 탈퇴 처리
        businessService.deleteBusiness(loginBusiness.getBusiness_id());
        // DI로 주입된 loginBusiness 초기화
        loginBusiness.setLogin(false);
        loginBusiness.setBusiness_id(null);
        loginBusiness.setBusiness_name(null);
        return "business/deleteAccount_success";
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

    @GetMapping("/category_info")
    public String getCategoryInfo(
            @RequestParam(value = "category_type", required = false, defaultValue = "all") String categoryType,
            @RequestParam(value = "sub_category", required = false, defaultValue = "all") String subCategory,
            Model model) {

        // 메인 카테고리 정보 가져오기
        List<CategoryBean> categoryList = categoryService.categoryType();
        model.addAttribute("categoryList", categoryList);

        // 서브 카테고리 정보 가져오기 (categoryType이 null이면 "all"로 처리)
        categoryType = (categoryType != null) ? categoryType : "all";
        List<CategoryBean> subCategoryList = categoryService.categoryInfo(categoryType);
        model.addAttribute("list", subCategoryList);

        // 선택한 서브 카테고리에 해당하는 동호회 목록 가져오기
        List<BusinessItemBean> itemList ;


        if("all".equals(categoryType)) {
            itemList = itemService.getAllItems();
        }
        else if ("all".equals(subCategory)) {
            itemList = itemService.getItemByCategory(categoryType);
        } else {
            itemList = itemService.getItemBySubCategory(subCategory);
        }

        model.addAttribute("itemList", itemList);

        return "business/item_category";
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
        final String UPLOAD_DIR = "C:/upload/image/businessitem/"; // 적절한 경로로 수정하세요
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
                LocalDate.now().toString()
        );


// ✅ 리뷰 리스트 가져오기
        List<ReservationReviewBean> reviews = reservationService.getReviewsByItemId(itemId);

        // ✅ 평균 평점 계산
        double averageRating = 0.0;
        if (!reviews.isEmpty()) {
            double total = 0;
            for (ReservationReviewBean review : reviews) {
                total += review.getRating();
            }
            averageRating = total / reviews.size();
        }

        model.addAttribute("item", item);
        model.addAttribute("businessInfo", businessInfo);
        model.addAttribute("reservedTimes", reservedTimes);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);

        return "reservation/item_info";
    }

    // 아이템 수정 페이지
    @GetMapping("/edit_item")
    public String editItem(
            @RequestParam("item_id") String itemId,
            Model model) {
        BusinessItemBean businessItemBean = itemService.getItemById(itemId);
        // 자신이 등록한 아이템인지 확인
        if (!businessItemBean.getBusiness_id().equals(loginBusiness.getBusiness_id())) {
            return "redirect:/business/category";
        }
        List<String> categoryTypes = categoryService.getAllCategoryType();

        model.addAttribute("categoryType", categoryTypes);
        model.addAttribute("businessItemBean", businessItemBean);

        return "business/edit_item";
    }

    // 아이템 수정 처리
    @PostMapping("/edit_item_pro")
    public String editItemPro(
            @ModelAttribute BusinessItemBean businessItemBean,
            @RequestParam(value = "itemImage", required = false) MultipartFile itemImage) {

        // 기존 아이템 정보 가져오기
        BusinessItemBean existingItem = itemService.getItemById(businessItemBean.getItem_id());
        // 아이템이 존재하는지 확인
        if (existingItem == null) {
            return "redirect:/business/category?error=item_not_found";
        }
        // 자신이 등록한 아이템인지 확인
        if (!existingItem.getBusiness_id().equals(loginBusiness.getBusiness_id())) {
            return "redirect:/business/category";
        }
        // 이미지 처리
        if (itemImage != null && !itemImage.isEmpty()) {
            try {
                // 파일 저장 경로 설정
                String uploadPath = "C:/upload/image/businessitem/";
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
                String uploadPath = "C:/upload/image/businessitem";
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

    // 아이디 찾기 페이지
    @GetMapping("/find_id")
    public String findId(Model model) {
        return "business/find_id";
    }


    // 아이디 찾기 처리
    @PostMapping("/find_id_pro")
    public String findIdPro(@RequestParam("business_phone") String phone,
                            Model model) {
        try {
            // 전화번호로 사업자 아이디 조회
            String foundId = businessService.findBusinessIdByPhone(phone);
            System.out.println("BC" + foundId);

            if (foundId != null) {
                model.addAttribute("foundId", foundId);
                model.addAttribute("message", "아이디를 찾았습니다.");
                model.addAttribute("success", true);
            } else {
                model.addAttribute("message", "일치하는 정보가 없습니다. 입력하신 휴대폰 번호를 다시 확인해 주세요.");
                model.addAttribute("success", false);
            }
        } catch (Exception e) {
            logger.error("아이디 찾기 오류", e);
            model.addAttribute("message", "아이디 찾기 중 오류가 발생했습니다.");
            model.addAttribute("success", false);
        }

        return "business/find_id";
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/find_password")
    public String findPassword(Model model) {
        return "business/find_password";
    }

    // 비밀번호 찾기 처리 (본인 확인)
    @PostMapping("/find_password_pro")
    public String findPasswordPro(@RequestParam("business_id") String businessId,
                                  @RequestParam("business_phone") String phone,
                                  Model model) {
        try {
            // 사업자 정보 확인 (아이디와 휴대폰 번호로)
            boolean verified = businessService.verifyBusinessIdAndPhone(businessId, phone);

            if (verified) {
                model.addAttribute("verified", true);
                model.addAttribute("business_id", businessId);
                model.addAttribute("message", "본인 확인이 완료되었습니다. 새로운 비밀번호를 설정해 주세요.");
                model.addAttribute("success", true);
            } else {
                model.addAttribute("message", "입력하신 정보와 일치하는 계정이 없습니다. 다시 확인해 주세요.");
                model.addAttribute("success", false);
            }
        } catch (Exception e) {
            logger.error("비밀번호 찾기 오류", e);
            model.addAttribute("message", "비밀번호 찾기 중 오류가 발생했습니다.");
            model.addAttribute("success", false);
        }

        return "business/find_password";
    }



    // 전화번호 형식 변환 메서드 (000-0000-0000 형식으로 변환)
    private String formatPhoneNumber(String phone) {
        // 모든 하이픈(-), 공백 제거
        String numberOnly = phone.replaceAll("[\\-\\s]", "");

        // 숫자만 남기기
        numberOnly = numberOnly.replaceAll("[^0-9]", "");

        // 형식이 맞지 않으면 원래 입력 반환
        if (numberOnly.length() < 10 || numberOnly.length() > 11) {
            return phone;
        }

        // 000-0000-0000 형식으로 변환
        if (numberOnly.length() == 11) {
            // 11자리 (대부분의 휴대폰 번호)
            return numberOnly.substring(0, 3) + "-" +
                    numberOnly.substring(3, 7) + "-" +
                    numberOnly.substring(7);
        } else {
            // 10자리 (일부 지역번호나 예전 휴대폰 번호)
            return numberOnly.substring(0, 3) + "-" +
                    numberOnly.substring(3, 6) + "-" +
                    numberOnly.substring(6);
        }
    }

    // 비밀번호 재설정 처리
    @PostMapping("/reset_password")
    public String resetPassword(@RequestParam("business_id") String businessId,
                                @RequestParam("new_password") String newPassword,
                                @RequestParam("confirm_password") String confirmPassword,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        // 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("verified", true);
            model.addAttribute("business_id", businessId);
            model.addAttribute("message", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("success", false);
            return "business/find_password";
        }

        // 비밀번호 길이 검사 (8~16자)
        if (newPassword.length() < 8 || newPassword.length() > 16) {
            model.addAttribute("verified", true);
            model.addAttribute("business_id", businessId);
            model.addAttribute("message", "비밀번호는 8~16자리여야 합니다.");
            model.addAttribute("success", false);
            return "business/find_password";
        }

        try {
            // 비밀번호 변경
            businessService.updatePassword(businessId, newPassword);

            // 성공 메시지를 리다이렉트 속성에 추가
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해 주세요.");

            // 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        } catch (Exception e) {
            logger.error("비밀번호 재설정 오류", e);
            model.addAttribute("verified", true);
            model.addAttribute("business_id", businessId);
            model.addAttribute("message", "비밀번호 변경 중 오류가 발생했습니다.");
            model.addAttribute("success", false);
            return "business/find_password";
        }
    }




}