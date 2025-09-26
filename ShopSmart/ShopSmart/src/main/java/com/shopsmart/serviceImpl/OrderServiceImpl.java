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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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
    
    @Override
    public byte[] generateOrderReportPdf(Long orderId) throws IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add a title
        document.add(new Paragraph("Order Summary Report")
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER));

        // Add order details
        document.add(new Paragraph("Order ID: " + order.getId()));
        document.add(new Paragraph("Order Date: " + order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        document.add(new Paragraph("Customer ID: " + order.getCustomer().getId()));
        document.add(new Paragraph("Status: " + order.getStatus()));
        document.add(new Paragraph("Shipping Address: " + order.getShippingAddress()));
        
        // Add a section for order items
        document.add(new Paragraph("Order Items").setFontSize(18));

        // Create a table for the order items
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell("Product Name");
        table.addHeaderCell("Quantity");
        table.addHeaderCell("Price");
        table.addHeaderCell("Total");

        for (OrderItem item : order.getOrderItems()) {
            table.addCell(item.getProduct().getName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("Rs.%.2f", item.getPrice()));
            table.addCell(String.format("Rs.%.2f", BigDecimal.valueOf(item.getQuantity()).multiply(item.getPrice())));
        }
        document.add(table);
        
        // Add summary at the end
        document.add(new Paragraph("Subtotal: Rs." + getOrderSubtotal(order).toPlainString())
                .setTextAlignment(TextAlignment.RIGHT));

        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            document.add(new Paragraph("Discount (" + order.getCouponCode() + "): -Rs." + order.getDiscountAmount().toPlainString())
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(new Paragraph("Total Amount: Rs." + order.getTotalAmount().toPlainString())
                .setFontSize(16)
                .setTextAlignment(TextAlignment.RIGHT));
        
        document.close();
        
        // Return the byte array directly
        return baos.toByteArray();
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

    private BigDecimal getOrderSubtotal(Order order) {
        return order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
