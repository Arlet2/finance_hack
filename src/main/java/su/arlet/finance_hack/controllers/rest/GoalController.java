package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.GoalService;
import su.arlet.finance_hack.services.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("${api.path}/goals")
@Tag(name = "Goal API")
public class GoalController {

    private final GoalService goalService;
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public GoalController(GoalService goalService, AuthService authService, UserService userService) {
        this.goalService = goalService;
        this.authService = authService;
        this.userService = userService;
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID")
    @ApiResponse(responseCode = "200", description = "Success - found goal", content = {
            @Content(schema = @Schema(implementation = Goal.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - goal not found")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<Goal> getGoalByID(@PathVariable Long id) {
        Goal goal = goalService.getGoalById(id);
        return ResponseEntity.ok(goal);
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
    public ResponseEntity<?> createGoal(@RequestBody GoalService.CreateGoalEntity createGoalEntity, HttpServletRequest servletRequest) {
        String username = authService.getUsernameFromHttpRequest(servletRequest);
        createGoalEntity.validate();
        User user = userService.getByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Goal goal = new Goal();
        goal.setName(createGoalEntity.getName());
        goal.setSum(createGoalEntity.getSum());
        goal.setDeadline(createGoalEntity.getDeadline());
        goal.setPriority(createGoalEntity.getPriority());
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
            @RequestBody GoalService.CreateGoalEntity.UpdateGoalEntity updateGoalEntity,
            HttpServletRequest servletRequest
    ) {
        Goal goal = goalService.getGoalById(id);
        String username = authService.getUsernameFromHttpRequest(servletRequest);
        if (!goal.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        updateGoalEntity.validate();
        if (updateGoalEntity.getSum() != null) {
            goal.setSum(updateGoalEntity.getSum());
        }
        if (updateGoalEntity.getDeadline() != null) {
            goal.setDeadline(updateGoalEntity.getDeadline());
        }
        if (updateGoalEntity.getPriority() != null) {
            goal.setPriority(updateGoalEntity.getPriority());
        }
        Goal updatedGoal = goalService.updateGoal(goal);
        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal")
    @ApiResponse(responseCode = "200", description = "Success - deleted goal", content = {@Content()})
    @ApiResponse(responseCode = "204", description = "Goal already removed", content = {@Content()})
    @ApiResponse(responseCode = "403", description = "Forbidden - user does not own the goal")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, HttpServletRequest servletRequest) {
        String username = authService.getUsernameFromHttpRequest(servletRequest);
        User user = authService.getByUsername(username);
        goalService.deleteGoal(id, user);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/")
    @ApiResponse(responseCode = "200", description = "OK",
            content = {
                    @Content(array = @ArraySchema(schema = @Schema(implementation = Goal.class)))
            })
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    @Operation(summary = "Get goals by filters")
    public ResponseEntity<List<Goal>> getGoals(
            @RequestParam(required = false) Boolean isDone,
            @RequestParam(required = false) LocalDate deadlineBefore,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<Goal> goals = goalService.getAllGoals();

        List<Goal> filteredGoals = goals.stream()
                .filter(goal -> isDone == null || goal.isDone() == isDone)
                .filter(goal -> deadlineBefore == null || goal.getDeadline().isBefore(deadlineBefore))
                .filter(goal -> (startDate == null || endDate == null) ||
                        (goal.getDeadline().isAfter(startDate.minusDays(1)) &&
                                goal.getDeadline().isBefore(endDate.plusDays(1))))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredGoals);
    }
}
