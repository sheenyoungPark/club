package com.spacedong.service;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.mapper.AdminMapper;
import com.spacedong.mapper.BusinessMapper;
import com.spacedong.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserSearchService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 모든 사용자 유형(Member, Business, Admin)에서 키워드로 검색
     */
    public List<Map<String, String>> searchAllUsersByKeyword(String keyword) {
        List<Map<String, String>> results = new ArrayList<>();

        // 1. Member 검색
        List<MemberBean> members = memberMapper.searchUsersByKeyword(keyword);
        for (MemberBean member : members) {
            Map<String, String> result = new HashMap<>();
            result.put("userId", member.getMember_id());
            result.put("userName", member.getMember_nickname() != null ?
                    member.getMember_nickname() : member.getMember_name());
            result.put("userType", "MEMBER");
            results.add(result);
        }

        // 2. Business 검색
        List<BusinessBean> businesses = businessMapper.searchBusinessByKeyword(keyword);
        for (BusinessBean business : businesses) {
            Map<String, String> result = new HashMap<>();
            result.put("userId", business.getBusiness_id());
            result.put("userName", business.getBusiness_name());
            result.put("userType", "BUSINESS");
            results.add(result);
        }

        // 3. Admin 검색
        List<AdminBean> admins = adminMapper.searchAdminsByKeyword(keyword);
        for (AdminBean admin : admins) {
            Map<String, String> result = new HashMap<>();
            result.put("userId", admin.getAdmin_id());
            result.put("userName", admin.getAdmin_name());
            result.put("userType", "ADMIN");
            results.add(result);
        }

        return results;
    }
}