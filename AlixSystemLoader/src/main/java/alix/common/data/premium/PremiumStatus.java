package alix.common.data.premium;

public enum PremiumStatus {

    PREMIUM,
    NON_PREMIUM,
    UNKNOWN;

    private final String readableName = firstUpperRestLower(this.name());

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

    public String readableName() {
        return this.readableName;
    }

    private static String firstUpperRestLower(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase().replace('_','-');
    }
}