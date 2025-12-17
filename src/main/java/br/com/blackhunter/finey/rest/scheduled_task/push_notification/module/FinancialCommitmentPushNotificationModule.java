package br.com.blackhunter.finey.rest.scheduled_task.push_notification.module;

import br.com.blackhunter.finey.rest.finance.financial_commitments.service.FinancialCommitmentService;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.PushNotificationModule;
import org.springframework.stereotype.Service;

@Service
public class FinancialCommitmentPushNotificationModule implements PushNotificationModule {
    private final FinancialCommitmentService financialCommitmentService;

    public FinancialCommitmentPushNotificationModule(FinancialCommitmentService financialCommitmentService) {
        this.financialCommitmentService = financialCommitmentService;
    }

    @Override
    public void runTask() {
        System.out.println("Tarefas agendadas de notificações de compromissos financeiros iniciadas.");
    }
}
