package br.com.blackhunter.finey.rest.finance.financial_commitments.repository;

import br.com.blackhunter.finey.rest.finance.financial_commitments.entity.FinancialCommitmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FinancialCommitmentRepository extends JpaRepository<FinancialCommitmentEntity, UUID> {
    @Query("SELECT fce FROM FinancialCommitmentEntity fce WHERE fce.userAccount.accountId = :accountId")
    List<FinancialCommitmentEntity> findAllByUserAccount(UUID accountId);

    @Query(
            "SELECT fce " +
            "  FROM FinancialCommitmentEntity fce " +
            " WHERE fce.userAccount.accountId = :accountId AND fce.endDate BETWEEN CURRENT_TIMESTAMP AND :dataLimite\n"
    )
    List<FinancialCommitmentEntity> findUpCommingUserCommitments(UUID accountId, LocalDateTime dataLimite);
}
