package br.com.blackhunter.finey.rest.scheduled_task.push_notification;

import br.com.blackhunter.finey.rest.scheduled_task.push_notification.module.FinancialCommitmentPushNotificationModule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PushNotificationModuleFactory {
    private FinancialCommitmentPushNotificationModule financialCommitmentPushNotificationModule;

    public PushNotificationModuleFactory(FinancialCommitmentPushNotificationModule financialCommitmentPushNotificationModule) {
        this.financialCommitmentPushNotificationModule = financialCommitmentPushNotificationModule;
    }

    /**
     * Esse método retorna uma lista de módulos de notificação push. <br>
     * Cada módulo implementa a interface PushNotificationModule, permitindo a execução de tarefas específicas de notificação push. <br>
     * */
    public List<PushNotificationModule> getPushNotificationModules() {
        return List.of(
                this.financialCommitmentPushNotificationModule
        );
    }
}
