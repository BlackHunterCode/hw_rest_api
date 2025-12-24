package br.com.blackhunter.finey.rest.finance.financial_commitments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialCommitmentData {
    private UUID financialCommitmentId;
    @JsonProperty("commitmentName")
    private String commitmentNameEncrypted;
    @JsonProperty("commitmentDescription")
    private String commitmentDescriptionEncrypted;
    @JsonProperty("commitmentType")
    private String commitmentTypeEncrypted;
    @JsonProperty("cronExpression")
    private String cronExpressionEncrypted;
    @JsonProperty("value")
    private String valueEncrypted;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
