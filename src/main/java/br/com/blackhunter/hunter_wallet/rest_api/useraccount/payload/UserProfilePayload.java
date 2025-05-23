/*
 * @(#)UserProfilePayload.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload;

import br.com.blackhunter.hunter_wallet.rest_api.core.annotations.MinAdultAge;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfilePayload {
    @NotBlank(message = "The \"firstName\" field is mandatory.")
    @Min(value = 3, message = "The \"firstName\" field must have at least 3 characters")
    private String firstName;

    @NotBlank(message = "The \"lastName\" field is mandatory.")
    @Min(value = 3, message = "The \"firstName\" field must have at least 3 characters")
    private String lastName;

    @NotBlank(message = "The \"phoneNumber\" field is mandatory.")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Oops! Looks like that phone number isn't in the correct format. Make sure to include the country code, without spaces or special characters.")
    private String phoneNumber;

    @NotNull(message = "The \"birthDate\" field is mandatory.")
    @MinAdultAge
    private LocalDate birthDate;

    private MultipartFile profilePictureFile;
}
