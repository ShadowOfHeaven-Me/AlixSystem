package shadow.systems.login.result;

public enum LoginVerdict {

    REGISTER_PREMIUM(true),
    LOGIN_PREMIUM(true),
    IP_AUTO_LOGIN(true),
    DISALLOWED_NO_DATA(false),//register (for the first time)
    DISALLOWED_PASSWORD_RESET(false),//register (after a password reset)
    DISALLOWED_LOGIN_REQUIRED(false);//a login is required

    private final boolean isAutoLogin;

    LoginVerdict(boolean isAutoLogin) {
        this.isAutoLogin = isAutoLogin;
    }

    public final boolean isAutoLogin() {
        return isAutoLogin;
    }
}