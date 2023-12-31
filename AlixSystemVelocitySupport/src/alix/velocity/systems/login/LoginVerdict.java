package alix.velocity.systems.login;

public enum LoginVerdict {

    DISALLOWED_(false),
    LOGIN_PREMIUM(true);

    private final boolean isAutoLogin;

    LoginVerdict(boolean isAutoLogin) {
        this.isAutoLogin = isAutoLogin;
    }

    public boolean isAutoLogin() {
        return isAutoLogin;
    }
}