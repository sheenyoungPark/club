package com.spacedong.beans;

import java.util.List;

public class SpaceType {

	private String type;
	private String planet;
	private String description;
	private String categories;
	private List<TypeActivityBean> activities;
	private List<String> roles;
	private String goodcompanion;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPlanet() {
		return planet;
	}
	public void setPlanet(String planet) {
		this.planet = planet;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	public List<TypeActivityBean> getActivities() {
		return activities;
	}
	public void setActivities(List<TypeActivityBean> activities) {
		this.activities = activities;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public String getGoodcompanion() {
		return goodcompanion;
	}
	public void setGoodcompanion(String goodcompanion) {
		this.goodcompanion = goodcompanion;
	}
	
	

	
	
}
