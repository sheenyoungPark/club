package com.spacedong.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spacedong.mapper.BusinessMapper;
import com.spacedong.mapper.MemberMapper;
import com.spacedong.repository.ClubRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.repository.AdminRepository;

@Service
public class AdminService {
	@Autowired
	private MemberMapper memberMapper;
	@Autowired
	private BusinessMapper businessMapper;
	
	@Resource(name="loginAdmin")
	private AdminBean loginAdmin;
	
	@Autowired
	private AdminRepository adminRepository;

	public boolean getLoginAdmin(AdminBean admin) {
		
		AdminBean tempAdmin = adminRepository.getLoginAdmin(admin);
		
		if(tempAdmin != null) {
			loginAdmin.setAdmin_id(tempAdmin.getAdmin_id());
			loginAdmin.setAdmin_name(tempAdmin.getAdmin_name());
			loginAdmin.setAdmin_login(true);
			
			return true;
		}else {
			return false;
		}
	}
	public int getTotalUsersCount() {
		try {
			int memberCount = memberMapper.getMemberCount();
			int businessCount = businessMapper.getBusinessCount();
			System.out.println("개인회원 수: " + memberCount);
			System.out.println("사업자회원 수: " + businessCount);
			return memberCount + businessCount;
		} catch (Exception e) {
			System.err.println("회원 수 집계 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}
	// 회원 성장률 계산
	public double getMemberGrowthRate() {
		// 현재 달의 회원 수
		int currentMonthCount = getTotalUsersCount();

		// 이전 달의 회원 수
		int previousMonthCount = memberMapper.getPreviousMonthMemberCount() + businessMapper.getPreviousMonthBusinessCount();

		// 성장률 계산 (퍼센트)
		if (previousMonthCount == 0) {
			if (currentMonthCount > 0) {
				return 100.0; // 이전 달 회원이 0명이고 현재 회원이 있으면 100% 증가로 표시
			} else {
				return 0.0; // 이전 달 회원도 0명, 현재 회원도 0명이면 0% 증가
			}
		}

		return ((double)(currentMonthCount - previousMonthCount) / previousMonthCount) * 100.0;
	}

	// 전체 동호회 수 조회
	public int getTotalClubsCount() {
		return adminRepository.getClubCount();
	}

	// 이전 달 동호회 수 조회
	public int getPreviousMonthClubCount() {
		return adminRepository.getPreviousMonthClubCount();
	}

	// 동호회 성장률 계산
	public double getClubGrowthRate() {
		// 현재 달의 동호회 수
		int currentMonthCount = getTotalClubsCount();

		// 이전 달의 동호회 수
		int previousMonthCount = getPreviousMonthClubCount();

		// 성장률 계산 (퍼센트)
		if (previousMonthCount == 0) {
			if (currentMonthCount > 0) {
				return 100.0; // 이전 달 동호회가 0개이고 현재 동호회가 있으면 100% 증가로 표시
			} else {
				return 0.0; // 이전 달 동호회도 0개, 현재 동호회도 0개이면 0% 증가
			}
		}

		return ((double)(currentMonthCount - previousMonthCount) / previousMonthCount) * 100.0;
	}
	// 월별 회원 수 조회 (1월~12월)
	public List<Map<String, Object>> getMonthlyMemberCounts() {
		List<Map<String, Object>> monthlyCounts = new ArrayList<>();

		// 1월부터 12월까지의 회원 수 조회
		for (int month = 1; month <= 12; month++) {
			Map<String, Object> monthData = new HashMap<>();

			try {
				// 각 월의 회원 수 조회 (개인 회원 + 사업자 회원)
				int memberCount = memberMapper.getMemberCountByMonth(month);
				int businessCount = businessMapper.getBusinessCountByMonth(month);
				int totalCount = memberCount + businessCount;

				monthData.put("month", month);
				monthData.put("label", month + "월");
				monthData.put("count", totalCount);
			} catch (Exception e) {
				System.err.println(month + "월 회원 수 조회 중 오류: " + e.getMessage());
				monthData.put("month", month);
				monthData.put("label", month + "월");
				monthData.put("count", 0);
			}

			monthlyCounts.add(monthData);
		}

		return monthlyCounts;
	}


}
