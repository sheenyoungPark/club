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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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
            loginBusiness.setLogin(true);
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

        BusinessItemBean item = businessService.getItemById(itemId);

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