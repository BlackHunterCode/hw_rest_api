package br.com.blackhunter.finey.rest.scheduled_task.push_notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationsScheduledTasks {
    private final PushNotificationModuleFactory pushNotificationModuleFactory;

    public PushNotificationsScheduledTasks(PushNotificationModuleFactory pushNotificationModuleFactory) {
        this.pushNotificationModuleFactory = pushNotificationModuleFactory;
    }

    @Scheduled(fixedRate = 60000)
    public void runPushNotificationsScheduledTasks() {
        pushNotificationModuleFactory.getPushNotificationModules()
                .forEach(PushNotificationModule::runTask);
    }
}
