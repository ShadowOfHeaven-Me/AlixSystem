package alix.common.connection.timestamp;

import alix.common.data.premium.PremiumStatus;

import java.util.Arrays;

public enum ClockSkewConfidence {

    NONE(0),
    VERY_EARLY_ESTIMATION(1),
    EXTREMELY_LOW(60),
    VERY_LOW(120),
    LOW(180),
    MEDIUM(600),
    HIGH(1200),//20 min
    VERY_HIGH(2400);//40 min

    private static final ClockSkewConfidence[] VALUES_REVERSED = Arrays.stream(values()).toList().reversed().toArray(new ClockSkewConfidence[0]);
    private final int measuredForSecMinimum;

    ClockSkewConfidence(int measuredForSecMinimum) {
        this.measuredForSecMinimum = measuredForSecMinimum;
    }

    public static ClockSkewConfidence of(double spanSeconds) {
        for (var c : VALUES_REVERSED) {
            if (spanSeconds > c.measuredForSecMinimum)
                return c;
        }
        return NONE;//0 seconds
    }

    public String readableName() {
        return readable(this.name());
    }

    //make the second, third etc. word capital too?
    public static String readable(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase().replace('_',' ');
    }

    public boolean isStillMeasuring() {
        return this != VALUES_REVERSED[0];
    }

    public boolean isVeryEarlyEstimation() {
        return this == VERY_EARLY_ESTIMATION;
    }

    public boolean isAny() {
        return this != NONE;
    }
}