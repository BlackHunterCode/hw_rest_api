/*
 * @(#)UserProfileServiceImplTest.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.hunter_wallet.rest_api.userprofile.service;

import br.com.blackhunter.hunter_wallet.rest_api.core.exception.BusinessException;
import br.com.blackhunter.hunter_wallet.rest_api.core.util.MultipartFileUtil;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserProfileData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserProfileEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.mapper.UserProfileMapper;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserProfilePayload;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository.UserProfileRepository;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.service.impl.UserProfileServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * <p>Classe <code>UserProfileServiceImplTest</code>.</p>
 * <p>Classe de testes unitários para os serviços de perfil de usuário.</p>
 * */
@ExtendWith(MockitoExtension.class)
public class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository repository;
    
    private UserProfileMapper mapper;

    private UserProfileServiceImpl userProfileService;

    private UserProfilePayload payload;
    private UserProfileEntity profileEntity;
    private UserAccountEntity userAccountEntity;
    private UserProfileData profileData;
    private final UUID profileId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final byte[] pictureBytes = "test image content".getBytes();

    /**
     * <p>Configuração inicial dos testes.</p>
     * <p>Método executado antes de cada teste para configurar os objetos necessários.</p>
     * */
    @BeforeEach
    void setUp() {
        // Criando uma implementação manual do mapper em vez de usar @Mock
        // para evitar o erro de inicialização do MapStruct
        mapper = new UserProfileMapper() {
            @Override
            public UserProfileEntity toEntity(UserProfilePayload payload) {
                return profileEntity;
            }

            @Override
            public UserProfileData toData(UserProfileEntity entity) {
                return profileData;
            }

            @Override
            public void updateEntity(UserProfileEntity entity, UserProfilePayload payload) {
                // Método vazio para o teste
            }
        };
        
        // Inicializando o serviço com os mocks
        userProfileService = new UserProfileServiceImpl(repository, mapper);
        
        // Configurando a entidade de conta de usuário
        userAccountEntity = new UserAccountEntity();
        userAccountEntity.setAccountId(accountId);
        userAccountEntity.setAccountName("Usuário Teste");
        userAccountEntity.setEmail("usuario.teste@example.com");
        
        // Configurando o payload
        payload = new UserProfilePayload();
        payload.setFirstName("João");
        payload.setLastName("Silva");
        payload.setPhoneNumber("+5511999998888");
        payload.setBirthDate(LocalDate.of(1990, 1, 15));
        
        // Configurando a entidade de perfil
        profileEntity = new UserProfileEntity();
        profileEntity.setProfileId(profileId);
        profileEntity.setUserAccount(userAccountEntity);
        profileEntity.setFirstName("João");
        profileEntity.setLastName("Silva");
        profileEntity.setPhoneNumber("+5511999998888");
        profileEntity.setBirthDate(LocalDate.of(1990, 1, 15));
        
        // Configurando o DTO
        profileData = new UserProfileData();
        profileData.setProfileId(profileId);
        profileData.setUserId(accountId);
        profileData.setFirstName("João");
        profileData.setLastName("Silva");
        profileData.setFullName("João Silva");
        profileData.setAge(33L);
    }

    /**
     * <p>Teste para o método createProfile com sucesso.</p>
     * <p>Verifica se o método createProfile está criando um perfil de usuário corretamente.</p>
     * */
    @Test
    @DisplayName("Deve criar um perfil de usuário com sucesso")
    void createProfileSuccess() {
        // Configuração dos mocks
        // Não podemos usar when() no mapper pois não é um mock
        // Os métodos toEntity e toData já estão implementados na classe anônima
        when(repository.save(profileEntity)).thenReturn(profileEntity);

        // Execução do método a ser testado
        UserProfileData result = userProfileService.createProfile(userAccountEntity, payload);

        // Verificações
        assertNotNull(result);
        assertEquals(profileId, result.getProfileId());
        assertEquals(accountId, result.getUserId());
        assertEquals("João Silva", result.getFullName());
        assertEquals("João", result.getFirstName());
        assertEquals("Silva", result.getLastName());
        assertEquals(33L, result.getAge());

        // Verificando se os métodos foram chamados corretamente
        verify(repository, times(1)).save(profileEntity);
    }

    /**
     * <p>Teste para o método createProfile com foto de perfil.</p>
     * <p>Verifica se o método createProfile está processando corretamente a foto de perfil.</p>
     * */
    @Test
    @DisplayName("Deve processar a foto de perfil corretamente ao criar perfil")
    void createProfileWithProfilePicture() {
        // Configurando o arquivo de imagem
        MockMultipartFile pictureFile = new MockMultipartFile(
                "profilePicture", 
                "profile.jpg", 
                "image/jpeg", 
                pictureBytes
        );
        payload.setProfilePictureFile(pictureFile);
        
        // Configuração dos mocks
        // Não podemos usar when() no mapper pois não é um mock
        // Os métodos toEntity e toData já estão implementados na classe anônima
        when(repository.save(profileEntity)).thenReturn(profileEntity);
        
        // Mock estático para o MultipartFileUtil
        try (MockedStatic<MultipartFileUtil> mockedStatic = mockStatic(MultipartFileUtil.class)) {
            // Use o objeto específico em vez de matchers para evitar erros
            mockedStatic.when(() -> MultipartFileUtil.validateAndGetMultipartFileBytes(pictureFile))
                    .thenReturn(pictureBytes);
            
            // Execução do método a ser testado
            UserProfileData result = userProfileService.createProfile(userAccountEntity, payload);
            
            // Verificações
            assertNotNull(result);
            
            // Verificando se os métodos foram chamados corretamente
            mockedStatic.verify(() -> MultipartFileUtil.validateAndGetMultipartFileBytes(pictureFile), times(1));
            // Verificamos que o repository.save foi chamado com o profileEntity
            verify(repository, times(1)).save(profileEntity);
        }
    }

    /**
     * <p>Teste para o método createProfile com payload nulo.</p>
     * <p>Verifica se o método createProfile lança BusinessException quando o payload é nulo.</p>
     * */
    @Test
    @DisplayName("Deve lançar BusinessException quando o payload é nulo")
    void createProfileWithNullPayload() {
        // Execução e verificação da exceção
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userProfileService.createProfile(userAccountEntity, null)
        );
        
        // Verificando a mensagem da exceção
        assertEquals("UserProfilePayload cannot be null", exception.getMessage());

        // Verificando que nenhum método foi chamado
        verify(repository, never()).save(any(UserProfileEntity.class));
        // Não podemos usar verify com nossa implementação manual do mapper pois não é um mock
    }

    /**
     * <p>Teste para o método createProfile com userAccount nulo.</p>
     * <p>Verifica se o método createProfile lança BusinessException quando o userAccount é nulo.</p>
     * */
    @Test
    @DisplayName("Deve lançar BusinessException quando o userAccount é nulo")
    void createProfileWithNullUserAccount() {
        // Execução e verificação da exceção
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userProfileService.createProfile(null, payload)
        );
        
        // Verificando a mensagem da exceção
        assertEquals("UserAccountEntity cannot be null", exception.getMessage());

        // Verificando que nenhum método foi chamado
        verify(repository, never()).save(any(UserProfileEntity.class));
        // Não podemos usar verify com nossa implementação manual do mapper
    }

    /**
     * <p>Teste para o método updateProfile com sucesso.</p>
     * <p>Verifica se o método updateProfile está atualizando um perfil corretamente.</p>
     * */
    @Test
    @DisplayName("Deve atualizar um perfil com sucesso")
    void updateProfileSuccess() {
        // Configuração dos mocks
        when(repository.findByAccountId(accountId)).thenReturn(Optional.of(profileEntity));
        // Não podemos usar doNothing().when() no mapper pois não é um mock
        // O método updateEntity já está implementado na classe anônima
        when(repository.save(any(UserProfileEntity.class))).thenReturn(profileEntity);
        // Não precisamos configurar mapper.toData pois já está implementado na classe anônima

        // Execução do método a ser testado
        UserProfileData result = userProfileService.updateProfile(accountId, payload);

        // Verificações
        assertNotNull(result);
        assertEquals(profileId, result.getProfileId());
        assertEquals("João Silva", result.getFullName());

        // Verificando se os métodos foram chamados corretamente
        verify(repository, times(1)).findByAccountId(accountId);
        verify(repository, times(1)).save(profileEntity);
    }

    /**
     * <p>Teste para o método updateProfile com foto de perfil.</p>
     * <p>Verifica se o método updateProfile está processando corretamente a atualização da foto de perfil.</p>
     * */
    @Test
    @DisplayName("Deve processar a foto de perfil corretamente ao atualizar perfil")
    void updateProfileWithProfilePicture() {
        // Configurando o arquivo de imagem
        MockMultipartFile pictureFile = new MockMultipartFile(
                "profilePicture", 
                "profile.jpg", 
                "image/jpeg", 
                pictureBytes
        );
        payload.setProfilePictureFile(pictureFile);
        
        // Configuração dos mocks
        when(repository.findByAccountId(accountId)).thenReturn(Optional.of(profileEntity));
        // Não podemos usar doNothing().when() no mapper pois não é um mock
        // O método updateEntity já está implementado na classe anônima
        when(repository.save(any(UserProfileEntity.class))).thenReturn(profileEntity);
        // Não precisamos configurar mapper.toData pois já está implementado na classe anônima
        
        // Mock estático para o MultipartFileUtil
        try (MockedStatic<MultipartFileUtil> mockedStatic = mockStatic(MultipartFileUtil.class)) {
            // Use o objeto específico em vez de matchers para evitar erros
            mockedStatic.when(() -> MultipartFileUtil.validateAndGetMultipartFileBytes(pictureFile))
                    .thenReturn(pictureBytes);
            
            // Execução do método a ser testado
            UserProfileData result = userProfileService.updateProfile(accountId, payload);
            
            // Verificações
            assertNotNull(result);
            
            // Verificando se os métodos foram chamados corretamente
            mockedStatic.verify(() -> MultipartFileUtil.validateAndGetMultipartFileBytes(pictureFile), times(1));
            // Verificamos que o repository.save foi chamado com o profileEntity
            verify(repository, times(1)).save(profileEntity);
        }
    }

    /**
     * <p>Teste para o método updateProfile quando o perfil não existe.</p>
     * <p>Verifica se o método updateProfile lança EntityNotFoundException quando o perfil não existe.</p>
     * */
    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o perfil não existe")
    void updateProfileNotFound() {
        // Configuração dos mocks
        when(repository.findByAccountId(accountId)).thenReturn(Optional.empty());

        // Execução e verificação da exceção
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userProfileService.updateProfile(accountId, payload)
        );

        // Verificando a mensagem da exceção
        assertEquals("The user profile provided does not exist.", exception.getMessage());

        // Verificando que os métodos foram chamados corretamente
        verify(repository, times(1)).findByAccountId(accountId);
        verify(repository, never()).save(any(UserProfileEntity.class));
    }

    /**
     * <p>Teste para o método findByUserAccountId com sucesso.</p>
     * <p>Verifica se o método findByUserAccountId está retornando um perfil corretamente.</p>
     * */
    @Test
    @DisplayName("Deve encontrar um perfil pelo ID da conta de usuário")
    void findByUserAccountIdSuccess() {
        // Configuração dos mocks
        when(repository.findByAccountId(accountId)).thenReturn(Optional.of(profileEntity));

        // Execução do método a ser testado
        UserProfileEntity result = userProfileService.findByUserAccountId(accountId);

        // Verificações
        assertNotNull(result);
        assertEquals(profileId, result.getProfileId());
        assertEquals("João", result.getFirstName());
        assertEquals("Silva", result.getLastName());
        assertEquals(userAccountEntity, result.getUserAccount());

        // Verificando se os métodos foram chamados corretamente
        verify(repository, times(1)).findByAccountId(accountId);
    }

    /**
     * <p>Teste para o método findByUserAccountId quando o perfil não existe.</p>
     * <p>Verifica se o método findByUserAccountId lança EntityNotFoundException quando o perfil não existe.</p>
     * */
    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o perfil não é encontrado")
    void findByUserAccountIdNotFound() {
        // Configuração dos mocks
        when(repository.findByAccountId(accountId)).thenReturn(Optional.empty());

        // Execução e verificação da exceção
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userProfileService.findByUserAccountId(accountId)
        );

        // Verificando a mensagem da exceção
        assertEquals("The user profile provided does not exist.", exception.getMessage());

        // Verificando que os métodos foram chamados corretamente
        verify(repository, times(1)).findByAccountId(accountId);
    }
}
