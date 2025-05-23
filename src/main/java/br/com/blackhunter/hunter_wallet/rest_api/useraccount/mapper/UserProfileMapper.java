/*
 * @(#)UserProfileMapper.java
 *
 * Copyright 2025, Black Hunter
 * http://www.blackhunter.com.br
 *
 * Todos os direitos reservados.
 */

/**
 * Classe protegida - Alterações somente por CODEOWNERS.
 */

package br.com.blackhunter.hunter_wallet.rest_api.useraccount.mapper;

import br.com.blackhunter.hunter_wallet.rest_api.useraccount.dto.UserProfileData;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.entity.UserProfileEntity;
import br.com.blackhunter.hunter_wallet.rest_api.useraccount.payload.UserProfilePayload;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Interface <code>UserProfileMapper</code>
 * <p>Interface responsável pelo mapeamento entre as diferentes representações de perfil de usuário.</p>
 * <p>Utiliza o MapStruct para conversão automática entre entidades, DTOs e payloads relacionados ao perfil do usuário.</p>
 * <p>Implementa conversões entre {@link UserProfileEntity}, {@link UserProfileData} e {@link UserProfilePayload}.</p>
 * 
 * @author Black Hunter
 * @since 2025
 */
public interface UserProfileMapper {
    /**
     * Instância singleton do mapper para uso em toda a aplicação.
     */
    UserProfileMapper INSTANCE = Mappers.getMapper(UserProfileMapper.class);

    /**
     * Converte um payload de perfil de usuário para uma entidade de perfil.
     * <p>Ignora campos que não devem ser definidos durante a criação, como IDs e relacionamentos.</p>
     * 
     * @param payload O payload contendo os dados do perfil de usuário
     * @return Uma nova entidade de perfil de usuário com os dados do payload
     */
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "userAccount", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "profilePictureFile", ignore = true)
    UserProfileEntity toEntity(UserProfilePayload payload);

    /**
     * Converte uma entidade de perfil de usuário para um DTO de dados de perfil.
     * <p>Calcula campos derivados como nome completo e idade a partir dos dados da entidade.</p>
     * 
     * @param entity A entidade de perfil de usuário a ser convertida
     * @return Um DTO com os dados do perfil formatados para apresentação
     */
    @Mapping(target = "profileId", source = "profileId")
    @Mapping(target = "userId", source = "userAccount.accountId")
    @Mapping(target = "fullName", expression = "java(entity.getFirstName() + ' ' + entity.getLastName())")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "profilePictureUrl", source = "profilePictureUrl")
    @Mapping(
            target = "age",
            expression = "java(java.time.Period.between(entity.getBirthDate(), java.time.LocalDate.now()).getYears())"
    )
    UserProfileData toData(UserProfileEntity entity);

    /**
     * Atualiza uma entidade de perfil de usuário existente com dados de um payload.
     * <p>Preserva campos que não devem ser alterados durante a atualização, como IDs e relacionamentos.</p>
     * 
     * @param entity A entidade de perfil de usuário a ser atualizada
     * @param payload O payload contendo os novos dados do perfil
     */
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "userAccount", ignore = true)
    @Mapping(target = "profilePictureFile", ignore = true)
    void updateEntity(@MappingTarget UserProfileEntity entity, UserProfilePayload payload);

}
