package com.shopsmart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopsmart.config.SecurityConstants;
import com.shopsmart.dto.CustomerDTO;
import com.shopsmart.dto.ProfileDTO;
import com.shopsmart.dto.UserDTO;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.service.CustomerService;
import com.shopsmart.service.ProfileService;
import com.shopsmart.service.UserService;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "http://localhost:4200")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    // Helper method to get the authenticated customer's ID (which is the User ID)
    public Long getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUsername = authentication.getName();
        UserDTO userDTO = userService.getUserByUserName(authenticatedUsername);
        return userDTO.getId();
    }

    // Helper method to get the customer ID associated with a given profile ID
    public Long getCustomerIdByProfileId(Long profileId) {
        ProfileDTO profileDTO = profileService.getProfileById(profileId);
        if (profileDTO != null && profileDTO.getCustomerId() != null) {
            return profileDTO.getCustomerId();
        }
        throw new ResourceNotFoundException("Profile", "Id", profileId);
    }


    @PostMapping
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #profileDTO.customerId == @profileController.getAuthenticatedCustomerId()")
    public ResponseEntity<ProfileDTO> saveProfile(@Validated @RequestBody ProfileDTO profileDTO) {
        // The @PreAuthorize annotation already handles the primary authorization check.
        // This internal check is a good defensive programming practice for complex logic,
        // but for simple access control, @PreAuthorize is often sufficient.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrSuperAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority(SecurityConstants.ROLE_ADMIN)) ||
                                       authentication.getAuthorities().contains(new SimpleGrantedAuthority(SecurityConstants.ROLE_SUPER_ADMIN));

        if (!isAdminOrSuperAdmin) {
            if (profileDTO.getCustomerId() == null || !profileDTO.getCustomerId().equals(getAuthenticatedCustomerId())) {
                throw new org.springframework.security.access.AccessDeniedException("Customers can only create profiles for their own account.");
            }
        }
        ProfileDTO savedProfile = profileService.saveProfile(profileDTO);
        return new ResponseEntity<>(savedProfile, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<List<ProfileDTO>> getAllProfiles() {
        List<ProfileDTO> profiles = profileService.getAllProfiles();
        return new ResponseEntity<>(profiles, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or @profileController.getAuthenticatedCustomerId() == @profileController.getCustomerIdByProfileId(#id)")
    public ResponseEntity<ProfileDTO> getProfileById(@PathVariable Long id) {
        ProfileDTO profile = profileService.getProfileById(id);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or @profileController.getAuthenticatedCustomerId() == @profileController.getCustomerIdByProfileId(#id)")
    public ResponseEntity<ProfileDTO> updateProfile(@PathVariable Long id,
            @Validated @RequestBody ProfileDTO profileDTO) {

        // Prevent changing customer ID of an existing profile - this is a valid business rule.
        if (profileDTO.getCustomerId() != null && !profileDTO.getCustomerId().equals(getCustomerIdByProfileId(id))) {
               throw new org.springframework.security.access.AccessDeniedException("Cannot change customer ID of an existing profile.");
        }

        ProfileDTO updatedProfile = profileService.updateProfile(id, profileDTO);
        return new ResponseEntity<>(updatedProfile, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
