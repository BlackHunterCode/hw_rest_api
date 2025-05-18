package br.com.blackhunter.hunter_wallet.rest_api.useraccount.service.impl;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserProfileData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserProfileEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserProfilePayload;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository.UserProfileRepository;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;
import java.util.UUID;

public class UserProfileServiceImpl implements UserProfileService {
    private final UserProfileRepository profileRepository;

    public UserProfileServiceImpl(
            UserProfileRepository profileRepository
    ) {
        this.profileRepository = profileRepository;
    }

    @Override
    public UserProfileData createProfile(UserAccountEntity userAccount, UserProfilePayload profilePayload) {
        return null;
    }

    @Override
    public UserProfileData updateProfile(UUID profileId, UserProfilePayload profilePayload) {
        return null;
    }

    @Override
    public UserProfileEntity findByUserAccountId(UUID userId) {
        return profileRepository.findByAccountId(userId).orElseThrow(
            () -> new EntityNotFoundException("The user profile provided does not exist.")
        );
    }
}
