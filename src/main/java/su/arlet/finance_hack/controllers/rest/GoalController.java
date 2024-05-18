package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
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
import su.arlet.finance_hack.exceptions.GoalNotFoundException;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.GoalService;

import java.time.LocalDate;
import java.util.Map;

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

    public class CreateGoalEntity {

        private long sum;
        private LocalDate deadline;
        private String name;
        private String description;

    }

    public class UpdateGoalEntity {

        private long sum;
        private LocalDate deadline;
        private String name;

        }

    private class GoalInfoEntity {

        private Goal goal;
        private String username;

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
            } catch (GoalNotFoundException e) {
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
    public ResponseEntity<Goal> createGoal(@RequestBody GoalInfoEntity goalinfo) {
        if (goalinfo.username == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = authService.(goalinfo.username); //где сраная функция
        Goal createdGoal = goalService.createGoal(goalinfo.goal);
        if (createdGoal == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); //что тут вернуть
        }
        createdGoal.setUser(user);
        return new ResponseEntity<>(createdGoal, HttpStatus.CREATED);
    }


    @PatchMapping("/{id}")
    @Operation(summary = "Update goal")
    @ApiResponse(responseCode = "200", description = "Success - updated goal", content = {@Content(schema = @Schema(implementation = Goal.class))})
    @ApiResponse(responseCode = "400", description = "Bad body", content = {@Content(schema = @Schema(implementation = String.class))})
    @ApiResponse(responseCode = "404", description = "Not found - goal not found", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> updateGoal(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        try {
            Goal updatedGoal = goalService.updateGoal(id, updates);
            return ResponseEntity.ok(updatedGoal);
        } catch (GoalNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Goal not found");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal")
    @ApiResponse(responseCode = "200", description = "Success - deleted goal", content = {@Content()})
    @ApiResponse(responseCode = "204", description = "No content", content = {@Content()})
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
