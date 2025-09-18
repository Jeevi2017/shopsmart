package com.shopsmart.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.shopsmart.entity.OrderItem;

@Repository

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
