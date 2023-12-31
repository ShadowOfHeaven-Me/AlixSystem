package shadow.utils.main.paper;

import alix.common.AlixCommonMain;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;
import shadow.systems.login.firewall.PaperChannelFireWall;
import shadow.utils.holders.methods.MethodProvider;

public final class PaperAccess {

    private static final Key key = Key.key("alixsystem");

    public static void initializeChannelListener() {
        AlixCommonMain.logInfo("Using Paper for FireWall Protection initialization");
        ChannelInitializeListenerHolder.addListener(key, new PaperChannelFireWall());
    }

    public static void unregisterChannelListener() {
        ChannelInitializeListenerHolder.removeListener(key);
    }

    private PaperAccess() {
    }
}