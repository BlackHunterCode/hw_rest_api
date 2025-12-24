package br.com.blackhunter.finey.rest.finance.financial_commitments.service.impl;

import br.com.blackhunter.finey.rest.auth.util.CryptUtil;
import br.com.blackhunter.finey.rest.auth.util.JwtUtil;
import br.com.blackhunter.finey.rest.core.util.Utils;
import br.com.blackhunter.finey.rest.finance.financial_commitments.dto.FinancialCommitmentData;
import br.com.blackhunter.finey.rest.finance.financial_commitments.dto.FinancialCommitmentPayload;
import br.com.blackhunter.finey.rest.finance.financial_commitments.entity.FinancialCommitmentEntity;
import br.com.blackhunter.finey.rest.finance.financial_commitments.mapper.FinancialCommitmentMapper;
import br.com.blackhunter.finey.rest.finance.financial_commitments.repository.FinancialCommitmentRepository;
import br.com.blackhunter.finey.rest.finance.financial_commitments.service.FinancialCommitmentService;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.dto.NotificationSchedulePayload;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.entity.NotificationScheduleEntity;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.enums.NotificationScheduleType;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.service.NotificationScheduleService;
import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class FinancialCommitmentServiceImpl implements FinancialCommitmentService {
    @Value("${hunter.secrets.pluggy.crypt-secret}")
    private String PLUGGY_CRYPT_SECRET;

    private final FinancialCommitmentRepository financialCommitmentRepository;
    private final FinancialCommitmentMapper financialCommitmentMapper;
    private final NotificationScheduleService notificationScheduleService;
    private final JwtUtil jwtUtil;

    public FinancialCommitmentServiceImpl(
            FinancialCommitmentRepository financialCommitmentRepository,
            FinancialCommitmentMapper financialCommitmentMapper,
            NotificationScheduleService notificationScheduleService,
            JwtUtil jwtUtil
    ) {
        this.financialCommitmentRepository = financialCommitmentRepository;
        this.financialCommitmentMapper = financialCommitmentMapper;
        this.notificationScheduleService = notificationScheduleService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public FinancialCommitmentData persist(@Validated FinancialCommitmentPayload payload, boolean update) {
        UserAccountEntity userAccount = this.jwtUtil.getUserAccountFromToken();

        FinancialCommitmentEntity entity = this.financialCommitmentMapper.toEntityEncrypted(payload);
        entity.setUserAccount(userAccount);
        if(update) {
            if(payload.getFinancialCommitmentId() == null) throw new IllegalArgumentException("financialCommitmentId cannot be null when updating a financial commitment.");
            entity.setFinancialCommitmentId(payload.getFinancialCommitmentId());
            entity.setUpdatedAt(LocalDateTime.now());
        }
        else
            entity.prePersist();

        FinancialCommitmentEntity persisted = this.financialCommitmentRepository.save(entity);
        if(persisted.getFinancialCommitmentId() == null)
            throw new RuntimeException("Error persisting financial commitment - bUpdate: " + update);
        if (persisted.getNotificationScheduleId() == null && update)
            throw new RuntimeException("Error: existing financial commitment has no associated notification schedule.");

        try {
            NotificationSchedulePayload notificationSchedulePayload = new NotificationSchedulePayload(
                    update ? persisted.getNotificationScheduleId() : null,
                    userAccount,
                    CryptUtil.encrypt(formatNotificationTitle(PLUGGY_CRYPT_SECRET, null, persisted), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(formatNotificationMessage(PLUGGY_CRYPT_SECRET, null, persisted), PLUGGY_CRYPT_SECRET),
                    CryptUtil.encrypt(persisted.getCronExpression(), PLUGGY_CRYPT_SECRET),
                    NotificationScheduleType.FINANCIAL_COMMITMENT,
                    persisted.getStartDate(),
                    persisted.getEndDate()
            );

            NotificationScheduleEntity notificationPersisted = this.notificationScheduleService.persistNotificationSchedule(notificationSchedulePayload, update);

            if(notificationPersisted.getNotificationId() == null) {
                throw new Exception("Error creating notification schedule for financial commitment");
            }

            // atualiza apenas o ID da notificação no compromisso financeiro na inserção
            if(!update) {
                persisted.setNotificationSchedule(notificationPersisted);
                if(this.financialCommitmentRepository.save(persisted).getNotificationScheduleId() == null)
                    throw new Exception("Error linking notification schedule to financial commitment");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return this.financialCommitmentMapper.toDataEncrypted(persisted);
    }

    @Override
    public List<FinancialCommitmentData> listAllUserCommitments() {
        return this.financialCommitmentRepository.findAllByUserAccount(
                this.jwtUtil.getUserAccountFromToken().getAccountId()
        ).stream()
                .map(this.financialCommitmentMapper::toDataEncrypted)
                .toList();
    }

    @Override
    public List<FinancialCommitmentData> listUpCommingUserCommitments() {
        return this.financialCommitmentRepository.findUpCommingUserCommitments(
                this.jwtUtil.getUserAccountFromToken().getAccountId(),
                LocalDateTime.now().plusDays(5)
        ).stream()
                .map(this.financialCommitmentMapper::toDataEncrypted)
                .toList();
    }

    public static String formatNotificationTitle(
            String decryptKey,
            NotificationScheduleEntity notificationSchedule,
            FinancialCommitmentEntity financialCommitmentEntity) throws Exception {

        LocalDate today = LocalDate.now();

        // Se não tem notification schedule (primeira criação)
        LocalDateTime nextExecution = notificationSchedule == null
                ? financialCommitmentEntity.getStartDate()
                : notificationSchedule.getNextExecutionDate();

        // Se não houver nextExecution definido, usa o fim do compromisso
        LocalDateTime dueDateTime = Optional.ofNullable(nextExecution)
                .orElse(financialCommitmentEntity.getEndDate());

        if (dueDateTime == null) {
            // Evita NullPointer em casos imprevistos
            throw new IllegalStateException("Expiration date cannot be null");
        }

        LocalDate dueDate = dueDateTime.toLocalDate();

        String name = CryptUtil.decrypt(financialCommitmentEntity.getCommitmentName(), decryptKey);
        String value = Utils.formatAmountInReal(CryptUtil.decrypt(financialCommitmentEntity.getValue(), decryptKey));

        boolean isRecurring = !Utils.isEmptyOrNull(financialCommitmentEntity.getCronExpression());
        boolean hasNoEndDate = financialCommitmentEntity.getEndDate() == null;

        if (isRecurring && hasNoEndDate) {
            // 💡 Recorrente indefinido — foca sempre no próximo evento
            long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
            if (daysUntilDue == 0) {
                return String.format("Hoje é dia de pagar: %s - %s", name, value);
            } else if (daysUntilDue > 0) {
                return String.format("Próximo pagamento em %d dias: %s - %s", daysUntilDue, name, value);
            } else {
                return String.format("Pagamento recente: %s - %s", name, value);
            }
        }

        // 🔹 Caso normal (com data final ou único)
        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

        if (daysUntilDue < 0) {
            return String.format("Pagamento ATRASADO: %s - %s", name, value);
        } else if (daysUntilDue == 0) {
            return String.format("Pagar HOJE: %s - %s", name, value);
        } else if (daysUntilDue == 1) {
            return String.format("Amanhã é dia de pagar: %s - %s", name, value);
        } else if (daysUntilDue == 5) {
            return String.format("Faltam 5 dias para pagar: %s - %s", name, value);
        } else if (daysUntilDue == 7) {
            return String.format("Pagamento em 7 dias: %s - %s", name, value);
        } else {
            return String.format("Lembrete: %s vence em %d dias - %s", name, daysUntilDue, value);
        }
    }

    public static String formatNotificationMessage(
            String decryptKey,
            NotificationScheduleEntity notificationSchedule,
            FinancialCommitmentEntity financialCommitmentEntity) throws Exception {

        LocalDate today = LocalDate.now();

        // Se a notificação ainda não existe, usamos a data inicial do compromisso
        LocalDateTime nextExecution = notificationSchedule == null
                ? financialCommitmentEntity.getStartDate()
                : notificationSchedule.getNextExecutionDate();

        // Fallback para endDate (ou erro controlado se for null)
        LocalDateTime dueDateTime = Optional.ofNullable(nextExecution)
                .orElse(financialCommitmentEntity.getEndDate());

        if (dueDateTime == null) {
            throw new IllegalStateException("Expiration data cannot be null to generate notification message.");
        }

        LocalDate dueDate = dueDateTime.toLocalDate();

        String name = CryptUtil.decrypt(financialCommitmentEntity.getCommitmentName(), decryptKey);
        String value = Utils.formatAmountInReal(CryptUtil.decrypt(financialCommitmentEntity.getValue(), decryptKey));

        boolean isRecurring = !Utils.isEmptyOrNull(financialCommitmentEntity.getCronExpression());
        boolean hasNoEndDate = financialCommitmentEntity.getEndDate() == null;

        // 🔁 Recorrente sem data final
        if (isRecurring && hasNoEndDate) {
            long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
            String dayOfWeek = dueDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

            if (daysUntilDue == 0) {
                return String.format("💰 Hoje é o dia do seu pagamento recorrente de %s no valor de %s.", name, value);
            } else if (daysUntilDue > 0) {
                return String.format("🔁 Seu próximo pagamento de %s será na %s, no valor de %s.", name, dayOfWeek, value);
            } else {
                return String.format("📆 Pagamento recorrente recente: %s - %s.", name, value);
            }
        }

        // 🔹 Caso normal (com data final ou único)
        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

        if (daysUntilDue < 0) {
            return String.format("⚠️ O pagamento de %s no valor de %s está atrasado desde %s.",
                    name, value, Utils.formatDate(dueDate));
        } else if (daysUntilDue == 0) {
            return String.format("💰 Hoje é o dia de pagar %s. Valor: %s.", name, value);
        } else if (daysUntilDue == 1) {
            return String.format("⏰ Amanhã vence seu compromisso %s no valor de %s.", name, value);
        } else if (daysUntilDue == 5) {
            return String.format("📅 Faltam 5 dias para o pagamento de %s. Valor: %s.", name, value);
        } else if (daysUntilDue == 7) {
            return String.format("🗓️ Em 7 dias vence o pagamento de %s no valor de %s.", name, value);
        } else {
            return String.format("📆 Seu compromisso %s vence em %d dias. Valor: %s.", name, daysUntilDue, value);
        }
    }

}
