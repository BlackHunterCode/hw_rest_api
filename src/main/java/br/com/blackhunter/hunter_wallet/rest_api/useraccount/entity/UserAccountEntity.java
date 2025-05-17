/**
 * 2025 © Black Hunter - Todos os Direitos Reservados.
 *
 * Classe protegida - Aletrações somente por CODEOWNERS.
 * */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de conta de usuário.
 * Essa é a entidade base do sistema.
 * */
@Entity
@Table(name = "hw_useraccounts")
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

    /**
     * Construtor padrão da classe.
     * */
    public UserAccountEntity() {

    }
}
