package su.arlet.finance_hack.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.User;

import java.time.LocalDate;
import java.util.List;

public interface GoalRepo extends JpaRepository<Goal, Long> {
    List<Goal> findByIsDone(boolean isDone);

    List<Goal> findByUser(User user);

    List<Goal> findByDeadlineBefore(LocalDate deadline);

    List<Goal> findByDeadlineAfter(LocalDate deadline);

    List<Goal> findAllByOrderByDeadlineAsc();

    List<Goal> findByDeadlineBetween(LocalDate startDate, LocalDate endDate);

}