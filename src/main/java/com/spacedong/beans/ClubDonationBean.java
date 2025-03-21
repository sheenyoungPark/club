package com.spacedong.beans;

import java.util.Date;

public class ClubDonationBean {

    private int donation_id;         // 후원 고유 ID (시퀀스)
    private int club_id;             // 동호회 ID
    private String member_id;        // 후원자 ID
    private int donation_point;      // 후원 포인트
    private Date donation_date;      // 후원 날짜

    private String member_nickname;

    public int getDonation_id() {
        return donation_id;
    }

    public void setDonation_id(int donation_id) {
        this.donation_id = donation_id;
    }

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

    public int getDonation_point() {
        return donation_point;
    }

    public void setDonation_point(int donation_point) {
        this.donation_point = donation_point;
    }

    public Date getDonation_date() {
        return donation_date;
    }

    public void setDonation_date(Date donation_date) {
        this.donation_date = donation_date;
    }

    public String getMember_nickname() {
        return member_nickname;
    }

    public void setMember_nickname(String member_nickname) {
        this.member_nickname = member_nickname;
    }
}
