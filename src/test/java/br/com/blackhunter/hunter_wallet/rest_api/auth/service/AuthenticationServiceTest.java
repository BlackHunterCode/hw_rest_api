package br.com.blackhunter.hunter_wallet.rest_api.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>AuthenticationServiceTest</code>.</p>
 * Testes unitários para o serviço de autenticação.
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Deve autenticar o usuário e retornar o token JWT")
    void authenticate_ShouldReturnJwtToken() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.1234567890";
        when(jwtService.generateToken(authentication)).thenReturn(expectedToken);

        // Act
        String actualToken = authenticationService.authenticate(authentication);

        // Assert
        assertEquals(expectedToken, actualToken);
        verify(jwtService, times(1)).generateToken(authentication);
    }

    @Test
    @DisplayName("Deve propagar exceções do serviço JWT")
    void authenticate_WhenJwtServiceThrowsException_ShouldPropagateException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("JWT generation failed");
        when(jwtService.generateToken(authentication)).thenThrow(expectedException);

        // Act & Assert
        try {
            authenticationService.authenticate(authentication);
        } catch (Exception e) {
            assertEquals(expectedException, e);
        }
        
        verify(jwtService, times(1)).generateToken(authentication);
    }
}
