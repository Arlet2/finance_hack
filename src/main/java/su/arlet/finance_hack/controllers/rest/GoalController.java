package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.services.GoalService;

import java.time.LocalDate;

@RestController
@RequestMapping("${api.path}/goals")
@Tag(name = "Goal API")
public class GoalController {

    private final GoalService goalService;

    @Autowired
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
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
        return new ResponseEntity<>(goal, HttpStatus.OK);
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
    public ResponseEntity<Goal> createGoal(@RequestBody Goal goal) {


        Goal createdGoal = goalService.createGoal(goal);
        return new ResponseEntity<>(createdGoal, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update goal")
    @ApiResponse(responseCode = "200", description = "Success - updated goal", content = {@Content()})
    @ApiResponse(
            responseCode = "400", description = "Bad body", content = {
            @Content(schema = @Schema(implementation = String.class))
    }
    )
    @ApiResponse(responseCode = "404", description = "Not found - goal not found", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> updateGoal(
            @PathVariable Long id,
            @RequestBody Goal goal
    ) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal")
    @ApiResponse(responseCode = "200", description = "Success - deleted goal", content = {@Content()})
    @ApiResponse(responseCode = "204", description = "No content", content = {@Content()})
    @ApiResponse(responseCode = "403", description = "Forbidden - user does not own the goal")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> deleteGoal(@PathVariable Long id) {
        return ResponseEntity.ok(null);
    }
}
