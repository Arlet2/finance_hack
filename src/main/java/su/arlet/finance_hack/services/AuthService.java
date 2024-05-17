package su.arlet.finance_hack.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.UserAlreadyExistsException;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.exceptions.WrongPasswordException;
import su.arlet.finance_hack.repos.UserRepo;

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
            .withIssuer("serv")
            .build();

    public String registerUser(String username, String hashPassword, LocalDate birthday, String email) {
        if (userRepo.existsByUsername(username)) {
            throw new UserAlreadyExistsException();
        } else {
            User user = new User(username, hashPassword, birthday, email);
            userRepo.save(user);
            return generateJwtToken(username);
        }
    }
    public String loginUser(String username, String hashPassword) {
        if(userRepo.existsByUsername(username)) {
            User user = userRepo.getUserByUsername(username);
            if (user.getHashPassword().equals(hashPassword)) {
                return generateJwtToken(username);
            } else {
                throw new WrongPasswordException();
            }
        } else {
            throw new UserNotFoundException();
        }
    }

    public String generateJwtToken(String username) {
        try {
            String token = JWT.create()
                    .withIssuer("finance")
                    .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 1 week
                    .withClaim("username", username)
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            return null;
        }
    }

    public String decodeJwtToken(String token) {
        DecodedJWT decodedJWT;
        try {
            decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim("username").asString();
        } catch (JWTVerificationException exception){
            return "";
        }
    }

}
