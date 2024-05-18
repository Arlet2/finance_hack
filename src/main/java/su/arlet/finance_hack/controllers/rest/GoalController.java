package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
<<<<<<< HEAD
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
=======
>>>>>>> 411a6c8 (check controller)
=======
import lombok.Getter;
import lombok.Setter;
>>>>>>> f5f4f0a (check controller 2)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.User;
<<<<<<< HEAD
<<<<<<< HEAD
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.WrongGoalDataException;
=======
import su.arlet.finance_hack.exceptions.GoalNotFoundException;
>>>>>>> 411a6c8 (check controller)
=======
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
>>>>>>> f5f4f0a (check controller 2)
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.GoalService;

import java.time.LocalDate;
<<<<<<< HEAD
<<<<<<< HEAD
import java.util.List;
import java.util.stream.Collectors;
=======
import java.util.Map;
>>>>>>> 411a6c8 (check controller)
=======
>>>>>>> f5f4f0a (check controller 2)

@RestController
@RequestMapping("${api.path}/goals")
@Tag(name = "Goal API")
public class GoalController {

    private final GoalService goalService;
    private final AuthService authService;
<<<<<<< HEAD
=======

>>>>>>> 411a6c8 (check controller)

    @Autowired
    public GoalController(GoalService goalService, AuthService authService) {
        this.goalService = goalService;
        this.authService = authService;
    }

<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
    @Getter
    @Setter
>>>>>>> f5f4f0a (check controller 2)
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


>>>>>>> 411a6c8 (check controller)

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
<<<<<<< HEAD
<<<<<<< HEAD
    public ResponseEntity<?> createGoal(@RequestBody GoalService.CreateGoalEntity createGoalEntity, HttpServletRequest servletRequest) {
        String username = authService.getUsernameFromHttpRequest(servletRequest);
        createGoalEntity.validate();
        User user = authService.getByUsername(username);
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
=======
    public ResponseEntity<Goal> createGoal(@RequestBody GoalInfoEntity goalinfo) {
        if (goalinfo.username == null) {
=======
    public ResponseEntity<?> createGoal(@RequestBody CreateGoalEntity createGoalEntity, HttpServletRequest servletRequest) {
        String username = authService.getUsernameByHttpRequest(servletRequest);
        if (username == null) {
>>>>>>> f5f4f0a (check controller 2)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = authService.get____(username); // где сраная функци
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
<<<<<<< HEAD
        createdGoal.setUser(user);
        return new ResponseEntity<>(createdGoal, HttpStatus.CREATED);
>>>>>>> 411a6c8 (check controller)
=======
        Goal goal = new Goal();
        goal.setName(createGoalEntity.getName());
        goal.setSum(createGoalEntity.getSum());
        goal.setDeadline(createGoalEntity.getDeadline());
        goal.setUser(user);

        Goal createdGoal = goalService.createGoal(goal);
        return new ResponseEntity<>(createdGoal.getId(), HttpStatus.CREATED);
>>>>>>> f5f4f0a (check controller 2)
    }


    @PatchMapping("/{id}")
    @Operation(summary = "Update goal")
    @ApiResponse(responseCode = "200", description = "Success - updated goal", content = {@Content(schema = @Schema(implementation = Goal.class))})
    @ApiResponse(responseCode = "400", description = "Bad body", content = {@Content(schema = @Schema(implementation = String.class))})
    @ApiResponse(responseCode = "404", description = "Not found - goal not found", content = {@Content()})
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<Goal> updateGoal(
            @PathVariable Long id,
<<<<<<< HEAD
<<<<<<< HEAD
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
        Goal updatedGoal = goalService.updateGoal(goal);
        return ResponseEntity.ok(updatedGoal);
=======
            @RequestBody Map<String, Object> updates
=======
            @RequestBody UpdateGoalEntity updateGoalEntity
>>>>>>> f5f4f0a (check controller 2)
    ) {
        Goal goal = goalService.getGoalById(id);
        if (updateGoalEntity.getSum() != null) {
            goal.setSum(updateGoalEntity.getSum());
        }
        if (updateGoalEntity.getDeadline() != null) {
            goal.setDeadline(updateGoalEntity.getDeadline());
        }
<<<<<<< HEAD
>>>>>>> 411a6c8 (check controller)
=======
        Goal updatedGoal = goalService.updateGoal(goal);
        return ResponseEntity.ok(updatedGoal);
>>>>>>> f5f4f0a (check controller 2)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal")
    @ApiResponse(responseCode = "200", description = "Success - deleted goal", content = {@Content()})
    @ApiResponse(responseCode = "204", description = "Goal already removed", content = {@Content()})
<<<<<<< HEAD
=======
    // TODO : посмотри как сделана у Зотова Артема обработка ошибок ExceptionHandler
>>>>>>> f5f4f0a (check controller 2)
    @ApiResponse(responseCode = "403", description = "Forbidden - user does not own the goal")
    @ApiResponse(responseCode = "500", description = "Server error", content = {@Content()})
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, HttpServletRequest servletRequest) {
<<<<<<< HEAD
        String username = authService.getUsernameFromHttpRequest(servletRequest);
        Goal goal = goalService.getGoalById(id);
=======
        String username = authService.getUsernameByHttpRequest(servletRequest);
<<<<<<< HEAD
         Goal goal = goalService.getGoalById(id);
>>>>>>> 411a6c8 (check controller)
=======
        Goal goal = goalService.getGoalById(id);
>>>>>>> f5f4f0a (check controller 2)
        if (!goal.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        goalService.deleteGoal(id);
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
