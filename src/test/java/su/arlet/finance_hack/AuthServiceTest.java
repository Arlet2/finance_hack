package su.arlet.finance_hack;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.InvalidAuthorizationHeaderException;
import su.arlet.finance_hack.exceptions.WrongPasswordException;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.UserService;
import su.arlet.finance_hack.utils.SHA1Hasher;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter registerCounter;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(meterRegistry.counter("register_counter")).thenReturn(registerCounter);
        authService = new AuthService(meterRegistry, userRepo, userService);
    }

    @Test
    void testRegisterUser_Success() {
        UserService.CreateUserEntity createUserEntity = new UserService.CreateUserEntity(
                "testUser", "password", LocalDate.now(), "test@example.com"
        );
        when(userRepo.existsByUsername("testUser")).thenReturn(false);

        String token = authService.registerUser(createUserEntity);

        assertNotNull(token);
        verify(userRepo, times(1)).save(any(User.class));
        verify(registerCounter, times(1)).increment();
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        UserService.CreateUserEntity createUserEntity = new UserService.CreateUserEntity(
                "testUser", "password", LocalDate.now(), "test@example.com"
        );
        when(userRepo.existsByUsername("testUser")).thenReturn(true);

        assertThrows(EntityNotFoundException.class, () -> authService.registerUser(createUserEntity));
        verify(userRepo, never()).save(any(User.class));
        verify(registerCounter, never()).increment();
    }

    @Test
    void testLoginUser_Success() {
        String username = "testUser";
        String password = "password";
        User user = new User(username, SHA1Hasher.toSHA1(password), LocalDate.now(), "test@example.com", 0, null, null, 0);
        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(userService.getByUsername(username)).thenReturn(user);

        String token = authService.loginUser(username, password);

        assertNotNull(token);
    }

    @Test
    void testLoginUser_UserNotFound() {
        String username = "testUser";
        String password = "password";
        when(userRepo.existsByUsername(username)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> authService.loginUser(username, password));
    }

    @Test
    void testLoginUser_WrongPassword() {
        String username = "testUser";
        String password = "password";
        User user = new User(username, SHA1Hasher.toSHA1("wrongPassword"), LocalDate.now(), "test@example.com", 0, null, null, 0);
        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(userService.getByUsername(username)).thenReturn(user);

        assertThrows(WrongPasswordException.class, () -> authService.loginUser(username, password));
    }

    @Test
    void testDecodeJwtToken_Success() {
        String username = "testUser";
        String token = authService.generateJwtToken(username);

        String decodedUsername = authService.decodeJwtToken(token);

        assertEquals(username, decodedUsername);
    }

    @Test
    void testGetUsernameFromHttpRequest_Success() {
        String username = "testUser";
        String token = authService.generateJwtToken(username);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        String extractedUsername = authService.getUserFromHttpRequest(request).getUsername();

        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetUsernameFromHttpRequest_InvalidHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        assertThrows(InvalidAuthorizationHeaderException.class, () -> authService.getUserFromHttpRequest(request));
    }

    @Test
    void testGenerateJwtToken_Success() {
        String username = "testUser";

        String token = authService.generateJwtToken(username);

        assertNotNull(token);
        DecodedJWT decodedJWT = authService.getVerifier().verify(token);
        assertEquals(username, decodedJWT.getSubject());
    }
}
