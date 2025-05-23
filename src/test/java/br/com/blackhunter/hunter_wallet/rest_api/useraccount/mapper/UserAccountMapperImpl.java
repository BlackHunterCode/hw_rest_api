/*
 * @(#)UserAccountMapperImpl.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.mapper;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserAccountData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserAccountEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserAccountPayload;
import org.springframework.stereotype.Component;

/**
 * <p>Classe <code>UserAccountMapperImpl</code>.</p>
 * <p>Implementação de teste para o UserAccountMapper.</p>
 * */
@Component
public class UserAccountMapperImpl implements UserAccountMapper {

    @Override
    public UserAccountEntity toEntity(UserAccountPayload reqPayload) {
        if (reqPayload == null) {
            return null;
        }

        UserAccountEntity entity = new UserAccountEntity();
        entity.setAccountName(reqPayload.getFullName());
        entity.setEmail(reqPayload.getEmail());
        entity.setPasswordHash(reqPayload.getHashedPassword());
        
        return entity;
    }

    @Override
    public UserAccountData toData(UserAccountEntity entity) {
        if (entity == null) {
            return null;
        }

        UserAccountData data = new UserAccountData();
        data.setAccountId(entity.getAccountId());
        data.setAccountName(entity.getAccountName());
        data.setAccountUsername(entity.getAccountUsername());
        data.setSubscriptionType("FREE");
        
        return data;
    }
}
