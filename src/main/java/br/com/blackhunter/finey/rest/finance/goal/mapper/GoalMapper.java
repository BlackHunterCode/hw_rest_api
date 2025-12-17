package br.com.blackhunter.finey.rest.finance.goal.mapper;

import br.com.blackhunter.finey.rest.finance.goal.dto.GoalData;
import br.com.blackhunter.finey.rest.finance.goal.dto.GoalPayload;
import br.com.blackhunter.finey.rest.finance.goal.entity.GoalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Interface <code>GoalMapper</code>.
 * Interface de mapeamentos de metas do usuário.
 * */
@Mapper(componentModel = "spring")
public interface GoalMapper {
    GoalMapper INSTANCE = Mappers.getMapper(GoalMapper.class);

    @Mapping(target = "goalId", ignore = true)
    @Mapping(target = "userAccount", ignore = true)
    @Mapping(target = "goalName", source = "goalName")
    @Mapping(target = "goalDescription", source = "goalDescription")
    @Mapping(target = "goalIcon", source = "goalIcon")
    @Mapping(target = "goalColor", source = "goalColor")
    @Mapping(target = "DGoalTargetAmount", source = "goalTargetAmount", defaultValue = "0.0")
    @Mapping(target = "goalDate", source = "goalDate")
    GoalEntity toEntityEncrypted(GoalPayload payload);

    @Mapping(target = "goalId", source = "goalId")
    @Mapping(target = "goalNameEncrypted", source = "goalName")
    @Mapping(target = "goalDescriptionEncrypted", source = "goalDescription")
    @Mapping(target = "goalIconEncrypted", source = "goalIcon")
    @Mapping(target = "goalColorEncrypted", source = "goalColor")
    @Mapping(target = "DGoalTargetAmountEncrypted", source = "DGoalTargetAmount")
    @Mapping(target = "goalDate", source = "goalDate")
    GoalData toDataEncrypted(GoalEntity entity);
}
