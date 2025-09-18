package com.shopsmart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.shopsmart.config.SecurityConstants;
import com.shopsmart.dto.OrderDTO;
import com.shopsmart.dto.PaymentDTO;
import com.shopsmart.dto.RazorpayOrderRequestDTO;
import com.shopsmart.dto.RazorpayOrderResponseDTO;
import com.shopsmart.dto.RazorpayPaymentCaptureRequestDTO;
import com.shopsmart.dto.UserDTO;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.service.CustomerService;
import com.shopsmart.service.OrderService;
import com.shopsmart.service.PaymentService;
import com.shopsmart.service.UserService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // 🔹 Get currently authenticated user
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO userDTO = userService.getUserByUserName(username);
        return userDTO.getId();
    }

    // 🔹 Validate that current user owns the order
    private void validateOrderOwnership(Long orderId) {
        Long loggedInUserId = getAuthenticatedUserId();
        OrderDTO orderDTO = orderService.getOrderById(orderId);

        if (orderDTO == null) {
            throw new ResourceNotFoundException("Order", "Id", orderId);
        }

        if (!orderDTO.getCustomerId().equals(loggedInUserId) &&
                !SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(SecurityConstants.ROLE_ADMIN))) {
            throw new SecurityException("Access denied: You do not own this order.");
        }
    }

    // 🔹 Validate that current user owns the payment
    private void validatePaymentOwnership(Long paymentId) {
        PaymentDTO paymentDTO = paymentService.getPaymentById(paymentId);
        if (paymentDTO == null) {
            throw new ResourceNotFoundException("Payment", "Id", paymentId);
        }
        validateOrderOwnership(paymentDTO.getOrderId());
    }

    // ================== PAYMENT APIs ==================

    // Process a manual payment (e.g., COD or Wallet)
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasAnyAuthority('" + SecurityConstants.ROLE_ADMIN + "', '" + SecurityConstants.ROLE_CUSTOMER + "')")
    public ResponseEntity<PaymentDTO> processPayment(@PathVariable Long orderId,
                                                     @Validated @RequestBody PaymentDTO paymentDTO) {
        validateOrderOwnership(orderId);
        PaymentDTO savedPayment = paymentService.processPayment(orderId, paymentDTO);
        return new ResponseEntity<>(savedPayment, HttpStatus.CREATED);
    }

    // Create Razorpay order (for initiating online payment)
    @PostMapping("/razorpay/order")
    @PreAuthorize("hasAnyAuthority('" + SecurityConstants.ROLE_ADMIN + "', '" + SecurityConstants.ROLE_CUSTOMER + "')")
    public ResponseEntity<RazorpayOrderResponseDTO> createRazorpayOrder(@Validated @RequestBody RazorpayOrderRequestDTO requestDTO) {
        try {
            // Ownership check: receipt/order reference should match logged-in user
            validateOrderOwnership(requestDTO.getInternalOrderId());

            RazorpayOrderResponseDTO response = paymentService.createRazorpayOrder(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating Razorpay order: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Capture Razorpay payment after client-side verification
    @PostMapping("/razorpay/capture")
    @PreAuthorize("hasAnyAuthority('" + SecurityConstants.ROLE_ADMIN + "', '" + SecurityConstants.ROLE_CUSTOMER + "')")
    public ResponseEntity<PaymentDTO> captureRazorpayPayment(@Validated @RequestBody RazorpayPaymentCaptureRequestDTO requestDTO) {
        try {
            validateOrderOwnership(requestDTO.getInternalOrderId());
            PaymentDTO paymentDTO = paymentService.captureRazorpayPayment(requestDTO);
            return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error capturing Razorpay payment: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get single payment details
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyAuthority('" + SecurityConstants.ROLE_ADMIN + "', '" + SecurityConstants.ROLE_CUSTOMER + "')")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long paymentId) {
        validatePaymentOwnership(paymentId);
        PaymentDTO paymentDTO = paymentService.getPaymentById(paymentId);
        return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
    }

    // Get all payments (ADMIN only)
    @GetMapping
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        List<PaymentDTO> payments = paymentService.getAllPayments();
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    // Get payments for a specific order
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyAuthority('" + SecurityConstants.ROLE_ADMIN + "', '" + SecurityConstants.ROLE_CUSTOMER + "')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByOrderId(@PathVariable Long orderId) {
        validateOrderOwnership(orderId);
        List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(orderId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    // Delete payment (ADMIN only)
    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        paymentService.deletePayment(paymentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
