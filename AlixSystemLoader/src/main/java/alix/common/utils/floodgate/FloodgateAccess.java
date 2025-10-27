package alix.common.utils.floodgate;

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
    private static final Class<?> CHANNEL_WRAPPER_CLAZZ = CommonReflection.forName("org.geysermc.geyser.network.netty.ChannelWrapper");

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

    public static boolean isBedrock(Channel channel) {
        return channel.getClass() == CHANNEL_WRAPPER_CLAZZ;// || getBedrockPlayer(channel) != null;
        //channel.hasAttr(floodgate_player);
        // channel.getClass() == CHANNEL_WRAPPER_CLAZZ;
    }

    /*public static String getName(Channel channel, String name) {
        return (isBedrock(channel) ? PLAYER_PREFIX : "") + name;
    }*/

/*    public static boolean isAccountLinked(Channel channel) {
        return FloodgateApi.getInstance().getPlayerLink().isLinkedPlayer()
    }*/
}