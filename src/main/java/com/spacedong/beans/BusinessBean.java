package com.spacedong.beans;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;

public class BusinessBean {

	@NotBlank
	@Pattern(regexp = "^\\S+$")
	@Size(min = 5, max = 40)
	private String business_id;

	@NotBlank
	@Size(min = 8, max = 80)
	private String business_pw;

	@NotBlank
	@Size(min = 2, max = 40)
	private String business_name;

	@Email(message = "유효한 이메일 형식이 아닙니다.")
	@NotBlank(message = "이메일을 입력해주세요.")
	private String business_email;

	@NotBlank
	@Size(max = 30)
	private String business_phone;

	@NotBlank
	private String business_address;
	private String business_profile;
	private Date business_joindate;
	private String business_public;
	private String business_number;
	private boolean login;

	// 추가된 필드
	private String business_info;     // 사업자 소개/정보
	private int business_point;       // 사업자 포인트

	// Business_id getter and setter
	public String getBusiness_id() {
		return business_id;
	}

	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}

	// Business_pw getter and setter
	public String getBusiness_pw() {
		return business_pw;
	}

	public void setBusiness_pw(String business_pw) {
		this.business_pw = business_pw;
	}

	// Business_name getter and setter
	public String getBusiness_name() {
		return business_name;
	}

	public void setBusiness_name(String business_name) {
		this.business_name = business_name;
	}

	// Business_email getter and setter
	public String getBusiness_email() {
		return business_email;
	}

	public void setBusiness_email(String business_email) {
		this.business_email = business_email;
	}

	// Business_phone getter and setter
	public String getBusiness_phone() {
		return business_phone;
	}

	public void setBusiness_phone(String business_phone) {
		this.business_phone = business_phone;
	}

	// Business_address getter and setter
	public String getBusiness_address() {
		return business_address;
	}

	public void setBusiness_address(String business_address) {
		this.business_address = business_address;
	}

	// Business_profile getter and setter
	public String getBusiness_profile() {
		return business_profile;
	}

	public void setBusiness_profile(String business_profile) {
		this.business_profile = business_profile;
	}

	// Business_joindate getter and setter
	public Date getBusiness_joindate() {
		return business_joindate;
	}

	public void setBusiness_joindate(Date business_joindate) {
		this.business_joindate = business_joindate;
	}

	// Business_public getter and setter
	public String getBusiness_public() {
		return business_public;
	}

	public void setBusiness_public(String business_public) {
		this.business_public = business_public;
	}

	// Business_number getter and setter
	public String getBusiness_number() {
		return business_number;
	}

	public void setBusiness_number(String business_number) {
		this.business_number = business_number;
	}

	// Login getter and setter
	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

	// Business_info getter and setter
	public String getBusiness_info() {
		return business_info;
	}

	public void setBusiness_info(String business_info) {
		this.business_info = business_info;
	}

	// Business_point getter and setter
	public int getBusiness_point() {
		return business_point;
	}

	public void setBusiness_point(int business_point) {
		this.business_point = business_point;
	}
}