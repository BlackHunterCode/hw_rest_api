package br.com.blackhunter.finey.rest.finance.analysis.controller;

import br.com.blackhunter.finey.rest.core.dto.ApiResponse;
import br.com.blackhunter.finey.rest.finance.analysis.dto.payload.AnalysisPayload;
import br.com.blackhunter.finey.rest.finance.analysis.dto.score.FinancialScorePeriodDTO;
import br.com.blackhunter.finey.rest.finance.analysis.service.FinancialScorePeriodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/finance/analysis")
public class AnalysisController {
    private FinancialScorePeriodService financialScorePeriodService;

    public AnalysisController(FinancialScorePeriodService financialScorePeriodService) {
        this.financialScorePeriodService = financialScorePeriodService;
    }

    @PostMapping("/financial-score")
    public ResponseEntity<ApiResponse<FinancialScorePeriodDTO>> getFinancialScore(
            @RequestBody AnalysisPayload payload ) {
        FinancialScorePeriodDTO data = this.financialScorePeriodService.getFinancialScorePeriod(
                payload.getBankAccountIds(),
                payload.getReferenceDateMonthYear(),
                payload.getStartDate(),
                payload.getEndDate()
        );

        ApiResponse<FinancialScorePeriodDTO> response = new ApiResponse<>(
                "success",
                200,
                data
        );

        return ResponseEntity.ok(response);
    }
}
