/*
 * @(#)UserAccountRepositoryTest.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Classe <code>UserAccountRepositoryTest</code>.</p>
 * <p>Classe de testes unitários para o repositório de contas de usuário.</p>
 * */
@DataJpaTest
public class UserAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     * <p>Teste para o método existsByEmail quando o e-mail existe.</p>
     * <p>Verifica se o método existsByEmail retorna true quando o e-mail existe no banco de dados.</p>
     * */
    @Test
    @DisplayName("Deve retornar true quando o e-mail existe")
    void existsByEmailWhenEmailExists() {
        // Criando uma entidade para o teste
        UserAccountEntity entity = new UserAccountEntity();
        // Não definimos o ID manualmente, pois ele é gerado automaticamente
        entity.setAccountName("Usuário Teste");
        entity.setEmail("usuario.existente@example.com");
        entity.setPasswordHash("senhaHasheada123");
        entity.setAccountUsername("usuario.teste");
        entity.setAccountStatus(UserAccountStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());

        // Persistindo a entidade
        entityManager.persist(entity);
        entityManager.flush();

        // Execução do método a ser testado
        boolean result = userAccountRepository.existsByEmail("usuario.existente@example.com");

        // Verificação
        assertTrue(result);
    }

    /**
     * <p>Teste para o método existsByEmail quando o e-mail não existe.</p>
     * <p>Verifica se o método existsByEmail retorna false quando o e-mail não existe no banco de dados.</p>
     * */
    @Test
    @DisplayName("Deve retornar false quando o e-mail não existe")
    void existsByEmailWhenEmailDoesNotExist() {
        // Execução do método a ser testado
        boolean result = userAccountRepository.existsByEmail("email.inexistente@example.com");

        // Verificação
        assertFalse(result);
    }

    /**
     * <p>Teste para o método existsByEmail com e-mail case-sensitive.</p>
     * <p>Verifica se o método existsByEmail é case-sensitive ao verificar e-mails.</p>
     * */
    @Test
    @DisplayName("Deve ser case-sensitive ao verificar e-mails")
    void existsByEmailCaseSensitive() {
        // Criando uma entidade para o teste
        UserAccountEntity entity = new UserAccountEntity();
        // Não definimos o ID manualmente, pois ele é gerado automaticamente
        entity.setAccountName("Usuário Teste");
        entity.setEmail("usuario.case@example.com");
        entity.setPasswordHash("senhaHasheada123");
        entity.setAccountUsername("usuario.teste");
        entity.setAccountStatus(UserAccountStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());

        // Persistindo a entidade
        entityManager.persist(entity);
        entityManager.flush();

        // Execução do método a ser testado com e-mail em maiúsculo
        boolean result = userAccountRepository.existsByEmail("USUARIO.CASE@EXAMPLE.COM");

        // Verificação - dependendo da implementação da consulta, isso pode variar
        // Aqui estamos assumindo que a consulta é case-sensitive
        assertFalse(result);
    }

    /**
     * <p>Teste para o método existsByEmail com e-mail vazio.</p>
     * <p>Verifica se o método existsByEmail lida corretamente com e-mail vazio.</p>
     * */
    @Test
    @DisplayName("Deve lidar corretamente com e-mail vazio")
    void existsByEmailWithEmptyEmail() {
        // Execução do método a ser testado
        boolean result = userAccountRepository.existsByEmail("");

        // Verificação
        assertFalse(result);
    }
}
