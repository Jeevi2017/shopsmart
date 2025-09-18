package com.shopsmart.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsmart.dto.CartDTO;
import com.shopsmart.dto.CartItemDTO;
import com.shopsmart.dto.ProductDTO;
import com.shopsmart.entity.Cart;
import com.shopsmart.entity.CartItem;
import com.shopsmart.entity.Customer;
import com.shopsmart.entity.Discount;
import com.shopsmart.entity.Discount.DiscountType;
import com.shopsmart.entity.Product;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.CartItemRepository;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.CustomerRepository;
import com.shopsmart.repository.DiscountRepository;
import com.shopsmart.repository.ProductRepository;
import com.shopsmart.service.CartService;
import com.shopsmart.service.DiscountService;


@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private DiscountService discountService;

	@Autowired
	private DiscountRepository discountRepository;

	@Override
	@Transactional
	public Cart getOrCreateCart(Long customerId) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", "Id", customerId));

		Optional<Cart> existingCart = cartRepository.findByCustomerId(customerId);

		if (existingCart.isPresent()) {
			Cart cart = existingCart.get();
			recalculateCartTotal(cart);
			return cart;
		} else {
			Cart newCart = new Cart();
			newCart.setCustomer(customer);
			newCart.setCreatedAt(LocalDateTime.now());
			newCart.setUpdatedAt(LocalDateTime.now());
			newCart.setTotalAmount(BigDecimal.ZERO);
			newCart.setTotalPrice(BigDecimal.ZERO); // Ensure totalPrice is initialized
			newCart.setDiscountAmount(BigDecimal.ZERO);
			newCart.setCouponCode(null);

			Cart savedCart = cartRepository.save(newCart);
			return savedCart;
		}
	}

	@Override
	@Transactional
	public CartDTO addProductToCart(Long customerId, Long productId, Long quantity) {
		Cart cart = getOrCreateCart(customerId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "Id", productId));

		if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
			throw new IllegalArgumentException("Not enough stock for product: " + product.getName() + ". Available: "
					+ (product.getStockQuantity() != null ? product.getStockQuantity() : 0));
		}

		Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

		CartItem cartItem;
		if (existingCartItem.isPresent()) {
			cartItem = existingCartItem.get();
			Long oldQuantity = cartItem.getQuantity();
			Long quantityToAdd = quantity;
			if (product.getStockQuantity() < (oldQuantity + quantityToAdd)) {
				throw new IllegalArgumentException("Adding " + quantityToAdd + " units of " + product.getName() + " would exceed available stock. Current in cart: " + oldQuantity + ", Available: " + product.getStockQuantity());
			}
			product.setStockQuantity(product.getStockQuantity() - quantityToAdd);
			cartItem.setQuantity(oldQuantity + quantityToAdd);
		} else {
			cartItem = new CartItem();
			cartItem.setCart(cart);
			cartItem.setProduct(product);
			cartItem.setQuantity(quantity);
			cartItem.setPrice(product.getPrice());
			cart.addCartItem(cartItem);
			product.setStockQuantity(product.getStockQuantity() - quantity);
		}

		cartItemRepository.save(cartItem);
		productRepository.save(product);

		recalculateCartTotal(cart);
		cartRepository.save(cart);

		return mapCartToDTO(cart);
	}

	@Override
	@Transactional
	public CartDTO updateProductQuantityInCart(Long customerId, Long productId, Long newQuantity) {
		Cart cart = getOrCreateCart(customerId);

		if (newQuantity <= 0) {
			return removeProductFromCart(customerId, productId);
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "Id", productId));

		CartItem cartItem = cart.getCartItems().stream()
				.filter(item -> item.getProduct().getId().equals(productId))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("CartItem", "Product Id in Cart", productId));

		Long oldQuantity = cartItem.getQuantity();
		Long quantityChange = newQuantity - oldQuantity;

		if (quantityChange > 0 && (product.getStockQuantity() == null || product.getStockQuantity() < quantityChange)) {
			throw new IllegalArgumentException("Not enough stock for product: " + product.getName() + ". Available: "
					+ (product.getStockQuantity() != null ? product.getStockQuantity() : 0));
		}

		product.setStockQuantity(product.getStockQuantity() - quantityChange);
		productRepository.save(product);

		cartItem.setQuantity(newQuantity);
		cartItemRepository.save(cartItem);

		recalculateCartTotal(cart);
		cartRepository.save(cart);

		return mapCartToDTO(cart);
	}

	@Override
	@Transactional
	public CartDTO removeProductFromCart(Long customerId, Long productId) {
		Cart cart = getOrCreateCart(customerId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "Id", productId));

		CartItem cartItem = cart.getCartItems().stream()
				.filter(item -> item.getProduct().getId().equals(productId))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("CartItem", "Product Id in Cart", productId));

		if (product.getStockQuantity() != null) {
			product.setStockQuantity(product.getStockQuantity() + cartItem.getQuantity());
			productRepository.save(product);
		}

		cart.removeCartItem(cartItem);
		cartItemRepository.delete(cartItem);

		recalculateCartTotal(cart);
		Cart updatedCart = cartRepository.save(cart);

		return mapCartToDTO(updatedCart);
	}

	@Override
	@Transactional
	public void clearCart(Long customerId) {
		Cart cart = getOrCreateCart(customerId);

		for (CartItem item : new ArrayList<>(cart.getCartItems())) {
			Product product = item.getProduct();
			if (product.getStockQuantity() != null) {
				product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
				productRepository.save(product);
			}
			cart.removeCartItem(item);
		}

		cartItemRepository.deleteAll(cart.getCartItems());

		cart.setTotalPrice(BigDecimal.ZERO); // Also reset totalPrice
		cart.setTotalAmount(BigDecimal.ZERO);
		cart.setCouponCode(null);
		cart.setDiscountAmount(BigDecimal.ZERO);
		cart.setUpdatedAt(LocalDateTime.now());
		cartRepository.save(cart);
	}

	@Override
	@Transactional(readOnly = true)
	public CartDTO getCartById(Long cartId) {
		Cart cart = cartRepository.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "Id", cartId));
		recalculateCartTotal(cart);
		return mapCartToDTO(cart);
	}

	@Override
	@Transactional(readOnly = true)
	public CartDTO getCartByCustomerId(Long customerId) {
		Cart cart = getOrCreateCart(customerId);
		return mapCartToDTO(cart);
	}

	@Override
	@Transactional
	public CartDTO applyCouponToCart(Long customerId, String couponCode) {
		Cart cart = getOrCreateCart(customerId);
		if (cart.getCartItems().isEmpty()) {
			throw new IllegalArgumentException("Cannot apply coupon to an empty cart.");
		}

		Discount discount = discountRepository.findByCode(couponCode)
				.orElseThrow(() -> new ResourceNotFoundException("Discount", "code", couponCode));

		if (!discountService.isValidDiscount(couponCode, calculateSubtotal(cart))) {
			throw new IllegalArgumentException("Coupon code is invalid or expired or does not meet minimum amount.");
		}

		cart.setCouponCode(couponCode);
		recalculateCartTotal(cart);
		cart.setUpdatedAt(LocalDateTime.now());
		Cart savedCart = cartRepository.save(cart);
		return mapCartToDTO(savedCart);
	}

	@Override
	@Transactional
	public CartDTO removeCouponFromCart(Long customerId) {
		Cart cart = getOrCreateCart(customerId);
		if (cart.getCouponCode() == null) {
			throw new IllegalArgumentException("No coupon is currently applied to the cart.");
		}
		cart.setCouponCode(null);
		cart.setDiscountAmount(BigDecimal.ZERO);
		recalculateCartTotal(cart);
		cart.setUpdatedAt(LocalDateTime.now());
		Cart savedCart = cartRepository.save(cart);
		return mapCartToDTO(savedCart);
	}


	// Inside your CartServiceImpl.java
	private void recalculateCartTotal(Cart cart) {
	    // 1. Calculate the subtotal from all items in the cart
	    final BigDecimal subtotal = calculateSubtotal(cart);
	    cart.setTotalPrice(subtotal);

	    // 2. Initialize the final total with the subtotal
	    BigDecimal finalTotal = subtotal;
	    cart.setDiscountAmount(BigDecimal.ZERO);

	    // 3. Check for and apply coupon discount
	    if (cart.getCouponCode() != null && !cart.getCouponCode().isEmpty()) {
	        Optional<Discount> discountOpt = discountRepository.findByCode(cart.getCouponCode());
	        if (discountOpt.isPresent()) {
	            Discount discount = discountOpt.get();
	            if (discountService.isValidDiscount(discount.getCode(), subtotal)) {
	                BigDecimal calculatedDiscount = BigDecimal.ZERO;
	                if (discount.getType() == DiscountType.PERCENTAGE) {
	                    calculatedDiscount = subtotal.multiply(discount.getValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
	                } else if (discount.getType() == DiscountType.FIXED_AMOUNT) {
	                    calculatedDiscount = discount.getValue();
	                }

	                // Ensure discount doesn't exceed the final total
	                if (calculatedDiscount.compareTo(finalTotal) > 0) {
	                    calculatedDiscount = finalTotal;
	                }

	                // Now, update the cart entity fields directly outside of a nested lambda
	                cart.setDiscountAmount(calculatedDiscount.setScale(2, RoundingMode.HALF_UP));
	                finalTotal = finalTotal.subtract(calculatedDiscount);

	            } else {
	                // If coupon is invalid, clear it
	                cart.setCouponCode(null);
	                cart.setDiscountAmount(BigDecimal.ZERO);
	            }
	        }
	    }

	    // 4. Ensure final total is not negative
	    if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
	        finalTotal = BigDecimal.ZERO;
	    }
	    
	    // 5. Set the final calculated total and update timestamp
	    cart.setTotalAmount(finalTotal.setScale(2, RoundingMode.HALF_UP));
	    cart.setUpdatedAt(LocalDateTime.now());
	}



	private BigDecimal calculateSubtotal(Cart cart) {
		BigDecimal subtotal = BigDecimal.ZERO;
		if (cart.getCartItems() != null) {
			for (CartItem item : cart.getCartItems()) {
				if (item.getProduct() != null && item.getPrice() != null && item.getQuantity() != null) {
					subtotal = subtotal.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
				}
			}
		}
		return subtotal.setScale(2, RoundingMode.HALF_UP);
	}

	private CartDTO mapCartToDTO(Cart cart) {
		CartDTO cartDTO = new CartDTO();
		cartDTO.setId(cart.getId());
		cartDTO.setCustomerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null);
		cartDTO.setCreatedAt(cart.getCreatedAt());
		cartDTO.setUpdatedAt(cart.getUpdatedAt());

		// FIX: Calculate and set the totalPrice before returning the DTO
		cartDTO.setTotalPrice(calculateSubtotal(cart));

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
