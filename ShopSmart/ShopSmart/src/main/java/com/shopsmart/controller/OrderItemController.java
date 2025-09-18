package com.shopsmart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // NEW: Import PreAuthorize
import org.springframework.security.core.Authentication; // NEW: Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // NEW: Import SecurityContextHolder
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopsmart.config.SecurityConstants; // NEW: Import SecurityConstants
import com.shopsmart.dto.OrderDTO; // NEW: Import OrderDTO for helper method
import com.shopsmart.dto.OrderItemDTO;
import com.shopsmart.dto.UserDTO; // NEW: Import UserDTO for helper method
import com.shopsmart.exception.ResourceNotFoundException; // NEW: Import ResourceNotFoundException
import com.shopsmart.service.OrderItemService;
import com.shopsmart.service.OrderService; // NEW: Autowire OrderService
import com.shopsmart.service.UserService; // NEW: Autowire UserService

@RestController
@RequestMapping("/api/order-items")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderItemController {

	@Autowired
	private OrderItemService orderItemService;

	@Autowired // NEW: Autowire OrderService to get order details for authorization
	private OrderService orderService;

	@Autowired // NEW: Autowire UserService to get authenticated user details
	private UserService userService;

	// Helper method to get the authenticated customer's ID (which is the User ID)
	public Long getAuthenticatedCustomerId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String authenticatedUsername = authentication.getName();
		UserDTO userDTO = userService.getUserByUserName(authenticatedUsername);
		return userDTO.getId();
	}

	// Helper method to get the customer ID associated with a given order item ID
	public Long getOrderItemOwnerId(Long orderItemId) {
		OrderItemDTO orderItemDTO = orderItemService.getOrderItemById(orderItemId);
		if (orderItemDTO != null && orderItemDTO.getId() != null) {
			// Use orderService to get the customer ID for the associated order
			OrderDTO orderDTO = orderService.getOrderById(orderItemDTO.getId());
			if (orderDTO != null && orderDTO.getCustomerId() != null) {
				return orderDTO.getCustomerId();
			}
		}
		throw new ResourceNotFoundException("OrderItem or its associated Order", "Id for owner check", orderItemId);
	}


	@PostMapping
	// Restrict direct creation of OrderItems to ADMINs.
	// Typically, order items are created internally when an order is placed from a cart.
	@PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
	public ResponseEntity<OrderItemDTO> createOrderItem(@Validated @RequestBody OrderItemDTO orderItemDTO) {
		OrderItemDTO savedOrderItem = orderItemService.saveOrderItem(orderItemDTO);
		return new ResponseEntity<>(savedOrderItem, HttpStatus.CREATED);
	}

	@GetMapping
	// Only ADMINs can view all order items in the system.
	@PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
	public ResponseEntity<List<OrderItemDTO>> getAllOrderItems() {
		List<OrderItemDTO> orderItems = orderItemService.getAllOrderItems();
		return new ResponseEntity<>(orderItems, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	// ADMINs can view any order item. CUSTOMERs can view their own order items.
	@PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or @orderItemController.getAuthenticatedCustomerId() == @orderItemController.getOrderItemOwnerId(#id)")
	public ResponseEntity<OrderItemDTO> getOrderItemById(@PathVariable Long id) {
		OrderItemDTO orderItem = orderItemService.getOrderItemById(id);
		return new ResponseEntity<>(orderItem, HttpStatus.OK);
	}
}
