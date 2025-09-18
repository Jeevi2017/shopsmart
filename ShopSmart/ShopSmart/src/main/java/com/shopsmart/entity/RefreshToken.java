package com.shopsmart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects; // NEW: Import Objects for equals/hashCode

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    public RefreshToken() {
    }

    // NEW: All-arguments constructor including all fields
    public RefreshToken(Long id, String token, User user, Instant expiryDate, boolean revoked) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    // NEW: Override equals() and hashCode() for proper JPA entity behavior
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        // For RefreshToken, the 'token' itself is a unique identifier, or the ID.
        // Using ID for persisted entities is generally safer.
        return Objects.equals(id, that.id) &&
               Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        // Use ID and token for hashCode.
        return Objects.hash(id, token);
    }

    // NEW: Override toString() for better logging and debugging
    @Override
    public String toString() {
        return "RefreshToken{" +
               "id=" + id +
               ", token='" + token.substring(0, Math.min(token.length(), 20)) + "...'" + // Truncate token for logging
               ", userId=" + (user != null ? user.getId() : "null") +
               ", expiryDate=" + expiryDate +
               ", revoked=" + revoked +
               '}';
    }
}
