package shadow.systems.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import shadow.Main;
import shadow.systems.dependencies.luckperms.LuckPermsExecutors;

public final class Dependencies {

    public static final boolean isPacketEventsPresent, isProtocolLibPresent;

    public static void initAdditional() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.isPluginEnabled("LuckPerms")) LuckPermsExecutors.register();
    }

    static {
        PluginManager pm = Main.pm;
        isPacketEventsPresent = pm.isPluginEnabled("packetevents");
        isProtocolLibPresent = pm.getPlugin("ProtocolLib") != null;
    }

    private Dependencies() {
    }
}