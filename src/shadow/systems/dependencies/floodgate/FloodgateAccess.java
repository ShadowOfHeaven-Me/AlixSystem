package shadow.systems.dependencies.floodgate;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.Nullable;

public final class FloodgateAccess {

    public static final String PLAYER_PREFIX = FloodgateApi.getInstance().getPlayerPrefix();

    @Nullable
    public static FloodgatePlayer getBedrockPlayer(Player player) {
        return FloodgateApi.getInstance().getPlayer(player.getUniqueId());
    }
}