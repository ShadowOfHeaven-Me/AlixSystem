package alix.velocity.systems.autoin;

import alix.velocity.Main;
import alix.velocity.systems.autoin.fastlogin.FastLoginAuthImpl;

public abstract class AuthAPI {

    public static AuthAPI getAuthAPI() {
        if (Main.pm.isLoaded("FastLogin")) return new FastLoginAuthImpl();
        return null;
    }

    public abstract Object getEventListener();
}