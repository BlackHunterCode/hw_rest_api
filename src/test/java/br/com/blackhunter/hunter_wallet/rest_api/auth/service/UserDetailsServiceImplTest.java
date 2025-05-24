package br.com.blackhunter.hunter_wallet.rest_api.auth.service;

import br.com.blackhunter.hunter_wallet.rest_api.auth.UserAuthenticated;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 * Classe protegida - Alterações somente por CODEOWNERS.
 * <p>Classe <code>UserDetailsServiceImplTest</code>.</p>
 * Testes unitários para o serviço de detalhes do usuário.
 */
@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserAccountRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Deve carregar usuário pelo nome de usuário quando o usuário existe")
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Arrange
        String email = "test@example.com";
        UserAccountEntity userEntity = new UserAccountEntity();
        userEntity.setEmail(email);
        userEntity.setPasswordHash("hashedPassword");
        userEntity.setAccountStatus(UserAccountStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof UserAuthenticated);
        assertEquals(email, userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o usuário não existe")
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve tratar nome de usuário nulo")
    void loadUserByUsername_WhenUsernameIsNull_ShouldThrowException() {
        // Arrange
        String email = null;
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );
        
        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    @DisplayName("Deve tratar nome de usuário vazio")
    void loadUserByUsername_WhenUsernameIsEmpty_ShouldThrowException() {
        // Arrange
        String email = "";
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );
        
        verify(userRepository, times(1)).findByEmail("");
    }
}
