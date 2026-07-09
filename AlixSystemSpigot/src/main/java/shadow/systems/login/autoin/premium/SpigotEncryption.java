package shadow.systems.login.autoin.premium;

import alix.common.data.PersistentUserData;
import com.github.retrooper.packetevents.PacketEvents;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;


public final class SpigotEncryption {

    public static Boolean isEncryptionEnabled(PersistentUserData data) {
        return switch (data.getPremiumData().getStatus()) {
            case NON_PREMIUM -> false;
            case PREMIUM -> true;
            case UNKNOWN -> isOnlineEncryptionEnabled(channel(data));
        };
    }

    public static Channel channel(PersistentUserData data) {
        var player = Bukkit.getPlayer(data.getName());
        if (player == null) return null;
        return (Channel) PacketEvents.getAPI().getPlayerManager().getChannel(player);
    }

    public static Boolean isOnlineEncryptionEnabled(Channel channel) {
        return channel == null ? null : channel.pipeline().context("decrypt") != null;
    }
}