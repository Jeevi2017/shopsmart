	package com.shopsmart.dto;
	
	import java.util.List;
	
	public class ProfileDTO {
	
	    private Long id;		
	    private String firstName;
	    private String lastName;
	    private String phoneNumber;
	    private Long customerId; 
	    private List<AddressDTO> addresses; 
	
	    public ProfileDTO() {
	    }
	
	    public ProfileDTO(Long id, String firstName, String lastName, String phoneNumber, Long customerId, List<AddressDTO> addresses) {
	        this.id = id;
	        this.firstName = firstName;
	        this.lastName = lastName;
	        this.phoneNumber = phoneNumber;
	        this.customerId = customerId;
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
	
	    public Long getCustomerId() {
	        return customerId;
	    }
	
	    public void setCustomerId(Long customerId) {
	        this.customerId = customerId;
	    }
	
	    public List<AddressDTO> getAddresses() {
	        return addresses;
	    }
	
	    public void setAddresses(List<AddressDTO> addresses) {
	        this.addresses = addresses;
	    }
	}
