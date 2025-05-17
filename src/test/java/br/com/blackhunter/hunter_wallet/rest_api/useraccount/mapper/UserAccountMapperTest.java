/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 *
 * Classe protegida - Aletrações somente por CODEOWNERS.
 * */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.mapper;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserAccountData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserAccountPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <p>Classe <code>UserAccountMapperTest</code>.</p>
 * <p>Classe de testes unitários para o mapeador de contas de usuário.</p>
 * */
public class UserAccountMapperTest {

    private UserAccountMapper mapper;
    private UserAccountPayload userAccountPayload;
    private UserAccountEntity userAccountEntity;
    private final UUID accountId = UUID.randomUUID();
    private final LocalDateTime createdAt = LocalDateTime.now();

    /**
     * <p>Configuração inicial dos testes.</p>
     * <p>Método executado antes de cada teste para configurar os objetos necessários.</p>
     * */
    @BeforeEach
    void setUp() {
        // Inicializando o mapper
        mapper = Mappers.getMapper(UserAccountMapper.class);

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
        userAccountEntity.setAccountStatus(UserAccountStatus.ACTIVE);
        userAccountEntity.setCreatedAt(createdAt);
    }

    /**
     * <p>Teste para o método toEntity.</p>
     * <p>Verifica se o método toEntity está mapeando corretamente de UserAccountPayload para UserAccountEntity.</p>
     * */
    @Test
    @DisplayName("Deve mapear corretamente de UserAccountPayload para UserAccountEntity")
    void toEntityTest() {
        // Execução do método a ser testado
        UserAccountEntity result = mapper.toEntity(userAccountPayload);

        // Verificações
        assertNotNull(result);
        assertEquals(userAccountPayload.getFullName(), result.getAccountName());
        assertEquals(userAccountPayload.getEmail(), result.getEmail());
        assertEquals(userAccountPayload.getHashedPassword(), result.getPasswordHash());
        // Nota: A entidade não possui o campo subscriptionType, ele é definido como constante no mapper
    }

    /**
     * <p>Teste para o método toData.</p>
     * <p>Verifica se o método toData está mapeando corretamente de UserAccountEntity para UserAccountData.</p>
     * */
    @Test
    @DisplayName("Deve mapear corretamente de UserAccountEntity para UserAccountData")
    void toDataTest() {
        // Execução do método a ser testado
        UserAccountData result = mapper.toData(userAccountEntity);

        // Verificações
        assertNotNull(result);
        assertEquals(userAccountEntity.getAccountId(), result.getAccountId());
        assertEquals(userAccountEntity.getAccountName(), result.getAccountName());
        assertEquals(userAccountEntity.getAccountUsername(), result.getAccountUsername());
        assertEquals("FREE", result.getSubscriptionType());
    }

    /**
     * <p>Teste para o método toEntity com valores nulos.</p>
     * <p>Verifica se o método toEntity lida corretamente com valores nulos.</p>
     * */
    @Test
    @DisplayName("Deve lidar corretamente com valores nulos ao mapear para Entity")
    void toEntityWithNullValuesTest() {
        // Configurando payload com valores nulos
        UserAccountPayload nullPayload = new UserAccountPayload();

        // Execução do método a ser testado
        UserAccountEntity result = mapper.toEntity(nullPayload);

        // Verificações
        assertNotNull(result);
        assertEquals(null, result.getAccountName());
        assertEquals(null, result.getEmail());
        assertEquals(null, result.getPasswordHash());
        // Nota: A entidade não possui o campo subscriptionType, ele é definido como constante no mapper
    }

    /**
     * <p>Teste para o método toData com valores nulos.</p>
     * <p>Verifica se o método toData lida corretamente com valores nulos.</p>
     * */
    @Test
    @DisplayName("Deve lidar corretamente com valores nulos ao mapear para Data")
    void toDataWithNullValuesTest() {
        // Configurando entidade com valores nulos
        UserAccountEntity nullEntity = new UserAccountEntity();

        // Execução do método a ser testado
        UserAccountData result = mapper.toData(nullEntity);

        // Verificações
        assertNotNull(result);
        assertEquals(null, result.getAccountId());
        assertEquals(null, result.getAccountName());
        assertEquals(null, result.getAccountUsername());
        assertEquals("FREE", result.getSubscriptionType());
    }
}
