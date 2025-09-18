package com.shopsmart.event;

import com.shopsmart.entity.Product;

public class ProductCreatedEvent {
    private final Product product;
    public ProductCreatedEvent(Product product) { this.product = product; }
    public Product getProduct() { return product; }
}