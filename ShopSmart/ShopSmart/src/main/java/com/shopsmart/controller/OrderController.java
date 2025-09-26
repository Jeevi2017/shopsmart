package com.shopsmart.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.shopsmart.config.KafkaConstants;
import com.shopsmart.config.SecurityConstants;
import com.shopsmart.dto.OrderDTO;
import com.shopsmart.dto.UserDTO;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.service.CustomerService;
import com.shopsmart.service.OrderService;
import com.shopsmart.service.UserService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    @Autowired
    private KafkaTemplate<String, OrderDTO> orderKafkaTemplate;

    /**
     * Helper: Get authenticated customer ID
     */
    public Long getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUsername = authentication.getName();
        UserDTO userDTO = userService.getUserByUserName(authenticatedUsername);
        return userDTO.getId();
    }

    /**
     * Helper: Get customerId from an order
     */
    public Long getCustomerIdByOrderId(Long orderId) {
        OrderDTO orderDTO = orderService.getOrderById(orderId);
        if (orderDTO != null && orderDTO.getCustomerId() != null) {
            return orderDTO.getCustomerId();
        }
        throw new ResourceNotFoundException("Order", "Id", orderId);
    }

    /**
     * Get all orders (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * Get order by ID (Admin or Order Owner)
     * FIX: The ownership check is now handled directly by the @PreAuthorize annotation,
     * which is a cleaner and more secure approach. The redundant manual check has been removed.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or (@orderController.getAuthenticatedCustomerId() == @orderController.getCustomerIdByOrderId(#orderId))")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    /**
     * Get orders by customerId (Admin or Owner)
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #customerId == @orderController.getAuthenticatedCustomerId()")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    /**
     * Create order (Customer only) -> Publishes event to Kafka
     */
    @PostMapping("/from-cart/{customerId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_CUSTOMER + "') and #customerId == @orderController.getAuthenticatedCustomerId()")
    public ResponseEntity<Map<String, Object>> createOrderFromCart(@PathVariable Long customerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // ✅ Create and persist order from cart
            OrderDTO createdOrder = orderService.createOrderFromCart(customerId);

            if (createdOrder == null || createdOrder.getId() == null) {
                response.put("success", false);
                response.put("message", "Order could not be created");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // ✅ Publish order event to Kafka
            orderKafkaTemplate.send(KafkaConstants.TOPIC_ORDERS, createdOrder);

            // ✅ Proper JSON response with orderId
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("orderId", createdOrder.getId());
            response.put("status", createdOrder.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating order");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete order (Admin only)
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update order status (Admin only)
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
                @PathVariable Long orderId,
                @Validated @RequestBody String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    /**
     * Generate PDF for a specific order by ID (Admin or Order Owner)
     * FIX: The @PreAuthorize expression is now correctly updated to include
     * an ownership check for customers, preventing a security vulnerability.
     */
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or (@orderController.getAuthenticatedCustomerId() == @orderController.getCustomerIdByOrderId(#orderId))")
    @GetMapping("/{orderId}/report/pdf")
    public ResponseEntity<byte[]> generateOrderReportPdf(@PathVariable Long orderId) {
        try {
            byte[] pdfBytes = orderService.generateOrderReportPdf(orderId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "order_report_" + orderId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            // Log the exception details for debugging
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
