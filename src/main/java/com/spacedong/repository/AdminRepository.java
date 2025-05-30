package com.spacedong.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.mapper.AdminMapper;

@Repository
public class AdminRepository {

	@Autowired
	private AdminMapper adminMapper;
	
	public AdminBean getLoginAdmin(AdminBean admin) {
		return adminMapper.getLoginAdmin(admin);
	}
	
	public List<ClubBean> getWaitClub() {
		return adminMapper.getWaitClub();
	}

	public AdminBean getAdminById(String adminId) {
		return adminMapper.getAdminById(adminId);
	}

	// 전체 동호회 수 조회
	public int getClubCount() {
		return adminMapper.getClubCount();
	}

	// 이전 달 동호회 수 조회
	public int getPreviousMonthClubCount() {
		return adminMapper.getPreviousMonthClubCount();
	}
	
}
