package br.com.blackhunter.finey.rest.finance.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Classe de DTO para a entidade Goal.
 * Essa classe carrega dados criptografados.
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoalData {
    private UUID goalId;
    @JsonProperty("goalName")
    private String goalNameEncrypted;
    @JsonProperty("goalDescription")
    private String goalDescriptionEncrypted;
    @JsonProperty("goalIcon")
    private String goalIconEncrypted;
    @JsonProperty("goalColor")
    private String goalColorEncrypted;
    @JsonProperty("dGoalTargetAmount")
    private String dGoalTargetAmountEncrypted;
    @JsonProperty("goalDate")
    private LocalDate goalDate;
}
