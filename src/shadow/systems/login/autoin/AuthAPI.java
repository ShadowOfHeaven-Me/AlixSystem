package shadow.systems.login.autoin;

import org.bukkit.event.Listener;

public final class AuthAPI {

    private final LoginAuthenticator authenticator;
    private final Listener listener;

    private AuthAPI(LoginAuthenticator authenticator, Listener listener) {
        this.authenticator = authenticator;
        this.listener = listener;
    }

    public static AuthAPI getAuthenticatorAPI() {
        //if (Main.pm.isPluginEnabled("FastLogin")) return new AuthAPI(new FastLoginAuth(), new FastLoginExecutors());
        return null;
    }

    public LoginAuthenticator getAuthenticator() {
        return authenticator;
    }

    public Listener getListener() {
        return listener;
    }
}