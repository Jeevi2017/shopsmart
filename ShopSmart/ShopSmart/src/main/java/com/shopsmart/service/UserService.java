package com.shopsmart.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.shopsmart.dto.UserDTO;
import com.shopsmart.dto.AdminRegisterRequest;

import java.util.List;

public interface UserService extends UserDetailsService {

    UserDTO registerAdmin(AdminRegisterRequest adminRegisterRequest);

    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    UserDTO getUserByUserName(String username);

    UserDTO updateUserRoles(Long userId, List<String> newRoleNames);

   

    
    UserDTO enable2FA(Long userId);

    
    UserDTO disable2FA(Long userId);

    String generateAndSend2FACode(String username);

   
    boolean verify2FACode(String username, String code);
}
