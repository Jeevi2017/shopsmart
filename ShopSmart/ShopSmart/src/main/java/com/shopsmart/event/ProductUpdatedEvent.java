package com.shopsmart.event;

import com.shopsmart.entity.Product;

public class ProductUpdatedEvent {
    private final Product product;
    public ProductUpdatedEvent(Product product) { this.product = product; }
    public Product getProduct() { return product; }
}
