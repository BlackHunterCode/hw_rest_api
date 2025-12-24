package br.com.blackhunter.finey.rest.finance.transaction.repository.custom;

import br.com.blackhunter.finey.rest.finance.transaction.entity.TransactionEntity;

import java.util.List;

public interface TransactionRepositoryCustom {
    void upsertAll(List<TransactionEntity> transactions);
}
