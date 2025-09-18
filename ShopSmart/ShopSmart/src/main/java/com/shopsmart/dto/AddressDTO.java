package com.shopsmart.dto;

public class AddressDTO {

	private Long id;
	private String street;
	private String city;
	private String state;
	private String country;
	private String postalCode;
	private String type;
	private Long profileId; // Represents the ID of the associated Profile

	public AddressDTO() {
	}

	// NEW: All-arguments constructor for convenience
	public AddressDTO(Long id, String street, String city, String state, String country, String postalCode, String type, Long profileId) {
		this.id = id;
		this.street = street;
		this.city = city;
		this.state = state;
		this.country = country;
		this.postalCode = postalCode;
		this.type = type;
		this.profileId = profileId;
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

	public Long getProfileId() {
		return profileId;
	}

	public void setProfileId(Long profileId) { // Changed parameter name for clarity
		this.profileId = profileId;
	}
}
