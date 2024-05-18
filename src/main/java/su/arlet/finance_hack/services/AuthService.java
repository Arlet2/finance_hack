package su.arlet.finance_hack.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.*;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.utils.SHA1Hasher;

import java.time.LocalDate;
import java.util.Date;

@Service
@AllArgsConstructor
@Setter
@Getter
public class AuthService {
    private final UserRepo userRepo;
    private final Algorithm algorithm = Algorithm.HMAC256("Shulga");
    private final JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer("finance")
            .build();

    public String registerUser(CreateUserEntity createUserEntity) {
        String hashPassword = SHA1Hasher.toSHA1(createUserEntity.password);
        var user = new User(
                createUserEntity.username,
                hashPassword,
                createUserEntity.birthday,
                createUserEntity.email, null, null, null
        );
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException();
        } else {
            userRepo.save(user);
            return generateJwtToken();
        }
    }

    public String loginUser(String username, String password) {
        if (userRepo.existsByUsername(username)) {
            User user = userRepo.getUserByUsername(username);
            if (user.getHashPassword().equals(SHA1Hasher.toSHA1(password))) {
                return generateJwtToken();
            } else {
                throw new WrongPasswordException();
            }
        } else {
            throw new UserNotFoundException();
        }
    }

    public void delete(String username) {
        if (!userRepo.existsByUsername(username)) {
            throw new UserAlreadyExistsException();
        }

        userRepo.deleteUserByUsername(username);

    }

    public String generateJwtToken() {
        String token = JWT.create()
                .withIssuer("finance")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 1 week
                .withSubject("sub")
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

    public User getByUsername(String userName) {
        User userByUsername = userRepo.getUserByUsername(userName);
        if (userByUsername == null)
            throw new UserNotFoundException();

        return userByUsername;
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class CreateUserEntity {
        private String username;

        private String password;

        private LocalDate birthday;

        private String email;

        public void validate() {
            if (!(this.username != null && !this.username.isEmpty())) {
                throw new WrongUserdataException();
            }
            if (!(this.password != null && !this.password.isEmpty())) {
                throw new WrongUserdataException();
            }
            if(this.birthday == null) {
                throw new WrongUserdataException();
            }
            if (!(this.email != null && !this.email.isEmpty())) {
                throw new WrongUserdataException();
            }

        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class UpdateUserEntity {
        private String hashPassword;

        private LocalDate birthday;

        private String email;

        private Goal[] goals;

        private Report[] reports;

        private Long limit;

        public void validate() {
            if (!(this.hashPassword != null && !this.hashPassword.isEmpty())) {
                throw new WrongUserdataException();
            }
            if(this.birthday == null) {
                throw new WrongUserdataException();
            }
            if (!(this.email != null && !this.email.isEmpty())) {
                throw new WrongUserdataException();
            }
            if (!(this.limit != null && this.limit > 0)) {
                throw new WrongUserdataException();
            }
            if (!(this.goals != null && this.goals.length > 0)) {
                throw new WrongUserdataException();
            }
            if (!(this.reports != null && this.reports.length > 0)) {
                throw new WrongUserdataException();
            }

        }
    }

}
