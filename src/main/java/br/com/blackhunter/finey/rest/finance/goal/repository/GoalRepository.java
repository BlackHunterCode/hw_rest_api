package br.com.blackhunter.finey.rest.finance.goal.repository;

import br.com.blackhunter.finey.rest.finance.goal.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    @Query("SELECT g FROM GoalEntity g WHERE g.userAccount.accountId = :accountId")
    List<GoalEntity> findAllByUserAccountId(UUID accountId);
}
