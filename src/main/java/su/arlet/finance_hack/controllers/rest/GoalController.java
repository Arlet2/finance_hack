package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.GoalService;

import java.time.LocalDate;

@RestController
@RequestMapping("${api.path}/goals")
@Tag(name = "Goal API")
public class GoalController {

    private final GoalService goalService;
    private final AuthService authService;


    @Autowired
    public GoalController(GoalService goalService, AuthService authService) {
        this.goalService = goalService;
        this.authService = authService;
    }

    @Getter
    @Setter
    public class CreateGoalEntity {

        private Long sum;
        private LocalDate deadline;
        private String name;
        private String description;

        public void validate() {

        }


    }

    @Getter
    @Setter
    public class UpdateGoalEntity {

        private Long sum;
        private LocalDate deadline;
        private String name;

        public void validate() {

        }

        }



    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID")
    @ApiResponse(responseCode = "200", description = "Success - found goal", content = {
            @Content(schema = @Schema(implementation = Goal.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - goal not found")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<Goal> getGoalByID(@PathVariable Long id) {public ResponseEntity<Goal> getGoalByID(@PathVariable Long id)
        {
            try {
                Goal goal = goalService.getGoalById(id);
                return ResponseEntity.ok(goal);
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        }
    }

    @PostMapping("/")
    @Operation(summary = "Create a new goal")
    @ApiResponse(
            responseCode = "201", description = "Created goal id", content = {
            @Content(schema = @Schema(implementation = Long.class))}
    )
    @ApiResponse(
            responseCode = "400", description = "Bad body", content = {
            @Content(schema = @Schema(implementation = String.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - goal not found")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> createGoal(@RequestBody CreateGoalEntity createGoalEntity, HttpServletRequest servletRequest) {
        String username = authService.getUsernameByHttpRequest(servletRequest);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = authService.get____(username); // где сраная функци
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Goal goal = new Goal();
        goal.setName(createGoalEntity.getName());
        goal.setSum(createGoalEntity.getSum());
        goal.setDeadline(createGoalEntity.getDeadline());
        goal.setUser(user);

        Goal createdGoal = goalService.createGoal(goal);
        return new ResponseEntity<>(createdGoal.getId(), HttpStatus.CREATED);
    }


    @PatchMapping("/{id}")
    @Operation(summary = "Update goal")
    @ApiResponse(responseCode = "200", description = "Success - updated goal", content = {@Content(schema = @Schema(implementation = Goal.class))})
    @ApiResponse(responseCode = "400", description = "Bad body", content = {@Content(schema = @Schema(implementation = String.class))})
    @ApiResponse(responseCode = "404", description = "Not found - goal not found", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<Goal> updateGoal(
            @PathVariable Long id,
            @RequestBody UpdateGoalEntity updateGoalEntity
    ) {
        Goal goal = goalService.getGoalById(id);
        if (updateGoalEntity.getSum() != null) {
            goal.setSum(updateGoalEntity.getSum());
        }
        if (updateGoalEntity.getDeadline() != null) {
            goal.setDeadline(updateGoalEntity.getDeadline());
        }
        Goal updatedGoal = goalService.updateGoal(goal);
        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal")
    @ApiResponse(responseCode = "200", description = "Success - deleted goal", content = {@Content()})
    @ApiResponse(responseCode = "204", description = "Goal already removed", content = {@Content()})
    // TODO : посмотри как сделана у Зотова Артема обработка ошибок ExceptionHandler
    @ApiResponse(responseCode = "403", description = "Forbidden - user does not own the goal")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, HttpServletRequest servletRequest) {
        String username = authService.getUsernameByHttpRequest(servletRequest);
        Goal goal = goalService.getGoalById(id);
        if (!goal.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        goalService.deleteGoal(id);
        return ResponseEntity.ok(null);
    }
}
