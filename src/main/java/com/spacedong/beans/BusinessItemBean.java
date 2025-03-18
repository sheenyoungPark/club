package com.spacedong.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessItemBean {

	private String business_id;   // 판매자 ID (외래 키)
	private String item_id;       // 아이템 ID (기본 키)
	private String item_title;    // 게시글 제목
	private String item_text;     // 게시글 내용
	private int item_price;    // 가격
	private String item_img;      // 이미지 경로
	private int item_starttime;   // 운영 시작 시간 (예: 9, 13 등)
	private int item_endtime;     // 운영 종료 시간 (예: 18, 22 등)
	private String item_category; // 추가된 카테고리 필드
}