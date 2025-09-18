package com.shopsmart.serviceImpl;

import com.shopsmart.config.SecurityConstants;
import com.shopsmart.dto.AdminRegisterRequest;
import com.shopsmart.dto.UserDTO;
import com.shopsmart.entity.Role;
import com.shopsmart.entity.User;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.RoleRepository;
import com.shopsmart.repository.UserRepository;
import com.shopsmart.service.EmailService; // NEW: Import EmailService
import com.shopsmart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom; // NEW: Import SecureRandom for code generation
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

	private static final int TWO_FACTOR_CODE_LENGTH = 6;
    private static final int TWO_FACTOR_CODE_VALIDITY_MINUTES = 5; // Code valid for 5 minutes

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom(); // Corrected instantiation

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("DEBUG (UserServiceImpl): User not found for username: " + username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        return user;
    }

    @Override
    @Transactional
    public UserDTO registerAdmin(AdminRegisterRequest adminRegisterRequest) {
        if (userRepository.findByUsername(adminRegisterRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmail(adminRegisterRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User admin = new User();
        admin.setUsername(adminRegisterRequest.getUsername());
        admin.setEmail(adminRegisterRequest.getEmail());
        admin.setPassword(passwordEncoder.encode(adminRegisterRequest.getPassword()));
        admin.setPhoneNumber(null);
        admin.setActive(true);
        admin.setIs2faEnabled(false);

        Role adminRole = roleRepository.findByName(SecurityConstants.ROLE_ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", SecurityConstants.ROLE_ADMIN));
        admin.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        User savedAdmin = userRepository.save(admin);
        return mapUserToDTO(savedAdmin);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapUserToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setActive(userDTO.isActive());

        User updatedUser = userRepository.save(existingUser);
        return mapUserToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    @Override
    public UserDTO getUserByUserName(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapUserToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUserRoles(Long userId, List<String> newRoleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> newRoles = newRoleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);
        User updatedUser = userRepository.save(user);
        return mapUserToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO enable2FA(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIs2faEnabled(true);
        User updatedUser = userRepository.save(user);
        System.out.println("2FA enabled for user: " + user.getUsername());
        return mapUserToDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO disable2FA(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIs2faEnabled(false);
        user.setTwoFactorCode(null);
        user.setTwoFactorCodeExpiry(null);
        User updatedUser = userRepository.save(user);
        System.out.println("2FA disabled for user: " + user.getUsername());
        return mapUserToDTO(updatedUser);
    }

    @Override
    @Transactional
    public String generateAndSend2FACode(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!user.isIs2faEnabled()) {
            throw new IllegalStateException("2FA is not enabled for this user.");
        }

        String code = String.format("%0" + TWO_FACTOR_CODE_LENGTH + "d", secureRandom.nextInt((int) Math.pow(10, TWO_FACTOR_CODE_LENGTH)));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(TWO_FACTOR_CODE_VALIDITY_MINUTES);

        user.setTwoFactorCode(code);
        user.setTwoFactorCodeExpiry(expiryTime);
        userRepository.save(user);

        // FIX: Pass TWO_FACTOR_CODE_VALIDITY_MINUTES to send2faCode
        emailService.send2faCode(user.getEmail(), code, TWO_FACTOR_CODE_VALIDITY_MINUTES);
        System.out.println("Generated and sent 2FA code for user: " + username + ". Code: " + code);
        return code;
    }

    @Override
    @Transactional
    public boolean verify2FACode(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!user.isIs2faEnabled()) {
            System.out.println("2FA not enabled for user: " + username);
            return false;
        }

        if (user.getTwoFactorCode() == null || !user.getTwoFactorCode().equals(code)) {
            System.out.println("Invalid 2FA code for user: " + username);
            return false;
        }

        if (user.getTwoFactorCodeExpiry() == null || user.getTwoFactorCodeExpiry().isBefore(LocalDateTime.now())) {
            System.out.println("Expired 2FA code for user: " + username);
            user.setTwoFactorCode(null);
            user.setTwoFactorCodeExpiry(null);
            userRepository.save(user);
            return false;
        }

        user.setTwoFactorCode(null);
        user.setTwoFactorCodeExpiry(null);
        userRepository.save(user);
        System.out.println("2FA code verified successfully for user: " + username);
        return true;
    }

    private UserDTO mapUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setActive(user.isActive());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setIs2faEnabled(user.isIs2faEnabled());
        userDTO.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        return userDTO;
    }
}
