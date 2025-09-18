package com.shopsmart.service;

import java.util.List;

import com.shopsmart.dto.PaymentDTO;
import com.shopsmart.dto.RazorpayOrderRequestDTO;
import com.shopsmart.dto.RazorpayOrderResponseDTO;
import com.shopsmart.dto.RazorpayPaymentCaptureRequestDTO;
public interface PaymentService {

    
    PaymentDTO processPayment(Long orderId, PaymentDTO paymentDTO);

    
    PaymentDTO getPaymentById(Long paymentId);

    
    List<PaymentDTO> getAllPayments();

   
    List<PaymentDTO> getPaymentsByOrderId(Long orderId);

    
    void deletePayment(Long paymentId);
    
    RazorpayOrderResponseDTO createRazorpayOrder(RazorpayOrderRequestDTO requestDTO) throws Exception;

   
    PaymentDTO captureRazorpayPayment(RazorpayPaymentCaptureRequestDTO requestDTO) throws Exception;
}

