package com.shopsmart.event;

public class ProductDeletedEvent {
    private final Long productId;
    public ProductDeletedEvent(Long productId) { this.productId = productId; }
    public Long getProductId() { return productId; }
}
