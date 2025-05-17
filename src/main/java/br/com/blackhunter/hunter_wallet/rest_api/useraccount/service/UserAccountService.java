package br.com.blackhunter.hunter_wallet.rest_api.useraccount.service;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserAccountData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserAccountPayload;

import java.util.UUID;

public interface UserAccountService {
    /* Serviços expostos como endpoints */
    UserAccountData registerUser(UserAccountPayload reqPayload);
    UserAccountData authenticateUser(Object loginDTO);
    Object getUserProfile(UUID userId);
    Object updateUserProfile(UUID userId, Object updateDTO);

    /* Não expostos como endpoint (usados internamente) */
    UserAccountData findById(UUID userId);
    void verifyEmail(String verificationToken);
    void initiatePasswordReset(String email);
    void completePasswordReset(String token, String newPassword);
    void deactivateAccount(UUID userId);
}
