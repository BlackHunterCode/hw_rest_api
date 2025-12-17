package br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.repository;

import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.entity.NotificationScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationScheduleRepository extends JpaRepository<NotificationScheduleEntity, UUID> {
}
