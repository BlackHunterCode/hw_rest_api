package br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileData {
    private UUID userId;
    private UUID profileId;
    private String profilePictureUrl;
    private String fullName;
    private String firstName;
    private String lastName;
    private Long age;
}
