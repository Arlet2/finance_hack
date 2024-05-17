package su.arlet.finance_hack.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import su.arlet.finance_hack.core.PaymentInfo;

public interface PaymentInfoRepo extends JpaRepository<PaymentInfo,Long> {
}
