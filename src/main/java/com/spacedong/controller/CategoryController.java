package com.spacedong.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spacedong.beans.CategoryBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.service.CategoryService;
import com.spacedong.service.ClubService;

@Controller
@RequestMapping("category")
public class CategoryController {

   @Autowired
   public CategoryService categoryService;

   @Autowired
   public ClubService clubService;

   @GetMapping("/category")
   public String category(Model model) {
      List<ClubBean> allList = null;
      List<CategoryBean> categoryList = categoryService.categoryType();
      allList = clubService.getAllClub();

      model.addAttribute("categoryList", categoryList);
      model.addAttribute("clublist", allList);

      return "category/category";
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
      List<ClubBean> clubList;


      if("all".equals(categoryType)) {
         clubList = clubService.getAllClub();
      }
      else if ("all".equals(subCategory)) {
         clubList = clubService.getClubsByCategory(categoryType);
      } else {
         clubList = clubService.getClubsBySubCategory(subCategory);
      }

      model.addAttribute("clublist", clubList);

      return "category/category";
   }



}