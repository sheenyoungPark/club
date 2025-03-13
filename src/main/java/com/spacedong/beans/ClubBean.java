package com.spacedong.beans;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ClubBean {

	private int club_id;

	@NotBlank
    @Size(min = 2, max = 40)
    private String club_name;

    private String club_info;

    private Date club_joindate = new Date();

    private int club_point;

    private String club_category;

	private int club_agemin;

	private String club_profile;
    
    @Size(max = 20)
    private String club_public;  // 공개 여부 (Y/N)

	public int getClub_id() {
		return club_id;
	}

	public void setClub_id(int club_id) {
		this.club_id = club_id;
	}

	public String getClub_name() {
		return club_name;
	}

	public void setClub_name(String club_name) {
		this.club_name = club_name;
	}

	public String getClub_info() {
		return club_info;
	}

	public void setClub_info(String club_info) {
		this.club_info = club_info;
	}

	public Date getClub_joindate() {
		return club_joindate;
	}

	public void setClub_joindate(Date club_joindate) {
		this.club_joindate = club_joindate;
	}

	public int getClub_point() {
		return club_point;
	}

	public void setClub_point(int club_point) {
		this.club_point = club_point;
	}

	public int getClub_agemin() {
		return club_agemin;
	}

	public void setClub_agemin(int club_agemin) {
		this.club_agemin = club_agemin;
	}

	public String getClub_category() {
		return club_category;
	}

	public void setClub_category(String club_category) {
		this.club_category = club_category;
	}

	public String getClub_public() {
		return club_public;
	}

	public void setClub_public(String club_public) {
		this.club_public = club_public;
	}

	public String getClub_profile() {
		return club_profile;
	}

	public void setClub_profile(String club_profile) {
		this.club_profile = club_profile;
	}
}
