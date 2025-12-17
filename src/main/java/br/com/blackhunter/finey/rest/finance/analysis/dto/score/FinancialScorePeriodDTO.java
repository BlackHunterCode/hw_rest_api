package br.com.blackhunter.finey.rest.finance.analysis.dto.score;

import br.com.blackhunter.finey.rest.finance.analysis.dto.insights.InsightDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialScorePeriodDTO {
    @JsonProperty(value = "period")
    private String periodEcrypted;
    @JsonProperty(value = "score")
    private String scoreEncrypted;
    @JsonProperty(value = "details")
    private String detailsEncrypted;
    @JsonProperty(value = "percentage")
    private String percentageEncrypted;
    @JsonProperty(value = "daysOfControl")
    private String daysOfControlEncrypted;
    private List<InsightDTO> insights;
}
