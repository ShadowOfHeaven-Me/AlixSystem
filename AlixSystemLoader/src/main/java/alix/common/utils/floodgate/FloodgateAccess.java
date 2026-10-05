package alix.common.utils.floodgate;

import alix.common.AlixCommonMain;
import alix.common.reflection.CommonReflection;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.util.LinkedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

final class FloodgateAccess {

    //public static final String PLAYER_PREFIX = FloodgateApi.getInstance().getPlayerPrefix();
    private static final AttributeKey<FloodgatePlayer> floodgate_player = AttributeKey.valueOf("floodgate-player");

    @Nullable
    public static FloodgatePlayer getBedrockPlayer(Player player) {
        return FloodgateApi.getInstance().getPlayer(player.getUniqueId());
    }

    @Nullable
    public static FloodgatePlayer getBedrockPlayer(String username) {
        for (FloodgatePlayer floodgatePlayer : FloodgateApi.getInstance().getPlayers())
            if (floodgatePlayer.getCorrectUsername().equals(username))
                return floodgatePlayer;
        return null;
    }

    @Nullable
    public static FloodgatePlayer getBedrockPlayer(Channel channel) {
        return channel.hasAttr(floodgate_player) ? channel.attr(floodgate_player).get() : null;
    }

    @NotNull
    public static String getCorrectUsername(Channel channel, @NotNull String forNull) {
        FloodgatePlayer player = getBedrockPlayer(channel);
        return player != null ? player.getCorrectUsername() : forNull;
    }

    @Nullable
    public static UUID getLinkedJavaUUID(@NotNull Channel channel) {
        FloodgatePlayer player = getBedrockPlayer(channel);
        if (player == null) return null;

        LinkedPlayer linked = player.getLinkedPlayer();
        if (linked == null) return null;

        return linked.getJavaUniqueId();
    }

    public static boolean isLinked(@NotNull Channel channel) {
        FloodgatePlayer pl = getBedrockPlayer(channel);
        return pl != null && pl.isLinked();
    }

    public static boolean hasFloodgatePlayerAttr(Channel channel) {
        return getBedrockPlayer(channel) != null;
        //channel.hasAttr(floodgate_player);
    }

    public static boolean isGeyserWrapperClazz(Channel channel) {
        Class<?> clazz = LazyLoad.CHANNEL_WRAPPER_CLAZZ;
        return clazz != null && channel.getClass() == clazz;
    }

    private static final class LazyLoad {

        //https://github.com/GeyserMC/Geyser/blob/be6749dfbb233aaf3a428afcfc5b5905a38169a4/core/src/main/java/org/geysermc/geyser/network/java/ChannelWrapper.java
        //None of these hardcoded names are a public Geyser API, so a Geyser internal refactor can move this
        //class again at any point (as already happened once between these two names) - forNameOrNull()
        //degrades to null instead of throwing so a future rename doesn't crash every connection's
        //isBedrock() check, just silently disables this one detection path (isGeyserWrapperClazz() below)
        //until the name list here is updated; Floodgate's own attribute-based detection is unaffected.
        private static final Class<?> CHANNEL_WRAPPER_CLAZZ = CommonReflection.forNameOrNull(
                "org.geysermc.geyser.network.java.ChannelWrapper",//newest version
                "org.geysermc.geyser.network.netty.ChannelWrapper");

        static {
            if (CHANNEL_WRAPPER_CLAZZ == null)
                AlixCommonMain.logWarning("Could not locate Geyser's internal ChannelWrapper class under any known name - " +
                        "Bedrock player detection via this path is disabled until AlixSystem is updated for this Geyser version.");
        }
    }

    /*public static String getName(Channel channel, String name) {
        return (isBedrock(channel) ? PLAYER_PREFIX : "") + name;
    }*/

/*    public static boolean isAccountLinked(Channel channel) {
        return FloodgateApi.getInstance().getPlayerLink().isLinkedPlayer()
    }*/
}