package com.shopsmart.service;

import com.shopsmart.config.KafkaConstants;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.shopsmart.dto.OrderDTO;

@Service
public class OrderProducerService {

    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;

    public OrderProducerService(KafkaTemplate<String, OrderDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderEvent(OrderDTO orderDTO) {
        kafkaTemplate.send(KafkaConstants.TOPIC_ORDERS, orderDTO);
        System.out.println("Sent order event to Kafka: " + orderDTO);
    }
}
