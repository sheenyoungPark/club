package com.spacedong.controller;

import com.spacedong.beans.*;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.service.BoardService;
import com.spacedong.service.CategoryService;
import com.spacedong.service.ClubService;
import com.spacedong.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ClubService clubService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BoardService boardService;

    @RequestMapping("/")
    public String home(Model model) {
        // 카테고리 정보 가져오기
        List<CategoryBean> categoryCount = categoryService.categoryTypeCount();

        // 인기 동호회 정보 가져오기
        List<ClubBean> clubCount = clubService.countClub();

        // 랜덤 아이템 리스트 가져오기 (5개 이상 가져오도록 확인)
        List<BusinessItemBean> itemList = itemService.randomItemList();

        //모든 보드 리스트
        List<BoardBean> boardList = boardService.homeAllList();

        // 모델에 데이터 추가
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("clubCount", clubCount);
        model.addAttribute("itemList", itemList);
        model.addAttribute("boardList", boardList);

        return "main";
    }
}
