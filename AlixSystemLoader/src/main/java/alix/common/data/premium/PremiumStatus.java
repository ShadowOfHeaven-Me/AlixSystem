package alix.common.data.premium;

public enum PremiumStatus {

    PREMIUM,
    NON_PREMIUM,
    UNKNOWN;

    public boolean isPremium() {
        return this == PREMIUM;
    }

    public boolean isNonPremium() {
        return this == NON_PREMIUM;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public boolean isKnown() {
        return !this.isUnknown();
    }
}