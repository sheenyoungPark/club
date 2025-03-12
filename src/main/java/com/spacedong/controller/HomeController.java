package com.spacedong.controller;

import com.spacedong.beans.Category;
import com.spacedong.beans.ClubBean;
import com.spacedong.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ClubService clubService;


    @RequestMapping("/")
    public String home(Model model) {

        List<Category> categoriesCount = clubService.countCategory();
        List<ClubBean> clubCount = clubService.countClub();

        for (ClubBean c : clubCount){
            System.out.println("id : " + c.getClub_id() + "name" + c.getClub_name());
        }
        model.addAttribute("categortCount", categoriesCount);
        model.addAttribute("clubCount", clubCount);


        return "main";
    }
}
