package com.shopsmart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopsmart.entity.Cart;
import com.shopsmart.entity.Customer;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
	Optional<Cart> findByCustomer(Customer customer);


    Optional<Cart> findByCustomerId(Long customerId);


}
