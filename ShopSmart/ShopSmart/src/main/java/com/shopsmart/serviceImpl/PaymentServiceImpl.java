package com.shopsmart.serviceImpl;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.shopsmart.dto.*;
import com.shopsmart.entity.Order;
import com.shopsmart.entity.Order.OrderStatus;
import com.shopsmart.entity.Payment;
import com.shopsmart.exception.BadRequestException;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.OrderRepository;
import com.shopsmart.repository.PaymentRepository;
import com.shopsmart.service.PaymentService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public PaymentDTO processPayment(Long orderId, PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "Id", orderId));

        if ("PAID".equals(order.getStatus().name()) || "CANCELLED".equals(order.getStatus().name())) {
            throw new IllegalArgumentException(
                    "Order " + orderId + " is already paid or cancelled. Cannot process payment.");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("COMPLETED");

        Payment savedPayment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return convertToDTO(savedPayment);
    }

    @Override
    @Transactional
    public RazorpayOrderResponseDTO createRazorpayOrder(RazorpayOrderRequestDTO requestDTO) throws Exception {
        if (!requestDTO.isValidRequest()) {
            throw new BadRequestException("Invalid Razorpay order request: Amount, currency, and receipt must be provided.");
        }

        Order order = orderRepository.findById(requestDTO.getInternalOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "Id", requestDTO.getInternalOrderId()));
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BadRequestException("Order is already paid.");
        }

        int amountInPaise = requestDTO.getAmountInPaise();
        if (amountInPaise <= 0) {
            throw new BadRequestException("Amount must be a positive value.");
        }

        try {
            System.out.println("DEBUG: Creating Razorpay order with payload: " + requestDTO.toString());
            
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", requestDTO.getCurrency());
            orderRequest.put("receipt", requestDTO.getReceipt());

            JSONObject notes = new JSONObject();
            notes.put("internal_order_id", requestDTO.getInternalOrderId());
            orderRequest.put("notes", notes);

            com.razorpay.Order razorpayOrder = razorpay.orders.create(orderRequest);

            RazorpayOrderResponseDTO responseDTO = new RazorpayOrderResponseDTO();
            responseDTO.setId(razorpayOrder.get("id").toString());
            responseDTO.setEntity(razorpayOrder.get("entity").toString());
            responseDTO.setAmount(razorpayOrder.get("amount"));
            responseDTO.setCurrency(razorpayOrder.get("currency").toString());
            responseDTO.setReceipt(razorpayOrder.get("receipt").toString());
            responseDTO.setStatus(razorpayOrder.get("status").toString());
            responseDTO.setAttempts(razorpayOrder.get("attempts"));

            // FIX: Safely retrieve and cast the 'created_at' field
            Object createdAtObj = razorpayOrder.get("created_at");
            if (createdAtObj instanceof Long) {
                responseDTO.setCreatedAt((Long) createdAtObj);
            } else if (createdAtObj != null) {
                try {
                    responseDTO.setCreatedAt(Long.parseLong(createdAtObj.toString()));
                } catch (NumberFormatException e) {
                    System.err.println("Unexpected created_at format: " + createdAtObj);
                    responseDTO.setCreatedAt(null);
                }
            } else {
                responseDTO.setCreatedAt(null);
            }

            return responseDTO;

        } catch (RazorpayException e) {
            throw new RuntimeException("Razorpay API error: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentDTO captureRazorpayPayment(RazorpayPaymentCaptureRequestDTO requestDTO) throws Exception {
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", requestDTO.getRazorpayOrderId());
        options.put("razorpay_payment_id", requestDTO.getRazorpayPaymentId());
        options.put("razorpay_signature", requestDTO.getRazorpaySignature());

        boolean signatureIsValid;
        try {
            signatureIsValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
        } catch (RazorpayException e) {
            throw new RuntimeException("Error during Razorpay signature verification.", e);
        }

        if (!signatureIsValid) {
            throw new IllegalArgumentException("Razorpay signature verification failed. Payment is not authentic.");
        }

        Order order = orderRepository.findById(requestDTO.getInternalOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "Id", requestDTO.getInternalOrderId()));

        if ("PAID".equals(order.getStatus().name())) {
            throw new IllegalArgumentException(
                    "Order " + order.getId() + " is already marked as PAID. Duplicate payment attempt.");
        }
        if ("CANCELLED".equals(order.getStatus().name())) {
            throw new IllegalArgumentException(
                    "Order " + order.getId() + " is CANCELLED. Cannot capture payment.");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod("RAZORPAY");
        payment.setAmount(requestDTO.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("COMPLETED");

        Payment savedPayment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return convertToDTO(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "Id", paymentId));
        return convertToDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "Id", orderId));
        return paymentRepository.findByOrder(order).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "Id", paymentId));
        paymentRepository.delete(payment);
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setAmount(payment.getAmount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setStatus(payment.getStatus());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        return dto;
    }
}