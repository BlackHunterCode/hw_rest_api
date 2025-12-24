package br.com.blackhunter.finey.rest.integrations.pluggy.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "fn_pluggy_items")
@Data
@AllArgsConstructor
public class PluggyItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id")
    private UUID itemId;
    
    @Column(name = "original_pluggy_item_id", unique = true)
    private String originalPluggyItemId;

    @Column(name = "connector_id", nullable = false)
    private String connectorId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccountEntity userAccount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PluggyItemEntity() { }
}
