package com.shopsmart.dto;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DiscountDTO {

	 private Long id;
	 private String code;
	 private String type;
	 private BigDecimal value;
	 private BigDecimal minOrderAmount;
	 @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	 private LocalDateTime startDate; 
	    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	 private LocalDateTime endDate;   
	 private Integer usageLimit;
	 private Integer usedCount;
	 private boolean active;

	public DiscountDTO() {
		super();
	}


	public DiscountDTO(Long id, String code, String type, BigDecimal value, BigDecimal minOrderAmount,
			LocalDateTime startDate, LocalDateTime endDate, Integer usageLimit, Integer usedCount, boolean active) {
		super();
		this.id = id;
		this.code = code;
		this.type = type;
		this.value = value;
		this.minOrderAmount = minOrderAmount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.usageLimit = usageLimit;
		this.usedCount = usedCount;
		this.active = active;
	}


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public BigDecimal getMinOrderAmount() {
		return minOrderAmount;
	}
	public void setMinOrderAmount(BigDecimal minOrderAmount) {
		this.minOrderAmount = minOrderAmount;
	}
	public LocalDateTime getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}
	public LocalDateTime getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	public Integer getUsageLimit() {
		return usageLimit;
	}
	public void setUsageLimit(Integer usageLimit) {
		this.usageLimit = usageLimit;
	}
	public Integer getUsedCount() {
		return usedCount;
	}
	public void setUsedCount(Integer usedCount) {
		this.usedCount = usedCount;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
