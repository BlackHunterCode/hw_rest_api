/*
 * @(#)UserProfileEntity.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.finey.rest.useraccount.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * <p>Classe <code>UserProfileEntity</code>.</p>
 * <p>Entidade de perfil de usuário.</p>
 * */
@Entity
@Table(name = "fn_userprofiles")
@Data
public class UserProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID profileId;

    @OneToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccountEntity userAccount;

    @Column(nullable = false, length = 80)
    private String firstName;
    @Column(nullable = false, length = 80)
    private String lastName;
    @Column(nullable = false, length = 20)
    private String phoneNumber;
    @Column(nullable = false)
    private LocalDate birthDate;
    @Lob
    private byte[] profilePictureFile;

    public UserProfileEntity() {

    }
}
