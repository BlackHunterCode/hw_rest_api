package br.com.blackhunter.hunter_wallet.rest_api.auth.controller;

import br.com.blackhunter.hunter_wallet.rest_api.auth.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>AuthenticationControllerTest</code>.</p>
 * Testes unitários para o controlador de autenticação.
 */
@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Deve autenticar o usuário e retornar o token JWT")
    void authenticate_ShouldReturnJwtToken() throws Exception {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.1234567890";
        when(authenticationService.authenticate(any(Authentication.class))).thenReturn(expectedToken);

        // Act & Assert
        mockMvc.perform(post("/v1/public/auth")
                .with(csrf())
                .with(user("test@example.com").password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));
    }

    @Test
    @DisplayName("Deve tratar falha de autenticação")
    void authenticate_WhenAuthenticationFails_ShouldReturnError() throws Exception {
        // Arrange
        when(authenticationService.authenticate(any(Authentication.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act & Assert
        mockMvc.perform(post("/v1/public/auth")
                .with(csrf())
                .with(user("test@example.com").password("password").roles("USER")))
                .andExpect(status().isInternalServerError());
    }
}
