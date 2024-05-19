package su.arlet.finance_hack.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.Goal;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.repos.GoalRepo;
import su.arlet.finance_hack.repos.ReportRepo;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeletedException;
import su.arlet.finance_hack.exceptions.ValidationException;
import su.arlet.finance_hack.repos.UserRepo;
import su.arlet.finance_hack.utils.SHA1Hasher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final GoalRepo goalRepo;
    private final ReportRepo reportRepo;

    @Autowired
    public UserService(UserRepo userRepo, GoalRepo goalRepo, ReportRepo reportRepo) {
        this.userRepo = userRepo;
        this.goalRepo = goalRepo;
        this.reportRepo = reportRepo;
    }

    public void delete(String username) {
        if (!userRepo.existsByUsername(username)) {
            throw new EntityWasAlreadyDeletedException();
        }
        userRepo.deleteById(username);

    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getByUsername(String userName) {
        User userByUsername = userRepo.getUserByUsername(userName);
        if (userByUsername == null)
            throw new EntityNotFoundException("user");

        return userByUsername;
    }

    public void updateUserByUsername(UpdateUserEntity updateUserEntity, String username) {
        if (!userRepo.existsByUsername(username)) {
            throw new EntityNotFoundException("user");
        }
        User user = getByUsername(username);

        String hashPassword;
        if (updateUserEntity.password != null) {
            hashPassword = SHA1Hasher.toSHA1(updateUserEntity.password);
        } else {
            hashPassword = user.getHashPassword();
        }

        LocalDate birthday;
        if (updateUserEntity.birthday != null) {
            birthday = updateUserEntity.birthday;
        } else {
            birthday = user.getBirthday();
        }

        String email;
        if (updateUserEntity.email != null) {
            email = updateUserEntity.email;
        } else {
            email = user.getEmail();
        }

        long wastings;
        wastings = Objects.requireNonNullElseGet(updateUserEntity.currentWastings, user::getCurrentWastings);

        Goal[] goals;
        if (updateUserEntity.goalIds != null) {
            List<Long> goalIds = updateUserEntity.goalIds;

            List<Goal> goalList = new ArrayList<>();
            for (Long num : goalIds) {
                Optional<Goal> optionalGoal = goalRepo.findById(num);
                optionalGoal.ifPresent(goalList::add);
            }
            goals = new Goal[goalList.size()];
            for (int i = 0; i < goalList.size(); i++) {
                goals[i] = goalList.get(i);
            }
            // TODO remove for method for casting
        } else {
            goals = user.getGoals();
        }

        Report[] reports;
        if (updateUserEntity.reportIds != null) {
            List<Long> reportIds = updateUserEntity.reportIds;

            List<Report> reportList = new ArrayList<>();
            for (Long num : reportIds) {
                Optional<Report> optionalReport = reportRepo.findById(num);
                optionalReport.ifPresent(reportList::add);
            }
            reports = new Report[reportList.size()];
            for (int i = 0; i < reportList.size(); i++) {
                reports[i] = reportList.get(i);
            }
            // TODO remove for method for casting
        } else {
            reports = user.getReports();
        }

        long limit;
        if (updateUserEntity.limit != null) {
            limit = updateUserEntity.limit;
        } else {
            limit = user.getLimit();
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
            if (this.birthday == null || (birthday.isBefore(LocalDate.now()))) {
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

        private List<Long> goalIds;

        private List<Long> reportIds;

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
            if (this.birthday != null && (birthday.isAfter(LocalDate.now()))) {
                throw new ValidationException("birthday is null");
            }
        }
    }
}
