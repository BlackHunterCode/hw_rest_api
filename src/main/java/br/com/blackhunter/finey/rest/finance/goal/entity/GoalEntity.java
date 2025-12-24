package br.com.blackhunter.finey.rest.finance.goal.entity;

import br.com.blackhunter.finey.rest.useraccount.entity.UserAccountEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fn_goals")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "goal_id", updatable = false)
    private UUID goalId;

    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false, updatable = false)
    private UserAccountEntity userAccount;

    @Column(name = "goal_name", length = 40, nullable = false)
    private String goalName;
    @Column(name = "goal_description", length = 240, nullable = false)
    private String goalDescription;
    @Column(name = "goal_icon", length = 1, nullable = false)
    private String goalIcon;
    @Column(name = "goal_color", length = 10, nullable = false)
    private String goalColor;
    @Column(name = "d_goal_target_amount", nullable = false)
    private String DGoalTargetAmount;

    @Column(name = "goal_date")
    private LocalDate goalDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
}
