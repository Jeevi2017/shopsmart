package com.shopsmart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "super_admin_user")
@PrimaryKeyJoinColumn(name = "user_id") // Link to app_user table via user_id
public class SuperAdmin extends User {

    public SuperAdmin() {
        super();
    }

    // Constructor with all fields including 2FA
    public SuperAdmin(Long id, String username, String password, String email, String phoneNumber,
                      LocalDateTime createdAt, LocalDateTime updatedAt, boolean active,
                      boolean is2faEnabled, String twoFactorCode, LocalDateTime twoFactorCodeExpiry,
                      Set<Role> roles) {
        super(id, username, password, email, phoneNumber, createdAt, updatedAt, active,
              is2faEnabled, twoFactorCode, twoFactorCodeExpiry, roles);
    }

    // Constructor without 2FA params
    public SuperAdmin(Long id, String username, String password, String email, String phoneNumber,
                      LocalDateTime createdAt, LocalDateTime updatedAt, boolean active, Set<Role> roles) {
        super(id, username, password, email, phoneNumber, createdAt, updatedAt, active,
              false, null, null, roles);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null) // FIX: Should be null check, not obj == obj
            return false;
        if (getClass() != obj.getClass())
            return false;
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "SuperAdmin [" + super.toString() + "]";
    }
}
