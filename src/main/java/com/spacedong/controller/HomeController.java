package com.spacedong.controller;

import com.spacedong.beans.Category;
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

        List<Category> count = clubService.countCategory();

        for (Category c : count){
            System.out.println(c.getCategory_name());
        }

        model.addAttribute("count", count);


        return "main";
    }
}
