package com.shopsmart.config;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsmart.dto.OrderDTO;
import com.shopsmart.service.OrderService;
import com.shopsmart.exception.ResourceNotFoundException;
import static com.shopsmart.config.KafkaConstants.GROUP_ORDERS;
import static com.shopsmart.config.KafkaConstants.TOPIC_ORDERS;

@Component
public class OrderConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderConsumer(OrderService orderService) {
        this.orderService = orderService;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = TOPIC_ORDERS, groupId = GROUP_ORDERS)
    public void listen(String message) {
        try {
            // Deserialize message as JSON to get customerId
            OrderDTO orderMessage = objectMapper.readValue(message, OrderDTO.class);
            Long customerId = orderMessage.getCustomerId();
            if (customerId == null) {
                throw new IllegalArgumentException("Customer ID is missing in Kafka message: " + message);
            }

            // Call service to create order from cart
            OrderDTO createdOrder = orderService.createOrderFromCart(customerId);
            System.out.println("✅ Order created successfully for customerId " + customerId + ": " + createdOrder.getId());

        } catch (ResourceNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to process Kafka order message: " + message);
            e.printStackTrace();
        }
    }
}