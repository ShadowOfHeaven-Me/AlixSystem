package alix.spigot.api.events.auth;

public enum AuthReason {

    MANUAL_REGISTER(false),
    MANUAL_LOGIN(false),
    PREMIUM_AUTO_REGISTER(true),
    PREMIUM_AUTO_LOGIN(true),
    IP_AUTO_LOGIN(true);

    private final boolean isAutoLogin;

    AuthReason(boolean isAutoLogin) {
        this.isAutoLogin = isAutoLogin;
    }

    public boolean isAutoLogin() {
        return isAutoLogin;
    }

    public boolean isPremium() {
        switch (this) {
            case PREMIUM_AUTO_LOGIN, PREMIUM_AUTO_REGISTER:
                return true;
            default:
                return false;
        }
    }
}