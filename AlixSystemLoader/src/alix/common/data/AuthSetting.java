package alix.common.data;

public enum AuthSetting {

    PASSWORD,
    AUTH_APP,
    PASSWORD_AND_AUTH_APP;

    public String toSavable() {
        return String.valueOf(this.ordinal());
    }

    public static AuthSetting fromString(String savable) {
        return values()[Integer.parseInt(savable)];
    }
}