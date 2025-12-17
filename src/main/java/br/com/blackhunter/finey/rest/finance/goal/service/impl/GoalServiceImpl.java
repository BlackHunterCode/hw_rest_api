package br.com.blackhunter.finey.rest.finance.goal.service.impl;

import br.com.blackhunter.finey.rest.auth.util.JwtUtil;
import br.com.blackhunter.finey.rest.finance.goal.dto.GoalData;
import br.com.blackhunter.finey.rest.finance.goal.dto.GoalPayload;
import br.com.blackhunter.finey.rest.finance.goal.entity.GoalEntity;
import br.com.blackhunter.finey.rest.finance.goal.mapper.GoalMapper;
import br.com.blackhunter.finey.rest.finance.goal.repository.GoalRepository;
import br.com.blackhunter.finey.rest.finance.goal.service.GoalService;
import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class GoalServiceImpl implements GoalService {
    @Value("${hunter.secrets.pluggy.crypt-secret}")
    private String PLUGGY_CRYPT_SECRET;

    private final GoalMapper goalMapper;
    private final GoalRepository goalRepository;
    private final JwtUtil jwtUtil;

    public GoalServiceImpl(
            GoalMapper goalMapper,
            GoalRepository goalRepository,
            JwtUtil jwtUtil) {
        this.goalMapper     = goalMapper;
        this.goalRepository = goalRepository;
        this.jwtUtil        = jwtUtil;
    }

    @Override
    public GoalData persistGoal(@Validated GoalPayload payload, boolean update) {
        log.debug("\tregisterGoal - Entrada: payload = {}", payload);
        try {
            GoalEntity goalEntity = goalMapper.toEntityEncrypted(payload);

            if(update) {
                if(payload.getGoalId() == null) throw new IllegalArgumentException("Goal ID must be provided for update operation");
                goalEntity.setGoalId(payload.getGoalId());
                goalEntity.setUpdatedAt(LocalDateTime.now());
            }
            else
            {
                goalEntity.setUserAccount(jwtUtil.getUserAccountFromToken());
                goalEntity.prePersist();
            }

            log.debug("[pre-persist] - goalEntity = {}", goalEntity);

            GoalEntity goalPersisted = this.goalRepository.save(goalEntity);

            log.debug("[post-persist] - goalPersisted = {}", goalPersisted);

            GoalData goalData = goalMapper.toDataEncrypted(goalPersisted);

            log.debug("\t@return = {}", goalData);

            return goalData;
        } catch (Exception e) {
            log.error("registerGoal", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<GoalData> listAllUserGoals() {
        log.debug("\tlistAllUserGoals - Entrada");

        UserAccountEntity user = jwtUtil.getUserAccountFromToken();
        List<GoalData> goals = this.goalRepository.findAllByUserAccountId(user.getAccountId())
                .stream().map(this.goalMapper::toDataEncrypted).toList();

        log.debug("\tgoals = {}", goals);
        return goals;
    }
}
