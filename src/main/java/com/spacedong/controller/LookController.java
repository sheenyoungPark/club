package com.spacedong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spacedong.service.CategoryService;
import com.spacedong.service.ClubService;

@Controller
@RequestMapping("/look")
public class LookController {
   
   @Autowired
   public CategoryService categoryService;
   
   @Autowired
   public ClubService clubService;
   
   
   @GetMapping("/search")
   public String search(@RequestParam(value ="region") String region,
                   @RequestParam(value = "age") int age, @RequestParam(value ="searchtxt") String searchtxt, Model model) {
      model.addAttribute("region",region);
      model.addAttribute("age",age);
      model.addAttribute("searchtxt",searchtxt);
      
      
      return "look/search";
    }

   
   
   
}
