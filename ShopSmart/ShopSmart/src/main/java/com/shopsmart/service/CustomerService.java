package com.shopsmart.service;

import java.util.List;

import com.shopsmart.dto.CustomerDTO;
import com.shopsmart.dto.ProfileDTO;
import com.shopsmart.dto.UserDTO  ;

public interface CustomerService {
    CustomerDTO getCustomerById(Long customerId);
    List<CustomerDTO> getAllCustomers();
    CustomerDTO saveCustomer(CustomerDTO customerDto);
    CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO);
    void deleteCustomer(Long customerId);
    CustomerDTO getCustomerByEmail(String email);
    UserDTO getCustomerByUsername(String username);
    ProfileDTO getCustomerProfile(Long customerId);
    ProfileDTO createOrUpdateCustomerProfile(Long customerId, ProfileDTO profileDTO);
}
