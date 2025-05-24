package br.com.blackhunter.hunter_wallet.rest_api.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>JwtTokenDecoderTest</code>.</p>
 * Testes unitários para a decodificação e validação de tokens JWT.
 */
@ExtendWith(MockitoExtension.class)
public class JwtTokenDecoderTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private Authentication authentication;

    @Test
    @DisplayName("Deve decodificar o token JWT e validar os claims")
    void decodeJwtToken_ShouldReturnValidClaims() {
        // Arrange
        JwtService jwtService = new JwtService(jwtEncoder);
        String username = "test@example.com";
        String scope = "ROLE_USER";
        Instant now = Instant.now();
        
        // Setup authentication mock
        when(authentication.getName()).thenReturn(username);
        
        // Use a raw type with suppressed warnings to match the expected return type
        @SuppressWarnings({"unchecked", "rawtypes"})
        Collection authorities = Collections.singletonList(new SimpleGrantedAuthority(scope));
        when(authentication.getAuthorities()).thenReturn(authorities);
        
        // Setup JWT encoder mock to return a token
        Jwt jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "RS256")
                .claim("sub", username)
                .claim("scope", scope)
                .claim("iss", "https://blakchunter.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
        
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        
        // Setup JWT decoder mock
        when(jwtDecoder.decode("dummy-token")).thenReturn(jwt);
        
        // Act
        String token = jwtService.generateToken(authentication);
        Jwt decodedJwt = jwtDecoder.decode(token);
        
        // Assert
        assertNotNull(decodedJwt);
        assertEquals(username, decodedJwt.getSubject());
        assertEquals(scope, decodedJwt.getClaimAsString("scope"));
        assertEquals("https://blakchunter.com", decodedJwt.getIssuer().toString());
        
        // Verify expiration is approximately 1 hour from now
        long tokenExpiration = decodedJwt.getExpiresAt().getEpochSecond();
        long expectedExpiration = now.plusSeconds(3600).getEpochSecond();
        // Allow a small margin of error
        assertTrue(Math.abs(tokenExpiration - expectedExpiration) < 10);
    }

    @Test
    @DisplayName("Deve validar a expiração do token")
    void validateJwtToken_ShouldCheckExpiration() {
        // Arrange
        Instant now = Instant.now();
        Instant pastTime = now.minusSeconds(3600); // 1 hour in the past
        
        // Create an expired token
        Jwt expiredJwt = Jwt.withTokenValue("expired-token")
                .header("alg", "RS256")
                .claim("sub", "test@example.com")
                .claim("iss", "https://blakchunter.com")
                .issuedAt(pastTime)
                .expiresAt(pastTime.plusSeconds(1)) // Expired 1 second after issue
                .build();
        
        // Act & Assert
        assertTrue(expiredJwt.getExpiresAt().isBefore(now), "Token should be expired");
    }

    @Test
    @DisplayName("Deve extrair todos os claims do token JWT")
    void extractAllClaims_ShouldReturnAllClaims() {
        // Arrange
        Instant now = Instant.now();
        
        // Create a token with multiple claims
        Jwt jwt = Jwt.withTokenValue("multi-claim-token")
                .header("alg", "RS256")
                .claim("sub", "test@example.com")
                .claim("scope", "ROLE_USER")
                .claim("iss", "https://blakchunter.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("user_id", "12345")
                .claim("email_verified", true)
                .build();
        
        // Act
        Map<String, Object> claims = jwt.getClaims();
        
        // Assert
        assertNotNull(claims);
        assertEquals(7, claims.size());
        assertEquals("test@example.com", claims.get("sub"));
        assertEquals("ROLE_USER", claims.get("scope"));
        assertEquals("https://blakchunter.com", claims.get("iss"));
        assertEquals("12345", claims.get("user_id"));
        assertEquals(true, claims.get("email_verified"));
    }
}
