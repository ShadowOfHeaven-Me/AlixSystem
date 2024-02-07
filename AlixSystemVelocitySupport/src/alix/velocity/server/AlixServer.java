package alix.velocity.server;

import alix.velocity.Main;
import alix.velocity.server.impl.AlixVirtualServer;
import net.elytrium.limboapi.LimboAPI;
import net.elytrium.limboapi.api.Limbo;

public final class AlixServer {

    private static final AlixVirtualServer impl = new AlixVirtualServer(Main.instance);

    public static Limbo getLimbo() {
        return impl.getLimbo();
    }

    public static LimboAPI getAPI() {
        return impl.getAPI();
    }

    public static void onProxyInit() {
        impl.onProxyInit();
    }

/*    public static void init() {
    }*/
}