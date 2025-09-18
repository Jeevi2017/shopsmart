package com.shopsmart.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RazorpayPaymentCaptureRequestDTO {

    @NotNull(message = "Payment ID is required")
    private String razorpayPaymentId; 

    @NotNull(message = "Order ID is required")
    private String razorpayOrderId; 

    @NotNull(message = "Signature is required")
    private String razorpaySignature; 

    @NotNull(message = "Amount is required for capture")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount; 

    @NotNull(message = "Internal order ID is required")
    private Long internalOrderId; 

    public RazorpayPaymentCaptureRequestDTO() {}

    public RazorpayPaymentCaptureRequestDTO(String razorpayPaymentId, String razorpayOrderId, String razorpaySignature, BigDecimal amount, Long internalOrderId) {
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpaySignature = razorpaySignature;
        this.amount = amount;
        this.internalOrderId = internalOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getInternalOrderId() {
        return internalOrderId;
    }

    public void voidsetInternalOrderId(Long internalOrderId) {
        this.internalOrderId = internalOrderId;
    }
}
