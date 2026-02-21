package alix.common.utils.floodgate;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class GeyserUtil {

    private final boolean isGeyserPresent, isFloodgatePresent;

    public GeyserUtil(boolean isGeyserPresent, boolean isFloodgatePresent) {
        this.isGeyserPresent = isGeyserPresent;
        this.isFloodgatePresent = isFloodgatePresent;
    }

    public boolean isFloodgatePresent() {
        return isFloodgatePresent;
    }

    @Nullable
    public Object getBedrockPlayer(Player player) {
        if (!isFloodgatePresent) return null;
        return FloodgateAccess.getBedrockPlayer(player);
    }

    @Nullable
    public Object getBedrockPlayer(String username) {
        if (!isFloodgatePresent) return null;
        return FloodgateAccess.getBedrockPlayer(username);
    }

    @Nullable
    public Object getBedrockPlayer(Channel channel) {
        if (!isFloodgatePresent) return null;
        return FloodgateAccess.getBedrockPlayer(channel);
    }

    @NotNull
    public String getCorrectUsername(Channel channel, @NotNull String forNull) {
        if (!isFloodgatePresent) return forNull;
        return FloodgateAccess.getCorrectUsername(channel, forNull);
    }

    @Nullable
    public UUID getLinkedJavaUUID(@NotNull Channel channel) {
        if (!isFloodgatePresent) return null;
        return FloodgateAccess.getLinkedJavaUUID(channel);
    }

    public boolean isLinked(@NotNull Channel channel) {
        if (!isFloodgatePresent) return false;
        return FloodgateAccess.isLinked(channel);
    }

    public boolean isBedrock(@NotNull Channel channel) {
        //AlixCommonMain.logInfo("isFloodgatePresent && FloodgateAccess.isBedrock(channel)=" + (isFloodgatePresent && FloodgateAccess.isBedrock(channel)));
        return isGeyserPresent && FloodgateAccess.isGeyserWrapperClazz(channel) || isFloodgatePresent && FloodgateAccess.hasFloodgatePlayerAttr(channel);
    }
}