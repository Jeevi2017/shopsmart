package com.shopsmart.service;

import com.shopsmart.config.KafkaConstants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = KafkaConstants.TOPIC_ORDERS, groupId = KafkaConstants.GROUP_ORDERS)
    public void consumeOrder(String message) {
        System.out.println("📥 Received Order Message: " + message);
    }
}