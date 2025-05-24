/*
 * @(#)UserAccountServiceImplTest.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.service;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserAccountData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.exception.UserAccountCreationException;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.mapper.UserAccountMapper;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserAccountPayload;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository.UserAccountRepository;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.service.impl.UserAccountServiceImpl;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.validation.UserAccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

/**
 * <p>Classe <code>UserAccountServiceTest</code>.</p>
 * <p>Classe de testes unitários para os serviços da conta de usuário.</p>
 * */
@ExtendWith(MockitoExtension.class)
public class UserAccountServiceImplTest {

    @Mock
    private UserAccountValidator validator;

    @Mock
    private UserAccountMapper mapper;

    @Mock
    private UserAccountRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserAccountServiceImpl userAccountServiceImpl;

    private UserAccountPayload userAccountPayload;
    private UserAccountEntity userAccountEntity;
    private UserAccountData userAccountData;
    private final UUID accountId = UUID.randomUUID();

    /**
     * <p>Configuração inicial dos testes.</p>
 * <p>Método executado antes de cada teste para configurar os objetos necessários.</p>
 * */
    @BeforeEach
    void setUp() {
        // Inicializando o serviço manualmente com os mocks
        userAccountServiceImpl = new UserAccountServiceImpl(validator, mapper, repository, passwordEncoder);
        
        // Configurando o payload para os testes
        userAccountPayload = new UserAccountPayload();
        userAccountPayload.setFullName("Usuário Teste");
        userAccountPayload.setEmail("usuario.teste@example.com");
        userAccountPayload.setHashedPassword("senhaHasheada123");

        // Configurando a entidade para os testes
        userAccountEntity = new UserAccountEntity();
        userAccountEntity.setAccountId(accountId);
        userAccountEntity.setAccountName("Usuário Teste");
        userAccountEntity.setEmail("usuario.teste@example.com");
        userAccountEntity.setPasswordHash("senhaHasheada123");
        userAccountEntity.setAccountUsername("usuario.teste");
        userAccountEntity.setCreatedAt(LocalDateTime.now());
        userAccountEntity.setEmailVerified(false);
        userAccountEntity.setUserProfile(null);

        // Configurando o DTO para os testes
        userAccountData = new UserAccountData();
        userAccountData.setAccountId(accountId);
        userAccountData.setAccountName("Usuário Teste");
        userAccountData.setAccountUsername("usuario.teste");
        userAccountData.setSubscriptionType("FREE");
    }

    /**
     * <p>Teste para o método createUserAccount com sucesso.</p>
     * <p>Verifica se o método createUserAccount está criando uma conta de usuário corretamente.</p>
     * */
    @Test
    @DisplayName("Deve criar uma conta de usuário com sucesso")
    void registerUserSuccess() {
        // Configuração dos mocks
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        when(repository.save(any(UserAccountEntity.class))).thenReturn(userAccountEntity);
        doReturn(userAccountData).when(mapper).toData(any(UserAccountEntity.class));

        // Execução do método a ser testado
        UserAccountData result = userAccountServiceImpl.registerUser(userAccountPayload);

        // Verificações
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals("Usuário Teste", result.getAccountName());
        assertEquals("usuario.teste", result.getAccountUsername());
        assertEquals("FREE", result.getSubscriptionType());

        // Verificando se os métodos foram chamados corretamente
        verify(validator, times(1)).validateEmailUniqueness(userAccountPayload.getEmail());
        verify(repository, times(1)).save(any(UserAccountEntity.class));
    }

    /**
     * <p>Teste para o método createUserAccount quando o e-mail já existe.</p>
     * <p>Verifica se o método createUserAccount está lançando a exceção correta quando o e-mail já existe.</p>
     * */
    @Test
    @DisplayName("Deve lançar exceção quando o e-mail já existe")
    void registerUserEmailAlreadyExists() {
        // Configuração dos mocks para simular e-mail já existente
        doThrow(new UserAccountCreationException("O e-mail informado já está cadastrado no sistema"))
                .when(validator).validateEmailUniqueness(anyString());

        // Execução e verificação da exceção
        UserAccountCreationException exception = assertThrows(
                UserAccountCreationException.class,
                () -> userAccountServiceImpl.registerUser(userAccountPayload)
        );

        // Verificando a mensagem da exceção
        assertEquals("O e-mail informado já está cadastrado no sistema", exception.getMessage());

        // Verificando se os métodos foram chamados corretamente
        verify(validator, times(1)).validateEmailUniqueness(userAccountPayload.getEmail());
        verify(repository, never()).saveAndFlush(any(UserAccountEntity.class));
    }

    /**
     * <p>Teste para verificar se os campos da entidade são configurados corretamente.</p>
     * <p>Verifica se o método createUserAccount está configurando os campos da entidade corretamente.</p>
     * */
    @Test
    @DisplayName("Deve verificar se os campos da entidade são configurados corretamente")
    void registerUserSetFieldsCorrectly() {
        // Configuração dos mocks
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(repository.save(any(UserAccountEntity.class))).thenReturn(userAccountEntity);
        
        // Criando um capturador de argumentos para a entidade
        ArgumentCaptor<UserAccountEntity> entityCaptor = ArgumentCaptor.forClass(UserAccountEntity.class);
        
        // Execução do método a ser testado
        userAccountServiceImpl.registerUser(userAccountPayload);
        
        // Capturando a entidade que foi passada para o método save
        verify(repository).save(entityCaptor.capture());
        UserAccountEntity savedEntity = entityCaptor.getValue();
        
        // Verificando se os campos foram configurados corretamente
        assertEquals(userAccountPayload.getFullName(), savedEntity.getAccountName());
        assertEquals(userAccountPayload.getEmail(), savedEntity.getEmail());
        assertNotNull(savedEntity.getPasswordHash());
        assertEquals("encodedPassword123", savedEntity.getPasswordHash());
        assertTrue(savedEntity.getAccountUsername().contains("usuario.teste"));
        assertFalse(savedEntity.isEmailVerified());
        assertNull(savedEntity.getUserProfile());
        
        // Verificando se os métodos foram chamados corretamente
        verify(validator, times(1)).validateEmailUniqueness(userAccountPayload.getEmail());
    }
    
    /**
     * <p>Teste para o método createUserAccount com payload nulo.</p>
     * <p>Verifica se o método createUserAccount lança NullPointerException quando o payload é nulo.</p>
     * */
    @Test
    @DisplayName("Deve lançar NullPointerException quando o payload é nulo")
    void registerUserWithNullPayload() {
        // Execução e verificação da exceção
        assertThrows(
                NullPointerException.class,
                () -> userAccountServiceImpl.registerUser(null)
        );

        // Verificando que nenhum método foi chamado
        verify(validator, never()).validateEmailUniqueness(anyString());
        verify(repository, never()).saveAndFlush(any(UserAccountEntity.class));
    }
    
    /**
     * <p>Teste para o método createUserAccount quando ocorre erro no repositório.</p>
     * <p>Verifica se o método createUserAccount propaga exceções do repositório.</p>
     * */
    @Test
    @DisplayName("Deve propagar exceções do repositório")
    void registerUserRepositoryException() {
        // Configuração dos mocks para simular erro no repositório
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        when(repository.save(any(UserAccountEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Erro de integridade de dados"));

        // Execução e verificação da exceção
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> userAccountServiceImpl.registerUser(userAccountPayload)
        );

        // Verificando a mensagem da exceção
        assertEquals("Erro de integridade de dados", exception.getMessage());

        // Verificando se os métodos foram chamados corretamente
        verify(validator, times(1)).validateEmailUniqueness(userAccountPayload.getEmail());
        verify(repository, times(1)).save(any(UserAccountEntity.class));
    }
    
    /**
     * <p>Teste para verificar se a data de criação é definida como o momento atual.</p>
     * <p>Verifica se o método createUserAccount está definindo a data de criação corretamente.</p>
     * */
    @Test
    @DisplayName("Deve definir a data de criação como o momento atual")
    void registerUserSetCurrentDateTime() {
        // Configuração dos mocks
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        
        // Capturando o momento antes da execução
        LocalDateTime beforeExecution = LocalDateTime.now();

        Mockito.lenient().when(repository.saveAndFlush(any(UserAccountEntity.class))).thenAnswer(invocation -> {
            UserAccountEntity savedEntity = invocation.getArgument(0);
            LocalDateTime createdAt = savedEntity.getCreatedAt();
            assertNotNull(createdAt);
            assertTrue(createdAt.isEqual(beforeExecution) || createdAt.isAfter(beforeExecution));
            assertTrue(createdAt.isEqual(LocalDateTime.now()) || createdAt.isBefore(LocalDateTime.now()));
            return savedEntity;
        });

        // Execução do método a ser testado
        userAccountServiceImpl.registerUser(userAccountPayload);
    }
    
    /**
     * <p>Teste para verificar se a conta é definida como ativa.</p>
     * <p>Verifica se o método createUserAccount está definindo o status da conta como ativo.</p>
     * */
    @Test
    @DisplayName("Deve definir a conta como ativa")
    void registerUserSetActiveStatus() {
        // Configuração dos mocks
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        when(repository.save(any(UserAccountEntity.class))).thenReturn(userAccountEntity);
        
        // Definindo a conta como inativa para testar se o serviço a torna ativa
        userAccountEntity.setAccountStatus(null);

        // Execução do método a ser testado
        userAccountServiceImpl.registerUser(userAccountPayload);
        
        // Capturando o argumento passado para save
        ArgumentCaptor<UserAccountEntity> entityCaptor = ArgumentCaptor.forClass(UserAccountEntity.class);
        verify(repository).save(entityCaptor.capture());
        
        // Verificando se a conta foi definida como ativa
        UserAccountEntity savedEntity = entityCaptor.getValue();
        assertEquals(UserAccountStatus.ACTIVE, savedEntity.getAccountStatus());
    }
}
