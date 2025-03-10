package com.spacedong.service;

import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.repository.AdminRepository;

@Service
public class AdminService {
	
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
}
