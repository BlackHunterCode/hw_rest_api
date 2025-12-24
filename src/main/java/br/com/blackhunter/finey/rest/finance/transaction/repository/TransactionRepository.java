/*
 * @(#)TransactionRepository.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.finey.rest.finance.transaction.repository;

import br.com.blackhunter.finey.rest.finance.transaction.entity.TransactionEntity;
import br.com.blackhunter.finey.rest.finance.transaction.repository.custom.TransactionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID>, TransactionRepositoryCustom {
    @Query("SELECT t FROM TransactionEntity t WHERE t.pluggyAccountId.pluggyAccountId = :accountId AND t.transactionLocalDate BETWEEN :startDate AND :endDate")
    List<TransactionEntity> findAllByFinancialAccountIdAndDateBetween(
            @Param("accountId") UUID accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t FROM TransactionEntity t WHERE t.pluggyAccountId.pluggyAccountId = :accountId")
    Optional<TransactionEntity> findByFinancialAccountId(
            @Param("accountId") UUID accountId
    );
}
