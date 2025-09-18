package com.shopsmart.service;

import com.shopsmart.config.KafkaConstants;
import com.shopsmart.dto.OrderDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, OrderDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderMessage(OrderDTO orderDTO) {
        kafkaTemplate.send(KafkaConstants.TOPIC_ORDERS, orderDTO);
    }
}