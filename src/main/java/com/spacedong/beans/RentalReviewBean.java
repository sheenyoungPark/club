package com.spacedong.beans;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;


public class RentalReviewBean {

	private int review_num;

	@NotNull
	private String review_text;

	private Date review_date;

	@Size(min = 1, max = 5)
	private int review_score;

	private String reviewer_id;
	
	private String review_items;
	
	private String business_id;
	
	private String business_item;
	
	private String reviewImg;

	public int getReview_num() {
		return review_num;
	}

	public void setReview_num(int review_num) {
		this.review_num = review_num;
	}

	public String getReview_text() {
		return review_text;
	}

	public void setReview_text(String review_text) {
		this.review_text = review_text;
	}

	public Date getReview_date() {
		return review_date;
	}

	public void setReview_date(Date review_date) {
		this.review_date = review_date;
	}

	public int getReview_score() {
		return review_score;
	}

	public void setReview_score(int review_score) {
		this.review_score = review_score;
	}

	public String getReviewer_id() {
		return reviewer_id;
	}

	public void setReviewer_id(String reviewer_id) {
		this.reviewer_id = reviewer_id;
	}

	public String getReview_items() {
		return review_items;
	}

	public void setReview_items(String review_items) {
		this.review_items = review_items;
	}

	public String getBusiness_id() {
		return business_id;
	}

	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}

	public String getBusiness_item() {
		return business_item;
	}

	public void setBusiness_item(String business_item) {
		this.business_item = business_item;
	}

	public String getReviewImg() {
		return reviewImg;
	}

	public void setReviewImg(String reviewImg) {
		this.reviewImg = reviewImg;
	}
	
	
	
}
