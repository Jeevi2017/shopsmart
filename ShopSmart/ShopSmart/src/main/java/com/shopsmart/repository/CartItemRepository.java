package com.shopsmart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopsmart.entity.Cart;
import com.shopsmart.entity.CartItem;
import com.shopsmart.entity.Product;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

	void deleteByCartAndProduct(Cart cart, Product product);
}
