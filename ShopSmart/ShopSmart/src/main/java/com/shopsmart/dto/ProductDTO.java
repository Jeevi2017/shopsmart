package com.shopsmart.dto;

import java.math.BigDecimal;
import java.util.List;



public class ProductDTO {

	private Long id;
	private String name;
	private String description;
	private List<String> images;
	private BigDecimal price;
	
	private Long categoryId;
	private String categoryName; 

	private Long stockQuantity;

	public ProductDTO() {
	}

	public ProductDTO(Long id, String name, String description, List<String> images, BigDecimal price,
			Long categoryId, String categoryName, Long stockQuantity) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.images = images;
		this.price = price;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.stockQuantity = stockQuantity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Long getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(Long stockQuantity) {
		this.stockQuantity = stockQuantity;
	}
}
