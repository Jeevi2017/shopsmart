package com.shopsmart.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsmart.dto.AddressDTO;
import com.shopsmart.entity.Address;
import com.shopsmart.entity.Profile;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.AddressRepository;
import com.shopsmart.repository.ProfileRepository;
import com.shopsmart.service.AddressService;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Override
	@Transactional
	public AddressDTO saveAddress(AddressDTO addressDTO) {
		Address address = dtoToEntity(addressDTO);
		Address savedAddress = addressRepository.save(address);
		return entityToDto(savedAddress);
	}

	@Override
	public AddressDTO getAddressById(Long id) {
		Address address = addressRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));
		return entityToDto(address);
	}

	@Override
	public List<AddressDTO> getAddressesByProfileId(Long profileId) {
		List<Address> addresses = addressRepository.findByProfileId(profileId);
		return addresses.stream().map(this::entityToDto).collect(Collectors.toList());
	}


	@Override
	@Transactional
	public void deleteAddress(Long id) {
		if (!addressRepository.existsById(id)) {
			throw new ResourceNotFoundException("Address", "id", id);
		}
		addressRepository.deleteById(id);
	}

	@Override
	@Transactional
	public AddressDTO updateAddress(Long id, AddressDTO addressDTO) {
		Address existingAddress = addressRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

		BeanUtils.copyProperties(addressDTO, existingAddress, "id", "profileId");

		if (addressDTO.getProfileId() != null) {

			if (existingAddress.getProfile() == null
					|| !existingAddress.getProfile().getId().equals(addressDTO.getProfileId())) {
				Profile newProfile = profileRepository.findById(addressDTO.getProfileId())
						.orElseThrow(() -> new ResourceNotFoundException("Profile", "id", addressDTO.getProfileId()));
				existingAddress.setProfile(newProfile);
			}
		} else {

			throw new IllegalArgumentException(
					"Profile ID cannot be null for updating an address, as it's a required field.");
		}

		Address updatedAddress = addressRepository.save(existingAddress);
		return entityToDto(updatedAddress);
	}

	private Address dtoToEntity(AddressDTO dto) {
		Address address = new Address();
		BeanUtils.copyProperties(dto, address, "profileId");

		if (dto.getProfileId() == null) {
			throw new IllegalArgumentException("Profile ID is required for an Address.");
		}
		Profile profile = profileRepository.findById(dto.getProfileId())
				.orElseThrow(() -> new ResourceNotFoundException("Profile", "id", dto.getProfileId()));
		address.setProfile(profile);

		return address;
	}

	private AddressDTO entityToDto(Address address) {
		AddressDTO dto = new AddressDTO();
		BeanUtils.copyProperties(address, dto);

		if (address.getProfile() != null) {
			dto.setProfileId(address.getProfile().getId());
		}
		return dto;
	}
}
