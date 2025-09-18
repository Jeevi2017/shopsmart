package com.shopsmart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime; // NEW: Import LocalDateTime

public class PaymentDTO {

    private Long id;
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private LocalDateTime paymentDate; 
    private String status; 

    public PaymentDTO() {
    }

    public PaymentDTO(Long id, Long orderId, String paymentMethod, BigDecimal amount, LocalDateTime paymentDate, String status) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentDate = paymentDate; 
        this.status = status;           
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
