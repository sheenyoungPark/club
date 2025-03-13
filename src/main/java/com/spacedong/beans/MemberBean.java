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
public class MemberBean {

	@NotBlank
	@Pattern(regexp = "^[^\\s]+$")
	@Size(min = 5, max = 40)
	private String member_id;
	
	private String sns_id;

	@NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
	private String member_pw;

	private String member_gender;

	private Date member_birthdate;

	@NotBlank(message = "비밀번호를 확인하세요")
	private String member_pw2;

	@NotBlank
	@Size(min = 2, max = 10)
	private String member_name;

	@Email
	private String member_email;

	@NotBlank
	@Size(max = 15)
	private String member_phone;

	@NotBlank
	private String member_address;

	private Date member_joindate;

	private String member_profile;

	@Size(min = 2, max = 40)
	private String member_nickname;

	private int member_personality;
	
	private int member_point = 0;
	
	private boolean login;

	private boolean idExist;

	private boolean nickExist;

	public boolean isNickExist() {
		return nickExist;
	}

	public void setNickExist(boolean nickExist) {
		this.nickExist = nickExist;
	}

	public MemberBean() {
		idExist = false;
		nickExist = false;
		login = false;
	}

	public boolean isIdExist() {
		return idExist;
	}

	public void setIdExist(boolean idExist) {
		this.idExist = idExist;
	}

	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

	public String getSns_id() {
		return sns_id;
	}

	public void setSns_id(String sns_id) {
		this.sns_id = sns_id;
	}

	public String getMember_pw2() {
		return member_pw2;
	}

	public void setMember_pw2(String member_pw2) {
		this.member_pw2 = member_pw2;
	}

	public String getMember_id() {
		return member_id;
	}

	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}

	public String getMember_pw() {
		return member_pw;
	}

	public void setMember_pw(String member_pw) {
		this.member_pw = member_pw;
	}

	public String getMember_name() {
		return member_name;
	}

	public void setMember_name(String member_name) {
		this.member_name = member_name;
	}

	public String getMember_email() {
		return member_email;
	}

	public void setMember_email(String member_email) {
		this.member_email = member_email;
	}

	public String getMember_phone() {
		return member_phone;
	}

	public void setMember_phone(String member_phone) {
		this.member_phone = member_phone;
	}

	public String getMember_address() {
		return member_address;
	}

	public void setMember_address(String member_address) {
		this.member_address = member_address;
	}

	public Date getMember_joindate() {
		return member_joindate;
	}

	public void setMember_joindate(Date member_joindate) {
		this.member_joindate = member_joindate;
	}

	public String getMember_profile() {
		return member_profile;
	}

	public void setMember_profile(String member_profile) {
		this.member_profile = member_profile;
	}

	public String getMember_nickname() {
		return member_nickname;
	}

	public void setMember_nickname(String member_nickname) {
		this.member_nickname = member_nickname;
	}

	public int getMember_personality() {
		return member_personality;
	}

	public void setMember_personality(int member_personality) {
		this.member_personality = member_personality;
	}

	public int getMember_point() {
		return member_point;
	}

	public void setMember_point(int member_point) {
		this.member_point = member_point;
	}


    public String getMember_gender() {
        return member_gender;
    }

    public void setMember_gender(String member_gender) {
        this.member_gender = member_gender;
    }

    public Date getMember_birthdate() {
        return member_birthdate;
    }

    public void setMember_birthdate(Date member_birthdate) {
        this.member_birthdate = member_birthdate;
    }
}
