package com.shopsmart.dto;

public class AuthRequestDTO {

	private String username; // FIX: Changed from userName to username

	private String password;

	public String getUsername() { // FIX: Changed from getUserName to getUsername
		return username;
	}

	public void setUsername(String username) { // FIX: Changed from setUserName to setUsername
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AuthRequestDTO() {
	}

	public AuthRequestDTO(String username, String password) { // FIX: Changed from userName to username in constructor
		this.username = username;
		this.password = password;
	}

}
