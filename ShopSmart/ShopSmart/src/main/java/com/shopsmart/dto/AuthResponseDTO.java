package com.shopsmart.dto;

public class AuthResponseDTO {

    private String token; 
    private String refreshToken; 
    private String message; 

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String refreshToken, String message) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.message = message;
    }

    public AuthResponseDTO(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.message = "Login successful!"; 
    }

    public AuthResponseDTO(String token) {
        this.token = token;
        this.message = "Login successful!"; 
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
