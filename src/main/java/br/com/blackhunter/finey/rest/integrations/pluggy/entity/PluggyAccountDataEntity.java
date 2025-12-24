package br.com.blackhunter.finey.rest.integrations.pluggy.entity;

import br.com.blackhunter.finey.rest.finance.transaction.entity.TransactionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "fn_pluggy_account_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluggyAccountDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID pluggyAccountId;

    @ManyToOne
    @JoinColumn(name = "pluggy_item_id", nullable = false)
    private PluggyItemEntity itemId;

    @OneToMany(mappedBy = "pluggyAccountId", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<TransactionEntity> transactions;

    @Column(nullable = false, name = "original_pluggy_account_id", unique = true)
    private String pluggyOriginalAccountId;

    @Column(nullable = false, name = "pluggy_account_name")
    private String pluggyAccountName;

    @Column(nullable = false, name = "pluggy_account_type")
    private String pluggyAccountType;

    @Column(nullable = false, name = "pluggy_account_balance")
    private String pluggyAccountBalance;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
}
