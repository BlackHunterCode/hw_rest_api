package br.com.blackhunter.finey.rest.finance.transaction.controller;

import br.com.blackhunter.finey.rest.core.dto.ApiResponse;
import br.com.blackhunter.finey.rest.finance.transaction.dto.TotalTransactionPeriodPayload;
import br.com.blackhunter.finey.rest.finance.transaction.dto.TotalTransactionsPeriod;
import br.com.blackhunter.finey.rest.finance.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/finance/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/total-period")
    @Deprecated
    public ResponseEntity<ApiResponse<TotalTransactionsPeriod>> getTotalTransactionsPeriod(@RequestBody TotalTransactionPeriodPayload payload) {
        TotalTransactionsPeriod totalTransactionsPeriod = transactionService.getTotalTransactionsPeriod(
                payload.getBankAccountIds(), payload.getReferenceDateMonthYear(), payload.getStartDate(), payload.getEndDate()
        );

        ApiResponse<TotalTransactionsPeriod> response = new ApiResponse<>(
                "success",
                200,
                totalTransactionsPeriod
        );

        return ResponseEntity.ok(response);
    }
}
