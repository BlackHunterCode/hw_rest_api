
/*
 * @(#)UserProfileRepository.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.repository;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
    @Query("SELECT p FROM UserProfileEntity p WHERE p.userAccount.accountId = :userId")
    Optional<UserProfileEntity> findByAccountId(@Param(value = "userId") UUID userId);
}
