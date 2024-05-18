package su.arlet.finance_hack.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.arlet.finance_hack.controllers.rest.ValidationException;
import su.arlet.finance_hack.core.enums.PaymentType;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sum;

    private Timestamp time;

    private String companyName;

    @ManyToOne
    private ItemCategory itemCategory;

    private PaymentType paymentType;

    @ManyToOne
    private User user;

    private Boolean isTransfer;

    public void validate() {
        if (sum == null || sum <= 0)
            throw new ValidationException("Payment sum undefined");
        if (time != null && time.after(Timestamp.valueOf(LocalDateTime.now())))
            throw new ValidationException("Operation time invalid");
        if (time == null)
            time = Timestamp.valueOf(LocalDateTime.now());

        if (!isTransfer && paymentType == PaymentType.FOR_GOAL)
            throw new ValidationException("FOR_GOAL can't be set without transfer");

        if (id == null && (!isTransfer && itemCategory == null || isTransfer && paymentType != PaymentType.FOR_GOAL)) {
            paymentType = PaymentType.UNKNOWN;
        }
        if (id == null && !isTransfer && itemCategory != null)
            paymentType = PaymentType.SAVED;

    }

}
