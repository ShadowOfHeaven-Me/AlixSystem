package shadow.systems.dependencies;

import alix.common.utils.floodgate.GeyserUtil;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shadow.Main;

import java.util.UUID;

public final class Dependencies {

    public static final boolean //isPacketEventsPresent,
            isProtocolLibPresent, isMultiverseCorePresent, isAnyViaPresent, isSkinsRestorerPresent, isFloodgatePresent;
    public static final GeyserUtil util;

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
        util = new GeyserUtil(isFloodgatePresent);
    }

    //public static final String FLOODGATE_PREFIX = isFloodgatePresent ? util.PLAYER_PREFIX : null;

    @Nullable
    public static Object getBedrockPlayer(Player player) {
        return util.getBedrockPlayer(player);
    }

    @Nullable
    public static Object getBedrockPlayer(String username) {
        return util.getBedrockPlayer(username);
    }

    @Nullable
    public static Object getBedrockPlayer(Channel channel) {
        return util.getBedrockPlayer(channel);
    }

    @NotNull
    public static String getCorrectUsername(Channel channel, @NotNull String forNull) {
        return util.getCorrectUsername(channel, forNull);
    }

    @Nullable
    public static UUID getLinkedJavaUUID(@NotNull Channel channel) {
        return util.getLinkedJavaUUID(channel);
    }

    public static boolean isLinked(@NotNull Channel channel) {
        return util.isLinked(channel);
    }

    public static boolean isBedrock(@NotNull Channel channel) {
        return util.isBedrock(channel);
    }

    private Dependencies() {
    }
}