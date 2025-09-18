package com.shopsmart.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; 

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;


    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "user_id", unique = true, nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    public Profile() {
    }

    
    public Profile(Long id, String firstName, String lastName, String phoneNumber, Customer customer, List<Address> addresses) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.customer = customer;
        this.addresses = addresses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        // Ensure bidirectional relationship is maintained
        if (customer != null && customer.getProfile() != this) {
            customer.setProfile(this);
        }
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(Address address) {
        this.addresses.add(address);
        address.setProfile(this);
    }

    public void removeAddress(Address address) {
        this.addresses.remove(address);
        address.setProfile(null); // Break the link
    }

    // NEW: Override equals() and hashCode() for proper JPA entity behavior
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        // Use ID for equality if it's already persisted, otherwise use natural keys.
        // For entities, it's common to rely on ID once persisted.
        return Objects.equals(id, profile.id) &&
               Objects.equals(firstName, profile.firstName) &&
               Objects.equals(lastName, profile.lastName) &&
               Objects.equals(phoneNumber, profile.phoneNumber) &&
               Objects.equals(customer != null ? customer.getId() : null, profile.customer != null ? profile.customer.getId() : null);
    }

    @Override
    public int hashCode() {
        // Use ID for hashCode if it's already persisted, otherwise use natural keys.
        return Objects.hash(id, firstName, lastName, phoneNumber, customer != null ? customer.getId() : null);
    }

    // NEW: Override toString() for better logging and debugging
    @Override
    public String toString() {
        return "Profile{" +
               "id=" + id +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", customerId=" + (customer != null ? customer.getId() : "null") +
               ", addressesCount=" + (addresses != null ? addresses.size() : 0) +
               '}';
    }
}
