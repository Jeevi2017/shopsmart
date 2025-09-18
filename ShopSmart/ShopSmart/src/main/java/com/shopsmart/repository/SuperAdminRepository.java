package com.shopsmart.repository;

import com.shopsmart.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing SuperAdmin entities.
 * Extends JpaRepository to provide CRUD and pagination methods.
 */
@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {

    /**
     * Find a SuperAdmin by username.
     * 
     * @param username the username to search for
     * @return Optional containing the SuperAdmin if found
     */
    Optional<SuperAdmin> findByUsername(String username);

    /**
     * Find a SuperAdmin by email.
     *
     * @param email the email to search for
     * @return Optional containing the SuperAdmin if found
     */
    Optional<SuperAdmin> findByEmail(String email);

    /**
     * Check if a SuperAdmin exists with the given email.
     *
     * @param email the email to check
     * @return true if a SuperAdmin exists with the given email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a SuperAdmin exists with the given username.
     *
     * @param username the username to check
     * @return true if a SuperAdmin exists with the given username, false otherwise
     */
    boolean existsByUsername(String username);
}
