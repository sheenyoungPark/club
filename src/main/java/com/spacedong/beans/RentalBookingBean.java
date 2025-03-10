package com.spacedong.beans;

import java.util.Date;

public class RentalBookingBean {

	private int rental_num;
	
    private String business_id;
    
    private Date rental_date;
    
    private String rental_id;
    
    private Date start_date;
    
    private Date end_date;

	public int getRental_num() {
		return rental_num;
	}

	public void setRental_num(int rental_num) {
		this.rental_num = rental_num;
	}

	public String getBusiness_id() {
		return business_id;
	}

	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}

	public Date getRental_date() {
		return rental_date;
	}

	public void setRental_date(Date rental_date) {
		this.rental_date = rental_date;
	}

	public String getRental_id() {
		return rental_id;
	}

	public void setRental_id(String rental_id) {
		this.rental_id = rental_id;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}
    
	
    
}
