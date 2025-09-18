package com.shopsmart.dto;

import jakarta.validation.constraints.NotBlank;

public class TwoFactorAuthRequestDTO {


    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "2FA code is required")
    private String twoFactorCode;

    public TwoFactorAuthRequestDTO() {
    }

    public TwoFactorAuthRequestDTO(String username, String twoFactorCode) {
        this.username = username;
        this.twoFactorCode = twoFactorCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTwoFactorCode() {
        return twoFactorCode;
    }

    public void setTwoFactorCode(String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }

}
