package br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.dto;

import br.com.blackhunter.finey.rest.core.annotations.Encrypted;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.enums.NotificationScheduleType;
import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
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
public class NotificationSchedulePayload {
    // nulo quando for um novo agendamento de notificação
    private UUID notificationScheduleId;

    @NotNull(message = "the \"userAccount\" field is required.")
    private UserAccountEntity userAccount;
    @NotBlank(message = "the \"title\" field is required.")
    @Encrypted
    private String title;
    @NotBlank(message = "the \"message\" field is required.")
    @Encrypted
    private String message;
    @NotBlank(message = "the \"cronExpression\" field is required.")
    @Encrypted
    private String cronExpression;
    @NotNull(message = "the \"type\" field is required.")
    private NotificationScheduleType type;
    @NotNull(message = "the \"startDate\" field is required.")
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
