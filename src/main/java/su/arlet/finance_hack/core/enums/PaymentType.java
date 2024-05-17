package su.arlet.finance_hack.core.enums;

import jakarta.persistence.Embeddable;

@Embeddable
public enum PaymentType {

    UNKNOWN,
    FOR_GOAL,
    SAVED,
    UNSAVED

}
