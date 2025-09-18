package com.shopsmart.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for creating a Razorpay Order request.
 * Includes business logic such as amount conversion and request validation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayOrderRequestDTO {

    // ✅ Internal ShopSmart order ID (maps to Orders table)
    private Long orderId;

    // ✅ Payment amount in INR
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    // ✅ Currency code (Razorpay supports INR)
    @NotNull(message = "Currency is required")
    private String currency;

    // ✅ Receipt ID (unique per order)
    @NotNull(message = "Receipt is required")
    private String receipt;

    // ---------------- Constructors ----------------

    public RazorpayOrderRequestDTO() {}

    public RazorpayOrderRequestDTO(Long orderId, BigDecimal amount, String currency, String receipt) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.receipt = receipt;
    }

    // ---------------- Getters / Setters ----------------

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    // Alias for clarity in service layer
    public Long getInternalOrderId() {
        return this.orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    // ---------------- Business Logic ----------------

    /**
     * Converts INR amount to paise as required by Razorpay.
     * Example: 500.00 INR → 50000 paise
     */
    public int getAmountInPaise() {
        if (this.amount == null) return 0;
        return this.amount.multiply(BigDecimal.valueOf(100)).intValue();
    }

    /**
     * Validates the request before sending to Razorpay.
     * Ensures amount > 0, currency & receipt are present.
     */
    public boolean isValidRequest() {
        return this.amount != null &&
               this.amount.compareTo(BigDecimal.ZERO) > 0 &&
               this.currency != null &&
               !this.currency.isEmpty() &&
               this.receipt != null &&
               !this.receipt.isEmpty();
    }

    /**
     * Optional: Generate a default receipt string if not provided.
     * Format: "shopSmart_order_{orderId}"
     */
    public void ensureReceipt() {
        if (this.receipt == null || this.receipt.isEmpty()) {
            this.receipt = "shopSmart_order_" + this.orderId;
        }
    }

    @Override
    public String toString() {
        return "RazorpayOrderRequestDTO{" +
                "orderId=" + orderId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", receipt='" + receipt + '\'' +
                ", amountInPaise=" + getAmountInPaise() +
                '}';
    }
}
