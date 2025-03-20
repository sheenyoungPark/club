package com.spacedong.controller;

import com.spacedong.beans.CategoryBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.service.CategoryService;
import com.spacedong.service.ClubService;
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


    @RequestMapping("/")
    public String home(Model model) {

        List<CategoryBean> categoryCount = categoryService.categoryTypeCount();
        List<ClubBean> clubCount = clubService.countClub();

        for (ClubBean c : clubCount){
            System.out.println("id : " + c.getClub_id() + "name" + c.getClub_name());
        }
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("clubCount", clubCount);


        return "main";
    }
}
