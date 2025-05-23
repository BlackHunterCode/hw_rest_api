/*
 * @(#)UserAccountMapper.java
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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * <p>Interface <code>UserAccountMapper</code>.</p>
 * <p>Interface de mapeamentos de contas de usuário.</p>
 * */
@Mapper(componentModel = "spring")
public interface UserAccountMapper {
    UserAccountMapper INSTANCE = Mappers.getMapper(UserAccountMapper.class);

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "accountName", source = "fullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "passwordHash", source = "hashedPassword")
    @Mapping(target = "accountUsername", source = "username")
    @Mapping(
            target = "accountStatus",
            expression = "java(br.com.blackhunter.hunter_wallet.rest_api.useraccount.enums.UserAccountStatus.ACTIVE)"
    )
    UserAccountEntity toEntity(UserAccountPayload reqPayload);

    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "accountName", source = "accountName")
    @Mapping(target = "accountUsername", source = "accountUsername")
    @Mapping(target = "subscriptionType", constant = "FREE")
    UserAccountData toData(UserAccountEntity entity);
}
