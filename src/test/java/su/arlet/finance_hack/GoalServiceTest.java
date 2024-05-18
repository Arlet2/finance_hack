package su.arlet.finance_hack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.AuthFailedException;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.repos.GoalRepo;
import su.arlet.finance_hack.services.GoalService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest {

    @Mock
    private GoalRepo goalRepo;

    @InjectMocks
    private GoalService goalService;

    private Goal testGoal;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testGoal = new Goal();
        testGoal.setId(1L);
        testGoal.setSum(1000L);
        testGoal.setCurrentTotal(100L);
        testGoal.setDeadline(LocalDate.now().plusMonths(6));
        testGoal.setName("Test Goal");
        testGoal.setDescription("Test Description");
        testGoal.setUser(testUser);
        testGoal.setDone(false);
    }

    @Test
    void testGetGoalById() {
        when(goalRepo.findById(1L)).thenReturn(Optional.of(testGoal));

        Goal goal = goalService.getGoalById(1L);

        assertNotNull(goal);
        assertEquals(testGoal.getId(), goal.getId());
    }

    @Test
    void testGetGoalByIdNotFound() {
        when(goalRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.getGoalById(1L));
    }

    @Test
    void testCreateGoal() {
        when(goalRepo.save(testGoal)).thenReturn(testGoal);

        Goal createdGoal = goalService.createGoal(testGoal);

        assertNotNull(createdGoal);
        assertEquals(testGoal.getId(), createdGoal.getId());
    }

    @Test
    void testGetAllGoals() {
        List<Goal> goals = List.of(testGoal);
        when(goalRepo.findAll()).thenReturn(goals);

        List<Goal> result = goalService.getAllGoals();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGoal.getId(), result.get(0).getId());
    }

    @Test
    void testDeleteGoal() {
        when(goalRepo.findById(1L)).thenReturn(Optional.of(testGoal));
        goalService.deleteGoal(1L, testUser);

        verify(goalRepo).deleteById(1L);
    }

    @Test
    void testDeleteGoalNotFound() {
        when(goalRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> goalService.deleteGoal(1L, testUser));
    }

    @Test
    void testDeleteGoalAuthFailed() {
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");

        when(goalRepo.findById(1L)).thenReturn(Optional.of(testGoal));

        assertThrows(AuthFailedException.class, () -> goalService.deleteGoal(1L, anotherUser));
    }

    @Test
    void testGetGoalsByIsDone() {
        List<Goal> goals = List.of(testGoal);
        when(goalRepo.findByIsDone(false)).thenReturn(goals);

        List<Goal> result = goalService.getGoalsByIsDone(false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGoal.getId(), result.get(0).getId());
    }

    @Test
    void testGetGoalsByUser() {
        List<Goal> goals = List.of(testGoal);
        when(goalRepo.findByUser(testUser)).thenReturn(goals);

        List<Goal> result = goalService.getGoalsByUser(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGoal.getId(), result.get(0).getId());
    }

    @Test
    void testGetGoalsByDeadlineBefore() {
        List<Goal> goals = List.of(testGoal);
        when(goalRepo.findByDeadlineBefore(any(LocalDate.class))).thenReturn(goals);

        List<Goal> result = goalService.getGoalsByDeadlineBefore(LocalDate.now().plusMonths(1));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGoal.getId(), result.get(0).getId());
    }

    @Test
    void testGetAllGoalsOrderByDeadlineAsc() {
        List<Goal> goals = List.of(testGoal);
        when(goalRepo.findAllByOrderByDeadlineAsc()).thenReturn(goals);

        List<Goal> result = goalService.getAllGoalsOrderByDeadlineAsc();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGoal.getId(), result.get(0).getId());
    }

    @Test
    void testGetGoalsWithinPeriod() {
        List<Goal> goals = List.of(testGoal);
        when(goalRepo.findByDeadlineBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(goals);

        List<Goal> result = goalService.getGoalsWithinPeriod(LocalDate.now(), LocalDate.now().plusMonths(1));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGoal.getId(), result.get(0).getId());
    }

    @Test
    void testAddContributionToGoal() {
        when(goalRepo.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepo.save(any(Goal.class))).thenReturn(testGoal);

        Goal updatedGoal = goalService.AddContributionToGoal(1L, 200L);

        assertNotNull(updatedGoal);
        assertEquals(300L, updatedGoal.getCurrentTotal());
    }

    @Test
    void testAddContributionToGoalNotFound() {
        when(goalRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.AddContributionToGoal(1L, 200L));
    }

    @Test
    void testUpdateGoal() {
        when(goalRepo.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepo.save(any(Goal.class))).thenReturn(testGoal);

        Goal updatedGoal = goalService.updateGoal(testGoal);

        assertNotNull(updatedGoal);
        assertEquals(testGoal.getId(), updatedGoal.getId());
    }

    @Test
    void testCalculateMonthlyContribution() {
        when(goalRepo.findById(1L)).thenReturn(Optional.of(testGoal));

        double monthlyContribution = goalService.calculateMonthlyContribution(1L);

        assertTrue(monthlyContribution > 0);
    }

    @Test
    void testCalculateMonthlyContributionGoalNotFound() {
        when(goalRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> goalService.calculateMonthlyContribution(1L));
    }
}
