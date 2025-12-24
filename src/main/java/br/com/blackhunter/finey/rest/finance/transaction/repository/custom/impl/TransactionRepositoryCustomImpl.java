package br.com.blackhunter.finey.rest.finance.transaction.repository.custom.impl;

import br.com.blackhunter.finey.rest.finance.transaction.entity.TransactionEntity;
import br.com.blackhunter.finey.rest.finance.transaction.repository.custom.TransactionRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Slf4j
public class TransactionRepositoryCustomImpl implements TransactionRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void upsertAll(List<TransactionEntity> transactions) {
        for (int i = 0; i < transactions.size(); i++) {
            TransactionEntity tx = transactions.get(i);

            // checa se ja existe pelo providerTransactionId
            TypedQuery<TransactionEntity> query = entityManager.createQuery(
                    "SELECT t FROM TransactionEntity t WHERE t.providerTransactionId = :pid",
                    TransactionEntity.class
            );
            query.setParameter("pid", tx.getProviderTransactionId());
            List<TransactionEntity> existing = query.getResultList();

            if (existing.isEmpty()) {
                entityManager.persist(tx);
            } else {
                TransactionEntity existingTx = existing.get(0);
                existingTx.setStatus(tx.getStatus());
                existingTx.setCategory(tx.getCategory());
                existingTx.setBalance(tx.getBalance());
                // ... outros campos que podem mudar
                entityManager.merge(existingTx);
            }

            // flush batch a cada 50 inserts para performance
            if (i % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }

            log.debug("Upserted transaction {}/{}: {}", i + 1, transactions.size(), tx.getProviderTransactionId());
        }
    }
}
