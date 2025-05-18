package br.com.blackhunter.hunter_wallet.rest_api.useraccount.service;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserProfileData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserProfileEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserProfilePayload;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileService {
    UserProfileData createProfile(UserAccountEntity userAccount, UserProfilePayload profilePayload);
    UserProfileData updateProfile(UUID profileId, UserProfilePayload profilePayload);
    UserProfileEntity findByUserAccountId(UUID userId);
}
