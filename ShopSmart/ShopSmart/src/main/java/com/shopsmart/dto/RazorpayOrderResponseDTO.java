package com.shopsmart.dto;

/**
 * DTO representing the response returned when creating an order in Razorpay.
 * Mirrors the structure of Razorpay's API response and includes helper business logic methods.
 */
public class RazorpayOrderResponseDTO {

    private String id;          // Razorpay order ID (e.g. "order_HK123...")
    private String entity;      // Entity type, usually "order"
    private Integer amount;     // Amount in the smallest unit (paise for INR)
    private String currency;    // Currency code (e.g. "INR")
    private String receipt;     // Receipt identifier you passed in request
    private String status;      // Current status (e.g. "created", "paid")
    private Integer attempts;   // Number of payment attempts on this order
    private Long createdAt;     // Epoch timestamp (seconds) when created

    // Default constructor
    public RazorpayOrderResponseDTO() {}

    // All-args constructor
    public RazorpayOrderResponseDTO(
            String id,
            String entity,
            Integer amount,
            String currency,
            String receipt,
            String status,
            Integer attempts,
            Long createdAt
    ) {
        this.id = id;
        this.entity = entity;
        this.amount = amount;
        this.currency = currency;
        this.receipt = receipt;
        this.status = status;
        this.attempts = attempts;
        this.createdAt = createdAt;
    }

    // ---------- Getters & Setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getReceipt() { return receipt; }
    public void setReceipt(String receipt) { this.receipt = receipt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    // ---------- Business Logic Methods ----------

    /**
     * Convert the amount from paise (Integer) to INR (BigDecimal)
     */
    public double getAmountInINR() {
        if (this.amount == null) return 0.0;
        return this.amount / 100.0;
    }

    /**
     * Check if the order is paid
     */
    public boolean isPaid() {
        return "paid".equalsIgnoreCase(this.status);
    }

    /**
     * Quick validity check to see if essential fields are present
     */
    public boolean isValidResponse() {
        return this.id != null && !this.id.isEmpty()
                && this.amount != null && this.amount > 0
                && this.currency != null && !this.currency.isEmpty()
                && this.status != null && !this.status.isEmpty();
    }

    /**
     * Returns a friendly summary of the order (for logs or frontend)
     */
    public String getSummary() {
        return String.format("RazorpayOrder[id=%s, amount=%s %s, status=%s, receipt=%s]",
                this.id, getAmountInINR(), this.currency, this.status, this.receipt);
    }
}
