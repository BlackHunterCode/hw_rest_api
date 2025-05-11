package br.com.blackhunter.hunter_wallet.rest_api.notification.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entidade para pagina de ações de notificações.
 * Essa é a entidade base do sistema.
 * */
@Entity
@Table(name = "hw_notification_action")
@Data
public class NotificationActionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationActionId;
    @OneToOne
    @JoinColumn(name = "notificationId")
    private NotificationEntity notificationId;
    @OneToOne
    @JoinColumn(name = "accountId")
    private UserAccountEntity accountId;
    /**
     * FK genérica.
     * Recebe o id da operação que vai ser executada.
     * */
    private Integer informationId;
    private String title;
    private String description;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
