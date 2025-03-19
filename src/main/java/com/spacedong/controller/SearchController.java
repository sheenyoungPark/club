package com.spacedong.controller;

import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false, defaultValue = "all") String searchType,
            @RequestParam(required = false) String region,
            @RequestParam(required = false, defaultValue = "0") int age,
            @RequestParam(required = false) String searchtxt,
            Model model) {

        // 검색 타입에 따라 다른 서비스 메소드 호출
        if("club".equals(searchType)) {
            // 동호회 검색
            List<ClubBean> searchedClubs = searchService.searchedClubs(region, age, searchtxt);
            model.addAttribute("searchedClubs", searchedClubs);
        } else if("business".equals(searchType)) {
            // 비즈니스아이템 검색
            List<BusinessItemBean> searchedBusinessItems = searchService.searchedBusinessItems(region, searchtxt);
            model.addAttribute("searchedBusinessItems", searchedBusinessItems);
        } else {
            // 통합 검색 (기본값: all)
            List<ClubBean> searchedClubs = searchService.searchedClubs(region, age, searchtxt);
            List<BusinessItemBean> searchedBusinessItems = searchService.searchedBusinessItems(region, searchtxt);

            model.addAttribute("searchedClubs", searchedClubs);
            model.addAttribute("searchedBusinessItems", searchedBusinessItems);
        }

        // 검색 타입 정보를 뷰에 전달
        model.addAttribute("searchType", searchType);
        model.addAttribute("region", region);
        model.addAttribute("age", age);
        model.addAttribute("searchtxt", searchtxt);

        return "search/searchResults";
    }
}