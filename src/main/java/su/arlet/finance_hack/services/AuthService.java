package su.arlet.finance_hack.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.InvalidAuthorizationHeaderException;
import su.arlet.finance_hack.exceptions.UserAlreadyExistsException;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.exceptions.WrongPasswordException;
import su.arlet.finance_hack.repos.UserRepo;

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

    public String registerUser(User user) {
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException();
        } else {
            userRepo.save(user);
            return generateJwtToken();
        }
    }

    public String loginUser(String username, String hashPassword) {
        if (userRepo.existsByUsername(username)) {
            User user = userRepo.getUserByUsername(username);
            if (user.getHashPassword().equals(hashPassword)) {
                return generateJwtToken();
            } else {
                throw new WrongPasswordException();
            }
        } else {
            throw new UserNotFoundException();
        }
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


    public String getUsernameByHttpRequest(HttpServletRequest req) {
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

}
