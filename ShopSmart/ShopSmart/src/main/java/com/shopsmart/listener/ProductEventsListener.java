package com.shopsmart.listener;

import com.shopsmart.entity.Product;
import com.shopsmart.event.ProductCreatedEvent;
import com.shopsmart.event.ProductDeletedEvent;
import com.shopsmart.event.ProductUpdatedEvent;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PostRemove;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ProductEventsListener {

    private static ApplicationEventPublisher publisher;

    @Autowired
    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostPersist
    public void onPostPersist(Product product) {
        publisher.publishEvent(new ProductCreatedEvent(product));
    }

    @PostUpdate
    public void onPostUpdate(Product product) {
        publisher.publishEvent(new ProductUpdatedEvent(product));
    }

    @PostRemove
    public void onPostRemove(Product product) {
        publisher.publishEvent(new ProductDeletedEvent(product.getId()));
    }
}