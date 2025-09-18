package com.shopsmart.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_order")
public class Order {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	@Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

	@Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

	@Column(name = "shipping_address")
    private String shippingAddress;

	@Column(name = "coupon_code")
    private String couponCode;

	@Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

	@ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
	
    public enum OrderStatus {
        PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED, PAID, RETURNED, DRAFT
    }

    public Order() {
        // No-argument constructor
    }

    public Order(Long id, LocalDateTime orderDate, BigDecimal totalAmount, OrderStatus status, String shippingAddress,
                 String couponCode, BigDecimal discountAmount, List<OrderItem> orderItems, List<Payment> payments,
                 Customer customer) {
        this.id = id;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
        this.orderItems = orderItems;
        this.payments = payments;
        this.customer = customer;
    }
	
    public Long getId() {
        return id;
    }
	
    public void setId(Long id) {
        this.id = id;
    }
	
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
	
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
	
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
	
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
	
    public OrderStatus getStatus() {
        return status;
    }
	
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
	
    public String getShippingAddress() {
        return shippingAddress;
    }
	
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
	
    public String getCouponCode() {
        return couponCode;
    }
	
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
	
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
	
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
	
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
	
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
	
    public List<Payment> getPayments() {
        return payments;
    }
	
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
	
    public Customer getCustomer() {
        return customer;
    }
	
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
	
    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }
	
    // Helper methods for bidirectional relationship with OrderItem
    public void addOrderItem(OrderItem orderItem) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
	
    public void removeOrderItem(OrderItem orderItem) {
        if (orderItems != null) {
            orderItems.remove(orderItem);
            orderItem.setOrder(null);
        }
    }
	
    // Helper methods for bidirectional relationship with Payment
    public void addPayment(Payment payment) {
        if (payments == null) {
            payments = new ArrayList<>();
        }
        payments.add(payment);
        payment.setOrder(this);
    }
	
    public void removePayment(Payment payment) {
        if (payments != null) {
            payments.remove(payment);
            payment.setOrder(null);
        }
    }
	
    // NEW: Override equals() and hashCode() for proper JPA entity behavior
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        // Use ID for equality if it's already persisted.
        return Objects.equals(id, order.id);
    }
	
    @Override
    public int hashCode() {
        // Use ID for hashCode if it's already persisted.
        return Objects.hash(id);
    }
	
    // NEW: Override toString() for better logging and debugging
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", couponCode='" + couponCode + '\'' +
                ", discountAmount=" + discountAmount +
                ", customerId=" + (customer != null ? customer.getId() : "null") +
                ", orderItemsCount=" + (orderItems != null ? orderItems.size() : 0) +
                ", paymentsCount=" + (payments != null ? payments.size() : 0) +
                '}';
    }
}
