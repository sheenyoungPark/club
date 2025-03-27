package com.spacedong.controller;

import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.CategoryBean;
import com.spacedong.beans.ClubBean;
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


    @RequestMapping("/")
    public String home(Model model) {

        List<CategoryBean> categoryCount = categoryService.categoryTypeCount();
        List<ClubBean> clubCount = clubService.countClub();
        List<BusinessItemBean> itemList = itemService.randomItemList();

        for (BusinessItemBean c : itemList){
            System.out.println("id : " + c.getItem_title() + "name" + c.getItem_text());
        }
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("clubCount", clubCount);
        model.addAttribute("itemList", itemList);


        return "main";
    }
}
