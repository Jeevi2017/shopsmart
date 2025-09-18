package com.shopsmart.controller;

import com.shopsmart.config.SecurityConstants; // NEW: Import SecurityConstants
import com.shopsmart.dto.UserDTO; // Import UserDTO
import com.shopsmart.service.UserService; // Import UserService
import com.shopsmart.exception.ResourceNotFoundException; // Import ResourceNotFoundException

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // For the role update request body

@RestController
@RequestMapping("/api/admin/users") // Base path for user management by admins
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private UserService userService; // Use UserService for all user management operations

    // Removed: @Autowired private AdminService adminService; (AdminService was removed)
    // Removed: @PostMapping("/login") (Login is handled by AuthController)

    // Endpoint to get all users (accessible by ADMIN and SUPER_ADMIN due to role hierarchy)
    @GetMapping
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Endpoint to get a user by ID (accessible by ADMIN and SUPER_ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // Endpoint to update user details (accessible by ADMIN and SUPER_ADMIN)
    // Note: This should not be used for password or role changes.
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Validated @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // Endpoint to delete a user (accessible by ADMIN and SUPER_ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // NEW: Endpoint for Super Admin to update a user's roles
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SUPER_ADMIN + "')") // ONLY SUPER_ADMIN can change roles
    public ResponseEntity<UserDTO> updateUserRoles(@PathVariable Long userId, @RequestBody Map<String, List<String>> requestBody) {
        List<String> newRoleNames = requestBody.get("roles");
        if (newRoleNames == null || newRoleNames.isEmpty()) {
            throw new IllegalArgumentException("New roles list cannot be empty.");
        }
        UserDTO updatedUser = userService.updateUserRoles(userId, newRoleNames);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
}
