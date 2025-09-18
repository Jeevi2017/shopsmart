package com.shopsmart.dto;

import java.math.BigDecimal;

public class CartItemDTO {

	private Long id;

	private Long productId;

	private Long quantity;
	private BigDecimal price;

	private ProductDTO productDetails;

	public CartItemDTO() {
		super();
	}

	public CartItemDTO(Long id, Long productId, Long quantity, BigDecimal price, ProductDTO productDetails) {
		super();
		this.id = id;
		this.productId = productId;
		this.quantity = quantity;
		this.price = price;
		this.productDetails = productDetails; 
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public ProductDTO getProductDetails() {
		return productDetails;
	}

	public void setProductDetails(ProductDTO productDetails) {
		this.productDetails = productDetails;
	}
}
