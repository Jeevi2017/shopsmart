package com.shopsmart.serviceImpl;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsmart.dto.AddressDTO;
import com.shopsmart.dto.ProfileDTO;
import com.shopsmart.entity.Address;
import com.shopsmart.entity.Customer;
import com.shopsmart.entity.Profile;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.CustomerRepository;
import com.shopsmart.repository.ProfileRepository;
import com.shopsmart.service.ProfileService;

@Service
public class ProfileServiceImpl implements ProfileService {

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Override
	@Transactional
	public ProfileDTO saveProfile(ProfileDTO profileDTO) {
		Profile profile = new Profile();

		if (profileDTO.getCustomerId() != null) {
			Customer customer = customerRepository.findById(profileDTO.getCustomerId())
					.orElseThrow(() -> new ResourceNotFoundException("Customer", "Id", profileDTO.getCustomerId()));
			profile.setCustomer(customer);
			customer.setProfile(profile);
		} else {
			throw new IllegalArgumentException("Customer ID is required to save a profile.");
		}

		profile.setFirstName(profileDTO.getFirstName());
		profile.setLastName(profileDTO.getLastName());
		profile.setPhoneNumber(profileDTO.getPhoneNumber());

		if (profileDTO.getAddresses() != null && !profileDTO.getAddresses().isEmpty()) {
			profileDTO.getAddresses().forEach(addressDto -> {
				Address address = new Address();

				address.setStreet(addressDto.getStreet());
				address.setCity(addressDto.getCity());
				address.setState(addressDto.getState());
				address.setCountry(addressDto.getCountry());
				address.setPostalCode(addressDto.getPostalCode());
				address.setType(addressDto.getType());
				profile.addAddress(address);
			});
		}

		Profile savedProfile = profileRepository.save(profile);

		return convertToProfileDTO(savedProfile);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProfileDTO> getAllProfiles() {
		return profileRepository.findAll().stream().map(this::convertToProfileDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ProfileDTO getProfileById(Long profileId) {
		Profile profile = profileRepository.findById(profileId)
				.orElseThrow(() -> new ResourceNotFoundException("Profile", "Id", profileId));
		return convertToProfileDTO(profile);
	}
	
	@Override
    @Transactional
    public ProfileDTO updateProfile(Long profileId, ProfileDTO profileDTO) {
        Profile existingProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "Id", profileId));

        existingProfile.setFirstName(profileDTO.getFirstName());
        existingProfile.setLastName(profileDTO.getLastName());
        existingProfile.setPhoneNumber(profileDTO.getPhoneNumber());

        existingProfile.getAddresses().clear();
        if (profileDTO.getAddresses() != null && !profileDTO.getAddresses().isEmpty()) {
            profileDTO.getAddresses().forEach(addressDTO -> {
                Address address = convertToAddressEntity(addressDTO);
                existingProfile.addAddress(address); 
            });
        }

        Profile updatedProfile = profileRepository.save(existingProfile);
        return convertToProfileDTO(updatedProfile);
    }

    @Override
    @Transactional
    public void deleteProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "Id", profileId));
        profileRepository.delete(profile);
    }

    private ProfileDTO convertToProfileDTO(Profile profile) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(profile.getId());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setPhoneNumber(profile.getPhoneNumber());
        dto.setCustomerId(profile.getCustomer() != null ? profile.getCustomer().getId() : null);

        if (profile.getAddresses() != null && !profile.getAddresses().isEmpty()) {
            dto.setAddresses(profile.getAddresses().stream()
                    .map(this::convertToAddressDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
    
	private AddressDTO convertToAddressDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setPostalCode(address.getPostalCode());
        dto.setType(address.getType());
        dto.setProfileId(address.getProfile() != null ? address.getProfile().getId() : null);
        return dto;
    }
    private Address convertToAddressEntity(AddressDTO dto) {
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

	

	
}