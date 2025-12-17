package br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.mapper;

import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.dto.NotificationSchedulePayload;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.entity.NotificationScheduleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NotificationScheduleMapper {
    NotificationScheduleMapper INSTANCE = Mappers.getMapper(NotificationScheduleMapper.class);

    @Mapping(target = "notificationId", ignore = true)
    @Mapping(target = "userAccount", source = "userAccount")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "cronExpression", source = "cronExpression")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "nextExecutionDate", source = "startDate")
    @Mapping(target = "type", source = "type")
    NotificationScheduleEntity toEntityEncrypted(NotificationSchedulePayload payload);
}
