package com.shopsmart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopsmart.config.SecurityConstants;
import com.shopsmart.dto.AddressDTO;
import com.shopsmart.dto.CustomerDTO;
import com.shopsmart.dto.ProfileDTO;
import com.shopsmart.dto.UserDTO;
import com.shopsmart.exception.CustomerHasActiveOrdersException;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.service.AddressService;
import com.shopsmart.service.CustomerService;
import com.shopsmart.service.ProfileService;
import com.shopsmart.service.UserService;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProfileService profileService;

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
    public Long getCustomerByProfileId(Long profileId) {
        ProfileDTO profileDTO = profileService.getProfileById(profileId);
        if (profileDTO != null) {
            return profileDTO.getCustomerId();
        }
        throw new ResourceNotFoundException("Profile", "Id", profileId);
    }


    @GetMapping
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    @PostMapping("/register")
    // No @PreAuthorize here, as this is a public endpoint for new customer registration
    public ResponseEntity<CustomerDTO> registerCustomer(@Validated @RequestBody CustomerDTO customerDto) {
        CustomerDTO savedCustomerDTO = customerService.saveCustomer(customerDto);
        return new ResponseEntity<>(savedCustomerDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #customerId == @customerController.getAuthenticatedCustomerId()")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long customerId) {
        CustomerDTO customerDTO = customerService.getCustomerById(customerId);
        return new ResponseEntity<>(customerDTO, HttpStatus.OK);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #username == authentication.name")
    public ResponseEntity<UserDTO> getCustomerByUsername(@PathVariable String username) {
        UserDTO userDTO = customerService.getCustomerByUsername(username);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #customerId == @customerController.getAuthenticatedCustomerId()")
    public ResponseEntity<CustomerDTO> updateCustomer(
                @PathVariable Long customerId,
                @Validated @RequestBody CustomerDTO customerDTO) {
        CustomerDTO updatedCustomer = customerService.updateCustomer(customerId, customerDTO);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{customerId}/profile")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #customerId == @customerController.getAuthenticatedCustomerId()")
    public ResponseEntity<ProfileDTO> getCustomerProfile(@PathVariable Long customerId) {
        ProfileDTO profileDTO = customerService.getCustomerProfile(customerId);
        return new ResponseEntity<>(profileDTO, HttpStatus.OK);
    }

    @PostMapping("/{customerId}/profile")
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or #customerId == @customerController.getAuthenticatedCustomerId()")
    public ResponseEntity<ProfileDTO> createOrUpdateCustomerProfile(
            @PathVariable Long customerId,
            @Validated @RequestBody ProfileDTO profileDTO) {
        ProfileDTO savedProfile = customerService.createOrUpdateCustomerProfile(customerId, profileDTO);
        return new ResponseEntity<>(savedProfile, HttpStatus.CREATED);
    }

    @ExceptionHandler(CustomerHasActiveOrdersException.class)
    public ResponseEntity<String> handleCustomerHasActiveOrdersException(CustomerHasActiveOrdersException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
    }
}
