package br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * <p>Classe <code>UserProfileEntity</code>.</p>
 * <p>Entidade de perfil de usuário.</p>
 * */
@Entity
@Table(name = "hw_userprofiles")
@Data
public class UserProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID profileId;

    @OneToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccountEntity userAccount;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthDate;
    @Lob
    private byte[] profilePictureFile;

    public UserProfileEntity() {

    }
}
