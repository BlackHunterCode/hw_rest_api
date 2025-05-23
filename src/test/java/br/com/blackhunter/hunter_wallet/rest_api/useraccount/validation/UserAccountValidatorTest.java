/*
 * @(#)UserAccountValidatorTest.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.validation;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.exception.UserAccountCreationException;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * <p>Classe <code>UserAccountValidatorTest</code>.</p>
 * <p>Classe de testes unitários para as validações complexas da conta de usuário.</p>
 * */
@ExtendWith(MockitoExtension.class)
public class UserAccountValidatorTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private UserAccountValidator userAccountValidator;

    /**
     * <p>Teste para o método validateEmailUniqueness quando o e-mail não existe.</p>
     * <p>Verifica se o método validateEmailUniqueness não lança exceção quando o e-mail não existe.</p>
     * */
    @Test
    @DisplayName("Não deve lançar exceção quando o e-mail não existe")
    void validateEmailUniquenessWhenEmailDoesNotExist() {
        // Configuração do mock
        when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);

        // Execução e verificação
        assertDoesNotThrow(() -> userAccountValidator.validateEmailUniqueness("novo.email@example.com"));

        // Verificando se o método foi chamado corretamente
        verify(userAccountRepository, times(1)).existsByEmail("novo.email@example.com");
    }

    /**
     * <p>Teste para o método validateEmailUniqueness quando o e-mail já existe.</p>
     * <p>Verifica se o método validateEmailUniqueness lança a exceção correta quando o e-mail já existe.</p>
     * */
    @Test
    @DisplayName("Deve lançar exceção quando o e-mail já existe")
    void validateEmailUniquenessWhenEmailExists() {
        // Configuração do mock
        when(userAccountRepository.existsByEmail(anyString())).thenReturn(true);

        // Execução e verificação da exceção
        UserAccountCreationException exception = assertThrows(
                UserAccountCreationException.class,
                () -> userAccountValidator.validateEmailUniqueness("email.existente@example.com")
        );

        // Verificando a mensagem da exceção
        assertEquals("O e-mail informado já está cadastrado no sistema", exception.getMessage());

        // Verificando se o método foi chamado corretamente
        verify(userAccountRepository, times(1)).existsByEmail("email.existente@example.com");
    }

    /**
     * <p>Teste para o método validateEmailUniqueness com e-mail nulo.</p>
     * <p>Verifica se o método validateEmailUniqueness é chamado com e-mail nulo.</p>
     * <p>Este teste é importante para garantir que o método não falhe com NullPointerException.</p>
     * */
    @Test
    @DisplayName("Deve lidar corretamente com e-mail nulo")
    void validateEmailUniquenessWithNullEmail() {
        // Configuração do mock
        when(userAccountRepository.existsByEmail(null)).thenReturn(false);

        // Execução e verificação
        assertDoesNotThrow(() -> userAccountValidator.validateEmailUniqueness(null));

        // Verificando se o método foi chamado corretamente
        verify(userAccountRepository, times(1)).existsByEmail(null);
    }

    /**
     * <p>Teste para o método validateEmailUniqueness com e-mail vazio.</p>
     * <p>Verifica se o método validateEmailUniqueness é chamado com e-mail vazio.</p>
     * */
    @Test
    @DisplayName("Deve lidar corretamente com e-mail vazio")
    void validateEmailUniquenessWithEmptyEmail() {
        // Configuração do mock
        when(userAccountRepository.existsByEmail("")).thenReturn(false);

        // Execução e verificação
        assertDoesNotThrow(() -> userAccountValidator.validateEmailUniqueness(""));

        // Verificando se o método foi chamado corretamente
        verify(userAccountRepository, times(1)).existsByEmail("");
    }

    private void assertEquals(String expected, String actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
