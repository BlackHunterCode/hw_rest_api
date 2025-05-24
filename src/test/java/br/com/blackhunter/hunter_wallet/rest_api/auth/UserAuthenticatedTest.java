package br.com.blackhunter.hunter_wallet.rest_api.auth;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>UserAuthenticatedTest</code>.</p>
 * Testes unitários para a classe UserAuthenticated.
 */
public class UserAuthenticatedTest {

    private UserAccountEntity mockUser;
    private UserAuthenticated userAuthenticated;

    @BeforeEach
    void setUp() {
        mockUser = mock(UserAccountEntity.class);
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.getPasswordHash()).thenReturn("hashedPassword");
        userAuthenticated = new UserAuthenticated(mockUser);
    }

    @Test
    @DisplayName("Deve retornar as autorizações corretas")
    void getAuthorities_ShouldReturnCorrectAuthorities() {
        // Act
        Collection<? extends GrantedAuthority> authorities = userAuthenticated.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertFalse(authorities.isEmpty());
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Deve retornar a senha correta")
    void getPassword_ShouldReturnCorrectPassword() {
        // Arrange
        when(mockUser.getPasswordHash()).thenReturn("hashedPassword");

        // Act
        String password = userAuthenticated.getPassword();

        // Assert
        assertEquals("hashedPassword", password);
    }

    @Test
    @DisplayName("Deve retornar o nome de usuário correto")
    void getUsername_ShouldReturnCorrectUsername() {
        // Arrange
        when(mockUser.getEmail()).thenReturn("test@example.com");

        // Act
        String username = userAuthenticated.getUsername();

        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Deve retornar que a conta não está expirada")
    void isAccountNonExpired_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userAuthenticated.isAccountNonExpired());
    }

    @Test
    @DisplayName("Deve retornar que a conta não está bloqueada")
    void isAccountNonLocked_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userAuthenticated.isAccountNonLocked());
    }

    @Test
    @DisplayName("Deve retornar que as credenciais não estão expiradas")
    void isCredentialsNonExpired_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(userAuthenticated.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Deve retornar habilitado quando o status da conta é ACTIVE")
    void isEnabled_WhenAccountStatusIsActive_ShouldReturnTrue() {
        // Arrange
        when(mockUser.getAccountStatus()).thenReturn(UserAccountStatus.ACTIVE);

        // Act
        boolean enabled = userAuthenticated.isEnabled();

        // Assert
        assertTrue(enabled);
    }

    @Test
    @DisplayName("Deve retornar desabilitado quando o status da conta não é ACTIVE")
    void isEnabled_WhenAccountStatusIsNotActive_ShouldReturnFalse() {
        // Arrange
        when(mockUser.getAccountStatus()).thenReturn(UserAccountStatus.INACTIVE);

        // Act
        boolean enabled = userAuthenticated.isEnabled();

        // Assert
        assertFalse(enabled);
    }
}
