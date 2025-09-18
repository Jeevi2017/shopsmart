package com.shopsmart.dto;

public class ApplyCouponRequest {
	 private String couponCode;

	public ApplyCouponRequest() {
		super();
	}

	public ApplyCouponRequest(String couponCode) {
		this.couponCode = couponCode;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
	
	 

}
