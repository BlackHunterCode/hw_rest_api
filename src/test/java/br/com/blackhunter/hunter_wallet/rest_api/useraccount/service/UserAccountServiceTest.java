/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 *
 * Classe protegida - Aletrações somente por CODEOWNERS.
 * */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.service;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserAccountData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.exception.UserAccountCreationException;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.mapper.UserAccountMapper;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserAccountPayload;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository.UserAccountRepository;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.service.impl.UserAccountService;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.validation.UserAccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * <p>Classe <code>UserAccountServiceTest</code>.</p>
 * <p>Classe de testes unitários para os serviços da conta de usuário.</p>
 * */
@ExtendWith(MockitoExtension.class)
public class UserAccountServiceTest {

    @Mock
    private UserAccountValidator validator;

    @Mock
    private UserAccountMapper mapper;

    @Mock
    private UserAccountRepository repository;

    private UserAccountService userAccountService;

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
        userAccountService = new UserAccountService(validator, mapper, repository);
        
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
    void createUserAccountSuccess() {
        // Configuração dos mocks
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        when(repository.saveAndFlush(any(UserAccountEntity.class))).thenReturn(userAccountEntity);
        when(mapper.toData(any(UserAccountEntity.class))).thenReturn(userAccountData);

        // Execução do método a ser testado
        UserAccountData result = userAccountService.createUserAccount(userAccountPayload);

        // Verificações
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals("Usuário Teste", result.getAccountName());
        assertEquals("usuario.teste", result.getAccountUsername());
        assertEquals("FREE", result.getSubscriptionType());

        // Verificando se os métodos foram chamados corretamente
        verify(validator, times(1)).validateEmailUniqueness(userAccountPayload.getEmail());
        verify(repository, times(1)).saveAndFlush(any(UserAccountEntity.class));
    }

    /**
     * <p>Teste para o método createUserAccount quando o e-mail já existe.</p>
     * <p>Verifica se o método createUserAccount está lançando a exceção correta quando o e-mail já existe.</p>
     * */
    @Test
    @DisplayName("Deve lançar exceção quando o e-mail já existe")
    void createUserAccountEmailAlreadyExists() {
        // Configuração dos mocks para simular e-mail já existente
        doThrow(new UserAccountCreationException("O e-mail informado já está cadastrado no sistema"))
                .when(validator).validateEmailUniqueness(anyString());

        // Execução e verificação da exceção
        UserAccountCreationException exception = assertThrows(
                UserAccountCreationException.class,
                () -> userAccountService.createUserAccount(userAccountPayload)
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
    @DisplayName("Deve configurar os campos da entidade corretamente")
    void createUserAccountSetFieldsCorrectly() {
        // Configuração dos mocks com captura do argumento
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        when(repository.saveAndFlush(any(UserAccountEntity.class))).thenAnswer(invocation -> {
            UserAccountEntity savedEntity = invocation.getArgument(0);
            // Verificando se os campos foram configurados corretamente
            assertEquals(UserAccountStatus.ACTIVE,savedEntity.getAccountStatus() );
            assertNotNull(savedEntity.getCreatedAt());
            return savedEntity;
        });

        // Execução do método a ser testado
        userAccountService.createUserAccount(userAccountPayload);

        // Verificações adicionais
        verify(repository, times(1)).saveAndFlush(argThat(entity ->
            Objects.equals(entity.getAccountStatus(), UserAccountStatus.ACTIVE) && entity.getCreatedAt() != null
        ));
    }
    
    /**
     * <p>Teste para o método createUserAccount com payload nulo.</p>
     * <p>Verifica se o método createUserAccount lança NullPointerException quando o payload é nulo.</p>
     * */
    @Test
    @DisplayName("Deve lançar NullPointerException quando o payload é nulo")
    void createUserAccountWithNullPayload() {
        // Execução e verificação da exceção
        assertThrows(
                NullPointerException.class,
                () -> userAccountService.createUserAccount(null)
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
    void createUserAccountRepositoryException() {
        // Configuração dos mocks para simular erro no repositório
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        when(repository.saveAndFlush(any(UserAccountEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Erro de integridade de dados"));

        // Execução e verificação da exceção
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> userAccountService.createUserAccount(userAccountPayload)
        );

        // Verificando a mensagem da exceção
        assertEquals("Erro de integridade de dados", exception.getMessage());

        // Verificando se os métodos foram chamados corretamente
        verify(validator, times(1)).validateEmailUniqueness(userAccountPayload.getEmail());
        verify(repository, times(1)).saveAndFlush(any(UserAccountEntity.class));
    }
    
    /**
     * <p>Teste para verificar se a data de criação é definida como o momento atual.</p>
     * <p>Verifica se o método createUserAccount está definindo a data de criação corretamente.</p>
     * */
    @Test
    @DisplayName("Deve definir a data de criação como o momento atual")
    void createUserAccountSetCurrentDateTime() {
        // Configuração dos mocks
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        
        // Capturando o momento antes da execução
        LocalDateTime beforeExecution = LocalDateTime.now();
        
        when(repository.saveAndFlush(any(UserAccountEntity.class))).thenAnswer(invocation -> {
            UserAccountEntity savedEntity = invocation.getArgument(0);
            // A data de criação deve ser posterior ou igual ao momento antes da execução
            // e anterior ou igual ao momento após a execução
            LocalDateTime createdAt = savedEntity.getCreatedAt();
            assertNotNull(createdAt);
            assertTrue(createdAt.isEqual(beforeExecution) || createdAt.isAfter(beforeExecution));
            assertTrue(createdAt.isEqual(LocalDateTime.now()) || createdAt.isBefore(LocalDateTime.now()));
            return savedEntity;
        });

        // Execução do método a ser testado
        userAccountService.createUserAccount(userAccountPayload);
    }
    
    /**
     * <p>Teste para verificar se a conta é definida como ativa.</p>
     * <p>Verifica se o método createUserAccount está definindo o status da conta como ativo.</p>
     * */
    @Test
    @DisplayName("Deve definir a conta como ativa")
    void createUserAccountSetActiveStatus() {
        // Configuração dos mocks
        doNothing().when(validator).validateEmailUniqueness(anyString());
        when(mapper.toEntity(any(UserAccountPayload.class))).thenReturn(userAccountEntity);
        
        // Definindo a conta como inativa para testar se o serviço a torna ativa
        userAccountEntity.setAccountStatus(null);
        
        when(repository.saveAndFlush(any(UserAccountEntity.class))).thenAnswer(invocation -> {
            UserAccountEntity savedEntity = invocation.getArgument(0);
            // Verificando se a conta foi definida como ativa
            assertEquals(UserAccountStatus.ACTIVE, savedEntity.getAccountStatus());
            return savedEntity;
        });

        // Execução do método a ser testado
        userAccountService.createUserAccount(userAccountPayload);
    }
}
