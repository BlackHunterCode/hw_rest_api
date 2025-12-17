package br.com.blackhunter.finey.rest.finance.financial_commitments.controller;

import br.com.blackhunter.finey.rest.core.dto.ApiResponse;
import br.com.blackhunter.finey.rest.finance.financial_commitments.dto.FinancialCommitmentData;
import br.com.blackhunter.finey.rest.finance.financial_commitments.dto.FinancialCommitmentPayload;
import br.com.blackhunter.finey.rest.finance.financial_commitments.service.FinancialCommitmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/finance/financial-commitments")
public class FinancialCommitmentController {
    private final FinancialCommitmentService financialCommitmentService;

    public FinancialCommitmentController(FinancialCommitmentService financialCommitmentService) {
        this.financialCommitmentService = financialCommitmentService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<FinancialCommitmentData>> createFinancialCommitment(@RequestBody FinancialCommitmentPayload payload) {
        FinancialCommitmentData data = this.financialCommitmentService.persist(payload, false);
        ApiResponse<FinancialCommitmentData> response = new ApiResponse<>(
                "success",
                201,
                data
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<FinancialCommitmentData>> updateFinancialCommitment(@RequestBody FinancialCommitmentPayload payload) {
        FinancialCommitmentData data = this.financialCommitmentService.persist(payload, true);
        ApiResponse<FinancialCommitmentData> response = new ApiResponse<>(
                "success",
                200,
                data
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/list-all")
    public ResponseEntity<ApiResponse<List<FinancialCommitmentData>>> listAllFinancialCommitments() {
        List<FinancialCommitmentData> dataList = this.financialCommitmentService.listAllUserCommitments();
        ApiResponse<List<FinancialCommitmentData>> response = new ApiResponse<>(
                "success",
                200,
                dataList
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/list-upcomming")
    public ResponseEntity<ApiResponse<List<FinancialCommitmentData>>> listUpCommingFinancialCommitments() {
        List<FinancialCommitmentData> dataList = this.financialCommitmentService.listUpCommingUserCommitments();
        ApiResponse<List<FinancialCommitmentData>> response = new ApiResponse<>(
                "success",
                200,
                dataList
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
