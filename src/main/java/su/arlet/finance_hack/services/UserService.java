package su.arlet.finance_hack.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.controllers.rest.ValidationException;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.IncorrectUsernameException;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeleteException;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.utils.SHA1Hasher;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {
    private final UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public long getLimit(String username) {
        if (username == null || username.isEmpty()) {
            throw new IncorrectUsernameException();
        }
        User user = userRepo.getUserByUsername(username);
        return user.getLimit();
    }
    public long getCurrentWastings(String username) {
        if (username == null || username.isEmpty()) {
            throw new IncorrectUsernameException();
        }
        User user = userRepo.getUserByUsername(username);
        return user.getCurrentWastings();
    }

    public void delete(String username) {
        if (!userRepo.existsByUsername(username)) {
            throw new EntityWasAlreadyDeleteException();
        }
        userRepo.deleteById(username);

    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getByUsername(String userName) {
        User userByUsername = userRepo.getUserByUsername(userName);
        if (userByUsername == null)
            throw new UserNotFoundException();

        return userByUsername;
    }

    public void updateUserByUsername(UpdateUserEntity updateUserEntity, String username) {
        if (!userRepo.existsByUsername(username)) {
            throw new UserNotFoundException();
        }
        User user = getByUsername(username);

        String hashPassword = user.getHashPassword();
        if (updateUserEntity.password != null) {
            hashPassword = SHA1Hasher.toSHA1(updateUserEntity.password);
        }

        LocalDate birthday = user.getBirthday();
        if (updateUserEntity.birthday != null) {
            birthday = updateUserEntity.birthday;
        }

        String email = user.getEmail();
        if (updateUserEntity.email != null) {
            email = updateUserEntity.email;
        }
        long wastings = user.getCurrentWastings();
        if (updateUserEntity.currentWastings != null) {
            wastings = updateUserEntity.currentWastings;
        }

        Goal[] goals = user.getGoals();
        if (updateUserEntity.goals != null) {
            goals = updateUserEntity.goals;
        }

        Report[] reports = user.getReports();
        if (updateUserEntity.reports != null) {
            reports = updateUserEntity.reports;
        }

        long limit = user.getLimit();
        if (updateUserEntity.limit != null) {
            limit = updateUserEntity.limit;
        }
        User updatedUser = new User(
                user.getUsername(),
                hashPassword,
                birthday,
                email,
                wastings,
                goals,
                reports,
                limit
        );

        userRepo.save(updatedUser);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateUserEntity {
        private String username;

        private String password;

        private LocalDate birthday;

        private String email;

        public void validate() {
            if (this.username == null || this.username.isEmpty()) {
                throw new ValidationException("username is empty");
            }
            if (this.password == null || this.password.isEmpty()) {
                throw new ValidationException("password is empty");
            }
            if (this.birthday == null) {
                throw new ValidationException("birthday is null");
            }
            if (this.email == null || this.email.isEmpty()) {
                throw new ValidationException("email is empty");
            }

        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateUserEntity {

        private String password;

        private LocalDate birthday;

        private String email;

        private Long currentWastings;

        private Goal[] goals;

        private Report[] reports;

        private Long limit;

        public void validate() {
            if (this.email != null && this.email.isEmpty()) {
                throw new ValidationException("email is empty");
            }
            if (this.currentWastings != null && this.currentWastings < 0) {
                throw new ValidationException("wastings mustn't be negative");
            }
            if (this.limit != null && this.limit < 0) {
                throw new ValidationException("limit mustn't be negative");
            }
        }
    }
}
