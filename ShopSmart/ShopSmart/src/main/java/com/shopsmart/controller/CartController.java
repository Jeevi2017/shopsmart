package com.shopsmart.controller;

import com.shopsmart.dto.CartDTO;
import com.shopsmart.dto.CartItemDTO;
import com.shopsmart.dto.ProductDTO;
import com.shopsmart.entity.Cart;
import com.shopsmart.entity.CartItem;
import com.shopsmart.service.CartService;
import com.shopsmart.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CustomerService customerService;

    // Helper method to get the authenticated customer's ID
    public Long getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserName = authentication.getName();
        return customerService.getCustomerByUsername(authenticatedUserName).getId();
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #customerId == @cartController.getAuthenticatedCustomerId()")
    public ResponseEntity<CartDTO> getCartByCustomerId(@PathVariable Long customerId) {
        Cart cart = cartService.getOrCreateCart(customerId);
        return new ResponseEntity<>(mapCartToDTO(cart), HttpStatus.OK);
    }

    @PostMapping("/customer/{customerId}/items")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #customerId == @cartController.getAuthenticatedCustomerId()")
    public ResponseEntity<CartDTO> addProductToCart(
            @PathVariable Long customerId,
            @RequestBody CartItemDTO cartItemDTO) {

        CartDTO updatedCart = cartService.addProductToCart(customerId, cartItemDTO.getProductId(), cartItemDTO.getQuantity());
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @PutMapping("/customer/{customerId}/items/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #customerId == @cartController.getAuthenticatedCustomerId()")
    public ResponseEntity<CartDTO> updateProductQuantityInCart(
            @PathVariable Long customerId,
            @PathVariable Long productId,
            @RequestParam Long newQuantity) { // FIX: Changed from Long to int
        CartDTO updatedCart = cartService.updateProductQuantityInCart(customerId, productId, newQuantity);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @DeleteMapping("/customer/{customerId}/items/{productId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #customerId == @cartController.getAuthenticatedCustomerId()")
    public ResponseEntity<CartDTO> removeProductFromCart(
            @PathVariable Long customerId,
            @PathVariable Long productId) {
        CartDTO updatedCart = cartService.removeProductFromCart(customerId, productId);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @DeleteMapping("/customer/{customerId}/clear")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #customerId == @cartController.getAuthenticatedCustomerId()")
    public ResponseEntity<Void> clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/id/{cartId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CartDTO> getCartById(@PathVariable Long cartId) {
        CartDTO cart = cartService.getCartById(cartId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    // Helper method to map Cart entity to CartDTO
 // Inside your CartController.java
 // Helper method to map Cart entity to CartDTO
 private CartDTO mapCartToDTO(Cart cart) {
     CartDTO cartDTO = new CartDTO();
     cartDTO.setId(cart.getId());
     cartDTO.setCustomerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null);
     cartDTO.setCreatedAt(cart.getCreatedAt());
     cartDTO.setUpdatedAt(cart.getUpdatedAt());

     // ADDED THIS LINE: Now mapping totalPrice from the entity to the DTO
     cartDTO.setTotalPrice(cart.getTotalPrice());

     cartDTO.setTotalAmount(cart.getTotalAmount());
     cartDTO.setCouponCode(cart.getCouponCode());
     cartDTO.setDiscountAmount(cart.getDiscountAmount());

     if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
         cartDTO.setCartItems(
                 cart.getCartItems().stream().map(this::mapCartItemToDTO).collect(Collectors.toList()));
     } else {
         cartDTO.setCartItems(new ArrayList<>());
     }
     return cartDTO;
 }


    // Helper method to map CartItem entity to CartItemDTO
    private CartItemDTO mapCartItemToDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(cartItem.getId());
        cartItemDTO.setProductId(cartItem.getProduct() != null ? cartItem.getProduct().getId() : null);
        cartItemDTO.setQuantity(cartItem.getQuantity());
        cartItemDTO.setPrice(cartItem.getPrice());

        ProductDTO productDTO = new ProductDTO();
        if (cartItem.getProduct() != null) {
            productDTO.setId(cartItem.getProduct().getId());
            productDTO.setName(cartItem.getProduct().getName());
            productDTO.setPrice(cartItem.getProduct().getPrice());
            productDTO.setImages(cartItem.getProduct().getImages());
            productDTO.setDescription(cartItem.getProduct().getDescription());
            productDTO.setStockQuantity(cartItem.getProduct().getStockQuantity());

            if (cartItem.getProduct().getCategory() != null) {
                productDTO.setCategoryId(cartItem.getProduct().getCategory().getId());
                productDTO.setCategoryName(cartItem.getProduct().getCategory().getName());
            }
        }
        cartItemDTO.setProductDetails(productDTO);

        return cartItemDTO;
    }
}
