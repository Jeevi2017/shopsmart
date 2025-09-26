package com.shopsmart.controller;

import com.shopsmart.dto.DiscountDTO;
import com.shopsmart.dto.ApplyCouponRequest;
import com.shopsmart.dto.CartDTO;
import com.shopsmart.service.DiscountService;
import com.shopsmart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@CrossOrigin(origins = "http://localhost:4200")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private CartService cartService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // This requires the user to have 'ROLE_ADMIN' authority.
    public ResponseEntity<DiscountDTO> createDiscount(@Validated @RequestBody DiscountDTO discountDTO) {
        DiscountDTO createdDiscount = discountService.createDiscount(discountDTO);
        return new ResponseEntity<>(createdDiscount, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DiscountDTO> getDiscountById(@PathVariable Long id) {
        DiscountDTO discount = discountService.getDiscountById(id);
        return new ResponseEntity<>(discount, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DiscountDTO>> getAllDiscounts() {
        List<DiscountDTO> discounts = discountService.getAllDiscounts();
        return new ResponseEntity<>(discounts, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DiscountDTO> updateDiscount(@PathVariable Long id, @Validated @RequestBody DiscountDTO discountDTO) {
        DiscountDTO updatedDiscount = discountService.updateDiscount(id, discountDTO);
        return new ResponseEntity<>(updatedDiscount, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/apply-coupon/{customerId}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<CartDTO> applyCoupon(@PathVariable Long customerId, @RequestBody ApplyCouponRequest request) {
        try {
            CartDTO updatedCart = cartService.applyCouponToCart(customerId, request.getCouponCode());
            return new ResponseEntity<>(updatedCart, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/remove-coupon/{customerId}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<CartDTO> removeCoupon(@PathVariable Long customerId) {
        try {
            CartDTO updatedCart = cartService.removeCouponFromCart(customerId);
            return new ResponseEntity<>(updatedCart, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/available-for-customer/{customerId}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')") // Or use a more complex expression if needed, e.g., "hasAuthority('ROLE_CUSTOMER') and #customerId == authentication.principal.id"
    public ResponseEntity<List<DiscountDTO>> getAvailableCouponsForCustomer(@PathVariable Long customerId) {
        // In a real application, you might pass the customerId to the service
        // to filter coupons based on user-specific criteria (e.g., usage history).
        // For now, we assume discountService.getAvailableCouponsForCustomer() handles filtering
        // active and non-expired coupons.
        List<DiscountDTO> availableCoupons = discountService.getAvailableCouponsForCustomer(customerId);
        return new ResponseEntity<>(availableCoupons, HttpStatus.OK);
    }
}
