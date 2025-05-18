package br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfilePayload {
    @NotBlank(message = "The \"firstName\" field is mandatory.")
    private String firstName;

    @NotBlank(message = "The \"lastName\" field is mandatory.")
    private String lastName;

    @NotBlank(message = "The \"phoneNumber\" field is mandatory.")
    private String phoneNumber;

    @NotNull(message = "The \"birthDate\" field is mandatory.")
    private LocalDate birthDate;

    private MultipartFile profilePictureFile;
}
