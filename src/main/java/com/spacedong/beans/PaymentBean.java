package com.spacedong.beans;

import java.util.Date;


public class PaymentBean {

	private int payment_id;

	private String payer_id;

	private String rental_id;

	private int price;
	
	private Date payment_date;

	public int getPayment_id() {
		return payment_id;
	}

	public void setPayment_id(int payment_id) {
		this.payment_id = payment_id;
	}

	public String getPayer_id() {
		return payer_id;
	}

	public void setPayer_id(String payer_id) {
		this.payer_id = payer_id;
	}

	public String getRental_id() {
		return rental_id;
	}

	public void setRental_id(String rental_id) {
		this.rental_id = rental_id;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public Date getPayment_date() {
		return payment_date;
	}

	public void setPayment_date(Date payment_date) {
		this.payment_date = payment_date;
	}
	
	
	
}
