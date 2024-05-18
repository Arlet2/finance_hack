package su.arlet.finance_hack.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.User;
import su.arlet.finance_hack.exceptions.IncorrectUsernameException;
import su.arlet.finance_hack.repos.UserRepo;

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
}
