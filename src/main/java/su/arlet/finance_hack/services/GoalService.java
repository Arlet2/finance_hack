package su.arlet.finance_hack.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.controllers.rest.ValidationException;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyRemovedException;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.repos.GoalRepo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Goal createGoal(Goal goal) {
        return goalRepo.save(goal);
    }

    public List<Goal> getAllGoals() {
        return goalRepo.findAll();
    }

    public void deleteGoal(Long id) {
        goalRepo.findById(id).orElseThrow(EntityWasAlreadyRemovedException::new);
        goalRepo.deleteById(id);
    }

    public List<Goal> getGoalsByIsDone(boolean isDone) {
        return goalRepo.findByIsDone(isDone);
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
    /*
    public Map<String, Long> getGoalAchievementStats() {
        long totalGoals = goalRepo.count();
        //long completedGoals = goalRepo.countByIsDone(true);
        //long uncompletedGoals = totalGoals - completedGoals;

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalGoals", totalGoals);
        //stats.put("completedGoals", completedGoals);
        //stats.put("uncompletedGoals", uncompletedGoals);

        return stats;
    }
     */

    @Getter
    @Setter
    public class CreateGoalEntity {

        private Long sum;
        private LocalDate deadline;
        private String name;
        private String description;

        public void validate() {
            if (this.sum == null || this.sum < 0) {
                throw new ValidationException("sum undefined");
            }
            if (this.deadline == null) {
                throw new ValidationException("deadline undefined");
            }
            if (this.name == null || this.name.isEmpty()) {
                throw new ValidationException("name undefined");
            }


        }

        @Getter
        @Setter
        public class UpdateGoalEntity {

            private Long sum;
            private LocalDate deadline;
            private String name;
            private String description;

            public void validate() {
                if (this.deadline != null
                        || this.deadline.compareTo(LocalDate.now()) < 0) {
                    throw new ValidationException("deadline undefined");
                }
                if (this.sum != null && this.sum < 0) {
                    throw new ValidationException("sum undefined");
                }
            }
        }

    }
}