package br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.entity;

import br.com.blackhunter.finey.rest.finance.financial_commitments.entity.FinancialCommitmentEntity;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.enums.NotificationScheduleType;
import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "fn_notification_schedules")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_schedule_id", updatable = false, nullable = false)
    private UUID notificationId;

    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccountEntity userAccount;

    @Column(name = "title", nullable = false, length = 40)
    private String title;

    @Column(name = "message", nullable = false, length = 400)
    private String message;

    // expressão cron, ex: "0 0 10 12 * ?" (todo dia 12 às 10h)
    @Column(name = "cron_expression", nullable = false, length = 20)
    private String cronExpression;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;  // data inicial da recorrência
    @Column(name = "end_date")
    private LocalDateTime endDate;    // pode ser null (sem fim)
    @Column(name = "next_execution_date")
    private LocalDateTime nextExecutionDate; // próxima execução agendada

    @Column(name = "active", nullable = false)
    private boolean active = true;

    // controla se já foi executada (no caso de notificação única)
    @Column(name = "executed")
    private boolean executed = false;

    // tipo de recorrência (ex: SINGLE, RECURRING)
    @Enumerated(EnumType.STRING)
    private NotificationScheduleType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // relacionamentos
    @OneToOne(mappedBy = "notificationSchedule", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private FinancialCommitmentEntity financialCommitment;

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
