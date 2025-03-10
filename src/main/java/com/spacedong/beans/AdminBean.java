package com.spacedong.beans;

import jakarta.validation.constraints.NotBlank;

public class AdminBean {
	
	@NotBlank
	private String admin_id;
	
	@NotBlank
	private String admin_pw;
	private String admin_name;
	private boolean admin_login;
	
	public AdminBean() {
		admin_login = false;
	}
	
	public String getAdmin_id() {
		return admin_id;
	}
	public void setAdmin_id(String admin_id) {
		this.admin_id = admin_id;
	}
	public String getAdmin_pw() {
		return admin_pw;
	}
	public void setAdmin_pw(String admin_pw) {
		this.admin_pw = admin_pw;
	}
	public String getAdmin_name() {
		return admin_name;
	}
	public void setAdmin_name(String admin_name) {
		this.admin_name = admin_name;
	}
	public boolean isAdmin_login() {
		return admin_login;
	}
	public void setAdmin_login(boolean admin_login) {
		this.admin_login = admin_login;
	}
	
	

}
