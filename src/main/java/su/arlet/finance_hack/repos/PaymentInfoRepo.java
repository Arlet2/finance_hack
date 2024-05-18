package su.arlet.finance_hack.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import su.arlet.finance_hack.core.PaymentInfo;
import su.arlet.finance_hack.core.User;

import java.util.List;

public interface PaymentInfoRepo extends JpaRepository<PaymentInfo,Long> {

    public List<PaymentInfo> findAllByUser(User user);
    List<PaymentInfo> findByUser(User user);
}
