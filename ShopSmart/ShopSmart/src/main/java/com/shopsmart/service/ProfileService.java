package com.shopsmart.service;

import java.util.List;

import com.shopsmart.dto.ProfileDTO;

public interface ProfileService {

	ProfileDTO saveProfile(ProfileDTO profileDTO);

	List<ProfileDTO> getAllProfiles();
	ProfileDTO updateProfile(Long profileId, ProfileDTO profileDTO);
	void deleteProfile(Long profileId);

	ProfileDTO getProfileById(Long profileId);

	





}
