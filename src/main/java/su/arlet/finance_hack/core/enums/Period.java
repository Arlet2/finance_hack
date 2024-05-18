package su.arlet.finance_hack.core.enums;

import java.util.Arrays;

public enum Period {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;
    public String getPeriodMessage() {
        return switch (this) {
            case DAILY -> "0 0 00 * * *";
            case WEEKLY -> "0 0 00 * * 1";
            case MONTHLY -> "0 0 0 1 * *";
            case YEARLY -> "0 0 0 1 1 *";
            default -> throw new IllegalArgumentException("Unexpected value: " + this);
        };
    }
    public static boolean isEnumContains(Period per) {
        return Arrays.asList(Period.values()).contains(per);
    }
}


