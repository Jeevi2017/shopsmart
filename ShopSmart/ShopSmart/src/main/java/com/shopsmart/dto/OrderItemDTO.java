package com.shopsmart.dto;

import java.math.BigDecimal;

public class OrderItemDTO {

	private Long id;

	private Long productId;

	private ProductDTO productDetails;

	private Long quantity;
	private BigDecimal price;
	

	public OrderItemDTO() {
		super();
	}

    public OrderItemDTO(Long id, Long productId, ProductDTO productDetails, Long quantity, BigDecimal price) {
        this.id = id;
        this.productId = productId;
        this.productDetails = productDetails;
        this.quantity = quantity;
        this.price = price;
    }

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
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

	public ProductDTO getProductDetails() {
		return productDetails;
	}

	public void setProductDetails(ProductDTO productDetails) {
		this.productDetails = productDetails;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}
}
