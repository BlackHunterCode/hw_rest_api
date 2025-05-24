package br.com.blackhunter.hunter_wallet.rest_api.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>JwtServiceTest</code>.</p>
 * Testes unitários para o serviço de JWT.
 */
@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private JwtService jwtService;

    @Test
    @DisplayName("Deve gerar token JWT com claims corretos")
    void generateToken_ShouldReturnValidJwtToken() {
        // Arrange
        String username = "test@example.com";
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.1234567890";
        
        when(authentication.getName()).thenReturn(username);
        
        // Use a raw type with suppressed warnings to match the expected return type
        @SuppressWarnings({"unchecked", "rawtypes"})
        Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        
        when(jwt.getTokenValue()).thenReturn(expectedToken);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Act
        String actualToken = jwtService.generateToken(authentication);

        // Assert
        assertNotNull(actualToken);
        assertEquals(expectedToken, actualToken);
        verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
    }

    @Test
    @DisplayName("Deve incluir claims corretos no token JWT")
    void generateToken_ShouldIncludeCorrectClaims() {
        // Arrange
        String username = "test@example.com";
        String expectedToken = "token";
        
        when(authentication.getName()).thenReturn(username);
        
        // Use a raw type with suppressed warnings to match the expected return type
        @SuppressWarnings({"unchecked", "rawtypes"})
        Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        
        when(jwt.getTokenValue()).thenReturn(expectedToken);
        
        // Capture the JwtClaimsSet to verify its contents
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
            JwtEncoderParameters parameters = invocation.getArgument(0);
            JwtClaimsSet claims = parameters.getClaims();
            
            // Verify claims
            assertEquals(username, claims.getSubject());
            assertEquals("ROLE_USER", claims.getClaim("scope"));
            assertNotNull(claims.getIssuedAt());
            assertNotNull(claims.getExpiresAt());
            assertEquals("https://blakchunter.com", claims.getIssuer().toString());
            
            return jwt;
        });

        // Act
        String actualToken = jwtService.generateToken(authentication);

        // Assert
        assertEquals(expectedToken, actualToken);
    }

    @Test
    @DisplayName("Deve definir o tempo de expiração do token corretamente")
    void generateToken_ShouldSetCorrectExpirationTime() {
        // Arrange
        Instant now = Instant.now();
        long expectedExpirationSeconds = 3600L; // 1 hour
        
        when(authentication.getName()).thenReturn("test@example.com");
        
        // Use a raw type with suppressed warnings to match the expected return type
        @SuppressWarnings({"unchecked", "rawtypes"})
        Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        
        when(jwt.getTokenValue()).thenReturn("token");
        
        // Capture the JwtClaimsSet to verify expiration time
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
            JwtEncoderParameters parameters = invocation.getArgument(0);
            JwtClaimsSet claims = parameters.getClaims();
            
            // Verify expiration time is approximately 1 hour from now
            Instant issuedAt = claims.getIssuedAt();
            Instant expiresAt = claims.getExpiresAt();
            
            long actualDuration = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
            
            // Allow a small margin of error for test execution time
            assertEquals(expectedExpirationSeconds, actualDuration, 5);
            
            return jwt;
        });

        // Act
        jwtService.generateToken(authentication);

        // Assert
        verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
    }
}
