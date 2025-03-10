package com.spacedong.beans;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class BusinessBean {

	@NotBlank
	@Pattern(regexp = "^[^\\s]+$")
	@Size(min = 5, max = 40)
	private String busines_id;
	
	@NotBlank
	@Size(min = 8, max = 80)
	private String business_pw;
	
	@NotBlank
	@Size(min = 2, max = 40)
	private String business_name;
	
	@Email
	private String business_email;
	
	@NotBlank
	@Size(max = 30)
	private String business_phone;
	
	@NotBlank
	private String business_address;
	
	private Date business_joindate;
	
	private String business_public;

	public String getBusines_id() {
		return busines_id;
	}

	public void setBusines_id(String busines_id) {
		this.busines_id = busines_id;
	}

	public String getBusiness_pw() {
		return business_pw;
	}

	public void setBusiness_pw(String business_pw) {
		this.business_pw = business_pw;
	}

	public String getBusiness_name() {
		return business_name;
	}

	public void setBusiness_name(String business_name) {
		this.business_name = business_name;
	}

	public String getBusiness_email() {
		return business_email;
	}

	public void setBusiness_email(String business_email) {
		this.business_email = business_email;
	}

	public String getBusiness_phone() {
		return business_phone;
	}

	public void setBusiness_phone(String business_phone) {
		this.business_phone = business_phone;
	}

	public String getBusiness_address() {
		return business_address;
	}

	public void setBusiness_address(String business_address) {
		this.business_address = business_address;
	}

	public Date getBusiness_joindate() {
		return business_joindate;
	}

	public void setBusiness_joindate(Date business_joindate) {
		this.business_joindate = business_joindate;
	}

	public String getBusiness_public() {
		return business_public;
	}

	public void setBusiness_public(String business_public) {
		this.business_public = business_public;
	}
	
	
	
}
