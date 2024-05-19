package su.arlet.finance_hack.services;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.AccessDeniedException;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeletedException;
import su.arlet.finance_hack.exceptions.ValidationException;
import su.arlet.finance_hack.repos.GoalRepo;

import java.time.LocalDate;
import java.util.List;

@Service
public class GoalService {
    private final GoalRepo goalRepo;

    private static final int minPriority = 1;
    private static final int maxPriority = 10;

    @Autowired
    public GoalService(GoalRepo goalRepo) {
        this.goalRepo = goalRepo;
    }

    public Goal getGoalById(Long id, User user) {
        var goal = goalRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("goal"));

        if (goal.getUser().getUsername().equals(user.getUsername()))
            throw new AccessDeniedException();

        return goal;
    }

    public Goal getGoalById(Long id) {
        return goalRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("goal"));
    }

    public Long createGoal(CreateGoalEntity createGoalEntity, User user) {
        Goal goal = new Goal();

        goal.setName(createGoalEntity.getName());
        goal.setSum(createGoalEntity.getSum());
        goal.setDeadline(createGoalEntity.getDeadline());
        goal.setPriority(createGoalEntity.getPriority());
        goal.setUser(user);

        return goalRepo.save(goal).getId();
    }

    public List<Goal> getAllGoals() {
        return goalRepo.findAll();
    }

    public void deleteGoal(Long id, User user) {
        try {
            getGoalById(id, user);
        } catch (EntityNotFoundException e) {
            throw new EntityWasAlreadyDeletedException();
        }

        goalRepo.deleteById(id);
    }

    public List<Goal> getGoalsByUser(User user) {
        return goalRepo.findByUser(user);
    }

    public Goal updateGoal(Long goalID, User user, UpdateGoalEntity updateGoalEntity) {
        Goal goal = getGoalById(goalID, user);

        if (updateGoalEntity.getSum() != null)
            goal.setSum(updateGoalEntity.getSum());

        if (updateGoalEntity.getDeadline() != null)
            goal.setDeadline(updateGoalEntity.getDeadline());

        if (updateGoalEntity.getPriority() != null)
            goal.setPriority(updateGoalEntity.getPriority());

        if (updateGoalEntity.getName() != null)
            goal.setName(updateGoalEntity.getName());

        if (updateGoalEntity.getDescription() != null)
            goal.setName(updateGoalEntity.getDescription());

        return goalRepo.save(goal);
    }

    public void updateGoal(Long goalID, UpdateGoalEntity updateGoalEntity) {
        Goal goal = getGoalById(goalID);

        if (updateGoalEntity.getSum() != null)
            goal.setSum(updateGoalEntity.getSum());

        if (updateGoalEntity.getDeadline() != null)
            goal.setDeadline(updateGoalEntity.getDeadline());

        if (updateGoalEntity.getPriority() != null)
            goal.setPriority(updateGoalEntity.getPriority());

        if (updateGoalEntity.getName() != null)
            goal.setName(updateGoalEntity.getName());

        if (updateGoalEntity.getDescription() != null)
            goal.setName(updateGoalEntity.getDescription());

        goalRepo.save(goal);
    }

    public void updateGoal(Goal goal) {
        goalRepo.save(goal);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGoalEntity {
        private Long sum;
        private LocalDate deadline;
        private String name;
        private String description;
        private Long priority;

        public void validate() {
            if (this.sum == null || this.sum < 0)
                throw new ValidationException("sum must be not negative");

            if (this.deadline == null)
                throw new ValidationException("deadline must be not empty");

            if (this.deadline.isBefore(LocalDate.now()))
                throw new ValidationException("deadline must be in future");

            if (this.name == null || this.name.isEmpty())
                throw new ValidationException("name must be not empty");

            if (this.priority == null || (this.priority < minPriority || this.priority > maxPriority))
                throw new ValidationException("priority must be at least " + minPriority + " and less than " + maxPriority);

        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateGoalEntity {
        private Long sum;
        private LocalDate deadline;
        private String name;
        private String description;
        private Long priority;

        public void validate() {
            if (this.deadline != null && this.deadline.isBefore(LocalDate.now()))
                throw new ValidationException("deadline must be in future");

            if (this.sum != null && this.sum < 0)
                throw new ValidationException("sum must be not negative");

            if (this.name != null && this.name.isEmpty())
                throw new ValidationException("name must be not empty");

            if (this.description != null && this.description.isEmpty())
                throw new ValidationException("description must be not empty");

            if (this.priority != null && (this.priority < minPriority || this.priority > maxPriority))
                throw new ValidationException("priority must be at least " + minPriority + " and less than " + maxPriority);

        }
    }

}