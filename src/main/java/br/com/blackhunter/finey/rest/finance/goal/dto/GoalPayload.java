package br.com.blackhunter.finey.rest.finance.goal.dto;

import br.com.blackhunter.finey.rest.auth.util.CryptUtil;
import br.com.blackhunter.finey.rest.core.annotations.Encrypted;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Classe de Payload para a entidade Goal.
 * Essa classe requer dados criptografados.
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoalPayload {
    // nulo quando for uma nova meta
    private UUID goalId;
    @Encrypted
    @NotBlank(message = "the goal name field is mandatory.")
    private String goalName;
    @Encrypted
    @NotBlank(message = "the goal description field is mandatory.")
    private String goalDescription;
    @Encrypted
    @NotBlank(message = "the goal icon field is mandatory.")
    private String goalIcon;
    @Encrypted
    @NotBlank(message = "the goal color field is mandatory.")
    private String goalColor;
    @Encrypted
    @NotBlank(message = "the goal target amount field is mandatory.")
    private String goalTargetAmount;

    private LocalDate goalDate;
}
