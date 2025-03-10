package com.spacedong.beans;

public class LocationBean {
	
	private String city;
	private String district;
	
	public LocationBean() {}
	
	public LocationBean(String city, String district) {
		this.city = city;
		this.district = district;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	
	

}
