package br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.service.impl;

import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.dto.NotificationSchedulePayload;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.entity.NotificationScheduleEntity;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.mapper.NotificationScheduleMapper;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.repository.NotificationScheduleRepository;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.service.NotificationScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Service
public class NotificationScheduleServiceImpl implements NotificationScheduleService {
    private final NotificationScheduleRepository notificationScheduleRepository;
    private final NotificationScheduleMapper notificationScheduleMapper;

    private NotificationScheduleServiceImpl(
            NotificationScheduleRepository notificationScheduleRepository,
            NotificationScheduleMapper notificationScheduleMapper
            ) {
        this.notificationScheduleRepository = notificationScheduleRepository;
        this.notificationScheduleMapper = notificationScheduleMapper;
    }
    @Override
    public NotificationScheduleEntity persistNotificationSchedule(@Validated NotificationSchedulePayload payload, boolean update) {
        NotificationScheduleEntity entity = this.notificationScheduleMapper.toEntityEncrypted(payload);
        if (update) {
            if(payload.getNotificationScheduleId() == null) throw new IllegalArgumentException("ID must be provided for update operation");
            entity.setNotificationId(payload.getNotificationScheduleId());
            entity.setUpdatedAt(LocalDateTime.now());
        }
        else
            entity.prePersist();

        return this.notificationScheduleRepository.save(entity);
    }
}
