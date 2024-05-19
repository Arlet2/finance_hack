package su.arlet.finance_hack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.InvalidAuthorizationHeaderException;
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
    void testGetAllGoals() {
        List<Goal> goals = List.of(testGoal);
        when(goalRepo.findAll()).thenReturn(goals);

        List<Goal> result = goalService.getAllGoals();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGoal.getId(), result.get(0).getId());
    }

    @Test
    void testDeleteGoalAccess() {
        when(goalRepo.findById(1L)).thenReturn(Optional.of(testGoal));
        assertThrows(InvalidAuthorizationHeaderException.class, () -> goalService.deleteGoal(1L, testUser));
    }

    @Test
    void testDeleteGoalNotFound() {
        when(goalRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> goalService.deleteGoal(1L, testUser));
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


}
