package com.spacedong.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.service.AdminService;
import com.spacedong.service.ClubService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ClubService clubService;
    
    @Resource(name = "loginAdmin")
    private AdminBean loginAdmin;
    
    @GetMapping("/init")
    public String admininit() {
        return "admin/init";
    }
    
    @GetMapping("/login")
    public String login(@ModelAttribute("tempLoginAdmin") AdminBean tempLoginAdmin) {
        return "admin/login";
    }
    
    @PostMapping("/loginproc")
    public String loginPro(@Valid @ModelAttribute("tempLoginAdmin") AdminBean tempLoginAdmin, Model model,
                           BindingResult result) {
        System.out.println(result.hasErrors());
        if (result.hasErrors()) {
            return "admin/login";
        }
        if (adminService.getLoginAdmin(tempLoginAdmin)) {
            return "admin/main";
        } else {
            model.addAttribute("loginfail", true);
            return "admin/login";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
    
    // 동호회 관리 페이지
    @GetMapping("/club_management")
    public String clubManagement(Model model) {
        // 모든 동호회 정보 가져오기
        List<ClubBean> clubList = clubService.getAllClub();
        model.addAttribute("clubList", clubList);
        
        return "admin/club_management";
    }
    
    // 동호회 검색 (AJAX) - GET과 POST 모두 지원
    @RequestMapping(value = "/searchClub", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> searchClub(@RequestParam("searchType") String searchType, 
                                        @RequestParam("keyword") String keyword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClubBean> searchResults = clubService.searchClubs(searchType, keyword);
            response.put("success", true);
            response.put("clubList", searchResults);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // 동호회 상세 정보
    @GetMapping("/club_detail")
    @ResponseBody
    public ClubBean getClubDetail(@RequestParam("club_id") int club_id, Model model) {
    	System.out.println(club_id);
        return clubService.oneClubInfo(club_id);
    }
    
    // 동호회 승인
    @PostMapping("/approve_club")
    @ResponseBody
    public Map<String, Object> approveClub(@RequestParam("clubId") int club_id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            clubService.updateClubStatus(club_id, "PASS");
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // 동호회 상태 변경
    @PostMapping("/update_club_status")
    @ResponseBody
    public Map<String, Object> updateClubStatus(@RequestParam("clubId") int club_id,
                                              @RequestParam("status") String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 상태가 public이면 PASS로, private이면 WAIT로 변환
            String dbStatus = "PASS";
            if (status.equals("private")) {
                dbStatus = "WAIT";
            }
            
            clubService.updateClubStatus(club_id, dbStatus);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // 판매자 관리 페이지
    @GetMapping("/seller_management")
    public String sellerManagement() {
        return "admin/seller_management";
    }
    
    // 회원 관리 페이지
    @GetMapping("/member_management")
    public String userManagement() {
        return "admin/member_management";
    }
    
    @GetMapping("/logout")
    public String adminlogout() {
        
        loginAdmin.setAdmin_login(false);
        loginAdmin.setAdmin_id(null);
        loginAdmin.setAdmin_name(null);
        
        return "admin/logout";
    }
}