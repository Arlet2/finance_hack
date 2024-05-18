package su.arlet.finance_hack.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.arlet.finance_hack.core.enums.PaymentType;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoFilter {

    private PaymentType paymentType;

    private boolean isTransfer;

    private ItemCategory itemCategory;

}
