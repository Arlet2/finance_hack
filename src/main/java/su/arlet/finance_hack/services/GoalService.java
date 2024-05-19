package su.arlet.finance_hack.services;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.controllers.rest.ValidationException;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.AuthFailedException;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeleteException;
import su.arlet.finance_hack.repos.GoalRepo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GoalService {


    private final GoalRepo goalRepo;

    @Autowired
    public GoalService(GoalRepo goalRepo) {
        this.goalRepo = goalRepo;
    }

    public Goal getGoalById(Long id) {

        return goalRepo.findById(id).orElseThrow(EntityNotFoundException::new);

    }

    public Long createGoal(Goal goal) {
        return goalRepo.save(goal).getId();
    }

    public List<Goal> getAllGoals() {
        return goalRepo.findAll();
    }

    public void deleteGoal(Long id, User user) {
        Goal goal = goalRepo.findById(id).orElseThrow(EntityWasAlreadyDeleteException::new);
        if (!goal.getUser().getUsername().equals(user.getUsername())) {
            throw new AuthFailedException();
        }
        goalRepo.deleteById(id);
    }

    public List<Goal> getGoalsByIsDone(boolean isDone) {
        return goalRepo.findByIsDone(isDone);
    }

    public List<Goal> getGoalsByUser(User user) {
        return goalRepo.findByUser(user);
    }

    public List<Goal> getGoalsByDeadlineBefore(LocalDate deadline) {
        return goalRepo.findByDeadlineBefore(deadline);
    }

    public List<Goal> getAllGoalsOrderByDeadlineAsc() {
        return goalRepo.findAllByOrderByDeadlineAsc();
    }

    public List<Goal> getGoalsWithinPeriod(LocalDate startDate, LocalDate endDate) {
        return goalRepo.findByDeadlineBetween(startDate, endDate);
    }

    public Goal AddContributionToGoal(Long id, long contribution) {
        Goal goal = goalRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        goal.setCurrentTotal(goal.getCurrentTotal() + contribution);
        return goalRepo.save(goal);
    }

    public Goal updateGoal(Goal goalDetails) {
        Goal goal = goalRepo.findById(goalDetails.getId()).orElseThrow(EntityNotFoundException::new);
        return goalRepo.save(goal);
    }

    public double calculateMonthlyContribution(long goalId) {
        Goal goal = goalRepo.findById(goalId).orElseThrow(EntityNotFoundException::new);
        long remainingSum = goal.getSum() - goal.getCurrentTotal();
        long monthsRemaining = ChronoUnit.MONTHS.between(LocalDate.now(), goal.getDeadline());
        return (double) remainingSum / monthsRemaining;
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

            if (this.sum == null || this.sum < 0) {
                throw new ValidationException("sum must be not negative");
            }
            if (this.deadline == null) {
                throw new ValidationException("deadline must be not empty");
            }
            if (this.deadline.isBefore(LocalDate.now())) {
                throw new ValidationException("deadline must be in future");
            }
            if (this.name == null || this.name.isEmpty()) {
                throw new ValidationException("name must be not empty");
            }
            if (this.priority == null || (this.priority <= 0 || this.priority > 10)) {
                throw new ValidationException("priority must be greater than 0 and less than 10");
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
                if (this.deadline != null && this.deadline.isBefore(LocalDate.now())) {
                    throw new ValidationException("deadline must be in future");
                }
                if (this.sum != null && this.sum < 0) {
                    throw new ValidationException("sum must be not negative");
                }
                if (this.name != null && this.name.isEmpty()) {
                    throw new ValidationException("name must be not empty");
                }
                if (this.description != null && this.description.isEmpty()) {
                    throw new ValidationException("description must be not empty");
                }
                if (this.priority != null && (this.priority <= 0 || this.priority > 10)) {
                    throw new ValidationException("priority must be greater than 0 and less or equal than 10");
                }

            }
        }

    }

}