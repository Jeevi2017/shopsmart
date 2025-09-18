package com.shopsmart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects; // NEW: Import Objects for equals/hashCode

@Entity
@Table(name = "address")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "street", nullable = false)
	private String street;
	@Column(name = "city", nullable = false)
	private String city;
	@Column(name = "state", nullable = false)
	private String state;
	@Column(name = "country", nullable = false)
	private String country;
	@Column(name = "postal_code", nullable = false)
	private String postalCode;
	@Column(name = "type", nullable = false)
	private String type;

	@ManyToOne
	@JoinColumn(name = "profile_id", nullable = false)
	private Profile profile;

	public Address() {
	}

	// NEW: All-arguments constructor
	public Address(Long id, String street, String city, String state, String country, String postalCode, String type, Profile profile) {
		this.id = id;
		this.street = street;
		this.city = city;
		this.state = state;
		this.country = country;
		this.postalCode = postalCode;
		this.type = type;
		this.profile = profile;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	// NEW: Override equals() and hashCode() for proper JPA entity behavior
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Address address = (Address) o;
		// Use ID for equality if it's already persisted, otherwise use natural keys.
		return Objects.equals(id, address.id) &&
			   Objects.equals(street, address.street) &&
			   Objects.equals(city, address.city) &&
			   Objects.equals(state, address.state) &&
			   Objects.equals(country, address.country) &&
			   Objects.equals(postalCode, address.postalCode) &&
			   Objects.equals(type, address.type) &&
			   Objects.equals(profile != null ? profile.getId() : null, address.profile != null ? address.profile.getId() : null);
	}

	@Override
	public int hashCode() {
		// Use ID for hashCode if it's already persisted, otherwise use natural keys.
		return Objects.hash(id, street, city, state, country, postalCode, type, profile != null ? profile.getId() : null);
	}

	// NEW: Override toString() for better logging and debugging
	@Override
	public String toString() {
		return "Address{" +
			   "id=" + id +
			   ", street='" + street + '\'' +
			   ", city='" + city + '\'' +
			   ", state='" + state + '\'' +
			   ", country='" + country + '\'' +
			   ", postalCode='" + postalCode + '\'' +
			   ", type='" + type + '\'' +
			   ", profileId=" + (profile != null ? profile.getId() : "null") +
			   '}';
	}
}
