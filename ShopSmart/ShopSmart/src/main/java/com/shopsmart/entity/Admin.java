package com.shopsmart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.PrimaryKeyJoinColumn;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "admin_user")
@PrimaryKeyJoinColumn(name = "user_id") // Links admin_user table to app_user table via user_id
public class Admin extends User {

    public Admin() {
        super();
    }

    // UPDATED: Constructor to correctly pass all User fields, including new 2FA fields
    public Admin(Long id, String username, String password, String email, String phoneNumber,
                 LocalDateTime createdAt, LocalDateTime updatedAt, boolean active,
                 boolean is2faEnabled, String twoFactorCode, LocalDateTime twoFactorCodeExpiry, // NEW 2FA params
                 Set<Role> roles) {
        super(id, username, password, email, phoneNumber, createdAt, updatedAt, active,
              is2faEnabled, twoFactorCode, twoFactorCodeExpiry, // Pass 2FA params to super
              roles);
    }

  
    public Admin(Long id, String username, String password, String email, String phoneNumber,
                 LocalDateTime createdAt, LocalDateTime updatedAt, boolean active, Set<Role> roles) {
        super(id, username, password, email, phoneNumber, createdAt, updatedAt, active,
              false, null, null, // Default values for 2FA
              roles);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == obj) // Corrected from obj == null to obj == obj (typo fix)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "Admin [" + super.toString() + "]";
    }
}
