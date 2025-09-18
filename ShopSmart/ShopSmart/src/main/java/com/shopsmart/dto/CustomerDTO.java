package com.shopsmart.dto;

import java.util.List;


public class CustomerDTO {

    private Long id;
    private UserDTO userDetails;
    private ProfileDTO profileDetails;

    public CustomerDTO() {
    }

    public CustomerDTO(Long id, UserDTO userDetails, ProfileDTO profileDetails) {
        this.id = id;
        this.userDetails = userDetails;
        this.profileDetails = profileDetails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDTO userDetails) {
        this.userDetails = userDetails;
    }

    public ProfileDTO getProfileDetails() {
        return profileDetails;
    }

    public void setProfileDetails(ProfileDTO profileDetails) {
        this.profileDetails = profileDetails;
    }
}
