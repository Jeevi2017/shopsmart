package com.shopsmart.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects; // NEW: Import Objects for equals/hashCode

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="discounts")
public class Discount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column( nullable = false, unique = true)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiscountType type;

	@Column(nullable = false , precision = 10 , scale = 2)
	private BigDecimal value;

	@Column(name = "min_order_amount" , precision = 10 , scale = 2)
	private BigDecimal minOrderAmount;

	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date",nullable = false)
	private LocalDateTime endDate;

	@Column(name="usage_limit")
	private Integer usageLimit;

	@Column(name = "used_count")
	private Integer usedCount = 0;

	@Column(nullable = false)
	private boolean active = true;

	public enum DiscountType {
		PERCENTAGE,
		FIXED_AMOUNT
	}

	public Discount() {
		super();
	}

	public Discount(Long id, String code, DiscountType type, BigDecimal value, BigDecimal minOrderAmount,
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

	public DiscountType getType() {
		return type;
	}

	public void setType(DiscountType type) {
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

	// NEW: Override equals() and hashCode() for proper JPA entity behavior
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Discount discount = (Discount) o;
		// Use ID for equality if it's already persisted. 'code' is also unique.
		return Objects.equals(id, discount.id) &&
			   Objects.equals(code, discount.code);
	}

	@Override
	public int hashCode() {
		// Use ID and code for hashCode.
		return Objects.hash(id, code);
	}

	// NEW: Override toString() for better logging and debugging
	@Override
	public String toString() {
		return "Discount{" +
			   "id=" + id +
			   ", code='" + code + '\'' +
			   ", type=" + type +
			   ", value=" + value +
			   ", minOrderAmount=" + minOrderAmount +
			   ", startDate=" + startDate +
			   ", endDate=" + endDate +
			   ", usageLimit=" + usageLimit +
			   ", usedCount=" + usedCount +
			   ", active=" + active +
			   '}';
	}
}
