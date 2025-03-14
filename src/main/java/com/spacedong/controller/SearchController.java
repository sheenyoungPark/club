package com.spacedong.controller;

import com.spacedong.beans.ClubBean;
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
            @RequestParam(required = false) String region,
            @RequestParam(required = false, defaultValue = "0") int age,
            @RequestParam(required = false) String searchtxt,
            Model model) {

        List<ClubBean> searchedClubs = searchService.searchedClubs(region, age, searchtxt);

        model.addAttribute("searchedClubs", searchedClubs);

        return "search/searchResults";
    }

}
