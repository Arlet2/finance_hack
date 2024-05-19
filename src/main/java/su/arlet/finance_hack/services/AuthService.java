package su.arlet.finance_hack.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.controllers.rest.ValidationException;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.InvalidAuthorizationHeaderException;
import su.arlet.finance_hack.exceptions.UserAlreadyExistsException;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.exceptions.WrongPasswordException;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.utils.SHA1Hasher;

import java.util.Date;

@Service
@AllArgsConstructor
@Setter
@Getter
public class AuthService {
    private final UserService userService;
    private final UserRepo userRepo;
    private final Algorithm algorithm = Algorithm.HMAC256("Shulga");
    private final JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer("finance")
            .build();

    private final Counter registerCounter;

    @Autowired
    public AuthService(MeterRegistry meterRegistry, UserRepo userRepo, UserService userService) {
        this.userRepo = userRepo;
        this.userService = userService;
        registerCounter = meterRegistry.counter("register_counter");
    }

    public String registerUser(UserService.CreateUserEntity createUserEntity) {
        String hashPassword = SHA1Hasher.toSHA1(createUserEntity.getPassword());
        var user = new User(
                createUserEntity.getUsername(),
                hashPassword,
                createUserEntity.getBirthday(),
                createUserEntity.getEmail(),
                0, null, null, 0
        );
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException();
        } else {
            userRepo.save(user);
            registerCounter.increment();
            return generateJwtToken(user.getUsername());
        }
    }

    public String loginUser(String username, String password) {
        if (!userRepo.existsByUsername(username)) {
            throw new UserNotFoundException();
        }
        User user = userService.getByUsername(username);
        if (!user.getHashPassword().equals(SHA1Hasher.toSHA1(password))) {
            throw new WrongPasswordException();
        }
        return generateJwtToken(username);

    }


    public String generateJwtToken(String username) {
        String token = JWT.create()
                .withIssuer("finance")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 1 week
                .withSubject(username)
                .sign(algorithm);
        return token;
    }

    public String decodeJwtToken(String token) {
        DecodedJWT decodedJWT;
        decodedJWT = verifier.verify(token);
        return decodedJWT.getSubject();
    }


    public String getUsernameFromHttpRequest(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");

        if (auth != null) {
            String[] parsedHeader = auth.split(" ");
            if (parsedHeader.length == 2 && (parsedHeader[0].equalsIgnoreCase("bearer")))
                return decodeJwtToken(parsedHeader[1]);
        }
        throw new InvalidAuthorizationHeaderException();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginUser {
        private String username;
        private String password;

        public void validate() {
            if (this.username == null || this.username.isEmpty()) {
                throw new ValidationException("username is empty");
            }
            if (this.password == null || this.password.isEmpty()) {
                throw new ValidationException("password is empty");
            }

        }
    }

}
