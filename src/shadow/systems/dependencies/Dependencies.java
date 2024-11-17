package shadow.systems.dependencies;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
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

    //public static final String FLOODGATE_PREFIX = isFloodgatePresent ? FloodgateAccess.PLAYER_PREFIX : null;

    @Nullable
    public static Object getBedrockPlayer(Player player) {
        if (!isFloodgatePresent) return null;
        return FloodgateAccess.getBedrockPlayer(player);
    }

    @Nullable
    public static Object getBedrockPlayer(String username) {
        if (!isFloodgatePresent) return null;
        return FloodgateAccess.getBedrockPlayer(username);
    }

    @Nullable
    public static Object getBedrockPlayer(Channel channel) {
        if (!isFloodgatePresent) return null;
        return FloodgateAccess.getBedrockPlayer(channel);
    }

    @NotNull
    public static String getCorrectUsername(Channel channel, @NotNull String forNull) {
        if (!isFloodgatePresent) return forNull;
        return FloodgateAccess.getCorrectUsername(channel, forNull);
    }

    public static boolean isBedrock(@NotNull Channel channel) {
        return isFloodgatePresent && FloodgateAccess.isBedrock(channel);
    }

    private Dependencies() {
    }
}