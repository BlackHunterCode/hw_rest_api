package br.com.blackhunter.finey.rest.finance.financial_commitments.dto;

import br.com.blackhunter.finey.rest.core.annotations.Encrypted;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialCommitmentPayload {
    // nulo quando for um novo compromisso financeiro
    private UUID financialCommitmentId;

    @Encrypted
    @NotBlank(message = "the \"commitmentName\" field is required.")
    private String commitmentName;
    @Encrypted
    @NotBlank(message = "the \"commitmentDescription\" field is required.")
    private String commitmentDescription;
    @Encrypted
    @NotBlank(message = "the \"commitmentType\" field is required.")
    private String commitmentType;
    @Encrypted
    @NotBlank(message = "the \"cronExpression\" field is required.")
    private String cronExpression;
    @Encrypted
    @NotBlank(message = "the \"value\" field is required.")
    private String value;
    @NotNull(message = "the \"startDate\" field is required.")
    private LocalDateTime startDate;
    @NotNull(message = "the \"endDate\" field is required.")
    private LocalDateTime endDate;
}
