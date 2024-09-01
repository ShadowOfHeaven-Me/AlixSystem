package shadow.systems.dependencies;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;
import shadow.Main;
import shadow.systems.dependencies.floodgate.FloodgateAccess;

public final class Dependencies {

    public static final boolean //isPacketEventsPresent,
            isProtocolLibPresent, isMultiverseCorePresent, isAnyViaPresent, isSkinsRestorerPresent, isFloodgatePresent;

    public static void initAdditional() {
        //if (pm.isPluginEnabled("LuckPerms")) LuckPermsExecutors.register();
        //if (isPacketEventsPresent) PacketEventsManager.onEnable();
    }

    static {
        PluginManager pm = Main.pm;
        //isPacketEventsPresent = pm.isPluginEnabled("packetevents");
        isProtocolLibPresent = pm.getPlugin("ProtocolLib") != null;
        isMultiverseCorePresent = pm.getPlugin("Multiverse-Core") != null;
        isSkinsRestorerPresent = pm.isPluginEnabled("SkinsRestorer");
        isAnyViaPresent = pm.getPlugin("ViaVersion") != null || pm.getPlugin("ViaBackwards") != null || pm.getPlugin("ViaRewind") != null;
        isFloodgatePresent = pm.isPluginEnabled("floodgate");
    }

    public static final String FLOODGATE_PREFIX = isFloodgatePresent ? FloodgateAccess.PLAYER_PREFIX : null;

    @Nullable
    public static Object getBedrockPlayer(Player player) {
        if (!isFloodgatePresent) return null;
        return FloodgateAccess.getBedrockPlayer(player);
    }

    private Dependencies() {
    }
}