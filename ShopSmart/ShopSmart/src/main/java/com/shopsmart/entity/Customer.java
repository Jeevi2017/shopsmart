package com.shopsmart.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrimaryKeyJoinColumn;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "customer_user")
@PrimaryKeyJoinColumn(name = "user_id") // Links customer_user table to app_user table via user_id
public class Customer extends User {

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
    private Cart cart; 

    public Customer() {
        super();
    }

    // UPDATED: Constructor to correctly pass all User fields, including new 2FA fields
    public Customer(Long id, String username, String password, String email, String phoneNumber,
                    LocalDateTime createdAt, LocalDateTime updatedAt, boolean active,
                    boolean is2faEnabled, String twoFactorCode, LocalDateTime twoFactorCodeExpiry, // NEW 2FA params
                    Set<Role> roles) {
        super(id, username, password, email, phoneNumber, createdAt, updatedAt, active,
              is2faEnabled, twoFactorCode, twoFactorCodeExpiry, // Pass 2FA params to super
              roles);
    }

    // UPDATED: All-arguments constructor including specific Customer fields, and new 2FA fields
    public Customer(Long id, String username, String password, String email, String phoneNumber,
                    LocalDateTime createdAt, LocalDateTime updatedAt, boolean active,
                    boolean is2faEnabled, String twoFactorCode, LocalDateTime twoFactorCodeExpiry, // NEW 2FA params
                    Set<Role> roles, Profile profile, Cart cart) {
        super(id, username, password, email, phoneNumber, createdAt, updatedAt, active,
              is2faEnabled, twoFactorCode, twoFactorCodeExpiry, // Pass 2FA params to super
              roles);
        this.profile = profile;
        this.cart = cart;
    }

    // NEW: Add a constructor that matches the original base User constructor signature
    // This allows creating a Customer without explicitly providing 2FA details,
    // which will then default to false/null as per the User entity's defaults.
    public Customer(Long id, String username, String password, String email, String phoneNumber,
                    LocalDateTime createdAt, LocalDateTime updatedAt, boolean active, Set<Role> roles) {
        super(id, username, password, email, phoneNumber, createdAt, updatedAt, active,
              false, null, null, // Default values for 2FA
              roles);
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        if (profile != null && profile.getCustomer() != this) {
            profile.setCustomer(this);
        }
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
        if (cart != null && cart.getCustomer() != this) {
            cart.setCustomer(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((profile == null) ? 0 : profile.hashCode());
        result = prime * result + ((cart == null) ? 0 : cart.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Customer other = (Customer) obj;
        if (profile == null) {
            if (other.profile != null)
                return false;
        } else if (!profile.equals(other.profile))
            return false;
        if (cart == null) {
            if (other.cart != null)
                return false;
        } else if (!cart.equals(other.cart))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Customer [" + super.toString() + ", profileId=" + (profile != null ? profile.getId() : "null")
                + ", cartId=" + (cart != null ? cart.getId() : "null") + "]";
    }
}
