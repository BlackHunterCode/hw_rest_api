/*
 * @(#)TransactionService.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.finey.rest.finance.transaction.service;

import br.com.blackhunter.finey.rest.core.dto.TransactionPeriodDate;
import br.com.blackhunter.finey.rest.finance.transaction.dto.TotalTransactionsPeriod;
import br.com.blackhunter.finey.rest.finance.transaction.dto.TransactionData;
import br.com.blackhunter.finey.rest.finance.transaction.payload.TransactionPayload;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    TransactionData registerTransaction(TransactionPayload transactionPayload);
    TotalTransactionsPeriod getTotalTransactionsPeriod(List<String> bankAccountIds, LocalDate referenceDateMonthYear, LocalDate startDate, LocalDate endDate);

    List<TransactionData> getAllTransactionsPeriodByAccountId(String accountId, TransactionPeriodDate periodDate);
    List<TransactionData> getAllTransactionsPeriodByAccountId(String accountId, TransactionPeriodDate periodDate, LocalDate referenceDateMonthYear, LocalDate startDate, LocalDate endDate);
}
