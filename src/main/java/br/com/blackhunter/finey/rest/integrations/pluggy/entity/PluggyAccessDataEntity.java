/*
 * @(#)PluggyAccessDataEntity.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.finey.rest.integrations.pluggy.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fn_pluggy_access_data")
@Data
public class PluggyAccessDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID accessId;
    @Lob
    @Column(name = "access_token", unique = true)
    private String accessToken;

    private LocalDateTime obtainedAt;

    public PluggyAccessDataEntity() { }

    public PluggyAccessDataEntity(String accessToken) {
        this.accessToken = accessToken;
        this.obtainedAt = LocalDateTime.now();
    }
}
