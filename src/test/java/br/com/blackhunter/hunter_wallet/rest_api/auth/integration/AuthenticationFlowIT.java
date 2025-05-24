package br.com.blackhunter.hunter_wallet.rest_api.auth.integration;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>AuthenticationFlowIT</code>.</p>
 * Testes de integração para o fluxo completo de autenticação.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "integration-test@example.com";
    private static final String TEST_PASSWORD = "Test@123456";
    private UserAccountEntity testUser;

    @BeforeEach
    void setUp() {
        // Create a test user for authentication
        testUser = new UserAccountEntity();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setAccountStatus(UserAccountStatus.ACTIVE);
        
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        userRepository.findByEmail(TEST_EMAIL)
            .ifPresent(user -> userRepository.delete(user));
    }

    @Test
    @DisplayName("Deve autenticar com credenciais válidas e retornar token JWT")
    void authenticate_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/v1/public/auth")
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String token = result.getResponse().getContentAsString();
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token structure (JWT tokens have 3 parts separated by dots)
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);
    }

    @Test
    @DisplayName("Deve rejeitar autenticação com credenciais inválidas")
    void authenticate_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/public/auth")
                .with(httpBasic(TEST_EMAIL, "wrong-password"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve rejeitar autenticação para usuário inativo")
    void authenticate_WithInactiveUser_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        testUser.setAccountStatus(UserAccountStatus.INACTIVE);
        userRepository.save(testUser);

        // Act & Assert
        mockMvc.perform(post("/v1/public/auth")
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve rejeitar autenticação para usuário inexistente")
    void authenticate_WithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/public/auth")
                .with(httpBasic("nonexistent@example.com", TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
