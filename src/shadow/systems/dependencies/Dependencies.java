package shadow.systems.dependencies;

import org.bukkit.plugin.PluginManager;
import shadow.Main;
import shadow.systems.executors.packetevents.PacketEventsAccess;

public final class Dependencies {

    public static final boolean isPacketEventsPresent, isProtocolLibPresent;

    public static void initAdditional() {
        //if (pm.isPluginEnabled("LuckPerms")) LuckPermsExecutors.register();
        if (isPacketEventsPresent) PacketEventsAccess.initAll();
    }

    static {
        PluginManager pm = Main.pm;
        isPacketEventsPresent = pm.isPluginEnabled("packetevents");
        isProtocolLibPresent = pm.getPlugin("ProtocolLib") != null;
    }

    private Dependencies() {
    }
}