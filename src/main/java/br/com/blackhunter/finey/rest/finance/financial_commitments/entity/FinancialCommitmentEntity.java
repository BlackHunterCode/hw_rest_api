package br.com.blackhunter.finey.rest.finance.financial_commitments.entity;

import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.entity.NotificationScheduleEntity;
import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fn_financial_commitments")
@Data
@NoArgsConstructor
public class FinancialCommitmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "financial_commitment_id", updatable = false, nullable = false)
    private UUID financialCommitmentId;

    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccountEntity userAccount;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_schedule_id", nullable = false)
    private NotificationScheduleEntity notificationSchedule;

    @Column(name = "commitment_name", nullable = false)
    private String commitmentName;
    @Column(name = "commitment_description", length = 400)
    private String commitmentDescription;
    @Column(name = "commitment_type", nullable = false)
    private String commitmentType;

    @Column(name = "commitment_value", nullable = false, length = 50)
    private String value;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "cron_expression", nullable = false, length = 20)
    private String cronExpression;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getNotificationScheduleId() {
        return notificationSchedule != null ? notificationSchedule.getNotificationId() : null;
    }
}
