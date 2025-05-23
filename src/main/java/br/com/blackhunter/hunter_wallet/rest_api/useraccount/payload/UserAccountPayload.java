/*
 * @(#)UserAccountPayload.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>Classe <code>UserAccountPayload</code>.</p>
 * <p>Classe payload utilizada APENAS em requisições da API REST
 * de contas de usuários.</p>
 *
 * <p>Essa classe utiliza anotações de validações.</p>
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountPayload {
    @NotBlank(message = "The \"fullName\" field is mandatory.")
    private String fullName;
    @NotBlank(message = "The \"email\" field is mandatory.")
    @Email(message = "the email provided must be valid.")
    private String email;
    @NotBlank(message = "The \"password\" field is mandatory.")
    private String hashedPassword;
    @NotBlank(message = "The \"username\" field is mandatory.")
    private String username;
}
