package br.com.blackhunter.hunter_wallet.rest_api.auth.integration;

import br.com.blackhunter.hunter_wallet.rest_api.auth.service.JwtService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>JwtTokenValidationIT</code>.</p>
 * Testes de integração para validação de tokens JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class JwtTokenValidationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "jwt-test@example.com";
    private static final String TEST_PASSWORD = "Test@123456";
    private UserAccountEntity testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new UserAccountEntity();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setAccountStatus(UserAccountStatus.ACTIVE);
        
        userRepository.save(testUser);
        
        // Generate a valid JWT token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_EMAIL,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        validToken = jwtService.generateToken(authentication);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        userRepository.findByEmail(TEST_EMAIL)
            .ifPresent(user -> userRepository.delete(user));
    }
    /*
    @Test
    @DisplayName("Deve permitir acesso ao endpoint protegido com token JWT válido")
    void accessProtectedEndpoint_WithValidToken_ShouldAllowAccess() throws Exception {
        // Act & Assert - Assuming there's a protected endpoint at /v1/api/user-profile
        mockMvc.perform(get("/v1/api/user-profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    */

    @Test
    @DisplayName("Deve negar acesso ao endpoint protegido sem token JWT")
    void accessProtectedEndpoint_WithoutToken_ShouldDenyAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/user-profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve negar acesso ao endpoint protegido com token JWT inválido")
    void accessProtectedEndpoint_WithInvalidToken_ShouldDenyAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/user-profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalidtoken123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve negar acesso ao endpoint protegido com token JWT expirado")
    void accessProtectedEndpoint_WithExpiredToken_ShouldDenyAccess() throws Exception {
        // Arrange - Create an expired token (this is a simplified example)
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiJqd3QtdGVzdEBleGFtcGxlLmNvbSIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ." +
                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // Act & Assert
        mockMvc.perform(get("/v1/api/user-profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
