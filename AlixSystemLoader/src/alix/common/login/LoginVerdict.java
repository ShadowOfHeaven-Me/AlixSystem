package alix.common.login;

public enum LoginVerdict {

    REGISTER_PREMIUM(true, "Premium AutoRegister"),//premium register
    LOGIN_PREMIUM(true, "Premium AutoLogin"),//premium login
    IP_AUTO_LOGIN(true, "IP AutoLogin"),//ip autologin was confirmed
    DISALLOWED_NO_DATA(false, "No data"),//register (for the first time)
    DISALLOWED_PASSWORD_RESET(false,"Password was reset"),//register (after a password reset)
    DISALLOWED_LOGIN_REQUIRED(false,"Login required");//login is required - none of the other tests came back positive

    private final boolean isAutoLogin;
    private final String readableName;

    LoginVerdict(boolean isAutoLogin, String readableName) {
        this.isAutoLogin = isAutoLogin;
        this.readableName = readableName;//format(this.name());
    }

    public final boolean isAutoLogin() {
        return isAutoLogin;
    }

    public final String readableName() {
        return readableName;
    }

    public final byte getId() {
        return (byte) this.ordinal();
    }
}