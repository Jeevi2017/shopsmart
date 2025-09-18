package com.shopsmart.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopsmart.entity.Category;
import com.shopsmart.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Core CRUD Operations (provided by JpaRepository, but explicitly listed for clarity)
    Optional<Product> findById(Long id);
    void deleteById(Long id); // Corrected parameter name from productId to id

    // New Business Logic Queries
    
    // Find products by category
    List<Product> findByCategory(Category category);

    // Find products by a specific price range
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find products with a stock quantity greater than zero
    List<Product> findByStockQuantityGreaterThan(Integer stockQuantity);

    // Find a product by name (case-insensitive) and category ID
    Optional<Product> findByNameIgnoreCaseAndCategoryId(String name, Long categoryId);
}