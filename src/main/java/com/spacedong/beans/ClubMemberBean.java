package com.spacedong.beans;

import java.util.Date;

public class ClubMemberBean {
	
	private int club_id;
	
    private String member_id;
    
    private Date member_joinDate;
    
    private String member_role;

	private String member_nickname;
	private String member_profile;


	public int getClub_id() {
		return club_id;
	}

	public void setClub_id(int club_id) {
		this.club_id = club_id;
	}

	public String getMember_id() {
		return member_id;
	}

	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}

	public Date getMember_joinDate() {
		return member_joinDate;
	}

	public void setMember_joinDate(Date member_joinDate) {
		this.member_joinDate = member_joinDate;
	}

	public String getMember_role() {
		return member_role;
	}

	public void setMember_role(String member_role) {
		this.member_role = member_role;
	}


    public String getMember_nickname() {
        return member_nickname;
    }

    public void setMember_nickname(String member_nickname) {
        this.member_nickname = member_nickname;
    }

    public String getMember_profile() {
        return member_profile;
    }

    public void setMember_profile(String member_profile) {
        this.member_profile = member_profile;
    }
}
