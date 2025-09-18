package com.shopsmart.service;

import java.util.List;

import com.shopsmart.dto.OrderDTO;

public interface OrderService {

   
    OrderDTO placeOrder(Long customerId); 

    List<OrderDTO> getAllOrders();

    OrderDTO getOrderById(Long orderId);

    void deleteOrder(Long orderId);

    List<OrderDTO> getOrdersByCustomerId(Long customerId);

    Long getCustomerIdForOrderInternal(Long orderId);

    OrderDTO updateOrderStatus(Long orderId, String status); 
    void cancelOrder(Long orderId);

    OrderDTO createOrderFromCart(Long customerId);
}
