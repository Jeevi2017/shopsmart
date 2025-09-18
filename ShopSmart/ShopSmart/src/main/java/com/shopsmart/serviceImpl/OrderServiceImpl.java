package com.shopsmart.serviceImpl;

import com.shopsmart.dto.OrderDTO;
import com.shopsmart.dto.OrderItemDTO;
import com.shopsmart.dto.ProductDTO;
import com.shopsmart.entity.*;
import com.shopsmart.entity.Order.OrderStatus;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.*;
import com.shopsmart.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(Long customerId) {
        OrderDTO orderDTO = placeOrder(customerId);
        return orderDTO;
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for customer: " + customerId));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with an empty cart.");
        }

        // Validate product stock before creating order
        for (CartItem item : cart.getCartItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProduct().getId()));
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity());
            }
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(cart.getTotalAmount());
        order.setCouponCode(cart.getCouponCode());
        order.setDiscountAmount(cart.getDiscountAmount());

        // Shipping Address
        if (customer.getProfile() != null && customer.getProfile().getAddresses() != null
                && !customer.getProfile().getAddresses().isEmpty()) {
            Address shippingAddressEntity = customer.getProfile().getAddresses().stream()
                    .filter(address -> "SHIPPING".equalsIgnoreCase(address.getType()))
                    .findFirst()
                    .orElse(null);
            if (shippingAddressEntity != null) {
                order.setShippingAddress(formatAddress(shippingAddressEntity));
            } else {
                order.setShippingAddress("N/A - No default SHIPPING address found");
            }
        } else {
            order.setShippingAddress("N/A - No profile address found");
        }

        // Process cart items
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            order.addOrderItem(orderItem);

            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Handle coupon usage
        if (order.getCouponCode() != null && order.getDiscountAmount() != null
                && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discountRepository.findByCode(order.getCouponCode()).ifPresent(discount -> {
                if (discount.getUsageLimit() == null || discount.getUsedCount() < discount.getUsageLimit()) {
                    discount.setUsedCount(discount.getUsedCount() + 1);
                    discountRepository.save(discount);
                }
            });
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cart.getCartItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setCouponCode(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cartRepository.save(cart);

        return mapOrderToDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapOrderToDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomer_Id(customerId).stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        Order updatedOrder = orderRepository.save(order);

        return mapOrderToDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot cancel an order that is already " + order.getStatus().name());
        }
        order.setStatus(OrderStatus.CANCELLED);

        // Restore product stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProduct().getId()));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        // Decrement coupon usage if needed
        if (order.getCouponCode() != null && order.getDiscountAmount() != null
                && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discountRepository.findByCode(order.getCouponCode()).ifPresent(discount -> {
                if (discount.getUsedCount() > 0) {
                    discount.setUsedCount(discount.getUsedCount() - 1);
                    discountRepository.save(discount);
                }
            });
        }

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "Id", orderId));
        orderRepository.delete(order);
    }

    @Override
    @Transactional
    public Long getCustomerIdForOrderInternal(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "Id", orderId));
        return order.getCustomer().getId();
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapOrderToDTO).collect(Collectors.toList());
    }

    private OrderDTO mapOrderToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setCustomerId(order.getCustomer().getId());
        orderDTO.setOrderDate(order.getOrderDate());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setStatus(order.getStatus().name());
        orderDTO.setCouponCode(order.getCouponCode());
        orderDTO.setDiscountAmount(order.getDiscountAmount());
        orderDTO.setShippingAddress(order.getShippingAddress());

        if (order.getOrderItems() != null) {
            orderDTO.setOrderItems(order.getOrderItems().stream()
                    .map(this::mapOrderItemToDTO)
                    .collect(Collectors.toList()));
        } else {
            orderDTO.setOrderItems(List.of());
        }
        return orderDTO;
    }

    private OrderItemDTO mapOrderItemToDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setId(orderItem.getId());

        ProductDTO productDTO = new ProductDTO();
        if (orderItem.getProduct() != null) {
            productDTO.setId(orderItem.getProduct().getId());
            productDTO.setName(orderItem.getProduct().getName());
            productDTO.setPrice(orderItem.getProduct().getPrice());
            productDTO.setImages(orderItem.getProduct().getImages());
            productDTO.setDescription(orderItem.getProduct().getDescription());
            productDTO.setStockQuantity(orderItem.getProduct().getStockQuantity());
            if (orderItem.getProduct().getCategory() != null) {
                productDTO.setCategoryId(orderItem.getProduct().getCategory().getId());
                productDTO.setCategoryName(orderItem.getProduct().getCategory().getName());
            }
        }
        orderItemDTO.setProductDetails(productDTO);
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        return orderItemDTO;
    }

    private String formatAddress(Address address) {
        if (address == null) {
            return "N/A";
        }
        return String.format("%s, %s, %s, %s - %s",
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry());
    }
}