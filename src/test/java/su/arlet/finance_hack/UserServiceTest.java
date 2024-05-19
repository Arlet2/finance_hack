package su.arlet.finance_hack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeletedException;
import su.arlet.finance_hack.exceptions.InvalidAuthorizationHeaderException;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.services.UserService;
import su.arlet.finance_hack.utils.SHA1Hasher;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDelete() {
        String username = "testUser";
        when(userRepo.existsByUsername(username)).thenReturn(true);

        userService.delete(username);

        verify(userRepo).existsByUsername(username);
        verify(userRepo).deleteById(username);
    }

    @Test
    void testDeleteThrowsExceptionIfUserNotFound() {
        String username = "testUser";
        when(userRepo.existsByUsername(username)).thenReturn(false);

        assertThrows(EntityWasAlreadyDeletedException.class, () -> userService.delete(username));

        verify(userRepo).existsByUsername(username);
        verify(userRepo, never()).deleteById(username);
    }

    @Test
    void testGetAllUsers() {
        List<User> expectedUsers = Collections.singletonList(new User());
        when(userRepo.findAll()).thenReturn(expectedUsers);

        List<User> users = userService.getAllUsers();

        assertEquals(expectedUsers, users);
        verify(userRepo).findAll();
    }

    @Test
    void testGetByUsername() {
        String username = "testUser";
        User expectedUser = new User();
        when(userRepo.getUserByUsername(username)).thenReturn(expectedUser);

        User user = userService.getByUsername(username);

        assertEquals(expectedUser, user);
        verify(userRepo).getUserByUsername(username);
    }

    @Test
    void testGetByUsernameThrowsExceptionIfUserNotFound() {
        String username = "testUser";
        when(userRepo.getUserByUsername(username)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> userService.getByUsername(username));

        verify(userRepo).getUserByUsername(username);
    }

    @Test
    void testUpdateUserByUsername() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(userRepo.getUserByUsername(username)).thenReturn(user);

        UserService.UpdateUserEntity updateUserEntity = new UserService.UpdateUserEntity();
        updateUserEntity.setPassword("newPassword");
        updateUserEntity.setBirthday(LocalDate.of(1990, 1, 1));
        updateUserEntity.setEmail("newEmail@example.com");
        updateUserEntity.setCurrentWastings(200L);
        updateUserEntity.setLimit(1000L);

        userService.updateUserByUsername(updateUserEntity, username);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals(username, updatedUser.getUsername());
        assertEquals(SHA1Hasher.toSHA1("newPassword"), updatedUser.getHashPassword());
        assertEquals(LocalDate.of(1990, 1, 1), updatedUser.getBirthday());
        assertEquals("newEmail@example.com", updatedUser.getEmail());
        assertEquals(200L, updatedUser.getCurrentWastings());
        assertArrayEquals(new Goal[]{}, updatedUser.getGoals());
        assertArrayEquals(new Report[]{}, updatedUser.getReports());
        assertEquals(1000L, updatedUser.getLimit());
    }

    @Test
    void testUpdateUserByUsernameThrowsExceptionIfUserNotFound() {
        String username = "testUser";
        UserService.UpdateUserEntity updateUserEntity = new UserService.UpdateUserEntity();
        when(userRepo.existsByUsername(username)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.updateUserByUsername(updateUserEntity, username));

        verify(userRepo).existsByUsername(username);
        verify(userRepo, never()).save(any(User.class));
    }
}

