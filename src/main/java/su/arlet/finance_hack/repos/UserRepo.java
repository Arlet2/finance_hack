package su.arlet.finance_hack.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import su.arlet.finance_hack.core.User;

public interface UserRepo extends JpaRepository<User, String> {
    User getUserByUsername(String username);
    void deleteUserByUsername(String username);

    boolean existsByUsername(String username);
}
