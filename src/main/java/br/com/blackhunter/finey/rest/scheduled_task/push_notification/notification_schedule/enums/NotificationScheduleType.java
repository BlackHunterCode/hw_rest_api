package br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.enums;

import br.com.blackhunter.finey.rest.finance.financial_commitments.entity.FinancialCommitmentEntity;

public enum NotificationScheduleType {
    FINANCIAL_COMMITMENT("FINANCIAL_COMMITMENT", FinancialCommitmentEntity.class);
    private final String type;
    private final Class<?> associatedClass;

    NotificationScheduleType(String type, Class<?> associatedClass) {
        this.type = type;
        this.associatedClass = associatedClass;
    }
}
