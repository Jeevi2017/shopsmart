package com.shopsmart.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsmart.config.SecurityConstants;
import com.shopsmart.dto.AddressDTO;
import com.shopsmart.dto.CustomerDTO;
import com.shopsmart.dto.ProfileDTO;
import com.shopsmart.dto.ProductDTO;
import com.shopsmart.dto.UserDTO;
import com.shopsmart.entity.Address;
import com.shopsmart.entity.Cart;
import com.shopsmart.entity.CartItem;
import com.shopsmart.entity.Customer;
import com.shopsmart.entity.Product;
import com.shopsmart.entity.Profile;
import com.shopsmart.entity.Role;
import com.shopsmart.entity.User;
import com.shopsmart.exception.CustomerHasActiveOrdersException;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.CartItemRepository;
import com.shopsmart.repository.CustomerRepository;
import com.shopsmart.repository.OrderRepository;
import com.shopsmart.repository.RoleRepository;
import com.shopsmart.repository.UserRepository;
import com.shopsmart.service.CustomerService;
import com.shopsmart.service.RefreshTokenService;
import com.shopsmart.service.ProductService;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderRepository orderRepository;


	@Override
	@Transactional(readOnly = true)
	public CustomerDTO getCustomerById(Long customerId) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", "Id", customerId));

		return mapCustomerToDTO(customer);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomerDTO> getAllCustomers() {
		List<Customer> customers = customerRepository.findAll();
		return customers.stream().map(this::mapCustomerToDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public CustomerDTO saveCustomer(CustomerDTO customerDto) {
		UserDTO userDetails = customerDto.getUserDetails();

		if (userDetails == null) {
            throw new IllegalArgumentException("User details are required for customer registration.");
        }

        // 1. Uniqueness checks using UserRepository (covers all user types)
        if (userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        if (userDetails.getPhoneNumber() != null && userRepository.findByPhoneNumber(userDetails.getPhoneNumber()).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists.");
        }

        // 2. Create and populate Customer entity (which extends User)
        Customer customer = new Customer();
        customer.setUsername(userDetails.getUsername());
        customer.setEmail(userDetails.getEmail());
        // FIX: Correctly access password from UserDTO
        customer.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        customer.setPhoneNumber(userDetails.getPhoneNumber());
        customer.setActive(true);

        // Assign ROLE_CUSTOMER
        Role customerRole = roleRepository.findByName(SecurityConstants.ROLE_CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", SecurityConstants.ROLE_CUSTOMER));
        Set<Role> rolesSet = new HashSet<>();
        rolesSet.add(customerRole);
        customer.setRoles(rolesSet);

        // Save the customer (which also saves the User part due to inheritance)
        Customer savedCustomer = customerRepository.save(customer);

        // 3. Create and populate Profile entity
        ProfileDTO profileDetails = customerDto.getProfileDetails();
        if (profileDetails != null) {
            Profile profile = new Profile();
            profile.setFirstName(profileDetails.getFirstName());
            profile.setLastName(profileDetails.getLastName());
            profile.setPhoneNumber(profileDetails.getPhoneNumber());

            savedCustomer.setProfile(profile); // Link profile to customer (bidirectional)

            // 4. Handle Addresses
            if (profileDetails.getAddresses() != null && !profileDetails.getAddresses().isEmpty()) {
                profileDetails.getAddresses().forEach(addressDto -> {
                    Address address = mapAddressDTOToEntity(addressDto);
                    profile.addAddress(address);
                });
            }
        }
        // Save again to persist profile and addresses if they were added/updated
        Customer finalSavedCustomer = customerRepository.save(savedCustomer);

		return mapCustomerToDTO(finalSavedCustomer);
	}

    @Override
    @Transactional
    public CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "Id", customerId));

        UserDTO userDetailsDTO = customerDTO.getUserDetails();
        if (userDetailsDTO != null) {
            existingCustomer.setEmail(userDetailsDTO.getEmail());
            existingCustomer.setPhoneNumber(userDetailsDTO.getPhoneNumber());
            existingCustomer.setActive(userDetailsDTO.isActive());
        }

        ProfileDTO profileDetailsDTO = customerDTO.getProfileDetails();
        if (profileDetailsDTO != null) {
            Profile profileToUpdate = existingCustomer.getProfile();
            if (profileToUpdate == null) {
                profileToUpdate = new Profile();
                profileToUpdate.setCustomer(existingCustomer);
                existingCustomer.setProfile(profileToUpdate);
            }

            profileToUpdate.setFirstName(profileDetailsDTO.getFirstName());
            profileToUpdate.setLastName(profileDetailsDTO.getLastName());
            profileToUpdate.setPhoneNumber(profileDetailsDTO.getPhoneNumber());

            if (profileToUpdate.getAddresses() != null) {
                profileToUpdate.getAddresses().clear();
            } else {
                profileToUpdate.setAddresses(new ArrayList<>());
            }

            if (profileDetailsDTO.getAddresses() != null && !profileDetailsDTO.getAddresses().isEmpty()) {
                final Profile finalProfileToUpdate = profileToUpdate;
                profileDetailsDTO.getAddresses().forEach(addressDTO -> {
                    Address address = mapAddressDTOToEntity(addressDTO);
                    finalProfileToUpdate.addAddress(address);
                });
            }
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return mapCustomerToDTO(updatedCustomer);
    }

	@Override
	@Transactional
	public void deleteCustomer(Long customerId) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", "Id", customerId));

        if (orderRepository.countByCustomer_Id(customerId) > 0) {
            throw new CustomerHasActiveOrdersException("Customer has associated orders. Please ensure all orders are completed or cancelled before deletion.");
        }

        refreshTokenService.deleteByUserId(customer.getId());

        Optional<Cart> customerCart = cartRepository.findByCustomerId(customer.getId());
        if (customerCart.isPresent()) {
            Cart cart = customerCart.get();
            for (CartItem item : new ArrayList<>(cart.getCartItems())) {
                if (item.getProduct() != null && item.getProduct().getStockQuantity() != null) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    ProductDTO productDTOToUpdate = mapProductEntityToDTO(product);
                    productService.updateProduct(product.getId(), productDTOToUpdate);
                }
                cartItemRepository.delete(item);
            }
            cart.getCartItems().clear();
            cartRepository.delete(cart);
        }

        customerRepository.delete(customer);
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerDTO getCustomerByEmail(String email) {
		return customerRepository.findByEmail(email)
				.map(this::mapCustomerToDTO)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", "Email", email));
	}

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCustomerByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Username", username));
        return mapUserEntityToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getCustomerProfile(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "Id", customerId));

        Profile profile = customer.getProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Customer ID", customerId);
        }
        return mapProfileToDTO(profile);
    }

    @Override
    @Transactional
    public ProfileDTO createOrUpdateCustomerProfile(Long customerId, ProfileDTO profileDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "Id", customerId));

        Profile profile;
        if (customer.getProfile() == null) {
            profile = new Profile();
            profile.setCustomer(customer);
            customer.setProfile(profile);
        } else {
            profile = customer.getProfile();
        }

        profile.setFirstName(profileDTO.getFirstName());
        profile.setLastName(profileDTO.getLastName());
        profile.setPhoneNumber(profileDTO.getPhoneNumber());

        if (profile.getAddresses() != null) {
            profile.getAddresses().clear();
        } else {
            profile.setAddresses(new ArrayList<>());
        }

        if (profileDTO.getAddresses() != null && !profileDTO.getAddresses().isEmpty()) {
            final Profile finalProfile = profile;
            profileDTO.getAddresses().forEach(addressDTO -> {
                Address address = mapAddressDTOToEntity(addressDTO);
                finalProfile.addAddress(address);
            });
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapProfileToDTO(updatedCustomer.getProfile());
    }

    // --- Helper Mapping Methods ---

    private CustomerDTO mapCustomerToDTO(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId());

        customerDTO.setUserDetails(mapUserEntityToDTO(customer));

        if (customer.getProfile() != null) {
            customerDTO.setProfileDetails(mapProfileToDTO(customer.getProfile()));
        }
        return customerDTO;
    }

    private UserDTO mapUserEntityToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setActive(user.isActive());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        return userDTO;
    }

    private ProfileDTO mapProfileToDTO(Profile profile) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(profile.getId());
        profileDTO.setFirstName(profile.getFirstName());
        profileDTO.setLastName(profile.getLastName());
        profileDTO.setPhoneNumber(profile.getPhoneNumber());
        profileDTO.setCustomerId(profile.getCustomer() != null ? profile.getCustomer().getId() : null);

        if (profile.getAddresses() != null && !profile.getAddresses().isEmpty()) {
            profileDTO.setAddresses(profile.getAddresses().stream()
                    .map(this::mapAddressToDTO)
                    .collect(Collectors.toList()));
        } else {
            profileDTO.setAddresses(Collections.emptyList());
        }
        return profileDTO;
    }

    private AddressDTO mapAddressToDTO(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(address.getId());
        addressDTO.setStreet(address.getStreet());
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setCountry(address.getCountry());
        addressDTO.setPostalCode(address.getPostalCode());
        addressDTO.setType(address.getType());
        addressDTO.setProfileId(address.getProfile() != null ? address.getProfile().getId() : null);
        return addressDTO;
    }

    private Address mapAddressDTOToEntity(AddressDTO dto) {
        Address address = new Address();
        address.setId(dto.getId());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setPostalCode(dto.getPostalCode());
        address.setType(dto.getType());
        return address;
    }

    private ProductDTO mapProductEntityToDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setImages(product.getImages());
        productDTO.setPrice(product.getPrice());
        productDTO.setStockQuantity(product.getStockQuantity());
        if (product.getCategory() != null) {
            productDTO.setCategoryId(product.getCategory().getId());
            productDTO.setCategoryName(product.getCategory().getName());
        }
        return productDTO;
    }
}
