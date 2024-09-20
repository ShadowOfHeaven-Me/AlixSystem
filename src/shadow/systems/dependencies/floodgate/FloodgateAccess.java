package shadow.systems.dependencies.floodgate;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.geyser.network.netty.ChannelWrapper;
import org.jetbrains.annotations.Nullable;

public final class FloodgateAccess {

    public static final String PLAYER_PREFIX = FloodgateApi.getInstance().getPlayerPrefix();

    @Nullable
    public static FloodgatePlayer getBedrockPlayer(Player player) {
        return FloodgateApi.getInstance().getPlayer(player.getUniqueId());
    }

    public static boolean isBedrock(Channel channel) {
        return channel.getClass() == ChannelWrapper.class;
    }
}