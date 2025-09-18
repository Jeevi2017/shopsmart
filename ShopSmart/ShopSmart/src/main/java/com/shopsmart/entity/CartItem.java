package com.shopsmart.entity;

import java.math.BigDecimal;
import java.util.Objects; // NEW: Import Objects for equals/hashCode

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name ="cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Transient
    public BigDecimal getItemTotal() {
        if (price != null && quantity != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    public CartItem() {
        super();
    }

    public CartItem(Long id, Cart cart, Product product, Long quantity, BigDecimal price) {
        super();
        this.id = id;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    // NEW: Override equals() and hashCode() for proper JPA entity behavior
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        // Use ID for equality if it's already persisted, otherwise use natural keys.
        // For CartItem, a combination of cart, product, and ID might be considered.
        // For simplicity, we'll use ID if available, otherwise a combination of relations.
        return Objects.equals(id, cartItem.id) &&
               Objects.equals(cart != null ? cart.getId() : null, cartItem.cart != null ? cartItem.cart.getId() : null) &&
               Objects.equals(product != null ? product.getId() : null, cartItem.product != null ? cartItem.product.getId() : null);
    }

    @Override
    public int hashCode() {
        // Use ID for hashCode if it's already persisted, otherwise a combination of relations.
        return Objects.hash(id, cart != null ? cart.getId() : null, product != null ? product.getId() : null);
    }

    // NEW: Override toString() for better logging and debugging
    @Override
    public String toString() {
        return "CartItem{" +
               "id=" + id +
               ", cartId=" + (cart != null ? cart.getId() : "null") +
               ", productId=" + (product != null ? product.getId() : "null") +
               ", quantity=" + quantity +
               ", price=" + price +
               ", itemTotal=" + getItemTotal() +
               '}';
    }
}
