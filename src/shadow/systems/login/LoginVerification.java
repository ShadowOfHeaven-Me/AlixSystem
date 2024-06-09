package shadow.systems.login;

import alix.common.data.Password;

public final class LoginVerification {

    //private final AlixGui alixGui;
    private final Password password;
    private final boolean phase1;

    public LoginVerification(Password password, boolean phase1) {
        this.password = password;
        this.phase1 = phase1;
    }

    public boolean isPasswordCorrect(String password) {
        return this.password.isEqualTo(password);
    }

    public boolean isPhase1() {
        return phase1;
    }
}