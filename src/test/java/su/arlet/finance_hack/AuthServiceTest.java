package su.arlet.finance_hack;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.*;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.utils.SHA1Hasher;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthService authService;

    private Counter registerCounter;

    @BeforeEach
    public void setUp() {
        registerCounter = mock(Counter.class);
        when(meterRegistry.counter("register_counter")).thenReturn(registerCounter);
        authService = new AuthService(meterRegistry, userRepo);
    }

    @Test
    public void testRegisterUserSuccess() {
        AuthService.CreateUserEntity createUserEntity = new AuthService.CreateUserEntity(
                "testuser", "password", LocalDate.now(), "test@example.com");

        when(userRepo.existsByUsername(anyString())).thenReturn(false);

        String token = authService.registerUser(createUserEntity);

        assertNotNull(token);
        verify(userRepo).save(any(User.class));
        verify(registerCounter).increment();
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        AuthService.CreateUserEntity createUserEntity = new AuthService.CreateUserEntity(
                "testuser", "password", LocalDate.now(), "test@example.com");

        when(userRepo.existsByUsername(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerUser(createUserEntity);
        });

        verify(userRepo, never()).save(any(User.class));
        verify(registerCounter, never()).increment();
    }

    @Test
    public void testLoginUserSuccess() {
        String username = "testuser";
        String password = "password";
        String hashPassword = SHA1Hasher.toSHA1(password);

        User user = new User(username, hashPassword, LocalDate.now(), "test@example.com", null, null, 0);

        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(userRepo.getUserByUsername(username)).thenReturn(user);

        String token = authService.loginUser(username, password);

        assertNotNull(token);
    }

    @Test
    public void testLoginUserWrongPassword() {
        String username = "testuser";
        String password = "password";
        String wrongPassword = "wrongpassword";
        String hashPassword = SHA1Hasher.toSHA1(password);

        User user = new User(username, hashPassword, LocalDate.now(), "test@example.com", null, null, 0);

        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(userRepo.getUserByUsername(username)).thenReturn(user);

        assertThrows(WrongPasswordException.class, () -> {
            authService.loginUser(username, wrongPassword);
        });
    }

    @Test
    public void testLoginUserNotFound() {
        String username = "testuser";
        String password = "password";

        when(userRepo.existsByUsername(username)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            authService.loginUser(username, password);
        });
    }

    @Test
    public void testDeleteUserSuccess() {
        String username = "testuser";

        when(userRepo.existsByUsername(username)).thenReturn(true);

        authService.delete(username);

        verify(userRepo).deleteUserByUsername(username);
    }

    @Test
    public void testDeleteUserNotFound() {
        String username = "testuser";

        when(userRepo.existsByUsername(username)).thenReturn(false);

        assertThrows(RepoAlreadyDeleteException.class, () -> {
            authService.delete(username);
        });

        verify(userRepo, never()).deleteUserByUsername(username);
    }

    @Test
    public void testGenerateJwtToken() {
        String token = authService.generateJwtToken();

        assertNotNull(token);
    }

    @Test
    public void testDecodeJwtToken() {
        String token = authService.generateJwtToken();

        String subject = authService.decodeJwtToken(token);

        assertEquals("sub", subject);
    }

    @Test
    public void testGetUsernameFromHttpRequest() {
        String token = authService.generateJwtToken();

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + token);

        String username = authService.getUsernameFromHttpRequest(httpServletRequest);

        assertEquals("sub", username);
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = List.of(new User("testuser", "hashPassword", LocalDate.now(), "test@example.com", null, null, 0));

        when(userRepo.findAll()).thenReturn(users);

        List<User> result = authService.getAllUsers();

        assertEquals(1, result.size());
    }

    @Test
    public void testGetByUsername() {
        String username = "testuser";
        User user = new User(username, "hashPassword", LocalDate.now(), "test@example.com", null, null, 0);

        when(userRepo.getUserByUsername(username)).thenReturn(user);

        User result = authService.getByUsername(username);

        assertEquals(username, result.getUsername());
    }

    @Test
    public void testUpdateUserByUsername() {
        String username = "testuser";
        User user = new User(username, "hashPassword", LocalDate.now(), "test@example.com", null, null, 0);
        AuthService.UpdateUserEntity updateUserEntity = new AuthService.UpdateUserEntity(
                "newPassword", LocalDate.now(), "new@example.com", null, null, 100L);

        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(userRepo.getUserByUsername(username)).thenReturn(user);

        authService.updateUserByUsername(updateUserEntity, username);

        verify(userRepo).save(any(User.class));
    }
}
