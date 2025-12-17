package br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.service;

import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.dto.NotificationSchedulePayload;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.entity.NotificationScheduleEntity;

public interface NotificationScheduleService {
    // esse método deve ser usado internamente e nunca exposto via API
    /**
     * Persiste uma entidade de agendamento de notificação.
     * Caso seja uma inserção (novo agendamento), o método cria uma nova entidade de agendamento de notificação no banco de dados.
     * Caso seja uma atualização, o método atualiza os dados da entidade de agendamento de notificação existente.
     *
     * @param payload Os dados do agendamento de notificação a serem persistidos.
     * @param update Indica se a operação é uma atualização (true) ou uma inserção (false).
     * @return A entidade de agendamento de notificação persistida.
     * */
    NotificationScheduleEntity persistNotificationSchedule(NotificationSchedulePayload payload, boolean update);
}
