package com.spacedong.beans;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BusinessItemBean {

	private String business_id;   // 판매자 ID (외래 키)
	private String item_id;       // 아이템 ID (기본 키)

	@NotBlank
	private String item_title;    // 게시글 제목
	@NotBlank
	private String item_text;     // 게시글 내용
	@NotNull
	private int item_price;    // 가격
	private String item_img;      // 이미지 경로
	private String item_category;//카테고리
	private int item_starttime; // 운영 시작 시간 (예: 9, 13 등)
	private int item_endtime;   // 운영 종료 시간 (예: 18, 22 등)

	// 🔴 아래 필드가 없으면 추가!
	private String business_name;  // 업체명
	private String business_address;  // 업체 주소

	public String getBusiness_id() {
		return business_id;
	}

	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public String getItem_title() {
		return item_title;
	}

	public void setItem_title(String item_title) {
		this.item_title = item_title;
	}

	public String getItem_text() {
		return item_text;
	}

	public void setItem_text(String item_text) {
		this.item_text = item_text;
	}

	public int getItem_price() {
		return item_price;
	}

	public void setItem_price(int item_price) {
		this.item_price = item_price;
	}

	public String getItem_img() {
		return item_img;
	}

	public void setItem_img(String item_img) {
		this.item_img = item_img;
	}

	public int getItem_starttime() {
		return item_starttime;
	}

	public void setItem_starttime(int item_starttime) {
		this.item_starttime = item_starttime;
	}

	public int getItem_endtime() {
		return item_endtime;
	}

	public void setItem_endtime(int item_endtime) {
		this.item_endtime = item_endtime;
	}

    public String getItem_category() {
        return item_category;
    }

    public void setItem_category(String item_category) {
        this.item_category = item_category;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public String getBusiness_address() {
        return business_address;
    }

    public void setBusiness_address(String business_address) {
        this.business_address = business_address;
    }
}
