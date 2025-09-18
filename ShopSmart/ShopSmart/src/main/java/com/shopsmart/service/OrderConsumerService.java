package com.shopsmart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.shopsmart.config.KafkaConstants;
import com.shopsmart.dto.OrderDTO;
import com.shopsmart.exception.ResourceNotFoundException;

@Service
public class OrderConsumerService {

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = KafkaConstants.TOPIC_ORDERS, groupId = KafkaConstants.GROUP_ORDERS)
    public void consumeOrderMessage(OrderDTO orderDTO) {
        try {
            Long customerId = orderDTO.getCustomerId();

            if (customerId == null) {
                throw new IllegalArgumentException("Customer ID is missing in Kafka message.");
            }

            // Call business logic to create order from customer cart
            OrderDTO createdOrder = orderService.createOrderFromCart(customerId);

            System.out.println("✅ Order created successfully: " + createdOrder.getId());
        } catch (ResourceNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to process order message: " + orderDTO);
            e.printStackTrace();
        }
    }
}