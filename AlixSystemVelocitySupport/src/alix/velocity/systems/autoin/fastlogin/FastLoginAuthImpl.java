package alix.velocity.systems.autoin.fastlogin;

import alix.velocity.Main;
import alix.velocity.systems.autoin.AuthAPI;
import alix.velocity.systems.events.FastLoginEvents;
import com.github.games647.fastlogin.velocity.FastLoginVelocity;

public final class FastLoginAuthImpl extends AuthAPI {

    private final FastLoginVelocity base;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public FastLoginAuthImpl() {
        this.base = (FastLoginVelocity) Main.pm.getPlugin("FastLogin").get();
        this.base.getCore().setAuthPluginHook(new AlixAuthImpl());
    }

    @Override
    public Object getEventListener() {
        return new FastLoginEvents();
    }
}
