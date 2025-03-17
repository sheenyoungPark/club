package com.spacedong.beans;

import java.util.Date;

public class ReservationReviewBean {

    private int review_id;      // 리뷰 ID (기본 키, 예약과 연결됨)
    private int rating;      // 평점 (0~5점)
    private String review_title; // 리뷰 제목
    private String review_text;  // 리뷰 내용
    private String review_img;   // 리뷰 이미지 경로 (URL 또는 파일 경로)
    private Date created_at;

    public int getReview_id() {
        return review_id;
    }

    public void setReview_id(int review_id) {
        this.review_id = review_id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReview_title() {
        return review_title;
    }

    public void setReview_title(String review_title) {
        this.review_title = review_title;
    }

    public String getReview_text() {
        return review_text;
    }

    public void setReview_text(String review_text) {
        this.review_text = review_text;
    }

    public String getReview_img() {
        return review_img;
    }

    public void setReview_img(String review_img) {
        this.review_img = review_img;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
