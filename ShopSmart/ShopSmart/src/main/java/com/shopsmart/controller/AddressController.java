package com.shopsmart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Explicitly import AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Keep for internal checks if needed, but mostly covered by @PreAuthorize
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

import com.shopsmart.config.SecurityConstants; // Import for role constants
import com.shopsmart.dto.AddressDTO; // Import for AddressDTO
import com.shopsmart.dto.CustomerDTO; // Kept for potential future use or if needed by other methods
import com.shopsmart.dto.ProfileDTO; // Import for ProfileDTO
import com.shopsmart.dto.UserDTO; // Import for UserDTO
import com.shopsmart.exception.ResourceNotFoundException; // Import for ResourceNotFoundException
import com.shopsmart.service.AddressService; // Import for AddressService
import com.shopsmart.service.CustomerService; // Import for CustomerService
import com.shopsmart.service.ProfileService; // Import for ProfileService
import com.shopsmart.service.UserService; // Import for UserService

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "http://localhost:4200")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    // Helper method to get the authenticated user's ID (who is a Customer)
    // This method assumes the authenticated user is a Customer for profile-related operations.
    // If an Admin is authenticated, this might return their User ID, which is fine for Admin checks.
    public Long getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUsername = authentication.getName();
        UserDTO userDTO = userService.getUserByUserName(authenticatedUsername);
        return userDTO.getId();
    }

    // Helper method to get the customer ID associated with a given address ID
    // Used in @PreAuthorize to check if the authenticated user owns the address
    public Long getCustomerIdByAddressId(Long addressId) {
        AddressDTO addressDTO = addressService.getAddressById(addressId);
        if (addressDTO != null && addressDTO.getProfileId() != null) {
            ProfileDTO profileDTO = profileService.getProfileById(addressDTO.getProfileId());
            if (profileDTO != null && profileDTO.getCustomerId() != null) {
                return profileDTO.getCustomerId();
            }
        }
        // If address or its associated profile/customer is not found, treat as not authorized
        // This will lead to AccessDeniedException if @PreAuthorize fails, or ResourceNotFoundException if accessed directly.
        throw new ResourceNotFoundException("Address or associated Profile", "Id", addressId);
    }

    // Helper method to get the profile ID for the authenticated customer
    // Used in @PreAuthorize to check if the authenticated user's profile matches the target profile
    public Long getProfileIdForAuthenticatedCustomer() {
        Long authenticatedCustomerId = getAuthenticatedCustomerId();
        ProfileDTO profileDTO = customerService.getCustomerProfile(authenticatedCustomerId);
        if (profileDTO != null) {
            return profileDTO.getId();
        }
        // If profile not found for authenticated customer, treat as not authorized
        throw new ResourceNotFoundException("Profile", "Customer ID", authenticatedCustomerId);
    }

    @PostMapping
    // Allows ADMIN/SUPER_ADMIN or a CUSTOMER to create an address for their OWN profile.
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #addressDTO.profileId == @addressController.getProfileIdForAuthenticatedCustomer()")
    public ResponseEntity<AddressDTO> createAddress(@Validated @RequestBody AddressDTO addressDTO) {
        // The @PreAuthorize already handles the authorization logic.
        // No need for redundant manual checks inside the method.
        AddressDTO savedAddress = addressService.saveAddress(addressDTO);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    // Allows ADMIN/SUPER_ADMIN or a CUSTOMER to get their OWN address by ID.
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or @addressController.getAuthenticatedCustomerId() == @addressController.getCustomerIdByAddressId(#id)")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        AddressDTO address = addressService.getAddressById(id);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @GetMapping("/profile/{profileId}")
    // Allows ADMIN/SUPER_ADMIN or a CUSTOMER to get addresses for their OWN profile.
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #profileId == @addressController.getProfileIdForAuthenticatedCustomer()")
    public ResponseEntity<List<AddressDTO>> getAddressesByProfileId(@PathVariable Long profileId) {
        // The @PreAuthorize already handles the authorization logic.
        // No need for redundant manual checks inside the method.
        List<AddressDTO> addresses = addressService.getAddressesByProfileId(profileId);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    // Allows ADMIN/SUPER_ADMIN or a CUSTOMER to delete their OWN address by ID.
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or @addressController.getAuthenticatedCustomerId() == @addressController.getCustomerIdByAddressId(#id)")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or @addressController.getAuthenticatedCustomerId() == @addressController.getCustomerIdByAddressId(#id)")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id, @Validated @RequestBody AddressDTO addressDTO) {
        Long existingProfileId = addressService.getAddressById(id).getProfileId();
        if (addressDTO.getProfileId() != null && !addressDTO.getProfileId().equals(existingProfileId)) {
            throw new AccessDeniedException("Cannot change the profile association of an existing address.");
        }
        AddressDTO updatedAddress = addressService.updateAddress(id, addressDTO);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }
}
