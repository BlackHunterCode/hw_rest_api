/*
 * @(#)UserAccountEntity.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.finey.rest.useraccount.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.blackhunter.finey.rest.finance.financial_commitments.entity.FinancialCommitmentEntity;
import br.com.blackhunter.finey.rest.finance.goal.entity.GoalEntity;
import br.com.blackhunter.finey.rest.finance.transaction.entity.TransactionEntity;
import br.com.blackhunter.finey.rest.integrations.pluggy.entity.PluggyItemEntity;
import br.com.blackhunter.finey.rest.scheduled_task.push_notification.notification_schedule.entity.NotificationScheduleEntity;
import br.com.blackhunter.finey.rest.useraccount.enums.UserAccountStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

/**
 * <p>Classe <code>UserAccountEntity</code>.</p>
 * <p>Entidade de conta de usuário.</p>
 * <p>Essa é a entidade base do sistema.</p>
 * */
@Entity
@Table(name = "fn_useraccounts")
@Data
public class UserAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID accountId;

    private String accountName;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    private String accountUsername;

    @Enumerated(EnumType.STRING)
    private UserAccountStatus accountStatus;

    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endDateTimeOfTutorialPeriod;
    private LocalDateTime lastLoginAt;

    @OneToOne(mappedBy = "userAccount", cascade = CascadeType.ALL)
    @ToString.Exclude
    private UserProfileEntity userProfile;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<TransactionEntity> transactions = new HashSet<>();

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<PluggyItemEntity> pluggyItems = new HashSet<>();

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<GoalEntity> goals = new HashSet<>();

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<FinancialCommitmentEntity> financialCommitments = new HashSet<>();

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<NotificationScheduleEntity> notificationSchedules = new HashSet<>();

    /**
     * Construtor padrão da classe.
     * */
    public UserAccountEntity() {

    }
}
