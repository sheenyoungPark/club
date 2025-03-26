package com.spacedong.service;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.MemberBean;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


@Service
public class SessionService {

    @Resource(name = "loginMember")
    private MemberBean loginMember;

    @Resource(name = "loginBusiness")
    private BusinessBean loginBusiness;

    @Resource(name = "loginAdmin")
    private AdminBean loginAdmin;

    /**
     * 모든 로그인 세션을 초기화
     */
    public void resetAllLoginSessions() {
        // 객체의 속성을 빈 문자열로 초기화
        if (loginMember != null) {
            loginMember.setLogin(false);
            loginMember.setMember_id(null);
            loginMember.setMember_nickname(null);
            loginMember.setMember_pw(null);
            // 기타 필요한 필드 초기화
        }

        if (loginBusiness != null) {
            loginBusiness.setLogin(false);
            loginBusiness.setBusiness_id(null);
            loginBusiness.setBusiness_name(null);
            loginBusiness.setBusiness_pw(null);
        }

        if (loginAdmin != null) {
            loginAdmin.setAdmin_login(false);
            loginAdmin.setAdmin_id(null);
            loginAdmin.setAdmin_name(null);
            // 기타 필요한 필드 초기화
        }
    }



    /**
     * 현재 로그인된 사용자의 타입을 반환
     * @return "MEMBER", "BUSINESS", "ADMIN" 또는 null(로그인 안됨)
     */
    public String getCurrentLoginType() {
        if (loginMember != null && loginMember.isLogin()) {
            return "MEMBER";
        } else if (loginBusiness != null && loginBusiness.isLogin()) {
            return "BUSINESS";
        } else if (loginAdmin != null && loginAdmin.isAdmin_login()) {
            return "ADMIN";
        }
        return null; // 로그인되지 않음
    }
}