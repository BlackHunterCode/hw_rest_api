package br.com.blackhunter.hunter_wallet.rest_api.notification.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entidade de notifições.
 * Essa é a entidade base do sistema.
 * */
@Entity
@Table(name = "hw_notification")
@Data
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;
    @OneToOne
    @Column(name = "accountId")
    private UserAccountEntity accountId;
    private Integer severity;
    private String title;
    private String subTitle;
    private String description;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
