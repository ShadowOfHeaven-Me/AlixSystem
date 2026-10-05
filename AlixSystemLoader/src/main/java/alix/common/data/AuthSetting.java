package alix.common.data;

public enum AuthSetting {

    PASSWORD,
    AUTH_APP,
    PASSWORD_AND_AUTH_APP;

    public boolean requiresAuthApp() {
        return this != PASSWORD;
    }

    public String toSavable() {
        return String.valueOf(this.ordinal());
    }

    public static AuthSetting fromString(String savable) {
        try {
            return values()[Integer.parseInt(savable)];
        } catch (NumberFormatException e) {
            return valueOf(savable);
        }
    }
}