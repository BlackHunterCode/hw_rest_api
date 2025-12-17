package br.com.blackhunter.finey.rest.finance.goal.controller;

import br.com.blackhunter.finey.rest.core.dto.ApiResponse;
import br.com.blackhunter.finey.rest.finance.goal.dto.GoalData;
import br.com.blackhunter.finey.rest.finance.goal.dto.GoalPayload;
import br.com.blackhunter.finey.rest.finance.goal.service.GoalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/finance/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GoalData>> createGoal(@RequestBody GoalPayload payload) {
        GoalData data = this.goalService.persistGoal(payload, false);
        ApiResponse<GoalData> response = new ApiResponse<>(
                "success",
                201,
                data
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<GoalData>> updateGoal(@RequestBody GoalPayload payload) {
        GoalData data = this.goalService.persistGoal(payload, true);
        ApiResponse<GoalData> response = new ApiResponse<>(
                "success",
                200,
                data
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/list-all")
    public ResponseEntity<ApiResponse<List<GoalData>>> listAllGoals() {
        List<GoalData> dataList = this.goalService.listAllUserGoals();
        ApiResponse<List<GoalData>> response = new ApiResponse<>(
                "success",
                200,
                dataList
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
