package shadow.systems.dependencies;

import org.bukkit.plugin.PluginManager;
import shadow.Main;
import shadow.systems.executors.packetevents.PacketEventsManager;

public final class Dependencies {

    public static final boolean isPacketEventsPresent, isProtocolLibPresent, isMultiverseCorePresent, isAnyViaPresent, isSkinsRestorerPresent;

    public static void initAdditional() {
        //if (pm.isPluginEnabled("LuckPerms")) LuckPermsExecutors.register();
        if (isPacketEventsPresent) PacketEventsManager.onEnable();
    }

    static {
        PluginManager pm = Main.pm;
        isPacketEventsPresent = pm.isPluginEnabled("packetevents");
        isProtocolLibPresent = pm.getPlugin("ProtocolLib") != null;
        isMultiverseCorePresent = pm.getPlugin("Multiverse-Core") != null;
        isSkinsRestorerPresent = pm.isPluginEnabled("SkinsRestorer");
        isAnyViaPresent = pm.getPlugin("ViaVersion") != null || pm.getPlugin("ViaBackwards") != null || pm.getPlugin("ViaRewind") != null;
    }

    private Dependencies() {
    }
}