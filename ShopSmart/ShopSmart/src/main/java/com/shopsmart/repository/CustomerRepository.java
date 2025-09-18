package com.shopsmart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
// Removed Query and Param imports as they are no longer needed for findByUsername
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shopsmart.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findById(Long customerId);

    // FIX: Changed to findByUsername directly as Customer extends User and User has 'username' field.
    // The @Param annotation with "userName" was incorrect for the 'username' field.
    Optional<Customer> findByUsername(String username);
}
