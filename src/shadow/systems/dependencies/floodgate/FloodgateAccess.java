package shadow.systems.dependencies.floodgate;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.geyser.network.netty.ChannelWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FloodgateAccess {

    public static final String PLAYER_PREFIX = FloodgateApi.getInstance().getPlayerPrefix();
    private static final AttributeKey<FloodgatePlayer> floodgate_player = AttributeKey.valueOf("floodgate-player");

    @Nullable
    public static FloodgatePlayer getBedrockPlayer(Player player) {
        return FloodgateApi.getInstance().getPlayer(player.getUniqueId());
    }

    @NotNull
    public static String getCorrectUsername(Channel channel) {
        return channel.attr(floodgate_player).get().getCorrectUsername();
    }

    public static boolean isBedrock(Channel channel) {
        return channel.getClass() == ChannelWrapper.class;
    }

/*    public static boolean isAccountLinked(Channel channel) {
        return FloodgateApi.getInstance().getPlayerLink().isLinkedPlayer()
    }*/
}