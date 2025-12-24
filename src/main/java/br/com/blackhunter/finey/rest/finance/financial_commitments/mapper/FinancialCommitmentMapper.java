package br.com.blackhunter.finey.rest.finance.financial_commitments.mapper;

import br.com.blackhunter.finey.rest.finance.financial_commitments.dto.FinancialCommitmentData;
import br.com.blackhunter.finey.rest.finance.financial_commitments.dto.FinancialCommitmentPayload;
import br.com.blackhunter.finey.rest.finance.financial_commitments.entity.FinancialCommitmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * interface <code>FinancialCommitmentMapper</code>.
 * Interface de mapeamentos de compromissos financeiros do usuário.
 * */
@Mapper(componentModel = "spring")
public interface FinancialCommitmentMapper {
    FinancialCommitmentMapper INSTANCE = Mappers.getMapper(FinancialCommitmentMapper.class);

    @Mapping(target = "commitmentName", source = "commitmentName")
    @Mapping(target = "commitmentDescription", source = "commitmentDescription")
    @Mapping(target = "commitmentType", source = "commitmentType")
    @Mapping(target = "cronExpression", source = "cronExpression")
    @Mapping(target = "value", source = "value")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    FinancialCommitmentEntity toEntityEncrypted(FinancialCommitmentPayload payload);

    @Mapping(target = "financialCommitmentId", source = "financialCommitmentId")
    @Mapping(target = "commitmentNameEncrypted", source = "commitmentName")
    @Mapping(target = "commitmentDescriptionEncrypted", source = "commitmentDescription")
    @Mapping(target = "commitmentTypeEncrypted", source = "commitmentType")
    @Mapping(target = "cronExpressionEncrypted", source = "cronExpression")
    @Mapping(target = "valueEncrypted", source = "value")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    FinancialCommitmentData toDataEncrypted(FinancialCommitmentEntity entity);
}
